package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReaderUtil {

    private static final Properties properties = new Properties();

    private ConfigReaderUtil() {}


    public static void load(String filePath) {
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + filePath, e);
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) throw new RuntimeException("Missing config key: " + key);
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
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