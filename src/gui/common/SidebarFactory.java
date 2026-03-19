package gui.common;

import utils.NavigationUtil;
import utils.NavigationUtil.ScreenKey;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public final class SidebarFactory {
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_SOFT = new Color(229, 231, 235);
    private static final Color BRAND_PRIMARY = new Color(30, 64, 175);
    private static final int SIDEBAR_LOGO_MAX_WIDTH = 132;
    private static final int SIDEBAR_LOGO_MAX_HEIGHT = 72;

    private SidebarFactory() {
    }

    public static JPanel createSidebar(JFrame owner, ScreenKey currentScreen, String username, String role) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(229, 231, 235));
        sidebar.setPreferredSize(new Dimension(228, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
                new EmptyBorder(12, 10, 12, 10)
        ));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(6, 8, 18, 8));

        JLabel lblTitle = AppBranding.createLogoLabel(
                SIDEBAR_LOGO_MAX_WIDTH,
                SIDEBAR_LOGO_MAX_HEIGHT,
                SwingConstants.CENTER,
                AppBranding.APP_DISPLAY_NAME,
                new Font("Segoe UI", Font.BOLD, 18),
                BRAND_PRIMARY
        );

        JLabel lblSub = new JLabel(
                "<html><div style='text-align:center;'>"
                        + AppBranding.APP_DESCRIPTION
                        + "</div></html>",
                SwingConstants.CENTER
        );
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);

        JPanel brandContent = new JPanel(new GridLayout(2, 1, 0, 8));
        brandContent.setOpaque(false);
        brandContent.add(lblTitle);
        brandContent.add(lblSub);

        brand.add(brandContent, BorderLayout.CENTER);

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        for (ScreenKey item : ScreenKey.values()) {
            JButton button = createMenuButton(item, currentScreen);
            button.addActionListener(e -> NavigationUtil.navigate(owner, currentScreen, item, username, role));
            menuPanel.add(button);
            menuPanel.add(Box.createVerticalStrut(6));
        }

        sidebar.add(brand, BorderLayout.NORTH);
        sidebar.add(menuPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private static JButton createMenuButton(ScreenKey item, ScreenKey currentScreen) {
        JButton button = new JButton(item.getLabel());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 12, 10, 12));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);

        if (item == currentScreen) {
            button.setBackground(new Color(219, 234, 254));
            button.setForeground(new Color(29, 78, 216));
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        } else {
            button.setBackground(new Color(229, 231, 235));
            button.setForeground(TEXT_PRIMARY);
        }
        return button;
    }
}
