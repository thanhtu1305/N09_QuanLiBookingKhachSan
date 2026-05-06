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
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Container;
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
    private static final int ROOM_GRID_COLUMNS = 6;
    private static final int ROOM_GRID_GAP = 8;
    private static final int ROOM_CARD_PREFERRED_WIDTH = 124;
    private static final int ROOM_CARD_PREFERRED_HEIGHT = 150;
    private static final int GANTT_DAY_COUNT = 7;
    private static final int GANTT_ROOM_INFO_WIDTH = 300;
    private static final int GANTT_ROW_MIN_HEIGHT = 86;
    private static final int GANTT_LANE_HEIGHT = 26;
    private static final int GANTT_ROW_GAP = 10;

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
    private final Map<Integer, StayRecord> recordsByBookingId = new LinkedHashMap<Integer, StayRecord>();
    private final Map<String, RoomBlockState> roomBlocksByCode = new LinkedHashMap<String, RoomBlockState>();
    private final Map<String, JPanel> roomBlockCards = new LinkedHashMap<String, JPanel>();
    private final Map<String, JButton> floorButtons = new LinkedHashMap<String, JButton>();
    private final Set<String> highlightedRoomCodes = new LinkedHashSet<String>();
    private final List<String> availableFloors = new ArrayList<String>();
    private JPanel roomGridPanel;
    private JPanel roomLegendPanel;
    private JPanel roomTimelineHeaderPanel;
    private JPanel detailRelatedRoomsPanel;
    private JPanel detailActionPanel;
    private JLabel lblRoomMapTitle;
    private JLabel lblRoomMapMeta;
    private JLabel lblGanttRange;
    private JLabel lblDetailBookingCode;
    private JLabel lblDetailStayCode;
    private JLabel lblDetailGuestName;
    private JLabel lblDetailRoomCode;
    private JLabel lblDetailRoomType;
    private JLabel lblDetailFloor;
    private JLabel lblDetailStatus;
    private JLabel lblDetailGuestCount;
    private JLabel lblDetailCheckIn;
    private JLabel lblDetailCheckOut;
    private JLabel lblDetailNextBooking;
    private JTextArea txtDetailNotes;
    private RoomBlockState selectedRoomBlock;
    private LocalDate ganttStartDate = LocalDate.now();

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
        top.add(buildRoomControlBar());

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

    private JPanel buildRoomControlBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("S\u01a1 \u0111\u1ed3 ph\u00f2ng Check-in / Check-out");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSubtitle = new JLabel("Hi\u1ec3n th\u1ecb ph\u00f2ng theo t\u1eebng t\u1ea7ng, badge v\u1eadn h\u00e0nh tr\u1ef1c quan v\u00e0 highlight to\u00e0n booking khi c\u1ea7n thao t\u00e1c nhanh.");
        lblSubtitle.setFont(BODY_FONT);
        lblSubtitle.setForeground(TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(4));
        left.add(lblSubtitle);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel lblHint = new JLabel("Double click ph\u00f2ng \u0111\u1ec3 m\u1edf popup \u0111\u00fang theo tr\u1ea1ng th\u00e1i.");
        lblHint.setFont(AppFonts.body(12));
        lblHint.setForeground(TEXT_MUTED);

        JLabel lblSearch = new JLabel("T\u00ecm nhanh");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(260, 34));
        txtTuKhoa.setToolTipText("S\u1ed1 ph\u00f2ng / m\u00e3 booking / t\u00ean ng\u01b0\u1eddi \u0111\u1ea1i di\u1ec7n");
        ScreenUIHelper.installLiveSearch(txtTuKhoa, this::refreshRoomGrid);

        right.add(lblHint);
        right.add(Box.createVerticalStrut(8));
        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));
        right.add(txtTuKhoa);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildRoomMapCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 12));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        lblRoomMapTitle = new JLabel("To\u00e0n b\u1ed9 ph\u00f2ng theo t\u1ea7ng");
        lblRoomMapTitle.setFont(SECTION_FONT);
        lblRoomMapTitle.setForeground(TEXT_PRIMARY);

        lblRoomMapMeta = new JLabel("M\u1ed7i t\u1ea7ng hi\u1ec3n th\u1ecb t\u1ed1i \u0111a 6 ph\u00f2ng tr\u00ean m\u1ed9t h\u00e0ng, cu\u1ed9n d\u1ecdc khi nhi\u1ec1u ph\u00f2ng.");
        lblRoomMapMeta.setFont(BODY_FONT);
        lblRoomMapMeta.setForeground(TEXT_MUTED);

        roomLegendPanel = buildRoomLegendPanel();
        header.add(lblRoomMapTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblRoomMapMeta);
        header.add(Box.createVerticalStrut(10));
        header.add(roomLegendPanel);

        roomGridPanel = new RoomMapScrollPanel();
        roomGridPanel.setLayout(new BoxLayout(roomGridPanel, BoxLayout.Y_AXIS));
        roomGridPanel.setOpaque(false);
        roomGridPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(roomGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                roomGridPanel.revalidate();
                roomGridPanel.repaint();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildRoomLegendPanel() {
        JPanel legend = new JPanel(new java.awt.GridLayout(0, 3, 8, 6));
        legend.setOpaque(false);
        legend.add(createLegendChip("S\u1eb5n s\u00e0ng", resolveBlockAccent(RoomStatusKey.AVAILABLE)));
        legend.add(createLegendChip("\u0110\u00e3 \u0111\u1eb7t", resolveBlockAccent(RoomStatusKey.BOOKED)));
        legend.add(createLegendChip("\u0110ang \u1edf", resolveBlockAccent(RoomStatusKey.OCCUPIED)));
        legend.add(createLegendChip("TT: Ch\u1edd thanh to\u00e1n", resolveBlockAccent(RoomStatusKey.WAIT_PAYMENT)));
        legend.add(createLegendChip("DN: \u0110ang d\u1ecdn", resolveBlockAccent(RoomStatusKey.CLEANING)));
        legend.add(createLegendChip("\u2022: S\u1eafp c\u00f3 kh\u00e1ch", new Color(17, 24, 39)));
        legend.add(createLegendChip("B\u1ea3o tr\u00ec", resolveBlockAccent(RoomStatusKey.MAINTENANCE)));
        return legend;
    }

    private JPanel createLegendChip(String text, Color color) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chip.setOpaque(false);

        JPanel dot = new JPanel();
        dot.setBackground(color);
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setBorder(BorderFactory.createLineBorder(color.darker(), 1, true));

        JLabel lbl = new JLabel(text);
        lbl.setFont(AppFonts.body(12));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setToolTipText(text);

        chip.add(dot);
        chip.add(lbl);
        return chip;
    }

    private JPanel buildRoomDetailColumn() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMinimumSize(new Dimension(360, 1));
        wrapper.setPreferredSize(new Dimension(390, 1));
        wrapper.add(buildOperationDetailCard(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildOperationDetailCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 12));

        JLabel lblTitle = new JLabel("Chi ti\u1ebft ph\u00f2ng / booking");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel content = new DetailScrollPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 0, 0, 4));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetailBookingCode = createValueLabel();
        lblDetailStayCode = createValueLabel();
        lblDetailGuestName = createValueLabel();
        lblDetailRoomCode = createValueLabel();
        lblDetailRoomType = createValueLabel();
        lblDetailFloor = createValueLabel();
        lblDetailStatus = createValueLabel();
        lblDetailGuestCount = createValueLabel();
        lblDetailCheckIn = createValueLabel();
        lblDetailCheckOut = createValueLabel();
        lblDetailNextBooking = createValueLabel();

        addDetailRow(content, "M\u00e3 booking", lblDetailBookingCode);
        addDetailRow(content, "M\u00e3 l\u01b0u tr\u00fa", lblDetailStayCode);
        addDetailRow(content, "Ng\u01b0\u1eddi \u0111\u1ea1i di\u1ec7n", lblDetailGuestName);
        addDetailRow(content, "Ph\u00f2ng", lblDetailRoomCode);
        addDetailRow(content, "Lo\u1ea1i ph\u00f2ng", lblDetailRoomType);
        addDetailRow(content, "T\u1ea7ng", lblDetailFloor);
        addDetailRow(content, "Tr\u1ea1ng th\u00e1i", lblDetailStatus);
        addDetailRow(content, "S\u1ed1 ng\u01b0\u1eddi", lblDetailGuestCount);
        addDetailRow(content, "Gi\u1edd v\u00e0o", lblDetailCheckIn);
        addDetailRow(content, "Gi\u1edd ra d\u1ef1 ki\u1ebfn", lblDetailCheckOut);
        addDetailRow(content, "Booking g\u1ea7n nh\u1ea5t ti\u1ebfp theo", lblDetailNextBooking);

        JLabel lblRelatedRooms = new JLabel("Ph\u00f2ng c\u00f9ng booking");
        lblRelatedRooms.setFont(LABEL_FONT);
        lblRelatedRooms.setForeground(TEXT_MUTED);
        lblRelatedRooms.setBorder(new EmptyBorder(4, 0, 4, 0));
        lblRelatedRooms.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRelatedRooms.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblRelatedRooms.getPreferredSize().height));
        detailRelatedRoomsPanel = new JPanel();
        detailRelatedRoomsPanel.setOpaque(false);
        detailRelatedRoomsPanel.setLayout(new BoxLayout(detailRelatedRoomsPanel, BoxLayout.Y_AXIS));
        detailRelatedRoomsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailRelatedRoomsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lblNotes = new JLabel("Ghi ch\u00fa v\u1eadn h\u00e0nh");
        lblNotes.setFont(LABEL_FONT);
        lblNotes.setForeground(TEXT_MUTED);
        lblNotes.setBorder(new EmptyBorder(6, 0, 4, 0));
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblNotes.getPreferredSize().height));

        txtDetailNotes = new JTextArea(5, 20);
        txtDetailNotes.setEditable(false);
        txtDetailNotes.setLineWrap(true);
        txtDetailNotes.setWrapStyleWord(true);
        txtDetailNotes.setFont(BODY_FONT);
        txtDetailNotes.setForeground(TEXT_PRIMARY);
        txtDetailNotes.setBackground(PANEL_SOFT);
        txtDetailNotes.setRows(4);
        txtDetailNotes.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtDetailNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDetailNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        txtDetailNotes.setMinimumSize(new Dimension(0, 88));

        JLabel lblActions = new JLabel("Thao t\u00e1c nhanh");
        lblActions.setFont(LABEL_FONT);
        lblActions.setForeground(TEXT_MUTED);
        lblActions.setBorder(new EmptyBorder(6, 0, 4, 0));
        lblActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblActions.getPreferredSize().height));

        detailActionPanel = new JPanel();
        detailActionPanel.setOpaque(false);
        detailActionPanel.setLayout(new BoxLayout(detailActionPanel, BoxLayout.Y_AXIS));
        detailActionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailActionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        content.add(lblRelatedRooms);
        content.add(detailRelatedRoomsPanel);
        content.add(lblNotes);
        content.add(txtDetailNotes);
        content.add(lblActions);
        content.add(detailActionPanel);

        JScrollPane detailScrollPane = new JScrollPane(content);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(18);
        detailScrollPane.setOpaque(false);
        detailScrollPane.getViewport().setOpaque(false);

        card.add(detailScrollPane, BorderLayout.CENTER);
        clearOperationDetailPanel();
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
        JPanel roomMapCard = buildRoomMapCard();
        JPanel detailColumn = buildRoomDetailColumn();
        roomMapCard.setMinimumSize(new Dimension(560, 1));
        detailColumn.setMinimumSize(new Dimension(360, 1));
        detailColumn.setPreferredSize(new Dimension(390, 1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, roomMapCard, detailColumn);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.76);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "Click Ch\u1ecdn ph\u00f2ng",
                "Double click Thao t\u00e1c theo tr\u1ea1ng th\u00e1i",
                "Panel ph\u1ea3i Xem booking c\u00f9ng \u0111\u01a1n",
                "Badge G\u1ee3i \u00fd ph\u00f2ng s\u1eafp c\u00f3 kh\u00e1ch"
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
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(132, 20));
        lbl.setMinimumSize(new Dimension(112, 20));
        value.setToolTipText(value.getText());

        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
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
        rebuildBookingIndex();
        loadRoomBlockStates();
        refreshRoomGrid();
        if (showMessage) {
            showInfo("\u0110\u00e3 l\u00e0m m\u1edbi d\u1eef li\u1ec7u check-in / check-out.");
        }
    }

    private void loadStayData() {
        allRecords.clear();
        Map<Integer, StayRecord> grouped = new LinkedHashMap<Integer, StayRecord>();
        loadPendingBookings(grouped);
        loadActiveStays(grouped);
        loadCompletedBookings(grouped);
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
                    record.observeSummaryCheckIn(checkInDuKien);
                    record.observeSummaryCheckOut(checkOutDuKien);
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
                record.observeSummaryCheckIn(checkInDuKien);
                record.observeSummaryCheckOut(checkOutDuKien);
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
                        record.observeSummaryCheckOut(expectedCheckOut);
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
                record.observeSummaryCheckIn(checkInTs);
                if (checkInTs != null) {
                    LocalDateTime current = checkInTs.toLocalDateTime();
                    if (record.expectedCheckInDate == null || current.toLocalDate().isBefore(record.expectedCheckInDate)) {
                        record.expectedCheckInDate = current.toLocalDate();
                        record.gioVao = DATE_FORMAT.format(current.toLocalDate()) + " " + TIME_FORMAT.format(current.toLocalTime());
                    }
                }
                Timestamp expectedCheckOut = rs.getTimestamp("checkOutDuKien");
                record.observeSummaryCheckOut(expectedCheckOut);
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
                    record.observeSummaryCheckIn(checkInDuKien);
                    record.observeSummaryCheckOut(checkOutDuKien);
                    grouped.put(Integer.valueOf(bookingId), record);
                }

                record.soNguoi += rs.getInt("soNguoi");
                record.addBookingDetail(rs.getInt("maChiTietDatPhong"));
                Timestamp checkInDuKien = rs.getTimestamp("checkInDuKien");
                Timestamp checkOutDuKien = rs.getTimestamp("checkOutDuKien");
                record.observeSummaryCheckIn(checkInDuKien);
                record.observeSummaryCheckOut(checkOutDuKien);
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

    private void rebuildBookingIndex() {
        recordsByBookingId.clear();
        filteredRecords.clear();
        for (StayRecord record : allRecords) {
            if (record == null) {
                continue;
            }
            recordsByBookingId.put(Integer.valueOf(record.maDatPhong), record);
            filteredRecords.add(record);
        }
    }

    private void loadRoomBlockStates() {
        roomBlocksByCode.clear();
        availableFloors.clear();
        synchronizeStalePendingPaymentRoomStatuses();
        loadBaseRoomBlocks();
        loadOccupiedRoomBlocks();
        loadPendingPaymentRoomBlocks();
        loadBookedRoomBlocks();
        loadFutureRoomHints();
    }

    private void loadBaseRoomBlocks() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        String sql = "SELECT p.maPhong, ISNULL(p.soPhong, N'-') AS soPhong, ISNULL(p.tang, N'T\u1ea7ng kh\u00e1c') AS tang, "
                + "ISNULL(lp.tenLoaiPhong, N'-') AS tenLoaiPhong, ISNULL(p.trangThai, N'Ho\u1ea1t \u0111\u1ed9ng') AS trangThai "
                + "FROM Phong p "
                + "LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "ORDER BY TRY_CAST(REPLACE(ISNULL(p.tang, N''), N'T\u1ea7ng ', N'') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String floorName = normalizeFloorName(rs.getString("tang"));
                RoomBlockState state = new RoomBlockState(
                        rs.getInt("maPhong"),
                        safeValue(rs.getString("soPhong"), "-"),
                        floorName,
                        safeValue(rs.getString("tenLoaiPhong"), "-"),
                        safeValue(rs.getString("trangThai"), "Ho\u1ea1t \u0111\u1ed9ng")
                );
                applyBaseRoomStatus(state);
                roomBlocksByCode.put(state.roomCode, state);
                registerAvailableFloor(floorName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOccupiedRoomBlocks() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, lt.checkIn, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, "
                + "ISNULL(p.soPhong, N'-') AS soPhong, ISNULL(kh.hoTen, N'-') AS hoTen, ISNULL(dp.trangThai, N'\u0110ang \u1edf') AS trangThaiDatPhong, "
                + "ISNULL(lt.soNguoi, ctdp.soNguoi) AS soNguoi "
                + "FROM LuuTru lt "
                + "JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong "
                + "JOIN ChiTietDatPhong ctdp ON ctdp.maChiTietDatPhong = lt.maChiTietDatPhong "
                + "JOIN Phong p ON p.maPhong = lt.maPhong "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE lt.checkOut IS NULL";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomBlockState state = roomBlocksByCode.get(safeValue(rs.getString("soPhong"), "-"));
                if (state == null || RoomStatusKey.MAINTENANCE.equals(state.statusKey)) {
                    continue;
                }
                state.statusKey = RoomStatusKey.OCCUPIED;
                state.statusText = "\u0110ang \u1edf";
                state.bookingId = Integer.valueOf(rs.getInt("maDatPhong"));
                state.stayId = Integer.valueOf(rs.getInt("maLuuTru"));
                state.detailId = Integer.valueOf(rs.getInt("maChiTietDatPhong"));
                state.bookingCode = "DP" + rs.getInt("maDatPhong");
                state.representativeName = safeValue(rs.getString("hoTen"), "-");
                state.guestCount = Math.max(0, rs.getInt("soNguoi"));
                state.checkInTime = rs.getTimestamp("checkIn") == null ? null : rs.getTimestamp("checkIn").toLocalDateTime();
                state.expectedCheckOut = rs.getTimestamp("checkOutDuKien") == null ? null : rs.getTimestamp("checkOutDuKien").toLocalDateTime();
                state.note = "Ph\u00f2ng \u0111ang c\u00f3 kh\u00e1ch l\u01b0u tr\u00fa. Double click \u0111\u1ec3 m\u1edf popup v\u1eadn h\u00e0nh.";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPendingPaymentRoomBlocks() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        String unpaidStatusClause = "ISNULL(hd.trangThai, N'Ch\u1edd thanh to\u00e1n') NOT IN (N'\u0110\u00e3 thanh to\u00e1n', N'\u0110\u00e3 ho\u00e0n c\u1ecdc')";
        String sql = "WITH latestClosedStay AS ("
                + "    SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, lt.checkIn, lt.checkOut, lt.soNguoi, "
                + "           ROW_NUMBER() OVER (PARTITION BY lt.maPhong ORDER BY lt.checkOut DESC, lt.maLuuTru DESC) AS rn "
                + "    FROM LuuTru lt "
                + "    WHERE lt.checkOut IS NOT NULL"
                + ") "
                + "SELECT lcs.maLuuTru, lcs.maChiTietDatPhong, lcs.maDatPhong, lcs.checkIn, lcs.checkOut, "
                + "       ISNULL(p.soPhong, N'-') AS soPhong, ISNULL(kh.hoTen, N'-') AS hoTen, ISNULL(lcs.soNguoi, 0) AS soNguoi, "
                + "       COALESCE(roomInv.maHoaDon, bookingInv.maHoaDon) AS maHoaDon "
                + "FROM latestClosedStay lcs "
                + "JOIN Phong p ON p.maPhong = lcs.maPhong "
                + "JOIN DatPhong dp ON dp.maDatPhong = lcs.maDatPhong "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "OUTER APPLY (SELECT TOP 1 hd.maHoaDon FROM HoaDon hd "
                + "             WHERE hd.maChiTietDatPhong = lcs.maChiTietDatPhong AND " + unpaidStatusClause + " "
                + "             ORDER BY CASE WHEN ISNULL(hd.trangThai, N'Ch\u1edd thanh to\u00e1n') = N'Ch\u1edd thanh to\u00e1n' THEN 0 ELSE 1 END, hd.maHoaDon DESC) roomInv "
                + "OUTER APPLY (SELECT TOP 1 hd.maHoaDon FROM HoaDon hd "
                + "             WHERE hd.maDatPhong = lcs.maDatPhong AND hd.maChiTietDatPhong IS NULL AND " + unpaidStatusClause + " "
                + "             ORDER BY CASE WHEN ISNULL(hd.trangThai, N'Ch\u1edd thanh to\u00e1n') = N'Ch\u1edd thanh to\u00e1n' THEN 0 ELSE 1 END, hd.maHoaDon DESC) bookingInv "
                + "WHERE lcs.rn = 1 AND COALESCE(roomInv.maHoaDon, bookingInv.maHoaDon) IS NOT NULL";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomBlockState state = roomBlocksByCode.get(safeValue(rs.getString("soPhong"), "-"));
                if (state == null || RoomStatusKey.MAINTENANCE.equals(state.statusKey) || RoomStatusKey.OCCUPIED.equals(state.statusKey)) {
                    continue;
                }
                state.statusKey = RoomStatusKey.WAIT_PAYMENT;
                state.statusText = "Ch\u1edd thanh to\u00e1n";
                state.bookingId = Integer.valueOf(rs.getInt("maDatPhong"));
                state.stayId = Integer.valueOf(rs.getInt("maLuuTru"));
                state.detailId = Integer.valueOf(rs.getInt("maChiTietDatPhong"));
                state.invoiceId = rs.getObject("maHoaDon") == null ? null : Integer.valueOf(rs.getInt("maHoaDon"));
                state.bookingCode = "DP" + rs.getInt("maDatPhong");
                state.representativeName = safeValue(rs.getString("hoTen"), "-");
                state.guestCount = Math.max(0, rs.getInt("soNguoi"));
                state.checkInTime = rs.getTimestamp("checkIn") == null ? null : rs.getTimestamp("checkIn").toLocalDateTime();
                state.actualCheckOut = rs.getTimestamp("checkOut") == null ? null : rs.getTimestamp("checkOut").toLocalDateTime();
                state.note = "Ph\u00f2ng \u0111\u00e3 check-out, \u0111ang ch\u1edd thanh to\u00e1n. Double click \u0111\u1ec3 m\u1edf popup thanh to\u00e1n.";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchronizeStalePendingPaymentRoomStatuses() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        String unpaidStatusClause = "ISNULL(hd.trangThai, N'Ch\u1edd thanh to\u00e1n') NOT IN (N'\u0110\u00e3 thanh to\u00e1n', N'\u0110\u00e3 ho\u00e0n c\u1ecdc')";
        String sql = "SELECT p.maPhong "
                + "FROM Phong p "
                + "OUTER APPLY ("
                + "    SELECT TOP 1 lt.maChiTietDatPhong, lt.maDatPhong "
                + "    FROM LuuTru lt "
                + "    WHERE lt.maPhong = p.maPhong AND lt.checkOut IS NOT NULL "
                + "    ORDER BY lt.checkOut DESC, lt.maLuuTru DESC"
                + ") lcs "
                + "OUTER APPLY (SELECT TOP 1 hd.maHoaDon FROM HoaDon hd "
                + "             WHERE hd.maChiTietDatPhong = lcs.maChiTietDatPhong AND " + unpaidStatusClause + " "
                + "             ORDER BY hd.maHoaDon DESC) roomInv "
                + "OUTER APPLY (SELECT TOP 1 hd.maHoaDon FROM HoaDon hd "
                + "             WHERE hd.maDatPhong = lcs.maDatPhong AND hd.maChiTietDatPhong IS NULL AND " + unpaidStatusClause + " "
                + "             ORDER BY hd.maHoaDon DESC) bookingInv "
                + "WHERE ISNULL(p.trangThai, N'') = N'Ch\u1edd thanh to\u00e1n' "
                + "AND roomInv.maHoaDon IS NULL AND bookingInv.maHoaDon IS NULL";
        List<Integer> staleRoomIds = new ArrayList<Integer>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                staleRoomIds.add(Integer.valueOf(rs.getInt("maPhong")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (Integer roomId : staleRoomIds) {
            try {
                datPhongDAO.refreshRoomStatus(con, roomId.intValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadBookedRoomBlocks() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maPhong, dp.maDatPhong, "
                + "COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))) AS checkInDuKien, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, "
                + "ISNULL(p.soPhong, N'-') AS soPhong, ISNULL(kh.hoTen, N'-') AS hoTen, ISNULL(ctdp.soNguoi, 0) AS soNguoi "
                + "FROM DatPhong dp "
                + "JOIN ChiTietDatPhong ctdp ON ctdp.maDatPhong = dp.maDatPhong "
                + "JOIN Phong p ON p.maPhong = ctdp.maPhong "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE ISNULL(dp.trangThai, N'') IN (N'\u0110\u00e3 \u0111\u1eb7t', N'\u0110\u00e3 x\u00e1c nh\u1eadn', N'\u0110\u00e3 c\u1ecdc', N'Ch\u1edd check-in') "
                + "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong AND lt.checkOut IS NULL)";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                RoomBlockState state = roomBlocksByCode.get(safeValue(rs.getString("soPhong"), "-"));
                if (state == null || RoomStatusKey.MAINTENANCE.equals(state.statusKey) || RoomStatusKey.OCCUPIED.equals(state.statusKey)
                        || RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey) || RoomStatusKey.CLEANING.equals(state.statusKey)) {
                    continue;
                }
                state.statusKey = RoomStatusKey.BOOKED;
                state.statusText = "\u0110\u00e3 \u0111\u1eb7t";
                state.bookingId = Integer.valueOf(rs.getInt("maDatPhong"));
                state.detailId = Integer.valueOf(rs.getInt("maChiTietDatPhong"));
                state.bookingCode = "DP" + rs.getInt("maDatPhong");
                state.representativeName = safeValue(rs.getString("hoTen"), "-");
                state.guestCount = Math.max(0, rs.getInt("soNguoi"));
                state.expectedCheckIn = rs.getTimestamp("checkInDuKien") == null ? null : rs.getTimestamp("checkInDuKien").toLocalDateTime();
                state.expectedCheckOut = rs.getTimestamp("checkOutDuKien") == null ? null : rs.getTimestamp("checkOutDuKien").toLocalDateTime();
                state.note = "Ph\u00f2ng \u0111ang ch\u1edd check-in. Double click \u0111\u1ec3 m\u1edf popup check-in t\u1ed1i gi\u1ea3n.";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFutureRoomHints() {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        Map<Integer, List<RoomFutureHint>> hintsByRoomId = new LinkedHashMap<Integer, List<RoomFutureHint>>();
        String sql = "SELECT ctdp.maPhong, ctdp.maChiTietDatPhong, dp.maDatPhong, ISNULL(kh.hoTen, N'-') AS hoTen, "
                + "COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))) AS checkInDuKien, "
                + "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien "
                + "FROM DatPhong dp "
                + "JOIN ChiTietDatPhong ctdp ON ctdp.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE ctdp.maPhong IS NOT NULL "
                + "AND ISNULL(dp.trangThai, N'') NOT IN (N'\u0110\u00e3 h\u1ee7y', N'H\u1ee7y booking', N'\u0110\u00e3 thanh to\u00e1n') "
                + "ORDER BY ctdp.maPhong, COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))), dp.maDatPhong";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer roomId = Integer.valueOf(rs.getInt("maPhong"));
                if (!hintsByRoomId.containsKey(roomId)) {
                    hintsByRoomId.put(roomId, new ArrayList<RoomFutureHint>());
                }
                hintsByRoomId.get(roomId).add(new RoomFutureHint(
                        rs.getInt("maDatPhong"),
                        rs.getInt("maChiTietDatPhong"),
                        rs.getTimestamp("checkInDuKien") == null ? null : rs.getTimestamp("checkInDuKien").toLocalDateTime(),
                        rs.getTimestamp("checkOutDuKien") == null ? null : rs.getTimestamp("checkOutDuKien").toLocalDateTime(),
                        safeValue(rs.getString("hoTen"), "-")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDateTime now = LocalDateTime.now();
        for (RoomBlockState state : roomBlocksByCode.values()) {
            List<RoomFutureHint> candidates = hintsByRoomId.get(Integer.valueOf(state.roomId));
            if (candidates == null || candidates.isEmpty()) {
                continue;
            }
            List<RoomFutureHint> relevant = new ArrayList<RoomFutureHint>();
            for (RoomFutureHint hint : candidates) {
                if (hint == null || hint.expectedCheckIn == null) {
                    continue;
                }
                if (state.detailId != null && hint.detailId == state.detailId.intValue()) {
                    continue;
                }
                if (RoomStatusKey.OCCUPIED.equals(state.statusKey)) {
                    LocalDateTime boundary = state.expectedCheckOut == null ? now : state.expectedCheckOut.minusMinutes(1L);
                    if (!hint.expectedCheckIn.isBefore(boundary)) {
                        relevant.add(hint);
                    }
                    continue;
                }
                if (RoomStatusKey.BOOKED.equals(state.statusKey)) {
                    LocalDateTime boundary = state.expectedCheckIn == null ? now : state.expectedCheckIn;
                    if (!hint.expectedCheckIn.isBefore(boundary)) {
                        relevant.add(hint);
                    }
                    continue;
                }
                if (!hint.expectedCheckIn.isBefore(now)) {
                    relevant.add(hint);
                }
            }
            if (!relevant.isEmpty()) {
                state.futureHintCount = relevant.size();
                state.nextFutureHint = relevant.get(0);
            }
        }
    }

    private void ensureSelectedFloorAvailable() {
    }

    private void registerAvailableFloor(String floorName) {
        if (!availableFloors.contains(floorName)) {
            availableFloors.add(floorName);
        }
    }

    private String normalizeFloorName(String floorName) {
        String value = safeValue(floorName, "T\u1ea7ng kh\u00e1c");
        if (value.matches("\\d+")) {
            return "T\u1ea7ng " + value;
        }
        return value;
    }

    private void applyBaseRoomStatus(RoomBlockState state) {
        if (state == null) {
            return;
        }
        String rawStatus = safeValue(state.rawRoomStatus, "Ho\u1ea1t \u0111\u1ed9ng");
        if ("B\u1ea3o tr\u00ec".equalsIgnoreCase(rawStatus) || "Kh\u00f4ng ho\u1ea1t \u0111\u1ed9ng".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.MAINTENANCE;
            state.statusText = "B\u1ea3o tr\u00ec";
            state.note = "Ph\u00f2ng \u0111ang \u1edf tr\u1ea1ng th\u00e1i b\u1ea3o tr\u00ec.";
            return;
        }
        if ("D\u1ecdn d\u1eb9p".equalsIgnoreCase(rawStatus) || "D\u1ecdn ph\u00f2ng".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.CLEANING;
            state.statusText = "D\u1ecdn ph\u00f2ng";
            state.note = "Ph\u00f2ng \u0111\u00e3 thanh to\u00e1n xong v\u00e0 \u0111ang ch\u1edd x\u00e1c nh\u1eadn d\u1ecdn.";
            return;
        }
        if ("Ch\u1edd thanh to\u00e1n".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.CLEANING;
            state.statusText = "D\u1ecdn ph\u00f2ng";
            state.note = "Ph\u00f2ng \u0111\u00e3 check-out. H\u1ec7 th\u1ed1ng s\u1ebd ch\u1ec9 hi\u1ec3n th\u1ecb ch\u1edd thanh to\u00e1n khi c\u00f2n h\u00f3a \u0111\u01a1n ch\u01b0a thanh to\u00e1n.";
            return;
        }
        if ("\u0110ang \u1edf".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.OCCUPIED;
            state.statusText = "\u0110ang \u1edf";
            state.note = "Ph\u00f2ng \u0111ang c\u00f3 kh\u00e1ch l\u01b0u tr\u00fa.";
            return;
        }
        if ("\u0110\u00e3 \u0111\u1eb7t".equalsIgnoreCase(rawStatus) || "Ch\u1edd check-in".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.BOOKED;
            state.statusText = "\u0110\u00e3 \u0111\u1eb7t";
            state.note = "Ph\u00f2ng \u0111ang ch\u1edd check-in.";
            return;
        }
        if ("Tr\u1ed1ng".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.AVAILABLE;
            state.statusText = "Tr\u1ed1ng";
            state.note = "Ph\u00f2ng tr\u1ed1ng v\u00e0 c\u00f3 th\u1ec3 ph\u1ee5c v\u1ee5 ngay.";
            return;
        }
        if ("S\u1eb5n s\u00e0ng".equalsIgnoreCase(rawStatus)) {
            state.statusKey = RoomStatusKey.AVAILABLE;
            state.statusText = "S\u1eb5n s\u00e0ng";
            state.note = "Ph\u00f2ng \u0111\u00e3 s\u1eb5n s\u00e0ng \u0111\u1ec3 khai th\u00e1c.";
            return;
        }
        state.statusKey = RoomStatusKey.AVAILABLE;
        state.statusText = "S\u1eb5n s\u00e0ng";
        state.note = "Ph\u00f2ng kh\u00f4ng c\u00f3 l\u01b0u tr\u00fa hi\u1ec7n t\u1ea1i.";
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
                    record.getSummaryCheckInDisplay(),
                    record.getSummaryCheckOutDisplay(),
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

    private void refreshRoomGrid() {
        if (roomGridPanel == null) {
            return;
        }
        roomGridPanel.removeAll();
        roomBlockCards.clear();

        String keyword = txtTuKhoa == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
        List<RoomBlockState> visibleRooms = new ArrayList<RoomBlockState>();
        for (RoomBlockState state : roomBlocksByCode.values()) {
            if (state == null) {
                continue;
            }
            if (!matchesRoomSearch(state, keyword)) {
                continue;
            }
            visibleRooms.add(state);
        }

        Map<String, List<RoomBlockState>> roomsByFloor = groupVisibleRoomsByFloor(visibleRooms);
        for (Map.Entry<String, List<RoomBlockState>> entry : roomsByFloor.entrySet()) {
            roomGridPanel.add(createFloorSectionPanel(entry.getKey(), entry.getValue()));
            roomGridPanel.add(Box.createVerticalStrut(12));
        }

        if (lblRoomMapTitle != null) {
            lblRoomMapTitle.setText("To\u00e0n b\u1ed9 ph\u00f2ng theo t\u1ea7ng");
        }
        if (lblRoomMapMeta != null) {
            lblRoomMapMeta.setText(visibleRooms.isEmpty()
                    ? "Kh\u00f4ng c\u00f3 ph\u00f2ng ph\u00f9 h\u1ee3p b\u1ed9 l\u1ecdc hi\u1ec7n t\u1ea1i."
                    : "Hi\u1ec3n th\u1ecb " + visibleRooms.size() + " ph\u00f2ng, t\u1ed1i \u0111a " + ROOM_GRID_COLUMNS + " ph\u00f2ng m\u1ed7i h\u00e0ng.");
        }

        if (visibleRooms.isEmpty()) {
            JPanel emptyState = new JPanel(new BorderLayout());
            emptyState.setOpaque(false);
            JLabel lblEmpty = new JLabel("Kh\u00f4ng c\u00f3 ph\u00f2ng n\u00e0o ph\u00f9 h\u1ee3p b\u1ed9 l\u1ecdc hi\u1ec7n t\u1ea1i.", SwingConstants.CENTER);
            lblEmpty.setFont(BODY_FONT);
            lblEmpty.setForeground(TEXT_MUTED);
            emptyState.add(lblEmpty, BorderLayout.CENTER);
            roomGridPanel.add(emptyState);
        } else {
            roomGridPanel.add(Box.createVerticalGlue());
        }

        roomGridPanel.revalidate();
        roomGridPanel.repaint();
        restoreRoomSelection(visibleRooms);
    }

    private boolean matchesRoomSearch(RoomBlockState state, String keyword) {
        if (state == null || keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String source = (safeValue(state.roomCode, "")
                + " " + safeValue(state.bookingCode, "")
                + " " + safeValue(state.representativeName, "")
                + " " + state.guestCount
                + " " + safeValue(state.floorName, "")
                + " " + safeValue(state.statusText, "")).toLowerCase(Locale.ROOT);
        return source.contains(keyword);
    }

    private void restoreRoomSelection(List<RoomBlockState> visibleRooms) {
        RoomBlockState preferred = null;
        if (pendingFocusedBookingId != null) {
            preferred = findFirstRoomForBooking(pendingFocusedBookingId.intValue());
        }
        if (preferred == null && selectedRoomBlock != null) {
            preferred = roomBlocksByCode.get(selectedRoomBlock.roomCode);
        }
        if (preferred != null && visibleRooms.contains(preferred)) {
            handleRoomBlockSelection(preferred, false);
            clearPendingFocusedBookingIfMatched(preferred.bookingId == null ? -1 : preferred.bookingId.intValue());
            return;
        }
        if (!visibleRooms.isEmpty()) {
            handleRoomBlockSelection(visibleRooms.get(0), false);
            return;
        }
        selectedRoomBlock = null;
        highlightedRoomCodes.clear();
        refreshRoomBlockStyles();
        clearOperationDetailPanel();
    }

    private RoomBlockState findFirstRoomForBooking(int bookingId) {
        for (RoomBlockState state : roomBlocksByCode.values()) {
            if (state != null && state.bookingId != null && state.bookingId.intValue() == bookingId) {
                return state;
            }
        }
        return null;
    }

    private JPanel createRoomBlockCard(RoomBlockState state) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setOpaque(true);
        card.setBackground(resolveBlockBackground(state.statusKey));
        card.setBorder(createRoomBlockBorder(state, false));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(ROOM_CARD_PREFERRED_WIDTH, ROOM_CARD_PREFERRED_HEIGHT));
        card.setMinimumSize(new Dimension(0, ROOM_CARD_PREFERRED_HEIGHT));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblRoom = new JLabel(state.roomCode);
        lblRoom.setFont(AppFonts.title(18));
        lblRoom.setForeground(TEXT_PRIMARY);
        lblRoom.setToolTipText(state.roomCode);

        JLabel lblBadge = createRoomBadgeLabel(state);
        top.add(lblRoom, BorderLayout.WEST);
        top.add(lblBadge, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel lblStatus = createRoomBlockLineLabel(safeValue(state.statusText, "-"),
                AppFonts.ui(Font.BOLD, 12), resolveBlockAccent(state.statusKey), 15);
        body.add(lblStatus);
        body.add(Box.createVerticalStrut(2));

        JLabel lblRoomType = createRoomBlockLineLabel(safeValue(state.roomType, "-"),
                AppFonts.body(12), TEXT_MUTED, 16);
        body.add(lblRoomType);
        body.add(Box.createVerticalStrut(2));

        JLabel lblGuest = createRoomBlockLineLabel(resolveRoomGuestDisplay(state),
                AppFonts.ui(Font.BOLD, 12), TEXT_PRIMARY, 17);
        body.add(lblGuest);
        body.add(Box.createVerticalStrut(2));

        JLabel lblMeta = createRoomBlockLineLabel(resolveRoomCompactMetaLine(state),
                AppFonts.body(12), TEXT_MUTED, 20);
        body.add(lblMeta);
        body.add(Box.createVerticalStrut(2));

        JLabel lblFuture = createRoomBlockLineLabel(resolveRoomCardHintDisplay(state),
                AppFonts.body(11), TEXT_MUTED, 18);
        body.add(lblFuture);

        card.setToolTipText(buildRoomBlockTooltip(state));
        bindRoomBlockClick(card, state);
        bindRoomBlockClick(top, state);
        bindRoomBlockClick(body, state);
        bindRoomBlockClick(lblRoom, state);
        bindRoomBlockClick(lblStatus, state);
        bindRoomBlockClick(lblBadge, state);
        bindRoomBlockClick(lblRoomType, state);
        bindRoomBlockClick(lblGuest, state);
        bindRoomBlockClick(lblMeta, state);
        bindRoomBlockClick(lblFuture, state);

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        roomBlockCards.put(state.roomCode, card);
        return card;
    }

    private JLabel createRoomBlockLineLabel(String text, Font font, Color foreground, int maxChars) {
        String safeText = safeValue(text, "-");
        JLabel label = new JLabel(truncateText(safeText, maxChars));
        label.setFont(font);
        label.setForeground(foreground);
        label.setToolTipText(safeText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
        return label;
    }

    private void bindRoomBlockClick(Component component, RoomBlockState state) {
        if (component == null || state == null) {
            return;
        }
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    handleRoomBlockSelection(state, true);
                    return;
                }
                handleRoomBlockSelection(state, false);
            }
        });
    }

    private JLabel createStatusChipLabel(String text, String statusKey) {
        String safeText = safeValue(text, "-");
        JLabel lbl = new JLabel(" " + truncateText(safeText, 16) + " ");
        lbl.setOpaque(true);
        lbl.setBackground(resolveBlockAccent(statusKey));
        lbl.setForeground(RoomStatusKey.MAINTENANCE.equals(statusKey) ? TEXT_PRIMARY : Color.WHITE);
        lbl.setFont(AppFonts.ui(Font.BOLD, 12));
        lbl.setBorder(new EmptyBorder(5, 8, 5, 8));
        lbl.setToolTipText(safeText);
        return lbl;
    }

    private JLabel createRoomBadgeLabel(RoomBlockState state) {
        RoomBadgeConfig badge = resolveRoomBadgeConfig(state);
        if (badge == null) {
            JLabel spacer = new JLabel(" ");
            spacer.setPreferredSize(new Dimension(26, 22));
            return spacer;
        }
        JLabel lbl = new JLabel(badge.text, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(badge.background);
        lbl.setForeground(badge.foreground);
        lbl.setFont(AppFonts.ui(Font.BOLD, 11));
        lbl.setPreferredSize(new Dimension(26, 22));
        lbl.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        lbl.setToolTipText(badge.tooltip);
        return lbl;
    }

    private RoomBadgeConfig resolveRoomBadgeConfig(RoomBlockState state) {
        if (state == null) {
            return null;
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey)) {
            return new RoomBadgeConfig("TT", new Color(220, 38, 38), Color.WHITE, buildRoomBadgeTooltip(state));
        }
        if (RoomStatusKey.CLEANING.equals(state.statusKey)) {
            return new RoomBadgeConfig("DN", new Color(13, 148, 136), Color.WHITE, buildRoomBadgeTooltip(state));
        }
        if (state.futureHintCount > 0 && state.nextFutureHint != null) {
            String text = state.futureHintCount > 1 ? String.valueOf(state.futureHintCount) : "\u2022";
            return new RoomBadgeConfig(text, new Color(17, 24, 39), Color.WHITE, buildRoomBadgeTooltip(state));
        }
        return null;
    }

    private String buildRoomBadgeTooltip(RoomBlockState state) {
        if (state == null) {
            return null;
        }
        StringBuilder html = new StringBuilder("<html>");
        if (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey)) {
            html.append("Ph\u00f2ng ").append(safeValue(state.roomCode, "-")).append(" \u0111ang ch\u1edd thanh to\u00e1n");
            if (state.actualCheckOut != null) {
                html.append("<br>Check-out l\u00fac: ").append(formatDateTime(state.actualCheckOut));
            }
            if (state.bookingCode != null) {
                html.append("<br>Booking: ").append(state.bookingCode);
            }
        } else if (RoomStatusKey.CLEANING.equals(state.statusKey)) {
            html.append("Ph\u00f2ng ").append(safeValue(state.roomCode, "-")).append(" \u0111ang \u1edf tr\u1ea1ng th\u00e1i d\u1ecdn ph\u00f2ng");
        } else if (state.nextFutureHint != null) {
            html.append("S\u1eafp c\u00f3 kh\u00e1ch");
            html.append("<br>Booking g\u1ea7n nh\u1ea5t: DP").append(state.nextFutureHint.bookingId);
            html.append("<br>D\u1ef1 ki\u1ebfn nh\u1eadn: ").append(formatDateTime(state.nextFutureHint.expectedCheckIn));
            html.append("<br>Ng\u01b0\u1eddi \u0111\u1ea1i di\u1ec7n: ").append(safeValue(state.nextFutureHint.representativeName, "-"));
            if (state.futureHintCount > 1) {
                html.append("<br>C\u00f2n ").append(state.futureHintCount - 1).append(" l\u01b0\u1ee3t k\u1ebf ti\u1ebfp.");
            }
        } else {
            return null;
        }
        if (state.nextFutureHint != null
                && (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey) || RoomStatusKey.CLEANING.equals(state.statusKey))) {
            html.append("<br><br>S\u1eafp t\u1edbi: DP").append(state.nextFutureHint.bookingId)
                    .append(" - ").append(formatDateTime(state.nextFutureHint.expectedCheckIn))
                    .append(" - ").append(safeValue(state.nextFutureHint.representativeName, "-"));
        }
        html.append("</html>");
        return html.toString();
    }

    private String buildRoomBlockTooltip(RoomBlockState state) {
        if (state == null) {
            return null;
        }
        StringBuilder html = new StringBuilder("<html>");
        html.append("Ph\u00f2ng: ").append(safeValue(state.roomCode, "-"));
        html.append("<br>Tr\u1ea1ng th\u00e1i: ").append(safeValue(state.statusText, "-"));
        html.append("<br>Lo\u1ea1i ph\u00f2ng: ").append(safeValue(state.roomType, "-"));
        html.append("<br>S\u1ed1 ng\u01b0\u1eddi: ").append(state.guestCount <= 0 ? 0 : state.guestCount);
        if (state.bookingCode != null) {
            html.append("<br>Booking: ").append(state.bookingCode);
        }
        if (state.representativeName != null) {
            html.append("<br>Kh\u00e1ch/đ\u1ea1i di\u1ec7n: ").append(state.representativeName);
        }
        if (state.nextFutureHint != null) {
            html.append("<br>S\u1eafp t\u1edbi: DP").append(state.nextFutureHint.bookingId)
                    .append(" - ").append(formatDateTime(state.nextFutureHint.expectedCheckIn));
        }
        html.append("</html>");
        return html.toString();
    }

    private String resolveRoomCardHint(RoomBlockState state) {
        if (state == null) {
            return "-";
        }
        if (RoomStatusKey.BOOKED.equals(state.statusKey)) {
            return "Ch\u1edd check-in";
        }
        if (RoomStatusKey.OCCUPIED.equals(state.statusKey)) {
            return "Kh\u00e1ch \u0111ang l\u01b0u tr\u00fa";
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey)) {
            return "\u0110\u00e3 check-out, c\u1ea7n thanh to\u00e1n";
        }
        if (RoomStatusKey.CLEANING.equals(state.statusKey)) {
            return "Ch\u1edd x\u00e1c nh\u1eadn d\u1ecdn";
        }
        if (RoomStatusKey.MAINTENANCE.equals(state.statusKey)) {
            return "Kh\u00f4ng tham gia v\u1eadn h\u00e0nh";
        }
        if (RoomStatusKey.AVAILABLE.equals(state.statusKey) && state.nextFutureHint != null) {
            return "S\u1eafp c\u00f3 kh\u00e1ch";
        }
        if (RoomStatusKey.AVAILABLE.equals(state.statusKey)) {
            return "S\u1eb5n s\u00e0ng \u0111\u00f3n kh\u00e1ch";
        }
        return "Kh\u00f4ng c\u00f3 thao t\u00e1c ngay";
    }

    private Map<String, List<RoomBlockState>> groupVisibleRoomsByFloor(List<RoomBlockState> visibleRooms) {
        Map<String, List<RoomBlockState>> roomsByFloor = new LinkedHashMap<String, List<RoomBlockState>>();
        for (String floorName : availableFloors) {
            roomsByFloor.put(floorName, new ArrayList<RoomBlockState>());
        }
        for (RoomBlockState state : visibleRooms) {
            if (state == null) {
                continue;
            }
            if (!roomsByFloor.containsKey(state.floorName)) {
                roomsByFloor.put(state.floorName, new ArrayList<RoomBlockState>());
            }
            roomsByFloor.get(state.floorName).add(state);
        }
        Map<String, List<RoomBlockState>> orderedFloors = new LinkedHashMap<String, List<RoomBlockState>>();
        for (Map.Entry<String, List<RoomBlockState>> entry : roomsByFloor.entrySet()) {
            List<RoomBlockState> floorRooms = entry.getValue();
            if (floorRooms.isEmpty()) {
                continue;
            }
            floorRooms.sort((left, right) -> compareRoomCodes(left == null ? null : left.roomCode, right == null ? null : right.roomCode));
            orderedFloors.put(entry.getKey(), floorRooms);
        }
        return orderedFloors;
    }

    private JPanel createFloorSectionPanel(String floorName, List<RoomBlockState> floorRooms) {
        JPanel section = new JPanel(new BorderLayout(0, 8));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);

        JLabel lblFloor = new JLabel(safeValue(floorName, "T\u1ea7ng"));
        lblFloor.setFont(AppFonts.ui(Font.BOLD, 13));
        lblFloor.setForeground(TEXT_PRIMARY);
        titleRow.add(lblFloor, BorderLayout.WEST);

        JLabel lblCount = new JLabel((floorRooms == null ? 0 : floorRooms.size()) + " ph\u00f2ng");
        lblCount.setFont(AppFonts.body(12));
        lblCount.setForeground(TEXT_MUTED);
        titleRow.add(lblCount, BorderLayout.EAST);
        section.add(titleRow, BorderLayout.NORTH);

        JPanel row = new JPanel(new ResponsiveRoomGridLayout(
                ROOM_GRID_COLUMNS,
                ROOM_CARD_PREFERRED_WIDTH,
                ROOM_CARD_PREFERRED_HEIGHT,
                ROOM_GRID_GAP,
                ROOM_GRID_GAP
        ));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (floorRooms != null) {
            for (RoomBlockState state : floorRooms) {
                row.add(createRoomBlockCard(state));
            }
        }
        section.add(row, BorderLayout.CENTER);
        return section;
    }

    private int compareRoomCodes(String left, String right) {
        int leftNumeric = extractRoomCodeNumber(left);
        int rightNumeric = extractRoomCodeNumber(right);
        if (leftNumeric != rightNumeric) {
            return Integer.compare(leftNumeric, rightNumeric);
        }
        return safeValue(left, "").compareToIgnoreCase(safeValue(right, ""));
    }

    private int extractRoomCodeNumber(String roomCode) {
        if (roomCode == null) {
            return Integer.MAX_VALUE;
        }
        String digits = roomCode.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private String resolveRoomGuestDisplay(RoomBlockState state) {
        if (state == null) {
            return "Kh\u00f4ng c\u00f3 th\u00f4ng tin kh\u00e1ch";
        }
        if (state.representativeName != null && !state.representativeName.trim().isEmpty() && !"-".equals(state.representativeName.trim())) {
            return state.representativeName;
        }
        if (RoomStatusKey.AVAILABLE.equals(state.statusKey)) {
            return "Ch\u01b0a c\u00f3 kh\u00e1ch";
        }
        if (RoomStatusKey.CLEANING.equals(state.statusKey)) {
            return "Kh\u00f4ng c\u00f2n kh\u00e1ch l\u01b0u tr\u00fa";
        }
        return "Ch\u01b0a c\u00f3 t\u00ean kh\u00e1ch";
    }

    private String resolveRoomPeopleDisplay(RoomBlockState state) {
        if (state == null) {
            return "0 ng\u01b0\u1eddi";
        }
        int guestCount = Math.max(0, state.guestCount);
        if (guestCount <= 0) {
            return RoomStatusKey.AVAILABLE.equals(state.statusKey) ? "0 ng\u01b0\u1eddi" : "Ch\u01b0a ghi nh\u1eadn s\u1ed1 ng\u01b0\u1eddi";
        }
        return guestCount + " ng\u01b0\u1eddi";
    }

    private String resolveRoomSecondaryLine(RoomBlockState state) {
        if (state == null) {
            return "-";
        }
        if (state.bookingCode != null) {
            return state.bookingCode + " | " + safeValue(state.floorName, "-");
        }
        return safeValue(state.roomType, "-") + " | " + safeValue(state.floorName, "-");
    }

    private String resolveRoomCompactMetaLine(RoomBlockState state) {
        if (state == null) {
            return "0 ng\u01b0\u1eddi | -";
        }
        String people = Math.max(0, state.guestCount) + " ng\u01b0\u1eddi";
        String floor = compactFloorName(state.floorName);
        if (state.bookingCode != null && !state.bookingCode.trim().isEmpty()) {
            return people + " | " + state.bookingCode + " | " + floor;
        }
        return people + " | " + floor;
    }

    private String compactFloorName(String floorName) {
        String value = safeValue(floorName, "-");
        String digits = value.replaceAll("\\D+", "");
        if (!digits.isEmpty()) {
            return "T" + digits;
        }
        return truncateText(value, 4);
    }

    private String resolveRoomCardHintDisplay(RoomBlockState state) {
        if (state == null) {
            return "-";
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey)) {
            return "C\u1ea7n thanh to\u00e1n";
        }
        if (RoomStatusKey.MAINTENANCE.equals(state.statusKey)) {
            return "B\u1ea3o tr\u00ec";
        }
        if (RoomStatusKey.AVAILABLE.equals(state.statusKey) && state.nextFutureHint == null) {
            return "S\u1eb5n s\u00e0ng";
        }
        return resolveRoomCardHint(state);
    }

    private void handleRoomBlockSelection(RoomBlockState state, boolean openAction) {
        if (state == null) {
            selectedRoomBlock = null;
            highlightedRoomCodes.clear();
            refreshRoomBlockStyles();
            clearOperationDetailPanel();
            return;
        }
        selectedRoomBlock = state;
        highlightBookingRooms(state);
        refreshRoomBlockStyles();
        updateOperationDetailPanel(state);
        if (openAction) {
            openRoomActionForState(state);
        }
    }

    private void highlightBookingRooms(RoomBlockState state) {
        highlightedRoomCodes.clear();
        if (state == null) {
            return;
        }
        StayRecord record = state.bookingId == null ? null : findRecordByBookingId(state.bookingId);
        if (record != null) {
            for (RoomBlockState related : record.resolveRoomStates(roomBlocksByCode)) {
                if (related != null) {
                    highlightedRoomCodes.add(related.roomCode);
                }
            }
        }
        if (highlightedRoomCodes.isEmpty()) {
            highlightedRoomCodes.add(state.roomCode);
        }
    }

    private void refreshRoomBlockStyles() {
        for (Map.Entry<String, JPanel> entry : roomBlockCards.entrySet()) {
            RoomBlockState state = roomBlocksByCode.get(entry.getKey());
            JPanel card = entry.getValue();
            if (card == null || state == null) {
                continue;
            }
            boolean selected = selectedRoomBlock != null && state.roomCode.equalsIgnoreCase(selectedRoomBlock.roomCode);
            card.setBorder(createRoomBlockBorder(state, selected));
            card.repaint();
        }
    }

    private javax.swing.border.Border createRoomBlockBorder(RoomBlockState state, boolean selected) {
        boolean related = state != null && highlightedRoomCodes.contains(state.roomCode);
        Color accent = selected ? new Color(37, 99, 235) : related ? new Color(59, 130, 246) : BORDER_SOFT;
        int thickness = selected ? 3 : related ? 2 : 1;
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, thickness, true),
                new EmptyBorder(7, 8, 7, 8)
        );
    }

    private void updateOperationDetailPanel(RoomBlockState state) {
        if (state == null) {
            clearOperationDetailPanel();
            return;
        }
        StayRecord record = state.bookingId == null ? null : findRecordByBookingId(state.bookingId);
        setDetailValue(lblDetailBookingCode, state.bookingCode == null ? "-" : state.bookingCode, 24);
        setDetailValue(lblDetailStayCode, state.getStayCodeDisplay(), 24);
        setDetailValue(lblDetailGuestName, safeValue(state.representativeName, record == null ? "-" : record.khachHang), 30);
        setDetailValue(lblDetailRoomCode, state.roomCode, 24);
        setDetailValue(lblDetailRoomType, safeValue(state.roomType, "-"), 28);
        setDetailValue(lblDetailFloor, safeValue(state.floorName, "-"), 24);
        setDetailValue(lblDetailStatus, safeValue(state.statusText, "-"), 28);
        setDetailValue(lblDetailGuestCount, resolveRoomPeopleDisplay(state), 26);
        setDetailValue(lblDetailCheckIn, formatDateTime(state.checkInTime == null && record != null ? parseDateTimeOrNull(record.gioVao) : state.checkInTime), 28);
        setDetailValue(lblDetailCheckOut, formatDateTime(state.expectedCheckOut == null && record != null ? parseDateTimeOrNull(record.gioRaDuKien) : state.expectedCheckOut), 28);
        setDetailValue(lblDetailNextBooking, buildNextBookingSummary(state), 36);
        String detailNotes = buildOperationNote(state, record);
        txtDetailNotes.setText(detailNotes);
        txtDetailNotes.setToolTipText(detailNotes);
        txtDetailNotes.setCaretPosition(0);
        rebuildRelatedRoomPanel(state, record);
        rebuildDetailActionPanel(state, record);
    }

    private void setDetailValue(JLabel label, String value, int maxChars) {
        if (label == null) {
            return;
        }
        String safeText = safeValue(value, "-");
        label.setText(truncateText(safeText, maxChars));
        label.setToolTipText(safeText);
    }

    private void clearOperationDetailPanel() {
        if (lblDetailBookingCode == null) {
            return;
        }
        setDetailValue(lblDetailBookingCode, "-", 24);
        setDetailValue(lblDetailStayCode, "-", 24);
        setDetailValue(lblDetailGuestName, "-", 30);
        setDetailValue(lblDetailRoomCode, "-", 24);
        setDetailValue(lblDetailRoomType, "-", 28);
        setDetailValue(lblDetailFloor, "-", 24);
        setDetailValue(lblDetailStatus, "-", 28);
        setDetailValue(lblDetailGuestCount, "-", 26);
        setDetailValue(lblDetailCheckIn, "-", 28);
        setDetailValue(lblDetailCheckOut, "-", 28);
        setDetailValue(lblDetailNextBooking, "-", 36);
        if (txtDetailNotes != null) {
            String emptyNote = "Ch\u1ecdn m\u1ed9t block ph\u00f2ng \u0111\u1ec3 xem chi ti\u1ebft v\u00e0 thao t\u00e1c.";
            txtDetailNotes.setText(emptyNote);
            txtDetailNotes.setToolTipText(emptyNote);
        }
        if (detailRelatedRoomsPanel != null) {
            detailRelatedRoomsPanel.removeAll();
            JLabel lbl = new JLabel("Ch\u01b0a c\u00f3 booking \u0111\u01b0\u1ee3c ch\u1ecdn.");
            lbl.setFont(BODY_FONT);
            lbl.setForeground(TEXT_MUTED);
            detailRelatedRoomsPanel.add(lbl);
            detailRelatedRoomsPanel.revalidate();
            detailRelatedRoomsPanel.repaint();
        }
        if (detailActionPanel != null) {
            detailActionPanel.removeAll();
            JLabel lbl = new JLabel("Double click block ph\u00f2ng \u0111\u1ec3 m\u1edf thao t\u00e1c.");
            lbl.setFont(BODY_FONT);
            lbl.setForeground(TEXT_MUTED);
            detailActionPanel.add(lbl);
            detailActionPanel.revalidate();
            detailActionPanel.repaint();
        }
    }

    private String buildNextBookingSummary(RoomBlockState state) {
        if (state == null || state.nextFutureHint == null) {
            return "-";
        }
        return "DP" + state.nextFutureHint.bookingId + " | " + formatDateTime(state.nextFutureHint.expectedCheckIn);
    }

    private String buildOperationNote(RoomBlockState state, StayRecord record) {
        if (state == null) {
            return "Ch\u01b0a c\u00f3 d\u1eef li\u1ec7u.";
        }
        StringBuilder note = new StringBuilder();
        note.append(safeValue(state.note, "Kh\u00f4ng c\u00f3 ghi ch\u00fa v\u1eadn h\u00e0nh."));
        note.append("\nKh\u00e1ch hi\u1ec7n t\u1ea1i: ").append(resolveRoomGuestDisplay(state));
        note.append("\nS\u1ed1 ng\u01b0\u1eddi: ").append(resolveRoomPeopleDisplay(state));
        if (record != null) {
            note.append("\n\nBooking c\u00f3 ").append(record.soLuongPhong).append(" ph\u00f2ng.");
            int pendingCount = record.countRoomsByStatus(roomBlocksByCode, RoomStatusKey.BOOKED);
            int occupiedCount = record.countRoomsByStatus(roomBlocksByCode, RoomStatusKey.OCCUPIED);
            int waitPaymentCount = record.countRoomsByStatus(roomBlocksByCode, RoomStatusKey.WAIT_PAYMENT);
            int cleaningCount = record.countRoomsByStatus(roomBlocksByCode, RoomStatusKey.CLEANING);
            if (pendingCount > 0) {
                note.append("\n- ").append(pendingCount).append(" ph\u00f2ng c\u00f2n ch\u1edd check-in.");
            }
            if (occupiedCount > 0) {
                note.append("\n- ").append(occupiedCount).append(" ph\u00f2ng \u0111ang \u1edf.");
            }
            if (waitPaymentCount > 0) {
                note.append("\n- ").append(waitPaymentCount).append(" ph\u00f2ng \u0111ang ch\u1edd thanh to\u00e1n.");
            }
            if (cleaningCount > 0) {
                note.append("\n- ").append(cleaningCount).append(" ph\u00f2ng \u0111ang d\u1ecdn.");
            }
        }
        if (state.nextFutureHint != null) {
            note.append("\n\nS\u1eafp t\u1edbi: ").append("DP").append(state.nextFutureHint.bookingId)
                    .append(" - ").append(formatDateTime(state.nextFutureHint.expectedCheckIn))
                    .append(" - ").append(safeValue(state.nextFutureHint.representativeName, "-"));
        }
        return note.toString();
    }

    private void rebuildRelatedRoomPanel(RoomBlockState state, StayRecord record) {
        detailRelatedRoomsPanel.removeAll();
        if (record == null || state.bookingId == null) {
            detailRelatedRoomsPanel.add(createRelatedRoomCard(state, true));
            detailRelatedRoomsPanel.revalidate();
            detailRelatedRoomsPanel.repaint();
            return;
        }
        for (String roomCode : record.getRoomCodes()) {
            RoomBlockState related = roomBlocksByCode.get(roomCode);
            detailRelatedRoomsPanel.add(createRelatedRoomCard(related,
                    selectedRoomBlock != null && related != null && roomCode.equalsIgnoreCase(selectedRoomBlock.roomCode)));
            detailRelatedRoomsPanel.add(Box.createVerticalStrut(6));
        }
        detailRelatedRoomsPanel.revalidate();
        detailRelatedRoomsPanel.repaint();
    }

    private JPanel createRelatedRoomCard(RoomBlockState roomState, boolean selected) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(true);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMinimumSize(new Dimension(0, 0));
        card.setBackground(selected ? new Color(239, 246, 255) : PANEL_SOFT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? new Color(59, 130, 246) : BORDER_SOFT, selected ? 2 : 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        if (roomState == null) {
            String message = "Ph\u00f2ng trong \u0111\u01a1n kh\u00f4ng c\u00f2n d\u1eef li\u1ec7u tr\u1ea1ng th\u00e1i.";
            JLabel lbl = new JLabel(truncateText(message, 42));
            lbl.setFont(BODY_FONT);
            lbl.setForeground(TEXT_MUTED);
            lbl.setToolTipText(message);
            card.add(lbl, BorderLayout.CENTER);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
            return card;
        }

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblRoom = new JLabel(roomState.roomCode);
        lblRoom.setFont(AppFonts.ui(Font.BOLD, 13));
        lblRoom.setForeground(TEXT_PRIMARY);
        lblRoom.setToolTipText(roomState.roomCode);
        top.add(lblRoom, BorderLayout.WEST);
        top.add(createStatusChipLabel(roomState.statusText, roomState.statusKey), BorderLayout.EAST);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, top.getPreferredSize().height));

        String guestText = resolveRoomGuestDisplay(roomState);
        JLabel lblGuest = new JLabel(truncateText(guestText, 28));
        lblGuest.setFont(BODY_FONT);
        lblGuest.setForeground(TEXT_PRIMARY);
        lblGuest.setToolTipText(guestText);
        lblGuest.setAlignmentX(Component.LEFT_ALIGNMENT);

        String peopleText = resolveRoomPeopleDisplay(roomState) + " | " + safeValue(roomState.roomType, "-");
        JLabel lblPeople = new JLabel(truncateText(peopleText, 32));
        lblPeople.setFont(AppFonts.body(12));
        lblPeople.setForeground(TEXT_MUTED);
        lblPeople.setToolTipText(peopleText);
        lblPeople.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(top);
        info.add(Box.createVerticalStrut(4));
        info.add(lblGuest);
        info.add(Box.createVerticalStrut(2));
        info.add(lblPeople);

        JButton btnAction = createRelatedRoomActionButton(roomState);
        card.add(info, BorderLayout.CENTER);
        if (btnAction != null) {
            card.add(btnAction, BorderLayout.SOUTH);
        }
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        bindRoomBlockClick(card, roomState);
        bindRoomBlockClick(info, roomState);
        bindRoomBlockClick(top, roomState);
        bindRoomBlockClick(lblRoom, roomState);
        bindRoomBlockClick(lblGuest, roomState);
        bindRoomBlockClick(lblPeople, roomState);
        return card;
    }

    private JButton createRelatedRoomActionButton(RoomBlockState state) {
        if (state == null) {
            return null;
        }
        String label = resolveQuickActionLabel(state);
        if (label == null) {
            return null;
        }
        JButton button = createOutlineButton(label, resolveBlockAccent(state.statusKey), e -> {
            handleRoomBlockSelection(state, false);
            openRoomActionForState(state);
        });
        button.setFont(AppFonts.ui(Font.BOLD, 12));
        button.setMargin(new Insets(6, 10, 6, 10));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        button.setMinimumSize(new Dimension(0, 34));
        button.setPreferredSize(new Dimension(1, 34));
        button.setToolTipText(label);
        return button;
    }

    private String resolveQuickActionLabel(RoomBlockState state) {
        if (state == null) {
            return null;
        }
        if (RoomStatusKey.BOOKED.equals(state.statusKey)) {
            return "Check-in";
        }
        if (RoomStatusKey.OCCUPIED.equals(state.statusKey)) {
            return "Chi ti\u1ebft";
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey)) {
            return "Thanh to\u00e1n";
        }
        if (RoomStatusKey.CLEANING.equals(state.statusKey)) {
            return "D\u1ecdn xong";
        }
        return null;
    }

    private void rebuildDetailActionPanel(RoomBlockState state, StayRecord record) {
        detailActionPanel.removeAll();
        if (state == null) {
            detailActionPanel.revalidate();
            detailActionPanel.repaint();
            return;
        }

        JLabel lblSelectedRoom = new JLabel("Ph\u00f2ng \u0111ang ch\u1ecdn");
        lblSelectedRoom.setFont(LABEL_FONT);
        lblSelectedRoom.setForeground(TEXT_MUTED);
        lblSelectedRoom.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSelectedRoom.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblSelectedRoom.getPreferredSize().height));
        detailActionPanel.add(lblSelectedRoom);
        detailActionPanel.add(Box.createVerticalStrut(8));

        switch (state.statusKey) {
            case RoomStatusKey.BOOKED:
                addDetailActionButton(createPrimaryButton("Check-in ph\u00f2ng \u0111ang ch\u1ecdn", new Color(22, 163, 74), Color.WHITE,
                        e -> openCheckInDialog(record, state.detailId, record != null && record.hasMultipleRooms())));
                break;
            case RoomStatusKey.OCCUPIED:
                addDetailActionButton(createPrimaryButton("M\u1edf popup \u0111ang \u1edf", new Color(37, 99, 235), Color.WHITE, e -> openRoomActionForState(state)));
                addDetailActionButton(createOutlineButton("Th\u00eam d\u1ecbch v\u1ee5", new Color(37, 99, 235), e -> {
                    handleRoomBlockSelection(state, false);
                    openAddServiceDialog();
                }));
                addDetailActionButton(createOutlineButton("\u0110\u1ed5i ph\u00f2ng", new Color(245, 158, 11), e -> openChangeRoomDialog(record, state.stayId)));
                addDetailActionButton(createOutlineButton("Gia h\u1ea1n", new Color(59, 130, 246), e -> {
                    handleRoomBlockSelection(state, false);
                    openExtendDialog();
                }));
                addDetailActionButton(createOutlineButton("Check-out ph\u00f2ng n\u00e0y", new Color(220, 38, 38), e -> openCheckOutDialog(record, state.stayId, false)));
                break;
            case RoomStatusKey.WAIT_PAYMENT:
                addDetailActionButton(createPrimaryButton("M\u1edf thanh to\u00e1n", new Color(220, 38, 38), Color.WHITE, e -> openRoomActionForState(state)));
                break;
            case RoomStatusKey.CLEANING:
                addDetailActionButton(createPrimaryButton("X\u00e1c nh\u1eadn d\u1ecdn xong", new Color(13, 148, 136), Color.WHITE, e -> confirmCleaningForState(state)));
                break;
            default:
                JLabel lbl = new JLabel("Kh\u00f4ng c\u00f3 thao t\u00e1c tr\u1ef1c ti\u1ebfp cho tr\u1ea1ng th\u00e1i n\u00e0y.");
                lbl.setFont(BODY_FONT);
                lbl.setForeground(TEXT_MUTED);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));
                lbl.setToolTipText(lbl.getText());
                detailActionPanel.add(lbl);
                break;
        }

        if (record != null && record.hasMultipleRooms()) {
            int bookableCount = countBookingRoomsByStatus(record.maDatPhong, RoomStatusKey.BOOKED);
            int occupiedCount = countBookingRoomsByStatus(record.maDatPhong, RoomStatusKey.OCCUPIED);
            if (bookableCount > 0 || occupiedCount > 0) {
                detailActionPanel.add(Box.createVerticalStrut(4));
                JLabel lblBulk = new JLabel("To\u00e0n b\u1ed9 \u0111\u01a1n");
                lblBulk.setFont(LABEL_FONT);
                lblBulk.setForeground(TEXT_MUTED);
                lblBulk.setAlignmentX(Component.LEFT_ALIGNMENT);
                lblBulk.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblBulk.getPreferredSize().height));
                detailActionPanel.add(lblBulk);
                detailActionPanel.add(Box.createVerticalStrut(8));
            }
            if (bookableCount > 0) {
                addDetailActionButton(createOutlineButton("Check-in to\u00e0n b\u1ed9 \u0111\u01a1n", new Color(22, 163, 74), e -> {
                    handleRoomBlockSelection(state, false);
                    openCheckInDialog(record, state.detailId, true);
                }));
            }
            if (occupiedCount > 0) {
                addDetailActionButton(createOutlineButton("Check-out to\u00e0n b\u1ed9 \u0111\u01a1n", new Color(220, 38, 38), e -> {
                    handleRoomBlockSelection(state, false);
                    openCheckOutDialog(record, state.stayId, true);
                }));
            }
        }

        detailActionPanel.revalidate();
        detailActionPanel.repaint();
    }

    private void addDetailActionButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setMinimumSize(new Dimension(0, 38));
        button.setPreferredSize(new Dimension(1, 38));
        button.setToolTipText(button.getText());
        detailActionPanel.add(button);
        detailActionPanel.add(Box.createVerticalStrut(8));
    }

    private int countBookingRoomsByStatus(int bookingId, String statusKey) {
        StayRecord record = findRecordByBookingId(Integer.valueOf(bookingId));
        if (record != null) {
            return record.countRoomsByStatus(roomBlocksByCode, statusKey);
        }
        int count = 0;
        for (RoomBlockState state : roomBlocksByCode.values()) {
            if (state.bookingId != null && state.bookingId.intValue() == bookingId && statusKey.equals(state.statusKey)) {
                count++;
            }
        }
        return count;
    }

    private StayRecord findRecordByBookingId(Integer bookingId) {
        if (bookingId == null || bookingId.intValue() <= 0) {
            return null;
        }
        return recordsByBookingId.get(bookingId);
    }

    private void openRoomActionForState(RoomBlockState state) {
        if (state == null) {
            return;
        }
        StayRecord record = state.bookingId == null ? null : findRecordByBookingId(state.bookingId);
        if (RoomStatusKey.BOOKED.equals(state.statusKey)) {
            openCheckInDialog(record, state.detailId, record != null && record.hasMultipleRooms());
            return;
        }
        if (RoomStatusKey.OCCUPIED.equals(state.statusKey)) {
            if (record == null || state.stayId == null) {
                showInfo("Kh\u00f4ng c\u00f2n d\u1eef li\u1ec7u l\u01b0u tr\u00fa \u0111\u1ec3 m\u1edf popup \u0111ang \u1edf.");
                return;
            }
            new ActiveStayOperationDialog(this, state, record).setVisible(true);
            return;
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(state.statusKey)) {
            openPaymentPopupForState(state);
            return;
        }
        if (RoomStatusKey.CLEANING.equals(state.statusKey)) {
            confirmCleaningForState(state);
            return;
        }
        showInfo("Tr\u1ea1ng th\u00e1i ph\u00f2ng n\u00e0y kh\u00f4ng c\u00f3 thao t\u00e1c nhanh.");
    }

    private void openPaymentPopupForState(RoomBlockState state) {
        if (state == null || state.bookingId == null) {
            showInfo("Kh\u00f4ng t\u00ecm th\u1ea5y booking ch\u1edd thanh to\u00e1n cho ph\u00f2ng n\u00e0y.");
            return;
        }
        String invoiceId = resolveInvoiceIdForState(state);
        if (invoiceId == null) {
            refreshRoomStatusAfterMissingPaymentInvoice(state);
            showInfo("Ph\u00f2ng n\u00e0y kh\u00f4ng c\u00f2n h\u00f3a \u0111\u01a1n ch\u1edd thanh to\u00e1n. D\u1eef li\u1ec7u tr\u1ea1ng th\u00e1i ph\u00f2ng s\u1ebd \u0111\u01b0\u1ee3c l\u00e0m m\u1edbi.");
            return;
        }
        ThanhToanGUI.requestInvoiceFocus(invoiceId);
        NavigationUtil.navigate(CheckInOutGUI.this, ScreenKey.CHECK_IN_OUT, ScreenKey.THANH_TOAN, username, role);
    }

    private String resolveInvoiceIdForState(RoomBlockState state) {
        if (state == null) {
            return null;
        }
        if (state.invoiceId != null && state.invoiceId.intValue() > 0) {
            String invoiceId = String.valueOf(state.invoiceId);
            if (isInvoiceAwaitingPayment(invoiceId)) {
                return invoiceId;
            }
        }
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }
        if (state.detailId != null && state.detailId.intValue() > 0) {
            String invoiceId = resolveAwaitingInvoiceByDetail(con, state.detailId.intValue());
            if (invoiceId != null) {
                return invoiceId;
            }
        }
        return state.bookingId == null || state.bookingId.intValue() <= 0
                ? null
                : resolveAwaitingInvoiceByBooking(con, state.bookingId.intValue());
    }

    private String resolveAwaitingInvoiceByDetail(Connection con, int detailId) {
        String sql = "SELECT TOP 1 maHoaDon FROM HoaDon "
                + "WHERE maChiTietDatPhong = ? "
                + "AND ISNULL(trangThai, N'Ch\u1edd thanh to\u00e1n') NOT IN (N'\u0110\u00e3 thanh to\u00e1n', N'\u0110\u00e3 ho\u00e0n c\u1ecdc') "
                + "ORDER BY CASE WHEN ISNULL(trangThai, N'Ch\u1edd thanh to\u00e1n') = N'Ch\u1edd thanh to\u00e1n' THEN 0 ELSE 1 END, maHoaDon DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, detailId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? String.valueOf(rs.getInt("maHoaDon")) : null;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private String resolveAwaitingInvoiceByBooking(Connection con, int bookingId) {
        String sql = "SELECT TOP 1 maHoaDon FROM HoaDon "
                + "WHERE maDatPhong = ? AND maChiTietDatPhong IS NULL "
                + "AND ISNULL(trangThai, N'Ch\u1edd thanh to\u00e1n') NOT IN (N'\u0110\u00e3 thanh to\u00e1n', N'\u0110\u00e3 ho\u00e0n c\u1ecdc') "
                + "ORDER BY CASE WHEN ISNULL(trangThai, N'Ch\u1edd thanh to\u00e1n') = N'Ch\u1edd thanh to\u00e1n' THEN 0 ELSE 1 END, maHoaDon DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? String.valueOf(rs.getInt("maHoaDon")) : null;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean isInvoiceAwaitingPayment(String invoiceId) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            return false;
        }
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ISNULL(trangThai, N'Ch\u1edd thanh to\u00e1n') AS trangThai FROM HoaDon WHERE maHoaDon = ?")) {
            ps.setInt(1, Integer.parseInt(invoiceId.trim()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = safeValue(rs.getString("trangThai"), "Ch\u1edd thanh to\u00e1n");
                    return !isCompletedInvoiceStatus(status);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isCompletedInvoiceStatus(String status) {
        String safeStatus = safeValue(status, "");
        return "\u0110\u00e3 thanh to\u00e1n".equalsIgnoreCase(safeStatus)
                || "\u0110\u00e3 ho\u00e0n c\u1ecdc".equalsIgnoreCase(safeStatus);
    }

    private void refreshRoomStatusAfterMissingPaymentInvoice(RoomBlockState state) {
        if (state == null) {
            return;
        }
        Connection con = ConnectDB.getConnection();
        if (con != null) {
            try {
                datPhongDAO.refreshRoomStatus(con, state.roomId);
                PhongGUI.refreshAllOpenInstances();
                DatPhongGUI.refreshAllOpenInstances();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        reloadSampleData(false);
    }

    private void confirmCleaningForState(RoomBlockState state) {
        if (state == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "X\u00e1c nh\u1eadn ph\u00f2ng " + state.roomCode + " \u0111\u00e3 d\u1ecdn xong v\u00e0 chuy\u1ec3n sang S\u1eb5n s\u00e0ng?",
                "X\u00e1c nh\u1eadn d\u1ecdn ph\u00f2ng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            showInfo("Kh\u00f4ng th\u1ec3 k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u.");
            return;
        }
        try {
            try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET trangThai = N'S\u1eb5n s\u00e0ng' WHERE maPhong = ?")) {
                ps.setInt(1, state.roomId);
                ps.executeUpdate();
            }
            datPhongDAO.refreshRoomStatus(con, state.roomId);
            PhongGUI.refreshAllOpenInstances();
            DatPhongGUI.refreshAllOpenInstances();
            CheckInOutGUI.refreshAllOpenInstances();
            showInfo("Ph\u00f2ng " + state.roomCode + " \u0111\u00e3 chuy\u1ec3n sang S\u1eb5n s\u00e0ng.");
        } catch (Exception e) {
            e.printStackTrace();
            showInfo("Kh\u00f4ng th\u1ec3 x\u00e1c nh\u1eadn d\u1ecdn ph\u00f2ng.");
        }
    }

    private LocalDateTime parseDateTimeOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "-".equals(trimmed) || "Kh\u00e1c nhau".equalsIgnoreCase(trimmed)) {
            return null;
        }
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return DATE_FORMAT.format(value.toLocalDate()) + " " + TIME_FORMAT.format(value.toLocalTime());
    }

    private StayRecord getSelectedRecord() {
        if (selectedRoomBlock != null && selectedRoomBlock.bookingId != null) {
            StayRecord record = findRecordByBookingId(selectedRoomBlock.bookingId);
            if (record != null) {
                return record;
            }
        }
        int row = tblLuuTru == null ? -1 : tblLuuTru.getSelectedRow();
        if (row >= 0 && row < filteredRecords.size()) {
            return filteredRecords.get(row);
        }
        showInfo("Vui l\u00f2ng ch\u1ecdn m\u1ed9t block ph\u00f2ng c\u00f3 booking.");
        return null;
    }

    private void openCheckInDialog() {
        openCheckInDialog(getSelectedRecord(), selectedRoomBlock == null ? null : selectedRoomBlock.detailId, false);
    }

    private void openCheckInDialog(StayRecord record, Integer preferredDetailId, boolean allowBulk) {
        if (record == null) {
            return;
        }
        if (!record.hasPendingCheckInRooms) {
            showInfo("\u0110\u01a1n n\u00e0y kh\u00f4ng c\u00f2n ph\u00f2ng n\u00e0o ch\u1edd check-in.");
            return;
        }
        new CheckInDialog(this, record, preferredDetailId, allowBulk).setVisible(true);
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
        openChangeRoomDialog(getSelectedRecord(), selectedRoomBlock == null ? null : selectedRoomBlock.stayId);
    }

    private void openChangeRoomDialog(StayRecord record, Integer preferredStayId) {
        if (record == null) {
            return;
        }
        if (!record.hasActiveStayRooms) {
            showInfo("Ch\u1ec9 booking c\u00f2n ph\u00f2ng \u0111ang \u1edf m\u1edbi \u0111\u1ed5i ph\u00f2ng.");
            return;
        }
        new ChangeRoomDialog(this, record, preferredStayId).setVisible(true);
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
        openCheckOutDialog(getSelectedRecord(), selectedRoomBlock == null ? null : selectedRoomBlock.stayId, false);
    }

    private void openCheckOutDialog(StayRecord record, Integer preferredStayId, boolean allowBulk) {
        if (record == null) {
            return;
        }
        if (!record.hasActiveStayRooms) {
            showInfo("Ch\u1ec9 booking c\u00f2n ph\u00f2ng \u0111ang \u1edf m\u1edbi check-out.");
            return;
        }
        new CheckOutDialog(this, record, preferredStayId, allowBulk).setVisible(true);
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

    private String truncateText(String value, int maxChars) {
        String safeText = safeValue(value, "-");
        if (maxChars <= 3 || safeText.length() <= maxChars) {
            return safeText;
        }
        return safeText.substring(0, maxChars - 3) + "...";
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
        private final JComboBox<String> cboCheckInBasis;
        private final Integer preferredDetailId;
        private final boolean allowBulkActions;
        private final Map<Integer, LocalDateTime> scheduledCheckInByDetailId = new LinkedHashMap<Integer, LocalDateTime>();
        private final Map<Integer, LocalDateTime> scheduledCheckOutByDetailId = new LinkedHashMap<Integer, LocalDateTime>();
        private boolean updatingCustomerCells;
        private boolean updatingScheduleFields;

        private CheckInDialog(Frame owner, StayRecord record, Integer preferredDetailId, boolean allowBulkActions) {
            super(owner, "Check-in", 820, 540);
            this.record = record;
            this.preferredDetailId = preferredDetailId;
            this.allowBulkActions = allowBulkActions;
            bookingItems.addAll(checkInOutDAO.getBookingCheckInItems(String.valueOf(record.maDatPhong)));
            initializeBookingItemSchedules();

            LocalDateTime initialCheckIn = resolveDefaultCheckIn();
            LocalDateTime initialCheckOut = resolveDefaultCheckOut(initialCheckIn);
            txtNgayVao = new AppDatePickerField(initialCheckIn.toLocalDate().format(DATE_FORMAT), true);
            txtGioVao = new AppTimePickerField(initialCheckIn.toLocalTime().format(TIME_FORMAT), true);
            txtNgayRa = new AppDatePickerField(initialCheckOut.toLocalDate().format(DATE_FORMAT), true);
            txtGioRa = new AppTimePickerField(initialCheckOut.toLocalTime().format(TIME_FORMAT), true);
            cboCheckInBasis = createComboBox(new String[]{
                    "Gi\u1eef l\u1ecbch t\u1eebng ph\u00f2ng",
                    "Check-in theo hi\u1ec7n t\u1ea1i",
                    "Check-in theo gi\u1edd d\u1ef1 ki\u1ebfn"
            });
            cboCheckInBasis.setPreferredSize(new Dimension(240, 34));
            cboCheckInBasis.setMaximumSize(new Dimension(280, 34));

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHECK-IN",
                    allowBulkActions
                            ? "Ch\u1ecdn t\u1eebng ph\u00f2ng \u0111\u1ec3 check-in ri\u00eang ho\u1eb7c d\u00f9ng thao t\u00e1c check-in to\u00e0n b\u1ed9 \u0111\u01a1n cho c\u00e1c ph\u00f2ng \u0111ang ch\u1edd."
                            : "Gi\u1eef \u0111\u00fang nghi\u1ec7p v\u1ee5 check-in c\u0169, b\u1eaft bu\u1ed9c nh\u1eadp th\u00f4ng tin ng\u01b0\u1eddi \u1edf cho ph\u00f2ng \u0111ang \u0111\u01b0\u1ee3c ch\u1ecdn."
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
            addFormRow(form, gbc, 8, "C\u01a1 ch\u1ebf check-in", cboCheckInBasis);

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
            JLabel lblRoomHint = new JLabel("Khung ng\u00e0y/gi\u1edd ph\u00eda tr\u00ean \u0111ang ch\u1ec9nh cho ph\u00f2ng \u0111ang ch\u1ecdn. Bulk action v\u1eabn t\u00f4n tr\u1ecdng ph\u00f2ng \u0111\u00e3 x\u1eed l\u00fd tr\u01b0\u1edbc.");
            lblRoomHint.setFont(AppFonts.body(12));
            lblRoomHint.setForeground(TEXT_MUTED);
            roomHeader.add(lblRoomHint);
            roomPanel.add(roomHeader, BorderLayout.NORTH);
            roomPanel.add(new JScrollPane(tblRooms), BorderLayout.CENTER);
            card.add(roomPanel, BorderLayout.CENTER);

            content.add(card, BorderLayout.CENTER);

            JButton btnCheckInSelected = createPrimaryButton(
                    allowBulkActions ? "Check-in ph\u00f2ng \u0111\u00e3 ch\u1ecdn" : "Check-in",
                    new Color(22, 163, 74),
                    Color.WHITE,
                    e -> submit(false)
            );
            JButton btnCancel = createOutlineButton("H\u1ee7y", new Color(107, 114, 128), e -> dispose());
            if (allowBulkActions) {
                JButton btnCheckInAll = createPrimaryButton(
                        "Check-in to\u00e0n b\u1ed9 \u0111\u01a1n",
                        new Color(21, 128, 61),
                        Color.WHITE,
                        e -> submit(true)
                );
                content.add(buildDialogButtons(btnCancel, btnCheckInSelected, btnCheckInAll), BorderLayout.SOUTH);
            } else {
                content.add(buildDialogButtons(btnCancel, btnCheckInSelected), BorderLayout.SOUTH);
            }
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
                scheduledCheckInByDetailId.put(Integer.valueOf(item.getMaChiTietDatPhong()), checkIn);
                scheduledCheckOutByDetailId.put(Integer.valueOf(item.getMaChiTietDatPhong()), checkOut);
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
            cboCheckInBasis.addActionListener(e -> applyCheckInBasisPreviewToSelection());
        }

        private void applyCheckInBasisPreviewToSelection() {
            CheckInOutDAO.CheckInBookingItem selected = getSelectedBookingItem();
            if (selected == null) {
                return;
            }
            if (isManualCheckInBasis()) {
                loadSelectedRoomScheduleIntoEditor();
                return;
            }
            LocalDateTime previewCheckIn = resolveCheckInForBasis(selected, LocalDateTime.now().withSecond(0).withNano(0));
            LocalDateTime previewCheckOut = ensureValidCheckOut(
                    resolveCheckOutForBasis(selected, previewCheckIn),
                    previewCheckIn
            );
            selected.setExpectedCheckIn(previewCheckIn);
            selected.setExpectedCheckOut(previewCheckOut);
            refreshScheduleCellsForItem(selected);
            loadSelectedRoomScheduleIntoEditor();
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
            if (preferredDetailId != null) {
                for (int i = 0; i < bookingItems.size(); i++) {
                    if (bookingItems.get(i).getMaChiTietDatPhong() == preferredDetailId.intValue()) {
                        tblRooms.setRowSelectionInterval(i, i);
                        return;
                    }
                }
            }
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

        private boolean isManualCheckInBasis() {
            return cboCheckInBasis.getSelectedIndex() <= 0;
        }

        private boolean isCurrentCheckInBasis() {
            return cboCheckInBasis.getSelectedIndex() == 1;
        }

        private LocalDateTime resolveScheduledCheckIn(CheckInOutDAO.CheckInBookingItem item) {
            LocalDateTime scheduled = item == null ? null : scheduledCheckInByDetailId.get(Integer.valueOf(item.getMaChiTietDatPhong()));
            return scheduled == null ? resolveDefaultCheckIn() : scheduled;
        }

        private LocalDateTime resolveScheduledCheckOut(CheckInOutDAO.CheckInBookingItem item) {
            LocalDateTime scheduled = item == null ? null : scheduledCheckOutByDetailId.get(Integer.valueOf(item.getMaChiTietDatPhong()));
            if (scheduled != null) {
                return scheduled;
            }
            LocalDateTime fallbackCheckIn = resolveScheduledCheckIn(item);
            return resolveDefaultCheckOut(fallbackCheckIn);
        }

        private LocalDateTime resolveCheckInForBasis(CheckInOutDAO.CheckInBookingItem item, LocalDateTime currentMoment) {
            if (item == null) {
                return currentMoment == null ? resolveDefaultCheckIn() : currentMoment;
            }
            if (isCurrentCheckInBasis()) {
                return currentMoment == null ? LocalDateTime.now().withSecond(0).withNano(0) : currentMoment;
            }
            if (!isManualCheckInBasis()) {
                return resolveScheduledCheckIn(item);
            }
            return item.getExpectedCheckIn() == null ? resolveDefaultCheckIn() : item.getExpectedCheckIn();
        }

        private LocalDateTime resolveCheckOutForBasis(CheckInOutDAO.CheckInBookingItem item, LocalDateTime checkIn) {
            LocalDateTime checkOut = isManualCheckInBasis()
                    ? item == null ? null : item.getExpectedCheckOut()
                    : resolveScheduledCheckOut(item);
            return ensureValidCheckOut(checkOut, checkIn);
        }

        private LocalDateTime ensureValidCheckOut(LocalDateTime checkOut, LocalDateTime checkIn) {
            if (checkIn == null) {
                return checkOut == null ? resolveDefaultCheckOut(resolveDefaultCheckIn()) : checkOut;
            }
            if (checkOut == null || !checkOut.isAfter(checkIn)) {
                return checkIn.plusDays(1);
            }
            return checkOut;
        }

        private void applyCheckInBasisToTargets(List<CheckInOutDAO.CheckInBookingItem> targets) {
            if (targets == null || targets.isEmpty() || isManualCheckInBasis()) {
                return;
            }
            LocalDateTime currentMoment = LocalDateTime.now().withSecond(0).withNano(0);
            for (CheckInOutDAO.CheckInBookingItem item : targets) {
                LocalDateTime checkIn = resolveCheckInForBasis(item, currentMoment);
                LocalDateTime checkOut = resolveCheckOutForBasis(item, checkIn);
                item.setExpectedCheckIn(checkIn);
                item.setExpectedCheckOut(checkOut);
                refreshScheduleCellsForItem(item);
            }
            loadSelectedRoomScheduleIntoEditor();
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
            applyCheckInBasisToTargets(targets);
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

    private final class ActiveStayOperationDialog extends BaseStayDialog {
        private final RoomBlockState roomState;
        private final StayRecord record;
        private final ActiveStaySnapshot snapshot;
        private final JComboBox<DichVu> cboService;
        private final JTextField txtSoLuong;
        private final DefaultTableModel serviceModel;
        private final JTable tblServices;
        private final List<SuDungDichVu> serviceItems = new ArrayList<SuDungDichVu>();

        private ActiveStayOperationDialog(Frame owner, RoomBlockState roomState, StayRecord record) {
            super(owner, "\u0110ang \u1edf", 860, 640);
            this.roomState = roomState;
            this.record = record;
            this.snapshot = roomState == null || roomState.stayId == null ? null : loadActiveStaySnapshot(roomState.stayId.intValue());
            cboService = createServiceComboBox(dichVuDAO.getAll());
            txtSoLuong = createInputField("1");
            txtSoLuong.setMaximumSize(new Dimension(90, 34));

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "PH\u00d2NG \u0110ANG \u1ede",
                    "Popup trung t\u00e2m cho l\u01b0u tr\u00fa hi\u1ec7n t\u1ea1i. T\u1ea5t c\u1ea3 thao t\u00e1c nhanh \u0111i t\u1eeb \u0111\u00fang ph\u00f2ng \u0111ang \u0111\u01b0\u1ee3c ch\u1ecdn."
            ), BorderLayout.NORTH);

            JPanel center = new JPanel(new BorderLayout(0, 12));
            center.setOpaque(false);
            center.add(buildActiveStayInfoCard(), BorderLayout.NORTH);

            serviceModel = new DefaultTableModel(new Object[]{"D\u1ecbch v\u1ee5", "SL", "\u0110\u01a1n gi\u00e1", "Th\u00e0nh ti\u1ec1n"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblServices = new JTable(serviceModel);
            tblServices.setRowHeight(28);
            tblServices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            center.add(buildActiveStayServiceCard(), BorderLayout.CENTER);
            content.add(center, BorderLayout.CENTER);

            JButton btnChangeRoom = createOutlineButton("\u0110\u1ed5i ph\u00f2ng", new Color(245, 158, 11), e -> {
                openChangeRoomDialog(record, roomState == null ? null : roomState.stayId);
                dispose();
            });
            JButton btnAddService = createOutlineButton("Th\u00eam d\u1ecbch v\u1ee5", new Color(37, 99, 235), e -> addSelectedService());
            JButton btnExtend = createOutlineButton("Gia h\u1ea1n", new Color(59, 130, 246), e -> {
                openExtendDialog();
                dispose();
            });
            JButton btnCheckOut = createPrimaryButton("Check-out", new Color(220, 38, 38), Color.WHITE, e -> {
                openCheckOutDialog(record, roomState == null ? null : roomState.stayId, false);
                dispose();
            });
            JButton btnClose = createOutlineButton("\u0110\u00f3ng", new Color(107, 114, 128), e -> dispose());
            List<JButton> actionButtons = new ArrayList<JButton>();
            actionButtons.add(btnClose);
            actionButtons.add(btnChangeRoom);
            actionButtons.add(btnAddService);
            actionButtons.add(btnExtend);
            actionButtons.add(btnCheckOut);
            if (record != null && record.hasMultipleRooms()) {
                if (record.countRoomsByStatus(roomBlocksByCode, RoomStatusKey.BOOKED) > 0) {
                    actionButtons.add(createOutlineButton("Check-in to\u00e0n b\u1ed9 \u0111\u01a1n", new Color(22, 163, 74), e -> {
                        openCheckInDialog(record, roomState == null ? null : roomState.detailId, true);
                        dispose();
                    }));
                }
                if (record.countRoomsByStatus(roomBlocksByCode, RoomStatusKey.OCCUPIED) > 1) {
                    actionButtons.add(createOutlineButton("Check-out to\u00e0n b\u1ed9 \u0111\u01a1n", new Color(220, 38, 38), e -> {
                        openCheckOutDialog(record, roomState == null ? null : roomState.stayId, true);
                        dispose();
                    }));
                }
            }
            content.add(buildDialogButtons(actionButtons.toArray(new JButton[0])), BorderLayout.SOUTH);

            add(content, BorderLayout.CENTER);
            reloadServiceHistory();
        }

        private JPanel buildActiveStayInfoCard() {
            JPanel card = createDialogCardPanel();
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

            JLabel lblTitle = new JLabel("Th\u00f4ng tin l\u01b0u tr\u00fa hi\u1ec7n t\u1ea1i");
            lblTitle.setFont(AppFonts.section(14));
            lblTitle.setForeground(TEXT_PRIMARY);
            body.add(lblTitle);
            body.add(Box.createVerticalStrut(10));
            body.add(createInfoLine("M\u00e3 booking", roomState == null ? "-" : safeValue(roomState.bookingCode, "-")));
            body.add(createInfoLine("M\u00e3 l\u01b0u tr\u00fa", snapshot == null ? "-" : "LT" + snapshot.maLuuTru));
            body.add(createInfoLine("Kh\u00e1ch \u0111\u1ea1i di\u1ec7n", roomState == null ? "-" : safeValue(roomState.representativeName, record == null ? "-" : record.khachHang)));
            body.add(createInfoLine("Ph\u00f2ng", roomState == null ? "-" : roomState.roomCode));
            body.add(createInfoLine("Lo\u1ea1i ph\u00f2ng", roomState == null ? "-" : safeValue(roomState.roomType, "-")));
            body.add(createInfoLine("T\u1ea7ng", roomState == null ? "-" : safeValue(roomState.floorName, "-")));
            body.add(createInfoLine("Tr\u1ea1ng th\u00e1i", roomState == null ? "-" : safeValue(roomState.statusText, "-")));
            body.add(createInfoLine("S\u1ed1 ng\u01b0\u1eddi", roomState == null ? "-" : resolveRoomPeopleDisplay(roomState)));
            body.add(createInfoLine("Gi\u1edd v\u00e0o", snapshot == null ? "-" : formatDateTime(snapshot.checkIn)));
            body.add(createInfoLine("Gi\u1edd ra d\u1ef1 ki\u1ebfn", snapshot == null ? "-" : formatDateTime(snapshot.expectedCheckOut)));
            body.add(createInfoLine("Booking ti\u1ebfp theo", buildNextBookingSummary(roomState)));
            body.add(Box.createVerticalStrut(8));

            JLabel lblRelated = new JLabel("Ph\u00f2ng c\u00f9ng booking");
            lblRelated.setFont(LABEL_FONT);
            lblRelated.setForeground(TEXT_MUTED);
            body.add(lblRelated);
            body.add(Box.createVerticalStrut(4));
            body.add(buildRelatedRoomsSummaryPanel());
            card.add(body, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildRelatedRoomsSummaryPanel() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            List<RoomBlockState> relatedRooms = record == null ? new ArrayList<RoomBlockState>() : record.resolveRoomStates(roomBlocksByCode);
            if (relatedRooms.isEmpty()) {
                JLabel lbl = new JLabel("Kh\u00f4ng c\u00f3 ph\u00f2ng kh\u00e1c trong booking.");
                lbl.setFont(AppFonts.body(12));
                lbl.setForeground(TEXT_MUTED);
                panel.add(lbl);
                return panel;
            }
            for (RoomBlockState related : relatedRooms) {
                panel.add(createRelatedRoomSummaryRow(related));
            }
            return panel;
        }

        private JPanel createRelatedRoomSummaryRow(RoomBlockState related) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(0, 0, 6, 0));

            JLabel lblRoom = new JLabel(related == null ? "-" : safeValue(related.roomCode, "-"));
            lblRoom.setFont(AppFonts.ui(Font.BOLD, 12));
            lblRoom.setForeground(TEXT_PRIMARY);

            JLabel lblMeta = new JLabel(related == null
                    ? "-"
                    : safeValue(related.statusText, "-") + " | " + safeValue(related.roomType, "-"));
            lblMeta.setFont(AppFonts.body(12));
            lblMeta.setForeground(TEXT_MUTED);

            row.add(lblRoom, BorderLayout.WEST);
            row.add(lblMeta, BorderLayout.CENTER);
            return row;
        }

        private JPanel buildActiveStayServiceCard() {
            JPanel card = createDialogCardPanel();
            JPanel content = new JPanel(new BorderLayout(0, 10));
            content.setOpaque(false);

            JLabel lblTitle = new JLabel("D\u1ecbch v\u1ee5 nhanh cho ph\u00f2ng \u0111ang \u1edf");
            lblTitle.setFont(AppFonts.section(14));
            lblTitle.setForeground(TEXT_PRIMARY);
            content.add(lblTitle, BorderLayout.NORTH);

            JPanel quickRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            quickRow.setOpaque(false);
            quickRow.add(cboService);
            quickRow.add(new JLabel("SL"));
            quickRow.add(txtSoLuong);
            quickRow.add(createPrimaryButton("+", new Color(37, 99, 235), Color.WHITE, e -> addSelectedService()));
            quickRow.add(createOutlineButton("-", new Color(220, 38, 38), e -> deleteSelectedService()));

            JPanel center = new JPanel(new BorderLayout(0, 8));
            center.setOpaque(false);
            center.add(quickRow, BorderLayout.NORTH);
            center.add(new JScrollPane(tblServices), BorderLayout.CENTER);

            JLabel lblHint = new JLabel("Gi\u1eef tinh th\u1ea7n thao t\u00e1c nhanh: d\u1ea5u + \u0111\u1ec3 th\u00eam, d\u1ea5u - \u0111\u1ec3 x\u00f3a. Kh\u00f4ng d\u00f9ng S\u1eeda / L\u01b0u.");
            lblHint.setFont(AppFonts.body(12));
            lblHint.setForeground(TEXT_MUTED);
            center.add(lblHint, BorderLayout.SOUTH);

            content.add(center, BorderLayout.CENTER);
            card.add(content, BorderLayout.CENTER);
            return card;
        }

        private JPanel createInfoLine(String label, String value) {
            JPanel line = new JPanel(new BorderLayout(8, 0));
            line.setOpaque(false);
            line.setBorder(new EmptyBorder(0, 0, 6, 0));

            JLabel lblLabel = new JLabel(label);
            lblLabel.setFont(LABEL_FONT);
            lblLabel.setForeground(TEXT_MUTED);

            JLabel lblValue = new JLabel(safeValue(value, "-"));
            lblValue.setFont(AppFonts.ui(Font.BOLD, 13));
            lblValue.setForeground(TEXT_PRIMARY);

            line.add(lblLabel, BorderLayout.WEST);
            line.add(lblValue, BorderLayout.CENTER);
            return line;
        }

        private void reloadServiceHistory() {
            serviceItems.clear();
            serviceModel.setRowCount(0);
            if (snapshot == null || snapshot.maLuuTru <= 0) {
                return;
            }
            serviceItems.addAll(suDungDichVuDAO.getByMaLuuTru(snapshot.maLuuTru));
            for (SuDungDichVu item : serviceItems) {
                serviceModel.addRow(new Object[]{
                        safeValue(item.getTenDichVu(), "-"),
                        item.getSoLuong(),
                        formatMoney(item.getDonGia()),
                        formatMoney(item.getThanhTien())
                });
            }
        }

        private void addSelectedService() {
            if (snapshot == null || snapshot.maLuuTru <= 0) {
                showInfo("Kh\u00f4ng c\u00f2n l\u01b0u tr\u00fa h\u1ee3p l\u1ec7 \u0111\u1ec3 th\u00eam d\u1ecbch v\u1ee5.");
                return;
            }
            if (!isStayCurrentlyActive(snapshot.maLuuTru)) {
                showInfo("Ph\u00f2ng n\u00e0y kh\u00f4ng c\u00f2n \u1edf tr\u1ea1ng th\u00e1i \u0111ang \u1edf.");
                return;
            }
            DichVu dichVu = getSelectedDichVu(cboService);
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
            SuDungDichVu usage = new SuDungDichVu(snapshot.maLuuTru, dichVu.getMaDichVu(), soLuong, dichVu.getDonGia());
            if (!suDungDichVuDAO.insertSuDungDichVu(usage)) {
                showInfo("Kh\u00f4ng th\u1ec3 th\u00eam d\u1ecbch v\u1ee5 cho ph\u00f2ng n\u00e0y.");
                return;
            }
            CheckInOutGUI.refreshAllOpenInstances();
            reloadServiceHistory();
            txtSoLuong.setText("1");
        }

        private void deleteSelectedService() {
            int row = tblServices.getSelectedRow();
            if (row < 0 || row >= serviceItems.size()) {
                showInfo("Vui l\u00f2ng ch\u1ecdn d\u1ecbch v\u1ee5 c\u1ea7n x\u00f3a.");
                return;
            }
            SuDungDichVu selected = serviceItems.get(row);
            if (!suDungDichVuDAO.deleteSuDungDichVu(selected.getMaSuDung())) {
                showInfo("Kh\u00f4ng th\u1ec3 x\u00f3a d\u1ecbch v\u1ee5 \u0111\u00e3 ch\u1ecdn.");
                return;
            }
            CheckInOutGUI.refreshAllOpenInstances();
            reloadServiceHistory();
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
        private final Integer preferredStayId;
        private ActiveStaySnapshot currentSnapshot;

        private ChangeRoomDialog(Frame owner, StayRecord record, Integer preferredStayId) {
            super(owner, "\u0110\u1ed5i ph\u00f2ng", 820, 640);
            this.record = record;
            this.preferredStayId = preferredStayId;
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

            selectPreferredStay();
            refreshDialogState();
        }

        private void selectPreferredStay() {
            if (preferredStayId == null) {
                return;
            }
            for (int i = 0; i < cboCurrentStay.getItemCount(); i++) {
                ServiceStayOption option = cboCurrentStay.getItemAt(i);
                if (option != null && option.maLuuTru == preferredStayId.intValue()) {
                    cboCurrentStay.setSelectedIndex(i);
                    return;
                }
            }
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
        private final Integer preferredStayId;
        private final boolean allowBulkActions;
        private boolean updatingCheckoutEditor;

        private CheckOutDialog(Frame owner, StayRecord record, Integer preferredStayId, boolean allowBulkActions) {
            super(owner, "Check-out", 820, 620);
            this.record = record;
            this.preferredStayId = preferredStayId;
            this.allowBulkActions = allowBulkActions;
            try {
                stayItems.addAll(loadCheckoutStayItems(record.maDatPhong));
            } catch (Exception e) {
                e.printStackTrace();
                showInfo("Kh\u00f4ng th\u1ec3 t\u1ea3i danh s\u00e1ch ph\u00f2ng c\u1ea7n check-out.");
            }
            initializeCheckoutEditorState();

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHECK-OUT",
                    allowBulkActions
                            ? "Ch\u1ecdn t\u1eebng ph\u00f2ng \u0111\u1ec3 check-out ri\u00eang ho\u1eb7c d\u00f9ng thao t\u00e1c check-out to\u00e0n b\u1ed9 \u0111\u01a1n."
                            : "Check-out cho ph\u00f2ng \u0111ang \u0111\u01b0\u1ee3c ch\u1ecdn, gi\u1eef \u0111\u00fang flow thanh to\u00e1n v\u00e0 d\u1ecdn ph\u00f2ng."
            ), BorderLayout.NORTH);

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
                    allowBulkActions ? "Check-out ph\u00f2ng \u0111\u00e3 ch\u1ecdn" : "Check-out",
                    new Color(220, 38, 38),
                    Color.WHITE,
                    e -> submit(txtNgayRa, txtGioRa, false)
            );
            JButton btnCancel = createOutlineButton("H\u1ee7y", new Color(107, 114, 128), e -> dispose());
            if (allowBulkActions) {
                JButton btnCheckOutAll = createPrimaryButton(
                        "Check-out to\u00e0n b\u1ed9 \u0111\u01a1n",
                        new Color(185, 28, 28),
                        Color.WHITE,
                        e -> submit(txtNgayRa, txtGioRa, true)
                );
                content.add(buildDialogButtons(btnCancel, btnCheckOutSelected, btnCheckOutAll), BorderLayout.SOUTH);
            } else {
                content.add(buildDialogButtons(btnCancel, btnCheckOutSelected), BorderLayout.SOUTH);
            }
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
            if (preferredStayId != null) {
                for (CheckoutStayItem item : stayItems) {
                    if (item.maLuuTru == preferredStayId.intValue()) {
                        return item;
                    }
                }
            }
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
                showInfo((checkOutAll
                        ? "\u0110\u00e3 check-out c\u00e1c ph\u00f2ng c\u00f2n \u0111ang \u1edf trong \u0111\u01a1n."
                        : "\u0110\u00e3 check-out ph\u00f2ng \u0111\u00e3 ch\u1ecdn.")
                        + (focusInvoiceId != null || bookingFinished
                        ? " C\u00e1c ph\u00f2ng v\u1eeba tr\u1ea3 \u0111ang chuy\u1ec3n sang Ch\u1edd thanh to\u00e1n ngay tr\u00ean s\u01a1 \u0111\u1ed3."
                        : " C\u00e1c ph\u00f2ng c\u00f2n l\u1ea1i v\u1eabn gi\u1eef tr\u1ea1ng th\u00e1i hi\u1ec7n t\u1ea1i."));
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
                     "UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ? AND checkOut IS NULL");
             PreparedStatement markWaitPayment = con.prepareStatement(
                     "UPDATE Phong SET trangThai = N'Ch\u1edd thanh to\u00e1n' WHERE maPhong = ? AND ISNULL(trangThai, N'') <> N'B\u1ea3o tr\u00ec'")) {
            for (CheckoutStayItem item : items) {
                LocalDateTime checkOut = item.editedActualCheckOut == null ? fallbackNow : item.editedActualCheckOut;
                updateStay.setTimestamp(1, Timestamp.valueOf(checkOut));
                updateStay.setInt(2, item.maLuuTru);
                int updated = updateStay.executeUpdate();
                if (updated > 0) {
                    affected += updated;
                    roomIds.add(Integer.valueOf(item.maPhong));
                    markWaitPayment.setInt(1, item.maPhong);
                    markWaitPayment.executeUpdate();
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

    private Color resolveBlockBackground(String statusKey) {
        if (RoomStatusKey.BOOKED.equals(statusKey)) {
            return new Color(255, 247, 237);
        }
        if (RoomStatusKey.OCCUPIED.equals(statusKey)) {
            return new Color(239, 246, 255);
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(statusKey)) {
            return new Color(254, 242, 242);
        }
        if (RoomStatusKey.CLEANING.equals(statusKey)) {
            return new Color(240, 253, 250);
        }
        if (RoomStatusKey.MAINTENANCE.equals(statusKey)) {
            return new Color(243, 244, 246);
        }
        return new Color(236, 253, 245);
    }

    private Color resolveBlockAccent(String statusKey) {
        if (RoomStatusKey.BOOKED.equals(statusKey)) {
            return new Color(217, 119, 6);
        }
        if (RoomStatusKey.OCCUPIED.equals(statusKey)) {
            return new Color(37, 99, 235);
        }
        if (RoomStatusKey.WAIT_PAYMENT.equals(statusKey)) {
            return new Color(220, 38, 38);
        }
        if (RoomStatusKey.CLEANING.equals(statusKey)) {
            return new Color(13, 148, 136);
        }
        if (RoomStatusKey.MAINTENANCE.equals(statusKey)) {
            return new Color(107, 114, 128);
        }
        return new Color(22, 163, 74);
    }

    private static final class RoomStatusKey {
        private static final String AVAILABLE = "AVAILABLE";
        private static final String BOOKED = "BOOKED";
        private static final String OCCUPIED = "OCCUPIED";
        private static final String WAIT_PAYMENT = "WAIT_PAYMENT";
        private static final String CLEANING = "CLEANING";
        private static final String MAINTENANCE = "MAINTENANCE";
    }

    private static final class RoomBadgeConfig {
        private final String text;
        private final Color background;
        private final Color foreground;
        private final String tooltip;

        private RoomBadgeConfig(String text, Color background, Color foreground, String tooltip) {
            this.text = text;
            this.background = background;
            this.foreground = foreground;
            this.tooltip = tooltip;
        }
    }

    private static final class RoomMapScrollPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 18);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class DetailScrollPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 18);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class ResponsiveRoomGridLayout implements LayoutManager {
        private final int columns;
        private final int minCellWidth;
        private final int preferredCellHeight;
        private final int horizontalGap;
        private final int verticalGap;

        private ResponsiveRoomGridLayout(int columns, int minCellWidth, int preferredCellHeight,
                                         int horizontalGap, int verticalGap) {
            this.columns = Math.max(1, columns);
            this.minCellWidth = Math.max(1, minCellWidth);
            this.preferredCellHeight = Math.max(1, preferredCellHeight);
            this.horizontalGap = Math.max(0, horizontalGap);
            this.verticalGap = Math.max(0, verticalGap);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            Insets insets = parent.getInsets();
            int count = parent.getComponentCount();
            int layoutColumns = resolveColumns(count);
            int availableWidth = parent.getWidth() > 0
                    ? Math.max(1, parent.getWidth() - insets.left - insets.right)
                    : preferredColumnsWidth(layoutColumns);
            int rows = count <= 0 ? 1 : (int) Math.ceil(count / (double) layoutColumns);
            int width = parent.getWidth() > 0
                    ? parent.getWidth()
                    : insets.left + insets.right + Math.min(availableWidth, preferredColumnsWidth(layoutColumns));
            int height = insets.top + insets.bottom + (rows * preferredCellHeight) + (Math.max(0, rows - 1) * verticalGap);
            return new Dimension(width, height);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            Insets insets = parent.getInsets();
            return new Dimension(insets.left + insets.right + preferredColumnsWidth(Math.min(columns, 2)),
                    insets.top + insets.bottom + preferredCellHeight);
        }

        @Override
        public void layoutContainer(Container parent) {
            Insets insets = parent.getInsets();
            int count = parent.getComponentCount();
            if (count <= 0) {
                return;
            }

            int availableWidth = Math.max(1, parent.getWidth() - insets.left - insets.right);
            int layoutColumns = resolveColumns(count);
            int totalHorizontalGap = Math.max(0, layoutColumns - 1) * horizontalGap;
            int cellWidth = Math.max(1, (availableWidth - totalHorizontalGap) / layoutColumns);
            int cellHeight = preferredCellHeight;

            for (int index = 0; index < count; index++) {
                Component component = parent.getComponent(index);
                int row = index / layoutColumns;
                int column = index % layoutColumns;
                int x = insets.left + (column * (cellWidth + horizontalGap));
                int y = insets.top + (row * (cellHeight + verticalGap));
                component.setBounds(x, y, cellWidth, cellHeight);
            }
        }

        private int resolveColumns(int componentCount) {
            return Math.max(1, Math.min(columns, Math.max(1, componentCount)));
        }

        private int preferredColumnsWidth(int columns) {
            int safeColumns = Math.max(1, columns);
            return (safeColumns * minCellWidth) + (Math.max(0, safeColumns - 1) * horizontalGap);
        }
    }

    private static final class RoomFutureHint {
        private final int bookingId;
        private final int detailId;
        private final LocalDateTime expectedCheckIn;
        private final LocalDateTime expectedCheckOut;
        private final String representativeName;

        private RoomFutureHint(int bookingId, int detailId, LocalDateTime expectedCheckIn, LocalDateTime expectedCheckOut, String representativeName) {
            this.bookingId = bookingId;
            this.detailId = detailId;
            this.expectedCheckIn = expectedCheckIn;
            this.expectedCheckOut = expectedCheckOut;
            this.representativeName = representativeName;
        }
    }

    private static final class RoomBlockState {
        private final int roomId;
        private final String roomCode;
        private final String floorName;
        private final String roomType;
        private final String rawRoomStatus;
        private String statusKey;
        private String statusText;
        private Integer bookingId;
        private Integer stayId;
        private Integer detailId;
        private Integer invoiceId;
        private String bookingCode;
        private String representativeName;
        private int guestCount;
        private LocalDateTime checkInTime;
        private LocalDateTime expectedCheckIn;
        private LocalDateTime expectedCheckOut;
        private LocalDateTime actualCheckOut;
        private String note;
        private int futureHintCount;
        private RoomFutureHint nextFutureHint;

        private RoomBlockState(int roomId, String roomCode, String floorName, String roomType, String rawRoomStatus) {
            this.roomId = roomId;
            this.roomCode = roomCode;
            this.floorName = floorName;
            this.roomType = roomType;
            this.rawRoomStatus = rawRoomStatus;
        }

        private boolean hasBookingContext() {
            return bookingId != null && bookingId.intValue() > 0;
        }

        private boolean isBulkCheckInCandidate() {
            return RoomStatusKey.BOOKED.equals(statusKey);
        }

        private boolean isBulkCheckOutCandidate() {
            return RoomStatusKey.OCCUPIED.equals(statusKey);
        }

        private String getStayCodeDisplay() {
            return stayId == null ? "-" : "LT" + stayId;
        }
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
        private final Set<String> summaryCheckInTimes = new LinkedHashSet<String>();
        private final Set<String> summaryCheckOutTimes = new LinkedHashSet<String>();
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

        private List<RoomBlockState> resolveRoomStates(Map<String, RoomBlockState> roomStatesByCode) {
            List<RoomBlockState> states = new ArrayList<RoomBlockState>();
            if (roomStatesByCode == null) {
                return states;
            }
            for (String roomCode : soPhongList) {
                RoomBlockState state = roomStatesByCode.get(roomCode);
                if (state != null) {
                    states.add(state);
                }
            }
            return states;
        }

        private int countRoomsByStatus(Map<String, RoomBlockState> roomStatesByCode, String statusKey) {
            int count = 0;
            for (RoomBlockState state : resolveRoomStates(roomStatesByCode)) {
                if (state != null && statusKey.equals(state.statusKey)) {
                    count++;
                }
            }
            return count;
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

        private void observeSummaryCheckIn(Timestamp value) {
            observeSummaryTime(summaryCheckInTimes, value);
        }

        private void observeSummaryCheckOut(Timestamp value) {
            observeSummaryTime(summaryCheckOutTimes, value);
        }

        private String getSummaryCheckInDisplay() {
            return resolveSummaryDisplay(summaryCheckInTimes, gioVao);
        }

        private String getSummaryCheckOutDisplay() {
            return resolveSummaryDisplay(summaryCheckOutTimes, gioRaDuKien);
        }

        private void observeSummaryTime(Set<String> summaryValues, Timestamp value) {
            if (value == null) {
                return;
            }
            summaryValues.add(formatSummaryDateTime(value));
        }

        private String resolveSummaryDisplay(Set<String> summaryValues, String fallback) {
            if (summaryValues.isEmpty()) {
                return normalizeSummaryDisplay(fallback);
            }
            if (summaryValues.size() > 1) {
                return "Kh\u00e1c nhau";
            }
            return summaryValues.iterator().next();
        }

        private String normalizeSummaryDisplay(String value) {
            return value == null || value.trim().isEmpty() ? "-" : value.trim();
        }

        private String formatSummaryDateTime(Timestamp value) {
            if (value == null) {
                return "-";
            }
            LocalDateTime dateTime = value.toLocalDateTime();
            return DATE_FORMAT.format(dateTime.toLocalDate()) + " " + TIME_FORMAT.format(dateTime.toLocalTime());
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

