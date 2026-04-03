package gui.common;

import java.awt.Color;

public final class RoomStatusColorScheme {
    private static final Color TEXT_COLOR = new Color(31, 41, 55);
    private static final Color BORDER_COLOR = new Color(209, 213, 219);

    private static final Color ACTIVE_BG = new Color(220, 252, 231);
    private static final Color BOOKED_BG = new Color(254, 249, 195);
    private static final Color OCCUPIED_BG = new Color(254, 226, 226);
    private static final Color INACTIVE_BG = new Color(229, 231, 235);
    private static final Color MAINTENANCE_BG = new Color(253, 230, 138);

    private RoomStatusColorScheme() {}

    public static Color getBackgroundColor(String statusOrCode) {
        String key = normalize(statusOrCode);
        if ("ACTIVE".equals(key)) {
            return ACTIVE_BG;
        }
        if ("BOOKED".equals(key)) {
            return BOOKED_BG;
        }
        if ("OCCUPIED".equals(key)) {
            return OCCUPIED_BG;
        }
        if ("INACTIVE".equals(key)) {
            return INACTIVE_BG;
        }
        return MAINTENANCE_BG;
    }

    public static Color getBorderColor(String statusOrCode) {
        return BORDER_COLOR;
    }

    public static Color getTextColor(String statusOrCode) {
        return TEXT_COLOR;
    }

    private static String normalize(String statusOrCode) {
        if (statusOrCode == null) {
            return "MAINTENANCE";
        }

        String value = statusOrCode.trim();
        if (value.isEmpty()) {
            return "MAINTENANCE";
        }

        if ("A".equalsIgnoreCase(value) || "T".equalsIgnoreCase(value)
                || "Ho\u1ea1t \u0111\u1ed9ng".equalsIgnoreCase(value)
                || "Tr\u1ed1ng".equalsIgnoreCase(value)) {
            return "ACTIVE";
        }
        if ("D".equalsIgnoreCase(value)
                || "\u0110\u00e3 \u0111\u1eb7t".equalsIgnoreCase(value)) {
            return "BOOKED";
        }
        if ("O".equalsIgnoreCase(value)
                || "\u0110ang \u1edf".equalsIgnoreCase(value)) {
            return "OCCUPIED";
        }
        if ("I".equalsIgnoreCase(value)
                || "Kh\u00f4ng ho\u1ea1t \u0111\u1ed9ng".equalsIgnoreCase(value)) {
            return "INACTIVE";
        }
        if ("B".equalsIgnoreCase(value)
                || "C".equalsIgnoreCase(value)
                || "M".equalsIgnoreCase(value)
                || "D\u1ecdn d\u1eb9p".equalsIgnoreCase(value)
                || "B\u1ea3o tr\u00ec".equalsIgnoreCase(value)) {
            return "MAINTENANCE";
        }
        return "MAINTENANCE";
    }
}
