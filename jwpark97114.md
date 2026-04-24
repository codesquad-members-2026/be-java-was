# 코드 리뷰: jwpark97114 브랜치

**리뷰어:** honux@codesquad.kr
**리뷰 날짜:** 2026-04-24
**브랜치:** `origin/jwpark97114`

---

## 요약

HTTP 파싱부터 커스텀 템플릿 엔진, DB 커넥션 풀, 세션 관리, 어노테이션 기반 라우팅까지 WAS의 핵심 요소들을 직접 구현한 수준 높은 프로젝트입니다. Spring Framework가 내부적으로 처리해 주는 많은 것들을 손수 구현했다는 점에서, HTTP 프로토콜과 웹 프레임워크 동작 원리에 대한 깊은 이해를 보여주고 있습니다. Virtual Thread 도입, BCrypt 패스워드 해싱, 직접 구현한 커넥션 풀 등 실무적인 고려도 인상적입니다.

---

## 1. 전체적인 구조와 설계

### 패키지 구조

```
webserver/          - 핵심 서버 컴포넌트 (WebServer, Router, RequestHandler)
  handlers/         - 요청 핸들러 클래스들
  scanner/          - 어노테이션 스캔 및 핸들러 등록
  session/          - 세션 관리
  template/         - 커스텀 템플릿 엔진 (Jhymeleaf)
jhttp/              - HTTP 요청/응답 파싱 및 추상화
db/                 - 데이터베이스 레이어 (커넥션 풀, DAO)
model/              - 도메인 모델 (User, Article)
auth/               - 인증 유틸리티
fileIO/             - 정적 파일 로딩
annotations/        - 커스텀 어노테이션
interfaces/         - 핸들러 인터페이스
```

전체 아키텍처는 MVC 패턴을 잘 따르고 있습니다. `Router`가 Front Controller 역할을 하고, `*Handlers` 클래스들이 Controller, `Jhymeleaf`가 View, `model` 패키지가 Model을 담당합니다. Spring MVC의 동작 원리를 직접 구현해봤다는 점에서 학습 효과가 매우 높습니다.

---

## 2. 잘한 점

### 어노테이션 기반 라우팅 시스템 구현

Spring의 `@RequestMapping`에서 영감을 받아 직접 어노테이션과 리플렉션 기반 디스패처를 구현한 것은 이 프로젝트의 가장 인상적인 부분입니다.

```java
// ComponentScannerWithoutGemini.java
HandlerMethod h = (req, res, sessionManager, ta, db) -> {
    Object[] args = new Object[params.length];
    for(int i=0; i < params.length; i++){
        if(params[i].getSimpleName().equals("HttpRequest")){
            args[i] = req;
        } else if(params[i].getSimpleName().equals("Session")){
            String sid = req.getSessionID();
            Session session = sessionManager.getSession(sid);
            args[i] = session;
        }
        // ...
    }
    return m.invoke(handlerInstance, args);
};
```

핸들러 메서드의 파라미터 타입을 런타임에 분석하여 적절한 의존성을 주입하는 방식은 Spring의 `HandlerMethodArgumentResolver` 개념과 정확히 일치합니다.

### Virtual Thread 적용

```java
// WebServer.java
private static final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();
```

Java 21의 Virtual Thread를 적용하고, 주석으로 그 이유(I/O 바운드 작업에서의 처리량 향상)까지 설명한 점이 좋습니다.

### 직접 구현한 커넥션 풀

```java
// ConnectionPool.java
public Connection getConnection(){
    lock.lock();
    try{
        while(connPool.isEmpty()){
            qAvailabilityCond.await();
        }
        return connPool.poll();
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Thread is interrupted while waiting for connPool", e);
    } finally {
        lock.unlock();
    }
}
```

`ReentrantLock`과 `Condition`을 사용하여 스레드 안전한 커넥션 풀을 직접 구현했습니다. `InterruptedException` 발생 시 `Thread.currentThread().interrupt()`를 호출하여 인터럽트 상태를 복원하는 올바른 패턴도 잘 지켜졌습니다.

### 커스텀 템플릿 엔진 (Jhymeleaf)

정규식을 활용해 `{{jh#if}}`, `{{jh#ifNot}}`, `{{jh#each}}`, `{{ variable }}` 문법을 지원하는 템플릿 엔진을 구현한 것은 매우 창의적입니다. `each` 블록 내에서 리플렉션으로 getter를 호출하여 객체 필드에 접근하는 방식은 실제 템플릿 엔진의 동작 원리와 유사합니다.

### BCrypt 패스워드 해싱

```java
// JUserAuth.java
public static String hashPassword(String userInput){
    String salt = BCrypt.gensalt();
    return BCrypt.hashpw(userInput, salt);
}
```

평문 비밀번호를 저장하는 대신 BCrypt를 사용하여 보안을 고려한 점이 좋습니다.

### 테스트 코드의 품질

- `given / when / then` 패턴 일관성 있게 사용
- `@DisplayName`으로 테스트 의도를 명확하게 표현
- Mockito의 `MockedStatic`을 적절히 활용하여 정적 메서드 의존성을 격리

### DBInterface를 통한 의존성 역전

```java
public class DBEntryPoint {
    private final DBInterface dbManager;
    public DBEntryPoint(DBInterface dbManager){ ... }
}
```

구체 클래스(`CodestagramDBManager`) 대신 인터페이스(`DBInterface`)에 의존하여 테스트 용이성과 교체 가능성을 확보한 것은 좋은 설계 원칙(DIP)의 적용입니다.

---

## 3. 개선이 필요한 부분

### 3-1. [심각] 하드코딩된 DB 자격 증명

**파일:** `src/main/java/db/CodestagramDBManager.java`

```java
// 현재 코드 - 절대 커밋하면 안 됨!
private static final String url = "jdbc:h2:file:~/Desktop/h2db/codestagram";
private static final String user = "jon";
private static final String password = "0000";
```

DB URL, 사용자명, 비밀번호가 소스 코드에 하드코딩되어 있습니다. 이는 심각한 보안 취약점으로, Git에 커밋되면 누구나 볼 수 있습니다.

**개선 방법:** 환경 변수 또는 설정 파일(`.properties`)을 사용하고, `.gitignore`에 추가하세요.

```java
// 개선 예시 (환경 변수 사용)
private static final String url = System.getenv().getOrDefault("DB_URL", "jdbc:h2:mem:test");
private static final String user = System.getenv().getOrDefault("DB_USER", "sa");
private static final String password = System.getenv("DB_PASSWORD");
```

### 3-2. [중요] `try-with-resources`로 리소스 관리 개선 필요

**파일:** `src/main/java/db/DBEntryPoint.java` - 모든 메서드

현재는 `finally` 블록에서 수동으로 `PreparedStatement`와 `ResultSet`을 닫고 있는데, `try-with-resources`를 사용하면 훨씬 간결하고 안전합니다.

```java
// 현재 코드
Connection conn = null;
PreparedStatement preppedStmt = null;
try {
    conn = dbManager.getConnection();
    preppedStmt = conn.prepareStatement(sql);
    // ...
} finally {
    if(preppedStmt != null) {
        try { preppedStmt.close(); } catch(SQLException e) { ... }
    }
    if(conn != null) { dbManager.returnConnection(conn); }
}

// 개선 방법
try (Connection conn = dbManager.getConnection();
     PreparedStatement preppedStmt = conn.prepareStatement(sql)) {
    // ...
}
```

### 3-3. [중요] 클래스 명명 규칙 위반

**파일:** `src/main/java/webserver/handlers/articleHandlers.java`

Java 컨벤션에서 클래스 이름은 반드시 대문자로 시작해야 합니다. `articleHandlers` → `ArticleHandlers`로 변경하세요. IDE와 빌드 도구에서 혼란을 일으킬 수 있습니다.

### 3-4. [중요] `RouterTest`와 실제 코드의 불일치

**파일:** `src/test/java/webserver/RouterTest.java`

`RouterTest`에서 `Router` 생성자를 2개 파라미터로 호출하고 있지만, 실제 `Router` 클래스는 3개 파라미터(DBEntryPoint 포함)를 요구합니다.

```java
// RouterTest.java - 실제로 컴파일/실행되지 않는 코드
router = new Router(mockHandlers, mockSessionManager);  // 2개 파라미터

// Router.java - 실제 생성자
public Router(Map<String, HandlerMethod> injectedRequestHandlers, SessionManager sm, DBEntryPoint db){ ... }
```

### 3-5. [중요] `LoginHandlersTest`의 구 버전 API 사용

**파일:** `src/test/java/webserver/handlers/LoginHandlersTest.java`

`LoginHandlersTest`가 `Database.findUserById()`라는 이전 인메모리 DB 방식을 mock하고 있지만, 실제 `LoginHandlers`는 `DBEntryPoint`를 파라미터로 받아 사용합니다. 이 테스트는 현재 실제 코드와 전혀 다른 API를 테스트하고 있어, 사실상 동작하지 않는 테스트입니다.

`DBEntryPoint`를 mock으로 주입하는 방식으로 테스트를 재작성해야 합니다.

### 3-6. [중요] 정적 파일 처리 시 `return` 누락으로 인한 버그

**파일:** `src/main/java/webserver/Router.java`, `returnStaticFiles` 메서드

```java
private void returnStaticFiles(HttpRequest request, HttpResponse response) {
    if(!request.getMethod().equals("GET")){
        response.setStatus("400 Bad Request");
        logger.error("...");
        response.send();
    }
    // return이 없어서 GET이 아닌 경우에도 아래 코드가 계속 실행됨!
    String url = request.getUrl();
    // ...
}
```

**수정 방법:**
```java
if(!request.getMethod().equals("GET")){
    response.setStatus("400 Bad Request");
    response.send();
    return; // <-- 반드시 필요
}
```

### 3-7. [보통] `MainPageHandlers.getFrontPage`에서 `User.getName()` vs `User.getUserId()` 불일치

**파일:** `src/main/java/webserver/handlers/MainPageHandlers.java` vs `UserHandlers.java`

```java
// MainPageHandlers.java
templateAttributes.setAttribute("userID", ((User)session.getAttribute("user")).getName());

// UserHandlers.java
attributes.setAttribute("userID", ((User)session.getAttribute("user")).getUserId());
```

같은 `userID` 키에 한 곳은 `getName()`(닉네임), 다른 곳은 `getUserId()`(로그인 ID)를 저장하고 있습니다. 어떤 값을 보여줄지 의도를 통일해야 합니다.

### 3-8. [보통] `processVar`에서 unchecked 캐스팅

**파일:** `src/main/java/webserver/template/Jhymeleaf.java`

```java
value = (String) target;  // Object를 String으로 강제 캐스팅 - ClassCastException 위험
```

`TemplateAttributes`의 `getAttribute`는 `Object`를 반환하는데, `Integer`나 `List` 등 다른 타입이 들어올 경우 `ClassCastException`이 발생합니다.

```java
// 개선 방법
value = (target != null) ? target.toString() : "";
```

### 3-9. [보통] `HttpResponse.send()`에서 `e.printStackTrace()` 사용

```java
// 다른 클래스에서는 logger를 잘 사용하는데, 여기만 예외
catch (IOException e) {
    e.printStackTrace();  // logger.error("Failed to send response", e) 로 변경 필요
}
```

### 3-10. [보통] `TemplateAttributes`의 접근 제어자

**파일:** `src/main/java/model/TemplateAttributes.java`

```java
public class TemplateAttributes {
    Map<String, Object> attributes = new HashMap<>();  // package-private! private이어야 함
```

---

## 4. 코드 품질

### 네이밍 개선 필요

- `preppedStmt` → `preparedStmt` 또는 `pstmt` (`DBEntryPoint.java` 전반)
- `foreachPatter` → `foreachPattern` (오타, `Jhymeleaf.java`)
- 파일명 `SessionMangerTest.java` → `SessionManagerTest.java` (오타)

### 중복 코드

`DBEntryPoint.java`의 각 메서드에서 JDBC 자원 관리 코드가 반복됩니다. 콜백 패턴으로 추상화하면 중복을 크게 줄일 수 있습니다.

```java
// 개선 예시: 콜백 패턴으로 JDBC 보일러플레이트 추상화
@FunctionalInterface
interface JdbcOperation<T> {
    T execute(Connection conn) throws SQLException;
}

private <T> T execute(JdbcOperation<T> operation) {
    Connection conn = dbManager.getConnection();
    try {
        return operation.execute(conn);
    } catch (SQLException e) {
        throw new RuntimeException(e);
    } finally {
        dbManager.returnConnection(conn);
    }
}
```

---

## 5. 학습 포인트

### 직접 구현한 것과 Spring Framework 비교

| 직접 구현 | Spring 대응 개념 |
|---|---|
| `ComponentScannerWithoutGemini` | `DispatcherServlet` + `RequestMappingHandlerMapping` |
| `HandlerMethod` 인터페이스 | `HandlerMethod` 클래스 + `HandlerMethodArgumentResolver` |
| `SessionManager` | `HttpSession` 관리 |
| `Jhymeleaf` | `ThymeleafViewResolver` |
| `ConnectionPool` | `HikariCP` / `DataSource` |
| `DBEntryPoint` | `JdbcTemplate` / `Repository` |

### `try-with-resources`와 `AutoCloseable` 심화

`ConnectionPool`의 커넥션을 자동 반환하는 래퍼 클래스를 만들어 보세요.

```java
class ManagedConnection implements AutoCloseable {
    private final Connection conn;
    private final ConnectionPool pool;
    // ...
    @Override
    public void close() { pool.returnConnection(conn); }
}
```

이렇게 하면 `try-with-resources`로 커넥션을 자동 반납할 수 있습니다.

### 환경 설정 분리 패턴

`application.properties` 파일을 읽어 설정을 주입하는 간단한 설정 관리자를 만들어 보면, Spring의 `@Value`와 `@ConfigurationProperties`가 어떻게 동작하는지 이해할 수 있습니다.

### 테스트 피라미드와 통합 테스트

현재 단위 테스트는 잘 되어 있습니다. 다음 단계로 실제 H2 인메모리 DB를 띄워 `DBEntryPoint`를 테스트하는 통합 테스트를 작성해 보세요.

### HTTP 상태 코드 `enum`으로 리팩토링

현재 `"302 Found"`, `"200 OK"` 등의 문자열이 코드 곳곳에 산재해 있습니다. `enum`으로 표준화하면 오타를 방지하고 코드 가독성을 높일 수 있습니다.

```java
public enum HttpStatus {
    OK(200, "OK"),
    FOUND(302, "Found"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found");

    private final int code;
    private final String phrase;
    // ...
}
```

### 보안 강화 - CSRF 방어

로그인/로그아웃/게시글 작성 등 상태를 변경하는 `POST` 요청에 CSRF 토큰 검증을 추가해 보세요. 이는 실무에서 필수적인 웹 보안 개념입니다.

---

## 종합 평가

WAS를 밑바닥부터 구현하는 프로젝트에서 단순한 기능 구현을 넘어 어노테이션 기반 디스패처, 커스텀 템플릿 엔진, 직접 구현한 커넥션 풀 등 프레임워크 수준의 컴포넌트들을 만들어낸 것은 높이 평가할 만합니다. 테스트 코드가 `given/when/then` 패턴과 `@DisplayName`을 일관되게 사용하고, `MockedStatic`을 통해 정적 메서드까지 격리한 점은 테스트 코드 작성 능력이 이미 상당한 수준임을 보여줍니다.

지금 가장 중요하게 해결해야 할 것은 **테스트와 실제 코드의 불일치** 문제입니다. `LoginHandlersTest`와 `RegistrationHandlersTest`가 현재 실제 코드를 테스트하지 않고 있어, 리팩토링 시 회귀 버그를 잡아낼 수 없는 상태입니다. 이 부분을 우선적으로 수정하고, `RouterTest`의 생성자 불일치도 함께 해결하면 코드 품질이 한 단계 더 높아질 것입니다.
