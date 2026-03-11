package services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dtos.RefreshResponseDto;
import exceptions.UnauthorizedException;
import records.User;
import configs.AppConfig;
import repositories.RedisRepository;

import java.util.Date;
import java.util.UUID;

public class TokenService {

    private final RedisRepository redisRepository;

    // ── TTL constants ─────────────────────────────────────────
    private static final int ACCESS_TOKEN_TTL  = 15 * 60;           // 15 minutes
    private static final int REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60;  // 7 days

    // ── key prefixes ──────────────────────────────────────────
    private static final String REFRESH_PREFIX   = "refresh_token:";
    private static final String BLACKLIST_PREFIX  = "blacklist:";

    public TokenService(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    // ── generate access token — short lived JWT ───────────────
    public String generateAccessToken(User user) {
        String secret = getSecret();

        return JWT.create()
                .withSubject(String.valueOf(user.id()))
                .withClaim("username", user.username())
                .withClaim("email", user.email())
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_TTL * 1000L))
                .sign(Algorithm.HMAC256(secret));
    }

    // ── generate refresh token — opaque token stored in Redis ─
    public String generateRefreshToken(long userId) {
        String token = UUID.randomUUID().toString();

        redisRepository.set(
                REFRESH_PREFIX + userId,
                token,
                REFRESH_TOKEN_TTL
        );

        return token;
    }

    // ── verify access token ───────────────────────────────────
    public DecodedJWT verifyAccessToken(String token) {
        try {
            DecodedJWT decoded = JWT.require(Algorithm.HMAC256(getSecret()))
                    .build()
                    .verify(token);

            // ✅ check blacklist
            if (redisRepository.exists(BLACKLIST_PREFIX + token))
                throw new UnauthorizedException("Token has been revoked");

            return decoded;

        } catch (JWTVerificationException e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    // ── refresh — rotate refresh token, return new access token
    public RefreshResponseDto refresh(long userId, String providedRefreshToken) {
        String storedToken = redisRepository.get(REFRESH_PREFIX + userId);

        if (storedToken == null)
            throw new UnauthorizedException("Session expired — please login again");

        if (!storedToken.equals(providedRefreshToken))
            throw new UnauthorizedException("Invalid refresh token");

        // ✅ rotate — delete old, issue new
        redisRepository.delete(REFRESH_PREFIX + userId);

        // need user to generate new access token — fetch from caller
        String newRefreshToken = generateRefreshToken(userId);
        return new RefreshResponseDto(newRefreshToken);
    }

    // ── revoke access token — blacklist until expiry ──────────
    public void revokeAccessToken(String accessToken) {
        try {
            DecodedJWT decoded    = JWT.decode(accessToken);
            long      remainingTtl = (decoded.getExpiresAt().getTime() - System.currentTimeMillis()) / 1000;

            if (remainingTtl > 0)
                redisRepository.set(BLACKLIST_PREFIX + accessToken, "revoked", (int) remainingTtl);

        } catch (Exception e) {
            // already invalid — nothing to revoke
        }
    }

    // ── delete refresh token — on logout ─────────────────────
    public void deleteRefreshToken(long userId) {
        redisRepository.delete(REFRESH_PREFIX + userId);
    }

    // ── private helpers ───────────────────────────────────────
    private String getSecret() {
        String secret = AppConfig.getInstance().getJwtSecret();
        if (secret == null || secret.isBlank())
            throw new IllegalStateException("JWT_SECRET env variable not set");
        return secret;
    }
}