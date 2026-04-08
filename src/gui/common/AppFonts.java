package gui.common;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AppFonts {
    private static final String[] PREFERRED_FAMILIES = {
            "Segoe UI",
            "Arial",
            "Tahoma",
            "Noto Sans",
            "Liberation Sans",
            "Dialog"
    };

    private static final String DEFAULT_FAMILY = resolveFamily();

    private AppFonts() {
    }

    public static Font title(int size) {
        return ui(Font.BOLD, size);
    }

    public static Font section(int size) {
        return ui(Font.BOLD, size);
    }

    public static Font body(int size) {
        return ui(Font.PLAIN, size);
    }

    public static Font label(int size) {
        return ui(Font.PLAIN, size);
    }

    public static Font ui(int style, int size) {
        return new Font(DEFAULT_FAMILY, style, size);
    }

    public static String getFamily() {
        return DEFAULT_FAMILY;
    }

    private static String resolveFamily() {
        try {
            String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.getDefault());
            Set<String> available = new HashSet<String>(Arrays.asList(families));
            for (String family : PREFERRED_FAMILIES) {
                if (available.contains(family)) {
                    return family;
                }
            }
        } catch (Throwable ignored) {
        }
        return Font.SANS_SERIF;
    }
}
