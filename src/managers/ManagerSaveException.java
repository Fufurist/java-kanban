package managers;

public class ManagerSaveException extends RuntimeException {
    ManagerSaveException(String message) {
        super(message);
    }

    ManagerSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
