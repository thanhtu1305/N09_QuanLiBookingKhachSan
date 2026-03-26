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
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;

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

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<InvoiceRecord> allInvoices = new ArrayList<InvoiceRecord>();
    private final List<InvoiceRecord> filteredInvoices = new ArrayList<InvoiceRecord>();

    private JTable tblHoaDon;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboPhuongThuc;
    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
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

    public ThanhToanGUI() {
        this("guest", "Lễ tân");
    }

    public ThanhToanGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Thanh toán - " + AppBranding.APP_DISPLAY_NAME);
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

        JLabel lblSub = new JLabel("Quản lý hóa đơn, thu tiền và hoàn cọc theo đúng luồng nghiệp vụ khách sạn.");
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
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Chờ thanh toán", "Đã thanh toán", "Đã hoàn cọc"});
        cboPhuongThuc = createComboBox(new String[]{"Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ", "Kết hợp"});
        txtTuNgay = createInputField("10/03/2026");
        txtDenNgay = createInputField("16/03/2026");
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(290, 34));
        txtTuKhoa.setToolTipText("Mã hóa đơn / số phòng / tên khách / số điện thoại");

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

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Thanh toán",
                "F2 In hóa đơn",
                "F3 Áp giảm giá",
                "F4 Hoàn cọc",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách hóa đơn");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Bấm một dòng để xem chi tiết hóa đơn.");
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
        tblHoaDon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblHoaDon.getTableHeader().setBackground(new Color(243, 244, 246));
        tblHoaDon.getTableHeader().setForeground(TEXT_PRIMARY);

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

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
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

    private void seedSampleData() {
        allInvoices.clear();
    }

    private InvoiceRecord createSampleInvoice(String maHoaDon, String maHoSo, String khachHang, String soPhong, String soDienThoai,
                                              String trangThai, String phuongThucThanhToan, String ngayHoaDon, double tienCoc,
                                              InvoiceLineRecord... lines) {
        InvoiceRecord invoice = InvoiceRecord.createHeader(maHoaDon, maHoSo, khachHang, soPhong, soDienThoai, trangThai, phuongThucThanhToan, ngayHoaDon);
        for (InvoiceLineRecord line : lines) {
            line.maHoaDon = maHoaDon;
            invoice.details.add(line);
        }
        invoice.tienCoc = tienCoc;
        invoice.syncSummaryFromDetails();
        if ("HD240506".equals(maHoaDon)) {
            invoice.tienCocDaHoan = tienCoc;
        }
        return invoice;
    }

    private InvoiceLineRecord createInvoiceLine(String maChiTietHoaDon, String loaiDong, String dienGiai, int soLuong,
                                                double donGia, double thanhTien, int thuTuHienThi, String ghiChu) {
        return InvoiceLineRecord.create(maChiTietHoaDon, "", loaiDong, dienGiai, soLuong, donGia, thanhTien, thuTuHienThi, ghiChu);
    }

    private void reloadSampleData(boolean showMessage) {
        cboTrangThai.setSelectedIndex(0);
        cboPhuongThuc.setSelectedIndex(0);
        txtTuNgay.setText("10/03/2026");
        txtDenNgay.setText("16/03/2026");
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu thanh toán.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredInvoices.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String phuongThuc = valueOf(cboPhuongThuc.getSelectedItem());
        String fromDate = txtTuNgay.getText() == null ? "" : txtTuNgay.getText().trim();
        String toDate = txtDenNgay.getText() == null ? "" : txtDenNgay.getText().trim();
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (InvoiceRecord invoice : allInvoices) {
            if (!"Tất cả".equals(trangThai) && !invoice.trangThai.equals(trangThai)) {
                continue;
            }
            if (!"Tất cả".equals(phuongThuc) && !invoice.phuongThucThanhToan.equals(phuongThuc)) {
                continue;
            }
            if (!fromDate.isEmpty() && invoice.ngayHoaDon.compareTo(fromDate) < 0) {
                continue;
            }
            if (!toDate.isEmpty() && invoice.ngayHoaDon.compareTo(toDate) > 0) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (invoice.maHoaDon + " " + invoice.soPhong + " " + invoice.khachHang + " " + invoice.soDienThoai).toLowerCase(Locale.ROOT);
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
        for (InvoiceRecord invoice : filteredInvoices) {
            tableModel.addRow(new Object[]{
                    invoice.maHoaDon,
                    invoice.khachHang,
                    invoice.getRoomLineSummary(),
                    invoice.getTongPhaiThuLabel(),
                    invoice.trangThai,
                    invoice.ngayHoaDon
            });
        }

        if (!filteredInvoices.isEmpty()) {
            tblHoaDon.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredInvoices.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(InvoiceRecord invoice) {
        lblMaHoaDon.setText(invoice.maHoaDon);
        lblMaHoSo.setText(invoice.maHoSo);
        lblKhachHang.setText(invoice.khachHang);
        lblSoPhong.setText(invoice.soPhong);
        lblTienPhong.setText(invoice.getTienPhongLabel());
        lblTienDichVu.setText(invoice.getTienDichVuLabel());
        lblPhuThu.setText(invoice.getPhuThuLabel());
        lblGiamGia.setText(invoice.getGiamGiaLabel());
        lblTienCoc.setText(invoice.getTienCocLabel());
        lblTongPhaiThu.setText(invoice.getTongPhaiThuLabel());
        lblPhuongThucThanhToan.setText(invoice.phuongThucThanhToan.isEmpty() ? "-" : invoice.phuongThucThanhToan);
        lblTrangThai.setText(invoice.trangThai);

        lblTongTienPhong.setText(invoice.getTienPhongLabel());
        lblTongDichVu.setText(invoice.getTienDichVuLabel());
        lblTongGiamGia.setText(invoice.getGiamGiaLabel());
        lblTongDatCoc.setText(invoice.getTienCocConLaiLabel());
        lblConPhaiThu.setText(invoice.getTongPhaiThuLabel());
        lblSoDongChiTiet.setText(String.valueOf(invoice.details.size()));
        lblNgayLapHoaDon.setText(invoice.ngayHoaDon);
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
    }

    private InvoiceRecord getSelectedInvoice() {
        int row = tblHoaDon.getSelectedRow();
        if (row < 0 || row >= filteredInvoices.size()) {
            showWarning("Vui lòng chọn một hóa đơn trong danh sách.");
            return null;
        }
        return filteredInvoices.get(row);
    }

    private void openPaymentDialog() {
        InvoiceRecord invoice = getSelectedInvoice();
        if (invoice != null) {
            new PaymentDialog(this, invoice).setVisible(true);
        }
    }

    private void openDiscountDialog() {
        InvoiceRecord invoice = getSelectedInvoice();
        if (invoice != null) {
            new DiscountDialog(this, invoice).setVisible(true);
        }
    }

    private void openInvoicePreviewDialog() {
        InvoiceRecord invoice = getSelectedInvoice();
        if (invoice != null) {
            new InvoicePreviewDialog(this, invoice).setVisible(true);
        }
    }

    private void openDepositRefundDialog() {
        InvoiceRecord invoice = getSelectedInvoice();
        if (invoice != null) {
            new DepositRefundDialog(this, invoice).setVisible(true);
        }
    }

    private void openInvoiceDetailDialog() {
        InvoiceRecord invoice = getSelectedInvoice();
        if (invoice != null) {
            new InvoiceDetailDialog(this, invoice).setVisible(true);
        }
    }

    private void refreshInvoiceViews(InvoiceRecord invoice, String message) {
        applyFilters(false);
        selectInvoice(invoice);
        showSuccess(message);
    }

    private void selectInvoice(InvoiceRecord invoice) {
        if (invoice == null) {
            return;
        }
        int index = filteredInvoices.indexOf(invoice);
        if (index >= 0) {
            tblHoaDon.setRowSelectionInterval(index, index);
            updateDetailPanel(invoice);
        } else if (!filteredInvoices.isEmpty()) {
            tblHoaDon.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredInvoices.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "thanhtoan-f1", this::openPaymentDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "thanhtoan-f2", this::openInvoicePreviewDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "thanhtoan-f3", this::openDiscountDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "thanhtoan-f4", this::openDepositRefundDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "thanhtoan-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "thanhtoan-enter", this::openInvoiceDetailDialog);
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

    private JTable createInvoiceLineTable(InvoiceRecord invoice) {
        String[] columns = {"STT", "Loại dòng", "Diễn giải", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        List<InvoiceLineRecord> lines = invoice.getSortedDetails();
        for (int i = 0; i < lines.size(); i++) {
            InvoiceLineRecord line = lines.get(i);
            model.addRow(new Object[]{
                    i + 1,
                    line.loaiDong,
                    line.dienGiai,
                    line.soLuong,
                    formatMoney(line.donGia),
                    formatMoney(line.thanhTien)
            });
        }

        JTable table = new JTable(model);
        table.setFont(BODY_FONT);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFillsViewportHeight(true);
        return table;
    }

    private JPanel buildInvoiceLineSection(String title, InvoiceRecord invoice) {
        JPanel card = createDialogCardPanel();
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        JLabel lblSection = new JLabel(title);
        lblSection.setFont(SECTION_FONT);
        lblSection.setForeground(TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(createInvoiceLineTable(invoice));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        scrollPane.setPreferredSize(new Dimension(0, 180));

        wrapper.add(lblSection, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        card.add(wrapper, BorderLayout.CENTER);
        return card;
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
        private final InvoiceRecord invoice;

        private PaymentDialog(Frame owner, InvoiceRecord invoice) {
            super(owner, "Thanh toán hóa đơn", 980, 660);
            this.invoice = invoice;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("THANH TOÁN HÓA ĐƠN", "Chỉ thanh toán cho hóa đơn đang ở trạng thái Chờ thanh toán."), BorderLayout.NORTH);

            JPanel leftColumn = new JPanel();
            leftColumn.setOpaque(false);
            leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
            JPanel infoSection = buildInvoiceInfoSection();
            infoSection.setPreferredSize(new Dimension(340, infoSection.getPreferredSize().height));
            JPanel amountSection = buildAmountSection();
            amountSection.setPreferredSize(new Dimension(340, amountSection.getPreferredSize().height));
            JPanel paymentSection = buildPaymentSection();
            paymentSection.setPreferredSize(new Dimension(340, paymentSection.getPreferredSize().height));
            leftColumn.add(infoSection);
            leftColumn.add(Box.createVerticalStrut(10));
            leftColumn.add(amountSection);
            leftColumn.add(Box.createVerticalStrut(10));
            leftColumn.add(paymentSection);

            JPanel rightColumn = new JPanel(new BorderLayout());
            rightColumn.setOpaque(false);
            rightColumn.add(buildInvoiceLineSection("CHI TIẾT HÓA ĐƠN", invoice), BorderLayout.CENTER);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, rightColumn);
            splitPane.setBorder(null);
            splitPane.setOpaque(false);
            splitPane.setResizeWeight(0.38d);
            splitPane.setDividerLocation(360);
            splitPane.setContinuousLayout(true);

            JPanel body = new JPanel(new BorderLayout());
            body.setOpaque(false);
            body.add(splitPane, BorderLayout.CENTER);
            content.add(body, BorderLayout.CENTER);

            JButton btnPay = createPrimaryButton("Xác nhận thu tiền", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnPayPrint = createOutlineButton("Xác nhận và in hóa đơn", new Color(37, 99, 235), e -> submit(true));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnPayPrint, btnPay), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private JTextField txtKhachDua;
        private JTextField txtTienThua;
        private JComboBox<String> cboPhuongThucDialog;
        private JTextField txtSoThamChieu;
        private JTextField txtNguoiThu;
        private JTextArea txtGhiChuDialog;

        private JPanel buildInvoiceInfoSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel(invoice.maHoaDon));
            addFormRow(form, gbc, 1, "Hồ sơ lưu trú", createValueLabel(invoice.maHoSo));
            addFormRow(form, gbc, 2, "Khách / Phòng", createValueLabel(invoice.khachHang));

            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildAmountSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Tiền phòng", createValueLabel(invoice.getTienPhongLabel()));
            addFormRow(form, gbc, 1, "Tiền dịch vụ", createValueLabel(invoice.getTienDichVuLabel()));
            addFormRow(form, gbc, 2, "Phụ thu", createValueLabel(invoice.getPhuThuLabel()));
            addFormRow(form, gbc, 3, "Giảm giá", createValueLabel(invoice.getGiamGiaLabel()));
            addFormRow(form, gbc, 4, "Trừ đặt cọc", createValueLabel(invoice.getTienCocLabel()));
            addFormRow(form, gbc, 5, "Tổng phải thu", createValueLabel(invoice.getTongPhaiThuLabel()));

            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildPaymentSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtKhachDua = createInputField(invoice.getTongPhaiThuLabel());
            txtTienThua = createInputField("0");
            txtTienThua.setEditable(false);
            cboPhuongThucDialog = createComboBox(new String[]{"Tiền mặt", "Thẻ", "Chuyển khoản", "Kết hợp"});
            txtSoThamChieu = createInputField("");
            txtNguoiThu = createInputField(username);
            txtGhiChuDialog = createDialogTextArea(3);
            txtKhachDua.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateTienThua();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateTienThua();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateTienThua();
                }
            });
            cboPhuongThucDialog.addActionListener(e -> updatePaymentFields());
            updatePaymentFields();

            addFormRow(form, gbc, 0, "Khách đưa", txtKhachDua);
            addFormRow(form, gbc, 1, "Tiền thừa", txtTienThua);
            addFormRow(form, gbc, 2, "Phương thức", cboPhuongThucDialog);
            addFormRow(form, gbc, 3, "Số tham chiếu", txtSoThamChieu);
            addFormRow(form, gbc, 4, "Người thu", txtNguoiThu);
            addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private void submit(boolean printAfter) {
            if (!"Chờ thanh toán".equals(invoice.trangThai)) {
                showError("Chỉ thanh toán cho hóa đơn đang ở trạng thái Chờ thanh toán.");
                return;
            }

            String method = valueOf(cboPhuongThucDialog.getSelectedItem());
            if ("Kết hợp".equals(method)) {
                new SplitPaymentDialog(ThanhToanGUI.this, invoice, txtNguoiThu.getText().trim(), txtGhiChuDialog.getText().trim(), printAfter).setVisible(true);
                dispose();
                return;
            }

            double tongPhaiThu = invoice.getTongPhaiThu();
            double khachDua = parseMoney(txtKhachDua.getText().trim());
            if (khachDua < 0) {
                showError("Khách đưa phải là số hợp lệ.");
                return;
            }
            if (txtNguoiThu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập người thu.");
                return;
            }
            if ("Tiền mặt".equals(method) && khachDua < tongPhaiThu) {
                showError("Nếu phương thức là Tiền mặt thì Khách đưa phải >= Tổng phải thu.");
                return;
            }
            if (("Thẻ".equals(method) || "Chuyển khoản".equals(method)) && txtSoThamChieu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập Số tham chiếu cho phương thức đã chọn.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận thanh toán",
                    "Hóa đơn sẽ chuyển sang Đã thanh toán và dữ liệu sẽ bị khóa. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(22, 163, 74)
            )) {
                return;
            }

            invoice.phuongThucThanhToan = method;
            invoice.nguoiThu = txtNguoiThu.getText().trim();
            invoice.soThamChieu = txtSoThamChieu.getText().trim();
            invoice.trangThai = "Đã thanh toán";
            invoice.tienThua = "Tiền mặt".equals(method) ? khachDua - tongPhaiThu : 0;
            invoice.thongTinThanhToanKetHop = "";
            invoice.ngayThanhToan = "19/03/2026";
            invoice.daKhoaDuLieu = true;
            if (!txtGhiChuDialog.getText().trim().isEmpty()) {
                invoice.ghiChu = txtGhiChuDialog.getText().trim();
            }
            refreshInvoiceViews(invoice, printAfter ? "Thanh toán thành công và sẵn sàng in hóa đơn." : "Thanh toán thành công.");
            if (printAfter) {
                new InvoicePreviewDialog(ThanhToanGUI.this, invoice).setVisible(true);
            }
            dispose();
        }

        private void updateTienThua() {
            String method = valueOf(cboPhuongThucDialog.getSelectedItem());
            double tongPhaiThu = invoice.getTongPhaiThu();
            double khachDua = parseMoney(txtKhachDua.getText().trim());
            if (khachDua < 0 || !"Tiền mặt".equals(method)) {
                txtTienThua.setText("0");
                return;
            }
            txtTienThua.setText(formatMoney(Math.max(0, khachDua - tongPhaiThu)));
        }

        private void updatePaymentFields() {
            String method = valueOf(cboPhuongThucDialog.getSelectedItem());
            boolean cash = "Tiền mặt".equals(method);
            boolean needRef = "Thẻ".equals(method) || "Chuyển khoản".equals(method);
            txtKhachDua.setEditable(cash);
            if (!cash) {
                txtKhachDua.setText(invoice.getTongPhaiThuLabel());
            }
            txtSoThamChieu.setEditable(needRef);
            if (!needRef) {
                txtSoThamChieu.setText("");
            }
            updateTienThua();
        }
    }

    private final class SplitPaymentDialog extends BasePaymentDialog {
        private final InvoiceRecord invoice;
        private final String nguoiThu;
        private final String ghiChu;
        private final boolean printAfter;

        private SplitPaymentDialog(Frame owner, InvoiceRecord invoice, String nguoiThu, String ghiChu, boolean printAfter) {
            super(owner, "Thanh toán kết hợp", 600, 440);
            this.invoice = invoice;
            this.nguoiThu = nguoiThu;
            this.ghiChu = ghiChu;
            this.printAfter = printAfter;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("THANH TOÁN KẾT HỢP", "Phân bổ số tiền theo nhiều phương thức và đảm bảo tổng nhận đúng bằng Tổng phải thu."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTienMat = createInputField("0");
            txtThe = createInputField("0");
            txtChuyenKhoan = createInputField("0");
            txtTongNhan = createInputField("0");
            txtTongNhan.setEditable(false);
            txtSoThamChieu = createInputField("");
            txtGhiChuMix = createDialogTextArea(3);
            txtGhiChuMix.setText(ghiChu);

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel(invoice.maHoaDon));
            addFormRow(form, gbc, 1, "Tổng phải thu", createValueLabel(invoice.getTongPhaiThuLabel()));
            addFormRow(form, gbc, 2, "Tiền mặt", txtTienMat);
            addFormRow(form, gbc, 3, "Thẻ", txtThe);
            addFormRow(form, gbc, 4, "Chuyển khoản", txtChuyenKhoan);
            addFormRow(form, gbc, 5, "Tổng nhận", txtTongNhan);
            addFormRow(form, gbc, 6, "Số tham chiếu", txtSoThamChieu);
            addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuMix));

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

        private JTextField txtTienMat;
        private JTextField txtThe;
        private JTextField txtChuyenKhoan;
        private JTextField txtTongNhan;
        private JTextField txtSoThamChieu;
        private JTextArea txtGhiChuMix;

        private void updateTongNhan() {
            double cash = parseMoney(txtTienMat.getText().trim());
            double card = parseMoney(txtThe.getText().trim());
            double transfer = parseMoney(txtChuyenKhoan.getText().trim());
            txtTongNhan.setText(formatMoney(Math.max(0, cash) + Math.max(0, card) + Math.max(0, transfer)));
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
            if (Math.abs(total - invoice.getTongPhaiThu()) > 0.1) {
                showError("Tổng Tiền mặt + Thẻ + Chuyển khoản phải bằng Tổng phải thu.");
                return;
            }
            if ((card > 0 || transfer > 0) && txtSoThamChieu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập Số tham chiếu khi có Thẻ hoặc Chuyển khoản.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận thanh toán",
                    "Hóa đơn sẽ chuyển sang Đã thanh toán và dữ liệu sẽ bị khóa. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(22, 163, 74)
            )) {
                return;
            }

            invoice.phuongThucThanhToan = "Kết hợp";
            invoice.soThamChieu = txtSoThamChieu.getText().trim();
            invoice.nguoiThu = nguoiThu;
            invoice.ngayThanhToan = "19/03/2026";
            invoice.trangThai = "Đã thanh toán";
            invoice.tienThua = 0;
            invoice.daKhoaDuLieu = true;
            invoice.thongTinThanhToanKetHop = "Tiền mặt: " + formatMoney(cash)
                    + " | Thẻ: " + formatMoney(card)
                    + " | Chuyển khoản: " + formatMoney(transfer);
            invoice.ghiChu = txtGhiChuMix.getText().trim();
            refreshInvoiceViews(invoice, printAfter ? "Thanh toán thành công và sẵn sàng in hóa đơn." : "Thanh toán thành công.");
            if (printAfter) {
                new InvoicePreviewDialog(ThanhToanGUI.this, invoice).setVisible(true);
            }
            dispose();
        }
    }

    private final class DiscountDialog extends BasePaymentDialog {
        private final InvoiceRecord invoice;

        private DiscountDialog(Frame owner, InvoiceRecord invoice) {
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

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel(invoice.maHoaDon));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(invoice.khachHang));
            addFormRow(form, gbc, 2, "Tổng hiện tại", createValueLabel(invoice.getTongPhaiThuLabel()));
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

        private JTextField txtGiaTri;
        private JComboBox<String> cboHinhThuc;
        private JTextField txtLyDo;
        private JTextField txtNguoiDuyet;
        private JTextArea txtGhiChu;

        private void submit() {
            if (!"Chờ thanh toán".equals(invoice.trangThai)) {
                showError("Chỉ áp dụng giảm giá cho hóa đơn đang chờ thanh toán.");
                return;
            }
            double value = parseMoney(txtGiaTri.getText().trim());
            if (value < 0) {
                showError("Giảm giá không được âm.");
                return;
            }
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do giảm giá.");
                return;
            }
            double newDiscount;
            if ("Phần trăm".equals(valueOf(cboHinhThuc.getSelectedItem()))) {
                if (value < 0 || value > 100) {
                    showError("Giảm giá theo phần trăm phải trong khoảng hợp lý.");
                    return;
                }
                newDiscount = (invoice.tienPhong + invoice.tienDichVu + invoice.phuThu) * (value / 100.0);
            } else {
                newDiscount = value;
            }
            if (newDiscount > invoice.tienPhong + invoice.tienDichVu + invoice.phuThu) {
                showError("Giảm giá không được vượt tổng hợp lệ.");
                return;
            }
            invoice.upsertSummaryLine("GIAM_GIA", txtLyDo.getText().trim(), -newDiscount, txtGhiChu.getText().trim());
            invoice.nguoiThu = txtNguoiDuyet.getText().trim();
            invoice.ghiChu = txtGhiChu.getText().trim().isEmpty() ? txtLyDo.getText().trim() : txtGhiChu.getText().trim();
            refreshInvoiceViews(invoice, "Áp dụng giảm giá thành công.");
            dispose();
        }
    }

    private final class InvoicePreviewDialog extends BasePaymentDialog {
        private InvoicePreviewDialog(Frame owner, InvoiceRecord invoice) {
            super(owner, "Xem trước hóa đơn", 720, 520);

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

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel(invoice.maHoaDon));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(invoice.khachHang));
            addFormRow(form, gbc, 2, "Ngày thanh toán", createValueLabel(invoice.ngayThanhToan.isEmpty() ? invoice.ngayHoaDon : invoice.ngayThanhToan));
            addFormRow(form, gbc, 3, "Người thu", createValueLabel(invoice.nguoiThu.isEmpty() ? "-" : invoice.nguoiThu));
            addFormRow(form, gbc, 4, "Phương thức", createValueLabel(invoice.phuongThucThanhToan.isEmpty() ? "-" : invoice.phuongThucThanhToan));
            if (!invoice.thongTinThanhToanKetHop.isEmpty()) {
                addFormRow(form, gbc, 5, "Chi tiết kết hợp", new JScrollPane(createReadonlyArea(invoice.thongTinThanhToanKetHop)));
            }

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            body.add(card);
            body.add(Box.createVerticalStrut(10));
            body.add(buildInvoiceLineSection("CHI TIẾT HÓA ĐƠN", invoice));
            body.add(Box.createVerticalStrut(10));

            JPanel totalCard = createDialogCardPanel();
            JPanel totalForm = createDialogFormPanel();
            GridBagConstraints totalGbc = new GridBagConstraints();
            totalGbc.insets = new Insets(6, 0, 6, 12);
            totalGbc.anchor = GridBagConstraints.WEST;
            addFormRow(totalForm, totalGbc, 0, "Tổng tiền phòng", createValueLabel(invoice.getTienPhongLabel()));
            addFormRow(totalForm, totalGbc, 1, "Tổng tiền dịch vụ", createValueLabel(invoice.getTienDichVuLabel()));
            addFormRow(totalForm, totalGbc, 2, "Phụ thu", createValueLabel(invoice.getPhuThuLabel()));
            addFormRow(totalForm, totalGbc, 3, "Giảm giá", createValueLabel(invoice.getGiamGiaLabel()));
            addFormRow(totalForm, totalGbc, 4, "Trừ đặt cọc", createValueLabel(invoice.getTienCocLabel()));
            addFormRow(totalForm, totalGbc, 5, "Tổng thanh toán", createValueLabel(invoice.getTongPhaiThuLabel()));
            totalCard.add(totalForm, BorderLayout.CENTER);
            body.add(totalCard);

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

    private final class DepositRefundDialog extends BasePaymentDialog {
        private final InvoiceRecord invoice;

        private DepositRefundDialog(Frame owner, InvoiceRecord invoice) {
            super(owner, "Hoàn cọc", 580, 420);
            this.invoice = invoice;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("HOÀN CỌC", "Ghi nhận giao dịch hoàn cọc cho hóa đơn đã chọn."), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtSoTienHoan = createInputField("0");
            cboHinhThuc = createComboBox(new String[]{"Tiền mặt", "Chuyển khoản"});
            txtSoThamChieu = createInputField("");
            txtLyDo = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel(invoice.maHoaDon));
            addFormRow(form, gbc, 1, "Khách / Phòng", createValueLabel(invoice.khachHang));
            addFormRow(form, gbc, 2, "Đã cọc", createValueLabel(invoice.getTienCocLabel()));
            addFormRow(form, gbc, 3, "Tổng trừ cọc", createValueLabel(invoice.getTongPhaiThuLabel()));
            addFormRow(form, gbc, 4, "Số tiền cần hoàn", txtSoTienHoan);
            addFormRow(form, gbc, 5, "Hình thức hoàn", cboHinhThuc);
            addFormRow(form, gbc, 6, "Số tham chiếu", txtSoThamChieu);
            addFormRow(form, gbc, 7, "Lý do", new JScrollPane(txtLyDo));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xác nhận hoàn", new Color(220, 38, 38), Color.WHITE, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private JTextField txtSoTienHoan;
        private JComboBox<String> cboHinhThuc;
        private JTextField txtSoThamChieu;
        private JTextArea txtLyDo;

        private void submit() {
            if (invoice.getTienCocConLai() <= 0) {
                showError("Hóa đơn này không còn số dư cọc để hoàn.");
                return;
            }
            double amount = parseMoney(txtSoTienHoan.getText().trim());
            if (amount < 0 || amount > invoice.getTienCocConLai()) {
                showError("Số tiền hoàn phải >= 0 và không vượt quá phần cọc hợp lệ.");
                return;
            }
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do hoàn cọc.");
                return;
            }
            if ("Chuyển khoản".equals(valueOf(cboHinhThuc.getSelectedItem())) && txtSoThamChieu.getText().trim().isEmpty()) {
                showError("Vui lòng nhập số tham chiếu khi hoàn bằng chuyển khoản.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận hoàn cọc",
                    "Hệ thống sẽ ghi nhận giao dịch hoàn cọc cho hóa đơn này. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(220, 38, 38)
            )) {
                return;
            }
            invoice.tienCocDaHoan += amount;
            invoice.hinhThucHoanCoc = valueOf(cboHinhThuc.getSelectedItem());
            invoice.soThamChieu = txtSoThamChieu.getText().trim();
            invoice.ghiChu = txtLyDo.getText().trim();
            if (!"Đã thanh toán".equals(invoice.trangThai)) {
                invoice.trangThai = "Đã hoàn cọc";
            }
            refreshInvoiceViews(invoice, "Hoàn cọc thành công.");
            dispose();
        }
    }

    private final class InvoiceDetailDialog extends BasePaymentDialog {
        private InvoiceDetailDialog(Frame owner, InvoiceRecord invoice) {
            super(owner, "Chi tiết hóa đơn", 680, 500);

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

            addFormRow(form, gbc, 0, "Mã hóa đơn", createValueLabel(invoice.maHoaDon));
            addFormRow(form, gbc, 1, "Hồ sơ lưu trú", createValueLabel(invoice.maHoSo));
            addFormRow(form, gbc, 2, "Khách / Phòng", createValueLabel(invoice.khachHang));
            addFormRow(form, gbc, 3, "Tiền phòng", createValueLabel(invoice.getTienPhongLabel()));
            addFormRow(form, gbc, 4, "Tiền dịch vụ", createValueLabel(invoice.getTienDichVuLabel()));
            addFormRow(form, gbc, 5, "Phụ thu", createValueLabel(invoice.getPhuThuLabel()));
            addFormRow(form, gbc, 6, "Giảm giá", createValueLabel(invoice.getGiamGiaLabel()));
            addFormRow(form, gbc, 7, "Trừ đặt cọc", createValueLabel(invoice.getTienCocLabel()));
            addFormRow(form, gbc, 8, "Tổng thanh toán", createValueLabel(invoice.getTongPhaiThuLabel()));
            addFormRow(form, gbc, 9, "Trạng thái", createValueLabel(invoice.trangThai));
            addFormRow(form, gbc, 10, "Đã hoàn cọc", createValueLabel(invoice.getTienCocDaHoanLabel()));
            addFormRow(form, gbc, 11, "Ghi chú", new JScrollPane(createReadonlyArea(invoice.ghiChu)));

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

    private JTextArea createReadonlyArea(String text) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(text);
        return area;
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

    private static final class InvoiceRecord {
        private String maHoaDon;
        private String maHoSo;
        private String khachHang;
        private String soPhong;
        private String soDienThoai;
        private final List<InvoiceLineRecord> details = new ArrayList<InvoiceLineRecord>();
        private double tienPhong;
        private double tienDichVu;
        private double phuThu;
        private double giamGia;
        private double tienCoc;
        private String phuongThucThanhToan;
        private String trangThai;
        private String ngayHoaDon;
        private String ngayThanhToan;
        private String nguoiThu = "";
        private String soThamChieu = "";
        private double tienThua = 0;
        private String ghiChu = "";
        private double tienCocDaHoan = 0;
        private String hinhThucHoanCoc = "";
        private String thongTinThanhToanKetHop = "";
        private boolean daKhoaDuLieu;

        private static InvoiceRecord createHeader(String maHoaDon, String maHoSo, String khachHang, String soPhong, String soDienThoai,
                                                  String trangThai, String phuongThucThanhToan, String ngayHoaDon) {
            InvoiceRecord invoice = new InvoiceRecord();
            invoice.maHoaDon = maHoaDon;
            invoice.maHoSo = maHoSo;
            invoice.khachHang = khachHang;
            invoice.soPhong = soPhong;
            invoice.soDienThoai = soDienThoai;
            invoice.trangThai = trangThai;
            invoice.phuongThucThanhToan = phuongThucThanhToan;
            invoice.ngayHoaDon = ngayHoaDon;
            invoice.ngayThanhToan = "";
            invoice.daKhoaDuLieu = "Đã thanh toán".equals(trangThai);
            return invoice;
        }

        private void syncSummaryFromDetails() {
            tienPhong = 0;
            tienDichVu = 0;
            phuThu = 0;
            giamGia = 0;
            for (InvoiceLineRecord line : details) {
                if ("TIEN_PHONG".equals(line.loaiDong)) {
                    tienPhong += line.thanhTien;
                } else if ("DICH_VU".equals(line.loaiDong)) {
                    tienDichVu += line.thanhTien;
                } else if ("PHU_THU".equals(line.loaiDong)) {
                    phuThu += line.thanhTien;
                } else if ("GIAM_GIA".equals(line.loaiDong)) {
                    giamGia += Math.abs(line.thanhTien);
                }
            }
        }

        private List<InvoiceLineRecord> getSortedDetails() {
            List<InvoiceLineRecord> lines = new ArrayList<InvoiceLineRecord>(details);
            lines.sort(Comparator.comparingInt(line -> line.thuTuHienThi));
            return lines;
        }

        private void upsertSummaryLine(String loaiDong, String dienGiai, double thanhTien, String ghiChu) {
            details.removeIf(line -> loaiDong.equals(line.loaiDong));
            if (Math.abs(thanhTien) > 0.1d) {
                int nextOrder = 1;
                for (InvoiceLineRecord line : details) {
                    nextOrder = Math.max(nextOrder, line.thuTuHienThi + 1);
                }
                details.add(InvoiceLineRecord.create(
                        maHoaDon + "-" + loaiDong,
                        maHoaDon,
                        loaiDong,
                        dienGiai,
                        1,
                        thanhTien,
                        thanhTien,
                        nextOrder,
                        ghiChu
                ));
            }
            syncSummaryFromDetails();
        }

        private String getRoomLineSummary() {
            return soPhong + " / " + details.size() + " dòng";
        }

        private double getTongPhaiThu() {
            return Math.max(0, tienPhong + tienDichVu + phuThu - giamGia - getTienCocConLai());
        }

        private double getTienCocConLai() {
            return Math.max(0, tienCoc - tienCocDaHoan);
        }

        private String getTienPhongLabel() {
            return String.format(Locale.US, "%,.0f", tienPhong).replace(',', '.');
        }

        private String getTienDichVuLabel() {
            return String.format(Locale.US, "%,.0f", tienDichVu).replace(',', '.');
        }

        private String getPhuThuLabel() {
            return String.format(Locale.US, "%,.0f", phuThu).replace(',', '.');
        }

        private String getGiamGiaLabel() {
            return String.format(Locale.US, "%,.0f", giamGia).replace(',', '.');
        }

        private String getTienCocLabel() {
            return String.format(Locale.US, "%,.0f", tienCoc).replace(',', '.');
        }

        private String getTienCocConLaiLabel() {
            return String.format(Locale.US, "%,.0f", getTienCocConLai()).replace(',', '.');
        }

        private String getTienCocDaHoanLabel() {
            return String.format(Locale.US, "%,.0f", tienCocDaHoan).replace(',', '.');
        }

        private String getTongPhaiThuLabel() {
            return String.format(Locale.US, "%,.0f", getTongPhaiThu()).replace(',', '.');
        }
    }

    private static final class InvoiceLineRecord {
        private String maChiTietHoaDon;
        private String maHoaDon;
        private String loaiDong;
        private String dienGiai;
        private int soLuong;
        private double donGia;
        private double thanhTien;
        private int thuTuHienThi;
        private String ghiChu;

        private static InvoiceLineRecord create(String maChiTietHoaDon, String maHoaDon, String loaiDong, String dienGiai,
                                                int soLuong, double donGia, double thanhTien, int thuTuHienThi, String ghiChu) {
            InvoiceLineRecord line = new InvoiceLineRecord();
            line.maChiTietHoaDon = maChiTietHoaDon;
            line.maHoaDon = maHoaDon;
            line.loaiDong = loaiDong;
            line.dienGiai = dienGiai;
            line.soLuong = soLuong;
            line.donGia = donGia;
            line.thanhTien = thanhTien;
            line.thuTuHienThi = thuTuHienThi;
            line.ghiChu = ghiChu;
            return line;
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
