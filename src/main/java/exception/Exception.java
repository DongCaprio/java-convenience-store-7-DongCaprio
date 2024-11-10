package exception;

public class Exception {

    public static final String ERROR_PREFIX = "[ERROR] ";

    public static void throwException(String message) {
        throw new IllegalArgumentException(ERROR_PREFIX + message);
    }

}
