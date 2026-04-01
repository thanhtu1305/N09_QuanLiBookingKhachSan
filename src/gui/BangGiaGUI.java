package gui;

import dao.BangGiaDAO;
import dao.ChiTietBangGiaDAO;
import dao.LoaiPhongDAO;
import entity.BangGia;
import entity.ChiTietBangGia;
import entity.LoaiPhong;
import gui.common.AppBranding;
import gui.common.AppDatePickerField;
import gui.common.AppTimePickerField;
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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BangGiaGUI extends JFrame {
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Date INVALID_DATE = Date.valueOf("0001-01-01");
    private static final String[] TRANG_THAI_OPTIONS = {"Đang áp dụng", "Ngừng áp dụng"};
    private static final String[] LOAI_NGAY_OPTIONS = {"Ngày thường", "Cuối tuần", "Ngày lễ"};

    private final BangGiaDAO bangGiaDAO = new BangGiaDAO();
    private final ChiTietBangGiaDAO chiTietBangGiaDAO = new ChiTietBangGiaDAO();
    private final LoaiPhongDAO loaiPhongDAO = new LoaiPhongDAO();

    private final String username;
    private final String role;

    private JPanel rootPanel;
    private final List<LoaiPhong> allLoaiPhong = new ArrayList<LoaiPhong>();
    private final List<BangGia> displayedBangGia = new ArrayList<BangGia>();
    private final List<ChiTietBangGia> displayedChiTiet = new ArrayList<ChiTietBangGia>();

    private JTable tblBangGia;
    private JTable tblChiTietBangGia;
    private DefaultTableModel bangGiaModel;
    private DefaultTableModel chiTietModel;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboLoaiNgay;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;
    private AppDatePickerField txtNgayBatDauFilter;
    private AppDatePickerField txtNgayKetThucFilter;
    private JLabel lblMaBangGia;
    private JLabel lblTenBangGia;
    private JLabel lblLoaiPhongChiTiet;
    private JLabel lblLoaiNgayChiTiet;
    private JLabel lblGiaTheoGio;
    private JLabel lblGiaTheoNgay;
    private JLabel lblGiaCuoiTuan;
    private JLabel lblGiaLe;
    private JLabel lblPhuThu;
    private JLabel lblTrangThaiChiTiet;

    public BangGiaGUI() {
        this("guest", "Lễ tân");
    }

    public BangGiaGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");
        setTitle("Quản lý bảng giá - Hotel PMS");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        loadLoaiPhongOptions();
        reloadBangGiaData(true, false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(SidebarFactory.createSidebar(this, ScreenKey.BANG_GIA, username, role), BorderLayout.WEST);
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
        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ BẢNG GIÁ"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Quản lý bảng giá và chi tiết bảng giá từ dữ liệu SQL Server.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Bảng giá"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("Thêm bảng giá", new Color(22, 163, 74), Color.WHITE, e -> openBangGiaDialog(null)));
        card.add(createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openEditSelectedBangGia()));
        card.add(createPrimaryButton("Ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> updateSelectedStatus()));
        card.add(createPrimaryButton("Xem chi tiết", new Color(99, 102, 241), Color.WHITE, e -> openViewSelectedBangGia()));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        cboLoaiPhong = createComboBox(new String[]{"Tất cả"});
        cboLoaiNgay = createComboBox(new String[]{"Tất cả", "Ngày thường", "Cuối tuần", "Ngày lễ"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Đang áp dụng", "Ngừng áp dụng"});
        txtNgayBatDauFilter = new AppDatePickerField("", false);
        txtNgayKetThucFilter = new AppDatePickerField("", false);
        left.add(createFieldGroup("Loại phòng", cboLoaiPhong));
        left.add(createFieldGroup("Loại ngày", cboLoaiNgay));
        left.add(createFieldGroup("Trạng thái", cboTrangThai));
        left.add(createFieldGroup("Ngày bắt đầu", txtNgayBatDauFilter));
        left.add(createFieldGroup("Ngày kết thúc", txtNgayKetThucFilter));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel lblSearch = new JLabel("Tìm kiếm");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);
        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(260, 34));
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
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTableCard(), buildDetailCard());
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.58);
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }

    private JPanel buildTableCard() {
        bangGiaModel = new DefaultTableModel(new String[]{
                "Mã bảng giá", "Tên bảng giá", "Loại phòng", "Ngày bắt đầu", "Ngày kết thúc", "Loại ngày", "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBangGia = new JTable(bangGiaModel);
        tblBangGia.setFont(BODY_FONT);
        tblBangGia.setRowHeight(32);
        tblBangGia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBangGia.setFillsViewportHeight(true);
        tblBangGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedBangGia();
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblBangGia, this::openEditSelectedBangGia);

        JScrollPane scrollPane = new JScrollPane(tblBangGia);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        JPanel wrapper = createCardPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout(0, 10));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        lblMaBangGia = createValueLabel();
        lblTenBangGia = createValueLabel();
        lblLoaiPhongChiTiet = createValueLabel();
        lblLoaiNgayChiTiet = createValueLabel();
        lblGiaTheoGio = createValueLabel();
        lblGiaTheoNgay = createValueLabel();
        lblGiaCuoiTuan = createValueLabel();
        lblGiaLe = createValueLabel();
        lblPhuThu = createValueLabel();
        lblTrangThaiChiTiet = createValueLabel();

        addDetailRow(body, "Mã bảng giá", lblMaBangGia);
        addDetailRow(body, "Tên bảng giá", lblTenBangGia);
        addDetailRow(body, "Loại phòng", lblLoaiPhongChiTiet);
        addDetailRow(body, "Loại ngày", lblLoaiNgayChiTiet);
        addDetailRow(body, "Giá theo giờ", lblGiaTheoGio);
        addDetailRow(body, "Giá theo ngày", lblGiaTheoNgay);
        addDetailRow(body, "Giá cuối tuần", lblGiaCuoiTuan);
        addDetailRow(body, "Giá lễ", lblGiaLe);
        addDetailRow(body, "Phụ thu", lblPhuThu);
        addDetailRow(body, "Trạng thái", lblTrangThaiChiTiet);

        chiTietModel = new DefaultTableModel(new String[]{
                "Mã CT", "Loại ngày", "Khung giờ", "Giá giờ", "Giá qua đêm", "Giá ngày", "Giá cuối tuần", "Giá lễ", "Phụ thu"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblChiTietBangGia = new JTable(chiTietModel);
        tblChiTietBangGia.setFont(BODY_FONT);
        tblChiTietBangGia.setRowHeight(28);
        tblChiTietBangGia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane detailScroll = new JScrollPane(tblChiTietBangGia);
        detailScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        detailScroll.setPreferredSize(new Dimension(0, 250));

        JPanel action = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        action.setOpaque(false);
        action.add(createPrimaryButton("Thêm CT", new Color(59, 130, 246), Color.WHITE, e -> openChiTietDialog(null)));
        action.add(createOutlineButton("Sửa CT", new Color(245, 158, 11), e -> openEditSelectedChiTiet()));
        action.add(createOutlineButton("Xóa CT", new Color(220, 38, 38), e -> deleteSelectedChiTiet()));

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(action, BorderLayout.NORTH);
        south.add(detailScroll, BorderLayout.CENTER);

        card.add(new JLabel("Chi tiết bảng giá"), BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG, BORDER_SOFT, TEXT_MUTED,
                "F1 Thêm bảng giá", "F2 Cập nhật", "F3 Ngừng áp dụng", "F4 Xem chi tiết", "Enter Cập nhật"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "banggia-f1", () -> openBangGiaDialog(null));
        ScreenUIHelper.registerShortcut(this, "F2", "banggia-f2", this::openEditSelectedBangGia);
        ScreenUIHelper.registerShortcut(this, "F3", "banggia-f3", this::updateSelectedStatus);
        ScreenUIHelper.registerShortcut(this, "F4", "banggia-f4", this::openViewSelectedBangGia);
        ScreenUIHelper.registerShortcut(this, "ENTER", "banggia-enter", this::openEditSelectedBangGia);
    }

    private void loadLoaiPhongOptions() {
        allLoaiPhong.clear();
        allLoaiPhong.addAll(loaiPhongDAO.getAll());
        String selected = valueOf(cboLoaiPhong.getSelectedItem());
        cboLoaiPhong.removeAllItems();
        cboLoaiPhong.addItem("Tất cả");
        for (LoaiPhong loaiPhong : allLoaiPhong) {
            cboLoaiPhong.addItem(loaiPhong.getTenLoaiPhong());
        }
        if (!selected.isEmpty()) {
            cboLoaiPhong.setSelectedItem(selected);
        }
    }

    private void reloadBangGiaData(boolean resetFilter, boolean showMessage) {
        if (resetFilter) {
            cboLoaiPhong.setSelectedIndex(0);
            cboLoaiNgay.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            txtTuKhoa.setText("");
            txtNgayBatDauFilter.setText("");
            txtNgayKetThucFilter.setText("");
        }
        applyFilters(false);
        refreshCurrentView();
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu bảng giá.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void applyFilters(boolean showMessage) {
        Date from = parseOptionalDate(txtNgayBatDauFilter.getText().trim(), "Ngày bắt đầu không đúng định dạng dd/MM/yyyy.");
        if (isInvalidDateMarker(from)) {
            return;
        }
        Date to = parseOptionalDate(txtNgayKetThucFilter.getText().trim(), "Ngày kết thúc không đúng định dạng dd/MM/yyyy.");
        if (isInvalidDateMarker(to)) {
            return;
        }
        if (from != null && to != null && from.after(to)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoaiPhong loaiPhong = findLoaiPhongByTen(valueOf(cboLoaiPhong.getSelectedItem()));
        String maLoaiPhong = loaiPhong == null ? "" : String.valueOf(loaiPhong.getMaLoaiPhong());
        String loaiNgay = "Tất cả".equals(valueOf(cboLoaiNgay.getSelectedItem())) ? "" : valueOf(cboLoaiNgay.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());

        displayedBangGia.clear();
        for (BangGia bangGia : bangGiaDAO.search(txtTuKhoa.getText().trim(), maLoaiPhong, from, to, loaiNgay)) {
            if (!"Tất cả".equals(trangThai) && !safeValue(bangGia.getTrangThai(), "").equals(trangThai)) {
                continue;
            }
            displayedBangGia.add(bangGia);
        }

        refillBangGiaTable();
        refreshCurrentView();
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "Đã lọc được " + displayedBangGia.size() + " bảng giá phù hợp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void refillBangGiaTable() {
        bangGiaModel.setRowCount(0);
        for (BangGia bangGia : displayedBangGia) {
            bangGiaModel.addRow(new Object[]{
                    formatBangGiaCode(bangGia.getMaBangGia()),
                    bangGia.getTenBangGia(),
                    bangGia.getTenLoaiPhong(),
                    formatDate(bangGia.getTuNgay()),
                    formatDate(bangGia.getDenNgay()),
                    bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia()),
                    bangGia.getTrangThai()
            });
        }
        if (!displayedBangGia.isEmpty()) {
            tblBangGia.setRowSelectionInterval(0, 0);
            showSelectedBangGia();
        } else {
            clearDetailPanel();
        }
    }

    private void showSelectedBangGia() {
        BangGia bangGia = getSelectedBangGia(false);
        if (bangGia == null) {
            clearDetailPanel();
            refreshCurrentView();
            return;
        }
        displayedChiTiet.clear();
        displayedChiTiet.addAll(chiTietBangGiaDAO.getByMaBangGia(bangGia.getMaBangGia()));

        lblMaBangGia.setText(formatBangGiaCode(bangGia.getMaBangGia()));
        lblTenBangGia.setText(safeValue(bangGia.getTenBangGia(), "-"));
        lblLoaiPhongChiTiet.setText(safeValue(bangGia.getTenLoaiPhong(), "-"));
        lblLoaiNgayChiTiet.setText(safeValue(bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia()), "-"));
        lblGiaTheoGio.setText(formatCurrency(findFirstPositive(displayedChiTiet, 0)));
        lblGiaTheoNgay.setText(formatCurrency(findFirstPositive(displayedChiTiet, 1)));
        lblGiaCuoiTuan.setText(formatCurrency(findFirstPositive(displayedChiTiet, 2)));
        lblGiaLe.setText(formatCurrency(findFirstPositive(displayedChiTiet, 3)));
        lblPhuThu.setText(formatCurrency(findFirstPositive(displayedChiTiet, 4)));
        lblTrangThaiChiTiet.setText(safeValue(bangGia.getTrangThai(), "-"));
        refillChiTietTable();
        refreshCurrentView();
    }

    private void refillChiTietTable() {
        chiTietModel.setRowCount(0);
        for (ChiTietBangGia chiTiet : displayedChiTiet) {
            chiTietModel.addRow(new Object[]{
                    "CT" + chiTiet.getMaChiTietBangGia(),
                    chiTiet.getLoaiNgay(),
                    chiTiet.getKhungGio(),
                    formatCurrency(chiTiet.getGiaTheoGio()),
                    formatCurrency(chiTiet.getGiaQuaDem()),
                    formatCurrency(chiTiet.getGiaTheoNgay()),
                    formatCurrency(chiTiet.getGiaCuoiTuan()),
                    formatCurrency(chiTiet.getGiaLe()),
                    formatCurrency(chiTiet.getPhuThu())
            });
        }
        refreshCurrentView();
    }

    private void clearDetailPanel() {
        lblMaBangGia.setText("-");
        lblTenBangGia.setText("-");
        lblLoaiPhongChiTiet.setText("-");
        lblLoaiNgayChiTiet.setText("-");
        lblGiaTheoGio.setText("-");
        lblGiaTheoNgay.setText("-");
        lblGiaCuoiTuan.setText("-");
        lblGiaLe.setText("-");
        lblPhuThu.setText("-");
        lblTrangThaiChiTiet.setText("-");
        displayedChiTiet.clear();
        refillChiTietTable();
        refreshCurrentView();
    }

    private BangGia getSelectedBangGia(boolean showMessage) {
        int row = tblBangGia.getSelectedRow();
        if (row < 0 || row >= displayedBangGia.size()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bảng giá.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        return displayedBangGia.get(row);
    }

    private ChiTietBangGia getSelectedChiTiet(boolean showMessage) {
        int row = tblChiTietBangGia.getSelectedRow();
        if (row < 0 || row >= displayedChiTiet.size()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một chi tiết bảng giá.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        return displayedChiTiet.get(row);
    }

    private void selectBangGia(int maBangGia) {
        for (int i = 0; i < displayedBangGia.size(); i++) {
            if (displayedBangGia.get(i).getMaBangGia() == maBangGia) {
                tblBangGia.setRowSelectionInterval(i, i);
                showSelectedBangGia();
                refreshCurrentView();
                return;
            }
        }
        clearDetailPanel();
        refreshCurrentView();
    }

    private void refreshCurrentView() {
        if (rootPanel != null) {
            rootPanel.revalidate();
            rootPanel.repaint();
        }
        if (tblBangGia != null) {
            tblBangGia.revalidate();
            tblBangGia.repaint();
        }
        if (tblChiTietBangGia != null) {
            tblChiTietBangGia.revalidate();
            tblChiTietBangGia.repaint();
        }
    }

    private void openBangGiaDialog(BangGia bangGia) {
        new BangGiaFormDialog(this, bangGia).setVisible(true);
    }

    private void openEditSelectedBangGia() {
        BangGia bangGia = getSelectedBangGia(true);
        if (bangGia != null) {
            openBangGiaDialog(bangGia);
        }
    }

    private void openViewSelectedBangGia() {
        BangGia bangGia = getSelectedBangGia(true);
        if (bangGia != null) {
            new BangGiaViewDialog(this, bangGia).setVisible(true);
        }
    }

    private void updateSelectedStatus() {
        BangGia bangGia = getSelectedBangGia(true);
        if (bangGia == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Chuyển trạng thái bảng giá này sang \"Ngừng áp dụng\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String loaiNgay = bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia());
        bangGia.setTrangThai("Ngừng áp dụng");
        if (bangGiaDAO.update(bangGia, loaiNgay)) {
            reloadBangGiaData(false, false);
            selectBangGia(bangGia.getMaBangGia());
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái bảng giá.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái bảng giá.\nChi tiết: " + safeValue(bangGiaDAO.getLastErrorMessage(), "Không xác định."), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openChiTietDialog(ChiTietBangGia chiTiet) {
        BangGia bangGia = getSelectedBangGia(true);
        if (bangGia != null) {
            new ChiTietBangGiaFormDialog(this, bangGia, chiTiet).setVisible(true);
        }
    }

    private void openEditSelectedChiTiet() {
        ChiTietBangGia chiTiet = getSelectedChiTiet(true);
        if (chiTiet != null) {
            openChiTietDialog(chiTiet);
        }
    }

    private void deleteSelectedChiTiet() {
        ChiTietBangGia chiTiet = getSelectedChiTiet(true);
        if (chiTiet == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa chi tiết bảng giá đang chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (chiTietBangGiaDAO.delete(chiTiet.getMaChiTietBangGia())) {
            reloadBangGiaData(false, false);
            selectBangGia(chiTiet.getMaBangGia());
            JOptionPane.showMessageDialog(this, "Đã xóa chi tiết bảng giá.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể xóa chi tiết bảng giá.\nChi tiết: " + safeValue(chiTietBangGiaDAO.getLastErrorMessage(), "Không xác định."), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double findFirstPositive(List<ChiTietBangGia> list, int type) {
        for (ChiTietBangGia chiTiet : list) {
            double value;
            if (type == 0) {
                value = chiTiet.getGiaTheoGio();
            } else if (type == 1) {
                value = chiTiet.getGiaTheoNgay();
            } else if (type == 2) {
                value = chiTiet.getGiaCuoiTuan();
            } else if (type == 3) {
                value = chiTiet.getGiaLe();
            } else {
                value = chiTiet.getPhuThu();
            }
            if (value > 0) {
                return value;
            }
        }
        return 0;
    }

    private String formatCurrency(double amount) {
        return amount <= 0 ? "-" : String.format(Locale.US, "%,.0f", amount).replace(',', '.');
    }

    private String formatDate(Date date) {
        return date == null ? "" : date.toLocalDate().format(DATE_FORMATTER);
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

    private Date parseOptionalDate(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            return null;
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

    private double parseMoney(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            double parsed = Double.parseDouble(value.trim().replace(".", ""));
            if (parsed < 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, fieldName + " phải là số lớn hơn hoặc bằng 0.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private String formatBangGiaCode(int maBangGia) {
        return "BG" + maBangGia;
    }

    private LoaiPhong findLoaiPhongByTen(String tenLoaiPhong) {
        for (LoaiPhong loaiPhong : allLoaiPhong) {
            if (safeValue(loaiPhong.getTenLoaiPhong(), "").equals(tenLoaiPhong)) {
                return loaiPhong;
            }
        }
        return null;
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
        comboBox.setPreferredSize(new Dimension(150, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(150, 34));
        return field;
    }

    private JButton createPrimaryButton(String text, Color background, Color foreground, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(foreground);
        button.setBackground(background);
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
        return label;
    }

    private void addDetailRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(120, 20));
        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
    }

    private abstract class BaseDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseDialog(Frame owner, String title, int width, int height) {
            super(ScreenUIHelper.resolveDialogOwner(owner), title, true);
            this.minimumWidth = width;
            this.minimumHeight = height;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            getContentPane().setBackground(APP_BG);
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));
            setLayout(new BorderLayout(0, 12));
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible) {
                ScreenUIHelper.prepareDialog(this, getOwner(), minimumWidth, minimumHeight);
            }
            super.setVisible(visible);
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
    }

    private final class BangGiaFormDialog extends BaseDialog {
        private final BangGia editingBangGia;
        private final JTextField txtTenBangGia;
        private final JComboBox<String> cboLoaiPhongDialog;
        private final AppDatePickerField txtNgayBatDau;
        private final AppDatePickerField txtNgayKetThuc;
        private final JComboBox<String> cboLoaiNgayDialog;
        private final JComboBox<String> cboTrangThaiDialog;

        private BangGiaFormDialog(Frame owner, BangGia bangGia) {
            super(owner, bangGia == null ? "Thêm bảng giá" : "Cập nhật bảng giá", 620, 460);
            this.editingBangGia = bangGia;

            add(buildHeader(
                    bangGia == null ? "THÊM BẢNG GIÁ" : "CẬP NHẬT BẢNG GIÁ",
                    "Lưu thông tin bảng giá theo đúng cột hiện tại của database."
            ), BorderLayout.NORTH);

            JPanel card = createCardPanel(new BorderLayout());
            JPanel form = createFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);

            txtTenBangGia = createInputField(bangGia == null ? "" : safeValue(bangGia.getTenBangGia(), ""));
            cboLoaiPhongDialog = createComboBox(new String[]{});
            for (LoaiPhong loaiPhong : allLoaiPhong) {
                cboLoaiPhongDialog.addItem(loaiPhong.getTenLoaiPhong());
            }
            if (bangGia != null) {
                cboLoaiPhongDialog.setSelectedItem(bangGia.getTenLoaiPhong());
            }

            txtNgayBatDau = new AppDatePickerField(bangGia == null ? "" : formatDate(bangGia.getTuNgay()), true);
            txtNgayKetThuc = new AppDatePickerField(bangGia == null ? "" : formatDate(bangGia.getDenNgay()), true);
            cboLoaiNgayDialog = createComboBox(LOAI_NGAY_OPTIONS);
            if (bangGia != null) {
                cboLoaiNgayDialog.setSelectedItem(bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia()));
            }
            cboTrangThaiDialog = createComboBox(TRANG_THAI_OPTIONS);
            if (bangGia != null) {
                cboTrangThaiDialog.setSelectedItem(bangGia.getTrangThai());
            }

            addFormRow(form, gbc, 0, "Tên bảng giá", txtTenBangGia);
            addFormRow(form, gbc, 1, "Loại phòng", cboLoaiPhongDialog);
            addFormRow(form, gbc, 2, "Ngày bắt đầu", txtNgayBatDau);
            addFormRow(form, gbc, 3, "Ngày kết thúc", txtNgayKetThuc);
            addFormRow(form, gbc, 4, "Loại ngày", cboLoaiNgayDialog);
            addFormRow(form, gbc, 5, "Trạng thái", cboTrangThaiDialog);

            card.add(form, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
            add(buildButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> saveBangGia())
            ), BorderLayout.SOUTH);
        }

        private void saveBangGia() {
            if (txtTenBangGia.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên bảng giá không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LoaiPhong loaiPhong = findLoaiPhongByTen(valueOf(cboLoaiPhongDialog.getSelectedItem()));
            if (loaiPhong == null) {
                JOptionPane.showMessageDialog(this, "Loại phòng không hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date ngayBatDau = parseRequiredDate(txtNgayBatDau.getText().trim(), "Ngày bắt đầu không đúng định dạng dd/MM/yyyy.");
            if (isInvalidDateMarker(ngayBatDau)) {
                return;
            }
            Date ngayKetThuc = parseRequiredDate(txtNgayKetThuc.getText().trim(), "Ngày kết thúc không đúng định dạng dd/MM/yyyy.");
            if (isInvalidDateMarker(ngayKetThuc)) {
                return;
            }
            if (ngayBatDau.after(ngayKetThuc)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String loaiNgay = valueOf(cboLoaiNgayDialog.getSelectedItem());
            if (loaiNgay.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Loại ngày không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            if (trangThai.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Trạng thái không được rỗng.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BangGia bangGia = editingBangGia == null ? new BangGia() : editingBangGia;
            bangGia.setTenBangGia(txtTenBangGia.getText().trim());
            bangGia.setMaLoaiPhong(loaiPhong.getMaLoaiPhong());
            bangGia.setTenLoaiPhong(loaiPhong.getTenLoaiPhong());
            bangGia.setTuNgay(ngayBatDau);
            bangGia.setDenNgay(ngayKetThuc);
            bangGia.setTrangThai(trangThai);

            boolean success = editingBangGia == null ? bangGiaDAO.insert(bangGia, loaiNgay) : bangGiaDAO.update(bangGia, loaiNgay);
            if (!success) {
                JOptionPane.showMessageDialog(this, "Không thể lưu bảng giá.\nChi tiết: " + safeValue(bangGiaDAO.getLastErrorMessage(), "Không xác định."), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            reloadBangGiaData(false, false);
            selectBangGia(bangGia.getMaBangGia());
            JOptionPane.showMessageDialog(this, editingBangGia == null ? "Thêm bảng giá thành công." : "Cập nhật bảng giá thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private final class ChiTietBangGiaFormDialog extends BaseDialog {
        private final BangGia bangGia;
        private final ChiTietBangGia editingChiTiet;
        private final JComboBox<String> cboLoaiNgayDialog;
        private final AppTimePickerField txtKhungGio;
        private final JTextField txtGiaTheoGio;
        private final JTextField txtGiaQuaDem;
        private final JTextField txtGiaTheoNgay;
        private final JTextField txtGiaCuoiTuan;
        private final JTextField txtGiaLe;
        private final JTextField txtPhuThu;

        private ChiTietBangGiaFormDialog(Frame owner, BangGia bangGia, ChiTietBangGia chiTiet) {
            super(owner, chiTiet == null ? "Thêm chi tiết bảng giá" : "Cập nhật chi tiết bảng giá", 620, 520);
            this.bangGia = bangGia;
            this.editingChiTiet = chiTiet;

            add(buildHeader(
                    chiTiet == null ? "THÊM CHI TIẾT BẢNG GIÁ" : "CẬP NHẬT CHI TIẾT BẢNG GIÁ",
                    "Khai báo giá chi tiết theo ChiTietBangGia."
            ), BorderLayout.NORTH);

            JPanel card = createCardPanel(new BorderLayout());
            JPanel form = createFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);

            cboLoaiNgayDialog = createComboBox(LOAI_NGAY_OPTIONS);
            txtKhungGio = new AppTimePickerField(chiTiet == null ? "" : safeValue(chiTiet.getKhungGio(), ""), true);
            txtGiaTheoGio = createInputField(chiTiet == null ? "" : String.valueOf((long) chiTiet.getGiaTheoGio()));
            txtGiaQuaDem = createInputField(chiTiet == null ? "" : String.valueOf((long) chiTiet.getGiaQuaDem()));
            txtGiaTheoNgay = createInputField(chiTiet == null ? "" : String.valueOf((long) chiTiet.getGiaTheoNgay()));
            txtGiaCuoiTuan = createInputField(chiTiet == null ? "" : String.valueOf((long) chiTiet.getGiaCuoiTuan()));
            txtGiaLe = createInputField(chiTiet == null ? "" : String.valueOf((long) chiTiet.getGiaLe()));
            txtPhuThu = createInputField(chiTiet == null ? "" : String.valueOf((long) chiTiet.getPhuThu()));
            if (chiTiet != null) {
                cboLoaiNgayDialog.setSelectedItem(chiTiet.getLoaiNgay());
            }

            addFormRow(form, gbc, 0, "Loại ngày", cboLoaiNgayDialog);
            addFormRow(form, gbc, 1, "Khung giờ", txtKhungGio);
            addFormRow(form, gbc, 2, "Giá theo giờ", txtGiaTheoGio);
            addFormRow(form, gbc, 3, "Giá qua đêm", txtGiaQuaDem);
            addFormRow(form, gbc, 4, "Giá theo ngày", txtGiaTheoNgay);
            addFormRow(form, gbc, 5, "Giá cuối tuần", txtGiaCuoiTuan);
            addFormRow(form, gbc, 6, "Giá lễ", txtGiaLe);
            addFormRow(form, gbc, 7, "Phụ thu", txtPhuThu);

            card.add(form, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
            add(buildButtons(
                    createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Lưu", new Color(59, 130, 246), Color.WHITE, e -> saveChiTiet())
            ), BorderLayout.SOUTH);
        }

        private void saveChiTiet() {
            if (valueOf(cboLoaiNgayDialog.getSelectedItem()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Loại ngày không được để trống.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtKhungGio.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khung giờ không được để trống.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtKhungGio.getTimeValue() == null) {
                JOptionPane.showMessageDialog(this, "Khung giờ không đúng định dạng HH:mm.", "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double giaTheoGio = parseMoney(txtGiaTheoGio.getText().trim(), "Giá theo giờ");
            double giaQuaDem = parseMoney(txtGiaQuaDem.getText().trim(), "Giá qua đêm");
            double giaTheoNgay = parseMoney(txtGiaTheoNgay.getText().trim(), "Giá theo ngày");
            double giaCuoiTuan = parseMoney(txtGiaCuoiTuan.getText().trim(), "Giá cuối tuần");
            double giaLe = parseMoney(txtGiaLe.getText().trim(), "Giá lễ");
            double phuThu = parseMoney(txtPhuThu.getText().trim(), "Phụ thu");
            if (giaTheoGio < 0 || giaQuaDem < 0 || giaTheoNgay < 0 || giaCuoiTuan < 0 || giaLe < 0 || phuThu < 0) {
                return;
            }

            ChiTietBangGia chiTiet = editingChiTiet == null ? new ChiTietBangGia() : editingChiTiet;
            chiTiet.setMaBangGia(bangGia.getMaBangGia());
            chiTiet.setLoaiNgay(valueOf(cboLoaiNgayDialog.getSelectedItem()));
            chiTiet.setKhungGio(txtKhungGio.getText().trim());
            chiTiet.setGiaTheoGio(giaTheoGio);
            chiTiet.setGiaQuaDem(giaQuaDem);
            chiTiet.setGiaTheoNgay(giaTheoNgay);
            chiTiet.setGiaCuoiTuan(giaCuoiTuan);
            chiTiet.setGiaLe(giaLe);
            chiTiet.setPhuThu(phuThu);

            boolean success = editingChiTiet == null ? chiTietBangGiaDAO.insert(chiTiet) : chiTietBangGiaDAO.update(chiTiet);
            if (!success) {
                JOptionPane.showMessageDialog(this, "Không thể lưu chi tiết bảng giá.\nChi tiết: " + safeValue(chiTietBangGiaDAO.getLastErrorMessage(), "Không xác định."), "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            reloadBangGiaData(false, false);
            selectBangGia(bangGia.getMaBangGia());
            JOptionPane.showMessageDialog(this, editingChiTiet == null ? "Thêm chi tiết bảng giá thành công." : "Cập nhật chi tiết bảng giá thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private final class BangGiaViewDialog extends BaseDialog {
        private BangGiaViewDialog(Frame owner, BangGia bangGia) {
            super(owner, "Xem chi tiết bảng giá", 860, 620);
            add(buildHeader("XEM CHI TIẾT BẢNG GIÁ", "Dữ liệu bảng giá đang lấy trực tiếp từ database."), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            JPanel headerCard = createCardPanel(new BorderLayout());
            JPanel form = createFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            addFormRow(form, gbc, 0, "Mã bảng giá", new JLabel(formatBangGiaCode(bangGia.getMaBangGia())));
            addFormRow(form, gbc, 1, "Tên bảng giá", new JLabel(safeValue(bangGia.getTenBangGia(), "-")));
            addFormRow(form, gbc, 2, "Loại phòng", new JLabel(safeValue(bangGia.getTenLoaiPhong(), "-")));
            addFormRow(form, gbc, 3, "Ngày bắt đầu", new JLabel(formatDate(bangGia.getTuNgay())));
            addFormRow(form, gbc, 4, "Ngày kết thúc", new JLabel(formatDate(bangGia.getDenNgay())));
            addFormRow(form, gbc, 5, "Loại ngày", new JLabel(safeValue(bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia()), "-")));
            addFormRow(form, gbc, 6, "Trạng thái", new JLabel(safeValue(bangGia.getTrangThai(), "-")));
            headerCard.add(form, BorderLayout.CENTER);

            DefaultTableModel model = new DefaultTableModel(new String[]{
                    "Mã CT", "Loại ngày", "Khung giờ", "Giá giờ", "Giá qua đêm", "Giá ngày", "Giá cuối tuần", "Giá lễ", "Phụ thu"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setFont(BODY_FONT);
            table.setRowHeight(28);
            for (ChiTietBangGia chiTiet : chiTietBangGiaDAO.getByMaBangGia(bangGia.getMaBangGia())) {
                model.addRow(new Object[]{
                        "CT" + chiTiet.getMaChiTietBangGia(), chiTiet.getLoaiNgay(), chiTiet.getKhungGio(),
                        formatCurrency(chiTiet.getGiaTheoGio()), formatCurrency(chiTiet.getGiaQuaDem()),
                        formatCurrency(chiTiet.getGiaTheoNgay()), formatCurrency(chiTiet.getGiaCuoiTuan()),
                        formatCurrency(chiTiet.getGiaLe()), formatCurrency(chiTiet.getPhuThu())
                });
            }
            JPanel detailCard = createCardPanel(new BorderLayout());
            detailCard.add(new JScrollPane(table), BorderLayout.CENTER);

            body.add(headerCard, BorderLayout.NORTH);
            body.add(detailCard, BorderLayout.CENTER);
            add(body, BorderLayout.CENTER);
            add(buildButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BangGiaGUI gui = new BangGiaGUI();
            ScreenUIHelper.prepareFrame(gui, 1400, 820);
            gui.setVisible(true);
        });
    }
}
