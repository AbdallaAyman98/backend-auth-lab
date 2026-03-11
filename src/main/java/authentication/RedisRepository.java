package authentication;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisRepository {

    private final JedisPool pool;

    public RedisRepository(JedisPool pool) {
        this.pool = pool;
    }

    // ── set with TTL ──────────────────────────────────────────
    public void set(String key, String value, int ttlSeconds) {
        try (Jedis jedis = pool.getResource()) {
            jedis.setex(key, ttlSeconds, value);
        }
    }

    // ── get ───────────────────────────────────────────────────
    public String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    // ── delete ────────────────────────────────────────────────
    public void delete(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }
    }

    // ── exists ────────────────────────────────────────────────
    public boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key);
        }
    }

    // ── ttl remaining ─────────────────────────────────────────
    public long ttl(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.ttl(key);
        }
    }
}