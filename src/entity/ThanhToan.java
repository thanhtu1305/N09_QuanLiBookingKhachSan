package entity;

public class ThanhToan {
    private String maThanhToan;
    private String maHoaDon;
    private String phuongThuc;

    public ThanhToan() {
    }

    public ThanhToan(String maThanhToan, String maHoaDon, String phuongThuc) {
        this.maThanhToan = maThanhToan;
        this.maHoaDon = maHoaDon;
        this.phuongThuc = phuongThuc;
    }

    public String getMaThanhToan() {
        return maThanhToan;
    }

    public void setMaThanhToan(String maThanhToan) {
        this.maThanhToan = maThanhToan;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    @Override
    public String toString() {
        return "ThanhToan{" +
                "maThanhToan='" + maThanhToan + '\'' +
                ", maHoaDon='" + maHoaDon + '\'' +
                ", phuongThuc='" + phuongThuc + '\'' +
                '}';
    }
}
