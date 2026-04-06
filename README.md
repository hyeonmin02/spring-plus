# SPRING PLUS
## 📌 프로젝트 소개
본 프로젝트는 Spring Boot 기반으로 인증, 데이터 처리, 성능 최적화까지  백엔드 핵심 기술을 종합적으로 학습하고 개선하기 위한 프로젝트입니다. 
@Transactional, JWT, JPA, AOP, N+1 문제 해결, QueryDSL 등 다양한 기술을 적용하여 기능을 개선하였으며, 500만 건의 데이터를 생성한 뒤  
닉네임 기반 조회에서 Page, Slice, Projection, Index 적용 여부에 따른 성능 차이를 비교 분석하여 조회 성능 최적화를 수행하였습니다.

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.3.3

### Data Access
- Spring Data JPA
- JDBC (Bulk Insert)

### Database
- MySQL

### Security
- Spring Security
- JWT

### Query
- QueryDSL

### Test
- JUnit5
- Spring Boot Test

### Tool
- IntelliJ IDEA
- Postman

## 📂 프로젝트 구조

```text
src
 ┣ main
 ┃ ┣ java/org/example/expert
 ┃ ┃ ┣ aop
 ┃ ┃ ┣ client
 ┃ ┃ ┣ config
 ┃ ┃ ┣ domain
 ┃ ┃ ┃ ┣ auth
 ┃ ┃ ┃ ┣ comment
 ┃ ┃ ┃ ┣ common
 ┃ ┃ ┃ ┣ log
 ┃ ┃ ┃ ┣ manager
 ┃ ┃ ┃ ┣ todo
 ┃ ┃ ┃ ┗ user
 ┃ ┃ ┗ ExpertApplication
 ┃ ┗ resources
 ┗ test
````

## 🚀 기능 구현 및 개선
### 1-1 @Transactional 문제 해결

#### ❗ 문제 상황
- readOnly 트랜잭션으로 인해 insert 실패 발생
#### 🔍 원인 분석
- @Transactional(readOnly = true) 상태에서 데이터 변경 시도
#### 🛠 해결 방법
- 해당 메서드에 @Transactional 추가
#### 💡 배운 점
- readOnly 옵션은 쓰기 작업을 제한한다

---

### 1-2 JWT의 이해
#### ❗ 문제 상황
- 기획자의 요구로 사용자 닉네임을 화면에 표시해야 하는 기능이 추가됨
#### 🔍 원인 분석
- User 엔티티 및 DB 테이블에 nickname 필드가 존재하지 않음
- JWT 생성 시 nickname 값이 claim에 포함되지 않음
#### 🛠 해결 방법
- User 엔티티 및 DB 테이블에 nickname 컬럼 추가 및 중복가능 설정
- JWT 생성 시 claim에 nickname 값 추가
- 인증 과정에서 JWT를 통해 nickname 값을 사용할 수 있도록 수정
#### 💡 배운 점
- 요구사항 변경 시, DB 구조만이 아닌 JWT 인증 구조를 함께 고려해야함

---

### 1-3 JPA의 이해
#### ❗ 문제 상황
- 기획자의 요청으로 할 일 조회 시 weather와 수정일 기간 조건을 활용한 검색 기능이 필요해짐
- 각 조건은 항상 들어오는 것이 아니라, 있을 수도 있고 없을 수도 있는 선택 조건임
#### 🔍 원인 분석
- 기존 조회 API는 단순 페이징 조회만 지원하고 있어, weather 및 수정일 범위 조건을 반영할 수 없었음
#### 🛠 해결 방법
- 컨트롤러에서 weather, start, end 값을 선택적으로 받을 수 있도록 요청 파라미터를 추가
- Repository에서는 JPQL을 사용해 weather 값이 없으면 해당 조건을 무시하고 startDate, endDate 값이 없으면 기간 조건을 제외하도록 처리
- 수정일 종료 조건은 사용자가 입력한 날짜를 당일 끝시간까지 포함하는 의미로 처리하기 위해, 단순 비교가 아니라 다음 날 00:00 이전까지 조회되도록 처리
#### 💡 배운 점
- 날짜 검색에서는 단순히 endDate만 비교하는 것이 아니라, 사용자가 기대하는 조회 범위가 어디까지인지를 기준으로 조건을 설계해야 한다는 점을 이해함

---

### 1-4 컨트롤러 테스트의 이해
#### ❗ 문제 상황
- 단건 조회 시 Todo가 존재하지 않을 경우 예외가 발생해야 하는 테스트가 실패함
- 테스트에서는 404 Not Found 응답을 기대하고 있었지만, 실제로는 200 OK가 반환되고 있었음
#### 🔍 원인 분석
- Todo 조회 실패 시 명확한 예외 처리가 이루어지지 않아, 정상 응답(200 OK)이 반환되고 있었음
#### 🛠 해결 방법
- Todo를 찾지 못한 경우 NotFoundException을 발생시키도록 추가, 수정
- 해당 예외를 통해 HTTP 상태 코드가 404 Not Found로 반환되도록 처리
#### 💡 배운 점
- 예외 상황에서는 단순히 에러를 발생시키는 것이 아니라, 적절한 HTTP 상태 코드와 메시지를 함께 전달하는 것이 중요하다는 것을 배웠음

---

### 1-5 AOP의 이해
#### ❗ 문제 상황
- 로그는 UserAdminController의 changeUserRole() 메서드 실행 전에 동작해야 했지만, 실제로는 실행 후에 동작하고 있었음
#### 🔍 원인 분석
- 포인트컷이 UserAdminController.changeUserRole()이 아닌, 다른 컨트롤러의 메서드를 바라보고 있었음
- 또한 Advice 타입이 @After로 설정되어 있어, 메소드 실행 전이 아니라 실행 후에 로직이 수행되고 있었음
#### 🛠 해결 방법
- 포인트컷 대상을 UserAdminController.changeUserRole() 메소드로 올바르게 수정
- Advice를 @After에서 @Before로 변경하여, 메소드 실행 전에 접근 로그가 남도록 수정
- 메서드명도 동작 의도에 맞게 logBeforeChangeRole로 명시하여 가독성을 높임
#### 💡 배운 점
- 포인트컷 대상이 조금만 잘못되어도 전혀 다른 메서드에 AOP가 적용될 수 있기 때문에, 대상 경로와 메서드 시그니처를 정확히 지정해주는 것이 중요하다는 것을 이해함

---

### 2-1 JPA Cascade
#### ❗ 문제 상황
- 연관 관계는 설정되어 있었지만, 실제로 Manager 엔티티가 함께 저장되지 않는 문제가 발생함
#### 🔍 원인 분석
- Todo와 Manager 간 연관 관계는 설정되어 있었지만, 영속성 전이(Cascade)가 적용되지 않아 Todo 저장 시 Manager가 함께 저장되지 않았음
#### 🛠 해결 방법
- Todo 엔티티의 @OneToMany 관계에 cascade = CascadeType.PERSIST를 추가함
- 이를 통해 Todo 저장 시 연관된 Manager 엔티티도 함께 저장되도록 설정
#### 💡 배운 점
- 컴파일 에러가 발생하지 않더라도, 연관 관계 설정이 잘못되면 런타임에서 예상치 못한 데이터 문제가 발생할 수 있다는 점을 이해함

---

### 2-2 N+1
#### ❗ 문제 상황
- CommentController의 getComments() API 호출 시, 댓글 목록 조회 과정에서 N+1 문제가 발생함
#### 🔍 원인 분석
- 댓글 목록은 한 번에 조회되었지만, 각 댓글이 참조하는 user 연관 엔티티를 조회할 때마다 추가 쿼리가 발생함
#### 🛠 해결 방법
- 댓글 조회 JPQL에 JOIN FETCH를 적용하여, 댓글과 연관된 사용자 정보를 한 번에 조회하도록 수정함
#### 💡 배운 점
- 기능이 정상 동작하더라도, 실제 실행되는 쿼리 수를 확인하지 않으면 성능 문제를 놓칠 수 있다는 점을 이해함

---

### 2-3 QueryDSL
#### ❗ 문제 상황
- 기존 JPQL로 작성되어 있었던 findByIdWithUser 조회 로직을 QueryDSL로 변경요청함
#### 🔍 원인 분석
- JPQL 문자열 기반 쿼리는 오타나 필드 변경에 취약하고, 컴파일 시점에 오류를 확인하기 어려움
#### 🛠 해결 방법
- QueryDSL 의존성을 추가하고, QuerydslConfig를 설정하여 QClass를 생성할 수 있도록 구성함
- 기존 JPQL 기반 findByIdWithUser를 QueryDSL로 변경하였고, fetchJoin()을 적용해 Todo와 연관된 User를 한 번에 조회하도록 수정
#### 💡 배운 점
- QueryDSL은 문자열 기반 JPQL보다 타입 안전성이 높아, 필드 변경이나 오타에 더 강한 조회 방식을 제공한다는 점을 배웠음

---

### 2-4 Spring Security
#### ❗ 문제 상황
- 기존 프로젝트는 Filter와 Argument Resolver를 직접 사용하여 인증 사용자 정보를 처리하고 있었음
- 또한 권한 체크 역시 프레임워크의 표준 기능이 아니라 직접 처리하고 있어, 유지보수 관점에서 개선이 필요했음
#### 🔍 원인 분석
- 인증 정보 추출과 권한 처리 로직을 Filter, Argument Resolver 등에서 직접 관리하고 있었고,
- Spring Security가 제공하는 Authentication, UserDetails, 권한 기반 인가 기능을 활용하지 못하고 있었음
#### 🛠 해결 방법
- Spring Security 의존성을 추가하고, SecurityConfig를 통해 보안 설정을 새롭게 구성함
- 세션 기반 인증이 아닌 JWT 기반 인증 방식을 유지하기 위해 SessionCreationPolicy.STATELESS를 적용함
- 기존 커스텀 Filter와 Argument Resolver 중심 구조를 제거하고, JWT를 검증하는 JwtAuthenticationFilter를 SecurityFilterChain에 등록하여 인증 과정을 Spring Security 필터 체인 안으로 편입
- JWT에서 추출한 사용자 정보(id, email, role)를 기반으로 MyUserDetails를 생성하고, 이를 Authentication 객체로 변환하여 SecurityContext에 저장하도록 수정
- 권한 처리는 직접 조건문으로 관리하는 대신, Spring Security의 hasRole, authenticated 설정을 사용하여 URL 접근 권한을 제어
#### 💡 배운 점
- Spring Security는 단순히 로그인 기능을 붙이는 도구가 아니라, 인증(Authentication)과 인가(Authorization)를 표준화된 흐름으로 관리하는 프레임워크라는 점을 이해
- JWT는 Spring Security 자체가 아니라 인증 수단이며, Spring Security와 결합할 때는 JWT를 검증한 뒤 인증 객체로 변환하여 SecurityContext에 저장하는 과정이 필요하다는 점을 배웠음
- JWT는 토큰 기반 인증 방식 / Spring Security는 이를 관리하는 보안 프레임워크라는 역할 차이를 이해하게 되었음

---

### 3-1 QueryDSL을 사용하여 검색 기능 만들기
### ❗ 문제 상황

- 일정 검색 기능이 필요해졌으며,  
  👉 **제목 키워드 / 담당자 닉네임 / 생성일 범위** 등  
  여러 조건을 조합한 검색이 요구됨

- 또한 검색 결과는  
  👉 **페이징 + 집계(담당자 수, 댓글 수)** 를 함께 고려해야 했음

---

### 🔍 원인 분석

- 조건이 많아질수록  
  👉 메서드명 기반 쿼리 / JPQL은 가독성과 유지보수성 저하

- 전체 엔티티 조회 시  
  👉 불필요한 데이터까지 조회 → 성능 비효율

- 페이징 처리에서  
  👉 content(현재 페이지 데이터) vs total(전체 개수) 역할이 다름

---

### 🛠 해결 방법

#### 1️⃣ QueryDSL 도입
- 동적 조건 조합을 위해 QueryDSL 적용
- `BooleanExpression`을 활용하여 조건을 유연하게 구성

#### 2️⃣ Projection 적용
- `Projections.constructor()` 활용
- 필요한 필드만 조회하여 DTO(`TodoSearchResponse`)로 반환

#### 3️⃣ 집계 처리 (countDistinct)
- 담당자 수 / 댓글 수 계산 시  
  👉 중복 제거를 위해 `countDistinct()` 사용

#### 4️⃣ 페이징 최적화
- content 조회 쿼리와  
  👉 total count 쿼리를 분리하여 작성

- `PageImpl`을 사용하여  
  👉 content + total 함께 반환
#### 💡 배운 점
- QueryDSL은 문자열 기반 JPQL보다 타입 안전성이 높고, 조건이 동적으로 바뀌는 검색 기능에 더 유연하게 대응할 수 있다는 점을 배웠음
- Projection을 활용하면 필요한 필드만 조회할 수 있어, 전체 엔티티를 조회하는 방식보다 응답 목적에 맞는 효율적인 조회가 가능하다는 점을 이해함
- 페이징 처리에서는 현재 페이지 데이터(content)와 전체 개수(total)의 역할이 다르며, total 값이 있어야 전체 페이지 수를 계산할 수 있다는 점을 배웠음

---

### 3-2 Transaction 심화
#### ❗ 문제 상황
- 매니저 등록 요청이 발생할 때마다 로그를 새롭게 남기도록 요구함
- 하지만 매니저 등록은 실패할 수 있는 작업이므로, 등록 실패 시에도 로그는 반드시 저장되어야 했음
#### 🔍 원인 분석
- 매니저 등록과 로그 저장을 동일한 트랜잭션으로 처리하면, 매니저 등록 과정에서 예외가 발생했을 때 로그 저장도 함께 롤백될 수 있었음
- 또한 REQUIRES_NEW를 사용하더라도 같은 클래스 내부에서 호출하면 Spring AOP 프록시를 거치지 않아, 기대한 대로 새 트랜잭션이 분리되지 않을 수 있는 문제가 있었음
#### 🛠 해결 방법
- 로그 전용 Log 엔티티, Repository, Service를 별도로 분리하여 구성
- 로그 저장 메서드는 @Transactional(propagation = Propagation.REQUIRES_NEW)를 적용해 기존 매니저 등록 트랜잭션과 독립적인 새 트랜잭션에서 수행되도록 했음
- 매니저 등록 로직에서는 try-catch를 사용해 성공 시 성공 로그 저장 실패 시 예외 메시지를 포함한 실패 로그 저장이 가능하도록 처리
#### 💡 배운 점
- REQUIRES_NEW는 단순 옵션이 아니라, 기존 트랜잭션과 완전히 분리된 별도 커밋 단위를 만드는 설정이라는 점을 이해했음
- 로그 설계 시에는 단순 메시지 저장이 아니라, 누가 요청했고 누구를 대상으로 어떤 작업을 수행했는지 식별 가능한 정보를 함께 남기는 것이 중요하다는 점을 배웠음

---

### 3-3 대용량 데이터 처리
#### ❗ 문제 상황
- 대용량 데이터 환경에서 닉네임 기반 사용자 검색 성능을 개선할 필요가 있었음
- 단순 조회 방식만으로는 데이터가 많아질수록 응답 속도가 느려질 수 있기 때문에, 여러 조회 전략을 비교하며 최적화 방안을 검증하고자 했음
#### 🔍 원인 분석
- 500만 건 규모의 사용자 데이터에서는 단순 조회 시 검색 비용이 커질 수 있었음
- 특히 nickname 조건 검색은 데이터 양이 많을수록 Full Scan 비용이 커질 수 있으며, Page 조회의 경우 count 쿼리가 추가되어 더 많은 비용이 발생할 수 있음
#### 🛠 해결 방법
#### 1️⃣ 대용량 데이터 생성
- JDBC `batchUpdate()` 활용 → 약 **500만 건 데이터 생성**

#### 2️⃣ 조회 방식 비교

| 방식 | 설명 |
|------|------|
| Page | 전체 개수(count) 포함 |
| Slice | 다음 페이지 존재 여부만 확인 |
| Projection | 필요한 필드만 조회 |

#### 3️⃣ 인덱스 적용
- `nickname` 컬럼에 인덱스 추가
- 인덱스 적용 전/후 성능 비교 진행
#### 💡 배운 점
- 대용량 데이터 환경에서는 같은 기능이라도 조회 전략(Page / Slice / Projection) 에 따라 성능 차이가 발생할 수 있다는 점을 확인
- Slice는 전체 개수가 필요하지 않은 상황에서 더 효율적인 선택이 될 수 있음을 알게 되었음
- 인덱스는 가장 큰 성능 개선 효과를 가져오지만, 무분별하게 추가할 경우 쓰기 성능 저하와 저장 공간 증가 등의 비용이 발생할 수 있다는 점도 함께 배웠음

---


## 🧪 실험 환경

### 🔹 테스트 조건
- 데이터: 약 5,000,000건
- 검색 조건: nickname = target_user
- 페이지 크기: 100
- 비교 방식: Page / Slice / Projection
---

## 📈 성능 비교 결과

### 1️⃣ 인덱스 미적용

| 방식 | 응답 시간 | 특징 |
|------|----------:|------|
| Page | 2.97s | count 쿼리 포함 |
| Slice | 1.59s | count 쿼리 없음 |
| Projection | 2.57s | 필요한 필드만 조회 |

<img width="721" height="819" alt="슬라이스 조회" src="https://github.com/user-attachments/assets/7816eaf5-5b59-4f4d-862e-706c39d4afd8" />

---

### 2️⃣ 인덱스 적용 후

| 방식 | 응답 시간 | 특징 |
|------|----------:|------|
| Page | 11ms | count 쿼리 포함 |
| Slice | 10ms | count 쿼리 없음 |
| Projection | 약 10ms | 필요한 필드만 조회 |

<img width="585" height="805" alt="인덱스 추가 후 슬라이스 성능 비교" src="https://github.com/user-attachments/assets/212753ec-0139-471c-b751-870b31fe9556" />

---

### 3️⃣ SQL 실행 시간 비교

| 쿼리 | 인덱스 없음 | 인덱스 적용 | 개선율 |
|------|------------:|------------:|--------|
| GROUP BY (중복 닉네임 조회) | 11.35s | 2.61s | 약 4.3배 개선 |
| COUNT (단일 닉네임 조회) | 1.34s | 0.35s | 약 3.8배 개선 |

---

### 4️⃣ EXPLAIN 분석

| 상태 | type | key | 의미 |
|------|------|-----|------|
| 인덱스 없음 | ALL | null | Full Table Scan |
| 인덱스 적용 | ref | idx_users_nickname | 인덱스 기반 조회 |


인덱스 없음
<img width="1276" height="197" alt="인덱스 없이 검증했을 시 " src="https://github.com/user-attachments/assets/b3a6d931-d4a7-4110-8ed3-e4413990ca28" />
인덱스 적용
<img width="1218" height="170" alt="인덱스 조회성능 검증" src="https://github.com/user-attachments/assets/774b2a92-38e6-4b5a-8a86-469fe38137e0" />
