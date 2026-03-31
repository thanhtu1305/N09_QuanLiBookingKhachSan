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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

public class BaoCaoGUI extends JFrame {
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

    private JComboBox<String> cboNgay;
    private JComboBox<String> cboThang;
    private JComboBox<String> cboQuy;
    private JComboBox<String> cboNam;
    private JComboBox<String> cboLoaiBaoCao;
    private JTextField txtTimNhanh;
    private JTable tblThongKe;
    private DefaultTableModel tableModel;

    private ReportFilter currentFilter = ReportFilter.defaultFilter();

    public BaoCaoGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");

        setTitle("Báo cáo và thống kê - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        reloadSampleData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(SidebarFactory.createSidebar(this, ScreenKey.BAO_CAO, username, role), BorderLayout.WEST);
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
        main.add(buildBodyContent(), BorderLayout.CENTER);
        main.add(buildFooterHints(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildHeader() {
        JPanel card = createCardPanel(new BorderLayout());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("BÁO CÁO VÀ THỐNG KÊ"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý chỉ số công suất phòng, doanh thu, dịch vụ sử dụng nhiều và tình trạng phòng realtime.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Báo cáo"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem báo cáo", new Color(22, 163, 74), Color.WHITE, e -> openReportFilterDialog()));
        card.add(createPrimaryButton("Xuất file", new Color(37, 99, 235), Color.WHITE, e -> openExportReportDialog()));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboNgay = createComboBox(new String[]{"Hôm nay", "01", "02", "03", "04", "05", "06", "07"});
        cboThang = createComboBox(new String[]{"03", "01", "02", "04", "05", "06"});
        cboQuy = createComboBox(new String[]{"Q1", "Q2", "Q3", "Q4"});
        cboNam = createComboBox(new String[]{"2026", "2025", "2024"});
        cboLoaiBaoCao = createComboBox(new String[]{"Tổng hợp", "Doanh thu", "Công suất phòng", "Dịch vụ", "Tình trạng phòng"});

        left.add(createFieldGroup("Ngày", cboNgay));
        left.add(createFieldGroup("Tháng", cboThang));
        left.add(createFieldGroup("Quý", cboQuy));
        left.add(createFieldGroup("Năm", cboNam));
        left.add(createFieldGroup("Loại báo cáo", cboLoaiBaoCao));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblSearch = new JLabel("Ô tìm nhanh");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);

        txtTimNhanh = createInputField("03/2026");
        txtTimNhanh.setPreferredSize(new Dimension(150, 34));

        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));
        right.add(txtTimNhanh);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildBodyContent() {
        JPanel body = new JPanel(new GridLayout(1, 2, 12, 0));
        body.setOpaque(false);
        body.add(buildSummaryColumn());
        body.add(buildDetailColumn());
        return body;
    }

    private JPanel buildSummaryColumn() {
        JPanel column = new JPanel(new BorderLayout(0, 12));
        column.setOpaque(false);

        JPanel section = createCardPanel(new BorderLayout(0, 10));
        JLabel lblTitle = new JLabel("Báo cáo tổng hợp");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel metrics = new JPanel(new GridLayout(5, 1, 0, 10));
        metrics.setOpaque(false);
        metrics.add(createMetricCard("Công suất phòng hôm nay", "82%", new Color(37, 99, 235), this::openOccupancyReportDialog));
        metrics.add(createMetricCard("Doanh thu ngày", "186.500.000", new Color(22, 163, 74), this::openRevenueReportDialog));
        metrics.add(createMetricCard("Doanh thu tháng", "4.280.000.000", new Color(15, 118, 110), this::openRevenueReportDialog));
        metrics.add(createMetricCard("Dịch vụ sử dụng nhiều nhất", "Bữa sáng", new Color(245, 158, 11), this::openTopServicesReportDialog));
        metrics.add(createMetricCard("Phòng đang ở", "48 phòng", new Color(220, 38, 38), this::openRoomStatusReportDialog));

        section.add(lblTitle, BorderLayout.NORTH);
        section.add(metrics, BorderLayout.CENTER);

        column.add(section, BorderLayout.CENTER);
        return column;
    }

    private JPanel createMetricCard(String label, String value, Color accent, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(PANEL_SOFT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel dot = new JPanel();
        dot.setBackground(accent);
        dot.setPreferredSize(new Dimension(10, 10));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(BODY_FONT);
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(TEXT_PRIMARY);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        top.add(dot);
        top.add(Box.createHorizontalStrut(8));
        top.add(lblLabel);

        card.add(top, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        attachClick(card, onClick);
        attachClick(lblValue, onClick);
        attachClick(lblLabel, onClick);
        return card;
    }

    private JPanel buildDetailColumn() {
        JPanel column = new JPanel(new BorderLayout(0, 12));
        column.setOpaque(false);
        column.add(buildFloorMapCard(), BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(2, 1, 0, 12));
        lower.setOpaque(false);
        lower.add(buildTopServiceCard());
        lower.add(buildStatisticsTableCard());

        column.add(lower, BorderLayout.CENTER);
        return column;
    }

    private JPanel buildFloorMapCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Tình trạng phòng realtime");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(createFloorRow("Tầng 1", new String[]{"101 T", "102 O", "103 C", "104 B"}));
        content.add(Box.createVerticalStrut(10));
        content.add(createFloorRow("Tầng 2", new String[]{"201 T", "202 D", "203 O", "204 T"}));
        content.add(Box.createVerticalStrut(12));
        content.add(buildStatusLegend());

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        attachClick(card, this::openRoomStatusReportDialog);
        attachClick(lblTitle, this::openRoomStatusReportDialog);
        return card;
    }

    private JPanel createFloorRow(String floorLabel, String[] rooms) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JLabel lblFloor = new JLabel(floorLabel + ":");
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFloor.setForeground(TEXT_PRIMARY);
        lblFloor.setPreferredSize(new Dimension(58, 30));
        row.add(lblFloor);

        for (String room : rooms) {
            row.add(createRoomStatusChip(room));
        }
        return row;
    }

    private JPanel createRoomStatusChip(String text) {
        String status = text.substring(text.length() - 1);
        Color color;
        if ("T".equals(status)) {
            color = new Color(34, 197, 94);
        } else if ("D".equals(status)) {
            color = new Color(245, 158, 11);
        } else if ("O".equals(status)) {
            color = new Color(59, 130, 246);
        } else if ("C".equals(status)) {
            color = new Color(168, 85, 247);
        } else {
            color = new Color(220, 38, 38);
        }

        JPanel chip = new JPanel(new BorderLayout());
        chip.setBackground(color);
        chip.setPreferredSize(new Dimension(68, 30));
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        chip.add(lbl, BorderLayout.CENTER);
        return chip;
    }

    private JPanel buildStatusLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem("T = Trống", new Color(34, 197, 94)));
        legend.add(createLegendItem("D = Đã đặt", new Color(245, 158, 11)));
        legend.add(createLegendItem("O = Đang ở", new Color(59, 130, 246)));
        legend.add(createLegendItem("C = Dọn dẹp", new Color(168, 85, 247)));
        legend.add(createLegendItem("B = Bảo trì", new Color(220, 38, 38)));
        return legend;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(12, 12));

        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_MUTED);

        panel.add(dot);
        panel.add(label);
        return panel;
    }

    private JPanel buildTopServiceCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Top dịch vụ");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel list = new JPanel(new GridLayout(3, 1, 0, 8));
        list.setOpaque(false);
        list.add(createTopServiceRow("1. Bữa sáng", "120 lần"));
        list.add(createTopServiceRow("2. Giặt ủi", "80 lần"));
        list.add(createTopServiceRow("3. Mini bar", "64 lần"));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(list, BorderLayout.CENTER);
        attachClick(card, this::openTopServicesReportDialog);
        attachClick(lblTitle, this::openTopServicesReportDialog);
        return card;
    }

    private JPanel createTopServiceRow(String name, String count) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(PANEL_SOFT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(TEXT_PRIMARY);

        JLabel lblCount = new JLabel(count);
        lblCount.setFont(BODY_FONT);
        lblCount.setForeground(TEXT_MUTED);

        row.add(lblName, BorderLayout.WEST);
        row.add(lblCount, BorderLayout.EAST);
        return row;
    }

    private JPanel buildStatisticsTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel("Bảng thống kê theo ngày");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        String[] columns = {"Ngày", "Booking", "CI", "CO", "Doanh thu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblThongKe = new JTable(tableModel);
        tblThongKe.setFont(BODY_FONT);
        tblThongKe.setRowHeight(30);
        tblThongKe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblThongKe.setGridColor(BORDER_SOFT);
        tblThongKe.setFillsViewportHeight(true);
        tblThongKe.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblThongKe.getTableHeader().setBackground(new Color(243, 244, 246));
        tblThongKe.getTableHeader().setForeground(TEXT_PRIMARY);
        tblThongKe.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openRevenueReportDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblThongKe);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooterHints() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Xem BC",
                "F2 Xuất file"
        );
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
        comboBox.setPreferredSize(new Dimension(130, 34));
        comboBox.setMaximumSize(new Dimension(180, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(150, 34));
        field.setMaximumSize(new Dimension(220, 34));
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

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private void reloadSampleData(boolean showMessage) {
        currentFilter = ReportFilter.defaultFilter();
        if (cboNgay != null) {
            cboNgay.setSelectedIndex(0);
            cboThang.setSelectedItem("03");
            cboQuy.setSelectedItem("Q1");
            cboNam.setSelectedItem("2026");
            cboLoaiBaoCao.setSelectedItem("Tổng hợp");
            txtTimNhanh.setText("03/2026");
        }
        refillStatisticsTable();
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu báo cáo mẫu.");
        }
    }

    private void refillStatisticsTable() {
        if (tableModel == null) {
            return;
        }
        tableModel.setRowCount(0);
    }

    private void openReportFilterDialog() {
        new ReportFilterDialog(this).setVisible(true);
    }

    private void openExportReportDialog() {
        new ExportReportDialog(this, currentFilter).setVisible(true);
    }

    private void openOccupancyReportDialog() {
        new OccupancyReportDialog(this, currentFilter).setVisible(true);
    }

    private void openRevenueReportDialog() {
        new RevenueReportDialog(this, currentFilter).setVisible(true);
    }

    private void openTopServicesReportDialog() {
        new TopServicesReportDialog(this, currentFilter).setVisible(true);
    }

    private void openRoomStatusReportDialog() {
        new RoomStatusReportDialog(this).setVisible(true);
    }

    private void applyReportFilter(ReportFilter filter, boolean printAfter) {
        currentFilter = filter;
        cboLoaiBaoCao.setSelectedItem(filter.loaiBaoCao);
        txtTimNhanh.setText(filter.tuNgay + " - " + filter.denNgay);
        refillStatisticsTable();
        showSuccess(printAfter ? "Tạo báo cáo thành công và sẵn sàng in." : "Tạo báo cáo thành công.");

        if ("Công suất phòng".equals(filter.loaiBaoCao)) {
            openOccupancyReportDialog();
        } else if ("Doanh thu".equals(filter.loaiBaoCao)) {
            openRevenueReportDialog();
        } else if ("Dịch vụ sử dụng nhiều".equals(filter.loaiBaoCao)) {
            openTopServicesReportDialog();
        } else if ("Tình trạng phòng realtime".equals(filter.loaiBaoCao)) {
            openRoomStatusReportDialog();
        }
    }

    private void attachClick(Component component, Runnable action) {
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }
        });
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }

    private void showSuccess(String message) {
        showMessageDialog("Thành công", message, new Color(22, 163, 74));
    }

    private void showWarning(String message) {
        showMessageDialog("Thông báo", message, new Color(245, 158, 11));
    }

    private void showMessageDialog(String title, String message, Color accentColor) {
        MessageDialog dialog = new MessageDialog(this, title, message, accentColor);
        dialog.setVisible(true);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "baocao-f1", this::openReportFilterDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "baocao-f2", this::openExportReportDialog);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private abstract class BaseReportDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseReportDialog(Frame owner, String title, int width, int height) {
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

    private final class ReportFilterDialog extends BaseReportDialog {
        private JComboBox<String> cboLoaiBaoCaoDialog;
        private AppDatePickerField txtTuNgay;
        private AppDatePickerField txtDenNgay;
        private JComboBox<String> cboNhomTheo;
        private JComboBox<String> cboLoaiPhongDialog;
        private JComboBox<String> cboTrangThaiPhong;
        private JComboBox<String> cboChiTietTheo;
        private JComboBox<String> cboGomDoanhThuDichVu;
        private JComboBox<String> cboGomDoanhThuPhong;
        private JTextArea txtGhiChu;

        private ReportFilterDialog(Frame owner) {
            super(owner, "Xem báo cáo", 680, 620);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "XEM BÁO CÁO",
                    "Chọn loại báo cáo và khoảng thời gian để xem chỉ số tổng hợp hoặc drill-down chi tiết."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboLoaiBaoCaoDialog = createComboBox(new String[]{"", "Công suất phòng", "Doanh thu", "Dịch vụ sử dụng nhiều", "Tình trạng phòng realtime"});
            txtTuNgay = new AppDatePickerField(currentFilter.tuNgay, true);
            txtDenNgay = new AppDatePickerField(currentFilter.denNgay, true);
            cboNhomTheo = createComboBox(new String[]{"Ngày", "Tháng", "Loại phòng"});
            cboLoaiPhongDialog = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite"});
            cboTrangThaiPhong = createComboBox(new String[]{"Tất cả", "Trống", "Đã đặt", "Đang ở", "Dọn dẹp", "Bảo trì"});
            cboChiTietTheo = createComboBox(new String[]{"Tổng hợp", "Ngày", "Tháng", "Phòng"});
            cboGomDoanhThuDichVu = createComboBox(new String[]{"Có", "Không"});
            cboGomDoanhThuPhong = createComboBox(new String[]{"Có", "Không"});
            txtGhiChu = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Loại báo cáo", cboLoaiBaoCaoDialog);
            addFormRow(form, gbc, 1, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 2, "Đến ngày", txtDenNgay);
            addFormRow(form, gbc, 3, "Nhóm theo", cboNhomTheo);
            addFormRow(form, gbc, 4, "Loại phòng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 5, "Trạng thái phòng", cboTrangThaiPhong);
            addFormRow(form, gbc, 6, "Chi tiết theo", cboChiTietTheo);
            addFormRow(form, gbc, 7, "Gom doanh thu dịch vụ", cboGomDoanhThuDichVu);
            addFormRow(form, gbc, 8, "Gom doanh thu phòng", cboGomDoanhThuPhong);
            addFormRow(form, gbc, 9, "Ghi chú", new JScrollPane(txtGhiChu));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createOutlineButton("Xem và in", new Color(37, 99, 235), e -> submit(true)),
                    createPrimaryButton("Xem", new Color(22, 163, 74), Color.WHITE, e -> submit(false))
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean printAfter) {
            String loaiBaoCao = valueOf(cboLoaiBaoCaoDialog.getSelectedItem());
            if (loaiBaoCao.isEmpty() || txtTuNgay.getText().trim().isEmpty() || txtDenNgay.getText().trim().isEmpty()) {
                showWarning("Vui lòng chọn loại báo cáo và khoảng thời gian hợp lệ.");
                return;
            }
            if (txtTuNgay.getDateValue() == null || txtDenNgay.getDateValue() == null) {
                showWarning("Ngày báo cáo phải đúng định dạng dd/MM/yyyy.");
                return;
            }
            if (txtTuNgay.getDateValue() != null && txtDenNgay.getDateValue() != null
                    && txtTuNgay.getDateValue().isAfter(txtDenNgay.getDateValue())) {
                showWarning("Vui lòng chọn loại báo cáo và khoảng thời gian hợp lệ.");
                return;
            }
            ReportFilter filter = new ReportFilter();
            filter.loaiBaoCao = loaiBaoCao;
            filter.tuNgay = txtTuNgay.getText().trim();
            filter.denNgay = txtDenNgay.getText().trim();
            filter.nhomTheo = valueOf(cboNhomTheo.getSelectedItem());
            filter.loaiPhong = valueOf(cboLoaiPhongDialog.getSelectedItem());
            filter.trangThaiPhong = valueOf(cboTrangThaiPhong.getSelectedItem());
            filter.chiTietTheo = valueOf(cboChiTietTheo.getSelectedItem());
            filter.gomDoanhThuDichVu = valueOf(cboGomDoanhThuDichVu.getSelectedItem());
            filter.gomDoanhThuPhong = valueOf(cboGomDoanhThuPhong.getSelectedItem());
            filter.ghiChu = txtGhiChu.getText().trim();
            dispose();
            applyReportFilter(filter, printAfter);
        }
    }

    private final class ExportReportDialog extends BaseReportDialog {
        private JTextField txtLoaiBaoCao;
        private JTextField txtTuNgay;
        private JTextField txtDenNgay;
        private JComboBox<String> cboLoaiFile;
        private JTextField txtTenFile;
        private JTextField txtThuMuc;
        private JTextArea txtGhiChu;

        private ExportReportDialog(Frame owner, ReportFilter filter) {
            super(owner, "Xuất file báo cáo", 620, 500);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "XUẤT FILE BÁO CÁO",
                    "Chọn định dạng file và thông tin lưu để xuất báo cáo."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtLoaiBaoCao = createInputField(filter.loaiBaoCao);
            txtLoaiBaoCao.setEditable(false);
            txtTuNgay = createInputField(filter.tuNgay);
            txtTuNgay.setEditable(false);
            txtDenNgay = createInputField(filter.denNgay);
            txtDenNgay.setEditable(false);
            cboLoaiFile = createComboBox(new String[]{"Excel", "PDF"});
            txtTenFile = createInputField("bao-cao-" + filter.loaiBaoCao.toLowerCase(Locale.ROOT).replace(' ', '-'));
            txtThuMuc = createInputField("/reports");
            txtGhiChu = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Loại báo cáo", txtLoaiBaoCao);
            addFormRow(form, gbc, 1, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 2, "Đến ngày", txtDenNgay);
            addFormRow(form, gbc, 3, "Loại file", cboLoaiFile);
            addFormRow(form, gbc, 4, "Tên file", txtTenFile);
            addFormRow(form, gbc, 5, "Thư mục lưu", txtThuMuc);
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(txtGhiChu));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xuất", new Color(37, 99, 235), Color.WHITE, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (txtLoaiBaoCao.getText().trim().isEmpty() || txtTuNgay.getText().trim().isEmpty() || txtDenNgay.getText().trim().isEmpty()) {
                showWarning("Vui lòng chọn loại báo cáo và khoảng thời gian hợp lệ.");
                return;
            }
            showSuccess("Xuất báo cáo thành công.");
            dispose();
        }
    }

    private final class OccupancyReportDialog extends BaseReportDialog {
        private OccupancyReportDialog(Frame owner, ReportFilter filter) {
            super(owner, "Chi tiết công suất phòng", 820, 560);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.add(buildDialogHeader("CHI TIẾT CÔNG SUẤT PHÒNG", "Drill-down công suất phòng theo ngày trong kỳ báo cáo."));
            top.add(Box.createVerticalStrut(10));
            top.add(buildInfoCard(filter));
            content.add(top, BorderLayout.NORTH);

            String[] columns = {"STT", "Ngày", "Tổng phòng kinh doanh", "Đang ở", "Trống", "Bảo trì", "Công suất"};
            JTable table = createReportTable(columns, new Object[][]{
                    {1, "05/03/2026", "58", "46", "9", "3", "79%"},
                    {2, "06/03/2026", "58", "47", "8", "3", "81%"},
                    {3, "07/03/2026", "58", "49", "6", "3", "84%"},
                    {4, "08/03/2026", "58", "48", "7", "3", "82%"}
            });

            JPanel summary = createDialogCardPanel();
            summary.add(createValueLabel("Công suất trung bình kỳ báo cáo: 82%"), BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(new JScrollPane(table), BorderLayout.CENTER);
            center.add(summary, BorderLayout.SOUTH);
            content.add(center, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xuất file", new Color(37, 99, 235), Color.WHITE, e -> showSuccess("Xuất báo cáo thành công."))
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class RevenueReportDialog extends BaseReportDialog {
        private RevenueReportDialog(Frame owner, ReportFilter filter) {
            super(owner, "Chi tiết doanh thu", 860, 560);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.add(buildDialogHeader("CHI TIẾT DOANH THU", "Drill-down doanh thu phòng và doanh thu dịch vụ theo kỳ báo cáo."));
            top.add(Box.createVerticalStrut(10));
            top.add(buildInfoCard(filter));
            content.add(top, BorderLayout.NORTH);

            String[] columns = {"STT", "Ngày", "Doanh thu phòng", "Doanh thu dịch vụ", "Giảm giá", "Thực thu"};
            JTable table = createReportTable(columns, new Object[][]{
                    {1, "05/03/2026", "132.000.000", "20.000.000", "0", "152.000.000"},
                    {2, "06/03/2026", "139.500.000", "24.000.000", "2.000.000", "161.500.000"},
                    {3, "07/03/2026", "151.000.000", "27.000.000", "4.000.000", "174.000.000"},
                    {4, "08/03/2026", "158.000.000", "29.200.000", "4.000.000", "183.200.000"}
            });

            JPanel summary = createDialogCardPanel();
            summary.add(createValueLabel("Tổng doanh thu kỳ báo cáo: 670.700.000"), BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(new JScrollPane(table), BorderLayout.CENTER);
            center.add(summary, BorderLayout.SOUTH);
            content.add(center, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createOutlineButton("Xuất file", new Color(37, 99, 235), e -> showSuccess("Xuất báo cáo thành công.")),
                    createPrimaryButton("Xem hóa đơn", new Color(99, 102, 241), Color.WHITE, e -> showSuccess("Đang mở danh sách hóa đơn liên quan (mô phỏng)."))
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class TopServicesReportDialog extends BaseReportDialog {
        private TopServicesReportDialog(Frame owner, ReportFilter filter) {
            super(owner, "Chi tiết dịch vụ sử dụng nhiều", 760, 520);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.add(buildDialogHeader("CHI TIẾT DỊCH VỤ SỬ DỤNG NHIỀU", "Drill-down nhóm dịch vụ nổi bật trong kỳ báo cáo."));
            top.add(Box.createVerticalStrut(10));
            top.add(buildInfoCard(filter));
            content.add(top, BorderLayout.NORTH);

            String[] columns = {"STT", "Dịch vụ", "Số lần sử dụng", "Tổng số lượng", "Doanh thu"};
            JTable table = createReportTable(columns, new Object[][]{
                    {1, "Bữa sáng", "120", "184", "92.000.000"},
                    {2, "Giặt ủi", "80", "126", "41.500.000"},
                    {3, "Mini bar", "64", "99", "36.000.000"}
            });

            JPanel summary = createDialogCardPanel();
            summary.add(createValueLabel("Dịch vụ nổi bật nhất: Bữa sáng"), BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(new JScrollPane(table), BorderLayout.CENTER);
            center.add(summary, BorderLayout.SOUTH);
            content.add(center, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xuất file", new Color(37, 99, 235), Color.WHITE, e -> showSuccess("Xuất báo cáo thành công."))
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class RoomStatusReportDialog extends BaseReportDialog {
        private RoomStatusReportDialog(Frame owner) {
            super(owner, "Tình trạng phòng realtime", 720, 500);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("TÌNH TRẠNG PHÒNG REALTIME", "Minh họa tình trạng phòng theo tầng tại thời điểm xem."), BorderLayout.NORTH);

            JPanel info = createDialogCardPanel();
            info.add(createValueLabel("Thời điểm xem: 19/03/2026 10:35"), BorderLayout.CENTER);

            JPanel grid = new JPanel();
            grid.setOpaque(false);
            grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
            grid.add(createFloorRow("Tầng 1", new String[]{"101 T", "102 O", "103 C", "104 B"}));
            grid.add(Box.createVerticalStrut(10));
            grid.add(createFloorRow("Tầng 2", new String[]{"201 T", "202 D", "203 O", "204 T"}));
            grid.add(Box.createVerticalStrut(10));
            grid.add(createFloorRow("Tầng 3", new String[]{"301 O", "302 T", "303 O", "304 C"}));
            grid.add(Box.createVerticalStrut(12));
            grid.add(buildStatusLegend());

            JPanel card = createDialogCardPanel();
            card.add(grid, BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(info, BorderLayout.NORTH);
            center.add(card, BorderLayout.CENTER);
            content.add(center, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xem chi tiết phòng", new Color(99, 102, 241), Color.WHITE, e -> showSuccess("Đang mở chi tiết phòng từ sơ đồ realtime (mô phỏng)."))
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JPanel buildInfoCard(ReportFilter filter) {
        JPanel info = createDialogCardPanel();
        JPanel form = createDialogFormPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;
        addFormRow(form, gbc, 0, "Loại báo cáo", createValueLabel(filter.loaiBaoCao));
        addFormRow(form, gbc, 1, "Từ ngày", createValueLabel(filter.tuNgay));
        addFormRow(form, gbc, 2, "Đến ngày", createValueLabel(filter.denNgay));
        info.add(form, BorderLayout.CENTER);
        return info;
    }

    private JTable createReportTable(String[] columns, Object[][] rows) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : rows) {
            model.addRow(row);
        }
        JTable table = new JTable(model);
        table.setFont(BODY_FONT);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_SOFT);
        return table;
    }

    private final class MessageDialog extends BaseReportDialog {
        private MessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class ReportFilter {
        private String loaiBaoCao;
        private String tuNgay;
        private String denNgay;
        private String nhomTheo;
        private String loaiPhong;
        private String trangThaiPhong;
        private String chiTietTheo;
        private String gomDoanhThuDichVu;
        private String gomDoanhThuPhong;
        private String ghiChu;

        private static ReportFilter defaultFilter() {
            ReportFilter filter = new ReportFilter();
            filter.loaiBaoCao = "Tổng hợp";
            filter.tuNgay = "01/03/2026";
            filter.denNgay = "31/03/2026";
            filter.nhomTheo = "Ngày";
            filter.loaiPhong = "Tất cả";
            filter.trangThaiPhong = "Tất cả";
            filter.chiTietTheo = "Tổng hợp";
            filter.gomDoanhThuDichVu = "Có";
            filter.gomDoanhThuPhong = "Có";
            filter.ghiChu = "";
            return filter;
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
