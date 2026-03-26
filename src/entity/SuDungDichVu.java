package entity;

public class SuDungDichVu {
    private int maSuDung;
    private int maLuuTru;
    private int maDichVu;
    private int soLuong;
    private double donGia;
    private double thanhTien;
    private String tenDichVu;
    private String donVi;

    public SuDungDichVu() {
    }

    public SuDungDichVu(int maSuDung, int maLuuTru, int maDichVu, int soLuong, double donGia) {
        this.maSuDung = maSuDung;
        this.maLuuTru = maLuuTru;
        this.maDichVu = maDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
    }

    public SuDungDichVu(int maLuuTru, int maDichVu, int soLuong, double donGia) {
        this(0, maLuuTru, maDichVu, soLuong, donGia);
    }

    public int getMaSuDung() {
        return maSuDung;
    }

    public void setMaSuDung(int maSuDung) {
        this.maSuDung = maSuDung;
    }

    public int getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(int maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public int getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(int maDichVu) {
        this.maDichVu = maDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        this.thanhTien = soLuong * donGia;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    @Override
    public String toString() {
        return "SuDungDichVu{" +
                "maSuDung=" + maSuDung +
                ", maLuuTru=" + maLuuTru +
                ", maDichVu=" + maDichVu +
                ", soLuong=" + soLuong +
                ", donGia=" + donGia +
                ", thanhTien=" + thanhTien +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", donVi='" + donVi + '\'' +
                '}';
    }
}
