package dao;

import dao.KhachHangDAO;
import db.ConnectDB;
import entity.ThanhToan;
import entity.ThanhToan.ChiTietDong;
import entity.ThanhToan.GiaoDichThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
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

            String sql = "SELECT hd.maHoaDon, hd.maLuuTru, hd.maDatPhong, hd.maKhachHang, hd.ngayLap, hd.ngayThanhToan, " +
                    "hd.tienPhong, hd.tienDichVu, ISNULL(hd.phuThu,0) AS phuThu, ISNULL(hd.giamGia,0) AS giamGia, " +
                    "ISNULL(hd.tienCocTru,0) AS tienCocTru, ISNULL(hd.trangThai,N'Chờ thanh toán') AS trangThai, " +
                    "ISNULL(hd.ghiChu,N'') AS ghiChu, kh.hoTen, kh.soDienThoai, p.soPhong, lt.tienCoc AS tienCocGoc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN LuuTru lt ON hd.maLuuTru = lt.maLuuTru " +
                    "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                    "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                    "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
                    "ORDER BY hd.maHoaDon DESC";

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
                    "ISNULL(hd.ghiChu,N'') AS ghiChu, kh.hoTen, kh.soDienThoai, p.soPhong, lt.tienCoc AS tienCocGoc " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN LuuTru lt ON hd.maLuuTru = lt.maLuuTru " +
                    "LEFT JOIN DatPhong dp ON hd.maDatPhong = dp.maDatPhong " +
                    "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                    "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
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
                    rebuildInvoiceLines(con, invoiceId.intValue(), thanhToan.getMaLuuTru());
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

            rebuildInvoiceLines(con, invoiceId.intValue(), invoice.getMaLuuTru());
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
        String sql = "SELECT lt.maLuuTru, lt.maDatPhong, dp.maKhachHang, lt.tienCoc, lt.giaPhong, lt.checkIn, lt.checkOut, " +
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
                ") ct";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int maLuuTru = rs.getInt("maLuuTru");
                int maDatPhong = rs.getInt("maDatPhong");
                int maKhachHang = rs.getInt("maKhachHang");
                double tienCoc = rs.getDouble("tienCoc");
                double giaPhong = rs.getDouble("giaPhong");
                Timestamp checkIn = rs.getTimestamp("checkIn");
                Timestamp checkOut = rs.getTimestamp("checkOut");
                long soDemDatPhong = rs.getLong("soDemDatPhong");
                double giaPhongDatPhong = rs.getDouble("giaPhongDatPhong");
                double thanhTienDatPhong = rs.getDouble("thanhTienDatPhong");

                double tienPhong = resolveRoomCharge(giaPhong, checkIn, checkOut, soDemDatPhong, giaPhongDatPhong, thanhTienDatPhong);
                double tienDichVu = loadServiceCharge(con, maLuuTru);

                Integer maHoaDon = findInvoiceIdByStay(con, maLuuTru);
                if (maHoaDon == null) {
                    maHoaDon = insertInvoiceHeader(con, maLuuTru, maDatPhong, maKhachHang, tienPhong, tienDichVu, tienCoc);
                } else {
                    try (PreparedStatement update = con.prepareStatement(
                            "UPDATE HoaDon SET tienPhong = ?, tienDichVu = ?, tienCocTru = CASE " +
                                    "WHEN tienCocTru IS NULL THEN ? " +
                                    "WHEN tienCocTru > ? THEN ? ELSE tienCocTru END " +
                                    "WHERE maHoaDon = ?")) {
                        double tienCocTru = Math.min(tienCoc, Math.max(0d, tienPhong + tienDichVu));
                        update.setDouble(1, tienPhong);
                        update.setDouble(2, tienDichVu);
                        update.setDouble(3, tienCocTru);
                        update.setDouble(4, tienCoc);
                        update.setDouble(5, tienCocTru);
                        update.setInt(6, maHoaDon.intValue());
                        update.executeUpdate();
                    }
                }

                rebuildInvoiceLines(con, maHoaDon.intValue(), String.valueOf(maLuuTru));
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

    private void rebuildInvoiceLines(Connection con, int maHoaDon, String maLuuTru) throws Exception {
        try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE maHoaDon = ?")) {
            del.setInt(1, maHoaDon);
            del.executeUpdate();
        }

        ThanhToan invoice = findHeaderById(con, maHoaDon);
        if (invoice == null) {
            return;
        }

        insertInvoiceLine(con, maHoaDon, "Tiền phòng", 1, invoice.getTienPhong());

        String serviceSql = "SELECT dv.tenDichVu, SUM(sddv.soLuong) AS soLuong, MAX(sddv.donGia) AS donGia " +
                "FROM SuDungDichVu sddv " +
                "JOIN DichVu dv ON sddv.maDichVu = dv.maDichVu " +
                "WHERE sddv.maLuuTru = ? GROUP BY dv.tenDichVu ORDER BY dv.tenDichVu";
        try (PreparedStatement ps = con.prepareStatement(serviceSql)) {
            ps.setInt(1, parseIntOrZero(maLuuTru));
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
            insertInvoiceLine(con, maHoaDon, "PHU_THU", 1, invoice.getPhuThu());
        }
        if (invoice.getGiamGia() > 0d) {
            insertInvoiceLine(con, maHoaDon, "GIAM_GIA", 1, -invoice.getGiamGia());
        }
    }

    private void insertInvoiceLine(Connection con, int maHoaDon, String loaiChiPhi, int soLuong, double donGia) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ChiTietHoaDon(maHoaDon, loaiChiPhi, soLuong, donGia) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, maHoaDon);
            ps.setString(2, loaiChiPhi);
            ps.setInt(3, soLuong);
            ps.setDouble(4, donGia);
            ps.executeUpdate();
        }
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
                "ISNULL(hd.ghiChu,N'') AS ghiChu, kh.hoTen, kh.soDienThoai, p.soPhong, lt.tienCoc AS tienCocGoc " +
                "FROM HoaDon hd " +
                "LEFT JOIN LuuTru lt ON hd.maLuuTru = lt.maLuuTru " +
                "LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN Phong p ON lt.maPhong = p.maPhong " +
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
        invoice.setMaHoSo(isBlank(invoice.getMaLuuTru()) ? "-" : "LT-" + invoice.getMaLuuTru());
        invoice.setKhachHang(safeTrim(rs.getString("hoTen")));
        invoice.setSoPhong(safeTrim(rs.getString("soPhong")));
        invoice.setSoDienThoai(safeTrim(rs.getString("soDienThoai")));
        invoice.setNgayLap(rs.getTimestamp("ngayLap"));
        invoice.setNgayThanhToan(rs.getTimestamp("ngayThanhToan"));
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

    private double calculateRoomCharge(double giaPhong, Timestamp checkIn, Timestamp checkOut) {
        if (giaPhong <= 0d) {
            return 0d;
        }
        LocalDateTime start = checkIn == null ? LocalDateTime.now() : checkIn.toLocalDateTime();
        LocalDateTime end = checkOut == null ? LocalDateTime.now() : checkOut.toLocalDateTime();
        long hours = Math.max(1L, Duration.between(start, end).toHours());
        long nights = Math.max(1L, (hours + 23L) / 24L);
        return giaPhong * nights;
    }

    private double resolveRoomCharge(double giaPhongLuuTru,
                                     Timestamp checkIn,
                                     Timestamp checkOut,
                                     long soDemDatPhong,
                                     double giaPhongDatPhong,
                                     double thanhTienDatPhong) {
        double tienTheoLuuTru = calculateRoomCharge(giaPhongLuuTru, checkIn, checkOut);
        if (tienTheoLuuTru > 0d) {
            return tienTheoLuuTru;
        }
        if (thanhTienDatPhong > 0d) {
            return thanhTienDatPhong;
        }
        if (giaPhongDatPhong > 0d) {
            long soDem = Math.max(1L, soDemDatPhong);
            return giaPhongDatPhong * soDem;
        }
        return 0d;
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

    private Integer findInvoiceIdByStay(Connection con, int maLuuTru) throws Exception {
        try (PreparedStatement ps = con.prepareStatement("SELECT TOP 1 maHoaDon FROM HoaDon WHERE maLuuTru = ? ORDER BY maHoaDon DESC")) {
            ps.setInt(1, maLuuTru);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        }
        return null;
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

            boolean allPaid = true;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM HoaDon WHERE maDatPhong = ? AND ISNULL(trangThai, N'Chờ thanh toán') <> N'Đã thanh toán'")) {
                ps.setInt(1, maDatPhong);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        allPaid = rs.getInt(1) == 0;
                    }
                }
            }
            if (!allPaid) {
                return;
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE DatPhong SET trangThai = N'Đã thanh toán' WHERE maDatPhong = ?")) {
                ps.setInt(1, maDatPhong);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Phong SET trangThai = N'Hoạt động' " +
                            "WHERE maPhong IN (SELECT DISTINCT maPhong FROM LuuTru WHERE maDatPhong = ? AND maPhong IS NOT NULL)")) {
                ps.setInt(1, maDatPhong);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {
        }
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
