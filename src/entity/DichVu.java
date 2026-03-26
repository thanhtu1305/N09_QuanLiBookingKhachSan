package entity;

public class DichVu {
    private int maDichVu;
    private String tenDichVu;
    private double donGia;
    private String donVi;

    public DichVu() {
    }

    public DichVu(int maDichVu, String tenDichVu, double donGia, String donVi) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donGia = donGia;
        this.donVi = donVi;
    }

    public DichVu(String tenDichVu, double donGia, String donVi) {
        this(0, tenDichVu, donGia, donVi);
    }

    public int getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(int maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    @Override
    public String toString() {
        return "DichVu{" +
                "maDichVu=" + maDichVu +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", donGia=" + donGia +
                ", donVi='" + donVi + '\'' +
                '}';
    }
}
