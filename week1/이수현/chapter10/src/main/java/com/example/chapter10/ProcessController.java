package com.example.chapter10;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessController {
  //현재 프로세스 정보 확인
  @GetMapping("/process")
  public String getProcessInfo(){
    ProcessHandle handle = ProcessHandle.current();
    ProcessHandle.Info info = handle.info();

    return String.format("""
            PID: %d
            Command: %s
            CPU Time: %s
            Start Time: %s
            User: %s
            """,
            handle.pid(),
            info.command().orElse("N/A"),
            info.totalCpuDuration().orElse(null),
            info.startInstant().orElse(null),
            info.user().orElse("N/A"));
  }
}
