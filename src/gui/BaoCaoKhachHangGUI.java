package gui;

import dao.KhachHangDAO;
import entity.KhachHang;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BaoCaoKhachHangGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color BRAND_GREEN = new Color(22, 163, 74);
    private static final Color BRAND_AMBER = new Color(245, 158, 11);
    private static final Color BRAND_INDIGO = new Color(99, 102, 241);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String username;
    private final String role;
    private final List<CustomerProfile> sourceData;

    private JPanel rootPanel;
    private JComboBox<String> cboCheDoLoc;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboNam;
    private JComboBox<String> cboNhomKhach;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblTongKhach;
    private JLabel lblKhachMoi;
    private JLabel lblKhachVip;
    private JLabel lblKhachNuocNgoai;
    private JLabel lblTongKhachSub;
    private JLabel lblKhachMoiSub;
    private JLabel lblKhachVipSub;
    private JLabel lblKhachNuocNgoaiSub;

    private JTable tblPhanLoai;
    private JTable tblTopKhach;
    private DefaultTableModel phanLoaiModel;
    private DefaultTableModel topKhachModel;
    private CustomerCategoryChartPanel chartPanel;

    public BaoCaoKhachHangGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoKhachHangGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");
        this.sourceData = loadCustomerProfiles();

        setTitle("Báo cáo khách hàng - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadCustomerReport(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.BAO_CAO_KHACH_HANG, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("BÁO CÁO KHÁCH HÀNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi nhóm khách, khách mới, VIP và khách nổi bật theo kỳ báo cáo.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "man hinh Báo cáo khách hàng"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem báo cáo", BRAND_GREEN, Color.WHITE, e -> loadCustomerReport(true)));
        card.add(createPrimaryButton("Xuất file", BRAND_BLUE, Color.WHITE, e -> showInfo("Đã sẵn sàng xuất báo cáo khách hàng.")));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboCheDoLoc = createComboBox(new String[]{"Khoảng thời gian", "Theo ngày", "Theo tháng", "Theo năm"});
        cboThang = createComboBox(new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        cboNam = createComboBox(new String[]{"2026", "2025", "2024", "2023"});
        cboNhomKhach = createComboBox(new String[]{"Tất cả", "VIP", "Thường", "Nước ngoài", "Nội địa", "Doanh nghiệp"});
        txtTuNgay = new AppDatePickerField("01/04/2026", true);
        txtDenNgay = new AppDatePickerField("30/04/2026", true);

        left.add(createFieldGroup("Chế độ lọc", cboCheDoLoc));
        left.add(createFieldGroup("Tháng", cboThang));
        left.add(createFieldGroup("Năm", cboNam));
        left.add(createFieldGroup("Nhóm khách", cboNhomKhach));
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
        txtTuKhoa.setToolTipText("Tên khách, mã khách, quốc tịch hoặc nhóm");
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> loadCustomerReport(false));
        ScreenUIHelper.installAutoFilter(() -> loadCustomerReport(false), cboCheDoLoc, cboThang, cboNam, cboNhomKhach);

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

        lblTongKhach = new JLabel();
        lblKhachMoi = new JLabel();
        lblKhachVip = new JLabel();
        lblKhachNuocNgoai = new JLabel();
        lblTongKhachSub = new JLabel();
        lblKhachMoiSub = new JLabel();
        lblKhachVipSub = new JLabel();
        lblKhachNuocNgoaiSub = new JLabel();

        panel.add(createSummaryCard("Tổng số khách", lblTongKhach, lblTongKhachSub, BRAND_BLUE));
        panel.add(createSummaryCard("Khách mới", lblKhachMoi, lblKhachMoiSub, BRAND_GREEN));
        panel.add(createSummaryCard("Khach VIP", lblKhachVip, lblKhachVipSub, BRAND_AMBER));
        panel.add(createSummaryCard("Khách nước ngoài", lblKhachNuocNgoai, lblKhachNuocNgoaiSub, BRAND_INDIGO));
        return panel;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(buildChartCard(), BorderLayout.NORTH);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);
        bottom.add(buildClassificationTableCard());
        bottom.add(buildTopCustomerTableCard());

        center.add(bottom, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildChartCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Biểu đồ phân loại khách hàng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("So sánh VIP, thường, nước ngoài và nội địa trong kỳ đang lọc.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        chartPanel = new CustomerCategoryChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 300));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildClassificationTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảng phân loại khách hàng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Tổng hợp theo nhóm để đối chiếu khi demo và dễ thay DAO thật.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        phanLoaiModel = new DefaultTableModel(
                new String[]{"Nhóm khách", "Số lượng", "Tỷ lệ", "Khách mới", "Ghi chú"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblPhanLoai = new JTable(phanLoaiModel);
        styleTable(tblPhanLoai);

        JScrollPane scrollPane = new JScrollPane(tblPhanLoai);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTopCustomerTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Top khách đặt nhiều lần");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Dữ liệu để mở rộng sang booking thật khi bổ sung DAO.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        topKhachModel = new DefaultTableModel(
                new String[]{"Mã KH", "Tên khách", "Nhom", "Số lần đặt", "Tổng chi tiêu", "Đánh giá"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTopKhach = new JTable(topKhachModel);
        styleTable(tblTopKhach);

        JScrollPane scrollPane = new JScrollPane(tblTopKhach);
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
                "Enter Xem nhanh"
        );
    }

    private void loadCustomerReport(boolean showMessage) {
        List<CustomerProfile> filtered = filterProfiles();
        SummaryData summary = buildSummary(filtered);
        updateSummary(summary);
        reloadClassificationTable(summary);
        reloadTopCustomerTable(filtered);
        chartPanel.setItems(summary.chartItems);
        if (showMessage) {
            showInfo("Da cap nhat bao cao khach hang.");
        }
    }

    private List<CustomerProfile> filterProfiles() {
        List<CustomerProfile> filtered = new ArrayList<CustomerProfile>();
        String keyword = txtTuKhoa == null ? "" : safeValue(txtTuKhoa.getText(), "").toLowerCase(Locale.ROOT);
        String selectedGroup = cboNhomKhach == null ? "Tất cả" : String.valueOf(cboNhomKhach.getSelectedItem());
        String filterMode = cboCheDoLoc == null ? "Khoảng thời gian" : String.valueOf(cboCheDoLoc.getSelectedItem());
        LocalDate fromDate = resolveFromDate(filterMode);
        LocalDate toDate = resolveToDate(filterMode);

        for (CustomerProfile profile : sourceData) {
            if (profile.registeredDate.isBefore(fromDate) || profile.registeredDate.isAfter(toDate)) {
                continue;
            }
            if (!matchesGroup(profile, selectedGroup)) {
                continue;
            }
            if (!keyword.isEmpty() && !containsKeyword(profile, keyword)) {
                continue;
            }
            filtered.add(profile);
        }
        return filtered;
    }

    private SummaryData buildSummary(List<CustomerProfile> profiles) {
        SummaryData summary = new SummaryData();
        summary.totalCustomers = profiles.size();

        for (CustomerProfile profile : profiles) {
            if (profile.isNewCustomer) {
                summary.newCustomers++;
            }
            if (profile.isVip) {
                summary.vipCustomers++;
            }
            if (profile.isForeignCustomer) {
                summary.foreignCustomers++;
            }
            addGroupCount(summary.groupCounts, profile.customerGroup, profile);
        }

        summary.chartItems.add(new ChartItem("VIP", summary.vipCustomers, BRAND_AMBER));
        summary.chartItems.add(new ChartItem("Thường", countRegularCustomers(profiles), BRAND_BLUE));
        summary.chartItems.add(new ChartItem("Nước ngoài", summary.foreignCustomers, BRAND_INDIGO));
        summary.chartItems.add(new ChartItem("Nội địa", summary.totalCustomers - summary.foreignCustomers, BRAND_GREEN));
        return summary;
    }

    private int countRegularCustomers(List<CustomerProfile> profiles) {
        int count = 0;
        for (CustomerProfile profile : profiles) {
            if (!profile.isVip) {
                count++;
            }
        }
        return count;
    }

    private void updateSummary(SummaryData summary) {
        lblTongKhach.setText(formatNumber(summary.totalCustomers));
        lblKhachMoi.setText(formatNumber(summary.newCustomers));
        lblKhachVip.setText(formatNumber(summary.vipCustomers));
        lblKhachNuocNgoai.setText(formatNumber(summary.foreignCustomers));

        lblTongKhachSub.setText("Ky loc: " + formatSelectedPeriod());
        lblKhachMoiSub.setText("Tỷ lệ: " + percentText(summary.newCustomers, summary.totalCustomers));
        lblKhachVipSub.setText("Tỷ lệ: " + percentText(summary.vipCustomers, summary.totalCustomers));
        lblKhachNuocNgoaiSub.setText("Tỷ lệ: " + percentText(summary.foreignCustomers, summary.totalCustomers));
    }

    private void reloadClassificationTable(SummaryData summary) {
        phanLoaiModel.setRowCount(0);

        for (Map.Entry<String, GroupMetric> entry : summary.groupCounts.entrySet()) {
            GroupMetric metric = entry.getValue();
            phanLoaiModel.addRow(new Object[]{
                    entry.getKey(),
                    formatNumber(metric.total),
                    percentText(metric.total, summary.totalCustomers),
                    formatNumber(metric.newCustomers),
                    metric.note
            });
        }

        if (phanLoaiModel.getRowCount() == 0) {
            phanLoaiModel.addRow(new Object[]{"Không có dữ liệu", "0", "0%", "0", "Không có khách phù hợp bộ lọc"});
        }
        tblPhanLoai.setRowSelectionInterval(0, 0);
    }

    private void reloadTopCustomerTable(List<CustomerProfile> profiles) {
        topKhachModel.setRowCount(0);
        List<CustomerProfile> sorted = new ArrayList<CustomerProfile>(profiles);
        sorted.sort((a, b) -> {
            if (b.bookingCount != a.bookingCount) {
                return b.bookingCount - a.bookingCount;
            }
            return Long.compare(b.totalSpent, a.totalSpent);
        });

        int limit = Math.min(7, sorted.size());
        for (int i = 0; i < limit; i++) {
            CustomerProfile profile = sorted.get(i);
            topKhachModel.addRow(new Object[]{
                    profile.customerId,
                    profile.customerName,
                    profile.customerGroup,
                    formatNumber(profile.bookingCount),
                    formatCurrency(profile.totalSpent),
                    profile.highlight
            });
        }

        if (topKhachModel.getRowCount() == 0) {
            topKhachModel.addRow(new Object[]{"-", "Không có dữ liệu", "-", "0", formatCurrency(0), "Không có khách phù hợp bộ lọc"});
        }
        tblTopKhach.setRowSelectionInterval(0, 0);
    }

    private List<CustomerProfile> loadCustomerProfiles() {
        List<CustomerProfile> profiles = new ArrayList<CustomerProfile>();
        try {
            List<KhachHang> customers = new KhachHangDAO().getAll();
            for (int i = 0; i < customers.size(); i++) {
                CustomerProfile profile = mapFromEntity(customers.get(i), i);
                if (profile != null) {
                    profiles.add(profile);
                }
            }
        } catch (Exception ignored) {
        }

        if (profiles.isEmpty()) {
            profiles.addAll(createSampleProfiles());
        }
        return profiles;
    }

    private CustomerProfile mapFromEntity(KhachHang khachHang, int index) {
        if (khachHang == null) {
            return null;
        }

        String id = safeValue(khachHang.getMaKhachHang(), "KH" + (100 + index));
        int numericId = extractNumber(id, index + 1);
        LocalDate registeredDate = LocalDate.of(2026, 1, 1).plusDays((numericId * 7L) % 95L);
        String nationality = safeValue(khachHang.getQuocTich(), "Viet Năm");
        String rank = safeValue(khachHang.getHangKhach(), "Thường");
        String type = safeValue(khachHang.getLoaiKhach(), "Khách lẻ");

        boolean foreign = !normalize(nationality).contains("viet");
        boolean vip = normalize(rank).contains("vip") || normalize(rank).contains("kim cuong") || normalize(type).contains("vip");
        int bookingCount = 1 + (numericId % 6);
        long totalSpent = 1800000L + (long) numericId * 240000L;
        boolean isNew = bookingCount <= 2 || registeredDate.isAfter(LocalDate.of(2026, 2, 15));
        String group = resolveGroup(type, vip, foreign);

        return new CustomerProfile(
                id,
                safeValue(khachHang.getHoTen(), "Khach " + id),
                nationality,
                group,
                vip,
                foreign,
                isNew,
                registeredDate,
                bookingCount,
                totalSpent,
                vip ? "Khách giá trị cao" : (foreign ? "Can uu tien ho tro ngoai ngu" : "Khách ổn định")
        );
    }

    private List<CustomerProfile> createSampleProfiles() {
        List<CustomerProfile> data = new ArrayList<CustomerProfile>();
        data.add(new CustomerProfile("KH101", "Nguyen Thi Minh", "Viet Năm", "Thường", false, false, true, LocalDate.of(2026, 4, 2), 2, 4200000L, "Khách mới, tiep can goi uu dai"));
        data.add(new CustomerProfile("KH102", "Tran Quoc Bao", "Viet Năm", "VIP", true, false, false, LocalDate.of(2026, 2, 15), 7, 28600000L, "Khách quay lại thường xuyên"));
        data.add(new CustomerProfile("KH103", "Anna Lee", "Singapore", "Nước ngoài", false, true, true, LocalDate.of(2026, 4, 4), 1, 5600000L, "Cần ưu tiên check-in nhanh"));
        data.add(new CustomerProfile("KH104", "Pham Gia Han", "Viet Năm", "Doanh nghiệp", false, false, false, LocalDate.of(2026, 1, 28), 5, 17100000L, "Đặt phòng theo công tác"));
        data.add(new CustomerProfile("KH105", "David Kim", "Korea", "VIP", true, true, false, LocalDate.of(2026, 1, 12), 6, 32400000L, "Khách VIP nước ngoài"));
        data.add(new CustomerProfile("KH106", "Le Quang Hủy", "Viet Năm", "Thường", false, false, true, LocalDate.of(2026, 3, 30), 1, 2300000L, "Mới phát sinh giao dịch"));
        data.add(new CustomerProfile("KH107", "Hoang My Linh", "Viet Năm", "VIP", true, false, false, LocalDate.of(2025, 12, 20), 8, 40100000L, "Khách thân thiết cần giữ chân"));
        data.add(new CustomerProfile("KH108", "Sokha Chan", "Cambodia", "Nước ngoài", false, true, true, LocalDate.of(2026, 4, 1), 2, 6100000L, "Tỷ lệ quay lại có thể tăng"));
        data.add(new CustomerProfile("KH109", "Vo Thanh Dat", "Viet Năm", "Doanh nghiệp", false, false, false, LocalDate.of(2026, 2, 8), 4, 15800000L, "Công tác theo tháng"));
        data.add(new CustomerProfile("KH110", "Mai Thu Trang", "Viet Năm", "Thường", false, false, false, LocalDate.of(2026, 1, 5), 3, 8900000L, "Khách cá nhân ổn định"));
        return data;
    }

    private void addGroupCount(Map<String, GroupMetric> groupCounts, String groupName, CustomerProfile profile) {
        GroupMetric metric = groupCounts.get(groupName);
        if (metric == null) {
            metric = new GroupMetric(resolveGroupNote(groupName));
            groupCounts.put(groupName, metric);
        }
        metric.total++;
        if (profile.isNewCustomer) {
            metric.newCustomers++;
        }
    }

    private String resolveGroup(String customerType, boolean vip, boolean foreign) {
        if (vip) {
            return "VIP";
        }
        if (foreign) {
            return "Nước ngoài";
        }
        String normalizedType = normalize(customerType);
        if (normalizedType.contains("doanh nghiep") || normalizedType.contains("cong tac") || normalizedType.contains("doan")) {
            return "Doanh nghiệp";
        }
        return "Thường";
    }

    private String resolveGroupNote(String groupName) {
        if ("VIP".equalsIgnoreCase(groupName)) {
            return "Nen uu tien chuong trinh cham soc";
        }
        if ("Nước ngoài".equalsIgnoreCase(groupName)) {
            return "Can quy trinh ho tro ngon ngu";
        }
        if ("Doanh nghiệp".equalsIgnoreCase(groupName)) {
            return "Phu hop goi hop dong va uu dai cong ty";
        }
        return "Tập khách ổn định trong vận hành hằng ngày";
    }

    private boolean matchesGroup(CustomerProfile profile, String selectedGroup) {
        if (selectedGroup == null || "Tất cả".equalsIgnoreCase(selectedGroup)) {
            return true;
        }
        if ("VIP".equalsIgnoreCase(selectedGroup)) {
            return profile.isVip;
        }
        if ("Thường".equalsIgnoreCase(selectedGroup)) {
            return !profile.isVip && !profile.isForeignCustomer && !"Doanh nghiệp".equalsIgnoreCase(profile.customerGroup);
        }
        if ("Nước ngoài".equalsIgnoreCase(selectedGroup)) {
            return profile.isForeignCustomer;
        }
        if ("Nội địa".equalsIgnoreCase(selectedGroup)) {
            return !profile.isForeignCustomer;
        }
        if ("Doanh nghiệp".equalsIgnoreCase(selectedGroup)) {
            return "Doanh nghiệp".equalsIgnoreCase(profile.customerGroup);
        }
        return true;
    }

    private boolean containsKeyword(CustomerProfile profile, String keyword) {
        return normalize(profile.customerId).contains(keyword)
                || normalize(profile.customerName).contains(keyword)
                || normalize(profile.nationality).contains(keyword)
                || normalize(profile.customerGroup).contains(keyword)
                || normalize(profile.highlight).contains(keyword);
    }

    private LocalDate resolveFromDate(String filterMode) {
        LocalDate today = LocalDate.of(2026, 4, 6);
        if ("Theo ngày".equalsIgnoreCase(filterMode)) {
            return parseDateOrFallback(txtTuNgay == null ? null : txtTuNgay.getText(), today);
        }
        if ("Theo tháng".equalsIgnoreCase(filterMode)) {
            int month = cboThang == null ? today.getMonthValue() : Integer.parseInt(String.valueOf(cboThang.getSelectedItem()));
            int year = cboNam == null ? today.getYear() : Integer.parseInt(String.valueOf(cboNam.getSelectedItem()));
            return YearMonth.of(year, month).atDay(1);
        }
        if ("Theo năm".equalsIgnoreCase(filterMode)) {
            int year = cboNam == null ? today.getYear() : Integer.parseInt(String.valueOf(cboNam.getSelectedItem()));
            return LocalDate.of(year, 1, 1);
        }
        return parseDateOrFallback(txtTuNgay == null ? null : txtTuNgay.getText(), today.minusDays(30));
    }

    private LocalDate resolveToDate(String filterMode) {
        LocalDate today = LocalDate.of(2026, 4, 6);
        if ("Theo ngày".equalsIgnoreCase(filterMode)) {
            return parseDateOrFallback(txtTuNgay == null ? null : txtTuNgay.getText(), today);
        }
        if ("Theo tháng".equalsIgnoreCase(filterMode)) {
            int month = cboThang == null ? today.getMonthValue() : Integer.parseInt(String.valueOf(cboThang.getSelectedItem()));
            int year = cboNam == null ? today.getYear() : Integer.parseInt(String.valueOf(cboNam.getSelectedItem()));
            return YearMonth.of(year, month).atEndOfMonth();
        }
        if ("Theo năm".equalsIgnoreCase(filterMode)) {
            int year = cboNam == null ? today.getYear() : Integer.parseInt(String.valueOf(cboNam.getSelectedItem()));
            return LocalDate.of(year, 12, 31);
        }
        return parseDateOrFallback(txtDenNgay == null ? null : txtDenNgay.getText(), today);
    }

    private LocalDate parseDateOrFallback(String text, LocalDate fallback) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return fallback;
            }
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private void resetFilters() {
        cboCheDoLoc.setSelectedIndex(0);
        cboThang.setSelectedIndex(3);
        cboNam.setSelectedIndex(0);
        cboNhomKhach.setSelectedIndex(0);
        txtTuNgay.setText("01/04/2026");
        txtDenNgay.setText("30/04/2026");
        txtTuKhoa.setText("");
        loadCustomerReport(false);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "bao_cao_khach_hang_view", new Runnable() {
            @Override
            public void run() {
                loadCustomerReport(true);
            }
        });
        ScreenUIHelper.registerShortcut(this, "F2", "bao_cao_khach_hang_export", new Runnable() {
            @Override
            public void run() {
                showInfo("Đã sẵn sàng xuất báo cáo khách hàng.");
            }
        });
        ScreenUIHelper.registerShortcut(this, "F5", "bao_cao_khach_hang_refresh", new Runnable() {
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
        comboBox.setPreferredSize(new Dimension(140, 34));
        comboBox.setMaximumSize(new Dimension(160, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(180, 34));
        field.setMaximumSize(new Dimension(240, 34));
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

    private void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER_SOFT);
        table.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(table);
    }

    private String formatNumber(long value) {
        return NUMBER_FORMAT.format(value);
    }

    private String formatCurrency(long value) {
        return NUMBER_FORMAT.format(value) + " d";
    }

    private String percentText(long part, long total) {
        if (total <= 0) {
            return "0%";
        }
        double percent = (double) part * 100d / (double) total;
        return String.format(Locale.US, "%.1f%%", percent);
    }

    private String formatSelectedPeriod() {
        String filterMode = cboCheDoLoc == null ? "Khoảng thời gian" : String.valueOf(cboCheDoLoc.getSelectedItem());
        if ("Theo ngày".equalsIgnoreCase(filterMode)) {
            return safeValue(txtTuNgay.getText(), "-");
        }
        if ("Theo tháng".equalsIgnoreCase(filterMode)) {
            return String.valueOf(cboThang.getSelectedItem()) + "/" + String.valueOf(cboNam.getSelectedItem());
        }
        if ("Theo năm".equalsIgnoreCase(filterMode)) {
            return String.valueOf(cboNam.getSelectedItem());
        }
        return safeValue(txtTuNgay.getText(), "-") + " - " + safeValue(txtDenNgay.getText(), "-");
    }

    private int extractNumber(String value, int fallback) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (Character.isDigit(ch)) {
                builder.append(ch);
            }
        }
        if (builder.length() == 0) {
            return fallback;
        }
        try {
            return Integer.parseInt(builder.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String normalize(String value) {
        return safeValue(value, "").trim().toLowerCase(Locale.ROOT);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Báo cáo khách hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    private static final class CustomerProfile {
        private final String customerId;
        private final String customerName;
        private final String nationality;
        private final String customerGroup;
        private final boolean isVip;
        private final boolean isForeignCustomer;
        private final boolean isNewCustomer;
        private final LocalDate registeredDate;
        private final int bookingCount;
        private final long totalSpent;
        private final String highlight;

        private CustomerProfile(String customerId, String customerName, String nationality,
                                String customerGroup, boolean isVip, boolean isForeignCustomer,
                                boolean isNewCustomer, LocalDate registeredDate, int bookingCount,
                                long totalSpent, String highlight) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.nationality = nationality;
            this.customerGroup = customerGroup;
            this.isVip = isVip;
            this.isForeignCustomer = isForeignCustomer;
            this.isNewCustomer = isNewCustomer;
            this.registeredDate = registeredDate;
            this.bookingCount = bookingCount;
            this.totalSpent = totalSpent;
            this.highlight = highlight;
        }
    }

    private static final class GroupMetric {
        private int total;
        private int newCustomers;
        private final String note;

        private GroupMetric(String note) {
            this.note = note;
        }
    }

    private static final class SummaryData {
        private int totalCustomers;
        private int newCustomers;
        private int vipCustomers;
        private int foreignCustomers;
        private final Map<String, GroupMetric> groupCounts = new LinkedHashMap<String, GroupMetric>();
        private final List<ChartItem> chartItems = new ArrayList<ChartItem>();
    }

    private static final class ChartItem {
        private final String label;
        private final int value;
        private final Color color;

        private ChartItem(String label, int value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final class CustomerCategoryChartPanel extends JPanel {
        private List<ChartItem> items = new ArrayList<ChartItem>();

        private CustomerCategoryChartPanel() {
            setOpaque(true);
            setBackground(PANEL_SOFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }

        private void setItems(List<ChartItem> items) {
            this.items = new ArrayList<ChartItem>(items);
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
                int left = 56;
                int right = 28;
                int top = 20;
                int bottom = 54;
                int chartWidth = width - left - right;
                int chartHeight = height - top - bottom;

                if (chartWidth <= 0 || chartHeight <= 0 || items.isEmpty()) {
                    drawEmptyState(g2, width, height);
                    return;
                }

                int maxValue = 0;
                for (ChartItem item : items) {
                    maxValue = Math.max(maxValue, item.value);
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

                int step = chartWidth / items.size();
                int barWidth = Math.max(38, step / 2);

                for (int i = 0; i < items.size(); i++) {
                    ChartItem item = items.get(i);
                    int centerX = left + step * i + step / 2;
                    int barHeight = (int) Math.round((double) item.value * chartHeight / (double) maxValue);
                    int barX = centerX - barWidth / 2;
                    int barY = top + chartHeight - barHeight;

                    g2.setColor(item.color);
                    g2.fillRoundRect(barX, barY, barWidth, barHeight, 14, 14);
                    g2.setColor(item.color.darker());
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(barX, barY, barWidth, barHeight, 14, 14);

                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    String valueText = String.valueOf(item.value);
                    int valueWidth = g2.getFontMetrics().stringWidth(valueText);
                    g2.drawString(valueText, centerX - valueWidth / 2, Math.max(top + 14, barY - 8));

                    g2.setFont(LABEL_FONT);
                    int labelWidth = g2.getFontMetrics().stringWidth(item.label);
                    g2.drawString(item.label, centerX - labelWidth / 2, top + chartHeight + 18);
                }

                drawLegend(g2, left, height - 18);
            } finally {
                g2.dispose();
            }
        }

        private void drawLegend(Graphics2D g2, int startX, int baselineY) {
            g2.setFont(LABEL_FONT);
            int currentX = startX;
            for (ChartItem item : items) {
                g2.setColor(item.color);
                g2.fillRoundRect(currentX, baselineY - 9, 18, 10, 6, 6);
                g2.setColor(TEXT_MUTED);
                g2.drawString(item.label, currentX + 24, baselineY);
                currentX += 24 + g2.getFontMetrics().stringWidth(item.label) + 26;
            }
        }

        private void drawEmptyState(Graphics2D g2, int width, int height) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(BODY_FONT);
            String message = "Chưa có dữ liệu khách hàng để hiển thị.";
            int textWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - textWidth) / 2, height / 2);
        }
    }
}
