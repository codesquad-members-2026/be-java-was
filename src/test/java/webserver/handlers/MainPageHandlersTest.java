package webserver.handlers;

import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.TemplateAttributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import webserver.session.Session;

import java.io.IOException;

import static org.mockito.Mockito.*;

class MainPageHandlersTest {

    private final MainPageHandlers mainPageHandlers = new MainPageHandlers();

    @Test
    @DisplayName("Valid session loads front page normally")
    void getFrontPage_WithValidSession() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        Session session = mock(Session.class);
        TemplateAttributes mockTemplateAtts = mock(TemplateAttributes.class);

        // when
        mainPageHandlers.getFrontPage(session,mockTemplateAtts);

        // then
        verify(response).sendHtml("/index.html");
        verify(response, never()).setHeader(eq("Set-Cookie"), anyString()); // Shouldn't overwrite cookie
    }

    @Test
    @DisplayName("Null session clears the client's cookie and loads front page")
    void getFrontPage_WithNullSession() throws IOException {
        // given
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        TemplateAttributes mockTemplateAtts = mock(TemplateAttributes.class);
        when(request.getSessionID()).thenReturn("expired-session-id");

        // when
        mainPageHandlers.getFrontPage(null,mockTemplateAtts);

        // then
        verify(response).setHeader(eq("Set-Cookie"), contains("Max-Age=0")); // Verifies cookie deletion
        verify(response).sendHtml("/index.html");
    }
}