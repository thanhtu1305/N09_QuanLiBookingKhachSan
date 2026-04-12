package gui.common;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RootPaneContainer;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.util.Enumeration;

public final class ScreenUIHelper {
    private static final String DIALOG_PREPARED_KEY = "dialogPrepared";
    private static final Color TABLE_HEADER_BG = new Color(44, 94, 143);
    private static final Color TABLE_HEADER_FG = Color.WHITE;
    private static final Color TABLE_HEADER_BORDER = new Color(122, 162, 202);
    private static final Font TITLE_FONT = AppFonts.title(18);
    private static final Font BODY_FONT = AppFonts.body(13);
    private static final Font LABEL_FONT = AppFonts.label(12);
    private static final Font TABLE_HEADER_FONT = AppFonts.ui(Font.BOLD, 13);
    private static boolean uiDefaultsInstalled;

    private ScreenUIHelper() {}

    /**
     * Nút [_] thu nhỏ và [X] đóng, hoạt động trên AppFrame singleton.
     */
    public static JPanel createWindowControlPanel(Object ignored, Color textPrimary, Color borderSoft, String screenName) {
        installGlobalUiDefaults();
        AppFrame frame = AppFrame.get();
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);
        panel.add(createWindowButton("[_]", textPrimary, borderSoft,
                e -> frame.setState(AppFrame.ICONIFIED)));
        panel.add(createWindowButton("[X]", textPrimary, borderSoft,
                e -> closeWindow(screenName)));
        return panel;
    }

    public static JPanel createShortcutBar(Color cardBg, Color borderSoft, Color textMuted, String... labels) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        panel.setBackground(cardBg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderSoft, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        for (String label : labels) {
            JLabel shortcut = new JLabel(label);
            shortcut.setFont(AppFonts.ui(Font.BOLD, 12));
            shortcut.setForeground(textMuted);
            panel.add(shortcut);
        }
        return panel;
    }

    /**
     * Đăng ký shortcut vào AppFrame (JFrame duy nhất).
     * Tham số frame bỏ qua, shortcut luôn bind vào AppFrame.get().
     */
    public static void registerShortcut(Object ignored, String keyStroke, String actionKey, Runnable runnable) {
        JRootPane rootPane = AppFrame.get().getRootPane();
        rootPane.getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        rootPane.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    public static void prepareDialog(JDialog dialog, Window parent, int minWidth, int minHeight) {
        if (dialog == null) {
            return;
        }

        installGlobalUiDefaults();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        if (!Boolean.TRUE.equals(dialog.getRootPane().getClientProperty(DIALOG_PREPARED_KEY))) {
            dialog.getRootPane().putClientProperty(DIALOG_PREPARED_KEY, Boolean.TRUE);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshOwner(parent);
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    refreshOwner(parent);
                }
            });
        }

        int safeMinWidth = Math.max(minWidth, 320);
        int safeMinHeight = Math.max(minHeight, 180);

        ensureScrollableDialogContent(dialog);
        styleTablesRecursively(dialog.getContentPane());
        dialog.getContentPane().revalidate();
        dialog.getContentPane().repaint();
        dialog.pack();

        Rectangle usableBounds = getUsableBounds(dialog, parent);
        Dimension packedSize = dialog.getSize();
        int maxWidth = Math.max(320, (int) Math.floor(usableBounds.width * 0.90d));
        int maxHeight = Math.max(220, (int) Math.floor(usableBounds.height * 0.85d));
        int targetWidth = Math.min(maxWidth, Math.max(packedSize.width, safeMinWidth));
        int targetHeight = Math.min(maxHeight, Math.max(packedSize.height, safeMinHeight));

        dialog.setMinimumSize(new Dimension(Math.min(safeMinWidth, maxWidth), Math.min(safeMinHeight, maxHeight)));
        dialog.setSize(targetWidth, targetHeight);
        positionWindow(dialog, usableBounds, parent);
        dialog.getContentPane().revalidate();
        dialog.getContentPane().repaint();
    }

    public static void prepareFrame(Window window, int preferredWidth, int preferredHeight) {
        if (window == null) {
            return;
        }
        installGlobalUiDefaults();
        Rectangle usableBounds = getUsableBounds(window, null);
        int width = Math.min(Math.max(preferredWidth, 800), usableBounds.width);
        int height = Math.min(Math.max(preferredHeight, 600), usableBounds.height);
        window.setSize(width, height);
        if (window instanceof RootPaneContainer) {
            styleTablesRecursively(((RootPaneContainer) window).getContentPane());
        }
        positionWindow(window, usableBounds, null);
    }

    public static void styleTableHeader(JTable table) {
        if (table == null) {
            return;
        }

        JTableHeader header = table.getTableHeader();
        if (header == null) {
            return;
        }

        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setOpaque(true);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_FG);
        header.setFont(TABLE_HEADER_FONT);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        final TableCellRenderer fallbackRenderer = header.getDefaultRenderer();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component;
                if (fallbackRenderer != null) {
                    component = fallbackRenderer.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                } else {
                    component = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                }

                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(TABLE_HEADER_FONT);
                    label.setForeground(TABLE_HEADER_FG);
                    label.setBackground(TABLE_HEADER_BG);
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 1, TABLE_HEADER_BORDER),
                            new EmptyBorder(8, 8, 8, 8)
                    ));
                } else {
                    component.setForeground(TABLE_HEADER_FG);
                    component.setBackground(TABLE_HEADER_BG);
                }
                return component;
            }
        });
        header.repaint();
    }

    public static void styleTablesRecursively(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof JTable) {
            styleTableHeader((JTable) component);
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                styleTablesRecursively(child);
            }
        }
    }

    public static Frame resolveDialogOwner(Component candidate) {
        Window ancestor = candidate == null ? null : SwingUtilities.getWindowAncestor(candidate);
        if (ancestor instanceof Frame && ancestor.isDisplayable()) {
            return (Frame) ancestor;
        }

        if (candidate instanceof Frame) {
            Frame frame = (Frame) candidate;
            if (frame.isDisplayable() || frame.isShowing()) {
                return frame;
            }
        }

        AppFrame appFrame = AppFrame.get();
        if (appFrame.isDisplayable() || appFrame.isShowing()) {
            return appFrame;
        }

        return candidate instanceof Frame ? (Frame) candidate : null;
    }

    public static Window resolveWindowOwner(Window candidate) {
        if (candidate instanceof Frame) {
            return resolveDialogOwner((Component) candidate);
        }
        if (candidate != null && candidate.isDisplayable()) {
            return candidate;
        }
        AppFrame appFrame = AppFrame.get();
        if (appFrame.isDisplayable() || appFrame.isShowing()) {
            return appFrame;
        }
        return candidate;
    }

    public static void registerTableDoubleClick(JTable table, Runnable action) {
        if (table == null || action == null) {
            return;
        }

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || e.getClickCount() != 2) {
                    return;
                }

                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0) {
                    return;
                }

                table.setRowSelectionInterval(viewRow, viewRow);
                action.run();
            }
        });
    }

    private static void refreshOwner(Window parent) {
        SwingUtilities.invokeLater(() -> {
            Window target = parent;
            if (target == null || !target.isDisplayable() || !target.isShowing()) {
                AppFrame appFrame = AppFrame.get();
                if (appFrame.isDisplayable() || appFrame.isShowing()) {
                    target = appFrame;
                }
            }
            if (target == null) {
                return;
            }
            if (target instanceof Container) {
                ((Container) target).revalidate();
            }
            if (target instanceof RootPaneContainer) {
                ((RootPaneContainer) target).getRootPane().revalidate();
                ((RootPaneContainer) target).getRootPane().repaint();
            }
            if (target instanceof Frame) {
                ((Frame) target).toFront();
                ((Frame) target).requestFocus();
            }
            target.repaint();
        });
    }

    public static void installLiveSearch(JTextField textField, Runnable filterAction) {
        if (textField == null || filterAction == null) {
            return;
        }

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterAction.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterAction.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterAction.run();
            }
        });
    }

    private static void ensureScrollableDialogContent(JDialog dialog) {
        if (dialog == null || !(dialog.getContentPane() instanceof Container)) {
            return;
        }
        ensureScrollableCenter((Container) dialog.getContentPane());
    }

    private static void ensureScrollableCenter(Container container) {
        if (container == null || !(container.getLayout() instanceof BorderLayout)) {
            return;
        }

        BorderLayout layout = (BorderLayout) container.getLayout();
        Component south = layout.getLayoutComponent(container, BorderLayout.SOUTH);
        Component center = layout.getLayoutComponent(container, BorderLayout.CENTER);
        if (center == null) {
            return;
        }

        if (south != null) {
            wrapCenterComponent(container, center);
            return;
        }

        if (center instanceof Container) {
            ensureScrollableCenter((Container) center);
        }
    }

    private static void wrapCenterComponent(Container parent, Component center) {
        if (center instanceof JScrollPane) {
            return;
        }

        BorderLayout layout = (BorderLayout) parent.getLayout();
        parent.remove(center);

        JScrollPane scrollPane = new JScrollPane(center);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        parent.add(scrollPane, BorderLayout.CENTER);
        parent.revalidate();
        parent.repaint();
    }

    private static Rectangle getUsableBounds(Window window, Window owner) {
        GraphicsConfiguration config = null;
        if (window != null && window.getGraphicsConfiguration() != null) {
            config = window.getGraphicsConfiguration();
        } else if (owner != null && owner.getGraphicsConfiguration() != null) {
            config = owner.getGraphicsConfiguration();
        }
        if (config == null) {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            config = environment.getDefaultScreenDevice().getDefaultConfiguration();
        }

        Rectangle bounds = new Rectangle(config.getBounds());
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);
        return bounds;
    }

    private static void positionWindow(Window window, Rectangle usableBounds, Window owner) {
        int x;
        int y;
        if (owner != null && owner.isShowing()) {
            Rectangle ownerBounds = owner.getBounds();
            x = ownerBounds.x + (ownerBounds.width - window.getWidth()) / 2;
            y = ownerBounds.y + (ownerBounds.height - window.getHeight()) / 2;
        } else {
            x = usableBounds.x + (usableBounds.width - window.getWidth()) / 2;
            y = usableBounds.y + (usableBounds.height - window.getHeight()) / 2;
        }

        int maxX = usableBounds.x + usableBounds.width - window.getWidth();
        int maxY = usableBounds.y + usableBounds.height - window.getHeight();
        x = Math.max(usableBounds.x, Math.min(x, maxX));
        y = Math.max(usableBounds.y, Math.min(y, maxY));
        window.setLocation(x, y);
    }

    private static JButton createWindowButton(String text, Color textPrimary, Color borderSoft,
                                              java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFont(AppFonts.ui(Font.BOLD, 12));
        button.setForeground(textPrimary);
        button.setBackground(new Color(243, 244, 246));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderSoft, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        button.addActionListener(listener);
        return button;
    }

    private static void closeWindow(String screenName) {
        int confirm = JOptionPane.showConfirmDialog(
                AppFrame.get(),
                "B\u1ea1n c\u00f3 ch\u1eafc mu\u1ed1n \u0111\u00f3ng " + screenName + "?",
                "X\u00e1c nh\u1eadn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AppFrame.get().dispose();
            System.exit(0);
        }
    }

    public static synchronized void installGlobalUiDefaults() {
        if (uiDefaultsInstalled) {
            return;
        }

        UIManager.put("OptionPane.messageFont", BODY_FONT);
        UIManager.put("OptionPane.buttonFont", BODY_FONT);
        UIManager.put("OptionPane.yesButtonText", "C\u00f3");
        UIManager.put("OptionPane.noButtonText", "Kh\u00f4ng");
        UIManager.put("OptionPane.cancelButtonText", "H\u1ee7y");
        UIManager.put("OptionPane.okButtonText", "\u0110\u1ed3ng \u00fd");
        UIManager.put("ToolTip.font", LABEL_FONT);
        UIManager.put("TitledBorder.font", TABLE_HEADER_FONT);
        UIManager.put("TitledBorder.titleColor", new Color(31, 41, 55));

        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            if (!(key instanceof String)) {
                continue;
            }
            String uiKey = (String) key;
            if (uiKey.endsWith(".font")) {
                Font font = UIManager.getFont(uiKey);
                if (font == null) {
                    continue;
                }
                if (uiKey.contains("Title") || uiKey.contains("Header")) {
                    UIManager.put(uiKey, AppFonts.ui(Font.BOLD, Math.max(font.getSize(), TITLE_FONT.getSize())));
                } else if (uiKey.contains("TableHeader")) {
                    UIManager.put(uiKey, TABLE_HEADER_FONT);
                } else {
                    UIManager.put(uiKey, AppFonts.ui(Font.PLAIN, Math.max(font.getSize(), BODY_FONT.getSize())));
                }
            }
        }
        uiDefaultsInstalled = true;
    }
}
