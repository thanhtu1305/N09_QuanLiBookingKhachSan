package gui;

import dao.BaoCaoDAO;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoDichVuGUI extends JFrame {
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
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String username;
    private final String role;
    private final BaoCaoDAO baoCaoDAO = new BaoCaoDAO();

    private JPanel rootPanel;
    private JComboBox<String> cboCheDoLoc;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboNam;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblTongLuotSuDung;
    private JLabel lblTongDoanhThuDichVu;
    private JLabel lblDichVuPhoBien;
    private JLabel lblSoDichVuPhatSinh;
    private JLabel lblTongLuotSuDungSub;
    private JLabel lblTongDoanhThuDichVuSub;
    private JLabel lblDichVuPhoBienSub;
    private JLabel lblSoDichVuPhatSinhSub;

    private JTable tblTopDichVu;
    private JTable tblDoanhThuDichVu;
    private DefaultTableModel topServiceModel;
    private DefaultTableModel revenueModel;
    private ServiceChartPanel chartPanel;

    public BaoCaoDichVuGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoDichVuGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");

        setTitle("Báo cáo dịch vụ - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        resetDateInputsToCurrentMonth();
        loadServiceData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.BAO_CAO_DICH_VU, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("BÁO CÁO DỊCH VỤ"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi mức sử dụng dịch vụ, doanh thu phát sinh và top dịch vụ phổ biến trong kỳ.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Báo cáo dịch vụ"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem báo cáo", BRAND_GREEN, Color.WHITE, e -> loadServiceData(true)));
        card.add(createPrimaryButton("Xuất file", BRAND_BLUE, Color.WHITE, e -> showInfo("Đã sẵn sàng xuất báo cáo dịch vụ.")));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboCheDoLoc = createComboBox(new String[]{"Theo khoảng thời gian", "Theo ngày", "Theo tháng", "Theo năm"});
        cboThang = createComboBox(new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        cboNam = createComboBox(new String[]{"2026", "2025", "2024"});
        txtTuNgay = new AppDatePickerField("", true);
        txtDenNgay = new AppDatePickerField("", true);

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
        txtTuKhoa.setToolTipText("Tên dịch vụ");
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> loadServiceData(false));
        ScreenUIHelper.installLiveSearch(txtTuNgay, () -> loadServiceData(false));
        ScreenUIHelper.installLiveSearch(txtDenNgay, () -> loadServiceData(false));
        ScreenUIHelper.installAutoFilter(() -> loadServiceData(false), cboCheDoLoc, cboThang, cboNam);

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

        lblTongLuotSuDung = new JLabel();
        lblTongDoanhThuDichVu = new JLabel();
        lblDichVuPhoBien = new JLabel();
        lblSoDichVuPhatSinh = new JLabel();
        lblTongLuotSuDungSub = new JLabel();
        lblTongDoanhThuDichVuSub = new JLabel();
        lblDichVuPhoBienSub = new JLabel();
        lblSoDichVuPhatSinhSub = new JLabel();

        panel.add(createSummaryCard("Tổng lượt sử dụng", lblTongLuotSuDung, lblTongLuotSuDungSub, BRAND_BLUE));
        panel.add(createSummaryCard("Tổng doanh thu dịch vụ", lblTongDoanhThuDichVu, lblTongDoanhThuDichVuSub, BRAND_GREEN));
        panel.add(createSummaryCard("Dịch vụ dùng nhiều nhất", lblDichVuPhoBien, lblDichVuPhoBienSub, BRAND_AMBER));
        panel.add(createSummaryCard("Số dịch vụ phát sinh", lblSoDichVuPhatSinh, lblSoDichVuPhatSinhSub, BRAND_INDIGO));
        return panel;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        center.add(buildChartCard(), BorderLayout.NORTH);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);
        bottom.add(buildTopServiceCard());
        bottom.add(buildRevenueTableCard());

        center.add(bottom, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildChartCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Biểu đồ top dịch vụ được sử dụng nhiều nhất");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Biểu đồ cột thể hiện số lượt sử dụng theo từng dịch vụ.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        chartPanel = new ServiceChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 300));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTopServiceCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Top dịch vụ phổ biến");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        topServiceModel = new DefaultTableModel(
                new String[]{"Dịch vụ", "Lượt sử dụng", "Số lượng", "Doanh thu"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTopDichVu = new JTable(topServiceModel);
        tblTopDichVu.setFont(BODY_FONT);
        tblTopDichVu.setRowHeight(30);
        tblTopDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTopDichVu.setGridColor(BORDER_SOFT);
        tblTopDichVu.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblTopDichVu);

        JScrollPane scrollPane = new JScrollPane(tblTopDichVu);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRevenueTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Doanh thu dịch vụ theo thời gian");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        revenueModel = new DefaultTableModel(
                new String[]{"Thời gian", "Lượt sử dụng", "Doanh thu", "Ghi chú"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDoanhThuDichVu = new JTable(revenueModel);
        tblDoanhThuDichVu.setFont(BODY_FONT);
        tblDoanhThuDichVu.setRowHeight(30);
        tblDoanhThuDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDoanhThuDichVu.setGridColor(BORDER_SOFT);
        tblDoanhThuDichVu.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblDoanhThuDichVu);

        JScrollPane scrollPane = new JScrollPane(tblDoanhThuDichVu);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lblTitle, BorderLayout.NORTH);
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

    private void loadServiceData(boolean showMessage) {
        ReportDateRange range = resolveSelectedDateRange(showMessage);
        if (range == null) {
            clearReportData();
            return;
        }

        BaoCaoDAO.ServiceSummary summary = baoCaoDAO.getServiceSummary(range.fromDate, range.toDate);
        List<ServiceUsageRecord> filteredUsage = filterUsageRecords(loadUsageRecords(range));
        List<ServiceRevenueRecord> filteredRevenue = loadRevenueRecords(range);
        updateSummary(summary, filteredUsage);
        reloadTopServiceTable(filteredUsage);
        reloadRevenueTable(filteredRevenue);
        chartPanel.setRecords(filteredUsage);
        if (showMessage) {
            String error = baoCaoDAO.getLastErrorMessage();
            showInfo(error.isEmpty() ? "Đã cập nhật báo cáo dịch vụ." : "Không thể tải báo cáo dịch vụ: " + error);
        }
    }

    private List<ServiceUsageRecord> loadUsageRecords(ReportDateRange range) {
        List<ServiceUsageRecord> records = new ArrayList<ServiceUsageRecord>();
        List<BaoCaoDAO.ServiceUsageStat> stats = baoCaoDAO.getServiceUsageStats(range.fromDate, range.toDate);
        for (BaoCaoDAO.ServiceUsageStat stat : stats) {
            records.add(new ServiceUsageRecord(
                    stat.getServiceName(),
                    stat.getUsageCount(),
                    stat.getQuantity(),
                    Math.round(stat.getRevenue())
            ));
        }
        return records;
    }

    private List<ServiceRevenueRecord> loadRevenueRecords(ReportDateRange range) {
        List<ServiceRevenueRecord> records = new ArrayList<ServiceRevenueRecord>();
        List<BaoCaoDAO.ServiceRevenueDateStat> stats = baoCaoDAO.getServiceRevenueByDate(range.fromDate, range.toDate);
        for (BaoCaoDAO.ServiceRevenueDateStat stat : stats) {
            records.add(new ServiceRevenueRecord(
                    formatDate(stat.getDate()),
                    stat.getUsageCount(),
                    Math.round(stat.getRevenue()),
                    ""
            ));
        }
        return records;
    }

    private List<ServiceUsageRecord> filterUsageRecords(List<ServiceUsageRecord> records) {
        List<ServiceUsageRecord> filtered = new ArrayList<ServiceUsageRecord>();
        String keyword = txtTuKhoa == null ? "" : txtTuKhoa.getText().trim().toLowerCase();
        for (ServiceUsageRecord record : records) {
            if (!keyword.isEmpty() && !record.tenDichVu.toLowerCase().contains(keyword)) {
                continue;
            }
            filtered.add(record);
        }
        return filtered;
    }

    private void updateSummary(BaoCaoDAO.ServiceSummary summary, List<ServiceUsageRecord> usageRecords) {
        int tongLuotSuDung = summary.getTotalUsage();
        long tongDoanhThu = Math.round(summary.getTotalRevenue());
        ServiceUsageRecord topService = null;

        for (ServiceUsageRecord record : usageRecords) {
            if (topService == null || record.luotSuDung > topService.luotSuDung) {
                topService = record;
            }
        }

        lblTongLuotSuDung.setText(String.valueOf(tongLuotSuDung));
        lblTongDoanhThuDichVu.setText(formatMoney(tongDoanhThu));
        lblSoDichVuPhatSinh.setText(String.valueOf(usageRecords.size()));

        if (topService == null) {
            lblDichVuPhoBien.setText("Chưa có");
            lblDichVuPhoBienSub.setText("Không có dữ liệu dịch vụ nổi bật.");
        } else {
            lblDichVuPhoBien.setText(topService.tenDichVu);
            lblDichVuPhoBienSub.setText(topService.luotSuDung + " lượt | " + formatMoney(topService.doanhThu));
        }

        lblTongLuotSuDungSub.setText("Tổng lượt phát sinh trong kỳ");
        lblTongDoanhThuDichVuSub.setText("Trung bình: " + formatMoney(usageRecords.isEmpty() ? 0L : tongDoanhThu / usageRecords.size()));
        lblSoDichVuPhatSinhSub.setText("Số loại dịch vụ có giao dịch");
    }

    private void reloadTopServiceTable(List<ServiceUsageRecord> records) {
        topServiceModel.setRowCount(0);
        if (records.isEmpty()) {
            topServiceModel.addRow(new Object[]{"Không có dữ liệu", 0, 0, formatMoney(0L)});
            tblTopDichVu.setRowSelectionInterval(0, 0);
            return;
        }
        for (ServiceUsageRecord record : records) {
            topServiceModel.addRow(new Object[]{
                    record.tenDichVu,
                    record.luotSuDung,
                    record.soLuong,
                    formatMoney(record.doanhThu)
            });
        }
        if (!records.isEmpty()) {
            tblTopDichVu.setRowSelectionInterval(0, 0);
        }
    }

    private void reloadRevenueTable(List<ServiceRevenueRecord> records) {
        revenueModel.setRowCount(0);
        if (records.isEmpty()) {
            revenueModel.addRow(new Object[]{"Không có dữ liệu trong khoảng thời gian này.", 0, formatMoney(0L), ""});
            tblDoanhThuDichVu.setRowSelectionInterval(0, 0);
            return;
        }
        for (ServiceRevenueRecord record : records) {
            revenueModel.addRow(new Object[]{
                    record.thoiGian,
                    record.luotSuDung,
                    formatMoney(record.doanhThu),
                    record.ghiChu
            });
        }
        if (!records.isEmpty()) {
            tblDoanhThuDichVu.setRowSelectionInterval(0, 0);
        }
    }

    private void resetFilters() {
        cboCheDoLoc.setSelectedIndex(0);
        resetDateInputsToCurrentMonth();
        txtTuKhoa.setText("");
        loadServiceData(false);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "bao_cao_dich_vu_view", new Runnable() {
            @Override
            public void run() {
                loadServiceData(true);
            }
        });
        ScreenUIHelper.registerShortcut(this, "F2", "bao_cao_dich_vu_export", new Runnable() {
            @Override
            public void run() {
                showInfo("Đã sẵn sàng xuất báo cáo dịch vụ.");
            }
        });
        ScreenUIHelper.registerShortcut(this, "F5", "bao_cao_dich_vu_refresh", new Runnable() {
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

    private String formatMoney(long value) {
        return MONEY_FORMAT.format(value) + " đ";
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Báo cáo dịch vụ", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearReportData() {
        List<ServiceUsageRecord> emptyUsage = new ArrayList<ServiceUsageRecord>();
        List<ServiceRevenueRecord> emptyRevenue = new ArrayList<ServiceRevenueRecord>();
        updateSummary(new BaoCaoDAO.ServiceSummary(), emptyUsage);
        reloadTopServiceTable(emptyUsage);
        reloadRevenueTable(emptyRevenue);
        chartPanel.setRecords(emptyUsage);
    }

    private void resetDateInputsToCurrentMonth() {
        LocalDate today = LocalDate.now();
        setComboValue(cboThang, String.format("%02d", today.getMonthValue()));
        setComboValue(cboNam, String.valueOf(today.getYear()));
        txtTuNgay.setDateValue(today.withDayOfMonth(1));
        txtDenNgay.setDateValue(today);
    }

    private void setComboValue(JComboBox<String> comboBox, String value) {
        if (comboBox == null || value == null) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (value.equals(String.valueOf(comboBox.getItemAt(i)))) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private ReportDateRange resolveSelectedDateRange(boolean showMessage) {
        LocalDate today = LocalDate.now();
        int mode = cboCheDoLoc == null ? 0 : cboCheDoLoc.getSelectedIndex();
        LocalDate fromDate;
        LocalDate toDate;

        if (mode == 1) {
            fromDate = txtTuNgay.getDateValue();
            if (fromDate == null) {
                fromDate = today;
            }
            toDate = fromDate;
        } else if (mode == 2) {
            int month = parseInt(String.valueOf(cboThang.getSelectedItem()), today.getMonthValue());
            int year = parseInt(String.valueOf(cboNam.getSelectedItem()), today.getYear());
            YearMonth selectedMonth = YearMonth.of(year, month);
            fromDate = selectedMonth.atDay(1);
            toDate = selectedMonth.atEndOfMonth();
        } else if (mode == 3) {
            int year = parseInt(String.valueOf(cboNam.getSelectedItem()), today.getYear());
            fromDate = LocalDate.of(year, 1, 1);
            toDate = LocalDate.of(year, 12, 31);
        } else {
            if (hasInvalidDateText(txtTuNgay) || hasInvalidDateText(txtDenNgay)) {
                if (showMessage) {
                    showInfo("Ngày lọc không hợp lệ. Định dạng đúng là dd/MM/yyyy.");
                }
                return null;
            }
            fromDate = txtTuNgay.getDateValue();
            toDate = txtDenNgay.getDateValue();
            if (fromDate == null) {
                fromDate = today.withDayOfMonth(1);
            }
            if (toDate == null) {
                toDate = today;
            }
        }

        if (fromDate.isAfter(toDate)) {
            if (showMessage) {
                showInfo("Từ ngày không được lớn hơn đến ngày.");
            }
            return null;
        }
        return new ReportDateRange(fromDate, toDate);
    }

    private boolean hasInvalidDateText(AppDatePickerField field) {
        return field != null && field.getDateValue() == null
                && field.getText() != null && !field.getText().trim().isEmpty();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static final class ReportDateRange {
        private final LocalDate fromDate;
        private final LocalDate toDate;

        private ReportDateRange(LocalDate fromDate, LocalDate toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    private static final class ServiceUsageRecord {
        private final String tenDichVu;
        private final int luotSuDung;
        private final int soLuong;
        private final long doanhThu;

        private ServiceUsageRecord(String tenDichVu, int luotSuDung, int soLuong, long doanhThu) {
            this.tenDichVu = tenDichVu;
            this.luotSuDung = luotSuDung;
            this.soLuong = soLuong;
            this.doanhThu = doanhThu;
        }
    }

    private static final class ServiceRevenueRecord {
        private final String thoiGian;
        private final int luotSuDung;
        private final long doanhThu;
        private final String ghiChu;

        private ServiceRevenueRecord(String thoiGian, int luotSuDung, long doanhThu, String ghiChu) {
            this.thoiGian = thoiGian;
            this.luotSuDung = luotSuDung;
            this.doanhThu = doanhThu;
            this.ghiChu = ghiChu;
        }
    }

    private final class ServiceChartPanel extends JPanel {
        private List<ServiceUsageRecord> records = new ArrayList<ServiceUsageRecord>();

        private ServiceChartPanel() {
            setOpaque(true);
            setBackground(PANEL_SOFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }

        private void setRecords(List<ServiceUsageRecord> records) {
            this.records = new ArrayList<ServiceUsageRecord>(records);
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
                for (ServiceUsageRecord record : records) {
                    maxValue = Math.max(maxValue, record.luotSuDung);
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
                    ServiceUsageRecord record = records.get(i);
                    int centerX = left + step * i + step / 2;
                    int barHeight = (int) Math.round((double) record.luotSuDung * chartHeight / (double) maxValue);
                    int barX = centerX - barWidth / 2;
                    int barY = top + chartHeight - barHeight;

                    g2.setColor(new Color(191, 219, 254));
                    g2.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
                    g2.setColor(BRAND_BLUE);
                    g2.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);

                    String value = String.valueOf(record.luotSuDung);
                    int valueWidth = g2.getFontMetrics().stringWidth(value);
                    g2.setColor(TEXT_PRIMARY);
                    g2.drawString(value, centerX - valueWidth / 2, barY - 6);

                    int labelWidth = g2.getFontMetrics().stringWidth(record.tenDichVu);
                    g2.setColor(TEXT_MUTED);
                    g2.drawString(record.tenDichVu, centerX - labelWidth / 2, top + chartHeight + 18);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawEmptyState(Graphics2D g2, int width, int height) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(BODY_FONT);
            String message = "Chưa có dữ liệu dịch vụ để hiển thị.";
            int textWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - textWidth) / 2, height / 2);
        }
    }
}
