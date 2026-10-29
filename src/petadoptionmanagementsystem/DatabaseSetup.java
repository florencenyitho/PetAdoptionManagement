package petadoptionmanagementsystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates and migrates the SQLite database schema.
 * Safe to run on every startup — uses CREATE TABLE IF NOT EXISTS
 * and ALTER TABLE ... ADD COLUMN IF NOT EXISTS (via try/catch for SQLite).
 */
public class DatabaseSetup {

    public static void initialize() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (Statement st = conn.createStatement()) {

            // ── Pets ─────────────────────────────────────────────────────────
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pets (
                    pet_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name         TEXT    NOT NULL,
                    species      TEXT    NOT NULL,
                    breed        TEXT,
                    age          INTEGER NOT NULL,
                    gender       TEXT,
                    description  TEXT,
                    available    INTEGER NOT NULL DEFAULT 1,
                    photo_path   TEXT,
                    date_added   TEXT    DEFAULT (datetime('now','localtime'))
                )
            """);

            // Migrate existing databases
            addColumnIfMissing(st, "pets", "photo_path", "TEXT");

            // ── Adopters ─────────────────────────────────────────────────────
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS adopters (
                    adopter_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    name            TEXT    NOT NULL,
                    phone           TEXT    NOT NULL,
                    email           TEXT,
                    address         TEXT,
                    date_registered TEXT    DEFAULT (datetime('now','localtime'))
                )
            """);

            // ── Adoptions ────────────────────────────────────────────────────
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS adoptions (
                    adoption_id    INTEGER PRIMARY KEY AUTOINCREMENT,
                    pet_id         INTEGER NOT NULL,
                    adopter_id     INTEGER NOT NULL,
                    adoption_date  TEXT    NOT NULL,
                    notes          TEXT,
                    adoption_fee   REAL    NOT NULL DEFAULT 0.0,
                    payment_status TEXT    NOT NULL DEFAULT 'Pending',
                    FOREIGN KEY (pet_id)     REFERENCES pets(pet_id),
                    FOREIGN KEY (adopter_id) REFERENCES adopters(adopter_id)
                )
            """);

            // Migrate existing databases — add fee columns if they don't exist yet
            addColumnIfMissing(st, "adoptions", "adoption_fee",   "REAL NOT NULL DEFAULT 0.0");
            addColumnIfMissing(st, "adoptions", "payment_status", "TEXT NOT NULL DEFAULT 'Pending'");

            // ── Fee schedule (default fees per species) ───────────────────────
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS fee_schedule (
                    species  TEXT PRIMARY KEY,
                    fee      REAL NOT NULL DEFAULT 50.0
                )
            """);

            // Seed default fees for common species in Uganda Shillings (INSERT OR IGNORE = won't overwrite)
            String[] seeds = {
                "INSERT OR IGNORE INTO fee_schedule VALUES ('Dog',    150000)",
                "INSERT OR IGNORE INTO fee_schedule VALUES ('Cat',    100000)",
                "INSERT OR IGNORE INTO fee_schedule VALUES ('Bird',    50000)",
                "INSERT OR IGNORE INTO fee_schedule VALUES ('Rabbit',  75000)",
                "INSERT OR IGNORE INTO fee_schedule VALUES ('Fish',    20000)",
                "INSERT OR IGNORE INTO fee_schedule VALUES ('Other',   60000)"
            };
            for (String seed : seeds) st.executeUpdate(seed);
        }
    }

    /**
     * Adds a column to an existing table only if it doesn't already exist.
     * SQLite doesn't support IF NOT EXISTS on ALTER TABLE, so we catch the error.
     */
    private static void addColumnIfMissing(Statement st, String table,
                                            String column, String definition) {
        try {
            st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException ignored) {
            // Column already exists — safe to ignore
        }
    }
}
