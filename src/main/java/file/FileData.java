package file;

import core.http.StatusCode;

public record FileData(byte[] body, String fileName, String extension, StatusCode statusCode) {
    public static FileData processRedirect() {
        return new FileData(new byte[0], "", "", StatusCode.FOUND);
    }
}