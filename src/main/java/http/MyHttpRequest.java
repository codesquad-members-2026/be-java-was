package http;

import java.security.InvalidParameterException;
import java.util.*;

public class MyHttpRequest {
    private String method;
    private String url;
    private byte[] body;
    private Map<String, String> header = new HashMap<>();
    private final Map<String, String> params = new HashMap<>();

    public MyHttpRequest(List<String> headers){

        if(headers == null || headers.isEmpty()) return;

        fillMethodLine(headers.get(0));

        for(int i = 1; i < headers.size(); i++){
            praseHeader(headers.get(i));
        }
    }

    private void praseHeader(String line) throws InvalidParameterException{
        if(line.contains(":")){
            String[] splitResult = line.split(":",2);
            if(splitResult.length != 2){
                throw new InvalidParameterException("Header Parsing Failed");
            }
            this.header.put(splitResult[0].strip() , splitResult[1].strip());
        }
        else{
            fillMethodLine(line);
        }
    }

    private void fillMethodLine(String line) {
        String[] firstLineStrings = line.split(" ");
        this.method = firstLineStrings[0];
        this.url = firstLineStrings[1];
    }


    public void setBody(byte[] body){
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public byte[] getBody() {
        return body;
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public String getParam(String key) {
        return params.get(key);
    }
}
