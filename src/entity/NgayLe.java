package entity;

import java.io.Serializable;
import java.time.LocalDate;

public class NgayLe implements Serializable {
    private static final long serialVersionUID = 1L;

    private int maNgayLe;
    private String tenNgayLe;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String loaiNgay;
    private double heSoPhuThu;
    private String trangThai;
    private String ghiChu;

    public NgayLe() {
    }

    public NgayLe(int maNgayLe, String tenNgayLe, LocalDate ngayBatDau, LocalDate ngayKetThuc,
                  String loaiNgay, double heSoPhuThu, String trangThai, String ghiChu) {
        this.maNgayLe = maNgayLe;
        this.tenNgayLe = tenNgayLe;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.loaiNgay = loaiNgay;
        this.heSoPhuThu = heSoPhuThu;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public int getMaNgayLe() {
        return maNgayLe;
    }

    public void setMaNgayLe(int maNgayLe) {
        this.maNgayLe = maNgayLe;
    }

    public String getTenNgayLe() {
        return tenNgayLe;
    }

    public void setTenNgayLe(String tenNgayLe) {
        this.tenNgayLe = tenNgayLe;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getLoaiNgay() {
        return loaiNgay;
    }

    public void setLoaiNgay(String loaiNgay) {
        this.loaiNgay = loaiNgay;
    }

    public double getHeSoPhuThu() {
        return heSoPhuThu;
    }

    public void setHeSoPhuThu(double heSoPhuThu) {
        this.heSoPhuThu = heSoPhuThu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
