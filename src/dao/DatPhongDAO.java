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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * DAO xử lý các thao tác liên quan đến đặt phòng.
 *
 * Lớp này phụ trách:
 * - Thêm, cập nhật, xóa và tìm kiếm đặt phòng.
 * - Quản lý chi tiết đặt phòng.
 * - Kiểm tra phòng trống và xung đột đặt phòng.
 * - Cập nhật trạng thái đặt phòng và trạng thái phòng.
 * - Tính giá phòng theo bảng giá, ngày thường, cuối tuần, ngày lễ.
 * - Kiểm tra trạng thái thanh toán của booking.
 */
public class DatPhongDAO {
    // Các trạng thái chính của đặt phòng.
    public static final String STATUS_PENDING_CHECKIN = "Chờ check-in";
    public static final String STATUS_ACTIVE = "Đang ở";
    public static final String STATUS_PARTIAL_CHECKOUT = "Check-out một phần";
    public static final String STATUS_WAIT_PAYMENT = "Chờ thanh toán";
    public static final String STATUS_PAID = "Đã thanh toán";
    public static final String STATUS_CHECKED_OUT = "Đã check-out";
    public static final String STATUS_CANCELLED = "Đã hủy";
    public static final String STATUS_CANCELLED_BOOKING = "Hủy booking";

    // Các loại ngày dùng khi xác định giá phòng.
    private static final String LOAI_NGAY_THUONG = "Ngày thường";
    private static final String LOAI_NGAY_CUOI_TUAN = "Cuối tuần";
    private static final String LOAI_NGAY_LE = "Ngày lễ";

    // Các loại giá dùng khi áp dụng bảng giá.
    private static final String LOAI_GIA_THEO_NGAY = "Theo ngày";
    private static final String LOAI_GIA_QUA_DEM = "Qua đêm";
    private static final String LOAI_GIA_THEO_GIO = "Theo giờ";
    private static final String LOAI_GIA_LE = "Giá lễ";
    private static final String LOAI_GIA_CUOI_TUAN = "Giá cuối tuần";

    // Nhóm trạng thái phòng có thể dùng để gán phòng.
    private static final String ROOM_READY_STATUS_SQL = "(N'Hoạt động', N'Trống', N'Sẵn sàng', N'Dọn dẹp', N'Dọn phòng')";

    // Nhóm trạng thái phòng bị khóa vận hành, không được tự động đổi trạng thái.
    private static final String ROOM_BLOCKED_STATUS_SQL = "(N'Bảo trì', N'Không hoạt động', N'Ngừng hoạt động', N'Đang sửa')";

    // Nhóm trạng thái booking có khả năng giữ phòng, dùng khi kiểm tra trùng lịch.
    private static final String BOOKING_BLOCKING_STATUS_SQL =
            "(N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in', N'Đang ở', N'Đã check-in', N'Check-out một phần', N'Chờ thanh toán')";

    // Key nội bộ cho loại ngày.
    private static final String DAY_TYPE_NORMAL = "THUONG";
    private static final String DAY_TYPE_WEEKEND = "CUOI_TUAN";
    private static final String DAY_TYPE_HOLIDAY = "NGAY_LE";

    // Key nội bộ cho loại lưu trú.
    private static final String STAY_TYPE_HOURLY = "THEO_GIO";
    private static final String STAY_TYPE_DAILY = "THEO_NGAY";
    private static final String STAY_TYPE_OVERNIGHT = "QUA_DEM";

    // Chuỗi hiển thị cho loại ngày và loại giá.
    private static final String DISPLAY_LOAI_NGAY_THUONG = "Ng\u00e0y th\u01b0\u1eddng";
    private static final String DISPLAY_LOAI_NGAY_CUOI_TUAN = "Cu\u1ed1i tu\u1ea7n";
    private static final String DISPLAY_LOAI_NGAY_LE = "Ng\u00e0y l\u1ec5";
    private static final String DISPLAY_LOAI_GIA_THEO_NGAY = "Theo ng\u00e0y";
    private static final String DISPLAY_LOAI_GIA_QUA_DEM = "Qua \u0111\u00eam";
    private static final String DISPLAY_LOAI_GIA_THEO_GIO = "Theo gi\u1edd";

    /**
     * Mốc giờ mặc định dùng cho chi tiết đặt phòng.
     *
     * Vì một số dữ liệu đặt phòng chỉ lưu ngày, hệ thống dùng mốc 12:00
     * để chuyển từ LocalDate sang LocalDateTime.
     */
    private static final LocalTime DETAIL_BOOKING_BOUNDARY_TIME = LocalTime.of(12, 0);

    /**
     * Đánh dấu đã kiểm tra/tạo schema lịch dự kiến cho ChiTietDatPhong hay chưa.
     */
    private static boolean detailScheduleSchemaEnsured = false;

    /**
     * Đánh dấu đã kiểm tra/tạo schema phạm vi hóa đơn theo chi tiết đặt phòng hay chưa.
     */
    private static boolean invoiceScopeSchemaEnsured = false;

    /**
     * Chuẩn hóa trạng thái booking theo giai đoạn xử lý.
     *
     * Một số trạng thái như "Đã check-in" hoặc "Đang lưu trú" được quy về "Đang ở"
     * để các màn hình xử lý trạng thái thống nhất hơn.
     *
     * @param status trạng thái cần chuẩn hóa.
     * @return trạng thái sau khi chuẩn hóa.
     */
    public static String normalizeStageStatus(String status) {
        String value = status == null ? "" : status.trim();
        if ("\u0110\u00e3 check-in".equalsIgnoreCase(value) || "\u0110ang l\u01b0u tr\u00fa".equalsIgnoreCase(value)) {
            return "\u0110ang \u1edf";
        }
        return value;
    }

    /**
     * Kiểm tra trạng thái có thuộc giai đoạn đặt phòng ban đầu hay không.
     *
     * @param status trạng thái cần kiểm tra.
     * @return true nếu trạng thái thuộc nhóm đã đặt/xác nhận/cọc/chờ check-in.
     */
    public static boolean isBookingStageStatus(String status) {
        String value = normalizeStageStatus(status);
        return "\u0110\u00e3 \u0111\u1eb7t".equalsIgnoreCase(value)
                || "\u0110\u00e3 x\u00e1c nh\u1eadn".equalsIgnoreCase(value)
                || "\u0110\u00e3 c\u1ecdc".equalsIgnoreCase(value)
                || "Ch\u1edd check-in".equalsIgnoreCase(value);
    }

    /**
     * Kiểm tra trạng thái có thuộc giai đoạn vận hành/lưu trú hay không.
     *
     * @param status trạng thái cần kiểm tra.
     * @return true nếu trạng thái thuộc nhóm chờ check-in, đang ở hoặc check-out một phần.
     */
    public static boolean isOperationalStageStatus(String status) {
        String value = normalizeStageStatus(status);
        return "Ch\u1edd check-in".equalsIgnoreCase(value)
                || "\u0110ang \u1edf".equalsIgnoreCase(value)
                || "Check-out m\u1ed9t ph\u1ea7n".equalsIgnoreCase(value);
    }

    /**
     * Kiểm tra trạng thái có thuộc giai đoạn thanh toán hay không.
     *
     * @param status trạng thái cần kiểm tra.
     * @return true nếu trạng thái thuộc nhóm đã check-out, chờ thanh toán hoặc đã thanh toán.
     */
    public static boolean isPaymentStageStatus(String status) {
        String value = normalizeStageStatus(status);
        return "\u0110\u00e3 check-out".equalsIgnoreCase(value)
                || "Ch\u1edd thanh to\u00e1n".equalsIgnoreCase(value)
                || "\u0110\u00e3 thanh to\u00e1n".equalsIgnoreCase(value);
    }

    /**
     * Câu SELECT cơ sở dùng để lấy thông tin header của đặt phòng.
     *
     * Dữ liệu được join thêm khách hàng để hiển thị tên, số điện thoại và CCCD/Passport.
     */
    private static final String SELECT_HEADER_BASE =
            "SELECT dp.maDatPhong, dp.maKhachHang, dp.maNhanVien, dp.maBangGia, dp.ngayDat, dp.ngayNhanPhong, dp.ngayTraPhong, "
                    + "dp.soLuongPhong, dp.soNguoi, dp.tienCoc, dp.trangThai, "
                    + "kh.hoTen AS tenKhachHang, kh.soDienThoai AS soDienThoaiKhach, kh.cccdPassport AS cccdPassportKhach "
                    + "FROM DatPhong dp "
                    + "LEFT JOIN KhachHang kh ON dp.maKhachHang = kh.maKhachHang";

    /**
     * Lưu thông báo lỗi gần nhất để giao diện hoặc lớp gọi có thể lấy ra hiển thị.
     */
    private String lastErrorMessage = "";

    /**
     * DAO bảng giá dùng để lấy chi tiết giá đang áp dụng.
     */
    private final BangGiaDAO bangGiaDAO = new BangGiaDAO();

    /**
     * DAO ngày lễ dùng để xác định ngày lễ khi tính loại ngày và phụ thu.
     */
    private final NgayLeDAO ngayLeDAO = new NgayLeDAO();

    /**
     * Lấy thông báo lỗi gần nhất phát sinh trong DAO.
     *
     * @return nội dung lỗi gần nhất, rỗng nếu chưa có lỗi.
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Kiểm tra trạng thái phòng có thuộc nhóm bị khóa vận hành hay không.
     *
     * @param status trạng thái phòng cần kiểm tra.
     * @return true nếu phòng đang bảo trì, không hoạt động, ngừng hoạt động hoặc đang sửa.
     */
    public boolean isOperationallyBlockedRoomStatus(String status) {
        String value = status == null ? "" : status.trim();
        return "Bảo trì".equalsIgnoreCase(value)
                || "Không hoạt động".equalsIgnoreCase(value)
                || "Ngừng hoạt động".equalsIgnoreCase(value)
                || "Đang sửa".equalsIgnoreCase(value);
    }

    /**
     * Lấy trạng thái khóa vận hành hiện tại của phòng.
     *
     * Nếu phòng không ở trạng thái bị khóa vận hành, method trả về chuỗi rỗng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần kiểm tra.
     * @return trạng thái khóa vận hành nếu có, ngược lại trả về chuỗi rỗng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public String getOperationalBlockStatus(Connection con, int maPhong) throws SQLException {
        if (con == null || maPhong <= 0) {
            return "";
        }
        String currentStatus = loadCurrentRoomStatus(con, maPhong);
        return isOperationallyBlockedRoomStatus(currentStatus) ? currentStatus : "";
    }

    /**
     * Xác định trạng thái hiển thị của phòng dựa trên mã phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần xác định trạng thái.
     * @return trạng thái hiển thị của phòng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public String resolveDisplayRoomStatus(Connection con, int maPhong) throws SQLException {
        return resolveDisplayRoomStatus(con, maPhong, null);
    }

    /**
     * Xác định trạng thái hiển thị của phòng dựa trên trạng thái hiện tại,
     * tình trạng lưu trú, đặt phòng và thanh toán.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần xác định trạng thái.
     * @param currentStatus trạng thái hiện tại của phòng, có thể null hoặc rỗng.
     * @return trạng thái hiển thị sau khi xử lý.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public String resolveDisplayRoomStatus(Connection con, int maPhong, String currentStatus) throws SQLException {
        String normalizedCurrent = safeTrim(currentStatus);
        if (normalizedCurrent.isEmpty() && con != null && maPhong > 0) {
            normalizedCurrent = loadCurrentRoomStatus(con, maPhong);
        }
        if (con != null && maPhong > 0) {
            if (hasPendingPaymentForRoom(con, maPhong)) {
                return STATUS_WAIT_PAYMENT;
            }
            if (hasActiveStayForRoom(con, maPhong)) {
                return STATUS_ACTIVE;
            }
            if (hasBookedAssignmentForRoom(con, maPhong)) {
                return "Đã đặt";
            }
        }
        if (STATUS_WAIT_PAYMENT.equalsIgnoreCase(normalizedCurrent)
                || STATUS_CHECKED_OUT.equalsIgnoreCase(normalizedCurrent)) {
            return STATUS_WAIT_PAYMENT;
        }
        if (STATUS_ACTIVE.equalsIgnoreCase(normalizedCurrent)
                || "Đã check-in".equalsIgnoreCase(normalizedCurrent)
                || "Đang lưu trú".equalsIgnoreCase(normalizedCurrent)) {
            return STATUS_ACTIVE;
        }
        if ("Đã đặt".equalsIgnoreCase(normalizedCurrent)
                || "Đã xác nhận".equalsIgnoreCase(normalizedCurrent)
                || "Đã cọc".equalsIgnoreCase(normalizedCurrent)
                || STATUS_PENDING_CHECKIN.equalsIgnoreCase(normalizedCurrent)) {
            return "Đã đặt";
        }
        if (isOperationallyBlockedRoomStatus(normalizedCurrent)) {
            return "Bảo trì";
        }
        return "Hoạt động";
    }

    /**
     * Đảm bảo bảng ChiTietDatPhong có đủ cột checkInDuKien và checkOutDuKien.
     *
     * Nếu cột chưa tồn tại, method sẽ tự thêm cột và cập nhật giá trị mặc định
     * dựa trên ngày nhận/trả phòng của DatPhong.
     *
     * @param con kết nối cơ sở dữ liệu.
     */
    public void ensureDetailScheduleSchema(Connection con) {
        if (con == null) {
            return;
        }
        synchronized (DatPhongDAO.class) {
            if (detailScheduleSchemaEnsured) {
                return;
            }
            try (PreparedStatement stmt = con.prepareStatement(
                    "IF COL_LENGTH('dbo.ChiTietDatPhong', 'checkInDuKien') IS NULL "
                            + "BEGIN ALTER TABLE dbo.ChiTietDatPhong ADD checkInDuKien DATETIME2 NULL END "
                            + "IF COL_LENGTH('dbo.ChiTietDatPhong', 'checkOutDuKien') IS NULL "
                            + "BEGIN ALTER TABLE dbo.ChiTietDatPhong ADD checkOutDuKien DATETIME2 NULL END "
                            + "UPDATE ctdp "
                            + "SET checkInDuKien = COALESCE(ctdp.checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayNhanPhong AS DATETIME2))), "
                            + "    checkOutDuKien = COALESCE(ctdp.checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour() + ", CAST(dp.ngayTraPhong AS DATETIME2))) "
                            + "FROM dbo.ChiTietDatPhong ctdp "
                            + "JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong")) {
                stmt.execute();
                detailScheduleSchemaEnsured = true;
            } catch (SQLException e) {
                setLastError(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Đảm bảo bảng HoaDonChiTietDatPhongScope tồn tại.
     *
     * Bảng này dùng để lưu phạm vi chi tiết đặt phòng thuộc một hóa đơn,
     * hỗ trợ kiểm tra thanh toán theo từng chi tiết phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     */
    private void ensureInvoiceScopeSchema(Connection con) {
        if (con == null) {
            return;
        }
        synchronized (DatPhongDAO.class) {
            if (invoiceScopeSchemaEnsured) {
                return;
            }
            try (PreparedStatement stmt = con.prepareStatement(
                    "IF OBJECT_ID(N'dbo.HoaDonChiTietDatPhongScope', N'U') IS NULL "
                            + "BEGIN "
                            + "CREATE TABLE dbo.HoaDonChiTietDatPhongScope("
                            + "maHoaDon INT NOT NULL, "
                            + "maChiTietDatPhong INT NOT NULL, "
                            + "thuTu INT NOT NULL CONSTRAINT DF_HoaDonChiTietDatPhongScope_thuTu DEFAULT 0, "
                            + "ngayTao DATETIME NOT NULL CONSTRAINT DF_HoaDonChiTietDatPhongScope_ngayTao DEFAULT GETDATE(), "
                            + "CONSTRAINT PK_HoaDonChiTietDatPhongScope PRIMARY KEY (maHoaDon, maChiTietDatPhong)"
                            + ") "
                            + "END")) {
                stmt.execute();
                invoiceScopeSchemaEnsured = true;
            } catch (SQLException e) {
                setLastError(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Lấy toàn bộ danh sách đặt phòng.
     *
     * Mỗi đặt phòng sẽ được nạp thêm danh sách chi tiết đặt phòng,
     * sau đó chuẩn hóa một số thông tin tổng hợp trước khi trả về.
     *
     * @return danh sách đặt phòng, rỗng nếu không có dữ liệu hoặc có lỗi.
     */
    public List<DatPhong> getAll() {
        clearLastError();
        List<DatPhong> result = new ArrayList<DatPhong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        ensureDetailScheduleSchema(con);
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

    /**
     * Tìm đặt phòng theo mã đặt phòng.
     *
     * @param maDatPhong mã đặt phòng dạng chuỗi.
     * @return đối tượng DatPhong nếu tìm thấy, ngược lại trả về null.
     */
    public DatPhong findById(String maDatPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return null;
        }

        ensureDetailScheduleSchema(con);
        try {
            return findByIdInternal(con, id.intValue());
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm danh sách đặt phòng theo trạng thái.
     *
     * Nếu trạng thái truyền vào rỗng, method sẽ trả về toàn bộ danh sách đặt phòng.
     *
     * @param trangThai trạng thái đặt phòng cần lọc.
     * @return danh sách đặt phòng phù hợp.
     */
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

    /**
     * Thêm mới một đặt phòng cùng danh sách chi tiết đặt phòng.
     *
     * Toàn bộ thao tác được thực hiện trong một transaction:
     * - Thêm header DatPhong.
     * - Thêm danh sách ChiTietDatPhong.
     * - Cập nhật trạng thái các phòng được gán.
     *
     * @param datPhong thông tin đặt phòng cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insert(DatPhong datPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || datPhong == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu đặt phòng không hợp lệ.");
            return false;
        }

        ensureDetailScheduleSchema(con);
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

    /**
     * Cập nhật thông tin đặt phòng.
     *
     * Nếu booking chưa phát sinh lưu trú, method sẽ đồng bộ lại danh sách chi tiết đặt phòng
     * và làm mới trạng thái các phòng liên quan.
     *
     * @param datPhong thông tin đặt phòng cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean update(DatPhong datPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = datPhong == null ? null : parseIntOrNull(datPhong.getMaDatPhong());
        if (con == null || datPhong == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return false;
        }

        ensureDetailScheduleSchema(con);
        String sql = "UPDATE DatPhong SET maKhachHang = ?, maNhanVien = ?, maBangGia = ?, ngayDat = ?, ngayNhanPhong = ?, ngayTraPhong = ?, "
                + "soLuongPhong = ?, soNguoi = ?, tienCoc = ?, trangThai = ? WHERE maDatPhong = ?";

        try {
            con.setAutoCommit(false);
            List<Integer> roomIdsToRefresh = new ArrayList<Integer>(getAssignedRoomIds(con, id.intValue()));
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
                syncChiTietList(con, datPhong);
                roomIdsToRefresh.addAll(collectAssignedRoomIds(datPhong));
                refreshRoomStatuses(con, roomIdsToRefresh);
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

    /**
     * Xóa một đặt phòng theo mã đặt phòng.
     *
     * Chỉ cho phép xóa khi booking chưa phát sinh lưu trú.
     * Khi xóa thành công, các phòng liên quan sẽ được refresh lại trạng thái.
     *
     * @param maDatPhong mã đặt phòng cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean delete(String maDatPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return false;
        }

        ensureDetailScheduleSchema(con);
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
    /**
     * Lấy danh sách chi tiết đặt phòng theo mã đặt phòng.
     *
     * @param maDatPhong mã đặt phòng cần lấy chi tiết.
     * @return danh sách chi tiết đặt phòng, rỗng nếu mã không hợp lệ hoặc có lỗi kết nối.
     */
    public List<ChiTietDatPhong> getChiTietByMaDatPhong(String maDatPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return new ArrayList<ChiTietDatPhong>();
        }
        ensureDetailScheduleSchema(con);
        DatPhong temp = new DatPhong();
        temp.setMaDatPhong(String.valueOf(id.intValue()));
        return getChiTietByMaDatPhongInternal(con, temp);
    }

    /**
     * Cập nhật trạng thái của một đặt phòng.
     *
     * Sau khi cập nhật trạng thái booking, method sẽ cập nhật lại trạng thái phòng liên quan
     * theo trạng thái mới của booking.
     *
     * @param maDatPhong mã đặt phòng cần cập nhật.
     * @param trangThai trạng thái mới.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateTrangThai(String maDatPhong, String trangThai) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer id = parseIntOrNull(maDatPhong);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã đặt phòng không hợp lệ.");
            return false;
        }

        ensureDetailScheduleSchema(con);
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

    /**
     * Khôi phục một booking đã bị hủy.
     *
     * Trước khi khôi phục, method kiểm tra:
     * - Booking có tồn tại hay không.
     * - Booking hiện tại có đang ở trạng thái hủy hay không.
     * - Booking đã phát sinh lưu trú hay chưa.
     * - Các chi tiết phòng còn hợp lệ và không bị trùng lịch với booking khác.
     *
     * @param maDatPhong mã đặt phòng cần khôi phục.
     * @param trangThaiKhoiPhuc trạng thái muốn khôi phục về, nếu rỗng sẽ dùng STATUS_PENDING_CHECKIN.
     * @return true nếu khôi phục thành công, false nếu thất bại.
     */
    public boolean restoreCancelledBooking(String maDatPhong, String trangThaiKhoiPhuc) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer id = parseIntOrNull(maDatPhong);
        String targetStatus = safeTrim(trangThaiKhoiPhuc);
        if (con == null || id == null) {
            setLastError(con == null ? "KhÃ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u." : "MÃ£ Ä‘áº·t phÃ²ng khÃ´ng há»£p lá»‡.");
            return false;
        }
        ensureDetailScheduleSchema(con);
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
                setLastError("Booking nÃ y khÃ´ng á»Ÿ tráº¡ng thÃ¡i ÄÃ£ há»§y.");
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
                    setLastError("Khoáº£ng ngÃ y cá»§a booking khÃ´ng há»£p lá»‡, khÃ´ng thá»ƒ khÃ´i phá»¥c.");
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

    /**
     * Kiểm tra xung đột phòng theo ngày nhận và ngày trả dạng LocalDate.
     *
     * Method này chuyển LocalDate sang LocalDateTime bằng mốc giờ mặc định,
     * sau đó gọi overload xử lý chính.
     *
     * @param maPhong mã phòng cần kiểm tra.
     * @param ngayNhanPhong ngày nhận phòng.
     * @param ngayTraPhong ngày trả phòng.
     * @param excludeMaDatPhong mã đặt phòng cần bỏ qua khi kiểm tra, dùng khi cập nhật/khôi phục.
     * @return thông tin xung đột nếu có, ngược lại trả về null.
     */
    public DatPhongConflictInfo findRoomConflict(int maPhong, LocalDate ngayNhanPhong, LocalDate ngayTraPhong, Integer excludeMaDatPhong) {
        return findRoomConflict(
                maPhong,
                toDetailScheduleDateTime(ngayNhanPhong),
                toDetailScheduleDateTime(ngayTraPhong),
                excludeMaDatPhong
        );
    }

    /**
     * Kiểm tra xung đột phòng theo khoảng thời gian nhận/trả phòng.
     *
     * Phòng được xem là xung đột nếu có booking hoặc lưu trú khác
     * giữ cùng phòng trong khoảng thời gian giao nhau.
     *
     * @param maPhong mã phòng cần kiểm tra.
     * @param ngayNhanPhong thời gian nhận phòng.
     * @param ngayTraPhong thời gian trả phòng.
     * @param excludeMaDatPhong mã đặt phòng cần bỏ qua khi kiểm tra, có thể null.
     * @return thông tin xung đột nếu có, ngược lại trả về null.
     */
    public DatPhongConflictInfo findRoomConflict(int maPhong, LocalDateTime ngayNhanPhong, LocalDateTime ngayTraPhong, Integer excludeMaDatPhong) {
        clearLastError();
        Connection con = getReadyConnection();
        if (con == null) {
            return null;
        }
        ensureDetailScheduleSchema(con);
        if (maPhong <= 0 || ngayNhanPhong == null || ngayTraPhong == null || !ngayTraPhong.isAfter(ngayNhanPhong)) {
            return null;
        }

        String detailCheckInExpr = buildDetailCheckInExpr("ctdp", "dp");
        String detailCheckOutExpr = buildDetailCheckOutExpr("ctdp", "dp");
        String conflictCheckInExpr = "COALESCE(lt.checkIn, " + detailCheckInExpr + ")";
        String conflictCheckOutExpr = "COALESCE(lt.checkOut, " + detailCheckOutExpr + ")";
        String paidInvoiceExistsSql = "EXISTS ("
                + "SELECT 1 FROM dbo.HoaDon hdDone "
                + "WHERE ISNULL(hdDone.trangThai, N'') = N'Đã thanh toán' "
                + "AND ((hdDone.maDatPhong = dp.maDatPhong AND hdDone.maChiTietDatPhong IS NULL) "
                + "     OR hdDone.maChiTietDatPhong = ctdp.maChiTietDatPhong)"
                + ")";
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 1 dp.maDatPhong, ctdp.maChiTietDatPhong, ")
                .append("ISNULL(lt.maLuuTru, 0) AS maLuuTru, ")
                .append("ISNULL(kh.hoTen, N'Khách chưa xác định') AS tenKhachHang, ")
                .append("ISNULL(p.soPhong, CAST(ctdp.maPhong AS NVARCHAR(20))) AS soPhong, ")
                .append(conflictCheckInExpr).append(" AS ngayNhanPhong, ")
                .append(conflictCheckOutExpr).append(" AS ngayTraPhong, ")
                .append("CASE ")
                .append(" WHEN lt.maLuuTru IS NOT NULL AND lt.checkOut IS NULL THEN N'Đang ở' ")
                .append(" WHEN lt.maLuuTru IS NOT NULL AND ISNULL(dp.trangThai, N'') IN ").append(BOOKING_BLOCKING_STATUS_SQL).append(" THEN N'Đang ở' ")
                .append(" ELSE dp.trangThai ")
                .append("END AS trangThai ")
                .append("FROM ChiTietDatPhong ctdp ")
                .append("JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong ")
                .append("LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang ")
                .append("LEFT JOIN Phong p ON p.maPhong = ctdp.maPhong ")
                .append("LEFT JOIN LuuTru lt ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong ")
                .append("WHERE ctdp.maPhong = ? ")
                .append("AND ").append(conflictCheckInExpr).append(" < ? ")
                .append("AND ").append(conflictCheckOutExpr).append(" > ? ");
        if (excludeMaDatPhong != null && excludeMaDatPhong.intValue() > 0) {
            sql.append("AND dp.maDatPhong <> ? ");
        }
        sql.append("AND (")
                .append(" (ISNULL(dp.trangThai, N'') IN ").append(BOOKING_BLOCKING_STATUS_SQL)
                .append(" AND NOT ").append(paidInvoiceExistsSql).append(") ")
                .append(" OR (lt.maLuuTru IS NOT NULL AND (lt.checkOut IS NULL OR lt.checkOut > ?))")
                .append(") ")
                .append("ORDER BY CASE WHEN lt.maLuuTru IS NOT NULL AND lt.checkOut IS NULL THEN 0 ELSE 1 END, ")
                .append(conflictCheckInExpr)
                .append(" ASC, dp.maDatPhong DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int index = 1;
            stmt.setInt(index++, maPhong);
            stmt.setTimestamp(index++, Timestamp.valueOf(ngayTraPhong));
            stmt.setTimestamp(index++, Timestamp.valueOf(ngayNhanPhong));
            if (excludeMaDatPhong != null && excludeMaDatPhong.intValue() > 0) {
                stmt.setInt(index++, excludeMaDatPhong.intValue());
            }
            stmt.setTimestamp(index, Timestamp.valueOf(ngayNhanPhong));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DatPhongConflictInfo info = new DatPhongConflictInfo();
                    info.setMaDatPhong(rs.getInt("maDatPhong"));
                    info.setMaChiTietDatPhong(rs.getInt("maChiTietDatPhong"));
                    info.setMaLuuTru(rs.getInt("maLuuTru"));
                    info.setTenKhachHang(safeTrim(rs.getString("tenKhachHang")));
                    info.setSoPhong(safeTrim(rs.getString("soPhong")));
                    info.setNgayNhanPhongDateTime(toLocalDateTime(rs.getTimestamp("ngayNhanPhong")));
                    info.setNgayTraPhongDateTime(toLocalDateTime(rs.getTimestamp("ngayTraPhong")));
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
    /**
     * Lấy danh sách phòng còn khả dụng theo khoảng ngày dạng LocalDate.
     *
     * Method này chuyển LocalDate sang LocalDateTime bằng mốc giờ mặc định,
     * sau đó gọi overload xử lý chính.
     *
     * @param ngayNhanPhong ngày nhận phòng.
     * @param ngayTraPhong ngày trả phòng.
     * @param excludeMaDatPhong mã đặt phòng cần bỏ qua khi kiểm tra, có thể null.
     * @param includeMaPhong mã phòng cần luôn đưa vào điều kiện xem xét, có thể null.
     * @return danh sách phòng còn khả dụng.
     */
    public List<AvailableRoomInfo> getAvailableRooms(LocalDate ngayNhanPhong, LocalDate ngayTraPhong, Integer excludeMaDatPhong, Integer includeMaPhong) {
        return getAvailableRooms(
                toDetailScheduleDateTime(ngayNhanPhong),
                toDetailScheduleDateTime(ngayTraPhong),
                excludeMaDatPhong,
                includeMaPhong
        );
    }

    /**
     * Lấy danh sách phòng còn khả dụng theo khoảng thời gian nhận/trả phòng.
     *
     * Phòng được đưa vào kết quả khi:
     * - Phòng đang ở trạng thái có thể sử dụng hoặc là phòng được include.
     * - Không có lưu trú đang hoạt động xung đột.
     * - Không có booking khác đang giữ phòng trong khoảng thời gian giao nhau.
     *
     * @param ngayNhanPhong thời gian nhận phòng.
     * @param ngayTraPhong thời gian trả phòng.
     * @param excludeMaDatPhong mã đặt phòng cần bỏ qua khi kiểm tra, có thể null.
     * @param includeMaPhong mã phòng cần đưa vào khi chỉnh sửa booking, có thể null.
     * @return danh sách phòng còn khả dụng.
     */
    public List<AvailableRoomInfo> getAvailableRooms(LocalDateTime ngayNhanPhong, LocalDateTime ngayTraPhong, Integer excludeMaDatPhong, Integer includeMaPhong) {
        clearLastError();
        List<AvailableRoomInfo> result = new ArrayList<AvailableRoomInfo>();
        Connection con = getReadyConnection();
        if (con == null) {
            return result;
        }
        ensureDetailScheduleSchema(con);
        if (ngayNhanPhong == null || ngayTraPhong == null || !ngayTraPhong.isAfter(ngayNhanPhong)) {
            return result;
        }

        String detailCheckInExpr = buildDetailCheckInExpr("ctdp", "dp");
        String detailCheckOutExpr = buildDetailCheckOutExpr("ctdp", "dp");
        String paidInvoiceExistsSql = "EXISTS ("
                + "SELECT 1 FROM dbo.HoaDon hdDone "
                + "WHERE ISNULL(hdDone.trangThai, N'') = N'Đã thanh toán' "
                + "AND ((hdDone.maDatPhong = dp.maDatPhong AND hdDone.maChiTietDatPhong IS NULL) "
                + "     OR hdDone.maChiTietDatPhong = ctdp.maChiTietDatPhong)"
                + ")";
        String sql = "SELECT p.maPhong, p.soPhong, p.tang, p.trangThai, p.sucChuaToiDa, lp.maLoaiPhong, lp.tenLoaiPhong, lp.giaThamChieu " +
                "FROM dbo.Phong p " +
                "JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong " +
                "WHERE (p.trangThai IN " + ROOM_READY_STATUS_SQL + " OR (? IS NOT NULL AND p.maPhong = ?)) " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM dbo.LuuTru ltActive " +
                "    LEFT JOIN dbo.DatPhong dpActive ON dpActive.maDatPhong = ltActive.maDatPhong " +
                "    OUTER APPLY (SELECT TOP 1 ctdpActive.maChiTietDatPhong " +
                "                 FROM dbo.ChiTietDatPhong ctdpActive " +
                "                 WHERE ctdpActive.maChiTietDatPhong = ltActive.maChiTietDatPhong) activeDetail " +
                "    WHERE ltActive.maPhong = p.maPhong " +
                "      AND ltActive.checkOut IS NULL " +
                "      AND (? IS NULL OR ltActive.maDatPhong IS NULL OR ltActive.maDatPhong <> ?) " +
                "      AND (ltActive.maDatPhong IS NULL OR ISNULL(dpActive.trangThai, N'') IN " + BOOKING_BLOCKING_STATUS_SQL + ") " +
                "      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDon hdDone " +
                "                      WHERE ISNULL(hdDone.trangThai, N'') = N'Đã thanh toán' " +
                "                        AND ((hdDone.maDatPhong = ltActive.maDatPhong AND hdDone.maChiTietDatPhong IS NULL) " +
                "                             OR hdDone.maChiTietDatPhong = activeDetail.maChiTietDatPhong)) " +
                ") " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 " +
                "    FROM dbo.ChiTietDatPhong ctdp " +
                "    JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                "    OUTER APPLY (SELECT TOP 1 lt.maLuuTru, lt.checkOut " +
                "                 FROM dbo.LuuTru lt " +
                "                 WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "                 ORDER BY CASE WHEN lt.checkOut IS NULL THEN 0 ELSE 1 END, COALESCE(lt.checkOut, lt.checkIn) DESC, lt.maLuuTru DESC) latestLt " +
                "    WHERE ctdp.maPhong = p.maPhong " +
                "      AND (? IS NULL OR dp.maDatPhong <> ?) " +
                "      AND ISNULL(dp.trangThai, N'') IN " + BOOKING_BLOCKING_STATUS_SQL + " " +
                "      AND NOT " + paidInvoiceExistsSql + " " +
                "      AND " + detailCheckInExpr + " < ? " +
                "      AND " + detailCheckOutExpr + " > ? " +
                "      AND (latestLt.maLuuTru IS NULL OR latestLt.checkOut IS NULL OR latestLt.checkOut > ? OR ISNULL(dp.trangThai, N'') = N'Chờ thanh toán') " +
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
            stmt.setTimestamp(index++, Timestamp.valueOf(ngayTraPhong));
            stmt.setTimestamp(index++, Timestamp.valueOf(ngayNhanPhong));
            stmt.setTimestamp(index, Timestamp.valueOf(ngayNhanPhong));

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

    /**
     * Lấy danh sách chi tiết đặt phòng theo header đặt phòng.
     *
     * Method này được dùng nội bộ khi load một booking đầy đủ.
     * Dữ liệu trả về bao gồm thông tin phòng, loại phòng, bảng giá,
     * lịch check-in/check-out dự kiến và trạng thái từng chi tiết.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param header thông tin header của đặt phòng.
     * @return danh sách chi tiết đặt phòng.
     */
    private List<ChiTietDatPhong> getChiTietByMaDatPhongInternal(Connection con, DatPhong header) {
        List<ChiTietDatPhong> details = new ArrayList<ChiTietDatPhong>();
        Integer bookingId = parseIntOrNull(header.getMaDatPhong());
        if (con == null || bookingId == null) {
            return details;
        }

        ensureDetailScheduleSchema(con);
        int detailCount = countDetails(con, bookingId.intValue());
        String detailCheckInExpr = buildDetailCheckInExpr("ctdp", "dp");
        String detailCheckOutExpr = buildDetailCheckOutExpr("ctdp", "dp");
        String sql = "SELECT ctdp.maChiTietDatPhong, ctdp.maDatPhong, ctdp.maPhong, ctdp.soNguoi, ctdp.giaPhong, ctdp.thanhTien, "
                + "dp.ngayNhanPhong, dp.ngayTraPhong, "
                + detailCheckInExpr + " AS checkInDuKien, "
                + detailCheckOutExpr + " AS checkOutDuKien, "
                + "ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, dp.tienCoc AS tienCocHeader, dp.trangThai AS trangThaiDatPhong, "
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
                    Timestamp detailCheckIn = rs.getTimestamp("checkInDuKien");
                    Timestamp detailCheckOut = rs.getTimestamp("checkOutDuKien");
                    detail.setCheckInDuKien(detailCheckIn == null ? toLocalDate(rs.getDate("ngayNhanPhong")) : detailCheckIn.toLocalDateTime().toLocalDate());
                    detail.setCheckOutDuKien(detailCheckOut == null ? toLocalDate(rs.getDate("ngayTraPhong")) : detailCheckOut.toLocalDateTime().toLocalDate());
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

    /**
     * Tìm đặt phòng theo mã bằng connection có sẵn.
     *
     * Method này load cả header và chi tiết đặt phòng,
     * sau đó chuẩn hóa thông tin tổng hợp của booking.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @return đối tượng DatPhong nếu tìm thấy, ngược lại trả về null.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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
    /**
     * Tạo thông báo lỗi khi không thể khôi phục booking do bị trùng phòng.
     *
     * @param conflictInfo thông tin xung đột phòng.
     * @return chuỗi thông báo lỗi.
     */
    private String buildRestoreConflictMessage(DatPhongConflictInfo conflictInfo) {
        if (conflictInfo == null) {
            return "Má»™t hoáº·c nhiá»u phÃ²ng trong booking Ä‘Ã£ khÃ´ng cÃ²n trá»‘ng trong khoáº£ng ngÃ y cÅ©. KhÃ´ng thá»ƒ khÃ´i phá»¥c tá»± Ä‘á»™ng.";
        }
        return "PhÃ²ng " + defaultIfEmpty(conflictInfo.getSoPhong(), "-")
                + " Ä‘Ã£ khÃ´ng cÃ²n trá»‘ng trong khoáº£ng " + formatDateValue(conflictInfo.getNgayNhanPhong())
                + " - " + formatDateValue(conflictInfo.getNgayTraPhong())
                + " do trÃ¹ng vá»›i booking DP" + conflictInfo.getMaDatPhong()
                + " (" + defaultIfEmpty(conflictInfo.getTrangThai(), "-") + ").";
    }

    /**
     * Chuyển LocalDate thành chuỗi hiển thị đơn giản.
     *
     * @param value ngày cần chuyển.
     * @return chuỗi ngày hoặc "-" nếu value null.
     */
    private String formatDateValue(LocalDate value) {
        return value == null ? "-" : value.toString();
    }

    /**
     * Ánh xạ dữ liệu header đặt phòng từ ResultSet sang đối tượng DatPhong.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu đặt phòng.
     * @return đối tượng DatPhong sau khi ánh xạ.
     * @throws SQLException nếu xảy ra lỗi khi đọc dữ liệu từ ResultSet.
     */
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

    /**
     * Gán dữ liệu header đặt phòng vào PreparedStatement.
     *
     * Method này dùng chung cho thêm mới và cập nhật DatPhong.
     *
     * @param stmt PreparedStatement cần gán tham số.
     * @param datPhong dữ liệu đặt phòng.
     * @throws SQLException nếu xảy ra lỗi khi gán tham số.
     */
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

    /**
     * Thêm danh sách chi tiết đặt phòng của một booking.
     *
     * Mỗi chi tiết sẽ được xác định bảng giá phù hợp, áp dụng giá phòng,
     * tính thành tiền và lưu lịch check-in/check-out dự kiến.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param datPhong booking chứa danh sách chi tiết cần thêm.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void insertChiTietList(Connection con, DatPhong datPhong) throws SQLException {
        List<ChiTietDatPhong> details = datPhong.getChiTietDatPhongs();
        if (details == null || details.isEmpty()) {
            return;
        }
        ensureDetailScheduleSchema(con);

        String sql = "INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien, checkInDuKien, checkOutDuKien) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (ChiTietDatPhong detail : details) {
                String detailBangGia = resolveBangGiaForDetail(con, detail, datPhong.getMaBangGia());
                applyResolvedRoomRate(detail, detailBangGia, datPhong.getNgayNhanPhong(), datPhong.getNgayTraPhong());
                stmt.setInt(1, Integer.parseInt(datPhong.getMaDatPhong()));
                setNullableInt(stmt, 2, detail.getMaPhong());
                stmt.setInt(3, detail.getSoNguoi() <= 0 ? 1 : detail.getSoNguoi());
                stmt.setDouble(4, detail.getGiaApDung());
                stmt.setDouble(5, calculateThanhTien(detail));
                stmt.setTimestamp(6, toDetailScheduleTimestamp(detail.getCheckInDuKien() == null ? datPhong.getNgayNhanPhong() : detail.getCheckInDuKien()));
                stmt.setTimestamp(7, toDetailScheduleTimestamp(detail.getCheckOutDuKien() == null ? datPhong.getNgayTraPhong() : detail.getCheckOutDuKien()));
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

    /**
     * Đồng bộ danh sách chi tiết đặt phòng khi cập nhật booking.
     *
     * Method xử lý:
     * - Cập nhật chi tiết đã tồn tại.
     * - Thêm chi tiết mới.
     * - Xóa chi tiết cũ không còn trong danh sách mới.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param datPhong booking chứa danh sách chi tiết cần đồng bộ.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void syncChiTietList(Connection con, DatPhong datPhong) throws SQLException {
        if (con == null || datPhong == null) {
            return;
        }
        List<ChiTietDatPhong> details = datPhong.getChiTietDatPhongs();
        if (details == null) {
            return;
        }

        ensureDetailScheduleSchema(con);
        Integer bookingId = parseIntOrNull(datPhong.getMaDatPhong());
        if (bookingId == null) {
            return;
        }

        LinkedHashSet<Integer> existingDetailIds = new LinkedHashSet<Integer>();
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT maChiTietDatPhong FROM ChiTietDatPhong WHERE maDatPhong = ?")) {
            stmt.setInt(1, bookingId.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    existingDetailIds.add(Integer.valueOf(rs.getInt("maChiTietDatPhong")));
                }
            }
        }

        LinkedHashSet<Integer> retainedDetailIds = new LinkedHashSet<Integer>();
        String updateSql = "UPDATE ChiTietDatPhong "
                + "SET maPhong = ?, soNguoi = ?, giaPhong = ?, thanhTien = ?, checkInDuKien = ?, checkOutDuKien = ? "
                + "WHERE maChiTietDatPhong = ? AND maDatPhong = ?";
        String insertSql = "INSERT INTO ChiTietDatPhong(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien, checkInDuKien, checkOutDuKien) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement updateStmt = con.prepareStatement(updateSql);
             PreparedStatement insertStmt = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            for (ChiTietDatPhong detail : details) {
                if (detail == null) {
                    continue;
                }

                detail.setMaDatPhong(datPhong.getMaDatPhong());
                String detailBangGia = resolveBangGiaForDetail(con, detail, datPhong.getMaBangGia());
                applyResolvedRoomRate(detail, detailBangGia, datPhong.getNgayNhanPhong(), datPhong.getNgayTraPhong());

                Integer detailId = parseIntOrNull(detail.getMaChiTietDatPhong());
                Timestamp checkInDuKien = toDetailScheduleTimestamp(
                        detail.getCheckInDuKien() == null ? datPhong.getNgayNhanPhong() : detail.getCheckInDuKien());
                Timestamp checkOutDuKien = toDetailScheduleTimestamp(
                        detail.getCheckOutDuKien() == null ? datPhong.getNgayTraPhong() : detail.getCheckOutDuKien());

                if (detailId != null && existingDetailIds.contains(detailId)) {
                    setNullableInt(updateStmt, 1, detail.getMaPhong());
                    updateStmt.setInt(2, detail.getSoNguoi() <= 0 ? 1 : detail.getSoNguoi());
                    updateStmt.setDouble(3, detail.getGiaApDung());
                    updateStmt.setDouble(4, calculateThanhTien(detail));
                    updateStmt.setTimestamp(5, checkInDuKien);
                    updateStmt.setTimestamp(6, checkOutDuKien);
                    updateStmt.setInt(7, detailId.intValue());
                    updateStmt.setInt(8, bookingId.intValue());
                    updateStmt.executeUpdate();
                    retainedDetailIds.add(detailId);
                    continue;
                }

                insertStmt.setInt(1, bookingId.intValue());
                setNullableInt(insertStmt, 2, detail.getMaPhong());
                insertStmt.setInt(3, detail.getSoNguoi() <= 0 ? 1 : detail.getSoNguoi());
                insertStmt.setDouble(4, detail.getGiaApDung());
                insertStmt.setDouble(5, calculateThanhTien(detail));
                insertStmt.setTimestamp(6, checkInDuKien);
                insertStmt.setTimestamp(7, checkOutDuKien);
                insertStmt.executeUpdate();

                try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newDetailId = rs.getInt(1);
                        detail.setMaChiTietDatPhong(String.valueOf(newDetailId));
                        retainedDetailIds.add(Integer.valueOf(newDetailId));
                    }
                }
            }
        }

        if (existingDetailIds.isEmpty()) {
            return;
        }
        try (PreparedStatement deleteStmt = con.prepareStatement(
                "DELETE FROM ChiTietDatPhong WHERE maChiTietDatPhong = ? AND maDatPhong = ?")) {
            for (Integer existingDetailId : existingDetailIds) {
                if (existingDetailId == null || retainedDetailIds.contains(existingDetailId)) {
                    continue;
                }
                deleteStmt.setInt(1, existingDetailId.intValue());
                deleteStmt.setInt(2, bookingId.intValue());
                deleteStmt.executeUpdate();
            }
        }
    }
    /**
     * Xóa toàn bộ chi tiết đặt phòng theo mã đặt phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần xóa chi tiết.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void deleteAllChiTietByMaDatPhong(Connection con, int maDatPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement("DELETE FROM ChiTietDatPhong WHERE maDatPhong = ?")) {
            stmt.setInt(1, maDatPhong);
            stmt.executeUpdate();
        }
    }

    /**
     * Cập nhật lại trạng thái các phòng được gán trong booking.
     *
     * Tham số roomStatus được giữ lại theo chữ ký method hiện tại,
     * nhưng trạng thái thực tế được refresh bằng refreshRoomStatuses().
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param datPhong booking chứa danh sách phòng được gán.
     * @param roomStatus trạng thái phòng theo booking.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Lấy danh sách mã phòng đã được gán cho một booking.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @return danh sách mã phòng đã gán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Thu thập danh sách mã phòng trong đối tượng DatPhong.
     *
     * @param datPhong booking cần lấy danh sách phòng.
     * @return danh sách mã phòng hợp lệ.
     */
    private List<Integer> collectAssignedRoomIds(DatPhong datPhong) {
        List<Integer> roomIds = new ArrayList<Integer>();
        if (datPhong == null || datPhong.getChiTietDatPhongs() == null) {
            return roomIds;
        }
        for (ChiTietDatPhong detail : datPhong.getChiTietDatPhongs()) {
            if (detail == null) {
                continue;
            }
            Integer roomId = parseIntOrNull(detail.getMaPhong());
            if (roomId != null) {
                roomIds.add(roomId);
            }
        }
        return roomIds;
    }

    /**
     * Refresh lại trạng thái phòng sau khi booking bị xóa hoặc hủy gán phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param roomIds danh sách phòng cần refresh.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void releaseRoomsIfBooked(Connection con, List<Integer> roomIds) throws SQLException {
        if (roomIds == null || roomIds.isEmpty()) {
            return;
        }
        refreshRoomStatuses(con, roomIds);
    }

    /**
     * Refresh trạng thái của nhiều phòng.
     *
     * Trạng thái phòng được xác định lại dựa trên lưu trú, booking giữ phòng,
     * hóa đơn chờ thanh toán và trạng thái hiện tại của phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param roomIds danh sách mã phòng cần refresh.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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
                String currentStatus = loadCurrentRoomStatus(con, roomId.intValue());
                if (isOperationallyBlockedRoomStatus(currentStatus)) {
                    continue;
                }
                String status = normalizeResolvedRoomStatus(resolveOperationalRoomStatusV2(con, roomId.intValue()), currentStatus);
                if (status.isEmpty()) {
                    continue;
                }
                stmt.setString(1, status);
                stmt.setInt(2, roomId.intValue());
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Chuẩn hóa trạng thái phòng sau khi hệ thống tự xác định lại.
     *
     * Method giúp giữ trạng thái đặc biệt như bảo trì,
     * đồng thời xử lý một số trường hợp chuyển trạng thái chờ thanh toán/dọn dẹp.
     *
     * @param resolvedStatus trạng thái vừa được hệ thống xác định.
     * @param currentStatus trạng thái hiện tại của phòng trong database.
     * @return trạng thái cuối cùng cần lưu.
     */
    private String normalizeResolvedRoomStatus(String resolvedStatus, String currentStatus) {
        String normalizedResolved = safeTrim(resolvedStatus);
        String normalizedCurrent = safeTrim(currentStatus);
        if (isOperationallyBlockedRoomStatus(normalizedCurrent)) {
            return normalizedCurrent;
        }
        if ("Ch\u1edd thanh to\u00e1n".equalsIgnoreCase(normalizedCurrent)
                && "D\u1ecdn d\u1eb9p".equalsIgnoreCase(normalizedResolved)) {
            return "S\u1eb5n s\u00e0ng";
        }
        return normalizedResolved;
    }

    /**
     * Refresh trạng thái của một phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần refresh.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public void refreshRoomStatus(Connection con, int maPhong) throws SQLException {
        if (maPhong <= 0) {
            return;
        }
        List<Integer> roomIds = new ArrayList<Integer>();
        roomIds.add(Integer.valueOf(maPhong));
        refreshRoomStatuses(con, roomIds);
    }

    /**
     * Xác định trạng thái vận hành của phòng theo phiên bản cũ.
     *
     * Method này dựa trên trạng thái hiện tại, lưu trú đang ở và booking giữ phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần xác định trạng thái.
     * @return trạng thái vận hành của phòng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private String resolveOperationalRoomStatus(Connection con, int maPhong) throws SQLException {
        String currentStatus = loadCurrentRoomStatus(con, maPhong);
        if (isOperationallyBlockedRoomStatus(currentStatus)) {
            return currentStatus;
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

    /**
     * Xác định trạng thái vận hành của phòng theo phiên bản mới.
     *
     * Method này xét thêm trạng thái chờ thanh toán, dọn dẹp, sẵn sàng và trống.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần xác định trạng thái.
     * @return trạng thái vận hành của phòng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private String resolveOperationalRoomStatusV2(Connection con, int maPhong) throws SQLException {
        String currentStatus = loadCurrentRoomStatus(con, maPhong);
        if (isOperationallyBlockedRoomStatus(currentStatus)) {
            return currentStatus;
        }
        if (hasActiveStayForRoom(con, maPhong)) {
            return "Đang ở";
        }
        if (hasBookedAssignmentForRoom(con, maPhong)) {
            return "Đã đặt";
        }
        if (hasPendingPaymentForRoom(con, maPhong)) {
            return "Chờ thanh toán";
        }
        if ("Dọn dẹp".equalsIgnoreCase(currentStatus) || "Dọn phòng".equalsIgnoreCase(currentStatus)) {
            return "Dọn dẹp";
        }
        if ("Chờ thanh toán".equalsIgnoreCase(currentStatus)) {
            return "Dọn dẹp";
        }
        if ("Sẵn sàng".equalsIgnoreCase(currentStatus)) {
            return "Sẵn sàng";
        }
        if ("Trống".equalsIgnoreCase(currentStatus)) {
            return "Trống";
        }
        return "Hoạt động";
    }

    /**
     * Lấy trạng thái hiện tại của phòng từ database.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần lấy trạng thái.
     * @return trạng thái phòng, hoặc chuỗi rỗng nếu không tìm thấy.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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
    /**
     * Kiểm tra phòng có lưu trú đang hoạt động hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần kiểm tra.
     * @return true nếu phòng đang có lưu trú chưa check-out.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasActiveStayForRoom(Connection con, int maPhong) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT COUNT(1) FROM dbo.LuuTru WHERE maPhong = ? AND checkOut IS NULL")) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Kiểm tra phòng có đang được giữ bởi booking chưa phát sinh lưu trú hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần kiểm tra.
     * @return true nếu phòng đang được gán cho booking chưa bị hủy và chưa có lưu trú.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasBookedAssignmentForRoom(Connection con, int maPhong) throws SQLException {
        String sql = "SELECT COUNT(1) "
                + "FROM dbo.ChiTietDatPhong ctdp "
                + "JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong "
                + "WHERE ctdp.maPhong = ? "
                + "AND ISNULL(dp.trangThai, N'') NOT IN (N'Đã hủy', N'Hủy booking') "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Kiểm tra phòng có hóa đơn/lưu trú mới nhất đang chờ thanh toán hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần kiểm tra.
     * @return true nếu phòng có trạng thái liên quan đến chờ thanh toán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasPendingPaymentForRoom(Connection con, int maPhong) throws SQLException {
        if (con != null) {
            return hasUnpaidInvoiceForLatestClosedStay(con, maPhong);
        }
        String sql = "WITH latestClosedStay AS ("
                + "    SELECT lt.maChiTietDatPhong, lt.maDatPhong, lt.checkOut, "
                + "           ROW_NUMBER() OVER (ORDER BY lt.checkOut DESC, lt.maLuuTru DESC) AS rn "
                + "    FROM dbo.LuuTru lt "
                + "    WHERE lt.maPhong = ? AND lt.checkOut IS NOT NULL"
                + ") "
                + "SELECT COUNT(1) "
                + "FROM latestClosedStay lcs "
                + "LEFT JOIN dbo.HoaDon hdRoom ON hdRoom.maChiTietDatPhong = lcs.maChiTietDatPhong "
                + "LEFT JOIN dbo.HoaDon hdBooking ON hdBooking.maDatPhong = lcs.maDatPhong AND hdBooking.maChiTietDatPhong IS NULL "
                + "LEFT JOIN dbo.DatPhong dp ON dp.maDatPhong = lcs.maDatPhong "
                + "WHERE lcs.rn = 1 "
                + "AND ("
                + "     ISNULL(dp.trangThai, N'') IN (N'Chờ thanh toán', N'Đã check-out') "
                + "  OR (hdRoom.maHoaDon IS NOT NULL AND ISNULL(hdRoom.trangThai, N'Chờ thanh toán') <> N'Đã thanh toán') "
                + "  OR (hdBooking.maHoaDon IS NOT NULL AND ISNULL(hdBooking.trangThai, N'Chờ thanh toán') <> N'Đã thanh toán')"
                + ")";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Kiểm tra một booking đã được thanh toán đầy đủ hay chưa.
     *
     * Booking chỉ được xem là đã thanh toán đầy đủ khi:
     * - Booking đã sẵn sàng thanh toán.
     * - Tất cả chi tiết đặt phòng đều có hóa đơn đã thanh toán.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần kiểm tra.
     * @return true nếu booking đã thanh toán đầy đủ.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    public boolean isBookingFullyPaid(Connection con, int maDatPhong) throws SQLException {
        if (con == null || maDatPhong <= 0 || !isBookingReadyForPayment(con, maDatPhong)) {
            return false;
        }
        ensureInvoiceScopeSchema(con);
        List<Integer> detailIds = new ArrayList<Integer>();
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = ?")) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    detailIds.add(Integer.valueOf(rs.getInt("maChiTietDatPhong")));
                }
            }
        }
        if (detailIds.isEmpty()) {
            return false;
        }
        for (Integer detailId : detailIds) {
            if (detailId == null || detailId.intValue() <= 0) {
                return false;
            }
            if (!hasPaidInvoiceForDetail(con, maDatPhong, detailId.intValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Kiểm tra booking đã đủ điều kiện chuyển sang giai đoạn thanh toán hay chưa.
     *
     * Điều kiện:
     * - Có ít nhất một chi tiết đặt phòng.
     * - Không còn lưu trú nào đang ở.
     * - Tất cả chi tiết đặt phòng đều đã có lưu trú check-out.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần kiểm tra.
     * @return true nếu booking đã sẵn sàng thanh toán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean isBookingReadyForPayment(Connection con, int maDatPhong) throws SQLException {
        if (con == null || maDatPhong <= 0) {
            return false;
        }
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT CASE WHEN "
                        + "EXISTS (SELECT 1 FROM dbo.ChiTietDatPhong ctdp WHERE ctdp.maDatPhong = ?) "
                        + "AND NOT EXISTS (SELECT 1 FROM dbo.LuuTru lt WHERE lt.maDatPhong = ? AND lt.checkOut IS NULL) "
                        + "AND NOT EXISTS ("
                        + "    SELECT 1 FROM dbo.ChiTietDatPhong ctdp "
                        + "    WHERE ctdp.maDatPhong = ? "
                        + "      AND NOT EXISTS ("
                        + "          SELECT 1 FROM dbo.LuuTru ltDone "
                        + "          WHERE ltDone.maChiTietDatPhong = ctdp.maChiTietDatPhong "
                        + "            AND ltDone.checkOut IS NOT NULL"
                        + "      )"
                        + ") "
                        + "THEN 1 ELSE 0 END")) {
            stmt.setInt(1, maDatPhong);
            stmt.setInt(2, maDatPhong);
            stmt.setInt(3, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    /**
     * Kiểm tra một chi tiết đặt phòng đã có hóa đơn thanh toán hay chưa.
     *
     * Method hỗ trợ nhiều trường hợp:
     * - Hóa đơn gắn trực tiếp với maChiTietDatPhong.
     * - Hóa đơn gắn qua bảng scope.
     * - Hóa đơn thanh toán toàn booking không có scope chi tiết.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @param maChiTietDatPhong mã chi tiết đặt phòng.
     * @return true nếu chi tiết đã được thanh toán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasPaidInvoiceForDetail(Connection con, int maDatPhong, int maChiTietDatPhong) throws SQLException {
        if (con == null || maChiTietDatPhong <= 0) {
            return false;
        }
        ensureInvoiceScopeSchema(con);
        String sql = "SELECT CASE WHEN "
                + "EXISTS (SELECT 1 FROM dbo.HoaDon hd WHERE hd.maChiTietDatPhong = ? AND ISNULL(hd.trangThai, N'') = N'Đã thanh toán') "
                + "OR EXISTS (SELECT 1 FROM dbo.HoaDon hd "
                + "          JOIN dbo.HoaDonChiTietDatPhongScope scope ON scope.maHoaDon = hd.maHoaDon "
                + "          WHERE scope.maChiTietDatPhong = ? AND ISNULL(hd.trangThai, N'') = N'Đã thanh toán') "
                + "OR EXISTS (SELECT 1 FROM dbo.HoaDon hd "
                + "          WHERE hd.maDatPhong = ? AND hd.maChiTietDatPhong IS NULL "
                + "            AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonChiTietDatPhongScope scopeAny WHERE scopeAny.maHoaDon = hd.maHoaDon) "
                + "            AND ISNULL(hd.trangThai, N'') = N'Đã thanh toán') "
                + "THEN 1 ELSE 0 END";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maChiTietDatPhong);
            stmt.setInt(2, maChiTietDatPhong);
            stmt.setInt(3, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    /**
     * Kiểm tra lưu trú đã đóng gần nhất của phòng có hóa đơn chưa thanh toán hay không.
     *
     * Method dùng để xác định phòng có nên hiển thị trạng thái chờ thanh toán hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần kiểm tra.
     * @return true nếu lưu trú đóng gần nhất còn hóa đơn chưa thanh toán.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasUnpaidInvoiceForLatestClosedStay(Connection con, int maPhong) throws SQLException {
        ensureInvoiceScopeSchema(con);
        String unpaidStatusClause = "ISNULL(hd.trangThai, N'Ch\u1edd thanh to\u00e1n') NOT IN (N'\u0110\u00e3 thanh to\u00e1n', N'\u0110\u00e3 ho\u00e0n c\u1ecdc')";
        String sql = "WITH latestClosedStay AS ("
                + "    SELECT lt.maChiTietDatPhong, lt.maDatPhong, lt.checkOut, "
                + "           ROW_NUMBER() OVER (ORDER BY lt.checkOut DESC, lt.maLuuTru DESC) AS rn "
                + "    FROM dbo.LuuTru lt "
                + "    WHERE lt.maPhong = ? AND lt.checkOut IS NOT NULL"
                + ") "
                + "SELECT COUNT(1) "
                + "FROM latestClosedStay lcs "
                + "WHERE lcs.rn = 1 "
                + "AND ("
                + "    EXISTS ("
                + "        SELECT 1 FROM dbo.HoaDon hd "
                + "        WHERE hd.maChiTietDatPhong = lcs.maChiTietDatPhong "
                + "          AND " + unpaidStatusClause
                + "    ) "
                + " OR EXISTS ("
                + "        SELECT 1 FROM dbo.HoaDon hd "
                + "        JOIN dbo.HoaDonChiTietDatPhongScope scope ON scope.maHoaDon = hd.maHoaDon "
                + "        WHERE scope.maChiTietDatPhong = lcs.maChiTietDatPhong "
                + "          AND " + unpaidStatusClause
                + "    ) "
                + " OR EXISTS ("
                + "        SELECT 1 FROM dbo.HoaDon hd "
                + "        WHERE hd.maDatPhong = lcs.maDatPhong "
                + "          AND hd.maChiTietDatPhong IS NULL "
                + "          AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonChiTietDatPhongScope scopeAny WHERE scopeAny.maHoaDon = hd.maHoaDon) "
                + "          AND " + unpaidStatusClause
                + "    )"
                + ")";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    /**
     * Kiểm tra booking đã phát sinh lưu trú hay chưa.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần kiểm tra.
     * @return true nếu booking đã có dữ liệu trong bảng LuuTru.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean hasLuuTruForBooking(Connection con, int maDatPhong) throws SQLException {
        String sql = "SELECT COUNT(1) FROM LuuTru WHERE maDatPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDatPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Đếm số dòng chi tiết đặt phòng của một booking.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng cần đếm chi tiết.
     * @return số lượng chi tiết đặt phòng.
     */
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

    /**
     * Chuẩn hóa thông tin tổng hợp của booking sau khi load từ database.
     *
     * Method bổ sung số lượng phòng, tiền cọc, số người và trạng thái booking
     * nếu các giá trị này đang thiếu hoặc cần tính lại từ chi tiết đặt phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param datPhong booking cần chuẩn hóa.
     */
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

    /**
     * Xác định số lượng phòng cần lưu cho booking.
     *
     * Nếu số lượng phòng trên header chưa có, method dùng số dòng chi tiết đặt phòng.
     *
     * @param datPhong booking cần xác định số lượng phòng.
     * @return số lượng phòng hợp lệ.
     */
    private int resolveSoLuongPhong(DatPhong datPhong) {
        if (datPhong.getSoLuongPhong() > 0) {
            return datPhong.getSoLuongPhong();
        }
        int size = datPhong.getChiTietDatPhongs() == null ? 0 : datPhong.getChiTietDatPhongs().size();
        return size <= 0 ? 1 : size;
    }

    /**
     * Xác định tổng số người của booking.
     *
     * Nếu header chưa có số người, method cộng số người từ các chi tiết đặt phòng.
     *
     * @param datPhong booking cần xác định số người.
     * @return số người hợp lệ.
     */
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

    /**
     * Xác định tiền cọc của booking.
     *
     * Ưu tiên tiền cọc trên header, sau đó tổng tiền cọc,
     * cuối cùng cộng từ các chi tiết đặt phòng.
     *
     * @param datPhong booking cần xác định tiền cọc.
     * @return số tiền cọc.
     */
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

    /**
     * Tính thành tiền của một chi tiết đặt phòng.
     *
     * Thành tiền được tính bằng giá áp dụng nhân với số đêm/ngày lưu trú tối thiểu là 1.
     *
     * @param detail chi tiết đặt phòng cần tính thành tiền.
     * @return thành tiền của chi tiết.
     */
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

    /**
     * Xác định loại ngày hiển thị cho một ngày cụ thể.
     *
     * @param date ngày cần xác định.
     * @return loại ngày hiển thị.
     */
    public String determineLoaiNgay(LocalDate date) {
        return toDayTypeDisplay(resolveDayTypeKey(date));
    }

    /**
     * Xác định loại ngày hiển thị cho khoảng lưu trú.
     *
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return loại ngày hiển thị.
     */
    public String determineLoaiNgay(LocalDate checkIn, LocalDate checkOut) {
        return toDayTypeDisplay(resolveDayTypeKey(checkIn, checkOut));
    }

    /**
     * Xác định giá phòng áp dụng theo mã bảng giá và khoảng ngày.
     *
     * @param maBangGia mã bảng giá.
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return kết quả phân giải giá phòng.
     */
    public RoomRateResolution resolveRoomRate(String maBangGia, LocalDate checkIn, LocalDate checkOut) {
        return resolveRoomRateWithSurcharge(maBangGia, checkIn, checkOut);
    }

    /**
     * Áp dụng giá phòng đã phân giải vào chi tiết đặt phòng.
     *
     * Method cập nhật giá áp dụng, mã bảng giá, mã chi tiết bảng giá và ghi chú giá.
     *
     * @param detail chi tiết đặt phòng cần áp dụng giá.
     * @param maBangGia mã bảng giá ưu tiên.
     * @param defaultCheckIn ngày nhận phòng mặc định.
     * @param defaultCheckOut ngày trả phòng mặc định.
     */
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

    /**
     * Xác định bảng giá phù hợp cho một chi tiết đặt phòng.
     *
     * Method ưu tiên bảng giá của booking, sau đó dựa vào loại phòng
     * để tìm bảng giá đang áp dụng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param detail chi tiết đặt phòng cần xác định bảng giá.
     * @param defaultMaBangGia mã bảng giá mặc định từ booking.
     * @return mã bảng giá phù hợp.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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
    /**
     * Tìm mã loại phòng theo mã phòng.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maPhong mã phòng cần tìm loại phòng.
     * @return mã loại phòng nếu tìm thấy, ngược lại trả về null.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Tìm bảng giá đang áp dụng theo loại phòng.
     *
     * Nếu có bảng giá ưu tiên, method sẽ ưu tiên bảng giá đó trước,
     * sau đó mới xét các bảng giá đang áp dụng khác.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maLoaiPhong mã loại phòng.
     * @param preferredBangGia mã bảng giá ưu tiên, có thể null.
     * @return mã bảng giá đang áp dụng nếu tìm thấy, ngược lại trả về null.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
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

    /**
     * Tạo ghi chú mô tả cách áp dụng giá cho chi tiết đặt phòng.
     *
     * Ghi chú gồm loại ngày, loại giá, giá cơ bản, phụ thu và thành tiền nếu có.
     *
     * @param resolution kết quả phân giải giá phòng.
     * @return chuỗi ghi chú giá.
     */
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

    /**
     * Kiểm tra một ngày có phải cuối tuần hay không.
     *
     * @param date ngày cần kiểm tra.
     * @return true nếu là thứ Bảy hoặc Chủ nhật.
     */
    private boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * Chọn giá trị dương nhỏ hơn trong hai giá trị.
     *
     * Nếu một trong hai giá trị không dương, method trả về giá trị dương còn lại.
     *
     * @param first giá trị thứ nhất.
     * @param second giá trị thứ hai.
     * @return giá trị dương nhỏ hơn, hoặc 0 nếu cả hai không dương.
     */
    private double chooseLowerPositive(double first, double second) {
        if (first <= 0d) {
            return Math.max(second, 0d);
        }
        if (second <= 0d) {
            return Math.max(first, 0d);
        }
        return Math.min(first, second);
    }

    /**
     * Chuẩn hóa loại ngày từ chuỗi nhập hoặc chuỗi đọc từ dữ liệu.
     *
     * Method xử lý cả trường hợp chuỗi tiếng Việt bình thường và chuỗi lỗi mã hóa.
     *
     * @param value chuỗi loại ngày cần chuẩn hóa.
     * @return loại ngày chuẩn.
     */
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

    /**
     * Chuẩn hóa loại giá từ chuỗi nhập hoặc chuỗi đọc từ dữ liệu.
     *
     * Method xử lý các loại giá theo ngày, qua đêm, giá lễ, giá cuối tuần và theo giờ.
     *
     * @param value chuỗi loại giá cần chuẩn hóa.
     * @return loại giá chuẩn.
     */
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

    /**
     * Phân giải giá phòng có tính phụ thu theo ngày lễ/cuối tuần.
     *
     * Method xác định:
     * - Loại ngày trong khoảng lưu trú.
     * - Loại lưu trú theo giờ/theo ngày/qua đêm.
     * - Giá nền áp dụng.
     * - Phụ thu áp dụng.
     * - Giá áp dụng trung bình và tổng thành tiền.
     *
     * @param maBangGia mã bảng giá cần dùng.
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return kết quả phân giải giá phòng.
     */
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

    /**
     * Xác định key loại ngày cho một ngày cụ thể.
     *
     * Ưu tiên ngày lễ trước, sau đó mới xét cuối tuần.
     *
     * @param date ngày cần xác định loại.
     * @return key loại ngày.
     */
    private String resolveDayTypeKey(LocalDate date) {
        if (date == null) {
            return DAY_TYPE_NORMAL;
        }
        if (ngayLeDAO.isHoliday(date)) {
            return DAY_TYPE_HOLIDAY;
        }
        return isWeekend(date) ? DAY_TYPE_WEEKEND : DAY_TYPE_NORMAL;
    }

    /**
     * Xác định key loại ngày cho cả khoảng lưu trú.
     *
     * Nếu trong khoảng có ngày lễ thì ưu tiên ngày lễ.
     * Nếu không có ngày lễ nhưng có cuối tuần thì trả về cuối tuần.
     *
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return key loại ngày của khoảng lưu trú.
     */
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

    /**
     * Tạo mô tả loại ngày cho khoảng lưu trú.
     *
     * Nếu khoảng lưu trú có nhiều loại ngày, method trả về mô tả tổng quát
     * như có ngày lễ hoặc có cuối tuần trong khoảng lưu trú.
     *
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return mô tả loại ngày.
     */
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

    /**
     * Tạo mô tả loại giá/lưu trú.
     *
     * @param stayType key loại lưu trú.
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return mô tả loại giá áp dụng.
     */
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
    /**
     * Xác định key loại lưu trú dựa trên ngày nhận và ngày trả.
     *
     * Nếu không đủ dữ liệu hoặc ngày trả không sau ngày nhận, method xem như theo giờ.
     * Nếu khoảng cách là 1 ngày, method xem như qua đêm.
     * Các trường hợp còn lại xem như theo ngày.
     *
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return key loại lưu trú.
     */
    private String resolveStayTypeKey(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return STAY_TYPE_HOURLY;
        }
        long soDem = Math.max(1L, ChronoUnit.DAYS.between(checkIn, checkOut));
        return soDem == 1L ? STAY_TYPE_OVERNIGHT : STAY_TYPE_DAILY;
    }

    /**
     * Tạo danh sách ngày cần tính giá trong khoảng lưu trú.
     *
     * Với đặt phòng nhiều ngày, danh sách bao gồm các ngày từ checkIn đến trước checkOut.
     *
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return danh sách ngày dùng để tính giá/phụ thu.
     */
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

    /**
     * Lấy giá cơ bản theo loại lưu trú.
     *
     * @param detail chi tiết bảng giá.
     * @param stayType key loại lưu trú.
     * @return giá cơ bản phù hợp.
     */
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

    /**
     * Tính tổng giá cơ bản theo loại lưu trú và số đơn vị tính giá.
     *
     * @param detail chi tiết bảng giá.
     * @param stayType key loại lưu trú.
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @param pricingUnits số đơn vị tính giá.
     * @return tổng giá cơ bản.
     */
    private double resolveBaseAmount(ChiTietBangGia detail, String stayType, LocalDate checkIn, LocalDate checkOut, long pricingUnits) {
        double baseRate = Math.max(resolveBaseRate(detail, stayType), 0d);
        if (STAY_TYPE_DAILY.equals(stayType)) {
            return baseRate * Math.max(pricingUnits, 1L);
        }
        return baseRate * Math.max(pricingUnits, 1L);
    }

    /**
     * Lấy phụ thu theo loại ngày.
     *
     * @param detail chi tiết bảng giá.
     * @param dayType key loại ngày.
     * @return phụ thu tương ứng.
     */
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

    /**
     * Tính tổng phụ thu cho khoảng lưu trú.
     *
     * Nếu lưu trú theo ngày, method cộng phụ thu từng ngày.
     * Các loại lưu trú khác lấy phụ thu theo loại ngày của toàn khoảng.
     *
     * @param detail chi tiết bảng giá.
     * @param stayType key loại lưu trú.
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return tổng phụ thu.
     */
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

    /**
     * Xác định số đơn vị tính giá.
     *
     * Với lưu trú theo ngày, đơn vị là số ngày giữa checkIn và checkOut.
     * Với các loại lưu trú khác, đơn vị mặc định là 1.
     *
     * @param stayType key loại lưu trú.
     * @param checkIn ngày nhận phòng.
     * @param checkOut ngày trả phòng.
     * @return số đơn vị tính giá.
     */
    private long resolvePricingUnits(String stayType, LocalDate checkIn, LocalDate checkOut) {
        if (!STAY_TYPE_DAILY.equals(stayType)) {
            return 1L;
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 1L;
        }
        return Math.max(1L, ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    /**
     * Lấy giá trị dương đầu tiên trong danh sách giá trị.
     *
     * @param values danh sách giá trị cần kiểm tra.
     * @return giá trị dương đầu tiên, hoặc 0 nếu không có.
     */
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

    /**
     * Chuyển key loại ngày sang chuỗi hiển thị.
     *
     * @param dayType key loại ngày.
     * @return chuỗi loại ngày hiển thị.
     */
    private String toDayTypeDisplay(String dayType) {
        if (DAY_TYPE_HOLIDAY.equals(dayType)) {
            return DISPLAY_LOAI_NGAY_LE;
        }
        if (DAY_TYPE_WEEKEND.equals(dayType)) {
            return DISPLAY_LOAI_NGAY_CUOI_TUAN;
        }
        return DISPLAY_LOAI_NGAY_THUONG;
    }

    /**
     * Chuyển key loại lưu trú sang chuỗi hiển thị.
     *
     * @param stayType key loại lưu trú.
     * @return chuỗi loại giá/lưu trú hiển thị.
     */
    private String toStayTypeDisplay(String stayType) {
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return DISPLAY_LOAI_GIA_QUA_DEM;
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return DISPLAY_LOAI_GIA_THEO_GIO;
        }
        return DISPLAY_LOAI_GIA_THEO_NGAY;
    }
    /**
     * Xác định trạng thái phòng tương ứng với trạng thái booking.
     *
     * @param bookingStatus trạng thái đặt phòng.
     * @return trạng thái phòng tương ứng.
     */
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

    /**
     * Xác định trạng thái của một chi tiết đặt phòng.
     *
     * Trạng thái chi tiết dựa trên trạng thái booking, thông tin phòng,
     * lưu trú gần nhất và thời gian check-out gần nhất.
     *
     * @param bookingStatus trạng thái booking.
     * @param soPhong số phòng của chi tiết.
     * @param latestStayId mã lưu trú gần nhất nếu có.
     * @param latestCheckOut thời gian check-out gần nhất nếu có.
     * @return trạng thái chi tiết đặt phòng.
     */
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

    /**
     * Xác định trạng thái booking dựa trên dữ liệu chi tiết đặt phòng và lưu trú.
     *
     * Method ưu tiên:
     * - Giữ trạng thái hủy nếu booking đã hủy.
     * - Đã thanh toán nếu booking đã thanh toán đủ.
     * - Đang ở nếu còn phòng đang lưu trú.
     * - Chờ thanh toán nếu tất cả chi tiết đã check-out.
     * - Chờ check-in nếu chưa phát sinh lưu trú.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param datPhong booking cần xác định trạng thái.
     * @return trạng thái booking sau khi tính toán.
     */
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

    /**
     * Kiểm tra booking đã thanh toán đầy đủ hay chưa.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maDatPhong mã đặt phòng.
     * @return true nếu booking đã thanh toán đầy đủ.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean isBookingPaid(Connection con, int maDatPhong) throws SQLException {
        return isBookingFullyPaid(con, maDatPhong);
    }

    /**
     * Gán giá trị Integer có thể null vào PreparedStatement.
     *
     * @param stmt PreparedStatement cần gán tham số.
     * @param index vị trí tham số.
     * @param value chuỗi cần chuyển sang Integer.
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
     * @param value chuỗi cần chuyển đổi.
     * @return Integer nếu chuyển được, ngược lại trả về null.
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
     * Chuyển LocalDate sang java.sql.Date.
     *
     * @param value ngày cần chuyển.
     * @return java.sql.Date tương ứng, hoặc null nếu value null.
     */
    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    /**
     * Chuyển LocalDate sang LocalDateTime theo mốc giờ mặc định của chi tiết đặt phòng.
     *
     * @param value ngày cần chuyển.
     * @return LocalDateTime tại DETAIL_BOOKING_BOUNDARY_TIME, hoặc null nếu value null.
     */
    private LocalDateTime toDetailScheduleDateTime(LocalDate value) {
        return value == null ? null : LocalDateTime.of(value, DETAIL_BOOKING_BOUNDARY_TIME);
    }

    /**
     * Chuyển LocalDate sang Timestamp theo mốc giờ mặc định của chi tiết đặt phòng.
     *
     * @param value ngày cần chuyển.
     * @return Timestamp tương ứng, hoặc null nếu value null.
     */
    private Timestamp toDetailScheduleTimestamp(LocalDate value) {
        return value == null ? null : Timestamp.valueOf(toDetailScheduleDateTime(value));
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
     * Tạo biểu thức SQL lấy thời gian check-in dự kiến của chi tiết đặt phòng.
     *
     * Nếu chi tiết chưa có checkInDuKien, hệ thống lấy ngày nhận phòng của DatPhong
     * cộng với mốc giờ mặc định.
     *
     * @param detailAlias alias của bảng ChiTietDatPhong.
     * @param headerAlias alias của bảng DatPhong.
     * @return biểu thức SQL lấy check-in dự kiến.
     */
    private String buildDetailCheckInExpr(String detailAlias, String headerAlias) {
        return "COALESCE(" + detailAlias + ".checkInDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour()
                + ", CAST(" + headerAlias + ".ngayNhanPhong AS DATETIME2)))";
    }

    /**
     * Tạo biểu thức SQL lấy thời gian check-out dự kiến của chi tiết đặt phòng.
     *
     * Nếu chi tiết chưa có checkOutDuKien, hệ thống lấy ngày trả phòng của DatPhong
     * cộng với mốc giờ mặc định.
     *
     * @param detailAlias alias của bảng ChiTietDatPhong.
     * @param headerAlias alias của bảng DatPhong.
     * @return biểu thức SQL lấy check-out dự kiến.
     */
    private String buildDetailCheckOutExpr(String detailAlias, String headerAlias) {
        return "COALESCE(" + detailAlias + ".checkOutDuKien, DATEADD(HOUR, " + DETAIL_BOOKING_BOUNDARY_TIME.getHour()
                + ", CAST(" + headerAlias + ".ngayTraPhong AS DATETIME2)))";
    }

    /**
     * Chuyển java.sql.Date sang LocalDate.
     *
     * @param value ngày SQL cần chuyển.
     * @return LocalDate tương ứng, hoặc null nếu value null.
     */
    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
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
     * Trả về fallback nếu chuỗi value rỗng.
     *
     * @param value giá trị cần kiểm tra.
     * @param fallback giá trị thay thế khi value rỗng.
     * @return value đã trim hoặc fallback.
     */
    private String defaultIfEmpty(String value, String fallback) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    /**
     * Rollback transaction, bỏ qua lỗi nếu rollback thất bại.
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
     * Bật lại auto-commit cho connection, bỏ qua lỗi nếu thao tác thất bại.
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
     * Lấy connection hiện tại từ ConnectDB.
     *
     * @return connection hiện tại, hoặc null nếu chưa kết nối.
     */
    private Connection getReadyConnection() {
        return ConnectDB.getConnection();
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
     * DTO chứa thông tin phòng khả dụng khi chọn phòng cho đặt phòng.
     */
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

    /**
     * DTO chứa kết quả phân giải giá phòng.
     *
     * Đối tượng này lưu:
     * - Loại ngày.
     * - Loại lưu trú.
     * - Giá nền.
     * - Phụ thu.
     * - Giá áp dụng.
     * - Thành tiền.
     * - Mã chi tiết bảng giá được dùng.
     */
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