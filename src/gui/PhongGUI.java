package gui;

import dao.LoaiPhongDAO;
import dao.PhongDAO;
import dao.TienNghiDAO;
import entity.LoaiPhong;
import entity.Phong;
import entity.TienNghi;
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
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    // Demo khach san duoc chot co 5 tang de dong bo voi du lieu seed.
    private static final String[] FLOORS = {"Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5"};
    private static final String[] CREATE_ROOM_FLOORS = {"Tầng 1", "Tầng 2", "Tầng 3", "Tầng 4", "Tầng 5"};
    private static final String[] ROOM_STATUS_OPTIONS = {"Hoạt động", "Không hoạt động", "Bảo trì"};
    private static final String[] ZONES = {"Khu A", "Khu B", "Khu C", "Khu VIP"};
    private static final java.util.List<PhongGUI> OPEN_INSTANCES = new java.util.ArrayList<PhongGUI>();
    private final PhongDAO phongDAO = new PhongDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();
    private final TienNghiDAO tienNghiDAO = new TienNghiDAO();

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<RoomRecord> allRooms = new ArrayList<RoomRecord>();
    private final List<RoomRecord> filteredRooms = new ArrayList<RoomRecord>();
    private final List<LoaiPhong> allLoaiPhong = new ArrayList<LoaiPhong>();
    private final List<TienNghi> allTienNghi = new ArrayList<TienNghi>();

    private JTable tblPhong;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTang;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;
    private JPanel pnlQuickMapContent;
    private Integer selectedRoomId;
    private boolean suppressTableSelectionEvent;

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

        initUI();
        loadFilterSources();
        reloadRoomData(true, false);
        registerShortcuts();
        synchronized (OPEN_INSTANCES) {
            OPEN_INSTANCES.add(this);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                synchronized (OPEN_INSTANCES) {
                    OPEN_INSTANCES.remove(PhongGUI.this);
                }
            }
        });
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
        JButton btnDoiTrangThai = createPrimaryButton("Đổi trạng thái", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivateRoomDialog());
        JButton btnXoaPhong = createPrimaryButton("Xóa phòng", new Color(220, 38, 38), Color.WHITE, e -> deleteSelectedRoom());
        JButton btnGanLoaiPhong = createPrimaryButton("Gán loại phòng", new Color(99, 102, 241), Color.WHITE, e -> openAssignRoomTypeDialog());
        card.add(btnThemPhong);
        card.add(btnDoiTrangThai);
        card.add(btnXoaPhong);
        card.add(btnGanLoaiPhong);
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTang = createComboBox(new String[]{"Tất cả"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Hoạt động", "Đã đặt", "Đang ở", "Không hoạt động", "Bảo trì"});
        txtTuKhoa = createInputField("");
        ScreenUIHelper.applySearchFieldSize(txtTuKhoa);
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> applyFilters(false));
        ScreenUIHelper.installAutoFilter(() -> applyFilters(false), cboTang, cboLoaiPhong, cboTrangThai);
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
        ScreenUIHelper.styleTableHeader(tblPhong);

        tblPhong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (suppressTableSelectionEvent) {
                    return;
                }
                int row = tblPhong.getSelectedRow();
                if (row >= 0 && row < filteredRooms.size()) {
                    handleRoomSelection(filteredRooms.get(row), false);
                } else if (selectedRoomId == null) {
                    clearDetailPanel();
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

        pnlQuickMapContent = new JPanel();
        pnlQuickMapContent.setOpaque(false);
        pnlQuickMapContent.setLayout(new BoxLayout(pnlQuickMapContent, BoxLayout.Y_AXIS));
        refreshQuickMap();

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(pnlQuickMapContent, BorderLayout.CENTER);
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

    private JPanel buildRoomRow(String floorName, List<RoomRecord> rooms) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName + ":");
        lblFloor.setPreferredSize(new Dimension(56, 32));
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFloor.setForeground(TEXT_PRIMARY);

        JPanel roomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomPanel.setOpaque(false);
        for (RoomRecord room : rooms) {
            roomPanel.add(createRoomBadge(room));
        }

        row.add(lblFloor, BorderLayout.WEST);
        row.add(roomPanel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createRoomBadge(RoomRecord room) {
        JPanel badge = new JPanel(new BorderLayout());
        badge.setPreferredSize(new Dimension(82, 40));
        badge.setBackground(resolveStatusColor(toStatusCode(room.trangThai)));
        badge.setBorder(createRoomBadgeBorder(room.maPhong == (selectedRoomId == null ? -1 : selectedRoomId.intValue())));
        badge.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel("<html><center>" + room.soPhong + "<br>" + resolveStatusText(toStatusCode(room.trangThai)) + "</center></html>", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleRoomSelection(room, true);
            }
        };
        badge.addMouseListener(clickHandler);
        lbl.addMouseListener(clickHandler);

        badge.add(lbl, BorderLayout.CENTER);
        return badge;
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

    private javax.swing.border.Border createRoomBadgeBorder(boolean selected) {
        if (selected) {
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(37, 99, 235), 3, true),
                    BorderFactory.createLineBorder(new Color(255, 255, 255), 1, true)
            );
        }
        return BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true);
    }

    private JPanel buildLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem("Hoạt động", resolveStatusColor("A")));
        legend.add(createLegendItem("Đã đặt", resolveStatusColor("B")));
        legend.add(createLegendItem("Đang ở", resolveStatusColor("O")));
        legend.add(createLegendItem("Không hoạt động", resolveStatusColor("I")));
        legend.add(createLegendItem("Bảo trì", resolveStatusColor("M")));
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

    private void loadFilterSources() {
        allLoaiPhong.clear();
        allLoaiPhong.addAll(loaiPhongDAO.getAll());
        allTienNghi.clear();
        allTienNghi.addAll(tienNghiDAO.getAll());
        populateLoaiPhongFilter();
    }

    private void populateLoaiPhongFilter() {
        String selected = valueOf(cboLoaiPhong.getSelectedItem());
        cboLoaiPhong.removeAllItems();
        cboLoaiPhong.addItem("Tất cả");
        for (LoaiPhong loaiPhong : allLoaiPhong) {
            cboLoaiPhong.addItem(loaiPhong.getTenLoaiPhong());
        }
        restoreSelection(cboLoaiPhong, selected, "Tất cả");
    }

    private void populateTangFilter() {
        String selected = valueOf(cboTang.getSelectedItem());
        cboTang.removeAllItems();
        cboTang.addItem("Tất cả");
        for (String tang : FLOORS) {
            for (RoomRecord room : allRooms) {
                if (tang.equals(room.tang)) {
                    cboTang.addItem(tang);
                    break;
                }
            }
        }
        restoreSelection(cboTang, selected, "Tất cả");
    }

    private void restoreSelection(JComboBox<String> comboBox, String selected, String fallback) {
        if (selected == null || selected.trim().isEmpty()) {
            comboBox.setSelectedItem(fallback);
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (selected.equals(comboBox.getItemAt(i))) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        comboBox.setSelectedItem(fallback);
    }

    private void reloadRoomData(boolean resetFilters, boolean showMessage) {
        if (resetFilters) {
            resetFilters();
        }

        List<Phong> dsPhong = phongDAO.getAll();
        allRooms.clear();
        for (Phong phong : dsPhong) {
            allRooms.add(RoomRecord.fromPhong(phong, buildAmenitySummary(phong.getMaLoaiPhong(), phong.getTenLoaiPhong())));
        }
        populateTangFilter();
        applyFilters(false);
        refreshCurrentView();
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu phòng.");
        }
    }

    private void resetFilters() {
        cboTang.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        if (tblPhong != null) {
            clearTableSelection();
        }
    }

    private String buildAmenitySummary(int maLoaiPhong, String tenLoaiPhong) {
        List<Integer> amenityIds = loaiPhongDAO.getTienNghiIdsByLoaiPhong(maLoaiPhong);
        if (amenityIds.isEmpty()) {
            return "Tiện nghi: Chưa cấu hình tiện nghi mặc định."
                    + "\nNguồn: Theo loại phòng " + safeValue(tenLoaiPhong, "-")
                    + "\nGhi chú: Không có dữ liệu tiện nghi kế thừa.";
        }

        Map<Integer, String> amenityById = new LinkedHashMap<Integer, String>();
        for (TienNghi tienNghi : allTienNghi) {
            amenityById.put(Integer.valueOf(tienNghi.getMaTienNghi()), tienNghi.getTenTienNghi());
        }

        List<String> names = new ArrayList<String>();
        for (Integer amenityId : amenityIds) {
            String name = amenityById.get(amenityId);
            if (name != null && !name.trim().isEmpty()) {
                names.add(name.trim());
            }
        }
        if (names.isEmpty()) {
            return "Tiện nghi: Chưa cấu hình tiện nghi mặc định."
                    + "\nNguồn: Theo loại phòng " + safeValue(tenLoaiPhong, "-")
                    + "\nGhi chú: Không có dữ liệu tiện nghi kế thừa.";
        }
        return "Tiện nghi: " + joinValues(names)
                + "\nNguồn: Theo loại phòng " + safeValue(tenLoaiPhong, "-")
                + "\nGhi chú: Tiện nghi của phòng được kế thừa từ loại phòng.";
    }

    private String joinValues(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }

    private void applyFilters(boolean showMessage) {
        filteredRooms.clear();

        String tang = valueOf(cboTang.getSelectedItem());
        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (RoomRecord room : allRooms) {
            if (!"Tất cả".equals(tang) && !room.tang.equalsIgnoreCase(tang)) {
                continue;
            }
            if (!"Tất cả".equals(loaiPhong) && !room.loaiPhong.equalsIgnoreCase(loaiPhong)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !room.trangThai.equalsIgnoreCase(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (room.soPhong + " " + room.loaiPhong + " " + room.khuVuc + " " + room.tang).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredRooms.add(room);
        }

        refillTable();
        refreshQuickMap();
        refreshCurrentView();
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
                    room.khuVuc
            });
        }

        if (!filteredRooms.isEmpty()) {
            int selectedIndex = resolvePreferredSelectionIndex();
            if (selectedIndex >= 0 && selectedIndex < filteredRooms.size()) {
                selectTableRow(selectedIndex);
                updateDetailPanel(filteredRooms.get(selectedIndex));
                return;
            }
        }

        if (selectedRoomId != null) {
            RoomRecord room = findRoomById(allRooms, selectedRoomId.intValue());
            if (room != null) {
                clearTableSelection();
                updateDetailPanel(room);
                return;
            }
        }

        if (!allRooms.isEmpty()) {
            selectedRoomId = Integer.valueOf(allRooms.get(0).maPhong);
            clearTableSelection();
            updateDetailPanel(allRooms.get(0));
        } else {
            selectedRoomId = null;
            clearDetailPanel();
        }
    }

    private void refreshQuickMap() {
        if (pnlQuickMapContent == null) {
            return;
        }

        pnlQuickMapContent.removeAll();
        Map<String, List<RoomRecord>> roomsByFloor = new LinkedHashMap<String, List<RoomRecord>>();
        List<RoomRecord> sourceRooms = filteredRooms.isEmpty() ? allRooms : filteredRooms;
        for (RoomRecord room : sourceRooms) {
            if (!roomsByFloor.containsKey(room.tang)) {
                roomsByFloor.put(room.tang, new ArrayList<RoomRecord>());
            }
            roomsByFloor.get(room.tang).add(room);
        }

        List<String> tangs = new ArrayList<String>(roomsByFloor.keySet());
        Collections.sort(tangs);
        for (int i = 0; i < tangs.size(); i++) {
            String tang = tangs.get(i);
            List<RoomRecord> rooms = roomsByFloor.get(tang);
            Collections.sort(rooms, Comparator.comparing(room -> room.soPhong));
            pnlQuickMapContent.add(buildRoomRow(tang, rooms));
            if (i < tangs.size() - 1) {
                pnlQuickMapContent.add(Box.createVerticalStrut(8));
            }
        }

        if (!tangs.isEmpty()) {
            pnlQuickMapContent.add(Box.createVerticalStrut(10));
        }
        pnlQuickMapContent.add(buildLegendPanel());
        pnlQuickMapContent.revalidate();
        pnlQuickMapContent.repaint();
    }

    private void updateDetailPanel(RoomRecord room) {
        if (room != null) {
            selectedRoomId = Integer.valueOf(room.maPhong);
        }
        lblSoPhong.setText(room.soPhong);
        lblLoaiPhong.setText(room.loaiPhong);
        lblTang.setText(room.tang + " - " + room.khuVuc);
        lblSucChuaChuan.setText(room.sucChuaChuan + " người");
        lblSucChuaToiDa.setText(room.sucChuaToiDa + " người");
        lblTrangThai.setText(room.trangThai);
        txtGhiChu.setText("Khu vực: " + room.khuVuc);
        txtGhiChu.setCaretPosition(0);
        txtTienNghi.setText(room.amenitySummary);
        txtTienNghi.setCaretPosition(0);
        refreshCurrentView();
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
        refreshCurrentView();
    }

    private void handleRoomSelection(RoomRecord room, boolean syncTableSelection) {
        if (room == null) {
            return;
        }
        selectedRoomId = Integer.valueOf(room.maPhong);
        updateDetailPanel(room);

        int rowIndex = findFilteredRoomIndexById(room.maPhong);
        if (syncTableSelection) {
            if (rowIndex >= 0) {
                selectTableRow(rowIndex);
            } else {
                clearTableSelection();
            }
        }
        refreshQuickMap();
    }

    private int resolvePreferredSelectionIndex() {
        if (selectedRoomId != null) {
            int selectedIndex = findFilteredRoomIndexById(selectedRoomId.intValue());
            if (selectedIndex >= 0) {
                return selectedIndex;
            }
        }
        int currentRow = tblPhong == null ? -1 : tblPhong.getSelectedRow();
        if (currentRow >= 0 && currentRow < filteredRooms.size()) {
            return currentRow;
        }
        return filteredRooms.isEmpty() ? -1 : 0;
    }

    private int findFilteredRoomIndexById(int maPhong) {
        for (int i = 0; i < filteredRooms.size(); i++) {
            if (filteredRooms.get(i).maPhong == maPhong) {
                return i;
            }
        }
        return -1;
    }

    private RoomRecord findRoomById(List<RoomRecord> rooms, int maPhong) {
        for (RoomRecord room : rooms) {
            if (room.maPhong == maPhong) {
                return room;
            }
        }
        return null;
    }

    private void selectTableRow(int rowIndex) {
        if (tblPhong == null || rowIndex < 0 || rowIndex >= filteredRooms.size()) {
            return;
        }
        suppressTableSelectionEvent = true;
        try {
            tblPhong.setRowSelectionInterval(rowIndex, rowIndex);
            java.awt.Rectangle cellRect = tblPhong.getCellRect(rowIndex, 0, true);
            if (cellRect != null) {
                tblPhong.scrollRectToVisible(cellRect);
            }
        } finally {
            suppressTableSelectionEvent = false;
        }
    }

    private void clearTableSelection() {
        if (tblPhong == null) {
            return;
        }
        suppressTableSelectionEvent = true;
        try {
            tblPhong.clearSelection();
        } finally {
            suppressTableSelectionEvent = false;
        }
    }

    private RoomRecord getSelectedRoom() {
        int row = tblPhong.getSelectedRow();
        if (row < 0 || row >= filteredRooms.size()) {
            showWarning("Vui lòng chọn một phòng trong danh sách.");
            return null;
        }
        return filteredRooms.get(row);
    }

    private RoomRecord getSelectedRoomSilently() {
        int row = tblPhong == null ? -1 : tblPhong.getSelectedRow();
        if (row < 0 || row >= filteredRooms.size()) {
            return null;
        }
        return filteredRooms.get(row);
    }

    private Color resolveStatusColor(String code) {
        if ("A".equals(code)) {
            return new Color(220, 252, 231);
        }
        if ("B".equals(code)) {
            return new Color(254, 249, 195);
        }
        if ("O".equals(code)) {
            return new Color(254, 226, 226);
        }
        if ("I".equals(code)) {
            return new Color(229, 231, 235);
        }
        return new Color(253, 230, 138);
    }

    private String resolveStatusText(String code) {
        if ("A".equals(code)) {
            return "Hoạt động";
        }
        if ("B".equals(code)) {
            return "Đã đặt";
        }
        if ("O".equals(code)) {
            return "Đang ở";
        }
        if ("I".equals(code)) {
            return "Không hoạt động";
        }
        return "Bảo trì";
    }

    private String toStatusCode(String trangThai) {
        if ("Hoạt động".equalsIgnoreCase(trangThai)) {
            return "A";
        }
        if ("Đã đặt".equalsIgnoreCase(trangThai)) {
            return "B";
        }
        if ("Đang ở".equalsIgnoreCase(trangThai)) {
            return "O";
        }
        if ("Không hoạt động".equalsIgnoreCase(trangThai)) {
            return "I";
        }
        return "M";
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

    private void openAssignRoomTypeDialog() {
        new AssignRoomTypeDialog(this).setVisible(true);
    }

    private void deleteSelectedRoom() {
        RoomRecord room = getSelectedRoom();
        if (room == null) {
            return;
        }
        boolean accepted = showConfirmDialog(
                "Xác nhận xóa phòng",
                "Phòng " + room.soPhong + " sẽ bị xóa khỏi danh sách. Bạn có chắc muốn tiếp tục không?",
                "Xóa phòng",
                new Color(220, 38, 38)
        );
        if (!accepted) {
            return;
        }
        if (!phongDAO.delete(room.maPhong)) {
            String err = phongDAO.getLastErrorMessage();
            showError("Không thể xóa phòng." + (err == null || err.trim().isEmpty() ? "" : " " + err));
            return;
        }
        reloadRoomData(false, false);
        if (!filteredRooms.isEmpty()) {
            tblPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredRooms.get(0));
        } else {
            clearDetailPanel();
        }
        showSuccess("Xóa phòng thành công.");
    }

    private void refreshRoomViews(int maPhong, String message) {
        reloadRoomData(false, false);
        selectRoomById(maPhong);
        showSuccess(message);
    }

    private void selectRoomById(int maPhong) {
        selectedRoomId = Integer.valueOf(maPhong);
        int selectedIndex = findFilteredRoomIndexById(maPhong);
        if (selectedIndex >= 0) {
            selectTableRow(selectedIndex);
            updateDetailPanel(filteredRooms.get(selectedIndex));
            refreshQuickMap();
            refreshCurrentView();
            return;
        }
        RoomRecord room = findRoomById(allRooms, maPhong);
        if (room != null) {
            clearTableSelection();
            updateDetailPanel(room);
        } else if (!filteredRooms.isEmpty()) {
            selectedRoomId = Integer.valueOf(filteredRooms.get(0).maPhong);
            selectTableRow(0);
            updateDetailPanel(filteredRooms.get(0));
        } else {
            selectedRoomId = null;
            clearDetailPanel();
        }
        refreshQuickMap();
        refreshCurrentView();
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

    private void showValidationMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
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
                "F3 Đổi trạng thái",
                "F4 Xóa phòng",
                "F5 Gán loại phòng",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "phong-f1", this::openCreateRoomDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "phong-f3", this::openDeactivateRoomDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "phong-f4", this::deleteSelectedRoom);
        ScreenUIHelper.registerShortcut(this, "F5", "phong-f5", this::openAssignRoomTypeDialog);
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

    private String resolveFloorFromRoomNumber(String soPhong) {
        String normalized = soPhong == null ? "" : soPhong.trim();
        if (!normalized.matches("\\d{3}")) {
            return "";
        }
        char floorDigit = normalized.charAt(0);
        if (floorDigit < '1' || floorDigit > '5') {
            return "";
        }
        return "Tầng " + floorDigit;
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

    private JComboBox<String> createLoaiPhongDialogComboBox() {
        JComboBox<String> comboBox = createComboBox(new String[]{});
        for (LoaiPhong loaiPhong : allLoaiPhong) {
            comboBox.addItem(loaiPhong.getTenLoaiPhong());
        }
        return comboBox;
    }

    private LoaiPhong findLoaiPhongByTen(String tenLoaiPhong) {
        for (LoaiPhong loaiPhong : allLoaiPhong) {
            if (loaiPhong.getTenLoaiPhong() != null && loaiPhong.getTenLoaiPhong().equals(tenLoaiPhong)) {
                return loaiPhong;
            }
        }
        return null;
    }

    private Phong validatePhongInput(Integer maPhong, JTextField txtSoPhong, JComboBox<String> cboLoaiPhongInput,
                                     JTextField txtTang, JTextField txtKhuVuc,
                                     JTextField txtSucChuaChuan, JTextField txtSucChuaToiDa,
                                     JComboBox<String> cboTrangThaiInput) {
        String soPhong = txtSoPhong.getText().trim();
        String tenLoaiPhong = valueOf(cboLoaiPhongInput.getSelectedItem());
        String tang = txtTang.getText().trim();
        String khuVuc = txtKhuVuc.getText().trim();
        String sucChuaChuanText = txtSucChuaChuan.getText().trim();
        String sucChuaToiDaText = txtSucChuaToiDa.getText().trim();
        String trangThai = valueOf(cboTrangThaiInput.getSelectedItem());

        if (soPhong.isEmpty()) {
            showValidationMessage("Số phòng không được để trống.");
            txtSoPhong.requestFocusInWindow();
            return null;
        }
        if (phongDAO.isSoPhongExists(soPhong, maPhong)) {
            showValidationMessage("Số phòng đã tồn tại.");
            txtSoPhong.requestFocusInWindow();
            return null;
        }

        LoaiPhong loaiPhong = findLoaiPhongByTen(tenLoaiPhong);
        if (loaiPhong == null || loaiPhong.getMaLoaiPhong() <= 0) {
            showValidationMessage("Loại phòng không hợp lệ.");
            cboLoaiPhongInput.requestFocusInWindow();
            return null;
        }
        if (tang.isEmpty()) {
            showValidationMessage("Tầng không được để trống.");
            txtTang.requestFocusInWindow();
            return null;
        }
        if (khuVuc.isEmpty()) {
            showValidationMessage("Khu vực không được để trống.");
            txtKhuVuc.requestFocusInWindow();
            return null;
        }

        int sucChuaChuan;
        int sucChuaToiDa;
        try {
            sucChuaChuan = Integer.parseInt(sucChuaChuanText);
        } catch (NumberFormatException ex) {
            showValidationMessage("Sức chứa chuẩn phải là số nguyên lớn hơn 0.");
            txtSucChuaChuan.requestFocusInWindow();
            return null;
        }
        try {
            sucChuaToiDa = Integer.parseInt(sucChuaToiDaText);
        } catch (NumberFormatException ex) {
            showValidationMessage("Sức chứa tối đa phải là số nguyên hợp lệ.");
            txtSucChuaToiDa.requestFocusInWindow();
            return null;
        }

        if (sucChuaChuan <= 0) {
            showValidationMessage("Sức chứa chuẩn phải lớn hơn 0.");
            txtSucChuaChuan.requestFocusInWindow();
            return null;
        }
        if (sucChuaToiDa < sucChuaChuan) {
            showValidationMessage("Sức chứa tối đa phải lớn hơn hoặc bằng sức chứa chuẩn.");
            txtSucChuaToiDa.requestFocusInWindow();
            return null;
        }
        if (trangThai.isEmpty()) {
            showValidationMessage("Trạng thái không được để trống.");
            cboTrangThaiInput.requestFocusInWindow();
            return null;
        }

        Phong phong = new Phong();
        phong.setMaPhong(maPhong == null ? 0 : maPhong.intValue());
        phong.setMaLoaiPhong(loaiPhong.getMaLoaiPhong());
        phong.setTenLoaiPhong(loaiPhong.getTenLoaiPhong());
        phong.setSoPhong(soPhong);
        phong.setTang(tang);
        phong.setKhuVuc(khuVuc);
        phong.setSucChuaChuan(sucChuaChuan);
        phong.setSucChuaToiDa(sucChuaToiDa);
        phong.setTrangThai(trangThai);
        return phong;
    }

    private Phong validatePhongInput(Integer maPhong, JTextField txtSoPhong, JComboBox<String> cboLoaiPhongInput,
                                     JComboBox<String> cboTangInput, JTextField txtKhuVuc,
                                     JTextField txtSucChuaChuan, JTextField txtSucChuaToiDa,
                                     JComboBox<String> cboTrangThaiInput) {
        String soPhong = txtSoPhong.getText().trim();
        String tenLoaiPhong = valueOf(cboLoaiPhongInput.getSelectedItem());
        String tang = valueOf(cboTangInput.getSelectedItem()).trim();
        String khuVuc = txtKhuVuc.getText().trim();
        String sucChuaChuanText = txtSucChuaChuan.getText().trim();
        String sucChuaToiDaText = txtSucChuaToiDa.getText().trim();
        String trangThai = valueOf(cboTrangThaiInput.getSelectedItem());

        if (soPhong.isEmpty()) {
            showValidationMessage("Số phòng không được để trống.");
            txtSoPhong.requestFocusInWindow();
            return null;
        }
        if (!soPhong.matches("\\d{3}")) {
            showValidationMessage("Số phòng phải gồm đúng 3 chữ số.");
            txtSoPhong.requestFocusInWindow();
            return null;
        }
        String expectedTang = resolveFloorFromRoomNumber(soPhong);
        if (expectedTang.isEmpty()) {
            showValidationMessage("Khách sạn chỉ hỗ trợ phòng thuộc tầng 1 đến tầng 5.");
            txtSoPhong.requestFocusInWindow();
            return null;
        }
        if (phongDAO.isSoPhongExists(soPhong, maPhong)) {
            showValidationMessage("Số phòng đã tồn tại.");
            txtSoPhong.requestFocusInWindow();
            return null;
        }

        LoaiPhong loaiPhong = findLoaiPhongByTen(tenLoaiPhong);
        if (loaiPhong == null || loaiPhong.getMaLoaiPhong() <= 0) {
            showValidationMessage("Loại phòng không hợp lệ.");
            cboLoaiPhongInput.requestFocusInWindow();
            return null;
        }
        if (tang.isEmpty()) {
            showValidationMessage("Tầng sẽ được tự động suy ra từ số phòng.");
            cboTangInput.requestFocusInWindow();
            return null;
        }
        if (!expectedTang.equals(tang)) {
            showValidationMessage("Tầng phải khớp với chữ số đầu tiên của số phòng.");
            cboTangInput.requestFocusInWindow();
            return null;
        }
        if (khuVuc.isEmpty()) {
            showValidationMessage("Khu vực không được để trống.");
            txtKhuVuc.requestFocusInWindow();
            return null;
        }

        int sucChuaChuan;
        int sucChuaToiDa;
        try {
            sucChuaChuan = Integer.parseInt(sucChuaChuanText);
        } catch (NumberFormatException ex) {
            showValidationMessage("Sức chứa chuẩn phải là số nguyên lớn hơn 0.");
            txtSucChuaChuan.requestFocusInWindow();
            return null;
        }
        try {
            sucChuaToiDa = Integer.parseInt(sucChuaToiDaText);
        } catch (NumberFormatException ex) {
            showValidationMessage("Sức chứa tối đa phải là số nguyên hợp lệ.");
            txtSucChuaToiDa.requestFocusInWindow();
            return null;
        }

        if (sucChuaChuan <= 0) {
            showValidationMessage("Sức chứa chuẩn phải lớn hơn 0.");
            txtSucChuaChuan.requestFocusInWindow();
            return null;
        }
        if (sucChuaToiDa < sucChuaChuan) {
            showValidationMessage("Sức chứa tối đa phải lớn hơn hoặc bằng sức chứa chuẩn.");
            txtSucChuaToiDa.requestFocusInWindow();
            return null;
        }
        if (trangThai.isEmpty()) {
            showValidationMessage("Trạng thái không được để trống.");
            cboTrangThaiInput.requestFocusInWindow();
            return null;
        }

        Phong phong = new Phong();
        phong.setMaPhong(maPhong == null ? 0 : maPhong.intValue());
        phong.setMaLoaiPhong(loaiPhong.getMaLoaiPhong());
        phong.setTenLoaiPhong(loaiPhong.getTenLoaiPhong());
        phong.setSoPhong(soPhong);
        phong.setTang(tang);
        phong.setKhuVuc(khuVuc);
        phong.setSucChuaChuan(sucChuaChuan);
        phong.setSucChuaToiDa(sucChuaToiDa);
        phong.setTrangThai(trangThai);
        return phong;
    }

    private final class CreateRoomDialog extends BaseRoomDialog {
        private final JTextField txtSoPhongDialog;
        private final JComboBox<String> cboLoaiPhongDialog;
        private final JComboBox<String> cboTangDialog;
        private final JTextField txtSucChuaChuanDialog;
        private final JTextField txtSucChuaToiDaDialog;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JTextField txtKhuVucDialog;

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
            cboLoaiPhongDialog = createLoaiPhongDialogComboBox();
            cboTangDialog = createComboBox(CREATE_ROOM_FLOORS);
            cboTangDialog.setEnabled(false);
            txtSoPhongDialog.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    syncTangFromSoPhong();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    syncTangFromSoPhong();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    syncTangFromSoPhong();
                }
            });
            txtSucChuaChuanDialog = createInputField("");
            txtSucChuaToiDaDialog = createInputField("");
            cboTrangThaiDialog = createComboBox(ROOM_STATUS_OPTIONS);
            txtKhuVucDialog = createInputField("");

            addFormRow(form, gbc, 0, "Số phòng", txtSoPhongDialog);
            addFormRow(form, gbc, 1, "Loại phòng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 2, "Tầng", cboTangDialog);
            addFormRow(form, gbc, 3, "Sức chứa chuẩn", txtSucChuaChuanDialog);
            addFormRow(form, gbc, 4, "Sức chứa tối đa", txtSucChuaToiDaDialog);
            addFormRow(form, gbc, 5, "Trạng thái đầu", cboTrangThaiDialog);
            addFormRow(form, gbc, 6, "Khu vực", txtKhuVucDialog);

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSaveNew, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void syncTangFromSoPhong() {
            String tang = resolveFloorFromRoomNumber(txtSoPhongDialog.getText());
            if (tang.isEmpty()) {
                cboTangDialog.setSelectedIndex(-1);
                return;
            }
            cboTangDialog.setSelectedItem(tang);
        }

        private void submit(boolean keepOpen) {
            Phong phong = validatePhongInput(
                    null,
                    txtSoPhongDialog,
                    cboLoaiPhongDialog,
                    cboTangDialog,
                    txtKhuVucDialog,
                    txtSucChuaChuanDialog,
                    txtSucChuaToiDaDialog,
                    cboTrangThaiDialog
            );
            if (phong == null) {
                return;
            }
            if (!phongDAO.insert(phong)) {
                showError("Không thể thêm phòng vào cơ sở dữ liệu.");
                return;
            }

            reloadRoomData(false, false);
            selectRoomById(phong.getMaPhong());
            showSuccess(keepOpen ? "Thêm phòng thành công và sẵn sàng tạo phòng mới." : "Thêm phòng thành công.");
            if (keepOpen) {
                resetForm();
            } else {
                dispose();
            }
        }

        private void resetForm() {
            txtSoPhongDialog.setText("");
            if (cboLoaiPhongDialog.getItemCount() > 0) {
                cboLoaiPhongDialog.setSelectedIndex(0);
            }
            cboTangDialog.setSelectedIndex(-1);
            txtSucChuaChuanDialog.setText("");
            txtSucChuaToiDaDialog.setText("");
            cboTrangThaiDialog.setSelectedIndex(0);
            txtKhuVucDialog.setText("");
            txtSoPhongDialog.requestFocusInWindow();
        }
    }

    private final class UpdateRoomStatusDialog extends BaseRoomDialog {
        private final RoomRecord room;
        private final JTextField txtSoPhongDialog;
        private final JComboBox<String> cboLoaiPhongDialog;
        private final JTextField txtTangDialog;
        private final JTextField txtKhuVucDialog;
        private final JTextField txtSucChuaChuanDialog;
        private final JTextField txtSucChuaToiDaDialog;
        private final JComboBox<String> cboTrangThaiMoi;

        private UpdateRoomStatusDialog(Frame owner, RoomRecord room) {
            super(owner, "Cập nhật phòng", 620, 500);
            this.room = room;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Cập nhật phòng", "Chỉnh sửa thông tin phòng hiện có và lưu trực tiếp xuống cơ sở dữ liệu."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtSoPhongDialog = createInputField(room.soPhong);
            cboLoaiPhongDialog = createLoaiPhongDialogComboBox();
            cboLoaiPhongDialog.setSelectedItem(room.loaiPhong);
            txtTangDialog = createInputField(room.tang);
            txtKhuVucDialog = createInputField(room.khuVuc);
            txtSucChuaChuanDialog = createInputField(String.valueOf(room.sucChuaChuan));
            txtSucChuaToiDaDialog = createInputField(String.valueOf(room.sucChuaToiDa));
            cboTrangThaiMoi = createComboBox(ROOM_STATUS_OPTIONS);
            cboTrangThaiMoi.setSelectedItem(room.trangThai);

            addFormRow(form, gbc, 0, "Số phòng", txtSoPhongDialog);
            addFormRow(form, gbc, 1, "Loại phòng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 2, "Tầng", txtTangDialog);
            addFormRow(form, gbc, 3, "Khu vực", txtKhuVucDialog);
            addFormRow(form, gbc, 4, "Sức chứa chuẩn", txtSucChuaChuanDialog);
            addFormRow(form, gbc, 5, "Sức chứa tối đa", txtSucChuaToiDaDialog);
            addFormRow(form, gbc, 6, "Trạng thái", cboTrangThaiMoi);

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            Phong phong = validatePhongInput(
                    Integer.valueOf(room.maPhong),
                    txtSoPhongDialog,
                    cboLoaiPhongDialog,
                    txtTangDialog,
                    txtKhuVucDialog,
                    txtSucChuaChuanDialog,
                    txtSucChuaToiDaDialog,
                    cboTrangThaiMoi
            );
            if (phong == null) {
                return;
            }
            if (!phongDAO.update(phong)) {
                showError("Không thể cập nhật phòng.");
                return;
            }

            refreshRoomViews(phong.getMaPhong(), "Cập nhật phòng thành công.");
            dispose();
        }
    }

    private final class DeactivateRoomDialog extends BaseRoomDialog {
        private final RoomRecord room;
        private final JComboBox<String> cboTrangThaiMoi;
        private final JTextArea txtLyDo;

        private DeactivateRoomDialog(Frame owner, RoomRecord room) {
            super(owner, "Đổi trạng thái phòng", 560, 360);
            this.room = room;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Đổi trạng thái phòng", "Cập nhật trạng thái hoạt động của phòng và đồng bộ loại phòng nếu cần."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboTrangThaiMoi = createComboBox(ROOM_STATUS_OPTIONS);
            cboTrangThaiMoi.setSelectedItem(room.trangThai);
            txtLyDo = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Số phòng", createValueTag(room.soPhong));
            addFormRow(form, gbc, 1, "Loại phòng", createValueTag(room.loaiPhong));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(room.trangThai));
            addFormRow(form, gbc, 3, "Trạng thái mới", cboTrangThaiMoi);
            addFormRow(form, gbc, 4, "Ghi chú", new JScrollPane(txtLyDo));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            String trangThaiMoi = valueOf(cboTrangThaiMoi.getSelectedItem());
            if (trangThaiMoi.trim().isEmpty()) {
                showError("Vui lòng chọn trạng thái mới.");
                return;
            }
            if (trangThaiMoi.equalsIgnoreCase(room.trangThai)) {
                dispose();
                return;
            }
            boolean accepted = showConfirmDialog(
                    "Xác nhận đổi trạng thái",
                    "Phòng " + room.soPhong + " sẽ chuyển sang trạng thái " + trangThaiMoi + ".",
                    "Đồng ý",
                    new Color(245, 158, 11)
            );
            if (!accepted) {
                return;
            }

            if (!phongDAO.updateTrangThai(room.maPhong, trangThaiMoi)) {
                String err = phongDAO.getLastErrorMessage();
                showError("Không thể cập nhật trạng thái phòng." + (err == null || err.trim().isEmpty() ? "" : " " + err));
                return;
            }

            refreshRoomViews(room.maPhong, "Đã cập nhật trạng thái phòng thành công.");
            dispose();
        }
    }


    private final class AssignRoomTypeDialog extends BaseRoomDialog {
        private final JComboBox<String> cboLoaiPhongDialog;
        private final JList<AssignableRoomOption> lstRooms;

        private AssignRoomTypeDialog(Frame owner) {
            super(owner, "Gán loại phòng", 720, 560);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Gán loại phòng cho nhiều phòng", "Chọn loại phòng và đánh dấu các phòng mong muốn bên danh sách phòng."), BorderLayout.NORTH);

            cboLoaiPhongDialog = createLoaiPhongDialogComboBox();
            RoomRecord selectedRoom = getSelectedRoomSilently();
            if (selectedRoom != null) {
                cboLoaiPhongDialog.setSelectedItem(selectedRoom.loaiPhong);
            }

            List<AssignableRoomOption> options = buildAssignableRoomOptions();
            lstRooms = new JList<AssignableRoomOption>(options.toArray(new AssignableRoomOption[0]));
            lstRooms.setVisibleRowCount(12);
            lstRooms.setFont(BODY_FONT);
            lstRooms.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            if (selectedRoom != null) {
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).maPhong == selectedRoom.maPhong) {
                        lstRooms.setSelectedIndex(i);
                        break;
                    }
                }
            }

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(form, gbc, 0, "Loại phòng áp dụng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 1, "Chọn phòng", new JScrollPane(lstRooms));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận gán", new Color(99, 102, 241), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private List<AssignableRoomOption> buildAssignableRoomOptions() {
            List<AssignableRoomOption> options = new ArrayList<AssignableRoomOption>();
            for (Phong phong : phongDAO.findAssiDgnableRooms()) {
                options.add(new AssignableRoomOption(
                        phong.getMaPhong(),
                        phong.getSoPhong(),
                        phong.getTang(),
                        safeValue(phong.getTenLoaiPhong(), "Chưa gán"),
                        safeValue(phong.getTrangThai(), "-")
                ));
            }
            return options;
        }

        private void submit() {
            String tenLoaiPhong = valueOf(cboLoaiPhongDialog.getSelectedItem());
            LoaiPhong loaiPhong = findLoaiPhongByTen(tenLoaiPhong);
            if (loaiPhong == null) {
                showError("Loại phòng không hợp lệ.");
                return;
            }
            List<AssignableRoomOption> selected = lstRooms.getSelectedValuesList();
            if (selected == null || selected.isEmpty()) {
                showError("Vui lòng chọn ít nhất một phòng để gán loại phòng.");
                return;
            }
            List<Integer> ids = new ArrayList<Integer>();
            for (AssignableRoomOption option : selected) {
                ids.add(Integer.valueOf(option.maPhong));
            }
            if (!phongDAO.updateLoaiPhongForRooms(ids, loaiPhong.getMaLoaiPhong())) {
                showError("Không thể gán loại phòng cho danh sách phòng đã chọn.");
                return;
            }
            reloadRoomData(false, false);
            if (!ids.isEmpty()) {
                selectRoomById(ids.get(0).intValue());
            }
            showSuccess("Đã gán loại phòng " + tenLoaiPhong + " cho " + ids.size() + " phòng.");
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

    private static final class AssignableRoomOption {
        private final int maPhong;
        private final String soPhong;
        private final String tang;
        private final String loaiPhong;
        private final String trangThai;

        private AssignableRoomOption(int maPhong, String soPhong, String tang, String loaiPhong, String trangThai) {
            this.maPhong = maPhong;
            this.soPhong = soPhong;
            this.tang = tang;
            this.loaiPhong = loaiPhong;
            this.trangThai = trangThai;
        }

        @Override
        public String toString() {
            return soPhong + " - " + tang + " - " + loaiPhong + " - " + trangThai;
        }
    }

    private static final class RoomRecord {
        private int maPhong;
        private int maLoaiPhong;
        private String soPhong;
        private String loaiPhong;
        private String tang;
        private int sucChuaChuan;
        private int sucChuaToiDa;
        private String trangThai;
        private String khuVuc;
        private String ghiChu;
        private String amenitySummary;
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

        private static RoomRecord fromPhong(Phong phong, String amenitySummary) {
            RoomRecord room = new RoomRecord();
            room.maPhong = phong.getMaPhong();
            room.maLoaiPhong = phong.getMaLoaiPhong();
            room.soPhong = phong.getSoPhong();
            room.loaiPhong = phong.getTenLoaiPhong() == null ? "" : phong.getTenLoaiPhong();
            room.tang = phong.getTang() == null ? "" : phong.getTang();
            room.sucChuaChuan = phong.getSucChuaChuan();
            room.sucChuaToiDa = phong.getSucChuaToiDa();
            room.trangThai = phong.getTrangThai() == null ? "" : phong.getTrangThai();
            room.khuVuc = phong.getKhuVuc() == null ? "" : phong.getKhuVuc();
            room.ghiChu = room.khuVuc;
            room.amenitySummary = amenitySummary == null ? "" : amenitySummary;
            return room;
        }

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

    private void reloadExternalRoomData() {
        reloadRoomData(false, false);
    }

    public static void refreshAllOpenInstances() {
        java.util.List<PhongGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new java.util.ArrayList<PhongGUI>(OPEN_INSTANCES);
        }
        for (PhongGUI gui : snapshot) {
            if (gui != null) {
                javax.swing.SwingUtilities.invokeLater(gui::reloadExternalRoomData);
            }
        }
    }

    /**
     * Trả về panel đã build — dùng bởi NavigationUtil để swap vào AppFrame.
     */
    private void refreshCurrentView() {
        if (rootPanel != null) {
            rootPanel.revalidate();
            rootPanel.repaint();
        }
        if (tblPhong != null) {
            tblPhong.revalidate();
            tblPhong.repaint();
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }

}
