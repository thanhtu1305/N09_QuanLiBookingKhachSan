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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhongGUI extends JFrame {
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
    private static final String[] ROOM_TYPES = {"Standard", "Deluxe", "Suite"};
    private static final String[] FLOORS = {"Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5"};
    private static final String[] ROOM_STATUS_OPTIONS = {"Trống", "Đã đặt", "Đang ở", "Dọn dẹp", "Bảo trì"};
    private static final String[] ZONES = {"Khu A", "Khu B", "Khu C", "Khu VIP"};

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<RoomRecord> allRooms = new ArrayList<RoomRecord>();
    private final List<RoomRecord> filteredRooms = new ArrayList<RoomRecord>();

    private JTable tblPhong;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTang;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblSoPhong;
    private JLabel lblLoaiPhong;
    private JLabel lblTang;
    private JLabel lblSucChuaChuan;
    private JLabel lblSucChuaToiDa;
    private JLabel lblTrangThai;
    private JTextArea txtGhiChu;
    private JTextArea txtTienNghi;

    public PhongGUI() {
        this("guest", "Lễ tân");
    }

    public PhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý phòng - Hotel PMS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        seedSampleData();
        initUI();
        reloadSampleData(false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.PHONG, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ PHÒNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Theo dõi tồn phòng, trạng thái sử dụng và danh sách tiện nghi kế thừa theo loại phòng.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Phòng"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton btnThemPhong = createPrimaryButton("Thêm phòng", new Color(22, 163, 74), Color.WHITE, e -> openCreateRoomDialog());
        JButton btnCapNhatTrangThai = createPrimaryButton("Cập nhật trạng thái", new Color(37, 99, 235), Color.WHITE, e -> openUpdateStatusDialog());
        JButton btnNgungSuDung = createPrimaryButton("Ngừng sử dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivateRoomDialog());
        JButton btnLamMoi = createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true));
        JButton btnTimKiem = createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true));

        card.add(btnThemPhong);
        card.add(btnCapNhatTrangThai);
        card.add(btnNgungSuDung);
        card.add(btnLamMoi);
        card.add(btnTimKiem);
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTang = createComboBox(new String[]{"Tất cả", "Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Trống", "Đã đặt", "Đang ở", "Dọn dẹp", "Bảo trì"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(260, 34));
        txtTuKhoa.setToolTipText("Số phòng / mã phòng");

        left.add(createFieldGroup("Tầng", cboTang));
        left.add(createFieldGroup("Loại phòng", cboLoaiPhong));
        left.add(createFieldGroup("Trạng thái", cboTrangThai));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblSearch = new JLabel("Tìm kiếm");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);
        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(txtTuKhoa, BorderLayout.CENTER);
        searchRow.add(createOutlineButton("Lọc ngay", new Color(59, 130, 246), e -> applyFilters(true)), BorderLayout.EAST);
        right.add(searchRow);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JSplitPane buildCenterContent() {
        JPanel left = buildTableCard();
        JPanel right = buildRightColumn();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chọn một phòng để xem chi tiết.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "Số phòng",
                "Loại phòng",
                "Tầng",
                "Sức chứa",
                "Trạng thái",
                "Ghi chú"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblPhong = new JTable(tableModel);
        tblPhong.setFont(BODY_FONT);
        tblPhong.setRowHeight(32);
        tblPhong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPhong.setGridColor(BORDER_SOFT);
        tblPhong.setShowGrid(true);
        tblPhong.setFillsViewportHeight(true);
        tblPhong.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblPhong.getTableHeader().setBackground(new Color(243, 244, 246));
        tblPhong.getTableHeader().setForeground(TEXT_PRIMARY);

        tblPhong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblPhong.getSelectedRow();
                if (row >= 0 && row < filteredRooms.size()) {
                    updateDetailPanel(filteredRooms.get(row));
                }
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblPhong, this::openUpdateStatusDialog);

        JScrollPane scrollPane = new JScrollPane(tblPhong);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(titleRow, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel wrapper = createCardPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRightColumn() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(buildDetailCard(), BorderLayout.CENTER);
        wrapper.add(buildQuickMapCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblSoPhong = createValueLabel();
        lblLoaiPhong = createValueLabel();
        lblTang = createValueLabel();
        lblSucChuaChuan = createValueLabel();
        lblSucChuaToiDa = createValueLabel();
        lblTrangThai = createValueLabel();

        addDetailRow(body, "Số phòng", lblSoPhong);
        addDetailRow(body, "Loại phòng", lblLoaiPhong);
        addDetailRow(body, "Tầng", lblTang);
        addDetailRow(body, "Sức chứa chuẩn", lblSucChuaChuan);
        addDetailRow(body, "Sức chứa tối đa", lblSucChuaToiDa);
        addDetailRow(body, "Trạng thái", lblTrangThai);

        JPanel notePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        notePanel.setOpaque(false);

        txtGhiChu = createReadonlyArea();
        txtTienNghi = createReadonlyArea();

        notePanel.add(createAreaCard("Ghi chú", txtGhiChu));
        notePanel.add(createAreaCard("Tiện nghi kế thừa theo loại phòng", txtTienNghi));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(notePanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createAreaCard(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(LABEL_FONT);
        lblTitle.setForeground(TEXT_MUTED);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildQuickMapCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Sơ đồ nhanh tình trạng phòng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildRoomRow("Tầng 1", new String[]{"101:T", "102:D", "103:O", "104:C"}));
        content.add(Box.createVerticalStrut(8));
        content.add(buildRoomRow("Tầng 2", new String[]{"201:T", "202:D", "203:O", "204:T"}));
        content.add(Box.createVerticalStrut(8));
        content.add(buildRoomRow("Tầng 3", new String[]{"301:O", "302:T", "303:O", "304:C"}));
        content.add(Box.createVerticalStrut(8));
        content.add(buildRoomRow("Tầng 5", new String[]{"501:B", "502:O", "503:D"}));
        content.add(Box.createVerticalStrut(10));
        content.add(buildLegendPanel());

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomRow(String floorName, String[] rooms) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName + ":");
        lblFloor.setPreferredSize(new Dimension(56, 32));
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFloor.setForeground(TEXT_PRIMARY);

        JPanel roomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomPanel.setOpaque(false);
        for (String room : rooms) {
            String[] pair = room.split(":");
            roomPanel.add(createRoomBadge(pair[0], pair[1]));
        }

        row.add(lblFloor, BorderLayout.WEST);
        row.add(roomPanel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createRoomBadge(String roomCode, String statusCode) {
        JPanel badge = new JPanel(new BorderLayout());
        badge.setPreferredSize(new Dimension(82, 40));
        badge.setBackground(resolveStatusColor(statusCode));
        badge.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true));

        JLabel lbl = new JLabel("<html><center>" + roomCode + "<br>" + resolveStatusText(statusCode) + "</center></html>", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_PRIMARY);

        badge.add(lbl, BorderLayout.CENTER);
        return badge;
    }

    private JPanel buildLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem("Trống", resolveStatusColor("T")));
        legend.add(createLegendItem("Đã đặt", resolveStatusColor("D")));
        legend.add(createLegendItem("Đang ở", resolveStatusColor("O")));
        legend.add(createLegendItem("Dọn dẹp", resolveStatusColor("C")));
        legend.add(createLegendItem("Bảo trì", resolveStatusColor("B")));
        return legend;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);

        JPanel swatch = new JPanel();
        swatch.setBackground(color);
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_PRIMARY);

        item.add(swatch);
        item.add(lbl);
        return item;
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
        comboBox.setMaximumSize(new Dimension(170, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(160, 34));
        field.setMaximumSize(new Dimension(260, 34));
        return field;
    }

    private JTextArea createReadonlyArea() {
        JTextArea area = new JTextArea(4, 20);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(BODY_FONT);
        area.setForeground(TEXT_PRIMARY);
        area.setBackground(PANEL_SOFT);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
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

    private void addDetailRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(150, 20));

        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
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

    private void seedSampleData() {
        allRooms.clear();
    }

    private void reloadSampleData(boolean showMessage) {
        cboTang.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu phòng.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredRooms.clear();

        String tang = valueOf(cboTang.getSelectedItem());
        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (RoomRecord room : allRooms) {
            if (!"Tất cả".equals(tang) && !room.tang.equals(tang)) {
                continue;
            }
            if (!"Tất cả".equals(loaiPhong) && !room.loaiPhong.equals(loaiPhong)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !room.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (room.soPhong + " P" + room.soPhong).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredRooms.add(room);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredRooms.size() + " phòng phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (RoomRecord room : filteredRooms) {
            tableModel.addRow(new Object[]{
                    room.soPhong,
                    room.loaiPhong,
                    room.tang,
                    room.sucChuaChuan + " người",
                    room.trangThai,
                    room.ghiChu
            });
        }

        if (!filteredRooms.isEmpty()) {
            tblPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredRooms.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(RoomRecord room) {
        lblSoPhong.setText(room.soPhong);
        lblLoaiPhong.setText(room.loaiPhong);
        lblTang.setText(room.tang);
        lblSucChuaChuan.setText(room.sucChuaChuan + " người");
        lblSucChuaToiDa.setText(room.sucChuaToiDa + " người");
        lblTrangThai.setText(room.trangThai);
        txtGhiChu.setText(room.ghiChu);
        txtGhiChu.setCaretPosition(0);
        txtTienNghi.setText(room.buildInheritedAmenitiesSummary());
        txtTienNghi.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblSoPhong.setText("-");
        lblLoaiPhong.setText("-");
        lblTang.setText("-");
        lblSucChuaChuan.setText("-");
        lblSucChuaToiDa.setText("-");
        lblTrangThai.setText("-");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
        txtTienNghi.setText("Không có dữ liệu.");
    }

    private RoomRecord getSelectedRoom() {
        int row = tblPhong.getSelectedRow();
        if (row < 0 || row >= filteredRooms.size()) {
            showWarning("Vui lòng chọn một phòng trong danh sách.");
            return null;
        }
        return filteredRooms.get(row);
    }

    private Color resolveStatusColor(String code) {
        if ("T".equals(code)) {
            return new Color(220, 252, 231);
        }
        if ("D".equals(code)) {
            return new Color(254, 249, 195);
        }
        if ("O".equals(code)) {
            return new Color(219, 234, 254);
        }
        if ("C".equals(code)) {
            return new Color(255, 237, 213);
        }
        return new Color(254, 226, 226);
    }

    private String resolveStatusText(String code) {
        if ("T".equals(code)) {
            return "Trống";
        }
        if ("D".equals(code)) {
            return "Đã đặt";
        }
        if ("O".equals(code)) {
            return "Đang ở";
        }
        if ("C".equals(code)) {
            return "Dọn dẹp";
        }
        return "Bảo trì";
    }

    private void openCreateRoomDialog() {
        new CreateRoomDialog(this).setVisible(true);
    }

    private void openUpdateStatusDialog() {
        RoomRecord room = getSelectedRoom();
        if (room != null) {
            new UpdateRoomStatusDialog(this, room).setVisible(true);
        }
    }

    private void openDeactivateRoomDialog() {
        RoomRecord room = getSelectedRoom();
        if (room != null) {
            new DeactivateRoomDialog(this, room).setVisible(true);
        }
    }

    private void addRoom(RoomRecord room, boolean keepDialogOpen) {
        allRooms.add(0, room);
        cboTang.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        selectRoom(room);
        showSuccess(keepDialogOpen ? "Thêm phòng thành công và sẵn sàng tạo phòng mới." : "Thêm phòng thành công.");
    }

    private void refreshRoomViews(RoomRecord room, String message) {
        applyFilters(false);
        selectRoom(room);
        showSuccess(message);
    }

    private void selectRoom(RoomRecord room) {
        if (room == null) {
            return;
        }
        int index = filteredRooms.indexOf(room);
        if (index >= 0) {
            tblPhong.setRowSelectionInterval(index, index);
            updateDetailPanel(room);
        } else if (!filteredRooms.isEmpty()) {
            tblPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredRooms.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void showSuccess(String message) {
        showMessageDialog("Thành công", message, new Color(22, 163, 74));
    }

    private void showWarning(String message) {
        showMessageDialog("Thông báo", message, new Color(245, 158, 11));
    }

    private void showError(String message) {
        showMessageDialog("Cảnh báo", message, new Color(220, 38, 38));
    }

    private void showMessageDialog(String title, String message, Color accentColor) {
        AppMessageDialog dialog = new AppMessageDialog(this, title, message, accentColor);
        dialog.setVisible(true);
    }

    private boolean showConfirmDialog(String title, String message, String confirmText, Color confirmColor) {
        ConfirmDialog dialog = new ConfirmDialog(this, title, message, confirmText, confirmColor);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Thêm phòng",
                "F2 Cập nhật trạng thái",
                "F3 Ngừng sử dụng",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "phong-f1", this::openCreateRoomDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "phong-f2", this::openUpdateStatusDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "phong-f3", this::openDeactivateRoomDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "phong-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "phong-enter", () -> {
            RoomRecord room = getSelectedRoom();
            if (room != null) {
                showMessageDialog("Chi tiết phòng", "Đang xem chi tiết phòng " + room.soPhong + ".", new Color(59, 130, 246));
            }
        });
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
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

    private JLabel createValueTag(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private abstract class BaseRoomDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseRoomDialog(Frame owner, String title, int width, int height) {
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

    private final class CreateRoomDialog extends BaseRoomDialog {
        private final JTextField txtSoPhongDialog;
        private final JComboBox<String> cboLoaiPhongDialog;
        private final JComboBox<String> cboTangDialog;
        private final JTextField txtSucChuaDialog;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JComboBox<String> cboKhuVucDialog;
        private final JTextArea txtGhiChuDialog;

        private CreateRoomDialog(Frame owner) {
            super(owner, "Thêm phòng", 620, 500);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Thêm phòng mới", "Nhập thông tin cơ bản để bổ sung phòng mới vào danh sách khai thác."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtSoPhongDialog = createInputField("");
            cboLoaiPhongDialog = createComboBox(ROOM_TYPES);
            cboTangDialog = createComboBox(FLOORS);
            txtSucChuaDialog = createInputField("");
            cboTrangThaiDialog = createComboBox(ROOM_STATUS_OPTIONS);
            cboKhuVucDialog = createComboBox(ZONES);
            txtGhiChuDialog = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Số phòng", txtSoPhongDialog);
            addFormRow(form, gbc, 1, "Loại phòng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 2, "Tầng", cboTangDialog);
            addFormRow(form, gbc, 3, "Sức chứa", txtSucChuaDialog);
            addFormRow(form, gbc, 4, "Trạng thái đầu", cboTrangThaiDialog);
            addFormRow(form, gbc, 5, "Khu vực", cboKhuVucDialog);
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSaveNew, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean keepOpen) {
            String soPhong = txtSoPhongDialog.getText().trim();
            String loaiPhong = valueOf(cboLoaiPhongDialog.getSelectedItem());
            String tang = valueOf(cboTangDialog.getSelectedItem());
            String sucChuaText = txtSucChuaDialog.getText().trim();

            if (soPhong.isEmpty()) {
                showError("Số phòng không được để trống.");
                return;
            }
            for (RoomRecord room : allRooms) {
                if (room.soPhong.equalsIgnoreCase(soPhong)) {
                    showError("Số phòng đã tồn tại trong danh sách.");
                    return;
                }
            }
            if (loaiPhong.isEmpty()) {
                showError("Vui lòng chọn loại phòng.");
                return;
            }

            int sucChua;
            try {
                sucChua = Integer.parseInt(sucChuaText);
            } catch (NumberFormatException ex) {
                showError("Sức chứa phải là số hợp lệ.");
                return;
            }
            if (sucChua <= 0) {
                showError("Sức chứa phải lớn hơn 0.");
                return;
            }

            RoomRecord room = RoomRecord.create(
                    soPhong,
                    loaiPhong,
                    tang,
                    Math.max(1, sucChua),
                    Math.max(1, sucChua + 1),
                    valueOf(cboTrangThaiDialog.getSelectedItem()),
                    valueOf(cboKhuVucDialog.getSelectedItem()),
                    txtGhiChuDialog.getText().trim().isEmpty() ? "Phòng mới được tạo từ popup." : txtGhiChuDialog.getText().trim()
            );
            room.sucChuaToiDa = Math.max(room.sucChuaChuan + 1, room.sucChuaChuan);
            room.applyInheritedAmenitiesByRoomType(room.loaiPhong);

            addRoom(room, keepOpen);
            if (keepOpen) {
                resetForm();
            } else {
                dispose();
            }
        }

        private void resetForm() {
            txtSoPhongDialog.setText("");
            cboLoaiPhongDialog.setSelectedIndex(0);
            cboTangDialog.setSelectedIndex(0);
            txtSucChuaDialog.setText("");
            cboTrangThaiDialog.setSelectedIndex(0);
            cboKhuVucDialog.setSelectedIndex(0);
            txtGhiChuDialog.setText("");
            txtSoPhongDialog.requestFocusInWindow();
        }
    }

    private final class UpdateRoomStatusDialog extends BaseRoomDialog {
        private final RoomRecord room;
        private final JComboBox<String> cboTrangThaiMoi;
        private final JTextField txtLyDo;
        private final JTextArea txtGhiChuDialog;

        private UpdateRoomStatusDialog(Frame owner, RoomRecord room) {
            super(owner, "Cập nhật trạng thái phòng", 560, 420);
            this.room = room;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Cập nhật trạng thái", "Chọn trạng thái mới và cung cấp lý do cập nhật nếu cần."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboTrangThaiMoi = createComboBox(ROOM_STATUS_OPTIONS);
            cboTrangThaiMoi.setSelectedItem(room.trangThai);
            txtLyDo = createInputField("");
            txtGhiChuDialog = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Số phòng", createValueTag(room.soPhong));
            addFormRow(form, gbc, 1, "Loại phòng", createValueTag(room.loaiPhong));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(room.trangThai));
            addFormRow(form, gbc, 3, "Trạng thái mới", cboTrangThaiMoi);
            addFormRow(form, gbc, 4, "Lý do cập nhật", txtLyDo);
            addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            String trangThaiMoi = valueOf(cboTrangThaiMoi.getSelectedItem());
            if ("Trống".equals(trangThaiMoi) && "Đang ở".equals(room.trangThai)) {
                showError("Không thể chuyển trực tiếp từ Đang ở sang Trống. Hãy xử lý theo luồng check-out.");
                return;
            }
            if ("Đang ở".equals(trangThaiMoi) && !"Đã đặt".equals(room.trangThai)) {
                showError("Không thể chuyển trực tiếp sang Đang ở nếu không đi từ luồng check-in.");
                return;
            }
            if ("Bảo trì".equals(trangThaiMoi)) {
                boolean accepted = showConfirmDialog(
                        "Xác nhận trạng thái bảo trì",
                        "Phòng sẽ không thể được đặt hoặc check-in. Bạn có muốn tiếp tục không?",
                        "Đồng ý",
                        new Color(245, 158, 11)
                );
                if (!accepted) {
                    return;
                }
            }

            room.trangThai = trangThaiMoi;
            String lyDo = txtLyDo.getText().trim();
            String ghiChu = txtGhiChuDialog.getText().trim();
            StringBuilder builder = new StringBuilder();
            if (!lyDo.isEmpty()) {
                builder.append("Lý do: ").append(lyDo).append(". ");
            }
            if (!ghiChu.isEmpty()) {
                builder.append(ghiChu);
            }
            room.ghiChu = builder.length() == 0 ? "Đã cập nhật trạng thái phòng." : builder.toString().trim();

            refreshRoomViews(room, "Cập nhật trạng thái phòng thành công.");
            dispose();
        }
    }

    private final class DeactivateRoomDialog extends BaseRoomDialog {
        private final RoomRecord room;
        private final JComboBox<String> cboHinhThuc;
        private final JTextField txtTuNgay;
        private final JTextField txtDenNgay;
        private final JTextArea txtLyDo;

        private DeactivateRoomDialog(Frame owner, RoomRecord room) {
            super(owner, "Ngừng sử dụng phòng", 600, 430);
            this.room = room;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Ngừng sử dụng phòng", "Tạm loại phòng khỏi khai thác trong thời gian xác định."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboHinhThuc = createComboBox(new String[]{"Bảo trì dài hạn", "Tạm ngưng khai thác"});
            txtTuNgay = createInputField("19/03/2026");
            txtDenNgay = createInputField("26/03/2026");
            txtLyDo = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Số phòng", createValueTag(room.soPhong));
            addFormRow(form, gbc, 1, "Loại phòng", createValueTag(room.loaiPhong));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(room.trangThai));
            addFormRow(form, gbc, 3, "Hình thức", cboHinhThuc);
            addFormRow(form, gbc, 4, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 5, "Đến ngày", txtDenNgay);
            addFormRow(form, gbc, 6, "Lý do", new JScrollPane(txtLyDo));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận ngừng sử dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if ("Đang ở".equals(room.trangThai)) {
                showError("Không thể ngừng sử dụng phòng đang có khách lưu trú.");
                return;
            }
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do ngừng sử dụng.");
                return;
            }

            StringBuilder warning = new StringBuilder("Phòng sẽ bị loại khỏi khai thác tạm thời. Bạn có muốn tiếp tục không?");
            if ("Đã đặt".equals(room.trangThai)) {
                warning.append(" Phòng hiện có booking sắp tới, cần xử lý đặt phòng liên quan.");
            }
            boolean accepted = showConfirmDialog(
                    "Xác nhận ngừng sử dụng",
                    warning.toString(),
                    "Đồng ý",
                    new Color(220, 38, 38)
            );
            if (!accepted) {
                return;
            }

            room.trangThai = "Bảo trì";
            room.ghiChu = valueOf(cboHinhThuc.getSelectedItem()) + " từ " + txtTuNgay.getText().trim()
                    + " đến " + txtDenNgay.getText().trim() + ". " + txtLyDo.getText().trim();
            refreshRoomViews(room, "Ngừng sử dụng phòng thành công.");
            dispose();
        }
    }

    private final class ConfirmDialog extends BaseRoomDialog {
        private boolean confirmed;

        private ConfirmDialog(Frame owner, String title, String message, String confirmText, Color confirmColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose());
            JButton btnConfirm = createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> {
                confirmed = true;
                dispose();
            });
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() {
            return confirmed;
        }
    }

    private final class AppMessageDialog extends BaseRoomDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            JButton btnClose = createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose());
            content.add(buildDialogButtons(btnClose), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class RoomRecord {
        private String soPhong;
        private String loaiPhong;
        private String tang;
        private int sucChuaChuan;
        private int sucChuaToiDa;
        private String trangThai;
        private String khuVuc;
        private String ghiChu;
        private boolean dieuHoa;
        private boolean tv;
        private boolean wifi;
        private boolean nuocNong;
        private boolean bonTam;
        private boolean banLamViec;
        private boolean minibar;
        private boolean maySayToc;
        private boolean khoaTu;
        private boolean sofa;

        private static RoomRecord create(String soPhong, String loaiPhong, String tang, int sucChuaChuan, int sucChuaToiDa,
                                         String trangThai, String khuVuc, String ghiChu) {
            RoomRecord room = new RoomRecord();
            room.soPhong = soPhong;
            room.loaiPhong = loaiPhong;
            room.tang = tang;
            room.sucChuaChuan = sucChuaChuan;
            room.sucChuaToiDa = sucChuaToiDa;
            room.trangThai = trangThai;
            room.khuVuc = khuVuc;
            room.ghiChu = ghiChu;
            room.applyInheritedAmenitiesByRoomType(loaiPhong);
            return room;
        }

        private void applyInheritedAmenitiesByRoomType(String roomType) {
            dieuHoa = true;
            tv = true;
            wifi = true;
            nuocNong = true;
            bonTam = false;
            banLamViec = false;
            minibar = false;
            maySayToc = false;
            khoaTu = true;
            sofa = false;

            if ("Deluxe".equals(roomType)) {
                bonTam = true;
                banLamViec = true;
                minibar = true;
                maySayToc = true;
                return;
            }

            if ("Suite".equals(roomType)) {
                bonTam = true;
                banLamViec = true;
                minibar = true;
                maySayToc = true;
                sofa = true;
            }
        }

        private String buildInheritedAmenitiesSummary() {
            List<String> amenities = new ArrayList<String>();
            if (dieuHoa) {
                amenities.add("Điều hòa");
            }
            if (tv) {
                amenities.add("TV");
            }
            if (wifi) {
                amenities.add("Wifi");
            }
            if (nuocNong) {
                amenities.add("Nước nóng");
            }
            if (bonTam) {
                amenities.add("Bồn tắm");
            }
            if (banLamViec) {
                amenities.add("Bàn làm việc");
            }
            if (minibar) {
                amenities.add("Minibar");
            }
            if (maySayToc) {
                amenities.add("Máy sấy tóc");
            }
            if (khoaTu) {
                amenities.add("Khóa từ");
            }
            if (sofa) {
                amenities.add("Sofa");
            }
            String inheritedAmenities = amenities.isEmpty() ? "Chưa có cấu hình tiện nghi mặc định." : join(amenities);
            return "Tiện nghi: " + inheritedAmenities
                    + "\nNguồn: Theo loại phòng " + loaiPhong
                    + "\nGhi chú: Tiện nghi của phòng được kế thừa từ loại phòng và không chỉnh riêng ở cấp phòng.";
        }

        private String join(List<String> values) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(values.get(i));
            }
            return builder.toString();
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
