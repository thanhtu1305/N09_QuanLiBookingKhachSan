package dao;

import db.ConnectDB;
import entity.KhachHang;
import entity.LuuTru;
import entity.Phong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckInOutDAO {
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
    private static final String STATUS_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String STATUS_CONFIRMED = "\u0110\u00e3 x\u00e1c nh\u1eadn";
    private static final String STATUS_DEPOSITED = "\u0110\u00e3 c\u1ecdc";
    private static final String STATUS_PENDING_CHECKIN = "Ch\u1edd check-in";
    private static final String STATUS_ACTIVE = "\u0110ang \u1edf";
    private static final String STATUS_ACTIVE_STAY = "\u0110ang l\u01b0u tr\u00fa";
    private static final String STATUS_PARTIAL_CHECKOUT = "Check-out m\u1ed9t ph\u1ea7n";
    private static final String STATUS_WAIT_PAYMENT = "Ch\u1edd thanh to\u00e1n";
    private static final String STATUS_PAID = "\u0110\u00e3 thanh to\u00e1n";
    private static final String STATUS_CHECKED_IN = "\u0110\u00e3 check-in";
    private static final String STATUS_CHECKED_OUT = "\u0110\u00e3 check-out";
    private static final String STATUS_CANCELLED = "\u0110\u00e3 h\u1ee7y";
    private static final String STATUS_CANCELLED_BOOKING = "H\u1ee7y booking";
    private static final String STATUS_ROOM_ACTIVE = "Ho\u1ea1t \u0111\u1ed9ng";
    private static final String STATUS_ROOM_EMPTY = "Tr\u1ed1ng";
    private static final String STATUS_ROOM_READY = "S\u1eb5n s\u00e0ng";
    private static final String STATUS_ROOM_OCCUPIED = "\u0110ang \u1edf";
    private static final LocalTime LEGACY_BOOKING_TIME_BOUNDARY = LocalTime.of(12, 0);
    private static boolean representativeGuestSchemaEnsured = false;

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public String resolveBookingStatusForBooking(Connection con, int maDatPhong) throws SQLException {
        return resolveBookingStatus(con, maDatPhong, null);
    }

    public void refreshBookingStatus(Connection con, int maDatPhong) throws SQLException {
        if (con == null || maDatPhong <= 0) {
            return;
        }
        updateBookingStatus(con, Integer.valueOf(maDatPhong), resolveBookingStatusForBooking(con, maDatPhong));
    }

    public List<LuuTru> getAll() {
        clearLastError();
        List<LuuTru> result = new ArrayList<LuuTru>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u.");
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
            setLastError("KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u.");
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
            setLastError(con == null ? "KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "MĂ£ lÆ°u trĂº khĂ´ng há»£p lá»‡.");
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
            setLastError(con == null ? "KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "Dá»¯ liá»‡u lÆ°u trĂº khĂ´ng há»£p lá»‡.");
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
                    setLastError("KhĂ´ng thá»ƒ thĂªm lÆ°u trĂº.");
                    return false;
                }
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        luuTru.setMaLuuTru(String.valueOf(rs.getInt(1)));
                    }
                }
            }

            updateBookingStatus(con, parseIntOrNull(luuTru.getMaDatPhong()), shouldMoveToCleaning(luuTru) ? STATUS_CHECKED_OUT : STATUS_ACTIVE);
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
            setLastError(con == null ? "KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "MĂ£ lÆ°u trĂº khĂ´ng há»£p lá»‡.");
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
                    setLastError("KhĂ´ng tĂ¬m tháº¥y lÆ°u trĂº Ä‘á»ƒ cáº­p nháº­t.");
                    return false;
                }
            }

            updateBookingStatus(con, parseIntOrNull(luuTru.getMaDatPhong()), shouldMoveToCleaning(luuTru) ? STATUS_CHECKED_OUT : STATUS_ACTIVE);
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
            setLastError(con == null ? "KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "MĂ£ lÆ°u trĂº khĂ´ng há»£p lá»‡.");
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
                    setLastError("KhĂ´ng tĂ¬m tháº¥y lÆ°u trĂº Ä‘á»ƒ xĂ³a.");
                    return false;
                }
            }

            updateBookingStatus(con, parseIntOrNull(existing.getMaDatPhong()), "ÄĂ£ xĂ¡c nháº­n");
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
            setLastError("KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u.");
            return result;
        }

        String sql = "SELECT p.maPhong, p.maLoaiPhong, p.soPhong, p.tang, p.khuVuc, p.sucChuaChuan, p.sucChuaToiDa, p.trangThai, lp.tenLoaiPhong "
                + "FROM Phong p "
                + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE p.trangThai = N'Trá»‘ng' AND (? = '' OR CAST(p.maLoaiPhong AS NVARCHAR(20)) = ?) "
                + "ORDER BY TRY_CAST(REPLACE(p.tang, N'Táº§ng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong ASC";
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

    public List<RoomChangeCandidate> getAvailableRoomsForRoomChange(int maDatPhong,
                                                                    int maChiTietDatPhong,
                                                                    int maLuuTru,
                                                                    int maPhongHienTai,
                                                                    int maLoaiPhongUuTien,
                                                                    LocalDateTime thoiDiemDoi,
                                                                    LocalDateTime thoiGianTraDuKien) {
        clearLastError();
        List<RoomChangeCandidate> result = new ArrayList<RoomChangeCandidate>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Kh\u00f4ng th\u1ec3 k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u.");
            return result;
        }
        try {
            return getAvailableRoomsForRoomChange(
                    con,
                    maDatPhong,
                    maChiTietDatPhong,
                    maLuuTru,
                    maPhongHienTai,
                    maLoaiPhongUuTien,
                    thoiDiemDoi,
                    thoiGianTraDuKien
            );
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return result;
        }
    }

    public List<RoomChangeCandidate> getAvailableRoomsForRoomChange(Connection con,
                                                                    int maDatPhong,
                                                                    int maChiTietDatPhong,
                                                                    int maLuuTru,
                                                                    int maPhongHienTai,
                                                                    int maLoaiPhongUuTien,
                                                                    LocalDateTime thoiDiemDoi,
                                                                    LocalDateTime thoiGianTraDuKien) throws SQLException {
        clearLastError();
        List<RoomChangeCandidate> result = new ArrayList<RoomChangeCandidate>();
        if (con == null
                || maDatPhong <= 0
                || maChiTietDatPhong <= 0
                || maLuuTru <= 0
                || maPhongHienTai <= 0
                || thoiDiemDoi == null
                || thoiGianTraDuKien == null
                || !thoiGianTraDuKien.isAfter(thoiDiemDoi)) {
            return result;
        }

        // DatPhong stores DATE only, so legacy midnight values are treated as the system's noon boundary.
        LocalDateTime normalizedExpectedCheckOut = normalizeLegacyBookingBoundary(thoiGianTraDuKien);
        if (!normalizedExpectedCheckOut.isAfter(thoiDiemDoi)) {
            return result;
        }

        Timestamp changeTimestamp = Timestamp.valueOf(thoiDiemDoi);
        Timestamp expectedCheckOutTimestamp = Timestamp.valueOf(normalizedExpectedCheckOut);

        String sql = "SELECT p.maPhong, p.soPhong, ISNULL(p.tang, N'-') AS tang, ISNULL(p.khuVuc, N'-') AS khuVuc, "
                + "ISNULL(p.sucChuaToiDa, 0) AS sucChuaToiDa, lp.maLoaiPhong, "
                + "COALESCE(lp.tenLoaiPhong, N'-') AS tenLoaiPhong, ISNULL(lp.giaThamChieu, 0) AS giaThamChieu "
                + "FROM dbo.Phong p "
                + "JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "WHERE p.maPhong <> ? "
                + "AND ISNULL(p.trangThai, N'') IN (N'" + STATUS_ROOM_ACTIVE + "', N'" + STATUS_ROOM_EMPTY + "', N'" + STATUS_ROOM_READY + "') "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM dbo.ChiTietDatPhong ctdpSameBooking "
                + "    WHERE ctdpSameBooking.maDatPhong = ? "
                + "      AND ctdpSameBooking.maPhong = p.maPhong "
                + "      AND ctdpSameBooking.maChiTietDatPhong <> ? "
                + ") "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM dbo.LuuTru ltActive "
                + "    WHERE ltActive.maPhong = p.maPhong "
                + "      AND ltActive.checkOut IS NULL "
                + "      AND ltActive.maLuuTru <> ? "
                + ") "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM dbo.ChiTietDatPhong ctdpHold "
                + "    JOIN dbo.DatPhong dpHold ON dpHold.maDatPhong = ctdpHold.maDatPhong "
                + "    WHERE ctdpHold.maPhong = p.maPhong "
                + "      AND ctdpHold.maDatPhong <> ? "
                + "      AND ISNULL(dpHold.trangThai, N'') IN ("
                + "N'" + STATUS_BOOKED + "', "
                + "N'" + STATUS_CONFIRMED + "', "
                + "N'" + STATUS_DEPOSITED + "', "
                + "N'" + STATUS_PENDING_CHECKIN + "', "
                + "N'" + STATUS_ACTIVE + "', "
                + "N'" + STATUS_ACTIVE_STAY + "', "
                + "N'" + STATUS_PARTIAL_CHECKOUT + "', "
                + "N'" + STATUS_CHECKED_IN + "') "
                + "      AND DATEADD(HOUR, " + LEGACY_BOOKING_TIME_BOUNDARY.getHour() + ", CAST(dpHold.ngayNhanPhong AS DATETIME2)) < ? "
                + "      AND DATEADD(HOUR, " + LEGACY_BOOKING_TIME_BOUNDARY.getHour() + ", CAST(dpHold.ngayTraPhong AS DATETIME2)) > ? "
                + "      AND NOT EXISTS ( "
                + "          SELECT 1 "
                + "          FROM dbo.LuuTru ltBooked "
                + "          WHERE ltBooked.maChiTietDatPhong = ctdpHold.maChiTietDatPhong "
                + "      ) "
                + ") "
                + "ORDER BY CASE WHEN lp.maLoaiPhong = ? THEN 0 ELSE 1 END, "
                + "CASE WHEN TRY_CAST(REPLACE(p.tang, N'T\u1ea7ng ', '') AS INT) IS NULL THEN 1 ELSE 0 END, "
                + "TRY_CAST(REPLACE(p.tang, N'T\u1ea7ng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setInt(index++, maPhongHienTai);
            stmt.setInt(index++, maDatPhong);
            stmt.setInt(index++, maChiTietDatPhong);
            stmt.setInt(index++, maLuuTru);
            stmt.setInt(index++, maDatPhong);
            stmt.setTimestamp(index++, expectedCheckOutTimestamp);
            stmt.setTimestamp(index++, changeTimestamp);
            stmt.setInt(index, maLoaiPhongUuTien);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RoomChangeCandidate item = new RoomChangeCandidate();
                    item.setMaPhong(rs.getInt("maPhong"));
                    item.setMaLoaiPhong(rs.getInt("maLoaiPhong"));
                    item.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
                    item.setSoPhong(safeTrim(rs.getString("soPhong")));
                    item.setTang(safeTrim(rs.getString("tang")));
                    item.setKhuVuc(safeTrim(rs.getString("khuVuc")));
                    item.setTenLoaiPhong(safeTrim(rs.getString("tenLoaiPhong")));
                    item.setGiaThamChieu(rs.getDouble("giaThamChieu"));
                    result.add(item);
                }
            }
        }
        return result;
    }

    private LocalDateTime normalizeLegacyBookingBoundary(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        if (value.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return LocalDateTime.of(value.toLocalDate(), LEGACY_BOOKING_TIME_BOUNDARY);
        }
        return value;
    }

    public List<CheckInBookingItem> getBookingCheckInItems(String maDatPhong) {
        clearLastError();
        List<CheckInBookingItem> items = new ArrayList<CheckInBookingItem>();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        if (con == null || bookingId == null) {
            setLastError(con == null ? "KhÄ‚Â´ng thĂ¡Â»Æ’ kĂ¡ÂºÂ¿t nĂ¡Â»â€˜i cĂ†Â¡ sĂ¡Â»Å¸ dĂ¡Â»Â¯ liĂ¡Â»â€¡u." : "MÄ‚Â£ Ă„â€˜Ă¡ÂºÂ·t phÄ‚Â²ng khÄ‚Â´ng hĂ¡Â»Â£p lĂ¡Â»â€¡.");
            return items;
        }

        ensureRepresentativeGuestSchema(con);
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, dp.tienCoc, " +
                "ISNULL(p.soPhong, N'ChĂ†Â°a gÄ‚Â¡n') AS soPhong, " +
                "COALESCE(lp.tenLoaiPhong, lp2.tenLoaiPhong, N'-') AS tenLoaiPhong, " +
                "dp.trangThai AS trangThaiDatPhong, latestLt.maLuuTru AS maLuuTruGanNhat, latestLt.checkOut AS checkOutGanNhat, " +
                "roomGuest.maKhachHang AS maKhachHangDaiDien, " +
                "ISNULL(roomKh.cccdPassport, N'') AS cccdPassportDaiDien, " +
                "ISNULL(roomKh.hoTen, N'') AS hoTenDaiDien, " +
                "ISNULL(roomKh.soDienThoai, N'') AS soDienThoaiDaiDien, " +
                "roomKh.ngaySinh AS ngaySinhDaiDien, " +
                "ISNULL(roomKh.email, N'') AS emailDaiDien, " +
                "ISNULL(roomKh.diaChi, N'') AS diaChiDaiDien, " +
                "ISNULL(roomKh.ghiChu, N'') AS ghiChuDaiDien, " +
                "ISNULL(bookingKh.cccdPassport, N'') AS cccdPassportBooking, " +
                "ISNULL(bookingKh.hoTen, N'') AS hoTenBooking, " +
                "ISNULL(bookingKh.soDienThoai, N'') AS soDienThoaiBooking, " +
                "bookingKh.ngaySinh AS ngaySinhBooking, " +
                "ISNULL(bookingKh.email, N'') AS emailBooking, " +
                "ISNULL(bookingKh.diaChi, N'') AS diaChiBooking, " +
                "ISNULL(bookingKh.ghiChu, N'') AS ghiChuBooking " +
                "FROM ChiTietDatPhong ctdp " +
                "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                "LEFT JOIN KhachHang bookingKh ON bookingKh.maKhachHang = dp.maKhachHang " +
                "LEFT JOIN ChiTietDatPhongKhachDaiDien roomGuest ON roomGuest.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "LEFT JOIN KhachHang roomKh ON roomKh.maKhachHang = roomGuest.maKhachHang " +
                "LEFT JOIN Phong p ON p.maPhong = ctdp.maPhong " +
                "LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong " +
                "LEFT JOIN BangGia bg ON bg.maBangGia = dp.maBangGia " +
                "LEFT JOIN LoaiPhong lp2 ON lp2.maLoaiPhong = bg.maLoaiPhong " +
                "OUTER APPLY (SELECT TOP 1 lt.maLuuTru, lt.checkOut FROM LuuTru lt " +
                "             WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "             ORDER BY CASE WHEN lt.checkOut IS NULL THEN 0 ELSE 1 END, COALESCE(lt.checkOut, lt.checkIn) DESC, lt.maLuuTru DESC) latestLt " +
                "WHERE ctdp.maDatPhong = ? " +
                "ORDER BY CASE WHEN TRY_CAST(p.soPhong AS INT) IS NULL THEN 1 ELSE 0 END, TRY_CAST(p.soPhong AS INT), p.soPhong, ctdp.maChiTietDatPhong";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, bookingId.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CheckInBookingItem item = new CheckInBookingItem();
                    item.setMaChiTietDatPhong(rs.getInt("maChiTietDatPhong"));
                    item.setMaPhong(rs.getObject("maPhong") == null ? 0 : rs.getInt("maPhong"));
                    item.setSoPhong(safeTrim(rs.getString("soPhong")));
                    item.setTenLoaiPhong(safeTrim(rs.getString("tenLoaiPhong")));
                    item.setSoNguoi(rs.getInt("soNguoi"));
                    item.setGiaPhong(rs.getDouble("giaPhong"));
                    item.setTienCoc(rs.getDouble("tienCoc"));
                    Integer latestStayId = rs.getObject("maLuuTruGanNhat") == null ? null : Integer.valueOf(rs.getInt("maLuuTruGanNhat"));
                    item.setTrangThai(resolveCheckInItemStatus(
                            item.getMaPhong(),
                            safeTrim(rs.getString("trangThaiDatPhong")),
                            latestStayId,
                            rs.getTimestamp("checkOutGanNhat")
                    ));
                    fillCheckInItemCustomer(item, rs, latestStayId != null);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public KhachHang findCustomerByCccdPassport(String cccdPassport) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        String value = safeTrim(cccdPassport);
        if (con == null || value.isEmpty()) {
            return null;
        }

        String sql = "SELECT TOP 1 maKhachHang, hoTen, soDienThoai, ngaySinh, email, cccdPassport, diaChi, ghiChu "
                + "FROM KhachHang WHERE cccdPassport = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    KhachHang khachHang = new KhachHang();
                    khachHang.setMaKhachHang(String.valueOf(rs.getInt("maKhachHang")));
                    khachHang.setHoTen(safeTrim(rs.getString("hoTen")));
                    khachHang.setSoDienThoai(safeTrim(rs.getString("soDienThoai")));
                    khachHang.setNgaySinh(rs.getDate("ngaySinh") == null ? "" : rs.getDate("ngaySinh").toLocalDate().toString());
                    khachHang.setEmail(safeTrim(rs.getString("email")));
                    khachHang.setCccdPassport(safeTrim(rs.getString("cccdPassport")));
                    khachHang.setDiaChi(safeTrim(rs.getString("diaChi")));
                    khachHang.setGhiChu(safeTrim(rs.getString("ghiChu")));
                    return khachHang;
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public int checkInBookingDetails(String maDatPhong, List<Integer> maChiTietDatPhongIds, LocalDateTime thoiGianCheckIn, LocalDateTime thoiGianCheckOutDuKien) {
        return checkInBookingDetails(maDatPhong, maChiTietDatPhongIds, thoiGianCheckIn, thoiGianCheckOutDuKien, null);
    }

    public int checkInBookingDetails(String maDatPhong,
                                     List<Integer> maChiTietDatPhongIds,
                                     LocalDateTime thoiGianCheckIn,
                                     LocalDateTime thoiGianCheckOutDuKien,
                                     Map<Integer, KhachHang> customerByDetailId) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        if (con == null || bookingId == null) {
            setLastError(con == null ? "KhÄ‚Â´ng thĂ¡Â»Æ’ kĂ¡ÂºÂ¿t nĂ¡Â»â€˜i cĂ†Â¡ sĂ¡Â»Å¸ dĂ¡Â»Â¯ liĂ¡Â»â€¡u." : "MÄ‚Â£ Ă„â€˜Ă¡ÂºÂ·t phÄ‚Â²ng khÄ‚Â´ng hĂ¡Â»Â£p lĂ¡Â»â€¡.");
            return 0;
        }
        List<Integer> detailIds = sanitizeDetailIds(maChiTietDatPhongIds);
        if (detailIds.isEmpty()) {
            setLastError("KhÄ‚Â´ng cÄ‚Â³ phÄ‚Â²ng nÄ‚Â o Ă„â€˜Ă†Â°Ă¡Â»Â£c chĂ¡Â»Ân Ă„â€˜Ă¡Â»Æ’ check-in.");
            return 0;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ctdp.maChiTietDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, dp.tienCoc ")
                .append("FROM ChiTietDatPhong ctdp ")
                .append("JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong ")
                .append("WHERE ctdp.maDatPhong = ? ")
                .append("AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong) ")
                .append("AND ctdp.maChiTietDatPhong IN (");
        for (int i = 0; i < detailIds.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");

        try {
            con.setAutoCommit(false);
            ensureRepresentativeGuestSchema(con);
            int affected = 0;
            try (PreparedStatement selectStmt = con.prepareStatement(sql.toString());
                 PreparedStatement insertStmt = con.prepareStatement(
                         "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement roomStmt = con.prepareStatement("UPDATE Phong SET trangThai = N'\u0110ang \u1edf' WHERE maPhong = ? AND trangThai <> N'B\u1ea3o tr\u00ec'")) {
                int index = 1;
                selectStmt.setInt(index++, bookingId.intValue());
                for (Integer detailId : detailIds) {
                    selectStmt.setInt(index++, detailId.intValue());
                }
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getObject("maPhong") == null) {
                            continue;
                        }

                        int maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                        int maPhong = rs.getInt("maPhong");
                        insertStmt.setInt(1, maChiTietDatPhong);
                        insertStmt.setInt(2, bookingId.intValue());
                        insertStmt.setInt(3, maPhong);
                        insertStmt.setTimestamp(4, toTimestamp(thoiGianCheckIn));
                        insertStmt.setTimestamp(5, null);
                        insertStmt.setInt(6, rs.getInt("soNguoi"));
                        insertStmt.setDouble(7, rs.getDouble("giaPhong"));
                        insertStmt.setDouble(8, rs.getDouble("tienCoc"));
                        affected += insertStmt.executeUpdate();

                        roomStmt.setInt(1, maPhong);
                        roomStmt.executeUpdate();
                        persistRepresentativeGuest(
                                con,
                                maChiTietDatPhong,
                                customerByDetailId == null ? null : customerByDetailId.get(Integer.valueOf(maChiTietDatPhong))
                        );
                    }
                }
            }

            if (affected <= 0) {
                con.rollback();
                setLastError("KhÄ‚Â´ng cÄ‚Â³ phÄ‚Â²ng nÄ‚Â o sĂ¡ÂºÂµn sÄ‚Â ng Ă„â€˜Ă¡Â»Æ’ check-in.");
                return 0;
            }

            updateBookingExpectedCheckOut(con, bookingId.intValue(), thoiGianCheckOutDuKien);
            refreshBookingStatusAfterCheckIn(con, bookingId.intValue());
            synchronizeOperationalStatuses(con);
            con.commit();
            return affected;
        } catch (Exception e) {
            rollbackQuietly(con);
            setLastError(e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            resetAutoCommit(con);
        }
    }

        public boolean checkInFromBooking(String maDatPhong, String maPhong, LocalDateTime thoiGianCheckIn, LocalDateTime thoiGianCheckOutDuKien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        Integer roomId = parseIntOrNull(maPhong);
        if (con == null || bookingId == null || roomId == null) {
            setLastError(con == null ? "Kh\u00f4ng th\u1ec3 k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u." : "M\u00e3 \u0111\u1eb7t ph\u00f2ng ho\u1eb7c m\u00e3 ph\u00f2ng kh\u00f4ng h\u1ee3p l\u1ec7.");
            return false;
        }

        String findDetailSql = "SELECT TOP 1 ctdp.maChiTietDatPhong "
                + "FROM ChiTietDatPhong ctdp "
                + "WHERE ctdp.maDatPhong = ? AND ctdp.maPhong = ? "
                + "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong) "
                + "ORDER BY ctdp.maChiTietDatPhong ASC";

        try (PreparedStatement stmt = con.prepareStatement(findDetailSql)) {
            stmt.setInt(1, bookingId.intValue());
            stmt.setInt(2, roomId.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    setLastError("Ph\u00f2ng \u0111\u01b0\u1ee3c ch\u1ecdn kh\u00f4ng c\u00f2n s\u1eb5n s\u00e0ng check-in.");
                    return false;
                }
                List<Integer> detailIds = new ArrayList<Integer>();
                detailIds.add(Integer.valueOf(rs.getInt("maChiTietDatPhong")));
                return checkInBookingDetails(maDatPhong, detailIds, thoiGianCheckIn, thoiGianCheckOutDuKien) > 0;
            }
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOut(String maLuuTru, LocalDateTime thoiGianCheckOutThucTe) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer stayId = parseIntOrNull(maLuuTru);
        if (con == null || stayId == null) {
            setLastError(con == null ? "KhĂ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "MĂ£ lÆ°u trĂº khĂ´ng há»£p lá»‡.");
            return false;
        }

        LuuTru current = findById(maLuuTru);
        if (current == null) {
            setLastError("KhĂ´ng tĂ¬m tháº¥y há»“ sÆ¡ lÆ°u trĂº.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement("UPDATE LuuTru SET checkOut = ? WHERE maLuuTru = ?")) {
                stmt.setTimestamp(1, toTimestamp(thoiGianCheckOutThucTe));
                stmt.setInt(2, stayId.intValue());
                if (stmt.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("KhĂ´ng thá»ƒ cáº­p nháº­t thá»i gian check-out.");
                    return false;
                }
            }

            refreshBookingStatusAfterCheckout(con, parseIntOrNull(current.getMaDatPhong()));
            Integer roomId = parseIntOrNull(current.getMaPhong());
            if (roomId != null) {
                new DatPhongDAO().refreshRoomStatus(con, roomId.intValue());
            }
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
        refreshAllRoomStatuses(con);
    }

    private void synchronizeOperationalStatusesWithoutCleaning(Connection con) throws SQLException {
        refreshAllRoomStatuses(con);
    }

    private void refreshAllRoomStatuses(Connection con) throws SQLException {
        List<Integer> roomIds = new ArrayList<Integer>();
        try (PreparedStatement ps = con.prepareStatement("SELECT maPhong FROM Phong");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomIds.add(Integer.valueOf(rs.getInt("maPhong")));
            }
        }
        new DatPhongDAO().refreshRoomStatuses(con, roomIds);
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
        try (PreparedStatement stmt = con.prepareStatement("UPDATE Phong SET trangThai = ? WHERE maPhong = ? AND trangThai <> N'Bảo trì'")) {
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

    private void refreshBookingStatusAfterCheckIn(Connection con, int maDatPhong) throws SQLException {
        String resolvedStatus = resolveBookingStatus(con, maDatPhong, null);
        updateBookingStatus(con, Integer.valueOf(maDatPhong), resolvedStatus);
    }

    private void refreshBookingStatusAfterCheckout(Connection con, Integer maDatPhong) throws SQLException {
        if (maDatPhong == null) {
            return;
        }
        String resolvedStatus = resolveBookingStatus(con, maDatPhong.intValue(), null);
        updateBookingStatus(con, maDatPhong, resolvedStatus);
    }

    private void updateBookingExpectedCheckOut(Connection con, int maDatPhong, LocalDateTime thoiGianCheckOutDuKien) throws SQLException {
        if (con == null || thoiGianCheckOutDuKien == null) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "UPDATE DatPhong SET ngayTraPhong = ? WHERE maDatPhong = ?")) {
            stmt.setTimestamp(1, Timestamp.valueOf(thoiGianCheckOutDuKien));
            stmt.setInt(2, maDatPhong);
            stmt.executeUpdate();
        }
    }

    private boolean hasActiveStay(Connection con, Integer maDatPhong) throws SQLException {
        if (maDatPhong == null) {
            return false;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT COUNT(1) FROM LuuTru WHERE maDatPhong = ? AND checkOut IS NULL")) {
            stmt.setInt(1, maDatPhong.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean hasPendingCheckInDetails(Connection con, int maDatPhong) throws SQLException {
        String sql = "SELECT COUNT(1) "
                + "FROM ChiTietDatPhong ctdp "
                + "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                + "WHERE ctdp.maDatPhong = ? "
                + "AND ISNULL(dp.trangThai, N'') IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in', N'Đang ở', N'Đã check-in') "
                + "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String resolveBookingStatus(Connection con, int maDatPhong, String currentStatus) throws SQLException {
        String status = safeTrim(currentStatus);
        if (STATUS_CANCELLED.equalsIgnoreCase(status) || STATUS_CANCELLED_BOOKING.equalsIgnoreCase(status)) {
            return STATUS_CANCELLED;
        }
        if (isBookingPaid(con, maDatPhong)) {
            return STATUS_PAID;
        }

        String sql = "SELECT COUNT(1) AS soChiTiet, "
                + "SUM(CASE WHEN stayStats.coDangO = 1 THEN 1 ELSE 0 END) AS soChiTietDangO, "
                + "SUM(CASE WHEN stayStats.coDaCheckOut = 1 THEN 1 ELSE 0 END) AS soChiTietDaCheckOut, "
                + "SUM(CASE WHEN stayStats.coLuuTru = 1 THEN 1 ELSE 0 END) AS soChiTietDaPhatSinhLuuTru "
                + "FROM ChiTietDatPhong ctdp "
                + "OUTER APPLY ( "
                + "    SELECT CASE WHEN EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong AND lt.checkOut IS NULL) THEN 1 ELSE 0 END AS coDangO, "
                + "           CASE WHEN EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong AND lt.checkOut IS NOT NULL) THEN 1 ELSE 0 END AS coDaCheckOut, "
                + "           CASE WHEN EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong) THEN 1 ELSE 0 END AS coLuuTru "
                + ") stayStats "
                + "WHERE ctdp.maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return status.isEmpty() ? STATUS_PENDING_CHECKIN : status;
                }
                int soChiTiet = rs.getInt("soChiTiet");
                int soChiTietDangO = rs.getInt("soChiTietDangO");
                int soChiTietDaCheckOut = rs.getInt("soChiTietDaCheckOut");
                int soChiTietDaPhatSinhLuuTru = rs.getInt("soChiTietDaPhatSinhLuuTru");

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
        }
        return status.isEmpty() ? STATUS_PENDING_CHECKIN : status;
    }

    private boolean isBookingPaid(Connection con, int maDatPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT COUNT(1) FROM HoaDon WHERE maDatPhong = ? AND ISNULL(trangThai, N'') = N'Đã thanh toán'")) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean shouldMoveToCleaning(LuuTru luuTru) {
        String status = safeTrim(luuTru.getTrangThai());
        return STATUS_CHECKED_OUT.equalsIgnoreCase(status) || "Check-out".equalsIgnoreCase(status);
    }

    private String resolveStayStatus(String trangThaiDatPhong, LocalDateTime checkIn, LocalDateTime checkOut) {
        String bookingStatus = safeTrim(trangThaiDatPhong);
        if (!bookingStatus.isEmpty()) {
            if (STATUS_ACTIVE.equalsIgnoreCase(bookingStatus)
                    || STATUS_PARTIAL_CHECKOUT.equalsIgnoreCase(bookingStatus)
                    || STATUS_CHECKED_IN.equalsIgnoreCase(bookingStatus)) {
                return STATUS_ROOM_OCCUPIED;
            }
            if (STATUS_PENDING_CHECKIN.equalsIgnoreCase(bookingStatus)
                    || STATUS_CONFIRMED.equalsIgnoreCase(bookingStatus)
                    || STATUS_BOOKED.equalsIgnoreCase(bookingStatus)
                    || STATUS_DEPOSITED.equalsIgnoreCase(bookingStatus)) {
                return STATUS_PENDING_CHECKIN;
            }
            if (STATUS_CHECKED_OUT.equalsIgnoreCase(bookingStatus)
                    || STATUS_WAIT_PAYMENT.equalsIgnoreCase(bookingStatus)
                    || STATUS_PAID.equalsIgnoreCase(bookingStatus)) {
                return STATUS_CHECKED_OUT;
            }
            return bookingStatus;
        }
        if (checkIn == null) {
            return STATUS_PENDING_CHECKIN;
        }
        if (checkOut == null) {
            return STATUS_ROOM_OCCUPIED;
        }
        return STATUS_CHECKED_OUT;
    }

    private String resolveCheckInItemStatus(int maPhong, String bookingStatus, Integer latestStayId, Timestamp latestCheckOut) {
        if (STATUS_CANCELLED.equalsIgnoreCase(bookingStatus) || STATUS_CANCELLED_BOOKING.equalsIgnoreCase(bookingStatus)) {
            return STATUS_CANCELLED;
        }
        if (maPhong <= 0) {
            return "Chưa gán phòng";
        }
        if (latestStayId != null) {
            return latestCheckOut == null ? STATUS_CHECKED_IN : STATUS_CHECKED_OUT;
        }
        return STATUS_PENDING_CHECKIN;
    }

    private void fillCheckInItemCustomer(CheckInBookingItem item, ResultSet rs, boolean hasExistingStay) throws SQLException {
        if (item == null || rs == null) {
            return;
        }
        if (rs.getObject("maKhachHangDaiDien") != null) {
            item.setCccdPassport(safeTrim(rs.getString("cccdPassportDaiDien")));
            item.setHoTenKhach(safeTrim(rs.getString("hoTenDaiDien")));
            item.setSoDienThoai(safeTrim(rs.getString("soDienThoaiDaiDien")));
            item.setNgaySinh(formatDisplayDate(rs.getDate("ngaySinhDaiDien")));
            item.setEmail(safeTrim(rs.getString("emailDaiDien")));
            item.setDiaChi(safeTrim(rs.getString("diaChiDaiDien")));
            item.setGhiChu(safeTrim(rs.getString("ghiChuDaiDien")));
            return;
        }
        if (!hasExistingStay) {
            return;
        }
        item.setCccdPassport(safeTrim(rs.getString("cccdPassportBooking")));
        item.setHoTenKhach(safeTrim(rs.getString("hoTenBooking")));
        item.setSoDienThoai(safeTrim(rs.getString("soDienThoaiBooking")));
        item.setNgaySinh(formatDisplayDate(rs.getDate("ngaySinhBooking")));
        item.setEmail(safeTrim(rs.getString("emailBooking")));
        item.setDiaChi(safeTrim(rs.getString("diaChiBooking")));
        item.setGhiChu(safeTrim(rs.getString("ghiChuBooking")));
    }

    private String formatDisplayDate(Date value) {
        return value == null ? "" : DISPLAY_DATE_FORMAT.format(value.toLocalDate());
    }

    private void ensureRepresentativeGuestSchema(Connection con) {
        if (con == null || representativeGuestSchemaEnsured) {
            return;
        }
        synchronized (CheckInOutDAO.class) {
            if (representativeGuestSchemaEnsured) {
                return;
            }
            try (Statement stmt = con.createStatement()) {
                stmt.execute(
                        "IF OBJECT_ID(N'dbo.ChiTietDatPhongKhachDaiDien', N'U') IS NULL " +
                                "BEGIN " +
                                "CREATE TABLE ChiTietDatPhongKhachDaiDien(" +
                                "maChiTietDatPhong INT NOT NULL PRIMARY KEY, " +
                                "maKhachHang INT NOT NULL, " +
                                "ngayTao DATETIME NOT NULL CONSTRAINT DF_ChiTietDatPhongKhachDaiDien_ngayTao DEFAULT GETDATE()" +
                                ") " +
                                "END"
                );
            } catch (SQLException ex) {
                setLastError(ex.getMessage());
                return;
            }
            representativeGuestSchemaEnsured = true;
        }
    }

    private void persistRepresentativeGuest(Connection con, int maChiTietDatPhong, KhachHang khachHang) throws SQLException {
        if (con == null || maChiTietDatPhong <= 0 || khachHang == null) {
            return;
        }
        Integer maKhachHang = upsertRepresentativeCustomer(con, khachHang);
        if (maKhachHang == null || maKhachHang.intValue() <= 0) {
            return;
        }
        try (PreparedStatement update = con.prepareStatement(
                "UPDATE ChiTietDatPhongKhachDaiDien SET maKhachHang = ? WHERE maChiTietDatPhong = ?")) {
            update.setInt(1, maKhachHang.intValue());
            update.setInt(2, maChiTietDatPhong);
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = con.prepareStatement(
                "INSERT INTO ChiTietDatPhongKhachDaiDien(maChiTietDatPhong, maKhachHang) VALUES (?, ?)")) {
            insert.setInt(1, maChiTietDatPhong);
            insert.setInt(2, maKhachHang.intValue());
            insert.executeUpdate();
        }
    }

    private Integer upsertRepresentativeCustomer(Connection con, KhachHang khachHang) throws SQLException {
        if (con == null || khachHang == null) {
            return null;
        }
        String cccdPassport = safeTrim(khachHang.getCccdPassport());
        String hoTen = safeTrim(khachHang.getHoTen());
        if (cccdPassport.isEmpty() || hoTen.isEmpty()) {
            return null;
        }

        Integer existingId = findCustomerIdByPassport(con, cccdPassport);
        if (existingId == null) {
            existingId = findCustomerIdByPhone(con, khachHang.getSoDienThoai());
        }
        if (existingId != null) {
            try (PreparedStatement update = con.prepareStatement(
                    "UPDATE KhachHang SET hoTen = ?, cccdPassport = ?, " +
                            "soDienThoai = CASE WHEN ? = '' THEN soDienThoai ELSE ? END, " +
                            "ngaySinh = COALESCE(?, ngaySinh), " +
                            "email = CASE WHEN ? = '' THEN email ELSE ? END, " +
                            "diaChi = CASE WHEN ? = '' THEN diaChi ELSE ? END, " +
                            "ghiChu = CASE WHEN ? = '' THEN ghiChu ELSE ? END " +
                            "WHERE maKhachHang = ?")) {
                String soDienThoai = safeTrim(khachHang.getSoDienThoai());
                String email = safeTrim(khachHang.getEmail());
                String diaChi = safeTrim(khachHang.getDiaChi());
                String ghiChu = safeTrim(khachHang.getGhiChu());
                update.setString(1, hoTen);
                update.setString(2, cccdPassport);
                update.setString(3, soDienThoai);
                update.setString(4, soDienThoai);
                setNullableDateFromText(update, 5, khachHang.getNgaySinh());
                update.setString(6, email);
                update.setString(7, email);
                update.setString(8, diaChi);
                update.setString(9, diaChi);
                update.setString(10, ghiChu);
                update.setString(11, ghiChu);
                update.setInt(12, existingId.intValue());
                update.executeUpdate();
            }
            return existingId;
        }

        String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) " +
                "VALUES (?, N'Kh\u00e1c', ?, ?, ?, ?, ?, N'Vi\u1ec7t Nam', N'C\u00e1 nh\u00e2n', N'Th\u01b0\u1eddng', N'Ho\u1ea1t \u0111\u1ed9ng', N'H\u1ec7 th\u1ed1ng', ?)";
        try (PreparedStatement insert = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, hoTen);
            setNullableDateFromText(insert, 2, khachHang.getNgaySinh());
            insert.setString(3, safeTrim(khachHang.getSoDienThoai()));
            insert.setString(4, safeTrim(khachHang.getEmail()));
            insert.setString(5, cccdPassport);
            insert.setString(6, safeTrim(khachHang.getDiaChi()));
            insert.setString(7, nullIfEmpty(khachHang.getGhiChu()) == null ? "T\u1ea1o t\u1eeb m\u00e0n h\u00ecnh check-in" : safeTrim(khachHang.getGhiChu()));
            insert.executeUpdate();
            try (ResultSet rs = insert.getGeneratedKeys()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private Integer findCustomerIdByPassport(Connection con, String cccdPassport) throws SQLException {
        String value = safeTrim(cccdPassport);
        if (con == null || value.isEmpty()) {
            return null;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT TOP 1 maKhachHang FROM KhachHang WHERE cccdPassport = ?")) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private Integer findCustomerIdByPhone(Connection con, String soDienThoai) throws SQLException {
        String value = safeTrim(soDienThoai);
        if (con == null || value.isEmpty()) {
            return null;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT TOP 1 maKhachHang FROM KhachHang WHERE soDienThoai = ?")) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private void setNullableDateFromText(PreparedStatement stmt, int index, String value) throws SQLException {
        LocalDate parsedDate = parseFlexibleDate(value);
        if (parsedDate == null) {
            stmt.setNull(index, java.sql.Types.DATE);
            return;
        }
        stmt.setDate(index, Date.valueOf(parsedDate));
    }

    private LocalDate parseFlexibleDate(String value) {
        String text = safeTrim(value);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text, DISPLAY_DATE_FORMAT);
        } catch (Exception ignore) {
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception ignore) {
            return null;
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

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullIfEmpty(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? null : trimmed;
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

    private List<Integer> sanitizeDetailIds(List<Integer> maChiTietDatPhongIds) {
        List<Integer> detailIds = new ArrayList<Integer>();
        if (maChiTietDatPhongIds == null) {
            return detailIds;
        }
        for (Integer value : maChiTietDatPhongIds) {
            if (value != null && value.intValue() > 0 && !detailIds.contains(value)) {
                detailIds.add(value);
            }
        }
        return detailIds;
    }

    public static final class CheckInBookingItem {
        private int maChiTietDatPhong;
        private int maPhong;
        private String soPhong;
        private String tenLoaiPhong;
        private int soNguoi;
        private double giaPhong;
        private double tienCoc;
        private String trangThai;
        private String cccdPassport;
        private String hoTenKhach;
        private String soDienThoai;
        private String ngaySinh;
        private String email;
        private String diaChi;
        private String ghiChu;

        public int getMaChiTietDatPhong() {
            return maChiTietDatPhong;
        }

        public void setMaChiTietDatPhong(int maChiTietDatPhong) {
            this.maChiTietDatPhong = maChiTietDatPhong;
        }

        public int getMaPhong() {
            return maPhong;
        }

        public void setMaPhong(int maPhong) {
            this.maPhong = maPhong;
        }

        public String getSoPhong() {
            return soPhong;
        }

        public void setSoPhong(String soPhong) {
            this.soPhong = soPhong;
        }

        public String getTenLoaiPhong() {
            return tenLoaiPhong;
        }

        public void setTenLoaiPhong(String tenLoaiPhong) {
            this.tenLoaiPhong = tenLoaiPhong;
        }

        public int getSoNguoi() {
            return soNguoi;
        }

        public void setSoNguoi(int soNguoi) {
            this.soNguoi = soNguoi;
        }

        public double getGiaPhong() {
            return giaPhong;
        }

        public void setGiaPhong(double giaPhong) {
            this.giaPhong = giaPhong;
        }

        public double getTienCoc() {
            return tienCoc;
        }

        public void setTienCoc(double tienCoc) {
            this.tienCoc = tienCoc;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public void setTrangThai(String trangThai) {
            this.trangThai = trangThai;
        }

        public String getCccdPassport() {
            return cccdPassport;
        }

        public void setCccdPassport(String cccdPassport) {
            this.cccdPassport = cccdPassport;
        }

        public String getHoTenKhach() {
            return hoTenKhach;
        }

        public void setHoTenKhach(String hoTenKhach) {
            this.hoTenKhach = hoTenKhach;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }

        public String getNgaySinh() {
            return ngaySinh;
        }

        public void setNgaySinh(String ngaySinh) {
            this.ngaySinh = ngaySinh;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }

        public boolean canCheckIn() {
            return maChiTietDatPhong > 0 && maPhong > 0 && "Ch\u1edd check-in".equalsIgnoreCase(trangThai);
        }
    }

    public static final class RoomChangeCandidate {
        private int maPhong;
        private int maLoaiPhong;
        private int sucChuaToiDa;
        private String soPhong;
        private String tang;
        private String khuVuc;
        private String tenLoaiPhong;
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

        public int getSucChuaToiDa() {
            return sucChuaToiDa;
        }

        public void setSucChuaToiDa(int sucChuaToiDa) {
            this.sucChuaToiDa = sucChuaToiDa;
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

        public String getKhuVuc() {
            return khuVuc;
        }

        public void setKhuVuc(String khuVuc) {
            this.khuVuc = khuVuc;
        }

        public String getTenLoaiPhong() {
            return tenLoaiPhong;
        }

        public void setTenLoaiPhong(String tenLoaiPhong) {
            this.tenLoaiPhong = tenLoaiPhong;
        }

        public double getGiaThamChieu() {
            return giaThamChieu;
        }

        public void setGiaThamChieu(double giaThamChieu) {
            this.giaThamChieu = giaThamChieu;
        }
    }
}



