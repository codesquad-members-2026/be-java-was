package webserver.template;

import model.TemplateAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JhymeleafTest {

    private TemplateAttributes ta;

    @BeforeEach
    void setUp() {
        ta = new TemplateAttributes();
    }

    @Test
    @DisplayName("Should replace simple variables")
    void testProcessVar() throws Exception {
        // Given
        ta.setAttribute("userID", "Jongwoo");
        String html = "<h1>Welcome, {{ userID }}!</h1>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<h1>Welcome, Jongwoo!</h1>");
    }

    @Test
    @DisplayName("Should replace missing variables with empty strings")
    void testProcessVarMissing() throws Exception {
        // Given (No attributes set)
        String html = "<h1>Welcome, {{ missingUser }}!</h1>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<h1>Welcome, !</h1>");
    }

    @Test
    @DisplayName("Should keep {{jh#if}} block if attribute exists")
    void testProcessIf_True() throws Exception {
        // Given
        ta.setAttribute("isLoggedIn", true);
        String html = "<div>{{jh#if isLoggedIn}}<p>User Menu</p>{{/if}}</div>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<div><p>User Menu</p></div>");
    }

    @Test
    @DisplayName("Should remove {{jh#if}} block if attribute is null")
    void testProcessIf_False() throws Exception {
        // Given (isLoggedIn is null)
        String html = "<div>{{jh#if isLoggedIn}}<p>User Menu</p>{{/if}}</div>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<div></div>");
    }

    @Test
    @DisplayName("Should keep {{jh#ifNot}} block if attribute is null")
    void testProcessIfNot_True() throws Exception {
        // Given (isLoggedIn is null)
        String html = "<div>{{jh#ifNot isLoggedIn}}<p>Login Button</p>{{/ifNot}}</div>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<div><p>Login Button</p></div>");
    }

    @Test
    @DisplayName("Should remove {{jh#ifNot}} block if attribute exists")
    void testProcessIfNot_False() throws Exception {
        // Given
        ta.setAttribute("isLoggedIn", true);
        String html = "<div>{{jh#ifNot isLoggedIn}}<p>Login Button</p>{{/ifNot}}</div>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<div></div>");
    }

    @Test
    @DisplayName("Should iterate and use reflection in {{jh#each}} block")
    void testProcessEachBlock() throws Exception {
        // Given
        List<TestUser> testUsers = new ArrayList<>();
        testUsers.add(new TestUser("Jon", "jon@squad.com"));
        testUsers.add(new TestUser("Arya", "arya@squad.com"));

        ta.setAttribute("users", testUsers);

        String html = "<ul>{{jh#each users}}<li>{{ name }} - {{ email }}</li>{{/each}}</ul>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<ul><li>Jon - jon@squad.com</li><li>Arya - arya@squad.com</li></ul>");
    }

    @Test
    @DisplayName("Should output nothing if {{jh#each}} list is empty")
    void testProcessEachBlock_EmptyList() throws Exception {
        // Given
        ta.setAttribute("users", new ArrayList<>()); // Empty list
        String html = "<ul>{{jh#each users}}<li>{{ name }}</li>{{/each}}</ul>";

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).isEqualTo("<ul></ul>");
    }

    @Test
    @DisplayName("Integration: Complex template with If, Each, and Vars")
    void testComplexIntegration() throws Exception {
        // Given
        ta.setAttribute("currentUser", "Jongwoo");
        List<TestUser> friends = List.of(new TestUser("Jon", "jon@squad.com"));
        ta.setAttribute("friends", friends);

        String html = """
                <header>
                    {{jh#if currentUser}}Welcome {{ currentUser }}{{/if}}
                    {{jh#ifNot currentUser}}Please Login{{/ifNot}}
                </header>
                <main>
                    {{jh#each friends}}
                        <p>{{ name }}</p>
                    {{/each}}
                </main>
                """;

        // When
        String result = Jhymeleaf.processHtmlString(html, ta);

        // Then
        assertThat(result).contains("Welcome Jongwoo");
        assertThat(result).doesNotContain("Please Login");
        assertThat(result).contains("<p>Jon</p>");
    }

    // --- Dummy Class for Reflection Testing ---
    public static class TestUser {
        private String name;
        private String email;

        public TestUser(String name, String email) {
            this.name = name;
            this.email = email;
        }

        // Getters are required for your reflection logic to work!
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}