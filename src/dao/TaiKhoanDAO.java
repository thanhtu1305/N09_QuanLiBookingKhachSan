package dao;

import entity.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {
    public List<TaiKhoan> getAll() {
        // TODO: Truy van danh sach tai khoan
        return new ArrayList<>();
    }

    public boolean insert(TaiKhoan taiKhoan) {
        // TODO: Them tai khoan
        return false;
    }

    public boolean update(TaiKhoan taiKhoan) {
        // TODO: Cap nhat tai khoan
        return false;
    }

    public boolean delete(String maTaiKhoan) {
        // TODO: Xoa tai khoan theo ma
        return false;
    }

    public TaiKhoan findById(String maTaiKhoan) {
        // TODO: Tim tai khoan theo ma
        return null;
    }
}
