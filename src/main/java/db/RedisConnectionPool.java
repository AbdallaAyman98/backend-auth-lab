package db;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionPool {

    private static JedisPool pool;

    private RedisConnectionPool() {}

    public static JedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);
            config.setMaxIdle(5);
            config.setMinIdle(1);

            pool = new JedisPool(
                    config,
                    ".ec2.cloud.redislabs.com",
                    14829,
                    2000,
                    "default",
                    "YourPassword"
            );
        }
        return pool;
    }

    public static void shutdown() {
        if (pool != null) pool.close();
    }
}