package dtos;

public record RegisterDto(
        // 1. Basic Identity
        String firstName,
        String lastName,
        String username,

        // 2. Contact Information
        String email,
        String phoneNumber,

        // 3. Authentication
        String password,
        String confirmPassword,

        // 4. Optional Profile Info
        String dateOfBirth,
        String gender,
        String profilePictureUrl,

        // 5. Address
        String country,
        String city,
        String streetAddress,
        String postalCode,

        // 6. Agreements
        boolean acceptedTerms,
        boolean acceptedPrivacyPolicy
) {}