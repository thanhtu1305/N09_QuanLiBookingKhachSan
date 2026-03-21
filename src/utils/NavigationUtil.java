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
import gui.common.AppFrame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.EnumMap;
import java.util.Map;

public final class NavigationUtil {

    private static final Map<ScreenKey, String> SCREEN_TITLES = new EnumMap<>(ScreenKey.class);

    static {
        for (ScreenKey key : ScreenKey.values()) {
            SCREEN_TITLES.put(key, key.getLabel());
        }
    }

    private NavigationUtil() {}

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
        ScreenKey(String label) { this.label = label; }
        public String getLabel() { return label; }

        public static ScreenKey fromLabel(String label) {
            for (ScreenKey k : values()) if (k.label.equals(label)) return k;
            return null;
        }
    }

    /**
     * Chuyển màn hình: swap panel vào AppFrame duy nhất — không tạo/hủy cửa sổ, không nháy.
     * Tham số currentFrame giữ nguyên để không cần sửa code gọi ở các GUI.
     */
    public static void navigate(Object currentFrame, ScreenKey currentScreen,
                                ScreenKey targetScreen, String username, String role) {
        if (targetScreen == null) return;
        if (currentScreen != null && currentScreen == targetScreen) return;

        SwingUtilities.invokeLater(() -> {
            JPanel panel = createPanel(targetScreen, username, role);
            if (panel == null) {
                JOptionPane.showMessageDialog(
                        AppFrame.get(),
                        "Không mở được màn hình: " + SCREEN_TITLES.getOrDefault(targetScreen, targetScreen.name()),
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            String title = "Hotel PMS - " + SCREEN_TITLES.getOrDefault(targetScreen, targetScreen.name());
            AppFrame.get().swapTo(targetScreen.name(), panel, title);
        });
    }

    public static void refresh(Object currentFrame, ScreenKey currentScreen,
                               String username, String role) {
        if (currentScreen == null) return;
        navigate(currentFrame, null, currentScreen, username, role);
    }

    private static JPanel createPanel(ScreenKey key, String username, String role) {
        String u = safe(username, "guest");
        String r = safe(role, "Lễ tân");
        switch (key) {
            case DASHBOARD:    return new DashboardGUI(u, r).buildPanel();
            case TAI_KHOAN:    return new TaiKhoanGUI(u, r).buildPanel();
            case PHONG:        return new PhongGUI(u, r).buildPanel();
            case LOAI_PHONG:   return new LoaiPhongGUI(u, r).buildPanel();
            case BANG_GIA:     return new BangGiaGUI(u, r).buildPanel();
            case DAT_PHONG:    return new DatPhongGUI(u, r).buildPanel();
            case CHECK_IN_OUT: return new CheckInOutGUI(u, r).buildPanel();
            case DICH_VU:      return new DichVuGUI(u, r).buildPanel();
            case TIEN_NGHI:    return new TienNghiGUI(u, r).buildPanel();
            case THANH_TOAN:   return new ThanhToanGUI(u, r).buildPanel();
            case KHACH_HANG:   return new KhachHangGUI(u, r).buildPanel();
            case NHAN_VIEN:    return new NhanVienGUI(u, r).buildPanel();
            case BAO_CAO:      return new BaoCaoGUI(u, r).buildPanel();
            default:           return null;
        }
    }

    private static String safe(String v, String fallback) {
        return v == null || v.trim().isEmpty() ? fallback : v.trim();
    }
}