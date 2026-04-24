# 코드 리뷰: LeeWanJa 브랜치

**리뷰어:** honux@codesquad.kr
**리뷰 날짜:** 2026-04-24
**브랜치:** `origin/LeeWanJa`

---

## 요약

직접 Java로 HTTP WAS를 구현하는 매우 도전적인 과제를 잘 수행했습니다. 단순한 소켓 서버 수준을 넘어, **레이어드 아키텍처**를 의식하며 패키지를 나누고, `record`, Virtual Thread 등 **최신 Java 21 기능**을 적극적으로 활용한 점이 인상적입니다. 특히 코드 곳곳에 남겨진 `TODO` 주석들이 단순히 "나중에 하겠다"는 미루기가 아닌, **스스로 왜(Why)를 질문하고 있다는 증거**여서 학습 태도 측면에서 매우 좋습니다.

전체적으로 완성도가 높지만, 몇 가지 **버그 및 보안 취약점**, 그리고 **설계 개선 포인트**가 있어 아래에 상세히 기술합니다.

---

## 1. 전체적인 구조와 설계

### 패키지 구조

```
src/main/java/
├── app/
│   ├── action/          # 비즈니스 로직 (Controller 역할)
│   └── user/            # 도메인 모델
├── core/
│   ├── http/            # HTTP 상수, MIME, 상태코드
│   ├── request/         # HTTP 요청 파싱
│   ├── response/        # HTTP 응답 생성
│   ├── routing/         # 라우팅
│   ├── session/         # 세션 관리
│   ├── view/            # 파일 리졸버, 템플릿 엔진
│   └── webserver/       # 서버 진입점
├── db/                  # 인메모리 DB
├── exception/           # 커스텀 예외
└── file/                # 파일 I/O
```

**핵심 요청 처리 흐름:**

```
Socket → RequestHandler → HttpRequest(파싱)
                        → Router(라우팅) → Action(비즈니스 로직)
                        → ResourceResolver(파일 경로 검증)
                        → WanjaTemplateEngine(템플릿 렌더링)
                        → HttpResponseBuilder → HttpResponse → OutputStream
```

`core` 패키지가 인프라/프레임워크 계층, `app` 패키지가 애플리케이션 계층 역할을 하는 의도는 명확합니다. 이는 Spring MVC의 구조와 대응되며, 직접 만들어보면서 "왜 프레임워크가 이렇게 설계되어 있는지"를 체감하기에 매우 좋은 구조입니다.

---

## 2. 잘한 점

### 2-1. Java 21 최신 기능의 적절한 활용

`record`를 적극 활용하여 불변 값 객체를 간결하게 표현했습니다.

```java
// StartLine.java - 간결하고 명확한 record 사용
public record StartLine(String method, String path, String protocol) { ... }

// RoutedInfo.java - compact constructor로 방어적 복사까지 구현
public record RoutedInfo(...) {
    public RoutedInfo {
        headers = Map.copyOf(headers);
        queries = Map.copyOf(queries);
        models = Map.copyOf(models);
    }
}
```

Virtual Thread 사용도 훌륭합니다. `Executors.newVirtualThreadPerTaskExecutor()`는 수천 개의 동시 접속을 플랫폼 스레드 수십 개로 처리할 수 있어, 현대적인 WAS 구현에 매우 적합한 선택입니다.

### 2-2. 보안을 의식한 `ResourceResolver` 설계

Path Traversal 공격(`../../../etc/passwd` 같은 요청)을 방어하기 위해 `getCanonicalPath()`로 실제 경로를 확인하고, 기준 디렉토리 밖의 파일 접근을 차단합니다. 파일 크기 제한(10MB), 숨김 파일 차단, 읽기 권한 확인 등 꼼꼼한 보안 검증은 실무 수준의 사고입니다.

```java
// ResourceResolver.java
private static void checkValidPath(File file) throws IOException {
    String targetCanonicalPath = file.getCanonicalPath();
    if (!targetCanonicalPath.startsWith(ABSOLUTE_STATIC_DIR)
            && !targetCanonicalPath.startsWith(ABSOLUTE_TEMPLATE_DIR)) {
        throw new ForbiddenException("Invalid path: " + targetCanonicalPath);
    }
}
```

### 2-3. 커스텀 예외 계층 설계

`ForbiddenException`, `ResourceNotFoundException` 등 HTTP 상태 코드와 1:1로 대응하는 예외를 정의한 것은 좋은 설계 방향입니다. 예외 자체에 의미가 담겨 있어 `catch` 블록에서 분기 처리가 명확해집니다.

### 2-4. 세션 만료 처리 구현

`Session.isExpired()`와 `SessionManager.isValid()` 내에서 자동으로 만료 세션을 삭제하는 로직은, 단순 구현에서 한 발 더 나아간 부분입니다. `ConcurrentHashMap` 선택 이유를 주석으로 설명한 것도 매우 좋습니다.

### 2-5. 의미 있는 테스트 작성

`HttpRequestTest`의 세 가지 테스트는 각각 정상 케이스, POST 바디 파싱, 엣지 케이스(빈 값)를 다루며 `given/when/then` 패턴을 충실히 따르고 있습니다. 특히 `assertDoesNotThrow`를 활용한 엣지 케이스 테스트는 실제 버그를 방지하는 의미 있는 테스트입니다.

---

## 3. 개선이 필요한 부분

### 3-1. [버그] `ResourceResolver.java` - STATIC/TEMPLATE 디렉토리 변수가 뒤바뀜

`ResourceResolver.java`의 `static` 블록에 **초기화 순서가 잘못되어 실제로 경로가 뒤바뀐 버그**가 있습니다.

```java
// ResourceResolver.java - 현재 코드 (버그!)
static {
    try {
        ABSOLUTE_TEMPLATE_DIR = new File(BASE_STATIC_DIR).getCanonicalPath();  // static 경로로 template 초기화!
        ABSOLUTE_STATIC_DIR = new File(BASE_TEMPLATES_DIR).getCanonicalPath(); // template 경로로 static 초기화!
    } catch (IOException ex) { ... }
}
```

**수정 방법:**
```java
static {
    try {
        ABSOLUTE_STATIC_DIR = new File(BASE_STATIC_DIR).getCanonicalPath();
        ABSOLUTE_TEMPLATE_DIR = new File(BASE_TEMPLATES_DIR).getCanonicalPath();
    } catch (IOException ex) { ... }
}
```

### 3-2. [버그/보안] `RequestHandler.java` - 예외가 사용자에게 노출되지 않음

`ResourceResolver`에서 던져진 `ForbiddenException`, `ResourceNotFoundException` 등이 `RequestHandler.run()`의 `catch (IOException e)` 블록에 걸리지 않아, 서버가 아무 응답도 보내지 않고 커넥션을 끊어버립니다.

```java
// RequestHandler.java - 현재 코드
} catch (EOFException e) {
    logger.debug("Client disconnected: {}", e.getMessage());
} catch (IOException e) {
    logger.error("I/O error occurred", e);
}
// RuntimeException(ForbiddenException 등)은 잡히지 않음!
```

**수정 방법:**
```java
} catch (ForbiddenException e) {
    sendErrorResponse(out, "403 Forbidden");
} catch (ResourceNotFoundException e) {
    sendErrorResponse(out, "404 Not Found");
} catch (RuntimeException e) {
    logger.error("Unexpected error", e);
    sendErrorResponse(out, "500 Internal Server Error");
} catch (IOException e) {
    logger.error("I/O error occurred", e);
}
```

### 3-3. [보안] `Database.java` - 스레드 안전하지 않은 `HashMap`

`Database`의 `users` 필드가 일반 `HashMap`으로 선언되어 있습니다. Virtual Thread를 사용하여 다수의 요청을 동시 처리할 때 여러 스레드가 동시에 `addUser`와 `findUserById`를 호출하면 **데이터 손상이나 무한 루프 같은 예측 불가한 버그**가 발생할 수 있습니다.

```java
// Database.java - 현재 코드 (스레드 안전하지 않음)
private static Map<String, User> users = new HashMap<>();

// 수정 후
private static final Map<String, User> users = new ConcurrentHashMap<>();
```

### 3-4. [설계] `UserListAction.java` - XSS 취약점

`UserListAction`에서 직접 HTML `<tr>` 태그를 생성할 때 HTML 이스케이프가 없습니다.

```java
// UserListAction.java - XSS 취약점
userListHtml.append("  <td>").append(user.userName()).append("</td>") // 이스케이프 없음!
```

악의적인 사용자가 이름을 `<script>alert(1)</script>`로 등록하면 해당 HTML이 그대로 렌더링됩니다.

**수정 방법:** 값을 삽입할 때 HTML 이스케이프 처리를 하세요.

```java
private static String escapeHtml(String value) {
    return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
}
```

### 3-5. [설계] `Router.java` - 라우팅 테이블이 하드코딩됨

현재 `Router`의 `static` 블록에 URL 매핑이 직접 작성되어 있어, 새로운 페이지나 액션이 생길 때마다 `Router.java`를 수정해야 합니다. 이는 **개방-폐쇄 원칙(OCP)**에 위배됩니다.

**개선 방향:** `Action` 구현체에 자신이 처리할 HTTP 메서드와 경로를 선언하게 하거나, 어노테이션 방식을 고려해 볼 수 있습니다. Spring MVC의 `@GetMapping`, `@PostMapping`이 바로 이 문제의 해법입니다.

### 3-6. [설계] `User.java` - 도메인 객체의 책임 분리

현재 `User.of(Map<String, String> bodies)` 팩토리 메서드가 데이터베이스 조회(`Database.findUserById`)까지 직접 수행하고 있습니다. 도메인 객체는 자신의 상태만 알아야 하며, 영속성 계층(DB)에 의존해서는 안 됩니다.

중복 검사 로직은 `UserCreateAction`이나 별도의 서비스 객체에서 담당해야 합니다.

```java
// UserCreateAction.java에서 처리하는 것이 더 적절
public RoutedInfo process(HttpRequest request) {
    String userId = request.getBodies().get("userId");
    if (Database.findUserById(userId) != null) {
        throw new DuplicateUserInDBException(...);
    }
    Database.addUser(new User(/* 파라미터 */));
    // ...
}
```

### 3-7. [코드 품질] `UserLoginAction.java` - `UUID.randomUUID() + ""`

```java
// UserLoginAction.java
String newSessionId = UUID.randomUUID() + "";
```

문자열 연결로 `toString()`을 간접 호출하는 방식보다 `UUID.randomUUID().toString()`을 명시적으로 사용하는 것이 의도가 명확합니다.

### 3-8. [코드 품질] `Database.java`의 `findAll()`과 `findAllUsers()` 중복

`findAll()`과 `findAllUsers()`가 동일한 역할을 하는 중복 메서드입니다. 하나로 통일하세요.

---

## 4. 코드 품질 종합

### 잘된 부분
- 메서드 분리가 일반적으로 잘 되어 있으며, 한 메서드가 하나의 역할을 합니다.
- `private static final` 상수 분리가 잘 되어 있습니다.
- 로그 레벨(`debug` vs `error`)을 적절히 구분하여 사용합니다.
- 헤더 키를 소문자로 정규화(`toLowerCase()`)하는 등 HTTP 스펙을 세심하게 반영합니다.

### 개선할 부분
- 에러 페이지 HTML(`403.html`, `404.html`, `500.html`)에서 홈으로 돌아가는 링크가 `/templates/index.html`로 하드코딩되어 있어 `/`로 수정이 필요합니다.

---

## 5. 학습 포인트

코드에 남긴 TODO들이 이미 훌륭한 다음 학습 목표가 되고 있습니다. 우선순위와 함께 정리합니다.

### [높음] 예외 처리 아키텍처 완성

3-2에서 언급한 대로, 예외가 사용자에게 적절한 HTTP 응답으로 변환되어야 합니다. Spring의 `@ExceptionHandler`나 `HandlerExceptionResolver`가 바로 이 역할을 합니다. "예외를 어디서 잡고, 무엇으로 변환할 것인가"를 직접 설계해보세요.

### [높음] 템플릿 엔진에 반복문 지원 추가

`WanjaTemplateEngine`은 `{{변수명}}`만 지원합니다. `{{#each users}}...{{/each}}` 같은 반복문을 지원하면 `UserListAction`의 HTML 생성 코드를 템플릿으로 옮길 수 있고, XSS 취약점도 템플릿 엔진 레벨에서 일괄 처리할 수 있습니다.

### [중간] HTTP 파서 분리

`HttpRequest.of()`가 파싱 로직을 모두 담당하고 있습니다. `HttpRequest`를 불변 데이터 객체(record)로, `HttpRequestParser`를 별도 클래스로 분리하면 단일 책임 원칙을 지키고 테스트도 더 쉬워집니다.

### [중간] 비밀번호 암호화

현재 비밀번호가 평문으로 저장됩니다. `BCrypt`나 `PBKDF2` 같은 단방향 해시 함수를 학습하고, `User.isEqualPassword()`가 해시 비교를 하도록 수정해 보세요.

### [낮음] `keep-alive` 연결 지원

현재 구현은 요청 1개를 처리하면 커넥션을 닫습니다. HTTP/1.1의 기본은 `keep-alive`이며, 하나의 TCP 연결로 여러 요청을 처리합니다. `RequestHandler`가 반복문으로 여러 요청을 처리하도록 확장하면 실제 WAS에 한 걸음 더 가까워집니다.

---

## 마무리

WAS를 밑바닥부터 구현하는 것은 소켓, HTTP 프로토콜, 파일 I/O, 동시성, 세션 관리, 보안 등 웹 개발의 핵심 개념을 한 번에 건드리는 매우 효과적인 학습입니다. 코드 전반에서 "왜 이렇게 해야 하나"를 계속 질문하고 있다는 것이 느껴지며, 이 태도를 유지하면 앞으로의 성장이 매우 기대됩니다.

버그 수정 우선순위: **3-1(경로 뒤바뀜) → 3-2(예외 미처리) → 3-3(스레드 안전성)** 순으로 먼저 처리하는 것을 권장합니다.
