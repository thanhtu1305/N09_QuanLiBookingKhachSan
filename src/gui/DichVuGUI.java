
package gui;

import dao.DichVuDAO;
import dao.SuDungDichVuDAO;
import db.ConnectDB;
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
import javax.swing.event.DocumentListener;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
        loadServices(true, false);
        registerShortcuts();
        ScreenUIHelper.prepareFrame(this, 1360, 820);
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
        main.add(ScreenUIHelper.createShortcutBar(CARD_BG, BORDER_SOFT, TEXT_MUTED, "F1 Thêm", "F3 Xóa", "F4 Sử dụng"), BorderLayout.SOUTH);
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

        JLabel sub = new JLabel("Quản lý danh mục dịch vụ và ghi nhận dịch vụ phát sinh theo lưu trú.");
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
        card.add(primary("Xóa dịch vụ", new Color(220, 38, 38), e -> deleteSelectedService()));
        card.add(primary("Sử dụng dịch vụ", new Color(99, 102, 241), e -> openUsageDialog(getSelectedService(false))));
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
        ScreenUIHelper.installLiveSearch(txtTuKhoa, () -> applyFilters(false));

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
        ScreenUIHelper.styleTableHeader(tblDichVu);
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
        JLabel t2 = new JLabel("Double click vào một dòng để cập nhật dịch vụ.");
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
        if (resetFilter && txtTuKhoa != null) {
            txtTuKhoa.setText("");
        }
        applyFilters(false);
        refreshCurrentView();
        if (showMessage) {
            info("Đã làm mới dữ liệu dịch vụ.");
        }
    }

    private void refreshDonVi(boolean resetSelection) {
        Set<String> units = new LinkedHashSet<String>();
        units.add(FILTER_ALL);
        for (DichVu dichVu : allServices) {
            if (!safe(dichVu.getDonVi()).isEmpty()) {
                units.add(dichVu.getDonVi().trim());
            }
        }

        String selected = resetSelection ? FILTER_ALL : item(cboDonVi);
        cboDonVi.removeAllItems();
        for (String unit : units) {
            cboDonVi.addItem(unit);
        }
        cboDonVi.setSelectedItem(units.contains(selected) ? selected : FILTER_ALL);
    }

    private void applyFilters(boolean notify) {
        filteredServices.clear();
        String keyword = txtTuKhoa == null ? "" : safe(txtTuKhoa.getText()).toLowerCase(Locale.ROOT);
        String donVi = item(cboDonVi);

        for (DichVu dichVu : allServices) {
            if (!FILTER_ALL.equals(donVi) && !safe(dichVu.getDonVi()).equalsIgnoreCase(donVi)) {
                continue;
            }
            if (!keyword.isEmpty() && !buildServiceSearchText(dichVu).contains(keyword)) {
                continue;
            }
            filteredServices.add(dichVu);
        }

        tableModel.setRowCount(0);
        for (DichVu dichVu : filteredServices) {
            tableModel.addRow(new Object[]{formatServiceCode(dichVu.getMaDichVu()), dichVu.getTenDichVu(), money(dichVu.getDonGia()), dichVu.getDonVi()});
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

    private String buildServiceSearchText(DichVu dichVu) {
        return (
                formatServiceCode(dichVu.getMaDichVu()) + " " +
                        safe(dichVu.getTenDichVu()) + " " +
                        money(dichVu.getDonGia()) + " " +
                        safe(dichVu.getDonVi())
        ).toLowerCase(Locale.ROOT);
    }

    private void updateDetail(DichVu dichVu) {
        lblMaDichVu.setText(formatServiceCode(dichVu.getMaDichVu()));
        lblTenDichVu.setText(safe(dichVu.getTenDichVu()).isEmpty() ? "-" : dichVu.getTenDichVu());
        lblDonGia.setText(money(dichVu.getDonGia()));
        lblDonVi.setText(safe(dichVu.getDonVi()).isEmpty() ? "-" : dichVu.getDonVi());

        List<SuDungDichVu> usages = suDungDichVuDAO.getByMaDichVu(dichVu.getMaDichVu());
        if (usages.isEmpty()) {
            txtLichSu.setText("Chưa có dữ liệu sử dụng dịch vụ.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (SuDungDichVu usage : usages) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("SD").append(usage.getMaSuDung())
                    .append(" | Lưu trú ").append(usage.getMaLuuTru())
                    .append(" | SL ").append(usage.getSoLuong())
                    .append(" | Thành tiền ").append(money(usage.getThanhTien()));
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
        ScreenUIHelper.registerShortcut(this, "F3", "dichvu-f3", this::deleteSelectedService);
        ScreenUIHelper.registerShortcut(this, "F4", "dichvu-f4", () -> openUsageDialog(getSelectedService(false)));
    }

    private class ServiceEditor extends JDialog {
        private ServiceEditor(Frame owner, DichVu editing) {
            super(ScreenUIHelper.resolveDialogOwner(owner), editing == null ? "Thêm dịch vụ" : "Cập nhật dịch vụ", true);
            getContentPane().setBackground(APP_BG);
            setLayout(new BorderLayout(0, 12));
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JTextField txtTen = input(editing == null ? "" : editing.getTenDichVu());
            JTextField txtDonGia = input(editing == null ? "0" : String.valueOf((long) editing.getDonGia()));
            JTextField txtDonVi = input(editing == null ? "" : editing.getDonVi());

            JPanel form = createForm();
            addForm(form, 0, "Tên dịch vụ", txtTen);
            addForm(form, 1, "Đơn giá", txtDonGia);
            addForm(form, 2, "Đơn vị", txtDonVi);

            add(dialogHeader(editing == null ? "Thêm dịch vụ mới" : "Cập nhật dịch vụ", "Nhập đúng dữ liệu của bảng DichVu."), BorderLayout.NORTH);
            add(cardWrap(form), BorderLayout.CENTER);
            add(buttonBar(
                    outline("Hủy", new Color(107, 114, 128), e -> dispose()),
                    primary(editing == null ? "Lưu" : "Lưu cập nhật", new Color(22, 163, 74), e -> {
                        if (safe(txtTen.getText()).isEmpty()) {
                            warn("Tên dịch vụ không được rỗng.");
                            return;
                        }
                        Double donGia = parseNonNegative(txtDonGia.getText(), "Đơn giá phải là số hợp lệ >= 0.");
                        if (donGia == null) {
                            return;
                        }
                        if (safe(txtDonVi.getText()).isEmpty()) {
                            warn("Đơn vị không được rỗng.");
                            return;
                        }

                        DichVu dichVu = editing == null
                                ? new DichVu(safe(txtTen.getText()), donGia, safe(txtDonVi.getText()))
                                : new DichVu(editing.getMaDichVu(), safe(txtTen.getText()), donGia, safe(txtDonVi.getText()));

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
        private final UsageContext usageContext;
        private boolean syncingSelection;

        private ServiceUsageEditor(Frame owner, DichVu preselected) {
            super(ScreenUIHelper.resolveDialogOwner(owner), "Sử dụng dịch vụ", true);
            getContentPane().setBackground(APP_BG);
            setLayout(new BorderLayout(0, 12));
            ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JTextField txtCccdPassport = input("");
            JComboBox<ActiveStayOption> cboKhachHang = new JComboBox<ActiveStayOption>();
            cboKhachHang.setFont(BODY_FONT);
            cboKhachHang.setPreferredSize(new Dimension(260, 34));

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
            tblUsage.setFont(BODY_FONT);
            tblUsage.setRowHeight(30);
            tblUsage.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblUsage.setGridColor(BORDER_SOFT);
            ScreenUIHelper.styleTableHeader(tblUsage);

            usageContext = new UsageContext(
                    txtCccdPassport,
                    cboKhachHang,
                    cboDichVu,
                    txtSoLuong,
                    txtDonGia,
                    txtThanhTien,
                    usageModel,
                    loadActiveStayOptions()
            );

            Runnable fillPrice = () -> {
                DichVu dichVu = serviceFromDisplay(item(cboDichVu));
                if (dichVu != null) {
                    txtDonGia.setText(String.valueOf((long) dichVu.getDonGia()));
                }
                updateThanhTien(txtSoLuong, txtDonGia, txtThanhTien);
            };

            Runnable refreshActiveStayAndUsage = () -> {
                if (syncingSelection) {
                    return;
                }
                ActiveStayOption current = getSelectedActiveStayOption(cboKhachHang);
                filterActiveStayOptions(usageContext, txtCccdPassport.getText(), current == null ? -1 : current.maLuuTru);
                reloadUsageRows(usageContext);
            };

            cboDichVu.addActionListener(e -> fillPrice.run());
            cboKhachHang.addActionListener(e -> {
                if (!syncingSelection) {
                    reloadUsageRows(usageContext);
                }
            });
            installAmountListeners(txtSoLuong, txtDonGia, txtThanhTien);
            installTextChangeListener(txtCccdPassport, refreshActiveStayAndUsage);
            tblUsage.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    populateUsageSelection(tblUsage, usageContext);
                }
            });

            JPanel form = createForm();
            addForm(form, 0, "CCCD/Passport", txtCccdPassport);
            addForm(form, 1, "Tên khách hàng", cboKhachHang);
            addForm(form, 2, "Dịch vụ", cboDichVu);
            addForm(form, 3, "Số lượng", txtSoLuong);
            addForm(form, 4, "Đơn giá", txtDonGia);
            addForm(form, 5, "Thành tiền", txtThanhTien);

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
            filterActiveStayOptions(usageContext, "", -1);
            reloadUsageRows(usageContext);
            ScreenUIHelper.prepareDialog(this, owner, 820, 620);
        }

        private void saveUsage(boolean editing) {
            UsageContext c = usageContext;
            SuDungDichVu suDung = usageFromForm(selected[0], editing, c.cboKhachHang, c.cboDichVu, c.txtSoLuong, c.txtDonGia);
            if (suDung == null) {
                return;
            }

            boolean ok = editing ? suDungDichVuDAO.updateSuDungDichVu(suDung) : suDungDichVuDAO.insertSuDungDichVu(suDung);
            if (!ok) {
                error(editing ? "Không thể cập nhật dịch vụ sử dụng." : "Không thể thêm dịch vụ sử dụng.");
                return;
            }

            info(editing ? "Cập nhật dịch vụ sử dụng thành công." : "Thêm dịch vụ sử dụng thành công.");
            usageContext.allActiveStayOptions.clear();
            usageContext.allActiveStayOptions.addAll(loadActiveStayOptions());
            filterActiveStayOptions(usageContext, usageContext.txtCccdPassport.getText(), suDung.getMaLuuTru());
            reloadUsageRows(usageContext);
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
            usageContext.allActiveStayOptions.clear();
            usageContext.allActiveStayOptions.addAll(loadActiveStayOptions());
            filterActiveStayOptions(usageContext, usageContext.txtCccdPassport.getText(), getSelectedMaLuuTru(usageContext.cboKhachHang));
            reloadUsageRows(usageContext);
            loadServices(false, false);

            DichVu cur = getSelectedService(false);
            if (cur != null) {
                updateDetail(cur);
            }
            info("Xóa dịch vụ sử dụng thành công.");
        }

        private void reloadUsageRows(UsageContext context) {
            context.usageModel.setRowCount(0);
            usageRows.clear();
            selected[0] = null;

            int maLuuTru = getSelectedMaLuuTru(context.cboKhachHang);
            if (maLuuTru <= 0) {
                return;
            }

            usageRows.addAll(suDungDichVuDAO.getByMaLuuTru(maLuuTru));
            for (SuDungDichVu suDung : usageRows) {
                context.usageModel.addRow(new Object[]{
                        "SD" + suDung.getMaSuDung(),
                        suDung.getTenDichVu(),
                        suDung.getSoLuong(),
                        money(suDung.getDonGia()),
                        money(suDung.getThanhTien())
                });
            }
        }
        private void populateUsageSelection(JTable tblUsage, UsageContext context) {
            int row = tblUsage.getSelectedRow();
            if (row < 0 || row >= usageRows.size()) {
                return;
            }

            SuDungDichVu suDung = usageRows.get(row);
            selected[0] = suDung;

            ActiveStayOption option = findActiveStayOptionByMaLuuTru(context.allActiveStayOptions, suDung.getMaLuuTru());
            if (option != null) {
                syncingSelection = true;
                try {
                    context.txtCccdPassport.setText(safe(option.cccdPassport));
                    filterActiveStayOptions(context, context.txtCccdPassport.getText(), option.maLuuTru);
                } finally {
                    syncingSelection = false;
                }
                reloadUsageRows(context);
                selected[0] = suDung;
            }

            context.cboDichVu.setSelectedItem(serviceDisplayById(suDung.getMaDichVu()));
            context.txtSoLuong.setText(String.valueOf(suDung.getSoLuong()));
            context.txtDonGia.setText(String.valueOf((long) suDung.getDonGia()));
            context.txtThanhTien.setText(money(suDung.getThanhTien()));
        }
    }

    private static final class UsageContext {
        private final JTextField txtCccdPassport;
        private final JComboBox<ActiveStayOption> cboKhachHang;
        private final JComboBox<String> cboDichVu;
        private final JTextField txtSoLuong;
        private final JTextField txtDonGia;
        private final JTextField txtThanhTien;
        private final DefaultTableModel usageModel;
        private final List<ActiveStayOption> allActiveStayOptions;

        private UsageContext(
                JTextField txtCccdPassport,
                JComboBox<ActiveStayOption> cboKhachHang,
                JComboBox<String> cboDichVu,
                JTextField txtSoLuong,
                JTextField txtDonGia,
                JTextField txtThanhTien,
                DefaultTableModel usageModel,
                List<ActiveStayOption> allActiveStayOptions
        ) {
            this.txtCccdPassport = txtCccdPassport;
            this.cboKhachHang = cboKhachHang;
            this.cboDichVu = cboDichVu;
            this.txtSoLuong = txtSoLuong;
            this.txtDonGia = txtDonGia;
            this.txtThanhTien = txtThanhTien;
            this.usageModel = usageModel;
            this.allActiveStayOptions = allActiveStayOptions;
        }
    }

    private static final class ActiveStayOption {
        private final int maLuuTru;
        private final String tenKhachHang;
        private final String cccdPassport;
        private final String soPhong;

        private ActiveStayOption(int maLuuTru, String tenKhachHang, String cccdPassport, String soPhong) {
            this.maLuuTru = maLuuTru;
            this.tenKhachHang = tenKhachHang;
            this.cccdPassport = cccdPassport;
            this.soPhong = soPhong;
        }

        @Override
        public String toString() {
            String ten = tenKhachHang == null || tenKhachHang.trim().isEmpty() ? "Khách lưu trú " + maLuuTru : tenKhachHang.trim();
            String phong = soPhong == null || soPhong.trim().isEmpty() ? "" : " - Phòng " + soPhong.trim();
            return ten + phong;
        }
    }

    private List<ActiveStayOption> loadActiveStayOptions() {
        List<ActiveStayOption> options = new ArrayList<ActiveStayOption>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return options;
        }

        String sql =
                "SELECT lt.maLuuTru, kh.hoTen, kh.cccdPassport, p.soPhong " +
                        "FROM LuuTru lt " +
                        "JOIN Phong p ON lt.maPhong = p.maPhong " +
                        "LEFT JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                        "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang " +
                        "WHERE p.trangThai = N'Đang ở' " +
                        "  AND lt.maLuuTru = ( " +
                        "      SELECT MAX(lt2.maLuuTru) " +
                        "      FROM LuuTru lt2 " +
                        "      WHERE lt2.maPhong = lt.maPhong " +
                        "  ) " +
                        "ORDER BY kh.hoTen, p.soPhong";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                options.add(new ActiveStayOption(
                        rs.getInt("maLuuTru"),
                        safe(rs.getString("hoTen")).isEmpty() ? "Khách lưu trú " + rs.getInt("maLuuTru") : rs.getString("hoTen").trim(),
                        safe(rs.getString("cccdPassport")),
                        safe(rs.getString("soPhong"))
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return options;
    }

    private ActiveStayOption findActiveStayOptionByMaLuuTru(List<ActiveStayOption> options, int maLuuTru) {
        for (ActiveStayOption option : options) {
            if (option.maLuuTru == maLuuTru) {
                return option;
            }
        }
        return null;
    }

    private int getSelectedMaLuuTru(JComboBox<ActiveStayOption> cboKhachHang) {
        ActiveStayOption option = getSelectedActiveStayOption(cboKhachHang);
        return option == null ? -1 : option.maLuuTru;
    }

    private ActiveStayOption getSelectedActiveStayOption(JComboBox<ActiveStayOption> cboKhachHang) {
        Object selected = cboKhachHang == null ? null : cboKhachHang.getSelectedItem();
        return selected instanceof ActiveStayOption ? (ActiveStayOption) selected : null;
    }

    private void filterActiveStayOptions(UsageContext context, String filterText, int preferredMaLuuTru) {
        List<ActiveStayOption> filtered = filterActiveStayOptions(context.allActiveStayOptions, filterText);
        context.cboKhachHang.removeAllItems();

        ActiveStayOption preferred = null;
        for (ActiveStayOption option : filtered) {
            context.cboKhachHang.addItem(option);
            if (preferredMaLuuTru > 0 && option.maLuuTru == preferredMaLuuTru) {
                preferred = option;
            }
        }

        if (preferred != null) {
            context.cboKhachHang.setSelectedItem(preferred);
        } else if (context.cboKhachHang.getItemCount() > 0) {
            context.cboKhachHang.setSelectedIndex(0);
        }
    }

    private List<ActiveStayOption> filterActiveStayOptions(List<ActiveStayOption> options, String filterText) {
        List<ActiveStayOption> filtered = new ArrayList<ActiveStayOption>();
        String keyword = safe(filterText).toLowerCase(Locale.ROOT);

        for (ActiveStayOption option : options) {
            if (keyword.isEmpty() || matchesActiveStayOption(option, keyword)) {
                filtered.add(option);
            }
        }
        return filtered;
    }

    private boolean matchesActiveStayOption(ActiveStayOption option, String keyword) {
        String source = (safe(option.cccdPassport) + " " + safe(option.tenKhachHang) + " " + safe(option.soPhong)).toLowerCase(Locale.ROOT);
        return source.contains(keyword);
    }
    private SuDungDichVu usageFromForm(
            SuDungDichVu selectedUsage,
            boolean editing,
            JComboBox<ActiveStayOption> cboKhachHang,
            JComboBox<String> cboDichVu,
            JTextField txtSoLuong,
            JTextField txtDonGia
    ) {
        int maLuuTru = getSelectedMaLuuTru(cboKhachHang);
        if (maLuuTru <= 0) {
            warn("Khách hàng lưu trú không hợp lệ.");
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

        SuDungDichVu suDung = new SuDungDichVu(maLuuTru, dichVu.getMaDichVu(), soLuong, donGia);
        if (editing) {
            if (selectedUsage == null) {
                warn("Vui lòng chọn một dòng dịch vụ đã dùng.");
                return null;
            }
            suDung.setMaSuDung(selectedUsage.getMaSuDung());
        }
        return suDung;
    }

    private String[] dichVuOptions() {
        String[] options = new String[allServices.size()];
        for (int i = 0; i < allServices.size(); i++) {
            options[i] = display(allServices.get(i));
        }
        return options;
    }

    private String display(DichVu dichVu) {
        return formatServiceCode(dichVu.getMaDichVu()) + " - " + dichVu.getTenDichVu();
    }

    private DichVu serviceFromDisplay(String display) {
        for (DichVu dichVu : allServices) {
            if (display(dichVu).equals(display)) {
                return dichVu;
            }
        }
        return null;
    }

    private String serviceDisplayById(int maDichVu) {
        for (DichVu dichVu : allServices) {
            if (dichVu.getMaDichVu() == maDichVu) {
                return display(dichVu);
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
        DocumentListener listener = new DocumentListener() {
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

    private void installTextChangeListener(JTextField textField, Runnable action) {
        DocumentListener listener = new DocumentListener() {
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
        };
        textField.getDocument().addDocumentListener(listener);
    }

    private JPanel card(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        return panel;
    }

    private JPanel compactCard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return panel;
    }

    private JPanel cardWrap(Component component) {
        JPanel panel = card(new BorderLayout());
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel field(String label, Component component) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(component);
        return panel;
    }

    private JComboBox<String> combo(String[] values) {
        JComboBox<String> comboBox = new JComboBox<String>(values);
        comboBox.setFont(BODY_FONT);
        comboBox.setPreferredSize(new Dimension(180, 34));
        return comboBox;
    }

    private JTextField input(String value) {
        JTextField textField = new JTextField(value);
        textField.setFont(BODY_FONT);
        textField.setPreferredSize(new Dimension(200, 34));
        return textField;
    }
    private JButton primary(String text, Color color, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JButton outline(String text, Color color, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        button.addActionListener(listener);
        return button;
    }

    private JLabel value(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
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
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
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
        JPanel panel = card(new BorderLayout());
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("<html>" + sub + "</html>");
        subLabel.setFont(BODY_FONT);
        subLabel.setForeground(TEXT_MUTED);

        body.add(titleLabel);
        body.add(Box.createVerticalStrut(6));
        body.add(subLabel);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buttonBar(JButton... buttons) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        for (JButton button : buttons) {
            panel.add(button);
        }
        return panel;
    }

    private String formatServiceCode(int id) {
        return id <= 0 ? "" : "DV" + id;
    }

    private String money(double amount) {
        return String.format(Locale.US, "%,.0f", amount).replace(',', '.');
    }

    private String item(JComboBox<?> comboBox) {
        return comboBox == null || comboBox.getSelectedItem() == null ? "" : comboBox.getSelectedItem().toString();
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
