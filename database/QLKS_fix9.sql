/* =========================
   CHUẨN HÓA 5 LOẠI PHÒNG
   ========================= */

SET NOCOUNT ON;

/* 1. Nếu thiếu loại phòng thì thêm, nếu đã có thì giữ nguyên */
IF NOT EXISTS (SELECT 1 FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn')
INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES (N'Phòng đơn', 1, 2, 18.0, N'1 giường đơn', 350000, N'Đang áp dụng', N'Phòng nhỏ gọn cho 1-2 khách');

IF NOT EXISTS (SELECT 1 FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi')
INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES (N'Phòng đôi', 2, 4, 24.0, N'1 giường đôi lớn', 550000, N'Đang áp dụng', N'Phòng phù hợp cặp đôi hoặc công tác');

IF NOT EXISTS (SELECT 1 FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe')
INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES (N'Deluxe', 2, 4, 28.0, N'1 giường đôi / 2 đơn', 800000, N'Đang áp dụng', N'Phòng cao cấp, tiện nghi tốt');

IF NOT EXISTS (SELECT 1 FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family')
INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES (N'Family', 4, 6, 38.0, N'2 giường đôi', 1200000, N'Đang áp dụng', N'Phòng gia đình cho nhóm 4-6 khách');

IF NOT EXISTS (SELECT 1 FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'VIP')
INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES (N'VIP', 3, 5, 45.0, N'1 giường king', 1800000, N'Đang áp dụng', N'Phòng VIP, không gian rộng và cao cấp');


/* 2. Lấy 1 mã loại phòng đại diện cho từng tên bằng MIN() để tránh lỗi subquery nhiều dòng */
DECLARE @lpDon    INT = (SELECT MIN(maLoaiPhong) FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn');
DECLARE @lpDoi    INT = (SELECT MIN(maLoaiPhong) FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi');
DECLARE @lpDeluxe INT = (SELECT MIN(maLoaiPhong) FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe');
DECLARE @lpFamily INT = (SELECT MIN(maLoaiPhong) FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family');
DECLARE @lpVIP    INT = (SELECT MIN(maLoaiPhong) FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'VIP');


/* =========================
   THÊM PHÒNG CÒN THIẾU
   Chỉ INSERT nếu số phòng chưa tồn tại
   ========================= */

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDon, '101', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '101');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDon, '102', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '102');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '103', N'Tầng 1', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '103');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '104', N'Tầng 1', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '104');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '105', N'Tầng 1', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '105');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpFamily, '106', N'Tầng 1', N'Khu B', 4, 6, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '106');


INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDon, '201', N'Tầng 2', N'Khu A', 1, 2, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '201');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDon, '202', N'Tầng 2', N'Khu A', 1, 2, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '202');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '203', N'Tầng 2', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '203');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '204', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '204');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '205', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '205');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpFamily, '206', N'Tầng 2', N'Khu B', 4, 6, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '206');


INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDon, '301', N'Tầng 3', N'Khu A', 1, 2, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '301');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '302', N'Tầng 3', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '302');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '303', N'Tầng 3', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '303');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '304', N'Tầng 3', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '304');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '305', N'Tầng 3', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '305');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpFamily, '306', N'Tầng 3', N'Khu B', 4, 6, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '306');


INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '401', N'Tầng 4', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '401');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '402', N'Tầng 4', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '402');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '403', N'Tầng 4', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '403');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '404', N'Tầng 4', N'Khu B', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '404');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpFamily, '405', N'Tầng 4', N'Khu B', 4, 6, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '405');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpVIP, '406', N'Tầng 4', N'Khu VIP', 3, 5, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '406');


INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDoi, '501', N'Tầng 5', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '501');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '502', N'Tầng 5', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '502');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpDeluxe, '503', N'Tầng 5', N'Khu A', 2, 4, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '503');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpFamily, '504', N'Tầng 5', N'Khu B', 4, 6, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '504');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpVIP, '505', N'Tầng 5', N'Khu VIP', 3, 5, N'Hoạt động'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '505');

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT @lpVIP, '506', N'Tầng 5', N'Khu VIP', 3, 5, N'Bảo trì'
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong WHERE soPhong = '506');


/* =========================
   CHUẨN HÓA TÊN TẦNG VỀ 5 TẦNG
   ========================= */

UPDATE dbo.Phong
SET tang = CASE
               WHEN soPhong LIKE '1%' THEN N'Tầng 1'
               WHEN soPhong LIKE '2%' THEN N'Tầng 2'
               WHEN soPhong LIKE '3%' THEN N'Tầng 3'
               WHEN soPhong LIKE '4%' THEN N'Tầng 4'
               WHEN soPhong LIKE '5%' THEN N'Tầng 5'
               ELSE tang
    END;

/* =========================
   XEM KẾT QUẢ
   ========================= */
SELECT tang, COUNT(*) AS soPhong
FROM dbo.Phong
GROUP BY tang
ORDER BY tang;

SELECT soPhong, tang, trangThai
FROM dbo.Phong
ORDER BY soPhong;