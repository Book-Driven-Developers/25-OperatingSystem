package com.example.chapter10;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ThreadController {
  @GetMapping("/threads")
  public String getThreads(){
    StringBuilder sb = new StringBuilder();
    Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
    sb.append("Active Thread Count: ").append(threads.size()).append("\n\n");

    threads.forEach((thread, stack) -> {
      sb.append("Thread Name: ").append(thread.getName()).append("\n");
      sb.append("Thread ID: ").append(thread.threadId()).append("\n");
      sb.append("State: ").append(thread.getState()).append("\n");
      sb.append("Is Daemon: ").append(thread.isDaemon()).append("\n\n");
    });

    return sb.toString();
  }
}
