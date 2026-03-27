package dao;

import db.ConnectDB;
import entity.ChiTietBangGia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChiTietBangGiaDAO {
    private static final String SELECT_BASE =
            "SELECT maChiTietBangGia, maBangGia, loaiNgay, khungGio, "
                    + "CAST(giaTheoGio AS FLOAT) AS giaTheoGio, "
                    + "CAST(giaQuaDem AS FLOAT) AS giaQuaDem, "
                    + "CAST(giaTheoNgay AS FLOAT) AS giaTheoNgay, "
                    + "CAST(giaCuoiTuan AS FLOAT) AS giaCuoiTuan, "
                    + "CAST(giaLe AS FLOAT) AS giaLe, "
                    + "CAST(phuThu AS FLOAT) AS phuThu "
                    + "FROM ChiTietBangGia";

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<ChiTietBangGia> getByMaBangGia(int maBangGia) {
        clearLastError();
        List<ChiTietBangGia> dsChiTiet = new ArrayList<ChiTietBangGia>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Khong the ket noi co so du lieu.");
            return dsChiTiet;
        }

        String sql = SELECT_BASE + " WHERE maBangGia = ? ORDER BY maChiTietBangGia ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsChiTiet.add(mapChiTietBangGia(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay chi tiet bang gia theo ma bang gia: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return dsChiTiet;
    }

    public ChiTietBangGia findById(int maChiTietBangGia) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Khong the ket noi co so du lieu.");
            return null;
        }

        String sql = SELECT_BASE + " WHERE maChiTietBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maChiTietBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapChiTietBangGia(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim chi tiet bang gia theo ma: " + maChiTietBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return null;
    }

    public boolean insert(ChiTietBangGia chiTietBangGia) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || chiTietBangGia == null) {
            setLastError(con == null ? "Khong the ket noi co so du lieu." : "Du lieu chi tiet bang gia khong hop le.");
            return false;
        }
        if (!validateChiTietBangGia(chiTietBangGia, false)) {
            return false;
        }

        String sql = "INSERT INTO ChiTietBangGia(maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, chiTietBangGia, false);
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        chiTietBangGia.setMaChiTietBangGia(rs.getInt(1));
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them chi tiet bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public boolean update(ChiTietBangGia chiTietBangGia) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || chiTietBangGia == null) {
            setLastError(con == null ? "Khong the ket noi co so du lieu." : "Du lieu chi tiet bang gia khong hop le.");
            return false;
        }
        if (!validateChiTietBangGia(chiTietBangGia, true)) {
            return false;
        }

        String sql = "UPDATE ChiTietBangGia SET maBangGia = ?, loaiNgay = ?, khungGio = ?, giaTheoGio = ?, "
                + "giaQuaDem = ?, giaTheoNgay = ?, giaCuoiTuan = ?, giaLe = ?, phuThu = ? "
                + "WHERE maChiTietBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            fillStatement(stmt, chiTietBangGia, true);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat chi tiet bang gia co ma: " + chiTietBangGia.getMaChiTietBangGia());
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public boolean delete(int maChiTietBangGia) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Khong the ket noi co so du lieu.");
            return false;
        }

        String sql = "DELETE FROM ChiTietBangGia WHERE maChiTietBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maChiTietBangGia);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi xoa chi tiet bang gia co ma: " + maChiTietBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    private void fillStatement(PreparedStatement stmt, ChiTietBangGia chiTietBangGia, boolean includeId) throws SQLException {
        stmt.setInt(1, chiTietBangGia.getMaBangGia());
        stmt.setString(2, chiTietBangGia.getLoaiNgay());
        stmt.setString(3, chiTietBangGia.getKhungGio());
        stmt.setDouble(4, chiTietBangGia.getGiaTheoGio());
        stmt.setDouble(5, chiTietBangGia.getGiaQuaDem());
        stmt.setDouble(6, chiTietBangGia.getGiaTheoNgay());
        stmt.setDouble(7, chiTietBangGia.getGiaCuoiTuan());
        stmt.setDouble(8, chiTietBangGia.getGiaLe());
        stmt.setDouble(9, chiTietBangGia.getPhuThu());
        if (includeId) {
            stmt.setInt(10, chiTietBangGia.getMaChiTietBangGia());
        }
    }

    private ChiTietBangGia mapChiTietBangGia(ResultSet rs) throws SQLException {
        return new ChiTietBangGia(
                rs.getInt("maChiTietBangGia"),
                rs.getInt("maBangGia"),
                rs.getString("loaiNgay"),
                rs.getString("khungGio"),
                rs.getDouble("giaTheoGio"),
                rs.getDouble("giaQuaDem"),
                rs.getDouble("giaTheoNgay"),
                rs.getDouble("giaCuoiTuan"),
                rs.getDouble("giaLe"),
                rs.getDouble("phuThu")
        );
    }

    private boolean validateChiTietBangGia(ChiTietBangGia chiTietBangGia, boolean updating) {
        if (updating && chiTietBangGia.getMaChiTietBangGia() <= 0) {
            setLastError("Ma chi tiet bang gia khong hop le.");
            return false;
        }
        if (chiTietBangGia.getMaBangGia() <= 0) {
            setLastError("Ma bang gia khong hop le.");
            return false;
        }
        if (chiTietBangGia.getLoaiNgay() == null || chiTietBangGia.getLoaiNgay().trim().isEmpty()) {
            setLastError("Loai ngay khong duoc rong.");
            return false;
        }
        if (chiTietBangGia.getKhungGio() == null || chiTietBangGia.getKhungGio().trim().isEmpty()) {
            setLastError("Khung gio khong duoc rong.");
            return false;
        }
        if (!isNonNegative(chiTietBangGia.getGiaTheoGio())
                || !isNonNegative(chiTietBangGia.getGiaQuaDem())
                || !isNonNegative(chiTietBangGia.getGiaTheoNgay())
                || !isNonNegative(chiTietBangGia.getGiaCuoiTuan())
                || !isNonNegative(chiTietBangGia.getGiaLe())
                || !isNonNegative(chiTietBangGia.getPhuThu())) {
            setLastError("Gia va phu thu phai lon hon hoac bang 0.");
            return false;
        }
        return true;
    }

    private boolean isNonNegative(double value) {
        return value >= 0;
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
