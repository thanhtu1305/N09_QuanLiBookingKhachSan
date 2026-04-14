package gui;

import dao.DatPhongDAO;
import db.ConnectDB;
import entity.DatPhongConflictInfo;
import gui.common.AppBranding;
import gui.common.AppDatePickerField;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
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
import java.time.LocalDate;
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

    private static final List<DatPhongGUI> OPEN_INSTANCES = new ArrayList<DatPhongGUI>();
    private static Integer pendingFocusedBookingId;

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
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Tạo booking", new Color(22, 163, 74), Color.WHITE, e -> openCreateBookingDialog()));
        card.add(createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> openConfirmBookingDialog()));
        card.add(createPrimaryButton("Hủy booking", new Color(220, 38, 38), Color.WHITE, e -> openCancelBookingDialog()));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"T\u1ea5t c\u1ea3", "\u0110\u00e3 \u0111\u1eb7t", "\u0110\u00e3 x\u00e1c nh\u1eadn", "\u0110\u00e3 c\u1ecdc", "Ch\u1edd check-in"});
        cboNguonDat = createComboBox(new String[]{"Tất cả", "Đặt trước", "Walk-in"});
        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite", "Family"});
        txtTuNgay = new AppDatePickerField("", false);
        txtDenNgay = new AppDatePickerField("", false);
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
        ScreenUIHelper.styleTableHeader(tblDatPhong);

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
                "F3 Xác nhận",
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

    private void reloadSampleData(boolean showMessage) {
        loadBookingsFromDatabase();
        cboTrangThai.setSelectedIndex(0);
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
                if (DatPhongDAO.isBookingStageStatus(booking.trangThai)) {
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
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maPhong AS maPhongId, ctdp.soNguoi, ctdp.giaPhong, ctdp.thanhTien, " +
                "ISNULL(p.soPhong, N'Chưa gán') AS soPhong, " +
                "dp.ngayNhanPhong, dp.ngayTraPhong, dp.trangThai, " +
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
                    detail.checkInDuKien = toLocalDate(rs.getDate("ngayNhanPhong"));
                    detail.checkOutDuKien = toLocalDate(rs.getDate("ngayTraPhong"));
                    detail.soNguoi = rs.getInt("soNguoi");
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
        for (entity.ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
            booking.details.add(toBookingDetailRecord(detail));
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
        record.checkInDuKien = detail.getCheckInDuKien();
        record.checkOutDuKien = detail.getCheckOutDuKien();
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
        }
    }

    private void updateDetailPanel(BookingRecord booking) {
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
        if (!canConfirmBooking(booking.trangThai)) {
            showWarning("Chỉ booking chưa check-in mới có thể xác nhận.");
            return;
        }
        new ConfirmBookingDialog(this, booking).setVisible(true);
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
        ScreenUIHelper.registerShortcut(this, "F3", "datphong-f3", this::openConfirmBookingDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "datphong-f5", this::openCancelBookingDialog);
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
                    CustomerLookup lookup = new CustomerLookup();
                    lookup.maKhachHang = rs.getInt("maKhachHang");
                    lookup.hoTen = safeValue(rs.getString("hoTen"), "");
                    lookup.soDienThoai = safeValue(rs.getString("soDienThoai"), "");
                    lookup.cccdPassport = safeValue(rs.getString("cccdPassport"), "");
                    lookup.ngaySinh = rs.getDate("ngaySinh") == null ? null : rs.getDate("ngaySinh").toLocalDate();
                    lookup.email = safeValue(rs.getString("email"), "");
                    lookup.diaChi = safeValue(rs.getString("diaChi"), "");
                    return lookup;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
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
                    CustomerLookup lookup = new CustomerLookup();
                    lookup.maKhachHang = rs.getInt("maKhachHang");
                    lookup.hoTen = safeValue(rs.getString("hoTen"), "");
                    lookup.soDienThoai = safeValue(rs.getString("soDienThoai"), "");
                    lookup.cccdPassport = safeValue(rs.getString("cccdPassport"), value);
                    lookup.ngaySinh = rs.getDate("ngaySinh") == null ? null : rs.getDate("ngaySinh").toLocalDate();
                    lookup.email = safeValue(rs.getString("email"), "");
                    lookup.diaChi = safeValue(rs.getString("diaChi"), "");
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
                detail.checkInDuKien,
                detail.checkOutDuKien
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


    private List<RoomOption> loadRoomOptions(LocalDate checkIn, LocalDate checkOut, Integer includeRoomId, Integer excludeBookingId,
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
                "WHERE p.trangThai = N'Hoạt động' OR p.maPhong = ? " +
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
            RoomOption option = findRoomOptionById(options, room.getMaPhong());
            if (option == null) {
                option = new RoomOption();
                option.maPhongId = room.getMaPhong();
                option.soPhong = safeValue(room.getSoPhong(), String.valueOf(option.maPhongId));
                option.tang = safeValue(room.getTang(), "-");
                option.trangThai = safeValue(room.getTrangThai(), "Hoạt động");
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

    public static synchronized void prepareFocusOnBooking(int maDatPhong) {
        pendingFocusedBookingId = maDatPhong > 0 ? Integer.valueOf(maDatPhong) : null;
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
        private JTextField txtCccdDialog;
        private JTextField txtHoTen;
        private JTextField txtSdt;
        private AppDatePickerField txtNgaySinhKhach;
        private JTextField txtEmailKhach;
        private JTextArea txtDiaChiKhach;
        private JTextField txtTongDatCocDialog;
        private JTextArea txtGhiChuDialog;
        private JTable tblBookingDetailDialog;
        private DefaultTableModel bookingDetailDialogModel;
        private JLabel lblDetailSummary;
        private JScrollPane bookingDetailScrollPane;
        private JPanel pnlConflictWarning;
        private JLabel lblConflictWarning;
        private final List<JButton> submitButtons = new ArrayList<JButton>();
        private Border defaultDetailTableBorder;
        private BookingDetailRecord highlightedConflictRow;
        private BookingEditorDialog(Window owner, BookingRecord booking) {
            super(owner, booking == null ? "T?o booking" : "C?p nh?t booking", 1340, 820);
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
                    editing
                            ? "Booking đã lưu trong SQL. Chỉ được cập nhật khi chưa check-in."
                            : "Booking bắt buộc phải có ít nhất một dòng chi tiết phòng. Có thể chọn nhiều phòng khác loại trong cùng booking."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildHeaderSection(), buildDetailSection());
            splitPane.setBorder(null);
            splitPane.setResizeWeight(0.35d);
            splitPane.setDividerLocation(430);
            splitPane.setContinuousLayout(true);
            body.add(splitPane, BorderLayout.CENTER);
            content.add(body, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            if (editing) {
                JButton btnUpdate = createPrimaryButton("Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit("update"));
                submitButtons.add(btnUpdate);
                content.add(buildDialogButtons(btnCancel, btnUpdate), BorderLayout.SOUTH);
            } else {
                JButton btnSaveConfirm = createOutlineButton("Lưu và xác nhận", new Color(59, 130, 246), e -> submit("confirm"));
                JButton btnSaveCheckIn = createPrimaryButton("Lưu và check-in", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit("checkin"));
                submitButtons.add(btnSaveConfirm);
                submitButtons.add(btnSaveCheckIn);
                content.add(buildDialogButtons(btnCancel, btnSaveConfirm, btnSaveCheckIn), BorderLayout.SOUTH);
            }
            add(content, BorderLayout.CENTER);
            reevaluateDetailValidationState(false);
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

            CustomerLookup existingCustomer = editing ? loadCustomerLookup(editingBooking.maKhachHang) : null;

            txtNgayDatDialog = new AppDatePickerField(editing && editingBooking.ngayDat != null ? editingBooking.formatNgayDat() : LocalDate.now().format(DATE_FORMAT), true);
            cboNguonBookingDialog = createComboBox(new String[]{"Đặt trước", "Walk-in"});
            txtCccdDialog = createInputField(editing ? editingBooking.cccd : "");
            txtHoTen = createInputField(editing ? safeValue(existingCustomer == null ? editingBooking.khachHang : existingCustomer.hoTen, "") : "");
            txtSdt = createInputField(editing ? safeValue(existingCustomer == null ? editingBooking.soDienThoai : existingCustomer.soDienThoai, "") : "");
            LocalDate customerBirthDate = editing ? (existingCustomer == null ? null : existingCustomer.ngaySinh) : null;
            txtNgaySinhKhach = new AppDatePickerField(customerBirthDate == null ? "" : customerBirthDate.format(DATE_FORMAT), false);
            txtNgayDatDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
            txtNgaySinhKhach.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
            txtEmailKhach = createInputField(editing && existingCustomer != null ? existingCustomer.email : "");
            txtDiaChiKhach = createDialogTextArea(3);
            if (editing && existingCustomer != null) {
                txtDiaChiKhach.setText(existingCustomer.diaChi);
            }
            txtTongDatCocDialog = createInputField(editing ? formatMoney(editingBooking.tongTienDatCoc) : "0");
            txtTongDatCocDialog.setEditable(false);
            txtGhiChuDialog = createDialogTextArea(3);

            if (editing) {
                cboNguonBookingDialog.setSelectedItem(editingBooking.nguonDat);
                txtGhiChuDialog.setText(editingBooking.ghiChu);
            }

            txtCccdDialog.addActionListener(e -> lookupCustomerByPassportOrCccd());

            addFormRow(form, gbc, 0, "Ngày đặt", txtNgayDatDialog);
            addFormRow(form, gbc, 1, "Nguồn booking", cboNguonBookingDialog);
            addFormRow(form, gbc, 2, "CCCD/Passport", txtCccdDialog);
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
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);
            JLabel lblSection = new JLabel("DETAIL - CÁC DÒNG CHI TIẾT ĐẶT PHÒNG");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            String[] columns = {"STT", "Phòng", "Loại phòng", "Check-in", "Check-out", "Số người", "Loại ngày", "Loại giá", "Giá áp dụng", "Tiền cọc", "Trạng thái"};
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
            tblBookingDetailDialog.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            applyBookingDetailColumnWidths();
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

            bookingDetailScrollPane = new JScrollPane(tblBookingDetailDialog);
            bookingDetailScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            bookingDetailScrollPane.setPreferredSize(new Dimension(0, 360));
            defaultDetailTableBorder = bookingDetailScrollPane.getBorder();

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.setOpaque(false);
            actions.add(createPrimaryButton("Thêm dòng phòng", new Color(59, 130, 246), Color.WHITE, e -> openBookingDetailDialog(null)));
            actions.add(createOutlineButton("Sửa dòng", new Color(245, 158, 11), e -> editSelectedDetailRow()));
            actions.add(createOutlineButton("Xóa dòng", new Color(220, 38, 38), e -> removeSelectedDetailRow()));

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

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(actions, BorderLayout.NORTH);
            center.add(bookingDetailScrollPane, BorderLayout.CENTER);
            center.add(lblDetailSummary, BorderLayout.SOUTH);

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(center, BorderLayout.CENTER);
            wrapper.add(pnlConflictWarning, BorderLayout.SOUTH);
            card.add(wrapper, BorderLayout.CENTER);
            refillBookingDetailDialogTable();
            return card;
        }


        private void applyBookingDetailColumnWidths() {
            if (tblBookingDetailDialog == null || tblBookingDetailDialog.getColumnModel().getColumnCount() < 11) {
                return;
            }
            int[] widths = {55, 85, 140, 105, 105, 90, 105, 105, 145, 120, 120};
            for (int i = 0; i < widths.length; i++) {
                tblBookingDetailDialog.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }

        private void refillBookingDetailDialogTable() {
            bookingDetailDialogModel.setRowCount(0);
            for (int i = 0; i < detailRows.size(); i++) {
                BookingDetailRecord detail = detailRows.get(i);
                bookingDetailDialogModel.addRow(new Object[]{
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
                        getDetailStatusDisplay(detail)
                });
            }
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
            txtTongDatCocDialog.setText(formatMoney(totalDeposit));
            lblDetailSummary.setText("Tổng số dòng: " + detailRows.size() + " | Tổng tiền cọc: " + formatMoney(totalDeposit));
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
            if (detailRows.isEmpty()) {
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
                    + ", tu " + formatDate(conflictInfo.getNgayNhanPhong())
                    + " den " + formatDate(conflictInfo.getNgayTraPhong())
                    + " (" + safeValue(conflictInfo.getTrangThai(), "-") + ").";
        }

        private String validateDetailRowsBeforeSave() {
            if (detailRows.isEmpty()) {
                reevaluateDetailValidationState(true);
                return "Phiếu đặt phòng phải có ít nhất 1 phòng.";
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
            detailRows.remove(row);
            refillBookingDetailDialogTable();
        }

        private void submit(String mode) {
            if (txtHoTen.getText().trim().isEmpty() || txtSdt.getText().trim().isEmpty()) {
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

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien) VALUES (?, ?, ?, ?, ?)")) {
                    for (BookingDetailRecord detail : detailRows) {
                        refreshResolvedRate(con, detail);
                        ps.setInt(1, maDatPhong);
                        ps.setInt(2, detail.maPhongId);
                        ps.setInt(3, detail.soNguoi);
                        ps.setDouble(4, detail.giaApDung);
                        ps.setDouble(5, detail.computeThanhTien());
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

        private LocalDate findMinCheckIn(List<BookingDetailRecord> details) {
            LocalDate min = details.get(0).checkInDuKien;
            for (BookingDetailRecord detail : details) {
                if (detail.checkInDuKien.isBefore(min)) {
                    min = detail.checkInDuKien;
                }
            }
            return min;
        }

        private LocalDate findMaxCheckOut(List<BookingDetailRecord> details) {
            LocalDate max = details.get(0).checkOutDuKien;
            for (BookingDetailRecord detail : details) {
                if (detail.checkOutDuKien.isAfter(max)) {
                    max = detail.checkOutDuKien;
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
            private final AppDatePickerField txtCheckOutDialog;
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
            private final Border defaultCheckOutBorder;
            private final Color defaultRoomBackground;
            private final Color defaultCheckInBackground;
            private final Color defaultCheckOutBackground;

            private BookingDetailEditorDialog(Dialog owner, BookingDetailRecord detail) {
                super(owner, detail == null ? "Thêm dòng chi tiết" : "Cập nhật dòng chi tiết", 700, 520);
                this.editingDetail = detail;

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
                txtCheckInDialog = new AppDatePickerField(detail == null ? LocalDate.now().format(DATE_FORMAT) : detail.formatCheckIn(), true);
                txtCheckOutDialog = new AppDatePickerField(detail == null ? LocalDate.now().plusDays(1).format(DATE_FORMAT) : detail.formatCheckOut(), true);
                txtCheckInDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 3/3/26");
                txtCheckOutDialog.setToolTipText("Nhập ngày dạng dd/MM/yyyy, ví dụ: 4/3/26");
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
                defaultCheckOutBorder = txtCheckOutDialog.getBorder();
                defaultRoomBackground = cboPhongDialog.getBackground();
                defaultCheckInBackground = resolveDateFieldBackground(txtCheckInDialog);
                defaultCheckOutBackground = resolveDateFieldBackground(txtCheckOutDialog);
                if (detail != null) {
                    txtGhiChuChiTietDialog.setText(detail.ghiChu);
                }
                cboPhongDialog.addActionListener(e -> syncSelectedRoomInfo());
                cboPhongDialog.addActionListener(e -> refreshSelectedRatePreview());
                cboPhongDialog.addActionListener(e -> reevaluateCurrentSelectionState());
                installDateFieldChangeListener(txtCheckInDialog, this::reloadAvailableRooms);
                installDateFieldChangeListener(txtCheckOutDialog, this::reloadAvailableRooms);
                installDateFieldChangeListener(txtCheckInDialog, this::refreshSelectedRatePreview);
                installDateFieldChangeListener(txtCheckInDialog, this::reevaluateCurrentSelectionState);
                installDateFieldChangeListener(txtCheckOutDialog, this::refreshSelectedRatePreview);
                installDateFieldChangeListener(txtCheckOutDialog, this::reevaluateCurrentSelectionState);

                addFormRow(form, gbc, 0, "Phòng", cboPhongDialog);
                addFormRow(form, gbc, 1, "Loại phòng", txtLoaiPhongDialog);
                addFormRow(form, gbc, 2, "Check-in dự kiến", txtCheckInDialog);
                addFormRow(form, gbc, 3, "Check-out dự kiến", txtCheckOutDialog);
                addFormRow(form, gbc, 4, "Số người", txtSoNguoiDialog);
                addFormRow(form, gbc, 5, "Thu tiền cọc", txtDatCocDialog);
                addFormRow(form, gbc, 6, "Giá áp dụng", lblRatePreview);
                addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuChiTietDialog));

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

            private void reloadAvailableRooms() {
                Integer preferredRoomId = resolvePreferredRoomId();
                LocalDate checkIn = parseDate(txtCheckInDialog.getText());
                LocalDate checkOut = parseDate(txtCheckOutDialog.getText());
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
                LocalDate checkIn = parseDate(txtCheckInDialog.getText());
                LocalDate checkOut = parseDate(txtCheckOutDialog.getText());
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
                LocalDate checkIn = parseDate(txtCheckInDialog.getText());
                LocalDate checkOut = parseDate(txtCheckOutDialog.getText());
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

            private void reevaluateCurrentSelectionState() {
                RoomOption option = (RoomOption) cboPhongDialog.getSelectedItem();
                LocalDate checkIn = parseDate(txtCheckInDialog.getText());
                LocalDate checkOut = parseDate(txtCheckOutDialog.getText());
                int soNguoi = parsePositiveIntOrZero(txtSoNguoiDialog.getText().trim());

                String warningMessage = null;
                boolean hasIssue = false;
                if (checkIn != null && checkOut != null && !checkOut.isAfter(checkIn)) {
                    warningMessage = "Ngày trả phòng phải lớn hơn ngày nhận phòng.";
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
                    warningMessage = "Ngày trả phòng phải lớn hơn ngày nhận phòng.";
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
                txtCheckOutDialog.setBorder(hasIssue ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultCheckOutBorder);
                setDateFieldBackground(txtCheckInDialog, hasIssue ? CONFLICT_BG : defaultCheckInBackground);
                setDateFieldBackground(txtCheckOutDialog, hasIssue ? CONFLICT_BG : defaultCheckOutBackground);
            }

            private Color resolveDateFieldBackground(AppDatePickerField field) {
                JTextField editor = findNestedTextField(field);
                return editor == null ? Color.WHITE : editor.getBackground();
            }

            private void setDateFieldBackground(AppDatePickerField field, Color color) {
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
                LocalDate checkIn = normalizeDateFieldValue(txtCheckInDialog, "Check-in dự kiến không hợp lệ.");
                LocalDate checkOut = normalizeDateFieldValue(txtCheckOutDialog, "Check-out dự kiến không hợp lệ.");
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

    private static final class CustomerLookup {
        private int maKhachHang;
        private String hoTen;
        private String soDienThoai;
        private String cccdPassport;
        private LocalDate ngaySinh;
        private String email;
        private String diaChi;
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
                if (ngayNhanPhong == null || detail.checkInDuKien.isBefore(ngayNhanPhong)) {
                    ngayNhanPhong = detail.checkInDuKien;
                }
                if (ngayTraPhong == null || detail.checkOutDuKien.isAfter(ngayTraPhong)) {
                    ngayTraPhong = detail.checkOutDuKien;
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
        private LocalDate checkInDuKien;
        private LocalDate checkOutDuKien;
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
            long soDem = java.time.temporal.ChronoUnit.DAYS.between(checkInDuKien, checkOutDuKien);
            if (soDem <= 0) {
                soDem = 1;
            }
            return soDem * giaApDung;
        }

        private String formatCheckIn() {
            return checkInDuKien == null ? "-" : checkInDuKien.format(DATE_FORMAT);
        }

        private String formatCheckOut() {
            return checkOutDuKien == null ? "-" : checkOutDuKien.format(DATE_FORMAT);
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) initUI();
        return rootPanel;
    }
}
