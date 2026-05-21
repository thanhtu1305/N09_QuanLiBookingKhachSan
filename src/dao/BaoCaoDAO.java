package dao;

import db.ConnectDB;
import entity.HoaDon;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BaoCaoDAO {
    private static final String PAYMENT_DONE = "Ho\u00e0n t\u1ea5t";
    private static final String PAYMENT_TYPE_DEFAULT = "THANH_TOAN";
    private static final String INVOICE_PENDING = "Ch\u1edd thanh to\u00e1n";
    private static final String INVOICE_PAID = "\u0110\u00e3 thanh to\u00e1n";
    private static final String ROOM_OCCUPIED = "\u0110ang \u1edf";
    private static final String ROOM_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String ROOM_PENDING_CHECKIN = "Ch\u1edd check-in";
    private static final String ROOM_EMPTY = "Tr\u1ed1ng";
    private static final String ROOM_ACTIVE = "Ho\u1ea1t \u0111\u1ed9ng";
    private static final String ROOM_READY = "S\u1eb5n s\u00e0ng";
    private static final String ROOM_MAINTENANCE = "B\u1ea3o tr\u00ec";
    private static final String ROOM_INACTIVE = "Kh\u00f4ng ho\u1ea1t \u0111\u1ed9ng";
    private static final String ROOM_STOPPED = "Ng\u1eebng ho\u1ea1t \u0111\u1ed9ng";
    private static final String ROOM_REPAIRING = "\u0110ang s\u1eeda";
    private static final String BOOKING_BOOKED = "\u0110\u00e3 \u0111\u1eb7t";
    private static final String BOOKING_CONFIRMED = "\u0110\u00e3 x\u00e1c nh\u1eadn";
    private static final String BOOKING_DEPOSITED = "\u0110\u00e3 c\u1ecdc";
    private static final String BOOKING_PENDING_CHECKIN = "Ch\u1edd check-in";
    private static final String BOOKING_STAYING = "\u0110ang l\u01b0u tr\u00fa";
    private static final String BOOKING_ACTIVE = "\u0110ang \u1edf";
    private static final String BOOKING_CHECKED_IN = "\u0110\u00e3 check-in";
    private static final String BOOKING_PARTIAL_CHECKOUT = "Check-out m\u1ed9t ph\u1ea7n";
    private static final String BOOKING_PAID = "\u0110\u00e3 thanh to\u00e1n";
    private static final String BOOKING_CANCELLED = "\u0110\u00e3 h\u1ee7y";
    private static final String BOOKING_CANCELLED_ALT = "H\u1ee7y booking";
    private static final String UNKNOWN = "Kh\u00e1c";

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<HoaDon> getAll() {
        clearLastError();
        List<HoaDon> result = new ArrayList<HoaDon>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "HoaDon")) {
            return result;
        }

        String sql = "SELECT maHoaDon, maDatPhong, maKhachHang, ngayLap, "
                + invoiceAmountExpression("HoaDon") + " AS tongTien, "
                + "ISNULL(tienPhong, 0) AS tienPhong, ISNULL(tienDichVu, 0) AS tienDichVu, "
                + "ISNULL(phuThu, 0) AS phuThu, ISNULL(giamGia, 0) AS giamGia, "
                + "ISNULL(tienCocTru, 0) AS tienCocTru, "
                + "ISNULL(trangThai, ?) AS trangThai "
                + "FROM HoaDon ORDER BY ngayLap DESC, maHoaDon DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, INVOICE_PENDING);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setMaHoaDon(String.valueOf(rs.getInt("maHoaDon")));
                    hoaDon.setMaDatPhong(String.valueOf(rs.getInt("maDatPhong")));
                    hoaDon.setMaKhachHang(String.valueOf(rs.getInt("maKhachHang")));
                    Timestamp ngayLap = rs.getTimestamp("ngayLap");
                    hoaDon.setNgayLap(ngayLap == null ? "" : ngayLap.toString());
                    hoaDon.setTongTienPhong(rs.getDouble("tienPhong"));
                    hoaDon.setTongTienDichVu(rs.getDouble("tienDichVu"));
                    hoaDon.setTongPhuThu(rs.getDouble("phuThu"));
                    hoaDon.setTongGiamGia(rs.getDouble("giamGia"));
                    hoaDon.setTongTruDatCoc(rs.getDouble("tienCocTru"));
                    hoaDon.setTongThanhToan(rs.getDouble("tongTien"));
                    hoaDon.setTrangThai(safeTrim(rs.getString("trangThai")));
                    result.add(hoaDon);
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public boolean insert(HoaDon hoaDon) {
        setLastError("BaoCaoDAO chi doc du lieu bao cao.");
        return false;
    }

    public boolean update(HoaDon hoaDon) {
        setLastError("BaoCaoDAO chi doc du lieu bao cao.");
        return false;
    }

    public boolean delete(String maHoaDon) {
        setLastError("BaoCaoDAO chi doc du lieu bao cao.");
        return false;
    }

    public HoaDon findById(String maHoaDon) {
        clearLastError();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (invoiceId == null) {
            return null;
        }
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "HoaDon")) {
            return null;
        }

        String sql = "SELECT maHoaDon, maDatPhong, maKhachHang, ngayLap, "
                + invoiceAmountExpression("HoaDon") + " AS tongTien, "
                + "ISNULL(tienPhong, 0) AS tienPhong, ISNULL(tienDichVu, 0) AS tienDichVu, "
                + "ISNULL(phuThu, 0) AS phuThu, ISNULL(giamGia, 0) AS giamGia, "
                + "ISNULL(tienCocTru, 0) AS tienCocTru, ISNULL(trangThai, ?) AS trangThai "
                + "FROM HoaDon WHERE maHoaDon = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, INVOICE_PENDING);
            stmt.setInt(2, invoiceId.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHoaDon(String.valueOf(rs.getInt("maHoaDon")));
                hoaDon.setMaDatPhong(String.valueOf(rs.getInt("maDatPhong")));
                hoaDon.setMaKhachHang(String.valueOf(rs.getInt("maKhachHang")));
                Timestamp ngayLap = rs.getTimestamp("ngayLap");
                hoaDon.setNgayLap(ngayLap == null ? "" : ngayLap.toString());
                hoaDon.setTongTienPhong(rs.getDouble("tienPhong"));
                hoaDon.setTongTienDichVu(rs.getDouble("tienDichVu"));
                hoaDon.setTongPhuThu(rs.getDouble("phuThu"));
                hoaDon.setTongGiamGia(rs.getDouble("giamGia"));
                hoaDon.setTongTruDatCoc(rs.getDouble("tienCocTru"));
                hoaDon.setTongThanhToan(rs.getDouble("tongTien"));
                hoaDon.setTrangThai(safeTrim(rs.getString("trangThai")));
                return hoaDon;
            }
        } catch (Exception ex) {
            setLastError(ex);
            return null;
        }
    }

    public RevenueSummary getRevenueSummary(LocalDate from, LocalDate to) {
        clearLastError();
        RevenueSummary summary = new RevenueSummary();
        Connection con = getReadyConnection();
        if (con == null) {
            return summary;
        }

        DateRange range = DateRange.of(from, to);
        if (hasTable(con, "HoaDon")) {
            loadInvoiceSummary(con, range, summary);
        }
        if (hasTable(con, "ThanhToan")) {
            loadPaymentSummary(con, range, summary);
        }
        return summary;
    }

    public List<RevenueDateStat> getRevenueByDate(LocalDate from, LocalDate to) {
        clearLastError();
        LinkedHashMap<LocalDate, RevenueDateStat> values = new LinkedHashMap<LocalDate, RevenueDateStat>();
        Connection con = getReadyConnection();
        if (con == null) {
            return new ArrayList<RevenueDateStat>();
        }
        DateRange range = DateRange.of(from, to);

        if (hasTable(con, "HoaDon")) {
            String sql = "SELECT CAST(ngayLap AS DATE) AS ngay, "
                    + "COUNT(1) AS soHoaDon, "
                    + "ISNULL(SUM(ISNULL(tienPhong, 0)), 0) AS tienPhong, "
                    + "ISNULL(SUM(ISNULL(tienDichVu, 0)), 0) AS tienDichVu, "
                    + "ISNULL(SUM(" + invoiceAmountExpression("HoaDon") + "), 0) AS tongHoaDon "
                    + "FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ? "
                    + "GROUP BY CAST(ngayLap AS DATE) ORDER BY CAST(ngayLap AS DATE)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                bindRange(stmt, range, 1);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDate date = toLocalDate(rs.getDate("ngay"));
                        if (date == null) {
                            continue;
                        }
                        RevenueDateStat stat = getRevenueDateStat(values, date);
                        stat.invoiceCount = rs.getInt("soHoaDon");
                        stat.roomRevenue = rs.getDouble("tienPhong");
                        stat.serviceRevenue = rs.getDouble("tienDichVu");
                        stat.invoiceRevenue = rs.getDouble("tongHoaDon");
                    }
                }
            } catch (Exception ex) {
                setLastError(ex);
            }
        }

        if (hasTable(con, "ThanhToan")) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT CAST(ngayThanhToan AS DATE) AS ngay, COUNT(1) AS soGiaoDich, ")
                    .append("COUNT(DISTINCT maHoaDon) AS soHoaDonDaThu, ")
                    .append("ISNULL(SUM(ISNULL(soTien, 0)), 0) AS thucThu ")
                    .append("FROM ThanhToan WHERE ngayThanhToan >= ? AND ngayThanhToan < ? ");
            appendCompletedPaymentFilter(con, sql, "ThanhToan");
            sql.append("GROUP BY CAST(ngayThanhToan AS DATE) ORDER BY CAST(ngayThanhToan AS DATE)");

            try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
                int index = bindRange(stmt, range, 1);
                bindCompletedPaymentFilter(con, stmt, index);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        LocalDate date = toLocalDate(rs.getDate("ngay"));
                        if (date == null) {
                            continue;
                        }
                        RevenueDateStat stat = getRevenueDateStat(values, date);
                        stat.paymentCount = rs.getInt("soGiaoDich");
                        stat.paidInvoiceCount = rs.getInt("soHoaDonDaThu");
                        stat.paidRevenue = rs.getDouble("thucThu");
                    }
                }
            } catch (Exception ex) {
                setLastError(ex);
            }
        }
        return new ArrayList<RevenueDateStat>(values.values());
    }

    public List<AmountStat> getRevenueByPaymentMethod(LocalDate from, LocalDate to) {
        clearLastError();
        List<AmountStat> result = new ArrayList<AmountStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "ThanhToan")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ISNULL(phuongThuc, N'Kh\u00e1c') AS label, COUNT(1) AS soLuong, ")
                .append("ISNULL(SUM(ISNULL(soTien, 0)), 0) AS tongTien ")
                .append("FROM ThanhToan WHERE ngayThanhToan >= ? AND ngayThanhToan < ? ");
        appendCompletedPaymentFilter(con, sql, "ThanhToan");
        sql.append("GROUP BY ISNULL(phuongThuc, N'Kh\u00e1c') ORDER BY tongTien DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int index = bindRange(stmt, range, 1);
            bindCompletedPaymentFilter(con, stmt, index);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new AmountStat(
                            safeLabel(rs.getString("label")),
                            rs.getInt("soLuong"),
                            rs.getDouble("tongTien")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<RevenueDetail> getRevenueDetails(LocalDate from, LocalDate to) {
        clearLastError();
        List<RevenueDetail> result = new ArrayList<RevenueDetail>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "HoaDon")) {
            return result;
        }
        DateRange range = DateRange.of(from, to);
        String paymentFilter = buildPaymentFilterForSubQuery(con, "tt");
        String sql = "SELECT hd.maHoaDon, hd.ngayLap, ISNULL(kh.hoTen, N'') AS khachHang, "
                + "ISNULL(hd.trangThai, ?) AS trangThai, "
                + "ISNULL(hd.tienPhong, 0) AS tienPhong, ISNULL(hd.tienDichVu, 0) AS tienDichVu, "
                + invoiceAmountExpression("hd") + " AS tongHoaDon, "
                + "ISNULL(pay.daThu, 0) AS daThu, ISNULL(pay.phuongThuc, N'') AS phuongThuc "
                + "FROM HoaDon hd "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = hd.maKhachHang "
                + "LEFT JOIN (SELECT tt.maHoaDon, SUM(ISNULL(tt.soTien, 0)) AS daThu, "
                + "STUFF((SELECT DISTINCT N', ' + ISNULL(tt2.phuongThuc, N'Kh\u00e1c') "
                + "       FROM ThanhToan tt2 WHERE tt2.maHoaDon = tt.maHoaDon " + buildPaymentFilterForSubQuery(con, "tt2")
                + "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS phuongThuc "
                + "FROM ThanhToan tt WHERE 1 = 1 " + paymentFilter + " GROUP BY tt.maHoaDon) pay "
                + "ON pay.maHoaDon = hd.maHoaDon "
                + "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? "
                + "ORDER BY hd.ngayLap DESC, hd.maHoaDon DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setString(index++, INVOICE_PENDING);
            index = bindPaymentSubQueryParams(con, stmt, index);
            index = bindPaymentSubQueryParams(con, stmt, index);
            bindRange(stmt, range, index);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new RevenueDetail(
                            rs.getInt("maHoaDon"),
                            toLocalDateTime(rs.getTimestamp("ngayLap")),
                            safeTrim(rs.getString("khachHang")),
                            safeTrim(rs.getString("trangThai")),
                            safeTrim(rs.getString("phuongThuc")),
                            rs.getDouble("tienPhong"),
                            rs.getDouble("tienDichVu"),
                            rs.getDouble("tongHoaDon"),
                            rs.getDouble("daThu")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public BookingSummary getBookingSummary(LocalDate from, LocalDate to) {
        clearLastError();
        BookingSummary summary = new BookingSummary();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "DatPhong")) {
            return summary;
        }

        DateRange range = DateRange.of(from, to);
        String sql = "SELECT COUNT(1) AS tongBooking, "
                + "ISNULL(SUM(ISNULL(soLuongPhong, 0)), 0) AS soPhongDat, "
                + "ISNULL(SUM(ISNULL(soNguoi, 0)), 0) AS soKhach "
                + "FROM DatPhong WHERE ngayDat >= ? AND ngayDat < ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.totalBookings = rs.getInt("tongBooking");
                    summary.bookedRooms = rs.getInt("soPhongDat");
                    summary.guests = rs.getInt("soKhach");
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }

        for (CountStat stat : getBookingStatusStats(from, to)) {
            String label = normalize(stat.label);
            if (isConfirmedStatus(label)) {
                summary.confirmedBookings += stat.count;
            }
            if (isPendingCheckinStatus(label)) {
                summary.pendingCheckinBookings += stat.count;
            }
            if (isCancelledStatus(label)) {
                summary.cancelledBookings += stat.count;
            }
        }
        summary.walkInBookings = getWalkInBookingCount(con, range);
        return summary;
    }

    public List<BookingDateStat> getBookingByDate(LocalDate from, LocalDate to) {
        clearLastError();
        List<BookingDateStat> result = new ArrayList<BookingDateStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "DatPhong")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        String sql = "SELECT CAST(ngayDat AS DATE) AS ngay, COUNT(1) AS tongBooking, "
                + "SUM(CASE WHEN trangThai IN (?, ?, ?, ?) THEN 1 ELSE 0 END) AS daXacNhan, "
                + "SUM(CASE WHEN trangThai IN (?, ?, ?, ?) THEN 1 ELSE 0 END) AS choCheckIn, "
                + "SUM(CASE WHEN trangThai IN (?, ?) THEN 1 ELSE 0 END) AS daHuy "
                + "FROM DatPhong WHERE ngayDat >= ? AND ngayDat < ? "
                + "GROUP BY CAST(ngayDat AS DATE) ORDER BY CAST(ngayDat AS DATE)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setString(index++, BOOKING_BOOKED);
            stmt.setString(index++, BOOKING_CONFIRMED);
            stmt.setString(index++, BOOKING_DEPOSITED);
            stmt.setString(index++, BOOKING_PAID);
            stmt.setString(index++, BOOKING_PENDING_CHECKIN);
            stmt.setString(index++, BOOKING_ACTIVE);
            stmt.setString(index++, BOOKING_CHECKED_IN);
            stmt.setString(index++, BOOKING_STAYING);
            stmt.setString(index++, BOOKING_CANCELLED);
            stmt.setString(index++, BOOKING_CANCELLED_ALT);
            bindRange(stmt, range, index);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new BookingDateStat(
                            toLocalDate(rs.getDate("ngay")),
                            rs.getInt("tongBooking"),
                            rs.getInt("daXacNhan"),
                            rs.getInt("choCheckIn"),
                            rs.getInt("daHuy"),
                            0
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<CountStat> getBookingStatusStats(LocalDate from, LocalDate to) {
        return queryBookingGroupStats(from, to, "ISNULL(trangThai, N'Kh\u00e1c')");
    }

    public List<CountStat> getBookingSourceStats(LocalDate from, LocalDate to) {
        clearLastError();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "DatPhong") || !hasColumn(con, "DatPhong", "nguonDatPhong")) {
            return new ArrayList<CountStat>();
        }
        return queryBookingGroupStats(from, to, "ISNULL(nguonDatPhong, N'Kh\u00e1c')");
    }

    public List<BookingDetail> getBookingDetails(LocalDate from, LocalDate to) {
        clearLastError();
        List<BookingDetail> result = new ArrayList<BookingDetail>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "DatPhong")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        String sourceExpr = hasColumn(con, "DatPhong", "nguonDatPhong") ? "ISNULL(dp.nguonDatPhong, N'')" : "N''";
        String sql = "SELECT dp.maDatPhong, dp.ngayDat, dp.ngayNhanPhong, dp.ngayTraPhong, "
                + "ISNULL(kh.hoTen, N'') AS khachHang, ISNULL(dp.trangThai, N'') AS trangThai, "
                + sourceExpr + " AS nguonDat, ISNULL(dp.soLuongPhong, 0) AS soPhong, "
                + "ISNULL(dp.soNguoi, 0) AS soNguoi "
                + "FROM DatPhong dp LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE dp.ngayDat >= ? AND dp.ngayDat < ? "
                + "ORDER BY dp.ngayDat DESC, dp.maDatPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new BookingDetail(
                            rs.getInt("maDatPhong"),
                            toLocalDate(rs.getDate("ngayDat")),
                            toLocalDate(rs.getDate("ngayNhanPhong")),
                            toLocalDate(rs.getDate("ngayTraPhong")),
                            safeTrim(rs.getString("khachHang")),
                            safeTrim(rs.getString("trangThai")),
                            safeTrim(rs.getString("nguonDat")),
                            rs.getInt("soPhong"),
                            rs.getInt("soNguoi")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<CountStat> getRoomStatusStats() {
        clearLastError();
        List<CountStat> result = new ArrayList<CountStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "Phong")) {
            return result;
        }

        String sql = "SELECT ISNULL(trangThai, N'Kh\u00e1c') AS label, COUNT(1) AS soLuong "
                + "FROM Phong GROUP BY ISNULL(trangThai, N'Kh\u00e1c') ORDER BY soLuong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new CountStat(safeLabel(rs.getString("label")), rs.getInt("soLuong")));
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<RoomTypeStat> getRoomTypeStats() {
        return getRoomTypeStats(null, null);
    }

    public List<RoomTypeStat> getRoomTypeStats(LocalDate from, LocalDate to) {
        clearLastError();
        List<RoomTypeStat> result = new ArrayList<RoomTypeStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "Phong") || !hasTable(con, "LoaiPhong")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        String sql = "SELECT lp.tenLoaiPhong, COUNT(p.maPhong) AS tongPhong, "
                + "SUM(CASE WHEN ISNULL(p.trangThai, N'') IN (?, ?, ?, ?) THEN 1 ELSE 0 END) AS baoTri, "
                + "SUM(CASE WHEN ISNULL(p.trangThai, N'') = ? THEN 1 ELSE 0 END) AS dangO, "
                + "SUM(CASE WHEN ISNULL(p.trangThai, N'') IN (?, ?) THEN 1 ELSE 0 END) AS daDat, "
                + "ISNULL(usageStats.soPhongSuDung, 0) AS soPhongSuDung "
                + "FROM LoaiPhong lp "
                + "LEFT JOIN Phong p ON p.maLoaiPhong = lp.maLoaiPhong "
                + "LEFT JOIN ("
                + "    SELECT p2.maLoaiPhong, COUNT(DISTINCT p2.maPhong) AS soPhongSuDung "
                + "    FROM LuuTru lt JOIN Phong p2 ON p2.maPhong = lt.maPhong "
                + "    WHERE lt.checkIn < ? AND ISNULL(lt.checkOut, GETDATE()) >= ? "
                + "    GROUP BY p2.maLoaiPhong"
                + ") usageStats ON usageStats.maLoaiPhong = lp.maLoaiPhong "
                + "GROUP BY lp.tenLoaiPhong, usageStats.soPhongSuDung "
                + "ORDER BY lp.tenLoaiPhong";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setString(index++, ROOM_MAINTENANCE);
            stmt.setString(index++, ROOM_INACTIVE);
            stmt.setString(index++, ROOM_STOPPED);
            stmt.setString(index++, ROOM_REPAIRING);
            stmt.setString(index++, ROOM_OCCUPIED);
            stmt.setString(index++, ROOM_BOOKED);
            stmt.setString(index++, ROOM_PENDING_CHECKIN);
            stmt.setTimestamp(index++, Timestamp.valueOf(range.endExclusive));
            stmt.setTimestamp(index++, Timestamp.valueOf(range.startInclusive));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int total = rs.getInt("tongPhong");
                    int maintenance = rs.getInt("baoTri");
                    int occupied = rs.getInt("dangO");
                    int booked = rs.getInt("daDat");
                    int active = Math.max(0, total - maintenance);
                    int usedInPeriod = rs.getInt("soPhongSuDung");
                    int occupancy = active <= 0 ? 0 : (int) Math.round((double) usedInPeriod * 100d / (double) active);
                    if (usedInPeriod <= 0) {
                        occupancy = active <= 0 ? 0 : (int) Math.round((double) (occupied + booked) * 100d / (double) active);
                    }
                    result.add(new RoomTypeStat(
                            safeLabel(rs.getString("tenLoaiPhong")),
                            active,
                            occupied,
                            booked,
                            maintenance,
                            clampPercent(occupancy)
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<RoomDetail> getRoomDetails() {
        clearLastError();
        List<RoomDetail> result = new ArrayList<RoomDetail>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "Phong")) {
            return result;
        }

        String sql = "SELECT p.maPhong, p.soPhong, ISNULL(lp.tenLoaiPhong, N'') AS loaiPhong, "
                + "ISNULL(p.tang, N'') AS tang, ISNULL(p.trangThai, N'') AS trangThai "
                + "FROM Phong p LEFT JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong "
                + "ORDER BY TRY_CAST(p.soPhong AS INT), p.soPhong";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new RoomDetail(
                        rs.getInt("maPhong"),
                        safeTrim(rs.getString("soPhong")),
                        safeTrim(rs.getString("loaiPhong")),
                        safeTrim(rs.getString("tang")),
                        safeTrim(rs.getString("trangThai"))
                ));
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public ServiceSummary getServiceSummary(LocalDate from, LocalDate to) {
        clearLastError();
        ServiceSummary summary = new ServiceSummary();
        List<ServiceUsageStat> usageStats = getServiceUsageStats(from, to);
        for (ServiceUsageStat stat : usageStats) {
            summary.totalUsage += stat.usageCount;
            summary.totalQuantity += stat.quantity;
            summary.totalRevenue += stat.revenue;
            summary.activeServiceCount++;
            if (summary.topByUsage == null || stat.usageCount > summary.topByUsage.usageCount) {
                summary.topByUsage = stat;
            }
            if (summary.topByRevenue == null || stat.revenue > summary.topByRevenue.revenue) {
                summary.topByRevenue = stat;
            }
        }
        return summary;
    }

    public List<ServiceUsageStat> getServiceUsageStats(LocalDate from, LocalDate to) {
        clearLastError();
        List<ServiceUsageStat> result = new ArrayList<ServiceUsageStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "SuDungDichVu") || !hasTable(con, "DichVu")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        String dateExpr = getServiceDateExpression(con);
        String sql = "SELECT dv.tenDichVu, COUNT(1) AS luotSuDung, "
                + "ISNULL(SUM(ISNULL(sdv.soLuong, 0)), 0) AS soLuong, "
                + "ISNULL(SUM(ISNULL(sdv.thanhTien, ISNULL(sdv.soLuong, 0) * ISNULL(sdv.donGia, 0))), 0) AS doanhThu "
                + "FROM SuDungDichVu sdv "
                + "JOIN DichVu dv ON dv.maDichVu = sdv.maDichVu "
                + "LEFT JOIN LuuTru lt ON lt.maLuuTru = sdv.maLuuTru "
                + "WHERE " + dateExpr + " >= ? AND " + dateExpr + " < ? "
                + "GROUP BY dv.tenDichVu ORDER BY luotSuDung DESC, doanhThu DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ServiceUsageStat(
                            safeLabel(rs.getString("tenDichVu")),
                            rs.getInt("luotSuDung"),
                            rs.getInt("soLuong"),
                            rs.getDouble("doanhThu")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<ServiceRevenueDateStat> getServiceRevenueByDate(LocalDate from, LocalDate to) {
        clearLastError();
        List<ServiceRevenueDateStat> result = new ArrayList<ServiceRevenueDateStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "SuDungDichVu")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        String dateExpr = getServiceDateExpression(con);
        String sql = "SELECT CAST(" + dateExpr + " AS DATE) AS ngay, COUNT(1) AS luotSuDung, "
                + "ISNULL(SUM(ISNULL(sdv.thanhTien, ISNULL(sdv.soLuong, 0) * ISNULL(sdv.donGia, 0))), 0) AS doanhThu "
                + "FROM SuDungDichVu sdv LEFT JOIN LuuTru lt ON lt.maLuuTru = sdv.maLuuTru "
                + "WHERE " + dateExpr + " >= ? AND " + dateExpr + " < ? "
                + "GROUP BY CAST(" + dateExpr + " AS DATE) ORDER BY CAST(" + dateExpr + " AS DATE)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ServiceRevenueDateStat(
                            toLocalDate(rs.getDate("ngay")),
                            rs.getInt("luotSuDung"),
                            rs.getDouble("doanhThu")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<ServiceUsageDetail> getServiceUsageDetails(LocalDate from, LocalDate to) {
        clearLastError();
        List<ServiceUsageDetail> result = new ArrayList<ServiceUsageDetail>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "SuDungDichVu")) {
            return result;
        }

        DateRange range = DateRange.of(from, to);
        String dateExpr = getServiceDateExpression(con);
        String sql = "SELECT sdv.maSuDung, " + dateExpr + " AS ngaySuDung, "
                + "ISNULL(dv.tenDichVu, N'') AS tenDichVu, ISNULL(kh.hoTen, N'') AS khachHang, "
                + "ISNULL(sdv.soLuong, 0) AS soLuong, ISNULL(sdv.donGia, 0) AS donGia, "
                + "ISNULL(sdv.thanhTien, ISNULL(sdv.soLuong, 0) * ISNULL(sdv.donGia, 0)) AS thanhTien "
                + "FROM SuDungDichVu sdv "
                + "LEFT JOIN DichVu dv ON dv.maDichVu = sdv.maDichVu "
                + "LEFT JOIN LuuTru lt ON lt.maLuuTru = sdv.maLuuTru "
                + "LEFT JOIN DatPhong dp ON dp.maDatPhong = lt.maDatPhong "
                + "LEFT JOIN KhachHang kh ON kh.maKhachHang = dp.maKhachHang "
                + "WHERE " + dateExpr + " >= ? AND " + dateExpr + " < ? "
                + "ORDER BY " + dateExpr + " DESC, sdv.maSuDung DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ServiceUsageDetail(
                            rs.getInt("maSuDung"),
                            toLocalDateTime(rs.getTimestamp("ngaySuDung")),
                            safeTrim(rs.getString("tenDichVu")),
                            safeTrim(rs.getString("khachHang")),
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia"),
                            rs.getDouble("thanhTien")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public CustomerSummary getCustomerSummary(LocalDate from, LocalDate to) {
        clearLastError();
        CustomerSummary summary = new CustomerSummary();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "KhachHang")) {
            return summary;
        }
        DateRange range = DateRange.of(from, to);

        String sql = "SELECT COUNT(DISTINCT kh.maKhachHang) AS tongKhach, "
                + "SUM(CASE WHEN ISNULL(kh.hangKhach, N'') = N'VIP' THEN 1 ELSE 0 END) AS khachVip, "
                + "SUM(CASE WHEN ISNULL(kh.quocTich, N'Vi\u1ec7t Nam') NOT IN (N'Vi\u1ec7t Nam', N'Vietnam') THEN 1 ELSE 0 END) AS khachNuocNgoai "
                + "FROM KhachHang kh "
                + "WHERE EXISTS (SELECT 1 FROM DatPhong dp WHERE dp.maKhachHang = kh.maKhachHang AND dp.ngayDat >= ? AND dp.ngayDat < ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.totalCustomers = rs.getInt("tongKhach");
                    summary.vipCustomers = rs.getInt("khachVip");
                    summary.foreignCustomers = rs.getInt("khachNuocNgoai");
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        summary.newCustomers = getNewCustomerCount(con, range);
        return summary;
    }

    public List<CustomerStat> getTopCustomers(LocalDate from, LocalDate to) {
        clearLastError();
        List<CustomerStat> result = new ArrayList<CustomerStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "KhachHang")) {
            return result;
        }
        DateRange range = DateRange.of(from, to);

        String paymentFilter = hasTable(con, "ThanhToan") ? buildPaymentFilterForSubQuery(con, "tt") : "";
        String sql = "SELECT kh.maKhachHang, kh.hoTen, ISNULL(kh.hangKhach, N'') AS hangKhach, "
                + "ISNULL(kh.quocTich, N'') AS quocTich, COUNT(DISTINCT dp.maDatPhong) AS soBooking, "
                + "ISNULL(SUM(pay.daThu), 0) AS doanhThu "
                + "FROM KhachHang kh "
                + "LEFT JOIN DatPhong dp ON dp.maKhachHang = kh.maKhachHang AND dp.ngayDat >= ? AND dp.ngayDat < ? "
                + "LEFT JOIN HoaDon hd ON hd.maDatPhong = dp.maDatPhong "
                + "LEFT JOIN (SELECT tt.maHoaDon, SUM(ISNULL(tt.soTien, 0)) AS daThu FROM ThanhToan tt WHERE 1 = 1 "
                + paymentFilter + " GROUP BY tt.maHoaDon) pay ON pay.maHoaDon = hd.maHoaDon "
                + "GROUP BY kh.maKhachHang, kh.hoTen, kh.hangKhach, kh.quocTich "
                + "HAVING COUNT(DISTINCT dp.maDatPhong) > 0 OR ISNULL(SUM(pay.daThu), 0) > 0 "
                + "ORDER BY soBooking DESC, doanhThu DESC, kh.hoTen";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = bindRange(stmt, range, 1);
            bindPaymentSubQueryParams(con, stmt, index);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new CustomerStat(
                            rs.getInt("maKhachHang"),
                            safeTrim(rs.getString("hoTen")),
                            resolveCustomerGroup(rs.getString("hangKhach"), rs.getString("quocTich")),
                            rs.getInt("soBooking"),
                            rs.getDouble("doanhThu")
                    ));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    public List<CountStat> getCustomerCategoryStats(LocalDate from, LocalDate to) {
        clearLastError();
        LinkedHashMap<String, CountStat> stats = new LinkedHashMap<String, CountStat>();
        List<CustomerStat> customers = getTopCustomers(from, to);
        for (CustomerStat customer : customers) {
            CountStat stat = stats.get(customer.group);
            if (stat == null) {
                stat = new CountStat(customer.group, 0);
                stats.put(customer.group, stat);
            }
            stat.count++;
        }
        return new ArrayList<CountStat>(stats.values());
    }

    public OverviewSummary getOverviewSummary(LocalDate from, LocalDate to) {
        RevenueSummary revenue = getRevenueSummary(from, to);
        BookingSummary booking = getBookingSummary(from, to);
        List<RoomTypeStat> rooms = getRoomTypeStats(from, to);
        ServiceSummary services = getServiceSummary(from, to);
        CustomerSummary customers = getCustomerSummary(from, to);

        int activeRooms = 0;
        int usedRooms = 0;
        for (RoomTypeStat room : rooms) {
            activeRooms += room.activeRooms;
            usedRooms += room.occupiedRooms + room.bookedRooms;
        }
        int occupancy = activeRooms <= 0 ? 0 : clampPercent((int) Math.round((double) usedRooms * 100d / (double) activeRooms));
        String topServiceName = services.topByUsage == null ? "" : services.topByUsage.serviceName;

        return new OverviewSummary(
                revenue.paidRevenue,
                revenue.invoiceRevenue,
                booking.totalBookings,
                occupancy,
                customers.totalCustomers,
                topServiceName,
                services.totalRevenue
        );
    }

    private void loadInvoiceSummary(Connection con, DateRange range, RevenueSummary summary) {
        String sql = "SELECT COUNT(1) AS soHoaDon, "
                + "ISNULL(SUM(ISNULL(tienPhong, 0)), 0) AS tienPhong, "
                + "ISNULL(SUM(ISNULL(tienDichVu, 0)), 0) AS tienDichVu, "
                + "ISNULL(SUM(" + invoiceAmountExpression("HoaDon") + "), 0) AS tongHoaDon, "
                + "ISNULL(SUM(CASE WHEN ISNULL(trangThai, ?) = ? THEN " + invoiceAmountExpression("HoaDon") + " ELSE 0 END), 0) AS choThanhToan, "
                + "SUM(CASE WHEN ISNULL(trangThai, N'') = ? THEN 1 ELSE 0 END) AS soHoaDonDaThanhToan "
                + "FROM HoaDon WHERE ngayLap >= ? AND ngayLap < ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            int index = 1;
            stmt.setString(index++, INVOICE_PENDING);
            stmt.setString(index++, INVOICE_PENDING);
            stmt.setString(index++, INVOICE_PAID);
            index = bindRange(stmt, range, index);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.invoiceCount = rs.getInt("soHoaDon");
                    summary.roomRevenue = rs.getDouble("tienPhong");
                    summary.serviceRevenue = rs.getDouble("tienDichVu");
                    summary.invoiceRevenue = rs.getDouble("tongHoaDon");
                    summary.pendingInvoiceRevenue = rs.getDouble("choThanhToan");
                    summary.paidInvoiceCount = rs.getInt("soHoaDonDaThanhToan");
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
    }

    private void loadPaymentSummary(Connection con, DateRange range, RevenueSummary summary) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) AS soGiaoDich, COUNT(DISTINCT maHoaDon) AS soHoaDonDaThu, ")
                .append("ISNULL(SUM(ISNULL(soTien, 0)), 0) AS thucThu ")
                .append("FROM ThanhToan WHERE ngayThanhToan >= ? AND ngayThanhToan < ? ");
        appendCompletedPaymentFilter(con, sql, "ThanhToan");
        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int index = bindRange(stmt, range, 1);
            bindCompletedPaymentFilter(con, stmt, index);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.paymentCount = rs.getInt("soGiaoDich");
                    summary.paidInvoiceCount = Math.max(summary.paidInvoiceCount, rs.getInt("soHoaDonDaThu"));
                    summary.paidRevenue = rs.getDouble("thucThu");
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
    }

    private List<CountStat> queryBookingGroupStats(LocalDate from, LocalDate to, String groupExpression) {
        clearLastError();
        List<CountStat> result = new ArrayList<CountStat>();
        Connection con = getReadyConnection();
        if (con == null || !hasTable(con, "DatPhong")) {
            return result;
        }
        DateRange range = DateRange.of(from, to);
        String sql = "SELECT " + groupExpression + " AS label, COUNT(1) AS soLuong "
                + "FROM DatPhong WHERE ngayDat >= ? AND ngayDat < ? "
                + "GROUP BY " + groupExpression + " ORDER BY soLuong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new CountStat(safeLabel(rs.getString("label")), rs.getInt("soLuong")));
                }
            }
        } catch (Exception ex) {
            setLastError(ex);
        }
        return result;
    }

    private int getWalkInBookingCount(Connection con, DateRange range) {
        if (!hasColumn(con, "DatPhong", "nguonDatPhong")) {
            return 0;
        }
        String sql = "SELECT COUNT(1) FROM DatPhong "
                + "WHERE ngayDat >= ? AND ngayDat < ? AND LOWER(ISNULL(nguonDatPhong, N'')) LIKE N'%walk%'";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            setLastError(ex);
            return 0;
        }
    }

    private int getNewCustomerCount(Connection con, DateRange range) {
        if (!hasTable(con, "DatPhong")) {
            return 0;
        }
        String sql = "SELECT COUNT(1) FROM ("
                + "SELECT maKhachHang, MIN(ngayDat) AS ngayDauTien FROM DatPhong GROUP BY maKhachHang"
                + ") firstBooking WHERE ngayDauTien >= ? AND ngayDauTien < ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            bindRange(stmt, range, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (Exception ex) {
            setLastError(ex);
            return 0;
        }
    }

    private String getServiceDateExpression(Connection con) {
        if (hasColumn(con, "SuDungDichVu", "ngaySuDung")) {
            return "sdv.ngaySuDung";
        }
        return "COALESCE(lt.checkOut, lt.checkIn)";
    }

    private String invoiceAmountExpression(String alias) {
        String prefix = alias == null || alias.trim().isEmpty() ? "" : alias.trim() + ".";
        return "(ISNULL(" + prefix + "tienPhong, 0) + ISNULL(" + prefix + "tienDichVu, 0) "
                + "+ ISNULL(" + prefix + "phuThu, 0) - ISNULL(" + prefix + "giamGia, 0))";
    }

    private void appendCompletedPaymentFilter(Connection con, StringBuilder sql, String tableAlias) {
        String prefix = tableAlias == null || tableAlias.trim().isEmpty() ? "" : tableAlias.trim() + ".";
        if (hasColumn(con, "ThanhToan", "trangThai")) {
            sql.append("AND ISNULL(").append(prefix).append("trangThai, ?) = ? ");
        }
        if (hasColumn(con, "ThanhToan", "loaiGiaoDich")) {
            sql.append("AND ISNULL(").append(prefix).append("loaiGiaoDich, ?) = ? ");
        }
    }

    private int bindCompletedPaymentFilter(Connection con, PreparedStatement stmt, int index) throws SQLException {
        if (hasColumn(con, "ThanhToan", "trangThai")) {
            stmt.setString(index++, PAYMENT_DONE);
            stmt.setString(index++, PAYMENT_DONE);
        }
        if (hasColumn(con, "ThanhToan", "loaiGiaoDich")) {
            stmt.setString(index++, PAYMENT_TYPE_DEFAULT);
            stmt.setString(index++, PAYMENT_TYPE_DEFAULT);
        }
        return index;
    }

    private String buildPaymentFilterForSubQuery(Connection con, String alias) {
        StringBuilder sql = new StringBuilder();
        appendCompletedPaymentFilter(con, sql, alias);
        return sql.toString();
    }

    private int bindPaymentSubQueryParams(Connection con, PreparedStatement stmt, int index) throws SQLException {
        return bindCompletedPaymentFilter(con, stmt, index);
    }

    private int bindRange(PreparedStatement stmt, DateRange range, int startIndex) throws SQLException {
        int index = startIndex;
        stmt.setTimestamp(index++, Timestamp.valueOf(range.startInclusive));
        stmt.setTimestamp(index++, Timestamp.valueOf(range.endExclusive));
        return index;
    }

    private RevenueDateStat getRevenueDateStat(Map<LocalDate, RevenueDateStat> values, LocalDate date) {
        RevenueDateStat stat = values.get(date);
        if (stat == null) {
            stat = new RevenueDateStat(date);
            values.put(date, stat);
        }
        return stat;
    }

    private Connection getReadyConnection() {
        return ConnectDB.getConnection();
    }

    private boolean hasTable(Connection con, String tableName) {
        String sql = "SELECT COUNT(1) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            return false;
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

    private Integer parseIntOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private String safeLabel(String value) {
        String result = safeTrim(value);
        return result.isEmpty() ? UNKNOWN : result;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return safeTrim(value).toLowerCase();
    }

    private boolean isConfirmedStatus(String status) {
        return status.equals(normalize(BOOKING_BOOKED))
                || status.equals(normalize(BOOKING_CONFIRMED))
                || status.equals(normalize(BOOKING_DEPOSITED))
                || status.equals(normalize(BOOKING_PAID));
    }

    private boolean isPendingCheckinStatus(String status) {
        return status.equals(normalize(BOOKING_PENDING_CHECKIN))
                || status.equals(normalize(BOOKING_ACTIVE))
                || status.equals(normalize(BOOKING_CHECKED_IN))
                || status.equals(normalize(BOOKING_STAYING))
                || status.equals(normalize(BOOKING_PARTIAL_CHECKOUT));
    }

    private boolean isCancelledStatus(String status) {
        return status.equals(normalize(BOOKING_CANCELLED))
                || status.equals(normalize(BOOKING_CANCELLED_ALT));
    }

    private String resolveCustomerGroup(String rank, String nationality) {
        if ("VIP".equalsIgnoreCase(safeTrim(rank))) {
            return "VIP";
        }
        String country = safeTrim(nationality);
        if (!country.isEmpty() && !"Vi\u1ec7t Nam".equalsIgnoreCase(country) && !"Vietnam".equalsIgnoreCase(country)) {
            return "N\u01b0\u1edbc ngo\u00e0i";
        }
        return "Th\u01b0\u1eddng";
    }

    private int clampPercent(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 100);
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(Exception ex) {
        if (ex != null && ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
            lastErrorMessage = ex.getMessage().trim();
        }
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    private void setLastError(String message) {
        if (message != null && !message.trim().isEmpty()) {
            lastErrorMessage = message.trim();
        }
    }

    private static final class DateRange {
        private final LocalDateTime startInclusive;
        private final LocalDateTime endExclusive;

        private DateRange(LocalDateTime startInclusive, LocalDateTime endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }

        private static DateRange of(LocalDate from, LocalDate to) {
            LocalDate start = from == null ? LocalDate.now().withDayOfMonth(1) : from;
            LocalDate end = to == null ? LocalDate.now() : to;
            return new DateRange(start.atStartOfDay(), end.plusDays(1L).atStartOfDay());
        }
    }

    public static final class RevenueSummary {
        private double paidRevenue;
        private double invoiceRevenue;
        private double roomRevenue;
        private double serviceRevenue;
        private double pendingInvoiceRevenue;
        private int invoiceCount;
        private int paidInvoiceCount;
        private int paymentCount;

        public double getPaidRevenue() {
            return paidRevenue;
        }

        public double getInvoiceRevenue() {
            return invoiceRevenue;
        }

        public double getRoomRevenue() {
            return roomRevenue;
        }

        public double getServiceRevenue() {
            return serviceRevenue;
        }

        public double getPendingInvoiceRevenue() {
            return pendingInvoiceRevenue;
        }

        public int getInvoiceCount() {
            return invoiceCount;
        }

        public int getPaidInvoiceCount() {
            return paidInvoiceCount;
        }

        public int getPaymentCount() {
            return paymentCount;
        }
    }

    public static final class RevenueDateStat {
        private final LocalDate date;
        private double roomRevenue;
        private double serviceRevenue;
        private double invoiceRevenue;
        private double paidRevenue;
        private int invoiceCount;
        private int paidInvoiceCount;
        private int paymentCount;

        private RevenueDateStat(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }

        public double getRoomRevenue() {
            return roomRevenue;
        }

        public double getServiceRevenue() {
            return serviceRevenue;
        }

        public double getInvoiceRevenue() {
            return invoiceRevenue;
        }

        public double getPaidRevenue() {
            return paidRevenue;
        }

        public int getInvoiceCount() {
            return invoiceCount;
        }

        public int getPaidInvoiceCount() {
            return paidInvoiceCount;
        }

        public int getPaymentCount() {
            return paymentCount;
        }
    }

    public static final class RevenueDetail {
        private final int invoiceId;
        private final LocalDateTime invoiceDate;
        private final String customerName;
        private final String invoiceStatus;
        private final String paymentMethod;
        private final double roomRevenue;
        private final double serviceRevenue;
        private final double invoiceRevenue;
        private final double paidRevenue;

        public RevenueDetail(int invoiceId, LocalDateTime invoiceDate, String customerName, String invoiceStatus,
                             String paymentMethod, double roomRevenue, double serviceRevenue,
                             double invoiceRevenue, double paidRevenue) {
            this.invoiceId = invoiceId;
            this.invoiceDate = invoiceDate;
            this.customerName = customerName;
            this.invoiceStatus = invoiceStatus;
            this.paymentMethod = paymentMethod;
            this.roomRevenue = roomRevenue;
            this.serviceRevenue = serviceRevenue;
            this.invoiceRevenue = invoiceRevenue;
            this.paidRevenue = paidRevenue;
        }

        public int getInvoiceId() {
            return invoiceId;
        }

        public LocalDateTime getInvoiceDate() {
            return invoiceDate;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getInvoiceStatus() {
            return invoiceStatus;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public double getRoomRevenue() {
            return roomRevenue;
        }

        public double getServiceRevenue() {
            return serviceRevenue;
        }

        public double getInvoiceRevenue() {
            return invoiceRevenue;
        }

        public double getPaidRevenue() {
            return paidRevenue;
        }
    }

    public static final class AmountStat {
        private final String label;
        private final int count;
        private final double amount;

        public AmountStat(String label, int count, double amount) {
            this.label = label;
            this.count = count;
            this.amount = amount;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static final class BookingSummary {
        private int totalBookings;
        private int confirmedBookings;
        private int pendingCheckinBookings;
        private int cancelledBookings;
        private int walkInBookings;
        private int bookedRooms;
        private int guests;

        public int getTotalBookings() {
            return totalBookings;
        }

        public int getConfirmedBookings() {
            return confirmedBookings;
        }

        public int getPendingCheckinBookings() {
            return pendingCheckinBookings;
        }

        public int getCancelledBookings() {
            return cancelledBookings;
        }

        public int getWalkInBookings() {
            return walkInBookings;
        }

        public int getBookedRooms() {
            return bookedRooms;
        }

        public int getGuests() {
            return guests;
        }
    }

    public static final class BookingDateStat {
        private final LocalDate date;
        private final int totalBookings;
        private final int confirmedBookings;
        private final int pendingCheckinBookings;
        private final int cancelledBookings;
        private final int walkInBookings;

        public BookingDateStat(LocalDate date, int totalBookings, int confirmedBookings,
                               int pendingCheckinBookings, int cancelledBookings, int walkInBookings) {
            this.date = date;
            this.totalBookings = totalBookings;
            this.confirmedBookings = confirmedBookings;
            this.pendingCheckinBookings = pendingCheckinBookings;
            this.cancelledBookings = cancelledBookings;
            this.walkInBookings = walkInBookings;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getTotalBookings() {
            return totalBookings;
        }

        public int getConfirmedBookings() {
            return confirmedBookings;
        }

        public int getPendingCheckinBookings() {
            return pendingCheckinBookings;
        }

        public int getCancelledBookings() {
            return cancelledBookings;
        }

        public int getWalkInBookings() {
            return walkInBookings;
        }
    }

    public static final class BookingDetail {
        private final int bookingId;
        private final LocalDate bookingDate;
        private final LocalDate checkInDate;
        private final LocalDate checkOutDate;
        private final String customerName;
        private final String status;
        private final String source;
        private final int roomCount;
        private final int guestCount;

        public BookingDetail(int bookingId, LocalDate bookingDate, LocalDate checkInDate, LocalDate checkOutDate,
                             String customerName, String status, String source, int roomCount, int guestCount) {
            this.bookingId = bookingId;
            this.bookingDate = bookingDate;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.customerName = customerName;
            this.status = status;
            this.source = source;
            this.roomCount = roomCount;
            this.guestCount = guestCount;
        }

        public int getBookingId() {
            return bookingId;
        }

        public LocalDate getBookingDate() {
            return bookingDate;
        }

        public LocalDate getCheckInDate() {
            return checkInDate;
        }

        public LocalDate getCheckOutDate() {
            return checkOutDate;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getStatus() {
            return status;
        }

        public String getSource() {
            return source;
        }

        public int getRoomCount() {
            return roomCount;
        }

        public int getGuestCount() {
            return guestCount;
        }
    }

    public static final class CountStat {
        private final String label;
        private int count;

        public CountStat(String label, int count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }
    }

    public static final class RoomTypeStat {
        private final String roomType;
        private final int activeRooms;
        private final int occupiedRooms;
        private final int bookedRooms;
        private final int maintenanceRooms;
        private final int occupancyPercent;

        public RoomTypeStat(String roomType, int activeRooms, int occupiedRooms, int bookedRooms,
                            int maintenanceRooms, int occupancyPercent) {
            this.roomType = roomType;
            this.activeRooms = activeRooms;
            this.occupiedRooms = occupiedRooms;
            this.bookedRooms = bookedRooms;
            this.maintenanceRooms = maintenanceRooms;
            this.occupancyPercent = occupancyPercent;
        }

        public String getRoomType() {
            return roomType;
        }

        public int getActiveRooms() {
            return activeRooms;
        }

        public int getOccupiedRooms() {
            return occupiedRooms;
        }

        public int getBookedRooms() {
            return bookedRooms;
        }

        public int getMaintenanceRooms() {
            return maintenanceRooms;
        }

        public int getOccupancyPercent() {
            return occupancyPercent;
        }
    }

    public static final class RoomDetail {
        private final int roomId;
        private final String roomNumber;
        private final String roomType;
        private final String floor;
        private final String status;

        public RoomDetail(int roomId, String roomNumber, String roomType, String floor, String status) {
            this.roomId = roomId;
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.floor = floor;
            this.status = status;
        }

        public int getRoomId() {
            return roomId;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public String getRoomType() {
            return roomType;
        }

        public String getFloor() {
            return floor;
        }

        public String getStatus() {
            return status;
        }
    }

    public static final class ServiceSummary {
        private int totalUsage;
        private int totalQuantity;
        private double totalRevenue;
        private int activeServiceCount;
        private ServiceUsageStat topByUsage;
        private ServiceUsageStat topByRevenue;

        public int getTotalUsage() {
            return totalUsage;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public int getActiveServiceCount() {
            return activeServiceCount;
        }

        public ServiceUsageStat getTopByUsage() {
            return topByUsage;
        }

        public ServiceUsageStat getTopByRevenue() {
            return topByRevenue;
        }
    }

    public static final class ServiceUsageStat {
        private final String serviceName;
        private final int usageCount;
        private final int quantity;
        private final double revenue;

        public ServiceUsageStat(String serviceName, int usageCount, int quantity, double revenue) {
            this.serviceName = serviceName;
            this.usageCount = usageCount;
            this.quantity = quantity;
            this.revenue = revenue;
        }

        public String getServiceName() {
            return serviceName;
        }

        public int getUsageCount() {
            return usageCount;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static final class ServiceRevenueDateStat {
        private final LocalDate date;
        private final int usageCount;
        private final double revenue;

        public ServiceRevenueDateStat(LocalDate date, int usageCount, double revenue) {
            this.date = date;
            this.usageCount = usageCount;
            this.revenue = revenue;
        }

        public LocalDate getDate() {
            return date;
        }

        public int getUsageCount() {
            return usageCount;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static final class ServiceUsageDetail {
        private final int usageId;
        private final LocalDateTime usageDate;
        private final String serviceName;
        private final String customerName;
        private final int quantity;
        private final double unitPrice;
        private final double totalAmount;

        public ServiceUsageDetail(int usageId, LocalDateTime usageDate, String serviceName, String customerName,
                                  int quantity, double unitPrice, double totalAmount) {
            this.usageId = usageId;
            this.usageDate = usageDate;
            this.serviceName = serviceName;
            this.customerName = customerName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalAmount = totalAmount;
        }

        public int getUsageId() {
            return usageId;
        }

        public LocalDateTime getUsageDate() {
            return usageDate;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getCustomerName() {
            return customerName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }

    public static final class CustomerSummary {
        private int totalCustomers;
        private int newCustomers;
        private int vipCustomers;
        private int foreignCustomers;

        public int getTotalCustomers() {
            return totalCustomers;
        }

        public int getNewCustomers() {
            return newCustomers;
        }

        public int getVipCustomers() {
            return vipCustomers;
        }

        public int getForeignCustomers() {
            return foreignCustomers;
        }
    }

    public static final class CustomerStat {
        private final int customerId;
        private final String customerName;
        private final String group;
        private final int bookingCount;
        private final double revenue;

        public CustomerStat(int customerId, String customerName, String group, int bookingCount, double revenue) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.group = group;
            this.bookingCount = bookingCount;
            this.revenue = revenue;
        }

        public int getCustomerId() {
            return customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getGroup() {
            return group;
        }

        public int getBookingCount() {
            return bookingCount;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    public static final class OverviewSummary {
        private final double paidRevenue;
        private final double invoiceRevenue;
        private final int totalBookings;
        private final int occupancyPercent;
        private final int totalCustomers;
        private final String topServiceName;
        private final double serviceRevenue;

        public OverviewSummary(double paidRevenue, double invoiceRevenue, int totalBookings, int occupancyPercent,
                               int totalCustomers, String topServiceName, double serviceRevenue) {
            this.paidRevenue = paidRevenue;
            this.invoiceRevenue = invoiceRevenue;
            this.totalBookings = totalBookings;
            this.occupancyPercent = occupancyPercent;
            this.totalCustomers = totalCustomers;
            this.topServiceName = topServiceName;
            this.serviceRevenue = serviceRevenue;
        }

        public double getPaidRevenue() {
            return paidRevenue;
        }

        public double getInvoiceRevenue() {
            return invoiceRevenue;
        }

        public int getTotalBookings() {
            return totalBookings;
        }

        public int getOccupancyPercent() {
            return occupancyPercent;
        }

        public int getTotalCustomers() {
            return totalCustomers;
        }

        public String getTopServiceName() {
            return topServiceName;
        }

        public double getServiceRevenue() {
            return serviceRevenue;
        }
    }
}
