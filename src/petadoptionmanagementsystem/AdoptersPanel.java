package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Adopters tab — register, view, search, edit, delete adopters.
 */
public class AdoptersPanel extends JPanel {

    private static final Color C_GREEN  = new Color(39,  174,  96);
    private static final Color C_BLUE   = new Color(41,  128, 185);
    private static final Color C_RED    = new Color(192,  57,  43);
    private static final Color C_BG     = new Color(245, 248, 252);
    private static final Color C_BORDER = new Color(210, 220, 235);

    private final AdopterDAO adopterDAO = new AdopterDAO();

    private DefaultTableModel tableModel;
    private JTable table;

    private final JTextField tfName    = PetsPanel.styledField("Full name",          18);
    private final JTextField tfPhone   = PetsPanel.styledField("+1 555 000 0000",    14);
    private final JTextField tfEmail   = PetsPanel.styledField("email@example.com",  18);
    private final JTextArea  taAddress = new JTextArea(3, 24);
    private final JTextField tfSearch  = PetsPanel.styledField("Search by name or phone…", 22);

    private int selectedAdopterId = -1;

    public AdoptersPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFormCard(),  BorderLayout.SOUTH);

        loadAdopters(null);
    }

    // ── Top bar ──────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(C_BG);

        tfSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                                   "Search by name or phone…");

        JButton btnSearch  = PetsPanel.actionButton("Search",   C_BLUE);
        JButton btnShowAll = PetsPanel.actionButton("Show All", new Color(100, 116, 139));

        bar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>👤 Registered Adopters</b></html>"));
        bar.add(Box.createHorizontalStrut(20));
        bar.add(tfSearch);
        bar.add(btnSearch);
        bar.add(btnShowAll);

        btnSearch .addActionListener(e -> loadAdopters(tfSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { tfSearch.setText(""); loadAdopters(null); });
        return bar;
    }

    // ── Table card ───────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        String[] cols = {"ID", "Name", "Phone", "Email", "Address", "Date Registered"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(210, 234, 255));
        table.setSelectionForeground(Color.BLACK);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        table.getSelectionModel().addListSelectionListener(
                e -> { if (!e.getValueIsAdjusting()) populateForm(); });

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
            + "Adopter Details</span>&nbsp;"
            + "<span style='font-size:11px; color:#888;'>(* required)</span></html>"),
            BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;

        taAddress.setLineWrap(true); taAddress.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(taAddress);
        addrScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));

        addRow(fields, g, 0, PetsPanel.htmlLabel("Name *"),  tfName,
                            PetsPanel.htmlLabel("Phone *"),  tfPhone);
        addRow(fields, g, 1, PetsPanel.htmlLabel("Email"),   tfEmail,
                            new JLabel(), new JLabel());
        g.gridx = 0; g.gridy = 2; fields.add(PetsPanel.htmlLabel("Address"), g);
        g.gridx = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        fields.add(addrScroll, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        card.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnAdd    = PetsPanel.actionButton("➕  Register Adopter", C_GREEN);
        JButton btnUpdate = PetsPanel.actionButton("✏️  Update",           C_BLUE);
        JButton btnDelete = PetsPanel.actionButton("🗑️  Delete",           C_RED);
        JButton btnClear  = PetsPanel.actionButton("✖  Clear", new Color(100, 116, 139));
        btnRow.add(btnClear); btnRow.add(btnDelete); btnRow.add(btnUpdate); btnRow.add(btnAdd);
        card.add(btnRow, BorderLayout.SOUTH);

        btnAdd   .addActionListener(e -> addAdopter());
        btnUpdate.addActionListener(e -> updateAdopter());
        btnDelete.addActionListener(e -> deleteAdopter());
        btnClear .addActionListener(e -> clearForm());
        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void loadAdopters(String keyword) {
        try {
            List<Adopter> list = (keyword == null || keyword.isEmpty())
                    ? adopterDAO.getAllAdopters() : adopterDAO.searchAdopters(keyword);
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
        try {
            if (adopterDAO.addAdopter(buildFromForm())) {
                showSuccess("Adopter registered!");
                clearForm(); loadAdopters(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void updateAdopter() {
        if (selectedAdopterId < 0) { showWarn("Select an adopter to update."); return; }
        if (!validateForm()) return;
        Adopter a = buildFromForm();
        a.setAdopterId(selectedAdopterId);
        try {
            if (adopterDAO.updateAdopter(a)) {
                showSuccess("Adopter updated!");
                clearForm(); loadAdopters(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void deleteAdopter() {
        if (selectedAdopterId < 0) { showWarn("Select an adopter to delete."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "<html>Delete this adopter?<br><small>This cannot be undone.</small></html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            if (adopterDAO.deleteAdopter(selectedAdopterId)) {
                showSuccess("Adopter deleted.");
                clearForm(); loadAdopters(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedAdopterId = (int) tableModel.getValueAt(row, 0);
        tfName   .setText((String) tableModel.getValueAt(row, 1));
        tfPhone  .setText((String) tableModel.getValueAt(row, 2));
        tfEmail  .setText((String) tableModel.getValueAt(row, 3));
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
        if (tfName.getText().trim().isEmpty())  { showWarn("Name is required.");         return false; }
        if (tfPhone.getText().trim().isEmpty()) { showWarn("Phone number is required."); return false; }
        return true;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        JComponent l1, JComponent f1, JComponent l2, JComponent f2) {
        g.gridx = 0; g.gridy = row; p.add(l1, g);
        g.gridx = 1;                p.add(f1, g);
        g.gridx = 2;                p.add(l2, g);
        g.gridx = 3;                p.add(f2, g);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this,
            "<html>✅ " + msg + "</html>", "Success", JOptionPane.INFORMATION_MESSAGE);
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
