package entity;

import java.io.Serializable;
import java.time.LocalDate;

public class KhachHang implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maKhachHang;
    private String hoTen;
    private String gioiTinh;
    private String ngaySinh;
    private String soDienThoai;
    private String email;
    private String cccdPassport;
    private String diaChi;
    private String quocTich;
    private String loaiKhach;
    private String hangKhach;
    private String trangThai;
    private String nguoiTao;
    private String ghiChu;

    public KhachHang() {
    }

    public KhachHang(String maKhachHang, String hoTen, String soDienThoai) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
    }

    public KhachHang(String maKhachHang, String hoTen, String gioiTinh, String ngaySinh,
                     String soDienThoai, String email, String cccdPassport, String diaChi,
                     String quocTich, String loaiKhach, String hangKhach,
                     String trangThai, String nguoiTao, String ghiChu) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.gioiTinh = gioiTinh;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.cccdPassport = cccdPassport;
        this.diaChi = diaChi;
        this.quocTich = quocTich;
        this.loaiKhach = loaiKhach;
        this.hangKhach = hangKhach;
        this.trangThai = trangThai;
        this.nguoiTao = nguoiTao;
        this.ghiChu = ghiChu;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = normalizeDateString(ngaySinh);
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh == null ? "" : ngaySinh.toString();
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

    public String getCccdPassport() {
        return cccdPassport;
    }

    public void setCccdPassport(String cccdPassport) {
        this.cccdPassport = cccdPassport;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getQuocTich() {
        return quocTich;
    }

    public void setQuocTich(String quocTich) {
        this.quocTich = quocTich;
    }

    public String getLoaiKhach() {
        return loaiKhach;
    }

    public void setLoaiKhach(String loaiKhach) {
        this.loaiKhach = loaiKhach;
    }

    public String getHangKhach() {
        return hangKhach;
    }

    public void setHangKhach(String hangKhach) {
        this.hangKhach = hangKhach;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // Alias methods để tránh lệch tên giữa GUI và DAO
    public String getTenKhachHang() {
        return hoTen;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.hoTen = tenKhachHang;
    }

    public String getMaKh() {
        return maKhachHang;
    }

    public void setMaKh(String maKh) {
        this.maKhachHang = maKh;
    }

    public String getHangThanhVien() {
        return hangKhach;
    }

    public void setHangThanhVien(String hangThanhVien) {
        this.hangKhach = hangThanhVien;
    }

    public LocalDate getNgaySinhAsLocalDate() {
        try {
            return ngaySinh == null || ngaySinh.trim().isEmpty() ? null : LocalDate.parse(ngaySinh.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeDateString(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", cccdPassport='" + cccdPassport + '\'' +
                '}';
    }
}
