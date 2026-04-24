package webserver.resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import webserver.exception.PageNotFoundException;

public class ResourceLoader {
    public byte[] loadAsBytes(String resourcePath) throws IOException {
        File file = new File("src/main/resources/static" + resourcePath);

        if (!file.exists() || !file.isFile()) {
            throw new PageNotFoundException("리소스를 찾을 수 없음");
        }

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return in.readAllBytes();
        }
    }

    public String loadAsString(String resourcePath) throws IOException {
        return new String(loadAsBytes(resourcePath), StandardCharsets.UTF_8);
    }

}
