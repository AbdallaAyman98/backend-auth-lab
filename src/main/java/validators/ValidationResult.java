package validators;

import exceptions.FieldValidationException;

import java.util.List;

// ValidationResult has a helper that throws if invalid
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid  = valid;
        this.errors = errors;
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(String error) {
        return new ValidationResult(false, List.of(error));
    }

    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    public boolean isValid()        { return valid; }
    public List<String> getErrors() { return errors; }

    public void throwIfInvalid() {
        if (!valid) throw new FieldValidationException(errors);
    }
}