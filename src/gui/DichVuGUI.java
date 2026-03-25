package gui;

import gui.common.AppBranding;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DichVuGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final String[] SERVICE_GROUP_OPTIONS = {"Minibar", "Giặt là", "Ăn uống", "Di chuyển"};

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<ServiceCatalogRecord> serviceCatalogs = new ArrayList<ServiceCatalogRecord>();
    private final List<ServiceUsageRecord> allServiceUsages = new ArrayList<ServiceUsageRecord>();
    private final List<ServiceUsageRecord> filteredServiceUsages = new ArrayList<ServiceUsageRecord>();

    private JTable tblDichVu;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboNhomDichVu;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblMaPhatSinh;
    private JLabel lblHoSo;
    private JLabel lblKhachPhong;
    private JLabel lblNhomDichVu;
    private JLabel lblTenDichVu;
    private JLabel lblThanhTien;
    private JLabel lblTrangThaiChiTiet;
    private JTextArea txtMoTa;

    public DichVuGUI() {
        this("guest", "Lễ tân");
    }

    public DichVuGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý dịch vụ - Hotel PMS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        seedSampleData();
        initUI();
        reloadSampleData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.DICH_VU, username, role), BorderLayout.WEST);
        root.add(buildMainContent(), BorderLayout.CENTER);

        rootPanel = root;
        setContentPane(root);
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(buildHeader());
        top.add(Box.createVerticalStrut(10));
        top.add(buildActionBar());
        top.add(Box.createVerticalStrut(10));
        top.add(buildFilterBar());

        main.add(top, BorderLayout.NORTH);
        main.add(buildCenterContent(), BorderLayout.CENTER);
        main.add(buildFooter(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildHeader() {
        JPanel card = createCardPanel(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ DỊCH VỤ"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi dịch vụ phát sinh theo hồ sơ lưu trú và quản lý danh mục dịch vụ nền của hệ thống.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_MUTED);

        JLabel lblMeta = new JLabel("Người dùng: " + username + " | Vai trò: " + role);
        lblMeta.setFont(BODY_FONT);
        lblMeta.setForeground(TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);
        left.add(Box.createVerticalStrut(6));
        left.add(lblMeta);

        card.add(left, BorderLayout.WEST);
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Dịch vụ"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Thêm dịch vụ", new Color(22, 163, 74), Color.WHITE, e -> openServiceUsageDialog(null)));
        card.add(createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openUpdateServiceUsageDialog()));
        card.add(createPrimaryButton("Chốt dịch vụ", new Color(245, 158, 11), TEXT_PRIMARY, e -> openCloseServiceDialog()));
        card.add(createPrimaryButton("Thêm danh mục", new Color(99, 102, 241), Color.WHITE, e -> openServiceCatalogDialog(null)));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboNhomDichVu = createComboBox(new String[]{"Tất cả", "Minibar", "Giặt là", "Ăn uống", "Di chuyển"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Mới ghi nhận", "Đã chốt", "Đã hủy"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(290, 34));
        txtTuKhoa.setToolTipText("Mã hồ sơ / khách / phòng / dịch vụ");

        left.add(createFieldGroup("Nhóm dịch vụ", cboNhomDichVu));
        left.add(createFieldGroup("Trạng thái", cboTrangThai));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblSearch = new JLabel("Tìm kiếm");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);
        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(txtTuKhoa, BorderLayout.CENTER);
        searchRow.add(createOutlineButton("Lọc ngay", new Color(59, 130, 246), e -> applyFilters(true)), BorderLayout.EAST);
        right.add(searchRow);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JSplitPane buildCenterContent() {
        JPanel left = buildTableCard();
        JPanel right = buildDetailCard();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách phát sinh dịch vụ");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chọn một dòng phát sinh để xem chi tiết và trạng thái chốt.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {"Mã PSDV", "Hồ sơ", "Khách / Phòng", "Nhóm", "Dịch vụ", "Thành tiền", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDichVu = new JTable(tableModel);
        tblDichVu.setFont(BODY_FONT);
        tblDichVu.setRowHeight(32);
        tblDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDichVu.setGridColor(BORDER_SOFT);
        tblDichVu.setShowGrid(true);
        tblDichVu.setFillsViewportHeight(true);
        tblDichVu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblDichVu.getTableHeader().setBackground(new Color(243, 244, 246));
        tblDichVu.getTableHeader().setForeground(TEXT_PRIMARY);

        tblDichVu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblDichVu.getSelectedRow();
                if (row >= 0 && row < filteredServiceUsages.size()) {
                    updateDetailPanel(filteredServiceUsages.get(row));
                }
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblDichVu, this::openUpdateServiceUsageDialog);

        JScrollPane scrollPane = new JScrollPane(tblDichVu);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(titleRow, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel wrapper = createCardPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết dịch vụ phát sinh");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(7, 2, 10, 8));
        body.setOpaque(false);

        lblMaPhatSinh = createValueLabel();
        lblHoSo = createValueLabel();
        lblKhachPhong = createValueLabel();
        lblNhomDichVu = createValueLabel();
        lblTenDichVu = createValueLabel();
        lblThanhTien = createValueLabel();
        lblTrangThaiChiTiet = createValueLabel();

        addDetailRow(body, "Mã phát sinh", lblMaPhatSinh);
        addDetailRow(body, "Hồ sơ lưu trú", lblHoSo);
        addDetailRow(body, "Khách / Phòng", lblKhachPhong);
        addDetailRow(body, "Nhóm dịch vụ", lblNhomDichVu);
        addDetailRow(body, "Tên dịch vụ", lblTenDichVu);
        addDetailRow(body, "Thành tiền", lblThanhTien);
        addDetailRow(body, "Trạng thái", lblTrangThaiChiTiet);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);

        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);

        txtMoTa = new JTextArea(5, 20);
        txtMoTa.setEditable(false);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setFont(BODY_FONT);
        txtMoTa.setForeground(TEXT_PRIMARY);
        txtMoTa.setBackground(PANEL_SOFT);
        txtMoTa.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane noteScroll = new JScrollPane(txtMoTa);
        noteScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        notePanel.add(lblNote, BorderLayout.NORTH);
        notePanel.add(noteScroll, BorderLayout.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(notePanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Thêm dịch vụ",
                "F2 Cập nhật",
                "F3 Chốt dịch vụ",
                "F4 Thêm danh mục",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private JPanel createFieldGroup(String label, Component component) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(TEXT_MUTED);

        group.add(lbl);
        group.add(Box.createVerticalStrut(4));
        group.add(component);
        return group;
    }

    private JComboBox<String> createComboBox(String[] values) {
        JComboBox<String> comboBox = new JComboBox<String>(values);
        comboBox.setFont(BODY_FONT);
        comboBox.setPreferredSize(new Dimension(165, 34));
        comboBox.setMaximumSize(new Dimension(220, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(180, 34));
        field.setMaximumSize(new Dimension(320, 34));
        return field;
    }

    private JButton createPrimaryButton(String text, Color background, Color foreground, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JButton createOutlineButton(String text, Color borderColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.addActionListener(listener);
        return button;
    }

    private void addDetailRow(JPanel panel, String label, JLabel value) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl);
        panel.add(value);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JPanel createCardPanel(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return panel;
    }

    private JPanel createCompactCardPanel(FlowLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return panel;
    }

    private void seedSampleData() {
        serviceCatalogs.clear();
        allServiceUsages.clear();
    }

    private void reloadSampleData(boolean showMessage) {
        cboNhomDichVu.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu dịch vụ.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredServiceUsages.clear();

        String nhomDichVu = valueOf(cboNhomDichVu.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (ServiceUsageRecord service : allServiceUsages) {
            if (!"Tất cả".equals(nhomDichVu) && !service.nhomDichVu.equals(nhomDichVu)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !service.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (service.maPhatSinh + " " + service.hoSoLuuTru + " " + service.khachPhong + " " + service.tenDichVu).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredServiceUsages.add(service);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredServiceUsages.size() + " dịch vụ phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (ServiceUsageRecord service : filteredServiceUsages) {
            tableModel.addRow(new Object[]{
                    service.maPhatSinh,
                    service.hoSoLuuTru,
                    service.khachPhong,
                    service.nhomDichVu,
                    service.tenDichVu,
                    service.getThanhTienLabel(),
                    service.trangThai
            });
        }

        if (!filteredServiceUsages.isEmpty()) {
            tblDichVu.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredServiceUsages.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(ServiceUsageRecord service) {
        lblMaPhatSinh.setText(service.maPhatSinh);
        lblHoSo.setText(service.hoSoLuuTru);
        lblKhachPhong.setText(service.khachPhong);
        lblNhomDichVu.setText(service.nhomDichVu);
        lblTenDichVu.setText(service.tenDichVu);
        lblThanhTien.setText(service.getThanhTienLabel());
        lblTrangThaiChiTiet.setText(service.trangThai);
        txtMoTa.setText(service.buildDetailNote());
        txtMoTa.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblMaPhatSinh.setText("-");
        lblHoSo.setText("-");
        lblKhachPhong.setText("-");
        lblNhomDichVu.setText("-");
        lblTenDichVu.setText("-");
        lblThanhTien.setText("-");
        lblTrangThaiChiTiet.setText("-");
        txtMoTa.setText("Không có dữ liệu phù hợp.");
    }

    private ServiceUsageRecord getSelectedServiceUsage() {
        int row = tblDichVu.getSelectedRow();
        if (row < 0 || row >= filteredServiceUsages.size()) {
            showWarning("Vui lòng chọn một dòng dịch vụ trong danh sách.");
            return null;
        }
        return filteredServiceUsages.get(row);
    }

    private void openServiceUsageDialog(ServiceUsageRecord record) {
        new ServiceUsageDialog(this, record).setVisible(true);
    }

    private void openUpdateServiceUsageDialog() {
        ServiceUsageRecord record = getSelectedServiceUsage();
        if (record != null) {
            openServiceUsageDialog(record);
        }
    }

    private void openCloseServiceDialog() {
        ServiceUsageRecord record = getSelectedServiceUsage();
        if (record != null) {
            new CloseServiceDialog(this, record).setVisible(true);
        }
    }

    private void openServiceCatalogDialog(ServiceCatalogRecord record) {
        new ServiceCatalogDialog(this, record).setVisible(true);
    }

    private void openServiceUsageDetailDialog() {
        ServiceUsageRecord record = getSelectedServiceUsage();
        if (record != null) {
            new ServiceUsageDetailDialog(this, record).setVisible(true);
        }
    }

    private void addServiceUsage(ServiceUsageRecord record, boolean keepOpen) {
        allServiceUsages.add(0, record);
        cboNhomDichVu.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        selectServiceUsage(record);
        showSuccess(keepOpen ? "Thêm dịch vụ thành công và sẵn sàng ghi nhận tiếp." : "Thêm dịch vụ thành công.");
    }

    private void refreshServiceUsage(ServiceUsageRecord record, String message) {
        applyFilters(false);
        selectServiceUsage(record);
        showSuccess(message);
    }

    private void selectServiceUsage(ServiceUsageRecord record) {
        if (record == null) {
            return;
        }
        int index = filteredServiceUsages.indexOf(record);
        if (index >= 0) {
            tblDichVu.setRowSelectionInterval(index, index);
            updateDetailPanel(record);
        } else if (!filteredServiceUsages.isEmpty()) {
            tblDichVu.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredServiceUsages.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void addCatalog(ServiceCatalogRecord record, boolean keepOpen) {
        serviceCatalogs.add(0, record);
        showSuccess(keepOpen ? "Thêm dịch vụ danh mục thành công và sẵn sàng tạo tiếp." : "Thêm dịch vụ danh mục thành công.");
    }

    private void showSuccess(String message) {
        showMessageDialog("Thành công", message, new Color(22, 163, 74));
    }

    private void showWarning(String message) {
        showMessageDialog("Thông báo", message, new Color(245, 158, 11));
    }

    private void showError(String message) {
        showMessageDialog("Cảnh báo", message, new Color(220, 38, 38));
    }

    private void showMessageDialog(String title, String message, Color accentColor) {
        AppMessageDialog dialog = new AppMessageDialog(this, title, message, accentColor);
        dialog.setVisible(true);
    }

    private boolean showConfirmDialog(String title, String message, String confirmText, Color confirmColor) {
        ConfirmDialog dialog = new ConfirmDialog(this, title, message, confirmText, confirmColor);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "dichvu-f1", () -> openServiceUsageDialog(null));
        ScreenUIHelper.registerShortcut(this, "F2", "dichvu-f2", this::openUpdateServiceUsageDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "dichvu-f3", this::openCloseServiceDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "dichvu-f4", () -> openServiceCatalogDialog(null));
        ScreenUIHelper.registerShortcut(this, "F5", "dichvu-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "dichvu-enter", this::openServiceUsageDetailDialog);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private JPanel createDialogCardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        return panel;
    }

    private JPanel createDialogFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    private JTextArea createDialogTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(BODY_FONT);
        area.setForeground(TEXT_PRIMARY);
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "%,.0f", amount).replace(',', '.');
    }

    private double parseMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(value.trim().replace(".", ""));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private ServiceCatalogRecord findCatalogByDisplay(String display) {
        for (ServiceCatalogRecord catalog : serviceCatalogs) {
            if ((catalog.maDichVu + " - " + catalog.tenDichVu).equals(display)) {
                return catalog;
            }
        }
        return null;
    }

    private abstract class BaseServiceDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseServiceDialog(Frame owner, String title, int width, int height) {
            super(ScreenUIHelper.resolveDialogOwner(owner), title, true);
            this.minimumWidth = width;
            this.minimumHeight = height;
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            getContentPane().setBackground(APP_BG);
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
            setLayout(new BorderLayout());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible) {
                ScreenUIHelper.prepareDialog(this, getOwner(), minimumWidth, minimumHeight);
            }
            super.setVisible(visible);
        }

        protected JPanel buildDialogHeader(String title, String subtitle) {
            JPanel panel = createDialogCardPanel();
            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTitle.setForeground(TEXT_PRIMARY);

            JLabel lblSub = new JLabel("<html>" + subtitle + "</html>");
            lblSub.setFont(BODY_FONT);
            lblSub.setForeground(TEXT_MUTED);

            content.add(lblTitle);
            content.add(Box.createVerticalStrut(6));
            content.add(lblSub);
            panel.add(content, BorderLayout.CENTER);
            return panel;
        }

        protected JPanel buildDialogButtons(JButton... buttons) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            panel.setOpaque(false);
            for (JButton button : buttons) {
                panel.add(button);
            }
            return panel;
        }
    }

    private final class ServiceUsageDialog extends BaseServiceDialog {
        private final ServiceUsageRecord editingRecord;
        private final boolean editing;
        private JComboBox<String> cboHoSo;
        private JTextField txtKhachPhong;
        private JTextField txtTrangThaiHoSo;
        private JComboBox<String> cboNhom;
        private JComboBox<String> cboDichVuDanhMuc;
        private JTextField txtDonGiaMoi;
        private JTextField txtSoLuong;
        private JTextField txtThanhTien;
        private JTextField txtThoiGianSuDung;
        private JTextField txtNguoiGhiNhan;
        private JTextArea txtGhiChuMoi;

        private ServiceUsageDialog(Frame owner, ServiceUsageRecord record) {
            super(owner, record == null ? "Thêm dịch vụ phát sinh" : "Cập nhật dịch vụ phát sinh", 760, 560);
            this.editingRecord = record;
            this.editing = record != null;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    editing ? "CẬP NHẬT DỊCH VỤ PHÁT SINH" : "THÊM DỊCH VỤ PHÁT SINH",
                    "Ghi nhận dịch vụ thực tế cho khách đang ở, khác với dịch vụ danh mục nền của hệ thống."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboHoSo = createComboBox(new String[]{"HS001", "HS002", "HS003"});
            txtKhachPhong = createInputField(editing ? editingRecord.khachPhong : "Nguyễn Minh Anh / P202");
            txtTrangThaiHoSo = createInputField(editing ? editingRecord.trangThaiHoSo : "Đang ở");
            txtTrangThaiHoSo.setEditable(false);
            cboNhom = createComboBox(SERVICE_GROUP_OPTIONS);
            cboDichVuDanhMuc = createComboBox(getCatalogNames());
            txtDonGiaMoi = createInputField(editing ? formatMoney(editingRecord.donGia) : "15000");
            txtSoLuong = createInputField(editing ? String.valueOf(editingRecord.soLuong) : "1");
            txtThanhTien = createInputField(editing ? editingRecord.getThanhTienLabel() : "15000");
            txtThanhTien.setEditable(false);
            txtThoiGianSuDung = createInputField(editing ? editingRecord.thoiGianSuDung : "10:30 19/03/2026");
            txtNguoiGhiNhan = createInputField(editing ? editingRecord.nguoiGhiNhan : "Lễ tân Lan");
            txtGhiChuMoi = createDialogTextArea(3);

            if (editing) {
                cboHoSo.setSelectedItem(editingRecord.hoSoLuuTru);
                cboNhom.setSelectedItem(editingRecord.nhomDichVu);
                cboDichVuDanhMuc.setSelectedItem(editingRecord.maDanhMuc + " - " + editingRecord.tenDichVu);
                txtGhiChuMoi.setText(editingRecord.ghiChu);
            }

            cboDichVuDanhMuc.addActionListener(e -> fillCatalogPrice());

            addFormRow(form, gbc, 0, "Hồ sơ lưu trú", cboHoSo);
            addFormRow(form, gbc, 1, "Khách / Phòng", txtKhachPhong);
            addFormRow(form, gbc, 2, "Trạng thái hồ sơ", txtTrangThaiHoSo);
            addFormRow(form, gbc, 3, "Nhóm dịch vụ", cboNhom);
            addFormRow(form, gbc, 4, "Dịch vụ", cboDichVuDanhMuc);
            addFormRow(form, gbc, 5, "Đơn giá", txtDonGiaMoi);
            addFormRow(form, gbc, 6, "Số lượng", txtSoLuong);
            addFormRow(form, gbc, 7, "Thành tiền", txtThanhTien);
            addFormRow(form, gbc, 8, "Thời gian sử dụng", txtThoiGianSuDung);
            addFormRow(form, gbc, 9, "Người ghi nhận", txtNguoiGhiNhan);
            addFormRow(form, gbc, 10, "Ghi chú", new JScrollPane(txtGhiChuMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton(editing ? "Lưu cập nhật" : "Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnSaveNext = createOutlineButton("Lưu và thêm tiếp", new Color(37, 99, 235), e -> submit(true));
            btnSaveNext.setVisible(!editing);
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSaveNext, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private String[] getCatalogNames() {
            String[] values = new String[serviceCatalogs.size()];
            for (int i = 0; i < serviceCatalogs.size(); i++) {
                values[i] = serviceCatalogs.get(i).maDichVu + " - " + serviceCatalogs.get(i).tenDichVu;
            }
            return values;
        }

        private void fillCatalogPrice() {
            ServiceCatalogRecord catalog = findCatalogByDisplay(valueOf(cboDichVuDanhMuc.getSelectedItem()));
            if (catalog != null) {
                cboNhom.setSelectedItem(catalog.nhomDichVu);
                txtDonGiaMoi.setText(formatMoney(catalog.donGiaMacDinh));
                updateThanhTien();
            }
        }

        private void updateThanhTien() {
            double donGia = parseMoney(txtDonGiaMoi.getText().trim());
            int soLuong;
            try {
                soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            } catch (NumberFormatException ex) {
                soLuong = 0;
            }
            txtThanhTien.setText(donGia < 0 || soLuong <= 0 ? "0" : formatMoney(donGia * soLuong));
        }

        private void submit(boolean keepOpen) {
            if ("Đã check-out".equals(txtTrangThaiHoSo.getText().trim())) {
                showError("Không cho thêm nếu hồ sơ đã check-out.");
                return;
            }
            if (editing && "Đã chốt".equals(editingRecord.trangThai)) {
                showError("Không cho sửa nếu dịch vụ đã bị chốt.");
                return;
            }

            ServiceCatalogRecord catalog = findCatalogByDisplay(valueOf(cboDichVuDanhMuc.getSelectedItem()));
            if (catalog == null) {
                showError("Dịch vụ bắt buộc chọn.");
                return;
            }

            double donGia = parseMoney(txtDonGiaMoi.getText().trim());
            int soLuong;
            try {
                soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Số lượng > 0.");
                return;
            }
            if (donGia < 0 || soLuong <= 0) {
                showError("Đơn giá hợp lệ và số lượng phải > 0.");
                return;
            }

            ServiceUsageRecord target = editing
                    ? editingRecord
                    : ServiceUsageRecord.create(
                    "PSDV" + String.format(Locale.US, "%03d", allServiceUsages.size() + 1),
                    valueOf(cboHoSo.getSelectedItem()),
                    txtKhachPhong.getText().trim(),
                    txtTrangThaiHoSo.getText().trim(),
                    catalog,
                    soLuong,
                    txtThoiGianSuDung.getText().trim(),
                    txtNguoiGhiNhan.getText().trim(),
                    "Mới ghi nhận"
            );

            target.hoSoLuuTru = valueOf(cboHoSo.getSelectedItem());
            target.khachPhong = txtKhachPhong.getText().trim();
            target.trangThaiHoSo = txtTrangThaiHoSo.getText().trim();
            target.maDanhMuc = catalog.maDichVu;
            target.tenDichVu = catalog.tenDichVu;
            target.nhomDichVu = catalog.nhomDichVu;
            target.donViTinh = catalog.donViTinh;
            target.donGia = donGia;
            target.soLuong = soLuong;
            target.thanhTien = donGia * soLuong;
            target.thoiGianSuDung = txtThoiGianSuDung.getText().trim();
            target.nguoiGhiNhan = txtNguoiGhiNhan.getText().trim();
            target.ghiChu = txtGhiChuMoi.getText().trim();

            if (editing) {
                refreshServiceUsage(target, "Cập nhật dịch vụ thành công.");
                dispose();
            } else {
                addServiceUsage(target, keepOpen);
                if (keepOpen) {
                    txtSoLuong.setText("1");
                    txtThanhTien.setText(txtDonGiaMoi.getText().trim());
                    txtGhiChuMoi.setText("");
                } else {
                    dispose();
                }
            }
        }
    }

    private final class CloseServiceDialog extends BaseServiceDialog {
        private final ServiceUsageRecord record;

        private CloseServiceDialog(Frame owner, ServiceUsageRecord record) {
            super(owner, "Chốt dịch vụ", 560, 360);
            this.record = record;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHỐT DỊCH VỤ", "Sau khi chốt, dịch vụ của hồ sơ này sẽ bị khóa chỉnh sửa và được chuyển vào tính hóa đơn."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextArea txtGhiChuMoi = createDialogTextArea(3);
            addFormRow(form, gbc, 0, "Hồ sơ lưu trú", createValueLabel(record.hoSoLuuTru));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(record.khachPhong));
            addFormRow(form, gbc, 2, "Số dòng dịch vụ", createValueLabel(String.valueOf(countByHoSo(record.hoSoLuuTru))));
            addFormRow(form, gbc, 3, "Tổng tiền dịch vụ", createValueLabel(formatMoney(sumByHoSo(record.hoSoLuuTru))));
            addFormRow(form, gbc, 4, "Ghi chú", new JScrollPane(txtGhiChuMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận chốt", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit(txtGhiChuMoi));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextArea txtGhiChuMoi) {
            if (!showConfirmDialog(
                    "Xác nhận chốt dịch vụ",
                    "Dịch vụ của hồ sơ này sẽ bị khóa chỉnh sửa. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(245, 158, 11)
            )) {
                return;
            }
            for (ServiceUsageRecord service : allServiceUsages) {
                if (service.hoSoLuuTru.equals(record.hoSoLuuTru)) {
                    service.trangThai = "Đã chốt";
                    if (!txtGhiChuMoi.getText().trim().isEmpty()) {
                        service.ghiChu = txtGhiChuMoi.getText().trim();
                    }
                }
            }
            refreshServiceUsage(record, "Chốt dịch vụ thành công.");
            dispose();
        }
    }

    private final class ServiceCatalogDialog extends BaseServiceDialog {
        private final ServiceCatalogRecord editingRecord;
        private final boolean editing;

        private ServiceCatalogDialog(Frame owner, ServiceCatalogRecord record) {
            super(owner, record == null ? "Thêm dịch vụ danh mục" : "Cập nhật dịch vụ danh mục", 620, 460);
            this.editingRecord = record;
            this.editing = record != null;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    editing ? "CẬP NHẬT DỊCH VỤ DANH MỤC" : "THÊM DỊCH VỤ DANH MỤC",
                    "Danh mục dịch vụ là lớp dữ liệu nền của hệ thống, dùng để chọn khi ghi nhận dịch vụ phát sinh."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtMa = createInputField(editing ? editingRecord.maDichVu : "");
            txtMa.setEditable(!editing);
            JTextField txtTen = createInputField(editing ? editingRecord.tenDichVu : "");
            JComboBox<String> cboNhom = createComboBox(SERVICE_GROUP_OPTIONS);
            JTextField txtDonGia = createInputField(editing ? formatMoney(editingRecord.donGiaMacDinh) : "0");
            JTextField txtDonVi = createInputField(editing ? editingRecord.donViTinh : "");
            JComboBox<String> cboStatus = createComboBox(new String[]{"Đang áp dụng", "Ngừng áp dụng"});
            JTextArea txtMoTaMoi = createDialogTextArea(3);

            if (editing) {
                cboNhom.setSelectedItem(editingRecord.nhomDichVu);
                cboStatus.setSelectedItem(editingRecord.trangThai);
                txtMoTaMoi.setText(editingRecord.moTa);
            }

            addFormRow(form, gbc, 0, "Mã dịch vụ", txtMa);
            addFormRow(form, gbc, 1, "Tên dịch vụ", txtTen);
            addFormRow(form, gbc, 2, "Nhóm dịch vụ", cboNhom);
            addFormRow(form, gbc, 3, "Đơn giá mặc định", txtDonGia);
            addFormRow(form, gbc, 4, "Đơn vị tính", txtDonVi);
            addFormRow(form, gbc, 5, "Trạng thái đầu", cboStatus);
            addFormRow(form, gbc, 6, "Mô tả", new JScrollPane(txtMoTaMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton(editing ? "Lưu cập nhật" : "Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false, txtMa, txtTen, cboNhom, txtDonGia, txtDonVi, cboStatus, txtMoTaMoi));
            JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true, txtMa, txtTen, cboNhom, txtDonGia, txtDonVi, cboStatus, txtMoTaMoi));
            btnSaveNew.setVisible(!editing);
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSaveNew, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean keepOpen, JTextField txtMa, JTextField txtTen, JComboBox<String> cboNhom, JTextField txtDonGia, JTextField txtDonVi, JComboBox<String> cboStatus, JTextArea txtMoTaMoi) {
            if (txtMa.getText().trim().isEmpty()) {
                showError("Mã dịch vụ không được để trống.");
                return;
            }
            if (!editing) {
                for (ServiceCatalogRecord catalog : serviceCatalogs) {
                    if (catalog.maDichVu.equalsIgnoreCase(txtMa.getText().trim())) {
                        showError("Mã dịch vụ không được trùng.");
                        return;
                    }
                }
            }
            if (txtTen.getText().trim().isEmpty()) {
                showError("Tên dịch vụ không được để trống.");
                return;
            }
            double donGia = parseMoney(txtDonGia.getText().trim());
            if (donGia < 0 || valueOf(cboNhom.getSelectedItem()).isEmpty()) {
                showError("Đơn giá mặc định >= 0 và nhóm dịch vụ bắt buộc chọn.");
                return;
            }

            ServiceCatalogRecord target = editing
                    ? editingRecord
                    : new ServiceCatalogRecord(txtMa.getText().trim(), txtTen.getText().trim(), valueOf(cboNhom.getSelectedItem()), donGia, txtDonVi.getText().trim(), valueOf(cboStatus.getSelectedItem()), txtMoTaMoi.getText().trim());

            target.tenDichVu = txtTen.getText().trim();
            target.nhomDichVu = valueOf(cboNhom.getSelectedItem());
            target.donGiaMacDinh = donGia;
            target.donViTinh = txtDonVi.getText().trim();
            target.trangThai = valueOf(cboStatus.getSelectedItem());
            target.moTa = txtMoTaMoi.getText().trim();

            if (editing) {
                showSuccess("Cập nhật dịch vụ danh mục thành công.");
                dispose();
            } else {
                addCatalog(target, keepOpen);
                if (keepOpen) {
                    txtMa.setText("");
                    txtTen.setText("");
                    txtDonGia.setText("0");
                    txtDonVi.setText("");
                    txtMoTaMoi.setText("");
                } else {
                    dispose();
                }
            }
        }
    }

    private final class ServiceUsageDetailDialog extends BaseServiceDialog {
        private ServiceUsageDetailDialog(Frame owner, ServiceUsageRecord record) {
            super(owner, "Chi tiết dịch vụ phát sinh", 620, 420);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHI TIẾT DỊCH VỤ PHÁT SINH", "Thông tin chi tiết của một dòng dịch vụ thực tế đã ghi nhận."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Hồ sơ lưu trú", createValueLabel(record.hoSoLuuTru));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(record.khachPhong));
            addFormRow(form, gbc, 2, "Nhóm dịch vụ", createValueLabel(record.nhomDichVu));
            addFormRow(form, gbc, 3, "Dịch vụ", createValueLabel(record.tenDichVu));
            addFormRow(form, gbc, 4, "Đơn giá", createValueLabel(formatMoney(record.donGia)));
            addFormRow(form, gbc, 5, "Số lượng", createValueLabel(String.valueOf(record.soLuong)));
            addFormRow(form, gbc, 6, "Thành tiền", createValueLabel(record.getThanhTienLabel()));
            addFormRow(form, gbc, 7, "Thời gian sử dụng", createValueLabel(record.thoiGianSuDung));
            addFormRow(form, gbc, 8, "Người ghi nhận", createValueLabel(record.nguoiGhiNhan));
            addFormRow(form, gbc, 9, "Ghi chú", new JScrollPane(createReadonlyViewArea(record.ghiChu)));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JTextArea createReadonlyViewArea(String text) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(text);
        return area;
    }

    private int countByHoSo(String hoSo) {
        int count = 0;
        for (ServiceUsageRecord service : allServiceUsages) {
            if (service.hoSoLuuTru.equals(hoSo)) {
                count++;
            }
        }
        return count;
    }

    private double sumByHoSo(String hoSo) {
        double total = 0;
        for (ServiceUsageRecord service : allServiceUsages) {
            if (service.hoSoLuuTru.equals(hoSo)) {
                total += service.thanhTien;
            }
        }
        return total;
    }

    private final class ConfirmDialog extends BaseServiceDialog {
        private boolean confirmed;

        private ConfirmDialog(Frame owner, String title, String message, String confirmText, Color confirmColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            JButton btnSkip = createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose());
            JButton btnConfirm = createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> {
                confirmed = true;
                dispose();
            });
            content.add(buildDialogButtons(btnSkip, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() {
            return confirmed;
        }
    }

    private final class AppMessageDialog extends BaseServiceDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class ServiceUsageRecord {
        private String maPhatSinh;
        private String hoSoLuuTru;
        private String khachPhong;
        private String trangThaiHoSo;
        private String maDanhMuc;
        private String tenDichVu;
        private String nhomDichVu;
        private String donViTinh;
        private double donGia;
        private int soLuong;
        private double thanhTien;
        private String thoiGianSuDung;
        private String nguoiGhiNhan;
        private String trangThai;
        private String ghiChu;

        private static ServiceUsageRecord create(String maPhatSinh, String hoSoLuuTru, String khachPhong, String trangThaiHoSo,
                                                 ServiceCatalogRecord catalog, int soLuong, String thoiGianSuDung, String nguoiGhiNhan,
                                                 String trangThai) {
            ServiceUsageRecord record = new ServiceUsageRecord();
            record.maPhatSinh = maPhatSinh;
            record.hoSoLuuTru = hoSoLuuTru;
            record.khachPhong = khachPhong;
            record.trangThaiHoSo = trangThaiHoSo;
            record.maDanhMuc = catalog.maDichVu;
            record.tenDichVu = catalog.tenDichVu;
            record.nhomDichVu = catalog.nhomDichVu;
            record.donViTinh = catalog.donViTinh;
            record.donGia = catalog.donGiaMacDinh;
            record.soLuong = soLuong;
            record.thanhTien = catalog.donGiaMacDinh * soLuong;
            record.thoiGianSuDung = thoiGianSuDung;
            record.nguoiGhiNhan = nguoiGhiNhan;
            record.trangThai = trangThai;
            record.ghiChu = catalog.moTa;
            return record;
        }

        private String getThanhTienLabel() {
            return String.format(Locale.US, "%,.0f", thanhTien).replace(',', '.');
        }

        private String buildDetailNote() {
            return "Đơn giá: " + String.format(Locale.US, "%,.0f", donGia).replace(',', '.')
                    + " | Số lượng: " + soLuong
                    + " | Thời gian: " + thoiGianSuDung
                    + " | Người ghi nhận: " + nguoiGhiNhan
                    + (ghiChu == null || ghiChu.trim().isEmpty() ? "" : "\n" + ghiChu);
        }
    }

    private static final class ServiceCatalogRecord {
        private String maDichVu;
        private String tenDichVu;
        private String nhomDichVu;
        private double donGiaMacDinh;
        private String donViTinh;
        private String trangThai;
        private String moTa;

        private ServiceCatalogRecord(String maDichVu, String tenDichVu, String nhomDichVu, double donGiaMacDinh,
                                     String donViTinh, String trangThai, String moTa) {
            this.maDichVu = maDichVu;
            this.tenDichVu = tenDichVu;
            this.nhomDichVu = nhomDichVu;
            this.donGiaMacDinh = donGiaMacDinh;
            this.donViTinh = donViTinh;
            this.trangThai = trangThai;
            this.moTa = moTa;
        }
    }

    /**
     * Trả về panel đã build — dùng bởi NavigationUtil để swap vào AppFrame.
     */
    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }

}
