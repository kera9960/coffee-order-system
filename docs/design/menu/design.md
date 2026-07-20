# 메뉴 기능 설계

## 목적

메뉴 기능은 메뉴의 현재 이름, 가격, 판매 상태를 관리하고,
정책에 맞는 조회 결과를 API로 제공한다.

비즈니스 규칙은 `../../policy/menu-sales.md`,
API 계약은 `../../api/README.md`,
DB 구조는 `../../db/README.md`를 따른다.

## 구현 범위

- 메뉴 등록
- 판매 중 메뉴 목록 조회
- 메뉴 단건 조회
- 메뉴 이름과 가격 수정
- 메뉴 판매 상태 변경

메뉴 삭제, 재고 관리, 할인, 이미지 관리는 현재 범위에 포함하지 않는다.

## API 매핑

- `POST /api/menus`: 메뉴 등록
- `GET /api/menus`: 판매 중 메뉴 목록 조회
- `GET /api/menus/{menuId}`: 메뉴 단건 조회
- `PUT /api/menus/{menuId}`: 메뉴 이름과 가격 수정
- `PATCH /api/menus/{menuId}/status`: 메뉴 판매 상태 변경

## 패키지와 주요 클래스

기준 패키지는 `com.example.coffeeordersystem`이다.

```text
domain/menu/controller/MenuController
domain/menu/service/MenuService
domain/menu/repository/MenuRepository
domain/menu/entity/Menu
domain/menu/entity/MenuStatus
domain/menu/dto/CreateMenuRequest
domain/menu/dto/UpdateMenuRequest
domain/menu/dto/ChangeMenuStatusRequest
domain/menu/dto/MenuResponse
```

공통 예외 응답과 에러 코드는 `common` 패키지의 전역 예외 처리 방식을 사용한다.
기능별 예외 클래스를 둘 경우 `domain/menu/exception`에 둔다.

## 계층과 객체별 책임

- `MenuController`
  - `/api/menus` 하위 요청을 받는다.
  - 요청 DTO의 형식 검증 결과를 사용한다.
  - `MenuService` 결과를 API 응답 DTO로 반환한다.
- `MenuService`
  - 메뉴 등록, 수정, 상태 변경의 처리 순서를 관리한다.
  - 메뉴 존재 여부, 이름 중복, 판매 상태 전환 요청을 검증한다.
  - 저장 작업의 트랜잭션 경계를 정의한다.
- `MenuRepository`
  - 메뉴 단건 조회, 판매 중 메뉴 목록 조회, 이름 중복 확인을 담당한다.
  - 일반 목록 조회에는 `status = ON_SALE` 조건이 드러나는 메서드를 사용한다.
- `Menu`
  - 메뉴 이름, 가격, 상태를 보유한다.
  - 이름/가격 변경과 판매 상태 변경은 의미 있는 메서드로 수행한다.
- `MenuStatus`
  - `ON_SALE`, `STOPPED` 값을 표현한다.

## 핵심 처리 흐름

### 메뉴 등록

```text
요청값 형식 검증
→ 메뉴 이름 중복 확인
→ Menu 생성
→ 초기 상태를 ON_SALE로 설정
→ 저장
→ MenuResponse 반환
```

### 메뉴 정보 수정

```text
요청값 형식 검증
→ 메뉴 조회
→ 다른 메뉴와 이름 중복 여부 확인
→ 이름과 가격 교체
→ 변경 결과 반환
```

이 API는 현재 메뉴의 이름과 가격을 교체한다. 판매 상태는 변경하지 않는다.

### 메뉴 판매 상태 변경

```text
요청 상태 파싱
→ 메뉴 조회
→ 상태 변경
→ 변경 결과 반환
```

`STOPPED` 메뉴는 다시 `ON_SALE`로 변경할 수 있다.
현재 상태와 같은 상태를 요청하면 오류로 처리하지 않고 현재 메뉴 정보를 반환한다.

### 메뉴 조회

- 목록 조회는 `ON_SALE` 메뉴만 조회한다.
- 단건 조회는 `STOPPED` 메뉴도 조회할 수 있다.

## 트랜잭션 범위

- 등록, 수정, 상태 변경은 각각 하나의 쓰기 트랜잭션으로 처리한다.
- 목록 조회와 단건 조회는 필요한 경우 읽기 전용 트랜잭션으로 처리한다.
- 메뉴 수정은 기존 주문 항목의 스냅샷 데이터를 변경하지 않는다.

## 동시성 처리

- 메뉴 이름은 DB의 `UNIQUE` 제약을 최종 방어선으로 둔다.
- 서비스에서는 사전 중복 검사를 수행하되, 동시 요청으로 DB 제약 위반이 발생하면 `DUPLICATED_MENU_NAME`에 맞는 예외로 변환한다.
- 판매 상태 변경은 현재 정책상 재고나 주문 예약과 연계하지 않으므로 별도 잠금 정책을 두지 않는다.

## 검증 책임

- Request DTO: 이름 공백 여부, 가격 양수, 상태 값 형식
- Service: 메뉴 존재 여부, 이름 중복, API별 변경 가능 범위
- Entity: 이름/가격/상태 변경 시 도메인 불변식
- DB: 이름 유일성, 가격 양수, 상태 허용 값

## 조회 및 저장 방식

- `findById`로 단건을 조회한다.
- `findAllByStatus(ON_SALE, pageable)` 형태의 메서드로 목록을 조회한다.
- `existsByName` 또는 `existsByNameAndIdNot` 형태로 이름 중복을 확인한다.
- 등록 시 생성한 `Menu`는 `MenuRepository.save`로 저장한다.

## 예외 상황

| 상황 | API 에러 코드 |
| --- | --- |
| 메뉴 이름이 비어 있거나 올바르지 않음 | `INVALID_MENU_NAME` |
| 메뉴 가격이 0 이하임 | `INVALID_MENU_PRICE` |
| 지원하지 않는 판매 상태 | `INVALID_MENU_STATUS` |
| 메뉴가 존재하지 않음 | `MENU_NOT_FOUND` |
| 메뉴 이름 중복 | `DUPLICATED_MENU_NAME` |

## 테스트 포인트

- 메뉴 등록 시 상태가 `ON_SALE`로 저장된다.
- 중복 이름으로 등록하거나 수정하면 실패한다.
- 메뉴 수정은 판매 상태를 변경하지 않는다.
- `STOPPED` 메뉴는 목록 조회에서 제외된다.
- `STOPPED` 메뉴도 단건 조회에서는 반환된다.
- `STOPPED → ON_SALE` 판매 재개가 가능하다.
- 동일 상태 변경 요청은 실패하지 않고 현재 상태를 반환한다.

## 중요한 설계 결정

- 신규 메뉴의 초기 상태는 API 요청으로 받지 않고 서버에서 `ON_SALE`로 설정한다.
- 판매 중지 메뉴는 삭제하지 않고 상태만 변경하여 과거 주문 참조를 유지한다.
- 판매 상태 변경 API를 분리하여 이름/가격 수정과 판매 가능 여부 변경의 책임을 나눈다.
