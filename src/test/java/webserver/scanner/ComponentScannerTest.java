package webserver.scanner;

import org.junit.jupiter.api.Test;
import interfaces.HandlerMethod;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ComponentScannerTest {

    @Test
    void loadHandlers_ShouldFindAllAnnotatedMethods() throws Exception {
        // 1. Act: Run your scanner on your actual handlers package
        Map<String, HandlerMethod> handlers = ComponentScannerWithoutGemini.loadHandlers("webserver/handlers");

        // 2. Assert: Check that it didn't return null or empty
        assertThat(handlers).isNotEqualTo(null);
        assertFalse(handlers.isEmpty(), "Handler map should not be empty");

        // 3. Assert: Check for specific routes you KNOW exist in MainPageHandlers and RegistrationHandlers
        assertTrue(handlers.containsKey("GET /"), "Should find root path handler");
        assertTrue(handlers.containsKey("GET /registration"), "Should find registration handler");
        assertTrue(handlers.containsKey("POST /create"), "Should find create user handler");

        // 4. Assert: Check that a fake route doesn't exist
        assertFalse(handlers.containsKey("POST /fake-path"));
    }
}