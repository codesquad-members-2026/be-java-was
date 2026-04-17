package webserver.handlers;

import auth.JUserAuth;
import db.Database;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import webserver.session.Session;
import webserver.session.SessionManager;

import java.io.IOException;

import static org.mockito.Mockito.*;

class LoginHandlersTest {

    private final LoginHandlers loginHandlers = new LoginHandlers();

    @Test
    @DisplayName("Successful login creates session and redirects to root")
    void postLoginRequest_Success() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        SessionManager sessionManager = mock(SessionManager.class);
        Session mockSession = mock(Session.class);
        User mockUser = new User("tester", "hashedPw", "TestNick", "test@test.com");

        when(request.getBodyParam("userID")).thenReturn("tester");
        when(request.getBodyParam("password")).thenReturn("1234");
        when(sessionManager.createNewSession()).thenReturn(mockSession);
        when(mockSession.getId()).thenReturn("mocked-session-id");

        // Mocking static methods for Database and JUserAuth
        try (MockedStatic<Database> mockedDb = mockStatic(Database.class);
             MockedStatic<JUserAuth> mockedAuth = mockStatic(JUserAuth.class)) {

            mockedDb.when(() -> Database.findUserById("tester")).thenReturn(mockUser);
            mockedAuth.when(() -> JUserAuth.checkPassword("hashedPw", "1234")).thenReturn(true);

            // when
            loginHandlers.postLoginRequest(request, response, sessionManager);

            // then
            verify(mockSession).addAttribute("user", mockUser);
            verify(response).setHeader(eq("Set-Cookie"), contains("SID=mocked-session-id"));
            verify(response).sendRedirect("/");
        }
    }

    @Test
    @DisplayName("Failed login (wrong password) redirects to failed.html")
    void postLoginRequest_WrongPassword() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        SessionManager sessionManager = mock(SessionManager.class);
        User mockUser = new User("tester", "hashedPw", "TestNick", "test@test.com");

        when(request.getBodyParam("userID")).thenReturn("tester");
        when(request.getBodyParam("password")).thenReturn("wrong-password");

        try (MockedStatic<Database> mockedDb = mockStatic(Database.class);
             MockedStatic<JUserAuth> mockedAuth = mockStatic(JUserAuth.class)) {

            mockedDb.when(() -> Database.findUserById("tester")).thenReturn(mockUser);
            mockedAuth.when(() -> JUserAuth.checkPassword("hashedPw", "wrong-password")).thenReturn(false);

            // when
            loginHandlers.postLoginRequest(request, response, sessionManager);

            // then
            verify(response).sendRedirect("/login/failed.html");
            verify(sessionManager, never()).createNewSession(); // Session shouldn't be created
        }
    }
}
