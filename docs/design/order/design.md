# 주문 기능 설계

## 목적

주문 기능은 고객이 판매 중인 메뉴를 포인트로 결제해 주문하도록 처리하고,
주문 당시 메뉴 정보를 보존한다.

비즈니스 규칙은 `../../policy/order.md`,
포인트 규칙은 `../../policy/point.md`,
메뉴 판매 규칙은 `../../policy/menu-sales.md`,
API 계약은 `../../api/README.md`,
DB 구조는 `../../db/README.md`를 따른다.

## 구현 범위

- 주문 생성
- 고객별 주문 목록 조회
- 주문 상세 조회
- 주문 생성 시 포인트 결제
- 주문 당시 메뉴 이름과 가격 저장

단일 메뉴 주문은 `items`에 주문 항목을 1개만 전달하는 방식으로 처리한다.
여러 메뉴 주문은 원본 요구사항을 확장한 기능이며 단일 메뉴 주문을 막지 않는다.

주문 취소, 환불, 배송/포장 상태, 외부 결제는 현재 범위에 포함하지 않는다.

## API 매핑

- `POST /api/customers/{customerId}/orders`: 주문 생성
- `GET /api/customers/{customerId}/orders`: 고객별 주문 목록 조회
- `GET /api/customers/{customerId}/orders/{orderId}`: 주문 상세 조회

## 패키지와 주요 클래스

기준 패키지는 `com.example.coffeeordersystem`이다.

```text
domain/order/controller/OrderController
domain/order/service/OrderService
domain/order/repository/OrderRepository
domain/order/entity/Order
domain/order/entity/OrderItem
domain/order/entity/OrderStatus
domain/order/dto/CreateOrderRequest
domain/order/dto/OrderItemRequest
domain/order/dto/OrderResponse
domain/order/dto/OrderSummaryResponse
domain/order/dto/OrderDetailResponse
domain/menu/repository/MenuRepository
domain/point/service/PointService
```

`OrderService`는 주문 처리 순서를 조율하고, 포인트 변경은 `PointService`의 차감 책임을 사용한다.

## 계층과 객체별 책임

- `OrderController`
  - `/api/customers/{customerId}/orders` 하위 API를 처리한다.
  - 요청 DTO 검증과 응답 DTO 반환을 담당한다.
- `OrderService`
  - 주문 항목 검증, 메뉴 조회, 금액 계산, 주문 저장, 포인트 차감을 하나의 흐름으로 관리한다.
  - 주문 생성 트랜잭션 경계를 정의한다.
  - 고객별 주문 조회에서 고객 존재 여부와 주문 소유 관계를 확인한다.
- `OrderRepository`
  - 주문 저장, 고객별 주문 목록 조회, 고객 ID와 주문 ID 기반 상세 조회를 담당한다.
- `MenuRepository`
  - 주문 요청에 포함된 메뉴를 조회한다.
- `PointService`
  - 주문 트랜잭션 안에서 고객 포인트를 잠금 조회하고 차감 이력을 저장한다.
- `Order`
  - 주문 대표 정보와 주문 항목을 가진다.
  - 현재 생성되는 주문 상태는 `COMPLETED`이다.
- `OrderItem`
  - 주문 당시 메뉴 ID, 이름, 가격, 수량, 항목 금액을 보존한다.

## 핵심 처리 흐름

### 주문 생성

```text
요청 항목 존재 여부 검증
→ 동일 메뉴 ID 중복 검증
→ 주문 수량 검증
→ PointService를 통해 고객을 PESSIMISTIC_WRITE로 조회
→ 메뉴 일괄 조회
→ 메뉴 존재 여부 확인
→ 메뉴 판매 상태 확인
→ 주문 항목 금액과 주문 총액 계산
→ 주문과 주문 항목 생성
→ 주문 저장
→ 포인트 차감과 USE 이력 저장
→ 주문 응답 반환
```

같은 주문 요청에 동일한 메뉴 ID가 중복되면 수량을 합산하지 않고
`DUPLICATED_ORDER_MENU` 실패로 처리한다.

### 주문 목록 조회

```text
고객 존재 확인
→ 고객 ID 기준 주문 페이지 조회
→ 요약 응답 반환
```

목록 응답의 `itemCount`는 주문 항목 수를 의미한다.

### 주문 상세 조회

```text
고객 존재 확인
→ 고객 ID와 주문 ID로 주문 조회
→ 주문 항목과 포인트 사용 이력 조회
→ 상세 응답 반환
```

상세 응답은 현재 메뉴 정보가 아니라 `OrderItem`에 저장된 주문 당시 정보를 사용한다.

## 트랜잭션 범위

- 주문 생성은 주문, 주문 항목, 포인트 차감, 포인트 사용 이력 저장을 하나의 쓰기 트랜잭션으로 처리한다.
- 주문 생성 중 하나라도 실패하면 주문 데이터와 포인트 변경은 모두 롤백되어야 한다.
- 주문 목록과 상세 조회는 필요한 경우 읽기 전용 트랜잭션으로 처리한다.

## 동시성 처리

- 주문 결제는 포인트 잔액을 변경하므로 `Customer` 행을 `PESSIMISTIC_WRITE`로 조회한다.
- 잠금은 잔액 검증, 주문 저장, 포인트 차감, 사용 이력 저장이 끝날 때까지 유지한다.
- 메뉴 판매 상태는 주문 처리 시점에 조회한 현재 상태를 기준으로 검증한다.

## 검증 책임

- Request DTO: 주문 항목 목록 존재, 메뉴 ID 형식, 수량 1 이상
- Service: 동일 메뉴 ID 중복, 고객 존재, 메뉴 존재, 판매 상태, 잔액 부족, 주문 소유 관계
- Entity: 주문 총액과 항목 금액 생성 시 기본 불변식
- DB: 주문/주문항목/포인트 거래 관계와 금액 제약조건

## 조회 및 저장 방식

- 메뉴는 요청 메뉴 ID 목록으로 한 번에 조회한다.
- 조회된 메뉴 수와 요청 메뉴 ID 수를 비교해 누락 메뉴를 판단한다.
- 주문 항목은 조회된 메뉴의 현재 이름과 가격으로 스냅샷을 만든다.
- `Order`와 `OrderItem`은 같은 트랜잭션에서 저장한다.
- 포인트 사용 거래는 저장된 주문과 연결한다.

## 예외 상황

| 상황 | API 에러 코드 |
| --- | --- |
| 고객이 존재하지 않음 | `CUSTOMER_NOT_FOUND` |
| 주문 항목이 비어 있음 | `EMPTY_ORDER_ITEMS` |
| 주문 수량이 1보다 작음 | `INVALID_ORDER_QUANTITY` |
| 동일 메뉴 ID가 중복됨 | `DUPLICATED_ORDER_MENU` |
| 메뉴가 존재하지 않음 | `MENU_NOT_FOUND` |
| 판매 중이 아닌 메뉴가 포함됨 | `MENU_NOT_ON_SALE` |
| 포인트 잔액이 부족함 | `INSUFFICIENT_POINTS` |
| 주문이 없거나 해당 고객의 주문이 아님 | `ORDER_NOT_FOUND` |
| 페이지 요청이 올바르지 않음 | `INVALID_PAGE_REQUEST` |

## 테스트 포인트

- 판매 중 메뉴와 충분한 포인트가 있으면 주문, 주문 항목, `USE` 이력이 함께 저장된다.
- 단일 메뉴만 포함한 주문을 생성할 수 있다.
- 동일 메뉴 ID가 중복되면 주문 생성이 실패한다.
- 판매 중지 메뉴가 하나라도 포함되면 주문 전체가 실패한다.
- 포인트 부족 시 주문과 주문 항목이 저장되지 않는다.
- 메뉴 이름/가격 변경 후에도 주문 상세는 주문 당시 정보를 반환한다.
- 주문 상세는 다른 고객의 주문을 `ORDER_NOT_FOUND`로 처리한다.
- 주문 생성은 포인트 변경용 `PESSIMISTIC_WRITE` 잠금 조회를 사용한다.

## 중요한 설계 결정

- 주문 요청의 동일 메뉴 ID는 합산하지 않고 실패 처리한다.
- 주문 생성 시 현재 메뉴 정보를 `OrderItem`에 스냅샷으로 저장한다.
- 포인트 차감은 주문 저장과 같은 트랜잭션에서 처리해 일부 반영 상태를 방지한다.
- 현재 API 범위에서 새로 생성되는 주문은 `COMPLETED` 상태만 사용한다. `CANCELED`는 취소 기능 추가 시 별도 정책과 설계로 다룬다.
