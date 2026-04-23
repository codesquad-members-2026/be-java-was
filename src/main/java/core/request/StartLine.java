package core.request;

import static core.http.HttpConstant.CRLF;

public record StartLine(String method, String path, String protocol) {

    public String printForDebug(){
        return method + " "  + path + " " + protocol + " " + CRLF;
    }
}
