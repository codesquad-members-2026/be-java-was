# 코드 리뷰: Java WAS (Web Application Server) - Hana 브랜치

> **리뷰어**: 코드스쿼드 멘토
> **리뷰 일자**: 2026-04-24
> **대상 브랜치**: `origin/Hana`

---

## 요약

전체적으로 잘 구조화된 코드입니다. 단순히 동작하는 서버를 만드는 것을 넘어서, 책임 분리와 패턴 적용에 대해 진지하게 고민한 흔적이 곳곳에 보입니다. 특히 Command 패턴 기반의 Action 구조와 세션 관리, 테스트 코드 작성이 인상적입니다. 아래에서 잘한 점과 함께 더 성장할 수 있는 부분도 함께 살펴보겠습니다.

---

## 1. 전체적인 구조와 설계

### 패키지 구조

```
src/main/java/
├── action/         # 요청 처리 로직 (Command 패턴)
├── db/             # 인메모리 데이터베이스
├── http/           # HTTP 요청/응답 추상화
├── model/          # 도메인 모델
├── session/        # 세션 관리
├── util/           # 유틸리티 클래스들
└── webserver/      # 서버 진입점
```

패키지 구조가 각 책임에 따라 명확하게 분리되어 있습니다. HTTP 처리, 비즈니스 로직(action), 유틸리티가 잘 구분됩니다.

### 아키텍처 흐름

```
브라우저 요청
    └→ WebServer (소켓 수락)
         └→ RequestHandler (스레드)
              └→ HttpRequest (파싱)
              └→ ActionMap (라우팅)
                   └→ Action.execute() (비즈니스 로직)
                        └→ HttpResponse.send() (응답)
```

요청이 들어오는 순간부터 응답이 나가는 순간까지의 흐름이 깔끔하게 레이어로 나뉘어져 있습니다. 이 흐름을 이해하고 설계했다는 점이 매우 좋습니다.

---

## 2. 잘한 점

### Command 패턴을 활용한 Action 구조

`Action` 인터페이스와 `AbstractAction` 추상 클래스, 그리고 `ActionMap`의 조합은 교과서적인 Command 패턴 적용입니다.

```java
// action/Action.java
public interface Action {
    void execute(HttpRequest request, HttpResponse response) throws IOException;
}

// action/ActionMap.java
public static Action getAction(String path) {
    Action action = actions.get(path);
    if (action == null) {
        return new StaticResourceAction();
    }
    return action;
}
```

새로운 URL 경로가 생겨도 `ActionMap`에 한 줄 추가하고 Action 구현체를 만들기만 하면 됩니다. `RequestHandler`는 전혀 수정할 필요가 없죠. 이것이 바로 **OCP(개방-폐쇄 원칙)**의 좋은 예입니다.

### 공통 로직을 AbstractAction으로 추상화

```java
// action/AbstractAction.java
protected User getSessionUser(HttpRequest request) {
    String sid = request.getCookie("SID");
    if (sid == null) return null;
    return SessionManager.getInstance().read(sid);
}

protected String renderWithLayout(String templatePath, User user) throws IOException {
    String mainHtml = readResource(templatePath);
    String fragmentPath = (user == null) ? "/fragments/nav_guest.html" : "/fragments/nav_user.html";
    String menuHtml = readResource(fragmentPath);
    if (user != null) {
        menuHtml = menuHtml.replace("{{userName}}", user.getName());
    }
    return mainHtml.replace("{{header_items}}", menuHtml);
}
```

세션 확인, 레이아웃 렌더링 같은 공통 작업을 `AbstractAction`에 모아 중복 코드를 없앤 것이 훌륭합니다. `IndexAction`, `UserListAction`이 이 덕분에 매우 짧고 명확해졌습니다.

### SessionManager의 싱글톤 + ConcurrentHashMap

```java
// session/SessionManager.java
private final Map<String, User> sessions = new ConcurrentHashMap<String, User>();
private static final SessionManager instance = new SessionManager();
```

멀티스레드 환경에서의 동시성 문제를 `ConcurrentHashMap`으로 해결한 것은 웹 서버 개발에서 매우 중요한 포인트입니다. `HashMap`을 쓰면 여러 스레드가 동시에 접근할 때 데이터가 꼬일 수 있는데, 이를 이미 인지하고 해결한 것이 인상적입니다.

### Virtual Thread 적용

```java
// webserver/WebServer.java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

Java 21의 Virtual Thread를 적극 활용한 것이 눈에 띕니다. 기존 `newFixedThreadPool(10)` 방식과의 차이를 주석으로 남겨둔 것도 학습 흔적이 잘 드러납니다.

### MimeType Enum 활용

```java
// util/MimeType.java
public enum MimeType {
    HTML(".html", "text/html"),
    CSS(".css", "text/css"),
    JS(".js", "text/javascript"),
    ...

    public static String getMime(String path) {
        return java.util.Arrays.stream(values())
                .filter(m -> !m.extension.isEmpty())
                .filter(m -> lowerPath.endsWith(m.extension))
                .findFirst()
                .map(m -> m.mime)
                .orElse(UNKNOWN.mime);
    }
}
```

MIME 타입 매핑을 `enum`으로 표현하고 Stream API를 활용해 조회한 것이 우아합니다. 새 파일 형식을 추가할 때 `enum` 상수 하나만 추가하면 되는 확장성도 좋습니다.

### 로그아웃 시 쿠키 만료 처리

```java
// action/UserLogoutAction.java
response.addHeader("Set-Cookie", "SID=" + sid + "; Path=/; Max-Age=0");
```

단순히 서버 세션만 삭제하는 것이 아니라, 브라우저의 쿠키도 `Max-Age=0`으로 명시적으로 만료시킨 것은 보안적으로 올바른 처리입니다.

### 테스트 코드의 @DisplayName 활용

```java
@Test
@DisplayName("로그인 후 로그아웃하면 세션 저장소에서 유저 정보가 완전히 삭제되어야 한다")
void logout_ShouldRemoveUserFromSession() {
```

`@DisplayName`으로 테스트의 의도를 한국어로 명확하게 표현한 것, 그리고 `// [1] ~ [4]` 같은 단계별 주석으로 테스트 흐름을 설명한 것은 훌륭한 습관입니다.

---

## 3. 개선이 필요한 부분

### 3-1. `BASIC_PATH` 상수 중복 정의

**문제**: 동일한 상수가 두 곳에 분산되어 있습니다.

- `AbstractAction.java:13` → `protected static final String BASIC_PATH = "./src/main/resources/static";`
- `StaticResourceAction.java:13` → `private static final String BASIC_PATH = "./src/main/resources/static";`

`StaticResourceAction`은 `AbstractAction`을 상속하지 않아 공통 상수를 재사용할 수 없고, 결국 동일한 값을 두 번 선언하게 됩니다. 경로가 바뀔 때 두 곳을 모두 수정해야 하는 유지보수 문제가 생깁니다.

**개선 방법**: 별도의 상수 클래스를 만들거나, `StaticResourceAction`도 `AbstractAction`을 상속하도록 변경합니다.

```java
// util/ServerConstants.java (새로 만들기)
public final class ServerConstants {
    public static final String STATIC_RESOURCE_PATH = "./src/main/resources/static";
    private ServerConstants() {}
}
```

또는 더 간단하게, `StaticResourceAction`이 `AbstractAction`을 상속하면 됩니다.

```java
// action/StaticResourceAction.java
public class StaticResourceAction extends AbstractAction {
    // BASIC_PATH를 AbstractAction에서 상속받아 사용
}
```

---

### 3-2. `Database.java`의 스레드 안전성 문제

**문제**: `Database.java:7`의 `HashMap`은 멀티스레드 환경에서 안전하지 않습니다.

```java
// db/Database.java
private static Map<String, User> users = new HashMap<>(); // 위험!
```

`SessionManager`에서는 `ConcurrentHashMap`을 사용했는데, `Database`에서는 일반 `HashMap`을 사용하고 있습니다. 동시에 여러 사용자가 회원가입을 시도하면 데이터 손실이나 예외가 발생할 수 있습니다.

**개선 방법**:

```java
// db/Database.java
private static final Map<String, User> users = new ConcurrentHashMap<>();
```

---

### 3-3. 입력값 검증 누락

**문제**: `UserCreateAction.java`에서 파라미터가 `null`이거나 빈 값일 때 처리가 없습니다.

```java
// action/UserCreateAction.java
String userId = request.getParameter("userId");   // null 가능
String password = request.getParameter("password"); // null 가능
String name = request.getParameter("name");
String email = request.getParameter("email");

User user = new User(userId, password, name, email); // null을 그대로 저장
Database.addUser(user);
```

악의적인 요청이나 잘못된 폼 제출로 `null`이 들어오면 `null` 값이 그대로 DB에 저장됩니다.

**개선 방법**: 검증 로직을 추가합니다.

```java
if (userId == null || userId.isBlank() || password == null || password.isBlank()) {
    response.sendRedirect("/registration/index.html");
    return;
}
```

---

### 3-4. `UserLoginAction`이 `AbstractAction`을 상속하지 않음

**문제**: `UserLoginAction.java:11`을 보면 `implements Action`으로 되어 있습니다.

```java
public class UserLoginAction implements Action { // AbstractAction을 상속하지 않음
```

`UserListAction`, `IndexAction` 등은 `AbstractAction`을 상속하는데, `UserLoginAction`만 직접 `Action`을 구현합니다. 구조적 일관성이 깨지고, 나중에 `AbstractAction`에 공통 기능이 추가되더라도 `UserLoginAction`은 혜택을 받지 못합니다.

**개선 방법**:

```java
public class UserLoginAction extends AbstractAction {
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        // ...
    }
}
```

---

### 3-5. `Parser.java`에서 key도 URL 디코딩 필요

**문제**: `Parser.java:23`에서 value만 URL 디코딩하고 key는 디코딩하지 않습니다.

```java
// util/Parser.java
String key = keyValue[0].trim(); // 디코딩 없음
String decodedValue = URLDecoder.decode(value, "UTF-8"); // value만 디코딩
map.put(key, decodedValue);
```

실무에서 key에 특수문자나 한글이 포함될 가능성은 낮지만, 이론적으로는 key도 URL 인코딩될 수 있습니다. 일관성을 위해 key도 디코딩하는 것이 좋습니다.

---

### 3-6. `HttpResponse`의 헤더 중복 문제

**문제**: `HttpResponse.java`에서 `Map<String, String>`을 헤더 저장에 사용합니다.

```java
// http/HttpResponse.java
private final Map<String, String> headers = new HashMap<>();
```

`Map`의 key는 고유해야 하므로, 같은 헤더 이름으로 `addHeader`를 두 번 호출하면 마지막 값만 남습니다. `Set-Cookie`처럼 같은 이름의 헤더가 여러 개 필요한 경우(예: 여러 쿠키 설정)에 문제가 됩니다.

현재 코드에서 쿠키는 하나만 사용하므로 당장은 문제가 없지만, 구조적 한계입니다.

**개선 방법**: `Map<String, List<String>>`이나 `LinkedList<String[]>`을 사용합니다.

---

### 3-7. HTML 생성 시 XSS 취약점

**문제**: `UserListAction.java:28-33`에서 사용자 데이터를 HTML에 직접 삽입합니다.

```java
// action/UserListAction.java
sb.append("<td>").append(u.getUserId()).append("</td>")
  .append("<td>").append(u.getName()).append("</td>")
  .append("<td>").append(u.getEmail()).append("</td>")
```

만약 사용자가 `name`을 `<script>alert('XSS')</script>`로 가입한다면, 이 스크립트가 그대로 HTML에 삽입되어 실행됩니다.

**개선 방법**: HTML 특수문자를 이스케이프해야 합니다.

```java
private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;")
               .replace("\"", "&quot;");
}
```

---

### 3-8. `FileUtil.java`의 `long` to `int` 캐스팅 위험

**문제**: `FileUtil.java:10`에서 파일 크기를 `int`로 캐스팅합니다.

```java
// util/FileUtil.java
long length = file.length();
byte[] bytes = new byte[(int) length]; // 2GB 이상 파일이면 오버플로우
```

2GB 이상의 파일을 처리할 때 `int` 오버플로우가 발생하여 음수 배열 크기가 됩니다. 지금 프로젝트에서는 발생할 가능성이 거의 없지만, 방어적 코딩 습관으로 체크 로직을 추가하면 좋습니다.

---

### 3-9. `PathUtil.normalize`의 경직된 하드코딩

**문제**: `PathUtil.java`에 경로가 하드코딩되어 있어 새 경로가 생길 때마다 수정이 필요합니다.

```java
// util/PathUtil.java
public static String normalize(String path) {
    if (path.equals("/")) return "/index.html";
    if (path.equals("/registration")) return "/registration/index.html";
    if (path.equals("/login")) return "/login/index.html";
    return path;
}
```

**개선 방법**: 확장자가 없는 경로는 자동으로 `/index.html`을 붙이는 범용 로직으로 변경할 수 있습니다.

```java
public static String normalize(String path) {
    if (path.equals("/")) return "/index.html";
    // 확장자가 없는 경우 /index.html 추가
    if (!path.contains(".")) {
        return path.endsWith("/") ? path + "index.html" : path + "/index.html";
    }
    return path;
}
```

---

### 3-10. `HttpRequestTest.java`의 불완전한 테스트

**문제**: `HttpRequestTest.java:29-41`의 `consumeHeadersTest` 테스트는 아무것도 검증하지 않습니다.

```java
@Test
@DisplayName("브라우저가 보낸 헤더들을 모두 읽어서 스트림을 비워준다")
void consumeHeadersTest() {
    // ...
    HttpRequest request = new HttpRequest(in);
    // 검증 코드가 없음!
}
```

테스트가 통과되더라도 아무것도 확인하지 않으므로, 실제로 버그가 있어도 이 테스트는 항상 통과합니다.

**개선 방법**: 실제로 검증할 내용을 추가합니다.

```java
assertThat(request.getPath()).isEqualTo("/index.html");
assertThat(request.getMethod()).isEqualTo("GET");
```

---

## 4. 코드 품질

### 가독성

전반적으로 가독성이 좋습니다. 메서드 이름(`serveStaticFile`, `serveErrorFile`, `renderWithLayout`, `getSessionUser`)이 역할을 명확하게 나타내고, 메서드 길이도 적절하게 유지되고 있습니다.

### 네이밍

- 대부분의 네이밍이 Java 컨벤션을 잘 따르고 있습니다.
- `WebTest.java:6`의 주석 `// 원재님의 실제 패키지 명으로 수정하세요!`가 남아 있어 제출 전에 정리가 필요합니다.

### 주석

- `WebServer.java`, `build.gradle`, `ActionMap.java` 등에 학습 과정의 분석 주석이 잘 달려 있습니다. 학습 목적으로는 훌륭하나, 프로덕션 코드라면 코드로 의도가 드러나야 할 내용이 많습니다.
- `WebServer.java`의 `//  고정 크기 방식 ExecutorService execcutor = Executors.newFixedThreadPool(10)` 주석에 오타(`execcutor`)가 있습니다.

### 중복 코드

- `BASIC_PATH` 중복 (3-1에서 언급)
- `login/index.html`과 `registration/index.html`의 HTML 헤더 내비게이션이 하드코딩되어 있어 `index.html`의 `{{header_items}}` 템플릿 방식과 통일성이 없습니다.

---

## 5. 학습 포인트

이 프로젝트를 통해 얻은 것들을 더욱 깊게 발전시킬 수 있는 주제들을 소개합니다.

### 5-1. 직접 만든 템플릿 엔진에서 Thymeleaf로

지금 `{{header_items}}`와 `{{userList}}` 같은 직접 만든 템플릿 엔진을 사용하고 있습니다. 이 경험을 바탕으로 실제 템플릿 엔진인 **Thymeleaf**나 **Mustache**를 배우면 훨씬 강력한 기능을 경험할 수 있습니다.

### 5-2. 필터/미들웨어 패턴

현재 로그인 여부 확인(`getSessionUser`)이 각 Action마다 반복됩니다. **필터(Filter)/인터셉터(Interceptor) 패턴**을 도입하면 인증 체크를 Action에서 분리할 수 있습니다. Spring의 `HandlerInterceptor`나 서블릿의 `Filter`가 바로 이 패턴입니다.

### 5-3. HTTP 스펙 더 깊이 이해하기

- 현재 구현한 내용 외에 **HTTP Keep-Alive**, **Chunked Transfer Encoding**, **ETag와 캐시 헤더**를 직접 구현해보면 HTTP를 훨씬 깊이 이해할 수 있습니다.
- 현재 `HttpResponse.send()`의 status line 끝에 공백이 하나 더 붙습니다 (`"HTTP/1.1 " + status + " \r\n"`). HTTP 스펙(`RFC 7230`)의 정확한 포맷을 확인해보세요.

### 5-4. 의존성 주입(DI) 패턴

`ActionMap`에서 `new UserCreateAction()`, `new UserLoginAction()` 등을 직접 생성하고, 각 Action 안에서 `SessionManager.getInstance()`를 직접 호출합니다. 이는 강한 결합(Tight Coupling)을 만듭니다. **의존성 주입(DI)** 패턴을 배우고 적용하면, Spring Framework가 왜 DI 컨테이너를 제공하는지 자연스럽게 이해하게 됩니다.

### 5-5. 예외 처리 전략

현재 `IOException`이 Action 인터페이스 시그니처에 선언되어 있어 모든 구현체가 이를 처리해야 합니다. **Checked Exception vs Unchecked Exception** 전략, **예외 계층 설계**, **전역 예외 처리기** 패턴을 공부하면 더 견고한 에러 처리 구조를 설계할 수 있습니다.

---

## 마치며

WAS를 밑바닥부터 구현하면서 HTTP 파싱, 라우팅, 세션 관리, 파일 서빙까지 핵심 개념들을 직접 손으로 만들어보는 것은 매우 값진 경험입니다. 이 과정에서 얻은 이해를 바탕으로 Spring MVC 같은 프레임워크를 공부할 때 "이 기능이 내가 직접 만든 그것과 같은 것이구나"라는 깊은 통찰이 생길 것입니다.

구조적 사고가 잘 드러나는 코드였습니다. 제안한 개선 사항들을 하나씩 적용해보세요!
