package entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LuuTru implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maLuuTru;
    private String maChiTietDatPhong;
    private String maDatPhong;
    private String maPhong;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int soNguoi;
    private double giaPhong;
    private double tienCoc;
    private String trangThai;

    // Join/display fields
    private String soPhong;
    private String tang;
    private String tenLoaiPhong;
    private String tenKhachHang;
    private String soDienThoaiKhach;
    private String trangThaiDatPhong;

    public LuuTru() {
    }

    public LuuTru(String maLuuTru, String maChiTietDatPhong, String trangThai) {
        this.maLuuTru = maLuuTru;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.trangThai = trangThai;
    }

    public String getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(String maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(String maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public int getSoNguoi() {
        return soNguoi;
    }

    public void setSoNguoi(int soNguoi) {
        this.soNguoi = soNguoi;
    }

    public double getGiaPhong() {
        return giaPhong;
    }

    public void setGiaPhong(double giaPhong) {
        this.giaPhong = giaPhong;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
    }

    public String getTang() {
        return tang;
    }

    public void setTang(String tang) {
        this.tang = tang;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
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

    public String getTrangThaiDatPhong() {
        return trangThaiDatPhong;
    }

    public void setTrangThaiDatPhong(String trangThaiDatPhong) {
        this.trangThaiDatPhong = trangThaiDatPhong;
    }

    // Alias methods để GUI/DAO gọi linh hoạt
    public String getMaHoSo() {
        return maLuuTru;
    }

    public void setMaHoSo(String maHoSo) {
        this.maLuuTru = maHoSo;
    }

    public String getKhachHang() {
        return tenKhachHang;
    }

    public void setKhachHang(String khachHang) {
        this.tenKhachHang = khachHang;
    }

    public String getLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.tenLoaiPhong = loaiPhong;
    }

    public String getPhong() {
        return soPhong == null || soPhong.trim().isEmpty() ? maPhong : soPhong;
    }

    public void setPhong(String phong) {
        this.soPhong = phong;
    }

    public boolean isDaCheckOut() {
        return checkOut != null;
    }

    @Override
    public String toString() {
        return "LuuTru{" +
                "maLuuTru='" + maLuuTru + '\'' +
                ", maChiTietDatPhong='" + maChiTietDatPhong + '\'' +
                ", maDatPhong='" + maDatPhong + '\'' +
                ", phong='" + getPhong() + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
