package entity;

import java.time.LocalDate;

public class ChiTietDatPhong {
    private String maChiTietDatPhong;
    private String maDatPhong;
    private String maLoaiPhong;
    private String maPhong;
    private String maBangGia;
    private String maChiTietBangGia;
    private LocalDate checkInDuKien;
    private LocalDate checkOutDuKien;
    private int soNguoi;
    private double giaApDung;
    private double tienDatCocChiTiet;
    private String trangThaiChiTiet;
    private String yeuCauKhac;
    private String ghiChu;

    public ChiTietDatPhong() {
    }

    public ChiTietDatPhong(String maChiTietDatPhong, String maDatPhong, String maLoaiPhong, String maPhong,
                           String maBangGia, String maChiTietBangGia, LocalDate checkInDuKien,
                           LocalDate checkOutDuKien, int soNguoi, double giaApDung,
                           double tienDatCocChiTiet, String trangThaiChiTiet, String yeuCauKhac, String ghiChu) {
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.maDatPhong = maDatPhong;
        this.maLoaiPhong = maLoaiPhong;
        this.maPhong = maPhong;
        this.maBangGia = maBangGia;
        this.maChiTietBangGia = maChiTietBangGia;
        this.checkInDuKien = checkInDuKien;
        this.checkOutDuKien = checkOutDuKien;
        this.soNguoi = soNguoi;
        this.giaApDung = giaApDung;
        this.tienDatCocChiTiet = tienDatCocChiTiet;
        this.trangThaiChiTiet = trangThaiChiTiet;
        this.yeuCauKhac = yeuCauKhac;
        this.ghiChu = ghiChu;
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

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public String getMaChiTietBangGia() {
        return maChiTietBangGia;
    }

    public void setMaChiTietBangGia(String maChiTietBangGia) {
        this.maChiTietBangGia = maChiTietBangGia;
    }

    public LocalDate getCheckInDuKien() {
        return checkInDuKien;
    }

    public void setCheckInDuKien(LocalDate checkInDuKien) {
        this.checkInDuKien = checkInDuKien;
    }

    public LocalDate getCheckOutDuKien() {
        return checkOutDuKien;
    }

    public void setCheckOutDuKien(LocalDate checkOutDuKien) {
        this.checkOutDuKien = checkOutDuKien;
    }

    public int getSoNguoi() {
        return soNguoi;
    }

    public void setSoNguoi(int soNguoi) {
        this.soNguoi = soNguoi;
    }

    public double getGiaApDung() {
        return giaApDung;
    }

    public void setGiaApDung(double giaApDung) {
        this.giaApDung = giaApDung;
    }

    public double getTienDatCocChiTiet() {
        return tienDatCocChiTiet;
    }

    public void setTienDatCocChiTiet(double tienDatCocChiTiet) {
        this.tienDatCocChiTiet = tienDatCocChiTiet;
    }

    public String getTrangThaiChiTiet() {
        return trangThaiChiTiet;
    }

    public void setTrangThaiChiTiet(String trangThaiChiTiet) {
        this.trangThaiChiTiet = trangThaiChiTiet;
    }

    public String getYeuCauKhac() {
        return yeuCauKhac;
    }

    public void setYeuCauKhac(String yeuCauKhac) {
        this.yeuCauKhac = yeuCauKhac;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
