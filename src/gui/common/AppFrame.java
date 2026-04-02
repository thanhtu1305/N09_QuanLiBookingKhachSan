package gui.common;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * JFrame duy nhất của toàn ứng dụng.
 * Tất cả màn hình được swap vào đây qua swapTo() — không tạo/hủy cửa sổ → không nháy.
 */
public class AppFrame extends JFrame {

    private static AppFrame instance;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    private AppFrame() {
        setTitle("Hotel PMS");
        setMinimumSize(new Dimension(1024, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(container);
        ScreenUIHelper.prepareFrame(this, 1440, 900);
    }

    public static AppFrame get() {
        if (instance == null) {
            instance = new AppFrame();
        }
        return instance;
    }

    /**
     * Swap nội dung màn hình — không tạo/hủy cửa sổ, không nháy.
     */
    public void swapTo(String screenName, JPanel panel, String title) {
        // Xóa card cũ cùng tên (nếu có) để luôn dùng panel mới nhất
        container.remove(getComponentByName(screenName));
        panel.setName(screenName);
        container.add(panel, screenName);
        cardLayout.show(container, screenName);
        setTitle(title);
        revalidate();
        repaint();
    }

    private java.awt.Component getComponentByName(String name) {
        for (java.awt.Component c : container.getComponents()) {
            if (name.equals(c.getName())) return c;
        }
        return new JPanel(); // trả về dummy nếu không tìm thấy
    }
}
