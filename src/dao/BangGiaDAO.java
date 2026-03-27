package dao;

import db.ConnectDB;
import entity.BangGia;
import entity.LoaiPhong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BangGiaDAO {
    private static final String SELECT_BASE =
            "SELECT bg.maBangGia, bg.tenBangGia, bg.maLoaiPhong, bg.ngayBatDau, bg.ngayKetThuc, "
                    + "bg.loaiNgay, bg.trangThai, lp.tenLoaiPhong "
                    + "FROM BangGia bg "
                    + "LEFT JOIN LoaiPhong lp ON bg.maLoaiPhong = lp.maLoaiPhong";

    private String lastErrorMessage = "";
    private final Map<Integer, String> loaiNgayCache = new HashMap<Integer, String>();

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<BangGia> getAll() {
        clearLastError();
        loaiNgayCache.clear();
        List<BangGia> dsBangGia = new ArrayList<BangGia>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsBangGia;
        }

        String sql = SELECT_BASE + " ORDER BY bg.maBangGia DESC";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dsBangGia.add(mapBangGia(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return dsBangGia;
    }

    public BangGia findById(int maBangGia) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }

        String sql = SELECT_BASE + " WHERE bg.maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapBangGia(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim bang gia theo ma: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return null;
    }

    public List<BangGia> search(String tenBangGia, String maLoaiPhong, Date from, Date to, String loaiNgay) {
        clearLastError();
        loaiNgayCache.clear();
        List<BangGia> dsBangGia = new ArrayList<BangGia>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsBangGia;
        }

        String tenSearch = tenBangGia == null ? "" : tenBangGia.trim();
        String maLoaiSearch = maLoaiPhong == null ? "" : maLoaiPhong.trim();
        String loaiNgaySearch = loaiNgay == null ? "" : loaiNgay.trim();

        String sql = "SELECT bg.maBangGia, bg.tenBangGia, bg.maLoaiPhong, bg.ngayBatDau, bg.ngayKetThuc, "
                + "bg.loaiNgay, bg.trangThai, lp.tenLoaiPhong "
                + "FROM BangGia bg "
                + "LEFT JOIN LoaiPhong lp ON bg.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE (? = '' OR bg.tenBangGia LIKE ?) "
                + "AND (? = '' OR CAST(bg.maLoaiPhong AS NVARCHAR(20)) = ?) "
                + "AND (? IS NULL OR bg.ngayBatDau >= ?) "
                + "AND (? IS NULL OR bg.ngayKetThuc <= ?) "
                + "AND (? = '' OR bg.loaiNgay = ?) "
                + "ORDER BY bg.maBangGia DESC";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, tenSearch);
            stmt.setString(2, "%" + tenSearch + "%");
            stmt.setString(3, maLoaiSearch);
            stmt.setString(4, maLoaiSearch);
            stmt.setDate(5, from);
            stmt.setDate(6, from);
            stmt.setDate(7, to);
            stmt.setDate(8, to);
            stmt.setString(9, loaiNgaySearch);
            stmt.setString(10, loaiNgaySearch);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dsBangGia.add(mapBangGia(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi tim kiem bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return dsBangGia;
    }

    public boolean insert(BangGia bangGia, String loaiNgay) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || bangGia == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu bảng giá không hợp lệ.");
            return false;
        }
        if (!validateBangGia(bangGia, loaiNgay, false)) {
            return false;
        }

        String sql = "INSERT INTO BangGia(tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, loaiNgay);
            stmt.setString(6, bangGia.getTrangThai());
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int maBangGia = rs.getInt(1);
                        bangGia.setMaBangGia(maBangGia);
                        loaiNgayCache.put(maBangGia, loaiNgay);
                    }
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.out.println("Loi them bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public boolean update(BangGia bangGia, String loaiNgay) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || bangGia == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu bảng giá không hợp lệ.");
            return false;
        }
        if (!validateBangGia(bangGia, loaiNgay, true)) {
            return false;
        }

        String sql = "UPDATE BangGia SET tenBangGia = ?, maLoaiPhong = ?, ngayBatDau = ?, ngayKetThuc = ?, "
                + "loaiNgay = ?, trangThai = ? WHERE maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, loaiNgay);
            stmt.setString(6, bangGia.getTrangThai());
            stmt.setInt(7, bangGia.getMaBangGia());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                loaiNgayCache.put(bangGia.getMaBangGia(), loaiNgay);
            }
            return updated;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat bang gia co ma: " + bangGia.getMaBangGia());
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public boolean delete(int maBangGia) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        try {
            con.setAutoCommit(false);
            try (PreparedStatement deleteDetail = con.prepareStatement("DELETE FROM ChiTietBangGia WHERE maBangGia = ?");
                 PreparedStatement deleteBangGia = con.prepareStatement("DELETE FROM BangGia WHERE maBangGia = ?")) {
                deleteDetail.setInt(1, maBangGia);
                deleteDetail.executeUpdate();
                deleteBangGia.setInt(1, maBangGia);
                boolean deleted = deleteBangGia.executeUpdate() > 0;
                con.commit();
                loaiNgayCache.remove(maBangGia);
                return deleted;
            } catch (SQLException e) {
                con.rollback();
                System.out.println("Loi xoa bang gia co ma: " + maBangGia);
                e.printStackTrace();
                setLastError(e.getMessage());
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Loi giao dich xoa bang gia co ma: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    public List<LoaiPhong> getAllLoaiPhong() {
        return new LoaiPhongDAO().getAll();
    }

    public String getLoaiNgayByMaBangGia(int maBangGia) {
        if (loaiNgayCache.containsKey(maBangGia)) {
            return loaiNgayCache.get(maBangGia);
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return "";
        }

        String sql = "SELECT loaiNgay FROM BangGia WHERE maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String loaiNgay = rs.getString("loaiNgay");
                    loaiNgayCache.put(maBangGia, loaiNgay);
                    return loaiNgay == null ? "" : loaiNgay;
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay loai ngay cua bang gia: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return "";
    }

    private BangGia mapBangGia(ResultSet rs) throws SQLException {
        int maBangGia = rs.getInt("maBangGia");
        loaiNgayCache.put(maBangGia, rs.getString("loaiNgay"));
        return new BangGia(
                maBangGia,
                rs.getString("tenBangGia"),
                rs.getInt("maLoaiPhong"),
                rs.getDate("ngayBatDau"),
                rs.getDate("ngayKetThuc"),
                rs.getString("trangThai"),
                "",
                rs.getString("tenLoaiPhong")
        );
    }

    private boolean validateBangGia(BangGia bangGia, String loaiNgay, boolean updating) {
        if (bangGia.getTenBangGia() == null || bangGia.getTenBangGia().trim().isEmpty()) {
            setLastError("Tên bảng giá không được rỗng.");
            return false;
        }
        if (bangGia.getMaLoaiPhong() <= 0 || !isLoaiPhongExists(bangGia.getMaLoaiPhong())) {
            setLastError("Loại phòng không hợp lệ hoặc không tồn tại.");
            return false;
        }
        if (bangGia.getTuNgay() == null || bangGia.getDenNgay() == null) {
            setLastError("Ngày bắt đầu và ngày kết thúc là bắt buộc.");
            return false;
        }
        if (bangGia.getTuNgay().after(bangGia.getDenNgay())) {
            setLastError("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
            return false;
        }
        if (loaiNgay == null || loaiNgay.trim().isEmpty()) {
            setLastError("Loại ngày không được rỗng.");
            return false;
        }
        if (bangGia.getTrangThai() == null || bangGia.getTrangThai().trim().isEmpty()) {
            setLastError("Trạng thái không được rỗng.");
            return false;
        }
        if (updating && bangGia.getMaBangGia() <= 0) {
            setLastError("Mã bảng giá không hợp lệ.");
            return false;
        }
        return true;
    }

    private boolean isLoaiPhongExists(int maLoaiPhong) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return false;
        }

        String sql = "SELECT 1 FROM LoaiPhong WHERE maLoaiPhong = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maLoaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Loi kiem tra loai phong co ma: " + maLoaiPhong);
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
