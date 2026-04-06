package dao;

import db.ConnectDB;
import entity.LuuTru;
import entity.Phong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckInOutDAO {
    private static final String SELECT_BASE =
            "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, lt.checkIn, lt.checkOut, lt.soNguoi, lt.giaPhong, lt.tienCoc, "
                    + "dp.trangThai AS trangThaiDatPhong, kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhach, "
                    + "p.soPhong, p.tang, p.trangThai AS trangThaiPhong, "
                    + "COALESCE(lp.tenLoaiPhong, lp2.tenLoaiPhong) AS tenLoaiPhong "
                    + "FROM LuuTru lt "
                    + "LEFT JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong "
                    + "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang "
                    + "LEFT JOIN Phong p ON lt.maPhong = p.maPhong "
                    + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                    + "LEFT JOIN ChiTietDatPhong ctdp ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong "
                    + "LEFT JOIN DatPhong dp2 ON ctdp.maDatPhong = dp2.maDatPhong "
                    + "LEFT JOIN BangGia bg ON dp2.maBangGia = bg.maBangGia "
                    + "LEFT JOIN LoaiPhong lp2 ON bg.maLoaiPhong = lp2.maLoaiPhong";

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<LuuTru> getAll() {
        clearLastError();
        List<LuuTru> result = new ArrayList<LuuTru>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String sql = SELECT_BASE + " ORDER BY lt.maLuuTru DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapLuuTru(rs));
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<LuuTru> getByTrangThaiDatPhong(String trangThaiDatPhong) {
        clearLastError();
        List<LuuTru> result = new ArrayList<LuuTru>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String status = safeTrim(trangThaiDatPhong);
        String sql = SELECT_BASE + " WHERE (? = '' OR dp.trangThai = ?) ORDER BY lt.maLuuTru DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapLuuTru(rs));
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public LuuTru findById(String maLuuTru) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maLuuTru);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã lưu trú không hợp lệ.");
            return null;
        }

        String sql = SELECT_BASE + " WHERE lt.maLuuTru = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapLuuTru(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(LuuTru luuTru) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || luuTru == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu lưu trú không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillStatement(stmt, luuTru);
                boolean inserted = stmt.executeUpdate() > 0;
                if (!inserted) {
                    con.rollback();
                    setLastError("Không thể thêm lưu trú.");
                    return false;
                }
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        luuTru.setMaLuuTru(String.valueOf(rs.getInt(1)));
                    }
                }
            }

            updateBookingStatus(con, parseIntOrNull(luuTru.getMaDatPhong()), shouldMoveToCleaning(luuTru) ? "Đã check-out" : "Đang lưu trú");
            synchronizeOperationalStatuses(con);
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

    public boolean update(LuuTru luuTru) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = luuTru == null ? null : parseIntOrNull(luuTru.getMaLuuTru());
        if (con == null || luuTru == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã lưu trú không hợp lệ.");
            return false;
        }

        String sql = "UPDATE LuuTru SET maChiTietDatPhong = ?, maDatPhong = ?, maPhong = ?, checkIn = ?, checkOut = ?, soNguoi = ?, giaPhong = ?, tienCoc = ? WHERE maLuuTru = ?";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                fillStatement(stmt, luuTru);
                stmt.setInt(9, id.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("Không tìm thấy lưu trú để cập nhật.");
                    return false;
                }
            }

            updateBookingStatus(con, parseIntOrNull(luuTru.getMaDatPhong()), shouldMoveToCleaning(luuTru) ? "Đã check-out" : "Đang lưu trú");
            synchronizeOperationalStatuses(con);
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

    public boolean delete(String maLuuTru) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maLuuTru);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã lưu trú không hợp lệ.");
            return false;
        }

        LuuTru existing = findById(maLuuTru);
        if (existing == null) {
            return false;
        }

        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement("DELETE FROM LuuTru WHERE maLuuTru = ?")) {
                stmt.setInt(1, id.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("Không tìm thấy lưu trú để xóa.");
                    return false;
                }
            }

            updateBookingStatus(con, parseIntOrNull(existing.getMaDatPhong()), "Đã xác nhận");
            synchronizeOperationalStatuses(con);
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

    public List<Phong> getPhongTrongTheoThuTuTang(String maLoaiPhong) {
        clearLastError();
        List<Phong> result = new ArrayList<Phong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String sql = "SELECT p.maPhong, p.maLoaiPhong, p.soPhong, p.tang, p.khuVuc, p.sucChuaChuan, p.sucChuaToiDa, p.trangThai, lp.tenLoaiPhong "
                + "FROM Phong p "
                + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE p.trangThai = N'Trống' AND (? = '' OR CAST(p.maLoaiPhong AS NVARCHAR(20)) = ?) "
                + "ORDER BY TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            String loaiPhong = safeTrim(maLoaiPhong);
            stmt.setString(1, loaiPhong);
            stmt.setString(2, loaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Phong phong = new Phong(
                            rs.getInt("maPhong"),
                            rs.getInt("maLoaiPhong"),
                            rs.getString("soPhong"),
                            rs.getString("tang"),
                            rs.getString("khuVuc"),
                            rs.getInt("sucChuaChuan"),
                            rs.getInt("sucChuaToiDa"),
                            rs.getString("trangThai"),
                            rs.getString("tenLoaiPhong")
                    );
                    result.add(phong);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkInFromBooking(String maDatPhong, String maPhong, LocalDateTime thoiGianCheckIn, LocalDateTime thoiGianCheckOutDuKien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        Integer roomId = parseIntOrNull(maPhong);
        if (con == null || bookingId == null || roomId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng hoặc mã phòng không hợp lệ.");
            return false;
        }

        String findDetailSql = "SELECT TOP 1 ctdp.maChiTietDatPhong, ctdp.soNguoi, ctdp.giaPhong, dp.tienCoc "
                + "FROM ChiTietDatPhong ctdp "
                + "JOIN DatPhong dp ON ctdp.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN LuuTru lt ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong "
                + "WHERE ctdp.maDatPhong = ? AND lt.maChiTietDatPhong IS NULL "
                + "ORDER BY ctdp.maChiTietDatPhong ASC";

        try {
            con.setAutoCommit(false);

            int maChiTietDatPhong;
            int soNguoi;
            double giaPhong;
            double tienCoc;

            try (PreparedStatement stmt = con.prepareStatement(findDetailSql)) {
                stmt.setInt(1, bookingId.intValue());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        setLastError("Booking không còn dòng chi tiết nào sẵn sàng check-in.");
                        return false;
                    }
                    maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                    soNguoi = rs.getInt("soNguoi");
                    giaPhong = rs.getDouble("giaPhong");
                    tienCoc = rs.getDouble("tienCoc");
                }
            }

            try (PreparedStatement stmt = con.prepareStatement("UPDATE ChiTietDatPhong SET maPhong = ? WHERE maChiTietDatPhong = ?")) {
                stmt.setInt(1, roomId.intValue());
                stmt.setInt(2, maChiTietDatPhong);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, maChiTietDatPhong);
                stmt.setInt(2, bookingId.intValue());
                stmt.setInt(3, roomId.intValue());
                stmt.setTimestamp(4, toTimestamp(thoiGianCheckIn));
                stmt.setTimestamp(5, toTimestamp(thoiGianCheckOutDuKien));
                stmt.setInt(6, soNguoi);
                stmt.setDouble(7, giaPhong);
                stmt.setDouble(8, tienCoc);
                stmt.executeUpdate();
            }

            updateBookingStatus(con, bookingId, "Đang lưu trú");
            synchronizeOperationalStatuses(con);
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

    public boolean checkOut(String maLuuTru, LocalDateTime thoiGianCheckOutThucTe) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer stayId = parseIntOrNull(maLuuTru);
        if (con == null || stayId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã lưu trú không hợp lệ.");
            return false;
        }

        LuuTru current = findById(maLuuTru);
        if (current == null) {
            setLastError("Không tìm thấy hồ sơ lưu trú.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement("UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ?")) {
                stmt.setTimestamp(1, toTimestamp(thoiGianCheckOutThucTe));
                stmt.setInt(2, stayId.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("Không thể cập nhật thời gian check-out.");
                    return false;
                }
            }

            updateBookingStatus(con, parseIntOrNull(current.getMaDatPhong()), "Đã check-out");
            updateCustomerStatusByBooking(con, parseIntOrNull(current.getMaDatPhong()), "Ngừng giao dịch");
            synchronizeOperationalStatuses(con);
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

    private void synchronizeOperationalStatuses(Connection con) throws SQLException {
        if (con == null) {
            return;
        }
        if (useDirectActiveStatusAfterCheckout()) {
            synchronizeOperationalStatusesWithoutCleaning(con);
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE Phong SET trangThai = N'Hoạt động' WHERE trangThai IN (N'Hoạt động', N'Trống', N'Đã đặt', N'Đang ở')")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE p SET p.trangThai = N'Đã đặt' FROM Phong p WHERE EXISTS (" +
                        "SELECT 1 FROM ChiTietDatPhong ctdp JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                        "WHERE ctdp.maPhong = p.maPhong AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in'))")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE p SET p.trangThai = N'Hoạt động' FROM Phong p WHERE EXISTS (" +
                        "SELECT 1 FROM LuuTru lt JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong " +
                        "WHERE lt.maPhong = p.maPhong AND dp.trangThai = N'Đã check-out')")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE p SET p.trangThai = N'Đang ở' FROM Phong p WHERE EXISTS (" +
                        "SELECT 1 FROM LuuTru lt JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong " +
                        "WHERE lt.maPhong = p.maPhong AND dp.trangThai = N'Đang lưu trú')")) {
            ps.executeUpdate();
        }
    }

    private void synchronizeOperationalStatusesWithoutCleaning(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE Phong SET trangThai = N'Ho\u1ea1t \u0111\u1ed9ng' " +
                        "WHERE trangThai IN (N'Ho\u1ea1t \u0111\u1ed9ng', N'Tr\u1ed1ng', N'\u0110\u00e3 \u0111\u1eb7t', N'\u0110ang \u1edf', N'D\u1ecdn d\u1eb9p')")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE p SET p.trangThai = N'\u0110\u00e3 \u0111\u1eb7t' FROM Phong p WHERE EXISTS (" +
                        "SELECT 1 FROM ChiTietDatPhong ctdp JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                        "WHERE ctdp.maPhong = p.maPhong AND dp.trangThai IN (N'\u0110\u00e3 \u0111\u1eb7t', N'\u0110\u00e3 x\u00e1c nh\u1eadn', N'\u0110\u00e3 c\u1ecdc', N'Ch\u1edd check-in'))")) {
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE p SET p.trangThai = N'\u0110ang \u1edf' FROM Phong p WHERE EXISTS (" +
                        "SELECT 1 FROM LuuTru lt JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong " +
                        "WHERE lt.maPhong = p.maPhong AND dp.trangThai = N'\u0110ang l\u01b0u tr\u00fa')")) {
            ps.executeUpdate();
        }
    }

    private boolean useDirectActiveStatusAfterCheckout() {
        return true;
    }

    private void fillStatement(PreparedStatement stmt, LuuTru luuTru) throws SQLException {
        setNullableInt(stmt, 1, luuTru.getMaChiTietDatPhong());
        setNullableInt(stmt, 2, luuTru.getMaDatPhong());
        setNullableInt(stmt, 3, luuTru.getMaPhong());
        stmt.setTimestamp(4, toTimestamp(luuTru.getCheckIn()));
        stmt.setTimestamp(5, toTimestamp(luuTru.getCheckOut()));
        stmt.setInt(6, luuTru.getSoNguoi());
        stmt.setDouble(7, luuTru.getGiaPhong());
        stmt.setDouble(8, luuTru.getTienCoc());
    }

    private LuuTru mapLuuTru(ResultSet rs) throws SQLException {
        LuuTru luuTru = new LuuTru();
        luuTru.setMaLuuTru(String.valueOf(rs.getInt("maLuuTru")));
        luuTru.setMaChiTietDatPhong(rs.getObject("maChiTietDatPhong") == null ? "" : String.valueOf(rs.getInt("maChiTietDatPhong")));
        luuTru.setMaDatPhong(rs.getObject("maDatPhong") == null ? "" : String.valueOf(rs.getInt("maDatPhong")));
        luuTru.setMaPhong(rs.getObject("maPhong") == null ? "" : String.valueOf(rs.getInt("maPhong")));
        luuTru.setCheckIn(toLocalDateTime(rs.getTimestamp("checkIn")));
        luuTru.setCheckOut(toLocalDateTime(rs.getTimestamp("checkOut")));
        luuTru.setSoNguoi(rs.getInt("soNguoi"));
        luuTru.setGiaPhong(rs.getDouble("giaPhong"));
        luuTru.setTienCoc(rs.getDouble("tienCoc"));
        luuTru.setTrangThaiDatPhong(safeTrim(rs.getString("trangThaiDatPhong")));
        luuTru.setTrangThai(resolveStayStatus(luuTru.getTrangThaiDatPhong(), luuTru.getCheckIn(), luuTru.getCheckOut()));
        luuTru.setTenKhachHang(safeTrim(rs.getString("tenKhachHang")));
        luuTru.setSoDienThoaiKhach(safeTrim(rs.getString("soDienThoaiKhach")));
        luuTru.setSoPhong(safeTrim(rs.getString("soPhong")));
        luuTru.setTang(safeTrim(rs.getString("tang")));
        luuTru.setTenLoaiPhong(safeTrim(rs.getString("tenLoaiPhong")));
        return luuTru;
    }

    private void updateRoomStatus(Connection con, Integer maPhong, String trangThai) throws SQLException {
        if (maPhong == null) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement("UPDATE Phong SET trangThai = ? WHERE maPhong = ?")) {
            stmt.setString(1, trangThai);
            stmt.setInt(2, maPhong.intValue());
            stmt.executeUpdate();
        }
    }

    private void updateBookingStatus(Connection con, Integer maDatPhong, String trangThai) throws SQLException {
        if (maDatPhong == null) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement("UPDATE DatPhong SET trangThai = ? WHERE maDatPhong = ?")) {
            stmt.setString(1, trangThai);
            stmt.setInt(2, maDatPhong.intValue());
            stmt.executeUpdate();
        }
    }

    private void updateCustomerStatusByBooking(Connection con, Integer maDatPhong, String trangThai) throws SQLException {
        if (maDatPhong == null || trangThai == null || trangThai.trim().isEmpty()) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "UPDATE KhachHang SET trangThai = ? WHERE maKhachHang = (" +
                        "SELECT TOP 1 maKhachHang FROM DatPhong WHERE maDatPhong = ?)")) {
            stmt.setString(1, trangThai);
            stmt.setInt(2, maDatPhong.intValue());
            stmt.executeUpdate();
        }
    }

    private boolean shouldMoveToCleaning(LuuTru luuTru) {
        String status = safeTrim(luuTru.getTrangThai());
        return "Đã check-out".equalsIgnoreCase(status) || "Check-out".equalsIgnoreCase(status);
    }

    private String resolveStayStatus(String trangThaiDatPhong, LocalDateTime checkIn, LocalDateTime checkOut) {
        String bookingStatus = safeTrim(trangThaiDatPhong);
        if (!bookingStatus.isEmpty()) {
            if ("Đang lưu trú".equalsIgnoreCase(bookingStatus)) {
                return "Đang ở";
            }
            if ("Chờ check-in".equalsIgnoreCase(bookingStatus) || "Đã xác nhận".equalsIgnoreCase(bookingStatus)) {
                return "Chờ check-in";
            }
            if ("Đã check-out".equalsIgnoreCase(bookingStatus)) {
                return "Đã check-out";
            }
            return bookingStatus;
        }
        if (checkIn == null) {
            return "Chờ check-in";
        }
        if (checkOut == null) {
            return "Đang ở";
        }
        return "Đã check-out";
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

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
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

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
