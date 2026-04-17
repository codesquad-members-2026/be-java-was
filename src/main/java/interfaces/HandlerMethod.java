package interfaces;

import jhttp.HttpRequest;
import jhttp.HttpResponse;
import model.TemplateAttributes;
import webserver.session.SessionManager;

import java.io.InvalidClassException;
import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface HandlerMethod {
    public Object handle(HttpRequest request, HttpResponse response, SessionManager sessionManager, TemplateAttributes ta) throws InvocationTargetException, IllegalAccessException, InvalidClassException;
}
