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

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }

    public List<Phong> getAll() {
        clearLastError();
        List<Phong> dsPhong = new ArrayList<Phong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsPhong;
        }

        String sql = SELECT_BASE
                + " ORDER BY "
                + "TRY_CAST(REPLACE(REPLACE(p.tang, N'Tầng ', ''), N'Tang ', '') AS INT), "
                + "TRY_CAST(p.soPhong AS INT), p.soPhong ASC, p.maPhong ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsPhong.add(mapPhong(rs));
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return dsPhong;
    }

    public Phong findById(int maPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
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
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Phong> search(String keyword, String maLoaiPhongOrTenLoaiPhong, String trangThai) {
        clearLastError();
        List<Phong> dsPhong = new ArrayList<Phong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsPhong;
        }

        String tuKhoa = safeTrim(keyword);
        String loaiPhongFilter = safeTrim(maLoaiPhongOrTenLoaiPhong);
        String trangThaiFilter = safeTrim(trangThai);

        String sql = SELECT_BASE
                + " WHERE (? = '' OR p.soPhong LIKE ? OR CAST(p.maPhong AS NVARCHAR(20)) LIKE ? OR p.tang LIKE ? OR p.khuVuc LIKE ?)"
                + " AND (? = '' OR CAST(p.maLoaiPhong AS NVARCHAR(20)) = ? OR lp.tenLoaiPhong = ?)"
                + " AND (? = '' OR p.trangThai = ?)"
                + " ORDER BY "
                + "TRY_CAST(REPLACE(REPLACE(p.tang, N'Tầng ', ''), N'Tang ', '') AS INT), "
                + "TRY_CAST(p.soPhong AS INT), p.soPhong ASC, p.maPhong ASC";

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
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return dsPhong;
    }

    public boolean insert(Phong phong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || phong == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu phòng không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO Phong(maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, phong);
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
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Phong phong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || phong == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu phòng không hợp lệ.");
            return false;
        }

        String sql = "UPDATE Phong SET maLoaiPhong = ?, soPhong = ?, tang = ?, khuVuc = ?, "
                + "sucChuaChuan = ?, sucChuaToiDa = ?, trangThai = ? WHERE maPhong = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            fillStatement(stmt, phong);
            stmt.setInt(8, phong.getMaPhong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTrangThai(int maPhong, String trangThai) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "UPDATE Phong SET trangThai = ? WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, safeTrim(trangThai));
            stmt.setInt(2, maPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSoPhongExists(String soPhong, Integer excludeMaPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || isBlank(soPhong)) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Số phòng không hợp lệ.");
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
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // PHÒNG CÓ THỂ GÁN
    // =========================

    public List<Phong> findAssignableRooms() {
        return findAssignableRooms(null);
    }

    public List<Phong> findAssignableRooms(String maLoaiPhong) {
        clearLastError();
        List<Phong> dsPhong = new ArrayList<Phong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsPhong;
        }

        String loaiPhong = safeTrim(maLoaiPhong);

        String sql = SELECT_BASE
                + " WHERE p.trangThai IN (N'Trống', N'Dọn dẹp')"
                + " AND (? = '' OR CAST(p.maLoaiPhong AS NVARCHAR(20)) = ? OR lp.tenLoaiPhong = ?)"
                + " ORDER BY "
                + "TRY_CAST(REPLACE(REPLACE(p.tang, N'Tầng ', ''), N'Tang ', '') AS INT), "
                + "TRY_CAST(p.soPhong AS INT), p.soPhong ASC, p.maPhong ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, loaiPhong);
            stmt.setString(2, loaiPhong);
            stmt.setString(3, loaiPhong);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsPhong.add(mapPhong(rs));
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return dsPhong;
    }

    // Alias để tương thích với GUI đang gọi sai chính tả
    public List<Phong> findAssiDgnableRooms() {
        return findAssignableRooms();
    }

    public List<Phong> findAssiDgnableRooms(String maLoaiPhong) {
        return findAssignableRooms(maLoaiPhong);
    }

    // =========================
    // GÁN LOẠI PHÒNG
    // =========================

    public boolean updateLoaiPhongForRooms(List<Integer> roomIds, int maLoaiPhong) {
        return assignLoaiPhongForRooms(roomIds, maLoaiPhong);
    }

    public boolean assignLoaiPhong(int maPhong, int maLoaiPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "UPDATE Phong SET maLoaiPhong = ? WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiPhong);
            stmt.setInt(2, maPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean assignLoaiPhongForRooms(List<Integer> roomIds, int maLoaiPhong) {
        clearLastError();
        if (roomIds == null || roomIds.isEmpty()) {
            setLastError("Danh sách phòng cần gán đang rỗng.");
            return false;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "UPDATE Phong SET maLoaiPhong = ? WHERE maPhong = ?";
        try {
            con.setAutoCommit(false);

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                boolean hasBatch = false;
                for (Integer roomId : roomIds) {
                    if (roomId == null || roomId.intValue() <= 0) {
                        continue;
                    }
                    stmt.setInt(1, maLoaiPhong);
                    stmt.setInt(2, roomId.intValue());
                    stmt.addBatch();
                    hasBatch = true;
                }

                if (!hasBatch) {
                    con.rollback();
                    setLastError("Không có phòng hợp lệ để cập nhật.");
                    return false;
                }

                stmt.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public List<LoaiPhong> getAllLoaiPhong() {
        return new LoaiPhongDAO().getAll();
    }

    private void fillStatement(PreparedStatement stmt, Phong phong) throws SQLException {
        stmt.setInt(1, phong.getMaLoaiPhong());
        stmt.setString(2, phong.getSoPhong());
        stmt.setString(3, phong.getTang());
        stmt.setString(4, phong.getKhuVuc());
        stmt.setInt(5, phong.getSucChuaChuan());
        stmt.setInt(6, phong.getSucChuaToiDa());
        stmt.setString(7, phong.getTrangThai());
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

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}