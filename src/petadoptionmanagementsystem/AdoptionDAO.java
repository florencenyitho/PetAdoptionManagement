package petadoptionmanagementsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Adoption records.
 * Handles fees, payment status, and revenue reporting.
 */
public class AdoptionDAO {

    // ── Record adoption ───────────────────────────────────────────────────────

    public boolean recordAdoption(Adoption adoption) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String sql = """
                INSERT INTO adoptions
                    (pet_id, adopter_id, adoption_date, notes, adoption_fee, payment_status)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt   (1, adoption.getPetId());
                ps.setInt   (2, adoption.getAdopterId());
                ps.setString(3, adoption.getAdoptionDate());
                ps.setString(4, adoption.getNotes());
                ps.setDouble(5, adoption.getAdoptionFee());
                ps.setString(6, adoption.getPaymentStatus());
                ps.executeUpdate();
            }
            // Mark pet unavailable
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE pets SET available = 0 WHERE pet_id = ?")) {
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

    // ── Update payment status ─────────────────────────────────────────────────

    public boolean updatePaymentStatus(int adoptionId, String status) throws SQLException {
        String sql = "UPDATE adoptions SET payment_status = ? WHERE adoption_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt   (2, adoptionId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Query adoptions ───────────────────────────────────────────────────────

    public List<Adoption> getAllAdoptions() throws SQLException {
        return query("""
            SELECT ad.adoption_id, ad.pet_id, ad.adopter_id,
                   ad.adoption_date, ad.notes, ad.adoption_fee, ad.payment_status,
                   p.name    AS pet_name,   p.species AS pet_species,
                   a.name    AS adopter_name, a.phone AS adopter_phone
            FROM adoptions ad
            JOIN pets     p ON p.pet_id     = ad.pet_id
            JOIN adopters a ON a.adopter_id = ad.adopter_id
            ORDER BY ad.adoption_date DESC
        """, null, null);
    }

    public List<Adoption> getAdoptionsByDateRange(String from, String to) throws SQLException {
        return query("""
            SELECT ad.adoption_id, ad.pet_id, ad.adopter_id,
                   ad.adoption_date, ad.notes, ad.adoption_fee, ad.payment_status,
                   p.name    AS pet_name,   p.species AS pet_species,
                   a.name    AS adopter_name, a.phone AS adopter_phone
            FROM adoptions ad
            JOIN pets     p ON p.pet_id     = ad.pet_id
            JOIN adopters a ON a.adopter_id = ad.adopter_id
            WHERE ad.adoption_date BETWEEN ? AND ?
            ORDER BY ad.adoption_date DESC
        """, from, to);
    }

    public List<Adoption> getAdoptionsByAdopter(int adopterId) throws SQLException {
        List<Adoption> list = new ArrayList<>();
        String sql = """
            SELECT ad.adoption_id, ad.pet_id, ad.adopter_id,
                   ad.adoption_date, ad.notes, ad.adoption_fee, ad.payment_status,
                   p.name    AS pet_name,   p.species AS pet_species,
                   a.name    AS adopter_name, a.phone AS adopter_phone
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

    // ── Stats ─────────────────────────────────────────────────────────────────

    public int countAdoptions() throws SQLException {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM adoptions")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Total revenue from all Paid adoptions. */
    public double totalRevenue() throws SQLException {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT COALESCE(SUM(adoption_fee), 0) FROM adoptions WHERE payment_status = 'Paid'")) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    /** Total outstanding (Pending) fees. */
    public double totalPending() throws SQLException {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT COALESCE(SUM(adoption_fee), 0) FROM adoptions WHERE payment_status = 'Pending'")) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    /** Monthly stats: {month, adoptions, revenue} */
    public List<Object[]> monthlyAdoptionStats() throws SQLException {
        List<Object[]> result = new ArrayList<>();
        String sql = """
            SELECT strftime('%Y-%m', adoption_date)    AS month,
                   COUNT(*)                             AS cnt,
                   COALESCE(SUM(adoption_fee), 0)       AS revenue
            FROM adoptions
            GROUP BY month
            ORDER BY month DESC
            LIMIT 12
        """;
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Object[]{
                    rs.getString("month"),
                    rs.getInt("cnt"),
                    rs.getDouble("revenue")
                });
            }
        }
        return result;
    }

    // ── Fee schedule ─────────────────────────────────────────────────────────

    /** Returns the configured fee for a species, or 50.0 if not found. */
    public double getFeeForSpecies(String species) throws SQLException {
        String sql = "SELECT fee FROM fee_schedule WHERE LOWER(species) = LOWER(?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, species);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("fee");
            }
        }
        // Fallback to "Other"
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT fee FROM fee_schedule WHERE species = 'Other'")) {
            if (rs.next()) return rs.getDouble("fee");
        }
        return 50.0;
    }

    /** All rows from fee_schedule. Returns {species, fee}. */
    public List<Object[]> getAllFees() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT species, fee FROM fee_schedule ORDER BY species")) {
            while (rs.next())
                list.add(new Object[]{ rs.getString("species"), rs.getDouble("fee") });
        }
        return list;
    }

    /** Upsert a fee for a species. */
    public boolean setFee(String species, double fee) throws SQLException {
        String sql = "INSERT INTO fee_schedule(species, fee) VALUES(?,?) "
                   + "ON CONFLICT(species) DO UPDATE SET fee = excluded.fee";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, species);
            ps.setDouble(2, fee);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private List<Adoption> query(String sql, String p1, String p2) throws SQLException {
        List<Adoption> list = new ArrayList<>();
        if (p1 == null) {
            try (Statement st = DatabaseConnection.getConnection().createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } else {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, p1);
                ps.setString(2, p2);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Adoption mapRow(ResultSet rs) throws SQLException {
        Adoption a = new Adoption(
            rs.getInt   ("adoption_id"),
            rs.getInt   ("pet_id"),
            rs.getInt   ("adopter_id"),
            rs.getString("adoption_date"),
            rs.getString("notes"),
            rs.getDouble("adoption_fee"),
            rs.getString("payment_status")
        );
        a.setPetName     (rs.getString("pet_name"));
        a.setPetSpecies  (rs.getString("pet_species"));
        a.setAdopterName (rs.getString("adopter_name"));
        a.setAdopterPhone(rs.getString("adopter_phone"));
        return a;
    }
}
