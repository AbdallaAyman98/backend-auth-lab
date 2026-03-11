package enums;

public enum AppProperty {

    JWT_SECRET("JWT_SECRET");

    private final String key;

    AppProperty(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}