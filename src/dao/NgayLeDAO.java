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

public class NgayLeDAO {
    private static final String ACTIVE_STATUS = "Đang áp dụng";
    private static final int SCHEMA_UNKNOWN = 0;
    private static final int SCHEMA_SINGLE_DAY = 1;
    private static final int SCHEMA_RANGE = 2;

    private volatile int schemaMode = SCHEMA_UNKNOWN;

    public boolean isHoliday(LocalDate date) {
        return findHolidayByDate(date) != null;
    }

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

    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}
