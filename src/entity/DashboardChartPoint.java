package entity;

import java.io.Serializable;

public class DashboardChartPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private double value;

    public DashboardChartPoint() {
    }

    public DashboardChartPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
