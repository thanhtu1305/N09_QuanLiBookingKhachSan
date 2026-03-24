package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatPhong {
    private String maDatPhong;
    private String maKhachHang;
    private LocalDate ngayDat;
    private String nguonDatPhong;
    private double tongTienDatCoc;
    private String trangThaiDatPhong;
    private String ghiChu;
    private final List<ChiTietDatPhong> chiTietDatPhongs = new ArrayList<ChiTietDatPhong>();

    public DatPhong() {
    }

    public DatPhong(String maDatPhong, String maKhachHang, LocalDate ngayDat, String nguonDatPhong,
                    double tongTienDatCoc, String trangThaiDatPhong, String ghiChu) {
        this.maDatPhong = maDatPhong;
        this.maKhachHang = maKhachHang;
        this.ngayDat = ngayDat;
        this.nguonDatPhong = nguonDatPhong;
        this.tongTienDatCoc = tongTienDatCoc;
        this.trangThaiDatPhong = trangThaiDatPhong;
        this.ghiChu = ghiChu;
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

    public LocalDate getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(LocalDate ngayDat) {
        this.ngayDat = ngayDat;
    }

    public String getNguonDatPhong() {
        return nguonDatPhong;
    }

    public void setNguonDatPhong(String nguonDatPhong) {
        this.nguonDatPhong = nguonDatPhong;
    }

    public double getTongTienDatCoc() {
        return tongTienDatCoc;
    }

    public void setTongTienDatCoc(double tongTienDatCoc) {
        this.tongTienDatCoc = tongTienDatCoc;
    }

    public String getTrangThaiDatPhong() {
        return trangThaiDatPhong;
    }

    public void setTrangThaiDatPhong(String trangThaiDatPhong) {
        this.trangThaiDatPhong = trangThaiDatPhong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<ChiTietDatPhong> getChiTietDatPhongs() {
        return chiTietDatPhongs;
    }

    @Override
    public String toString() {
        return "DatPhong{" +
                "maDatPhong='" + maDatPhong + '\'' +
                ", maKhachHang='" + maKhachHang + '\'' +
                ", ngayDat=" + ngayDat +
                ", nguonDatPhong='" + nguonDatPhong + '\'' +
                ", tongTienDatCoc=" + tongTienDatCoc +
                ", trangThaiDatPhong='" + trangThaiDatPhong + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                ", soDongChiTiet=" + chiTietDatPhongs.size() +
                '}';
    }
}
