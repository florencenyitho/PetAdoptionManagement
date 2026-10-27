package petadoptionmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for generating and viewing basic reports.
 */
public class ReportsPanel extends JPanel {

    private final PetDAO      petDAO      = new PetDAO();
    private final AdopterDAO  adopterDAO  = new AdopterDAO();
    private final AdoptionDAO adoptionDAO = new AdoptionDAO();

    private final JTextArea summaryArea = new JTextArea();
    private final DefaultTableModel speciesModel;
    private final DefaultTableModel monthlyModel;

    public ReportsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Top button bar ───────────────────────────────────────────────────
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnGenerate = new JButton("📊 Generate Reports");
        styleButton(btnGenerate, new Color(52, 152, 219));
        btnBar.add(btnGenerate);
        add(btnBar, BorderLayout.NORTH);

        // ── Summary ──────────────────────────────────────────────────────────
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        summaryArea.setBorder(BorderFactory.createTitledBorder("Summary"));
        summaryArea.setRows(8);

        // ── Species breakdown table ──────────────────────────────────────────
        speciesModel = new DefaultTableModel(new String[]{"Species", "Total Pets"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable speciesTable = new JTable(speciesModel);
        speciesTable.setRowHeight(24);
        JPanel speciesPanel = new JPanel(new BorderLayout());
        speciesPanel.setBorder(BorderFactory.createTitledBorder("Pets by Species"));
        speciesPanel.add(new JScrollPane(speciesTable));

        // ── Monthly adoptions table ──────────────────────────────────────────
        monthlyModel = new DefaultTableModel(new String[]{"Month", "Adoptions"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable monthlyTable = new JTable(monthlyModel);
        monthlyTable.setRowHeight(24);
        JPanel monthlyPanel = new JPanel(new BorderLayout());
        monthlyPanel.setBorder(BorderFactory.createTitledBorder("Monthly Adoptions (Last 12 Months)"));
        monthlyPanel.add(new JScrollPane(monthlyTable));

        // ── Arrange sub-panels ───────────────────────────────────────────────
        JSplitPane tables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, speciesPanel, monthlyPanel);
        tables.setResizeWeight(0.5);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(summaryArea), tables);
        main.setResizeWeight(0.35);

        add(main, BorderLayout.CENTER);

        // ── Listener ─────────────────────────────────────────────────────────
        btnGenerate.addActionListener(e -> generateReports());

        // Auto-generate on open
        generateReports();
    }

    private void generateReports() {
        try {
            int totalPets      = petDAO.getAllPets().size();
            int availablePets  = petDAO.countAvailable();
            int adoptedPets    = petDAO.countAdopted();
            int totalAdopters  = adopterDAO.countAdopters();
            int totalAdoptions = adoptionDAO.countAdoptions();

            String summary = String.format("""
                ╔══════════════════════════════════════════╗
                ║       PET ADOPTION SYSTEM  SUMMARY       ║
                ╠══════════════════════════════════════════╣
                ║  Total Pets Registered  : %5d           ║
                ║  Available for Adoption : %5d           ║
                ║  Already Adopted        : %5d           ║
                ║  Total Adopters         : %5d           ║
                ║  Total Adoptions        : %5d           ║
                ╚══════════════════════════════════════════╝
                """,
                    totalPets, availablePets, adoptedPets, totalAdopters, totalAdoptions);

            summaryArea.setText(summary);

            // Species breakdown
            speciesModel.setRowCount(0);
            List<Object[]> bySpecies = petDAO.countBySpecies();
            for (Object[] row : bySpecies) {
                speciesModel.addRow(row);
            }

            // Monthly adoptions
            monthlyModel.setRowCount(0);
            List<Object[]> monthly = adoptionDAO.monthlyAdoptionStats();
            for (Object[] row : monthly) {
                monthlyModel.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating reports:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }
}
