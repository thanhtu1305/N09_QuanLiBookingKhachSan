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
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private static final String[] BOOKING_STATUS_OPTIONS = {"Mới tạo", "Đã xác nhận", "Đã cọc", "Đã check-in", "Đã hủy"};
    private static final String[] BOOKING_SOURCE_OPTIONS = {"Đặt trước", "Walk-in"};
    private static final String[] ROOM_TYPE_OPTIONS = {"Standard", "Deluxe", "Suite"};
    private static final String[] PRICE_TABLE_OPTIONS = {"BG01 - Standard", "BG02 - Deluxe", "BG03 - Suite"};

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

    public DatPhongGUI() {
        this("guest", "Lễ tân");
    }

    public DatPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý đặt phòng - Hotel PMS");
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

        JLabel lblSub = new JLabel("Theo dõi booking, tiền cọc và lịch nhận phòng bằng dữ liệu mẫu.");
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
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đã nhận cọc", "Đã hủy"});
        cboNguonDat = createComboBox(new String[]{"Tất cả", "Đặt trước", "Walk-in"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite"});
        txtTuNgay = new AppDatePickerField(LocalDate.now().format(DATE_FORMAT), true);
        txtDenNgay = new AppDatePickerField(LocalDate.now().plusDays(6).format(DATE_FORMAT), true);
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(260, 34));
        txtTuKhoa.setToolTipText("Mã đặt phòng / tên khách / số điện thoại");

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
        tblDatPhong.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblDatPhong.getTableHeader().setBackground(new Color(243, 244, 246));
        tblDatPhong.getTableHeader().setForeground(TEXT_PRIMARY);

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
        wrapper.add(buildDetailCard(), BorderLayout.CENTER);
        wrapper.add(buildDetailLinesCard(), BorderLayout.SOUTH);
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

        String[] columns = {"STT", "Loại phòng", "Phòng", "Check-in", "Check-out", "Số người", "Giá áp dụng", "Cọc chi tiết", "Trạng thái"};
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
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Tạo booking",
                "F2 Cập nhật",
                "F3 Xác nhận",
                "F4 Nhận cọc",
                "F5 Hủy booking",
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

    private void seedSampleData() {
        allBookings.clear();
    }

    private BookingRecord createSampleBooking(String maDatPhong, String maKhachHang, String khachHang, String soDienThoai, String cccd,
                                              LocalDate ngayDat, String nguonDat, String trangThai, String ghiChu,
                                              BookingDetailRecord... details) {
        BookingRecord booking = BookingRecord.createHeader(maDatPhong, maKhachHang, khachHang, soDienThoai, cccd, ngayDat, nguonDat, trangThai, ghiChu);
        for (BookingDetailRecord detail : details) {
            booking.details.add(detail);
        }
        booking.syncDerivedData();
        return booking;
    }

    private BookingDetailRecord createSampleBookingDetail(String maChiTiet, String loaiPhong, String soPhong, String bangGia,
                                                          String chiTietBangGia, LocalDate checkIn, LocalDate checkOut,
                                                          int soNguoi, double giaApDung, double tienDatCocChiTiet,
                                                          String trangThaiChiTiet, String yeuCauKhac, String ghiChu) {
        return BookingDetailRecord.create(maChiTiet, loaiPhong, soPhong, bangGia, chiTietBangGia, checkIn, checkOut,
                soNguoi, giaApDung, tienDatCocChiTiet, trangThaiChiTiet, yeuCauKhac, ghiChu);
    }

    private void reloadSampleData(boolean showMessage) {
        cboTrangThai.setSelectedIndex(0);
        cboNguonDat.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        txtTuNgay.setText(LocalDate.now().format(DATE_FORMAT));
        txtDenNgay.setText(LocalDate.now().plusDays(6).format(DATE_FORMAT));
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu mẫu đặt phòng.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredBookings.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String nguonDat = valueOf(cboNguonDat.getSelectedItem());
        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
        String fromText = txtTuNgay.getText().trim();
        String toText = txtDenNgay.getText().trim();

        LocalDate fromDate = parseDate(fromText);
        LocalDate toDate = parseDate(toText);
        if (!fromText.isEmpty() && fromDate == null) {
            showWarning("Từ ngày không đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (!toText.isEmpty() && toDate == null) {
            showWarning("Đến ngày không đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            showWarning("Khoảng ngày không hợp lệ. Vui lòng kiểm tra lại từ ngày và đến ngày.");
            return;
        }

        for (BookingRecord booking : allBookings) {
            if (!"Tất cả".equals(trangThai) && !booking.trangThai.equals(trangThai)) {
                continue;
            }
            if (!"Tất cả".equals(nguonDat) && !booking.nguonDat.equals(nguonDat)) {
                continue;
            }
            if (!"Tất cả".equals(loaiPhong) && !booking.matchesRoomType(loaiPhong)) {
                continue;
            }
            if (fromDate != null && booking.ngayNhanPhong.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && booking.ngayTraPhong.isAfter(toDate)) {
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
                    booking.maDatPhong,
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
            tblDatPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredBookings.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(BookingRecord booking) {
        lblMaDatPhong.setText(booking.maDatPhong);
        lblKhachHang.setText(booking.khachHang);
        lblSoDienThoai.setText(booking.soDienThoai);
        lblCccd.setText(booking.cccd);
        lblLoaiPhong.setText(booking.getRoomTypeSummary());
        lblSoNguoi.setText(String.valueOf(booking.getTotalGuests()));
        lblNgayNhanPhong.setText(booking.formatNgayNhanPhong());
        lblNgayTraPhong.setText(booking.formatNgayTraPhong());
        lblTrangThai.setText(booking.trangThai);
        lblTienCoc.setText(formatMoney(booking.tongTienDatCoc));
        txtGhiChu.setText(booking.ghiChu);
        txtGhiChu.setCaretPosition(0);
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
        if (bookingDetailModel != null) {
            bookingDetailModel.setRowCount(0);
        }
    }

    private void refillBookingDetailTable(BookingRecord booking) {
        if (bookingDetailModel == null) {
            return;
        }
        bookingDetailModel.setRowCount(0);
        for (int i = 0; i < booking.details.size(); i++) {
            BookingDetailRecord detail = booking.details.get(i);
            bookingDetailModel.addRow(new Object[]{
                    i + 1,
                    detail.loaiPhong,
                    detail.maPhong == null || detail.maPhong.trim().isEmpty() ? "Chưa gán" : detail.maPhong,
                    detail.formatCheckIn(),
                    detail.formatCheckOut(),
                    detail.soNguoi,
                    formatMoney(detail.giaApDung),
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

    private void openUpdateBookingDialog() {
        BookingRecord booking = getSelectedBooking();
        if (booking != null) {
            new BookingEditorDialog(this, booking).setVisible(true);
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

    private void addBooking(BookingRecord booking, String successMessage) {
        allBookings.add(0, booking);
        cboTrangThai.setSelectedIndex(0);
        cboNguonDat.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        txtTuNgay.setText(LocalDate.now().format(DATE_FORMAT));
        txtDenNgay.setText(LocalDate.now().plusDays(6).format(DATE_FORMAT));
        txtTuKhoa.setText("");
        applyFilters(false);
        selectBooking(booking);
        showSuccess(successMessage);
    }

    private void refreshBookingViews(BookingRecord booking, String successMessage) {
        applyFilters(false);
        selectBooking(booking);
        showSuccess(successMessage);
    }

    private void selectBooking(BookingRecord booking) {
        if (booking == null) {
            return;
        }
        int index = filteredBookings.indexOf(booking);
        if (index >= 0) {
            tblDatPhong.setRowSelectionInterval(index, index);
            updateDetailPanel(booking);
        } else if (!filteredBookings.isEmpty()) {
            tblDatPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredBookings.get(0));
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

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "datphong-f1", this::openCreateBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "datphong-f2", this::openUpdateBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "datphong-f3", this::openConfirmBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "datphong-f4", this::openDepositDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "datphong-f5", this::openCancelBookingDialog);
        ScreenUIHelper.registerShortcut(this, "ENTER", "datphong-enter", () -> {
            openViewBookingDialog();
        });
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

    private LocalDate requireDate(String value, String errorMessage) {
        LocalDate date = parseDate(value);
        if (date == null) {
            showError(errorMessage);
        }
        return date;
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
        private JTextField txtMaKh;
        private JTextField txtHoTen;
        private JTextField txtSdt;
        private JTextField txtCccdDialog;
        private JTextField txtTongDatCocDialog;
        private JTextArea txtGhiChuDialog;
        private JTable tblBookingDetailDialog;
        private DefaultTableModel bookingDetailDialogModel;
        private JLabel lblDetailSummary;
        private JTextArea txtSelectedDetailNote;

        private BookingEditorDialog(Frame owner, BookingRecord booking) {
            super(owner, booking == null ? "Tạo booking" : "Cập nhật booking", 980, 760);
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
                    "Phần trên là header của phiếu đặt phòng, phần dưới là các dòng chi tiết phòng theo mô hình header-detail."
            ), BorderLayout.NORTH);

            if (detailRows.isEmpty()) {
                loadSampleBookingDetailRows();
            }

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildHeaderSection(), buildDetailSection());
            splitPane.setBorder(null);
            splitPane.setOpaque(false);
            splitPane.setResizeWeight(0.38d);
            splitPane.setDividerLocation(360);
            splitPane.setContinuousLayout(true);
            body.add(splitPane, BorderLayout.CENTER);
            content.add(body, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit("Mới tạo"));
            JButton btnSaveConfirm = createOutlineButton("Lưu và xác nhận", new Color(59, 130, 246), e -> submit("Đã xác nhận"));
            JButton btnSaveCheckIn = createOutlineButton("Lưu và check-in", new Color(245, 158, 11), e -> submit("Đã check-in"));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            btnSaveConfirm.setVisible(!editing);
            btnSaveCheckIn.setVisible(!editing);

            content.add(buildDialogButtons(btnCancel, btnSaveCheckIn, btnSaveConfirm, btnSave), BorderLayout.SOUTH);
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

            txtMaBooking = createInputField(editing ? editingBooking.maDatPhong : "DP" + (240300 + allBookings.size() + 1));
            txtMaBooking.setEditable(!editing);
            txtNgayDatDialog = new AppDatePickerField(editing ? editingBooking.formatNgayDat() : LocalDate.now().format(DATE_FORMAT), true);
            cboNguonBookingDialog = createComboBox(BOOKING_SOURCE_OPTIONS);
            txtMaKh = createInputField(editing ? editingBooking.maKhachHang : "");
            txtHoTen = createInputField(editing ? editingBooking.khachHang : "");
            txtSdt = createInputField(editing ? editingBooking.soDienThoai : "");
            txtCccdDialog = createInputField(editing ? editingBooking.cccd : "");
            txtTongDatCocDialog = createInputField(editing ? formatMoney(editingBooking.tongTienDatCoc) : "0");
            txtTongDatCocDialog.setEditable(false);
            txtGhiChuDialog = createDialogTextArea(3);

            if (editing) {
                cboNguonBookingDialog.setSelectedItem(editingBooking.nguonDat);
                txtGhiChuDialog.setText(editingBooking.ghiChu);
            }

            JButton btnTimKh = createOutlineButton("Tìm KH", new Color(59, 130, 246), e -> showWarning("Chức năng tìm KH đang dùng demo data nội bộ."));
            JButton btnKhachMoi = createOutlineButton("Khách mới", new Color(22, 163, 74), e -> new QuickCustomerDialog(this).setVisible(true));

            JPanel maKhPanel = new JPanel(new BorderLayout(8, 0));
            maKhPanel.setOpaque(false);
            maKhPanel.add(txtMaKh, BorderLayout.CENTER);

            JPanel customerButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            customerButtons.setOpaque(false);
            customerButtons.add(btnTimKh);
            customerButtons.add(btnKhachMoi);
            maKhPanel.add(customerButtons, BorderLayout.EAST);

            addFormRow(form, gbc, 0, "Mã booking", txtMaBooking);
            addFormRow(form, gbc, 1, "Ngày đặt", txtNgayDatDialog);
            addFormRow(form, gbc, 2, "Nguồn booking", cboNguonBookingDialog);
            addFormRow(form, gbc, 3, "Mã KH", maKhPanel);
            addFormRow(form, gbc, 4, "Họ tên", txtHoTen);
            addFormRow(form, gbc, 5, "SĐT", txtSdt);
            addFormRow(form, gbc, 6, "CCCD/Passport", txtCccdDialog);
            addFormRow(form, gbc, 7, "Tổng tiền cọc", txtTongDatCocDialog);
            addFormRow(form, gbc, 8, "Ghi chú chung", new JScrollPane(txtGhiChuDialog));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            card.setPreferredSize(new Dimension(360, 0));
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

            String[] columns = {"STT", "Loại phòng", "Phòng", "Check-in", "Check-out", "Số người", "Giá áp dụng", "Cọc chi tiết", "Trạng thái", "Yêu cầu khác"};
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
            tblBookingDetailDialog.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

            JScrollPane scrollPane = new JScrollPane(tblBookingDetailDialog);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            scrollPane.setPreferredSize(new Dimension(0, 360));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.setOpaque(false);
            actions.add(createPrimaryButton("Thêm dòng phòng", new Color(59, 130, 246), Color.WHITE, e -> openBookingDetailDialog(null)));
            actions.add(createOutlineButton("Sửa dòng", new Color(245, 158, 11), e -> editSelectedDetailRow()));
            actions.add(createOutlineButton("Xóa dòng", new Color(220, 38, 38), e -> removeSelectedDetailRow()));

            lblDetailSummary = new JLabel();
            lblDetailSummary.setFont(BODY_FONT);
            lblDetailSummary.setForeground(TEXT_MUTED);

            txtSelectedDetailNote = createDialogTextArea(3);
            txtSelectedDetailNote.setEditable(false);
            txtSelectedDetailNote.setBackground(PANEL_SOFT);

            JPanel footer = new JPanel(new BorderLayout(0, 8));
            footer.setOpaque(false);
            footer.add(lblDetailSummary, BorderLayout.NORTH);
            footer.add(new JScrollPane(txtSelectedDetailNote), BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(actions, BorderLayout.NORTH);
            center.add(scrollPane, BorderLayout.CENTER);
            center.add(footer, BorderLayout.SOUTH);

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(center, BorderLayout.CENTER);
            card.add(wrapper, BorderLayout.CENTER);
            tblBookingDetailDialog.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    updateSelectedDetailPreview();
                }
            });
            refillBookingDetailDialogTable();
            return card;
        }

        private void refillBookingDetailDialogTable() {
            bookingDetailDialogModel.setRowCount(0);
            for (int i = 0; i < detailRows.size(); i++) {
                BookingDetailRecord detail = detailRows.get(i);
                bookingDetailDialogModel.addRow(new Object[]{
                        i + 1,
                        detail.loaiPhong,
                        detail.maPhong == null || detail.maPhong.trim().isEmpty() ? "Chưa gán" : detail.maPhong,
                        detail.formatCheckIn(),
                        detail.formatCheckOut(),
                        detail.soNguoi,
                        formatMoney(detail.giaApDung),
                        formatMoney(detail.tienDatCocChiTiet),
                        detail.trangThaiChiTiet,
                        detail.yeuCauKhac
                });
            }
            if (!detailRows.isEmpty()) {
                tblBookingDetailDialog.setRowSelectionInterval(0, 0);
            }
            refreshDepositSummary();
            refreshDetailSummary();
            updateSelectedDetailPreview();
        }

        private void loadSampleBookingDetailRows() {
            detailRows.clear();
        }

        private BookingDetailRecord createSampleDetailRow(String maChiTiet, String loaiPhong, String maPhong, String maBangGia,
                                                          String maChiTietBangGia, LocalDate checkIn, LocalDate checkOut,
                                                          int soNguoi, double giaApDung, double tienDatCocChiTiet,
                                                          String trangThaiChiTiet, String yeuCauKhac, String ghiChu) {
            return BookingDetailRecord.create(
                    maChiTiet,
                    loaiPhong,
                    maPhong,
                    maBangGia,
                    maChiTietBangGia,
                    checkIn,
                    checkOut,
                    soNguoi,
                    giaApDung,
                    tienDatCocChiTiet,
                    trangThaiChiTiet,
                    yeuCauKhac,
                    ghiChu
            );
        }

        private void refreshDepositSummary() {
            double totalDeposit = 0;
            for (BookingDetailRecord detail : detailRows) {
                totalDeposit += detail.tienDatCocChiTiet;
            }
            txtTongDatCocDialog.setText(formatMoney(totalDeposit));
        }

        private void refreshDetailSummary() {
            if (lblDetailSummary == null) {
                return;
            }
            double totalDeposit = 0;
            for (BookingDetailRecord detail : detailRows) {
                totalDeposit += detail.tienDatCocChiTiet;
            }
            lblDetailSummary.setText("Tổng số dòng: " + detailRows.size() + "    Tổng cọc detail: " + formatMoney(totalDeposit));
        }

        private void updateSelectedDetailPreview() {
            if (txtSelectedDetailNote == null) {
                return;
            }
            int row = tblBookingDetailDialog == null ? -1 : tblBookingDetailDialog.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                txtSelectedDetailNote.setText("Chọn một dòng để xem yêu cầu khác và ghi chú nhanh.");
                return;
            }
            BookingDetailRecord detail = detailRows.get(row);
            txtSelectedDetailNote.setText("Yêu cầu khác: " + safeValue(detail.yeuCauKhac, "-")
                    + "\nGhi chú: " + safeValue(detail.ghiChu, "-"));
        }

        private void openBookingDetailDialog(BookingDetailRecord detail) {
            new BookingDetailEditorDialog(this, detail).setVisible(true);
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
            if (!showConfirmDialog(
                    "Xác nhận xóa dòng chi tiết",
                    "Dòng chi tiết đặt phòng đang chọn sẽ bị xóa khỏi phiếu đặt phòng. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(220, 38, 38)
            )) {
                return;
            }
            detailRows.remove(row);
            refillBookingDetailDialogTable();
        }

        private void submit(String targetStatus) {
            if (txtMaBooking.getText().trim().isEmpty()) {
                showError("Mã đặt phòng không được để trống.");
                return;
            }
            if (txtMaKh.getText().trim().isEmpty() || txtHoTen.getText().trim().isEmpty()) {
                showError("Phiếu đặt phòng phải có thông tin khách hàng.");
                return;
            }
            LocalDate ngayDat = requireDate(txtNgayDatDialog.getText().trim(), "Ngày đặt không hợp lệ.");
            if (ngayDat == null) {
                return;
            }
            if (detailRows.isEmpty()) {
                showError("Phiếu đặt phòng phải có ít nhất 1 dòng chi tiết.");
                return;
            }
            if ("Đã check-in".equals(targetStatus)) {
                if (!showConfirmDialog(
                        "Xác nhận lưu và check-in",
                        "Mỗi dòng chi tiết đặt phòng sẽ sẵn sàng đi tiếp sang lưu trú/check-in. Bạn có muốn tiếp tục không?",
                        "Đồng ý",
                        new Color(245, 158, 11)
                )) {
                    return;
                }
            }

            BookingRecord target = editing
                    ? editingBooking
                    : BookingRecord.createHeader(
                    txtMaBooking.getText().trim(),
                    txtMaKh.getText().trim(),
                    txtHoTen.getText().trim(),
                    txtSdt.getText().trim(),
                    txtCccdDialog.getText().trim(),
                    ngayDat,
                    valueOf(cboNguonBookingDialog.getSelectedItem()),
                    targetStatus,
                    txtGhiChuDialog.getText().trim()
            );

            target.maDatPhong = txtMaBooking.getText().trim();
            target.maKhachHang = txtMaKh.getText().trim();
            target.khachHang = txtHoTen.getText().trim();
            target.soDienThoai = txtSdt.getText().trim();
            target.cccd = txtCccdDialog.getText().trim();
            target.ngayDat = ngayDat;
            target.nguonDat = valueOf(cboNguonBookingDialog.getSelectedItem());
            target.trangThai = targetStatus;
            target.ghiChu = txtGhiChuDialog.getText().trim();
            target.details.clear();
            for (BookingDetailRecord detail : detailRows) {
                BookingDetailRecord copy = detail.copy();
                if ("Đã check-in".equals(targetStatus)
                        && ("Giữ chỗ".equals(copy.trangThaiChiTiet) || "Mới tạo".equals(copy.trangThaiChiTiet))) {
                    copy.trangThaiChiTiet = "Sẵn sàng check-in";
                }
                target.details.add(copy);
            }
            target.syncDerivedData();

            if (editing) {
                refreshBookingViews(target, "Cập nhật booking thành công.");
            } else {
                addBooking(target, "Tạo booking thành công.");
            }
            dispose();
        }

        private final class QuickCustomerDialog extends BaseBookingDialog {
            private QuickCustomerDialog(Dialog owner) {
                super(DatPhongGUI.this, "Khách mới", 520, 380);

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader("KHÁCH MỚI", "Tạo nhanh khách hàng và đưa thông tin trở lại quy trình booking."), BorderLayout.NORTH);

                JPanel form = createDialogFormPanel();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new java.awt.Insets(6, 0, 6, 12);
                gbc.anchor = GridBagConstraints.WEST;

                JTextField txtHoTenMoi = createInputField("");
                JTextField txtSdtMoi = createInputField("");
                JTextField txtCccdMoi = createInputField("");
                JTextField txtEmail = createInputField("");
                JTextField txtDiaChi = createInputField("");
                JTextArea txtGhiChuMoi = createDialogTextArea(3);

                addFormRow(form, gbc, 0, "Họ tên", txtHoTenMoi);
                addFormRow(form, gbc, 1, "SĐT", txtSdtMoi);
                addFormRow(form, gbc, 2, "CCCD/Passport", txtCccdMoi);
                addFormRow(form, gbc, 3, "Email", txtEmail);
                addFormRow(form, gbc, 4, "Địa chỉ", txtDiaChi);
                addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtGhiChuMoi));

                content.add(createDialogCardPanel(), BorderLayout.CENTER);
                ((JPanel) content.getComponent(1)).add(form, BorderLayout.CENTER);

                JButton btnSave = createPrimaryButton("Lưu và chọn", new Color(22, 163, 74), Color.WHITE, e -> {
                    if (txtHoTenMoi.getText().trim().isEmpty()) {
                        showError("Họ tên khách hàng không được trống.");
                        return;
                    }
                    txtMaKh.setText("KH" + (100 + allBookings.size() + 1));
                    txtHoTen.setText(txtHoTenMoi.getText().trim());
                    txtSdt.setText(txtSdtMoi.getText().trim());
                    txtCccdDialog.setText(txtCccdMoi.getText().trim());
                    dispose();
                });
                JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
                content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
            }
        }

        private final class BookingDetailEditorDialog extends BaseBookingDialog {
            private final BookingDetailRecord editingDetail;
            private final JComboBox<String> cboLoaiPhongDialog;
            private final JTextField txtPhongDialog;
        private final AppDatePickerField txtCheckInDialog;
        private final AppDatePickerField txtCheckOutDialog;
            private final JTextField txtSoNguoiDialog;
            private final JComboBox<String> cboBangGiaDialog;
            private final JTextField txtChiTietBangGiaDialog;
            private final JTextField txtGiaApDungDialog;
            private final JTextField txtDatCocDialog;
            private final JComboBox<String> cboTrangThaiChiTietDialog;
            private final JTextArea txtYeuCauKhacDialog;
            private final JTextArea txtGhiChuChiTietDialog;

            private BookingDetailEditorDialog(Dialog owner, BookingDetailRecord detail) {
                super(DatPhongGUI.this, detail == null ? "Thêm chi tiết đặt phòng" : "Cập nhật chi tiết đặt phòng", 700, 620);
                this.editingDetail = detail;

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader(
                        detail == null ? "THÊM CHI TIẾT ĐẶT PHÒNG" : "CẬP NHẬT CHI TIẾT ĐẶT PHÒNG",
                        "Mỗi dòng chi tiết đại diện cho một phòng cụ thể hoặc một nhu cầu phòng trong cùng phiếu đặt phòng."
                ), BorderLayout.NORTH);

                JPanel form = createDialogFormPanel();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new java.awt.Insets(6, 0, 6, 12);
                gbc.anchor = GridBagConstraints.WEST;

                cboLoaiPhongDialog = createComboBox(ROOM_TYPE_OPTIONS);
                txtPhongDialog = createInputField(detail == null ? "" : detail.maPhong);
            txtCheckInDialog = new AppDatePickerField(detail == null ? LocalDate.now().format(DATE_FORMAT) : detail.formatCheckIn(), true);
            txtCheckOutDialog = new AppDatePickerField(detail == null ? LocalDate.now().plusDays(1).format(DATE_FORMAT) : detail.formatCheckOut(), true);
                txtSoNguoiDialog = createInputField(detail == null ? "2" : String.valueOf(detail.soNguoi));
                cboBangGiaDialog = createComboBox(PRICE_TABLE_OPTIONS);
                txtChiTietBangGiaDialog = createInputField(detail == null ? "" : detail.maChiTietBangGia);
                txtGiaApDungDialog = createInputField(detail == null ? "850000" : formatMoney(detail.giaApDung));
                txtDatCocDialog = createInputField(detail == null ? "0" : formatMoney(detail.tienDatCocChiTiet));
                cboTrangThaiChiTietDialog = createComboBox(new String[]{"Mới tạo", "Giữ chỗ", "Đã xác nhận", "Sẵn sàng check-in", "Đã hủy"});
                txtYeuCauKhacDialog = createDialogTextArea(2);
                txtGhiChuChiTietDialog = createDialogTextArea(2);

                if (detail != null) {
                    cboLoaiPhongDialog.setSelectedItem(detail.loaiPhong);
                    cboBangGiaDialog.setSelectedItem(detail.maBangGia);
                    cboTrangThaiChiTietDialog.setSelectedItem(detail.trangThaiChiTiet);
                    txtYeuCauKhacDialog.setText(detail.yeuCauKhac);
                    txtGhiChuChiTietDialog.setText(detail.ghiChu);
                }

                addFormRow(form, gbc, 0, "Loại phòng", cboLoaiPhongDialog);
                addFormRow(form, gbc, 1, "Phòng cụ thể", txtPhongDialog);
                addFormRow(form, gbc, 2, "Check-in dự kiến", txtCheckInDialog);
                addFormRow(form, gbc, 3, "Check-out dự kiến", txtCheckOutDialog);
                addFormRow(form, gbc, 4, "Số người", txtSoNguoiDialog);
                addFormRow(form, gbc, 5, "Bảng giá", cboBangGiaDialog);
                addFormRow(form, gbc, 6, "Chi tiết bảng giá", txtChiTietBangGiaDialog);
                addFormRow(form, gbc, 7, "Giá áp dụng", txtGiaApDungDialog);
                addFormRow(form, gbc, 8, "Tiền cọc chi tiết", txtDatCocDialog);
                addFormRow(form, gbc, 9, "Trạng thái chi tiết", cboTrangThaiChiTietDialog);
                addFormRow(form, gbc, 10, "Yêu cầu khác", new JScrollPane(txtYeuCauKhacDialog));
                addFormRow(form, gbc, 11, "Ghi chú", new JScrollPane(txtGhiChuChiTietDialog));

                JPanel card = createDialogCardPanel();
                card.add(form, BorderLayout.CENTER);
                content.add(card, BorderLayout.CENTER);

                JButton btnPrimary = createPrimaryButton(detail == null ? "Lưu dòng" : "Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit(false));
                JButton btnSaveNext = createOutlineButton("Lưu và thêm tiếp", new Color(22, 163, 74), e -> submit(true));
                btnSaveNext.setVisible(detail == null);
                JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
                content.add(buildDialogButtons(btnCancel, btnSaveNext, btnPrimary), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
            }

            private void submit(boolean keepOpen) {
                String loaiPhong = valueOf(cboLoaiPhongDialog.getSelectedItem());
                if (loaiPhong.isEmpty()) {
                    showError("Loại phòng là bắt buộc.");
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

                BookingDetailRecord target = editingDetail == null
                        ? BookingDetailRecord.create(
                        txtMaBooking.getText().trim() + "-CT" + (detailRows.size() + 1),
                        loaiPhong,
                        txtPhongDialog.getText().trim(),
                        valueOf(cboBangGiaDialog.getSelectedItem()),
                        txtChiTietBangGiaDialog.getText().trim(),
                        checkIn,
                        checkOut,
                        soNguoi,
                        giaApDung,
                        tienCoc,
                        valueOf(cboTrangThaiChiTietDialog.getSelectedItem()),
                        txtYeuCauKhacDialog.getText().trim(),
                        txtGhiChuChiTietDialog.getText().trim()
                )
                        : editingDetail;

                target.loaiPhong = loaiPhong;
                target.maPhong = txtPhongDialog.getText().trim();
                target.maBangGia = valueOf(cboBangGiaDialog.getSelectedItem());
                target.maChiTietBangGia = txtChiTietBangGiaDialog.getText().trim();
                target.checkInDuKien = checkIn;
                target.checkOutDuKien = checkOut;
                target.soNguoi = soNguoi;
                target.giaApDung = giaApDung;
                target.tienDatCocChiTiet = tienCoc;
                target.trangThaiChiTiet = valueOf(cboTrangThaiChiTietDialog.getSelectedItem());
                target.yeuCauKhac = txtYeuCauKhacDialog.getText().trim();
                target.ghiChu = txtGhiChuChiTietDialog.getText().trim();

                if (editingDetail == null) {
                    detailRows.add(target);
                }
                refillBookingDetailDialogTable();

                if (keepOpen && editingDetail == null) {
                    txtPhongDialog.setText("");
            txtCheckInDialog.setText(LocalDate.now().format(DATE_FORMAT));
            txtCheckOutDialog.setText(LocalDate.now().plusDays(1).format(DATE_FORMAT));
                    txtSoNguoiDialog.setText("2");
                    txtChiTietBangGiaDialog.setText("");
                    txtGiaApDungDialog.setText("850000");
                    txtDatCocDialog.setText("0");
                    txtYeuCauKhacDialog.setText("");
                    txtGhiChuChiTietDialog.setText("");
                } else {
                    dispose();
                }
            }
        }
    }

    private final class ConfirmBookingDialog extends BaseBookingDialog {
        private final BookingRecord booking;
        private final JTextArea txtNote;

        private ConfirmBookingDialog(Frame owner, BookingRecord booking) {
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
            addFormRow(form, gbc, 0, "Mã booking", createValueTag(booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Loại phòng", createValueTag(booking.loaiPhong));
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
            booking.trangThai = "Đã xác nhận";
            if (!txtNote.getText().trim().isEmpty()) {
                booking.ghiChu = txtNote.getText().trim();
            }
            refreshBookingViews(booking, "Xác nhận booking thành công.");
            dispose();
        }
    }

    private final class DepositDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private DepositDialog(Frame owner, BookingRecord booking) {
            super(owner, "Nhận cọc", 620, 420);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("NHẬN CỌC BOOKING", "Ghi nhận thêm tiền cọc cho booking đã chọn."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtTongTamTinh = createInputField(formatMoney(booking.tamTinh));
            txtTongTamTinh.setEditable(false);
            JTextField txtDaCoc = createInputField(formatMoney(booking.tienCoc));
            txtDaCoc.setEditable(false);
            JTextField txtThuThem = createInputField("");
            JComboBox<String> cboPhuongThuc = createComboBox(new String[]{"Tiền mặt", "Thẻ", "Chuyển khoản"});
            JTextField txtSoThamChieu = createInputField("");
            JTextArea txtGhiChuMoi = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã booking", createValueTag(booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Tổng tạm tính", txtTongTamTinh);
            addFormRow(form, gbc, 3, "Đã cọc trước", txtDaCoc);
            addFormRow(form, gbc, 4, "Thu thêm", txtThuThem);
            addFormRow(form, gbc, 5, "Phương thức", cboPhuongThuc);
            addFormRow(form, gbc, 6, "Số tham chiếu", txtSoThamChieu);
            addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu phiếu cọc", new Color(22, 163, 74), Color.WHITE, e -> submit(false, txtThuThem, cboPhuongThuc, txtSoThamChieu, txtGhiChuMoi));
            JButton btnSavePrint = createOutlineButton("Lưu và in", new Color(59, 130, 246), e -> submit(true, txtThuThem, cboPhuongThuc, txtSoThamChieu, txtGhiChuMoi));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSavePrint, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean printAfter, JTextField txtThuThem, JComboBox<String> cboPhuongThuc, JTextField txtSoThamChieu, JTextArea txtGhiChuMoi) {
            double thuThem = parseMoney(txtThuThem.getText().trim());
            if (thuThem < 0) {
                showError("Số tiền thu thêm phải hợp lệ.");
                return;
            }
            String phuongThuc = valueOf(cboPhuongThuc.getSelectedItem());
            if (("Thẻ".equals(phuongThuc) || "Chuyển khoản".equals(phuongThuc)) && txtSoThamChieu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập số tham chiếu cho phương thức đã chọn.");
                return;
            }
            booking.tienCoc += thuThem;
            booking.trangThai = booking.tienCoc > 0 ? "Đã cọc" : booking.trangThai;
            if (!txtGhiChuMoi.getText().trim().isEmpty()) {
                booking.ghiChu = txtGhiChuMoi.getText().trim();
            }
            refreshBookingViews(booking, printAfter ? "Nhận cọc thành công và sẵn sàng in phiếu." : "Nhận cọc thành công.");
            dispose();
        }
    }

    private final class CancelBookingDialog extends BaseBookingDialog {
        private final BookingRecord booking;

        private CancelBookingDialog(Frame owner, BookingRecord booking) {
            super(owner, "Hủy booking", 620, 430);
            this.booking = booking;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("HỦY BOOKING", "Booking sẽ chuyển sang trạng thái Đã hủy và giải phóng phòng nếu đang giữ chỗ."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField txtLyDo = createInputField("");
            JComboBox<String> cboXuLyCoc = createComboBox(new String[]{"Không hoàn", "Hoàn một phần", "Hoàn toàn"});
            JTextField txtSoTienHoan = createInputField("0");
            JTextArea txtGhiChuMoi = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã booking", createValueTag(booking.maDatPhong));
            addFormRow(form, gbc, 1, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(booking.trangThai));
            addFormRow(form, gbc, 3, "Check-in dự kiến", createValueTag(booking.formatNgayNhanPhong()));
            addFormRow(form, gbc, 4, "Lý do hủy", txtLyDo);
            addFormRow(form, gbc, 5, "Xử lý tiền cọc", cboXuLyCoc);
            addFormRow(form, gbc, 6, "Số tiền hoàn", txtSoTienHoan);
            addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuMoi));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận hủy", new Color(220, 38, 38), Color.WHITE, e -> submit(txtLyDo, cboXuLyCoc, txtSoTienHoan, txtGhiChuMoi));
            JButton btnCancel = createOutlineButton("Hủy thao tác", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(JTextField txtLyDo, JComboBox<String> cboXuLyCoc, JTextField txtSoTienHoan, JTextArea txtGhiChuMoi) {
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Phải nhập Lý do hủy.");
                return;
            }
            if (booking.tienCoc > 0 && valueOf(cboXuLyCoc.getSelectedItem()).isEmpty()) {
                showError("Vui lòng chọn cách xử lý tiền cọc.");
                return;
            }
            double tienHoan = parseMoney(txtSoTienHoan.getText().trim());
            if (tienHoan < 0) {
                showError("Số tiền hoàn phải hợp lệ.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận hủy booking",
                    "Booking sẽ chuyển sang trạng thái Đã hủy. Nếu có giữ chỗ phòng, hệ thống sẽ giải phóng phòng. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(220, 38, 38)
            )) {
                return;
            }

            booking.trangThai = "Đã hủy";
            booking.soPhong = "";
            booking.tienCoc = Math.max(0, booking.tienCoc - tienHoan);
            booking.ghiChu = txtLyDo.getText().trim() + (txtGhiChuMoi.getText().trim().isEmpty() ? "" : ". " + txtGhiChuMoi.getText().trim());
            refreshBookingViews(booking, "Hủy booking thành công.");
            dispose();
        }
    }

    private final class ViewBookingDialog extends BaseBookingDialog {
        private ViewBookingDialog(Frame owner, BookingRecord booking) {
            super(owner, "Xem chi tiết booking", 720, 520);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("XEM CHI TIẾT BOOKING", "Thông tin booking ở chế độ chỉ đọc."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã booking", createValueTag(booking.maDatPhong));
            addFormRow(form, gbc, 1, "Nguồn booking", createValueTag(booking.nguonDat));
            addFormRow(form, gbc, 2, "Khách hàng", createValueTag(booking.khachHang));
            addFormRow(form, gbc, 3, "SĐT", createValueTag(booking.soDienThoai));
            addFormRow(form, gbc, 4, "CCCD/Passport", createValueTag(booking.cccd));
            addFormRow(form, gbc, 5, "Loại phòng", createValueTag(booking.loaiPhong));
            addFormRow(form, gbc, 6, "Số phòng", createValueTag(booking.soPhong.isEmpty() ? "Chưa gán trước" : booking.soPhong));
            addFormRow(form, gbc, 7, "Check-in dự kiến", createValueTag(booking.formatNgayNhanPhong()));
            addFormRow(form, gbc, 8, "Check-out dự kiến", createValueTag(booking.formatNgayTraPhong()));
            addFormRow(form, gbc, 9, "Số người", createValueTag(String.valueOf(booking.soNguoi)));
            addFormRow(form, gbc, 10, "Trạng thái", createValueTag(booking.trangThai));
            addFormRow(form, gbc, 11, "Bảng giá áp dụng", createValueTag(booking.bangGiaApDung));
            addFormRow(form, gbc, 12, "Tạm tính", createValueTag(formatMoney(booking.tamTinh)));
            addFormRow(form, gbc, 13, "Đã cọc", createValueTag(formatMoney(booking.tienCoc)));
            addFormRow(form, gbc, 14, "Ghi chú", new JScrollPane(createReadonlyViewArea(booking.ghiChu)));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JTextArea createReadonlyViewArea(String text) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(text);
        return area;
    }

    private final class ConfirmDialog extends BaseBookingDialog {
        private boolean confirmed;

        private ConfirmDialog(Frame owner, String title, String message, String confirmText, Color confirmColor) {
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
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class BookingRecord {
        private String maDatPhong;
        private String maKhachHang;
        private String khachHang;
        private String soDienThoai;
        private String cccd;
        private LocalDate ngayDat;
        private String nguonDat;
        private double tongTienDatCoc;
        private String trangThai;
        private String ghiChu;
        private final List<BookingDetailRecord> details = new ArrayList<BookingDetailRecord>();

        // Compatibility fields for legacy dialogs and summaries.
        private String loaiPhong;
        private boolean ganPhongTruoc;
        private String soPhong;
        private LocalDate ngayNhanPhong;
        private LocalDate ngayTraPhong;
        private int soNguoi;
        private String bangGiaApDung;
        private double tamTinh;
        private double tienCoc;
        private String yeuCauKhac;
        private String maChiTietBangGia;

        private static BookingRecord createHeader(String maDatPhong, String maKhachHang, String khachHang, String soDienThoai, String cccd,
                                                  LocalDate ngayDat, String nguonDat, String trangThai, String ghiChu) {
            BookingRecord booking = new BookingRecord();
            booking.maDatPhong = maDatPhong;
            booking.maKhachHang = maKhachHang;
            booking.khachHang = khachHang;
            booking.soDienThoai = soDienThoai;
            booking.cccd = cccd;
            booking.ngayDat = ngayDat;
            booking.nguonDat = nguonDat;
            booking.trangThai = trangThai;
            booking.ghiChu = ghiChu;
            booking.yeuCauKhac = "";
            return booking;
        }

        private static BookingRecord create(String maDatPhong, String maKhachHang, String khachHang, String soDienThoai, String cccd,
                                            String nguonDat, String loaiPhong, boolean ganPhongTruoc, String soPhong,
                                            LocalDate ngayNhanPhong, LocalDate ngayTraPhong, int soNguoi, String bangGiaApDung,
                                            double tamTinh, double tienCoc, String trangThai, String ghiChu) {
            BookingRecord booking = createHeader(maDatPhong, maKhachHang, khachHang, soDienThoai, cccd, LocalDate.now(), nguonDat, trangThai, ghiChu);
            booking.details.add(BookingDetailRecord.create(
                    maDatPhong + "-01",
                    loaiPhong,
                    ganPhongTruoc ? soPhong : "",
                    bangGiaApDung,
                    "",
                    ngayNhanPhong,
                    ngayTraPhong,
                    soNguoi,
                    tamTinh,
                    tienCoc,
                    trangThai,
                    "",
                    ghiChu
            ));
            booking.syncDerivedData();
            return booking;
        }

        private void syncDerivedData() {
            tongTienDatCoc = 0;
            tamTinh = 0;
            soNguoi = 0;

            if (details.isEmpty()) {
                loaiPhong = "";
                soPhong = "";
                ngayNhanPhong = LocalDate.now();
                ngayTraPhong = LocalDate.now();
                bangGiaApDung = "";
                tienCoc = 0;
                yeuCauKhac = "";
                maChiTietBangGia = "";
                return;
            }

            BookingDetailRecord primary = details.get(0);
            loaiPhong = primary.loaiPhong;
            soPhong = primary.maPhong == null ? "" : primary.maPhong;
            ganPhongTruoc = soPhong != null && !soPhong.trim().isEmpty();
            ngayNhanPhong = primary.checkInDuKien;
            ngayTraPhong = primary.checkOutDuKien;
            bangGiaApDung = primary.maBangGia;
            yeuCauKhac = primary.yeuCauKhac;
            maChiTietBangGia = primary.maChiTietBangGia;

            for (BookingDetailRecord detail : details) {
                tongTienDatCoc += detail.tienDatCocChiTiet;
                tamTinh += detail.giaApDung;
                soNguoi += detail.soNguoi;
                if (detail.checkInDuKien.isBefore(ngayNhanPhong)) {
                    ngayNhanPhong = detail.checkInDuKien;
                }
                if (detail.checkOutDuKien.isAfter(ngayTraPhong)) {
                    ngayTraPhong = detail.checkOutDuKien;
                }
            }
            tienCoc = tongTienDatCoc;
        }

        private boolean matchesRoomType(String roomType) {
            for (BookingDetailRecord detail : details) {
                if (roomType.equals(detail.loaiPhong)) {
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

        private String getRoomTypeSummary() {
            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < details.size(); i++) {
                if (i > 0) {
                    summary.append(", ");
                }
                summary.append(details.get(i).loaiPhong);
            }
            return summary.length() == 0 ? "-" : summary.toString();
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
        private String maChiTietDatPhong;
        private String loaiPhong;
        private String maPhong;
        private String maBangGia;
        private String maChiTietBangGia;
        private LocalDate checkInDuKien;
        private LocalDate checkOutDuKien;
        private int soNguoi;
        private double giaApDung;
        private double tienDatCocChiTiet;
        private String trangThaiChiTiet;
        private String yeuCauKhac;
        private String ghiChu;

        private static BookingDetailRecord create(String maChiTietDatPhong, String loaiPhong, String maPhong, String maBangGia,
                                                  String maChiTietBangGia, LocalDate checkInDuKien, LocalDate checkOutDuKien,
                                                  int soNguoi, double giaApDung, double tienDatCocChiTiet,
                                                  String trangThaiChiTiet, String yeuCauKhac, String ghiChu) {
            BookingDetailRecord detail = new BookingDetailRecord();
            detail.maChiTietDatPhong = maChiTietDatPhong;
            detail.loaiPhong = loaiPhong;
            detail.maPhong = maPhong;
            detail.maBangGia = maBangGia;
            detail.maChiTietBangGia = maChiTietBangGia;
            detail.checkInDuKien = checkInDuKien;
            detail.checkOutDuKien = checkOutDuKien;
            detail.soNguoi = soNguoi;
            detail.giaApDung = giaApDung;
            detail.tienDatCocChiTiet = tienDatCocChiTiet;
            detail.trangThaiChiTiet = trangThaiChiTiet;
            detail.yeuCauKhac = yeuCauKhac;
            detail.ghiChu = ghiChu;
            return detail;
        }

        private BookingDetailRecord copy() {
            return create(maChiTietDatPhong, loaiPhong, maPhong, maBangGia, maChiTietBangGia,
                    checkInDuKien, checkOutDuKien, soNguoi, giaApDung, tienDatCocChiTiet, trangThaiChiTiet, yeuCauKhac, ghiChu);
        }

        private String formatCheckIn() {
            return checkInDuKien == null ? "-" : checkInDuKien.format(DATE_FORMAT);
        }

        private String formatCheckOut() {
            return checkOutDuKien == null ? "-" : checkOutDuKien.format(DATE_FORMAT);
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
