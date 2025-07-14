package server;

public class NoSuchEndpoint extends RuntimeException {
    public NoSuchEndpoint(String message) {
        super(message);
    }

    public NoSuchEndpoint(String message, Throwable cause) {
        super(message, cause);
    }
}
