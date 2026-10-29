package petadoptionmanagementsystem;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Adoptions tab — record adoptions, track fees, view history.
 */
public class AdoptionsPanel extends JPanel {

    private static final Color C_GREEN   = new Color(39,  174,  96);
    private static final Color C_BLUE    = new Color(41,  128, 185);
    private static final Color C_ORANGE  = new Color(230, 126,  34);
    private static final Color C_BG      = new Color(245, 248, 252);
    private static final Color C_BORDER  = new Color(210, 220, 235);

    private final AdoptionDAO adoptionDAO = new AdoptionDAO();
    private final PetDAO      petDAO      = new PetDAO();
    private final AdopterDAO  adopterDAO  = new AdopterDAO();

    private DefaultTableModel tableModel;
    private JTable table;

    private final JComboBox<Pet>     cbPet     = new JComboBox<>();
    private final JComboBox<Adopter> cbAdopter = new JComboBox<>();
    private final JTextField         tfDate    = PetsPanel.styledField("yyyy-MM-dd", 12);
    private final JTextField         tfFee     = PetsPanel.styledField("e.g. 150000", 12);
    private final JComboBox<String>  cbPayment = new JComboBox<>(
            new String[]{"Paid", "Pending", "Waived"});
    private final JTextArea          taNotes   = new JTextArea(2, 20);
    private final JTextField         tfFrom    = PetsPanel.styledField("yyyy-MM-dd", 10);
    private final JTextField         tfTo      = PetsPanel.styledField("yyyy-MM-dd", 10);

    public AdoptionsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(C_BG);

        add(buildFilterBar(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildFormCard(),  BorderLayout.SOUTH);

        loadHistory(null, null);
        refreshDropdowns();
    }

    // ── Filter bar ───────────────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(C_BG);

        JButton btnFilter  = PetsPanel.iconButton("Filter",   IconGenerator.search(15),  C_BLUE);
        JButton btnShowAll = PetsPanel.actionButton("Show All", new Color(100, 116, 139));

        bar.add(new JLabel(IconGenerator.adoptions(22)));
        bar.add(new JLabel(
            "<html><b style='color:#1e5aa0; font-size:13px;'>Adoption History</b></html>"));
        bar.add(Box.createHorizontalStrut(16));
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
        String[] cols = {"ID", "Pet", "Adopter", "Phone", "Date", "Fee (UGX)", "Payment", "Notes"};
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

        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(3).setMaxWidth(110);
        table.getColumnModel().getColumn(4).setMaxWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(110);
        table.getColumnModel().getColumn(6).setMaxWidth(90);

        // Styled header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(30, 90, 160));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));

        // Payment status column — coloured badge
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel();
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String status = val == null ? "" : val.toString();
                switch (status) {
                    case "Paid"    -> { lbl.setText("● Paid");    lbl.setForeground(new Color(21, 128,  61)); }
                    case "Waived"  -> { lbl.setText("● Waived");  lbl.setForeground(new Color(41, 128, 185)); }
                    default        -> { lbl.setText("● Pending"); lbl.setForeground(new Color(180, 100, 20)); }
                }
                lbl.setBackground(sel ? new Color(210, 234, 255)
                        : (row % 2 == 0 ? Color.WHITE : new Color(245, 248, 252)));
                return lbl;
            }
        });

        // Fee column — right-aligned
        DefaultTableCellRenderer feeRenderer = new DefaultTableCellRenderer();
        feeRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(feeRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Mark as Paid button below table
        JButton btnMarkPaid   = PetsPanel.iconButton("Mark as Paid",    IconGenerator.record(15), C_GREEN);
        JButton btnMarkPending = PetsPanel.iconButton("Mark as Pending", IconGenerator.clear(15),  C_ORANGE);
        JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        tableButtons.setOpaque(false);
        tableButtons.add(PetsPanel.htmlLabel("Selected record:"));
        tableButtons.add(btnMarkPaid);
        tableButtons.add(btnMarkPending);

        btnMarkPaid   .addActionListener(e -> updatePayment("Paid"));
        btnMarkPending.addActionListener(e -> updatePayment("Pending"));

        JPanel card = PetsPanel.card();
        card.setLayout(new BorderLayout());
        card.add(scroll, BorderLayout.CENTER);
        card.add(tableButtons, BorderLayout.SOUTH);
        card.setBorder(new EmptyBorder(0, 10, 0, 10));
        return card;
    }

    // ── Form card ────────────────────────────────────────────────────────────

    private JPanel buildFormCard() {
        JPanel card = PetsPanel.card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER),
            new EmptyBorder(10, 16, 10, 16)
        ));

        card.add(new JLabel(
            "<html><span style='font-size:13px; font-weight:bold; color:#1e5aa0;'>"
            + "Record New Adoption</span>&nbsp;"
            + "<span style='font-size:11px; color:#888;'>"
            + "Fee is auto-filled from the species schedule</span></html>"),
            BorderLayout.NORTH);

        tfDate.setText(LocalDate.now().toString());

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.anchor = GridBagConstraints.WEST;

        taNotes.setLineWrap(true); taNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(taNotes);
        notesScroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        notesScroll.setPreferredSize(new Dimension(200, 50));

        // Row 0: Pet | Adopter
        g.gridx = 0; g.gridy = 0; fields.add(PetsPanel.htmlLabel("Pet (available):"), g);
        g.gridx = 1;               fields.add(cbPet, g);
        g.gridx = 2;               fields.add(PetsPanel.htmlLabel("Adopter:"), g);
        g.gridx = 3;               fields.add(cbAdopter, g);

        // Row 1: Date | Fee | Payment status
        g.gridx = 0; g.gridy = 1; fields.add(PetsPanel.htmlLabel("Adoption Date *:"), g);
        g.gridx = 1;               fields.add(tfDate, g);
        g.gridx = 2;               fields.add(PetsPanel.htmlLabel("Fee (UGX):"), g);
        g.gridx = 3;               fields.add(tfFee, g);

        g.gridx = 0; g.gridy = 2; fields.add(PetsPanel.htmlLabel("Payment Status:"), g);
        g.gridx = 1;               fields.add(cbPayment, g);

        // Row 2: Notes
        g.gridx = 0; g.gridy = 3; fields.add(PetsPanel.htmlLabel("Notes:"), g);
        g.gridx = 1; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        fields.add(notesScroll, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;

        card.add(fields, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnRecord  = PetsPanel.iconButton("Record Adoption", IconGenerator.record(16),  C_GREEN);
        JButton btnRefresh = PetsPanel.iconButton("Refresh Lists",   IconGenerator.refresh(16), new Color(100, 116, 139));
        JButton btnFees    = PetsPanel.iconButton("Fee Schedule",    IconGenerator.edit(16),    C_BLUE);
        btnRow.add(btnFees);
        btnRow.add(btnRefresh);
        btnRow.add(btnRecord);
        card.add(btnRow, BorderLayout.SOUTH);

        btnRecord .addActionListener(e -> recordAdoption());
        btnRefresh.addActionListener(e -> refreshDropdowns());
        btnFees   .addActionListener(e -> showFeeSchedule());

        // Auto-fill fee when pet selection changes
        cbPet.addActionListener(e -> autoFillFee());

        return card;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void loadHistory(String from, String to) {
        try {
            List<Adoption> list = (from != null && !from.isEmpty() && to != null && !to.isEmpty())
                    ? adoptionDAO.getAdoptionsByDateRange(from, to)
                    : adoptionDAO.getAllAdoptions();
            tableModel.setRowCount(0);
            for (Adoption a : list) {
                tableModel.addRow(new Object[]{
                    a.getAdoptionId(),
                    a.getPetName(),
                    a.getAdopterName(),
                    a.getAdopterPhone(),
                    a.getAdoptionDate(),
                    UGX.compact(a.getAdoptionFee()),
                    a.getPaymentStatus(),
                    a.getNotes()
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
            autoFillFee();
        } catch (SQLException ex) { showError(ex); }
    }

    /** Auto-fill the fee field based on selected pet's species. */
    private void autoFillFee() {
        Pet pet = (Pet) cbPet.getSelectedItem();
        if (pet == null) { tfFee.setText("0"); return; }
        try {
            double fee = adoptionDAO.getFeeForSpecies(pet.getSpecies());
            tfFee.setText(UGX.compact(fee));
        } catch (SQLException ex) { tfFee.setText("0"); }
    }

    private void recordAdoption() {
        Pet     pet     = (Pet)     cbPet.getSelectedItem();
        Adopter adopter = (Adopter) cbAdopter.getSelectedItem();
        if (pet == null)     { showWarn("No available pets. Register a pet first.");      return; }
        if (adopter == null) { showWarn("No adopters found. Register an adopter first."); return; }
        if (tfDate.getText().trim().isEmpty()) { showWarn("Adoption date is required.");  return; }

        double fee;
        try {
            fee = UGX.parse(tfFee.getText().trim());
            if (fee < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showWarn("Fee must be a valid non-negative number (e.g. 150000)."); return;
        }

        String payStatus = (String) cbPayment.getSelectedItem();

        // Confirmation dialog showing fee in UGX
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>"
            + "Record adoption of <b>" + pet.getName() + "</b> "
            + "(" + pet.getSpecies() + ")<br>"
            + "by <b>" + adopter.getName() + "</b>?<br><br>"
            + "<table cellpadding='3'>"
            + "<tr><td>Adoption Fee:</td><td><b>" + UGX.format(fee) + "</b></td></tr>"
            + "<tr><td>Payment Status:</td><td><b>" + payStatus + "</b></td></tr>"
            + "</table><br>"
            + "<small style='color:gray;'>Pet will be marked as no longer available.</small>"
            + "</html>",
            "Confirm Adoption", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Adoption adoption = new Adoption();
        adoption.setPetId        (pet.getPetId());
        adoption.setAdopterId    (adopter.getAdopterId());
        adoption.setAdoptionDate (tfDate.getText().trim());
        adoption.setNotes        (taNotes.getText().trim());
        adoption.setAdoptionFee  (fee);
        adoption.setPaymentStatus(payStatus);

        try {
            if (adoptionDAO.recordAdoption(adoption)) {
                JOptionPane.showMessageDialog(this,
                    "<html>✅ Adoption recorded!<br>"
                    + "<b>" + pet.getName() + "</b> → <b>" + adopter.getName() + "</b><br>"
                    + "Fee: <b>" + UGX.format(fee) + "</b> — " + payStatus
                    + "</html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                taNotes.setText("");
                tfDate.setText(LocalDate.now().toString());
                loadHistory(null, null);
                refreshDropdowns();
            }
        } catch (SQLException ex) { showError(ex); }
    }

    /** Update payment status of the selected table row. */
    private void updatePayment(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) { showWarn("Select a record in the table first."); return; }
        int adoptionId = (int) tableModel.getValueAt(row, 0);
        try {
            if (adoptionDAO.updatePaymentStatus(adoptionId, newStatus)) {
                tableModel.setValueAt(newStatus, row, 6);
                JOptionPane.showMessageDialog(this,
                    "<html>✅ Payment status updated to <b>" + newStatus + "</b>.</html>",
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showFeeSchedule() {
        String[] cols = {"Species", "Fee (UGX)"};
        DefaultTableModel feeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 1; }
        };
        try {
            for (Object[] row : adoptionDAO.getAllFees())
                feeModel.addRow(new Object[]{ row[0], UGX.compact((double) row[1]) });
        } catch (SQLException ex) { showError(ex); return; }

        JTable feeTable = new JTable(feeModel);
        feeTable.setRowHeight(28);
        feeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        feeTable.getTableHeader().setBackground(new Color(30, 90, 160));
        feeTable.getTableHeader().setForeground(Color.WHITE);
        feeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField tfSpecies = PetsPanel.styledField("e.g. Guinea Pig", 14);
        JTextField tfNewFee  = PetsPanel.styledField("e.g. 80000",       10);
        JButton    btnAdd    = PetsPanel.iconButton("Add", IconGenerator.add(14), C_GREEN);

        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addRow.add(PetsPanel.htmlLabel("Species:"));
        addRow.add(tfSpecies);
        addRow.add(PetsPanel.htmlLabel("Fee (UGX):"));
        addRow.add(tfNewFee);
        addRow.add(btnAdd);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(new JLabel(
            "<html><b style='color:#1e5aa0;'>Adoption Fee Schedule — Uganda Shillings (UGX)</b><br>"
            + "<small style='color:#888;'>Double-click the Fee column to edit. Enter whole numbers (e.g. 150000).</small></html>"),
            BorderLayout.NORTH);
        panel.add(new JScrollPane(feeTable), BorderLayout.CENTER);
        panel.add(addRow, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(400, 300));

        btnAdd.addActionListener(e -> {
            String sp = tfSpecies.getText().trim();
            if (sp.isEmpty()) { JOptionPane.showMessageDialog(panel, "Enter a species name."); return; }
            try {
                double f = UGX.parse(tfNewFee.getText().trim());
                adoptionDAO.setFee(sp, f);
                feeModel.addRow(new Object[]{ sp, UGX.compact(f) });
                tfSpecies.setText(""); tfNewFee.setText("");
            } catch (Exception ex) { showError(ex); }
        });

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Fee Schedule (UGX)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                for (int r = 0; r < feeModel.getRowCount(); r++) {
                    String sp = feeModel.getValueAt(r, 0).toString();
                    double f  = UGX.parse(feeModel.getValueAt(r, 1).toString());
                    adoptionDAO.setFee(sp, f);
                }
            } catch (Exception ex) { showError(ex); }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this,
            "<html>⚠️ " + msg + "</html>", "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
            "<html><b>Error:</b><br>" + ex.getMessage() + "</html>",
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}
