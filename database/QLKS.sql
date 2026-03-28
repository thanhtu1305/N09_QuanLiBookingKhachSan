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
    tienPhong DECIMAL(15,0),
    tienDichVu DECIMAL(15,0),
    tongTien AS (tienPhong + tienDichVu) PERSISTED,
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
    soTien DECIMAL(15,0),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

INSERT INTO NhanVien (hoTen, ngaySinh, gioiTinh, cccd, soDienThoai, email, diaChi, boPhan, chucVu, caLam, ngayVaoLam, trangThai, ghiChu)
VALUES
(N'Nguyễn Văn A', '2000-01-01', N'Nam', '012345678901', '0909000001', 'nva@gmail.com', N'Hà Nội', N'Lễ tân', N'Nhân viên', N'Ca sáng', '2024-01-01', N'Hoạt động', N'Nhân viên lễ tân'),
(N'Trần Thị B', '1998-05-10', N'Nữ', '012345678902', '0909000002', 'ttb@gmail.com', N'Đà Nẵng', N'Quản lý', N'Quản lí', N'Ca hành chính', '2023-01-01', N'Hoạt động', N'Quản lý khách sạn'),
(N'Lê Văn C', '1999-08-20', N'Nam', '012345678903', '0909000003', 'lvc@gmail.com', N'TP.HCM', N'Buồng phòng', N'Nhân viên', N'Ca chiều', '2024-02-15', N'Hoạt động', N'Nhân viên hỗ trợ');
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
(1, '101', N'Tầng 1', N'Khu A', 2, 3, N'Trống'),
(1, '102', N'Tầng 1', N'Khu A', 2, 3, N'Đã đặt'),
(2, '201', N'Tầng 2', N'Khu A', 2, 4, N'Đang ở'),
(2, '202', N'Tầng 2', N'Khu B', 2, 4, N'Trống'),
(3, '301', N'Tầng 3', N'Khu VIP', 3, 5, N'Bảo trì'),
(4, '401', N'Tầng 4', N'Khu Family', 4, 6, N'Trống');
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

INSERT INTO DatPhong (maKhachHang, maNhanVien, maBangGia, ngayDat, ngayNhanPhong, ngayTraPhong, soLuongPhong, soNguoi, tienCoc, trangThai)
VALUES
(1, 1, 1, '2026-03-20', '2026-03-26', '2026-03-27', 1, 2, 200000, N'Đã xác nhận'),
(2, 1, 2, '2026-03-21', '2026-03-26', '2026-03-28', 1, 2, 300000, N'Đang lưu trú'),
(3, 2, 3, '2026-03-22', '2026-03-27', '2026-03-29', 1, 3, 500000, N'Chờ check-in');
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