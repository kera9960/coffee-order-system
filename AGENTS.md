# AGENTS.md

## 목적
이 저장소에서 작업하는 모든 코딩 에이전트가 따라야 하는
공통 작업 흐름과 참조 문서를 정의한다.

## 실행 흐름
Plan
→ 사용자 선택 및 승인
→ Generate
→ Evaluate

모든 파일 변경은 Generate 단계에서만 수행한다.

## 컨텍스트 맵

### 작업 단계

- Plan: `docs/workflow/plan-guide.md`
- Generate: `docs/workflow/generate-guide.md`
- Evaluate: `docs/workflow/evaluate-guide.md`

### 프로젝트 문서

- 프로젝트 범위와 구조: `docs/project-overview.md`
- 비즈니스 정책: `docs/policy/README.md`
- API 계약: `docs/api/README.md`
- DB 구조: `docs/db/README.md`
- 코드 작성 규칙: `docs/code-convention.md`
- Git 작업 규칙: `docs/git-guide.md`
- 위험 작업 규칙: `docs/safety-guide.md`

세부 비즈니스 정책은 `docs/policy/README.md`에서
관련 정책 문서를 찾아 확인한다.

## 문서 책임과 우선순위

- 비즈니스 규칙은 `docs/policy/`를 기준으로 한다.
- API의 URL, 요청, 응답과 상태 코드는 `docs/api/README.md`를 기준으로 한다.
- 테이블, 컬럼, 관계, 제약조건과 인덱스는 `docs/db/README.md`를 기준으로 한다.
- 코드 작성 방식은 `docs/code-convention.md`를 기준으로 한다.
- 구체적인 구현 방법은 해당 기능의 `design.md`를 기준으로 한다.
- 문서 사이에 충돌이 있으면 임의로 판단하지 않고 Plan 단계에서 보고한다.

## 공통 원칙

- 작업과 관련된 코드와 문서를 먼저 확인한다.
- 파일을 읽지 않고 프로젝트 구조를 추측하지 않는다.
- 기존 프로젝트 패턴을 우선한다.
- 사용자가 승인한 범위만 변경한다.
- 관련 없는 리팩터링이나 포맷 변경을 하지 않는다.
- 사용자가 작성한 기존 변경사항을 훼손하거나 되돌리지 않는다.
- 실행하지 않은 테스트를 통과했다고 보고하지 않는다.
- API, DB, 기능 설계 문서에 정책 내용을 중복해서 정의하지 않는다.
- 기존 정책과 충돌하는 변경이 필요하면 작업을 중단하고 Plan으로 돌아간다.
- commit, push, merge는 수행하지 않는다.

## 기본 검증 명령어

- ./gradlew compileJava
- ./gradlew test
- ./gradlew build

작업 특성상 일부 명령을 실행하지 않았다면
Evaluate 결과에 실행하지 않은 이유를 기록한다.