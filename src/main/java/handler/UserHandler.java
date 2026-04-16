package handler;

import webserver.annotation.Mapping;

public class UserHandler {

    @Mapping("/")
    public String home() {
        return "/index.html";
    }

    @Mapping("/main")
    public String mainPage() {
        return "/index.html";
    }

    @Mapping("/registration")
    public String registerForm() {
        return "/registration/index.html";
    }
}
