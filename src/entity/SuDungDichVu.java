package entity;

public class SuDungDichVu {
    private String maSuDung;
    private String maLuuTru;
    private String maDichVu;

    public SuDungDichVu() {
    }

    public SuDungDichVu(String maSuDung, String maLuuTru, String maDichVu) {
        this.maSuDung = maSuDung;
        this.maLuuTru = maLuuTru;
        this.maDichVu = maDichVu;
    }

    public String getMaSuDung() {
        return maSuDung;
    }

    public void setMaSuDung(String maSuDung) {
        this.maSuDung = maSuDung;
    }

    public String getMaLuuTru() {
        return maLuuTru;
    }

    public void setMaLuuTru(String maLuuTru) {
        this.maLuuTru = maLuuTru;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    @Override
    public String toString() {
        return "SuDungDichVu{" +
                "maSuDung='" + maSuDung + '\'' +
                ", maLuuTru='" + maLuuTru + '\'' +
                ", maDichVu='" + maDichVu + '\'' +
                '}';
    }
}
