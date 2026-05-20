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

/**
 * DAO xử lý các thao tác liên quan đến bảng giá phòng.
 * Lớp này chịu trách nhiệm:
 * - Lấy danh sách bảng giá.
 * - Thêm, cập nhật, xóa bảng giá.
 * - Lưu bảng giá kèm chi tiết giá theo giao dịch.
 * - Kiểm tra trùng khoảng thời gian áp dụng của bảng giá.
 */
public class BangGiaDAO {
    public static final String DEFAULT_LOAI_NGAY = ChiTietBangGia.DEFAULT_LOAI_NGAY;

    private static final String SELECT_BASE =
            "SELECT bg.maBangGia, bg.tenBangGia, bg.maLoaiPhong, bg.ngayBatDau, bg.ngayKetThuc, "
                    + "bg.loaiNgay, bg.trangThai, lp.tenLoaiPhong "
                    + "FROM BangGia bg "
                    + "LEFT JOIN LoaiPhong lp ON bg.maLoaiPhong = lp.maLoaiPhong";

    private String lastErrorMessage = "";
    private final Map<Integer, String> loaiNgayCache = new HashMap<Integer, String>();

    /**
     * Lấy thông báo lỗi gần nhất phát sinh trong DAO.
     *
     * @return nội dung lỗi gần nhất, rỗng nếu chưa có lỗi
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Lấy toàn bộ danh sách bảng giá, sắp xếp bảng giá mới nhất lên trước.
     * @return danh sách bảng giá
     */
    public List<BangGia> getAll() {
        clearLastError();
        // Xóa cache để bảo đảm dữ liệu loại ngày được lấy lại theo kết quả truy vấn mới.
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
                // Ánh xạ từng dòng dữ liệu trong ResultSet thành đối tượng BangGia.
                dsBangGia.add(mapBangGia(rs));
            }
        } catch (SQLException e) {
            System.out.println("Loi truy van danh sach bang gia.");
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return dsBangGia;
    }

    /**
     * Tìm bảng giá theo mã bảng giá.
     * @param maBangGia mã bảng giá cần tìm
     * @return đối tượng BangGia nếu tìm thấy, ngược lại trả về null
     */
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

    /**
     * Tìm kiếm bảng giá theo tên, loại phòng và khoảng ngày áp dụng.
     * @param tenBangGia tên bảng giá cần tìm, có thể rỗng
     * @param maLoaiPhong mã loại phòng cần lọc, có thể rỗng
     * @param from ngày bắt đầu lọc, có thể null
     * @param to ngày kết thúc lọc, có thể null
     * @param loaiNgay loại ngày, hiện chưa dùng trực tiếp trong câu truy vấn
     * @return danh sách bảng giá phù hợp với điều kiện tìm kiếm
     */
    public List<BangGia> search(String tenBangGia, String maLoaiPhong, Date from, Date to, String loaiNgay) {
        clearLastError();
        // Xóa cache để bảo đảm dữ liệu loại ngày được lấy lại theo kết quả truy vấn mới.
        loaiNgayCache.clear();
        List<BangGia> dsBangGia = new ArrayList<BangGia>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return dsBangGia;
        }

        String tenSearch = tenBangGia == null ? "" : tenBangGia.trim();
        String maLoaiSearch = maLoaiPhong == null ? "" : maLoaiPhong.trim();

        String sql = "SELECT bg.maBangGia, bg.tenBangGia, bg.maLoaiPhong, bg.ngayBatDau, bg.ngayKetThuc, "
                + "bg.loaiNgay, bg.trangThai, lp.tenLoaiPhong "
                + "FROM BangGia bg "
                + "LEFT JOIN LoaiPhong lp ON bg.maLoaiPhong = lp.maLoaiPhong "
                + "WHERE (? = '' OR bg.tenBangGia LIKE ?) "
                + "AND (? = '' OR CAST(bg.maLoaiPhong AS NVARCHAR(20)) = ?) "
                + "AND (? IS NULL OR bg.ngayBatDau >= ?) "
                + "AND (? IS NULL OR bg.ngayKetThuc <= ?) "
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

    /**
     * Thêm mới một bảng giá vào cơ sở dữ liệu.
     * @param bangGia thông tin bảng giá cần thêm
     * @param loaiNgay loại ngày áp dụng cho bảng giá
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean insert(BangGia bangGia, String loaiNgay) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || bangGia == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu bảng giá không hợp lệ.");
            return false;
        }
        // Chuẩn hóa loại ngày trước khi validate và lưu vào cơ sở dữ liệu.
        String normalizedLoaiNgay = normalizeLoaiNgay(loaiNgay);
        if (!validateBangGia(bangGia, normalizedLoaiNgay, false)) {
            return false;
        }

        String sql = "INSERT INTO BangGia(tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, normalizedLoaiNgay);
            stmt.setString(6, bangGia.getTrangThai());
            boolean inserted = stmt.executeUpdate() > 0;
            if (inserted) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int maBangGia = rs.getInt(1);
                        bangGia.setMaBangGia(maBangGia);
                        loaiNgayCache.put(maBangGia, normalizedLoaiNgay);
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

    /**
     * Cập nhật thông tin bảng giá đã tồn tại.
     * @param bangGia thông tin bảng giá cần cập nhật
     * @param loaiNgay loại ngày áp dụng cho bảng giá
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean update(BangGia bangGia, String loaiNgay) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || bangGia == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu bảng giá không hợp lệ.");
            return false;
        }
        // Chuẩn hóa loại ngày trước khi validate và lưu vào cơ sở dữ liệu.
        String normalizedLoaiNgay = normalizeLoaiNgay(loaiNgay);
        if (!validateBangGia(bangGia, normalizedLoaiNgay, true)) {
            return false;
        }

        String sql = "UPDATE BangGia SET tenBangGia = ?, maLoaiPhong = ?, ngayBatDau = ?, ngayKetThuc = ?, "
                + "loaiNgay = ?, trangThai = ? WHERE maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, normalizedLoaiNgay);
            stmt.setString(6, bangGia.getTrangThai());
            stmt.setInt(7, bangGia.getMaBangGia());
            boolean updated = stmt.executeUpdate() > 0;
            if (updated) {
                loaiNgayCache.put(bangGia.getMaBangGia(), normalizedLoaiNgay);
            }
            return updated;
        } catch (SQLException e) {
            System.out.println("Loi cap nhat bang gia co ma: " + bangGia.getMaBangGia());
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra khoảng thời gian áp dụng của bảng giá có bị trùng với bảng giá khác hay không.
     * @param maLoaiPhong mã loại phòng cần kiểm tra
     * @param ngayBatDau ngày bắt đầu của khoảng thời gian mới
     * @param ngayKetThuc ngày kết thúc của khoảng thời gian mới
     * @param excludeMaBangGia mã bảng giá cần bỏ qua khi cập nhật, null khi thêm mới
     * @return thông tin bảng giá bị trùng nếu có, ngược lại trả về null
     */
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

        // Câu truy vấn tìm bảng giá có khoảng ngày giao nhau với khoảng ngày đang nhập.
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

    /**
     * Lưu bảng giá cùng danh sách chi tiết bảng giá trong cùng một giao dịch.
     * Nếu bảng giá đã có mã, phương thức sẽ cập nhật bảng giá và xóa chi tiết cũ trước khi thêm chi tiết mới.
     * Nếu bảng giá chưa có mã, phương thức sẽ thêm mới bảng giá rồi thêm các dòng chi tiết.
     * @param bangGia thông tin bảng giá cần lưu
     * @param loaiNgay loại ngày áp dụng
     * @param chiTietBangGiaList danh sách chi tiết giá cần lưu
     * @return true nếu toàn bộ giao dịch thành công, false nếu có lỗi và đã rollback
     */
    public boolean saveWithDetails(BangGia bangGia, String loaiNgay, List<ChiTietBangGia> chiTietBangGiaList) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null || bangGia == null) {
            setLastError(con == null ? "Không thể kết nối cơ sở dữ liệu." : "Dữ liệu bảng giá không hợp lệ.");
            return false;
        }
        // Chuẩn hóa loại ngày trước khi validate và lưu vào cơ sở dữ liệu.
        String normalizedLoaiNgay = normalizeLoaiNgay(loaiNgay);
        if (!validateBangGia(bangGia, normalizedLoaiNgay, bangGia.getMaBangGia() > 0)) {
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
            // Tắt auto-commit để đảm bảo bảng giá và chi tiết bảng giá được lưu như một giao dịch duy nhất.
            previousAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            if (bangGia.getMaBangGia() > 0) {
                if (!updateBangGiaTransaction(con, bangGia, normalizedLoaiNgay)) {
                    con.rollback();
                    return false;
                }
                if (!deleteChiTietByMaBangGia(con, bangGia.getMaBangGia())) {
                    con.rollback();
                    return false;
                }
            } else {
                if (!insertBangGiaTransaction(con, bangGia, normalizedLoaiNgay)) {
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

            // Chỉ commit khi bảng giá và toàn bộ chi tiết đều được lưu thành công.
            con.commit();
            loaiNgayCache.put(bangGia.getMaBangGia(), normalizedLoaiNgay);
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

    /**
     * Xóa bảng giá và các chi tiết bảng giá liên quan.
     * @param maBangGia mã bảng giá cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
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
                // Nếu xóa chi tiết hoặc bảng giá lỗi thì rollback toàn bộ thao tác xóa.
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

    /**
     * Lấy danh sách loại phòng để phục vụ việc chọn loại phòng khi lập bảng giá.
     * @return danh sách loại phòng
     */
    public List<LoaiPhong> getAllLoaiPhong() {
        return new LoaiPhongDAO().getAll();
    }

    /**
     * Lấy loại ngày của bảng giá theo mã bảng giá.
     * Dữ liệu được ưu tiên lấy từ cache để giảm số lần truy vấn cơ sở dữ liệu.
     * @param maBangGia mã bảng giá cần lấy loại ngày
     * @return loại ngày của bảng giá, hoặc giá trị mặc định nếu không tìm thấy
     */
    public String getLoaiNgayByMaBangGia(int maBangGia) {
        // Ưu tiên lấy từ cache nếu mã bảng giá đã từng được truy vấn trước đó.
        if (loaiNgayCache.containsKey(maBangGia)) {
            return loaiNgayCache.get(maBangGia);
        }

        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return DEFAULT_LOAI_NGAY;
        }

        String sql = "SELECT loaiNgay FROM BangGia WHERE maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String loaiNgay = normalizeLoaiNgay(rs.getString("loaiNgay"));
                    loaiNgayCache.put(maBangGia, loaiNgay);
                    return loaiNgay;
                }
            }
        } catch (SQLException e) {
            System.out.println("Loi lay loai ngay cua bang gia: " + maBangGia);
            e.printStackTrace();
            setLastError(e.getMessage());
        }
        return DEFAULT_LOAI_NGAY;
    }

    /**
     * Lấy danh sách chi tiết bảng giá theo mã bảng giá.
     * @param maBangGia mã bảng giá cần lấy chi tiết
     * @return danh sách chi tiết bảng giá
     */
    public List<ChiTietBangGia> getChiTietBangGiaByMaBangGia(int maBangGia) {
        clearLastError();
        List<ChiTietBangGia> details = new ArrayList<ChiTietBangGia>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return details;
        }

        String sql = "SELECT maChiTietBangGia, maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu "
                + "FROM ChiTietBangGia WHERE maBangGia = ? ORDER BY maChiTietBangGia ASC";
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

    /**
     * Lấy chi tiết bảng giá đang áp dụng theo mã bảng giá.
     * @param maBangGia mã bảng giá cần kiểm tra
     * @return chi tiết bảng giá đang áp dụng, hoặc null nếu không có
     */
    public ChiTietBangGia getChiTietBangGiaDangApDung(int maBangGia) {
        return getChiTietBangGiaDangApDung(maBangGia, null);
    }

    /**
     * Lấy chi tiết bảng giá đang áp dụng tại một ngày cụ thể.
     * @param maBangGia mã bảng giá cần kiểm tra
     * @param ngayApDung ngày cần kiểm tra, có thể null nếu không lọc theo ngày
     * @return chi tiết bảng giá đang áp dụng, hoặc null nếu không có
     */
    public ChiTietBangGia getChiTietBangGiaDangApDung(int maBangGia, LocalDate ngayApDung) {
        clearLastError();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            setLastError("Không thể kết nối cơ sở dữ liệu.");
            return null;
        }

        String sql = "SELECT TOP 1 ct.maChiTietBangGia, ct.maBangGia, ct.loaiNgay, ct.khungGio, ct.giaTheoGio, ct.giaQuaDem, ct.giaTheoNgay, ct.giaCuoiTuan, ct.giaLe, ct.phuThu "
                + "FROM ChiTietBangGia ct "
                + "JOIN BangGia bg ON ct.maBangGia = bg.maBangGia "
                + "WHERE ct.maBangGia = ? "
                + "AND bg.trangThai = N'Đang áp dụng' "
                + "AND (? IS NULL OR ? BETWEEN bg.ngayBatDau AND bg.ngayKetThuc) "
                + "ORDER BY ct.maChiTietBangGia ASC";
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

    /**
     * Chuyển dữ liệu từ ResultSet thành đối tượng BangGia.
     * @param rs ResultSet đang trỏ tới dòng dữ liệu bảng giá
     * @return đối tượng BangGia sau khi ánh xạ dữ liệu
     * @throws SQLException nếu lỗi khi đọc dữ liệu từ ResultSet
     */
    private BangGia mapBangGia(ResultSet rs) throws SQLException {
        int maBangGia = rs.getInt("maBangGia");
        // Lưu loại ngày vào cache vì BangGia hiện không nhận trực tiếp trường này trong constructor.
        loaiNgayCache.put(maBangGia, normalizeLoaiNgay(rs.getString("loaiNgay")));
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

    /**
     * Chuyển dữ liệu từ ResultSet thành đối tượng ChiTietBangGia.
     * @param rs ResultSet đang trỏ tới dòng dữ liệu chi tiết bảng giá
     * @return đối tượng ChiTietBangGia sau khi ánh xạ dữ liệu
     * @throws SQLException nếu lỗi khi đọc dữ liệu từ ResultSet
     */
    private ChiTietBangGia mapChiTietBangGia(ResultSet rs) throws SQLException {
        return new ChiTietBangGia(
                rs.getInt("maChiTietBangGia"),
                rs.getInt("maBangGia"),
                normalizeLoaiNgay(rs.getString("loaiNgay")),
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
     * Kiểm tra dữ liệu bảng giá trước khi thêm hoặc cập nhật.
     * @param bangGia bảng giá cần kiểm tra
     * @param loaiNgay loại ngày đã được chuẩn hóa
     * @param updating true nếu đang cập nhật, false nếu đang thêm mới
     * @return true nếu dữ liệu hợp lệ, false nếu dữ liệu không hợp lệ
     */
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

    /**
     * Kiểm tra danh sách chi tiết bảng giá trước khi lưu.
     * @param chiTietBangGiaList danh sách chi tiết bảng giá cần kiểm tra
     * @return true nếu toàn bộ danh sách hợp lệ, false nếu có dòng không hợp lệ
     */
    private boolean validateChiTietList(List<ChiTietBangGia> chiTietBangGiaList) {
        if (chiTietBangGiaList == null || chiTietBangGiaList.isEmpty()) {
            setLastError("Bảng giá phải có ít nhất 1 dòng chi tiết.");
            return false;
        }

        for (int i = 0; i < chiTietBangGiaList.size(); i++) {
            // Kiểm tra từng dòng chi tiết để báo lỗi chính xác theo vị trí dòng.
            ChiTietBangGia chiTietBangGia = chiTietBangGiaList.get(i);
            String prefix = "Dòng chi tiết " + (i + 1) + ": ";
            if (chiTietBangGia == null) {
                setLastError(prefix + "Dữ liệu không hợp lệ.");
                return false;
            }
            chiTietBangGia.setLoaiNgay(normalizeLoaiNgay(chiTietBangGia.getLoaiNgay()));
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

    /**
     * Thêm bảng giá trong giao dịch hiện tại.
     * Phương thức này không tự commit hoặc rollback, việc quản lý giao dịch do phương thức gọi bên ngoài xử lý.
     * @param con kết nối cơ sở dữ liệu đang nằm trong giao dịch
     * @param bangGia bảng giá cần thêm
     * @param loaiNgay loại ngày áp dụng
     * @return true nếu thêm thành công, false nếu thất bại
     */
    private boolean insertBangGiaTransaction(Connection con, BangGia bangGia, String loaiNgay) {
        String sql = "INSERT INTO BangGia(tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, normalizeLoaiNgay(loaiNgay));
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

    /**
     * Cập nhật bảng giá trong giao dịch hiện tại.
     * Phương thức này không tự commit hoặc rollback, việc quản lý giao dịch do phương thức gọi bên ngoài xử lý.
     * @param con kết nối cơ sở dữ liệu đang nằm trong giao dịch
     * @param bangGia bảng giá cần cập nhật
     * @param loaiNgay loại ngày áp dụng
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    private boolean updateBangGiaTransaction(Connection con, BangGia bangGia, String loaiNgay) {
        String sql = "UPDATE BangGia SET tenBangGia = ?, maLoaiPhong = ?, ngayBatDau = ?, ngayKetThuc = ?, "
                + "loaiNgay = ?, trangThai = ? WHERE maBangGia = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bangGia.getTenBangGia());
            stmt.setInt(2, bangGia.getMaLoaiPhong());
            stmt.setDate(3, bangGia.getTuNgay());
            stmt.setDate(4, bangGia.getDenNgay());
            stmt.setString(5, normalizeLoaiNgay(loaiNgay));
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

    /**
     * Xóa toàn bộ chi tiết bảng giá theo mã bảng giá trong giao dịch hiện tại.
     * @param con kết nối cơ sở dữ liệu đang nằm trong giao dịch
     * @param maBangGia mã bảng giá cần xóa chi tiết
     * @return true nếu xóa thành công, false nếu thất bại
     */
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

    /**
     * Thêm một dòng chi tiết bảng giá vào cơ sở dữ liệu.
     * @param con kết nối cơ sở dữ liệu đang nằm trong giao dịch
     * @param chiTietBangGia chi tiết bảng giá cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    private boolean insertChiTietBangGia(Connection con, ChiTietBangGia chiTietBangGia) {
        String sql = "INSERT INTO ChiTietBangGia(maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, chiTietBangGia.getMaBangGia());
            stmt.setString(2, normalizeLoaiNgay(chiTietBangGia.getLoaiNgay()));
            stmt.setString(3, chiTietBangGia.getKhungGio());
            stmt.setDouble(4, chiTietBangGia.getGiaTheoGio());
            stmt.setDouble(5, chiTietBangGia.getGiaQuaDem());
            stmt.setDouble(6, chiTietBangGia.getGiaTheoNgay());
            stmt.setDouble(7, chiTietBangGia.getGiaCuoiTuan());
            stmt.setDouble(8, chiTietBangGia.getGiaLe());
            stmt.setDouble(9, 0d);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Loi them chi tiet bang gia theo giao dich.");
            e.printStackTrace();
            setLastError(e.getMessage());
            return false;
        }
    }

    /**
     * Tạo bản sao của chi tiết bảng giá trước khi lưu.
     * Việc tạo bản sao giúp tránh thay đổi trực tiếp đối tượng gốc đang được truyền từ giao diện hoặc lớp gọi.
     * @param source chi tiết bảng giá gốc
     * @return bản sao của chi tiết bảng giá
     */
    private ChiTietBangGia copyChiTietBangGia(ChiTietBangGia source) {
        ChiTietBangGia copy = new ChiTietBangGia();
        copy.setMaChiTietBangGia(source.getMaChiTietBangGia());
        copy.setMaBangGia(source.getMaBangGia());
        copy.setLoaiNgay(normalizeLoaiNgay(source.getLoaiNgay()));
        copy.setKhungGio(source.getKhungGio());
        copy.setGiaTheoGio(source.getGiaTheoGio());
        copy.setGiaQuaDem(source.getGiaQuaDem());
        copy.setGiaTheoNgay(source.getGiaTheoNgay());
        copy.setGiaCuoiTuan(source.getGiaCuoiTuan());
        copy.setGiaLe(source.getGiaLe());
        copy.setPhuThu(0d);
        return copy;
    }

    /**
     * Tạo thông báo lỗi khi khoảng thời gian áp dụng bảng giá bị trùng.
     * @param conflictInfo thông tin bảng giá đang bị trùng thời gian
     * @return thông báo lỗi để hiển thị cho người dùng
     */
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

    /**
     * Kiểm tra loại phòng có tồn tại trong cơ sở dữ liệu hay không.
     * @param maLoaiPhong mã loại phòng cần kiểm tra
     * @return true nếu loại phòng tồn tại, false nếu không tồn tại hoặc có lỗi
     */
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

    /**
     * Kiểm tra giá trị số có lớn hơn hoặc bằng 0 hay không.
     * @param value giá trị cần kiểm tra
     * @return true nếu value >= 0
     */
    private boolean isNonNegative(double value) {
        return value >= 0;
    }

    /**
     * Chuẩn hóa loại ngày, dùng giá trị mặc định nếu dữ liệu rỗng.
     * @param loaiNgay loại ngày cần chuẩn hóa
     * @return loại ngày hợp lệ sau khi chuẩn hóa
     */
    private String normalizeLoaiNgay(String loaiNgay) {
        return loaiNgay == null || loaiNgay.trim().isEmpty() ? DEFAULT_LOAI_NGAY : loaiNgay.trim();
    }

    /**
     * Chuyển ngày SQL sang chuỗi theo định dạng LocalDate.
     *
     * @param date ngày cần chuyển đổi
     * @return chuỗi ngày, hoặc rỗng nếu date null
     */
    private String formatDate(Date date) {
        return date == null ? "" : date.toLocalDate().toString();
    }

    /**
     * Lấy giá trị chuỗi an toàn, dùng fallback nếu chuỗi null hoặc rỗng.
     *
     * @param value giá trị cần kiểm tra
     * @param fallback giá trị thay thế khi value không hợp lệ
     * @return value đã trim hoặc fallback
     */
    private String safeValue(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    /**
     * Xóa thông báo lỗi gần nhất trước khi thực hiện thao tác mới.
     */
    private void clearLastError() {
        lastErrorMessage = "";
    }

    /**
     * Lưu thông báo lỗi gần nhất để lớp gọi có thể lấy ra hiển thị.
     *
     * @param message nội dung lỗi cần lưu
     */
    private void setLastError(String message) {
        lastErrorMessage = message == null ? "" : message;
    }
}