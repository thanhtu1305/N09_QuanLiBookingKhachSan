package utils;

import gui.BangGiaGUI;
import gui.BaoCaoDatPhongGUI;
import gui.BaoCaoDichVuGUI;
import gui.BaoCaoDoanhThuGUI;
import gui.BaoCaoGUI;
import gui.BaoCaoKhachHangGUI;
import gui.BaoCaoPhongGUI;
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
import gui.common.AccountPermissionHelper;
import gui.common.AppFrame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.EnumMap;
import java.util.Map;

public final class NavigationUtil {

    private static final Map<ScreenKey, String> SCREEN_TITLES = new EnumMap<ScreenKey, String>(ScreenKey.class);

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
        BAO_CAO("Báo cáo thống kê"),
        BAO_CAO_DOANH_THU("Doanh thu"),
        BAO_CAO_DAT_PHONG("Đặt phòng"),
        BAO_CAO_PHONG("Phòng"),
        BAO_CAO_DICH_VU("Dịch vụ"),
        BAO_CAO_KHACH_HANG("Khách hàng");

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

    public static void navigate(Object currentFrame, ScreenKey currentScreen,
                                ScreenKey targetScreen, String username, String role) {
        if (targetScreen == null) {
            return;
        }
        if (currentScreen != null && currentScreen == targetScreen) {
            return;
        }
        if (!AccountPermissionHelper.hasPermission(username, role, targetScreen)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(
                            AppFrame.get(),
                            "Tài khoản này không được cấp quyền sử dụng màn "
                                    + SCREEN_TITLES.getOrDefault(targetScreen, targetScreen.name()) + ".",
                            "Không có quyền truy cập",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            });
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    public static void refresh(Object currentFrame, ScreenKey currentScreen,
                               String username, String role) {
        if (currentScreen == null) {
            return;
        }
        navigate(currentFrame, null, currentScreen, username, role);
    }

    private static JPanel createPanel(ScreenKey key, String username, String role) {
        String safeUsername = safe(username, "guest");
        String safeRole = safe(role, "Lễ tân");
        switch (key) {
            case DASHBOARD:
                return new DashboardGUI(safeUsername, safeRole).buildPanel();
            case TAI_KHOAN:
                return new TaiKhoanGUI(safeUsername, safeRole).buildPanel();
            case PHONG:
                return new PhongGUI(safeUsername, safeRole).buildPanel();
            case LOAI_PHONG:
                return new LoaiPhongGUI(safeUsername, safeRole).buildPanel();
            case BANG_GIA:
                return new BangGiaGUI(safeUsername, safeRole).buildPanel();
            case DAT_PHONG:
                return new DatPhongGUI(safeUsername, safeRole).buildPanel();
            case CHECK_IN_OUT:
                return new CheckInOutGUI(safeUsername, safeRole).buildPanel();
            case DICH_VU:
                return new DichVuGUI(safeUsername, safeRole).buildPanel();
            case TIEN_NGHI:
                return new TienNghiGUI(safeUsername, safeRole).buildPanel();
            case THANH_TOAN:
                return new ThanhToanGUI(safeUsername, safeRole).buildPanel();
            case KHACH_HANG:
                return new KhachHangGUI(safeUsername, safeRole).buildPanel();
            case NHAN_VIEN:
                return new NhanVienGUI(safeUsername, safeRole).buildPanel();
            case BAO_CAO:
                return new BaoCaoGUI(safeUsername, safeRole).buildPanel();
            case BAO_CAO_DOANH_THU:
                return new BaoCaoDoanhThuGUI(safeUsername, safeRole).buildPanel();
            case BAO_CAO_DAT_PHONG:
                return new BaoCaoDatPhongGUI(safeUsername, safeRole).buildPanel();
            case BAO_CAO_PHONG:
                return new BaoCaoPhongGUI(safeUsername, safeRole).buildPanel();
            case BAO_CAO_DICH_VU:
                return new BaoCaoDichVuGUI(safeUsername, safeRole).buildPanel();
            case BAO_CAO_KHACH_HANG:
                return new BaoCaoKhachHangGUI(safeUsername, safeRole).buildPanel();
            default:
                return null;
        }
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
