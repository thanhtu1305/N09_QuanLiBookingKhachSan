package gui.common;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

public final class ScreenUIHelper {

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