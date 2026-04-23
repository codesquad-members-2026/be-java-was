package webserver.template;

import fileIO.FileLoader;
import model.TemplateAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Jhymeleaf {

    public static final Pattern varPattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
    public static final Pattern foreachPatter = Pattern.compile("\\{\\{jh#each (.*?)\\}\\}(.*?)\\{\\{/each\\}\\}", Pattern.DOTALL);
    public static final Pattern ifPattern = Pattern.compile("\\{\\{jh#if (.*?)\\}\\}(.*?)\\{\\{/if\\}\\}", Pattern.DOTALL);
    public static final Pattern ifNotPattern = Pattern.compile("\\{\\{jh#ifNot (.*?)\\}\\}(.*?)\\{\\{/ifNot\\}\\}", Pattern.DOTALL);
    private static final Logger logger = LoggerFactory.getLogger(Jhymeleaf.class);

    public static byte[] fillTemplate(String templateUrl, TemplateAttributes templateAttributes) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String htmlString =new String(FileLoader.getStaticFile(templateUrl));
        htmlString = processHtmlString(htmlString, templateAttributes);
        return htmlString.getBytes(StandardCharsets.UTF_8);
    }

    public static String processHtmlString(String htmlString, TemplateAttributes templateAttributes)throws InvocationTargetException, NoSuchMethodException, IllegalAccessException{
        htmlString = processIf(htmlString,templateAttributes);
        htmlString = processEachBlock(htmlString,templateAttributes);
        htmlString = processVar(htmlString,templateAttributes);
        return htmlString;
    }

    private static String processIf(String htmlString, TemplateAttributes templateAttributes)  {
        Matcher ifBlockFinder = ifPattern.matcher(htmlString);
        StringBuilder stringBuilder = new StringBuilder();
        while(ifBlockFinder.find()){
            String ifKey = ifBlockFinder.group(1).strip();
            String target = ifBlockFinder.group(2);
            Object attribute = templateAttributes.getAttribute(ifKey);
            if(attribute == null){
                ifBlockFinder.appendReplacement(stringBuilder, "");
            }
            else{
                ifBlockFinder.appendReplacement(stringBuilder, Matcher.quoteReplacement(target));
            }
        }
        ifBlockFinder.appendTail(stringBuilder);
        String tmpResult =  stringBuilder.toString();

        Matcher ifNotFinder = ifNotPattern.matcher(tmpResult);
        StringBuilder ifnotbuilder = new StringBuilder();

        while(ifNotFinder.find()){
            String ifNotKey = ifNotFinder.group(1).strip();
            String target = ifNotFinder.group(2);
            Object attribute = templateAttributes.getAttribute(ifNotKey);
            if(attribute == null){
                ifNotFinder.appendReplacement(ifnotbuilder, Matcher.quoteReplacement(target));
            }
            else{
                ifNotFinder.appendReplacement(ifnotbuilder, "");
            }
        }
        ifNotFinder.appendTail(ifnotbuilder);
        return ifnotbuilder.toString();
    }

    private static String processEachBlock(String htmlString, TemplateAttributes templateAttributes) {

        Matcher blockFinder = foreachPatter.matcher(htmlString);
        StringBuilder stringBuilder = new StringBuilder();

        while(blockFinder.find()){
            String key = blockFinder.group(1).strip();
            String innerString = blockFinder.group(2);
            Object attribute = templateAttributes.getAttribute(key);
            StringBuilder replacePart = new StringBuilder();

            if(attribute instanceof Iterable<?> i){
                for(Object elem : i){
                    String processedString = innerString;
                    Matcher varFinder = varPattern.matcher(processedString);
                    while(varFinder.find()){
                        String target = varFinder.group(1).strip();
                        String val = getValueByReflection(target, elem);
                        processedString = processedString.replace(varFinder.group(0),val);
                    }
                    replacePart.append(processedString);
                }
            }
            blockFinder.appendReplacement(stringBuilder, Matcher.quoteReplacement(replacePart.toString()));
        }

        blockFinder.appendTail(stringBuilder);
        return stringBuilder.toString();

    }

    private static String getValueByReflection(String target, Object elem) {

        if(target == null || elem == null || target.isEmpty()){
            return "";
        }
        try{
            String methodName = "get" + target.substring(0,1).toUpperCase() + target.substring(1);
            Method method = elem.getClass().getMethod(methodName);
            return (String)method.invoke(elem);
        } catch (Exception e) {
            logger.error("Jhymeleaf getValueByReflection Encountered an Exception- {}", e.getClass());
            logger.error("Exception invoking request was for - target : {}, elem : {}", target, elem);
            return "";
        }

    }

    private static String processVar(String htmlString, TemplateAttributes templateAttributes) {
        Matcher varFinder = varPattern.matcher(htmlString);
        StringBuilder stringBuilder = new StringBuilder();
        while(varFinder.find()){
            String var = varFinder.group(1).strip();
            Object target = templateAttributes.getAttribute(var);
            String value = "";
            if(target != null){
                value = (String) target;
            }
            varFinder.appendReplacement(stringBuilder,value);
        }
        varFinder.appendTail(stringBuilder);
        return stringBuilder.toString();
    }




}
