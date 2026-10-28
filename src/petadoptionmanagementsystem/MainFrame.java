package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window with FlatLaf styling.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Pet Adoption Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // ── Root panel ───────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 248, 252));
        setContentPane(root);

        // ── Header ───────────────────────────────────────────────────────────
        root.add(buildHeader(), BorderLayout.NORTH);

        // ── Tabbed pane ──────────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE,
                               FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);
        tabs.setBorder(new EmptyBorder(8, 10, 10, 10));
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabs.addTab("🐶  Pets",      new PetsPanel());
        tabs.addTab("👤  Adopters",  new AdoptersPanel());
        tabs.addTab("🤝  Adoptions", new AdoptionsPanel());
        tabs.addTab("📊  Reports",   new ReportsPanel());

        root.add(tabs, BorderLayout.CENTER);

        // ── Status bar ───────────────────────────────────────────────────────
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Private builders ─────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 160));
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Left: icon + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🐾");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 0));
        titles.setOpaque(false);
        JLabel title = new JLabel("Pet Adoption Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Connecting pets with loving homes");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 210, 255));
        titles.add(title);
        titles.add(subtitle);

        left.add(icon);
        left.add(titles);
        header.add(left, BorderLayout.WEST);

        // Right: version badge (HTML inside JLabel)
        JLabel version = new JLabel(
            "<html><div style='background:#1a5fa8; color:#aad4ff; "
            + "padding:4px 10px; border-radius:12px; font-size:11px;'>v1.0</div></html>");
        version.setBorder(new EmptyBorder(0, 0, 0, 4));
        header.add(version, BorderLayout.EAST);

        return header;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 220, 235)),
            new EmptyBorder(5, 14, 5, 14)
        ));
        bar.setBackground(new Color(250, 252, 255));

        JLabel status = new JLabel(
            "<html><span style='color:#666;'>●</span>&nbsp; "
            + "<span style='color:#333;'>Connected to SQLite database</span></html>");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel copy = new JLabel(
            "<html><span style='color:#aaa; font-size:11px;'>Pet Adoption System © 2026</span></html>");

        bar.add(status, BorderLayout.WEST);
        bar.add(copy,   BorderLayout.EAST);
        return bar;
    }
}
