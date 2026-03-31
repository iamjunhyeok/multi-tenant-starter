# Multi-Tenant Starter

멀티테넌트 SaaS 애플리케이션을 위한 Spring Boot 3.x 자동 구성 스타터.
의존성 하나 추가하고, 프로퍼티 몇 줄 설정하면 웹 레이어, JPA/Hibernate, MyBatis 전반에 걸친 투명한 테넌트 격리를 제공합니다.

## 해결하는 문제

멀티테넌트 SaaS를 만들 때마다 반복되는 인프라 구현:
- 요청마다 테넌트 식별 (헤더, JWT, 서브도메인)
- 요청 생명주기 전체에 테넌트 컨텍스트 전파
- 모든 데이터베이스 쿼리에 테넌트 격리 강제
- 테넌트 간 데이터 누수 방지
- 테넌트 포함 로깅

이 스타터는 위 모든 기능을 플러그 앤 플레이 라이브러리로 제공합니다.

## 빠른 시작

### 1. 의존성 추가

```groovy
// build.gradle
dependencies {
    implementation 'io.github.iamjunhyeok:multi-tenant-starter:0.0.1-SNAPSHOT'
}
```

### 2. 설정 (선택 사항 - 기본값으로 바로 동작)

```yaml
# application.yml
tenant:
  resolver:
    strategy: HEADER           # HEADER (기본값), JWT
    header-name: X-Tenant-ID   # 기본값
```

### 3. 테넌트 헤더와 함께 요청

```bash
curl -H "X-Tenant-ID: tenant-a" http://localhost:8080/users
```

이게 전부입니다. 모든 데이터베이스 쿼리가 자동으로 `tenant-a` 기준으로 필터링됩니다.

---

## 기능

### 테넌트 식별

| 전략 | 소스 | 설정 |
|------|------|------|
| **Header** (기본값) | HTTP 헤더 `X-Tenant-ID` | `tenant.resolver.header-name` |
| **JWT** | JWT claim `tenant_id` | `tenant.resolver.jwt-claim-name` |

`TenantResolver` 인터페이스를 구현하고 빈으로 등록하면 커스텀 리졸버를 사용할 수 있습니다.

### 테넌트 ID 검증

모든 요청에서 테넌트 ID를 검증합니다:
- 최대 길이: 64자 (설정 가능)
- 허용 문자: `a-zA-Z0-9_-` (정규식 설정 가능)

```yaml
tenant:
  id:
    max-length: 128
    pattern: "^[a-z0-9-]+$"
```

### JPA / Hibernate 통합

`TenantAwareEntity`를 상속하면 자동으로 테넌트 격리가 적용됩니다:

```java
@Entity
@Table(name = "orders")
public class Order extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
}
```

**자동으로 처리되는 것:**
- **SELECT**: Hibernate `@Filter`가 모든 쿼리에 `WHERE tenant_id = ?` 추가
- **INSERT**: `@PrePersist` 리스너가 현재 컨텍스트에서 `tenant_id` 자동 세팅
- **UPDATE 보호**: `tenant_id` 컬럼은 `updatable = false`

```java
// 모든 쿼리가 자동으로 현재 테넌트 기준으로 필터링됨
List<Order> orders = orderRepository.findAll();
// -> SELECT ... FROM orders WHERE tenant_id = 'tenant-a'

Order order = new Order();
orderRepository.save(order);
// -> INSERT INTO orders (..., tenant_id) VALUES (..., 'tenant-a')
```

### MyBatis 통합

별도 상속이 필요 없습니다. [JSqlParser](https://github.com/JSQLParser/JSqlParser) 기반 SQL 인터셉터가 AST 수준에서 쿼리를 변환합니다:

```java
@Mapper
public interface OrderMapper {

    @Select("SELECT * FROM orders")
    List<Order> findAll();
    // -> SELECT * FROM orders WHERE tenant_id = ?

    @Insert("INSERT INTO orders (product_name) VALUES (#{productName})")
    void insert(Order order);
    // -> INSERT INTO orders (product_name, tenant_id) VALUES (?, ?)
}
```

**복잡한 쿼리도 정확하게 처리:**

```sql
-- JOIN: 각 테이블에 개별 테넌트 조건 추가
SELECT o.id, u.name FROM orders o JOIN users u ON o.user_id = u.id
-- -> ... WHERE o.tenant_id = ? AND u.tenant_id = ?

-- UNION: 각 SELECT에 테넌트 조건 추가
SELECT id FROM orders UNION SELECT id FROM products
-- -> SELECT id FROM orders WHERE tenant_id = ? UNION SELECT id FROM products WHERE tenant_id = ?

-- 서브쿼리
SELECT * FROM orders WHERE user_id IN (SELECT id FROM users WHERE active = true)
-- -> ... WHERE orders.tenant_id = ? AND user_id IN (SELECT id FROM users WHERE users.tenant_id = ? AND active = true)
```

### `@TenantRequired` 어노테이션

테넌트가 필수인 엔드포인트를 지정합니다. 테넌트 없이 요청하면 `400 Bad Request`:

```java
@RestController
@TenantRequired  // 클래스 레벨: 모든 메서드에 적용
public class OrderController {

    @GetMapping("/orders")
    public List<Order> getOrders() { ... }
}

// 또는 메서드 레벨:
@TenantRequired
@GetMapping("/orders")
public List<Order> getOrders() { ... }
```

`@TenantRequired`가 **없는** 엔드포인트는 테넌트 유무와 관계없이 요청을 허용합니다.

### `TenantId` 인자 리졸버

컨트롤러 메서드에 현재 테넌트를 직접 주입:

```java
@GetMapping("/orders")
public List<Order> getOrders(TenantId tenantId) {
    log.info("테넌트 {} 의 주문 조회", tenantId.value());
    return orderRepository.findAll();
}
```

### MDC 로깅

테넌트 ID가 SLF4J MDC에 자동 주입됩니다. 로그 패턴에 `%X{tenantId}`를 추가하세요:

```yaml
logging:
  pattern:
    console: "%d{HH:mm:ss} [%X{tenantId}] %-5level %logger{36} - %msg%n"
```

```
14:23:45 [tenant-a] INFO  OrderService - 새 주문 생성
14:23:45 [tenant-a] INFO  OrderRepository - findAll 쿼리 실행
```

---

## 아키텍처

```
HTTP Request (X-Tenant-ID: tenant-a)
       |
  TenantContextFilter          resolve -> validate -> set ThreadLocal
       |
  TenantMdcFilter              MDC.put("tenantId", "tenant-a")
       |
  TenantValidationInterceptor  @TenantRequired 검사
       |
  Controller                   TenantId 인자 주입
       |
  +----+----+
  |         |
  JPA     MyBatis
  |         |
  Aspect    Interceptor
  |         |
  Session   JSqlParser
  .enable   AST 변환
  Filter()  SQL
  |         |
  WHERE tenant_id = ?
       |
  Response
       |
  finally { TenantContextHolder.clear() }
```

### 요청 흐름

1. **TenantContextFilter** (order: -100) 헤더/JWT에서 테넌트 식별, 포맷 검증, `InheritableThreadLocal`에 저장
2. **TenantMdcFilter** (order: -99) SLF4J MDC에 테넌트 복사
3. **TenantValidationInterceptor** 핸들러의 `@TenantRequired` 검사
4. **Controller** 인자 리졸버를 통해 `TenantId` 주입
5. **JPA**: `TenantHibernateFilterAspect`가 모든 `JpaRepository` 호출 전 Hibernate `@Filter` 활성화
6. **MyBatis**: `TenantMyBatisInterceptor`가 JSqlParser AST로 SQL 파싱 및 변환
7. **정리**: finally 블록에서 `TenantContextHolder.clear()` 및 `MDC.remove()` 실행

### 조건부 자동 구성

| 구성 클래스 | 활성화 조건 | 등록하는 빈 |
|------------|-----------|-----------|
| `TenantWebAutoConfig` | Servlet 웹 앱 | Filter, Interceptor, Resolver, ArgumentResolver |
| `TenantJpaAutoConfig` | classpath에 `EntityManager` 존재 | Hibernate 필터 Aspect |
| `TenantMyBatisAutoConfig` | classpath에 MyBatis `Interceptor` 존재 | SQL 인터셉터 |
| `TenantLoggingAutoConfig` | Servlet 웹 앱 | MDC 필터 |

JPA만, MyBatis만, 또는 둘 다 사용 가능합니다. 스타터가 classpath에 맞춰 자동으로 적응합니다.

---

## 설정 레퍼런스

```yaml
tenant:
  enabled: true                          # 스타터 전체 활성화/비활성화

  resolver:
    strategy: HEADER                     # HEADER | JWT
    header-name: X-Tenant-ID            # HTTP 헤더명
    jwt-claim-name: tenant_id           # JWT claim 키

  filter:
    exclude-paths:                       # 테넌트 식별을 건너뛸 경로
      - /actuator/**
      - /health
    order: -100                          # 서블릿 필터 순서

  id:
    max-length: 64                       # 테넌트 ID 최대 길이
    pattern: "^[a-zA-Z0-9_-]+$"         # 테넌트 ID 허용 문자 패턴

  logging:
    mdc-key: tenantId                    # SLF4J MDC 키 이름
```

---

## 커스터마이징

### 커스텀 테넌트 리졸버

```java
@Bean
public TenantResolver customResolver() {
    return request -> {
        String tenantId = request.getHeader("X-Custom-Tenant");
        return Optional.ofNullable(tenantId)
            .filter(s -> !s.isBlank())
            .map(TenantId::new);
    };
}
```

### 커스텀 테넌트 ID 검증기

```java
@Bean
public TenantIdValidator customValidator(TenantProperties properties) {
    return new TenantIdValidator(properties) {
        @Override
        public void validate(TenantId tenantId) {
            super.validate(tenantId);
            // 추가 검증: DB에서 유효한 테넌트인지 확인
        }
    };
}
```

### 프로그래밍 방식 테넌트 설정 (배치 작업, 메시징)

```java
TenantContextHolder.setContext(new TenantContext(new TenantId("tenant-a")));
try {
    // 모든 DB 쿼리가 tenant-a 기준으로 필터링
    orderRepository.findAll();
} finally {
    TenantContextHolder.clear();
}
```

---

## 에러 처리

| 예외 | HTTP 상태 | 발생 시점 |
|-----|----------|----------|
| `TenantNotFoundException` | 404 | 테넌트 컨텍스트가 필요하지만 설정되지 않음 |
| `TenantNotValidException` | 400 | 테넌트 ID 검증 실패 또는 `@TenantRequired` 없이 요청 |

응답 형식:
```json
{
  "error": "TENANT_NOT_VALID",
  "message": "Tenant ID contains invalid characters. Allowed pattern: ^[a-zA-Z0-9_-]+$"
}
```

---

## 설계 결정

| 결정 | 이유 |
|-----|-----|
| `InheritableThreadLocal` | Spring Security의 `SecurityContextHolder`와 동일한 패턴. `@Async` 자식 스레드에 자동 전파 |
| Hibernate `@Filter` + AOP Aspect | `@Filter`는 Hibernate 공식 메커니즘. AOP로 트랜잭션 내 올바른 Session에서 필터 활성화 보장 |
| MyBatis에 JSqlParser | AST 기반 SQL 변환으로 JOIN, UNION, 서브쿼리 정확히 처리. MyBatis-Plus `TenantLineInnerInterceptor`와 동일한 접근 방식 |
| `tenant_id` 컬럼명 고정 | JPA `@Column`, `@Filter` 어노테이션은 컴파일 타임 상수만 허용. Convention over Configuration |
| `@TenantRequired` opt-in 방식 | 모든 엔드포인트가 테넌트를 필요로 하지 않음 (헬스체크, 공개 API). 명시적 어노테이션으로 의도치 않은 차단 방지 |
| PreparedStatement 파라미터 바인딩 | SQL Injection 방지. 정규식 검증과 결합한 다중 방어 |
| `@ConditionalOnMissingBean` | 모든 빈을 오버라이드 가능. 스타터는 합리적인 기본값을 제공하되 제약하지 않음 |

---

## 패키지 구조

```
io.github.iamjunhyeok.multitenant
├── core/           TenantId, TenantContext, TenantContextHolder, TenantIdValidator
├── config/         자동 구성 클래스 + TenantProperties
├── constant/       TenantConstants, TenantResolverStrategy
├── resolver/       TenantResolver 인터페이스 + Header/JWT 구현체
├── web/            Filter, Interceptor, ArgumentResolver, @TenantRequired, ExceptionHandler
├── jpa/            TenantAwareEntity, TenantEntityListener
├── aop/            TenantHibernateFilterAspect
├── mybatis/        TenantMyBatisInterceptor, TenantSqlModifier
├── logging/        TenantMdcFilter
└── exception/      TenantNotFoundException, TenantNotValidException
```

## 기술 스택

| 구성 요소 | 버전 |
|----------|------|
| Spring Boot | 3.5.x |
| Hibernate | 6.6.x |
| MyBatis | 3.5.x |
| JSqlParser | 5.3 |
| AspectJ | 1.9.x |
| Jakarta Servlet | 6.1 |
| Java | 21+ |

## 라이선스

MIT
