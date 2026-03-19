package entity;

public class TienNghi {
    private String maTienNghi;
    private String tenTienNghi;
    private String moTa;

    public TienNghi() {
    }

    public TienNghi(String maTienNghi, String tenTienNghi, String moTa) {
        this.maTienNghi = maTienNghi;
        this.tenTienNghi = tenTienNghi;
        this.moTa = moTa;
    }

    public String getMaTienNghi() {
        return maTienNghi;
    }

    public void setMaTienNghi(String maTienNghi) {
        this.maTienNghi = maTienNghi;
    }

    public String getTenTienNghi() {
        return tenTienNghi;
    }

    public void setTenTienNghi(String tenTienNghi) {
        this.tenTienNghi = tenTienNghi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public String toString() {
        return "TienNghi{" +
                "maTienNghi='" + maTienNghi + '\'' +
                ", tenTienNghi='" + tenTienNghi + '\'' +
                ", moTa='" + moTa + '\'' +
                '}';
    }
}
