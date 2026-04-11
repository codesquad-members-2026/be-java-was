package webserver.scanner;

import annotations.RequestMapping;
import interfaces.HandlerMethod;

import java.io.*;

import java.lang.reflect.Method;

import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ComponentScannerWithoutGemini {

    public static Map<String,HandlerMethod> loadHandlers(String targetPackage) throws IOException, URISyntaxException, ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Map<String, HandlerMethod> result = new HashMap<>();

        // TODO : WHAT IF THE URL IS NOT FILE BUT JAR? ("jar".equals(r.getProtocol()))
        URL r = cl.getResource(targetPackage);
        if(r == null){
            throw new IOException("NO SUCH PACKAGE EXISTS");
        }
        Path dir = Paths.get(r.toURI());
        File file = dir.toFile();
        File[] listFile = file.listFiles();
        List<Class<?>> classes = new ArrayList<>();
        for (File f : listFile) {
            String fs = f.toString();
            if (fs.endsWith(".class")) {
                String targetHandlerClass = "";
                int i = fs.lastIndexOf(".class");
                int j = fs.lastIndexOf("webserver/handlers");
                targetHandlerClass = fs.substring(0, i);
                targetHandlerClass = targetHandlerClass.substring(j);
                targetHandlerClass = targetHandlerClass.replaceAll("/", ".");
                Class<?> handlerClass = cl.loadClass(targetHandlerClass);
                classes.add(handlerClass);
            }
        }
        for (Class<?> c : classes) {

            try {
                Object handlerInstance = c.getDeclaredConstructor().newInstance();

                Method[] methods = c.getDeclaredMethods();
                for (Method m : methods) {
                    if (m.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = m.getAnnotation(RequestMapping.class);
                        String method = annotation.method();
                        String url = annotation.path();

                        HandlerMethod h = (req, res) -> {
                            m.invoke(handlerInstance, req, res);
                        };
                        result.put(method + " " + url, h);
                    }
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        return result;
    }
}
