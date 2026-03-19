package dao;

import entity.LuuTru;

import java.util.ArrayList;
import java.util.List;

public class CheckInOutDAO {
    public List<LuuTru> getAll() {
        // TODO: Truy van danh sach luu tru
        return new ArrayList<>();
    }

    public boolean insert(LuuTru luuTru) {
        // TODO: Them thong tin check-in/check-out
        return false;
    }

    public boolean update(LuuTru luuTru) {
        // TODO: Cap nhat thong tin check-in/check-out
        return false;
    }

    public boolean delete(String maLuuTru) {
        // TODO: Xoa thong tin luu tru theo ma
        return false;
    }

    public LuuTru findById(String maLuuTru) {
        // TODO: Tim thong tin luu tru theo ma
        return null;
    }
}
