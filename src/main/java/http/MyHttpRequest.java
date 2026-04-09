package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.*;

public class MyHttpRequest {
    private String method;
    private String url;
    private byte[] body;
    private final Map<String, String> header = new HashMap<>();
    private final Map<String, String> params = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MyHttpRequest.class);

    public MyHttpRequest(List<String> headers){

        if(headers == null || headers.isEmpty()) return;

        fillMethodLine(headers.get(0));

        for(int i = 1; i < headers.size(); i++){
            parseHeader(headers.get(i));
        }
    }

    private void parseHeader(String line) throws InvalidParameterException{
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
        String unTreatedUrl = firstLineStrings[1];
        if(unTreatedUrl.contains("?")){
            String[] splitByQMark = unTreatedUrl.split("\\?",2);
            this.url = splitByQMark[0].strip();
            String[] params = splitByQMark[1].split("&");
            for(String parameter : params){
                String[] keyVal = parameter.split("=",2);
                if(keyVal.length == 2){
                    this.params.put(keyVal[0],keyVal[1]);
                }
                else if(keyVal.length == 1){
                    this.params.put(keyVal[0], "");
                }
            }
        }
        else{
            this.url = unTreatedUrl.strip();
        }
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
