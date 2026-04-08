-- FILE SQL TONG HOP QLKS
-- Gom: SQL goc + seed demo fixed + reports fixed
-- Chay toan bo file nay tu tren xuong duoi.

USE master;
GO

IF DB_ID('QLKS') IS NOT NULL
BEGIN
    ALTER DATABASE QLKS SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLKS;
END
GO

CREATE DATABASE QLKS;
GO
USE QLKS;
GO

CREATE TABLE NhanVien (
                          maNhanVien INT IDENTITY(1,1) PRIMARY KEY,
                          hoTen NVARCHAR(100),
                          ngaySinh DATE,
                          gioiTinh NVARCHAR(10),
                          cccd VARCHAR(20) UNIQUE,
                          soDienThoai VARCHAR(15),
                          email VARCHAR(100),
                          diaChi NVARCHAR(255),
                          boPhan NVARCHAR(50),
                          chucVu NVARCHAR(50),
                          caLam NVARCHAR(20),
                          ngayVaoLam DATE,
                          trangThai NVARCHAR(30),
                          ghiChu NVARCHAR(MAX)
);

CREATE TABLE TaiKhoan (
                          maTaiKhoan INT IDENTITY(1,1) PRIMARY KEY,
                          maNhanVien INT,
                          tenDangNhap VARCHAR(50) UNIQUE,
                          matKhau VARCHAR(255),
                          vaiTro NVARCHAR(30),
                          trangThai NVARCHAR(30),
                          lanDangNhapCuoi DATETIME,
                          emailKhoiPhuc VARCHAR(100),
                          FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);

CREATE TABLE TaiKhoanQuyen (
                               maTaiKhoan INT PRIMARY KEY,
                               permDashboard BIT NOT NULL DEFAULT 0,
                               permDatPhong BIT NOT NULL DEFAULT 0,
                               permCheckInOut BIT NOT NULL DEFAULT 0,
                               permThanhToan BIT NOT NULL DEFAULT 0,
                               permKhachHang BIT NOT NULL DEFAULT 0,
                               permPhong BIT NOT NULL DEFAULT 0,
                               permLoaiPhong BIT NOT NULL DEFAULT 0,
                               permBangGia BIT NOT NULL DEFAULT 0,
                               permDichVu BIT NOT NULL DEFAULT 0,
                               permTienNghi BIT NOT NULL DEFAULT 0,
                               permTaiKhoan BIT NOT NULL DEFAULT 0,
                               permNhanVien BIT NOT NULL DEFAULT 0,
                               permBaoCao BIT NOT NULL DEFAULT 0,
                               FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(maTaiKhoan)
);

CREATE TABLE KhachHang (
                           maKhachHang INT IDENTITY(1,1) PRIMARY KEY,
                           hoTen NVARCHAR(100),
                           gioiTinh NVARCHAR(10),
                           ngaySinh DATE,
                           soDienThoai VARCHAR(15),
                           email VARCHAR(100),
                           cccdPassport VARCHAR(30),
                           diaChi NVARCHAR(255),
                           quocTich NVARCHAR(50),
                           loaiKhach NVARCHAR(30),
                           hangKhach NVARCHAR(30),
                           trangThai NVARCHAR(30),
                           nguoiTao VARCHAR(50),
                           ghiChu NVARCHAR(MAX)
);

CREATE TABLE TienNghi (
                          maTienNghi INT IDENTITY(1,1) PRIMARY KEY,
                          tenTienNghi NVARCHAR(100),
                          nhomTienNghi NVARCHAR(50),
                          trangThai NVARCHAR(30),
                          uuTien INT,
                          moTa NVARCHAR(MAX)
);

CREATE TABLE LoaiPhong (
                           maLoaiPhong INT IDENTITY(1,1) PRIMARY KEY,
                           tenLoaiPhong NVARCHAR(100),
                           sucChua INT,
                           khachToiDa INT,
                           dienTich DECIMAL(6,1),
                           loaiGiuong NVARCHAR(50),
                           giaThamChieu DECIMAL(15,0),
                           trangThai NVARCHAR(30),
                           moTa NVARCHAR(MAX)
);

CREATE TABLE LoaiPhongTienNghi (
                                   maLoaiPhong INT,
                                   maTienNghi INT,
                                   PRIMARY KEY (maLoaiPhong, maTienNghi),
                                   FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong),
                                   FOREIGN KEY (maTienNghi) REFERENCES TienNghi(maTienNghi)
);

CREATE TABLE Phong (
                       maPhong INT IDENTITY(1,1) PRIMARY KEY,
                       maLoaiPhong INT,
                       soPhong VARCHAR(10) UNIQUE,
                       tang NVARCHAR(10),
                       khuVuc NVARCHAR(20),
                       sucChuaChuan INT,
                       sucChuaToiDa INT,
                       trangThai NVARCHAR(30),
                       FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);

CREATE TABLE BangGia (
                         maBangGia INT IDENTITY(1,1) PRIMARY KEY,
                         tenBangGia NVARCHAR(100),
                         maLoaiPhong INT,
                         ngayBatDau DATE,
                         ngayKetThuc DATE,
                         loaiNgay NVARCHAR(30),
                         trangThai NVARCHAR(30),
                         FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);

CREATE TABLE ChiTietBangGia (
                                maChiTietBangGia INT IDENTITY(1,1) PRIMARY KEY,
                                maBangGia INT,
                                loaiNgay NVARCHAR(30),
                                khungGio NVARCHAR(50),
                                giaTheoGio DECIMAL(15,0),
                                giaQuaDem DECIMAL(15,0),
                                giaTheoNgay DECIMAL(15,0),
                                giaCuoiTuan DECIMAL(15,0),
                                giaLe DECIMAL(15,0),
                                phuThu DECIMAL(15,0),
                                FOREIGN KEY (maBangGia) REFERENCES BangGia(maBangGia)
);



/* =====================
   HOTFIX NGAY LE V2 - KHOP DAO JAVA
   ===================== */

/* =====================
   HOTFIX NGAY LE V2 - KHOP DAO JAVA
   Sua loi: Invalid column name 'loaiNgay' / 'heSoPhuThu'
   ===================== */

IF OBJECT_ID('dbo.NgayLe', 'U') IS NULL
BEGIN
CREATE TABLE dbo.NgayLe (
                            maNgayLe INT IDENTITY(1,1) PRIMARY KEY,
                            tenNgayLe NVARCHAR(100) NOT NULL,
                            ngayBatDau DATE NULL,
                            ngayKetThuc DATE NULL,
                            loaiNgay NVARCHAR(30) NOT NULL DEFAULT N'Ngày lễ',
                            heSoPhuThu DECIMAL(5,2) NOT NULL DEFAULT 1.00,
                            trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng',
                            ghiChu NVARCHAR(MAX) NULL,
    /* cot tuong thich voi cac ban SQL cu */
                            ngayLe DATE NULL,
    [ngay] DATE NULL,
                            tuNgay DATE NULL,
                            denNgay DATE NULL,
                            heSoGia DECIMAL(5,2) NULL,
                            phuThu DECIMAL(15,0) NULL,
                            phanTramPhuThu DECIMAL(5,2) NULL,
                            moTa NVARCHAR(255) NULL
);
END
GO

IF COL_LENGTH('dbo.NgayLe', 'ngayBatDau') IS NULL
ALTER TABLE dbo.NgayLe ADD ngayBatDau DATE NULL;
IF COL_LENGTH('dbo.NgayLe', 'ngayKetThuc') IS NULL
ALTER TABLE dbo.NgayLe ADD ngayKetThuc DATE NULL;
IF COL_LENGTH('dbo.NgayLe', 'loaiNgay') IS NULL
ALTER TABLE dbo.NgayLe ADD loaiNgay NVARCHAR(30) NULL;
IF COL_LENGTH('dbo.NgayLe', 'heSoPhuThu') IS NULL
ALTER TABLE dbo.NgayLe ADD heSoPhuThu DECIMAL(5,2) NULL;
IF COL_LENGTH('dbo.NgayLe', 'trangThai') IS NULL
ALTER TABLE dbo.NgayLe ADD trangThai NVARCHAR(30) NULL;
IF COL_LENGTH('dbo.NgayLe', 'ghiChu') IS NULL
ALTER TABLE dbo.NgayLe ADD ghiChu NVARCHAR(MAX) NULL;

/* cot tuong thich neu DB dang o schema cu */
IF COL_LENGTH('dbo.NgayLe', 'ngayLe') IS NULL
ALTER TABLE dbo.NgayLe ADD ngayLe DATE NULL;
IF COL_LENGTH('dbo.NgayLe', 'ngay') IS NULL
ALTER TABLE dbo.NgayLe ADD [ngay] DATE NULL;
IF COL_LENGTH('dbo.NgayLe', 'tuNgay') IS NULL
ALTER TABLE dbo.NgayLe ADD tuNgay DATE NULL;
IF COL_LENGTH('dbo.NgayLe', 'denNgay') IS NULL
ALTER TABLE dbo.NgayLe ADD denNgay DATE NULL;
IF COL_LENGTH('dbo.NgayLe', 'heSoGia') IS NULL
ALTER TABLE dbo.NgayLe ADD heSoGia DECIMAL(5,2) NULL;
IF COL_LENGTH('dbo.NgayLe', 'phuThu') IS NULL
ALTER TABLE dbo.NgayLe ADD phuThu DECIMAL(15,0) NULL;
IF COL_LENGTH('dbo.NgayLe', 'phanTramPhuThu') IS NULL
ALTER TABLE dbo.NgayLe ADD phanTramPhuThu DECIMAL(5,2) NULL;
IF COL_LENGTH('dbo.NgayLe', 'moTa') IS NULL
ALTER TABLE dbo.NgayLe ADD moTa NVARCHAR(255) NULL;
GO

/* dong bo du lieu ve dung schema ma DAO dang doc */
UPDATE dbo.NgayLe
SET ngayBatDau = COALESCE(ngayBatDau, tuNgay, ngayLe, [ngay]),
    ngayKetThuc = COALESCE(ngayKetThuc, denNgay, ngayLe, [ngay], ngayBatDau),
    loaiNgay = ISNULL(NULLIF(loaiNgay, N''), N'Ngày lễ'),
    heSoPhuThu = COALESCE(
            heSoPhuThu,
            heSoGia,
            CASE
                WHEN phanTramPhuThu IS NOT NULL THEN CAST(1 + (phanTramPhuThu / 100.0) AS DECIMAL(5,2))
                ELSE CAST(1.00 AS DECIMAL(5,2))
                END
                 ),
    trangThai = ISNULL(NULLIF(trangThai, N''), N'Đang áp dụng'),
    ghiChu = ISNULL(ghiChu, N'');
GO

/* seed neu bang chua co du lieu */
IF NOT EXISTS (SELECT 1 FROM dbo.NgayLe)
BEGIN
INSERT INTO dbo.NgayLe
(tenNgayLe, ngayBatDau, ngayKetThuc, loaiNgay, heSoPhuThu, trangThai, ghiChu,
 ngayLe, [ngay], tuNgay, denNgay, heSoGia, phuThu, phanTramPhuThu, moTa)
VALUES
    (N'Tết Dương lịch 2026', '2026-01-01', '2026-01-01', N'Ngày lễ', 1.20, N'Đang áp dụng', N'', '2026-01-01', '2026-01-01', '2026-01-01', '2026-01-01', 1.20, 0, 20, N'Nghỉ lễ đầu năm'),
    (N'Giỗ Tổ Hùng Vương 2026', '2026-04-25', '2026-04-25', N'Ngày lễ', 1.20, N'Đang áp dụng', N'', '2026-04-25', '2026-04-25', '2026-04-25', '2026-04-25', 1.20, 0, 20, N'Ngày lễ truyền thống'),
    (N'Ngày Giải phóng miền Nam 2026', '2026-04-30', '2026-04-30', N'Ngày lễ', 1.30, N'Đang áp dụng', N'', '2026-04-30', '2026-04-30', '2026-04-30', '2026-04-30', 1.30, 0, 30, N'Lễ 30/4'),
    (N'Quốc tế Lao động 2026', '2026-05-01', '2026-05-01', N'Ngày lễ', 1.30, N'Đang áp dụng', N'', '2026-05-01', '2026-05-01', '2026-05-01', '2026-05-01', 1.30, 0, 30, N'Lễ 1/5'),
    (N'Quốc khánh 2026', '2026-09-02', '2026-09-02', N'Ngày lễ', 1.30, N'Đang áp dụng', N'', '2026-09-02', '2026-09-02', '2026-09-02', '2026-09-02', 1.30, 0, 30, N'Lễ 2/9');
END
GO

/* dam bao cac dong cu co ngayBatDau/ngayKetThuc hop le */
DELETE FROM dbo.NgayLe
WHERE ngayBatDau IS NULL OR ngayKetThuc IS NULL;
GO

/* kiem tra nhanh */
SELECT maNgayLe, tenNgayLe, ngayBatDau, ngayKetThuc, loaiNgay, heSoPhuThu, trangThai
FROM dbo.NgayLe
ORDER BY ngayBatDau, maNgayLe;
GO

CREATE TABLE DatPhong (
                          maDatPhong INT IDENTITY(1,1) PRIMARY KEY,
                          maKhachHang INT,
                          maNhanVien INT,
                          maBangGia INT,
                          ngayDat DATE,
                          ngayNhanPhong DATE,
                          ngayTraPhong DATE,
                          soLuongPhong INT,
                          soNguoi INT,
                          tienCoc DECIMAL(15,0),
                          trangThai NVARCHAR(30),
                          ghiChu NVARCHAR(MAX),
                          FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
                          FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
                          FOREIGN KEY (maBangGia) REFERENCES BangGia(maBangGia)
);

CREATE TABLE ChiTietDatPhong (
                                 maChiTietDatPhong INT IDENTITY(1,1) PRIMARY KEY,
                                 maDatPhong INT,
                                 maPhong INT,
                                 soNguoi INT,
                                 giaPhong DECIMAL(15,0),
                                 thanhTien DECIMAL(15,0),
                                 FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong),
                                 FOREIGN KEY (maPhong) REFERENCES Phong(maPhong)
);

CREATE TABLE LuuTru (
                        maLuuTru INT IDENTITY(1,1) PRIMARY KEY,
                        maChiTietDatPhong INT,
                        maDatPhong INT,
                        maPhong INT,
                        checkIn DATETIME,
                        checkOut DATETIME,
                        soNguoi INT,
                        giaPhong DECIMAL(15,0),
                        tienCoc DECIMAL(15,0),
                        FOREIGN KEY (maChiTietDatPhong) REFERENCES ChiTietDatPhong(maChiTietDatPhong),
                        FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong),
                        FOREIGN KEY (maPhong) REFERENCES Phong(maPhong)
);

CREATE TABLE DichVu (
                        maDichVu INT IDENTITY(1,1) PRIMARY KEY,
                        tenDichVu NVARCHAR(100),
                        donGia DECIMAL(15,0),
                        donVi NVARCHAR(20)
);

CREATE TABLE SuDungDichVu (
                              maSuDung INT IDENTITY(1,1) PRIMARY KEY,
                              maLuuTru INT,
                              maDichVu INT,
                              soLuong INT,
                              donGia DECIMAL(15,0),
                              thanhTien AS (soLuong * donGia) PERSISTED,
                              FOREIGN KEY (maLuuTru) REFERENCES LuuTru(maLuuTru),
                              FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
);

CREATE TABLE HoaDon (
                        maHoaDon INT IDENTITY(1,1) PRIMARY KEY,
                        maLuuTru INT,
                        maDatPhong INT,
                        maKhachHang INT,
                        ngayLap DATETIME DEFAULT GETDATE(),
                        tienPhong DECIMAL(15,0) DEFAULT 0,
                        tienDichVu DECIMAL(15,0) DEFAULT 0,
                        phuThu DECIMAL(15,0) DEFAULT 0,
                        giamGia DECIMAL(15,0) DEFAULT 0,
                        tienCocTru DECIMAL(15,0) DEFAULT 0,
                        trangThai NVARCHAR(30) DEFAULT N'Chờ thanh toán',
                        ghiChu NVARCHAR(MAX) NULL,
                        ngayThanhToan DATETIME NULL,
                        tongTien AS (ISNULL(tienPhong,0) + ISNULL(tienDichVu,0) + ISNULL(phuThu,0) - ISNULL(giamGia,0)) PERSISTED,
                        FOREIGN KEY (maLuuTru) REFERENCES LuuTru(maLuuTru)
);

CREATE TABLE ChiTietHoaDon (
                               maChiTietHoaDon INT IDENTITY(1,1) PRIMARY KEY,
                               maHoaDon INT,
                               loaiChiPhi NVARCHAR(50),
                               soLuong INT,
                               donGia DECIMAL(15,0),
                               thanhTien AS (soLuong * donGia) PERSISTED,
                               FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon)
);

CREATE TABLE ThanhToan (
                           maThanhToan INT IDENTITY(1,1) PRIMARY KEY,
                           maHoaDon INT,
                           maNhanVien INT,
                           ngayThanhToan DATETIME DEFAULT GETDATE(),
                           soTien DECIMAL(15,0) DEFAULT 0,
                           phuongThuc NVARCHAR(30) DEFAULT N'Tiền mặt',
                           soThamChieu NVARCHAR(100) DEFAULT N'',
                           ghiChu NVARCHAR(MAX) NULL,
                           loaiGiaoDich NVARCHAR(30) DEFAULT N'THANH_TOAN',
                           FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
                           FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

INSERT INTO NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES
(N'Nguyễn Văn A', '2000-01-01', N'Nam', '012345678901', '0909000001', 'nva@gmail.com', N'Hà Nội', N'Lễ Tân', N'Ca sáng', '2024-01-01', N'Hoạt động', N'Nhân viên lễ tân'),
(N'Trần Thị B', '1998-05-10', N'Nữ', '012345678902', '0909000002', 'ttb@gmail.com', N'Đà Nẵng', N'Quản lí', N'Ca hành chính', '2023-01-01', N'Hoạt động', N'Quản lý khách sạn'),
(N'Lê Văn C', '1999-08-20', N'Nam', '012345678903', '0909000003', 'lvc@gmail.com', N'TP.HCM', N'Lễ Tân', N'Ca chiều', '2024-02-15', N'Hoạt động', N'Nhân viên Lễ tân');
GO

INSERT INTO TaiKhoan (maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc)
VALUES
(1, 'letan1', '123', N'Lễ tân', N'Hoạt động', GETDATE(), 'letan1@gmail.com'),
(2, 'quanli1', '123', N'Quản lí', N'Hoạt động', GETDATE(), 'quanli1@gmail.com'),
(3, 'nhanvien1', '123', N'Lễ tân', N'Hoạt động', GETDATE(), 'nhanvien1@gmail.com');
GO

INSERT INTO KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES
(N'Phạm Minh Hùng', N'Nam', '1995-04-12', '0911111111', 'hung@gmail.com', '079123456789', N'Hà Nội', N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', 'quanli1', N'Khách quen'),
(N'Nguyễn Thị Lan', N'Nữ', '1997-09-20', '0922222222', 'lan@gmail.com', '079123456780', N'Đà Nẵng', N'Việt Nam', N'Cá nhân', N'VIP', N'Hoạt động', 'letan1', N'Ưu tiên check-in sớm'),
(N'John Smith', N'Nam', '1990-06-10', '0933333333', 'john@gmail.com', 'P12345678', N'New York', N'Mỹ', N'Nước ngoài', N'VIP', N'Hoạt động', 'letan1', N'Khách quốc tế');
GO

INSERT INTO TienNghi (tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa)
VALUES
(N'Wifi', N'Cơ bản', N'Đang áp dụng', 1, N'Kết nối internet không dây'),
(N'TV', N'Cơ bản', N'Đang áp dụng', 2, N'Tivi trong phòng'),
(N'Điều hòa', N'Cơ bản', N'Đang áp dụng', 3, N'Thiết bị làm mát phòng'),
(N'Nước nóng', N'Cơ bản', N'Đang áp dụng', 4, N'Cung cấp nước nóng'),
(N'Tủ quần áo', N'Cơ bản', N'Đang áp dụng', 5, N'Tủ để quần áo'),
(N'Smart TV', N'Giải trí', N'Đang áp dụng', 1, N'Tivi thông minh kết nối internet'),
(N'Netflix', N'Giải trí', N'Đang áp dụng', 2, N'Hỗ trợ xem Netflix'),
(N'Loa Bluetooth', N'Giải trí', N'Đang áp dụng', 3, N'Loa kết nối bluetooth'),
(N'Bồn tắm', N'Phòng tắm', N'Đang áp dụng', 1, N'Bồn tắm riêng'),
(N'Vòi sen', N'Phòng tắm', N'Đang áp dụng', 2, N'Vòi sen tắm đứng'),
(N'Máy sấy tóc', N'Phòng tắm', N'Đang áp dụng', 3, N'Máy sấy tóc'),
(N'Khăn tắm', N'Phòng tắm', N'Đang áp dụng', 4, N'Khăn tắm sạch'),
(N'Minibar', N'Tiện nghi bổ sung', N'Đang áp dụng', 1, N'Tủ minibar trong phòng'),
(N'Tủ lạnh', N'Tiện nghi bổ sung', N'Đang áp dụng', 2, N'Tủ lạnh mini'),
(N'Ấm đun nước', N'Tiện nghi bổ sung', N'Đang áp dụng', 3, N'Ấm đun siêu tốc'),
(N'Két sắt', N'Tiện nghi bổ sung', N'Đang áp dụng', 4, N'Két sắt bảo quản tài sản'),
(N'Khóa từ', N'An toàn', N'Đang áp dụng', 1, N'Khóa cửa bằng thẻ từ'),
(N'Chuông báo cháy', N'An toàn', N'Đang áp dụng', 2, N'Hệ thống báo cháy'),
(N'Bình chữa cháy', N'An toàn', N'Đang áp dụng', 3, N'Trang bị bình chữa cháy');
GO

INSERT INTO LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES
(N'Standard', 2, 3, 22.5, N'1 giường đôi', 500000, N'Đang áp dụng', N'Phòng tiêu chuẩn'),
(N'Deluxe', 2, 4, 28.0, N'2 giường đơn', 800000, N'Đang áp dụng', N'Phòng cao cấp'),
(N'Suite', 3, 5, 40.0, N'1 giường king', 1500000, N'Đang áp dụng', N'Phòng hạng sang'),
(N'Family', 4, 6, 45.0, N'2 giường đôi', 1800000, N'Đang áp dụng', N'Phòng gia đình');
GO

INSERT INTO LoaiPhongTienNghi (maLoaiPhong, maTienNghi)
VALUES
(1,1),(1,2),(1,3),(1,4),(1,10),
(2,1),(2,2),(2,3),(2,4),(2,6),(2,7),(2,13),
(3,1),(3,3),(3,4),(3,6),(3,7),(3,9),(3,11),(3,13),(3,14),(3,16),
(4,1),(4,2),(4,3),(4,4),(4,10),(4,11),(4,12),(4,17);
GO

INSERT INTO Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
VALUES
(1, '101', N'Tầng 1', N'Khu A', 2, 3, N'Hoạt động'),
(1, '102', N'Tầng 1', N'Khu A', 2, 3, N'Đã đặt'),
(2, '201', N'Tầng 2', N'Khu A', 2, 4, N'Đang ở'),
(2, '202', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'),
(3, '301', N'Tầng 3', N'Khu VIP', 3, 5, N'Bảo trì'),
(4, '401', N'Tầng 4', N'Khu Family', 4, 6, N'Hoạt động');
GO

INSERT INTO BangGia (tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai)
VALUES
(N'Bảng giá Standard 2026', 1, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá Deluxe 2026', 2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá Suite 2026', 3, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
(N'Bảng giá Family 2026', 4, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng');
GO

INSERT INTO ChiTietBangGia (maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
VALUES
(1, N'Thường', N'00:00-23:59', 120000, 350000, 500000, 600000, 700000, 50000),
(2, N'Thường', N'00:00-23:59', 180000, 550000, 800000, 950000, 1100000, 80000),
(3, N'Thường', N'00:00-23:59', 300000, 1000000, 1500000, 1700000, 2000000, 120000),
(4, N'Thường', N'00:00-23:59', 350000, 1200000, 1800000, 2000000, 2200000, 150000);
GO

INSERT INTO DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES
(1, 1, 1, '2026-03-20', '2026-03-26', '2026-03-27', 1, 2, 200000, N'Đã xác nhận', N'Khách đặt trước qua lễ tân'),
(2, 1, 2, '2026-03-21', '2026-03-26', '2026-03-28', 1, 2, 300000, N'Đang lưu trú', N'Khách đã check-in'),
(3, 2, 3, '2026-03-22', '2026-03-27', '2026-03-29', 1, 3, 500000, N'Chờ check-in', N'Khách sẽ đến buổi chiều');
GO

INSERT INTO ChiTietDatPhong (maDatPhong, maPhong, soNguoi, giaPhong, thanhTien)
VALUES
(1, 2, 2, 500000, 500000),
(2, 3, 2, 800000, 1600000),
(3, 6, 3, 1500000, 3000000);
GO

INSERT INTO LuuTru (maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc)
VALUES
(2, 2, 3, '2026-03-26 14:00:00', '2026-03-28 12:00:00', 2, 800000, 300000);
GO

INSERT INTO DichVu (tenDichVu, donGia, donVi)
VALUES
(N'Nước suối', 10000, N'Chai'),
(N'Bữa sáng', 80000, N'Suất'),
(N'Giặt ủi', 50000, N'Kg'),
(N'Thuê xe máy', 150000, N'Ngày'),
(N'Đưa đón sân bay', 250000, N'Lượt');
GO

INSERT INTO SuDungDichVu (maLuuTru, maDichVu, soLuong, donGia)
VALUES
(1, 1, 2, 10000),
(1, 2, 2, 80000),
(1, 3, 1, 50000);
GO

INSERT INTO HoaDon (maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu)
VALUES
(1, 2, 2, GETDATE(), 1600000, 230000);
GO

INSERT INTO ChiTietHoaDon (maHoaDon, loaiChiPhi, soLuong, donGia)
VALUES
(1, N'Tiền phòng', 2, 800000),
(1, N'Nước suối', 2, 10000),
(1, N'Bữa sáng', 2, 80000),
(1, N'Giặt ủi', 1, 50000);
GO

INSERT INTO ThanhToan (maHoaDon, maNhanVien, ngayThanhToan, soTien)
VALUES
(1, 1, GETDATE(), 1830000);
GO

/* Chạy 1 lần nếu muốn nâng schema thanh toán ngay trên SQL Server.
   DAO cũng tự bổ sung các cột này khi màn Thanh toán chạy. */

IF COL_LENGTH('HoaDon', 'phuThu') IS NULL ALTER TABLE HoaDon ADD phuThu DECIMAL(15,0) NULL;
IF COL_LENGTH('HoaDon', 'giamGia') IS NULL ALTER TABLE HoaDon ADD giamGia DECIMAL(15,0) NULL;
IF COL_LENGTH('HoaDon', 'tienCocTru') IS NULL ALTER TABLE HoaDon ADD tienCocTru DECIMAL(15,0) NULL;
IF COL_LENGTH('HoaDon', 'trangThai') IS NULL ALTER TABLE HoaDon ADD trangThai NVARCHAR(30) NULL;
IF COL_LENGTH('HoaDon', 'ghiChu') IS NULL ALTER TABLE HoaDon ADD ghiChu NVARCHAR(MAX) NULL;
IF COL_LENGTH('HoaDon', 'ngayThanhToan') IS NULL ALTER TABLE HoaDon ADD ngayThanhToan DATETIME NULL;

UPDATE HoaDon
SET phuThu = ISNULL(phuThu, 0),
    giamGia = ISNULL(giamGia, 0),
    tienCocTru = ISNULL(tienCocTru, 0),
    trangThai = ISNULL(trangThai, N'Chờ thanh toán'),
    ghiChu = ISNULL(ghiChu, N'');

IF COL_LENGTH('ThanhToan', 'phuongThuc') IS NULL ALTER TABLE ThanhToan ADD phuongThuc NVARCHAR(30) NULL;
IF COL_LENGTH('ThanhToan', 'soThamChieu') IS NULL ALTER TABLE ThanhToan ADD soThamChieu NVARCHAR(100) NULL;
IF COL_LENGTH('ThanhToan', 'ghiChu') IS NULL ALTER TABLE ThanhToan ADD ghiChu NVARCHAR(MAX) NULL;
IF COL_LENGTH('ThanhToan', 'loaiGiaoDich') IS NULL ALTER TABLE ThanhToan ADD loaiGiaoDich NVARCHAR(30) NULL;

UPDATE ThanhToan
SET phuongThuc = ISNULL(phuongThuc, N'Tiền mặt'),
    soThamChieu = ISNULL(soThamChieu, N''),
    ghiChu = ISNULL(ghiChu, N''),
    loaiGiaoDich = ISNULL(loaiGiaoDich, N'THANH_TOAN');

IF COL_LENGTH('DatPhong', 'ghiChu') IS NULL ALTER TABLE DatPhong ADD ghiChu NVARCHAR(MAX) NULL;
UPDATE DatPhong SET ghiChu = ISNULL(ghiChu, N'');
UPDATE Phong SET trangThai = N'Hoạt động' WHERE trangThai = N'Trống';

/* ==================== SEED DEMO FULL FIXED ==================== */

USE QLKS;
GO
SET NOCOUNT ON;
GO

/* =====================
   1. NHAN VIEN / TAI KHOAN
   ===================== */
IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE cccd = '012345678901')
INSERT INTO dbo.NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES (N'Nguyễn Văn A', '2000-01-01', N'Nam', '012345678901', '0909000001', 'nva@gmail.com', N'Hà Nội', N'Lễ tân', N'Lễ tân', N'Ca sáng', '2024-01-01', N'Hoạt động', N'Nhân viên lễ tân');

IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE cccd = '012345678902')
INSERT INTO dbo.NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES (N'Trần Thị B', '1998-05-10', N'Nữ', '012345678902', '0909000002', 'ttb@gmail.com', N'Đà Nẵng', N'Điều hành', N'Quản lí', N'Ca hành chính', '2023-01-01', N'Hoạt động', N'Quản lý khách sạn');

IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE cccd = '012345678903')
INSERT INTO dbo.NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES (N'Lê Văn C', '1999-08-20', N'Nam', '012345678903', '0909000003', 'lvc@gmail.com', N'TP.HCM', N'Lễ tân', N'Lễ tân', N'Ca chiều', '2024-02-15', N'Hoạt động', N'Nhân viên lễ tân');

IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE cccd = '012345678904')
INSERT INTO dbo.NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES (N'Phạm Quang Dũng', '1996-11-15', N'Nam', '012345678904', '0909000004', 'pqd@gmail.com', N'Hải Phòng', N'Kế toán', N'Kế toán', N'Ca hành chính', '2023-06-01', N'Hoạt động', N'Phụ trách thanh toán và hóa đơn');

IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE cccd = '012345678905')
INSERT INTO dbo.NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES (N'Hoàng Mỹ Linh', '1997-07-09', N'Nữ', '012345678905', '0909000005', 'hml@gmail.com', N'Cần Thơ', N'Buồng phòng', N'Giám sát buồng', N'Ca sáng', '2024-03-01', N'Hoạt động', N'Phụ trách trạng thái phòng');

DECLARE @nvA INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678901');
DECLARE @nvB INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678902');
DECLARE @nvC INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678903');
DECLARE @nvD INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678904');
DECLARE @nvE INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678905');

IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap = 'letan1')
INSERT INTO dbo.TaiKhoan (maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc)
VALUES (@nvA, 'letan1', '123', N'Lễ tân', N'Hoạt động', GETDATE(), 'letan1@gmail.com');

IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap = 'quanli1')
INSERT INTO dbo.TaiKhoan (maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc)
VALUES (@nvB, 'quanli1', '123', N'Quản lí', N'Hoạt động', GETDATE(), 'quanli1@gmail.com');

IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap = 'nhanvien1')
INSERT INTO dbo.TaiKhoan (maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc)
VALUES (@nvC, 'nhanvien1', '123', N'Lễ tân', N'Hoạt động', GETDATE(), 'nhanvien1@gmail.com');

IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap = 'ketoan1')
INSERT INTO dbo.TaiKhoan (maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc)
VALUES (@nvD, 'ketoan1', '123', N'Kế toán', N'Hoạt động', GETDATE(), 'ketoan1@gmail.com');

MERGE dbo.TaiKhoanQuyen AS target
    USING (
    SELECT maTaiKhoan,
    CAST(1 AS BIT) permDashboard,
    CAST(1 AS BIT) permDatPhong,
    CAST(1 AS BIT) permCheckInOut,
    CAST(1 AS BIT) permThanhToan,
    CAST(1 AS BIT) permKhachHang,
    CAST(1 AS BIT) permPhong,
    CAST(1 AS BIT) permLoaiPhong,
    CAST(1 AS BIT) permBangGia,
    CAST(1 AS BIT) permDichVu,
    CAST(1 AS BIT) permTienNghi,
    CAST(1 AS BIT) permTaiKhoan,
    CAST(1 AS BIT) permNhanVien,
    CAST(1 AS BIT) permBaoCao
    FROM dbo.TaiKhoan
    WHERE tenDangNhap = 'quanli1'
    ) AS src
    ON target.maTaiKhoan = src.maTaiKhoan
    WHEN MATCHED THEN UPDATE SET
    permDashboard = src.permDashboard,
                          permDatPhong = src.permDatPhong,
                          permCheckInOut = src.permCheckInOut,
                          permThanhToan = src.permThanhToan,
                          permKhachHang = src.permKhachHang,
                          permPhong = src.permPhong,
                          permLoaiPhong = src.permLoaiPhong,
                          permBangGia = src.permBangGia,
                          permDichVu = src.permDichVu,
                          permTienNghi = src.permTienNghi,
                          permTaiKhoan = src.permTaiKhoan,
                          permNhanVien = src.permNhanVien,
                          permBaoCao = src.permBaoCao
                          WHEN NOT MATCHED THEN
                      INSERT (maTaiKhoan, permDashboard, permDatPhong, permCheckInOut, permThanhToan, permKhachHang, permPhong, permLoaiPhong, permBangGia, permDichVu, permTienNghi, permTaiKhoan, permNhanVien, permBaoCao)
                      VALUES (src.maTaiKhoan, src.permDashboard, src.permDatPhong, src.permCheckInOut, src.permThanhToan, src.permKhachHang, src.permPhong, src.permLoaiPhong, src.permBangGia, src.permDichVu, src.permTienNghi, src.permTaiKhoan, src.permNhanVien, src.permBaoCao);

/* =====================
   2. KHACH HANG
   ===================== */
IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE cccdPassport = '079123456789')
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES (N'Phạm Minh Hùng', N'Nam', '1995-04-12', '0911111111', 'hung@gmail.com', '079123456789', N'Hà Nội', N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', 'quanli1', N'Khách quen');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE cccdPassport = '079123456780')
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES (N'Nguyễn Thị Lan', N'Nữ', '1997-09-20', '0922222222', 'lan@gmail.com', '079123456780', N'Đà Nẵng', N'Việt Nam', N'Cá nhân', N'VIP', N'Hoạt động', 'letan1', N'Ưu tiên check-in sớm');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE cccdPassport = 'P12345678')
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES (N'John Smith', N'Nam', '1990-06-10', '0933333333', 'john@gmail.com', 'P12345678', N'New York', N'Mỹ', N'Nước ngoài', N'VIP', N'Hoạt động', 'letan1', N'Khách quốc tế');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE cccdPassport = '079123456781')
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES (N'Lê Hoài An', N'Nữ', '2001-08-14', '0944444444', 'anhoai@gmail.com', '079123456781', N'Huế', N'Việt Nam', N'Cá nhân', N'Thân thiết', N'Hoạt động', 'quanli1', N'Hay sử dụng dịch vụ giặt ủi');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE cccdPassport = '079123456782')
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES (N'Trương Quốc Bảo', N'Nam', '1989-03-05', '0955555555', 'bao@gmail.com', '079123456782', N'Quảng Nam', N'Việt Nam', N'Doanh nghiệp', N'VIP', N'Hoạt động', 'quanli1', N'Công tác thường xuyên');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE cccdPassport = 'P99887766')
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES (N'Sarah Lee', N'Nữ', '1992-02-17', '0966666666', 'sarah@gmail.com', 'P99887766', N'Seoul', N'Hàn Quốc', N'Nước ngoài', N'VIP', N'Hoạt động', 'letan1', N'Khách ưu tiên phòng yên tĩnh');

/* =====================
   3. TIEN NGHI / LOAI PHONG / PHONG
   ===================== */
INSERT INTO dbo.TienNghi (tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa)
SELECT v.tenTienNghi, v.nhomTienNghi, v.trangThai, v.uuTien, v.moTa
FROM (VALUES
          (N'Wifi', N'Cơ bản', N'Đang áp dụng', 1, N'Kết nối internet không dây'),
          (N'TV', N'Cơ bản', N'Đang áp dụng', 2, N'Tivi trong phòng'),
          (N'Điều hòa', N'Cơ bản', N'Đang áp dụng', 3, N'Thiết bị làm mát phòng'),
          (N'Nước nóng', N'Cơ bản', N'Đang áp dụng', 4, N'Cung cấp nước nóng'),
          (N'Tủ quần áo', N'Cơ bản', N'Đang áp dụng', 5, N'Tủ để quần áo'),
          (N'Smart TV', N'Giải trí', N'Đang áp dụng', 1, N'Tivi thông minh kết nối internet'),
          (N'Netflix', N'Giải trí', N'Đang áp dụng', 2, N'Hỗ trợ xem Netflix'),
          (N'Loa Bluetooth', N'Giải trí', N'Đang áp dụng', 3, N'Loa kết nối bluetooth'),
          (N'Bồn tắm', N'Phòng tắm', N'Đang áp dụng', 1, N'Bồn tắm riêng'),
          (N'Vòi sen', N'Phòng tắm', N'Đang áp dụng', 2, N'Vòi sen tắm đứng'),
          (N'Máy sấy tóc', N'Phòng tắm', N'Đang áp dụng', 3, N'Máy sấy tóc'),
          (N'Khăn tắm', N'Phòng tắm', N'Đang áp dụng', 4, N'Khăn tắm sạch'),
          (N'Minibar', N'Tiện nghi bổ sung', N'Đang áp dụng', 1, N'Tủ minibar trong phòng'),
          (N'Tủ lạnh', N'Tiện nghi bổ sung', N'Đang áp dụng', 2, N'Tủ lạnh mini'),
          (N'Ấm đun nước', N'Tiện nghi bổ sung', N'Đang áp dụng', 3, N'Ấm đun siêu tốc'),
          (N'Két sắt', N'Tiện nghi bổ sung', N'Đang áp dụng', 4, N'Két sắt bảo quản tài sản'),
          (N'Khóa từ', N'An toàn', N'Đang áp dụng', 1, N'Khóa cửa bằng thẻ từ'),
          (N'Chuông báo cháy', N'An toàn', N'Đang áp dụng', 2, N'Hệ thống báo cháy'),
          (N'Bình chữa cháy', N'An toàn', N'Đang áp dụng', 3, N'Trang bị bình chữa cháy'),
          (N'Bàn làm việc', N'Tiện nghi bổ sung', N'Đang áp dụng', 5, N'Bàn làm việc trong phòng'),
          (N'Sofa', N'Tiện nghi bổ sung', N'Đang áp dụng', 6, N'Sofa thư giãn'),
          (N'Ban công', N'Tiện nghi mở rộng', N'Đang áp dụng', 1, N'Ban công hướng phố hoặc sân vườn'),
          (N'Máy pha cà phê', N'Tiện nghi mở rộng', N'Đang áp dụng', 2, N'Máy pha cà phê cá nhân'),
          (N'Bếp mini', N'Tiện nghi mở rộng', N'Đang áp dụng', 3, N'Bếp mini cho lưu trú dài ngày')
     ) v(tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa)
WHERE NOT EXISTS (SELECT 1 FROM dbo.TienNghi t WHERE t.tenTienNghi = v.tenTienNghi);

INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
SELECT v.tenLoaiPhong, v.sucChua, v.khachToiDa, v.dienTich, v.loaiGiuong, v.giaThamChieu, v.trangThai, v.moTa
FROM (VALUES
          (N'Standard', 2, 3, 22.5, N'1 giường đôi', 500000, N'Đang áp dụng', N'Phòng tiêu chuẩn'),
          (N'Deluxe', 2, 4, 28.0, N'2 giường đơn', 800000, N'Đang áp dụng', N'Phòng cao cấp'),
          (N'Suite', 3, 5, 40.0, N'1 giường king', 1500000, N'Đang áp dụng', N'Phòng hạng sang'),
          (N'Family', 4, 6, 45.0, N'2 giường đôi', 1800000, N'Đang áp dụng', N'Phòng gia đình'),
          (N'Phòng đơn', 1, 2, 18.0, N'1 giường đơn', 350000, N'Đang áp dụng', N'Phòng nhỏ cho 1-2 khách'),
          (N'Phòng đôi', 2, 4, 26.0, N'1 giường đôi lớn', 650000, N'Đang áp dụng', N'Phòng đôi phù hợp cặp đôi / công tác')
     ) v(tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
WHERE NOT EXISTS (SELECT 1 FROM dbo.LoaiPhong lp WHERE lp.tenLoaiPhong = v.tenLoaiPhong);

DECLARE @lpStandard INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Standard');
DECLARE @lpDeluxe   INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe');
DECLARE @lpSuite    INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Suite');
DECLARE @lpFamily   INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family');
DECLARE @lpDon      INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn');
DECLARE @lpDoi      INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi');

INSERT INTO dbo.LoaiPhongTienNghi (maLoaiPhong, maTienNghi)
SELECT x.maLoaiPhong, x.maTienNghi
FROM (
         SELECT @lpStandard maLoaiPhong, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') maTienNghi UNION ALL
         SELECT @lpStandard, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'TV') UNION ALL
         SELECT @lpStandard, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Điều hòa') UNION ALL
         SELECT @lpStandard, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Nước nóng') UNION ALL
         SELECT @lpStandard, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Vòi sen') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Smart TV') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Netflix') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Minibar') UNION ALL
         SELECT @lpDeluxe, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bàn làm việc') UNION ALL
         SELECT @lpSuite, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpSuite, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bồn tắm') UNION ALL
         SELECT @lpSuite, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Sofa') UNION ALL
         SELECT @lpSuite, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Ban công') UNION ALL
         SELECT @lpSuite, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Máy pha cà phê') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Khăn tắm') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Tủ lạnh') UNION ALL
         SELECT @lpFamily, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bếp mini') UNION ALL
         SELECT @lpDon, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpDon, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Điều hòa') UNION ALL
         SELECT @lpDon, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bàn làm việc') UNION ALL
         SELECT @lpDoi, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi') UNION ALL
         SELECT @lpDoi, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Smart TV') UNION ALL
         SELECT @lpDoi, (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Ban công')
     ) x
WHERE x.maLoaiPhong IS NOT NULL AND x.maTienNghi IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM dbo.LoaiPhongTienNghi lptn
    WHERE lptn.maLoaiPhong = x.maLoaiPhong AND lptn.maTienNghi = x.maTienNghi
);

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
SELECT v.maLoaiPhong, v.soPhong, v.tang, v.khuVuc, v.sucChuaChuan, v.sucChuaToiDa, v.trangThai
FROM (VALUES
          (@lpStandard, '101', N'Tầng 1', N'Khu A', 2, 3, N'Hoạt động'),
          (@lpStandard, '102', N'Tầng 1', N'Khu A', 2, 3, N'Đã đặt'),
          (@lpDeluxe,   '201', N'Tầng 2', N'Khu A', 2, 4, N'Đang ở'),
          (@lpDeluxe,   '202', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'),
          (@lpSuite,    '301', N'Tầng 3', N'Khu VIP', 3, 5, N'Bảo trì'),
          (@lpFamily,   '401', N'Tầng 4', N'Khu Family', 4, 6, N'Hoạt động'),
          (@lpDon,      '103', N'Tầng 1', N'Khu A', 1, 2, N'Hoạt động'),
          (@lpDon,      '104', N'Tầng 1', N'Khu B', 1, 2, N'Hoạt động'),
          (@lpDoi,      '203', N'Tầng 2', N'Khu B', 2, 4, N'Đã đặt'),
          (@lpDoi,      '204', N'Tầng 2', N'Khu B', 2, 4, N'Hoạt động'),
          (@lpSuite,    '302', N'Tầng 3', N'Khu VIP', 3, 5, N'Hoạt động'),
          (@lpFamily,   '402', N'Tầng 4', N'Khu Family', 4, 6, N'Đang ở')
     ) v(maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
WHERE NOT EXISTS (SELECT 1 FROM dbo.Phong p WHERE p.soPhong = v.soPhong);

/* =====================
   4. BANG GIA / CHI TIET BANG GIA
   ===================== */
INSERT INTO dbo.BangGia (tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai)
SELECT v.tenBangGia, v.maLoaiPhong, v.ngayBatDau, v.ngayKetThuc, v.loaiNgay, v.trangThai
FROM (VALUES
          (N'Bảng giá Standard 2026', @lpStandard, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
          (N'Bảng giá Deluxe 2026', @lpDeluxe, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
          (N'Bảng giá Suite 2026', @lpSuite, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
          (N'Bảng giá Family 2026', @lpFamily, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
          (N'Bảng giá Phòng đơn 2026', @lpDon, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
          (N'Bảng giá Phòng đôi 2026', @lpDoi, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng')
     ) v(tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai)
WHERE NOT EXISTS (SELECT 1 FROM dbo.BangGia bg WHERE bg.tenBangGia = v.tenBangGia);

DECLARE @bgStandard INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Standard 2026');
DECLARE @bgDeluxe   INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Deluxe 2026');
DECLARE @bgSuite    INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Suite 2026');
DECLARE @bgFamily   INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Family 2026');
DECLARE @bgDon      INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Phòng đơn 2026');
DECLARE @bgDoi      INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Phòng đôi 2026');

INSERT INTO dbo.ChiTietBangGia (maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
SELECT v.maBangGia, v.loaiNgay, v.khungGio, v.giaTheoGio, v.giaQuaDem, v.giaTheoNgay, v.giaCuoiTuan, v.giaLe, v.phuThu
FROM (VALUES
          (@bgStandard, N'Thường', N'00:00-23:59', 120000, 350000, 500000, 600000, 700000, 50000),
          (@bgDeluxe,   N'Thường', N'00:00-23:59', 180000, 550000, 800000, 950000, 1100000, 80000),
          (@bgSuite,    N'Thường', N'00:00-23:59', 300000, 1000000, 1500000, 1700000, 2000000, 120000),
          (@bgFamily,   N'Thường', N'00:00-23:59', 350000, 1200000, 1800000, 2000000, 2200000, 150000),
          (@bgDon,      N'Thường', N'00:00-23:59', 90000, 250000, 350000, 420000, 500000, 30000),
          (@bgDoi,      N'Thường', N'00:00-23:59', 150000, 450000, 650000, 780000, 900000, 60000),
          (@bgStandard, N'Cuối tuần', N'00:00-23:59', 135000, 380000, 600000, 650000, 750000, 70000),
          (@bgDeluxe,   N'Cuối tuần', N'00:00-23:59', 200000, 600000, 950000, 1000000, 1150000, 90000)
     ) v(maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
WHERE v.maBangGia IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM dbo.ChiTietBangGia ct
    WHERE ct.maBangGia = v.maBangGia
      AND ISNULL(ct.loaiNgay, N'') = ISNULL(v.loaiNgay, N'')
      AND ISNULL(ct.khungGio, N'') = ISNULL(v.khungGio, N'')
);

/* =====================
   5. DICH VU
   ===================== */
INSERT INTO dbo.DichVu (tenDichVu, donGia, donVi)
SELECT v.tenDichVu, v.donGia, v.donVi
FROM (VALUES
          (N'Nước suối', 10000, N'Chai'),
          (N'Bữa sáng', 80000, N'Suất'),
          (N'Giặt ủi', 50000, N'Kg'),
          (N'Thuê xe máy', 150000, N'Ngày'),
          (N'Đưa đón sân bay', 250000, N'Lượt'),
          (N'Cà phê', 30000, N'Ly'),
          (N'Bữa tối', 180000, N'Suất'),
          (N'In ấn tài liệu', 5000, N'Trang'),
          (N'Thuê phòng họp mini', 400000, N'Giờ'),
          (N'Phụ thu thêm khách', 120000, N'Người')
     ) v(tenDichVu, donGia, donVi)
WHERE NOT EXISTS (SELECT 1 FROM dbo.DichVu dv WHERE dv.tenDichVu = v.tenDichVu);

/* =====================
   6. DAT PHONG HEADER-DETAIL
   ===================== */
DECLARE @khHung  INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456789');
DECLARE @khLan   INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456780');
DECLARE @khJohn  INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = 'P12345678');
DECLARE @khAn    INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456781');
DECLARE @khBao   INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456782');
DECLARE @khSarah INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = 'P99887766');

DECLARE @ph102 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '102');
DECLARE @ph201 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '201');
DECLARE @ph401 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '401');
DECLARE @ph103 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '103');
DECLARE @ph203 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '203');
DECLARE @ph204 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '204');
DECLARE @ph402 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '402');
DECLARE @ph302 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '302');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khHung AND ngayNhanPhong = '2026-03-26' AND ngayTraPhong = '2026-03-27')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khHung, @nvA, @bgStandard, '2026-03-20', '2026-03-26', '2026-03-27', 1, 2, 200000, N'Đã xác nhận', N'Khách đặt trước qua lễ tân');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khLan AND ngayNhanPhong = '2026-03-26' AND ngayTraPhong = '2026-03-28')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khLan, @nvA, @bgDeluxe, '2026-03-21', '2026-03-26', '2026-03-28', 1, 2, 300000, N'Đang lưu trú', N'Khách đã check-in');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khJohn AND ngayNhanPhong = '2026-03-27' AND ngayTraPhong = '2026-03-29')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khJohn, @nvB, @bgSuite, '2026-03-22', '2026-03-27', '2026-03-29', 1, 3, 500000, N'Chờ check-in', N'Khách sẽ đến buổi chiều');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khAn AND ngayNhanPhong = '2026-04-03' AND ngayTraPhong = '2026-04-04')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khAn, @nvA, @bgDon, '2026-04-01', '2026-04-03', '2026-04-04', 1, 1, 100000, N'Đã đặt', N'Khách đặt online, chờ xác nhận');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khBao AND ngayNhanPhong = '2026-04-05' AND ngayTraPhong = '2026-04-07')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khBao, @nvB, @bgDoi, '2026-04-02', '2026-04-05', '2026-04-07', 1, 2, 250000, N'Đã cọc', N'Khách công tác, cần xuất hóa đơn VAT');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khSarah AND ngayNhanPhong = '2026-04-06' AND ngayTraPhong = '2026-04-08')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khSarah, @nvA, @bgFamily, '2026-04-04', '2026-04-06', '2026-04-08', 1, 4, 600000, N'Chờ check-in', N'Gia đình có trẻ em');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khHung AND ngayNhanPhong = '2026-03-15' AND ngayTraPhong = '2026-03-17')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khHung, @nvA, @bgDoi, '2026-03-10', '2026-03-15', '2026-03-17', 1, 2, 200000, N'Đã check-out', N'Đã hoàn tất lưu trú');

IF NOT EXISTS (SELECT 1 FROM dbo.DatPhong WHERE maKhachHang = @khLan AND ngayNhanPhong = '2026-03-18' AND ngayTraPhong = '2026-03-19')
INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES (@khLan, @nvA, @bgDon, '2026-03-17', '2026-03-18', '2026-03-19', 1, 1, 100000, N'Đã thanh toán', N'Booking đã thanh toán xong');

DECLARE @dp1 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khHung  AND ngayNhanPhong = '2026-03-26' AND ngayTraPhong = '2026-03-27');
DECLARE @dp2 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khLan   AND ngayNhanPhong = '2026-03-26' AND ngayTraPhong = '2026-03-28');
DECLARE @dp3 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khJohn  AND ngayNhanPhong = '2026-03-27' AND ngayTraPhong = '2026-03-29');
DECLARE @dp4 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khAn    AND ngayNhanPhong = '2026-04-03' AND ngayTraPhong = '2026-04-04');
DECLARE @dp5 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khBao   AND ngayNhanPhong = '2026-04-05' AND ngayTraPhong = '2026-04-07');
DECLARE @dp6 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khSarah AND ngayNhanPhong = '2026-04-06' AND ngayTraPhong = '2026-04-08');
DECLARE @dp7 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khHung  AND ngayNhanPhong = '2026-03-15' AND ngayTraPhong = '2026-03-17');
DECLARE @dp8 INT = (SELECT maDatPhong FROM dbo.DatPhong WHERE maKhachHang = @khLan   AND ngayNhanPhong = '2026-03-18' AND ngayTraPhong = '2026-03-19');

INSERT INTO dbo.ChiTietDatPhong (maDatPhong, maPhong, soNguoi, giaPhong, thanhTien)
SELECT v.maDatPhong, v.maPhong, v.soNguoi, v.giaPhong, v.thanhTien
FROM (VALUES
          (@dp1, @ph102, 2, 500000, 500000),
          (@dp2, @ph201, 2, 800000, 1600000),
          (@dp3, @ph401, 3, 1500000, 3000000),
          (@dp4, @ph103, 1, 350000, 350000),
          (@dp5, @ph203, 2, 650000, 1300000),
          (@dp6, @ph402, 4, 1800000, 3600000),
          (@dp7, @ph204, 2, 650000, 1300000),
          (@dp8, @ph103, 1, 350000, 350000)
     ) v(maDatPhong, maPhong, soNguoi, giaPhong, thanhTien)
WHERE v.maDatPhong IS NOT NULL AND v.maPhong IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM dbo.ChiTietDatPhong c
    WHERE c.maDatPhong = v.maDatPhong AND c.maPhong = v.maPhong
);

/* =====================
   7. LUU TRU / SU DUNG DICH VU
   ===================== */
DECLARE @ctdp2 INT = (SELECT TOP 1 maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = @dp2 AND maPhong = @ph201);
DECLARE @ctdp7 INT = (SELECT TOP 1 maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = @dp7 AND maPhong = @ph204);
DECLARE @ctdp8 INT = (SELECT TOP 1 maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = @dp8 AND maPhong = @ph103);

IF NOT EXISTS (SELECT 1 FROM dbo.LuuTru WHERE maChiTietDatPhong = @ctdp2)
INSERT INTO dbo.LuuTru (maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc)
VALUES (@ctdp2, @dp2, @ph201, '2026-03-26T14:00:00', '2026-03-28T12:00:00', 2, 800000, 300000);

IF NOT EXISTS (SELECT 1 FROM dbo.LuuTru WHERE maChiTietDatPhong = @ctdp7)
INSERT INTO dbo.LuuTru (maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc)
VALUES (@ctdp7, @dp7, @ph204, '2026-03-15T13:30:00', '2026-03-17T11:30:00', 2, 650000, 200000);

IF NOT EXISTS (SELECT 1 FROM dbo.LuuTru WHERE maChiTietDatPhong = @ctdp8)
INSERT INTO dbo.LuuTru (maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc)
VALUES (@ctdp8, @dp8, @ph103, '2026-03-18T14:10:00', '2026-03-19T11:50:00', 1, 350000, 100000);

DECLARE @lt2 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maChiTietDatPhong = @ctdp2);
DECLARE @lt7 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maChiTietDatPhong = @ctdp7);
DECLARE @lt8 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maChiTietDatPhong = @ctdp8);
DECLARE @dvNuoc INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Nước suối');
DECLARE @dvSang INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Bữa sáng');
DECLARE @dvGiat INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Giặt ủi');
DECLARE @dvCafe INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Cà phê');
DECLARE @dvAirport INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Đưa đón sân bay');

INSERT INTO dbo.SuDungDichVu (maLuuTru, maDichVu, soLuong, donGia)
SELECT v.maLuuTru, v.maDichVu, v.soLuong, v.donGia
FROM (VALUES
          (@lt2, @dvNuoc, 2, 10000),
          (@lt2, @dvSang, 2, 80000),
          (@lt2, @dvGiat, 1, 50000),
          (@lt7, @dvCafe, 2, 30000),
          (@lt7, @dvAirport, 1, 250000),
          (@lt8, @dvNuoc, 1, 10000)
     ) v(maLuuTru, maDichVu, soLuong, donGia)
WHERE v.maLuuTru IS NOT NULL AND v.maDichVu IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM dbo.SuDungDichVu s
    WHERE s.maLuuTru = v.maLuuTru AND s.maDichVu = v.maDichVu AND s.soLuong = v.soLuong AND s.donGia = v.donGia
);

/* =====================
   8. HOA DON / CHI TIET HOA DON / THANH TOAN
   ===================== */
IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maDatPhong = @dp2)
INSERT INTO dbo.HoaDon (maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu, ngayThanhToan)
VALUES (@lt2, @dp2, @khLan, '2026-03-28T12:30:00', 1600000, 230000, 0, 0, 300000, N'Đã thanh toán', N'Đã thu đủ sau khi trừ tiền cọc', '2026-03-28T12:45:00');

IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maDatPhong = @dp7)
INSERT INTO dbo.HoaDon (maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu, ngayThanhToan)
VALUES (@lt7, @dp7, @khHung, '2026-03-17T11:45:00', 1300000, 310000, 50000, 100000, 200000, N'Đã thanh toán', N'Khách dùng thêm airport transfer', '2026-03-17T12:00:00');

IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maDatPhong = @dp8)
INSERT INTO dbo.HoaDon (maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu, ngayThanhToan)
VALUES (@lt8, @dp8, @khLan, '2026-03-19T11:55:00', 350000, 10000, 0, 0, 100000, N'Đã thanh toán', N'Booking ngắn ngày', '2026-03-19T12:00:00');

DECLARE @hd2 INT = (SELECT maHoaDon FROM dbo.HoaDon WHERE maDatPhong = @dp2);
DECLARE @hd7 INT = (SELECT maHoaDon FROM dbo.HoaDon WHERE maDatPhong = @dp7);
DECLARE @hd8 INT = (SELECT maHoaDon FROM dbo.HoaDon WHERE maDatPhong = @dp8);

INSERT INTO dbo.ChiTietHoaDon (maHoaDon, loaiChiPhi, soLuong, donGia)
SELECT v.maHoaDon, v.loaiChiPhi, v.soLuong, v.donGia
FROM (VALUES
          (@hd2, N'Tiền phòng', 2, 800000),
          (@hd2, N'Nước suối', 2, 10000),
          (@hd2, N'Bữa sáng', 2, 80000),
          (@hd2, N'Giặt ủi', 1, 50000),
          (@hd7, N'Tiền phòng', 2, 650000),
          (@hd7, N'Cà phê', 2, 30000),
          (@hd7, N'Đưa đón sân bay', 1, 250000),
          (@hd8, N'Tiền phòng', 1, 350000),
          (@hd8, N'Nước suối', 1, 10000)
     ) v(maHoaDon, loaiChiPhi, soLuong, donGia)
WHERE v.maHoaDon IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM dbo.ChiTietHoaDon c
    WHERE c.maHoaDon = v.maHoaDon AND c.loaiChiPhi = v.loaiChiPhi AND c.soLuong = v.soLuong AND c.donGia = v.donGia
);

INSERT INTO dbo.ThanhToan (maHoaDon, maNhanVien, ngayThanhToan, soTien, phuongThuc, soThamChieu, ghiChu, loaiGiaoDich)
SELECT v.maHoaDon, v.maNhanVien, v.ngayThanhToan, v.soTien, v.phuongThuc, v.soThamChieu, v.ghiChu, v.loaiGiaoDich
FROM (VALUES
          (@hd2, @nvA, '2026-03-28T12:45:00', 1530000, N'Tiền mặt', N'', N'Thanh toán sau khi trừ tiền cọc', N'THANH_TOAN'),
          (@hd7, @nvD, '2026-03-17T12:00:00', 1360000, N'Chuyển khoản', N'VCB-20260317-001', N'Thanh toán công tác', N'THANH_TOAN'),
          (@hd8, @nvD, '2026-03-19T12:00:00', 260000, N'Tiền mặt', N'', N'Thanh toán sau khi trừ tiền cọc', N'THANH_TOAN')
     ) v(maHoaDon, maNhanVien, ngayThanhToan, soTien, phuongThuc, soThamChieu, ghiChu, loaiGiaoDich)
WHERE v.maHoaDon IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM dbo.ThanhToan t
    WHERE t.maHoaDon = v.maHoaDon AND t.soTien = v.soTien AND t.ngayThanhToan = v.ngayThanhToan
);

/* =====================
   9. DONG BO TRANG THAI PHONG
   ===================== */
UPDATE p
SET p.trangThai = CASE
                      WHEN EXISTS (
                          SELECT 1
                          FROM dbo.LuuTru lt
                                   JOIN dbo.DatPhong dp ON dp.maDatPhong = lt.maDatPhong
                          WHERE lt.maPhong = p.maPhong
                            AND dp.trangThai IN (N'Đang lưu trú')
                      ) THEN N'Đang ở'
                      WHEN EXISTS (
                          SELECT 1
                          FROM dbo.ChiTietDatPhong ctdp
                                   JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong
                          WHERE ctdp.maPhong = p.maPhong
                            AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in')
                      ) THEN N'Đã đặt'
                      WHEN p.trangThai = N'Bảo trì' THEN N'Bảo trì'
                      ELSE N'Hoạt động'
    END
    FROM dbo.Phong p;

/* ==================== REPORTS VIEWS PROCS FIXED ==================== */

USE QLKS;
GO

IF OBJECT_ID('dbo.vw_DashboardTongQuan', 'V') IS NOT NULL
DROP VIEW dbo.vw_DashboardTongQuan;
GO
CREATE VIEW dbo.vw_DashboardTongQuan
AS
SELECT
    COUNT(*) AS tongPhong,
    SUM(CASE WHEN p.trangThai IN (N'Hoạt động') THEN 1 ELSE 0 END) AS phongHoatDong,
    SUM(CASE WHEN p.trangThai = N'Đang ở' THEN 1 ELSE 0 END) AS phongDangO,
    SUM(CASE WHEN p.trangThai = N'Đã đặt' THEN 1 ELSE 0 END) AS phongDaDat,
    SUM(CASE WHEN p.trangThai = N'Bảo trì' THEN 1 ELSE 0 END) AS phongBaoTri,
    (SELECT COUNT(*) FROM dbo.DatPhong dp WHERE CAST(dp.ngayDat AS DATE) = CAST(GETDATE() AS DATE)) AS bookingHomNay,
    (SELECT COUNT(*) FROM dbo.DatPhong dp WHERE CAST(dp.ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE)
                                            AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in')) AS choCheckInHomNay,
    (SELECT COUNT(*) FROM dbo.HoaDon hd WHERE ISNULL(hd.trangThai, N'Chờ thanh toán') = N'Chờ thanh toán') AS choThanhToan,
    (SELECT ISNULL(SUM(tt.soTien),0) FROM dbo.ThanhToan tt WHERE CAST(tt.ngayThanhToan AS DATE) = CAST(GETDATE() AS DATE)) AS doanhThuHomNay,
    (SELECT ISNULL(SUM(tt.soTien),0) FROM dbo.ThanhToan tt WHERE YEAR(tt.ngayThanhToan) = YEAR(GETDATE()) AND MONTH(tt.ngayThanhToan) = MONTH(GETDATE())) AS doanhThuThangNay
FROM dbo.Phong p;
GO

IF OBJECT_ID('dbo.vw_BaoCaoDoanhThuNgay', 'V') IS NOT NULL
DROP VIEW dbo.vw_BaoCaoDoanhThuNgay;
GO
CREATE VIEW dbo.vw_BaoCaoDoanhThuNgay
AS
SELECT
    CAST(hd.ngayLap AS DATE) AS ngay,
    COUNT(DISTINCT hd.maHoaDon) AS soHoaDon,
    ISNULL(SUM(ISNULL(hd.tienPhong,0)),0) AS tongTienPhong,
    ISNULL(SUM(ISNULL(hd.tienDichVu,0)),0) AS tongTienDichVu,
    ISNULL(SUM(ISNULL(hd.phuThu,0)),0) AS tongPhuThu,
    ISNULL(SUM(ISNULL(hd.giamGia,0)),0) AS tongGiamGia,
    ISNULL(SUM(ISNULL(hd.tienCocTru,0)),0) AS tongTienCocTru,
    ISNULL(SUM(ISNULL(hd.tienPhong,0) + ISNULL(hd.tienDichVu,0) + ISNULL(hd.phuThu,0) - ISNULL(hd.giamGia,0)),0) AS tongPhatSinh,
    ISNULL((
               SELECT SUM(tt.soTien)
               FROM dbo.ThanhToan tt
               WHERE CAST(tt.ngayThanhToan AS DATE) = CAST(hd.ngayLap AS DATE)
           ),0) AS tongThanhToan
FROM dbo.HoaDon hd
GROUP BY CAST(hd.ngayLap AS DATE);
GO

IF OBJECT_ID('dbo.vw_BaoCaoDatPhong', 'V') IS NOT NULL
DROP VIEW dbo.vw_BaoCaoDatPhong;
GO
CREATE VIEW dbo.vw_BaoCaoDatPhong
AS
SELECT
    dp.maDatPhong,
    dp.ngayDat,
    dp.ngayNhanPhong,
    dp.ngayTraPhong,
    dp.trangThai,
    kh.hoTen AS tenKhachHang,
    nv.hoTen AS tenNhanVien,
    bg.tenBangGia,
    lp.tenLoaiPhong,
    dp.soLuongPhong,
    dp.soNguoi,
    dp.tienCoc,
    COUNT(ctdp.maChiTietDatPhong) AS soDongChiTiet,
    ISNULL(SUM(ISNULL(ctdp.thanhTien,0)),0) AS tongTienChiTiet
FROM dbo.DatPhong dp
         LEFT JOIN dbo.KhachHang kh ON kh.maKhachHang = dp.maKhachHang
         LEFT JOIN dbo.NhanVien nv ON nv.maNhanVien = dp.maNhanVien
         LEFT JOIN dbo.BangGia bg ON bg.maBangGia = dp.maBangGia
         LEFT JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = bg.maLoaiPhong
         LEFT JOIN dbo.ChiTietDatPhong ctdp ON ctdp.maDatPhong = dp.maDatPhong
GROUP BY dp.maDatPhong, dp.ngayDat, dp.ngayNhanPhong, dp.ngayTraPhong, dp.trangThai,
         kh.hoTen, nv.hoTen, bg.tenBangGia, lp.tenLoaiPhong, dp.soLuongPhong, dp.soNguoi, dp.tienCoc;
GO

IF OBJECT_ID('dbo.vw_BaoCaoPhongCongSuat', 'V') IS NOT NULL
DROP VIEW dbo.vw_BaoCaoPhongCongSuat;
GO
CREATE VIEW dbo.vw_BaoCaoPhongCongSuat
AS
SELECT
    p.maPhong,
    p.soPhong,
    lp.tenLoaiPhong,
    p.trangThai,
    p.tang,
    p.khuVuc,
    p.sucChuaChuan,
    p.sucChuaToiDa,
    COUNT(DISTINCT CASE WHEN dp.trangThai IN (N'Đã check-out', N'Đã thanh toán', N'Đang lưu trú') THEN dp.maDatPhong END) AS soLanSuDung,
    ISNULL(SUM(CASE WHEN dp.trangThai IN (N'Đã check-out', N'Đã thanh toán', N'Đang lưu trú') THEN DATEDIFF(DAY, dp.ngayNhanPhong, dp.ngayTraPhong) ELSE 0 END),0) AS tongDemDaDat
FROM dbo.Phong p
         LEFT JOIN dbo.LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong
         LEFT JOIN dbo.ChiTietDatPhong ctdp ON ctdp.maPhong = p.maPhong
         LEFT JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong
GROUP BY p.maPhong, p.soPhong, lp.tenLoaiPhong, p.trangThai, p.tang, p.khuVuc, p.sucChuaChuan, p.sucChuaToiDa;
GO

IF OBJECT_ID('dbo.vw_BaoCaoDichVu', 'V') IS NOT NULL
DROP VIEW dbo.vw_BaoCaoDichVu;
GO
CREATE VIEW dbo.vw_BaoCaoDichVu
AS
SELECT
    dv.maDichVu,
    dv.tenDichVu,
    dv.donVi,
    COUNT(sddv.maSuDung) AS soLanSuDung,
    ISNULL(SUM(ISNULL(sddv.soLuong,0)),0) AS tongSoLuong,
    ISNULL(SUM(ISNULL(sddv.thanhTien,0)),0) AS tongDoanhThu,
    MAX(lt.checkIn) AS lanGanNhat
FROM dbo.DichVu dv
         LEFT JOIN dbo.SuDungDichVu sddv ON sddv.maDichVu = dv.maDichVu
         LEFT JOIN dbo.LuuTru lt ON lt.maLuuTru = sddv.maLuuTru
GROUP BY dv.maDichVu, dv.tenDichVu, dv.donVi;
GO

IF OBJECT_ID('dbo.vw_BaoCaoKhachHang', 'V') IS NOT NULL
DROP VIEW dbo.vw_BaoCaoKhachHang;
GO
CREATE VIEW dbo.vw_BaoCaoKhachHang
AS
SELECT
    kh.maKhachHang,
    kh.hoTen,
    kh.soDienThoai,
    kh.quocTich,
    kh.loaiKhach,
    kh.hangKhach,
    COUNT(DISTINCT dp.maDatPhong) AS tongBooking,
    ISNULL(SUM(ISNULL(dp.tienCoc,0)),0) AS tongTienCoc,
    ISNULL(SUM(CASE WHEN hd.maHoaDon IS NOT NULL THEN ISNULL(hd.tienPhong,0) + ISNULL(hd.tienDichVu,0) + ISNULL(hd.phuThu,0) - ISNULL(hd.giamGia,0) ELSE 0 END),0) AS tongChiTieu,
    MAX(dp.ngayDat) AS lanDatGanNhat
FROM dbo.KhachHang kh
         LEFT JOIN dbo.DatPhong dp ON dp.maKhachHang = kh.maKhachHang
         LEFT JOIN dbo.HoaDon hd ON hd.maKhachHang = kh.maKhachHang AND hd.maDatPhong = dp.maDatPhong
GROUP BY kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.quocTich, kh.loaiKhach, kh.hangKhach;
GO

IF OBJECT_ID('dbo.sp_BaoCaoDoanhThu', 'P') IS NOT NULL
    DROP PROC dbo.sp_BaoCaoDoanhThu;
GO
CREATE PROC dbo.sp_BaoCaoDoanhThu
    @TuNgay DATE = NULL,
    @DenNgay DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;
SELECT *
FROM dbo.vw_BaoCaoDoanhThuNgay
WHERE (@TuNgay IS NULL OR ngay >= @TuNgay)
  AND (@DenNgay IS NULL OR ngay <= @DenNgay)
ORDER BY ngay;
END
GO

IF OBJECT_ID('dbo.sp_BaoCaoDatPhong', 'P') IS NOT NULL
    DROP PROC dbo.sp_BaoCaoDatPhong;
GO
CREATE PROC dbo.sp_BaoCaoDatPhong
    @TuNgay DATE = NULL,
    @DenNgay DATE = NULL,
    @TrangThai NVARCHAR(30) = NULL
AS
BEGIN
    SET NOCOUNT ON;
SELECT *
FROM dbo.vw_BaoCaoDatPhong
WHERE (@TuNgay IS NULL OR ngayNhanPhong >= @TuNgay)
  AND (@DenNgay IS NULL OR ngayTraPhong <= @DenNgay)
  AND (@TrangThai IS NULL OR trangThai = @TrangThai)
ORDER BY maDatPhong DESC;
END
GO

IF OBJECT_ID('dbo.sp_BaoCaoPhong', 'P') IS NOT NULL
    DROP PROC dbo.sp_BaoCaoPhong;
GO
CREATE PROC dbo.sp_BaoCaoPhong
AS
BEGIN
    SET NOCOUNT ON;
SELECT *
FROM dbo.vw_BaoCaoPhongCongSuat
ORDER BY soPhong;
END
GO

IF OBJECT_ID('dbo.sp_BaoCaoDichVu', 'P') IS NOT NULL
    DROP PROC dbo.sp_BaoCaoDichVu;
GO
CREATE PROC dbo.sp_BaoCaoDichVu
AS
BEGIN
    SET NOCOUNT ON;
SELECT *
FROM dbo.vw_BaoCaoDichVu
ORDER BY tongDoanhThu DESC, tongSoLuong DESC;
END
GO

IF OBJECT_ID('dbo.sp_BaoCaoKhachHang', 'P') IS NOT NULL
    DROP PROC dbo.sp_BaoCaoKhachHang;
GO
CREATE PROC dbo.sp_BaoCaoKhachHang
AS
BEGIN
    SET NOCOUNT ON;
SELECT *
FROM dbo.vw_BaoCaoKhachHang
ORDER BY tongChiTieu DESC, tongBooking DESC;
END
GO
