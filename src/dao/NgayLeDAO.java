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

        String sql = "SELECT TOP 1 maNgayLe, tenNgayLe, ngayBatDau, ngayKetThuc, loaiNgay, heSoPhuThu, trangThai, ghiChu " +
                "FROM dbo.NgayLe " +
                "WHERE trangThai = N'Đang áp dụng' AND ? BETWEEN ngayBatDau AND ngayKetThuc " +
                "ORDER BY ngayBatDau ASC, maNgayLe ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
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

    public List<NgayLe> getAllActiveHolidays() {
        List<NgayLe> holidays = new ArrayList<NgayLe>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return holidays;
        }

        String sql = "SELECT maNgayLe, tenNgayLe, ngayBatDau, ngayKetThuc, loaiNgay, heSoPhuThu, trangThai, ghiChu " +
                "FROM dbo.NgayLe " +
                "WHERE trangThai = N'Đang áp dụng' " +
                "ORDER BY ngayBatDau ASC, maNgayLe ASC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                holidays.add(mapNgayLe(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return holidays;
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
