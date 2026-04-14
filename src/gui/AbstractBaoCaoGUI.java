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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

public abstract class AbstractBaoCaoGUI extends JFrame {
    protected static final Color APP_BG = new Color(243, 244, 246);
    protected static final Color CARD_BG = Color.WHITE;
    protected static final Color PANEL_SOFT = new Color(249, 250, 251);
    protected static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    protected static final Color TEXT_MUTED = new Color(107, 114, 128);
    protected static final Color BORDER_SOFT = new Color(229, 231, 235);
    protected static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    protected static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    protected static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    protected static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private final String username;
    private final String role;
    private final ScreenKey currentScreen;
    private final String frameTitle;
    private final String pageTitle;
    private final String pageSubtitle;
    private final String tableTitle;
    private final MetricItem[] metrics;
    private final InfoItem[] highlights;
    private final String[] tableColumns;
    private final Object[][] tableRows;
    private final String[] shortcuts;

    private JPanel rootPanel;
    private JTable reportTable;
    private JComboBox<String> cboKyBaoCao;
    private JComboBox<String> cboNhomTheo;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTimKiem;
    private TableRowSorter<DefaultTableModel> reportTableSorter;

    protected AbstractBaoCaoGUI(String username, String role, ScreenKey currentScreen,
                                String frameTitle, String pageTitle, String pageSubtitle,
                                String tableTitle, MetricItem[] metrics, InfoItem[] highlights,
                                String[] tableColumns, Object[][] tableRows, String... shortcuts) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Quản lý");
        this.currentScreen = currentScreen;
        this.frameTitle = frameTitle;
        this.pageTitle = pageTitle;
        this.pageSubtitle = pageSubtitle;
        this.tableTitle = tableTitle;
        this.metrics = metrics;
        this.highlights = highlights;
        this.tableColumns = tableColumns;
        this.tableRows = tableRows;
        this.shortcuts = shortcuts;

        setTitle(frameTitle + " - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(SidebarFactory.createSidebar(this, currentScreen, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle(pageTitle));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel(pageSubtitle);
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, frameTitle), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Xem chi tiết", new Color(22, 163, 74), Color.WHITE,
                e -> showInfo("Đang mở dữ liệu chi tiết cho " + frameTitle.toLowerCase() + ".")));
        card.add(createPrimaryButton("Xuất file", new Color(37, 99, 235), Color.WHITE,
                e -> showInfo("Đã sẵn sàng xuất file cho " + frameTitle.toLowerCase() + ".")));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboKyBaoCao = createComboBox(new String[]{"Hôm nay", "Tuần này", "Tháng này", "Quý này", "Năm nay"});
        cboNhomTheo = createComboBox(new String[]{"Ngày", "Tuần", "Tháng", "Loại"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Đang hoạt động", "Cần theo dõi", "Nổi bật"});

        left.add(createFieldGroup("Kỳ báo cáo", cboKyBaoCao));
        left.add(createFieldGroup("Nhóm theo", cboNhomTheo));
        left.add(createFieldGroup("Trạng thái", cboTrangThai));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblSearch = new JLabel("Tìm nhanh");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);

        txtTimKiem = createInputField("");
        ScreenUIHelper.applySearchFieldSize(txtTimKiem);
        ScreenUIHelper.installLiveSearch(txtTimKiem, this::applySearchFilter);

        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));
        right.add(txtTimKiem);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(buildMetricsCard(), BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.add(buildHighlightsCard(), BorderLayout.WEST);
        bottom.add(buildTableCard(), BorderLayout.CENTER);

        center.add(bottom, BorderLayout.CENTER);
        return center;
    }

    private JPanel buildMetricsCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));
        JLabel lblTitle = new JLabel("Tổng quan nhanh");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel grid = new JPanel(new java.awt.GridLayout(1, metrics.length, 10, 0));
        grid.setOpaque(false);
        for (MetricItem metric : metrics) {
            grid.add(createMetricCard(metric));
        }

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHighlightsCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(320, 0));

        JLabel lblTitle = new JLabel("Điểm nhấn");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        for (int i = 0; i < highlights.length; i++) {
            content.add(createHighlightRow(highlights[i]));
            if (i < highlights.length - 1) {
                content.add(Box.createVerticalStrut(8));
            }
        }

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JLabel lblTitle = new JLabel(tableTitle);
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        DefaultTableModel tableModel = new DefaultTableModel(tableColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : tableRows) {
            tableModel.addRow(row);
        }

        reportTable = new JTable(tableModel);
        reportTableSorter = new TableRowSorter<DefaultTableModel>(tableModel);
        reportTable.setRowSorter(reportTableSorter);
        reportTable.setFont(BODY_FONT);
        reportTable.setRowHeight(30);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setGridColor(BORDER_SOFT);
        reportTable.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(CARD_BG, BORDER_SOFT, TEXT_MUTED, shortcuts);
    }

    private JPanel createMetricCard(MetricItem metric) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(PANEL_SOFT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setBackground(metric.accentColor);
        dot.setPreferredSize(new Dimension(10, 10));

        JLabel lblLabel = new JLabel(metric.label);
        lblLabel.setFont(BODY_FONT);
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(metric.value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(TEXT_PRIMARY);

        JLabel lblNote = new JLabel(metric.note);
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);

        top.add(dot);
        top.add(Box.createHorizontalStrut(8));
        top.add(lblLabel);

        card.add(top, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblNote, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createHighlightRow(InfoItem item) {
        JPanel row = new JPanel(new BorderLayout(0, 6));
        row.setBackground(PANEL_SOFT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTitle = new JLabel(item.title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblValue = new JLabel(item.value);
        lblValue.setFont(BODY_FONT);
        lblValue.setForeground(TEXT_MUTED);

        row.add(lblTitle, BorderLayout.NORTH);
        row.add(lblValue, BorderLayout.CENTER);
        return row;
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
        comboBox.setMaximumSize(new Dimension(180, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(160, 34));
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

    protected JPanel createCardPanel(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return panel;
    }

    private void applySearchFilter() {
        if (reportTableSorter == null || txtTimKiem == null) {
            return;
        }
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().trim();
        if (keyword.isEmpty()) {
            reportTableSorter.setRowFilter(null);
            return;
        }
        reportTableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(keyword)));
    }

    protected JPanel createCompactCardPanel(FlowLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return panel;
    }

    private void resetFilters() {
        cboKyBaoCao.setSelectedIndex(0);
        cboNhomTheo.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTimKiem.setText("");
        if (reportTable.getRowCount() > 0) {
            reportTable.setRowSelectionInterval(0, 0);
        }
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", currentScreen.name() + "_detail",
                new Runnable() {
                    @Override
                    public void run() {
                        showInfo("Đang mở dữ liệu chi tiết cho " + frameTitle.toLowerCase() + ".");
                    }
                });
        ScreenUIHelper.registerShortcut(this, "F2", currentScreen.name() + "_export",
                new Runnable() {
                    @Override
                    public void run() {
                        showInfo("Đã sẵn sàng xuất file cho " + frameTitle.toLowerCase() + ".");
                    }
                });
        ScreenUIHelper.registerShortcut(this, "F5", currentScreen.name() + "_refresh",
                new Runnable() {
                    @Override
                    public void run() {
                        resetFilters();
                    }
                });
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, frameTitle, JOptionPane.INFORMATION_MESSAGE);
    }

    protected String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    protected static final class MetricItem {
        private final String label;
        private final String value;
        private final String note;
        private final Color accentColor;

        protected MetricItem(String label, String value, String note, Color accentColor) {
            this.label = label;
            this.value = value;
            this.note = note;
            this.accentColor = accentColor;
        }
    }

    protected static final class InfoItem {
        private final String title;
        private final String value;

        protected InfoItem(String title, String value) {
            this.title = title;
            this.value = value;
        }
    }
}
