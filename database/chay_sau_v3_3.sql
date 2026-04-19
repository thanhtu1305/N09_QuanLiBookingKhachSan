USE QLKS;
GO

IF COL_LENGTH('dbo.ChiTietDatPhong', 'checkInDuKien') IS NULL
BEGIN
ALTER TABLE dbo.ChiTietDatPhong
    ADD checkInDuKien DATETIME NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietDatPhong', 'checkOutDuKien') IS NULL
BEGIN
ALTER TABLE dbo.ChiTietDatPhong
    ADD checkOutDuKien DATETIME NULL;
END
GO

UPDATE ctdp
SET
    checkInDuKien = ISNULL(
            checkInDuKien,
            DATEADD(HOUR, 12, CAST(dp.ngayNhanPhong AS DATETIME))
                    ),
    checkOutDuKien = ISNULL(
            checkOutDuKien,
            DATEADD(HOUR, 12, CAST(dp.ngayTraPhong AS DATETIME))
                     )
    FROM dbo.ChiTietDatPhong ctdp
JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong;
GO