package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtil {

    private FileUtil() {}

    public static byte[] readFile(File file) throws IOException {
        long length = file.length();

        // TODO: (int) 형변환 시 발생할 수 있는 데이터 오버플로우를 방지할 안전장치를 고민해 보세요.
        byte[] bytes = new byte[(int) length];

        try (FileInputStream fis = new FileInputStream(file)) {
            int offset = 0;
            int numRead;

            while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        }

        // TODO: 대용량 파일을 읽을 때 메모리 부족(OutOfMemory)을 방지하기 위해
        // byte[] 대신 스트림(Stream)을 직접 사용하는 구조로 변경하는 시나리오를 구상해 보세요.
        return bytes;
    }
}