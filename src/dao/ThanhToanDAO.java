package dao;

import db.ConnectDB;
import entity.ChiTietBangGia;
import entity.NgayLe;
import entity.ThanhToan;
import entity.ThanhToan.ChiTietDong;
import entity.ThanhToan.GiaoDichThanhToan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThanhToanDAO {
    private String lastErrorMessage = "";
    private static boolean schemaEnsured = false;
    private static boolean synchronizingInvoices = false;
    private static final String INCOMPLETE_CHECKOUT_MESSAGE = "Đơn này vẫn còn phòng chưa check-out, chưa thể thanh toán toàn bộ.";
    private static final String DAY_TYPE_NORMAL = "THUONG";
    private static final String DAY_TYPE_WEEKEND = "CUOI_TUAN";
    private static final String DAY_TYPE_HOLIDAY = "NGAY_LE";
    private static final String STAY_TYPE_HOURLY = "THEO_GIO";
    private static final String STAY_TYPE_DAILY = "THEO_NGAY";
    private static final String STAY_TYPE_OVERNIGHT = "QUA_DEM";
    private static final LocalTime LEGACY_EXPECTED_CHECKOUT_TIME = LocalTime.of(12, 0);
    private final BangGiaDAO bangGiaDAO = new BangGiaDAO();
    private final NgayLeDAO ngayLeDAO = new NgayLeDAO();

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<ThanhToan> getAll() {
        clearLastError();
        List<ThanhToan> result = new ArrayList<ThanhToan>();
        Connection con = getReadyConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);
            if (useScopedInvoiceQuery()) {
                String sql = buildInvoiceHeaderQuery(buildInvoiceVisibleClause("hd")) + " ORDER BY hd.maHoaDon DESC";
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ThanhToan invoice = mapHeader(rs);
                        loadInvoiceLines(con, invoice);
                        loadPaymentSummary(con, invoice);
                        result.add(invoice);
                    }
                }
                return result;
            }
            String readyForPaymentClause = buildBookingReadyForPaymentClause("hd.maDatPhong");

            String sql = "WITH ranked AS (" +
                    "SELECT hd.maHoaDon, hd.maLuuTru, hd.maDatPhong, hd.maKhachHang, hd.ngayLap, hd.ngayThanhToan, " +
                    "hd.tienPhong, hd.tienDichVu, ISNULL(hd.phuThu,0) AS phuThu, ISNULL(hd.giamGia,0) AS giamGia, " +
                    "ISNULL(hd.tienCocTru,0) AS tienCocTru, ISNULL(hd.trangThai,N'Chờ thanh toán') AS trangThai, " +
                    "ISNULL(hd.ghiChu,N'') AS ghiChu, dp.trangThai AS trangThaiDatPhong, " +
                    "ROW_NUMBER() OVER (PARTITION BY hd.maDatPhong ORDER BY hd.maHoaDon DESC) AS rn " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                    "WHERE " + readyForPaymentClause +
                    ") " +
                    "SELECT ranked.maHoaDon, ranked.maLuuTru, ranked.maDatPhong, ranked.maKhachHang, ranked.ngayLap, ranked.ngayThanhToan, " +
                    "ranked.tienPhong, ranked.tienDichVu, ranked.phuThu, ranked.giamGia, ranked.tienCocTru, ranked.trangThai, ranked.ghiChu, " +
                    "kh.hoTen, kh.soDienThoai, kh.email, kh.cccdPassport, " +
                    "COALESCE(stayBounds.checkIn, CAST(dp.ngayNhanPhong AS DATETIME)) AS ngayNhanPhong, " +
                    "COALESCE(stayBounds.checkOut, CAST(dp.ngayTraPhong AS DATETIME)) AS ngayTraPhong, " +
                    "ISNULL(roomSummary.soPhong, N'-') AS soPhong, ISNULL(dp.tienCoc, 0) AS tienCocGoc " +
                    "FROM ranked " +
                    "LEFT JOIN DatPhong dp ON ranked.maDatPhong = dp.maDatPhong " +
                    "LEFT JOIN KhachHang kh ON ranked.maKhachHang = kh.maKhachHang " +
                    "OUTER APPLY (" +
                    "   SELECT STUFF((" +
                    "       SELECT N', ' + p2.soPhong " +
                    "       FROM ChiTietDatPhong c2 " +
                    "       JOIN Phong p2 ON c2.maPhong = p2.maPhong " +
                    "       WHERE c2.maDatPhong = ranked.maDatPhong " +
                    "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong " +
                    "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong" +
                    ") roomSummary " +
                    "OUTER APPLY (" +
                    "   SELECT MIN(lt.checkIn) AS checkIn, MAX(lt.checkOut) AS checkOut " +
                    "   FROM LuuTru lt WHERE lt.maDatPhong = ranked.maDatPhong" +
                    ") stayBounds " +
                    "WHERE ranked.rn = 1 " +
                    "ORDER BY ranked.maHoaDon DESC";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThanhToan invoice = mapHeader(rs);
                    loadInvoiceLines(con, invoice);
                    loadPaymentSummary(con, invoice);
                    result.add(invoice);
                }
            }
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public ThanhToan findById(String maHoaDon) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (con == null || invoiceId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã hóa đơn không hợp lệ.");
            return null;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);
            if (useScopedInvoiceQuery()) {
                String sql = buildInvoiceHeaderQuery("hd.maHoaDon = ?");
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, invoiceId.intValue());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            ThanhToan invoice = mapHeader(rs);
                            loadInvoiceLines(con, invoice);
                            loadPaymentSummary(con, invoice);
                            return invoice;
                        }
                    }
                }
                return null;
            }

            String sql = "SELECT hd.maHoaDon, hd.maLuuTru, hd.maDatPhong, hd.maKhachHang, hd.ngayLap, hd.ngayThanhToan, " +
                    "hd.tienPhong, hd.tienDichVu, ISNULL(hd.phuThu,0) AS phuThu, ISNULL(hd.giamGia,0) AS giamGia, " +
                    "ISNULL(hd.tienCocTru,0) AS tienCocTru, ISNULL(hd.trangThai,N'Chờ thanh toán') AS trangThai, " +
                    "ISNULL(hd.ghiChu,N'') AS ghiChu, kh.hoTen, kh.soDienThoai, kh.email, kh.cccdPassport, " +
                    "COALESCE(stayBounds.checkIn, CAST(dp.ngayNhanPhong AS DATETIME)) AS ngayNhanPhong, " +
                    "COALESCE(stayBounds.checkOut, CAST(dp.ngayTraPhong AS DATETIME)) AS ngayTraPhong, " +
                    "ISNULL(roomSummary.soPhong, N'-') AS soPhong, ISNULL(dp.tienCoc, 0) AS tienCocGoc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                    "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                    "OUTER APPLY (" +
                    "   SELECT STUFF((" +
                    "       SELECT N', ' + p2.soPhong " +
                    "       FROM ChiTietDatPhong c2 " +
                    "       JOIN Phong p2 ON c2.maPhong = p2.maPhong " +
                    "       WHERE c2.maDatPhong = hd.maDatPhong " +
                    "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong " +
                    "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong" +
                    ") roomSummary " +
                    "OUTER APPLY (" +
                    "   SELECT MIN(lt.checkIn) AS checkIn, MAX(lt.checkOut) AS checkOut " +
                    "   FROM LuuTru lt WHERE lt.maDatPhong = hd.maDatPhong" +
                    ") stayBounds " +
                    "WHERE hd.maHoaDon = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, invoiceId.intValue());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ThanhToan invoice = mapHeader(rs);
                        loadInvoiceLines(con, invoice);
                        loadPaymentSummary(con, invoice);
                        return invoice;
                    }
                }
            }
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(ThanhToan thanhToan) {
        clearLastError();
        Connection con = getReadyConnection();
        if (con == null || thanhToan == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu thanh toán không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            List<PaymentPart> parts = new ArrayList<PaymentPart>();
            PaymentPart part = new PaymentPart();
            part.setPhuongThuc(isBlank(thanhToan.getPhuongThuc()) ? "Tiền mặt" : thanhToan.getPhuongThuc());
            part.setSoTien(thanhToan.getSoTienDaThanhToan() > 0 ? thanhToan.getSoTienDaThanhToan() : thanhToan.getConPhaiThu());
            part.setSoThamChieu(thanhToan.getSoThamChieu());
            parts.add(part);
            int maNhanVien = parseIntOrZero(thanhToan.getMaNhanVien());
            return recordPayment(String.valueOf(parseIntOrZero(thanhToan.getMaHoaDon())), maNhanVien, parts, thanhToan.getGhiChu());
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ThanhToan thanhToan) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = thanhToan == null ? null : parseIntOrNull(thanhToan.getMaHoaDon());
        if (con == null || thanhToan == null || invoiceId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Hóa đơn không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);
            String sql = "UPDATE HoaDon SET phuThu = ?, giamGia = ?, tienCocTru = ?, ghiChu = ?, trangThai = ? WHERE maHoaDon = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDouble(1, thanhToan.getPhuThu());
                ps.setDouble(2, thanhToan.getGiamGia());
                ps.setDouble(3, thanhToan.getTienCocTru());
                ps.setString(4, safeTrim(thanhToan.getGhiChu()));
                ps.setString(5, safeTrim(thanhToan.getTrangThai()));
                ps.setInt(6, invoiceId.intValue());
                boolean ok = ps.executeUpdate() > 0;
                if (ok) {
                    rebuildInvoiceLines(con, invoiceId.intValue(), Integer.parseInt(thanhToan.getMaLuuTru()));
                }
                return ok;
            }
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String maThanhToan) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer paymentId = parseIntOrNull(maThanhToan);
        if (con == null || paymentId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã thanh toán không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM ThanhToan WHERE maThanhToan = ?")) {
                ps.setInt(1, paymentId.intValue());
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean applyDiscount(String maHoaDon, double value, boolean percentage, String lyDo, String nguoiDuyet, String ghiChu) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (con == null || invoiceId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã hóa đơn không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);
            ThanhToan invoice = findById(maHoaDon);
            if (invoice == null) {
                setLastError("Không tìm thấy hóa đơn.");
                return false;
            }
            if (!validateInvoiceReadyForPayment(con, invoice)) {
                return false;
            }

            double baseAmount = invoice.getTienPhong() + invoice.getTienDichVu() + invoice.getPhuThu();
            double discountAmount = percentage ? baseAmount * (value / 100.0) : value;
            discountAmount = Math.max(0d, Math.min(discountAmount, baseAmount));

            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE HoaDon SET giamGia = ?, ghiChu = ? WHERE maHoaDon = ?")) {
                ps.setDouble(1, discountAmount);
                ps.setString(2, buildNote("Giảm giá: " + safeTrim(lyDo), safeTrim(ghiChu)));
                ps.setInt(3, invoiceId.intValue());
                ps.executeUpdate();
            }

            rebuildInvoiceLines(con, invoiceId.intValue(), parseIntOrZero(invoice.getMaDatPhong()));
            refreshInvoiceStatus(con, invoiceId.intValue());
            con.commit();
            syncCustomerAfterInvoicePaid(con, invoiceId.intValue());
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

    public boolean recordPayment(String maHoaDon, int maNhanVien, List<PaymentPart> parts, String ghiChu) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (con == null || invoiceId == null || parts == null || parts.isEmpty()) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu thanh toán không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);

            ThanhToan invoice = findById(maHoaDon);
            if (invoice == null) {
                setLastError("Không tìm thấy hóa đơn.");
                return false;
            }
            if (!validateInvoiceReadyForPayment(con, invoice)) {
                return false;
            }

            double total = 0d;
            for (PaymentPart part : parts) {
                total += Math.max(0d, part.getSoTien());
            }
            if (total <= 0d) {
                setLastError("Số tiền thanh toán phải lớn hơn 0.");
                return false;
            }
            if (total - invoice.getConPhaiThu() > 0.1d) {
                setLastError("Tổng tiền thanh toán vượt quá số còn phải thu.");
                return false;
            }

            con.setAutoCommit(false);
            String sql = "INSERT INTO ThanhToan(maHoaDon, maNhanVien, ngayThanhToan, soTien, phuongThuc, soThamChieu, ghiChu, loaiGiaoDich) " +
                    "VALUES (?, ?, GETDATE(), ?, ?, ?, ?, N'THANH_TOAN')";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (PaymentPart part : parts) {
                    double soTien = Math.max(0d, part.getSoTien());
                    if (soTien <= 0d) {
                        continue;
                    }
                    ps.setInt(1, invoiceId.intValue());
                    ps.setInt(2, maNhanVien);
                    ps.setDouble(3, soTien);
                    ps.setString(4, safeTrim(part.getPhuongThuc()));
                    ps.setString(5, safeTrim(part.getSoThamChieu()));
                    ps.setString(6, safeTrim(ghiChu));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            refreshInvoiceStatus(con, invoiceId.intValue());
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

    public boolean refundDeposit(String maHoaDon, int maNhanVien, double amount, String phuongThuc, String soThamChieu, String lyDo) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (con == null || invoiceId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã hóa đơn không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);

            ThanhToan invoice = findById(maHoaDon);
            if (invoice == null) {
                setLastError("Không tìm thấy hóa đơn.");
                return false;
            }
            if (!validateInvoiceReadyForPayment(con, invoice)) {
                return false;
            }
            double maxRefund = invoice.getSoTienCoTheHoanCoc();
            if (amount <= 0d || amount - maxRefund > 0.1d) {
                setLastError("Số tiền hoàn cọc không hợp lệ.");
                return false;
            }

            con.setAutoCommit(false);
            String sql = "INSERT INTO ThanhToan(maHoaDon, maNhanVien, ngayThanhToan, soTien, phuongThuc, soThamChieu, ghiChu, loaiGiaoDich) " +
                    "VALUES (?, ?, GETDATE(), ?, ?, ?, ?, N'HOAN_COC')";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, invoiceId.intValue());
                ps.setInt(2, maNhanVien);
                ps.setDouble(3, amount);
                ps.setString(4, safeTrim(phuongThuc));
                ps.setString(5, safeTrim(soThamChieu));
                ps.setString(6, safeTrim(lyDo));
                ps.executeUpdate();
            }

            refreshInvoiceStatus(con, invoiceId.intValue());
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

    private void synchronizeInvoices(Connection con) throws Exception {
        if (synchronizingInvoices) {
            return;
        }
        synchronizingInvoices = true;
        try {
            String readyForPaymentClause = "(" + buildBookingReadyForPaymentClause("lt.maDatPhong") + ") " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon hdRoom WHERE hdRoom.maDatPhong = lt.maDatPhong AND hdRoom.maChiTietDatPhong IS NOT NULL)";
            String sql = "SELECT lt.maLuuTru, lt.maDatPhong, lt.maChiTietDatPhong, dp.maKhachHang, ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, ISNULL(dp.tienCoc, 0) AS tienCocDatPhong, lt.giaPhong, lt.checkIn, lt.checkOut, dp.ngayTraPhong AS ngayTraPhong, " +
                    "ISNULL(ct.soDemDatPhong,0) AS soDemDatPhong, " +
                    "ISNULL(ct.giaPhongDatPhong,0) AS giaPhongDatPhong, " +
                    "ISNULL(ct.thanhTienDatPhong,0) AS thanhTienDatPhong " +
                    "FROM LuuTru lt " +
                    "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                    "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
                    "LEFT JOIN BangGia bgHeader ON dp.maBangGia = bgHeader.maBangGia " +
                    "OUTER APPLY (SELECT TOP 1 bgRoom.maBangGia FROM BangGia bgRoom " +
                    "             WHERE bgRoom.maLoaiPhong = COALESCE(p.maLoaiPhong, bgHeader.maLoaiPhong) " +
                    "               AND bgRoom.trangThai = N'Đang áp dụng' " +
                    "             ORDER BY CASE WHEN bgRoom.maBangGia = dp.maBangGia THEN 0 ELSE 1 END, bgRoom.maBangGia DESC) bgResolved " +
                    "OUTER APPLY ( " +
                    "   SELECT TOP 1 " +
                    "       ISNULL(DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong),0) AS soDemDatPhong, " +
                    "       ISNULL(ctdp.giaPhong,0) AS giaPhongDatPhong, " +
                    "       ISNULL(ctdp.thanhTien,0) AS thanhTienDatPhong " +
                    "   FROM ChiTietDatPhong ctdp " +
                    "   WHERE ctdp.maChiTietDatPhong = lt.maChiTietDatPhong " +
                    ") ct " +
                    "WHERE " + readyForPaymentClause;

            Map<Integer, InvoiceAggregate> aggregates = new LinkedHashMap<Integer, InvoiceAggregate>();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int maDatPhong = rs.getInt("maDatPhong");
                    InvoiceAggregate aggregate = aggregates.get(Integer.valueOf(maDatPhong));
                    if (aggregate == null) {
                        aggregate = new InvoiceAggregate();
                        aggregate.maDatPhong = maDatPhong;
                        aggregate.maKhachHang = rs.getInt("maKhachHang");
                        aggregate.maLuuTruDaiDien = rs.getInt("maLuuTru");
                        aggregate.tienCoc = rs.getDouble("tienCocDatPhong");
                        aggregates.put(Integer.valueOf(maDatPhong), aggregate);
                    }
                    RoomChargeBreakdown roomCharge = calculateRoomCharge(
                            rs.getInt("maBangGiaResolved"),
                            rs.getDouble("giaPhong"),
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("ngayTraPhong"),
                            rs.getTimestamp("checkOut"),
                            rs.getLong("soDemDatPhong"),
                            rs.getDouble("giaPhongDatPhong"),
                            rs.getDouble("thanhTienDatPhong"));
                    aggregate.tienPhong += roomCharge.getThanhTien().doubleValue();
                    aggregate.phuThu += roomCharge.getLateCheckoutCharge().doubleValue();
                    aggregate.tienDichVu += loadServiceCharge(con, rs.getInt("maLuuTru"));
                }
            }

            for (InvoiceAggregate aggregate : aggregates.values()) {
                Integer maHoaDon = findLatestInvoiceIdByBooking(con, aggregate.maDatPhong);
                if (maHoaDon == null) {
                    maHoaDon = insertInvoiceHeader(con, aggregate.maLuuTruDaiDien, aggregate.maDatPhong, aggregate.maKhachHang,
                            aggregate.tienPhong, aggregate.tienDichVu, aggregate.phuThu, aggregate.tienCoc);
                } else {
                    try (PreparedStatement update = con.prepareStatement(
                            "UPDATE HoaDon SET maLuuTru = ?, maKhachHang = ?, tienPhong = ?, tienDichVu = ?, phuThu = ?, tienCocTru = CASE " +
                                    "WHEN tienCocTru IS NULL THEN ? " +
                                    "WHEN tienCocTru > ? THEN ? ELSE tienCocTru END " +
                                    "WHERE maHoaDon = ?")) {
                        double tongTruocDatCoc = Math.max(0d, aggregate.tienPhong + aggregate.tienDichVu + aggregate.phuThu);
                        double tienCocTru = Math.min(aggregate.tienCoc, tongTruocDatCoc);
                        update.setInt(1, aggregate.maLuuTruDaiDien);
                        update.setInt(2, aggregate.maKhachHang);
                        update.setDouble(3, aggregate.tienPhong);
                        update.setDouble(4, aggregate.tienDichVu);
                        update.setDouble(5, aggregate.phuThu);
                        update.setDouble(6, tienCocTru);
                        update.setDouble(7, aggregate.tienCoc);
                        update.setDouble(8, tienCocTru);
                        update.setInt(9, maHoaDon.intValue());
                        update.executeUpdate();
                    }
                }

                cleanupDuplicateInvoicesForBooking(con, aggregate.maDatPhong, maHoaDon.intValue());
                rebuildInvoiceLines(con, maHoaDon.intValue(), aggregate.maDatPhong);
                refreshInvoiceStatus(con, maHoaDon.intValue());
            }
            removeInvoicesForBookingsNotReady(con);
        } finally {
            synchronizingInvoices = false;
        }
    }

    private Integer insertInvoiceHeader(Connection con, int maLuuTru, int maDatPhong, int maKhachHang,
                                        double tienPhong, double tienDichVu, double phuThu, double tienCoc) throws Exception {
        if (useScopedInvoiceQuery()) {
            return insertInvoiceHeader(con, maLuuTru, maDatPhong, null, maKhachHang, "BOOKING", tienPhong, tienDichVu, phuThu, tienCoc);
        }
        String sql = "INSERT INTO HoaDon(maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu) " +
                "VALUES (?, ?, ?, GETDATE(), ?, ?, ?, 0, ?, N'Chờ thanh toán', N'')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maLuuTru);
            ps.setInt(2, maDatPhong);
            ps.setInt(3, maKhachHang);
            ps.setDouble(4, tienPhong);
            ps.setDouble(5, tienDichVu);
            ps.setDouble(6, phuThu);
            ps.setDouble(7, Math.min(tienCoc, Math.max(0d, tienPhong + tienDichVu + phuThu)));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private Integer insertInvoiceHeader(Connection con,
                                        int maLuuTru,
                                        int maDatPhong,
                                        Integer maChiTietDatPhong,
                                        int maKhachHang,
                                        String loaiHoaDon,
                                        double tienPhong,
                                        double tienDichVu,
                                        double phuThu,
                                        double tienCoc) throws Exception {
        String sql = "INSERT INTO HoaDon(maLuuTru, maDatPhong, maChiTietDatPhong, maKhachHang, loaiHoaDon, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu) " +
                "VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, 0, ?, N'Chá» thanh toÃ¡n', N'')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maLuuTru);
            ps.setInt(2, maDatPhong);
            if (maChiTietDatPhong == null || maChiTietDatPhong.intValue() <= 0) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, maChiTietDatPhong.intValue());
            }
            ps.setInt(4, maKhachHang);
            ps.setString(5, safeTrim(loaiHoaDon));
            ps.setDouble(6, tienPhong);
            ps.setDouble(7, tienDichVu);
            ps.setDouble(8, phuThu);
            ps.setDouble(9, Math.min(tienCoc, Math.max(0d, tienPhong + tienDichVu + phuThu)));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private void rebuildInvoiceLines(Connection con, int maHoaDon, int maDatPhong) throws Exception {
        if (useScopedInvoiceQuery()) {
            rebuildInvoiceLines(con, maHoaDon);
            return;
        }
        try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
            del.setInt(1, maHoaDon);
            del.executeUpdate();
        }

        ThanhToan invoice = findHeaderById(con, maHoaDon);
        if (invoice == null) {
            return;
        }
        double lateCheckoutCharge = insertRoomChargeLines(con, maHoaDon, maDatPhong, invoice);
        String serviceSql = "SELECT dv.tenDichVu, SUM(sddv.soLuong) AS soLuong, MAX(sddv.donGia) AS donGia " +
                "FROM SuDungDichVu sddv " +
                "JOIN DichVu dv ON sddv.maDichVu = dv.maDichVu " +
                "JOIN LuuTru lt ON sddv.maLuuTru = lt.maLuuTru " +
                "WHERE lt.maDatPhong = ? GROUP BY dv.tenDichVu ORDER BY dv.tenDichVu";
        try (PreparedStatement ps = con.prepareStatement(serviceSql)) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    insertInvoiceLine(
                            con,
                            maHoaDon,
                            safeTrim(rs.getString("tenDichVu")),
                            rs.getInt("soLuong"),
                            rs.getDouble("donGia")
                    );
                }
            }
        }

        double otherSurcharge = invoice.getPhuThu() - lateCheckoutCharge;
        if (otherSurcharge > 0.1d) {
            insertInvoiceLine(con, maHoaDon, "Phụ thu", 1, otherSurcharge);
        }
        if (invoice.getGiamGia() > 0d) {
            insertInvoiceLine(con, maHoaDon, "Giảm giá", 1, -invoice.getGiamGia());
        }
    }

    private void rebuildInvoiceLines(Connection con, int maHoaDon) throws Exception {
        try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
            del.setInt(1, maHoaDon);
            del.executeUpdate();
        }

        ThanhToan invoice = findHeaderById(con, maHoaDon);
        if (invoice == null) {
            return;
        }
        InvoiceScope scope = loadInvoiceScope(con, maHoaDon);
        if (scope == null) {
            return;
        }
        double lateCheckoutCharge = insertRoomChargeLines(con, maHoaDon, scope, invoice);
        String serviceSql = "SELECT dv.tenDichVu, SUM(sddv.soLuong) AS soLuong, MAX(sddv.donGia) AS donGia " +
                "FROM SuDungDichVu sddv " +
                "JOIN DichVu dv ON sddv.maDichVu = dv.maDichVu " +
                "JOIN LuuTru lt ON sddv.maLuuTru = lt.maLuuTru " +
                "WHERE " + (scope.isRoomScoped() ? "lt.maChiTietDatPhong = ?" : "lt.maDatPhong = ?") +
                " GROUP BY dv.tenDichVu ORDER BY dv.tenDichVu";
        try (PreparedStatement ps = con.prepareStatement(serviceSql)) {
            ps.setInt(1, scope.isRoomScoped() ? scope.maChiTietDatPhong : scope.maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    insertInvoiceLine(con, maHoaDon, safeTrim(rs.getString("tenDichVu")), rs.getInt("soLuong"), rs.getDouble("donGia"));
                }
            }
        }

        double otherSurcharge = invoice.getPhuThu() - lateCheckoutCharge;
        if (otherSurcharge > 0.1d) {
            insertInvoiceLine(con, maHoaDon, "Phá»¥ thu", 1, otherSurcharge);
        }
        if (invoice.getGiamGia() > 0d) {
            insertInvoiceLine(con, maHoaDon, "Giáº£m giÃ¡", 1, -invoice.getGiamGia());
        }
    }

    private double insertRoomChargeLines(Connection con, int maHoaDon, int maDatPhong, ThanhToan invoice) throws Exception {
        if (useScopedInvoiceQuery()) {
            return insertRoomChargeLines(con, maHoaDon, loadInvoiceScope(con, maHoaDon), invoice);
        }
        String roomSql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.giaPhong, lt.checkIn, lt.checkOut, dp.ngayTraPhong AS checkOutDuKien, ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, " +
                "ISNULL(p.soPhong, N'Phong') AS soPhong, " +
                "ISNULL(DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong),0) AS soDemDatPhong, " +
                "ISNULL(ctdp.giaPhong,0) AS giaPhongDatPhong, " +
                "ISNULL(ctdp.thanhTien,0) AS thanhTienDatPhong " +
                "FROM LuuTru lt " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
                "LEFT JOIN BangGia bgHeader ON dp.maBangGia = bgHeader.maBangGia " +
                "OUTER APPLY (SELECT TOP 1 bgRoom.maBangGia FROM BangGia bgRoom " +
                "             WHERE bgRoom.maLoaiPhong = COALESCE(p.maLoaiPhong, bgHeader.maLoaiPhong) " +
                "               AND bgRoom.trangThai = N'Đang áp dụng' " +
                "             ORDER BY CASE WHEN bgRoom.maBangGia = dp.maBangGia THEN 0 ELSE 1 END, bgRoom.maBangGia DESC) bgResolved " +
                "LEFT JOIN ChiTietDatPhong ctdp ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "WHERE lt.maDatPhong = ? " +
                "ORDER BY lt.maLuuTru ASC";
        try (PreparedStatement ps = con.prepareStatement(roomSql)) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                boolean insertedRoomLine = false;
                double totalLateCheckoutCharge = 0d;
                while (rs.next()) {
                    RoomChargeBreakdown roomCharge = calculateRoomCharge(
                            rs.getInt("maBangGiaResolved"),
                            rs.getDouble("giaPhong"),
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("checkOutDuKien"),
                            rs.getTimestamp("checkOut"),
                            rs.getLong("soDemDatPhong"),
                            rs.getDouble("giaPhongDatPhong"),
                            rs.getDouble("thanhTienDatPhong"));
                    if (roomCharge.getThanhTien().doubleValue() <= 0d) {
                        continue;
                    }
                    String roomLine = buildRoomInvoiceLine(
                            safeTrim(rs.getString("soPhong")),
                            roomCharge.getDurationLabel(),
                            roomCharge.getLoaiNgay());
                    insertInvoiceLine(con, maHoaDon, roomLine, 1, roomCharge.getThanhTien().doubleValue());
                    if (roomCharge.getLateCheckoutCharge().doubleValue() > 0d) {
                        insertInvoiceLine(
                                con,
                                maHoaDon,
                                buildLateCheckoutInvoiceLine(safeTrim(rs.getString("soPhong")), roomCharge.getLateCheckoutHours()),
                                1,
                                roomCharge.getLateCheckoutCharge().doubleValue()
                        );
                        totalLateCheckoutCharge += roomCharge.getLateCheckoutCharge().doubleValue();
                    }
                    insertedRoomLine = true;
                }
                if (!insertedRoomLine && invoice.getTienPhong() > 0d) {
                    insertInvoiceLine(con, maHoaDon, "Tiền phòng", 1, invoice.getTienPhong());
                }
                return totalLateCheckoutCharge;
            }
        }
    }

    private double insertRoomChargeLines(Connection con, int maHoaDon, InvoiceScope scope, ThanhToan invoice) throws Exception {
        if (scope == null) {
            return 0d;
        }
        String roomSql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.giaPhong, lt.checkIn, lt.checkOut, dp.ngayTraPhong AS checkOutDuKien, ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, " +
                "ISNULL(p.soPhong, N'Phong') AS soPhong, " +
                "ISNULL(DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong),0) AS soDemDatPhong, " +
                "ISNULL(ctdp.giaPhong,0) AS giaPhongDatPhong, " +
                "ISNULL(ctdp.thanhTien,0) AS thanhTienDatPhong " +
                "FROM LuuTru lt " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
                "LEFT JOIN BangGia bgHeader ON dp.maBangGia = bgHeader.maBangGia " +
                "OUTER APPLY (SELECT TOP 1 bgRoom.maBangGia FROM BangGia bgRoom " +
                "             WHERE bgRoom.maLoaiPhong = COALESCE(p.maLoaiPhong, bgHeader.maLoaiPhong) " +
                "               AND bgRoom.trangThai = N'Äang Ã¡p dá»¥ng' " +
                "             ORDER BY CASE WHEN bgRoom.maBangGia = dp.maBangGia THEN 0 ELSE 1 END, bgRoom.maBangGia DESC) bgResolved " +
                "LEFT JOIN ChiTietDatPhong ctdp ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "WHERE " + (scope.isRoomScoped() ? "lt.maChiTietDatPhong = ? " : "lt.maDatPhong = ? ") +
                "ORDER BY lt.maLuuTru ASC";
        try (PreparedStatement ps = con.prepareStatement(roomSql)) {
            ps.setInt(1, scope.isRoomScoped() ? scope.maChiTietDatPhong : scope.maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                boolean insertedRoomLine = false;
                double totalLateCheckoutCharge = 0d;
                while (rs.next()) {
                    RoomChargeBreakdown roomCharge = calculateRoomCharge(
                            rs.getInt("maBangGiaResolved"),
                            rs.getDouble("giaPhong"),
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("checkOutDuKien"),
                            rs.getTimestamp("checkOut"),
                            rs.getLong("soDemDatPhong"),
                            rs.getDouble("giaPhongDatPhong"),
                            rs.getDouble("thanhTienDatPhong"));
                    if (roomCharge.getThanhTien().doubleValue() <= 0d) {
                        continue;
                    }
                    String roomLine = buildRoomInvoiceLine(
                            safeTrim(rs.getString("soPhong")),
                            roomCharge.getDurationLabel(),
                            roomCharge.getLoaiNgay());
                    insertInvoiceLine(con, maHoaDon, roomLine, 1, roomCharge.getThanhTien().doubleValue());
                    if (roomCharge.getLateCheckoutCharge().doubleValue() > 0d) {
                        insertInvoiceLine(
                                con,
                                maHoaDon,
                                buildLateCheckoutInvoiceLine(safeTrim(rs.getString("soPhong")), roomCharge.getLateCheckoutHours()),
                                1,
                                roomCharge.getLateCheckoutCharge().doubleValue()
                        );
                        totalLateCheckoutCharge += roomCharge.getLateCheckoutCharge().doubleValue();
                    }
                    insertedRoomLine = true;
                }
                if (!insertedRoomLine && invoice.getTienPhong() > 0d) {
                    insertInvoiceLine(con, maHoaDon, "Tiá»n phÃ²ng", 1, invoice.getTienPhong());
                }
                return totalLateCheckoutCharge;
            }
        }
    }

    private void insertInvoiceLine(Connection con, int maHoaDon, String loaiChiPhi, int soLuong, double donGia) throws Exception {
        String normalizedLabel = normalizeInvoiceLineLabel(loaiChiPhi);
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ChiTietHoaDon(maHoaDon, loaiChiPhi, soLuong, donGia) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, maHoaDon);
            ps.setString(2, normalizedLabel);
            ps.setInt(3, soLuong);
            ps.setDouble(4, donGia);
            ps.executeUpdate();
        }
    }

    private String buildRoomInvoiceLine(String soPhong, String durationLabel, String loaiNgay) {
        StringBuilder builder = new StringBuilder("Tiền phòng");
        if (!isBlank(soPhong)) {
            builder.append(" - P").append(soPhong);
        }
        if (!isBlank(durationLabel)) {
            builder.append(" - ").append(durationLabel);
        }
        if (!isBlank(loaiNgay)) {
            builder.append(" - ").append(loaiNgay);
        }
        return builder.toString();
    }

    private String buildLateCheckoutInvoiceLine(String soPhong, long lateHours) {
        StringBuilder builder = new StringBuilder("Phụ thu trả phòng trễ");
        if (!isBlank(soPhong)) {
            builder.append(" - P").append(soPhong);
        }
        builder.append(" - ").append(Math.max(0L, lateHours)).append(" giờ");
        return builder.toString();
    }

    private String normalizeInvoiceLineLabel(String value) {
        String normalized = safeTrim(value);
        if (normalized.isEmpty()) {
            normalized = "Chi phí";
        }
        final int maxLength = 120;
        if (normalized.length() > maxLength) {
            normalized = normalized.substring(0, maxLength - 1).trim() + "…";
        }
        return normalized;
    }

    private void loadInvoiceLines(Connection con, ThanhToan invoice) throws Exception {
        invoice.getChiTiet().clear();
        String sql = "SELECT maChiTietHoaDon, maHoaDon, loaiChiPhi, soLuong, donGia, thanhTien " +
                "FROM ChiTietHoaDon WHERE maHoaDon = ? ORDER BY maChiTietHoaDon ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parseIntOrZero(invoice.getMaHoaDon()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietDong line = new ChiTietDong();
                    line.setMaChiTietHoaDon(String.valueOf(rs.getInt("maChiTietHoaDon")));
                    line.setMaHoaDon(String.valueOf(rs.getInt("maHoaDon")));
                    line.setLoaiChiPhi(safeTrim(rs.getString("loaiChiPhi")));
                    line.setDienGiai(safeTrim(rs.getString("loaiChiPhi")));
                    line.setSoLuong(rs.getInt("soLuong"));
                    line.setDonGia(rs.getDouble("donGia"));
                    line.setThanhTien(rs.getDouble("thanhTien"));
                    invoice.getChiTiet().add(line);
                }
            }
        }
    }

    private void loadPaymentSummary(Connection con, ThanhToan invoice) throws Exception {
        invoice.getGiaoDichThanhToans().clear();
        String sql = "SELECT tt.maThanhToan, tt.maNhanVien, tt.ngayThanhToan, tt.soTien, ISNULL(tt.phuongThuc,N'Tiền mặt') AS phuongThuc, " +
                "ISNULL(tt.soThamChieu,N'') AS soThamChieu, ISNULL(tt.ghiChu,N'') AS ghiChu, ISNULL(tt.loaiGiaoDich,N'THANH_TOAN') AS loaiGiaoDich, " +
                "ISNULL(nv.hoTen, N'') AS nguoiThu " +
                "FROM ThanhToan tt LEFT JOIN NhanVien nv ON tt.maNhanVien = nv.maNhanVien WHERE tt.maHoaDon = ? ORDER BY tt.maThanhToan ASC";
        double paid = 0d;
        double refunded = 0d;
        String singleMethod = "";
        boolean mixedMethod = false;
        Timestamp latestPaidAt = null;
        String latestCollector = "";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parseIntOrZero(invoice.getMaHoaDon()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GiaoDichThanhToan gd = new GiaoDichThanhToan();
                    gd.setMaThanhToan(String.valueOf(rs.getInt("maThanhToan")));
                    gd.setLoaiGiaoDich(safeTrim(rs.getString("loaiGiaoDich")));
                    gd.setPhuongThuc(safeTrim(rs.getString("phuongThuc")));
                    gd.setSoThamChieu(safeTrim(rs.getString("soThamChieu")));
                    gd.setNgayThanhToan(rs.getTimestamp("ngayThanhToan"));
                    gd.setSoTien(rs.getDouble("soTien"));
                    gd.setGhiChu(safeTrim(rs.getString("ghiChu")));
                    invoice.getGiaoDichThanhToans().add(gd);

                    if ("HOAN_COC".equalsIgnoreCase(gd.getLoaiGiaoDich())) {
                        refunded += gd.getSoTien();
                    } else {
                        paid += gd.getSoTien();
                        if (latestPaidAt == null || (gd.getNgayThanhToan() != null && gd.getNgayThanhToan().after(latestPaidAt))) {
                            latestPaidAt = gd.getNgayThanhToan();
                            latestCollector = safeTrim(rs.getString("nguoiThu"));
                        }
                        if (isBlank(singleMethod)) {
                            singleMethod = gd.getPhuongThuc();
                        } else if (!singleMethod.equalsIgnoreCase(gd.getPhuongThuc())) {
                            mixedMethod = true;
                        }
                    }
                }
            }
        }

        invoice.setSoTienDaThanhToan(paid);
        invoice.setTienCocDaHoan(refunded);
        invoice.setNgayThanhToan(latestPaidAt);
        invoice.setNguoiThu(latestCollector);
        invoice.setPhuongThuc(mixedMethod ? "Kết hợp" : singleMethod);
        if (mixedMethod) {
            invoice.setThongTinThanhToanKetHop(buildMixedPaymentInfo(invoice));
        } else {
            invoice.setThongTinThanhToanKetHop("");
        }
    }

    private void refreshInvoiceStatus(Connection con, int maHoaDon) throws Exception {
        ThanhToan invoice = findHeaderById(con, maHoaDon);
        if (invoice == null) {
            return;
        }

        loadPaymentSummary(con, invoice);

        String status;
        if (invoice.getTienCocDaHoan() > 0d) {
            status = "Đã hoàn cọc";
        } else if (invoice.getSoTienDaThanhToan() > 0.1d && invoice.getConPhaiThu() <= 0.1d) {
            status = "Đã thanh toán";
        } else {
            status = "Chờ thanh toán";
        }

        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE HoaDon SET trangThai = ?, ngayThanhToan = CASE WHEN ? = N'Chờ thanh toán' THEN NULL ELSE GETDATE() END WHERE maHoaDon = ?")) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, maHoaDon);
            ps.executeUpdate();
        }
        if ("Đã thanh toán".equalsIgnoreCase(status)) {
            syncBookingAndRoomsAfterPayment(con, maHoaDon);
            syncCustomerAfterInvoicePaid(con, maHoaDon);
        }
    }

    private ThanhToan findHeaderById(Connection con, int maHoaDon) throws Exception {
        if (useScopedInvoiceQuery()) {
            String sql = buildInvoiceHeaderQuery("hd.maHoaDon = ?");
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapHeader(rs);
                    }
                }
            }
            return null;
        }
        String sql = "SELECT hd.maHoaDon, hd.maLuuTru, hd.maDatPhong, hd.maKhachHang, hd.ngayLap, hd.ngayThanhToan, " +
                "hd.tienPhong, hd.tienDichVu, ISNULL(hd.phuThu,0) AS phuThu, ISNULL(hd.giamGia,0) AS giamGia, " +
                "ISNULL(hd.tienCocTru,0) AS tienCocTru, ISNULL(hd.trangThai,N'Chờ thanh toán') AS trangThai, " +
                "ISNULL(hd.ghiChu,N'') AS ghiChu, kh.hoTen, kh.soDienThoai, kh.email, kh.cccdPassport, " +
                "COALESCE(stayBounds.checkIn, CAST(dp.ngayNhanPhong AS DATETIME)) AS ngayNhanPhong, " +
                "COALESCE(stayBounds.checkOut, CAST(dp.ngayTraPhong AS DATETIME)) AS ngayTraPhong, " +
                "ISNULL(roomSummary.soPhong, N'-') AS soPhong, ISNULL(dp.tienCoc, 0) AS tienCocGoc " +
                "FROM HoaDon hd " +
                "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                "OUTER APPLY (" +
                "   SELECT STUFF((" +
                "       SELECT N', ' + p2.soPhong " +
                "       FROM ChiTietDatPhong c2 " +
                "       JOIN Phong p2 ON c2.maPhong = p2.maPhong " +
                "       WHERE c2.maDatPhong = hd.maDatPhong " +
                "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong " +
                "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong" +
                ") roomSummary " +
                "OUTER APPLY (" +
                "   SELECT MIN(lt.checkIn) AS checkIn, MAX(lt.checkOut) AS checkOut " +
                "   FROM LuuTru lt WHERE lt.maDatPhong = hd.maDatPhong" +
                ") stayBounds " +
                "WHERE hd.maHoaDon = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapHeader(rs);
                }
            }
        }
        return null;
    }

    private ThanhToan mapHeader(ResultSet rs) throws Exception {
        ThanhToan invoice = new ThanhToan();
        invoice.setMaHoaDon(String.valueOf(rs.getInt("maHoaDon")));
        invoice.setMaLuuTru(rs.getObject("maLuuTru") == null ? "" : String.valueOf(rs.getInt("maLuuTru")));
        invoice.setMaDatPhong(rs.getObject("maDatPhong") == null ? "" : String.valueOf(rs.getInt("maDatPhong")));
        invoice.setMaChiTietDatPhong(readOptionalIntAsString(rs, "maChiTietDatPhong"));
        invoice.setMaKhachHang(rs.getObject("maKhachHang") == null ? "" : String.valueOf(rs.getInt("maKhachHang")));
        invoice.setMaHoSo(isBlank(invoice.getMaDatPhong()) ? "-" : "LT-DP" + invoice.getMaDatPhong());
        invoice.setKhachHang(safeTrim(rs.getString("hoTen")));
        invoice.setSoPhong(safeTrim(rs.getString("soPhong")));
        invoice.setSoDienThoai(safeTrim(rs.getString("soDienThoai")));
        invoice.setEmail(safeTrim(rs.getString("email")));
        invoice.setCccdPassport(safeTrim(rs.getString("cccdPassport")));
        invoice.setNgayLap(rs.getTimestamp("ngayLap"));
        invoice.setNgayThanhToan(rs.getTimestamp("ngayThanhToan"));
        invoice.setNgayNhanPhong(rs.getTimestamp("ngayNhanPhong"));
        invoice.setNgayTraPhong(rs.getTimestamp("ngayTraPhong"));
        invoice.setTienPhong(rs.getDouble("tienPhong"));
        invoice.setTienDichVu(rs.getDouble("tienDichVu"));
        invoice.setPhuThu(rs.getDouble("phuThu"));
        invoice.setGiamGia(rs.getDouble("giamGia"));
        invoice.setTienCocTru(rs.getDouble("tienCocTru"));
        invoice.setTienCoc(rs.getDouble("tienCocGoc"));
        invoice.setLoaiHoaDon(readOptionalString(rs, "loaiHoaDon"));
        invoice.setTrangThai(safeTrim(rs.getString("trangThai")));
        invoice.setGhiChu(safeTrim(rs.getString("ghiChu")));
        return invoice;
    }

    private String readOptionalIntAsString(ResultSet rs, String columnName) throws SQLException {
        if (!hasColumn(rs, columnName) || rs.getObject(columnName) == null) {
            return "";
        }
        int value = rs.getInt(columnName);
        return value > 0 ? String.valueOf(value) : "";
    }

    private String readOptionalString(ResultSet rs, String columnName) throws SQLException {
        if (!hasColumn(rs, columnName)) {
            return "";
        }
        return safeTrim(rs.getString(columnName));
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private String determineLoaiNgay(LocalDate date) {
        if (date == null) {
            return "Ngay thuong";
        }
        return ngayLeDAO.isHoliday(date) ? "Ngay le" : (isWeekend(date) ? "Cuoi tuan" : "Ngay thuong");
    }

    private long calculateStayHours(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 1L;
        }
        return Math.max(1L, (long) Math.ceil(Duration.between(checkIn, checkOut).toMinutes() / 60.0d));
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

    private String resolveStayTypeKey(LocalDateTime checkIn, LocalDateTime checkOut, long stayHours) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return STAY_TYPE_DAILY;
        }
        if (stayHours >= 24L) {
            return STAY_TYPE_DAILY;
        }
        if (checkOut.toLocalDate().isAfter(checkIn.toLocalDate()) && stayHours <= 24L) {
            return STAY_TYPE_OVERNIGHT;
        }
        if (!checkOut.toLocalDate().isAfter(checkIn.toLocalDate())) {
            return STAY_TYPE_HOURLY;
        }
        return STAY_TYPE_DAILY;
    }

    private BigDecimal resolveBaseRate(ChiTietBangGia detail, String stayType) {
        if (detail == null) {
            return BigDecimal.ZERO;
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return toMoney(detail.getGiaTheoGio());
        }
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return toMoney(detail.getGiaQuaDem());
        }
        return toMoney(detail.getGiaTheoNgay());
    }

    private BigDecimal resolveSurcharge(ChiTietBangGia detail, String dayType) {
        if (detail == null) {
            return BigDecimal.ZERO;
        }
        if (DAY_TYPE_HOLIDAY.equals(dayType)) {
            return toMoney(detail.getHolidaySurcharge());
        }
        if (DAY_TYPE_WEEKEND.equals(dayType)) {
            return toMoney(detail.getWeekendSurcharge());
        }
        return BigDecimal.ZERO;
    }

    private long resolvePricingUnits(String stayType, long stayHours) {
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return Math.max(1L, stayHours);
        }
        if (!STAY_TYPE_DAILY.equals(stayType)) {
            return 1L;
        }
        return Math.max(1L, (long) Math.ceil(stayHours / 24.0d));
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
            return "Ng\u00e0y l\u1ec5";
        }
        if (DAY_TYPE_WEEKEND.equals(dayType)) {
            return "Cu\u1ed1i tu\u1ea7n";
        }
        return "Ng\u00e0y th\u01b0\u1eddng";
    }

    private String toStayTypeDisplay(String stayType) {
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return "Qua \u0111\u00eam";
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return "Theo gi\u1edd";
        }
        return "Theo ng\u00e0y";
    }

    private String buildDayTypeSummary(LocalDateTime checkIn, LocalDateTime checkOut) {
        LocalDate start = checkIn == null ? null : checkIn.toLocalDate();
        LocalDate end = checkOut == null ? start : checkOut.toLocalDate();
        if (start == null) {
            return "Ngày thường";
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
        if (hasHoliday && (hasWeekend || hasNormal)) {
            return "Có ngày lễ trong khoảng lưu trú";
        }
        if (hasHoliday) {
            return "Ngày lễ";
        }
        if (hasWeekend && hasNormal) {
            return "Có cuối tuần trong khoảng lưu trú";
        }
        if (hasWeekend) {
            return "Cuối tuần";
        }
        return "Ngày thường";
    }

    private String buildDurationLabel(String stayType, long stayHours) {
        if (STAY_TYPE_OVERNIGHT.equals(stayType)) {
            return "1 đêm";
        }
        if (STAY_TYPE_HOURLY.equals(stayType)) {
            return Math.max(1L, stayHours) + " giờ";
        }
        long soNgay = Math.max(1L, (long) Math.ceil(stayHours / 24.0d));
        return soNgay + " ngày";
    }

    private Timestamp normalizeExpectedCheckout(Timestamp value) {
        if (value == null) {
            return null;
        }
        LocalDateTime dateTime = value.toLocalDateTime();
        if (dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)) {
            return Timestamp.valueOf(LocalDateTime.of(dateTime.toLocalDate(), LEGACY_EXPECTED_CHECKOUT_TIME));
        }
        return value;
    }

    private long calculateRoundedLateHours(LocalDateTime expectedCheckOut, LocalDateTime actualCheckOut) {
        if (expectedCheckOut == null || actualCheckOut == null || !actualCheckOut.isAfter(expectedCheckOut)) {
            return 0L;
        }
        return Math.max(1L, (long) Math.ceil(Duration.between(expectedCheckOut, actualCheckOut).toMinutes() / 60.0d));
    }

    private BigDecimal resolveAppliedRate(ChiTietBangGia detail, String loaiNgay) {
        if (detail == null) {
            return BigDecimal.ZERO;
        }
        if ("Ngay le".equalsIgnoreCase(loaiNgay) && detail.getHolidaySurcharge() > 0d) {
            return toMoney(detail.getGiaTheoNgay()).add(toMoney(detail.getHolidaySurcharge()));
        }
        if ("Cuoi tuan".equalsIgnoreCase(loaiNgay) && detail.getWeekendSurcharge() > 0d) {
            return toMoney(detail.getGiaTheoNgay()).add(toMoney(detail.getWeekendSurcharge()));
        }
        return toMoney(detail.getGiaTheoNgay());
    }

    private RoomChargeBreakdown calculateRoomCharge(int maBangGia,
                                                    double giaPhongLuuTru,
                                                    Timestamp checkIn,
                                                    Timestamp expectedCheckOut,
                                                    Timestamp checkOut,
                                                    long soDemDatPhong,
                                                    double giaPhongDatPhong,
                                                    double thanhTienDatPhong) {
        LocalDateTime start = checkIn == null ? null : checkIn.toLocalDateTime();
        LocalDateTime actualEnd = checkOut == null ? null : checkOut.toLocalDateTime();
        Timestamp normalizedExpected = normalizeExpectedCheckout(expectedCheckOut);
        LocalDateTime expectedEnd = normalizedExpected == null ? null : normalizedExpected.toLocalDateTime();
        LocalDateTime billedEnd = actualEnd;
        if (billedEnd == null) {
            billedEnd = expectedEnd;
        }
        if (expectedEnd != null && actualEnd != null && actualEnd.isAfter(expectedEnd)) {
            billedEnd = expectedEnd;
        }
        LocalDate ngayApDung = start == null ? null : start.toLocalDate();
        long soGio = calculateStayHours(start, billedEnd);
        String dayType = resolveDayTypeKey(ngayApDung);
        String stayType = resolveStayTypeKey(start, billedEnd, soGio);
        ChiTietBangGia detail = bangGiaDAO.getChiTietBangGiaDangApDung(maBangGia, ngayApDung);
        if (detail == null) {
            List<ChiTietBangGia> details = bangGiaDAO.getChiTietBangGiaByMaBangGia(maBangGia);
            if (!details.isEmpty()) {
                detail = details.get(0);
            }
        }

        RoomChargeBreakdown breakdown = new RoomChargeBreakdown();
        breakdown.setLoaiNgay(buildDayTypeSummary(start, billedEnd));
        breakdown.setSoGioLuuTru(soGio);
        breakdown.setLoaiGiaApDung(toStayTypeDisplay(stayType));
        breakdown.setDurationLabel(buildDurationLabel(stayType, soGio));

        BigDecimal tienPhong = BigDecimal.ZERO;
        if (detail != null) {
            BigDecimal baseRate = resolveBaseRate(detail, stayType);
            BigDecimal surcharge = resolveSurcharge(detail, dayType);
            long pricingUnits = resolvePricingUnits(stayType, soGio);
            if (baseRate.signum() > 0 || surcharge.signum() > 0) {
                tienPhong = baseRate.add(surcharge).multiply(BigDecimal.valueOf(pricingUnits));
            }
        }

        if (tienPhong.signum() <= 0 && thanhTienDatPhong > 0d) {
            breakdown.setLoaiGiaApDung(toStayTypeDisplay(STAY_TYPE_DAILY));
            breakdown.setDurationLabel(buildDurationLabel(STAY_TYPE_DAILY, soGio));
            tienPhong = toMoney(thanhTienDatPhong);
        }
        if (tienPhong.signum() <= 0 && giaPhongDatPhong > 0d) {
            long soDem = Math.max(1L, soDemDatPhong);
            breakdown.setLoaiGiaApDung(toStayTypeDisplay(STAY_TYPE_DAILY));
            breakdown.setDurationLabel(soDem + " ngày");
            tienPhong = toMoney(giaPhongDatPhong).multiply(BigDecimal.valueOf(soDem));
        }
        if (tienPhong.signum() <= 0 && giaPhongLuuTru > 0d) {
            long soNgay = Math.max(1L, (long) Math.ceil(soGio / 24.0d));
            breakdown.setLoaiGiaApDung(toStayTypeDisplay(STAY_TYPE_DAILY));
            breakdown.setDurationLabel(soNgay + " ngày");
            tienPhong = toMoney(giaPhongLuuTru).multiply(BigDecimal.valueOf(soNgay));
        }

        long lateHours = calculateRoundedLateHours(expectedEnd, actualEnd);
        BigDecimal lateCharge = BigDecimal.ZERO;
        if (lateHours > 0L && detail != null) {
            lateCharge = toMoney(detail.getGiaTheoGio()).multiply(BigDecimal.valueOf(lateHours));
        }

        breakdown.setLateCheckoutHours(lateHours);
        breakdown.setLateCheckoutCharge(lateCharge.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP));
        breakdown.setThanhTien(tienPhong.max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP));
        return breakdown;
    }

    private BigDecimal toMoney(double value) {
        if (value <= 0d) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP);
    }

    private double loadServiceCharge(Connection con, int maLuuTru) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ISNULL(SUM(thanhTien),0) FROM SuDungDichVu WHERE maLuuTru = ?")) {
            ps.setInt(1, maLuuTru);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0d;
    }

    private Integer findLatestInvoiceIdByBooking(Connection con, int maDatPhong) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT TOP 1 maHoaDon FROM HoaDon WHERE maDatPhong = ? AND maChiTietDatPhong IS NULL ORDER BY maHoaDon DESC")) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    public boolean isInvoiceReadyForPayment(String maHoaDon) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (con == null || invoiceId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã hóa đơn không hợp lệ.");
            return false;
        }
        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);
            if (useScopedInvoiceQuery()) {
                InvoiceScope scope = loadInvoiceScope(con, invoiceId.intValue());
                if (scope == null) {
                    setLastError("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n.");
                    return false;
                }
                if (scope.isRoomScoped()) {
                    if (!isRoomInvoiceReadyForPayment(con, scope)) {
                        setLastError("Phòng này chưa hoàn tất check-out nên chưa thể thanh toán riêng.");
                        return false;
                    }
                    return true;
                }
            }
            Integer maDatPhong = findBookingIdByInvoice(con, invoiceId.intValue());
            if (maDatPhong == null || maDatPhong.intValue() <= 0) {
                setLastError("Không tìm thấy hóa đơn.");
                return false;
            }
            if (!isBookingReadyForPayment(con, maDatPhong.intValue())) {
                setLastError(INCOMPLETE_CHECKOUT_MESSAGE);
                return false;
            }
            return true;
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Integer findBookingIdByInvoice(Connection con, int maHoaDon) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT TOP 1 ISNULL(maDatPhong, 0) AS maDatPhong FROM HoaDon WHERE maHoaDon = ?")) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt("maDatPhong"));
                }
            }
        }
        return null;
    }

    private void cleanupDuplicateInvoicesForBooking(Connection con, int maDatPhong, int keepMaHoaDon) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT maHoaDon FROM HoaDon WHERE maDatPhong = ? AND maChiTietDatPhong IS NULL AND maHoaDon <> ? ORDER BY maHoaDon DESC")) {
            ps.setInt(1, maDatPhong);
            ps.setInt(2, keepMaHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int duplicateId = rs.getInt(1);
                    if (hasAnyPaymentTransaction(con, duplicateId)) {
                        continue;
                    }
                    try (PreparedStatement delLines = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
                        delLines.setInt(1, duplicateId);
                        delLines.executeUpdate();
                    }
                    try (PreparedStatement delHeader = con.prepareStatement("DELETE FROM HoaDon WHERE maHoaDon = ?")) {
                        delHeader.setInt(1, duplicateId);
                        delHeader.executeUpdate();
                    }
                }
            }
        }
    }

    private void removeInvoicesForBookingsNotReady(Connection con) throws Exception {
        String sql = "SELECT hd.maHoaDon, hd.maDatPhong, ISNULL(hd.trangThai, N'') AS trangThai " +
                "FROM HoaDon hd " +
                "WHERE hd.maChiTietDatPhong IS NULL AND NOT (" + buildBookingReadyForPaymentClause("hd.maDatPhong") + ")";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int maHoaDon = rs.getInt("maHoaDon");
                int maDatPhong = rs.getInt("maDatPhong");
                String trangThai = safeTrim(rs.getString("trangThai"));
                if (maDatPhong <= 0 || "Đã thanh toán".equalsIgnoreCase(trangThai) || hasAnyPaymentTransaction(con, maHoaDon)) {
                    continue;
                }
                try (PreparedStatement delLines = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
                    delLines.setInt(1, maHoaDon);
                    delLines.executeUpdate();
                }
                try (PreparedStatement delHeader = con.prepareStatement("DELETE FROM HoaDon WHERE maHoaDon = ?")) {
                    delHeader.setInt(1, maHoaDon);
                    delHeader.executeUpdate();
                }
            }
        }
    }

    public List<Integer> prepareInvoicesForCheckout(Connection con,
                                                    int maDatPhong,
                                                    List<Integer> maLuuTruIds,
                                                    boolean fullBookingCheckout) throws Exception {
        List<Integer> invoiceIds = new ArrayList<Integer>();
        if (con == null || maDatPhong <= 0 || maLuuTruIds == null || maLuuTruIds.isEmpty()) {
            return invoiceIds;
        }
        ensureExtendedSchema(con);
        if (fullBookingCheckout && canUseBookingInvoiceForCheckout(con, maDatPhong, maLuuTruIds)) {
            synchronizeInvoices(con);
            Integer invoiceId = findLatestInvoiceIdByBooking(con, maDatPhong);
            if (invoiceId != null) {
                invoiceIds.add(invoiceId);
            }
            return invoiceIds;
        }
        for (Integer stayId : maLuuTruIds) {
            if (stayId == null || stayId.intValue() <= 0) {
                continue;
            }
            Integer invoiceId = createOrRefreshRoomInvoiceForStay(con, stayId.intValue());
            if (invoiceId != null && !invoiceIds.contains(invoiceId)) {
                invoiceIds.add(invoiceId);
            }
        }
        return invoiceIds;
    }

    private boolean canUseBookingInvoiceForCheckout(Connection con, int maDatPhong, List<Integer> maLuuTruIds) throws Exception {
        if (con == null || maDatPhong <= 0 || maLuuTruIds == null || maLuuTruIds.isEmpty()) {
            return false;
        }
        if (!isBookingReadyForPayment(con, maDatPhong)) {
            return false;
        }
        if (hasRoomScopedInvoiceForBooking(con, maDatPhong)) {
            return false;
        }
        return !hasCheckedOutStayOutsideTargets(con, maDatPhong, maLuuTruIds);
    }

    private Integer createOrRefreshRoomInvoiceForStay(Connection con, int maLuuTru) throws Exception {
        InvoiceAggregate aggregate = loadRoomInvoiceAggregate(con, maLuuTru);
        if (aggregate == null || aggregate.maChiTietDatPhong <= 0) {
            return null;
        }
        Integer maHoaDon = findRoomInvoiceIdByDetail(con, aggregate.maChiTietDatPhong);
        if (maHoaDon == null) {
            maHoaDon = insertInvoiceHeader(
                    con,
                    aggregate.maLuuTruDaiDien,
                    aggregate.maDatPhong,
                    Integer.valueOf(aggregate.maChiTietDatPhong),
                    aggregate.maKhachHang,
                    "PHONG",
                    aggregate.tienPhong,
                    aggregate.tienDichVu,
                    aggregate.phuThu,
                    aggregate.tienCoc
            );
        } else {
            try (PreparedStatement update = con.prepareStatement(
                    "UPDATE HoaDon SET maLuuTru = ?, maDatPhong = ?, maChiTietDatPhong = ?, maKhachHang = ?, loaiHoaDon = N'PHONG', " +
                            "tienPhong = ?, tienDichVu = ?, phuThu = ?, tienCocTru = CASE " +
                            "WHEN tienCocTru IS NULL THEN ? " +
                            "WHEN tienCocTru > ? THEN ? ELSE tienCocTru END WHERE maHoaDon = ?")) {
                double tongTruocDatCoc = Math.max(0d, aggregate.tienPhong + aggregate.tienDichVu + aggregate.phuThu);
                double tienCocTru = Math.min(aggregate.tienCoc, tongTruocDatCoc);
                update.setInt(1, aggregate.maLuuTruDaiDien);
                update.setInt(2, aggregate.maDatPhong);
                update.setInt(3, aggregate.maChiTietDatPhong);
                update.setInt(4, aggregate.maKhachHang);
                update.setDouble(5, aggregate.tienPhong);
                update.setDouble(6, aggregate.tienDichVu);
                update.setDouble(7, aggregate.phuThu);
                update.setDouble(8, tienCocTru);
                update.setDouble(9, aggregate.tienCoc);
                update.setDouble(10, tienCocTru);
                update.setInt(11, maHoaDon.intValue());
                update.executeUpdate();
            }
        }
        rebuildInvoiceLines(con, maHoaDon.intValue());
        refreshInvoiceStatus(con, maHoaDon.intValue());
        return maHoaDon;
    }

    private InvoiceAggregate loadRoomInvoiceAggregate(Connection con, int maLuuTru) throws Exception {
        String sql = "SELECT lt.maLuuTru, lt.maDatPhong, lt.maChiTietDatPhong, " +
                "COALESCE(roomGuest.maKhachHang, dp.maKhachHang) AS maKhachHang, " +
                "ISNULL(bgResolved.maBangGia, dp.maBangGia) AS maBangGiaResolved, ISNULL(dp.tienCoc, 0) AS tienCocDatPhong, " +
                "lt.giaPhong, lt.checkIn, lt.checkOut, dp.ngayTraPhong AS ngayTraPhong, " +
                "ISNULL(ct.soDemDatPhong,0) AS soDemDatPhong, ISNULL(ct.giaPhongDatPhong,0) AS giaPhongDatPhong, " +
                "ISNULL(ct.thanhTienDatPhong,0) AS thanhTienDatPhong, " +
                "(SELECT COUNT(1) FROM ChiTietDatPhong WHERE maDatPhong = lt.maDatPhong) AS tongChiTiet " +
                "FROM LuuTru lt " +
                "JOIN LuuTru scopeStay ON scopeStay.maLuuTru = ? AND scopeStay.maChiTietDatPhong = lt.maChiTietDatPhong " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN ChiTietDatPhongKhachDaiDien roomGuest ON roomGuest.maChiTietDatPhong = lt.maChiTietDatPhong " +
                "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
                "LEFT JOIN BangGia bgHeader ON dp.maBangGia = bgHeader.maBangGia " +
                "OUTER APPLY (SELECT TOP 1 bgRoom.maBangGia FROM BangGia bgRoom " +
                "             WHERE bgRoom.maLoaiPhong = COALESCE(p.maLoaiPhong, bgHeader.maLoaiPhong) " +
                "               AND bgRoom.trangThai = N'Äang Ã¡p dá»¥ng' " +
                "             ORDER BY CASE WHEN bgRoom.maBangGia = dp.maBangGia THEN 0 ELSE 1 END, bgRoom.maBangGia DESC) bgResolved " +
                "OUTER APPLY (SELECT TOP 1 " +
                "       ISNULL(DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong),0) AS soDemDatPhong, " +
                "       ISNULL(ctdp.giaPhong,0) AS giaPhongDatPhong, " +
                "       ISNULL(ctdp.thanhTien,0) AS thanhTienDatPhong " +
                "   FROM ChiTietDatPhong ctdp WHERE ctdp.maChiTietDatPhong = lt.maChiTietDatPhong) ct " +
                "WHERE lt.checkOut IS NOT NULL " +
                "ORDER BY lt.maLuuTru ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLuuTru);
            try (ResultSet rs = ps.executeQuery()) {
                InvoiceAggregate aggregate = null;
                while (rs.next()) {
                    RoomChargeBreakdown roomCharge = calculateRoomCharge(
                            rs.getInt("maBangGiaResolved"),
                            rs.getDouble("giaPhong"),
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("ngayTraPhong"),
                            rs.getTimestamp("checkOut"),
                            rs.getLong("soDemDatPhong"),
                            rs.getDouble("giaPhongDatPhong"),
                            rs.getDouble("thanhTienDatPhong"));
                    if (aggregate == null) {
                        aggregate = new InvoiceAggregate();
                        aggregate.maDatPhong = rs.getInt("maDatPhong");
                        aggregate.maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                        aggregate.maKhachHang = rs.getInt("maKhachHang");
                        aggregate.maLuuTruDaiDien = rs.getInt("maLuuTru");
                        int tongChiTiet = Math.max(1, rs.getInt("tongChiTiet"));
                        aggregate.tienCoc = rs.getDouble("tienCocDatPhong") / (double) tongChiTiet;
                    }
                    if (rs.getInt("maLuuTru") == maLuuTru) {
                        aggregate.maLuuTruDaiDien = rs.getInt("maLuuTru");
                    }
                    aggregate.tienPhong += roomCharge.getThanhTien().doubleValue();
                    aggregate.phuThu += roomCharge.getLateCheckoutCharge().doubleValue();
                    aggregate.tienDichVu += loadServiceCharge(con, rs.getInt("maLuuTru"));
                }
                return aggregate;
            }
        }
    }

    private Integer findRoomInvoiceIdByDetail(Connection con, int maChiTietDatPhong) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT TOP 1 maHoaDon FROM HoaDon WHERE maChiTietDatPhong = ? ORDER BY maHoaDon DESC")) {
            ps.setInt(1, maChiTietDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private boolean hasRoomScopedInvoiceForBooking(Connection con, int maDatPhong) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(1) FROM HoaDon WHERE maDatPhong = ? AND maChiTietDatPhong IS NOT NULL")) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean hasCheckedOutStayOutsideTargets(Connection con, int maDatPhong, List<Integer> maLuuTruIds) throws Exception {
        if (con == null || maDatPhong <= 0 || maLuuTruIds == null || maLuuTruIds.isEmpty()) {
            return false;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(1) FROM LuuTru WHERE maDatPhong = ? AND checkOut IS NOT NULL AND maLuuTru NOT IN (");
        for (int i = 0; i < maLuuTruIds.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, maDatPhong);
            for (int i = 0; i < maLuuTruIds.size(); i++) {
                ps.setInt(i + 2, maLuuTruIds.get(i).intValue());
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean useScopedInvoiceQuery() {
        return true;
    }

    private String buildInvoiceVisibleClause(String invoiceAlias) {
        return "((" + invoiceAlias + ".maChiTietDatPhong IS NULL AND " + buildBookingReadyForPaymentClause(invoiceAlias + ".maDatPhong") + ") " +
                "OR (" + invoiceAlias + ".maChiTietDatPhong IS NOT NULL AND EXISTS (" +
                "SELECT 1 FROM LuuTru ltDone WHERE ltDone.maChiTietDatPhong = " + invoiceAlias + ".maChiTietDatPhong AND ltDone.checkOut IS NOT NULL)))";
    }

    private String buildInvoiceHeaderQuery(String whereClause) {
        return "SELECT hd.maHoaDon, hd.maLuuTru, hd.maDatPhong, hd.maKhachHang, hd.maChiTietDatPhong, ISNULL(hd.loaiHoaDon, N'') AS loaiHoaDon, hd.ngayLap, hd.ngayThanhToan, " +
                "hd.tienPhong, hd.tienDichVu, ISNULL(hd.phuThu,0) AS phuThu, ISNULL(hd.giamGia,0) AS giamGia, " +
                "ISNULL(hd.tienCocTru,0) AS tienCocTru, ISNULL(hd.trangThai,N'Chá» thanh toÃ¡n') AS trangThai, " +
                "ISNULL(hd.ghiChu,N'') AS ghiChu, kh.hoTen, kh.soDienThoai, kh.email, kh.cccdPassport, " +
                "COALESCE(stayBounds.checkIn, CAST(dp.ngayNhanPhong AS DATETIME)) AS ngayNhanPhong, " +
                "COALESCE(stayBounds.checkOut, CAST(dp.ngayTraPhong AS DATETIME)) AS ngayTraPhong, " +
                "ISNULL(roomSummary.soPhong, N'-') AS soPhong, ISNULL(dp.tienCoc, 0) AS tienCocGoc " +
                "FROM HoaDon hd " +
                "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                "OUTER APPLY (" +
                "   SELECT STUFF((" +
                "       SELECT N', ' + p2.soPhong " +
                "       FROM ChiTietDatPhong c2 " +
                "       JOIN Phong p2 ON c2.maPhong = p2.maPhong " +
                "       WHERE (hd.maChiTietDatPhong IS NULL AND c2.maDatPhong = hd.maDatPhong) " +
                "          OR (hd.maChiTietDatPhong IS NOT NULL AND c2.maChiTietDatPhong = hd.maChiTietDatPhong) " +
                "       ORDER BY TRY_CAST(p2.soPhong AS INT), p2.soPhong " +
                "       FOR XML PATH(''), TYPE).value('.', 'NVARCHAR(MAX)'), 1, 2, N'') AS soPhong" +
                ") roomSummary " +
                "OUTER APPLY (" +
                "   SELECT MIN(lt.checkIn) AS checkIn, MAX(lt.checkOut) AS checkOut " +
                "   FROM LuuTru lt " +
                "   WHERE (hd.maChiTietDatPhong IS NULL AND lt.maDatPhong = hd.maDatPhong) " +
                "      OR (hd.maChiTietDatPhong IS NOT NULL AND lt.maChiTietDatPhong = hd.maChiTietDatPhong)" +
                ") stayBounds " +
                "WHERE " + whereClause;
    }

    private InvoiceScope loadInvoiceScope(Connection con, int maHoaDon) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ISNULL(maDatPhong, 0) AS maDatPhong, ISNULL(maChiTietDatPhong, 0) AS maChiTietDatPhong, ISNULL(loaiHoaDon, N'') AS loaiHoaDon " +
                        "FROM HoaDon WHERE maHoaDon = ?")) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InvoiceScope scope = new InvoiceScope();
                    scope.maDatPhong = rs.getInt("maDatPhong");
                    scope.maChiTietDatPhong = rs.getInt("maChiTietDatPhong");
                    scope.loaiHoaDon = safeTrim(rs.getString("loaiHoaDon"));
                    return scope;
                }
            }
        }
        return null;
    }

    private boolean isRoomInvoiceReadyForPayment(Connection con, InvoiceScope scope) throws Exception {
        if (scope == null || !scope.isRoomScoped()) {
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(1) FROM LuuTru WHERE maChiTietDatPhong = ? AND checkOut IS NOT NULL")) {
            ps.setInt(1, scope.maChiTietDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean isBookingPaidByRoomInvoices(Connection con, int maDatPhong) throws Exception {
        if (con == null || maDatPhong <= 0) {
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT CASE WHEN " +
                        "NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maDatPhong = ? AND lt.checkOut IS NULL) " +
                        "AND NOT EXISTS (" +
                        "   SELECT 1 FROM ChiTietDatPhong ctdp " +
                        "   WHERE ctdp.maDatPhong = ? " +
                        "     AND NOT EXISTS (" +
                        "         SELECT 1 FROM HoaDon hd " +
                        "         WHERE hd.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                        "           AND ISNULL(hd.trangThai, N'') = N'ÄÃ£ thanh toÃ¡n'" +
                        "     )" +
                        ") " +
                        "THEN 1 ELSE 0 END")) {
            ps.setInt(1, maDatPhong);
            ps.setInt(2, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    private boolean hasAnyPaymentTransaction(Connection con, int maHoaDon) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(1) FROM ThanhToan WHERE maHoaDon = ?")) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean markInvoiceAsPaid(String maHoaDon, int maNhanVien, String ghiChu) {
        clearLastError();
        Connection con = getReadyConnection();
        Integer invoiceId = parseIntOrNull(maHoaDon);
        if (con == null || invoiceId == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã hóa đơn không hợp lệ.");
            return false;
        }

        try {
            ensureExtendedSchema(con);
            synchronizeInvoices(con);
            boolean roomScoped = false;
            if (useScopedInvoiceQuery()) {
                InvoiceScope scope = loadInvoiceScope(con, invoiceId.intValue());
                if (scope == null) {
                    setLastError("KhÃ´ng tÃ¬m tháº¥y hÃ³a Ä‘Æ¡n.");
                    return false;
                }
                roomScoped = scope.isRoomScoped();
                if (roomScoped && !isRoomInvoiceReadyForPayment(con, scope)) {
                    setLastError("Phòng này chưa hoàn tất check-out nên chưa thể thanh toán riêng.");
                    return false;
                }
            }
            Integer maDatPhong = findBookingIdByInvoice(con, invoiceId.intValue());
            if (maDatPhong == null || maDatPhong.intValue() <= 0) {
                setLastError("Không tìm thấy hóa đơn.");
                return false;
            }
            if (!roomScoped && !isBookingReadyForPayment(con, maDatPhong.intValue())) {
                setLastError(INCOMPLETE_CHECKOUT_MESSAGE);
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE HoaDon SET trangThai = N'Đã thanh toán', ngayThanhToan = GETDATE(), ghiChu = CASE " +
                            "WHEN ISNULL(ghiChu,N'') = N'' THEN ? ELSE ghiChu + N' | ' + ? END WHERE maHoaDon = ?")) {
                String note = safeTrim(ghiChu);
                if (note.isEmpty()) {
                    note = "Xác nhận hoàn tất hóa đơn.";
                }
                ps.setString(1, note);
                ps.setString(2, note);
                ps.setInt(3, invoiceId.intValue());
                boolean ok = ps.executeUpdate() > 0;
                if (ok) {
                    syncBookingAndRoomsAfterPayment(con, invoiceId.intValue());
                    syncCustomerAfterInvoicePaid(con, invoiceId.intValue());
                }
                return ok;
            }
        } catch (Exception e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String buildMixedPaymentInfo(ThanhToan invoice) {
        Map<String, Double> totals = new LinkedHashMap<String, Double>();
        for (GiaoDichThanhToan gd : invoice.getGiaoDichThanhToans()) {
            if (!"HOAN_COC".equalsIgnoreCase(gd.getLoaiGiaoDich())) {
                double old = totals.containsKey(gd.getPhuongThuc()) ? totals.get(gd.getPhuongThuc()).doubleValue() : 0d;
                totals.put(gd.getPhuongThuc(), old + gd.getSoTien());
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            builder.append(entry.getKey()).append(": ").append(ThanhToan.formatMoney(entry.getValue().doubleValue()));
        }
        return builder.toString();
    }

    private static final class InvoiceAggregate {
        private int maDatPhong;
        private int maChiTietDatPhong;
        private int maKhachHang;
        private int maLuuTruDaiDien;
        private double tienPhong;
        private double tienDichVu;
        private double tienCoc;
        private double phuThu;
    }

    private static final class InvoiceScope {
        private int maDatPhong;
        private int maChiTietDatPhong;
        private String loaiHoaDon;

        private boolean isRoomScoped() {
            return maChiTietDatPhong > 0 || "PHONG".equalsIgnoreCase(loaiHoaDon);
        }
    }

    private static final class RoomChargeBreakdown {
        private String loaiNgay;
        private String loaiGiaApDung;
        private long soGioLuuTru;
        private String durationLabel;
        private long lateCheckoutHours;
        private BigDecimal lateCheckoutCharge = BigDecimal.ZERO;
        private BigDecimal thanhTien = BigDecimal.ZERO;

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

        public long getSoGioLuuTru() {
            return soGioLuuTru;
        }

        public void setSoGioLuuTru(long soGioLuuTru) {
            this.soGioLuuTru = soGioLuuTru;
        }

        public String getDurationLabel() {
            return durationLabel;
        }

        public void setDurationLabel(String durationLabel) {
            this.durationLabel = durationLabel;
        }

        public long getLateCheckoutHours() {
            return lateCheckoutHours;
        }

        public void setLateCheckoutHours(long lateCheckoutHours) {
            this.lateCheckoutHours = lateCheckoutHours;
        }

        public BigDecimal getLateCheckoutCharge() {
            return lateCheckoutCharge;
        }

        public void setLateCheckoutCharge(BigDecimal lateCheckoutCharge) {
            this.lateCheckoutCharge = lateCheckoutCharge == null ? BigDecimal.ZERO : lateCheckoutCharge;
        }

        public BigDecimal getThanhTien() {
            return thanhTien;
        }

        public void setThanhTien(BigDecimal thanhTien) {
            this.thanhTien = thanhTien;
        }
    }

    private void ensureExtendedSchema(Connection con) throws Exception {
        if (schemaEnsured || con == null) {
            return;
        }
        executeSql(con,
                "IF OBJECT_ID(N'dbo.ChiTietDatPhongKhachDaiDien', N'U') IS NULL " +
                        "BEGIN " +
                        "CREATE TABLE ChiTietDatPhongKhachDaiDien(" +
                        "maChiTietDatPhong INT NOT NULL PRIMARY KEY, " +
                        "maKhachHang INT NOT NULL, " +
                        "ngayTao DATETIME NOT NULL CONSTRAINT DF_ChiTietDatPhongKhachDaiDien_ngayTao DEFAULT GETDATE()" +
                        ") END");
        executeSql(con,
                "IF COL_LENGTH('HoaDon', 'maChiTietDatPhong') IS NULL " +
                        "BEGIN ALTER TABLE HoaDon ADD maChiTietDatPhong INT NULL END");
        executeSql(con,
                "IF COL_LENGTH('HoaDon', 'loaiHoaDon') IS NULL " +
                        "BEGIN ALTER TABLE HoaDon ADD loaiHoaDon NVARCHAR(30) NULL END");
        schemaEnsured = true;
    }

    private void executeSql(Connection con, String sql) throws Exception {
        try (Statement st = con.createStatement()) {
            st.execute(sql);
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

    private Integer parseIntOrNull(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Integer.valueOf(Integer.parseInt(value.trim()));
        } catch (Exception ex) {
            return null;
        }
    }

    private int parseIntOrZero(String value) {
        Integer parsed = parseIntOrNull(value);
        return parsed == null ? 0 : parsed.intValue();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String buildNote(String first, String second) {
        if (isBlank(first)) {
            return safeTrim(second);
        }
        if (isBlank(second)) {
            return safeTrim(first);
        }
        return safeTrim(first) + ". " + safeTrim(second);
    }

    private void rollbackQuietly(Connection con) {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (Exception ignored) {
        }
    }

    private void resetAutoCommit(Connection con) {
        try {
            if (con != null) {
                con.setAutoCommit(true);
            }
        } catch (Exception ignored) {
        }
    }

    private void syncCustomerAfterInvoicePaid(Connection con, int maHoaDon) {
        // KhachHang.trangThai la trang thai master data; khong doi tu dong sau thanh toan.
    }

    private void syncBookingAndRoomsAfterPayment(Connection con, int maHoaDon) {
        try {
            if (useScopedInvoiceQuery()) {
                InvoiceScope scope = loadInvoiceScope(con, maHoaDon);
                if (scope == null) {
                    return;
                }
                if (scope.isRoomScoped()) {
                    if (isBookingPaidByRoomInvoices(con, scope.maDatPhong)) {
                        try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'ÄÃ£ thanh toÃ¡n' WHERE maDatPhong = ?")) {
                            ps.setInt(1, scope.maDatPhong);
                            ps.executeUpdate();
                        }
                    }
                    synchronizeOperationalStatuses(con);
                    return;
                }
            }
            int maDatPhong = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT ISNULL(maDatPhong, 0) AS maDatPhong FROM HoaDon WHERE maHoaDon = ?")) {
                ps.setInt(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maDatPhong = rs.getInt("maDatPhong");
                    }
                }
            }
            if (maDatPhong <= 0) {
                return;
            }
            if (!isBookingReadyForPayment(con, maDatPhong)) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'Đã thanh toán' WHERE maDatPhong = ?")) {
                ps.setInt(1, maDatPhong);
                ps.executeUpdate();
            }
            synchronizeOperationalStatuses(con);
        } catch (Exception ignored) {
        }
    }

    private void synchronizeOperationalStatuses(Connection con) {
        try {
            if (con == null) {
                return;
            }
            synchronizeOperationalStatusesWithoutCleaning(con);
        } catch (Exception ignored) {
        }
    }

    private void synchronizeOperationalStatusesWithoutCleaning(Connection con) throws SQLException {
        List<Integer> roomIds = new ArrayList<Integer>();
        try (PreparedStatement ps = con.prepareStatement("SELECT maPhong FROM Phong");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomIds.add(Integer.valueOf(rs.getInt("maPhong")));
            }
        }
        new DatPhongDAO().refreshRoomStatuses(con, roomIds);
    }

    private boolean validateInvoiceReadyForPayment(Connection con, ThanhToan invoice) throws Exception {
        if (useScopedInvoiceQuery()) {
            InvoiceScope scope = loadInvoiceScope(con, parseIntOrZero(invoice == null ? null : invoice.getMaHoaDon()));
            if (scope == null) {
                setLastError("KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c hÃ³a Ä‘Æ¡n.");
                return false;
            }
            if (scope.isRoomScoped()) {
                if (!isRoomInvoiceReadyForPayment(con, scope)) {
                    setLastError("Phòng này chưa hoàn tất check-out nên chưa thể thanh toán riêng.");
                    return false;
                }
                return true;
            }
        }
        int maDatPhong = parseIntOrZero(invoice == null ? null : invoice.getMaDatPhong());
        if (maDatPhong <= 0) {
            setLastError("Không xác định được đơn đặt phòng của hóa đơn.");
            return false;
        }
        if (!isBookingReadyForPayment(con, maDatPhong)) {
            setLastError(INCOMPLETE_CHECKOUT_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isBookingReadyForPayment(Connection con, int maDatPhong) throws Exception {
        if (con == null || maDatPhong <= 0) {
            return false;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT CASE WHEN " +
                        "EXISTS (SELECT 1 FROM ChiTietDatPhong ctdp WHERE ctdp.maDatPhong = ?) " +
                        "AND NOT EXISTS (SELECT 1 FROM LuuTru lt WHERE lt.maDatPhong = ? AND lt.checkOut IS NULL) " +
                        "AND NOT EXISTS (" +
                        "    SELECT 1 FROM ChiTietDatPhong ctdp " +
                        "    WHERE ctdp.maDatPhong = ? " +
                        "      AND NOT EXISTS (" +
                        "          SELECT 1 FROM LuuTru ltDone " +
                        "          WHERE ltDone.maChiTietDatPhong = ctdp.maChiTietDatPhong AND ltDone.checkOut IS NOT NULL" +
                        "      )" +
                        ") " +
                        "THEN 1 ELSE 0 END")) {
            ps.setInt(1, maDatPhong);
            ps.setInt(2, maDatPhong);
            ps.setInt(3, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    private String buildBookingReadyForPaymentClause(String bookingIdExpression) {
        return "EXISTS (SELECT 1 FROM ChiTietDatPhong ctdpAll WHERE ctdpAll.maDatPhong = " + bookingIdExpression + ") " +
                "AND NOT EXISTS (SELECT 1 FROM LuuTru ltActive WHERE ltActive.maDatPhong = " + bookingIdExpression + " AND ltActive.checkOut IS NULL) " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM ChiTietDatPhong ctdpPending " +
                "    WHERE ctdpPending.maDatPhong = " + bookingIdExpression +
                "      AND NOT EXISTS (" +
                "          SELECT 1 FROM LuuTru ltDone " +
                "          WHERE ltDone.maChiTietDatPhong = ctdpPending.maChiTietDatPhong AND ltDone.checkOut IS NOT NULL" +
                "      )" +
                ")";
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }

    public static final class PaymentPart {
        private String phuongThuc;
        private double soTien;
        private String soThamChieu;

        public String getPhuongThuc() {
            return phuongThuc;
        }

        public void setPhuongThuc(String phuongThuc) {
            this.phuongThuc = phuongThuc;
        }

        public double getSoTien() {
            return soTien;
        }

        public void setSoTien(double soTien) {
            this.soTien = soTien;
        }

        public String getSoThamChieu() {
            return soThamChieu;
        }

        public void setSoThamChieu(String soThamChieu) {
            this.soThamChieu = soThamChieu;
        }
    }
}
