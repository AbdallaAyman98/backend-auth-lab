package utilities;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvReader {

    private static Dotenv dotenv;

    private EnvReader() {}

    // loads .env from project root (default)
    public static void load() {
        dotenv = Dotenv.load();
    }

    // loads custom file from a given directory e.g. load("src/main/resources", "db.env")
    public static void load(String directory, String filename) {
        dotenv = Dotenv.configure()
                .directory(directory)
                .filename(filename)
                .load();
    }

    private static void ensureLoaded() {
        if (dotenv == null) throw new RuntimeException("utilities.EnvReader not loaded. Call utilities.EnvReader.load() first.");
    }

    public static String get(String key) {
        ensureLoaded();
        String value = dotenv.get(key);
        if (value == null) throw new RuntimeException("Missing .env key: " + key);
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        ensureLoaded();
        String value = dotenv.get(key, defaultValue);
        return (value != null) ? value.trim() : defaultValue;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static int getInt(String key, int defaultValue) {
        try { return Integer.parseInt(get(key)); } catch (Exception e) { return defaultValue; }
    }

    public static long getLong(String key) {
        return Long.parseLong(get(key));
    }

    public static long getLong(String key, long defaultValue) {
        try { return Long.parseLong(get(key)); } catch (Exception e) { return defaultValue; }
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        try { return Boolean.parseBoolean(get(key)); } catch (Exception e) { return defaultValue; }
    }
}