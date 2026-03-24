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

public class LoaiPhongGUI extends JFrame {
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
    private static final String[] ROOM_TYPE_STATUS_OPTIONS = {"Đang áp dụng", "Ngừng áp dụng"};
    private static final boolean[] DEFAULT_STANDARD_AMENITIES = {true, true, true, true, false, false, false, false, true, false, false, false};
    private static final boolean[] DEFAULT_DELUXE_AMENITIES = {true, true, true, true, true, true, true, true, true, false, true, true};
    private static final boolean[] DEFAULT_SUITE_AMENITIES = {true, true, true, true, true, true, true, true, true, true, true, true};
    private static final boolean[] DEFAULT_FAMILY_AMENITIES = {true, true, true, true, true, true, false, true, true, true, true, true};

    private final String username;
    private final String role;
    private JPanel rootPanel;
    private final List<RoomTypeRecord> allTypes = new ArrayList<RoomTypeRecord>();
    private final List<RoomTypeRecord> filteredTypes = new ArrayList<RoomTypeRecord>();

    private JTable tblLoaiPhong;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboTrangThai;
    private JComboBox<String> cboSucChua;
    private JTextField txtTuKhoa;

    private JLabel lblMaLoai;
    private JLabel lblTenLoai;
    private JTextArea txtMoTa;
    private JLabel lblSucChuaChuan;
    private JLabel lblKhachToiDa;
    private JLabel lblDienTich;
    private JLabel lblLoaiGiuong;
    private JLabel lblTrangThai;
    private JLabel lblGiaThamChieu;
    private JTextArea txtTienNghi;

    public LoaiPhongGUI() {
        this("guest", "Lễ tân");
    }

    public LoaiPhongGUI(String username, String role) {
        this.username = safeValue(username, "guest");
        this.role = safeValue(role, "Lễ tân");

        setTitle("Quản lý loại phòng - Hotel PMS");
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

        root.add(SidebarFactory.createSidebar(this, ScreenKey.LOAI_PHONG, username, role), BorderLayout.WEST);
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

        JLabel lblTitle = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ LOẠI PHÒNG"));
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Thiết lập sức chứa, diện tích, mô tả và tiện nghi mặc định áp dụng chung cho các phòng thuộc cùng loại.");
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
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Loại phòng"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActionBar() {
        JPanel card = createCompactCardPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton btnThemLoaiPhong = createPrimaryButton("Thêm loại phòng", new Color(22, 163, 74), Color.WHITE, e -> openCreateRoomTypeDialog());
        JButton btnCapNhat = createPrimaryButton("Cập nhật", new Color(37, 99, 235), Color.WHITE, e -> openUpdateRoomTypeDialog());
        JButton btnNgungApDung = createPrimaryButton("Ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> openDeactivateRoomTypeDialog());
        JButton btnTienNghiMacDinh = createPrimaryButton("Tiện nghi mặc định", new Color(99, 102, 241), Color.WHITE, e -> openDefaultAmenitiesDialog());
        JButton btnLamMoi = createPrimaryButton("Làm mới", new Color(107, 114, 128), Color.WHITE, e -> reloadSampleData(true));
        JButton btnTimKiem = createPrimaryButton("Tìm kiếm", new Color(15, 118, 110), Color.WHITE, e -> applyFilters(true));

        btnTienNghiMacDinh.setToolTipText("Thiết lập bộ tiện nghi mặc định áp dụng chung cho mọi phòng thuộc loại này.");

        card.add(btnThemLoaiPhong);
        card.add(btnCapNhat);
        card.add(btnNgungApDung);
        card.add(btnTienNghiMacDinh);
        card.add(btnLamMoi);
        card.add(btnTimKiem);
        return card;
    }

    private JPanel buildFilterBar() {
        JPanel card = createCardPanel(new BorderLayout(12, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        cboTrangThai = createComboBox(new String[]{"Tất cả", "Đang áp dụng", "Ngừng áp dụng"});
        cboSucChua = createComboBox(new String[]{"Tất cả", "2 người", "3 người", "4 người"});
        txtTuKhoa = createInputField("");
        txtTuKhoa.setPreferredSize(new Dimension(290, 34));
        txtTuKhoa.setToolTipText("Mã loại / tên loại phòng");

        left.add(createFieldGroup("Trạng thái", cboTrangThai));
        left.add(createFieldGroup("Sức chứa", cboSucChua));

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
                "F1 Thêm loại phòng",
                "F2 Cập nhật",
                "F3 Ngừng áp dụng",
                "F4 Tiện nghi mặc định",
                "F5 Làm mới",
                "Enter Xem chi tiết"
        );
    }

    private JPanel buildTableCard() {
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách loại phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Chọn một dòng để xem chi tiết loại phòng.");
        lblSub.setFont(BODY_FONT);
        lblSub.setForeground(TEXT_MUTED);

        titleRow.add(lblTitle, BorderLayout.WEST);
        titleRow.add(lblSub, BorderLayout.EAST);

        String[] columns = {
                "Mã loại",
                "Tên loại phòng",
                "Sức chứa chuẩn",
                "Khách tối đa",
                "Diện tích",
                "Trạng thái"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLoaiPhong = new JTable(tableModel);
        tblLoaiPhong.setFont(BODY_FONT);
        tblLoaiPhong.setRowHeight(32);
        tblLoaiPhong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLoaiPhong.setGridColor(BORDER_SOFT);
        tblLoaiPhong.setShowGrid(true);
        tblLoaiPhong.setFillsViewportHeight(true);
        tblLoaiPhong.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblLoaiPhong.getTableHeader().setBackground(new Color(243, 244, 246));
        tblLoaiPhong.getTableHeader().setForeground(TEXT_PRIMARY);

        tblLoaiPhong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblLoaiPhong.getSelectedRow();
                if (row >= 0 && row < filteredTypes.size()) {
                    updateDetailPanel(filteredTypes.get(row));
                }
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblLoaiPhong, this::openUpdateRoomTypeDialog);

        JScrollPane scrollPane = new JScrollPane(tblLoaiPhong);
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
        wrapper.add(buildImagePlaceholderCard(), BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel buildDetailCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Chi tiết loại phòng");
        lblTitle.setFont(SECTION_FONT);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel body = new JPanel(new GridLayout(8, 2, 10, 8));
        body.setOpaque(false);

        lblMaLoai = createValueLabel();
        lblTenLoai = createValueLabel();
        lblSucChuaChuan = createValueLabel();
        lblKhachToiDa = createValueLabel();
        lblDienTich = createValueLabel();
        lblLoaiGiuong = createValueLabel();
        lblTrangThai = createValueLabel();
        lblGiaThamChieu = createValueLabel();

        addDetailRow(body, "Mã loại", lblMaLoai);
        addDetailRow(body, "Tên loại", lblTenLoai);
        addDetailRow(body, "Sức chứa", lblSucChuaChuan);
        addDetailRow(body, "Khách tối đa", lblKhachToiDa);
        addDetailRow(body, "Diện tích", lblDienTich);
        addDetailRow(body, "Loại giường", lblLoaiGiuong);
        addDetailRow(body, "Trạng thái áp dụng", lblTrangThai);
        addDetailRow(body, "Giá tham chiếu", lblGiaThamChieu);

        JPanel lower = new JPanel(new GridLayout(1, 2, 10, 0));
        lower.setOpaque(false);

        txtMoTa = createReadonlyArea();
        txtTienNghi = createReadonlyArea();

        lower.add(createAreaCard("Mô tả", txtMoTa));
        lower.add(createAreaCard("Tiện nghi mặc định của loại phòng", txtTienNghi));

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

    private JPanel buildImagePlaceholderCard() {
        JPanel card = createCardPanel(new BorderLayout());

        JLabel lblTitle = new JLabel("Ảnh mô phỏng loại phòng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBackground(PANEL_SOFT);
        placeholder.setPreferredSize(new Dimension(0, 170));
        placeholder.setBorder(BorderFactory.createDashedBorder(new Color(203, 213, 225), 4, 4));

        JLabel lblPlaceholder = new JLabel("Khu vực ảnh loại phòng", SwingConstants.CENTER);
        lblPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblPlaceholder.setForeground(TEXT_MUTED);
        placeholder.add(lblPlaceholder, BorderLayout.CENTER);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(placeholder, BorderLayout.CENTER);
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

    private JPanel createWindowControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);
        panel.add(createWindowButton("[-]", e -> setState(JFrame.ICONIFIED)));
        panel.add(createWindowButton("[x]", e -> closeWindow()));
        return panel;
    }

    private JButton createWindowButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(new Color(243, 244, 246));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        button.addActionListener(listener);
        return button;
    }

    private void closeWindow() {
        if (showConfirmDialog(
                "Xác nhận",
                "Bạn có chắc muốn đóng màn hình Loại phòng?",
                "Đồng ý",
                new Color(59, 130, 246)
        )) {
            dispose();
        }
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
        allTypes.clear();
        allTypes.add(RoomTypeRecord.create("LP01", "Standard", "Phòng tiêu chuẩn cho khách công tác và lưu trú ngắn ngày.", 2, 3, 24.0, "1 giường Queen", "Đang áp dụng", 650000));
        allTypes.add(RoomTypeRecord.create("LP02", "Deluxe", "Phòng cao cấp với không gian rộng hơn và góc làm việc riêng.", 2, 4, 32.0, "1 giường King", "Đang áp dụng", 950000));
        allTypes.add(RoomTypeRecord.create("LP03", "Suite", "Phòng hạng sang dành cho khách VIP và gia đình nhỏ.", 3, 5, 45.0, "1 giường King + Sofa bed", "Đang áp dụng", 1450000));
        allTypes.add(RoomTypeRecord.create("LP04", "Family", "Phòng gia đình diện tích lớn, phù hợp khách đoàn nhỏ.", 4, 6, 50.0, "2 giường đôi", "Ngừng áp dụng", 1250000));
    }

    private void reloadSampleData(boolean showMessage) {
        cboTrangThai.setSelectedIndex(0);
        cboSucChua.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        if (showMessage) {
            showSuccess("Đã làm mới dữ liệu loại phòng.");
        }
    }

    private void applyFilters(boolean showMessage) {
        filteredTypes.clear();

        String trangThai = valueOf(cboTrangThai.getSelectedItem());
        String sucChua = valueOf(cboSucChua.getSelectedItem());
        String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);

        for (RoomTypeRecord type : allTypes) {
            if (!"Tất cả".equals(trangThai) && !type.trangThai.equals(trangThai)) {
                continue;
            }
            if (!"Tất cả".equals(sucChua) && !type.getSucChuaChuanLabel().equals(sucChua)) {
                continue;
            }
            if (!tuKhoa.isEmpty()) {
                String source = (type.maLoai + " " + type.tenLoaiPhong).toLowerCase(Locale.ROOT);
                if (!source.contains(tuKhoa)) {
                    continue;
                }
            }
            filteredTypes.add(type);
        }

        refillTable();
        if (showMessage) {
            showSuccess("Đã lọc được " + filteredTypes.size() + " loại phòng phù hợp.");
        }
    }

    private void refillTable() {
        tableModel.setRowCount(0);
        for (RoomTypeRecord type : filteredTypes) {
            tableModel.addRow(new Object[]{
                    type.maLoai,
                    type.tenLoaiPhong,
                    type.getSucChuaChuanLabel(),
                    type.getKhachToiDaLabel(),
                    type.getDienTichLabel(),
                    type.trangThai
            });
        }

        if (!filteredTypes.isEmpty()) {
            tblLoaiPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredTypes.get(0));
        } else {
            clearDetailPanel();
        }
    }

    private void updateDetailPanel(RoomTypeRecord type) {
        lblMaLoai.setText(type.maLoai);
        lblTenLoai.setText(type.tenLoaiPhong);
        lblSucChuaChuan.setText(type.getSucChuaChuanLabel());
        lblKhachToiDa.setText(type.getKhachToiDaLabel());
        lblDienTich.setText(type.getDienTichLabel());
        lblLoaiGiuong.setText(type.loaiGiuong);
        lblTrangThai.setText(type.trangThai);
        lblGiaThamChieu.setText(type.getGiaThamChieuLabel());
        txtMoTa.setText(type.moTa);
        txtMoTa.setCaretPosition(0);
        txtTienNghi.setText(type.buildAmenitiesSummary());
        txtTienNghi.setCaretPosition(0);
    }

    private void clearDetailPanel() {
        lblMaLoai.setText("-");
        lblTenLoai.setText("-");
        lblSucChuaChuan.setText("-");
        lblKhachToiDa.setText("-");
        lblDienTich.setText("-");
        lblLoaiGiuong.setText("-");
        lblTrangThai.setText("-");
        lblGiaThamChieu.setText("-");
        txtMoTa.setText("Không có dữ liệu phù hợp.");
        txtTienNghi.setText("Không có dữ liệu.");
    }

    private RoomTypeRecord getSelectedRoomType() {
        int row = tblLoaiPhong.getSelectedRow();
        if (row < 0 || row >= filteredTypes.size()) {
            showWarning("Vui lòng chọn một loại phòng trong danh sách.");
            return null;
        }
        return filteredTypes.get(row);
    }

    private void openCreateRoomTypeDialog() {
        new CreateRoomTypeDialog(this).setVisible(true);
    }

    private void openUpdateRoomTypeDialog() {
        RoomTypeRecord type = getSelectedRoomType();
        if (type != null) {
            new UpdateRoomTypeDialog(this, type).setVisible(true);
        }
    }

    private void openDeactivateRoomTypeDialog() {
        RoomTypeRecord type = getSelectedRoomType();
        if (type != null) {
            new DeactivateRoomTypeDialog(this, type).setVisible(true);
        }
    }

    private void openDefaultAmenitiesDialog() {
        RoomTypeRecord type = getSelectedRoomType();
        if (type != null) {
            new DefaultAmenitiesDialog(this, type).setVisible(true);
        }
    }

    private JCheckBox createAmenityCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(BODY_FONT);
        checkBox.setForeground(TEXT_PRIMARY);
        checkBox.setOpaque(false);
        return checkBox;
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "loaiphong-f1", this::openCreateRoomTypeDialog);
        ScreenUIHelper.registerShortcut(this, "F2", "loaiphong-f2", this::openUpdateRoomTypeDialog);
        ScreenUIHelper.registerShortcut(this, "F3", "loaiphong-f3", this::openDeactivateRoomTypeDialog);
        ScreenUIHelper.registerShortcut(this, "F4", "loaiphong-f4", this::openDefaultAmenitiesDialog);
        ScreenUIHelper.registerShortcut(this, "F5", "loaiphong-f5", () -> reloadSampleData(true));
        ScreenUIHelper.registerShortcut(this, "ENTER", "loaiphong-enter", () -> {
            RoomTypeRecord type = getSelectedRoomType();
            if (type != null) {
                showMessageDialog("Chi tiết loại phòng", "Đang xem chi tiết loại phòng " + type.maLoai + ".", new Color(59, 130, 246));
            }
        });
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }

    private void addRoomType(RoomTypeRecord type, boolean keepDialogOpen) {
        allTypes.add(0, type);
        cboTrangThai.setSelectedIndex(0);
        cboSucChua.setSelectedIndex(0);
        txtTuKhoa.setText("");
        applyFilters(false);
        selectRoomType(type);
        showSuccess(keepDialogOpen ? "Thêm loại phòng thành công và sẵn sàng tạo loại phòng mới." : "Thêm loại phòng thành công.");
    }

    private void refreshRoomTypeViews(RoomTypeRecord type, String message) {
        applyFilters(false);
        selectRoomType(type);
        showSuccess(message);
    }

    private void selectRoomType(RoomTypeRecord type) {
        if (type == null) {
            return;
        }
        int index = filteredTypes.indexOf(type);
        if (index >= 0) {
            tblLoaiPhong.setRowSelectionInterval(index, index);
            updateDetailPanel(type);
        } else if (!filteredTypes.isEmpty()) {
            tblLoaiPhong.setRowSelectionInterval(0, 0);
            updateDetailPanel(filteredTypes.get(0));
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

    private abstract class BaseRoomTypeDialog extends JDialog {
        private final int minimumWidth;
        private final int minimumHeight;

        protected BaseRoomTypeDialog(Frame owner, String title, int width, int height) {
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

    private final class CreateRoomTypeDialog extends BaseRoomTypeDialog {
        private final JTextField txtMaLoai;
        private final JTextField txtTenLoai;
        private final JTextField txtSucChuaChuanDialog;
        private final JTextField txtKhachToiDaDialog;
        private final JTextField txtDienTichDialog;
        private final JTextField txtGiaThamChieuDialog;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JTextArea txtMoTaDialog;

        private CreateRoomTypeDialog(Frame owner) {
            super(owner, "Thêm loại phòng", 640, 540);

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Thêm loại phòng mới", "Nhập thông tin cơ bản và trạng thái đầu cho loại phòng mới."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMaLoai = createInputField("");
            txtTenLoai = createInputField("");
            txtSucChuaChuanDialog = createInputField("");
            txtKhachToiDaDialog = createInputField("");
            txtDienTichDialog = createInputField("");
            txtGiaThamChieuDialog = createInputField("");
            cboTrangThaiDialog = createComboBox(ROOM_TYPE_STATUS_OPTIONS);
            txtMoTaDialog = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Mã loại phòng", txtMaLoai);
            addFormRow(form, gbc, 1, "Tên loại phòng", txtTenLoai);
            addFormRow(form, gbc, 2, "Sức chứa chuẩn", txtSucChuaChuanDialog);
            addFormRow(form, gbc, 3, "Khách tối đa", txtKhachToiDaDialog);
            addFormRow(form, gbc, 4, "Diện tích", txtDienTichDialog);
            addFormRow(form, gbc, 5, "Giá tham chiếu", txtGiaThamChieuDialog);
            addFormRow(form, gbc, 6, "Trạng thái đầu", cboTrangThaiDialog);
            addFormRow(form, gbc, 7, "Mô tả", new JScrollPane(txtMoTaDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(22, 163, 74), Color.WHITE, e -> submit(false));
            JButton btnSaveNew = createOutlineButton("Lưu và tạo mới", new Color(37, 99, 235), e -> submit(true));
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSaveNew, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit(boolean keepOpen) {
            String maLoai = txtMaLoai.getText().trim();
            String tenLoai = txtTenLoai.getText().trim();
            if (maLoai.isEmpty()) {
                showError("Mã loại phòng không được để trống.");
                return;
            }
            for (RoomTypeRecord type : allTypes) {
                if (type.maLoai.equalsIgnoreCase(maLoai)) {
                    showError("Mã loại phòng đã tồn tại.");
                    return;
                }
            }
            if (tenLoai.isEmpty()) {
                showError("Tên loại phòng không được để trống.");
                return;
            }

            Integer sucChuaChuan = parsePositiveInt(txtSucChuaChuanDialog.getText().trim(), "Sức chứa chuẩn phải lớn hơn 0.");
            if (sucChuaChuan == null) {
                return;
            }
            Integer khachToiDa = parsePositiveInt(txtKhachToiDaDialog.getText().trim(), "Khách tối đa phải là số hợp lệ và không nhỏ hơn sức chứa chuẩn.");
            if (khachToiDa == null || khachToiDa < sucChuaChuan) {
                showError("Khách tối đa phải là số hợp lệ và không nhỏ hơn sức chứa chuẩn.");
                return;
            }
            Double dienTich = parseNonNegativeDouble(txtDienTichDialog.getText().trim(), "Diện tích phải là số hợp lệ.");
            if (dienTich == null || dienTich <= 0) {
                showError("Diện tích phải lớn hơn 0.");
                return;
            }
            Double giaThamChieu = parseNonNegativeDouble(txtGiaThamChieuDialog.getText().trim(), "Giá tham chiếu phải là số hợp lệ.");
            if (giaThamChieu == null) {
                return;
            }

            RoomTypeRecord type = RoomTypeRecord.create(
                    maLoai,
                    tenLoai,
                    txtMoTaDialog.getText().trim().isEmpty() ? "Loại phòng mới được tạo từ popup." : txtMoTaDialog.getText().trim(),
                    sucChuaChuan,
                    khachToiDa,
                    dienTich,
                    inferBedType(tenLoai),
                    valueOf(cboTrangThaiDialog.getSelectedItem()),
                    giaThamChieu
            );
            type.applyDefaultAmenitiesByName(type.tenLoaiPhong);

            addRoomType(type, keepOpen);
            if (keepOpen) {
                resetForm();
            } else {
                dispose();
            }
        }

        private void resetForm() {
            txtMaLoai.setText("");
            txtTenLoai.setText("");
            txtSucChuaChuanDialog.setText("");
            txtKhachToiDaDialog.setText("");
            txtDienTichDialog.setText("");
            txtGiaThamChieuDialog.setText("");
            cboTrangThaiDialog.setSelectedIndex(0);
            txtMoTaDialog.setText("");
            txtMaLoai.requestFocusInWindow();
        }
    }

    private final class UpdateRoomTypeDialog extends BaseRoomTypeDialog {
        private final RoomTypeRecord type;
        private final JTextField txtMaLoai;
        private final JTextField txtTenLoai;
        private final JTextField txtSucChuaChuanDialog;
        private final JTextField txtKhachToiDaDialog;
        private final JTextField txtDienTichDialog;
        private final JTextField txtGiaThamChieuDialog;
        private final JComboBox<String> cboTrangThaiDialog;
        private final JTextArea txtMoTaDialog;

        private UpdateRoomTypeDialog(Frame owner, RoomTypeRecord type) {
            super(owner, "Cập nhật loại phòng", 640, 540);
            this.type = type;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Cập nhật loại phòng", "Chỉnh sửa thông tin cấu hình của loại phòng đang chọn."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtMaLoai = createInputField(type.maLoai);
            txtMaLoai.setEditable(false);
            txtTenLoai = createInputField(type.tenLoaiPhong);
            txtSucChuaChuanDialog = createInputField(String.valueOf(type.sucChuaChuan));
            txtKhachToiDaDialog = createInputField(String.valueOf(type.khachToiDa));
            txtDienTichDialog = createInputField(String.valueOf(type.dienTich));
            txtGiaThamChieuDialog = createInputField(String.valueOf((long) type.giaThamChieu));
            cboTrangThaiDialog = createComboBox(ROOM_TYPE_STATUS_OPTIONS);
            cboTrangThaiDialog.setSelectedItem(type.trangThai);
            txtMoTaDialog = createDialogTextArea(4);
            txtMoTaDialog.setText(type.moTa);

            addFormRow(form, gbc, 0, "Mã loại phòng", txtMaLoai);
            addFormRow(form, gbc, 1, "Tên loại phòng", txtTenLoai);
            addFormRow(form, gbc, 2, "Sức chứa chuẩn", txtSucChuaChuanDialog);
            addFormRow(form, gbc, 3, "Khách tối đa", txtKhachToiDaDialog);
            addFormRow(form, gbc, 4, "Diện tích", txtDienTichDialog);
            addFormRow(form, gbc, 5, "Giá tham chiếu", txtGiaThamChieuDialog);
            addFormRow(form, gbc, 6, "Trạng thái", cboTrangThaiDialog);
            addFormRow(form, gbc, 7, "Mô tả", new JScrollPane(txtMoTaDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu cập nhật", new Color(37, 99, 235), Color.WHITE, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            String tenLoai = txtTenLoai.getText().trim();
            if (tenLoai.isEmpty()) {
                showError("Tên loại phòng không được để trống.");
                return;
            }
            Integer sucChuaChuan = parsePositiveInt(txtSucChuaChuanDialog.getText().trim(), "Sức chứa chuẩn phải lớn hơn 0.");
            if (sucChuaChuan == null) {
                return;
            }
            Integer khachToiDa = parsePositiveInt(txtKhachToiDaDialog.getText().trim(), "Khách tối đa phải là số hợp lệ.");
            if (khachToiDa == null || khachToiDa < sucChuaChuan) {
                showError("Khách tối đa phải là số hợp lệ và không nhỏ hơn sức chứa chuẩn.");
                return;
            }
            Double dienTich = parseNonNegativeDouble(txtDienTichDialog.getText().trim(), "Diện tích phải là số hợp lệ.");
            if (dienTich == null || dienTich <= 0) {
                showError("Diện tích phải lớn hơn 0.");
                return;
            }
            Double giaThamChieu = parseNonNegativeDouble(txtGiaThamChieuDialog.getText().trim(), "Giá tham chiếu phải là số hợp lệ.");
            if (giaThamChieu == null) {
                return;
            }

            type.tenLoaiPhong = tenLoai;
            type.sucChuaChuan = sucChuaChuan;
            type.khachToiDa = khachToiDa;
            type.dienTich = dienTich;
            type.giaThamChieu = giaThamChieu;
            type.trangThai = valueOf(cboTrangThaiDialog.getSelectedItem());
            type.moTa = txtMoTaDialog.getText().trim().isEmpty() ? type.moTa : txtMoTaDialog.getText().trim();
            type.loaiGiuong = inferBedType(tenLoai);

            refreshRoomTypeViews(type, "Cập nhật loại phòng thành công.");
            dispose();
        }
    }

    private final class DeactivateRoomTypeDialog extends BaseRoomTypeDialog {
        private final RoomTypeRecord type;
        private final JTextField txtTuNgay;
        private final JTextField txtLyDo;
        private final JTextArea txtGhiChuDialog;

        private DeactivateRoomTypeDialog(Frame owner, RoomTypeRecord type) {
            super(owner, "Ngừng áp dụng loại phòng", 580, 400);
            this.type = type;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader("Ngừng áp dụng loại phòng", "Loại phòng sẽ không còn dùng cho booking mới hoặc cấu hình mới."), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel form = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            txtTuNgay = createInputField("19/03/2026");
            txtLyDo = createInputField("");
            txtGhiChuDialog = createDialogTextArea(4);

            addFormRow(form, gbc, 0, "Mã loại phòng", createValueTag(type.maLoai));
            addFormRow(form, gbc, 1, "Tên loại phòng", createValueTag(type.tenLoaiPhong));
            addFormRow(form, gbc, 2, "Trạng thái hiện tại", createValueTag(type.trangThai));
            addFormRow(form, gbc, 3, "Từ ngày", txtTuNgay);
            addFormRow(form, gbc, 4, "Lý do", txtLyDo);
            addFormRow(form, gbc, 5, "Ghi chú", new JScrollPane(txtGhiChuDialog));

            card.add(form, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnConfirm = createPrimaryButton("Xác nhận ngừng áp dụng", new Color(245, 158, 11), TEXT_PRIMARY, e -> submit());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnConfirm), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void submit() {
            if (txtLyDo.getText().trim().isEmpty()) {
                showError("Vui lòng nhập lý do ngừng áp dụng.");
                return;
            }
            if (!showConfirmDialog(
                    "Xác nhận ngừng áp dụng",
                    "Loại phòng sẽ không còn được dùng cho booking mới sau thời điểm áp dụng. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(220, 38, 38)
            )) {
                return;
            }

            type.trangThai = "Ngừng áp dụng";
            type.moTa = "Ngừng áp dụng từ " + txtTuNgay.getText().trim() + ". " + txtLyDo.getText().trim()
                    + (txtGhiChuDialog.getText().trim().isEmpty() ? "" : " " + txtGhiChuDialog.getText().trim());
            refreshRoomTypeViews(type, "Ngừng áp dụng loại phòng thành công.");
            dispose();
        }
    }

    private final class DefaultAmenitiesDialog extends BaseRoomTypeDialog {
        private final RoomTypeRecord type;
        private final JCheckBox chkDieuHoa;
        private final JCheckBox chkTv;
        private final JCheckBox chkWifi;
        private final JCheckBox chkNuocNong;
        private final JCheckBox chkBonTam;
        private final JCheckBox chkBanLamViec;
        private final JCheckBox chkMinibar;
        private final JCheckBox chkMaySayToc;
        private final JCheckBox chkKhoaTu;
        private final JCheckBox chkSofa;
        private final JCheckBox chkBanTra;
        private final JCheckBox chkTuQuanAoLon;
        private final JTextArea txtGhiChuDialog;

        private DefaultAmenitiesDialog(Frame owner, RoomTypeRecord type) {
            super(owner, "TIỆN NGHI MẶC ĐỊNH CỦA LOẠI PHÒNG", 720, 560);
            this.type = type;

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(buildDialogHeader(
                    "TIỆN NGHI MẶC ĐỊNH CỦA LOẠI PHÒNG",
                    "Các tiện nghi dưới đây là cấu hình mặc định áp dụng cho tất cả các phòng thuộc loại phòng này."
            ), BorderLayout.NORTH);

            JPanel card = createDialogCardPanel();
            JPanel wrapper = new JPanel(new BorderLayout(0, 12));
            wrapper.setOpaque(false);

            JPanel infoForm = createDialogFormPanel();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new java.awt.Insets(6, 0, 6, 12);
            gbc.anchor = GridBagConstraints.WEST;

            addFormRow(infoForm, gbc, 0, "Mã loại phòng", createValueTag(type.maLoai));
            addFormRow(infoForm, gbc, 1, "Tên loại phòng", createValueTag(type.tenLoaiPhong));

            JPanel checks = new JPanel(new GridLayout(6, 2, 10, 8));
            checks.setOpaque(false);

            chkDieuHoa = createAmenityCheckBox("Điều hòa");
            chkTv = createAmenityCheckBox("TV");
            chkWifi = createAmenityCheckBox("Wifi");
            chkNuocNong = createAmenityCheckBox("Nước nóng");
            chkBonTam = createAmenityCheckBox("Bồn tắm");
            chkBanLamViec = createAmenityCheckBox("Bàn làm việc");
            chkMinibar = createAmenityCheckBox("Minibar");
            chkMaySayToc = createAmenityCheckBox("Máy sấy tóc");
            chkKhoaTu = createAmenityCheckBox("Khóa từ");
            chkSofa = createAmenityCheckBox("Sofa");
            chkBanTra = createAmenityCheckBox("Bàn trà");
            chkTuQuanAoLon = createAmenityCheckBox("Tủ quần áo lớn");

            applyAmenitiesToChecks(type);

            checks.add(chkDieuHoa);
            checks.add(chkTv);
            checks.add(chkWifi);
            checks.add(chkNuocNong);
            checks.add(chkBonTam);
            checks.add(chkBanLamViec);
            checks.add(chkMinibar);
            checks.add(chkMaySayToc);
            checks.add(chkKhoaTu);
            checks.add(chkSofa);
            checks.add(chkBanTra);
            checks.add(chkTuQuanAoLon);

            txtGhiChuDialog = createDialogTextArea(4);

            wrapper.add(infoForm, BorderLayout.NORTH);
            wrapper.add(checks, BorderLayout.CENTER);
            wrapper.add(createAreaCard("Ghi chú", txtGhiChuDialog), BorderLayout.SOUTH);
            card.add(wrapper, BorderLayout.CENTER);
            content.add(card, BorderLayout.CENTER);

            JButton btnSave = createPrimaryButton("Lưu", new Color(99, 102, 241), Color.WHITE, e -> submit());
            JButton btnRestore = createOutlineButton("Khôi phục mặc định", new Color(245, 158, 11), e -> restoreDefaults());
            JButton btnCancel = createOutlineButton("Hủy", new Color(107, 114, 128), e -> dispose());
            content.add(buildDialogButtons(btnCancel, btnRestore, btnSave), BorderLayout.SOUTH);
            add(content, BorderLayout.CENTER);
        }

        private void applyAmenitiesToChecks(RoomTypeRecord source) {
            chkDieuHoa.setSelected(source.dieuHoa);
            chkTv.setSelected(source.tv);
            chkWifi.setSelected(source.wifi);
            chkNuocNong.setSelected(source.nuocNong);
            chkBonTam.setSelected(source.bonTam);
            chkBanLamViec.setSelected(source.banLamViec);
            chkMinibar.setSelected(source.minibar);
            chkMaySayToc.setSelected(source.maySayToc);
            chkKhoaTu.setSelected(source.khoaTu);
            chkSofa.setSelected(source.sofa);
            chkBanTra.setSelected(source.banTra);
            chkTuQuanAoLon.setSelected(source.tuQuanAoLon);
        }

        private void restoreDefaults() {
            if (!showConfirmDialog(
                    "Xác nhận khôi phục tiện nghi mặc định",
                    "Hệ thống sẽ khôi phục danh sách tiện nghi mặc định của loại phòng. Bạn có muốn tiếp tục không?",
                    "Đồng ý",
                    new Color(245, 158, 11)
            )) {
                return;
            }
            RoomTypeRecord snapshot = RoomTypeRecord.create(type.maLoai, type.tenLoaiPhong, type.moTa, type.sucChuaChuan, type.khachToiDa, type.dienTich, type.loaiGiuong, type.trangThai, type.giaThamChieu);
            snapshot.applyDefaultAmenitiesByName(type.tenLoaiPhong);
            applyAmenitiesToChecks(snapshot);
        }

        private void submit() {
            type.dieuHoa = chkDieuHoa.isSelected();
            type.tv = chkTv.isSelected();
            type.wifi = chkWifi.isSelected();
            type.nuocNong = chkNuocNong.isSelected();
            type.bonTam = chkBonTam.isSelected();
            type.banLamViec = chkBanLamViec.isSelected();
            type.minibar = chkMinibar.isSelected();
            type.maySayToc = chkMaySayToc.isSelected();
            type.khoaTu = chkKhoaTu.isSelected();
            type.sofa = chkSofa.isSelected();
            type.banTra = chkBanTra.isSelected();
            type.tuQuanAoLon = chkTuQuanAoLon.isSelected();
            if (!txtGhiChuDialog.getText().trim().isEmpty()) {
                type.moTa = txtGhiChuDialog.getText().trim();
            }
            refreshRoomTypeViews(type, "Cập nhật tiện nghi mặc định thành công.");
            dispose();
        }
    }

    private final class ConfirmDialog extends BaseRoomTypeDialog {
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

    private final class AppMessageDialog extends BaseRoomTypeDialog {
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

    private Integer parsePositiveInt(String value, String errorMessage) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                showError(errorMessage);
                return null;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            showError(errorMessage);
            return null;
        }
    }

    private Double parseNonNegativeDouble(String value, String errorMessage) {
        try {
            double parsed = Double.parseDouble(value);
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

    private String inferBedType(String tenLoai) {
        String lower = tenLoai.toLowerCase(Locale.ROOT);
        if (lower.contains("suite")) {
            return "1 giường King + Sofa bed";
        }
        if (lower.contains("family")) {
            return "2 giường đôi";
        }
        if (lower.contains("deluxe")) {
            return "1 giường King";
        }
        return "1 giường Queen";
    }

    private static final class RoomTypeRecord {
        private String maLoai;
        private String tenLoaiPhong;
        private String moTa;
        private int sucChuaChuan;
        private int khachToiDa;
        private double dienTich;
        private String loaiGiuong;
        private String trangThai;
        private double giaThamChieu;
        private boolean dieuHoa;
        private boolean tv;
        private boolean wifi;
        private boolean nuocNong;
        private boolean bonTam;
        private boolean banLamViec;
        private boolean minibar;
        private boolean maySayToc;
        private boolean khoaTu;
        private boolean sofa;
        private boolean banTra;
        private boolean tuQuanAoLon;

        private static RoomTypeRecord create(String maLoai, String tenLoaiPhong, String moTa, int sucChuaChuan,
                                             int khachToiDa, double dienTich, String loaiGiuong, String trangThai,
                                             double giaThamChieu) {
            RoomTypeRecord type = new RoomTypeRecord();
            type.maLoai = maLoai;
            type.tenLoaiPhong = tenLoaiPhong;
            type.moTa = moTa;
            type.sucChuaChuan = sucChuaChuan;
            type.khachToiDa = khachToiDa;
            type.dienTich = dienTich;
            type.loaiGiuong = loaiGiuong;
            type.trangThai = trangThai;
            type.giaThamChieu = giaThamChieu;
            type.applyDefaultAmenitiesByName(tenLoaiPhong);
            return type;
        }

        private void applyDefaultAmenitiesByName(String name) {
            boolean[] defaults = DEFAULT_STANDARD_AMENITIES;
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.contains("deluxe")) {
                defaults = DEFAULT_DELUXE_AMENITIES;
            } else if (lower.contains("suite")) {
                defaults = DEFAULT_SUITE_AMENITIES;
            } else if (lower.contains("family")) {
                defaults = DEFAULT_FAMILY_AMENITIES;
            }
            dieuHoa = defaults[0];
            tv = defaults[1];
            wifi = defaults[2];
            nuocNong = defaults[3];
            bonTam = defaults[4];
            banLamViec = defaults[5];
            minibar = defaults[6];
            maySayToc = defaults[7];
            khoaTu = defaults[8];
            sofa = defaults[9];
            banTra = defaults[10];
            tuQuanAoLon = defaults[11];
        }

        private String getSucChuaChuanLabel() {
            return sucChuaChuan + " người";
        }

        private String getKhachToiDaLabel() {
            return khachToiDa + " người";
        }

        private String getDienTichLabel() {
            return ((dienTich % 1 == 0) ? String.valueOf((int) dienTich) : String.valueOf(dienTich)) + " m2";
        }

        private String getGiaThamChieuLabel() {
            return String.format(Locale.US, "%,.0f / đêm", giaThamChieu).replace(',', '.');
        }

        private String buildAmenitiesSummary() {
            List<String> amenities = new ArrayList<String>();
            if (dieuHoa) amenities.add("Điều hòa");
            if (tv) amenities.add("TV");
            if (wifi) amenities.add("Wifi");
            if (nuocNong) amenities.add("Nước nóng");
            if (bonTam) amenities.add("Bồn tắm");
            if (banLamViec) amenities.add("Bàn làm việc");
            if (minibar) amenities.add("Minibar");
            if (maySayToc) amenities.add("Máy sấy tóc");
            if (khoaTu) amenities.add("Khóa từ");
            if (sofa) amenities.add("Sofa");
            if (banTra) amenities.add("Bàn trà");
            if (tuQuanAoLon) amenities.add("Tủ quần áo lớn");

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < amenities.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(amenities.get(i));
            }
            return builder.length() == 0 ? "Chưa cấu hình tiện nghi mặc định." : builder.toString();
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
