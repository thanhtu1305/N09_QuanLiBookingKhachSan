package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatPhong implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maDatPhong;
    private String maKhachHang;
    private String maNhanVien;
    private String maBangGia;
    private LocalDate ngayDat;
    private LocalDate ngayNhanPhong;
    private LocalDate ngayTraPhong;
    private int soLuongPhong;
    private int soNguoi;
    private double tienCoc;
    private String nguonDatPhong;
    private double tongTienDatCoc;
    private String trangThaiDatPhong;
    private String ghiChu;

    // Join/display fields
    private String tenKhachHang;
    private String soDienThoaiKhach;
    private String cccdPassportKhach;

    private final List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<ChiTietDatPhong>();

    public DatPhong() {
    }

    public DatPhong(String maDatPhong, String maKhachHang, LocalDate ngayDat, String nguonDatPhong,
                    double tongTienDatCoc, String trangThaiDatPhong, String ghiChu) {
        this.maDatPhong = maDatPhong;
        this.maKhachHang = maKhachHang;
        this.ngayDat = ngayDat;
        this.nguonDatPhong = nguonDatPhong;
        this.tongTienDatCoc = tongTienDatCoc;
        this.trangThaiDatPhong = trangThaiDatPhong;
        this.ghiChu = ghiChu;
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

    public String getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public LocalDate getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDate ngayDat) {
        this.ngayDat = ngayDat;
    }

    public LocalDate getNgayNhanPhong() {
        return ngayNhanPhong;
    }

    public void setNgayNhanPhong(LocalDate ngayNhanPhong) {
        this.ngayNhanPhong = ngayNhanPhong;
    }

    public LocalDate getNgayTraPhong() {
        return ngayTraPhong;
    }

    public void setNgayTraPhong(LocalDate ngayTraPhong) {
        this.ngayTraPhong = ngayTraPhong;
    }

    public int getSoLuongPhong() {
        return soLuongPhong;
    }

    public void setSoLuongPhong(int soLuongPhong) {
        this.soLuongPhong = soLuongPhong;
    }

    public int getSoNguoi() {
        return soNguoi;
    }

    public void setSoNguoi(int soNguoi) {
        this.soNguoi = soNguoi;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public String getNguonDatPhong() {
        return nguonDatPhong;
    }

    public void setNguonDatPhong(String nguonDatPhong) {
        this.nguonDatPhong = nguonDatPhong;
    }

    public double getTongTienDatCoc() {
        return tongTienDatCoc;
    }

    public void setTongTienDatCoc(double tongTienDatCoc) {
        this.tongTienDatCoc = tongTienDatCoc;
    }

    public String getTrangThaiDatPhong() {
        return trangThaiDatPhong;
    }

    public void setTrangThaiDatPhong(String trangThaiDatPhong) {
        this.trangThaiDatPhong = trangThaiDatPhong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoDienThoaiKhach() {
        return soDienThoaiKhach;
    }

    public void setSoDienThoaiKhach(String soDienThoaiKhach) {
        this.soDienThoaiKhach = soDienThoaiKhach;
    }

    public String getCccdPassportKhach() {
        return cccdPassportKhach;
    }

    public void setCccdPassportKhach(String cccdPassportKhach) {
        this.cccdPassportKhach = cccdPassportKhach;
    }

    public List<ChiTietDatPhong> getChiTietDatPhongs() {
        return chiTietDatPhongs;
    }

    public void setChiTietDatPhongs(List<ChiTietDatPhong> details) {
        this.chiTietDatPhongs.clear();
        if (details != null) {
            this.chiTietDatPhongs.addAll(details);
        }
    }

    // Alias methods để GUI và DAO dùng linh hoạt
    public String getTrangThai() {
        return trangThaiDatPhong;
    }

    public void setTrangThai(String trangThai) {
        this.trangThaiDatPhong = trangThai;
    }

    public String getNguonDat() {
        return nguonDatPhong;
    }

    public void setNguonDat(String nguonDat) {
        this.nguonDatPhong = nguonDat;
    }

    public String getTenKhach() {
        return tenKhachHang;
    }

    public void setTenKhach(String tenKhach) {
        this.tenKhachHang = tenKhach;
    }

    public String getSoDienThoai() {
        return soDienThoaiKhach;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoaiKhach = soDienThoai;
    }

    public String getCccdPassport() {
        return cccdPassportKhach;
    }

    public void setCccdPassport(String cccdPassport) {
        this.cccdPassportKhach = cccdPassport;
    }

    public int getTongSoNguoiChiTiet() {
        if (chiTietDatPhongs.isEmpty()) {
            return soNguoi;
        }
        int total = 0;
        for (ChiTietDatPhong detail : chiTietDatPhongs) {
            total += Math.max(detail.getSoNguoi(), 0);
        }
        return total;
    }

    public double tinhTongCocChiTiet() {
        double total = 0d;
        for (ChiTietDatPhong detail : chiTietDatPhongs) {
            total += Math.max(detail.getTienDatCocChiTiet(), 0d);
        }
        return total;
    }

    public void dongBoTongTienTuChiTiet() {
        if (!chiTietDatPhongs.isEmpty()) {
            this.soLuongPhong = chiTietDatPhongs.size();
            this.soNguoi = getTongSoNguoiChiTiet();
            this.tongTienDatCoc = tinhTongCocChiTiet();
            if (this.tienCoc <= 0) {
                this.tienCoc = this.tongTienDatCoc;
            }
        }
    }

    @Override
    public String toString() {
        return "DatPhong{" +
                "maDatPhong='" + maDatPhong + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", ngayDat=" + ngayDat +
                ", ngayNhanPhong=" + ngayNhanPhong +
                ", ngayTraPhong=" + ngayTraPhong +
                ", soLuongPhong=" + soLuongPhong +
                ", soNguoi=" + soNguoi +
                ", tienCoc=" + tienCoc +
                ", nguonDatPhong='" + nguonDatPhong + '\'' +
                ", tongTienDatCoc=" + tongTienDatCoc +
                ", trangThaiDatPhong='" + trangThaiDatPhong + '\'' +
                ", soDongChiTiet=" + chiTietDatPhongs.size() +
                '}';
    }
}
