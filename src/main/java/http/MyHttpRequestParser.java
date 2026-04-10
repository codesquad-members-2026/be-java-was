package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyHttpRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(MyHttpRequestParser.class);
    public static MyHttpRequest parse(InputStream is) throws IOException {
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

            MyHttpRequest request = new MyHttpRequest(headers);
            String contentLengthString = request.getHeader("Content-Length");
            if(contentLengthString != null){
                int length = Integer.parseInt(contentLengthString.strip());
                byte[] body = bis.readNBytes(length);
                request.setBody(body);
            }
//            logger.debug("{}: MyHttpParser Complete",Thread.currentThread().getName());
//            logger.debug("{}: Parsed Method : {}",Thread.currentThread().getName(),request.getMethod());
            return request;

        }
}
