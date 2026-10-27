package petadoptionmanagementsystem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Adopter operations.
 */
public class AdopterDAO {

    public boolean addAdopter(Adopter adopter) throws SQLException {
        String sql = "INSERT INTO adopters (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, adopter.getName());
            ps.setString(2, adopter.getPhone());
            ps.setString(3, adopter.getEmail());
            ps.setString(4, adopter.getAddress());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Adopter> getAllAdopters() throws SQLException {
        List<Adopter> list = new ArrayList<>();
        String sql = "SELECT * FROM adopters ORDER BY name";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Adopter> searchAdopters(String keyword) throws SQLException {
        List<Adopter> list = new ArrayList<>();
        String sql = "SELECT * FROM adopters WHERE name LIKE ? OR phone LIKE ? ORDER BY name";
        String kw = "%" + keyword + "%";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Adopter getAdopterById(int adopterId) throws SQLException {
        String sql = "SELECT * FROM adopters WHERE adopter_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, adopterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean updateAdopter(Adopter adopter) throws SQLException {
        String sql = "UPDATE adopters SET name=?, phone=?, email=?, address=? WHERE adopter_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, adopter.getName());
            ps.setString(2, adopter.getPhone());
            ps.setString(3, adopter.getEmail());
            ps.setString(4, adopter.getAddress());
            ps.setInt   (5, adopter.getAdopterId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAdopter(int adopterId) throws SQLException {
        String sql = "DELETE FROM adopters WHERE adopter_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, adopterId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAdopters() throws SQLException {
        String sql = "SELECT COUNT(*) FROM adopters";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Adopter mapRow(ResultSet rs) throws SQLException {
        return new Adopter(
            rs.getInt   ("adopter_id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getString("date_registered")
        );
    }
}
