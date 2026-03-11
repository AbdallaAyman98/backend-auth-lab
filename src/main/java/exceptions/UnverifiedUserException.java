package exceptions;

public class UnverifiedUserException extends RuntimeException {
    public UnverifiedUserException(String message){
        super(message);
    }
}
