package ris.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {
    private static final String CREATE_NODE_TABLE =
            "CREATE TABLE IF NOT EXISTS nodes(id bigint primary key, lat double precision not null, " +
                    "lon double precision not null, userName text not null, uid bigint not null, " +
                    "visible boolean default false, version bigint not null, changeSet bigint not null, " +
                    "timeStamp timestamp not null);";

    private static final String CREATE_TAG_TABLE =
            "CREATE TABLE IF NOT EXISTS tags(id serial primary key, k text not null, " +
                    "v text not null, nodeId bigint references nodes(id));";

    private final Connection connection;

    public DBInitializer() throws SQLException {
        connection = ConnectionManager.getConnection();
    }

    public void createBDStructure() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_NODE_TABLE);
            statement.execute(CREATE_TAG_TABLE);
            connection.commit();
        } catch (SQLException exception) {
            exception.printStackTrace();
            connection.rollback();
        }
    }
}
