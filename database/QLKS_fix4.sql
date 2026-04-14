IF COL_LENGTH('dbo.HoaDon', 'phuThu') IS NULL
ALTER TABLE dbo.HoaDon ADD phuThu DECIMAL(15,0) NOT NULL CONSTRAINT DF_HoaDon_phuThu DEFAULT 0;

IF COL_LENGTH('dbo.HoaDon', 'giamGia') IS NULL
ALTER TABLE dbo.HoaDon ADD giamGia DECIMAL(15,0) NOT NULL CONSTRAINT DF_HoaDon_giamGia DEFAULT 0;

IF COL_LENGTH('dbo.HoaDon', 'tienCocTru') IS NULL
ALTER TABLE dbo.HoaDon ADD tienCocTru DECIMAL(15,0) NOT NULL CONSTRAINT DF_HoaDon_tienCocTru DEFAULT 0;

IF COL_LENGTH('dbo.HoaDon', 'trangThai') IS NULL
ALTER TABLE dbo.HoaDon ADD trangThai NVARCHAR(50) NOT NULL CONSTRAINT DF_HoaDon_trangThai DEFAULT N'Chờ thanh toán';

IF COL_LENGTH('dbo.HoaDon', 'ngayThanhToan') IS NULL
ALTER TABLE dbo.HoaDon ADD ngayThanhToan DATETIME NULL;

IF COL_LENGTH('dbo.HoaDon', 'ghiChu') IS NULL
ALTER TABLE dbo.HoaDon ADD ghiChu NVARCHAR(500) NULL;