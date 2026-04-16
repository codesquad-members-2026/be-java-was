package webserver.handlers;

import auth.JUserAuth;
import db.Database;
import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrationHandlersTest {

    private final RegistrationHandlers registrationHandlers = new RegistrationHandlers();

    @Test
    @DisplayName("Missing registration fields redirects back to registration page")
    void postCreateUserAccount_MissingFields() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        // Simulating missing 'email' parameter
        when(request.getBodyParam("userID")).thenReturn("newUser");
        when(request.getBodyParam("nickname")).thenReturn("nick");
        when(request.getBodyParam("email")).thenReturn(null);
        when(request.getBodyParam("password")).thenReturn("1234");

        // when
        registrationHandlers.postCreateUserAccount(request, response);

        // then
        verify(response).sendRedirect("/registration");
    }

    @Test
    @DisplayName("Valid registration fields creates user, hashes password, and redirects to root")
    void postCreateUserAccount_Success() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        when(request.getBodyParam("userID")).thenReturn("newUser");
        when(request.getBodyParam("nickname")).thenReturn("nick");
        when(request.getBodyParam("email")).thenReturn("test@test.com");
        when(request.getBodyParam("password")).thenReturn("1234");

        try (MockedStatic<JUserAuth> mockedAuth = mockStatic(JUserAuth.class);
             MockedStatic<Database> mockedDb = mockStatic(Database.class)) {

            mockedAuth.when(() -> JUserAuth.hashPassword("1234")).thenReturn("hashed-1234");

            // when
            registrationHandlers.postCreateUserAccount(request, response);

            // then
            mockedDb.verify(() -> Database.addUser(any(User.class)), times(1));
            verify(response).sendRedirect("/");
        }
    }
}