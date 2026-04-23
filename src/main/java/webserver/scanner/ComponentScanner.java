package webserver.scanner;

import annotations.RequestMapping;
import interfaces.HandlerMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentScanner {

    public static Map<String, HandlerMethod> scanHandlers(String packageName) throws Exception{
        Map<String, HandlerMethod> handlers = new HashMap<>();

        List<Class<?>> classes = getClassesInPackage(packageName);
        for(Class<?> c : classes){

            Object controllerInstance = c.getDeclaredConstructor().newInstance();
            for(Method m : c.getDeclaredMethods()){
                if(m.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping annotation = m.getAnnotation(RequestMapping.class);
                    String sig = annotation.method() + " " + annotation.path();
                    HandlerMethod newHandler = (req, res, sessionManager, ta, db) -> {
                       return m.invoke(controllerInstance, req,res);
                    };
                    handlers.put(sig, newHandler);
                }
            }
        }
        return handlers;
    }

    public static List<Class<?>> getClassesInPackage(String packageName) throws ClassNotFoundException{
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace(".","/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if(resource == null){
            throw new IllegalArgumentException("Package Not Found During Component Scan : PACKAGE NAME - " + packageName);
        }

        File directory = new File(resource.getFile());
        if(directory.exists() && directory.isDirectory()){
            for(File f : directory.listFiles()){
                if(f.getName().endsWith(".class")){
                    String className = f.getName().substring(0,f.getName().length()-6);
                    classes.add(Class.forName(packageName+"."+className));
                }
            }
        }
        return classes;
    }
}
