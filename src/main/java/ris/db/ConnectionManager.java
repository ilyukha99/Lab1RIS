package ris.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ris.db.exceptions.ConnectionEstablishException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static ris.db.PropertyContainer.*;

public class ConnectionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("MainLogger");
    private static Connection connection;

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = createConnection();
        }
        return connection;
    }

    private static Connection createConnection() {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(PROPERTY_FILE)) {
            properties.load(fileInputStream);
            Connection connection = DriverManager.getConnection(
                    properties.getProperty(URL_PROPERTY),
                    properties.getProperty(LOGIN_PROPERTY),
                    properties.getProperty(PASSWORD_PROPERTY));

                if (connection != null) {
                    LOGGER.info("Connection made");
                    connection.setAutoCommit(false);
                    return connection;
                }
            } catch (IOException | SQLException exception) {
            exception.printStackTrace();
        }
        LOGGER.error("Can not establish the connection");
        throw new ConnectionEstablishException("Unable to connect. Check your db server and authentication data.");
    }

    public static void closeConnection() throws SQLException {
        if (!connection.isClosed()) {
            connection.close();
        }
    }
}
