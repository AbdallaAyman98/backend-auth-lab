package enums;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS");

    private final String value;

    HttpMethod(String value) {
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public String toString() { return value; }

    public static HttpMethod from(String method) {
        try {
            return valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown HTTP method: " + method);
        }
    }
}