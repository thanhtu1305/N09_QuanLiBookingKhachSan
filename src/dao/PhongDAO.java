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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PhongDAO {
    private static final String SELECT_BASE =
            "SELECT p.maPhong, p.maLoaiPhong, p.soPhong, p.tang, p.khuVuc, "
                    + "p.sucChuaChuan, p.sucChuaToiDa, p.trangThai, lp.tenLoaiPhong "
                    + "FROM Phong p "
                    + "LEFT JOIN LoaiPhong lp ON p.maLoaiPhong = lp.maLoaiPhong";

    private static final String ROOM_STATUS_ACTIVE = "Hoạt động";
    private static final String ROOM_STATUS_INACTIVE = "Không hoạt động";
    private static final String ROOM_STATUS_MAINTENANCE = "Bảo trì";
    private static final String ROOM_TYPE_STATUS_ACTIVE = "Đang áp dụng";
    private static final String ROOM_TYPE_STATUS_INACTIVE = "Ngừng áp dụng";

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
                syncLoaiPhongStatusByRooms(con, phong.getMaLoaiPhong());
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

        Integer oldLoaiPhong = findLoaiPhongIdByRoom(con, phong.getMaPhong());
        String sql = "UPDATE Phong SET maLoaiPhong = ?, soPhong = ?, tang = ?, khuVuc = ?, "
                + "sucChuaChuan = ?, sucChuaToiDa = ?, trangThai = ? WHERE maPhong = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            fillStatement(stmt, phong);
            stmt.setInt(8, phong.getMaPhong());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                syncLoaiPhongStatusByRooms(con, phong.getMaLoaiPhong());
                if (oldLoaiPhong != null && oldLoaiPhong.intValue() != phong.getMaLoaiPhong()) {
                    syncLoaiPhongStatusByRooms(con, oldLoaiPhong.intValue());
                }
            }
            return updated;
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

        Integer maLoaiPhong = findLoaiPhongIdByRoom(con, maPhong);
        String sql = "UPDATE Phong SET trangThai = ? WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, safeTrim(trangThai));
            stmt.setInt(2, maPhong);
            boolean updated = stmt.executeUpdate() > 0;
            if (updated && maLoaiPhong != null) {
                syncLoaiPhongStatusByRooms(con, maLoaiPhong.intValue());
            }
            return updated;
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

        Integer maLoaiPhong = findLoaiPhongIdByRoom(con, maPhong);
        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted && maLoaiPhong != null) {
                syncLoaiPhongStatusByRooms(con, maLoaiPhong.intValue());
            }
            return deleted;
        } catch (SQLException e) {
            setLastError("Không thể xóa phòng. Phòng này có thể đang được sử dụng hoặc đã phát sinh dữ liệu liên quan.");
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
                + " WHERE p.trangThai = ?"
                + " AND (? = '' OR CAST(p.maLoaiPhong AS NVARCHAR(20)) = ? OR lp.tenLoaiPhong = ?)"
                + " ORDER BY "
                + "TRY_CAST(REPLACE(REPLACE(p.tang, N'Tầng ', ''), N'Tang ', '') AS INT), "
                + "TRY_CAST(p.soPhong AS INT), p.soPhong ASC, p.maPhong ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, ROOM_STATUS_ACTIVE);
            stmt.setString(2, loaiPhong);
            stmt.setString(3, loaiPhong);
            stmt.setString(4, loaiPhong);

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

    public List<Phong> findAssiDgnableRooms() {
        return findAssignableRooms();
    }

    public List<Phong> findAssiDgnableRooms(String maLoaiPhong) {
        return findAssignableRooms(maLoaiPhong);
    }

    public boolean updateLoaiPhongForRooms(List<Integer> roomIds, int maLoaiPhong) {
        return assignLoaiPhongForRooms(roomIds, maLoaiPhong);
    }

    public boolean assignLoaiPhong(int maPhong, int maLoaiPhong) {
        List<Integer> ids = new ArrayList<Integer>();
        ids.add(Integer.valueOf(maPhong));
        return assignLoaiPhongForRooms(ids, maLoaiPhong);
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

        Set<Integer> oldLoaiPhongIds = findDistinctLoaiPhongIdsByRooms(con, roomIds);
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

            syncLoaiPhongStatusByRooms(con, maLoaiPhong);
            for (Integer oldId : oldLoaiPhongIds) {
                if (oldId != null && oldId.intValue() != maLoaiPhong) {
                    syncLoaiPhongStatusByRooms(con, oldId.intValue());
                }
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

    private Integer findLoaiPhongIdByRoom(Connection con, int maPhong) {
        String sql = "SELECT maLoaiPhong FROM Phong WHERE maPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Integer.valueOf(rs.getInt(1));
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private Set<Integer> findDistinctLoaiPhongIdsByRooms(Connection con, List<Integer> roomIds) {
        Set<Integer> ids = new LinkedHashSet<Integer>();
        for (Integer roomId : roomIds) {
            if (roomId == null || roomId.intValue() <= 0) {
                continue;
            }
            Integer maLoaiPhong = findLoaiPhongIdByRoom(con, roomId.intValue());
            if (maLoaiPhong != null && maLoaiPhong.intValue() > 0) {
                ids.add(maLoaiPhong);
            }
        }
        return ids;
    }

    private void syncLoaiPhongStatusByRooms(Connection con, int maLoaiPhong) throws SQLException {
        String countSql = "SELECT COUNT(1) AS tongPhong, "
                + "SUM(CASE WHEN trangThai NOT IN (?, ?) THEN 1 ELSE 0 END) AS tongHoatDong "
                + "FROM Phong WHERE maLoaiPhong = ?";

        int tongPhong = 0;
        int tongHoatDong = 0;
        try (PreparedStatement stmt = con.prepareStatement(countSql)) {
            stmt.setString(1, ROOM_STATUS_INACTIVE);
            stmt.setString(2, ROOM_STATUS_MAINTENANCE);
            stmt.setInt(3, maLoaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    tongPhong = rs.getInt("tongPhong");
                    tongHoatDong = rs.getInt("tongHoatDong");
                }
            }
        }

        if (tongPhong <= 0) {
            return;
        }

        String trangThaiLoaiPhong = tongHoatDong > 0 ? ROOM_TYPE_STATUS_ACTIVE : ROOM_TYPE_STATUS_INACTIVE;
        try (PreparedStatement stmt = con.prepareStatement("UPDATE LoaiPhong SET trangThai = ? WHERE maLoaiPhong = ?")) {
            stmt.setString(1, trangThaiLoaiPhong);
            stmt.setInt(2, maLoaiPhong);
            stmt.executeUpdate();
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
