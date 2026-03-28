package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ChiTietDatPhong implements Serializable {
    private static final long serialVersionUID = 1L;

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

    // Join/display fields
    private String soPhong;
    private String tenLoaiPhong;

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

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    // Alias methods de tranh xung dot giua GUI va DAO
    public String getLoaiPhong() {
        return isBlank(tenLoaiPhong) ? maLoaiPhong : tenLoaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.tenLoaiPhong = loaiPhong;
    }

    public String getPhong() {
        return isBlank(soPhong) ? maPhong : soPhong;
    }

    public void setPhong(String phong) {
        this.soPhong = phong;
    }

    public double getGiaPhong() {
        return giaApDung;
    }

    public void setGiaPhong(double giaPhong) {
        this.giaApDung = giaPhong;
    }

    public double getTienDatCoc() {
        return tienDatCocChiTiet;
    }

    public void setTienDatCoc(double tienDatCoc) {
        this.tienDatCocChiTiet = tienDatCoc;
    }

    public long getSoDem() {
        if (checkInDuKien == null || checkOutDuKien == null) {
            return 0L;
        }
        long nights = ChronoUnit.DAYS.between(checkInDuKien, checkOutDuKien);
        return Math.max(nights, 0L);
    }

    public double getThanhTienTamTinh() {
        long soDem = getSoDem();
        return soDem <= 0 ? giaApDung : giaApDung * soDem;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "ChiTietDatPhong{" +
                "maChiTietDatPhong='" + maChiTietDatPhong + '\'' +
                ", maDatPhong='" + maDatPhong + '\'' +
                ", loaiPhong='" + getLoaiPhong() + '\'' +
                ", phong='" + getPhong() + '\'' +
                ", checkInDuKien=" + checkInDuKien +
                ", checkOutDuKien=" + checkOutDuKien +
                ", soNguoi=" + soNguoi +
                ", giaApDung=" + giaApDung +
                ", tienDatCocChiTiet=" + tienDatCocChiTiet +
                ", trangThaiChiTiet='" + trangThaiChiTiet + '\'' +
                '}';
    }
}
