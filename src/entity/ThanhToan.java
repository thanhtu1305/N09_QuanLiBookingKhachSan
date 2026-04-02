package entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ThanhToan implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maThanhToan;
    private String maHoaDon;
    private String maLuuTru;
    private String maDatPhong;
    private String maKhachHang;
    private String maNhanVien;

    private String maHoSo;
    private String khachHang;
    private String soPhong;
    private String soDienThoai;
    private String email;
    private String cccdPassport;
    private String nguoiThu;
    private String phuongThuc;
    private String soThamChieu;
    private String thongTinThanhToanKetHop;
    private String ghiChu;
    private String trangThai;

    private Timestamp ngayLap;
    private Timestamp ngayThanhToan;
    private Timestamp ngayNhanPhong;
    private Timestamp ngayTraPhong;

    private double tienPhong;
    private double tienDichVu;
    private double phuThu;
    private double giamGia;
    private double tienCoc;
    private double tienCocTru;
    private double tienCocDaHoan;
    private double soTienDaThanhToan;

    private final List<ChiTietDong> chiTiet = new ArrayList<ChiTietDong>();
    private final List<GiaoDichThanhToan> giaoDichThanhToans = new ArrayList<GiaoDichThanhToan>();

    public ThanhToan() {
    }

    public ThanhToan(String maThanhToan, String maHoaDon, String phuongThuc) {
        this.maThanhToan = maThanhToan;
        this.maHoaDon = maHoaDon;
        this.phuongThuc = phuongThuc;
    }

    public String getMaThanhToan() {
        return maThanhToan;
    }

    public void setMaThanhToan(String maThanhToan) {
        this.maThanhToan = maThanhToan;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(String maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public String getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(String maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        this.maHoSo = maHoSo;
    }

    public String getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(String khachHang) {
        this.khachHang = khachHang;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCccdPassport() {
        return cccdPassport;
    }

    public void setCccdPassport(String cccdPassport) {
        this.cccdPassport = cccdPassport;
    }

    public String getNguoiThu() {
        return nguoiThu;
    }

    public void setNguoiThu(String nguoiThu) {
        this.nguoiThu = nguoiThu;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public String getSoThamChieu() {
        return soThamChieu;
    }

    public void setSoThamChieu(String soThamChieu) {
        this.soThamChieu = soThamChieu;
    }

    public String getThongTinThanhToanKetHop() {
        return thongTinThanhToanKetHop;
    }

    public void setThongTinThanhToanKetHop(String thongTinThanhToanKetHop) {
        this.thongTinThanhToanKetHop = thongTinThanhToanKetHop;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public Timestamp getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(Timestamp ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public Timestamp getNgayNhanPhong() {
        return ngayNhanPhong;
    }

    public void setNgayNhanPhong(Timestamp ngayNhanPhong) {
        this.ngayNhanPhong = ngayNhanPhong;
    }

    public Timestamp getNgayTraPhong() {
        return ngayTraPhong;
    }

    public void setNgayTraPhong(Timestamp ngayTraPhong) {
        this.ngayTraPhong = ngayTraPhong;
    }

    public double getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(double tienPhong) {
        this.tienPhong = tienPhong;
    }

    public double getTienDichVu() {
        return tienDichVu;
    }

    public void setTienDichVu(double tienDichVu) {
        this.tienDichVu = tienDichVu;
    }

    public double getPhuThu() {
        return phuThu;
    }

    public void setPhuThu(double phuThu) {
        this.phuThu = phuThu;
    }

    public double getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(double giamGia) {
        this.giamGia = giamGia;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public double getTienCocTru() {
        return tienCocTru;
    }

    public void setTienCocTru(double tienCocTru) {
        this.tienCocTru = tienCocTru;
    }

    public double getTienCocDaHoan() {
        return tienCocDaHoan;
    }

    public void setTienCocDaHoan(double tienCocDaHoan) {
        this.tienCocDaHoan = tienCocDaHoan;
    }

    public double getSoTienDaThanhToan() {
        return soTienDaThanhToan;
    }

    public void setSoTienDaThanhToan(double soTienDaThanhToan) {
        this.soTienDaThanhToan = soTienDaThanhToan;
    }

    public List<ChiTietDong> getChiTiet() {
        return chiTiet;
    }

    public List<GiaoDichThanhToan> getGiaoDichThanhToans() {
        return giaoDichThanhToans;
    }

    public double getTongTruocDatCoc() {
        return Math.max(0d, tienPhong + tienDichVu + phuThu - giamGia);
    }

    public double getTongPhaiThu() {
        return Math.max(0d, getTongTruocDatCoc() - tienCocTru);
    }

    public double getConPhaiThu() {
        return Math.max(0d, getTongPhaiThu() - soTienDaThanhToan);
    }

    public double getSoTienCoTheHoanCoc() {
        return Math.max(0d, tienCoc - tienCocTru - tienCocDaHoan);
    }

    public String getPhongVaSoDong() {
        int soDong = chiTiet.size();
        String phong = soPhong == null || soPhong.trim().isEmpty() ? "-" : soPhong;
        return phong + " / " + soDong + " dòng";
    }

    public static String formatMoney(double value) {
        return String.format(Locale.US, "%,.0f", value).replace(',', '.');
    }

    public static final class ChiTietDong implements Serializable {
        private static final long serialVersionUID = 1L;

        private String maChiTietHoaDon;
        private String maHoaDon;
        private String loaiChiPhi;
        private String dienGiai;
        private int soLuong;
        private double donGia;
        private double thanhTien;

        public String getMaChiTietHoaDon() {
            return maChiTietHoaDon;
        }

        public void setMaChiTietHoaDon(String maChiTietHoaDon) {
            this.maChiTietHoaDon = maChiTietHoaDon;
        }

        public String getMaHoaDon() {
            return maHoaDon;
        }

        public void setMaHoaDon(String maHoaDon) {
            this.maHoaDon = maHoaDon;
        }

        public String getLoaiChiPhi() {
            return loaiChiPhi;
        }

        public void setLoaiChiPhi(String loaiChiPhi) {
            this.loaiChiPhi = loaiChiPhi;
        }

        public String getDienGiai() {
            return dienGiai;
        }

        public void setDienGiai(String dienGiai) {
            this.dienGiai = dienGiai;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public void setSoLuong(int soLuong) {
            this.soLuong = soLuong;
        }

        public double getDonGia() {
            return donGia;
        }

        public void setDonGia(double donGia) {
            this.donGia = donGia;
        }

        public double getThanhTien() {
            return thanhTien;
        }

        public void setThanhTien(double thanhTien) {
            this.thanhTien = thanhTien;
        }
    }

    public static final class GiaoDichThanhToan implements Serializable {
        private static final long serialVersionUID = 1L;

        private String maThanhToan;
        private String loaiGiaoDich;
        private String phuongThuc;
        private String soThamChieu;
        private Timestamp ngayThanhToan;
        private double soTien;
        private String ghiChu;

        public String getMaThanhToan() {
            return maThanhToan;
        }

        public void setMaThanhToan(String maThanhToan) {
            this.maThanhToan = maThanhToan;
        }

        public String getLoaiGiaoDich() {
            return loaiGiaoDich;
        }

        public void setLoaiGiaoDich(String loaiGiaoDich) {
            this.loaiGiaoDich = loaiGiaoDich;
        }

        public String getPhuongThuc() {
            return phuongThuc;
        }

        public void setPhuongThuc(String phuongThuc) {
            this.phuongThuc = phuongThuc;
        }

        public String getSoThamChieu() {
            return soThamChieu;
        }

        public void setSoThamChieu(String soThamChieu) {
            this.soThamChieu = soThamChieu;
        }

        public Timestamp getNgayThanhToan() {
            return ngayThanhToan;
        }

        public void setNgayThanhToan(Timestamp ngayThanhToan) {
            this.ngayThanhToan = ngayThanhToan;
        }

        public double getSoTien() {
            return soTien;
        }

        public void setSoTien(double soTien) {
            this.soTien = soTien;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }
    }
}
