package gui;

import dao.DatPhongDAO;
import db.ConnectDB;
import entity.ChiTietDatPhong;
import entity.DatPhong;
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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatPhongGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("vi-VN"));

    private static final List<DatPhongGUI> OPEN_INSTANCES = new ArrayList<DatPhongGUI>();

    private final String username;
    private final String role;
    private final DatPhongDAO datPhongDAO = new DatPhongDAO();
    private JPanel rootPanel;

    private final List<BookingRecord> allBookings = new ArrayList<BookingRecord>();
    private final List<BookingRecord> filteredBookings = new ArrayList<BookingRecord>();

    private JTable tblDatPhong;
    private DefaultTableModel bookingModel;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;

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
    private JTextArea txtGhiChu;
    private JTable tblBookingDetails;
    private DefaultTableModel bookingDetailModel;

    public DatPhongGUI() {
        this("guest", "Lễ tân");
    }

    public DatPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý đặt phòng - " + AppBranding.APP_DISPLAY_NAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        reloadData(false);
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

        JLabel lblSub = new JLabel("Chọn trực tiếp phòng từ danh sách phòng, một khách có thể đặt từ một phòng trở lên.");
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
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Tạo booking", new Color(22, 163, 74), Color.WHITE, e -> openCreateBookingDialog()));
        card.add(createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> openConfirmBookingDialog()));
        card.add(createPrimaryButton("Nhận cọc", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDepositDialog()));
        card.add(createPrimaryButton("Cập nhật", new Color(59, 130, 246), Color.WHITE, e -> openUpdateBookingDialog()));
        card.add(createPrimaryButton("Hủy booking", new Color(220, 38, 38), Color.WHITE, e -> openCancelBookingDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Đã xác nhận", "Chờ check-in", "Đang lưu trú", "Đã check-out", "Đã hủy"});
        txtTuNgay = new AppDatePickerField("", false);
        txtDenNgay = new AppDatePickerField("", false);

        left.add(createFieldGroup("Trạng thái", cboTrangThai));
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

        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(280, 34));

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
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildRightColumn());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0.60);
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

        String[] columns = {"Mã đặt phòng", "Khách hàng", "Ngày đặt", "Trạng thái", "Số phòng", "Số người", "Tiền cọc", "Ghi chú"};
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
        tblDatPhong.setGridColor(BORDER_SOFT);
        tblDatPhong.setShowGrid(true);
        tblDatPhong.setFillsViewportHeight(true);
        tblDatPhong.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

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
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(buildDetailCard(), BorderLayout.NORTH);
        wrapper.add(buildDetailLinesCard(), BorderLayout.CENTER);
        return wrapper;
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
        addDetailRow(body, "CCCD/Passport", lblCccd);
        addDetailRow(body, "Phòng / loại phòng", lblLoaiPhong);
        addDetailRow(body, "Tổng số người", lblSoNguoi);
        addDetailRow(body, "Ngày nhận", lblNgayNhanPhong);
        addDetailRow(body, "Ngày trả", lblNgayTraPhong);
        addDetailRow(body, "Trạng thái", lblTrangThai);
        addDetailRow(body, "Tổng tiền cọc", lblTienCoc);

        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setEditable(false);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(BODY_FONT);
        txtGhiChu.setForeground(TEXT_PRIMARY);
        txtGhiChu.setBackground(PANEL_SOFT);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);
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

        String[] columns = {"STT", "Phòng", "Loại phòng", "Check-in", "Check-out", "Số người", "Giá áp dụng", "Cọc chi tiết", "Trạng thái"};
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
        tblBookingDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tblBookingDetails);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG, BORDER_SOFT, TEXT_MUTED,
                "F1 Tạo booking", "F2 Cập nhật", "F3 Xác nhận", "F4 Nhận cọc", "F5 Hủy booking", "Enter Xem chi tiết"
        );
    }

    private void reloadData(boolean showMessage) {
        loadBookingsFromDatabase();
        cboTrangThai.setSelectedIndex(0);
        txtTuNgay.setText("");
        txtDenNgay.setText("");
        txtTuKhoa.setText("");
        showAllBookings();
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu đặt phòng.");
        }
    }

    private void showAllBookings() {
        filteredBookings.clear();
        filteredBookings.addAll(allBookings);
        refillTable();
    }

    private void loadBookingsFromDatabase() {
        allBookings.clear();
        List<DatPhong> list = datPhongDAO.getAll();
        for (DatPhong source : list) {
            BookingRecord booking = new BookingRecord();
            booking.maDatPhong = parseIntSafe(source.getMaDatPhong());
            booking.maKhachHang = parseIntSafe(source.getMaKhachHang());
            booking.khachHang = safeValue(source.getTenKhachHang(), "-");
            booking.soDienThoai = safeValue(source.getSoDienThoaiKhach(), "-");
            booking.cccd = safeValue(source.getCccdPassportKhach(), "-");
            booking.ngayDat = source.getNgayDat();
            booking.ngayNhanPhong = source.getNgayNhanPhong();
            booking.ngayTraPhong = source.getNgayTraPhong();
            booking.trangThai = safeValue(source.getTrangThaiDatPhong(), "Đã xác nhận");
            booking.nguonDat = safeValue(source.getNguonDatPhong(), "Đặt trước");
            booking.ghiChu = safeValue(source.getGhiChu(), "");
            booking.tongTienDatCoc = source.getTongTienDatCoc() > 0 ? source.getTongTienDatCoc() : source.getTienCoc();

            for (ChiTietDatPhong d : source.getChiTietDatPhongs()) {
                BookingDetailRecord detail = new BookingDetailRecord();
                detail.maChiTietDatPhong = parseIntSafe(d.getMaChiTietDatPhong());
                detail.maPhong = safeValue(d.getSoPhong(), safeValue(d.getMaPhong(), ""));
                detail.maPhongId = safeValue(d.getMaPhong(), "");
                detail.loaiPhong = safeValue(d.getTenLoaiPhong(), safeValue(d.getMaLoaiPhong(), ""));
                detail.checkInDuKien = d.getCheckInDuKien();
                detail.checkOutDuKien = d.getCheckOutDuKien();
                detail.soNguoi = d.getSoNguoi();
                detail.giaApDung = d.getGiaApDung();
                detail.tienDatCocChiTiet = d.getTienDatCocChiTiet();
                detail.trangThaiChiTiet = safeValue(d.getTrangThaiChiTiet(), booking.trangThai);
                detail.ghiChu = safeValue(d.getGhiChu(), "");
                booking.details.add(detail);
            }
            booking.syncDerivedData();
            if (booking.tongTienDatCoc <= 0) {
                booking.tongTienDatCoc = source.getTienCoc();
            }
            allBookings.add(booking);
        }

        if (allBookings.isEmpty()) {
            String err = datPhongDAO.getLastErrorMessage();
            if (err != null && !err.trim().isEmpty()) {
                showError("Không thể tải dữ liệu đặt phòng: " + err);
            }
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredBookings.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
        LocalDate fromDate = parseDate(txtTuNgay.getText().trim());
        LocalDate toDate = parseDate(txtDenNgay.getText().trim());

        if (!txtTuNgay.getText().trim().isEmpty() && fromDate == null) {
            showWarning("Từ ngày không đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (!txtDenNgay.getText().trim().isEmpty() && toDate == null) {
            showWarning("Đến ngày không đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showWarning("Khoảng ngày không hợp lệ.");
            return;
        }

        for (BookingRecord booking : allBookings) {
            if (!"Tất cả".equals(trangThai) && !booking.trangThai.equalsIgnoreCase(trangThai)) {
                continue;
            }

            if (fromDate != null && booking.ngayTraPhong != null && booking.ngayTraPhong.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && booking.ngayNhanPhong != null && booking.ngayNhanPhong.isAfter(toDate)) {
                continue;
            }

            if (!tuKhoa.isEmpty()) {
                String source = ("DP" + booking.maDatPhong + " " + booking.khachHang + " " + booking.soDienThoai + " " + booking.getRoomSummary())
                        .toLowerCase(Locale.ROOT);
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
                    booking.trangThai,
                    booking.details.size(),
                    booking.getTotalGuests(),
                    formatMoney(booking.tongTienDatCoc),
                    booking.ghiChu
            });
        }
        if (!filteredBookings.isEmpty()) {
            tblDatPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredBookings.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(BookingRecord booking) {
        lblMaDatPhong.setText("DP" + booking.maDatPhong);
        lblKhachHang.setText(booking.khachHang);
        lblSoDienThoai.setText(booking.soDienThoai);
        lblCccd.setText(booking.cccd);
        lblLoaiPhong.setText(booking.getRoomSummary());
        lblSoNguoi.setText(String.valueOf(booking.getTotalGuests()));
        lblNgayNhanPhong.setText(booking.formatNgayNhanPhong());
        lblNgayTraPhong.setText(booking.formatNgayTraPhong());
        lblTrangThai.setText(booking.trangThai);
        lblTienCoc.setText(formatMoney(booking.tongTienDatCoc));
        txtGhiChu.setText(booking.ghiChu.isEmpty() ? "-" : booking.ghiChu);
        refillBookingDetailTable(booking);
    }

    private void clearDetailPanel() {
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
        bookingDetailModel.setRowCount(0);
    }

    private void refillBookingDetailTable(BookingRecord booking) {
        bookingDetailModel.setRowCount(0);
        for (int i = 0; i < booking.details.size(); i++) {
            BookingDetailRecord d = booking.details.get(i);
            bookingDetailModel.addRow(new Object[]{
                    i + 1,
                    d.maPhong,
                    d.loaiPhong,
                    d.formatCheckIn(),
                    d.formatCheckOut(),
                    d.soNguoi,
                    formatMoney(d.giaApDung),
                    formatMoney(d.tienDatCocChiTiet),
                    d.trangThaiChiTiet
            });
        }
    }

    private BookingRecord getSelectedBooking() {
        int row = tblDatPhong.getSelectedRow();
        if (row < 0 || row >= filteredBookings.size()) {
            showWarning("Vui lòng chọn một booking.");
            return null;
        }
        return filteredBookings.get(row);
    }

    private void openCreateBookingDialog() {
        new BookingEditorDialog(this, null).setVisible(true);
    }

    private void openUpdateBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new BookingEditorDialog(this, booking).setVisible(true);
        }
    }

    private void openConfirmBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new ConfirmBookingDialog(this, booking).setVisible(true);
        }
    }

    private void openDepositDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new DepositDialog(this, booking).setVisible(true);
        }
    }

    private void openCancelBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new CancelBookingDialog(this, booking).setVisible(true);
        }
    }

    private void openViewBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new ViewBookingDialog(this, booking).setVisible(true);
        }
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "datphong-f1", this::openCreateBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "datphong-f2", this::openUpdateBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "datphong-f3", this::openConfirmBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "datphong-f4", this::openDepositDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "datphong-f5", this::openCancelBookingDialog);
        ScreenUIHelper.registerShortcut(this, "ENTER", "datphong-enter", this::openViewBookingDialog);
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

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private LocalDate requireDate(String value, String message) {
        LocalDate date = parseDate(value);
        if (date == null) {
            showError(message);
        }
        return date;
    }

    private double parseMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0d;
        }
        try {
            return Double.parseDouble(value.trim().replace(".", ""));
        } catch (NumberFormatException ex) {
            return -1d;
        }
    }

    private String formatMoney(double value) {
        return String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }

    private int parseIntSafe(String value) {
        try {
            return value == null || value.trim().isEmpty() ? 0 : Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private Connection getReadyConnection() {
        Connection con = ConnectDB.getConnection();
        if (con != null) {
            return con;
        }
        try {
            java.lang.reflect.Method m = ConnectDB.class.getMethod("connect");
            m.invoke(null);
        } catch (Exception ignored) {
        }
        return ConnectDB.getConnection();
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
        JComboBox<String> box = new JComboBox<String>(values);
        box.setFont(BODY_FONT);
        box.setPreferredSize(new Dimension(180, 34));
        box.setMaximumSize(new Dimension(260, 34));
        return box;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(180, 34));
        field.setMaximumSize(new Dimension(280, 34));
        return field;
    }

    private JButton createPrimaryButton(String text, Color background, Color foreground, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
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
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
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

    private JLabel createValueTag(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private Integer findExistingCustomerByPhoneOrPassport(Connection con, String phone, String cccd) {
        String sql = "SELECT TOP 1 maKhachHang FROM KhachHang WHERE soDienThoai = ? OR (? <> '' AND cccdPassport = ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
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


    private Integer findOrCreateCustomer(Connection con, String hoTen, LocalDate ngaySinh, String phone, String cccd) throws Exception {
        Integer existing = findExistingCustomerByPhoneOrPassport(con, phone, cccd);
        if (existing != null) {
            if (ngaySinh != null) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE KhachHang SET ngaySinh = ? WHERE maKhachHang = ?")) {
                    ps.setDate(1, Date.valueOf(ngaySinh));
                    ps.setInt(2, existing.intValue());
                    ps.executeUpdate();
                }
            }
            return existing;
        }

        String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) "
                + "VALUES (?, N'Khác', ?, ?, '', ?, '', N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', ?, N'Tạo từ màn đặt phòng')";
        try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hoTen);
            if (ngaySinh == null) {
                ps.setNull(2, java.sql.Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(ngaySinh));
            }
            ps.setString(3, phone);
            ps.setString(4, cccd);
            ps.setString(5, username);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private int findEmployeeIdByUsername(Connection con) {
        String sql = "SELECT TOP 1 nv.maNhanVien FROM TaiKhoan tk JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien WHERE tk.tenDangNhap = ?";
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


    private LocalDate loadCustomerNgaySinh(int maKhachHang) {
        if (maKhachHang <= 0) {
            return null;
        }
        Connection con = getReadyConnection();
        if (con == null) {
            return null;
        }
        String sql = "SELECT ngaySinh FROM KhachHang WHERE maKhachHang = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getDate(1) != null) {
                    return rs.getDate(1).toLocalDate();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void refreshAllOpenInstances() {
        List<DatPhongGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new ArrayList<DatPhongGUI>(OPEN_INSTANCES);
        }
        for (DatPhongGUI gui : snapshot) {
            if (gui != null) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        gui.reloadData(false);
                    }
                });
            }
        }
    }

    private abstract class BaseBookingDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseBookingDialog(Frame owner, String title, int width, int height) {
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

    private final class BookingEditorDialog extends BaseBookingDialog {
        private final BookingRecord editingBooking;
        private final boolean editing;
        private final List<BookingDetailRecord> detailRows = new ArrayList<BookingDetailRecord>();

        private JTextField txtMaBooking;
        private AppDatePickerField txtNgayDatDialog;
        private JComboBox<String> cboNguonBookingDialog;
        private JTextField txtHoTen;
        private JTextField txtSdt;
        private AppDatePickerField txtNgaySinhKhach;
        private JTextField txtCccdDialog;
        private JTextField txtTongDatCocDialog;
        private JTextArea txtGhiChuDialog;

        private JTable tblBookingDetailDialog;
        private DefaultTableModel bookingDetailDialogModel;
        private JLabel lblDetailSummary;

        private BookingEditorDialog(Frame owner, BookingRecord booking) {
            super(owner, booking == null ? "Tạo booking" : "Cập nhật booking", 1040, 760);
            this.editingBooking = booking;
            this.editing = booking != null;

            if (editing) {
                for (BookingDetailRecord detail : booking.details) {
                    detailRows.add(detail.copy());
                }
            }

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    editing ? "CẬP NHẬT BOOKING" : "TẠO BOOKING",
                    "Một khách có thể đặt một hoặc nhiều phòng. Mỗi dòng chi tiết tương ứng một phòng cụ thể."
            ), BorderLayout.NORTH);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildHeaderSection(), buildDetailSection());
            splitPane.setBorder(null);
            splitPane.setOpaque(false);
            splitPane.setResizeWeight(0.38);
            splitPane.setDividerLocation(360);
            splitPane.setContinuousLayout(true);
            content.add(splitPane, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            JButton btnSave = createPrimaryButton(editing ? "Lưu cập nhật" : "Lưu booking", new Color(37, 99, 235), Color.WHITE, e -> submit());
            content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);

            add(content, BorderLayout.CENTER);
        }

        private JPanel buildHeaderSection() {
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

            txtMaBooking = createInputField(editing ? "DP" + editingBooking.maDatPhong : "(Tự sinh)");
            txtMaBooking.setEditable(false);
            txtNgayDatDialog = new AppDatePickerField(editing && editingBooking.ngayDat != null ? editingBooking.formatNgayDat() : LocalDate.now().format(DATE_FORMAT), true);
            cboNguonBookingDialog = createComboBox(new String[]{"Đặt trước", "Walk-in"});
            txtHoTen = createInputField(editing ? editingBooking.khachHang : "");
            txtSdt = createInputField(editing ? editingBooking.soDienThoai : "");
            LocalDate customerBirthDate = editing ? loadCustomerNgaySinh(editingBooking.maKhachHang) : null;
            txtNgaySinhKhach = new AppDatePickerField(customerBirthDate == null ? "" : customerBirthDate.format(DATE_FORMAT), false);
            txtCccdDialog = createInputField(editing ? editingBooking.cccd : "");
            txtTongDatCocDialog = createInputField(editing ? formatMoney(editingBooking.tongTienDatCoc) : "0");
            txtTongDatCocDialog.setEditable(false);
            txtGhiChuDialog = createDialogTextArea(4);

            if (editing) {
                txtGhiChuDialog.setText(editingBooking.ghiChu);
                cboNguonBookingDialog.setSelectedItem(editingBooking.nguonDat);
            }

            addFormRow(form, gbc, 0, "Mã booking", txtMaBooking);
            addFormRow(form, gbc, 1, "Ngày đặt", txtNgayDatDialog);
            addFormRow(form, gbc, 2, "Nguồn booking", cboNguonBookingDialog);
            addFormRow(form, gbc, 3, "Họ tên KH", txtHoTen);
            addFormRow(form, gbc, 4, "SĐT", txtSdt);
            addFormRow(form, gbc, 5, "Ngày sinh", txtNgaySinhKhach);
            addFormRow(form, gbc, 6, "CCCD/Passport", txtCccdDialog);
            addFormRow(form, gbc, 7, "Tổng tiền cọc", txtTongDatCocDialog);
            addFormRow(form, gbc, 8, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            card.add(wrapper, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildDetailSection() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);

            JLabel lblSection = new JLabel("DETAIL - CÁC DÒNG CHI TIẾT ĐẶT PHÒNG");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            String[] columns = {"STT", "Phòng", "Loại phòng", "Check-in", "Check-out", "Số người", "Giá áp dụng", "Cọc chi tiết", "Trạng thái"};
            bookingDetailDialogModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            tblBookingDetailDialog = new JTable(bookingDetailDialogModel);
            tblBookingDetailDialog.setFont(BODY_FONT);
            tblBookingDetailDialog.setRowHeight(30);
            tblBookingDetailDialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(tblBookingDetailDialog);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.setOpaque(false);
            actions.add(createPrimaryButton("Thêm dòng phòng", new Color(59, 130, 246), Color.WHITE, e -> openBookingDetailDialog(null)));
            actions.add(createOutlineButton("Sửa dòng", new Color(245, 158, 11), e -> editSelectedDetailRow()));
            actions.add(createOutlineButton("Xóa dòng", new Color(220, 38, 38), e -> removeSelectedDetailRow()));

            lblDetailSummary = new JLabel();
            lblDetailSummary.setFont(BODY_FONT);
            lblDetailSummary.setForeground(TEXT_MUTED);

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(actions, BorderLayout.CENTER);

            JPanel tableWrap = new JPanel(new BorderLayout(0, 10));
            tableWrap.setOpaque(false);
            tableWrap.add(scrollPane, BorderLayout.CENTER);
            tableWrap.add(lblDetailSummary, BorderLayout.SOUTH);

            card.add(wrapper, BorderLayout.NORTH);
            card.add(tableWrap, BorderLayout.CENTER);
            refillBookingDetailDialogTable();
            return card;
        }

        private void refillBookingDetailDialogTable() {
            bookingDetailDialogModel.setRowCount(0);
            double totalDeposit = 0d;
            for (int i = 0; i < detailRows.size(); i++) {
                BookingDetailRecord d = detailRows.get(i);
                bookingDetailDialogModel.addRow(new Object[]{
                        i + 1,
                        d.maPhong,
                        d.loaiPhong,
                        d.formatCheckIn(),
                        d.formatCheckOut(),
                        d.soNguoi,
                        formatMoney(d.giaApDung),
                        formatMoney(d.tienDatCocChiTiet),
                        d.trangThaiChiTiet
                });
                totalDeposit += d.tienDatCocChiTiet;
            }
            txtTongDatCocDialog.setText(formatMoney(totalDeposit));
            lblDetailSummary.setText("Tổng số dòng: " + detailRows.size() + " | Tổng cọc detail: " + formatMoney(totalDeposit));
        }

        private void openBookingDetailDialog(BookingDetailRecord detail) {
            new BookingDetailEditorDialog(DatPhongGUI.this, detail).setVisible(true);
        }

        private void editSelectedDetailRow() {
            int row = tblBookingDetailDialog.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                showWarning("Vui lòng chọn dòng cần sửa.");
                return;
            }
            openBookingDetailDialog(detailRows.get(row));
        }

        private void removeSelectedDetailRow() {
            int row = tblBookingDetailDialog.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                showWarning("Vui lòng chọn dòng cần xóa.");
                return;
            }
            detailRows.remove(row);
            refillBookingDetailDialogTable();
        }

        private void submit() {
            if (txtHoTen.getText().trim().isEmpty() || txtSdt.getText().trim().isEmpty()) {
                showError("Họ tên và số điện thoại khách hàng không được rỗng.");
                return;
            }
            LocalDate ngayDat = requireDate(txtNgayDatDialog.getText().trim(), "Ngày đặt không hợp lệ.");
            if (ngayDat == null) {
                return;
            }
            String ngaySinhText = txtNgaySinhKhach.getText() == null ? "" : txtNgaySinhKhach.getText().trim();
            LocalDate ngaySinhKhach = null;
            if (!ngaySinhText.isEmpty()) {
                ngaySinhKhach = requireDate(ngaySinhText, "Ngày sinh khách hàng không hợp lệ.");
                if (ngaySinhKhach == null) {
                    return;
                }
            }
            if (detailRows.isEmpty()) {
                showError("Phải có ít nhất một dòng chi tiết phòng.");
                return;
            }

            Connection con = getReadyConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }

            try {
                Integer maKhachHang = findOrCreateCustomer(con, txtHoTen.getText().trim(), ngaySinhKhach, txtSdt.getText().trim(), txtCccdDialog.getText().trim());
                if (maKhachHang == null) {
                    showError("Không thể tạo hoặc tìm khách hàng.");
                    return;
                }

                int maNhanVien = findEmployeeIdByUsername(con);

                DatPhong datPhong = new DatPhong();
                if (editing) {
                    datPhong.setMaDatPhong(String.valueOf(editingBooking.maDatPhong));
                    datPhong.setTrangThaiDatPhong(editingBooking.trangThai);
                } else {
                    datPhong.setTrangThaiDatPhong("Đã xác nhận");
                }

                datPhong.setMaKhachHang(String.valueOf(maKhachHang.intValue()));
                datPhong.setMaNhanVien(String.valueOf(maNhanVien));
                datPhong.setNgayDat(ngayDat);
                datPhong.setNgayNhanPhong(findMinCheckIn(detailRows));
                datPhong.setNgayTraPhong(findMaxCheckOut(detailRows));
                datPhong.setSoLuongPhong(detailRows.size());
                datPhong.setSoNguoi(totalGuests(detailRows));
                datPhong.setTienCoc(totalDeposit(detailRows));
                datPhong.setTongTienDatCoc(totalDeposit(detailRows));
                datPhong.setNguonDatPhong(valueOf(cboNguonBookingDialog.getSelectedItem()));
                datPhong.setGhiChu(txtGhiChuDialog.getText().trim());

                // Header maBangGia: lấy từ dòng đầu tiên nếu có
                if (!detailRows.isEmpty()) {
                    datPhong.setMaBangGia(detailRows.get(0).maBangGia);
                }

                List<ChiTietDatPhong> details = new ArrayList<ChiTietDatPhong>();
                for (BookingDetailRecord row : detailRows) {
                    ChiTietDatPhong d = new ChiTietDatPhong();
                    d.setMaChiTietDatPhong(row.maChiTietDatPhong <= 0 ? "" : String.valueOf(row.maChiTietDatPhong));
                    d.setMaPhong(row.maPhongId);
                    d.setSoPhong(row.maPhong);
                    d.setMaLoaiPhong(row.maLoaiPhong);
                    d.setTenLoaiPhong(row.loaiPhong);
                    d.setMaBangGia(row.maBangGia);
                    d.setCheckInDuKien(row.checkInDuKien);
                    d.setCheckOutDuKien(row.checkOutDuKien);
                    d.setSoNguoi(row.soNguoi);
                    d.setGiaApDung(row.giaApDung);
                    d.setTienDatCocChiTiet(row.tienDatCocChiTiet);
                    d.setTrangThaiChiTiet(row.trangThaiChiTiet);
                    d.setGhiChu(row.ghiChu);
                    details.add(d);
                }
                datPhong.setChiTietDatPhongs(details);
                datPhong.dongBoTongTienTuChiTiet();

                boolean ok = editing ? datPhongDAO.update(datPhong) : datPhongDAO.insert(datPhong);
                if (!ok) {
                    showError("Không thể lưu booking: " + safeValue(datPhongDAO.getLastErrorMessage(), "Không xác định."));
                    return;
                }

                reloadData(false);
                refreshAllOpenInstances();
                try {
                    KhachHangGUI.refreshAllOpenInstances();
                } catch (Throwable ignored) {
                }

                showSuccess(editing ? "Cập nhật booking thành công." : "Tạo booking thành công.");
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Không thể lưu booking: " + e.getMessage());
            }
        }

        private LocalDate findMinCheckIn(List<BookingDetailRecord> details) {
            LocalDate min = details.get(0).checkInDuKien;
            for (BookingDetailRecord d : details) {
                if (d.checkInDuKien != null && d.checkInDuKien.isBefore(min)) {
                    min = d.checkInDuKien;
                }
            }
            return min;
        }

        private LocalDate findMaxCheckOut(List<BookingDetailRecord> details) {
            LocalDate max = details.get(0).checkOutDuKien;
            for (BookingDetailRecord d : details) {
                if (d.checkOutDuKien != null && d.checkOutDuKien.isAfter(max)) {
                    max = d.checkOutDuKien;
                }
            }
            return max;
        }

        private int totalGuests(List<BookingDetailRecord> details) {
            int total = 0;
            for (BookingDetailRecord d : details) {
                total += d.soNguoi;
            }
            return total;
        }

        private double totalDeposit(List<BookingDetailRecord> details) {
            double total = 0d;
            for (BookingDetailRecord d : details) {
                total += d.tienDatCocChiTiet;
            }
            return total;
        }

        private final class BookingDetailEditorDialog extends BaseBookingDialog {
            private final BookingDetailRecord editingDetail;
            private JComboBox<RoomOption> cboRoom;
            private JTextField txtLoaiPhong;
            private AppDatePickerField txtCheckInDialog;
            private AppDatePickerField txtCheckOutDialog;
            private JTextField txtSoNguoiDialog;
            private JTextField txtGiaApDungDialog;
            private JTextField txtDatCocDialog;
            private JTextArea txtGhiChuChiTietDialog;
            private final List<RoomOption> roomOptions = new ArrayList<RoomOption>();

            private BookingDetailEditorDialog(Frame owner, BookingDetailRecord detail) {
                super(owner, detail == null ? "Thêm chi tiết đặt phòng" : "Cập nhật chi tiết đặt phòng", 760, 560);
                this.editingDetail = detail;

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader(
                        detail == null ? "THÊM CHI TIẾT ĐẶT PHÒNG" : "CẬP NHẬT CHI TIẾT ĐẶT PHÒNG",
                        "Chọn trực tiếp phòng, hệ thống tự hiển thị loại phòng tương ứng."
                ), BorderLayout.NORTH);

                JPanel form = createDialogFormPanel();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new java.awt.Insets(6, 0, 6, 12);
                gbc.anchor = GridBagConstraints.WEST;

                roomOptions.addAll(loadRoomOptions(detail == null ? null : detail.maPhongId));
                cboRoom = new JComboBox<RoomOption>(roomOptions.toArray(new RoomOption[0]));
                cboRoom.setFont(BODY_FONT);

                txtLoaiPhong = createInputField("");
                txtLoaiPhong.setEditable(false);

                txtCheckInDialog = new AppDatePickerField(detail == null ? LocalDate.now().format(DATE_FORMAT) : detail.formatCheckIn(), true);
                txtCheckOutDialog = new AppDatePickerField(detail == null ? LocalDate.now().plusDays(1).format(DATE_FORMAT) : detail.formatCheckOut(), true);
                txtSoNguoiDialog = createInputField(detail == null ? "2" : String.valueOf(detail.soNguoi));
                txtGiaApDungDialog = createInputField(detail == null ? "0" : formatMoney(detail.giaApDung));
                txtDatCocDialog = createInputField(detail == null ? "0" : formatMoney(detail.tienDatCocChiTiet));
                txtGhiChuChiTietDialog = createDialogTextArea(3);

                if (detail != null) {
                    selectRoomOption(detail.maPhongId);
                    txtGhiChuChiTietDialog.setText(detail.ghiChu);
                } else if (!roomOptions.isEmpty()) {
                    cboRoom.setSelectedIndex(0);
                }

                cboRoom.addActionListener(e -> syncSelectedRoomInfo());
                syncSelectedRoomInfo();

                addFormRow(form, gbc, 0, "Phòng", cboRoom);
                addFormRow(form, gbc, 1, "Loại phòng", txtLoaiPhong);
                addFormRow(form, gbc, 2, "Check-in dự kiến", txtCheckInDialog);
                addFormRow(form, gbc, 3, "Check-out dự kiến", txtCheckOutDialog);
                addFormRow(form, gbc, 4, "Số người", txtSoNguoiDialog);
                addFormRow(form, gbc, 5, "Giá áp dụng", txtGiaApDungDialog);
                addFormRow(form, gbc, 6, "Cọc chi tiết", txtDatCocDialog);
                addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuChiTietDialog));

                JPanel card = createDialogCardPanel();
                card.add(form, BorderLayout.CENTER);
                content.add(card, BorderLayout.CENTER);

                JButton btnPrimary = createPrimaryButton(detail == null ? "Lưu dòng" : "Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit());
                JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
                content.add(buildDialogButtons(btnCancel, btnPrimary), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
            }

            private void selectRoomOption(String maPhongId) {
                if (maPhongId == null || maPhongId.trim().isEmpty()) {
                    return;
                }
                for (int i = 0; i < cboRoom.getItemCount(); i++) {
                    RoomOption option = cboRoom.getItemAt(i);
                    if (maPhongId.equals(option.maPhong)) {
                        cboRoom.setSelectedIndex(i);
                        return;
                    }
                }
            }

            private void syncSelectedRoomInfo() {
                RoomOption option = (RoomOption) cboRoom.getSelectedItem();
                if (option == null) {
                    txtLoaiPhong.setText("");
                    return;
                }
                txtLoaiPhong.setText(option.tenLoaiPhong);
                if (parseMoney(txtGiaApDungDialog.getText().trim()) <= 0 && option.giaMacDinh > 0) {
                    txtGiaApDungDialog.setText(formatMoney(option.giaMacDinh));
                }
            }

            private void submit() {
                RoomOption option = (RoomOption) cboRoom.getSelectedItem();
                if (option == null) {
                    showError("Vui lòng chọn phòng.");
                    return;
                }

                LocalDate checkIn = requireDate(txtCheckInDialog.getText().trim(), "Check-in dự kiến không hợp lệ.");
                LocalDate checkOut = requireDate(txtCheckOutDialog.getText().trim(), "Check-out dự kiến không hợp lệ.");
                if (checkIn == null || checkOut == null) {
                    return;
                }
                if (!checkOut.isAfter(checkIn)) {
                    showError("Check-out dự kiến phải lớn hơn check-in dự kiến.");
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

                double giaApDung = parseMoney(txtGiaApDungDialog.getText().trim());
                double tienCoc = parseMoney(txtDatCocDialog.getText().trim());
                if (giaApDung < 0 || tienCoc < 0) {
                    showError("Giá áp dụng và cọc chi tiết phải hợp lệ.");
                    return;
                }

                // tránh chọn trùng phòng trong cùng booking
                for (BookingDetailRecord row : detailRows) {
                    if (row != editingDetail && option.maPhong.equals(row.maPhongId)) {
                        showError("Phòng này đã có trong booking.");
                        return;
                    }
                }

                BookingDetailRecord target = editingDetail == null ? new BookingDetailRecord() : editingDetail;
                target.maPhongId = option.maPhong;
                target.maPhong = option.soPhong;
                target.maLoaiPhong = option.maLoaiPhong;
                target.maBangGia = option.maBangGia;
                target.loaiPhong = option.tenLoaiPhong;
                target.checkInDuKien = checkIn;
                target.checkOutDuKien = checkOut;
                target.soNguoi = soNguoi;
                target.giaApDung = giaApDung;
                target.tienDatCocChiTiet = tienCoc;
                target.trangThaiChiTiet = "Đã xác nhận";
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

        private ConfirmBookingDialog(Frame owner, BookingRecord booking) {
            super(owner, "Xác nhận booking", 520, 280);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("XÁC NHẬN BOOKING", "Cập nhật trạng thái booking sang Đã xác nhận."), BorderLayout.NORTH);

            JTextArea area = createDialogTextArea(5);
            area.setEditable(false);
            area.setBackground(PANEL_SOFT);
            area.setText("Mã booking: DP" + booking.maDatPhong + "\nKhách hàng: " + booking.khachHang + "\nPhòng: " + booking.getRoomSummary());

            JPanel card = createDialogCardPanel();
            card.add(area, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnOk = createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnOk), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            boolean ok = datPhongDAO.updateTrangThai(String.valueOf(booking.maDatPhong), "Đã xác nhận");
            if (!ok) {
                showError("Không thể xác nhận booking: " + safeValue(datPhongDAO.getLastErrorMessage(), "Không xác định."));
                return;
            }
            reloadData(false);
            showSuccess("Xác nhận booking thành công.");
            dispose();
        }
    }

    private final class DepositDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private DepositDialog(Frame owner, BookingRecord booking) {
            super(owner, "Nhận cọc", 560, 320);
            this.booking = booking;

            JTextField txtThuThem = createInputField("");
            JTextArea txtNote = createDialogTextArea(3);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("NHẬN CỌC BOOKING", "Nhập số tiền cọc thu thêm cho booking đã chọn."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã booking", createValueTag("DP" + booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Đã cọc", createValueTag(formatMoney(booking.tongTienDatCoc)));
            addFormRow(form, gbc, 3, "Thu thêm", txtThuThem);
            addFormRow(form, gbc, 4, "Ghi chú", new JScrollPane(txtNote));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(txtThuThem, txtNote));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextField txtThuThem, JTextArea txtNote) {
            double thuThem = parseMoney(txtThuThem.getText().trim());
            if (thuThem < 0) {
                showError("Số tiền thu thêm không hợp lệ.");
                return;
            }

            Connection con = getReadyConnection();
            if (con == null) {
                showError("Không thể kết nối cơ sở dữ liệu.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET tienCoc = ISNULL(tienCoc, 0) + ? WHERE maDatPhong = ?")) {
                ps.setDouble(1, thuThem);
                ps.setInt(2, booking.maDatPhong);
                ps.executeUpdate();
                reloadData(false);
                showSuccess("Nhận cọc thành công.");
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Không thể nhận cọc: " + ex.getMessage());
            }
        }
    }

    private final class CancelBookingDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private CancelBookingDialog(Frame owner, BookingRecord booking) {
            super(owner, "Hủy booking", 560, 340);
            this.booking = booking;

            JTextField txtReason = createInputField("");
            JTextArea txtNote = createDialogTextArea(3);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("HỦY BOOKING", "Booking sẽ chuyển sang trạng thái Đã hủy."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã booking", createValueTag("DP" + booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Lý do hủy", txtReason);
            addFormRow(form, gbc, 3, "Ghi chú", new JScrollPane(txtNote));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Xác nhận hủy", new Color(220, 38, 38), Color.WHITE, e -> submit(txtReason));
            JButton btnCancel = createOutlineButton("Hủy thao tác", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextField txtReason) {
            if (txtReason.getText().trim().isEmpty()) {
                showError("Phải nhập lý do hủy.");
                return;
            }
            boolean ok = datPhongDAO.updateTrangThai(String.valueOf(booking.maDatPhong), "Đã hủy");
            if (!ok) {
                showError("Không thể hủy booking: " + safeValue(datPhongDAO.getLastErrorMessage(), "Không xác định."));
                return;
            }
            reloadData(false);
            showSuccess("Hủy booking thành công.");
            dispose();
        }
    }

    private final class ViewBookingDialog extends BaseBookingDialog {
        private ViewBookingDialog(Frame owner, BookingRecord booking) {
            super(owner, "Xem chi tiết booking", 720, 520);

            JTextArea area = createDialogTextArea(18);
            area.setEditable(false);
            area.setBackground(PANEL_SOFT);

            StringBuilder builder = new StringBuilder();
            builder.append("Mã booking: DP").append(booking.maDatPhong).append("\n");
            builder.append("Khách hàng: ").append(booking.khachHang).append("\n");
            builder.append("SĐT: ").append(booking.soDienThoai).append("\n");
            builder.append("Phòng / loại phòng: ").append(booking.getRoomSummary()).append("\n");
            builder.append("Ngày nhận: ").append(booking.formatNgayNhanPhong()).append("\n");
            builder.append("Ngày trả: ").append(booking.formatNgayTraPhong()).append("\n");
            builder.append("Trạng thái: ").append(booking.trangThai).append("\n");
            builder.append("Tổng cọc: ").append(formatMoney(booking.tongTienDatCoc)).append("\n\n");
            builder.append("Chi tiết:\n");
            for (BookingDetailRecord d : booking.details) {
                builder.append("- ").append(d.maPhong).append(" / ").append(d.loaiPhong)
                        .append(" | ").append(d.formatCheckIn()).append(" -> ").append(d.formatCheckOut())
                        .append(" | ").append(d.soNguoi).append(" khách")
                        .append(" | ").append(formatMoney(d.giaApDung)).append("\n");
            }
            area.setText(builder.toString());
            area.setCaretPosition(0);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHI TIẾT BOOKING", "Thông tin hiện tại của booking được chọn."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            card.add(new JScrollPane(area), BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class AppMessageDialog extends BaseBookingDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private List<RoomOption> loadRoomOptions(String includeRoomId) {
        List<RoomOption> options = new ArrayList<RoomOption>();
        Connection con = getReadyConnection();
        if (con == null) {
            return options;
        }

        String sql = "SELECT p.maPhong, p.soPhong, p.tang, p.trangThai, p.maLoaiPhong, lp.tenLoaiPhong, CAST(lp.giaThamChieu AS FLOAT) AS giaMacDinh "
                + "FROM Phong p "
                + "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE p.trangThai NOT IN (N'Đang ở', N'Bảo trì') "
                + "OR CAST(p.maPhong AS NVARCHAR(20)) = ? "
                + "ORDER BY TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong ASC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, includeRoomId == null ? "" : includeRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoomOption option = new RoomOption();
                    option.maPhong = String.valueOf(rs.getInt("maPhong"));
                    option.soPhong = safeValue(rs.getString("soPhong"), option.maPhong);
                    option.tang = safeValue(rs.getString("tang"), "");
                    option.trangThai = safeValue(rs.getString("trangThai"), "");
                    option.maLoaiPhong = String.valueOf(rs.getInt("maLoaiPhong"));
                    option.tenLoaiPhong = safeValue(rs.getString("tenLoaiPhong"), "");
                    option.giaMacDinh = rs.getDouble("giaMacDinh");
                    option.maBangGia = findBangGiaIdByLoaiPhongId(con, option.maLoaiPhong);
                    options.add(option);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return options;
    }

    private String findBangGiaIdByLoaiPhongId(Connection con, String maLoaiPhong) {
        if (con == null || maLoaiPhong == null || maLoaiPhong.trim().isEmpty()) {
            return "";
        }
        String sql = "SELECT TOP 1 maBangGia FROM BangGia WHERE maLoaiPhong = ? AND trangThai = N'Đang áp dụng' ORDER BY maBangGia DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(maLoaiPhong));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt(1));
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static final class RoomOption {
        private String maPhong;
        private String soPhong;
        private String tang;
        private String trangThai;
        private String maLoaiPhong;
        private String tenLoaiPhong;
        private String maBangGia;
        private double giaMacDinh;

        @Override
        public String toString() {
            return soPhong + " - " + tenLoaiPhong + " - " + tang + " - " + trangThai;
        }
    }

    private static final class BookingRecord {
        private int maDatPhong;
        private int maKhachHang;
        private String khachHang;
        private String soDienThoai;
        private String cccd;
        private LocalDate ngayDat;
        private LocalDate ngayNhanPhong;
        private LocalDate ngayTraPhong;
        private String nguonDat;
        private String trangThai;
        private String ghiChu;
        private double tongTienDatCoc;
        private final List<BookingDetailRecord> details = new ArrayList<BookingDetailRecord>();

        private void syncDerivedData() {
            if (details.isEmpty()) {
                return;
            }
            ngayNhanPhong = details.get(0).checkInDuKien;
            ngayTraPhong = details.get(0).checkOutDuKien;
            if (tongTienDatCoc <= 0) {
                tongTienDatCoc = 0d;
            }
            for (BookingDetailRecord d : details) {
                if (d.checkInDuKien != null && d.checkInDuKien.isBefore(ngayNhanPhong)) {
                    ngayNhanPhong = d.checkInDuKien;
                }
                if (d.checkOutDuKien != null && d.checkOutDuKien.isAfter(ngayTraPhong)) {
                    ngayTraPhong = d.checkOutDuKien;
                }
            }
        }

        private int getTotalGuests() {
            int total = 0;
            for (BookingDetailRecord d : details) {
                total += d.soNguoi;
            }
            return total;
        }

        private String getRoomSummary() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < details.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(details.get(i).maPhong).append(" / ").append(details.get(i).loaiPhong);
            }
            return sb.length() == 0 ? "-" : sb.toString();
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
        private String maPhongId;
        private String maPhong;
        private String maLoaiPhong;
        private String maBangGia;
        private String loaiPhong;
        private LocalDate checkInDuKien;
        private LocalDate checkOutDuKien;
        private int soNguoi;
        private double giaApDung;
        private double tienDatCocChiTiet;
        private String trangThaiChiTiet;
        private String ghiChu;

        private BookingDetailRecord copy() {
            BookingDetailRecord d = new BookingDetailRecord();
            d.maChiTietDatPhong = maChiTietDatPhong;
            d.maPhongId = maPhongId;
            d.maPhong = maPhong;
            d.maLoaiPhong = maLoaiPhong;
            d.maBangGia = maBangGia;
            d.loaiPhong = loaiPhong;
            d.checkInDuKien = checkInDuKien;
            d.checkOutDuKien = checkOutDuKien;
            d.soNguoi = soNguoi;
            d.giaApDung = giaApDung;
            d.tienDatCocChiTiet = tienDatCocChiTiet;
            d.trangThaiChiTiet = trangThaiChiTiet;
            d.ghiChu = ghiChu;
            return d;
        }

        private double computeThanhTien() {
            long nights = ChronoUnit.DAYS.between(checkInDuKien, checkOutDuKien);
            if (nights <= 0) {
                nights = 1;
            }
            return nights * giaApDung;
        }

        private String formatCheckIn() {
            return checkInDuKien == null ? "-" : checkInDuKien.format(DATE_FORMAT);
        }

        private String formatCheckOut() {
            return checkOutDuKien == null ? "-" : checkOutDuKien.format(DATE_FORMAT);
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }
}