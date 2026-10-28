package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Pets tab — register, view, search, edit, delete pets.
 */
public class PetsPanel extends JPanel {

    // ── Colors ───────────────────────────────────────────────────────────────
    private static final Color C_GREEN  = new Color(39,  174,  96);
    private static final Color C_BLUE   = new Color(41,  128, 185);
    private static final Color C_RED    = new Color(192,  57,  43);
    private static final Color C_BG     = new Color(245, 248, 252);
    private static final Color C_BORDER = new Color(210, 220, 235);

    private final PetDAO petDAO = new PetDAO();

    private DefaultTableModel tableModel;
    private JTable table;

    private final JTextField        tfName        = styledField("e.g. Buddy",    18);
    private final JTextField        tfSpecies     = styledField("e.g. Dog",      14);
    private final JTextField        tfBreed       = styledField("e.g. Labrador", 14);
    private final JTextField        tfAge         = styledField("0",               5);
    private final JComboBox<String> cbGender      = new JComboBox<>(new String[]{"Male", "Female", "Unknown"});
    private final JTextArea         taDescription = new JTextArea(3, 24);
    private final JTextField        tfSearch      = styledField("Search by name, species or breed…", 22);
    private final JCheckBox         cbAvailable   = new JCheckBox("Available for adoption", true);

    private int selectedPetId = -1;

    public PetsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFormCard(),  BorderLayout.SOUTH);

        loadPets(null);
    }

    // ── Top search bar ───────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(C_BG);

        // Placeholder only — no leading icon (FlatLaf icon requires an ImageIcon, not emoji)
        tfSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                                   "Search by name, species or breed…");

        JButton btnSearch  = actionButton("🔍 Search",  C_BLUE);
        JButton btnShowAll = actionButton("Show All", new Color(100, 116, 139));

        bar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>🐾 Registered Pets</b></html>"));
        bar.add(Box.createHorizontalStrut(20));
        bar.add(tfSearch);
        bar.add(btnSearch);
        bar.add(btnShowAll);

        btnSearch .addActionListener(e -> loadPets(tfSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { tfSearch.setText(""); loadPets(null); });
        return bar;
    }

    // ── Table card ───────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        String[] cols = {"ID", "Name", "Species", "Breed", "Age", "Gender", "Status", "Date Added"};
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

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(4).setMaxWidth(55);
        table.getColumnModel().getColumn(6).setMaxWidth(90);

        // Styled header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        // Status column — coloured badge renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel();
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                boolean available = "Available".equals(val);
                lbl.setText(available ? "● Available" : "● Adopted");
                lbl.setForeground(available ? new Color(21, 128, 61) : new Color(180, 60, 40));
                lbl.setBackground(sel ? new Color(210, 234, 255)
                        : (row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252)));
                return lbl;
            }
        });

        table.getSelectionModel().addListSelectionListener(
                e -> { if (!e.getValueIsAdjusting()) populateFormFromTable(); });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = card();
        card.setLayout(new BorderLayout());
        card.add(scroll);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER),
            new EmptyBorder(0, 10, 0, 10)
        ));
        return card;
    }

    // ── Form card ────────────────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER),
            new EmptyBorder(12, 16, 12, 16)
        ));

        // Section title
        JLabel sectionTitle = new JLabel(
            "<html><span style='font-size:13px; font-weight:bold; color:#1e5aa0;'>"
            + "Pet Details</span>&nbsp;"
            + "<span style='font-size:11px; color:#888;'>(* required)</span></html>");
        card.add(sectionTitle, BorderLayout.NORTH);

        // Fields grid
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;

        // Description — plain JTextArea inside a scrollpane (no FlatLaf arc style on JTextArea)
        taDescription.setRows(3);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        taDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(taDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));

        addRow(fields, g, 0, htmlLabel("Name *"),     tfName,
                            htmlLabel("Species *"),   tfSpecies);
        addRow(fields, g, 1, htmlLabel("Breed"),      tfBreed,
                            htmlLabel("Age (yrs) *"), tfAge);
        addRow(fields, g, 2, htmlLabel("Gender"),     cbGender,
                            cbAvailable,              new JLabel());

        g.gridx = 0; g.gridy = 3;
        fields.add(htmlLabel("Description"), g);
        g.gridx = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        fields.add(descScroll, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        card.add(fields, BorderLayout.CENTER);

        // Action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnAdd    = actionButton("➕  Register Pet", C_GREEN);
        JButton btnUpdate = actionButton("✏️  Update",       C_BLUE);
        JButton btnDelete = actionButton("🗑️  Delete",       C_RED);
        JButton btnClear  = actionButton("✖  Clear",         new Color(100, 116, 139));
        btnRow.add(btnClear);
        btnRow.add(btnDelete);
        btnRow.add(btnUpdate);
        btnRow.add(btnAdd);
        card.add(btnRow, BorderLayout.SOUTH);

        btnAdd   .addActionListener(e -> addPet());
        btnUpdate.addActionListener(e -> updatePet());
        btnDelete.addActionListener(e -> deletePet());
        btnClear .addActionListener(e -> clearForm());
        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void loadPets(String keyword) {
        try {
            List<Pet> pets = (keyword == null || keyword.isEmpty())
                    ? petDAO.getAllPets() : petDAO.searchPets(keyword);
            tableModel.setRowCount(0);
            for (Pet p : pets) {
                tableModel.addRow(new Object[]{
                    p.getPetId(), p.getName(), p.getSpecies(), p.getBreed(),
                    p.getAge(),   p.getGender(),
                    p.isAvailable() ? "Available" : "Adopted",
                    p.getDateAdded()
                });
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void addPet() {
        if (!validateForm()) return;
        try {
            if (petDAO.addPet(buildPetFromForm())) {
                showSuccess("Pet registered successfully!");
                clearForm();
                loadPets(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void updatePet() {
        if (selectedPetId < 0) { showWarn("Select a pet to update."); return; }
        if (!validateForm()) return;
        Pet p = buildPetFromForm();
        p.setPetId    (selectedPetId);
        p.setAvailable(cbAvailable.isSelected());
        try {
            if (petDAO.updatePet(p)) {
                showSuccess("Pet updated successfully!");
                clearForm();
                loadPets(null);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void deletePet() {
        if (selectedPetId < 0) { showWarn("Select a pet to delete."); return; }
        if (JOptionPane.showConfirmDialog(this,
                "<html>Delete this pet?<br><small>This cannot be undone.</small></html>",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            if (petDAO.deletePet(selectedPetId)) {
                showSuccess("Pet deleted.");
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
        cbAvailable.setSelected("Available".equals(tableModel.getValueAt(row, 6)));
        try {
            Pet p = petDAO.getPetById(selectedPetId);
            if (p != null) taDescription.setText(p.getDescription());
        } catch (SQLException ex) { showError(ex); }
    }

    private void clearForm() {
        selectedPetId = -1;
        tfName       .setText("");
        tfSpecies    .setText("");
        tfBreed      .setText("");
        tfAge        .setText("");
        taDescription.setText("");
        cbGender     .setSelectedIndex(0);
        cbAvailable  .setSelected(true);
        table        .clearSelection();
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
        if (tfName.getText().trim().isEmpty())   { showWarn("Pet name is required.");          return false; }
        if (tfSpecies.getText().trim().isEmpty()) { showWarn("Species is required.");           return false; }
        try {
            if (Integer.parseInt(tfAge.getText().trim()) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e)         { showWarn("Age must be a non-negative number."); return false; }
        return true;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        JComponent l1, JComponent f1,
                        JComponent l2, JComponent f2) {
        g.gridx = 0; g.gridy = row; p.add(l1, g);
        g.gridx = 1;                p.add(f1, g);
        g.gridx = 2;                p.add(l2, g);
        g.gridx = 3;                p.add(f2, g);
    }

    // ── Static UI factories (shared by other panels) ─────────────────────────

    static JTextField styledField(String placeholder, int cols) {
        JTextField tf = new JTextField(cols);
        tf.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return tf;
    }

    static JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE,
                              FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JLabel htmlLabel(String text) {
        return new JLabel(
            "<html><span style='color:#444; font-size:12px;'>" + text + "</span></html>");
    }

    static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        return p;
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this,
            "<html><span style='font-size:13px;'>✅ " + msg + "</span></html>",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this,
            "<html><span style='font-size:13px;'>⚠️ " + msg + "</span></html>",
            "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
            "<html><b>Database error:</b><br>" + ex.getMessage() + "</html>",
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
