package entity;

import java.sql.Date;

public class NhanVien {
    private int maNhanVien;
    private String hoTen;
    private Date ngaySinh;
    private String gioiTinh;
    private String cccd;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String boPhan;
    private String chucVu;
    private String caLam;
    private Date ngayVaoLam;
    private String trangThai;
    private String ghiChu;

    public NhanVien() {
    }

    public NhanVien(int maNhanVien, String hoTen, Date ngaySinh, String gioiTinh, String cccd,
                    String soDienThoai, String email, String diaChi, String boPhan,
                    String chucVu, String caLam, Date ngayVaoLam, String trangThai, String ghiChu) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.cccd = cccd;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.boPhan = boPhan;
        this.chucVu = chucVu;
        this.caLam = caLam;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
    }

    public NhanVien(String hoTen, Date ngaySinh, String gioiTinh, String cccd,
                    String soDienThoai, String email, String diaChi, String boPhan,
                    String chucVu, String caLam, Date ngayVaoLam, String trangThai, String ghiChu) {
        this(0, hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu);
    }

    public int getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(int maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getBoPhan() {
        return boPhan;
    }

    public void setBoPhan(String boPhan) {
        this.boPhan = boPhan;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getCaLam() {
        return caLam;
    }

    public void setCaLam(String caLam) {
        this.caLam = caLam;
    }

    public Date getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(Date ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return hoTen == null ? "" : hoTen;
    }
}
