package operation_system.cpu_scheduling.entity;


public class SchedulingInfo {
    private int priority;          // 우선순위 (낮을수록 높은 우선순위)
    private int burstTime;         // CPU 버스트 시간 (예상 실행 시간)
    private int arrivalTime;       // 프로세스 도착 시간
    private int remainingTime;     // 남은 실행 시간 (RR에서 사용)
    private long lastScheduledTime; // 마지막 스케줄링 시간

    public SchedulingInfo(int priority, int burstTime, int arrivalTime, int remainingTime, long lastScheduledTime) {
        this.priority = priority;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.remainingTime = remainingTime;
        this.lastScheduledTime = lastScheduledTime;
    }

    // 필수적인 내용만 들어간 생성자 (약식)
    public SchedulingInfo(int priority,int remainingTime){
        this.priority = priority;
        this.remainingTime = remainingTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public long getLastScheduledTime() {
        return lastScheduledTime;
    }

    public void setLastScheduledTime(long lastScheduledTime) {
        this.lastScheduledTime = lastScheduledTime;
    }
}
