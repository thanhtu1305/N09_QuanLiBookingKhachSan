package gui.common;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public final class SidebarFactory {

    private static final Color TEXT_PRIMARY  = new Color(31, 41, 55);
    private static final Color TEXT_MUTED    = new Color(107, 114, 128);
    private static final Color BORDER_SOFT   = new Color(229, 231, 235);
    private static final Color BRAND_PRIMARY = new Color(30, 64, 175);
    private static final Color SIDEBAR_BG    = new Color(244, 247, 251);
    private static final Color GROUP_BG      = new Color(235, 240, 246);
    private static final Color GROUP_HOVER   = new Color(228, 234, 242);
    private static final Color ITEM_BG       = new Color(255, 255, 255);
    private static final Color ITEM_HOVER    = new Color(241, 245, 249);
    private static final Color SUBMENU_BG    = new Color(248, 250, 252);
    private static final Color ACTIVE_BG     = new Color(219, 234, 254);
    private static final Color ACTIVE_TEXT   = new Color(29, 78, 216);
    private static final Color PROFILE_BG    = new Color(248, 250, 252);
    private static final Color PROFILE_HOVER = new Color(239, 246, 255);
    private static final Color PROFILE_CARD  = new Color(255, 255, 255);
    private static final int LOGO_W = 132, LOGO_H = 72;
    private static final int MENU_ICON_SIZE = 24;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private SidebarFactory() {}

    /**
     * Tham số owner giữ nguyên kiểu Object để không cần sửa code gọi ở các GUI.
     */
    public static JPanel createSidebar(Object owner, ScreenKey currentScreen,
                                       String username, String role) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(228, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 10, 12, 10)
        ));

        // Brand / logo
        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(6, 8, 18, 8));

        JLabel lblTitle = AppBranding.createLogoLabel(LOGO_W, LOGO_H,
                SwingConstants.CENTER, AppBranding.APP_DISPLAY_NAME,
                new Font("Segoe UI", Font.BOLD, 18), BRAND_PRIMARY);

        JLabel lblSub = new JLabel(
                "<html><div style='text-align:center;'>" + AppBranding.APP_DESCRIPTION + "</div></html>",
                SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);

        JPanel brandContent = new JPanel(new GridLayout(2, 1, 0, 8));
        brandContent.setOpaque(false);
        brandContent.add(lblTitle);
        brandContent.add(lblSub);
        brand.add(brandContent, BorderLayout.CENTER);

        // Menu buttons
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(4, 2, 4, 2));

        addDirectItem(menuPanel, ScreenKey.DASHBOARD, currentScreen, username, role,
                "/images/tong_quan.png");
        addGroup(menuPanel, "Quản lý đặt phòng", currentScreen, username, role,
                "/images/quan_ly_dat_phong.png",
                ScreenKey.DAT_PHONG, ScreenKey.CHECK_IN_OUT, ScreenKey.THANH_TOAN);
        addGroup(menuPanel, "Quản lý phòng", currentScreen, username, role,
                "/images/quan_ly_phong.png",
                ScreenKey.PHONG, ScreenKey.LOAI_PHONG, ScreenKey.BANG_GIA, ScreenKey.TIEN_NGHI, ScreenKey.DICH_VU);
        addGroup(menuPanel, "Quản lý khách hàng", currentScreen, username, role,
                "/images/quan_ly_khach_hang.png",
                ScreenKey.KHACH_HANG);
        addGroup(menuPanel, "Quản lý nhân sự", currentScreen, username, role,
                "/images/quan_ly_nhan_su.png",
                ScreenKey.NHAN_VIEN, ScreenKey.TAI_KHOAN);
        addGroup(menuPanel, "Báo cáo thống kê", currentScreen, username, role,
                "/images/bao_cao_thong_ke.png",
                ScreenKey.BAO_CAO);

        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setBorder(null);
        menuScrollPane.setOpaque(false);
        menuScrollPane.getViewport().setOpaque(false);
        menuScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottomPanel = buildBottomPanel(username, role);

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(menuScrollPane, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);
        return sidebar;
    }

    private static JPanel buildBottomPanel(String username, String role) {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(18, 2, 0, 2));

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_SOFT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel lblUtility = new JLabel("Tiện ích người dùng");
        lblUtility.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUtility.setForeground(TEXT_MUTED);
        lblUtility.setBorder(new EmptyBorder(10, 4, 8, 4));
        lblUtility.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnProfile = createProfileButton(username, role);

        bottomPanel.add(separator);
        bottomPanel.add(lblUtility);
        bottomPanel.add(btnProfile);
        return bottomPanel;
    }

    private static void addDirectItem(JPanel menuPanel, ScreenKey item, ScreenKey currentScreen,
                                      String username, String role, String iconPath) {
        boolean allowed = AccountPermissionHelper.hasPermission(username, role, item);
        JButton button = createMenuButton(item, currentScreen, iconPath, allowed);
        if (allowed) {
            button.addActionListener(e -> NavigationUtil.navigate(null, currentScreen, item, username, role));
        }
        menuPanel.add(button);
        menuPanel.add(Box.createVerticalStrut(6));
    }

    private static void addGroup(JPanel menuPanel, String title, ScreenKey currentScreen,
                                 String username, String role, String iconPath, ScreenKey... items) {
        List<ScreenKey> groupItems = Arrays.asList(items);
        boolean expanded = groupItems.contains(currentScreen);
        boolean anyAllowed = false;
        for (ScreenKey item : groupItems) {
            if (AccountPermissionHelper.hasPermission(username, role, item)) {
                anyAllowed = true;
                break;
            }
        }

        JButton header = createGroupHeader(title, expanded, groupItems.contains(currentScreen), iconPath, anyAllowed);
        JPanel children = new JPanel();
        children.setOpaque(true);
        children.setBackground(SUBMENU_BG);
        children.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(236, 240, 245), 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));
        children.setLayout(new BoxLayout(children, BoxLayout.Y_AXIS));
        children.setVisible(expanded);

        for (ScreenKey item : groupItems) {
            boolean allowed = AccountPermissionHelper.hasPermission(username, role, item);
            JButton child = createSubMenuButton(item, currentScreen, allowed);
            if (allowed) {
                child.addActionListener(e -> NavigationUtil.navigate(null, currentScreen, item, username, role));
            }
            children.add(child);
            children.add(Box.createVerticalStrut(4));
        }

        if (anyAllowed) {
            header.addActionListener(e -> {
                boolean visible = !children.isVisible();
                children.setVisible(visible);
                header.putClientProperty("expanded", visible);
                children.revalidate();
                children.repaint();
                header.repaint();
            });
        }

        menuPanel.add(header);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(children);
        menuPanel.add(Box.createVerticalStrut(10));
    }

    private static JButton createGroupHeader(String title, boolean expanded, boolean activeGroup, String iconPath, boolean enabled) {
        JButton button = new JButton(title);
        button.putClientProperty("expanded", expanded);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIcon(loadMenuIcon(iconPath, MENU_ICON_SIZE, MENU_ICON_SIZE));
        button.setIconTextGap(14);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(activeGroup ? ACTIVE_BG : GROUP_BG, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (activeGroup) {
            button.setBackground(ACTIVE_BG);
            button.setForeground(ACTIVE_TEXT);
        } else {
            button.setBackground(GROUP_BG);
            button.setForeground(TEXT_PRIMARY);
        }
        applyMenuEnabledState(button, enabled, activeGroup ? ACTIVE_BG : GROUP_BG);
        installHover(button, activeGroup ? ACTIVE_BG : GROUP_BG, activeGroup ? ACTIVE_BG : GROUP_HOVER);
        return button;
    }

    private static JButton createMenuButton(ScreenKey item, ScreenKey currentScreen, String iconPath, boolean enabled) {
        JButton button = new JButton(item.getLabel());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIcon(loadMenuIcon(iconPath, MENU_ICON_SIZE, MENU_ICON_SIZE));
        button.setIconTextGap(14);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(item == currentScreen ? ACTIVE_BG : ITEM_BG, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (item == currentScreen) {
            button.setBackground(ACTIVE_BG);
            button.setForeground(ACTIVE_TEXT);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        } else {
            button.setBackground(ITEM_BG);
            button.setForeground(TEXT_PRIMARY);
        }
        applyMenuEnabledState(button, enabled, item == currentScreen ? ACTIVE_BG : ITEM_BG);
        installHover(button, item == currentScreen ? ACTIVE_BG : ITEM_BG, item == currentScreen ? ACTIVE_BG : ITEM_HOVER);
        return button;
    }

    private static JButton createSubMenuButton(ScreenKey item, ScreenKey currentScreen, boolean enabled) {
        JButton button = new JButton(item.getLabel());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(item == currentScreen ? ACTIVE_BG : SUBMENU_BG, 1, true),
                new EmptyBorder(8, 24, 8, 12)
        ));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (item == currentScreen) {
            button.setBackground(ACTIVE_BG);
            button.setForeground(ACTIVE_TEXT);
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        } else {
            button.setBackground(SUBMENU_BG);
            button.setForeground(TEXT_PRIMARY);
        }
        applyMenuEnabledState(button, enabled, item == currentScreen ? ACTIVE_BG : SUBMENU_BG);
        installHover(button, item == currentScreen ? ACTIVE_BG : SUBMENU_BG, item == currentScreen ? ACTIVE_BG : ITEM_HOVER);
        return button;
    }

    private static JButton createProfileButton(String username, String role) {
        String displayName = username == null || username.trim().isEmpty() ? "Người dùng" : username.trim();
        JButton button = new JButton("<html><div style='line-height:1.4'><b>Thông tin cá nhân</b><br/><span style='font-size:10px;'>"
                + displayName + " • " + safe(role, "Nhân viên") + "</span></div></html>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBackground(PROFILE_BG);
        button.setForeground(TEXT_PRIMARY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        installHover(button, PROFILE_BG, PROFILE_HOVER);
        button.addActionListener(e -> showProfileDialog(username, role));
        return button;
    }

    private static void applyMenuEnabledState(JButton button, boolean enabled, Color normalBackground) {
        button.setEnabled(enabled);
        if (!enabled) {
            button.setCursor(Cursor.getDefaultCursor());
            button.setForeground(TEXT_MUTED);
            button.setBackground(new Color(
                    Math.min(255, normalBackground.getRed() + 6),
                    Math.min(255, normalBackground.getGreen() + 6),
                    Math.min(255, normalBackground.getBlue() + 6)
            ));
        }
    }

    private static void installHover(JButton button, Color normalBg, Color hoverBg) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!button.isEnabled()) {
                    return;
                }
                button.setBackground(normalBg);
            }
        });
    }

    private static ImageIcon loadMenuIcon(String path, int w, int h) {
        if (path == null || path.trim().isEmpty()) return null;

        URL resource = SidebarFactory.class.getResource(path);
        if (resource == null) return null;

        ImageIcon raw = new ImageIcon(resource);
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) return null;

        Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static void showProfileDialog(String username, String role) {
        TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
        TaiKhoan taiKhoan = taiKhoanDAO.findByUsername(username);
        NhanVien nhanVien = null;
        if (taiKhoan != null && taiKhoan.getMaNhanVien() > 0) {
            nhanVien = new NhanVienDAO().findById(taiKhoan.getMaNhanVien());
        }

        Frame owner = AppFrame.get();
        JDialog dialog = new JDialog(owner, "Thông tin cá nhân", true);
        dialog.getContentPane().setBackground(new Color(240, 244, 248));
        dialog.setLayout(new BorderLayout(0, 16));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        root.add(buildProfileHeader(username, role, taiKhoan, nhanVien), BorderLayout.NORTH);
        root.add(buildProfileContent(username, role, taiKhoan, nhanVien), BorderLayout.CENTER);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFocusPainted(false);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(BRAND_PRIMARY);
        btnClose.setBorder(new EmptyBorder(10, 18, 10, 18));
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);
        actionPanel.add(btnClose, BorderLayout.EAST);
        root.add(actionPanel, BorderLayout.SOUTH);

        dialog.add(root, BorderLayout.CENTER);
        dialog.setSize(640, 520);
        dialog.setMinimumSize(new Dimension(600, 480));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static JPanel buildProfileHeader(String username, String role, TaiKhoan taiKhoan, NhanVien nhanVien) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(BRAND_PRIMARY);
        header.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel avatar = new JLabel(buildInitials(nhanVien, username), SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(72, 72));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(219, 234, 254));
        avatar.setForeground(BRAND_PRIMARY);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 24));
        avatar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Thông tin cá nhân");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblName = new JLabel(safe(nhanVien == null ? null : nhanVien.getHoTen(), safe(username, "Người dùng")));
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(new Color(219, 234, 254));

        JLabel lblRole = new JLabel(safe(role, safe(taiKhoan == null ? null : taiKhoan.getVaiTro(), "Nhân viên")));
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(new Color(219, 234, 254));

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(lblName);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblRole);

        header.add(avatar, BorderLayout.WEST);
        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private static JPanel buildProfileContent(String username, String role, TaiKhoan taiKhoan, NhanVien nhanVien) {
        JPanel container = new JPanel(new BorderLayout(0, 14));
        container.setOpaque(false);

        JPanel infoCard = createProfileCard();
        infoCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        addProfileRow(infoCard, gbc, 0, "Họ tên", safe(nhanVien == null ? null : nhanVien.getHoTen(), "Chưa cập nhật"));
        addProfileRow(infoCard, gbc, 1, "Tên đăng nhập", safe(username, "Chưa cập nhật"));
        addProfileRow(infoCard, gbc, 2, "Vai trò", safe(role, safe(taiKhoan == null ? null : taiKhoan.getVaiTro(), "Chưa cập nhật")));
        addProfileRow(infoCard, gbc, 3, "Bộ phận", safe(nhanVien == null ? null : nhanVien.getBoPhan(), "Chưa cập nhật"));
        addProfileRow(infoCard, gbc, 4, "Email", resolveEmail(taiKhoan, nhanVien));
        addProfileRow(infoCard, gbc, 5, "Số điện thoại", safe(nhanVien == null ? null : nhanVien.getSoDienThoai(), "Chưa cập nhật"));
        addProfileRow(infoCard, gbc, 6, "Trạng thái tài khoản", safe(taiKhoan == null ? null : taiKhoan.getTrangThai(), "Chưa cập nhật"));
        addProfileRow(infoCard, gbc, 7, "Ngày vào làm", formatDate(nhanVien == null ? null : nhanVien.getNgayVaoLam()));

        JPanel summaryCard = createProfileCard();
        summaryCard.setLayout(new GridLayout(1, 2, 12, 0));
        summaryCard.add(createMetricCard("Mã nhân viên", nhanVien == null || nhanVien.getMaNhanVien() <= 0 ? "Chưa có" : "NV" + nhanVien.getMaNhanVien()));
        summaryCard.add(createMetricCard("Đăng nhập gần nhất", formatTimestamp(taiKhoan == null ? null : taiKhoan.getLanDangNhapCuoi())));

        container.add(summaryCard, BorderLayout.NORTH);
        container.add(infoCard, BorderLayout.CENTER);
        return container;
    }

    private static JPanel createProfileCard() {
        JPanel panel = new JPanel();
        panel.setBackground(PROFILE_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return panel;
    }

    private static JPanel createMetricCard(String label, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLabel.setForeground(TEXT_MUTED);

        JLabel lblValue = new JLabel(safe(value, "Chưa cập nhật"));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValue.setForeground(TEXT_PRIMARY);

        panel.add(lblLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblValue);
        return panel;
    }

    private static void addProfileRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLabel.setForeground(TEXT_MUTED);
        panel.add(lblLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblValue = new JLabel(safe(value, "Chưa cập nhật"));
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValue.setForeground(TEXT_PRIMARY);
        panel.add(lblValue, gbc);
    }

    private static String buildInitials(NhanVien nhanVien, String username) {
        String source = nhanVien != null && nhanVien.getHoTen() != null && !nhanVien.getHoTen().trim().isEmpty()
                ? nhanVien.getHoTen().trim()
                : safe(username, "ND");
        String[] parts = source.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase();
    }

    private static String resolveEmail(TaiKhoan taiKhoan, NhanVien nhanVien) {
        if (nhanVien != null && nhanVien.getEmail() != null && !nhanVien.getEmail().trim().isEmpty()) {
            return nhanVien.getEmail().trim();
        }
        return safe(taiKhoan == null ? null : taiKhoan.getEmailKhoiPhuc(), "Chưa cập nhật");
    }

    private static String formatDate(Date date) {
        return date == null ? "Chưa cập nhật" : DATE_FORMAT.format(date);
    }

    private static String formatTimestamp(Timestamp timestamp) {
        return timestamp == null ? "Chưa cập nhật" : DATETIME_FORMAT.format(timestamp);
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
