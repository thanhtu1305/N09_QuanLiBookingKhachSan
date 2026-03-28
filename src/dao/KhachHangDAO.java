package dao;

import db.ConnectDB;
import entity.KhachHang;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {
    private static final String SELECT_BASE =
            "SELECT maKhachHang, hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, "
                    + "quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu "
                    + "FROM KhachHang";

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<KhachHang> getAll() {
        clearLastError();
        List<KhachHang> result = new ArrayList<KhachHang>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String sql = SELECT_BASE + " ORDER BY maKhachHang DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapKhachHang(rs));
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<KhachHang> search(String keyword, String loaiKhach, String hangKhach, String trangThai) {
        clearLastError();
        List<KhachHang> result = new ArrayList<KhachHang>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String tuKhoa = safeTrim(keyword);
        String loai = safeTrim(loaiKhach);
        String hang = safeTrim(hangKhach);
        String status = safeTrim(trangThai);

        String sql = SELECT_BASE
                + " WHERE (? = '' OR CAST(maKhachHang AS NVARCHAR(20)) LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ? OR cccdPassport LIKE ?)"
                + " AND (? = '' OR loaiKhach = ?)"
                + " AND (? = '' OR hangKhach = ?)"
                + " AND (? = '' OR trangThai = ?)"
                + " ORDER BY maKhachHang DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tuKhoa);
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, "%" + tuKhoa + "%");
            stmt.setString(4, "%" + tuKhoa + "%");
            stmt.setString(5, "%" + tuKhoa + "%");
            stmt.setString(6, loai);
            stmt.setString(7, loai);
            stmt.setString(8, hang);
            stmt.setString(9, hang);
            stmt.setString(10, status);
            stmt.setString(11, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public KhachHang findById(String maKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return null;
        }

        String sql = SELECT_BASE + " WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public KhachHang findByPhone(String soDienThoai) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }
        String sdt = safeTrim(soDienThoai);
        if (sdt.isEmpty()) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE soDienThoai = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, sdt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public KhachHang findByCccdPassport(String cccdPassport) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }
        String value = safeTrim(cccdPassport);
        if (value.isEmpty()) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE cccdPassport = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(KhachHang khachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || khachHang == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu khách hàng không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, khachHang);
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        khachHang.setMaKhachHang(String.valueOf(rs.getInt(1)));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(KhachHang khachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = khachHang == null ? null : parseIntOrNull(khachHang.getMaKhachHang());
        if (con == null || khachHang == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return false;
        }

        String sql = "UPDATE KhachHang SET hoTen = ?, gioiTinh = ?, ngaySinh = ?, soDienThoai = ?, email = ?, "
                + "cccdPassport = ?, diaChi = ?, quocTich = ?, loaiKhach = ?, hangKhach = ?, trangThai = ?, nguoiTao = ?, ghiChu = ? "
                + "WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            fillStatement(stmt, khachHang);
            stmt.setInt(14, id.intValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String maKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return false;
        }

        String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByPhone(String soDienThoai, String excludeMaKhachHang) {
        return existsByField("soDienThoai", soDienThoai, excludeMaKhachHang);
    }

    public boolean existsByCccdPassport(String cccdPassport, String excludeMaKhachHang) {
        return existsByField("cccdPassport", cccdPassport, excludeMaKhachHang);
    }

    public List<String> getLichSuLuuTruGanDay(String maKhachHang) {
        clearLastError();
        List<String> result = new ArrayList<String>();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return result;
        }

        String sql = "SELECT TOP 10 p.soPhong, dp.ngayNhanPhong, dp.ngayTraPhong, dp.trangThai "
                + "FROM DatPhong dp "
                + "LEFT JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong "
                + "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong "
                + "WHERE dp.maKhachHang = ? "
                + "ORDER BY dp.ngayNhanPhong DESC, dp.maDatPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String soPhong = safeTrim(rs.getString("soPhong"));
                    String ngayNhan = rs.getDate("ngayNhanPhong") == null ? "-" : rs.getDate("ngayNhanPhong").toLocalDate().toString();
                    String ngayTra = rs.getDate("ngayTraPhong") == null ? "-" : rs.getDate("ngayTraPhong").toLocalDate().toString();
                    String trangThai = safeTrim(rs.getString("trangThai"));
                    result.add((soPhong.isEmpty() ? "Chưa gán phòng" : soPhong) + " - " + ngayNhan + " - " + ngayTra + " - " + trangThai);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        if (result.isEmpty()) {
            result.add("Không có lịch sử lưu trú gần đây");
        }
        return result;
    }

    private boolean existsByField(String fieldName, String value, String excludeMaKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }
        String safeValue = safeTrim(value);
        if (safeValue.isEmpty()) {
            return false;
        }

        Integer excludeId = parseIntOrNull(excludeMaKhachHang);
        String sql = "SELECT COUNT(1) FROM KhachHang WHERE " + fieldName + " = ?"
                + (excludeId != null ? " AND maKhachHang <> ?" : "");
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, safeValue);
            if (excludeId != null) {
                stmt.setInt(2, excludeId.intValue());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void fillStatement(PreparedStatement stmt, KhachHang khachHang) throws SQLException {
        stmt.setString(1, safeTrim(khachHang.getHoTen()));
        stmt.setString(2, nullIfEmpty(khachHang.getGioiTinh()));
        setNullableDate(stmt, 3, khachHang.getNgaySinh());
        stmt.setString(4, nullIfEmpty(khachHang.getSoDienThoai()));
        stmt.setString(5, nullIfEmpty(khachHang.getEmail()));
        stmt.setString(6, nullIfEmpty(khachHang.getCccdPassport()));
        stmt.setString(7, nullIfEmpty(khachHang.getDiaChi()));
        stmt.setString(8, nullIfEmpty(khachHang.getQuocTich()));
        stmt.setString(9, nullIfEmpty(khachHang.getLoaiKhach()));
        stmt.setString(10, nullIfEmpty(khachHang.getHangKhach()));
        stmt.setString(11, nullIfEmpty(khachHang.getTrangThai()));
        stmt.setString(12, nullIfEmpty(khachHang.getNguoiTao()));
        stmt.setString(13, nullIfEmpty(khachHang.getGhiChu()));
    }

    private KhachHang mapKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(String.valueOf(rs.getInt("maKhachHang")));
        kh.setHoTen(rs.getString("hoTen"));
        kh.setGioiTinh(rs.getString("gioiTinh"));
        kh.setNgaySinh(rs.getDate("ngaySinh") == null ? "" : rs.getDate("ngaySinh").toLocalDate().toString());
        kh.setSoDienThoai(rs.getString("soDienThoai"));
        kh.setEmail(rs.getString("email"));
        kh.setCccdPassport(rs.getString("cccdPassport"));
        kh.setDiaChi(rs.getString("diaChi"));
        kh.setQuocTich(rs.getString("quocTich"));
        kh.setLoaiKhach(rs.getString("loaiKhach"));
        kh.setHangKhach(rs.getString("hangKhach"));
        kh.setTrangThai(rs.getString("trangThai"));
        kh.setNguoiTao(rs.getString("nguoiTao"));
        kh.setGhiChu(rs.getString("ghiChu"));
        return kh;
    }

    private void setNullableDate(PreparedStatement stmt, int index, String value) throws SQLException {
        String text = safeTrim(value);
        if (text.isEmpty()) {
            stmt.setDate(index, null);
            return;
        }
        stmt.setDate(index, Date.valueOf(LocalDate.parse(text)));
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

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String nullIfEmpty(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
