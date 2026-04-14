package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DashboardGanttRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private int maPhong;
    private String soPhong;
    private String tang;
    private String loaiPhong;
    private String khuVuc;
    private int sucChuaChuan;
    private int sucChuaToiDa;
    private String trangThaiPhong;
    private final List<DashboardGanttCell> cells = new ArrayList<DashboardGanttCell>();

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
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

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
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

    public String getTrangThaiPhong() {
        return trangThaiPhong;
    }

    public void setTrangThaiPhong(String trangThaiPhong) {
        this.trangThaiPhong = trangThaiPhong;
    }

    public List<DashboardGanttCell> getCells() {
        return cells;
    }
}
