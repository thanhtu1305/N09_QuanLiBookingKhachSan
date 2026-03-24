package entity;

public class ChiTietHoaDon {
    private String maChiTietHoaDon;
    private String maHoaDon;
    private String loaiDong;
    private String dienGiai;
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private int thuTuHienThi;
    private String ghiChu;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(String maChiTietHoaDon, String maHoaDon, String loaiDong, String dienGiai,
                         int soLuong, double donGia, double thanhTien, int thuTuHienThi, String ghiChu) {
        this.maChiTietHoaDon = maChiTietHoaDon;
        this.maHoaDon = maHoaDon;
        this.loaiDong = loaiDong;
        this.dienGiai = dienGiai;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.thuTuHienThi = thuTuHienThi;
        this.ghiChu = ghiChu;
    }

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

    public String getLoaiDong() {
        return loaiDong;
    }

    public void setLoaiDong(String loaiDong) {
        this.loaiDong = loaiDong;
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

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
