package gui;

import dao.DatPhongDAO;
import db.ConnectDB;
import entity.DatPhongConflictInfo;
import gui.common.AppBranding;
import gui.common.AppDatePickerField;
import gui.common.AppTimePickerField;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class DatPhongGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color CONFLICT_BG = new Color(254, 242, 242);
    private static final Color CONFLICT_BORDER = new Color(239, 68, 68);
    private static final Color CONFLICT_TEXT = new Color(185, 28, 28);
    private static final Color CONFLICT_ROW_BG = new Color(254, 226, 226);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.forLanguageTag("vi-VN")).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm", Locale.forLanguageTag("vi-VN")).withResolverStyle(ResolverStyle.STRICT);
    private static final LocalTime DETAIL_BOOKING_BOUNDARY_TIME = LocalTime.of(12, 0);

    private static final List<DatPhongGUI> OPEN_INSTANCES = new ArrayList<DatPhongGUI>();
    private static Integer pendingFocusedBookingId;
    private static String pendingStatusFilter;

    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<BookingRecord> allBookings = new ArrayList<BookingRecord>();
    private final List<BookingRecord> filteredBookings = new ArrayList<BookingRecord>();

    private JTable tblDatPhong;
    private DefaultTableModel bookingModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboNguonDat;
    private JComboBox<String> cboLoaiPhong;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblMaDatPhong;
    private JLabel lblKhachHang;
    private JLabel lblSoDienThoai;
    private JLabel lblCccd;
    private JLabel lblLoaiPhong;
    private JLabel lblSoNguoi;
    private JLabel lblNgayNhanPhong;
    private JLabel lblNgayTraPhong;
    private JLabel lblTrangThai;
    private JLabel lblTienCoc;
    private JTable tblBookingDetails;
    private DefaultTableModel bookingDetailModel;
    private JTextArea txtGhiChu;
    private JButton btnRestoreBooking;

    public DatPhongGUI() {
        this("guest", "Lễ tân");
    }

    public DatPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý đặt phòng - Hotel PMS");
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
                    OPEN_INSTANCES.remove(DatPhongGUI.this);
                }
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.DAT_PHONG, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ ĐẶT PHÒNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Tạo booking có dòng chi tiết phòng, đồng bộ SQL và bỏ nút lưu đơn lẻ.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_MUTED);

        JLabel lblMeta = new JLabel("Người dùng: " + username + " | Vai trò: " + role);
        lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMeta.setForeground(TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);
        left.add(Box.createVerticalStrut(6));
        left.add(lblMeta);

        card.add(left, BorderLayout.WEST);
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Đặt phòng"), BorderLayout.EAST);
        btnRestoreBooking = createOutlineButton("KhÃ´i phá»¥c booking", new Color(37, 99, 235), e -> openRestoreBookingDialog());
        btnRestoreBooking.setEnabled(false);
        btnRestoreBooking = createOutlineButton("Khôi phục booking", new Color(37, 99, 235), e -> openRestoreBookingDialog());
        btnRestoreBooking.setEnabled(false);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Tạo booking", new Color(22, 163, 74), Color.WHITE, e -> openCreateBookingDialog()));
        card.add(createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> openConfirmBookingDialog()));
        card.add(createPrimaryButton("Nhận cọc", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDepositDialog()));
        card.add(createPrimaryButton("Hủy booking", new Color(220, 38, 38), Color.WHITE, e -> openCancelBookingDialog()));
        btnRestoreBooking = createOutlineButton("Khôi phục booking", new Color(37, 99, 235), e -> openRestoreBookingDialog());
        btnRestoreBooking.setEnabled(false);
        card.add(btnRestoreBooking);
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"T\u1ea5t c\u1ea3", "\u0110\u00e3 \u0111\u1eb7t", "\u0110\u00e3 x\u00e1c nh\u1eadn", "\u0110\u00e3 c\u1ecdc", "Ch\u1edd check-in", "Đã hủy"});
        cboNguonDat = createComboBox(new String[]{"Tất cả", "Đặt trước", "Walk-in"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite", "Family"});
        txtTuNgay = new AppDatePickerField("", false);
        txtDenNgay = new AppDatePickerField("", false);
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(260, 34));
        txtTuKhoa.setToolTipText("Mã đặt phòng / tên khách / số điện thoại");
        ScreenUIHelper.installAutoFilter(() -> applyFilters(false), cboTrangThai, cboNguonDat, cboLoaiPhong);
        ScreenUIHelper.installLiveSearch(txtTuNgay, () -> applyFilters(false));
        ScreenUIHelper.installLiveSearch(txtDenNgay, () -> applyFilters(false));
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> applyFilters(false));

        left.add(createFieldGroup("Trạng thái", cboTrangThai));
        left.add(createFieldGroup("Nguồn đặt", cboNguonDat));
        left.add(createFieldGroup("Loại phòng", cboLoaiPhong));
        left.add(createFieldGroup("Từ ngày", txtTuNgay));
        left.add(createFieldGroup("Đến ngày", txtDenNgay));

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
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.62);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JLabel lblTitle = new JLabel("Danh sách đặt phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Bấm vào từng dòng để xem chi tiết.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "Mã đặt phòng",
                "Khách hàng",
                "Ngày đặt",
                "Nguồn",
                "Tổng tiền cọc",
                "Trạng thái",
                "Số dòng chi tiết",
                "Ghi chú"
        };

        bookingModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDatPhong = new JTable(bookingModel);
        tblDatPhong.setFont(BODY_FONT);
        tblDatPhong.setRowHeight(32);
        tblDatPhong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDatPhong.setGridColor(new Color(229, 231, 235));
        tblDatPhong.setShowGrid(true);
        tblDatPhong.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblDatPhong);

        tblDatPhong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblDatPhong.getSelectedRow();
                if (row >= 0 && row < filteredBookings.size()) {
                    updateDetailPanel(filteredBookings.get(row));
                }
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblDatPhong, this::openUpdateBookingDialog);

        JScrollPane scrollPane = new JScrollPane(tblDatPhong);
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
        ensureHiddenDetailFieldsInitialized();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(buildDetailLinesCard(), BorderLayout.CENTER);
        return wrapper;
    }

    private void ensureHiddenDetailFieldsInitialized() {
        if (lblMaDatPhong != null && lblKhachHang != null && txtGhiChu != null) {
            return;
        }
        buildDetailCard();
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết đặt phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblMaDatPhong = createValueLabel();
        lblKhachHang = createValueLabel();
        lblSoDienThoai = createValueLabel();
        lblCccd = createValueLabel();
        lblLoaiPhong = createValueLabel();
        lblSoNguoi = createValueLabel();
        lblNgayNhanPhong = createValueLabel();
        lblNgayTraPhong = createValueLabel();
        lblTrangThai = createValueLabel();
        lblTienCoc = createValueLabel();

        addDetailRow(body, "Mã đặt phòng", lblMaDatPhong);
        addDetailRow(body, "Khách hàng", lblKhachHang);
        addDetailRow(body, "Số điện thoại", lblSoDienThoai);
        addDetailRow(body, "CCCD", lblCccd);
        addDetailRow(body, "Tóm tắt loại phòng", lblLoaiPhong);
        addDetailRow(body, "Tổng số người", lblSoNguoi);
        addDetailRow(body, "Check-in sớm nhất", lblNgayNhanPhong);
        addDetailRow(body, "Check-out muộn nhất", lblNgayTraPhong);
        addDetailRow(body, "Trạng thái", lblTrangThai);
        addDetailRow(body, "Tổng tiền cọc", lblTienCoc);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);
        JLabel lblNote = new JLabel("Ghi chú");
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

    private JPanel buildDetailLinesCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết các dòng đặt phòng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] columns = {"STT", "Phòng", "Loại phòng", "Check-in", "Check-out", "Số người", "Loại ngày", "Loại giá", "Giá áp dụng", "Tiền cọc", "Trạng thái"};
        bookingDetailModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBookingDetails = new JTable(bookingDetailModel);
        tblBookingDetails.setFont(BODY_FONT);
        tblBookingDetails.setRowHeight(28);
        tblBookingDetails.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ScreenUIHelper.styleTableHeader(tblBookingDetails);

        JScrollPane scrollPane = new JScrollPane(tblBookingDetails);
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
                "F1 Tạo booking",
                "F2 Xác nhận",
                "F3 Nhận cọc",
                "F4 Hủy booking",
                "Enter Xem chi tiết"
        );
    }

    private JPanel createFieldGroup(String label, java.awt.Component component) {
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
        field.setPreferredSize(new Dimension(120, 34));
        field.setMaximumSize(new Dimension(160, 34));
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

    private void reloadSampleData(boolean showMessage) {
        loadBookingsFromDatabase();
        String pendingStatus = getPendingStatusFilter();
        cboTrangThai.setSelectedItem(pendingStatus == null ? "T\u1ea5t c\u1ea3" : pendingStatus);
        cboNguonDat.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        txtTuNgay.setText("");
        txtDenNgay.setText("");
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu đặt phòng.");
        }
    }

    private void loadBookingsFromDatabase() {
        allBookings.clear();
        try {
            for (entity.DatPhong datPhong : datPhongDAO.getAll()) {
                BookingRecord booking = toBookingRecord(datPhong);
                booking.trangThai = DatPhongDAO.normalizeStageStatus(booking.trangThai);
                if (DatPhongDAO.isBookingStageStatus(booking.trangThai) || isCancelledBooking(booking.trangThai)) {
                    allBookings.add(booking);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Kh?ng th? t?i d? li?u ??t ph?ng.");
        }
    }

    private List<BookingDetailRecord> loadDetailsForBooking(int maDatPhong, String headerLoaiPhong) {
        List<BookingDetailRecord> details = new ArrayList<BookingDetailRecord>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return details;
        }
        datPhongDAO.ensureDetailScheduleSchema(con);
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maPhong AS maPhongId, ctdp.soNguoi, ctdp.giaPhong, ctdp.thanhTien, " +
                "ISNULL(p.soPhong, N'Chưa gán') AS soPhong, " +
                "ISNULL(p.sucChuaToiDa, 0) AS sucChuaToiDa, " +
                "COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))) AS checkInDuKien, " +
                "COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) AS checkOutDuKien, " +
                "dp.trangThai, " +
                "ISNULL(lp.tenLoaiPhong, ?) AS tenLoaiPhong, " +
                "ISNULL(CAST(lp.maLoaiPhong AS NVARCHAR(20)), '') AS maLoaiPhong " +
                "FROM ChiTietDatPhong ctdp " +
                "JOIN DatPhong dp ON ctdp.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong " +
                "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "WHERE ctdp.maDatPhong = ? " +
                "ORDER BY ctdp.maChiTietDatPhong";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, headerLoaiPhong);
            ps.setInt(2, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookingDetailRecord detail = new BookingDetailRecord();
                    detail.maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                    detail.maPhongId = rs.getObject("maPhongId") == null ? 0 : rs.getInt("maPhongId");
                    detail.maPhong = safeValue(rs.getString("soPhong"), "Chưa gán");
                    detail.loaiPhong = safeValue(rs.getString("tenLoaiPhong"), headerLoaiPhong);
                    detail.maLoaiPhong = rs.getObject("maLoaiPhong") == null ? 0 : Integer.parseInt(safeValue(rs.getString("maLoaiPhong"), "0"));
                    Timestamp checkInTs = rs.getTimestamp("checkInDuKien");
                    Timestamp checkOutTs = rs.getTimestamp("checkOutDuKien");
                    detail.checkInDuKien = normalizeLegacyDetailDateTime(checkInTs == null ? null : checkInTs.toLocalDateTime());
                    detail.checkOutDuKien = normalizeLegacyDetailDateTime(checkOutTs == null ? null : checkOutTs.toLocalDateTime());
                    detail.soNguoi = rs.getInt("soNguoi");
                    detail.sucChuaToiDa = rs.getInt("sucChuaToiDa");
                    detail.giaApDung = rs.getDouble("giaPhong");
                    detail.tienDatCocChiTiet = 0;
                    detail.thanhTien = rs.getDouble("thanhTien");
                    detail.trangThaiChiTiet = safeValue(rs.getString("trangThai"), "Đã đặt");
                    refreshResolvedRate(con, detail);
                    details.add(detail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return details;
    }

    private BookingRecord toBookingRecord(entity.DatPhong datPhong) {
        BookingRecord booking = new BookingRecord();
        booking.maDatPhong = parseIntSafe(datPhong.getMaDatPhong());
        booking.maKhachHang = parseIntSafe(datPhong.getMaKhachHang());
        booking.maKhachHangText = formatCustomerId(booking.maKhachHang);
        booking.khachHang = safeValue(datPhong.getTenKhachHang(), "-");
        booking.soDienThoai = safeValue(datPhong.getSoDienThoaiKhach(), "-");
        booking.cccd = safeValue(datPhong.getCccdPassportKhach(), "");
        booking.ngayDat = datPhong.getNgayDat();
        booking.nguonDat = safeValue(datPhong.getNguonDatPhong(), "Đặt trước");
        booking.trangThai = safeValue(datPhong.getTrangThaiDatPhong(), DatPhongDAO.STATUS_PENDING_CHECKIN);
        booking.ghiChu = safeValue(datPhong.getGhiChu(), "");
        booking.tongTienDatCoc = datPhong.getTongTienDatCoc() > 0d ? datPhong.getTongTienDatCoc() : datPhong.getTienCoc();
        List<BookingDetailRecord> loadedDetails = loadDetailsForBooking(booking.maDatPhong, "-");
        if (loadedDetails.isEmpty()) {
            for (entity.ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
                booking.details.add(toBookingDetailRecord(detail));
            }
        } else {
            booking.details.addAll(loadedDetails);
        }
        if (!booking.details.isEmpty()) {
            distributeHeaderDeposit(booking.details, booking.tongTienDatCoc);
        }
        booking.syncDerivedData();
        return booking;
    }

    private BookingDetailRecord toBookingDetailRecord(entity.ChiTietDatPhong detail) {
        BookingDetailRecord record = new BookingDetailRecord();
        record.maChiTietDatPhong = parseIntSafe(detail.getMaChiTietDatPhong());
        record.loaiPhong = safeValue(detail.getTenLoaiPhong(), safeValue(detail.getLoaiPhong(), "-"));
        record.maLoaiPhong = parseIntSafe(detail.getMaLoaiPhong());
        record.maPhongId = parseIntSafe(detail.getMaPhong());
        record.maPhong = safeValue(detail.getSoPhong(), safeValue(detail.getPhong(), "Chưa gán"));
        record.checkInDuKien = toDefaultDetailDateTime(detail.getCheckInDuKien());
        record.checkOutDuKien = toDefaultDetailDateTime(detail.getCheckOutDuKien());
        record.soNguoi = detail.getSoNguoi();
        record.giaApDung = detail.getGiaApDung();
        record.tienDatCocChiTiet = detail.getTienDatCocChiTiet();
        record.trangThaiChiTiet = safeValue(detail.getTrangThaiChiTiet(), "Chờ check-in");
        record.ghiChu = safeValue(detail.getGhiChu(), "");
        record.thanhTien = detail.getThanhTienTamTinh();
        return record;
    }

    private int parseIntSafe(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void distributeHeaderDeposit(List<BookingDetailRecord> details, double totalDeposit) {
        if (details == null || details.isEmpty()) {
            return;
        }
        double distributed = 0d;
        double each = totalDeposit <= 0d ? 0d : totalDeposit / details.size();
        for (int i = 0; i < details.size(); i++) {
            BookingDetailRecord detail = details.get(i);
            if (i == details.size() - 1) {
                detail.tienDatCocChiTiet = Math.max(0d, totalDeposit - distributed);
            } else {
                detail.tienDatCocChiTiet = each;
                distributed += each;
            }
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredBookings.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String nguonDat = valueOf(cboNguonDat.getSelectedItem());
        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
        LocalDate fromDate = parseDate(txtTuNgay.getText().trim());
        LocalDate toDate = parseDate(txtDenNgay.getText().trim());

        if (!txtTuNgay.getText().trim().isEmpty() && fromDate == null) {
            if (showMessage) {
                showWarning("Từ ngày không đúng định dạng dd/MM/yyyy.");
            }
            return;
        }
        if (!txtDenNgay.getText().trim().isEmpty() && toDate == null) {
            if (showMessage) {
                showWarning("Đến ngày không đúng định dạng dd/MM/yyyy.");
            }
            return;
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            if (showMessage) {
                showWarning("Khoảng ngày không hợp lệ.");
            }
            return;
        }

        for (BookingRecord booking : allBookings) {
            if (!"Tất cả".equals(trangThai) && !booking.trangThai.equalsIgnoreCase(trangThai)) {
                continue;
            }
            if (!"Tất cả".equals(nguonDat) && !booking.nguonDat.equalsIgnoreCase(nguonDat)) {
                continue;
            }
            if (!"Tất cả".equals(loaiPhong) && !booking.matchesRoomType(loaiPhong)) {
                continue;
            }
            if (fromDate != null && booking.ngayNhanPhong != null && booking.ngayNhanPhong.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && booking.ngayTraPhong != null && booking.ngayTraPhong.isAfter(toDate)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (booking.maDatPhong + " " + booking.khachHang + " " + booking.soDienThoai).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredBookings.add(booking);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredBookings.size() + " đặt phòng phù hợp.");
        }
    }

    private void refillTable() {
        bookingModel.setRowCount(0);
        for (BookingRecord booking : filteredBookings) {
            bookingModel.addRow(new Object[]{
                    "DP" + booking.maDatPhong,
                    booking.khachHang,
                    booking.formatNgayDat(),
                    booking.nguonDat,
                    formatMoney(booking.tongTienDatCoc),
                    booking.trangThai,
                    booking.details.size(),
                    booking.ghiChu
            });
        }

        if (!filteredBookings.isEmpty()) {
            int rowToSelect = resolvePreferredSelectionIndex();
            tblDatPhong.setRowSelectionInterval(rowToSelect, rowToSelect);
            tblDatPhong.scrollRectToVisible(tblDatPhong.getCellRect(rowToSelect, 0, true));
            tblDatPhong.requestFocusInWindow();
            updateDetailPanel(filteredBookings.get(rowToSelect));
            clearPendingFocusedBookingIfMatched(filteredBookings.get(rowToSelect).maDatPhong);
        } else {
            clearDetailPanel();
        }
    }

    private int resolvePreferredSelectionIndex() {
        Integer bookingId = pendingFocusedBookingId;
        if (bookingId != null) {
            for (int i = 0; i < filteredBookings.size(); i++) {
                if (filteredBookings.get(i).maDatPhong == bookingId.intValue()) {
                    return i;
                }
            }
        }
        return 0;
    }

    private static synchronized void clearPendingFocusedBookingIfMatched(int maDatPhong) {
        if (pendingFocusedBookingId != null && pendingFocusedBookingId.intValue() == maDatPhong) {
            pendingFocusedBookingId = null;
            pendingStatusFilter = null;
        }
    }

    private void updateDetailPanel(BookingRecord booking) {
        ensureHiddenDetailFieldsInitialized();
        lblMaDatPhong.setText("DP" + booking.maDatPhong);
        lblKhachHang.setText(booking.khachHang);
        lblSoDienThoai.setText(booking.soDienThoai);
        lblCccd.setText(booking.cccd.isEmpty() ? "-" : booking.cccd);
        lblLoaiPhong.setText(booking.getRoomSummary());
        lblSoNguoi.setText(String.valueOf(booking.getTotalGuests()));
        lblNgayNhanPhong.setText(booking.formatNgayNhanPhong());
        lblNgayTraPhong.setText(booking.formatNgayTraPhong());
        lblTrangThai.setText(booking.trangThai);
        lblTienCoc.setText(formatMoney(booking.tongTienDatCoc));
        txtGhiChu.setText(booking.ghiChu.isEmpty() ? "-" : booking.ghiChu);
        txtGhiChu.setCaretPosition(0);
        refillBookingDetailTable(booking);
        updateActionButtonStates(booking);
    }

    private void clearDetailPanel() {
        ensureHiddenDetailFieldsInitialized();
        lblMaDatPhong.setText("-");
        lblKhachHang.setText("-");
        lblSoDienThoai.setText("-");
        lblCccd.setText("-");
        lblLoaiPhong.setText("-");
        lblSoNguoi.setText("-");
        lblNgayNhanPhong.setText("-");
        lblNgayTraPhong.setText("-");
        lblTrangThai.setText("-");
        lblTienCoc.setText("-");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
        if (bookingDetailModel != null) {
            bookingDetailModel.setRowCount(0);
        }
        updateActionButtonStates(null);
    }

    private void refillBookingDetailTable(BookingRecord booking) {
        bookingDetailModel.setRowCount(0);
        for (int i = 0; i < booking.details.size(); i++) {
            BookingDetailRecord detail = booking.details.get(i);
            bookingDetailModel.addRow(new Object[]{
                    i + 1,
                    detail.maPhong == null || detail.maPhong.trim().isEmpty() ? "Chưa gán" : detail.maPhong,
                    detail.loaiPhong,
                    detail.formatCheckIn(),
                    detail.formatCheckOut(),
                    detail.soNguoi,
                    normalizeAppliedRateText(detail.loaiNgayApDung),
                    normalizeAppliedRateText(detail.loaiGiaApDung),
                    formatAppliedRateValue(detail),
                    formatMoney(detail.tienDatCocChiTiet),
                    detail.trangThaiChiTiet
            });
        }
    }

    private BookingRecord getSelectedBooking() {
        int selectedRow = tblDatPhong.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= filteredBookings.size()) {
            showWarning("Vui lòng chọn một booking trong danh sách.");
            return null;
        }
        return filteredBookings.get(selectedRow);
    }

    private void openCreateBookingDialog() {
        new BookingEditorDialog(this, null).setVisible(true);
    }

    private void openConfirmBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking == null) {
            return;
        }
        if (isCancelledBooking(booking.trangThai)) {
            showWarning("Booking đã huỷ. Vui lòng khôi phục trước khi xác nhận.");
            return;
        }
        if (!canConfirmBooking(booking.trangThai)) {
            showWarning("Chỉ booking chưa check-in mới có thể xác nhận.");
            return;
        }
        new ConfirmBookingDialog(this, booking).setVisible(true);
    }

    private void openDepositDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            if (isCancelledBooking(booking.trangThai)) {
                showWarning("Booking Ä‘Ã£ há»§y. Vui lÃ²ng khÃ´i phá»¥c trÆ°á»›c khi nháº­n cá»c.");
                return;
            }
            new DepositDialog(this, booking).setVisible(true);
        }
    }

    private void openUpdateBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            if (isCancelledBooking(booking.trangThai)) {
                showWarning("Booking Ä‘Ã£ há»§y. Vui lÃ²ng khÃ´i phá»¥c trÆ°á»›c khi cáº­p nháº­t.");
                return;
            }
            new BookingEditorDialog(this, booking).setVisible(true);
        }
    }

    private void openCancelBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            if (isCancelledBooking(booking.trangThai)) {
                showWarning("Booking nÃ y Ä‘Ã£ á»Ÿ tráº¡ng thÃ¡i ÄÃ£ há»§y.");
                return;
            }
            new CancelBookingDialog(this, booking).setVisible(true);
        }
    }

    private void openRestoreBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking == null) {
            return;
        }
        if (!isCancelledBooking(booking.trangThai)) {
            showWarning("Chá»‰ booking Ä‘Ã£ há»§y má»›i cÃ³ thá»ƒ khÃ´i phá»¥c.");
            return;
        }
        new RestoreBookingDialog(this, booking).setVisible(true);
    }

    private void openViewBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new ViewBookingDialog(this, booking).setVisible(true);
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

    private boolean canConfirmBooking(String trangThai) {
        String status = safeValue(trangThai, "");
        if ("Đã xác nhận".equalsIgnoreCase(status)
                || "Đã cọc".equalsIgnoreCase(status)
                || DatPhongDAO.STATUS_PENDING_CHECKIN.equalsIgnoreCase(status)) {
            return true;
        }
        return "Đã đặt".equalsIgnoreCase(status) || "Chờ xác nhận".equalsIgnoreCase(status);
    }

    private boolean isCancelledBooking(String trangThai) {
        String status = safeValue(trangThai, "");
        return DatPhongDAO.STATUS_CANCELLED.equalsIgnoreCase(status) || DatPhongDAO.STATUS_CANCELLED_BOOKING.equalsIgnoreCase(status);
    }

    private void updateActionButtonStates(BookingRecord booking) {
        if (btnRestoreBooking != null) {
            btnRestoreBooking.setEnabled(booking != null && isCancelledBooking(booking.trangThai));
        }
    }

    private String resolveRestoreStatus(BookingRecord booking) {
        return DatPhongDAO.STATUS_PENDING_CHECKIN;
    }

    private String buildRestoreConflictMessage(DatPhongConflictInfo conflictInfo) {
        if (conflictInfo == null) {
            return "Má»™t hoáº·c nhiá»u phÃ²ng trong booking Ä‘Ã£ khÃ´ng cÃ²n trá»‘ng trong khoáº£ng ngÃ y cÅ©. KhÃ´ng thá»ƒ khÃ´i phá»¥c tá»± Ä‘á»™ng.";
        }
        return "PhÃ²ng " + safeValue(conflictInfo.getSoPhong(), "-")
                + " Ä‘Ã£ khÃ´ng cÃ²n trá»‘ng trong khoáº£ng " + formatDateTime(conflictInfo.getNgayNhanPhongDateTime())
                + " - " + formatDateTime(conflictInfo.getNgayTraPhongDateTime())
                + " do trÃ¹ng vá»›i booking DP" + conflictInfo.getMaDatPhong()
                + " (" + safeValue(conflictInfo.getTrangThai(), "-") + ").";
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

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "datphong-f1", this::openCreateBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "datphong-f3", this::openConfirmBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "datphong-f4", this::openDepositDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "datphong-f5", this::openCancelBookingDialog);
        ScreenUIHelper.registerShortcut(this, "ENTER", "datphong-enter", this::openViewBookingDialog);
    }

    private LocalDate parseDate(String value) {
        String normalized = normalizeDateInput(value);
        if (normalized == null) {
            return null;
        }
        try {
            return LocalDate.parse(normalized, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String normalizeDateInput(String input) {
        return AppDatePickerField.normalizeFlexibleDateInput(input);
    }

    private LocalDate normalizeDateFieldValue(AppDatePickerField field, String errorMessage) {
        if (field == null) {
            return null;
        }
        String rawValue = field.getText();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            field.setText("");
            return null;
        }
        String normalized = normalizeDateInput(rawValue);
        if (normalized == null) {
            showError(errorMessage);
            return null;
        }
        field.setText(normalized);
        return LocalDate.parse(normalized, DATE_FORMAT);
    }

    private LocalDate normalizeDateFieldValue(JTextField field, String errorMessage) {
        if (field == null) {
            return null;
        }
        String rawValue = field.getText();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            field.setText("");
            return null;
        }
        String normalized = normalizeDateInput(rawValue);
        if (normalized == null) {
            showError(errorMessage);
            return null;
        }
        field.setText(normalized);
        return LocalDate.parse(normalized, DATE_FORMAT);
    }

    private LocalTime normalizeTimeFieldValue(AppTimePickerField field, String errorMessage) {
        if (field == null) {
            return null;
        }
        String rawValue = field.getText();
        if (rawValue == null || rawValue.trim().isEmpty()) {
            showError(errorMessage);
            return null;
        }
        LocalTime parsed = field.getTimeValue();
        if (parsed == null) {
            showError(errorMessage);
            return null;
        }
        LocalTime normalized = parsed.withSecond(0).withNano(0);
        field.setTimeValue(normalized);
        return normalized;
    }

    private void installTimeFieldChangeListener(AppTimePickerField field, Runnable action) {
        if (field == null || action == null) {
            return;
        }
        field.addTextChangeListener(action);
    }

    private LocalDateTime combineDateTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return null;
        }
        return LocalDateTime.of(date, time.withSecond(0).withNano(0));
    }

    private LocalDateTime toDefaultDetailDateTime(LocalDate value) {
        return value == null ? null : LocalDateTime.of(value, DETAIL_BOOKING_BOUNDARY_TIME);
    }

    private LocalDateTime normalizeLegacyDetailDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        LocalDateTime normalized = value.withSecond(0).withNano(0);
        return normalized.toLocalTime().equals(LocalTime.MIDNIGHT)
                ? LocalDateTime.of(normalized.toLocalDate(), DETAIL_BOOKING_BOUNDARY_TIME)
                : normalized;
    }

    private Timestamp toSqlTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value.withSecond(0).withNano(0));
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATETIME_FORMAT);
    }

    private LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    private void installDateFieldChangeListener(AppDatePickerField field, Runnable action) {
        JTextField editor = findNestedTextField(field);
        if (editor == null || action == null) {
            return;
        }
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    private JTextField findNestedTextField(Component component) {
        if (component instanceof JTextField) {
            return (JTextField) component;
        }
        if (component instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) component).getComponents()) {
                JTextField found = findNestedTextField(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String normalizeAppliedRateText(String value) {
        return safeValue(value, "-");
    }

    private String formatAppliedRateValue(BookingDetailRecord detail) {
        if (detail == null) {
            return "-";
        }
        double amount = detail.thanhTien > 0d ? detail.thanhTien : detail.giaApDung;
        String formatted = formatMoney(amount);
        double surcharge = detail.tongPhuThuApDung > 0d ? detail.tongPhuThuApDung : detail.phuThuApDung;
        if (surcharge > 0d) {
            formatted += " (gồm phụ thu " + formatMoney(surcharge) + ")";
        }
        return formatted;
    }

    private int countMojibakeMarkers(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == 'Ã' || ch == 'Â' || ch == 'Ä' || ch == 'Æ') {
                count++;
            }
        }
        return count;
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

    private double parseMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(value.trim().replace(".", ""));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }

    private int parsePositiveIntOrZero(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String formatDate(LocalDate value) {
        return value == null ? "-" : value.format(DATE_FORMAT);
    }

    private LocalDate requireDate(String value, String errorMessage) {
        LocalDate date = parseDate(value);
        if (date == null) {
            showError(errorMessage);
        }
        return date;
    }

    private int findEmployeeIdByUsername(Connection con) {
        String sql = "SELECT nv.maNhanVien FROM TaiKhoan tk JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien WHERE tk.tenDangNhap = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private String formatCustomerId(int maKhachHang) {
        return String.format(Locale.ROOT, "KH%03d", maKhachHang);
    }

    private String buildCustomerOptionDisplay(CustomerLookup lookup) {
        if (lookup == null || lookup.maKhachHang <= 0) {
            return "-- Chọn khách hàng --";
        }
        return formatCustomerId(lookup.maKhachHang)
                + " - " + safeValue(lookup.hoTen, "Chưa có tên")
                + " - " + safeValue(lookup.soDienThoai, "-");
    }

    private CustomerLookup createCustomerPlaceholderOption() {
        CustomerLookup lookup = new CustomerLookup();
        lookup.displayText = buildCustomerOptionDisplay(null);
        return lookup;
    }

    private CustomerLookup mapCustomerLookup(ResultSet rs) throws Exception {
        CustomerLookup lookup = new CustomerLookup();
        lookup.maKhachHang = rs.getInt("maKhachHang");
        lookup.hoTen = safeValue(rs.getString("hoTen"), "");
        lookup.soDienThoai = safeValue(rs.getString("soDienThoai"), "");
        lookup.cccdPassport = safeValue(rs.getString("cccdPassport"), "");
        lookup.ngaySinh = rs.getDate("ngaySinh") == null ? null : rs.getDate("ngaySinh").toLocalDate();
        lookup.email = safeValue(rs.getString("email"), "");
        lookup.diaChi = safeValue(rs.getString("diaChi"), "");
        lookup.displayText = buildCustomerOptionDisplay(lookup);
        return lookup;
    }

    private Integer findExistingCustomerByPhoneOrPassport(Connection con, String soDienThoai, String cccd) {
        String sql = "SELECT TOP 1 maKhachHang FROM KhachHang WHERE soDienThoai = ? OR (? <> '' AND cccdPassport = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            ps.setString(2, cccd == null ? "" : cccd.trim());
            ps.setString(3, cccd == null ? "" : cccd.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer findOrCreateCustomer(Connection con, String hoTen, LocalDate ngaySinh, String soDienThoai, String cccd,
                                         String email, String diaChi) throws Exception {
        Integer existing = findExistingCustomerByPhoneOrPassport(con, soDienThoai, cccd);
        if (existing != null) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, cccdPassport = ?, ngaySinh = COALESCE(?, ngaySinh), "
                            + "email = CASE WHEN ? = '' THEN email ELSE ? END, diaChi = CASE WHEN ? = '' THEN diaChi ELSE ? END WHERE maKhachHang = ?")) {
                ps.setString(1, hoTen);
                ps.setString(2, soDienThoai);
                ps.setString(3, cccd);
                if (ngaySinh == null) {
                    ps.setNull(4, java.sql.Types.DATE);
                } else {
                    ps.setDate(4, Date.valueOf(ngaySinh));
                }
                String safeEmail = email == null ? "" : email.trim();
                String safeDiaChi = diaChi == null ? "" : diaChi.trim();
                ps.setString(5, safeEmail);
                ps.setString(6, safeEmail);
                ps.setString(7, safeDiaChi);
                ps.setString(8, safeDiaChi);
                ps.setInt(9, existing.intValue());
                ps.executeUpdate();
            }
            return existing;
        }
        String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) " +
                "VALUES (?, N'Khác', ?, ?, ?, ?, ?, N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', ?, N'Tạo từ màn đặt phòng')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hoTen);
            if (ngaySinh == null) {
                ps.setNull(2, java.sql.Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(ngaySinh));
            }
            ps.setString(3, soDienThoai);
            ps.setString(4, email == null ? "" : email.trim());
            ps.setString(5, cccd);
            ps.setString(6, diaChi == null ? "" : diaChi.trim());
            ps.setString(7, username);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private CustomerLookup loadCustomerLookup(int maKhachHang) {
        if (maKhachHang <= 0) {
            return null;
        }
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT maKhachHang, hoTen, soDienThoai, cccdPassport, ngaySinh, email, diaChi FROM KhachHang WHERE maKhachHang = ?")) {
            ps.setInt(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCustomerLookup(rs);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private List<CustomerLookup> loadCustomerOptions(Integer preferredCustomerId) {
        List<CustomerLookup> customers = new ArrayList<CustomerLookup>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return customers;
        }

        boolean containsPreferred = false;
        String sql = "SELECT maKhachHang, hoTen, soDienThoai, cccdPassport, ngaySinh, email, diaChi "
                + "FROM KhachHang "
                + "WHERE ISNULL(trangThai, N'Hoạt động') <> N'Ngừng giao dịch' "
                + "ORDER BY maKhachHang DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CustomerLookup lookup = mapCustomerLookup(rs);
                customers.add(lookup);
                if (preferredCustomerId != null && lookup.maKhachHang == preferredCustomerId.intValue()) {
                    containsPreferred = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (preferredCustomerId != null && preferredCustomerId.intValue() > 0 && !containsPreferred) {
            CustomerLookup preferredCustomer = loadCustomerLookup(preferredCustomerId.intValue());
            if (preferredCustomer != null) {
                customers.add(0, preferredCustomer);
            }
        }
        return customers;
    }

    private CustomerLookup findCustomerByPassportOrCccd(String cccd) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }
        String value = cccd == null ? "" : cccd.trim();
        if (value.isEmpty()) {
            return null;
        }
        String sql = "SELECT TOP 1 maKhachHang, hoTen, soDienThoai, cccdPassport, ngaySinh, email, diaChi FROM KhachHang WHERE cccdPassport = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CustomerLookup lookup = mapCustomerLookup(rs);
                    if (lookup.cccdPassport.isEmpty()) {
                        lookup.cccdPassport = value;
                    }
                    return lookup;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer findBangGiaByRoomType(Connection con, String roomType) throws Exception {
        String sql = "SELECT TOP 1 bg.maBangGia FROM BangGia bg JOIN LoaiPhong lp ON bg.maLoaiPhong = lp.maLoaiPhong " +
                "WHERE lp.tenLoaiPhong = ? AND bg.trangThai = N'Đang áp dụng' ORDER BY bg.maBangGia DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private Integer findBangGiaByLoaiPhongId(Connection con, int maLoaiPhong) throws Exception {
        String sql = "SELECT TOP 1 maBangGia FROM BangGia WHERE maLoaiPhong = ? AND trangThai = N'Đang áp dụng' ORDER BY maBangGia DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLoaiPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }
    private void refreshResolvedRate(Connection con, BookingDetailRecord detail) throws Exception {
        if (detail == null || detail.maLoaiPhong <= 0 || detail.checkInDuKien == null) {
            return;
        }
        Integer maBangGia = findBangGiaByLoaiPhongId(con, detail.maLoaiPhong);
        if (maBangGia == null) {
            return;
        }
        DatPhongDAO.RoomRateResolution resolution = datPhongDAO.resolveRoomRateWithSurcharge(
                String.valueOf(maBangGia.intValue()),
                detail.checkInDuKien == null ? null : detail.checkInDuKien.toLocalDate(),
                detail.checkOutDuKien == null ? null : detail.checkOutDuKien.toLocalDate()
        );
        detail.loaiNgayApDung = normalizeAppliedRateText(resolution.getLoaiNgay());
        detail.loaiGiaApDung = normalizeAppliedRateText(resolution.getLoaiGiaApDung());
        detail.giaNenApDung = resolution.getGiaNenApDung();
        detail.phuThuApDung = resolution.getPhuThuApDung();
        detail.tongPhuThuApDung = resolution.getTongPhuThuApDung();
        if (resolution.getGiaApDung() > 0d) {
            detail.giaApDung = resolution.getGiaApDung();
        }
        if (resolution.getThanhTienApDung() > 0d) {
            detail.thanhTien = resolution.getThanhTienApDung();
        }
    }


    private List<RoomOption> loadRoomOptions(LocalDateTime checkIn, LocalDateTime checkOut, Integer includeRoomId, Integer excludeBookingId,
                                             List<BookingDetailRecord> currentRows, BookingDetailRecord currentDetail) {
        List<RoomOption> options = new ArrayList<RoomOption>();
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return options;
        }
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return options;
        }
        String sql = "SELECT p.maPhong, p.soPhong, p.tang, p.trangThai, p.sucChuaToiDa, lp.maLoaiPhong, lp.tenLoaiPhong, lp.giaThamChieu " +
                "FROM Phong p JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "WHERE p.trangThai IN (N'Hoạt động', N'Trống', N'Sẵn sàng') OR p.maPhong = ? " +
                "ORDER BY TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, includeRoomId == null ? -1 : includeRoomId.intValue());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomOption option = new RoomOption();
                    option.maPhongId = rs.getInt("maPhong");
                    option.soPhong = safeValue(rs.getString("soPhong"), String.valueOf(option.maPhongId));
                    option.tang = safeValue(rs.getString("tang"), "-");
                    option.trangThai = safeValue(rs.getString("trangThai"), "Hoạt động");
                    option.maLoaiPhong = rs.getInt("maLoaiPhong");
                    option.tenLoaiPhong = safeValue(rs.getString("tenLoaiPhong"), "-");
                    option.sucChuaToiDa = rs.getInt("sucChuaToiDa");
                    option.giaMacDinh = rs.getDouble("giaThamChieu");
                    options.add(option);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<DatPhongDAO.AvailableRoomInfo> availableRooms = datPhongDAO.getAvailableRooms(checkIn, checkOut, excludeBookingId, includeRoomId);
        List<RoomOption> filteredOptions = new ArrayList<RoomOption>();
        for (DatPhongDAO.AvailableRoomInfo room : availableRooms) {
            if (room == null || room.getMaPhong() <= 0 || isRoomAssignedInOtherDetail(currentRows, room.getMaPhong(), currentDetail)) {
                continue;
            }
            boolean isIncludedRoom = includeRoomId != null && room.getMaPhong() == includeRoomId.intValue();
            String roomStatus = safeValue(room.getTrangThai(), "Hoạt động");
            boolean isSelectableStatus = "Hoạt động".equalsIgnoreCase(roomStatus)
                    || "Trống".equalsIgnoreCase(roomStatus)
                    || "Sẵn sàng".equalsIgnoreCase(roomStatus);
            if (!isSelectableStatus && !isIncludedRoom) {
                continue;
            }
            RoomOption option = findRoomOptionById(options, room.getMaPhong());
            if (option == null) {
                option = new RoomOption();
                option.maPhongId = room.getMaPhong();
                option.soPhong = safeValue(room.getSoPhong(), String.valueOf(option.maPhongId));
                option.tang = safeValue(room.getTang(), "-");
                option.trangThai = roomStatus;
                option.maLoaiPhong = room.getMaLoaiPhong();
                option.tenLoaiPhong = safeValue(room.getTenLoaiPhong(), "-");
                option.sucChuaToiDa = room.getSucChuaToiDa();
                option.giaMacDinh = room.getGiaThamChieu();
            }
            filteredOptions.add(option);
        }
        return filteredOptions;
    }

    private RoomOption findRoomOptionById(List<RoomOption> options, int maPhongId) {
        if (options == null || maPhongId <= 0) {
            return null;
        }
        for (RoomOption option : options) {
            if (option != null && option.maPhongId == maPhongId) {
                return option;
            }
        }
        return null;
    }

    private boolean isRoomAssignedInOtherDetail(List<BookingDetailRecord> currentRows, int maPhongId, BookingDetailRecord currentDetail) {
        if (maPhongId <= 0) {
            return false;
        }
        if (currentRows == null) {
            return false;
        }
        for (BookingDetailRecord row : currentRows) {
            if (row != null && row != currentDetail && row.maPhongId == maPhongId) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> getAssignedRoomIdsForBooking(Connection con, int maDatPhong) throws Exception {
        List<Integer> roomIds = new ArrayList<Integer>();
        try (PreparedStatement ps = con.prepareStatement("SELECT maPhong FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong IS NOT NULL")) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roomIds.add(rs.getInt(1));
                }
            }
        }
        return roomIds;
    }

    private void releaseAssignedRooms(Connection con, List<Integer> roomIds) throws Exception {
        refreshRoomStatuses(con, roomIds);
    }

    private void markRoomsBooked(Connection con, List<BookingDetailRecord> rows) throws Exception {
        refreshRoomStatuses(con, collectRoomIds(rows));
    }

    private void refreshRoomStatuses(Connection con, List<Integer> roomIds) throws Exception {
        datPhongDAO.refreshRoomStatuses(con, roomIds);
    }

    private List<Integer> collectRoomIds(List<BookingDetailRecord> rows) {
        List<Integer> roomIds = new ArrayList<Integer>();
        if (rows == null) {
            return roomIds;
        }
        for (BookingDetailRecord row : rows) {
            if (row != null && row.maPhongId > 0) {
                roomIds.add(Integer.valueOf(row.maPhongId));
            }
        }
        return roomIds;
    }

    private boolean bookingHasStay(Connection con, int maDatPhong) throws Exception {
        String sql = "SELECT COUNT(1) FROM LuuTru WHERE maDatPhong = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Integer findOpenBookingIdByCustomer(Connection con, int maKhachHang, Integer excludeMaDatPhong) throws Exception {
        // Booking availability is validated by room/time conflicts on detail rows,
        // not by limiting a customer to a single open booking.
        if (maKhachHang >= 0) {
            return null;
        }
        String sql = "SELECT TOP 1 maDatPhong FROM DatPhong " +
                "WHERE maKhachHang = ? " +
                "AND trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Chờ check-in') " +
                "AND (? IS NULL OR maDatPhong <> ?) " +
                "ORDER BY maDatPhong DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKhachHang);
            if (excludeMaDatPhong == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, excludeMaDatPhong.intValue());
                ps.setInt(3, excludeMaDatPhong.intValue());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
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
        List<DatPhongGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new ArrayList<DatPhongGUI>(OPEN_INSTANCES);
        }
        for (DatPhongGUI gui : snapshot) {
            if (gui != null) {
                javax.swing.SwingUtilities.invokeLater(() -> gui.reloadSampleData(false));
            }
        }
    }

    private static synchronized String getPendingStatusFilter() {
        return pendingStatusFilter;
    }

    public static synchronized void prepareFocusOnBooking(int maDatPhong) {
        pendingFocusedBookingId = maDatPhong > 0 ? Integer.valueOf(maDatPhong) : null;
    }

    public static synchronized void prepareFocusOnCancelledBooking(int maDatPhong) {
        prepareFocusOnBooking(maDatPhong);
        pendingStatusFilter = DatPhongDAO.STATUS_CANCELLED;
    }

    private abstract class BaseBookingDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseBookingDialog(Window owner, String title, int width, int height) {
            super(ScreenUIHelper.resolveWindowOwner(owner), title, Dialog.ModalityType.APPLICATION_MODAL);
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

    private final class BookingEditorDialog extends BaseBookingDialog {
        private final BookingRecord editingBooking;
        private final boolean editing;
        private final List<BookingDetailRecord> detailRows = new ArrayList<BookingDetailRecord>();

        private AppDatePickerField txtNgayDatDialog;
        private JComboBox<String> cboNguonBookingDialog;
        private JComboBox<CustomerLookup> cboKhachHangDialog;
        private JTextField txtCccdDialog;
        private JTextField txtHoTen;
        private JTextField txtSdt;
        private JTextField txtNgaySinhKhach;
        private JTextField txtEmailKhach;
        private JTextArea txtDiaChiKhach;
        private JTextField txtTongSoNguoiDialog;
        private JTextField txtTongDatCocDialog;
        private JTextArea txtGhiChuDialog;
        private AppDatePickerField txtDetailCheckInDate;
        private AppTimePickerField txtDetailCheckInTime;
        private AppDatePickerField txtDetailCheckOutDate;
        private AppTimePickerField txtDetailCheckOutTime;
        private JTable tblAvailableRoomsDialog;
        private JTable tblBookingDetailDialog;
        private AbstractTableModel availableRoomTableModel;
        private AbstractTableModel bookingDetailDialogModel;
        private final List<RoomOption> availableRoomOptions = new ArrayList<RoomOption>();
        private JLabel lblDetailSummary;
        private JLabel lblSuggestedDeposit;
        private JScrollPane bookingDetailScrollPane;
        private JPanel pnlConflictWarning;
        private JLabel lblConflictWarning;
        private final List<JButton> submitButtons = new ArrayList<JButton>();
        private Border defaultDetailTableBorder;
        private BookingDetailRecord highlightedConflictRow;
        private boolean syncingSharedSchedule;
        private boolean reloadingCustomerOptions;
        private BookingEditorDialog(Window owner, BookingRecord booking) {
            super(owner, booking == null ? "Tạo booking" : "Cập nhật booking", 1420, 860);
            this.editingBooking = booking;
            this.editing = booking != null;
            if (editing) {
                for (BookingDetailRecord detail : booking.details) {
                    detailRows.add(detail.copy());
                }
            }

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildBookingEditorHeader(
                    editing ? "CẬP NHẬT BOOKING" : "TẠO BOOKING",
                    editing
                            ? "Booking đã lưu trong SQL. Chỉ cập nhật khi booking chưa check-in."
                            : "Nhập thông tin khách hàng, chọn khoảng thời gian, thêm phòng trống vào danh sách phòng đã chọn rồi lưu booking."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            JPanel headerSection = buildHeaderSection();
            JPanel detailSection = buildDetailSection();
            headerSection.setMinimumSize(new Dimension(455, 1));
            headerSection.setPreferredSize(new Dimension(480, 1));
            detailSection.setMinimumSize(new Dimension(780, 1));
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, headerSection, detailSection);
            splitPane.setBorder(null);
            splitPane.setResizeWeight(0.36d);
            splitPane.setDividerLocation(480);
            splitPane.setContinuousLayout(true);
            body.add(splitPane, BorderLayout.CENTER);
            content.add(body, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            if (editing) {
                JButton btnUpdate = createPrimaryButton("Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit("update"));
                configureBookingFooterButton(btnCancel);
                configureBookingFooterButton(btnUpdate);
                submitButtons.add(btnUpdate);
                content.add(buildBookingDialogButtons(btnCancel, btnUpdate), BorderLayout.SOUTH);
            } else {
                JButton btnSaveConfirm = createOutlineButton("Lưu và xác nhận", new Color(59, 130, 246), e -> submit("confirm"));
                JButton btnSaveCheckIn = createPrimaryButton("Lưu và check-in", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit("checkin"));
                configureBookingFooterButton(btnCancel);
                configureBookingFooterButton(btnSaveConfirm);
                configureBookingFooterButton(btnSaveCheckIn);
                submitButtons.add(btnSaveConfirm);
                submitButtons.add(btnSaveCheckIn);
                content.add(buildBookingDialogButtons(btnCancel, btnSaveConfirm, btnSaveCheckIn), BorderLayout.SOUTH);
            }
            add(content, BorderLayout.CENTER);
            reevaluateDetailValidationState(false);
        }

        private JPanel buildBookingEditorHeader(String title, String subtitle) {
            JPanel panel = createDialogCardPanel();
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                    new EmptyBorder(16, 18, 16, 18)
            ));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
            lblTitle.setForeground(TEXT_PRIMARY);
            lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblSub = new JLabel("<html><div style='width:980px; line-height:1.35;'>" + subtitle + "</div></html>");
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblSub.setForeground(TEXT_MUTED);
            lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

            content.add(lblTitle);
            content.add(Box.createVerticalStrut(8));
            content.add(lblSub);
            panel.add(content, BorderLayout.CENTER);
            return panel;
        }

        private JPanel buildBookingDialogButtons(JButton... buttons) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
            panel.setOpaque(false);
            panel.setBorder(new EmptyBorder(2, 0, 0, 0));
            for (JButton button : buttons) {
                panel.add(button);
            }
            return panel;
        }

        private void configureBookingFooterButton(JButton button) {
            button.setPreferredSize(new Dimension(Math.max(132, button.getPreferredSize().width), 38));
            button.setMinimumSize(new Dimension(120, 38));
        }

        private JPanel buildHeaderSectionLegacy() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);
            JLabel lblSection = new JLabel("HEADER - THÔNG TIN CHUNG");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            CustomerLookup existingCustomer = editing ? loadCustomerLookup(editingBooking.maKhachHang) : null;

            txtNgayDatDialog = new AppDatePickerField(editing && editingBooking.ngayDat != null ? editingBooking.formatNgayDat() : LocalDate.now().format(DATE_FORMAT), true);
            cboNguonBookingDialog = createComboBox(new String[]{"Đặt trước", "Walk-in"});
            cboKhachHangDialog = new JComboBox<CustomerLookup>();
            cboKhachHangDialog.setFont(BODY_FONT);
            cboKhachHangDialog.setPreferredSize(new Dimension(240, 34));
            cboKhachHangDialog.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            JButton btnAddCustomer = createOutlineButton("+", new Color(37, 99, 235), e -> openCreateCustomerDialog());
            btnAddCustomer.setPreferredSize(new Dimension(44, 34));
            btnAddCustomer.setToolTipText("Thêm khách hàng mới");

            JPanel customerSelectorPanel = new JPanel(new BorderLayout(8, 0));
            customerSelectorPanel.setOpaque(false);
            customerSelectorPanel.add(cboKhachHangDialog, BorderLayout.CENTER);
            customerSelectorPanel.add(btnAddCustomer, BorderLayout.EAST);

            txtCccdDialog = createInputField("");
            txtHoTen = createInputField("");
            txtSdt = createInputField("");
            txtNgaySinhKhach = createInputField("");
            txtNgayDatDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
            txtNgaySinhKhach.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
            txtEmailKhach = createInputField("");
            txtDiaChiKhach = createDialogTextArea(3);
            configureReadonlyCustomerFields();
            txtTongDatCocDialog = createInputField(editing ? formatMoney(editingBooking.tongTienDatCoc) : "0");
            txtTongDatCocDialog.setEditable(false);
            txtGhiChuDialog = createDialogTextArea(3);

            if (editing) {
                cboNguonBookingDialog.setSelectedItem(editingBooking.nguonDat);
                txtGhiChuDialog.setText(editingBooking.ghiChu);
            }

            cboKhachHangDialog.addActionListener(e -> {
                if (!reloadingCustomerOptions) {
                    applySelectedCustomerToHeader((CustomerLookup) cboKhachHangDialog.getSelectedItem());
                }
            });
            reloadCustomerOptions(editing ? Integer.valueOf(editingBooking.maKhachHang) : null);

            addFormRow(form, gbc, 0, "Ngày đặt", txtNgayDatDialog);
            addFormRow(form, gbc, 1, "Nguồn booking", cboNguonBookingDialog);
            addFormRow(form, gbc, 2, "Khách hàng", customerSelectorPanel);
            addFormRow(form, gbc, 3, "Họ tên KH", txtHoTen);
            addFormRow(form, gbc, 4, "SĐT", txtSdt);
            addFormRow(form, gbc, 5, "Ngày sinh", txtNgaySinhKhach);
            addFormRow(form, gbc, 6, "Email", txtEmailKhach);
            addFormRow(form, gbc, 7, "Địa chỉ", new JScrollPane(txtDiaChiKhach));
            addFormRow(form, gbc, 8, "Tổng tiền cọc", txtTongDatCocDialog);
            addFormRow(form, gbc, 9, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            card.setPreferredSize(new Dimension(430, 0));
            card.setMinimumSize(new Dimension(400, 0));
            card.add(wrapper, BorderLayout.CENTER);
            return card;
        }


        private JPanel buildHeaderSection() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 12));
            wrapper.setOpaque(false);

            JLabel lblSection = new JLabel("Thông tin khách hàng / booking");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);
            lblSection.setBorder(new EmptyBorder(0, 0, 2, 0));

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(7, 0, 7, 0);
            gbc.anchor = GridBagConstraints.WEST;

            txtNgayDatDialog = new AppDatePickerField(
                    editing && editingBooking.ngayDat != null
                            ? editingBooking.formatNgayDat()
                            : LocalDate.now().format(DATE_FORMAT),
                    true
            );
            configureBookingEditorField(txtNgayDatDialog, 270, 34);
            txtNgayDatDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 03/03/2026");

            cboNguonBookingDialog = createComboBox(new String[]{"Đặt trước", "Walk-in"});
            configureBookingEditorField(cboNguonBookingDialog, 270, 34);
            cboKhachHangDialog = new JComboBox<CustomerLookup>();
            cboKhachHangDialog.setFont(BODY_FONT);
            configureBookingEditorField(cboKhachHangDialog, 270, 34);

            JButton btnAddCustomer = createOutlineButton("+", new Color(37, 99, 235), e -> openCreateCustomerDialog());
            btnAddCustomer.setPreferredSize(new Dimension(46, 34));
            btnAddCustomer.setMinimumSize(new Dimension(46, 34));
            btnAddCustomer.setToolTipText("Thêm khách hàng mới");

            JPanel customerSelectorPanel = new JPanel(new BorderLayout(8, 0));
            customerSelectorPanel.setOpaque(false);
            customerSelectorPanel.setPreferredSize(new Dimension(270, 34));
            customerSelectorPanel.setMinimumSize(new Dimension(0, 34));
            customerSelectorPanel.add(cboKhachHangDialog, BorderLayout.CENTER);
            customerSelectorPanel.add(btnAddCustomer, BorderLayout.EAST);

            txtCccdDialog = createInputField("");
            txtHoTen = createInputField("");
            txtSdt = createInputField("");
            txtNgaySinhKhach = createInputField("");
            txtEmailKhach = createInputField("");
            txtDiaChiKhach = createDialogTextArea(3);
            configureReadonlyCustomerFields();

            txtTongSoNguoiDialog = createInputField(String.valueOf(calculateTotalGuests()));
            configureReadonlyTextField(txtTongSoNguoiDialog);
            txtTongDatCocDialog = createInputField(editing ? formatMoney(editingBooking.tongTienDatCoc) : "0");
            txtTongDatCocDialog.setEditable(false);
            txtGhiChuDialog = createDialogTextArea(3);
            configureBookingEditorField(txtCccdDialog, 270, 34);
            configureBookingEditorField(txtHoTen, 270, 34);
            configureBookingEditorField(txtSdt, 270, 34);
            configureBookingEditorField(txtNgaySinhKhach, 270, 34);
            configureBookingEditorField(txtEmailKhach, 270, 34);
            configureBookingEditorField(txtTongSoNguoiDialog, 270, 34);
            configureBookingEditorField(txtTongDatCocDialog, 270, 34);

            if (editing) {
                cboNguonBookingDialog.setSelectedItem(editingBooking.nguonDat);
                txtGhiChuDialog.setText(editingBooking.ghiChu);
            }

            cboKhachHangDialog.addActionListener(e -> {
                if (!reloadingCustomerOptions) {
                    applySelectedCustomerToHeader((CustomerLookup) cboKhachHangDialog.getSelectedItem());
                }
            });
            reloadCustomerOptions(editing ? Integer.valueOf(editingBooking.maKhachHang) : null);

            addBookingFormRow(form, gbc, 0, "Ngày đặt", txtNgayDatDialog);
            addBookingFormRow(form, gbc, 1, "Nguồn booking", cboNguonBookingDialog);
            addBookingFormRow(form, gbc, 2, "Khách hàng", customerSelectorPanel);
            addBookingFormRow(form, gbc, 3, "CCCD/Passport", txtCccdDialog);
            addBookingFormRow(form, gbc, 4, "Họ tên KH", txtHoTen);
            addBookingFormRow(form, gbc, 5, "SĐT", txtSdt);
            addBookingFormRow(form, gbc, 6, "Ngày sinh", txtNgaySinhKhach);
            addBookingFormRow(form, gbc, 7, "Email", txtEmailKhach);
            addBookingFormRow(form, gbc, 8, "Địa chỉ", createBookingTextAreaScroll(txtDiaChiKhach, 72));
            addBookingFormRow(form, gbc, 9, "Tổng số người", txtTongSoNguoiDialog);
            addBookingFormRow(form, gbc, 10, "Tổng tiền cọc", txtTongDatCocDialog);
            addBookingFormRow(form, gbc, 11, "Ghi chú", createBookingTextAreaScroll(txtGhiChuDialog, 78));

            wrapper.add(lblSection, BorderLayout.NORTH);
            JScrollPane formScroll = new JScrollPane(form);
            formScroll.setBorder(BorderFactory.createEmptyBorder());
            formScroll.setOpaque(false);
            formScroll.getViewport().setOpaque(false);
            formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            formScroll.getVerticalScrollBar().setUnitIncrement(16);
            wrapper.add(formScroll, BorderLayout.CENTER);
            card.setPreferredSize(new Dimension(480, 0));
            card.setMinimumSize(new Dimension(455, 0));
            card.add(wrapper, BorderLayout.CENTER);
            return card;
        }

        private void configureBookingEditorField(JComponent component, int width, int height) {
            component.setPreferredSize(new Dimension(width, height));
            component.setMinimumSize(new Dimension(0, height));
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        }

        private JScrollPane createBookingTextAreaScroll(JTextArea area, int height) {
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setPreferredSize(new Dimension(270, height));
            scrollPane.setMinimumSize(new Dimension(0, height));
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(14);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            return scrollPane;
        }

        private void addBookingFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component component) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lbl = new JLabel(label + ":");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setForeground(TEXT_MUTED);
            lbl.setPreferredSize(new Dimension(126, 24));
            lbl.setBorder(new EmptyBorder(0, 0, 0, 8));
            panel.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(component, gbc);
        }

        private void configureReadonlyCustomerFields() {
            configureReadonlyTextField(txtCccdDialog);
            configureReadonlyTextField(txtHoTen);
            configureReadonlyTextField(txtSdt);
            configureReadonlyTextField(txtNgaySinhKhach);
            configureReadonlyTextField(txtEmailKhach);
            txtDiaChiKhach.setEditable(false);
            txtDiaChiKhach.setBackground(PANEL_SOFT);
            txtDiaChiKhach.setForeground(TEXT_PRIMARY);
        }

        private void configureReadonlyTextField(JTextField field) {
            field.setEditable(false);
            field.setBackground(PANEL_SOFT);
            field.setForeground(TEXT_PRIMARY);
        }

        private void reloadCustomerOptions(Integer preferredCustomerId) {
            int selectedCustomerId = preferredCustomerId == null ? getSelectedCustomerId() : preferredCustomerId.intValue();
            List<CustomerLookup> customers = loadCustomerOptions(selectedCustomerId > 0 ? Integer.valueOf(selectedCustomerId) : null);
            CustomerLookup selectedOption = createCustomerPlaceholderOption();

            reloadingCustomerOptions = true;
            cboKhachHangDialog.removeAllItems();
            cboKhachHangDialog.addItem(selectedOption);
            for (CustomerLookup customer : customers) {
                cboKhachHangDialog.addItem(customer);
                if (customer.maKhachHang == selectedCustomerId) {
                    selectedOption = customer;
                }
            }
            cboKhachHangDialog.setSelectedItem(selectedOption);
            reloadingCustomerOptions = false;
            applySelectedCustomerToHeader(selectedOption);
        }

        private int getSelectedCustomerId() {
            CustomerLookup selectedCustomer = (CustomerLookup) cboKhachHangDialog.getSelectedItem();
            return selectedCustomer == null ? 0 : selectedCustomer.maKhachHang;
        }

        private void applySelectedCustomerToHeader(CustomerLookup customer) {
            if (customer == null || customer.maKhachHang <= 0) {
                clearSelectedCustomerInfo();
                return;
            }
            txtCccdDialog.setText(safeValue(customer.cccdPassport, ""));
            txtHoTen.setText(safeValue(customer.hoTen, ""));
            txtSdt.setText(safeValue(customer.soDienThoai, ""));
            txtNgaySinhKhach.setText(customer.ngaySinh == null ? "" : customer.ngaySinh.format(DATE_FORMAT));
            txtEmailKhach.setText(safeValue(customer.email, ""));
            txtDiaChiKhach.setText(safeValue(customer.diaChi, ""));
            txtDiaChiKhach.setCaretPosition(0);
        }

        private void clearSelectedCustomerInfo() {
            txtCccdDialog.setText("");
            txtHoTen.setText("");
            txtSdt.setText("");
            txtNgaySinhKhach.setText("");
            txtEmailKhach.setText("");
            txtDiaChiKhach.setText("");
        }

        private void openCreateCustomerDialog() {
            CustomerQuickCreateDialog dialog = new CustomerQuickCreateDialog(this);
            dialog.setVisible(true);
            Integer createdCustomerId = dialog.getCreatedCustomerId();
            if (createdCustomerId != null && createdCustomerId.intValue() > 0) {
                reloadCustomerOptions(createdCustomerId);
                showSuccess("Đã thêm khách hàng mới và có thể chọn ngay cho booking.");
            }
        }

        private boolean isCustomerEmailValid(String email) {
            if (email == null || email.trim().isEmpty()) {
                return true;
            }
            String trimmed = email.trim();
            return trimmed.contains("@") && trimmed.indexOf('@') > 0 && trimmed.indexOf('@') < trimmed.length() - 1;
        }

        private boolean isCustomerPassportValueValid(String value) {
            if (value == null || value.trim().isEmpty()) {
                return true;
            }
            String trimmed = value.trim();
            return trimmed.length() >= 8 && trimmed.length() <= 20;
        }

        private Integer insertCustomerFromBooking(String hoTen, String soDienThoai, String cccdPassport,
                                                  LocalDate ngaySinh, String email, String diaChi) {
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return null;
            }

            Integer existingCustomerId = findExistingCustomerByPhoneOrPassport(con, soDienThoai, cccdPassport);
            if (existingCustomerId != null) {
                showError("Số điện thoại hoặc CCCD/Passport đã tồn tại. Vui lòng chọn khách hàng có sẵn trong combobox.");
                return null;
            }

            String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) "
                    + "VALUES (?, N'Khác', ?, ?, ?, ?, ?, N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', ?, N'Tạo từ màn đặt phòng')";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, hoTen);
                if (ngaySinh == null) {
                    ps.setNull(2, java.sql.Types.DATE);
                } else {
                    ps.setDate(2, Date.valueOf(ngaySinh));
                }
                ps.setString(3, soDienThoai);
                ps.setString(4, email == null ? "" : email.trim());
                ps.setString(5, cccdPassport == null ? "" : cccdPassport.trim());
                ps.setString(6, diaChi == null ? "" : diaChi.trim());
                ps.setString(7, username);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return Integer.valueOf(rs.getInt(1));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Không thể thêm khách hàng.");
            }
            return null;
        }

        private final class CustomerQuickCreateDialog extends BaseBookingDialog {
            private JTextField txtHoTenDialog;
            private JTextField txtSoDienThoaiDialog;
            private JTextField txtCccdCustomerDialog;
            private AppDatePickerField txtNgaySinhDialog;
            private JTextField txtEmailDialog;
            private JTextArea txtDiaChiDialog;
            private Integer createdCustomerId;

            private CustomerQuickCreateDialog(Window owner) {
                super(owner, "Thêm khách hàng", 640, 520);

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader(
                        "THÊM KHÁCH HÀNG",
                        "Khách hàng mới sẽ được lưu vào bảng KhachHang và nạp lại ngay ở combobox booking."
                ), BorderLayout.NORTH);

                JPanel form = createDialogFormPanel();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new java.awt.Insets(6, 0, 6, 12);
                gbc.anchor = GridBagConstraints.WEST;

                txtHoTenDialog = createInputField("");
                txtSoDienThoaiDialog = createInputField("");
                txtCccdCustomerDialog = createInputField("");
                txtNgaySinhDialog = new AppDatePickerField("", false);
                txtEmailDialog = createInputField("");
                txtDiaChiDialog = createDialogTextArea(3);

                addFormRow(form, gbc, 0, "Họ tên", txtHoTenDialog);
                addFormRow(form, gbc, 1, "SĐT", txtSoDienThoaiDialog);
                addFormRow(form, gbc, 2, "CCCD/Passport", txtCccdCustomerDialog);
                addFormRow(form, gbc, 3, "Ngày sinh", txtNgaySinhDialog);
                addFormRow(form, gbc, 4, "Email", txtEmailDialog);
                addFormRow(form, gbc, 5, "Địa chỉ", new JScrollPane(txtDiaChiDialog));

                JPanel card = createDialogCardPanel();
                card.add(form, BorderLayout.CENTER);
                content.add(card, BorderLayout.CENTER);

                JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
                JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit());
                content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
            }

            private void submit() {
                String hoTen = txtHoTenDialog.getText().trim();
                String soDienThoai = txtSoDienThoaiDialog.getText().trim();
                String cccdPassport = txtCccdCustomerDialog.getText().trim();
                String email = txtEmailDialog.getText().trim();

                if (hoTen.isEmpty()) {
                    showError("Họ tên bắt buộc nhập.");
                    return;
                }
                if (soDienThoai.isEmpty()) {
                    showError("Số điện thoại bắt buộc nhập.");
                    return;
                }
                if (!isCustomerPassportValueValid(cccdPassport)) {
                    showError("CCCD/Passport không hợp lệ.");
                    return;
                }
                if (!isCustomerEmailValid(email)) {
                    showError("Email không hợp lệ.");
                    return;
                }

                LocalDate ngaySinh = null;
                String ngaySinhText = txtNgaySinhDialog.getText() == null ? "" : txtNgaySinhDialog.getText().trim();
                if (!ngaySinhText.isEmpty()) {
                    ngaySinh = normalizeDateFieldValue(txtNgaySinhDialog, "Ngày sinh không hợp lệ.");
                    if (ngaySinh == null) {
                        return;
                    }
                }

                Integer newCustomerId = insertCustomerFromBooking(
                        hoTen,
                        soDienThoai,
                        cccdPassport,
                        ngaySinh,
                        email,
                        txtDiaChiDialog.getText().trim()
                );
                if (newCustomerId == null) {
                    return;
                }

                createdCustomerId = newCustomerId;
                KhachHangGUI.refreshAllOpenInstances();
                dispose();
            }

            private Integer getCreatedCustomerId() {
                return createdCustomerId;
            }
        }


        private void lookupCustomerByPassportOrCccd() {
            String cccd = txtCccdDialog.getText() == null ? "" : txtCccdDialog.getText().trim();
            if (cccd.isEmpty()) {
                return;
            }
            CustomerLookup lookup = findCustomerByPassportOrCccd(cccd);
            if (lookup == null) {
                txtHoTen.setText("");
                txtSdt.setText("");
                txtNgaySinhKhach.setText("");
                txtEmailKhach.setText("");
                txtDiaChiKhach.setText("");
                showWarning("Chưa có thông tin khách hàng với CCCD/Passport này. Vui lòng nhập thông tin khách hàng và nhấn xác nhận.");
                txtHoTen.requestFocusInWindow();
                return;
            }
            txtHoTen.setText(lookup.hoTen);
            txtSdt.setText(lookup.soDienThoai);
            txtCccdDialog.setText(lookup.cccdPassport);
            txtNgaySinhKhach.setText(lookup.ngaySinh == null ? "" : lookup.ngaySinh.format(DATE_FORMAT));
            txtEmailKhach.setText(lookup.email);
            txtDiaChiKhach.setText(lookup.diaChi);
            showSuccess("Đã tìm thấy khách hàng trong danh sách khách hàng.");
        }

        private JPanel buildDetailSection() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 12));
            wrapper.setOpaque(false);
            JLabel lblSection = new JLabel("Thông tin phòng");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);
            lblSection.setBorder(new EmptyBorder(0, 0, 2, 0));

            JPanel topInfoPanel = buildRoomInfoPanel();

            availableRoomTableModel = createAvailableRoomTableModel();
            tblAvailableRoomsDialog = new JTable(availableRoomTableModel);
            configureAvailableRoomsTable();

            bookingDetailDialogModel = createSelectedRoomTableModel();
            tblBookingDetailDialog = new JTable(bookingDetailDialogModel);
            configureSelectedRoomsTable();

            JScrollPane availableRoomScrollPane = new JScrollPane(tblAvailableRoomsDialog);
            availableRoomScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            availableRoomScrollPane.setPreferredSize(new Dimension(0, 335));
            availableRoomScrollPane.setMinimumSize(new Dimension(320, 260));
            availableRoomScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            availableRoomScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            availableRoomScrollPane.getVerticalScrollBar().setUnitIncrement(16);

            bookingDetailScrollPane = new JScrollPane(tblBookingDetailDialog);
            bookingDetailScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            bookingDetailScrollPane.setPreferredSize(new Dimension(0, 335));
            bookingDetailScrollPane.setMinimumSize(new Dimension(480, 260));
            bookingDetailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            bookingDetailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            bookingDetailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
            defaultDetailTableBorder = bookingDetailScrollPane.getBorder();

            lblDetailSummary = new JLabel();
            lblDetailSummary.setFont(BODY_FONT);
            lblDetailSummary.setForeground(TEXT_MUTED);

            pnlConflictWarning = new JPanel(new BorderLayout());
            pnlConflictWarning.setBackground(CONFLICT_BG);
            pnlConflictWarning.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            lblConflictWarning = new JLabel();
            lblConflictWarning.setFont(BODY_FONT);
            lblConflictWarning.setForeground(CONFLICT_TEXT);
            pnlConflictWarning.add(lblConflictWarning, BorderLayout.CENTER);
            pnlConflictWarning.setVisible(false);

            JPanel availableTablePanel = buildRoomTablePanel(
                    "Danh sách phòng trống",
                    "Thêm nhanh phòng khả dụng.",
                    availableRoomScrollPane
            );
            JPanel selectedTablePanel = buildRoomTablePanel(
                    "Phòng đã chọn",
                    "Các phòng sẽ lưu trong booking.",
                    bookingDetailScrollPane
            );
            availableTablePanel.setMinimumSize(new Dimension(320, 1));
            availableTablePanel.setPreferredSize(new Dimension(345, 1));
            selectedTablePanel.setMinimumSize(new Dimension(500, 1));
            JSplitPane centerSplitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    availableTablePanel,
                    selectedTablePanel
            );
            centerSplitPane.setBorder(null);
            centerSplitPane.setResizeWeight(0.40d);
            centerSplitPane.setDividerLocation(345);
            centerSplitPane.setContinuousLayout(true);
            centerSplitPane.setDividerSize(8);

            JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));
            bottomPanel.setOpaque(false);
            bottomPanel.add(lblDetailSummary, BorderLayout.NORTH);
            bottomPanel.add(pnlConflictWarning, BorderLayout.SOUTH);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(topInfoPanel, BorderLayout.NORTH);
            center.add(centerSplitPane, BorderLayout.CENTER);
            center.add(bottomPanel, BorderLayout.SOUTH);

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(center, BorderLayout.CENTER);
            card.setMinimumSize(new Dimension(780, 0));
            card.add(wrapper, BorderLayout.CENTER);
            refillBookingDetailDialogTable();
            return card;
        }


        private JPanel buildRoomInfoPanel() {
            LocalDateTime initialCheckIn = resolveInitialSharedCheckIn();
            LocalDateTime initialCheckOut = resolveInitialSharedCheckOut(initialCheckIn);

            txtDetailCheckInDate = new AppDatePickerField(initialCheckIn.toLocalDate().format(DATE_FORMAT), true);
            txtDetailCheckInTime = new AppTimePickerField(initialCheckIn.toLocalTime().format(TIME_FORMAT), false);
            txtDetailCheckOutDate = new AppDatePickerField(initialCheckOut.toLocalDate().format(DATE_FORMAT), true);
            txtDetailCheckOutTime = new AppTimePickerField(initialCheckOut.toLocalTime().format(TIME_FORMAT), false);
            txtDetailCheckInDate.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
            txtDetailCheckInTime.setToolTipText("Nhập giờ dạng HH:mm, ví dụ: 12:00");
            txtDetailCheckOutDate.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 4/3/26");
            txtDetailCheckOutTime.setToolTipText("Nhập giờ dạng HH:mm, ví dụ: 12:00");

            installDateFieldChangeListener(txtDetailCheckInDate, this::handleSharedScheduleChanged);
            installTimeFieldChangeListener(txtDetailCheckInTime, this::handleSharedScheduleChanged);
            installDateFieldChangeListener(txtDetailCheckOutDate, this::handleSharedScheduleChanged);
            installTimeFieldChangeListener(txtDetailCheckOutTime, this::handleSharedScheduleChanged);

            JLabel lblTitle = new JLabel("Khoảng thời gian áp dụng");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTitle.setForeground(TEXT_PRIMARY);

            JLabel lblSubtitle = new JLabel("Chọn ngày giờ chung để lọc phòng trống và tự tính số tiền cần đặt cọc.");
            lblSubtitle.setFont(BODY_FONT);
            lblSubtitle.setForeground(TEXT_MUTED);

            JPanel titlePanel = new JPanel(new BorderLayout(0, 4));
            titlePanel.setOpaque(false);
            titlePanel.add(lblTitle, BorderLayout.NORTH);
            titlePanel.add(lblSubtitle, BorderLayout.SOUTH);

            configureBookingEditorField(txtDetailCheckInDate, 170, 34);
            configureBookingEditorField(txtDetailCheckInTime, 150, 34);
            configureBookingEditorField(txtDetailCheckOutDate, 170, 34);
            configureBookingEditorField(txtDetailCheckOutTime, 150, 34);

            JPanel fields = new JPanel(new GridLayout(2, 2, 14, 10));
            fields.setOpaque(false);
            fields.add(createFieldGroup("Ngày bắt đầu", txtDetailCheckInDate));
            fields.add(createFieldGroup("Giờ bắt đầu", txtDetailCheckInTime));
            fields.add(createFieldGroup("Ngày kết thúc", txtDetailCheckOutDate));
            fields.add(createFieldGroup("Giờ kết thúc", txtDetailCheckOutTime));

            lblSuggestedDeposit = new JLabel();
            lblSuggestedDeposit.setFont(BODY_FONT);
            lblSuggestedDeposit.setForeground(TEXT_MUTED);

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(14, 14, 14, 14)
            ));
            panel.add(titlePanel, BorderLayout.NORTH);
            panel.add(fields, BorderLayout.CENTER);
            panel.add(lblSuggestedDeposit, BorderLayout.SOUTH);
            return panel;
        }

        private JPanel buildRoomTablePanel(String title, String subtitle, JScrollPane tableScrollPane) {
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setForeground(TEXT_PRIMARY);

            JLabel lblSubtitle = new JLabel(subtitle);
            lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblSubtitle.setForeground(TEXT_MUTED);

            JPanel header = new JPanel(new BorderLayout(0, 4));
            header.setOpaque(false);
            header.add(lblTitle, BorderLayout.NORTH);
            header.add(lblSubtitle, BorderLayout.SOUTH);

            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(PANEL_SOFT);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            panel.add(header, BorderLayout.NORTH);
            panel.add(tableScrollPane, BorderLayout.CENTER);
            return panel;
        }

        private AbstractTableModel createAvailableRoomTableModel() {
            return new AbstractTableModel() {
                private final String[] columns = {"Số phòng", "Loại phòng", "Sức chứa", "Thêm"};

                @Override
                public int getRowCount() {
                    return availableRoomOptions.size();
                }

                @Override
                public int getColumnCount() {
                    return columns.length;
                }

                @Override
                public String getColumnName(int column) {
                    return columns[column];
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    RoomOption option = availableRoomOptions.get(rowIndex);
                    switch (columnIndex) {
                        case 0:
                            return safeValue(option.soPhong, String.valueOf(option.maPhongId));
                        case 1:
                            return safeValue(option.tenLoaiPhong, "-");
                        case 2:
                            return option.sucChuaToiDa <= 0 ? "-" : String.valueOf(option.sucChuaToiDa);
                        case 3:
                            return "+";
                        default:
                            return "";
                    }
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnIndex == 3;
                }
            };
        }

        private AbstractTableModel createSelectedRoomTableModelLegacy() {
            return new AbstractTableModel() {
                private final String[] columns = {"Số phòng", "Số người", "Ngày bắt đầu", "Ngày kết thúc", "Xóa"};

                @Override
                public int getRowCount() {
                    return detailRows.size();
                }

                @Override
                public int getColumnCount() {
                    return columns.length;
                }

                @Override
                public String getColumnName(int column) {
                    return columns[column];
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    BookingDetailRecord detail = detailRows.get(rowIndex);
                    switch (columnIndex) {
                        case 0:
                            return safeValue(detail.maPhong, detail.maPhongId <= 0 ? "Chưa gán" : String.valueOf(detail.maPhongId));
                        case 1:
                            return String.valueOf(detail.soNguoi);
                        case 2:
                            return formatDateTime(detail.checkInDuKien);
                        case 3:
                            return formatDateTime(detail.checkOutDuKien);
                        case 4:
                            return "Xóa";
                        default:
                            return "";
                    }
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnIndex == 1 || columnIndex == 4;
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    if (rowIndex < 0 || rowIndex >= detailRows.size() || columnIndex != 1) {
                        return;
                    }
                    BookingDetailRecord detail = detailRows.get(rowIndex);
                    int soNguoiMoi;
                    try {
                        soNguoiMoi = Integer.parseInt(valueOf(aValue).trim());
                    } catch (NumberFormatException ex) {
                        showWarning("Số người phải là số nguyên hợp lệ.");
                        fireTableRowsUpdated(rowIndex, rowIndex);
                        return;
                    }
                    if (soNguoiMoi <= 0) {
                        showWarning("Số người phải lớn hơn 0.");
                        fireTableRowsUpdated(rowIndex, rowIndex);
                        return;
                    }
                    detail.soNguoi = soNguoiMoi;
                    reevaluateDetailValidationState(false);
                    fireTableRowsUpdated(rowIndex, rowIndex);
                }
            };
        }

        private AbstractTableModel createSelectedRoomTableModel() {
            return new AbstractTableModel() {
                private final String[] columns = {"Số phòng", "Số người ở", "Ngày bắt đầu", "Ngày kết thúc", "Xóa"};

                @Override
                public int getRowCount() {
                    return detailRows.size();
                }

                @Override
                public int getColumnCount() {
                    return columns.length;
                }

                @Override
                public String getColumnName(int column) {
                    return columns[column];
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    BookingDetailRecord detail = detailRows.get(rowIndex);
                    switch (columnIndex) {
                        case 0:
                            return safeValue(detail.maPhong, detail.maPhongId <= 0 ? "Chưa gán" : String.valueOf(detail.maPhongId));
                        case 1:
                            return String.valueOf(detail.soNguoi);
                        case 2:
                            return formatDateTime(detail.checkInDuKien);
                        case 3:
                            return formatDateTime(detail.checkOutDuKien);
                        case 4:
                            return "Xóa";
                        default:
                            return "";
                    }
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnIndex == 1 || columnIndex == 4;
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    if (rowIndex < 0 || rowIndex >= detailRows.size() || columnIndex != 1) {
                        return;
                    }
                    BookingDetailRecord detail = detailRows.get(rowIndex);
                    int soNguoiMoi;
                    try {
                        soNguoiMoi = Integer.parseInt(valueOf(aValue).trim());
                    } catch (NumberFormatException ex) {
                        showWarning("Số người ở của phòng " + safeValue(detail.maPhong, String.valueOf(detail.maPhongId)) + " phải là số nguyên hợp lệ.");
                        fireTableRowsUpdated(rowIndex, rowIndex);
                        return;
                    }

                    String guestError = validateGuestCount(detail, soNguoiMoi);
                    if (guestError != null) {
                        showWarning(guestError);
                        fireTableRowsUpdated(rowIndex, rowIndex);
                        return;
                    }

                    detail.soNguoi = soNguoiMoi;
                    refreshHeaderGuestSummary();
                    reevaluateDetailValidationState(false);
                    fireTableRowsUpdated(rowIndex, rowIndex);
                }
            };
        }

        private void configureAvailableRoomsTable() {
            tblAvailableRoomsDialog.setFont(BODY_FONT);
            tblAvailableRoomsDialog.setRowHeight(34);
            tblAvailableRoomsDialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblAvailableRoomsDialog.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            tblAvailableRoomsDialog.setFillsViewportHeight(true);
            tblAvailableRoomsDialog.getTableHeader().setPreferredSize(new Dimension(0, 34));
            ScreenUIHelper.styleTableHeader(tblAvailableRoomsDialog);
            tblAvailableRoomsDialog.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    component.setFont(BODY_FONT);
                    if (component instanceof JComponent) {
                        ((JComponent) component).setBorder(new EmptyBorder(0, 8, 0, 8));
                    }
                    component.setBackground(isSelected ? new Color(219, 234, 254) : Color.WHITE);
                    component.setForeground(TEXT_PRIMARY);
                    return component;
                }
            });
            applyAvailableRoomColumnWidths();
            tblAvailableRoomsDialog.getColumnModel().getColumn(3).setCellRenderer(new TableActionButtonRenderer(new Color(22, 163, 74), Color.WHITE));
            tblAvailableRoomsDialog.getColumnModel().getColumn(3).setCellEditor(new TableActionButtonEditor(
                    tblAvailableRoomsDialog,
                    new Color(22, 163, 74),
                    Color.WHITE,
                    row -> addRoomFromAvailableList(row)
            ));
        }

        private void configureSelectedRoomsTable() {
            tblBookingDetailDialog.setFont(BODY_FONT);
            tblBookingDetailDialog.setRowHeight(34);
            tblBookingDetailDialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblBookingDetailDialog.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            tblBookingDetailDialog.setToolTipText("Double click để cập nhật dòng chi tiết.");
            tblBookingDetailDialog.getTableHeader().setPreferredSize(new Dimension(0, 34));
            tblBookingDetailDialog.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() != 2 || tblBookingDetailDialog == null) {
                        return;
                    }
                    int row = tblBookingDetailDialog.rowAtPoint(e.getPoint());
                    if (row < 0 || row >= detailRows.size()) {
                        return;
                    }
                    tblBookingDetailDialog.setRowSelectionInterval(row, row);
                    editSelectedDetailRow();
                }
            });
            ScreenUIHelper.styleTableHeader(tblBookingDetailDialog);
            tblBookingDetailDialog.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    component.setFont(BODY_FONT);
                    if (component instanceof JComponent) {
                        ((JComponent) component).setBorder(new EmptyBorder(0, 8, 0, 8));
                    }
                    if (isSelected) {
                        component.setBackground(new Color(219, 234, 254));
                        component.setForeground(TEXT_PRIMARY);
                    } else if (isConflictDetailRow(row)) {
                        component.setBackground(CONFLICT_ROW_BG);
                        component.setForeground(TEXT_PRIMARY);
                    } else {
                        component.setBackground(Color.WHITE);
                        component.setForeground(TEXT_PRIMARY);
                    }
                    return component;
                }
            });
            applyBookingDetailColumnWidths();
            tblBookingDetailDialog.getColumnModel().getColumn(1).setCellEditor(createGuestCountCellEditor());
            tblBookingDetailDialog.getColumnModel().getColumn(4).setCellRenderer(new TableActionButtonRenderer(new Color(220, 38, 38), Color.WHITE));
            tblBookingDetailDialog.getColumnModel().getColumn(4).setCellEditor(new TableActionButtonEditor(
                    tblBookingDetailDialog,
                    new Color(220, 38, 38),
                    Color.WHITE,
                    row -> removeDetailRowAt(row)
            ));
        }

        private TableCellEditor createGuestCountCellEditor() {
            JTextField field = new JTextField();
            field.setFont(BODY_FONT);
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new DigitsOnlyDocumentFilter());
            return new DefaultCellEditor(field) {
                @Override
                public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                    JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                    editor.setText(valueOf(value));
                    editor.selectAll();
                    return editor;
                }

                @Override
                public Object getCellEditorValue() {
                    return valueOf(super.getCellEditorValue()).trim();
                }
            };
        }

        private String validateGuestCount(BookingDetailRecord detail, int soNguoi) {
            String roomLabel = safeValue(detail == null ? null : detail.maPhong, detail == null ? "" : String.valueOf(detail.maPhongId));
            if (soNguoi <= 0) {
                return "Số người ở của phòng " + roomLabel + " phải lớn hơn 0.";
            }
            if (detail != null && detail.sucChuaToiDa > 0 && soNguoi > detail.sucChuaToiDa) {
                return "Phòng " + roomLabel + " chỉ cho phép tối đa " + detail.sucChuaToiDa + " người.";
            }
            return null;
        }

        private String validateGuestCount(RoomOption option, int soNguoi) {
            BookingDetailRecord detail = new BookingDetailRecord();
            if (option != null) {
                detail.maPhong = option.soPhong;
                detail.maPhongId = option.maPhongId;
                detail.sucChuaToiDa = option.sucChuaToiDa;
            }
            return validateGuestCount(detail, soNguoi);
        }

        private LocalDateTime resolveInitialSharedCheckIn() {
            LocalDateTime result = null;
            for (BookingDetailRecord detail : detailRows) {
                LocalDateTime checkIn = normalizeLegacyDetailDateTime(detail.checkInDuKien);
                if (checkIn != null && (result == null || checkIn.isBefore(result))) {
                    result = checkIn;
                }
            }
            return result == null ? toDefaultDetailDateTime(LocalDate.now()) : result;
        }

        private LocalDateTime resolveInitialSharedCheckOut(LocalDateTime fallbackCheckIn) {
            LocalDateTime result = null;
            for (BookingDetailRecord detail : detailRows) {
                LocalDateTime checkOut = normalizeLegacyDetailDateTime(detail.checkOutDuKien);
                if (checkOut != null && (result == null || checkOut.isAfter(result))) {
                    result = checkOut;
                }
            }
            if (result == null) {
                return fallbackCheckIn.plusDays(1);
            }
            return result.isAfter(fallbackCheckIn) ? result : fallbackCheckIn.plusDays(1);
        }

        private void applyAvailableRoomColumnWidths() {
            if (tblAvailableRoomsDialog == null || tblAvailableRoomsDialog.getColumnModel().getColumnCount() < 4) {
                return;
            }
            int[] widths = {78, 150, 82, 58};
            int[] minWidths = {70, 120, 74, 52};
            int[] maxWidths = {95, Integer.MAX_VALUE, 98, 64};
            for (int i = 0; i < widths.length; i++) {
                javax.swing.table.TableColumn column = tblAvailableRoomsDialog.getColumnModel().getColumn(i);
                column.setPreferredWidth(widths[i]);
                column.setMinWidth(minWidths[i]);
                if (maxWidths[i] < Integer.MAX_VALUE) {
                    column.setMaxWidth(maxWidths[i]);
                }
            }
        }

        private void applyBookingDetailColumnWidths() {
            if (tblBookingDetailDialog == null || tblBookingDetailDialog.getColumnModel().getColumnCount() < 5) {
                return;
            }
            int[] widths = {82, 86, 145, 145, 58};
            int[] minWidths = {74, 78, 132, 132, 52};
            int[] maxWidths = {100, 105, Integer.MAX_VALUE, Integer.MAX_VALUE, 64};
            for (int i = 0; i < widths.length; i++) {
                javax.swing.table.TableColumn column = tblBookingDetailDialog.getColumnModel().getColumn(i);
                column.setPreferredWidth(widths[i]);
                column.setMinWidth(minWidths[i]);
                if (maxWidths[i] < Integer.MAX_VALUE) {
                    column.setMaxWidth(maxWidths[i]);
                }
            }
        }

        private void refillBookingDetailDialogTable() {
            if (bookingDetailDialogModel != null) {
                bookingDetailDialogModel.fireTableDataChanged();
            }
            reloadAvailableRoomOptions();
            refreshDepositSummary();
            reevaluateDetailValidationState(false);
        }

        private String getDetailStatusDisplay(BookingDetailRecord detail) {
            if (detail == null) {
                return "-";
            }
            if (detail.duplicateInBooking) {
                return "Trùng phòng";
            }
            if (detail.capacityExceeded) {
                return "Vuot suc chua";
            }
            if (detail.conflictInfo != null) {
                return "Xung dot lich";
            }
            return safeValue(detail.trangThaiChiTiet, "Da dat");
        }

        private void refreshDepositSummary() {
            double totalDeposit = 0;
            for (BookingDetailRecord detail : detailRows) {
                totalDeposit += detail.tienDatCocChiTiet;
            }
            refreshHeaderGuestSummary();
            txtTongDatCocDialog.setText(formatMoney(totalDeposit));
            if (lblSuggestedDeposit != null) {
                lblSuggestedDeposit.setText("Số tiền cần đặt cọc: " + formatMoney(calculateSuggestedDeposit()));
            }
            lblDetailSummary.setText(
                    "Đã chọn: " + detailRows.size()
                            + " phòng | Tạm tính: " + formatMoney(calculateSelectedRoomSubtotal())
                            + " | Tổng tiền cọc: " + formatMoney(totalDeposit)
            );
        }

        private int calculateTotalGuests() {
            return totalGuests(detailRows);
        }

        private void refreshHeaderGuestSummary() {
            if (txtTongSoNguoiDialog != null) {
                txtTongSoNguoiDialog.setText(String.valueOf(calculateTotalGuests()));
            }
        }

        private LocalDateTime resolveSharedCheckInSelection() {
            return combineDateTime(
                    parseDate(txtDetailCheckInDate == null ? null : txtDetailCheckInDate.getText()),
                    txtDetailCheckInTime == null ? null : txtDetailCheckInTime.getTimeValue()
            );
        }

        private LocalDateTime resolveSharedCheckOutSelection() {
            return combineDateTime(
                    parseDate(txtDetailCheckOutDate == null ? null : txtDetailCheckOutDate.getText()),
                    txtDetailCheckOutTime == null ? null : txtDetailCheckOutTime.getTimeValue()
            );
        }

        private String validateSharedScheduleSelection() {
            LocalDateTime checkIn = resolveSharedCheckInSelection();
            LocalDateTime checkOut = resolveSharedCheckOutSelection();
            if (checkIn == null || checkOut == null) {
                return "Vui lòng chọn ngày giờ bắt đầu/kết thúc hợp lệ để xem phòng trống.";
            }
            if (!checkOut.isAfter(checkIn)) {
                return "Giờ kết thúc phải lớn hơn giờ bắt đầu.";
            }
            return null;
        }

        private void handleSharedScheduleChanged() {
            if (syncingSharedSchedule) {
                return;
            }
            stopDetailTableEditing();
            String scheduleWarning = validateSharedScheduleSelection();
            if (scheduleWarning == null) {
                applySharedScheduleToSelectedRooms(resolveSharedCheckInSelection(), resolveSharedCheckOutSelection());
                applySuggestedDepositToDetails();
            }
            refillBookingDetailDialogTable();
        }

        private void stopDetailTableEditing() {
            if (tblBookingDetailDialog != null && tblBookingDetailDialog.isEditing()) {
                TableCellEditor editor = tblBookingDetailDialog.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }
            }
        }

        private void reloadAvailableRoomOptions() {
            availableRoomOptions.clear();
            LocalDateTime checkIn = resolveSharedCheckInSelection();
            LocalDateTime checkOut = resolveSharedCheckOutSelection();
            if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                availableRoomOptions.addAll(loadRoomOptions(
                        checkIn,
                        checkOut,
                        null,
                        editing ? Integer.valueOf(editingBooking.maDatPhong) : null,
                        detailRows,
                        null
                ));
            }
            if (availableRoomTableModel != null) {
                availableRoomTableModel.fireTableDataChanged();
            }
        }

        private void addRoomFromAvailableList(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= availableRoomOptions.size()) {
                return;
            }
            String scheduleWarning = validateSharedScheduleSelection();
            if (scheduleWarning != null) {
                showWarning(scheduleWarning);
                reevaluateDetailValidationState(false);
                return;
            }
            RoomOption option = availableRoomOptions.get(rowIndex);
            if (option == null) {
                return;
            }

            BookingDetailRecord detail = new BookingDetailRecord();
            detail.loaiPhong = option.tenLoaiPhong;
            detail.maLoaiPhong = option.maLoaiPhong;
            detail.maPhongId = option.maPhongId;
            detail.maPhong = option.soPhong;
            detail.checkInDuKien = resolveSharedCheckInSelection();
            detail.checkOutDuKien = resolveSharedCheckOutSelection();
            detail.soNguoi = resolveDefaultGuestCount(option);
            detail.sucChuaToiDa = option.sucChuaToiDa;
            detail.giaApDung = option.giaMacDinh;
            detail.trangThaiChiTiet = "Đã đặt";

            Connection con = ConnectDB.getConnection();
            refreshResolvedRateSafely(con, detail);
            detail.conflictInfo = findRoomConflict(detail);
            if (isRoomAssignedInOtherDetail(detailRows, detail.maPhongId, null)) {
                showWarning("Phòng " + safeValue(detail.maPhong, String.valueOf(detail.maPhongId)) + " đã có trong danh sách đã chọn.");
                refillBookingDetailDialogTable();
                return;
            }
            if (detail.conflictInfo != null) {
                showWarning(buildConflictMessage(detail.conflictInfo));
                refillBookingDetailDialogTable();
                return;
            }
            detailRows.add(detail);
            applySuggestedDepositToDetails();
            refillBookingDetailDialogTable();
            focusDetailRow(detail);
        }

        private int resolveDefaultGuestCount(RoomOption option) {
            return 1;
        }

        private void removeDetailRowAt(int rowIndex) {
            if (rowIndex < 0 || rowIndex >= detailRows.size()) {
                return;
            }
            detailRows.remove(rowIndex);
            applySuggestedDepositToDetails();
            refillBookingDetailDialogTable();
        }

        private void applySharedScheduleToSelectedRooms(LocalDateTime checkIn, LocalDateTime checkOut) {
            Connection con = ConnectDB.getConnection();
            for (BookingDetailRecord detail : detailRows) {
                if (detail == null) {
                    continue;
                }
                detail.checkInDuKien = checkIn;
                detail.checkOutDuKien = checkOut;
                refreshResolvedRateSafely(con, detail);
            }
        }

        private void refreshResolvedRateSafely(Connection con, BookingDetailRecord detail) {
            if (detail == null) {
                return;
            }
            try {
                if (con != null) {
                    refreshResolvedRate(con, detail);
                }
            } catch (Exception ex) {
                detail.loaiNgayApDung = "-";
                detail.loaiGiaApDung = "-";
                detail.giaNenApDung = 0d;
                detail.phuThuApDung = 0d;
                detail.tongPhuThuApDung = 0d;
                detail.thanhTien = 0d;
            }
        }

        private double calculateSelectedRoomSubtotal() {
            double total = 0d;
            for (BookingDetailRecord detail : detailRows) {
                total += Math.max(0d, detail == null ? 0d : detail.computeThanhTien());
            }
            return total;
        }

        private double calculateSuggestedDeposit() {
            return Math.max(0d, Math.round(calculateSelectedRoomSubtotal() * 0.3d));
        }

        private void applySuggestedDepositToDetails() {
            if (detailRows.isEmpty()) {
                return;
            }
            double subtotal = calculateSelectedRoomSubtotal();
            double remainingSubtotal = subtotal;
            double remainingDeposit = calculateSuggestedDeposit();
            for (int i = 0; i < detailRows.size(); i++) {
                BookingDetailRecord detail = detailRows.get(i);
                double roomTotal = Math.max(0d, detail.computeThanhTien());
                double assignedDeposit;
                if (i == detailRows.size() - 1) {
                    assignedDeposit = Math.max(0d, remainingDeposit);
                } else if (remainingSubtotal <= 0d) {
                    assignedDeposit = 0d;
                } else {
                    assignedDeposit = Math.max(0d, Math.round(remainingDeposit * (roomTotal / remainingSubtotal)));
                    assignedDeposit = Math.min(assignedDeposit, remainingDeposit);
                }
                detail.tienDatCocChiTiet = assignedDeposit;
                remainingDeposit -= assignedDeposit;
                remainingSubtotal -= roomTotal;
            }
        }

        private boolean isConflictDetailRow(int row) {
            if (row < 0 || row >= detailRows.size()) {
                return false;
            }
            BookingDetailRecord detail = detailRows.get(row);
            return detail != null && (detail.conflictInfo != null || detail.duplicateInBooking || detail.capacityExceeded);
        }

        private void reevaluateDetailValidationState(boolean focusConflictRow) {
            DatPhongConflictInfo firstConflict = null;
            highlightedConflictRow = null;
            String scheduleWarning = validateSharedScheduleSelection();

            for (int i = 0; i < detailRows.size(); i++) {
                BookingDetailRecord current = detailRows.get(i);
                current.duplicateInBooking = false;
                current.capacityExceeded = current.sucChuaToiDa > 0 && current.soNguoi > current.sucChuaToiDa;
                current.conflictInfo = findRoomConflict(current);
            }

            for (int i = 0; i < detailRows.size(); i++) {
                BookingDetailRecord current = detailRows.get(i);
                for (int j = i + 1; j < detailRows.size(); j++) {
                    BookingDetailRecord other = detailRows.get(j);
                    if (current.maPhongId > 0 && current.maPhongId == other.maPhongId) {
                        current.duplicateInBooking = true;
                        other.duplicateInBooking = true;
                    }
                }
                if (highlightedConflictRow == null && (current.duplicateInBooking || current.capacityExceeded || current.conflictInfo != null)) {
                    highlightedConflictRow = current;
                    firstConflict = current.conflictInfo;
                }
            }

            String warningMessage = null;
            if (scheduleWarning != null) {
                warningMessage = scheduleWarning;
            } else if (detailRows.isEmpty()) {
                warningMessage = "Phiếu đặt phòng phải có ít nhất 1 phòng.";
            } else if (highlightedConflictRow != null) {
                if (highlightedConflictRow.duplicateInBooking) {
                    warningMessage = "Phong " + safeValue(highlightedConflictRow.maPhong, String.valueOf(highlightedConflictRow.maPhongId))
                            + " đang bị chọn trùng trong cùng phiếu đặt phòng.";
                } else if (highlightedConflictRow.capacityExceeded) {
                    warningMessage = "Phong " + safeValue(highlightedConflictRow.maPhong, String.valueOf(highlightedConflictRow.maPhongId))
                            + " chi nhan toi da " + highlightedConflictRow.sucChuaToiDa
                            + " khach, hien dang nhap " + highlightedConflictRow.soNguoi + ".";
                } else if (firstConflict != null) {
                    warningMessage = buildConflictMessage(firstConflict);
                }
            }

            applyDetailWarningState(warningMessage, focusConflictRow);
        }

        private void applyDetailWarningState(String warningMessage, boolean focusConflictRow) {
            boolean hasWarning = warningMessage != null && !warningMessage.trim().isEmpty();
            lblConflictWarning.setText(hasWarning
                    ? "<html>" + warningMessage + "<br/>Vui lòng chọn phòng khác hoặc đổi ngày nhận/trả phòng.</html>"
                    : "");
            pnlConflictWarning.setVisible(hasWarning);
            bookingDetailScrollPane.setBorder(hasWarning
                    ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true)
                    : defaultDetailTableBorder);
            updateSubmitButtonsState(!hasWarning);
            tblBookingDetailDialog.repaint();
            if (hasWarning && focusConflictRow && highlightedConflictRow != null) {
                focusDetailRow(highlightedConflictRow);
            }
        }

        private void updateSubmitButtonsState(boolean enabled) {
            for (JButton button : submitButtons) {
                button.setEnabled(enabled);
            }
        }

        private void focusDetailRow(BookingDetailRecord detail) {
            int rowIndex = detailRows.indexOf(detail);
            if (rowIndex >= 0 && rowIndex < tblBookingDetailDialog.getRowCount()) {
                tblBookingDetailDialog.setRowSelectionInterval(rowIndex, rowIndex);
                tblBookingDetailDialog.scrollRectToVisible(tblBookingDetailDialog.getCellRect(rowIndex, 0, true));
                tblBookingDetailDialog.requestFocusInWindow();
            }
        }

        private DatPhongConflictInfo findRoomConflict(BookingDetailRecord detail) {
            if (detail == null || detail.maPhongId <= 0 || detail.checkInDuKien == null || detail.checkOutDuKien == null
                    || !detail.checkOutDuKien.isAfter(detail.checkInDuKien)) {
                return null;
            }
            Integer excludeBookingId = editing ? Integer.valueOf(editingBooking.maDatPhong) : null;
            return datPhongDAO.findRoomConflict(detail.maPhongId, detail.checkInDuKien, detail.checkOutDuKien, excludeBookingId);
        }

        private String buildConflictMessage(DatPhongConflictInfo conflictInfo) {
            return "Phong " + safeValue(conflictInfo.getSoPhong(), "-")
                    + " đang trùng với phiếu đặt phòng DP" + conflictInfo.getMaDatPhong()
                    + " của khách " + safeValue(conflictInfo.getTenKhachHang(), "Khách chưa xác định")
                    + ", tu " + formatDateTime(conflictInfo.getNgayNhanPhongDateTime())
                    + " den " + formatDateTime(conflictInfo.getNgayTraPhongDateTime())
                    + " (" + safeValue(conflictInfo.getTrangThai(), "-") + ").";
        }

        private String validateDetailRowsBeforeSave() {
            if (detailRows.isEmpty()) {
                reevaluateDetailValidationState(true);
                return "Phiếu đặt phòng phải có ít nhất 1 phòng.";
            }
            for (BookingDetailRecord detail : detailRows) {
                String guestError = validateGuestCount(detail, detail.soNguoi);
                if (guestError != null) {
                    return guestError;
                }
            }
            reevaluateDetailValidationState(true);
            if (highlightedConflictRow != null) {
                if (highlightedConflictRow.duplicateInBooking) {
                    return "Phong " + safeValue(highlightedConflictRow.maPhong, String.valueOf(highlightedConflictRow.maPhongId))
                            + " đang bị chọn trùng trong cùng phiếu đặt phòng.";
                }
                if (highlightedConflictRow.capacityExceeded) {
                    return "Phong " + safeValue(highlightedConflictRow.maPhong, String.valueOf(highlightedConflictRow.maPhongId))
                            + " chi nhan toi da " + highlightedConflictRow.sucChuaToiDa
                            + " khach, hien dang nhap " + highlightedConflictRow.soNguoi + ".";
                }
                if (highlightedConflictRow.conflictInfo != null) {
                    return buildConflictMessage(highlightedConflictRow.conflictInfo);
                }
            }
            return null;
        }

        private void openBookingDetailDialog(BookingDetailRecord detail) {
            try {
                new BookingDetailEditorDialog(this, detail).setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Không thể mở popup thêm dòng chi tiết: " + ex.getMessage());
            }
        }

        private void editSelectedDetailRow() {
            int row = tblBookingDetailDialog.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                showWarning("Vui lòng chọn một dòng chi tiết để cập nhật.");
                return;
            }
            openBookingDetailDialog(detailRows.get(row));
        }

        private void removeSelectedDetailRow() {
            int row = tblBookingDetailDialog.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                showWarning("Vui lòng chọn một dòng chi tiết để xóa.");
                return;
            }
            removeDetailRowAt(row);
        }

        private void submitLegacy(String mode) {
            stopDetailTableEditing();
            CustomerLookup selectedCustomer = (CustomerLookup) cboKhachHangDialog.getSelectedItem();
            if (selectedCustomer == null || selectedCustomer.maKhachHang <= 0) {
                showError("Booking phải có họ tên và số điện thoại khách hàng.");
                return;
            }
            LocalDate ngayDat = normalizeDateFieldValue(txtNgayDatDialog, "Ngày đặt không hợp lệ.");
            if (ngayDat == null) {
                return;
            }
            String ngaySinhText = txtNgaySinhKhach.getText() == null ? "" : txtNgaySinhKhach.getText().trim();
            LocalDate ngaySinhKhach = null;
            if (!ngaySinhText.isEmpty()) {
                ngaySinhKhach = normalizeDateFieldValue(txtNgaySinhKhach, "Ngày sinh không hợp lệ.");
                if (ngaySinhKhach == null) {
                    return;
                }
            }
            if (detailRows.isEmpty()) {
                showError("Booking bắt buộc phải có ít nhất 1 dòng chi tiết phòng.");
                return;
            }
            for (BookingDetailRecord detail : detailRows) {
                if (detail.maPhongId <= 0) {
                    showError("Mỗi dòng chi tiết phải chọn một phòng cụ thể.");
                    return;
                }
            }
            String detailValidationMessage = validateDetailRowsBeforeSave();
            if (detailValidationMessage != null) {
                showError(detailValidationMessage);
                return;
            }
            int maLoaiPhong = detailRows.get(0).maLoaiPhong;

            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }

            datPhongDAO.ensureDetailScheduleSchema(con);
            try {
                con.setAutoCommit(false);

                int maNhanVien = findEmployeeIdByUsername(con);
                Integer maKhachHang = findOrCreateCustomer(
                        con,
                        txtHoTen.getText().trim(),
                        ngaySinhKhach,
                        txtSdt.getText().trim(),
                        txtCccdDialog.getText().trim(),
                        txtEmailKhach.getText().trim(),
                        txtDiaChiKhach.getText().trim()
                );
                if (maKhachHang == null) {
                    con.rollback();
                    showError("Không thể tạo/tìm khách hàng cho booking.");
                    return;
                }

                Integer openBookingId = findOpenBookingIdByCustomer(
                        con,
                        maKhachHang.intValue(),
                        editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                );
                if (openBookingId != null) {
                    con.rollback();
                    showError("Khách hàng này đang có booking DP" + openBookingId + " ở trạng thái đã đặt/chờ check-in. Vui lòng cập nhật booking hiện có, không tạo thêm booking mới.");
                    return;
                }

                Integer maBangGia = findBangGiaByLoaiPhongId(con, maLoaiPhong);
                if (maBangGia == null) {
                    con.rollback();
                    showError("Không tìm thấy bảng giá đang áp dụng cho loại phòng đã chọn.");
                    return;
                }

                int maDatPhong;
                if (editing) {
                    if (bookingHasStay(con, editingBooking.maDatPhong)) {
                        con.rollback();
                        showError("Booking đã check-in thì không cập nhật ở màn này. Vui lòng sang màn Check-in/Check-out.");
                        return;
                    }
                    maDatPhong = editingBooking.maDatPhong;
                    List<Integer> oldRoomIds = getAssignedRoomIdsForBooking(con, maDatPhong);
                    try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET maKhachHang = ?, maNhanVien = ?, maBangGia = ?, ngayDat = ?, ngayNhanPhong = ?, ngayTraPhong = ?, soLuongPhong = ?, soNguoi = ?, tienCoc = ?, trangThai = ?, ghiChu = ? WHERE maDatPhong = ?")) {
                        ps.setInt(1, maKhachHang.intValue());
                        ps.setInt(2, maNhanVien);
                        ps.setInt(3, maBangGia.intValue());
                        ps.setDate(4, Date.valueOf(ngayDat));
                        ps.setDate(5, Date.valueOf(findMinCheckIn(detailRows)));
                        ps.setDate(6, Date.valueOf(findMaxCheckOut(detailRows)));
                        ps.setInt(7, detailRows.size());
                        ps.setInt(8, totalGuests(detailRows));
                        ps.setDouble(9, totalDeposit(detailRows));
                        ps.setString(10, "Đã đặt");
                        ps.setString(11, txtGhiChuDialog.getText().trim());
                        ps.setInt(12, maDatPhong);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietDatPhong WHERE maDatPhong = ?")) {
                        ps.setInt(1, maDatPhong);
                        ps.executeUpdate();
                    }
                    refreshRoomStatuses(con, oldRoomIds);
                } else {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO DatPhong(maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, maKhachHang.intValue());
                        ps.setInt(2, maNhanVien);
                        ps.setInt(3, maBangGia.intValue());
                        ps.setDate(4, Date.valueOf(ngayDat));
                        ps.setDate(5, Date.valueOf(findMinCheckIn(detailRows)));
                        ps.setDate(6, Date.valueOf(findMaxCheckOut(detailRows)));
                        ps.setInt(7, detailRows.size());
                        ps.setInt(8, totalGuests(detailRows));
                        ps.setDouble(9, totalDeposit(detailRows));
                        ps.setString(10, "Đã đặt");
                        ps.setString(11, txtGhiChuDialog.getText().trim());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            rs.next();
                            maDatPhong = rs.getInt(1);
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien, checkInDuKien, checkOutDuKien) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    for (BookingDetailRecord detail : detailRows) {
                        refreshResolvedRate(con, detail);
                        ps.setInt(1, maDatPhong);
                        ps.setInt(2, detail.maPhongId);
                        ps.setInt(3, detail.soNguoi);
                        ps.setDouble(4, detail.giaApDung);
                        ps.setDouble(5, detail.computeThanhTien());
                        ps.setTimestamp(6, toSqlTimestamp(detail.checkInDuKien));
                        ps.setTimestamp(7, toSqlTimestamp(detail.checkOutDuKien));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                refreshRoomStatuses(con, collectRoomIds(detailRows));

                con.commit();
                reloadSampleData(false);
                refreshAllOpenInstances();
                refreshKhachHangViewsSafely();

                if ("update".equals(mode)) {
                    showSuccess("Cập nhật booking thành công.");
                } else {
                    showSuccess("Lưu booking thành công. Có thể đặt nhiều phòng khác loại trong cùng booking, và các phòng đã chuyển sang trạng thái Đã đặt.");
                }
                dispose();
            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignore) {}
                e.printStackTrace();
                showError("Không thể lưu booking: " + e.getMessage());
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }

        private void submit(String mode) {
            stopDetailTableEditing();

            CustomerLookup selectedCustomer = (CustomerLookup) cboKhachHangDialog.getSelectedItem();
            if (selectedCustomer == null || selectedCustomer.maKhachHang <= 0) {
                showError("Vui lòng chọn khách hàng cho booking.");
                return;
            }

            LocalDate ngayDat = normalizeDateFieldValue(txtNgayDatDialog, "Ngày đặt không hợp lệ.");
            if (ngayDat == null) {
                return;
            }
            if (detailRows.isEmpty()) {
                showError("Booking bắt buộc phải có ít nhất 1 dòng chi tiết phòng.");
                return;
            }
            for (BookingDetailRecord detail : detailRows) {
                if (detail.maPhongId <= 0) {
                    showError("Mỗi dòng chi tiết phải chọn một phòng cụ thể.");
                    return;
                }
            }

            String detailValidationMessage = validateDetailRowsBeforeSave();
            if (detailValidationMessage != null) {
                showError(detailValidationMessage);
                return;
            }
            int maLoaiPhong = detailRows.get(0).maLoaiPhong;

            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }

            datPhongDAO.ensureDetailScheduleSchema(con);
            try {
                con.setAutoCommit(false);

                int maNhanVien = findEmployeeIdByUsername(con);
                Integer maKhachHang = Integer.valueOf(selectedCustomer.maKhachHang);

                Integer openBookingId = findOpenBookingIdByCustomer(
                        con,
                        maKhachHang.intValue(),
                        editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                );
                if (openBookingId != null) {
                    con.rollback();
                    showError("Khách hàng này đang có booking DP" + openBookingId + " ở trạng thái đã đặt/chờ check-in. Vui lòng cập nhật booking hiện có, không tạo thêm booking mới.");
                    return;
                }

                Integer maBangGia = findBangGiaByLoaiPhongId(con, maLoaiPhong);
                if (maBangGia == null) {
                    con.rollback();
                    showError("Không tìm thấy bảng giá đang áp dụng cho loại phòng đã chọn.");
                    return;
                }

                int maDatPhong;
                if (editing) {
                    if (bookingHasStay(con, editingBooking.maDatPhong)) {
                        con.rollback();
                        showError("Booking đã check-in thì không cập nhật ở màn này. Vui lòng sang màn Check-in/Check-out.");
                        return;
                    }
                    maDatPhong = editingBooking.maDatPhong;
                    List<Integer> oldRoomIds = getAssignedRoomIdsForBooking(con, maDatPhong);
                    try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET maKhachHang = ?, maNhanVien = ?, maBangGia = ?, ngayDat = ?, ngayNhanPhong = ?, ngayTraPhong = ?, soLuongPhong = ?, soNguoi = ?, tienCoc = ?, trangThai = ?, ghiChu = ? WHERE maDatPhong = ?")) {
                        ps.setInt(1, maKhachHang.intValue());
                        ps.setInt(2, maNhanVien);
                        ps.setInt(3, maBangGia.intValue());
                        ps.setDate(4, Date.valueOf(ngayDat));
                        ps.setDate(5, Date.valueOf(findMinCheckIn(detailRows)));
                        ps.setDate(6, Date.valueOf(findMaxCheckOut(detailRows)));
                        ps.setInt(7, detailRows.size());
                        ps.setInt(8, totalGuests(detailRows));
                        ps.setDouble(9, totalDeposit(detailRows));
                        ps.setString(10, "Đã đặt");
                        ps.setString(11, txtGhiChuDialog.getText().trim());
                        ps.setInt(12, maDatPhong);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietDatPhong WHERE maDatPhong = ?")) {
                        ps.setInt(1, maDatPhong);
                        ps.executeUpdate();
                    }
                    refreshRoomStatuses(con, oldRoomIds);
                } else {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO DatPhong(maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, maKhachHang.intValue());
                        ps.setInt(2, maNhanVien);
                        ps.setInt(3, maBangGia.intValue());
                        ps.setDate(4, Date.valueOf(ngayDat));
                        ps.setDate(5, Date.valueOf(findMinCheckIn(detailRows)));
                        ps.setDate(6, Date.valueOf(findMaxCheckOut(detailRows)));
                        ps.setInt(7, detailRows.size());
                        ps.setInt(8, totalGuests(detailRows));
                        ps.setDouble(9, totalDeposit(detailRows));
                        ps.setString(10, "Đã đặt");
                        ps.setString(11, txtGhiChuDialog.getText().trim());
                        ps.executeUpdate();
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            rs.next();
                            maDatPhong = rs.getInt(1);
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien, checkInDuKien, checkOutDuKien) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    for (BookingDetailRecord detail : detailRows) {
                        refreshResolvedRate(con, detail);
                        ps.setInt(1, maDatPhong);
                        ps.setInt(2, detail.maPhongId);
                        ps.setInt(3, detail.soNguoi);
                        ps.setDouble(4, detail.giaApDung);
                        ps.setDouble(5, detail.computeThanhTien());
                        ps.setTimestamp(6, toSqlTimestamp(detail.checkInDuKien));
                        ps.setTimestamp(7, toSqlTimestamp(detail.checkOutDuKien));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                refreshRoomStatuses(con, collectRoomIds(detailRows));

                con.commit();
                reloadSampleData(false);
                refreshAllOpenInstances();
                refreshKhachHangViewsSafely();

                if ("update".equals(mode)) {
                    showSuccess("Cập nhật booking thành công.");
                } else {
                    showSuccess("Lưu booking thành công. Có thể đặt nhiều phòng khác loại trong cùng booking, và các phòng đã chuyển sang trạng thái Đã đặt.");
                }
                dispose();
            } catch (Exception e) {
                try {
                    con.rollback();
                } catch (Exception ignore) {
                }
                e.printStackTrace();
                showError("Không thể lưu booking: " + e.getMessage());
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (Exception ignore) {
                }
            }
        }

        private LocalDate findMinCheckIn(List<BookingDetailRecord> details) {
            LocalDate min = details.get(0).checkInDuKien == null ? null : details.get(0).checkInDuKien.toLocalDate();
            for (BookingDetailRecord detail : details) {
                if (detail == null || detail.checkInDuKien == null) {
                    continue;
                }
                LocalDate current = detail.checkInDuKien.toLocalDate();
                if (min == null || current.isBefore(min)) {
                    min = current;
                }
            }
            return min;
        }

        private LocalDate findMaxCheckOut(List<BookingDetailRecord> details) {
            LocalDate max = details.get(0).checkOutDuKien == null ? null : details.get(0).checkOutDuKien.toLocalDate();
            for (BookingDetailRecord detail : details) {
                if (detail == null || detail.checkOutDuKien == null) {
                    continue;
                }
                LocalDate current = detail.checkOutDuKien.toLocalDate();
                if (max == null || current.isAfter(max)) {
                    max = current;
                }
            }
            return max;
        }

        private int totalGuests(List<BookingDetailRecord> details) {
            int total = 0;
            for (BookingDetailRecord detail : details) {
                total += detail.soNguoi;
            }
            return total;
        }

        private double totalDeposit(List<BookingDetailRecord> details) {
            double total = 0;
            for (BookingDetailRecord detail : details) {
                total += detail.tienDatCocChiTiet;
            }
            return total;
        }

        private final class BookingDetailEditorDialog extends BaseBookingDialog {
            private final BookingDetailRecord editingDetail;
            private final JComboBox<RoomOption> cboPhongDialog;
            private final JTextField txtLoaiPhongDialog;
            private final AppDatePickerField txtCheckInDialog;
            private final AppTimePickerField txtCheckInTimeDialog;
            private final AppDatePickerField txtCheckOutDialog;
            private final AppTimePickerField txtCheckOutTimeDialog;
            private final JTextField txtSoNguoiDialog;
            private final JTextField txtDatCocDialog;
            private final JTextArea txtGhiChuChiTietDialog;
            private final JPanel pnlInlineWarning;
            private final JLabel lblInlineWarning;
            private final JPanel lblRatePreview;
            private final JLabel lblRateDayType;
            private final JLabel lblRateStayType;
            private final JLabel lblRateBasePrice;
            private final JLabel lblRateSurcharge;
            private final JLabel lblRateAppliedPrice;
            private final JButton btnPrimary;
            private String warningMessage;
            private boolean hasIssue;
            private final Border defaultRoomBorder;
            private final Border defaultCheckInBorder;
            private final Border defaultCheckInTimeBorder;
            private final Border defaultCheckOutBorder;
            private final Border defaultCheckOutTimeBorder;
            private final Color defaultRoomBackground;
            private final Color defaultCheckInBackground;
            private final Color defaultCheckInTimeBackground;
            private final Color defaultCheckOutBackground;
            private final Color defaultCheckOutTimeBackground;

            private BookingDetailEditorDialog(Dialog owner, BookingDetailRecord detail) {
                super(owner, detail == null ? "Thêm dòng chi tiết" : "Cập nhật dòng chi tiết", 740, 600);
                this.editingDetail = detail;

                LocalDateTime initialCheckIn = detail == null
                        ? toDefaultDetailDateTime(LocalDate.now())
                        : normalizeLegacyDetailDateTime(detail.checkInDuKien);
                if (initialCheckIn == null) {
                    initialCheckIn = toDefaultDetailDateTime(LocalDate.now());
                }
                LocalDateTime initialCheckOut = detail == null
                        ? toDefaultDetailDateTime(LocalDate.now().plusDays(1))
                        : normalizeLegacyDetailDateTime(detail.checkOutDuKien);
                if (initialCheckOut == null || !initialCheckOut.isAfter(initialCheckIn)) {
                    initialCheckOut = initialCheckIn.plusDays(1);
                }

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader(
                        detail == null ? "THÊM DÒNG CHI TIẾT" : "CẬP NHẬT DÒNG CHI TIẾT",
                        "Chọn trực tiếp phòng trong màn Đặt phòng. Khi lưu, phòng sẽ được ghi vào ChiTietDatPhong và chuyển sang trạng thái Đã đặt."
                ), BorderLayout.NORTH);

                JPanel form = createDialogFormPanel();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new java.awt.Insets(6, 0, 6, 12);
                gbc.anchor = GridBagConstraints.WEST;

                cboPhongDialog = new JComboBox<RoomOption>();
                cboPhongDialog.setFont(BODY_FONT);
                txtLoaiPhongDialog = createInputField("");
                txtLoaiPhongDialog.setEditable(false);
                txtCheckInDialog = new AppDatePickerField(initialCheckIn.toLocalDate().format(DATE_FORMAT), true);
                txtCheckInTimeDialog = new AppTimePickerField(initialCheckIn.toLocalTime().format(TIME_FORMAT), false);
                txtCheckOutDialog = new AppDatePickerField(initialCheckOut.toLocalDate().format(DATE_FORMAT), true);
                txtCheckOutTimeDialog = new AppTimePickerField(initialCheckOut.toLocalTime().format(TIME_FORMAT), false);
                txtCheckInDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
                txtCheckInTimeDialog.setToolTipText("Nhập giờ dạng HH:mm, ví dụ: 12:00");
                txtCheckOutDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 4/3/26");
                txtCheckOutTimeDialog.setToolTipText("Nhập giờ dạng HH:mm, ví dụ: 12:00");
                txtSoNguoiDialog = createInputField(detail == null ? "2" : String.valueOf(detail.soNguoi));
                txtDatCocDialog = createInputField(detail == null ? "0" : formatMoney(detail.tienDatCocChiTiet));
                txtGhiChuChiTietDialog = createDialogTextArea(2);
                lblRatePreview = createRatePreviewPanel();
                lblRateDayType = createRatePreviewLineLabel();
                lblRateStayType = createRatePreviewLineLabel();
                lblRateBasePrice = createRatePreviewLineLabel();
                lblRateSurcharge = createRatePreviewLineLabel();
                lblRateAppliedPrice = createRatePreviewLineLabel();
                lblRatePreview.add(lblRateDayType);
                lblRatePreview.add(lblRateStayType);
                lblRatePreview.add(lblRateBasePrice);
                lblRatePreview.add(lblRateSurcharge);
                lblRatePreview.add(lblRateAppliedPrice);
                updateRatePreviewDisplay(detail);
                defaultRoomBorder = cboPhongDialog.getBorder();
                defaultCheckInBorder = txtCheckInDialog.getBorder();
                defaultCheckInTimeBorder = txtCheckInTimeDialog.getBorder();
                defaultCheckOutBorder = txtCheckOutDialog.getBorder();
                defaultCheckOutTimeBorder = txtCheckOutTimeDialog.getBorder();
                defaultRoomBackground = cboPhongDialog.getBackground();
                defaultCheckInBackground = resolveFieldBackground(txtCheckInDialog);
                defaultCheckInTimeBackground = resolveFieldBackground(txtCheckInTimeDialog);
                defaultCheckOutBackground = resolveFieldBackground(txtCheckOutDialog);
                defaultCheckOutTimeBackground = resolveFieldBackground(txtCheckOutTimeDialog);
                if (detail != null) {
                    txtGhiChuChiTietDialog.setText(detail.ghiChu);
                }
                cboPhongDialog.addActionListener(e -> syncSelectedRoomInfo());
                cboPhongDialog.addActionListener(e -> refreshSelectedRatePreview());
                cboPhongDialog.addActionListener(e -> reevaluateCurrentSelectionState());
                installDateFieldChangeListener(txtCheckInDialog, this::reloadAvailableRooms);
                installTimeFieldChangeListener(txtCheckInTimeDialog, this::reloadAvailableRooms);
                installDateFieldChangeListener(txtCheckOutDialog, this::reloadAvailableRooms);
                installTimeFieldChangeListener(txtCheckOutTimeDialog, this::reloadAvailableRooms);
                installDateFieldChangeListener(txtCheckInDialog, this::refreshSelectedRatePreview);
                installTimeFieldChangeListener(txtCheckInTimeDialog, this::refreshSelectedRatePreview);
                installDateFieldChangeListener(txtCheckInDialog, this::reevaluateCurrentSelectionState);
                installTimeFieldChangeListener(txtCheckInTimeDialog, this::reevaluateCurrentSelectionState);
                installDateFieldChangeListener(txtCheckOutDialog, this::refreshSelectedRatePreview);
                installTimeFieldChangeListener(txtCheckOutTimeDialog, this::refreshSelectedRatePreview);
                installDateFieldChangeListener(txtCheckOutDialog, this::reevaluateCurrentSelectionState);
                installTimeFieldChangeListener(txtCheckOutTimeDialog, this::reevaluateCurrentSelectionState);

                addFormRow(form, gbc, 0, "Phòng", cboPhongDialog);
                addFormRow(form, gbc, 1, "Loại phòng", txtLoaiPhongDialog);
                addFormRow(form, gbc, 2, "Check-in dự kiến", txtCheckInDialog);
                addFormRow(form, gbc, 3, "Giờ vào dự kiến", txtCheckInTimeDialog);
                addFormRow(form, gbc, 4, "Check-out dự kiến", txtCheckOutDialog);
                addFormRow(form, gbc, 5, "Giờ ra dự kiến", txtCheckOutTimeDialog);
                addFormRow(form, gbc, 6, "Số người", txtSoNguoiDialog);
                addFormRow(form, gbc, 7, "Thu tiền cọc", txtDatCocDialog);
                addFormRow(form, gbc, 8, "Giá áp dụng", lblRatePreview);
                addFormRow(form, gbc, 9, "Ghi chú", new JScrollPane(txtGhiChuChiTietDialog));

                pnlInlineWarning = new JPanel(new BorderLayout());
                pnlInlineWarning.setBackground(CONFLICT_BG);
                pnlInlineWarning.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
                lblInlineWarning = new JLabel();
                lblInlineWarning.setFont(BODY_FONT);
                lblInlineWarning.setForeground(CONFLICT_TEXT);
                pnlInlineWarning.add(lblInlineWarning, BorderLayout.CENTER);
                pnlInlineWarning.setVisible(false);

                JPanel card = createDialogCardPanel();
                card.add(form, BorderLayout.CENTER);
                card.add(pnlInlineWarning, BorderLayout.SOUTH);
                content.add(card, BorderLayout.CENTER);
                reloadAvailableRooms();
                syncSelectedRoomInfo();

                JButton btnPrimary = createPrimaryButton(detail == null ? "Lưu dòng" : "Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit());
                JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
                this.btnPrimary = btnPrimary;
                content.add(buildDialogButtons(btnCancel, btnPrimary), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
                reevaluateCurrentSelectionState();
                refreshSelectedRatePreview();
            }

            private void syncSelectedRoomInfo() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                if (option == null) {
                    warningMessage = "KhÃ´ng cÃ²n phÃ²ng trá»‘ng phÃ¹ há»£p trong khoáº£ng thá»i gian Ä‘Ã£ chá»n.";
                    txtLoaiPhongDialog.setText(resolveRoomAvailabilityMessage());
                    return;
                }
                txtLoaiPhongDialog.setText(option.tenLoaiPhong);
            }

            private LocalDateTime resolveSelectedCheckIn() {
                return combineDateTime(parseDate(txtCheckInDialog.getText()), txtCheckInTimeDialog.getTimeValue());
            }

            private LocalDateTime resolveSelectedCheckOut() {
                return combineDateTime(parseDate(txtCheckOutDialog.getText()), txtCheckOutTimeDialog.getTimeValue());
            }

            private void reloadAvailableRooms() {
                Integer preferredRoomId = resolvePreferredRoomId();
                LocalDateTime checkIn = resolveSelectedCheckIn();
                LocalDateTime checkOut = resolveSelectedCheckOut();
                List<RoomOption> roomOptions = loadRoomOptions(
                        checkIn,
                        checkOut,
                        preferredRoomId,
                        editing ? Integer.valueOf(editingBooking.maDatPhong) : null,
                        detailRows,
                        editingDetail
                );
                cboPhongDialog.removeAllItems();
                for (RoomOption option : roomOptions) {
                    cboPhongDialog.addItem(option);
                }
                selectPreferredRoom(preferredRoomId);
                cboPhongDialog.setEnabled(cboPhongDialog.getItemCount() > 0);
                cboPhongDialog.setToolTipText(cboPhongDialog.getItemCount() > 0 ? null : resolveRoomAvailabilityMessage());
                syncSelectedRoomInfo();
            }

            private Integer resolvePreferredRoomId() {
                RoomOption selected = (RoomOption) cboPhongDialog.getSelectedItem();
                if (selected != null && selected.maPhongId > 0) {
                    return Integer.valueOf(selected.maPhongId);
                }
                if (editingDetail != null && editingDetail.maPhongId > 0) {
                    return Integer.valueOf(editingDetail.maPhongId);
                }
                return null;
            }

            private void selectPreferredRoom(Integer preferredRoomId) {
                if (cboPhongDialog.getItemCount() <= 0) {
                    return;
                }
                if (preferredRoomId != null) {
                    for (int i = 0; i < cboPhongDialog.getItemCount(); i++) {
                        RoomOption option = cboPhongDialog.getItemAt(i);
                        if (option != null && option.maPhongId == preferredRoomId.intValue()) {
                            cboPhongDialog.setSelectedIndex(i);
                            return;
                        }
                    }
                }
                cboPhongDialog.setSelectedIndex(0);
            }

            private String resolveRoomAvailabilityMessage() {
                LocalDateTime checkIn = resolveSelectedCheckIn();
                LocalDateTime checkOut = resolveSelectedCheckOut();
                if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
                    return "Vui lòng chọn ngày nhận/trả hợp lệ để xem phòng trống.";
                }
                if (cboPhongDialog.getItemCount() == 0) {
                    return "Không còn phòng trống phù hợp trong khoảng thời gian đã chọn.";
                }
                return "";
            }

            private void refreshSelectedRatePreview() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                LocalDateTime checkIn = resolveSelectedCheckIn();
                LocalDateTime checkOut = resolveSelectedCheckOut();
                if (option == null || checkIn == null) {
                    updateRatePreviewDisplay(null);
                    return;
                }
                try {
                    Connection con = ConnectDB.getConnection();
                    if (con == null) {
                        updateRatePreviewDisplay(null);
                        return;
                    }
                    BookingDetailRecord preview = new BookingDetailRecord();
                    preview.maLoaiPhong = option.maLoaiPhong;
                    preview.checkInDuKien = checkIn;
                    preview.checkOutDuKien = checkOut == null ? checkIn.plusDays(1) : checkOut;
                    preview.giaApDung = option.giaMacDinh;
                    refreshResolvedRate(con, preview);
                    updateRatePreviewDisplay(preview);
                } catch (Exception ex) {
                    updateRatePreviewDisplay(null);
                }
            }

            private JPanel createRatePreviewPanel() {
                JPanel panel = new JPanel(new GridLayout(5, 1, 0, 4));
                panel.setBackground(PANEL_SOFT);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                        new EmptyBorder(8, 10, 8, 10)
                ));
                return panel;
            }

            private JLabel createRatePreviewLineLabel() {
                JLabel label = new JLabel("-");
                label.setFont(BODY_FONT);
                label.setForeground(TEXT_PRIMARY);
                label.setVerticalAlignment(SwingConstants.TOP);
                return label;
            }

            private void updateRatePreviewDisplay(BookingDetailRecord detail) {
                if (detail == null) {
                    lblRateDayType.setText("Lo\u1ea1i ng\u00e0y trong kho\u1ea3ng: -");
                    lblRateStayType.setText("Ki\u1ec3u t\u00ednh gi\u00e1: -");
                    lblRateBasePrice.setText("Gi\u00e1 c\u01a1 b\u1ea3n: -");
                    lblRateSurcharge.setText("Ph\u1ee5 thu: -");
                    lblRateAppliedPrice.setText("Gi\u00e1 \u00e1p d\u1ee5ng: -");
                    return;
                }
                double appliedAmount = detail.thanhTien > 0d ? detail.thanhTien : detail.giaApDung;
                lblRateDayType.setText("Lo\u1ea1i ng\u00e0y trong kho\u1ea3ng: " + normalizeAppliedRateText(detail.loaiNgayApDung));
                lblRateStayType.setText("Ki\u1ec3u t\u00ednh gi\u00e1: " + normalizeAppliedRateText(detail.loaiGiaApDung));
                lblRateBasePrice.setText("Gi\u00e1 c\u01a1 b\u1ea3n: " + formatMoney(Math.max(detail.giaNenApDung, 0d)));
                lblRateSurcharge.setText(resolveRateSurchargeText(detail));
                lblRateAppliedPrice.setText("Gi\u00e1 \u00e1p d\u1ee5ng: " + formatMoney(Math.max(appliedAmount, 0d)));
            }

            private String resolveRateSurchargeText(BookingDetailRecord detail) {
                double surcharge = detail == null ? 0d : Math.max(detail.tongPhuThuApDung > 0d ? detail.tongPhuThuApDung : detail.phuThuApDung, 0d);
                if (surcharge <= 0d) {
                    return "Ph\u1ee5 thu: 0";
                }
                String dayType = detail == null ? "" : normalizeAppliedRateText(detail.loaiNgayApDung);
                if ("Ngày lễ".equalsIgnoreCase(dayType)) {
                    return "Tổng phụ thu ngày lễ: " + formatMoney(surcharge);
                }
                if ("Cuối tuần".equalsIgnoreCase(dayType)) {
                    return "Tổng phụ thu cuối tuần: " + formatMoney(surcharge);
                }
                return "T\u1ed5ng ph\u1ee5 thu: " + formatMoney(surcharge);
            }

            private void reevaluateCurrentSelectionStateLegacy() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                LocalDateTime checkIn = resolveSelectedCheckIn();
                LocalDateTime checkOut = resolveSelectedCheckOut();
                int soNguoi = parsePositiveIntOrZero(txtSoNguoiDialog.getText().trim());

                String warningMessage = null;
                boolean hasIssue = false;
                if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
                    warningMessage = "Giờ ra dự kiến phải lớn hơn giờ vào dự kiến.";
                    hasIssue = true;
                }
                if (!hasIssue && option == null && cboPhongDialog.getItemCount() == 0 && checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    warningMessage = "Không còn phòng trống phù hợp trong khoảng thời gian đã chọn.";
                    hasIssue = true;
                } else if (!hasIssue && option == null) {
                    warningMessage = "Vui lòng chọn phòng trước khi lưu.";
                    hasIssue = true;
                } else if (!hasIssue && isDuplicateRoomSelection(option.maPhongId)) {
                    warningMessage = "Phòng " + option.soPhong + " đã được chọn trong phiếu đặt phòng này.";
                    hasIssue = true;
                } else if (!hasIssue && checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
                    warningMessage = "Giờ ra dự kiến phải lớn hơn giờ vào dự kiến.";
                    hasIssue = true;
                } else if (!hasIssue && option != null && option.sucChuaToiDa > 0 && soNguoi > option.sucChuaToiDa) {
                    warningMessage = "Phòng " + option.soPhong + " chỉ nhận tối đa " + option.sucChuaToiDa + " khách.";
                    hasIssue = true;
                } else if (!hasIssue && option != null && checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    DatPhongConflictInfo conflictInfo = datPhongDAO.findRoomConflict(
                            option.maPhongId,
                            checkIn,
                            checkOut,
                            editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                    );
                    if (conflictInfo != null) {
                        warningMessage = buildConflictMessage(conflictInfo);
                        hasIssue = true;
                    }
                }

                applyInlineWarningState(warningMessage, hasIssue);
            }

            private void reevaluateCurrentSelectionState() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                LocalDateTime checkIn = resolveSelectedCheckIn();
                LocalDateTime checkOut = resolveSelectedCheckOut();
                int soNguoi = parsePositiveIntOrZero(txtSoNguoiDialog.getText().trim());

                String warningMessage = null;
                boolean hasIssue = false;
                if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
                    warningMessage = "Giờ ra dự kiến phải lớn hơn giờ vào dự kiến.";
                    hasIssue = true;
                }
                if (!hasIssue && option == null && cboPhongDialog.getItemCount() == 0 && checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    warningMessage = "Không còn phòng trống phù hợp trong khoảng thời gian đã chọn.";
                    hasIssue = true;
                } else if (!hasIssue && option == null) {
                    warningMessage = "Vui lòng chọn phòng trước khi lưu.";
                    hasIssue = true;
                } else if (!hasIssue && isDuplicateRoomSelection(option.maPhongId)) {
                    warningMessage = "Phòng " + option.soPhong + " đã được chọn trong phiếu đặt phòng này.";
                    hasIssue = true;
                } else if (!hasIssue) {
                    warningMessage = validateGuestCount(option, soNguoi);
                    hasIssue = warningMessage != null;
                } else if (!hasIssue && option != null && checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    DatPhongConflictInfo conflictInfo = datPhongDAO.findRoomConflict(
                            option.maPhongId,
                            checkIn,
                            checkOut,
                            editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                    );
                    if (conflictInfo != null) {
                        warningMessage = buildConflictMessage(conflictInfo);
                        hasIssue = true;
                    }
                }

                if (!hasIssue && option != null && checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                    DatPhongConflictInfo conflictInfo = datPhongDAO.findRoomConflict(
                            option.maPhongId,
                            checkIn,
                            checkOut,
                            editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                    );
                    if (conflictInfo != null) {
                        warningMessage = buildConflictMessage(conflictInfo);
                        hasIssue = true;
                    }
                }

                applyInlineWarningState(warningMessage, hasIssue);
            }

            private void applyInlineWarningState(String warningMessage, boolean hasIssue) {
                if (lblInlineWarning == null || pnlInlineWarning == null) {
                    return;
                }
                lblInlineWarning.setText(hasIssue
                        ? "<html>" + warningMessage + "<br/>Vui lòng chọn phòng khác hoặc đổi ngày nhận/trả phòng.</html>"
                        : "");
                pnlInlineWarning.setVisible(hasIssue);
                cboPhongDialog.setBorder(hasIssue ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultRoomBorder);
                cboPhongDialog.setBackground(hasIssue ? CONFLICT_BG : defaultRoomBackground);
                txtCheckInDialog.setBorder(hasIssue ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultCheckInBorder);
                txtCheckInTimeDialog.setBorder(hasIssue ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultCheckInTimeBorder);
                txtCheckOutDialog.setBorder(hasIssue ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultCheckOutBorder);
                txtCheckOutTimeDialog.setBorder(hasIssue ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultCheckOutTimeBorder);
                setFieldBackground(txtCheckInDialog, hasIssue ? CONFLICT_BG : defaultCheckInBackground);
                setFieldBackground(txtCheckInTimeDialog, hasIssue ? CONFLICT_BG : defaultCheckInTimeBackground);
                setFieldBackground(txtCheckOutDialog, hasIssue ? CONFLICT_BG : defaultCheckOutBackground);
                setFieldBackground(txtCheckOutTimeDialog, hasIssue ? CONFLICT_BG : defaultCheckOutTimeBackground);
            }

            private Color resolveFieldBackground(Component field) {
                JTextField editor = findNestedTextField(field);
                return editor == null ? Color.WHITE : editor.getBackground();
            }

            private void setFieldBackground(Component field, Color color) {
                JTextField editor = findNestedTextField(field);
                if (editor != null) {
                    editor.setBackground(color);
                }
            }

            private boolean isDuplicateRoomSelection(int maPhongId) {
                for (BookingDetailRecord row : detailRows) {
                    if (row != editingDetail && row.maPhongId == maPhongId) {
                        return true;
                    }
                }
                return false;
            }

            private void submitLegacy() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                if (option == null) {
                    showError("Vui lòng chọn phòng.");
                    return;
                }
                for (BookingDetailRecord row : detailRows) {
                    if (row != editingDetail && row.maPhongId == option.maPhongId) {
                        showError("Phòng này đã được chọn trong booking.");
                        return;
                    }
                }
                LocalDate checkInDate = normalizeDateFieldValue(txtCheckInDialog, "Check-in dự kiến không hợp lệ.");
                LocalTime checkInTime = normalizeTimeFieldValue(txtCheckInTimeDialog, "Giờ vào dự kiến không hợp lệ.");
                LocalDate checkOutDate = normalizeDateFieldValue(txtCheckOutDialog, "Check-out dự kiến không hợp lệ.");
                LocalTime checkOutTime = normalizeTimeFieldValue(txtCheckOutTimeDialog, "Giờ ra dự kiến không hợp lệ.");
                if (checkInDate == null || checkInTime == null || checkOutDate == null || checkOutTime == null) {
                    return;
                }
                LocalDateTime checkIn = combineDateTime(checkInDate, checkInTime);
                LocalDateTime checkOut = combineDateTime(checkOutDate, checkOutTime);
                if (!checkOut.isAfter(checkIn)) {
                    showError("Giờ ra dự kiến phải lớn hơn giờ vào dự kiến.");
                    return;
                }
                int soNguoi;
                try {
                    soNguoi = Integer.parseInt(txtSoNguoiDialog.getText().trim());
                } catch (NumberFormatException ex) {
                    showError("Số người phải hợp lệ.");
                    return;
                }
                if (soNguoi <= 0) {
                    showError("Số người phải lớn hơn 0.");
                    return;
                }
                double tienCoc = parseMoney(txtDatCocDialog.getText().trim());
                if (tienCoc < 0) {
                    showError("Thu tiền cọc phải hợp lệ.");
                    return;
                }

                BookingDetailRecord target = editingDetail == null ? new BookingDetailRecord() : editingDetail;
                target.loaiPhong = option.tenLoaiPhong;
                target.maLoaiPhong = option.maLoaiPhong;
                target.maPhongId = option.maPhongId;
                target.maPhong = option.soPhong;
                target.checkInDuKien = checkIn;
                target.checkOutDuKien = checkOut;
                target.soNguoi = soNguoi;
                target.sucChuaToiDa = option.sucChuaToiDa;
                target.giaApDung = option.giaMacDinh > 0 ? option.giaMacDinh : (editingDetail == null ? 0d : editingDetail.giaApDung);
                try {
                    Connection con = ConnectDB.getConnection();
                    if (con != null) {
                        refreshResolvedRate(con, target);
                    }
                } catch (Exception ex) {
                    target.loaiNgayApDung = "-";
                    target.loaiGiaApDung = "-";
                    target.giaNenApDung = 0d;
                    target.phuThuApDung = 0d;
                    target.tongPhuThuApDung = 0d;
                    target.thanhTien = 0d;
                }
                target.tienDatCocChiTiet = tienCoc;
                target.duplicateInBooking = isDuplicateRoomSelection(option.maPhongId);
                target.capacityExceeded = option.sucChuaToiDa > 0 && soNguoi > option.sucChuaToiDa;
                target.conflictInfo = datPhongDAO.findRoomConflict(
                        option.maPhongId,
                        checkIn,
                        checkOut,
                        editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                );
                target.trangThaiChiTiet = target.conflictInfo != null ? "Xung dot lich" : "Đã đặt";
                target.ghiChu = txtGhiChuChiTietDialog.getText().trim();
                if (editingDetail == null) {
                    detailRows.add(target);
                }
                refillBookingDetailDialogTable();
                dispose();
            }

            private void submit() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                if (option == null) {
                    showError("Vui lòng chọn phòng.");
                    return;
                }
                for (BookingDetailRecord row : detailRows) {
                    if (row != editingDetail && row.maPhongId == option.maPhongId) {
                        showError("Phòng này đã được chọn trong booking.");
                        return;
                    }
                }

                LocalDate checkInDate = normalizeDateFieldValue(txtCheckInDialog, "Check-in dự kiến không hợp lệ.");
                LocalTime checkInTime = normalizeTimeFieldValue(txtCheckInTimeDialog, "Giờ vào dự kiến không hợp lệ.");
                LocalDate checkOutDate = normalizeDateFieldValue(txtCheckOutDialog, "Check-out dự kiến không hợp lệ.");
                LocalTime checkOutTime = normalizeTimeFieldValue(txtCheckOutTimeDialog, "Giờ ra dự kiến không hợp lệ.");
                if (checkInDate == null || checkInTime == null || checkOutDate == null || checkOutTime == null) {
                    return;
                }

                LocalDateTime checkIn = combineDateTime(checkInDate, checkInTime);
                LocalDateTime checkOut = combineDateTime(checkOutDate, checkOutTime);
                if (!checkOut.isAfter(checkIn)) {
                    showError("Giờ ra dự kiến phải lớn hơn giờ vào dự kiến.");
                    return;
                }

                int soNguoi;
                try {
                    soNguoi = Integer.parseInt(txtSoNguoiDialog.getText().trim());
                } catch (NumberFormatException ex) {
                    showError("Số người ở phải là số nguyên hợp lệ.");
                    return;
                }

                String guestError = validateGuestCount(option, soNguoi);
                if (guestError != null) {
                    showError(guestError);
                    return;
                }

                double tienCoc = parseMoney(txtDatCocDialog.getText().trim());
                if (tienCoc < 0) {
                    showError("Thu tiền cọc phải hợp lệ.");
                    return;
                }

                BookingDetailRecord target = editingDetail == null ? new BookingDetailRecord() : editingDetail;
                target.loaiPhong = option.tenLoaiPhong;
                target.maLoaiPhong = option.maLoaiPhong;
                target.maPhongId = option.maPhongId;
                target.maPhong = option.soPhong;
                target.checkInDuKien = checkIn;
                target.checkOutDuKien = checkOut;
                target.soNguoi = soNguoi;
                target.sucChuaToiDa = option.sucChuaToiDa;
                target.giaApDung = option.giaMacDinh > 0 ? option.giaMacDinh : (editingDetail == null ? 0d : editingDetail.giaApDung);
                try {
                    Connection con = ConnectDB.getConnection();
                    if (con != null) {
                        refreshResolvedRate(con, target);
                    }
                } catch (Exception ex) {
                    target.loaiNgayApDung = "-";
                    target.loaiGiaApDung = "-";
                    target.giaNenApDung = 0d;
                    target.phuThuApDung = 0d;
                    target.tongPhuThuApDung = 0d;
                    target.thanhTien = 0d;
                }
                target.tienDatCocChiTiet = tienCoc;
                target.duplicateInBooking = isDuplicateRoomSelection(option.maPhongId);
                target.capacityExceeded = option.sucChuaToiDa > 0 && soNguoi > option.sucChuaToiDa;
                target.conflictInfo = datPhongDAO.findRoomConflict(
                        option.maPhongId,
                        checkIn,
                        checkOut,
                        editing ? Integer.valueOf(editingBooking.maDatPhong) : null
                );
                target.trangThaiChiTiet = target.conflictInfo != null ? "Xung dot lich" : "Đã đặt";
                target.ghiChu = txtGhiChuChiTietDialog.getText().trim();
                if (editingDetail == null) {
                    detailRows.add(target);
                }
                refillBookingDetailDialogTable();
                dispose();
            }
        }
    }

    private final class ConfirmBookingDialog extends BaseBookingDialog {
        private final BookingRecord booking;
        private final JTextArea txtNote;

        private ConfirmBookingDialog(Window owner, BookingRecord booking) {
            super(owner, "Xác nhận booking", 560, 360);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("XÁC NHẬN BOOKING", "Bạn có chắc muốn xác nhận booking này không?"), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtNote = createDialogTextArea(3);
            addFormRow(form, gbc, 0, "Mã booking", createValueTag("DP" + booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Phòng / loại", createValueTag(booking.getRoomSummary()));
            addFormRow(form, gbc, 3, "Check-in / Check-out", createValueTag(booking.formatNgayNhanPhong() + " - " + booking.formatNgayTraPhong()));
            addFormRow(form, gbc, 4, "Trạng thái hiện tại", createValueTag(booking.trangThai));
            addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtNote));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            JButton btnOk = createPrimaryButton("Đồng ý", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnSkip = createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnSkip, btnOk), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (!canConfirmBooking(booking.trangThai)) {
                showError("Booking này không còn ở trạng thái chờ xác nhận/check-in.");
                return;
            }
            boolean updated = datPhongDAO.updateTrangThai(String.valueOf(booking.maDatPhong), "Đã xác nhận");
            if (!updated) {
                String message = safeValue(datPhongDAO.getLastErrorMessage(), "");
                showError(message.isEmpty() ? "Không thể xác nhận booking." : message);
                return;
            }
            refreshAllOpenInstances();
            CheckInOutGUI.prepareFocusOnBooking(booking.maDatPhong);
            NavigationUtil.navigate(
                    DatPhongGUI.this,
                    ScreenKey.DAT_PHONG,
                    ScreenKey.CHECK_IN_OUT,
                    username,
                    role
            );
            showSuccess("Xác nhận booking thành công.");
            dispose();
        }

        private void submitConfirmedBookingLegacy() {
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'Đã đặt' WHERE maDatPhong = ?")) {
                ps.setInt(1, booking.maDatPhong);
                ps.executeUpdate();
                refreshAllOpenInstances();
                showSuccess("Xác nhận booking thành công.");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Không thể xác nhận booking.");
            }
        }
    }

    private final class DepositDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private DepositDialog(Window owner, BookingRecord booking) {
            super(owner, "Nhận cọc", 620, 420);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("NHẬN CỌC BOOKING", "Ghi nhận thêm tiền cọc cho booking đã chọn."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtDaCoc = createInputField(formatMoney(booking.tongTienDatCoc));
            txtDaCoc.setEditable(false);
            JTextField txtThuThem = createInputField("");
            JTextArea txtGhiChuMoi = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã booking", createValueTag("DP" + booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Đã cọc trước", txtDaCoc);
            addFormRow(form, gbc, 3, "Thu thêm", txtThuThem);
            addFormRow(form, gbc, 4, "Ghi chú", new JScrollPane(txtGhiChuMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu phiếu cọc", new Color(22, 163, 74), Color.WHITE, e -> submit(txtThuThem, txtGhiChuMoi));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextField txtThuThem, JTextArea txtGhiChuMoi) {
            double thuThem = parseMoney(txtThuThem.getText().trim());
            if (thuThem < 0) {
                showError("Số tiền thu thêm phải hợp lệ.");
                return;
            }
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET tienCoc = ISNULL(tienCoc,0) + ? WHERE maDatPhong = ?")) {
                ps.setDouble(1, thuThem);
                ps.setInt(2, booking.maDatPhong);
                ps.executeUpdate();
                refreshAllOpenInstances();
                showSuccess("Nhận cọc thành công.");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Không thể lưu phiếu cọc.");
            }
        }
    }

    private final class CancelBookingDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private CancelBookingDialog(Window owner, BookingRecord booking) {
            super(owner, "Hủy booking", 620, 430);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("HỦY BOOKING", "Booking sẽ chuyển sang trạng thái Đã hủy."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtLyDo = createInputField("");
            JTextArea txtGhiChuMoi = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã booking", createValueTag("DP" + booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(booking.trangThai));
            addFormRow(form, gbc, 3, "Lý do hủy", txtLyDo);
            addFormRow(form, gbc, 4, "Ghi chú", new JScrollPane(txtGhiChuMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận hủy", new Color(220, 38, 38), Color.WHITE, e -> submit(txtLyDo, txtGhiChuMoi));
            JButton btnCancel = createOutlineButton("Hủy thao tác", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextField txtLyDo, JTextArea txtGhiChuMoi) {
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Phải nhập lý do hủy.");
                return;
            }
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }
            try {
                con.setAutoCommit(false);
                List<Integer> roomIds = getAssignedRoomIdsForBooking(con, booking.maDatPhong);
                try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'Đã hủy' WHERE maDatPhong = ?")) {
                    ps.setInt(1, booking.maDatPhong);
                    ps.executeUpdate();
                }
                releaseAssignedRooms(con, roomIds);
                con.commit();
                prepareFocusOnCancelledBooking(booking.maDatPhong);
                refreshAllOpenInstances();
                showSuccess("Hủy booking thành công.");
                dispose();
            } catch (Exception e) {
                try { con.rollback(); } catch (Exception ignore) {}
                e.printStackTrace();
                showError("Không thể hủy booking.");
            } finally {
                try { con.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }
    }

    private final class RestoreBookingDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private RestoreBookingDialog(Window owner, BookingRecord booking) {
            super(owner, "Khôi phục booking", 620, 380);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("KHÔI PHỤC BOOKING", "Khôi phục booking đã hủy về trạng thái có thể tiếp tục xử lý."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã booking", createValueTag("DP" + booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(booking.trangThai));
            addFormRow(form, gbc, 3, "Khôi phục về", createValueTag(resolveRestoreStatus(booking)));
            addFormRow(form, gbc, 4, "Phòng / loại", createValueTag(booking.getRoomSummary()));
            addFormRow(form, gbc, 5, "Check-in / Check-out", createValueTag(booking.formatNgayNhanPhong() + " - " + booking.formatNgayTraPhong()));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Khôi phục booking", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (!isCancelledBooking(booking.trangThai)) {
                showError("Booking này không còn ở trạng thái Đã hủy.");
                return;
            }

            String restoreStatus = resolveRestoreStatus(booking);
            boolean restored = datPhongDAO.restoreCancelledBooking(String.valueOf(booking.maDatPhong), restoreStatus);
            if (!restored) {
                String message = safeValue(datPhongDAO.getLastErrorMessage(), "");
                showError(message.isEmpty() ? buildRestoreConflictMessage(null) : message);
                return;
            }

            prepareFocusOnBooking(booking.maDatPhong);
            refreshAllOpenInstances();
            showSuccess("Khôi phục booking thành công.");
            dispose();
        }
    }

    private final class ViewBookingDialog extends BaseBookingDialog {
        private ViewBookingDialog(Window owner, BookingRecord booking) {
            super(owner, "Xem chi tiết booking", 720, 520);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHI TIẾT BOOKING", "Thông tin hiện tại của booking được chọn."), BorderLayout.NORTH);

            JTextArea area = createDialogTextArea(18);
            area.setEditable(false);
            area.setBackground(PANEL_SOFT);
            StringBuilder builder = new StringBuilder();
            builder.append("Mã booking: DP").append(booking.maDatPhong).append("\n");
            builder.append("Khách hàng: ").append(booking.khachHang).append("\n");
            builder.append("SĐT: ").append(booking.soDienThoai).append("\n");
            builder.append("Phòng / loại: ").append(booking.getRoomSummary()).append("\n");
            builder.append("Ngày nhận: ").append(booking.formatNgayNhanPhong()).append("\n");
            builder.append("Ngày trả: ").append(booking.formatNgayTraPhong()).append("\n");
            builder.append("Trạng thái: ").append(booking.trangThai).append("\n");
            builder.append("Tổng cọc: ").append(formatMoney(booking.tongTienDatCoc)).append("\n\n");
            builder.append("Chi tiết:\n");
            for (BookingDetailRecord detail : booking.details) {
                builder.append("- ").append(detail.loaiPhong)
                        .append(" | ").append(detail.formatCheckIn()).append(" -> ").append(detail.formatCheckOut())
                        .append(" | ").append(detail.soNguoi).append(" khách")
                        .append(" | ").append(formatAppliedRateValue(detail)).append("\n");
            }
            area.setText(builder.toString());
            area.setCaretPosition(0);

            JPanel card = createDialogCardPanel();
            card.add(new JScrollPane(area), BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class ConfirmDialog extends BaseBookingDialog {
        private boolean confirmed;

        private ConfirmDialog(Window owner, String title, String message, String confirmText, Color confirmColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            JButton btnSkip = createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose());
            JButton btnConfirm = createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> {
                confirmed = true;
                dispose();
            });
            content.add(buildDialogButtons(btnSkip, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() {
            return confirmed;
        }
    }

    private final class AppMessageDialog extends BaseBookingDialog {
        private AppMessageDialog(Window owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private interface TableRowAction {
        void execute(int rowIndex);
    }

    private final class TableActionButtonRenderer extends JButton implements TableCellRenderer {
        private TableActionButtonRenderer(Color background, Color foreground) {
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBackground(background);
            setForeground(foreground);
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(valueOf(value));
            return this;
        }
    }

    private final class TableActionButtonEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        private final JTable table;
        private final JButton button;
        private final TableRowAction action;
        private String label;

        private TableActionButtonEditor(JTable table, Color background, Color foreground, TableRowAction action) {
            this.table = table;
            this.action = action;
            this.button = new JButton();
            this.button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            this.button.setBackground(background);
            this.button.setForeground(foreground);
            this.button.setOpaque(true);
            this.button.setBorderPainted(false);
            this.button.setFocusPainted(false);
            this.button.addActionListener(this);
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = valueOf(value);
            button.setText(label);
            return button;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            int editingRow = table.getEditingRow();
            fireEditingStopped();
            if (editingRow >= 0) {
                action.execute(table.convertRowIndexToModel(editingRow));
            }
        }
    }

    private static final class DigitsOnlyDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (isDigitsOnly(string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (isDigitsOnly(text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isDigitsOnly(String text) {
            if (text == null || text.isEmpty()) {
                return true;
            }
            for (int i = 0; i < text.length(); i++) {
                if (!Character.isDigit(text.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final class CustomerLookup {
        private int maKhachHang;
        private String hoTen;
        private String soDienThoai;
        private String cccdPassport;
        private LocalDate ngaySinh;
        private String email;
        private String diaChi;
        private String displayText;

        @Override
        public String toString() {
            return displayText == null ? "" : displayText;
        }
    }

    private static final class RoomOption {
        private int maPhongId;
        private int maLoaiPhong;
        private String soPhong;
        private String tang;
        private String trangThai;
        private String tenLoaiPhong;
        private int sucChuaToiDa;
        private double giaMacDinh;

        @Override
        public String toString() {
            return soPhong + " - " + tenLoaiPhong + " - " + tang + " - " + trangThai;
        }
    }

    private static final class BookingRecord {
        private int maDatPhong;
        private int maKhachHang;
        private String maKhachHangText;
        private String khachHang;
        private String soDienThoai;
        private String cccd;
        private LocalDate ngaySinhKhach;
        private LocalDate ngayDat;
        private String nguonDat;
        private double tongTienDatCoc;
        private String trangThai;
        private String ghiChu;
        private final List<BookingDetailRecord> details = new ArrayList<BookingDetailRecord>();

        private String loaiPhong;
        private LocalDate ngayNhanPhong;
        private LocalDate ngayTraPhong;

        private void syncDerivedData() {
            tongTienDatCoc = 0;
            loaiPhong = "";
            ngayNhanPhong = null;
            ngayTraPhong = null;
            LinkedHashSet<String> roomTypes = new LinkedHashSet<String>();
            for (BookingDetailRecord detail : details) {
                tongTienDatCoc += detail.tienDatCocChiTiet;
                if (detail.loaiPhong != null && !detail.loaiPhong.trim().isEmpty()) {
                    roomTypes.add(detail.loaiPhong.trim());
                }
                if (detail.checkInDuKien != null) {
                    LocalDate currentCheckIn = detail.checkInDuKien.toLocalDate();
                    if (ngayNhanPhong == null || currentCheckIn.isBefore(ngayNhanPhong)) {
                        ngayNhanPhong = currentCheckIn;
                    }
                }
                if (detail.checkOutDuKien != null) {
                    LocalDate currentCheckOut = detail.checkOutDuKien.toLocalDate();
                    if (ngayTraPhong == null || currentCheckOut.isAfter(ngayTraPhong)) {
                        ngayTraPhong = currentCheckOut;
                    }
                }
            }
            if (roomTypes.size() > 1) {
                loaiPhong = "Nhiều loại phòng";
            } else if (!roomTypes.isEmpty()) {
                loaiPhong = roomTypes.iterator().next();
            }
        }

        private boolean matchesRoomType(String roomType) {
            for (BookingDetailRecord detail : details) {
                if (roomType.equalsIgnoreCase(detail.loaiPhong)) {
                    return true;
                }
            }
            return false;
        }

        private int getTotalGuests() {
            int totalGuests = 0;
            for (BookingDetailRecord detail : details) {
                totalGuests += detail.soNguoi;
            }
            return totalGuests;
        }

        private String getRoomSummary() {
            LinkedHashSet<String> roomTypes = new LinkedHashSet<String>();
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < details.size(); i++) {
                if (i > 0) {
                    summary.append(", ");
                }
                BookingDetailRecord detail = details.get(i);
                if (detail.loaiPhong != null && !detail.loaiPhong.trim().isEmpty()) {
                    roomTypes.add(detail.loaiPhong.trim());
                }
                summary.append(detail.maPhong == null || detail.maPhong.trim().isEmpty() ? "Chưa gán" : detail.maPhong)
                        .append(" / ")
                        .append(detail.loaiPhong);
            }
            if (summary.length() == 0) {
                return "-";
            }
            return roomTypes.size() > 1 ? "Nhiều loại phòng: " + summary : summary.toString();
        }

        private String formatNgayDat() {
            return ngayDat == null ? "-" : ngayDat.format(DATE_FORMAT);
        }

        private String formatNgayNhanPhong() {
            return ngayNhanPhong == null ? "-" : ngayNhanPhong.format(DATE_FORMAT);
        }

        private String formatNgayTraPhong() {
            return ngayTraPhong == null ? "-" : ngayTraPhong.format(DATE_FORMAT);
        }
    }

    private static final class BookingDetailRecord {
        private int maChiTietDatPhong;
        private String loaiPhong;
        private int maLoaiPhong;
        private int maPhongId;
        private String maPhong;
        private LocalDateTime checkInDuKien;
        private LocalDateTime checkOutDuKien;
        private int soNguoi;
        private int sucChuaToiDa;
        private double giaApDung;
        private double giaNenApDung;
        private double phuThuApDung;
        private double tongPhuThuApDung;
        private String loaiNgayApDung;
        private String loaiGiaApDung;
        private double tienDatCocChiTiet;
        private String trangThaiChiTiet;
        private String ghiChu;
        private double thanhTien;
        private boolean duplicateInBooking;
        private boolean capacityExceeded;
        private DatPhongConflictInfo conflictInfo;

        private BookingDetailRecord copy() {
            BookingDetailRecord detail = new BookingDetailRecord();
            detail.maChiTietDatPhong = maChiTietDatPhong;
            detail.loaiPhong = loaiPhong;
            detail.maLoaiPhong = maLoaiPhong;
            detail.maPhongId = maPhongId;
            detail.maPhong = maPhong;
            detail.checkInDuKien = checkInDuKien;
            detail.checkOutDuKien = checkOutDuKien;
            detail.soNguoi = soNguoi;
            detail.sucChuaToiDa = sucChuaToiDa;
            detail.giaApDung = giaApDung;
            detail.giaNenApDung = giaNenApDung;
            detail.phuThuApDung = phuThuApDung;
            detail.tongPhuThuApDung = tongPhuThuApDung;
            detail.loaiNgayApDung = loaiNgayApDung;
            detail.loaiGiaApDung = loaiGiaApDung;
            detail.tienDatCocChiTiet = tienDatCocChiTiet;
            detail.trangThaiChiTiet = trangThaiChiTiet;
            detail.ghiChu = ghiChu;
            detail.thanhTien = thanhTien;
            detail.duplicateInBooking = duplicateInBooking;
            detail.capacityExceeded = capacityExceeded;
            detail.conflictInfo = conflictInfo;
            return detail;
        }

        private double computeThanhTien() {
            if (thanhTien > 0d) {
                return thanhTien;
            }
            if (checkInDuKien == null || checkOutDuKien == null) {
                return giaApDung;
            }
            long soDem = ChronoUnit.DAYS.between(checkInDuKien.toLocalDate(), checkOutDuKien.toLocalDate());
            if (soDem <= 0) {
                soDem = 1;
            }
            return soDem * giaApDung;
        }

        private String formatCheckIn() {
            return checkInDuKien == null ? "-" : checkInDuKien.format(DATETIME_FORMAT);
        }

        private String formatCheckOut() {
            return checkOutDuKien == null ? "-" : checkOutDuKien.format(DATETIME_FORMAT);
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }
}
