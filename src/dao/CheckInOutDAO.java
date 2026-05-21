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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO xử lý các nghiệp vụ liên quan đến check-in, check-out và lưu trú.
 *
 * Lớp này chịu trách nhiệm:
 * - Lấy danh sách lưu trú.
 * - Thực hiện check-in từ đơn đặt phòng.
 * - Thực hiện check-out.
 * - Cập nhật trạng thái đặt phòng và trạng thái phòng.
 * - Quản lý khách đại diện cho từng chi tiết đặt phòng.
 * - Tìm phòng khả dụng khi đổi phòng.
 */
public class CheckInOutDAO {
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Câu truy vấn cơ sở dùng để lấy thông tin lưu trú kèm thông tin đặt phòng,
     * khách hàng, phòng và loại phòng.
     */
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

    // Các trạng thái của đơn đặt phòng.
    private static final String STATUS_BOOKED = "Đã đặt";
    private static final String STATUS_CONFIRMED = "Đã xác nhận";
    private static final String STATUS_DEPOSITED = "Đã cọc";
    private static final String STATUS_PENDING_CHECKIN = "Chờ check-in";
    private static final String STATUS_ACTIVE = "Đang ở";
    private static final String STATUS_ACTIVE_STAY = "Đang lưu trú";
    private static final String STATUS_PARTIAL_CHECKOUT = "Check-out một phần";
    private static final String STATUS_WAIT_PAYMENT = "Chờ thanh toán";
    private static final String STATUS_PAID = "Đã thanh toán";
    private static final String STATUS_CHECKED_IN = "Đã check-in";
    private static final String STATUS_CHECKED_OUT = "Đã check-out";
    private static final String STATUS_CANCELLED = "Đã hủy";
    private static final String STATUS_CANCELLED_BOOKING = "Hủy booking";

    // Các trạng thái của phòng.
    private static final String STATUS_ROOM_ACTIVE = "Hoạt động";
    private static final String STATUS_ROOM_EMPTY = "Trống";
    private static final String STATUS_ROOM_READY = "Sẵn sàng";
    private static final String STATUS_ROOM_OCCUPIED = "Đang ở";

    /**
     * Mốc giờ mặc định dùng cho các đơn cũ chỉ lưu ngày mà không có giờ.
     */
    private static final LocalTime LEGACY_BOOKING_TIME_BOUNDARY = LocalTime.of(12, 0);

    /**
     * Biến đánh dấu bảng khách đại diện đã được kiểm tra/tạo hay chưa.
     */
    private static boolean representativeGuestSchemaEnsured = false;

    private String lastErrorMessage = "";

    /**
     * Lấy thông báo lỗi gần nhất trong DAO.
     *
     * @return nội dung lỗi gần nhất, rỗng nếu chưa có lỗi.
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Xác định trạng thái hiện tại của một đơn đặt phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần kiểm tra.
     * @return trạng thái đặt phòng sau khi tính toán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public String resolveBookingStatusForBooking(Connection con, int maDatPhong) throws SQLException {
        return resolveBookingStatus(con, maDatPhong, null);
    }

    /**
     * Cập nhật lại trạng thái đặt phòng dựa trên tình trạng lưu trú hiện tại.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần cập nhật.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public void refreshBookingStatus(Connection con, int maDatPhong) throws SQLException {
        if (con == null || maDatPhong <= 0) {
            return;
        }
        updateBookingStatus(con, Integer.valueOf(maDatPhong), resolveBookingStatusForBooking(con, maDatPhong));
    }

    /**
     * Lấy danh sách mã lưu trú còn đang ở, dùng cho nghiệp vụ check-out.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần lấy danh sách lưu trú.
     * @return danh sách mã lưu trú chưa check-out.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public List<Integer> loadActiveStayIdsForCheckout(Connection con, int maDatPhong) throws SQLException {
        List<Integer> stayIds = new ArrayList<Integer>();
        if (con == null || maDatPhong <= 0) {
            return stayIds;
        }

        String sql = "SELECT lt.maLuuTru FROM LuuTru lt "
                + "WHERE lt.maDatPhong = ? AND lt.checkOut IS NULL "
                + "ORDER BY lt.maLuuTru ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stayIds.add(Integer.valueOf(rs.getInt("maLuuTru")));
                }
            }
        }
        return stayIds;
    }

    /**
     * Lấy toàn bộ danh sách lưu trú.
     *
     * @return danh sách lưu trú, rỗng nếu không có dữ liệu hoặc lỗi kết nối.
     */
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

    /**
     * Lấy danh sách lưu trú theo trạng thái đặt phòng.
     *
     * Nếu trạng thái truyền vào rỗng, phương thức sẽ lấy toàn bộ dữ liệu.
     *
     * @param trangThaiDatPhong trạng thái đặt phòng cần lọc.
     * @return danh sách lưu trú phù hợp với trạng thái.
     */
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

    /**
     * Tìm thông tin lưu trú theo mã lưu trú.
     *
     * @param maLuuTru mã lưu trú dạng chuỗi.
     * @return đối tượng LuuTru nếu tìm thấy, ngược lại trả về null.
     */
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

    /**
     * Thêm mới một bản ghi lưu trú.
     *
     * Sau khi thêm lưu trú, phương thức sẽ cập nhật trạng thái đặt phòng
     * và đồng bộ lại trạng thái các phòng liên quan.
     *
     * @param luuTru thông tin lưu trú cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insert(LuuTru luuTru) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || luuTru == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu lưu trú không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            // Tắt auto-commit để thao tác thêm lưu trú và cập nhật trạng thái được xử lý trong cùng một giao dịch.
            con.setAutoCommit(false);
            try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillStatement(stmt, luuTru);
                boolean inserted = stmt.executeUpdate() > 0;
                if (!inserted) {
                    con.rollback();
                    setLastError("Không thể thêm lưu trú.");
                    return false;
                }

                // Lấy mã lưu trú vừa được tạo để cập nhật lại vào đối tượng.
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        luuTru.setMaLuuTru(String.valueOf(rs.getInt(1)));
                    }
                }
            }

            updateBookingStatus(
                    con,
                    parseIntOrNull(luuTru.getMaDatPhong()),
                    shouldMoveToCleaning(luuTru) ? STATUS_CHECKED_OUT : STATUS_ACTIVE
            );
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

    /**
     * Cập nhật thông tin một bản ghi lưu trú.
     *
     * Sau khi cập nhật, phương thức đồng bộ lại trạng thái đặt phòng và trạng thái phòng.
     *
     * @param luuTru thông tin lưu trú cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
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

            updateBookingStatus(
                    con,
                    parseIntOrNull(luuTru.getMaDatPhong()),
                    shouldMoveToCleaning(luuTru) ? STATUS_CHECKED_OUT : STATUS_ACTIVE
            );
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

    /**
     * Xóa một bản ghi lưu trú theo mã lưu trú.
     *
     * Sau khi xóa, phương thức cập nhật lại trạng thái đặt phòng và đồng bộ trạng thái phòng.
     *
     * @param maLuuTru mã lưu trú cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
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
    /**
     * Lấy danh sách phòng trống theo thứ tự tầng và số phòng.
     *
     * @param maLoaiPhong mã loại phòng cần lọc, có thể rỗng nếu muốn lấy tất cả phòng trống.
     * @return danh sách phòng đang trống.
     */
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

    /**
     * Lấy danh sách phòng khả dụng để đổi phòng.
     *
     * Method này tự lấy connection từ ConnectDB, sau đó gọi method overload bên dưới
     * để xử lý truy vấn chính.
     *
     * @param maDatPhong mã đặt phòng hiện tại.
     * @param maChiTietDatPhong mã chi tiết đặt phòng đang đổi phòng.
     * @param maLuuTru mã lưu trú hiện tại.
     * @param maPhongHienTai mã phòng hiện tại đang ở.
     * @param maLoaiPhongUuTien mã loại phòng ưu tiên khi sắp xếp kết quả.
     * @param thoiDiemDoi thời điểm thực hiện đổi phòng.
     * @param thoiGianTraDuKien thời gian trả phòng dự kiến.
     * @return danh sách phòng có thể đổi sang.
     */
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
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        // Đảm bảo bảng ChiTietDatPhong có các cột lịch dự kiến trước khi truy vấn.
        ensureDetailScheduleSchema(con);
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

    /**
     * Lấy danh sách phòng khả dụng để đổi phòng bằng connection có sẵn.
     *
     * Phòng được xem là khả dụng khi:
     * - Không phải phòng hiện tại.
     * - Đang ở trạng thái có thể sử dụng.
     * - Không thuộc chi tiết đặt phòng khác trong cùng booking.
     * - Không có lưu trú đang hoạt động.
     * - Không bị giữ bởi booking khác có thời gian giao nhau.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng hiện tại.
     * @param maChiTietDatPhong mã chi tiết đặt phòng đang đổi phòng.
     * @param maLuuTru mã lưu trú hiện tại.
     * @param maPhongHienTai mã phòng hiện tại.
     * @param maLoaiPhongUuTien mã loại phòng ưu tiên.
     * @param thoiDiemDoi thời điểm đổi phòng.
     * @param thoiGianTraDuKien thời gian trả phòng dự kiến.
     * @return danh sách phòng khả dụng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

        ensureDetailScheduleSchema(con);

        LocalDateTime normalizedExpectedCheckOut = normalizeLegacyBookingBoundary(thoiGianTraDuKien);
        if (!normalizedExpectedCheckOut.isAfter(thoiDiemDoi)) {
            return result;
        }

        Timestamp changeTimestamp = Timestamp.valueOf(thoiDiemDoi);
        Timestamp expectedCheckOutTimestamp = Timestamp.valueOf(normalizedExpectedCheckOut);
        String detailHoldCheckInExpr = buildDetailCheckInExpr("ctdpHold", "dpHold");
        String detailHoldCheckOutExpr = buildDetailCheckOutExpr("ctdpHold", "dpHold");
        String bookingBlockingStatusSql = "(N'" + STATUS_BOOKED + "', N'" + STATUS_CONFIRMED + "', N'" + STATUS_DEPOSITED
                + "', N'" + STATUS_PENDING_CHECKIN + "', N'" + STATUS_ACTIVE + "', N'" + STATUS_ACTIVE_STAY + "', N'"
                + STATUS_PARTIAL_CHECKOUT + "', N'" + STATUS_CHECKED_IN + "', N'" + STATUS_WAIT_PAYMENT + "')";
        String paidInvoiceExistsSql = "EXISTS ("
                + "SELECT 1 FROM dbo.HoaDon hdDone "
                + "WHERE ISNULL(hdDone.trangThai, N'') = N'" + STATUS_PAID + "' "
                + "AND ((hdDone.maDatPhong = dpHold.maDatPhong AND hdDone.maChiTietDatPhong IS NULL) "
                + "     OR hdDone.maChiTietDatPhong = ctdpHold.maChiTietDatPhong)"
                + ")";

        String sql = "SELECT p.maPhong, p.soPhong, ISNULL(p.tang, N'-') AS tang, ISNULL(p.khuVuc, N'-') AS khuVuc, "
                + "ISNULL(p.sucChuaToiDa, 0) AS sucChuaToiDa, lp.maLoaiPhong, "
                + "COALESCE(lp.tenLoaiPhong, N'-') AS tenLoaiPhong, ISNULL(lp.giaThamChieu, 0) AS giaThamChieu "
                + "FROM dbo.Phong p "
                + "JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "WHERE p.maPhong <> ? "
                + "AND ISNULL(p.trangThai, N'') IN (N'" + STATUS_ROOM_ACTIVE + "', N'" + STATUS_ROOM_EMPTY + "', N'" + STATUS_ROOM_READY + "') "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM ( "
                + "        SELECT ltClosed.maChiTietDatPhong, ltClosed.maDatPhong, "
                + "               ROW_NUMBER() OVER (ORDER BY ltClosed.checkOut DESC, ltClosed.maLuuTru DESC) AS rn "
                + "        FROM dbo.LuuTru ltClosed "
                + "        WHERE ltClosed.maPhong = p.maPhong AND ltClosed.checkOut IS NOT NULL "
                + "    ) latestClosedStay "
                + "    LEFT JOIN dbo.HoaDon hdRoom ON hdRoom.maChiTietDatPhong = latestClosedStay.maChiTietDatPhong "
                + "    LEFT JOIN dbo.HoaDon hdBooking ON hdBooking.maDatPhong = latestClosedStay.maDatPhong AND hdBooking.maChiTietDatPhong IS NULL "
                + "    LEFT JOIN dbo.DatPhong dpClosed ON dpClosed.maDatPhong = latestClosedStay.maDatPhong "
                + "    WHERE latestClosedStay.rn = 1 "
                + "      AND ( "
                + "           ISNULL(dpClosed.trangThai, N'') IN (N'" + STATUS_WAIT_PAYMENT + "', N'" + STATUS_CHECKED_OUT + "') "
                + "        OR (hdRoom.maHoaDon IS NOT NULL AND ISNULL(hdRoom.trangThai, N'" + STATUS_WAIT_PAYMENT + "') <> N'" + STATUS_PAID + "') "
                + "        OR (hdBooking.maHoaDon IS NOT NULL AND ISNULL(hdBooking.trangThai, N'" + STATUS_WAIT_PAYMENT + "') <> N'" + STATUS_PAID + "') "
                + "      ) "
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
                + "    OUTER APPLY ( "
                + "        SELECT TOP 1 ltHistory.maLuuTru, ltHistory.checkOut "
                + "        FROM dbo.LuuTru ltHistory "
                + "        WHERE ltHistory.maChiTietDatPhong = ctdpHold.maChiTietDatPhong "
                + "        ORDER BY CASE WHEN ltHistory.checkOut IS NULL THEN 0 ELSE 1 END, "
                + "                 COALESCE(ltHistory.checkOut, ltHistory.checkIn) DESC, ltHistory.maLuuTru DESC "
                + "    ) latestLt "
                + "    WHERE ctdpHold.maPhong = p.maPhong "
                + "      AND ctdpHold.maChiTietDatPhong <> ? "
                + "      AND ISNULL(dpHold.trangThai, N'') IN " + bookingBlockingStatusSql + " "
                + "      AND NOT " + paidInvoiceExistsSql + " "
                + "      AND " + detailHoldCheckInExpr + " < ? "
                + "      AND " + detailHoldCheckOutExpr + " > ? "
                + "      AND ((ctdpHold.maDatPhong = ? AND (latestLt.maLuuTru IS NULL OR latestLt.checkOut IS NULL)) "
                + "        OR (ctdpHold.maDatPhong <> ? AND (latestLt.maLuuTru IS NULL "
                + "           OR latestLt.checkOut IS NULL "
                + "           OR latestLt.checkOut > ? "
                + "           OR ISNULL(dpHold.trangThai, N'') = N'" + STATUS_WAIT_PAYMENT + "'))) "
                + ") "
                + "ORDER BY CASE WHEN lp.maLoaiPhong = ? THEN 0 ELSE 1 END, "
                + "CASE WHEN TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT) IS NULL THEN 1 ELSE 0 END, "
                + "TRY_CAST(REPLACE(p.tang, N'Tầng ', '') AS INT), TRY_CAST(p.soPhong AS INT), p.soPhong";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setInt(index++, maPhongHienTai);
            stmt.setInt(index++, maLuuTru);
            stmt.setInt(index++, maChiTietDatPhong);
            stmt.setTimestamp(index++, expectedCheckOutTimestamp);
            stmt.setTimestamp(index++, changeTimestamp);
            stmt.setInt(index++, maDatPhong);
            stmt.setInt(index++, maDatPhong);
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

    /**
     * Chuẩn hóa thời gian của booking cũ.
     *
     * Nếu thời gian là 00:00, hệ thống hiểu là mốc 12:00 cùng ngày
     * để phù hợp với dữ liệu đặt phòng cũ chỉ lưu ngày.
     *
     * @param value thời gian cần chuẩn hóa.
     * @return thời gian sau khi chuẩn hóa.
     */
    private LocalDateTime normalizeLegacyBookingBoundary(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        if (value.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return LocalDateTime.of(value.toLocalDate(), LEGACY_BOOKING_TIME_BOUNDARY);
        }
        return value;
    }

    /**
     * Đảm bảo cấu trúc dữ liệu lịch dự kiến của chi tiết đặt phòng đã tồn tại.
     *
     * @param con kết nối cơ sở dữ liệu.
     */
    private void ensureDetailScheduleSchema(Connection con) {
        new DatPhongDAO().ensureDetailScheduleSchema(con);
    }

    /**
     * Tạo biểu thức SQL lấy thời gian check-in dự kiến của chi tiết đặt phòng.
     *
     * Nếu chi tiết chưa có checkInDuKien, hệ thống dùng ngày nhận phòng ở DatPhong
     * cộng với mốc giờ mặc định.
     *
     * @param detailAlias alias của bảng ChiTietDatPhong.
     * @param headerAlias alias của bảng DatPhong.
     * @return biểu thức SQL lấy check-in dự kiến.
     */
    private String buildDetailCheckInExpr(String detailAlias, String headerAlias) {
        return "COALESCE(" + detailAlias + ".checkInDuKien, DATEADD(HOUR, " + LEGACY_BOOKING_TIME_BOUNDARY.getHour()
                + ", CAST(" + headerAlias + ".ngayNhanPhong AS DATETIME2)))";
    }

    /**
     * Tạo biểu thức SQL lấy thời gian check-out dự kiến của chi tiết đặt phòng.
     *
     * Nếu chi tiết chưa có checkOutDuKien, hệ thống dùng ngày trả phòng ở DatPhong
     * cộng với mốc giờ mặc định.
     *
     * @param detailAlias alias của bảng ChiTietDatPhong.
     * @param headerAlias alias của bảng DatPhong.
     * @return biểu thức SQL lấy check-out dự kiến.
     */
    private String buildDetailCheckOutExpr(String detailAlias, String headerAlias) {
        return "COALESCE(" + detailAlias + ".checkOutDuKien, DATEADD(HOUR, " + LEGACY_BOOKING_TIME_BOUNDARY.getHour()
                + ", CAST(" + headerAlias + ".ngayTraPhong AS DATETIME2)))";
    }
    /**
     * Lấy danh sách các chi tiết đặt phòng cần hiển thị trên màn hình check-in.
     *
     * Method này lấy thông tin phòng, loại phòng, thời gian dự kiến,
     * trạng thái check-in và thông tin khách đại diện của từng chi tiết đặt phòng.
     *
     * @param maDatPhong mã đặt phòng cần lấy danh sách chi tiết.
     * @return danh sách chi tiết đặt phòng phục vụ check-in.
     */
    public List<CheckInBookingItem> getBookingCheckInItems(String maDatPhong) {
        clearLastError();
        List<CheckInBookingItem> items = new ArrayList<CheckInBookingItem>();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        if (con == null || bookingId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return items;
        }

        ensureDetailScheduleSchema(con);
        ensureRepresentativeGuestSchema(con);
        String detailCheckInExpr = buildDetailCheckInExpr("ctdp", "dp");
        String detailCheckOutExpr = buildDetailCheckOutExpr("ctdp", "dp");
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, dp.tienCoc, " +
                "ISNULL(p.soPhong, N'Chưa gán') AS soPhong, " +
                "COALESCE(lp.tenLoaiPhong, lp2.tenLoaiPhong, N'-') AS tenLoaiPhong, " +
                detailCheckInExpr + " AS checkInDuKien, " +
                detailCheckOutExpr + " AS checkOutDuKien, " +
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
                    item.setExpectedCheckIn(normalizeLegacyBookingBoundary(toLocalDateTime(rs.getTimestamp("checkInDuKien"))));
                    item.setExpectedCheckOut(normalizeLegacyBookingBoundary(toLocalDateTime(rs.getTimestamp("checkOutDuKien"))));

                    // Dựa vào lưu trú gần nhất để xác định chi tiết này đang chờ check-in, đã check-in hay đã check-out.
                    Integer latestStayId = rs.getObject("maLuuTruGanNhat") == null
                            ? null
                            : Integer.valueOf(rs.getInt("maLuuTruGanNhat"));
                    item.setTrangThai(resolveCheckInItemStatus(
                            item.getMaPhong(),
                            safeTrim(rs.getString("trangThaiDatPhong")),
                            latestStayId,
                            rs.getTimestamp("checkOutGanNhat")
                    ));

                    fillCheckInItemCustomer(item, rs);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Tìm khách hàng theo CCCD/Passport.
     *
     * @param cccdPassport số CCCD hoặc Passport cần tìm.
     * @return khách hàng nếu tìm thấy, ngược lại trả về null.
     */
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

    /**
     * Tìm khách hàng theo mã khách hàng.
     *
     * @param maKhachHang mã khách hàng cần tìm.
     * @return khách hàng nếu tìm thấy, ngược lại trả về null.
     */
    public KhachHang findCustomerById(int maKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || maKhachHang <= 0) {
            return null;
        }

        String sql = "SELECT TOP 1 maKhachHang, hoTen, soDienThoai, ngaySinh, email, cccdPassport, diaChi, ghiChu "
                + "FROM KhachHang WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maKhachHang);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRepresentativeCustomer(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách khách hàng còn hoạt động để chọn làm khách đại diện.
     *
     * Nếu khách hàng ưu tiên không nằm trong danh sách đang hoạt động,
     * method sẽ cố gắng tìm và chèn khách hàng đó lên đầu danh sách.
     *
     * @param preferredCustomerId mã khách hàng ưu tiên, có thể null.
     * @return danh sách khách hàng có thể chọn.
     */
    public List<KhachHang> getAvailableCustomers(Integer preferredCustomerId) {
        clearLastError();
        List<KhachHang> customers = new ArrayList<KhachHang>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return customers;
        }

        String sql = "SELECT maKhachHang, hoTen, soDienThoai, ngaySinh, email, cccdPassport, diaChi, ghiChu "
                + "FROM KhachHang "
                + "WHERE ISNULL(trangThai, N'Hoạt động') <> N'Ngừng giao dịch' "
                + "ORDER BY maKhachHang DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapRepresentativeCustomer(rs));
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return customers;
        }

        if (preferredCustomerId != null
                && preferredCustomerId.intValue() > 0
                && !containsCustomerId(customers, preferredCustomerId.intValue())) {
            KhachHang preferredCustomer = findCustomerById(preferredCustomerId.intValue());
            if (preferredCustomer != null) {
                customers.add(0, preferredCustomer);
            }
        }
        return customers;
    }

    /**
     * Kiểm tra danh sách khách hàng đã chứa một mã khách hàng cụ thể hay chưa.
     *
     * @param customers danh sách khách hàng cần kiểm tra.
     * @param maKhachHang mã khách hàng cần tìm.
     * @return true nếu danh sách đã có khách hàng này, false nếu chưa có.
     */
    private boolean containsCustomerId(List<KhachHang> customers, int maKhachHang) {
        if (customers == null || maKhachHang <= 0) {
            return false;
        }

        for (KhachHang customer : customers) {
            if (customer == null) {
                continue;
            }
            Integer customerId = parseIntOrNull(customer.getMaKhachHang());
            if (customerId != null && customerId.intValue() == maKhachHang) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ánh xạ dữ liệu khách hàng từ ResultSet sang đối tượng KhachHang.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu khách hàng.
     * @return đối tượng KhachHang sau khi ánh xạ.
     * @throws SQLException nếu lỗi khi đọc dữ liệu từ ResultSet.
     */
    private KhachHang mapRepresentativeCustomer(ResultSet rs) throws SQLException {
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
    /**
     * Check-in nhiều chi tiết đặt phòng với cùng một thời gian check-in và check-out dự kiến.
     *
     * @param maDatPhong mã đặt phòng.
     * @param maChiTietDatPhongIds danh sách mã chi tiết đặt phòng cần check-in.
     * @param thoiGianCheckIn thời gian check-in thực tế.
     * @param thoiGianCheckOutDuKien thời gian check-out dự kiến.
     * @return số dòng lưu trú được tạo thành công.
     */
    public int checkInBookingDetails(String maDatPhong,
                                     List<Integer> maChiTietDatPhongIds,
                                     LocalDateTime thoiGianCheckIn,
                                     LocalDateTime thoiGianCheckOutDuKien) {
        return checkInBookingDetails(
                maDatPhong,
                maChiTietDatPhongIds,
                buildUniformCheckInTimings(maChiTietDatPhongIds, thoiGianCheckIn, thoiGianCheckOutDuKien),
                null
        );
    }

    /**
     * Check-in nhiều chi tiết đặt phòng với cùng một thời gian,
     * đồng thời lưu thông tin khách đại diện cho từng phòng nếu có.
     *
     * @param maDatPhong mã đặt phòng.
     * @param maChiTietDatPhongIds danh sách mã chi tiết đặt phòng cần check-in.
     * @param thoiGianCheckIn thời gian check-in thực tế.
     * @param thoiGianCheckOutDuKien thời gian check-out dự kiến.
     * @param customerByDetailId map khách đại diện theo mã chi tiết đặt phòng.
     * @return số dòng lưu trú được tạo thành công.
     */
    public int checkInBookingDetails(String maDatPhong,
                                     List<Integer> maChiTietDatPhongIds,
                                     LocalDateTime thoiGianCheckIn,
                                     LocalDateTime thoiGianCheckOutDuKien,
                                     Map<Integer, KhachHang> customerByDetailId) {
        return checkInBookingDetails(
                maDatPhong,
                maChiTietDatPhongIds,
                buildUniformCheckInTimings(maChiTietDatPhongIds, thoiGianCheckIn, thoiGianCheckOutDuKien),
                customerByDetailId
        );
    }

    /**
     * Check-in các chi tiết đặt phòng theo lịch riêng của từng phòng.
     *
     * Phương thức này thực hiện trong một giao dịch:
     * - Kiểm tra booking và danh sách chi tiết hợp lệ.
     * - Kiểm tra khách đại diện không bị trùng CCCD/Passport trong cùng booking.
     * - Tạo bản ghi lưu trú cho từng chi tiết đặt phòng.
     * - Cập nhật lịch dự kiến của chi tiết đặt phòng.
     * - Cập nhật trạng thái phòng sang đang ở.
     * - Lưu khách đại diện nếu có.
     * - Cập nhật trạng thái booking và đồng bộ trạng thái phòng.
     *
     * @param maDatPhong mã đặt phòng.
     * @param maChiTietDatPhongIds danh sách mã chi tiết đặt phòng cần check-in.
     * @param scheduleByDetailId lịch check-in/check-out dự kiến theo từng chi tiết đặt phòng.
     * @param customerByDetailId khách đại diện theo từng chi tiết đặt phòng.
     * @return số phòng được check-in thành công.
     */
    public int checkInBookingDetails(String maDatPhong,
                                     List<Integer> maChiTietDatPhongIds,
                                     Map<Integer, CheckInTiming> scheduleByDetailId,
                                     Map<Integer, KhachHang> customerByDetailId) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        if (con == null || bookingId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return 0;
        }

        List<Integer> detailIds = sanitizeDetailIds(maChiTietDatPhongIds);
        if (detailIds.isEmpty()) {
            setLastError("Không có phòng nào được chọn để check-in.");
            return 0;
        }

        String detailCheckInExpr = buildDetailCheckInExpr("ctdp", "dp");
        String detailCheckOutExpr = buildDetailCheckOutExpr("ctdp", "dp");
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ctdp.maChiTietDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, dp.tienCoc, ")
                .append("ISNULL(p.soPhong, CAST(ctdp.maPhong AS NVARCHAR(20))) AS soPhong, ")
                .append(detailCheckInExpr).append(" AS checkInDuKien, ")
                .append(detailCheckOutExpr).append(" AS checkOutDuKien ")
                .append("FROM ChiTietDatPhong ctdp ")
                .append("JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong ")
                .append("LEFT JOIN Phong p ON p.maPhong = ctdp.maPhong ")
                .append("WHERE ctdp.maDatPhong = ? ")
                .append("AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong) ")
                .append("AND ctdp.maChiTietDatPhong IN (");

        // Tạo danh sách dấu ? tương ứng với số lượng chi tiết đặt phòng cần check-in.
        for (int i = 0; i < detailIds.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");

        try {
            con.setAutoCommit(false);
            ensureDetailScheduleSchema(con);
            ensureRepresentativeGuestSchema(con);

            // Kiểm tra CCCD/Passport của khách đại diện không bị trùng giữa các phòng trong cùng booking.
            if (!validateRepresentativeGuestsForBooking(con, bookingId.intValue(), detailIds, customerByDetailId)) {
                con.rollback();
                return 0;
            }

            DatPhongDAO roomStatusDAO = new DatPhongDAO();
            int affected = 0;
            try (PreparedStatement selectStmt = con.prepareStatement(sql.toString());
                 PreparedStatement insertStmt = con.prepareStatement(
                         "INSERT INTO LuuTru(maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement detailScheduleStmt = con.prepareStatement(
                         "UPDATE ChiTietDatPhong SET checkInDuKien = ?, checkOutDuKien = ? WHERE maChiTietDatPhong = ?");
                 PreparedStatement roomStmt = con.prepareStatement(
                         "UPDATE Phong SET trangThai = N'Đang ở' WHERE maPhong = ? "
                                 + "AND ISNULL(trangThai, N'') NOT IN (N'Bảo trì', N'Không hoạt động', N'Ngừng hoạt động', N'Đang sửa')")) {
                int index = 1;
                selectStmt.setInt(index++, bookingId.intValue());
                for (Integer detailId : detailIds) {
                    selectStmt.setInt(index++, detailId.intValue());
                }

                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        // Bỏ qua chi tiết đặt phòng chưa được gán phòng.
                        if (rs.getObject("maPhong") == null) {
                            continue;
                        }

                        int maPhong = rs.getInt("maPhong");
                        String blockedStatus = roomStatusDAO.getOperationalBlockStatus(con, maPhong);
                        if (!blockedStatus.isEmpty()) {
                            con.rollback();
                            setLastError("Phòng " + safeTrim(rs.getString("soPhong"))
                                    + " đang ở trạng thái " + blockedStatus + ", không thể check-in.");
                            return 0;
                        }

                        int maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                        CheckInTiming timing = resolveCheckInTiming(
                                maChiTietDatPhong,
                                scheduleByDetailId,
                                normalizeLegacyBookingBoundary(toLocalDateTime(rs.getTimestamp("checkInDuKien"))),
                                normalizeLegacyBookingBoundary(toLocalDateTime(rs.getTimestamp("checkOutDuKien")))
                        );

                        if (timing == null || !timing.isValid()) {
                            con.rollback();
                            setLastError("Thời gian check-in / check-out dự kiến của chi tiết đặt phòng CTDP"
                                    + maChiTietDatPhong + " không hợp lệ.");
                            return 0;
                        }

                        // Tạo bản ghi lưu trú cho phòng được check-in.
                        insertStmt.setInt(1, maChiTietDatPhong);
                        insertStmt.setInt(2, bookingId.intValue());
                        insertStmt.setInt(3, maPhong);
                        insertStmt.setTimestamp(4, toTimestamp(timing.getCheckIn()));
                        insertStmt.setTimestamp(5, null);
                        insertStmt.setInt(6, rs.getInt("soNguoi"));
                        insertStmt.setDouble(7, rs.getDouble("giaPhong"));
                        insertStmt.setDouble(8, rs.getDouble("tienCoc"));
                        affected += insertStmt.executeUpdate();

                        // Cập nhật lại lịch dự kiến cho chi tiết đặt phòng.
                        detailScheduleStmt.setTimestamp(1, toTimestamp(timing.getCheckIn()));
                        detailScheduleStmt.setTimestamp(2, toTimestamp(timing.getExpectedCheckOut()));
                        detailScheduleStmt.setInt(3, maChiTietDatPhong);
                        detailScheduleStmt.executeUpdate();

                        // Cập nhật trạng thái phòng sang đang ở.
                        roomStmt.setInt(1, maPhong);
                        roomStmt.executeUpdate();

                        // Lưu khách đại diện cho phòng nếu người dùng có nhập/chọn.
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
                setLastError("Không có phòng nào sẵn sàng để check-in.");
                return 0;
            }

            updateBookingScheduleSummaryFromDetails(con, bookingId.intValue());
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

    /**
     * Check-in một phòng cụ thể từ một đơn đặt phòng.
     *
     * Phương thức này tìm chi tiết đặt phòng tương ứng với mã đặt phòng và mã phòng,
     * sau đó gọi lại phương thức check-in nhiều chi tiết đặt phòng.
     *
     * @param maDatPhong mã đặt phòng.
     * @param maPhong mã phòng cần check-in.
     * @param thoiGianCheckIn thời gian check-in thực tế.
     * @param thoiGianCheckOutDuKien thời gian check-out dự kiến.
     * @return true nếu check-in thành công, false nếu thất bại.
     */
    public boolean checkInFromBooking(String maDatPhong,
                                      String maPhong,
                                      LocalDateTime thoiGianCheckIn,
                                      LocalDateTime thoiGianCheckOutDuKien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        Integer roomId = parseIntOrNull(maPhong);
        if (con == null || bookingId == null || roomId == null) {
            setLastError(con == null
                    ? "Không thể kết nối cơ sở dữ liệu."
                    : "Mã đặt phòng hoặc mã phòng không hợp lệ.");
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
                    setLastError("Phòng được chọn không còn sẵn sàng check-in.");
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

    /**
     * Check-out một bản ghi lưu trú.
     *
     * Phương thức cập nhật thời gian check-out thực tế, sau đó cập nhật lại trạng thái booking
     * và trạng thái phòng liên quan.
     *
     * @param maLuuTru mã lưu trú cần check-out.
     * @param thoiGianCheckOutThucTe thời gian check-out thực tế.
     * @return true nếu check-out thành công, false nếu thất bại.
     */
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

    /**
     * Gia hạn thời gian trả dự kiến cho một hồ sơ lưu trú đang ở.
     *
     * Hệ thống cập nhật đúng dòng ChiTietDatPhong của hồ sơ đang chọn rồi đồng bộ
     * lại ngày trả ở DatPhong theo mốc check-out lớn nhất của toàn booking.
     *
     * @param maLuuTru mã lưu trú đang ở cần gia hạn.
     * @param thoiGianTraMoi thời gian trả dự kiến mới.
     * @return true nếu gia hạn thành công, false nếu thất bại.
     */
    public boolean extendStayExpectedCheckout(int maLuuTru, LocalDateTime thoiGianTraMoi) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }
        if (maLuuTru <= 0 || thoiGianTraMoi == null) {
            setLastError("Dữ liệu gia hạn không hợp lệ.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            ensureDetailScheduleSchema(con);

            ActiveStayExtensionContext context = loadActiveStayExtensionContext(con, maLuuTru);
            if (context == null) {
                con.rollback();
                setLastError("Không tìm thấy hồ sơ lưu trú đang ở.");
                return false;
            }

            LocalDateTime currentExpectedCheckOut = normalizeLegacyBookingBoundary(context.expectedCheckOut);
            LocalDateTime currentCheckIn = context.checkIn;
            LocalDateTime normalizedNewExpectedCheckOut = normalizeLegacyBookingBoundary(thoiGianTraMoi);

            if (currentCheckIn == null) {
                con.rollback();
                setLastError("Không xác định được thời gian nhận phòng.");
                return false;
            }
            if (currentExpectedCheckOut == null) {
                con.rollback();
                setLastError("Không xác định được thời gian trả dự kiến hiện tại.");
                return false;
            }
            if (!normalizedNewExpectedCheckOut.isAfter(currentCheckIn)) {
                con.rollback();
                setLastError("Thời gian trả mới phải lớn hơn thời gian nhận phòng.");
                return false;
            }
            if (!normalizedNewExpectedCheckOut.isAfter(currentExpectedCheckOut)) {
                con.rollback();
                setLastError("Thời gian trả mới phải lớn hơn thời gian trả dự kiến hiện tại.");
                return false;
            }

            try (PreparedStatement updateDetail = con.prepareStatement(
                    "UPDATE ChiTietDatPhong "
                            + "SET checkOutDuKien = ? "
                            + "WHERE maChiTietDatPhong = ? AND maDatPhong = ?")) {
                updateDetail.setTimestamp(1, toTimestamp(normalizedNewExpectedCheckOut));
                updateDetail.setInt(2, context.maChiTietDatPhong);
                updateDetail.setInt(3, context.maDatPhong);
                if (updateDetail.executeUpdate() <= 0) {
                    con.rollback();
                    setLastError("Không thể cập nhật thời gian trả dự kiến của phòng đang ở.");
                    return false;
                }
            }

            updateBookingScheduleSummaryFromDetails(con, context.maDatPhong);
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
    /**
     * Đồng bộ trạng thái vận hành sau các thao tác check-in/check-out.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void synchronizeOperationalStatuses(Connection con) throws SQLException {
        if (con == null) {
            return;
        }
        refreshAllRoomStatuses(con);
    }

    /**
     * Đồng bộ trạng thái vận hành nhưng không xử lý trạng thái dọn phòng.
     *
     * Hiện tại method này vẫn gọi refreshAllRoomStatuses giống method đồng bộ chính.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void synchronizeOperationalStatusesWithoutCleaning(Connection con) throws SQLException {
        refreshAllRoomStatuses(con);
    }

    /**
     * Làm mới trạng thái của toàn bộ phòng trong hệ thống.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Gán dữ liệu của đối tượng LuuTru vào PreparedStatement.
     *
     * Method này dùng chung cho thao tác insert và update lưu trú.
     *
     * @param stmt PreparedStatement cần gán tham số.
     * @param luuTru dữ liệu lưu trú cần gán.
     * @throws SQLException nếu xảy ra lỗi khi gán tham số.
     */
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

    /**
     * Chuyển một dòng ResultSet thành đối tượng LuuTru.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu lưu trú.
     * @return đối tượng LuuTru sau khi ánh xạ dữ liệu.
     * @throws SQLException nếu xảy ra lỗi khi đọc ResultSet.
     */
    private LuuTru mapLuuTru(ResultSet rs) throws SQLException {
        LuuTru luuTru = new LuuTru();
        luuTru.setMaLuuTru(String.valueOf(rs.getInt("maLuuTru")));
        luuTru.setMaChiTietDatPhong(
                rs.getObject("maChiTietDatPhong") == null ? "" : String.valueOf(rs.getInt("maChiTietDatPhong"))
        );
        luuTru.setMaDatPhong(
                rs.getObject("maDatPhong") == null ? "" : String.valueOf(rs.getInt("maDatPhong"))
        );
        luuTru.setMaPhong(
                rs.getObject("maPhong") == null ? "" : String.valueOf(rs.getInt("maPhong"))
        );
        luuTru.setCheckIn(toLocalDateTime(rs.getTimestamp("checkIn")));
        luuTru.setCheckOut(toLocalDateTime(rs.getTimestamp("checkOut")));
        luuTru.setSoNguoi(rs.getInt("soNguoi"));
        luuTru.setGiaPhong(rs.getDouble("giaPhong"));
        luuTru.setTienCoc(rs.getDouble("tienCoc"));
        luuTru.setTrangThaiDatPhong(safeTrim(rs.getString("trangThaiDatPhong")));
        luuTru.setTrangThai(resolveStayStatus(
                luuTru.getTrangThaiDatPhong(),
                luuTru.getCheckIn(),
                luuTru.getCheckOut()
        ));
        luuTru.setTenKhachHang(safeTrim(rs.getString("tenKhachHang")));
        luuTru.setSoDienThoaiKhach(safeTrim(rs.getString("soDienThoaiKhach")));
        luuTru.setSoPhong(safeTrim(rs.getString("soPhong")));
        luuTru.setTang(safeTrim(rs.getString("tang")));
        luuTru.setTenLoaiPhong(safeTrim(rs.getString("tenLoaiPhong")));
        return luuTru;
    }

    /**
     * Cập nhật trạng thái phòng nếu phòng không nằm trong các trạng thái bị khóa vận hành.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần cập nhật.
     * @param trangThai trạng thái mới.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void updateRoomStatus(Connection con, Integer maPhong, String trangThai) throws SQLException {
        if (maPhong == null) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "UPDATE Phong SET trangThai = ? WHERE maPhong = ? "
                        + "AND ISNULL(trangThai, N'') NOT IN (N'Bảo trì', N'Không hoạt động', N'Ngừng hoạt động', N'Đang sửa')")) {
            stmt.setString(1, trangThai);
            stmt.setInt(2, maPhong.intValue());
            stmt.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái của đơn đặt phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần cập nhật.
     * @param trangThai trạng thái mới.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Làm mới trạng thái booking sau khi check-in.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void refreshBookingStatusAfterCheckIn(Connection con, int maDatPhong) throws SQLException {
        String resolvedStatus = resolveBookingStatus(con, maDatPhong, null);
        updateBookingStatus(con, Integer.valueOf(maDatPhong), resolvedStatus);
    }

    /**
     * Làm mới trạng thái booking sau khi check-out.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng, có thể null.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void refreshBookingStatusAfterCheckout(Connection con, Integer maDatPhong) throws SQLException {
        if (maDatPhong == null) {
            return;
        }
        String resolvedStatus = resolveBookingStatus(con, maDatPhong.intValue(), null);
        updateBookingStatus(con, maDatPhong, resolvedStatus);
    }

    /**
     * Cập nhật ngày nhận/trả phòng tổng quát của booking dựa trên lịch của các chi tiết đặt phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần cập nhật.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void updateBookingScheduleSummaryFromDetails(Connection con, int maDatPhong) throws SQLException {
        if (con == null || maDatPhong <= 0) {
            return;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "WITH detail_bounds AS ("
                        + "    SELECT ctdp.maDatPhong, "
                        + "           " + buildDetailCheckInExpr("ctdp", "dp") + " AS resolvedCheckIn, "
                        + "           " + buildDetailCheckOutExpr("ctdp", "dp") + " AS resolvedCheckOut "
                        + "    FROM ChiTietDatPhong ctdp "
                        + "    JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                        + "    WHERE ctdp.maDatPhong = ?"
                        + "), aggregated AS ("
                        + "    SELECT maDatPhong, "
                        + "           CAST(MIN(resolvedCheckIn) AS DATE) AS minCheckIn, "
                        + "           CAST(MAX(resolvedCheckOut) AS DATE) AS maxCheckOut "
                        + "    FROM detail_bounds "
                        + "    GROUP BY maDatPhong"
                        + ") "
                        + "UPDATE dp "
                        + "SET dp.ngayNhanPhong = COALESCE(agg.minCheckIn, dp.ngayNhanPhong), "
                        + "    dp.ngayTraPhong = COALESCE(agg.maxCheckOut, dp.ngayTraPhong) "
                        + "FROM DatPhong dp "
                        + "LEFT JOIN aggregated agg ON agg.maDatPhong = dp.maDatPhong "
                        + "WHERE dp.maDatPhong = ?")) {
            stmt.setInt(1, maDatPhong);
            stmt.setInt(2, maDatPhong);
            stmt.executeUpdate();
        }
    }

    /**
     * Tải dữ liệu lõi của hồ sơ lưu trú đang ở để phục vụ nghiệp vụ gia hạn.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maLuuTru mã lưu trú đang ở.
     * @return thông tin lưu trú hiện hành hoặc null nếu không tìm thấy.
     * @throws SQLException nếu xảy ra lỗi truy vấn.
     */
    private ActiveStayExtensionContext loadActiveStayExtensionContext(Connection con, int maLuuTru) throws SQLException {
        if (con == null || maLuuTru <= 0) {
            return null;
        }
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, lt.checkIn, "
                + buildDetailCheckOutExpr("ctdp", "dp") + " AS expectedCheckOut "
                + "FROM LuuTru lt "
                + "JOIN ChiTietDatPhong ctdp ON ctdp.maChiTietDatPhong = lt.maChiTietDatPhong "
                + "JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong "
                + "WHERE lt.maLuuTru = ? AND lt.checkOut IS NULL";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLuuTru);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ActiveStayExtensionContext(
                            rs.getInt("maLuuTru"),
                            rs.getInt("maChiTietDatPhong"),
                            rs.getInt("maDatPhong"),
                            rs.getInt("maPhong"),
                            toLocalDateTime(rs.getTimestamp("checkIn")),
                            toLocalDateTime(rs.getTimestamp("expectedCheckOut"))
                    );
                }
            }
        }
        return null;
    }

    /**
     * Kiểm tra booking có lưu trú nào đang hoạt động hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @return true nếu còn lưu trú chưa check-out.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Kiểm tra booking còn chi tiết đặt phòng nào chưa phát sinh lưu trú hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @return true nếu còn chi tiết đặt phòng đang chờ check-in.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasPendingCheckInDetails(Connection con, int maDatPhong) throws SQLException {
        String sql = "SELECT COUNT(1) "
                + "FROM ChiTietDatPhong ctdp "
                + "JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                + "WHERE ctdp.maDatPhong = ? "
                + "AND ISNULL(dp.trangThai, N'') NOT IN (N'Đã hủy', N'Hủy booking') "
                + "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    /**
     * Xác định trạng thái hiện tại của đơn đặt phòng dựa trên dữ liệu lưu trú và thanh toán.
     *
     * Quy tắc chính:
     * - Nếu booking đã hủy thì giữ trạng thái hủy.
     * - Nếu booking đã thanh toán đủ thì trả về trạng thái đã thanh toán.
     * - Nếu còn phòng đang ở thì booking ở trạng thái đang ở.
     * - Nếu tất cả chi tiết đã check-out thì chuyển sang chờ thanh toán.
     * - Nếu chưa phát sinh lưu trú thì chuyển sang chờ check-in.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần xác định trạng thái.
     * @param currentStatus trạng thái hiện tại, có thể null.
     * @return trạng thái booking sau khi tính toán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Kiểm tra booking đã được thanh toán đầy đủ hay chưa.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @return true nếu booking đã thanh toán đủ, false nếu chưa.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean isBookingPaid(Connection con, int maDatPhong) throws SQLException {
        return new DatPhongDAO().isBookingFullyPaid(con, maDatPhong);
    }

    /**
     * Kiểm tra lưu trú có cần chuyển sang trạng thái đã check-out hay không.
     *
     * @param luuTru thông tin lưu trú cần kiểm tra.
     * @return true nếu trạng thái là đã check-out hoặc check-out.
     */
    private boolean shouldMoveToCleaning(LuuTru luuTru) {
        String status = safeTrim(luuTru.getTrangThai());
        return STATUS_CHECKED_OUT.equalsIgnoreCase(status) || "Check-out".equalsIgnoreCase(status);
    }

    /**
     * Xác định trạng thái hiển thị của lưu trú dựa trên trạng thái booking,
     * thời gian check-in và thời gian check-out.
     *
     * @param trangThaiDatPhong trạng thái của đơn đặt phòng.
     * @param checkIn thời gian check-in.
     * @param checkOut thời gian check-out.
     * @return trạng thái lưu trú dùng để hiển thị.
     */
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

    /**
     * Xác định trạng thái của một dòng chi tiết trên màn hình check-in.
     *
     * @param maPhong mã phòng của chi tiết đặt phòng.
     * @param bookingStatus trạng thái booking.
     * @param latestStayId mã lưu trú gần nhất nếu có.
     * @param latestCheckOut thời gian check-out gần nhất nếu có.
     * @return trạng thái check-in của chi tiết đặt phòng.
     */
    private String resolveCheckInItemStatus(int maPhong,
                                            String bookingStatus,
                                            Integer latestStayId,
                                            Timestamp latestCheckOut) {
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

    /**
     * Gán thông tin khách đại diện từ ResultSet vào item check-in.
     *
     * Nếu chi tiết đặt phòng chưa có khách đại diện, method sẽ xóa thông tin khách trên item.
     *
     * @param item item check-in cần gán thông tin khách.
     * @param rs ResultSet chứa dữ liệu khách đại diện.
     * @throws SQLException nếu xảy ra lỗi khi đọc ResultSet.
     */
    private void fillCheckInItemCustomer(CheckInBookingItem item, ResultSet rs) throws SQLException {
        if (item == null || rs == null) {
            return;
        }

        if (rs.getObject("maKhachHangDaiDien") != null) {
            item.setRepresentativeCustomerId(rs.getInt("maKhachHangDaiDien"));
            item.setCccdPassport(safeTrim(rs.getString("cccdPassportDaiDien")));
            item.setHoTenKhach(safeTrim(rs.getString("hoTenDaiDien")));
            item.setSoDienThoai(safeTrim(rs.getString("soDienThoaiDaiDien")));
            item.setNgaySinh(formatDisplayDate(rs.getDate("ngaySinhDaiDien")));
            item.setEmail(safeTrim(rs.getString("emailDaiDien")));
            item.setDiaChi(safeTrim(rs.getString("diaChiDaiDien")));
            item.setGhiChu(safeTrim(rs.getString("ghiChuDaiDien")));
            return;
        }

        clearCheckInItemCustomer(item);
    }

    /**
     * Xóa thông tin khách đại diện trên item check-in.
     *
     * @param item item cần xóa thông tin khách.
     */
    private void clearCheckInItemCustomer(CheckInBookingItem item) {
        if (item == null) {
            return;
        }
        item.setRepresentativeCustomerId(0);
        item.setCccdPassport("");
        item.setHoTenKhach("");
        item.setSoDienThoai("");
        item.setNgaySinh("");
        item.setEmail("");
        item.setDiaChi("");
        item.setGhiChu("");
    }

    /**
     * Định dạng ngày theo dạng dd/MM/yyyy để hiển thị trên giao diện.
     *
     * @param value ngày cần định dạng.
     * @return chuỗi ngày đã định dạng, hoặc chuỗi rỗng nếu value null.
     */
    private String formatDisplayDate(Date value) {
        return value == null ? "" : DISPLAY_DATE_FORMAT.format(value.toLocalDate());
    }

    /**
     * Đảm bảo bảng lưu khách đại diện theo chi tiết đặt phòng đã tồn tại.
     *
     * Method này chỉ tạo bảng nếu bảng chưa tồn tại, và chỉ thực hiện một lần trong vòng đời chương trình.
     *
     * @param con kết nối cơ sở dữ liệu.
     */
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

    /**
     * Lưu thông tin khách đại diện cho một chi tiết đặt phòng.
     *
     * Nếu khách chưa tồn tại thì tạo mới khách hàng.
     * Nếu chi tiết đã có khách đại diện thì cập nhật, ngược lại thì thêm mới.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maChiTietDatPhong mã chi tiết đặt phòng.
     * @param khachHang thông tin khách đại diện.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void persistRepresentativeGuest(Connection con,
                                            int maChiTietDatPhong,
                                            KhachHang khachHang) throws SQLException {
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
    /**
     * Thêm mới hoặc cập nhật khách hàng đại diện.
     *
     * Nếu khách hàng đã tồn tại theo mã khách hàng hoặc CCCD/Passport,
     * method sẽ cập nhật thông tin khách hàng đó. Nếu chưa tồn tại thì tạo mới.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param khachHang thông tin khách hàng đại diện.
     * @return mã khách hàng sau khi thêm/cập nhật, hoặc null nếu dữ liệu không hợp lệ.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private Integer upsertRepresentativeCustomer(Connection con, KhachHang khachHang) throws SQLException {
        if (con == null || khachHang == null) {
            return null;
        }

        String cccdPassport = safeTrim(khachHang.getCccdPassport());
        String hoTen = safeTrim(khachHang.getHoTen());
        if (cccdPassport.isEmpty() || hoTen.isEmpty()) {
            return null;
        }

        Integer requestedCustomerId = parseIntOrNull(khachHang.getMaKhachHang());
        Integer existingId = isExistingCustomerId(con, requestedCustomerId)
                ? requestedCustomerId
                : findCustomerIdByPassport(con, cccdPassport);

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

        String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) "
                + "VALUES (?, N'Khác', ?, ?, ?, ?, ?, N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', N'Hệ thống', ?)";
        try (PreparedStatement insert = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, hoTen);
            setNullableDateFromText(insert, 2, khachHang.getNgaySinh());
            insert.setString(3, safeTrim(khachHang.getSoDienThoai()));
            insert.setString(4, safeTrim(khachHang.getEmail()));
            insert.setString(5, cccdPassport);
            insert.setString(6, safeTrim(khachHang.getDiaChi()));
            insert.setString(
                    7,
                    nullIfEmpty(khachHang.getGhiChu()) == null
                            ? "Tạo từ màn hình check-in"
                            : safeTrim(khachHang.getGhiChu())
            );
            insert.executeUpdate();

            try (ResultSet rs = insert.getGeneratedKeys()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    /**
     * Lưu khách đại diện cho một chi tiết đặt phòng cụ thể.
     *
     * Method này dùng khi người dùng chọn hoặc nhập khách đại diện trước khi check-in.
     *
     * @param maDatPhong mã đặt phòng.
     * @param maChiTietDatPhong mã chi tiết đặt phòng.
     * @param khachHang thông tin khách đại diện.
     * @return true nếu lưu thành công, false nếu thất bại.
     */
    public boolean saveRepresentativeGuestForBookingDetail(String maDatPhong,
                                                           int maChiTietDatPhong,
                                                           KhachHang khachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer bookingId = parseIntOrNull(maDatPhong);
        if (con == null || bookingId == null || maChiTietDatPhong <= 0) {
            setLastError(con == null
                    ? "Không thể kết nối cơ sở dữ liệu."
                    : "Thông tin booking hoặc chi tiết phòng không hợp lệ.");
            return false;
        }

        if (khachHang == null
                || safeTrim(khachHang.getHoTen()).isEmpty()
                || safeTrim(khachHang.getCccdPassport()).isEmpty()) {
            setLastError("Vui lòng nhập đầy đủ Họ tên và CCCD/Passport cho khách đại diện.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            ensureRepresentativeGuestSchema(con);

            if (!belongsToBooking(con, bookingId.intValue(), maChiTietDatPhong)) {
                con.rollback();
                setLastError("Phòng được chọn không thuộc booking hiện tại.");
                return false;
            }

            Map<Integer, KhachHang> customerByDetailId = new LinkedHashMap<Integer, KhachHang>();
            customerByDetailId.put(Integer.valueOf(maChiTietDatPhong), khachHang);

            List<Integer> targetDetailIds = new ArrayList<Integer>();
            targetDetailIds.add(Integer.valueOf(maChiTietDatPhong));

            if (!validateRepresentativeGuestsForBooking(con, bookingId.intValue(), targetDetailIds, customerByDetailId)) {
                con.rollback();
                return false;
            }

            persistRepresentativeGuest(con, maChiTietDatPhong, khachHang);
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

    /**
     * Kiểm tra khách đại diện trong cùng một booking có bị trùng CCCD/Passport hay không.
     *
     * Method kiểm tra cả dữ liệu người dùng vừa nhập và dữ liệu khách đại diện đã lưu trong database.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @param targetDetailIds danh sách chi tiết đặt phòng đang được cập nhật.
     * @param customerByDetailId map khách đại diện theo từng chi tiết đặt phòng.
     * @return true nếu không bị trùng, false nếu phát hiện trùng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean validateRepresentativeGuestsForBooking(Connection con,
                                                           int maDatPhong,
                                                           List<Integer> targetDetailIds,
                                                           Map<Integer, KhachHang> customerByDetailId) throws SQLException {
        if (con == null || maDatPhong <= 0 || customerByDetailId == null || customerByDetailId.isEmpty()) {
            return true;
        }

        java.util.Set<Integer> targetSet = new java.util.LinkedHashSet<Integer>();
        if (targetDetailIds != null) {
            targetSet.addAll(targetDetailIds);
        }

        java.util.Map<String, Integer> passportToDetailId = new java.util.LinkedHashMap<String, Integer>();
        for (Map.Entry<Integer, KhachHang> entry : customerByDetailId.entrySet()) {
            Integer detailId = entry.getKey();
            KhachHang khachHang = entry.getValue();
            String passport = normalizePassportKey(khachHang == null ? null : khachHang.getCccdPassport());

            if (detailId == null || detailId.intValue() <= 0 || passport.isEmpty()) {
                continue;
            }

            Integer existingDetailId = passportToDetailId.get(passport);
            if (existingDetailId != null && existingDetailId.intValue() != detailId.intValue()) {
                setLastError("CCCD/Passport này đã được sử dụng cho phòng khác trong cùng đơn. Vui lòng chọn khách hàng khác.");
                return false;
            }
            passportToDetailId.put(passport, detailId);
        }

        String sql = "SELECT ctdp.maChiTietDatPhong, ISNULL(kh.cccdPassport, N'') AS cccdPassport "
                + "FROM ChiTietDatPhong ctdp "
                + "JOIN ChiTietDatPhongKhachDaiDien rep ON rep.maChiTietDatPhong = ctdp.maChiTietDatPhong "
                + "JOIN KhachHang kh ON kh.maKhachHang = rep.maKhachHang "
                + "WHERE ctdp.maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int detailId = rs.getInt("maChiTietDatPhong");

                    // Bỏ qua các chi tiết đang được cập nhật trong lần lưu hiện tại.
                    if (targetSet.contains(Integer.valueOf(detailId))) {
                        continue;
                    }

                    String passport = normalizePassportKey(rs.getString("cccdPassport"));
                    if (passport.isEmpty()) {
                        continue;
                    }

                    Integer inputDetailId = passportToDetailId.get(passport);
                    if (inputDetailId != null && inputDetailId.intValue() != detailId) {
                        setLastError("CCCD/Passport này đã được sử dụng cho phòng khác trong cùng đơn. Vui lòng chọn khách hàng khác.");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Kiểm tra một chi tiết đặt phòng có thuộc booking hiện tại hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @param maChiTietDatPhong mã chi tiết đặt phòng.
     * @return true nếu chi tiết đặt phòng thuộc booking, false nếu không.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean belongsToBooking(Connection con, int maDatPhong, int maChiTietDatPhong) throws SQLException {
        if (con == null || maDatPhong <= 0 || maChiTietDatPhong <= 0) {
            return false;
        }

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT 1 FROM ChiTietDatPhong WHERE maDatPhong = ? AND maChiTietDatPhong = ?")) {
            stmt.setInt(1, maDatPhong);
            stmt.setInt(2, maChiTietDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Kiểm tra mã khách hàng có tồn tại hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maKhachHang mã khách hàng cần kiểm tra.
     * @return true nếu khách hàng tồn tại, false nếu không.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean isExistingCustomerId(Connection con, Integer maKhachHang) throws SQLException {
        if (con == null || maKhachHang == null || maKhachHang.intValue() <= 0) {
            return false;
        }

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT 1 FROM KhachHang WHERE maKhachHang = ?")) {
            stmt.setInt(1, maKhachHang.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Tìm mã khách hàng theo CCCD/Passport.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param cccdPassport CCCD/Passport cần tìm.
     * @return mã khách hàng nếu tìm thấy, ngược lại trả về null.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Tìm mã khách hàng theo số điện thoại.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param soDienThoai số điện thoại cần tìm.
     * @return mã khách hàng nếu tìm thấy, ngược lại trả về null.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Chuẩn hóa CCCD/Passport để so sánh chống trùng.
     *
     * @param value CCCD/Passport cần chuẩn hóa.
     * @return chuỗi đã trim và chuyển sang chữ hoa.
     */
    private String normalizePassportKey(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    /**
     * Gán giá trị ngày vào PreparedStatement từ chuỗi ngày nhập.
     *
     * Nếu chuỗi ngày không hợp lệ hoặc rỗng, method sẽ gán NULL.
     *
     * @param stmt PreparedStatement cần gán tham số.
     * @param index vị trí tham số.
     * @param value chuỗi ngày cần chuyển đổi.
     * @throws SQLException nếu xảy ra lỗi khi gán tham số.
     */
    private void setNullableDateFromText(PreparedStatement stmt, int index, String value) throws SQLException {
        LocalDate parsedDate = parseFlexibleDate(value);
        if (parsedDate == null) {
            stmt.setNull(index, java.sql.Types.DATE);
            return;
        }
        stmt.setDate(index, Date.valueOf(parsedDate));
    }

    /**
     * Chuyển chuỗi ngày sang LocalDate.
     *
     * Method hỗ trợ hai định dạng:
     * - dd/MM/yyyy
     * - yyyy-MM-dd
     *
     * @param value chuỗi ngày cần chuyển.
     * @return LocalDate nếu parse được, ngược lại trả về null.
     */
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

    /**
     * Gán số nguyên có thể null vào PreparedStatement.
     *
     * @param stmt PreparedStatement cần gán tham số.
     * @param index vị trí tham số.
     * @param value chuỗi số nguyên cần chuyển đổi.
     * @throws SQLException nếu xảy ra lỗi khi gán tham số.
     */
    private void setNullableInt(PreparedStatement stmt, int index, String value) throws SQLException {
        Integer parsed = parseIntOrNull(value);
        if (parsed == null) {
            stmt.setObject(index, null);
        } else {
            stmt.setInt(index, parsed.intValue());
        }
    }

    /**
     * Chuyển chuỗi sang Integer.
     *
     * @param value chuỗi cần chuyển.
     * @return Integer nếu hợp lệ, ngược lại trả về null.
     */
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

    /**
     * Chuyển LocalDateTime sang Timestamp để lưu database.
     *
     * @param value thời gian cần chuyển.
     * @return Timestamp tương ứng, hoặc null nếu value null.
     */
    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    /**
     * Chuyển Timestamp sang LocalDateTime.
     *
     * @param value Timestamp cần chuyển.
     * @return LocalDateTime tương ứng, hoặc null nếu value null.
     */
    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    /**
     * Cắt khoảng trắng đầu/cuối của chuỗi.
     *
     * @param value chuỗi cần xử lý.
     * @return chuỗi đã trim, hoặc chuỗi rỗng nếu value null.
     */
    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Chuyển chuỗi rỗng thành null.
     *
     * @param value chuỗi cần xử lý.
     * @return null nếu chuỗi rỗng, ngược lại trả về chuỗi đã trim.
     */
    private String nullIfEmpty(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Rollback giao dịch, bỏ qua lỗi rollback nếu có.
     *
     * @param con kết nối cơ sở dữ liệu.
     */
    private void rollbackQuietly(Connection con) {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Bật lại auto-commit cho connection, bỏ qua lỗi nếu có.
     *
     * @param con kết nối cơ sở dữ liệu.
     */
    private void resetAutoCommit(Connection con) {
        try {
            if (con != null) {
                con.setAutoCommit(true);
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Xóa thông báo lỗi gần nhất trước khi thực hiện thao tác mới.
     */
    private void clearLastError() {
        lastErrorMessage = "";
    }

    /**
     * Lưu thông báo lỗi gần nhất.
     *
     * @param message nội dung lỗi cần lưu.
     */
    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }

    /**
     * Làm sạch danh sách mã chi tiết đặt phòng.
     *
     * Method loại bỏ giá trị null, giá trị <= 0 và mã bị trùng.
     *
     * @param maChiTietDatPhongIds danh sách mã chi tiết đặt phòng đầu vào.
     * @return danh sách mã hợp lệ, không trùng.
     */
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

    /**
     * Tạo lịch check-in/check-out giống nhau cho nhiều chi tiết đặt phòng.
     *
     * @param maChiTietDatPhongIds danh sách mã chi tiết đặt phòng.
     * @param thoiGianCheckIn thời gian check-in.
     * @param thoiGianCheckOutDuKien thời gian check-out dự kiến.
     * @return map lịch check-in theo từng mã chi tiết đặt phòng.
     */
    private Map<Integer, CheckInTiming> buildUniformCheckInTimings(List<Integer> maChiTietDatPhongIds,
                                                                   LocalDateTime thoiGianCheckIn,
                                                                   LocalDateTime thoiGianCheckOutDuKien) {
        Map<Integer, CheckInTiming> timings = new java.util.LinkedHashMap<Integer, CheckInTiming>();
        CheckInTiming timing = new CheckInTiming(thoiGianCheckIn, thoiGianCheckOutDuKien);
        for (Integer detailId : sanitizeDetailIds(maChiTietDatPhongIds)) {
            timings.put(detailId, timing);
        }
        return timings;
    }

    /**
     * Xác định lịch check-in/check-out cho một chi tiết đặt phòng.
     *
     * Ưu tiên lấy từ scheduleByDetailId. Nếu không có hoặc không hợp lệ,
     * method dùng lịch fallback lấy từ database.
     *
     * @param maChiTietDatPhong mã chi tiết đặt phòng.
     * @param scheduleByDetailId map lịch theo chi tiết đặt phòng.
     * @param fallbackCheckIn thời gian check-in dự phòng.
     * @param fallbackCheckOut thời gian check-out dự phòng.
     * @return lịch hợp lệ, hoặc null nếu không có lịch hợp lệ.
     */
    private CheckInTiming resolveCheckInTiming(int maChiTietDatPhong,
                                               Map<Integer, CheckInTiming> scheduleByDetailId,
                                               LocalDateTime fallbackCheckIn,
                                               LocalDateTime fallbackCheckOut) {
        CheckInTiming timing = scheduleByDetailId == null ? null : scheduleByDetailId.get(Integer.valueOf(maChiTietDatPhong));
        if (timing != null && timing.isValid()) {
            return timing;
        }

        CheckInTiming fallback = new CheckInTiming(fallbackCheckIn, fallbackCheckOut);
        return fallback.isValid() ? fallback : null;
    }

    /**
     * Lớp lưu thời gian check-in và check-out dự kiến của một chi tiết đặt phòng.
     */
    public static final class CheckInTiming {
        private final LocalDateTime checkIn;
        private final LocalDateTime expectedCheckOut;

        /**
         * Khởi tạo lịch check-in/check-out.
         *
         * @param checkIn thời gian check-in.
         * @param expectedCheckOut thời gian check-out dự kiến.
         */
        public CheckInTiming(LocalDateTime checkIn, LocalDateTime expectedCheckOut) {
            this.checkIn = checkIn;
            this.expectedCheckOut = expectedCheckOut;
        }

        public LocalDateTime getCheckIn() {
            return checkIn;
        }

        public LocalDateTime getExpectedCheckOut() {
            return expectedCheckOut;
        }

        /**
         * Kiểm tra lịch check-in/check-out có hợp lệ hay không.
         *
         * @return true nếu có đủ check-in, check-out và check-out sau check-in.
         */
        public boolean isValid() {
            return checkIn != null && expectedCheckOut != null && expectedCheckOut.isAfter(checkIn);
        }
    }

    /**
     * DTO dùng để hiển thị một dòng chi tiết đặt phòng trên màn hình check-in.
     */
    public static final class CheckInBookingItem {
        private int maChiTietDatPhong;
        private int maPhong;
        private String soPhong;
        private String tenLoaiPhong;
        private int soNguoi;
        private double giaPhong;
        private double tienCoc;
        private String trangThai;
        private LocalDateTime expectedCheckIn;
        private LocalDateTime expectedCheckOut;
        private String cccdPassport;
        private String hoTenKhach;
        private String soDienThoai;
        private String ngaySinh;
        private String email;
        private String diaChi;
        private String ghiChu;
        private int representativeCustomerId;

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

        public LocalDateTime getExpectedCheckIn() {
            return expectedCheckIn;
        }

        public void setExpectedCheckIn(LocalDateTime expectedCheckIn) {
            this.expectedCheckIn = expectedCheckIn;
        }

        public LocalDateTime getExpectedCheckOut() {
            return expectedCheckOut;
        }

        public void setExpectedCheckOut(LocalDateTime expectedCheckOut) {
            this.expectedCheckOut = expectedCheckOut;
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

        public int getRepresentativeCustomerId() {
            return representativeCustomerId;
        }

        public void setRepresentativeCustomerId(int representativeCustomerId) {
            this.representativeCustomerId = representativeCustomerId;
        }

        /**
         * Kiểm tra dòng chi tiết này có đủ điều kiện check-in hay không.
         *
         * @return true nếu đã có chi tiết đặt phòng, đã có phòng và đang chờ check-in.
         */
        public boolean canCheckIn() {
            return maChiTietDatPhong > 0 && maPhong > 0 && "Chờ check-in".equalsIgnoreCase(trangThai);
        }
    }

    /**
     * DTO chứa thông tin phòng có thể chọn khi đổi phòng.
     */
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

    private static final class ActiveStayExtensionContext {
        private final int maLuuTru;
        private final int maChiTietDatPhong;
        private final int maDatPhong;
        private final int maPhong;
        private final LocalDateTime checkIn;
        private final LocalDateTime expectedCheckOut;

        private ActiveStayExtensionContext(int maLuuTru,
                                           int maChiTietDatPhong,
                                           int maDatPhong,
                                           int maPhong,
                                           LocalDateTime checkIn,
                                           LocalDateTime expectedCheckOut) {
            this.maLuuTru = maLuuTru;
            this.maChiTietDatPhong = maChiTietDatPhong;
            this.maDatPhong = maDatPhong;
            this.maPhong = maPhong;
            this.checkIn = checkIn;
            this.expectedCheckOut = expectedCheckOut;
        }
    }
}
