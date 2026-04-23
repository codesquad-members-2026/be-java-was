package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {

    private FileUtil() {}

    public static byte[] readFile(File file) throws IOException {
        long length = file.length();

        byte[] bytes = new byte[(int) length];

        try (FileInputStream fis = new FileInputStream(file)) {
            int offset = 0;
            int numRead;

            while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        }
        return bytes;
    }
}