USE QLKS;
GO

IF OBJECT_ID('dbo.NgayLe', 'U') IS NULL
BEGIN
CREATE TABLE dbo.NgayLe (
                            maNgayLe INT IDENTITY(1,1) PRIMARY KEY,
                            tenNgayLe NVARCHAR(200) NOT NULL,
                            ngay DATE NOT NULL,
                            moTa NVARCHAR(500) NULL,
                            trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng'
);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.NgayLe WHERE ngay = '2026-01-01')
    INSERT INTO dbo.NgayLe (tenNgayLe, ngay, moTa, trangThai)
    VALUES (N'Tết Dương lịch', '2026-01-01', N'Nghỉ lễ đầu năm', N'Đang áp dụng');

IF NOT EXISTS (SELECT 1 FROM dbo.NgayLe WHERE ngay = '2026-04-30')
    INSERT INTO dbo.NgayLe (tenNgayLe, ngay, moTa, trangThai)
    VALUES (N'Ngày Giải phóng miền Nam', '2026-04-30', N'Ngày lễ quốc gia', N'Đang áp dụng');

IF NOT EXISTS (SELECT 1 FROM dbo.NgayLe WHERE ngay = '2026-05-01')
    INSERT INTO dbo.NgayLe (tenNgayLe, ngay, moTa, trangThai)
    VALUES (N'Quốc tế Lao động', '2026-05-01', N'Ngày lễ quốc gia', N'Đang áp dụng');

IF NOT EXISTS (SELECT 1 FROM dbo.NgayLe WHERE ngay = '2026-09-02')
    INSERT INTO dbo.NgayLe (tenNgayLe, ngay, moTa, trangThai)
    VALUES (N'Quốc khánh', '2026-09-02', N'Ngày lễ quốc gia', N'Đang áp dụng');
GO