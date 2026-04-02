package gui;

import dao.ThanhToanDAO;
import dao.ThanhToanDAO.PaymentPart;
import entity.ThanhToan;
import entity.ThanhToan.ChiTietDong;
import entity.ThanhToan.GiaoDichThanhToan;
import gui.common.AppBranding;
import gui.common.AppDatePickerField;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class ThanhToanGUI extends JFrame {
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final List<ThanhToanGUI> OPEN_INSTANCES = new ArrayList<ThanhToanGUI>();

    private final String username;
    private final String role;
    private final ThanhToanDAO thanhToanDAO = new ThanhToanDAO();
    private JPanel rootPanel;

    private final List<ThanhToan> allInvoices = new ArrayList<ThanhToan>();
    private final List<ThanhToan> filteredInvoices = new ArrayList<ThanhToan>();

    private JTable tblHoaDon;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboPhuongThuc;
    private AppDatePickerField txtTuNgay;
    private AppDatePickerField txtDenNgay;
    private JTextField txtTuKhoa;

    private JLabel lblMaHoaDon;
    private JLabel lblMaHoSo;
    private JLabel lblKhachHang;
    private JLabel lblSoPhong;
    private JLabel lblTienPhong;
    private JLabel lblTienDichVu;
    private JLabel lblPhuThu;
    private JLabel lblGiamGia;
    private JLabel lblTienCoc;
    private JLabel lblTongPhaiThu;
    private JLabel lblPhuongThucThanhToan;
    private JLabel lblTrangThai;

    private JLabel lblTongTienPhong;
    private JLabel lblTongDichVu;
    private JLabel lblTongGiamGia;
    private JLabel lblTongDatCoc;
    private JLabel lblConPhaiThu;
    private JLabel lblSoDongChiTiet;
    private JLabel lblNgayLapHoaDon;
    private JTextArea txtGhiChu;

    public ThanhToanGUI() {
        this("guest", "Lễ tân");
    }

    public ThanhToanGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Thanh toán - " + AppBranding.APP_DISPLAY_NAME);
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
                    OPEN_INSTANCES.remove(ThanhToanGUI.this);
                }
            }
        });
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(SidebarFactory.createSidebar(this, ScreenKey.THANH_TOAN, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("THANH TOÁN"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Liên kết trực tiếp với Lưu trú, Dịch vụ, Đặt phòng và bảng ThanhToan / HoaDon trong SQL.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Thanh toán"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Thanh toán", new Color(22, 163, 74), Color.WHITE, e -> openPaymentDialog()));
        card.add(createPrimaryButton("In hóa đơn", new Color(37, 99, 235), Color.WHITE, e -> openInvoicePreviewDialog()));
        card.add(createPrimaryButton("Áp giảm giá", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDiscountDialog()));
        card.add(createPrimaryButton("Hoàn cọc", new Color(220, 38, 38), Color.WHITE, e -> openDepositRefundDialog()));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Chờ thanh toán", "Đã thanh toán", "Đã hoàn cọc"});
        cboPhuongThuc = createComboBox(new String[]{"Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ", "Kết hợp"});
        txtTuNgay = new AppDatePickerField("", false);
        txtDenNgay = new AppDatePickerField("", false);
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(290, 34));

        left.add(createFieldGroup("Trạng thái hóa đơn", cboTrangThai));
        left.add(createFieldGroup("Phương thức thanh toán", cboPhuongThuc));
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
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildRightColumn());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG, BORDER_SOFT, TEXT_MUTED,
                "F1 Thanh toán", "F2 In hóa đơn", "F3 Áp giảm giá", "F4 Hoàn cọc", "Enter Xem chi tiết"
        );
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách hóa đơn");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Hóa đơn lấy từ Lưu trú và dịch vụ phát sinh.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {"Mã hóa đơn", "Khách hàng", "Số phòng / số dòng", "Tổng tiền", "Trạng thái", "Ngày lập"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHoaDon = new JTable(tableModel);
        tblHoaDon.setFont(BODY_FONT);
        tblHoaDon.setRowHeight(32);
        tblHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHoaDon.setGridColor(BORDER_SOFT);
        tblHoaDon.setShowGrid(true);
        tblHoaDon.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblHoaDon);

        tblHoaDon.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblHoaDon.getSelectedRow();
                if (row >= 0 && row < filteredInvoices.size()) {
                    updateDetailPanel(filteredInvoices.get(row));
                }
            }
        });
        tblHoaDon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblHoaDon.getSelectedRow() >= 0) {
                    openInvoiceDetailDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblHoaDon);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

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

        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);
        bottom.add(buildSummaryCard());
        bottom.add(buildMethodCard());

        wrapper.add(bottom, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết hóa đơn");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblMaHoaDon = createValueLabel();
        lblMaHoSo = createValueLabel();
        lblKhachHang = createValueLabel();
        lblSoPhong = createValueLabel();
        lblTienPhong = createValueLabel();
        lblTienDichVu = createValueLabel();
        lblPhuThu = createValueLabel();
        lblGiamGia = createValueLabel();
        lblTienCoc = createValueLabel();
        lblTongPhaiThu = createValueLabel();
        lblPhuongThucThanhToan = createValueLabel();
        lblTrangThai = createValueLabel();

        addDetailRow(body, "Mã hóa đơn", lblMaHoaDon);
        addDetailRow(body, "Mã hồ sơ", lblMaHoSo);
        addDetailRow(body, "Khách hàng", lblKhachHang);
        addDetailRow(body, "Số phòng", lblSoPhong);
        addDetailRow(body, "Tiền phòng", lblTienPhong);
        addDetailRow(body, "Tiền dịch vụ", lblTienDichVu);
        addDetailRow(body, "Phụ thu", lblPhuThu);
        addDetailRow(body, "Giảm giá", lblGiamGia);
        addDetailRow(body, "Trừ đặt cọc", lblTienCoc);
        addDetailRow(body, "Tổng phải thu", lblTongPhaiThu);
        addDetailRow(body, "Phương thức", lblPhuongThucThanhToan);
        addDetailRow(body, "Trạng thái", lblTrangThai);

        txtGhiChu = new JTextArea(4, 20);
        txtGhiChu.setEditable(false);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(BODY_FONT);
        txtGhiChu.setForeground(TEXT_PRIMARY);
        txtGhiChu.setBackground(PANEL_SOFT);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel note = new JPanel(new BorderLayout(0, 6));
        note.setOpaque(false);
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);
        note.add(lblNote, BorderLayout.NORTH);
        note.add(new JScrollPane(txtGhiChu), BorderLayout.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildSummaryCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Tóm tắt thu tiền");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblTongTienPhong = createValueLabel();
        lblTongDichVu = createValueLabel();
        lblTongGiamGia = createValueLabel();
        lblTongDatCoc = createValueLabel();
        lblConPhaiThu = createValueLabel();
        lblSoDongChiTiet = createValueLabel();
        lblNgayLapHoaDon = createValueLabel();

        addDetailRow(body, "Tổng tiền phòng", lblTongTienPhong);
        addDetailRow(body, "Tổng dịch vụ", lblTongDichVu);
        addDetailRow(body, "Giảm giá", lblTongGiamGia);
        addDetailRow(body, "Đặt cọc", lblTongDatCoc);
        addDetailRow(body, "Còn phải thu", lblConPhaiThu);
        addDetailRow(body, "Số dòng chi tiết", lblSoDongChiTiet);
        addDetailRow(body, "Ngày lập", lblNgayLapHoaDon);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMethodCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Phương thức thanh toán");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel methods = new JPanel(new GridLayout(4, 1, 0, 8));
        methods.setOpaque(false);
        methods.add(createMethodBadge("Tiền mặt", new Color(220, 252, 231)));
        methods.add(createMethodBadge("Chuyển khoản", new Color(219, 234, 254)));
        methods.add(createMethodBadge("Thẻ", new Color(254, 249, 195)));
        methods.add(createMethodBadge("Kết hợp", new Color(243, 232, 255)));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(methods, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMethodBadge(String text, Color background) {
        JPanel badge = new JPanel(new BorderLayout());
        badge.setBackground(background);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        badge.add(lbl, BorderLayout.CENTER);
        return badge;
    }

    private void reloadData(boolean showMessage) {
        allInvoices.clear();
        allInvoices.addAll(thanhToanDAO.getAll());
        cboTrangThai.setSelectedIndex(0);
        cboPhuongThuc.setSelectedIndex(0);
        txtTuNgay.setText("");
        txtDenNgay.setText("");
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu thanh toán.");
        }
        if (allInvoices.isEmpty()) {
            String err = thanhToanDAO.getLastErrorMessage();
            if (err != null && !err.trim().isEmpty()) {
                showWarning(err);
            }
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredInvoices.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String phuongThuc = valueOf(cboPhuongThuc.getSelectedItem());
        LocalDate fromDate = parseDate(txtTuNgay.getText().trim());
        LocalDate toDate = parseDate(txtDenNgay.getText().trim());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

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

        for (ThanhToan invoice : allInvoices) {
            LocalDate ngayLap = invoice.getNgayLap() == null ? null : invoice.getNgayLap().toLocalDateTime().toLocalDate();
            if (!"Tất cả".equals(trangThai) && !invoice.getTrangThai().equalsIgnoreCase(trangThai)) {
                continue;
            }
            String currentMethod = safeValue(invoice.getPhuongThuc(), "-");
            if (!"Tất cả".equals(phuongThuc) && !currentMethod.equalsIgnoreCase(phuongThuc)) {
                continue;
            }
            if (fromDate != null && ngayLap != null && ngayLap.isBefore(fromDate)) {
                continue;
            }
            if (toDate != null && ngayLap != null && ngayLap.isAfter(toDate)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (invoice.getMaHoaDon() + " " + invoice.getSoPhong() + " " + invoice.getKhachHang() + " " + invoice.getSoDienThoai()).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredInvoices.add(invoice);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredInvoices.size() + " hóa đơn phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (ThanhToan invoice : filteredInvoices) {
            tableModel.addRow(new Object[]{
                    "HD" + invoice.getMaHoaDon(),
                    safeValue(invoice.getKhachHang(), "-"),
                    formatRoomAndCount(invoice),
                    ThanhToan.formatMoney(invoice.getTongPhaiThu()),
                    safeValue(invoice.getTrangThai(), "-"),
                    formatDateTime(invoice.getNgayLap())
            });
        }

        if (!filteredInvoices.isEmpty()) {
            tblHoaDon.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredInvoices.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private String formatRoomAndCount(ThanhToan invoice) {
        if (invoice == null) {
            return "-";
        }
        String soPhong = safeValue(invoice.getSoPhong(), "-");
        if ("-".equals(soPhong)) {
            return soPhong;
        }
        String[] parts = soPhong.split(",");
        int roomCount = 0;
        java.util.LinkedHashSet<String> uniqueRooms = new java.util.LinkedHashSet<String>();
        for (String part : parts) {
            String room = safeValue(part, "");
            if (!room.isEmpty()) {
                uniqueRooms.add(room);
            }
        }
        roomCount = uniqueRooms.size();
        if (roomCount <= 0) {
            roomCount = 1;
        }
        return soPhong + " / " + roomCount + " phòng";
    }


    private void updateDetailPanel(ThanhToan invoice) {
        lblMaHoaDon.setText("HD" + invoice.getMaHoaDon());
        lblMaHoSo.setText(safeValue(invoice.getMaHoSo(), "-"));
        lblKhachHang.setText(safeValue(invoice.getKhachHang(), "-"));
        lblSoPhong.setText(safeValue(invoice.getSoPhong(), "-"));
        lblTienPhong.setText(ThanhToan.formatMoney(invoice.getTienPhong()));
        lblTienDichVu.setText(ThanhToan.formatMoney(invoice.getTienDichVu()));
        lblPhuThu.setText(ThanhToan.formatMoney(invoice.getPhuThu()));
        lblGiamGia.setText(ThanhToan.formatMoney(invoice.getGiamGia()));
        lblTienCoc.setText(ThanhToan.formatMoney(invoice.getTienCocTru()));
        lblTongPhaiThu.setText(ThanhToan.formatMoney(invoice.getTongPhaiThu()));
        lblPhuongThucThanhToan.setText(isBlank(invoice.getPhuongThuc()) ? "-" : invoice.getPhuongThuc());
        lblTrangThai.setText(safeValue(invoice.getTrangThai(), "-"));

        lblTongTienPhong.setText(ThanhToan.formatMoney(invoice.getTienPhong()));
        lblTongDichVu.setText(ThanhToan.formatMoney(invoice.getTienDichVu()));
        lblTongGiamGia.setText(ThanhToan.formatMoney(invoice.getGiamGia()));
        lblTongDatCoc.setText(ThanhToan.formatMoney(invoice.getTienCoc()));
        lblConPhaiThu.setText(ThanhToan.formatMoney(invoice.getConPhaiThu()));
        lblSoDongChiTiet.setText(String.valueOf(invoice.getChiTiet().size()));
        lblNgayLapHoaDon.setText(formatDateTime(invoice.getNgayLap()));

        StringBuilder note = new StringBuilder();
        if (!isBlank(invoice.getGhiChu())) {
            note.append(invoice.getGhiChu()).append("\n");
        }
        if (!isBlank(invoice.getThongTinThanhToanKetHop())) {
            note.append(invoice.getThongTinThanhToanKetHop()).append("\n");
        }
        if (!invoice.getGiaoDichThanhToans().isEmpty()) {
            note.append("Giao dịch:\n");
            for (GiaoDichThanhToan gd : invoice.getGiaoDichThanhToans()) {
                note.append("- ")
                        .append(safeValue(gd.getLoaiGiaoDich(), "THANH_TOAN"))
                        .append(" | ")
                        .append(safeValue(gd.getPhuongThuc(), "-"))
                        .append(" | ")
                        .append(ThanhToan.formatMoney(gd.getSoTien()));
                if (!isBlank(gd.getSoThamChieu())) {
                    note.append(" | Ref: ").append(gd.getSoThamChieu());
                }
                note.append("\n");
            }
        }
        txtGhiChu.setText(note.length() == 0 ? "-" : note.toString().trim());
        txtGhiChu.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblMaHoaDon.setText("-");
        lblMaHoSo.setText("-");
        lblKhachHang.setText("-");
        lblSoPhong.setText("-");
        lblTienPhong.setText("-");
        lblTienDichVu.setText("-");
        lblPhuThu.setText("-");
        lblGiamGia.setText("-");
        lblTienCoc.setText("-");
        lblTongPhaiThu.setText("-");
        lblPhuongThucThanhToan.setText("-");
        lblTrangThai.setText("-");
        lblTongTienPhong.setText("-");
        lblTongDichVu.setText("-");
        lblTongGiamGia.setText("-");
        lblTongDatCoc.setText("-");
        lblConPhaiThu.setText("-");
        lblSoDongChiTiet.setText("-");
        lblNgayLapHoaDon.setText("-");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
    }

    private ThanhToan getSelectedInvoice() {
        int row = tblHoaDon.getSelectedRow();
        if (row < 0 || row >= filteredInvoices.size()) {
            showWarning("Vui lòng chọn một hóa đơn trong danh sách.");
            return null;
        }
        return filteredInvoices.get(row);
    }

    private void openPaymentDialog() {
        ThanhToan invoice = getSelectedInvoice();
        if (invoice != null) {
            if ("Đã thanh toán".equalsIgnoreCase(invoice.getTrangThai()) || invoice.getConPhaiThu() <= 0.1d) {
                showWarning("Hóa đơn này đã thanh toán. Bạn chỉ có thể in hóa đơn.");
                return;
            }
            new PaymentDialog(this, invoice).setVisible(true);
        }
    }

    private void openDiscountDialog() {
        ThanhToan invoice = getSelectedInvoice();
        if (invoice != null) {
            new DiscountDialog(this, invoice).setVisible(true);
        }
    }

    private void openDepositRefundDialog() {
        ThanhToan invoice = getSelectedInvoice();
        if (invoice != null) {
            new DepositRefundDialog(this, invoice).setVisible(true);
        }
    }

    private void openInvoicePreviewDialog() {
        ThanhToan invoice = getSelectedInvoice();
        if (invoice != null) {
            new ModernInvoicePreviewDialog(this, invoice).setVisible(true);
        }
    }

    private void openInvoiceDetailDialog() {
        ThanhToan invoice = getSelectedInvoice();
        if (invoice != null) {
            new InvoiceDetailDialog(this, invoice).setVisible(true);
        }
    }

    private void refreshInvoiceViews(ThanhToan invoice, String message) {
        reloadData(false);
        selectInvoice(invoice == null ? null : invoice.getMaHoaDon());
        refreshLinkedViews();
        showSuccess(message);
    }

    private void selectInvoice(String maHoaDon) {
        if (maHoaDon == null) {
            return;
        }
        for (int i = 0; i < filteredInvoices.size(); i++) {
            if (maHoaDon.equals(filteredInvoices.get(i).getMaHoaDon())) {
                tblHoaDon.setRowSelectionInterval(i, i);
                updateDetailPanel(filteredInvoices.get(i));
                return;
            }
        }
        if (!filteredInvoices.isEmpty()) {
            tblHoaDon.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredInvoices.get(0));
        }
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "thanhtoan-f1", this::openPaymentDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "thanhtoan-f2", this::openInvoicePreviewDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "thanhtoan-f3", this::openDiscountDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "thanhtoan-f4", this::openDepositRefundDialog);
        ScreenUIHelper.registerShortcut(this, "ENTER", "thanhtoan-enter", this::openInvoiceDetailDialog);
    }

    public static void refreshAllOpenInstances() {
        List<ThanhToanGUI> snapshot;
        synchronized (OPEN_INSTANCES) {
            snapshot = new ArrayList<ThanhToanGUI>(OPEN_INSTANCES);
        }
        for (ThanhToanGUI gui : snapshot) {
            javax.swing.SwingUtilities.invokeLater(() -> gui.reloadData(false));
        }
    }

    private void refreshLinkedViews() {
        invokeStaticRefresh("gui.CheckInOutGUI");
        invokeStaticRefresh("gui.DatPhongGUI");
        invokeStaticRefresh("gui.KhachHangGUI");
        invokeStaticRefresh("gui.PhongGUI");
    }

    private void invokeStaticRefresh(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Method m = clazz.getMethod("refreshAllOpenInstances");
            m.invoke(null);
        } catch (Throwable ignored) {
        }
    }

    private abstract class BasePaymentDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BasePaymentDialog(Frame owner, String title, int width, int height) {
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

    private final class PaymentDialog extends BasePaymentDialog {
        private final ThanhToan invoice;
        private JTextField txtKhachDua;
        private JTextField txtTienThua;
        private JComboBox<String> cboPhuongThucDialog;
        private JTextField txtSoThamChieu;
        private JTextField txtNguoiThu;
        private JTextArea txtGhiChuDialog;

        private PaymentDialog(Frame owner, ThanhToan invoice) {
            super(owner, "Thanh toán hóa đơn", 980, 660);
            this.invoice = invoice;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("THANH TOÁN HÓA ĐƠN", "Liên kết trực tiếp bảng ThanhToan và cập nhật trạng thái hóa đơn."), BorderLayout.NORTH);

            JPanel leftColumn = new JPanel();
            leftColumn.setOpaque(false);
            leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
            leftColumn.add(buildInvoiceInfoSection());
            leftColumn.add(Box.createVerticalStrut(10));
            leftColumn.add(buildAmountSection());
            leftColumn.add(Box.createVerticalStrut(10));
            leftColumn.add(buildPaymentSection());

            JPanel rightColumn = new JPanel(new BorderLayout());
            rightColumn.setOpaque(false);
            rightColumn.add(buildInvoiceLineSection("CHI TIẾT HÓA ĐƠN", invoice), BorderLayout.CENTER);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, rightColumn);
            splitPane.setBorder(null);
            splitPane.setOpaque(false);
            splitPane.setResizeWeight(0.38d);
            splitPane.setDividerLocation(360);
            splitPane.setContinuousLayout(true);

            content.add(splitPane, BorderLayout.CENTER);

            JButton btnPay = createPrimaryButton("Xác nhận thu tiền", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnPayPrint = createOutlineButton("Xác nhận và in hóa đơn", new Color(37, 99, 235), e -> submit(true));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnPayPrint, btnPay), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private JPanel buildInvoiceInfoSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel("HD" + invoice.getMaHoaDon()));
            addFormRow(form, gbc, 1, "Hồ sơ lưu trú", createValueLabel(safeValue(invoice.getMaHoSo(), "-")));
            addFormRow(form, gbc, 2, "Khách / Phòng", createValueLabel(safeValue(invoice.getKhachHang(), "-") + " / " + safeValue(invoice.getSoPhong(), "-")));
            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildAmountSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(form, gbc, 0, "Tiền phòng", createValueLabel(ThanhToan.formatMoney(invoice.getTienPhong())));
            addFormRow(form, gbc, 1, "Tiền dịch vụ", createValueLabel(ThanhToan.formatMoney(invoice.getTienDichVu())));
            addFormRow(form, gbc, 2, "Phụ thu", createValueLabel(ThanhToan.formatMoney(invoice.getPhuThu())));
            addFormRow(form, gbc, 3, "Giảm giá", createValueLabel(ThanhToan.formatMoney(invoice.getGiamGia())));
            addFormRow(form, gbc, 4, "Trừ đặt cọc", createValueLabel(ThanhToan.formatMoney(invoice.getTienCocTru())));
            addFormRow(form, gbc, 5, "Còn phải thu", createValueLabel(ThanhToan.formatMoney(invoice.getConPhaiThu())));
            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildPaymentSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtKhachDua = createInputField(ThanhToan.formatMoney(invoice.getConPhaiThu()));
            txtTienThua = createInputField("0");
            txtTienThua.setEditable(false);
            cboPhuongThucDialog = createComboBox(new String[]{"Tiền mặt", "Thẻ", "Chuyển khoản", "Kết hợp"});
            txtSoThamChieu = createInputField("");
            txtNguoiThu = createInputField(findEmployeeName());
            txtNguoiThu.setEditable(false);
            txtGhiChuDialog = createDialogTextArea(3);

            txtKhachDua.getDocument().addDocumentListener(new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { updateTienThua(); }
                @Override public void removeUpdate(DocumentEvent e) { updateTienThua(); }
                @Override public void changedUpdate(DocumentEvent e) { updateTienThua(); }
            });
            cboPhuongThucDialog.addActionListener(e -> updatePaymentFields());
            updatePaymentFields();

            addFormRow(form, gbc, 0, "Khách đưa", txtKhachDua);
            addFormRow(form, gbc, 1, "Tiền thừa", txtTienThua);
            addFormRow(form, gbc, 2, "Phương thức", cboPhuongThucDialog);
            addFormRow(form, gbc, 3, "Người thu", txtNguoiThu);
            addFormRow(form, gbc, 4, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private void updateTienThua() {
            String method = valueOf(cboPhuongThucDialog.getSelectedItem());
            double tongPhaiThu = invoice.getConPhaiThu();
            double khachDua = parseMoney(txtKhachDua.getText().trim());
            if (khachDua < 0 || !"Tiền mặt".equals(method)) {
                txtTienThua.setText("0");
                return;
            }
            txtTienThua.setText(ThanhToan.formatMoney(Math.max(0d, khachDua - tongPhaiThu)));
        }

        private void updatePaymentFields() {
            String method = valueOf(cboPhuongThucDialog.getSelectedItem());
            boolean cash = "Tiền mặt".equals(method);
            txtKhachDua.setEditable(cash);
            if (!cash) {
                txtKhachDua.setText(ThanhToan.formatMoney(invoice.getConPhaiThu()));
            }
            txtSoThamChieu.setText("");
            txtSoThamChieu.setEditable(false);
            updateTienThua();
        }

        private void submit(boolean printAfter) {
            if ("Đã thanh toán".equalsIgnoreCase(invoice.getTrangThai())) {
                showError("Hóa đơn này đã thanh toán xong.");
                return;
            }
            if ("Đã hoàn cọc".equalsIgnoreCase(invoice.getTrangThai())) {
                showError("Hóa đơn này đã hoàn cọc.");
                return;
            }
            if (txtNguoiThu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập người thu.");
                return;
            }

            String method = valueOf(cboPhuongThucDialog.getSelectedItem());
            if (invoice.getConPhaiThu() <= 0.1d) {
                if (!showConfirmDialog("Xác nhận hoàn tất hóa đơn",
                        "Hóa đơn hiện không còn số tiền phải thu. Hệ thống sẽ chuyển trạng thái sang Đã thanh toán.",
                        "Đồng ý", new Color(22, 163, 74))) {
                    return;
                }
                int maNhanVien = findEmployeeId();
                boolean ok = thanhToanDAO.markInvoiceAsPaid(invoice.getMaHoaDon(), maNhanVien, txtGhiChuDialog.getText().trim());
                if (!ok) {
                    showError("Không thể cập nhật trạng thái hóa đơn: " + safeValue(thanhToanDAO.getLastErrorMessage(), "Không xác định."));
                    return;
                }
                refreshInvoiceViews(invoice, printAfter ? "Hóa đơn đã hoàn tất và sẵn sàng in." : "Hóa đơn đã hoàn tất.");
                if (printAfter) {
                    ThanhToan refreshed = thanhToanDAO.findById(invoice.getMaHoaDon());
                    if (refreshed != null) {
                        new InvoicePreviewDialog(ThanhToanGUI.this, refreshed).setVisible(true);
                    }
                }
                dispose();
                return;
            }
            if ("Kết hợp".equals(method)) {
                new SplitPaymentDialog(ThanhToanGUI.this, invoice, txtNguoiThu.getText().trim(), txtGhiChuDialog.getText().trim(), printAfter).setVisible(true);
                dispose();
                return;
            }

            double khachDua = parseMoney(txtKhachDua.getText().trim());
            if (khachDua < 0d) {
                showError("Khách đưa phải là số hợp lệ.");
                return;
            }
            if ("Tiền mặt".equals(method) && khachDua + 0.1d < invoice.getConPhaiThu()) {
                showError("Nếu phương thức là Tiền mặt thì Khách đưa phải >= Còn phải thu.");
                return;
            }
            if (!showConfirmDialog("Xác nhận thanh toán",
                    "Hệ thống sẽ ghi giao dịch vào bảng ThanhToan và cập nhật trạng thái hóa đơn.",
                    "Đồng ý", new Color(22, 163, 74))) {
                return;
            }

            PaymentPart part = new PaymentPart();
            part.setPhuongThuc(method);
            part.setSoTien(invoice.getConPhaiThu());
            part.setSoThamChieu("");

            int maNhanVien = findEmployeeId();
            boolean ok = thanhToanDAO.recordPayment(invoice.getMaHoaDon(), maNhanVien, java.util.Collections.singletonList(part), txtGhiChuDialog.getText().trim());
            if (!ok) {
                showError("Không thể thanh toán: " + safeValue(thanhToanDAO.getLastErrorMessage(), "Không xác định."));
                return;
            }

            refreshInvoiceViews(invoice, printAfter ? "Thanh toán thành công và sẵn sàng in hóa đơn." : "Thanh toán thành công.");
            if (printAfter) {
                ThanhToan refreshed = thanhToanDAO.findById(invoice.getMaHoaDon());
                if (refreshed != null) {
                    new InvoicePreviewDialog(ThanhToanGUI.this, refreshed).setVisible(true);
                }
            }
            dispose();
        }
    }

    private final class SplitPaymentDialog extends BasePaymentDialog {
        private final ThanhToan invoice;
        private final String ghiChu;
        private final boolean printAfter;
        private JTextField txtTienMat;
        private JTextField txtThe;
        private JTextField txtChuyenKhoan;
        private JTextField txtTongNhan;
        private JTextField txtRefThe;
        private JTextField txtRefChuyenKhoan;
        private JTextField txtNguoiThu;
        private JTextArea txtGhiChuMix;

        private SplitPaymentDialog(Frame owner, ThanhToan invoice, String nguoiThu, String ghiChu, boolean printAfter) {
            super(owner, "Thanh toán kết hợp", 620, 500);
            this.invoice = invoice;
            this.ghiChu = ghiChu;
            this.printAfter = printAfter;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("THANH TOÁN KẾT HỢP", "Phân bổ số tiền theo nhiều phương thức và lưu nhiều dòng vào bảng ThanhToan."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTienMat = createInputField("0");
            txtThe = createInputField("0");
            txtChuyenKhoan = createInputField("0");
            txtTongNhan = createInputField("0");
            txtTongNhan.setEditable(false);
            txtRefThe = createInputField("");
            txtRefChuyenKhoan = createInputField("");
            txtNguoiThu = createInputField(nguoiThu);
            txtNguoiThu.setEditable(false);
            txtGhiChuMix = createDialogTextArea(3);
            txtGhiChuMix.setText(ghiChu);

            DocumentListener listener = new DocumentListener() {
                @Override public void insertUpdate(DocumentEvent e) { updateTongNhan(); }
                @Override public void removeUpdate(DocumentEvent e) { updateTongNhan(); }
                @Override public void changedUpdate(DocumentEvent e) { updateTongNhan(); }
            };
            txtTienMat.getDocument().addDocumentListener(listener);
            txtThe.getDocument().addDocumentListener(listener);
            txtChuyenKhoan.getDocument().addDocumentListener(listener);

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel("HD" + invoice.getMaHoaDon()));
            addFormRow(form, gbc, 1, "Còn phải thu", createValueLabel(ThanhToan.formatMoney(invoice.getConPhaiThu())));
            addFormRow(form, gbc, 2, "Tiền mặt", txtTienMat);
            addFormRow(form, gbc, 3, "Thẻ", txtThe);
            addFormRow(form, gbc, 4, "Ref thẻ", txtRefThe);
            addFormRow(form, gbc, 5, "Chuyển khoản", txtChuyenKhoan);
            addFormRow(form, gbc, 6, "Ref chuyển khoản", txtRefChuyenKhoan);
            addFormRow(form, gbc, 7, "Tổng nhận", txtTongNhan);
            addFormRow(form, gbc, 8, "Người thu", txtNguoiThu);
            addFormRow(form, gbc, 9, "Ghi chú", new JScrollPane(txtGhiChuMix));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnCheck = createOutlineButton("Kiểm tra tổng", new Color(59, 130, 246), e -> updateTongNhan());
            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(22, 163, 74), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(btnCheck, BorderLayout.WEST);
            bottom.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.EAST);
            content.add(bottom, BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void updateTongNhan() {
            double total = Math.max(0d, parseMoney(txtTienMat.getText().trim()))
                    + Math.max(0d, parseMoney(txtThe.getText().trim()))
                    + Math.max(0d, parseMoney(txtChuyenKhoan.getText().trim()));
            txtTongNhan.setText(ThanhToan.formatMoney(total));
        }

        private void submit() {
            double cash = parseMoney(txtTienMat.getText().trim());
            double card = parseMoney(txtThe.getText().trim());
            double transfer = parseMoney(txtChuyenKhoan.getText().trim());
            if (cash < 0 || card < 0 || transfer < 0) {
                showError("Từng khoản thanh toán phải >= 0.");
                return;
            }

            double total = cash + card + transfer;
            if (Math.abs(total - invoice.getConPhaiThu()) > 0.1d) {
                showError("Tổng các khoản phải đúng bằng Còn phải thu.");
                return;
            }
            if (card > 0d && txtRefThe.getText().trim().isEmpty()) {
                showError("Vui lòng nhập Ref thẻ.");
                return;
            }
            if (transfer > 0d && txtRefChuyenKhoan.getText().trim().isEmpty()) {
                showError("Vui lòng nhập Ref chuyển khoản.");
                return;
            }

            List<PaymentPart> parts = new ArrayList<PaymentPart>();
            if (cash > 0d) {
                PaymentPart p = new PaymentPart();
                p.setPhuongThuc("Tiền mặt");
                p.setSoTien(cash);
                p.setSoThamChieu("");
                parts.add(p);
            }
            if (card > 0d) {
                PaymentPart p = new PaymentPart();
                p.setPhuongThuc("Thẻ");
                p.setSoTien(card);
                p.setSoThamChieu(txtRefThe.getText().trim());
                parts.add(p);
            }
            if (transfer > 0d) {
                PaymentPart p = new PaymentPart();
                p.setPhuongThuc("Chuyển khoản");
                p.setSoTien(transfer);
                p.setSoThamChieu(txtRefChuyenKhoan.getText().trim());
                parts.add(p);
            }

            int maNhanVien = findEmployeeId();
            boolean ok = thanhToanDAO.recordPayment(invoice.getMaHoaDon(), maNhanVien, parts, txtGhiChuMix.getText().trim());
            if (!ok) {
                showError("Không thể thanh toán kết hợp: " + safeValue(thanhToanDAO.getLastErrorMessage(), "Không xác định."));
                return;
            }

            refreshInvoiceViews(invoice, printAfter ? "Thanh toán kết hợp thành công và sẵn sàng in hóa đơn." : "Thanh toán kết hợp thành công.");
            if (printAfter) {
                ThanhToan refreshed = thanhToanDAO.findById(invoice.getMaHoaDon());
                if (refreshed != null) {
                    new InvoicePreviewDialog(ThanhToanGUI.this, refreshed).setVisible(true);
                }
            }
            dispose();
        }
    }

    private final class DiscountDialog extends BasePaymentDialog {
        private final ThanhToan invoice;
        private JTextField txtGiaTri;
        private JComboBox<String> cboHinhThuc;
        private JTextField txtLyDo;
        private JTextField txtNguoiDuyet;
        private JTextArea txtGhiChu;

        private DiscountDialog(Frame owner, ThanhToan invoice) {
            super(owner, "Áp dụng giảm giá", 580, 420);
            this.invoice = invoice;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("ÁP DỤNG GIẢM GIÁ", "Điều chỉnh giảm giá trước khi hóa đơn được thanh toán."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtGiaTri = createInputField("0");
            cboHinhThuc = createComboBox(new String[]{"Tiền mặt", "Phần trăm"});
            txtLyDo = createInputField("");
            txtNguoiDuyet = createInputField(username);
            txtGhiChu = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel("HD" + invoice.getMaHoaDon()));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(safeValue(invoice.getKhachHang(), "-") + " / " + safeValue(invoice.getSoPhong(), "-")));
            addFormRow(form, gbc, 2, "Tổng hiện tại", createValueLabel(ThanhToan.formatMoney(invoice.getTongPhaiThu())));
            addFormRow(form, gbc, 3, "Giảm giá", txtGiaTri);
            addFormRow(form, gbc, 4, "Hình thức", cboHinhThuc);
            addFormRow(form, gbc, 5, "Lý do", txtLyDo);
            addFormRow(form, gbc, 6, "Người duyệt", txtNguoiDuyet);
            addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChu));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if ("Đã thanh toán".equalsIgnoreCase(invoice.getTrangThai()) || "Đã hoàn cọc".equalsIgnoreCase(invoice.getTrangThai())) {
                showError("Chỉ áp dụng giảm giá khi hóa đơn chưa thanh toán xong.");
                return;
            }
            double value = parseMoney(txtGiaTri.getText().trim());
            if (value < 0d) {
                showError("Giảm giá không được âm.");
                return;
            }
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do giảm giá.");
                return;
            }
            boolean percentage = "Phần trăm".equals(valueOf(cboHinhThuc.getSelectedItem()));
            if (percentage && value > 100d) {
                showError("Giảm giá phần trăm không được vượt 100.");
                return;
            }

            boolean ok = thanhToanDAO.applyDiscount(
                    invoice.getMaHoaDon(),
                    value,
                    percentage,
                    txtLyDo.getText().trim(),
                    txtNguoiDuyet.getText().trim(),
                    txtGhiChu.getText().trim()
            );
            if (!ok) {
                showError("Không thể áp giảm giá: " + safeValue(thanhToanDAO.getLastErrorMessage(), "Không xác định."));
                return;
            }

            refreshInvoiceViews(invoice, "Áp dụng giảm giá thành công.");
            dispose();
        }
    }

    private final class DepositRefundDialog extends BasePaymentDialog {
        private final ThanhToan invoice;
        private JTextField txtSoTienHoan;
        private JComboBox<String> cboHinhThuc;
        private JTextField txtSoThamChieu;
        private JTextArea txtLyDo;

        private DepositRefundDialog(Frame owner, ThanhToan invoice) {
            super(owner, "Hoàn cọc", 580, 420);
            this.invoice = invoice;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("HOÀN CỌC", "Ghi nhận giao dịch hoàn cọc vào bảng ThanhToan."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtSoTienHoan = createInputField("0");
            cboHinhThuc = createComboBox(new String[]{"Tiền mặt", "Chuyển khoản"});
            txtSoThamChieu = createInputField("");
            txtLyDo = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel("HD" + invoice.getMaHoaDon()));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(safeValue(invoice.getKhachHang(), "-") + " / " + safeValue(invoice.getSoPhong(), "-")));
            addFormRow(form, gbc, 2, "Tiền cọc gốc", createValueLabel(ThanhToan.formatMoney(invoice.getTienCoc())));
            addFormRow(form, gbc, 3, "Đã trừ vào hóa đơn", createValueLabel(ThanhToan.formatMoney(invoice.getTienCocTru())));
            addFormRow(form, gbc, 4, "Có thể hoàn", createValueLabel(ThanhToan.formatMoney(invoice.getSoTienCoTheHoanCoc())));
            addFormRow(form, gbc, 5, "Số tiền cần hoàn", txtSoTienHoan);
            addFormRow(form, gbc, 6, "Hình thức hoàn", cboHinhThuc);
            addFormRow(form, gbc, 7, "Số tham chiếu", txtSoThamChieu);
            addFormRow(form, gbc, 8, "Lý do", new JScrollPane(txtLyDo));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xác nhận hoàn", new Color(220, 38, 38), Color.WHITE, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            double amount = parseMoney(txtSoTienHoan.getText().trim());
            if (amount <= 0d) {
                showError("Số tiền hoàn phải lớn hơn 0.");
                return;
            }
            if (amount - invoice.getSoTienCoTheHoanCoc() > 0.1d) {
                showError("Số tiền hoàn vượt quá phần cọc còn có thể hoàn.");
                return;
            }
            if ("Chuyển khoản".equals(valueOf(cboHinhThuc.getSelectedItem())) && txtSoThamChieu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập số tham chiếu khi hoàn bằng chuyển khoản.");
                return;
            }
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do hoàn cọc.");
                return;
            }

            int maNhanVien = findEmployeeId();
            boolean ok = thanhToanDAO.refundDeposit(
                    invoice.getMaHoaDon(),
                    maNhanVien,
                    amount,
                    valueOf(cboHinhThuc.getSelectedItem()),
                    txtSoThamChieu.getText().trim(),
                    txtLyDo.getText().trim()
            );
            if (!ok) {
                showError("Không thể hoàn cọc: " + safeValue(thanhToanDAO.getLastErrorMessage(), "Không xác định."));
                return;
            }

            refreshInvoiceViews(invoice, "Hoàn cọc thành công.");
            dispose();
        }
    }

    private final class InvoicePreviewDialog extends BasePaymentDialog {
        private InvoicePreviewDialog(Frame owner, ThanhToan invoice) {
            super(owner, "Xem trước hóa đơn", 760, 600);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("XEM TRƯỚC HÓA ĐƠN", "Xem trước nội dung hóa đơn trước khi in."), BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel("HD" + invoice.getMaHoaDon()));
            addFormRow(form, gbc, 1, "Hồ sơ lưu trú", createValueLabel(safeValue(invoice.getMaHoSo(), "-")));
            addFormRow(form, gbc, 2, "Khách / Phòng", createValueLabel(safeValue(invoice.getKhachHang(), "-") + " / " + safeValue(invoice.getSoPhong(), "-")));
            addFormRow(form, gbc, 3, "Ngày giờ lập", createValueLabel(formatDateTime(invoice.getNgayLap())));
            addFormRow(form, gbc, 4, "Ngày thanh toán", createValueLabel(formatDateTime(invoice.getNgayThanhToan())));
            addFormRow(form, gbc, 5, "Nhân viên thu", createValueLabel(isBlank(invoice.getNguoiThu()) ? "-" : invoice.getNguoiThu()));
            addFormRow(form, gbc, 6, "Phương thức", createValueLabel(isBlank(invoice.getPhuongThuc()) ? "-" : invoice.getPhuongThuc()));
            if (!isBlank(invoice.getThongTinThanhToanKetHop())) {
                addFormRow(form, gbc, 7, "Chi tiết kết hợp", new JScrollPane(createReadonlyArea(invoice.getThongTinThanhToanKetHop())));
            }

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            body.add(card);
            body.add(Box.createVerticalStrut(10));
            body.add(buildInvoiceLineSection("CHI TIẾT HÓA ĐƠN", invoice));

            content.add(body, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("In hóa đơn", new Color(37, 99, 235), Color.WHITE, e -> {
                        showSuccess("In hóa đơn thành công.");
                        dispose();
                    })
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class ModernInvoicePreviewDialog extends BasePaymentDialog {
        private final ThanhToan invoice;
        private final InvoicePrintPanel previewPanel;

        private ModernInvoicePreviewDialog(Frame owner, ThanhToan invoice) {
            super(owner, "Xem trước hóa đơn", 1040, 820);
            this.invoice = invoice;
            this.previewPanel = new InvoicePrintPanel(invoice);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("XEM TRƯỚC HÓA ĐƠN", "Mẫu hóa đơn khách sạn theo bố cục header-detail để xem trước và in."), BorderLayout.NORTH);

            JScrollPane scrollPane = new JScrollPane(previewPanel);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            scrollPane.getVerticalScrollBar().setUnitIncrement(18);
            scrollPane.getViewport().setBackground(APP_BG);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            styleInvoicePreviewScrollBar(scrollPane.getVerticalScrollBar());

            content.add(scrollPane, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("In hóa đơn", new Color(37, 99, 235), Color.WHITE, e -> printInvoice())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void printInvoice() {
            Dimension preferred = previewPanel.getPreferredSize();
            previewPanel.setSize(preferred);
            previewPanel.doLayout();

            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("HoaDon_" + formatInvoiceCode(invoice.getMaHoaDon()));
            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }

                    Graphics2D g2 = (Graphics2D) graphics.create();
                    try {
                        double scale = Math.min(
                                pageFormat.getImageableWidth() / previewPanel.getWidth(),
                                pageFormat.getImageableHeight() / previewPanel.getHeight()
                        );
                        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                        g2.scale(scale, scale);
                        previewPanel.printAll(g2);
                        return Printable.PAGE_EXISTS;
                    } finally {
                        g2.dispose();
                    }
                }
            });

            if (!printerJob.printDialog()) {
                return;
            }

            try {
                printerJob.print();
                showSuccess("In hóa đơn thành công.");
                dispose();
            } catch (PrinterException ex) {
                showError("Không thể in hóa đơn: " + safeValue(ex.getMessage(), "Không xác định."));
            }
        }
    }

    private final class InvoiceDetailDialog extends BasePaymentDialog {
        private InvoiceDetailDialog(Frame owner, ThanhToan invoice) {
            super(owner, "Chi tiết hóa đơn", 720, 540);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("CHI TIẾT HÓA ĐƠN", "Thông tin hóa đơn ở chế độ chỉ đọc."), BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel("HD" + invoice.getMaHoaDon()));
            addFormRow(form, gbc, 1, "Hồ sơ lưu trú", createValueLabel(safeValue(invoice.getMaHoSo(), "-")));
            addFormRow(form, gbc, 2, "Khách / Phòng", createValueLabel(safeValue(invoice.getKhachHang(), "-") + " / " + safeValue(invoice.getSoPhong(), "-")));
            addFormRow(form, gbc, 3, "Tiền phòng", createValueLabel(ThanhToan.formatMoney(invoice.getTienPhong())));
            addFormRow(form, gbc, 4, "Tiền dịch vụ", createValueLabel(ThanhToan.formatMoney(invoice.getTienDichVu())));
            addFormRow(form, gbc, 5, "Phụ thu", createValueLabel(ThanhToan.formatMoney(invoice.getPhuThu())));
            addFormRow(form, gbc, 6, "Giảm giá", createValueLabel(ThanhToan.formatMoney(invoice.getGiamGia())));
            addFormRow(form, gbc, 7, "Trừ đặt cọc", createValueLabel(ThanhToan.formatMoney(invoice.getTienCocTru())));
            addFormRow(form, gbc, 8, "Tổng thanh toán", createValueLabel(ThanhToan.formatMoney(invoice.getTongPhaiThu())));
            addFormRow(form, gbc, 9, "Đã thanh toán", createValueLabel(ThanhToan.formatMoney(invoice.getSoTienDaThanhToan())));
            addFormRow(form, gbc, 10, "Đã hoàn cọc", createValueLabel(ThanhToan.formatMoney(invoice.getTienCocDaHoan())));
            addFormRow(form, gbc, 11, "Trạng thái", createValueLabel(safeValue(invoice.getTrangThai(), "-")));
            addFormRow(form, gbc, 12, "Ghi chú", new JScrollPane(createReadonlyArea(safeValue(invoice.getGhiChu(), "-"))));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            body.add(card);
            body.add(Box.createVerticalStrut(10));
            body.add(buildInvoiceLineSection("CÁC DÒNG HÓA ĐƠN", invoice));

            content.add(body, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JPanel buildInvoiceLineSection(String title, ThanhToan invoice) {
        JPanel card = createDialogCardPanel();
        JLabel lbl = new JLabel(title);
        lbl.setFont(SECTION_FONT);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] columns = {"STT", "Loại dòng", "Diễn giải", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        List<ChiTietDong> lines = invoice.getChiTiet();
        for (int i = 0; i < lines.size(); i++) {
            ChiTietDong line = lines.get(i);
            model.addRow(new Object[]{
                    i + 1,
                    safeValue(line.getLoaiChiPhi(), "-"),
                    safeValue(line.getDienGiai(), "-"),
                    line.getSoLuong(),
                    ThanhToan.formatMoney(line.getDonGia()),
                    ThanhToan.formatMoney(line.getThanhTien())
            });
        }

        JTable table = new JTable(model);
        table.setFont(BODY_FONT);
        table.setRowHeight(28);
        ScreenUIHelper.styleTableHeader(table);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lbl, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private String formatInvoiceCode(String maHoaDon) {
        return isBlank(maHoaDon) ? "-" : "HD-" + maHoaDon;
    }

    private String formatCurrency(double value) {
        return ThanhToan.formatMoney(value) + " đ";
    }

    private String formatDateOnly(Timestamp value) {
        if (value == null) {
            return "-";
        }
        return value.toLocalDateTime().format(DATE_FORMAT);
    }

    private String resolveStayNights(ThanhToan invoice) {
        int nights = 0;
        for (ChiTietDong line : invoice.getChiTiet()) {
            String type = safeValue(line.getLoaiChiPhi(), "").toLowerCase(Locale.ROOT);
            if (type.contains("phòng") || type.contains("phong")) {
                nights = Math.max(nights, line.getSoLuong());
            }
        }
        return nights > 0 ? String.valueOf(nights) : "-";
    }

    private String resolveLinePeriod(ThanhToan invoice, ChiTietDong line) {
        String type = safeValue(line.getLoaiChiPhi(), "").toLowerCase(Locale.ROOT);
        if (type.contains("phòng") || type.contains("phong")) {
            return "Theo đêm lưu trú";
        }
        if (type.contains("dịch vụ") || type.contains("dich vu")) {
            return formatDateOnly(invoice.getNgayThanhToan());
        }
        return formatDateOnly(invoice.getNgayThanhToan());
    }

    private List<InvoiceLineView> buildInvoicePreviewLines(ThanhToan invoice) {
        List<InvoiceLineView> rows = new ArrayList<InvoiceLineView>();
        List<ChiTietDong> lines = invoice.getChiTiet();
        for (int i = 0; i < lines.size(); i++) {
            ChiTietDong line = lines.get(i);
            rows.add(new InvoiceLineView(
                    i + 1,
                    safeValue(line.getDienGiai(), safeValue(line.getLoaiChiPhi(), "-")),
                    resolveLinePeriod(invoice, line),
                    Math.max(1, line.getSoLuong()),
                    line.getDonGia(),
                    line.getThanhTien()
            ));
        }

        if (!rows.isEmpty()) {
            return rows;
        }

        int index = 1;
        if (invoice.getTienPhong() > 0d) {
            rows.add(new InvoiceLineView(index++, "Tiền phòng " + safeValue(invoice.getSoPhong(), "-"), "Theo đêm lưu trú", 1, invoice.getTienPhong(), invoice.getTienPhong()));
        }
        if (invoice.getTienDichVu() > 0d) {
            rows.add(new InvoiceLineView(index++, "Dịch vụ phát sinh", formatDateOnly(invoice.getNgayThanhToan()), 1, invoice.getTienDichVu(), invoice.getTienDichVu()));
        }
        if (invoice.getPhuThu() > 0d) {
            rows.add(new InvoiceLineView(index, "Phụ thu", formatDateOnly(invoice.getNgayThanhToan()), 1, invoice.getPhuThu(), invoice.getPhuThu()));
        }
        return rows;
    }

    private JPanel createInvoiceHeaderPanel(ThanhToan invoice) {
        Color darkBlue = new Color(39, 86, 132);
        Color lightBlue = new Color(227, 237, 247);

        JPanel header = new JPanel(new GridLayout(1, 2, 0, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel left = new JPanel();
        left.setBackground(darkBlue);
        left.setBorder(new EmptyBorder(16, 18, 14, 18));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel hotel = new JLabel(AppBranding.APP_DISPLAY_NAME);
        hotel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hotel.setForeground(Color.WHITE);
        JLabel line1 = createHeaderLine("Địa chỉ: 12 Nguyễn Văn Bảo, Phường Hạnh Thông, Quận Gò Vấp,TP.HCM", Color.WHITE);
        JLabel line2 = createHeaderLine("Điện thoại: 0236 3 888 999 | Email:info@hotelvtttt.vn", Color.WHITE);
        JLabel line3 = createHeaderLine("MST: 4201234567", Color.WHITE);
        left.add(hotel);
        left.add(Box.createVerticalStrut(8));
        left.add(line1);
        left.add(Box.createVerticalStrut(2));
        left.add(line2);
        left.add(Box.createVerticalStrut(2));
        left.add(line3);
        left.add(Box.createVerticalStrut(2));

        JPanel right = new JPanel();
        right.setBackground(lightBlue);
        right.setBorder(new EmptyBorder(14, 18, 14, 18));
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("HÓA ĐƠN KHÁCH SẠN", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(darkBlue);
        JLabel invoiceNo = new JLabel("Số HĐ: " + formatInvoiceCode(invoice.getMaHoaDon()), SwingConstants.CENTER);
        invoiceNo.setAlignmentX(Component.CENTER_ALIGNMENT);
        invoiceNo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        invoiceNo.setForeground(TEXT_PRIMARY);
        JLabel issueDate = new JLabel("Ngày lập: " + formatDateOnly(invoice.getNgayLap()), SwingConstants.CENTER);
        issueDate.setAlignmentX(Component.CENTER_ALIGNMENT);
        issueDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        issueDate.setForeground(TEXT_PRIMARY);
        right.add(title);
        right.add(Box.createVerticalStrut(8));
        right.add(Box.createVerticalStrut(6));
        right.add(invoiceNo);
        right.add(Box.createVerticalStrut(6));
        right.add(issueDate);

        header.add(left);
        header.add(right);
        return header;
    }

    private JLabel createHeaderLine(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(color);
        return label;
    }

    private JPanel createInvoiceInfoSection(String title, String[][] rows) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createLineBorder(new Color(191, 219, 254), 1));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        heading.setForeground(new Color(39, 86, 132));
        heading.setBorder(new EmptyBorder(10, 12, 4, 12));
        card.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(0, 12, 10, 12));
        for (String[] row : rows) {
            JLabel label = new JLabel(row[0] + ": " + safeValue(row[1], "-"));
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            label.setForeground(TEXT_PRIMARY);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(label);
            body.add(Box.createVerticalStrut(4));
        }
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCompactCustomerInfoSection(ThanhToan invoice) {
        return createInvoiceInfoSection("THÔNG TIN KHÁCH HÀNG", new String[][]{
                {"Khách hàng", safeValue(invoice.getKhachHang(), "-")},
                {"CCCD/Hộ chiếu", safeValue(invoice.getCccdPassport(), "-")},
                {"Điện thoại", safeValue(invoice.getSoDienThoai(), "-")},
                {"Email", safeValue(invoice.getEmail(), "-")}
        });
    }

    private JPanel createCompactStayInfoSection(ThanhToan invoice) {
        return createInvoiceInfoSection("THÔNG TIN LƯU TRÚ", new String[][]{
                {"Mã đặt phòng", safeValue(invoice.getMaHoSo(), isBlank(invoice.getMaDatPhong()) ? "-" : "DP-" + invoice.getMaDatPhong())},
                {"Phòng", safeValue(invoice.getSoPhong(), "-")},
                {"Check-in", formatDateTime(invoice.getNgayNhanPhong())},
                {"Check-out", formatDateTime(invoice.getNgayTraPhong())},
                {"Số đêm", resolveStayNights(invoice)}
        });
    }

    private JPanel createInvoiceTablePanelWithoutPeriod(ThanhToan invoice) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("CHI TIẾT THANH TOÁN");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(new Color(39, 86, 132));
        panel.add(heading, BorderLayout.NORTH);

        JPanel table = new JPanel(new GridBagLayout());
        table.setOpaque(false);
        double[] weights = {0.7d, 3.45d, 0.8d, 1.25d, 1.4d};
        String[] header = {"STT", "Diễn giải", "SL", "Đơn giá (VND)", "Thành tiền\n(VND)"};
        addInvoiceGridRow(table, 0, header, weights, true, false);

        List<InvoiceLineView> rows = buildInvoicePreviewLines(invoice);
        if (rows.isEmpty()) {
            addInvoiceGridRow(table, 1, new String[]{"-", "Chưa có dòng chi tiết", "-", "-", "-"}, weights, false, false);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                InvoiceLineView row = rows.get(i);
                addInvoiceGridRow(table, i + 1, new String[]{
                        String.valueOf(row.index),
                        row.description,
                        String.valueOf(row.quantity),
                        ThanhToan.formatMoney(row.unitPrice),
                        ThanhToan.formatMoney(row.total)
                }, weights, false, false);
            }
        }

        int rowCount = Math.max(2, rows.size() + 1);
        table.setPreferredSize(new Dimension(0, rowCount * 42));
        table.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowCount * 42));
        panel.add(table, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowCount * 42 + 42));
        return panel;
    }

    private JPanel createInvoiceTablePanel(ThanhToan invoice) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel("CHI TIẾT THANH TOÁN");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        heading.setForeground(new Color(39, 86, 132));
        panel.add(heading, BorderLayout.NORTH);

        JPanel table = new JPanel(new GridBagLayout());
        table.setOpaque(false);
        double[] weights = {0.7d, 2.8d, 1.45d, 0.7d, 1.2d, 1.3d};
        String[] header = {"STT", "Diễn giải", "Ngày/Khung\nthời gian", "SL", "Đơn giá (VND)", "Thành tiền\n(VND)"};
        addInvoiceGridRow(table, 0, header, weights, true, false);

        List<InvoiceLineView> rows = buildInvoicePreviewLines(invoice);
        if (rows.isEmpty()) {
            addInvoiceGridRow(table, 1, new String[]{"-", "Chưa có dòng chi tiết", "-", "-", "-", "-"}, weights, false, false);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                InvoiceLineView row = rows.get(i);
                addInvoiceGridRow(table, i + 1, new String[]{
                        String.valueOf(row.index),
                        row.description,
                        row.period,
                        String.valueOf(row.quantity),
                        ThanhToan.formatMoney(row.unitPrice),
                        ThanhToan.formatMoney(row.total)
                }, weights, false, false);
            }
        }
        int rowCount = Math.max(2, rows.size() + 1);
        table.setPreferredSize(new Dimension(0, rowCount * 42));
        table.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowCount * 42));
        panel.add(table, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowCount * 42 + 42));
        return panel;
    }

    private void addInvoiceGridRow(JPanel panel, int rowIndex, String[] values, double[] weights, boolean header, boolean alternate) {
        Color darkBlue = new Color(39, 86, 132);
        Color border = new Color(191, 219, 254);
        for (int i = 0; i < values.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.gridy = rowIndex;
            gbc.weightx = weights[i];
            gbc.fill = GridBagConstraints.BOTH;

            JLabel cell = new JLabel("<html><div style='text-align:center;'>" + safeValue(values[i], "-").replace("\n", "<br>") + "</div></html>");
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, border),
                    new EmptyBorder(header ? 10 : 8, 8, header ? 10 : 8, 8)
            ));
            cell.setFont(new Font("Segoe UI", header ? Font.BOLD : Font.PLAIN, header ? 12 : 13));
            cell.setForeground(header ? Color.WHITE : TEXT_PRIMARY);
            cell.setBackground(header ? darkBlue : Color.WHITE);
            if (i == 1) {
                cell.setHorizontalAlignment(SwingConstants.LEFT);
            } else if (!header && i >= values.length - 2) {
                cell.setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                cell.setHorizontalAlignment(SwingConstants.CENTER);
            }
            panel.add(cell, gbc);
        }
    }

    private JPanel createInvoiceTotalsPanel(ThanhToan invoice) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 0, 0, 0));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        double tamTinh = invoice.getTienPhong() + invoice.getTienDichVu() + invoice.getPhuThu();
        double daThanhToan = invoice.getTienCocTru() + invoice.getSoTienDaThanhToan();

        addTotalGridRow(grid, 0, "Tạm tính", ThanhToan.formatMoney(tamTinh), TEXT_PRIMARY, false);
        addTotalGridRow(grid, 1, "Giảm giá", ThanhToan.formatMoney(invoice.getGiamGia()), TEXT_PRIMARY, false);
        addTotalGridRow(grid, 3, "Đã thanh toán", ThanhToan.formatMoney(daThanhToan), TEXT_PRIMARY, false);
        addTotalGridRow(grid, 4, "Còn lại", ThanhToan.formatMoney(invoice.getConPhaiThu()), new Color(220, 38, 38), true);
        wrapper.add(grid, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        return wrapper;
    }

    private void addTotalGridRow(JPanel panel, int row, String label, String value, Color valueColor, boolean emphasized) {
        Color border = new Color(191, 219, 254);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.weightx = 1.2;
        left.fill = GridBagConstraints.BOTH;

        JLabel lblLeft = new JLabel(label);
        lblLeft.setOpaque(true);
        lblLeft.setBackground(Color.WHITE);
        lblLeft.setFont(new Font("Segoe UI", emphasized ? Font.BOLD : Font.PLAIN, 13));
        lblLeft.setForeground(TEXT_PRIMARY);
        lblLeft.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, border),
                new EmptyBorder(7, 8, 7, 8)
        ));
        panel.add(lblLeft, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1.0;
        right.fill = GridBagConstraints.BOTH;

        JLabel lblRight = new JLabel(value, SwingConstants.RIGHT);
        lblRight.setOpaque(true);
        lblRight.setBackground(Color.WHITE);
        lblRight.setFont(new Font("Segoe UI", Font.BOLD, emphasized ? 16 : 13));
        lblRight.setForeground(valueColor);
        lblRight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 1, border),
                new EmptyBorder(7, 8, 7, 8)
        ));
        panel.add(lblRight, right);
    }

    private JPanel createInvoiceFooterPanel(ThanhToan invoice) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        StringBuilder note = new StringBuilder("Ghi chú: ");
        if (!isBlank(invoice.getGhiChu())) {
            note.append(invoice.getGhiChu());
        } else {
            note.append("Quý khách vui lòng kiểm tra thông tin trước khi thanh toán.");
        }
        if (!isBlank(invoice.getThongTinThanhToanKetHop())) {
            note.append(" ").append(invoice.getThongTinThanhToanKetHop());
        }

        JLabel noteLabel = new JLabel("<html><b>Ghi chú:</b> " + note.substring("Ghi chú: ".length()) + "</html>");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noteLabel.setForeground(TEXT_PRIMARY);
        panel.add(noteLabel);
        panel.add(Box.createVerticalStrut(8));

        JPanel signatures = new JPanel(new GridLayout(1, 2, 24, 0));
        signatures.setOpaque(false);
        signatures.add(createSignatureLabel("Khách hàng"));
        signatures.add(createSignatureLabel("Lễ tân / Thu ngân"));
        panel.add(signatures);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        return panel;
    }

    private JPanel createSignatureLabel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(48), BorderLayout.CENTER);
        return panel;
    }

    private final class InvoicePrintPanel extends JPanel implements Scrollable {
        private InvoicePrintPanel(ThanhToan invoice) {
            setBackground(Color.WHITE);
            setOpaque(true);
            setBorder(new EmptyBorder(16, 18, 18, 18));
            setLayout(new GridBagLayout());

            JPanel header = createInvoiceHeaderPanel(invoice);
            JPanel info = new JPanel(new GridLayout(1, 2, 0, 0));
            info.setOpaque(false);
            info.add(createInvoiceInfoSection("THÔNG TIN KHÁCH HÀNG", new String[][]{
                    {"Khách hàng", safeValue(invoice.getKhachHang(), "-")},
                    {"CCCD/Hộ chiếu", "-"},
                    {"Điện thoại", safeValue(invoice.getSoDienThoai(), "-")},
                    {"Email", "-"}
            }));
            info.add(createInvoiceInfoSection("THÔNG TIN LƯU TRÚ", new String[][]{
                    {"Mã đặt phòng", safeValue(invoice.getMaHoSo(), isBlank(invoice.getMaDatPhong()) ? "-" : "DP-" + invoice.getMaDatPhong())},
                    {"Phòng", safeValue(invoice.getSoPhong(), "-")},
                    {"Check-in", "-"},
                    {"Check-out", "-"},
                    {"Số đêm", resolveStayNights(invoice)}
            }));

            info.removeAll();
            info.add(createCompactCustomerInfoSection(invoice));
            info.add(createCompactStayInfoSection(invoice));

            JPanel detail = createInvoiceTablePanelWithoutPeriod(invoice);
            JPanel totals = createInvoiceTotalsPanel(invoice);
            JPanel footer = createInvoiceFooterPanel(invoice);

            addInvoiceSection(this, header, 0, 0, 1.0, 0.0, new Insets(0, 0, 18, 0));
            addInvoiceSection(this, info, 1, 0, 1.0, 0.0, new Insets(0, 0, 20, 0));
            addInvoiceSection(this, detail, 2, 0, 1.0, 0.0, new Insets(0, 0, 18, 0));
            addInvoiceSection(this, totals, 3, 0, 1.0, 0.0, new Insets(0, 0, 12, 0));
            addInvoiceSection(this, footer, 4, 0, 1.0, 1.0, new Insets(0, 0, 0, 0));

            int contentHeight = 0;
            contentHeight += header.getPreferredSize().height;
            contentHeight += 18;
            contentHeight += info.getPreferredSize().height;
            contentHeight += 20;
            contentHeight += detail.getPreferredSize().height;
            contentHeight += 18;
            contentHeight += totals.getPreferredSize().height;
            contentHeight += 12;
            contentHeight += footer.getPreferredSize().height;
            contentHeight += 34;

            int preferredHeight = Math.max(1120, contentHeight);
            setPreferredSize(new Dimension(820, preferredHeight));
            revalidate();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 48, 120);
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

    private void addInvoiceSection(JPanel container, Component component, int gridy, int fill, double weightx, double weighty, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.fill = fill == 0 ? GridBagConstraints.HORIZONTAL : fill;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        container.add(component, gbc);
    }

    private static final class InvoiceLineView {
        private final int index;
        private final String description;
        private final String period;
        private final int quantity;
        private final double unitPrice;
        private final double total;

        private InvoiceLineView(int index, String description, String period, int quantity, double unitPrice, double total) {
            this.index = index;
            this.description = description;
            this.period = period;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }
    }

    private JTextArea createReadonlyArea(String text) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(text);
        return area;
    }

    private void styleInvoicePreviewScrollBar(JScrollBar scrollBar) {
        if (scrollBar == null) {
            return;
        }
        scrollBar.setPreferredSize(new Dimension(18, 0));
        scrollBar.setBackground(new Color(235, 242, 251));
        scrollBar.setOpaque(true);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.trackColor = new Color(226, 236, 248);
                this.thumbColor = new Color(182, 205, 234);
                this.thumbDarkShadowColor = new Color(150, 182, 220);
                this.thumbHighlightColor = new Color(214, 228, 245);
                this.thumbLightShadowColor = new Color(170, 196, 228);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createScrollButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createScrollButton();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
                g.setColor(new Color(173, 196, 226));
                g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width - 1, trackBounds.height - 1);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(new Color(188, 209, 236));
                    g2.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - 1, thumbBounds.height - 1);
                    g2.setColor(new Color(126, 160, 204));
                    g2.drawRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - 1, thumbBounds.height - 1);
                    g2.setColor(Color.WHITE);
                    int centerX = thumbBounds.x + (thumbBounds.width / 2);
                    int centerY = thumbBounds.y + (thumbBounds.height / 2);
                    g2.drawLine(centerX - 4, centerY - 2, centerX + 4, centerY - 2);
                    g2.drawLine(centerX - 4, centerY, centerX + 4, centerY);
                    g2.drawLine(centerX - 4, centerY + 2, centerX + 4, centerY + 2);
                } finally {
                    g2.dispose();
                }
            }

            private JButton createScrollButton() {
                JButton button = new JButton();
                button.setBackground(new Color(224, 236, 250));
                button.setBorder(BorderFactory.createLineBorder(new Color(173, 196, 226)));
                button.setFocusPainted(false);
                button.setPreferredSize(new Dimension(18, 18));
                return button;
            }
        });
    }

    private String findEmployeeName() {
        try {
            java.sql.Connection con = db.ConnectDB.getConnection();
            if (con == null) {
                return username;
            }
            try (java.sql.PreparedStatement ps = con.prepareStatement(
                    "SELECT TOP 1 nv.hoTen FROM TaiKhoan tk JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien WHERE tk.tenDangNhap = ?")) {
                ps.setString(1, username);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return safeValue(rs.getString(1), username);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return username;
    }

    private int findEmployeeId() {
        try {
            java.sql.Connection con = db.ConnectDB.getConnection();
            if (con == null) {
                return 1;
            }
            try (java.sql.PreparedStatement ps = con.prepareStatement(
                    "SELECT TOP 1 nv.maNhanVien FROM TaiKhoan tk JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien WHERE tk.tenDangNhap = ?")) {
                ps.setString(1, username);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return 1;
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
        comboBox.setPreferredSize(new Dimension(165, 34));
        comboBox.setMaximumSize(new Dimension(220, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(160, 34));
        field.setMaximumSize(new Dimension(320, 34));
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
        lbl.setPreferredSize(new Dimension(145, 20));

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

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
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

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
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

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_FORMAT);
        } catch (Exception ex) {
            return null;
        }
    }

    private String formatDateTime(Timestamp value) {
        if (value == null) {
            return "-";
        }
        return value.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private final class ConfirmDialog extends BasePaymentDialog {
        private boolean confirmed;

        private ConfirmDialog(Frame owner, String title, String message, String confirmText, Color confirmColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> {
                        confirmed = true;
                        dispose();
                    })
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() {
            return confirmed;
        }
    }

    private final class AppMessageDialog extends BasePaymentDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }
}
