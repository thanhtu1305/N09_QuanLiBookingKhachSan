/* =====================
   LOẠI PHÒNG + PHÒNG
   Chuẩn hóa khách sạn 5 tầng, mỗi tầng 6 phòng
   5 loại phòng: Phòng đơn, Phòng đôi, Deluxe, Family, VIP
   ===================== */

DELETE FROM dbo.Phong;
DELETE FROM dbo.LoaiPhongTienNghi;
DELETE FROM dbo.LoaiPhong;

INSERT INTO dbo.LoaiPhong
(tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES
    (N'Phòng đơn', 1, 2, 18.0, N'1 giường đơn',       350000,  N'Đang áp dụng', N'Phòng nhỏ gọn cho 1-2 khách'),
    (N'Phòng đôi', 2, 4, 24.0, N'1 giường đôi lớn',   550000,  N'Đang áp dụng', N'Phòng phù hợp cặp đôi hoặc công tác'),
    (N'Deluxe',    2, 4, 28.0, N'1 giường đôi / 2 đơn', 800000, N'Đang áp dụng', N'Phòng cao cấp, tiện nghi tốt'),
    (N'Family',    4, 6, 38.0, N'2 giường đôi',      1200000, N'Đang áp dụng', N'Phòng gia đình cho nhóm 4-6 khách'),
    (N'VIP',       3, 5, 45.0, N'1 giường king',     1800000, N'Đang áp dụng', N'Phòng VIP, không gian rộng và cao cấp');

DECLARE @lpDon    INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn');
DECLARE @lpDoi    INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi');
DECLARE @lpDeluxe INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe');
DECLARE @lpFamily INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family');
DECLARE @lpVIP    INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'VIP');

/* Gán tiện nghi theo loại phòng */
INSERT INTO dbo.LoaiPhongTienNghi (maLoaiPhong, maTienNghi)
SELECT x.maLoaiPhong, x.maTienNghi
FROM (
         /* Phòng đơn */
         SELECT @lpDon,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpDon,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'TV') UNION ALL
         SELECT @lpDon,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Điều hòa') UNION ALL
         SELECT @lpDon,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Nước nóng') UNION ALL
         SELECT @lpDon,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Vòi sen') UNION ALL

         /* Phòng đôi */
         SELECT @lpDoi,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpDoi,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'TV') UNION ALL
         SELECT @lpDoi,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Điều hòa') UNION ALL
         SELECT @lpDoi,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Nước nóng') UNION ALL
         SELECT @lpDoi,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Ban công') UNION ALL

         /* Deluxe */
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Smart TV') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Netflix') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Minibar') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bàn làm việc') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Khăn tắm') UNION ALL

         /* Family */
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Tủ lạnh') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Ấm đun nước') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Khăn tắm') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bếp mini') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Sofa') UNION ALL

         /* VIP */
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Smart TV') UNION ALL
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Netflix') UNION ALL
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bồn tắm') UNION ALL
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Máy pha cà phê') UNION ALL
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Ban công') UNION ALL
         SELECT @lpVIP,    (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Két sắt')
     ) x(maLoaiPhong, maTienNghi)
WHERE x.maLoaiPhong IS NOT NULL
  AND x.maTienNghi IS NOT NULL;

/* 5 tầng, mỗi tầng 6 phòng = 30 phòng */
INSERT INTO dbo.Phong
(maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
VALUES
    /* Tầng 1 */
    (@lpDon,    '101', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'),
    (@lpDon,    '102', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'),
    (@lpDoi,    '103', N'Tầng 1', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDoi,    '104', N'Tầng 1', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '105', N'Tầng 1', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpFamily, '106', N'Tầng 1', N'Khu B', 4, 6, N'Hoạt động'),

    /* Tầng 2 */
    (@lpDon,    '201', N'Tầng 2', N'Khu A', 1, 2, N'Hoạt động'),
    (@lpDon,    '202', N'Tầng 2', N'Khu A', 1, 2, N'Hoạt động'),
    (@lpDoi,    '203', N'Tầng 2', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDoi,    '204', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '205', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpFamily, '206', N'Tầng 2', N'Khu B', 4, 6, N'Hoạt động'),

    /* Tầng 3 */
    (@lpDon,    '301', N'Tầng 3', N'Khu A', 1, 2, N'Hoạt động'),
    (@lpDoi,    '302', N'Tầng 3', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDoi,    '303', N'Tầng 3', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '304', N'Tầng 3', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '305', N'Tầng 3', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpFamily, '306', N'Tầng 3', N'Khu B', 4, 6, N'Hoạt động'),

    /* Tầng 4 */
    (@lpDoi,    '401', N'Tầng 4', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDoi,    '402', N'Tầng 4', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '403', N'Tầng 4', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '404', N'Tầng 4', N'Khu B', 2, 4, N'Hoạt động'),
    (@lpFamily, '405', N'Tầng 4', N'Khu B', 4, 6, N'Hoạt động'),
    (@lpVIP,    '406', N'Tầng 4', N'Khu VIP', 3, 5, N'Hoạt động'),

    /* Tầng 5 */
    (@lpDoi,    '501', N'Tầng 5', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '502', N'Tầng 5', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpDeluxe, '503', N'Tầng 5', N'Khu A', 2, 4, N'Hoạt động'),
    (@lpFamily, '504', N'Tầng 5', N'Khu B', 4, 6, N'Hoạt động'),
    (@lpVIP,    '505', N'Tầng 5', N'Khu VIP', 3, 5, N'Hoạt động'),
    (@lpVIP,    '506', N'Tầng 5', N'Khu VIP', 3, 5, N'Bảo trì');