package ris.db.dao;

import lombok.SneakyThrows;
import org.openstreetmap.osm._0.Tag;
import ris.db.ConnectionManager;
import ris.db.exceptions.RollbackOccuredException;
import ris.db.info.PropertyContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TagDAO {
    private final Connection connection;
    private static final String DROP = "DROP TABLE IF EXISTS tags;";
    private static final String INSERT_TAG_PREPARED = "INSERT INTO tags(k, v, nodeid) values(?, ?, ?)";
    public static final String INSERT_TAG = "INSERT INTO tags(k, v, nodeid) values(%s, %d)";
    private PreparedStatement preparedStatement;
    private final Statement statement;
    private int counter;
    private final NodeDAO nodeDAO;

    public TagDAO(NodeDAO nodeDAO) throws SQLException {
        connection = ConnectionManager.getConnection();
        statement = connection.createStatement();
        this.nodeDAO = nodeDAO;
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
    public void saveTag(Tag tag, long nodeId) {
        try {
            if (tag.getV().contains("'")) {
                tag.setV(tag.getV().replace("'", "''"));
            }
            String sql = String.format(INSERT_TAG, tag, nodeId);
            statement.executeUpdate(sql);
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("saveTag", sqlException);
        }
    }

    @SneakyThrows
    public void saveTagPrepared(Tag tag, long nodeId) {
        try {
            setTagPrepared(preparedStatement, tag, nodeId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("saveTagPrepared", sqlException);
        }
    }

    public void initializePreparedStatement() throws SQLException {
        preparedStatement = connection.prepareStatement(INSERT_TAG_PREPARED);
    }

    public void closeTagPreparedStatement() throws SQLException {
        if (!preparedStatement.isClosed()) {
            preparedStatement.close();
        }
    }

    @SneakyThrows
    public void saveTagPreparedWithBatch(Tag tag, long batchId) {
        setTagPrepared(preparedStatement, tag, batchId);
        preparedStatement.addBatch();
        if (++counter == PropertyContainer.TAG_BATCH_SIZE) {
            nodeDAO.executeBatch(); // need to do this to have data consistency (foreign key)
            executeBatch();
        }
    }

    public void executeBatch() throws SQLException {
        try {
            preparedStatement.executeBatch();
            counter = 0;
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
            throw new RollbackOccuredException("executeBatch", sqlException);
        }
    }

    private void setTagPrepared(PreparedStatement statement, Tag tag, long batchId) throws SQLException {
        statement.setString(1, tag.getK());
        statement.setString(2, tag.getV());
        statement.setLong(3, batchId);
    }
}
