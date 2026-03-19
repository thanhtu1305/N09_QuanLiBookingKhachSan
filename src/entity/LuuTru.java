package entity;

public class LuuTru {
    private String maLuuTru;
    private String maDatPhong;
    private String trangThai;

    public LuuTru() {
    }

    public LuuTru(String maLuuTru, String maDatPhong, String trangThai) {
        this.maLuuTru = maLuuTru;
        this.maDatPhong = maDatPhong;
        this.trangThai = trangThai;
    }

    public String getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(String maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public String getMaDatPhong() {
        return maDatPhong;
    }

    public void setMaDatPhong(String maDatPhong) {
        this.maDatPhong = maDatPhong;
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
                ", maDatPhong='" + maDatPhong + '\'' +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
