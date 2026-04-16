package jhttp;

import fileIO.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.MimeTypeParser;

import java.io.BufferedOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream out;
    private String status = "200 OK";
    private Map<String, String> headers =new HashMap<>();
    private byte[] responseBody;

    public HttpResponse(OutputStream out){
        this.out = new DataOutputStream(new BufferedOutputStream(out));
        this.headers.put("Content-Type", MimeTypeParser.MimeType.HTML.getContentType());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    public String getHeader(String headerKey){
        return this.headers.get(headerKey);
    }

    public void setHeader(String key, String val) {
        this.headers.put(key,val);
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.headers.put("Content-Length", String.valueOf(responseBody.length));
        this.responseBody = responseBody;
    }


    public void send(){
        try{
            this.out.writeBytes("HTTP/1.1 " + this.status + " \r\n");
            for(String key : this.headers.keySet()){
                this.out.writeBytes(key + ": "+this.headers.get(key) +"\r\n");
            }
            this.out.writeBytes("\r\n");
            if(this.responseBody != null){
                this.out.write(this.responseBody, 0 , this.responseBody.length);
            }
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendRedirect(String url){
        this.setStatus("302 Found");
        this.setHeader("Location",url);
        this.send();
    }

    public void sendHtml(String fileName) throws IOException {
        this.setStatus("200 OK");
        this.setHeader("Content-Type", MimeTypeParser.MimeType.HTML.getContentType());
        byte[] body = FileLoader.getStaticFile(fileName);
        this.setHeader("Content-Length",String.valueOf(body.length));
        this.setResponseBody(body);
        this.send();
    }

    public void sendInvalidSessionResponse(String sessionID, String redirectUrl) throws IOException {
        this.setHeader("Set-Cookie", "SID="+ sessionID +"; Path=/; Max-Age=0; HttpOnly");
        this.sendHtml("/login");
    }
}
