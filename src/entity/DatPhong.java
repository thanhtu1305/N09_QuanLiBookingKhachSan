package entity;

public class DatPhong {
    private String maDatPhong;
    private String maKhachHang;
    private String maPhong;

    public DatPhong() {
    }

    public DatPhong(String maDatPhong, String maKhachHang, String maPhong) {
        this.maDatPhong = maDatPhong;
        this.maKhachHang = maKhachHang;
        this.maPhong = maPhong;
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

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    @Override
    public String toString() {
        return "DatPhong{" +
                "maDatPhong='" + maDatPhong + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", maPhong='" + maPhong + '\'' +
                '}';
    }
}
