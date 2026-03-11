package validators;

import dtos.LoginDto;

import java.util.ArrayList;
import java.util.List;

public class LoginValidator {

    private LoginValidator() {}

    public static ValidationResult validate(LoginDto dto) {
        List<String> errors = new ArrayList<>();

        FieldValidator.validateEmail(dto.email(), errors);
        FieldValidator.validatePasswordForLogin(dto.password(), errors);

        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }
}