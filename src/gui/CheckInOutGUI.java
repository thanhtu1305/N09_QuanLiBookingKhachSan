package gui;

import db.ConnectDB;
import gui.common.AppBranding;
import gui.common.AppDatePickerField;
import gui.common.AppTimePickerField;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil;
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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final List<CheckInOutGUI> OPEN_INSTANCES = new ArrayList<CheckInOutGUI>();

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
    private JPanel realtimeMapPanel;

    public CheckInOutGUI() {
        this("guest", "Lễ tân");
    }

    public CheckInOutGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý Check-in / Check-out - Hotel PMS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        reloadSampleData(false);
        registerShortcuts();

        synchronized (OPEN_INSTANCES) {
            OPEN_INSTANCES.add(this);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                synchronized (OPEN_INSTANCES) {
                    OPEN_INSTANCES.remove(CheckInOutGUI.this);
                }
            }
        });
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

        JLabel lblSub = new JLabel("Lấy dữ liệu từ Đặt phòng, chọn phòng trống theo tầng và cập nhật sơ đồ phòng thời gian thực.");
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
        card.add(createPrimaryButton("Check-in", new Color(22, 163, 74), Color.WHITE, e -> openCheckInDialog()));
        card.add(createPrimaryButton("Thêm dịch vụ", new Color(37, 99, 235), Color.WHITE, e -> openAddServiceDialog()));
        card.add(createPrimaryButton("Đổi phòng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openChangeRoomDialog()));
        card.add(createPrimaryButton("Gia hạn", new Color(59, 130, 246), Color.WHITE, e -> openExtendDialog()));
        card.add(createPrimaryButton("Check-out", new Color(220, 38, 38), Color.WHITE, e -> openCheckOutDialog()));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Chờ check-in", "Đang ở", "Đã check-out"});
        cboTang = createComboBox(new String[]{"Tất cả", "Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite", "Family"});
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

        realtimeMapPanel = new JPanel();
        realtimeMapPanel.setOpaque(false);
        realtimeMapPanel.setLayout(new BoxLayout(realtimeMapPanel, BoxLayout.Y_AXIS));
        refreshRealtimeMap();

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(realtimeMapPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomRow(String floorName, List<RoomBadge> rooms) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName + ":");
        lblFloor.setPreferredSize(new Dimension(56, 32));
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFloor.setForeground(TEXT_PRIMARY);

        JPanel roomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomPanel.setOpaque(false);
        for (RoomBadge room : rooms) {
            roomPanel.add(createRoomBadge(room.roomCode, room.statusCode));
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
        legend.add(createLegendItem("Hoạt động", resolveStatusColor("T")));
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

    private void reloadSampleData(boolean showMessage) {
        loadStayData();
        cboTrangThai.setSelectedIndex(0);
        cboTang.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        cboCaLam.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        refreshRealtimeMap();
        if (showMessage) {
            showInfo("Đã làm mới dữ liệu check-in / check-out.");
        }
    }

    private void loadStayData() {
        allRecords.clear();
        loadPendingBookings();
        loadActiveStays();
    }

    private void loadPendingBookings() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        String sql = "SELECT ctdp.maChiTietDatPhong, dp.maDatPhong, kh.hoTen, dp.tienCoc, dp.ngayNhanPhong, dp.ngayTraPhong, dp.trangThai, ctdp.soNguoi, " +
                "ctdp.maPhong, ISNULL(p.soPhong, N'Chưa gán') AS soPhong, ISNULL(p.tang, N'-') AS tang, " +
                "ISNULL(lp.tenLoaiPhong, lp2.tenLoaiPhong) AS tenLoaiPhong " +
                "FROM DatPhong dp " +
                "JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
                "LEFT JOIN LuuTru lt ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "LEFT JOIN BangGia bg ON dp.maBangGia = bg.maBangGia " +
                "LEFT JOIN LoaiPhong lp2 ON bg.maLoaiPhong = lp2.maLoaiPhong " +
                "WHERE lt.maLuuTru IS NULL AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in') " +
                "ORDER BY dp.maDatPhong DESC, ctdp.maChiTietDatPhong ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StayRecord record = new StayRecord();
                record.maHoSo = "CHO-" + rs.getInt("maChiTietDatPhong");
                record.maLuuTru = 0;
                record.maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                record.maDatPhong = rs.getInt("maDatPhong");
                record.maPhongId = rs.getObject("maPhong") == null ? 0 : rs.getInt("maPhong");
                record.khachHang = safeValue(rs.getString("hoTen"), "-");
                record.soPhong = safeValue(rs.getString("soPhong"), "Chưa gán");
                record.loaiPhong = safeValue(rs.getString("tenLoaiPhong"), "-");
                record.trangThaiPhong = record.maPhongId > 0 ? "Đã đặt" : "Hoạt động";
                record.tienCoc = formatMoney(rs.getDouble("tienCoc"));
                record.dichVuPhatSinh = "0";
                record.ghiChu = record.maPhongId > 0
                        ? "Phòng đã được đặt từ màn Đặt phòng. Check-in sẽ dùng đúng phòng này."
                        : "Chưa gán phòng ở màn Đặt phòng, cần chọn phòng đang Hoạt động để check-in.";
                record.gioVao = rs.getDate("ngayNhanPhong") == null ? "-" : DATE_FORMAT.format(((Date) rs.getDate("ngayNhanPhong")).toLocalDate());
                record.gioRaDuKien = rs.getDate("ngayTraPhong") == null ? "-" : DATE_FORMAT.format(((Date) rs.getDate("ngayTraPhong")).toLocalDate());
                record.trangThai = "Chờ check-in";
                record.tang = safeValue(rs.getString("tang"), "-");
                record.caLam = resolveCurrentShift();
                record.expectedCheckInDate = rs.getDate("ngayNhanPhong") == null ? LocalDate.now() : ((Date) rs.getDate("ngayNhanPhong")).toLocalDate();
                record.expectedCheckOutDate = rs.getDate("ngayTraPhong") == null ? LocalDate.now().plusDays(1) : ((Date) rs.getDate("ngayTraPhong")).toLocalDate();
                record.soNguoi = rs.getInt("soNguoi");
                allRecords.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Không thể tải danh sách booking chờ check-in.");
        }
    }

    private void loadActiveStays() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, kh.hoTen, p.soPhong, lp.tenLoaiPhong, p.trangThai, p.tang, lt.tienCoc, dp.trangThai AS trangThaiDatPhong, " +
                "ISNULL(SUM(sddv.thanhTien), 0) AS tienDichVu, lt.checkIn, lt.checkOut " +
                "FROM LuuTru lt " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang " +
                "JOIN Phong p ON lt.maPhong = p.maPhong " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "LEFT JOIN SuDungDichVu sddv ON lt.maLuuTru = sddv.maLuuTru " +
                "GROUP BY lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, kh.hoTen, p.soPhong, lp.tenLoaiPhong, p.trangThai, p.tang, lt.tienCoc, dp.trangThai, lt.checkIn, lt.checkOut";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                StayRecord record = new StayRecord();
                record.maHoSo = "LT-" + rs.getInt("maLuuTru");
                record.maLuuTru = rs.getInt("maLuuTru");
                record.maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                record.maDatPhong = rs.getInt("maDatPhong");
                record.maPhongId = rs.getInt("maPhong");
                record.khachHang = safeValue(rs.getString("hoTen"), "-");
                record.soPhong = safeValue(rs.getString("soPhong"), "-");
                record.loaiPhong = safeValue(rs.getString("tenLoaiPhong"), "-");
                record.trangThaiPhong = safeValue(rs.getString("trangThai"), "-");
                record.tienCoc = formatMoney(rs.getDouble("tienCoc"));
                record.dichVuPhatSinh = formatMoney(rs.getDouble("tienDichVu"));
                record.ghiChu = "Dữ liệu đang lấy từ bảng LuuTru.";
                Timestamp checkInTs = rs.getTimestamp("checkIn");
                Timestamp checkOutTs = rs.getTimestamp("checkOut");
                record.gioVao = checkInTs == null ? "-" : DATE_FORMAT.format(checkInTs.toLocalDateTime().toLocalDate()) + " " + TIME_FORMAT.format(checkInTs.toLocalDateTime().toLocalTime());
                record.gioRaDuKien = checkOutTs == null ? "-" : DATE_FORMAT.format(checkOutTs.toLocalDateTime().toLocalDate()) + " " + TIME_FORMAT.format(checkOutTs.toLocalDateTime().toLocalTime());
                String trangThaiDatPhong = safeValue(rs.getString("trangThaiDatPhong"), "");
                if ("Đã check-out".equalsIgnoreCase(trangThaiDatPhong)) {
                    record.trangThai = "Đã check-out";
                } else {
                    record.trangThai = "Đang ở";
                }
                record.tang = safeValue(rs.getString("tang"), "-");
                record.caLam = resolveCurrentShift();
                record.expectedCheckInDate = checkInTs == null ? LocalDate.now() : checkInTs.toLocalDateTime().toLocalDate();
                record.expectedCheckOutDate = checkOutTs == null ? LocalDate.now().plusDays(1) : checkOutTs.toLocalDateTime().toLocalDate();
                allRecords.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Không thể tải dữ liệu lưu trú.");
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
            showInfo("Đã lọc được " + filteredRecords.size() + " hồ sơ phù hợp.");
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
        lblMaDatPhong.setText("DP" + record.maDatPhong);
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

    private StayRecord getSelectedRecord() {
        int row = tblLuuTru.getSelectedRow();
        if (row < 0 || row >= filteredRecords.size()) {
            showInfo("Vui lòng chọn một hồ sơ trong bảng.");
            return null;
        }
        return filteredRecords.get(row);
    }

    private void openCheckInDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!"Chờ check-in".equals(record.trangThai)) {
            showInfo("Chỉ booking chờ check-in mới dùng được chức năng này.");
            return;
        }
        new CheckInDialog(this, record).setVisible(true);
    }

    private void openAddServiceDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!"Đang ở".equals(record.trangThai)) {
            showInfo("Chỉ hồ sơ đang ở mới thêm dịch vụ.");
            return;
        }
        new AddServiceDialog(this, record).setVisible(true);
    }

    private void openChangeRoomDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!"Đang ở".equals(record.trangThai)) {
            showInfo("Chỉ hồ sơ đang ở mới đổi phòng.");
            return;
        }
        new ChangeRoomDialog(this, record).setVisible(true);
    }

    private void openExtendDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!"Đang ở".equals(record.trangThai)) {
            showInfo("Chỉ hồ sơ đang ở mới gia hạn.");
            return;
        }
        new ExtendStayDialog(this, record).setVisible(true);
    }

    private void openCheckOutDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!"Đang ở".equals(record.trangThai)) {
            showInfo("Chỉ hồ sơ đang ở mới check-out.");
            return;
        }
        new CheckOutDialog(this, record).setVisible(true);
    }

    private void refreshRealtimeMap() {
        if (realtimeMapPanel == null) {
            return;
        }
        realtimeMapPanel.removeAll();

        List<String> floors = new ArrayList<String>();
        List<List<RoomBadge>> floorRooms = new ArrayList<List<RoomBadge>>();
        Connection con = ConnectDB.getConnection();
        if (con != null) {
            String sql = "SELECT soPhong, tang, trangThai FROM Phong ORDER BY TRY_CAST(REPLACE(tang, N'Tầng ', '') AS INT), TRY_CAST(soPhong AS INT), soPhong";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tang = safeValue(rs.getString("tang"), "Khác");
                    int idx = floors.indexOf(tang);
                    if (idx < 0) {
                        floors.add(tang);
                        floorRooms.add(new ArrayList<RoomBadge>());
                        idx = floors.size() - 1;
                    }
                    floorRooms.get(idx).add(new RoomBadge(safeValue(rs.getString("soPhong"), "-"), toStatusCode(safeValue(rs.getString("trangThai"), "Bảo trì"))));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < floors.size(); i++) {
            realtimeMapPanel.add(buildRoomRow(floors.get(i), floorRooms.get(i)));
            if (i < floors.size() - 1) {
                realtimeMapPanel.add(Box.createVerticalStrut(8));
            }
        }
        if (!floors.isEmpty()) {
            realtimeMapPanel.add(Box.createVerticalStrut(10));
        }
        realtimeMapPanel.add(buildLegendPanel());
        realtimeMapPanel.revalidate();
        realtimeMapPanel.repaint();
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
            return "Hoạt động";
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

    private String toStatusCode(String trangThai) {
        if ("Hoạt động".equalsIgnoreCase(trangThai) || "Trống".equalsIgnoreCase(trangThai)) {
            return "T";
        }
        if ("Đã đặt".equalsIgnoreCase(trangThai)) {
            return "D";
        }
        if ("Đang ở".equalsIgnoreCase(trangThai)) {
            return "O";
        }
        if ("Dọn dẹp".equalsIgnoreCase(trangThai)) {
            return "C";
        }
        return "B";
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }

    private String resolveCurrentShift() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Ca sáng";
        }
        if (hour < 18) {
            return "Ca chiều";
        }
        return "Ca tối";
    }

    private List<RoomOption> loadAvailableRooms(String roomType) {
        List<RoomOption> rooms = new ArrayList<RoomOption>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return rooms;
        }
        String sql = "SELECT p.maPhong, p.soPhong, p.tang FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "WHERE p.trangThai = N'Hoạt động' AND (? = '' OR lp.tenLoaiPhong = ?) " +
                "ORDER BY TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String normalized = roomType == null ? "" : roomType.trim();
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new RoomOption(rs.getInt("maPhong"), rs.getString("soPhong"), rs.getString("tang")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }


    private void refreshKhachHangViewsSafely() {
        try {
            Class<?> clazz = Class.forName("gui.KhachHangGUI");
            java.lang.reflect.Method method = clazz.getMethod("refreshAllOpenInstances");
            method.invoke(null);
        } catch (Throwable ignored) {
        }
    }

    public static void refreshAllOpenInstances() {
        List<CheckInOutGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new ArrayList<CheckInOutGUI>(OPEN_INSTANCES);
        }
        for (CheckInOutGUI gui : snapshot) {
            if (gui != null) {
                javax.swing.SwingUtilities.invokeLater(() -> gui.reloadSampleData(false));
            }
        }
    }

    private abstract class BaseStayDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseStayDialog(Frame owner, String title, int width, int height) {
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

    private final class CheckInDialog extends BaseStayDialog {
        private final StayRecord record;
        private final JComboBox<RoomOption> cboRoom;
        private final AppDatePickerField txtNgayVao;
        private final AppTimePickerField txtGioVao;
        private final AppDatePickerField txtNgayRa;
        private final AppTimePickerField txtGioRa;
        private final JTextArea txtGhiChuDialog;
        private final boolean useBookedRoom;

        private CheckInDialog(Frame owner, StayRecord record) {
            super(owner, "Check-in", 680, 520);
            this.record = record;
            this.useBookedRoom = record.maPhongId > 0;

            List<RoomOption> roomOptions = loadAvailableRooms(record.loaiPhong);
            cboRoom = new JComboBox<RoomOption>(roomOptions.toArray(new RoomOption[0]));
            cboRoom.setFont(BODY_FONT);

            txtNgayVao = new AppDatePickerField(record.expectedCheckInDate.format(DATE_FORMAT), true);
            txtGioVao = new AppTimePickerField(LocalTime.now().format(TIME_FORMAT), true);
            txtNgayRa = new AppDatePickerField(record.expectedCheckOutDate.format(DATE_FORMAT), true);
            txtGioRa = new AppTimePickerField("12:00", true);
            txtGhiChuDialog = new JTextArea(3, 20);
            txtGhiChuDialog.setLineWrap(true);
            txtGhiChuDialog.setWrapStyleWord(true);
            txtGhiChuDialog.setFont(BODY_FONT);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHECK-IN TỪ BOOKING",
                    useBookedRoom
                            ? "Khách sẽ check-in đúng phòng đã đặt ở màn Đặt phòng. Sau khi xác nhận, phòng sẽ chuyển sang trạng thái Đang ở."
                            : "Booking chưa gán phòng ở màn Đặt phòng, nhân viên chọn phòng trống để check-in."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(form, gbc, 0, "Mã đặt phòng", createValueLabel("DP" + record.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueLabel(record.khachHang));
            addFormRow(form, gbc, 2, "Loại phòng", createValueLabel(record.loaiPhong));
            if (useBookedRoom) {
                addFormRow(form, gbc, 3, "Phòng đã đặt", createValueLabel(record.soPhong));
            } else {
                addFormRow(form, gbc, 3, "Phòng trống", cboRoom);
            }
            addFormRow(form, gbc, 4, "Ngày vào", txtNgayVao);
            addFormRow(form, gbc, 5, "Giờ vào", txtGioVao);
            addFormRow(form, gbc, 6, "Ngày ra", txtNgayRa);
            addFormRow(form, gbc, 7, "Giờ ra", txtGioRa);
            addFormRow(form, gbc, 8, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(22, 163, 74), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            Integer maPhong;
            if (useBookedRoom) {
                maPhong = Integer.valueOf(record.maPhongId);
            } else {
                if (cboRoom.getItemCount() == 0 || cboRoom.getSelectedItem() == null) {
                    showInfo("Không còn phòng trống phù hợp cho loại phòng này.");
                    return;
                }
                maPhong = Integer.valueOf(((RoomOption) cboRoom.getSelectedItem()).maPhong);
            }
            LocalDate ngayVao = txtNgayVao.getDateValue();
            LocalDate ngayRa = txtNgayRa.getDateValue();
            LocalTime gioVao = txtGioVao.getTimeValue();
            LocalTime gioRa = txtGioRa.getTimeValue();
            if (ngayVao == null || ngayRa == null || gioVao == null || gioRa == null) {
                showInfo("Ngày giờ vào/ra không hợp lệ.");
                return;
            }
            LocalDateTime checkIn = LocalDateTime.of(ngayVao, gioVao);
            LocalDateTime checkOut = LocalDateTime.of(ngayRa, gioRa);
            if (!checkOut.isAfter(checkIn)) {
                showInfo("Giờ ra phải lớn hơn giờ vào.");
                return;
            }

            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Không thể kết nối cơ sở dữ liệu.");
                return;
            }

            try {
                con.setAutoCommit(false);

                if (!useBookedRoom) {
                    try (PreparedStatement ps = con.prepareStatement("UPDATE ChiTietDatPhong SET maPhong = ? WHERE maChiTietDatPhong = ?")) {
                        ps.setInt(1, maPhong.intValue());
                        ps.setInt(2, record.maChiTietDatPhong);
                        ps.executeUpdate();
                    }
                }

                String insertStay = "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(insertStay)) {
                    ps.setInt(1, record.maChiTietDatPhong);
                    ps.setInt(2, record.maDatPhong);
                    ps.setInt(3, maPhong.intValue());
                    ps.setTimestamp(4, Timestamp.valueOf(checkIn));
                    ps.setTimestamp(5, Timestamp.valueOf(checkOut));
                    ps.setInt(6, record.soNguoi);
                    ps.setDouble(7, 0);
                    ps.setDouble(8, parseDoubleMoney(record.tienCoc));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET trangThai = N'Đang ở' WHERE maPhong = ?")) {
                    ps.setInt(1, maPhong.intValue());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'Đang lưu trú' WHERE maDatPhong = ?")) {
                    ps.setInt(1, record.maDatPhong);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE KhachHang SET trangThai = N'Hoạt động' " +
                                "WHERE maKhachHang = (SELECT TOP 1 maKhachHang FROM DatPhong WHERE maDatPhong = ?)")) {
                    ps.setInt(1, record.maDatPhong);
                    ps.executeUpdate();
                }

                con.commit();
                PhongGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
                refreshKhachHangViewsSafely();
                CheckInOutGUI.refreshAllOpenInstances();
                showInfo(useBookedRoom ? "Check-in thành công đúng phòng khách đã đặt." : "Check-in thành công.");
                dispose();
            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignore) {}
                e.printStackTrace();
                showInfo("Không thể check-in: " + e.getMessage());
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }
    }

    private final class AddServiceDialog extends BaseStayDialog {
        private final StayRecord record;

        private AddServiceDialog(Frame owner, StayRecord record) {
            super(owner, "Thêm dịch vụ", 560, 360);
            this.record = record;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("THÊM DỊCH VỤ", "Thêm dịch vụ vào hồ sơ lưu trú hiện tại."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtDichVu = createInputField("");
            JTextField txtSoLuong = createInputField("1");

            addFormRow(form, gbc, 0, "Mã hồ sơ", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Dịch vụ", txtDichVu);
            addFormRow(form, gbc, 2, "Số lượng", txtSoLuong);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Lưu", new Color(37, 99, 235), Color.WHITE, e -> submit(txtDichVu, txtSoLuong));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextField txtDichVu, JTextField txtSoLuong) {
            String tenDichVu = txtDichVu.getText().trim();
            if (tenDichVu.isEmpty()) {
                showInfo("Tên dịch vụ không được trống.");
                return;
            }
            int soLuong;
            try {
                soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            } catch (NumberFormatException ex) {
                showInfo("Số lượng không hợp lệ.");
                return;
            }
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try {
                Integer maDichVu = null;
                double donGia = 0;
                try (PreparedStatement ps = con.prepareStatement("SELECT TOP 1 maDichVu, donGia FROM DichVu WHERE tenDichVu = ?")) {
                    ps.setString(1, tenDichVu);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maDichVu = Integer.valueOf(rs.getInt("maDichVu"));
                            donGia = rs.getDouble("donGia");
                        }
                    }
                }
                if (maDichVu == null) {
                    showInfo("Không tìm thấy dịch vụ trong bảng DichVu.");
                    return;
                }
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO SuDungDichVu(maLuuTru, maDichVu, soLuong, donGia) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, record.maLuuTru);
                    ps.setInt(2, maDichVu.intValue());
                    ps.setInt(3, soLuong);
                    ps.setDouble(4, donGia);
                    ps.executeUpdate();
                }
                CheckInOutGUI.refreshAllOpenInstances();
                showInfo("Đã thêm dịch vụ.");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Không thể thêm dịch vụ.");
            }
        }
    }

    private final class ChangeRoomDialog extends BaseStayDialog {
        private final StayRecord record;

        private ChangeRoomDialog(Frame owner, StayRecord record) {
            super(owner, "Đổi phòng", 560, 360);
            this.record = record;

            List<RoomOption> roomOptions = loadAvailableRooms(record.loaiPhong);
            JComboBox<RoomOption> cboRoom = new JComboBox<RoomOption>(roomOptions.toArray(new RoomOption[0]));
            cboRoom.setFont(BODY_FONT);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("ĐỔI PHÒNG", "Cập nhật phòng mới cho khách lưu trú và đổi trạng thái phòng cũ sang Dọn dẹp."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(form, gbc, 0, "Mã hồ sơ", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Phòng hiện tại", createValueLabel(record.soPhong));
            addFormRow(form, gbc, 2, "Phòng mới", cboRoom);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit(cboRoom));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JComboBox<RoomOption> cboRoom) {
            if (cboRoom.getItemCount() == 0 || cboRoom.getSelectedItem() == null) {
                showInfo("Không còn phòng trống cùng loại để đổi.");
                return;
            }
            RoomOption room = (RoomOption) cboRoom.getSelectedItem();
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try {
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement("UPDATE LuuTru SET maPhong = ? WHERE maLuuTru = ?")) {
                    ps.setInt(1, room.maPhong);
                    ps.setInt(2, record.maLuuTru);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE ChiTietDatPhong SET maPhong = ? WHERE maChiTietDatPhong = ?")) {
                    ps.setInt(1, room.maPhong);
                    ps.setInt(2, record.maChiTietDatPhong);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET trangThai = N'Hoạt động' WHERE soPhong = ?")) {
                    ps.setString(1, record.soPhong);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET trangThai = N'Đang ở' WHERE maPhong = ?")) {
                    ps.setInt(1, room.maPhong);
                    ps.executeUpdate();
                }
                con.commit();
                PhongGUI.refreshAllOpenInstances();
                CheckInOutGUI.refreshAllOpenInstances();
                showInfo("Đổi phòng thành công.");
                dispose();
            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignore) {}
                e.printStackTrace();
                showInfo("Không thể đổi phòng.");
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }
    }

    private final class ExtendStayDialog extends BaseStayDialog {
        private final StayRecord record;

        private ExtendStayDialog(Frame owner, StayRecord record) {
            super(owner, "Gia hạn", 560, 360);
            this.record = record;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("GIA HẠN LƯU TRÚ", "Cập nhật ngày giờ trả mới cho hồ sơ đang ở."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            AppDatePickerField txtNgayRa = new AppDatePickerField(record.expectedCheckOutDate.format(DATE_FORMAT), true);
            AppTimePickerField txtGioRa = new AppTimePickerField("12:00", true);

            addFormRow(form, gbc, 0, "Mã hồ sơ", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Ngày trả mới", txtNgayRa);
            addFormRow(form, gbc, 2, "Giờ trả mới", txtGioRa);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(59, 130, 246), Color.WHITE, e -> submit(txtNgayRa, txtGioRa));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa) {
            if (txtNgayRa.getDateValue() == null || txtGioRa.getTimeValue() == null) {
                showInfo("Ngày giờ trả mới không hợp lệ.");
                return;
            }
            LocalDateTime newCheckOut = LocalDateTime.of(txtNgayRa.getDateValue(), txtGioRa.getTimeValue());
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(newCheckOut));
                    ps.setInt(2, record.maLuuTru);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET ngayTraPhong = ? WHERE maDatPhong = ?")) {
                    ps.setDate(1, Date.valueOf(txtNgayRa.getDateValue()));
                    ps.setInt(2, record.maDatPhong);
                    ps.executeUpdate();
                }
                CheckInOutGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
                showInfo("Gia hạn thành công.");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Không thể gia hạn.");
            }
        }
    }

    private final class CheckOutDialog extends BaseStayDialog {
        private final StayRecord record;

        private CheckOutDialog(Frame owner, StayRecord record) {
            super(owner, "Check-out", 560, 380);
            this.record = record;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHECK-OUT", "Xác nhận trả phòng, trạng thái phòng sẽ chuyển sang Hoạt động và tự động mở màn Thanh toán."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            AppDatePickerField txtNgayRa = new AppDatePickerField(LocalDate.now().format(DATE_FORMAT), true);
            AppTimePickerField txtGioRa = new AppTimePickerField(LocalTime.now().format(TIME_FORMAT), true);

            addFormRow(form, gbc, 0, "Mã hồ sơ", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Số phòng", createValueLabel(record.soPhong));
            addFormRow(form, gbc, 2, "Ngày ra", txtNgayRa);
            addFormRow(form, gbc, 3, "Giờ ra", txtGioRa);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(220, 38, 38), Color.WHITE, e -> submit(txtNgayRa, txtGioRa));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa) {
            if (txtNgayRa.getDateValue() == null || txtGioRa.getTimeValue() == null) {
                showInfo("Ngày giờ ra không hợp lệ.");
                return;
            }
            LocalDateTime checkOut = LocalDateTime.of(txtNgayRa.getDateValue(), txtGioRa.getTimeValue());
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try {
                con.setAutoCommit(false);
                try (PreparedStatement ps = con.prepareStatement("UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(checkOut));
                    ps.setInt(2, record.maLuuTru);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET trangThai = N'Hoạt động' WHERE soPhong = ?")) {
                    ps.setString(1, record.soPhong);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'Đã check-out' WHERE maDatPhong = ?")) {
                    ps.setInt(1, record.maDatPhong);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE KhachHang SET trangThai = N'Ngừng giao dịch' " +
                                "WHERE maKhachHang = (SELECT TOP 1 maKhachHang FROM DatPhong WHERE maDatPhong = ?)")) {
                    ps.setInt(1, record.maDatPhong);
                    ps.executeUpdate();
                }
                con.commit();
                PhongGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
                refreshKhachHangViewsSafely();
                CheckInOutGUI.refreshAllOpenInstances();
                NavigationUtil.navigate(
                        CheckInOutGUI.this,
                        ScreenKey.CHECK_IN_OUT,
                        ScreenKey.THANH_TOAN,
                        username,
                        role
                );
                dispose();
            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignore) {}
                e.printStackTrace();
                showInfo("Không thể check-out.");
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }
    }

    private double parseDoubleMoney(String value) {
        try {
            return Double.parseDouble(value.replace(".", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "cio-f1", this::openCheckInDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "cio-f2", this::openAddServiceDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "cio-f3", this::openChangeRoomDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "cio-f4", this::openExtendDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "cio-f5", this::openCheckOutDialog);
        ScreenUIHelper.registerShortcut(this, "ENTER", "cio-enter", () -> {
            StayRecord record = getSelectedRecord();
            if (record != null) {
                showInfo("Đang xem chi tiết hồ sơ " + record.maHoSo + ".");
            }
        });
    }

    private static final class StayRecord {
        private String maHoSo;
        private int maLuuTru;
        private int maChiTietDatPhong;
        private int maDatPhong;
        private int maPhongId;
        private String khachHang;
        private String soPhong;
        private String loaiPhong;
        private String trangThaiPhong;
        private String tienCoc;
        private String dichVuPhatSinh;
        private String ghiChu;
        private String gioVao;
        private String gioRaDuKien;
        private String trangThai;
        private String tang;
        private String caLam;
        private LocalDate expectedCheckInDate;
        private LocalDate expectedCheckOutDate;
        private int soNguoi;
    }

    private static final class RoomBadge {
        private final String roomCode;
        private final String statusCode;

        private RoomBadge(String roomCode, String statusCode) {
            this.roomCode = roomCode;
            this.statusCode = statusCode;
        }
    }

    private static final class RoomOption {
        private final int maPhong;
        private final String soPhong;
        private final String tang;

        private RoomOption(int maPhong, String soPhong, String tang) {
            this.maPhong = maPhong;
            this.soPhong = soPhong;
            this.tang = tang;
        }

        @Override
        public String toString() {
            return soPhong + " - " + tang;
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }
}
