package exceptions;

public class InexistentUserException extends RuntimeException {
    public InexistentUserException(String message){
        super(message);
    }
}
