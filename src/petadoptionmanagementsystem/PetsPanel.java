package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Pets tab — register, view, search, edit, delete pets.
 * Supports optional pet photo: browse, preview in form, thumbnail in table,
 * full-size popup on double-click.
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

    // ── Form fields ──────────────────────────────────────────────────────────
    private final JTextField        tfName        = styledField("e.g. Buddy",    18);
    private final JTextField        tfSpecies     = styledField("e.g. Dog",      14);
    private final JTextField        tfBreed       = styledField("e.g. Labrador", 14);
    private final JTextField        tfAge         = styledField("0",               5);
    private final JComboBox<String> cbGender      = new JComboBox<>(new String[]{"Male", "Female", "Unknown"});
    private final JTextArea         taDescription = new JTextArea(2, 20);
    private final JTextField        tfSearch      = styledField("Search by name, species or breed…", 22);
    private final JCheckBox         cbAvailable   = new JCheckBox("Available for adoption", true);

    // ── Photo controls ───────────────────────────────────────────────────────
    private String  selectedPhotoPath = null;
    private final JLabel  photoPreview  = new JLabel();     // shows preview in form
    private final JButton btnBrowse     = iconButton("Browse Photo", IconGenerator.add(14), C_BLUE);
    private final JButton btnRemove     = iconButton("Remove",        IconGenerator.clear(14), new Color(150, 150, 150));

    private int selectedPetId = -1;

    public PetsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFormCard(),  BorderLayout.SOUTH);

        loadPets(null);
    }

    // ── Top search bar ────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(C_BG);

        tfSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                                   "Search by name, species or breed…");

        JButton btnSearch  = iconButton("Search",   IconGenerator.search(15), C_BLUE);
        JButton btnShowAll = actionButton("Show All", new Color(100, 116, 139));

        bar.add(new JLabel(IconGenerator.pets(22)));
        bar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>Registered Pets</b></html>"));
        bar.add(Box.createHorizontalStrut(20));
        bar.add(tfSearch);
        bar.add(btnSearch);
        bar.add(btnShowAll);

        btnSearch .addActionListener(e -> loadPets(tfSearch.getText().trim()));
        btnShowAll.addActionListener(e -> { tfSearch.setText(""); loadPets(null); });
        return bar;
    }

    // ── Table card ────────────────────────────────────────────────────────────

    private JPanel buildTableCard() {
        String[] cols = {"ID", "Photo", "Name", "Species", "Breed", "Age", "Gender", "Status", "Date Added"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 1 ? ImageIcon.class : Object.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(PetImageUtil.THUMB_H + 6);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(210, 234, 255));
        table.setSelectionForeground(Color.BLACK);

        // Column widths
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setMinWidth(PetImageUtil.THUMB_W + 8);
        table.getColumnModel().getColumn(1).setMaxWidth(PetImageUtil.THUMB_W + 8);
        table.getColumnModel().getColumn(5).setMaxWidth(50);
        table.getColumnModel().getColumn(7).setMaxWidth(90);

        // Styled header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        // Photo column renderer — centres the thumbnail
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel();
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(sel ? new Color(210, 234, 255)
                        : (row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252)));
                if (val instanceof ImageIcon) lbl.setIcon((ImageIcon) val);
                return lbl;
            }
        });

        // Status column — coloured badge
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Double-click a row → show full-size photo popup
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) showFullPhoto();
            }
        });

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

    // ── Form card ─────────────────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER),
            new EmptyBorder(10, 16, 10, 16)
        ));

        card.add(new JLabel(
            "<html><span style='font-size:13px; font-weight:bold; color:#1e5aa0;'>"
            + "Pet Details</span>&nbsp;"
            + "<span style='font-size:11px; color:#888;'>(* required)</span></html>"),
            BorderLayout.NORTH);

        // ── Left: fields grid ────────────────────────────────────────────────
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.anchor = GridBagConstraints.WEST;

        taDescription.setRows(2);
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

        // ── Right: photo panel ───────────────────────────────────────────────
        JPanel photoPanel = buildPhotoPanel();

        // ── Combined centre ──────────────────────────────────────────────────
        JPanel centre = new JPanel(new BorderLayout(12, 0));
        centre.setOpaque(false);
        centre.add(fields,     BorderLayout.CENTER);
        centre.add(photoPanel, BorderLayout.EAST);
        card.add(centre, BorderLayout.CENTER);

        // ── Action buttons ───────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnAdd    = iconButton("Register Pet", IconGenerator.add(16),    C_GREEN);
        JButton btnUpdate = iconButton("Update",       IconGenerator.edit(16),   C_BLUE);
        JButton btnDelete = iconButton("Delete",       IconGenerator.delete(16), C_RED);
        JButton btnClear  = iconButton("Clear",        IconGenerator.clear(16),  new Color(100, 116, 139));
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

    // ── Photo panel ───────────────────────────────────────────────────────────

    private JPanel buildPhotoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1, true),
            new EmptyBorder(6, 6, 6, 6)
        ));
        panel.setPreferredSize(new Dimension(
                PetImageUtil.PREVIEW_W + 14,
                PetImageUtil.PREVIEW_H + 60));

        // Title
        panel.add(new JLabel(
            "<html><span style='font-size:11px; color:#555;'>Pet Photo</span></html>",
            SwingConstants.CENTER), BorderLayout.NORTH);

        // Preview label
        photoPreview.setPreferredSize(new Dimension(
                PetImageUtil.PREVIEW_W, PetImageUtil.PREVIEW_H));
        photoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreview.setVerticalAlignment(SwingConstants.CENTER);
        photoPreview.setIcon(PetImageUtil.preview(null));
        panel.add(photoPreview, BorderLayout.CENTER);

        // Browse / Remove buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnBrowse);
        btnRow.add(btnRemove);
        panel.add(btnRow, BorderLayout.SOUTH);

        btnBrowse.addActionListener(e -> browsePhoto());
        btnRemove.addActionListener(e -> removePhoto());
        return panel;
    }

    // ── Photo actions ─────────────────────────────────────────────────────────

    private void browsePhoto() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Pet Photo");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Image files (JPG, PNG, GIF, BMP)", "jpg", "jpeg", "png", "gif", "bmp"));
        fc.setAcceptAllFileFilterUsed(false);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPhotoPath = fc.getSelectedFile().getAbsolutePath();
            photoPreview.setIcon(PetImageUtil.preview(selectedPhotoPath));
        }
    }

    private void removePhoto() {
        selectedPhotoPath = null;
        photoPreview.setIcon(PetImageUtil.preview(null));
    }

    /** Shows a full-size popup of the selected row's photo. */
    private void showFullPhoto() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int petId = (int) tableModel.getValueAt(row, 0);
        try {
            Pet p = petDAO.getPetById(petId);
            if (p == null) return;
            if (p.getPhotoPath() == null || p.getPhotoPath().isBlank()) {
                JOptionPane.showMessageDialog(this,
                    "<html>No photo registered for <b>" + p.getName() + "</b>.</html>",
                    "No Photo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ImageIcon full = PetImageUtil.full(p.getPhotoPath());
            JLabel imgLbl  = new JLabel(full);
            JLabel caption = new JLabel(
                "<html><b>" + p.getName() + "</b> · " + p.getSpecies()
                + (p.getBreed() != null && !p.getBreed().isBlank() ? " · " + p.getBreed() : "")
                + "</html>", SwingConstants.CENTER);
            caption.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JPanel dlgPanel = new JPanel(new BorderLayout(0, 8));
            dlgPanel.add(imgLbl,   BorderLayout.CENTER);
            dlgPanel.add(caption,  BorderLayout.SOUTH);
            JOptionPane.showMessageDialog(this, dlgPanel,
                    "Photo — " + p.getName(), JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) { showError(ex); }
    }

    // ── CRUD actions ──────────────────────────────────────────────────────────

    private void loadPets(String keyword) {
        try {
            List<Pet> pets = (keyword == null || keyword.isEmpty())
                    ? petDAO.getAllPets() : petDAO.searchPets(keyword);
            tableModel.setRowCount(0);
            for (Pet p : pets) {
                tableModel.addRow(new Object[]{
                    p.getPetId(),
                    PetImageUtil.thumbnail(p.getPhotoPath()),   // ← thumbnail
                    p.getName(), p.getSpecies(), p.getBreed(),
                    p.getAge(),  p.getGender(),
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

    // ── Form helpers ──────────────────────────────────────────────────────────

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedPetId = (int) tableModel.getValueAt(row, 0);
        tfName    .setText((String) tableModel.getValueAt(row, 2));
        tfSpecies .setText((String) tableModel.getValueAt(row, 3));
        tfBreed   .setText((String) tableModel.getValueAt(row, 4));
        tfAge     .setText(String.valueOf(tableModel.getValueAt(row, 5)));
        cbGender  .setSelectedItem(tableModel.getValueAt(row, 6));
        cbAvailable.setSelected("Available".equals(tableModel.getValueAt(row, 7)));
        try {
            Pet p = petDAO.getPetById(selectedPetId);
            if (p != null) {
                taDescription    .setText(p.getDescription());
                selectedPhotoPath = p.getPhotoPath();
                photoPreview.setIcon(PetImageUtil.preview(selectedPhotoPath));
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void clearForm() {
        selectedPetId     = -1;
        selectedPhotoPath = null;
        tfName       .setText("");
        tfSpecies    .setText("");
        tfBreed      .setText("");
        tfAge        .setText("");
        taDescription.setText("");
        cbGender     .setSelectedIndex(0);
        cbAvailable  .setSelected(true);
        photoPreview .setIcon(PetImageUtil.preview(null));
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
        p.setPhotoPath  (selectedPhotoPath);
        return p;
    }

    private boolean validateForm() {
        if (tfName.getText().trim().isEmpty())    { showWarn("Pet name is required.");          return false; }
        if (tfSpecies.getText().trim().isEmpty())  { showWarn("Species is required.");           return false; }
        try {
            if (Integer.parseInt(tfAge.getText().trim()) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e)          { showWarn("Age must be a non-negative number."); return false; }
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

    // ── Static UI factories (shared by other panels) ──────────────────────────

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

    static JButton iconButton(String text, ImageIcon icon, Color bg) {
        JButton btn = new JButton(text, icon);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setIconTextGap(6);
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

    // ── Dialogs ───────────────────────────────────────────────────────────────

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
