package webserver;

import org.junit.jupiter.api.Test;
import interfaces.HandlerMethod;
import http.MyHttpRequest;
import http.MyHttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RouterTest {

    @Test
    void handleRequest_ShouldCallCorrectHandler() throws Exception {
        // 1. Arrange: Create a fake Map and a flag to track if it was called
        boolean[] wasCalled = {false};
        Map<String, HandlerMethod> fakeHandlers = new HashMap<>();

        // Put a fake lambda in the map that just flips our boolean flag
        fakeHandlers.put("GET /test", (req, res) -> {
            wasCalled[0] = true;
        });

        Router router = new Router(fakeHandlers);

        // NOTE: You will need to create dummy instances of MyHttpRequest and MyHttpResponse here.
        // Assuming you have a way to manually construct them for tests:
        MyHttpRequest mockRequest = new MyHttpRequest(List.of("GET /test HTTP/1.1"));
        MyHttpResponse mockResponse = new MyHttpResponse(null);

        // 2. Act: Tell the router to handle it
        router.handleRequest(mockRequest, mockResponse);

        // 3. Assert: Did the router actually invoke our fake lambda?
        assertTrue(wasCalled[0], "The handler for GET /test was never called!");
    }
}