package gui.common;

import db.ConnectDB;
import utils.NavigationUtil.ScreenKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class AccountPermissionHelper {
    private static final String ROLE_MANAGER = "Qu\u1ea3n l\u00fd";
    private static final String ROLE_RECEPTIONIST = "L\u1ec5 t\u00e2n";

    private static final Map<ScreenKey, String> PERMISSION_COLUMNS = new EnumMap<ScreenKey, String>(ScreenKey.class);

    static {
        PERMISSION_COLUMNS.put(ScreenKey.DASHBOARD, "permDashboard");
        PERMISSION_COLUMNS.put(ScreenKey.DAT_PHONG, "permDatPhong");
        PERMISSION_COLUMNS.put(ScreenKey.CHECK_IN_OUT, "permCheckInOut");
        PERMISSION_COLUMNS.put(ScreenKey.THANH_TOAN, "permThanhToan");
        PERMISSION_COLUMNS.put(ScreenKey.KHACH_HANG, "permKhachHang");
        PERMISSION_COLUMNS.put(ScreenKey.PHONG, "permPhong");
        PERMISSION_COLUMNS.put(ScreenKey.LOAI_PHONG, "permLoaiPhong");
        PERMISSION_COLUMNS.put(ScreenKey.BANG_GIA, "permBangGia");
        PERMISSION_COLUMNS.put(ScreenKey.DICH_VU, "permDichVu");
        PERMISSION_COLUMNS.put(ScreenKey.TIEN_NGHI, "permTienNghi");
        PERMISSION_COLUMNS.put(ScreenKey.TAI_KHOAN, "permTaiKhoan");
        PERMISSION_COLUMNS.put(ScreenKey.NHAN_VIEN, "permNhanVien");
        PERMISSION_COLUMNS.put(ScreenKey.BAO_CAO, "permBaoCao");
        PERMISSION_COLUMNS.put(ScreenKey.BAO_CAO_DOANH_THU, "permBaoCao");
        PERMISSION_COLUMNS.put(ScreenKey.BAO_CAO_DAT_PHONG, "permBaoCao");
        PERMISSION_COLUMNS.put(ScreenKey.BAO_CAO_PHONG, "permBaoCao");
        PERMISSION_COLUMNS.put(ScreenKey.BAO_CAO_DICH_VU, "permBaoCao");
        PERMISSION_COLUMNS.put(ScreenKey.BAO_CAO_KHACH_HANG, "permBaoCao");
    }

    private AccountPermissionHelper() {
    }

    public static boolean hasPermission(String username, String role, ScreenKey screenKey) {
        if (screenKey == null) {
            return false;
        }

        String normalizedRole = normalizeRole(role);
        if (isManagerRole(normalizedRole)) {
            return true;
        }
        if (!isReceptionistRole(normalizedRole)) {
            return false;
        }

        String column = PERMISSION_COLUMNS.get(screenKey);
        if (column == null) {
            return false;
        }

        Boolean dbValue = loadPermissionFromDatabase(username, column);
        return dbValue != null ? dbValue.booleanValue() : getDefaultReceptionistPermission(screenKey);
    }

    public static boolean isManagerRole(String role) {
        return ROLE_MANAGER.equals(normalizeRole(role));
    }

    public static boolean isReceptionistRole(String role) {
        return ROLE_RECEPTIONIST.equals(normalizeRole(role));
    }

    public static String normalizeRole(String role) {
        if (role == null) {
            return ROLE_MANAGER;
        }

        String trimmed = role.trim();
        String canonical = canonicalize(trimmed);
        if ("receptionist".equals(canonical) || canonical.contains("le tan")) {
            return ROLE_RECEPTIONIST;
        }
        return ROLE_MANAGER;
    }

    private static Boolean loadPermissionFromDatabase(String username, String permissionColumn) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql =
                "SELECT q." + permissionColumn + " " +
                        "FROM TaiKhoan tk " +
                        "LEFT JOIN TaiKhoanQuyen q ON tk.maTaiKhoan = q.maTaiKhoan " +
                        "WHERE tk.tenDangNhap = ?";

        try {
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Object value = rs.getObject(1);
                    return value == null ? null : Boolean.valueOf(rs.getBoolean(1));
                }
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean getDefaultReceptionistPermission(ScreenKey screenKey) {
        switch (screenKey) {
            case DASHBOARD:
            case DAT_PHONG:
            case CHECK_IN_OUT:
            case KHACH_HANG:
                return true;
            default:
                return false;
        }
    }

    private static String canonicalize(String value) {
        String lowered = value.toLowerCase(Locale.ROOT);
        if (lowered.contains("manager")) {
            return "manager";
        }
        if (lowered.contains("receptionist")) {
            return "receptionist";
        }
        if ((lowered.contains("qu") && lowered.contains("ly")) || lowered.contains("qu\u1ea3n l\u00fd") || lowered.contains("quản")) {
            return "quan ly";
        }
        if ((lowered.contains("le") && lowered.contains("tan")) || lowered.contains("l\u1ec5 t\u00e2n") || lowered.contains("lễ")) {
            return "le tan";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }
}
