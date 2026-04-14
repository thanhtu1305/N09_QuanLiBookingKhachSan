package gui;

import dao.NhanVienDAO;
import entity.NhanVien;
import gui.common.AppBranding;
import gui.common.AppDatePickerField;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NhanVienGUI extends JFrame {
    private static final Color APP_BG = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(249, 250, 251);
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final Date INVALID_DATE = Date.valueOf("0001-01-01");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,15}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{9,20}$");
    private static final String[] GIOI_TINH_OPTIONS = {"Nam", "Nữ", "Khác"};
    private static final String[] BO_PHAN_OPTIONS = {"Lễ tân"};
    private static final String[] CHUC_VU_OPTIONS = {"Lễ tân"};
    private static final String[] CA_LAM_OPTIONS = {"Ca sáng", "Ca chiều", "Ca tối"};
    private static final String[] TRANG_THAI_OPTIONS = {"Hoạt động", "Tạm ngừng", "Ngừng làm việc", "Khóa"};
    private static final String[] STAFF_ROLE_OPTIONS = {"Lễ tân"};

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final String username;
    private final String role;
    private final List<NhanVien> displayedNhanVien = new ArrayList<NhanVien>();

    private JPanel rootPanel;
    private JTable tblNhanVien;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboChucVuFilter;
    private JComboBox<String> cboTrangThaiFilter;
    private JTextField txtHoTenFilter;

    private JLabel lblMaNhanVien;
    private JLabel lblHoTen;
    private JLabel lblNgaySinh;
    private JLabel lblGioiTinh;
    private JLabel lblCccd;
    private JLabel lblSoDienThoai;
    private JLabel lblEmail;
    private JLabel lblChucVu;
    private JLabel lblCaLam;
    private JLabel lblNgayVaoLam;
    private JLabel lblTrangThai;
    private JTextArea txtDiaChi;
    private JTextArea txtGhiChu;

    public NhanVienGUI() {
        this("guest", "Lễ tân");
    }

    public NhanVienGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        // Lễ tân: không khởi tạo UI — buildPanel() sẽ hiện dialog thông báo

        setTitle("Quản lý nhân viên - " + AppBranding.APP_DISPLAY_NAME);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        reloadNhanVien(true, false);
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
                        + "Chức năng <b>Quản lý Nhân viên</b> chỉ dành cho Quản lý.<br>"
                        + "Vui lòng liên hệ Quản lý để được hỗ trợ.</html>",
                "Không có quyền truy cập",
                javax.swing.JOptionPane.WARNING_MESSAGE
        );
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

        JLabel lblSub = new JLabel("Quản lý hồ sơ nhân viên theo dữ liệu thực tế từ SQL Server.");
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
        card.add(createPrimaryButton("Thêm nhân viên", ACCENT_GREEN, Color.WHITE, e -> openNhanVienDialog(null)));
        card.add(createPrimaryButton("Xem chi tiết", new Color(99, 102, 241), Color.WHITE, e -> openViewSelectedNhanVien()));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        cboChucVuFilter = createComboBox(prependAll(STAFF_ROLE_OPTIONS));
        cboTrangThaiFilter = createComboBox(prependAll(TRANG_THAI_OPTIONS));
        txtHoTenFilter = createInputField("");
        ScreenUIHelper.applySearchFieldSize(txtHoTenFilter);
        ScreenUIHelper.installLiveSearch(txtHoTenFilter, () -> applyFilters(false));
        ScreenUIHelper.installAutoFilter(() -> applyFilters(false), cboChucVuFilter, cboTrangThaiFilter);

        left.add(createFieldGroup("Chức vụ", cboChucVuFilter));
        left.add(createFieldGroup("Trạng thái", cboTrangThaiFilter));

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
        searchRow.add(txtHoTenFilter, BorderLayout.CENTER);
        right.add(searchRow);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JSplitPane buildCenterContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildDetailCard());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.56);
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

        JLabel lblSub = new JLabel("Double click để cập nhật hồ sơ nhân viên.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        tableModel = new DefaultTableModel(new String[]{
                "Mã NV", "Họ tên", "SĐT", "Chức vụ", "Trạng thái"
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
        tblNhanVien.setGridColor(BORDER_SOFT);
        tblNhanVien.setShowGrid(true);
        tblNhanVien.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblNhanVien);
        tblNhanVien.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedNhanVien();
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblNhanVien, this::openEditSelectedNhanVien);

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
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);
        wrapper.add(createOverviewCard(), BorderLayout.NORTH);
        wrapper.add(createNotesCard(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createOverviewCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết nhân viên");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblMaNhanVien = createValueLabel();
        lblHoTen = createValueLabel();
        lblNgaySinh = createValueLabel();
        lblGioiTinh = createValueLabel();
        lblCccd = createValueLabel();
        lblSoDienThoai = createValueLabel();
        lblEmail = createValueLabel();
        lblChucVu = createValueLabel();
        lblCaLam = createValueLabel();
        lblNgayVaoLam = createValueLabel();
        lblTrangThai = createValueLabel();

        addDetailRow(body, "Mã nhân viên", lblMaNhanVien);
        addDetailRow(body, "Họ tên", lblHoTen);
        addDetailRow(body, "Ngày sinh", lblNgaySinh);
        addDetailRow(body, "Giới tính", lblGioiTinh);
        addDetailRow(body, "CCCD", lblCccd);
        addDetailRow(body, "Số điện thoại", lblSoDienThoai);
        addDetailRow(body, "Email", lblEmail);
        addDetailRow(body, "Chức vụ", lblChucVu);
        addDetailRow(body, "Ca làm", lblCaLam);
        addDetailRow(body, "Ngày vào làm", lblNgayVaoLam);
        addDetailRow(body, "Trạng thái", lblTrangThai);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNotesCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Thông tin bổ sung");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 10));
        body.setOpaque(false);

        txtDiaChi = createReadonlyArea(4);
        txtGhiChu = createReadonlyArea(4);
        body.add(createReadOnlyBlock("Địa chỉ", txtDiaChi));
        body.add(createReadOnlyBlock("Ghi chú", txtGhiChu));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel createReadOnlyBlock(String label, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(TEXT_MUTED);

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG, BORDER_SOFT, TEXT_MUTED,
                "F1 Thêm nhân viên", "F2 Cập nhật", "F3 Xóa", "F4 Xem chi tiết", "Enter Cập nhật"
        );
    }

    private void reloadNhanVien(boolean resetFilters, boolean showMessage) {
        if (resetFilters) {
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
        String chucVu = "Tất cả".equals(valueOf(cboChucVuFilter.getSelectedItem())) ? "" : valueOf(cboChucVuFilter.getSelectedItem());
        String trangThai = "Tất cả".equals(valueOf(cboTrangThaiFilter.getSelectedItem())) ? "" : valueOf(cboTrangThaiFilter.getSelectedItem());

        String tuKhoa = safeValue(txtHoTenFilter.getText(), "").trim().toLowerCase(java.util.Locale.ROOT);

        displayedNhanVien.clear();
        for (NhanVien nhanVien : nhanVienDAO.search("", "", "", trangThai)) {
            normalizePersonnelFields(nhanVien);
            if (!chucVu.isEmpty() && !chucVu.equals(safeValue(nhanVien.getChucVu(), ""))) {
                continue;
            }
            if (!tuKhoa.isEmpty() && !buildNhanVienSearchText(nhanVien).contains(tuKhoa)) {
                continue;
            }
            displayedNhanVien.add(nhanVien);
        }
        refillTable();
        refreshCurrentView();

        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Đã lọc được " + displayedNhanVien.size() + " nhân viên phù hợp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (NhanVien nhanVien : displayedNhanVien) {
            normalizePersonnelFields(nhanVien);
            tableModel.addRow(new Object[]{
                    formatEmployeeCode(nhanVien.getMaNhanVien()),
                    nhanVien.getHoTen(),
                    nhanVien.getSoDienThoai(),
                    nhanVien.getChucVu(),
                    nhanVien.getTrangThai()
            });
        }

        if (!displayedNhanVien.isEmpty()) {
            tblNhanVien.setRowSelectionInterval(0, 0);
            showSelectedNhanVien();
        } else {
            clearDetailPanel();
        }
    }

    private void showSelectedNhanVien() {
        NhanVien nhanVien = getSelectedNhanVien(false);
        if (nhanVien == null) {
            clearDetailPanel();
            refreshCurrentView();
            return;
        }
        normalizePersonnelFields(nhanVien);

        lblMaNhanVien.setText(formatEmployeeCode(nhanVien.getMaNhanVien()));
        lblHoTen.setText(safeValue(nhanVien.getHoTen(), "-"));
        lblNgaySinh.setText(formatDate(nhanVien.getNgaySinh()));
        lblGioiTinh.setText(safeValue(nhanVien.getGioiTinh(), "-"));
        lblCccd.setText(safeValue(nhanVien.getCccd(), "-"));
        lblSoDienThoai.setText(safeValue(nhanVien.getSoDienThoai(), "-"));
        lblEmail.setText(safeValue(nhanVien.getEmail(), "-"));
        lblChucVu.setText(safeValue(nhanVien.getChucVu(), "-"));
        lblCaLam.setText(safeValue(nhanVien.getCaLam(), "-"));
        lblNgayVaoLam.setText(formatDate(nhanVien.getNgayVaoLam()));
        lblTrangThai.setText(safeValue(nhanVien.getTrangThai(), "-"));
        txtDiaChi.setText(safeValue(nhanVien.getDiaChi(), "-"));
        txtGhiChu.setText(safeValue(nhanVien.getGhiChu(), "-"));
        refreshCurrentView();
    }

    private void clearDetailPanel() {
        lblMaNhanVien.setText("-");
        lblHoTen.setText("-");
        lblNgaySinh.setText("-");
        lblGioiTinh.setText("-");
        lblCccd.setText("-");
        lblSoDienThoai.setText("-");
        lblEmail.setText("-");
        lblChucVu.setText("-");
        lblCaLam.setText("-");
        lblNgayVaoLam.setText("-");
        lblTrangThai.setText("-");
        txtDiaChi.setText("-");
        txtGhiChu.setText("-");
    }

    private void openNhanVienDialog(NhanVien nhanVien) {
        new NhanVienFormDialog(this, nhanVien).setVisible(true);
    }

    private void openEditSelectedNhanVien() {
        NhanVien nhanVien = getSelectedNhanVien(true);
        if (nhanVien != null) {
            openNhanVienDialog(nhanVien);
        }
    }

    private void openViewSelectedNhanVien() {
        NhanVien nhanVien = getSelectedNhanVien(true);
        if (nhanVien != null) {
            new NhanVienViewDialog(this, nhanVien).setVisible(true);
        }
    }

    private void deleteSelectedNhanVien() {
        NhanVien nhanVien = getSelectedNhanVien(true);
        if (nhanVien == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xóa nhân viên đang chọn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (nhanVienDAO.delete(nhanVien.getMaNhanVien())) {
            reloadNhanVien(false, false);
            JOptionPane.showMessageDialog(this, "Đã xóa nhân viên.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa nhân viên.\nChi tiết: " + safeValue(nhanVienDAO.getLastErrorMessage(), "Không xác định."),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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

    private void selectNhanVien(int maNhanVien) {
        for (int i = 0; i < displayedNhanVien.size(); i++) {
            if (displayedNhanVien.get(i).getMaNhanVien() == maNhanVien) {
                tblNhanVien.setRowSelectionInterval(i, i);
                showSelectedNhanVien();
                refreshCurrentView();
                return;
            }
        }
        refreshCurrentView();
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "nhanvien-f1", () -> openNhanVienDialog(null));
        ScreenUIHelper.registerShortcut(this, "F2", "nhanvien-f2", this::openEditSelectedNhanVien);
        ScreenUIHelper.registerShortcut(this, "F3", "nhanvien-f3", this::deleteSelectedNhanVien);
        ScreenUIHelper.registerShortcut(this, "F4", "nhanvien-f4", this::openViewSelectedNhanVien);
        ScreenUIHelper.registerShortcut(this, "ENTER", "nhanvien-enter", this::openEditSelectedNhanVien);
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
        return date == null ? "-" : date.toLocalDate().format(DATE_FORMATTER);
    }

    private String formatDateOrEmpty(Date date) {
        return date == null ? "" : date.toLocalDate().format(DATE_FORMATTER);
    }

    private String safeValue(String value) {
        return safeValue(value, "");
    }

    private String safeValue(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private String[] prependAll(String[] values) {
        String[] result = new String[values.length + 1];
        result[0] = "Tất cả";
        System.arraycopy(values, 0, result, 1, values.length);
        return result;
    }

    private String formatEmployeeCode(int id) {
        return id <= 0 ? "" : "NV" + id;
    }

    private void normalizePersonnelFields(NhanVien nhanVien) {
        if (nhanVien == null) {
            return;
        }
        nhanVien.setBoPhan(normalizePersonnelCategory(nhanVien.getBoPhan()));
        nhanVien.setChucVu(normalizePersonnelCategory(nhanVien.getChucVu()));
    }

    private String normalizePersonnelCategory(String value) {
        return "Lễ tân".equalsIgnoreCase(safeValue(value, "").trim()) ? "Lễ tân" : "Quản lý";
    }

    private String buildNhanVienSearchText(NhanVien nhanVien) {
        normalizePersonnelFields(nhanVien);
        return (
                formatEmployeeCode(nhanVien.getMaNhanVien()) + " " + nhanVien.getMaNhanVien() + " " +
                safeValue(nhanVien.getHoTen(), "") + " " +
                safeValue(nhanVien.getSoDienThoai(), "") + " " +
                safeValue(nhanVien.getBoPhan(), "") + " " +
                safeValue(nhanVien.getChucVu(), "") + " " +
                safeValue(nhanVien.getTrangThai(), "")
        ).toLowerCase(java.util.Locale.ROOT);
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
        comboBox.setMaximumSize(new Dimension(280, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(220, 34));
        field.setMaximumSize(new Dimension(360, 34));
        return field;
    }

    private JTextArea createDialogTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(BODY_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JTextArea createReadonlyArea(int rows) {
        JTextArea area = createDialogTextArea(rows);
        area.setEditable(false);
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

    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    private JLabel createValueLabel(String value) {
        JLabel label = createValueLabel();
        label.setText(value);
        return label;
    }

    private void addDetailRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(125, 20));

        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
    }

    /**
     * Trả về panel nội dung để NavigationUtil swap vào AppFrame.
     * Nếu là Lễ tân: hiện dialog cảnh báo và trả về null
     * (NavigationUtil phải kiểm tra null để không swap màn hình).
     */
    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
            reloadNhanVien(true, false);
            registerShortcuts();
        }
        return rootPanel;
    }

    private abstract class BaseDialog extends JDialog {
        protected BaseDialog(Frame owner, String title) {
            super(ScreenUIHelper.resolveDialogOwner(owner), title, true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setBackground(APP_BG);
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
            setLayout(new BorderLayout(0, 12));
        }

        protected JPanel buildHeader(String title, String subtitle) {
            JPanel panel = createCardPanel(new BorderLayout());
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

        protected JPanel buildButtons(JButton... buttons) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            panel.setOpaque(false);
            for (JButton button : buttons) {
                panel.add(button);
            }
            return panel;
        }

        protected JPanel createFormPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            return panel;
        }

        protected void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component component) {
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

        protected void prepareDialog(Frame owner, int minWidth, int minHeight) {
            ScreenUIHelper.prepareDialog(this, owner, minWidth, minHeight);
        }
    }

    private final class NhanVienFormDialog extends BaseDialog {
        private final NhanVien editingNhanVien;
        private final JTextField txtHoTenDialog;
        private final AppDatePickerField pickerNgaySinh;
        private final JComboBox<String> cboGioiTinhDialog;
        private final JTextField txtCccdDialog;
        private final JTextField txtSoDienThoaiDialog;
        private final JTextField txtEmailDialog;
        private final JTextArea txtDiaChiDialog;
        private final JComboBox<String> cboChucVuDialog;
        private final JComboBox<String> cboCaLamDialog;
        private final AppDatePickerField pickerNgayVaoLam;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JTextArea txtGhiChuDialog;

        private NhanVienFormDialog(Frame owner, NhanVien nhanVien) {
            super(owner, nhanVien == null ? "Thêm nhân viên" : "Cập nhật nhân viên");
            this.editingNhanVien = nhanVien;

            add(buildHeader(
                    nhanVien == null ? "THÊM NHÂN VIÊN" : "CẬP NHẬT NHÂN VIÊN",
                    "Lưu thông tin hồ sơ nhân viên theo đúng các cột hiện tại của bảng NhanVien."
            ), BorderLayout.NORTH);

            JPanel card = createCardPanel(new BorderLayout());
            JPanel form = createFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);

            txtHoTenDialog = createInputField(nhanVien == null ? "" : safeValue(nhanVien.getHoTen()));
            pickerNgaySinh = new AppDatePickerField(nhanVien == null ? "" : formatDateOrEmpty(nhanVien.getNgaySinh()), true);
            cboGioiTinhDialog = createComboBox(GIOI_TINH_OPTIONS);
            txtCccdDialog = createInputField(nhanVien == null ? "" : safeValue(nhanVien.getCccd()));
            txtSoDienThoaiDialog = createInputField(nhanVien == null ? "" : safeValue(nhanVien.getSoDienThoai()));
            txtEmailDialog = createInputField(nhanVien == null ? "" : safeValue(nhanVien.getEmail()));
            txtDiaChiDialog = createDialogTextArea(3);
            cboChucVuDialog = createComboBox(STAFF_ROLE_OPTIONS);
            cboChucVuDialog.setSelectedItem("Lễ tân");
            cboChucVuDialog.setEnabled(false);
            cboCaLamDialog = createComboBox(CA_LAM_OPTIONS);
            pickerNgayVaoLam = new AppDatePickerField(nhanVien == null ? "" : formatDateOrEmpty(nhanVien.getNgayVaoLam()), true);
            cboTrangThaiDialog = createComboBox(TRANG_THAI_OPTIONS);
            txtGhiChuDialog = createDialogTextArea(3);

            if (nhanVien != null) {
                cboGioiTinhDialog.setSelectedItem(safeValue(nhanVien.getGioiTinh(), GIOI_TINH_OPTIONS[0]));
                txtDiaChiDialog.setText(safeValue(nhanVien.getDiaChi()));
                cboChucVuDialog.setSelectedItem(normalizePersonnelCategory(nhanVien.getChucVu()));
                cboChucVuDialog.setEnabled(false);
                cboCaLamDialog.setSelectedItem(safeValue(nhanVien.getCaLam(), CA_LAM_OPTIONS[0]));
                cboTrangThaiDialog.setSelectedItem(safeValue(nhanVien.getTrangThai(), TRANG_THAI_OPTIONS[0]));
                txtGhiChuDialog.setText(safeValue(nhanVien.getGhiChu()));
            }

            addFormRow(form, gbc, 0, "Họ tên", txtHoTenDialog);
            addFormRow(form, gbc, 1, "Ngày sinh", pickerNgaySinh);
            addFormRow(form, gbc, 2, "Giới tính", cboGioiTinhDialog);
            addFormRow(form, gbc, 3, "CCCD", txtCccdDialog);
            addFormRow(form, gbc, 4, "Số điện thoại", txtSoDienThoaiDialog);
            addFormRow(form, gbc, 5, "Email", txtEmailDialog);
            addFormRow(form, gbc, 6, "Địa chỉ", new JScrollPane(txtDiaChiDialog));
            addFormRow(form, gbc, 7, "Chức vụ", cboChucVuDialog);
            addFormRow(form, gbc, 8, "Ca làm", cboCaLamDialog);
            addFormRow(form, gbc, 9, "Ngày vào làm", pickerNgayVaoLam);
            addFormRow(form, gbc, 10, "Trạng thái", cboTrangThaiDialog);
            addFormRow(form, gbc, 11, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);

            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            JButton btnSave = createPrimaryButton("Lưu", ACCENT_GREEN, Color.WHITE, e -> submit(false));
            if (nhanVien == null) {
                JButton btnSaveAndNew = createOutlineButton("Lưu và tạo mới", ACCENT_BLUE, e -> submit(true));
                add(buildButtons(btnCancel, btnSaveAndNew, btnSave), BorderLayout.SOUTH);
            } else {
                add(buildButtons(btnCancel, btnSave), BorderLayout.SOUTH);
            }

            prepareDialog(owner, 700, 760);
        }

        private void submit(boolean createAnother) {
            NhanVien nhanVien = buildNhanVienFromDialog();
            if (nhanVien == null) {
                return;
            }

            boolean updating = editingNhanVien != null;
            boolean success;
            if (updating) {
                nhanVien.setMaNhanVien(editingNhanVien.getMaNhanVien());
                success = nhanVienDAO.update(nhanVien);
            } else {
                success = nhanVienDAO.insert(nhanVien);
            }

            if (!success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể lưu nhân viên.\nChi tiết: " + safeValue(nhanVienDAO.getLastErrorMessage(), "Không xác định."),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if ("Ngừng làm việc".equalsIgnoreCase(nhanVien.getTrangThai())
                    || "Khóa".equalsIgnoreCase(nhanVien.getTrangThai())) {
                nhanVienDAO.khoaTaiKhoanNeuNhanVienBiNgung(nhanVien.getMaNhanVien(), nhanVien.getTrangThai());
            }

            reloadNhanVien(false, false);
            selectNhanVien(nhanVien.getMaNhanVien());
            JOptionPane.showMessageDialog(
                    this,
                    updating ? "Cập nhật nhân viên thành công." : "Thêm nhân viên thành công.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (createAnother && !updating) {
                resetEditorForm();
                return;
            }
            dispose();
        }

        private NhanVien buildNhanVienFromDialog() {
            String hoTen = txtHoTenDialog.getText().trim();
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            }

            Date ngaySinh = parseRequiredDate(pickerNgaySinh.getText(), "Ngày sinh không đúng định dạng dd/MM/yyyy.");
            if (isInvalidDateMarker(ngaySinh)) {
                return null;
            }

            String cccd = txtCccdDialog.getText().trim();
            if (cccd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "CCCD không được để trống.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (!CCCD_PATTERN.matcher(cccd).matches()) {
                JOptionPane.showMessageDialog(this, "CCCD không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            String soDienThoai = txtSoDienThoaiDialog.getText().trim();
            if (soDienThoai.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (!PHONE_PATTERN.matcher(soDienThoai).matches()) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            String email = txtEmailDialog.getText().trim();
            if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
                JOptionPane.showMessageDialog(this, "Email không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            String chucVu = normalizePersonnelCategory(valueOf(cboChucVuDialog.getSelectedItem()).trim());
            if (chucVu.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chức vụ không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            String caLam = valueOf(cboCaLamDialog.getSelectedItem()).trim();
            if (caLam.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ca làm không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            String trangThai = valueOf(cboTrangThaiDialog.getSelectedItem()).trim();
            if (trangThai.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Trạng thái không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            Date ngayVaoLam = parseRequiredDate(pickerNgayVaoLam.getText(), "Ngày vào làm không đúng định dạng dd/MM/yyyy.");
            if (isInvalidDateMarker(ngayVaoLam)) {
                return null;
            }
            if (ngayVaoLam.before(ngaySinh)) {
                JOptionPane.showMessageDialog(this, "Ngày vào làm phải lớn hơn hoặc bằng ngày sinh.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return new NhanVien(
                    hoTen,
                    ngaySinh,
                    valueOf(cboGioiTinhDialog.getSelectedItem()),
                    cccd,
                    soDienThoai,
                    email,
                    txtDiaChiDialog.getText().trim(),
                    chucVu,
                    chucVu,
                    caLam,
                    ngayVaoLam,
                    trangThai,
                    txtGhiChuDialog.getText().trim()
            );
        }

        private void resetEditorForm() {
            txtHoTenDialog.setText("");
            pickerNgaySinh.setText("");
            cboGioiTinhDialog.setSelectedIndex(0);
            txtCccdDialog.setText("");
            txtSoDienThoaiDialog.setText("");
            txtEmailDialog.setText("");
            txtDiaChiDialog.setText("");
            cboChucVuDialog.setSelectedIndex(0);
            cboCaLamDialog.setSelectedIndex(0);
            pickerNgayVaoLam.setText("");
            cboTrangThaiDialog.setSelectedIndex(0);
            txtGhiChuDialog.setText("");
            txtHoTenDialog.requestFocusInWindow();
        }
    }

    private final class NhanVienViewDialog extends BaseDialog {
        private NhanVienViewDialog(Frame owner, NhanVien nhanVien) {
            super(owner, "Chi tiết nhân viên");

            add(buildHeader(
                    "CHI TIẾT NHÂN VIÊN",
                    "Thông tin hồ sơ nhân viên đang được hiển thị ở chế độ chỉ đọc."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);

            JPanel formCard = createCardPanel(new BorderLayout());
            JPanel form = createFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);

            addFormRow(form, gbc, 0, "Mã nhân viên", createValueLabel(formatEmployeeCode(nhanVien.getMaNhanVien())));
            addFormRow(form, gbc, 1, "Họ tên", createValueLabel(safeValue(nhanVien.getHoTen(), "-")));
            addFormRow(form, gbc, 2, "Ngày sinh", createValueLabel(formatDate(nhanVien.getNgaySinh())));
            addFormRow(form, gbc, 3, "Giới tính", createValueLabel(safeValue(nhanVien.getGioiTinh(), "-")));
            addFormRow(form, gbc, 4, "CCCD", createValueLabel(safeValue(nhanVien.getCccd(), "-")));
            addFormRow(form, gbc, 5, "Số điện thoại", createValueLabel(safeValue(nhanVien.getSoDienThoai(), "-")));
            addFormRow(form, gbc, 6, "Email", createValueLabel(safeValue(nhanVien.getEmail(), "-")));
            addFormRow(form, gbc, 7, "Ch\u1ee9c v\u1ee5", createValueLabel(safeValue(nhanVien.getChucVu(), "-")));
            addFormRow(form, gbc, 8, "Ca l\u00e0m", createValueLabel(safeValue(nhanVien.getCaLam(), "-")));
            addFormRow(form, gbc, 9, "Ng\u00e0y v\u00e0o l\u00e0m", createValueLabel(formatDate(nhanVien.getNgayVaoLam())));
            addFormRow(form, gbc, 10, "Tr\u1ea1ng th\u00e1i", createValueLabel(safeValue(nhanVien.getTrangThai(), "-")));
            addFormRow(form, gbc, 11, "\u0110\u1ecba ch\u1ec9", new JScrollPane(readOnlyDialogArea(safeValue(nhanVien.getDiaChi()))));
            addFormRow(form, gbc, 12, "Ghi ch\u00fa", new JScrollPane(readOnlyDialogArea(safeValue(nhanVien.getGhiChu()))));

            formCard.add(form, BorderLayout.CENTER);
            body.add(formCard, BorderLayout.CENTER);

            add(body, BorderLayout.CENTER);
            add(buildButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            prepareDialog(owner, 700, 700);
        }
    }

    private JTextArea readOnlyDialogArea(String value) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(value);
        return area;
    }

    private final class DatePickerField extends JPanel {
        private final JTextField txtDisplay;
        private final JPopupMenu popup;
        private final CalendarPanel calendarPanel;

        private DatePickerField(Date value) {
            super(new BorderLayout(6, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(240, 34));

            txtDisplay = createInputField(value == null ? "" : formatDateOrEmpty(value));
            txtDisplay.setEditable(false);
            txtDisplay.setBackground(Color.WHITE);
            txtDisplay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            JButton btnCalendar = createOutlineButton("...", ACCENT_BLUE, e -> togglePopup());
            btnCalendar.setPreferredSize(new Dimension(46, 34));

            popup = new JPopupMenu();
            popup.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            calendarPanel = new CalendarPanel(this, value == null ? LocalDate.now() : value.toLocalDate());
            popup.add(calendarPanel);

            MouseAdapter openPopupListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    togglePopup();
                }
            };
            txtDisplay.addMouseListener(openPopupListener);

            add(txtDisplay, BorderLayout.CENTER);
            add(btnCalendar, BorderLayout.EAST);

            if (value != null) {
                calendarPanel.setSelectedDate(value.toLocalDate());
            }
        }

        private void togglePopup() {
            if (popup.isVisible()) {
                popup.setVisible(false);
                return;
            }
            popup.show(this, 0, getHeight());
        }

        private void setDate(Date value) {
            if (value == null) {
                txtDisplay.setText("");
                calendarPanel.setSelectedDate(null);
                return;
            }
            txtDisplay.setText(formatDateOrEmpty(value));
            calendarPanel.setSelectedDate(value.toLocalDate());
        }

        private void setDate(LocalDate value) {
            if (value == null) {
                setDate((Date) null);
                return;
            }
            txtDisplay.setText(value.format(DATE_FORMATTER));
            calendarPanel.setSelectedDate(value);
        }

        private String getText() {
            return txtDisplay.getText().trim();
        }
    }

    private final class CalendarPanel extends JPanel {
        private final DatePickerField owner;
        private final JLabel lblMonth;
        private final JPanel dayGrid;
        private LocalDate selectedDate;
        private YearMonth displayedMonth;

        private CalendarPanel(DatePickerField owner, LocalDate initialDate) {
            super(new BorderLayout(0, 8));
            this.owner = owner;
            this.displayedMonth = YearMonth.from(initialDate);
            this.selectedDate = initialDate;
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnPrev = createOutlineButton("<", BORDER_SOFT, e -> changeMonth(-1));
            JButton btnNext = createOutlineButton(">", BORDER_SOFT, e -> changeMonth(1));
            btnPrev.setPreferredSize(new Dimension(44, 30));
            btnNext.setPreferredSize(new Dimension(44, 30));

            lblMonth = new JLabel("", SwingConstants.CENTER);
            lblMonth.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblMonth.setForeground(TEXT_PRIMARY);

            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setOpaque(false);
            header.add(btnPrev, BorderLayout.WEST);
            header.add(lblMonth, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);

            JPanel weekdayHeader = new JPanel(new GridLayout(1, 7, 6, 6));
            weekdayHeader.setOpaque(false);
            String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (String weekday : weekdays) {
                JLabel lbl = new JLabel(weekday, SwingConstants.CENTER);
                lbl.setFont(LABEL_FONT);
                lbl.setForeground(TEXT_MUTED);
                weekdayHeader.add(lbl);
            }

            dayGrid = new JPanel(new GridLayout(6, 7, 6, 6));
            dayGrid.setOpaque(false);

            add(header, BorderLayout.NORTH);
            add(weekdayHeader, BorderLayout.CENTER);
            add(dayGrid, BorderLayout.SOUTH);
            refreshCalendar();
        }

        private void changeMonth(int offset) {
            displayedMonth = displayedMonth.plusMonths(offset);
            refreshCalendar();
        }

        private void setSelectedDate(LocalDate date) {
            selectedDate = date;
            if (date != null) {
                displayedMonth = YearMonth.from(date);
            }
            refreshCalendar();
        }

        private void refreshCalendar() {
            lblMonth.setText("Tháng " + displayedMonth.atDay(1).format(MONTH_FORMATTER));
            dayGrid.removeAll();

            LocalDate firstDay = displayedMonth.atDay(1);
            int offset = firstDay.getDayOfWeek().getValue() - 1;
            int totalDays = displayedMonth.lengthOfMonth();

            for (int i = 0; i < 42; i++) {
                if (i < offset || i >= offset + totalDays) {
                    JPanel panel = new JPanel();
                    panel.setOpaque(false);
                    dayGrid.add(panel);
                    continue;
                }

                int day = i - offset + 1;
                LocalDate date = displayedMonth.atDay(day);
                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFont(BODY_FONT);
                btnDay.setFocusPainted(false);
                btnDay.setOpaque(true);
                btnDay.setContentAreaFilled(true);
                btnDay.setBorderPainted(true);
                btnDay.setPreferredSize(new Dimension(36, 32));

                boolean selected = selectedDate != null && selectedDate.equals(date);
                if (selected) {
                    btnDay.setBackground(ACCENT_BLUE);
                    btnDay.setForeground(Color.WHITE);
                    btnDay.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1, true));
                } else if (LocalDate.now().equals(date)) {
                    btnDay.setBackground(new Color(219, 234, 254));
                    btnDay.setForeground(TEXT_PRIMARY);
                    btnDay.setBorder(BorderFactory.createLineBorder(new Color(96, 165, 250), 1, true));
                } else {
                    btnDay.setBackground(Color.WHITE);
                    btnDay.setForeground(TEXT_PRIMARY);
                    btnDay.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
                }

                btnDay.addActionListener(e -> {
                    selectedDate = date;
                    owner.setDate(date);
                    owner.popup.setVisible(false);
                });
                dayGrid.add(btnDay);
            }

            dayGrid.revalidate();
            dayGrid.repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NhanVienGUI gui = new NhanVienGUI();
            ScreenUIHelper.prepareFrame(gui, 1450, 850);
            gui.setVisible(true);
        });
    }
}
