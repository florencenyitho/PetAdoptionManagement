package petadoptionmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for registering, viewing, searching, and editing pets.
 */
public class PetsPanel extends JPanel {

    private final PetDAO petDAO = new PetDAO();

    // Table
    private final DefaultTableModel tableModel;
    private final JTable table;

    // Form fields
    private final JTextField tfName        = new JTextField(15);
    private final JTextField tfSpecies     = new JTextField(12);
    private final JTextField tfBreed       = new JTextField(12);
    private final JTextField tfAge         = new JTextField(5);
    private final JComboBox<String> cbGender =
            new JComboBox<>(new String[]{"Male", "Female", "Unknown"});
    private final JTextArea  taDescription = new JTextArea(3, 20);
    private final JTextField tfSearch      = new JTextField(18);
    private final JCheckBox  cbAvailable   = new JCheckBox("Available", true);

    private int selectedPetId = -1;

    public PetsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Search bar ───────────────────────────────────────────────────────
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search:"));
        searchBar.add(tfSearch);
        JButton btnSearch = new JButton("🔍 Search");
        JButton btnShowAll = new JButton("Show All");
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        add(searchBar, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Name", "Species", "Breed", "Age", "Gender", "Available", "Date Added"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumn("ID").setMaxWidth(50);
        table.getColumn("Age").setMaxWidth(50);
        table.getColumn("Available").setMaxWidth(80);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // ── Form panel ───────────────────────────────────────────────────────
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Pet Details"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addField(fields, gbc, 0, "Name*:",        tfName);
        addField(fields, gbc, 1, "Species*:",     tfSpecies);
        addField(fields, gbc, 2, "Breed:",        tfBreed);
        addField(fields, gbc, 3, "Age (years)*:", tfAge);
        addField(fields, gbc, 4, "Gender:",       cbGender);

        // Description spans full width
        gbc.gridx = 0; gbc.gridy = 5; fields.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        fields.add(new JScrollPane(taDescription), gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 6; fields.add(cbAvailable, gbc);

        formPanel.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd    = new JButton("➕ Register Pet");
        JButton btnUpdate = new JButton("✏️ Update Pet");
        JButton btnDelete = new JButton("🗑️ Delete Pet");
        JButton btnClear  = new JButton("Clear");
        styleButton(btnAdd,    new Color(39, 174, 96));
        styleButton(btnUpdate, new Color(41, 128, 185));
        styleButton(btnDelete, new Color(192, 57, 43));
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        formPanel.add(btnPanel, BorderLayout.SOUTH);

        add(formPanel, BorderLayout.SOUTH);

        // ── Load data ────────────────────────────────────────────────────────
        loadPets(null);

        // ── Listeners ────────────────────────────────────────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        btnSearch .addActionListener(e -> loadPets(tfSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { tfSearch.setText(""); loadPets(null); });
        btnAdd    .addActionListener(e -> addPet());
        btnUpdate .addActionListener(e -> updatePet());
        btnDelete .addActionListener(e -> deletePet());
        btnClear  .addActionListener(e -> clearForm());
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void loadPets(String keyword) {
        try {
            List<Pet> pets = (keyword == null || keyword.isEmpty())
                    ? petDAO.getAllPets()
                    : petDAO.searchPets(keyword);
            tableModel.setRowCount(0);
            for (Pet p : pets) {
                tableModel.addRow(new Object[]{
                    p.getPetId(), p.getName(), p.getSpecies(), p.getBreed(),
                    p.getAge(), p.getGender(),
                    p.isAvailable() ? "Yes" : "No",
                    p.getDateAdded()
                });
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void addPet() {
        if (!validateForm()) return;
        Pet p = buildPetFromForm();
        try {
            if (petDAO.addPet(p)) {
                JOptionPane.showMessageDialog(this, "Pet registered successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadPets(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void updatePet() {
        if (selectedPetId < 0) { JOptionPane.showMessageDialog(this, "Select a pet to update."); return; }
        if (!validateForm()) return;
        Pet p = buildPetFromForm();
        p.setPetId(selectedPetId);
        p.setAvailable(cbAvailable.isSelected());
        try {
            if (petDAO.updatePet(p)) {
                JOptionPane.showMessageDialog(this, "Pet updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadPets(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void deletePet() {
        if (selectedPetId < 0) { JOptionPane.showMessageDialog(this, "Select a pet to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this pet? This cannot be undone.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (petDAO.deletePet(selectedPetId)) {
                JOptionPane.showMessageDialog(this, "Pet deleted.");
                clearForm();
                loadPets(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedPetId = (int) tableModel.getValueAt(row, 0);
        tfName    .setText((String) tableModel.getValueAt(row, 1));
        tfSpecies .setText((String) tableModel.getValueAt(row, 2));
        tfBreed   .setText((String) tableModel.getValueAt(row, 3));
        tfAge     .setText(String.valueOf(tableModel.getValueAt(row, 4)));
        cbGender  .setSelectedItem(tableModel.getValueAt(row, 5));
        cbAvailable.setSelected("Yes".equals(tableModel.getValueAt(row, 6)));
        try {
            Pet p = petDAO.getPetById(selectedPetId);
            if (p != null) taDescription.setText(p.getDescription());
        } catch (SQLException ex) { showError(ex); }
    }

    private void clearForm() {
        selectedPetId = -1;
        tfName.setText(""); tfSpecies.setText(""); tfBreed.setText("");
        tfAge.setText(""); taDescription.setText("");
        cbGender.setSelectedIndex(0); cbAvailable.setSelected(true);
        table.clearSelection();
    }

    private Pet buildPetFromForm() {
        Pet p = new Pet();
        p.setName       (tfName.getText().trim());
        p.setSpecies    (tfSpecies.getText().trim());
        p.setBreed      (tfBreed.getText().trim());
        p.setAge        (Integer.parseInt(tfAge.getText().trim()));
        p.setGender     ((String) cbGender.getSelectedItem());
        p.setDescription(taDescription.getText().trim());
        p.setAvailable  (true);
        return p;
    }

    private boolean validateForm() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pet name is required."); return false;
        }
        if (tfSpecies.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Species is required."); return false;
        }
        try {
            int age = Integer.parseInt(tfAge.getText().trim());
            if (age < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a non-negative number."); return false;
        }
        return true;
    }

    private void addField(JPanel p, GridBagConstraints gbc, int row,
                          String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel(label), gbc);
        gbc.gridx = 1; p.add(field, gbc);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
