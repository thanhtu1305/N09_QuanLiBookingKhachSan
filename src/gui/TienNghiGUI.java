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
import javax.swing.SwingConstants;
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
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TienNghiGUI extends JFrame {
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
    private static final String[] AMENITY_GROUP_OPTIONS = {"Cơ bản", "Giải trí", "Phòng tắm", "Tiện nghi bổ sung", "An toàn"};

    private final String username;
    private final String role;
    private final List<AmenityRecord> allAmenities = new ArrayList<AmenityRecord>();
    private final List<AmenityRecord> filteredAmenities = new ArrayList<AmenityRecord>();

    private JTable tblTienNghi;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboNhomTienNghi;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblMaTienNghi;
    private JLabel lblTenTienNghi;
    private JLabel lblNhomTienNghi;
    private JLabel lblTrangThaiChiTiet;
    private JLabel lblUuTien;
    private JTextArea txtMoTa;
    private JTextArea txtGhiChu;
    private JLabel lblUsedByRoomTypes;
    private JLabel lblUsedByRooms;

    public TienNghiGUI() {
        this("guest", "Lễ tân");
    }

    public TienNghiGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý tiện nghi - " + AppBranding.APP_DISPLAY_NAME);
        setSize(1360, 820);
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

        root.add(SidebarFactory.createSidebar(this, ScreenKey.TIEN_NGHI, username, role), BorderLayout.WEST);
        root.add(buildMainContent(), BorderLayout.CENTER);

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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ TIỆN NGHI"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý danh mục tiện nghi nền dùng cho cấu hình tiện nghi mặc định của loại phòng.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Tiện nghi"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Thêm tiện nghi", new Color(22, 163, 74), Color.WHITE, e -> openCreateAmenityDialog()));
        card.add(createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openUpdateAmenityDialog()));
        card.add(createPrimaryButton("Ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivateAmenityDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboNhomTienNghi = createComboBox(new String[]{"Tất cả", "Cơ bản", "Giải trí", "Phòng tắm", "Tiện nghi bổ sung", "An toàn"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Đang áp dụng", "Ngừng áp dụng"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(290, 34));
        txtTuKhoa.setToolTipText("Mã tiện nghi / tên tiện nghi");

        left.add(createFieldGroup("Nhóm tiện nghi", cboNhomTienNghi));
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
        JPanel right = buildRightColumn();

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

        JLabel lblTitle = new JLabel("Danh sách tiện nghi nền");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Double click để xem chi tiết tiện nghi.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {"Mã tiện nghi", "Tên tiện nghi", "Nhóm", "Ưu tiên", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTienNghi = new JTable(tableModel);
        tblTienNghi.setFont(BODY_FONT);
        tblTienNghi.setRowHeight(32);
        tblTienNghi.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTienNghi.setGridColor(BORDER_SOFT);
        tblTienNghi.setShowGrid(true);
        tblTienNghi.setFillsViewportHeight(true);
        tblTienNghi.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTienNghi.getTableHeader().setBackground(new Color(243, 244, 246));
        tblTienNghi.getTableHeader().setForeground(TEXT_PRIMARY);

        tblTienNghi.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblTienNghi.getSelectedRow();
                if (row >= 0 && row < filteredAmenities.size()) {
                    updateDetailPanel(filteredAmenities.get(row));
                }
            }
        });
        tblTienNghi.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblTienNghi.getSelectedRow() >= 0) {
                    openAmenityDetailDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblTienNghi);
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

    private JPanel buildRightColumn() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(buildDetailCard(), BorderLayout.CENTER);
        wrapper.add(buildUsageCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết tiện nghi");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(5, 2, 10, 8));
        body.setOpaque(false);

        lblMaTienNghi = createValueLabel();
        lblTenTienNghi = createValueLabel();
        lblNhomTienNghi = createValueLabel();
        lblTrangThaiChiTiet = createValueLabel();
        lblUuTien = createValueLabel();

        addDetailRow(body, "Mã tiện nghi", lblMaTienNghi);
        addDetailRow(body, "Tên tiện nghi", lblTenTienNghi);
        addDetailRow(body, "Nhóm tiện nghi", lblNhomTienNghi);
        addDetailRow(body, "Trạng thái", lblTrangThaiChiTiet);
        addDetailRow(body, "Ưu tiên hiển thị", lblUuTien);

        JPanel notes = new JPanel(new GridLayout(2, 1, 0, 10));
        notes.setOpaque(false);

        txtMoTa = createReadonlyArea(4);
        txtGhiChu = createReadonlyArea(3);
        notes.add(createReadOnlyBlock("Mô tả", txtMoTa));
        notes.add(createReadOnlyBlock("Ghi chú", txtGhiChu));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(notes, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildUsageCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Liên kết sử dụng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblHint = new JLabel("<html>Danh mục tiện nghi nền được dùng ở cấu hình <b>Tiện nghi mặc định</b> của Loại phòng. Tại trang Phòng, tiện nghi chỉ được hiển thị theo loại phòng.</html>");
        lblHint.setFont(BODY_FONT);
        lblHint.setForeground(TEXT_MUTED);

        JPanel body = new JPanel(new GridLayout(2, 2, 10, 8));
        body.setOpaque(false);
        lblUsedByRoomTypes = createValueLabel();
        lblUsedByRooms = createValueLabel();
        addDetailRow(body, "Loại phòng", lblUsedByRoomTypes);
        addDetailRow(body, "Phòng", lblUsedByRooms);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblHint, BorderLayout.CENTER);
        card.add(body, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createReadOnlyBlock(String label, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(TEXT_MUTED);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createReadonlyArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(BODY_FONT);
        area.setForeground(TEXT_PRIMARY);
        area.setBackground(PANEL_SOFT);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
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
        allAmenities.clear();
        allAmenities.add(AmenityRecord.create("TN001", "WiFi", "Cơ bản", "Đang áp dụng", 1, "Internet tốc độ cao phủ toàn bộ phòng.", "Có mặt ở hầu hết các phòng.", "Deluxe, Superior, Standard", "P101, P203, P502"));
        allAmenities.add(AmenityRecord.create("TN002", "TV", "Giải trí", "Đang áp dụng", 2, "TV màn hình phẳng với truyền hình cáp.", "Dùng làm tiện nghi mặc định phổ biến.", "Deluxe, Superior", "P101, P202"));
        allAmenities.add(AmenityRecord.create("TN003", "Điều hòa", "Cơ bản", "Đang áp dụng", 3, "Điều hòa inverter hai chiều.", "Bắt buộc với hầu hết cấu hình mới.", "Deluxe, Superior, Standard", "P101, P103, P202"));
        allAmenities.add(AmenityRecord.create("TN004", "Bồn tắm", "Phòng tắm", "Đang áp dụng", 6, "Bồn tắm nằm dành cho phòng hạng sang.", "Chỉ áp dụng cho hạng phòng cao cấp.", "Suite, Deluxe", "P502"));
        allAmenities.add(AmenityRecord.create("TN005", "Máy sấy tóc", "Tiện nghi bổ sung", "Ngừng áp dụng", 7, "Máy sấy tóc công suất 1200W.", "Lịch sử cũ vẫn được giữ nguyên.", "Suite", "P404"));
        allAmenities.add(AmenityRecord.create("TN006", "Khóa từ", "An toàn", "Đang áp dụng", 4, "Khóa từ tích hợp kiểm soát ra vào.", "Ưu tiên cao trong cấu hình mới.", "Deluxe, Superior", "P101, P203"));
    }

    private void reloadSampleData(boolean showMessage) {
        cboNhomTienNghi.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu tiện nghi.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredAmenities.clear();

        String nhomTienNghi = valueOf(cboNhomTienNghi.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (AmenityRecord amenity : allAmenities) {
            if (!"Tất cả".equals(nhomTienNghi) && !amenity.nhomTienNghi.equals(nhomTienNghi)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !amenity.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (amenity.maTienNghi + " " + amenity.tenTienNghi + " " + amenity.nhomTienNghi).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredAmenities.add(amenity);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredAmenities.size() + " tiện nghi phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (AmenityRecord amenity : filteredAmenities) {
            tableModel.addRow(new Object[]{
                    amenity.maTienNghi,
                    amenity.tenTienNghi,
                    amenity.nhomTienNghi,
                    amenity.uuTienHienThi,
                    amenity.trangThai
            });
        }

        if (!filteredAmenities.isEmpty()) {
            tblTienNghi.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredAmenities.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(AmenityRecord amenity) {
        lblMaTienNghi.setText(amenity.maTienNghi);
        lblTenTienNghi.setText(amenity.tenTienNghi);
        lblNhomTienNghi.setText(amenity.nhomTienNghi);
        lblTrangThaiChiTiet.setText(amenity.trangThai);
        lblUuTien.setText(String.valueOf(amenity.uuTienHienThi));
        txtMoTa.setText(amenity.moTa);
        txtGhiChu.setText(amenity.ghiChu);
        lblUsedByRoomTypes.setText(safeValue(amenity.suDungChoLoaiPhong, "Chưa có"));
        lblUsedByRooms.setText(safeValue(amenity.suDungChoPhong, "Chưa có"));
        txtMoTa.setCaretPosition(0);
        txtGhiChu.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblMaTienNghi.setText("-");
        lblTenTienNghi.setText("-");
        lblNhomTienNghi.setText("-");
        lblTrangThaiChiTiet.setText("-");
        lblUuTien.setText("-");
        txtMoTa.setText("Không có dữ liệu phù hợp.");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
        lblUsedByRoomTypes.setText("-");
        lblUsedByRooms.setText("-");
    }

    private AmenityRecord getSelectedAmenity() {
        int row = tblTienNghi.getSelectedRow();
        if (row < 0 || row >= filteredAmenities.size()) {
            showWarning("Vui lòng chọn một tiện nghi trong danh sách.");
            return null;
        }
        return filteredAmenities.get(row);
    }

    private void openCreateAmenityDialog() {
        new AmenityEditorDialog(this, null).setVisible(true);
    }

    private void openUpdateAmenityDialog() {
        AmenityRecord amenity = getSelectedAmenity();
        if (amenity != null) {
            new AmenityEditorDialog(this, amenity).setVisible(true);
        }
    }

    private void openDeactivateAmenityDialog() {
        AmenityRecord amenity = getSelectedAmenity();
        if (amenity != null) {
            new DeactivateAmenityDialog(this, amenity).setVisible(true);
        }
    }

    private void openAmenityDetailDialog() {
        AmenityRecord amenity = getSelectedAmenity();
        if (amenity != null) {
            new AmenityDetailDialog(this, amenity).setVisible(true);
        }
    }

    private void refreshAmenityViews(AmenityRecord amenity, String message) {
        applyFilters(false);
        selectAmenity(amenity);
        showSuccess(message);
    }

    private void selectAmenity(AmenityRecord amenity) {
        if (amenity == null) {
            return;
        }
        int index = filteredAmenities.indexOf(amenity);
        if (index >= 0) {
            tblTienNghi.setRowSelectionInterval(index, index);
            updateDetailPanel(amenity);
        } else if (!filteredAmenities.isEmpty()) {
            tblTienNghi.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredAmenities.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Thêm tiện nghi",
                "F2 Cập nhật",
                "F3 Ngừng áp dụng",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "tiennghi-f1", this::openCreateAmenityDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "tiennghi-f2", this::openUpdateAmenityDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "tiennghi-f3", this::openDeactivateAmenityDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "tiennghi-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "tiennghi-enter", this::openAmenityDetailDialog);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private int parsePositiveInteger(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
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

    private abstract class BaseAmenityDialog extends JDialog {
        protected BaseAmenityDialog(Frame owner, String title, int width, int height) {
            super(owner, title, true);
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setSize(width, height);
            setLocationRelativeTo(owner);
            getContentPane().setBackground(APP_BG);
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
            setLayout(new BorderLayout());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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

    private final class AmenityEditorDialog extends BaseAmenityDialog {
        private final AmenityRecord amenity;

        private JTextField txtMaTienNghiDialog;
        private JTextField txtTenTienNghiDialog;
        private JComboBox<String> cboNhomTienNghiDialog;
        private JComboBox<String> cboTrangThaiDialog;
        private JTextField txtUuTien;
        private JTextArea txtMoTaDialog;
        private JTextArea txtGhiChuDialog;

        private AmenityEditorDialog(Frame owner, AmenityRecord amenity) {
            super(owner, amenity == null ? "Thêm tiện nghi" : "Cập nhật tiện nghi", 640, 560);
            this.amenity = amenity;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    amenity == null ? "THÊM TIỆN NGHI" : "CẬP NHẬT TIỆN NGHI",
                    "Quản lý danh mục tiện nghi nền của hệ thống. Dữ liệu này sẽ được dùng ở cấu hình Tiện nghi mặc định của Loại phòng."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMaTienNghiDialog = createInputField(amenity == null ? "" : amenity.maTienNghi);
            txtTenTienNghiDialog = createInputField(amenity == null ? "" : amenity.tenTienNghi);
            cboNhomTienNghiDialog = createComboBox(AMENITY_GROUP_OPTIONS);
            cboTrangThaiDialog = createComboBox(new String[]{"Đang áp dụng", "Ngừng áp dụng"});
            txtUuTien = createInputField(amenity == null ? "1" : String.valueOf(amenity.uuTienHienThi));
            txtMoTaDialog = createDialogTextArea(4);
            txtGhiChuDialog = createDialogTextArea(3);

            if (amenity != null) {
                cboNhomTienNghiDialog.setSelectedItem(amenity.nhomTienNghi);
                cboTrangThaiDialog.setSelectedItem(amenity.trangThai);
                txtMoTaDialog.setText(amenity.moTa);
                txtGhiChuDialog.setText(amenity.ghiChu);
                if (amenity.isInUse()) {
                    txtMaTienNghiDialog.setEditable(false);
                }
            }

            addFormRow(form, gbc, 0, "Mã tiện nghi", txtMaTienNghiDialog);
            addFormRow(form, gbc, 1, "Tên tiện nghi", txtTenTienNghiDialog);
            addFormRow(form, gbc, 2, "Nhóm tiện nghi", cboNhomTienNghiDialog);
            addFormRow(form, gbc, 3, "Trạng thái", cboTrangThaiDialog);
            addFormRow(form, gbc, 4, "Mức ưu tiên hiển thị", txtUuTien);
            addFormRow(form, gbc, 5, "Mô tả", new JScrollPane(txtMoTaDialog));
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            JButton btnSave = createPrimaryButton(amenity == null ? "Lưu" : "Lưu cập nhật", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            if (amenity == null) {
                JButton btnSaveAndNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true));
                content.add(buildDialogButtons(btnCancel, btnSaveAndNew, btnSave), BorderLayout.SOUTH);
            } else {
                content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
            }
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean createAnother) {
            String maTienNghi = txtMaTienNghiDialog.getText().trim();
            String tenTienNghi = txtTenTienNghiDialog.getText().trim();
            String nhomTienNghi = valueOf(cboNhomTienNghiDialog.getSelectedItem());
            int uuTien = parsePositiveInteger(txtUuTien.getText().trim(), 1);

            if (maTienNghi.isEmpty()) {
                showError("Mã tiện nghi không được rỗng.");
                return;
            }
            if (tenTienNghi.isEmpty()) {
                showError("Tên tiện nghi bắt buộc nhập.");
                return;
            }
            if (nhomTienNghi.isEmpty()) {
                showError("Nhóm tiện nghi bắt buộc chọn.");
                return;
            }
            if (uuTien < 0) {
                showError("Mức ưu tiên hiển thị phải là giá trị hợp lệ.");
                return;
            }
            for (AmenityRecord existing : allAmenities) {
                if (existing != amenity && existing.maTienNghi.equalsIgnoreCase(maTienNghi)) {
                    showError("Mã tiện nghi không được trùng.");
                    return;
                }
            }

            if (amenity == null) {
                AmenityRecord newAmenity = AmenityRecord.create(
                        maTienNghi,
                        tenTienNghi,
                        nhomTienNghi,
                        valueOf(cboTrangThaiDialog.getSelectedItem()),
                        uuTien,
                        txtMoTaDialog.getText().trim(),
                        txtGhiChuDialog.getText().trim(),
                        "Chưa cấu hình",
                        "Chưa cấu hình"
                );
                allAmenities.add(0, newAmenity);
                applyFilters(false);
                selectAmenity(newAmenity);
                showSuccess("Thêm tiện nghi thành công.");
                if (createAnother) {
                    txtMaTienNghiDialog.setText("");
                    txtTenTienNghiDialog.setText("");
                    txtUuTien.setText("1");
                    txtMoTaDialog.setText("");
                    txtGhiChuDialog.setText("");
                    cboNhomTienNghiDialog.setSelectedIndex(0);
                    cboTrangThaiDialog.setSelectedItem("Đang áp dụng");
                    txtMaTienNghiDialog.requestFocusInWindow();
                } else {
                    dispose();
                }
                return;
            }

            amenity.tenTienNghi = tenTienNghi;
            amenity.nhomTienNghi = nhomTienNghi;
            amenity.trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            amenity.uuTienHienThi = uuTien;
            amenity.moTa = txtMoTaDialog.getText().trim();
            amenity.ghiChu = txtGhiChuDialog.getText().trim();
            refreshAmenityViews(amenity, "Cập nhật tiện nghi thành công.");
            dispose();
        }
    }

    private final class DeactivateAmenityDialog extends BaseAmenityDialog {
        private final AmenityRecord amenity;
        private JTextField txtTuNgay;
        private JTextArea txtLyDo;
        private JTextArea txtGhiChu;

        private DeactivateAmenityDialog(Frame owner, AmenityRecord amenity) {
            super(owner, "Ngừng áp dụng tiện nghi", 600, 470);
            this.amenity = amenity;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "NGỪNG ÁP DỤNG TIỆN NGHI",
                    "Tiện nghi này sẽ không còn hiển thị trong danh sách cấu hình mới. Dữ liệu cũ đã được gán trước đó vẫn được giữ nguyên."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTuNgay = createInputField("19/03/2026");
            txtLyDo = createDialogTextArea(3);
            txtGhiChu = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã tiện nghi", createValueLabel(amenity.maTienNghi));
            addFormRow(form, gbc, 1, "Tên tiện nghi", createValueLabel(amenity.tenTienNghi));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueLabel(amenity.trangThai));
            addFormRow(form, gbc, 3, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 4, "Lý do", new JScrollPane(txtLyDo));
            addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtGhiChu));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xác nhận ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (txtTuNgay.getText().trim().isEmpty()) {
                showError("Từ ngày là trường bắt buộc.");
                return;
            }
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do ngừng áp dụng.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận ngừng áp dụng tiện nghi",
                    "Tiện nghi này sẽ không còn được dùng cho cấu hình mới. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(245, 158, 11)
            )) {
                return;
            }

            amenity.trangThai = "Ngừng áp dụng";
            amenity.ghiChu = txtGhiChu.getText().trim().isEmpty() ? txtLyDo.getText().trim() : txtGhiChu.getText().trim();
            refreshAmenityViews(amenity, "Ngừng áp dụng tiện nghi thành công.");
            dispose();
        }
    }

    private final class AmenityDetailDialog extends BaseAmenityDialog {
        private AmenityDetailDialog(Frame owner, AmenityRecord amenity) {
            super(owner, "Chi tiết tiện nghi", 640, 520);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHI TIẾT TIỆN NGHI",
                    "Thông tin danh mục tiện nghi nền ở chế độ chỉ đọc."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã tiện nghi", createValueLabel(amenity.maTienNghi));
            addFormRow(form, gbc, 1, "Tên tiện nghi", createValueLabel(amenity.tenTienNghi));
            addFormRow(form, gbc, 2, "Nhóm tiện nghi", createValueLabel(amenity.nhomTienNghi));
            addFormRow(form, gbc, 3, "Trạng thái", createValueLabel(amenity.trangThai));
            addFormRow(form, gbc, 4, "Mức ưu tiên hiển thị", createValueLabel(String.valueOf(amenity.uuTienHienThi)));
            addFormRow(form, gbc, 5, "Mô tả", new JScrollPane(readOnlyDialogArea(amenity.moTa)));
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(readOnlyDialogArea(amenity.ghiChu)));
            addFormRow(form, gbc, 7, "Loại phòng", createValueLabel(safeValue(amenity.suDungChoLoaiPhong, "Chưa có")));
            addFormRow(form, gbc, 8, "Phòng", createValueLabel(safeValue(amenity.suDungChoPhong, "Chưa có")));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JTextArea readOnlyDialogArea(String value) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(value);
        return area;
    }

    private final class ConfirmDialog extends BaseAmenityDialog {
        private boolean confirmed;

        private ConfirmDialog(Frame owner, String title, String message, String confirmText, Color confirmColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> {
                        confirmed = true;
                        dispose();
                    })
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() {
            return confirmed;
        }
    }

    private final class AppMessageDialog extends BaseAmenityDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class AmenityRecord {
        private String maTienNghi;
        private String tenTienNghi;
        private String nhomTienNghi;
        private String trangThai;
        private int uuTienHienThi;
        private String moTa;
        private String ghiChu;
        private String suDungChoLoaiPhong;
        private String suDungChoPhong;

        private static AmenityRecord create(String maTienNghi, String tenTienNghi, String nhomTienNghi, String trangThai,
                                            int uuTienHienThi, String moTa, String ghiChu,
                                            String suDungChoLoaiPhong, String suDungChoPhong) {
            AmenityRecord record = new AmenityRecord();
            record.maTienNghi = maTienNghi;
            record.tenTienNghi = tenTienNghi;
            record.nhomTienNghi = nhomTienNghi;
            record.trangThai = trangThai;
            record.uuTienHienThi = uuTienHienThi;
            record.moTa = moTa;
            record.ghiChu = ghiChu;
            record.suDungChoLoaiPhong = suDungChoLoaiPhong;
            record.suDungChoPhong = suDungChoPhong;
            return record;
        }

        private boolean isInUse() {
            return (suDungChoLoaiPhong != null && !suDungChoLoaiPhong.trim().isEmpty() && !"Chưa cấu hình".equalsIgnoreCase(suDungChoLoaiPhong.trim()))
                    || (suDungChoPhong != null && !suDungChoPhong.trim().isEmpty() && !"Chưa cấu hình".equalsIgnoreCase(suDungChoPhong.trim()));
        }
    }
}
