package dao;

import db.ConnectDB;
import entity.DichVu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {
    private static final String SELECT_BASE =
            "SELECT maDichVu, tenDichVu, CAST(donGia AS FLOAT) AS donGia, donVi FROM DichVu";

    public List<DichVu> getAll() {
        List<DichVu> dsDichVu = new ArrayList<DichVu>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsDichVu;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " ORDER BY maDichVu DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsDichVu.add(mapDichVu(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach dich vu.");
            e.printStackTrace();
        }
        return dsDichVu;
    }

    public DichVu findById(int maDichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " WHERE maDichVu = ?")) {
            stmt.setInt(1, maDichVu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapDichVu(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim dich vu theo ma: " + maDichVu);
            e.printStackTrace();
        }
        return null;
    }

    public List<DichVu> search(String keyword) {
        List<DichVu> dsDichVu = new ArrayList<DichVu>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsDichVu;
        }

        String tuKhoa = keyword == null ? "" : keyword.trim();
        String sql = SELECT_BASE
                + " WHERE (? = '' OR CAST(maDichVu AS NVARCHAR(20)) LIKE ? OR tenDichVu LIKE ? OR donVi LIKE ?)"
                + " ORDER BY maDichVu DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tuKhoa);
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, "%" + tuKhoa + "%");
            stmt.setString(4, "%" + tuKhoa + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsDichVu.add(mapDichVu(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim kiem dich vu.");
            e.printStackTrace();
        }
        return dsDichVu;
    }

    public boolean insert(DichVu dichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null || dichVu == null) {
            return false;
        }

        String sql = "INSERT INTO DichVu(tenDichVu, donGia, donVi) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dichVu.getTenDichVu());
            stmt.setDouble(2, dichVu.getDonGia());
            stmt.setString(3, dichVu.getDonVi());
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        dichVu.setMaDichVu(rs.getInt(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them dich vu.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(DichVu dichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null || dichVu == null) {
            return false;
        }

        String sql = "UPDATE DichVu SET tenDichVu = ?, donGia = ?, donVi = ? WHERE maDichVu = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, dichVu.getTenDichVu());
            stmt.setDouble(2, dichVu.getDonGia());
            stmt.setString(3, dichVu.getDonVi());
            stmt.setInt(4, dichVu.getMaDichVu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat dich vu co ma: " + dichVu.getMaDichVu());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maDichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "DELETE FROM DichVu WHERE maDichVu = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maDichVu);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi xoa dich vu co ma: " + maDichVu);
            e.printStackTrace();
            return false;
        }
    }

    private DichVu mapDichVu(ResultSet rs) throws SQLException {
        return new DichVu(
                rs.getInt("maDichVu"),
                rs.getString("tenDichVu"),
                rs.getDouble("donGia"),
                rs.getString("donVi")
        );
    }
}
