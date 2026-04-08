package dao;

import dao.KhachHangDAO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThanhToanDAO {
    private String lastErrorMessage = "";
    private static boolean schemaEnsured = false;
    private static boolean synchronizingInvoices = false;
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

            String sql = "WITH ranked AS (" +
                    "SELECT hd.maHoaDon, hd.maLuuTru, hd.maDatPhong, hd.maKhachHang, hd.ngayLap, hd.ngayThanhToan, " +
                    "hd.tienPhong, hd.tienDichVu, ISNULL(hd.phuThu,0) AS phuThu, ISNULL(hd.giamGia,0) AS giamGia, " +
                    "ISNULL(hd.tienCocTru,0) AS tienCocTru, ISNULL(hd.trangThai,N'Chờ thanh toán') AS trangThai, " +
                    "ISNULL(hd.ghiChu,N'') AS ghiChu, dp.trangThai AS trangThaiDatPhong, " +
                    "ROW_NUMBER() OVER (PARTITION BY hd.maDatPhong ORDER BY hd.maHoaDon DESC) AS rn " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                    "WHERE ISNULL(dp.trangThai, N'') IN (N'Đã check-out', N'Đã thanh toán')" +
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
            String sql = "SELECT lt.maLuuTru, lt.maDatPhong, lt.maChiTietDatPhong, dp.maKhachHang, dp.maBangGia, ISNULL(dp.tienCoc, 0) AS tienCocDatPhong, lt.giaPhong, lt.checkIn, lt.checkOut, " +
                    "ISNULL(ct.soDemDatPhong,0) AS soDemDatPhong, " +
                    "ISNULL(ct.giaPhongDatPhong,0) AS giaPhongDatPhong, " +
                    "ISNULL(ct.thanhTienDatPhong,0) AS thanhTienDatPhong " +
                    "FROM LuuTru lt " +
                    "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                    "OUTER APPLY ( " +
                    "   SELECT TOP 1 " +
                    "       ISNULL(DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong),0) AS soDemDatPhong, " +
                    "       ISNULL(ctdp.giaPhong,0) AS giaPhongDatPhong, " +
                    "       ISNULL(ctdp.thanhTien,0) AS thanhTienDatPhong " +
                    "   FROM ChiTietDatPhong ctdp " +
                    "   WHERE ctdp.maChiTietDatPhong = lt.maChiTietDatPhong " +
                    ") ct " +
                    "WHERE dp.trangThai IN (N'Đã check-out', N'Đã thanh toán')";

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
                            rs.getInt("maBangGia"),
                            rs.getDouble("giaPhong"),
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("checkOut"),
                            rs.getLong("soDemDatPhong"),
                            rs.getDouble("giaPhongDatPhong"),
                            rs.getDouble("thanhTienDatPhong"));
                    aggregate.tienPhong += roomCharge.getThanhTien().doubleValue();
                    aggregate.tienDichVu += loadServiceCharge(con, rs.getInt("maLuuTru"));
                }
            }

            for (InvoiceAggregate aggregate : aggregates.values()) {
                Integer maHoaDon = findLatestInvoiceIdByBooking(con, aggregate.maDatPhong);
                if (maHoaDon == null) {
                    maHoaDon = insertInvoiceHeader(con, aggregate.maLuuTruDaiDien, aggregate.maDatPhong, aggregate.maKhachHang,
                            aggregate.tienPhong, aggregate.tienDichVu, aggregate.tienCoc);
                } else {
                    try (PreparedStatement update = con.prepareStatement(
                            "UPDATE HoaDon SET maLuuTru = ?, maKhachHang = ?, tienPhong = ?, tienDichVu = ?, tienCocTru = CASE " +
                                    "WHEN tienCocTru IS NULL THEN ? " +
                                    "WHEN tienCocTru > ? THEN ? ELSE tienCocTru END " +
                                    "WHERE maHoaDon = ?")) {
                        double tienCocTru = Math.min(aggregate.tienCoc, Math.max(0d, aggregate.tienPhong + aggregate.tienDichVu));
                        update.setInt(1, aggregate.maLuuTruDaiDien);
                        update.setInt(2, aggregate.maKhachHang);
                        update.setDouble(3, aggregate.tienPhong);
                        update.setDouble(4, aggregate.tienDichVu);
                        update.setDouble(5, tienCocTru);
                        update.setDouble(6, aggregate.tienCoc);
                        update.setDouble(7, tienCocTru);
                        update.setInt(8, maHoaDon.intValue());
                        update.executeUpdate();
                    }
                }

                cleanupDuplicateInvoicesForBooking(con, aggregate.maDatPhong, maHoaDon.intValue());
                rebuildInvoiceLines(con, maHoaDon.intValue(), aggregate.maDatPhong);
                refreshInvoiceStatus(con, maHoaDon.intValue());
            }
        } finally {
            synchronizingInvoices = false;
        }
    }

    private Integer insertInvoiceHeader(Connection con, int maLuuTru, int maDatPhong, int maKhachHang,
                                        double tienPhong, double tienDichVu, double tienCoc) throws Exception {
        String sql = "INSERT INTO HoaDon(maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu) " +
                "VALUES (?, ?, ?, GETDATE(), ?, ?, 0, 0, ?, N'Chờ thanh toán', N'')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maLuuTru);
            ps.setInt(2, maDatPhong);
            ps.setInt(3, maKhachHang);
            ps.setDouble(4, tienPhong);
            ps.setDouble(5, tienDichVu);
            ps.setDouble(6, Math.min(tienCoc, Math.max(0d, tienPhong + tienDichVu)));
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
        try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
            del.setInt(1, maHoaDon);
            del.executeUpdate();
        }

        ThanhToan invoice = findHeaderById(con, maHoaDon);
        if (invoice == null) {
            return;
        }
        insertRoomChargeLines(con, maHoaDon, maDatPhong, invoice);
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

        if (invoice.getPhuThu() > 0d) {
            insertInvoiceLine(con, maHoaDon, "Phụ thu", 1, invoice.getPhuThu());
        }
        if (invoice.getGiamGia() > 0d) {
            insertInvoiceLine(con, maHoaDon, "Giảm giá", 1, -invoice.getGiamGia());
        }
    }

    private void insertRoomChargeLines(Connection con, int maHoaDon, int maDatPhong, ThanhToan invoice) throws Exception {
        String roomSql = "SELECT lt.maLuuTru, lt.maChiTietDatPhong, lt.giaPhong, lt.checkIn, lt.checkOut, dp.maBangGia, " +
                "ISNULL(p.soPhong, N'Phong') AS soPhong, " +
                "ISNULL(DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong),0) AS soDemDatPhong, " +
                "ISNULL(ctdp.giaPhong,0) AS giaPhongDatPhong, " +
                "ISNULL(ctdp.thanhTien,0) AS thanhTienDatPhong " +
                "FROM LuuTru lt " +
                "JOIN DatPhong dp ON lt.maDatPhong = dp.maDatPhong " +
                "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
                "LEFT JOIN ChiTietDatPhong ctdp ON lt.maChiTietDatPhong = ctdp.maChiTietDatPhong " +
                "WHERE lt.maDatPhong = ? " +
                "ORDER BY lt.maLuuTru ASC";
        try (PreparedStatement ps = con.prepareStatement(roomSql)) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                boolean insertedRoomLine = false;
                while (rs.next()) {
                    RoomChargeBreakdown roomCharge = calculateRoomCharge(
                            rs.getInt("maBangGia"),
                            rs.getDouble("giaPhong"),
                            rs.getTimestamp("checkIn"),
                            rs.getTimestamp("checkOut"),
                            rs.getLong("soDemDatPhong"),
                            rs.getDouble("giaPhongDatPhong"),
                            rs.getDouble("thanhTienDatPhong"));
                    if (roomCharge.getThanhTien().doubleValue() <= 0d) {
                        continue;
                    }
                    String roomLine = buildRoomInvoiceLine(
                            safeTrim(rs.getString("soPhong")),
                            roomCharge.getLoaiNgay(),
                            roomCharge.getLoaiGiaApDung(),
                            roomCharge.getSoGioLuuTru());
                    insertInvoiceLine(con, maHoaDon, roomLine, 1, roomCharge.getThanhTien().doubleValue());
                    insertedRoomLine = true;
                }
                if (!insertedRoomLine && invoice.getTienPhong() > 0d) {
                    insertInvoiceLine(con, maHoaDon, "Tiền phòng", 1, invoice.getTienPhong());
                }
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

    private String buildRoomInvoiceLine(String soPhong, String loaiNgay, String loaiGiaApDung, long soGioLuuTru) {
        StringBuilder builder = new StringBuilder("Tiền phòng");
        if (!isBlank(soPhong)) {
            builder.append(" - P").append(soPhong);
        }
        if (!isBlank(loaiGiaApDung)) {
            builder.append(" - ").append(loaiGiaApDung);
        }
        if (!isBlank(loaiNgay)) {
            builder.append(" - ").append(loaiNgay);
        }
        if (soGioLuuTru > 0) {
            builder.append(" - ").append(soGioLuuTru).append(" giờ");
        }
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
        invoice.setTrangThai(safeTrim(rs.getString("trangThai")));
        invoice.setGhiChu(safeTrim(rs.getString("ghiChu")));
        return invoice;
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

    private BigDecimal resolveAppliedRate(ChiTietBangGia detail, String loaiNgay) {
        if (detail == null) {
            return BigDecimal.ZERO;
        }
        if ("Ngay le".equalsIgnoreCase(loaiNgay) && detail.getGiaLe() > 0d) {
            return toMoney(detail.getGiaLe());
        }
        if ("Cuoi tuan".equalsIgnoreCase(loaiNgay) && detail.getGiaCuoiTuan() > 0d) {
            return toMoney(detail.getGiaCuoiTuan());
        }
        return toMoney(detail.getGiaTheoNgay());
    }

    private RoomChargeBreakdown calculateRoomCharge(int maBangGia,
                                                    double giaPhongLuuTru,
                                                    Timestamp checkIn,
                                                    Timestamp checkOut,
                                                    long soDemDatPhong,
                                                    double giaPhongDatPhong,
                                                    double thanhTienDatPhong) {
        LocalDateTime start = checkIn == null ? null : checkIn.toLocalDateTime();
        LocalDateTime end = checkOut == null ? null : checkOut.toLocalDateTime();
        long soGio = calculateStayHours(start, end);
        LocalDate ngayApDung = start == null ? null : start.toLocalDate();
        String loaiNgay = determineLoaiNgay(ngayApDung);
        ChiTietBangGia detail = bangGiaDAO.getChiTietBangGiaDangApDung(maBangGia, ngayApDung);
        if (detail == null) {
            List<ChiTietBangGia> details = bangGiaDAO.getChiTietBangGiaByMaBangGia(maBangGia);
            if (!details.isEmpty()) {
                detail = details.get(0);
            }
        }

        RoomChargeBreakdown breakdown = new RoomChargeBreakdown();
        breakdown.setLoaiNgay(loaiNgay);
        breakdown.setSoGioLuuTru(soGio);
        breakdown.setLoaiGiaApDung("Theo ngay");

        BigDecimal tienPhong = BigDecimal.ZERO;
        if (detail != null) {
            BigDecimal giaNgayUuTien = resolveAppliedRate(detail, loaiNgay);
            long soNgay = Math.max(1L, (long) Math.ceil(soGio / 24.0d));
            boolean quaDem = start != null && end != null && end.toLocalDate().isAfter(start.toLocalDate());

            if ("Ngay le".equalsIgnoreCase(loaiNgay) && detail.getGiaLe() > 0d) {
                breakdown.setLoaiGiaApDung("Gia le");
                tienPhong = toMoney(detail.getGiaLe()).multiply(BigDecimal.valueOf(soNgay));
            } else if ("Cuoi tuan".equalsIgnoreCase(loaiNgay) && detail.getGiaCuoiTuan() > 0d) {
                breakdown.setLoaiGiaApDung("Gia cuoi tuan");
                tienPhong = toMoney(detail.getGiaCuoiTuan()).multiply(BigDecimal.valueOf(soNgay));
            } else if (quaDem && soGio <= 24L && detail.getGiaQuaDem() > 0d) {
                breakdown.setLoaiGiaApDung("Qua dem");
                tienPhong = toMoney(detail.getGiaQuaDem());
            } else if (!quaDem && detail.getGiaTheoGio() > 0d) {
                BigDecimal tienTheoGio = toMoney(detail.getGiaTheoGio()).multiply(BigDecimal.valueOf(soGio));
                if (giaNgayUuTien.signum() > 0 && tienTheoGio.compareTo(giaNgayUuTien) > 0) {
                    breakdown.setLoaiGiaApDung("Theo ngay");
                    tienPhong = giaNgayUuTien;
                } else {
                    breakdown.setLoaiGiaApDung("Theo gio");
                    tienPhong = tienTheoGio;
                }
            } else if (giaNgayUuTien.signum() > 0) {
                breakdown.setLoaiGiaApDung("Theo ngay");
                tienPhong = giaNgayUuTien.multiply(BigDecimal.valueOf(soNgay));
            }
        }

        if (tienPhong.signum() <= 0 && thanhTienDatPhong > 0d) {
            breakdown.setLoaiGiaApDung("Theo ngay");
            tienPhong = toMoney(thanhTienDatPhong);
        }
        if (tienPhong.signum() <= 0 && giaPhongDatPhong > 0d) {
            long soDem = Math.max(1L, soDemDatPhong);
            breakdown.setLoaiGiaApDung("Theo ngay");
            tienPhong = toMoney(giaPhongDatPhong).multiply(BigDecimal.valueOf(soDem));
        }
        if (tienPhong.signum() <= 0 && giaPhongLuuTru > 0d) {
            long soNgay = Math.max(1L, (long) Math.ceil(soGio / 24.0d));
            breakdown.setLoaiGiaApDung("Theo ngay");
            tienPhong = toMoney(giaPhongLuuTru).multiply(BigDecimal.valueOf(soNgay));
        }

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
        try (PreparedStatement ps = con.prepareStatement("SELECT TOP 1 maHoaDon FROM HoaDon WHERE maDatPhong = ? ORDER BY maHoaDon DESC")) {
            ps.setInt(1, maDatPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
    }

    private void cleanupDuplicateInvoicesForBooking(Connection con, int maDatPhong, int keepMaHoaDon) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT maHoaDon FROM HoaDon WHERE maDatPhong = ? AND maHoaDon <> ? ORDER BY maHoaDon DESC")) {
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
        private int maKhachHang;
        private int maLuuTruDaiDien;
        private double tienPhong;
        private double tienDichVu;
        private double tienCoc;
    }

    private static final class RoomChargeBreakdown {
        private String loaiNgay;
        private String loaiGiaApDung;
        private long soGioLuuTru;
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

        public BigDecimal getThanhTien() {
            return thanhTien;
        }

        public void setThanhTien(BigDecimal thanhTien) {
            this.thanhTien = thanhTien;
        }
    }

    private void ensureExtendedSchema(Connection con) throws Exception {
        if (schemaEnsured) {
            return;
        }
        executeSql(con, "IF COL_LENGTH('HoaDon', 'phuThu') IS NULL ALTER TABLE HoaDon ADD phuThu DECIMAL(15,0) NULL");
        executeSql(con, "IF COL_LENGTH('HoaDon', 'giamGia') IS NULL ALTER TABLE HoaDon ADD giamGia DECIMAL(15,0) NULL");
        executeSql(con, "IF COL_LENGTH('HoaDon', 'tienCocTru') IS NULL ALTER TABLE HoaDon ADD tienCocTru DECIMAL(15,0) NULL");
        executeSql(con, "IF COL_LENGTH('HoaDon', 'trangThai') IS NULL ALTER TABLE HoaDon ADD trangThai NVARCHAR(30) NULL");
        executeSql(con, "IF COL_LENGTH('HoaDon', 'ghiChu') IS NULL ALTER TABLE HoaDon ADD ghiChu NVARCHAR(MAX) NULL");
        executeSql(con, "IF COL_LENGTH('HoaDon', 'ngayThanhToan') IS NULL ALTER TABLE HoaDon ADD ngayThanhToan DATETIME NULL");
        executeSql(con, "UPDATE HoaDon SET phuThu = ISNULL(phuThu,0), giamGia = ISNULL(giamGia,0), tienCocTru = ISNULL(tienCocTru,0), " +
                "trangThai = ISNULL(trangThai,N'Chờ thanh toán'), ghiChu = ISNULL(ghiChu,N'')");

        executeSql(con, "IF COL_LENGTH('ThanhToan', 'phuongThuc') IS NULL ALTER TABLE ThanhToan ADD phuongThuc NVARCHAR(30) NULL");
        executeSql(con, "IF COL_LENGTH('ThanhToan', 'soThamChieu') IS NULL ALTER TABLE ThanhToan ADD soThamChieu NVARCHAR(100) NULL");
        executeSql(con, "IF COL_LENGTH('ThanhToan', 'ghiChu') IS NULL ALTER TABLE ThanhToan ADD ghiChu NVARCHAR(MAX) NULL");
        executeSql(con, "IF COL_LENGTH('ThanhToan', 'loaiGiaoDich') IS NULL ALTER TABLE ThanhToan ADD loaiGiaoDich NVARCHAR(30) NULL");
        executeSql(con, "UPDATE ThanhToan SET phuongThuc = ISNULL(phuongThuc,N'Tiền mặt'), soThamChieu = ISNULL(soThamChieu,N''), " +
                "ghiChu = ISNULL(ghiChu,N''), loaiGiaoDich = ISNULL(loaiGiaoDich,N'THANH_TOAN')");
        executeSql(con, "IF COL_LENGTH('ChiTietHoaDon', 'loaiChiPhi') IS NOT NULL AND COL_LENGTH('ChiTietHoaDon', 'loaiChiPhi') < 240 ALTER TABLE ChiTietHoaDon ALTER COLUMN loaiChiPhi NVARCHAR(255) NULL");
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
        try {
            String sql = "SELECT maKhachHang, trangThai FROM HoaDon WHERE maHoaDon = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int maKhachHang = rs.getInt("maKhachHang");
                        String trangThai = safeTrim(rs.getString("trangThai"));
                        if (maKhachHang > 0 && "Đã thanh toán".equalsIgnoreCase(trangThai)) {
                            KhachHangDAO khachHangDAO = new KhachHangDAO();
                            khachHangDAO.updateTrangThaiTuDong(
                                    String.valueOf(maKhachHang),
                                    "Ngừng giao dịch",
                                    "Tự động cập nhật sau khi thanh toán xong hóa đơn HD" + maHoaDon
                            );
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void syncBookingAndRoomsAfterPayment(Connection con, int maHoaDon) {
        try {
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
            if (useDirectActiveStatusAfterCheckout()) {
                synchronizeOperationalStatusesWithoutCleaning(con);
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Phong SET trangThai = N'Hoạt động' WHERE trangThai IN (N'Hoạt động', N'Trống', N'Đã đặt', N'Đang ở', N'Dọn dẹp')")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE p SET p.trangThai = N'Đã đặt' FROM Phong p WHERE EXISTS (" +
                            "SELECT 1 FROM ChiTietDatPhong ctdp JOIN DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong " +
                            "WHERE ctdp.maPhong = p.maPhong AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in'))")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE p SET p.trangThai = N'Dọn dẹp' FROM Phong p WHERE EXISTS (" +
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
        } catch (Exception ignored) {
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
