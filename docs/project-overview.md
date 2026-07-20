# Project Overview

## 목적

이 프로젝트는 고객이 포인트를 충전하고,
판매 중인 메뉴를 포인트로 주문할 수 있는 커피 주문 시스템이다.

회원가입, 로그인과 인증·인가는 구현하지 않으며,
고객 데이터는 초기 더미 데이터로 제공한다.

비즈니스 규칙의 세부 기준은 `docs/policy/`를 따른다.

## 주요 기능

### 고객

- 고객 정보 조회
- 현재 포인트 잔액 조회
- 포인트 충전
- 포인트 충전·사용 이력 조회

### 메뉴

- 메뉴 등록
- 메뉴 목록 조회
- 메뉴 상세 조회
- 메뉴 정보 수정
- 메뉴 판매 상태 변경

### 주문

- 여러 메뉴를 포함한 주문 생성
- 고객별 주문 목록 조회
- 주문 상세 조회
- 주문 시 포인트 결제
- 주문 당시 메뉴 이름과 가격 보존

### 인기 메뉴

- 최근 7일 동안의 인기 메뉴 조회
- 완료된 주문을 기준으로 메뉴 순위 집계
- 판매 중인 메뉴만 결과에 포함

인기 메뉴의 집계 기간, 반환 개수와 정렬 기준은
`docs/policy/popular-menu.md`를 따른다.

## 핵심 도메인

### Customer

고객 정보와 현재 포인트 잔액을 관리한다.

상세 정책:

- `docs/policy/point.md`

### Menu

메뉴의 현재 이름, 가격과 판매 상태를 관리한다.

상세 정책:

- `docs/policy/menu-sales.md`

### Order

고객이 주문한 메뉴와 주문 금액을 관리한다.

한 주문에는 여러 메뉴가 포함될 수 있으며,
같은 메뉴는 수량으로 합쳐 하나의 주문 항목으로 저장한다.
단일 메뉴만 포함한 주문도 생성할 수 있다.

상세 정책:

- `docs/policy/order.md`

### PointTransaction

고객의 포인트 충전과 사용 이력을 관리한다.

현재 포인트 잔액은 고객 정보에 저장하고,
각 증감 내역은 별도의 거래 이력으로 저장한다.

상세 정책:

- `docs/policy/point.md`

## 주요 처리 흐름

### 포인트 충전

```text
고객 확인
→ 충전 금액 검증
→ 포인트 잔액 증가
→ 충전 이력 저장
```

### 주문 생성

```text
고객 확인
→ 주문 메뉴 확인
→ 메뉴 판매 상태와 수량 검증
→ 서버에서 주문 금액 계산
→ 포인트 잔액 검증
→ 주문과 주문 항목 저장
→ 포인트 차감
→ 포인트 사용 이력 저장
```

주문, 주문 항목, 포인트 차감과 사용 이력은
하나의 작업으로 처리되어야 한다.

구체적인 트랜잭션 적용 방법은 주문 기능의 `design.md`에서 정의한다.

### 인기 메뉴 조회

```text
최근 7일 조회 기간 계산
→ 판매 중 메뉴 선별
→ 집계 대상 주문 조회
→ 메뉴별 포함 주문 수 집계
→ 정렬 후 상위 3개 메뉴 반환
```

구체적인 조회 방법은 인기 메뉴 기능의 `design.md`에서 정의한다.

## 프로젝트 범위에서 제외하는 기능

현재 프로젝트에서는 다음 기능을 구현하지 않는다.

- 회원가입과 로그인
- 인증과 인가
- 현금, 카드와 외부 결제
- 주문 취소와 환불
- 재고 관리
- 쿠폰과 할인
- 매장과 지점 관리
- 배송과 포장 상태 관리
- 운영 환경 배포
- 실제 결제 또는 외부 시스템 연동

범위 밖 기능이 필요해지면 기존 정책과 영향을 확인하고
Plan 단계에서 작업 범위를 다시 결정한다.

## 문서 구조

```text
AGENTS.md
docs/
├── project-overview.md
├── requirements.md
├── code-convention.md
├── git-guide.md
├── safety-guide.md
├── api/
│   └── README.md
├── db/
│   └── README.md
├── policy/
│   ├── README.md
│   ├── point.md
│   ├── menu-sales.md
│   ├── order.md
│   └── popular-menu.md
├── design/
│   ├── README.md
│   ├── menu/
│   │   └── design.md
│   ├── point/
│   │   └── design.md
│   ├── order/
│   │   └── design.md
│   └── popular-menu/
│       └── design.md
└── workflow/
    ├── plan-guide.md
    ├── generate-guide.md
    └── evaluate-guide.md
```

기능별 구현 설계가 필요한 경우 해당 기능 영역에
`design.md`를 작성한다.

## 문서별 책임

- `docs/policy/`: 비즈니스 규칙
- `docs/requirements.md`: 원본 필수 요구사항과 프로젝트 확장 요구사항
- `docs/api/README.md`: 외부 API 계약
- `docs/db/README.md`: 데이터 구조와 제약조건
- `docs/project-overview.md`: 프로젝트 범위와 전체 구조
- `docs/code-convention.md`: 코드 작성 규칙
- 기능별 `design.md`: 구체적인 구현 방법
- `docs/workflow/`: 에이전트 작업 흐름
- `docs/git-guide.md`: Git 사용 규칙
- `docs/safety-guide.md`: 위험 작업 처리 기준

문서 사이의 책임과 우선순위는 루트 `AGENTS.md`를 따른다.
