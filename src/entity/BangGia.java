package entity;

import java.sql.Date;

public class BangGia {
    private int maBangGia;
    private String tenBangGia;
    private int maLoaiPhong;
    private Date tuNgay;
    private Date denNgay;
    private String trangThai;
    private String ghiChu;
    private String tenLoaiPhong;

    public BangGia() {
    }

    public BangGia(int maBangGia, String tenBangGia, int maLoaiPhong, Date tuNgay, Date denNgay,
                   String trangThai, String ghiChu) {
        this(maBangGia, tenBangGia, maLoaiPhong, tuNgay, denNgay, trangThai, ghiChu, null);
    }

    public BangGia(int maBangGia, String tenBangGia, int maLoaiPhong, Date tuNgay, Date denNgay,
                   String trangThai, String ghiChu, String tenLoaiPhong) {
        this.maBangGia = maBangGia;
        this.tenBangGia = tenBangGia;
        this.maLoaiPhong = maLoaiPhong;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public BangGia(String tenBangGia, int maLoaiPhong, Date tuNgay, Date denNgay, String trangThai, String ghiChu) {
        this(0, tenBangGia, maLoaiPhong, tuNgay, denNgay, trangThai, ghiChu, null);
    }

    public int getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(int maBangGia) {
        this.maBangGia = maBangGia;
    }

    public String getTenBangGia() {
        return tenBangGia;
    }

    public void setTenBangGia(String tenBangGia) {
        this.tenBangGia = tenBangGia;
    }

    public int getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(int maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public Date getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(Date tuNgay) {
        this.tuNgay = tuNgay;
    }

    public Date getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(Date denNgay) {
        this.denNgay = denNgay;
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

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    @Override
    public String toString() {
        return "BangGia{" +
                "maBangGia=" + maBangGia +
                ", tenBangGia='" + tenBangGia + '\'' +
                ", maLoaiPhong=" + maLoaiPhong +
                ", tuNgay=" + tuNgay +
                ", denNgay=" + denNgay +
                ", trangThai='" + trangThai + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                ", tenLoaiPhong='" + tenLoaiPhong + '\'' +
                '}';
    }
}
