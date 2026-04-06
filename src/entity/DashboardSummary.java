package entity;

import java.io.Serializable;

public class DashboardSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private int activeRooms;
    private int occupiedRooms;
    private int bookedRooms;
    private int maintenanceRooms;
    private int todayBookings;
    private int pendingCheckinToday;
    private int pendingPaymentCount;
    private int checkoutDueTodayCount;
    private double revenueToday;
    private double revenueThisMonth;

    public int getActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(int activeRooms) {
        this.activeRooms = activeRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(int occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public int getBookedRooms() {
        return bookedRooms;
    }

    public void setBookedRooms(int bookedRooms) {
        this.bookedRooms = bookedRooms;
    }

    public int getMaintenanceRooms() {
        return maintenanceRooms;
    }

    public void setMaintenanceRooms(int maintenanceRooms) {
        this.maintenanceRooms = maintenanceRooms;
    }

    public int getTodayBookings() {
        return todayBookings;
    }

    public void setTodayBookings(int todayBookings) {
        this.todayBookings = todayBookings;
    }

    public int getPendingCheckinToday() {
        return pendingCheckinToday;
    }

    public void setPendingCheckinToday(int pendingCheckinToday) {
        this.pendingCheckinToday = pendingCheckinToday;
    }

    public int getPendingPaymentCount() {
        return pendingPaymentCount;
    }

    public void setPendingPaymentCount(int pendingPaymentCount) {
        this.pendingPaymentCount = pendingPaymentCount;
    }

    public int getCheckoutDueTodayCount() {
        return checkoutDueTodayCount;
    }

    public void setCheckoutDueTodayCount(int checkoutDueTodayCount) {
        this.checkoutDueTodayCount = checkoutDueTodayCount;
    }

    public double getRevenueToday() {
        return revenueToday;
    }

    public void setRevenueToday(double revenueToday) {
        this.revenueToday = revenueToday;
    }

    public double getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(double revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }
}
