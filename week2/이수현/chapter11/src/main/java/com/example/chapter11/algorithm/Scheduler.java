package com.example.chapter11.algorithm;

import com.example.chapter11.domain.model.Process;
import java.util.Queue;

public interface Scheduler {
  Process pickNext(Queue<Process> readyQueue, Process current);
}
