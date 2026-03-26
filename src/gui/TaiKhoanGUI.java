package gui;

import gui.common.AppBranding;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaiKhoanGUI extends JFrame {
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
    private static final String[] ROLE_OPTIONS = {"Lễ tân", "Kế toán", "Quản trị", "Quản lý"};
    private static final String[] STATUS_OPTIONS = {"Hoạt động", "Khóa"};
    private static final String[] EMPLOYEE_OPTIONS = {
            "Nguyễn Hải",
            "Phạm Khôi",
            "Lê My",
            "Trần Anh",
            "Lê Quỳnh",
            "Nguyễn Lan",
            "Đỗ Minh"
    };

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<AccountRecord> allAccounts = new ArrayList<AccountRecord>();
    private final List<AccountRecord> filteredAccounts = new ArrayList<AccountRecord>();

    private JTable tblTaiKhoan;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboVaiTro;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblTenDangNhap;
    private JLabel lblNhanVien;
    private JLabel lblVaiTro;
    private JLabel lblTrangThai;
    private JLabel lblLanDangNhap;
    private JLabel lblEmailKhoiPhuc;
    private JTextArea txtGhiChu;

    private JCheckBox chkDashboard;
    private JCheckBox chkDatPhong;
    private JCheckBox chkCheckInOut;
    private JCheckBox chkThanhToan;
    private JCheckBox chkKhachHang;
    private JCheckBox chkNhanVien;
    private JCheckBox chkBaoCao;
    private JCheckBox chkDanhMuc;

    public TaiKhoanGUI() {
        this("guest", "Lễ tân");
    }

    public TaiKhoanGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý tài khoản - Hotel PMS");
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

        root.add(SidebarFactory.createSidebar(this, ScreenKey.TAI_KHOAN, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ TÀI KHOẢN"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý tài khoản đăng nhập, phân quyền sử dụng và trạng thái truy cập hệ thống.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Tài khoản"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Tạo tài khoản", new Color(22, 163, 74), Color.WHITE, e -> openCreateAccountDialog()));
        card.add(createPrimaryButton("Đặt lại mật khẩu", new Color(37, 99, 235), Color.WHITE, e -> openResetPasswordDialog()));
        card.add(createPrimaryButton("Phân quyền", new Color(59, 130, 246), Color.WHITE, e -> openPermissionDialog()));
        card.add(createPrimaryButton("Khóa tài khoản", new Color(245, 158, 11), TEXT_PRIMARY, e -> openLockAccountDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboVaiTro = createComboBox(new String[]{"Tất cả", "Lễ tân", "Kế toán", "Quản trị", "Quản lý"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Hoạt động", "Khóa"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(320, 34));
        txtTuKhoa.setToolTipText("Tên đăng nhập / nhân viên");

        left.add(createFieldGroup("Vai trò", cboVaiTro));
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

        JLabel lblTitle = new JLabel("Danh sách tài khoản");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chọn một tài khoản để xem chi tiết và quyền chức năng.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "Tên đăng nhập",
                "Nhân viên",
                "Vai trò",
                "Trạng thái",
                "Lần đăng nhập cuối"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTaiKhoan = new JTable(tableModel);
        tblTaiKhoan.setFont(BODY_FONT);
        tblTaiKhoan.setRowHeight(32);
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setGridColor(BORDER_SOFT);
        tblTaiKhoan.setShowGrid(true);
        tblTaiKhoan.setFillsViewportHeight(true);
        tblTaiKhoan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTaiKhoan.getTableHeader().setBackground(new Color(243, 244, 246));
        tblTaiKhoan.getTableHeader().setForeground(TEXT_PRIMARY);

        tblTaiKhoan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblTaiKhoan.getSelectedRow();
                if (row >= 0 && row < filteredAccounts.size()) {
                    updateDetailPanel(filteredAccounts.get(row));
                }
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblTaiKhoan, this::openPermissionDialog);

        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
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
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        JPanel detail = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết tài khoản");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblTenDangNhap = createValueLabel();
        lblNhanVien = createValueLabel();
        lblVaiTro = createValueLabel();
        lblTrangThai = createValueLabel();
        lblLanDangNhap = createValueLabel();
        lblEmailKhoiPhuc = createValueLabel();

        addDetailRow(body, "Tên đăng nhập", lblTenDangNhap);
        addDetailRow(body, "Nhân viên liên kết", lblNhanVien);
        addDetailRow(body, "Vai trò", lblVaiTro);
        addDetailRow(body, "Trạng thái", lblTrangThai);
        addDetailRow(body, "Lần đăng nhập cuối", lblLanDangNhap);
        addDetailRow(body, "Email khôi phục", lblEmailKhoiPhuc);

        txtGhiChu = createReadonlyArea();

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);
        JLabel lblNoteTitle = new JLabel("Ghi chú");
        lblNoteTitle.setFont(LABEL_FONT);
        lblNoteTitle.setForeground(TEXT_MUTED);
        JScrollPane noteScroll = new JScrollPane(txtGhiChu);
        noteScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        notePanel.add(lblNoteTitle, BorderLayout.NORTH);
        notePanel.add(noteScroll, BorderLayout.CENTER);

        detail.add(lblTitle, BorderLayout.NORTH);
        detail.add(body, BorderLayout.CENTER);
        detail.add(notePanel, BorderLayout.SOUTH);

        JPanel permissionCard = createCardPanel(new BorderLayout());
        JLabel lblPermissionTitle = new JLabel("Quyền chức năng");
        lblPermissionTitle.setFont(SECTION_FONT);
        lblPermissionTitle.setForeground(TEXT_PRIMARY);
        lblPermissionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel checks = new JPanel(new GridLayout(4, 2, 10, 8));
        checks.setOpaque(false);

        chkDashboard = createPermissionCheck("Dashboard");
        chkDatPhong = createPermissionCheck("Đặt phòng");
        chkCheckInOut = createPermissionCheck("Check-in/out");
        chkThanhToan = createPermissionCheck("Thanh toán");
        chkKhachHang = createPermissionCheck("Khách hàng");
        chkNhanVien = createPermissionCheck("Nhân viên");
        chkBaoCao = createPermissionCheck("Báo cáo");
        chkDanhMuc = createPermissionCheck("Danh mục hệ thống");

        checks.add(chkDashboard);
        checks.add(chkDatPhong);
        checks.add(chkCheckInOut);
        checks.add(chkThanhToan);
        checks.add(chkKhachHang);
        checks.add(chkNhanVien);
        checks.add(chkBaoCao);
        checks.add(chkDanhMuc);

        permissionCard.add(lblPermissionTitle, BorderLayout.NORTH);
        permissionCard.add(checks, BorderLayout.CENTER);

        wrapper.add(detail, BorderLayout.CENTER);
        wrapper.add(permissionCard, BorderLayout.SOUTH);
        return wrapper;
    }

    private JCheckBox createPermissionCheck(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(BODY_FONT);
        checkBox.setOpaque(false);
        checkBox.setForeground(TEXT_PRIMARY);
        checkBox.setEnabled(false);
        return checkBox;
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
        comboBox.setPreferredSize(new Dimension(180, 34));
        comboBox.setMaximumSize(new Dimension(190, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(240, 34));
        field.setMaximumSize(new Dimension(320, 34));
        return field;
    }

    private JTextArea createReadonlyArea() {
        JTextArea area = new JTextArea(4, 20);
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
        allAccounts.clear();
    }

    private void reloadSampleData(boolean showMessage) {
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu tài khoản.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredAccounts.clear();

        String vaiTro = valueOf(cboVaiTro.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (AccountRecord account : allAccounts) {
            if (!"Tất cả".equals(vaiTro) && !account.vaiTro.equals(vaiTro)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !account.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (account.tenDangNhap + " " + account.nhanVien).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredAccounts.add(account);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredAccounts.size() + " tài khoản phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (AccountRecord account : filteredAccounts) {
            tableModel.addRow(new Object[]{
                    account.tenDangNhap,
                    account.nhanVien,
                    account.vaiTro,
                    account.trangThai,
                    account.lanDangNhapCuoi
            });
        }

        if (!filteredAccounts.isEmpty()) {
            tblTaiKhoan.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredAccounts.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(AccountRecord account) {
        lblTenDangNhap.setText(account.tenDangNhap);
        lblNhanVien.setText(account.nhanVien);
        lblVaiTro.setText(account.vaiTro);
        lblTrangThai.setText(account.trangThai);
        lblLanDangNhap.setText(account.lanDangNhapCuoi);
        lblEmailKhoiPhuc.setText(account.emailKhoiPhuc);
        txtGhiChu.setText(account.ghiChu);
        txtGhiChu.setCaretPosition(0);

        chkDashboard.setSelected(account.permDashboard);
        chkDatPhong.setSelected(account.permDatPhong);
        chkCheckInOut.setSelected(account.permCheckInOut);
        chkThanhToan.setSelected(account.permThanhToan);
        chkKhachHang.setSelected(account.permKhachHang);
        chkNhanVien.setSelected(account.permNhanVien || account.permTaiKhoan);
        chkBaoCao.setSelected(account.permBaoCao);
        chkDanhMuc.setSelected(
                account.permPhong
                        || account.permLoaiPhong
                        || account.permBangGia
                        || account.permDichVu
                        || account.permTienNghi
        );
    }

    private void clearDetailPanel() {
        lblTenDangNhap.setText("-");
        lblNhanVien.setText("-");
        lblVaiTro.setText("-");
        lblTrangThai.setText("-");
        lblLanDangNhap.setText("-");
        lblEmailKhoiPhuc.setText("-");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");

        chkDashboard.setSelected(false);
        chkDatPhong.setSelected(false);
        chkCheckInOut.setSelected(false);
        chkThanhToan.setSelected(false);
        chkKhachHang.setSelected(false);
        chkNhanVien.setSelected(false);
        chkBaoCao.setSelected(false);
        chkDanhMuc.setSelected(false);
    }

    private AccountRecord getSelectedAccount() {
        int row = tblTaiKhoan.getSelectedRow();
        if (row < 0 || row >= filteredAccounts.size()) {
            showWarning("Vui lòng chọn một tài khoản trong danh sách.");
            return null;
        }
        return filteredAccounts.get(row);
    }

    private void openCreateAccountDialog() {
        new CreateAccountDialog(this).setVisible(true);
    }

    private void openResetPasswordDialog() {
        AccountRecord account = getSelectedAccount();
        if (account != null) {
            new ResetPasswordDialog(this, account).setVisible(true);
        }
    }

    private void openPermissionDialog() {
        AccountRecord account = getSelectedAccount();
        if (account != null) {
            new PermissionDialog(this, account).setVisible(true);
        }
    }

    private void openLockAccountDialog() {
        AccountRecord account = getSelectedAccount();
        if (account != null) {
            new LockAccountDialog(this, account).setVisible(true);
        }
    }

    private void addAccount(AccountRecord account, boolean keepDialogOpen) {
        allAccounts.add(0, account);
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        selectAccount(account);
        showSuccess(keepDialogOpen ? "Tạo tài khoản thành công và sẵn sàng nhập tài khoản mới." : "Tạo tài khoản thành công.");
    }

    private void refreshAccountViews(AccountRecord account, String message) {
        applyFilters(false);
        selectAccount(account);
        showSuccess(message);
    }

    private void selectAccount(AccountRecord target) {
        if (target == null) {
            return;
        }

        int index = filteredAccounts.indexOf(target);
        if (index >= 0) {
            tblTaiKhoan.setRowSelectionInterval(index, index);
            updateDetailPanel(target);
        } else if (!filteredAccounts.isEmpty()) {
            tblTaiKhoan.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredAccounts.get(0));
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

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG,
                BORDER_SOFT,
                TEXT_MUTED,
                "F1 Tạo tài khoản",
                "F2 Đặt lại mật khẩu",
                "F3 Phân quyền",
                "F4 Khóa tài khoản",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "taikhoan-f1", this::openCreateAccountDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "taikhoan-f2", this::openResetPasswordDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "taikhoan-f3", this::openPermissionDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "taikhoan-f4", this::openLockAccountDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "taikhoan-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "taikhoan-enter", () -> {
            AccountRecord account = getSelectedAccount();
            if (account != null) {
                showMessageDialog("Chi tiết tài khoản", "Đang xem chi tiết tài khoản " + account.tenDangNhap + ".", new Color(59, 130, 246));
            }
        });
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

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(260, 34));
        return field;
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

    private abstract class BaseAccountDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseAccountDialog(Frame owner, String title, int width, int height) {
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

    private final class CreateAccountDialog extends BaseAccountDialog {
        private final JComboBox<String> cboNhanVien;
        private final JTextField txtTenDangNhapDialog;
        private final JPasswordField txtMatKhauTam;
        private final JPasswordField txtXacNhanMatKhau;
        private final JComboBox<String> cboVaiTroDialog;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JTextField txtEmailKhoiPhucDialog;
        private final JTextArea txtGhiChuDialog;

        private CreateAccountDialog(Frame owner) {
            super(owner, "Tạo tài khoản", 620, 520);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "Tạo tài khoản mới",
                    "Nhập đầy đủ thông tin để tạo tài khoản đăng nhập mới cho nhân viên."
            ), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboNhanVien = createComboBox(EMPLOYEE_OPTIONS);
            txtTenDangNhapDialog = createInputField("");
            txtMatKhauTam = createPasswordField();
            txtXacNhanMatKhau = createPasswordField();
            cboVaiTroDialog = createComboBox(ROLE_OPTIONS);
            cboTrangThaiDialog = createComboBox(STATUS_OPTIONS);
            txtEmailKhoiPhucDialog = createInputField("");
            txtGhiChuDialog = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Nhân viên liên kết", cboNhanVien);
            addFormRow(form, gbc, 1, "Tên đăng nhập", txtTenDangNhapDialog);
            addFormRow(form, gbc, 2, "Mật khẩu tạm", txtMatKhauTam);
            addFormRow(form, gbc, 3, "Xác nhận mật khẩu", txtXacNhanMatKhau);
            addFormRow(form, gbc, 4, "Vai trò", cboVaiTroDialog);
            addFormRow(form, gbc, 5, "Trạng thái", cboTrangThaiDialog);
            addFormRow(form, gbc, 6, "Email khôi phục", txtEmailKhoiPhucDialog);
            addFormRow(form, gbc, 7, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());

            content.add(buildDialogButtons(btnCancel, btnSaveNew, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean keepOpen) {
            String tenDangNhap = txtTenDangNhapDialog.getText().trim();
            String matKhau = new String(txtMatKhauTam.getPassword()).trim();
            String xacNhan = new String(txtXacNhanMatKhau.getPassword()).trim();
            String email = txtEmailKhoiPhucDialog.getText().trim();

            if (tenDangNhap.isEmpty()) {
                showError("Tên đăng nhập không được để trống.");
                txtTenDangNhapDialog.requestFocusInWindow();
                return;
            }
            if (matKhau.isEmpty()) {
                showError("Mật khẩu tạm không được để trống.");
                txtMatKhauTam.requestFocusInWindow();
                return;
            }
            if (!matKhau.equals(xacNhan)) {
                showError("Mật khẩu và xác nhận mật khẩu phải khớp nhau.");
                txtXacNhanMatKhau.requestFocusInWindow();
                return;
            }
            for (AccountRecord account : allAccounts) {
                if (account.tenDangNhap.equalsIgnoreCase(tenDangNhap)) {
                    showError("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");
                    txtTenDangNhapDialog.requestFocusInWindow();
                    return;
                }
            }

            AccountRecord account = new AccountRecord(
                    tenDangNhap,
                    valueOf(cboNhanVien.getSelectedItem()),
                    valueOf(cboVaiTroDialog.getSelectedItem()),
                    valueOf(cboTrangThaiDialog.getSelectedItem()),
                    "Chưa đăng nhập",
                    email.isEmpty() ? tenDangNhap + "@hotel.com" : email,
                    txtGhiChuDialog.getText().trim().isEmpty() ? "Tạo mới từ popup tài khoản." : txtGhiChuDialog.getText().trim(),
                    true,
                    true,
                    true,
                    "Kế toán".equals(valueOf(cboVaiTroDialog.getSelectedItem())) || "Quản trị".equals(valueOf(cboVaiTroDialog.getSelectedItem())) || "Quản lý".equals(valueOf(cboVaiTroDialog.getSelectedItem())),
                    true,
                    "Quản trị".equals(valueOf(cboVaiTroDialog.getSelectedItem())) || "Quản lý".equals(valueOf(cboVaiTroDialog.getSelectedItem())),
                    "Quản trị".equals(valueOf(cboVaiTroDialog.getSelectedItem())) || "Quản lý".equals(valueOf(cboVaiTroDialog.getSelectedItem())),
                    "Quản trị".equals(valueOf(cboVaiTroDialog.getSelectedItem())) || "Quản lý".equals(valueOf(cboVaiTroDialog.getSelectedItem()))
            );
            account.applyRoleDefaults(account.vaiTro);

            addAccount(account, keepOpen);
            if (keepOpen) {
                resetForm();
            } else {
                dispose();
            }
        }

        private void resetForm() {
            cboNhanVien.setSelectedIndex(0);
            txtTenDangNhapDialog.setText("");
            txtMatKhauTam.setText("");
            txtXacNhanMatKhau.setText("");
            cboVaiTroDialog.setSelectedIndex(0);
            cboTrangThaiDialog.setSelectedIndex(0);
            txtEmailKhoiPhucDialog.setText("");
            txtGhiChuDialog.setText("");
            txtTenDangNhapDialog.requestFocusInWindow();
        }
    }

    private final class ResetPasswordDialog extends BaseAccountDialog {
        private final AccountRecord account;
        private final JPasswordField txtMatKhauMoi;
        private final JPasswordField txtXacNhan;
        private final JCheckBox chkBatDoi;
        private final JTextArea txtGhiChuDialog;

        private ResetPasswordDialog(Frame owner, AccountRecord account) {
            super(owner, "Đặt lại mật khẩu", 560, 420);
            this.account = account;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "Đặt lại mật khẩu",
                    "Cập nhật mật khẩu mới cho tài khoản đã chọn và xác nhận trước khi thực hiện."
            ), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMatKhauMoi = createPasswordField();
            txtXacNhan = createPasswordField();
            chkBatDoi = new JCheckBox("Bắt đổi mật khẩu khi đăng nhập lại");
            chkBatDoi.setFont(BODY_FONT);
            chkBatDoi.setOpaque(false);
            chkBatDoi.setSelected(true);
            txtGhiChuDialog = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Tài khoản", createValueTag(account.tenDangNhap));
            addFormRow(form, gbc, 1, "Nhân viên", createValueTag(account.nhanVien));
            addFormRow(form, gbc, 2, "Vai trò", createValueTag(account.vaiTro));
            addFormRow(form, gbc, 3, "Mật khẩu mới", txtMatKhauMoi);
            addFormRow(form, gbc, 4, "Xác nhận mật khẩu", txtXacNhan);
            addFormRow(form, gbc, 5, "Tùy chọn", chkBatDoi);
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            String matKhauMoi = new String(txtMatKhauMoi.getPassword()).trim();
            String xacNhan = new String(txtXacNhan.getPassword()).trim();
            if (matKhauMoi.isEmpty()) {
                showError("Mật khẩu mới không được để trống.");
                return;
            }
            if (!matKhauMoi.equals(xacNhan)) {
                showError("Xác nhận mật khẩu chưa khớp.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận đặt lại mật khẩu",
                    "Bạn có chắc muốn đặt lại mật khẩu cho tài khoản này không?",
                    "Đồng ý",
                    new Color(37, 99, 235)
            )) {
                return;
            }

            StringBuilder note = new StringBuilder("Đã đặt lại mật khẩu");
            if (chkBatDoi.isSelected()) {
                note.append(", yêu cầu đổi mật khẩu ở lần đăng nhập tiếp theo");
            }
            if (!txtGhiChuDialog.getText().trim().isEmpty()) {
                note.append(". ").append(txtGhiChuDialog.getText().trim());
            }
            account.ghiChu = note.toString();
            refreshAccountViews(account, "Đặt lại mật khẩu thành công.");
            dispose();
        }
    }

    private final class PermissionDialog extends BaseAccountDialog {
        private final AccountRecord account;
        private final JComboBox<String> cboVaiTroDialog;
        private final JCheckBox chkDashboardDialog;
        private final JCheckBox chkDatPhongDialog;
        private final JCheckBox chkCheckInOutDialog;
        private final JCheckBox chkThanhToanDialog;
        private final JCheckBox chkKhachHangDialog;
        private final JCheckBox chkPhongDialog;
        private final JCheckBox chkLoaiPhongDialog;
        private final JCheckBox chkBangGiaDialog;
        private final JCheckBox chkDichVuDialog;
        private final JCheckBox chkTienNghiDialog;
        private final JCheckBox chkTaiKhoanDialog;
        private final JCheckBox chkNhanVienDialog;
        private final JCheckBox chkBaoCaoDialog;

        private PermissionDialog(Frame owner, AccountRecord account) {
            super(owner, "Phân quyền tài khoản", 720, 520);
            this.account = account;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "Phân quyền sử dụng",
                    "Thiết lập vai trò và các quyền chức năng cho tài khoản được chọn."
            ), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 12));
            wrapper.setOpaque(false);

            JPanel infoForm = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            cboVaiTroDialog = createComboBox(ROLE_OPTIONS);
            cboVaiTroDialog.setSelectedItem(account.vaiTro);
            cboVaiTroDialog.addActionListener(e -> applyRolePresetIfNeeded());

            addFormRow(infoForm, gbc, 0, "Tài khoản", createValueTag(account.tenDangNhap));
            addFormRow(infoForm, gbc, 1, "Nhân viên", createValueTag(account.nhanVien));
            addFormRow(infoForm, gbc, 2, "Vai trò hiện tại", createValueTag(account.vaiTro));
            addFormRow(infoForm, gbc, 3, "Vai trò áp dụng", cboVaiTroDialog);

            JPanel permissionGrid = new JPanel(new GridLayout(7, 2, 10, 8));
            permissionGrid.setOpaque(false);

            chkDashboardDialog = createEditableCheck("Dashboard", account.permDashboard);
            chkDatPhongDialog = createEditableCheck("Đặt phòng", account.permDatPhong);
            chkCheckInOutDialog = createEditableCheck("Check-in/out", account.permCheckInOut);
            chkThanhToanDialog = createEditableCheck("Thanh toán", account.permThanhToan);
            chkKhachHangDialog = createEditableCheck("Khách hàng", account.permKhachHang);
            chkPhongDialog = createEditableCheck("Phòng", account.permPhong);
            chkLoaiPhongDialog = createEditableCheck("Loại phòng", account.permLoaiPhong);
            chkBangGiaDialog = createEditableCheck("Bảng giá", account.permBangGia);
            chkDichVuDialog = createEditableCheck("Dịch vụ", account.permDichVu);
            chkTienNghiDialog = createEditableCheck("Tiện nghi", account.permTienNghi);
            chkTaiKhoanDialog = createEditableCheck("Tài khoản", account.permTaiKhoan);
            chkNhanVienDialog = createEditableCheck("Nhân viên", account.permNhanVien);
            chkBaoCaoDialog = createEditableCheck("Báo cáo thống kê", account.permBaoCao);

            permissionGrid.add(chkDashboardDialog);
            permissionGrid.add(chkDatPhongDialog);
            permissionGrid.add(chkCheckInOutDialog);
            permissionGrid.add(chkThanhToanDialog);
            permissionGrid.add(chkKhachHangDialog);
            permissionGrid.add(chkPhongDialog);
            permissionGrid.add(chkLoaiPhongDialog);
            permissionGrid.add(chkBangGiaDialog);
            permissionGrid.add(chkDichVuDialog);
            permissionGrid.add(chkTienNghiDialog);
            permissionGrid.add(chkTaiKhoanDialog);
            permissionGrid.add(chkNhanVienDialog);
            permissionGrid.add(chkBaoCaoDialog);

            JPanel permissionCard = new JPanel(new BorderLayout(0, 8));
            permissionCard.setOpaque(false);
            JLabel lblPermission = new JLabel("Quyền chức năng");
            lblPermission.setFont(SECTION_FONT);
            lblPermission.setForeground(TEXT_PRIMARY);
            permissionCard.add(lblPermission, BorderLayout.NORTH);
            permissionCard.add(permissionGrid, BorderLayout.CENTER);

            wrapper.add(infoForm, BorderLayout.NORTH);
            wrapper.add(permissionCard, BorderLayout.CENTER);
            card.add(wrapper, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu quyền", new Color(59, 130, 246), Color.WHITE, e -> submit());
            JButton btnDefault = createOutlineButton("Khôi phục mặc định theo vai trò", new Color(245, 158, 11), e -> applyRoleDefaults(valueOf(cboVaiTroDialog.getSelectedItem())));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnDefault, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private JCheckBox createEditableCheck(String text, boolean selected) {
            JCheckBox checkBox = new JCheckBox(text, selected);
            checkBox.setOpaque(false);
            checkBox.setFont(BODY_FONT);
            checkBox.setForeground(TEXT_PRIMARY);
            return checkBox;
        }

        private void applyRolePresetIfNeeded() {
            String selectedRole = valueOf(cboVaiTroDialog.getSelectedItem());
            if ("Lễ tân".equals(selectedRole)) {
                chkTaiKhoanDialog.setSelected(false);
                chkNhanVienDialog.setSelected(false);
                chkBaoCaoDialog.setSelected(false);
            }
        }

        private void applyRoleDefaults(String selectedRole) {
            AccountRecord snapshot = new AccountRecord(account.tenDangNhap, account.nhanVien, selectedRole, account.trangThai, account.lanDangNhapCuoi, account.emailKhoiPhuc, account.ghiChu, true, true, true, true, true, true, true, true);
            snapshot.applyRoleDefaults(selectedRole);

            chkDashboardDialog.setSelected(snapshot.permDashboard);
            chkDatPhongDialog.setSelected(snapshot.permDatPhong);
            chkCheckInOutDialog.setSelected(snapshot.permCheckInOut);
            chkThanhToanDialog.setSelected(snapshot.permThanhToan);
            chkKhachHangDialog.setSelected(snapshot.permKhachHang);
            chkPhongDialog.setSelected(snapshot.permPhong);
            chkLoaiPhongDialog.setSelected(snapshot.permLoaiPhong);
            chkBangGiaDialog.setSelected(snapshot.permBangGia);
            chkDichVuDialog.setSelected(snapshot.permDichVu);
            chkTienNghiDialog.setSelected(snapshot.permTienNghi);
            chkTaiKhoanDialog.setSelected(snapshot.permTaiKhoan);
            chkNhanVienDialog.setSelected(snapshot.permNhanVien);
            chkBaoCaoDialog.setSelected(snapshot.permBaoCao);
        }

        private void submit() {
            account.vaiTro = valueOf(cboVaiTroDialog.getSelectedItem());
            account.permDashboard = chkDashboardDialog.isSelected();
            account.permDatPhong = chkDatPhongDialog.isSelected();
            account.permCheckInOut = chkCheckInOutDialog.isSelected();
            account.permThanhToan = chkThanhToanDialog.isSelected();
            account.permKhachHang = chkKhachHangDialog.isSelected();
            account.permPhong = chkPhongDialog.isSelected();
            account.permLoaiPhong = chkLoaiPhongDialog.isSelected();
            account.permBangGia = chkBangGiaDialog.isSelected();
            account.permDichVu = chkDichVuDialog.isSelected();
            account.permTienNghi = chkTienNghiDialog.isSelected();
            account.permTaiKhoan = chkTaiKhoanDialog.isSelected();
            account.permNhanVien = chkNhanVienDialog.isSelected();
            account.permBaoCao = chkBaoCaoDialog.isSelected();
            account.ghiChu = "Đã cập nhật phân quyền cho vai trò " + account.vaiTro + ".";

            refreshAccountViews(account, "Cập nhật phân quyền thành công.");
            dispose();
        }
    }

    private final class LockAccountDialog extends BaseAccountDialog {
        private final AccountRecord account;
        private final JTextArea txtLyDo;

        private LockAccountDialog(Frame owner, AccountRecord account) {
            super(owner, "Khóa tài khoản", 560, 380);
            this.account = account;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "Khóa tài khoản",
                    "Nhập lý do khóa tài khoản. Sau khi xác nhận, người dùng sẽ không thể đăng nhập hệ thống."
            ), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtLyDo = createDialogTextArea(5);

            addFormRow(form, gbc, 0, "Tài khoản", createValueTag(account.tenDangNhap));
            addFormRow(form, gbc, 1, "Nhân viên", createValueTag(account.nhanVien));
            addFormRow(form, gbc, 2, "Vai trò", createValueTag(account.vaiTro));
            addFormRow(form, gbc, 3, "Trạng thái hiện tại", createValueTag(account.trangThai));
            addFormRow(form, gbc, 4, "Lý do khóa", new JScrollPane(txtLyDo));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận khóa", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            String lyDo = txtLyDo.getText().trim();
            if (lyDo.isEmpty()) {
                showError("Vui lòng nhập lý do khóa tài khoản.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận khóa tài khoản",
                    "Tài khoản sẽ bị khóa và không thể đăng nhập hệ thống. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(245, 158, 11)
            )) {
                return;
            }

            account.trangThai = "Khóa";
            account.ghiChu = "Lý do khóa: " + lyDo;
            refreshAccountViews(account, "Khóa tài khoản thành công.");
            dispose();
        }
    }

    private JLabel createValueTag(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private final class ConfirmDialog extends BaseAccountDialog {
        private boolean confirmed;

        private ConfirmDialog(Frame owner, String title, String message, String confirmText, Color confirmColor) {
            super(owner, title, 430, 220);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose());
            JButton btnConfirm = createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> {
                confirmed = true;
                dispose();
            });
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() {
            return confirmed;
        }
    }

    private final class AppMessageDialog extends BaseAccountDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);

            JButton btnClose = createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose());
            content.add(buildDialogButtons(btnClose), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class AccountRecord {
        private String tenDangNhap;
        private String nhanVien;
        private String vaiTro;
        private String trangThai;
        private String lanDangNhapCuoi;
        private String emailKhoiPhuc;
        private String ghiChu;
        private boolean permDashboard;
        private boolean permDatPhong;
        private boolean permCheckInOut;
        private boolean permThanhToan;
        private boolean permKhachHang;
        private boolean permPhong;
        private boolean permLoaiPhong;
        private boolean permBangGia;
        private boolean permDichVu;
        private boolean permTienNghi;
        private boolean permTaiKhoan;
        private boolean permNhanVien;
        private boolean permBaoCao;

        private AccountRecord(String tenDangNhap, String nhanVien, String vaiTro, String trangThai, String lanDangNhapCuoi,
                              String emailKhoiPhuc, String ghiChu, boolean dashboard, boolean datPhong, boolean checkInOut,
                              boolean thanhToan, boolean khachHang, boolean nhanVienQuyen, boolean baoCao, boolean danhMucHeThong) {
            this.tenDangNhap = tenDangNhap;
            this.nhanVien = nhanVien;
            this.vaiTro = vaiTro;
            this.trangThai = trangThai;
            this.lanDangNhapCuoi = lanDangNhapCuoi;
            this.emailKhoiPhuc = emailKhoiPhuc;
            this.ghiChu = ghiChu;
            this.permDashboard = dashboard;
            this.permDatPhong = datPhong;
            this.permCheckInOut = checkInOut;
            this.permThanhToan = thanhToan;
            this.permKhachHang = khachHang;
            this.permPhong = danhMucHeThong;
            this.permLoaiPhong = danhMucHeThong;
            this.permBangGia = danhMucHeThong;
            this.permDichVu = danhMucHeThong;
            this.permTienNghi = danhMucHeThong;
            this.permTaiKhoan = nhanVienQuyen;
            this.permNhanVien = nhanVienQuyen;
            this.permBaoCao = baoCao;
        }

        private void applyRoleDefaults(String selectedRole) {
            permDashboard = true;
            permDatPhong = true;
            permCheckInOut = true;
            permKhachHang = true;

            if ("Lễ tân".equals(selectedRole)) {
                permThanhToan = false;
                permPhong = false;
                permLoaiPhong = false;
                permBangGia = false;
                permDichVu = false;
                permTienNghi = false;
                permTaiKhoan = false;
                permNhanVien = false;
                permBaoCao = false;
                return;
            }

            if ("Kế toán".equals(selectedRole)) {
                permThanhToan = true;
                permPhong = false;
                permLoaiPhong = false;
                permBangGia = true;
                permDichVu = false;
                permTienNghi = false;
                permTaiKhoan = false;
                permNhanVien = false;
                permBaoCao = true;
                return;
            }

            if ("Quản lý".equals(selectedRole)) {
                permThanhToan = true;
                permPhong = true;
                permLoaiPhong = true;
                permBangGia = true;
                permDichVu = true;
                permTienNghi = true;
                permTaiKhoan = true;
                permNhanVien = true;
                permBaoCao = true;
                return;
            }

            permThanhToan = true;
            permPhong = true;
            permLoaiPhong = true;
            permBangGia = true;
            permDichVu = true;
            permTienNghi = true;
            permTaiKhoan = true;
            permNhanVien = true;
            permBaoCao = true;
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
