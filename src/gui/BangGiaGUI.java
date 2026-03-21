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
    private static final String[] ROOM_TYPES = {"Standard", "Deluxe", "Suite"};
    private static final String[] PRICE_STATUS_OPTIONS = {"Đang áp dụng", "Ngừng áp dụng"};
    private static final String[] DAY_TYPE_OPTIONS = {"Ngày thường", "Cuối tuần", "Ngày lễ", "Mùa cao điểm"};

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<PriceRecord> allPrices = new ArrayList<PriceRecord>();
    private final List<PriceRecord> filteredPrices = new ArrayList<PriceRecord>();

    private JTable tblBangGia;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboLoaiPhong;
    private JComboBox<String> cboMuaGia;
    private JComboBox<String> cboTrangThai;
    private JTextField txtTuKhoa;

    private JLabel lblMaBangGia;
    private JLabel lblTenBangGia;
    private JLabel lblLoaiPhongChiTiet;
    private JLabel lblGiaTheoGio;
    private JLabel lblGiaTheoNgay;
    private JLabel lblGiaCuoiTuan;
    private JLabel lblGiaLe;
    private JLabel lblPhuThuExtraBed;
    private JLabel lblPhuThuQuaGio;
    private JLabel lblTrangThaiChiTiet;
    private JTextArea txtGhiChu;

    public BangGiaGUI() {
        this("guest", "Lễ tân");
    }

    public BangGiaGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý bảng giá - Hotel PMS");
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

        JLabel lblSub = new JLabel("Quản lý giá phòng theo mùa, phụ thu và trạng thái áp dụng bằng dữ liệu mẫu.");
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
        card.add(createPrimaryButton("Thêm bảng giá", new Color(22, 163, 74), Color.WHITE, e -> openCreatePriceTableDialog()));
        card.add(createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openUpdatePriceTableDialog()));
        card.add(createPrimaryButton("Ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivatePriceTableDialog()));
        card.add(createPrimaryButton("Xem chi tiết", new Color(99, 102, 241), Color.WHITE, e -> openViewPriceTableDialog()));
        card.add(createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true)));
        card.add(createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboLoaiPhong = createComboBox(new String[]{"Tất cả", "Standard", "Deluxe", "Suite"});
        cboMuaGia = createComboBox(new String[]{"Tất cả", "Thường", "Cuối tuần", "Lễ", "Cao điểm"});
        cboTrangThai = createComboBox(new String[]{"Tất cả", "Đang áp dụng", "Ngừng áp dụng"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(290, 34));
        txtTuKhoa.setToolTipText("Mã bảng giá / tên bảng giá");

        left.add(createFieldGroup("Loại phòng", cboLoaiPhong));
        left.add(createFieldGroup("Mùa giá", cboMuaGia));
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

        JLabel lblTitle = new JLabel("Danh sách bảng giá");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chọn một dòng để xem chi tiết bảng giá.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "Mã bảng giá",
                "Tên bảng giá",
                "Loại phòng",
                "Giá giờ",
                "Giá ngày",
                "Trạng thái"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBangGia = new JTable(tableModel);
        tblBangGia.setFont(BODY_FONT);
        tblBangGia.setRowHeight(32);
        tblBangGia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBangGia.setGridColor(BORDER_SOFT);
        tblBangGia.setShowGrid(true);
        tblBangGia.setFillsViewportHeight(true);
        tblBangGia.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBangGia.getTableHeader().setBackground(new Color(243, 244, 246));
        tblBangGia.getTableHeader().setForeground(TEXT_PRIMARY);

        tblBangGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblBangGia.getSelectedRow();
                if (row >= 0 && row < filteredPrices.size()) {
                    updateDetailPanel(filteredPrices.get(row));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblBangGia);
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

        JLabel lblTitle = new JLabel("Chi tiết bảng giá");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(10, 2, 10, 8));
        body.setOpaque(false);

        lblMaBangGia = createValueLabel();
        lblTenBangGia = createValueLabel();
        lblLoaiPhongChiTiet = createValueLabel();
        lblGiaTheoGio = createValueLabel();
        lblGiaTheoNgay = createValueLabel();
        lblGiaCuoiTuan = createValueLabel();
        lblGiaLe = createValueLabel();
        lblPhuThuExtraBed = createValueLabel();
        lblPhuThuQuaGio = createValueLabel();
        lblTrangThaiChiTiet = createValueLabel();

        addDetailRow(body, "Mã bảng giá", lblMaBangGia);
        addDetailRow(body, "Tên bảng giá", lblTenBangGia);
        addDetailRow(body, "Loại phòng", lblLoaiPhongChiTiet);
        addDetailRow(body, "Giá theo giờ", lblGiaTheoGio);
        addDetailRow(body, "Giá theo ngày", lblGiaTheoNgay);
        addDetailRow(body, "Giá cuối tuần", lblGiaCuoiTuan);
        addDetailRow(body, "Giá lễ", lblGiaLe);
        addDetailRow(body, "Phụ thu extra bed", lblPhuThuExtraBed);
        addDetailRow(body, "Phụ thu quá giờ", lblPhuThuQuaGio);
        addDetailRow(body, "Trạng thái", lblTrangThaiChiTiet);

        JPanel notePanel = new JPanel(new BorderLayout(0, 6));
        notePanel.setOpaque(false);

        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(TEXT_MUTED);

        txtGhiChu = new JTextArea(4, 20);
        txtGhiChu.setEditable(false);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setFont(BODY_FONT);
        txtGhiChu.setForeground(TEXT_PRIMARY);
        txtGhiChu.setBackground(PANEL_SOFT);
        txtGhiChu.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane noteScroll = new JScrollPane(txtGhiChu);
        noteScroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

        notePanel.add(lblNote, BorderLayout.NORTH);
        notePanel.add(noteScroll, BorderLayout.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(notePanel, BorderLayout.SOUTH);
        return card;
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
        comboBox.setMaximumSize(new Dimension(180, 34));
        return comboBox;
    }

    private JTextField createInputField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(180, 34));
        field.setMaximumSize(new Dimension(290, 34));
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
        allPrices.clear();
        PriceRecord bg01 = PriceRecord.create("BG01", "Giá thường Standard", "Standard", "01/03/2026", "31/12/2026", "Đang áp dụng", "Áp dụng cho ngày thường, không bao gồm lễ.");
        bg01.details.add(new PriceDetailRecord("Ngày thường", "Qua đêm", 850000, 120000, 150000, 1, "Áp dụng mặc định"));
        bg01.details.add(new PriceDetailRecord("Cuối tuần", "Qua đêm", 930000, 130000, 150000, 2, "Tăng nhẹ cuối tuần"));
        allPrices.add(bg01);

        PriceRecord bg02 = PriceRecord.create("BG02", "Giá cuối tuần Deluxe", "Deluxe", "01/03/2026", "30/09/2026", "Đang áp dụng", "Áp dụng tối thứ Sáu đến hết Chủ nhật.");
        bg02.details.add(new PriceDetailRecord("Cuối tuần", "Qua đêm", 1450000, 180000, 200000, 1, "Gói cuối tuần"));
        bg02.details.add(new PriceDetailRecord("Ngày thường", "Theo giờ 3h đầu", 1250000, 160000, 180000, 2, "Áp dụng ngoài cuối tuần"));
        allPrices.add(bg02);

        PriceRecord bg03 = PriceRecord.create("BG03", "Giá lễ Suite", "Suite", "20/04/2026", "05/05/2026", "Đang áp dụng", "Áp dụng cho dịp lễ lớn và kỳ nghỉ kéo dài.");
        bg03.details.add(new PriceDetailRecord("Ngày lễ", "Qua đêm", 2800000, 250000, 300000, 1, "Lễ chính"));
        bg03.details.add(new PriceDetailRecord("Mùa cao điểm", "Theo giờ", 2400000, 220000, 280000, 2, "Backup"));
        allPrices.add(bg03);

        PriceRecord bg04 = PriceRecord.create("BG04", "Giá cao điểm Deluxe", "Deluxe", "01/06/2025", "31/08/2025", "Ngừng áp dụng", "Bảng giá cũ của mùa du lịch cao điểm.");
        bg04.details.add(new PriceDetailRecord("Mùa cao điểm", "Qua đêm", 1950000, 210000, 220000, 1, "Lưu lịch sử"));
        allPrices.add(bg04);
    }

    private void reloadSampleData(boolean showMessage) {
        cboLoaiPhong.setSelectedIndex(0);
        cboMuaGia.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu bảng giá.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredPrices.clear();

        String loaiPhong = valueOf(cboLoaiPhong.getSelectedItem());
        String muaGia = valueOf(cboMuaGia.getSelectedItem());
        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (PriceRecord price : allPrices) {
            if (!"Tất cả".equals(loaiPhong) && !price.loaiPhong.equals(loaiPhong)) {
                continue;
            }
            if (!"Tất cả".equals(muaGia) && !price.muaGia.equals(muaGia)) {
                continue;
            }
            if (!"Tất cả".equals(trangThai) && !price.trangThai.equals(trangThai)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (price.maBangGia + " " + price.tenBangGia).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredPrices.add(price);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredPrices.size() + " bảng giá phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (PriceRecord price : filteredPrices) {
            tableModel.addRow(new Object[]{
                    price.maBangGia,
                    price.tenBangGia,
                    price.loaiPhong,
                    price.getDisplayGiaTheoGio(),
                    price.getDisplayGiaTheoNgay(),
                    price.trangThai
            });
        }

        if (!filteredPrices.isEmpty()) {
            tblBangGia.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredPrices.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(PriceRecord price) {
        lblMaBangGia.setText(price.maBangGia);
        lblTenBangGia.setText(price.tenBangGia);
        lblLoaiPhongChiTiet.setText(price.loaiPhong);
        lblGiaTheoGio.setText(price.getDisplayGiaTheoGio());
        lblGiaTheoNgay.setText(price.getDisplayGiaTheoNgay());
        lblGiaCuoiTuan.setText(price.getDisplayGiaCuoiTuan());
        lblGiaLe.setText(price.getDisplayGiaLe());
        lblPhuThuExtraBed.setText(price.getDisplayPhuThuExtraBed());
        lblPhuThuQuaGio.setText(price.getDisplayPhuThuQuaGio());
        lblTrangThaiChiTiet.setText(price.trangThai);
        txtGhiChu.setText(price.ghiChu);
        txtGhiChu.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblMaBangGia.setText("-");
        lblTenBangGia.setText("-");
        lblLoaiPhongChiTiet.setText("-");
        lblGiaTheoGio.setText("-");
        lblGiaTheoNgay.setText("-");
        lblGiaCuoiTuan.setText("-");
        lblGiaLe.setText("-");
        lblPhuThuExtraBed.setText("-");
        lblPhuThuQuaGio.setText("-");
        lblTrangThaiChiTiet.setText("-");
        txtGhiChu.setText("Không có dữ liệu phù hợp.");
    }

    private PriceRecord getSelectedPrice() {
        int row = tblBangGia.getSelectedRow();
        if (row < 0 || row >= filteredPrices.size()) {
            showWarning("Vui lòng chọn một bảng giá trong danh sách.");
            return null;
        }
        return filteredPrices.get(row);
    }

    private void openCreatePriceTableDialog() {
        new PriceTableEditorDialog(this, null).setVisible(true);
    }

    private void openUpdatePriceTableDialog() {
        PriceRecord price = getSelectedPrice();
        if (price != null) {
            new PriceTableEditorDialog(this, price).setVisible(true);
        }
    }

    private void openDeactivatePriceTableDialog() {
        PriceRecord price = getSelectedPrice();
        if (price != null) {
            new DeactivatePriceTableDialog(this, price).setVisible(true);
        }
    }

    private void openViewPriceTableDialog() {
        PriceRecord price = getSelectedPrice();
        if (price != null) {
            new ViewPriceTableDialog(this, price).setVisible(true);
        }
    }

    private void addPriceTable(PriceRecord price, boolean keepDialogOpen) {
        allPrices.add(0, price);
        cboLoaiPhong.setSelectedIndex(0);
        cboMuaGia.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        selectPriceTable(price);
        showSuccess(keepDialogOpen ? "Thêm bảng giá thành công và sẵn sàng tạo bảng giá mới." : "Thêm bảng giá thành công.");
    }

    private void refreshPriceTableViews(PriceRecord price, String message) {
        applyFilters(false);
        selectPriceTable(price);
        showSuccess(message);
    }

    private void selectPriceTable(PriceRecord price) {
        if (price == null) {
            return;
        }
        int index = filteredPrices.indexOf(price);
        if (index >= 0) {
            tblBangGia.setRowSelectionInterval(index, index);
            updateDetailPanel(price);
        } else if (!filteredPrices.isEmpty()) {
            tblBangGia.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredPrices.get(0));
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
                "F1 Thêm bảng giá",
                "F2 Cập nhật",
                "F3 Ngừng áp dụng",
                "F4 Xem chi tiết",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "banggia-f1", this::openCreatePriceTableDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "banggia-f2", this::openUpdatePriceTableDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "banggia-f3", this::openDeactivatePriceTableDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "banggia-f4", this::openViewPriceTableDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "banggia-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "banggia-enter", () -> {
            PriceRecord price = getSelectedPrice();
            if (price != null) {
                showMessageDialog("Chi tiết bảng giá", "Đang xem chi tiết bảng giá " + price.maBangGia + ".", new Color(59, 130, 246));
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

    private JLabel createValueTag(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.US, "%,.0f", amount).replace(',', '.');
    }

    private Double parseMoney(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.trim().replace(".", ""));
            if (parsed < 0) {
                showError(errorMessage);
                return null;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            showError(errorMessage);
            return null;
        }
    }

    private Integer parseOptionalInt(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            showError(errorMessage);
            return null;
        }
    }

    private abstract class BasePriceDialog extends JDialog {
        protected BasePriceDialog(Frame owner, String title, int width, int height) {
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

    private final class PriceTableEditorDialog extends BasePriceDialog {
        private final PriceRecord editingRecord;
        private final boolean editing;
        private final List<PriceDetailRecord> detailRows = new ArrayList<PriceDetailRecord>();

        private PriceTableEditorDialog(Frame owner, PriceRecord record) {
            super(owner, record == null ? "Thêm bảng giá" : "Cập nhật bảng giá", 940, 700);
            this.editingRecord = record;
            this.editing = record != null;

            if (record != null) {
                for (PriceDetailRecord detail : record.details) {
                    detailRows.add(detail.copy());
                }
            }

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    editing ? "CẬP NHẬT BẢNG GIÁ" : "THÊM BẢNG GIÁ",
                    "Phần trên là thông tin chung của bảng giá, phần dưới là các dòng chi tiết mức giá theo mô hình header-detail."
            ), BorderLayout.NORTH);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            body.add(buildHeaderSection(), BorderLayout.NORTH);
            body.add(buildDetailSection(), BorderLayout.CENTER);

            content.add(body, BorderLayout.CENTER);

            JButton btnPrimary = createPrimaryButton(editing ? "Lưu cập nhật" : "Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true));
            btnSaveNew.setVisible(!editing);
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSaveNew, btnPrimary), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private JPanel buildHeaderSection() {
            JPanel section = createDialogCardPanel();
            section.add(new JLabel(""), BorderLayout.CENTER);
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);

            JLabel lblSection = new JLabel("HEADER - THÔNG TIN CHUNG");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            JTextField maField = createInputField(editing ? editingRecord.maBangGia : "");
            maField.setEditable(!editing);
            JTextField tenField = createInputField(editing ? editingRecord.tenBangGia : "");
            JComboBox<String> loaiField = createComboBox(ROOM_TYPES);
            if (editing) {
                loaiField.setSelectedItem(editingRecord.loaiPhong);
            }
            JTextField tuNgayField = createInputField(editing ? editingRecord.tuNgay : "01/04/2026");
            JTextField denNgayField = createInputField(editing ? editingRecord.denNgay : "31/12/2026");
            JComboBox<String> statusField = createComboBox(PRICE_STATUS_OPTIONS);
            if (editing) {
                statusField.setSelectedItem(editingRecord.trangThai);
            }
            JTextArea ghiChuField = createDialogTextArea(3);
            if (editing) {
                ghiChuField.setText(editingRecord.ghiChu);
            }

            addFormRow(form, gbc, 0, "Mã bảng giá", maField);
            addFormRow(form, gbc, 1, "Tên bảng giá", tenField);
            addFormRow(form, gbc, 2, "Loại phòng", loaiField);
            addFormRow(form, gbc, 3, "Từ ngày", tuNgayField);
            addFormRow(form, gbc, 4, "Đến ngày", denNgayField);
            addFormRow(form, gbc, 5, "Trạng thái đầu", statusField);
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(ghiChuField));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(form, BorderLayout.CENTER);
            section.removeAll();
            section.add(wrapper, BorderLayout.CENTER);

            this.headerMaField = maField;
            this.headerTenField = tenField;
            this.headerLoaiField = loaiField;
            this.headerTuNgayField = tuNgayField;
            this.headerDenNgayField = denNgayField;
            this.headerStatusField = statusField;
            this.headerGhiChuField = ghiChuField;
            return section;
        }

        private JTextField headerMaField;
        private JTextField headerTenField;
        private JComboBox<String> headerLoaiField;
        private JTextField headerTuNgayField;
        private JTextField headerDenNgayField;
        private JComboBox<String> headerStatusField;
        private JTextArea headerGhiChuField;

        private JPanel buildDetailSection() {
            JPanel section = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 10));
            wrapper.setOpaque(false);

            JLabel lblSection = new JLabel("DETAIL - CÁC DÒNG CHI TIẾT MỨC GIÁ");
            lblSection.setFont(SECTION_FONT);
            lblSection.setForeground(TEXT_PRIMARY);

            String[] columns = {"STT", "Loại ngày", "Khung giờ", "Giá qua đêm", "Giá theo giờ", "Phụ thu", "Ưu tiên", "Ghi chú"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(BODY_FONT);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.setOpaque(false);
            actions.add(createPrimaryButton("Thêm dòng CT", new Color(59, 130, 246), Color.WHITE, e -> openPriceDetailDialog(null)));
            actions.add(createOutlineButton("Sửa dòng CT", new Color(245, 158, 11), e -> editSelectedDetail()));
            actions.add(createOutlineButton("Xóa dòng CT", new Color(220, 38, 38), e -> removeSelectedDetail()));

            wrapper.add(lblSection, BorderLayout.NORTH);
            wrapper.add(actions, BorderLayout.CENTER);
            wrapper.add(scroll, BorderLayout.SOUTH);
            section.add(wrapper, BorderLayout.CENTER);

            this.detailTableModelRef = model;
            this.detailTableRef = table;
            refillDetailTable();
            return section;
        }

        private DefaultTableModel detailTableModelRef;
        private JTable detailTableRef;

        private void refillDetailTable() {
            detailTableModelRef.setRowCount(0);
            for (int i = 0; i < detailRows.size(); i++) {
                PriceDetailRecord detail = detailRows.get(i);
                detailTableModelRef.addRow(new Object[]{
                        i + 1,
                        detail.loaiNgay,
                        detail.khungGio,
                        detail.getGiaQuaDemLabel(),
                        detail.getGiaTheoGioLabel(),
                        detail.getPhuThuLabel(),
                        detail.uuTienApDung,
                        detail.ghiChu
                });
            }
        }

        private void openPriceDetailDialog(PriceDetailRecord detail) {
            new PriceDetailDialog(this, detail).setVisible(true);
        }

        private void editSelectedDetail() {
            int row = detailTableRef.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                showWarning("Vui lòng chọn một dòng chi tiết.");
                return;
            }
            openPriceDetailDialog(detailRows.get(row));
        }

        private void removeSelectedDetail() {
            int row = detailTableRef.getSelectedRow();
            if (row < 0 || row >= detailRows.size()) {
                showWarning("Vui lòng chọn một dòng chi tiết.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận xóa dòng chi tiết",
                    "Dòng chi tiết bảng giá đang chọn sẽ bị xóa khỏi bảng giá. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(220, 38, 38)
            )) {
                return;
            }
            detailRows.remove(row);
            refillDetailTable();
        }

        private void submit(boolean keepOpen) {
            String maBangGia = headerMaField.getText().trim();
            String tenBangGia = headerTenField.getText().trim();
            String tuNgay = headerTuNgayField.getText().trim();
            String denNgay = headerDenNgayField.getText().trim();

            if (maBangGia.isEmpty()) {
                showError("Mã bảng giá không được rỗng.");
                return;
            }
            if (!editing) {
                for (PriceRecord record : allPrices) {
                    if (record.maBangGia.equalsIgnoreCase(maBangGia)) {
                        showError("Mã bảng giá đã tồn tại.");
                        return;
                    }
                }
            }
            if (valueOf(headerLoaiField.getSelectedItem()).isEmpty()) {
                showError("Loại phòng là bắt buộc.");
                return;
            }
            if (tuNgay.isEmpty() || denNgay.isEmpty()) {
                showError("Vui lòng nhập đầy đủ Từ ngày và Đến ngày.");
                return;
            }
            if (tuNgay.compareTo(denNgay) > 0) {
                showError("Từ ngày phải nhỏ hơn hoặc bằng Đến ngày.");
                return;
            }
            if (detailRows.isEmpty()) {
                showError("Bảng giá phải có ít nhất 1 dòng chi tiết.");
                return;
            }

            PriceRecord target = editing ? editingRecord : PriceRecord.create(
                    maBangGia,
                    tenBangGia,
                    valueOf(headerLoaiField.getSelectedItem()),
                    tuNgay,
                    denNgay,
                    valueOf(headerStatusField.getSelectedItem()),
                    headerGhiChuField.getText().trim()
            );

            target.tenBangGia = tenBangGia;
            target.loaiPhong = valueOf(headerLoaiField.getSelectedItem());
            target.tuNgay = tuNgay;
            target.denNgay = denNgay;
            target.trangThai = valueOf(headerStatusField.getSelectedItem());
            target.ghiChu = headerGhiChuField.getText().trim();
            target.details.clear();
            for (PriceDetailRecord detail : detailRows) {
                target.details.add(detail.copy());
            }
            target.syncSummaryFromDetails();

            if (editing) {
                refreshPriceTableViews(target, "Cập nhật bảng giá thành công.");
                dispose();
            } else {
                addPriceTable(target, keepOpen);
                if (keepOpen) {
                    headerMaField.setText("");
                    headerTenField.setText("");
                    headerLoaiField.setSelectedIndex(0);
                    headerTuNgayField.setText("01/04/2026");
                    headerDenNgayField.setText("31/12/2026");
                    headerStatusField.setSelectedIndex(0);
                    headerGhiChuField.setText("");
                    detailRows.clear();
                    refillDetailTable();
                } else {
                    dispose();
                }
            }
        }

        private final class PriceDetailDialog extends BasePriceDialog {
            private final PriceDetailRecord editingDetail;
            private final JComboBox<String> cboLoaiNgay;
            private final JTextField txtKhungGio;
            private final JTextField txtGiaQuaDem;
            private final JTextField txtGiaTheoGio;
            private final JTextField txtPhuThu;
            private final JTextField txtUuTien;
            private final JTextArea txtGhiChu;

            private PriceDetailDialog(Dialog owner, PriceDetailRecord detail) {
                super((Frame) BangGiaGUI.this, detail == null ? "Thêm chi tiết bảng giá" : "Cập nhật chi tiết bảng giá", 560, 430);
                this.editingDetail = detail;

                JPanel content = new JPanel(new BorderLayout(0, 12));
                content.setOpaque(false);
                content.add(buildDialogHeader(
                        detail == null ? "THÊM CHI TIẾT BẢNG GIÁ" : "CẬP NHẬT CHI TIẾT BẢNG GIÁ",
                        "Khai báo một dòng detail của bảng giá trong cấu trúc ChiTietBangGia."
                ), BorderLayout.NORTH);

                JPanel card = createDialogCardPanel();
                JPanel form = createDialogFormPanel();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new java.awt.Insets(6, 0, 6, 12);
                gbc.anchor = GridBagConstraints.WEST;

                cboLoaiNgay = createComboBox(DAY_TYPE_OPTIONS);
                txtKhungGio = createInputField(detail == null ? "" : detail.khungGio);
                txtGiaQuaDem = createInputField(detail == null ? "" : String.valueOf((long) detail.giaQuaDem));
                txtGiaTheoGio = createInputField(detail == null ? "" : String.valueOf((long) detail.giaTheoGio));
                txtPhuThu = createInputField(detail == null ? "" : String.valueOf((long) detail.phuThu));
                txtUuTien = createInputField(detail == null ? "" : String.valueOf(detail.uuTienApDung));
                txtGhiChu = createDialogTextArea(3);
                if (detail != null) {
                    cboLoaiNgay.setSelectedItem(detail.loaiNgay);
                    txtGhiChu.setText(detail.ghiChu);
                }

                addFormRow(form, gbc, 0, "Loại ngày", cboLoaiNgay);
                addFormRow(form, gbc, 1, "Khung giờ", txtKhungGio);
                addFormRow(form, gbc, 2, "Giá qua đêm", txtGiaQuaDem);
                addFormRow(form, gbc, 3, "Giá theo giờ", txtGiaTheoGio);
                addFormRow(form, gbc, 4, "Phụ thu", txtPhuThu);
                addFormRow(form, gbc, 5, "Ưu tiên áp dụng", txtUuTien);
                addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(txtGhiChu));

                card.add(form, BorderLayout.CENTER);
                content.add(card, BorderLayout.CENTER);

                JButton btnPrimary = createPrimaryButton(detail == null ? "Lưu dòng" : "Lưu cập nhật", new Color(59, 130, 246), Color.WHITE, e -> submit(false));
                JButton btnSaveNext = createOutlineButton("Lưu và thêm tiếp", new Color(22, 163, 74), e -> submit(true));
                btnSaveNext.setVisible(detail == null);
                JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
                content.add(buildDialogButtons(btnCancel, btnSaveNext, btnPrimary), BorderLayout.SOUTH);
                add(content, BorderLayout.CENTER);
            }

            private void submit(boolean keepOpen) {
                if (valueOf(cboLoaiNgay.getSelectedItem()).isEmpty()) {
                    showError("Loại ngày bắt buộc chọn.");
                    return;
                }
                if (txtKhungGio.getText().trim().isEmpty()) {
                    showError("Khung giờ không được rỗng.");
                    return;
                }
                Double giaQuaDem = parseMoney(txtGiaQuaDem.getText().trim(), "Giá qua đêm không hợp lệ.");
                Double giaTheoGio = parseMoney(txtGiaTheoGio.getText().trim(), "Giá theo giờ không hợp lệ.");
                if (giaQuaDem == null && giaTheoGio == null) {
                    showError("Ít nhất một trong hai trường Giá qua đêm hoặc Giá theo giờ phải có giá trị hợp lệ.");
                    return;
                }
                Double phuThu = parseMoney(txtPhuThu.getText().trim(), "Phụ thu không hợp lệ.");
                Integer uuTien = parseOptionalInt(txtUuTien.getText().trim(), "Ưu tiên áp dụng phải là số hợp lệ.");
                if (uuTien == null) {
                    return;
                }

                PriceDetailRecord target = editingDetail == null
                        ? new PriceDetailRecord(valueOf(cboLoaiNgay.getSelectedItem()), txtKhungGio.getText().trim(), giaQuaDem == null ? 0 : giaQuaDem, giaTheoGio == null ? 0 : giaTheoGio, phuThu == null ? 0 : phuThu, uuTien, txtGhiChu.getText().trim())
                        : editingDetail;

                target.loaiNgay = valueOf(cboLoaiNgay.getSelectedItem());
                target.khungGio = txtKhungGio.getText().trim();
                target.giaQuaDem = giaQuaDem == null ? 0 : giaQuaDem;
                target.giaTheoGio = giaTheoGio == null ? 0 : giaTheoGio;
                target.phuThu = phuThu == null ? 0 : phuThu;
                target.uuTienApDung = uuTien;
                target.ghiChu = txtGhiChu.getText().trim();

                if (editingDetail == null) {
                    detailRows.add(target);
                }
                refillDetailTable();
                showSuccess(editingDetail == null ? "Thêm dòng chi tiết thành công." : "Cập nhật dòng chi tiết thành công.");

                if (keepOpen && editingDetail == null) {
                    cboLoaiNgay.setSelectedIndex(0);
                    txtKhungGio.setText("");
                    txtGiaQuaDem.setText("");
                    txtGiaTheoGio.setText("");
                    txtPhuThu.setText("");
                    txtUuTien.setText("");
                    txtGhiChu.setText("");
                } else {
                    dispose();
                }
            }
        }
    }

    private final class DeactivatePriceTableDialog extends BasePriceDialog {
        private final PriceRecord price;
        private final JTextField txtTuNgay;
        private final JTextField txtLyDo;
        private final JTextArea txtGhiChuDialog;

        private DeactivatePriceTableDialog(Frame owner, PriceRecord price) {
            super(owner, "Ngừng áp dụng bảng giá", 580, 390);
            this.price = price;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("NGỪNG ÁP DỤNG BẢNG GIÁ", "Bảng giá này sẽ không còn được áp dụng cho nghiệp vụ mới sau thời điểm đã chọn."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTuNgay = createInputField(price.tuNgay);
            txtLyDo = createInputField("");
            txtGhiChuDialog = createDialogTextArea(3);

            addFormRow(form, gbc, 0, "Mã bảng giá", createValueTag(price.maBangGia));
            addFormRow(form, gbc, 1, "Tên bảng giá", createValueTag(price.tenBangGia));
            addFormRow(form, gbc, 2, "Loại phòng", createValueTag(price.loaiPhong));
            addFormRow(form, gbc, 3, "Trạng thái hiện tại", createValueTag(price.trangThai));
            addFormRow(form, gbc, 4, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 5, "Lý do", txtLyDo);
            addFormRow(form, gbc, 6, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận ngừng áp dụng bảng giá",
                    "Bảng giá này sẽ không còn được áp dụng cho nghiệp vụ mới. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(220, 38, 38)
            )) {
                return;
            }

            price.trangThai = "Ngừng áp dụng";
            price.denNgay = txtTuNgay.getText().trim();
            price.ghiChu = txtLyDo.getText().trim() + (txtGhiChuDialog.getText().trim().isEmpty() ? "" : ". " + txtGhiChuDialog.getText().trim());
            refreshPriceTableViews(price, "Ngừng áp dụng bảng giá thành công.");
            dispose();
        }
    }

    private final class ViewPriceTableDialog extends BasePriceDialog {
        private ViewPriceTableDialog(Frame owner, PriceRecord price) {
            super(owner, "Xem chi tiết bảng giá", 860, 620);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("XEM CHI TIẾT BẢNG GIÁ", "Phần trên là thông tin header, phần dưới là danh sách detail của bảng giá."), BorderLayout.NORTH);

            JPanel headerCard = createDialogCardPanel();
            JPanel headerForm = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;
            addFormRow(headerForm, gbc, 0, "Mã bảng giá", createValueTag(price.maBangGia));
            addFormRow(headerForm, gbc, 1, "Tên bảng giá", createValueTag(price.tenBangGia));
            addFormRow(headerForm, gbc, 2, "Loại phòng", createValueTag(price.loaiPhong));
            addFormRow(headerForm, gbc, 3, "Từ ngày", createValueTag(price.tuNgay));
            addFormRow(headerForm, gbc, 4, "Đến ngày", createValueTag(price.denNgay));
            addFormRow(headerForm, gbc, 5, "Trạng thái", createValueTag(price.trangThai));
            addFormRow(headerForm, gbc, 6, "Ghi chú", new JScrollPane(createReadonlyText(price.ghiChu)));
            headerCard.add(headerForm, BorderLayout.CENTER);

            JPanel detailCard = createDialogCardPanel();
            String[] columns = {"STT", "Loại ngày", "Khung giờ", "Giá qua đêm", "Giá theo giờ", "Phụ thu", "Ưu tiên", "Ghi chú"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(BODY_FONT);
            for (int i = 0; i < price.details.size(); i++) {
                PriceDetailRecord detail = price.details.get(i);
                model.addRow(new Object[]{i + 1, detail.loaiNgay, detail.khungGio, detail.getGiaQuaDemLabel(), detail.getGiaTheoGioLabel(), detail.getPhuThuLabel(), detail.uuTienApDung, detail.ghiChu});
            }
            detailCard.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel body = new JPanel(new BorderLayout(0, 12));
            body.setOpaque(false);
            body.add(headerCard, BorderLayout.NORTH);
            body.add(detailCard, BorderLayout.CENTER);
            content.add(body, BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", new Color(59, 130, 246), Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private JTextArea createReadonlyText(String text) {
        JTextArea area = createDialogTextArea(3);
        area.setEditable(false);
        area.setBackground(PANEL_SOFT);
        area.setText(text);
        return area;
    }

    private final class ConfirmDialog extends BasePriceDialog {
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

    private final class AppMessageDialog extends BasePriceDialog {
        private AppMessageDialog(Frame owner, String title, String message, Color accentColor) {
            super(owner, title, 430, 220);
            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(title, message), BorderLayout.CENTER);
            content.add(buildDialogButtons(createPrimaryButton("Đóng", accentColor, Color.WHITE, e -> dispose())), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }
    }

    private static final class PriceRecord {
        private String maBangGia;
        private String tenBangGia;
        private String loaiPhong;
        private String muaGia;
        private String tuNgay;
        private String denNgay;
        private double giaTheoGio;
        private double giaTheoNgay;
        private double giaCuoiTuan;
        private double giaLe;
        private double phuThuExtraBed;
        private double phuThuQuaGio;
        private String trangThai;
        private String ghiChu;
        private final List<PriceDetailRecord> details = new ArrayList<PriceDetailRecord>();

        private static PriceRecord create(String maBangGia, String tenBangGia, String loaiPhong, String tuNgay, String denNgay, String trangThai, String ghiChu) {
            PriceRecord record = new PriceRecord();
            record.maBangGia = maBangGia;
            record.tenBangGia = tenBangGia;
            record.loaiPhong = loaiPhong;
            record.tuNgay = tuNgay;
            record.denNgay = denNgay;
            record.trangThai = trangThai;
            record.ghiChu = ghiChu;
            record.muaGia = "Thường";
            return record;
        }

        private void syncSummaryFromDetails() {
            muaGia = details.isEmpty() ? "Thường" : details.get(0).loaiNgay;
            giaTheoGio = firstPositiveGiaTheoGio();
            giaTheoNgay = firstPositiveGiaQuaDem();
            giaCuoiTuan = findGiaQuaDem("Cuối tuần");
            giaLe = findGiaQuaDem("Ngày lễ");
            phuThuExtraBed = details.isEmpty() ? 0 : details.get(0).phuThu;
            phuThuQuaGio = details.isEmpty() ? 0 : details.get(0).giaTheoGio * 0.5;
        }

        private double firstPositiveGiaTheoGio() {
            for (PriceDetailRecord detail : details) {
                if (detail.giaTheoGio > 0) {
                    return detail.giaTheoGio;
                }
            }
            return 0;
        }

        private double firstPositiveGiaQuaDem() {
            for (PriceDetailRecord detail : details) {
                if (detail.giaQuaDem > 0) {
                    return detail.giaQuaDem;
                }
            }
            return 0;
        }

        private double findGiaQuaDem(String loaiNgayTarget) {
            for (PriceDetailRecord detail : details) {
                if (loaiNgayTarget.equals(detail.loaiNgay) && detail.giaQuaDem > 0) {
                    return detail.giaQuaDem;
                }
            }
            return firstPositiveGiaQuaDem();
        }

        private String getDisplayGiaTheoGio() {
            return giaTheoGio <= 0 ? "-" : String.format(Locale.US, "%,.0f", giaTheoGio).replace(',', '.');
        }

        private String getDisplayGiaTheoNgay() {
            return giaTheoNgay <= 0 ? "-" : String.format(Locale.US, "%,.0f", giaTheoNgay).replace(',', '.');
        }

        private String getDisplayGiaCuoiTuan() {
            return giaCuoiTuan <= 0 ? "-" : String.format(Locale.US, "%,.0f", giaCuoiTuan).replace(',', '.');
        }

        private String getDisplayGiaLe() {
            return giaLe <= 0 ? "-" : String.format(Locale.US, "%,.0f", giaLe).replace(',', '.');
        }

        private String getDisplayPhuThuExtraBed() {
            return phuThuExtraBed <= 0 ? "-" : String.format(Locale.US, "%,.0f", phuThuExtraBed).replace(',', '.');
        }

        private String getDisplayPhuThuQuaGio() {
            return phuThuQuaGio <= 0 ? "-" : String.format(Locale.US, "%,.0f", phuThuQuaGio).replace(',', '.');
        }
    }

    private static final class PriceDetailRecord {
        private String loaiNgay;
        private String khungGio;
        private double giaQuaDem;
        private double giaTheoGio;
        private double phuThu;
        private int uuTienApDung;
        private String ghiChu;

        private PriceDetailRecord(String loaiNgay, String khungGio, double giaQuaDem, double giaTheoGio, double phuThu, int uuTienApDung, String ghiChu) {
            this.loaiNgay = loaiNgay;
            this.khungGio = khungGio;
            this.giaQuaDem = giaQuaDem;
            this.giaTheoGio = giaTheoGio;
            this.phuThu = phuThu;
            this.uuTienApDung = uuTienApDung;
            this.ghiChu = ghiChu;
        }

        private PriceDetailRecord copy() {
            return new PriceDetailRecord(loaiNgay, khungGio, giaQuaDem, giaTheoGio, phuThu, uuTienApDung, ghiChu);
        }

        private String getGiaQuaDemLabel() {
            return giaQuaDem <= 0 ? "-" : String.format(Locale.US, "%,.0f", giaQuaDem).replace(',', '.');
        }

        private String getGiaTheoGioLabel() {
            return giaTheoGio <= 0 ? "-" : String.format(Locale.US, "%,.0f", giaTheoGio).replace(',', '.');
        }

        private String getPhuThuLabel() {
            return phuThu <= 0 ? "-" : String.format(Locale.US, "%,.0f", phuThu).replace(',', '.');
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