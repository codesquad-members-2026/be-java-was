package jhttp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParser.class);
    public static HttpRequest parse(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
            List<String> headers = new ArrayList<>();
            StringBuilder lineBuilder = new StringBuilder();
            int prev = -1;
            int curr;
            while((curr = bis.read()) != -1){
                if(prev == '\r' && curr =='\n'){
                    String line = lineBuilder.substring(0, lineBuilder.length() -1);
                    if(line.isEmpty()){
                        break;
                    }
                    headers.add(line);
                    lineBuilder.setLength(0);
                }
                else{
                    lineBuilder.append((char) curr);
                }
                prev = curr;
            }

            if(headers.isEmpty()){
                return null;
            }

            HttpRequest request = new HttpRequest(headers);
            String contentLengthString = request.getHeader("Content-Length");
            if(contentLengthString != null){
                int length = Integer.parseInt(contentLengthString.strip());
                byte[] body = bis.readNBytes(length);
                request.setBody(body);
                if(request.getHeader("Content-Type").equals("application/x-www-form-urlencoded")){
                    request.setBodyParams();
                }
            }
//            logger.debug("{}: MyHttpParser Complete",Thread.currentThread().getName());
//            logger.debug("{}: Parsed Method : {}",Thread.currentThread().getName(),request.getMethod());
            return request;

        }
}
