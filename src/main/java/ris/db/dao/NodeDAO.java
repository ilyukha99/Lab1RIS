package ris.db.dao;

import lombok.SneakyThrows;
import org.openstreetmap.osm._0.Node;
import ris.db.ConnectionManager;
import ris.db.exceptions.RollbackOccuredException;
import ris.db.info.PropertyContainer;

import java.sql.*;

public class NodeDAO {
    private final Connection connection;
    private static final String DROP = "DROP TABLE IF EXISTS nodes;";
    private static final String INSERT_NODE_PREPARED = "INSERT INTO nodes values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_NODE = "INSERT INTO nodes values(%s);";
    private PreparedStatement preparedStatement;
    private final Statement statement;
    private int counter;
    private long executionTime;

    public NodeDAO() throws SQLException {
        connection = ConnectionManager.getConnection();
        statement = connection.createStatement();
    }

    public void dropTableIfExists() throws SQLException {
        try {
            statement.execute(DROP);
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("dropTableIfExists", sqlException);
        }
    }

    @SneakyThrows
    public void saveNode(Node node) {
        long start = System.nanoTime();
        try {
            if (node.getUserName().contains("'")) {
                node.setUserName(node.getUserName().replace("'", "''"));
            }
            String sql = String.format(INSERT_NODE, node);
            statement.executeUpdate(sql);
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("saveNode", sqlException);
        }
        executionTime += System.nanoTime() - start;
    }

    @SneakyThrows
    public void saveNodePrepared(Node node) {
        long start = System.nanoTime();
        try {
            setNodePrepared(preparedStatement, node);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("saveNodePrepared", sqlException);
        }
        executionTime += System.nanoTime() - start;
    }

    private void setNodePrepared(PreparedStatement statement, Node node) throws SQLException {
        statement.setLong(1, node.getId());
        statement.setDouble(2, node.getLat());
        statement.setDouble(3, node.getLon());
        statement.setString(4, node.getUserName());
        statement.setLong(5, node.getUid());
        statement.setBoolean(6, node.isVisible());
        statement.setLong(7, node.getVersion());
        statement.setLong(8, node.getChangeSet());
        statement.setTimestamp(9, new Timestamp(
                node.getTimestamp().toGregorianCalendar().getTimeInMillis()));
    }

    public void initializePreparedStatement() throws SQLException {
        long start = System.nanoTime();
        preparedStatement = connection.prepareStatement(INSERT_NODE_PREPARED);
        executionTime += System.nanoTime() - start;
    }

    public void closeNodePreparedStatement() throws SQLException {
        long start = System.nanoTime();
        if (!preparedStatement.isClosed()) {
            preparedStatement.close();
        }
        executionTime += System.nanoTime() - start;
    }

    public void executeBatch() throws SQLException {
        long start = System.nanoTime();
        try {
            preparedStatement.executeBatch();
            counter = 0;
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("saveNodePreparedWithBatch", sqlException);
        }
        executionTime += System.nanoTime() - start;
    }

    @SneakyThrows
    public void saveNodePreparedWithBatch(Node node) {
        long start = System.nanoTime();
        setNodePrepared(preparedStatement, node);
        preparedStatement.addBatch();
        executionTime += System.nanoTime() - start;
        if (++counter == PropertyContainer.NODE_BATCH_SIZE) {
            executeBatch();
        }
    }

    public long getExecutionTime() {
        return executionTime;
    }
}
