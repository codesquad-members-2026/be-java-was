# 코드 리뷰: gabi 브랜치

**리뷰어:** honux@codesquad.kr
**리뷰 날짜:** 2026-04-24
**브랜치:** `origin/gabi`

---

## 요약

직접 HTTP 파싱부터 커스텀 템플릿 엔진, 어노테이션 기반 라우팅, 세션 관리까지 WAS의 핵심 구성요소를 직접 구현한 인상적인 프로젝트입니다. Spring MVC의 구조를 직접 모방해가며 핵심 원리를 학습한 흔적이 곳곳에 보이며, 테스트 코드도 꼼꼼하게 작성되어 있습니다. 전반적으로 높은 완성도를 보여주지만 몇 가지 개선 포인트가 있어 함께 살펴봅니다.

---

## 1. 전체적인 구조와 설계

패키지 구조가 레이어별로 잘 분리되어 있습니다.

```
webserver/
  annotation/        - @GetMapping, @PostMapping
  exception/         - 도메인 예외
  http/              - HttpRequest, HttpResponse, HttpRequestParser
  resource/          - 정적 리소스 로딩
  response/          - 렌더러, 템플릿 엔진
    template/        - 토크나이저, 파서, 빌더
  servlet/           - DispatcherServlet, HandlerMappings
    exception/       - ExceptionResolver
    handler/         - HandlerMethod
  session/           - Session, Cookie, SessionManager
handler/             - 사용자 핸들러
```

Spring MVC의 아키텍처를 모방한 구조로, `DispatcherServlet → HandlerMappings → HandlerMethod` 흐름이 실제 Spring의 동작 방식과 매우 유사합니다. 이 구조를 직접 구현해본 것 자체가 큰 학습 성과입니다.

---

## 2. 잘한 점

### 커스텀 템플릿 엔진 구현

Mustache와 유사한 템플릿 엔진을 직접 구현한 점이 가장 인상적입니다. `Token`을 sealed interface + record로 정의하여 타입 안전성을 확보하고, 패턴 매칭 switch를 활용한 구현이 매우 현대적입니다.

```java
// Token.java - sealed interface + record 조합이 깔끔하다
public sealed interface Token {
    record Text(String context) implements Token {}
    record Variable(String key) implements Token {}
    record SectionStart(String key, boolean inverted) implements Token {}
    record SectionEnd(String key) implements Token {}
}

// TemplateBuilder.java - 패턴 매칭 switch 활용
switch (token) {
    case Text text -> { sb.append(text.context()); i++; }
    case Variable variable -> { ... }
    case SectionStart section -> { ... }
    case SectionEnd sectionEnd -> { ... }
}
```

Java 17+ 문법을 적극 활용하여 코드가 간결하고 읽기 쉽습니다.

### 계층화된 예외 처리

`RequestHandler → ServletManager → ExceptionResolver` 로 이어지는 예외 처리 계층이 잘 설계되어 있습니다. 에러 페이지 렌더링 자체가 실패했을 때를 위한 `writeFailsafeResponse`까지 준비한 점이 꼼꼼합니다.

```java
// ServletManager.java - 2중 안전망
private void handleError(HttpRequest request, HttpResponse response, Exception e) {
    try {
        exceptionResolver.resolve(request, response, e);
    } catch (Exception exception) {
        writeFailsafeResponse(response); // 에러 페이지 렌더링마저 실패한 경우
    }
}
```

### 풍부하고 명확한 테스트 코드

`@DisplayName`을 이용한 한국어 테스트 설명, `@Nested`로 계층화된 테스트 구조, AssertJ 활용 등 테스트 코드의 품질이 높습니다. 특히 `SessionManagerTest`의 `cookieRoundTripThroughRawBytes`처럼 실제 HTTP 바이트 흐름을 end-to-end로 검증하는 테스트는 인상적입니다.

### Virtual Thread 활용

```java
// WebServer.java
ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()
```

Java 21의 Virtual Thread를 적용하여 I/O 블로킹에 강한 서버를 구성한 점이 좋습니다.

### `HttpResponse`의 버퍼 기반 설계

응답을 `ByteArrayOutputStream`에 버퍼링했다가 `flush()` 시 한 번에 전송하는 방식은 Content-Length를 정확히 계산할 수 있게 해주며, `reset()` 기능을 통해 에러 발생 시 응답을 초기화하는 것도 실용적입니다.

---

## 3. 개선이 필요한 부분

### 3-1. `ResourceLoader`의 하드코딩된 경로

**파일:** `src/main/java/webserver/resource/ResourceLoader.java`

```java
// 현재 코드 - 상대 경로 하드코딩
File file = new File("src/main/resources/static" + resourcePath);
```

이 코드는 서버를 프로젝트 루트 디렉토리에서 실행할 때만 동작합니다. IDE, JAR 실행, 다른 디렉토리에서 실행하면 파일을 찾지 못합니다. 클래스패스(classpath)를 이용하는 방식으로 변경해야 합니다.

```java
// 개선 방법
public byte[] loadAsBytes(String resourcePath) throws IOException {
    InputStream in = getClass().getResourceAsStream("/static" + resourcePath);
    if (in == null) {
        throw new PageNotFoundException("리소스를 찾을 수 없음: " + resourcePath);
    }
    try (in) {
        return in.readAllBytes();
    }
}
```

### 3-2. `isStaticResource` 판별 로직의 중복과 불완전성

**파일:** `src/main/java/webserver/servlet/ServletManager.java`

```java
// 현재 코드 - 확장자를 문자열로 직접 나열
private boolean isStaticResource(HttpRequest request) {
    String method = request.getMethod();
    String path = request.getPath();
    // todo: 확장자 분리하기
    return method.equals("GET") && (path.endsWith(".html") || path.endsWith(".css")
            || path.endsWith(".ico") || path.endsWith(".svg"));
}
```

이미 `Mime` enum에 지원하는 확장자 목록이 있는데, `ServletManager`에서 별도로 문자열 비교를 하고 있습니다. `.js`, `.png`, `.jpg` 등 `Mime`에 있는 타입이 빠져 있어 해당 파일 요청은 정적 리소스로 처리되지 않습니다.

```java
// 개선 방법 - Mime enum을 재활용
private boolean isStaticResource(HttpRequest request) {
    if (!request.getMethod().equals("GET")) return false;
    String path = request.getPath();
    int dotIdx = path.lastIndexOf('.');
    if (dotIdx == -1) return false;
    String ext = path.substring(dotIdx + 1).toUpperCase();
    return Mime.getMime(ext) != Mime.DEFAULT;
}
```

### 3-3. `HandlerMethod`의 필드가 불필요하게 가변(non-final)

**파일:** `src/main/java/webserver/servlet/handler/HandlerMethod.java`

```java
// 현재 코드 - final 없음
public class HandlerMethod {
    private Object handler;  // final이어야 함
    private Method method;   // final이어야 함
```

`handler`와 `method`는 생성 후 변경될 이유가 없습니다. `final`로 선언하여 불변성을 보장해야 합니다.

### 3-4. `DispatcherServlet`의 예외 메시지 누락

**파일:** `src/main/java/webserver/servlet/DispatcherServlet.java`

```java
// 현재 코드 - 어떤 경로인지 메시지에 포함되지 않음
throw new PageNotFoundException("페이지를 찾을 수 없음: ");

// 개선 방법
throw new PageNotFoundException("페이지를 찾을 수 없음: " + httpMethod + " " + path);
```

### 3-5. `Session`의 내부 `Map`이 스레드 안전하지 않음

**파일:** `src/main/java/webserver/session/Session.java`

`SessionManager`는 `ConcurrentHashMap`을 쓰고 있지만, `Session` 내부의 `sessionStore`는 일반 `HashMap`입니다. Virtual Thread 환경에서 동일 세션에 동시 접근이 발생하면 데이터 경합이 생길 수 있습니다.

```java
// 현재 코드
private final Map<String, Object> sessionStore = new HashMap<>();

// 개선 방법
private final Map<String, Object> sessionStore = new ConcurrentHashMap<>();
```

### 3-6. `HandlerMappings`가 GET, POST만 지원하도록 하드코딩

**파일:** `src/main/java/webserver/servlet/handler/HandlerMappings.java`

```java
// 현재 코드 - 생성자에서 GET, POST만 등록
public HandlerMappings(List<Object> handlers) {
    methodMap.put("GET", new HashMap<>());
    methodMap.put("POST", new HashMap<>());
    initialize(handlers);
}
```

PUT, DELETE 등의 HTTP 메서드를 추가하려면 생성자를 수정해야 합니다.

```java
// 개선 방법 - 동적으로 등록
private void register(String httpMethod, String path, Object handler, Method method) {
    methodMap.computeIfAbsent(httpMethod, k -> new HashMap<>())
             .put(path, new HandlerMethod(handler, method));
}
```

### 3-7. `UserHandler`에서 비밀번호 평문 비교

**파일:** `src/main/java/handler/UserHandler.java`

```java
if (user == null || !user.getPassword().equals(password)) {
```

학습 프로젝트이지만, 비밀번호를 평문으로 저장하고 비교하는 패턴은 지양하는 것이 좋습니다. 실제 프로젝트라면 BCrypt 등의 해시를 사용해야 한다는 점을 인지하고 있으면 좋겠습니다.

### 3-8. `DatabaseTest`에서 리플렉션으로 DB 초기화

**파일:** `src/test/java/db/DatabaseTest.java`

```java
@BeforeEach
void clearDatabase() throws Exception {
    Field field = Database.class.getDeclaredField("users");
    field.setAccessible(true);
    ((Map<?, ?>) field.get(null)).clear();
}
```

`Database` 클래스가 `clearForTest()` 같은 패키지 접근 수준의 메서드를 제공하거나, `Database`를 싱글턴 static 클래스가 아닌 인스턴스로 변경하고 DI(의존성 주입)를 통해 테스트 시 새 인스턴스를 넘기는 방식도 고려해볼 만합니다.

### 3-9. `ContextLookup`의 `getField` vs `getDeclaredField`

**파일:** `src/main/java/webserver/response/template/ContextLookup.java`

```java
Field field = context.getClass().getField(key); // public 필드만 찾음
```

`getField`는 `public` 필드만 탐색합니다. 일반적으로 도메인 객체의 필드는 `private`이므로 사실상 작동하지 않습니다. `getDeclaredField`를 쓰고 `setAccessible(true)`를 호출하거나, getter 탐색 실패 시 명확한 오류를 내는 것이 더 명확합니다.

---

## 4. 코드 품질

### 잘 된 점
- 인터페이스(`HttpServlet`, `ResponseRenderer`, `ExceptionResolver`) 도입으로 교체 가능성(OCP)이 좋습니다.
- `Cookie`를 record로 선언하여 불변 값 객체로 만든 점이 좋습니다.
- `HttpResponse`의 `CRLF` 상수 추출 등 매직 스트링 제거가 잘 되어 있습니다.
- TODO 주석으로 알고 있는 개선 사항을 표시해둔 점이 좋습니다.

### 개선이 필요한 점

**`TemplateData`의 `toMap()` 불변성 미보장:**
`toMap()` 메서드는 내부 `Map`의 참조를 그대로 반환합니다. 외부에서 수정하면 내부 상태가 바뀔 수 있으므로 `Collections.unmodifiableMap(map)` 또는 `Map.copyOf(map)`으로 반환하는 것이 안전합니다.

**`Mime` enum의 `contentType` 필드가 `final`이 아님:**

```java
// Mime.java
private String contentType; // final이어야 함
```

enum 상수의 필드는 보통 `final`로 선언합니다.

---

## 5. 학습 포인트

이 프로젝트를 통해 한 단계 더 성장할 수 있는 방향을 제안합니다.

### 의존성 주입(DI) 컨테이너 구현

현재 `WebServer.main()`에서 `new UserHandler()`를 직접 생성해 주입합니다. `@Component` 어노테이션을 만들고 컴포넌트 스캔으로 자동 등록하는 간단한 DI 컨테이너를 직접 구현해보면 Spring의 핵심을 더 깊이 이해할 수 있습니다.

### HTTP 메서드의 `String` → `enum` 전환

현재 코드에 `// todo: enum 고려` 주석이 있습니다. `HttpMethod` enum을 만들어 적용해보세요. 잘못된 메서드 문자열이 들어올 때 파싱 시점에 즉시 오류를 낼 수 있고, switch 표현식과 잘 어울립니다.

### 경로 변수(Path Variable) 지원

현재는 `/user/list`처럼 정확한 경로만 매칭됩니다. `/user/{id}`처럼 변수를 포함한 경로를 지원하려면 `HandlerMappings`의 탐색 로직을 패턴 매칭으로 확장해야 합니다. 이를 구현해보면 라우팅의 복잡성을 체감할 수 있습니다.

### 로그아웃 기능 구현

`index.html`에 로그아웃 버튼(`/user/logout`)이 있지만 핸들러가 없습니다. 세션을 무효화하고 쿠키를 만료시키는 로그아웃 기능을 추가해보세요. `Max-Age=0`을 사용하여 쿠키를 삭제하는 HTTP 표준 방법을 학습할 수 있습니다.

### `TemplateEngine`의 XSS 방어

현재 `ContextLookup.lookup()`이 반환한 값을 `sb.append(lookup)`으로 그대로 출력합니다. 사용자 입력에 `<script>` 같은 HTML 특수문자가 포함되면 XSS 취약점이 생깁니다. `&`, `<`, `>`, `"`, `'`를 HTML entity로 이스케이프하는 처리를 추가해보세요.

---

전반적으로 WAS의 핵심 원리를 직접 손으로 구현하면서 깊이 이해한 훌륭한 프로젝트입니다. 특히 커스텀 템플릿 엔진 구현과 세션-쿠키 흐름의 end-to-end 테스트는 눈에 띄는 성과입니다. 위의 개선 포인트들을 하나씩 적용해가면서 더 견고한 코드를 만들어 가시길 바랍니다.
