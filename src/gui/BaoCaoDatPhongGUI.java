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

public class BaoCaoDatPhongGUI extends JFrame {
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
    private final List<BookingReportRecord> bookingRecords = createSampleData();

    private JPanel rootPanel;
    private JComboBox<String> cboCheDoLoc;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboNam;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblTongBooking;
    private JLabel lblDaXacNhan;
    private JLabel lblChoCheckIn;
    private JLabel lblDaHuy;
    private JLabel lblTongBookingSub;
    private JLabel lblDaXacNhanSub;
    private JLabel lblChoCheckInSub;
    private JLabel lblDaHuySub;

    private JLabel lblTrangThaiXacNhan;
    private JLabel lblTrangThaiChoCheckIn;
    private JLabel lblTrangThaiDaHuy;
    private JLabel lblTrangThaiWalkIn;

    private JTable tblBooking;
    private DefaultTableModel tableModel;
    private BookingChartPanel chartPanel;

    public BaoCaoDatPhongGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoDatPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");

        setTitle("Báo cáo đặt phòng - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadBookingData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.BAO_CAO_DAT_PHONG, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("BÁO CÁO ĐẶT PHÒNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi số lượng booking, trạng thái xử lý và xu hướng đặt phòng theo từng giai đoạn.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Báo cáo đặt phòng"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem báo cáo", BRAND_GREEN, Color.WHITE, e -> loadBookingData(true)));
        card.add(createPrimaryButton("Xuất file", BRAND_BLUE, Color.WHITE, e -> showInfo("Đã sẵn sàng xuất báo cáo đặt phòng.")));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboCheDoLoc = createComboBox(new String[]{"Theo khoảng thời gian", "Theo ngày", "Theo tháng", "Theo năm"});
        cboThang = createComboBox(new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        cboNam = createComboBox(new String[]{"2026", "2025", "2024"});
        txtTuNgay = new AppDatePickerField("01/04/2026", true);
        txtDenNgay = new AppDatePickerField("30/04/2026", true);

        left.add(createFieldGroup("Chế độ lọc", cboCheDoLoc));
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
        ScreenUIHelper.applySearchFieldSize(txtTuKhoa);
        txtTuKhoa.setToolTipText("Ngày hoặc trạng thái booking");
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> loadBookingData(false));
        ScreenUIHelper.installAutoFilter(() -> loadBookingData(false), cboCheDoLoc, cboThang, cboNam);

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

        lblTongBooking = new JLabel();
        lblDaXacNhan = new JLabel();
        lblChoCheckIn = new JLabel();
        lblDaHuy = new JLabel();
        lblTongBookingSub = new JLabel();
        lblDaXacNhanSub = new JLabel();
        lblChoCheckInSub = new JLabel();
        lblDaHuySub = new JLabel();

        panel.add(createSummaryCard("Tổng số booking", lblTongBooking, lblTongBookingSub, BRAND_BLUE));
        panel.add(createSummaryCard("Đã xác nhận", lblDaXacNhan, lblDaXacNhanSub, BRAND_GREEN));
        panel.add(createSummaryCard("Chờ check-in", lblChoCheckIn, lblChoCheckInSub, BRAND_AMBER));
        panel.add(createSummaryCard("Đã hủy", lblDaHuy, lblDaHuySub, BRAND_RED));
        return panel;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));
        top.setOpaque(false);
        top.add(buildChartCard());
        top.add(buildStatusCard());

        center.add(top, BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        return center;
    }

    private JPanel buildChartCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Biểu đồ booking theo ngày");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Số lượng booking mới phát sinh theo từng ngày.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        chartPanel = new BookingChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 280));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStatusCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Thống kê booking theo trạng thái");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel content = new JPanel(new GridLayout(4, 1, 0, 8));
        content.setOpaque(false);

        lblTrangThaiXacNhan = createStatusValueLabel();
        lblTrangThaiChoCheckIn = createStatusValueLabel();
        lblTrangThaiDaHuy = createStatusValueLabel();
        lblTrangThaiWalkIn = createStatusValueLabel();

        content.add(createStatusRow("Đã xác nhận", lblTrangThaiXacNhan, BRAND_GREEN));
        content.add(createStatusRow("Chờ check-in", lblTrangThaiChoCheckIn, BRAND_AMBER));
        content.add(createStatusRow("Đã hủy", lblTrangThaiDaHuy, BRAND_RED));
        content.add(createStatusRow("Walk-in", lblTrangThaiWalkIn, BRAND_BLUE));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảng thống kê booking");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chi tiết booking theo thời gian để đối chiếu nhanh khi demo.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new String[]{"Thời gian", "Tổng booking", "Đã xác nhận", "Chờ check-in", "Đã hủy", "Walk-in"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBooking = new JTable(tableModel);
        tblBooking.setFont(BODY_FONT);
        tblBooking.setRowHeight(30);
        tblBooking.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBooking.setGridColor(BORDER_SOFT);
        tblBooking.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblBooking);

        JScrollPane scrollPane = new JScrollPane(tblBooking);
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
                "Enter Xem chi tiết"
        );
    }

    private void loadBookingData(boolean showMessage) {
        List<BookingReportRecord> filtered = filterRecords();
        updateSummary(filtered);
        updateStatusPanel(filtered);
        reloadTable(filtered);
        chartPanel.setRecords(filtered);
        if (showMessage) {
            showInfo("Đã cập nhật báo cáo đặt phòng.");
        }
    }

    private List<BookingReportRecord> filterRecords() {
        List<BookingReportRecord> filtered = new ArrayList<BookingReportRecord>();
        String keyword = txtTuKhoa == null ? "" : txtTuKhoa.getText().trim().toLowerCase();
        for (BookingReportRecord record : bookingRecords) {
            if (!keyword.isEmpty() && !record.label.toLowerCase().contains(keyword)) {
                continue;
            }
            filtered.add(record);
        }
        return filtered;
    }

    private void updateSummary(List<BookingReportRecord> records) {
        int tongBooking = 0;
        int daXacNhan = 0;
        int choCheckIn = 0;
        int daHuy = 0;

        for (BookingReportRecord record : records) {
            tongBooking += record.tongBooking;
            daXacNhan += record.daXacNhan;
            choCheckIn += record.choCheckIn;
            daHuy += record.daHuy;
        }

        lblTongBooking.setText(String.valueOf(tongBooking));
        lblDaXacNhan.setText(String.valueOf(daXacNhan));
        lblChoCheckIn.setText(String.valueOf(choCheckIn));
        lblDaHuy.setText(String.valueOf(daHuy));

        lblTongBookingSub.setText("Trung bình/ngày: " + (records.isEmpty() ? 0 : tongBooking / records.size()));
        lblDaXacNhanSub.setText("Tỷ lệ: " + percentText(daXacNhan, tongBooking));
        lblChoCheckInSub.setText("Tỷ lệ: " + percentText(choCheckIn, tongBooking));
        lblDaHuySub.setText("Tỷ lệ: " + percentText(daHuy, tongBooking));
    }

    private void updateStatusPanel(List<BookingReportRecord> records) {
        int daXacNhan = 0;
        int choCheckIn = 0;
        int daHuy = 0;
        int walkIn = 0;
        int tongBooking = 0;

        for (BookingReportRecord record : records) {
            tongBooking += record.tongBooking;
            daXacNhan += record.daXacNhan;
            choCheckIn += record.choCheckIn;
            daHuy += record.daHuy;
            walkIn += record.walkIn;
        }

        lblTrangThaiXacNhan.setText(daXacNhan + " booking  |  " + percentText(daXacNhan, tongBooking));
        lblTrangThaiChoCheckIn.setText(choCheckIn + " booking  |  " + percentText(choCheckIn, tongBooking));
        lblTrangThaiDaHuy.setText(daHuy + " booking  |  " + percentText(daHuy, tongBooking));
        lblTrangThaiWalkIn.setText(walkIn + " booking  |  " + percentText(walkIn, tongBooking));
    }

    private void reloadTable(List<BookingReportRecord> records) {
        tableModel.setRowCount(0);
        for (BookingReportRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.label,
                    record.tongBooking,
                    record.daXacNhan,
                    record.choCheckIn,
                    record.daHuy,
                    record.walkIn
            });
        }
        if (!records.isEmpty()) {
            tblBooking.setRowSelectionInterval(0, 0);
        }
    }

    private void resetFilters() {
        cboCheDoLoc.setSelectedIndex(0);
        cboThang.setSelectedIndex(3);
        cboNam.setSelectedIndex(0);
        txtTuNgay.setText("01/04/2026");
        txtDenNgay.setText("30/04/2026");
        txtTuKhoa.setText("");
        loadBookingData(false);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "bao_cao_dat_phong_view", new Runnable() {
            @Override
            public void run() {
                loadBookingData(true);
            }
        });
        ScreenUIHelper.registerShortcut(this, "F2", "bao_cao_dat_phong_export", new Runnable() {
            @Override
            public void run() {
                showInfo("Đã sẵn sàng xuất báo cáo đặt phòng.");
            }
        });
        ScreenUIHelper.registerShortcut(this, "F5", "bao_cao_dat_phong_refresh", new Runnable() {
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

    private JPanel createStatusRow(String title, JLabel valueLabel, Color accent) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setBackground(PANEL_SOFT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        head.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBackground(accent);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY);

        head.add(dot);
        head.add(Box.createHorizontalStrut(8));
        head.add(lblTitle);

        row.add(head, BorderLayout.NORTH);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JLabel createStatusValueLabel() {
        JLabel label = new JLabel();
        label.setFont(BODY_FONT);
        label.setForeground(TEXT_MUTED);
        return label;
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
        JOptionPane.showMessageDialog(this, message, "Báo cáo đặt phòng", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<BookingReportRecord> createSampleData() {
        List<BookingReportRecord> data = new ArrayList<BookingReportRecord>();
        data.add(new BookingReportRecord("01/04/2026", 36, 29, 5, 2, 7));
        data.add(new BookingReportRecord("02/04/2026", 42, 34, 6, 2, 9));
        data.add(new BookingReportRecord("03/04/2026", 38, 31, 5, 2, 8));
        data.add(new BookingReportRecord("04/04/2026", 51, 42, 6, 3, 11));
        data.add(new BookingReportRecord("05/04/2026", 56, 45, 7, 4, 12));
        data.add(new BookingReportRecord("06/04/2026", 48, 38, 7, 3, 10));
        data.add(new BookingReportRecord("07/04/2026", 60, 49, 8, 3, 14));
        return data;
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    private static final class BookingReportRecord {
        private final String label;
        private final int tongBooking;
        private final int daXacNhan;
        private final int choCheckIn;
        private final int daHuy;
        private final int walkIn;

        private BookingReportRecord(String label, int tongBooking, int daXacNhan, int choCheckIn, int daHuy, int walkIn) {
            this.label = label;
            this.tongBooking = tongBooking;
            this.daXacNhan = daXacNhan;
            this.choCheckIn = choCheckIn;
            this.daHuy = daHuy;
            this.walkIn = walkIn;
        }
    }

    private final class BookingChartPanel extends JPanel {
        private List<BookingReportRecord> records = new ArrayList<BookingReportRecord>();

        private BookingChartPanel() {
            setOpaque(true);
            setBackground(PANEL_SOFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }

        private void setRecords(List<BookingReportRecord> records) {
            this.records = new ArrayList<BookingReportRecord>(records);
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

                int maxValue = 0;
                for (BookingReportRecord record : records) {
                    maxValue = Math.max(maxValue, record.tongBooking);
                }
                if (maxValue <= 0) {
                    drawEmptyState(g2, width, height);
                    return;
                }

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
                    String text = String.valueOf(axisValue);
                    int textWidth = g2.getFontMetrics().stringWidth(text);
                    g2.drawString(text, left - textWidth - 8, y + 4);
                }

                int step = chartWidth / records.size();
                int barWidth = Math.max(24, step / 2);

                for (int i = 0; i < records.size(); i++) {
                    BookingReportRecord record = records.get(i);
                    int centerX = left + step * i + step / 2;
                    int barHeight = (int) Math.round((double) record.tongBooking * chartHeight / (double) maxValue);
                    int barX = centerX - barWidth / 2;
                    int barY = top + chartHeight - barHeight;

                    g2.setColor(new Color(191, 219, 254));
                    g2.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
                    g2.setColor(BRAND_BLUE);
                    g2.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);

                    String value = String.valueOf(record.tongBooking);
                    int valueWidth = g2.getFontMetrics().stringWidth(value);
                    g2.setColor(TEXT_PRIMARY);
                    g2.drawString(value, centerX - valueWidth / 2, barY - 6);

                    String label = record.label.substring(0, 5);
                    int labelWidth = g2.getFontMetrics().stringWidth(label);
                    g2.setColor(TEXT_MUTED);
                    g2.drawString(label, centerX - labelWidth / 2, top + chartHeight + 18);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawEmptyState(Graphics2D g2, int width, int height) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(BODY_FONT);
            String message = "Chưa có dữ liệu booking để hiển thị.";
            int textWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - textWidth) / 2, height / 2);
        }
    }
}
