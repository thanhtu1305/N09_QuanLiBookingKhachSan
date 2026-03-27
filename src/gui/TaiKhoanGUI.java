package gui;

import dao.TaiKhoanDAO;
import dao.NhanVienDAO;
import db.ConnectDB;
import entity.TaiKhoan;
import entity.NhanVien;
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
import javax.swing.SwingUtilities;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<AccountRecord> allAccounts = new ArrayList<>();
    private final List<AccountRecord> filteredAccounts = new ArrayList<>();

    // DAO
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

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

        // Lễ tân: không khởi tạo UI — buildPanel() sẽ hiện dialog thông báo
        if (isLetanRole()) return;

        setTitle("Hotel PMS - Tài khoản");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        loadFromDatabase();
        initUI();
        reloadData(false);
        registerShortcuts();
    }

    /** Trả về true nếu vai trò hiện tại là Lễ tân (không có quyền truy cập). */
    private boolean isLetanRole() {
        return "Lễ tân".equalsIgnoreCase(this.role);
    }

    /**
     * Hiện dialog thông báo từ chối quyền — KHÔNG swap màn hình.
     * Gọi từ buildPanel() khi là Lễ tân.
     */
    private void showAccessDeniedDialog() {
        javax.swing.JOptionPane.showMessageDialog(
                null,
                "<html><b>Lễ tân không được sử dụng quyền này.</b><br><br>"
                        + "Chức năng <b>Quản lý Tài khoản</b> chỉ dành cho Quản lý.<br>"
                        + "Vui lòng liên hệ Quản lý để được hỗ trợ.</html>",
                "Không có quyền truy cập",
                javax.swing.JOptionPane.WARNING_MESSAGE
        );
    }

    // =========================================================================
    // LOAD DỮ LIỆU TỪ DATABASE
    // =========================================================================
    private void loadFromDatabase() {
        allAccounts.clear();
        try {
            // Câu truy vấn JOIN TaiKhoan với NhanVien để lấy tên nhân viên
            String sql = "SELECT tk.maTaiKhoan, tk.maNhanVien, tk.tenDangNhap, tk.matKhau, " +
                    "       tk.vaiTro, tk.trangThai, tk.lanDangNhapCuoi, tk.emailKhoiPhuc, " +
                    "       nv.hoTen " +
                    "FROM TaiKhoan tk " +
                    "LEFT JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien " +
                    "ORDER BY tk.maTaiKhoan";

            Connection con = ConnectDB.getConnection();
            if (con == null) {
                System.err.println("[TaiKhoanGUI] Không thể kết nối CSDL.");
                return;
            }
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int maTK = rs.getInt("maTaiKhoan");
                String tenDN = rs.getString("tenDangNhap");
                String hoTen = rs.getString("hoTen");
                if (hoTen == null) hoTen = "Không xác định";
                String vaiTro = rs.getString("vaiTro");
                String trangThai = rs.getString("trangThai");
                String lanDN = rs.getTimestamp("lanDangNhapCuoi") != null
                        ? rs.getTimestamp("lanDangNhapCuoi").toString().substring(0, 16)
                        : "Chưa đăng nhập";
                String email = safeValue(rs.getString("emailKhoiPhuc"), "");

                // Tải quyền từ bảng TaiKhoanQuyen (nếu có), nếu không dùng defaults theo vai trò
                AccountRecord record = new AccountRecord(
                        maTK, tenDN, hoTen, vaiTro, trangThai, lanDN, email, ""
                );
                record.applyRoleDefaults(vaiTro);

                // Nạp quyền tuỳ chỉnh từ DB nếu bảng TaiKhoanQuyen tồn tại
                loadCustomPermissions(record, con);

                allAccounts.add(record);
            }
        } catch (Exception e) {
            System.err.println("[TaiKhoanGUI] Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tải quyền tuỳ chỉnh từ bảng TaiKhoanQuyen.
     * Nếu bảng chưa tồn tại → bỏ qua (dùng mặc định theo vai trò).
     * Schema gợi ý:
     *   CREATE TABLE TaiKhoanQuyen (
     *       maTaiKhoan INT PRIMARY KEY,
     *       permDashboard BIT, permDatPhong BIT, permCheckInOut BIT,
     *       permThanhToan BIT, permKhachHang BIT, permPhong BIT,
     *       permLoaiPhong BIT, permBangGia BIT, permDichVu BIT,
     *       permTienNghi BIT, permTaiKhoan BIT, permNhanVien BIT, permBaoCao BIT,
     *       FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(maTaiKhoan)
     *   );
     */
    private void loadCustomPermissions(AccountRecord record, Connection con) {
        try {
            String sql = "SELECT * FROM TaiKhoanQuyen WHERE maTaiKhoan = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, record.maTaiKhoan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                record.permDashboard  = rs.getBoolean("permDashboard");
                record.permDatPhong   = rs.getBoolean("permDatPhong");
                record.permCheckInOut = rs.getBoolean("permCheckInOut");
                record.permThanhToan  = rs.getBoolean("permThanhToan");
                record.permKhachHang  = rs.getBoolean("permKhachHang");
                record.permPhong      = rs.getBoolean("permPhong");
                record.permLoaiPhong  = rs.getBoolean("permLoaiPhong");
                record.permBangGia    = rs.getBoolean("permBangGia");
                record.permDichVu     = rs.getBoolean("permDichVu");
                record.permTienNghi   = rs.getBoolean("permTienNghi");
                record.permTaiKhoan   = rs.getBoolean("permTaiKhoan");
                record.permNhanVien   = rs.getBoolean("permNhanVien");
                record.permBaoCao     = rs.getBoolean("permBaoCao");
            }
        } catch (Exception ignored) {
            // Bảng TaiKhoanQuyen chưa tồn tại → dùng mặc định theo vai trò
        }
    }

    /**
     * Lưu quyền tuỳ chỉnh của một tài khoản vào bảng TaiKhoanQuyen.
     * Tự động tạo bảng nếu chưa có.
     */
    private void savePermissionsToDatabase(AccountRecord record) {
        try {
            Connection con = ConnectDB.getConnection();
            if (con == null) return;

            // Tự tạo bảng nếu chưa tồn tại
            String createSql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='TaiKhoanQuyen' AND xtype='U') " +
                    "CREATE TABLE TaiKhoanQuyen (" +
                    "  maTaiKhoan INT PRIMARY KEY," +
                    "  permDashboard BIT, permDatPhong BIT, permCheckInOut BIT," +
                    "  permThanhToan BIT, permKhachHang BIT, permPhong BIT," +
                    "  permLoaiPhong BIT, permBangGia BIT, permDichVu BIT," +
                    "  permTienNghi BIT, permTaiKhoan BIT, permNhanVien BIT, permBaoCao BIT," +
                    "  FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(maTaiKhoan)" +
                    ")";
            con.prepareStatement(createSql).executeUpdate();

            // UPSERT: nếu đã có thì UPDATE, chưa có thì INSERT
            String upsertSql = "MERGE TaiKhoanQuyen AS target " +
                    "USING (SELECT ? AS maTaiKhoan) AS src ON target.maTaiKhoan = src.maTaiKhoan " +
                    "WHEN MATCHED THEN UPDATE SET " +
                    "  permDashboard=?, permDatPhong=?, permCheckInOut=?, permThanhToan=?," +
                    "  permKhachHang=?, permPhong=?, permLoaiPhong=?, permBangGia=?," +
                    "  permDichVu=?, permTienNghi=?, permTaiKhoan=?, permNhanVien=?, permBaoCao=? " +
                    "WHEN NOT MATCHED THEN INSERT (maTaiKhoan, permDashboard, permDatPhong, permCheckInOut," +
                    "  permThanhToan, permKhachHang, permPhong, permLoaiPhong, permBangGia," +
                    "  permDichVu, permTienNghi, permTaiKhoan, permNhanVien, permBaoCao) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            PreparedStatement ps = con.prepareStatement(upsertSql);
            // WHEN MATCHED params
            ps.setInt(1, record.maTaiKhoan);
            ps.setBoolean(2, record.permDashboard);
            ps.setBoolean(3, record.permDatPhong);
            ps.setBoolean(4, record.permCheckInOut);
            ps.setBoolean(5, record.permThanhToan);
            ps.setBoolean(6, record.permKhachHang);
            ps.setBoolean(7, record.permPhong);
            ps.setBoolean(8, record.permLoaiPhong);
            ps.setBoolean(9, record.permBangGia);
            ps.setBoolean(10, record.permDichVu);
            ps.setBoolean(11, record.permTienNghi);
            ps.setBoolean(12, record.permTaiKhoan);
            ps.setBoolean(13, record.permNhanVien);
            ps.setBoolean(14, record.permBaoCao);
            // WHEN NOT MATCHED params
            ps.setInt(15, record.maTaiKhoan);
            ps.setBoolean(16, record.permDashboard);
            ps.setBoolean(17, record.permDatPhong);
            ps.setBoolean(18, record.permCheckInOut);
            ps.setBoolean(19, record.permThanhToan);
            ps.setBoolean(20, record.permKhachHang);
            ps.setBoolean(21, record.permPhong);
            ps.setBoolean(22, record.permLoaiPhong);
            ps.setBoolean(23, record.permBangGia);
            ps.setBoolean(24, record.permDichVu);
            ps.setBoolean(25, record.permTienNghi);
            ps.setBoolean(26, record.permTaiKhoan);
            ps.setBoolean(27, record.permNhanVien);
            ps.setBoolean(28, record.permBaoCao);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("[TaiKhoanGUI] Lỗi lưu quyền: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách nhân viên từ DB để dùng trong ComboBox
     */
    private String[] loadEmployeeOptions() {
        List<String> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            if (con == null) return new String[]{"Không có dữ liệu"};
            PreparedStatement ps = con.prepareStatement(
                    "SELECT maNhanVien, hoTen FROM NhanVien WHERE trangThai = N'Hoạt động' ORDER BY hoTen"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("maNhanVien") + " - " + rs.getString("hoTen"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.isEmpty() ? new String[]{"Không có dữ liệu"} : list.toArray(new String[0]);
    }

    // =========================================================================
    // INIT UI
    // =========================================================================
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
        card.add(createPrimaryButton("Khóa / Mở tài khoản", new Color(245, 158, 11), TEXT_PRIMARY, e -> openToggleAccountDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadData(true)));
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

        String[] columns = {"Tên đăng nhập", "Nhân viên", "Vai trò", "Trạng thái", "Lần đăng nhập cuối"};

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

    // =========================================================================
    // DATA RELOAD & FILTER
    // =========================================================================
    private void reloadData(boolean showMessage) {
        loadFromDatabase();
        cboVaiTro.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã tải lại dữ liệu từ cơ sở dữ liệu (" + allAccounts.size() + " tài khoản).");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredAccounts.clear();

        String vaiTro = valueOf(cboVaiTro.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (AccountRecord account : allAccounts) {
            if (!"Tất cả".equals(vaiTro) && !account.vaiTro.equals(vaiTro)) continue;
            if (!"Tất cả".equals(trangThai) && !account.trangThai.equals(trangThai)) continue;
            if (!tuKhoa.isEmpty()) {
                String source = (account.tenDangNhap + " " + account.nhanVien).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) continue;
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
        txtGhiChu.setText(account.ghiChu.isEmpty() ? "Không có ghi chú." : account.ghiChu);
        txtGhiChu.setCaretPosition(0);

        chkDashboard.setSelected(account.permDashboard);
        chkDatPhong.setSelected(account.permDatPhong);
        chkCheckInOut.setSelected(account.permCheckInOut);
        chkThanhToan.setSelected(account.permThanhToan);
        chkKhachHang.setSelected(account.permKhachHang);
        chkNhanVien.setSelected(account.permNhanVien || account.permTaiKhoan);
        chkBaoCao.setSelected(account.permBaoCao);
        chkDanhMuc.setSelected(
                account.permPhong || account.permLoaiPhong ||
                        account.permBangGia || account.permDichVu || account.permTienNghi
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

    // =========================================================================
    // ACTION HANDLERS
    // =========================================================================
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
        if (account != null) new ResetPasswordDialog(this, account).setVisible(true);
    }

    private void openPermissionDialog() {
        AccountRecord account = getSelectedAccount();
        if (account != null) new PermissionDialog(this, account).setVisible(true);
    }

    private void openToggleAccountDialog() {
        AccountRecord account = getSelectedAccount();
        if (account != null) new ToggleAccountDialog(this, account).setVisible(true);
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
        if (target == null) return;
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

    // =========================================================================
    // UI HELPERS
    // =========================================================================
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
        JComboBox<String> comboBox = new JComboBox<>(values);
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
        new AppMessageDialog(this, title, message, accentColor).setVisible(true);
    }

    private boolean showConfirmDialog(String title, String message, String confirmText, Color confirmColor) {
        ConfirmDialog dialog = new ConfirmDialog(this, title, message, confirmText, confirmColor);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG, BORDER_SOFT, TEXT_MUTED,
                "F1 Tạo tài khoản", "F2 Đặt lại mật khẩu",
                "F3 Phân quyền", "F4 Khóa / Mở tài khoản",
                "F5 Làm mới", "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "taikhoan-f1", this::openCreateAccountDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "taikhoan-f2", this::openResetPasswordDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "taikhoan-f3", this::openPermissionDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "taikhoan-f4", this::openToggleAccountDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "taikhoan-f5", () -> reloadData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "taikhoan-enter", () -> {
            AccountRecord account = getSelectedAccount();
            if (account != null) {
                showMessageDialog("Chi tiết tài khoản",
                        "Đang xem chi tiết tài khoản " + account.tenDangNhap + ".",
                        new Color(59, 130, 246));
            }
        });
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    // =========================================================================
    // DIALOG HELPERS
    // =========================================================================
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

    // =========================================================================
    // BASE DIALOG
    // =========================================================================
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
            if (visible) ScreenUIHelper.prepareDialog(this, getOwner(), minimumWidth, minimumHeight);
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
            for (JButton button : buttons) panel.add(button);
            return panel;
        }
    }

    // =========================================================================
    // DIALOG: TẠO TÀI KHOẢN — ghi vào DB
    // =========================================================================
    private final class CreateAccountDialog extends BaseAccountDialog {
        private JComboBox<String> cboNhanVien;
        private final JTextField txtTenDangNhapDialog;
        private final JPasswordField txtMatKhauTam;
        private final JPasswordField txtXacNhanMatKhau;
        private final JComboBox<String> cboVaiTroDialog;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JTextField txtEmailKhoiPhucDialog;
        private final JTextArea txtGhiChuDialog;

        // Lưu mapping "hiển thị → maNhanVien"
        private final List<Integer> maNhanVienList = new ArrayList<>();

        private CreateAccountDialog(Frame owner) {
            super(owner, "Tạo tài khoản", 640, 540);

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

            // Load nhân viên từ DB
            cboNhanVien = buildNhanVienCombo();
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

        private JComboBox<String> buildNhanVienCombo() {
            maNhanVienList.clear();
            List<String> labels = new ArrayList<>();
            try {
                Connection con = ConnectDB.getConnection();
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT maNhanVien, hoTen FROM NhanVien WHERE trangThai = N'Hoạt động' ORDER BY hoTen"
                    );
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        maNhanVienList.add(rs.getInt("maNhanVien"));
                        labels.add(rs.getString("hoTen"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (labels.isEmpty()) {
                maNhanVienList.add(0);
                labels.add("Không có nhân viên");
            }
            return createComboBox(labels.toArray(new String[0]));
        }

        private void submit(boolean keepOpen) {
            String tenDangNhap = txtTenDangNhapDialog.getText().trim();
            String matKhau = new String(txtMatKhauTam.getPassword()).trim();
            String xacNhan = new String(txtXacNhanMatKhau.getPassword()).trim();
            String email = txtEmailKhoiPhucDialog.getText().trim();
            String vaiTro = valueOf(cboVaiTroDialog.getSelectedItem());
            String trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            int idx = cboNhanVien.getSelectedIndex();
            int maNV = (idx >= 0 && idx < maNhanVienList.size()) ? maNhanVienList.get(idx) : 0;

            if (tenDangNhap.isEmpty()) { showError("Tên đăng nhập không được để trống."); return; }
            if (matKhau.isEmpty())     { showError("Mật khẩu tạm không được để trống."); return; }
            if (!matKhau.equals(xacNhan)) { showError("Mật khẩu và xác nhận mật khẩu phải khớp nhau."); return; }

            // Kiểm tra trùng tên đăng nhập trong DB
            for (AccountRecord ac : allAccounts) {
                if (ac.tenDangNhap.equalsIgnoreCase(tenDangNhap)) {
                    showError("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.");
                    return;
                }
            }

            // Ghi vào DB
            TaiKhoan tk = new TaiKhoan();
            tk.setMaNhanVien(maNV);
            tk.setTenDangNhap(tenDangNhap);
            tk.setMatKhau(matKhau);
            tk.setVaiTro(vaiTro);
            tk.setTrangThai(trangThai);
            tk.setEmailKhoiPhuc(email.isEmpty() ? tenDangNhap + "@hotel.com" : email);
            tk.setLanDangNhapCuoi(null);

            boolean ok = taiKhoanDAO.insert(tk);
            if (!ok) {
                showError("Lỗi khi lưu tài khoản vào cơ sở dữ liệu.");
                return;
            }

            // Tải lại để lấy maTaiKhoan mới từ DB
            loadFromDatabase();
            // Tìm record vừa thêm
            AccountRecord newRecord = null;
            for (AccountRecord ac : allAccounts) {
                if (ac.tenDangNhap.equalsIgnoreCase(tenDangNhap)) { newRecord = ac; break; }
            }
            if (newRecord != null) {
                newRecord.applyRoleDefaults(vaiTro);
                savePermissionsToDatabase(newRecord);
            }

            // Refresh UI
            applyFilters(false);
            if (newRecord != null) selectAccount(newRecord);
            showSuccess(keepOpen ? "Tạo tài khoản thành công và sẵn sàng nhập tài khoản mới." : "Tạo tài khoản thành công.");
            if (keepOpen) resetForm(); else dispose();
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

    // =========================================================================
    // DIALOG: ĐẶT LẠI MẬT KHẨU — cập nhật DB
    // =========================================================================
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
            if (matKhauMoi.isEmpty()) { showError("Mật khẩu mới không được để trống."); return; }
            if (!matKhauMoi.equals(xacNhan)) { showError("Xác nhận mật khẩu chưa khớp."); return; }
            if (!showConfirmDialog("Xác nhận đặt lại mật khẩu",
                    "Bạn có chắc muốn đặt lại mật khẩu cho tài khoản này không?",
                    "Đồng ý", new Color(37, 99, 235))) return;

            // Cập nhật DB
            try {
                Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?"
                );
                ps.setString(1, matKhauMoi);
                ps.setInt(2, account.maTaiKhoan);
                ps.executeUpdate();
            } catch (Exception e) {
                showError("Lỗi cập nhật mật khẩu: " + e.getMessage());
                return;
            }

            StringBuilder note = new StringBuilder("Đã đặt lại mật khẩu");
            if (chkBatDoi.isSelected()) note.append(", yêu cầu đổi mật khẩu ở lần đăng nhập tiếp theo");
            if (!txtGhiChuDialog.getText().trim().isEmpty())
                note.append(". ").append(txtGhiChuDialog.getText().trim());
            account.ghiChu = note.toString();
            refreshAccountViews(account, "Đặt lại mật khẩu thành công.");
            dispose();
        }
    }

    // =========================================================================
    // DIALOG: PHÂN QUYỀN — lưu xuống bảng TaiKhoanQuyen
    // =========================================================================
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
            super(owner, "Phân quyền tài khoản", 720, 560);
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

            chkDashboardDialog  = createEditableCheck("Dashboard",         account.permDashboard);
            chkDatPhongDialog   = createEditableCheck("Đặt phòng",         account.permDatPhong);
            chkCheckInOutDialog = createEditableCheck("Check-in/out",      account.permCheckInOut);
            chkThanhToanDialog  = createEditableCheck("Thanh toán",        account.permThanhToan);
            chkKhachHangDialog  = createEditableCheck("Khách hàng",        account.permKhachHang);
            chkPhongDialog      = createEditableCheck("Phòng",             account.permPhong);
            chkLoaiPhongDialog  = createEditableCheck("Loại phòng",        account.permLoaiPhong);
            chkBangGiaDialog    = createEditableCheck("Bảng giá",          account.permBangGia);
            chkDichVuDialog     = createEditableCheck("Dịch vụ",           account.permDichVu);
            chkTienNghiDialog   = createEditableCheck("Tiện nghi",         account.permTienNghi);
            chkTaiKhoanDialog   = createEditableCheck("Tài khoản",         account.permTaiKhoan);
            chkNhanVienDialog   = createEditableCheck("Nhân viên",         account.permNhanVien);
            chkBaoCaoDialog     = createEditableCheck("Báo cáo thống kê",  account.permBaoCao);

            // Lễ tân KHÔNG được cấp quyền Tài khoản & Nhân viên
            if ("Lễ tân".equals(account.vaiTro)) {
                chkTaiKhoanDialog.setEnabled(false);
                chkNhanVienDialog.setEnabled(false);
                chkTaiKhoanDialog.setSelected(false);
                chkNhanVienDialog.setSelected(false);
            }

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

            JButton btnSave    = createPrimaryButton("Lưu quyền", new Color(59, 130, 246), Color.WHITE, e -> submit());
            JButton btnDefault = createOutlineButton("Khôi phục mặc định theo vai trò", new Color(245, 158, 11),
                    e -> applyRoleDefaults(valueOf(cboVaiTroDialog.getSelectedItem())));
            JButton btnCancel  = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
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
                // Lễ tân KHÔNG được có quyền Tài khoản & Nhân viên
                chkTaiKhoanDialog.setSelected(false);
                chkTaiKhoanDialog.setEnabled(false);
                chkNhanVienDialog.setSelected(false);
                chkNhanVienDialog.setEnabled(false);
            } else {
                chkTaiKhoanDialog.setEnabled(true);
                chkNhanVienDialog.setEnabled(true);
            }
        }

        private void applyRoleDefaults(String selectedRole) {
            AccountRecord snapshot = new AccountRecord(
                    0, account.tenDangNhap, account.nhanVien, selectedRole,
                    account.trangThai, account.lanDangNhapCuoi, account.emailKhoiPhuc, ""
            );
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

            // Đảm bảo Lễ tân không có quyền Tài khoản/Nhân viên
            if ("Lễ tân".equals(selectedRole)) {
                chkTaiKhoanDialog.setSelected(false);
                chkTaiKhoanDialog.setEnabled(false);
                chkNhanVienDialog.setSelected(false);
                chkNhanVienDialog.setEnabled(false);
            } else {
                chkTaiKhoanDialog.setEnabled(true);
                chkNhanVienDialog.setEnabled(true);
            }
        }

        private void submit() {
            String vaiTroMoi = valueOf(cboVaiTroDialog.getSelectedItem());

            // Áp dụng quyền vào record
            account.vaiTro        = vaiTroMoi;
            account.permDashboard  = chkDashboardDialog.isSelected();
            account.permDatPhong   = chkDatPhongDialog.isSelected();
            account.permCheckInOut = chkCheckInOutDialog.isSelected();
            account.permThanhToan  = chkThanhToanDialog.isSelected();
            account.permKhachHang  = chkKhachHangDialog.isSelected();
            account.permPhong      = chkPhongDialog.isSelected();
            account.permLoaiPhong  = chkLoaiPhongDialog.isSelected();
            account.permBangGia    = chkBangGiaDialog.isSelected();
            account.permDichVu     = chkDichVuDialog.isSelected();
            account.permTienNghi   = chkTienNghiDialog.isSelected();
            account.permTaiKhoan   = chkTaiKhoanDialog.isSelected();
            account.permNhanVien   = chkNhanVienDialog.isSelected();
            account.permBaoCao     = chkBaoCaoDialog.isSelected();

            // Lễ tân bắt buộc false
            if ("Lễ tân".equals(vaiTroMoi)) {
                account.permTaiKhoan = false;
                account.permNhanVien = false;
            }

            account.ghiChu = "Đã cập nhật phân quyền cho vai trò " + vaiTroMoi + ".";

            // Cập nhật vaiTro trong bảng TaiKhoan
            try {
                Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE TaiKhoan SET vaiTro = ? WHERE maTaiKhoan = ?"
                );
                ps.setString(1, vaiTroMoi);
                ps.setInt(2, account.maTaiKhoan);
                ps.executeUpdate();
            } catch (Exception e) {
                showError("Lỗi cập nhật vai trò: " + e.getMessage());
                return;
            }

            // Lưu quyền vào bảng TaiKhoanQuyen
            savePermissionsToDatabase(account);

            refreshAccountViews(account, "Cập nhật phân quyền thành công.");
            dispose();
        }
    }

    // =========================================================================
    // DIALOG: KHÓA TÀI KHOẢN — cập nhật DB
    // =========================================================================
    private final class ToggleAccountDialog extends BaseAccountDialog {
        private final AccountRecord account;
        private final JTextArea txtLyDo;

        private ToggleAccountDialog(Frame owner, AccountRecord account) {
            super(owner,
                    "Hoạt động".equalsIgnoreCase(account.trangThai) ? "Khóa tài khoản" : "Mở tài khoản",
                    560, 380);
            this.account = account;

            boolean isKhoa = "Hoạt động".equalsIgnoreCase(account.trangThai);
            String actionText = isKhoa ? "khóa" : "mở";
            String newStatus = isKhoa ? "Khóa" : "Hoạt động";

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    (isKhoa ? "Khóa" : "Mở") + " tài khoản",
                    "Nhập lý do " + actionText + " tài khoản. Sau khi xác nhận, trạng thái tài khoản sẽ chuyển sang <b>" + newStatus + "</b>."
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
            addFormRow(form, gbc, 4, "Lý do " + actionText, new JScrollPane(txtLyDo));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton(
                    isKhoa ? "Xác nhận khóa" : "Xác nhận mở",
                    isKhoa ? new Color(245, 158, 11) : new Color(22, 163, 74),
                    isKhoa ? TEXT_PRIMARY : Color.WHITE,
                    e -> submit()
            );
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());

            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            String lyDo = txtLyDo.getText().trim();
            if (lyDo.isEmpty()) {
                showError("Vui lòng nhập lý do.");
                return;
            }

            boolean isKhoa = "Hoạt động".equalsIgnoreCase(account.trangThai);
            String newStatus = isKhoa ? "Khóa" : "Hoạt động";

            if (!showConfirmDialog(
                    "Xác nhận cập nhật trạng thái",
                    "Tài khoản sẽ chuyển sang trạng thái " + newStatus + ". Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    isKhoa ? new Color(245, 158, 11) : new Color(22, 163, 74)
            )) return;

            try {
                Connection con = ConnectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE TaiKhoan SET trangThai = ? WHERE maTaiKhoan = ?"
                );
                ps.setString(1, newStatus);
                ps.setInt(2, account.maTaiKhoan);
                ps.executeUpdate();
            } catch (Exception e) {
                showError("Lỗi cập nhật trạng thái: " + e.getMessage());
                return;
            }

            account.trangThai = newStatus;
            account.ghiChu = (isKhoa ? "Lý do khóa: " : "Lý do mở: ") + lyDo;
            refreshAccountViews(account,
                    isKhoa ? "Khóa tài khoản thành công." : "Mở tài khoản thành công.");
            dispose();
        }
    }

    // =========================================================================
    // SHARED DIALOGS
    // =========================================================================
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
            JButton btnCancel  = createOutlineButton("Bỏ qua", new Color(107, 114, 128), e -> dispose());
            JButton btnConfirm = createPrimaryButton(confirmText, confirmColor, Color.WHITE, e -> { confirmed = true; dispose(); });
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private boolean isConfirmed() { return confirmed; }
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

    // =========================================================================
    // ACCOUNT RECORD (in-memory model, đồng bộ với DB)
    // =========================================================================
    private static final class AccountRecord {
        int maTaiKhoan;           // Khoá chính từ DB
        String tenDangNhap;
        String nhanVien;          // hoTen từ JOIN
        String vaiTro;
        String trangThai;
        String lanDangNhapCuoi;
        String emailKhoiPhuc;
        String ghiChu;

        // Quyền chức năng — lưu trong bảng TaiKhoanQuyen
        boolean permDashboard;
        boolean permDatPhong;
        boolean permCheckInOut;
        boolean permThanhToan;
        boolean permKhachHang;
        boolean permPhong;
        boolean permLoaiPhong;
        boolean permBangGia;
        boolean permDichVu;
        boolean permTienNghi;
        boolean permTaiKhoan;
        boolean permNhanVien;
        boolean permBaoCao;

        AccountRecord(int maTaiKhoan, String tenDangNhap, String nhanVien, String vaiTro,
                      String trangThai, String lanDangNhapCuoi, String emailKhoiPhuc, String ghiChu) {
            this.maTaiKhoan = maTaiKhoan;
            this.tenDangNhap = tenDangNhap;
            this.nhanVien = nhanVien;
            this.vaiTro = vaiTro;
            this.trangThai = trangThai;
            this.lanDangNhapCuoi = lanDangNhapCuoi;
            this.emailKhoiPhuc = emailKhoiPhuc;
            this.ghiChu = ghiChu != null ? ghiChu : "";
        }

        /**
         * Áp dụng quyền mặc định theo vai trò.
         * Đây là giá trị khởi tạo; có thể bị ghi đè bởi TaiKhoanQuyen trong DB.
         */
        void applyRoleDefaults(String selectedRole) {
            permDashboard = true;
            permDatPhong  = true;
            permCheckInOut = true;
            permKhachHang = true;

            if ("Lễ tân".equals(selectedRole)) {
                permThanhToan = false;
                permPhong = false; permLoaiPhong = false; permBangGia = false;
                permDichVu = false; permTienNghi = false;
                permTaiKhoan = false; permNhanVien = false; permBaoCao = false;
                return;
            }

            if ("Kế toán".equals(selectedRole)) {
                permThanhToan = true;
                permPhong = false; permLoaiPhong = false;
                permBangGia = true;
                permDichVu = false; permTienNghi = false;
                permTaiKhoan = false; permNhanVien = false; permBaoCao = true;
                return;
            }

            if ("Quản lý".equals(selectedRole)) {
                permThanhToan = true;
                permPhong = true; permLoaiPhong = true; permBangGia = true;
                permDichVu = true; permTienNghi = true;
                permTaiKhoan = true; permNhanVien = true; permBaoCao = true;
                return;
            }

            // Quản trị — toàn quyền
            permThanhToan = true;
            permPhong = true; permLoaiPhong = true; permBangGia = true;
            permDichVu = true; permTienNghi = true;
            permTaiKhoan = true; permNhanVien = true; permBaoCao = true;
        }
    }

    // =========================================================================
    // BUILD PANEL (dùng bởi NavigationUtil để swap vào AppFrame)
    // =========================================================================
    /**
     * Trả về panel nội dung để NavigationUtil swap vào AppFrame.
     * Nếu là Lễ tân: hiện dialog cảnh báo và trả về null
     * (NavigationUtil phải kiểm tra null để không swap màn hình).
     */
    public JPanel buildPanel() {
        if (isLetanRole()) {
            showAccessDeniedDialog();
            return null;   // Không swap — giữ nguyên màn hình hiện tại
        }
        if (rootPanel == null) initUI();
        return rootPanel;
    }
}