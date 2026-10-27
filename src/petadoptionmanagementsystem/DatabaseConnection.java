package petadoptionmanagementsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the SQLite database connection.
 * The database file is created automatically in the user's home directory
 * under a subfolder called "PetAdoptionDB".
 */
public class DatabaseConnection {

    // Database file stored next to the running application
    private static final String DB_FILE = "pet_adoption.db";
    private static final String DB_URL  = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection = null;

    /** Returns a singleton Connection, creating it if necessary. */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                // Enable foreign key enforcement (off by default in SQLite)
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "SQLite JDBC driver not found. Add sqlite-jdbc-*.jar to the project libraries.", e);
            }
        }
        return connection;
    }

    /** Closes the connection cleanly. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
