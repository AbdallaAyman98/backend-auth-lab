package authentication;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.auth0.jwt.interfaces.DecodedJWT;
import enums.HttpMethod;
import enums.HttpStatus;
import exceptions.UnauthorizedException;
import utilities.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LogoutHandler implements HttpHandler {

    private final AuthService  authService;
    private final TokenService tokenService;

    public LogoutHandler(AuthService authService, TokenService tokenService) {
        this.authService  = authService;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (HttpMethod.from(exchange.getRequestMethod()) != HttpMethod.POST) {
            respond(exchange, HttpStatus.METHOD_NOT_ALLOWED,
                    "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            // 1. extract token from Authorization header
            // Authorization: Bearer <token>
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                respond(exchange, HttpStatus.UNAUTHORIZED,
                        "{\"error\": \"Missing or invalid Authorization header\"}");
                return;
            }

            String accessToken = authHeader.substring(7); // strip "Bearer "

            // 2. verify token — throws if invalid/expired
            DecodedJWT decoded = tokenService.verifyAccessToken(accessToken);

            // 3. extract userId from token subject
            long userId = Long.parseLong(decoded.getSubject());

            // 4. logout — blacklist access token + delete refresh token
            authService.logout(accessToken, userId);

            Logger.info("User logged out: [" + userId + "]");

            respond(exchange, HttpStatus.OK,
                    "{\"message\": \"Logged out successfully\"}");

        } catch (UnauthorizedException e) {
            respond(exchange, HttpStatus.UNAUTHORIZED,
                    "{\"error\": \"Invalid or expired token\"}");

        } catch (Exception e) {
            Logger.error("Unexpected error during logout", e);
            respond(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                    "{\"error\": \"Something went wrong\"}");
        }
    }

    private void respond(HttpExchange exchange, HttpStatus status, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status.getCode(), bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}