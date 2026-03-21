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

public class NhanVienGUI extends JFrame {
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
    private final List<EmployeeRecord> allEmployees = new ArrayList<EmployeeRecord>();
    private final List<EmployeeRecord> filteredEmployees = new ArrayList<EmployeeRecord>();

    private JTable tblNhanVien;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboBoPhan;
    private JComboBox<String> cboChucVu;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblMaNhanVien;
    private JLabel lblHoTen;
    private JLabel lblGioiTinh;
    private JLabel lblNgaySinh;
    private JLabel lblSoDienThoai;
    private JLabel lblEmail;
    private JLabel lblBoPhan;
    private JLabel lblChucVu;
    private JLabel lblCaLam;
    private JLabel lblTrangThaiChiTiet;
    private JLabel lblCoTaiKhoan;
    private JTextArea txtDiaChi;
    private JTextArea txtGhiChu;

    public NhanVienGUI() {
        this("guest", "Lễ tân");
    }

    public NhanVienGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý nhân viên - " + AppBranding.APP_DISPLAY_NAME);
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

        root.add(SidebarFactory.createSidebar(this, ScreenKey.NHAN_VIEN, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ NHÂN VIÊN"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý hồ sơ nhân sự nội bộ. Tài khoản đăng nhập chỉ là thông tin bổ sung, không thay thế hồ sơ nhân viên.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Nhân viên"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Thêm NV", new Color(22, 163, 74), Color.WHITE, e -> openCreateEmployeeDialog()));
        card.add(createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openUpdateEmployeeDialog()));
        card.add(createPrimaryButton("Ngừng làm việc", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivateEmployeeDialog()));
        card.add(createPrimaryButton("Cấp tài khoản", new Color(99, 102, 241), Color.WHITE, e -> openCreateEmployeeAccountDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboBoPhan = createComboBox(new String[]{"Tất cả", "Lễ tân", "Buồng phòng", "Kế toán", "Kỹ thuật", "Điều hành"});
        cboChucVu = createComboBox(new String[]{"Tất cả", "Trưởng ca", "Nhân viên", "Kế toán tổng hợp", "Quản lý"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Hoạt động", "Tạm ngừng", "Ngừng làm việc"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(300, 34));
        txtTuKhoa.setToolTipText("Mã nhân viên / tên / số điện thoại / CCCD");

        left.add(createFieldGroup("Bộ phận", cboBoPhan));
        left.add(createFieldGroup("Chức vụ", cboChucVu));
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
        JPanel right = buildDetailCard();

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

        JLabel lblTitle = new JLabel("Danh sách nhân viên");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Double click để xem chi tiết nhân viên.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {"Mã nhân viên", "Họ tên", "SĐT", "Bộ phận", "Chức vụ", "Trạng thái"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblNhanVien = new JTable(tableModel);
        tblNhanVien.setFont(BODY_FONT);
        tblNhanVien.setRowHeight(32);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.setGridColor(BORDER_SOFT);
        tblNhanVien.setShowGrid(true);
        tblNhanVien.setFillsViewportHeight(true);
        tblNhanVien.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblNhanVien.getTableHeader().setBackground(new Color(243, 244, 246));
        tblNhanVien.getTableHeader().setForeground(TEXT_PRIMARY);

        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblNhanVien.getSelectedRow();
                if (row >= 0 && row < filteredEmployees.size()) {
                    updateDetailPanel(filteredEmployees.get(row));
                }
            }
        });
        tblNhanVien.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblNhanVien.getSelectedRow() >= 0) {
                    openEmployeeDetailDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblNhanVien);
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

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết nhân viên");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(10, 2, 10, 8));
        body.setOpaque(false);

        lblMaNhanVien = createValueLabel();
        lblHoTen = createValueLabel();
        lblGioiTinh = createValueLabel();
        lblNgaySinh = createValueLabel();
        lblSoDienThoai = createValueLabel();
        lblEmail = createValueLabel();
        lblBoPhan = createValueLabel();
        lblChucVu = createValueLabel();
        lblCaLam = createValueLabel();
        lblTrangThaiChiTiet = createValueLabel();

        addDetailRow(body, "Mã nhân viên", lblMaNhanVien);
        addDetailRow(body, "Họ tên", lblHoTen);
        addDetailRow(body, "Giới tính", lblGioiTinh);
        addDetailRow(body, "Ngày sinh", lblNgaySinh);
        addDetailRow(body, "SĐT", lblSoDienThoai);
        addDetailRow(body, "Email", lblEmail);
        addDetailRow(body, "Bộ phận", lblBoPhan);
        addDetailRow(body, "Chức vụ", lblChucVu);
        addDetailRow(body, "Ca làm", lblCaLam);
        addDetailRow(body, "Trạng thái", lblTrangThaiChiTiet);

        JPanel lower = new JPanel(new GridLayout(1, 3, 10, 0));
        lower.setOpaque(false);

        txtDiaChi = createReadonlyArea(4);
        txtGhiChu = createReadonlyArea(4);
        lblCoTaiKhoan = createValueLabel();

        lower.add(createAreaCard("Địa chỉ", txtDiaChi));
        lower.add(createAreaCard("Ghi chú", txtGhiChu));
        lower.add(createMiniInfoCard("Có tài khoản", lblCoTaiKhoan));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(lower, BorderLayout.SOUTH);
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

    private JPanel createMiniInfoCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(PANEL_SOFT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(LABEL_FONT);
        lblTitle.setForeground(TEXT_MUTED);
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
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
        allEmployees.clear();
        allEmployees.add(EmployeeRecord.create("NV001", "Nguyễn Hải", "10/10/1992", "Nam", "079100000111", "0901111111",
                "Lễ tân", "Trưởng ca", "Ca sáng", "01/03/2022", "Hoạt động", "hai.nguyen@hotel.com", "Quận 7, TP.HCM",
                "Quản lý khu vực lễ tân buổi sáng.", true));
        allEmployees.add(EmployeeRecord.create("NV002", "Trần Mai", "21/06/1996", "Nữ", "079100000222", "0902222222",
                "Buồng phòng", "Nhân viên", "Ca chiều", "15/08/2023", "Hoạt động", "mai.tran@hotel.com", "Quận 10, TP.HCM",
                "Ưu tiên xử lý phòng VIP.", false));
        allEmployees.add(EmployeeRecord.create("NV003", "Phạm Khôi", "03/01/1989", "Nam", "079100000333", "0903333333",
                "Kế toán", "Kế toán tổng hợp", "Giờ hành chính", "20/02/2021", "Hoạt động", "khoi.pham@hotel.com", "Bình Thạnh, TP.HCM",
                "Theo dõi đối soát doanh thu.", true));
        EmployeeRecord nv4 = EmployeeRecord.create("NV004", "Lê Quỳnh", "15/12/1997", "Nữ", "079100000444", "0904444444",
                "Lễ tân", "Nhân viên", "Ca tối", "09/11/2024", "Tạm ngừng", "quynh.le@hotel.com", "Thủ Đức, TP.HCM",
                "Đang tạm nghỉ theo lịch cá nhân.", true);
        nv4.username = "quynh.le";
        allEmployees.add(nv4);
    }

    private void reloadSampleData(boolean showMessage) {
        cboBoPhan.setSelectedIndex(0);
        cboChucVu.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu nhân viên.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredEmployees.clear();

        String boPhan = valueOf(cboBoPhan.getSelectedItem());
        String chucVu = valueOf(cboChucVu.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (EmployeeRecord employee : allEmployees) {
            if (!"Tất cả".equals(boPhan) && !employee.boPhan.equals(boPhan)) {
                continue;
            }
            if (!"Tất cả".equals(chucVu) && !employee.chucVu.equals(chucVu)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !employee.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (employee.maNhanVien + " " + employee.hoTen + " " + employee.soDienThoai + " " + employee.cccd).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredEmployees.add(employee);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredEmployees.size() + " nhân viên phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (EmployeeRecord employee : filteredEmployees) {
            tableModel.addRow(new Object[]{
                    employee.maNhanVien,
                    employee.hoTen,
                    employee.soDienThoai,
                    employee.boPhan,
                    employee.chucVu,
                    employee.trangThai
            });
        }

        if (!filteredEmployees.isEmpty()) {
            tblNhanVien.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredEmployees.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(EmployeeRecord employee) {
        lblMaNhanVien.setText(employee.maNhanVien);
        lblHoTen.setText(employee.hoTen);
        lblGioiTinh.setText(employee.gioiTinh);
        lblNgaySinh.setText(employee.ngaySinh);
        lblSoDienThoai.setText(employee.soDienThoai);
        lblEmail.setText(employee.email.isEmpty() ? "-" : employee.email);
        txtDiaChi.setText(employee.diaChi);
        txtDiaChi.setCaretPosition(0);
        txtGhiChu.setText(employee.ghiChu);
        txtGhiChu.setCaretPosition(0);
        lblBoPhan.setText(employee.boPhan);
        lblChucVu.setText(employee.chucVu);
        lblCaLam.setText(employee.caLam);
        lblTrangThaiChiTiet.setText(employee.trangThai);
        lblCoTaiKhoan.setText(employee.coTaiKhoan ? "Có" : "Chưa có");
    }

    private void clearDetailPanel() {
        lblMaNhanVien.setText("-");
        lblHoTen.setText("-");
        lblGioiTinh.setText("-");
        lblNgaySinh.setText("-");
        lblSoDienThoai.setText("-");
        lblEmail.setText("-");
        txtDiaChi.setText("Không có dữ liệu phù hợp.");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
        lblBoPhan.setText("-");
        lblChucVu.setText("-");
        lblCaLam.setText("-");
        lblTrangThaiChiTiet.setText("-");
        lblCoTaiKhoan.setText("-");
    }

    private EmployeeRecord getSelectedEmployee() {
        int row = tblNhanVien.getSelectedRow();
        if (row < 0 || row >= filteredEmployees.size()) {
            showWarning("Vui lòng chọn một nhân viên trong danh sách.");
            return null;
        }
        return filteredEmployees.get(row);
    }

    private void openCreateEmployeeDialog() {
        new EmployeeEditorDialog(this, null).setVisible(true);
    }

    private void openUpdateEmployeeDialog() {
        EmployeeRecord employee = getSelectedEmployee();
        if (employee != null) {
            new EmployeeEditorDialog(this, employee).setVisible(true);
        }
    }

    private void openDeactivateEmployeeDialog() {
        EmployeeRecord employee = getSelectedEmployee();
        if (employee != null) {
            new DeactivateEmployeeDialog(this, employee).setVisible(true);
        }
    }

    private void openCreateEmployeeAccountDialog() {
        EmployeeRecord employee = getSelectedEmployee();
        if (employee != null) {
            new CreateEmployeeAccountDialog(this, employee).setVisible(true);
        }
    }

    private void openEmployeeDetailDialog() {
        EmployeeRecord employee = getSelectedEmployee();
        if (employee != null) {
            new EmployeeDetailDialog(this, employee).setVisible(true);
        }
    }

    private void refreshEmployeeViews(EmployeeRecord employee, String message) {
        applyFilters(false);
        selectEmployee(employee);
        showSuccess(message);
    }

    private void selectEmployee(EmployeeRecord employee) {
        if (employee == null) {
            return;
        }
        int index = filteredEmployees.indexOf(employee);
        if (index >= 0) {
            tblNhanVien.setRowSelectionInterval(index, index);
            updateDetailPanel(employee);
        } else if (!filteredEmployees.isEmpty()) {
            tblNhanVien.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredEmployees.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Thêm NV",
                "F2 Cập nhật",
                "F3 Ngừng làm việc",
                "F4 Cấp tài khoản",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "nhanvien-f1", this::openCreateEmployeeDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "nhanvien-f2", this::openUpdateEmployeeDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "nhanvien-f3", this::openDeactivateEmployeeDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "nhanvien-f4", this::openCreateEmployeeAccountDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "nhanvien-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "nhanvien-enter", this::openEmployeeDetailDialog);
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        String trimmed = email.trim();
        return trimmed.contains("@") && trimmed.indexOf('@') > 0 && trimmed.indexOf('@') < trimmed.length() - 1;
    }

    private boolean isIdentityValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        String trimmed = value.trim();
        return trimmed.length() >= 8 && trimmed.length() <= 20;
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

    private abstract class BaseEmployeeDialog extends JDialog {
        protected BaseEmployeeDialog(Frame owner, String title, int width, int height) {
            super(owner, title, true);
            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
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

    private final class EmployeeEditorDialog extends BaseEmployeeDialog {
        private final EmployeeRecord employee;

        private JTextField txtMaNv;
        private JTextField txtHoTen;
        private JTextField txtNgaySinh;
        private JComboBox<String> cboGioiTinh;
        private JTextField txtCccd;
        private JTextField txtSoDienThoaiDialog;
        private JComboBox<String> cboBoPhanDialog;
        private JComboBox<String> cboChucVuDialog;
        private JComboBox<String> cboCaLamDialog;
        private JTextField txtNgayVaoLam;
        private JComboBox<String> cboTrangThaiDialog;
        private JTextField txtEmailDialog;
        private JTextArea txtDiaChiDialog;
        private JTextArea txtGhiChuDialog;

        private EmployeeEditorDialog(Frame owner, EmployeeRecord employee) {
            super(owner, employee == null ? "Thêm nhân viên" : "Cập nhật nhân viên", 780, 760);
            this.employee = employee;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    employee == null ? "THÊM NHÂN VIÊN" : "CẬP NHẬT NHÂN VIÊN",
                    "Quản lý hồ sơ nhân sự nội bộ. Tài khoản đăng nhập nếu có sẽ được cấp riêng và không thay thế hồ sơ nhân viên."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.add(buildBasicSection());
            body.add(Box.createVerticalStrut(10));
            body.add(buildWorkSection());
            body.add(Box.createVerticalStrut(10));
            body.add(buildExtraSection());

            content.add(body, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            if (employee == null) {
                JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit("save"));
                JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit("new"));
                JButton btnSaveAccount = createOutlineButton("Lưu và cấp tài khoản", new Color(99, 102, 241), e -> submit("account"));
                content.add(buildDialogButtons(btnCancel, btnSaveAccount, btnSaveNew, btnSave), BorderLayout.SOUTH);
            } else {
                JButton btnUpdate = createPrimaryButton("Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit("update"));
                content.add(buildDialogButtons(btnCancel, btnUpdate), BorderLayout.SOUTH);
            }
            add(content, BorderLayout.CENTER);
        }

        private JPanel buildBasicSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMaNv = createInputField(employee == null ? "NV" + String.format(Locale.ROOT, "%03d", allEmployees.size() + 1) : employee.maNhanVien);
            txtHoTen = createInputField(employee == null ? "" : employee.hoTen);
            txtNgaySinh = createInputField(employee == null ? "" : employee.ngaySinh);
            cboGioiTinh = createComboBox(new String[]{"Nam", "Nữ", "Khác"});
            txtCccd = createInputField(employee == null ? "" : employee.cccd);
            txtSoDienThoaiDialog = createInputField(employee == null ? "" : employee.soDienThoai);

            if (employee != null) {
                cboGioiTinh.setSelectedItem(employee.gioiTinh);
                txtMaNv.setEditable(false);
            }

            addFormRow(form, gbc, 0, "Mã NV", txtMaNv);
            addFormRow(form, gbc, 1, "Họ tên", txtHoTen);
            addFormRow(form, gbc, 2, "Ngày sinh", txtNgaySinh);
            addFormRow(form, gbc, 3, "Giới tính", cboGioiTinh);
            addFormRow(form, gbc, 4, "CCCD", txtCccd);
            addFormRow(form, gbc, 5, "SĐT", txtSoDienThoaiDialog);

            card.add(new JLabel("Thông tin cơ bản"), BorderLayout.NORTH);
            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildWorkSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboBoPhanDialog = createComboBox(new String[]{"Lễ tân", "Buồng phòng", "Kế toán", "Kỹ thuật", "Điều hành"});
            cboChucVuDialog = createComboBox(new String[]{"Nhân viên", "Trưởng ca", "Kế toán tổng hợp", "Quản lý"});
            cboCaLamDialog = createComboBox(new String[]{"Ca sáng", "Ca chiều", "Ca tối", "Giờ hành chính"});
            txtNgayVaoLam = createInputField(employee == null ? "19/03/2026" : employee.ngayVaoLam);
            cboTrangThaiDialog = createComboBox(new String[]{"Hoạt động", "Tạm ngừng", "Ngừng làm việc"});

            if (employee != null) {
                cboBoPhanDialog.setSelectedItem(employee.boPhan);
                cboChucVuDialog.setSelectedItem(employee.chucVu);
                cboCaLamDialog.setSelectedItem(employee.caLam);
                cboTrangThaiDialog.setSelectedItem(employee.trangThai);
            }

            addFormRow(form, gbc, 0, "Bộ phận", cboBoPhanDialog);
            addFormRow(form, gbc, 1, "Chức vụ", cboChucVuDialog);
            addFormRow(form, gbc, 2, "Ca làm", cboCaLamDialog);
            addFormRow(form, gbc, 3, "Ngày vào làm", txtNgayVaoLam);
            addFormRow(form, gbc, 4, employee == null ? "Trạng thái đầu" : "Trạng thái", cboTrangThaiDialog);

            card.add(new JLabel("Thông tin công việc"), BorderLayout.NORTH);
            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private JPanel buildExtraSection() {
            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtEmailDialog = createInputField(employee == null ? "" : employee.email);
            txtDiaChiDialog = createDialogTextArea(3);
            txtGhiChuDialog = createDialogTextArea(3);

            if (employee != null) {
                txtDiaChiDialog.setText(employee.diaChi);
                txtGhiChuDialog.setText(employee.ghiChu);
            }

            addFormRow(form, gbc, 0, "Email", txtEmailDialog);
            addFormRow(form, gbc, 1, "Địa chỉ", new JScrollPane(txtDiaChiDialog));
            addFormRow(form, gbc, 2, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(new JLabel("Thông tin bổ sung"), BorderLayout.NORTH);
            card.add(form, BorderLayout.CENTER);
            return card;
        }

        private void submit(String action) {
            String maNv = txtMaNv.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            String cccd = txtCccd.getText().trim();
            String soDienThoai = txtSoDienThoaiDialog.getText().trim();

            if (hoTen.isEmpty()) {
                showError("Họ tên bắt buộc nhập.");
                return;
            }
            if (valueOf(cboBoPhanDialog.getSelectedItem()).isEmpty()) {
                showError("Bộ phận bắt buộc chọn.");
                return;
            }
            if (valueOf(cboCaLamDialog.getSelectedItem()).isEmpty()) {
                showError("Ca làm bắt buộc chọn.");
                return;
            }
            if (!isIdentityValid(cccd)) {
                showError("CCCD không hợp lệ.");
                return;
            }
            if (!isIdentityValid(soDienThoai) && !soDienThoai.isEmpty()) {
                showError("SĐT không hợp lệ.");
                return;
            }
            if (txtNgayVaoLam.getText().trim().isEmpty()) {
                showError("Ngày vào làm hợp lệ là bắt buộc.");
                return;
            }
            if (!isEmailValid(txtEmailDialog.getText().trim())) {
                showError("Email không hợp lệ.");
                return;
            }

            for (EmployeeRecord existing : allEmployees) {
                if (existing != employee && existing.maNhanVien.equalsIgnoreCase(maNv)) {
                    showError("Mã NV đã tồn tại.");
                    return;
                }
                if (!cccd.isEmpty() && existing != employee && existing.cccd.equalsIgnoreCase(cccd)) {
                    showError("CCCD đã tồn tại.");
                    return;
                }
                if (!soDienThoai.isEmpty() && existing != employee && existing.soDienThoai.equalsIgnoreCase(soDienThoai)) {
                    showError("SĐT đã tồn tại.");
                    return;
                }
            }

            if (employee == null) {
                EmployeeRecord newEmployee = EmployeeRecord.create(
                        maNv,
                        hoTen,
                        txtNgaySinh.getText().trim(),
                        valueOf(cboGioiTinh.getSelectedItem()),
                        cccd,
                        soDienThoai,
                        valueOf(cboBoPhanDialog.getSelectedItem()),
                        valueOf(cboChucVuDialog.getSelectedItem()),
                        valueOf(cboCaLamDialog.getSelectedItem()),
                        txtNgayVaoLam.getText().trim(),
                        valueOf(cboTrangThaiDialog.getSelectedItem()),
                        txtEmailDialog.getText().trim(),
                        txtDiaChiDialog.getText().trim(),
                        txtGhiChuDialog.getText().trim(),
                        false
                );
                allEmployees.add(0, newEmployee);
                applyFilters(false);
                selectEmployee(newEmployee);
                if ("new".equals(action)) {
                    showSuccess("Thêm nhân viên thành công.");
                    resetCreateForm();
                    return;
                }
                if ("account".equals(action)) {
                    showSuccess("Thêm nhân viên thành công.");
                    dispose();
                    new CreateEmployeeAccountDialog(NhanVienGUI.this, newEmployee).setVisible(true);
                    return;
                }
                showSuccess("Thêm nhân viên thành công.");
                dispose();
                return;
            }

            employee.hoTen = hoTen;
            employee.ngaySinh = txtNgaySinh.getText().trim();
            employee.gioiTinh = valueOf(cboGioiTinh.getSelectedItem());
            employee.cccd = cccd;
            employee.soDienThoai = soDienThoai;
            employee.boPhan = valueOf(cboBoPhanDialog.getSelectedItem());
            employee.chucVu = valueOf(cboChucVuDialog.getSelectedItem());
            employee.caLam = valueOf(cboCaLamDialog.getSelectedItem());
            employee.ngayVaoLam = txtNgayVaoLam.getText().trim();
            employee.trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            employee.email = txtEmailDialog.getText().trim();
            employee.diaChi = txtDiaChiDialog.getText().trim();
            employee.ghiChu = txtGhiChuDialog.getText().trim();
            refreshEmployeeViews(employee, "Cập nhật nhân viên thành công.");
            dispose();
        }

        private void resetCreateForm() {
            txtMaNv.setText("NV" + String.format(Locale.ROOT, "%03d", allEmployees.size() + 1));
            txtHoTen.setText("");
            txtNgaySinh.setText("");
            cboGioiTinh.setSelectedIndex(0);
            txtCccd.setText("");
            txtSoDienThoaiDialog.setText("");
            cboBoPhanDialog.setSelectedIndex(0);
            cboChucVuDialog.setSelectedIndex(0);
            cboCaLamDialog.setSelectedIndex(0);
            txtNgayVaoLam.setText("19/03/2026");
            cboTrangThaiDialog.setSelectedItem("Hoạt động");
            txtEmailDialog.setText("");
            txtDiaChiDialog.setText("");
            txtGhiChuDialog.setText("");
            txtHoTen.requestFocusInWindow();
        }
    }

    private final class DeactivateEmployeeDialog extends BaseEmployeeDialog {
        private final EmployeeRecord employee;
        private JTextField txtTuNgay;
        private JComboBox<String> cboHinhThuc;
        private JTextArea txtLyDo;
        private JTextArea txtGhiChuDialog;

        private DeactivateEmployeeDialog(Frame owner, EmployeeRecord employee) {
            super(owner, "Ngừng làm việc", 620, 500);
            this.employee = employee;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "NGỪNG LÀM VIỆC",
                    "Nhân viên này sẽ không còn được phân công nghiệp vụ mới. Nếu đã có tài khoản, bạn nên khóa tài khoản đăng nhập tương ứng."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTuNgay = createInputField("19/03/2026");
            cboHinhThuc = createComboBox(new String[]{"Nghỉ việc", "Tạm ngừng"});
            txtLyDo = createDialogTextArea(3);
            txtGhiChuDialog = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã NV", createValueLabel(employee.maNhanVien));
            addFormRow(form, gbc, 1, "Họ tên", createValueLabel(employee.hoTen));
            addFormRow(form, gbc, 2, "Bộ phận", createValueLabel(employee.boPhan));
            addFormRow(form, gbc, 3, "Trạng thái hiện tại", createValueLabel(employee.trangThai));
            addFormRow(form, gbc, 4, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 5, "Hình thức", cboHinhThuc);
            addFormRow(form, gbc, 6, "Lý do", new JScrollPane(txtLyDo));
            addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Xác nhận ngừng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do ngừng làm việc.");
                return;
            }
            String message = "Nhân viên này sẽ không còn được dùng cho nghiệp vụ mới. Bạn có muốn tiếp tục không?";
            if (employee.coTaiKhoan) {
                message += " Nhân viên đã có tài khoản, nên khóa tài khoản đăng nhập tương ứng.";
            }
            if (!showConfirmDialog("Xác nhận ngừng làm việc", message, "Đồng ý", new Color(245, 158, 11))) {
                return;
            }
            employee.trangThai = "Ngừng làm việc";
            employee.ghiChu = txtGhiChuDialog.getText().trim().isEmpty() ? txtLyDo.getText().trim() : txtGhiChuDialog.getText().trim();
            refreshEmployeeViews(employee, "Ngừng làm việc thành công.");
            dispose();
        }
    }

    private final class CreateEmployeeAccountDialog extends BaseEmployeeDialog {
        private final EmployeeRecord employee;
        private JTextField txtUsername;
        private JComboBox<String> cboVaiTro;
        private JTextField txtMatKhau;
        private JTextField txtXacNhanMatKhau;
        private JComboBox<String> cboTrangThaiDialog;

        private CreateEmployeeAccountDialog(Frame owner, EmployeeRecord employee) {
            super(owner, "Cấp tài khoản", 560, 440);
            this.employee = employee;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CẤP TÀI KHOẢN",
                    "Tạo tài khoản nhanh cho nhân viên. Việc phân quyền chi tiết vẫn thuộc trang Tài khoản."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtUsername = createInputField(employee.username.isEmpty() ? employee.maNhanVien.toLowerCase(Locale.ROOT) : employee.username);
            cboVaiTro = createComboBox(new String[]{"Lễ tân", "Quản lý", "Kế toán", "Nhân viên vận hành"});
            txtMatKhau = createInputField("");
            txtXacNhanMatKhau = createInputField("");
            cboTrangThaiDialog = createComboBox(new String[]{"Đang hoạt động", "Tạm khóa"});

            addFormRow(form, gbc, 0, "Mã NV", createValueLabel(employee.maNhanVien));
            addFormRow(form, gbc, 1, "Họ tên", createValueLabel(employee.hoTen));
            addFormRow(form, gbc, 2, "Bộ phận", createValueLabel(employee.boPhan));
            addFormRow(form, gbc, 3, "Tên đăng nhập", txtUsername);
            addFormRow(form, gbc, 4, "Vai trò", cboVaiTro);
            addFormRow(form, gbc, 5, "Mật khẩu tạm", txtMatKhau);
            addFormRow(form, gbc, 6, "Xác nhận mật khẩu", txtXacNhanMatKhau);
            addFormRow(form, gbc, 7, "Trạng thái tài khoản", cboTrangThaiDialog);

            JPanel card = createDialogCardPanel();
            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);
            content.add(buildDialogButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Tạo tài khoản", new Color(99, 102, 241), Color.WHITE, e -> submit())
            ), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (employee.coTaiKhoan) {
                showWarning("Nhân viên này đã có tài khoản. Vui lòng quản lý chi tiết ở trang Tài khoản.");
                return;
            }
            String usernameValue = txtUsername.getText().trim();
            if (usernameValue.isEmpty()) {
                showError("Tên đăng nhập không được trống.");
                return;
            }
            for (EmployeeRecord existing : allEmployees) {
                if (existing != employee && existing.username.equalsIgnoreCase(usernameValue)) {
                    showError("Tên đăng nhập không được trùng.");
                    return;
                }
            }
            if (!txtMatKhau.getText().trim().equals(txtXacNhanMatKhau.getText().trim())) {
                showError("Mật khẩu và xác nhận mật khẩu phải khớp.");
                return;
            }
            if (txtMatKhau.getText().trim().isEmpty()) {
                showError("Mật khẩu tạm là bắt buộc.");
                return;
            }
            if (valueOf(cboVaiTro.getSelectedItem()).isEmpty()) {
                showError("Vai trò bắt buộc chọn.");
                return;
            }

            employee.coTaiKhoan = true;
            employee.username = usernameValue;
            employee.vaiTroTaiKhoan = valueOf(cboVaiTro.getSelectedItem());
            refreshEmployeeViews(employee, "Cấp tài khoản thành công.");
            dispose();
        }
    }

    private final class EmployeeDetailDialog extends BaseEmployeeDialog {
        private EmployeeDetailDialog(Frame owner, EmployeeRecord employee) {
            super(owner, "Chi tiết nhân viên", 700, 620);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "CHI TIẾT NHÂN VIÊN",
                    "Thông tin hồ sơ nhân sự ở chế độ chỉ đọc."
            ), BorderLayout.NORTH);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(form, gbc, 0, "Mã NV", createValueLabel(employee.maNhanVien));
            addFormRow(form, gbc, 1, "Họ tên", createValueLabel(employee.hoTen));
            addFormRow(form, gbc, 2, "Ngày sinh", createValueLabel(employee.ngaySinh));
            addFormRow(form, gbc, 3, "Giới tính", createValueLabel(employee.gioiTinh));
            addFormRow(form, gbc, 4, "CCCD", createValueLabel(employee.cccd.isEmpty() ? "-" : employee.cccd));
            addFormRow(form, gbc, 5, "SĐT", createValueLabel(employee.soDienThoai.isEmpty() ? "-" : employee.soDienThoai));
            addFormRow(form, gbc, 6, "Bộ phận", createValueLabel(employee.boPhan));
            addFormRow(form, gbc, 7, "Chức vụ", createValueLabel(employee.chucVu));
            addFormRow(form, gbc, 8, "Ca làm", createValueLabel(employee.caLam));
            addFormRow(form, gbc, 9, "Ngày vào làm", createValueLabel(employee.ngayVaoLam));
            addFormRow(form, gbc, 10, "Trạng thái", createValueLabel(employee.trangThai));
            addFormRow(form, gbc, 11, "Email", createValueLabel(employee.email.isEmpty() ? "-" : employee.email));
            addFormRow(form, gbc, 12, "Địa chỉ", new JScrollPane(readonlyDialogArea(employee.diaChi)));
            addFormRow(form, gbc, 13, "Có tài khoản", createValueLabel(employee.coTaiKhoan ? "Có" : "Chưa có"));
            addFormRow(form, gbc, 14, "Ghi chú", new JScrollPane(readonlyDialogArea(employee.ghiChu)));

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

    private final class ConfirmDialog extends BaseEmployeeDialog {
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

    private final class MessageDialog extends BaseEmployeeDialog {
        private MessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class EmployeeRecord {
        private String maNhanVien;
        private String hoTen;
        private String ngaySinh;
        private String gioiTinh;
        private String cccd;
        private String soDienThoai;
        private String boPhan;
        private String chucVu;
        private String caLam;
        private String ngayVaoLam;
        private String trangThai;
        private String email;
        private String diaChi;
        private String ghiChu;
        private boolean coTaiKhoan;
        private String username = "";
        private String vaiTroTaiKhoan = "";

        private static EmployeeRecord create(String maNhanVien, String hoTen, String ngaySinh, String gioiTinh,
                                             String cccd, String soDienThoai, String boPhan, String chucVu,
                                             String caLam, String ngayVaoLam, String trangThai,
                                             String email, String diaChi, String ghiChu, boolean coTaiKhoan) {
            EmployeeRecord record = new EmployeeRecord();
            record.maNhanVien = maNhanVien;
            record.hoTen = hoTen;
            record.ngaySinh = ngaySinh;
            record.gioiTinh = gioiTinh;
            record.cccd = cccd;
            record.soDienThoai = soDienThoai;
            record.boPhan = boPhan;
            record.chucVu = chucVu;
            record.caLam = caLam;
            record.ngayVaoLam = ngayVaoLam;
            record.trangThai = trangThai;
            record.email = email;
            record.diaChi = diaChi;
            record.ghiChu = ghiChu;
            record.coTaiKhoan = coTaiKhoan;
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