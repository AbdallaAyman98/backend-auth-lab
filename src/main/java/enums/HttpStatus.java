package enums;

public enum HttpStatus {

    // 2xx — success
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),

    // 3xx — redirection
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),

    // 4xx — client errors
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),

    // 5xx — server errors
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code    = code;
        this.message = message;
    }

    public int    getCode()    { return code; }
    public String getMessage() { return message; }

    public static HttpStatus from(int code) {
        for (HttpStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }

    // convenience checks
    public boolean isSuccess()     { return code >= 200 && code < 300; }
    public boolean isClientError() { return code >= 400 && code < 500; }
    public boolean isServerError() { return code >= 500; }
}