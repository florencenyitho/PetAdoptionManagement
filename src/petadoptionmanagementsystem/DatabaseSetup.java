package petadoptionmanagementsystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the SQLite database schema on first run.
 * Call DatabaseSetup.initialize() before opening the main window.
 *
 * SQLite notes:
 *  - INTEGER PRIMARY KEY AUTOINCREMENT is the SQLite equivalent of AUTO_INCREMENT.
 *  - Booleans are stored as INTEGER (0 = false, 1 = true).
 *  - No CREATE DATABASE / USE statements needed — SQLite is a single file.
 */
public class DatabaseSetup {

    public static void initialize() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement()) {

            // Pets table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pets (
                    pet_id       INTEGER  PRIMARY KEY AUTOINCREMENT,
                    name         TEXT     NOT NULL,
                    species      TEXT     NOT NULL,
                    breed        TEXT,
                    age          INTEGER  NOT NULL,
                    gender       TEXT,
                    description  TEXT,
                    available    INTEGER  NOT NULL DEFAULT 1,
                    date_added   TEXT     DEFAULT (datetime('now','localtime'))
                )
            """);

            // Adopters table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS adopters (
                    adopter_id      INTEGER  PRIMARY KEY AUTOINCREMENT,
                    name            TEXT     NOT NULL,
                    phone           TEXT     NOT NULL,
                    email           TEXT,
                    address         TEXT,
                    date_registered TEXT     DEFAULT (datetime('now','localtime'))
                )
            """);

            // Adoptions table
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS adoptions (
                    adoption_id    INTEGER  PRIMARY KEY AUTOINCREMENT,
                    pet_id         INTEGER  NOT NULL,
                    adopter_id     INTEGER  NOT NULL,
                    adoption_date  TEXT     NOT NULL,
                    notes          TEXT,
                    FOREIGN KEY (pet_id)     REFERENCES pets(pet_id),
                    FOREIGN KEY (adopter_id) REFERENCES adopters(adopter_id)
                )
            """);
        }
    }
}
