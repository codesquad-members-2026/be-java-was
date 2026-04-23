package webserver.response.template;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ContextLookup {
    public static Object lookup(String key, Object context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Map<?, ?> map) {
            return map.get(key);
        }

        String cap = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        try {
            Method method = context.getClass().getMethod("get" + cap);
            return method.invoke(context);
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Field field = context.getClass().getField(key);
            return field.get(context);
        } catch (NoSuchFieldException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }


    public static boolean isFalsy(Object value) {
        return switch (value) {
            case Boolean b -> !b;
            case Iterable<?> iterable -> !iterable.iterator().hasNext();
            case null -> true;
            default -> false;
        };
    }
}
