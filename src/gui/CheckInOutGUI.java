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
import javax.swing.JOptionPane;
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

public class CheckInOutGUI extends JFrame {
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

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<StayRecord> allRecords = new ArrayList<StayRecord>();
    private final List<StayRecord> filteredRecords = new ArrayList<StayRecord>();

    private JTable tblLuuTru;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboTang;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboCaLam;
    private JTextField txtTuKhoa;

    private JLabel lblMaHoSo;
    private JLabel lblMaDatPhong;
    private JLabel lblKhachHang;
    private JLabel lblSoPhong;
    private JLabel lblLoaiPhongChiTiet;
    private JLabel lblTrangThaiPhong;
    private JLabel lblTienCoc;
    private JLabel lblDichVuPhatSinh;
    private JTextArea txtGhiChu;

    public CheckInOutGUI() {
        this("guest", "Lễ tân");
    }

    public CheckInOutGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý Check-in / Check-out - Hotel PMS");
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

        root.add(SidebarFactory.createSidebar(this, ScreenKey.CHECK_IN_OUT, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ CHECK-IN / CHECK-OUT"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi lưu trú hiện tại, xử lý check-in/check-out và trạng thái phòng thời gian thực.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Check-in / Check-out"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Check-in", new Color(22, 163, 74), Color.WHITE, e -> openSimpleDialog("Check-in", new String[]{"Mã đặt phòng", "Số phòng", "Giờ vào", "Ghi chú"})));
        card.add(createPrimaryButton("Thêm dịch vụ", new Color(37, 99, 235), Color.WHITE, e -> openSimpleDialog("Thêm dịch vụ", new String[]{"Mã hồ sơ", "Dịch vụ", "Số lượng", "Ghi chú"})));
        card.add(createPrimaryButton("Đổi phòng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openSimpleDialog("Đổi phòng", new String[]{"Mã hồ sơ", "Phòng hiện tại", "Phòng mới", "Lý do"})));
        card.add(createPrimaryButton("Gia hạn", new Color(59, 130, 246), Color.WHITE, e -> openSimpleDialog("Gia hạn lưu trú", new String[]{"Mã hồ sơ", "Ngày trả cũ", "Ngày trả mới", "Ghi chú"})));
        card.add(createPrimaryButton("Check-out", new Color(220, 38, 38), Color.WHITE, e -> openSimpleDialog("Check-out", new String[]{"Mã hồ sơ", "Số phòng", "Giờ ra", "Ghi chú"})));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Chờ check-in", "Đang ở", "Chờ check-out", "Đã check-out"});
        cboTang = createComboBox(new String[]{"Tất cả", "Tầng 1", "Tầng 2", "Tầng 3", "Tầng 5"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite"});
        cboCaLam = createComboBox(new String[]{"Tất cả", "Ca sáng", "Ca chiều", "Ca tối"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(280, 34));
        txtTuKhoa.setToolTipText("Mã đặt phòng / số phòng / tên khách");

        left.add(createFieldGroup("Trạng thái", cboTrangThai));
        left.add(createFieldGroup("Tầng", cboTang));
        left.add(createFieldGroup("Loại phòng", cboLoaiPhong));
        left.add(createFieldGroup("Ca làm", cboCaLam));

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
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Check-in",
                "F2 Thêm dịch vụ",
                "F3 Đổi phòng",
                "F4 Gia hạn",
                "F5 Check-out",
                "Enter Xem chi tiết"
        );
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách check-in / check-out");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Bấm một dòng để xem chi tiết lưu trú.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "Mã hồ sơ",
                "Khách hàng",
                "Phòng",
                "Giờ vào",
                "Giờ ra dự kiến",
                "Trạng thái"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLuuTru = new JTable(tableModel);
        tblLuuTru.setFont(BODY_FONT);
        tblLuuTru.setRowHeight(32);
        tblLuuTru.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLuuTru.setGridColor(BORDER_SOFT);
        tblLuuTru.setShowGrid(true);
        tblLuuTru.setFillsViewportHeight(true);
        tblLuuTru.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblLuuTru.getTableHeader().setBackground(new Color(243, 244, 246));
        tblLuuTru.getTableHeader().setForeground(TEXT_PRIMARY);

        tblLuuTru.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblLuuTru.getSelectedRow();
                if (row >= 0 && row < filteredRecords.size()) {
                    updateDetailPanel(filteredRecords.get(row));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblLuuTru);
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
        wrapper.add(buildRealtimeCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết lưu trú");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblMaHoSo = createValueLabel();
        lblMaDatPhong = createValueLabel();
        lblKhachHang = createValueLabel();
        lblSoPhong = createValueLabel();
        lblLoaiPhongChiTiet = createValueLabel();
        lblTrangThaiPhong = createValueLabel();
        lblTienCoc = createValueLabel();
        lblDichVuPhatSinh = createValueLabel();

        addDetailRow(body, "Mã hồ sơ", lblMaHoSo);
        addDetailRow(body, "Mã đặt phòng", lblMaDatPhong);
        addDetailRow(body, "Khách hàng", lblKhachHang);
        addDetailRow(body, "Số phòng", lblSoPhong);
        addDetailRow(body, "Loại phòng", lblLoaiPhongChiTiet);
        addDetailRow(body, "Trạng thái phòng", lblTrangThaiPhong);
        addDetailRow(body, "Tiền cọc", lblTienCoc);
        addDetailRow(body, "Dịch vụ phát sinh", lblDichVuPhatSinh);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);

        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);

        txtGhiChu = new JTextArea(4, 20);
        txtGhiChu.setEditable(false);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(BODY_FONT);
        txtGhiChu.setForeground(TEXT_PRIMARY);
        txtGhiChu.setBackground(PANEL_SOFT);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        notePanel.add(lblNote, BorderLayout.NORTH);
        notePanel.add(new JScrollPane(txtGhiChu), BorderLayout.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(notePanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRealtimeCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Sơ đồ phòng thời gian thực");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel map = new JPanel();
        map.setOpaque(false);
        map.setLayout(new BoxLayout(map, BoxLayout.Y_AXIS));
        map.add(buildRoomRow("Tầng 1", new String[]{"101:T", "102:D", "103:O", "104:C"}));
        map.add(Box.createVerticalStrut(8));
        map.add(buildRoomRow("Tầng 2", new String[]{"201:O", "202:O", "203:T", "204:D"}));
        map.add(Box.createVerticalStrut(8));
        map.add(buildRoomRow("Tầng 3", new String[]{"301:T", "302:C", "303:O", "304:T"}));
        map.add(Box.createVerticalStrut(8));
        map.add(buildRoomRow("Tầng 5", new String[]{"501:B", "502:O", "503:D"}));
        map.add(Box.createVerticalStrut(10));
        map.add(buildLegendPanel());

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(map, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomRow(String floorName, String[] rooms) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName + ":");
        lblFloor.setPreferredSize(new Dimension(56, 32));
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFloor.setForeground(TEXT_PRIMARY);

        JPanel roomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomPanel.setOpaque(false);
        for (String room : rooms) {
            String[] pair = room.split(":");
            roomPanel.add(createRoomBadge(pair[0], pair[1]));
        }

        row.add(lblFloor, BorderLayout.WEST);
        row.add(roomPanel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createRoomBadge(String roomCode, String status) {
        JPanel badge = new JPanel(new BorderLayout());
        badge.setPreferredSize(new Dimension(82, 40));
        badge.setBackground(resolveStatusColor(status));
        badge.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true));

        JLabel lbl = new JLabel("<html><center>" + roomCode + "<br>" + resolveStatusCode(status) + "</center></html>", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_PRIMARY);

        badge.add(lbl, BorderLayout.CENTER);
        return badge;
    }

    private JPanel buildLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem("Trống", resolveStatusColor("T")));
        legend.add(createLegendItem("Đã đặt", resolveStatusColor("D")));
        legend.add(createLegendItem("Đang ở", resolveStatusColor("O")));
        legend.add(createLegendItem("Dọn dẹp", resolveStatusColor("C")));
        legend.add(createLegendItem("Bảo trì", resolveStatusColor("B")));
        return legend;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);

        JPanel swatch = new JPanel();
        swatch.setBackground(color);
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_PRIMARY);

        item.add(swatch);
        item.add(lbl);
        return item;
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
        comboBox.setPreferredSize(new Dimension(150, 34));
        comboBox.setMaximumSize(new Dimension(160, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(150, 34));
        field.setMaximumSize(new Dimension(280, 34));
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
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(150, 20));

        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setVerticalAlignment(SwingConstants.TOP);
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
        allRecords.clear();
    }

    private void reloadSampleData(boolean showMessage) {
        cboTrangThai.setSelectedIndex(0);
        cboTang.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        cboCaLam.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showInfo("Đã làm mới dữ liệu check-in / check-out.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredRecords.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tang = valueOf(cboTang.getSelectedItem());
        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String caLam = valueOf(cboCaLam.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (StayRecord record : allRecords) {
            if (!"Tất cả".equals(trangThai) && !record.trangThai.equals(trangThai)) {
                continue;
            }
            if (!"Tất cả".equals(tang) && !record.tang.equals(tang)) {
                continue;
            }
            if (!"Tất cả".equals(loaiPhong) && !record.loaiPhong.equals(loaiPhong)) {
                continue;
            }
            if (!"Tất cả".equals(caLam) && !record.caLam.equals(caLam)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (record.maDatPhong + " " + record.soPhong + " " + record.khachHang).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredRecords.add(record);
        }

        refillTable();
        if (showMessage) {
            showInfo("Đã lọc được " + filteredRecords.size() + " hồ sơ lưu trú phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (StayRecord record : filteredRecords) {
            tableModel.addRow(new Object[]{
                    record.maHoSo,
                    record.khachHang,
                    record.soPhong,
                    record.gioVao,
                    record.gioRaDuKien,
                    record.trangThai
            });
        }

        if (!filteredRecords.isEmpty()) {
            tblLuuTru.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredRecords.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(StayRecord record) {
        lblMaHoSo.setText(record.maHoSo);
        lblMaDatPhong.setText(record.maDatPhong);
        lblKhachHang.setText(record.khachHang);
        lblSoPhong.setText(record.soPhong);
        lblLoaiPhongChiTiet.setText(record.loaiPhong);
        lblTrangThaiPhong.setText(record.trangThaiPhong);
        lblTienCoc.setText(record.tienCoc);
        lblDichVuPhatSinh.setText(record.dichVuPhatSinh);
        txtGhiChu.setText(record.ghiChu);
        txtGhiChu.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblMaHoSo.setText("-");
        lblMaDatPhong.setText("-");
        lblKhachHang.setText("-");
        lblSoPhong.setText("-");
        lblLoaiPhongChiTiet.setText("-");
        lblTrangThaiPhong.setText("-");
        lblTienCoc.setText("-");
        lblDichVuPhatSinh.setText("-");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
    }

    private Color resolveStatusColor(String code) {
        if ("T".equals(code)) {
            return new Color(220, 252, 231);
        }
        if ("D".equals(code)) {
            return new Color(254, 249, 195);
        }
        if ("O".equals(code)) {
            return new Color(219, 234, 254);
        }
        if ("C".equals(code)) {
            return new Color(255, 237, 213);
        }
        return new Color(254, 226, 226);
    }

    private String resolveStatusCode(String code) {
        if ("T".equals(code)) {
            return "Trống";
        }
        if ("D".equals(code)) {
            return "Đã đặt";
        }
        if ("O".equals(code)) {
            return "Đang ở";
        }
        if ("C".equals(code)) {
            return "Dọn dẹp";
        }
        return "Bảo trì";
    }

    private void openSimpleDialog(String title, String[] fields) {
        Frame dialogOwner = ScreenUIHelper.resolveDialogOwner(this);
        JDialog dialog = new JDialog(dialogOwner, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(CARD_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            JLabel lbl = new JLabel(fields[i] + ":");
            lbl.setFont(BODY_FONT);
            lbl.setForeground(TEXT_PRIMARY);
            form.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JTextField txt = new JTextField();
            txt.setFont(BODY_FONT);
            form.add(txt, gbc);
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(createOutlineButton("Hủy", new Color(220, 38, 38), e -> dialog.dispose()));
        actions.add(createPrimaryButton("Xác nhận", new Color(22, 163, 74), Color.WHITE, e -> {
            JOptionPane.showMessageDialog(dialog, "Đã ghi nhận thao tác: " + title, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        }));

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        ScreenUIHelper.prepareDialog(dialog, dialogOwner, 520, 320);
        dialog.setVisible(true);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "cio-f1", () -> openSimpleDialog("Check-in", new String[]{"Mã đặt phòng", "Số phòng", "Giờ vào", "Ghi chú"}));
        ScreenUIHelper.registerShortcut(this, "F2", "cio-f2", () -> openSimpleDialog("Thêm dịch vụ", new String[]{"Mã hồ sơ", "Dịch vụ", "Số lượng", "Ghi chú"}));
        ScreenUIHelper.registerShortcut(this, "F3", "cio-f3", () -> openSimpleDialog("Đổi phòng", new String[]{"Mã hồ sơ", "Phòng hiện tại", "Phòng mới", "Lý do"}));
        ScreenUIHelper.registerShortcut(this, "F4", "cio-f4", () -> openSimpleDialog("Gia hạn lưu trú", new String[]{"Mã hồ sơ", "Ngày trả cũ", "Ngày trả mới", "Ghi chú"}));
        ScreenUIHelper.registerShortcut(this, "F5", "cio-f5", () -> openSimpleDialog("Check-out", new String[]{"Mã hồ sơ", "Số phòng", "Giờ ra", "Ghi chú"}));
        ScreenUIHelper.registerShortcut(this, "ENTER", "cio-enter", () -> {
            int row = tblLuuTru.getSelectedRow();
            if (row < 0 || row >= filteredRecords.size()) {
                showInfo("Vui lòng chọn một hồ sơ lưu trú trong bảng.");
                return;
            }
            showInfo("Đang xem chi tiết hồ sơ " + filteredRecords.get(row).maHoSo + ".");
        });
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private static final class StayRecord {
        private final String maHoSo;
        private final String maDatPhong;
        private final String khachHang;
        private final String soPhong;
        private final String loaiPhong;
        private final String trangThaiPhong;
        private final String tienCoc;
        private final String dichVuPhatSinh;
        private final String ghiChu;
        private final String gioVao;
        private final String gioRaDuKien;
        private final String trangThai;
        private final String tang;
        private final String caLam;

        private StayRecord(String maHoSo, String maDatPhong, String khachHang, String soPhong, String loaiPhong,
                           String trangThaiPhong, String tienCoc, String dichVuPhatSinh, String ghiChu,
                           String gioVao, String gioRaDuKien, String trangThai, String tang, String caLam) {
            this.maHoSo = maHoSo;
            this.maDatPhong = maDatPhong;
            this.khachHang = khachHang;
            this.soPhong = soPhong;
            this.loaiPhong = loaiPhong;
            this.trangThaiPhong = trangThaiPhong;
            this.tienCoc = tienCoc;
            this.dichVuPhatSinh = dichVuPhatSinh;
            this.ghiChu = ghiChu;
            this.gioVao = gioVao;
            this.gioRaDuKien = gioRaDuKien;
            this.trangThai = trangThai;
            this.tang = tang;
            this.caLam = caLam;
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
