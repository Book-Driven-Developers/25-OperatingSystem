package operation_system.cpu_scheduling.entity;

public class PCB {
    private int pid; // 프로세스 ID

    private String register;  // 레지스터 값

    private String processState; //프로세스 상태

    private SchedulingInfo cpuScheduledInfo; // CPU 스케줄링 정보

    private String memory; //메모리 정보

    private String usedFile; // 사용한 파일 정보

    private String ioDevice; // 입출력 장치 정보

    private int ppid; //부모 프로세스 ID

    //필수 생성자 (약식)
    public PCB(int pid, String processState, SchedulingInfo cpuScheduledInfo) {
        this.pid = pid;
        this.processState = processState;
        this.cpuScheduledInfo = cpuScheduledInfo;
    }

    public String getProcessState() {
        return processState;
    }

    public SchedulingInfo getCpuScheduledInfo() {
        return cpuScheduledInfo;
    }

    public int getPid() {
        return pid;
    }
}
