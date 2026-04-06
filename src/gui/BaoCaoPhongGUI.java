package gui;

import gui.common.AppBranding;
import gui.common.AppDatePickerField;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoPhongGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color BRAND_GREEN = new Color(22, 163, 74);
    private static final Color BRAND_AMBER = new Color(245, 158, 11);
    private static final Color BRAND_RED = new Color(220, 38, 38);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private final String username;
    private final String role;
    private final List<RoomReportRecord> roomRecords = createSampleData();

    private JPanel rootPanel;
    private JComboBox<String> cboNgay;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboNam;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblSoPhongHoatDong;
    private JLabel lblSoPhongDangO;
    private JLabel lblSoPhongDaDat;
    private JLabel lblSoPhongBaoTri;
    private JLabel lblSoPhongHoatDongSub;
    private JLabel lblSoPhongDangOSub;
    private JLabel lblSoPhongDaDatSub;
    private JLabel lblSoPhongBaoTriSub;

    private JLabel lblCongSuat;
    private JLabel lblLoaiPhongPhoBien;
    private JLabel lblLoaiPhongPhoBienSub;

    private JTable tblTrangThaiPhong;
    private DefaultTableModel tableModel;
    private RoomUsageChartPanel chartPanel;

    public BaoCaoPhongGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");

        setTitle("Báo cáo phòng - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadRoomData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.BAO_CAO_PHONG, username, role), BorderLayout.WEST);
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
        top.add(Box.createVerticalStrut(10));
        top.add(buildSummaryCards());

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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("BÁO CÁO PHÒNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi công suất phòng, trạng thái khai thác và loại phòng được sử dụng nhiều nhất.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Báo cáo phòng"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem báo cáo", BRAND_GREEN, Color.WHITE, e -> loadRoomData(true)));
        card.add(createPrimaryButton("Xuất file", BRAND_BLUE, Color.WHITE, e -> showInfo("Đã sẵn sàng xuất báo cáo phòng.")));
        card.add(createPrimaryButton("Làm mới", new Color(15, 118, 110), Color.WHITE, e -> resetFilters()));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboNgay = createComboBox(new String[]{"Hôm nay", "Tuần này", "Tháng này"});
        cboThang = createComboBox(new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        cboNam = createComboBox(new String[]{"2026", "2025", "2024"});
        txtTuNgay = new AppDatePickerField("01/04/2026", true);
        txtDenNgay = new AppDatePickerField("30/04/2026", true);

        left.add(createFieldGroup("Kỳ xem", cboNgay));
        left.add(createFieldGroup("Tháng", cboThang));
        left.add(createFieldGroup("Năm", cboNam));
        left.add(createFieldGroup("Từ ngày", txtTuNgay));
        left.add(createFieldGroup("Đến ngày", txtDenNgay));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblSearch = new JLabel("Tìm nhanh");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);

        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(210, 34));
        txtTuKhoa.setToolTipText("Loại phòng hoặc trạng thái");

        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));
        right.add(txtTuKhoa);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setOpaque(false);

        lblSoPhongHoatDong = new JLabel();
        lblSoPhongDangO = new JLabel();
        lblSoPhongDaDat = new JLabel();
        lblSoPhongBaoTri = new JLabel();
        lblSoPhongHoatDongSub = new JLabel();
        lblSoPhongDangOSub = new JLabel();
        lblSoPhongDaDatSub = new JLabel();
        lblSoPhongBaoTriSub = new JLabel();

        panel.add(createSummaryCard("Số phòng hoạt động", lblSoPhongHoatDong, lblSoPhongHoatDongSub, BRAND_BLUE));
        panel.add(createSummaryCard("Số phòng đang ở", lblSoPhongDangO, lblSoPhongDangOSub, BRAND_GREEN));
        panel.add(createSummaryCard("Số phòng đã đặt", lblSoPhongDaDat, lblSoPhongDaDatSub, BRAND_AMBER));
        panel.add(createSummaryCard("Số phòng bảo trì", lblSoPhongBaoTri, lblSoPhongBaoTriSub, BRAND_RED));
        return panel;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));
        top.setOpaque(false);
        top.add(buildChartCard());
        top.add(buildOccupancyCard());

        center.add(top, BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        return center;
    }

    private JPanel buildChartCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Biểu đồ công suất theo loại phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Mức sử dụng của từng hạng phòng đang khai thác.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        chartPanel = new RoomUsageChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 280));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOccupancyCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Cụm công suất sử dụng phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        lblCongSuat = new JLabel();
        lblCongSuat.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblCongSuat.setForeground(BRAND_BLUE);
        lblCongSuat.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCongSuatSub = new JLabel("Công suất sử dụng trung bình toàn khách sạn");
        lblCongSuatSub.setFont(BODY_FONT);
        lblCongSuatSub.setForeground(TEXT_MUTED);
        lblCongSuatSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topBadge = createHighlightBadge("Loại phòng dùng nhiều nhất");
        lblLoaiPhongPhoBien = new JLabel();
        lblLoaiPhongPhoBien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLoaiPhongPhoBien.setForeground(TEXT_PRIMARY);

        lblLoaiPhongPhoBienSub = new JLabel();
        lblLoaiPhongPhoBienSub.setFont(BODY_FONT);
        lblLoaiPhongPhoBienSub.setForeground(TEXT_MUTED);

        content.add(lblCongSuat);
        content.add(Box.createVerticalStrut(6));
        content.add(lblCongSuatSub);
        content.add(Box.createVerticalStrut(18));
        content.add(topBadge);
        content.add(Box.createVerticalStrut(8));
        content.add(lblLoaiPhongPhoBien);
        content.add(Box.createVerticalStrut(4));
        content.add(lblLoaiPhongPhoBienSub);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảng thống kê trạng thái phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chi tiết theo loại phòng để dễ đối chiếu và thuyết trình.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new String[]{"Loại phòng", "Hoạt động", "Đang ở", "Đã đặt", "Bảo trì", "Công suất"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTrangThaiPhong = new JTable(tableModel);
        tblTrangThaiPhong.setFont(BODY_FONT);
        tblTrangThaiPhong.setRowHeight(30);
        tblTrangThaiPhong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTrangThaiPhong.setGridColor(BORDER_SOFT);
        tblTrangThaiPhong.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblTrangThaiPhong);

        JScrollPane scrollPane = new JScrollPane(tblTrangThaiPhong);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Xem báo cáo",
                "F2 Xuất file",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void loadRoomData(boolean showMessage) {
        List<RoomReportRecord> filtered = filterRecords();
        updateSummary(filtered);
        reloadTable(filtered);
        updateOccupancyArea(filtered);
        chartPanel.setRecords(filtered);
        if (showMessage) {
            showInfo("Đã cập nhật báo cáo phòng.");
        }
    }

    private List<RoomReportRecord> filterRecords() {
        List<RoomReportRecord> filtered = new ArrayList<RoomReportRecord>();
        String keyword = txtTuKhoa == null ? "" : txtTuKhoa.getText().trim().toLowerCase();
        for (RoomReportRecord record : roomRecords) {
            if (!keyword.isEmpty() && !record.loaiPhong.toLowerCase().contains(keyword)) {
                continue;
            }
            filtered.add(record);
        }
        return filtered;
    }

    private void updateSummary(List<RoomReportRecord> records) {
        int tongHoatDong = 0;
        int tongDangO = 0;
        int tongDaDat = 0;
        int tongBaoTri = 0;

        for (RoomReportRecord record : records) {
            tongHoatDong += record.soPhongHoatDong;
            tongDangO += record.soPhongDangO;
            tongDaDat += record.soPhongDaDat;
            tongBaoTri += record.soPhongBaoTri;
        }

        lblSoPhongHoatDong.setText(String.valueOf(tongHoatDong));
        lblSoPhongDangO.setText(String.valueOf(tongDangO));
        lblSoPhongDaDat.setText(String.valueOf(tongDaDat));
        lblSoPhongBaoTri.setText(String.valueOf(tongBaoTri));

        lblSoPhongHoatDongSub.setText("Tỷ lệ khai thác: " + percentText(tongDangO + tongDaDat, tongHoatDong));
        lblSoPhongDangOSub.setText("Đang sử dụng thực tế");
        lblSoPhongDaDatSub.setText("Chuẩn bị đón khách");
        lblSoPhongBaoTriSub.setText("Cần xử lý kỹ thuật");
    }

    private void updateOccupancyArea(List<RoomReportRecord> records) {
        int tongHoatDong = 0;
        int tongDangO = 0;
        int tongDaDat = 0;
        RoomReportRecord topRoomType = null;

        for (RoomReportRecord record : records) {
            tongHoatDong += record.soPhongHoatDong;
            tongDangO += record.soPhongDangO;
            tongDaDat += record.soPhongDaDat;
            if (topRoomType == null || record.soPhongDangO > topRoomType.soPhongDangO) {
                topRoomType = record;
            }
        }

        lblCongSuat.setText(percentText(tongDangO + tongDaDat, tongHoatDong));
        if (topRoomType == null) {
            lblLoaiPhongPhoBien.setText("Chưa có dữ liệu");
            lblLoaiPhongPhoBienSub.setText("Không xác định được loại phòng nổi bật.");
        } else {
            lblLoaiPhongPhoBien.setText(topRoomType.loaiPhong);
            lblLoaiPhongPhoBienSub.setText("Đang ở " + topRoomType.soPhongDangO + "/" + topRoomType.soPhongHoatDong
                    + " phòng | Công suất " + topRoomType.congSuat + "%");
        }
    }

    private void reloadTable(List<RoomReportRecord> records) {
        tableModel.setRowCount(0);
        for (RoomReportRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.loaiPhong,
                    record.soPhongHoatDong,
                    record.soPhongDangO,
                    record.soPhongDaDat,
                    record.soPhongBaoTri,
                    record.congSuat + "%"
            });
        }
        if (!records.isEmpty()) {
            tblTrangThaiPhong.setRowSelectionInterval(0, 0);
        }
    }

    private void resetFilters() {
        cboNgay.setSelectedIndex(0);
        cboThang.setSelectedIndex(3);
        cboNam.setSelectedIndex(0);
        txtTuNgay.setText("01/04/2026");
        txtDenNgay.setText("30/04/2026");
        txtTuKhoa.setText("");
        loadRoomData(false);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "bao_cao_phong_view", new Runnable() {
            @Override
            public void run() {
                loadRoomData(true);
            }
        });
        ScreenUIHelper.registerShortcut(this, "F2", "bao_cao_phong_export", new Runnable() {
            @Override
            public void run() {
                showInfo("Đã sẵn sàng xuất báo cáo phòng.");
            }
        });
        ScreenUIHelper.registerShortcut(this, "F5", "bao_cao_phong_refresh", new Runnable() {
            @Override
            public void run() {
                resetFilters();
            }
        });
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, JLabel subLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBackground(accent);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(BODY_FONT);
        lblTitle.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_PRIMARY);

        subLabel.setFont(LABEL_FONT);
        subLabel.setForeground(TEXT_MUTED);

        top.add(dot);
        top.add(Box.createHorizontalStrut(8));
        top.add(lblTitle);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createHighlightBadge(String text) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        badge.setOpaque(false);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBackground(BRAND_AMBER);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_MUTED);

        badge.add(dot);
        badge.add(label);
        return badge;
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
        comboBox.setPreferredSize(new Dimension(145, 34));
        comboBox.setMaximumSize(new Dimension(165, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(180, 34));
        field.setMaximumSize(new Dimension(220, 34));
        return field;
    }

    private JButton createPrimaryButton(String text, Color background, Color foreground,
                                        java.awt.event.ActionListener listener) {
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

    private String percentText(int part, int total) {
        if (total <= 0) {
            return "0%";
        }
        double percent = (double) part * 100d / (double) total;
        return String.format("%.1f%%", percent);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Báo cáo phòng", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<RoomReportRecord> createSampleData() {
        List<RoomReportRecord> data = new ArrayList<RoomReportRecord>();
        data.add(new RoomReportRecord("Standard", 20, 13, 4, 1, 85));
        data.add(new RoomReportRecord("Deluxe", 22, 17, 3, 1, 91));
        data.add(new RoomReportRecord("Suite", 10, 8, 1, 1, 90));
        data.add(new RoomReportRecord("Family", 6, 5, 1, 0, 100));
        return data;
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    private static final class RoomReportRecord {
        private final String loaiPhong;
        private final int soPhongHoatDong;
        private final int soPhongDangO;
        private final int soPhongDaDat;
        private final int soPhongBaoTri;
        private final int congSuat;

        private RoomReportRecord(String loaiPhong, int soPhongHoatDong, int soPhongDangO,
                                 int soPhongDaDat, int soPhongBaoTri, int congSuat) {
            this.loaiPhong = loaiPhong;
            this.soPhongHoatDong = soPhongHoatDong;
            this.soPhongDangO = soPhongDangO;
            this.soPhongDaDat = soPhongDaDat;
            this.soPhongBaoTri = soPhongBaoTri;
            this.congSuat = congSuat;
        }
    }

    private final class RoomUsageChartPanel extends JPanel {
        private List<RoomReportRecord> records = new ArrayList<RoomReportRecord>();

        private RoomUsageChartPanel() {
            setOpaque(true);
            setBackground(PANEL_SOFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }

        private void setRecords(List<RoomReportRecord> records) {
            this.records = new ArrayList<RoomReportRecord>(records);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int left = 48;
                int right = 20;
                int top = 16;
                int bottom = 48;
                int chartWidth = width - left - right;
                int chartHeight = height - top - bottom;

                if (chartWidth <= 0 || chartHeight <= 0 || records.isEmpty()) {
                    drawEmptyState(g2, width, height);
                    return;
                }

                int maxValue = 100;
                g2.setColor(new Color(226, 232, 240));
                for (int i = 0; i <= 4; i++) {
                    int y = top + i * chartHeight / 4;
                    g2.drawLine(left, y, left + chartWidth, y);
                }

                g2.setColor(TEXT_MUTED);
                g2.setFont(LABEL_FONT);
                for (int i = 0; i <= 4; i++) {
                    int axisValue = maxValue - (maxValue * i / 4);
                    int y = top + i * chartHeight / 4;
                    String text = axisValue + "%";
                    int textWidth = g2.getFontMetrics().stringWidth(text);
                    g2.drawString(text, left - textWidth - 8, y + 4);
                }

                int step = chartWidth / records.size();
                int barWidth = Math.max(24, step / 2);

                for (int i = 0; i < records.size(); i++) {
                    RoomReportRecord record = records.get(i);
                    int centerX = left + step * i + step / 2;
                    int barHeight = (int) Math.round((double) record.congSuat * chartHeight / (double) maxValue);
                    int barX = centerX - barWidth / 2;
                    int barY = top + chartHeight - barHeight;

                    g2.setColor(new Color(191, 219, 254));
                    g2.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
                    g2.setColor(BRAND_BLUE);
                    g2.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);

                    String value = record.congSuat + "%";
                    int valueWidth = g2.getFontMetrics().stringWidth(value);
                    g2.setColor(TEXT_PRIMARY);
                    g2.drawString(value, centerX - valueWidth / 2, barY - 6);

                    int labelWidth = g2.getFontMetrics().stringWidth(record.loaiPhong);
                    g2.setColor(TEXT_MUTED);
                    g2.drawString(record.loaiPhong, centerX - labelWidth / 2, top + chartHeight + 18);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawEmptyState(Graphics2D g2, int width, int height) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(BODY_FONT);
            String message = "Chưa có dữ liệu phòng để hiển thị.";
            int textWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - textWidth) / 2, height / 2);
        }
    }
}
