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

/**
 * DAO (Data Access Object) xử lý các thao tác CRUD với bảng DichVu trong cơ sở dữ liệu.
 */
public class DichVuDAO {

    // Câu truy vấn SELECT dùng chung, tránh lặp lại ở nhiều phương thức
    private static final String SELECT_BASE =
            "SELECT maDichVu, tenDichVu, CAST(donGia AS FLOAT) AS donGia, donVi FROM DichVu";

    /**
     * Lấy toàn bộ danh sách dịch vụ, sắp xếp theo mã giảm dần.
     *
     * @return Danh sách tất cả dịch vụ, hoặc danh sách rỗng nếu lỗi kết nối.
     */
    public List<DichVu> getAll() {
        List<DichVu> dsDichVu = new ArrayList<DichVu>();

        // Lấy kết nối từ pool, trả về danh sách rỗng nếu không kết nối được
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsDichVu;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " ORDER BY maDichVu DESC");
             ResultSet rs = stmt.executeQuery()) {

            // Duyệt qua từng bản ghi và ánh xạ thành đối tượng DichVu
            while (rs.next()) {
                dsDichVu.add(mapDichVu(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach dich vu.");
            e.printStackTrace();
        }
        return dsDichVu;
    }

    /**
     * Tìm một dịch vụ theo mã dịch vụ.
     *
     * @param maDichVu Mã dịch vụ cần tìm.
     * @return Đối tượng DichVu nếu tìm thấy, null nếu không có hoặc lỗi.
     */
    public DichVu findById(int maDichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_BASE + " WHERE maDichVu = ?")) {
            stmt.setInt(1, maDichVu);
            try (ResultSet rs = stmt.executeQuery()) {
                // Chỉ lấy bản ghi đầu tiên vì maDichVu là khóa chính
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

    /**
     * Tìm kiếm dịch vụ theo từ khóa trên các cột: maDichVu, tenDichVu, donVi.
     *
     * @param keyword Từ khóa tìm kiếm. Nếu null hoặc rỗng, trả về toàn bộ danh sách.
     * @return Danh sách dịch vụ phù hợp với từ khóa.
     */
    public List<DichVu> search(String keyword) {
        List<DichVu> dsDichVu = new ArrayList<DichVu>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsDichVu;
        }

        // Chuẩn hóa từ khóa: null → chuỗi rỗng, xóa khoảng trắng thừa
        String tuKhoa = keyword == null ? "" : keyword.trim();

        // Nếu từ khóa rỗng (? = ''), điều kiện WHERE luôn đúng → trả về tất cả
        String sql = SELECT_BASE
                + " WHERE (? = '' OR CAST(maDichVu AS NVARCHAR(20)) LIKE ? OR tenDichVu LIKE ? OR donVi LIKE ?)"
                + " ORDER BY maDichVu DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            // Tham số 1: kiểm tra từ khóa có rỗng không
            stmt.setString(1, tuKhoa);
            // Tham số 2–4: tìm kiếm LIKE trên từng cột
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

    /**
     * Thêm một dịch vụ mới vào cơ sở dữ liệu.
     * Sau khi thêm thành công, maDichVu được sinh tự động sẽ được gán lại vào đối tượng.
     *
     * @param dichVu Đối tượng DichVu cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insert(DichVu dichVu) {
        Connection con = ConnectDB.getConnection();
        if (con == null || dichVu == null) {
            return false;
        }

        // maDichVu là IDENTITY (tự tăng), không cần truyền vào câu INSERT
        String sql = "INSERT INTO DichVu(tenDichVu, donGia, donVi) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dichVu.getTenDichVu());
            stmt.setDouble(2, dichVu.getDonGia());
            stmt.setString(3, dichVu.getDonVi());
            boolean inserted = stmt.executeUpdate() > 0;

            if (inserted) {
                // Lấy mã tự sinh và gán lại vào entity để dùng tiếp ở tầng trên
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

    /**
     * Cập nhật thông tin dịch vụ đã tồn tại trong cơ sở dữ liệu.
     *
     * @param dichVu Đối tượng DichVu với thông tin mới (phải có maDichVu hợp lệ).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
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
            // Điều kiện WHERE dựa trên khóa chính để đảm bảo chỉ cập nhật đúng bản ghi
            stmt.setInt(4, dichVu.getMaDichVu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat dich vu co ma: " + dichVu.getMaDichVu());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa một dịch vụ khỏi cơ sở dữ liệu theo mã dịch vụ.
     *
     * @param maDichVu Mã dịch vụ cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
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

    /**
     * Ánh xạ một hàng dữ liệu từ ResultSet thành đối tượng DichVu.
     * Phương thức dùng chung cho tất cả các truy vấn SELECT trong class này.
     *
     * @param rs ResultSet đang trỏ đến hàng cần đọc.
     * @return Đối tượng DichVu tương ứng.
     * @throws SQLException Nếu tên cột không tồn tại hoặc lỗi đọc dữ liệu.
     */
    private DichVu mapDichVu(ResultSet rs) throws SQLException {
        return new DichVu(
                rs.getInt("maDichVu"),
                rs.getString("tenDichVu"),
                rs.getDouble("donGia"),
                rs.getString("donVi")
        );
    }
}