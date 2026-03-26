package dao;

import db.ConnectDB;
import entity.SuDungDichVu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SuDungDichVuDAO {
    private static final String SELECT_BASE =
            "SELECT sdv.maSuDung, sdv.maLuuTru, sdv.maDichVu, sdv.soLuong, "
                    + "CAST(sdv.donGia AS FLOAT) AS donGia, CAST(sdv.thanhTien AS FLOAT) AS thanhTien, "
                    + "dv.tenDichVu, dv.donVi "
                    + "FROM SuDungDichVu sdv "
                    + "INNER JOIN DichVu dv ON dv.maDichVu = sdv.maDichVu";

    public List<SuDungDichVu> getAll() {
        List<SuDungDichVu> dsSuDung = new ArrayList<SuDungDichVu>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsSuDung;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " ORDER BY sdv.maSuDung DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsSuDung.add(mapSuDungDichVu(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach su dung dich vu.");
            e.printStackTrace();
        }
        return dsSuDung;
    }

    public SuDungDichVu findById(int maSuDung) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " WHERE sdv.maSuDung = ?")) {
            stmt.setInt(1, maSuDung);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSuDungDichVu(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim su dung dich vu theo ma: " + maSuDung);
            e.printStackTrace();
        }
        return null;
    }

    public List<SuDungDichVu> getByMaLuuTru(int maLuuTru) {
        List<SuDungDichVu> dsSuDung = new ArrayList<SuDungDichVu>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsSuDung;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " WHERE sdv.maLuuTru = ? ORDER BY sdv.maSuDung DESC")) {
            stmt.setInt(1, maLuuTru);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsSuDung.add(mapSuDungDichVu(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay su dung dich vu theo ma luu tru: " + maLuuTru);
            e.printStackTrace();
        }
        return dsSuDung;
    }

    public List<SuDungDichVu> getByMaDichVu(int maDichVu) {
        List<SuDungDichVu> dsSuDung = new ArrayList<SuDungDichVu>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsSuDung;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " WHERE sdv.maDichVu = ? ORDER BY sdv.maSuDung DESC")) {
            stmt.setInt(1, maDichVu);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsSuDung.add(mapSuDungDichVu(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay su dung dich vu theo ma dich vu: " + maDichVu);
            e.printStackTrace();
        }
        return dsSuDung;
    }

    public boolean insertSuDungDichVu(SuDungDichVu suDungDichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null || suDungDichVu == null) {
            return false;
        }

        String sql = "INSERT INTO SuDungDichVu(maLuuTru, maDichVu, soLuong, donGia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, suDungDichVu.getMaLuuTru());
            stmt.setInt(2, suDungDichVu.getMaDichVu());
            stmt.setInt(3, suDungDichVu.getSoLuong());
            stmt.setDouble(4, suDungDichVu.getDonGia());
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        suDungDichVu.setMaSuDung(rs.getInt(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them su dung dich vu.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSuDungDichVu(SuDungDichVu suDungDichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null || suDungDichVu == null) {
            return false;
        }

        String sql = "UPDATE SuDungDichVu SET maLuuTru = ?, maDichVu = ?, soLuong = ?, donGia = ? WHERE maSuDung = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, suDungDichVu.getMaLuuTru());
            stmt.setInt(2, suDungDichVu.getMaDichVu());
            stmt.setInt(3, suDungDichVu.getSoLuong());
            stmt.setDouble(4, suDungDichVu.getDonGia());
            stmt.setInt(5, suDungDichVu.getMaSuDung());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat su dung dich vu co ma: " + suDungDichVu.getMaSuDung());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSuDungDichVu(int maSuDung) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "DELETE FROM SuDungDichVu WHERE maSuDung = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maSuDung);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi xoa su dung dich vu co ma: " + maSuDung);
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> getAvailableMaLuuTru() {
        List<Integer> dsMaLuuTru = new ArrayList<Integer>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsMaLuuTru;
        }

        String sql = "SELECT maLuuTru FROM LuuTru ORDER BY maLuuTru DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsMaLuuTru.add(rs.getInt("maLuuTru"));
            }
        } catch (SQLException e) {
            System.out.println("Loi lay danh sach ma luu tru.");
            e.printStackTrace();
        }
        return dsMaLuuTru;
    }

    private SuDungDichVu mapSuDungDichVu(ResultSet rs) throws SQLException {
        SuDungDichVu suDungDichVu = new SuDungDichVu(
                rs.getInt("maSuDung"),
                rs.getInt("maLuuTru"),
                rs.getInt("maDichVu"),
                rs.getInt("soLuong"),
                rs.getDouble("donGia")
        );
        suDungDichVu.setThanhTien(rs.getDouble("thanhTien"));
        suDungDichVu.setTenDichVu(rs.getString("tenDichVu"));
        suDungDichVu.setDonVi(rs.getString("donVi"));
        return suDungDichVu;
    }
}
