package gui;

import utils.NavigationUtil.ScreenKey;

import java.awt.Color;

public class BaoCaoGUI extends AbstractBaoCaoGUI {

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
                new MetricItem[]{
                        new MetricItem("Doanh thu tháng", "4,28 tỷ", "Tăng 8,6% so với tháng trước", new Color(22, 163, 74)),
                        new MetricItem("Booking mới", "326", "Đơn đã xác nhận trong kỳ", new Color(37, 99, 235)),
                        new MetricItem("Công suất phòng", "82%", "Tỷ lệ lấp đầy trung bình", new Color(245, 158, 11)),
                        new MetricItem("Khách quay lại", "41%", "Tỷ lệ khách hàng thân thiết", new Color(168, 85, 247))
                },
                new InfoItem[]{
                        new InfoItem("Nhóm nổi bật", "Doanh thu dịch vụ tăng mạnh vào cuối tuần."),
                        new InfoItem("Điểm cần theo dõi", "3 hạng phòng có công suất dưới 60%."),
                        new InfoItem("Khuyến nghị", "Ưu tiên upsell combo dịch vụ cho khách đoàn.")
                },
                new String[]{"Nhóm báo cáo", "Chỉ số chính", "Kết quả", "Ghi chú"},
                new Object[][]{
                        {"Doanh thu", "Thực thu", "4.280.000.000", "Ổn định"},
                        {"Đặt phòng", "Booking mới", "326", "Cuối tuần tăng cao"},
                        {"Phòng", "Công suất TB", "82%", "Tầng 3 vượt mục tiêu"},
                        {"Dịch vụ", "Top bán chạy", "Bữa sáng", "Chiếm 31% doanh thu DV"},
                        {"Khách hàng", "Khách quay lại", "41%", "Tăng 5%"}
                },
                "F1 Xem chi tiết",
                "F2 Xuất file",
                "F5 Làm mới"
        );
    }
}
