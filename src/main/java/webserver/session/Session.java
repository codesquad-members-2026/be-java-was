package webserver.session;

import model.User;

import java.time.LocalDateTime;

public class Session {
    private final User user;
    private LocalDateTime creationTime;
    private LocalDateTime lastAccessedTime;
    private LocalDateTime maxInactiveInterval;


}
