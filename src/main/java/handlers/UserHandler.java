package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.HttpMethod;
import exceptions.DuplicateUserException;
import exceptions.FieldValidationException;
import services.UserService;
import utilities.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class UserHandler implements HttpHandler {

    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (HttpMethod.from(exchange.getRequestMethod()) != HttpMethod.GET) {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
            return;
        }

        try {
            String query           = exchange.getRequestURI().getQuery();
            String emailToCheck    = getQueryParam(query, "email");
            String usernameToCheck = getQueryParam(query, "username");

            Logger.info("Query params received: email: " + emailToCheck + ", username: " + usernameToCheck);

            userService.checkAvailability(emailToCheck, usernameToCheck);

            sendResponse(exchange, 200, "{\"available\": true}");

        } catch (FieldValidationException e) {
            String errorsJson = e.getErrors().stream()
                    .map(err -> "\"" + err + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));
            sendResponse(exchange, 400, "{\"errors\": " + errorsJson + "}");

        } catch (DuplicateUserException e) {
            sendResponse(exchange, 409, "{\"available\": false, \"message\": \"Email or username already taken\"}");

        } catch (SQLException e) {
            Logger.error("Database error during availability check", e);
            sendResponse(exchange, 500, "{\"error\": \"An internal error occurred\"}");

        } catch (IOException e) {
            sendResponse(exchange, 500, "{\"error\": \"An internal error occurred\"}");
        }catch (Exception e) {
            Logger.error("Unexpected error during availability check", e);
            sendResponse(exchange, 500, "{\"error\": \"An internal error occurred\"}");
        }
    }

    // ── extract value from query string ───────────────────────
    // e.g. "email=john@gmail.com&username=john" → "john@gmail.com"
    private String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}
