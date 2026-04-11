USE QLKS;
GO

/* =========================================================
   RESET DỮ LIỆU LIÊN QUAN ĐỂ SEED LẠI CHO GỌN
   ========================================================= */

DELETE FROM ThanhToan;
DELETE FROM ChiTietHoaDon;
DELETE FROM HoaDon;
DELETE FROM SuDungDichVu;
DELETE FROM LuuTru;
DELETE FROM ChiTietDatPhong;
DELETE FROM DatPhong;

DELETE FROM ChiTietBangGia;
DELETE FROM BangGia;

DELETE FROM LoaiPhongTienNghi;
DELETE FROM Phong;
DELETE FROM DichVu;
DELETE FROM LoaiPhong;
GO

/* Reset identity để mã đẹp hơn khi demo */
DBCC CHECKIDENT ('LoaiPhong', RESEED, 0);
DBCC CHECKIDENT ('Phong', RESEED, 0);
DBCC CHECKIDENT ('BangGia', RESEED, 0);
DBCC CHECKIDENT ('ChiTietBangGia', RESEED, 0);
DBCC CHECKIDENT ('DichVu', RESEED, 0);
GO

/* =========================================================
   1) LOẠI PHÒNG - CHỈ 5 LOẠI
   Bắt buộc có: Phòng đơn, Phòng đôi, VIP
   ========================================================= */

INSERT INTO LoaiPhong
(
    tenLoaiPhong, sucChua, khachToiDa, dienTich,
    loaiGiuong, giaThamChieu, trangThai, moTa
)
VALUES
(N'Phòng đơn', 1, 2, 18.0, N'1 giường đơn', 250000, N'Đang áp dụng', N'Phòng phù hợp 1 khách hoặc 2 khách ở ngắn ngày'),
(N'Phòng đôi', 2, 3, 22.0, N'1 giường đôi', 350000, N'Đang áp dụng', N'Phòng tiêu chuẩn cho cặp đôi hoặc 2 khách'),
(N'Deluxe', 2, 3, 28.0, N'1 giường đôi lớn', 450000, N'Đang áp dụng', N'Phòng cao cấp hơn, rộng rãi và tiện nghi tốt hơn'),
(N'Family', 4, 6, 35.0, N'2 giường đôi', 650000, N'Đang áp dụng', N'Phòng gia đình cho nhóm 4-6 người'),
(N'VIP', 2, 4, 40.0, N'1 giường king', 900000, N'Đang áp dụng', N'Phòng VIP có thêm tiện nghi cao cấp');
GO

/* =========================================================
   2) PHÒNG - CHIA ĐỀU THEO 5 LOẠI
   ========================================================= */

INSERT INTO Phong
(
    maLoaiPhong, soPhong, tang, khuVuc,
    sucChuaChuan, sucChuaToiDa, trangThai
)
VALUES
(1, '101', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'),
(1, '102', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'),

(2, '201', N'Tầng 2', N'Khu A', 2, 3, N'Hoạt động'),
(2, '202', N'Tầng 2', N'Khu A', 2, 3, N'Hoạt động'),

(3, '301', N'Tầng 3', N'Khu B', 2, 3, N'Hoạt động'),
(3, '302', N'Tầng 3', N'Khu B', 2, 3, N'Hoạt động'),

(4, '401', N'Tầng 4', N'Khu Family', 4, 6, N'Hoạt động'),
(4, '402', N'Tầng 4', N'Khu Family', 4, 6, N'Bảo trì'),

(5, '501', N'Tầng 5', N'Khu VIP', 2, 4, N'Hoạt động'),
(5, '502', N'Tầng 5', N'Khu VIP', 2, 4, N'Hoạt động');
GO

/* =========================================================
   3) BẢNG GIÁ - GIÁ HỢP LÝ HƠN
   Gợi ý theo hướng mới:
   - giaTheoGio   = giá cơ bản theo giờ
   - giaQuaDem    = giá cơ bản qua đêm
   - giaTheoNgay  = giá cơ bản theo ngày
   - giaCuoiTuan  = phụ thu cuối tuần
   - giaLe        = phụ thu ngày lễ
   ========================================================= */

INSERT INTO BangGia
(
    tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc,
    loaiNgay, trangThai
)
VALUES
(N'Bảng giá Phòng đơn 2026', 1, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá Phòng đôi 2026', 2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá Deluxe 2026',    3, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá Family 2026',    4, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá VIP 2026',       5, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng');
GO

INSERT INTO ChiTietBangGia
(
    maBangGia, loaiNgay, khungGio,
    giaTheoGio, giaQuaDem, giaTheoNgay,
    giaCuoiTuan, giaLe, phuThu
)
VALUES
/* Phòng đơn */
(1, N'Thường', N'00:00-23:59',  60000, 250000, 200000,  20000,  50000, 0),

/* Phòng đôi */
(2, N'Thường', N'00:00-23:59',  90000, 350000, 300000,  30000,  70000, 0),

/* Deluxe */
(3, N'Thường', N'00:00-23:59', 120000, 450000, 400000,  50000, 100000, 0),

/* Family */
(4, N'Thường', N'00:00-23:59', 160000, 650000, 600000,  70000, 150000, 0),

/* VIP */
(5, N'Thường', N'00:00-23:59', 220000, 900000, 850000, 120000, 250000, 0);
GO

/* =========================================================
   4) DỊCH VỤ - NHIỀU HƠN, GIÁ HỢP LÝ
   ========================================================= */

INSERT INTO DichVu (tenDichVu, donGia, donVi)
VALUES
(N'Nước suối',           10000,  N'Chai'),
(N'Nước ngọt lon',       18000,  N'Lon'),
(N'Mì ly',               15000,  N'Ly'),
(N'Bữa sáng',            50000,  N'Suất'),
(N'Bữa trưa',            90000,  N'Suất'),
(N'Giặt ủi',             30000,  N'Kg'),
(N'Thuê xe máy',        150000,  N'Ngày'),
(N'Đưa đón sân bay',    200000,  N'Lượt'),
(N'Kê thêm giường',     100000,  N'Đêm'),
(N'Massage thư giãn',   250000,  N'60 phút'),
(N'Dịch vụ in ấn',        2000,  N'Trang'),
(N'Trang trí sinh nhật',300000,  N'Gói'),
(N'Đặt hoa tươi',       150000,  N'Bó'),
(N'Combo minibar',       35000,  N'Phần'),
(N'Phòng họp mini',     400000,  N'2 giờ');
GO

/* =========================================================
   5) CẬP NHẬT GIÁ THAM CHIẾU CHO DỄ NHÌN TRÊN UI
   ========================================================= */
UPDATE lp
SET lp.giaThamChieu = ct.giaQuaDem
    FROM LoaiPhong lp
JOIN BangGia bg ON bg.maLoaiPhong = lp.maLoaiPhong
    JOIN ChiTietBangGia ct ON ct.maBangGia = bg.maBangGia;
GO

/* =========================================================
   6) VIEW KIỂM TRA NHANH
   ========================================================= */
IF OBJECT_ID('dbo.v_BangGiaHienThi', 'V') IS NOT NULL
DROP VIEW dbo.v_BangGiaHienThi;
GO

CREATE VIEW dbo.v_BangGiaHienThi
AS
SELECT
    lp.tenLoaiPhong,
    bg.tenBangGia,
    ct.giaTheoGio,
    ct.giaQuaDem,
    ct.giaTheoNgay,
    ct.giaCuoiTuan AS phuThuCuoiTuan,
    ct.giaLe AS phuThuNgayLe
FROM BangGia bg
         JOIN LoaiPhong lp ON lp.maLoaiPhong = bg.maLoaiPhong
         JOIN ChiTietBangGia ct ON ct.maBangGia = bg.maBangGia;
GO

SELECT * FROM dbo.v_BangGiaHienThi;
GO