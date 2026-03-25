package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class ConnectDB {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "1433";
    private static final String DEFAULT_DATABASE = "QLKS";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String DEFAULT_ENCRYPT = "true";
    private static final String DEFAULT_TRUST_SERVER_CERTIFICATE = "true";

    private static Connection connection;

    private ConnectDB() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                String url = buildJdbcUrl();
                String user = getConfig("DB_USER", DEFAULT_USER);
                String password = getConfig("DB_PASSWORD", DEFAULT_PASSWORD);

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Ket noi SQL Server thanh cong: " + url);
            }
        } catch (SQLException e) {
            System.out.println("Ket noi SQL Server that bai.");
            System.out.println("Kiem tra lai DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD va cau hinh SQL Server.");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Connection con = getConnection();
        if (con != null) {
            System.out.println("TEST OK");
        } else {
            System.out.println("TEST FAIL");
        }
    }

    private static String buildJdbcUrl() {
        String host = getConfig("DB_HOST", DEFAULT_HOST);
        String port = getConfig("DB_PORT", DEFAULT_PORT);
        String database = getConfig("DB_NAME", DEFAULT_DATABASE);
        String encrypt = getConfig("DB_ENCRYPT", DEFAULT_ENCRYPT);
        String trustServerCertificate =
                getConfig("DB_TRUST_SERVER_CERTIFICATE", DEFAULT_TRUST_SERVER_CERTIFICATE);

        return "jdbc:sqlserver://"
                + host
                + ":"
                + port
                + ";databaseName="
                + database
                + ";encrypt="
                + encrypt
                + ";trustServerCertificate="
                + trustServerCertificate;
    }

    private static String getConfig(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return Objects.requireNonNull(defaultValue);
    }
}
