package petadoptionmanagementsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Pet operations.
 */
public class PetDAO {

    // ── CREATE ──────────────────────────────────────────────────────────────

    public boolean addPet(Pet pet) throws SQLException {
        String sql = "INSERT INTO pets (name, species, breed, age, gender, description, available) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, pet.getName());
            ps.setString(2, pet.getSpecies());
            ps.setString(3, pet.getBreed());
            ps.setInt   (4, pet.getAge());
            ps.setString(5, pet.getGender());
            ps.setString(6, pet.getDescription());
            return ps.executeUpdate() > 0;
        }
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<Pet> getAllPets() throws SQLException {
        List<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pets ORDER BY name";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Pet> getAvailablePets() throws SQLException {
        List<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pets WHERE available = 1 ORDER BY name";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Searches pets by name, species, or breed (case-insensitive).
     */
    public List<Pet> searchPets(String keyword) throws SQLException {
        List<Pet> list = new ArrayList<>();
        String sql = "SELECT * FROM pets WHERE "
                   + "name LIKE ? OR species LIKE ? OR breed LIKE ? "
                   + "ORDER BY name";
        String kw = "%" + keyword + "%";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Pet getPetById(int petId) throws SQLException {
        String sql = "SELECT * FROM pets WHERE pet_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, petId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public boolean updatePet(Pet pet) throws SQLException {
        String sql = "UPDATE pets SET name=?, species=?, breed=?, age=?, gender=?, description=?, available=? "
                   + "WHERE pet_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString (1, pet.getName());
            ps.setString (2, pet.getSpecies());
            ps.setString (3, pet.getBreed());
            ps.setInt    (4, pet.getAge());
            ps.setString (5, pet.getGender());
            ps.setString (6, pet.getDescription());
            ps.setInt    (7, pet.isAvailable() ? 1 : 0);
            ps.setInt    (8, pet.getPetId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Marks a pet as adopted (available = 0). */
    public boolean markAsAdopted(int petId) throws SQLException {
        String sql = "UPDATE pets SET available = 0 WHERE pet_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, petId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    public boolean deletePet(int petId) throws SQLException {
        String sql = "DELETE FROM pets WHERE pet_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, petId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── REPORT helpers ───────────────────────────────────────────────────────

    public int countAvailable() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE available = 1";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countAdopted() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE available = 0";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Returns count per species: {species, count} */
    public List<Object[]> countBySpecies() throws SQLException {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT species, COUNT(*) AS cnt FROM pets GROUP BY species ORDER BY cnt DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Object[]{ rs.getString("species"), rs.getInt("cnt") });
            }
        }
        return result;
    }

    // ── Private mapper ───────────────────────────────────────────────────────

    private Pet mapRow(ResultSet rs) throws SQLException {
        return new Pet(
            rs.getInt   ("pet_id"),
            rs.getString("name"),
            rs.getString("species"),
            rs.getString("breed"),
            rs.getInt   ("age"),
            rs.getString("gender"),
            rs.getString("description"),
            rs.getInt   ("available") == 1,
            rs.getString("date_added")
        );
    }
}
