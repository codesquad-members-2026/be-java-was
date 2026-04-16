package webserver;

import fileIO.FileLoader;
import interfaces.HandlerMethod;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import webserver.session.SessionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

class RouterTest {

    private Map<String, HandlerMethod> mockHandlers;
    private SessionManager mockSessionManager;
    private Router router;

    @BeforeEach
    void setUp() {
        mockHandlers = new HashMap<>();
        mockSessionManager = mock(SessionManager.class);
        router = new Router(mockHandlers, mockSessionManager);
    }

    @Test
    @DisplayName("Routes correctly to a dynamically registered handler")
    void handleRequest_WithRegisteredHandler() throws Exception {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        HandlerMethod mockHandler = mock(HandlerMethod.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getUrl()).thenReturn("/test");

        // Register the handler for the exact signature "GET /test"
        mockHandlers.put("GET /test", mockHandler);

        // when
        router.handleRequest(request, response);

        // then
        verify(request).setSessionManage(mockSessionManager);
        verify(mockHandler).handle(request, response, mockSessionManager); // verifies the handler took over
    }

    @Test
    @DisplayName("Returns 200 OK and sends file when no handler exists but a static file is found")
    void handleRequest_StaticFileFound() throws Exception {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getUrl()).thenReturn("/index.html");

        byte[] fakeFileContent = "<html>Hello</html>".getBytes();

        try (MockedStatic<FileLoader> mockedFileLoader = mockStatic(FileLoader.class);
             MockedStatic<MimeTypeParser> mockedMimeParser = mockStatic(MimeTypeParser.class)) {

            mockedFileLoader.when(() -> FileLoader.getStaticFile("/index.html")).thenReturn(fakeFileContent);
            mockedMimeParser.when(() -> MimeTypeParser.extractExtension("/index.html")).thenReturn("html");
            mockedMimeParser.when(() -> MimeTypeParser.getContentType("html")).thenReturn("text/html");

            // when
            router.handleRequest(request, response);

            // then
            verify(response).setStatus("200 OK");
            verify(response).setHeader("Content-Type", "text/html");
            verify(response).setHeader("Content-Length", String.valueOf(fakeFileContent.length));
            verify(response).setResponseBody(fakeFileContent);
            verify(response).send();
        }
    }

    @Test
    @DisplayName("Returns 404 Not Found when a static file does not exist")
    void handleRequest_StaticFileNotFound() throws Exception {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getUrl()).thenReturn("/unknown.html");

        try (MockedStatic<FileLoader> mockedFileLoader = mockStatic(FileLoader.class)) {
            // Force an IOException to simulate file not found
            mockedFileLoader.when(() -> FileLoader.getStaticFile("/unknown.html")).thenThrow(new IOException());

            // when
            router.handleRequest(request, response);

            // then
            verify(response).setStatus("404 Not Found");
            verify(response).send();
        }
    }

    @Test
    @DisplayName("Returns 400 Bad Request for unregistered non-GET routes")
    void handleRequest_UnregisteredNonGetMethod() throws Exception {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        when(request.getMethod()).thenReturn("POST");
        when(request.getUrl()).thenReturn("/static-target-that-only-accepts-get");

        try (MockedStatic<FileLoader> mockedFileLoader = mockStatic(FileLoader.class)) {
            // Force an IOException to simulate fallback logic execution
            mockedFileLoader.when(() -> FileLoader.getStaticFile(anyString())).thenThrow(new IOException());

            // when
            router.handleRequest(request, response);

            // then
            verify(response).setStatus("400 Bad Request");
            verify(response, atLeastOnce()).send();
        }
    }
}