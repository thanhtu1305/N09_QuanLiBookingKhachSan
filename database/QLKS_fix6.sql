IF COL_LENGTH('dbo.ThanhToan', 'phuongThuc') IS NULL
BEGIN
ALTER TABLE dbo.ThanhToan
    ADD phuongThuc NVARCHAR(50) NOT NULL
        CONSTRAINT DF_ThanhToan_phuongThuc DEFAULT N'Tiền mặt';
END

IF COL_LENGTH('dbo.ThanhToan', 'soThamChieu') IS NULL
BEGIN
ALTER TABLE dbo.ThanhToan
    ADD soThamChieu NVARCHAR(100) NULL;
END

IF COL_LENGTH('dbo.ThanhToan', 'loaiGiaoDich') IS NULL
BEGIN
ALTER TABLE dbo.ThanhToan
    ADD loaiGiaoDich NVARCHAR(50) NULL;
END

IF COL_LENGTH('dbo.ThanhToan', 'ghiChu') IS NULL
BEGIN
ALTER TABLE dbo.ThanhToan
    ADD ghiChu NVARCHAR(500) NULL;
END

IF COL_LENGTH('dbo.ThanhToan', 'trangThai') IS NULL
BEGIN
ALTER TABLE dbo.ThanhToan
    ADD trangThai NVARCHAR(50) NOT NULL
        CONSTRAINT DF_ThanhToan_trangThai DEFAULT N'Đã thanh toán';
END