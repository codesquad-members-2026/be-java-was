package webserver;

import java.io.*;
import java.net.Socket;

import fileIO.FileLoader;
import http.MyHttpRequest;
import http.MyHttpRequestParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out =connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            MyHttpRequest newRequest = MyHttpRequestParser.parse(in);
            if(newRequest == null) return;

            byte[] body = handleRequest(newRequest);

            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = "<h1>Hello World</h1>".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private byte[] handleRequest(MyHttpRequest request){
        String url = "";
        if(request.getMethod().equals("GET")){
            url = getHandlerResolver(request);
        }
        return viewResolver(url);
    }

    private byte[] viewResolver(String url) {
        if(url.contains("static")){
            try{
                return FileLoader.getStaticFile(url);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getHandlerResolver(MyHttpRequest request) {
        String url = request.getUrl();
        logger.debug("GetRequest received for : {}" ,url);
        if(url.equals("/")){
            return "/static/index.html";
        }
        else{
            return "/static/" + url;
        }
    }



    private String convertRequestToString(InputStream in) throws IOException {
        try(BufferedReader bs = new BufferedReader( new InputStreamReader(in)))
        {
            StringBuilder sb = new StringBuilder();
            String s;
            while((s = bs.readLine()) != null){
                sb.append(s);
            }
            return sb.toString();
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
