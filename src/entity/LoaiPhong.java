package entity;

public class LoaiPhong {
    private int maLoaiPhong;
    private String tenLoaiPhong;
    private int khachToiDa;
    private double dienTich;
    private String loaiGiuong;
    private double giaThamChieu;
    private String trangThai;
    private String moTa;

    public LoaiPhong() {
    }

    public LoaiPhong(int maLoaiPhong, String tenLoaiPhong, int khachToiDa,
                     double dienTich, String loaiGiuong, double giaThamChieu,
                     String trangThai, String moTa) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.khachToiDa = khachToiDa;
        this.dienTich = dienTich;
        this.loaiGiuong = loaiGiuong;
        this.giaThamChieu = giaThamChieu;
        this.trangThai = trangThai;
        this.moTa = moTa;
    }

    public LoaiPhong(String tenLoaiPhong, int khachToiDa,
                     double dienTich, String loaiGiuong, double giaThamChieu,
                     String trangThai, String moTa) {
        this(0, tenLoaiPhong, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa);
    }

    public int getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(int maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public int getKhachToiDa() {
        return khachToiDa;
    }

    public void setKhachToiDa(int khachToiDa) {
        this.khachToiDa = khachToiDa;
    }

    public double getDienTich() {
        return dienTich;
    }

    public void setDienTich(double dienTich) {
        this.dienTich = dienTich;
    }

    public String getLoaiGiuong() {
        return loaiGiuong;
    }

    public void setLoaiGiuong(String loaiGiuong) {
        this.loaiGiuong = loaiGiuong;
    }

    public double getGiaThamChieu() {
        return giaThamChieu;
    }

    public void setGiaThamChieu(double giaThamChieu) {
        this.giaThamChieu = giaThamChieu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "LoaiPhong{" +
                "maLoaiPhong=" + maLoaiPhong +
                ", tenLoaiPhong='" + tenLoaiPhong + '\'' +
                ", khachToiDa=" + khachToiDa +
                ", dienTich=" + dienTich +
                ", loaiGiuong='" + loaiGiuong + '\'' +
                ", giaThamChieu=" + giaThamChieu +
                ", trangThai='" + trangThai + '\'' +
                ", moTa='" + moTa + '\'' +
                '}';
    }
}
