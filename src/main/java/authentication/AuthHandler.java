package authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.HttpMethod;
import enums.HttpStatus;
import exceptions.FieldValidationException;
import exceptions.InexistentUserException;
import exceptions.UnauthorizedException;
import exceptions.UnverifiedUserException;
import login.LoginDto;
import login.LoginResponseDto;
import utilities.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class AuthHandler implements HttpHandler {

    private final AuthService  authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (HttpMethod.from(exchange.getRequestMethod()) != HttpMethod.POST) {
            respond(exchange, HttpStatus.METHOD_NOT_ALLOWED,
                    "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            // 1. read body
            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            // 2. check body not empty
            if (body.isBlank()) {
                respond(exchange, HttpStatus.BAD_REQUEST,
                        "{\"error\": \"Request body is required\"}");
                return;
            }

            // 3. parse JSON → DTO
            LoginDto dto = objectMapper.readValue(body, LoginDto.class);

            // ✅ log email only — never log passwords
            Logger.info("Login attempt: [" + dto.email() + "]");

            // 4. delegate to service — returns both tokens
            LoginResponseDto response = authService.login(dto);

            // 5. serialize response with ObjectMapper — no manual JSON strings
            respond(exchange, HttpStatus.OK,
                    objectMapper.writeValueAsString(response));

        } catch (FieldValidationException e) {
            String errorsJson = e.getErrors().stream()
                    .map(err -> "\"" + err + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
            respond(exchange, HttpStatus.BAD_REQUEST,
                    "{\"errors\": " + errorsJson + "}");

        } catch (UnverifiedUserException e) {
            respond(exchange, HttpStatus.FORBIDDEN,
                    "{\"error\": \"Please verify your email\"}");

        } catch (InexistentUserException | UnauthorizedException e) {
            respond(exchange, HttpStatus.UNAUTHORIZED,
                    "{\"error\": \"Invalid credentials\"}");

        } catch (Exception e) {
            Logger.error("Unexpected error during login", e);
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