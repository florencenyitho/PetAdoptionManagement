package petadoptionmanagementsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Adoption records.
 */
public class AdoptionDAO {

    /**
     * Records an adoption: inserts into adoptions and marks pet unavailable.
     * Both operations run inside a single transaction.
     */
    public boolean recordAdoption(Adoption adoption) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // Insert adoption record
            String sql = "INSERT INTO adoptions (pet_id, adopter_id, adoption_date, notes) "
                       + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt   (1, adoption.getPetId());
                ps.setInt   (2, adoption.getAdopterId());
                ps.setString(3, adoption.getAdoptionDate());
                ps.setString(4, adoption.getNotes());
                ps.executeUpdate();
            }
            // Mark pet as adopted
            String update = "UPDATE pets SET available = 0 WHERE pet_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setInt(1, adoption.getPetId());
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Full adoption history with joined pet and adopter names. */
    public List<Adoption> getAllAdoptions() throws SQLException {
        List<Adoption> list = new ArrayList<>();
        String sql = """
            SELECT ad.adoption_id, ad.pet_id, ad.adopter_id, ad.adoption_date, ad.notes,
                   p.name  AS pet_name,
                   a.name  AS adopter_name,
                   a.phone AS adopter_phone
            FROM adoptions ad
            JOIN pets     p ON p.pet_id     = ad.pet_id
            JOIN adopters a ON a.adopter_id = ad.adopter_id
            ORDER BY ad.adoption_date DESC
        """;
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Adoptions for a specific adopter. */
    public List<Adoption> getAdoptionsByAdopter(int adopterId) throws SQLException {
        List<Adoption> list = new ArrayList<>();
        String sql = """
            SELECT ad.adoption_id, ad.pet_id, ad.adopter_id, ad.adoption_date, ad.notes,
                   p.name  AS pet_name,
                   a.name  AS adopter_name,
                   a.phone AS adopter_phone
            FROM adoptions ad
            JOIN pets     p ON p.pet_id     = ad.pet_id
            JOIN adopters a ON a.adopter_id = ad.adopter_id
            WHERE ad.adopter_id = ?
            ORDER BY ad.adoption_date DESC
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, adopterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Adoptions within a date range (yyyy-MM-dd). */
    public List<Adoption> getAdoptionsByDateRange(String from, String to) throws SQLException {
        List<Adoption> list = new ArrayList<>();
        String sql = """
            SELECT ad.adoption_id, ad.pet_id, ad.adopter_id, ad.adoption_date, ad.notes,
                   p.name  AS pet_name,
                   a.name  AS adopter_name,
                   a.phone AS adopter_phone
            FROM adoptions ad
            JOIN pets     p ON p.pet_id     = ad.pet_id
            JOIN adopters a ON a.adopter_id = ad.adopter_id
            WHERE ad.adoption_date BETWEEN ? AND ?
            ORDER BY ad.adoption_date DESC
        """;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countAdoptions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM adoptions";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Monthly adoption counts for the last 12 months.
     * Uses SQLite's strftime() instead of MySQL's DATE_FORMAT().
     * Returns rows: {month (yyyy-MM), count}
     */
    public List<Object[]> monthlyAdoptionStats() throws SQLException {
        List<Object[]> result = new ArrayList<>();
        String sql = """
            SELECT strftime('%Y-%m', adoption_date) AS month,
                   COUNT(*) AS cnt
            FROM adoptions
            GROUP BY month
            ORDER BY month DESC
            LIMIT 12
        """;
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Object[]{ rs.getString("month"), rs.getInt("cnt") });
            }
        }
        return result;
    }

    // ── Private mapper ───────────────────────────────────────────────────────

    private Adoption mapRow(ResultSet rs) throws SQLException {
        Adoption a = new Adoption(
            rs.getInt   ("adoption_id"),
            rs.getInt   ("pet_id"),
            rs.getInt   ("adopter_id"),
            rs.getString("adoption_date"),
            rs.getString("notes")
        );
        a.setPetName     (rs.getString("pet_name"));
        a.setAdopterName (rs.getString("adopter_name"));
        a.setAdopterPhone(rs.getString("adopter_phone"));
        return a;
    }
}
