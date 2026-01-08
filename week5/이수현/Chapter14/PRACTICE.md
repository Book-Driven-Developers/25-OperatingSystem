# JVM Heap & OS Virtual Memory Interaction Lab (macOS)
## 📌실습 목표
이 실습은 JVM 힙 메모리 사용이 macOS의 가상 메모리(Virtual Memory)와 실제 물리 메모리(RSS)에 어떻게 반영되는지를 직접 관찰하는 것을 목표로 한다.
+ VSZ(virtual size): 주소 공간을 얼마나 확보했는지 (예약 / 매핑 포함)
+ RSS(Resident Set Size): 진짜 RAM에 올라온 페이지가 얼마나 되는지 (물리 공간)

## 🧪 환경
OS: macOS (ARM64)
JDK: OpenJDK 21.0.6
Framework: Spring Boot
JVM 옵션:
  ```-Xms64m -Xmx4096m```

## 실습 포인트
+ heapHold와 directHold는 List의 동시성 제어가 가능한 CopyOnWriteArrayList 클래스를 사용한다.
  + heapHold: 힙 메모리에 올라가는 byte를 계속 보관함으로써 GC가 메소드 종료 시 바로 회수하지 않도록 함
  + directHold: 오프힙(네이티브 메모리)를 쓰는 ByteBuffer.allocateDirect() 결과를 계속 보관

## 🔬 실험 구성
### API
| Endpoint             | 설명                   |
|:---------------------|:---------------------|
| /practice/mem/snap   | JVM 힙 상태 및 PID 조회    |
| /practice/mem/heap   | 힙 메모리 점진 할당          |
| /practice/mem/direct | 오프힙(DirectBuffer) 할당 |
| /practice/mem/clear  | 참조 제거 + GC           |

### OS 관측 명령어
```bash
#macos 기준
ps -o pid,rss,vsz,comm -p <pid>
vmmap -summary <pid>
```

## 📊 실험 결과
### 1. 초기 상태 (메모리 할당 전)
#### JVM 상태
<img width="855" height="63" alt="Image" src="https://github.com/user-attachments/assets/9cb09672-a5e3-4c0c-b6ea-198f5143b270" />

```json
{
  "heapUsedMB": 19,
  "heapCommittedMB": 64,
  "heapMaxMB": 4096,
  "heapHoldChunks": 0,
  "directBuffers": 0,
  "pid": 45494
}

```
#### OS 상태
<img width="748" height="70" alt="Image" src="https://github.com/user-attachments/assets/463fd338-a05b-4998-bd82-1656130866d0" />

```text
RSS: 144,560 KB  (~141 MB)
VSZ: 416,951,168 KB (~397 GB)
```

#### 해석
  + JVM 힙 사용량은 매우 적음
  + 그러나 OS RSS는 이미 140MB 이상
  + 이는 JVM 자체 런타임, Metaspace, JIT(Code Cache), Thread Stack 등으로 인해 발생
  > JVM 힙 사용량 ≠ 프로세스 전체 메모리 사용량

### 2. 힙(Heap) 메모리 64MB 할당
#### 실행 코드
```bash
POST /practice/mem/heap?allocSize=64&arraySize=1024&touch=true
```
#### JVM 상태
<img width="1045" height="154" alt="Image" src="https://github.com/user-attachments/assets/4c104919-ef65-46bc-b34b-80da18933ce4" />

```json
{
  "heapUsedMB": 143,
  "heapCommittedMB": 164,
  "heapMaxMB": 4096,
  "heapHoldChunks": 64
}
```

#### OS 상태
<img width="741" height="60" alt="Image" src="https://github.com/user-attachments/assets/f50a4bef-5cc3-4850-ad9b-6724301a66e5" />

```text
RSS: 147,568 KB (~144 MB)
VSZ: 416,961,408 KB
```

#### 해석
  + JVM 힙 사용량 및 커밋 메모리는 증가
  + 하지만 OS RSS 증가는 제한적(+약 3MB)
  > 힙 커밋 증가 ≠ 즉시 물리 메모리(RSS) 증가
  > 
  > 이는 macOS 가상 메모리 정책과 JVM의 페이지 관리 전략에 따른 정상 동작이다.

### 3. 오프힙(DirectBuffer) 64MB 할당
#### 실행 코드
```bash
POST /practice/mem/direct?allocSize=64&touch=true
```
#### JVM 상태
<img width="1048" height="130" alt="Image" src="https://github.com/user-attachments/assets/707aee40-ba5c-4330-bddc-d4768e56a697" />

```json
{
  "heapUsedMB": 145,
  "heapCommittedMB": 164,
  "directBuffers": 1
}
```
#### OS 상태
<img width="732" height="68" alt="Image" src="https://github.com/user-attachments/assets/51657640-1c89-4cc2-906f-53706662dfa5" />

```text
RSS: 204,496 KB (~200 MB)
VSZ: 417,026,960 KB
```

#### 해석
  + JVM 힙 사용량은 거의 변하지 않음
  + OS RSS는 약 +56MB 급증
  > DirectBuffer는 JVM 힙 외부의 네이티브 메모리를 사용하며, OS 물리 메모리를 직접 점유한다
  >
  > 이로 인해 “힙은 여유 있는데 OOM(Out Of Memory) 발생” 같은 현상이 나타날 수 있다.

### 4. clear + GC 이후
#### 실행 코드
```bash
POST /practice/mem/clear?gc=true
```
#### JVM 상태
<img width="1051" height="68" alt="Image" src="https://github.com/user-attachments/assets/96f7fd7d-e245-46b8-8d4d-a86c2884979c" />

```json
{
  "heapUsedMB": 11,
  "heapCommittedMB": 48,
  "heapHoldChunks": 0,
  "directBuffers": 0
}
```
#### OS 상태
<img width="737" height="69" alt="Image" src="https://github.com/user-attachments/assets/3ec5c6cd-9db6-490f-b060-f568de617557" />

```text
RSS: 193,600 KB (~189 MB)
VSZ: 417,026,960 KB
```

#### 해석
  + JVM 내부 힙은 정상적으로 정리됨
  + 그러나 OS RSS는 즉시 크게 줄지 않음
  > GC는 JVM 내부 객체 정리일 뿐, OS 메모리 반환을 즉시 보장하지 않는다
  >
  > 즉, JVM은 반환된 메모리를 재사용을 위해 보유할 수 있으며, OS 역시 메모리 회수를 지연할 수 있다.

## 🎯 결론
+ JVM 힙 사용량과 OS 물리 메모리 사용량은 1:1 관계가 아니다
+ 가상 메모리(VSZ), 힙 커밋, 실제 물리 메모리(RSS)는 서로 다른 기준으로 관리된다
+ 오프힙 메모리는 JVM 힙과 무관하게 OS 메모리를 직접 점유한다
+ GC 이후에도 OS RSS가 즉시 감소하지 않는 것은 정상 동작이다

## 🧠 실무적 시사점
+ “힙 여유 있음에도 OOM 발생” 원인 설명 가능
+ 컨테이너 메모리 초과(Kubernetes OOMKill) 분석에 필수 개념
+ JVM 튜닝 시 힙 외 메모리(Metaspace, DirectBuffer, Stack)를 반드시 고려해야 함