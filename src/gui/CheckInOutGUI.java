package gui;

import dao.BangGiaDAO;
import dao.CheckInOutDAO;
import dao.DatPhongDAO;
import dao.DichVuDAO;
import dao.SuDungDichVuDAO;
import dao.ThanhToanDAO;
import db.ConnectDB;
import entity.ChiTietBangGia;
import entity.DatPhongConflictInfo;
import entity.DichVu;
import entity.KhachHang;
import entity.SuDungDichVu;
import gui.common.AppBranding;
import gui.common.AppFonts;
import gui.common.AppDatePickerField;
import gui.common.AppTimePickerField;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CheckInOutGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Font TITLE_FONT = AppFonts.title(24);
    private static final Font SECTION_FONT = AppFonts.section(16);
    private static final Font BODY_FONT = AppFonts.body(13);
    private static final Font LABEL_FONT = AppFonts.label(12);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime DETAIL_BOOKING_BOUNDARY_TIME = LocalTime.of(12, 0);

    private static final List<CheckInOutGUI> OPEN_INSTANCES = new ArrayList<CheckInOutGUI>();
    private static Integer pendingFocusedBookingId;

    private final String username;
    private final String role;
    private final BangGiaDAO bangGiaDAO = new BangGiaDAO();
    private final CheckInOutDAO checkInOutDAO = new CheckInOutDAO();
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final SuDungDichVuDAO suDungDichVuDAO = new SuDungDichVuDAO();
    private JPanel rootPanel;
    private final List<StayRecord> allRecords = new ArrayList<StayRecord>();
    private final List<StayRecord> filteredRecords = new ArrayList<StayRecord>();

    private JTable tblLuuTru;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboTang;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboCaLam;
    private JTextField txtTuKhoa;

    private JLabel lblMaHoSo;
    private JLabel lblMaDatPhong;
    private JLabel lblKhachHang;
    private JLabel lblSoPhong;
    private JLabel lblLoaiPhongChiTiet;
    private JLabel lblTrangThaiPhong;
    private JLabel lblTienCoc;
    private JLabel lblDichVuPhatSinh;
    private JTextArea txtGhiChu;
    private JPanel realtimeMapPanel;
    private final Map<String, JPanel> realtimeRoomBadges = new LinkedHashMap<String, JPanel>();
    private final Set<String> highlightedRealtimeRoomCodes = new LinkedHashSet<String>();
    private String selectedRealtimeRoomCode;

    public CheckInOutGUI() {
        this("guest", "L\u1ec5 t\u00e2n");
    }

    public CheckInOutGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "L\u1ec5 t\u00e2n");

        setTitle("Qu\u1ea3n l\u00fd Check-in / Check-out - Hotel PMS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        reloadSampleData(false);
        registerShortcuts();

        synchronized (OPEN_INSTANCES) {
            OPEN_INSTANCES.add(this);
        }
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                synchronized (OPEN_INSTANCES) {
                    OPEN_INSTANCES.remove(CheckInOutGUI.this);
                }
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.CHECK_IN_OUT, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QU\u1ea2N L\u00dd CHECK-IN / CHECK-OUT"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("L\u1ea5y d\u1eef li\u1ec7u t\u1eeb \u0110\u1eb7t ph\u00f2ng, ch\u1ecdn ph\u00f2ng tr\u1ed1ng theo t\u1ea7ng v\u00e0 c\u1eadp nh\u1eadt s\u01a1 \u0111\u1ed3 ph\u00f2ng th\u1eddi gian th\u1ef1c.");
        lblSub.setFont(AppFonts.body(14));
        lblSub.setForeground(TEXT_MUTED);

        JLabel lblMeta = new JLabel("Ng\u01b0\u1eddi d\u00f9ng: " + username + " | Vai tr\u00f2: " + role);
        lblMeta.setFont(BODY_FONT);
        lblMeta.setForeground(TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);
        left.add(Box.createVerticalStrut(6));
        left.add(lblMeta);

        card.add(left, BorderLayout.WEST);
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "m\u00e0n h\u00ecnh Check-in / Check-out"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Check-in", new Color(22, 163, 74), Color.WHITE, e -> openCheckInDialog()));
        card.add(createPrimaryButton("Th\u00eam d\u1ecbch v\u1ee5", new Color(37, 99, 235), Color.WHITE, e -> openAddServiceDialog()));
        card.add(createPrimaryButton("\u0110\u1ed5i ph\u00f2ng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openChangeRoomDialog()));
        card.add(createPrimaryButton("Gia h\u1ea1n", new Color(59, 130, 246), Color.WHITE, e -> openExtendDialog()));
        card.add(createPrimaryButton("Check-out", new Color(220, 38, 38), Color.WHITE, e -> openCheckOutDialog()));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"T\u1ea5t c\u1ea3", "Ch\u1edd check-in", "\u0110ang \u1edf"});
        cboTang = createComboBox(new String[]{"T\u1ea5t c\u1ea3", "T\u1ea7ng 1", "T\u1ea7ng 2", "T\u1ea7ng 3", "T\u1ea7ng 4", "T\u1ea7ng 5"});
        cboLoaiPhong = createComboBox(new String[]{"T\u1ea5t c\u1ea3", "Standard", "Deluxe", "Suite", "Family", "VIP", "Ph\u00f2ng \u0111\u01a1n", "Ph\u00f2ng \u0111\u00f4i"});
        cboCaLam = createComboBox(new String[]{"T\u1ea5t c\u1ea3", "Ca s\u00e1ng", "Ca chi\u1ec1u", "Ca t\u1ed1i"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(280, 34));
        txtTuKhoa.setToolTipText("M\u00e3 \u0111\u1eb7t ph\u00f2ng / s\u1ed1 ph\u00f2ng / t\u00ean kh\u00e1ch");
        ScreenUIHelper.installAutoFilter(() -> applyFilters(false), cboTrangThai, cboTang, cboLoaiPhong, cboCaLam);
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> applyFilters(false));

        left.add(createFieldGroup("Tr\u1ea1ng th\u00e1i", cboTrangThai));
        left.add(createFieldGroup("T\u1ea7ng", cboTang));
        left.add(createFieldGroup("Lo\u1ea1i ph\u00f2ng", cboLoaiPhong));
        left.add(createFieldGroup("Ca l\u00e0m", cboCaLam));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblSearch = new JLabel("T\u00ecm ki\u1ebfm");
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
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Check-in",
                "F2 Th\u00eam d\u1ecbch v\u1ee5",
                "F3 \u0110\u1ed5i ph\u00f2ng",
                "F4 Gia h\u1ea1n",
                "F5 Check-out",
                "Enter Xem chi ti\u1ebft"
        );
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh s\u00e1ch check-in / check-out");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("B\u1ea5m m\u1ed9t d\u00f2ng \u0111\u1ec3 xem chi ti\u1ebft l\u01b0u tr\u00fa.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "M\u00e3 h\u1ed3 s\u01a1",
                "Kh\u00e1ch h\u00e0ng",
                "Ph\u00f2ng",
                "S\u1ed1 l\u01b0\u1ee3ng ph\u00f2ng",
                "Gi\u1edd v\u00e0o",
                "Gi\u1edd ra d\u1ef1 ki\u1ebfn",
                "Tr\u1ea1ng th\u00e1i"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLuuTru = new JTable(tableModel);
        tblLuuTru.setFont(BODY_FONT);
        tblLuuTru.setRowHeight(32);
        tblLuuTru.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLuuTru.setGridColor(BORDER_SOFT);
        tblLuuTru.setShowGrid(true);
        tblLuuTru.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblLuuTru);

        tblLuuTru.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblLuuTru.getSelectedRow();
                if (row >= 0 && row < filteredRecords.size()) {
                    StayRecord selectedRecord = filteredRecords.get(row);
                    updateDetailPanel(selectedRecord, null);
                    syncRealtimeMapHighlightFromTable(selectedRecord);
                } else {
                    clearRealtimeMapHighlightFromTable();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblLuuTru);
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
        wrapper.add(buildRealtimeCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi ti\u1ebft l\u01b0u tr\u00fa");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblMaHoSo = createValueLabel();
        lblMaDatPhong = createValueLabel();
        lblKhachHang = createValueLabel();
        lblSoPhong = createValueLabel();
        lblLoaiPhongChiTiet = createValueLabel();
        lblTrangThaiPhong = createValueLabel();
        lblTienCoc = createValueLabel();
        lblDichVuPhatSinh = createValueLabel();

        addDetailRow(body, "M\u00e3 h\u1ed3 s\u01a1", lblMaHoSo);
        addDetailRow(body, "M\u00e3 \u0111\u1eb7t ph\u00f2ng", lblMaDatPhong);
        addDetailRow(body, "Kh\u00e1ch h\u00e0ng", lblKhachHang);
        addDetailRow(body, "S\u1ed1 ph\u00f2ng", lblSoPhong);
        addDetailRow(body, "Lo\u1ea1i ph\u00f2ng", lblLoaiPhongChiTiet);
        addDetailRow(body, "Tr\u1ea1ng th\u00e1i ph\u00f2ng", lblTrangThaiPhong);
        addDetailRow(body, "Ti\u1ec1n c\u1ecdc", lblTienCoc);
        addDetailRow(body, "D\u1ecbch v\u1ee5 ph\u00e1t sinh", lblDichVuPhatSinh);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);

        JLabel lblNote = new JLabel("Ghi ch\u00fa");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);

        txtGhiChu = new JTextArea(4, 20);
        txtGhiChu.setEditable(false);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(BODY_FONT);
        txtGhiChu.setForeground(TEXT_PRIMARY);
        txtGhiChu.setBackground(PANEL_SOFT);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        notePanel.add(lblNote, BorderLayout.NORTH);
        notePanel.add(new JScrollPane(txtGhiChu), BorderLayout.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(notePanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRealtimeCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("S\u01a1 \u0111\u1ed3 ph\u00f2ng th\u1eddi gian th\u1ef1c");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        realtimeMapPanel = new JPanel();
        realtimeMapPanel.setOpaque(false);
        realtimeMapPanel.setLayout(new BoxLayout(realtimeMapPanel, BoxLayout.Y_AXIS));
        refreshRealtimeMap();

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(realtimeMapPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomRow(String floorName, List<RoomBadge> rooms) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblFloor = new JLabel(floorName + ":");
        lblFloor.setPreferredSize(new Dimension(56, 32));
        lblFloor.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFloor.setForeground(TEXT_PRIMARY);

        JPanel roomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomPanel.setOpaque(false);
        for (RoomBadge room : rooms) {
            roomPanel.add(createRoomBadge(room));
        }

        row.add(lblFloor, BorderLayout.WEST);
        row.add(roomPanel, BorderLayout.CENTER);
        return row;
    }

    private JPanel createRoomBadge(RoomBadge room) {
        JPanel badge = new JPanel(new BorderLayout());
        badge.setPreferredSize(new Dimension(82, 40));
        badge.setBackground(resolveStatusColor(room.statusCode));
        badge.setBorder(createRoomBadgeBorder(room.statusCode, isRealtimeRoomHighlighted(room.roomCode)));
        badge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        badge.putClientProperty("statusCode", room.statusCode);
        badge.setToolTipText("<html>Phòng: " + room.roomCode
                + "<br>Tầng: " + safeValue(room.floorName, "-")
                + "<br>Loại phòng: " + safeValue(room.roomType, "-")
                + "<br>Trạng thái: " + safeValue(room.statusText, resolveStatusCode(room.statusCode))
                + "</html>");

        JLabel lbl = new JLabel("<html><center>" + room.roomCode + "<br>" + resolveStatusCode(room.statusCode) + "</center></html>", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(resolveStatusTextColor(room.statusCode));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleRealtimeRoomSelection(room);
            }
        };
        badge.addMouseListener(clickHandler);
        lbl.addMouseListener(clickHandler);
        badge.add(lbl, BorderLayout.CENTER);
        realtimeRoomBadges.put(room.roomCode, badge);
        return badge;
    }

    private JPanel buildLegendPanel() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        legend.add(createLegendItem("Ho\u1ea1t \u0111\u1ed9ng", resolveStatusColor("T")));
        legend.add(createLegendItem("\u0110\u00e3 \u0111\u1eb7t", resolveStatusColor("D")));
        legend.add(createLegendItem("\u0110ang \u1edf", resolveStatusColor("O")));
        legend.add(createLegendItem("B\u1ea3o tr\u00ec", resolveStatusColor("B")));
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
        comboBox.setMaximumSize(new Dimension(160, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(150, 34));
        field.setMaximumSize(new Dimension(280, 34));
        return field;
    }

    private JComboBox<DichVu> createServiceComboBox(List<DichVu> services) {
        JComboBox<DichVu> comboBox = new JComboBox<DichVu>(services.toArray(new DichVu[0]));
        comboBox.setFont(BODY_FONT);
        comboBox.setPreferredSize(new Dimension(150, 34));
        comboBox.setMaximumSize(new Dimension(280, 34));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DichVu) {
                    DichVu dichVu = (DichVu) value;
                    setText("DV" + dichVu.getMaDichVu() + " - " + safeValue(dichVu.getTenDichVu(), "D\u1ecbch v\u1ee5"));
                } else if (value == null) {
                    setText("Ch\u1ecdn d\u1ecbch v\u1ee5");
                }
                return this;
            }
        });
        return comboBox;
    }

    private void updateServiceReferencePrice(JComboBox<DichVu> cboDichVu, JTextField txtDonGia) {
        DichVu dichVu = getSelectedDichVu(cboDichVu);
        txtDonGia.setText(dichVu == null ? "0" : formatMoney(dichVu.getDonGia()));
    }

    private DichVu getSelectedDichVu(JComboBox<DichVu> cboDichVu) {
        Object selected = cboDichVu == null ? null : cboDichVu.getSelectedItem();
        return selected instanceof DichVu ? (DichVu) selected : null;
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
        label.setFont(AppFonts.ui(Font.BOLD, 13));
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

    private void reloadSampleData(boolean showMessage) {
        loadStayData();
        cboTrangThai.setSelectedIndex(0);
        cboTang.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        cboCaLam.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        refreshRealtimeMap();
        if (showMessage) {
            showInfo("\u0110\u00e3 l\u00e0m m\u1edbi d\u1eef li\u1ec7u check-in / check-out.");
        }
    }

        private void loadStayData() {
        allRecords.clear();
        Map<Integer, StayRecord> grouped = new LinkedHashMap<Integer, StayRecord>();
        loadPendingBookings(grouped);
        loadActiveStays(grouped);
        for (StayRecord record : grouped.values()) {
            record.refreshAggregateState();
        }
        allRecords.addAll(grouped.values());
    }

    private void loadPendingBookings(Map<Integer, StayRecord> grouped) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);

        String sql = "SELECT ctdp.maChiTietDatPhong, dp.maDatPhong, ISNULL(kh.hoTen, N'-') AS hoTen, dp.tienCoc, "
                + "COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))) AS checkInDuKien, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, "
                + "dp.trangThai, ctdp.soNguoi, " +
                "ctdp.maPhong, ISNULL(p.soPhong, N'Ch\u01b0a g\u00e1n') AS soPhong, ISNULL(p.tang, N'-') AS tang, " +
                "ISNULL(lp.tenLoaiPhong, lp2.tenLoaiPhong) AS tenLoaiPhong " +
                "FROM DatPhong dp " +
                "JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
                "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "LEFT JOIN BangGia bg ON dp.maBangGia = bg.maBangGia " +
                "LEFT JOIN LoaiPhong lp2 ON bg.maLoaiPhong = lp2.maLoaiPhong " +
                "WHERE ISNULL(dp.trangThai, N'') IN (N'\u0110\u00e3 \u0111\u1eb7t', N'\u0110\u00e3 x\u00e1c nh\u1eadn', N'\u0110\u00e3 c\u1ecdc', N'Ch\u1edd check-in', N'\u0110ang \u1edf', N'Check-out m\u1ed9t ph\u1ea7n', N'\u0110\u00e3 check-in') " +
                "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong) " +
                "ORDER BY dp.maDatPhong DESC, ctdp.maChiTietDatPhong ASC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int bookingId = rs.getInt("maDatPhong");
                StayRecord record = grouped.get(Integer.valueOf(bookingId));

                if (record == null) {
                    record = new StayRecord();
                    record.maHoSo = "DP" + bookingId;
                    record.maLuuTru = 0;
                    record.maDatPhong = bookingId;
                    record.khachHang = safeValue(rs.getString("hoTen"), "-");
                    record.loaiPhong = safeValue(rs.getString("tenLoaiPhong"), "-");
                    record.tienCoc = formatMoney(rs.getDouble("tienCoc"));
                    record.dichVuPhatSinh = "0";
                    Timestamp checkInDuKien = rs.getTimestamp("checkInDuKien");
                    Timestamp checkOutDuKien = rs.getTimestamp("checkOutDuKien");
                    record.gioVao = checkInDuKien == null ? "-" : formatDateTime(checkInDuKien);
                    record.gioRaDuKien = checkOutDuKien == null ? "-" : formatDateTime(checkOutDuKien);
                    record.tang = safeValue(rs.getString("tang"), "-");
                    record.caLam = resolveCurrentShift();
                    record.bookingTrangThai = safeValue(rs.getString("trangThai"), "Ch\u1edd check-in");
                    record.expectedCheckInDate = checkInDuKien == null ? LocalDate.now() : checkInDuKien.toLocalDateTime().toLocalDate();
                    record.expectedCheckOutDate = checkOutDuKien == null ? LocalDate.now().plusDays(1) : checkOutDuKien.toLocalDateTime().toLocalDate();
                    grouped.put(Integer.valueOf(bookingId), record);
                }

                int maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                int maPhongId = rs.getObject("maPhong") == null ? 0 : rs.getInt("maPhong");

                record.soNguoi += rs.getInt("soNguoi");
                record.hasPendingCheckInRooms = true;
                record.addBookingDetail(maChiTietDatPhong);
                Timestamp checkInDuKien = rs.getTimestamp("checkInDuKien");
                Timestamp checkOutDuKien = rs.getTimestamp("checkOutDuKien");
                if (checkInDuKien != null) {
                    LocalDate current = checkInDuKien.toLocalDateTime().toLocalDate();
                    if (record.expectedCheckInDate == null || current.isBefore(record.expectedCheckInDate)) {
                        record.expectedCheckInDate = current;
                        record.gioVao = formatDateTime(checkInDuKien);
                    }
                }
                if (checkOutDuKien != null) {
                    LocalDate current = checkOutDuKien.toLocalDateTime().toLocalDate();
                    if (record.expectedCheckOutDate == null || current.isAfter(record.expectedCheckOutDate)) {
                        record.expectedCheckOutDate = current;
                        record.gioRaDuKien = formatDateTime(checkOutDuKien);
                    }
                }

                if (maPhongId > 0) {
                    record.addRoom(maPhongId, safeValue(rs.getString("soPhong"), "-"));
                } else {
                    record.hasUnassignedRoom = true;
                    record.addDisplayRoom("Ch\u01b0a g\u00e1n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch booking ch\u1edd check-in.");
        }
    }

    private void loadActiveStays(Map<Integer, StayRecord> grouped) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, kh.hoTen, p.soPhong, lp.tenLoaiPhong, p.trangThai, p.tang, lt.tienCoc, dp.trangThai AS trangThaiDatPhong, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, ctdp.soNguoi, " +
                "ISNULL(SUM(sddv.thanhTien), 0) AS tienDichVu, lt.checkIn, lt.checkOut " +
                "FROM LuuTru lt " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "JOIN ChiTietDatPhong ctdp ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang " +
                "JOIN Phong p ON lt.maPhong = p.maPhong " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "LEFT JOIN SuDungDichVu sddv ON lt.maLuuTru = sddv.maLuuTru " +
                "WHERE lt.checkOut IS NULL " +
                "GROUP BY lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, kh.hoTen, p.soPhong, lp.tenLoaiPhong, p.trangThai, p.tang, lt.tienCoc, dp.trangThai, ctdp.checkOutDuKien, dp.ngayTraPhong, ctdp.soNguoi, lt.checkIn, lt.checkOut " +
                "ORDER BY lt.maDatPhong DESC, CASE WHEN TRY_CAST(p.soPhong AS INT) IS NULL THEN 1 ELSE 0 END, TRY_CAST(p.soPhong AS INT), p.soPhong, lt.maLuuTru";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int bookingId = rs.getInt("maDatPhong");
                StayRecord record = grouped.get(Integer.valueOf(bookingId));
                if (record == null) {
                    record = new StayRecord();
                    record.maHoSo = "DP" + bookingId;
                    record.maDatPhong = bookingId;
                    record.khachHang = safeValue(rs.getString("hoTen"), "-");
                    record.loaiPhong = safeValue(rs.getString("tenLoaiPhong"), "-");
                    record.tienCoc = "0";
                    record.dichVuPhatSinh = "0";
                    record.tang = safeValue(rs.getString("tang"), "-");
                    record.caLam = resolveCurrentShift();
                    record.bookingTrangThai = safeValue(rs.getString("trangThaiDatPhong"), "\u0110ang \u1edf");
                    Timestamp expectedCheckOut = rs.getTimestamp("checkOutDuKien");
                    if (expectedCheckOut != null) {
                        record.expectedCheckOutDate = expectedCheckOut.toLocalDateTime().toLocalDate();
                        record.gioRaDuKien = formatDateTime(expectedCheckOut);
                    }
                    grouped.put(Integer.valueOf(bookingId), record);
                }

                int maLuuTru = rs.getInt("maLuuTru");
                int maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                int maPhong = rs.getInt("maPhong");
                record.bookingTrangThai = safeValue(rs.getString("trangThaiDatPhong"), record.bookingTrangThai);
                record.hasActiveStayRooms = true;
                record.addStay(maLuuTru, maChiTietDatPhong, maPhong, safeValue(rs.getString("soPhong"), "-"), safeValue(rs.getString("tenLoaiPhong"), "-"));
                record.soNguoi += rs.getInt("soNguoi");
                record.tienCoc = formatMoney(parseDoubleMoney(record.tienCoc) + rs.getDouble("tienCoc"));
                record.dichVuPhatSinh = formatMoney(parseDoubleMoney(record.dichVuPhatSinh) + rs.getDouble("tienDichVu"));

                Timestamp checkInTs = rs.getTimestamp("checkIn");
                if (checkInTs != null) {
                    LocalDateTime current = checkInTs.toLocalDateTime();
                    if (record.expectedCheckInDate == null || current.toLocalDate().isBefore(record.expectedCheckInDate)) {
                        record.expectedCheckInDate = current.toLocalDate();
                        record.gioVao = DATE_FORMAT.format(current.toLocalDate()) + " " + TIME_FORMAT.format(current.toLocalTime());
                    }
                }
                Timestamp expectedCheckOut = rs.getTimestamp("checkOutDuKien");
                if (expectedCheckOut != null) {
                    LocalDate current = expectedCheckOut.toLocalDateTime().toLocalDate();
                    if (record.expectedCheckOutDate == null || current.isAfter(record.expectedCheckOutDate)) {
                        record.expectedCheckOutDate = current;
                        record.gioRaDuKien = formatDateTime(expectedCheckOut);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Kh\u00f4ng th\u1ec3 t\u1ea3i d\u1eef li\u1ec7u l\u01b0u tr\u00fa.");
        }
    }

    private void loadCompletedBookings(Map<Integer, StayRecord> grouped) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);

        String sql = "SELECT ctdp.maChiTietDatPhong, dp.maDatPhong, ISNULL(kh.hoTen, N'-') AS hoTen, dp.tienCoc, "
                + "COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))) AS checkInDuKien, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, "
                + "dp.trangThai, ctdp.soNguoi, " +
                "ctdp.maPhong, ISNULL(p.soPhong, N'Ch\u01b0a g\u00e1n') AS soPhong, ISNULL(p.tang, N'-') AS tang, " +
                "ISNULL(lp.tenLoaiPhong, lp2.tenLoaiPhong) AS tenLoaiPhong, " +
                "ISNULL((SELECT SUM(sddv.thanhTien) FROM LuuTru lt2 LEFT JOIN SuDungDichVu sddv ON sddv.maLuuTru = lt2.maLuuTru WHERE lt2.maDatPhong = dp.maDatPhong), 0) AS tienDichVu " +
                "FROM DatPhong dp " +
                "JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong " +
                "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "LEFT JOIN BangGia bg ON dp.maBangGia = bg.maBangGia " +
                "LEFT JOIN LoaiPhong lp2 ON bg.maLoaiPhong = lp2.maLoaiPhong " +
                "WHERE ISNULL(dp.trangThai, N'') IN (N'Ch\u1edd thanh to\u00e1n', N'\u0110\u00e3 thanh to\u00e1n', N'\u0110\u00e3 check-out') " +
                "ORDER BY dp.maDatPhong DESC, ctdp.maChiTietDatPhong ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int bookingId = rs.getInt("maDatPhong");
                StayRecord record = grouped.get(Integer.valueOf(bookingId));
                if (record == null) {
                    record = new StayRecord();
                    record.maHoSo = "DP" + bookingId;
                    record.maDatPhong = bookingId;
                    record.khachHang = safeValue(rs.getString("hoTen"), "-");
                    record.loaiPhong = safeValue(rs.getString("tenLoaiPhong"), "-");
                    record.tienCoc = formatMoney(rs.getDouble("tienCoc"));
                    record.dichVuPhatSinh = formatMoney(rs.getDouble("tienDichVu"));
                    record.tang = safeValue(rs.getString("tang"), "-");
                    record.caLam = resolveCurrentShift();
                    record.bookingTrangThai = safeValue(rs.getString("trangThai"), "Ch\u1edd thanh to\u00e1n");
                    Timestamp checkInDuKien = rs.getTimestamp("checkInDuKien");
                    Timestamp checkOutDuKien = rs.getTimestamp("checkOutDuKien");
                    record.expectedCheckInDate = checkInDuKien == null ? null : checkInDuKien.toLocalDateTime().toLocalDate();
                    record.expectedCheckOutDate = checkOutDuKien == null ? null : checkOutDuKien.toLocalDateTime().toLocalDate();
                    record.gioVao = checkInDuKien == null ? "-" : formatDateTime(checkInDuKien);
                    record.gioRaDuKien = checkOutDuKien == null ? "-" : formatDateTime(checkOutDuKien);
                    grouped.put(Integer.valueOf(bookingId), record);
                }

                record.soNguoi += rs.getInt("soNguoi");
                record.addBookingDetail(rs.getInt("maChiTietDatPhong"));
                Timestamp checkInDuKien = rs.getTimestamp("checkInDuKien");
                Timestamp checkOutDuKien = rs.getTimestamp("checkOutDuKien");
                if (checkInDuKien != null) {
                    LocalDate current = checkInDuKien.toLocalDateTime().toLocalDate();
                    if (record.expectedCheckInDate == null || current.isBefore(record.expectedCheckInDate)) {
                        record.expectedCheckInDate = current;
                        record.gioVao = formatDateTime(checkInDuKien);
                    }
                }
                if (checkOutDuKien != null) {
                    LocalDate current = checkOutDuKien.toLocalDateTime().toLocalDate();
                    if (record.expectedCheckOutDate == null || current.isAfter(record.expectedCheckOutDate)) {
                        record.expectedCheckOutDate = current;
                        record.gioRaDuKien = formatDateTime(checkOutDuKien);
                    }
                }
                int maPhong = rs.getObject("maPhong") == null ? 0 : rs.getInt("maPhong");
                if (maPhong > 0) {
                    record.addRoom(maPhong, safeValue(rs.getString("soPhong"), "-"));
                } else {
                    record.hasUnassignedRoom = true;
                    record.addDisplayRoom("Ch\u01b0a g\u00e1n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch booking \u0111\u00e3 check-out.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredRecords.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tang = valueOf(cboTang.getSelectedItem());
        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String caLam = valueOf(cboCaLam.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (StayRecord record : allRecords) {
            if (!"T\u1ea5t c\u1ea3".equals(trangThai) && !record.trangThai.equals(trangThai)) {
                continue;
            }
            if (!"T\u1ea5t c\u1ea3".equals(tang) && !record.tang.equals(tang)) {
                continue;
            }
            if (!"T\u1ea5t c\u1ea3".equals(loaiPhong) && !record.loaiPhong.equals(loaiPhong)) {
                continue;
            }
            if (!"T\u1ea5t c\u1ea3".equals(caLam) && !record.caLam.equals(caLam)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (record.maDatPhong + " " + record.soPhong + " " + record.khachHang).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredRecords.add(record);
        }

        refillTable();
        if (showMessage) {
            showInfo("\u0110\u00e3 l\u1ecdc \u0111\u01b0\u1ee3c " + filteredRecords.size() + " h\u1ed3 s\u01a1 ph\u00f9 h\u1ee3p.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (StayRecord record : filteredRecords) {
            tableModel.addRow(new Object[]{
                    record.maHoSo,
                    record.khachHang,
                    record.soPhong,
                    record.soLuongPhong,
                    record.gioVao,
                    record.gioRaDuKien,
                    record.trangThai
            });
        }

        if (!filteredRecords.isEmpty()) {
            int rowToSelect = resolvePreferredSelectionIndex();
            tblLuuTru.setRowSelectionInterval(rowToSelect, rowToSelect);
            updateDetailPanel(filteredRecords.get(rowToSelect), null);
            clearPendingFocusedBookingIfMatched(filteredRecords.get(rowToSelect).maDatPhong);
        } else {
            clearDetailPanel();
        }
    }

    private int resolvePreferredSelectionIndex() {
        Integer bookingId = pendingFocusedBookingId;
        if (bookingId != null) {
            for (int i = 0; i < filteredRecords.size(); i++) {
                if (filteredRecords.get(i).maDatPhong == bookingId.intValue()) {
                    return i;
                }
            }
        }
        return 0;
    }

    private static synchronized void clearPendingFocusedBookingIfMatched(int maDatPhong) {
        if (pendingFocusedBookingId != null && pendingFocusedBookingId.intValue() == maDatPhong) {
            pendingFocusedBookingId = null;
        }
    }

    private void updateDetailPanel(StayRecord record) {
        updateDetailPanel(record, null);
    }

    private void updateDetailPanel(StayRecord record, String focusedRoomCode) {
        lblMaHoSo.setText(record.maHoSo);
        lblMaDatPhong.setText("DP" + record.maDatPhong);
        lblKhachHang.setText(record.khachHang);
        lblSoPhong.setText(buildDetailRoomText(record, focusedRoomCode));
        lblLoaiPhongChiTiet.setText(record.loaiPhong);
        lblTrangThaiPhong.setText(record.trangThaiPhong);
        lblTienCoc.setText(record.tienCoc);
        lblDichVuPhatSinh.setText(record.dichVuPhatSinh);
        txtGhiChu.setText(buildDetailNoteText(record, focusedRoomCode));
        txtGhiChu.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        clearRealtimeMapHighlightFromTable();
        lblMaHoSo.setText("-");
        lblMaDatPhong.setText("-");
        lblKhachHang.setText("-");
        lblSoPhong.setText("-");
        lblLoaiPhongChiTiet.setText("-");
        lblTrangThaiPhong.setText("-");
        lblTienCoc.setText("-");
        lblDichVuPhatSinh.setText("-");
        txtGhiChu.setText("Kh\u00f4ng c\u00f3 d\u1eef li\u1ec7u ph\u00f9 h\u1ee3p.");
    }

    private String buildDetailRoomText(StayRecord record, String focusedRoomCode) {
        String roomText = safeValue(record == null ? null : record.soPhong, "-");
        if (record != null && focusedRoomCode != null && record.containsRoomCode(focusedRoomCode)) {
            return roomText + " (đang chọn: " + focusedRoomCode + ")";
        }
        return roomText;
    }

    private String buildDetailNoteText(StayRecord record, String focusedRoomCode) {
        String note = safeValue(record == null ? null : record.ghiChu, "-");
        if (record != null && focusedRoomCode != null && record.containsRoomCode(focusedRoomCode)) {
            return "Phòng đang chọn trên sơ đồ: " + focusedRoomCode + ".\n" + note;
        }
        return note;
    }

    private void updateDetailPanelForStandaloneRoom(RoomBadge room) {
        lblMaHoSo.setText("-");
        lblMaDatPhong.setText("-");
        lblKhachHang.setText("-");
        lblSoPhong.setText(room == null ? "-" : room.roomCode + " - " + safeValue(room.floorName, "-"));
        lblLoaiPhongChiTiet.setText(room == null ? "-" : safeValue(room.roomType, "-"));
        lblTrangThaiPhong.setText(room == null ? "-" : safeValue(room.statusText, resolveStatusCode(room.statusCode)));
        lblTienCoc.setText("-");
        lblDichVuPhatSinh.setText("-");
        if (room == null) {
            txtGhiChu.setText("Kh\u00f4ng c\u00f3 d\u1eef li\u1ec7u ph\u00f9 h\u1ee3p.");
        } else if ("B".equalsIgnoreCase(room.statusCode)) {
            txtGhiChu.setText("Phòng " + room.roomCode + " đang ở trạng thái bảo trì. Hiện chưa có hồ sơ lưu trú hoặc booking khả dụng.");
        } else {
            txtGhiChu.setText("Phòng " + room.roomCode + " hiện chưa có lưu trú. Trạng thái hiện tại: "
                    + safeValue(room.statusText, resolveStatusCode(room.statusCode)) + ".");
        }
        txtGhiChu.setCaretPosition(0);
    }

    private StayRecord findRecordByRoomCode(List<StayRecord> source, String roomCode) {
        if (roomCode == null || roomCode.trim().isEmpty() || source == null) {
            return null;
        }
        for (StayRecord record : source) {
            if (record != null && record.containsRoomCode(roomCode)) {
                return record;
            }
        }
        return null;
    }

    private int findFilteredRowIndexByRoomCode(String roomCode) {
        if (roomCode == null || roomCode.trim().isEmpty()) {
            return -1;
        }
        for (int i = 0; i < filteredRecords.size(); i++) {
            if (filteredRecords.get(i).containsRoomCode(roomCode)) {
                return i;
            }
        }
        return -1;
    }

    private void syncRealtimeMapHighlightFromTable(StayRecord record) {
        selectedRealtimeRoomCode = null;
        highlightedRealtimeRoomCodes.clear();
        if (record != null) {
            for (String roomCode : record.getRoomCodes()) {
                String normalizedRoomCode = normalizeRealtimeRoomCode(roomCode);
                if (!normalizedRoomCode.isEmpty()) {
                    highlightedRealtimeRoomCodes.add(normalizedRoomCode);
                }
            }
        }
        refreshSelectedRealtimeRoomStyle();
    }

    private void clearRealtimeMapHighlightFromTable() {
        selectedRealtimeRoomCode = null;
        if (!highlightedRealtimeRoomCodes.isEmpty()) {
            highlightedRealtimeRoomCodes.clear();
        }
        refreshSelectedRealtimeRoomStyle();
    }

    private String normalizeRealtimeRoomCode(String roomCode) {
        if (roomCode == null) {
            return "";
        }
        String normalized = roomCode.trim();
        if (normalized.isEmpty()
                || "-".equals(normalized)
                || "Chưa gán".equalsIgnoreCase(normalized)) {
            return "";
        }
        return normalized;
    }

    private void handleRealtimeRoomSelection(RoomBadge room) {
        if (room == null) {
            return;
        }
        highlightedRealtimeRoomCodes.clear();
        selectedRealtimeRoomCode = room.roomCode;
        refreshSelectedRealtimeRoomStyle();

        StayRecord detailRecord = findRecordByRoomCode(allRecords, room.roomCode);
        int tableRow = findFilteredRowIndexByRoomCode(room.roomCode);
        if (tableRow >= 0) {
            tblLuuTru.setRowSelectionInterval(tableRow, tableRow);
            tblLuuTru.scrollRectToVisible(tblLuuTru.getCellRect(tableRow, 0, true));
            updateDetailPanel(filteredRecords.get(tableRow), room.roomCode);
            return;
        }

        tblLuuTru.clearSelection();
        selectedRealtimeRoomCode = room.roomCode;
        refreshSelectedRealtimeRoomStyle();
        if (detailRecord != null) {
            updateDetailPanel(detailRecord, room.roomCode);
        } else {
            updateDetailPanelForStandaloneRoom(room);
        }
    }

    private StayRecord getSelectedRecord() {
        int row = tblLuuTru.getSelectedRow();
        if (row < 0 || row >= filteredRecords.size()) {
            showInfo("Vui l\u00f2ng ch\u1ecdn m\u1ed9t h\u1ed3 s\u01a1 trong b\u1ea3ng.");
            return null;
        }
        return filteredRecords.get(row);
    }

        private void openCheckInDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!record.hasPendingCheckInRooms) {
            showInfo("\u0110\u01a1n n\u00e0y kh\u00f4ng c\u00f2n ph\u00f2ng n\u00e0o ch\u1edd check-in.");
            return;
        }
        new CheckInDialog(this, record).setVisible(true);
    }

    private void openAddServiceDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        List<ServiceStayOption> activeOptions = resolveActiveServiceOptions(record);
        if (activeOptions.isEmpty()) {
            showInfo("Ch\u1ec9 booking c\u00f2n ph\u00f2ng \u0111ang \u1edf m\u1edbi th\u00eam d\u1ecbch v\u1ee5.");
            return;
        }
        new AddServiceDialog(this, record, activeOptions).setVisible(true);
    }

    private void openChangeRoomDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!record.hasActiveStayRooms) {
            showInfo("Ch\u1ec9 booking c\u00f2n ph\u00f2ng \u0111ang \u1edf m\u1edbi \u0111\u1ed5i ph\u00f2ng.");
            return;
        }
        new ChangeRoomDialog(this, record).setVisible(true);
    }

    private void openExtendDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!record.hasActiveStayRooms) {
            showInfo("Ch\u1ec9 booking c\u00f2n ph\u00f2ng \u0111ang \u1edf m\u1edbi gia h\u1ea1n.");
            return;
        }
        new ExtendStayDialog(this, record).setVisible(true);
    }

    private void openCheckOutDialog() {
        StayRecord record = getSelectedRecord();
        if (record == null) {
            return;
        }
        if (!record.hasActiveStayRooms) {
            showInfo("Ch\u1ec9 booking c\u00f2n ph\u00f2ng \u0111ang \u1edf m\u1edbi check-out.");
            return;
        }
        new CheckOutDialog(this, record).setVisible(true);
    }

    private void refreshRealtimeMap() {
        if (realtimeMapPanel == null) {
            return;
        }
        realtimeMapPanel.removeAll();
        realtimeRoomBadges.clear();

        List<String> floors = new ArrayList<String>();
        List<List<RoomBadge>> floorRooms = new ArrayList<List<RoomBadge>>();
        Connection con = ConnectDB.getConnection();
        if (con != null) {
            String sql = "SELECT p.maPhong, p.soPhong, p.tang, p.trangThai, ISNULL(lp.tenLoaiPhong, N'-') AS tenLoaiPhong " +
                    "FROM Phong p " +
                    "LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
                    "ORDER BY TRY_CAST(REPLACE(p.tang, N'T\u1ea7ng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tang = safeValue(rs.getString("tang"), "Kh\u00e1c");
                    int idx = floors.indexOf(tang);
                    if (idx < 0) {
                        floors.add(tang);
                        floorRooms.add(new ArrayList<RoomBadge>());
                        idx = floors.size() - 1;
                    }
                    String statusText = safeValue(rs.getString("trangThai"), "B\u1ea3o tr\u00ec");
                    floorRooms.get(idx).add(new RoomBadge(
                            rs.getInt("maPhong"),
                            safeValue(rs.getString("soPhong"), "-"),
                            tang,
                            safeValue(rs.getString("tenLoaiPhong"), "-"),
                            toStatusCode(statusText),
                            statusText
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < floors.size(); i++) {
            realtimeMapPanel.add(buildRoomRow(floors.get(i), floorRooms.get(i)));
            if (i < floors.size() - 1) {
                realtimeMapPanel.add(Box.createVerticalStrut(8));
            }
        }
        if (!floors.isEmpty()) {
            realtimeMapPanel.add(Box.createVerticalStrut(10));
        }
        realtimeMapPanel.add(buildLegendPanel());
        realtimeMapPanel.revalidate();
        realtimeMapPanel.repaint();
        refreshSelectedRealtimeRoomStyle();
    }

    private void refreshSelectedRealtimeRoomStyle() {
        for (Map.Entry<String, JPanel> entry : realtimeRoomBadges.entrySet()) {
            String roomCode = entry.getKey();
            JPanel badge = entry.getValue();
            if (badge == null) {
                continue;
            }
            String statusCode = valueOf(badge.getClientProperty("statusCode"));
            boolean selected = isRealtimeRoomHighlighted(roomCode);
            badge.setBorder(createRoomBadgeBorder(statusCode, selected));
            badge.repaint();
        }
    }

    private boolean isRealtimeRoomHighlighted(String roomCode) {
        String normalizedRoomCode = normalizeRealtimeRoomCode(roomCode);
        if (normalizedRoomCode.isEmpty()) {
            return false;
        }
        if (normalizedRoomCode.equalsIgnoreCase(safeValue(selectedRealtimeRoomCode, ""))) {
            return true;
        }
        for (String highlightedRoomCode : highlightedRealtimeRoomCodes) {
            if (normalizedRoomCode.equalsIgnoreCase(highlightedRoomCode)) {
                return true;
            }
        }
        return false;
    }

    private javax.swing.border.Border createRoomBadgeBorder(String statusCode, boolean selected) {
        if (selected) {
            return BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(37, 99, 235), 3, true),
                    BorderFactory.createLineBorder(Color.WHITE, 1, true)
            );
        }
        return BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true);
    }

    private Color resolveStatusColor(String code) {
        if ("T".equals(code)) {
            return new Color(220, 252, 231);
        }
        if ("D".equals(code)) {
            return new Color(254, 249, 195);
        }
        if ("O".equals(code)) {
            return new Color(96, 165, 250);
        }
        if ("C".equals(code)) {
            return new Color(255, 237, 213);
        }
        return new Color(254, 226, 226);
    }

    private Color resolveStatusTextColor(String code) {
        if ("O".equals(code)) {
            return Color.WHITE;
        }
        return TEXT_PRIMARY;
    }

    private String resolveStatusCode(String code) {
        if ("T".equals(code)) {
            return "Ho\u1ea1t \u0111\u1ed9ng";
        }
        if ("D".equals(code)) {
            return "\u0110\u00e3 \u0111\u1eb7t";
        }
        if ("O".equals(code)) {
            return "\u0110ang \u1edf";
        }
        if ("C".equals(code)) {
            return "D\u1ecdn d\u1eb9p";
        }
        return "B\u1ea3o tr\u00ec";
    }

    private String toStatusCode(String trangThai) {
        if ("Ho\u1ea1t \u0111\u1ed9ng".equalsIgnoreCase(trangThai) || "Tr\u1ed1ng".equalsIgnoreCase(trangThai)) {
            return "T";
        }
        if ("\u0110\u00e3 \u0111\u1eb7t".equalsIgnoreCase(trangThai)) {
            return "D";
        }
        if ("\u0110ang \u1edf".equalsIgnoreCase(trangThai)) {
            return "O";
        }
        if ("D\u1ecdn d\u1eb9p".equalsIgnoreCase(trangThai)) {
            return "C";
        }
        return "B";
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Th\u00f4ng b\u00e1o", JOptionPane.INFORMATION_MESSAGE);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private String formatDate(LocalDate value) {
        return value == null ? "-" : DATE_FORMAT.format(value);
    }

    private String formatDateTime(Timestamp value) {
        if (value == null) {
            return "-";
        }
        LocalDateTime dateTime = value.toLocalDateTime();
        return DATE_FORMAT.format(dateTime.toLocalDate()) + " " + TIME_FORMAT.format(dateTime.toLocalTime());
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }

    private List<ServiceStayOption> resolveActiveServiceOptions(StayRecord record) {
        List<ServiceStayOption> activeOptions = new ArrayList<ServiceStayOption>();
        if (record == null) {
            return activeOptions;
        }
        for (ServiceStayOption option : record.getActiveStayOptions()) {
            if (isStayCurrentlyActive(option.maLuuTru)) {
                activeOptions.add(option);
            }
        }
        return activeOptions;
    }

    private boolean isStayCurrentlyActive(int maLuuTru) {
        if (maLuuTru <= 0) {
            return false;
        }
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(1) FROM LuuTru WHERE maLuuTru = ? AND checkOut IS NULL")) {
            ps.setInt(1, maLuuTru);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveCurrentShift() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Ca s\u00e1ng";
        }
        if (hour < 18) {
            return "Ca chi\u1ec1u";
        }
        return "Ca t\u1ed1i";
    }

    private ActiveStaySnapshot loadActiveStaySnapshot(int maLuuTru) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }
        try {
            return loadActiveStaySnapshot(con, maLuuTru);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ActiveStaySnapshot loadActiveStaySnapshot(Connection con, int maLuuTru) throws Exception {
        if (con == null || maLuuTru <= 0) {
            return null;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, lt.checkIn, lt.soNguoi, lt.giaPhong, lt.tienCoc, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS expectedCheckOut, "
                + "ISNULL(p.soPhong, N'-') AS soPhong, ISNULL(p.tang, N'-') AS tang, "
                + "ISNULL(p.khuVuc, N'-') AS khuVuc, ISNULL(p.maLoaiPhong, 0) AS maLoaiPhong, ISNULL(lp.tenLoaiPhong, N'-') AS tenLoaiPhong "
                + "FROM LuuTru lt "
                + "JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong "
                + "JOIN ChiTietDatPhong ctdp ON ctdp.maChiTietDatPhong = lt.maChiTietDatPhong "
                + "LEFT JOIN Phong p ON p.maPhong = lt.maPhong "
                + "LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "WHERE lt.maLuuTru = ? AND lt.checkOut IS NULL";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLuuTru);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ActiveStaySnapshot(
                            rs.getInt("maLuuTru"),
                            rs.getInt("maChiTietDatPhong"),
                            rs.getInt("maDatPhong"),
                            rs.getInt("maPhong"),
                            rs.getInt("maLoaiPhong"),
                            rs.getInt("soNguoi"),
                            safeValue(rs.getString("soPhong"), "-"),
                            safeValue(rs.getString("tenLoaiPhong"), "-"),
                            safeValue(rs.getString("tang"), "-"),
                            safeValue(rs.getString("khuVuc"), "-"),
                            rs.getTimestamp("checkIn"),
                            normalizeExpectedCheckOut(rs.getTimestamp("expectedCheckOut")),
                            rs.getDouble("giaPhong"),
                            rs.getDouble("tienCoc")
                    );
                }
            }
        }
        return null;
    }

    private Timestamp normalizeExpectedCheckOut(Timestamp value) {
        if (value == null) {
            return null;
        }
        LocalDateTime dateTime = value.toLocalDateTime();
        if (dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return Timestamp.valueOf(LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(12, 0)));
        }
        return value;
    }

    private List<RoomOption> loadAvailableRooms(ActiveStaySnapshot staySnapshot, LocalDateTime changeTime) {
        List<RoomOption> rooms = new ArrayList<RoomOption>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return rooms;
        }
        try {
            return loadAvailableRoomsUsingDao(con, staySnapshot, changeTime);
        } catch (Exception e) {
            e.printStackTrace();
            return rooms;
        }
    }

    // Keep the legacy overload aligned with the DAO-backed room-change filter.
    private boolean shouldUseRoomChangeDaoAvailability() {
        return checkInOutDAO != null;
    }

    private List<RoomOption> loadAvailableRooms(Connection con, ActiveStaySnapshot staySnapshot, LocalDateTime changeTime) throws Exception {
        if (shouldUseRoomChangeDaoAvailability()) {
            return loadAvailableRoomsUsingDao(con, staySnapshot, changeTime);
        }
        List<RoomOption> rooms = new ArrayList<RoomOption>();
        if (con == null || staySnapshot == null || changeTime == null
                || staySnapshot.expectedCheckOut == null
                || !staySnapshot.expectedCheckOut.toLocalDateTime().isAfter(changeTime)) {
            return rooms;
        }

        String sql = "SELECT p.maPhong, p.soPhong, p.tang, ISNULL(p.khuVuc, N'-') AS khuVuc, ISNULL(p.sucChuaToiDa, 0) AS sucChuaToiDa, "
                + "lp.maLoaiPhong, lp.tenLoaiPhong, ISNULL(lp.giaThamChieu, 0) AS giaThamChieu "
                + "FROM Phong p "
                + "JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "WHERE p.maPhong <> ? "
                + "AND ISNULL(p.trangThai, N'') IN (N'Hoạt động', N'Trống', N'Sẵn sàng') "
                + "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maPhong = p.maPhong AND lt.checkOut IS NULL AND lt.maLuuTru <> ?) "
                + "AND NOT EXISTS ("
                + "    SELECT 1 FROM ChiTietDatPhong ctdp "
                + "    JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                + "    WHERE ctdp.maPhong = p.maPhong "
                + "      AND ctdp.maChiTietDatPhong <> ? "
                + "      AND ISNULL(dp.trangThai, N'') IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in') "
                + "      AND dp.ngayNhanPhong < ? "
                + "      AND dp.ngayTraPhong > ? "
                + "      AND NOT EXISTS (SELECT 1 FROM LuuTru ltBooked WHERE ltBooked.maChiTietDatPhong = ctdp.maChiTietDatPhong)"
                + ") "
                + "ORDER BY CASE WHEN lp.maLoaiPhong = ? THEN 0 ELSE 1 END, "
                + "CASE WHEN TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT) IS NULL THEN 1 ELSE 0 END, "
                + "TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int index = 1;
            ps.setInt(index++, staySnapshot.maPhong);
            ps.setInt(index++, staySnapshot.maLuuTru);
            ps.setInt(index++, staySnapshot.maChiTietDatPhong);
            ps.setTimestamp(index++, staySnapshot.expectedCheckOut);
            ps.setTimestamp(index++, Timestamp.valueOf(changeTime));
            ps.setInt(index, staySnapshot.maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new RoomOption(
                            rs.getInt("maPhong"),
                            rs.getInt("maLoaiPhong"),
                            rs.getInt("sucChuaToiDa"),
                            safeValue(rs.getString("soPhong"), "-"),
                            safeValue(rs.getString("tang"), "-"),
                            safeValue(rs.getString("khuVuc"), "-"),
                            safeValue(rs.getString("tenLoaiPhong"), "-"),
                            rs.getDouble("giaThamChieu")
                    ));
                }
            }
        }
        return rooms;
    }

    private List<RoomOption> loadAvailableRoomsUsingDao(Connection con, ActiveStaySnapshot staySnapshot, LocalDateTime changeTime) throws Exception {
        List<RoomOption> rooms = new ArrayList<RoomOption>();
        Timestamp expectedCheckOut = staySnapshot == null ? null : normalizeExpectedCheckOut(staySnapshot.expectedCheckOut);
        if (con == null || staySnapshot == null || changeTime == null
                || expectedCheckOut == null
                || !expectedCheckOut.toLocalDateTime().isAfter(changeTime)) {
            return rooms;
        }

        List<CheckInOutDAO.RoomChangeCandidate> candidates = checkInOutDAO.getAvailableRoomsForRoomChange(
                con,
                staySnapshot.maDatPhong,
                staySnapshot.maChiTietDatPhong,
                staySnapshot.maLuuTru,
                staySnapshot.maPhong,
                staySnapshot.maLoaiPhong,
                changeTime,
                expectedCheckOut.toLocalDateTime()
        );
        for (CheckInOutDAO.RoomChangeCandidate candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            rooms.add(new RoomOption(
                    candidate.getMaPhong(),
                    candidate.getMaLoaiPhong(),
                    candidate.getSucChuaToiDa(),
                    safeValue(candidate.getSoPhong(), "-"),
                    safeValue(candidate.getTang(), "-"),
                    safeValue(candidate.getKhuVuc(), "-"),
                    safeValue(candidate.getTenLoaiPhong(), "-"),
                    candidate.getGiaThamChieu()
            ));
        }
        return rooms;
    }

    private void refreshKhachHangViewsSafely() {
        try {
            Class<?> clazz = Class.forName("gui.KhachHangGUI");
            java.lang.reflect.Method method = clazz.getMethod("refreshAllOpenInstances");
            method.invoke(null);
        } catch (Throwable ignored) {
        }
    }

    public static void refreshAllOpenInstances() {
        List<CheckInOutGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new ArrayList<CheckInOutGUI>(OPEN_INSTANCES);
        }
        for (CheckInOutGUI gui : snapshot) {
            if (gui != null) {
                javax.swing.SwingUtilities.invokeLater(() -> gui.reloadSampleData(false));
            }
        }
    }

    public static synchronized void prepareFocusOnBooking(int maDatPhong) {
        pendingFocusedBookingId = maDatPhong > 0 ? Integer.valueOf(maDatPhong) : null;
    }

    public static void focusBookingAcrossOpenInstances(int maDatPhong) {
        prepareFocusOnBooking(maDatPhong);
        List<CheckInOutGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new ArrayList<CheckInOutGUI>(OPEN_INSTANCES);
        }
        for (CheckInOutGUI gui : snapshot) {
            if (gui != null) {
                javax.swing.SwingUtilities.invokeLater(gui::reloadSampleDataForFocusedBooking);
            }
        }
    }

    private void reloadSampleDataForFocusedBooking() {
        reloadSampleData(false);
    }

    private abstract class BaseStayDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseStayDialog(Frame owner, String title, int width, int height) {
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
            lblTitle.setFont(AppFonts.title(18));
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

    private final class CheckInDialog extends BaseStayDialog {
        private static final int COL_CHECK_IN_DATE = 4;
        private static final int COL_CHECK_IN_TIME = 5;
        private static final int COL_CHECK_OUT_DATE = 6;
        private static final int COL_CHECK_OUT_TIME = 7;
        private static final int COL_CCCD = 8;
        private static final int COL_HO_TEN = 9;
        private static final int COL_SDT = 10;
        private static final int COL_NGAY_SINH = 11;
        private static final int COL_EMAIL = 12;
        private static final int COL_DIA_CHI = 13;
        private static final int COL_GHI_CHU = 14;
        private final StayRecord record;
        private final List<CheckInOutDAO.CheckInBookingItem> bookingItems = new ArrayList<CheckInOutDAO.CheckInBookingItem>();
        private final JTable tblRooms;
        private final DefaultTableModel roomTableModel;
        private final AppDatePickerField txtNgayVao;
        private final AppTimePickerField txtGioVao;
        private final AppDatePickerField txtNgayRa;
        private final AppTimePickerField txtGioRa;
        private boolean updatingCustomerCells;
        private boolean updatingScheduleFields;

        private CheckInDialog(Frame owner, StayRecord record) {
            super(owner, "Check-in", 820, 540);
            this.record = record;
            bookingItems.addAll(checkInOutDAO.getBookingCheckInItems(String.valueOf(record.maDatPhong)));
            initializeBookingItemSchedules();

            LocalDateTime initialCheckIn = resolveDefaultCheckIn();
            LocalDateTime initialCheckOut = resolveDefaultCheckOut(initialCheckIn);
            txtNgayVao = new AppDatePickerField(initialCheckIn.toLocalDate().format(DATE_FORMAT), true);
            txtGioVao = new AppTimePickerField(initialCheckIn.toLocalTime().format(TIME_FORMAT), true);
            txtNgayRa = new AppDatePickerField(initialCheckOut.toLocalDate().format(DATE_FORMAT), true);
            txtGioRa = new AppTimePickerField(initialCheckOut.toLocalTime().format(TIME_FORMAT), true);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHECK-IN",
                    "Ch\u1ecdn t\u1eebng ph\u00f2ng \u0111\u1ec3 check-in ri\u00eang ho\u1eb7c d\u00f9ng thao t\u00e1c check-in to\u00e0n b\u1ed9 \u0111\u01a1n cho c\u00e1c ph\u00f2ng \u0111ang ch\u1edd."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "M\u00e3 \u0111\u1eb7t ph\u00f2ng", createValueLabel("DP" + record.maDatPhong));
            addFormRow(form, gbc, 1, "Kh\u00e1ch h\u00e0ng", createValueLabel(record.khachHang));
            addFormRow(form, gbc, 2, "Lo\u1ea1i ph\u00f2ng", createValueLabel(record.loaiPhong));
            addFormRow(form, gbc, 3, "Ph\u00f2ng trong \u0111\u01a1n", createValueLabel(record.soPhong));
            addFormRow(form, gbc, 4, "Ng\u00e0y v\u00e0o", txtNgayVao);
            addFormRow(form, gbc, 5, "Gi\u1edd v\u00e0o", txtGioVao);
            addFormRow(form, gbc, 6, "Ng\u00e0y ra d\u1ef1 ki\u1ebfn", txtNgayRa);
            addFormRow(form, gbc, 7, "Gi\u1edd ra d\u1ef1 ki\u1ebfn", txtGioRa);

            roomTableModel = new DefaultTableModel(
                    new Object[]{"Ph\u00f2ng", "Lo\u1ea1i ph\u00f2ng", "Tr\u1ea1ng th\u00e1i", "S\u1ed1 ng\u01b0\u1eddi",
                            "Ng\u00e0y v\u00e0o", "Gi\u1edd v\u00e0o", "Ng\u00e0y ra DK", "Gi\u1edd ra DK",
                            "CCCD/Passport", "H\u1ecd t\u00ean KH", "S\u0110T", "Ng\u00e0y sinh", "Email", "\u0110\u1ecba ch\u1ec9", "Ghi ch\u00fa"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column >= COL_CCCD;
                }
            };
            tblRooms = new JTable(roomTableModel);
            tblRooms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblRooms.setRowHeight(28);
            tblRooms.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            configureRoomTableColumns();
            registerCustomerAutoFillListener();
            registerScheduleFieldListeners();
            registerRoomSelectionListener();
            refillRoomTable();
            loadSelectedRoomScheduleIntoEditor();

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.NORTH);

            JPanel roomPanel = new JPanel(new BorderLayout(0, 8));
            roomPanel.setOpaque(false);
            JLabel lblRooms = new JLabel("Danh s\u00e1ch ph\u00f2ng trong \u0111\u01a1n");
            lblRooms.setFont(AppFonts.section(14));
            lblRooms.setForeground(TEXT_PRIMARY);
            JPanel roomHeader = new JPanel();
            roomHeader.setOpaque(false);
            roomHeader.setLayout(new BoxLayout(roomHeader, BoxLayout.Y_AXIS));
            roomHeader.add(lblRooms);
            roomHeader.add(Box.createVerticalStrut(4));
            JLabel lblRoomHint = new JLabel("4 \u00f4 ng\u00e0y/gi\u1edd ph\u00eda tr\u00ean \u0111ang ch\u1ec9nh cho d\u00f2ng ph\u00f2ng \u0111ang ch\u1ecdn.");
            lblRoomHint.setFont(AppFonts.body(12));
            lblRoomHint.setForeground(TEXT_MUTED);
            roomHeader.add(lblRoomHint);
            roomPanel.add(roomHeader, BorderLayout.NORTH);
            roomPanel.add(new JScrollPane(tblRooms), BorderLayout.CENTER);
            card.add(roomPanel, BorderLayout.CENTER);

            content.add(card, BorderLayout.CENTER);

            JButton btnCheckInSelected = createPrimaryButton(
                    "Check-in ph\u00f2ng \u0111\u00e3 ch\u1ecdn",
                    new Color(22, 163, 74),
                    Color.WHITE,
                    e -> submit(false)
            );
            JButton btnCheckInAll = createPrimaryButton(
                    "Check-in to\u00e0n b\u1ed9 \u0111\u01a1n",
                    new Color(21, 128, 61),
                    Color.WHITE,
                    e -> submit(true)
            );
            JButton btnCancel = createOutlineButton("H\u1ee7y", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnCheckInSelected, btnCheckInAll), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void initializeBookingItemSchedules() {
            LocalDateTime fallbackCheckIn = resolveDefaultCheckIn();
            LocalDateTime fallbackCheckOut = resolveDefaultCheckOut(fallbackCheckIn);
            for (CheckInOutDAO.CheckInBookingItem item : bookingItems) {
                LocalDateTime checkIn = item.getExpectedCheckIn() == null ? fallbackCheckIn : item.getExpectedCheckIn();
                LocalDateTime checkOut = item.getExpectedCheckOut() == null ? fallbackCheckOut : item.getExpectedCheckOut();
                if (!checkOut.isAfter(checkIn)) {
                    checkOut = checkIn.plusDays(1);
                }
                item.setExpectedCheckIn(checkIn);
                item.setExpectedCheckOut(checkOut);
            }
        }

        private LocalDateTime resolveDefaultCheckIn() {
            if (record.expectedCheckInDate != null) {
                return LocalDateTime.of(record.expectedCheckInDate, DETAIL_BOOKING_BOUNDARY_TIME);
            }
            return LocalDateTime.of(LocalDate.now(), DETAIL_BOOKING_BOUNDARY_TIME);
        }

        private LocalDateTime resolveDefaultCheckOut(LocalDateTime checkIn) {
            LocalDate checkOutDate = record.expectedCheckOutDate;
            if (checkOutDate == null) {
                return (checkIn == null ? LocalDateTime.of(LocalDate.now().plusDays(1), DETAIL_BOOKING_BOUNDARY_TIME) : checkIn.plusDays(1));
            }
            LocalDateTime checkOut = LocalDateTime.of(checkOutDate, DETAIL_BOOKING_BOUNDARY_TIME);
            if (checkIn != null && !checkOut.isAfter(checkIn)) {
                return checkIn.plusDays(1);
            }
            return checkOut;
        }

        private void registerRoomSelectionListener() {
            tblRooms.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                loadSelectedRoomScheduleIntoEditor();
            });
        }

        private void registerScheduleFieldListeners() {
            txtNgayVao.addTextChangeListener(this::syncSelectedRoomScheduleFromEditor);
            txtGioVao.addTextChangeListener(this::syncSelectedRoomScheduleFromEditor);
            txtNgayRa.addTextChangeListener(this::syncSelectedRoomScheduleFromEditor);
            txtGioRa.addTextChangeListener(this::syncSelectedRoomScheduleFromEditor);
        }

        private CheckInOutDAO.CheckInBookingItem getSelectedBookingItem() {
            int row = tblRooms.getSelectedRow();
            if (row < 0 || row >= bookingItems.size()) {
                return null;
            }
            return bookingItems.get(row);
        }

        private void loadSelectedRoomScheduleIntoEditor() {
            CheckInOutDAO.CheckInBookingItem selected = getSelectedBookingItem();
            LocalDateTime checkIn = selected == null ? resolveDefaultCheckIn() : selected.getExpectedCheckIn();
            LocalDateTime checkOut = selected == null ? resolveDefaultCheckOut(checkIn) : selected.getExpectedCheckOut();
            if (checkIn == null) {
                checkIn = resolveDefaultCheckIn();
            }
            if (checkOut == null || !checkOut.isAfter(checkIn)) {
                checkOut = resolveDefaultCheckOut(checkIn);
            }
            if (selected != null) {
                selected.setExpectedCheckIn(checkIn);
                selected.setExpectedCheckOut(checkOut);
                refreshScheduleCellsForItem(selected);
            }
            updatingScheduleFields = true;
            try {
                txtNgayVao.setDateValue(checkIn.toLocalDate());
                txtGioVao.setTimeValue(checkIn.toLocalTime());
                txtNgayRa.setDateValue(checkOut.toLocalDate());
                txtGioRa.setTimeValue(checkOut.toLocalTime());
            } finally {
                updatingScheduleFields = false;
            }
        }

        private void syncSelectedRoomScheduleFromEditor() {
            if (updatingScheduleFields) {
                return;
            }
            CheckInOutDAO.CheckInBookingItem selected = getSelectedBookingItem();
            if (selected == null) {
                return;
            }
            LocalDate ngayVao = txtNgayVao.getDateValue();
            LocalTime gioVao = txtGioVao.getTimeValue();
            LocalDate ngayRa = txtNgayRa.getDateValue();
            LocalTime gioRa = txtGioRa.getTimeValue();
            if (ngayVao == null || ngayRa == null || gioVao == null || gioRa == null) {
                return;
            }
            if (ngayVao != null && gioVao != null) {
                selected.setExpectedCheckIn(LocalDateTime.of(ngayVao, gioVao));
            }
            if (ngayRa != null && gioRa != null) {
                selected.setExpectedCheckOut(LocalDateTime.of(ngayRa, gioRa));
            }
            refreshScheduleCellsForItem(selected);
        }

        private void refillRoomTable() {
            roomTableModel.setRowCount(0);
            for (CheckInOutDAO.CheckInBookingItem item : bookingItems) {
                roomTableModel.addRow(new Object[]{
                        safeValue(item.getSoPhong(), "-"),
                        safeValue(item.getTenLoaiPhong(), "-"),
                        safeValue(item.getTrangThai(), "-"),
                        item.getSoNguoi(),
                        formatScheduleDate(item.getExpectedCheckIn()),
                        formatScheduleTime(item.getExpectedCheckIn()),
                        formatScheduleDate(item.getExpectedCheckOut()),
                        formatScheduleTime(item.getExpectedCheckOut()),
                        safeValue(item.getCccdPassport(), ""),
                        safeValue(item.getHoTenKhach(), ""),
                        safeValue(item.getSoDienThoai(), ""),
                        safeValue(item.getNgaySinh(), ""),
                        safeValue(item.getEmail(), ""),
                        safeValue(item.getDiaChi(), ""),
                        safeValue(item.getGhiChu(), "")
                });
            }
            selectFirstPendingRoom();
        }

        private void configureRoomTableColumns() {
            tblRooms.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            int[] preferredWidths = {70, 110, 100, 60, 95, 70, 95, 70, 120, 160, 105, 95, 170, 190, 180};
            int[] minWidths = {55, 90, 90, 50, 90, 65, 90, 65, 105, 130, 90, 85, 130, 140, 140};
            int[] maxWidths = {85, 160, 140, 70, 110, 80, 110, 80, 150, 260, 130, 110, 260, 320, 320};
            for (int i = 0; i < preferredWidths.length && i < tblRooms.getColumnModel().getColumnCount(); i++) {
                tblRooms.getColumnModel().getColumn(i).setPreferredWidth(preferredWidths[i]);
                tblRooms.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
                tblRooms.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);
            }
        }

        private String formatScheduleDate(LocalDateTime value) {
            return value == null ? "-" : value.toLocalDate().format(DATE_FORMAT);
        }

        private String formatScheduleTime(LocalDateTime value) {
            return value == null ? "-" : value.toLocalTime().format(TIME_FORMAT);
        }

        private void refreshScheduleCellsForItem(CheckInOutDAO.CheckInBookingItem item) {
            if (item == null || roomTableModel == null) {
                return;
            }
            int row = bookingItems.indexOf(item);
            if (row < 0 || row >= roomTableModel.getRowCount()) {
                return;
            }
            roomTableModel.setValueAt(formatScheduleDate(item.getExpectedCheckIn()), row, COL_CHECK_IN_DATE);
            roomTableModel.setValueAt(formatScheduleTime(item.getExpectedCheckIn()), row, COL_CHECK_IN_TIME);
            roomTableModel.setValueAt(formatScheduleDate(item.getExpectedCheckOut()), row, COL_CHECK_OUT_DATE);
            roomTableModel.setValueAt(formatScheduleTime(item.getExpectedCheckOut()), row, COL_CHECK_OUT_TIME);
        }

        private void registerCustomerAutoFillListener() {
            roomTableModel.addTableModelListener(e -> {
                if (updatingCustomerCells
                        || e.getType() != javax.swing.event.TableModelEvent.UPDATE
                        || e.getColumn() != COL_CCCD) {
                    return;
                }
                int row = e.getFirstRow();
                if (row < 0 || row >= roomTableModel.getRowCount()) {
                    return;
                }
                handleCccdEdited(row);
            });
        }

        private void handleCccdEdited(int row) {
            String cccdPassport = valueOf(roomTableModel.getValueAt(row, COL_CCCD)).trim();
            if (cccdPassport.isEmpty()) {
                return;
            }

            KhachHang khachHang = checkInOutDAO.findCustomerByCccdPassport(cccdPassport);
            if (khachHang != null) {
                fillCustomerRow(row, khachHang);
                return;
            }

            javax.swing.SwingUtilities.invokeLater(() ->
                    showInfo("Kh\u00e1ch ch\u01b0a c\u00f3 trong h\u1ec7 th\u1ed1ng. Vui l\u00f2ng nh\u1eadp th\u00f4ng tin."));
        }

        private void fillCustomerRow(int row, KhachHang khachHang) {
            updatingCustomerCells = true;
            try {
                roomTableModel.setValueAt(safeValue(khachHang.getCccdPassport(), ""), row, COL_CCCD);
                roomTableModel.setValueAt(safeValue(khachHang.getHoTen(), ""), row, COL_HO_TEN);
                roomTableModel.setValueAt(safeValue(khachHang.getSoDienThoai(), ""), row, COL_SDT);
                roomTableModel.setValueAt(formatCustomerBirthDate(khachHang), row, COL_NGAY_SINH);
                roomTableModel.setValueAt(safeValue(khachHang.getEmail(), ""), row, COL_EMAIL);
                roomTableModel.setValueAt(safeValue(khachHang.getDiaChi(), ""), row, COL_DIA_CHI);
                roomTableModel.setValueAt(safeValue(khachHang.getGhiChu(), ""), row, COL_GHI_CHU);
            } finally {
                updatingCustomerCells = false;
            }
        }

        private String formatCustomerBirthDate(KhachHang khachHang) {
            if (khachHang == null) {
                return "";
            }
            LocalDate ngaySinh = khachHang.getNgaySinhAsLocalDate();
            return ngaySinh == null ? safeValue(khachHang.getNgaySinh(), "") : formatDate(ngaySinh);
        }

        private void selectFirstPendingRoom() {
            for (int i = 0; i < bookingItems.size(); i++) {
                if (bookingItems.get(i).canCheckIn()) {
                    tblRooms.setRowSelectionInterval(i, i);
                    return;
                }
            }
            if (!bookingItems.isEmpty()) {
                tblRooms.setRowSelectionInterval(0, 0);
            }
        }

        private List<CheckInOutDAO.CheckInBookingItem> resolveTargets(boolean checkInAll) {
            List<CheckInOutDAO.CheckInBookingItem> targets = new ArrayList<CheckInOutDAO.CheckInBookingItem>();
            if (checkInAll) {
                for (CheckInOutDAO.CheckInBookingItem item : bookingItems) {
                    if (item.canCheckIn()) {
                        targets.add(item);
                    }
                }
                return targets;
            }

            int row = tblRooms.getSelectedRow();
            if (row < 0 || row >= bookingItems.size()) {
                return targets;
            }
            CheckInOutDAO.CheckInBookingItem selected = bookingItems.get(row);
            if (selected.canCheckIn()) {
                targets.add(selected);
            }
            return targets;
        }

        private Map<Integer, KhachHang> buildCustomerInputs(List<CheckInOutDAO.CheckInBookingItem> targets) {
            Map<Integer, KhachHang> customers = new LinkedHashMap<Integer, KhachHang>();
            for (CheckInOutDAO.CheckInBookingItem item : targets) {
                int row = bookingItems.indexOf(item);
                if (row < 0 || row >= roomTableModel.getRowCount()) {
                    continue;
                }
                customers.put(Integer.valueOf(item.getMaChiTietDatPhong()), buildCustomerFromRow(row));
            }
            return customers;
        }

        private KhachHang buildCustomerFromRow(int row) {
            KhachHang khachHang = new KhachHang();
            khachHang.setCccdPassport(valueOf(roomTableModel.getValueAt(row, COL_CCCD)).trim());
            khachHang.setHoTen(valueOf(roomTableModel.getValueAt(row, COL_HO_TEN)).trim());
            khachHang.setSoDienThoai(valueOf(roomTableModel.getValueAt(row, COL_SDT)).trim());
            khachHang.setNgaySinh(valueOf(roomTableModel.getValueAt(row, COL_NGAY_SINH)).trim());
            khachHang.setEmail(valueOf(roomTableModel.getValueAt(row, COL_EMAIL)).trim());
            khachHang.setDiaChi(valueOf(roomTableModel.getValueAt(row, COL_DIA_CHI)).trim());
            khachHang.setGhiChu(valueOf(roomTableModel.getValueAt(row, COL_GHI_CHU)).trim());
            return khachHang;
        }

        private boolean syncCurrentEditorToSelectedRoom(List<CheckInOutDAO.CheckInBookingItem> targets) {
            CheckInOutDAO.CheckInBookingItem selected = getSelectedBookingItem();
            if (selected == null) {
                return true;
            }
            if (targets == null || !targets.contains(selected)) {
                syncSelectedRoomScheduleFromEditor();
                return true;
            }

            LocalDate ngayVao = txtNgayVao.getDateValue();
            LocalDate ngayRa = txtNgayRa.getDateValue();
            LocalTime gioVao = txtGioVao.getTimeValue();
            LocalTime gioRa = txtGioRa.getTimeValue();
            if (ngayVao == null || ngayRa == null || gioVao == null || gioRa == null) {
                showInfo("Ngày giờ vào/ra của phòng đang chọn không hợp lệ.");
                return false;
            }
            if (ngayVao == null || ngayRa == null || gioVao == null || gioRa == null) {
                showInfo("Ngày giờ vào/ra của phòng đang chọn không hợp lệ.");
                return false;
            }

            LocalDateTime checkIn = LocalDateTime.of(ngayVao, gioVao);
            LocalDateTime checkOut = LocalDateTime.of(ngayRa, gioRa);
            if (!checkOut.isAfter(checkIn)) {
                showInfo("Giờ ra dự kiến phải lớn hơn giờ vào của phòng đang chọn.");
                return false;
            }
            if (!checkOut.isAfter(checkIn)) {
                showInfo("Giờ ra dự kiến phải lớn hơn giờ vào của phòng đang chọn.");
                return false;
            }

            selected.setExpectedCheckIn(checkIn);
            selected.setExpectedCheckOut(checkOut);
            refreshScheduleCellsForItem(selected);
            return true;
        }

        private boolean validateTargetSchedules(List<CheckInOutDAO.CheckInBookingItem> targets) {
            for (CheckInOutDAO.CheckInBookingItem item : targets) {
                LocalDateTime checkIn = item.getExpectedCheckIn();
                LocalDateTime checkOut = item.getExpectedCheckOut();
                if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    continue;
                }
                int row = bookingItems.indexOf(item);
                if (row >= 0) {
                    tblRooms.setRowSelectionInterval(row, row);
                    tblRooms.scrollRectToVisible(tblRooms.getCellRect(row, 0, true));
                }
                if (System.currentTimeMillis() >= 0L) {
                    showInfo("Thời gian check-in / check-out dự kiến của phòng " + safeValue(item.getSoPhong(), "-") + " không hợp lệ.");
                    return false;
                }
                showInfo("Thời gian check-in / check-out dự kiến của phòng " + safeValue(item.getSoPhong(), "-") + " không hợp lệ.");
                return false;
            }
            return true;
        }

        private Map<Integer, CheckInOutDAO.CheckInTiming> buildScheduleInputs(List<CheckInOutDAO.CheckInBookingItem> targets) {
            Map<Integer, CheckInOutDAO.CheckInTiming> schedules = new LinkedHashMap<Integer, CheckInOutDAO.CheckInTiming>();
            for (CheckInOutDAO.CheckInBookingItem item : targets) {
                schedules.put(
                        Integer.valueOf(item.getMaChiTietDatPhong()),
                        new CheckInOutDAO.CheckInTiming(item.getExpectedCheckIn(), item.getExpectedCheckOut())
                );
            }
            return schedules;
        }

        private void submit(boolean checkInAll) {
            if (tblRooms.isEditing() && tblRooms.getCellEditor() != null) {
                tblRooms.getCellEditor().stopCellEditing();
            }

            List<CheckInOutDAO.CheckInBookingItem> targets = resolveTargets(checkInAll);
            if (targets.isEmpty()) {
                showInfo(checkInAll
                        ? "\u0110\u01a1n n\u00e0y kh\u00f4ng c\u00f2n ph\u00f2ng n\u00e0o s\u1eb5n s\u00e0ng check-in."
                        : "Vui l\u00f2ng ch\u1ecdn ph\u00f2ng \u0111\u00e3 \u0111\u01b0\u1ee3c g\u00e1n \u0111\u1ec3 check-in.");
                return;
            }
            if (!syncCurrentEditorToSelectedRoom(targets)) {
                return;
            }
            if (!validateTargetSchedules(targets)) {
                return;
            }
            if (!validateRequiredCustomerInfo(targets)) {
                return;
            }

            List<Integer> detailIds = new ArrayList<Integer>();
            for (CheckInOutDAO.CheckInBookingItem item : targets) {
                detailIds.add(Integer.valueOf(item.getMaChiTietDatPhong()));
            }
            Map<Integer, KhachHang> customerInputs = buildCustomerInputs(targets);
            Map<Integer, CheckInOutDAO.CheckInTiming> scheduleInputs = buildScheduleInputs(targets);

            int affected = checkInOutDAO.checkInBookingDetails(
                    String.valueOf(record.maDatPhong),
                    detailIds,
                    scheduleInputs,
                    customerInputs
            );
            if (affected <= 0) {
                String message = safeValue(checkInOutDAO.getLastErrorMessage(), "");
                showInfo(message.isEmpty() ? "Kh\u00f4ng th\u1ec3 check-in ph\u00f2ng \u0111\u00e3 ch\u1ecdn." : message);
                return;
            }

            PhongGUI.refreshAllOpenInstances();
            DatPhongGUI.refreshAllOpenInstances();
            refreshKhachHangViewsSafely();
            CheckInOutGUI.refreshAllOpenInstances();
            showInfo(checkInAll
                    ? "\u0110\u00e3 check-in c\u00e1c ph\u00f2ng \u0111ang ch\u1edd trong \u0111\u01a1n."
                    : "\u0110\u00e3 check-in ph\u00f2ng \u0111\u00e3 ch\u1ecdn. C\u00e1c ph\u00f2ng c\u00f2n l\u1ea1i gi\u1eef nguy\u00ean tr\u1ea1ng th\u00e1i.");
            dispose();
        }

        private boolean validateRequiredCustomerInfo(List<CheckInOutDAO.CheckInBookingItem> targets) {
            for (CheckInOutDAO.CheckInBookingItem item : targets) {
                int row = bookingItems.indexOf(item);
                if (row < 0 || row >= roomTableModel.getRowCount()) {
                    continue;
                }

                String cccdPassport = valueOf(roomTableModel.getValueAt(row, COL_CCCD)).trim();
                String hoTen = valueOf(roomTableModel.getValueAt(row, COL_HO_TEN)).trim();
                if (cccdPassport.isEmpty()) {
                    continue;
                }
                if (!hoTen.isEmpty()) {
                    continue;
                }

                tblRooms.setRowSelectionInterval(row, row);
                tblRooms.scrollRectToVisible(tblRooms.getCellRect(row, COL_CCCD, true));
                showInfo("Ph\u00f2ng " + safeValue(item.getSoPhong(), "-")
                        + " \u0111\u00e3 nh\u1eadp CCCD/Passport nh\u01b0ng ch\u01b0a c\u00f3 H\u1ecd t\u00ean. "
                        + "Vui l\u00f2ng b\u1ed5 sung H\u1ecd t\u00ean n\u1ebfu mu\u1ed1n l\u01b0u kh\u00e1ch \u0111\u1ea1i di\u1ec7n.");
                return false;
            }
            return true;
        }
    }

    private final class AddServiceDialog extends BaseStayDialog {
        private final StayRecord record;
        private final List<ServiceStayOption> activeOptions;
        private final JComboBox<ServiceStayOption> cboStay;
        private final JLabel lblMaLuuTruValue;
        private final JTextField txtSoLuong;
        private final JTextField txtDonGia;
        private final JComboBox<DichVu> cboDichVu;
        private final DefaultTableModel serviceHistoryModel;
        private final JTable tblServiceHistory;
        private final List<SuDungDichVu> currentHistoryItems = new ArrayList<SuDungDichVu>();
        private final JButton btnSubmit;
        private final JButton btnResetForm;
        private final JButton btnEditHistory;
        private final JButton btnDeleteHistory;
        private SuDungDichVu editingUsage;

        private AddServiceDialog(Frame owner, StayRecord record, List<ServiceStayOption> activeOptions) {
            super(owner, "Th\u00eam d\u1ecbch v\u1ee5 cho kh\u00e1ch \u0111ang \u1edf", 860, 620);
            this.record = record;
            this.activeOptions = new ArrayList<ServiceStayOption>(activeOptions);
            List<DichVu> services = dichVuDAO.getAll();

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "GHI NH\u1eacN D\u1ecaCH V\u1ee4 PH\u00c1T SINH",
                    "Ch\u1ecdn ph\u00f2ng ngay trong popup, theo d\u00f5i l\u1ecbch s\u1eed d\u1ecbch v\u1ee5 theo \u0111\u00fang l\u01b0\u1ee3t l\u01b0u tr\u00fa, r\u1ed3i ghi nh\u1eadn d\u1ecbch v\u1ee5 m\u1edbi."
            ), BorderLayout.NORTH);

            JPanel topPanel = new JPanel(new BorderLayout(0, 12));
            topPanel.setOpaque(false);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboStay = new JComboBox<ServiceStayOption>(this.activeOptions.toArray(new ServiceStayOption[0]));
            cboStay.setFont(BODY_FONT);
            cboDichVu = createServiceComboBox(services);
            txtSoLuong = createInputField("1");
            txtDonGia = createInputField("0");
            txtDonGia.setEditable(true);
            lblMaLuuTruValue = createValueLabel("-");

            updateServiceReferencePrice(cboDichVu, txtDonGia);
            cboDichVu.addActionListener(e -> updateServiceReferencePrice(cboDichVu, txtDonGia));
            cboStay.addActionListener(e -> refreshSelectedStayInfo());

            addFormRow(form, gbc, 0, "M\u00e3 h\u1ed3 s\u01a1", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Kh\u00e1ch h\u00e0ng", createValueLabel(record.khachHang));
            addFormRow(form, gbc, 2, "Ph\u00f2ng \u00e1p d\u1ee5ng", cboStay);
            addFormRow(form, gbc, 3, "M\u00e3 l\u01b0u tr\u00fa", lblMaLuuTruValue);
            addFormRow(form, gbc, 4, "D\u1ecbch v\u1ee5", cboDichVu);
            addFormRow(form, gbc, 5, "\u0110\u01a1n gi\u00e1 tham kh\u1ea3o", txtDonGia);
            addFormRow(form, gbc, 6, "S\u1ed1 l\u01b0\u1ee3ng", txtSoLuong);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            formButtons.setOpaque(false);
            btnResetForm = createOutlineButton("Nh\u1eadp m\u1edbi", new Color(59, 130, 246), e -> resetFormState());
            btnSubmit = createPrimaryButton("L\u01b0u", new Color(37, 99, 235), Color.WHITE, e -> submit());
            formButtons.add(btnResetForm);
            formButtons.add(btnSubmit);
            card.add(formButtons, BorderLayout.SOUTH);
            topPanel.add(card, BorderLayout.CENTER);
            content.add(topPanel, BorderLayout.NORTH);

            serviceHistoryModel = new DefaultTableModel(
                    new Object[]{"STT", "D\u1ecbch v\u1ee5", "S\u1ed1 l\u01b0\u1ee3ng", "\u0110\u01a1n gi\u00e1", "Th\u00e0nh ti\u1ec1n"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblServiceHistory = new JTable(serviceHistoryModel);
            tblServiceHistory.setRowHeight(28);
            tblServiceHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblServiceHistory.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    updateHistoryActionState();
                }
            });

            JPanel historyPanel = createDialogCardPanel();
            JPanel historyContent = new JPanel(new BorderLayout(0, 8));
            historyContent.setOpaque(false);
            JLabel lblHistory = new JLabel("L\u1ecbch s\u1eed d\u1ecbch v\u1ee5 c\u1ee7a ph\u00f2ng \u0111ang ch\u1ecdn");
            lblHistory.setFont(AppFonts.section(14));
            lblHistory.setForeground(TEXT_PRIMARY);
            historyContent.add(lblHistory, BorderLayout.NORTH);
            JScrollPane historyScroll = new JScrollPane(tblServiceHistory);
            historyScroll.setPreferredSize(new Dimension(720, 220));
            historyContent.add(historyScroll, BorderLayout.CENTER);
            JPanel historyActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            historyActions.setOpaque(false);
            btnEditHistory = createOutlineButton("S\u1eeda d\u1ecbch v\u1ee5", new Color(245, 158, 11), e -> startEditSelectedHistory());
            btnDeleteHistory = createOutlineButton("X\u00f3a d\u1ecbch v\u1ee5", new Color(220, 38, 38), e -> deleteSelectedHistory());
            historyActions.add(btnEditHistory);
            historyActions.add(btnDeleteHistory);
            historyContent.add(historyActions, BorderLayout.SOUTH);
            historyPanel.add(historyContent, BorderLayout.CENTER);
            content.add(historyPanel, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("H\u1ee7y", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);

            refreshSelectedStayInfo();
        }

        private void submit() {
            ServiceStayOption serviceTarget = getSelectedServiceTarget();
            if (serviceTarget == null || serviceTarget.maLuuTru <= 0) {
                showInfo("M\u00e3 l\u01b0u tr\u00fa hi\u1ec7n t\u1ea1i kh\u00f4ng h\u1ee3p l\u1ec7.");
                return;
            }
            if (!isStayCurrentlyActive(serviceTarget.maLuuTru)) {
                showInfo("Ch\u1ec9 ph\u00f2ng \u0111ang \u1edf m\u1edbi \u0111\u01b0\u1ee3c ghi nh\u1eadn d\u1ecbch v\u1ee5.");
                return;
            }

            DichVu dichVu = getSelectedDichVu(cboDichVu);
            if (dichVu == null || dichVu.getMaDichVu() <= 0) {
                showInfo("Vui l\u00f2ng ch\u1ecdn d\u1ecbch v\u1ee5 h\u1ee3p l\u1ec7.");
                return;
            }

            int soLuong;
            try {
                soLuong = Integer.parseInt(txtSoLuong.getText().trim());
            } catch (NumberFormatException ex) {
                showInfo("S\u1ed1 l\u01b0\u1ee3ng kh\u00f4ng h\u1ee3p l\u1ec7.");
                return;
            }
            if (soLuong <= 0) {
                showInfo("S\u1ed1 l\u01b0\u1ee3ng ph\u1ea3i l\u1edbn h\u01a1n 0.");
                return;
            }

            double donGia = dichVu.getDonGia();
            try {
                String normalizedPrice = txtDonGia.getText().trim().replace(".", "");
                if (!normalizedPrice.isEmpty()) {
                    donGia = Double.parseDouble(normalizedPrice);
                }
            } catch (NumberFormatException ignored) {
                donGia = dichVu.getDonGia();
            }

            try {
                SuDungDichVu usage = new SuDungDichVu(serviceTarget.maLuuTru, dichVu.getMaDichVu(), soLuong, donGia);
                boolean success;
                boolean editing = editingUsage != null;
                if (editing) {
                    usage.setMaSuDung(editingUsage.getMaSuDung());
                    success = suDungDichVuDAO.updateSuDungDichVu(usage);
                } else {
                    success = suDungDichVuDAO.insertSuDungDichVu(usage);
                }
                if (!success) {
                    showInfo(editing ? "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt d\u1ecbch v\u1ee5." : "Kh\u00f4ng th\u1ec3 ghi nh\u1eadn s\u1eed d\u1ee5ng d\u1ecbch v\u1ee5.");
                    return;
                }
                CheckInOutGUI.refreshAllOpenInstances();
                refreshSelectedStayInfo();
                resetFormState();
                showInfo(editing ? "\u0110\u00e3 c\u1eadp nh\u1eadt d\u1ecbch v\u1ee5." : "\u0110\u00e3 ghi nh\u1eadn s\u1eed d\u1ee5ng d\u1ecbch v\u1ee5 th\u00e0nh c\u00f4ng.");
            } catch (Exception e) {
                e.printStackTrace();
                showInfo(editingUsage == null ? "Kh\u00f4ng th\u1ec3 ghi nh\u1eadn s\u1eed d\u1ee5ng d\u1ecbch v\u1ee5." : "Kh\u00f4ng th\u1ec3 c\u1eadp nh\u1eadt d\u1ecbch v\u1ee5.");
            }
        }

        private ServiceStayOption getSelectedServiceTarget() {
            return (ServiceStayOption) cboStay.getSelectedItem();
        }

        private void refreshSelectedStayInfo() {
            ServiceStayOption serviceTarget = getSelectedServiceTarget();
            lblMaLuuTruValue.setText(serviceTarget == null ? "-" : String.valueOf(serviceTarget.maLuuTru));
            reloadServiceHistory(serviceTarget);
            resetFormState();
        }

        private void reloadServiceHistory(ServiceStayOption serviceTarget) {
            currentHistoryItems.clear();
            serviceHistoryModel.setRowCount(0);
            if (serviceTarget == null || serviceTarget.maLuuTru <= 0) {
                updateHistoryActionState();
                return;
            }
            currentHistoryItems.addAll(suDungDichVuDAO.getByMaLuuTru(serviceTarget.maLuuTru));
            for (int i = 0; i < currentHistoryItems.size(); i++) {
                SuDungDichVu item = currentHistoryItems.get(i);
                serviceHistoryModel.addRow(new Object[]{
                        i + 1,
                        safeValue(item.getTenDichVu(), "-"),
                        item.getSoLuong(),
                        formatMoney(item.getDonGia()),
                        formatMoney(item.getThanhTien())
                });
            }
            updateHistoryActionState();
        }

        private void startEditSelectedHistory() {
            SuDungDichVu selected = getSelectedHistoryItem();
            if (selected == null) {
                showInfo("Vui l\u00f2ng ch\u1ecdn d\u1ecbch v\u1ee5 c\u1ea7n s\u1eeda trong l\u1ecbch s\u1eed.");
                return;
            }
            editingUsage = selected;
            selectServiceById(selected.getMaDichVu());
            txtSoLuong.setText(String.valueOf(selected.getSoLuong()));
            txtDonGia.setText(formatMoney(selected.getDonGia()));
            btnSubmit.setText("C\u1eadp nh\u1eadt");
        }

        private void deleteSelectedHistory() {
            SuDungDichVu selected = getSelectedHistoryItem();
            if (selected == null) {
                showInfo("Vui l\u00f2ng ch\u1ecdn d\u1ecbch v\u1ee5 c\u1ea7n x\u00f3a.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "X\u00f3a d\u1ecbch v\u1ee5 '" + safeValue(selected.getTenDichVu(), "-") + "' kh\u1ecfi ph\u00f2ng \u0111ang ch\u1ecdn?",
                    "X\u00e1c nh\u1eadn x\u00f3a",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            if (!suDungDichVuDAO.deleteSuDungDichVu(selected.getMaSuDung())) {
                showInfo("Kh\u00f4ng th\u1ec3 x\u00f3a d\u1ecbch v\u1ee5 \u0111\u00e3 ch\u1ecdn.");
                return;
            }
            CheckInOutGUI.refreshAllOpenInstances();
            refreshSelectedStayInfo();
            if (editingUsage != null && editingUsage.getMaSuDung() == selected.getMaSuDung()) {
                resetFormState();
            }
            showInfo("\u0110\u00e3 x\u00f3a d\u1ecbch v\u1ee5 \u0111\u00e3 ch\u1ecdn.");
        }

        private SuDungDichVu getSelectedHistoryItem() {
            int row = tblServiceHistory.getSelectedRow();
            if (row < 0 || row >= currentHistoryItems.size()) {
                return null;
            }
            return currentHistoryItems.get(row);
        }

        private void updateHistoryActionState() {
            boolean hasSelection = getSelectedHistoryItem() != null;
            btnEditHistory.setEnabled(hasSelection);
            btnDeleteHistory.setEnabled(hasSelection);
        }

        private void resetFormState() {
            editingUsage = null;
            btnSubmit.setText("L\u01b0u");
            txtSoLuong.setText("1");
            updateServiceReferencePrice(cboDichVu, txtDonGia);
            tblServiceHistory.clearSelection();
            updateHistoryActionState();
        }

        private void selectServiceById(int maDichVu) {
            for (int i = 0; i < cboDichVu.getItemCount(); i++) {
                DichVu item = cboDichVu.getItemAt(i);
                if (item != null && item.getMaDichVu() == maDichVu) {
                    cboDichVu.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private final class ChangeRoomDialog extends BaseStayDialog {
        private final StayRecord record;
        private final List<ServiceStayOption> activeOptions;
        private final JComboBox<ServiceStayOption> cboCurrentStay;
        private final JComboBox<RoomOption> cboNewRoom;
        private final AppDatePickerField txtNgayDoi;
        private final AppTimePickerField txtGioDoi;
        private final JTextArea txtLyDo;
        private final JLabel lblCurrentStayValue;
        private final JLabel lblCurrentRoomValue;
        private final JLabel lblCurrentTypeValue;
        private final JLabel lblCurrentPositionValue;
        private final JLabel lblNewRoomValue;
        private final JLabel lblNewTypeValue;
        private final JLabel lblNewPositionValue;
        private final JLabel lblNewRateValue;
        private final JLabel lblAvailabilityHint;
        private final JButton btnConfirm;
        private ActiveStaySnapshot currentSnapshot;

        private ChangeRoomDialog(Frame owner, StayRecord record) {
            super(owner, "\u0110\u1ed5i ph\u00f2ng", 820, 640);
            this.record = record;
            this.activeOptions = resolveActiveServiceOptions(record);

            cboCurrentStay = new JComboBox<ServiceStayOption>(this.activeOptions.toArray(new ServiceStayOption[0]));
            cboCurrentStay.setFont(BODY_FONT);
            cboCurrentStay.setEnabled(this.activeOptions.size() > 1);

            cboNewRoom = new JComboBox<RoomOption>();
            cboNewRoom.setFont(BODY_FONT);

            txtNgayDoi = new AppDatePickerField(LocalDate.now().format(DATE_FORMAT), true);
            txtGioDoi = new AppTimePickerField(LocalTime.now().format(TIME_FORMAT), true);

            txtLyDo = new JTextArea(4, 20);
            txtLyDo.setLineWrap(true);
            txtLyDo.setWrapStyleWord(true);
            txtLyDo.setFont(BODY_FONT);
            txtLyDo.setForeground(TEXT_PRIMARY);
            txtLyDo.setBackground(PANEL_SOFT);
            txtLyDo.setBorder(new EmptyBorder(8, 10, 8, 10));

            lblCurrentStayValue = createValueLabel("-");
            lblCurrentRoomValue = createValueLabel("-");
            lblCurrentTypeValue = createValueLabel("-");
            lblCurrentPositionValue = createValueLabel("-");
            lblNewRoomValue = createValueLabel("-");
            lblNewTypeValue = createValueLabel("-");
            lblNewPositionValue = createValueLabel("-");
            lblNewRateValue = createValueLabel("-");
            lblAvailabilityHint = new JLabel(" ");
            lblAvailabilityHint.setFont(BODY_FONT);
            lblAvailabilityHint.setForeground(TEXT_MUTED);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "ĐỔI PHÒNG ĐANG Ở",
                    "Đóng lượt lưu trú hiện tại ở phòng cũ, mở lượt lưu trú mới cho phòng thay thế và giữ nguyên lịch sử dịch vụ theo từng lần ở."
            ), BorderLayout.NORTH);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(form, gbc, 0, "Mã hồ sơ", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Mã đặt phòng", createValueLabel("DP" + record.maDatPhong));
            addFormRow(form, gbc, 2, "Khách hàng", createValueLabel(record.khachHang));
            addFormRow(form, gbc, 3, "Phòng đang đổi", cboCurrentStay);
            addFormRow(form, gbc, 4, "Ngày giờ đổi", buildChangeTimeRow());
            addFormRow(form, gbc, 5, "Phòng mới", cboNewRoom);

            JPanel card = createDialogCardPanel();
            JPanel overview = new JPanel(new java.awt.GridLayout(1, 2, 12, 0));
            overview.setOpaque(false);
            overview.add(buildCurrentRoomCard());
            overview.add(buildTargetRoomCard());
            card.add(overview, BorderLayout.NORTH);
            card.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

            JPanel formWrapper = new JPanel(new BorderLayout(0, 8));
            formWrapper.setOpaque(false);
            formWrapper.add(form, BorderLayout.CENTER);
            formWrapper.add(lblAvailabilityHint, BorderLayout.SOUTH);
            card.add(formWrapper, BorderLayout.SOUTH);
            center.add(card);
            center.add(Box.createVerticalStrut(12));

            JPanel noteCard = createDialogCardPanel();
            JPanel notePanel = new JPanel(new BorderLayout(0, 8));
            notePanel.setOpaque(false);
            JLabel lblNote = new JLabel("Lý do / ghi chú đổi phòng");
            lblNote.setFont(AppFonts.section(14));
            lblNote.setForeground(TEXT_PRIMARY);
            notePanel.add(lblNote, BorderLayout.NORTH);
            notePanel.add(new JScrollPane(txtLyDo), BorderLayout.CENTER);
            noteCard.add(notePanel, BorderLayout.CENTER);
            center.add(noteCard);

            content.add(center, BorderLayout.CENTER);

            btnConfirm = createPrimaryButton("Xác nhận đổi phòng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);

            cboCurrentStay.addActionListener(e -> refreshDialogState());
            cboNewRoom.addActionListener(e -> refreshTargetRoomInfo());
            txtNgayDoi.addTextChangeListener(this::refreshDialogState);
            txtGioDoi.addTextChangeListener(this::refreshDialogState);

            refreshDialogState();
        }

        private JPanel buildChangeTimeRow() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            panel.setOpaque(false);
            panel.add(txtNgayDoi);
            panel.add(txtGioDoi);
            return panel;
        }

        private JPanel buildCurrentRoomCard() {
            JPanel card = createDialogCardPanel();
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

            JLabel lblTitle = new JLabel("Phòng cũ đang ở");
            lblTitle.setFont(AppFonts.section(14));
            lblTitle.setForeground(new Color(180, 83, 9));
            body.add(lblTitle);
            body.add(Box.createVerticalStrut(8));
            addInfoRow(body, "Lưu trú", lblCurrentStayValue);
            addInfoRow(body, "Số phòng", lblCurrentRoomValue);
            addInfoRow(body, "Loại phòng", lblCurrentTypeValue);
            addInfoRow(body, "Tầng / khu vực", lblCurrentPositionValue);
            card.add(body, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildTargetRoomCard() {
            JPanel card = createDialogCardPanel();
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

            JLabel lblTitle = new JLabel("Phòng mới thay thế");
            lblTitle.setFont(AppFonts.section(14));
            lblTitle.setForeground(new Color(37, 99, 235));
            body.add(lblTitle);
            body.add(Box.createVerticalStrut(8));
            addInfoRow(body, "Số phòng", lblNewRoomValue);
            addInfoRow(body, "Loại phòng", lblNewTypeValue);
            addInfoRow(body, "Tầng / khu vực", lblNewPositionValue);
            addInfoRow(body, "Giá tham chiếu", lblNewRateValue);
            card.add(body, BorderLayout.CENTER);
            return card;
        }

        private void addInfoRow(JPanel panel, String label, JLabel value) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);

            JLabel lbl = new JLabel(label);
            lbl.setFont(LABEL_FONT);
            lbl.setForeground(TEXT_MUTED);
            row.add(lbl, BorderLayout.WEST);
            row.add(value, BorderLayout.CENTER);

            panel.add(row);
            panel.add(Box.createVerticalStrut(6));
        }

        private void refreshDialogState() {
            currentSnapshot = resolveSelectedSnapshot();
            refreshCurrentRoomInfo();
            reloadAvailableRooms();
            refreshTargetRoomInfo();
        }

        private ActiveStaySnapshot resolveSelectedSnapshot() {
            ServiceStayOption option = getSelectedStayTarget();
            return option == null ? null : loadActiveStaySnapshot(option.maLuuTru);
        }

        private ServiceStayOption getSelectedStayTarget() {
            return (ServiceStayOption) cboCurrentStay.getSelectedItem();
        }

        private RoomOption getSelectedTargetRoom() {
            return (RoomOption) cboNewRoom.getSelectedItem();
        }

        private void refreshCurrentRoomInfo() {
            if (currentSnapshot == null) {
                lblCurrentStayValue.setText("-");
                lblCurrentRoomValue.setText("-");
                lblCurrentTypeValue.setText("-");
                lblCurrentPositionValue.setText("-");
                return;
            }
            lblCurrentStayValue.setText("LT" + currentSnapshot.maLuuTru + " / CTDP" + currentSnapshot.maChiTietDatPhong);
            lblCurrentRoomValue.setText(currentSnapshot.soPhong);
            lblCurrentTypeValue.setText(currentSnapshot.tenLoaiPhong);
            lblCurrentPositionValue.setText(buildPositionText(currentSnapshot.tang, currentSnapshot.khuVuc));
        }

        private void reloadAvailableRooms() {
            RoomOption previousRoom = getSelectedTargetRoom();
            int previousRoomId = previousRoom == null ? 0 : previousRoom.maPhong;

            cboNewRoom.removeAllItems();
            String validationMessage = validateChangeTime(currentSnapshot);
            if (!validationMessage.isEmpty()) {
                lblAvailabilityHint.setText(validationMessage);
                btnConfirm.setEnabled(false);
                return;
            }

            List<RoomOption> roomOptions = loadAvailableRooms(currentSnapshot, getSelectedChangeTime());
            for (RoomOption roomOption : roomOptions) {
                cboNewRoom.addItem(roomOption);
            }

            RoomOption matched = findRoomOption(roomOptions, previousRoomId);
            if (matched != null) {
                cboNewRoom.setSelectedItem(matched);
            } else if (cboNewRoom.getItemCount() > 0) {
                cboNewRoom.setSelectedIndex(0);
            }

            if (roomOptions.isEmpty()) {
                lblAvailabilityHint.setText("Không có phòng khả dụng trong khoảng lưu trú còn lại.");
            } else {
                lblAvailabilityHint.setText("Chỉ hiển thị phòng đang trống, không bảo trì và không bị booking/lưu trú khác chồng lấn.");
            }
            if (roomOptions.isEmpty()) {
                lblAvailabilityHint.setText("Kh\u00f4ng c\u00f3 ph\u00f2ng tr\u1ed1ng ph\u00f9 h\u1ee3p \u0111\u1ec3 \u0111\u1ed5i.");
            } else {
                lblAvailabilityHint.setText("Ch\u1ec9 hi\u1ec3n th\u1ecb ph\u00f2ng tr\u1ed1ng h\u1ee3p l\u1ec7, kh\u00f4ng tr\u00f9ng booking hi\u1ec7n t\u1ea1i v\u00e0 kh\u00f4ng b\u1ecb gi\u1eef b\u1edfi kh\u00e1ch kh\u00e1c.");
            }
            btnConfirm.setEnabled(!roomOptions.isEmpty());
        }

        private void refreshTargetRoomInfo() {
            RoomOption room = getSelectedTargetRoom();
            if (room == null) {
                lblNewRoomValue.setText("-");
                lblNewTypeValue.setText("-");
                lblNewPositionValue.setText("-");
                lblNewRateValue.setText("-");
                return;
            }
            lblNewRoomValue.setText(room.soPhong);
            lblNewTypeValue.setText(room.loaiPhong);
            lblNewPositionValue.setText(buildPositionText(room.tang, room.khuVuc));
            lblNewRateValue.setText(room.giaThamChieu > 0d ? formatMoney(room.giaThamChieu) : "Theo bảng giá hiện hành");
        }

        private String validateChangeTime(ActiveStaySnapshot staySnapshot) {
            if (staySnapshot == null) {
                return "Không còn tìm thấy lượt lưu trú đang ở để đổi phòng.";
            }
            LocalDateTime changeTime = getSelectedChangeTime();
            if (changeTime == null) {
                return "Thời điểm đổi phòng không hợp lệ.";
            }
            if (staySnapshot.checkIn != null && !changeTime.isAfter(staySnapshot.checkIn.toLocalDateTime())) {
                return "Thời điểm đổi phải sau giờ check-in của phòng hiện tại.";
            }
            Timestamp expectedCheckOut = normalizeExpectedCheckOut(staySnapshot.expectedCheckOut);
            if (expectedCheckOut == null) {
                return "Không xác định được giờ trả phòng dự kiến của booking.";
            }
            if (!expectedCheckOut.toLocalDateTime().isAfter(changeTime)) {
                return "Thời điểm đổi phải trước giờ trả phòng dự kiến.";
            }
            if (changeTime.isAfter(LocalDateTime.now().plusMinutes(1L))) {
                return "Thời điểm đổi phòng không được lớn hơn thời gian hiện tại.";
            }
            return "";
        }

        private LocalDateTime getSelectedChangeTime() {
            LocalDate ngayDoi = txtNgayDoi.getDateValue();
            LocalTime gioDoi = txtGioDoi.getTimeValue();
            if (ngayDoi == null || gioDoi == null) {
                return null;
            }
            return LocalDateTime.of(ngayDoi, gioDoi);
        }

        private RoomOption findRoomOption(List<RoomOption> roomOptions, int maPhong) {
            if (roomOptions == null || maPhong <= 0) {
                return null;
            }
            for (RoomOption roomOption : roomOptions) {
                if (roomOption != null && roomOption.maPhong == maPhong) {
                    return roomOption;
                }
            }
            return null;
        }

        private String buildPositionText(String tang, String khuVuc) {
            String floor = safeValue(tang, "-");
            String area = safeValue(khuVuc, "-");
            if ("-".equals(area)) {
                return floor;
            }
            return floor + " / " + area;
        }

        private void submit() {
            ServiceStayOption stayTarget = getSelectedStayTarget();
            if (stayTarget == null || stayTarget.maLuuTru <= 0) {
                showInfo("Vui lòng chọn đúng phòng đang ở cần đổi.");
                return;
            }

            LocalDateTime changeTime = getSelectedChangeTime();
            if (changeTime == null) {
                showInfo("Thời điểm đổi phòng không hợp lệ.");
                return;
            }

            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try {
                con.setAutoCommit(false);

                ActiveStaySnapshot staySnapshot = loadActiveStaySnapshot(con, stayTarget.maLuuTru);
                String validationMessage = validateChangeTime(staySnapshot);
                if (!validationMessage.isEmpty()) {
                    con.rollback();
                    showInfo(validationMessage);
                    return;
                }

                RoomOption selectedRoom = getSelectedTargetRoom();
                if (selectedRoom == null) {
                    con.rollback();
                    showInfo("Vui lòng chọn phòng mới khả dụng.");
                    return;
                }

                List<RoomOption> latestOptions = loadAvailableRoomsUsingDao(con, staySnapshot, changeTime);
                RoomOption targetRoom = findRoomOption(latestOptions, selectedRoom.maPhong);
                if (targetRoom == null) {
                    con.rollback();
                    showInfo("Phòng mới vừa bị chiếm hoặc đang bảo trì. Vui lòng chọn lại.");
                    return;
                }
                if (targetRoom.maPhong == staySnapshot.maPhong) {
                    con.rollback();
                    showInfo("Phòng mới không được trùng với phòng hiện tại.");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ? AND checkOut IS NULL")) {
                    ps.setTimestamp(1, Timestamp.valueOf(changeTime));
                    ps.setInt(2, staySnapshot.maLuuTru);
                    if (ps.executeUpdate() <= 0) {
                        con.rollback();
                        showInfo("Phòng hiện tại không còn ở trạng thái đang ở để đổi.");
                        return;
                    }
                }

                double giaPhongMoi = targetRoom.giaThamChieu > 0d ? targetRoom.giaThamChieu : staySnapshot.giaPhong;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setInt(1, staySnapshot.maChiTietDatPhong);
                    ps.setInt(2, staySnapshot.maDatPhong);
                    ps.setInt(3, targetRoom.maPhong);
                    ps.setTimestamp(4, Timestamp.valueOf(changeTime));
                    ps.setTimestamp(5, null);
                    ps.setInt(6, staySnapshot.soNguoi);
                    ps.setDouble(7, giaPhongMoi);
                    ps.setDouble(8, staySnapshot.tienCoc);
                    if (ps.executeUpdate() <= 0) {
                        con.rollback();
                        showInfo("Không thể tạo lượt lưu trú mới cho phòng thay thế.");
                        return;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE ChiTietDatPhong SET maPhong = ? WHERE maChiTietDatPhong = ?")) {
                    ps.setInt(1, targetRoom.maPhong);
                    ps.setInt(2, staySnapshot.maChiTietDatPhong);
                    if (ps.executeUpdate() <= 0) {
                        con.rollback();
                        showInfo("Không thể cập nhật phòng hiện hành của booking.");
                        return;
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Phong SET trangThai = N'Dọn dẹp' WHERE maPhong = ? AND ISNULL(trangThai, N'') <> N'Bảo trì'")) {
                    ps.setInt(1, staySnapshot.maPhong);
                    ps.executeUpdate();
                }

                List<Integer> roomIds = new ArrayList<Integer>();
                roomIds.add(Integer.valueOf(staySnapshot.maPhong));
                roomIds.add(Integer.valueOf(targetRoom.maPhong));
                datPhongDAO.refreshRoomStatuses(con, roomIds);
                checkInOutDAO.refreshBookingStatus(con, record.maDatPhong);
                con.commit();

                CheckInOutGUI.prepareFocusOnBooking(record.maDatPhong);
                DatPhongGUI.prepareFocusOnBooking(record.maDatPhong);
                PhongGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
                CheckInOutGUI.refreshAllOpenInstances();
                showInfo("Đã đổi phòng " + staySnapshot.soPhong + " sang " + targetRoom.soPhong + " thành công.");
                dispose();
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (Exception ignore) {
                }
                e.printStackTrace();
                showInfo("Không thể đổi phòng.");
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (Exception ignore) {
                }
            }
        }
    }

    private final class ExtendStayDialog extends BaseStayDialog {
        private final StayRecord record;

        private ExtendStayDialog(Frame owner, StayRecord record) {
            super(owner, "Gia h\u1ea1n", 560, 360);
            this.record = record;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("GIA H\u1ea0N L\u01afU TR\u00da", "C\u1eadp nh\u1eadt ng\u00e0y gi\u1edd tr\u1ea3 m\u1edbi cho h\u1ed3 s\u01a1 \u0111ang \u1edf."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            AppDatePickerField txtNgayRa = new AppDatePickerField(record.expectedCheckOutDate.format(DATE_FORMAT), true);
            AppTimePickerField txtGioRa = new AppTimePickerField("12:00", true);

            addFormRow(form, gbc, 0, "M\u00e3 h\u1ed3 s\u01a1", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Ng\u00e0y tr\u1ea3 m\u1edbi", txtNgayRa);
            addFormRow(form, gbc, 2, "Gi\u1edd tr\u1ea3 m\u1edbi", txtGioRa);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("X\u00e1c nh\u1eadn", new Color(59, 130, 246), Color.WHITE, e -> submit(txtNgayRa, txtGioRa));
            JButton btnCancel = createOutlineButton("H\u1ee7y", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa) {
            if (txtNgayRa.getDateValue() == null || txtGioRa.getTimeValue() == null) {
                showInfo("Ng\u00e0y gi\u1edd tr\u1ea3 m\u1edbi kh\u00f4ng h\u1ee3p l\u1ec7.");
                return;
            }
            if (record.expectedCheckOutDate != null && !txtNgayRa.getDateValue().isAfter(record.expectedCheckOutDate)) {
                showInfo("Ng\u00e0y tr\u1ea3 m\u1edbi ph\u1ea3i l\u1edbn h\u01a1n ng\u00e0y tr\u1ea3 hi\u1ec7n t\u1ea1i.");
                return;
            }
            String conflictMessage = findExtendConflictMessage(record, txtNgayRa.getDateValue());
            if (!conflictMessage.isEmpty()) {
                showInfo(conflictMessage);
                return;
            }
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Kh\u00f4ng th\u1ec3 k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u.");
                return;
            }
            try {
                try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET ngayTraPhong = ? WHERE maDatPhong = ?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.of(txtNgayRa.getDateValue(), txtGioRa.getTimeValue())));
                    ps.setInt(2, record.maDatPhong);
                    ps.executeUpdate();
                }
                CheckInOutGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
                showInfo("Gia h\u1ea1n th\u00e0nh c\u00f4ng.");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Kh\u00f4ng th\u1ec3 gia h\u1ea1n.");
            }
        }
    }

    private String findExtendConflictMessage(StayRecord record, LocalDate newCheckOutDate) {
        if (record == null || newCheckOutDate == null) {
            return "";
        }
        LocalDate extensionStart = record.expectedCheckOutDate == null ? LocalDate.now() : record.expectedCheckOutDate;
        if (!newCheckOutDate.isAfter(extensionStart)) {
            return "";
        }
        for (Integer roomId : record.maPhongIds) {
            if (roomId == null || roomId.intValue() <= 0) {
                continue;
            }
            DatPhongConflictInfo conflict = datPhongDAO.findRoomConflict(
                    roomId.intValue(),
                    extensionStart,
                    newCheckOutDate,
                    Integer.valueOf(record.maDatPhong)
            );
            if (conflict != null) {
                return "Kh\u00f4ng th\u1ec3 gia h\u1ea1n. Ph\u00f2ng "
                        + safeValue(conflict.getSoPhong(), String.valueOf(roomId.intValue()))
                        + " \u0111ang tr\u00f9ng v\u1edbi booking DP"
                        + conflict.getMaDatPhong()
                        + " t\u1eeb "
                        + formatDate(conflict.getNgayNhanPhong())
                        + " \u0111\u1ebfn "
                        + formatDate(conflict.getNgayTraPhong())
                        + ".";
            }
        }
        return "";
    }

    private final class CheckOutDialog extends BaseStayDialog {
        private static final LocalTime LEGACY_EXPECTED_CHECKOUT_TIME = LocalTime.of(12, 0);
        private final StayRecord record;
        private final List<CheckoutStayItem> stayItems = new ArrayList<CheckoutStayItem>();
        private final JTable tblRooms;
        private final DefaultTableModel roomTableModel;
        private final JLabel lblPhongDangChonValue;
        private final JLabel lblCheckInValue;
        private final JLabel lblGioRaDuKienValue;
        private final JLabel lblGioRaThucTeValue;
        private final JLabel lblTraMuonValue;
        private final JLabel lblSoGioTreValue;
        private final JLabel lblPhuThuTreValue;
        private boolean updatingCheckoutEditor;

        private CheckOutDialog(Frame owner, StayRecord record) {
            super(owner, "Check-out", 820, 620);
            this.record = record;
            try {
                stayItems.addAll(loadCheckoutStayItems(record.maDatPhong));
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch ph\u00f2ng c\u1ea7n check-out.");
            }
            initializeCheckoutEditorState();

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHECK-OUT", "Ch\u1ecdn t\u1eebng ph\u00f2ng \u0111\u1ec3 check-out ri\u00eang ho\u1eb7c d\u00f9ng thao t\u00e1c check-out to\u00e0n b\u1ed9 \u0111\u01a1n."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            LocalDateTime initialActualCheckOut = resolveDialogInitialActualCheckOut();
            AppDatePickerField txtNgayRa = new AppDatePickerField(initialActualCheckOut.toLocalDate().format(DATE_FORMAT), true);
            AppTimePickerField txtGioRa = new AppTimePickerField(initialActualCheckOut.toLocalTime().format(TIME_FORMAT), true);
            lblPhongDangChonValue = createValueLabel("-");
            lblCheckInValue = createValueLabel("-");
            lblGioRaDuKienValue = createValueLabel("-");
            lblGioRaThucTeValue = createValueLabel("-");
            lblTraMuonValue = createValueLabel("Kh\u00f4ng");
            lblSoGioTreValue = createValueLabel("0 gi\u1edd");
            lblPhuThuTreValue = createValueLabel("0");

            addFormRow(form, gbc, 0, "M\u00e3 h\u1ed3 s\u01a1", createValueLabel(record.maHoSo));
            addFormRow(form, gbc, 1, "Kh\u00e1ch h\u00e0ng", createValueLabel(record.khachHang));
            addFormRow(form, gbc, 2, "Ph\u00f2ng \u0111ang ch\u1ecdn", lblPhongDangChonValue);
            addFormRow(form, gbc, 3, "Gi\u1edd v\u00e0o", lblCheckInValue);
            addFormRow(form, gbc, 4, "Ng\u00e0y ra", txtNgayRa);
            addFormRow(form, gbc, 5, "Gi\u1edd ra", txtGioRa);
            addFormRow(form, gbc, 6, "Gi\u1edd ra d\u1ef1 ki\u1ebfn", lblGioRaDuKienValue);
            addFormRow(form, gbc, 7, "Gi\u1edd ra th\u1ef1c t\u1ebf", lblGioRaThucTeValue);
            addFormRow(form, gbc, 8, "Tr\u1ea3 ph\u00f2ng tr\u1ec5", lblTraMuonValue);
            addFormRow(form, gbc, 9, "S\u1ed1 gi\u1edd tr\u1ec5", lblSoGioTreValue);
            addFormRow(form, gbc, 10, "Ph\u1ee5 thu tr\u1ea3 mu\u1ed9n", lblPhuThuTreValue);

            roomTableModel = new DefaultTableModel(
                    new Object[]{"Ph\u00f2ng", "Lo\u1ea1i ph\u00f2ng", "Tr\u1ea1ng th\u00e1i", "Check-in", "Check-out", "DV ph\u00e1t sinh"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblRooms = new JTable(roomTableModel);
            tblRooms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblRooms.setRowHeight(28);
            tblRooms.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedCheckoutIntoEditor(txtNgayRa, txtGioRa);
                    refreshLateCheckoutPreview(txtNgayRa, txtGioRa);
                }
            });
            txtNgayRa.addTextChangeListener(() -> {
                syncSelectedCheckoutFromEditor(txtNgayRa, txtGioRa);
                refreshLateCheckoutPreview(txtNgayRa, txtGioRa);
            });
            txtGioRa.addTextChangeListener(() -> {
                syncSelectedCheckoutFromEditor(txtNgayRa, txtGioRa);
                refreshLateCheckoutPreview(txtNgayRa, txtGioRa);
            });
            refillRoomTable();

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.NORTH);

            JPanel roomPanel = new JPanel(new BorderLayout(0, 8));
            roomPanel.setOpaque(false);
            JLabel lblRooms = new JLabel("Danh s\u00e1ch ph\u00f2ng trong \u0111\u01a1n");
            lblRooms.setFont(AppFonts.section(14));
            lblRooms.setForeground(TEXT_PRIMARY);
            roomPanel.add(lblRooms, BorderLayout.NORTH);
            roomPanel.add(new JScrollPane(tblRooms), BorderLayout.CENTER);
            card.add(roomPanel, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnCheckOutSelected = createPrimaryButton(
                    "Check-out ph\u00f2ng \u0111\u00e3 ch\u1ecdn",
                    new Color(220, 38, 38),
                    Color.WHITE,
                    e -> submit(txtNgayRa, txtGioRa, false)
            );
            JButton btnCheckOutAll = createPrimaryButton(
                    "Check-out to\u00e0n b\u1ed9 \u0111\u01a1n",
                    new Color(185, 28, 28),
                    Color.WHITE,
                    e -> submit(txtNgayRa, txtGioRa, true)
            );
            JButton btnCancel = createOutlineButton("H\u1ee7y", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnCheckOutSelected, btnCheckOutAll), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
            loadSelectedCheckoutIntoEditor(txtNgayRa, txtGioRa);
            refreshLateCheckoutPreview(txtNgayRa, txtGioRa);
        }

        private void refillRoomTable() {
            roomTableModel.setRowCount(0);
            for (CheckoutStayItem item : stayItems) {
                roomTableModel.addRow(new Object[]{
                        item.soPhong,
                        item.loaiPhong,
                        item.trangThai,
                        formatDateTime(item.checkIn),
                        formatDateTime(item.checkOut),
                        formatMoney(item.tienDichVu)
                });
            }
            selectFirstActiveStay();
        }

        private void initializeCheckoutEditorState() {
            for (CheckoutStayItem item : stayItems) {
                item.editedActualCheckOut = item.checkOut == null ? null : item.checkOut.toLocalDateTime();
            }
        }

        private LocalDateTime resolveDialogInitialActualCheckOut() {
            CheckoutStayItem selected = resolveFirstSelectableStay();
            return resolveEffectiveActualCheckOut(selected, LocalDateTime.now().withSecond(0).withNano(0));
        }

        private CheckoutStayItem resolveFirstSelectableStay() {
            for (CheckoutStayItem item : stayItems) {
                if (!item.isCheckedOut()) {
                    return item;
                }
            }
            return stayItems.isEmpty() ? null : stayItems.get(0);
        }

        private void selectFirstActiveStay() {
            CheckoutStayItem first = resolveFirstSelectableStay();
            if (first == null) {
                return;
            }
            int row = stayItems.indexOf(first);
            if (row >= 0) {
                tblRooms.setRowSelectionInterval(row, row);
            }
        }

        private CheckoutStayItem getSelectedCheckoutItem() {
            int row = tblRooms.getSelectedRow();
            if (row < 0 || row >= stayItems.size()) {
                return resolveFirstSelectableStay();
            }
            return stayItems.get(row);
        }

        private LocalDateTime resolveEffectiveActualCheckOut(CheckoutStayItem item, LocalDateTime fallbackNow) {
            if (item == null) {
                return fallbackNow;
            }
            if (item.editedActualCheckOut != null) {
                return item.editedActualCheckOut;
            }
            if (item.checkOut != null) {
                return item.checkOut.toLocalDateTime();
            }
            return fallbackNow;
        }

        private void loadSelectedCheckoutIntoEditor(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa) {
            CheckoutStayItem selected = getSelectedCheckoutItem();
            LocalDateTime effectiveActual = resolveEffectiveActualCheckOut(selected, LocalDateTime.now().withSecond(0).withNano(0));
            if (selected != null && selected.editedActualCheckOut == null && selected.checkOut == null) {
                selected.editedActualCheckOut = effectiveActual;
            }

            updatingCheckoutEditor = true;
            try {
                txtNgayRa.setDateValue(effectiveActual == null ? null : effectiveActual.toLocalDate());
                txtGioRa.setTimeValue(effectiveActual == null ? null : effectiveActual.toLocalTime());
            } finally {
                updatingCheckoutEditor = false;
            }
        }

        private void syncSelectedCheckoutFromEditor(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa) {
            if (updatingCheckoutEditor) {
                return;
            }
            CheckoutStayItem selected = getSelectedCheckoutItem();
            if (selected == null) {
                return;
            }
            LocalDate ngayRa = txtNgayRa.getDateValue();
            LocalTime gioRa = txtGioRa.getTimeValue();
            if (ngayRa == null || gioRa == null) {
                selected.editedActualCheckOut = null;
                return;
            }
            selected.editedActualCheckOut = LocalDateTime.of(ngayRa, gioRa);
        }

        private void submit(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa, boolean checkOutAll) {
            List<CheckoutStayItem> targets = resolveTargets(checkOutAll);
            if (targets.isEmpty()) {
                showInfo(checkOutAll ? "\u0110\u01a1n n\u00e0y kh\u00f4ng c\u00f2n ph\u00f2ng n\u00e0o \u0111ang \u1edf \u0111\u1ec3 check-out." : "Vui l\u00f2ng ch\u1ecdn ph\u00f2ng \u0111ang \u1edf \u0111\u1ec3 check-out.");
                return;
            }
            if (!syncCurrentEditorToSelectedStay(txtNgayRa, txtGioRa, targets)) {
                return;
            }
            LocalDateTime fallbackNow = LocalDateTime.now().withSecond(0).withNano(0);
            for (CheckoutStayItem item : targets) {
                LocalDateTime actualCheckOut = resolveEffectiveActualCheckOut(item, fallbackNow);
                if (item.checkIn != null && !actualCheckOut.isAfter(item.checkIn.toLocalDateTime())) {
                    showInfo("Gi\u1edd check-out ph\u1ea3i l\u1edbn h\u01a1n gi\u1edd check-in c\u1ee7a ph\u00f2ng " + item.soPhong + ".");
                    return;
                }
            }

            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showInfo("Kh\u00f4ng th\u1ec3 k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u.");
                return;
            }
            try {
                con.setAutoCommit(false);
                int affected = processCheckout(con, targets, fallbackNow);
                if (affected <= 0) {
                    con.rollback();
                    showInfo("Kh\u00f4ng c\u00f3 ph\u00f2ng n\u00e0o \u0111\u01b0\u1ee3c check-out. D\u1eef li\u1ec7u c\u00f3 th\u1ec3 \u0111\u00e3 \u0111\u01b0\u1ee3c c\u1eadp nh\u1eadt tr\u01b0\u1edbc \u0111\u00f3.");
                    return;
                }
                refreshBookingStatusAfterCheckout(con, record.maDatPhong);
                List<Integer> invoiceIds = new ThanhToanDAO().prepareInvoicesForCheckout(
                        con,
                        record.maDatPhong,
                        collectStayIds(targets),
                        checkOutAll
                );
                boolean bookingFinished = isBookingReadyForFinalPayment(con, record.maDatPhong);
                synchronizeOperationalStatuses(con);
                con.commit();

                PhongGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
                refreshKhachHangViewsSafely();
                CheckInOutGUI.refreshAllOpenInstances();
                String focusInvoiceId = resolveFocusInvoiceId(invoiceIds);
                if (focusInvoiceId != null) {
                    ThanhToanGUI.requestInvoiceFocus(focusInvoiceId);
                } else {
                    ThanhToanGUI.refreshAllOpenInstances();
                }

                if (focusInvoiceId != null || bookingFinished) {
                    NavigationUtil.navigate(
                            CheckInOutGUI.this,
                            ScreenKey.CHECK_IN_OUT,
                            ScreenKey.THANH_TOAN,
                            username,
                            role
                    );
                } else {
                    showInfo(checkOutAll
                            ? "\u0110\u00e3 check-out c\u00e1c ph\u00f2ng c\u00f2n \u0111ang \u1edf trong \u0111\u01a1n."
                            : "\u0110\u00e3 check-out ph\u00f2ng \u0111\u00e3 ch\u1ecdn. C\u00e1c ph\u00f2ng c\u00f2n l\u1ea1i v\u1eabn gi\u1eef tr\u1ea1ng th\u00e1i hi\u1ec7n t\u1ea1i.");
                }
                dispose();
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (Exception ignore) {
                }
                e.printStackTrace();
                showInfo("Kh\u00f4ng th\u1ec3 check-out ph\u00f2ng \u0111\u00e3 ch\u1ecdn.");
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (Exception ignore) {
                }
            }
        }

        private boolean syncCurrentEditorToSelectedStay(AppDatePickerField txtNgayRa,
                                                        AppTimePickerField txtGioRa,
                                                        List<CheckoutStayItem> targets) {
            CheckoutStayItem selected = getSelectedCheckoutItem();
            if (selected == null) {
                return true;
            }
            if (targets == null || !targets.contains(selected)) {
                syncSelectedCheckoutFromEditor(txtNgayRa, txtGioRa);
                return true;
            }
            LocalDate selectedDate = txtNgayRa.getDateValue();
            LocalTime selectedTime = txtGioRa.getTimeValue();
            if (selectedDate == null || selectedTime == null) {
                showInfo("Ngay gio ra cua phong dang chon khong hop le.");
                return false;
            }
            if (false && (txtNgayRa.getDateValue() == null || txtGioRa.getTimeValue() == null)) {
                showInfo("Ngày giờ ra của phòng đang chọn không hợp lệ.");
                return false;
            }
            selected.editedActualCheckOut = LocalDateTime.of(selectedDate, selectedTime);
            return true;
        }

        private List<CheckoutStayItem> resolveTargets(boolean checkOutAll) {
            List<CheckoutStayItem> targets = new ArrayList<CheckoutStayItem>();
            if (checkOutAll) {
                for (CheckoutStayItem item : stayItems) {
                    if (!item.isCheckedOut()) {
                        targets.add(item);
                    }
                }
                return targets;
            }

            int row = tblRooms.getSelectedRow();
            if (row < 0 || row >= stayItems.size()) {
                return targets;
            }
            CheckoutStayItem selected = stayItems.get(row);
            if (!selected.isCheckedOut()) {
                targets.add(selected);
            }
            return targets;
        }

        private List<Integer> collectStayIds(List<CheckoutStayItem> items) {
            List<Integer> stayIds = new ArrayList<Integer>();
            if (items == null) {
                return stayIds;
            }
            for (CheckoutStayItem item : items) {
                if (item != null && item.maLuuTru > 0) {
                    stayIds.add(Integer.valueOf(item.maLuuTru));
                }
            }
            return stayIds;
        }

        private String resolveFocusInvoiceId(List<Integer> invoiceIds) {
            if (invoiceIds == null || invoiceIds.isEmpty()) {
                return null;
            }
            Integer invoiceId = invoiceIds.get(0);
            return invoiceId == null || invoiceId.intValue() <= 0 ? null : String.valueOf(invoiceId);
        }

        private void refreshLateCheckoutPreview(AppDatePickerField txtNgayRa, AppTimePickerField txtGioRa) {
            CheckoutStayItem selected = getSelectedPreviewItem();
            lblPhongDangChonValue.setText(selected == null ? "-" : safeValue(selected.soPhong, "-"));
            lblCheckInValue.setText(selected == null ? "-" : formatDateTime(selected.checkIn));
            LocalDate selectedDate = txtNgayRa.getDateValue();
            LocalTime selectedTime = txtGioRa.getTimeValue();
            LocalDateTime actualCheckOut = selectedDate == null || selectedTime == null ? null : LocalDateTime.of(selectedDate, selectedTime);
            LateCheckoutInfo info = buildLateCheckoutInfo(selected, actualCheckOut);

            lblGioRaDuKienValue.setText(formatDateTime(info.expectedCheckOut));
            lblGioRaThucTeValue.setText(actualCheckOut == null ? "-" : formatDateTime(Timestamp.valueOf(actualCheckOut)));
            lblTraMuonValue.setText(info.isLate() ? "C\u00f3" : "Kh\u00f4ng");
            lblSoGioTreValue.setText(info.lateHours + " gi\u1edd");
            lblPhuThuTreValue.setText(formatMoney(info.lateCharge));

            Color accent = info.isLate() ? new Color(185, 28, 28) : new Color(22, 101, 52);
            lblTraMuonValue.setForeground(accent);
            lblSoGioTreValue.setForeground(accent);
            lblPhuThuTreValue.setForeground(accent);
        }

        private CheckoutStayItem getSelectedPreviewItem() {
            return getSelectedCheckoutItem();
        }

        private LateCheckoutInfo buildLateCheckoutInfo(CheckoutStayItem item, LocalDateTime actualCheckOut) {
            if (item == null) {
                return LateCheckoutInfo.empty();
            }
            Timestamp expectedCheckOut = normalizeExpectedCheckOut(item.expectedCheckOut);
            if (actualCheckOut == null) {
                return new LateCheckoutInfo(expectedCheckOut, 0L, 0d);
            }
            if (expectedCheckOut == null || !actualCheckOut.isAfter(expectedCheckOut.toLocalDateTime())) {
                return new LateCheckoutInfo(expectedCheckOut, 0L, 0d);
            }
            long lateHours = Math.max(1L, (long) Math.ceil(Duration.between(expectedCheckOut.toLocalDateTime(), actualCheckOut).toMinutes() / 60.0d));
            double hourlyRate = resolveHourlyRate(item.maBangGia, expectedCheckOut.toLocalDateTime().toLocalDate());
            return new LateCheckoutInfo(expectedCheckOut, lateHours, lateHours * Math.max(hourlyRate, 0d));
        }

        private Timestamp normalizeExpectedCheckOut(Timestamp value) {
            if (value == null) {
                return null;
            }
            LocalDateTime dateTime = value.toLocalDateTime();
            if (dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
                return Timestamp.valueOf(LocalDateTime.of(dateTime.toLocalDate(), LEGACY_EXPECTED_CHECKOUT_TIME));
            }
            return value;
        }

        private double resolveHourlyRate(int maBangGia, LocalDate pricingDate) {
            if (maBangGia <= 0) {
                return 0d;
            }
            ChiTietBangGia detail = bangGiaDAO.getChiTietBangGiaDangApDung(maBangGia, pricingDate);
            if (detail == null) {
                List<ChiTietBangGia> details = bangGiaDAO.getChiTietBangGiaByMaBangGia(maBangGia);
                if (!details.isEmpty()) {
                    detail = details.get(0);
                }
            }
            return detail == null ? 0d : Math.max(detail.getGiaTheoGio(), 0d);
        }

        private String formatDateTime(Timestamp value) {
            if (value == null) {
                return "-";
            }
            LocalDateTime dateTime = value.toLocalDateTime();
            return DATE_FORMAT.format(dateTime.toLocalDate()) + " " + TIME_FORMAT.format(dateTime.toLocalTime());
        }
    }

    private List<CheckoutStayItem> loadCheckoutStayItems(int maDatPhong) throws Exception {
        List<CheckoutStayItem> items = new ArrayList<CheckoutStayItem>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return items;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maPhong, p.soPhong, " +
                "COALESCE(lp.tenLoaiPhong, N'-') AS tenLoaiPhong, lt.checkIn, lt.checkOut, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, " +
                "ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, " +
                "ISNULL(SUM(sddv.thanhTien), 0) AS tienDichVu " +
                "FROM LuuTru lt " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "JOIN ChiTietDatPhong ctdp ON ctdp.maChiTietDatPhong = lt.maChiTietDatPhong " +
                "JOIN Phong p ON lt.maPhong = p.maPhong " +
                "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "LEFT JOIN BangGia bgHeader ON dp.maBangGia = bgHeader.maBangGia " +
                "OUTER APPLY (SELECT TOP 1 bgRoom.maBangGia FROM BangGia bgRoom " +
                "             WHERE bgRoom.maLoaiPhong = COALESCE(p.maLoaiPhong, bgHeader.maLoaiPhong) " +
                "               AND bgRoom.trangThai = N'\u0110ang \u00e1p d\u1ee5ng' " +
                "             ORDER BY CASE WHEN bgRoom.maBangGia = dp.maBangGia THEN 0 ELSE 1 END, bgRoom.maBangGia DESC) bgResolved " +
                "LEFT JOIN SuDungDichVu sddv ON sddv.maLuuTru = lt.maLuuTru " +
                "WHERE lt.maDatPhong = ? " +
                "GROUP BY lt.maLuuTru, lt.maChiTietDatPhong, lt.maPhong, p.soPhong, lp.tenLoaiPhong, lt.checkIn, lt.checkOut, ctdp.checkOutDuKien, dp.ngayTraPhong, bgResolved.maBangGia, dp.maBangGia " +
                "ORDER BY CASE WHEN TRY_CAST(p.soPhong AS INT) IS NULL THEN 1 ELSE 0 END, TRY_CAST(p.soPhong AS INT), p.soPhong, lt.maLuuTru";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CheckoutStayItem(
                            rs.getInt("maLuuTru"),
                            rs.getInt("maChiTietDatPhong"),
                            rs.getInt("maPhong"),
                            safeValue(rs.getString("soPhong"), "-"),
                            safeValue(rs.getString("tenLoaiPhong"), "-"),
                            rs.getTimestamp("checkOut") == null ? "\u0110ang \u1edf" : "\u0110\u00e3 check-out",
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("checkOut"),
                            rs.getTimestamp("checkOutDuKien"),
                            rs.getInt("maBangGiaResolved"),
                            rs.getDouble("tienDichVu")
                    ));
                }
            }
        }
        return items;
    }

    private int processCheckout(Connection con, List<CheckoutStayItem> items, LocalDateTime fallbackNow) throws Exception {
        int affected = 0;
        List<Integer> roomIds = new ArrayList<Integer>();
        try (PreparedStatement updateStay = con.prepareStatement(
                "UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ? AND checkOut IS NULL")) {
            for (CheckoutStayItem item : items) {
                LocalDateTime checkOut = item.editedActualCheckOut == null ? fallbackNow : item.editedActualCheckOut;
                updateStay.setTimestamp(1, Timestamp.valueOf(checkOut));
                updateStay.setInt(2, item.maLuuTru);
                int updated = updateStay.executeUpdate();
                if (updated > 0) {
                    affected += updated;
                    roomIds.add(Integer.valueOf(item.maPhong));
                }
            }
        }
        datPhongDAO.refreshRoomStatuses(con, roomIds);
        return affected;
    }

    private void refreshBookingStatusAfterCheckout(Connection con, int maDatPhong) throws Exception {
        checkInOutDAO.refreshBookingStatus(con, maDatPhong);
    }

    private boolean hasActiveStay(Connection con, int maDatPhong) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(1) FROM LuuTru WHERE maDatPhong = ? AND checkOut IS NULL")) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean hasPendingCheckInDetails(Connection con, int maDatPhong) throws Exception {
        String sql = "SELECT COUNT(1) "
                + "FROM ChiTietDatPhong ctdp "
                + "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                + "WHERE ctdp.maDatPhong = ? "
                + "AND ISNULL(dp.trangThai, N'') IN (N'\u0110\u00e3 \u0111\u1eb7t', N'\u0110\u00e3 x\u00e1c nh\u1eadn', N'\u0110\u00e3 c\u1ecdc', N'Ch\u1edd check-in', N'\u0110ang l\u01b0u tr\u00fa', N'\u0110\u00e3 check-in') "
                + "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong AND lt.checkOut IS NOT NULL)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean isBookingReadyForFinalPayment(Connection con, int maDatPhong) throws Exception {
        String status = checkInOutDAO.resolveBookingStatusForBooking(con, maDatPhong);
        return "Ch\u1edd thanh to\u00e1n".equalsIgnoreCase(status)
                || "\u0110\u00e3 thanh to\u00e1n".equalsIgnoreCase(status)
                || "\u0110\u00e3 check-out".equalsIgnoreCase(status);
    }

    private void synchronizeOperationalStatuses(Connection con) throws Exception {
        if (con == null) {
            return;
        }
        synchronizeOperationalStatusesWithoutCleaning(con);
    }

    private void synchronizeOperationalStatusesWithoutCleaning(Connection con) throws Exception {
        List<Integer> roomIds = new ArrayList<Integer>();
        try (PreparedStatement ps = con.prepareStatement("SELECT maPhong FROM Phong");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomIds.add(Integer.valueOf(rs.getInt("maPhong")));
            }
        }
        datPhongDAO.refreshRoomStatuses(con, roomIds);
    }

    private double parseDoubleMoney(String value) {
        try {
            return Double.parseDouble(value.replace(".", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(AppFonts.ui(Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "cio-f1", this::openCheckInDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "cio-f2", this::openAddServiceDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "cio-f3", this::openChangeRoomDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "cio-f4", this::openExtendDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "cio-f5", this::openCheckOutDialog);
        ScreenUIHelper.registerShortcut(this, "ENTER", "cio-enter", () -> {
            StayRecord record = getSelectedRecord();
            if (record != null) {
                showInfo("\u0110ang xem chi ti\u1ebft h\u1ed3 s\u01a1 " + record.maHoSo + ".");
            }
        });
    }

    private static final class StayRecord {
        private String maHoSo;
        private int maLuuTru;
        private int maChiTietDatPhong;
        private int maDatPhong;
        private int maPhongId;
        private final List<Integer> maLuuTruIds = new ArrayList<Integer>();
        private final List<Integer> maChiTietDatPhongIds = new ArrayList<Integer>();
        private final List<Integer> maPhongIds = new ArrayList<Integer>();
        private final List<String> soPhongList = new ArrayList<String>();
        private final List<ServiceStayOption> activeStayOptions = new ArrayList<ServiceStayOption>();
        private String khachHang;
        private String soPhong;
        private String loaiPhong;
        private String trangThaiPhong;
        private String tienCoc;
        private String dichVuPhatSinh;
        private String ghiChu;
        private String gioVao = "-";
        private String gioRaDuKien = "-";
        private String trangThai;
        private String bookingTrangThai;
        private String tang;
        private String caLam;
        private LocalDate expectedCheckInDate;
        private LocalDate expectedCheckOutDate;
        private int soNguoi;
        private int soLuongPhong;
        private boolean hasUnassignedRoom;
        private boolean hasPendingCheckInRooms;
        private boolean hasActiveStayRooms;

        private void addBookingDetail(int maChiTietDatPhong) {
            this.maChiTietDatPhong = maChiTietDatPhong;
            if (!maChiTietDatPhongIds.contains(Integer.valueOf(maChiTietDatPhong))) {
                maChiTietDatPhongIds.add(Integer.valueOf(maChiTietDatPhong));
            }
        }

        private void addStay(int maLuuTru, int maChiTietDatPhong, int maPhong, String soPhong, String loaiPhong) {
            this.maLuuTru = maLuuTru;
            this.maChiTietDatPhong = maChiTietDatPhong;
            addBookingDetail(maChiTietDatPhong);
            if (!maLuuTruIds.contains(Integer.valueOf(maLuuTru))) {
                maLuuTruIds.add(Integer.valueOf(maLuuTru));
            }
            addRoom(maPhong, soPhong);
            addActiveStayOption(maLuuTru, maChiTietDatPhong, maPhong, soPhong, loaiPhong);
        }

        private void addRoom(int maPhong, String soPhong) {
            this.maPhongId = maPhong;
            if (maPhong > 0 && !maPhongIds.contains(Integer.valueOf(maPhong))) {
                maPhongIds.add(Integer.valueOf(maPhong));
            }
            addDisplayRoom(soPhong);
        }

        private void addDisplayRoom(String soPhong) {
            String normalized = soPhong == null ? "-" : soPhong.trim();
            if (!normalized.isEmpty() && !soPhongList.contains(normalized)) {
                soPhongList.add(normalized);
            }
        }

        private List<String> getRoomCodes() {
            return new ArrayList<String>(soPhongList);
        }

        private boolean containsRoomCode(String roomCode) {
            if (roomCode == null || roomCode.trim().isEmpty()) {
                return false;
            }
            for (String current : soPhongList) {
                if (roomCode.equalsIgnoreCase(safeRoomCode(current))) {
                    return true;
                }
            }
            return false;
        }

        private String safeRoomCode(String value) {
            return value == null ? "" : value.trim();
        }

        private void addActiveStayOption(int maLuuTru, int maChiTietDatPhong, int maPhong, String soPhong, String loaiPhong) {
            for (ServiceStayOption option : activeStayOptions) {
                if (option.maLuuTru == maLuuTru) {
                    return;
                }
            }
            activeStayOptions.add(new ServiceStayOption(maLuuTru, maChiTietDatPhong, maPhong, soPhong, loaiPhong));
        }

        private List<ServiceStayOption> getActiveStayOptions() {
            return new ArrayList<ServiceStayOption>(activeStayOptions);
        }

        private void updateRoomSummary() {
            this.soLuongPhong = Math.max(Math.max(maPhongIds.size(), maChiTietDatPhongIds.size()), soPhongList.size());
            if (soLuongPhong <= 0) {
                soLuongPhong = hasUnassignedRoom ? 1 : 0;
            }
            this.soPhong = soPhongList.isEmpty() ? (hasUnassignedRoom ? "Ch\u01b0a g\u00e1n" : "-") : String.join(", ", soPhongList);
        }

        private void refreshAggregateState() {
            updateRoomSummary();
            String status = bookingTrangThai == null ? "" : bookingTrangThai.trim();
            if (hasActiveStayRooms) {
                this.trangThai = "\u0110ang \u1edf";
                this.trangThaiPhong = hasPendingCheckInRooms ? "\u0110ang \u1edf / c\u00f2n ph\u00f2ng ch\u1edd check-in" : "\u0110ang \u1edf";
                this.ghiChu = hasPendingCheckInRooms
                        ? (hasUnassignedRoom
                        ? "\u0110\u01a1n c\u00f3 ph\u00f2ng \u0111\u00e3 check-in v\u00e0 c\u00f2n ph\u00f2ng ch\u01b0a g\u00e1n/ch\u1edd check-in."
                        : "\u0110\u01a1n c\u00f3 ph\u00f2ng \u0111\u00e3 check-in, c\u00e1c ph\u00f2ng c\u00f2n l\u1ea1i v\u1eabn ch\u1edd check-in.")
                        : "C\u00e1c ph\u00f2ng \u0111ang \u1edf \u0111\u01b0\u1ee3c qu\u1ea3n l\u00fd theo t\u1eebng h\u1ed3 s\u01a1 l\u01b0u tr\u00fa.";
                return;
            }
            if (hasPendingCheckInRooms) {
                this.trangThai = "Ch\u1edd check-in";
                this.trangThaiPhong = hasUnassignedRoom ? "Ch\u1edd check-in / ch\u01b0a g\u00e1n ph\u00f2ng" : "\u0110\u00e3 \u0111\u1eb7t";
                this.ghiChu = hasUnassignedRoom
                        ? "\u0110\u01a1n c\u00f2n ph\u00f2ng ch\u01b0a g\u00e1n. Ch\u1ec9 c\u00e1c ph\u00f2ng \u0111\u00e3 g\u00e1n m\u1edbi c\u00f3 th\u1ec3 check-in."
                        : "\u0110\u01a1n \u0111ang ch\u1edd check-in theo t\u1eebng ph\u00f2ng.";
                return;
            }
            if ("\u0110\u00e3 thanh to\u00e1n".equalsIgnoreCase(status)) {
                this.trangThai = "\u0110\u00e3 thanh to\u00e1n";
                this.trangThaiPhong = "\u0110\u00e3 thanh to\u00e1n";
                this.ghiChu = "Booking \u0111\u00e3 ho\u00e0n t\u1ea5t thanh to\u00e1n to\u00e0n \u0111\u01a1n.";
                return;
            }
            if ("Ch\u1edd thanh to\u00e1n".equalsIgnoreCase(status) || "\u0110\u00e3 check-out".equalsIgnoreCase(status)) {
                this.trangThai = "Ch\u1edd thanh to\u00e1n";
                this.trangThaiPhong = "T\u1ea5t c\u1ea3 ph\u00f2ng \u0111\u00e3 check-out";
                this.ghiChu = "T\u1ea5t c\u1ea3 c\u00e1c ph\u00f2ng trong booking \u0111\u00e3 check-out v\u00e0 \u0111ang ch\u1edd thanh to\u00e1n.";
                return;
            }
            if ("Check-out m\u1ed9t ph\u1ea7n".equalsIgnoreCase(status)) {
                this.trangThai = "Ch\u1edd check-in";
                this.trangThaiPhong = "M\u1ed9t ph\u1ea7n ph\u00f2ng \u0111\u00e3 check-out";
                this.ghiChu = "Booking c\u00f2n ph\u00f2ng ch\u01b0a ho\u00e0n t\u1ea5t check-in n\u00ean v\u1eabn thu\u1ed9c giai \u0111o\u1ea1n v\u1eadn h\u00e0nh.";
                return;
            }
            this.trangThai = status.isEmpty() ? "Ch\u1edd check-in" : status;
            this.trangThaiPhong = this.trangThai;
            this.ghiChu = "Tr\u1ea1ng th\u00e1i booking \u0111\u00e3 \u0111\u01b0\u1ee3c \u0111\u1ed3ng b\u1ed9 theo d\u1eef li\u1ec7u th\u1ef1c t\u1ebf.";
        }

        private boolean hasMultipleRooms() {
            return soLuongPhong > 1;
        }
    }

    private static final class ServiceStayOption {
        private final int maLuuTru;
        private final int maChiTietDatPhong;
        private final int maPhong;
        private final String soPhong;
        private final String loaiPhong;

        private ServiceStayOption(int maLuuTru, int maChiTietDatPhong, int maPhong, String soPhong, String loaiPhong) {
            this.maLuuTru = maLuuTru;
            this.maChiTietDatPhong = maChiTietDatPhong;
            this.maPhong = maPhong;
            this.soPhong = soPhong == null || soPhong.trim().isEmpty() ? "-" : soPhong.trim();
            this.loaiPhong = loaiPhong == null || loaiPhong.trim().isEmpty() ? "-" : loaiPhong.trim();
        }

        private String getDisplayLabel() {
            return soPhong + " - " + loaiPhong;
        }

        @Override
        public String toString() {
            return getDisplayLabel();
        }
    }

    private static final class CheckoutStayItem {
        private final int maLuuTru;
        private final int maChiTietDatPhong;
        private final int maPhong;
        private final String soPhong;
        private final String loaiPhong;
        private final String trangThai;
        private final Timestamp checkIn;
        private final Timestamp checkOut;
        private final Timestamp expectedCheckOut;
        private final int maBangGia;
        private final double tienDichVu;
        private LocalDateTime editedActualCheckOut;

        private CheckoutStayItem(int maLuuTru, int maChiTietDatPhong, int maPhong, String soPhong,
                                 String loaiPhong, String trangThai, Timestamp checkIn, Timestamp checkOut,
                                 Timestamp expectedCheckOut, int maBangGia,
                                 double tienDichVu) {
            this.maLuuTru = maLuuTru;
            this.maChiTietDatPhong = maChiTietDatPhong;
            this.maPhong = maPhong;
            this.soPhong = soPhong;
            this.loaiPhong = loaiPhong;
            this.trangThai = trangThai;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.expectedCheckOut = expectedCheckOut;
            this.maBangGia = maBangGia;
            this.tienDichVu = tienDichVu;
        }

        private boolean isCheckedOut() {
            return checkOut != null;
        }
    }

    private static final class LateCheckoutInfo {
        private final Timestamp expectedCheckOut;
        private final long lateHours;
        private final double lateCharge;

        private LateCheckoutInfo(Timestamp expectedCheckOut, long lateHours, double lateCharge) {
            this.expectedCheckOut = expectedCheckOut;
            this.lateHours = lateHours;
            this.lateCharge = lateCharge;
        }

        private static LateCheckoutInfo empty() {
            return new LateCheckoutInfo(null, 0L, 0d);
        }

        private boolean isLate() {
            return lateHours > 0L;
        }
    }

    private static final class RoomBadge {
        private final int roomId;
        private final String roomCode;
        private final String floorName;
        private final String roomType;
        private final String statusCode;
        private final String statusText;

        private RoomBadge(int roomId, String roomCode, String floorName, String roomType, String statusCode, String statusText) {
            this.roomId = roomId;
            this.roomCode = roomCode;
            this.floorName = floorName;
            this.roomType = roomType;
            this.statusCode = statusCode;
            this.statusText = statusText;
        }
    }

    private static final class ActiveStaySnapshot {
        private final int maLuuTru;
        private final int maChiTietDatPhong;
        private final int maDatPhong;
        private final int maPhong;
        private final int maLoaiPhong;
        private final int soNguoi;
        private final String soPhong;
        private final String tenLoaiPhong;
        private final String tang;
        private final String khuVuc;
        private final Timestamp checkIn;
        private final Timestamp expectedCheckOut;
        private final double giaPhong;
        private final double tienCoc;

        private ActiveStaySnapshot(int maLuuTru,
                                   int maChiTietDatPhong,
                                   int maDatPhong,
                                   int maPhong,
                                   int maLoaiPhong,
                                   int soNguoi,
                                   String soPhong,
                                   String tenLoaiPhong,
                                   String tang,
                                   String khuVuc,
                                   Timestamp checkIn,
                                   Timestamp expectedCheckOut,
                                   double giaPhong,
                                   double tienCoc) {
            this.maLuuTru = maLuuTru;
            this.maChiTietDatPhong = maChiTietDatPhong;
            this.maDatPhong = maDatPhong;
            this.maPhong = maPhong;
            this.maLoaiPhong = maLoaiPhong;
            this.soNguoi = soNguoi;
            this.soPhong = soPhong;
            this.tenLoaiPhong = tenLoaiPhong;
            this.tang = tang;
            this.khuVuc = khuVuc;
            this.checkIn = checkIn;
            this.expectedCheckOut = expectedCheckOut;
            this.giaPhong = giaPhong;
            this.tienCoc = tienCoc;
        }
    }

    private static final class RoomOption {
        private final int maPhong;
        private final int maLoaiPhong;
        private final int sucChuaToiDa;
        private final String soPhong;
        private final String tang;
        private final String khuVuc;
        private final String loaiPhong;
        private final double giaThamChieu;

        private RoomOption(int maPhong,
                           int maLoaiPhong,
                           int sucChuaToiDa,
                           String soPhong,
                           String tang,
                           String khuVuc,
                           String loaiPhong,
                           double giaThamChieu) {
            this.maPhong = maPhong;
            this.soPhong = soPhong;
            this.tang = tang;
            this.maLoaiPhong = maLoaiPhong;
            this.sucChuaToiDa = sucChuaToiDa;
            this.khuVuc = khuVuc;
            this.loaiPhong = loaiPhong;
            this.giaThamChieu = giaThamChieu;
        }

        @Override
        public String toString() {
            if (khuVuc == null || khuVuc.trim().isEmpty() || "-".equals(khuVuc.trim())) {
                return soPhong + " - " + loaiPhong + " - " + tang;
            }
            return soPhong + " - " + loaiPhong + " - " + tang + " / " + khuVuc;
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }
}

