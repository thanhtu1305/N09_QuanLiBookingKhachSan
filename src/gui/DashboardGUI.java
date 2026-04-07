package gui;

import dao.DashboardDAO;
import entity.DashboardChartPoint;
import entity.DashboardSummary;
import entity.DashboardTaskRow;
import gui.common.AppBranding;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardGUI extends JFrame {
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
    private static final Color BRAND_INDIGO = new Color(99, 102, 241);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font VALUE_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font METRIC_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font CHART_VALUE_FONT = new Font("Segoe UI", Font.BOLD, 11);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String username;
    private final String role;
    private final DashboardDAO dashboardDAO;

    private JPanel rootPanel;
    private JLabel lblNgayLamViecValue;
    private JLabel lblLanCapNhatValue;

    private MetricCard cardPhongHoatDong;
    private MetricCard cardPhongDangO;
    private MetricCard cardPhongDaDat;
    private MetricCard cardPhongBaoTri;
    private MetricCard cardBookingHomNay;
    private MetricCard cardChoCheckin;
    private MetricCard cardCheckoutHomNay;
    private MetricCard cardChoThanhToan;
    private MetricCard cardDoanhThuHomNay;
    private MetricCard cardDoanhThuThang;

    private DashboardBarChartPanel revenueChartPanel;
    private DashboardBarChartPanel bookingChartPanel;

    private JTable tblCongViec;
    private DefaultTableModel taskTableModel;
    private List<DashboardTaskRow> currentTaskRows = new ArrayList<DashboardTaskRow>();
    private JLabel lblChiTietMa;
    private JLabel lblChiTietLoai;
    private JLabel lblChiTietDoiTuong;
    private JLabel lblChiTietThoiGian;
    private JLabel lblChiTietTrangThai;
    private JLabel lblChiTietHuongXuLy;

    public DashboardGUI() {
        this("guest", "Lễ tân");
    }

    public DashboardGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");
        this.dashboardDAO = new DashboardDAO();

        setTitle("Dashboard - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        loadDashboardData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.DASHBOARD, username, role), BorderLayout.WEST);
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
        top.add(buildInfoRow());

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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("DASHBOARD TỔNG QUAN"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi vận hành khách sạn từ dữ liệu thực: phòng, booking, check-in/out và thanh toán.");
        lblSub.setFont(SUBTITLE_FONT);
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Dashboard"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Đặt phòng", BRAND_GREEN, Color.WHITE, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.DAT_PHONG, username, role)));
        card.add(createPrimaryButton("Check-in", BRAND_BLUE, Color.WHITE, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role)));
        card.add(createPrimaryButton("Check-out", BRAND_AMBER, TEXT_PRIMARY, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role)));
        card.add(createPrimaryButton("Thanh toán", BRAND_RED, Color.WHITE, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.THANH_TOAN, username, role)));
        card.add(createPrimaryButton("Làm mới", new Color(15, 118, 110), Color.WHITE, e ->
                loadDashboardData(true)));
        return card;
    }

    private JPanel buildInfoRow() {
        JPanel card = createInfoCardPanel(new GridLayout(1, 4, 12, 0));

        lblNgayLamViecValue = new JLabel();
        lblLanCapNhatValue = new JLabel();

        card.add(createInfoCell("Ngày làm việc", lblNgayLamViecValue));
        card.add(createInfoCell("Lần cập nhật", lblLanCapNhatValue));
        card.add(createInfoCell("Người dùng", createStaticValueLabel(username)));
        card.add(createInfoCell("Vai trò", createStaticValueLabel(role)));
        return card;
    }

    private JPanel createInfoCell(String label, JLabel valueLabel) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(LABEL_FONT);
        lblLabel.setForeground(TEXT_MUTED);

        valueLabel.setFont(VALUE_FONT);
        valueLabel.setForeground(TEXT_PRIMARY);

        cell.add(lblLabel);
        cell.add(Box.createVerticalStrut(4));
        cell.add(valueLabel);
        return cell;
    }

    private JLabel createStaticValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(VALUE_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JScrollPane buildCenterContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel kpiSection = buildKpiSection();
        kpiSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, kpiSection.getPreferredSize().height));

        JSplitPane chartSplitPane = buildChartsSplitPane();
        chartSplitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        chartSplitPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        JPanel taskSection = buildTaskSection();
        taskSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(kpiSection);
        content.add(Box.createVerticalStrut(12));
        content.add(chartSplitPane);
        content.add(Box.createVerticalStrut(12));
        content.add(taskSection);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel buildKpiSection() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Chỉ số vận hành nhanh");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Tổng hợp KPI thực từ database QLKS theo ngày làm việc hiện tại.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(2, 5, 10, 10));
        grid.setOpaque(false);

        cardPhongHoatDong = createMetricCard("Phòng hoạt động", new Color(219, 234, 254));
        cardPhongDangO = createMetricCard("Phòng đang ở", new Color(220, 252, 231));
        cardPhongDaDat = createMetricCard("Phòng đã đặt", new Color(254, 249, 195));
        cardPhongBaoTri = createMetricCard("Phòng bảo trì", new Color(254, 226, 226));
        cardBookingHomNay = createMetricCard("Booking hôm nay", new Color(238, 242, 255));
        cardChoCheckin = createMetricCard("Chờ check-in", new Color(224, 242, 254));
        cardCheckoutHomNay = createMetricCard("Checkout hôm nay", new Color(255, 237, 213));
        cardChoThanhToan = createMetricCard("Hóa đơn chờ thanh toán", new Color(254, 226, 226));
        cardDoanhThuHomNay = createMetricCard("Doanh thu hôm nay", new Color(236, 253, 245));
        cardDoanhThuThang = createMetricCard("Doanh thu tháng", new Color(224, 231, 255));

        grid.add(cardPhongHoatDong.panel);
        grid.add(cardPhongDangO.panel);
        grid.add(cardPhongDaDat.panel);
        grid.add(cardPhongBaoTri.panel);
        grid.add(cardBookingHomNay.panel);
        grid.add(cardChoCheckin.panel);
        grid.add(cardCheckoutHomNay.panel);
        grid.add(cardChoThanhToan.panel);
        grid.add(cardDoanhThuHomNay.panel);
        grid.add(cardDoanhThuThang.panel);

        card.add(titleRow, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JSplitPane buildChartsSplitPane() {
        revenueChartPanel = new DashboardBarChartPanel(true, BRAND_BLUE, "Doanh thu 7 ngày gần nhất");
        bookingChartPanel = new DashboardBarChartPanel(false, BRAND_INDIGO, "Booking 7 ngày gần nhất");

        JPanel revenueCard = createChartCard(
                "Doanh thu 7 ngày gần nhất",
                "Tổng tiền thu được từ bảng ThanhToán theo ngày.",
                revenueChartPanel
        );
        JPanel bookingCard = createChartCard(
                "Booking 7 ngày gần nhất",
                "Số booking tạo mới theo ngày từ bảng ĐặtPhòng.",
                bookingChartPanel
        );

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, revenueCard, bookingCard);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel createChartCard(String title, String subtitle, DashboardBarChartPanel chart) {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        chart.setPreferredSize(new Dimension(0, 280));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTaskSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        JPanel tableCard = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Công việc cần xử lý");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Booking chờ check-in, khách sắp checkout và hóa đơn chờ thanh toán.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        taskTableModel = new DefaultTableModel(
                new Object[]{"Mã", "Đối tượng", "Thời gian", "Trạng thái"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblCongViec = new JTable(taskTableModel);
        tblCongViec.setFont(BODY_FONT);
        tblCongViec.setRowHeight(32);
        tblCongViec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCongViec.setGridColor(BORDER_SOFT);
        tblCongViec.setShowGrid(true);
        tblCongViec.setFillsViewportHeight(true);
        tblCongViec.setToolTipText("Danh sách công việc cần xử lý trong ngày.");
        ScreenUIHelper.styleTableHeader(tblCongViec);

        tblCongViec.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateTaskDetail(tblCongViec.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblCongViec);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        tableCard.add(titleRow, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel detailCard = createCardPanel(new BorderLayout(0, 10));
        JLabel lblDetailTitle = new JLabel("Chi tiết xử lý");
        lblDetailTitle.setFont(SECTION_FONT);
        lblDetailTitle.setForeground(TEXT_PRIMARY);

        JPanel detailBody = new JPanel();
        detailBody.setOpaque(false);
        detailBody.setLayout(new BoxLayout(detailBody, BoxLayout.Y_AXIS));

        lblChiTietMa = createValueLabel();
        lblChiTietLoai = createValueLabel();
        lblChiTietDoiTuong = createValueLabel();
        lblChiTietThoiGian = createValueLabel();
        lblChiTietTrangThai = createValueLabel();
        lblChiTietHuongXuLy = createValueLabel();

        addDetailRow(detailBody, "Mã nghiệp vụ", lblChiTietMa);
        addDetailRow(detailBody, "Loại công việc", lblChiTietLoai);
        addDetailRow(detailBody, "Đối tượng", lblChiTietDoiTuong);
        addDetailRow(detailBody, "Thời gian", lblChiTietThoiGian);
        addDetailRow(detailBody, "Trạng thái", lblChiTietTrangThai);
        addDetailRow(detailBody, "Hướng xử lý", lblChiTietHuongXuLy);

        detailCard.add(lblDetailTitle, BorderLayout.NORTH);
        detailCard.add(detailBody, BorderLayout.CENTER);

        wrapper.add(tableCard, BorderLayout.CENTER);
        wrapper.add(detailCard, BorderLayout.SOUTH);
        return wrapper;
    }

    private void loadDashboardData(boolean showMessage) {
        DashboardSummary summary = dashboardDAO.getDashboardSummary();
        String errorMessage = safeValue(dashboardDAO.getLastErrorMessage(), "");

        List<DashboardChartPoint> revenuePoints = dashboardDAO.getRevenueLast7Days();
        errorMessage = mergeErrors(errorMessage, dashboardDAO.getLastErrorMessage());

        List<DashboardChartPoint> bookingPoints = dashboardDAO.getBookingLast7Days();
        errorMessage = mergeErrors(errorMessage, dashboardDAO.getLastErrorMessage());

        List<DashboardTaskRow> taskRows = dashboardDAO.getTodayTasks();
        errorMessage = mergeErrors(errorMessage, dashboardDAO.getLastErrorMessage());

        loadSummaryCards(summary);
        loadCharts(revenuePoints, bookingPoints);
        loadTaskTable(taskRows);
        updateInfoRow();

        if (showMessage) {
            if (!errorMessage.isEmpty()) {
                showMessage("Đã tải dashboard với fallback an toàn. Chi tiết: " + errorMessage);
            } else {
                showMessage("Đã cập nhật dashboard.");
            }
        }
    }

    private void loadSummaryCards(DashboardSummary summary) {
        cardPhongHoatDong.setValue(formatCount(summary.getActiveRooms()));
        cardPhongHoatDong.setNote("Phòng trống + phòng sẵn sàng khai thác");

        cardPhongDangO.setValue(formatCount(summary.getOccupiedRooms()));
        cardPhongDangO.setNote("Trạng thái phòng đang ở");

        cardPhongDaDat.setValue(formatCount(summary.getBookedRooms()));
        cardPhongDaDat.setNote("Phòng khóa cho booking đã xác nhận");

        cardPhongBaoTri.setValue(formatCount(summary.getMaintenanceRooms()));
        cardPhongBaoTri.setNote("Cần kỹ thuật hoặc tạm ngưng khai thác");

        cardBookingHomNay.setValue(formatCount(summary.getTodayBookings()));
        cardBookingHomNay.setNote("Số booking tạo trong ngày");

        cardChoCheckin.setValue(formatCount(summary.getPendingCheckinToday()));
        cardChoCheckin.setNote("Nhận phòng trong hôm nay");

        cardCheckoutHomNay.setValue(formatCount(summary.getCheckoutDueTodayCount()));
        cardCheckoutHomNay.setNote("Lượt lưu trú đến hạn checkout");

        cardChoThanhToan.setValue(formatCount(summary.getPendingPaymentCount()));
        cardChoThanhToan.setNote("Hóa đơn còn trạng thái chờ thanh toán");

        cardDoanhThuHomNay.setValue(formatMoney(summary.getRevenueToday()));
        cardDoanhThuHomNay.setNote("Tiền thu được trong ngày");

        cardDoanhThuThang.setValue(formatMoney(summary.getRevenueThisMonth()));
        cardDoanhThuThang.setNote("Tiền thu từ ngày 01 đến hiện tại");
    }

    private void loadCharts(List<DashboardChartPoint> revenuePoints, List<DashboardChartPoint> bookingPoints) {
        revenueChartPanel.setPoints(revenuePoints);
        bookingChartPanel.setPoints(bookingPoints);
    }

    private void loadTaskTable(List<DashboardTaskRow> rows) {
        currentTaskRows = rows == null ? new ArrayList<DashboardTaskRow>() : new ArrayList<DashboardTaskRow>(rows);
        taskTableModel.setRowCount(0);

        for (DashboardTaskRow row : currentTaskRows) {
            taskTableModel.addRow(new Object[]{
                    row.getTaskCode(),
                    row.getTarget(),
                    row.getTimeText(),
                    row.getStatus()
            });
        }

        if (!currentTaskRows.isEmpty()) {
            tblCongViec.setRowSelectionInterval(0, 0);
            updateTaskDetail(0);
        } else {
            resetTaskDetail();
        }
    }

    private void updateInfoRow() {
        lblNgayLamViecValue.setText(LocalDate.now().format(DATE_LABEL_FORMAT));
        lblLanCapNhatValue.setText(LocalDateTime.now().format(DATETIME_LABEL_FORMAT));
    }

    private void updateTaskDetail(int row) {
        if (row < 0 || row >= currentTaskRows.size()) {
            resetTaskDetail();
            return;
        }

        DashboardTaskRow taskRow = currentTaskRows.get(row);
        lblChiTietMa.setText(safeValue(taskRow.getTaskCode(), "-"));
        lblChiTietLoai.setText(safeValue(taskRow.getTaskType(), "-"));
        lblChiTietDoiTuong.setText(safeValue(taskRow.getTarget(), "-"));
        lblChiTietThoiGian.setText(safeValue(taskRow.getTimeText(), "-"));
        lblChiTietTrangThai.setText(safeValue(taskRow.getStatus(), "-"));
        lblChiTietHuongXuLy.setText(safeValue(taskRow.getActionHint(), "-"));
    }

    private void resetTaskDetail() {
        lblChiTietMa.setText("-");
        lblChiTietLoai.setText("-");
        lblChiTietDoiTuong.setText("-");
        lblChiTietThoiGian.setText("-");
        lblChiTietTrangThai.setText("-");
        lblChiTietHuongXuLy.setText("-");
    }

    private void openSelectedTask() {
        int row = tblCongViec == null ? -1 : tblCongViec.getSelectedRow();
        if (row < 0 || row >= currentTaskRows.size()) {
            showMessage("Vui lòng chọn một công việc trong danh sách.");
            return;
        }

        DashboardTaskRow taskRow = currentTaskRows.get(row);
        String taskType = safeValue(taskRow.getTaskType(), "");
        if ("PAYMENT".equalsIgnoreCase(taskType)) {
            NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.THANH_TOAN, username, role);
            return;
        }
        if ("CHECKIN".equalsIgnoreCase(taskType) || "CHECKOUT".equalsIgnoreCase(taskType)) {
            NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role);
            return;
        }
        NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.DAT_PHONG, username, role);
    }

    private void addDetailRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(120, 20));

        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(VALUE_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private MetricCard createMetricCard(String title, Color badgeBg) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(PANEL_SOFT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblLabel = new JLabel(title);
        lblLabel.setFont(LABEL_FONT);
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel("0");
        lblValue.setFont(METRIC_VALUE_FONT);
        lblValue.setForeground(TEXT_PRIMARY);

        JLabel lblNote = new JLabel("-");
        lblNote.setFont(BODY_FONT);
        lblNote.setForeground(TEXT_PRIMARY);
        lblNote.setOpaque(true);
        lblNote.setBackground(badgeBg);
        lblNote.setBorder(new EmptyBorder(4, 8, 4, 8));

        card.add(lblLabel, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblNote, BorderLayout.SOUTH);
        return new MetricCard(card, lblValue, lblNote);
    }

    private JButton createPrimaryButton(String text, Color background, Color foreground,
                                        java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
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

    private JPanel createInfoCardPanel(GridLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return panel;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Đặt phòng",
                "F2 Check-in",
                "F3 Check-out",
                "F4 Thanh toán",
                "F5 Làm mới",
                "Enter Mở công việc"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "dashboard-f1", () ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.DAT_PHONG, username, role));
        ScreenUIHelper.registerShortcut(this, "F2", "dashboard-f2", () ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role));
        ScreenUIHelper.registerShortcut(this, "F3", "dashboard-f3", () ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role));
        ScreenUIHelper.registerShortcut(this, "F4", "dashboard-f4", () ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.THANH_TOAN, username, role));
        ScreenUIHelper.registerShortcut(this, "F5", "dashboard-f5", () ->
                loadDashboardData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "dashboard-enter", this::openSelectedTask);
    }

    private String formatCount(int value) {
        return String.valueOf(value);
    }

    private String formatMoney(double value) {
        return MONEY_FORMAT.format(value) + " đ";
    }

    private String mergeErrors(String current, String next) {
        String left = safeValue(current, "");
        String right = safeValue(next, "");
        if (right.isEmpty()) {
            return left;
        }
        if (left.isEmpty()) {
            return right;
        }
        if (left.contains(right)) {
            return left;
        }
        return left + " | " + right;
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Tổng quan", JOptionPane.INFORMATION_MESSAGE);
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
            loadDashboardData(false);
        }
        return rootPanel;
    }

    private static final class MetricCard {
        private final JPanel panel;
        private final JLabel valueLabel;
        private final JLabel noteLabel;

        private MetricCard(JPanel panel, JLabel valueLabel, JLabel noteLabel) {
            this.panel = panel;
            this.valueLabel = valueLabel;
            this.noteLabel = noteLabel;
        }

        private void setValue(String value) {
            valueLabel.setText(value);
        }

        private void setNote(String note) {
            noteLabel.setText(note);
        }
    }

    private final class DashboardBarChartPanel extends JPanel {
        private final boolean moneyMode;
        private final Color accentColor;
        private final String emptyMessage;
        private List<DashboardChartPoint> points = new ArrayList<DashboardChartPoint>();

        private DashboardBarChartPanel(boolean moneyMode, Color accentColor, String emptyMessage) {
            this.moneyMode = moneyMode;
            this.accentColor = accentColor;
            this.emptyMessage = emptyMessage;
            setOpaque(true);
            setBackground(PANEL_SOFT);
            setToolTipText(emptyMessage);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }

        private void setPoints(List<DashboardChartPoint> points) {
            this.points = points == null ? new ArrayList<DashboardChartPoint>() : new ArrayList<DashboardChartPoint>(points);
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
                int left = 52;
                int right = 20;
                int top = 18;
                int bottom = 42;
                int chartWidth = width - left - right;
                int chartHeight = height - top - bottom;

                if (chartWidth <= 0 || chartHeight <= 0 || points.isEmpty()) {
                    drawEmptyState(g2, width, height);
                    return;
                }

                double maxValue = 0d;
                for (DashboardChartPoint point : points) {
                    maxValue = Math.max(maxValue, point.getValue());
                }
                if (maxValue <= 0d) {
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
                    double axisValue = maxValue - (maxValue * i / 4d);
                    int y = top + i * chartHeight / 4;
                    String text = formatAxisValue(axisValue);
                    int textWidth = g2.getFontMetrics().stringWidth(text);
                    g2.drawString(text, left - textWidth - 8, y + 4);
                }

                int step = Math.max(1, chartWidth / Math.max(1, points.size()));
                int barWidth = Math.max(22, step / 2);

                for (int i = 0; i < points.size(); i++) {
                    DashboardChartPoint point = points.get(i);
                    int centerX = left + step * i + step / 2;
                    int barHeight = (int) Math.round(point.getValue() * chartHeight / maxValue);
                    int barX = centerX - barWidth / 2;
                    int barY = top + chartHeight - barHeight;

                    g2.setColor(new Color(
                            Math.min(255, accentColor.getRed() + 130),
                            Math.min(255, accentColor.getGreen() + 120),
                            Math.min(255, accentColor.getBlue() + 110)
                    ));
                    g2.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
                    g2.setColor(accentColor);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);

                    String valueText = moneyMode ? shortMoney(point.getValue()) : String.valueOf((int) Math.round(point.getValue()));
                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(CHART_VALUE_FONT);
                    int valueWidth = g2.getFontMetrics().stringWidth(valueText);
                    g2.drawString(valueText, centerX - valueWidth / 2, Math.max(top + 12, barY - 6));

                    g2.setFont(LABEL_FONT);
                    String label = point.getLabel();
                    int labelWidth = g2.getFontMetrics().stringWidth(label);
                    g2.drawString(label, centerX - labelWidth / 2, top + chartHeight + 16);
                }
            } finally {
                g2.dispose();
            }
        }

        private String formatAxisValue(double value) {
            if (moneyMode) {
                if (value >= 1000000d) {
                    return String.format(Locale.US, "%.1ftr", value / 1000000d);
                }
                if (value >= 1000d) {
                    return String.format(Locale.US, "%.0fk", value / 1000d);
                }
                return String.format(Locale.US, "%.0f", value);
            }
            return String.valueOf((int) Math.round(value));
        }

        private String shortMoney(double value) {
            if (value >= 1000000d) {
                return String.format(Locale.US, "%.1ftr", value / 1000000d);
            }
            if (value >= 1000d) {
                return String.format(Locale.US, "%.0fk", value / 1000d);
            }
            return String.format(Locale.US, "%.0f", value);
        }

        private void drawEmptyState(Graphics2D g2, int width, int height) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(BODY_FONT);
            String message = "Chưa có dữ liệu cho " + emptyMessage.toLowerCase(Locale.ROOT) + ".";
            int textWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - textWidth) / 2, height / 2);
        }
    }
}
