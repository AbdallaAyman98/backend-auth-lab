package authentication;

import exceptions.InexistentUserException;
import exceptions.UnauthorizedException;
import exceptions.UnverifiedUserException;
import login.LoginDto;
import login.LoginResponseDto;
import login.LoginValidator;
import records.User;
import repositories.UserRepository;
import utilities.PasswordHasherUtil;

import java.sql.SQLException;

public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService   = tokenService;
    }

    // ── login ─────────────────────────────────────────────────
    public LoginResponseDto login(LoginDto dto) throws SQLException {

        LoginValidator.validate(dto).throwIfInvalid();

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InexistentUserException("Invalid credentials"));

        if (!PasswordHasherUtil.verify(dto.password(), user.passwordHash()))
            throw new UnauthorizedException("Invalid credentials");

        if (!user.isVerified())
            throw new UnverifiedUserException("Please verify your email first");

        String accessToken  = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user.id());

        return new LoginResponseDto(accessToken, refreshToken);
    }

    // ── logout ────────────────────────────────────────────────
    public void logout(String accessToken, long userId) {
        tokenService.revokeAccessToken(accessToken);
        tokenService.deleteRefreshToken(userId);
    }

    // ── refresh ───────────────────────────────────────────────
    public RefreshResponseDto refresh(long userId, String refreshToken) {
        return tokenService.refresh(userId, refreshToken);
    }
}