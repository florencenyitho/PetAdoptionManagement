package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Adoptions tab — record adoptions and view history.
 */
public class AdoptionsPanel extends JPanel {

    private static final Color C_GREEN  = new Color(39,  174, 96);
    private static final Color C_BLUE   = new Color(41,  128, 185);
    private static final Color C_BG     = new Color(245, 248, 252);
    private static final Color C_BORDER = new Color(210, 220, 235);

    private final AdoptionDAO adoptionDAO = new AdoptionDAO();
    private final PetDAO      petDAO      = new PetDAO();
    private final AdopterDAO  adopterDAO  = new AdopterDAO();

    private DefaultTableModel tableModel;
    private JTable table;

    private final JComboBox<Pet>     cbPet     = new JComboBox<>();
    private final JComboBox<Adopter> cbAdopter = new JComboBox<>();
    private final JTextField         tfDate    = PetsPanel.styledField("yyyy-MM-dd", 12);
    private final JTextArea          taNotes   = new JTextArea(2, 24);
    private final JTextField         tfFrom    = PetsPanel.styledField("2026-01-01", 10);
    private final JTextField         tfTo      = PetsPanel.styledField("2026-12-31", 10);

    public AdoptionsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildFilterBar(),  BorderLayout.NORTH);
        add(buildTableCard(),  BorderLayout.CENTER);
        add(buildFormCard(),   BorderLayout.SOUTH);

        loadHistory(null, null);
        refreshDropdowns();
    }

    // ── Filter bar ───────────────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(C_BG);

        JButton btnFilter  = PetsPanel.actionButton("🔍  Filter", C_BLUE);
        JButton btnShowAll = PetsPanel.actionButton("Show All",   new Color(100, 116, 139));

        bar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>🤝 Adoption History</b></html>"));
        bar.add(Box.createHorizontalStrut(20));
        bar.add(PetsPanel.htmlLabel("From:"));
        bar.add(tfFrom);
        bar.add(PetsPanel.htmlLabel("To:"));
        bar.add(tfTo);
        bar.add(btnFilter);
        bar.add(btnShowAll);

        btnFilter .addActionListener(e -> loadHistory(tfFrom.getText().trim(), tfTo.getText().trim()));
        btnShowAll.addActionListener(e -> { tfFrom.setText(""); tfTo.setText(""); loadHistory(null, null); });
        return bar;
    }

    // ── Table card ───────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        String[] cols = {"ID", "Pet", "Adopter", "Phone", "Adoption Date", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(210, 234, 255));
        table.setSelectionForeground(Color.BLACK);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setMaxWidth(120);
        table.getColumnModel().getColumn(4).setMaxWidth(120);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = PetsPanel.card();
        card.setLayout(new BorderLayout());
        card.add(scroll);
        card.setBorder(new EmptyBorder(0, 10, 0, 10));
        return card;
    }

    // ── Form card ────────────────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = PetsPanel.card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER),
            new EmptyBorder(12, 16, 12, 16)
        ));

        card.add(new JLabel(
            "<html><span style='font-size:13px; font-weight:bold; color:#1e5aa0;'>"
            + "Record New Adoption</span>&nbsp;"
            + "<span style='font-size:11px; color:#888;'>"
            + "Pet will be marked unavailable automatically</span></html>"),
            BorderLayout.NORTH);

        tfDate.setText(LocalDate.now().toString());

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;

        taNotes.setLineWrap(true); taNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(taNotes);
        notesScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));

        g.gridx = 0; g.gridy = 0; fields.add(PetsPanel.htmlLabel("Pet (available):"), g);
        g.gridx = 1;               fields.add(cbPet, g);
        g.gridx = 2;               fields.add(PetsPanel.htmlLabel("Adopter:"), g);
        g.gridx = 3;               fields.add(cbAdopter, g);

        g.gridx = 0; g.gridy = 1; fields.add(PetsPanel.htmlLabel("Adoption Date *:"), g);
        g.gridx = 1;               fields.add(tfDate, g);

        g.gridx = 0; g.gridy = 2; fields.add(PetsPanel.htmlLabel("Notes:"), g);
        g.gridx = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        fields.add(notesScroll, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        card.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnRecord  = PetsPanel.actionButton("🤝  Record Adoption", C_GREEN);
        JButton btnRefresh = PetsPanel.actionButton("🔄  Refresh Lists",   new Color(100, 116, 139));
        btnRow.add(btnRefresh);
        btnRow.add(btnRecord);
        card.add(btnRow, BorderLayout.SOUTH);

        btnRecord .addActionListener(e -> recordAdoption());
        btnRefresh.addActionListener(e -> refreshDropdowns());
        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void loadHistory(String from, String to) {
        try {
            List<Adoption> list;
            if (from != null && !from.isEmpty() && to != null && !to.isEmpty())
                list = adoptionDAO.getAdoptionsByDateRange(from, to);
            else
                list = adoptionDAO.getAllAdoptions();
            tableModel.setRowCount(0);
            for (Adoption a : list) {
                tableModel.addRow(new Object[]{
                    a.getAdoptionId(), a.getPetName(), a.getAdopterName(),
                    a.getAdopterPhone(), a.getAdoptionDate(), a.getNotes()
                });
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void refreshDropdowns() {
        try {
            cbPet.removeAllItems();
            for (Pet p : petDAO.getAvailablePets()) cbPet.addItem(p);
            cbAdopter.removeAllItems();
            for (Adopter a : adopterDAO.getAllAdopters()) cbAdopter.addItem(a);
        } catch (SQLException ex) { showError(ex); }
    }

    private void recordAdoption() {
        Pet     pet     = (Pet)     cbPet.getSelectedItem();
        Adopter adopter = (Adopter) cbAdopter.getSelectedItem();
        if (pet == null)     { showWarn("No available pets. Register a pet first.");    return; }
        if (adopter == null) { showWarn("No adopters found. Register an adopter first."); return; }
        if (tfDate.getText().trim().isEmpty()) { showWarn("Adoption date is required."); return; }

        if (JOptionPane.showConfirmDialog(this,
                "<html>Record adoption of <b>" + pet.getName()
                + "</b> by <b>" + adopter.getName() + "</b>?<br>"
                + "<small style='color:gray;'>This will mark the pet as no longer available.</small></html>",
                "Confirm Adoption", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;

        Adoption adoption = new Adoption();
        adoption.setPetId       (pet.getPetId());
        adoption.setAdopterId   (adopter.getAdopterId());
        adoption.setAdoptionDate(tfDate.getText().trim());
        adoption.setNotes       (taNotes.getText().trim());
        try {
            if (adoptionDAO.recordAdoption(adoption)) {
                JOptionPane.showMessageDialog(this,
                    "<html>✅ Adoption recorded successfully!<br>"
                    + "<small>" + pet.getName() + " → " + adopter.getName() + "</small></html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                taNotes.setText("");
                tfDate.setText(LocalDate.now().toString());
                loadHistory(null, null);
                refreshDropdowns();
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this,
            "<html>⚠️ " + msg + "</html>", "Validation", JOptionPane.WARNING_MESSAGE);
    }
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
            "<html><b>Database error:</b><br>" + ex.getMessage() + "</html>",
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
