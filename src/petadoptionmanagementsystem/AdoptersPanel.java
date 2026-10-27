package petadoptionmanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for registering, viewing, and searching adopters.
 */
public class AdoptersPanel extends JPanel {

    private final AdopterDAO adopterDAO = new AdopterDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField tfName    = new JTextField(18);
    private final JTextField tfPhone   = new JTextField(14);
    private final JTextField tfEmail   = new JTextField(18);
    private final JTextArea  taAddress = new JTextArea(3, 20);
    private final JTextField tfSearch  = new JTextField(18);

    private int selectedAdopterId = -1;

    public AdoptersPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search:"));
        searchBar.add(tfSearch);
        JButton btnSearch  = new JButton("🔍 Search");
        JButton btnShowAll = new JButton("Show All");
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        add(searchBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Phone", "Email", "Address", "Date Registered"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumn("ID").setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Adopter Details"));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        addField(fields, gbc, 0, "Name*:",  tfName);
        addField(fields, gbc, 1, "Phone*:", tfPhone);
        addField(fields, gbc, 2, "Email:",  tfEmail);

        gbc.gridx = 0; gbc.gridy = 3; fields.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        fields.add(new JScrollPane(taAddress), gbc);
        gbc.gridwidth = 1;

        formPanel.add(fields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd    = new JButton("➕ Register Adopter");
        JButton btnUpdate = new JButton("✏️ Update");
        JButton btnDelete = new JButton("🗑️ Delete");
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

        loadAdopters(null);

        // Listeners
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });
        btnSearch .addActionListener(e -> loadAdopters(tfSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { tfSearch.setText(""); loadAdopters(null); });
        btnAdd    .addActionListener(e -> addAdopter());
        btnUpdate .addActionListener(e -> updateAdopter());
        btnDelete .addActionListener(e -> deleteAdopter());
        btnClear  .addActionListener(e -> clearForm());
    }

    private void loadAdopters(String keyword) {
        try {
            List<Adopter> list = (keyword == null || keyword.isEmpty())
                    ? adopterDAO.getAllAdopters()
                    : adopterDAO.searchAdopters(keyword);
            tableModel.setRowCount(0);
            for (Adopter a : list) {
                tableModel.addRow(new Object[]{
                    a.getAdopterId(), a.getName(), a.getPhone(),
                    a.getEmail(), a.getAddress(), a.getDateRegistered()
                });
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void addAdopter() {
        if (!validateForm()) return;
        Adopter a = buildFromForm();
        try {
            if (adopterDAO.addAdopter(a)) {
                JOptionPane.showMessageDialog(this, "Adopter registered!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm(); loadAdopters(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void updateAdopter() {
        if (selectedAdopterId < 0) { JOptionPane.showMessageDialog(this, "Select an adopter."); return; }
        if (!validateForm()) return;
        Adopter a = buildFromForm();
        a.setAdopterId(selectedAdopterId);
        try {
            if (adopterDAO.updateAdopter(a)) {
                JOptionPane.showMessageDialog(this, "Adopter updated!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm(); loadAdopters(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void deleteAdopter() {
        if (selectedAdopterId < 0) { JOptionPane.showMessageDialog(this, "Select an adopter."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this adopter?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (adopterDAO.deleteAdopter(selectedAdopterId)) {
                JOptionPane.showMessageDialog(this, "Adopter deleted.");
                clearForm(); loadAdopters(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedAdopterId = (int) tableModel.getValueAt(row, 0);
        tfName .setText((String) tableModel.getValueAt(row, 1));
        tfPhone.setText((String) tableModel.getValueAt(row, 2));
        tfEmail.setText((String) tableModel.getValueAt(row, 3));
        taAddress.setText((String) tableModel.getValueAt(row, 4));
    }

    private void clearForm() {
        selectedAdopterId = -1;
        tfName.setText(""); tfPhone.setText(""); tfEmail.setText(""); taAddress.setText("");
        table.clearSelection();
    }

    private Adopter buildFromForm() {
        Adopter a = new Adopter();
        a.setName   (tfName.getText().trim());
        a.setPhone  (tfPhone.getText().trim());
        a.setEmail  (tfEmail.getText().trim());
        a.setAddress(taAddress.getText().trim());
        return a;
    }

    private boolean validateForm() {
        if (tfName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required."); return false;
        }
        if (tfPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number is required."); return false;
        }
        return true;
    }

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
