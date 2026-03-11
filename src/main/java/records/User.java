package records;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record User(
        Long          id,
        String        username,
        String        firstName,
        String        lastName,
        String        email,
        String        passwordHash,
        String        phoneNumber,
        LocalDate     dateOfBirth,
        String        gender,
        String        profilePictureUrl,
        String        country,
        String        city,
        String        streetAddress,
        String        postalCode,
        boolean       acceptedTerms,
        boolean       acceptedPrivacyPolicy,
        boolean       isVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}