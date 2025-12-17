package com.example.chapter11.app;

import com.example.chapter11.algorithm.Scheduler;
import com.example.chapter11.domain.model.Process;
import com.example.chapter11.domain.model.SchedulerFactory;
import com.example.chapter11.domain.model.SchedulerType;
import com.example.chapter11.simulator.Simulator;
import com.example.chapter11.simulator.clock.ClockSimulator;
import com.example.chapter11.simulator.report.ReportPrinter;
import com.example.chapter11.simulator.report.SimulationResult;
import com.example.chapter11.view.console.ConsoleInputReader;
import com.example.chapter11.view.console.ConsoleListener;
import com.example.chapter11.view.console.ProcessArrivalHandler;
import com.example.chapter11.view.console.ProcessParser;
import com.example.chapter11.view.file.ProcessLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulatorRunner {
  private static final long DEFAULT_TICK_MILLIS = 2000L;

  public void run(String[] args) throws Exception{
    // 실행할 스케줄러 생성
    RunConfig runConfig = parseArgs(args);
    Scheduler scheduler = createScheduler(runConfig.schedulerType);

    // 텍스트파일로부터 pcb 정보 가져오기
    List<Process> initialProcesses = loadInitialProcesses(runConfig.processFilePath);

    // CPU 시뮬레이터 생성
    Simulator simulator = createSimulator(initialProcesses, scheduler);
    startConsoleListener(simulator);

    // 실행 결과 보고
    SimulationResult simulationResult = runSimulation(simulator, DEFAULT_TICK_MILLIS);
    printReport(simulationResult);
  }
  private record RunConfig(String processFilePath, SchedulerType schedulerType) {}

  private RunConfig parseArgs(String[] args){
    if (args == null || args.length < 2){
      throw new IllegalArgumentException(
              "사용법: <process_file> <algorithm>\n예) processes.txt fcfs"
      );
    }
    String filePath = args[0];
    SchedulerType schedulerType = SchedulerType.from(args[1]);
    return new RunConfig(filePath, schedulerType);
  }

  private Scheduler createScheduler(SchedulerType schedulerType){
    return SchedulerFactory.create(schedulerType);
  }

  private List<Process> loadInitialProcesses(String filePath) throws Exception{
    return new ProcessLoader().load(filePath);
  }

  private Simulator createSimulator(List<Process> initialProcesses, Scheduler scheduler){
    return new Simulator(initialProcesses, scheduler);
  }

  private void startConsoleListener(Simulator simulator){
    AtomicBoolean inputRunning = new AtomicBoolean(true);

    ConsoleListener consoleListener = new ConsoleListener(
            inputRunning,
            new ConsoleInputReader(),
            new ProcessParser(),
            new ProcessArrivalHandler(simulator.getReadyQueue(), simulator.getFutureArrivals()),
            new ClockSimulator(simulator::getClock)
    );
    consoleListener.start();
  }

  private SimulationResult runSimulation(Simulator simulator, long tickMillis) throws InterruptedException{
    while (!simulator.isFinished()){
      simulator.tick();
      Thread.sleep(tickMillis); // 시뮬레이션 속도 제어 목적
    }
    return simulator.getResult();
  }

  private void printReport(SimulationResult simulationResult){
    new ReportPrinter().print(simulationResult);
  }
}
