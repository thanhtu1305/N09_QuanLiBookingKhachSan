package entity;

public class HoaDon {
    private String maHoaDon;
    private String maLuuTru;
    private double tongTien;

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String maLuuTru, double tongTien) {
        this.maHoaDon = maHoaDon;
        this.maLuuTru = maLuuTru;
        this.tongTien = tongTien;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(String maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", maLuuTru='" + maLuuTru + '\'' +
                ", tongTien=" + tongTien +
                '}';
    }
}
