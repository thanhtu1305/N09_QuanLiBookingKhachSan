package entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class DashboardTaskRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskCode;
    private String taskType;
    private String target;
    private String timeText;
    private String status;
    private String actionHint;
    private int priority;
    private Timestamp sortTime;

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionHint() {
        return actionHint;
    }

    public void setActionHint(String actionHint) {
        this.actionHint = actionHint;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Timestamp getSortTime() {
        return sortTime;
    }

    public void setSortTime(Timestamp sortTime) {
        this.sortTime = sortTime;
    }
}
