package enums;

public enum DBProperty {

    HOST("APP_DB_HOST"),
    PORT("APP_DB_PORT"),
    USERNAME("APP_DB_USERNAME"),
    PASSWORD("APP_DB_PASSWORD"),
    DB_NAME("APP_DB_NAME");

    private static final String PREFIX = "APP_DB";
    private final String key;

    DBProperty(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    // bonus — auto-build key from enum name
    public String prefixedKey() {
        return PREFIX + "_" + this.name(); // e.g. APP_DB_HOST
    }
}