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

SET NOCOUNT ON;
GO

/* =========================================================
   1. SCHEMA
   ========================================================= */

CREATE TABLE dbo.NhanVien (
                              maNhanVien INT IDENTITY(1,1) PRIMARY KEY,
                              hoTen NVARCHAR(100) NOT NULL,
                              ngaySinh DATE NULL,
                              gioiTinh NVARCHAR(10) NULL,
                              cccd VARCHAR(20) UNIQUE NULL,
                              soDienThoai VARCHAR(15) NULL,
                              email VARCHAR(100) NULL,
                              diaChi NVARCHAR(255) NULL,
                              boPhan NVARCHAR(50) NULL,
                              chucVu NVARCHAR(50) NULL,
                              caLam NVARCHAR(20) NULL,
                              ngayVaoLam DATE NULL,
                              trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
                              ghiChu NVARCHAR(MAX) NULL
);
GO

CREATE TABLE dbo.TaiKhoan (
                              maTaiKhoan INT IDENTITY(1,1) PRIMARY KEY,
                              maNhanVien INT NOT NULL,
                              tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
                              matKhau VARCHAR(255) NOT NULL,
                              vaiTro NVARCHAR(30) NOT NULL,
                              trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
                              lanDangNhapCuoi DATETIME NULL,
                              emailKhoiPhuc VARCHAR(100) NULL,
                              FOREIGN KEY (maNhanVien) REFERENCES dbo.NhanVien(maNhanVien)
);
GO

CREATE TABLE dbo.TaiKhoanQuyen (
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
                                   FOREIGN KEY (maTaiKhoan) REFERENCES dbo.TaiKhoan(maTaiKhoan)
);
GO

CREATE TABLE dbo.KhachHang (
                               maKhachHang INT IDENTITY(1,1) PRIMARY KEY,
                               hoTen NVARCHAR(100) NOT NULL,
                               gioiTinh NVARCHAR(10) NULL,
                               ngaySinh DATE NULL,
                               soDienThoai VARCHAR(15) NULL,
                               email VARCHAR(100) NULL,
                               cccdPassport VARCHAR(30) UNIQUE NULL,
                               diaChi NVARCHAR(255) NULL,
                               quocTich NVARCHAR(50) NULL,
                               loaiKhach NVARCHAR(30) NULL,
                               hangKhach NVARCHAR(30) NULL,
                               trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
                               nguoiTao VARCHAR(50) NULL,
                               ghiChu NVARCHAR(MAX) NULL
);
GO

CREATE TABLE dbo.TienNghi (
                              maTienNghi INT IDENTITY(1,1) PRIMARY KEY,
                              tenTienNghi NVARCHAR(100) NOT NULL UNIQUE,
                              nhomTienNghi NVARCHAR(50) NULL,
                              trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng',
                              uuTien INT NULL,
                              moTa NVARCHAR(MAX) NULL
);
GO

CREATE TABLE dbo.LoaiPhong (
                               maLoaiPhong INT IDENTITY(1,1) PRIMARY KEY,
                               tenLoaiPhong NVARCHAR(100) NOT NULL UNIQUE,
                               sucChua INT NOT NULL,
                               khachToiDa INT NOT NULL,
                               dienTich DECIMAL(6,1) NULL,
                               loaiGiuong NVARCHAR(50) NULL,
                               giaThamChieu DECIMAL(15,0) NOT NULL,
                               trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng',
                               moTa NVARCHAR(MAX) NULL
);
GO

CREATE TABLE dbo.LoaiPhongTienNghi (
                                       maLoaiPhong INT NOT NULL,
                                       maTienNghi INT NOT NULL,
                                       PRIMARY KEY (maLoaiPhong, maTienNghi),
                                       FOREIGN KEY (maLoaiPhong) REFERENCES dbo.LoaiPhong(maLoaiPhong),
                                       FOREIGN KEY (maTienNghi) REFERENCES dbo.TienNghi(maTienNghi)
);
GO

CREATE TABLE dbo.Phong (
                           maPhong INT IDENTITY(1,1) PRIMARY KEY,
                           maLoaiPhong INT NOT NULL,
                           soPhong VARCHAR(10) NOT NULL UNIQUE,
                           tang NVARCHAR(10) NOT NULL,
                           khuVuc NVARCHAR(20) NULL,
                           sucChuaChuan INT NOT NULL,
                           sucChuaToiDa INT NOT NULL,
                           trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
                           FOREIGN KEY (maLoaiPhong) REFERENCES dbo.LoaiPhong(maLoaiPhong)
);
GO

CREATE TABLE dbo.BangGia (
                             maBangGia INT IDENTITY(1,1) PRIMARY KEY,
                             tenBangGia NVARCHAR(100) NOT NULL UNIQUE,
                             maLoaiPhong INT NOT NULL,
                             ngayBatDau DATE NOT NULL,
                             ngayKetThuc DATE NOT NULL,
                             loaiNgay NVARCHAR(30) NOT NULL,
                             trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng',
                             FOREIGN KEY (maLoaiPhong) REFERENCES dbo.LoaiPhong(maLoaiPhong)
);
GO

CREATE TABLE dbo.ChiTietBangGia (
                                    maChiTietBangGia INT IDENTITY(1,1) PRIMARY KEY,
                                    maBangGia INT NOT NULL,
                                    loaiNgay NVARCHAR(30) NOT NULL,
                                    khungGio NVARCHAR(50) NOT NULL,
                                    giaTheoGio DECIMAL(15,0) NOT NULL,
                                    giaQuaDem DECIMAL(15,0) NOT NULL,
                                    giaTheoNgay DECIMAL(15,0) NOT NULL,
                                    giaCuoiTuan DECIMAL(15,0) NOT NULL,
                                    giaLe DECIMAL(15,0) NOT NULL,
                                    phuThu DECIMAL(15,0) NOT NULL DEFAULT 0,
                                    FOREIGN KEY (maBangGia) REFERENCES dbo.BangGia(maBangGia)
);
GO

CREATE TABLE dbo.NgayLe (
                            maNgayLe INT IDENTITY(1,1) PRIMARY KEY,
                            tenNgayLe NVARCHAR(200) NOT NULL,
                            ngay DATE NOT NULL UNIQUE,
                            moTa NVARCHAR(500) NULL,
                            trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng'
);
GO

CREATE TABLE dbo.DatPhong (
                              maDatPhong INT IDENTITY(1,1) PRIMARY KEY,
                              maKhachHang INT NOT NULL,
                              maNhanVien INT NOT NULL,
                              maBangGia INT NOT NULL,
                              ngayDat DATE NOT NULL,
                              ngayNhanPhong DATE NOT NULL,
                              ngayTraPhong DATE NOT NULL,
                              soLuongPhong INT NOT NULL,
                              soNguoi INT NOT NULL,
                              tienCoc DECIMAL(15,0) NOT NULL DEFAULT 0,
                              trangThai NVARCHAR(30) NOT NULL,
                              ghiChu NVARCHAR(MAX) NULL,
                              FOREIGN KEY (maKhachHang) REFERENCES dbo.KhachHang(maKhachHang),
                              FOREIGN KEY (maNhanVien) REFERENCES dbo.NhanVien(maNhanVien),
                              FOREIGN KEY (maBangGia) REFERENCES dbo.BangGia(maBangGia)
);
GO

CREATE TABLE dbo.ChiTietDatPhong (
                                     maChiTietDatPhong INT IDENTITY(1,1) PRIMARY KEY,
                                     maDatPhong INT NOT NULL,
                                     maPhong INT NOT NULL,
                                     soNguoi INT NOT NULL,
                                     giaPhong DECIMAL(15,0) NOT NULL,
                                     thanhTien DECIMAL(15,0) NOT NULL,
                                     FOREIGN KEY (maDatPhong) REFERENCES dbo.DatPhong(maDatPhong),
                                     FOREIGN KEY (maPhong) REFERENCES dbo.Phong(maPhong)
);
GO

CREATE TABLE dbo.LuuTru (
                            maLuuTru INT IDENTITY(1,1) PRIMARY KEY,
                            maChiTietDatPhong INT NOT NULL,
                            maDatPhong INT NOT NULL,
                            maPhong INT NOT NULL,
                            checkIn DATETIME NOT NULL,
                            checkOut DATETIME NULL,
                            soNguoi INT NOT NULL,
                            giaPhong DECIMAL(15,0) NOT NULL,
                            tienCoc DECIMAL(15,0) NOT NULL DEFAULT 0,
                            FOREIGN KEY (maChiTietDatPhong) REFERENCES dbo.ChiTietDatPhong(maChiTietDatPhong),
                            FOREIGN KEY (maDatPhong) REFERENCES dbo.DatPhong(maDatPhong),
                            FOREIGN KEY (maPhong) REFERENCES dbo.Phong(maPhong)
);
GO

CREATE TABLE dbo.DichVu (
                            maDichVu INT IDENTITY(1,1) PRIMARY KEY,
                            tenDichVu NVARCHAR(100) NOT NULL UNIQUE,
                            donGia DECIMAL(15,0) NOT NULL,
                            donVi NVARCHAR(20) NOT NULL
);
GO

CREATE TABLE dbo.SuDungDichVu (
                                  maSuDung INT IDENTITY(1,1) PRIMARY KEY,
                                  maLuuTru INT NOT NULL,
                                  maDichVu INT NOT NULL,
                                  soLuong INT NOT NULL,
                                  donGia DECIMAL(15,0) NOT NULL,
                                  thanhTien AS (soLuong * donGia) PERSISTED,
                                  FOREIGN KEY (maLuuTru) REFERENCES dbo.LuuTru(maLuuTru),
                                  FOREIGN KEY (maDichVu) REFERENCES dbo.DichVu(maDichVu)
);
GO

CREATE TABLE dbo.HoaDon (
                            maHoaDon INT IDENTITY(1,1) PRIMARY KEY,
                            maLuuTru INT NOT NULL,
                            maDatPhong INT NOT NULL,
                            maKhachHang INT NOT NULL,
                            ngayLap DATETIME NOT NULL DEFAULT GETDATE(),
                            tienPhong DECIMAL(15,0) NOT NULL DEFAULT 0,
                            tienDichVu DECIMAL(15,0) NOT NULL DEFAULT 0,
                            phuThu DECIMAL(15,0) NOT NULL DEFAULT 0,
                            giamGia DECIMAL(15,0) NOT NULL DEFAULT 0,
                            tienCocTru DECIMAL(15,0) NOT NULL DEFAULT 0,
                            trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ thanh toán',
                            ghiChu NVARCHAR(MAX) NULL,
                            ngayThanhToan DATETIME NULL,
                            tongTien AS (ISNULL(tienPhong,0) + ISNULL(tienDichVu,0) + ISNULL(phuThu,0) - ISNULL(giamGia,0)) PERSISTED,
                            FOREIGN KEY (maLuuTru) REFERENCES dbo.LuuTru(maLuuTru),
                            FOREIGN KEY (maDatPhong) REFERENCES dbo.DatPhong(maDatPhong),
                            FOREIGN KEY (maKhachHang) REFERENCES dbo.KhachHang(maKhachHang)
);
GO

CREATE TABLE dbo.ChiTietHoaDon (
                                   maChiTietHoaDon INT IDENTITY(1,1) PRIMARY KEY,
                                   maHoaDon INT NOT NULL,
                                   loaiChiPhi NVARCHAR(500) NOT NULL,
                                   soLuong INT NOT NULL,
                                   donGia DECIMAL(15,0) NOT NULL,
                                   thanhTien AS (soLuong * donGia) PERSISTED,
                                   FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon)
);
GO

CREATE TABLE dbo.ThanhToan (
                               maThanhToan INT IDENTITY(1,1) PRIMARY KEY,
                               maHoaDon INT NOT NULL,
                               maNhanVien INT NOT NULL,
                               ngayThanhToan DATETIME NOT NULL DEFAULT GETDATE(),
                               soTien DECIMAL(15,0) NOT NULL,
                               phuongThuc NVARCHAR(30) NOT NULL DEFAULT N'Tiền mặt',
                               soThamChieu NVARCHAR(100) NULL,
                               ghiChu NVARCHAR(MAX) NULL,
                               loaiGiaoDich NVARCHAR(30) NOT NULL DEFAULT N'THANH_TOAN',
                               trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoàn tất',
                               FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon),
                               FOREIGN KEY (maNhanVien) REFERENCES dbo.NhanVien(maNhanVien)
);
GO

/* =========================================================
   2. NHAN VIEN / TAI KHOAN / QUYEN
   ========================================================= */
INSERT INTO dbo.NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES
(N'Nguyễn Văn A', '2000-01-01', N'Nam', '012345678901', '0909000001', 'nva@gmail.com', N'Hà Nội', N'Lễ tân', N'Lễ tân', N'Ca sáng', '2024-01-01', N'Hoạt động', N'Nhân viên lễ tân'),
(N'Trần Thị B', '1998-05-10', N'Nữ', '012345678902', '0909000002', 'ttb@gmail.com', N'Đà Nẵng', N'Điều hành', N'Quản lí', N'Ca hành chính', '2023-01-01', N'Hoạt động', N'Quản lý khách sạn'),
(N'Lê Văn C', '1999-08-20', N'Nam', '012345678903', '0909000003', 'lvc@gmail.com', N'TP.HCM', N'Lễ tân', N'Lễ tân', N'Ca chiều', '2024-02-15', N'Hoạt động', N'Nhân viên lễ tân'),
(N'Phạm Quang Dũng', '1996-11-15', N'Nam', '012345678904', '0909000004', 'pqd@gmail.com', N'Hải Phòng', N'Kế toán', N'Kế toán', N'Ca hành chính', '2023-06-01', N'Hoạt động', N'Phụ trách thanh toán và hóa đơn'),
(N'Hoàng Mỹ Linh', '1997-07-09', N'Nữ', '012345678905', '0909000005', 'hml@gmail.com', N'Cần Thơ', N'Buồng phòng', N'Giám sát buồng', N'Ca sáng', '2024-03-01', N'Hoạt động', N'Phụ trách trạng thái phòng');

DECLARE @nvA INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678901');
DECLARE @nvB INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678902');
DECLARE @nvC INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678903');
DECLARE @nvD INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678904');

INSERT INTO dbo.TaiKhoan (maNhanVien, tenDangNhap, matKhau, vaiTro, trangThai, lanDangNhapCuoi, emailKhoiPhuc)
VALUES
    (@nvA, 'letan1', '123', N'Lễ tân', N'Hoạt động', GETDATE(), 'letan1@gmail.com'),
    (@nvB, 'quanli1', '123', N'Quản lí', N'Hoạt động', GETDATE(), 'quanli1@gmail.com'),
    (@nvC, 'nhanvien1', '123', N'Lễ tân', N'Hoạt động', GETDATE(), 'nhanvien1@gmail.com'),
    (@nvD, 'ketoan1', '123', N'Kế toán', N'Hoạt động', GETDATE(), 'ketoan1@gmail.com');

INSERT INTO dbo.TaiKhoanQuyen (maTaiKhoan, permDashboard, permDatPhong, permCheckInOut, permThanhToan, permKhachHang, permPhong, permLoaiPhong, permBangGia, permDichVu, permTienNghi, permTaiKhoan, permNhanVien, permBaoCao)
SELECT maTaiKhoan, 1,1,1,1,1,1,1,1,1,1,1,1,1
FROM dbo.TaiKhoan
WHERE tenDangNhap = 'quanli1';

INSERT INTO dbo.TaiKhoanQuyen (maTaiKhoan, permDashboard, permDatPhong, permCheckInOut, permThanhToan, permKhachHang, permPhong, permLoaiPhong, permBangGia, permDichVu, permTienNghi, permTaiKhoan, permNhanVien, permBaoCao)
SELECT maTaiKhoan, 1,1,1,1,1,1,1,1,1,1,0,0,1
FROM dbo.TaiKhoan
WHERE tenDangNhap = 'letan1';

INSERT INTO dbo.TaiKhoanQuyen (maTaiKhoan, permDashboard, permDatPhong, permCheckInOut, permThanhToan, permKhachHang, permPhong, permLoaiPhong, permBangGia, permDichVu, permTienNghi, permTaiKhoan, permNhanVien, permBaoCao)
SELECT maTaiKhoan, 1,1,1,0,1,0,0,0,0,0,0,0,0
FROM dbo.TaiKhoan
WHERE tenDangNhap = 'nhanvien1';

INSERT INTO dbo.TaiKhoanQuyen (maTaiKhoan, permDashboard, permDatPhong, permCheckInOut, permThanhToan, permKhachHang, permPhong, permLoaiPhong, permBangGia, permDichVu, permTienNghi, permTaiKhoan, permNhanVien, permBaoCao)
SELECT maTaiKhoan, 1,0,0,1,0,0,0,0,0,0,0,0,1
FROM dbo.TaiKhoan
WHERE tenDangNhap = 'ketoan1';
GO

/* =========================================================
   3. KHACH HANG
   ========================================================= */
INSERT INTO dbo.KhachHang (hoTen, gioiTinh, ngaySinh, soDienThoai, email, cccdPassport, diaChi, quocTich, loaiKhach, hangKhach, trangThai, nguoiTao, ghiChu)
VALUES
(N'Phạm Minh Hùng', N'Nam', '1995-04-12', '0911111111', 'hung@gmail.com', '079123456789', N'Hà Nội', N'Việt Nam', N'Cá nhân', N'Thường', N'Hoạt động', 'quanli1', N'Khách quen'),
(N'Nguyễn Thị Lan', N'Nữ', '1997-09-20', '0922222222', 'lan@gmail.com', '079123456780', N'Đà Nẵng', N'Việt Nam', N'Cá nhân', N'VIP', N'Hoạt động', 'letan1', N'Ưu tiên check-in sớm'),
(N'John Smith', N'Nam', '1990-06-10', '0933333333', 'john@gmail.com', 'P12345678', N'New York', N'Mỹ', N'Nước ngoài', N'VIP', N'Hoạt động', 'letan1', N'Khách quốc tế'),
(N'Lê Hoài An', N'Nữ', '2001-08-14', '0944444444', 'anhoai@gmail.com', '079123456781', N'Huế', N'Việt Nam', N'Cá nhân', N'Thân thiết', N'Hoạt động', 'quanli1', N'Hay dùng dịch vụ giặt ủi'),
(N'Trương Quốc Bảo', N'Nam', '1989-03-05', '0955555555', 'bao@gmail.com', '079123456782', N'Quảng Nam', N'Việt Nam', N'Doanh nghiệp', N'VIP', N'Hoạt động', 'quanli1', N'Công tác thường xuyên'),
(N'Sarah Lee', N'Nữ', '1992-02-17', '0966666666', 'sarah@gmail.com', 'P99887766', N'Seoul', N'Hàn Quốc', N'Nước ngoài', N'VIP', N'Hoạt động', 'letan1', N'Khách ưu tiên phòng yên tĩnh');
GO

/* =========================================================
   4. TIEN NGHI / LOAI PHONG / LOAI PHONG TIEN NGHI / PHONG
   ========================================================= */
INSERT INTO dbo.TienNghi (tenTienNghi, nhomTienNghi, trangThai, uuTien, moTa)
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
(N'Bình chữa cháy', N'An toàn', N'Đang áp dụng', 3, N'Trang bị bình chữa cháy'),
(N'Bàn làm việc', N'Tiện nghi bổ sung', N'Đang áp dụng', 5, N'Bàn làm việc trong phòng'),
(N'Sofa', N'Tiện nghi bổ sung', N'Đang áp dụng', 6, N'Sofa thư giãn'),
(N'Ban công', N'Tiện nghi mở rộng', N'Đang áp dụng', 1, N'Ban công hướng phố hoặc sân vườn'),
(N'Máy pha cà phê', N'Tiện nghi mở rộng', N'Đang áp dụng', 2, N'Máy pha cà phê cá nhân'),
(N'Bếp mini', N'Tiện nghi mở rộng', N'Đang áp dụng', 3, N'Bếp mini cho lưu trú dài ngày');

INSERT INTO dbo.LoaiPhong (tenLoaiPhong, sucChua, khachToiDa, dienTich, loaiGiuong, giaThamChieu, trangThai, moTa)
VALUES
    (N'Phòng đơn', 1, 2, 18.0, N'1 giường đơn', 350000, N'Đang áp dụng', N'Phòng nhỏ gọn cho 1-2 khách'),
    (N'Phòng đôi', 2, 4, 24.0, N'1 giường đôi lớn', 650000, N'Đang áp dụng', N'Phòng phù hợp cặp đôi hoặc công tác'),
    (N'Deluxe', 2, 4, 28.0, N'1 giường đôi / 2 giường đơn', 850000, N'Đang áp dụng', N'Phòng cao cấp, tiện nghi tốt'),
    (N'Family', 4, 6, 38.0, N'2 giường đôi', 1200000, N'Đang áp dụng', N'Phòng gia đình cho nhóm 4-6 khách'),
    (N'VIP', 3, 5, 45.0, N'1 giường king', 1800000, N'Đang áp dụng', N'Phòng VIP, không gian rộng và cao cấp');

DECLARE @lpDon INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn');
DECLARE @lpDoi INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi');
DECLARE @lpDeluxe INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe');
DECLARE @lpFamily INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family');
DECLARE @lpVIP INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'VIP');

DECLARE @tnWifi INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Wifi');
DECLARE @tnTV INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'TV');
DECLARE @tnDieuHoa INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Điều hòa');
DECLARE @tnNuocNong INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Nước nóng');
DECLARE @tnVoiSen INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Vòi sen');
DECLARE @tnSmartTV INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Smart TV');
DECLARE @tnNetflix INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Netflix');
DECLARE @tnMinibar INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Minibar');
DECLARE @tnBanLamViec INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bàn làm việc');
DECLARE @tnKhanTam INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Khăn tắm');
DECLARE @tnTuLanh INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Tủ lạnh');
DECLARE @tnBepMini INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bếp mini');
DECLARE @tnSofa INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Sofa');
DECLARE @tnBonTam INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Bồn tắm');
DECLARE @tnMayPhaCafe INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Máy pha cà phê');
DECLARE @tnBanCong INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Ban công');
DECLARE @tnKetSat INT = (SELECT maTienNghi FROM dbo.TienNghi WHERE tenTienNghi = N'Két sắt');

INSERT INTO dbo.LoaiPhongTienNghi (maLoaiPhong, maTienNghi)
VALUES
    (@lpDon, @tnWifi), (@lpDon, @tnDieuHoa), (@lpDon, @tnNuocNong), (@lpDon, @tnVoiSen), (@lpDon, @tnBanLamViec),
    (@lpDoi, @tnWifi), (@lpDoi, @tnTV), (@lpDoi, @tnDieuHoa), (@lpDoi, @tnNuocNong), (@lpDoi, @tnBanCong),
    (@lpDeluxe, @tnWifi), (@lpDeluxe, @tnSmartTV), (@lpDeluxe, @tnNetflix), (@lpDeluxe, @tnMinibar), (@lpDeluxe, @tnBanLamViec), (@lpDeluxe, @tnKhanTam),
    (@lpFamily, @tnWifi), (@lpFamily, @tnTV), (@lpFamily, @tnTuLanh), (@lpFamily, @tnBepMini), (@lpFamily, @tnKhanTam), (@lpFamily, @tnSofa),
    (@lpVIP, @tnWifi), (@lpVIP, @tnSmartTV), (@lpVIP, @tnNetflix), (@lpVIP, @tnBonTam), (@lpVIP, @tnMayPhaCafe), (@lpVIP, @tnBanCong), (@lpVIP, @tnKetSat);

INSERT INTO dbo.Phong (maLoaiPhong, soPhong, tang, khuVuc, sucChuaChuan, sucChuaToiDa, trangThai)
VALUES
    (@lpDon,    '101', N'Tầng 1', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDon,    '102', N'Tầng 1', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDoi,    '103', N'Tầng 1', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDoi,    '104', N'Tầng 1', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDeluxe, '105', N'Tầng 1', N'Khu C',   2, 4, N'Hoạt động'),
    (@lpFamily, '106', N'Tầng 1', N'Khu C',   4, 6, N'Hoạt động'),

    (@lpDon,    '201', N'Tầng 2', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDon,    '202', N'Tầng 2', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDoi,    '203', N'Tầng 2', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDoi,    '204', N'Tầng 2', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDeluxe, '205', N'Tầng 2', N'Khu C',   2, 4, N'Hoạt động'),
    (@lpFamily, '206', N'Tầng 2', N'Khu C',   4, 6, N'Hoạt động'),

    (@lpDon,    '301', N'Tầng 3', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDoi,    '302', N'Tầng 3', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDeluxe, '303', N'Tầng 3', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDeluxe, '304', N'Tầng 3', N'Khu C',   2, 4, N'Hoạt động'),
    (@lpFamily, '305', N'Tầng 3', N'Khu Family', 4, 6, N'Hoạt động'),
    (@lpVIP,    '306', N'Tầng 3', N'Khu VIP', 3, 5, N'Hoạt động'),

    (@lpDon,    '401', N'Tầng 4', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDoi,    '402', N'Tầng 4', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDeluxe, '403', N'Tầng 4', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpFamily, '404', N'Tầng 4', N'Khu Family', 4, 6, N'Hoạt động'),
    (@lpVIP,    '405', N'Tầng 4', N'Khu VIP', 3, 5, N'Hoạt động'),
    (@lpVIP,    '406', N'Tầng 4', N'Khu VIP', 3, 5, N'Hoạt động'),

    (@lpDon,    '501', N'Tầng 5', N'Khu A',   1, 2, N'Hoạt động'),
    (@lpDoi,    '502', N'Tầng 5', N'Khu B',   2, 4, N'Hoạt động'),
    (@lpDeluxe, '503', N'Tầng 5', N'Khu C',   2, 4, N'Bảo trì'),
    (@lpFamily, '504', N'Tầng 5', N'Khu Family', 4, 6, N'Hoạt động'),
    (@lpVIP,    '505', N'Tầng 5', N'Khu VIP', 3, 5, N'Hoạt động'),
    (@lpVIP,    '506', N'Tầng 5', N'Khu VIP', 3, 5, N'Hoạt động');
GO

/* =========================================================
   5. BANG GIA / CHI TIET BANG GIA
   ========================================================= */
DECLARE @lpDon2 INT    = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đơn');
DECLARE @lpDoi2 INT    = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Phòng đôi');
DECLARE @lpDeluxe2 INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Deluxe');
DECLARE @lpFamily2 INT = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'Family');
DECLARE @lpVIP2 INT    = (SELECT maLoaiPhong FROM dbo.LoaiPhong WHERE tenLoaiPhong = N'VIP');

INSERT INTO dbo.BangGia (tenBangGia, maLoaiPhong, ngayBatDau, ngayKetThuc, loaiNgay, trangThai)
VALUES
    (N'Bảng giá Phòng đơn 2026', @lpDon2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
    (N'Bảng giá Phòng đôi 2026', @lpDoi2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
    (N'Bảng giá Deluxe 2026',    @lpDeluxe2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
    (N'Bảng giá Family 2026',    @lpFamily2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng'),
    (N'Bảng giá VIP 2026',       @lpVIP2, '2026-01-01', '2026-12-31', N'Thường', N'Đang áp dụng');

DECLARE @bgDon2 INT    = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Phòng đơn 2026');
DECLARE @bgDoi2 INT    = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Phòng đôi 2026');
DECLARE @bgDeluxe2 INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Deluxe 2026');
DECLARE @bgFamily2 INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Family 2026');
DECLARE @bgVIP2 INT    = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá VIP 2026');

INSERT INTO dbo.ChiTietBangGia (maBangGia, loaiNgay, khungGio, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
VALUES
    (@bgDon2,    N'Thường', N'00:00-23:59',  90000, 250000, 350000,  20000,  50000, 0),
    (@bgDoi2,    N'Thường', N'00:00-23:59', 150000, 450000, 650000,  40000,  80000, 0),
    (@bgDeluxe2, N'Thường', N'00:00-23:59', 180000, 550000, 850000,  60000, 100000, 0),
    (@bgFamily2, N'Thường', N'00:00-23:59', 260000, 800000,1200000, 100000, 150000, 0),
    (@bgVIP2,    N'Thường', N'00:00-23:59', 350000,1200000,1800000, 150000, 250000, 0);
GO

/* =========================================================
   6. NGAY LE
   ========================================================= */
INSERT INTO dbo.NgayLe (tenNgayLe, ngay, moTa, trangThai)
VALUES
(N'Tết Dương lịch', '2026-01-01', N'Nghỉ lễ đầu năm', N'Đang áp dụng'),
(N'Ngày Giải phóng miền Nam', '2026-04-30', N'Ngày lễ quốc gia', N'Đang áp dụng'),
(N'Quốc tế Lao động', '2026-05-01', N'Ngày lễ quốc gia', N'Đang áp dụng'),
(N'Quốc khánh', '2026-09-02', N'Ngày lễ quốc gia', N'Đang áp dụng');
GO

/* =========================================================
   7. DICH VU
   ========================================================= */
INSERT INTO dbo.DichVu (tenDichVu, donGia, donVi)
VALUES
(N'Nước suối', 10000, N'Chai'),
(N'Bữa sáng', 80000, N'Suất'),
(N'Giặt ủi', 50000, N'Kg'),
(N'Thuê xe máy', 150000, N'Ngày'),
(N'Đưa đón sân bay', 250000, N'Lượt'),
(N'Cà phê', 30000, N'Ly'),
(N'Bữa tối', 180000, N'Suất'),
(N'In ấn tài liệu', 5000, N'Trang'),
(N'Thuê phòng họp mini', 400000, N'Giờ'),
(N'Phụ thu thêm khách', 120000, N'Người'),
(N'Massage thư giãn', 250000, N'Lượt'),
(N'Combo ăn trưa', 120000, N'Suất');
GO

/* =========================================================
   8. DAT PHONG / CHI TIET DAT PHONG
   ========================================================= */
DECLARE @nvA2 INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678901');
DECLARE @nvB2 INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678902');

DECLARE @khHung2 INT  = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456789');
DECLARE @khLan2 INT   = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456780');
DECLARE @khJohn2 INT  = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = 'P12345678');
DECLARE @khAn2 INT    = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456781');
DECLARE @khBao2 INT   = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456782');
DECLARE @khSarah2 INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = 'P99887766');

DECLARE @bgDon3 INT    = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Phòng đơn 2026');
DECLARE @bgDoi3 INT    = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Phòng đôi 2026');
DECLARE @bgDeluxe3 INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Deluxe 2026');
DECLARE @bgFamily3 INT = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá Family 2026');
DECLARE @bgVIP3 INT    = (SELECT maBangGia FROM dbo.BangGia WHERE tenBangGia = N'Bảng giá VIP 2026');

DECLARE @ph102_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '102');
DECLARE @ph204_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '204');
DECLARE @ph205_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '205');
DECLARE @ph305_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '305');
DECLARE @ph401_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '401');
DECLARE @ph505_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '505');
DECLARE @ph103_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '103');
DECLARE @ph104_2 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '104');

INSERT INTO dbo.DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai, ghiChu)
VALUES
    (@khJohn2,  @nvA2, @bgDon3,    '2026-04-14', '2026-04-14', '2026-04-15', 1, 2, 100000, N'Chờ check-in', N'Khách đến buổi tối'),
    (@khLan2,   @nvA2, @bgDoi3,    '2026-04-08', '2026-04-08', '2026-04-10', 1, 2, 200000, N'Đã thanh toán', N'Đã hoàn tất lưu trú'),
    (@khHung2,  @nvB2, @bgDeluxe3, '2026-04-13', '2026-04-13', '2026-04-15', 1, 2, 300000, N'Đang lưu trú', N'Khách đang ở phòng Deluxe'),
    (@khSarah2, @nvA2, @bgFamily3, '2026-04-14', '2026-04-16', '2026-04-18', 1, 4, 500000, N'Đã xác nhận', N'Gia đình 4 người'),
    (@khBao2,   @nvB2, @bgDon3,    '2026-04-12', '2026-04-20', '2026-04-22', 1, 1, 150000, N'Đã cọc', N'Khách công tác'),
    (@khAn2,    @nvA2, @bgVIP3,    '2026-04-12', '2026-04-12', '2026-04-14', 1, 2, 400000, N'Đang lưu trú', N'Khách VIP đang ở'),
    (@khJohn2,  @nvA2, @bgDoi3,    '2026-04-29', '2026-04-30', '2026-05-01', 2, 4, 300000, N'Đã xác nhận', N'Booking nhiều phòng cho đoàn nhỏ');

DECLARE @dp1_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khJohn2  AND ngayNhanPhong = '2026-04-14');
DECLARE @dp2_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khLan2   AND ngayNhanPhong = '2026-04-08');
DECLARE @dp3_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khHung2  AND ngayNhanPhong = '2026-04-13');
DECLARE @dp4_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khSarah2 AND ngayNhanPhong = '2026-04-16');
DECLARE @dp5_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khBao2   AND ngayNhanPhong = '2026-04-20');
DECLARE @dp6_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khAn2    AND ngayNhanPhong = '2026-04-12');
DECLARE @dp7_2 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khJohn2  AND ngayNhanPhong = '2026-04-30');

INSERT INTO dbo.ChiTietDatPhong (maDatPhong, maPhong, soNguoi, giaPhong, thanhTien)
VALUES
    (@dp1_2, @ph102_2, 2, 350000, 350000),
    (@dp2_2, @ph204_2, 2, 650000, 1300000),
    (@dp3_2, @ph205_2, 2, 850000, 1700000),
    (@dp4_2, @ph305_2, 4, 1200000, 2400000),
    (@dp5_2, @ph401_2, 1, 350000, 700000),
    (@dp6_2, @ph505_2, 2, 1800000, 3600000),
    (@dp7_2, @ph103_2, 2, 650000, 1300000),
    (@dp7_2, @ph104_2, 2, 650000, 1300000);
GO

/* =========================================================
   9. LUU TRU
   ========================================================= */
DECLARE @khLan3 INT  = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456780');
DECLARE @khHung3 INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456789');
DECLARE @khAn3 INT   = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456781');

DECLARE @dp2_3 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khLan3  AND ngayNhanPhong = '2026-04-08');
DECLARE @dp3_3 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khHung3 AND ngayNhanPhong = '2026-04-13');
DECLARE @dp6_3 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khAn3   AND ngayNhanPhong = '2026-04-12');

DECLARE @ph204_3 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '204');
DECLARE @ph205_3 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '205');
DECLARE @ph505_3 INT = (SELECT maPhong FROM dbo.Phong WHERE soPhong = '505');

DECLARE @ctdp2_3 INT = (SELECT maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = @dp2_3 AND maPhong = @ph204_3);
DECLARE @ctdp3_3 INT = (SELECT maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = @dp3_3 AND maPhong = @ph205_3);
DECLARE @ctdp6_3 INT = (SELECT maChiTietDatPhong FROM dbo.ChiTietDatPhong WHERE maDatPhong = @dp6_3 AND maPhong = @ph505_3);

INSERT INTO dbo.LuuTru (maChiTietDatPhong, maDatPhong, maPhong, checkIn, checkOut, soNguoi, giaPhong, tienCoc)
VALUES
    (@ctdp2_3, @dp2_3, @ph204_3, '2026-04-08T14:00:00', '2026-04-10T11:30:00', 2, 650000, 200000),
    (@ctdp3_3, @dp3_3, @ph205_3, '2026-04-13T13:45:00', NULL,                  2, 850000, 300000),
    (@ctdp6_3, @dp6_3, @ph505_3, '2026-04-12T12:20:00', NULL,                  2, 1800000, 400000);
GO

/* =========================================================
   10. SU DUNG DICH VU
   ========================================================= */
DECLARE @khLan4 INT  = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456780');
DECLARE @khHung4 INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456789');
DECLARE @khAn4 INT   = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456781');

DECLARE @dp2_4 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khLan4  AND ngayNhanPhong = '2026-04-08');
DECLARE @dp3_4 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khHung4 AND ngayNhanPhong = '2026-04-13');
DECLARE @dp6_4 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khAn4   AND ngayNhanPhong = '2026-04-12');

DECLARE @lt2_4 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maDatPhong = @dp2_4);
DECLARE @lt3_4 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maDatPhong = @dp3_4);
DECLARE @lt6_4 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maDatPhong = @dp6_4);

DECLARE @dvNuoc4 INT    = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Nước suối');
DECLARE @dvSang4 INT    = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Bữa sáng');
DECLARE @dvGiat4 INT    = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Giặt ủi');
DECLARE @dvCafe4 INT    = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Cà phê');
DECLARE @dvAirport4 INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Đưa đón sân bay');
DECLARE @dvMassage4 INT = (SELECT maDichVu FROM dbo.DichVu WHERE tenDichVu = N'Massage thư giãn');

INSERT INTO dbo.SuDungDichVu (maLuuTru, maDichVu, soLuong, donGia)
VALUES
    (@lt2_4, @dvNuoc4,    2, 10000),
    (@lt2_4, @dvSang4,    2, 80000),
    (@lt2_4, @dvGiat4,    1, 50000),
    (@lt3_4, @dvCafe4,    2, 30000),
    (@lt3_4, @dvNuoc4,    4, 10000),
    (@lt6_4, @dvAirport4, 1, 250000),
    (@lt6_4, @dvMassage4, 1, 250000);
GO

/* =========================================================
   11. HOA DON / CHI TIET HOA DON / THANH TOAN
   ========================================================= */
DECLARE @khLan5 INT  = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456780');
DECLARE @khHung5 INT = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456789');
DECLARE @khAn5 INT   = (SELECT maKhachHang FROM dbo.KhachHang WHERE cccdPassport = '079123456781');

DECLARE @nvD5 INT = (SELECT maNhanVien FROM dbo.NhanVien WHERE cccd = '012345678904');

DECLARE @dp2_5 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khLan5  AND ngayNhanPhong = '2026-04-08');
DECLARE @dp3_5 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khHung5 AND ngayNhanPhong = '2026-04-13');
DECLARE @dp6_5 INT = (SELECT MIN(maDatPhong) FROM dbo.DatPhong WHERE maKhachHang = @khAn5   AND ngayNhanPhong = '2026-04-12');

DECLARE @lt2_5 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maDatPhong = @dp2_5);
DECLARE @lt3_5 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maDatPhong = @dp3_5);
DECLARE @lt6_5 INT = (SELECT maLuuTru FROM dbo.LuuTru WHERE maDatPhong = @dp6_5);

INSERT INTO dbo.HoaDon (maLuuTru, maDatPhong, maKhachHang, ngayLap, tienPhong, tienDichVu, phuThu, giamGia, tienCocTru, trangThai, ghiChu, ngayThanhToan)
VALUES
    (@lt2_5, @dp2_5, @khLan5,  '2026-04-10T11:45:00', 1300000, 230000, 0,      0,      200000, N'Đã thanh toán',   N'Đã thu đủ sau khi trừ tiền cọc', '2026-04-10T12:00:00'),
    (@lt3_5, @dp3_5, @khHung5, '2026-04-14T18:00:00', 1700000, 100000, 50000,  0,      300000, N'Chờ thanh toán', N'Khách đang lưu trú, chưa thanh toán', NULL),
    (@lt6_5, @dp6_5, @khAn5,   '2026-04-14T19:00:00', 3600000, 500000, 100000, 100000, 400000, N'Chờ thanh toán', N'Khách VIP đang dùng thêm dịch vụ', NULL);

DECLARE @hd2_5 INT = (SELECT maHoaDon FROM dbo.HoaDon WHERE maDatPhong = @dp2_5);
DECLARE @hd3_5 INT = (SELECT maHoaDon FROM dbo.HoaDon WHERE maDatPhong = @dp3_5);
DECLARE @hd6_5 INT = (SELECT maHoaDon FROM dbo.HoaDon WHERE maDatPhong = @dp6_5);

INSERT INTO dbo.ChiTietHoaDon (maHoaDon, loaiChiPhi, soLuong, donGia)
VALUES
    (@hd2_5, N'Tiền phòng', 2, 650000),
    (@hd2_5, N'Nước suối', 2, 10000),
    (@hd2_5, N'Bữa sáng', 2, 80000),
    (@hd2_5, N'Giặt ủi', 1, 50000),
    (@hd3_5, N'Tiền phòng', 2, 850000),
    (@hd3_5, N'Cà phê', 2, 30000),
    (@hd3_5, N'Nước suối', 4, 10000),
    (@hd6_5, N'Tiền phòng', 2, 1800000),
    (@hd6_5, N'Đưa đón sân bay', 1, 250000),
    (@hd6_5, N'Massage thư giãn', 1, 250000);

INSERT INTO dbo.ThanhToan (maHoaDon, maNhanVien, ngayThanhToan, soTien, phuongThuc, soThamChieu, ghiChu, loaiGiaoDich, trangThai)
VALUES
    (@hd2_5, @nvD5, '2026-04-10T12:00:00', 1330000, N'Chuyển khoản', N'VCB-20260410-001', N'Thanh toán sau khi trừ tiền cọc', N'THANH_TOAN', N'Hoàn tất');
GO

/* =========================================================
   12. DONG BO TRANG THAI PHONG
   ========================================================= */
UPDATE p
SET p.trangThai =
        CASE
            WHEN p.soPhong = '503' THEN N'Bảo trì'
            WHEN EXISTS (
                SELECT 1
                FROM dbo.LuuTru lt
                         JOIN dbo.DatPhong dp ON dp.maDatPhong = lt.maDatPhong
                WHERE lt.maPhong = p.maPhong
                  AND dp.trangThai = N'Đang lưu trú'
                  AND lt.checkOut IS NULL
            ) THEN N'Đang ở'
            WHEN EXISTS (
                SELECT 1
                FROM dbo.ChiTietDatPhong ctdp
                         JOIN dbo.DatPhong dp ON dp.maDatPhong = ctdp.maDatPhong
                WHERE ctdp.maPhong = p.maPhong
                  AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in')
            ) THEN N'Đã đặt'
            ELSE N'Hoạt động'
            END
    FROM dbo.Phong p;
GO

/* =========================================================
   13. REPORT VIEW / PROC
   ========================================================= */
IF OBJECT_ID('dbo.vw_DashboardTongQuan', 'V') IS NOT NULL
DROP VIEW dbo.vw_DashboardTongQuan;
GO
CREATE VIEW dbo.vw_DashboardTongQuan
AS
SELECT
    COUNT(*) AS tongPhong,
    SUM(CASE WHEN p.trangThai = N'Hoạt động' THEN 1 ELSE 0 END) AS phongHoatDong,
    SUM(CASE WHEN p.trangThai = N'Đang ở' THEN 1 ELSE 0 END) AS phongDangO,
    SUM(CASE WHEN p.trangThai = N'Đã đặt' THEN 1 ELSE 0 END) AS phongDaDat,
    SUM(CASE WHEN p.trangThai = N'Bảo trì' THEN 1 ELSE 0 END) AS phongBaoTri,
    (SELECT COUNT(*) FROM dbo.DatPhong dp WHERE CAST(dp.ngayDat AS DATE) = CAST(GETDATE() AS DATE)) AS bookingHomNay,
    (SELECT COUNT(*) FROM dbo.DatPhong dp WHERE CAST(dp.ngayNhanPhong AS DATE) = CAST(GETDATE() AS DATE)
                                            AND dp.trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã cọc', N'Chờ check-in')) AS choCheckInHomNay,
    (SELECT COUNT(*) FROM dbo.HoaDon hd WHERE hd.trangThai = N'Chờ thanh toán') AS choThanhToan,
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

/* =========================================================
   14. KIEM TRA NHANH
   ========================================================= */
SELECT tang, COUNT(*) AS soPhong
FROM dbo.Phong
GROUP BY tang
ORDER BY tang;

SELECT tenLoaiPhong
FROM dbo.LoaiPhong
ORDER BY maLoaiPhong;

SELECT tenDichVu, donGia
FROM dbo.DichVu
ORDER BY maDichVu;
GO