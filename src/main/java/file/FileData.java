package file;

import core.http.StatusCode;

import java.io.ByteArrayOutputStream;

public record FileData(ByteArrayOutputStream body, String fileName, String extension, StatusCode statusCode) {
    public static FileData processRedirect() {
        return new FileData(new ByteArrayOutputStream(), "", "", StatusCode.FOUND);
    }
}