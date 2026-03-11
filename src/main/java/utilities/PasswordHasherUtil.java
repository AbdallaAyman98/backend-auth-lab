package utilities;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.util.Arrays;

public class PasswordHasherUtil {

    // ── Argon2id instance — created once, thread safe ─────────
    private static final Argon2 ARGON2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id
    );

    // ── tuning parameters ──────────────────────────────────────
    private static final int ITERATIONS  = 3;      // time cost
    private static final int MEMORY      = 65536;  // 64MB memory cost
    private static final int PARALLELISM = 2;      // threads

    // ── private constructor — no instantiation, static use only
    private PasswordHasherUtil() {}

    // ── hash a plain text password ────────────────────────────
    public static String hash(String plainPassword) {
        char[] chars = plainPassword.toCharArray();
        try {
            return ARGON2.hash(ITERATIONS, MEMORY, PARALLELISM, chars);
        } finally {
            Arrays.fill(chars, '\0'); //wipe from memory immediately after use
        }
    }

    // ── verify plain text against stored hash ─────────────────
    public static boolean verify(String plainPassword, String storedHash) {
        char[] chars = plainPassword.toCharArray();
        try {
            return ARGON2.verify(storedHash, chars);
        } finally {
            Arrays.fill(chars, '\0'); //
        }
    }

    // ── check if hash needs rehashing ─────────────────────────
    // useful when you upgrade tuning parameters in the future
    public static boolean needsRehash(String storedHash) {
        return ARGON2.needsRehash(storedHash, ITERATIONS, MEMORY, PARALLELISM);
    }
}