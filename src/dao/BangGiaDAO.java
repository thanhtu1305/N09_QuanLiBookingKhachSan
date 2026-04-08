package dao;

import db.ConnectDB;
import entity.BangGia;
import entity.BangGiaConflictInfo;
import entity.ChiTietBangGia;
import entity.LoaiPhong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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

    public BangGiaConflictInfo findDateConflict(int maLoaiPhong, Date ngayBatDau, Date ngayKetThuc, Integer excludeMaBangGia) {
        clearLastError();
        if (maLoaiPhong <= 0 || ngayBatDau == null || ngayKetThuc == null) {
            return null;
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 1 bg.maBangGia, bg.tenBangGia, bg.maLoaiPhong, lp.tenLoaiPhong, ")
                .append("bg.ngayBatDau, bg.ngayKetThuc, bg.trangThai ")
                .append("FROM BangGia bg ")
                .append("LEFT JOIN LoaiPhong lp ON bg.maLoaiPhong = lp.maLoaiPhong ")
                .append("WHERE bg.maLoaiPhong = ? ")
                .append("AND bg.ngayBatDau <= ? ")
                .append("AND bg.ngayKetThuc >= ? ");
        if (excludeMaBangGia != null && excludeMaBangGia.intValue() > 0) {
            sql.append("AND bg.maBangGia <> ? ");
        }
        sql.append("ORDER BY CASE WHEN bg.trangThai = N'Đang áp dụng' THEN 0 ELSE 1 END, ")
                .append("bg.ngayBatDau DESC, bg.maBangGia DESC");

        try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int index = 1;
            stmt.setInt(index++, maLoaiPhong);
            stmt.setDate(index++, ngayKetThuc);
            stmt.setDate(index++, ngayBatDau);
            if (excludeMaBangGia != null && excludeMaBangGia.intValue() > 0) {
                stmt.setInt(index, excludeMaBangGia.intValue());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new BangGiaConflictInfo(
                            rs.getInt("maBangGia"),
                            rs.getString("tenBangGia"),
                            rs.getInt("maLoaiPhong"),
                            rs.getString("tenLoaiPhong"),
                            rs.getDate("ngayBatDau"),
                            rs.getDate("ngayKetThuc"),
                            rs.getString("trangThai")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi kiem tra trung thoi gian bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return null;
    }

    public boolean saveWithDetails(BangGia bangGia, String loaiNgay, List<ChiTietBangGia> chiTietBangGiaList) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || bangGia == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu bảng giá không hợp lệ.");
            return false;
        }
        if (!validateBangGia(bangGia, loaiNgay, bangGia.getMaBangGia() > 0)) {
            return false;
        }
        if (!validateChiTietList(chiTietBangGiaList)) {
            return false;
        }

        BangGiaConflictInfo conflictInfo = findDateConflict(
                bangGia.getMaLoaiPhong(),
                bangGia.getTuNgay(),
                bangGia.getDenNgay(),
                bangGia.getMaBangGia() > 0 ? Integer.valueOf(bangGia.getMaBangGia()) : null
        );
        if (conflictInfo != null) {
            setLastError(buildConflictErrorMessage(conflictInfo));
            return false;
        }

        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            if (bangGia.getMaBangGia() > 0) {
                if (!updateBangGiaTransaction(con, bangGia, loaiNgay)) {
                    con.rollback();
                    return false;
                }
                if (!deleteChiTietByMaBangGia(con, bangGia.getMaBangGia())) {
                    con.rollback();
                    return false;
                }
            } else {
                if (!insertBangGiaTransaction(con, bangGia, loaiNgay)) {
                    con.rollback();
                    return false;
                }
            }

            for (ChiTietBangGia chiTietBangGia : chiTietBangGiaList) {
                ChiTietBangGia detailToSave = copyChiTietBangGia(chiTietBangGia);
                detailToSave.setMaBangGia(bangGia.getMaBangGia());
                if (!insertChiTietBangGia(con, detailToSave)) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            loaiNgayCache.put(bangGia.getMaBangGia(), loaiNgay);
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            System.out.println("Loi luu bang gia kem chi tiet.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        } finally {
            try {
                con.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    public List<ChiTietBangGia> getChiTietBangGiaByMaBangGia(int maBangGia) {
        clearLastError();
        List<ChiTietBangGia> details = new ArrayList<ChiTietBangGia>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("KhÃ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u.");
            return details;
        }

        String sql = "SELECT maChiTietBangGia, maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu " +
                "FROM ChiTietBangGia WHERE maBangGia = ? ORDER BY maChiTietBangGia ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    details.add(mapChiTietBangGia(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay chi tiet bang gia theo ma bang gia: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return details;
    }

    public ChiTietBangGia getChiTietBangGiaDangApDung(int maBangGia) {
        return getChiTietBangGiaDangApDung(maBangGia, null);
    }

    public ChiTietBangGia getChiTietBangGiaDangApDung(int maBangGia, LocalDate ngayApDung) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("KhÃ´ng thá»ƒ káº¿t ná»‘i cÆ¡ sá»Ÿ dá»¯ liá»‡u.");
            return null;
        }

        String sql = "SELECT TOP 1 ct.maChiTietBangGia, ct.maBangGia, ct.loaiNgay, ct.khungGio, ct.giaTheoGio, ct.giaQuaDem, ct.giaTheoNgay, ct.giaCuoiTuan, ct.giaLe, ct.phuThu " +
                "FROM ChiTietBangGia ct " +
                "JOIN BangGia bg ON ct.maBangGia = bg.maBangGia " +
                "WHERE ct.maBangGia = ? " +
                "AND bg.trangThai = N'Đang áp dụng' " +
                "AND (? IS NULL OR ? BETWEEN bg.ngayBatDau AND bg.ngayKetThuc) " +
                "ORDER BY ct.maChiTietBangGia ASC";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            if (ngayApDung == null) {
                stmt.setDate(2, null);
                stmt.setDate(3, null);
            } else {
                Date sqlDate = Date.valueOf(ngayApDung);
                stmt.setDate(2, sqlDate);
                stmt.setDate(3, sqlDate);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapChiTietBangGia(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay chi tiet bang gia dang ap dung: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return null;
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

    private boolean validateChiTietList(List<ChiTietBangGia> chiTietBangGiaList) {
        if (chiTietBangGiaList == null || chiTietBangGiaList.isEmpty()) {
            setLastError("Bảng giá phải có ít nhất 1 dòng chi tiết.");
            return false;
        }

        for (int i = 0; i < chiTietBangGiaList.size(); i++) {
            ChiTietBangGia chiTietBangGia = chiTietBangGiaList.get(i);
            String prefix = "Dòng chi tiết " + (i + 1) + ": ";
            if (chiTietBangGia == null) {
                setLastError(prefix + "Dữ liệu không hợp lệ.");
                return false;
            }
            if (chiTietBangGia.getLoaiNgay() == null || chiTietBangGia.getLoaiNgay().trim().isEmpty()) {
                setLastError(prefix + "Loại ngày không được rỗng.");
                return false;
            }
            if (chiTietBangGia.getKhungGio() == null || chiTietBangGia.getKhungGio().trim().isEmpty()) {
                setLastError(prefix + "Khung giờ không được rỗng.");
                return false;
            }
            if (!isNonNegative(chiTietBangGia.getGiaTheoGio())
                    || !isNonNegative(chiTietBangGia.getGiaQuaDem())
                    || !isNonNegative(chiTietBangGia.getGiaTheoNgay())
                    || !isNonNegative(chiTietBangGia.getGiaCuoiTuan())
                    || !isNonNegative(chiTietBangGia.getGiaLe())
                    || !isNonNegative(chiTietBangGia.getPhuThu())) {
                setLastError(prefix + "Giá và phụ thu phải lớn hơn hoặc bằng 0.");
                return false;
            }
        }
        return true;
    }

    private boolean insertBangGiaTransaction(Connection con, BangGia bangGia, String loaiNgay) {
        String sql = "INSERT INTO BangGia(tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, loaiNgay);
            stmt.setString(6, bangGia.getTrangThai());
            if (stmt.executeUpdate() <= 0) {
                setLastError("Không thể thêm bảng giá.");
                return false;
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    bangGia.setMaBangGia(rs.getInt(1));
                    return true;
                }
            }
            setLastError("Không lấy được mã bảng giá vừa tạo.");
            return false;
        } catch (SQLException e) {
            System.out.println("Loi them bang gia theo giao dich.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    private boolean updateBangGiaTransaction(Connection con, BangGia bangGia, String loaiNgay) {
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
            if (stmt.executeUpdate() <= 0) {
                setLastError("Không thể cập nhật bảng giá.");
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat bang gia theo giao dich.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    private boolean deleteChiTietByMaBangGia(Connection con, int maBangGia) {
        String sql = "DELETE FROM ChiTietBangGia WHERE maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Loi xoa chi tiet bang gia theo ma bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    private boolean insertChiTietBangGia(Connection con, ChiTietBangGia chiTietBangGia) {
        String sql = "INSERT INTO ChiTietBangGia(maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, chiTietBangGia.getMaBangGia());
            stmt.setString(2, chiTietBangGia.getLoaiNgay());
            stmt.setString(3, chiTietBangGia.getKhungGio());
            stmt.setDouble(4, chiTietBangGia.getGiaTheoGio());
            stmt.setDouble(5, chiTietBangGia.getGiaQuaDem());
            stmt.setDouble(6, chiTietBangGia.getGiaTheoNgay());
            stmt.setDouble(7, chiTietBangGia.getGiaCuoiTuan());
            stmt.setDouble(8, chiTietBangGia.getGiaLe());
            stmt.setDouble(9, chiTietBangGia.getPhuThu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi them chi tiet bang gia theo giao dich.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    private ChiTietBangGia copyChiTietBangGia(ChiTietBangGia source) {
        ChiTietBangGia copy = new ChiTietBangGia();
        copy.setMaChiTietBangGia(source.getMaChiTietBangGia());
        copy.setMaBangGia(source.getMaBangGia());
        copy.setLoaiNgay(source.getLoaiNgay());
        copy.setKhungGio(source.getKhungGio());
        copy.setGiaTheoGio(source.getGiaTheoGio());
        copy.setGiaQuaDem(source.getGiaQuaDem());
        copy.setGiaTheoNgay(source.getGiaTheoNgay());
        copy.setGiaCuoiTuan(source.getGiaCuoiTuan());
        copy.setGiaLe(source.getGiaLe());
        copy.setPhuThu(source.getPhuThu());
        return copy;
    }

    private String buildConflictErrorMessage(BangGiaConflictInfo conflictInfo) {
        if (conflictInfo == null) {
            return "Khoảng thời gian áp dụng đang bị trùng.";
        }
        return "Loại phòng " + safeValue(conflictInfo.getTenLoaiPhong(), String.valueOf(conflictInfo.getMaLoaiPhong()))
                + " đã có bảng giá " + safeValue(conflictInfo.getTenBangGia(), "BG" + conflictInfo.getMaBangGia())
                + " áp dụng từ " + formatDate(conflictInfo.getNgayBatDau())
                + " đến " + formatDate(conflictInfo.getNgayKetThuc())
                + " (" + safeValue(conflictInfo.getTrangThai(), "-") + ").";
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

    private boolean isNonNegative(double value) {
        return value >= 0;
    }

    private String formatDate(Date date) {
        return date == null ? "" : date.toLocalDate().toString();
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void clearLastError() {
        lastErrorMessage = "";
    }

    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}
