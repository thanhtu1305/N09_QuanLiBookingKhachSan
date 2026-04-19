USE QLKS;
GO

/* =========================================================
   A. CẬP NHẬT GIÁ THAM CHIẾU LOẠI PHÒNG
   ========================================================= */
UPDATE dbo.LoaiPhong
SET giaThamChieu =
        CASE tenLoaiPhong
            WHEN N'Phòng đơn' THEN 180000
            WHEN N'Phòng đôi' THEN 260000
            WHEN N'Deluxe'    THEN 350000
            WHEN N'Family'    THEN 520000
            WHEN N'VIP'       THEN 750000
            ELSE giaThamChieu
            END;
GO

/* =========================================================
   B. CẬP NHẬT CHI TIẾT BẢNG GIÁ
   Giá nhẹ hơn, dễ demo, nhìn thực tế với bài đồ án
   ========================================================= */
UPDATE ct
SET
    ct.giaTheoGio =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 50000
            WHEN N'Phòng đôi' THEN 70000
            WHEN N'Deluxe'    THEN 90000
            WHEN N'Family'    THEN 130000
            WHEN N'VIP'       THEN 180000
            ELSE ct.giaTheoGio
            END,
    ct.giaQuaDem =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 120000
            WHEN N'Phòng đôi' THEN 180000
            WHEN N'Deluxe'    THEN 250000
            WHEN N'Family'    THEN 380000
            WHEN N'VIP'       THEN 550000
            ELSE ct.giaQuaDem
            END,
    ct.giaTheoNgay =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 180000
            WHEN N'Phòng đôi' THEN 260000
            WHEN N'Deluxe'    THEN 350000
            WHEN N'Family'    THEN 520000
            WHEN N'VIP'       THEN 750000
            ELSE ct.giaTheoNgay
            END,
    ct.giaCuoiTuan =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 20000
            WHEN N'Phòng đôi' THEN 30000
            WHEN N'Deluxe'    THEN 50000
            WHEN N'Family'    THEN 70000
            WHEN N'VIP'       THEN 100000
            ELSE ct.giaCuoiTuan
            END,
    ct.giaLe =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 50000
            WHEN N'Phòng đôi' THEN 70000
            WHEN N'Deluxe'    THEN 90000
            WHEN N'Family'    THEN 120000
            WHEN N'VIP'       THEN 180000
            ELSE ct.giaLe
            END,
    ct.phuThu = 0
    FROM dbo.ChiTietBangGia ct
JOIN dbo.BangGia bg ON bg.maBangGia = ct.maBangGia
    JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = bg.maLoaiPhong;
GO

/* =========================================================
   C. CẬP NHẬT GIÁ DỊCH VỤ
   ========================================================= */
UPDATE dbo.DichVu
SET donGia =
        CASE tenDichVu
            WHEN N'Nước suối'            THEN 5000
            WHEN N'Bữa sáng'             THEN 25000
            WHEN N'Giặt ủi'              THEN 20000
            WHEN N'Thuê xe máy'          THEN 80000
            WHEN N'Đưa đón sân bay'      THEN 120000
            WHEN N'Cà phê'               THEN 15000
            WHEN N'Bữa tối'              THEN 60000
            WHEN N'In ấn tài liệu'       THEN 2000
            WHEN N'Thuê phòng họp mini'  THEN 150000
            WHEN N'Phụ thu thêm khách'   THEN 50000
            WHEN N'Massage thư giãn'     THEN 100000
            WHEN N'Combo ăn trưa'        THEN 50000
            ELSE donGia
            END;
GO

/* =========================================================
   D. KIỂM TRA NHANH
   ========================================================= */
SELECT tenLoaiPhong, giaThamChieu
FROM dbo.LoaiPhong
ORDER BY maLoaiPhong;

SELECT bg.tenBangGia, lp.tenLoaiPhong,
       ct.giaTheoGio, ct.giaQuaDem, ct.giaTheoNgay, ct.giaCuoiTuan, ct.giaLe, ct.phuThu
FROM dbo.ChiTietBangGia ct
         JOIN dbo.BangGia bg ON bg.maBangGia = ct.maBangGia
         JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = bg.maLoaiPhong
ORDER BY lp.maLoaiPhong;

SELECT tenDichVu, donGia
FROM dbo.DichVu
ORDER BY maDichVu;
GO