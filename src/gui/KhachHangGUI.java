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
import javax.swing.JList;
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KhachHangGUI extends JFrame {
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
    private final List<CustomerRecord> allCustomers = new ArrayList<CustomerRecord>();
    private final List<CustomerRecord> filteredCustomers = new ArrayList<CustomerRecord>();

    private JTable tblKhachHang;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboLoaiKhach;
    private JComboBox<String> cboHangKhach;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblMaKhachHang;
    private JLabel lblHoTen;
    private JLabel lblGioiTinh;
    private JLabel lblNgaySinh;
    private JLabel lblSoDienThoai;
    private JLabel lblCccd;
    private JLabel lblEmail;
    private JLabel lblHangKhach;
    private JLabel lblLoaiKhach;
    private JLabel lblTrangThai;
    private JTextArea txtDiaChi;
    private JTextArea txtGhiChu;
    private JList<String> lstLichSu;

    public KhachHangGUI() {
        this("guest", "Lễ tân");
    }

    public KhachHangGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý khách hàng - " + AppBranding.APP_DISPLAY_NAME);
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

        root.add(SidebarFactory.createSidebar(this, ScreenKey.KHACH_HANG, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ KHÁCH HÀNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý hồ sơ khách để phục vụ đặt phòng, check-in, tra cứu lịch sử lưu trú và nhận diện khách quen hoặc khách VIP.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Khách hàng"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Thêm KH", new Color(22, 163, 74), Color.WHITE, e -> openCreateCustomerDialog()));
        card.add(createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openUpdateCustomerDialog()));
        card.add(createPrimaryButton("Ngừng giao dịch", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivateCustomerDialog()));
        card.add(createPrimaryButton("Xem lịch sử", new Color(99, 102, 241), Color.WHITE, e -> openCustomerHistoryDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboLoaiKhach = createComboBox(new String[]{"Tất cả", "Cá nhân", "Doanh nghiệp"});
        cboHangKhach = createComboBox(new String[]{"Tất cả", "Đồng", "Bạc", "Vàng", "Kim cương"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Hoạt động", "Ngừng giao dịch"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(320, 34));
        txtTuKhoa.setToolTipText("Mã khách hàng / tên / số điện thoại / CCCD");

        left.add(createFieldGroup("Loại khách", cboLoaiKhach));
        left.add(createFieldGroup("Hạng khách", cboHangKhach));
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

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách khách hàng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Double click để xem chi tiết khách hàng.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {"Mã khách hàng", "Họ tên", "Số điện thoại", "CCCD/Passport", "Hạng KH", "Trạng thái"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKhachHang = new JTable(tableModel);
        tblKhachHang.setFont(BODY_FONT);
        tblKhachHang.setRowHeight(32);
        tblKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKhachHang.setGridColor(BORDER_SOFT);
        tblKhachHang.setShowGrid(true);
        tblKhachHang.setFillsViewportHeight(true);
        tblKhachHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblKhachHang.getTableHeader().setBackground(new Color(243, 244, 246));
        tblKhachHang.getTableHeader().setForeground(TEXT_PRIMARY);

        tblKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblKhachHang.getSelectedRow();
                if (row >= 0 && row < filteredCustomers.size()) {
                    updateDetailPanel(filteredCustomers.get(row));
                }
            }
        });
        tblKhachHang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblKhachHang.getSelectedRow() >= 0) {
                    openCustomerDetailDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblKhachHang);
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
        wrapper.add(buildHistoryCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết khách hàng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(10, 2, 10, 8));
        body.setOpaque(false);

        lblMaKhachHang = createValueLabel();
        lblHoTen = createValueLabel();
        lblLoaiKhach = createValueLabel();
        lblGioiTinh = createValueLabel();
        lblNgaySinh = createValueLabel();
        lblSoDienThoai = createValueLabel();
        lblCccd = createValueLabel();
        lblEmail = createValueLabel();
        lblHangKhach = createValueLabel();
        lblTrangThai = createValueLabel();

        addDetailRow(body, "Mã khách hàng", lblMaKhachHang);
        addDetailRow(body, "Họ tên", lblHoTen);
        addDetailRow(body, "Loại khách", lblLoaiKhach);
        addDetailRow(body, "Giới tính", lblGioiTinh);
        addDetailRow(body, "Ngày sinh", lblNgaySinh);
        addDetailRow(body, "Số điện thoại", lblSoDienThoai);
        addDetailRow(body, "CCCD/Passport", lblCccd);
        addDetailRow(body, "Email", lblEmail);
        addDetailRow(body, "Hạng thành viên", lblHangKhach);
        addDetailRow(body, "Trạng thái", lblTrangThai);

        JPanel lower = new JPanel(new GridLayout(1, 2, 10, 0));
        lower.setOpaque(false);

        txtDiaChi = createReadonlyArea(4);
        txtGhiChu = createReadonlyArea(4);

        lower.add(createAreaCard("Địa chỉ", txtDiaChi));
        lower.add(createAreaCard("Ghi chú", txtGhiChu));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(lower, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Lịch sử lưu trú gần đây");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        lstLichSu = new JList<String>(new String[]{"Không có lịch sử lưu trú gần đây"});
        lstLichSu.setFont(BODY_FONT);

        JScrollPane scrollPane = new JScrollPane(lstLichSu);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
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
        field.setPreferredSize(new Dimension(220, 34));
        field.setMaximumSize(new Dimension(340, 34));
        return field;
    }

    private JTextArea createReadonlyArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
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
        allCustomers.clear();

        CustomerRecord kh1 = CustomerRecord.create("KH001", "Cá nhân", "Nguyễn Lan", "0901001001", "079200000111",
                "12/07/1995", "Nữ", "lan.nguyen@email.com", "Quận 1, TP.HCM", "Việt Nam", "Vàng", "Hoạt động", username,
                "Khách quay lại nhiều lần, ưu tiên phòng tầng cao.");
        kh1.lichSuLuuTru.add(StayHistoryRecord.create("02/03/2026", "03/03/2026", "P203", 1, 1650000, "Đã hoàn tất"));
        kh1.lichSuLuuTru.add(StayHistoryRecord.create("12/02/2026", "14/02/2026", "P502", 2, 5400000, "Đã hoàn tất"));
        allCustomers.add(kh1);

        CustomerRecord kh2 = CustomerRecord.create("KH002", "Cá nhân", "Trần Phú", "0902002002", "079200000222",
                "23/03/1993", "Nam", "phu.tran@email.com", "Quận 3, TP.HCM", "Việt Nam", "Bạc", "Hoạt động", username,
                "Ưu tiên phòng yên tĩnh, gần thang máy.");
        kh2.lichSuLuuTru.add(StayHistoryRecord.create("20/01/2026", "21/01/2026", "P104", 1, 980000, "Đã hoàn tất"));
        kh2.lichSuLuuTru.add(StayHistoryRecord.create("15/12/2025", "16/12/2025", "P203", 1, 1560000, "Đã hoàn tất"));
        allCustomers.add(kh2);

        CustomerRecord kh3 = CustomerRecord.create("KH003", "Doanh nghiệp", "Lê Mỹ", "0903003003", "079200000333",
                "05/11/1988", "Nữ", "my.le@email.com", "Hà Nội", "Việt Nam", "Kim cương", "Hoạt động", username,
                "Khách VIP doanh nghiệp, thường đặt nhiều phòng.");
        kh3.lichSuLuuTru.add(StayHistoryRecord.create("05/01/2026", "07/01/2026", "P305", 2, 4200000, "Đã hoàn tất"));
        kh3.lichSuLuuTru.add(StayHistoryRecord.create("28/11/2025", "30/11/2025", "P502", 2, 6100000, "Đã hoàn tất"));
        kh3.coBookingDangMo = true;
        allCustomers.add(kh3);

        CustomerRecord kh4 = CustomerRecord.create("KH004", "Cá nhân", "Phạm Hưng", "0904004004", "079200000444",
                "18/01/1990", "Nam", "hung.pham@email.com", "Đà Nẵng", "Việt Nam", "Đồng", "Ngừng giao dịch", username,
                "Từng hủy booking sát giờ.");
        kh4.lichSuLuuTru.add(StayHistoryRecord.create("10/10/2025", "11/10/2025", "P101", 1, 850000, "Đã hoàn tất"));
        allCustomers.add(kh4);
    }

    private void reloadSampleData(boolean showMessage) {
        cboLoaiKhach.setSelectedIndex(0);
        cboHangKhach.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu khách hàng.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredCustomers.clear();

        String loaiKhach = valueOf(cboLoaiKhach.getSelectedItem());
        String hangKhach = valueOf(cboHangKhach.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (CustomerRecord customer : allCustomers) {
            if (!"Tất cả".equals(loaiKhach) && !customer.loaiKhach.equals(loaiKhach)) {
                continue;
            }
            if (!"Tất cả".equals(hangKhach) && !customer.hangThanhVien.equals(hangKhach)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !customer.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (customer.maKhachHang + " " + customer.hoTen + " " + customer.soDienThoai + " " + customer.cccdPassport).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredCustomers.add(customer);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredCustomers.size() + " khách hàng phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (CustomerRecord customer : filteredCustomers) {
            tableModel.addRow(new Object[]{
                    customer.maKhachHang,
                    customer.hoTen,
                    customer.soDienThoai,
                    customer.cccdPassport,
                    customer.hangThanhVien,
                    customer.trangThai
            });
        }

        if (!filteredCustomers.isEmpty()) {
            tblKhachHang.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredCustomers.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(CustomerRecord customer) {
        lblMaKhachHang.setText(customer.maKhachHang);
        lblHoTen.setText(customer.hoTen);
        lblLoaiKhach.setText(customer.loaiKhach);
        lblGioiTinh.setText(customer.gioiTinh);
        lblNgaySinh.setText(customer.ngaySinh);
        lblSoDienThoai.setText(customer.soDienThoai);
        lblCccd.setText(customer.cccdPassport);
        lblEmail.setText(customer.email.isEmpty() ? "-" : customer.email);
        lblHangKhach.setText(customer.hangThanhVien);
        lblTrangThai.setText(customer.trangThai);
        txtDiaChi.setText(customer.diaChi);
        txtDiaChi.setCaretPosition(0);
        txtGhiChu.setText(customer.ghiChu);
        txtGhiChu.setCaretPosition(0);
        lstLichSu.setListData(buildHistoryPreview(customer));
    }

    private String[] buildHistoryPreview(CustomerRecord customer) {
        if (customer.lichSuLuuTru.isEmpty()) {
            return new String[]{"Không có lịch sử lưu trú gần đây"};
        }
        List<String> items = new ArrayList<String>();
        for (StayHistoryRecord history : customer.lichSuLuuTru) {
            items.add(history.phong + " - " + history.ngayDen + " - " + history.ngayDi);
        }
        return items.toArray(new String[0]);
    }

    private void clearDetailPanel() {
        lblMaKhachHang.setText("-");
        lblHoTen.setText("-");
        lblLoaiKhach.setText("-");
        lblGioiTinh.setText("-");
        lblNgaySinh.setText("-");
        lblSoDienThoai.setText("-");
        lblCccd.setText("-");
        lblEmail.setText("-");
        lblHangKhach.setText("-");
        lblTrangThai.setText("-");
        txtDiaChi.setText("Không có dữ liệu phù hợp.");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
        lstLichSu.setListData(new String[]{"Không có lịch sử lưu trú gần đây"});
    }

    private CustomerRecord getSelectedCustomer() {
        int row = tblKhachHang.getSelectedRow();
        if (row < 0 || row >= filteredCustomers.size()) {
            showWarning("Vui lòng chọn một khách hàng trong danh sách.");
            return null;
        }
        return filteredCustomers.get(row);
    }

    private void openCreateCustomerDialog() {
        new CustomerEditorDialog(this, null).setVisible(true);
    }

    private void openUpdateCustomerDialog() {
        CustomerRecord customer = getSelectedCustomer();
        if (customer != null) {
            new CustomerEditorDialog(this, customer).setVisible(true);
        }
    }

    private void openDeactivateCustomerDialog() {
        CustomerRecord customer = getSelectedCustomer();
        if (customer != null) {
            new DeactivateCustomerDialog(this, customer).setVisible(true);
        }
    }

    private void openCustomerHistoryDialog() {
        CustomerRecord customer = getSelectedCustomer();
        if (customer != null) {
            new CustomerHistoryDialog(this, customer).setVisible(true);
        }
    }

    private void openCustomerDetailDialog() {
        CustomerRecord customer = getSelectedCustomer();
        if (customer != null) {
            new CustomerDetailDialog(this, customer).setVisible(true);
        }
    }

    private void refreshCustomerViews(CustomerRecord customer, String message) {
        applyFilters(false);
        selectCustomer(customer);
        showSuccess(message);
    }

    private void selectCustomer(CustomerRecord customer) {
        if (customer == null) {
            return;
        }
        int index = filteredCustomers.indexOf(customer);
        if (index >= 0) {
            tblKhachHang.setRowSelectionInterval(index, index);
            updateDetailPanel(customer);
        } else if (!filteredCustomers.isEmpty()) {
            tblKhachHang.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredCustomers.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Thêm KH",
                "F2 Cập nhật",
                "F3 Ngừng giao dịch",
                "F4 Xem lịch sử",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "khachhang-f1", this::openCreateCustomerDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "khachhang-f2", this::openUpdateCustomerDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "khachhang-f3", this::openDeactivateCustomerDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "khachhang-f4", this::openCustomerHistoryDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "khachhang-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "khachhang-enter", this::openCustomerDetailDialog);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
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

    private boolean isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        String trimmed = email.trim();
        return trimmed.contains("@") && trimmed.indexOf('@') > 0 && trimmed.indexOf('@') < trimmed.length() - 1;
    }

    private boolean isPassportValueValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        String trimmed = value.trim();
        return trimmed.length() >= 8 && trimmed.length() <= 20;
    }

    private double formatRevenue(List<StayHistoryRecord> histories) {
        double sum = 0;
        for (StayHistoryRecord history : histories) {
            sum += history.tongTien;
        }
        return sum;
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
        MessageDialog dialog = new MessageDialog(this, title, message, accentColor);
        dialog.setVisible(true);
    }

    private boolean showConfirmDialog(String title, String message, String confirmText, Color confirmColor) {
        ConfirmDialog dialog = new ConfirmDialog(this, title, message, confirmText, confirmColor);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private abstract class BaseCustomerDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseCustomerDialog(Frame owner, String title, int width, int height) {
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

    private final class CustomerEditorDialog extends BaseCustomerDialog {
        private final CustomerRecord customer;

        private JTextField txtMaKh;
        private JComboBox<String> cboLoaiKh;
        private JTextField txtHoTen;
        private JTextField txtSoDienThoaiDialog;
        private JTextField txtCccdDialog;
        private JTextField txtNgaySinhDialog;
        private JComboBox<String> cboGioiTinh;
        private JTextField txtEmailDialog;
        private JTextArea txtDiaChiDialog;
        private JTextField txtQuocTich;
        private JComboBox<String> cboHangThanhVienDialog;
        private JTextField txtNguoiTao;
        private JComboBox<String> cboTrangThaiDialog;
        private JTextArea txtGhiChuDialog;

        private CustomerEditorDialog(Frame owner, CustomerRecord customer) {
            super(owner, customer == null ? "Thêm khách hàng" : "Cập nhật khách hàng", 760, 700);
            this.customer = customer;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    customer == null ? "THÊM KHÁCH HÀNG" : "CẬP NHẬT KHÁCH HÀNG",
                    "Quản lý hồ sơ khách để phục vụ đặt phòng, check-in và tra cứu lịch sử lưu trú."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMaKh = createInputField(customer == null ? "KH" + String.format(Locale.ROOT, "%03d", allCustomers.size() + 1) : customer.maKhachHang);
            cboLoaiKh = createComboBox(new String[]{"Cá nhân", "Doanh nghiệp"});
            txtHoTen = createInputField(customer == null ? "" : customer.hoTen);
            txtSoDienThoaiDialog = createInputField(customer == null ? "" : customer.soDienThoai);
            txtCccdDialog = createInputField(customer == null ? "" : customer.cccdPassport);
            txtNgaySinhDialog = createInputField(customer == null ? "" : customer.ngaySinh);
            cboGioiTinh = createComboBox(new String[]{"Nam", "Nữ", "Khác"});
            txtEmailDialog = createInputField(customer == null ? "" : customer.email);
            txtDiaChiDialog = createDialogTextArea(3);
            txtQuocTich = createInputField(customer == null ? "Việt Nam" : customer.quocTich);
            cboHangThanhVienDialog = createComboBox(new String[]{"Đồng", "Bạc", "Vàng", "Kim cương"});
            txtNguoiTao = createInputField(customer == null ? username : customer.nguoiTao);
            cboTrangThaiDialog = createComboBox(new String[]{"Hoạt động", "Ngừng giao dịch"});
            txtGhiChuDialog = createDialogTextArea(3);

            if (customer != null) {
                cboLoaiKh.setSelectedItem(customer.loaiKhach);
                cboGioiTinh.setSelectedItem(customer.gioiTinh);
                cboHangThanhVienDialog.setSelectedItem(customer.hangThanhVien);
                cboTrangThaiDialog.setSelectedItem(customer.trangThai);
                txtDiaChiDialog.setText(customer.diaChi);
                txtGhiChuDialog.setText(customer.ghiChu);
                txtMaKh.setEditable(false);
            }

            addFormRow(form, gbc, 0, "Mã KH", txtMaKh);
            addFormRow(form, gbc, 1, "Loại KH", cboLoaiKh);
            addFormRow(form, gbc, 2, "Họ tên", txtHoTen);
            addFormRow(form, gbc, 3, "SĐT", txtSoDienThoaiDialog);
            addFormRow(form, gbc, 4, "CCCD/Passport", txtCccdDialog);
            addFormRow(form, gbc, 5, "Ngày sinh", txtNgaySinhDialog);
            addFormRow(form, gbc, 6, "Giới tính", cboGioiTinh);
            addFormRow(form, gbc, 7, "Email", txtEmailDialog);
            addFormRow(form, gbc, 8, "Địa chỉ", new JScrollPane(txtDiaChiDialog));
            addFormRow(form, gbc, 9, "Quốc tịch", txtQuocTich);
            addFormRow(form, gbc, 10, "Hạng thành viên", cboHangThanhVienDialog);
            if (customer == null) {
                addFormRow(form, gbc, 11, "Người tạo", txtNguoiTao);
                addFormRow(form, gbc, 12, "Ghi chú", new JScrollPane(txtGhiChuDialog));
            } else {
                addFormRow(form, gbc, 11, "Trạng thái", cboTrangThaiDialog);
                addFormRow(form, gbc, 12, "Ghi chú", new JScrollPane(txtGhiChuDialog));
            }

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            if (customer == null) {
                JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit("save"));
                JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit("new"));
                JButton btnSaveBooking = createOutlineButton("Lưu và tạo booking", new Color(99, 102, 241), e -> submit("booking"));
                content.add(buildDialogButtons(btnCancel, btnSaveBooking, btnSaveNew, btnSave), BorderLayout.SOUTH);
            } else {
                JButton btnUpdate = createPrimaryButton("Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit("update"));
                content.add(buildDialogButtons(btnCancel, btnUpdate), BorderLayout.SOUTH);
            }
            add(content, BorderLayout.CENTER);
        }

        private void submit(String action) {
            String maKh = txtMaKh.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            String soDienThoai = txtSoDienThoaiDialog.getText().trim();
            String cccdPassport = txtCccdDialog.getText().trim();
            String email = txtEmailDialog.getText().trim();

            if (hoTen.isEmpty()) {
                showError("Họ tên bắt buộc nhập.");
                return;
            }
            if (soDienThoai.isEmpty()) {
                showError("Số điện thoại bắt buộc nhập.");
                return;
            }
            if (!isPassportValueValid(cccdPassport)) {
                showError("CCCD/Passport không hợp lệ.");
                return;
            }
            if (!isEmailValid(email)) {
                showError("Email không hợp lệ.");
                return;
            }
            for (CustomerRecord existing : allCustomers) {
                if (existing != customer && existing.soDienThoai.equalsIgnoreCase(soDienThoai)) {
                    showError("Số điện thoại đã tồn tại trong danh sách.");
                    return;
                }
                if (!cccdPassport.isEmpty() && existing != customer && existing.cccdPassport.equalsIgnoreCase(cccdPassport)) {
                    showError("CCCD/Passport đã tồn tại trong danh sách.");
                    return;
                }
                if (existing != customer && existing.maKhachHang.equalsIgnoreCase(maKh)) {
                    showError("Mã KH đã tồn tại trong danh sách.");
                    return;
                }
            }

            if (customer == null) {
                CustomerRecord newCustomer = CustomerRecord.create(
                        maKh,
                        valueOf(cboLoaiKh.getSelectedItem()),
                        hoTen,
                        soDienThoai,
                        cccdPassport,
                        txtNgaySinhDialog.getText().trim(),
                        valueOf(cboGioiTinh.getSelectedItem()),
                        email,
                        txtDiaChiDialog.getText().trim(),
                        txtQuocTich.getText().trim(),
                        valueOf(cboHangThanhVienDialog.getSelectedItem()),
                        "Hoạt động",
                        txtNguoiTao.getText().trim(),
                        txtGhiChuDialog.getText().trim()
                );
                allCustomers.add(0, newCustomer);
                applyFilters(false);
                selectCustomer(newCustomer);
                if ("new".equals(action)) {
                    showSuccess("Thêm khách hàng thành công.");
                    resetCreateForm();
                    return;
                }
                if ("booking".equals(action)) {
                    showSuccess("Thêm khách hàng thành công và sẵn sàng chuyển sang luồng đặt phòng.");
                } else {
                    showSuccess("Thêm khách hàng thành công.");
                }
                dispose();
                return;
            }

            customer.loaiKhach = valueOf(cboLoaiKh.getSelectedItem());
            customer.hoTen = hoTen;
            customer.soDienThoai = soDienThoai;
            customer.cccdPassport = cccdPassport;
            customer.ngaySinh = txtNgaySinhDialog.getText().trim();
            customer.gioiTinh = valueOf(cboGioiTinh.getSelectedItem());
            customer.email = email;
            customer.diaChi = txtDiaChiDialog.getText().trim();
            customer.quocTich = txtQuocTich.getText().trim();
            customer.hangThanhVien = valueOf(cboHangThanhVienDialog.getSelectedItem());
            customer.trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            customer.ghiChu = txtGhiChuDialog.getText().trim();
            refreshCustomerViews(customer, "Cập nhật khách hàng thành công.");
            dispose();
        }

        private void resetCreateForm() {
            txtMaKh.setText("KH" + String.format(Locale.ROOT, "%03d", allCustomers.size() + 1));
            cboLoaiKh.setSelectedIndex(0);
            txtHoTen.setText("");
            txtSoDienThoaiDialog.setText("");
            txtCccdDialog.setText("");
            txtNgaySinhDialog.setText("");
            cboGioiTinh.setSelectedIndex(0);
            txtEmailDialog.setText("");
            txtDiaChiDialog.setText("");
            txtQuocTich.setText("Việt Nam");
            cboHangThanhVienDialog.setSelectedIndex(0);
            txtGhiChuDialog.setText("");
            txtHoTen.requestFocusInWindow();
        }
    }

    private final class DeactivateCustomerDialog extends BaseCustomerDialog {
        private final CustomerRecord customer;
        private JTextField txtTuNgay;
        private JTextArea txtLyDo;
        private JTextArea txtGhiChuDialog;

        private DeactivateCustomerDialog(Frame owner, CustomerRecord customer) {
            super(owner, "Ngừng giao dịch khách hàng", 600, 460);
            this.customer = customer;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "NGỪNG GIAO DỊCH KHÁCH HÀNG",
                    "Khách hàng này sẽ không được dùng cho booking mới. Lịch sử lưu trú và giao dịch cũ vẫn được giữ nguyên."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTuNgay = createInputField("19/03/2026");
            txtLyDo = createDialogTextArea(3);
            txtGhiChuDialog = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã KH", createValueLabel(customer.maKhachHang));
            addFormRow(form, gbc, 1, "Tên KH", createValueLabel(customer.hoTen));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueLabel(customer.trangThai));
            addFormRow(form, gbc, 3, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 4, "Lý do", new JScrollPane(txtLyDo));
            addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xác nhận ngừng GD", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do ngừng giao dịch.");
                return;
            }
            String warning = "Khách hàng này sẽ không được dùng cho giao dịch mới. Bạn có muốn tiếp tục không?";
            if (customer.coBookingDangMo) {
                warning += " Khách hiện có booking đang mở, cần kiểm tra luồng nghiệp vụ liên quan.";
            }
            if (!showConfirmDialog("Xác nhận ngừng giao dịch", warning, "Đồng ý", new Color(245, 158, 11))) {
                return;
            }
            customer.trangThai = "Ngừng giao dịch";
            customer.ghiChu = txtGhiChuDialog.getText().trim().isEmpty() ? txtLyDo.getText().trim() : txtGhiChuDialog.getText().trim();
            refreshCustomerViews(customer, "Ngừng giao dịch thành công.");
            dispose();
        }
    }

    private final class CustomerHistoryDialog extends BaseCustomerDialog {
        private final CustomerRecord customer;
        private JTable tblHistory;

        private CustomerHistoryDialog(Frame owner, CustomerRecord customer) {
            super(owner, "Lịch sử lưu trú", 820, 560);
            this.customer = customer;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "LỊCH SỬ LƯU TRÚ",
                    "Tra cứu lịch sử lưu trú để nhận diện khách quen, khách VIP và tần suất sử dụng dịch vụ."
            ), BorderLayout.NORTH);

            JPanel topCard = createDialogCardPanel();
            JPanel topForm = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(topForm, gbc, 0, "Mã KH", createValueLabel(customer.maKhachHang));
            addFormRow(topForm, gbc, 1, "Tên KH", createValueLabel(customer.hoTen));
            addFormRow(topForm, gbc, 2, "Hạng thành viên", createValueLabel(customer.hangThanhVien));
            topCard.add(topForm, BorderLayout.CENTER);

            String[] columns = {"STT", "Ngày đến", "Ngày đi", "Phòng", "Số đêm", "Tổng tiền", "Trạng thái"};
            DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblHistory = new JTable(historyModel);
            tblHistory.setFont(BODY_FONT);
            tblHistory.setRowHeight(30);
            tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

            int index = 1;
            for (StayHistoryRecord history : customer.lichSuLuuTru) {
                historyModel.addRow(new Object[]{
                        index++,
                        history.ngayDen,
                        history.ngayDi,
                        history.phong,
                        history.soDem,
                        formatMoney(history.tongTien),
                        history.trangThai
                });
            }

            JPanel tableCard = createDialogCardPanel();
            tableCard.add(new JScrollPane(tblHistory), BorderLayout.CENTER);

            JPanel summary = createDialogCardPanel();
            JPanel summaryForm = createDialogFormPanel();
            GridBagConstraints summaryGbc = new GridBagConstraints();
            summaryGbc.insets = new Insets(6, 0, 6, 12);
            summaryGbc.anchor = GridBagConstraints.WEST;
            addFormRow(summaryForm, summaryGbc, 0, "Tổng số lần lưu trú", createValueLabel(String.valueOf(customer.lichSuLuuTru.size())));
            addFormRow(summaryForm, summaryGbc, 1, "Tổng doanh thu", createValueLabel(formatMoney(formatRevenue(customer.lichSuLuuTru))));
            summary.add(summaryForm, BorderLayout.CENTER);

            JPanel center = new JPanel(new BorderLayout(0, 10));
            center.setOpaque(false);
            center.add(topCard, BorderLayout.NORTH);
            center.add(tableCard, BorderLayout.CENTER);
            center.add(summary, BorderLayout.SOUTH);
            content.add(center, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Đóng", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xem chi tiết lần ở", new Color(99, 102, 241), Color.WHITE, e -> showSuccess("Đang mở chi tiết lần ở của khách hàng " + customer.maKhachHang + "."))
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private final class CustomerDetailDialog extends BaseCustomerDialog {
        private CustomerDetailDialog(Frame owner, CustomerRecord customer) {
            super(owner, "Chi tiết khách hàng", 700, 620);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHI TIẾT KHÁCH HÀNG",
                    "Thông tin hồ sơ khách hàng ở chế độ chỉ đọc."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã KH", createValueLabel(customer.maKhachHang));
            addFormRow(form, gbc, 1, "Loại KH", createValueLabel(customer.loaiKhach));
            addFormRow(form, gbc, 2, "Họ tên", createValueLabel(customer.hoTen));
            addFormRow(form, gbc, 3, "SĐT", createValueLabel(customer.soDienThoai));
            addFormRow(form, gbc, 4, "CCCD/Passport", createValueLabel(customer.cccdPassport.isEmpty() ? "-" : customer.cccdPassport));
            addFormRow(form, gbc, 5, "Ngày sinh", createValueLabel(customer.ngaySinh.isEmpty() ? "-" : customer.ngaySinh));
            addFormRow(form, gbc, 6, "Giới tính", createValueLabel(customer.gioiTinh));
            addFormRow(form, gbc, 7, "Email", createValueLabel(customer.email.isEmpty() ? "-" : customer.email));
            addFormRow(form, gbc, 8, "Địa chỉ", new JScrollPane(readonlyDialogArea(customer.diaChi)));
            addFormRow(form, gbc, 9, "Quốc tịch", createValueLabel(customer.quocTich));
            addFormRow(form, gbc, 10, "Hạng thành viên", createValueLabel(customer.hangThanhVien));
            addFormRow(form, gbc, 11, "Trạng thái", createValueLabel(customer.trangThai));
            addFormRow(form, gbc, 12, "Ghi chú", new JScrollPane(readonlyDialogArea(customer.ghiChu)));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JTextArea readonlyDialogArea(String value) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(value);
        return area;
    }

    private final class ConfirmDialog extends BaseCustomerDialog {
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

    private final class MessageDialog extends BaseCustomerDialog {
        private MessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class CustomerRecord {
        private String maKhachHang;
        private String loaiKhach;
        private String hoTen;
        private String soDienThoai;
        private String cccdPassport;
        private String ngaySinh;
        private String gioiTinh;
        private String email;
        private String diaChi;
        private String quocTich;
        private String hangThanhVien;
        private String trangThai;
        private String nguoiTao;
        private String ghiChu;
        private boolean coBookingDangMo;
        private final List<StayHistoryRecord> lichSuLuuTru = new ArrayList<StayHistoryRecord>();

        private static CustomerRecord create(String maKhachHang, String loaiKhach, String hoTen, String soDienThoai,
                                             String cccdPassport, String ngaySinh, String gioiTinh, String email,
                                             String diaChi, String quocTich, String hangThanhVien, String trangThai,
                                             String nguoiTao, String ghiChu) {
            CustomerRecord record = new CustomerRecord();
            record.maKhachHang = maKhachHang;
            record.loaiKhach = loaiKhach;
            record.hoTen = hoTen;
            record.soDienThoai = soDienThoai;
            record.cccdPassport = cccdPassport;
            record.ngaySinh = ngaySinh;
            record.gioiTinh = gioiTinh;
            record.email = email;
            record.diaChi = diaChi;
            record.quocTich = quocTich;
            record.hangThanhVien = hangThanhVien;
            record.trangThai = trangThai;
            record.nguoiTao = nguoiTao;
            record.ghiChu = ghiChu;
            return record;
        }
    }

    private static final class StayHistoryRecord {
        private String ngayDen;
        private String ngayDi;
        private String phong;
        private int soDem;
        private double tongTien;
        private String trangThai;

        private static StayHistoryRecord create(String ngayDen, String ngayDi, String phong, int soDem, double tongTien, String trangThai) {
            StayHistoryRecord record = new StayHistoryRecord();
            record.ngayDen = ngayDen;
            record.ngayDi = ngayDi;
            record.phong = phong;
            record.soDem = soDem;
            record.tongTien = tongTien;
            record.trangThai = trangThai;
            return record;
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
