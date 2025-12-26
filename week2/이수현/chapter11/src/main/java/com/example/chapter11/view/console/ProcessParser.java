package com.example.chapter11.view.console;

import com.example.chapter11.domain.model.Process;

import java.util.Optional;

// 프로세스 정보 파싱 클래스
public class ProcessParser {
  public Optional<ProcessSpec> parse(String line){
    if(line == null || line.isBlank()) return Optional.empty();
    if(line.equalsIgnoreCase("quit")) return Optional.empty();

    try{
      return Optional.of(parseStrict(line));
    }catch (Exception e){
      return Optional.empty();
    }
  }

  public ProcessSpec parseStrict(String line){
    String[] pcb = line.split(",");
    if (pcb.length != 2) {
      throw new IllegalArgumentException("잘못된 포맷: " + line + " -> pid,burst");
    }

    try {
      long pid = Long.parseLong(pcb[0].trim());
      int burstTime = Integer.parseInt(pcb[1].trim());
      return new ProcessSpec(pid, burstTime);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("숫자 파싱 실패: " + line, nfe);
    }
  }

  public record ProcessSpec(long pid, int burstTime) {}
}
