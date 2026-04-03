package dao;

import db.ConnectDB;
import entity.TienNghi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TienNghiDAO {
    private static final String SELECT_BASE =
            "SELECT maTienNghi, tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa FROM TienNghi";

    public List<TienNghi> getAll() {
        List<TienNghi> dsTienNghi = new ArrayList<TienNghi>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsTienNghi;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " ORDER BY uuTien, maTienNghi");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsTienNghi.add(mapTienNghi(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach tien nghi.");
            e.printStackTrace();
        }
        return dsTienNghi;
    }

    public TienNghi findById(int maTienNghi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE maTienNghi = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maTienNghi);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTienNghi(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim tien nghi theo ma: " + maTienNghi);
            e.printStackTrace();
        }
        return null;
    }

    public List<TienNghi> search(String keyword, String nhomTienNghi, String trangThai) {
        List<TienNghi> dsTienNghi = new ArrayList<TienNghi>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsTienNghi;
        }

        String sql = SELECT_BASE
                + " WHERE (? = '' OR tenTienNghi LIKE ?)"
                + " AND (? = '' OR nhomTienNghi = ?)"
                + " AND (? = '' OR trangThai = ?)"
                + " ORDER BY uuTien, maTienNghi";

        String tuKhoa = keyword == null ? "" : keyword.trim();
        String nhom = nhomTienNghi == null ? "" : nhomTienNghi.trim();
        String trangThaiSearch = trangThai == null ? "" : trangThai.trim();

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tuKhoa);
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, nhom);
            stmt.setString(4, nhom);
            stmt.setString(5, trangThaiSearch);
            stmt.setString(6, trangThaiSearch);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsTienNghi.add(mapTienNghi(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim kiem tien nghi.");
            e.printStackTrace();
        }
        return dsTienNghi;
    }

    public List<String> getDistinctNhomTienNghi() {
        List<String> dsNhom = new ArrayList<String>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsNhom;
        }

        String sql = "SELECT DISTINCT nhomTienNghi FROM TienNghi "
                + "WHERE nhomTienNghi IS NOT NULL AND LTRIM(RTRIM(nhomTienNghi)) <> '' "
                + "ORDER BY nhomTienNghi";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsNhom.add(rs.getString("nhomTienNghi"));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van nhom tien nghi.");
            e.printStackTrace();
        }
        return dsNhom;
    }

    public List<TienNghi> getByNhomTienNghi(String nhomTienNghi) {
        List<TienNghi> dsTienNghi = new ArrayList<TienNghi>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsTienNghi;
        }

        String nhom = nhomTienNghi == null ? "" : nhomTienNghi.trim();
        String sql;
        if (nhom.isEmpty()) {
            sql = SELECT_BASE + " ORDER BY nhomTienNghi, uuTien, maTienNghi";
        } else {
            sql = SELECT_BASE + " WHERE nhomTienNghi = ? ORDER BY uuTien, maTienNghi";
        }

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            if (!nhom.isEmpty()) {
                stmt.setString(1, nhom);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsTienNghi.add(mapTienNghi(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van tien nghi theo nhom.");
            e.printStackTrace();
        }
        return dsTienNghi;
    }

    public boolean insert(TienNghi tienNghi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "INSERT INTO TienNghi(tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tienNghi.getTenTienNghi());
            stmt.setString(2, tienNghi.getNhomTienNghi());
            stmt.setString(3, tienNghi.getTrangThai());
            stmt.setInt(4, tienNghi.getUuTien());
            stmt.setString(5, tienNghi.getMoTa());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi them tien nghi.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(TienNghi tienNghi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "UPDATE TienNghi SET tenTienNghi = ?, nhomTienNghi = ?, trangThai = ?, uuTien = ?, moTa = ? WHERE maTienNghi = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tienNghi.getTenTienNghi());
            stmt.setString(2, tienNghi.getNhomTienNghi());
            stmt.setString(3, tienNghi.getTrangThai());
            stmt.setInt(4, tienNghi.getUuTien());
            stmt.setString(5, tienNghi.getMoTa());
            stmt.setInt(6, tienNghi.getMaTienNghi());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat tien nghi co ma: " + tienNghi.getMaTienNghi());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maTienNghi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "DELETE FROM TienNghi WHERE maTienNghi = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maTienNghi);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi xoa tien nghi co ma: " + maTienNghi);
            e.printStackTrace();
            return false;
        }
    }

    private TienNghi mapTienNghi(ResultSet rs) throws SQLException {
        return new TienNghi(
                rs.getInt("maTienNghi"),
                rs.getString("tenTienNghi"),
                rs.getString("nhomTienNghi"),
                rs.getString("trangThai"),
                rs.getInt("uuTien"),
                rs.getString("moTa")
        );
    }
}
