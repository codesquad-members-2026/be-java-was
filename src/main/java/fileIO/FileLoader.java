package fileIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.io.InputStream;

public class FileLoader {
    private static final Logger logger = LoggerFactory.getLogger(FileLoader.class);
    public static byte[] getStaticFile(String fileName) throws IOException {

        String staticFilePath =  (fileName);
        try(InputStream resourceAsStream = FileLoader.class.getResourceAsStream(staticFilePath)){
            return resourceAsStream.readAllBytes();
        }
    }
}
