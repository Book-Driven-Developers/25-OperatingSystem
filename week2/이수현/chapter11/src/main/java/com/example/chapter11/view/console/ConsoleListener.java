package com.example.chapter11.view.console;

import com.example.chapter11.simulator.clock.ClockProvider;
import com.example.chapter11.domain.model.Process;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleListener {
  private final AtomicBoolean running;
  private final ConsoleInputReader reader;
  private final ProcessParser parser;
  private final ProcessArrivalHandler arrivalHandler;
  private final ClockProvider clockProvider;

  public ConsoleListener(
          AtomicBoolean running,
          ConsoleInputReader reader,
          ProcessParser parser,
          ProcessArrivalHandler arrivalHandler,
          ClockProvider clockProvider
  ){
    this.running = running;
    this.reader = reader;
    this.parser = parser;
    this.arrivalHandler = arrivalHandler;
    this.clockProvider = clockProvider;
  }

  public void start(){
    Thread t = new Thread(this::loop);
    t.setDaemon(true);
    t.start();
  }

  public void loop(){
    System.out.println("새로운 프로세스 정보를 입력해주세요(종료 시 quit): ");
    while(running.get()){
      String line = reader.readLine();
      if (line == null) {
        System.out.println("콘솔 입력을 사용할 수 없어 입력 리스너를 종료합니다.");
        running.set(false);
        break;
      }

      if("quit".equalsIgnoreCase(line)){
        running.set(false);
        System.out.println("콘솔 입력을 종료합니다. (나머지 프로세스들은 계속 실행됨)");
        break;
      }

      parser.parse(line)
              .ifPresent(processSpec -> {
                int now = clockProvider.now();
                Process process = new Process(processSpec.pid(), now, processSpec.burstTime());
                arrivalHandler.handle(process, now);
                System.out.printf("[t=%d] ADD %s%n", clockProvider.now(), process);
              });
    }
  }
}
