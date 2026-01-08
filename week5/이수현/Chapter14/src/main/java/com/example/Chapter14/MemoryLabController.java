package com.example.Chapter14;

import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/practice/mem")
public class MemoryLabController {
  private final List<byte[]> heapHold = new CopyOnWriteArrayList<>();
  private final List<ByteBuffer> directHold = new CopyOnWriteArrayList<>();

  // 힙 점진 할당
  @PostMapping("/heap")
  public Map<String, Object> heap(
          @RequestParam int allocSize, //megaByte
          @RequestParam(defaultValue = "1024") int arraySize,
          @RequestParam(defaultValue = "true") boolean touch
  ){
    int chunks = (allocSize * 1024) / arraySize;
    int bytes = arraySize * 1024;

    //할당
    for (int i =0; i<chunks; i++){
      byte[] arr = new byte[bytes]; //JVM 힙에 배열 할당 -> /lab/mem/snap 에서 heapUsedMB 증가
      if (touch) touchEachPage(arr);
      heapHold.add(arr); // GC가 못 지우게 유지
    }
    return snapshot("heapAllocatedMb", allocSize, "heapChunks", chunks);
  }

  // 오프힙(네이티브 메모리) 점진 할당
  @PostMapping("/direct")
  public Map<String, Object> direct(
          @RequestParam int allocSize, //megaByte
          @RequestParam(defaultValue = "true") boolean touch){
    int bytes = allocSize * 1024 * 1024;
    ByteBuffer buf = ByteBuffer.allocateDirect(bytes);  // JVM 힙이 아닌, OS 네이티브 메모리를 확보함
    if (touch) touchDirect(buf);
    directHold.add(buf);
    return snapshot("directAllocatedMb", allocSize, "directBuffers", directHold.size());
  }

  // 참조 제거 + GC 호출
  @PostMapping("/clear")
  public Map<String, Object> clear(@RequestParam(defaultValue = "true") boolean gc){
    heapHold.clear();
    directHold.clear();
    if (gc) System.gc(); // gc를 호출했을 때 바로 OS RSS가 줄어들지?
    return snapshot("cleared", true, "gcCalled", gc);
  }

  // 현재 상태 로깅
  @GetMapping("/snap")
  public Map<String, Object> snap() {
    return snapshot();
  }

  //배열의 매 페이지에 값을 넣어 OS가 실제로 해당 페이지를 RAM에 올리도록 함
  private void touchEachPage(byte[] arr){
    // OS는 보통 메모리를 페이지 단위(4kb)로 관리
    for(int i = 0; i<arr.length; i += 4096){
      arr[i] = 1;
    }
  }

  // 주소만 잡는 게 아니라 실제 페이지에 값을 저장함으로써 RSS를 확실하게 증가시킴
  private void touchDirect(ByteBuffer buf) {
    for (int i = 0; i < buf.capacity(); i += 4096) {
      buf.put(i, (byte) 1);
    }
  }

  private Map<String, Object> snapshot(Object... extraKv) {
    Runtime rt = Runtime.getRuntime();
    long max = rt.maxMemory();     // -Xmx 근처 (JVM이 최대로 쓰겠다고 허용된 힙 상한)
    long total = rt.totalMemory(); // 현재 JVM이 OS로부터 "커밋/확보"해 둔 힙 크기
    long free = rt.freeMemory();   // total 안에서 아직 안 쓰는 여유
    long used = total - free;      // total 중 실제 사용중

    Map<String, Object> m = new LinkedHashMap<>();
    m.put("heapUsedMB", used / 1024 / 1024);
    m.put("heapCommittedMB", total / 1024 / 1024);
    m.put("heapMaxMB", max / 1024 / 1024);
    m.put("heapHoldChunks", heapHold.size());
    m.put("directBuffers", directHold.size());
    m.put("pid", ProcessHandle.current().pid());

    for (int i = 0; i + 1 < extraKv.length; i += 2) {
      m.put(String.valueOf(extraKv[i]), extraKv[i + 1]);
    }
    return m;
  }
}
