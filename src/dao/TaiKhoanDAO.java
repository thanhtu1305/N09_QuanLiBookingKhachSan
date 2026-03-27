package dao;

import db.ConnectDB;
import entity.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {
    private String lastLoginMessage = "";

    public String getLastLoginMessage() {
        return lastLoginMessage;
    }

    private void setLastLoginMessage(String message) {
        lastLoginMessage = message == null ? "" : message;
    }

    public List<TaiKhoan> getAll() {
        List<TaiKhoan> ds = new ArrayList<>();
        String sql = "SELECT * FROM TaiKhoan";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TaiKhoan tk = mapResultSet(rs);
                ds.add(tk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public TaiKhoan findById(String maTaiKhoan) {
        String sql = "SELECT * FROM TaiKhoan WHERE maTaiKhoan = ?";

        try {
            Connection con = ConnectDB.getConnection();
            if (con == null) {
                System.out.println("Connection null");
                return null;
            }
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(maTaiKhoan));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(TaiKhoan taiKhoan) {
        String sql = "INSERT INTO TaiKhoan(maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, taiKhoan.getMaNhanVien());
            ps.setString(2, taiKhoan.getTenDangNhap());
            ps.setString(3, taiKhoan.getMatKhau());
            ps.setString(4, taiKhoan.getVaiTro());
            ps.setString(5, taiKhoan.getTrangThai());
            ps.setTimestamp(6, taiKhoan.getLanDangNhapCuoi());
            ps.setString(7, taiKhoan.getEmailKhoiPhuc());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(TaiKhoan taiKhoan) {
        String sql = "UPDATE TaiKhoan SET maNhanVien=?, tenDangNhap=?, matKhau=?, vaiTro=?, trangThai=?, "
                + "lanDangNhapCuoi=?, emailKhoiPhuc=? WHERE maTaiKhoan=?";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, taiKhoan.getMaNhanVien());
            ps.setString(2, taiKhoan.getTenDangNhap());
            ps.setString(3, taiKhoan.getMatKhau());
            ps.setString(4, taiKhoan.getVaiTro());
            ps.setString(5, taiKhoan.getTrangThai());
            ps.setTimestamp(6, taiKhoan.getLanDangNhapCuoi());
            ps.setString(7, taiKhoan.getEmailKhoiPhuc());
            ps.setInt(8, taiKhoan.getMaTaiKhoan());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(String maTaiKhoan) {
        String sql = "DELETE FROM TaiKhoan WHERE maTaiKhoan=?";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(maTaiKhoan));

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public TaiKhoan dangNhap(String tenDangNhap, String matKhau, String vaiTro) {
        setLastLoginMessage("");

        String sql = "SELECT tk.*, nv.trangThai AS trangThaiNhanVien "
                + "FROM TaiKhoan tk "
                + "LEFT JOIN NhanVien nv ON tk.maNhanVien = nv.maNhanVien "
                + "WHERE tk.tenDangNhap = ? AND tk.matKhau = ? AND tk.vaiTro = ?";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            ps.setString(3, vaiTro);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                setLastLoginMessage("Sai tên đăng nhập, mật khẩu hoặc vai trò.");
                return null;
            }

            String trangThaiTaiKhoan = rs.getString("trangThai");
            String trangThaiNhanVien = rs.getString("trangThaiNhanVien");

            if (!"Hoạt động".equalsIgnoreCase(trangThaiTaiKhoan)) {
                setLastLoginMessage("Tài khoản của bạn đã bị khóa.");
                return null;
            }

            if ("Ngừng làm việc".equalsIgnoreCase(trangThaiNhanVien)
                    || "Khóa".equalsIgnoreCase(trangThaiNhanVien)) {
                setLastLoginMessage("Bạn đã bị đuổi việc");
                return null;
            }

            TaiKhoan tk = mapResultSet(rs);
            capNhatLanDangNhapCuoi(tk.getMaTaiKhoan());
            return tk;
        } catch (Exception e) {
            e.printStackTrace();
            setLastLoginMessage("Lỗi hệ thống khi đăng nhập.");
        }

        return null;
    }

    public void capNhatLanDangNhapCuoi(int maTaiKhoan) {
        String sql = "UPDATE TaiKhoan SET lanDangNhapCuoi = GETDATE() WHERE maTaiKhoan = ?";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maTaiKhoan);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TaiKhoan mapResultSet(ResultSet rs) throws Exception {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTaiKhoan(rs.getInt("maTaiKhoan"));
        tk.setMaNhanVien(rs.getInt("maNhanVien"));
        tk.setTenDangNhap(rs.getString("tenDangNhap"));
        tk.setMatKhau(rs.getString("matKhau"));
        tk.setVaiTro(rs.getString("vaiTro"));
        tk.setTrangThai(rs.getString("trangThai"));
        tk.setLanDangNhapCuoi(rs.getTimestamp("lanDangNhapCuoi"));
        tk.setEmailKhoiPhuc(rs.getString("emailKhoiPhuc"));
        return tk;
    }
}
