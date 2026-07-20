# API Design

## 관련 정책

- 포인트 정책: `docs/policy/point.md`
- 메뉴 판매 정책: `docs/policy/menu-sales.md`
- 주문 정책: `docs/policy/order.md`
- 인기 메뉴 정책: `docs/policy/popular-menu.md`

이 문서에서는 요청, 응답, 상태 코드와 같은 API 계약을 정의한다. 
비즈니스 규칙은 `docs/policy/`를 따른다.

## 공통 규칙

### Base URL

```text
/api
```

### Content-Type

```http
Content-Type: application/json
Accept: application/json
```

### 성공 응답 형식

단건 응답:

```json
{
  "data": {}
}
```

고정 목록 응답:

```json
{
  "data": []
}
```

페이지 목록 응답:

```json
{
  "data": [],
  "page": {
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "sort": "orderedAt,desc"
  }
}
```

생성 응답은 `201 Created`를 사용하고, 생성된 리소스를 `data`에 담아 반환한다.

### 실패 응답 형식

```json
{
  "code": "ERROR_CODE",
  "message": "실패 사유"
}
```

### 페이지네이션

페이지네이션이 필요한 목록 API에서만 `page`, `size`, `sort` 쿼리 파라미터를 사용한다.

각 API에 Query Parameter가 명시되어 있지 않으면 페이지네이션을 적용하지 않는다.

| 파라미터 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| `page` | `number` | `0` | 0부터 시작하는 페이지 번호 |
| `size` | `number` | `20` | 페이지 크기 |
| `sort` | `string` | API별 기본값 | `필드명,방향` 형식. 예: `orderedAt,desc` |

`size`는 1 이상이어야 한다. 지원하지 않는 정렬 필드를 요청하면 `400 Bad Request`를 반환한다.

### 고객 식별

현재 회원가입, 로그인, 인증, 인가는 구현하지 않는다.

고객별 주문과 포인트 리소스는 URL의 `customerId`로 식별한다.

## 고객 API

### 고객 목록 조회

초기 더미 고객 목록을 조회한다.

```http
GET /api/customers
```

#### Query Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `page` | `number` | 아니오 | 페이지 번호. 기본값 `0` |
| `size` | `number` | 아니오 | 페이지 크기. 기본값 `20` |
| `sort` | `string` | 아니오 | 정렬 기준. 기본값 `id,asc` |

#### 성공 응답

`200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "name": "홍길동",
      "pointBalance": 12000,
      "createdAt": "2026-07-15T10:00:00",
      "updatedAt": "2026-07-15T10:00:00"
    }
  ],
  "page": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "sort": "id,asc"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_PAGE_REQUEST` | 페이지 번호, 크기, 정렬 기준이 올바르지 않음 |

### 고객 단건 조회

```http
GET /api/customers/{customerId}
```

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |

#### 성공 응답

`200 OK`

```json
{
  "data": {
    "id": 1,
    "name": "홍길동",
    "pointBalance": 12000,
    "createdAt": "2026-07-15T10:00:00",
    "updatedAt": "2026-07-15T10:00:00"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |

### 고객 포인트 잔액 조회

```http
GET /api/customers/{customerId}/points/balance
```

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |

#### 성공 응답

`200 OK`

```json
{
  "data": {
    "customerId": 1,
    "pointBalance": 12000
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |

## 포인트 API

### 포인트 충전

고객의 포인트를 충전한다.

```http
POST /api/customers/{customerId}/points/charge
```

#### 관련 정책:

- `docs/policy/point.md`

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |

#### 요청값

```json
{
  "amount": 5000
}
```

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `amount` | `number` | 예 | 충전할 포인트. 0보다 커야 함 |

#### 성공 응답

`201 Created`

```json
{
  "data": {
    "transactionId": 10,
    "customerId": 1,
    "orderId": null,
    "type": "CHARGE",
    "amount": 5000,
    "balanceAfter": 17000,
    "transactedAt": "2026-07-15T10:30:00"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_POINT_AMOUNT` | 충전 포인트가 0 이하임 |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |

### 포인트 거래 이력 조회

고객의 포인트 충전 및 사용 이력을 조회한다.

```http
GET /api/customers/{customerId}/points/transactions
```

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |

#### Query Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `type` | `string` | 아니오 | `CHARGE`, `USE` 중 하나 |
| `page` | `number` | 아니오 | 페이지 번호. 기본값 `0` |
| `size` | `number` | 아니오 | 페이지 크기. 기본값 `20` |
| `sort` | `string` | 아니오 | 정렬 기준. 기본값 `transactedAt,desc` |

#### 성공 응답

`200 OK`

```json
{
  "data": [
    {
      "id": 10,
      "customerId": 1,
      "orderId": null,
      "type": "CHARGE",
      "amount": 5000,
      "balanceAfter": 17000,
      "transactedAt": "2026-07-15T10:30:00"
    },
    {
      "id": 11,
      "customerId": 1,
      "orderId": 20,
      "type": "USE",
      "amount": -4500,
      "balanceAfter": 12500,
      "transactedAt": "2026-07-15T10:40:00"
    }
  ],
  "page": {
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1,
    "sort": "transactedAt,desc"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_POINT_TRANSACTION_TYPE` | 지원하지 않는 거래 유형 |
| `400 Bad Request` | `INVALID_PAGE_REQUEST` | 페이지 번호, 크기, 정렬 기준이 올바르지 않음 |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |

## 메뉴 API

### 메뉴 등록

새로운 메뉴를 등록한다.

등록된 메뉴의 초기 판매 상태는 `ON_SALE`이다. 판매 중지 상태로 변경하려면 메뉴 판매 상태 변경 API를 사용한다.

```http
POST /api/menus
```

#### 요청값

```json
{
  "name": "바닐라라떼",
  "price": 5500
}
```

| 이름      | 타입       | 필수 | 설명                                  |
| ------- | -------- | -- | ----------------------------------- |
| `name`  | `string` | 예  | 메뉴 이름. 공백일 수 없으며 다른 메뉴 이름과 중복될 수 없음 |
| `price` | `number` | 예  | 메뉴 가격. 0보다 커야 함                     |

#### 성공 응답

`201 Created`

```json
{
  "data": {
    "id": 3,
    "name": "바닐라라떼",
    "price": 5500,
    "status": "ON_SALE"
  }
}
```

#### 실패 응답

| 상태 코드             | code                   | 설명                    |
| ----------------- | ---------------------- | --------------------- |
| `400 Bad Request` | `INVALID_MENU_NAME`    | 메뉴 이름이 비어 있거나 올바르지 않음 |
| `400 Bad Request` | `INVALID_MENU_PRICE`   | 메뉴 가격이 0 이하임          |
| `409 Conflict`    | `DUPLICATED_MENU_NAME` | 같은 이름의 메뉴가 이미 존재함     |

### 메뉴 정보 수정

메뉴의 현재 이름과 가격을 수정한다.

판매 상태는 이 API에서 변경하지 않는다. 판매 상태 변경은 별도의 메뉴 판매 상태 변경 API를 사용한다.

메뉴 정보가 변경되어도 기존 주문에 저장된 주문 당시 메뉴 이름과 가격은 변경하지 않는다.

```http
PUT /api/menus/{menuId}
```

#### Path Parameters

| 이름       | 타입       | 필수 | 설명        |
| -------- | -------- | -- | --------- |
| `menuId` | `number` | 예  | 수정할 메뉴 ID |

#### 요청값

```json
{
  "name": "아이스 바닐라라떼",
  "price": 5800
}
```

| 이름      | 타입       | 필수 | 설명                                      |
| ------- | -------- | -- | --------------------------------------- |
| `name`  | `string` | 예  | 변경할 메뉴 이름. 공백일 수 없으며 다른 메뉴 이름과 중복될 수 없음 |
| `price` | `number` | 예  | 변경할 메뉴 가격. 0보다 커야 함                     |

이 API는 메뉴의 현재 정보를 교체하므로 `name`과 `price`를 모두 전달해야 한다.

#### 성공 응답

`200 OK`

```json
{
  "data": {
    "id": 3,
    "name": "아이스 바닐라라떼",
    "price": 5800,
    "status": "ON_SALE"
  }
}
```

#### 실패 응답

| 상태 코드             | code                   | 설명                    |
| ----------------- | ---------------------- | --------------------- |
| `400 Bad Request` | `INVALID_MENU_NAME`    | 메뉴 이름이 비어 있거나 올바르지 않음 |
| `400 Bad Request` | `INVALID_MENU_PRICE`   | 메뉴 가격이 0 이하임          |
| `404 Not Found`   | `MENU_NOT_FOUND`       | 메뉴가 존재하지 않음           |
| `409 Conflict`    | `DUPLICATED_MENU_NAME` | 다른 메뉴가 같은 이름을 사용하고 있음 |

### 메뉴 판매 상태 변경

메뉴의 현재 판매 상태를 변경한다.

판매 중지된 메뉴는 DB에서 삭제하지 않는다. `STOPPED` 메뉴는 일반 메뉴 목록과 인기 메뉴 조회 결과에서 제외되며 신규 주문에 포함할 수 없다.

판매가 중지된 메뉴를 다시 `ON_SALE` 상태로 변경할 수 있다.

```http
PATCH /api/menus/{menuId}/status
```

#### Path Parameters

| 이름       | 타입       | 필수 | 설명               |
| -------- | -------- | -- | ---------------- |
| `menuId` | `number` | 예  | 판매 상태를 변경할 메뉴 ID |

#### 요청값

판매 중지 요청:

```json
{
  "status": "STOPPED"
}
```

판매 재개 요청:

```json
{
  "status": "ON_SALE"
}
```

| 이름       | 타입       | 필수 | 설명                                |
| -------- | -------- | -- | --------------------------------- |
| `status` | `string` | 예  | 변경할 판매 상태. `ON_SALE` 또는 `STOPPED` |

현재 상태와 같은 상태를 요청해도 오류로 처리하지 않고 현재 메뉴 정보를 반환한다.

#### 성공 응답

`200 OK`

```json
{
  "data": {
    "id": 3,
    "name": "아이스 바닐라라떼",
    "price": 5800,
    "status": "STOPPED"
  }
}
```

#### 실패 응답

| 상태 코드             | code                  | 설명                |
| ----------------- | --------------------- | ----------------- |
| `400 Bad Request` | `INVALID_MENU_STATUS` | 지원하지 않는 메뉴 판매 상태임 |
| `404 Not Found`   | `MENU_NOT_FOUND`      | 메뉴가 존재하지 않음       |


### 판매 중인 메뉴 목록 조회

일반 메뉴 조회는 판매 중인 메뉴만 반환한다. `STOPPED` 메뉴는 DB에는 남지만 이 API 응답에는 포함하지 않는다.

```http
GET /api/menus
```

#### Query Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `page` | `number` | 아니오 | 페이지 번호. 기본값 `0` |
| `size` | `number` | 아니오 | 페이지 크기. 기본값 `20` |
| `sort` | `string` | 아니오 | 정렬 기준. 기본값 `id,asc` |

#### 성공 응답

`200 OK`

```json
{
  "data": [
    {
      "id": 1,
      "name": "아메리카노",
      "price": 4500,
      "status": "ON_SALE"
    }
  ],
  "page": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "sort": "id,asc"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_PAGE_REQUEST` | 페이지 번호, 크기, 정렬 기준이 올바르지 않음 |

### 메뉴 단건 조회

메뉴 상세 정보를 조회한다.
판매 중지 메뉴도 조회할 수 있으며 현재 판매 상태는 `status`로 반환한다.

```http
GET /api/menus/{menuId}
```

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `menuId` | `number` | 예 | 메뉴 ID |

#### 성공 응답

`200 OK`

```json
{
  "data": {
    "id": 1,
    "name": "아메리카노",
    "price": 4500,
    "status": "ON_SALE"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `404 Not Found` | `MENU_NOT_FOUND` | 메뉴가 존재하지 않음 |

### 인기 메뉴 조회

조회 시점을 기준으로 최근 7일 동안 생성된 완료 주문을 기준으로 인기 메뉴를 최대 3개 조회한다.

```http
GET /api/menus/popular
```

#### Query Parameters

없음

#### 성공 응답

`200 OK`

```json
{
  "data": [
    {
      "menuId": 1,
      "menuName": "아메리카노",
      "orderCount": 25
    },
    {
      "menuId": 2,
      "menuName": "카페라떼",
      "orderCount": 20
    }
  ]
}
```

조건을 만족하는 메뉴가 없으면 빈 목록을 반환한다.

```json
{
  "data": []
}
```

#### 정렬 기준

1. 최근 7일 동안의 완료 주문 건수 내림차순
2. 주문 건수가 같으면 가장 최근 완료 주문 시각 내림차순
3. 최근 주문 시각까지 같으면 메뉴 ID 오름차순

#### 관련 정책:

- `docs/policy/popular-menu.md`
- `docs/policy/menu-sales.md`

## 주문 API

### 주문 생성

한 고객의 주문을 생성하고 포인트로 결제한다.

```http
POST /api/customers/{customerId}/orders
```

#### 관련 정책:

- `docs/policy/order.md`
- `docs/policy/point.md`
- `docs/policy/menu-sales.md`

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |

#### 요청값

```json
{
  "items": [
    {
      "menuId": 1,
      "quantity": 2
    },
    {
      "menuId": 2,
      "quantity": 1
    }
  ]
}
```

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `items` | `array` | 예 | 주문 항목 목록. 1개 이상 필요 |
| `items[].menuId` | `number` | 예 | 메뉴 ID |
| `items[].quantity` | `number` | 예 | 주문 수량. 1 이상 필요 |

단일 메뉴 주문은 `items`에 주문 항목을 1개만 전달한다.

#### 성공 응답

`201 Created`

```json
{
  "data": {
    "id": 20,
    "customerId": 1,
    "totalAmount": 13500,
    "status": "COMPLETED",
    "orderedAt": "2026-07-15T10:40:00",
    "items": [
      {
        "id": 100,
        "menuId": 1,
        "menuName": "아메리카노",
        "menuPrice": 4500,
        "quantity": 2,
        "lineAmount": 9000
      },
      {
        "id": 101,
        "menuId": 2,
        "menuName": "카페라떼",
        "menuPrice": 4500,
        "quantity": 1,
        "lineAmount": 4500
      }
    ],
    "pointTransaction": {
      "id": 11,
      "type": "USE",
      "amount": -13500,
      "balanceAfter": 3500
    }
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `EMPTY_ORDER_ITEMS` | 주문 항목이 비어 있음 |
| `400 Bad Request` | `INVALID_ORDER_QUANTITY` | 주문 수량이 1보다 작음 |
| `400 Bad Request` | `DUPLICATED_ORDER_MENU` | 같은 주문 요청에 동일 메뉴가 중복됨 |
| `400 Bad Request` | `MENU_NOT_ON_SALE` | 판매 중지 메뉴를 주문하려 함 |
| `400 Bad Request` | `INSUFFICIENT_POINTS` | 포인트 잔액이 주문 총액보다 부족함 |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |
| `404 Not Found` | `MENU_NOT_FOUND` | 메뉴가 존재하지 않음 |

### 고객별 주문 목록 조회

```http
GET /api/customers/{customerId}/orders
```

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |

#### Query Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `page` | `number` | 아니오 | 페이지 번호. 기본값 `0` |
| `size` | `number` | 아니오 | 페이지 크기. 기본값 `20` |
| `sort` | `string` | 아니오 | 정렬 기준. 기본값 `orderedAt,desc` |

#### 성공 응답

`200 OK`

```json
{
  "data": [
    {
      "id": 20,
      "customerId": 1,
      "totalAmount": 13500,
      "status": "COMPLETED",
      "orderedAt": "2026-07-15T10:40:00",
      "itemCount": 2
    }
  ],
  "page": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "sort": "orderedAt,desc"
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `400 Bad Request` | `INVALID_PAGE_REQUEST` | 페이지 번호, 크기, 정렬 기준이 올바르지 않음 |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |

### 주문 상세 조회

주문 상세는 주문 당시 메뉴명, 주문 당시 메뉴 단가, 주문 항목 금액을 반환한다. 현재 메뉴 이름이나 가격이 변경되어도 주문 상세 응답은 주문 당시 정보를 기준으로 한다.

```http
GET /api/customers/{customerId}/orders/{orderId}
```

#### Path Parameters

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `customerId` | `number` | 예 | 고객 ID |
| `orderId` | `number` | 예 | 주문 ID |

#### 성공 응답

`200 OK`

```json
{
  "data": {
    "id": 20,
    "customerId": 1,
    "totalAmount": 13500,
    "status": "COMPLETED",
    "orderedAt": "2026-07-15T10:40:00",
    "items": [
      {
        "id": 100,
        "menuId": 1,
        "menuName": "아메리카노",
        "menuPrice": 4500,
        "quantity": 2,
        "lineAmount": 9000
      },
      {
        "id": 101,
        "menuId": 2,
        "menuName": "카페라떼",
        "menuPrice": 4500,
        "quantity": 1,
        "lineAmount": 4500
      }
    ],
    "pointTransaction": {
      "id": 11,
      "type": "USE",
      "amount": -13500,
      "balanceAfter": 3500,
      "transactedAt": "2026-07-15T10:40:00"
    }
  }
}
```

#### 실패 응답

| 상태 코드 | code | 설명 |
| --- | --- | --- |
| `404 Not Found` | `CUSTOMER_NOT_FOUND` | 고객이 존재하지 않음 |
| `404 Not Found` | `ORDER_NOT_FOUND` | 주문이 존재하지 않거나 해당 고객의 주문이 아님 |

## 에러 코드

| code | HTTP 상태 코드 | 설명 |
| --- | --- | --- |
| `INVALID_PAGE_REQUEST` | `400 Bad Request` | 페이지 번호, 크기, 정렬 기준이 올바르지 않음 |
| `CUSTOMER_NOT_FOUND` | `404 Not Found` | 고객이 존재하지 않음 |
| `MENU_NOT_FOUND` | `404 Not Found` | 메뉴가 존재하지 않음 |
| `MENU_NOT_ON_SALE` | `400 Bad Request` | 판매 중인 메뉴만 주문할 수 있음 |
| `EMPTY_ORDER_ITEMS` | `400 Bad Request` | 주문 항목이 비어 있음 |
| `INVALID_ORDER_QUANTITY` | `400 Bad Request` | 주문 수량이 1보다 작음 |
| `DUPLICATED_ORDER_MENU` | `400 Bad Request` | 한 주문 요청에 같은 메뉴가 중복됨 |
| `INSUFFICIENT_POINTS` | `400 Bad Request` | 포인트 잔액이 부족함 |
| `ORDER_NOT_FOUND` | `404 Not Found` | 주문이 존재하지 않거나 해당 고객의 주문이 아님 |
| `INVALID_POINT_AMOUNT` | `400 Bad Request` | 충전 포인트가 0 이하임 |
| `INVALID_POINT_TRANSACTION_TYPE` | `400 Bad Request` | 지원하지 않는 포인트 거래 유형 |
| `INVALID_MENU_NAME` | `400 Bad Request` | 메뉴 이름이 비어 있거나 올바르지 않음 |
| `INVALID_MENU_PRICE` | `400 Bad Request` | 메뉴 가격이 0 이하임 |
| `INVALID_MENU_STATUS` | `400 Bad Request` | 지원하지 않는 메뉴 판매 상태임 |
| `DUPLICATED_MENU_NAME` | `409 Conflict` | 같은 이름의 메뉴가 이미 존재함 |
