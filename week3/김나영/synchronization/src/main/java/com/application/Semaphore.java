package com.application;

import java.util.LinkedList;
import java.util.Queue;

/**
세마포어 값이 -2에서 -1로 변하는 구간 설명

- 초기 상태  
  - 공유 자원 수: 2  
  - 실행 중인 프로세스: 1, 2  
  - 대기 큐: [3, 4]  
  - 세마포어 값 S = -2  

- 프로세스 1이 작업을 종료하고 `signal()` 호출  

- 내부 동작  
  - `S++` 실행 → S: -2 → -1  
  - S ≤ 0 이므로 대기 중인 프로세스가 존재함을 의미  
  - 대기 큐에서 하나의 프로세스를 깨움 (3번)  

- 의미 해석  
  - S가 음수라는 것은 자원이 음수라는 뜻이 아님  
  - `-S`는 대기 중인 프로세스 수를 의미  
  - S = -1 → 대기 중인 프로세스 1명(4번) 남아 있음  

- 결과 상태  
  - 반환된 자원 1개가 즉시 프로세스 3에게 할당됨  
  - 실행 중인 프로세스: 2, 3  
  - 자원 사용 개수는 여전히 2개로 정상 */ 

public class Semaphore {
    private int S;
    private Queue<Thread> queue;

    public Semaphore(int S) {
        this.S = S;
        this.queue = new LinkedList<>();
    }

    public synchronized void wait_sempahore() throws InterruptedException {
        S--;

        // wait은 반드시 while문으로 감싸야 함
        while (S < 0) {
            queue.add(Thread.currentThread());
            wait();   // 깨어나도 다시 조건 확인
        }
    }

    public synchronized void signal() {
        S++;
        if (S <= 0) {
            Thread t = queue.poll();
            if (t != null) {
                notify();
            }
        }
    }
}
