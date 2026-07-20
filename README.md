# Coffee Order System

## 프로젝트 소개

Coffee Order System은 고객이 포인트를 충전하고 판매 중인 커피 메뉴를 포인트로 주문하는 시스템을 목표로 하는 Spring Boot 프로젝트입니다.

## 주요 요구사항

상세 요구사항은 [docs/requirements.md](docs/requirements.md)를 기준으로 확인합니다.

### 원본 필수 요구사항

- 커피 메뉴 목록을 조회할 수 있어야 합니다.
- 사용자 식별값과 충전 금액으로 포인트를 충전할 수 있어야 합니다.
- 사용자 식별값과 메뉴 ID로 커피 주문과 포인트 결제를 처리할 수 있어야 합니다.
- 최근 7일 동안의 주문을 기준으로 인기 메뉴 3개를 조회할 수 있어야 합니다.
- 동시성, 데이터 일관성, 기능과 예외 상황에 대한 테스트를 고려해야 합니다.

### 프로젝트 확장 요구사항

- 고객 정보, 포인트 잔액, 포인트 거래 이력을 조회합니다.
- 메뉴 등록, 상세 조회, 이름/가격 수정, 판매 상태 변경을 지원합니다.
- 한 주문에 여러 메뉴를 포함할 수 있으며, 단일 메뉴 주문도 허용합니다.
- 주문 당시 메뉴 이름과 가격을 보존합니다.
- 판매 중인 메뉴만 일반 메뉴 목록, 신규 주문, 인기 메뉴 결과에 포함합니다.

## 핵심 설계

핵심 도메인은 다음과 같이 정리되어 있습니다.

- `Customer`: 고객 정보와 현재 포인트 잔액
- `Menu`: 메뉴 이름, 가격, 판매 상태
- `Order`: 고객 주문과 주문 총액
- `OrderItem`: 주문 당시 메뉴 정보와 수량
- `PointTransaction`: 포인트 충전과 사용 이력

주요 처리 흐름은 [docs/project-overview.md](docs/project-overview.md)에 요약되어 있으며, 기능별 구현 설계는 [docs/design/](docs/design/)에서 확인합니다.

## 기술 스택

`build.gradle`에서 확인한 기술만 정리했습니다.

- Java 17
- Gradle Wrapper
- Spring Boot 4.1.0
- Spring WebMVC
- Spring Data JPA
- Spring Validation
- Lombok
- MySQL Connector/J
- JUnit Platform
- Spring Boot Test

## 문서 안내

- [요구사항](docs/requirements.md)
- [프로젝트 개요](docs/project-overview.md)
- [API 계약](docs/api/README.md)
- [DB 설계](docs/db/README.md)
- [비즈니스 정책](docs/policy/)
- [기능별 설계](docs/design/)

README는 핵심만 요약합니다. 정책, API 요청/응답, DB 제약조건, 기능별 설계의 상세 내용은 위 문서를 기준으로 확인합니다.

## 실행 방법

Gradle Wrapper가 포함되어 있으므로 Spring Boot 애플리케이션 실행 후보 명령은 다음과 같습니다.

```bash
./gradlew bootRun
```

Windows PowerShell에서는 다음 명령을 사용할 수 있습니다.

```powershell
.\gradlew.bat bootRun
```

## 테스트

현재 확인된 테스트 코드는 기본 Spring Boot 컨텍스트 로딩 테스트입니다.

```text
src/test/java/com/example/coffeeordersystem/CoffeeOrderSystemApplicationTests.java
```
