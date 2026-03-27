package dao;

import db.ConnectDB;
import entity.LoaiPhong;
import entity.Phong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {
    private static final String SELECT_BASE =
            "SELECT p.maPhong, p.maLoaiPhong, p.soPhong, p.tang, p.khuVuc, "
                    + "p.sucChuaChuan, p.sucChuaToiDa, p.trangThai, lp.tenLoaiPhong "
                    + "FROM Phong p "
                    + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong";

    public List<Phong> getAll() {
        List<Phong> dsPhong = new ArrayList<Phong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsPhong;
        }

        String sql = SELECT_BASE + " ORDER BY p.soPhong ASC, p.maPhong ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsPhong.add(mapPhong(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach phong.");
            e.printStackTrace();
        }
        return dsPhong;
    }

    public Phong findById(int maPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE p.maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPhong(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim phong theo ma: " + maPhong);
            e.printStackTrace();
        }
        return null;
    }

    public List<Phong> search(String keyword, String maLoaiPhongOrTenLoaiPhong, String trangThai) {
        List<Phong> dsPhong = new ArrayList<Phong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsPhong;
        }

        String tuKhoa = keyword == null ? "" : keyword.trim();
        String loaiPhongFilter = maLoaiPhongOrTenLoaiPhong == null ? "" : maLoaiPhongOrTenLoaiPhong.trim();
        String trangThaiFilter = trangThai == null ? "" : trangThai.trim();

        String sql = SELECT_BASE
                + " WHERE (? = '' OR p.soPhong LIKE ? OR CAST(p.maPhong AS NVARCHAR(20)) LIKE ? OR p.tang LIKE ? OR p.khuVuc LIKE ?)"
                + " AND (? = '' OR CAST(p.maLoaiPhong AS NVARCHAR(20)) = ? OR lp.tenLoaiPhong = ?)"
                + " AND (? = '' OR p.trangThai = ?)"
                + " ORDER BY p.soPhong ASC, p.maPhong ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tuKhoa);
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, "%" + tuKhoa + "%");
            stmt.setString(4, "%" + tuKhoa + "%");
            stmt.setString(5, "%" + tuKhoa + "%");
            stmt.setString(6, loaiPhongFilter);
            stmt.setString(7, loaiPhongFilter);
            stmt.setString(8, loaiPhongFilter);
            stmt.setString(9, trangThaiFilter);
            stmt.setString(10, trangThaiFilter);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsPhong.add(mapPhong(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim kiem phong.");
            e.printStackTrace();
        }
        return dsPhong;
    }

    public boolean insert(Phong phong) {
        Connection con = ConnectDB.getConnection();
        if (con == null || phong == null) {
            return false;
        }

        String sql = "INSERT INTO Phong(maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, phong.getMaLoaiPhong());
            stmt.setString(2, phong.getSoPhong());
            stmt.setString(3, phong.getTang());
            stmt.setString(4, phong.getKhuVuc());
            stmt.setInt(5, phong.getSucChuaChuan());
            stmt.setInt(6, phong.getSucChuaToiDa());
            stmt.setString(7, phong.getTrangThai());
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        phong.setMaPhong(rs.getInt(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them phong.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Phong phong) {
        Connection con = ConnectDB.getConnection();
        if (con == null || phong == null) {
            return false;
        }

        String sql = "UPDATE Phong SET maLoaiPhong = ?, soPhong = ?, tang = ?, khuVuc = ?, "
                + "sucChuaChuan = ?, sucChuaToiDa = ?, trangThai = ? WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, phong.getMaLoaiPhong());
            stmt.setString(2, phong.getSoPhong());
            stmt.setString(3, phong.getTang());
            stmt.setString(4, phong.getKhuVuc());
            stmt.setInt(5, phong.getSucChuaChuan());
            stmt.setInt(6, phong.getSucChuaToiDa());
            stmt.setString(7, phong.getTrangThai());
            stmt.setInt(8, phong.getMaPhong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat phong co ma: " + phong.getMaPhong());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi xoa phong co ma: " + maPhong);
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSoPhongExists(String soPhong, Integer excludeMaPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null || soPhong == null || soPhong.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(1) FROM Phong WHERE soPhong = ?"
                + (excludeMaPhong != null && excludeMaPhong.intValue() > 0 ? " AND maPhong <> ?" : "");

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, soPhong.trim());
            if (excludeMaPhong != null && excludeMaPhong.intValue() > 0) {
                stmt.setInt(2, excludeMaPhong.intValue());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi kiem tra trung so phong: " + soPhong);
            e.printStackTrace();
        }
        return false;
    }

    public List<LoaiPhong> getAllLoaiPhong() {
        return new LoaiPhongDAO().getAll();
    }

    private Phong mapPhong(ResultSet rs) throws SQLException {
        return new Phong(
                rs.getInt("maPhong"),
                rs.getInt("maLoaiPhong"),
                rs.getString("soPhong"),
                rs.getString("tang"),
                rs.getString("khuVuc"),
                rs.getInt("sucChuaChuan"),
                rs.getInt("sucChuaToiDa"),
                rs.getString("trangThai"),
                rs.getString("tenLoaiPhong")
        );
    }
}
