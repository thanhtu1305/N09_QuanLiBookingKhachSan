package dao;

import db.ConnectDB;
import entity.DashboardChartPoint;
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

public class DashboardDAO {
    private static final String ROOM_STATUS_OPERATIONAL = "Ho\u1ea1t \u0111\u1ed9ng";
    private static final String ROOM_STATUS_EMPTY = "Tr\u1ed1ng";
    private static final String ROOM_STATUS_OCCUPIED = "\u0110ang \u1edf";
    private static final String ROOM_STATUS_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String ROOM_STATUS_MAINTENANCE = "B\u1ea3o tr\u00ec";

    private static final String BOOKING_STATUS_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String BOOKING_STATUS_CONFIRMED = "\u0110\u00e3 x\u00e1c nh\u1eadn";
    private static final String BOOKING_STATUS_DEPOSITED = "\u0110\u00e3 c\u1ecdc";
    private static final String BOOKING_STATUS_PENDING_CHECKIN = "Ch\u1edd check-in";
    private static final String BOOKING_STATUS_STAYING = "\u0110ang l\u01b0u tr\u00fa";
    private static final String BOOKING_STATUS_CHECKED_OUT = "\u0110\u00e3 check-out";
    private static final String BOOKING_STATUS_PAID = "\u0110\u00e3 thanh to\u00e1n";

    private static final String INVOICE_STATUS_PENDING = "Ch\u1edd thanh to\u00e1n";
    private static final String PAYMENT_TYPE_DEFAULT = "THANH_TOAN";
    private static final DateTimeFormatter CHART_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter TASK_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TASK_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

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

    public int getTodayBookingCount() {
        String sql = "SELECT COUNT(1) FROM DatPhong WHERE CAST(ngayDat AS DATE) = CAST(GETDATE() AS DATE)";
        return queryForCount(sql);
    }

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

    public int getCheckoutDueTodayCount() {
        String sql = "SELECT COUNT(DISTINCT dp.maDatPhong) "
                + "FROM LuuTru lt "
                + "JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong "
                + "WHERE CAST(lt.checkOut AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND dp.trangThai = ?";

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

    public double getRevenueToday() {
        LocalDate today = LocalDate.now();
        return queryRevenueBetween(today, today.plusDays(1L));
    }

    public double getRevenueThisMonth() {
        LocalDate firstDay = YearMonth.now().atDay(1);
        return queryRevenueBetween(firstDay, firstDay.plusMonths(1L));
    }

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

    private List<DashboardTaskRow> getCheckoutDueTodayTasks() {
        List<DashboardTaskRow> rows = new ArrayList<DashboardTaskRow>();
        String sql = "SELECT TOP 6 dp.maDatPhong, kh.hoTen, MIN(lt.checkOut) AS checkOutDue, dp.trangThai, "
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
                + "WHERE CAST(lt.checkOut AS DATE) = CAST(GETDATE() AS DATE) "
                + "AND dp.trangThai = ? "
                + "GROUP BY dp.maDatPhong, kh.hoTen, dp.trangThai, roomSummary.soPhong "
                + "ORDER BY MIN(lt.checkOut) ASC, dp.maDatPhong DESC";

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

    private List<DashboardChartPoint> initDateSeries(LocalDate startDate, LocalDate endDate) {
        List<DashboardChartPoint> points = new ArrayList<DashboardChartPoint>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            points.add(new DashboardChartPoint(cursor.format(CHART_LABEL_FORMAT), 0d));
            cursor = cursor.plusDays(1L);
        }
        return points;
    }

    private void applySeriesValues(List<DashboardChartPoint> points, Map<LocalDate, Double> valuesByDate) {
        LocalDate cursor = LocalDate.now().minusDays(points.size() - 1L);
        for (DashboardChartPoint point : points) {
            if (valuesByDate.containsKey(cursor)) {
                point.setValue(valuesByDate.get(cursor).doubleValue());
            }
            cursor = cursor.plusDays(1L);
        }
    }

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

    private Connection getReadyConnection() {
        return ConnectDB.getConnection();
    }

    private int valueOf(Map<String, Integer> map, String key) {
        Integer value = map.get(key);
        return value == null ? 0 : value.intValue();
    }

    private int sum(Map<String, Integer> map, String... keys) {
        int total = 0;
        for (String key : keys) {
            total += valueOf(map, key);
        }
        return total;
    }

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

    private String formatDate(Date value) {
        if (value == null) {
            return "-";
        }
        return TASK_DATE_FORMAT.format(value.toLocalDate());
    }

    private String formatTimestamp(Timestamp value) {
        if (value == null) {
            return "-";
        }
        return TASK_TIME_FORMAT.format(value.toLocalDateTime());
    }

    private Timestamp toTimestamp(Date value) {
        if (value == null) {
            return null;
        }
        return Timestamp.valueOf(value.toLocalDate().atStartOfDay());
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        if (message != null && !message.trim().isEmpty()) {
            lastErrorMessage = message.trim();
        }
    }
}
