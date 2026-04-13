package entity;

public class ChiTietBangGia {
    public static final String DEFAULT_LOAI_NGAY = "Thường";

    private int maChiTietBangGia;
    private int maBangGia;
    private String loaiNgay;
    private String khungGio;
    private double giaTheoGio;
    private double giaQuaDem;
    private double giaTheoNgay;
    private double giaCuoiTuan;
    private double giaLe;
    private double phuThu;

    public ChiTietBangGia() {
    }

    public ChiTietBangGia(int maChiTietBangGia, int maBangGia, String loaiNgay, String khungGio,
                          double giaTheoGio, double giaQuaDem, double giaTheoNgay,
                          double giaCuoiTuan, double giaLe, double phuThu) {
        this.maChiTietBangGia = maChiTietBangGia;
        this.maBangGia = maBangGia;
        this.loaiNgay = loaiNgay;
        this.khungGio = khungGio;
        this.giaTheoGio = giaTheoGio;
        this.giaQuaDem = giaQuaDem;
        this.giaTheoNgay = giaTheoNgay;
        this.giaCuoiTuan = giaCuoiTuan;
        this.giaLe = giaLe;
        this.phuThu = phuThu;
    }

    public int getMaChiTietBangGia() {
        return maChiTietBangGia;
    }

    public void setMaChiTietBangGia(int maChiTietBangGia) {
        this.maChiTietBangGia = maChiTietBangGia;
    }

    public int getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(int maBangGia) {
        this.maBangGia = maBangGia;
    }

    public String getLoaiNgay() {
        return loaiNgay == null || loaiNgay.trim().isEmpty() ? DEFAULT_LOAI_NGAY : loaiNgay;
    }

    public void setLoaiNgay(String loaiNgay) {
        this.loaiNgay = loaiNgay == null || loaiNgay.trim().isEmpty() ? DEFAULT_LOAI_NGAY : loaiNgay.trim();
    }

    public String getKhungGio() {
        return khungGio;
    }

    public void setKhungGio(String khungGio) {
        this.khungGio = khungGio;
    }

    public double getGiaTheoGio() {
        return giaTheoGio;
    }

    public void setGiaTheoGio(double giaTheoGio) {
        this.giaTheoGio = giaTheoGio;
    }

    public double getGiaQuaDem() {
        return giaQuaDem;
    }

    public void setGiaQuaDem(double giaQuaDem) {
        this.giaQuaDem = giaQuaDem;
    }

    public double getGiaTheoNgay() {
        return giaTheoNgay;
    }

    public void setGiaTheoNgay(double giaTheoNgay) {
        this.giaTheoNgay = giaTheoNgay;
    }

    public double getGiaCuoiTuan() {
        return giaCuoiTuan;
    }

    public void setGiaCuoiTuan(double giaCuoiTuan) {
        this.giaCuoiTuan = giaCuoiTuan;
    }

    public double getPhuThuCuoiTuan() {
        return giaCuoiTuan;
    }

    public void setPhuThuCuoiTuan(double phuThuCuoiTuan) {
        this.giaCuoiTuan = phuThuCuoiTuan;
    }

    public double getGiaLe() {
        return giaLe;
    }

    public void setGiaLe(double giaLe) {
        this.giaLe = giaLe;
    }

    public double getPhuThuNgayLe() {
        return giaLe;
    }

    public void setPhuThuNgayLe(double phuThuNgayLe) {
        this.giaLe = phuThuNgayLe;
    }

    public double getWeekendSurcharge() {
        return getPhuThuCuoiTuan();
    }

    public void setWeekendSurcharge(double weekendSurcharge) {
        setPhuThuCuoiTuan(weekendSurcharge);
    }

    public double getHolidaySurcharge() {
        return getPhuThuNgayLe();
    }

    public void setHolidaySurcharge(double holidaySurcharge) {
        setPhuThuNgayLe(holidaySurcharge);
    }

    public double getPhuThu() {
        return phuThu;
    }

    public void setPhuThu(double phuThu) {
        this.phuThu = phuThu;
    }

    @Override
    public String toString() {
        return "ChiTietBangGia{"
                + "maChiTietBangGia=" + maChiTietBangGia
                + ", maBangGia=" + maBangGia
                + ", loaiNgay='" + loaiNgay + '\''
                + ", khungGio='" + khungGio + '\''
                + ", giaTheoGio=" + giaTheoGio
                + ", giaQuaDem=" + giaQuaDem
                + ", giaTheoNgay=" + giaTheoNgay
                + ", giaCuoiTuan=" + giaCuoiTuan
                + ", giaLe=" + giaLe
                + ", phuThu=" + phuThu
                + '}';
    }
}
