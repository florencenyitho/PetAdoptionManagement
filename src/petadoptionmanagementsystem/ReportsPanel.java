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
 */
public class ReportsPanel extends JPanel {

    private static final Color C_BLUE   = new Color(41,  128, 185);
    private static final Color C_BG     = new Color(245, 248, 252);
    private static final Color C_BORDER = new Color(210, 220, 235);

    private final PetDAO      petDAO      = new PetDAO();
    private final AdopterDAO  adopterDAO  = new AdopterDAO();
    private final AdoptionDAO adoptionDAO = new AdoptionDAO();

    // Summary stat cards
    private final JLabel lblTotalPets     = statValue("—");
    private final JLabel lblAvailable     = statValue("—");
    private final JLabel lblAdopted       = statValue("—");
    private final JLabel lblAdopters      = statValue("—");
    private final JLabel lblTotalAdopted  = statValue("—");

    private final DefaultTableModel speciesModel;
    private final DefaultTableModel monthlyModel;

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(C_BG);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        // Top button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>📊 System Reports</b></html>"));
        topBar.add(Box.createHorizontalStrut(20));
        JButton btnGen = PetsPanel.actionButton("🔄  Refresh Reports", C_BLUE);
        btnGen.addActionListener(e -> generateReports());
        topBar.add(btnGen);
        add(topBar, BorderLayout.NORTH);

        // Stat cards row
        add(buildStatCards(), BorderLayout.CENTER);

        // Tables row
        speciesModel = new DefaultTableModel(new String[]{"Species", "Total Pets"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        monthlyModel = new DefaultTableModel(new String[]{"Month", "Adoptions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        add(buildTablesRow(), BorderLayout.SOUTH);

        generateReports();
    }

    // ── Stat cards ───────────────────────────────────────────────────────────

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);
        row.add(statCard("Total Pets",      lblTotalPets,    new Color(52,  152, 219)));
        row.add(statCard("Available",        lblAvailable,    new Color(39,  174,  96)));
        row.add(statCard("Adopted",          lblAdopted,      new Color(231, 76,   60)));
        row.add(statCard("Adopters",         lblAdopters,     new Color(155,  89, 182)));
        row.add(statCard("Total Adoptions",  lblTotalAdopted, new Color(230, 126,  34)));
        return row;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        // Accent top stripe
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(0, 5));
        card.add(stripe, BorderLayout.NORTH);

        JLabel titleLbl = new JLabel(
            "<html><span style='font-size:11px; color:#666;'>" + title + "</span></html>",
            SwingConstants.CENTER);
        card.add(titleLbl, BorderLayout.CENTER);

        valueLabel.setForeground(accent);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    private static JLabel statValue(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 32));
        return l;
    }

    // ── Tables row ───────────────────────────────────────────────────────────

    private JSplitPane buildTablesRow() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTableCard("🐾 Pets by Species",             speciesModel),
                buildTableCard("📅 Monthly Adoptions (Last 12)", monthlyModel));
        split.setResizeWeight(0.5);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setDividerSize(8);
        return split;
    }

    private JPanel buildTableCard(String title, DefaultTableModel model) {
        JTable tbl = new JTable(model);
        tbl.setRowHeight(28);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setSelectionBackground(new Color(210, 234, 255));

        JTableHeader header = tbl.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 32));

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.add(new JLabel(
            "<html><b style='color:#1e5aa0;'>" + title + "</b></html>"), BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ── Generate ─────────────────────────────────────────────────────────────

    private void generateReports() {
        try {
            int totalPets      = petDAO.getAllPets().size();
            int availablePets  = petDAO.countAvailable();
            int adoptedPets    = petDAO.countAdopted();
            int totalAdopters  = adopterDAO.countAdopters();
            int totalAdoptions = adoptionDAO.countAdoptions();

            lblTotalPets   .setText(String.valueOf(totalPets));
            lblAvailable   .setText(String.valueOf(availablePets));
            lblAdopted     .setText(String.valueOf(adoptedPets));
            lblAdopters    .setText(String.valueOf(totalAdopters));
            lblTotalAdopted.setText(String.valueOf(totalAdoptions));

            speciesModel.setRowCount(0);
            for (Object[] row : petDAO.countBySpecies()) speciesModel.addRow(row);

            monthlyModel.setRowCount(0);
            for (Object[] row : adoptionDAO.monthlyAdoptionStats()) monthlyModel.addRow(row);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "<html><b>Error generating reports:</b><br>" + ex.getMessage() + "</html>",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
