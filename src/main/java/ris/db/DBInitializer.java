package ris.db;

import java.sql.Connection;
import static ris.db.PropertyContainer.*;

public class DBInitializer {

    public void createBDStructure() {
        final Connection connection = ConnectionManager.getConnection(
                PROPERTY_FILE, URL_PROPERTY, LOGIN_PROPERTY, PASSWORD_PROPERTY);

    }
}
