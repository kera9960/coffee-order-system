# Code Convention

이 문서는 프로젝트의 코드 작성 규칙을 정의한다.

기존 코드에 일관된 패턴이 있다면 기존 방식을 우선하고,
새로운 코드는 이 문서의 규칙을 따른다.

## 기본 원칙

- 읽기 쉬운 코드를 우선한다.
- 하나의 클래스와 메서드는 하나의 책임에 집중한다.
- 비즈니스 규칙을 Controller에 작성하지 않는다.
- 중복을 줄이되 불필요한 추상화는 만들지 않는다.
- 요구사항에 없는 기능을 미리 구현하지 않는다.
- 관련 없는 리팩터링과 포맷 변경을 함께 수행하지 않는다.

## 패키지 구조

비즈니스 기능은 `domain` 아래에서 기능별로 분리한다.

```text
common/

domain/
├── customer/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── entity/
├── menu/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── entity/
├── order/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── entity/
└── point/
    ├── controller/
    ├── service/
    ├── repository/
    ├── dto/
    └── entity/
```
- Entity와 관련 Enum은 기능별 `entity` 패키지에 둔다.
- Enum이 많아지면 기능 내부의 `type` 패키지로 분리할 수 있다.
- 기능에 종속된 예외는 해당 기능의 `exception` 패키지에 둔다.
- 공통 예외 응답, 전역 예외 처리와 공통 에러 코드는 `common`에 둔다.
- 기능에 필요한 하위 패키지만 생성하며, 빈 패키지를 미리 만들지 않는다.
- 기존 프로젝트 구조가 있다면 패키지를 일괄 변경하지 않고 기존 방식을 우선한다.

## 계층별 책임

### Controller

- HTTP 요청을 받는다.
- 요청값을 검증한다.
- Service를 호출한다.
- API 계약에 맞는 응답을 반환한다.
- 비즈니스 로직과 직접적인 DB 접근을 작성하지 않는다.

### Service

- 비즈니스 처리 순서를 관리한다.
- 정책에 따른 검증을 수행한다.
- 여러 도메인과 Repository 작업을 조합한다.
- 필요한 트랜잭션 경계를 정의한다.
- HTTP 요청이나 응답 객체에 의존하지 않는다.

### Entity

- 도메인의 상태와 행동을 관리한다.
- 상태 변경에 필요한 기본 검증을 수행한다.
- Controller, DTO와 같은 외부 계층의 객체에 의존하지 않는다.

### Repository

- 데이터 저장과 조회를 담당한다.
- 비즈니스 판단을 수행하지 않는다.
- 조회 목적이 드러나는 메서드 이름을 사용한다.

### DTO

- API 요청과 응답 데이터를 전달한다.
- Entity를 API 응답으로 직접 반환하지 않는다.
- 요청 DTO와 응답 DTO를 구분한다.

## 이름 규칙

### 클래스

역할이 드러나는 이름을 사용한다.

```text
MenuController
MenuService
MenuRepository
Menu
CreateMenuRequest
MenuResponse
ErrorCode
```

### 메서드

동작을 나타내는 동사로 시작한다.

```text
createOrder
getOrder
getOrders
chargePoint
changeMenuStatus
validateOrderable
calculateTotalAmount
```

다음처럼 의미가 불분명한 이름은 피한다.

```text
process
handle
execute
doSomething
data
info
```

단, 클래스의 역할상 의미가 명확한 경우에는 사용할 수 있다.

### 조회 메서드

조회 결과의 의미가 드러나도록 작성한다.

```text
findById
findAllByCustomerId
findOnSaleMenus
existsByName
```

조회 결과가 없을 수 있다면 `Optional` 사용 여부를
기존 Repository 패턴에 맞춘다.

### 상수

상수는 대문자 스네이크 표기법을 사용한다.

```text
MAX_PAGE_SIZE
DEFAULT_PAGE_SIZE
POPULAR_MENU_LIMIT
```

## Controller 규칙

- Controller는 요청 전달과 응답 변환에 집중한다.
- 요청 검증은 Bean Validation을 우선한다.
- 동일한 요청값을 Service에서 불필요하게 다시 파싱하지 않는다.
- HTTP 상태 코드는 `docs/api/README.md`를 따른다.
- 공통 예외 처리는 전역 예외 처리 방식을 사용한다.

## Service 규칙

- 읽기 작업은 필요한 경우 읽기 전용 트랜잭션을 사용한다.
- 여러 저장 작업이 하나의 비즈니스 작업이면 하나의 트랜잭션으로 처리한다.
- 트랜잭션 안에서 불필요한 외부 호출을 하지 않는다.
- Service 메서드 이름은 비즈니스 동작을 표현한다.
- 다른 Service를 과도하게 연쇄 호출하지 않는다.
- 정책에 없는 검증을 임의로 추가하지 않는다.

## Entity 규칙

- 필드는 가능한 한 외부에서 직접 변경하지 못하게 한다.
- 상태 변경은 의미 있는 메서드를 통해 수행한다.
- 공개 setter 사용을 피한다.
- 생성 시 반드시 필요한 값은 생성 과정에서 받는다.
- Entity에 Controller나 API 응답 책임을 넣지 않는다.
- 양방향 연관관계는 필요한 경우에만 사용한다.

예시 형태:

```text
menu.changePrice(...)
menu.stopSales()
customer.chargePoint(...)
customer.usePoint(...)
```

## DTO 규칙

요청과 응답 DTO의 이름을 명확히 구분한다.

```text
CreateOrderRequest
OrderItemRequest
OrderResponse
OrderDetailResponse
```

- Request DTO에는 입력값 검증을 작성한다.
- Response DTO에는 API에 필요한 정보만 포함한다.
- Entity를 DTO 내부에 그대로 포함하지 않는다.
- DTO 변환 위치는 프로젝트의 기존 패턴을 따른다.

## 예외 처리

- 예상 가능한 비즈니스 실패는 프로젝트의 공통 예외 처리 방식으로 표현한다.
- `RuntimeException`을 의미 없이 직접 생성해서 던지는 방식은 피한다.
- 예외 또는 에러 코드는 실패 원인을 명확하게 나타내야 한다.

```text
CUSTOMER_NOT_FOUND
MENU_NOT_FOUND
MENU_NOT_ON_SALE
INSUFFICIENT_POINTS
ORDER_NOT_FOUND
```

- 예외 처리 방식은 기존 프로젝트 패턴을 우선한다.
- 예외를 잡고 아무 처리 없이 무시하지 않는다.
- 내부 예외 메시지나 스택 트레이스를 API 응답에 직접 노출하지 않는다.
- 에러 코드와 HTTP 상태는 `docs/api/README.md`를 따른다.

## 검증 규칙

검증은 책임에 맞는 위치에 둔다.

- 입력 형식 검증: Request DTO
- 조회 대상 존재 여부: Service
- 비즈니스 정책 검증: Service 또는 Entity
- 데이터 무결성: DB 제약조건
- API 응답 형식: Controller와 Response DTO

같은 검증을 여러 계층에 불필요하게 반복하지 않는다.

## 테스트 규칙

- 테스트 이름에서 검증하려는 동작이 드러나야 한다.
- 정상 상황과 주요 실패 상황을 함께 테스트한다.
- 하나의 테스트는 하나의 핵심 동작을 검증한다.
- 테스트끼리 실행 순서에 의존하지 않는다.
- 외부 환경과 운영 데이터에 의존하지 않는다.
- 테스트를 통과시키기 위해 검증을 삭제하거나 약화하지 않는다.

테스트 이름은 프로젝트의 기존 언어와 형식을 따른다.

예시:

```text
포인트가_충분하면_주문을_생성한다
포인트가_부족하면_주문이_생성되지_않는다
판매_중지된_메뉴는_주문할_수_없다
```

## 주석 규칙

- 코드만으로 의도가 드러나면 주석을 작성하지 않는다.
- 무엇을 하는지보다 왜 그렇게 했는지를 설명한다.
- 코드와 맞지 않는 오래된 주석은 남기지 않는다.
- 주석 처리된 코드를 보관용으로 남기지 않는다.

## 포맷 규칙

- 들여쓰기는 공백 4칸을 사용한다.
- 한 줄에는 하나의 주요 동작을 작성한다.
- 와일드카드 import를 사용하지 않는다.
- 사용하지 않는 import와 코드는 제거한다.
- 파일 마지막에는 빈 줄을 둔다.
- 프로젝트에 자동 포맷 설정이 있다면 해당 설정을 따른다.

## 금지 사항

- Controller에 비즈니스 로직 작성
- Entity를 API 응답으로 직접 반환
- 공개 setter를 통한 무분별한 상태 변경
- 의미 없는 공통 클래스와 추상화 추가
- 테스트 삭제 또는 비활성화
- 예외를 무조건 무시하는 처리
- 비밀번호, API 키와 토큰 작성
- 관련 없는 코드의 일괄 수정
- 승인되지 않은 API, DB와 정책 변경