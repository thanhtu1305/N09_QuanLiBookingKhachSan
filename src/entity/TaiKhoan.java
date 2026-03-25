package entity;

import java.sql.Timestamp;

public class TaiKhoan {
    private int maTaiKhoan;
    private int maNhanVien;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String trangThai;
    private Timestamp lanDangNhapCuoi;
    private String emailKhoiPhuc;

    public TaiKhoan() {
    }

    public TaiKhoan(int maTaiKhoan, int maNhanVien, String tenDangNhap, String matKhau,
                    String vaiTro, String trangThai, Timestamp lanDangNhapCuoi, String emailKhoiPhuc) {
        this.maTaiKhoan = maTaiKhoan;
        this.maNhanVien = maNhanVien;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThai = trangThai;
        this.lanDangNhapCuoi = lanDangNhapCuoi;
        this.emailKhoiPhuc = emailKhoiPhuc;
    }

    public int getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(int maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public int getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(int maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Timestamp getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(Timestamp lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public String getEmailKhoiPhuc() {
        return emailKhoiPhuc;
    }

    public void setEmailKhoiPhuc(String emailKhoiPhuc) {
        this.emailKhoiPhuc = emailKhoiPhuc;
    }
}