USE QLKS;
GO

/* =========================================================
   A. CẬP NHẬT GIÁ THAM CHIẾU LOẠI PHÒNG
   ========================================================= */
UPDATE dbo.LoaiPhong
SET giaThamChieu =
        CASE tenLoaiPhong
            WHEN N'Phòng đơn' THEN 250000
            WHEN N'Phòng đôi' THEN 380000
            WHEN N'Deluxe'    THEN 520000
            WHEN N'Family'    THEN 780000
            WHEN N'VIP'       THEN 1100000
            ELSE giaThamChieu
            END;
GO

/* =========================================================
   B. CẬP NHẬT CHI TIẾT BẢNG GIÁ
   Giá rẻ hơn, dễ demo, chênh lệch hợp lý giữa các loại phòng
   ========================================================= */
UPDATE ct
SET
    ct.giaTheoGio =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 70000
            WHEN N'Phòng đôi' THEN 100000
            WHEN N'Deluxe'    THEN 140000
            WHEN N'Family'    THEN 210000
            WHEN N'VIP'       THEN 280000
            ELSE ct.giaTheoGio
            END,
    ct.giaQuaDem =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 180000
            WHEN N'Phòng đôi' THEN 280000
            WHEN N'Deluxe'    THEN 380000
            WHEN N'Family'    THEN 580000
            WHEN N'VIP'       THEN 850000
            ELSE ct.giaQuaDem
            END,
    ct.giaTheoNgay =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 250000
            WHEN N'Phòng đôi' THEN 380000
            WHEN N'Deluxe'    THEN 520000
            WHEN N'Family'    THEN 780000
            WHEN N'VIP'       THEN 1100000
            ELSE ct.giaTheoNgay
            END,
    ct.giaCuoiTuan =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 30000
            WHEN N'Phòng đôi' THEN 50000
            WHEN N'Deluxe'    THEN 70000
            WHEN N'Family'    THEN 100000
            WHEN N'VIP'       THEN 150000
            ELSE ct.giaCuoiTuan
            END,
    ct.giaLe =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 70000
            WHEN N'Phòng đôi' THEN 100000
            WHEN N'Deluxe'    THEN 130000
            WHEN N'Family'    THEN 180000
            WHEN N'VIP'       THEN 250000
            ELSE ct.giaLe
            END,
    ct.phuThu =
        CASE lp.tenLoaiPhong
            WHEN N'Phòng đơn' THEN 0
            WHEN N'Phòng đôi' THEN 0
            WHEN N'Deluxe'    THEN 0
            WHEN N'Family'    THEN 0
            WHEN N'VIP'       THEN 0
            ELSE ct.phuThu
            END
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
            WHEN N'Nước suối'            THEN 8000
            WHEN N'Bữa sáng'             THEN 40000
            WHEN N'Giặt ủi'              THEN 30000
            WHEN N'Thuê xe máy'          THEN 120000
            WHEN N'Đưa đón sân bay'      THEN 180000
            WHEN N'Cà phê'               THEN 20000
            WHEN N'Bữa tối'              THEN 90000
            WHEN N'In ấn tài liệu'       THEN 3000
            WHEN N'Thuê phòng họp mini'  THEN 250000
            WHEN N'Phụ thu thêm khách'   THEN 80000
            WHEN N'Massage thư giãn'     THEN 150000
            WHEN N'Combo ăn trưa'        THEN 70000
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