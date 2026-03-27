package gui;

import dao.NhanVienDAO;
import entity.NhanVien;
import gui.common.AppBranding;
import gui.common.ScreenUIHelper;
import gui.common.SidebarFactory;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NhanVienGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Font TITLE_FONT = new Font("Tahoma", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Tahoma", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Tahoma", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 12);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Date INVALID_DATE = Date.valueOf("0001-01-01");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,15}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,20}$");

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final String username;
    private final String role;
    private final List<NhanVien> displayedNhanVien = new ArrayList<NhanVien>();

    private JPanel rootPanel;
    private JTable tblNhanVien;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboBoPhanFilter;
    private JComboBox<String> cboChucVuFilter;
    private JComboBox<String> cboTrangThaiFilter;
    private JTextField txtHoTenFilter;

    private JTextField txtMaNhanVien;
    private JTextField txtHoTen;
    private JTextField txtNgaySinh;
    private JComboBox<String> cboGioiTinh;
    private JTextField txtCccd;
    private JTextField txtSoDienThoai;
    private JTextField txtEmail;
    private JTextArea txtDiaChi;
    private JComboBox<String> cboBoPhan;
    private JComboBox<String> cboChucVu;
    private JComboBox<String> cboCaLam;
    private JTextField txtNgayVaoLam;
    private JComboBox<String> cboTrangThai;
    private JTextArea txtGhiChu;

    public NhanVienGUI() {
        this("guest", "Lễ tân");
    }

    public NhanVienGUI(String username, String role) {
        this.username = username == null || username.trim().isEmpty() ? "guest" : username.trim();
        this.role = role == null || role.trim().isEmpty() ? "Lễ tân" : role.trim();
        setTitle("Quản lý nhân viên - " + AppBranding.APP_DISPLAY_NAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        reloadNhanVien(true, false);
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
        JLabel lblSub = new JLabel("Quản lý hồ sơ nhân viên theo dữ liệu thực từ SQL Server.");
        lblSub.setFont(BODY_FONT);
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
        card.add(createPrimaryButton("Thêm", new Color(22, 163, 74), Color.WHITE, e -> prepareCreate()));
        card.add(createPrimaryButton("Lưu", new Color(37, 99, 235), Color.WHITE, e -> saveNhanVien()));
        card.add(createPrimaryButton("Xóa", new Color(220, 38, 38), Color.WHITE, e -> deleteSelectedNhanVien()));
        card.add(createPrimaryButton("Xem chi tiết", new Color(99, 102, 241), Color.WHITE, e -> showSelectedDetails()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadNhanVien(true, true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        cboBoPhanFilter = createComboBox(new String[]{"Tất cả", "Lễ tân", "Buồng phòng", "Kế toán", "Kỹ thuật", "Điều hành"});
        cboChucVuFilter = createComboBox(new String[]{"Tất cả", "Nhân viên", "Trưởng ca", "Quản lý", "Kế toán tổng hợp"});
        cboTrangThaiFilter = createComboBox(new String[]{"Tất cả", "Hoạt động", "Tạm ngừng", "Ngừng làm việc"});
        txtHoTenFilter = createInputField("");
        txtHoTenFilter.setPreferredSize(new Dimension(260, 34));
        left.add(createFieldGroup("Bộ phận", cboBoPhanFilter));
        left.add(createFieldGroup("Chức vụ", cboChucVuFilter));
        left.add(createFieldGroup("Trạng thái", cboTrangThaiFilter));
        left.add(createFieldGroup("Họ tên", txtHoTenFilter));
        card.add(left, BorderLayout.CENTER);
        card.add(createPrimaryButton("Lọc", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)), BorderLayout.EAST);
        return card;
    }

    private JSplitPane buildCenterContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildDetailCard());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildTableCard() {
        tableModel = new DefaultTableModel(new String[]{
                "Mã NV", "Họ tên", "SĐT", "Bộ phận", "Chức vụ", "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblNhanVien = new JTable(tableModel);
        tblNhanVien.setFont(BODY_FONT);
        tblNhanVien.setRowHeight(32);
        tblNhanVien.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillSelectedNhanVienToForm();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblNhanVien);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        JPanel wrapper = createCardPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Chi tiết nhân viên");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 12);

        txtMaNhanVien = createInputField("");
        txtMaNhanVien.setEditable(false);
        txtHoTen = createInputField("");
        txtNgaySinh = createInputField("");
        cboGioiTinh = createComboBox(new String[]{"Nam", "Nữ", "Khác"});
        txtCccd = createInputField("");
        txtSoDienThoai = createInputField("");
        txtEmail = createInputField("");
        txtDiaChi = createTextArea(3);
        cboBoPhan = createComboBox(new String[]{"Lễ tân", "Buồng phòng", "Kế toán", "Kỹ thuật", "Điều hành"});
        cboChucVu = createComboBox(new String[]{"Nhân viên", "Trưởng ca", "Quản lý", "Kế toán tổng hợp"});
        cboCaLam = createComboBox(new String[]{"Ca sáng", "Ca chiều", "Ca tối"});
        txtNgayVaoLam = createInputField("");
        cboTrangThai = createComboBox(new String[]{"Hoạt động", "Tạm ngừng", "Ngừng làm việc"});
        txtGhiChu = createTextArea(3);

        addFormRow(form, gbc, 0, "Mã nhân viên", txtMaNhanVien);
        addFormRow(form, gbc, 1, "Họ tên", txtHoTen);
        addFormRow(form, gbc, 2, "Ngày sinh", txtNgaySinh);
        addFormRow(form, gbc, 3, "Giới tính", cboGioiTinh);
        addFormRow(form, gbc, 4, "CCCD", txtCccd);
        addFormRow(form, gbc, 5, "Số điện thoại", txtSoDienThoai);
        addFormRow(form, gbc, 6, "Email", txtEmail);
        addFormRow(form, gbc, 7, "Địa chỉ", new JScrollPane(txtDiaChi));
        addFormRow(form, gbc, 8, "Bộ phận", cboBoPhan);
        addFormRow(form, gbc, 9, "Chức vụ", cboChucVu);
        addFormRow(form, gbc, 10, "Ca làm", cboCaLam);
        addFormRow(form, gbc, 11, "Ngày vào làm", txtNgayVaoLam);
        addFormRow(form, gbc, 12, "Trạng thái", cboTrangThai);
        addFormRow(form, gbc, 13, "Ghi chú", new JScrollPane(txtGhiChu));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private void reloadNhanVien(boolean resetFilters, boolean showMessage) {
        if (resetFilters) {
            cboBoPhanFilter.setSelectedIndex(0);
            cboChucVuFilter.setSelectedIndex(0);
            cboTrangThaiFilter.setSelectedIndex(0);
            txtHoTenFilter.setText("");
        }
        applyFilters(false);
        refreshCurrentView();
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu nhân viên.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void applyFilters(boolean showMessage) {
        String boPhan = "Tất cả".equals(valueOf(cboBoPhanFilter.getSelectedItem())) ? "" : valueOf(cboBoPhanFilter.getSelectedItem());
        String chucVu = "Tất cả".equals(valueOf(cboChucVuFilter.getSelectedItem())) ? "" : valueOf(cboChucVuFilter.getSelectedItem());
        String trangThai = "Tất cả".equals(valueOf(cboTrangThaiFilter.getSelectedItem())) ? "" : valueOf(cboTrangThaiFilter.getSelectedItem());
        displayedNhanVien.clear();
        displayedNhanVien.addAll(nhanVienDAO.search(txtHoTenFilter.getText().trim(), boPhan, chucVu, trangThai));
        refillTable();
        refreshCurrentView();
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Đã lọc được " + displayedNhanVien.size() + " nhân viên phù hợp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (NhanVien nhanVien : displayedNhanVien) {
            tableModel.addRow(new Object[]{
                    nhanVien.getMaNhanVien(),
                    nhanVien.getHoTen(),
                    nhanVien.getSoDienThoai(),
                    nhanVien.getBoPhan(),
                    nhanVien.getChucVu(),
                    nhanVien.getTrangThai()
            });
        }
        if (!displayedNhanVien.isEmpty()) {
            tblNhanVien.setRowSelectionInterval(0, 0);
            fillSelectedNhanVienToForm();
        } else {
            resetForm();
        }
    }

    private void fillSelectedNhanVienToForm() {
        NhanVien nhanVien = getSelectedNhanVien(false);
        if (nhanVien == null) {
            refreshCurrentView();
            return;
        }
        txtMaNhanVien.setText(String.valueOf(nhanVien.getMaNhanVien()));
        txtHoTen.setText(safeValue(nhanVien.getHoTen()));
        txtNgaySinh.setText(formatDate(nhanVien.getNgaySinh()));
        cboGioiTinh.setSelectedItem(safeValue(nhanVien.getGioiTinh()));
        txtCccd.setText(safeValue(nhanVien.getCccd()));
        txtSoDienThoai.setText(safeValue(nhanVien.getSoDienThoai()));
        txtEmail.setText(safeValue(nhanVien.getEmail()));
        txtDiaChi.setText(safeValue(nhanVien.getDiaChi()));
        cboBoPhan.setSelectedItem(safeValue(nhanVien.getBoPhan()));
        cboChucVu.setSelectedItem(safeValue(nhanVien.getChucVu()));
        cboCaLam.setSelectedItem(safeValue(nhanVien.getCaLam()));
        txtNgayVaoLam.setText(formatDate(nhanVien.getNgayVaoLam()));
        cboTrangThai.setSelectedItem(safeValue(nhanVien.getTrangThai()));
        txtGhiChu.setText(safeValue(nhanVien.getGhiChu()));
        refreshCurrentView();
    }

    private void prepareCreate() {
        tblNhanVien.clearSelection();
        resetForm();
        txtHoTen.requestFocusInWindow();
    }

    private void saveNhanVien() {
        NhanVien nhanVien = buildNhanVienFromForm();
        if (nhanVien == null) {
            return;
        }

        boolean updating = !txtMaNhanVien.getText().trim().isEmpty();
        boolean success;
        if (updating) {
            nhanVien.setMaNhanVien(Integer.parseInt(txtMaNhanVien.getText().trim()));
            success = nhanVienDAO.update(nhanVien);
        } else {
            success = nhanVienDAO.insert(nhanVien);
        }

        if (!success) {
            JOptionPane.showMessageDialog(this, "Không thể lưu nhân viên.\nChi tiết: " + safeValue(nhanVienDAO.getLastErrorMessage()), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        reloadNhanVien(false, false);
        selectNhanVien(nhanVien.getMaNhanVien());
        JOptionPane.showMessageDialog(this, updating ? "Cập nhật nhân viên thành công." : "Thêm nhân viên thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        if (!updating) {
            resetForm();
        }
    }

    private void deleteSelectedNhanVien() {
        NhanVien nhanVien = getSelectedNhanVien(true);
        if (nhanVien == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa nhân viên đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (nhanVienDAO.delete(nhanVien.getMaNhanVien())) {
            reloadNhanVien(false, false);
            resetForm();
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa nhân viên.\nChi tiết: " + safeValue(nhanVienDAO.getLastErrorMessage()), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedDetails() {
        NhanVien nhanVien = getSelectedNhanVien(true);
        if (nhanVien == null) {
            return;
        }
        JOptionPane.showMessageDialog(
                this,
                "Mã NV: " + nhanVien.getMaNhanVien()
                        + "\nHọ tên: " + nhanVien.getHoTen()
                        + "\nBộ phận: " + safeValue(nhanVien.getBoPhan())
                        + "\nChức vụ: " + safeValue(nhanVien.getChucVu())
                        + "\nCa làm: " + safeValue(nhanVien.getCaLam())
                        + "\nTrạng thái: " + safeValue(nhanVien.getTrangThai())
                        + "\nEmail: " + safeValue(nhanVien.getEmail())
                        + "\nCCCD: " + safeValue(nhanVien.getCccd()),
                "Chi tiết nhân viên",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private NhanVien getSelectedNhanVien(boolean showMessage) {
        int row = tblNhanVien.getSelectedRow();
        if (row < 0 || row >= displayedNhanVien.size()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        return displayedNhanVien.get(row);
    }

    private NhanVien buildNhanVienFromForm() {
        String hoTen = txtHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Date ngaySinh = parseRequiredDate(txtNgaySinh.getText().trim(), "Ngày sinh không đúng định dạng dd/MM/yyyy.");
        if (isInvalidDateMarker(ngaySinh)) {
            return null;
        }
        Date ngayVaoLam = parseRequiredDate(txtNgayVaoLam.getText().trim(), "Ngày vào làm không đúng định dạng dd/MM/yyyy.");
        if (isInvalidDateMarker(ngayVaoLam)) {
            return null;
        }
        String cccd = txtCccd.getText().trim();
        if (!cccd.isEmpty() && !CCCD_PATTERN.matcher(cccd).matches()) {
            JOptionPane.showMessageDialog(this, "CCCD không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String soDienThoai = txtSoDienThoai.getText().trim();
        if (!soDienThoai.isEmpty() && !PHONE_PATTERN.matcher(soDienThoai).matches()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (valueOf(cboBoPhan.getSelectedItem()).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bộ phận không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (valueOf(cboChucVu.getSelectedItem()).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chức vụ không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (valueOf(cboCaLam.getSelectedItem()).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ca làm không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        return new NhanVien(
                hoTen,
                ngaySinh,
                valueOf(cboGioiTinh.getSelectedItem()),
                cccd,
                soDienThoai,
                email,
                txtDiaChi.getText().trim(),
                valueOf(cboBoPhan.getSelectedItem()),
                valueOf(cboChucVu.getSelectedItem()),
                valueOf(cboCaLam.getSelectedItem()),
                ngayVaoLam,
                valueOf(cboTrangThai.getSelectedItem()),
                txtGhiChu.getText().trim()
        );
    }

    private void resetForm() {
        txtMaNhanVien.setText("");
        txtHoTen.setText("");
        txtNgaySinh.setText("");
        cboGioiTinh.setSelectedIndex(0);
        txtCccd.setText("");
        txtSoDienThoai.setText("");
        txtEmail.setText("");
        txtDiaChi.setText("");
        cboBoPhan.setSelectedIndex(0);
        cboChucVu.setSelectedIndex(0);
        cboCaLam.setSelectedIndex(0);
        txtNgayVaoLam.setText("");
        cboTrangThai.setSelectedIndex(0);
        txtGhiChu.setText("");
        refreshCurrentView();
    }

    private void selectNhanVien(int maNhanVien) {
        for (int i = 0; i < displayedNhanVien.size(); i++) {
            if (displayedNhanVien.get(i).getMaNhanVien() == maNhanVien) {
                tblNhanVien.setRowSelectionInterval(i, i);
                fillSelectedNhanVienToForm();
                refreshCurrentView();
                return;
            }
        }
        refreshCurrentView();
    }

    private void refreshCurrentView() {
        if (rootPanel != null) {
            rootPanel.revalidate();
            rootPanel.repaint();
        }
        if (tblNhanVien != null) {
            tblNhanVien.revalidate();
            tblNhanVien.repaint();
        }
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "nhanvien-f1", this::prepareCreate);
        ScreenUIHelper.registerShortcut(this, "F2", "nhanvien-f2", this::saveNhanVien);
        ScreenUIHelper.registerShortcut(this, "F3", "nhanvien-f3", this::deleteSelectedNhanVien);
        ScreenUIHelper.registerShortcut(this, "F5", "nhanvien-f5", () -> reloadNhanVien(true, true));
    }

    private Date parseRequiredDate(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return INVALID_DATE;
        }
        try {
            return Date.valueOf(LocalDate.parse(value.trim(), DATE_FORMATTER));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return INVALID_DATE;
        }
    }

    private boolean isInvalidDateMarker(Date date) {
        return date != null && INVALID_DATE.equals(date);
    }

    private String formatDate(Date date) {
        return date == null ? "" : date.toLocalDate().format(DATE_FORMATTER);
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
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
        JComboBox<String> comboBox = new JComboBox<String>(values);
        comboBox.setFont(BODY_FONT);
        comboBox.setPreferredSize(new Dimension(170, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(220, 34));
        return field;
    }

    private JTextArea createTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(BODY_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JButton createPrimaryButton(String text, Color background, Color foreground, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Tahoma", Font.BOLD, 13));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.addActionListener(listener);
        return button;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NhanVienGUI gui = new NhanVienGUI();
            gui.setSize(1450, 850);
            gui.setLocationRelativeTo(null);
            gui.setVisible(true);
        });
    }
}
