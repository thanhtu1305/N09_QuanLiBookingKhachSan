package entity;

import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private String maDatPhong;
    private String maKhachHang;
    private String ngayLap;
    private double tongTienPhong;
    private double tongTienDichVu;
    private double tongPhuThu;
    private double tongGiamGia;
    private double tongTruDatCoc;
    private double tongThanhToan;
    private String trangThai;
    private final List<ChiTietHoaDon> chiTietHoaDons = new ArrayList<ChiTietHoaDon>();

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String maDatPhong, String maKhachHang, String ngayLap, String trangThai) {
        this.maHoaDon = maHoaDon;
        this.maDatPhong = maDatPhong;
        this.maKhachHang = maKhachHang;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
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

    public String getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(String ngayLap) {
        this.ngayLap = ngayLap;
    }

    public double getTongTienPhong() {
        return tongTienPhong;
    }

    public void setTongTienPhong(double tongTienPhong) {
        this.tongTienPhong = tongTienPhong;
    }

    public double getTongTienDichVu() {
        return tongTienDichVu;
    }

    public void setTongTienDichVu(double tongTienDichVu) {
        this.tongTienDichVu = tongTienDichVu;
    }

    public double getTongPhuThu() {
        return tongPhuThu;
    }

    public void setTongPhuThu(double tongPhuThu) {
        this.tongPhuThu = tongPhuThu;
    }

    public double getTongGiamGia() {
        return tongGiamGia;
    }

    public void setTongGiamGia(double tongGiamGia) {
        this.tongGiamGia = tongGiamGia;
    }

    public double getTongTruDatCoc() {
        return tongTruDatCoc;
    }

    public void setTongTruDatCoc(double tongTruDatCoc) {
        this.tongTruDatCoc = tongTruDatCoc;
    }

    public double getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(double tongThanhToan) {
        this.tongThanhToan = tongThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public List<ChiTietHoaDon> getChiTietHoaDons() {
        return chiTietHoaDons;
    }
}
