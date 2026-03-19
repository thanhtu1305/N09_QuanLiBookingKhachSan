package gui.common;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
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
    private ScreenUIHelper() {
    }

    public static JPanel createWindowControlPanel(JFrame frame, Color textPrimary, Color borderSoft, String screenName) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);
        panel.add(createWindowButton("[_]", textPrimary, borderSoft, e -> frame.setState(JFrame.ICONIFIED)));
        panel.add(createWindowButton("[X]", textPrimary, borderSoft, e -> closeWindow(frame, screenName)));
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

    public static void registerShortcut(JFrame frame, String keyStroke, String actionKey, final Runnable runnable) {
        JRootPane rootPane = frame.getRootPane();
        rootPane.getInputMap(JRootPane.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyStroke), actionKey);
        rootPane.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    private static JButton createWindowButton(String text, Color textPrimary, Color borderSoft, java.awt.event.ActionListener listener) {
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

    private static void closeWindow(JFrame frame, String screenName) {
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Bạn có chắc muốn đóng " + screenName + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            frame.dispose();
        }
    }
}
