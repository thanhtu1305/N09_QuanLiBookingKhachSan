package gui.common;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;

public final class AppBranding {
    public static final String APP_DISPLAY_NAME = "HOTEL VTTTT";
    public static final String APP_DESCRIPTION = "Hệ thống quản lý đặt phòng khách sạn";
    public static final String LOGO_RESOURCE_PATH = "/images/logos/logo2.png";

    private AppBranding() {
    }

    public static String formatPageTitle(String pageName) {
        return APP_DISPLAY_NAME + " - " + pageName;
    }

    public static JLabel createLogoLabel(int maxWidth, int maxHeight, int horizontalAlignment, String fallbackText, Font fallbackFont, Color fallbackColor) {
        ImageIcon logoIcon = loadScaledLogo(maxWidth, maxHeight);
        if (logoIcon != null) {
            JLabel label = new JLabel(logoIcon, horizontalAlignment);
            label.setHorizontalAlignment(horizontalAlignment);
            label.setVerticalAlignment(SwingConstants.CENTER);
            return label;
        }

        JLabel fallbackLabel = new JLabel(fallbackText, horizontalAlignment);
        fallbackLabel.setFont(fallbackFont);
        fallbackLabel.setForeground(fallbackColor);
        fallbackLabel.setHorizontalAlignment(horizontalAlignment);
        return fallbackLabel;
    }

    private static ImageIcon loadScaledLogo(int maxWidth, int maxHeight) {
        URL resource = AppBranding.class.getResource(LOGO_RESOURCE_PATH);
        if (resource == null) {
            return null;
        }

        ImageIcon originalIcon = new ImageIcon(resource);
        int originalWidth = originalIcon.getIconWidth();
        int originalHeight = originalIcon.getIconHeight();
        if (originalWidth <= 0 || originalHeight <= 0) {
            return null;
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scaleRatio = Math.min(1.0, Math.min(widthRatio, heightRatio));

        int scaledWidth = Math.max(1, (int) Math.round(originalWidth * scaleRatio));
        int scaledHeight = Math.max(1, (int) Math.round(originalHeight * scaleRatio));
        Image scaledImage = originalIcon.getImage().getScaledInstance(
                scaledWidth,
                scaledHeight,
                Image.SCALE_SMOOTH
        );
        return new ImageIcon(scaledImage);
    }
}
