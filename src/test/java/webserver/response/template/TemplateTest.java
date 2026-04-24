package webserver.response.template;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TemplateTest {
    public TemplateEngine engine;

    @BeforeEach
    void init() {
        engine = new TemplateEngine(new TemplateParser(), new TemplateBuilder());
    }

    @Test
    void helloNested() {
        String template = """
            <h1>{{company}}</h1>
            {{#departments}}
              <h2>{{deptName}}</h2>
              <ul>
              {{#employees}}
                <li>{{name}} ({{position}})</li>
              {{/employees}}
              </ul>
            {{/departments}}
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("company", "코드스쿼드");
        templateData.put("departments", List.of(
                Map.of(
                        "deptName", "개발팀",
                        "employees", List.of(
                                Map.of("name", "철수", "position", "백엔드"),
                                Map.of("name", "영희", "position", "프론트엔드")
                        )
                ),
                Map.of(
                        "deptName", "디자인팀",
                        "employees", List.of(
                                Map.of("name", "민수", "position", "UI 디자이너")
                        )
                )
        ));

        String html = engine.render(template, templateData);

        String expected = """
            <h1>코드스쿼드</h1>
            <h2>개발팀</h2>
            <ul>
                <li>철수 (백엔드)</li>
                <li>영희 (프론트엔드)</li>
            </ul>
            <h2>디자인팀</h2>
            <ul>
                <li>민수 (UI 디자이너)</li>
            </ul>
            """;

        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void helloNestedWithInverted() {
        String template = """
            <h1>{{company}}</h1>
            {{#departments}}
              <h2>{{deptName}}</h2>
              {{#employees}}
                <p>{{name}}</p>
              {{/employees}}
              {{^employees}}
                <p>직원 없음</p>
              {{/employees}}
            {{/departments}}
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("company", "코드스쿼드");
        templateData.put("departments", List.of(
                Map.of(
                        "deptName", "개발팀",
                        "employees", List.of(
                                Map.of("name", "철수"),
                                Map.of("name", "영희")
                        )
                ),
                Map.of(
                        "deptName", "신설팀",
                        "employees", List.of()
                )
        ));

        String html = engine.render(template, templateData);

        String expected = """
        <h1>코드스쿼드</h1>
        <h2>개발팀</h2>
        <p>철수</p>
        <p>영희</p>
        <h2>신설팀</h2>
        <p>직원 없음</p>
        """;
        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void helloDeepNested() {
        String template = """
            {{#company}}
              <h1>{{name}}</h1>
              {{#departments}}
                <h2>{{deptName}}</h2>
                {{#employees}}
                  <div>
                    <p>{{name}}</p>
                    {{#skills}}
                      <span>{{skillName}}</span>
                    {{/skills}}
                  </div>
                {{/employees}}
              {{/departments}}
            {{/company}}
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("company", Map.of(
                "name", "코드스쿼드",
                "departments", List.of(
                        Map.of(
                                "deptName", "개발팀",
                                "employees", List.of(
                                        Map.of(
                                                "name", "철수",
                                                "skills", List.of(
                                                        Map.of("skillName", "Java"),
                                                        Map.of("skillName", "Spring")
                                                )
                                        )
                                )
                        )
                )
        ));

        String html = engine.render(template, templateData);

        String expected = """
        <h1>코드스쿼드</h1>
        <h2>개발팀</h2>
        <div>
            <p>철수</p>
            <span>Java</span>
            <span>Spring</span>
        </div>
        """;
        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void renderSingleUser() {
        String template = """
            <h1>회원 정보</h1>
            <p>ID: {{userId}}</p>
            <p>이름: {{name}}</p>
            <p>이메일: {{email}}</p>
            """;

        User user = new User("kim123", "pw1234", "김철수", "kim@example.com");

        String html = engine.render(template, user);

        String expected = """
            <h1>회원 정보</h1>
            <p>ID: kim123</p>
            <p>이름: 김철수</p>
            <p>이메일: kim@example.com</p>
            """;

        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void renderUserList() {
        String template = """
            <h1>회원 목록</h1>
            <ul>
            {{#users}}
              <li>{{name}} ({{email}})</li>
            {{/users}}
            </ul>
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("users", List.of(
                new User("kim123", "pw1", "김철수", "kim@example.com"),
                new User("lee456", "pw2", "이영희", "lee@example.com"),
                new User("park789", "pw3", "박민수", "park@example.com")
        ));

        String html = engine.render(template, templateData);

        String expected = """
            <h1>회원 목록</h1>
            <ul>
              <li>김철수 (kim@example.com)</li>
              <li>이영희 (lee@example.com)</li>
              <li>박민수 (park@example.com)</li>
            </ul>
            """;

        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void renderEmptyUserListWithInverted() {
        String template = """
            <h1>회원 목록</h1>
            {{#users}}
              <p>{{name}}</p>
            {{/users}}
            {{^users}}
              <p>회원이 없습니다</p>
            {{/users}}
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("users", List.of());

        String html = engine.render(template, templateData);

        String expected = """
            <h1>회원 목록</h1>
            <p>회원이 없습니다</p>
            """;

        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void renderUserAsSectionContext() {
        String template = """
            <h1>로그인 정보</h1>
            {{#user}}
              <p>환영합니다, {{name}}님</p>
              <p>이메일: {{email}}</p>
            {{/user}}
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("user", new User("kim123", "pw1234", "김철수", "kim@example.com"));

        String html = engine.render(template, templateData);

        String expected = """
            <h1>로그인 정보</h1>
            <p>환영합니다, 김철수님</p>
            <p>이메일: kim@example.com</p>
            """;

        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }

    @Test
    void renderWithBooleanValue() {
        String template = """
            <h1>{{title}}</h1>
            {{#isAdmin}}
              <p>관리자 메뉴</p>
            {{/isAdmin}}
            {{^isAdmin}}
              <p>일반 사용자 메뉴</p>
            {{/isAdmin}}
            """;

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("title", "대시보드");
        templateData.put("isAdmin", true);

        String html = engine.render(template, templateData);

        String expected = """
            <h1>대시보드</h1>
            <p>관리자 메뉴</p>
            """;

        assertThat(html).isEqualToNormalizingWhitespace(expected);
    }
}
