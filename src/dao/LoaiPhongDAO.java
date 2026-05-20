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

/**
 * DAO xử lý các thao tác dữ liệu liên quan đến loại phòng.
 *
 * Lớp này phụ trách:
 * - Lấy danh sách loại phòng.
 * - Thêm, cập nhật, xóa loại phòng.
 * - Tìm kiếm loại phòng theo từ khóa và trạng thái.
 * - Quản lý danh sách tiện nghi gắn với loại phòng.
 * - Đồng bộ trạng thái phòng khi trạng thái loại phòng thay đổi.
 */
public class LoaiPhongDAO {

    /**
     * Câu SELECT cơ sở dùng chung cho các truy vấn loại phòng.
     *
     * Các cột số thực được CAST sang FLOAT để đảm bảo dữ liệu đọc ra
     * phù hợp với kiểu double trong entity LoaiPhong.
     */
    private static final String SELECT_BASE =
            "SELECT maLoaiPhong, tenLoaiPhong, khachToiDa, "
                    + "CAST(dienTich AS FLOAT) AS dienTich, loaiGiuong, "
                    + "CAST(giaThamChieu AS FLOAT) AS giaThamChieu, trangThai, moTa "
                    + "FROM LoaiPhong";

    // Trạng thái của loại phòng.
    private static final String ROOM_TYPE_STATUS_ACTIVE = "Đang áp dụng";
    private static final String ROOM_TYPE_STATUS_INACTIVE = "Ngừng áp dụng";

    // Trạng thái phòng dùng khi đồng bộ theo trạng thái loại phòng.
    private static final String ROOM_STATUS_ACTIVE = "Hoạt động";
    private static final String ROOM_STATUS_INACTIVE = "Không hoạt động";
    private static final String ROOM_STATUS_MAINTENANCE = "Bảo trì";

    /**
     * Lưu thông báo lỗi gần nhất để lớp gọi có thể lấy ra hiển thị.
     */
    private String lastErrorMessage = "";

    /**
     * Lấy thông báo lỗi gần nhất phát sinh trong DAO.
     *
     * @return nội dung lỗi gần nhất, rỗng nếu chưa có lỗi.
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Xóa thông báo lỗi gần nhất trước khi thực hiện thao tác mới.
     */
    private void clearLastError() {
        lastErrorMessage = "";
    }

    /**
     * Lưu thông báo lỗi gần nhất.
     *
     * @param message nội dung lỗi cần lưu.
     */
    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }

    /**
     * Lấy toàn bộ danh sách loại phòng.
     *
     * @return danh sách loại phòng, rỗng nếu không có dữ liệu hoặc có lỗi.
     */
    public List<LoaiPhong> getAll() {
        clearLastError();
        List<LoaiPhong> dsLoaiPhong = new ArrayList<LoaiPhong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsLoaiPhong;
        }

        String sql = SELECT_BASE + " ORDER BY maLoaiPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsLoaiPhong.add(mapLoaiPhong(rs));
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return dsLoaiPhong;
    }

    /**
     * Thêm mới một loại phòng.
     *
     * Nếu thêm thành công, mã loại phòng tự sinh sẽ được gán lại vào đối tượng loaiPhong.
     *
     * @param loaiPhong thông tin loại phòng cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insert(LoaiPhong loaiPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || loaiPhong == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu loại phòng không hợp lệ.");
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
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin loại phòng.
     *
     * Sau khi cập nhật loại phòng thành công, method sẽ đồng bộ trạng thái
     * của các phòng thuộc loại phòng đó theo trạng thái loại phòng.
     *
     * @param loaiPhong thông tin loại phòng cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean update(LoaiPhong loaiPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || loaiPhong == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu loại phòng không hợp lệ.");
            return false;
        }

        String sql = "UPDATE LoaiPhong SET tenLoaiPhong = ?, khachToiDa = ?, dienTich = ?, "
                + "loaiGiuong = ?, giaThamChieu = ?, trangThai = ?, moTa = ? WHERE maLoaiPhong = ?";
        try {
            con.setAutoCommit(false);
            boolean updated;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, loaiPhong.getTenLoaiPhong());
                stmt.setInt(2, loaiPhong.getKhachToiDa());
                stmt.setDouble(3, loaiPhong.getDienTich());
                stmt.setString(4, loaiPhong.getLoaiGiuong());
                stmt.setDouble(5, loaiPhong.getGiaThamChieu());
                stmt.setString(6, loaiPhong.getTrangThai());
                stmt.setString(7, loaiPhong.getMoTa());
                stmt.setInt(8, loaiPhong.getMaLoaiPhong());
                updated = stmt.executeUpdate() > 0;
            }

            if (updated) {
                syncRoomsByRoomTypeStatus(con, loaiPhong.getMaLoaiPhong(), loaiPhong.getTrangThai());
            }
            con.commit();
            return updated;
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

    /**
     * Xóa loại phòng theo mã loại phòng.
     *
     * Chỉ cho phép xóa khi không còn phòng nào đang liên kết với loại phòng này.
     * Trước khi xóa loại phòng, method xóa các liên kết tiện nghi của loại phòng.
     *
     * @param maLoaiPhong mã loại phòng cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean delete(int maLoaiPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        try {
            if (existsPhongByLoaiPhong(con, maLoaiPhong)) {
                setLastError("Không thể xóa loại phòng vì vẫn còn phòng đang liên kết.");
                return false;
            }

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
                setLastError(e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tìm loại phòng theo mã loại phòng.
     *
     * @param maLoaiPhong mã loại phòng cần tìm.
     * @return đối tượng LoaiPhong nếu tìm thấy, ngược lại trả về null.
     */
    public LoaiPhong findById(int maLoaiPhong) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
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
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm kiếm loại phòng theo từ khóa và trạng thái.
     *
     * Từ khóa được dùng để tìm theo mã loại phòng hoặc tên loại phòng.
     * Nếu từ khóa hoặc trạng thái rỗng, điều kiện tương ứng sẽ được bỏ qua.
     *
     * @param keyword từ khóa tìm kiếm.
     * @param trangThai trạng thái loại phòng cần lọc.
     * @return danh sách loại phòng phù hợp.
     */
    public List<LoaiPhong> search(String keyword, String trangThai) {
        clearLastError();
        List<LoaiPhong> dsLoaiPhong = new ArrayList<LoaiPhong>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
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
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return dsLoaiPhong;
    }

    /**
     * Lấy danh sách mã tiện nghi đang gắn với một loại phòng.
     *
     * @param maLoaiPhong mã loại phòng cần lấy danh sách tiện nghi.
     * @return danh sách mã tiện nghi.
     */
    public List<Integer> getTienNghiIdsByLoaiPhong(int maLoaiPhong) {
        clearLastError();
        List<Integer> dsMaTienNghi = new ArrayList<Integer>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
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
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return dsMaTienNghi;
    }

    /**
     * Cập nhật danh sách tiện nghi của một loại phòng.
     *
     * Method sẽ xóa toàn bộ liên kết tiện nghi cũ,
     * sau đó thêm lại danh sách tiện nghi mới không trùng mã.
     *
     * @param maLoaiPhong mã loại phòng cần cập nhật tiện nghi.
     * @param dsMaTienNghi danh sách mã tiện nghi mới.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateTienNghiLoaiPhong(int maLoaiPhong, List<Integer> dsMaTienNghi) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
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
                setLastError(e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra có phòng nào đang liên kết với loại phòng hay không.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maLoaiPhong mã loại phòng cần kiểm tra.
     * @return true nếu còn phòng liên kết với loại phòng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private boolean existsPhongByLoaiPhong(Connection con, int maLoaiPhong) throws SQLException {
        String sql = "SELECT COUNT(1) FROM Phong WHERE maLoaiPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Đồng bộ trạng thái phòng theo trạng thái của loại phòng.
     *
     * Nếu loại phòng ngừng áp dụng, toàn bộ phòng thuộc loại này được chuyển sang không hoạt động.
     * Nếu loại phòng đang áp dụng, các phòng đang không hoạt động được chuyển lại sang hoạt động.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param maLoaiPhong mã loại phòng cần đồng bộ.
     * @param trangThaiLoaiPhong trạng thái hiện tại của loại phòng.
     * @throws SQLException nếu xảy ra lỗi truy vấn cơ sở dữ liệu.
     */
    private void syncRoomsByRoomTypeStatus(Connection con, int maLoaiPhong, String trangThaiLoaiPhong) throws SQLException {
        if (ROOM_TYPE_STATUS_INACTIVE.equalsIgnoreCase(trangThaiLoaiPhong)) {
            String sql = "UPDATE Phong SET trangThai = ? WHERE maLoaiPhong = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, ROOM_STATUS_INACTIVE);
                stmt.setInt(2, maLoaiPhong);
                stmt.executeUpdate();
            }
            return;
        }

        if (ROOM_TYPE_STATUS_ACTIVE.equalsIgnoreCase(trangThaiLoaiPhong)) {
            String sql = "UPDATE Phong SET trangThai = ? WHERE maLoaiPhong = ? AND trangThai = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, ROOM_STATUS_ACTIVE);
                stmt.setInt(2, maLoaiPhong);
                stmt.setString(3, ROOM_STATUS_INACTIVE);
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Chuyển dữ liệu từ ResultSet thành đối tượng LoaiPhong.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu loại phòng.
     * @return đối tượng LoaiPhong sau khi ánh xạ.
     * @throws SQLException nếu xảy ra lỗi khi đọc dữ liệu từ ResultSet.
     */
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