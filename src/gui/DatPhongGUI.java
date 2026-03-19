package gui;

import gui.common.AppBranding;
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
    private final List<BookingRecord> allBookings = new ArrayList<BookingRecord>();
    private final List<BookingRecord> filteredBookings = new ArrayList<BookingRecord>();

    private JTable tblDatPhong;
    private DefaultTableModel bookingModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboNguonDat;
    private JComboBox<String> cboLoaiPhong;
    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
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
    private JTextArea txtGhiChu;

    public DatPhongGUI() {
        this("guest", "Lễ tân");
    }

    public DatPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý đặt phòng - Hotel PMS");
        setSize(1360, 820);
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
        txtTuNgay = createInputField("10/03/2026");
        txtDenNgay = createInputField("16/03/2026");
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
                "Số điện thoại",
                "Loại phòng",
                "Check-in dự kiến",
                "Check-out dự kiến",
                "Trạng thái"
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
        wrapper.add(buildHoldStatusCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết đặt phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(10, 2, 10, 8));
        body.setOpaque(false);

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
        addDetailRow(body, "Loại phòng", lblLoaiPhong);
        addDetailRow(body, "Số người", lblSoNguoi);
        addDetailRow(body, "Ngày nhận phòng", lblNgayNhanPhong);
        addDetailRow(body, "Ngày trả phòng", lblNgayTraPhong);
        addDetailRow(body, "Trạng thái", lblTrangThai);
        addDetailRow(body, "Tiền cọc", lblTienCoc);

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

    private JPanel buildHoldStatusCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Tình trạng giữ chỗ theo loại phòng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel rows = new JPanel(new GridLayout(3, 1, 0, 8));
        rows.setOpaque(false);
        rows.add(createHoldRow("Standard", "5 trống / 2 đã đặt", new Color(22, 163, 74)));
        rows.add(createHoldRow("Deluxe", "3 trống / 4 đã đặt", new Color(37, 99, 235)));
        rows.add(createHoldRow("Suite", "1 trống / 1 đã đặt", new Color(245, 158, 11)));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHoldRow(String roomType, String status, Color accent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel lblType = new JLabel(roomType, SwingConstants.CENTER);
        lblType.setOpaque(true);
        lblType.setBackground(new Color(239, 246, 255));
        lblType.setForeground(TEXT_PRIMARY);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblType.setPreferredSize(new Dimension(92, 30));
        lblType.setBorder(BorderFactory.createLineBorder(accent, 1, true));

        JLabel lblStatus = new JLabel(status);
        lblStatus.setFont(BODY_FONT);
        lblStatus.setForeground(TEXT_PRIMARY);

        row.add(lblType, BorderLayout.WEST);
        row.add(lblStatus, BorderLayout.CENTER);
        return row;
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
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl);
        panel.add(value);
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
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
        allBookings.add(BookingRecord.create("DP240301", "KH001", "Nguyễn Minh Anh", "0901234567", "079203001234",
                "Đặt trước", "Standard", false, "", LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 11), 2,
                "BG01 - Standard", 850000, 500000, "Mới tạo", "Khách yêu cầu phòng gần thang máy."));
        allBookings.add(BookingRecord.create("DP240302", "KH002", "Trần Hoài Nam", "0912345678", "079203001235",
                "Đặt trước", "Deluxe", true, "202", LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12), 2,
                "BG02 - Deluxe", 2500000, 1000000, "Đã xác nhận", "Đến sau 20:00, giữ phòng muộn."));
        allBookings.add(BookingRecord.create("DP240303", "KH003", "Lê Thu Hà", "0988555777", "079203001236",
                "Đặt trước", "Suite", true, "502", LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 13), 3,
                "BG03 - Suite", 4200000, 2500000, "Đã cọc", "Cần xuất hóa đơn công ty."));
        allBookings.add(BookingRecord.create("DP240304", "KH004", "Phạm Quốc Bảo", "0977666111", "079203001237",
                "Đặt trước", "Standard", false, "", LocalDate.of(2026, 3, 12), LocalDate.of(2026, 3, 13), 1,
                "BG01 - Standard", 850000, 0, "Mới tạo", "Gọi trước khi check-in 30 phút."));
        allBookings.add(BookingRecord.create("DP240305", "KH005", "Võ Ngọc Linh", "0933222444", "079203001238",
                "Walk-in", "Deluxe", true, "203", LocalDate.of(2026, 3, 13), LocalDate.of(2026, 3, 15), 2,
                "BG02 - Deluxe", 2900000, 800000, "Đã xác nhận", "Ưu tiên tầng cao, không hút thuốc."));
        allBookings.add(BookingRecord.create("DP240306", "KH006", "Đặng Gia Huy", "0966777888", "079203001239",
                "Đặt trước", "Suite", false, "", LocalDate.of(2026, 3, 14), LocalDate.of(2026, 3, 16), 2,
                "BG03 - Suite", 4200000, 0, "Đã hủy", "Khách hủy do thay đổi lịch công tác."));
    }

    private void reloadSampleData(boolean showMessage) {
        cboTrangThai.setSelectedIndex(0);
        cboNguonDat.setSelectedIndex(0);
        cboLoaiPhong.setSelectedIndex(0);
        txtTuNgay.setText("10/03/2026");
        txtDenNgay.setText("16/03/2026");
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

        LocalDate fromDate = parseDate(txtTuNgay.getText().trim());
        LocalDate toDate = parseDate(txtDenNgay.getText().trim());
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
            if (!"Tất cả".equals(loaiPhong) && !booking.loaiPhong.equals(loaiPhong)) {
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
                    booking.soDienThoai,
                    booking.loaiPhong,
                    booking.formatNgayNhanPhong(),
                    booking.formatNgayTraPhong(),
                    booking.trangThai
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
        lblLoaiPhong.setText(booking.loaiPhong);
        lblSoNguoi.setText(String.valueOf(booking.soNguoi));
        lblNgayNhanPhong.setText(booking.formatNgayNhanPhong());
        lblNgayTraPhong.setText(booking.formatNgayTraPhong());
        lblTrangThai.setText(booking.trangThai);
        lblTienCoc.setText(formatMoney(booking.tienCoc));
        txtGhiChu.setText(booking.ghiChu);
        txtGhiChu.setCaretPosition(0);
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
        txtTuNgay.setText("10/03/2026");
        txtDenNgay.setText("16/03/2026");
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
        protected BaseBookingDialog(Frame owner, String title, int width, int height) {
            super(owner, title, true);
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setSize(width, height);
            setLocationRelativeTo(owner);
            getContentPane().setBackground(APP_BG);
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
            setLayout(new BorderLayout());
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
        private JTextField txtMaBooking;
        private JComboBox<String> cboNguonBookingDialog;
        private JTextField txtMaKh;
        private JTextField txtHoTen;
        private JTextField txtSdt;
        private JTextField txtCccdDialog;
        private JComboBox<String> cboLoaiPhongDialog;
        private JComboBox<String> cboGanPhong;
        private JTextField txtSoPhongDialog;
        private JTextField txtCheckInDialog;
        private JTextField txtCheckOutDialog;
        private JTextField txtSoNguoiDialog;
        private JTextArea txtYeuCauKhac;
        private JComboBox<String> cboBangGiaDialog;
        private JTextField txtTamTinhDialog;
        private JTextField txtDatCocDialog;
        private JTextArea txtGhiChuDialog;

        private BookingEditorDialog(Frame owner, BookingRecord booking) {
            super(owner, booking == null ? "Tạo booking" : "Cập nhật booking", 920, 720);
            this.editingBooking = booking;
            this.editing = booking != null;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    editing ? "CẬP NHẬT BOOKING" : "TẠO BOOKING",
                    "Hỗ trợ luồng đặt trước hoặc walk-in, giữ chỗ theo loại phòng hoặc gán trước phòng cụ thể."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.add(buildCustomerSection());
            body.add(Box.createVerticalStrut(10));
            body.add(buildStaySection());
            body.add(Box.createVerticalStrut(10));
            body.add(buildPriceSection());

            content.add(body, BorderLayout.CENTER);

            JButton btnCheckAvailability = createOutlineButton("Kiểm tra phòng trống", new Color(37, 99, 235), e -> openAvailableRoomsDialog());
            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit("Mới tạo"));
            JButton btnSaveConfirm = createOutlineButton("Lưu và xác nhận", new Color(59, 130, 246), e -> submit("Đã xác nhận"));
            JButton btnSaveCheckIn = createOutlineButton("Lưu và check-in", new Color(245, 158, 11), e -> submit("Đã check-in"));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            btnSaveConfirm.setVisible(!editing);
            btnSaveCheckIn.setVisible(!editing);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(btnCheckAvailability, BorderLayout.WEST);
            bottom.add(buildDialogButtons(btnCancel, btnSaveCheckIn, btnSaveConfirm, btnSave), BorderLayout.EAST);
            content.add(bottom, BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private JPanel buildCustomerSection() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);
            JLabel lblSection = new JLabel("THÔNG TIN KHÁCH HÀNG");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMaBooking = createInputField(editing ? editingBooking.maDatPhong : "DP" + (240300 + allBookings.size() + 1));
            txtMaBooking.setEditable(!editing);
            cboNguonBookingDialog = createComboBox(BOOKING_SOURCE_OPTIONS);
            txtMaKh = createInputField(editing ? editingBooking.maKhachHang : "");
            txtHoTen = createInputField(editing ? editingBooking.khachHang : "");
            txtSdt = createInputField(editing ? editingBooking.soDienThoai : "");
            txtCccdDialog = createInputField(editing ? editingBooking.cccd : "");

            if (editing) {
                cboNguonBookingDialog.setSelectedItem(editingBooking.nguonDat);
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
            addFormRow(form, gbc, 1, "Nguồn booking", cboNguonBookingDialog);
            addFormRow(form, gbc, 2, "Mã KH", maKhPanel);
            addFormRow(form, gbc, 3, "Họ tên", txtHoTen);
            addFormRow(form, gbc, 4, "SĐT", txtSdt);
            addFormRow(form, gbc, 5, "CCCD/Passport", txtCccdDialog);

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            card.add(wrapper, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildStaySection() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);
            JLabel lblSection = new JLabel("THÔNG TIN LƯU TRÚ");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboLoaiPhongDialog = createComboBox(ROOM_TYPE_OPTIONS);
            cboGanPhong = createComboBox(new String[]{"Không", "Có"});
            txtSoPhongDialog = createInputField(editing ? editingBooking.soPhong : "");
            txtCheckInDialog = createInputField(editing ? editingBooking.formatNgayNhanPhong() : "20/03/2026");
            txtCheckOutDialog = createInputField(editing ? editingBooking.formatNgayTraPhong() : "21/03/2026");
            txtSoNguoiDialog = createInputField(editing ? String.valueOf(editingBooking.soNguoi) : "2");
            txtYeuCauKhac = createDialogTextArea(3);

            if (editing) {
                cboLoaiPhongDialog.setSelectedItem(editingBooking.loaiPhong);
                cboGanPhong.setSelectedItem(editingBooking.ganPhongTruoc ? "Có" : "Không");
                txtYeuCauKhac.setText(editingBooking.yeuCauKhac);
            }

            if (editing && "Đã check-in".equals(editingBooking.trangThai)) {
                cboLoaiPhongDialog.setEnabled(false);
                txtCheckInDialog.setEditable(false);
            }

            addFormRow(form, gbc, 0, "Loại phòng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 1, "Gán phòng trước", cboGanPhong);
            addFormRow(form, gbc, 2, "Số phòng", txtSoPhongDialog);
            addFormRow(form, gbc, 3, "Check-in dự kiến", txtCheckInDialog);
            addFormRow(form, gbc, 4, "Check-out dự kiến", txtCheckOutDialog);
            addFormRow(form, gbc, 5, "Số người", txtSoNguoiDialog);
            addFormRow(form, gbc, 6, "Yêu cầu khác", new JScrollPane(txtYeuCauKhac));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            card.add(wrapper, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildPriceSection() {
            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);
            JLabel lblSection = new JLabel("THÔNG TIN GIÁ");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboBangGiaDialog = createComboBox(PRICE_TABLE_OPTIONS);
            txtTamTinhDialog = createInputField(editing ? formatMoney(editingBooking.tamTinh) : "850000");
            txtDatCocDialog = createInputField(editing ? formatMoney(editingBooking.tienCoc) : "0");
            txtGhiChuDialog = createDialogTextArea(3);
            if (editing) {
                cboBangGiaDialog.setSelectedItem(editingBooking.bangGiaApDung);
                txtGhiChuDialog.setText(editingBooking.ghiChu);
            }

            addFormRow(form, gbc, 0, "Bảng giá áp dụng", cboBangGiaDialog);
            addFormRow(form, gbc, 1, "Tạm tính", txtTamTinhDialog);
            addFormRow(form, gbc, 2, "Đặt cọc", txtDatCocDialog);
            addFormRow(form, gbc, 3, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            card.add(wrapper, BorderLayout.CENTER);
            return card;
        }

        private void openAvailableRoomsDialog() {
            new AvailableRoomsDialog(this,
                    valueOf(cboLoaiPhongDialog.getSelectedItem()),
                    txtCheckInDialog.getText().trim(),
                    txtCheckOutDialog.getText().trim()).setVisible(true);
        }

        private void submit(String targetStatus) {
            if (txtMaKh.getText().trim().isEmpty() || txtHoTen.getText().trim().isEmpty()) {
                showError("Booking phải có thông tin khách hàng.");
                return;
            }

            LocalDate checkIn = requireDate(txtCheckInDialog.getText().trim(), "Ngày check-in dự kiến không hợp lệ.");
            LocalDate checkOut = requireDate(txtCheckOutDialog.getText().trim(), "Ngày check-out dự kiến không hợp lệ.");
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
                showError("Số người phải hợp lệ.");
                return;
            }

            String loaiPhong = valueOf(cboLoaiPhongDialog.getSelectedItem());
            if (loaiPhong.isEmpty()) {
                showError("Loại phòng là bắt buộc.");
                return;
            }
            boolean ganPhongTruoc = "Có".equals(valueOf(cboGanPhong.getSelectedItem()));
            if (ganPhongTruoc && txtSoPhongDialog.getText().trim().isEmpty()) {
                showError("Nếu chọn gán phòng trước thì phải chọn phòng phù hợp.");
                return;
            }

            double datCoc = parseMoney(txtDatCocDialog.getText().trim());
            double tamTinh = parseMoney(txtTamTinhDialog.getText().trim());
            if (datCoc < 0 || tamTinh < 0) {
                showError("Tạm tính và đặt cọc phải hợp lệ.");
                return;
            }

            if ("Đã check-in".equals(targetStatus)) {
                if (!showConfirmDialog(
                        "Xác nhận lưu và check-in",
                        "Booking mới sẽ được tạo và chuyển sang quy trình check-in. Chỉ tiếp tục nếu có phòng phù hợp và phòng đã sẵn sàng.",
                        "Đồng ý",
                        new Color(245, 158, 11)
                )) {
                    return;
                }
            }

            BookingRecord target = editing ? editingBooking : BookingRecord.create(
                    txtMaBooking.getText().trim(),
                    txtMaKh.getText().trim(),
                    txtHoTen.getText().trim(),
                    txtSdt.getText().trim(),
                    txtCccdDialog.getText().trim(),
                    valueOf(cboNguonBookingDialog.getSelectedItem()),
                    loaiPhong,
                    ganPhongTruoc,
                    txtSoPhongDialog.getText().trim(),
                    checkIn,
                    checkOut,
                    soNguoi,
                    valueOf(cboBangGiaDialog.getSelectedItem()),
                    tamTinh,
                    datCoc,
                    targetStatus,
                    txtGhiChuDialog.getText().trim()
            );

            target.maKhachHang = txtMaKh.getText().trim();
            target.khachHang = txtHoTen.getText().trim();
            target.soDienThoai = txtSdt.getText().trim();
            target.cccd = txtCccdDialog.getText().trim();
            target.nguonDat = valueOf(cboNguonBookingDialog.getSelectedItem());
            target.loaiPhong = loaiPhong;
            target.ganPhongTruoc = ganPhongTruoc;
            target.soPhong = txtSoPhongDialog.getText().trim();
            target.ngayNhanPhong = checkIn;
            target.ngayTraPhong = checkOut;
            target.soNguoi = soNguoi;
            target.bangGiaApDung = valueOf(cboBangGiaDialog.getSelectedItem());
            target.tamTinh = tamTinh;
            target.tienCoc = datCoc;
            target.trangThai = targetStatus;
            target.ghiChu = txtGhiChuDialog.getText().trim();
            target.yeuCauKhac = txtYeuCauKhac.getText().trim();

            if (editing) {
                refreshBookingViews(target, "Cập nhật booking thành công.");
            } else {
                addBooking(target, "Tạo booking thành công.");
            }
            dispose();
        }

        private final class AvailableRoomsDialog extends BaseBookingDialog {
            private final String loaiPhong;
            private final String checkIn;
            private final String checkOut;
            private final JTable tblRooms;
            private final DefaultTableModel roomModel;

            private AvailableRoomsDialog(Dialog owner, String loaiPhong, String checkIn, String checkOut) {
                super(DatPhongGUI.this, "Kiểm tra phòng trống", 700, 420);
                this.loaiPhong = loaiPhong;
                this.checkIn = checkIn;
                this.checkOut = checkOut;

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader(
                        "KIỂM TRA PHÒNG TRỐNG",
                        "Kết quả demo theo loại phòng " + loaiPhong + " từ " + checkIn + " đến " + checkOut + "."
                ), BorderLayout.NORTH);

                String[] columns = {"Số phòng", "Tầng", "Trạng thái", "Giá tham khảo", "Ghi chú"};
                roomModel = new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                tblRooms = new JTable(roomModel);
                tblRooms.setRowHeight(30);
                fillAvailableRooms();

                content.add(new JScrollPane(tblRooms), BorderLayout.CENTER);
                JButton btnSelect = createPrimaryButton("Chọn phòng", new Color(37, 99, 235), Color.WHITE, e -> chooseRoom());
                JButton btnClose = createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose());
                content.add(buildDialogButtons(btnClose, btnSelect), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
            }

            private void fillAvailableRooms() {
                roomModel.setRowCount(0);
                if ("Deluxe".equals(loaiPhong)) {
                    roomModel.addRow(new Object[]{"202", "Tầng 2", "Trống", "1.250.000", "Gần thang máy"});
                    roomModel.addRow(new Object[]{"203", "Tầng 2", "Trống", "1.300.000", "Có ban công"});
                } else if ("Suite".equals(loaiPhong)) {
                    roomModel.addRow(new Object[]{"501", "Tầng 5", "Trống", "2.100.000", "Hướng thành phố"});
                } else {
                    roomModel.addRow(new Object[]{"101", "Tầng 1", "Trống", "850.000", "Gần lễ tân"});
                    roomModel.addRow(new Object[]{"104", "Tầng 1", "Trống", "850.000", "Yên tĩnh"});
                }
            }

            private void chooseRoom() {
                int row = tblRooms.getSelectedRow();
                if (row < 0) {
                    showWarning("Vui lòng chọn một phòng trong danh sách.");
                    return;
                }
                cboGanPhong.setSelectedItem("Có");
                txtSoPhongDialog.setText(valueOf(tblRooms.getValueAt(row, 0)));
                dispose();
            }
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
        private String loaiPhong;
        private boolean ganPhongTruoc;
        private String soPhong;
        private LocalDate ngayNhanPhong;
        private LocalDate ngayTraPhong;
        private String trangThai;
        private String nguonDat;
        private int soNguoi;
        private String bangGiaApDung;
        private double tamTinh;
        private double tienCoc;
        private String yeuCauKhac;
        private String ghiChu;

        private static BookingRecord create(String maDatPhong, String maKhachHang, String khachHang, String soDienThoai, String cccd,
                                            String nguonDat, String loaiPhong, boolean ganPhongTruoc, String soPhong,
                                            LocalDate ngayNhanPhong, LocalDate ngayTraPhong, int soNguoi, String bangGiaApDung,
                                            double tamTinh, double tienCoc, String trangThai, String ghiChu) {
            BookingRecord booking = new BookingRecord();
            booking.maDatPhong = maDatPhong;
            booking.maKhachHang = maKhachHang;
            booking.khachHang = khachHang;
            booking.soDienThoai = soDienThoai;
            booking.cccd = cccd;
            booking.nguonDat = nguonDat;
            booking.loaiPhong = loaiPhong;
            booking.ganPhongTruoc = ganPhongTruoc;
            booking.soPhong = soPhong;
            booking.ngayNhanPhong = ngayNhanPhong;
            booking.ngayTraPhong = ngayTraPhong;
            booking.soNguoi = soNguoi;
            booking.bangGiaApDung = bangGiaApDung;
            booking.tamTinh = tamTinh;
            booking.tienCoc = tienCoc;
            booking.trangThai = trangThai;
            booking.ghiChu = ghiChu;
            booking.yeuCauKhac = "";
            return booking;
        }

        private String formatNgayNhanPhong() {
            return ngayNhanPhong.format(DATE_FORMAT);
        }

        private String formatNgayTraPhong() {
            return ngayTraPhong.format(DATE_FORMAT);
        }
    }
}
