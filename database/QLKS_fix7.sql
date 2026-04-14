SET NOCOUNT ON;
GO

DECLARE @lpStandard INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Standard');
DECLARE @lpDeluxe   INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe');
DECLARE @lpSuite    INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Suite');
DECLARE @lpFamily   INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family');
DECLARE @lpDon      INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn');
DECLARE @lpDoi      INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi');

;WITH DesiredRooms AS (
    SELECT *
    FROM (VALUES
        (@lpDon,      '101', N'Tầng 1', N'Khu A',      1, 2, N'Hoạt động'),
        (@lpStandard, '102', N'Tầng 1', N'Khu A',      2, 3, N'Đã đặt'),
        (@lpDon,      '103', N'Tầng 1', N'Khu A',      1, 2, N'Hoạt động'),
        (@lpDoi,      '104', N'Tầng 1', N'Khu B',      2, 4, N'Hoạt động'),
        (@lpStandard, '105', N'Tầng 1', N'Khu B',      2, 3, N'Hoạt động'),
        (@lpDoi,      '106', N'Tầng 1', N'Khu C',      2, 4, N'Hoạt động'),
        (@lpDeluxe,   '201', N'Tầng 2', N'Khu A',      2, 4, N'Đang ở'),
        (@lpDeluxe,   '202', N'Tầng 2', N'Khu A',      2, 4, N'Hoạt động'),
        (@lpDoi,      '203', N'Tầng 2', N'Khu B',      2, 4, N'Đã đặt'),
        (@lpDoi,      '204', N'Tầng 2', N'Khu B',      2, 4, N'Hoạt động'),
        (@lpStandard, '205', N'Tầng 2', N'Khu C',      2, 3, N'Hoạt động'),
        (@lpStandard, '206', N'Tầng 2', N'Khu C',      2, 3, N'Hoạt động'),
        (@lpSuite,    '301', N'Tầng 3', N'Khu VIP',    3, 5, N'Hoạt động'),
        (@lpSuite,    '302', N'Tầng 3', N'Khu VIP',    3, 5, N'Hoạt động'),
        (@lpDeluxe,   '303', N'Tầng 3', N'Khu B',      2, 4, N'Hoạt động'),
        (@lpDeluxe,   '304', N'Tầng 3', N'Khu B',      2, 4, N'Hoạt động'),
        (@lpFamily,   '305', N'Tầng 3', N'Khu Family', 4, 6, N'Hoạt động'),
        (@lpStandard, '306', N'Tầng 3', N'Khu C',      2, 3, N'Hoạt động'),
        (@lpFamily,   '401', N'Tầng 4', N'Khu Family', 4, 6, N'Hoạt động'),
        (@lpFamily,   '402', N'Tầng 4', N'Khu Family', 4, 6, N'Đang ở'),
        (@lpFamily,   '403', N'Tầng 4', N'Khu Family', 4, 6, N'Hoạt động'),
        (@lpDeluxe,   '404', N'Tầng 4', N'Khu B',      2, 4, N'Hoạt động'),
        (@lpStandard, '405', N'Tầng 4', N'Khu C',      2, 3, N'Hoạt động'),
        (@lpSuite,    '406', N'Tầng 4', N'Khu VIP',    3, 5, N'Hoạt động'),
        (@lpFamily,   '501', N'Tầng 5', N'Khu Family', 4, 6, N'Hoạt động'),
        (@lpSuite,    '502', N'Tầng 5', N'Khu VIP',    3, 5, N'Hoạt động'),
        (@lpSuite,    '503', N'Tầng 5', N'Khu VIP',    3, 5, N'Bảo trì'),
        (@lpSuite,    '504', N'Tầng 5', N'Khu VIP',    3, 5, N'Hoạt động'),
        (@lpDeluxe,   '505', N'Tầng 5', N'Khu C',      2, 4, N'Hoạt động'),
        (@lpDoi,      '506', N'Tầng 5', N'Khu C',      2, 4, N'Hoạt động')
    ) v(maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
)
MERGE dbo.Phong AS target
USING DesiredRooms AS source
ON target.soPhong = source.soPhong
WHEN MATCHED THEN
    UPDATE SET
        target.maLoaiPhong = source.maLoaiPhong,
        target.tang = source.tang,
        target.khuVuc = source.khuVuc,
        target.sucChuaChuan = source.sucChuaChuan,
        target.sucChuaToiDa = source.sucChuaToiDa,
        target.trangThai = source.trangThai
WHEN NOT MATCHED BY TARGET THEN
    INSERT (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
    VALUES (source.maLoaiPhong, source.soPhong, source.tang, source.khuVuc, source.sucChuaChuan, source.sucChuaToiDa, source.trangThai)
WHEN NOT MATCHED BY SOURCE
    AND NOT EXISTS (SELECT 1 FROM dbo.ChiTietDatPhong ctdp WHERE ctdp.maPhong = target.maPhong)
    AND NOT EXISTS (SELECT 1 FROM dbo.LuuTru lt WHERE lt.maPhong = target.maPhong)
THEN DELETE;
GO
