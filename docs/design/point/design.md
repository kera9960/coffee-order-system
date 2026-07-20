# 포인트 기능 설계

## 목적

포인트 기능은 고객의 현재 포인트 잔액과 충전/사용 이력을 일관되게 관리한다.

비즈니스 규칙은 `../../policy/point.md`,
API 계약은 `../../api/README.md`,
DB 구조는 `../../db/README.md`를 따른다.

## 구현 범위

- 고객 포인트 잔액 조회
- 포인트 충전
- 포인트 거래 이력 조회
- 주문 결제에서 사용할 포인트 차감 처리

환불, 주문 취소에 따른 포인트 복원, 외부 결제 연동은 현재 범위에 포함하지 않는다.

## API 매핑

- `GET /api/customers/{customerId}/points/balance`: 고객 포인트 잔액 조회
- `POST /api/customers/{customerId}/points/charge`: 포인트 충전
- `GET /api/customers/{customerId}/points/transactions`: 포인트 거래 이력 조회

## 패키지와 주요 클래스

기준 패키지는 `com.example.coffeeordersystem`이다.

```text
domain/customer/entity/Customer
domain/customer/repository/CustomerRepository
domain/point/controller/PointController
domain/point/service/PointService
domain/point/repository/PointTransactionRepository
domain/point/entity/PointTransaction
domain/point/entity/PointTransactionType
domain/point/dto/ChargePointRequest
domain/point/dto/PointBalanceResponse
domain/point/dto/PointTransactionResponse
```

`CustomerRepository`에는 포인트 변경용 `PESSIMISTIC_WRITE` 조회 메서드를 둔다.

## 계층과 객체별 책임

- `PointController`
  - 포인트 잔액, 충전, 거래 이력 API를 처리한다.
  - 요청 DTO 검증과 응답 DTO 반환을 담당한다.
- `PointService`
  - 고객 조회, 포인트 충전, 이력 저장의 처리 순서를 관리한다.
  - 포인트 변경 작업의 트랜잭션 경계를 정의한다.
  - 주문 서비스가 사용할 포인트 차감 메서드를 제공한다.
- `CustomerRepository`
  - 일반 고객 조회와 포인트 변경용 잠금 조회를 구분한다.
- `PointTransactionRepository`
  - 포인트 거래 저장과 고객별 거래 이력 조회를 담당한다.
- `Customer`
  - 현재 포인트 잔액을 보유한다.
  - `chargePoint`, `usePoint` 같은 도메인 메서드로 잔액을 변경한다.
- `PointTransaction`
  - 거래 유형, 증감량, 거래 후 잔액, 연결 주문을 기록한다.

## 핵심 처리 흐름

### 포인트 충전

```text
충전 금액 검증
→ Customer를 PESSIMISTIC_WRITE로 조회
→ 잔액 증가
→ CHARGE 거래 이력 생성
→ 거래 이력 저장
→ 충전 결과 반환
```

충전 거래는 주문과 연결하지 않는다.

### 포인트 차감

주문 결제 과정에서 `PointService`는 이미 시작된 주문 트랜잭션 안에서 호출된다.

```text
Customer를 PESSIMISTIC_WRITE로 조회
→ 잔액 충분 여부 확인
→ 잔액 차감
→ USE 거래 이력 생성
→ 거래 이력 저장
```

`USE` 거래는 결제한 주문과 연결한다.

### 거래 이력 조회

```text
고객 존재 확인
→ 거래 유형 필터 검증
→ 고객별 거래 이력 페이지 조회
→ 응답 반환
```

## 트랜잭션 범위

- 포인트 충전은 고객 잔액 증가와 `CHARGE` 이력 저장을 하나의 쓰기 트랜잭션으로 처리한다.
- 주문 결제의 포인트 차감과 `USE` 이력 저장은 주문 생성 트랜잭션에 포함된다.
- 잔액 조회와 이력 조회는 필요한 경우 읽기 전용 트랜잭션으로 처리한다.

## 동시성 처리

- 동일 고객의 포인트를 변경하는 충전과 주문 결제는 `Customer` 행을 `PESSIMISTIC_WRITE`로 조회한 뒤 처리한다.
- 잠금은 잔액 검증, 잔액 변경, 거래 이력 생성이 끝날 때까지 같은 트랜잭션에서 유지한다.
- 이 방식으로 동시에 들어온 결제 요청이 같은 잔액을 기준으로 중복 차감하는 상황을 방지한다.

## 검증 책임

- Request DTO: 충전 금액이 0보다 큰지 검증
- Service: 고객 존재 여부, 거래 유형 필터, 잔액 부족 여부
- Entity: 잔액이 음수가 되지 않도록 포인트 증감 검증
- DB: `point_balance >= 0`, 거래 유형, 금액 부호, `balance_after >= 0`

## 조회 및 저장 방식

- 포인트 변경 시 `CustomerRepository.findByIdForUpdate` 형태의 잠금 조회를 사용한다.
- 일반 조회에는 잠금 없는 `findById`를 사용한다.
- 거래 이력은 `PointTransactionRepository.save`로 저장한다.
- 고객별 거래 이력은 `customer_id`, `transacted_at` 인덱스를 활용할 수 있는 조건으로 조회한다.

## 예외 상황

| 상황 | API 에러 코드 |
| --- | --- |
| 고객이 존재하지 않음 | `CUSTOMER_NOT_FOUND` |
| 충전 금액이 0 이하임 | `INVALID_POINT_AMOUNT` |
| 지원하지 않는 거래 유형 | `INVALID_POINT_TRANSACTION_TYPE` |
| 주문 금액보다 잔액이 부족함 | `INSUFFICIENT_POINTS` |
| 페이지 요청이 올바르지 않음 | `INVALID_PAGE_REQUEST` |

## 테스트 포인트

- 포인트 충전 시 고객 잔액과 `CHARGE` 이력이 함께 저장된다.
- 충전 금액이 0 이하이면 잔액과 이력이 변경되지 않는다.
- 잔액이 부족하면 포인트 차감이 실패한다.
- 주문 결제 차감 시 `USE` 이력은 주문과 연결된다.
- 동일 고객의 동시 포인트 변경은 `PESSIMISTIC_WRITE` 잠금 조회를 사용한다.
- 거래 이력 조회는 유형 필터와 페이지 정렬을 따른다.

## 중요한 설계 결정

- 현재 잔액은 `Customer`에 저장하고 모든 증감 근거는 `PointTransaction`에 남긴다.
- 포인트 변경 작업은 `Customer` 행 잠금으로 직렬화한다.
- 주문 결제용 포인트 차감은 독립 트랜잭션을 열지 않고 주문 생성 트랜잭션에 참여한다.
