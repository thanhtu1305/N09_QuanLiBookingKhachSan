package dao;

import db.ConnectDB;
import entity.ChiTietDatPhong;
import entity.DatPhongConflictInfo;
import entity.DatPhong;
import entity.ChiTietBangGia;
import entity.NgayLe;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class DatPhongDAO {
    public static final String STATUS_PENDING_CHECKIN = "Chờ check-in";
    public static final String STATUS_ACTIVE = "Đang ở";
    public static final String STATUS_PARTIAL_CHECKOUT = "Check-out một phần";
    public static final String STATUS_WAIT_PAYMENT = "Chờ thanh toán";
    public static final String STATUS_PAID = "Đã thanh toán";
    public static final String STATUS_CHECKED_OUT = "Đã check-out";
    public static final String STATUS_CANCELLED = "Đã hủy";
    public static final String STATUS_CANCELLED_BOOKING = "Hủy booking";
    private static final String LOAI_NGAY_THUONG = "Ngày thường";
    private static final String LOAI_NGAY_CUOI_TUAN = "Cuối tuần";
    private static final String LOAI_NGAY_LE = "Ngày lễ";
    private static final String LOAI_GIA_THEO_NGAY = "Theo ngày";
    private static final String LOAI_GIA_QUA_DEM = "Qua đêm";
    private static final String LOAI_GIA_THEO_GIO = "Theo giờ";
    private static final String LOAI_GIA_LE = "Giá lễ";
    private static final String LOAI_GIA_CUOI_TUAN = "Giá cuối tuần";

    private static final String DAY_TYPE_NORMAL = "THUONG";
    private static final String DAY_TYPE_WEEKEND = "CUOI_TUAN";
    private static final String DAY_TYPE_HOLIDAY = "NGAY_LE";
    private static final String STAY_TYPE_HOURLY = "THEO_GIO";
    private static final String STAY_TYPE_DAILY = "THEO_NGAY";
    private static final String STAY_TYPE_OVERNIGHT = "QUA_DEM";
    private static final String DISPLAY_LOAI_NGAY_THUONG = "Ng\u00e0y th\u01b0\u1eddng";
    private static final String DISPLAY_LOAI_NGAY_CUOI_TUAN = "Cu\u1ed1i tu\u1ea7n";
    private static final String DISPLAY_LOAI_NGAY_LE = "Ng\u00e0y l\u1ec5";
    private static final String DISPLAY_LOAI_GIA_THEO_NGAY = "Theo ng\u00e0y";
    private static final String DISPLAY_LOAI_GIA_QUA_DEM = "Qua \u0111\u00eam";
    private static final String DISPLAY_LOAI_GIA_THEO_GIO = "Theo gi\u1edd";

    public static String normalizeStageStatus(String status) {
        String value = status == null ? "" : status.trim();
        if ("\u0110\u00e3 check-in".equalsIgnoreCase(value) || "\u0110ang l\u01b0u tr\u00fa".equalsIgnoreCase(value)) {
            return "\u0110ang \u1edf";
        }
        return value;
    }

    public static boolean isBookingStageStatus(String status) {
        String value = normalizeStageStatus(status);
        return "\u0110\u00e3 \u0111\u1eb7t".equalsIgnoreCase(value)
                || "\u0110\u00e3 x\u00e1c nh\u1eadn".equalsIgnoreCase(value)
                || "\u0110\u00e3 c\u1ecdc".equalsIgnoreCase(value)
                || "Ch\u1edd check-in".equalsIgnoreCase(value);
    }

    public static boolean isOperationalStageStatus(String status) {
        String value = normalizeStageStatus(status);
        return "Ch\u1edd check-in".equalsIgnoreCase(value)
                || "\u0110ang \u1edf".equalsIgnoreCase(value)
                || "Check-out m\u1ed9t ph\u1ea7n".equalsIgnoreCase(value);
    }

    public static boolean isPaymentStageStatus(String status) {
        String value = normalizeStageStatus(status);
        return "\u0110\u00e3 check-out".equalsIgnoreCase(value)
                || "Ch\u1edd thanh to\u00e1n".equalsIgnoreCase(value)
                || "\u0110\u00e3 thanh to\u00e1n".equalsIgnoreCase(value);
    }

    private static final String SELECT_HEADER_BASE =
            "SELECT dp.maDatPhong, dp.maKhachHang, dp.maNhanVien, dp.maBangGia, dp.ngayDat, dp.ngayNhanPhong, dp.ngayTraPhong, "
                    + "dp.soLuongPhong, dp.soNguoi, dp.tienCoc, dp.trangThai, "
                    + "kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhach, kh.cccdPassport AS cccdPassportKhach "
                    + "FROM DatPhong dp "
                    + "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang";

    private String lastErrorMessage = "";
    private final BangGiaDAO bangGiaDAO = new BangGiaDAO();
    private final NgayLeDAO ngayLeDAO = new NgayLeDAO();

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<DatPhong> getAll() {
        clearLastError();
        List<DatPhong> result = new ArrayList<DatPhong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String sql = SELECT_HEADER_BASE + " ORDER BY dp.maDatPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                DatPhong datPhong = mapDatPhongHeader(rs);
                datPhong.getChiTietDatPhongs().addAll(getChiTietByMaDatPhongInternal(con, datPhong));
                normalizeBooking(con, datPhong);
                result.add(datPhong);
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public DatPhong findById(String maDatPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return null;
        }

        try {
            return findByIdInternal(con, id.intValue());
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<DatPhong> findByTrangThai(String trangThai) {
        clearLastError();
        List<DatPhong> result = new ArrayList<DatPhong>();
        String normalizedStatus = safeTrim(trangThai);
        for (DatPhong datPhong : getAll()) {
            if (normalizedStatus.isEmpty() || normalizedStatus.equalsIgnoreCase(safeTrim(datPhong.getTrangThaiDatPhong()))) {
                result.add(datPhong);
            }
        }
        return result;
    }

    public boolean insert(DatPhong datPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || datPhong == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu đặt phòng không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO DatPhong(maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillHeaderStatement(stmt, datPhong);
                boolean inserted = stmt.executeUpdate() > 0;
                if (!inserted) {
                    con.rollback();
                    setLastError("Không thể thêm đặt phòng.");
                    return false;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        datPhong.setMaDatPhong(String.valueOf(rs.getInt(1)));
                    }
                }
            }

            insertChiTietList(con, datPhong);
            updateAssignedRoomStatuses(con, datPhong, resolveRoomStatusForBooking(datPhong.getTrangThaiDatPhong()));
            con.commit();
            return true;
        } catch (Exception e) {
            rollbackQuietly(con);
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(con);
        }
    }

    public boolean update(DatPhong datPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = datPhong == null ? null : parseIntOrNull(datPhong.getMaDatPhong());
        if (con == null || datPhong == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return false;
        }

        String sql = "UPDATE DatPhong SET maKhachHang = ?, maNhanVien = ?, maBangGia = ?, ngayDat = ?, ngayNhanPhong = ?, ngayTraPhong = ?, "
                + "soLuongPhong = ?, soNguoi = ?, tienCoc = ?, trangThai = ? WHERE maDatPhong = ?";

        try {
            con.setAutoCommit(false);
            List<Integer> oldRoomIds = getAssignedRoomIds(con, id.intValue());
            boolean hasStay = hasLuuTruForBooking(con, id.intValue());

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                fillHeaderStatement(stmt, datPhong);
                stmt.setInt(11, id.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("Không tìm thấy đặt phòng để cập nhật.");
                    return false;
                }
            }

            if (!hasStay) {
                releaseRoomsIfBooked(con, oldRoomIds);
                deleteAllChiTietByMaDatPhong(con, id.intValue());
                insertChiTietList(con, datPhong);
                updateAssignedRoomStatuses(con, datPhong, resolveRoomStatusForBooking(datPhong.getTrangThaiDatPhong()));
            }

            con.commit();
            return true;
        } catch (Exception e) {
            rollbackQuietly(con);
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(con);
        }
    }

    public boolean delete(String maDatPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            if (hasLuuTruForBooking(con, id.intValue())) {
                con.rollback();
                setLastError("Đặt phòng đã phát sinh lưu trú, không thể xóa.");
                return false;
            }

            List<Integer> roomIds = getAssignedRoomIds(con, id.intValue());
            deleteAllChiTietByMaDatPhong(con, id.intValue());
            releaseRoomsIfBooked(con, roomIds);

            try (PreparedStatement stmt = con.prepareStatement("DELETE FROM DatPhong WHERE maDatPhong = ?")) {
                stmt.setInt(1, id.intValue());
                boolean deleted = stmt.executeUpdate() > 0;
                if (!deleted) {
                    con.rollback();
                    setLastError("Không tìm thấy đặt phòng để xóa.");
                    return false;
                }
            }

            con.commit();
            return true;
        } catch (Exception e) {
            rollbackQuietly(con);
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(con);
        }
    }

    public List<ChiTietDatPhong> getChiTietByMaDatPhong(String maDatPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return new ArrayList<ChiTietDatPhong>();
        }
        DatPhong temp = new DatPhong();
        temp.setMaDatPhong(String.valueOf(id.intValue()));
        return getChiTietByMaDatPhongInternal(con, temp);
    }

    public boolean updateTrangThai(String maDatPhong, String trangThai) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            String sql = "UPDATE dbo.DatPhong SET trangThai = ? WHERE maDatPhong = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, safeTrim(trangThai));
                stmt.setInt(2, id.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    return false;
                }
            }
            DatPhong booking = findByIdInternal(con, id.intValue());
            if (booking != null) {
                if ("Đã hủy".equalsIgnoreCase(trangThai)) {
                    refreshRoomStatuses(con, getAssignedRoomIds(con, id.intValue()));
                } else {
                    updateAssignedRoomStatuses(con, booking, resolveRoomStatusForBooking(trangThai));
                }
            }
            con.commit();
            return true;
        } catch (Exception e) {
            rollbackQuietly(con);
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(con);
        }
    }

    public boolean restoreCancelledBooking(String maDatPhong, String trangThaiKhoiPhuc) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer id = parseIntOrNull(maDatPhong);
        String targetStatus = safeTrim(trangThaiKhoiPhuc);
        if (con == null || id == null) {
            setLastError(con == null ? "KhÃ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "MÃ£ Ä‘áº·t phÃ²ng khÃ´ng há»£p lá»‡.");
            return false;
        }
        if (targetStatus.isEmpty()) {
            targetStatus = STATUS_PENDING_CHECKIN;
        }

        try {
            con.setAutoCommit(false);
            DatPhong booking = findByIdInternal(con, id.intValue());
            if (booking == null) {
                con.rollback();
                setLastError("KhÃ´ng tÃ¬m tháº¥y booking cáº§n khÃ´i phá»¥c.");
                return false;
            }

            String currentStatus = safeTrim(booking.getTrangThaiDatPhong());
            if (!STATUS_CANCELLED.equalsIgnoreCase(currentStatus) && !STATUS_CANCELLED_BOOKING.equalsIgnoreCase(currentStatus)) {
                con.rollback();
                setLastError("Booking nÃ y khÃ´ng á»Ÿ tráº¡ng thÃ¡i ÄÃ£ há»§y.");
                return false;
            }
            if (hasLuuTruForBooking(con, id.intValue())) {
                con.rollback();
                setLastError("Booking Ä‘Ã£ phÃ¡t sinh lÆ°u trÃº, khÃ´ng thá»ƒ khÃ´i phá»¥c tá»± Ä‘á»™ng.");
                return false;
            }
            if (booking.getChiTietDatPhongs() == null || booking.getChiTietDatPhongs().isEmpty()) {
                con.rollback();
                setLastError("Booking khÃ´ng cÃ²n dÃ²ng chi tiáº¿t phÃ²ng Ä‘á»ƒ khÃ´i phá»¥c.");
                return false;
            }

            for (ChiTietDatPhong detail : booking.getChiTietDatPhongs()) {
                Integer roomId = parseIntOrNull(detail.getMaPhong());
                if (roomId == null || roomId.intValue() <= 0) {
                    con.rollback();
                    setLastError("Booking cÃ²n dÃ²ng chi tiáº¿t chÆ°a gÃ¡n phÃ²ng, khÃ´ng thá»ƒ khÃ´i phá»¥c tá»± Ä‘á»™ng.");
                    return false;
                }
                if (detail.getCheckInDuKien() == null || detail.getCheckOutDuKien() == null
                        || !detail.getCheckOutDuKien().isAfter(detail.getCheckInDuKien())) {
                    con.rollback();
                    setLastError("Khoáº£ng ngÃ y cá»§a booking khÃ´ng há»£p lá»‡, khÃ´ng thá»ƒ khÃ´i phá»¥c.");
                    return false;
                }

                DatPhongConflictInfo conflictInfo = findRoomConflict(
                        roomId.intValue(),
                        detail.getCheckInDuKien(),
                        detail.getCheckOutDuKien(),
                        Integer.valueOf(id.intValue())
                );
                String conflictError = safeTrim(getLastErrorMessage());
                if (!conflictError.isEmpty()) {
                    con.rollback();
                    setLastError(conflictError);
                    return false;
                }
                if (conflictInfo != null) {
                    con.rollback();
                    setLastError(buildRestoreConflictMessage(conflictInfo));
                    return false;
                }
            }

            try (PreparedStatement stmt = con.prepareStatement("UPDATE dbo.DatPhong SET trangThai = ? WHERE maDatPhong = ?")) {
                stmt.setString(1, targetStatus);
                stmt.setInt(2, id.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("KhÃ´ng thá»ƒ cáº­p nháº­t tráº¡ng thÃ¡i khÃ´i phá»¥c.");
                    return false;
                }
            }

            booking.setTrangThaiDatPhong(targetStatus);
            updateAssignedRoomStatuses(con, booking, resolveRoomStatusForBooking(targetStatus));
            con.commit();
            return true;
        } catch (Exception e) {
            rollbackQuietly(con);
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            resetAutoCommit(con);
        }
    }

    public DatPhongConflictInfo findRoomConflict(int maPhong, LocalDate ngayNhanPhong, LocalDate ngayTraPhong, Integer excludeMaDatPhong) {
        clearLastError();
        Connection con = getReadyConnection();
        if (con == null) {
            return null;
        }
        if (maPhong <= 0 || ngayNhanPhong == null || ngayTraPhong == null || !ngayTraPhong.isAfter(ngayNhanPhong)) {
            return null;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 1 dp.maDatPhong, ctdp.maChiTietDatPhong, ")
                .append("ISNULL(lt.maLuuTru, 0) AS maLuuTru, ")
                .append("ISNULL(kh.hoTen, N'Khách chưa xác định') AS tenKhachHang, ")
                .append("ISNULL(p.soPhong, CAST(ctdp.maPhong AS NVARCHAR(20))) AS soPhong, ")
                .append("CAST(COALESCE(lt.checkIn, CAST(dp.ngayNhanPhong AS DATETIME)) AS DATE) AS ngayNhanPhong, ")
                .append("CAST(COALESCE(lt.checkOut, CAST(dp.ngayTraPhong AS DATETIME)) AS DATE) AS ngayTraPhong, ")
                .append("CASE ")
                .append(" WHEN lt.maLuuTru IS NOT NULL AND lt.checkOut IS NULL THEN N'Đang ở' ")
                .append(" WHEN lt.maLuuTru IS NOT NULL AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in') THEN N'Đang ở' ")
                .append(" ELSE dp.trangThai ")
                .append("END AS trangThai ")
                .append("FROM ChiTietDatPhong ctdp ")
                .append("JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong ")
                .append("LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang ")
                .append("LEFT JOIN Phong p ON p.maPhong = ctdp.maPhong ")
                .append("LEFT JOIN LuuTru lt ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong ")
                .append("WHERE ctdp.maPhong = ? ")
                .append("AND CAST(COALESCE(lt.checkIn, CAST(dp.ngayNhanPhong AS DATETIME)) AS DATE) < ? ")
                .append("AND CAST(COALESCE(lt.checkOut, CAST(dp.ngayTraPhong AS DATETIME)) AS DATE) > ? ");
        if (excludeMaDatPhong != null && excludeMaDatPhong.intValue() > 0) {
            sql.append("AND dp.maDatPhong <> ? ");
        }
        sql.append("AND (")
                .append(" dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in', N'Đang ở', N'Đã check-in') ")
                .append(" OR (lt.maLuuTru IS NOT NULL AND (lt.checkOut IS NULL OR CAST(lt.checkOut AS DATE) > ?))")
                .append(") ")
                .append("ORDER BY CASE WHEN lt.maLuuTru IS NOT NULL AND lt.checkOut IS NULL THEN 0 ELSE 1 END, dp.ngayNhanPhong ASC, dp.maDatPhong DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int index = 1;
            stmt.setInt(index++, maPhong);
            stmt.setDate(index++, Date.valueOf(ngayTraPhong));
            stmt.setDate(index++, Date.valueOf(ngayNhanPhong));
            if (excludeMaDatPhong != null && excludeMaDatPhong.intValue() > 0) {
                stmt.setInt(index++, excludeMaDatPhong.intValue());
            }
            stmt.setDate(index, Date.valueOf(ngayNhanPhong));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DatPhongConflictInfo info = new DatPhongConflictInfo();
                    info.setMaDatPhong(rs.getInt("maDatPhong"));
                    info.setMaChiTietDatPhong(rs.getInt("maChiTietDatPhong"));
                    info.setMaLuuTru(rs.getInt("maLuuTru"));
                    info.setTenKhachHang(safeTrim(rs.getString("tenKhachHang")));
                    info.setSoPhong(safeTrim(rs.getString("soPhong")));
                    info.setNgayNhanPhong(toLocalDate(rs.getDate("ngayNhanPhong")));
                    info.setNgayTraPhong(toLocalDate(rs.getDate("ngayTraPhong")));
                    info.setTrangThai(safeTrim(rs.getString("trangThai")));
                    return info;
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<AvailableRoomInfo> getAvailableRooms(LocalDate ngayNhanPhong, LocalDate ngayTraPhong, Integer excludeMaDatPhong, Integer includeMaPhong) {
        clearLastError();
        List<AvailableRoomInfo> result = new ArrayList<AvailableRoomInfo>();
        Connection con = getReadyConnection();
        if (con == null) {
            return result;
        }
        if (ngayNhanPhong == null || ngayTraPhong == null || !ngayTraPhong.isAfter(ngayNhanPhong)) {
            return result;
        }

        String sql = "SELECT p.maPhong, p.soPhong, p.tang, p.trangThai, p.sucChuaToiDa, lp.maLoaiPhong, lp.tenLoaiPhong, lp.giaThamChieu " +
                "FROM dbo.Phong p " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "WHERE (p.trangThai IN (N'Hoạt động', N'Trống', N'Sẵn sàng') OR (? IS NOT NULL AND p.maPhong = ?)) " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM dbo.LuuTru ltActive " +
                "    WHERE ltActive.maPhong = p.maPhong " +
                "      AND ltActive.checkOut IS NULL " +
                "      AND (? IS NULL OR ltActive.maDatPhong <> ?) " +
                ") " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 " +
                "    FROM dbo.ChiTietDatPhong ctdp " +
                "    JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                "    OUTER APPLY (SELECT TOP 1 lt.maLuuTru " +
                "                 FROM dbo.LuuTru lt " +
                "                 WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "                 ORDER BY CASE WHEN lt.checkOut IS NULL THEN 0 ELSE 1 END, COALESCE(lt.checkOut, lt.checkIn) DESC, lt.maLuuTru DESC) latestLt " +
                "    WHERE ctdp.maPhong = p.maPhong " +
                "      AND (? IS NULL OR dp.maDatPhong <> ?) " +
                "      AND latestLt.maLuuTru IS NULL " +
                "      AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in') " +
                "      AND dp.ngayNhanPhong < ? " +
                "      AND dp.ngayTraPhong > ? " +
                ") " +
                "ORDER BY CASE WHEN TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT) IS NULL THEN 1 ELSE 0 END, " +
                "TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            if (includeMaPhong == null) {
                stmt.setNull(index++, java.sql.Types.INTEGER);
                stmt.setNull(index++, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(index++, includeMaPhong.intValue());
                stmt.setInt(index++, includeMaPhong.intValue());
            }
            if (excludeMaDatPhong == null) {
                stmt.setNull(index++, java.sql.Types.INTEGER);
                stmt.setNull(index++, java.sql.Types.INTEGER);
                stmt.setNull(index++, java.sql.Types.INTEGER);
                stmt.setNull(index++, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(index++, excludeMaDatPhong.intValue());
                stmt.setInt(index++, excludeMaDatPhong.intValue());
                stmt.setInt(index++, excludeMaDatPhong.intValue());
                stmt.setInt(index++, excludeMaDatPhong.intValue());
            }
            stmt.setDate(index++, Date.valueOf(ngayTraPhong));
            stmt.setDate(index, Date.valueOf(ngayNhanPhong));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AvailableRoomInfo info = new AvailableRoomInfo();
                    info.setMaPhong(rs.getInt("maPhong"));
                    info.setMaLoaiPhong(rs.getInt("maLoaiPhong"));
                    info.setSoPhong(safeTrim(rs.getString("soPhong")));
                    info.setTang(safeTrim(rs.getString("tang")));
                    info.setTrangThai(safeTrim(rs.getString("trangThai")));
                    info.setTenLoaiPhong(safeTrim(rs.getString("tenLoaiPhong")));
                    info.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
                    info.setGiaThamChieu(rs.getDouble("giaThamChieu"));
                    result.add(info);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private List<ChiTietDatPhong> getChiTietByMaDatPhongInternal(Connection con, DatPhong header) {
        List<ChiTietDatPhong> details = new ArrayList<ChiTietDatPhong>();
        Integer bookingId = parseIntOrNull(header.getMaDatPhong());
        if (con == null || bookingId == null) {
            return details;
        }

        int detailCount = countDetails(con, bookingId.intValue());
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, ctdp.thanhTien, "
                + "dp.ngayNhanPhong, dp.ngayTraPhong, ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, dp.tienCoc AS tienCocHeader, dp.trangThai AS trangThaiDatPhong, "
                + "p.soPhong, p.tang, latestLt.maLuuTru AS maLuuTruGanNhat, latestLt.checkOut AS checkOutGanNhat, "
                + "COALESCE(CAST(p.maLoaiPhong AS NVARCHAR(20)), CAST(bg.maLoaiPhong AS NVARCHAR(20))) AS maLoaiPhongResolved, "
                + "COALESCE(lp.tenLoaiPhong, lp2.tenLoaiPhong) AS tenLoaiPhongResolved "
                + "FROM dbo.ChiTietDatPhong ctdp "
                + "JOIN dbo.DatPhong dp ON ctdp.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN dbo.Phong p ON ctdp.maPhong = p.maPhong "
                + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "LEFT JOIN BangGia bg ON dp.maBangGia = bg.maBangGia "
                + "LEFT JOIN LoaiPhong lp2 ON bg.maLoaiPhong = lp2.maLoaiPhong "
                + "OUTER APPLY (SELECT TOP 1 bgRoom.maBangGia FROM BangGia bgRoom "
                + "             WHERE bgRoom.maLoaiPhong = COALESCE(p.maLoaiPhong, bg.maLoaiPhong) "
                + "               AND bgRoom.trangThai = N'Đang áp dụng' "
                + "             ORDER BY CASE WHEN bgRoom.maBangGia = dp.maBangGia THEN 0 ELSE 1 END, bgRoom.maBangGia DESC) bgResolved "
                + "OUTER APPLY (SELECT TOP 1 lt.maLuuTru, lt.checkOut "
                + "             FROM dbo.LuuTru lt "
                + "             WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong "
                + "             ORDER BY CASE WHEN lt.checkOut IS NULL THEN 0 ELSE 1 END, COALESCE(lt.checkOut, lt.checkIn) DESC, lt.maLuuTru DESC) latestLt "
                + "WHERE ctdp.maDatPhong = ? "
                + "ORDER BY ctdp.maChiTietDatPhong ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, bookingId.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChiTietDatPhong detail = new ChiTietDatPhong();
                    detail.setMaChiTietDatPhong(String.valueOf(rs.getInt("maChiTietDatPhong")));
                    detail.setMaDatPhong(String.valueOf(rs.getInt("maDatPhong")));
                    detail.setMaPhong(rs.getObject("maPhong") == null ? "" : String.valueOf(rs.getInt("maPhong")));
                    detail.setMaLoaiPhong(safeTrim(rs.getString("maLoaiPhongResolved")));
                    detail.setMaBangGia(rs.getObject("maBangGiaResolved") == null ? "" : String.valueOf(rs.getInt("maBangGiaResolved")));
                    detail.setMaChiTietBangGia("");
                    detail.setCheckInDuKien(toLocalDate(rs.getDate("ngayNhanPhong")));
                    detail.setCheckOutDuKien(toLocalDate(rs.getDate("ngayTraPhong")));
                    detail.setSoNguoi(rs.getInt("soNguoi"));
                    detail.setGiaApDung(rs.getDouble("giaPhong"));
                    detail.setTienDatCocChiTiet(detailCount <= 0 ? 0d : rs.getDouble("tienCocHeader") / detailCount);
                    detail.setTrangThaiChiTiet(resolveDetailStatus(rs.getString("trangThaiDatPhong"), rs.getString("soPhong"), rs.getObject("maLuuTruGanNhat") == null ? null : Integer.valueOf(rs.getInt("maLuuTruGanNhat")), rs.getTimestamp("checkOutGanNhat")));
                    detail.setYeuCauKhac("");
                    detail.setGhiChu("");
                    detail.setSoPhong(safeTrim(rs.getString("soPhong")));
                    detail.setTenLoaiPhong(safeTrim(rs.getString("tenLoaiPhongResolved")));
                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return details;
    }

    private DatPhong findByIdInternal(Connection con, int maDatPhong) throws SQLException {
        String sql = SELECT_HEADER_BASE + " WHERE dp.maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DatPhong datPhong = mapDatPhongHeader(rs);
                    datPhong.getChiTietDatPhongs().addAll(getChiTietByMaDatPhongInternal(con, datPhong));
                    normalizeBooking(con, datPhong);
                    return datPhong;
                }
            }
        }
        return null;
    }

    private String buildRestoreConflictMessage(DatPhongConflictInfo conflictInfo) {
        if (conflictInfo == null) {
            return "Má»™t hoáº·c nhiá»u phÃ²ng trong booking Ä‘Ã£ khÃ´ng cÃ²n trá»‘ng trong khoáº£ng ngÃ y cÅ©. KhÃ´ng thá»ƒ khÃ´i phá»¥c tá»± Ä‘á»™ng.";
        }
        return "PhÃ²ng " + defaultIfEmpty(conflictInfo.getSoPhong(), "-")
                + " Ä‘Ã£ khÃ´ng cÃ²n trá»‘ng trong khoáº£ng " + formatDateValue(conflictInfo.getNgayNhanPhong())
                + " - " + formatDateValue(conflictInfo.getNgayTraPhong())
                + " do trÃ¹ng vá»›i booking DP" + conflictInfo.getMaDatPhong()
                + " (" + defaultIfEmpty(conflictInfo.getTrangThai(), "-") + ").";
    }

    private String formatDateValue(LocalDate value) {
        return value == null ? "-" : value.toString();
    }

    private DatPhong mapDatPhongHeader(ResultSet rs) throws SQLException {
        DatPhong datPhong = new DatPhong();
        datPhong.setMaDatPhong(String.valueOf(rs.getInt("maDatPhong")));
        datPhong.setMaKhachHang(rs.getObject("maKhachHang") == null ? "" : String.valueOf(rs.getInt("maKhachHang")));
        datPhong.setMaNhanVien(rs.getObject("maNhanVien") == null ? "" : String.valueOf(rs.getInt("maNhanVien")));
        datPhong.setMaBangGia(rs.getObject("maBangGia") == null ? "" : String.valueOf(rs.getInt("maBangGia")));
        datPhong.setNgayDat(toLocalDate(rs.getDate("ngayDat")));
        datPhong.setNgayNhanPhong(toLocalDate(rs.getDate("ngayNhanPhong")));
        datPhong.setNgayTraPhong(toLocalDate(rs.getDate("ngayTraPhong")));
        datPhong.setSoLuongPhong(rs.getInt("soLuongPhong"));
        datPhong.setSoNguoi(rs.getInt("soNguoi"));
        datPhong.setTienCoc(rs.getDouble("tienCoc"));
        datPhong.setTongTienDatCoc(rs.getDouble("tienCoc"));
        datPhong.setTrangThaiDatPhong(safeTrim(rs.getString("trangThai")));
        datPhong.setNguonDatPhong("");
        datPhong.setGhiChu("");
        datPhong.setTenKhachHang(safeTrim(rs.getString("tenKhachHang")));
        datPhong.setSoDienThoaiKhach(safeTrim(rs.getString("soDienThoaiKhach")));
        datPhong.setCccdPassportKhach(safeTrim(rs.getString("cccdPassportKhach")));
        return datPhong;
    }

    private void fillHeaderStatement(PreparedStatement stmt, DatPhong datPhong) throws SQLException {
        setNullableInt(stmt, 1, datPhong.getMaKhachHang());
        setNullableInt(stmt, 2, datPhong.getMaNhanVien());
        setNullableInt(stmt, 3, datPhong.getMaBangGia());
        stmt.setDate(4, toSqlDate(datPhong.getNgayDat()));
        stmt.setDate(5, toSqlDate(datPhong.getNgayNhanPhong()));
        stmt.setDate(6, toSqlDate(datPhong.getNgayTraPhong()));
        stmt.setInt(7, resolveSoLuongPhong(datPhong));
        stmt.setInt(8, resolveSoNguoi(datPhong));
        stmt.setDouble(9, resolveTienCoc(datPhong));
        stmt.setString(10, defaultIfEmpty(datPhong.getTrangThaiDatPhong(), "Đã đặt"));
    }

    private void insertChiTietList(Connection con, DatPhong datPhong) throws SQLException {
        List<ChiTietDatPhong> details = datPhong.getChiTietDatPhongs();
        if (details == null || details.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (ChiTietDatPhong detail : details) {
                String detailBangGia = resolveBangGiaForDetail(con, detail, datPhong.getMaBangGia());
                applyResolvedRoomRate(detail, detailBangGia, datPhong.getNgayNhanPhong(), datPhong.getNgayTraPhong());
                stmt.setInt(1, Integer.parseInt(datPhong.getMaDatPhong()));
                setNullableInt(stmt, 2, detail.getMaPhong());
                stmt.setInt(3, detail.getSoNguoi() <= 0 ? 1 : detail.getSoNguoi());
                stmt.setDouble(4, detail.getGiaApDung());
                stmt.setDouble(5, calculateThanhTien(detail));
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        detail.setMaChiTietDatPhong(String.valueOf(rs.getInt(1)));
                    }
                }
                detail.setMaDatPhong(datPhong.getMaDatPhong());
            }
        }
    }

    private void deleteAllChiTietByMaDatPhong(Connection con, int maDatPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement("DELETE FROM ChiTietDatPhong WHERE maDatPhong = ?")) {
            stmt.setInt(1, maDatPhong);
            stmt.executeUpdate();
        }
    }

    private void updateAssignedRoomStatuses(Connection con, DatPhong datPhong, String roomStatus) throws SQLException {
        if (datPhong.getChiTietDatPhongs() == null) {
            return;
        }
        List<Integer> roomIds = new ArrayList<Integer>();
        for (ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
            Integer roomId = parseIntOrNull(detail.getMaPhong());
            if (roomId != null) {
                roomIds.add(roomId);
            }
        }
        refreshRoomStatuses(con, roomIds);
    }

    private List<Integer> getAssignedRoomIds(Connection con, int maDatPhong) throws SQLException {
        List<Integer> roomIds = new ArrayList<Integer>();
        String sql = "SELECT maPhong FROM ChiTietDatPhong WHERE maDatPhong = ? AND maPhong IS NOT NULL";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roomIds.add(Integer.valueOf(rs.getInt(1)));
                }
            }
        }
        return roomIds;
    }

    private void releaseRoomsIfBooked(Connection con, List<Integer> roomIds) throws SQLException {
        if (roomIds == null || roomIds.isEmpty()) {
            return;
        }
        refreshRoomStatuses(con, roomIds);
    }

    public void refreshRoomStatuses(Connection con, List<Integer> roomIds) throws SQLException {
        if (con == null || roomIds == null || roomIds.isEmpty()) {
            return;
        }
        LinkedHashSet<Integer> uniqueRoomIds = new LinkedHashSet<Integer>();
        for (Integer roomId : roomIds) {
            if (roomId != null && roomId.intValue() > 0) {
                uniqueRoomIds.add(roomId);
            }
        }
        if (uniqueRoomIds.isEmpty()) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement("UPDATE dbo.Phong SET trangThai = ? WHERE maPhong = ?")) {
            for (Integer roomId : uniqueRoomIds) {
                String status = resolveOperationalRoomStatus(con, roomId.intValue());
                if (status.isEmpty()) {
                    continue;
                }
                stmt.setString(1, status);
                stmt.setInt(2, roomId.intValue());
                stmt.executeUpdate();
            }
        }
    }

    public void refreshRoomStatus(Connection con, int maPhong) throws SQLException {
        if (maPhong <= 0) {
            return;
        }
        List<Integer> roomIds = new ArrayList<Integer>();
        roomIds.add(Integer.valueOf(maPhong));
        refreshRoomStatuses(con, roomIds);
    }

    private String resolveOperationalRoomStatus(Connection con, int maPhong) throws SQLException {
        String currentStatus = loadCurrentRoomStatus(con, maPhong);
        if ("Bảo trì".equalsIgnoreCase(currentStatus)) {
            return "Bảo trì";
        }
        if (hasActiveStayForRoom(con, maPhong)) {
            return "Đang ở";
        }
        if (hasBookedAssignmentForRoom(con, maPhong)) {
            return "Đã đặt";
        }
        if ("Dọn dẹp".equalsIgnoreCase(currentStatus)) {
            return "Dọn dẹp";
        }
        return "Hoạt động";
    }

    private String loadCurrentRoomStatus(Connection con, int maPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement("SELECT ISNULL(trangThai, N'') FROM Phong WHERE maPhong = ?")) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return safeTrim(rs.getString(1));
                }
            }
        }
        return "";
    }

    private boolean hasActiveStayForRoom(Connection con, int maPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT COUNT(1) FROM dbo.LuuTru WHERE maPhong = ? AND checkOut IS NULL")) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasBookedAssignmentForRoom(Connection con, int maPhong) throws SQLException {
        String sql = "SELECT COUNT(1) "
                + "FROM dbo.ChiTietDatPhong ctdp "
                + "JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                + "WHERE ctdp.maPhong = ? "
                + "AND ISNULL(dp.trangThai, N'') IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in', N'Đang ở', N'Check-out một phần', N'Đã check-in') "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasLuuTruForBooking(Connection con, int maDatPhong) throws SQLException {
        String sql = "SELECT COUNT(1) FROM LuuTru WHERE maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private int countDetails(Connection con, int maDatPhong) {
        String sql = "SELECT COUNT(1) FROM ChiTietDatPhong WHERE maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private void normalizeBooking(Connection con, DatPhong datPhong) {
        int detailCount = datPhong.getChiTietDatPhongs().size();
        if (datPhong.getSoLuongPhong() <= 0) {
            datPhong.setSoLuongPhong(detailCount == 0 ? 1 : detailCount);
        }
        if (datPhong.getTongTienDatCoc() <= 0) {
            datPhong.setTongTienDatCoc(datPhong.getTienCoc());
        }
        if (datPhong.getSoNguoi() <= 0 && detailCount > 0) {
            int totalGuests = 0;
            for (ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
                totalGuests += detail.getSoNguoi();
            }
            datPhong.setSoNguoi(totalGuests);
        }
        String resolvedStatus = resolveBookingStatus(con, datPhong);
        if (!resolvedStatus.isEmpty()) {
            datPhong.setTrangThaiDatPhong(resolvedStatus);
        }
    }

    private int resolveSoLuongPhong(DatPhong datPhong) {
        if (datPhong.getSoLuongPhong() > 0) {
            return datPhong.getSoLuongPhong();
        }
        int size = datPhong.getChiTietDatPhongs() == null ? 0 : datPhong.getChiTietDatPhongs().size();
        return size <= 0 ? 1 : size;
    }

    private int resolveSoNguoi(DatPhong datPhong) {
        if (datPhong.getSoNguoi() > 0) {
            return datPhong.getSoNguoi();
        }
        if (datPhong.getChiTietDatPhongs() == null || datPhong.getChiTietDatPhongs().isEmpty()) {
            return 1;
        }
        int total = 0;
        for (ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
            total += detail.getSoNguoi();
        }
        return total <= 0 ? 1 : total;
    }

    private double resolveTienCoc(DatPhong datPhong) {
        if (datPhong.getTienCoc() > 0) {
            return datPhong.getTienCoc();
        }
        if (datPhong.getTongTienDatCoc() > 0) {
            return datPhong.getTongTienDatCoc();
        }
        if (datPhong.getChiTietDatPhongs() == null) {
            return 0d;
        }
        double total = 0d;
        for (ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
            total += detail.getTienDatCocChiTiet();
        }
        return total;
    }

    private double calculateThanhTien(ChiTietDatPhong detail) {
        LocalDate checkIn = detail.getCheckInDuKien();
        LocalDate checkOut = detail.getCheckOutDuKien();
        long nights = 1L;
        if (checkIn != null && checkOut != null) {
            long diff = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (diff > 0) {
                nights = diff;
            }
        }
        return detail.getGiaApDung() * nights;
    }

    public String determineLoaiNgay(LocalDate date) {
        return toDayTypeDisplay(resolveDayTypeKey(date));
    }

    public String determineLoaiNgay(LocalDate checkIn, LocalDate checkOut) {
        return toDayTypeDisplay(resolveDayTypeKey(checkIn, checkOut));
    }

    public RoomRateResolution resolveRoomRate(String maBangGia, LocalDate checkIn, LocalDate checkOut) {
        return resolveRoomRateWithSurcharge(maBangGia, checkIn, checkOut);
    }

    private void applyResolvedRoomRate(ChiTietDatPhong detail, String maBangGia, LocalDate defaultCheckIn, LocalDate defaultCheckOut) {
        if (detail == null) {
            return;
        }
        LocalDate checkIn = detail.getCheckInDuKien() == null ? defaultCheckIn : detail.getCheckInDuKien();
        LocalDate checkOut = detail.getCheckOutDuKien() == null ? defaultCheckOut : detail.getCheckOutDuKien();
        RoomRateResolution resolution = resolveRoomRateWithSurcharge(maBangGia, checkIn, checkOut);
        detail.setGiaApDung(resolution.getGiaApDung());
        detail.setMaBangGia(defaultIfEmpty(maBangGia, detail.getMaBangGia()));
        detail.setMaChiTietBangGia(resolution.getMaChiTietBangGia());
        detail.setGhiChu(buildRateNote(resolution));
    }

    private String resolveBangGiaForDetail(Connection con, ChiTietDatPhong detail, String defaultMaBangGia) throws SQLException {
        if (detail == null) {
            return defaultMaBangGia;
        }
        Integer preferredBangGia = parseIntOrNull(defaultMaBangGia);
        Integer roomTypeId = parseIntOrNull(detail.getMaLoaiPhong());
        if (roomTypeId == null) {
            Integer roomId = parseIntOrNull(detail.getMaPhong());
            if (roomId != null) {
                roomTypeId = findRoomTypeIdByRoom(con, roomId.intValue());
            }
        }
        if (roomTypeId == null) {
            return defaultIfEmpty(detail.getMaBangGia(), defaultMaBangGia);
        }
        Integer bangGiaId = findAppliedBangGiaByRoomType(con, roomTypeId.intValue(), preferredBangGia);
        if (bangGiaId != null) {
            return String.valueOf(bangGiaId.intValue());
        }
        return defaultIfEmpty(detail.getMaBangGia(), defaultMaBangGia);
    }

    private Integer findRoomTypeIdByRoom(Connection con, int maPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement("SELECT TOP 1 maLoaiPhong FROM Phong WHERE maPhong = ?")) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private Integer findAppliedBangGiaByRoomType(Connection con, int maLoaiPhong, Integer preferredBangGia) throws SQLException {
        String sql = "SELECT TOP 1 maBangGia FROM BangGia WHERE maLoaiPhong = ? AND trangThai = N'Đang áp dụng' "
                + "ORDER BY CASE WHEN ? IS NOT NULL AND maBangGia = ? THEN 0 ELSE 1 END, maBangGia DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiPhong);
            if (preferredBangGia == null) {
                stmt.setObject(2, null);
                stmt.setObject(3, null);
            } else {
                stmt.setInt(2, preferredBangGia.intValue());
                stmt.setInt(3, preferredBangGia.intValue());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private String buildRateNote(RoomRateResolution resolution) {
        if (resolution == null) {
            return "";
        }
        String note = resolution.getLoaiNgay() + " - " + resolution.getLoaiGiaApDung();
        if (resolution.getGiaNenApDung() > 0d) {
            note += " - gia co ban " + Math.round(resolution.getGiaNenApDung());
        }
        if (resolution.getTongPhuThuApDung() > 0d) {
            note += " - phu thu " + Math.round(resolution.getTongPhuThuApDung());
        }
        if (resolution.getThanhTienApDung() > 0d) {
            note += " - thanh tien " + Math.round(resolution.getThanhTienApDung());
        }
        return note;
    }

    private boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private double chooseLowerPositive(double first, double second) {
        if (first <= 0d) {
            return Math.max(second, 0d);
        }
        if (second <= 0d) {
            return Math.max(first, 0d);
        }
        return Math.min(first, second);
    }

    private String normalizeLoaiNgay(String value) {
        String normalized = safeTrim(value);
        if (normalized.isEmpty()) {
            return LOAI_NGAY_THUONG;
        }
        String lower = normalized.toLowerCase();
        if (lower.contains("lá»") || lower.contains("lễ") || lower.endsWith("le")) {
            return LOAI_NGAY_LE;
        }
        if (lower.contains("cuá»") || lower.contains("tuáº§n") || lower.contains("cuối") || lower.contains("tuan")) {
            return LOAI_NGAY_CUOI_TUAN;
        }
        return LOAI_NGAY_THUONG;
    }

    private String normalizeLoaiGia(String value) {
        String normalized = safeTrim(value);
        if (normalized.isEmpty()) {
            return LOAI_GIA_THEO_NGAY;
        }
        String lower = normalized.toLowerCase();
        if (lower.contains("qua") && (lower.contains("đêm") || lower.contains("ªm") || lower.contains("dem"))) {
            return LOAI_GIA_QUA_DEM;
        }
        if (lower.contains("lễ") || lower.contains("lá»") || lower.endsWith("le")) {
            return LOAI_GIA_LE;
        }
        if (lower.contains("cuối") || lower.contains("cuá»") || lower.contains("tuan") || lower.contains("tuáº§n")) {
            return LOAI_GIA_CUOI_TUAN;
        }
        if (lower.contains("giờ") || lower.contains("giá»") || lower.contains("gio")) {
            return LOAI_GIA_THEO_GIO;
        }
        return LOAI_GIA_THEO_NGAY;
    }

    public RoomRateResolution resolveRoomRateWithSurcharge(String maBangGia, LocalDate checkIn, LocalDate checkOut) {
        RoomRateResolution resolution = new RoomRateResolution();
        String dayType = resolveDayTypeKey(checkIn, checkOut);
        String stayType = resolveStayTypeKey(checkIn, checkOut);
        resolution.setLoaiNgayKey(dayType);
        resolution.setLoaiLuuTruKey(stayType);
        resolution.setLoaiNgay(buildDayTypeSummary(checkIn, checkOut));
        resolution.setLoaiGiaApDung(buildStayTypeSummary(stayType, checkIn, checkOut));
        resolution.setGiaNenApDung(0d);
        resolution.setPhuThuApDung(0d);
        resolution.setTongPhuThuApDung(0d);
        resolution.setGiaApDung(0d);
        resolution.setThanhTienApDung(0d);
        resolution.setMaChiTietBangGia("");

        Integer bangGiaId = parseIntOrNull(maBangGia);
        if (bangGiaId == null) {
            return resolution;
        }

        ChiTietBangGia detail = bangGiaDAO.getChiTietBangGiaDangApDung(bangGiaId.intValue(), checkIn);
        if (detail == null) {
            List<ChiTietBangGia> details = bangGiaDAO.getChiTietBangGiaByMaBangGia(bangGiaId.intValue());
            if (!details.isEmpty()) {
                detail = details.get(0);
            }
        }
        if (detail == null) {
            return resolution;
        }

        long pricingUnits = resolvePricingUnits(stayType, checkIn, checkOut);
        double baseTotal = Math.max(resolveBaseAmount(detail, stayType, checkIn, checkOut, pricingUnits), 0d);
        double surchargeTotal = Math.max(resolveSurchargeAmount(detail, stayType, checkIn, checkOut), 0d);
        double appliedTotal = baseTotal + surchargeTotal;
        double appliedRate = pricingUnits <= 0L ? appliedTotal : appliedTotal / pricingUnits;

        resolution.setMaChiTietBangGia(String.valueOf(detail.getMaChiTietBangGia()));
        resolution.setGiaNenApDung(baseTotal);
        resolution.setPhuThuApDung(pricingUnits <= 0L ? surchargeTotal : surchargeTotal / pricingUnits);
        resolution.setTongPhuThuApDung(surchargeTotal);
        resolution.setGiaApDung(appliedRate);
        resolution.setThanhTienApDung(Math.max(appliedTotal, 0d));
        return resolution;
    }

    private String resolveDayTypeKey(LocalDate date) {
        if (date == null) {
            return DAY_TYPE_NORMAL;
        }
        if (ngayLeDAO.isHoliday(date)) {
            return DAY_TYPE_HOLIDAY;
        }
        return isWeekend(date) ? DAY_TYPE_WEEKEND : DAY_TYPE_NORMAL;
    }

    private String resolveDayTypeKey(LocalDate checkIn, LocalDate checkOut) {
        LocalDate start = checkIn != null ? checkIn : checkOut;
        LocalDate end = checkOut != null ? checkOut : checkIn;
        if (start == null) {
            return DAY_TYPE_NORMAL;
        }
        if (end == null || end.isBefore(start)) {
            end = start;
        }

        boolean hasWeekend = false;
        for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
            if (ngayLeDAO.isHoliday(current)) {
                return DAY_TYPE_HOLIDAY;
            }
            if (isWeekend(current)) {
                hasWeekend = true;
            }
        }
        return hasWeekend ? DAY_TYPE_WEEKEND : DAY_TYPE_NORMAL;
    }

    private String buildDayTypeSummary(LocalDate checkIn, LocalDate checkOut) {
        LocalDate start = checkIn != null ? checkIn : checkOut;
        LocalDate end = checkOut != null ? checkOut : checkIn;
        if (start == null) {
            return DISPLAY_LOAI_NGAY_THUONG;
        }
        if (end == null || end.isBefore(start)) {
            end = start;
        }

        boolean hasHoliday = false;
        boolean hasWeekend = false;
        boolean hasNormal = false;
        for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
            if (ngayLeDAO.isHoliday(current)) {
                hasHoliday = true;
            } else if (isWeekend(current)) {
                hasWeekend = true;
            } else {
                hasNormal = true;
            }
        }

        if (!hasHoliday && !hasWeekend) {
            return DISPLAY_LOAI_NGAY_THUONG;
        }
        if (hasHoliday && !hasWeekend && !hasNormal) {
            return DISPLAY_LOAI_NGAY_LE;
        }
        if (!hasHoliday && hasWeekend && !hasNormal) {
            return DISPLAY_LOAI_NGAY_CUOI_TUAN;
        }

        if (hasHoliday) {
            return "C\u00f3 ng\u00e0y l\u1ec5 trong kho\u1ea3ng l\u01b0u tr\u00fa";
        }
        return "C\u00f3 cu\u1ed1i tu\u1ea7n trong kho\u1ea3ng l\u01b0u tr\u00fa";
    }

    private String buildStayTypeSummary(String stayType, LocalDate checkIn, LocalDate checkOut) {
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return "Qua \u0111\u00eam - 1 \u0111\u00eam";
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return "Theo gi\u1edd";
        }
        long soNgay = resolvePricingUnits(STAY_TYPE_DAILY, checkIn, checkOut);
        return "Theo ng\u00e0y - " + Math.max(1L, soNgay) + " ng\u00e0y";
    }

    private String resolveStayTypeKey(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return STAY_TYPE_HOURLY;
        }
        long soDem = Math.max(1L, ChronoUnit.DAYS.between(checkIn, checkOut));
        return soDem == 1L ? STAY_TYPE_OVERNIGHT : STAY_TYPE_DAILY;
    }

    private List<LocalDate> resolvePricingDates(LocalDate checkIn, LocalDate checkOut) {
        List<LocalDate> pricingDates = new ArrayList<LocalDate>();
        LocalDate start = checkIn != null ? checkIn : checkOut;
        if (start == null) {
            return pricingDates;
        }
        if (checkOut == null || !checkOut.isAfter(start)) {
            pricingDates.add(start);
            return pricingDates;
        }
        for (LocalDate current = start; current.isBefore(checkOut); current = current.plusDays(1)) {
            pricingDates.add(current);
        }
        if (pricingDates.isEmpty()) {
            pricingDates.add(start);
        }
        return pricingDates;
    }

    private double resolveBaseRate(ChiTietBangGia detail, String stayType) {
        if (detail == null) {
            return 0d;
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return detail.getGiaTheoGio();
        }
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return detail.getGiaQuaDem();
        }
        return detail.getGiaTheoNgay();
    }

    private double resolveBaseAmount(ChiTietBangGia detail, String stayType, LocalDate checkIn, LocalDate checkOut, long pricingUnits) {
        double baseRate = Math.max(resolveBaseRate(detail, stayType), 0d);
        if (STAY_TYPE_DAILY.equals(stayType)) {
            return baseRate * Math.max(pricingUnits, 1L);
        }
        return baseRate * Math.max(pricingUnits, 1L);
    }

    private double resolveSurcharge(ChiTietBangGia detail, String dayType) {
        if (detail == null) {
            return 0d;
        }
        if (DAY_TYPE_HOLIDAY.equals(dayType)) {
            return detail.getHolidaySurcharge();
        }
        if (DAY_TYPE_WEEKEND.equals(dayType)) {
            return detail.getWeekendSurcharge();
        }
        return 0d;
    }

    private double resolveSurchargeAmount(ChiTietBangGia detail, String stayType, LocalDate checkIn, LocalDate checkOut) {
        if (detail == null) {
            return 0d;
        }
        if (STAY_TYPE_DAILY.equals(stayType)) {
            double total = 0d;
            for (LocalDate pricingDate : resolvePricingDates(checkIn, checkOut)) {
                total += Math.max(resolveSurcharge(detail, resolveDayTypeKey(pricingDate)), 0d);
            }
            return total;
        }
        return Math.max(resolveSurcharge(detail, resolveDayTypeKey(checkIn, checkOut)), 0d);
    }

    private long resolvePricingUnits(String stayType, LocalDate checkIn, LocalDate checkOut) {
        if (!STAY_TYPE_DAILY.equals(stayType)) {
            return 1L;
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 1L;
        }
        return Math.max(1L, ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    private double firstPositive(double... values) {
        if (values == null) {
            return 0d;
        }
        for (double value : values) {
            if (value > 0d) {
                return value;
            }
        }
        return 0d;
    }

    private String toDayTypeDisplay(String dayType) {
        if (DAY_TYPE_HOLIDAY.equals(dayType)) {
            return DISPLAY_LOAI_NGAY_LE;
        }
        if (DAY_TYPE_WEEKEND.equals(dayType)) {
            return DISPLAY_LOAI_NGAY_CUOI_TUAN;
        }
        return DISPLAY_LOAI_NGAY_THUONG;
    }

    private String toStayTypeDisplay(String stayType) {
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return DISPLAY_LOAI_GIA_QUA_DEM;
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return DISPLAY_LOAI_GIA_THEO_GIO;
        }
        return DISPLAY_LOAI_GIA_THEO_NGAY;
    }

    private String resolveRoomStatusForBooking(String bookingStatus) {
        String status = safeTrim(bookingStatus);
        if (STATUS_ACTIVE.equalsIgnoreCase(status)
                || STATUS_PARTIAL_CHECKOUT.equalsIgnoreCase(status)
                || "Đã check-in".equalsIgnoreCase(status)) {
            return "Đang ở";
        }
        if (STATUS_CANCELLED.equalsIgnoreCase(status)
                || STATUS_CANCELLED_BOOKING.equalsIgnoreCase(status)
                || STATUS_WAIT_PAYMENT.equalsIgnoreCase(status)
                || STATUS_PAID.equalsIgnoreCase(status)
                || STATUS_CHECKED_OUT.equalsIgnoreCase(status)) {
            return "Hoạt động";
        }
        if ("Đã đặt".equalsIgnoreCase(status)
                || "Đã xác nhận".equalsIgnoreCase(status)
                || "Đã cọc".equalsIgnoreCase(status)
                || STATUS_PENDING_CHECKIN.equalsIgnoreCase(status)) {
            return "Đã đặt";
        }
        return "Đã đặt";
    }

    private String resolveDetailStatus(String bookingStatus, String soPhong, Integer latestStayId, Timestamp latestCheckOut) {
        String status = safeTrim(bookingStatus);
        if (STATUS_CANCELLED.equalsIgnoreCase(status) || STATUS_CANCELLED_BOOKING.equalsIgnoreCase(status)) {
            return "Đã hủy";
        }
        if (latestStayId != null) {
            return latestCheckOut == null ? "Đang ở" : "Đã check-out";
        }
        if (safeTrim(soPhong).isEmpty()) {
            return "Chưa gán phòng";
        }
        if (STATUS_ACTIVE.equalsIgnoreCase(status)
                || STATUS_PARTIAL_CHECKOUT.equalsIgnoreCase(status)
                || "Đã check-in".equalsIgnoreCase(status)) {
            return "Chờ check-in";
        }
        if ("Đã đặt".equalsIgnoreCase(status)
                || "Đã xác nhận".equalsIgnoreCase(status)
                || "Đã cọc".equalsIgnoreCase(status)
                || STATUS_PENDING_CHECKIN.equalsIgnoreCase(status)) {
            return "Chờ check-in";
        }
        if (STATUS_CHECKED_OUT.equalsIgnoreCase(status)
                || STATUS_WAIT_PAYMENT.equalsIgnoreCase(status)
                || STATUS_PAID.equalsIgnoreCase(status)) {
            return "Đã check-out";
        }
        return status.isEmpty() ? "Đã gán phòng" : status;
    }

    private String resolveBookingStatus(Connection con, DatPhong datPhong) {
        if (con == null || datPhong == null) {
            return "";
        }
        String currentStatus = safeTrim(datPhong.getTrangThaiDatPhong());
        if (STATUS_CANCELLED.equalsIgnoreCase(currentStatus) || STATUS_CANCELLED_BOOKING.equalsIgnoreCase(currentStatus)) {
            return STATUS_CANCELLED;
        }

        Integer maDatPhong = parseIntOrNull(datPhong.getMaDatPhong());
        if (maDatPhong == null) {
            return currentStatus;
        }

        String sql = "SELECT COUNT(1) AS soChiTiet, " +
                "SUM(CASE WHEN stayStats.coDangO = 1 THEN 1 ELSE 0 END) AS soChiTietDangO, " +
                "SUM(CASE WHEN stayStats.coDaCheckOut = 1 THEN 1 ELSE 0 END) AS soChiTietDaCheckOut, " +
                "SUM(CASE WHEN stayStats.coLuuTru = 1 THEN 1 ELSE 0 END) AS soChiTietDaPhatSinhLuuTru " +
                "FROM ChiTietDatPhong ctdp " +
                "OUTER APPLY ( " +
                "    SELECT CASE WHEN EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong AND lt.checkOut IS NULL) THEN 1 ELSE 0 END AS coDangO, " +
                "           CASE WHEN EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong AND lt.checkOut IS NOT NULL) THEN 1 ELSE 0 END AS coDaCheckOut, " +
                "           CASE WHEN EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong) THEN 1 ELSE 0 END AS coLuuTru " +
                ") stayStats " +
                "WHERE ctdp.maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return currentStatus;
                }

                int soChiTiet = rs.getInt("soChiTiet");
                int soChiTietDangO = rs.getInt("soChiTietDangO");
                int soChiTietDaCheckOut = rs.getInt("soChiTietDaCheckOut");
                int soChiTietDaPhatSinhLuuTru = rs.getInt("soChiTietDaPhatSinhLuuTru");

                if (isBookingPaid(con, maDatPhong.intValue())) {
                    return STATUS_PAID;
                }
                if (soChiTietDangO > 0) {
                    return STATUS_ACTIVE;
                }
                if (soChiTiet > 0 && soChiTietDaCheckOut >= soChiTiet) {
                    return STATUS_WAIT_PAYMENT;
                }
                if (soChiTietDaPhatSinhLuuTru <= 0) {
                    return STATUS_PENDING_CHECKIN;
                }
                if (soChiTietDaCheckOut > 0 && soChiTietDaPhatSinhLuuTru < soChiTiet) {
                    return STATUS_PENDING_CHECKIN;
                }
                if (soChiTietDaCheckOut > 0) {
                    return STATUS_PARTIAL_CHECKOUT;
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return currentStatus.isEmpty() ? STATUS_PENDING_CHECKIN : currentStatus;
    }

    private boolean isBookingPaid(Connection con, int maDatPhong) throws SQLException {
        String sql = "SELECT COUNT(1) FROM HoaDon WHERE maDatPhong = ? AND ISNULL(trangThai, N'') = N'Đã thanh toán'";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, String value) throws SQLException {
        Integer parsed = parseIntOrNull(value);
        if (parsed == null) {
            stmt.setObject(index, null);
        } else {
            stmt.setInt(index, parsed.intValue());
        }
    }

    private Integer parseIntOrNull(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Integer.valueOf(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultIfEmpty(String value, String fallback) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private void rollbackQuietly(Connection con) {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (SQLException ignored) {
        }
    }

    private void resetAutoCommit(Connection con) {
        try {
            if (con != null) {
                con.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }
    }


    private Connection getReadyConnection() {
        return ConnectDB.getConnection();
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }

    public static final class AvailableRoomInfo {
        private int maPhong;
        private int maLoaiPhong;
        private String soPhong;
        private String tang;
        private String trangThai;
        private String tenLoaiPhong;
        private int sucChuaToiDa;
        private double giaThamChieu;

        public int getMaPhong() {
            return maPhong;
        }

        public void setMaPhong(int maPhong) {
            this.maPhong = maPhong;
        }

        public int getMaLoaiPhong() {
            return maLoaiPhong;
        }

        public void setMaLoaiPhong(int maLoaiPhong) {
            this.maLoaiPhong = maLoaiPhong;
        }

        public String getSoPhong() {
            return soPhong;
        }

        public void setSoPhong(String soPhong) {
            this.soPhong = soPhong;
        }

        public String getTang() {
            return tang;
        }

        public void setTang(String tang) {
            this.tang = tang;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public void setTrangThai(String trangThai) {
            this.trangThai = trangThai;
        }

        public String getTenLoaiPhong() {
            return tenLoaiPhong;
        }

        public void setTenLoaiPhong(String tenLoaiPhong) {
            this.tenLoaiPhong = tenLoaiPhong;
        }

        public int getSucChuaToiDa() {
            return sucChuaToiDa;
        }

        public void setSucChuaToiDa(int sucChuaToiDa) {
            this.sucChuaToiDa = sucChuaToiDa;
        }

        public double getGiaThamChieu() {
            return giaThamChieu;
        }

        public void setGiaThamChieu(double giaThamChieu) {
            this.giaThamChieu = giaThamChieu;
        }
    }

    public static final class RoomRateResolution {
        private String loaiNgayKey;
        private String loaiLuuTruKey;
        private String loaiNgay;
        private String loaiGiaApDung;
        private double giaNenApDung;
        private double phuThuApDung;
        private double tongPhuThuApDung;
        private double giaApDung;
        private double thanhTienApDung;
        private String maChiTietBangGia;

        public String getLoaiNgayKey() {
            return loaiNgayKey;
        }

        public void setLoaiNgayKey(String loaiNgayKey) {
            this.loaiNgayKey = loaiNgayKey;
        }

        public String getLoaiLuuTruKey() {
            return loaiLuuTruKey;
        }

        public void setLoaiLuuTruKey(String loaiLuuTruKey) {
            this.loaiLuuTruKey = loaiLuuTruKey;
        }

        public String getLoaiNgay() {
            return loaiNgay;
        }

        public void setLoaiNgay(String loaiNgay) {
            this.loaiNgay = loaiNgay;
        }

        public String getLoaiGiaApDung() {
            return loaiGiaApDung;
        }

        public void setLoaiGiaApDung(String loaiGiaApDung) {
            this.loaiGiaApDung = loaiGiaApDung;
        }

        public double getGiaNenApDung() {
            return giaNenApDung;
        }

        public void setGiaNenApDung(double giaNenApDung) {
            this.giaNenApDung = giaNenApDung;
        }

        public double getPhuThuApDung() {
            return phuThuApDung;
        }

        public void setPhuThuApDung(double phuThuApDung) {
            this.phuThuApDung = phuThuApDung;
        }

        public double getTongPhuThuApDung() {
            return tongPhuThuApDung;
        }

        public void setTongPhuThuApDung(double tongPhuThuApDung) {
            this.tongPhuThuApDung = tongPhuThuApDung;
        }

        public double getGiaApDung() {
            return giaApDung;
        }

        public void setGiaApDung(double giaApDung) {
            this.giaApDung = giaApDung;
        }

        public double getThanhTienApDung() {
            return thanhTienApDung;
        }

        public void setThanhTienApDung(double thanhTienApDung) {
            this.thanhTienApDung = thanhTienApDung;
        }

        public String getMaChiTietBangGia() {
            return maChiTietBangGia;
        }

        public void setMaChiTietBangGia(String maChiTietBangGia) {
            this.maChiTietBangGia = maChiTietBangGia;
        }
    }
}

