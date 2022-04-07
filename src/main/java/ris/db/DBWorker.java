package ris.db;

import org.openstreetmap.osm._0.Node;

import java.sql.*;

public class DBWorker {
    private final Connection connection;
    private static final String TRUNCATE = "truncate table tags; truncate table nodes;";
    private static final String INSERT_NODE_PREPARED = "INSERT INTO nodes " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public DBWorker() throws SQLException {
        connection = ConnectionManager.getConnection();
    }

    public void truncateTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(TRUNCATE);
        }
    }

    public void saveNode(Node node) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String sql = "INSERT INTO nodes values(" + node + ");";
            statement.executeUpdate(sql);
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
        }
    }

    public void saveNodePrepared(Node node) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_NODE_PREPARED)) {
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
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException sqlException) {
            connection.rollback();
        }
    }
}
