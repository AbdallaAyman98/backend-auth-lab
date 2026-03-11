package register;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.HttpMethod;
import exceptions.DuplicateUserException;
import exceptions.FieldValidationException;
import records.RegisterDto;
import utilities.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class RegisterHandler implements HttpHandler {

    private final RegisterService registerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegisterHandler(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public void handle(HttpExchange objExchange) throws IOException {

        if (HttpMethod.from(objExchange.getRequestMethod()) != HttpMethod.POST) {
            sendResponse(objExchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            String strReqJsonBody = new String(
                    objExchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            // 1. parse
            RegisterDto dto = objectMapper.readValue(strReqJsonBody, RegisterDto.class);

            // 2. validate — throws FieldValidationException if invalid
            RegisterValidator.validate(dto).throwIfInvalid();

            // 3. process
            registerService.register(dto);


            sendResponse(objExchange, 201, "{\"message\": \"User registered successfully\"}");

        } catch (FieldValidationException e) {
            String errorsJson = e.getErrors().stream()
                    .map(err -> "\"" + err + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
            sendResponse(objExchange, 400, "{\"errors\": " + errorsJson + "}");

        } catch (DuplicateUserException e) {
            sendResponse(objExchange, 409, "{\"error\": \"Email or username already taken\"}");

        } catch (SQLException e) {
            // log full details server side — never send to client
            Logger.error("Database error during registration", e);
            sendResponse(objExchange, 500, "{\"error\": \"An internal error occurred\"}");

        } catch (Exception e) {
            Logger.error("Unexpected error during registration", e);
            sendResponse(objExchange, 500, "{\"error\": \"An internal error occurred\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}