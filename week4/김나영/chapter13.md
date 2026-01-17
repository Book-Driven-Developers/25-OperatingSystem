# 🧵 프로젝트에서 데드락을 어떻게 고민했는가

## 1️⃣ 데드락 발생 코드

### 기본 구현 코드
```java
@Transactional
public void savePurchaseNaive(PurchaseRequestDto dto, Long eventId) {
    Event event = findEventById(eventId);
    validateQuantity(dto.getQuantity());

    event.deductStock(dto.getQuantity());
    eventRepository.save(event);

    savePurchaseRecord(dto, event);
}
```

### 테스트 환경
- 재고 10개의 이벤트 생성
- 10개의 스레드를 동시에 실행하여 구매 요청

### 트랜잭션 실행 순서
1. Event 조회 (SELECT)
2. 재고 차감 (자바 객체에서 계산)
3. Event UPDATE
4. Purchase INSERT

### 문제 원인 분석
- 전형적인 **read → modify → write 패턴**
- 여러 스레드가 동시에
  - 동일한 Event row에 UPDATE 시도
  - Purchase INSERT 과정에서 FK / Index 락 획득
- 트랜잭션마다 락 획득 순서가 달라지며 **Deadlock 발생**

---

## 2️⃣ Atomic Update — DB에 책임 위임

### 접근 전략
- 조회 → 계산 → 업데이트 과정을 **단일 SQL**로 처리
- 락을 애플리케이션이 아닌 **DB 레벨**에서만 획득하도록 변경

### SQL 예시
```sql
UPDATE event
SET remaining_stock = remaining_stock - :qty
WHERE id = :eventId
  AND remaining_stock >= :qty
```

### 효과
- Deadlock 제거
- 재고 정합성 보장
- 구현 단순

### 한계
- 복잡한 도메인 정책 (유저 제한, 조건 분기) 표현 어려움
- 비즈니스 로직 확장성 낮음

---

## 3️⃣ Optimistic Lock + Retry — 낙관적 접근

### 방식
- `@Version` 기반 낙관적 락
- 충돌 시 Exception 발생 → Retry 로직 처리

### 동작 흐름
1. Event 조회 (version 포함)
2. 유저 구매 한도 등 도메인 정책 검증
3. 재고 차감
   - UPDATE 시  
     `WHERE id = ? AND version = ?`
   - 동일 version으로 동시에 UPDATE 시 한 쪽만 성공
4. Purchase 엔티티 저장

### 특징
- DB 락 없음 → 병목 최소화
- 충돌을 version으로 감지
- 실패를 애플리케이션 레벨에서 제어 가능

---

## 최종 선택: **Optimistic Lock + Retry**

#### 선택 이유
1. 성능이 가장 우수
2. 비관적 락 대비 Deadlock 및 병목 없음
3. Atomic Update보다 복잡한 비즈니스 정책 구현 가능
4. 실제 운영 서비스에서 가장 널리 사용되는 구조

> 동시성 충돌을 막기보다  
> **허용하고 제어하는 방향**을 선택

## 데드락 발생 원인 (추가)
### 작성한 테스트코드
데드락 출력해 보는 부분은 AI로 작성했습니다.
```java
    @Test
    void 동시에_여러_구매_요청_보내보기() throws Exception {
        Event event = Event.builder()
                .name("동시성 테스트 이벤트")
                .totalStock(10)
                .remainingStock(10)
                .build();
        Event savedEvent = eventRepository.save(event);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // 참고: 현재 트랜잭션 격리수준/DB 상태 확인
        try {
            String isolation = jdbcTemplate.queryForObject("SELECT @@transaction_isolation", String.class);
            System.out.println("DB transaction_isolation = " + isolation);
        } catch (Exception ignore) {
        }

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1); // 동시에 시작시키기 위한 래치
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 끝나는 거 기다리기 위한 래치

        for (int i = 0; i < threadCount; i++) {
            int idx = i;
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    PurchaseRequestDto dto = new PurchaseRequestDto();
                    dto.setUserName("user-" + idx);
                    dto.setQuantity(1);

                    // 실제 구매 호출
                    purchaseService.savePurchaseNaive(dto, savedEvent.getId());
                } catch (Exception e) {
                    System.out.println("스레드 예외: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시에 출발
        startLatch.countDown();
        // 전부 끝날 때까지 대기
        doneLatch.await();

        // 데드락 원인을 DB가 기록한 그대로 확인 (InnoDB Status)
        try {
            String innodbStatus = jdbcTemplate.queryForObject("SHOW ENGINE INNODB STATUS", (rs, rowNum) -> rs.getString("Status"));
            if (innodbStatus != null) {
                int start = innodbStatus.indexOf("LATEST DETECTED DEADLOCK");
                if (start >= 0) {
                    int end = Math.min(innodbStatus.length(), start + 8000);
                    System.out.println("\n===== INNODB LATEST DETECTED DEADLOCK (first 8k chars) =====\n" + innodbStatus.substring(start, end));
                } else {
                    // 데드락이 아주 최근이 아니면 섹션이 없을 수 있음. 대신 전체에서 'DEADLOCK' 키워드 주변을 찾는다.
                    int any = innodbStatus.indexOf("DEADLOCK");
                    if (any >= 0) {
                        int from = Math.max(0, any - 2000);
                        int to = Math.min(innodbStatus.length(), any + 8000);
                        System.out.println("\n===== INNODB STATUS around 'DEADLOCK' =====\n" + innodbStatus.substring(from, to));
                    } else {
                        System.out.println("\n(INNODB STATUS에 DEADLOCK 관련 키워드가 없음)\n");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("INNODB STATUS 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }

        // when: DB 상태 다시 확인
        long purchaseCount = purchaseRepository.countByEventId(savedEvent.getId());
        Event afterEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();

        System.out.println("생성된 Purchase 개수 = " + purchaseCount);
        System.out.println("남은 재고 = " + afterEvent.getRemainingStock());

        assertThat(purchaseCount).isEqualTo(10); // 10명이 다 성공했는지
        assertThat(afterEvent.getRemainingStock()).isEqualTo(0); // 현재 구현 기준
        executorService.shutdown();
    }

```
### 발생 로그
```
===== INNODB LATEST DETECTED DEADLOCK (first 8k chars) =====
LATEST DETECTED DEADLOCK
------------------------
2026-01-16 17:07:46 0x16dce3000
*** (1) TRANSACTION:
TRANSACTION 4124350, ACTIVE 0 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 2 row lock(s), undo log entries 1
MySQL thread id 82, OS thread handle 6154612736, query id 1382 localhost 127.0.0.1 root updating
update event set created_at='2026-01-16 17:07:46.019113',name='동시성 테스트 이벤트',remaining_stock=9,total_stock=10 where id=55

*** (1) HOLDS THE LOCK(S):
RECORD LOCKS space id 1010 page no 4 n bits 88 index PRIMARY of table `ticketdb`.`event` trx id 4124350 lock mode S locks rec but not gap
Record lock, heap no 16 PHYSICAL RECORD: n_fields 8; compact format; info bits 64
 0: len 8; hex 8000000000000037; asc        7;;
 1: len 6; hex 0000003eeeb7; asc    >  ;;
 2: len 7; hex 81000000b70110; asc        ;;
 3: len 8; hex 99b8e111ee004aa9; asc       J ;;
 4: len 29; hex eb8f99ec8b9cec84b120ed858cec8aa4ed8ab820ec9db4ebb2a4ed8ab8; asc                              ;;
 5: len 4; hex 8000000a; asc     ;;
 6: len 4; hex 8000000a; asc     ;;
 7: SQL NULL;


*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 1010 page no 4 n bits 88 index PRIMARY of table `ticketdb`.`event` trx id 4124350 lock_mode X locks rec but not gap waiting
Record lock, heap no 16 PHYSICAL RECORD: n_fields 8; compact format; info bits 64
 0: len 8; hex 8000000000000037; asc        7;;
 1: len 6; hex 0000003eeeb7; asc    >  ;;
 2: len 7; hex 81000000b70110; asc        ;;
 3: len 8; hex 99b8e111ee004aa9; asc       J ;;
 4: len 29; hex eb8f99ec8b9cec84b120ed858cec8aa4ed8ab820ec9db4ebb2a4ed8ab8; asc                              ;;
 5: len 4; hex 8000000a; asc     ;;
 6: len 4; hex 8000000a; asc     ;;
 7: SQL NULL;


*** (2) TRANSACTION:
TRANSACTION 4124348, ACTIVE 0 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 5 lock struct(s), heap size 1128, 2 row lock(s), undo log entries 1
MySQL thread id 81, OS thread handle 6152384512, query id 1383 localhost 127.0.0.1 root updating
update event set created_at='2026-01-16 17:07:46.019113',name='동시성 테스트 이벤트',remaining_stock=9,total_stock=10 where id=55

*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 1010 page no 4 n bits 88 index PRIMARY of table `ticketdb`.`event` trx id 4124348 lock mode S locks rec but not gap
Record lock, heap no 16 PHYSICAL RECORD: n_fields 8; compact format; info bits 64
 0: len 8; hex 8000000000000037; asc        7;;
 1: len 6; hex 0000003eeeb7; asc    >  ;;
 2: len 7; hex 81000000b70110; asc        ;;
 3: len 8; hex 99b8e111ee004aa9; asc       J ;;
 4: len 29; hex eb8f99ec8b9cec84b120ed858cec8aa4ed8ab820ec9db4ebb2a4ed8ab8; asc                              ;;
 5: len 4; hex 8000000a; asc     ;;
 6: len 4; hex 8000000a; asc     ;;
 7: SQL NULL;


*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 1010 page no 4 n bits 88 index PRIMARY of table `ticketdb`.`event` trx id 4124348 lock_mode X locks rec but not gap waiting
Record lock, heap no 16 PHYSICAL RECORD: n_fields 8; compact format; info bits 64
 0: len 8; hex 8000000000000037; asc        7;;
 1: len 6; hex 0000003eeeb7; asc    >  ;;
 2: len 7; hex 81000000b70110; asc        ;;
 3: len 8; hex 99b8e111ee004aa9; asc       J ;;
 4: len 29; hex eb8f99ec8b9cec84b120ed858cec8aa4ed8ab820ec9db4ebb2a4ed8ab8; asc                              ;;
 5: len 4; hex 8000000a; asc     ;;
 6: len 4; hex 8000000a; asc     ;;
 7: SQL NULL;
```

### 상황
- MySQL(InnoDB), 격리수준: **REPEATABLE-READ**
- 여러 스레드가 **같은 event(id=55)** 를 동시에 구매 처리

### 코드 흐름
1) `findEventById(eventId)`  
2) `eventRepository.save(event)` → `UPDATE event ... WHERE id=?` 실행  
3) `purchaseRepository.save(purchase)` → `INSERT purchase ... (event_id=?)`

-> 하지만 실제로 로그에서 SELECT -> INSERT -> UPDATE 순서로 SQL문이 실행되었습니다....

---

### FK(외래키)로 인해 S락이 잡히는 이유
- `purchase.event_id` → `event.id` 로 **외래키(FK)** 가 설정되어 있음
- InnoDB는 `purchase INSERT` 시 다음을 보장해야 함
  - 참조하는 부모 row(`event.id = ?`)가 **실제로 존재하는지**
  - 트랜잭션 중에 **삭제되거나 변경되지 않는지**
- 이를 위해 `purchase INSERT` 시점에  
  **부모 테이블 `event`의 해당 row에 S(Shared) 레코드 락을 획득**

---

### InnoDB 데드락 리포트 핵심
- 트랜잭션 (1), (2) 모두 **같은 row (`event` PK id=55)** 를 업데이트하려고 함
- 두 트랜잭션 모두 이미 해당 row에 **S(Shared) 레코드 락**을 보유  
  - (원인: `purchase INSERT` 시 FK 체크)
  - `HOLDS THE LOCK(S): lock mode S locks rec but not gap`
- `UPDATE event`를 수행하기 위해  
  **X(Exclusive) 락으로 승격(S → X)** 을 시도
- 서로가 가진 S락 때문에 X락으로 승격 불가  
  - `WAITING FOR THIS LOCK: lock_mode X ... waiting`

---

### 데드락 구조
- 트랜잭션 A  
  → “트랜잭션 B가 S락을 풀기를 대기”
- 트랜잭션 B  
  → “트랜잭션 A가 S락을 풀기를 대기”

-> **원형 대기(cycle)** 가 형성되어 데드락 발생  
-> DB가 한 쪽을 강제 롤백  
- `WE ROLL BACK TRANSACTION (2)`

---

### 결론
- 데드락의 직접적인 발생 지점은 `event UPDATE`
- 하지만 **근본 원인은 FK 제약으로 인해 `purchase INSERT`가 먼저 `event` row에 S락을 잡는 구조**
- 이후 동일 row에 대해 **S → X 락 승격이 동시에 발생하면서 데드락이 발생한 케이스임**


# 실무/프로젝트에서 교착상태가 나타나는 경우
## 언제 발생하는가
- 여러 트랜잭션이 **같은 자원(Row, Index, FK)** 을 동시에 수정할 때
- 하나의 트랜잭션에서 **여러 테이블/인덱스** 를 함께 건드릴 때
- 트래픽이 몰리는 **피크 타임 / 이벤트성 요청** 상황