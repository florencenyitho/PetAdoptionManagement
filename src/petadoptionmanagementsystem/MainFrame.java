package petadoptionmanagementsystem;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window.
 * Contains tabbed panels for Pets, Adopters, Adoptions, and Reports.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Pet Adoption Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 700);
        setLocationRelativeTo(null);
        // Only set icon if the resource actually exists (avoids NullPointerException)
        java.net.URL iconUrl = getClass().getResource("/petadoptionmanagementsystem/icon.png");
        if (iconUrl != null) {
            setIconImage(Toolkit.getDefaultToolkit().createImage(iconUrl));
        }

        // ── Header banner ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 152, 219));
        header.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("🐾  Pet Adoption Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ── Tabs ─────────────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabs.addTab("🐶  Pets",        new PetsPanel());
        tabs.addTab("👤  Adopters",    new AdoptersPanel());
        tabs.addTab("🤝  Adoptions",   new AdoptionsPanel());
        tabs.addTab("📊  Reports",     new ReportsPanel());

        add(tabs, BorderLayout.CENTER);

        // ── Status bar ───────────────────────────────────────────────────────
        JLabel statusBar = new JLabel("  Ready", SwingConstants.LEFT);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusBar, BorderLayout.SOUTH);
    }
}
