package entity;

public class Phong {
    private String maPhong;
    private String maLoaiPhong;
    private String trangThai;

    public Phong() {
    }

    public Phong(String maPhong, String maLoaiPhong, String trangThai) {
        this.maPhong = maPhong;
        this.maLoaiPhong = maLoaiPhong;
        this.trangThai = trangThai;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "Phong{" +
                "maPhong='" + maPhong + '\'' +
                ", maLoaiPhong='" + maLoaiPhong + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
