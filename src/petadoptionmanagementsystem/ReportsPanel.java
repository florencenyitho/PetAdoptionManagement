package petadoptionmanagementsystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Reports tab — summary stats, species breakdown, monthly trend.
 * Layout: top bar → stat cards row → two equal tables side by side.
 */
public class ReportsPanel extends JPanel {

    private static final Color C_BLUE   = new Color(41,  128, 185);
    private static final Color C_BG     = new Color(245, 248, 252);
    private static final Color C_BORDER = new Color(210, 220, 235);

    private final PetDAO      petDAO      = new PetDAO();
    private final AdopterDAO  adopterDAO  = new AdopterDAO();
    private final AdoptionDAO adoptionDAO = new AdoptionDAO();

    // Stat card value labels
    private final JLabel lblTotalPets    = statValue("—");
    private final JLabel lblAvailable    = statValue("—");
    private final JLabel lblAdopted      = statValue("—");
    private final JLabel lblAdopters     = statValue("—");
    private final JLabel lblTotalAdopted = statValue("—");
    private final JLabel lblRevenue      = statValue("—");
    private final JLabel lblPending      = statValue("—");

    private final DefaultTableModel speciesModel;
    private final DefaultTableModel monthlyModel;

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(C_BG);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildStatCards(), BorderLayout.CENTER);

        speciesModel = new DefaultTableModel(new String[]{"Species", "Total Pets"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        monthlyModel = new DefaultTableModel(
                new String[]{"Month (yyyy-MM)", "Adoptions", "Revenue (UGX)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };        add(buildTablesRow(), BorderLayout.SOUTH);

        generateReports();
    }

    // ── Top bar ──────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);

        bar.add(new JLabel(IconGenerator.reports(22)));
        bar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>System Reports</b></html>"));
        bar.add(Box.createHorizontalStrut(16));

        JButton btnRefresh = PetsPanel.iconButton("Refresh Reports",
                IconGenerator.refresh(15), C_BLUE);
        btnRefresh.addActionListener(e -> generateReports());
        bar.add(btnRefresh);

        return bar;
    }

    // ── Stat cards ───────────────────────────────────────────────────────────

    private JPanel buildStatCards() {
        // Row 1: pet/adoption counts
        JPanel row1 = new JPanel(new GridLayout(1, 5, 12, 0));
        row1.setOpaque(false);
        row1.add(statCard("Total Pets",     lblTotalPets,    new Color(52,  152, 219), IconGenerator.pets(28)));
        row1.add(statCard("Available",       lblAvailable,    new Color(39,  174,  96), IconGenerator.pets(28)));
        row1.add(statCard("Adopted",         lblAdopted,      new Color(231,  76,  60), IconGenerator.adoptions(28)));
        row1.add(statCard("Adopters",        lblAdopters,     new Color(155,  89, 182), IconGenerator.adopters(28)));
        row1.add(statCard("Total Adoptions", lblTotalAdopted, new Color(230, 126,  34), IconGenerator.adoptions(28)));

        // Row 2: financial summary
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.add(statCard("Revenue Collected (UGX)", lblRevenue, new Color(22,  163,  74), IconGenerator.reports(28)));
        row2.add(statCard("Outstanding / Pending (UGX)", lblPending, new Color(202, 138,   4), IconGenerator.reports(28)));

        JPanel wrapper = new JPanel(new GridLayout(2, 1, 0, 10));
        wrapper.setOpaque(false);
        wrapper.add(row1);
        wrapper.add(row2);
        return wrapper;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent, ImageIcon icon) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Accent stripe at top
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(0, 4));
        card.add(stripe, BorderLayout.NORTH);

        // Icon + title row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        titleRow.setOpaque(false);
        // Tint icon to match accent
        titleRow.add(new JLabel(tintIcon(icon, accent)));
        JLabel titleLbl = new JLabel(
            "<html><span style='font-size:11px; color:#555;'>" + title + "</span></html>");
        titleRow.add(titleLbl);
        card.add(titleRow, BorderLayout.CENTER);

        // Big number
        valueLabel.setForeground(accent);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    private static JLabel statValue(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        return l;
    }

    // ── Tables row ───────────────────────────────────────────────────────────

    private JPanel buildTablesRow() {
        // Use GridLayout(1,2) so both tables always get equal width — no squeezing
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 240));

        row.add(buildTableCard(
            IconGenerator.pets(16),    "Pets by Species",
            speciesModel));
        row.add(buildTableCard(
            IconGenerator.adoptions(16), "Monthly Adoptions  (last 12 months)",
            monthlyModel));

        return row;
    }

    private JPanel buildTableCard(ImageIcon icon, String title, DefaultTableModel model) {
        JTable tbl = new JTable(model);
        tbl.setRowHeight(28);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionBackground(new Color(210, 234, 255));
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTableHeader header = tbl.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));

        // Title row: icon + text
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titleRow.setOpaque(false);
        titleRow.add(new JLabel(icon));
        titleRow.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:12px;'>" + title + "</b></html>"));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(scroll,   BorderLayout.CENTER);
        return card;
    }

    // ── Generate ─────────────────────────────────────────────────────────────

    private void generateReports() {
        try {
            lblTotalPets   .setText(String.valueOf(petDAO.getAllPets().size()));
            lblAvailable   .setText(String.valueOf(petDAO.countAvailable()));
            lblAdopted     .setText(String.valueOf(petDAO.countAdopted()));
            lblAdopters    .setText(String.valueOf(adopterDAO.countAdopters()));
            lblTotalAdopted.setText(String.valueOf(adoptionDAO.countAdoptions()));
            lblRevenue     .setText(UGX.format(adoptionDAO.totalRevenue()));
            lblPending     .setText(UGX.format(adoptionDAO.totalPending()));

            speciesModel.setRowCount(0);
            for (Object[] row : petDAO.countBySpecies()) speciesModel.addRow(row);

            monthlyModel.setRowCount(0);
            for (Object[] row : adoptionDAO.monthlyAdoptionStats()) {
                // row = {month, count, revenue}
                monthlyModel.addRow(new Object[]{
                    row[0],
                    row[1],
                    UGX.compact((double) row[2])
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "<html><b>Error generating reports:</b><br>" + ex.getMessage() + "</html>",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Tints a white-drawn icon to a target color by replacing white pixels
     * with the target color, preserving alpha.
     */
    private static ImageIcon tintIcon(ImageIcon src, Color tint) {
        int w = src.getIconWidth();
        int h = src.getIconHeight();
        java.awt.image.BufferedImage out =
            new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src.getImage(), 0, 0, null);
        g.dispose();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb  = out.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                if (alpha > 10) {
                    // Replace the pixel color with tint, keep alpha
                    int newArgb = (alpha << 24)
                                | (tint.getRed()   << 16)
                                | (tint.getGreen() <<  8)
                                |  tint.getBlue();
                    out.setRGB(x, y, newArgb);
                }
            }
        }
        return new ImageIcon(out);
    }
}
