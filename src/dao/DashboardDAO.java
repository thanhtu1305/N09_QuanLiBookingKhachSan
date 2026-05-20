package dao;

import db.ConnectDB;
import entity.DashboardChartPoint;
import entity.DashboardGanttCell;
import entity.DashboardGanttRow;
import entity.DashboardSummary;
import entity.DashboardTaskRow;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DAO xử lý dữ liệu cho màn hình Dashboard.
 *
 * Lớp này phụ trách:
 * - Lấy số liệu tổng quan về phòng, đặt phòng, thanh toán.
 * - Lấy dữ liệu biểu đồ doanh thu và số lượng đặt phòng.
 * - Lấy danh sách công việc cần xử lý trong ngày.
 * - Lấy dữ liệu sơ đồ Gantt theo phòng.
 */
public class DashboardDAO {
    // Các trạng thái phòng dùng để thống kê và hiển thị trên Dashboard.
    private static final String ROOM_STATUS_OPERATIONAL = "Ho\u1ea1t \u0111\u1ed9ng";
    private static final String ROOM_STATUS_EMPTY = "Tr\u1ed1ng";
    private static final String ROOM_STATUS_OCCUPIED = "\u0110ang \u1edf";
    private static final String ROOM_STATUS_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String ROOM_STATUS_PENDING_CHECKIN = "Ch\u1edd check-in";
    private static final String ROOM_STATUS_MAINTENANCE = "B\u1ea3o tr\u00ec";
    private static final String GANTT_STATUS_TEXT_BOOKED = "\u0110\u1eb7t";

    // Các trạng thái đặt phòng dùng để lọc dữ liệu booking.
    private static final String BOOKING_STATUS_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String BOOKING_STATUS_CONFIRMED = "\u0110\u00e3 x\u00e1c nh\u1eadn";
    private static final String BOOKING_STATUS_DEPOSITED = "\u0110\u00e3 c\u1ecdc";
    private static final String BOOKING_STATUS_PENDING_CHECKIN = "Ch\u1edd check-in";
    private static final String BOOKING_STATUS_ACTIVE = "\u0110ang \u1edf";
    private static final String BOOKING_STATUS_CHECKED_IN = "\u0110\u00e3 check-in";
    private static final String BOOKING_STATUS_PARTIAL_CHECKOUT = "Check-out m\u1ed9t ph\u1ea7n";
    private static final String BOOKING_STATUS_STAYING = "\u0110ang l\u01b0u tr\u00fa";
    private static final String BOOKING_STATUS_CHECKED_OUT = "\u0110\u00e3 check-out";
    private static final String BOOKING_STATUS_PAID = "\u0110\u00e3 thanh to\u00e1n";

    // Các hằng số phục vụ thống kê thanh toán và trạng thái ô trong sơ đồ Gantt.
    private static final String INVOICE_STATUS_PENDING = "Ch\u1edd thanh to\u00e1n";
    private static final String PAYMENT_TYPE_DEFAULT = "THANH_TOAN";
    private static final String GANTT_STATUS_EMPTY = "E";
    private static final String GANTT_STATUS_BOOKED = "B";
    private static final String GANTT_STATUS_PENDING_CHECKIN = "C";
    private static final String GANTT_STATUS_OCCUPIED = "O";
    private static final String GANTT_STATUS_MAINTENANCE = "M";

    // Độ ưu tiên của trạng thái khi nhiều nguồn dữ liệu cùng tác động lên một ô Gantt.
    private static final int GANTT_PRIORITY_EMPTY = 1;
    private static final int GANTT_PRIORITY_BOOKED = 2;
    private static final int GANTT_PRIORITY_PENDING_CHECKIN = 3;
    private static final int GANTT_PRIORITY_OCCUPIED = 4;
    private static final int GANTT_PRIORITY_MAINTENANCE = 5;

    // Định dạng ngày giờ dùng cho biểu đồ và danh sách công việc.
    private static final DateTimeFormatter CHART_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter TASK_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TASK_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Lưu thông báo lỗi gần nhất để lớp gọi có thể hiển thị nếu cần.
     */
    private String lastErrorMessage = "";

    /**
     * Lấy thông báo lỗi gần nhất phát sinh trong DAO.
     *
     * @return nội dung lỗi gần nhất, rỗng nếu chưa có lỗi.
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Lấy toàn bộ số liệu tổng quan cho Dashboard.
     *
     * Các số liệu gồm:
     * - Số phòng hoạt động.
     * - Số phòng đang ở.
     * - Số phòng đã đặt.
     * - Số phòng bảo trì.
     * - Số booking hôm nay.
     * - Số booking chờ check-in hôm nay.
     * - Số hóa đơn chờ thanh toán.
     * - Số booking cần check-out hôm nay.
     * - Doanh thu hôm nay.
     * - Doanh thu tháng hiện tại.
     *
     * @return đối tượng DashboardSummary chứa các số liệu tổng quan.
     */
    public DashboardSummary getDashboardSummary() {
        clearLastError();
        DashboardSummary summary = new DashboardSummary();
        try {
            Map<String, Integer> roomSummary = getRoomStatusSummary();
            summary.setActiveRooms(sum(roomSummary, ROOM_STATUS_OPERATIONAL, ROOM_STATUS_EMPTY));
            summary.setOccupiedRooms(valueOf(roomSummary, ROOM_STATUS_OCCUPIED));
            summary.setBookedRooms(valueOf(roomSummary, ROOM_STATUS_BOOKED));
            summary.setMaintenanceRooms(valueOf(roomSummary, ROOM_STATUS_MAINTENANCE));
            summary.setTodayBookings(getTodayBookingCount());
            summary.setPendingCheckinToday(getPendingCheckinTodayCount());
            summary.setPendingPaymentCount(getPendingPaymentCount());
            summary.setCheckoutDueTodayCount(getCheckoutDueTodayCount());
            summary.setRevenueToday(getRevenueToday());
            summary.setRevenueThisMonth(getRevenueThisMonth());
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return summary;
    }

    /**
     * Thống kê số lượng phòng theo từng trạng thái chính.
     *
     * @return map có key là trạng thái phòng, value là số lượng phòng theo trạng thái đó.
     */
    public Map<String, Integer> getRoomStatusSummary() {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        result.put(ROOM_STATUS_OPERATIONAL, Integer.valueOf(0));
        result.put(ROOM_STATUS_EMPTY, Integer.valueOf(0));
        result.put(ROOM_STATUS_OCCUPIED, Integer.valueOf(0));
        result.put(ROOM_STATUS_BOOKED, Integer.valueOf(0));
        result.put(ROOM_STATUS_MAINTENANCE, Integer.valueOf(0));

        String sql = "SELECT "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS phongHoatDong, "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS phongTrong, "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS phongDangO, "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS phongDaDat, "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS phongBaoTri "
                + "FROM Phong";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return result;
            }
            stmt.setString(1, ROOM_STATUS_OPERATIONAL);
            stmt.setString(2, ROOM_STATUS_EMPTY);
            stmt.setString(3, ROOM_STATUS_OCCUPIED);
            stmt.setString(4, ROOM_STATUS_BOOKED);
            stmt.setString(5, ROOM_STATUS_MAINTENANCE);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.put(ROOM_STATUS_OPERATIONAL, Integer.valueOf(rs.getInt("phongHoatDong")));
                    result.put(ROOM_STATUS_EMPTY, Integer.valueOf(rs.getInt("phongTrong")));
                    result.put(ROOM_STATUS_OCCUPIED, Integer.valueOf(rs.getInt("phongDangO")));
                    result.put(ROOM_STATUS_BOOKED, Integer.valueOf(rs.getInt("phongDaDat")));
                    result.put(ROOM_STATUS_MAINTENANCE, Integer.valueOf(rs.getInt("phongBaoTri")));
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return result;
    }

    /**
     * Thống kê số lượng booking theo một số nhóm trạng thái dùng trên Dashboard.
     *
     * @return map chứa số lượng booking hôm nay, chờ check-in, đang lưu trú và đã thanh toán.
     */
    public Map<String, Integer> getBookingStatusSummary() {
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        result.put("bookingHomNay", Integer.valueOf(0));
        result.put("choCheckin", Integer.valueOf(0));
        result.put("dangLuuTru", Integer.valueOf(0));
        result.put("daThanhToan", Integer.valueOf(0));

        String sql = "SELECT "
                + "SUM(CASE WHEN CAST(ngayDat AS DATE) = CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS bookingHomNay, "
                + "SUM(CASE WHEN trangThai IN (?, ?, ?, ?) AND CAST(ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE) THEN 1 ELSE 0 END) AS choCheckin, "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS dangLuuTru, "
                + "SUM(CASE WHEN trangThai = ? THEN 1 ELSE 0 END) AS daThanhToan "
                + "FROM DatPhong";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return result;
            }
            stmt.setString(1, BOOKING_STATUS_BOOKED);
            stmt.setString(2, BOOKING_STATUS_CONFIRMED);
            stmt.setString(3, BOOKING_STATUS_DEPOSITED);
            stmt.setString(4, BOOKING_STATUS_PENDING_CHECKIN);
            stmt.setString(5, BOOKING_STATUS_STAYING);
            stmt.setString(6, BOOKING_STATUS_PAID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result.put("bookingHomNay", Integer.valueOf(rs.getInt("bookingHomNay")));
                    result.put("choCheckin", Integer.valueOf(rs.getInt("choCheckin")));
                    result.put("dangLuuTru", Integer.valueOf(rs.getInt("dangLuuTru")));
                    result.put("daThanhToan", Integer.valueOf(rs.getInt("daThanhToan")));
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return result;
    }

    /**
     * Đếm số booking được tạo trong ngày hiện tại.
     *
     * @return số booking hôm nay.
     */
    public int getTodayBookingCount() {
        String sql = "SELECT COUNT(1) FROM DatPhong WHERE CAST(ngayDat AS DATE) = CAST(GETDATE() AS DATE)";
        return queryForCount(sql);
    }

    /**
     * Đếm số booking có ngày nhận phòng là hôm nay và đang chờ check-in.
     *
     * @return số booking chờ check-in hôm nay.
     */
    public int getPendingCheckinTodayCount() {
        String sql = "SELECT COUNT(1) "
                + "FROM DatPhong "
                + "WHERE CAST(ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND trangThai IN (?, ?, ?, ?)";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return 0;
            }
            stmt.setString(1, BOOKING_STATUS_BOOKED);
            stmt.setString(2, BOOKING_STATUS_CONFIRMED);
            stmt.setString(3, BOOKING_STATUS_DEPOSITED);
            stmt.setString(4, BOOKING_STATUS_PENDING_CHECKIN);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
            return 0;
        }
    }

    /**
     * Đếm số booking đến hạn check-out trong ngày hiện tại.
     *
     * @return số booking cần check-out hôm nay.
     */
    public int getCheckoutDueTodayCount() {
        String sql = "SELECT COUNT(DISTINCT dp.maDatPhong) "
                + "FROM DatPhong dp "
                + "WHERE CAST(dp.ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND dp.trangThai = ? "
                + "AND EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maDatPhong = dp.maDatPhong AND lt.checkOut IS NULL)";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return 0;
            }
            stmt.setString(1, BOOKING_STATUS_STAYING);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
            return 0;
        }
    }

    /**
     * Đếm số hóa đơn đang chờ thanh toán.
     *
     * Method có kiểm tra sự tồn tại của cột trangThai trong bảng HoaDon
     * để tránh lỗi nếu database chưa có cột này.
     *
     * @return số hóa đơn chờ thanh toán.
     */
    public int getPendingPaymentCount() {
        try (Connection con = getReadyConnection()) {
            if (con == null || !hasColumn(con, "HoaDon", "trangThai")) {
                return 0;
            }
            String sql = "SELECT COUNT(1) FROM HoaDon WHERE ISNULL(trangThai, ?) = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, INVOICE_STATUS_PENDING);
                stmt.setString(2, INVOICE_STATUS_PENDING);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
            return 0;
        }
    }

    /**
     * Tính doanh thu trong ngày hiện tại.
     *
     * @return doanh thu hôm nay.
     */
    public double getRevenueToday() {
        LocalDate today = LocalDate.now();
        return queryRevenueBetween(today, today.plusDays(1L));
    }

    /**
     * Tính doanh thu trong tháng hiện tại.
     *
     * @return doanh thu tháng hiện tại.
     */
    public double getRevenueThisMonth() {
        LocalDate firstDay = YearMonth.now().atDay(1);
        return queryRevenueBetween(firstDay, firstDay.plusMonths(1L));
    }
    /**
     * Lấy dữ liệu doanh thu trong 7 ngày gần nhất để vẽ biểu đồ.
     *
     * Nếu ngày nào không có doanh thu, giá trị ngày đó vẫn được giữ là 0
     * để biểu đồ luôn đủ 7 điểm dữ liệu.
     *
     * @return danh sách điểm dữ liệu doanh thu 7 ngày gần nhất.
     */
    public List<DashboardChartPoint> getRevenueLast7Days() {
        clearLastError();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6L);
        Map<LocalDate, Double> valuesByDate = new LinkedHashMap<LocalDate, Double>();
        List<DashboardChartPoint> result = initDateSeries(startDate, endDate);

        try (Connection con = getReadyConnection()) {
            if (con == null) {
                return result;
            }
            boolean hasTransactionType = hasColumn(con, "ThanhToan", "loaiGiaoDich");
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT CAST(ngayThanhToan AS DATE) AS ngay, ISNULL(SUM(soTien), 0) AS tongTien ")
                    .append("FROM ThanhToan ")
                    .append("WHERE ngayThanhToan >= ? AND ngayThanhToan < ? ");
            if (hasTransactionType) {
                sql.append("AND ISNULL(loaiGiaoDich, ?) = ? ");
            }
            sql.append("GROUP BY CAST(ngayThanhToan AS DATE)");

            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                int index = 1;
                stmt.setTimestamp(index++, Timestamp.valueOf(startDate.atStartOfDay()));
                stmt.setTimestamp(index++, Timestamp.valueOf(endDate.plusDays(1L).atStartOfDay()));
                if (hasTransactionType) {
                    stmt.setString(index++, PAYMENT_TYPE_DEFAULT);
                    stmt.setString(index, PAYMENT_TYPE_DEFAULT);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Date sqlDate = rs.getDate("ngay");
                        if (sqlDate != null) {
                            valuesByDate.put(sqlDate.toLocalDate(), Double.valueOf(rs.getDouble("tongTien")));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }

        applySeriesValues(result, valuesByDate);
        return result;
    }

    /**
     * Lấy dữ liệu số lượng đặt phòng trong 7 ngày gần nhất để vẽ biểu đồ.
     *
     * Nếu ngày nào không có booking, giá trị ngày đó vẫn được giữ là 0
     * để biểu đồ luôn đủ 7 điểm dữ liệu.
     *
     * @return danh sách điểm dữ liệu booking 7 ngày gần nhất.
     */
    public List<DashboardChartPoint> getBookingLast7Days() {
        clearLastError();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6L);
        Map<LocalDate, Double> valuesByDate = new LinkedHashMap<LocalDate, Double>();
        List<DashboardChartPoint> result = initDateSeries(startDate, endDate);

        String sql = "SELECT CAST(ngayDat AS DATE) AS ngay, COUNT(1) AS soLuong "
                + "FROM DatPhong "
                + "WHERE ngayDat >= ? AND ngayDat < ? "
                + "GROUP BY CAST(ngayDat AS DATE)";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return result;
            }
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate.plusDays(1L)));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("ngay");
                    if (sqlDate != null) {
                        valuesByDate.put(sqlDate.toLocalDate(), Double.valueOf(rs.getInt("soLuong")));
                    }
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }

        applySeriesValues(result, valuesByDate);
        return result;
    }

    /**
     * Lấy danh sách công việc cần xử lý trong ngày.
     *
     * Danh sách gồm:
     * - Booking chờ check-in.
     * - Booking đến hạn check-out.
     * - Hóa đơn chờ thanh toán.
     *
     * Kết quả được sắp xếp theo độ ưu tiên và thời gian,
     * sau đó giới hạn tối đa 12 dòng để hiển thị trên Dashboard.
     *
     * @return danh sách công việc trong ngày.
     */
    public List<DashboardTaskRow> getTodayTasks() {
        clearLastError();
        List<DashboardTaskRow> rows = new ArrayList<DashboardTaskRow>();
        rows.addAll(getPendingCheckinTasks());
        rows.addAll(getCheckoutDueTodayTasks());
        rows.addAll(getPendingPaymentTasks());

        rows.sort((left, right) -> {
            if (left.getPriority() != right.getPriority()) {
                return left.getPriority() - right.getPriority();
            }
            Timestamp leftTime = left.getSortTime();
            Timestamp rightTime = right.getSortTime();
            LocalDateTime defaultTime = LocalDateTime.now();
            LocalDateTime a = leftTime == null ? defaultTime : leftTime.toLocalDateTime();
            LocalDateTime b = rightTime == null ? defaultTime : rightTime.toLocalDateTime();
            return a.compareTo(b);
        });

        if (rows.size() > 12) {
            return new ArrayList<DashboardTaskRow>(rows.subList(0, 12));
        }
        return rows;
    }

    /**
     * Lấy dữ liệu Gantt theo phòng trong một khoảng ngày.
     *
     * Mỗi phòng sẽ có một dòng, mỗi ngày là một ô trạng thái.
     * Sau khi tạo dữ liệu nền, method sẽ áp dụng thêm trạng thái đang ở và đã đặt.
     *
     * @param startDate ngày bắt đầu hiển thị.
     * @param dayCount số ngày cần hiển thị.
     * @return danh sách dòng Gantt theo phòng.
     */
    public List<DashboardGanttRow> getRoomGanttRows(LocalDate startDate, int dayCount) {
        clearLastError();
        List<DashboardGanttRow> rows = new ArrayList<DashboardGanttRow>();
        if (startDate == null || dayCount <= 0) {
            return rows;
        }

        Map<Integer, DashboardGanttRow> rowByRoomId = loadBaseGanttRows(startDate, dayCount);
        rows.addAll(rowByRoomId.values());
        if (rows.isEmpty()) {
            return rows;
        }

        LocalDate endDate = startDate.plusDays(dayCount - 1L);
        applyOccupiedCells(rowByRoomId, startDate, endDate);
        applyBookedCells(rowByRoomId, startDate, endDate);
        return rows;
    }

    /**
     * Lấy danh sách booking chờ check-in trong ngày để hiển thị ở khu vực công việc.
     *
     * @return danh sách công việc check-in.
     */
    private List<DashboardTaskRow> getPendingCheckinTasks() {
        List<DashboardTaskRow> rows = new ArrayList<DashboardTaskRow>();
        String sql = "SELECT TOP 6 dp.maDatPhong, kh.hoTen, dp.ngayNhanPhong, dp.trangThai, "
                + "ISNULL(roomSummary.soPhong, N'Chua gan phong') AS soPhong "
                + "FROM DatPhong dp "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "OUTER APPLY ("
                + "   SELECT STUFF(("
                + "       SELECT N', ' + p2.soPhong "
                + "       FROM ChiTietDatPhong c2 "
                + "       LEFT JOIN Phong p2 ON p2.maPhong = c2.maPhong "
                + "       WHERE c2.maDatPhong = dp.maDatPhong "
                + "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong "
                + "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong"
                + ") roomSummary "
                + "WHERE CAST(dp.ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND dp.trangThai IN (?, ?, ?, ?) "
                + "ORDER BY dp.ngayNhanPhong ASC, dp.maDatPhong DESC";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return rows;
            }
            stmt.setString(1, BOOKING_STATUS_BOOKED);
            stmt.setString(2, BOOKING_STATUS_CONFIRMED);
            stmt.setString(3, BOOKING_STATUS_DEPOSITED);
            stmt.setString(4, BOOKING_STATUS_PENDING_CHECKIN);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DashboardTaskRow row = new DashboardTaskRow();
                    row.setTaskCode("DP" + rs.getInt("maDatPhong"));
                    row.setTaskType("CHECKIN");
                    row.setTarget(buildTarget(rs.getString("hoTen"), rs.getString("soPhong")));
                    row.setTimeText(formatDate(rs.getDate("ngayNhanPhong")));
                    row.setStatus(safeTrim(rs.getString("trangThai")));
                    row.setActionHint("Mo man Check-in/out de tiep nhan phong va xac nhan luu tru.");
                    row.setPriority(1);
                    row.setSortTime(toTimestamp(rs.getDate("ngayNhanPhong")));
                    rows.add(row);
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return rows;
    }
    /**
     * Lấy danh sách booking đến hạn check-out trong ngày để hiển thị ở khu vực công việc.
     *
     * @return danh sách công việc check-out.
     */
    private List<DashboardTaskRow> getCheckoutDueTodayTasks() {
        List<DashboardTaskRow> rows = new ArrayList<DashboardTaskRow>();
        String sql = "SELECT TOP 6 dp.maDatPhong, kh.hoTen, dp.ngayTraPhong AS checkOutDue, dp.trangThai, "
                + "ISNULL(roomSummary.soPhong, N'Chua gan phong') AS soPhong "
                + "FROM DatPhong dp "
                + "JOIN LuuTru lt ON lt.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "OUTER APPLY ("
                + "   SELECT STUFF(("
                + "       SELECT N', ' + p2.soPhong "
                + "       FROM ChiTietDatPhong c2 "
                + "       LEFT JOIN Phong p2 ON p2.maPhong = c2.maPhong "
                + "       WHERE c2.maDatPhong = dp.maDatPhong "
                + "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong "
                + "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong"
                + ") roomSummary "
                + "WHERE CAST(dp.ngayTraPhong AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND lt.checkOut IS NULL "
                + "AND dp.trangThai = ? "
                + "GROUP BY dp.maDatPhong, kh.hoTen, dp.ngayTraPhong, dp.trangThai, roomSummary.soPhong "
                + "ORDER BY dp.ngayTraPhong ASC, dp.maDatPhong DESC";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return rows;
            }
            stmt.setString(1, BOOKING_STATUS_STAYING);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DashboardTaskRow row = new DashboardTaskRow();
                    row.setTaskCode("LT" + rs.getInt("maDatPhong"));
                    row.setTaskType("CHECKOUT");
                    row.setTarget(buildTarget(rs.getString("hoTen"), rs.getString("soPhong")));
                    row.setTimeText(formatTimestamp(rs.getTimestamp("checkOutDue")));
                    row.setStatus("Sap check-out");
                    row.setActionHint("Kiem tra dich vu su dung va chuan bi quy trinh check-out.");
                    row.setPriority(2);
                    row.setSortTime(rs.getTimestamp("checkOutDue"));
                    rows.add(row);
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return rows;
    }

    /**
     * Lấy danh sách hóa đơn đang chờ thanh toán để hiển thị ở khu vực công việc.
     *
     * Method có kiểm tra cột trangThai của bảng HoaDon trước khi truy vấn,
     * giúp tránh lỗi nếu database chưa có cột này.
     *
     * @return danh sách công việc thanh toán.
     */
    private List<DashboardTaskRow> getPendingPaymentTasks() {
        List<DashboardTaskRow> rows = new ArrayList<DashboardTaskRow>();
        try (Connection con = getReadyConnection()) {
            if (con == null || !hasColumn(con, "HoaDon", "trangThai")) {
                return rows;
            }

            String sql = "SELECT TOP 6 hd.maHoaDon, kh.hoTen, hd.ngayLap, ISNULL(hd.trangThai, ?) AS trangThai, "
                    + "ISNULL(roomSummary.soPhong, N'-') AS soPhong "
                    + "FROM HoaDon hd "
                    + "LEFT JOIN DatPhong dp ON dp.maDatPhong = hd.maDatPhong "
                    + "LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang "
                    + "OUTER APPLY ("
                    + "   SELECT STUFF(("
                    + "       SELECT N', ' + p2.soPhong "
                    + "       FROM ChiTietDatPhong c2 "
                    + "       LEFT JOIN Phong p2 ON p2.maPhong = c2.maPhong "
                    + "       WHERE c2.maDatPhong = hd.maDatPhong "
                    + "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong "
                    + "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong"
                    + ") roomSummary "
                    + "WHERE ISNULL(hd.trangThai, ?) = ? "
                    + "ORDER BY hd.ngayLap ASC, hd.maHoaDon DESC";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, INVOICE_STATUS_PENDING);
                stmt.setString(2, INVOICE_STATUS_PENDING);
                stmt.setString(3, INVOICE_STATUS_PENDING);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DashboardTaskRow row = new DashboardTaskRow();
                        row.setTaskCode("HD" + rs.getInt("maHoaDon"));
                        row.setTaskType("PAYMENT");
                        row.setTarget(buildTarget(rs.getString("hoTen"), rs.getString("soPhong")));
                        row.setTimeText(formatTimestamp(rs.getTimestamp("ngayLap")));
                        row.setStatus(safeTrim(rs.getString("trangThai")));
                        row.setActionHint("Mo man Thanh toan de doi chieu hoa don va thu tien.");
                        row.setPriority(3);
                        row.setSortTime(rs.getTimestamp("ngayLap"));
                        rows.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return rows;
    }

    /**
     * Tạo dữ liệu nền cho sơ đồ Gantt theo phòng.
     *
     * Mỗi phòng được tạo thành một DashboardGanttRow.
     * Mỗi ngày trong khoảng hiển thị được tạo thành một DashboardGanttCell.
     * Trạng thái ban đầu của ô là trống hoặc bảo trì tùy theo trạng thái phòng.
     *
     * @param startDate ngày bắt đầu hiển thị.
     * @param dayCount số ngày cần tạo ô Gantt.
     * @return map các dòng Gantt, key là mã phòng.
     */
    private Map<Integer, DashboardGanttRow> loadBaseGanttRows(LocalDate startDate, int dayCount) {
        Map<Integer, DashboardGanttRow> rows = new LinkedHashMap<Integer, DashboardGanttRow>();
        String sql = "SELECT p.maPhong, p.soPhong, p.tang, p.khuVuc, p.sucChuaChuan, p.sucChuaToiDa, p.trangThai, "
                + "ISNULL(lp.tenLoaiPhong, N'-') AS tenLoaiPhong "
                + "FROM dbo.Phong p "
                + "LEFT JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "ORDER BY CASE WHEN TRY_CAST(p.soPhong AS INT) IS NULL THEN 1 ELSE 0 END, TRY_CAST(p.soPhong AS INT), p.soPhong";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return rows;
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DashboardGanttRow row = new DashboardGanttRow();
                    row.setMaPhong(rs.getInt("maPhong"));
                    row.setSoPhong(safeTrim(rs.getString("soPhong")));
                    row.setTang(safeTrim(rs.getString("tang")));
                    row.setLoaiPhong(safeTrim(rs.getString("tenLoaiPhong")));
                    row.setKhuVuc(safeTrim(rs.getString("khuVuc")));
                    row.setSucChuaChuan(rs.getInt("sucChuaChuan"));
                    row.setSucChuaToiDa(rs.getInt("sucChuaToiDa"));
                    row.setTrangThaiPhong(safeTrim(rs.getString("trangThai")));

                    for (int i = 0; i < dayCount; i++) {
                        LocalDate cellDate = startDate.plusDays(i);
                        DashboardGanttCell cell = new DashboardGanttCell();
                        cell.setDate(cellDate);
                        cell.setMaPhong(row.getMaPhong());
                        cell.setSoPhong(row.getSoPhong());
                        cell.setTang(row.getTang());
                        cell.setLoaiPhong(row.getLoaiPhong());
                        cell.setKhuVuc(row.getKhuVuc());
                        cell.setPhongTrangThai(row.getTrangThaiPhong());

                        // Nếu phòng đang bảo trì hoặc không hoạt động, ô Gantt mặc định là trạng thái bảo trì.
                        if (isMaintenanceLike(row.getTrangThaiPhong())) {
                            cell.setStatusCode(GANTT_STATUS_MAINTENANCE);
                            cell.setStatusText(row.getTrangThaiPhong().isEmpty() ? ROOM_STATUS_MAINTENANCE : row.getTrangThaiPhong());
                            cell.setSourceType("MAINTENANCE");
                            cell.setPriority(GANTT_PRIORITY_MAINTENANCE);
                        } else {
                            cell.setStatusCode(GANTT_STATUS_EMPTY);
                            cell.setStatusText(ROOM_STATUS_EMPTY);
                            cell.setSourceType("EMPTY");
                            cell.setPriority(GANTT_PRIORITY_EMPTY);
                        }
                        row.getCells().add(cell);
                    }
                    rows.put(Integer.valueOf(row.getMaPhong()), row);
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
        return rows;
    }    /**
     * Áp dụng trạng thái đang ở cho các ô Gantt dựa trên dữ liệu lưu trú đang hoạt động.
     *
     * Chỉ các lưu trú chưa check-out và có thời gian nằm trong khoảng hiển thị
     * mới được áp dụng lên ô Gantt.
     *
     * @param rowByRoomId map dòng Gantt theo mã phòng.
     * @param startDate ngày bắt đầu khoảng hiển thị.
     * @param endDate ngày kết thúc khoảng hiển thị.
     */
    private void applyOccupiedCells(Map<Integer, DashboardGanttRow> rowByRoomId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.maDatPhong, lt.maPhong, "
                + "ISNULL(kh.hoTen, N'-') AS hoTen, CAST(lt.checkIn AS DATE) AS stayFrom, "
                + "CAST(COALESCE(dp.ngayTraPhong, GETDATE()) AS DATE) AS stayTo "
                + "FROM dbo.LuuTru lt "
                + "JOIN dbo.DatPhong dp ON dp.maDatPhong = lt.maDatPhong "
                + "JOIN dbo.Phong p ON p.maPhong = lt.maPhong "
                + "LEFT JOIN dbo.KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE lt.checkIn IS NOT NULL "
                + "AND lt.checkOut IS NULL "
                + "AND lt.checkIn < ? "
                + "AND COALESCE(dp.ngayTraPhong, GETDATE()) >= ?";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return;
            }
            stmt.setTimestamp(1, Timestamp.valueOf(endDate.plusDays(1L).atStartOfDay()));
            stmt.setDate(2, Date.valueOf(startDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int maPhong = rs.getInt("maPhong");
                    DashboardGanttRow row = rowByRoomId.get(Integer.valueOf(maPhong));
                    if (row == null) {
                        continue;
                    }
                    LocalDate fromDate = toLocalDate(rs.getDate("stayFrom"));
                    LocalDate toDate = toLocalDate(rs.getDate("stayTo"));
                    if (fromDate == null) {
                        continue;
                    }
                    if (toDate == null) {
                        toDate = endDate;
                    }
                    for (DashboardGanttCell cell : row.getCells()) {
                        if (isDateInside(cell.getDate(), fromDate, toDate)) {
                            applyCellState(
                                    cell,
                                    GANTT_STATUS_OCCUPIED,
                                    ROOM_STATUS_OCCUPIED,
                                    "STAY",
                                    GANTT_PRIORITY_OCCUPIED,
                                    rs.getInt("maDatPhong"),
                                    rs.getInt("maLuuTru"),
                                    rs.getInt("maChiTietDatPhong"),
                                    safeTrim(rs.getString("hoTen")),
                                    fromDate,
                                    toDate
                            );
                        }
                    }
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
    }

    /**
     * Áp dụng trạng thái đã đặt/chờ check-in cho các ô Gantt dựa trên chi tiết đặt phòng.
     *
     * Chỉ các chi tiết đặt phòng chưa phát sinh lưu trú và có thời gian nằm trong khoảng hiển thị
     * mới được áp dụng lên ô Gantt.
     *
     * @param rowByRoomId map dòng Gantt theo mã phòng.
     * @param startDate ngày bắt đầu khoảng hiển thị.
     * @param endDate ngày kết thúc khoảng hiển thị.
     */
    private void applyBookedCells(Map<Integer, DashboardGanttRow> rowByRoomId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT dp.maDatPhong, ctdp.maChiTietDatPhong, ctdp.maPhong, "
                + "ISNULL(kh.hoTen, N'-') AS hoTen, ISNULL(dp.trangThai, N'') AS trangThai, "
                + "CAST(dp.ngayNhanPhong AS DATE) AS bookingFrom, CAST(dp.ngayTraPhong AS DATE) AS bookingTo "
                + "FROM dbo.DatPhong dp "
                + "JOIN dbo.ChiTietDatPhong ctdp ON ctdp.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN dbo.KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE ctdp.maPhong IS NOT NULL "
                + "AND ISNULL(dp.trangThai, N'') IN (?, ?, ?, ?, ?, ?, ?, ?) "
                + "AND dp.ngayNhanPhong < ? "
                + "AND dp.ngayTraPhong >= ? "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.LuuTru lt WHERE lt.maChiTietDatPhong = ctdp.maChiTietDatPhong)";

        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return;
            }
            stmt.setString(1, BOOKING_STATUS_BOOKED);
            stmt.setString(2, BOOKING_STATUS_CONFIRMED);
            stmt.setString(3, BOOKING_STATUS_DEPOSITED);
            stmt.setString(4, BOOKING_STATUS_PENDING_CHECKIN);
            stmt.setString(5, BOOKING_STATUS_ACTIVE);
            stmt.setString(6, BOOKING_STATUS_CHECKED_IN);
            stmt.setString(7, BOOKING_STATUS_PARTIAL_CHECKOUT);
            stmt.setString(8, BOOKING_STATUS_STAYING);
            stmt.setDate(9, Date.valueOf(endDate.plusDays(1L)));
            stmt.setDate(10, Date.valueOf(startDate));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int maPhong = rs.getInt("maPhong");
                    DashboardGanttRow row = rowByRoomId.get(Integer.valueOf(maPhong));
                    if (row == null) {
                        continue;
                    }
                    LocalDate fromDate = toLocalDate(rs.getDate("bookingFrom"));
                    LocalDate toDate = toLocalDate(rs.getDate("bookingTo"));
                    if (fromDate == null) {
                        continue;
                    }
                    if (toDate == null) {
                        toDate = fromDate;
                    }
                    String bookingStatus = safeTrim(rs.getString("trangThai"));
                    String statusCode = resolveBookingCellStatusCode(bookingStatus);
                    if (statusCode == null) {
                        continue;
                    }
                    String statusText = resolveBookingCellStatusText(statusCode, bookingStatus);
                    for (DashboardGanttCell cell : row.getCells()) {
                        if (isDateInside(cell.getDate(), fromDate, toDate)) {
                            applyCellState(
                                    cell,
                                    statusCode,
                                    statusText,
                                    "BOOKING",
                                    resolveGanttPriority(statusCode),
                                    rs.getInt("maDatPhong"),
                                    0,
                                    rs.getInt("maChiTietDatPhong"),
                                    safeTrim(rs.getString("hoTen")),
                                    fromDate,
                                    toDate
                            );
                        }
                    }
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
        }
    }

    /**
     * Chuyển trạng thái booking thành mã trạng thái hiển thị trên ô Gantt.
     *
     * @param bookingStatus trạng thái booking cần xử lý.
     * @return mã trạng thái Gantt, hoặc null nếu trạng thái không cần hiển thị.
     */
    private String resolveBookingCellStatusCode(String bookingStatus) {
        String status = safeTrim(bookingStatus);
        if (BOOKING_STATUS_BOOKED.equalsIgnoreCase(status)) {
            return GANTT_STATUS_BOOKED;
        }
        if (BOOKING_STATUS_CONFIRMED.equalsIgnoreCase(status)
                || BOOKING_STATUS_DEPOSITED.equalsIgnoreCase(status)
                || BOOKING_STATUS_PENDING_CHECKIN.equalsIgnoreCase(status)
                || BOOKING_STATUS_ACTIVE.equalsIgnoreCase(status)
                || BOOKING_STATUS_CHECKED_IN.equalsIgnoreCase(status)
                || BOOKING_STATUS_PARTIAL_CHECKOUT.equalsIgnoreCase(status)
                || BOOKING_STATUS_STAYING.equalsIgnoreCase(status)) {
            return GANTT_STATUS_PENDING_CHECKIN;
        }
        return null;
    }

    /**
     * Xác định nội dung trạng thái hiển thị trên ô Gantt từ mã trạng thái.
     *
     * @param statusCode mã trạng thái Gantt.
     * @param bookingStatus trạng thái booking gốc.
     * @return nội dung trạng thái hiển thị.
     */
    private String resolveBookingCellStatusText(String statusCode, String bookingStatus) {
        if (GANTT_STATUS_PENDING_CHECKIN.equalsIgnoreCase(statusCode)) {
            return ROOM_STATUS_PENDING_CHECKIN;
        }
        if (GANTT_STATUS_BOOKED.equalsIgnoreCase(statusCode)) {
            return GANTT_STATUS_TEXT_BOOKED;
        }
        return safeTrim(bookingStatus);
    }

    /**
     * Lấy độ ưu tiên của một mã trạng thái Gantt.
     *
     * Trạng thái có độ ưu tiên cao hơn sẽ được phép ghi đè trạng thái có độ ưu tiên thấp hơn.
     *
     * @param statusCode mã trạng thái Gantt.
     * @return độ ưu tiên tương ứng.
     */
    private int resolveGanttPriority(String statusCode) {
        if (GANTT_STATUS_MAINTENANCE.equalsIgnoreCase(statusCode)) {
            return GANTT_PRIORITY_MAINTENANCE;
        }
        if (GANTT_STATUS_OCCUPIED.equalsIgnoreCase(statusCode)) {
            return GANTT_PRIORITY_OCCUPIED;
        }
        if (GANTT_STATUS_PENDING_CHECKIN.equalsIgnoreCase(statusCode)) {
            return GANTT_PRIORITY_PENDING_CHECKIN;
        }
        if (GANTT_STATUS_BOOKED.equalsIgnoreCase(statusCode)) {
            return GANTT_PRIORITY_BOOKED;
        }
        return GANTT_PRIORITY_EMPTY;
    }

    /**
     * Gán trạng thái và thông tin nguồn dữ liệu cho một ô Gantt.
     *
     * Nếu trạng thái mới có độ ưu tiên thấp hơn trạng thái hiện tại của ô,
     * method sẽ bỏ qua để không làm mất trạng thái quan trọng hơn.
     *
     * @param cell ô Gantt cần cập nhật.
     * @param statusCode mã trạng thái.
     * @param statusText nội dung trạng thái hiển thị.
     * @param sourceType loại nguồn dữ liệu tạo trạng thái.
     * @param priority độ ưu tiên của trạng thái.
     * @param maDatPhong mã đặt phòng liên quan.
     * @param maLuuTru mã lưu trú liên quan.
     * @param maChiTietDatPhong mã chi tiết đặt phòng liên quan.
     * @param customerName tên khách hàng liên quan.
     * @param fromDate ngày bắt đầu hiệu lực.
     * @param toDate ngày kết thúc hiệu lực.
     */
    private void applyCellState(DashboardGanttCell cell, String statusCode, String statusText, String sourceType,
                                int priority, int maDatPhong, int maLuuTru, int maChiTietDatPhong,
                                String customerName, LocalDate fromDate, LocalDate toDate) {
        if (cell == null || priority < cell.getPriority()) {
            return;
        }
        cell.setStatusCode(statusCode);
        cell.setStatusText(statusText);
        cell.setSourceType(sourceType);
        cell.setPriority(priority);
        cell.setMaDatPhong(maDatPhong);
        cell.setMaLuuTru(maLuuTru);
        cell.setMaChiTietDatPhong(maChiTietDatPhong);
        cell.setCustomerName(customerName);
        cell.setFromDate(fromDate);
        cell.setToDate(toDate);
    }

    /**
     * Kiểm tra một ngày có nằm trong khoảng từ ngày bắt đầu đến ngày kết thúc hay không.
     *
     * @param targetDate ngày cần kiểm tra.
     * @param fromDate ngày bắt đầu.
     * @param toDate ngày kết thúc, nếu null sẽ dùng fromDate.
     * @return true nếu targetDate nằm trong khoảng [fromDate, toDate].
     */
    private boolean isDateInside(LocalDate targetDate, LocalDate fromDate, LocalDate toDate) {
        if (targetDate == null || fromDate == null) {
            return false;
        }
        LocalDate normalizedToDate = toDate == null ? fromDate : toDate;
        return !targetDate.isBefore(fromDate) && !targetDate.isAfter(normalizedToDate);
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
     * Kiểm tra trạng thái phòng có thuộc nhóm bảo trì hoặc không hoạt động hay không.
     *
     * @param trangThai trạng thái phòng cần kiểm tra.
     * @return true nếu phòng thuộc nhóm không thể sử dụng.
     */
    private boolean isMaintenanceLike(String trangThai) {
        String normalized = safeTrim(trangThai);
        return ROOM_STATUS_MAINTENANCE.equalsIgnoreCase(normalized)
                || "Không hoạt động".equalsIgnoreCase(normalized);
    }
    /**
     * Tính tổng doanh thu trong khoảng thời gian truyền vào.
     *
     * Khoảng thời gian được tính theo dạng [startInclusive, endExclusive),
     * nghĩa là lấy từ ngày bắt đầu và không bao gồm ngày kết thúc.
     *
     * Nếu bảng ThanhToan có cột loaiGiaoDich, method chỉ lấy giao dịch
     * có loại mặc định PAYMENT_TYPE_DEFAULT.
     *
     * @param startInclusive ngày bắt đầu tính doanh thu.
     * @param endExclusive ngày kết thúc, không bao gồm ngày này.
     * @return tổng doanh thu trong khoảng thời gian.
     */
    private double queryRevenueBetween(LocalDate startInclusive, LocalDate endExclusive) {
        try (Connection con = getReadyConnection()) {
            if (con == null) {
                return 0d;
            }
            boolean hasTransactionType = hasColumn(con, "ThanhToan", "loaiGiaoDich");
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ISNULL(SUM(soTien), 0) FROM ThanhToan ")
                    .append("WHERE ngayThanhToan >= ? AND ngayThanhToan < ? ");
            if (hasTransactionType) {
                sql.append("AND ISNULL(loaiGiaoDich, ?) = ? ");
            }

            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                int index = 1;
                stmt.setTimestamp(index++, Timestamp.valueOf(startInclusive.atStartOfDay()));
                stmt.setTimestamp(index++, Timestamp.valueOf(endExclusive.atStartOfDay()));
                if (hasTransactionType) {
                    stmt.setString(index++, PAYMENT_TYPE_DEFAULT);
                    stmt.setString(index, PAYMENT_TYPE_DEFAULT);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getDouble(1) : 0d;
                }
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
            return 0d;
        }
    }

    /**
     * Thực hiện câu truy vấn đếm đơn giản và trả về kết quả COUNT.
     *
     * @param sql câu SQL trả về một giá trị COUNT.
     * @return số lượng đếm được, hoặc 0 nếu có lỗi.
     */
    private int queryForCount(String sql) {
        try (Connection con = getReadyConnection();
             PreparedStatement stmt = con == null ? null : con.prepareStatement(sql)) {
            if (con == null || stmt == null) {
                return 0;
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            setLastError(ex.getMessage());
            return 0;
        }
    }

    /**
     * Khởi tạo danh sách điểm biểu đồ theo từng ngày trong khoảng truyền vào.
     *
     * Mỗi điểm ban đầu có giá trị 0 để đảm bảo biểu đồ không bị thiếu ngày.
     *
     * @param startDate ngày bắt đầu.
     * @param endDate ngày kết thúc.
     * @return danh sách điểm biểu đồ theo ngày.
     */
    private List<DashboardChartPoint> initDateSeries(LocalDate startDate, LocalDate endDate) {
        List<DashboardChartPoint> points = new ArrayList<DashboardChartPoint>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            points.add(new DashboardChartPoint(cursor.format(CHART_LABEL_FORMAT), 0d));
            cursor = cursor.plusDays(1L);
        }
        return points;
    }

    /**
     * Gán giá trị thực tế vào danh sách điểm biểu đồ đã khởi tạo.
     *
     * Method này dựa trên thứ tự ngày trong 7 ngày gần nhất để cập nhật value
     * cho từng DashboardChartPoint.
     *
     * @param points danh sách điểm biểu đồ cần cập nhật.
     * @param valuesByDate map chứa giá trị thực tế theo ngày.
     */
    private void applySeriesValues(List<DashboardChartPoint> points, Map<LocalDate, Double> valuesByDate) {
        LocalDate cursor = LocalDate.now().minusDays(points.size() - 1L);
        for (DashboardChartPoint point : points) {
            if (valuesByDate.containsKey(cursor)) {
                point.setValue(valuesByDate.get(cursor).doubleValue());
            }
            cursor = cursor.plusDays(1L);
        }
    }

    /**
     * Kiểm tra một bảng có tồn tại cột cụ thể hay không.
     *
     * Method này thường dùng để tránh lỗi khi database ở các phiên bản khác nhau
     * có thể thiếu một số cột mới.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param tableName tên bảng cần kiểm tra.
     * @param columnName tên cột cần kiểm tra.
     * @return true nếu cột tồn tại, false nếu không tồn tại hoặc có lỗi.
     */
    private boolean hasColumn(Connection con, String tableName, String columnName) {
        String sql = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Lấy connection hiện tại từ lớp ConnectDB.
     *
     * @return connection tới cơ sở dữ liệu, hoặc null nếu chưa kết nối.
     */
    private Connection getReadyConnection() {
        return ConnectDB.getConnection();
    }

    /**
     * Lấy giá trị số nguyên trong map theo key.
     *
     * @param map map chứa dữ liệu thống kê.
     * @param key khóa cần lấy.
     * @return giá trị tương ứng với key, hoặc 0 nếu key không tồn tại.
     */
    private int valueOf(Map<String, Integer> map, String key) {
        Integer value = map.get(key);
        return value == null ? 0 : value.intValue();
    }

    /**
     * Tính tổng nhiều giá trị trong map theo danh sách key.
     *
     * @param map map chứa dữ liệu thống kê.
     * @param keys danh sách key cần cộng.
     * @return tổng giá trị của các key.
     */
    private int sum(Map<String, Integer> map, String... keys) {
        int total = 0;
        for (String key : keys) {
            total += valueOf(map, key);
        }
        return total;
    }

    /**
     * Tạo chuỗi mô tả đối tượng công việc gồm tên khách hàng và phòng.
     *
     * @param customerName tên khách hàng.
     * @param roomSummary danh sách/tóm tắt phòng.
     * @return chuỗi mô tả dùng để hiển thị trên Dashboard.
     */
    private String buildTarget(String customerName, String roomSummary) {
        String customer = safeTrim(customerName);
        String room = safeTrim(roomSummary);
        if (customer.isEmpty()) {
            customer = "Khach hang";
        }
        if (room.isEmpty() || "-".equals(room)) {
            return customer;
        }
        return customer + " - Phong " + room;
    }

    /**
     * Định dạng java.sql.Date thành chuỗi ngày để hiển thị.
     *
     * @param value ngày cần định dạng.
     * @return chuỗi ngày đã định dạng, hoặc "-" nếu value null.
     */
    private String formatDate(Date value) {
        if (value == null) {
            return "-";
        }
        return TASK_DATE_FORMAT.format(value.toLocalDate());
    }

    /**
     * Định dạng Timestamp thành chuỗi ngày giờ để hiển thị.
     *
     * @param value thời gian cần định dạng.
     * @return chuỗi ngày giờ đã định dạng, hoặc "-" nếu value null.
     */
    private String formatTimestamp(Timestamp value) {
        if (value == null) {
            return "-";
        }
        return TASK_TIME_FORMAT.format(value.toLocalDateTime());
    }

    /**
     * Chuyển java.sql.Date sang Timestamp tại thời điểm bắt đầu ngày.
     *
     * @param value ngày cần chuyển.
     * @return Timestamp tương ứng, hoặc null nếu value null.
     */
    private Timestamp toTimestamp(Date value) {
        if (value == null) {
            return null;
        }
        return Timestamp.valueOf(value.toLocalDate().atStartOfDay());
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
     * Xóa thông báo lỗi gần nhất trước khi thực hiện thao tác mới.
     */
    private void clearLastError() {
        lastErrorMessage = "";
    }

    /**
     * Lưu thông báo lỗi gần nhất.
     *
     * Chỉ cập nhật khi message khác null và không rỗng.
     *
     * @param message nội dung lỗi cần lưu.
     */
    private void setLastError(String message) {
        if (message != null && !message.trim().isEmpty()) {
            lastErrorMessage = message.trim();
        }
    }
}