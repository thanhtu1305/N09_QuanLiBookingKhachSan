package entity;

public class Phong {
    private int maPhong;
    private int maLoaiPhong;
    private String soPhong;
    private String tang;
    private String khuVuc;
    private int sucChuaChuan;
    private int sucChuaToiDa;
    private String trangThai;
    private String tenLoaiPhong;

    public Phong() {
    }

    public Phong(int maPhong, int maLoaiPhong, String soPhong, String tang, String khuVuc,
                 int sucChuaChuan, int sucChuaToiDa, String trangThai) {
        this(maPhong, maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai, null);
    }

    public Phong(int maPhong, int maLoaiPhong, String soPhong, String tang, String khuVuc,
                 int sucChuaChuan, int sucChuaToiDa, String trangThai, String tenLoaiPhong) {
        this.maPhong = maPhong;
        this.maLoaiPhong = maLoaiPhong;
        this.soPhong = soPhong;
        this.tang = tang;
        this.khuVuc = khuVuc;
        this.sucChuaChuan = sucChuaChuan;
        this.sucChuaToiDa = sucChuaToiDa;
        this.trangThai = trangThai;
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public Phong(int maLoaiPhong, String soPhong, String tang, String khuVuc,
                 int sucChuaChuan, int sucChuaToiDa, String trangThai) {
        this(0, maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai, null);
    }

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
    }

    public int getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(int maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String soPhong) {
        this.soPhong = soPhong;
    }

    public String getTang() {
        return tang;
    }

    public void setTang(String tang) {
        this.tang = tang;
    }

    public String getKhuVuc() {
        return khuVuc;
    }

    public void setKhuVuc(String khuVuc) {
        this.khuVuc = khuVuc;
    }

    public int getSucChuaChuan() {
        return sucChuaChuan;
    }

    public void setSucChuaChuan(int sucChuaChuan) {
        this.sucChuaChuan = sucChuaChuan;
    }

    public int getSucChuaToiDa() {
        return sucChuaToiDa;
    }

    public void setSucChuaToiDa(int sucChuaToiDa) {
        this.sucChuaToiDa = sucChuaToiDa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    @Override
    public String toString() {
        return "Phong{" +
                "maPhong=" + maPhong +
                ", maLoaiPhong=" + maLoaiPhong +
                ", soPhong='" + soPhong + '\'' +
                ", tang='" + tang + '\'' +
                ", khuVuc='" + khuVuc + '\'' +
                ", sucChuaChuan=" + sucChuaChuan +
                ", sucChuaToiDa=" + sucChuaToiDa +
                ", trangThai='" + trangThai + '\'' +
                ", tenLoaiPhong='" + tenLoaiPhong + '\'' +
                '}';
    }
}
