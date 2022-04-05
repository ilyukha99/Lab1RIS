package ris.db;

import lombok.extern.slf4j.Slf4j;
import ris.db.exceptions.ConnectionEstablishException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class ConnectionManager {
    private static Connection connection;

    private ConnectionManager() {}

    public static Connection getConnection(String propertyFile, String urlProp,
                                           String loginProp, String passwordProb) {
        if (connection == null) {
            connection = createConnection(propertyFile, urlProp, loginProp, passwordProb);
        }
        return connection;
    }

    private static Connection createConnection(String propertyFile, String urlProp,
                                    String loginProp, String passwordProb) {
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(propertyFile)) {
            properties.load(fileInputStream);
            try (Connection connection = DriverManager.getConnection(
                    properties.getProperty(urlProp),
                    properties.getProperty(loginProp),
                    properties.getProperty(passwordProb)
            )) {

                if (connection == null) {
                    log.error("Can not establish the connection");
                } else {
                    log.info("Connection made");
                    return connection;
                }
            }
        } catch (IOException | SQLException exception) {
            exception.printStackTrace();
        }
        throw new ConnectionEstablishException("Unable to connect. Check your db server and authentication data");
    }
}
