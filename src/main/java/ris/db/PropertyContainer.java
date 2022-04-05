package ris.db;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyContainer {
    public static final String PROPERTY_FILE = "src/main/resources/db.properties";
    public static final String URL_PROPERTY = "db.host";
    public static final String LOGIN_PROPERTY = "db.login";
    public static final String PASSWORD_PROPERTY = "db.password";
}
