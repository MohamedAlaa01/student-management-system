package boubyan.com.studentmanagementsystem.exception;

public class UniqueConstraintViolationException extends RuntimeException {

    public UniqueConstraintViolationException(final String message) {
        super(message);
    }
}