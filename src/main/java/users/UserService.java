package users;

import exceptions.DuplicateUserException;
import exceptions.FieldValidationException;
import utilities.FieldValidator;
import repositories.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── check email availability only ─────────────────────────
    public void checkEmailAvailability(String email) throws SQLException {
        FieldValidator.validateEmail(email).throwIfInvalid();

        if (userRepository.isDuplicateEmail(email))
            throw new DuplicateUserException("Email already registered");
    }

    // ── check username availability only ──────────────────────
    public void checkUsernameAvailability(String username) throws SQLException {
        FieldValidator.validateUsername(username).throwIfInvalid();

        if (userRepository.isDuplicateUsername(username))
            throw new DuplicateUserException("Username already taken");
    }

    // ── check both — used when both params provided ───────────
    public void checkBothAvailability(String email, String username) throws SQLException {
        FieldValidator.validateEmail(email).throwIfInvalid();
        FieldValidator.validateUsername(username).throwIfInvalid();

        userRepository.findDuplicate(email, username).ifPresent(field -> {
            throw new DuplicateUserException(field.equals("email")
                    ? "Email already registered"
                    : "Username already taken");
        });
    }

    // ── entry point — routes to correct check ─────────────────
    public void checkAvailability(String email, String username) throws SQLException {

        if (email == null && username == null)
            throw new FieldValidationException(List.of("At least one field must be provided"));

        if (email != null && username != null) {
            checkBothAvailability(email, username);  // both provided
        } else if (email != null) {
            checkEmailAvailability(email);           // email only
        } else {
            checkUsernameAvailability(username);     // username only
        }
    }
}
