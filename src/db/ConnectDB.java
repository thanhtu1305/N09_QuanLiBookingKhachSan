package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QLKS;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "Admin@123";

    private static Connection connection;

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Kết nối SQL Server thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Không tìm thấy JDBC driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Kết nối SQL Server thất bại!");
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        Connection con = getConnection();
        if (con != null) {
            System.out.println("TEST OK");
        } else {
            System.out.println("TEST FAIL");
        }
    }
}