package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    private static Level globalLevel = Level.INFO;
    private static boolean logToFile = true;
    private static String logFile = "application.log";

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static BufferedWriter writer;

    static {
        if (logToFile) {
            try {
                writer = new BufferedWriter(new FileWriter(logFile, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setLevel(Level level) {
        globalLevel = level;
    }

    // ── TRACE ─────────────────────────────────────────────
    public static void trace(String message) {
        log(Level.TRACE, message);
    }

    public static void trace(String message, Throwable t) {
        log(Level.TRACE, message, t);
    }

    // ── DEBUG ─────────────────────────────────────────────
    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void debug(String message, Throwable t) {
        log(Level.DEBUG, message, t);
    }

    // ── INFO ──────────────────────────────────────────────
    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void info(String message, Throwable t) {
        log(Level.INFO, message, t);
    }

    // ── WARN ──────────────────────────────────────────────
    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void warn(String message, Throwable t) {
        log(Level.WARN, message, t);
    }

    // ── ERROR ─────────────────────────────────────────────
    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Throwable t) {
        log(Level.ERROR, message, t);
    }

    // ── Core logging methods ──────────────────────────────
    private static synchronized void log(Level level, String message) {
        log(level, message, null);
    }

    private static synchronized void log(Level level, String message, Throwable t) {

        if (level.ordinal() < globalLevel.ordinal())
            return;

        String timestamp = LocalDateTime.now().format(formatter);
        String thread = Thread.currentThread().getName();
        String caller = getCaller();

        StringBuilder formatted = new StringBuilder();
        formatted.append(String.format(
                "%s [%s] [%s] (%s) - %s",
                timestamp,
                level,
                thread,
                caller,
                message
        ));

        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            formatted.append("\n").append(sw.toString());
        }

        String output = formatted.toString();

        System.out.println(output);

        if (logToFile && writer != null) {
            try {
                writer.write(output);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getCaller() {

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stack) {
            if (!element.getClassName().equals(Logger.class.getName())
                    && !element.getClassName().contains("java.lang.Thread")) {

                return element.getClassName() + ":" + element.getLineNumber();
            }
        }

        return "Unknown";
    }
}