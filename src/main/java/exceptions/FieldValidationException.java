package exceptions;

import java.util.List;

public class FieldValidationException extends RuntimeException {

    private final List<String> errors;

    public FieldValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() { return errors; }
}