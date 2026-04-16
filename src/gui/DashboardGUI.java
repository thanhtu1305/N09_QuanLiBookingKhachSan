package gui;

import dao.DashboardDAO;
import entity.DashboardChartPoint;
import entity.DashboardGanttCell;
import entity.DashboardGanttRow;
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final Color GANTT_EMPTY_BG = new Color(220, 252, 231);
    private static final Color GANTT_BOOKED_BG = new Color(254, 249, 195);
    private static final Color GANTT_PENDING_CHECKIN_BG = new Color(254, 215, 170);
    private static final Color GANTT_OCCUPIED_BG = new Color(191, 219, 254);
    private static final Color GANTT_MAINTENANCE_BG = new Color(254, 226, 226);
    private static final Color GANTT_EMPTY_FG = new Color(22, 101, 52);
    private static final Color GANTT_BOOKED_FG = new Color(146, 64, 14);
    private static final Color GANTT_PENDING_CHECKIN_FG = new Color(154, 52, 18);
    private static final Color GANTT_OCCUPIED_FG = new Color(30, 64, 175);
    private static final Color GANTT_MAINTENANCE_FG = new Color(153, 27, 27);

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
    private static final DateTimeFormatter GANTT_HEADER_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final int GANTT_DAY_COUNT = 7;
    private static final int GANTT_VISIBLE_ROW_COUNT = 9;

    private final String username;
    private final String role;
    private final DashboardDAO dashboardDAO;
    private final List<DashboardGanttRow> allGanttRows = new ArrayList<DashboardGanttRow>();
    private final List<DashboardGanttRow> filteredGanttRows = new ArrayList<DashboardGanttRow>();
    private final List<LocalDate> ganttDates = new ArrayList<LocalDate>();
    private LocalDate ganttStartDate = LocalDate.now();

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
    private JTable tblGantt;
    private JScrollPane ganttScrollPane;
    private DefaultTableModel ganttTableModel;
    private JComboBox<String> cboGanttTang;
    private JComboBox<String> cboGanttLoaiPhong;
    private JLabel lblGanttRangeValue;
    private JLabel lblGanttPhongValue;
    private JLabel lblGanttNgayValue;
    private JLabel lblGanttTrangThaiValue;
    private JLabel lblGanttKhachValue;
    private JLabel lblGanttThoiGianValue;
    private JLabel lblGanttMaThamChieuValue;
    private JLabel lblGanttHuongXuLyValue;
    private JButton btnGanttPrevRange;
    private JButton btnGanttNextRange;
    private JButton btnGanttRefresh;
    private JButton btnGanttOpenDatPhong;
    private JButton btnGanttOpenCheckInOut;
    private DashboardGanttCell selectedGanttCell;
    private boolean suppressGanttFilterEvents;
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

        JPanel infoSection = buildInfoRow();
        infoSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, infoSection.getPreferredSize().height));

        JPanel kpiSection = buildKpiSection();
        kpiSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, kpiSection.getPreferredSize().height));

        JPanel ganttSection = buildGanttSection();
        ganttSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel taskSection = buildTaskSection();
        taskSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(infoSection);
        content.add(Box.createVerticalStrut(12));
        content.add(kpiSection);
        content.add(Box.createVerticalStrut(12));
        content.add(ganttSection);
        content.add(Box.createVerticalStrut(12));
        content.add(taskSection);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel buildGanttSection() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Sơ đồ Gantt tình trạng phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi lịch sử dụng phòng trong 7 ngày tới.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);

        cboGanttTang = createFilterComboBox(new String[]{"Tất cả"});
        cboGanttLoaiPhong = createFilterComboBox(new String[]{"Tất cả"});
        cboGanttTang.addActionListener(e -> {
            if (!suppressGanttFilterEvents) {
                applyGanttFilters();
            }
        });
        cboGanttLoaiPhong.addActionListener(e -> {
            if (!suppressGanttFilterEvents) {
                applyGanttFilters();
            }
        });

        controls.add(createFieldGroup("Tầng", cboGanttTang));
        controls.add(createFieldGroup("Loại phòng", cboGanttLoaiPhong));
        btnGanttPrevRange = createGanttRangeButton("<", e -> shiftGanttRange(-GANTT_DAY_COUNT));
        btnGanttPrevRange.setToolTipText("Lùi block 7 ngày trước");
        lblGanttRangeValue = createGanttRangeLabel();
        btnGanttNextRange = createGanttRangeButton(">", e -> shiftGanttRange(GANTT_DAY_COUNT));
        btnGanttNextRange.setToolTipText("Tiến sang block 7 ngày kế tiếp");
        btnGanttRefresh = createOutlineButton("Làm mới", BRAND_INDIGO, e -> reloadGanttData(true));
        btnGanttRefresh.setToolTipText("Tải lại trạng thái phòng trong khoảng ngày đang xem");

        controls.add(Box.createHorizontalStrut(8));
        controls.add(btnGanttPrevRange);
        controls.add(lblGanttRangeValue);
        controls.add(btnGanttNextRange);
        controls.add(btnGanttRefresh);

        header.add(titleRow);
        header.add(Box.createVerticalStrut(8));
        header.add(controls);

        ganttTableModel = new DefaultTableModel(new Object[]{"Phòng"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblGantt = new JTable(ganttTableModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent event) {
                int row = rowAtPoint(event.getPoint());
                int column = columnAtPoint(event.getPoint());
                if (row < 0 || column < 0) {
                    return null;
                }
                Object value = getValueAt(row, column);
                if (value instanceof DashboardGanttCell) {
                    return buildGanttTooltip((DashboardGanttCell) value);
                }
                if (value instanceof DashboardGanttRow) {
                    DashboardGanttRow ganttRow = (DashboardGanttRow) value;
                    return "<html>Phòng: " + safeValue(ganttRow.getSoPhong(), "-")
                            + "<br>Loại: " + safeValue(ganttRow.getLoaiPhong(), "-")
                            + "<br>Tầng: " + safeValue(ganttRow.getTang(), "-")
                            + "<br>Trạng thái phòng: " + safeValue(ganttRow.getTrangThaiPhong(), "-")
                            + "</html>";
                }
                return super.getToolTipText(event);
            }
        };
        tblGantt.setFont(BODY_FONT);
        tblGantt.setRowHeight(44);
        tblGantt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblGantt.setRowSelectionAllowed(false);
        tblGantt.setColumnSelectionAllowed(false);
        tblGantt.setCellSelectionEnabled(true);
        tblGantt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblGantt.setGridColor(BORDER_SOFT);
        tblGantt.setShowGrid(true);
        tblGantt.setFillsViewportHeight(true);
        tblGantt.setDefaultRenderer(Object.class, new DashboardGanttCellRenderer());
        ScreenUIHelper.styleTableHeader(tblGantt);
        tblGantt.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblGantt.rowAtPoint(e.getPoint());
                int column = tblGantt.columnAtPoint(e.getPoint());
                if (row < 0 || column <= 0 || row >= filteredGanttRows.size()) {
                    return;
                }
                DashboardGanttRow ganttRow = filteredGanttRows.get(row);
                if (column - 1 >= ganttRow.getCells().size()) {
                    return;
                }
                tblGantt.changeSelection(row, column, false, false);
                selectGanttCell(ganttRow, ganttRow.getCells().get(column - 1));
            }
        });

        ganttScrollPane = new JScrollPane(tblGantt);
        ganttScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        ganttScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        ganttScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScrollPane.setWheelScrollingEnabled(true);
        ganttScrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        ganttScrollPane.getVerticalScrollBar().setUnitIncrement(tblGantt.getRowHeight());
        ganttScrollPane.getVerticalScrollBar().setBlockIncrement(tblGantt.getRowHeight() * 4);
        int ganttViewportHeight = resolveGanttViewportHeightByRows();
        ganttScrollPane.setPreferredSize(new Dimension(0, ganttViewportHeight));
        ganttScrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                scheduleGanttColumnResize();
            }
        });

        JPanel detailCard = createInfoCardPanel(new GridLayout(1, 2, 12, 0));
        lblGanttPhongValue = createValueLabel();
        lblGanttNgayValue = createValueLabel();
        lblGanttTrangThaiValue = createValueLabel();
        lblGanttKhachValue = createValueLabel();
        lblGanttThoiGianValue = createValueLabel();
        lblGanttMaThamChieuValue = createValueLabel();
        lblGanttHuongXuLyValue = createValueLabel();

        JPanel leftDetail = new JPanel();
        leftDetail.setOpaque(false);
        leftDetail.setLayout(new BoxLayout(leftDetail, BoxLayout.Y_AXIS));
        addDetailRow(leftDetail, "Phòng", lblGanttPhongValue);
        addDetailRow(leftDetail, "Ngày", lblGanttNgayValue);
        addDetailRow(leftDetail, "Trạng thái", lblGanttTrangThaiValue);
        addDetailRow(leftDetail, "Khách", lblGanttKhachValue);

        JPanel rightDetail = new JPanel(new BorderLayout(0, 8));
        rightDetail.setOpaque(false);
        JPanel rightDetailBody = new JPanel();
        rightDetailBody.setOpaque(false);
        rightDetailBody.setLayout(new BoxLayout(rightDetailBody, BoxLayout.Y_AXIS));
        addDetailRow(rightDetailBody, "Thời gian", lblGanttThoiGianValue);
        addDetailRow(rightDetailBody, "Tham chiếu", lblGanttMaThamChieuValue);
        addDetailRow(rightDetailBody, "Hướng xử lý", lblGanttHuongXuLyValue);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionRow.setOpaque(false);
        btnGanttOpenDatPhong = createOutlineButton("Mở Đặt phòng", BRAND_GREEN, e -> openSelectedGanttBooking());
        btnGanttOpenCheckInOut = createOutlineButton("Mở Check-in / Check-out", BRAND_BLUE, e -> openSelectedGanttStay());
        actionRow.add(btnGanttOpenDatPhong);
        actionRow.add(btnGanttOpenCheckInOut);

        rightDetail.add(rightDetailBody, BorderLayout.CENTER);
        rightDetail.add(actionRow, BorderLayout.SOUTH);

        detailCard.add(leftDetail);
        detailCard.add(rightDetail);

        card.add(header, BorderLayout.NORTH);
        card.add(ganttScrollPane, BorderLayout.CENTER);
        card.add(detailCard, BorderLayout.SOUTH);

        updateGanttDetail(null, null);
        updateGanttRangeLabel();
        rebuildGanttColumns();
        scheduleGanttColumnResize();
        return card;
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

    private JComboBox<String> createFilterComboBox(String[] values) {
        JComboBox<String> comboBox = new JComboBox<String>(values);
        comboBox.setFont(BODY_FONT);
        comboBox.setPreferredSize(new Dimension(150, 34));
        comboBox.setMaximumSize(new Dimension(180, 34));
        return comboBox;
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

    private JButton createOutlineButton(String text, Color accentColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(accentColor);
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JButton createGanttRangeButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT.deriveFont(Font.BOLD, 16f));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(PANEL_SOFT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(40, 34));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BRAND_BLUE, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JLabel createGanttRangeLabel() {
        JLabel label = new JLabel("-", SwingConstants.CENTER);
        label.setFont(VALUE_FONT);
        label.setForeground(TEXT_PRIMARY);
        label.setOpaque(true);
        label.setBackground(PANEL_SOFT);
        label.setPreferredSize(new Dimension(220, 34));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return label;
    }

    private void loadGanttRows(List<DashboardGanttRow> rows) {
        allGanttRows.clear();
        if (rows != null) {
            allGanttRows.addAll(rows);
        }

        ganttDates.clear();
        if (!allGanttRows.isEmpty() && !allGanttRows.get(0).getCells().isEmpty()) {
            for (DashboardGanttCell cell : allGanttRows.get(0).getCells()) {
                ganttDates.add(cell.getDate());
            }
        } else {
            LocalDate startDate = safeGanttStartDate();
            for (int i = 0; i < GANTT_DAY_COUNT; i++) {
                ganttDates.add(startDate.plusDays(i));
            }
        }

        updateGanttRangeLabel();
        refreshGanttFilterOptions();
        applyGanttFilters();
    }

    private void updateGanttRangeLabel() {
        if (lblGanttRangeValue == null) {
            return;
        }
        LocalDate rangeStart = ganttDates.isEmpty() ? safeGanttStartDate() : ganttDates.get(0);
        LocalDate rangeEnd = ganttDates.isEmpty()
                ? rangeStart.plusDays(GANTT_DAY_COUNT - 1L)
                : ganttDates.get(ganttDates.size() - 1);
        String text = rangeStart.format(DATE_LABEL_FORMAT) + " - " + rangeEnd.format(DATE_LABEL_FORMAT);
        lblGanttRangeValue.setText(text);
        lblGanttRangeValue.setToolTipText("Khoảng ngày đang xem: " + text);
        lblGanttRangeValue.revalidate();
        lblGanttRangeValue.repaint();
    }

    private LocalDate safeGanttStartDate() {
        return ganttStartDate == null ? LocalDate.now() : ganttStartDate;
    }

    private void shiftGanttRange(int dayOffset) {
        if (dayOffset == 0) {
            return;
        }
        ganttStartDate = safeGanttStartDate().plusDays(dayOffset);
        reloadGanttData(false);
    }

    private String reloadGanttData(boolean notifyUser) {
        List<DashboardGanttRow> ganttRows = dashboardDAO.getRoomGanttRows(safeGanttStartDate(), GANTT_DAY_COUNT);
        String errorMessage = safeValue(dashboardDAO.getLastErrorMessage(), "");
        loadGanttRows(ganttRows);
        updateInfoRow();

        if (notifyUser) {
            if (!errorMessage.isEmpty()) {
                showMessage("Đã tải lại sơ đồ Gantt với fallback an toàn. Chi tiết: " + errorMessage);
            } else {
                showMessage("Đã cập nhật sơ đồ Gantt.");
            }
        }
        return errorMessage;
    }

    private void refreshGanttFilterOptions() {
        if (cboGanttTang == null || cboGanttLoaiPhong == null) {
            return;
        }
        String selectedTang = valueOf(cboGanttTang.getSelectedItem());
        String selectedLoaiPhong = valueOf(cboGanttLoaiPhong.getSelectedItem());

        suppressGanttFilterEvents = true;
        try {
            cboGanttTang.removeAllItems();
            cboGanttTang.addItem("Tất cả");
            List<String> floors = new ArrayList<String>();
            for (DashboardGanttRow row : allGanttRows) {
                String tang = safeValue(row.getTang(), "-");
                if (!floors.contains(tang)) {
                    floors.add(tang);
                }
            }
            floors.sort((left, right) -> left.compareToIgnoreCase(right));
            for (String floor : floors) {
                cboGanttTang.addItem(floor);
            }

            cboGanttLoaiPhong.removeAllItems();
            cboGanttLoaiPhong.addItem("Tất cả");
            List<String> roomTypes = new ArrayList<String>();
            for (DashboardGanttRow row : allGanttRows) {
                String loaiPhong = safeValue(row.getLoaiPhong(), "-");
                if (!roomTypes.contains(loaiPhong)) {
                    roomTypes.add(loaiPhong);
                }
            }
            roomTypes.sort((left, right) -> left.compareToIgnoreCase(right));
            for (String roomType : roomTypes) {
                cboGanttLoaiPhong.addItem(roomType);
            }

            restoreComboSelection(cboGanttTang, selectedTang);
            restoreComboSelection(cboGanttLoaiPhong, selectedLoaiPhong);
        } finally {
            suppressGanttFilterEvents = false;
        }
    }

    private void restoreComboSelection(JComboBox<String> comboBox, String preferredValue) {
        if (comboBox == null) {
            return;
        }
        String normalized = safeValue(preferredValue, "Tất cả");
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (normalized.equals(comboBox.getItemAt(i))) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    private void applyGanttFilters() {
        filteredGanttRows.clear();
        String tang = valueOf(cboGanttTang == null ? null : cboGanttTang.getSelectedItem());
        String loaiPhong = valueOf(cboGanttLoaiPhong == null ? null : cboGanttLoaiPhong.getSelectedItem());

        for (DashboardGanttRow row : allGanttRows) {
            if (!"Tất cả".equals(tang) && !safeValue(row.getTang(), "-").equalsIgnoreCase(tang)) {
                continue;
            }
            if (!"Tất cả".equals(loaiPhong) && !safeValue(row.getLoaiPhong(), "-").equalsIgnoreCase(loaiPhong)) {
                continue;
            }
            filteredGanttRows.add(row);
        }
        refillGanttTable();
    }

    private void rebuildGanttColumns() {
        if (ganttTableModel == null) {
            return;
        }
        Object[] columns = new Object[1 + ganttDates.size()];
        columns[0] = "Phòng";
        for (int i = 0; i < ganttDates.size(); i++) {
            columns[i + 1] = ganttDates.get(i).format(GANTT_HEADER_FORMAT);
        }
        ganttTableModel.setColumnIdentifiers(columns);
        configureGanttColumnWidths();
    }

    private void configureGanttColumnWidths() {
        if (tblGantt == null || tblGantt.getColumnModel().getColumnCount() == 0) {
            return;
        }
        int columnCount = tblGantt.getColumnModel().getColumnCount();
        int dayColumnCount = Math.max(0, columnCount - 1);
        int minRoomWidth = 140;
        int minDayWidth = 90;
        int viewportWidth = resolveGanttViewportWidth();
        int minimumTableWidth = minRoomWidth + (dayColumnCount * minDayWidth);
        int targetTableWidth = Math.max(minimumTableWidth, viewportWidth);

        int roomWidth = dayColumnCount == 0
                ? targetTableWidth
                : Math.max(minRoomWidth, Math.min(190, (int) Math.round(targetTableWidth * 0.18d)));
        int remainingWidth = Math.max(0, targetTableWidth - roomWidth);
        int baseDayWidth = dayColumnCount == 0 ? 0 : Math.max(minDayWidth, remainingWidth / dayColumnCount);
        int extraPixels = dayColumnCount == 0 ? 0 : Math.max(0, remainingWidth - (baseDayWidth * dayColumnCount));

        tblGantt.getColumnModel().getColumn(0).setMinWidth(minRoomWidth);
        tblGantt.getColumnModel().getColumn(0).setPreferredWidth(roomWidth);

        for (int i = 1; i < columnCount; i++) {
            int dayWidth = baseDayWidth + (extraPixels > 0 ? 1 : 0);
            if (extraPixels > 0) {
                extraPixels--;
            }
            tblGantt.getColumnModel().getColumn(i).setMinWidth(minDayWidth);
            tblGantt.getColumnModel().getColumn(i).setPreferredWidth(dayWidth);
        }
        tblGantt.revalidate();
        if (tblGantt.getTableHeader() != null) {
            tblGantt.getTableHeader().revalidate();
            tblGantt.getTableHeader().repaint();
        }
    }

    private int resolveGanttViewportHeightByRows() {
        int rowHeight = tblGantt == null ? 44 : tblGantt.getRowHeight();
        int headerHeight = tblGantt != null && tblGantt.getTableHeader() != null
                ? tblGantt.getTableHeader().getPreferredSize().height
                : 28;
        return (rowHeight * GANTT_VISIBLE_ROW_COUNT) + headerHeight + 4;
    }

    private int resolveGanttViewportWidth() {
        if (ganttScrollPane != null && ganttScrollPane.getViewport() != null) {
            int viewportWidth = ganttScrollPane.getViewport().getWidth();
            if (viewportWidth > 0) {
                return viewportWidth;
            }
        }
        if (tblGantt != null && tblGantt.getParent() != null && tblGantt.getParent().getWidth() > 0) {
            return tblGantt.getParent().getWidth();
        }
        return 0;
    }

    private void scheduleGanttColumnResize() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                configureGanttColumnWidths();
            }
        });
    }

    private void refillGanttTable() {
        rebuildGanttColumns();
        ganttTableModel.setRowCount(0);

        for (DashboardGanttRow row : filteredGanttRows) {
            Object[] data = new Object[1 + ganttDates.size()];
            data[0] = row;
            for (int i = 0; i < ganttDates.size() && i < row.getCells().size(); i++) {
                data[i + 1] = row.getCells().get(i);
            }
            ganttTableModel.addRow(data);
        }

        restoreSelectedGanttCell();
        if (filteredGanttRows.isEmpty()) {
            tblGantt.clearSelection();
            updateGanttDetail(null, null);
        }
        scheduleGanttColumnResize();
    }

    private void restoreSelectedGanttCell() {
        if (tblGantt == null || selectedGanttCell == null) {
            if (tblGantt != null) {
                tblGantt.clearSelection();
            }
            return;
        }
        for (int rowIndex = 0; rowIndex < filteredGanttRows.size(); rowIndex++) {
            DashboardGanttRow row = filteredGanttRows.get(rowIndex);
            if (row.getMaPhong() != selectedGanttCell.getMaPhong()) {
                continue;
            }
            for (int i = 0; i < row.getCells().size(); i++) {
                DashboardGanttCell cell = row.getCells().get(i);
                if (cell.getDate() != null && cell.getDate().equals(selectedGanttCell.getDate())) {
                    tblGantt.changeSelection(rowIndex, i + 1, false, false);
                    selectedGanttCell = cell;
                    updateGanttDetail(row, cell);
                    return;
                }
            }
        }
        tblGantt.clearSelection();
        selectedGanttCell = null;
        updateGanttDetail(null, null);
    }

    private void selectGanttCell(DashboardGanttRow row, DashboardGanttCell cell) {
        selectedGanttCell = cell;
        updateGanttDetail(row, cell);
        if (tblGantt != null) {
            tblGantt.repaint();
        }
    }

    private void updateGanttDetail(DashboardGanttRow row, DashboardGanttCell cell) {
        if (lblGanttPhongValue == null) {
            return;
        }
        if (row == null || cell == null) {
            lblGanttPhongValue.setText("-");
            lblGanttNgayValue.setText("-");
            lblGanttTrangThaiValue.setText("-");
            lblGanttKhachValue.setText("-");
            lblGanttThoiGianValue.setText("-");
            lblGanttMaThamChieuValue.setText("-");
            lblGanttHuongXuLyValue.setText("<html>" + (filteredGanttRows.isEmpty()
                    ? "Chưa có dữ liệu lịch phòng trong khoảng thời gian này."
                    : "Chọn một ô phòng-ngày để xem booking hoặc lưu trú liên quan.") + "</html>");
            btnGanttOpenDatPhong.setEnabled(false);
            btnGanttOpenCheckInOut.setEnabled(false);
            return;
        }

        lblGanttPhongValue.setText("<html><b>" + safeValue(row.getSoPhong(), "-") + "</b> - "
                + safeValue(row.getLoaiPhong(), "-") + " - " + safeValue(row.getTang(), "-") + "</html>");
        lblGanttNgayValue.setText(cell.getDate() == null ? "-" : cell.getDate().format(DATE_LABEL_FORMAT));
        lblGanttTrangThaiValue.setText(safeValue(cell.getStatusText(), "-"));
        lblGanttKhachValue.setText(safeValue(cell.getCustomerName(), "-"));
        lblGanttThoiGianValue.setText(cell.getFromDate() == null
                ? "-"
                : "Từ " + cell.getFromDate().format(DATE_LABEL_FORMAT) + " đến "
                + (cell.getToDate() == null ? cell.getFromDate().format(DATE_LABEL_FORMAT) : cell.getToDate().format(DATE_LABEL_FORMAT)));
        lblGanttMaThamChieuValue.setText(buildGanttReferenceDisplayText(cell));
        lblGanttHuongXuLyValue.setText("<html>" + buildGanttActionDisplayHint(cell) + "</html>");
        btnGanttOpenDatPhong.setEnabled("BOOKING".equalsIgnoreCase(cell.getSourceType()));
        btnGanttOpenCheckInOut.setEnabled("STAY".equalsIgnoreCase(cell.getSourceType()));
    }

    private String buildGanttReferenceText(DashboardGanttCell cell) {
        if (cell == null) {
            return "-";
        }
        if ("STAY".equalsIgnoreCase(cell.getSourceType())) {
            return "LT" + cell.getMaLuuTru() + " / DP" + cell.getMaDatPhong();
        }
        String sourceType = safeValue(cell.getSourceType(), "");
        if ("BOOKING".equalsIgnoreCase(sourceType)) {
            if ("C".equalsIgnoreCase(safeValue(cell.getStatusCode(), ""))) {
                return "Phòng đã được giữ và đang chờ check-in. Có thể mở màn Đặt phòng để xử lý booking DP" + cell.getMaDatPhong() + ".";
            }
            return "Booking đã tạo nhưng chưa tới bước check-in. Có thể mở màn Đặt phòng để xem booking DP" + cell.getMaDatPhong() + ".";
        }
        if ("STAY".equalsIgnoreCase(sourceType)) {
            return "Lưu trú đang ở. Có thể mở màn Check-in / Check-out để xử lý hồ sơ DP" + cell.getMaDatPhong() + ".";
        }
        if ("MAINTENANCE".equalsIgnoreCase(sourceType)) {
            return "Phòng đang ở trạng thái " + safeValue(cell.getStatusText(), "Bảo trì") + ". Không có booking/lưu trú khả dụng cho ô này.";
        }
        if ("BOOKING".equalsIgnoreCase(cell.getSourceType())) {
            return "DP" + cell.getMaDatPhong();
        }
        return "-";
    }

    private String buildGanttActionHint(DashboardGanttCell cell) {
        if (cell == null) {
            return "-";
        }
        if ("BOOKING".equalsIgnoreCase(cell.getSourceType())) {
            return "Booking chưa check-in. Có thể mở màn Đặt phòng để xem và xử lý booking DP" + cell.getMaDatPhong() + ".";
        }
        if ("STAY".equalsIgnoreCase(cell.getSourceType())) {
            return "Lưu trú đang ở. Có thể mở màn Check-in / Check-out để xử lý hồ sơ DP" + cell.getMaDatPhong() + ".";
        }
        if ("MAINTENANCE".equalsIgnoreCase(cell.getSourceType())) {
            return "Phòng đang ở trạng thái " + safeValue(cell.getStatusText(), "Bảo trì") + ". Không có booking/lưu trú khả dụng cho ô này.";
        }
        return "Phòng hiện đang trống trong ngày đã chọn.";
    }

    private String buildGanttReferenceDisplayText(DashboardGanttCell cell) {
        if (cell == null) {
            return "-";
        }
        if ("STAY".equalsIgnoreCase(cell.getSourceType())) {
            return "LT" + cell.getMaLuuTru() + " / DP" + cell.getMaDatPhong();
        }
        if ("BOOKING".equalsIgnoreCase(cell.getSourceType())) {
            return "DP" + cell.getMaDatPhong();
        }
        return "-";
    }

    private String buildGanttActionDisplayHint(DashboardGanttCell cell) {
        if (cell == null) {
            return "-";
        }
        String sourceType = safeValue(cell.getSourceType(), "");
        if ("BOOKING".equalsIgnoreCase(sourceType)) {
            if ("C".equalsIgnoreCase(safeValue(cell.getStatusCode(), ""))) {
                return "Phòng đã được giữ và đang chờ check-in. Có thể mở màn Đặt phòng để xử lý booking DP" + cell.getMaDatPhong() + ".";
            }
            return "Booking đã tạo nhưng chưa tới bước check-in. Có thể mở màn Đặt phòng để xem booking DP" + cell.getMaDatPhong() + ".";
        }
        if ("STAY".equalsIgnoreCase(sourceType)) {
            return "Lưu trú đang ở. Có thể mở màn Check-in / Check-out để xử lý hồ sơ DP" + cell.getMaDatPhong() + ".";
        }
        if ("MAINTENANCE".equalsIgnoreCase(sourceType)) {
            return "Phòng đang ở trạng thái " + safeValue(cell.getStatusText(), "Bảo trì") + ". Không có booking/lưu trú khả dụng cho ô này.";
        }
        return "Phòng hiện đang trống trong ngày đã chọn.";
    }

    private void openSelectedGanttBooking() {
        if (selectedGanttCell == null || selectedGanttCell.getMaDatPhong() <= 0
                || !"BOOKING".equalsIgnoreCase(selectedGanttCell.getSourceType())) {
            showMessage("Ô đang chọn không có booking chờ check-in để mở.");
            return;
        }
        DatPhongGUI.prepareFocusOnBooking(selectedGanttCell.getMaDatPhong());
        NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.DAT_PHONG, username, role);
    }

    private void openSelectedGanttStay() {
        if (selectedGanttCell == null || selectedGanttCell.getMaDatPhong() <= 0
                || !"STAY".equalsIgnoreCase(selectedGanttCell.getSourceType())) {
            showMessage("Ô đang chọn không có lưu trú đang ở để mở.");
            return;
        }
        CheckInOutGUI.prepareFocusOnBooking(selectedGanttCell.getMaDatPhong());
        NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role);
    }

    private String buildGanttTooltip(DashboardGanttCell cell) {
        if (cell == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("<html>");
        builder.append("Phòng: ").append(safeValue(cell.getSoPhong(), "-"));
        builder.append("<br>Ngày: ").append(cell.getDate() == null ? "-" : cell.getDate().format(DATE_LABEL_FORMAT));
        builder.append("<br>Trạng thái: ").append(safeValue(cell.getStatusText(), "-"));
        if ("BOOKING".equalsIgnoreCase(cell.getSourceType())) {
            builder.append("<br>Mã đặt phòng: DP").append(cell.getMaDatPhong());
            builder.append("<br>Khách: ").append(safeValue(cell.getCustomerName(), "-"));
            builder.append("<br>Từ ")
                    .append(cell.getFromDate() == null ? "-" : cell.getFromDate().format(DATE_LABEL_FORMAT))
                    .append(" đến ")
                    .append(cell.getToDate() == null ? "-" : cell.getToDate().format(DATE_LABEL_FORMAT));
        } else if ("STAY".equalsIgnoreCase(cell.getSourceType())) {
            builder.append("<br>Mã lưu trú: LT").append(cell.getMaLuuTru());
            builder.append("<br>Mã đặt phòng: DP").append(cell.getMaDatPhong());
            builder.append("<br>Khách: ").append(safeValue(cell.getCustomerName(), "-"));
            builder.append("<br>Từ ")
                    .append(cell.getFromDate() == null ? "-" : cell.getFromDate().format(DATE_LABEL_FORMAT))
                    .append(" đến ")
                    .append(cell.getToDate() == null ? "-" : cell.getToDate().format(DATE_LABEL_FORMAT));
        }
        builder.append("</html>");
        return builder.toString();
    }

    private Color resolveGanttCellColor(DashboardGanttCell cell) {
        if (cell == null) {
            return CARD_BG;
        }
        String statusCode = safeValue(cell.getStatusCode(), "E");
        if ("M".equalsIgnoreCase(statusCode)) {
            return GANTT_MAINTENANCE_BG;
        }
        if ("O".equalsIgnoreCase(statusCode)) {
            return GANTT_OCCUPIED_BG;
        }
        if ("C".equalsIgnoreCase(statusCode)) {
            return GANTT_PENDING_CHECKIN_BG;
        }
        if ("B".equalsIgnoreCase(statusCode)) {
            return GANTT_BOOKED_BG;
        }
        return GANTT_EMPTY_BG;
    }

    private Color resolveGanttCellForeground(DashboardGanttCell cell) {
        if (cell == null) {
            return TEXT_PRIMARY;
        }
        String statusCode = safeValue(cell.getStatusCode(), "E");
        if ("M".equalsIgnoreCase(statusCode)) {
            return GANTT_MAINTENANCE_FG;
        }
        if ("O".equalsIgnoreCase(statusCode)) {
            return GANTT_OCCUPIED_FG;
        }
        if ("C".equalsIgnoreCase(statusCode)) {
            return GANTT_PENDING_CHECKIN_FG;
        }
        if ("B".equalsIgnoreCase(statusCode)) {
            return GANTT_BOOKED_FG;
        }
        return GANTT_EMPTY_FG;
    }

    private String resolveGanttCellLabel(DashboardGanttCell cell) {
        if (cell == null) {
            return "";
        }
        String statusCode = safeValue(cell.getStatusCode(), "E");
        if ("M".equalsIgnoreCase(statusCode)) {
            return "BT";
        }
        if ("O".equalsIgnoreCase(statusCode)) {
            return "Ở";
        }
        if ("C".equalsIgnoreCase(statusCode)) {
            return "Chờ CI";
        }
        if ("B".equalsIgnoreCase(statusCode)) {
            return "Đặt";
        }
        return "Trống";
    }

    private void loadDashboardData(boolean showMessage) {
        String errorMessage = reloadGanttData(false);

        DashboardSummary summary = dashboardDAO.getDashboardSummary();
        errorMessage = mergeErrors(errorMessage, dashboardDAO.getLastErrorMessage());

        List<DashboardTaskRow> taskRows = dashboardDAO.getTodayTasks();
        errorMessage = mergeErrors(errorMessage, dashboardDAO.getLastErrorMessage());

        loadSummaryCards(summary);
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
        if (lblNgayLamViecValue == null || lblLanCapNhatValue == null) {
            return;
        }
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

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
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

    private final class DashboardGanttCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(BODY_FONT);
            setForeground(TEXT_PRIMARY);
            setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

            if (value instanceof DashboardGanttRow) {
                DashboardGanttRow ganttRow = (DashboardGanttRow) value;
                setText("<html><b>" + safeValue(ganttRow.getSoPhong(), "-") + "</b><br><span style='color:#6b7280;'>"
                        + safeValue(ganttRow.getTang(), "-") + "</span></html>");
                setBackground(Color.WHITE);
                setHorizontalAlignment(SwingConstants.LEFT);
                return this;
            }

            if (value instanceof DashboardGanttCell) {
                DashboardGanttCell cell = (DashboardGanttCell) value;
                setText(resolveGanttCellLabel(cell));
                setBackground(resolveGanttCellColor(cell));
                setForeground(resolveGanttCellForeground(cell));
                boolean selected = selectedGanttCell != null
                        && selectedGanttCell.getMaPhong() == cell.getMaPhong()
                        && selectedGanttCell.getDate() != null
                        && selectedGanttCell.getDate().equals(cell.getDate());
                setBorder(selected
                        ? BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BRAND_BLUE, 3, true),
                        BorderFactory.createLineBorder(Color.WHITE, 1, true))
                        : BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
                return this;
            }

            setText(value == null ? "" : value.toString());
            setBackground(Color.WHITE);
            return this;
        }
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
