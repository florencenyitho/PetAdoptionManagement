package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window.
 * Uses programmatically-generated icons on tabs and in the header.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Pet Adoption Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // App window icon (paw in blue)
        setIconImage(IconGenerator.pets(32).getImage());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 248, 252));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 90, 160));
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Left side: large paw icon + title text
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        // Large white paw icon in header
        JLabel iconLbl = new JLabel(IconGenerator.header(42));
        left.add(iconLbl);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel title = new JLabel("Pet Adoption Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Connecting pets with loving homes");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 210, 255));
        titles.add(title);
        titles.add(subtitle);
        left.add(titles);

        header.add(left, BorderLayout.WEST);

        // Right side: small stat badges (pet paw icon + label)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(buildHeaderBadge(IconGenerator.pets(18),
                  "<html><span style='color:#aad4ff;'>Manage Pets &amp; Adoptions</span></html>"));
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel buildHeaderBadge(ImageIcon icon, String html) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badge.setOpaque(false);
        badge.add(new JLabel(icon));
        badge.add(new JLabel(html));
        return badge;
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE,
                               FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);
        tabs.setBorder(new EmptyBorder(8, 10, 10, 10));
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Each tab gets a custom icon + text label
        tabs.addTab(null, IconGenerator.pets(20),      new PetsPanel(),      "Pets");
        tabs.addTab(null, IconGenerator.adopters(20),  new AdoptersPanel(),  "Adopters");
        tabs.addTab(null, IconGenerator.adoptions(20), new AdoptionsPanel(), "Adoptions");
        tabs.addTab(null, IconGenerator.reports(20),   new ReportsPanel(),   "Reports");

        // Set custom tab components with icon + label side-by-side
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setTabComponentAt(i, buildTabLabel(
                tabs.getIconAt(i),
                tabs.getToolTipTextAt(i),
                tabs
            ));
        }

        return tabs;
    }

    private JPanel buildTabLabel(Icon icon, String text, JTabbedPane tabs) {
        JPanel lbl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        lbl.setOpaque(false);
        lbl.add(new JLabel(icon));
        JLabel textLbl = new JLabel(text);
        textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.add(textLbl);
        // Click on custom tab component must still switch tabs
        lbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    if (tabs.getTabComponentAt(i) == lbl) {
                        tabs.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
        return lbl;
    }

    // ── Status bar ───────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 220, 235)),
            new EmptyBorder(5, 14, 5, 14)
        ));
        bar.setBackground(new Color(250, 252, 255));

        // Small paw icon in status bar
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(new JLabel(IconGenerator.pets(14)));
        left.add(new JLabel(
            "<html><span style='color:#333;'>Connected to SQLite database</span></html>"));

        JLabel copy = new JLabel(
            "<html><span style='color:#aaa; font-size:11px;'>" +
            "Pet Adoption System © 2026</span></html>");

        bar.add(left, BorderLayout.WEST);
        bar.add(copy, BorderLayout.EAST);
        return bar;
    }
}
