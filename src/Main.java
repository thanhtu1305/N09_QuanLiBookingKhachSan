    import gui.DangNhapGUI;
    import javax.swing.SwingUtilities;

    public class Main {
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> new DangNhapGUI().setVisible(true));
        }
    }