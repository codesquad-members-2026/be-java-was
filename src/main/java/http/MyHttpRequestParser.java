package http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyHttpRequestParser {

    public static MyHttpRequest parse(InputStream is) throws IOException {
        List<String> headers = new ArrayList<>();
        StringBuilder lineBuilder = new StringBuilder();
        int prev = -1;
        int curr;
        while((curr = is.read()) != -1){
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
            int length = Integer.parseInt(request.getHeader(contentLengthString.strip()));
            byte[] body = is.readNBytes(length);
            request.setBody(body);
        }

        return request;
    }
}
