package dao;

import db.ConnectDB;
import entity.NhanVien;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    private static final String SELECT_BASE =
            "SELECT maNhanVien, hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, "
                    + "boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu FROM NhanVien";

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<NhanVien> getAll() {
        clearLastError();
        List<NhanVien> dsNhanVien = new ArrayList<NhanVien>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsNhanVien;
        }

        String sql = SELECT_BASE + " ORDER BY maNhanVien DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsNhanVien.add(mapNhanVien(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach nhan vien.");
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return dsNhanVien;
    }

    public NhanVien findById(int maNhanVien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }

        String sql = SELECT_BASE + " WHERE maNhanVien = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maNhanVien);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapNhanVien(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim nhan vien theo ma: " + maNhanVien);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return null;
    }

    public boolean insert(NhanVien nhanVien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || nhanVien == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu nhân viên không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO NhanVien(hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, "
                + "boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, nhanVien);
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        nhanVien.setMaNhanVien(rs.getInt(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them nhan vien.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public boolean update(NhanVien nhanVien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || nhanVien == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu nhân viên không hợp lệ.");
            return false;
        }

        String sql = "UPDATE NhanVien SET hoTen = ?, ngaySinh = ?, gioiTinh = ?, cccd = ?, soDienThoai = ?, "
                + "email = ?, diaChi = ?, boPhan = ?, chucVu = ?, caLam = ?, ngayVaoLam = ?, trangThai = ?, ghiChu = ? "
                + "WHERE maNhanVien = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            fillStatement(stmt, nhanVien);
            stmt.setInt(14, nhanVien.getMaNhanVien());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat nhan vien co ma: " + nhanVien.getMaNhanVien());
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public boolean delete(int maNhanVien) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "DELETE FROM NhanVien WHERE maNhanVien = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maNhanVien);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi xoa nhan vien co ma: " + maNhanVien);
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public List<NhanVien> search(String hoTen, String boPhan, String chucVu, String trangThai) {
        clearLastError();
        List<NhanVien> dsNhanVien = new ArrayList<NhanVien>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsNhanVien;
        }

        String hoTenSearch = hoTen == null ? "" : hoTen.trim();
        String boPhanSearch = boPhan == null ? "" : boPhan.trim();
        String chucVuSearch = chucVu == null ? "" : chucVu.trim();
        String trangThaiSearch = trangThai == null ? "" : trangThai.trim();

        String sql = SELECT_BASE
                + " WHERE (? = '' OR hoTen LIKE ?)"
                + " AND (? = '' OR boPhan = ?)"
                + " AND (? = '' OR chucVu = ?)"
                + " AND (? = '' OR trangThai = ?)"
                + " ORDER BY maNhanVien DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, hoTenSearch);
            stmt.setString(2, "%" + hoTenSearch + "%");
            stmt.setString(3, boPhanSearch);
            stmt.setString(4, boPhanSearch);
            stmt.setString(5, chucVuSearch);
            stmt.setString(6, chucVuSearch);
            stmt.setString(7, trangThaiSearch);
            stmt.setString(8, trangThaiSearch);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsNhanVien.add(mapNhanVien(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim kiem nhan vien.");
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return dsNhanVien;
    }

    public void khoaTaiKhoanNeuNhanVienBiNgung(int maNhanVien, String trangThaiNhanVien) {
        clearLastError();

        if (maNhanVien <= 0 || trangThaiNhanVien == null) {
            return;
        }

        String trangThai = trangThaiNhanVien.trim();
        if (!"Ngừng làm việc".equalsIgnoreCase(trangThai) && !"Khóa".equalsIgnoreCase(trangThai)) {
            return;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return;
        }

        String sql = "UPDATE TaiKhoan SET trangThai = N'Khóa' WHERE maNhanVien = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maNhanVien);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            setLastError(e.getMessage());
        }
    }

    private void fillStatement(PreparedStatement stmt, NhanVien nhanVien) throws SQLException {
        stmt.setString(1, nhanVien.getHoTen());
        stmt.setDate(2, nhanVien.getNgaySinh());
        stmt.setString(3, nhanVien.getGioiTinh());
        stmt.setString(4, nhanVien.getCccd());
        stmt.setString(5, nhanVien.getSoDienThoai());
        stmt.setString(6, nhanVien.getEmail());
        stmt.setString(7, nhanVien.getDiaChi());
        stmt.setString(8, nhanVien.getBoPhan());
        stmt.setString(9, nhanVien.getChucVu());
        stmt.setString(10, nhanVien.getCaLam());
        stmt.setDate(11, nhanVien.getNgayVaoLam());
        stmt.setString(12, nhanVien.getTrangThai());
        stmt.setString(13, nhanVien.getGhiChu());
    }

    private NhanVien mapNhanVien(ResultSet rs) throws SQLException {
        return new NhanVien(
                rs.getInt("maNhanVien"),
                rs.getString("hoTen"),
                rs.getDate("ngaySinh"),
                rs.getString("gioiTinh"),
                rs.getString("cccd"),
                rs.getString("soDienThoai"),
                rs.getString("email"),
                rs.getString("diaChi"),
                rs.getString("boPhan"),
                rs.getString("chucVu"),
                rs.getString("caLam"),
                rs.getDate("ngayVaoLam"),
                rs.getString("trangThai"),
                rs.getString("ghiChu")
        );
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
