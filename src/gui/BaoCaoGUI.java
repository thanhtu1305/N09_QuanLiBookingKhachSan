package gui;

import dao.BaoCaoDAO;
import utils.NavigationUtil.ScreenKey;

import java.awt.Color;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class BaoCaoGUI extends AbstractBaoCaoGUI {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");

    public BaoCaoGUI() {
        this("guest", "Quản lý");
    }

    public BaoCaoGUI(String username, String role) {
        super(
                username,
                role,
                ScreenKey.BAO_CAO,
                "Báo cáo tổng quan",
                "BÁO CÁO TỔNG QUAN",
                "Tổng hợp nhanh các nhóm báo cáo doanh thu, đặt phòng, phòng, dịch vụ và khách hàng trên cùng một màn hình.",
                "Bảng tổng quan báo cáo",
                buildMetrics(),
                buildHighlights(),
                new String[]{"Nhóm báo cáo", "Chỉ số chính", "Kết quả", "Ghi chú"},
                buildRows(),
                "F1 Xem chi tiết",
                "F2 Xuất file"
        );
    }

    private static MetricItem[] buildMetrics() {
        BaoCaoDAO.OverviewSummary data = loadOverview();
        return new MetricItem[]{
                new MetricItem("Doanh thu tháng", formatMoney(data.getPaidRevenue()), "Thực thu từ thanh toán hoàn tất", new Color(22, 163, 74)),
                new MetricItem("Booking mới", formatNumber(data.getTotalBookings()), "Đơn đặt phòng trong kỳ", new Color(37, 99, 235)),
                new MetricItem("Công suất phòng", data.getOccupancyPercent() + "%", "Từ trạng thái phòng hiện tại", new Color(245, 158, 11)),
                new MetricItem("Khách có booking", formatNumber(data.getTotalCustomers()), "Khách phát sinh đặt phòng", new Color(168, 85, 247))
        };
    }

    private static InfoItem[] buildHighlights() {
        BaoCaoDAO.OverviewSummary data = loadOverview();
        String topService = data.getTopServiceName() == null || data.getTopServiceName().trim().isEmpty()
                ? "Không có dữ liệu dịch vụ" : data.getTopServiceName();
        return new InfoItem[]{
                new InfoItem("Nguồn dữ liệu", "Đang đọc trực tiếp từ SQL Server QLKS."),
                new InfoItem("Dịch vụ nổi bật", topService),
                new InfoItem("Trạng thái", data.getTotalBookings() == 0 ? "Không có dữ liệu trong kỳ." : "Có dữ liệu phát sinh trong kỳ.")
        };
    }

    private static Object[][] buildRows() {
        BaoCaoDAO.OverviewSummary data = loadOverview();
        return new Object[][]{
                {"Doanh thu", "Thực thu", formatMoney(data.getPaidRevenue()), "ThanhToan hoàn tất"},
                {"Hóa đơn", "Tổng hóa đơn", formatMoney(data.getInvoiceRevenue()), "Từ HoaDon"},
                {"Đặt phòng", "Booking mới", formatNumber(data.getTotalBookings()), "Từ DatPhong"},
                {"Phòng", "Công suất TB", data.getOccupancyPercent() + "%", "Từ Phong/LoaiPhong"},
                {"Dịch vụ", "Doanh thu dịch vụ", formatMoney(data.getServiceRevenue()), "Từ SuDungDichVu"}
        };
    }

    private static BaoCaoDAO.OverviewSummary loadOverview() {
        LocalDate today = LocalDate.now();
        return new BaoCaoDAO().getOverviewSummary(today.withDayOfMonth(1), today);
    }

    private static String formatMoney(double value) {
        return NUMBER_FORMAT.format(Math.round(value)) + " đ";
    }

    private static String formatNumber(int value) {
        return NUMBER_FORMAT.format(value);
    }
}
