package entity;

public class BangGia {
    private String maBangGia;
    private String maLoaiPhong;
    private double donGia;

    public BangGia() {
    }

    public BangGia(String maBangGia, String maLoaiPhong, double donGia) {
        this.maBangGia = maBangGia;
        this.maLoaiPhong = maLoaiPhong;
        this.donGia = donGia;
    }

    public String getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    @Override
    public String toString() {
        return "BangGia{" +
                "maBangGia='" + maBangGia + '\'' +
                ", maLoaiPhong='" + maLoaiPhong + '\'' +
                ", donGia=" + donGia +
                '}';
    }
}
