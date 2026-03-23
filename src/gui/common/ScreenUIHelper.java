package gui.common;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class ScreenUIHelper {
    private static final String DIALOG_PREPARED_KEY = "dialogPrepared";

    private ScreenUIHelper() {}

    /**
     * Nút [_] thu nhỏ và [X] đóng — hoạt động trên AppFrame singleton.
     */
    public static JPanel createWindowControlPanel(Object ignored, Color textPrimary, Color borderSoft, String screenName) {
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
            shortcut.setFont(new Font("Segoe UI", Font.BOLD, 12));
            shortcut.setForeground(textMuted);
            panel.add(shortcut);
        }
        return panel;
    }

    /**
     * Đăng ký shortcut vào AppFrame (JFrame duy nhất).
     * Tham số frame bỏ qua — shortcut luôn bind vào AppFrame.get().
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

        dialog.getContentPane().revalidate();
        dialog.getContentPane().repaint();
        dialog.pack();

        Dimension packedSize = dialog.getSize();
        int targetWidth = Math.max(packedSize.width, safeMinWidth);
        int targetHeight = Math.max(packedSize.height, safeMinHeight);

        dialog.setMinimumSize(new Dimension(safeMinWidth, safeMinHeight));
        if (packedSize.width != targetWidth || packedSize.height != targetHeight) {
            dialog.setSize(targetWidth, targetHeight);
        }
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().revalidate();
        dialog.getContentPane().repaint();
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

    private static void refreshOwner(Window parent) {
        if (parent == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (parent instanceof Container) {
                ((Container) parent).revalidate();
            }
            parent.repaint();
        });
    }

    private static JButton createWindowButton(String text, Color textPrimary, Color borderSoft,
                                              java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
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
                "Bạn có chắc muốn đóng " + screenName + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AppFrame.get().dispose();
            System.exit(0);
        }
    }
}
