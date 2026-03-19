package dao;

import entity.KhachHang;

import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO {
    public List<KhachHang> getAll() {
        // TODO: Truy van danh sach khach hang
        return new ArrayList<>();
    }

    public boolean insert(KhachHang khachHang) {
        // TODO: Them khach hang
        return false;
    }

    public boolean update(KhachHang khachHang) {
        // TODO: Cap nhat khach hang
        return false;
    }

    public boolean delete(String maKhachHang) {
        // TODO: Xoa khach hang theo ma
        return false;
    }

    public KhachHang findById(String maKhachHang) {
        // TODO: Tim khach hang theo ma
        return null;
    }
}
