package petadoptionmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for recording adoptions and viewing adoption history.
 */
public class AdoptionsPanel extends JPanel {

    private final AdoptionDAO adoptionDAO = new AdoptionDAO();
    private final PetDAO      petDAO      = new PetDAO();
    private final AdopterDAO  adopterDAO  = new AdopterDAO();

    // History table
    private final DefaultTableModel tableModel;
    private final JTable table;

    // Record-adoption form
    private final JComboBox<Pet>     cbPet      = new JComboBox<>();
    private final JComboBox<Adopter> cbAdopter  = new JComboBox<>();
    private final JTextField         tfDate     = new JTextField(12);
    private final JTextArea          taNotes    = new JTextArea(3, 25);

    // Filter
    private final JTextField tfFrom = new JTextField(10);
    private final JTextField tfTo   = new JTextField(10);

    public AdoptionsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Filter bar ───────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterBar.add(new JLabel("From (yyyy-MM-dd):"));
        filterBar.add(tfFrom);
        filterBar.add(new JLabel("To:"));
        filterBar.add(tfTo);
        JButton btnFilter  = new JButton("🔍 Filter");
        JButton btnShowAll = new JButton("Show All");
        filterBar.add(btnFilter);
        filterBar.add(btnShowAll);
        add(filterBar, BorderLayout.NORTH);

        // ── History table ────────────────────────────────────────────────────
        String[] cols = {"ID", "Pet", "Adopter", "Phone", "Adoption Date", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getColumn("ID").setMaxWidth(50);
        table.getColumn("Phone").setMaxWidth(110);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Record-adoption form ─────────────────────────────────────────────
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Record New Adoption"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addField(fields, gbc, 0, "Pet (available):", cbPet);
        addField(fields, gbc, 1, "Adopter:",         cbAdopter);

        tfDate.setText(LocalDate.now().toString());
        addField(fields, gbc, 2, "Adoption Date*:",  tfDate);

        gbc.gridx = 0; gbc.gridy = 3; fields.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        fields.add(new JScrollPane(taNotes), gbc);
        gbc.gridwidth = 1;

        formPanel.add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnRecord  = new JButton("🤝 Record Adoption");
        JButton btnRefresh = new JButton("🔄 Refresh Lists");
        styleButton(btnRecord,  new Color(39, 174, 96));
        styleButton(btnRefresh, new Color(100, 100, 100));
        btnPanel.add(btnRecord);
        btnPanel.add(btnRefresh);
        formPanel.add(btnPanel, BorderLayout.SOUTH);

        add(formPanel, BorderLayout.SOUTH);

        // ── Initial load ─────────────────────────────────────────────────────
        loadHistory(null, null);
        refreshDropdowns();

        // ── Listeners ────────────────────────────────────────────────────────
        btnFilter .addActionListener(e -> loadHistory(tfFrom.getText().trim(), tfTo.getText().trim()));
        btnShowAll.addActionListener(e -> { tfFrom.setText(""); tfTo.setText(""); loadHistory(null, null); });
        btnRecord .addActionListener(e -> recordAdoption());
        btnRefresh.addActionListener(e -> refreshDropdowns());
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void loadHistory(String from, String to) {
        try {
            List<Adoption> list;
            if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
                list = adoptionDAO.getAdoptionsByDateRange(from, to);
            } else {
                list = adoptionDAO.getAllAdoptions();
            }
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

        if (pet == null) {
            JOptionPane.showMessageDialog(this,
                    "No available pets. Register a pet or check availability."); return;
        }
        if (adopter == null) {
            JOptionPane.showMessageDialog(this,
                    "No adopters found. Register an adopter first."); return;
        }
        String date = tfDate.getText().trim();
        if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adoption date is required."); return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Record adoption of \"" + pet.getName() + "\" by \"" + adopter.getName() + "\"?\n"
                + "This will mark the pet as no longer available.",
                "Confirm Adoption", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Adoption adoption = new Adoption();
        adoption.setPetId       (pet.getPetId());
        adoption.setAdopterId   (adopter.getAdopterId());
        adoption.setAdoptionDate(date);
        adoption.setNotes       (taNotes.getText().trim());

        try {
            if (adoptionDAO.recordAdoption(adoption)) {
                JOptionPane.showMessageDialog(this, "Adoption recorded successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                taNotes.setText("");
                tfDate.setText(LocalDate.now().toString());
                loadHistory(null, null);
                refreshDropdowns();
            }
        } catch (SQLException ex) { showError(ex); }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void addField(JPanel p, GridBagConstraints gbc, int row,
                          String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; p.add(field, gbc);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
