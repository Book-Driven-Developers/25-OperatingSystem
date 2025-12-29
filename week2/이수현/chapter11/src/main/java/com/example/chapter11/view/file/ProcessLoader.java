package com.example.chapter11.view.file;

import com.example.chapter11.domain.model.Process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProcessLoader {
  public List<Process> load(String path) throws Exception{
    List<Process> list = new ArrayList<>();
    for(String line : readFile(path)){
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) continue;
      list.add(parseLine(line));
    }
    return list;
  }

  public Process parseLine(String line){
    String[] pcb = line.split(",");
    if (pcb.length < 3){
      throw new IllegalArgumentException("잘못된 포맷: " + line +
              "  -> pid,arrival,burst 형식이어야 합니다.");
    }
    Process process;
    try{
      long pid = Long.parseLong(pcb[0].trim());
      int arrivalTime = Integer.parseInt(pcb[1].trim());
      int burstTime = Integer.parseInt(pcb[2].trim());
      process = new Process(pid, arrivalTime, burstTime);
    } catch (NumberFormatException nfe){
      throw new IllegalArgumentException("숫자 파싱 실패");
    }
    return process;
  }

  public List<String> readFile(String path) throws Exception{
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream resource = classLoader.getResourceAsStream(path);

    List<String> lines;
    if(resource != null){
      try(BufferedReader br = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))){
        lines = br.lines().toList();
      }
    }else{
      lines = Files.readAllLines(Path.of(path));
    }
    return lines;
  }
}
