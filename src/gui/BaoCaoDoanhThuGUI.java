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
import javax.swing.SwingConstants;
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
import java.util.ArrayList;
import java.util.List;

public class BaoCaoDoanhThuGUI extends JFrame {
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

    private final String username;
    private final String role;
    private final List<RevenueRecord> revenueRecords = createSampleData();

    private JPanel rootPanel;
    private JComboBox<String> cboCheDoLoc;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboNam;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblTongDoanhThu;
    private JLabel lblTongTienPhong;
    private JLabel lblTongTienDichVu;
    private JLabel lblTongThanhToan;
    private JLabel lblTongDoanhThuSub;
    private JLabel lblTongTienPhongSub;
    private JLabel lblTongTienDichVuSub;
    private JLabel lblTongThanhToanSub;

    private JTable tblDoanhThu;
    private DefaultTableModel tableModel;
    private RevenueChartPanel chartPanel;

    public BaoCaoDoanhThuGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoDoanhThuGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");

        setTitle("Báo cáo doanh thu - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadRevenueData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.BAO_CAO_DOANH_THU, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("BÁO CÁO DOANH THU"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi doanh thu phòng, dịch vụ và thanh toán theo kỳ báo cáo với biểu đồ trực quan.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Báo cáo doanh thu"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem báo cáo", BRAND_GREEN, Color.WHITE, e -> loadRevenueData(true)));
        card.add(createPrimaryButton("Xuất file", BRAND_BLUE, Color.WHITE, e -> showInfo("Đã sẵn sàng xuất báo cáo doanh thu.")));
        card.add(createPrimaryButton("Làm mới", new Color(15, 118, 110), Color.WHITE, e -> resetFilters()));
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
        txtTuKhoa.setPreferredSize(new Dimension(210, 34));
        txtTuKhoa.setToolTipText("Ngày hoặc nội dung ghi chú");

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

        lblTongDoanhThu = new JLabel();
        lblTongTienPhong = new JLabel();
        lblTongTienDichVu = new JLabel();
        lblTongThanhToan = new JLabel();
        lblTongDoanhThuSub = new JLabel();
        lblTongTienPhongSub = new JLabel();
        lblTongTienDichVuSub = new JLabel();
        lblTongThanhToanSub = new JLabel();

        panel.add(createSummaryCard("Tổng doanh thu", lblTongDoanhThu, lblTongDoanhThuSub, BRAND_BLUE));
        panel.add(createSummaryCard("Tổng tiền phòng", lblTongTienPhong, lblTongTienPhongSub, BRAND_GREEN));
        panel.add(createSummaryCard("Tổng tiền dịch vụ", lblTongTienDichVu, lblTongTienDichVuSub, BRAND_AMBER));
        panel.add(createSummaryCard("Tổng thanh toán", lblTongThanhToan, lblTongThanhToanSub, BRAND_INDIGO));
        return panel;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(buildChartCard(), BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        return center;
    }

    private JPanel buildChartCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Biểu đồ doanh thu theo ngày");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Cột thể hiện tổng doanh thu, đường thể hiện tổng thanh toán.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        chartPanel = new RevenueChartPanel();
        chartPanel.setPreferredSize(new Dimension(0, 320));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Bảng thống kê doanh thu");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chi tiết doanh thu theo thời gian để đối chiếu khi demo.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new String[]{"Thời gian", "Tiền phòng", "Tiền dịch vụ", "Tổng doanh thu", "Tổng thanh toán"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDoanhThu = new JTable(tableModel);
        tblDoanhThu.setFont(BODY_FONT);
        tblDoanhThu.setRowHeight(30);
        tblDoanhThu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDoanhThu.setGridColor(BORDER_SOFT);
        tblDoanhThu.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblDoanhThu);

        JScrollPane scrollPane = new JScrollPane(tblDoanhThu);
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

    private void loadRevenueData(boolean showMessage) {
        List<RevenueRecord> filtered = filterRecords();
        updateSummary(filtered);
        reloadTable(filtered);
        chartPanel.setRecords(filtered);
        if (showMessage) {
            showInfo("Đã cập nhật báo cáo doanh thu.");
        }
    }

    private List<RevenueRecord> filterRecords() {
        List<RevenueRecord> filtered = new ArrayList<RevenueRecord>();
        String keyword = txtTuKhoa == null ? "" : txtTuKhoa.getText().trim().toLowerCase();
        for (RevenueRecord record : revenueRecords) {
            if (!keyword.isEmpty() && !record.label.toLowerCase().contains(keyword)) {
                continue;
            }
            filtered.add(record);
        }
        return filtered;
    }

    private void updateSummary(List<RevenueRecord> records) {
        long tongDoanhThu = 0L;
        long tongTienPhong = 0L;
        long tongTienDichVu = 0L;
        long tongThanhToan = 0L;

        for (RevenueRecord record : records) {
            tongDoanhThu += record.tongDoanhThu;
            tongTienPhong += record.tienPhong;
            tongTienDichVu += record.tienDichVu;
            tongThanhToan += record.tongThanhToan;
        }

        lblTongDoanhThu.setText(formatMoney(tongDoanhThu));
        lblTongTienPhong.setText(formatMoney(tongTienPhong));
        lblTongTienDichVu.setText(formatMoney(tongTienDichVu));
        lblTongThanhToan.setText(formatMoney(tongThanhToan));

        lblTongDoanhThuSub.setText("Trung bình/ngày: " + formatMoney(records.isEmpty() ? 0L : tongDoanhThu / records.size()));
        lblTongTienPhongSub.setText("Tỷ trọng: " + percentText(tongTienPhong, tongDoanhThu));
        lblTongTienDichVuSub.setText("Tỷ trọng: " + percentText(tongTienDichVu, tongDoanhThu));
        lblTongThanhToanSub.setText("Hoàn tất: " + percentText(tongThanhToan, tongDoanhThu));
    }

    private void reloadTable(List<RevenueRecord> records) {
        tableModel.setRowCount(0);
        for (RevenueRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.label,
                    formatMoney(record.tienPhong),
                    formatMoney(record.tienDichVu),
                    formatMoney(record.tongDoanhThu),
                    formatMoney(record.tongThanhToan)
            });
        }
        if (!records.isEmpty()) {
            tblDoanhThu.setRowSelectionInterval(0, 0);
        }
    }

    private void resetFilters() {
        cboCheDoLoc.setSelectedIndex(0);
        cboThang.setSelectedIndex(3);
        cboNam.setSelectedIndex(0);
        txtTuNgay.setText("01/04/2026");
        txtDenNgay.setText("30/04/2026");
        txtTuKhoa.setText("");
        loadRevenueData(false);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "bao_cao_doanh_thu_view", new Runnable() {
            @Override
            public void run() {
                loadRevenueData(true);
            }
        });
        ScreenUIHelper.registerShortcut(this, "F2", "bao_cao_doanh_thu_export", new Runnable() {
            @Override
            public void run() {
                showInfo("Đã sẵn sàng xuất báo cáo doanh thu.");
            }
        });
        ScreenUIHelper.registerShortcut(this, "F5", "bao_cao_doanh_thu_refresh", new Runnable() {
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

    private String percentText(long part, long total) {
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
        JOptionPane.showMessageDialog(this, message, "Báo cáo doanh thu", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<RevenueRecord> createSampleData() {
        List<RevenueRecord> data = new ArrayList<RevenueRecord>();
        data.add(new RevenueRecord("01/04/2026", 124000000L, 18000000L, 142000000L, 138000000L));
        data.add(new RevenueRecord("02/04/2026", 131000000L, 21000000L, 152000000L, 149000000L));
        data.add(new RevenueRecord("03/04/2026", 128000000L, 19000000L, 147000000L, 143500000L));
        data.add(new RevenueRecord("04/04/2026", 146000000L, 26000000L, 172000000L, 168000000L));
        data.add(new RevenueRecord("05/04/2026", 154000000L, 28500000L, 182500000L, 178000000L));
        data.add(new RevenueRecord("06/04/2026", 149500000L, 24500000L, 174000000L, 170000000L));
        data.add(new RevenueRecord("07/04/2026", 158000000L, 30200000L, 188200000L, 184000000L));
        return data;
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    private static final class RevenueRecord {
        private final String label;
        private final long tienPhong;
        private final long tienDichVu;
        private final long tongDoanhThu;
        private final long tongThanhToan;

        private RevenueRecord(String label, long tienPhong, long tienDichVu, long tongDoanhThu, long tongThanhToan) {
            this.label = label;
            this.tienPhong = tienPhong;
            this.tienDichVu = tienDichVu;
            this.tongDoanhThu = tongDoanhThu;
            this.tongThanhToan = tongThanhToan;
        }
    }

    private final class RevenueChartPanel extends JPanel {
        private List<RevenueRecord> records = new ArrayList<RevenueRecord>();

        private RevenueChartPanel() {
            setOpaque(true);
            setBackground(PANEL_SOFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }

        private void setRecords(List<RevenueRecord> records) {
            this.records = new ArrayList<RevenueRecord>(records);
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
                int right = 24;
                int top = 18;
                int bottom = 52;
                int chartWidth = width - left - right;
                int chartHeight = height - top - bottom;

                if (chartWidth <= 0 || chartHeight <= 0 || records.isEmpty()) {
                    drawEmptyState(g2, width, height);
                    return;
                }

                long maxValue = 0L;
                for (RevenueRecord record : records) {
                    maxValue = Math.max(maxValue, Math.max(record.tongDoanhThu, record.tongThanhToan));
                }
                if (maxValue <= 0L) {
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
                    long axisValue = maxValue - (maxValue * i / 4);
                    int y = top + i * chartHeight / 4;
                    String text = shortMoney(axisValue);
                    int textWidth = g2.getFontMetrics().stringWidth(text);
                    g2.drawString(text, left - textWidth - 8, y + 4);
                }

                int step = chartWidth / records.size();
                int barWidth = Math.max(24, step / 2);
                int linePrevX = -1;
                int linePrevY = -1;

                for (int i = 0; i < records.size(); i++) {
                    RevenueRecord record = records.get(i);
                    int centerX = left + step * i + step / 2;

                    int barHeight = (int) Math.round((double) record.tongDoanhThu * chartHeight / (double) maxValue);
                    int barX = centerX - barWidth / 2;
                    int barY = top + chartHeight - barHeight;

                    g2.setColor(new Color(191, 219, 254));
                    g2.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
                    g2.setColor(BRAND_BLUE);
                    g2.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);

                    int lineY = top + chartHeight - (int) Math.round((double) record.tongThanhToan * chartHeight / (double) maxValue);
                    g2.setColor(BRAND_GREEN);
                    g2.fillOval(centerX - 4, lineY - 4, 8, 8);

                    if (linePrevX >= 0) {
                        g2.setStroke(new BasicStroke(2.2f));
                        g2.drawLine(linePrevX, linePrevY, centerX, lineY);
                    }
                    linePrevX = centerX;
                    linePrevY = lineY;

                    g2.setColor(TEXT_PRIMARY);
                    g2.setFont(LABEL_FONT);
                    String label = record.label.substring(0, 5);
                    int labelWidth = g2.getFontMetrics().stringWidth(label);
                    g2.drawString(label, centerX - labelWidth / 2, top + chartHeight + 18);
                }

                drawLegend(g2, left, height - 22);
            } finally {
                g2.dispose();
            }
        }

        private void drawLegend(Graphics2D g2, int x, int y) {
            g2.setFont(LABEL_FONT);

            g2.setColor(BRAND_BLUE);
            g2.fillRoundRect(x, y - 8, 18, 10, 6, 6);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Tổng doanh thu", x + 24, y);

            int nextX = x + 140;
            g2.setColor(BRAND_GREEN);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawLine(nextX, y - 3, nextX + 18, y - 3);
            g2.fillOval(nextX + 7, y - 7, 8, 8);
            g2.setColor(TEXT_MUTED);
            g2.drawString("Tổng thanh toán", nextX + 28, y);
        }

        private void drawEmptyState(Graphics2D g2, int width, int height) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(BODY_FONT);
            String message = "Chưa có dữ liệu doanh thu để hiển thị.";
            int textWidth = g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, (width - textWidth) / 2, height / 2);
        }

        private String shortMoney(long value) {
            if (value >= 1000000000L) {
                return (value / 1000000000L) + " tỷ";
            }
            return (value / 1000000L) + " tr";
        }
    }
}
