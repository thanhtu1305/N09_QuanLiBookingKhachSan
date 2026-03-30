package gui;

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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;

public class DashboardGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final String WORKING_DATE = "16/03/2026";

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    private final String username;
    private final String role;
    private JPanel rootPanel;

    private JTable tblCongViec;
    private DefaultTableModel taskTableModel;
    private JLabel lblChiTietMa;
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

        setTitle("Dashboard - Hotel PMS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
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

        JLabel lblSub = new JLabel("Theo dõi nhanh tình trạng vận hành, đặt phòng, check-in/check-out, thanh toán và trạng thái phòng.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Dashboard"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Đặt phòng", new Color(22, 163, 74), Color.WHITE, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.DAT_PHONG, username, role)));
        card.add(createPrimaryButton("Check-in", new Color(37, 99, 235), Color.WHITE, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role)));
        card.add(createPrimaryButton("Check-out", new Color(245, 158, 11), TEXT_PRIMARY, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.CHECK_IN_OUT, username, role)));
        card.add(createPrimaryButton("Thanh toán", new Color(220, 38, 38), Color.WHITE, e ->
                NavigationUtil.navigate(this, ScreenKey.DASHBOARD, ScreenKey.THANH_TOAN, username, role)));
        card.add(createPrimaryButton("Tìm nhanh", new Color(15, 118, 110), Color.WHITE, e ->
                showMessage("Mở tìm kiếm nhanh.")));
        return card;
    }

    private JPanel buildInfoRow() {
        JPanel card = createCardPanel(new GridLayout(1, 3, 12, 0));
        card.add(createInfoCell("Ngày làm việc", WORKING_DATE));
        card.add(createInfoCell("Người dùng", username));
        card.add(createInfoCell("Vai trò", role));
        return card;
    }

    private JPanel createInfoCell(String label, String value) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(LABEL_FONT);
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(TEXT_PRIMARY);

        cell.add(lblLabel);
        cell.add(Box.createVerticalStrut(4));
        cell.add(lblValue);
        return cell;
    }

    private JScrollPane buildCenterContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel kpiSection = buildKpiSection();
        kpiSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, kpiSection.getPreferredSize().height));

        // Wrap JSplitPane trong mot JPanel de can trai dung
        JSplitPane splitPane = buildMainSplitPanels();
        JPanel splitWrapper = new JPanel(new BorderLayout());
        splitWrapper.setOpaque(false);
        splitWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        splitWrapper.add(splitPane, BorderLayout.CENTER);
        content.add(kpiSection);
        content.add(Box.createVerticalStrut(12));
        content.add(splitWrapper);

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

        JLabel lblSub = new JLabel("Tổng hợp nhanh theo ca làm việc hiện tại.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 10));
        grid.setOpaque(false);

        grid.add(createKpiCard("Phòng trống", "12", "Sẵn sàng bán", new Color(220, 252, 231)));
        grid.add(createKpiCard("Đang ở", "18", "Khách đang lưu trú", new Color(219, 234, 254)));
        grid.add(createKpiCard("Đã đặt", "05", "Booking đã xác nhận", new Color(254, 249, 195)));
        grid.add(createKpiCard("Dọn dẹp", "04", "Phòng chờ vệ sinh", new Color(255, 237, 213)));
        grid.add(createKpiCard("Booking mới", "07", "Trong hôm nay", new Color(236, 253, 245)));
        grid.add(createKpiCard("Chờ check-in", "05", "Dự kiến nhận phòng", new Color(219, 234, 254)));
        grid.add(createKpiCard("Chờ check-out", "06", "Dự kiến trả phòng", new Color(254, 243, 199)));
        grid.add(createKpiCard("Chờ thanh toán", "03", "Hóa đơn chưa tất toán", new Color(254, 226, 226)));
        grid.add(createKpiCard("Doanh thu tạm tính ngày", "12.450.000", "VNĐ", new Color(238, 242, 255)));

        card.add(titleRow, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel createKpiCard(String label, String value, String note, Color badgeBg) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(PANEL_SOFT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 5, 10, 12)
        ));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(LABEL_FONT);
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(TEXT_PRIMARY);

        JLabel lblNote = new JLabel(note);
        lblNote.setFont(BODY_FONT);
        lblNote.setForeground(TEXT_PRIMARY);
        lblNote.setOpaque(true);
        lblNote.setBackground(badgeBg);
        lblNote.setBorder(new EmptyBorder(4, 8, 4, 8));

        card.add(lblLabel, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblNote, BorderLayout.SOUTH);
        return card;
    }

    private JSplitPane buildMainSplitPanels() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTaskPanel(), buildRoomStatusPanel());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildTaskPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        JPanel tableCard = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Công việc cần xử lý");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Danh sách nghiệp vụ cần theo dõi trong ca.");
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

        seedTaskTable();

        tblCongViec = new JTable(taskTableModel);
        tblCongViec.setFont(BODY_FONT);
        tblCongViec.setRowHeight(32);
        tblCongViec.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCongViec.setGridColor(BORDER_SOFT);
        tblCongViec.setShowGrid(true);
        tblCongViec.setFillsViewportHeight(true);
        tblCongViec.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblCongViec.getTableHeader().setBackground(new Color(243, 244, 246));
        tblCongViec.getTableHeader().setForeground(TEXT_PRIMARY);

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
        lblChiTietDoiTuong = createValueLabel();
        lblChiTietThoiGian = createValueLabel();
        lblChiTietTrangThai = createValueLabel();
        lblChiTietHuongXuLy = createValueLabel();

        addDetailRow(detailBody, "Mã nghiệp vụ", lblChiTietMa);
        addDetailRow(detailBody, "Đối tượng", lblChiTietDoiTuong);
        addDetailRow(detailBody, "Thời gian", lblChiTietThoiGian);
        addDetailRow(detailBody, "Trạng thái", lblChiTietTrangThai);
        addDetailRow(detailBody, "Hướng xử lý", lblChiTietHuongXuLy);

        detailCard.add(lblDetailTitle, BorderLayout.NORTH);
        detailCard.add(detailBody, BorderLayout.CENTER);

        wrapper.add(tableCard, BorderLayout.CENTER);
        wrapper.add(detailCard, BorderLayout.SOUTH);

        if (taskTableModel.getRowCount() > 0) {
            tblCongViec.setRowSelectionInterval(0, 0);
            updateTaskDetail(0);
        }

        return wrapper;
    }

    private JPanel buildRoomStatusPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        JPanel roomCard = createCardPanel(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Sơ đồ phòng realtime");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Trạng thái phòng đang vận hành theo khu vực.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        JPanel floors = new JPanel();
        floors.setOpaque(false);
        floors.setLayout(new BoxLayout(floors, BoxLayout.Y_AXIS));
        floors.add(buildFloorRow("Tầng 1", new String[]{"101:T", "102:O", "103:D", "104:C"}));
        floors.add(Box.createVerticalStrut(10));
        floors.add(buildFloorRow("Tầng 2", new String[]{"201:T", "202:D", "203:O", "204:T"}));
        floors.add(Box.createVerticalStrut(10));
        floors.add(buildFloorRow("Tầng 5", new String[]{"501:B", "502:O", "503:T", "504:C"}));

        roomCard.add(titleRow, BorderLayout.NORTH);
        roomCard.add(floors, BorderLayout.CENTER);

        JPanel legendCard = createCardPanel(new BorderLayout(0, 10));
        JLabel lblLegendTitle = new JLabel("Chú thích trạng thái");
        lblLegendTitle.setFont(SECTION_FONT);
        lblLegendTitle.setForeground(TEXT_PRIMARY);

        JPanel legendGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        legendGrid.setOpaque(false);
        legendGrid.add(createLegendItem("T", "Trống"));
        legendGrid.add(createLegendItem("O", "Đang ở"));
        legendGrid.add(createLegendItem("D", "Đã đặt"));
        legendGrid.add(createLegendItem("C", "Dọn dẹp"));
        legendGrid.add(createLegendItem("B", "Bảo trì"));

        legendCard.add(lblLegendTitle, BorderLayout.NORTH);
        legendCard.add(legendGrid, BorderLayout.CENTER);

        wrapper.add(roomCard, BorderLayout.CENTER);
        wrapper.add(legendCard, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildFloorRow(String floorName, String[] rooms) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel floorLabel = new JLabel(floorName);
        floorLabel.setPreferredSize(new Dimension(68, 46));
        floorLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        floorLabel.setForeground(TEXT_PRIMARY);

        JPanel roomGrid = new JPanel(new GridLayout(1, rooms.length, 8, 0));
        roomGrid.setOpaque(false);

        for (int i = 0; i < rooms.length; i++) {
            String[] pair = rooms[i].split(":");
            roomGrid.add(createRoomBadge(pair[0], pair[1]));
        }

        row.add(floorLabel, BorderLayout.WEST);
        row.add(roomGrid, BorderLayout.CENTER);
        return row;
    }

    private JPanel createRoomBadge(String roomCode, String status) {
        JPanel badge = new JPanel(new BorderLayout(0, 4));
        badge.setPreferredSize(new Dimension(76, 54));
        badge.setBackground(resolveStatusColor(status));
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));

        JLabel lblRoom = new JLabel(roomCode, SwingConstants.CENTER);
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRoom.setForeground(TEXT_PRIMARY);

        JLabel lblStatus = new JLabel(status, SwingConstants.CENTER);
        lblStatus.setFont(BODY_FONT);
        lblStatus.setForeground(TEXT_MUTED);

        badge.add(lblRoom, BorderLayout.CENTER);
        badge.add(lblStatus, BorderLayout.SOUTH);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showMessage("Phòng " + roomCode + " - Trạng thái: " + resolveStatusText(status));
            }
        };
        badge.addMouseListener(mouseAdapter);
        lblRoom.addMouseListener(mouseAdapter);
        lblStatus.addMouseListener(mouseAdapter);
        return badge;
    }

    private JPanel createLegendItem(String code, String meaning) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        item.setOpaque(false);

        JPanel swatch = new JPanel(new BorderLayout());
        swatch.setPreferredSize(new Dimension(28, 24));
        swatch.setBackground(resolveStatusColor(code));
        swatch.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        JLabel lblCode = new JLabel(code, SwingConstants.CENTER);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCode.setForeground(TEXT_PRIMARY);
        swatch.add(lblCode, BorderLayout.CENTER);

        JLabel lblMeaning = new JLabel(meaning);
        lblMeaning.setFont(BODY_FONT);
        lblMeaning.setForeground(TEXT_PRIMARY);

        item.add(swatch);
        item.add(lblMeaning);
        return item;
    }

    private void seedTaskTable() {
        taskTableModel.setRowCount(0);
    }

    private void updateTaskDetail(int row) {
        if (row < 0 || row >= taskTableModel.getRowCount()) {
            lblChiTietMa.setText("-");
            lblChiTietDoiTuong.setText("-");
            lblChiTietThoiGian.setText("-");
            lblChiTietTrangThai.setText("-");
            lblChiTietHuongXuLy.setText("-");
            return;
        }

        String ma = valueAt(row, 0);
        String doiTuong = valueAt(row, 1);
        String thoiGian = valueAt(row, 2);
        String trangThai = valueAt(row, 3);

        lblChiTietMa.setText(ma);
        lblChiTietDoiTuong.setText(doiTuong);
        lblChiTietThoiGian.setText(thoiGian);
        lblChiTietTrangThai.setText(trangThai);
        lblChiTietHuongXuLy.setText(resolveTaskAction(ma, trangThai));
    }

    private String resolveTaskAction(String ma, String trangThai) {
        if (ma.startsWith("DP")) {
            return "Mở hồ sơ đặt phòng và xác nhận tiếp nhận.";
        }
        if (ma.startsWith("ST")) {
            return "Kiểm tra tình trạng phòng và chuẩn bị checkout.";
        }
        if (ma.startsWith("HD")) {
            return "Đối chiếu dịch vụ và hoàn tất thanh toán.";
        }
        return "Chuyển bộ phận kỹ thuật xử lý.";
    }

    private String valueAt(int row, int column) {
        Object value = taskTableModel.getValueAt(row, column);
        return value == null ? "" : value.toString();
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
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

    private JPanel createCardPanel(GridLayout layout) {
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
                "Enter Mở chi tiết"
        );
    }

    private Color resolveStatusColor(String status) {
        if ("T".equals(status)) {
            return new Color(220, 252, 231);
        }
        if ("D".equals(status)) {
            return new Color(254, 249, 195);
        }
        if ("O".equals(status)) {
            return new Color(219, 234, 254);
        }
        if ("C".equals(status)) {
            return new Color(255, 237, 213);
        }
        return new Color(254, 226, 226);
    }

    private String resolveStatusText(String status) {
        if ("T".equals(status)) {
            return "Trống";
        }
        if ("D".equals(status)) {
            return "Đã đặt";
        }
        if ("O".equals(status)) {
            return "Đang ở";
        }
        if ("C".equals(status)) {
            return "Dọn dẹp";
        }
        return "Bảo trì";
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
        ScreenUIHelper.registerShortcut(this, "ENTER", "dashboard-enter", () -> {
            int row = tblCongViec == null ? -1 : tblCongViec.getSelectedRow();
            if (row < 0) {
                showMessage("Vui lòng chọn một công việc trong danh sách.");
                return;
            }
            showMessage("Đang mở chi tiết " + valueAt(row, 0) + ".");
        });
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    /**
     * Trả về panel đã build — dùng bởi NavigationUtil để swap vào AppFrame.
     */
    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }

}
