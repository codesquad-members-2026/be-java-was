package file;

import exception.InternalServerException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileContentReader {

    public static byte[] extract(File file){
        // TODO: 왜 8KB 만큼 퍼오는가? 이렇게 했을 때의 트레이드 오프는?
        byte[] buffer = new byte[8192];
        int readCount;

        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            while((readCount = fis.read(buffer)) != -1){
                bos.write(buffer, 0, readCount);
            }

            return bos.toByteArray();

        } catch (IOException ie) {
            throw new InternalServerException("A fatal error occurred while reading file data: " + file.getName() + " " + ie);
        }
    }
}
