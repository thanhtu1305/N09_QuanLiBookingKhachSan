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

/**
 * DAO xử lý các thao tác dữ liệu cho chi tiết bảng giá.
 *
 * Lớp này phụ trách:
 * - Lấy danh sách chi tiết bảng giá theo mã bảng giá.
 * - Tìm chi tiết bảng giá theo mã chi tiết.
 * - Thêm, cập nhật, xóa chi tiết bảng giá.
 * - Kiểm tra dữ liệu chi tiết bảng giá trước khi lưu.
 */
public class ChiTietBangGiaDAO {
    /**
     * Câu SELECT cơ sở dùng chung cho các truy vấn chi tiết bảng giá.
     *
     * Các cột giá được CAST sang FLOAT để đảm bảo kiểu dữ liệu đọc ra phù hợp
     * với các thuộc tính double trong entity ChiTietBangGia.
     */
    private static final String SELECT_BASE =
            "SELECT maChiTietBangGia, maBangGia, loaiNgay, khungGio, "
                    + "CAST(giaTheoGio AS FLOAT) AS giaTheoGio, "
                    + "CAST(giaQuaDem AS FLOAT) AS giaQuaDem, "
                    + "CAST(giaTheoNgay AS FLOAT) AS giaTheoNgay, "
                    + "CAST(giaCuoiTuan AS FLOAT) AS giaCuoiTuan, "
                    + "CAST(giaLe AS FLOAT) AS giaLe, "
                    + "CAST(phuThu AS FLOAT) AS phuThu "
                    + "FROM ChiTietBangGia";

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
     * Lấy danh sách chi tiết bảng giá theo mã bảng giá.
     *
     * @param maBangGia mã bảng giá cần lấy danh sách chi tiết.
     * @return danh sách chi tiết bảng giá, rỗng nếu không có dữ liệu hoặc có lỗi.
     */
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

    /**
     * Tìm chi tiết bảng giá theo mã chi tiết bảng giá.
     *
     * @param maChiTietBangGia mã chi tiết bảng giá cần tìm.
     * @return đối tượng ChiTietBangGia nếu tìm thấy, ngược lại trả về null.
     */
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

    /**
     * Thêm mới một chi tiết bảng giá.
     *
     * Trước khi thêm, dữ liệu sẽ được kiểm tra bằng validateChiTietBangGia().
     * Nếu thêm thành công, mã chi tiết bảng giá vừa sinh sẽ được gán lại vào đối tượng.
     *
     * @param chiTietBangGia thông tin chi tiết bảng giá cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
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

    /**
     * Cập nhật thông tin chi tiết bảng giá.
     *
     * Trước khi cập nhật, dữ liệu sẽ được kiểm tra bằng validateChiTietBangGia().
     *
     * @param chiTietBangGia thông tin chi tiết bảng giá cần cập nhật.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
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

    /**
     * Xóa chi tiết bảng giá theo mã chi tiết bảng giá.
     *
     * @param maChiTietBangGia mã chi tiết bảng giá cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
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

    /**
     * Gán dữ liệu từ đối tượng ChiTietBangGia vào PreparedStatement.
     *
     * Method này dùng chung cho cả insert và update.
     * Khi includeId = true, method sẽ gán thêm mã chi tiết bảng giá ở vị trí tham số thứ 10.
     *
     * @param stmt PreparedStatement cần gán dữ liệu.
     * @param chiTietBangGia dữ liệu chi tiết bảng giá.
     * @param includeId true nếu đang cập nhật và cần gán maChiTietBangGia.
     * @throws SQLException nếu xảy ra lỗi khi gán tham số.
     */
    private void fillStatement(PreparedStatement stmt, ChiTietBangGia chiTietBangGia, boolean includeId) throws SQLException {
        stmt.setInt(1, chiTietBangGia.getMaBangGia());
        stmt.setString(2, normalizeLoaiNgay(chiTietBangGia.getLoaiNgay()));
        stmt.setString(3, chiTietBangGia.getKhungGio());
        stmt.setDouble(4, chiTietBangGia.getGiaTheoGio());
        stmt.setDouble(5, chiTietBangGia.getGiaQuaDem());
        stmt.setDouble(6, chiTietBangGia.getGiaTheoNgay());
        stmt.setDouble(7, chiTietBangGia.getGiaCuoiTuan());
        stmt.setDouble(8, chiTietBangGia.getGiaLe());
        stmt.setDouble(9, 0d);
        if (includeId) {
            stmt.setInt(10, chiTietBangGia.getMaChiTietBangGia());
        }
    }

    /**
     * Chuyển dữ liệu từ ResultSet thành đối tượng ChiTietBangGia.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu chi tiết bảng giá.
     * @return đối tượng ChiTietBangGia sau khi ánh xạ.
     * @throws SQLException nếu xảy ra lỗi khi đọc dữ liệu từ ResultSet.
     */
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

    /**
     * Kiểm tra dữ liệu chi tiết bảng giá trước khi thêm hoặc cập nhật.
     *
     * Các điều kiện kiểm tra:
     * - Khi cập nhật, mã chi tiết bảng giá phải hợp lệ.
     * - Mã bảng giá phải hợp lệ.
     * - Loại ngày được chuẩn hóa về giá trị mặc định nếu rỗng.
     * - Khung giờ không được rỗng.
     * - Các giá và phụ thu phải lớn hơn hoặc bằng 0.
     *
     * @param chiTietBangGia dữ liệu chi tiết bảng giá cần kiểm tra.
     * @param updating true nếu đang cập nhật, false nếu đang thêm mới.
     * @return true nếu dữ liệu hợp lệ, false nếu không hợp lệ.
     */
    private boolean validateChiTietBangGia(ChiTietBangGia chiTietBangGia, boolean updating) {
        if (updating && chiTietBangGia.getMaChiTietBangGia() <= 0) {
            setLastError("Ma chi tiet bang gia khong hop le.");
            return false;
        }
        if (chiTietBangGia.getMaBangGia() <= 0) {
            setLastError("Ma bang gia khong hop le.");
            return false;
        }
        chiTietBangGia.setLoaiNgay(normalizeLoaiNgay(chiTietBangGia.getLoaiNgay()));
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

    /**
     * Kiểm tra giá trị số có lớn hơn hoặc bằng 0 hay không.
     *
     * @param value giá trị cần kiểm tra.
     * @return true nếu value >= 0, false nếu value < 0.
     */
    private boolean isNonNegative(double value) {
        return value >= 0;
    }

    /**
     * Chuẩn hóa loại ngày.
     *
     * Nếu loại ngày null hoặc rỗng, method sẽ dùng loại ngày mặc định
     * được định nghĩa trong ChiTietBangGia.DEFAULT_LOAI_NGAY.
     *
     * @param loaiNgay loại ngày cần chuẩn hóa.
     * @return loại ngày sau khi chuẩn hóa.
     */
    private String normalizeLoaiNgay(String loaiNgay) {
        return loaiNgay == null || loaiNgay.trim().isEmpty() ? ChiTietBangGia.DEFAULT_LOAI_NGAY : loaiNgay.trim();
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
}