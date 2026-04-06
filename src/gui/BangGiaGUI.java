package gui;

import dao.BangGiaDAO;
import dao.ChiTietBangGiaDAO;
import dao.LoaiPhongDAO;
import entity.BangGia;
import entity.BangGiaConflictInfo;
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
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final Color CONFLICT_BG = new Color(254, 242, 242);
    private static final Color CONFLICT_BORDER = new Color(239, 68, 68);
    private static final Color CONFLICT_TEXT = new Color(185, 28, 28);
    private static final Color CONFLICT_ROW_BG = new Color(254, 226, 226);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Date INVALID_DATE = Date.valueOf("0001-01-01");
    private static final String[] TRANG_THAI_OPTIONS = {"Äang Ă¡p dá»¥ng", "Ngá»«ng Ă¡p dá»¥ng"};
    private static final String[] LOAI_NGAY_OPTIONS = {"NgĂ y thÆ°á»ng", "Cuá»‘i tuáº§n", "NgĂ y lá»…"};

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
    private Integer highlightedConflictBangGiaId;

    public BangGiaGUI() {
        this("guest", "Lá»… tĂ¢n");
    }

    public BangGiaGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lá»… tĂ¢n");
        setTitle("Quáº£n lĂ½ báº£ng giĂ¡ - Hotel PMS");
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
        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUáº¢N LĂ Báº¢NG GIĂ"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        JLabel lblSub = new JLabel("Quáº£n lĂ½ báº£ng giĂ¡ vĂ  chi tiáº¿t báº£ng giĂ¡ tá»« dá»¯ liá»‡u SQL Server.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(TEXT_MUTED);
        JLabel lblMeta = new JLabel("NgÆ°á»i dĂ¹ng: " + username + " | Vai trĂ²: " + role);
        lblMeta.setFont(BODY_FONT);
        lblMeta.setForeground(TEXT_MUTED);
        left.add(lblTitle);
        left.add(Box.createVerticalStrut(6));
        left.add(lblSub);
        left.add(Box.createVerticalStrut(6));
        left.add(lblMeta);
        card.add(left, BorderLayout.WEST);
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "mĂ n hĂ¬nh Báº£ng giĂ¡"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        card.add(createPrimaryButton("ThĂªm báº£ng giĂ¡", new Color(22, 163, 74), Color.WHITE, e -> openBangGiaDialog(null)));
        card.add(createPrimaryButton("Ngá»«ng Ă¡p dá»¥ng", new Color(245, 158, 11), TEXT_PRIMARY, e -> updateSelectedStatus()));
        card.add(createPrimaryButton("Xem chi tiáº¿t", new Color(99, 102, 241), Color.WHITE, e -> openViewSelectedBangGia()));
        card.add(createPrimaryButton("TĂ¬m kiáº¿m", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        cboLoaiPhong = createComboBox(new String[]{"Táº¥t cáº£"});
        cboLoaiNgay = createComboBox(new String[]{"Táº¥t cáº£", "NgĂ y thÆ°á»ng", "Cuá»‘i tuáº§n", "NgĂ y lá»…"});
        cboTrangThai = createComboBox(new String[]{"Táº¥t cáº£", "Äang Ă¡p dá»¥ng", "Ngá»«ng Ă¡p dá»¥ng"});
        txtNgayBatDauFilter = new AppDatePickerField("", false);
        txtNgayKetThucFilter = new AppDatePickerField("", false);
        left.add(createFieldGroup("Loáº¡i phĂ²ng", cboLoaiPhong));
        left.add(createFieldGroup("Loáº¡i ngĂ y", cboLoaiNgay));
        left.add(createFieldGroup("Tráº¡ng thĂ¡i", cboTrangThai));
        left.add(createFieldGroup("NgĂ y báº¯t Ä‘áº§u", txtNgayBatDauFilter));
        left.add(createFieldGroup("NgĂ y káº¿t thĂºc", txtNgayKetThucFilter));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel lblSearch = new JLabel("TĂ¬m kiáº¿m");
        lblSearch.setFont(LABEL_FONT);
        lblSearch.setForeground(TEXT_MUTED);
        right.add(lblSearch);
        right.add(Box.createVerticalStrut(4));
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(260, 34));
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> applyFilters(false));
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(txtTuKhoa, BorderLayout.CENTER);
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
                "MĂ£ báº£ng giĂ¡", "TĂªn báº£ng giĂ¡", "Loáº¡i phĂ²ng", "NgĂ y báº¯t Ä‘áº§u", "NgĂ y káº¿t thĂºc", "Loáº¡i ngĂ y", "Tráº¡ng thĂ¡i"
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
        tblBangGia.setGridColor(BORDER_SOFT);
        tblBangGia.setShowGrid(true);
        tblBangGia.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblBangGia);
        tblBangGia.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                } else if (isConflictBangGiaRow(row)) {
                    component.setBackground(CONFLICT_ROW_BG);
                    component.setForeground(TEXT_PRIMARY);
                } else {
                    component.setBackground(Color.WHITE);
                    component.setForeground(TEXT_PRIMARY);
                }
                return component;
            }
        });
        tblBangGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedBangGia();
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblBangGia, this::openEditSelectedBangGia);

        JScrollPane scrollPane = new JScrollPane(tblBangGia);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel wrapper = createCardPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
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

        addDetailRow(body, "MĂ£ báº£ng giĂ¡", lblMaBangGia);
        addDetailRow(body, "TĂªn báº£ng giĂ¡", lblTenBangGia);
        addDetailRow(body, "Loáº¡i phĂ²ng", lblLoaiPhongChiTiet);
        addDetailRow(body, "Loáº¡i ngĂ y", lblLoaiNgayChiTiet);
        addDetailRow(body, "GiĂ¡ theo giá»", lblGiaTheoGio);
        addDetailRow(body, "GiĂ¡ theo ngĂ y", lblGiaTheoNgay);
        addDetailRow(body, "GiĂ¡ cuá»‘i tuáº§n", lblGiaCuoiTuan);
        addDetailRow(body, "GiĂ¡ lá»…", lblGiaLe);
        addDetailRow(body, "Phá»¥ thu", lblPhuThu);
        addDetailRow(body, "Tráº¡ng thĂ¡i", lblTrangThaiChiTiet);

        chiTietModel = new DefaultTableModel(new String[]{
                "MĂ£ CT", "Loáº¡i ngĂ y", "Khung giá»", "GiĂ¡ giá»", "GiĂ¡ qua Ä‘Ăªm", "GiĂ¡ ngĂ y", "GiĂ¡ cuá»‘i tuáº§n", "GiĂ¡ lá»…", "Phá»¥ thu"
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
        tblChiTietBangGia.setGridColor(BORDER_SOFT);
        tblChiTietBangGia.setShowGrid(true);
        tblChiTietBangGia.setFillsViewportHeight(true);
        ScreenUIHelper.styleTableHeader(tblChiTietBangGia);

        JScrollPane detailScroll = new JScrollPane(tblChiTietBangGia);
        detailScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        detailScroll.setPreferredSize(new Dimension(0, 250));
        detailScroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel action = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        action.setOpaque(false);
        action.add(createPrimaryButton("ThĂªm CT", new Color(59, 130, 246), Color.WHITE, e -> openChiTietDialog(null)));
        action.add(createOutlineButton("Sá»­a CT", new Color(245, 158, 11), e -> openEditSelectedChiTiet()));
        action.add(createOutlineButton("XĂ³a CT", new Color(220, 38, 38), e -> deleteSelectedChiTiet()));

        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(action, BorderLayout.NORTH);
        south.add(detailScroll, BorderLayout.CENTER);

        card.add(new JLabel("Chi tiáº¿t báº£ng giĂ¡"), BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFooter() {
        return ScreenUIHelper.createShortcutBar(
                CARD_BG, BORDER_SOFT, TEXT_MUTED,
                "F1 ThĂªm báº£ng giĂ¡", "F3 Ngá»«ng Ă¡p dá»¥ng", "F4 Xem chi tiáº¿t"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "banggia-f1", () -> openBangGiaDialog(null));
        ScreenUIHelper.registerShortcut(this, "F3", "banggia-f3", this::updateSelectedStatus);
        ScreenUIHelper.registerShortcut(this, "F4", "banggia-f4", this::openViewSelectedBangGia);
    }

    private void loadLoaiPhongOptions() {
        allLoaiPhong.clear();
        allLoaiPhong.addAll(loaiPhongDAO.getAll());
        String selected = valueOf(cboLoaiPhong.getSelectedItem());
        cboLoaiPhong.removeAllItems();
        cboLoaiPhong.addItem("Táº¥t cáº£");
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
            JOptionPane.showMessageDialog(this, "ÄĂ£ lĂ m má»›i dá»¯ liá»‡u báº£ng giĂ¡.", "ThĂ´ng bĂ¡o", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void applyFilters(boolean showMessage) {
        Date from = parseOptionalDate(txtNgayBatDauFilter.getText().trim(), "NgĂ y báº¯t Ä‘áº§u khĂ´ng Ä‘Ăºng Ä‘á»‹nh dáº¡ng dd/MM/yyyy.");
        if (isInvalidDateMarker(from)) {
            return;
        }
        Date to = parseOptionalDate(txtNgayKetThucFilter.getText().trim(), "NgĂ y káº¿t thĂºc khĂ´ng Ä‘Ăºng Ä‘á»‹nh dáº¡ng dd/MM/yyyy.");
        if (isInvalidDateMarker(to)) {
            return;
        }
        if (from != null && to != null && from.after(to)) {
            JOptionPane.showMessageDialog(this, "NgĂ y báº¯t Ä‘áº§u pháº£i nhá» hÆ¡n hoáº·c báº±ng ngĂ y káº¿t thĂºc.", "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LoaiPhong loaiPhong = findLoaiPhongByTen(valueOf(cboLoaiPhong.getSelectedItem()));
        String maLoaiPhong = loaiPhong == null ? "" : String.valueOf(loaiPhong.getMaLoaiPhong());
        String loaiNgay = "Táº¥t cáº£".equals(valueOf(cboLoaiNgay.getSelectedItem())) ? "" : valueOf(cboLoaiNgay.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());

        String tuKhoa = safeValue(txtTuKhoa.getText(), "").trim().toLowerCase(Locale.ROOT);
        displayedBangGia.clear();
        for (BangGia bangGia : bangGiaDAO.search("", maLoaiPhong, from, to, loaiNgay)) {
            if (!"Táº¥t cáº£".equals(trangThai) && !safeValue(bangGia.getTrangThai(), "").equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty() && !buildBangGiaSearchText(bangGia).contains(tuKhoa)) {
                continue;
            }
            displayedBangGia.add(bangGia);
        }

        refillBangGiaTable();
        refreshCurrentView();
        if (showMessage) {
            JOptionPane.showMessageDialog(this, "ÄĂ£ lá»c Ä‘Æ°á»£c " + displayedBangGia.size() + " báº£ng giĂ¡ phĂ¹ há»£p.", "ThĂ´ng bĂ¡o", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String buildBangGiaSearchText(BangGia bangGia) {
        return (
                formatBangGiaCode(bangGia.getMaBangGia()) + " " +
                safeValue(bangGia.getTenBangGia(), "") + " " +
                safeValue(bangGia.getTenLoaiPhong(), "") + " " +
                formatDate(bangGia.getTuNgay()) + " " +
                formatDate(bangGia.getDenNgay()) + " " +
                safeValue(bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia()), "") + " " +
                safeValue(bangGia.getTrangThai(), "")
        ).toLowerCase(Locale.ROOT);
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
                JOptionPane.showMessageDialog(this, "Vui lĂ²ng chá»n má»™t báº£ng giĂ¡.", "ThĂ´ng bĂ¡o", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        return displayedBangGia.get(row);
    }

    private ChiTietBangGia getSelectedChiTiet(boolean showMessage) {
        int row = tblChiTietBangGia.getSelectedRow();
        if (row < 0 || row >= displayedChiTiet.size()) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "Vui lĂ²ng chá»n má»™t chi tiáº¿t báº£ng giĂ¡.", "ThĂ´ng bĂ¡o", JOptionPane.WARNING_MESSAGE);
            }
            return null;
        }
        return displayedChiTiet.get(row);
    }

    private void selectBangGia(int maBangGia) {
        for (int i = 0; i < displayedBangGia.size(); i++) {
            if (displayedBangGia.get(i).getMaBangGia() == maBangGia) {
                tblBangGia.setRowSelectionInterval(i, i);
                scrollBangGiaRowToVisible(i);
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

    private boolean isConflictBangGiaRow(int row) {
        if (highlightedConflictBangGiaId == null || row < 0 || row >= displayedBangGia.size()) {
            return false;
        }
        return displayedBangGia.get(row).getMaBangGia() == highlightedConflictBangGiaId.intValue();
    }

    private void highlightConflictBangGia(Integer maBangGia) {
        highlightedConflictBangGiaId = maBangGia;
        if (maBangGia != null) {
            int rowIndex = findBangGiaRowIndex(maBangGia.intValue());
            if (rowIndex >= 0) {
                tblBangGia.setRowSelectionInterval(rowIndex, rowIndex);
                scrollBangGiaRowToVisible(rowIndex);
            }
        }
        refreshCurrentView();
    }

    private void clearConflictBangGiaHighlight() {
        highlightedConflictBangGiaId = null;
        refreshCurrentView();
    }

    private int findBangGiaRowIndex(int maBangGia) {
        for (int i = 0; i < displayedBangGia.size(); i++) {
            if (displayedBangGia.get(i).getMaBangGia() == maBangGia) {
                return i;
            }
        }
        return -1;
    }

    private void scrollBangGiaRowToVisible(int rowIndex) {
        if (tblBangGia == null || rowIndex < 0 || rowIndex >= tblBangGia.getRowCount()) {
            return;
        }
        tblBangGia.scrollRectToVisible(tblBangGia.getCellRect(rowIndex, 0, true));
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
        int confirm = JOptionPane.showConfirmDialog(this, "Chuyá»ƒn tráº¡ng thĂ¡i báº£ng giĂ¡ nĂ y sang \"Ngá»«ng Ă¡p dá»¥ng\"?", "XĂ¡c nháº­n", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String loaiNgay = bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia());
        bangGia.setTrangThai("Ngá»«ng Ă¡p dá»¥ng");
        if (bangGiaDAO.update(bangGia, loaiNgay)) {
            reloadBangGiaData(false, false);
            selectBangGia(bangGia.getMaBangGia());
            JOptionPane.showMessageDialog(this, "ÄĂ£ cáº­p nháº­t tráº¡ng thĂ¡i báº£ng giĂ¡.", "ThĂ´ng bĂ¡o", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "KhĂ´ng thá»ƒ cáº­p nháº­t tráº¡ng thĂ¡i báº£ng giĂ¡.\nChi tiáº¿t: " + safeValue(bangGiaDAO.getLastErrorMessage(), "KhĂ´ng xĂ¡c Ä‘á»‹nh."), "Lá»—i", JOptionPane.ERROR_MESSAGE);
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
        int confirm = JOptionPane.showConfirmDialog(this, "XĂ³a chi tiáº¿t báº£ng giĂ¡ Ä‘ang chá»n?", "XĂ¡c nháº­n", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (chiTietBangGiaDAO.delete(chiTiet.getMaChiTietBangGia())) {
            reloadBangGiaData(false, false);
            selectBangGia(chiTiet.getMaBangGia());
            JOptionPane.showMessageDialog(this, "ÄĂ£ xĂ³a chi tiáº¿t báº£ng giĂ¡.", "ThĂ´ng bĂ¡o", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "KhĂ´ng thá»ƒ xĂ³a chi tiáº¿t báº£ng giĂ¡.\nChi tiáº¿t: " + safeValue(chiTietBangGiaDAO.getLastErrorMessage(), "KhĂ´ng xĂ¡c Ä‘á»‹nh."), "Lá»—i", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, message, "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
            return INVALID_DATE;
        }
        try {
            return Date.valueOf(LocalDate.parse(value.trim(), DATE_FORMATTER));
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, message, "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, message, "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
            return INVALID_DATE;
        }
    }

    private boolean isInvalidDateMarker(Date date) {
        return date != null && INVALID_DATE.equals(date);
    }

    private Date parseDateSilently(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(LocalDate.parse(value.trim(), DATE_FORMATTER));
        } catch (DateTimeParseException ex) {
            return null;
        }
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
            JOptionPane.showMessageDialog(this, fieldName + " pháº£i lĂ  sá»‘ lá»›n hÆ¡n hoáº·c báº±ng 0.", "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
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
            Component[] children = ((java.awt.Container) component).getComponents();
            for (Component child : children) {
                JTextField found = findNestedTextField(child);
                if (found != null) {
                    return found;
                }
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
        private final List<ChiTietBangGia> draftChiTietList = new ArrayList<ChiTietBangGia>();
        private final DefaultTableModel draftChiTietModel;
        private final JTable tblDraftChiTiet;
        private final JScrollPane draftChiTietScroll;
        private final JPanel pnlConflictWarning;
        private final JLabel lblConflictWarning;
        private final JButton btnSave;
        private final Border defaultLoaiPhongBorder;
        private final Border defaultNgayBatDauBorder;
        private final Border defaultNgayKetThucBorder;
        private final Border defaultDraftDetailBorder;
        private final Color defaultLoaiPhongBackground;
        private final Color defaultNgayBatDauBackground;
        private final Color defaultNgayKetThucBackground;
        private BangGiaConflictInfo currentConflict;

        private BangGiaFormDialog(Frame owner, BangGia bangGia) {
            super(owner, bangGia == null ? "Them bang gia" : "Cap nhat bang gia", 940, 720);
            this.editingBangGia = bangGia;

            add(buildHeader(
                    bangGia == null ? "THEM BANG GIA" : "CAP NHAT BANG GIA",
                    "Luu header va chi tiet trong cung mot lan thao tac de tranh thieu du lieu."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);

            JPanel card = createCardPanel(new BorderLayout(0, 10));
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

            defaultLoaiPhongBorder = cboLoaiPhongDialog.getBorder();
            defaultLoaiPhongBackground = cboLoaiPhongDialog.getBackground();
            defaultNgayBatDauBorder = txtNgayBatDau.getBorder();
            defaultNgayKetThucBorder = txtNgayKetThuc.getBorder();
            defaultNgayBatDauBackground = resolveDateFieldBackground(txtNgayBatDau);
            defaultNgayKetThucBackground = resolveDateFieldBackground(txtNgayKetThuc);

            addFormRow(form, gbc, 0, "Ten bang gia", txtTenBangGia);
            addFormRow(form, gbc, 1, "Loai phong", cboLoaiPhongDialog);
            addFormRow(form, gbc, 2, "Ngay bat dau", txtNgayBatDau);
            addFormRow(form, gbc, 3, "Ngay ket thuc", txtNgayKetThuc);
            addFormRow(form, gbc, 4, "Loai ngay", cboLoaiNgayDialog);
            addFormRow(form, gbc, 5, "Trang thai", cboTrangThaiDialog);

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

            card.add(form, BorderLayout.CENTER);
            card.add(pnlConflictWarning, BorderLayout.SOUTH);

            JPanel detailCard = createCardPanel(new BorderLayout(0, 10));
            JLabel lblDetailTitle = new JLabel("Chi tiet bang gia");
            lblDetailTitle.setFont(SECTION_FONT);
            lblDetailTitle.setForeground(TEXT_PRIMARY);
            detailCard.add(lblDetailTitle, BorderLayout.NORTH);

            draftChiTietModel = new DefaultTableModel(new String[]{
                    "Ma CT", "Loai ngay", "Khung gio", "Gia gio", "Gia qua dem", "Gia ngay", "Gia cuoi tuan", "Gia le", "Phu thu"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tblDraftChiTiet = new JTable(draftChiTietModel);
            tblDraftChiTiet.setFont(BODY_FONT);
            tblDraftChiTiet.setRowHeight(28);
            tblDraftChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblDraftChiTiet.setGridColor(BORDER_SOFT);
            tblDraftChiTiet.setShowGrid(true);
            tblDraftChiTiet.setFillsViewportHeight(true);
            ScreenUIHelper.styleTableHeader(tblDraftChiTiet);
            draftChiTietScroll = new JScrollPane(tblDraftChiTiet);
            draftChiTietScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            draftChiTietScroll.getVerticalScrollBar().setUnitIncrement(18);
            draftChiTietScroll.setPreferredSize(new Dimension(0, 240));
            defaultDraftDetailBorder = draftChiTietScroll.getBorder();

            JPanel detailAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            detailAction.setOpaque(false);
            detailAction.add(createPrimaryButton("Them CT", new Color(59, 130, 246), Color.WHITE, e -> openDraftChiTietDialog(null, -1)));
            detailAction.add(createOutlineButton("Sua CT", new Color(245, 158, 11), e -> openEditDraftChiTiet()));
            detailAction.add(createOutlineButton("Xoa CT", new Color(220, 38, 38), e -> deleteSelectedDraftChiTiet()));

            detailCard.add(detailAction, BorderLayout.CENTER);
            detailCard.add(draftChiTietScroll, BorderLayout.SOUTH);

            body.add(card, BorderLayout.NORTH);
            body.add(detailCard, BorderLayout.CENTER);
            add(body, BorderLayout.CENTER);

            btnSave = createPrimaryButton("Luu", new Color(22, 163, 74), Color.WHITE, e -> saveBangGia());
            add(buildButtons(
                    createOutlineButton("Huy", new Color(107, 114, 128), e -> dispose()),
                    btnSave
            ), BorderLayout.SOUTH);

            if (bangGia != null) {
                for (ChiTietBangGia chiTietBangGia : chiTietBangGiaDAO.getByMaBangGia(bangGia.getMaBangGia())) {
                    draftChiTietList.add(copyChiTietBangGia(chiTietBangGia));
                }
            }
            refillDraftChiTietTable();
            registerConflictListeners();
            validateConflictSilently();
        }

        private void saveBangGia() {
            clearDetailErrorState();
            if (txtTenBangGia.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ten bang gia khong duoc rong.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LoaiPhong loaiPhong = findLoaiPhongByTen(valueOf(cboLoaiPhongDialog.getSelectedItem()));
            if (loaiPhong == null) {
                JOptionPane.showMessageDialog(this, "Loai phong khong hop le.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Date ngayBatDau = parseRequiredDate(txtNgayBatDau.getText().trim(), "Ngay bat dau khong dung dinh dang dd/MM/yyyy.");
            if (isInvalidDateMarker(ngayBatDau)) {
                return;
            }
            Date ngayKetThuc = parseRequiredDate(txtNgayKetThuc.getText().trim(), "Ngay ket thuc khong dung dinh dang dd/MM/yyyy.");
            if (isInvalidDateMarker(ngayKetThuc)) {
                return;
            }
            if (ngayBatDau.after(ngayKetThuc)) {
                JOptionPane.showMessageDialog(this, "Ngay bat dau phai nho hon hoac bang ngay ket thuc.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String loaiNgay = valueOf(cboLoaiNgayDialog.getSelectedItem());
            if (loaiNgay.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Loai ngay khong duoc rong.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            if (trangThai.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Trang thai khong duoc rong.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BangGiaConflictInfo conflictInfo = bangGiaDAO.findDateConflict(
                    loaiPhong.getMaLoaiPhong(),
                    ngayBatDau,
                    ngayKetThuc,
                    editingBangGia == null ? null : Integer.valueOf(editingBangGia.getMaBangGia())
            );
            if (conflictInfo != null) {
                applyConflictState(conflictInfo);
                JOptionPane.showMessageDialog(this, buildConflictPopupMessage(conflictInfo), "Bang gia bi chong thoi gian", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String detailErrorMessage = validateDraftChiTietBeforeSave();
            if (detailErrorMessage != null) {
                JOptionPane.showMessageDialog(this, detailErrorMessage, "Chi tiet bang gia chua hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BangGia bangGia = editingBangGia == null ? new BangGia() : editingBangGia;
            bangGia.setTenBangGia(txtTenBangGia.getText().trim());
            bangGia.setMaLoaiPhong(loaiPhong.getMaLoaiPhong());
            bangGia.setTenLoaiPhong(loaiPhong.getTenLoaiPhong());
            bangGia.setTuNgay(ngayBatDau);
            bangGia.setDenNgay(ngayKetThuc);
            bangGia.setTrangThai(trangThai);

            boolean success = bangGiaDAO.saveWithDetails(bangGia, loaiNgay, draftChiTietList);
            if (!success) {
                JOptionPane.showMessageDialog(this, "Khong the luu bang gia.\nChi tiet: " + safeValue(bangGiaDAO.getLastErrorMessage(), "Khong xac dinh."), "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            reloadBangGiaData(false, false);
            selectBangGia(bangGia.getMaBangGia());
            clearConflictBangGiaHighlight();
            JOptionPane.showMessageDialog(this, editingBangGia == null ? "Them bang gia thanh cong." : "Cap nhat bang gia thanh cong.", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }

        @Override
        public void dispose() {
            clearConflictBangGiaHighlight();
            if (editingBangGia != null && editingBangGia.getMaBangGia() > 0) {
                selectBangGia(editingBangGia.getMaBangGia());
            }
            super.dispose();
        }

        private void registerConflictListeners() {
            cboLoaiPhongDialog.addActionListener(e -> validateConflictSilently());
            installDateFieldChangeListener(txtNgayBatDau, this::validateConflictSilently);
            installDateFieldChangeListener(txtNgayKetThuc, this::validateConflictSilently);
        }

        private void validateConflictSilently() {
            LoaiPhong loaiPhong = findLoaiPhongByTen(valueOf(cboLoaiPhongDialog.getSelectedItem()));
            Date ngayBatDau = parseDateSilently(txtNgayBatDau.getText().trim());
            Date ngayKetThuc = parseDateSilently(txtNgayKetThuc.getText().trim());
            if (loaiPhong == null || ngayBatDau == null || ngayKetThuc == null || ngayBatDau.after(ngayKetThuc)) {
                clearConflictState();
                return;
            }

            BangGiaConflictInfo conflictInfo = bangGiaDAO.findDateConflict(
                    loaiPhong.getMaLoaiPhong(),
                    ngayBatDau,
                    ngayKetThuc,
                    editingBangGia == null ? null : Integer.valueOf(editingBangGia.getMaBangGia())
            );
            if (conflictInfo != null) {
                applyConflictState(conflictInfo);
            } else {
                clearConflictState();
            }
        }

        private void applyConflictState(BangGiaConflictInfo conflictInfo) {
            currentConflict = conflictInfo;
            lblConflictWarning.setText("<html><b>Dang bi chong thoi gian.</b> " + buildConflictHtml(conflictInfo)
                    + "<br/>Vui long doi ngay bat dau, ngay ket thuc hoac chon loai phong khac.</html>");
            pnlConflictWarning.setVisible(true);
            setHeaderConflictState(true);
            highlightConflictBangGia(Integer.valueOf(conflictInfo.getMaBangGia()));
            updateSaveButtonState();
        }

        private void clearConflictState() {
            currentConflict = null;
            lblConflictWarning.setText("");
            pnlConflictWarning.setVisible(false);
            setHeaderConflictState(false);
            clearConflictBangGiaHighlight();
            updateSaveButtonState();
        }

        private void updateSaveButtonState() {
            btnSave.setEnabled(currentConflict == null);
        }

        private void setHeaderConflictState(boolean conflict) {
            cboLoaiPhongDialog.setBorder(conflict ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultLoaiPhongBorder);
            cboLoaiPhongDialog.setBackground(conflict ? CONFLICT_BG : defaultLoaiPhongBackground);
            txtNgayBatDau.setBorder(conflict ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultNgayBatDauBorder);
            txtNgayKetThuc.setBorder(conflict ? BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true) : defaultNgayKetThucBorder);
            setDateFieldBackground(txtNgayBatDau, conflict ? CONFLICT_BG : defaultNgayBatDauBackground);
            setDateFieldBackground(txtNgayKetThuc, conflict ? CONFLICT_BG : defaultNgayKetThucBackground);
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

        private void openDraftChiTietDialog(ChiTietBangGia chiTietBangGia, int rowIndex) {
            new DraftChiTietBangGiaFormDialog(BangGiaGUI.this, this, chiTietBangGia, rowIndex).setVisible(true);
        }

        private void openEditDraftChiTiet() {
            int rowIndex = getSelectedDraftChiTietRow(true);
            if (rowIndex >= 0) {
                openDraftChiTietDialog(draftChiTietList.get(rowIndex), rowIndex);
            }
        }

        private void deleteSelectedDraftChiTiet() {
            int rowIndex = getSelectedDraftChiTietRow(true);
            if (rowIndex < 0) {
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Xoa dong chi tiet bang gia dang chon?", "Xac nhan", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            draftChiTietList.remove(rowIndex);
            refillDraftChiTietTable();
            clearDetailErrorState();
        }

        private int getSelectedDraftChiTietRow(boolean showMessage) {
            int row = tblDraftChiTiet.getSelectedRow();
            if (row < 0 || row >= draftChiTietList.size()) {
                if (showMessage) {
                    JOptionPane.showMessageDialog(this, "Vui long chon mot dong chi tiet bang gia.", "Thong bao", JOptionPane.WARNING_MESSAGE);
                }
                return -1;
            }
            return row;
        }

        private void refillDraftChiTietTable() {
            draftChiTietModel.setRowCount(0);
            for (ChiTietBangGia chiTietBangGia : draftChiTietList) {
                draftChiTietModel.addRow(new Object[]{
                        chiTietBangGia.getMaChiTietBangGia() > 0 ? "CT" + chiTietBangGia.getMaChiTietBangGia() : "Moi",
                        chiTietBangGia.getLoaiNgay(),
                        chiTietBangGia.getKhungGio(),
                        formatCurrency(chiTietBangGia.getGiaTheoGio()),
                        formatCurrency(chiTietBangGia.getGiaQuaDem()),
                        formatCurrency(chiTietBangGia.getGiaTheoNgay()),
                        formatCurrency(chiTietBangGia.getGiaCuoiTuan()),
                        formatCurrency(chiTietBangGia.getGiaLe()),
                        formatCurrency(chiTietBangGia.getPhuThu())
                });
            }
        }

        private String validateDraftChiTietBeforeSave() {
            if (draftChiTietList.isEmpty()) {
                showDetailErrorState(-1);
                return "Bang gia phai co it nhat 1 dong chi tiet.";
            }
            for (int i = 0; i < draftChiTietList.size(); i++) {
                ChiTietBangGia chiTietBangGia = draftChiTietList.get(i);
                if (chiTietBangGia == null) {
                    showDetailErrorState(i);
                    return "Dong chi tiet " + (i + 1) + " khong hop le.";
                }
                if (safeValue(chiTietBangGia.getLoaiNgay(), "").isEmpty()) {
                    showDetailErrorState(i);
                    return "Dong chi tiet " + (i + 1) + " dang thieu loai ngay.";
                }
                if (safeValue(chiTietBangGia.getKhungGio(), "").isEmpty()) {
                    showDetailErrorState(i);
                    return "Dong chi tiet " + (i + 1) + " dang thieu khung gio.";
                }
                if (chiTietBangGia.getGiaTheoGio() < 0 || chiTietBangGia.getGiaQuaDem() < 0 || chiTietBangGia.getGiaTheoNgay() < 0
                        || chiTietBangGia.getGiaCuoiTuan() < 0 || chiTietBangGia.getGiaLe() < 0 || chiTietBangGia.getPhuThu() < 0) {
                    showDetailErrorState(i);
                    return "Dong chi tiet " + (i + 1) + " co gia tri tien khong hop le.";
                }
            }
            clearDetailErrorState();
            return null;
        }

        private void showDetailErrorState(int rowIndex) {
            draftChiTietScroll.setBorder(BorderFactory.createLineBorder(CONFLICT_BORDER, 1, true));
            if (rowIndex >= 0 && rowIndex < tblDraftChiTiet.getRowCount()) {
                tblDraftChiTiet.setRowSelectionInterval(rowIndex, rowIndex);
                tblDraftChiTiet.scrollRectToVisible(tblDraftChiTiet.getCellRect(rowIndex, 0, true));
            }
            tblDraftChiTiet.requestFocusInWindow();
        }

        private void clearDetailErrorState() {
            draftChiTietScroll.setBorder(defaultDraftDetailBorder);
        }

        private String buildConflictHtml(BangGiaConflictInfo conflictInfo) {
            return "Loai phong <b>" + safeValue(conflictInfo.getTenLoaiPhong(), "-") + "</b> da co bang gia <b>"
                    + safeValue(conflictInfo.getTenBangGia(), formatBangGiaCode(conflictInfo.getMaBangGia())) + "</b> ("
                    + formatBangGiaCode(conflictInfo.getMaBangGia()) + ") ap dung tu <b>"
                    + formatDate(conflictInfo.getNgayBatDau()) + "</b> den <b>"
                    + formatDate(conflictInfo.getNgayKetThuc()) + "</b>, trang thai <b>"
                    + safeValue(conflictInfo.getTrangThai(), "-") + "</b>.";
        }

        private String buildConflictPopupMessage(BangGiaConflictInfo conflictInfo) {
            return "Loai phong " + safeValue(conflictInfo.getTenLoaiPhong(), "-")
                    + " da co bang gia " + safeValue(conflictInfo.getTenBangGia(), formatBangGiaCode(conflictInfo.getMaBangGia()))
                    + " (" + formatBangGiaCode(conflictInfo.getMaBangGia()) + ") ap dung tu "
                    + formatDate(conflictInfo.getNgayBatDau()) + " den " + formatDate(conflictInfo.getNgayKetThuc())
                    + ". Khoang thoi gian ban chon dang bi chong.";
        }

        private ChiTietBangGia copyChiTietBangGia(ChiTietBangGia source) {
            ChiTietBangGia copy = new ChiTietBangGia();
            copy.setMaChiTietBangGia(source.getMaChiTietBangGia());
            copy.setMaBangGia(source.getMaBangGia());
            copy.setLoaiNgay(source.getLoaiNgay());
            copy.setKhungGio(source.getKhungGio());
            copy.setGiaTheoGio(source.getGiaTheoGio());
            copy.setGiaQuaDem(source.getGiaQuaDem());
            copy.setGiaTheoNgay(source.getGiaTheoNgay());
            copy.setGiaCuoiTuan(source.getGiaCuoiTuan());
            copy.setGiaLe(source.getGiaLe());
            copy.setPhuThu(source.getPhuThu());
            return copy;
        }
    }

    private final class DraftChiTietBangGiaFormDialog extends BaseDialog {
        private final BangGiaFormDialog parentDialog;
        private final ChiTietBangGia editingChiTiet;
        private final int editingRowIndex;
        private final JComboBox<String> cboLoaiNgayDialog;
        private final AppTimePickerField txtKhungGio;
        private final JTextField txtGiaTheoGio;
        private final JTextField txtGiaQuaDem;
        private final JTextField txtGiaTheoNgay;
        private final JTextField txtGiaCuoiTuan;
        private final JTextField txtGiaLe;
        private final JTextField txtPhuThu;

        private DraftChiTietBangGiaFormDialog(Frame owner, BangGiaFormDialog parentDialog, ChiTietBangGia chiTiet, int rowIndex) {
            super(owner, chiTiet == null ? "Them chi tiet bang gia" : "Cap nhat chi tiet bang gia", 620, 520);
            this.parentDialog = parentDialog;
            this.editingChiTiet = chiTiet == null ? null : copyChiTietBangGia(chiTiet);
            this.editingRowIndex = rowIndex;

            add(buildHeader(
                    chiTiet == null ? "THEM CHI TIET BANG GIA" : "CAP NHAT CHI TIET BANG GIA",
                    "Dong chi tiet nay se duoc luu cung header khi ban bam Luu bang gia."
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

            addFormRow(form, gbc, 0, "Loai ngay", cboLoaiNgayDialog);
            addFormRow(form, gbc, 1, "Khung gio", txtKhungGio);
            addFormRow(form, gbc, 2, "Gia theo gio", txtGiaTheoGio);
            addFormRow(form, gbc, 3, "Gia qua dem", txtGiaQuaDem);
            addFormRow(form, gbc, 4, "Gia theo ngay", txtGiaTheoNgay);
            addFormRow(form, gbc, 5, "Gia cuoi tuan", txtGiaCuoiTuan);
            addFormRow(form, gbc, 6, "Gia le", txtGiaLe);
            addFormRow(form, gbc, 7, "Phu thu", txtPhuThu);

            card.add(form, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
            add(buildButtons(
                    createOutlineButton("Huy", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("Luu chi tiet", new Color(59, 130, 246), Color.WHITE, e -> saveDraftChiTiet())
            ), BorderLayout.SOUTH);
        }

        private void saveDraftChiTiet() {
            if (valueOf(cboLoaiNgayDialog.getSelectedItem()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Loai ngay khong duoc de trong.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtKhungGio.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khung gio khong duoc de trong.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtKhungGio.getTimeValue() == null) {
                JOptionPane.showMessageDialog(this, "Khung gio khong dung dinh dang HH:mm.", "Du lieu khong hop le", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double giaTheoGio = parseMoney(txtGiaTheoGio.getText().trim(), "Gia theo gio");
            double giaQuaDem = parseMoney(txtGiaQuaDem.getText().trim(), "Gia qua dem");
            double giaTheoNgay = parseMoney(txtGiaTheoNgay.getText().trim(), "Gia theo ngay");
            double giaCuoiTuan = parseMoney(txtGiaCuoiTuan.getText().trim(), "Gia cuoi tuan");
            double giaLe = parseMoney(txtGiaLe.getText().trim(), "Gia le");
            double phuThu = parseMoney(txtPhuThu.getText().trim(), "Phu thu");
            if (giaTheoGio < 0 || giaQuaDem < 0 || giaTheoNgay < 0 || giaCuoiTuan < 0 || giaLe < 0 || phuThu < 0) {
                return;
            }

            ChiTietBangGia chiTietBangGia = editingChiTiet == null ? new ChiTietBangGia() : editingChiTiet;
            chiTietBangGia.setLoaiNgay(valueOf(cboLoaiNgayDialog.getSelectedItem()));
            chiTietBangGia.setKhungGio(txtKhungGio.getText().trim());
            chiTietBangGia.setGiaTheoGio(giaTheoGio);
            chiTietBangGia.setGiaQuaDem(giaQuaDem);
            chiTietBangGia.setGiaTheoNgay(giaTheoNgay);
            chiTietBangGia.setGiaCuoiTuan(giaCuoiTuan);
            chiTietBangGia.setGiaLe(giaLe);
            chiTietBangGia.setPhuThu(phuThu);

            if (editingRowIndex >= 0 && editingRowIndex < parentDialog.draftChiTietList.size()) {
                parentDialog.draftChiTietList.set(editingRowIndex, chiTietBangGia);
            } else {
                parentDialog.draftChiTietList.add(chiTietBangGia);
            }
            parentDialog.refillDraftChiTietTable();
            parentDialog.clearDetailErrorState();
            dispose();
        }

        private ChiTietBangGia copyChiTietBangGia(ChiTietBangGia source) {
            ChiTietBangGia copy = new ChiTietBangGia();
            copy.setMaChiTietBangGia(source.getMaChiTietBangGia());
            copy.setMaBangGia(source.getMaBangGia());
            copy.setLoaiNgay(source.getLoaiNgay());
            copy.setKhungGio(source.getKhungGio());
            copy.setGiaTheoGio(source.getGiaTheoGio());
            copy.setGiaQuaDem(source.getGiaQuaDem());
            copy.setGiaTheoNgay(source.getGiaTheoNgay());
            copy.setGiaCuoiTuan(source.getGiaCuoiTuan());
            copy.setGiaLe(source.getGiaLe());
            copy.setPhuThu(source.getPhuThu());
            return copy;
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
            super(owner, chiTiet == null ? "ThĂªm chi tiáº¿t báº£ng giĂ¡" : "Cáº­p nháº­t chi tiáº¿t báº£ng giĂ¡", 620, 520);
            this.bangGia = bangGia;
            this.editingChiTiet = chiTiet;

            add(buildHeader(
                    chiTiet == null ? "THĂM CHI TIáº¾T Báº¢NG GIĂ" : "Cáº¬P NHáº¬T CHI TIáº¾T Báº¢NG GIĂ",
                    "Khai bĂ¡o giĂ¡ chi tiáº¿t theo ChiTietBangGia."
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

            addFormRow(form, gbc, 0, "Loáº¡i ngĂ y", cboLoaiNgayDialog);
            addFormRow(form, gbc, 1, "Khung giá»", txtKhungGio);
            addFormRow(form, gbc, 2, "GiĂ¡ theo giá»", txtGiaTheoGio);
            addFormRow(form, gbc, 3, "GiĂ¡ qua Ä‘Ăªm", txtGiaQuaDem);
            addFormRow(form, gbc, 4, "GiĂ¡ theo ngĂ y", txtGiaTheoNgay);
            addFormRow(form, gbc, 5, "GiĂ¡ cuá»‘i tuáº§n", txtGiaCuoiTuan);
            addFormRow(form, gbc, 6, "GiĂ¡ lá»…", txtGiaLe);
            addFormRow(form, gbc, 7, "Phá»¥ thu", txtPhuThu);

            card.add(form, BorderLayout.CENTER);
            add(card, BorderLayout.CENTER);
            add(buildButtons(
                    createOutlineButton("Há»§y", new Color(107, 114, 128), e -> dispose()),
                    createPrimaryButton("LÆ°u", new Color(59, 130, 246), Color.WHITE, e -> saveChiTiet())
            ), BorderLayout.SOUTH);
        }

        private void saveChiTiet() {
            if (valueOf(cboLoaiNgayDialog.getSelectedItem()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Loáº¡i ngĂ y khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.", "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtKhungGio.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khung giá» khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.", "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtKhungGio.getTimeValue() == null) {
                JOptionPane.showMessageDialog(this, "Khung giá» khĂ´ng Ä‘Ăºng Ä‘á»‹nh dáº¡ng HH:mm.", "Dá»¯ liá»‡u khĂ´ng há»£p lá»‡", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double giaTheoGio = parseMoney(txtGiaTheoGio.getText().trim(), "GiĂ¡ theo giá»");
            double giaQuaDem = parseMoney(txtGiaQuaDem.getText().trim(), "GiĂ¡ qua Ä‘Ăªm");
            double giaTheoNgay = parseMoney(txtGiaTheoNgay.getText().trim(), "GiĂ¡ theo ngĂ y");
            double giaCuoiTuan = parseMoney(txtGiaCuoiTuan.getText().trim(), "GiĂ¡ cuá»‘i tuáº§n");
            double giaLe = parseMoney(txtGiaLe.getText().trim(), "GiĂ¡ lá»…");
            double phuThu = parseMoney(txtPhuThu.getText().trim(), "Phá»¥ thu");
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
                JOptionPane.showMessageDialog(this, "KhĂ´ng thá»ƒ lÆ°u chi tiáº¿t báº£ng giĂ¡.\nChi tiáº¿t: " + safeValue(chiTietBangGiaDAO.getLastErrorMessage(), "KhĂ´ng xĂ¡c Ä‘á»‹nh."), "Lá»—i", JOptionPane.ERROR_MESSAGE);
                return;
            }

            reloadBangGiaData(false, false);
            selectBangGia(bangGia.getMaBangGia());
            JOptionPane.showMessageDialog(this, editingChiTiet == null ? "ThĂªm chi tiáº¿t báº£ng giĂ¡ thĂ nh cĂ´ng." : "Cáº­p nháº­t chi tiáº¿t báº£ng giĂ¡ thĂ nh cĂ´ng.", "ThĂ´ng bĂ¡o", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private final class BangGiaViewDialog extends BaseDialog {
        private BangGiaViewDialog(Frame owner, BangGia bangGia) {
            super(owner, "Xem chi tiáº¿t báº£ng giĂ¡", 860, 620);
            add(buildHeader("XEM CHI TIáº¾T Báº¢NG GIĂ", "Dá»¯ liá»‡u báº£ng giĂ¡ Ä‘ang láº¥y trá»±c tiáº¿p tá»« database."), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            JPanel headerCard = createCardPanel(new BorderLayout());
            JPanel form = createFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 0, 6, 12);
            addFormRow(form, gbc, 0, "MĂ£ báº£ng giĂ¡", new JLabel(formatBangGiaCode(bangGia.getMaBangGia())));
            addFormRow(form, gbc, 1, "TĂªn báº£ng giĂ¡", new JLabel(safeValue(bangGia.getTenBangGia(), "-")));
            addFormRow(form, gbc, 2, "Loáº¡i phĂ²ng", new JLabel(safeValue(bangGia.getTenLoaiPhong(), "-")));
            addFormRow(form, gbc, 3, "NgĂ y báº¯t Ä‘áº§u", new JLabel(formatDate(bangGia.getTuNgay())));
            addFormRow(form, gbc, 4, "NgĂ y káº¿t thĂºc", new JLabel(formatDate(bangGia.getDenNgay())));
            addFormRow(form, gbc, 5, "Loáº¡i ngĂ y", new JLabel(safeValue(bangGiaDAO.getLoaiNgayByMaBangGia(bangGia.getMaBangGia()), "-")));
            addFormRow(form, gbc, 6, "Tráº¡ng thĂ¡i", new JLabel(safeValue(bangGia.getTrangThai(), "-")));
            headerCard.add(form, BorderLayout.CENTER);

            DefaultTableModel model = new DefaultTableModel(new String[]{
                    "MĂ£ CT", "Loáº¡i ngĂ y", "Khung giá»", "GiĂ¡ giá»", "GiĂ¡ qua Ä‘Ăªm", "GiĂ¡ ngĂ y", "GiĂ¡ cuá»‘i tuáº§n", "GiĂ¡ lá»…", "Phá»¥ thu"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setFont(BODY_FONT);
            table.setRowHeight(28);
            table.setGridColor(BORDER_SOFT);
            table.setShowGrid(true);
            table.setFillsViewportHeight(true);
            ScreenUIHelper.styleTableHeader(table);
            for (ChiTietBangGia chiTiet : chiTietBangGiaDAO.getByMaBangGia(bangGia.getMaBangGia())) {
                model.addRow(new Object[]{
                        "CT" + chiTiet.getMaChiTietBangGia(), chiTiet.getLoaiNgay(), chiTiet.getKhungGio(),
                        formatCurrency(chiTiet.getGiaTheoGio()), formatCurrency(chiTiet.getGiaQuaDem()),
                        formatCurrency(chiTiet.getGiaTheoNgay()), formatCurrency(chiTiet.getGiaCuoiTuan()),
                        formatCurrency(chiTiet.getGiaLe()), formatCurrency(chiTiet.getPhuThu())
                });
            }
            JPanel detailCard = createCardPanel(new BorderLayout());
            JScrollPane detailScroll = new JScrollPane(table);
            detailScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
            detailScroll.getVerticalScrollBar().setUnitIncrement(18);
            detailCard.add(detailScroll, BorderLayout.CENTER);

            body.add(headerCard, BorderLayout.NORTH);
            body.add(detailCard, BorderLayout.CENTER);
            add(body, BorderLayout.CENTER);
            add(buildButtons(createPrimaryButton("ÄĂ³ng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
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
