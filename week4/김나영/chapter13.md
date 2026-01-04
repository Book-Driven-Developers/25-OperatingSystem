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


# 실무/프로젝트에서 교착상태가 나타나는 경우
## 언제 발생하는가
- 여러 트랜잭션이 **같은 자원(Row, Index, FK)** 을 동시에 수정할 때
- 하나의 트랜잭션에서 **여러 테이블/인덱스** 를 함께 건드릴 때
- 트래픽이 몰리는 **피크 타임 / 이벤트성 요청** 상황