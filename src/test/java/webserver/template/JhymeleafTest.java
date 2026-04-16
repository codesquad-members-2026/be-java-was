package webserver.template;

import model.TemplateAttributes;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

public class JhymeleafTest {

    TemplateAttributes ta;
    byte[] testHtml;

    @BeforeEach
    void setUp(){
        ta = new TemplateAttributes();
        testHtml = null;
    }

    @Test
    @DisplayName("Jhymeleaf should be able to dynamically convert the {{}} into values")
    void testConversion() throws IOException {
        ta.setAttribute("userID", "Jon");
        ta.setAttribute("email", "Jons@squad.com");
        testHtml = "Hi {{ userID }}\n Your email is : {{ email }}".getBytes();
        StringBuilder sb = new StringBuilder();
        Jhymeleaf.convertTemplate(ta,testHtml,sb);
        String result = sb.toString();
        assertThat(result).isEqualTo("Hi Jon\n Your email is : Jons@squad.com");
    }
}
