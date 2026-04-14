package entity;

import java.io.Serializable;
import java.time.LocalDate;

public class DashboardGanttCell implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private int maPhong;
    private String soPhong;
    private String tang;
    private String loaiPhong;
    private String khuVuc;
    private String phongTrangThai;
    private String statusCode;
    private String statusText;
    private String sourceType;
    private int priority;
    private int maDatPhong;
    private int maLuuTru;
    private int maChiTietDatPhong;
    private String customerName;
    private LocalDate fromDate;
    private LocalDate toDate;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

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

    public String getPhongTrangThai() {
        return phongTrangThai;
    }

    public void setPhongTrangThai(String phongTrangThai) {
        this.phongTrangThai = phongTrangThai;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(int maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public int getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(int maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public int getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(int maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
