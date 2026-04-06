package entity;

import java.time.LocalDate;

public class DatPhongConflictInfo {
    private int maDatPhong;
    private int maChiTietDatPhong;
    private int maLuuTru;
    private String tenKhachHang;
    private String soPhong;
    private LocalDate ngayNhanPhong;
    private LocalDate ngayTraPhong;
    private String trangThai;

    public int getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(int maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public int getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(int maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public int getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(int maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
