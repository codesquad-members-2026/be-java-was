package webserver;

import jhttp.HttpRequest;
import jhttp.HttpRequestParser;
import jhttp.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestHandlerTest {

    private Socket mockSocket;
    private Router mockRouter;
    private RequestHandler requestHandler;

    @BeforeEach
    void setUp() throws Exception {
        mockSocket = mock(Socket.class);
        mockRouter = mock(Router.class);

        requestHandler = new RequestHandler(mockSocket, mockRouter);
    }

    @Test
    @DisplayName("Successfully parses stream and calls router")
    void run_ValidRequest() throws Exception {
        // given
        InputStream mockInputStream = new ByteArrayInputStream("Mock Data".getBytes());
        OutputStream mockOutputStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        HttpRequest mockRequest = mock(HttpRequest.class);

        // Mock the static parse method
        try (MockedStatic<HttpRequestParser> mockedParser = mockStatic(HttpRequestParser.class)) {
            mockedParser.when(() -> HttpRequestParser.parse(any(InputStream.class))).thenReturn(mockRequest);

            // when
            requestHandler.run();

            // then
            // Verify the router received the generated mock request
            verify(mockRouter, times(1)).handleRequest(eq(mockRequest), any(HttpResponse.class));
        }
    }

    @Test
    @DisplayName("Aborts early if HttpRequestParser returns null")
    void run_NullRequest() throws Exception {
        // given
        InputStream mockInputStream = new ByteArrayInputStream(new byte[0]);
        OutputStream mockOutputStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        try (MockedStatic<HttpRequestParser> mockedParser = mockStatic(HttpRequestParser.class)) {
            // Simulate parser returning null (e.g. empty/invalid raw request)
            mockedParser.when(() -> HttpRequestParser.parse(any(InputStream.class))).thenReturn(null);

            // when
            requestHandler.run();

            // then
            // Ensure handleRequest is never executed
            verify(mockRouter, never()).handleRequest(any(), any());
        }
    }
}