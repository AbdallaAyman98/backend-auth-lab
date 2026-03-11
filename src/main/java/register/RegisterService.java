package register;
import java.sql.SQLException;
import java.time.LocalDate;

import exceptions.DuplicateUserException;
import records.RegisterDto;
import records.User;
import repositories.UserRepository;
import utilities.PasswordHasherUtil;

public class RegisterService {

    private final UserRepository userRepository;

    public RegisterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterDto dto) throws SQLException {

        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateUserException("Email already registered");
        }
        if (userRepository.existsByUsername(dto.username())) {
            throw new DuplicateUserException("Username already taken");
        }


        User user = new User(
                null,
                dto.username(),
                dto.firstName(),
                dto.lastName(),
                dto.email(),
                PasswordHasherUtil.hash(dto.password()),
                dto.phoneNumber(),
                dto.dateOfBirth() != null ? LocalDate.parse(dto.dateOfBirth()) : null,
                dto.gender(),
                dto.profilePictureUrl(),
                dto.country(),
                dto.city(),
                dto.streetAddress(),
                dto.postalCode(),
                dto.acceptedTerms(),
                dto.acceptedPrivacyPolicy(),
                false,  // isVerified — false until email confirmed
                null,   // createdAt — DB assigns
                null    // updatedAt — DB assigns
        );

        return userRepository.save(user);
    }
}