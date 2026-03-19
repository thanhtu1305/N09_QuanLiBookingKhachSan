package utils;

import gui.BangGiaGUI;
import gui.BaoCaoGUI;
import gui.CheckInOutGUI;
import gui.DashboardGUI;
import gui.DatPhongGUI;
import gui.DichVuGUI;
import gui.KhachHangGUI;
import gui.LoaiPhongGUI;
import gui.NhanVienGUI;
import gui.PhongGUI;
import gui.TaiKhoanGUI;
import gui.ThanhToanGUI;
import gui.TienNghiGUI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.EnumMap;
import java.util.Map;

public final class NavigationUtil {
    private static final Map<ScreenKey, String> SCREEN_TITLES = new EnumMap<>(ScreenKey.class);

    static {
        for (ScreenKey key : ScreenKey.values()) {
            SCREEN_TITLES.put(key, key.getLabel());
        }
    }

    private NavigationUtil() {
    }

    public enum ScreenKey {
        DASHBOARD("Tổng quan"),
        TAI_KHOAN("Tài khoản"),
        PHONG("Phòng"),
        LOAI_PHONG("Loại phòng"),
        BANG_GIA("Bảng giá"),
        DAT_PHONG("Đặt phòng"),
        CHECK_IN_OUT("Check-in/out"),
        DICH_VU("Dịch vụ"),
        TIEN_NGHI("Tiện nghi"),
        THANH_TOAN("Thanh toán"),
        KHACH_HANG("Khách hàng"),
        NHAN_VIEN("Nhân viên"),
        BAO_CAO("Báo cáo thống kê");

        private final String label;

        ScreenKey(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static ScreenKey fromLabel(String label) {
            for (ScreenKey key : values()) {
                if (key.label.equals(label)) {
                    return key;
                }
            }
            return null;
        }
    }

    public static void navigate(JFrame currentFrame, ScreenKey currentScreen, ScreenKey targetScreen, String username, String role) {
        if (targetScreen == null) {
            return;
        }
        if (currentScreen != null && currentScreen == targetScreen) {
            return;
        }

        JFrame nextFrame = createFrame(targetScreen, username, role);
        if (nextFrame == null) {
            JOptionPane.showMessageDialog(
                    currentFrame,
                    "Không mở được màn hình: " + SCREEN_TITLES.getOrDefault(targetScreen, targetScreen.name()),
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        nextFrame.setVisible(true);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    public static void refresh(JFrame currentFrame, ScreenKey currentScreen, String username, String role) {
        if (currentScreen == null) {
            return;
        }

        JFrame nextFrame = createFrame(currentScreen, username, role);
        if (nextFrame == null) {
            JOptionPane.showMessageDialog(
                    currentFrame,
                    "Không làm mới được màn hình: " + SCREEN_TITLES.getOrDefault(currentScreen, currentScreen.name()),
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        nextFrame.setVisible(true);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }

    private static JFrame createFrame(ScreenKey screenKey, String username, String role) {
        String safeUsername = safeUsername(username);
        String safeRole = safeRole(role);

        switch (screenKey) {
            case DASHBOARD:
                return new DashboardGUI(safeUsername, safeRole);
            case TAI_KHOAN:
                return new TaiKhoanGUI(safeUsername, safeRole);
            case PHONG:
                return new PhongGUI(safeUsername, safeRole);
            case LOAI_PHONG:
                return new LoaiPhongGUI(safeUsername, safeRole);
            case BANG_GIA:
                return new BangGiaGUI(safeUsername, safeRole);
            case DAT_PHONG:
                return new DatPhongGUI(safeUsername, safeRole);
            case CHECK_IN_OUT:
                return new CheckInOutGUI(safeUsername, safeRole);
            case DICH_VU:
                return new DichVuGUI(safeUsername, safeRole);
            case TIEN_NGHI:
                return new TienNghiGUI(safeUsername, safeRole);
            case THANH_TOAN:
                return new ThanhToanGUI(safeUsername, safeRole);
            case KHACH_HANG:
                return new KhachHangGUI(safeUsername, safeRole);
            case NHAN_VIEN:
                return new NhanVienGUI(safeUsername, safeRole);
            case BAO_CAO:
                return new BaoCaoGUI(safeUsername, safeRole);
            default:
                return null;
        }
    }

    private static String safeUsername(String username) {
        return username == null || username.trim().isEmpty() ? "guest" : username.trim();
    }

    private static String safeRole(String role) {
        return role == null || role.trim().isEmpty() ? "Lễ tân" : role.trim();
    }
}
