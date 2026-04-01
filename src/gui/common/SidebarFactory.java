package gui.common;

import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
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
    private static final int LOGO_W = 132, LOGO_H = 72;
    private static final int MENU_ICON_SIZE = 24;

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

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(menuScrollPane, BorderLayout.CENTER);
        return sidebar;
    }

    private static void addDirectItem(JPanel menuPanel, ScreenKey item, ScreenKey currentScreen,
                                      String username, String role, String iconPath) {
        JButton button = createMenuButton(item, currentScreen, iconPath);
        button.addActionListener(e -> NavigationUtil.navigate(null, currentScreen, item, username, role));
        menuPanel.add(button);
        menuPanel.add(Box.createVerticalStrut(6));
    }

    private static void addGroup(JPanel menuPanel, String title, ScreenKey currentScreen,
                                 String username, String role, String iconPath, ScreenKey... items) {
        List<ScreenKey> groupItems = Arrays.asList(items);
        boolean expanded = groupItems.contains(currentScreen);

        JButton header = createGroupHeader(title, expanded, groupItems.contains(currentScreen), iconPath);
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
            JButton child = createSubMenuButton(item, currentScreen);
            child.addActionListener(e -> NavigationUtil.navigate(null, currentScreen, item, username, role));
            children.add(child);
            children.add(Box.createVerticalStrut(4));
        }

        header.addActionListener(e -> {
            boolean visible = !children.isVisible();
            children.setVisible(visible);
            header.putClientProperty("expanded", visible);
            children.revalidate();
            children.repaint();
            header.repaint();
        });

        menuPanel.add(header);
        menuPanel.add(Box.createVerticalStrut(4));
        menuPanel.add(children);
        menuPanel.add(Box.createVerticalStrut(10));
    }

    private static JButton createGroupHeader(String title, boolean expanded, boolean activeGroup, String iconPath) {
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
        installHover(button, activeGroup ? ACTIVE_BG : GROUP_BG, activeGroup ? ACTIVE_BG : GROUP_HOVER);
        return button;
    }

    private static JButton createMenuButton(ScreenKey item, ScreenKey currentScreen, String iconPath) {
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
        installHover(button, item == currentScreen ? ACTIVE_BG : ITEM_BG, item == currentScreen ? ACTIVE_BG : ITEM_HOVER);
        return button;
    }

    private static JButton createSubMenuButton(ScreenKey item, ScreenKey currentScreen) {
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
        installHover(button, item == currentScreen ? ACTIVE_BG : SUBMENU_BG, item == currentScreen ? ACTIVE_BG : ITEM_HOVER);
        return button;
    }

    private static void installHover(JButton button, Color normalBg, Color hoverBg) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
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
}
