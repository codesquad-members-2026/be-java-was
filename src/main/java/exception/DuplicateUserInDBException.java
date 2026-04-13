package exception;

public class DuplicateUserInDBException extends RuntimeException{
    public DuplicateUserInDBException(String message) {
        super(message);
    }
}
