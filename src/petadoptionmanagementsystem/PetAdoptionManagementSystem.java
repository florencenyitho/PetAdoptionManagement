package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Application entry point.
 * Activates FlatLaf Look & Feel before building the UI.
 */
public class PetAdoptionManagementSystem {

    public static void main(String[] args) {

        // ── FlatLaf Look & Feel ──────────────────────────────────────────────
        FlatLightLaf.setup();

        // Global UI tweaks (CSS-like property overrides)
        UIManager.put("defaultFont",               new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.arc",                10);           // rounded corners
        UIManager.put("Component.arc",             8);
        UIManager.put("TextComponent.arc",         6);
        UIManager.put("Button.margin",             new Insets(6, 14, 6, 14));
        UIManager.put("TabbedPane.selectedBackground", new Color(230, 242, 255));
        UIManager.put("TabbedPane.underlineColor",     new Color(52, 152, 219));
        UIManager.put("TabbedPane.tabHeight",          38);
        UIManager.put("Table.rowHeight",               28);
        UIManager.put("Table.showHorizontalLines",     true);
        UIManager.put("Table.showVerticalLines",       false);
        UIManager.put("Table.alternateRowColor",       new Color(245, 248, 252));
        UIManager.put("ScrollBar.thumbArc",            999);
        UIManager.put("ScrollBar.width",               10);
        UIManager.put("TextField.placeholderForeground", new Color(160, 160, 160));
        UIManager.put("ProgressBar.arc",               6);

        SwingUtilities.invokeLater(() -> {
            // Initialize database schema
            try {
                DatabaseSetup.initialize();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "<html><b>Failed to initialize the SQLite database.</b><br><br>"
                        + e.getMessage()
                        + "<br><br>Make sure sqlite-jdbc-*.jar is added to the project Libraries.</html>",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
