package utils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MessageDialog {
    private MessageDialog() {
    }

    public static void showInfo(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(JFrame parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }
}
