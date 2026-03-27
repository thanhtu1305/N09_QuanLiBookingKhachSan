package gui;

import dao.DichVuDAO;
import dao.SuDungDichVuDAO;
import entity.DichVu;
import entity.SuDungDichVu;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DichVuGUI extends JFrame {
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
    private static final String AUTO_CODE_TEXT = "AUTO";
    private static final String FILTER_ALL = "Tất cả";

    private final String username;
    private final String role;
    private final DichVuDAO dichVuDAO = new DichVuDAO();
    private final SuDungDichVuDAO suDungDichVuDAO = new SuDungDichVuDAO();
    private final List<DichVu> allServices = new ArrayList<DichVu>();
    private final List<DichVu> filteredServices = new ArrayList<DichVu>();

    private JPanel rootPanel;
    private JTable tblDichVu;
    private DefaultTableModel tableModel;
    private JComboBox<String> cboDonVi;
    private JTextField txtTuKhoa;
    private JLabel lblMaDichVu;
    private JLabel lblTenDichVu;
    private JLabel lblDonGia;
    private JLabel lblDonVi;
    private JTextArea txtLichSu;

    public DichVuGUI() {
        this("guest", "Lễ tân");
    }

    public DichVuGUI(String username, String role) {
        this.username = username == null || username.trim().isEmpty() ? "guest" : username.trim();
        this.role = role == null || role.trim().isEmpty() ? "Lễ tân" : role.trim();
        setTitle("Quản lý dịch vụ - Hotel PMS");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        loadServices(true, false);
        registerShortcuts();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(16, 0));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(SidebarFactory.createSidebar(this, ScreenKey.DICH_VU, username, role), BorderLayout.WEST);
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
        top.add(buildActions());
        top.add(Box.createVerticalStrut(10));
        top.add(buildFilters());
        main.add(top, BorderLayout.NORTH);
        main.add(buildCenter(), BorderLayout.CENTER);
        main.add(ScreenUIHelper.createShortcutBar(CARD_BG, BORDER_SOFT, TEXT_MUTED, "F1 Thêm", "F2 Cập nhật", "F3 Xóa", "F4 Sử dụng", "F5 Làm mới"), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildHeader() {
        JPanel card = card(new BorderLayout());
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(AppBranding.formatPageTitle("QUẢN LÝ DỊCH VỤ"));
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT_PRIMARY);
        JLabel sub = new JLabel("Quản lý danh mục dịch vụ và ghi nhận dịch vụ phát sinh theo lưu trú từ dữ liệu thật.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);
        JLabel meta = new JLabel("Người dùng: " + username + " | Vai trò: " + role);
        meta.setFont(BODY_FONT);
        meta.setForeground(TEXT_MUTED);
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);
        left.add(Box.createVerticalStrut(6));
        left.add(meta);
        card.add(left, BorderLayout.WEST);
        card.add(ScreenUIHelper.createWindowControlPanel(this, TEXT_PRIMARY, BORDER_SOFT, "màn hình Dịch vụ"), BorderLayout.EAST);
        return card;
    }

    private JPanel buildActions() {
        JPanel card = compactCard();
        card.add(primary("Thêm dịch vụ", new Color(22, 163, 74), e -> openServiceDialog(null)));
        card.add(primary("Cập nhật", new Color(37, 99, 235), e -> openServiceDialog(getSelectedService(true))));
        card.add(primary("Xóa dịch vụ", new Color(220, 38, 38), e -> deleteSelectedService()));
        card.add(primary("Sử dụng dịch vụ", new Color(99, 102, 241), e -> openUsageDialog(getSelectedService(false))));
        card.add(primary("Làm mới", new Color(107, 114, 128), e -> loadServices(true, true)));
        card.add(primary("Tìm kiếm", new Color(15, 118, 110), e -> applyFilters(true)));
        return card;
    }

    private JPanel buildFilters() {
        JPanel card = card(new BorderLayout(12, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        cboDonVi = combo(new String[]{FILTER_ALL});
        left.add(field("Đơn vị", cboDonVi));
        txtTuKhoa = input("");
        txtTuKhoa.setPreferredSize(new Dimension(300, 34));
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel lbl = new JLabel("Tìm kiếm");
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(TEXT_MUTED);
        right.add(lbl);
        right.add(Box.createVerticalStrut(4));
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.add(txtTuKhoa, BorderLayout.CENTER);
        row.add(outline("Lọc ngay", new Color(59, 130, 246), e -> applyFilters(true)), BorderLayout.EAST);
        right.add(row);
        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JSplitPane buildCenter() {
        tableModel = new DefaultTableModel(new String[]{"Mã dịch vụ", "Tên dịch vụ", "Đơn giá", "Đơn vị"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDichVu = new JTable(tableModel);
        tblDichVu.setFont(BODY_FONT);
        tblDichVu.setRowHeight(32);
        tblDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDichVu.setGridColor(BORDER_SOFT);
        tblDichVu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblDichVu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                DichVu dichVu = getSelectedService(false);
                if (dichVu != null) {
                    updateDetail(dichVu);
                }
            }
        });
        ScreenUIHelper.registerTableDoubleClick(tblDichVu, () -> openServiceDialog(getSelectedService(true)));

        JPanel left = card(new BorderLayout(0, 10));
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        JLabel t1 = new JLabel("Danh sách dịch vụ");
        t1.setFont(SECTION_FONT);
        t1.setForeground(TEXT_PRIMARY);
        JLabel t2 = new JLabel("Chọn một dòng để xem chi tiết.");
        t2.setFont(BODY_FONT);
        t2.setForeground(TEXT_MUTED);
        title.add(t1, BorderLayout.WEST);
        title.add(t2, BorderLayout.EAST);
        left.add(title, BorderLayout.NORTH);
        left.add(new JScrollPane(tblDichVu), BorderLayout.CENTER);

        JPanel right = card(new BorderLayout(0, 10));
        JLabel rt = new JLabel("Chi tiết dịch vụ");
        rt.setFont(SECTION_FONT);
        rt.setForeground(TEXT_PRIMARY);
        right.add(rt, BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        lblMaDichVu = value("-");
        lblTenDichVu = value("-");
        lblDonGia = value("-");
        lblDonVi = value("-");
        detailRow(body, "Mã dịch vụ", lblMaDichVu);
        detailRow(body, "Tên dịch vụ", lblTenDichVu);
        detailRow(body, "Đơn giá", lblDonGia);
        detailRow(body, "Đơn vị", lblDonVi);
        right.add(body, BorderLayout.CENTER);

        txtLichSu = new JTextArea(10, 20);
        txtLichSu.setEditable(false);
        txtLichSu.setLineWrap(true);
        txtLichSu.setWrapStyleWord(true);
        txtLichSu.setFont(BODY_FONT);
        txtLichSu.setBackground(PANEL_SOFT);
        txtLichSu.setBorder(new EmptyBorder(8, 10, 8, 10));
        JPanel usageCard = new JPanel(new BorderLayout(0, 6));
        usageCard.setOpaque(false);
        JLabel usageTitle = new JLabel("Lịch sử sử dụng gần đây");
        usageTitle.setFont(LABEL_FONT);
        usageTitle.setForeground(TEXT_MUTED);
        usageCard.add(usageTitle, BorderLayout.NORTH);
        usageCard.add(new JScrollPane(txtLichSu), BorderLayout.CENTER);
        right.add(usageCard, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBorder(null);
        split.setResizeWeight(0.58);
        split.setDividerSize(8);
        return split;
    }

    private void loadServices(boolean resetFilter, boolean showMessage) {
        allServices.clear();
        allServices.addAll(dichVuDAO.getAll());
        refreshDonVi(resetFilter);
        if (resetFilter) {
            txtTuKhoa.setText("");
        }
        applyFilters(false);
        refreshCurrentView();
        if (showMessage) {
            info("Đã làm mới dữ liệu dịch vụ.");
        }
    }

    private void refreshDonVi(boolean resetSelection) {
        Set<String> ds = new LinkedHashSet<String>();
        ds.add(FILTER_ALL);
        for (DichVu d : allServices) {
            if (d.getDonVi() != null && !d.getDonVi().trim().isEmpty()) {
                ds.add(d.getDonVi().trim());
            }
        }
        String selected = resetSelection ? FILTER_ALL : item(cboDonVi);
        cboDonVi.removeAllItems();
        for (String s : ds) {
            cboDonVi.addItem(s);
        }
        cboDonVi.setSelectedItem(ds.contains(selected) ? selected : FILTER_ALL);
    }

    private void applyFilters(boolean notify) {
        filteredServices.clear();
        String keyword = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim().toLowerCase(Locale.ROOT);
        String donVi = item(cboDonVi);
        for (DichVu d : allServices) {
            if (!FILTER_ALL.equals(donVi) && !safe(d.getDonVi()).equalsIgnoreCase(donVi)) {
                continue;
            }
            String source = (d.getMaDichVu() + " " + safe(d.getTenDichVu()) + " " + safe(d.getDonVi())).toLowerCase(Locale.ROOT);
            if (!keyword.isEmpty() && !source.contains(keyword)) {
                continue;
            }
            filteredServices.add(d);
        }
        tableModel.setRowCount(0);
        for (DichVu d : filteredServices) {
            tableModel.addRow(new Object[]{d.getMaDichVu(), d.getTenDichVu(), money(d.getDonGia()), d.getDonVi()});
        }
        if (!filteredServices.isEmpty()) {
            tblDichVu.setRowSelectionInterval(0, 0);
            updateDetail(filteredServices.get(0));
        } else {
            clearDetail();
        }
        refreshCurrentView();
        if (notify) {
            info("Đã lọc được " + filteredServices.size() + " dịch vụ phù hợp.");
        }
    }

    private void updateDetail(DichVu dichVu) {
        lblMaDichVu.setText(String.valueOf(dichVu.getMaDichVu()));
        lblTenDichVu.setText(safe(dichVu.getTenDichVu()).isEmpty() ? "-" : dichVu.getTenDichVu());
        lblDonGia.setText(money(dichVu.getDonGia()));
        lblDonVi.setText(safe(dichVu.getDonVi()).isEmpty() ? "-" : dichVu.getDonVi());
        List<SuDungDichVu> usages = suDungDichVuDAO.getByMaDichVu(dichVu.getMaDichVu());
        if (usages.isEmpty()) {
            txtLichSu.setText("Chưa có dữ liệu sử dụng dịch vụ.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (SuDungDichVu sd : usages) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("SD").append(sd.getMaSuDung())
                    .append(" | Lưu trú ").append(sd.getMaLuuTru())
                    .append(" | SL ").append(sd.getSoLuong())
                    .append(" | Thành tiền ").append(money(sd.getThanhTien()));
        }
        txtLichSu.setText(sb.toString());
        txtLichSu.setCaretPosition(0);
        refreshCurrentView();
    }

    private void clearDetail() {
        lblMaDichVu.setText("-");
        lblTenDichVu.setText("-");
        lblDonGia.setText("-");
        lblDonVi.setText("-");
        txtLichSu.setText("Không có dữ liệu phù hợp.");
        refreshCurrentView();
    }

    private DichVu getSelectedService(boolean warnIfMissing) {
        int row = tblDichVu == null ? -1 : tblDichVu.getSelectedRow();
        if (row < 0 || row >= filteredServices.size()) {
            if (warnIfMissing) {
                warn("Vui lòng chọn một dịch vụ trong danh sách.");
            }
            return null;
        }
        return filteredServices.get(row);
    }

    private void openServiceDialog(DichVu editing) {
        ServiceEditor dialog = new ServiceEditor(this, editing);
        dialog.setVisible(true);
    }

    private void deleteSelectedService() {
        DichVu dichVu = getSelectedService(true);
        if (dichVu == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dịch vụ này không?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        if (!dichVuDAO.delete(dichVu.getMaDichVu())) {
            error("Không thể xóa dịch vụ. Có thể dịch vụ đã phát sinh dữ liệu sử dụng.");
            return;
        }
        loadServices(false, false);
        info("Xóa dịch vụ thành công.");
    }

    private void openUsageDialog(DichVu preselected) {
        if (allServices.isEmpty()) {
            warn("Chưa có dịch vụ để ghi nhận sử dụng.");
            return;
        }
        ServiceUsageEditor dialog = new ServiceUsageEditor(this, preselected);
        dialog.setVisible(true);
    }

    private void registerShortcuts() {
        ScreenUIHelper.registerShortcut(this, "F1", "dichvu-f1", () -> openServiceDialog(null));
        ScreenUIHelper.registerShortcut(this, "F2", "dichvu-f2", () -> openServiceDialog(getSelectedService(true)));
        ScreenUIHelper.registerShortcut(this, "F3", "dichvu-f3", this::deleteSelectedService);
        ScreenUIHelper.registerShortcut(this, "F4", "dichvu-f4", () -> openUsageDialog(getSelectedService(false)));
        ScreenUIHelper.registerShortcut(this, "F5", "dichvu-f5", () -> loadServices(true, true));
    }

    private class ServiceEditor extends JDialog {
        private ServiceEditor(Frame owner, DichVu editing) {
            super(owner, editing == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ", true);
            getContentPane().setBackground(APP_BG);
            setLayout(new BorderLayout(0, 12));
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JTextField txtMa = input(editing == null ? AUTO_CODE_TEXT : String.valueOf(editing.getMaDichVu()));
            JTextField txtTen = input(editing == null ? "" : editing.getTenDichVu());
            JTextField txtDonGia = input(editing == null ? "0" : String.valueOf((long) editing.getDonGia()));
            JTextField txtDonVi = input(editing == null ? "" : editing.getDonVi());
            txtMa.setEditable(false);

            JPanel form = createForm();
            addForm(form, 0, "Mã dịch vụ", txtMa);
            addForm(form, 1, "Tên dịch vụ", txtTen);
            addForm(form, 2, "Đơn giá", txtDonGia);
            addForm(form, 3, "Đơn vị", txtDonVi);

            add(dialogHeader(editing == null ? "Thêm dịch vụ mới" : "Cập nhật dịch vụ", "Nhập đúng dữ liệu của bảng DichVu."), BorderLayout.NORTH);
            add(cardWrap(form), BorderLayout.CENTER);
            add(buttonBar(
                    outline("Hủy", new Color(107, 114, 128), e -> dispose()),
                    primary(editing == null ? "Lưu" : "Lưu cập nhật", new Color(22, 163, 74), e -> {
                        if (txtTen.getText().trim().isEmpty()) {
                            warn("Tên dịch vụ không được rỗng.");
                            return;
                        }
                        Double donGia = parseNonNegative(txtDonGia.getText(), "Đơn giá phải là số hợp lệ >= 0.");
                        if (donGia == null) {
                            return;
                        }
                        if (txtDonVi.getText().trim().isEmpty()) {
                            warn("Đơn vị không được rỗng.");
                            return;
                        }
                        DichVu dichVu = editing == null
                                ? new DichVu(txtTen.getText().trim(), donGia, txtDonVi.getText().trim())
                                : new DichVu(editing.getMaDichVu(), txtTen.getText().trim(), donGia, txtDonVi.getText().trim());
                        boolean ok = editing == null ? dichVuDAO.insert(dichVu) : dichVuDAO.update(dichVu);
                        if (!ok) {
                            error(editing == null ? "Không thể thêm dịch vụ." : "Không thể cập nhật dịch vụ.");
                            return;
                        }
                        loadServices(false, false);
                        info(editing == null ? "Thêm dịch vụ thành công." : "Cập nhật dịch vụ thành công.");
                        dispose();
                    })
            ), BorderLayout.SOUTH);
            ScreenUIHelper.prepareDialog(this, owner, 560, 340);
        }
    }

    private class ServiceUsageEditor extends JDialog {
        private final List<SuDungDichVu> usageRows = new ArrayList<SuDungDichVu>();
        private final SuDungDichVu[] selected = new SuDungDichVu[1];

        private ServiceUsageEditor(Frame owner, DichVu preselected) {
            super(owner, "Sử dụng dịch vụ", true);
            getContentPane().setBackground(APP_BG);
            setLayout(new BorderLayout(0, 12));
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JComboBox<String> cboMaLuuTru = combo(maLuuTruOptions());
            JComboBox<String> cboDichVu = combo(dichVuOptions());
            JTextField txtSoLuong = input("1");
            JTextField txtDonGia = input("0");
            JTextField txtThanhTien = input("0");
            txtThanhTien.setEditable(false);
            if (preselected != null) {
                cboDichVu.setSelectedItem(display(preselected));
                txtDonGia.setText(String.valueOf((long) preselected.getDonGia()));
            }

            DefaultTableModel usageModel = new DefaultTableModel(new String[]{"Mã sử dụng", "Tên dịch vụ", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable tblUsage = new JTable(usageModel);

            Runnable fillPrice = () -> {
                DichVu dichVu = serviceFromDisplay(item(cboDichVu));
                if (dichVu != null) {
                    txtDonGia.setText(String.valueOf((long) dichVu.getDonGia()));
                }
                updateThanhTien(txtSoLuong, txtDonGia, txtThanhTien);
            };
            Runnable reloadUsage = () -> reloadUsageRows(cboMaLuuTru, usageModel);

            cboDichVu.addActionListener(e -> fillPrice.run());
            cboMaLuuTru.addActionListener(e -> reloadUsage.run());
            installAmountListeners(txtSoLuong, txtDonGia, txtThanhTien);
            tblUsage.getSelectionModel().addListSelectionListener(e -> populateUsageSelection(tblUsage, cboMaLuuTru, cboDichVu, txtSoLuong, txtDonGia, txtThanhTien));

            JPanel form = createForm();
            addForm(form, 0, "Mã lưu trú", cboMaLuuTru);
            addForm(form, 1, "Dịch vụ", cboDichVu);
            addForm(form, 2, "Số lượng", txtSoLuong);
            addForm(form, 3, "Đơn giá", txtDonGia);
            addForm(form, 4, "Thành tiền", txtThanhTien);

            JPanel center = new JPanel(new BorderLayout(0, 12));
            center.setOpaque(false);
            center.add(cardWrap(form), BorderLayout.NORTH);
            JPanel tableCard = card(new BorderLayout(0, 8));
            JLabel title = new JLabel("Danh sách dịch vụ đã dùng theo lưu trú");
            title.setFont(SECTION_FONT);
            title.setForeground(TEXT_PRIMARY);
            tableCard.add(title, BorderLayout.NORTH);
            tableCard.add(new JScrollPane(tblUsage), BorderLayout.CENTER);
            center.add(tableCard, BorderLayout.CENTER);

            add(dialogHeader("Ghi nhận sử dụng dịch vụ", "Lưu dữ liệu thật vào bảng SuDungDichVu."), BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
            add(buttonBar(
                    outline("Đóng", new Color(107, 114, 128), e -> dispose()),
                    primary("Xóa", new Color(220, 38, 38), e -> deleteUsage()),
                    primary("Cập nhật", new Color(37, 99, 235), e -> saveUsage(true)),
                    primary("Thêm", new Color(22, 163, 74), e -> saveUsage(false))
            ), BorderLayout.SOUTH);

            fillPrice.run();
            reloadUsage.run();
            ScreenUIHelper.prepareDialog(this, owner, 820, 620);

            voidUpdateContext = new UsageContext(cboMaLuuTru, cboDichVu, txtSoLuong, txtDonGia, txtThanhTien, usageModel);
        }

        private final UsageContext voidUpdateContext;

        private void saveUsage(boolean editing) {
            UsageContext c = voidUpdateContext;
            SuDungDichVu suDung = usageFromForm(selected[0], editing, c.cboMaLuuTru, c.cboDichVu, c.txtSoLuong, c.txtDonGia);
            if (suDung == null) {
                return;
            }
            boolean ok = editing ? suDungDichVuDAO.updateSuDungDichVu(suDung) : suDungDichVuDAO.insertSuDungDichVu(suDung);
            if (!ok) {
                error(editing ? "Không thể cập nhật dịch vụ sử dụng." : "Không thể thêm dịch vụ sử dụng.");
                return;
            }
            info(editing ? "Cập nhật dịch vụ sử dụng thành công." : "Thêm dịch vụ sử dụng thành công.");
            reloadUsageRows(c.cboMaLuuTru, c.usageModel);
            loadServices(false, false);
            DichVu cur = getSelectedService(false);
            if (cur != null) {
                updateDetail(cur);
            }
        }

        private void deleteUsage() {
            if (selected[0] == null) {
                warn("Vui lòng chọn một dòng dịch vụ đã dùng.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa dòng sử dụng dịch vụ này không?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            if (!suDungDichVuDAO.deleteSuDungDichVu(selected[0].getMaSuDung())) {
                error("Không thể xóa dịch vụ sử dụng.");
                return;
            }
            selected[0] = null;
            reloadUsageRows(voidUpdateContext.cboMaLuuTru, voidUpdateContext.usageModel);
            loadServices(false, false);
            DichVu cur = getSelectedService(false);
            if (cur != null) {
                updateDetail(cur);
            }
            info("Xóa dịch vụ sử dụng thành công.");
        }

        private void reloadUsageRows(JComboBox<String> cboMaLuuTru, DefaultTableModel usageModel) {
            usageModel.setRowCount(0);
            usageRows.clear();
            if (item(cboMaLuuTru).isEmpty()) {
                return;
            }
            usageRows.addAll(suDungDichVuDAO.getByMaLuuTru(Integer.parseInt(item(cboMaLuuTru))));
            for (SuDungDichVu sd : usageRows) {
                usageModel.addRow(new Object[]{sd.getMaSuDung(), sd.getTenDichVu(), sd.getSoLuong(), money(sd.getDonGia()), money(sd.getThanhTien())});
            }
        }

        private void populateUsageSelection(JTable tblUsage, JComboBox<String> cboMaLuuTru, JComboBox<String> cboDichVu, JTextField txtSoLuong, JTextField txtDonGia, JTextField txtThanhTien) {
            int row = tblUsage.getSelectedRow();
            if (row < 0 || row >= usageRows.size()) {
                return;
            }
            SuDungDichVu sd = usageRows.get(row);
            selected[0] = sd;
            cboMaLuuTru.setSelectedItem(String.valueOf(sd.getMaLuuTru()));
            cboDichVu.setSelectedItem(serviceDisplayById(sd.getMaDichVu()));
            txtSoLuong.setText(String.valueOf(sd.getSoLuong()));
            txtDonGia.setText(String.valueOf((long) sd.getDonGia()));
            txtThanhTien.setText(money(sd.getThanhTien()));
        }
    }

    private static final class UsageContext {
        private final JComboBox<String> cboMaLuuTru;
        private final JComboBox<String> cboDichVu;
        private final JTextField txtSoLuong;
        private final JTextField txtDonGia;
        private final JTextField txtThanhTien;
        private final DefaultTableModel usageModel;

        private UsageContext(JComboBox<String> cboMaLuuTru, JComboBox<String> cboDichVu, JTextField txtSoLuong, JTextField txtDonGia, JTextField txtThanhTien, DefaultTableModel usageModel) {
            this.cboMaLuuTru = cboMaLuuTru;
            this.cboDichVu = cboDichVu;
            this.txtSoLuong = txtSoLuong;
            this.txtDonGia = txtDonGia;
            this.txtThanhTien = txtThanhTien;
            this.usageModel = usageModel;
        }
    }

    private SuDungDichVu usageFromForm(SuDungDichVu selected, boolean editing, JComboBox<String> cboMaLuuTru, JComboBox<String> cboDichVu, JTextField txtSoLuong, JTextField txtDonGia) {
        if (item(cboMaLuuTru).isEmpty()) {
            warn("Mã lưu trú không hợp lệ.");
            return null;
        }
        DichVu dichVu = serviceFromDisplay(item(cboDichVu));
        if (dichVu == null) {
            warn("Dịch vụ không hợp lệ.");
            return null;
        }
        Integer soLuong = parsePositive(txtSoLuong.getText(), "Số lượng phải là số nguyên > 0.");
        if (soLuong == null) {
            return null;
        }
        Double donGia = parseNonNegative(txtDonGia.getText(), "Đơn giá phải là số hợp lệ >= 0.");
        if (donGia == null) {
            return null;
        }
        SuDungDichVu sd = new SuDungDichVu(Integer.parseInt(item(cboMaLuuTru)), dichVu.getMaDichVu(), soLuong, donGia);
        if (editing) {
            if (selected == null) {
                warn("Vui lòng chọn một dòng dịch vụ đã dùng.");
                return null;
            }
            sd.setMaSuDung(selected.getMaSuDung());
        }
        return sd;
    }

    private String[] maLuuTruOptions() {
        List<Integer> values = suDungDichVuDAO.getAvailableMaLuuTru();
        String[] options = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            options[i] = String.valueOf(values.get(i));
        }
        return options;
    }

    private String[] dichVuOptions() {
        String[] options = new String[allServices.size()];
        for (int i = 0; i < allServices.size(); i++) {
            options[i] = display(allServices.get(i));
        }
        return options;
    }

    private String display(DichVu dichVu) {
        return dichVu.getMaDichVu() + " - " + dichVu.getTenDichVu();
    }

    private DichVu serviceFromDisplay(String display) {
        for (DichVu d : allServices) {
            if (display(d).equals(display)) {
                return d;
            }
        }
        return null;
    }

    private String serviceDisplayById(int maDichVu) {
        for (DichVu d : allServices) {
            if (d.getMaDichVu() == maDichVu) {
                return display(d);
            }
        }
        return "";
    }

    private void updateThanhTien(JTextField txtSoLuong, JTextField txtDonGia, JTextField txtThanhTien) {
        Double donGia = parseNonNegativeSilent(txtDonGia.getText());
        Integer soLuong = parsePositiveSilent(txtSoLuong.getText());
        txtThanhTien.setText(donGia == null || soLuong == null ? "0" : money(donGia * soLuong));
    }

    private void installAmountListeners(JTextField txtSoLuong, JTextField txtDonGia, JTextField txtThanhTien) {
        javax.swing.event.DocumentListener listener = new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateThanhTien(txtSoLuong, txtDonGia, txtThanhTien);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateThanhTien(txtSoLuong, txtDonGia, txtThanhTien);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateThanhTien(txtSoLuong, txtDonGia, txtThanhTien);
            }
        };
        txtSoLuong.getDocument().addDocumentListener(listener);
        txtDonGia.getDocument().addDocumentListener(listener);
    }

    private JPanel card(BorderLayout layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true), new EmptyBorder(12, 14, 12, 14)));
        return p;
    }

    private JPanel compactCard() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true), new EmptyBorder(8, 10, 8, 10)));
        return p;
    }

    private JPanel cardWrap(Component c) {
        JPanel p = card(new BorderLayout());
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JPanel field(String label, Component c) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label);
        l.setFont(LABEL_FONT);
        l.setForeground(TEXT_MUTED);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(c);
        return p;
    }

    private JComboBox<String> combo(String[] values) {
        JComboBox<String> cb = new JComboBox<String>(values);
        cb.setFont(BODY_FONT);
        cb.setPreferredSize(new Dimension(180, 34));
        return cb;
    }

    private JTextField input(String value) {
        JTextField tf = new JTextField(value);
        tf.setFont(BODY_FONT);
        tf.setPreferredSize(new Dimension(200, 34));
        return tf;
    }

    private JButton primary(String text, Color color, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color.darker(), 1, true), new EmptyBorder(9, 14, 9, 14)));
        b.addActionListener(l);
        return b;
    }

    private JButton outline(String text, Color color, java.awt.event.ActionListener l) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(TEXT_PRIMARY);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, 1, true), new EmptyBorder(8, 12, 8, 12)));
        b.addActionListener(l);
        return b;
    }

    private JLabel value(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_PRIMARY);
        l.setVerticalAlignment(SwingConstants.TOP);
        return l;
    }

    private void detailRow(JPanel panel, String label, JLabel value) {
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

    private JPanel createForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        return p;
    }

    private void addForm(JPanel panel, int row, String label, Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 0, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(BODY_FONT);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private JPanel dialogHeader(String title, String sub) {
        JPanel p = card(new BorderLayout());
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 18));
        t.setForeground(TEXT_PRIMARY);
        JLabel s = new JLabel("<html>" + sub + "</html>");
        s.setFont(BODY_FONT);
        s.setForeground(TEXT_MUTED);
        body.add(t);
        body.add(Box.createVerticalStrut(6));
        body.add(s);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buttonBar(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        for (JButton b : buttons) {
            p.add(b);
        }
        return p;
    }

    private String money(double amount) {
        return String.format(Locale.US, "%,.0f", amount).replace(',', '.');
    }

    private String item(JComboBox<String> cb) {
        return cb == null || cb.getSelectedItem() == null ? "" : cb.getSelectedItem().toString();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Double parseNonNegative(String value, String message) {
        Double parsed = parseNonNegativeSilent(value);
        if (parsed == null) {
            warn(message);
        }
        return parsed;
    }

    private Integer parsePositive(String value, String message) {
        Integer parsed = parsePositiveSilent(value);
        if (parsed == null) {
            warn(message);
        }
        return parsed;
    }

    private Double parseNonNegativeSilent(String value) {
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed < 0 ? null : parsed;
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer parsePositiveSilent(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.ERROR_MESSAGE);
    }

    private void refreshCurrentView() {
        if (rootPanel != null) {
            rootPanel.revalidate();
            rootPanel.repaint();
        }
        if (tblDichVu != null) {
            tblDichVu.revalidate();
            tblDichVu.repaint();
        }
    }

    public JPanel buildPanel() {
        if (rootPanel == null) {
            initUI();
        }
        return rootPanel;
    }
}
