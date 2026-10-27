package petadoptionmanagementsystem;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Application entry point.
 */
public class PetAdoptionManagementSystem {

    public static void main(String[] args) {
        // Set look-and-feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            // Initialize database schema
            try {
                DatabaseSetup.initialize();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to initialize the SQLite database.\n\n"
                        + e.getMessage()
                        + "\n\nMake sure sqlite-jdbc-*.jar is added to the project Libraries.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
