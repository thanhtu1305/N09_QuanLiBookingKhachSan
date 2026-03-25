package entity;

public class TienNghi {
    private int maTienNghi;
    private String tenTienNghi;
    private String nhomTienNghi;
    private String trangThai;
    private int uuTien;
    private String moTa;

    public TienNghi() {
    }

    public TienNghi(int maTienNghi, String tenTienNghi, String nhomTienNghi, String trangThai, int uuTien, String moTa) {
        this.maTienNghi = maTienNghi;
        this.tenTienNghi = tenTienNghi;
        this.nhomTienNghi = nhomTienNghi;
        this.trangThai = trangThai;
        this.uuTien = uuTien;
        this.moTa = moTa;
    }

    public TienNghi(String tenTienNghi, String nhomTienNghi, String trangThai, int uuTien, String moTa) {
        this(0, tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa);
    }

    public int getMaTienNghi() {
        return maTienNghi;
    }

    public void setMaTienNghi(int maTienNghi) {
        this.maTienNghi = maTienNghi;
    }

    public String getTenTienNghi() {
        return tenTienNghi;
    }

    public void setTenTienNghi(String tenTienNghi) {
        this.tenTienNghi = tenTienNghi;
    }

    public String getNhomTienNghi() {
        return nhomTienNghi;
    }

    public void setNhomTienNghi(String nhomTienNghi) {
        this.nhomTienNghi = nhomTienNghi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public int getUuTien() {
        return uuTien;
    }

    public void setUuTien(int uuTien) {
        this.uuTien = uuTien;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return tenTienNghi;
    }
}
