# 인기 메뉴 기능 설계

## 목적

인기 메뉴 기능은 최근 7일 동안의 완료 주문을 기준으로
판매 중인 메뉴의 인기 순위를 조회한다.

비즈니스 규칙은 `../../policy/popular-menu.md`,
메뉴 판매 규칙은 `../../policy/menu-sales.md`,
API 계약은 `../../api/README.md`,
DB 구조는 `../../db/README.md`를 따른다.

## 구현 범위

- 인기 메뉴 조회 API
- 최근 7일 조회 기간 계산
- 현재 판매 중인 메뉴 대상 집계
- 완료 주문 기준 주문 건수와 최근 주문 시각 계산
- 최대 3개 결과 반환

인기 메뉴 캐시, 배치 집계, 통계 테이블 생성은 현재 범위에 포함하지 않는다.

## 패키지와 주요 클래스

인기 메뉴는 별도 저장 엔티티 없이 조회 기능으로 구현한다.
기준 패키지는 `com.example.coffeeordersystem`이다.

```text
domain/menu/controller/MenuController
domain/menu/service/PopularMenuService
domain/menu/repository/PopularMenuQueryRepository
domain/menu/dto/PopularMenuResponse
domain/menu/dto/PopularMenuRow
```

`PopularMenuRow`는 DB 집계 결과를 서비스로 전달하기 위한 내부 조회 DTO 또는 projection으로 둔다.

## 계층과 객체별 책임

- `MenuController`
  - `GET /api/menus/popular` 요청을 받는다.
  - `PopularMenuService` 결과를 API 응답으로 반환한다.
- `PopularMenuService`
  - 현재 시각을 기준으로 최근 7일 조회 기간을 계산한다.
  - 조회 repository에 집계 조건을 전달한다.
  - 조회 결과를 `PopularMenuResponse`로 변환한다.
- `PopularMenuQueryRepository`
  - `menus`, `order_items`, `orders`를 조합해 인기 메뉴를 집계한다.
  - `menus.status = ON_SALE` 필터를 집계 전에 적용한다.
- `PopularMenuResponse`
  - API 응답 필드인 `menuId`, `menuName`, `orderCount`만 포함한다.

## 핵심 처리 흐름

```text
현재 시각 확인
→ 최근 7일 조회 기간 계산
→ ON_SALE 메뉴를 먼저 선별
→ 최근 7일 안의 COMPLETED 주문과 주문 항목 조인
→ 메뉴별 포함 주문 건수와 최근 주문 시각 계산
→ 정책 정렬 기준 적용
→ 최대 3개 반환
```

조회 조건을 만족하는 메뉴가 없으면 빈 목록을 반환한다.

## 조회 기간 계산

- 조회 시점을 종료 시각으로 사용한다.
- 시작 시각은 조회 시점에서 7일을 뺀 시각으로 계산한다.
- 쿼리 조건은 시작 시각 이상, 조회 시점 이하 범위를 사용한다.

## 트랜잭션 범위

- 인기 메뉴 조회는 저장 작업이 없으므로 읽기 전용 트랜잭션으로 처리한다.
- 조회 중 메뉴 상태나 주문이 변경될 수 있지만, 현재 범위에서는 조회 시점의 DB 일관성 수준을 따른다.
- 별도 캐시나 스냅샷 테이블을 사용하지 않는다.

## 동시성 처리

- 인기 메뉴 조회는 데이터 변경을 하지 않으므로 별도 잠금을 사용하지 않는다.
- 주문 생성과 메뉴 상태 변경이 동시에 발생하는 경우 DB가 제공하는 일반 읽기 일관성을 따른다.
- 강한 실시간 순위 일관성이 필요해지면 별도 정책과 캐시/집계 설계가 필요하다.

## 검증 책임

- Controller: 쿼리 파라미터가 없음을 API 계약과 맞춘다.
- Service: 최근 7일 조회 기간 계산 기준과 현재 시각 주입 방식을 관리한다.
- Repository: `ON_SALE`, `COMPLETED`, 조회 기간 조건을 정확히 적용한다.
- DB: 주문, 주문 항목, 메뉴 관계와 인덱스로 조회를 지원한다.

## 조회 방식

집계 쿼리는 다음 조건을 포함한다.

```text
menus.status = 'ON_SALE'
orders.status = 'COMPLETED'
orders.ordered_at >= 조회기간 시작시각
orders.ordered_at <= 조회시점
```

메뉴별 집계 값은 다음과 같이 계산한다.

- `orderCount`: 해당 메뉴가 포함된 주문 수
- `lastOrderedAt`: 해당 메뉴가 포함된 완료 주문 중 가장 최근 주문 시각

정렬은 `orderCount desc`, `lastOrderedAt desc`, `menuId asc` 순서로 적용한다.

## 예외 상황

인기 메뉴 조회는 입력값이 없으므로 정책상 별도 비즈니스 예외를 정의하지 않는다.
조건을 만족하는 데이터가 없으면 `200 OK`와 빈 목록을 반환한다.

## 테스트 포인트

- 조회 시점을 기준으로 최근 7일 조회 기간이 올바르게 계산된다.
- `COMPLETED` 주문만 집계한다.
- `STOPPED` 메뉴는 주문 건수가 많아도 결과에서 제외된다.
- 판매 중 메뉴를 먼저 선별한 뒤 집계한다.
- 한 주문의 동일 메뉴 수량은 주문 건수 1건으로 계산된다.
- 서로 다른 메뉴가 한 주문에 포함되면 각 메뉴의 주문 건수가 1건씩 증가한다.
- 정렬 기준과 최대 3개 제한을 따른다.
- 조건을 만족하는 메뉴가 없으면 빈 목록을 반환한다.

## 중요한 설계 결정

- 인기 메뉴는 현재 범위에서 저장하지 않고 조회 시점에 집계한다.
- 정책의 “판매 중 메뉴 선별 후 집계” 의미를 쿼리 조건으로 직접 반영한다.
- 향후 트래픽이나 성능 요구가 커지면 캐시 또는 통계 테이블 설계를 별도 문서로 추가한다.

## 확인 필요 사항

- 현재 정책에는 인기 메뉴 조회 실패에 대한 별도 에러 코드가 없다. 입력 파라미터가 없는 조회 API이므로 현 설계에서는 추가 에러 코드를 만들지 않는다.
