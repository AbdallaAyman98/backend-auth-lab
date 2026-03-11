package register;

import records.RegisterDto;
import utilities.FieldValidator;
import utilities.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class RegisterValidator {

    private RegisterValidator() {}

    public static ValidationResult validate(RegisterDto dto) {
        List<String> errors = new ArrayList<>();

        FieldValidator.validateUsername(dto.username(), errors);
        FieldValidator.validateEmail(dto.email(), errors);
        FieldValidator.validatePasswordStrength(dto.password(), errors);
        validateConfirmPassword(dto.password(), dto.confirmPassword(), errors);
        validateAgreements(dto.acceptedTerms(), dto.acceptedPrivacyPolicy(), errors);

        if (dto.phoneNumber() != null && !dto.phoneNumber().isBlank())
            validatePhoneNumber(dto.phoneNumber(), errors);
        if (dto.dateOfBirth() != null && !dto.dateOfBirth().isBlank())
            validateDateOfBirth(dto.dateOfBirth(), errors);
        if (dto.firstName() != null && !dto.firstName().isBlank())
            FieldValidator.checkForAttacks(dto.firstName(), "First name", errors);
        if (dto.lastName() != null && !dto.lastName().isBlank())
            FieldValidator.checkForAttacks(dto.lastName(), "Last name", errors);

        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    // ─── Confirm Password ─────────────────────────────────────
    private static void validateConfirmPassword(String password, String confirmPassword, List<String> errors) {
        if (confirmPassword == null || confirmPassword.isBlank()) {
            errors.add("Confirm password is required");
            return;
        }
        if (!confirmPassword.equals(password))
            errors.add("Passwords do not match");
    }

    // ─── Agreements ───────────────────────────────────────────
    private static void validateAgreements(boolean acceptedTerms, boolean acceptedPrivacyPolicy, List<String> errors) {
        if (!acceptedTerms)
            errors.add("You must accept the terms and conditions");
        if (!acceptedPrivacyPolicy)
            errors.add("You must accept the privacy policy");
    }

    // ─── Phone Number ─────────────────────────────────────────
    private static void validatePhoneNumber(String phone, List<String> errors) {
        String cleaned = phone.replaceAll("[\\s\\-()]", ""); // strip spaces, dashes, parens
        if (!cleaned.matches("^\\+?[0-9]{7,15}$"))
            errors.add("Phone number format is invalid");
    }

    // ─── Date of Birth ────────────────────────────────────────
    private static void validateDateOfBirth(String dob, List<String> errors) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dob); // expects YYYY-MM-DD

            // must be in the past
            if (!date.isBefore(java.time.LocalDate.now()))
                errors.add("Date of birth must be in the past");

            // must be realistic — no older than 120 years
            if (date.isBefore(java.time.LocalDate.now().minusYears(120)))
                errors.add("Date of birth is not valid");

            // must be at least 13 years old
            if (date.isAfter(java.time.LocalDate.now().minusYears(13)))
                errors.add("You must be at least 13 years old to register");

        } catch (java.time.format.DateTimeParseException e) {
            errors.add("Date of birth must be in format YYYY-MM-DD");
        }
    }
}