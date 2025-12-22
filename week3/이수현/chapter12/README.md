# chapter12 - 프로세스 동기화
## 12-1 동기화란 
> 동시다발적으로 실행되는 프로세스, 스레드들은 서로 협력하며 영향을 주고 받음
> 
> 이 과정에서 자원의 일관성 (= 동기화)을 보장해야한다.
> > 앞으로 정리한 내용들에서 나오는 동기화 대상을 편의상 프로세스와 스레드를 합쳐서 프로세스라고 하겠다.
### 동기화의 의미
+ 프로세스(또는 스레드)들의 수행 시기를 맞추는 것
+ 어떻게?
  + **실행 순서 제어**: 프로세스를 올바른 순서대로 실행
  + **상호 배제**: 동시에 접근해서는 안되는 자원에 하나의 프로세스만 접근하게 하기 
### 실행 순서 제어를 위한 동기화
#### 필요한 이유: Reader Writer Problem
<img width="310" height="215" alt="Image" src="https://github.com/user-attachments/assets/1dd62e26-14ab-44f8-95ea-d30458bbb3b7" />

+ 상황
  + Writer: Book.txt 파일에 값을 저장하는 프로세스
  + Reader: Book.txt 파일에 저장된 값을 읽어들이는 프로세스
+ Reader, Writer 프로세스는 무작정 아무렇게나 실행되어선 안됨 -> 실행 순서가 있기 때문!
+ Reader 프로세스는 'Book.txt 안에 값이 존재한다'틑 특정 조건이 만족되어야만 실행 가능
### 상호 배제를 위한 동기화
+ 공유가 불가능한 자원의 동시 사용을 피하기 위한 동기화
+ **한 번에 하나의 프로세스**만 접근해야 하는 자원에 동시 접근을 피하기 위한 동기화
#### 필요한 이유: Back Account Problem
<img width="478" height="257" alt="Image" src="https://github.com/user-attachments/assets/1a62cc37-9088-46bc-917a-bd6f068ed5b0" />

+ 상황
  + 현재 계좌의 잔액: 10만원
  + 프로세스A: 현재 잔액에 2만원을 추가하는 프로세스
  + 프로세스B: 현재 잔액에 5만을 추가하는 프로세스
+ 만약 동기화 없이 무작위로 프로세스들이 실행된다면?
    -> 실행 결과: 둘다 잔액을 10만원으로 읽게 되어 최종적으로 잔액이 15만원이 될 수 있다.
    -> why? 프로세스A의 자원에 대한 실행이 다 끝나기 전에 프로세스B가 해당 자원에 접근해서
#### 필요한 이유: Producer & Consumer Problem
<img width="554" height="238" alt="Image" src="https://github.com/user-attachments/assets/79cce9af-7969-4568-a3cc-84b529d05279" />

+ 상황
  + Producer: 물건을 계속해서 생산하는 프로세스
  + Consumer: 물건을 계속해서 소비하는 프로세스
  + '총합' 변수를 공유하고 있음
+ 만약 생산자를 100,000번, 소비자를 100,000번 실행하면 총합은?
  -> 떄로는 0과 다른 값이 되거나 오류가 발생하기도 함
  <img width="561" height="157" alt="Image" src="https://github.com/user-attachments/assets/00ec897c-c85f-425b-a424-e94618e16a38" />

#### 상호 배제를 위한 동기화를 위한 세가지 원칙
1. 상호 배제 (mutual exclusion)
   + 한 프로세스가 임계구역에 진입했다면, 다른 프로세스는 들어올 수 없다.
2. 진행 (progress)
   + 임계 구역에 어떤 프로세스도 진입하지 않았다면, 진입하고자 하는 프로세스는 들어갈 수 있어야 한다.
3. 유한 대기 (bounded waiting)
   + 한 프로세스가 임계 구역에 진입하고 싶다면, 언제가는 임계 구역에 들어올 수 있어야 한다.
   + 즉, 임계 구역에 들어오기 위해 ~~무한대기~~를 해서는 안된다.

### 공유 자원과 임계 구역
> 동시에 접근해서는 안되는 자원을 가리킴
#### 공유 자원
+ 여러 프로세스 혹은 스레드가 **공유하는 자원**
+ ex) 전역 변수, 파일, 입출력장치, 보조기억장치, ...
#### 임계 구역
<img width="500" height="192" alt="Image" src="https://github.com/user-attachments/assets/c7deb851-7066-417e-82d3-4496e6c7d10b" />

+ 동시에 실행하면 문제가 발생하는 자원에 접근하는 **코드 영역**
+ ex) 총합 변수에 접근하는 코드, 잔액 변수에 접근하는 코드
+ 임계구역에 진입하고자 하면, 진입한 프로세스 이외에는 대기해야함.

#### Race Condition
+ 임계 구역에 동시에 접근하여 자원의 일관성이 깨지게되는 현상
+ Back Account Problem, Producer & Consumer Problem 모두 레이스 컨디션 사례에 해당함
<p>
<img width="464" height="304" alt="Image" src="https://github.com/user-attachments/assets/24cf4feb-1fb8-4611-bef6-18fc307e2e9a" />
<em>
고급언어로 작성된 한 줄짜리 코드라도, 저급언어로는 여러 줄로 변환될 수 있다. <br>
이때 문맥교환이 발생하면, 자원의 일관성이 깨질 수 있음
</em>
</p>

## 12-2 동기화 기법