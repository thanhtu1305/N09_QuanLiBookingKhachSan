package entity;

public class LoaiPhong {
    private String maLoaiPhong;
    private String tenLoaiPhong;
    private int soNguoiToiDa;

    public LoaiPhong() {
    }

    public LoaiPhong(String maLoaiPhong, String tenLoaiPhong, int soNguoiToiDa) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
        this.soNguoiToiDa = soNguoiToiDa;
    }

    public String getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(String maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public int getSoNguoiToiDa() {
        return soNguoiToiDa;
    }

    public void setSoNguoiToiDa(int soNguoiToiDa) {
        this.soNguoiToiDa = soNguoiToiDa;
    }

    @Override
    public String toString() {
        return "LoaiPhong{" +
                "maLoaiPhong='" + maLoaiPhong + '\'' +
                ", tenLoaiPhong='" + tenLoaiPhong + '\'' +
                ", soNguoiToiDa=" + soNguoiToiDa +
                '}';
    }
}
