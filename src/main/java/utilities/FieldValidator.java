package utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FieldValidator {

    // ─── Email ────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    // ─── Password Strength ────────────────────────────────────
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT     = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_SPECIAL   = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

    // ─── Repeated Characters ──────────────────────────────────
    private static final Pattern REPEATED_CHARS = Pattern.compile(
            "(.)\\1{2,}"  // same char 3+ times e.g. "aaa", "111", "!!!"
    );

    // ─── Sequential Characters ────────────────────────────────
    private static final Pattern SEQUENTIAL_CHARS = Pattern.compile(
            "(?i)(abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz|" +
                    "012|123|234|345|456|567|678|789|" +
                    "987|876|765|654|543|432|321|210|" +
                    "zyx|yxw|xwv|wvu|vut|uts|tsr|srq|rqp|qpo|pon|onm|nml|mlk|lkj|kji|jih|ihg|hgf|gfe|fed|edc|dcb|cba)"
    );

    // ─── Keyboard Patterns ────────────────────────────────────
    private static final Pattern KEYBOARD_PATTERNS = Pattern.compile(
            "(?i)(qwerty|asdfgh|zxcvbn|qweasd|asdzxc|" +
                    "qazwsx|wsxedc|edcrfv|rfvtgb|tgbyhn|" +
                    "password|letmein|welcome|admin123)"
    );

    // ─── Repeating Groups ─────────────────────────────────────
    private static final Pattern REPEATING_GROUPS = Pattern.compile(
            "(.{2,})\\1+"  // any group of 2+ chars repeated e.g. "abcabc", "abab"
    );

    // ─── Attack Detection ─────────────────────────────────────
    private static final Pattern SQL_INJECTION = Pattern.compile(
            "(?i)(--|;|'|\"|\\bOR\\b|\\bAND\\b|\\bDROP\\b|\\bSELECT\\b|\\bINSERT\\b|" +
                    "\\bDELETE\\b|\\bUPDATE\\b|\\bUNION\\b|\\bEXEC\\b|\\bSCRIPT\\b|xp_)"
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script|<iframe|javascript:|onerror=|onload=|" +
                    "onclick=|<img|<svg|alert\\(|document\\.cookie)"
    );
    private static final Pattern PATH_TRAVERSAL = Pattern.compile(
            "(\\.\\./|\\.\\.\\\\|%2e%2e|%252e)"
    );

    // ─── Limits ───────────────────────────────────────────────
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final int MAX_EMAIL_LENGTH    = 254;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MIN_DISTINCT_CHARS  = 4;

    private FieldValidator() {}


    // ─── Single Overloaded field check ────────────────────────────────────────────────
    public static ValidationResult validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        validateEmail(email, errors);  // calls existing method
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    public static ValidationResult validateUsername(String username) {
        List<String> errors = new ArrayList<>();
        validateUsername(username, errors);
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    public static ValidationResult validatePasswordStrength(String password) {
        List<String> errors = new ArrayList<>();
        validatePasswordStrength(password, errors);
        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    // ─── Email ────────────────────────────────────────────────
    public static void validateEmail(String email, List<String> errors) {
        if (email == null || email.isBlank()) {
            errors.add("Email is required");
            return;
        }
        email = email.trim().toLowerCase();
        if (email.length() > MAX_EMAIL_LENGTH)
            errors.add("Email is too long");
        if (!EMAIL_PATTERN.matcher(email).matches())
            errors.add("Email format is invalid");
        checkForAttacks(email, "Email", errors);
    }

    // ─── Username ─────────────────────────────────────────────
    public static void validateUsername(String username, List<String> errors) {
        if (username == null || username.isBlank()) {
            errors.add("Username is required");
            return;
        }
        username = username.trim();
        if (username.length() < MIN_USERNAME_LENGTH)
            errors.add("Username must be at least " + MIN_USERNAME_LENGTH + " characters");
        if (username.length() > MAX_USERNAME_LENGTH)
            errors.add("Username must be under " + MAX_USERNAME_LENGTH + " characters");
        if (!username.matches("^[a-zA-Z0-9_.-]+$"))
            errors.add("Username can only contain letters, numbers, underscores, dots, hyphens");
        checkForAttacks(username, "Username", errors);
    }

    // ─── Password Presence — login ────────────────────────────
    public static void validatePasswordPresence(String password, List<String> errors) {
        if (password == null || password.isBlank())
            errors.add("Password is required");
    }

    // ─── Password Strength — registration / reset ─────────────
    public static void validatePasswordStrength(String password, List<String> errors) {
        if (password == null || password.isBlank()) {
            errors.add("Password is required");
            return;
        }

        // ── length ────────────────────────────────────────────
        if (password.length() < MIN_PASSWORD_LENGTH)
            errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        if (password.length() > MAX_PASSWORD_LENGTH)
            errors.add("Password must be under " + MAX_PASSWORD_LENGTH + " characters");

        // ── character type requirements ───────────────────────
        if (!PASSWORD_UPPERCASE.matcher(password).matches())
            errors.add("Password must contain at least one uppercase letter");
        if (!PASSWORD_LOWERCASE.matcher(password).matches())
            errors.add("Password must contain at least one lowercase letter");
        if (!PASSWORD_DIGIT.matcher(password).matches())
            errors.add("Password must contain at least one number");
        if (!PASSWORD_SPECIAL.matcher(password).matches())
            errors.add("Password must contain at least one special character");

        // ── illegal characters ────────────────────────────────
        if (password.contains("\0"))
            errors.add("Password contains illegal characters");

        // ── distinct characters ───────────────────────────────
        if (password.chars().distinct().count() < MIN_DISTINCT_CHARS)
            errors.add("Password must contain at least " + MIN_DISTINCT_CHARS + " distinct characters");

        // ── repeated same char e.g. "aaa", "111" ─────────────
        if (REPEATED_CHARS.matcher(password).find())
            errors.add("Password must not contain repeated characters (e.g. 'aaa', '111')");

        // ── repeating char blocks e.g. "aaabbb", "aabbcc" ────
        if (hasRepeatingCharBlocks(password))
            errors.add("Password must not contain repeating character blocks (e.g. 'aaabbb', 'aabbcc')");

        // ── repeating groups e.g. "abcabc", "abab" ───────────
        if (REPEATING_GROUPS.matcher(password).find())
            errors.add("Password must not contain repeating patterns (e.g. 'abcabc', 'abab')");

        // ── sequential chars e.g. "abc", "123", "987" ────────
        if (SEQUENTIAL_CHARS.matcher(password).find())
            errors.add("Password must not contain sequential characters (e.g. 'abc', '123')");

        // ── keyboard patterns e.g. "qwerty", "asdfgh" ────────
        if (KEYBOARD_PATTERNS.matcher(password).find())
            errors.add("Password must not contain keyboard patterns (e.g. 'qwerty', 'asdfgh')");
    }

    // ─── Repeating Char Blocks ────────────────────────────────
    // detects e.g. "aaabbb", "aabbcc", "aaabbbccc"
    private static boolean hasRepeatingCharBlocks(String password) {
        int i             = 0;
        int repeatBlocks  = 0;

        while (i < password.length()) {
            char current   = password.charAt(i);
            int blockStart = i;

            while (i < password.length() && password.charAt(i) == current) {
                i++;
            }

            int blockLength = i - blockStart;
            if (blockLength >= 2) repeatBlocks++;
        }

        return repeatBlocks >= 2; // "aaabbb" → 2 blocks → flagged
    }

    // ─── Attack Detection ─────────────────────────────────────
    public static void checkForAttacks(String value, String fieldName, List<String> errors) {
        if (SQL_INJECTION.matcher(value).find())
            errors.add(fieldName + " contains invalid characters");
        if (XSS_PATTERN.matcher(value).find())
            errors.add(fieldName + " contains invalid characters");
        if (PATH_TRAVERSAL.matcher(value).find())
            errors.add(fieldName + " contains invalid characters");
    }



    public static void validatePasswordForLogin(String password, List<String> errors) {
        if (password == null || password.isBlank()) {
            errors.add("Password is required");
            return;
        }
        if (password.length() > MAX_PASSWORD_LENGTH)
            errors.add("Password is too long");
        if (password.contains("\0"))
            errors.add("Password contains illegal characters");

        // ✅ attack detection
        checkForAttacks(password, "Password", errors);
    }





}