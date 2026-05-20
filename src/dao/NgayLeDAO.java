package dao;

import db.ConnectDB;
import entity.NgayLe;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO xử lý dữ liệu ngày lễ.
 *
 * Lớp này phụ trách:
 * - Kiểm tra một ngày có phải ngày lễ hay không.
 * - Tìm ngày lễ theo ngày cụ thể.
 * - Lấy danh sách ngày lễ đang áp dụng.
 * - Tự nhận diện cấu trúc bảng NgayLe theo dạng một ngày hoặc khoảng ngày.
 */
public class NgayLeDAO {
    /**
     * Trạng thái ngày lễ đang được áp dụng.
     */
    private static final String ACTIVE_STATUS = "Đang áp dụng";

    /**
     * Chưa xác định được cấu trúc bảng NgayLe.
     */
    private static final int SCHEMA_UNKNOWN = 0;

    /**
     * Bảng NgayLe dùng cột ngay để lưu một ngày lễ đơn lẻ.
     */
    private static final int SCHEMA_SINGLE_DAY = 1;

    /**
     * Bảng NgayLe dùng cột ngayBatDau và ngayKetThuc để lưu khoảng ngày lễ.
     */
    private static final int SCHEMA_RANGE = 2;

    /**
     * Lưu loại cấu trúc bảng NgayLe hiện tại.
     *
     * volatile giúp giá trị được nhìn thấy chính xác hơn khi nhiều luồng cùng truy cập.
     */
    private volatile int schemaMode = SCHEMA_UNKNOWN;

    /**
     * Kiểm tra một ngày có phải ngày lễ đang áp dụng hay không.
     *
     * @param date ngày cần kiểm tra.
     * @return true nếu ngày đó là ngày lễ, false nếu không phải hoặc không có dữ liệu.
     */
    public boolean isHoliday(LocalDate date) {
        return findHolidayByDate(date) != null;
    }

    /**
     * Tìm thông tin ngày lễ theo một ngày cụ thể.
     *
     * Method sẽ tự xác định cấu trúc bảng NgayLe:
     * - Nếu bảng có cột ngay thì tìm theo một ngày.
     * - Nếu bảng có cột ngayBatDau/ngayKetThuc thì tìm theo khoảng ngày.
     *
     * @param date ngày cần tìm.
     * @return đối tượng NgayLe nếu tìm thấy, ngược lại trả về null.
     */
    public NgayLe findHolidayByDate(LocalDate date) {
        if (date == null) {
            return null;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        int mode = resolveSchemaMode(con);
        if (mode == SCHEMA_SINGLE_DAY) {
            return findHolidayByDateSingleDay(con, date);
        }
        if (mode == SCHEMA_RANGE) {
            return findHolidayByDateRange(con, date);
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách ngày lễ đang áp dụng.
     *
     * Method hỗ trợ cả hai kiểu cấu trúc bảng:
     * - Ngày lễ dạng một ngày.
     * - Ngày lễ dạng khoảng ngày.
     *
     * @return danh sách ngày lễ đang áp dụng.
     */
    public List<NgayLe> getAllActiveHolidays() {
        List<NgayLe> holidays = new ArrayList<NgayLe>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return holidays;
        }

        int mode = resolveSchemaMode(con);
        if (mode == SCHEMA_SINGLE_DAY) {
            loadSingleDayHolidays(con, holidays);
        } else if (mode == SCHEMA_RANGE) {
            loadRangeHolidays(con, holidays);
        }
        return holidays;
    }

    /**
     * Xác định cấu trúc hiện tại của bảng NgayLe.
     *
     * Method kiểm tra sự tồn tại của các cột:
     * - ngay: cấu trúc ngày lễ đơn.
     * - ngayBatDau/ngayKetThuc: cấu trúc khoảng ngày lễ.
     *
     * Sau khi xác định, kết quả được lưu vào schemaMode để tránh kiểm tra lại nhiều lần.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @return mã cấu trúc bảng NgayLe.
     */
    private int resolveSchemaMode(Connection con) {
        if (schemaMode != SCHEMA_UNKNOWN) {
            return schemaMode;
        }
        String sql = "SELECT "
                + "CASE WHEN COL_LENGTH('dbo.NgayLe', 'ngay') IS NOT NULL THEN 1 ELSE 0 END AS hasNgay, "
                + "CASE WHEN COL_LENGTH('dbo.NgayLe', 'ngayBatDau') IS NOT NULL THEN 1 ELSE 0 END AS hasNgayBatDau, "
                + "CASE WHEN COL_LENGTH('dbo.NgayLe', 'ngayKetThuc') IS NOT NULL THEN 1 ELSE 0 END AS hasNgayKetThuc";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                if (rs.getInt("hasNgay") == 1) {
                    schemaMode = SCHEMA_SINGLE_DAY;
                } else if (rs.getInt("hasNgayBatDau") == 1 || rs.getInt("hasNgayKetThuc") == 1) {
                    schemaMode = SCHEMA_RANGE;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schemaMode;
    }

    /**
     * Tìm ngày lễ theo cấu trúc bảng có cột ngay.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param date ngày cần tìm.
     * @return ngày lễ nếu tìm thấy, ngược lại trả về null.
     */
    private NgayLe findHolidayByDateSingleDay(Connection con, LocalDate date) {
        String sql = "SELECT TOP 1 maNgayLe, tenNgayLe, ngay AS ngayBatDau, ngay AS ngayKetThuc, "
                + "N'Ngày lễ' AS loaiNgay, CAST(0 AS FLOAT) AS heSoPhuThu, trangThai, moTa AS ghiChu "
                + "FROM dbo.NgayLe "
                + "WHERE trangThai = ? AND ngay = ? "
                + "ORDER BY ngay ASC, maNgayLe ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ACTIVE_STATUS);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapNgayLe(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tìm ngày lễ theo cấu trúc bảng có khoảng ngày.
     *
     * Một ngày được xem là ngày lễ nếu nằm trong khoảng ngayBatDau đến ngayKetThuc.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param date ngày cần tìm.
     * @return ngày lễ nếu tìm thấy, ngược lại trả về null.
     */
    private NgayLe findHolidayByDateRange(Connection con, LocalDate date) {
        String sql = "SELECT TOP 1 maNgayLe, tenNgayLe, ngayBatDau, ngayKetThuc, "
                + "ISNULL(loaiNgay, N'Ngày lễ') AS loaiNgay, ISNULL(heSoPhuThu, 0) AS heSoPhuThu, trangThai, ghiChu "
                + "FROM dbo.NgayLe "
                + "WHERE trangThai = ? AND ? BETWEEN ngayBatDau AND ngayKetThuc "
                + "ORDER BY ngayBatDau ASC, maNgayLe ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ACTIVE_STATUS);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapNgayLe(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Nạp danh sách ngày lễ đang áp dụng theo cấu trúc bảng có cột ngay.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param holidays danh sách dùng để chứa kết quả nạp được.
     */
    private void loadSingleDayHolidays(Connection con, List<NgayLe> holidays) {
        String sql = "SELECT maNgayLe, tenNgayLe, ngay AS ngayBatDau, ngay AS ngayKetThuc, "
                + "N'Ngày lễ' AS loaiNgay, CAST(0 AS FLOAT) AS heSoPhuThu, trangThai, moTa AS ghiChu "
                + "FROM dbo.NgayLe "
                + "WHERE trangThai = ? "
                + "ORDER BY ngay ASC, maNgayLe ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ACTIVE_STATUS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    holidays.add(mapNgayLe(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Nạp danh sách ngày lễ đang áp dụng theo cấu trúc bảng có khoảng ngày.
     *
     * @param con kết nối cơ sở dữ liệu.
     * @param holidays danh sách dùng để chứa kết quả nạp được.
     */
    private void loadRangeHolidays(Connection con, List<NgayLe> holidays) {
        String sql = "SELECT maNgayLe, tenNgayLe, ngayBatDau, ngayKetThuc, "
                + "ISNULL(loaiNgay, N'Ngày lễ') AS loaiNgay, ISNULL(heSoPhuThu, 0) AS heSoPhuThu, trangThai, ghiChu "
                + "FROM dbo.NgayLe "
                + "WHERE trangThai = ? "
                + "ORDER BY ngayBatDau ASC, maNgayLe ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ACTIVE_STATUS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    holidays.add(mapNgayLe(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chuyển dữ liệu từ ResultSet thành đối tượng NgayLe.
     *
     * @param rs ResultSet đang trỏ tới dòng dữ liệu ngày lễ.
     * @return đối tượng NgayLe sau khi ánh xạ dữ liệu.
     * @throws SQLException nếu xảy ra lỗi khi đọc dữ liệu từ ResultSet.
     */
    private NgayLe mapNgayLe(ResultSet rs) throws SQLException {
        NgayLe ngayLe = new NgayLe();
        ngayLe.setMaNgayLe(rs.getInt("maNgayLe"));
        ngayLe.setTenNgayLe(rs.getString("tenNgayLe"));
        ngayLe.setNgayBatDau(toLocalDate(rs.getDate("ngayBatDau")));
        ngayLe.setNgayKetThuc(toLocalDate(rs.getDate("ngayKetThuc")));
        ngayLe.setLoaiNgay(rs.getString("loaiNgay"));
        ngayLe.setHeSoPhuThu(rs.getDouble("heSoPhuThu"));
        ngayLe.setTrangThai(rs.getString("trangThai"));
        ngayLe.setGhiChu(rs.getString("ghiChu"));
        return ngayLe;
    }

    /**
     * Chuyển java.sql.Date sang LocalDate.
     *
     * @param value ngày SQL cần chuyển.
     * @return LocalDate tương ứng, hoặc null nếu value null.
     */
    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}