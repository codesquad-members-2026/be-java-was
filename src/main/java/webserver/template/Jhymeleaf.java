package webserver.template;

import fileIO.FileLoader;
import model.TemplateAttributes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jhymeleaf {

    public static Pattern targetPattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
    public static Matcher targetMatcher;

    public static byte[] fillTemplate(String templateUrl, TemplateAttributes templateAttributes) throws IOException {
       StringBuilder sb = new StringBuilder();
        byte[] templateHtml = FileLoader.getStaticFile(templateUrl);
        convertTemplate(templateAttributes, templateHtml, sb);
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static void convertTemplate(TemplateAttributes templateAttributes, byte[] templateHtml, StringBuilder sb) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(templateHtml)))){
            String nextLine;
            while((nextLine = br.readLine())!= null ){
                targetMatcher = targetPattern.matcher(nextLine);
                while(targetMatcher.find()){
                    String match = targetMatcher.group(0);
                    Object matchObject = templateAttributes.getAttribute(targetMatcher.group(1).strip());
                    if(matchObject instanceof List<?>){
                        for(Object val : ((List)matchObject)){

                        }
                    }
                    String replaceValue =
                    nextLine = nextLine.replace(match, replaceValue);
                }

                if(sb.length() > 0){
                    sb.append("\n");
                }

                sb.append(nextLine);
            }
        }
    }

}
