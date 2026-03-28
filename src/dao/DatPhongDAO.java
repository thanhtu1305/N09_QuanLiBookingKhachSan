package dao;

import db.ConnectDB;
import entity.ChiTietDatPhong;
import entity.DatPhong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DatPhongDAO {
    private static final String SELECT_HEADER_BASE =
            "SELECT dp.maDatPhong, dp.maKhachHang, dp.maNhanVien, dp.maBangGia, dp.ngayDat, dp.ngayNhanPhong, dp.ngayTraPhong, "
                    + "dp.soLuongPhong, dp.soNguoi, dp.tienCoc, dp.trangThai, "
                    + "kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhach, kh.cccdPassport AS cccdPassportKhach "
                    + "FROM DatPhong dp "
                    + "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang";

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<DatPhong> getAll() {
        clearLastError();
        List<DatPhong> result = new ArrayList<DatPhong>();
        Connection con = getReadyConnection();
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
                normalizeBooking(datPhong);
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
        Connection con = getReadyConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return null;
        }

        String sql = SELECT_HEADER_BASE + " WHERE dp.maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DatPhong datPhong = mapDatPhongHeader(rs);
                    datPhong.getChiTietDatPhongs().addAll(getChiTietByMaDatPhongInternal(con, datPhong));
                    normalizeBooking(datPhong);
                    return datPhong;
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<DatPhong> findByTrangThai(String trangThai) {
        clearLastError();
        List<DatPhong> result = new ArrayList<DatPhong>();
        Connection con = getReadyConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String status = safeTrim(trangThai);
        String sql = SELECT_HEADER_BASE + " WHERE (? = '' OR dp.trangThai = ?) ORDER BY dp.maDatPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DatPhong datPhong = mapDatPhongHeader(rs);
                    datPhong.getChiTietDatPhongs().addAll(getChiTietByMaDatPhongInternal(con, datPhong));
                    normalizeBooking(datPhong);
                    result.add(datPhong);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public boolean insert(DatPhong datPhong) {
        clearLastError();
        Connection con = getReadyConnection();
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
        Connection con = getReadyConnection();
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
        Connection con = getReadyConnection();
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
        Connection con = getReadyConnection();
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

        String sql = "UPDATE DatPhong SET trangThai = ? WHERE maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, safeTrim(trangThai));
            stmt.setInt(2, id.intValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private List<ChiTietDatPhong> getChiTietByMaDatPhongInternal(Connection con, DatPhong header) {
        List<ChiTietDatPhong> details = new ArrayList<ChiTietDatPhong>();
        Integer bookingId = parseIntOrNull(header.getMaDatPhong());
        if (con == null || bookingId == null) {
            return details;
        }

        int detailCount = countDetails(con, bookingId.intValue());
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, ctdp.thanhTien, "
                + "dp.ngayNhanPhong, dp.ngayTraPhong, dp.maBangGia, dp.tienCoc AS tienCocHeader, dp.trangThai AS trangThaiDatPhong, "
                + "p.soPhong, p.tang, "
                + "COALESCE(CAST(p.maLoaiPhong AS NVARCHAR(20)), CAST(bg.maLoaiPhong AS NVARCHAR(20))) AS maLoaiPhongResolved, "
                + "COALESCE(lp.tenLoaiPhong, lp2.tenLoaiPhong) AS tenLoaiPhongResolved "
                + "FROM ChiTietDatPhong ctdp "
                + "JOIN DatPhong dp ON ctdp.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong "
                + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong "
                + "LEFT JOIN BangGia bg ON dp.maBangGia = bg.maBangGia "
                + "LEFT JOIN LoaiPhong lp2 ON bg.maLoaiPhong = lp2.maLoaiPhong "
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
                    detail.setMaBangGia(rs.getObject("maBangGia") == null ? "" : String.valueOf(rs.getInt("maBangGia")));
                    detail.setMaChiTietBangGia("");
                    detail.setCheckInDuKien(toLocalDate(rs.getDate("ngayNhanPhong")));
                    detail.setCheckOutDuKien(toLocalDate(rs.getDate("ngayTraPhong")));
                    detail.setSoNguoi(rs.getInt("soNguoi"));
                    detail.setGiaApDung(rs.getDouble("giaPhong"));
                    detail.setTienDatCocChiTiet(detailCount <= 0 ? 0d : rs.getDouble("tienCocHeader") / detailCount);
                    detail.setTrangThaiChiTiet(resolveDetailStatus(rs.getString("trangThaiDatPhong"), rs.getString("soPhong")));
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
        stmt.setString(10, defaultIfEmpty(datPhong.getTrangThaiDatPhong(), "Chờ xác nhận"));
    }

    private void insertChiTietList(Connection con, DatPhong datPhong) throws SQLException {
        List<ChiTietDatPhong> details = datPhong.getChiTietDatPhongs();
        if (details == null || details.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (ChiTietDatPhong detail : details) {
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
        String sql = "UPDATE Phong SET trangThai = ? WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
                Integer roomId = parseIntOrNull(detail.getMaPhong());
                if (roomId == null) {
                    continue;
                }
                stmt.setString(1, roomStatus);
                stmt.setInt(2, roomId.intValue());
                stmt.executeUpdate();
            }
        }
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
        String sql = "UPDATE Phong SET trangThai = N'Trống' WHERE maPhong = ? AND trangThai IN (N'Đã đặt', N'Trống')";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (Integer roomId : roomIds) {
                stmt.setInt(1, roomId.intValue());
                stmt.executeUpdate();
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

    private void normalizeBooking(DatPhong datPhong) {
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

    private String resolveRoomStatusForBooking(String bookingStatus) {
        String status = safeTrim(bookingStatus);
        if ("Đang lưu trú".equalsIgnoreCase(status) || "Đã check-in".equalsIgnoreCase(status)) {
            return "Đang ở";
        }
        if ("Đã hủy".equalsIgnoreCase(status) || "Hủy booking".equalsIgnoreCase(status)) {
            return "Trống";
        }
        return "Đã đặt";
    }

    private String resolveDetailStatus(String bookingStatus, String soPhong) {
        String status = safeTrim(bookingStatus);
        if ("Đang lưu trú".equalsIgnoreCase(status)) {
            return "Đang ở";
        }
        if (status.isEmpty()) {
            return safeTrim(soPhong).isEmpty() ? "Mới tạo" : "Đã gán phòng";
        }
        return status;
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
        Connection con = ConnectDB.getConnection();
        if (con != null) {
            return con;
        }
        try {
            java.lang.reflect.Method method = ConnectDB.class.getMethod("connect");
            method.invoke(null);
        } catch (Exception ignored) {
        }
        return ConnectDB.getConnection();
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
