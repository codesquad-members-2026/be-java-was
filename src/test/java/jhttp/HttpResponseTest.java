package jhttp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpResponseTest {

    @Test
    @DisplayName("sendRedirect sets 302 status, Location header, and writes to stream")
    void sendRedirect() {
        // given
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(out);

        // when
        response.sendRedirect("/home");

        // then
        assertThat(response.getStatus()).isEqualTo("302 Found");
        assertThat(response.getHeader("Location")).isEqualTo("/home");

        // Verify stream output
        String outputStreamContent = out.toString();
        assertThat(outputStreamContent).contains("HTTP/1.1 302 Found");
        assertThat(outputStreamContent).contains("Location: /home");
    }
}