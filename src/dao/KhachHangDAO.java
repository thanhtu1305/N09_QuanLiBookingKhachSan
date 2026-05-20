package dao;

import db.ConnectDB;
import entity.KhachHang;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) xử lý các thao tác CRUD và truy vấn với bảng KhachHang.
 * Hỗ trợ lưu thông báo lỗi cuối cùng để tầng trên có thể hiển thị cho người dùng.
 */
public class KhachHangDAO {

    // Câu truy vấn SELECT dùng chung cho tất cả các phương thức tìm kiếm
    private static final String SELECT_BASE =
            "SELECT maKhachHang, hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, "
                    + "quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu "
                    + "FROM KhachHang";

    // Lưu thông báo lỗi của thao tác gần nhất để tầng giao diện có thể truy xuất
    private String lastErrorMessage = "";

    /**
     * Trả về thông báo lỗi của thao tác gần nhất.
     *
     * @return Chuỗi thông báo lỗi, hoặc chuỗi rỗng nếu không có lỗi.
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Lấy toàn bộ danh sách khách hàng, sắp xếp theo mã giảm dần.
     *
     * @return Danh sách tất cả khách hàng, hoặc danh sách rỗng nếu lỗi.
     */
    public List<KhachHang> getAll() {
        clearLastError();
        List<KhachHang> result = new ArrayList<KhachHang>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        String sql = SELECT_BASE + " ORDER BY maKhachHang DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapKhachHang(rs));
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Tìm kiếm khách hàng theo từ khóa kết hợp với các bộ lọc loại, hạng và trạng thái.
     * Các bộ lọc nào để null hoặc rỗng thì sẽ không áp dụng.
     *
     * @param keyword    Từ khóa tìm theo mã, họ tên, số điện thoại hoặc CCCD/Passport.
     * @param loaiKhach  Lọc theo loại khách (ví dụ: Cá nhân, Doanh nghiệp).
     * @param hangKhach  Lọc theo hạng khách (ví dụ: Thường, VIP).
     * @param trangThai  Lọc theo trạng thái (ví dụ: Hoạt động, Khóa).
     * @return Danh sách khách hàng phù hợp với điều kiện lọc.
     */
    public List<KhachHang> search(String keyword, String loaiKhach, String hangKhach, String trangThai) {
        clearLastError();
        List<KhachHang> result = new ArrayList<KhachHang>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return result;
        }

        // Chuẩn hóa tất cả tham số đầu vào: null → chuỗi rỗng, xóa khoảng trắng
        String tuKhoa = safeTrim(keyword);
        String loai = safeTrim(loaiKhach);
        String hang = safeTrim(hangKhach);
        String status = safeTrim(trangThai);

        // Nếu một bộ lọc rỗng (? = ''), điều kiện tương ứng luôn đúng → không lọc theo trường đó
        String sql = SELECT_BASE
                + " WHERE (? = '' OR CAST(maKhachHang AS NVARCHAR(20)) LIKE ? OR hoTen LIKE ? OR soDienThoai LIKE ? OR cccdPassport LIKE ?)"
                + " AND (? = '' OR loaiKhach = ?)"
                + " AND (? = '' OR hangKhach = ?)"
                + " AND (? = '' OR trangThai = ?)"
                + " ORDER BY maKhachHang DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            // Tham số 1–5: tìm kiếm từ khóa trên nhiều cột
            stmt.setString(1, tuKhoa);
            stmt.setString(2, "%" + tuKhoa + "%");
            stmt.setString(3, "%" + tuKhoa + "%");
            stmt.setString(4, "%" + tuKhoa + "%");
            stmt.setString(5, "%" + tuKhoa + "%");
            // Tham số 6–7: bộ lọc loại khách
            stmt.setString(6, loai);
            stmt.setString(7, loai);
            // Tham số 8–9: bộ lọc hạng khách
            stmt.setString(8, hang);
            stmt.setString(9, hang);
            // Tham số 10–11: bộ lọc trạng thái
            stmt.setString(10, status);
            stmt.setString(11, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapKhachHang(rs));
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Tìm khách hàng theo mã (dạng chuỗi, sẽ được parse sang số nguyên).
     *
     * @param maKhachHang Mã khách hàng dạng chuỗi.
     * @return Đối tượng KhachHang nếu tìm thấy, null nếu không có hoặc lỗi.
     */
    public KhachHang findById(String maKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();

        // Parse mã sang Integer, trả về null nếu không hợp lệ
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return null;
        }

        String sql = SELECT_BASE + " WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm khách hàng theo số điện thoại (khớp chính xác).
     *
     * @param soDienThoai Số điện thoại cần tìm.
     * @return Đối tượng KhachHang nếu tìm thấy, null nếu không có hoặc số rỗng.
     */
    public KhachHang findByPhone(String soDienThoai) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }
        String sdt = safeTrim(soDienThoai);

        // Không truy vấn nếu số điện thoại rỗng
        if (sdt.isEmpty()) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE soDienThoai = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, sdt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm khách hàng theo số CCCD hoặc Passport (khớp chính xác).
     *
     * @param cccdPassport Số CCCD hoặc Passport cần tìm.
     * @return Đối tượng KhachHang nếu tìm thấy, null nếu không có hoặc giá trị rỗng.
     */
    public KhachHang findByCccdPassport(String cccdPassport) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }
        String value = safeTrim(cccdPassport);

        // Không truy vấn nếu giá trị rỗng
        if (value.isEmpty()) {
            return null;
        }

        String sql = SELECT_BASE + " WHERE cccdPassport = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKhachHang(rs);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm khách hàng mới vào cơ sở dữ liệu.
     * Sau khi thêm thành công, mã tự sinh sẽ được gán lại vào đối tượng.
     *
     * @param khachHang Đối tượng KhachHang cần thêm.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean insert(KhachHang khachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || khachHang == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu khách hàng không hợp lệ.");
            return false;
        }

        // maKhachHang là IDENTITY (tự tăng), không cần truyền vào câu INSERT
        String sql = "INSERT INTO KhachHang(hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, khachHang);
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                // Lấy mã tự sinh và gán lại vào entity để dùng tiếp ở tầng trên
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        khachHang.setMaKhachHang(String.valueOf(rs.getInt(1)));
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
     * Cập nhật thông tin khách hàng (không cập nhật trạng thái, dùng updateTrangThaiTuDong cho trường đó).
     *
     * @param khachHang Đối tượng KhachHang với thông tin mới (phải có mã hợp lệ).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean update(KhachHang khachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = khachHang == null ? null : parseIntOrNull(khachHang.getMaKhachHang());
        if (con == null || khachHang == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return false;
        }

        // trangThai được quản lý riêng qua updateTrangThaiTuDong, không cập nhật ở đây
        String sql = "UPDATE KhachHang SET hoTen = ?, gioiTinh = ?, ngaySinh = ?, soDienThoai = ?, email = ?, "
                + "cccdPassport = ?, diaChi = ?, quocTich = ?, loaiKhach = ?, hangKhach = ?, nguoiTao = ?, ghiChu = ? "
                + "WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            fillStatementWithoutStatus(stmt, khachHang);
            stmt.setInt(13, id.intValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa khách hàng khỏi cơ sở dữ liệu theo mã.
     *
     * @param maKhachHang Mã khách hàng cần xóa (dạng chuỗi).
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean delete(String maKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return false;
        }

        String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật trạng thái khách hàng và tự động nối thêm ghi chú vào cuối ghi chú hiện có.
     * Nếu ghi chú hiện tại rỗng thì ghi đè, nếu có rồi thì nối thêm với dấu phân cách " | ".
     *
     * @param maKhachHang Mã khách hàng cần cập nhật.
     * @param trangThai   Trạng thái mới.
     * @param ghiChu      Nội dung ghi chú cần thêm vào.
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateTrangThaiTuDong(String maKhachHang, String trangThai, String ghiChu) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return false;
        }

        // CASE trong SQL: nếu ghi chú đang rỗng thì ghi mới, ngược lại nối thêm sau dấu " | "
        String sql = "UPDATE KhachHang SET trangThai = ?, ghiChu = CASE " +
                "WHEN ISNULL(ghiChu,'') = '' THEN ? ELSE ghiChu + N' | ' + ? END WHERE maKhachHang = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            String note = safeTrim(ghiChu);
            stmt.setString(1, safeTrim(trangThai));
            stmt.setString(2, note); // Trường hợp ghi chú rỗng: ghi đè
            stmt.setString(3, note); // Trường hợp đã có ghi chú: nối thêm
            stmt.setInt(4, id.intValue());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra số điện thoại đã tồn tại trong hệ thống chưa, có thể loại trừ một khách hàng cụ thể
     * (dùng khi cập nhật để tránh báo trùng chính mình).
     *
     * @param soDienThoai        Số điện thoại cần kiểm tra.
     * @param excludeMaKhachHang Mã khách hàng cần loại trừ, null nếu không cần loại trừ.
     * @return true nếu số điện thoại đã tồn tại, false nếu chưa.
     */
    public boolean existsByPhone(String soDienThoai, String excludeMaKhachHang) {
        return existsByField("soDienThoai", soDienThoai, excludeMaKhachHang);
    }

    /**
     * Kiểm tra CCCD/Passport đã tồn tại trong hệ thống chưa, có thể loại trừ một khách hàng cụ thể.
     *
     * @param cccdPassport       Số CCCD hoặc Passport cần kiểm tra.
     * @param excludeMaKhachHang Mã khách hàng cần loại trừ, null nếu không cần loại trừ.
     * @return true nếu CCCD/Passport đã tồn tại, false nếu chưa.
     */
    public boolean existsByCccdPassport(String cccdPassport, String excludeMaKhachHang) {
        return existsByField("cccdPassport", cccdPassport, excludeMaKhachHang);
    }

    /**
     * Lấy tối đa 10 bản ghi lịch sử lưu trú gần nhất của khách hàng.
     * Mỗi phần tử trong danh sách là chuỗi định dạng: "soPhong - ngayNhan - ngayTra - trangThai".
     *
     * @param maKhachHang Mã khách hàng cần truy vấn lịch sử.
     * @return Danh sách chuỗi mô tả lịch sử lưu trú, hoặc danh sách với thông báo nếu không có dữ liệu.
     */
    public List<String> getLichSuLuuTruGanDay(String maKhachHang) {
        clearLastError();
        List<String> result = new ArrayList<String>();
        Connection con = ConnectDB.getConnection();
        Integer id = parseIntOrNull(maKhachHang);
        if (con == null || id == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Mã khách hàng không hợp lệ.");
            return result;
        }

        // JOIN 3 bảng: DatPhong → ChiTietDatPhong → Phong để lấy số phòng thực tế
        String sql = "SELECT TOP 10 p.soPhong, dp.ngayNhanPhong, dp.ngayTraPhong, dp.trangThai "
                + "FROM DatPhong dp "
                + "LEFT JOIN ChiTietDatPhong ctdp ON dp.maDatPhong = ctdp.maDatPhong "
                + "LEFT JOIN Phong p ON ctdp.maPhong = p.maPhong "
                + "WHERE dp.maKhachHang = ? "
                + "ORDER BY dp.ngayNhanPhong DESC, dp.maDatPhong DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id.intValue());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String soPhong = safeTrim(rs.getString("soPhong"));
                    // Hiển thị "-" nếu ngày null, ngược lại format thành chuỗi ISO (yyyy-MM-dd)
                    String ngayNhan = rs.getDate("ngayNhanPhong") == null ? "-" : rs.getDate("ngayNhanPhong").toLocalDate().toString();
                    String ngayTra = rs.getDate("ngayTraPhong") == null ? "-" : rs.getDate("ngayTraPhong").toLocalDate().toString();
                    String trangThai = safeTrim(rs.getString("trangThai"));
                    // Ghép thành chuỗi hiển thị; nếu chưa gán phòng thì dùng nhãn mặc định
                    result.add((soPhong.isEmpty() ? "Chưa gán phòng" : soPhong) + " - " + ngayNhan + " - " + ngayTra + " - " + trangThai);
                }
            }
        } catch (SQLException e) {
            setLastError(e.getMessage());
            e.printStackTrace();
        }

        // Trả về thông báo mặc định thay vì danh sách rỗng để UI dễ xử lý
        if (result.isEmpty()) {
            result.add("Không có lịch sử lưu trú gần đây");
        }
        return result;
    }

    /**
     * Kiểm tra giá trị của một trường có tồn tại trong bảng KhachHang không,
     * tùy chọn loại trừ một khách hàng cụ thể (dùng chung cho phone và cccdPassport).
     *
     * @param fieldName          Tên cột cần kiểm tra (phải là tên cột hợp lệ trong DB).
     * @param value              Giá trị cần kiểm tra.
     * @param excludeMaKhachHang Mã khách hàng loại trừ, null nếu không cần.
     * @return true nếu đã tồn tại bản ghi trùng, false nếu chưa.
     */
    private boolean existsByField(String fieldName, String value, String excludeMaKhachHang) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }
        String safeValue = safeTrim(value);
        if (safeValue.isEmpty()) {
            return false;
        }

        Integer excludeId = parseIntOrNull(excludeMaKhachHang);
        // Thêm điều kiện loại trừ khách hàng hiện tại nếu có (tránh báo trùng chính mình khi update)
        String sql = "SELECT COUNT(1) FROM KhachHang WHERE " + fieldName + " = ?"
                + (excludeId != null ? " AND maKhachHang <> ?" : "");
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, safeValue);
            if (excludeId != null) {
                stmt.setInt(2, excludeId.intValue());
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

    /**
     * Gán toàn bộ tham số vào PreparedStatement cho câu INSERT (bao gồm cả trangThai).
     * Thứ tự tham số phải khớp với câu SQL trong phương thức insert().
     *
     * @param stmt      PreparedStatement cần gán tham số.
     * @param khachHang Đối tượng KhachHang chứa dữ liệu nguồn.
     */
    private void fillStatement(PreparedStatement stmt, KhachHang khachHang) throws SQLException {
        stmt.setString(1, safeTrim(khachHang.getHoTen()));
        stmt.setString(2, nullIfEmpty(khachHang.getGioiTinh()));
        setNullableDate(stmt, 3, khachHang.getNgaySinh()); // Xử lý riêng vì cần parse LocalDate
        stmt.setString(4, nullIfEmpty(khachHang.getSoDienThoai()));
        stmt.setString(5, nullIfEmpty(khachHang.getEmail()));
        stmt.setString(6, nullIfEmpty(khachHang.getCccdPassport()));
        stmt.setString(7, nullIfEmpty(khachHang.getDiaChi()));
        stmt.setString(8, nullIfEmpty(khachHang.getQuocTich()));
        stmt.setString(9, nullIfEmpty(khachHang.getLoaiKhach()));
        stmt.setString(10, nullIfEmpty(khachHang.getHangKhach()));
        stmt.setString(11, nullIfEmpty(khachHang.getTrangThai())); // Chỉ INSERT mới set trangThai
        stmt.setString(12, nullIfEmpty(khachHang.getNguoiTao()));
        stmt.setString(13, nullIfEmpty(khachHang.getGhiChu()));
    }

    /**
     * Gán tham số vào PreparedStatement cho câu UPDATE (không bao gồm trangThai).
     * trangThai được quản lý riêng qua updateTrangThaiTuDong để tránh ghi đè ngoài ý muốn.
     *
     * @param stmt      PreparedStatement cần gán tham số.
     * @param khachHang Đối tượng KhachHang chứa dữ liệu nguồn.
     */
    private void fillStatementWithoutStatus(PreparedStatement stmt, KhachHang khachHang) throws SQLException {
        stmt.setString(1, safeTrim(khachHang.getHoTen()));
        stmt.setString(2, nullIfEmpty(khachHang.getGioiTinh()));
        setNullableDate(stmt, 3, khachHang.getNgaySinh());
        stmt.setString(4, nullIfEmpty(khachHang.getSoDienThoai()));
        stmt.setString(5, nullIfEmpty(khachHang.getEmail()));
        stmt.setString(6, nullIfEmpty(khachHang.getCccdPassport()));
        stmt.setString(7, nullIfEmpty(khachHang.getDiaChi()));
        stmt.setString(8, nullIfEmpty(khachHang.getQuocTich()));
        stmt.setString(9, nullIfEmpty(khachHang.getLoaiKhach()));
        stmt.setString(10, nullIfEmpty(khachHang.getHangKhach()));
        // Không có trangThai ở đây
        stmt.setString(11, nullIfEmpty(khachHang.getNguoiTao()));
        stmt.setString(12, nullIfEmpty(khachHang.getGhiChu()));
    }

    /**
     * Ánh xạ một hàng dữ liệu từ ResultSet thành đối tượng KhachHang.
     * Ngày sinh null trong DB được chuyển thành chuỗi rỗng để tránh NullPointerException ở tầng trên.
     *
     * @param rs ResultSet đang trỏ đến hàng cần đọc.
     * @return Đối tượng KhachHang tương ứng.
     * @throws SQLException Nếu tên cột không tồn tại hoặc lỗi đọc dữ liệu.
     */
    private KhachHang mapKhachHang(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(String.valueOf(rs.getInt("maKhachHang")));
        kh.setHoTen(rs.getString("hoTen"));
        kh.setGioiTinh(rs.getString("gioiTinh"));
        // Chuyển Date → chuỗi ISO, hoặc chuỗi rỗng nếu null
        kh.setNgaySinh(rs.getDate("ngaySinh") == null ? "" : rs.getDate("ngaySinh").toLocalDate().toString());
        kh.setSoDienThoai(rs.getString("soDienThoai"));
        kh.setEmail(rs.getString("email"));
        kh.setCccdPassport(rs.getString("cccdPassport"));
        kh.setDiaChi(rs.getString("diaChi"));
        kh.setQuocTich(rs.getString("quocTich"));
        kh.setLoaiKhach(rs.getString("loaiKhach"));
        kh.setHangKhach(rs.getString("hangKhach"));
        kh.setTrangThai(rs.getString("trangThai"));
        kh.setNguoiTao(rs.getString("nguoiTao"));
        kh.setGhiChu(rs.getString("ghiChu"));
        return kh;
    }

    /**
     * Gán giá trị ngày vào PreparedStatement, xử lý trường hợp chuỗi rỗng hoặc null → set NULL trong DB.
     *
     * @param stmt  PreparedStatement cần gán.
     * @param index Vị trí tham số (1-based).
     * @param value Chuỗi ngày định dạng ISO (yyyy-MM-dd), hoặc null/rỗng.
     */
    private void setNullableDate(PreparedStatement stmt, int index, String value) throws SQLException {
        String text = safeTrim(value);
        if (text.isEmpty()) {
            stmt.setDate(index, null); // Không có ngày → lưu NULL vào DB
            return;
        }
        stmt.setDate(index, Date.valueOf(LocalDate.parse(text)));
    }

    /**
     * Parse chuỗi sang Integer, trả về null nếu chuỗi null, rỗng hoặc không phải số.
     *
     * @param value Chuỗi cần parse.
     * @return Giá trị Integer, hoặc null nếu không hợp lệ.
     */
    private Integer parseIntOrNull(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Integer.valueOf(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Trả về chuỗi rỗng nếu value là null, ngược lại trả về chuỗi đã trim.
     *
     * @param value Chuỗi đầu vào.
     * @return Chuỗi đã trim hoặc chuỗi rỗng.
     */
    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Trả về null nếu chuỗi rỗng sau khi trim, ngược lại trả về chuỗi đã trim.
     * Dùng để lưu NULL vào DB thay vì chuỗi rỗng cho các trường không bắt buộc.
     *
     * @param value Chuỗi đầu vào.
     * @return Chuỗi đã trim, hoặc null nếu rỗng.
     */
    private String nullIfEmpty(String value) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Xóa thông báo lỗi trước khi bắt đầu mỗi thao tác mới.
     */
    private void clearLastError() {
        lastErrorMessage = "";
    }

    /**
     * Lưu thông báo lỗi, xử lý trường hợp message null.
     *
     * @param message Nội dung lỗi cần lưu.
     */
    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}