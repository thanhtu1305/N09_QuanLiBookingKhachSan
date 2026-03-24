package entity;

public class LuuTru {
    private String maLuuTru;
    private String maChiTietDatPhong;
    private String trangThai;

    public LuuTru() {
    }

    public LuuTru(String maLuuTru, String maChiTietDatPhong, String trangThai) {
        this.maLuuTru = maLuuTru;
        this.maChiTietDatPhong = maChiTietDatPhong;
        this.trangThai = trangThai;
    }

    public String getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(String maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public String getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(String maChiTietDatPhong) {
        this.maChiTietDatPhong = maChiTietDatPhong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "LuuTru{" +
                "maLuuTru='" + maLuuTru + '\'' +
                ", maChiTietDatPhong='" + maChiTietDatPhong + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
