package dao;

import db.ConnectDB;
import entity.LoaiPhong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LoaiPhongDAO {
    private static final String SELECT_BASE =
            "SELECT maLoaiPhong, tenLoaiPhong, khachToiDa, "
                    + "CAST(dienTich AS FLOAT) AS dienTich, loaiGiuong, "
                    + "CAST(giaThamChieu AS FLOAT) AS giaThamChieu, trangThai, moTa "
                    + "FROM LoaiPhong";

    public List<LoaiPhong> getAll() {
        List<LoaiPhong> dsLoaiPhong = new ArrayList<LoaiPhong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsLoaiPhong;
        }

        String sql = SELECT_BASE + " ORDER BY maLoaiPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsLoaiPhong.add(mapLoaiPhong(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach loai phong.");
            e.printStackTrace();
        }
        return dsLoaiPhong;
    }

    public boolean insert(LoaiPhong loaiPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null || loaiPhong == null) {
            return false;
        }

        String sql = "INSERT INTO LoaiPhong(tenLoaiPhong, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, loaiPhong.getTenLoaiPhong());
            stmt.setInt(2, loaiPhong.getKhachToiDa());
            stmt.setDouble(3, loaiPhong.getDienTich());
            stmt.setString(4, loaiPhong.getLoaiGiuong());
            stmt.setDouble(5, loaiPhong.getGiaThamChieu());
            stmt.setString(6, loaiPhong.getTrangThai());
            stmt.setString(7, loaiPhong.getMoTa());
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        loaiPhong.setMaLoaiPhong(rs.getInt(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them loai phong.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(LoaiPhong loaiPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null || loaiPhong == null) {
            return false;
        }

        String sql = "UPDATE LoaiPhong SET tenLoaiPhong = ?, khachToiDa = ?, dienTich = ?, "
                + "loaiGiuong = ?, giaThamChieu = ?, trangThai = ?, moTa = ? WHERE maLoaiPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, loaiPhong.getTenLoaiPhong());
            stmt.setInt(2, loaiPhong.getKhachToiDa());
            stmt.setDouble(3, loaiPhong.getDienTich());
            stmt.setString(4, loaiPhong.getLoaiGiuong());
            stmt.setDouble(5, loaiPhong.getGiaThamChieu());
            stmt.setString(6, loaiPhong.getTrangThai());
            stmt.setString(7, loaiPhong.getMoTa());
            stmt.setInt(8, loaiPhong.getMaLoaiPhong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat loai phong co ma: " + loaiPhong.getMaLoaiPhong());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maLoaiPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        try {
            con.setAutoCommit(false);
            try (PreparedStatement deleteLienKet = con.prepareStatement("DELETE FROM LoaiPhongTienNghi WHERE maLoaiPhong = ?");
                 PreparedStatement deleteLoaiPhong = con.prepareStatement("DELETE FROM LoaiPhong WHERE maLoaiPhong = ?")) {
                deleteLienKet.setInt(1, maLoaiPhong);
                deleteLienKet.executeUpdate();

                deleteLoaiPhong.setInt(1, maLoaiPhong);
                boolean deleted = deleteLoaiPhong.executeUpdate() > 0;
                con.commit();
                return deleted;
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Loi xoa loai phong co ma: " + maLoaiPhong);
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Loi giao dich xoa loai phong co ma: " + maLoaiPhong);
            e.printStackTrace();
            return false;
        }
    }

    public LoaiPhong findById(int maLoaiPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE maLoaiPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapLoaiPhong(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim loai phong theo ma: " + maLoaiPhong);
            e.printStackTrace();
        }
        return null;
    }

    public List<LoaiPhong> search(String keyword, String trangThai) {
        List<LoaiPhong> dsLoaiPhong = new ArrayList<LoaiPhong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsLoaiPhong;
        }

        String tuKhoa = keyword == null ? "" : keyword.trim();
        String trangThaiSearch = trangThai == null ? "" : trangThai.trim();
        String sql = SELECT_BASE
                + " WHERE (? = '' OR CAST(maLoaiPhong AS NVARCHAR(20)) LIKE ? OR tenLoaiPhong LIKE ?)"
                + " AND (? = '' OR trangThai = ?)"
                + " ORDER BY maLoaiPhong DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tuKhoa);
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, "%" + tuKhoa + "%");
            stmt.setString(4, trangThaiSearch);
            stmt.setString(5, trangThaiSearch);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsLoaiPhong.add(mapLoaiPhong(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim kiem loai phong.");
            e.printStackTrace();
        }
        return dsLoaiPhong;
    }

    public List<Integer> getTienNghiIdsByLoaiPhong(int maLoaiPhong) {
        List<Integer> dsMaTienNghi = new ArrayList<Integer>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsMaTienNghi;
        }

        String sql = "SELECT maTienNghi FROM LoaiPhongTienNghi WHERE maLoaiPhong = ? ORDER BY maTienNghi";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsMaTienNghi.add(rs.getInt("maTienNghi"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay danh sach tien nghi cua loai phong: " + maLoaiPhong);
            e.printStackTrace();
        }
        return dsMaTienNghi;
    }

    public boolean updateTienNghiLoaiPhong(int maLoaiPhong, List<Integer> dsMaTienNghi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        Set<Integer> uniqueIds = new LinkedHashSet<Integer>();
        if (dsMaTienNghi != null) {
            uniqueIds.addAll(dsMaTienNghi);
        }

        try {
            con.setAutoCommit(false);
            try (PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM LoaiPhongTienNghi WHERE maLoaiPhong = ?");
                 PreparedStatement insertStmt = con.prepareStatement("INSERT INTO LoaiPhongTienNghi(maLoaiPhong, maTienNghi) VALUES (?, ?)")) {
                deleteStmt.setInt(1, maLoaiPhong);
                deleteStmt.executeUpdate();

                for (Integer maTienNghi : uniqueIds) {
                    if (maTienNghi == null || maTienNghi.intValue() <= 0) {
                        continue;
                    }
                    insertStmt.setInt(1, maLoaiPhong);
                    insertStmt.setInt(2, maTienNghi.intValue());
                    insertStmt.addBatch();
                }
                if (!uniqueIds.isEmpty()) {
                    insertStmt.executeBatch();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Loi cap nhat tien nghi cho loai phong: " + maLoaiPhong);
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Loi giao dich cap nhat tien nghi cho loai phong: " + maLoaiPhong);
            e.printStackTrace();
            return false;
        }
    }

    private LoaiPhong mapLoaiPhong(ResultSet rs) throws SQLException {
        return new LoaiPhong(
                rs.getInt("maLoaiPhong"),
                rs.getString("tenLoaiPhong"),
                rs.getInt("khachToiDa"),
                rs.getDouble("dienTich"),
                rs.getString("loaiGiuong"),
                rs.getDouble("giaThamChieu"),
                rs.getString("trangThai"),
                rs.getString("moTa")
        );
    }
}
