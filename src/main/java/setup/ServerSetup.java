package setup;

import db.DBConnectionPool;
import db.RedisConnectionPool;
import handlers.AuthHandler;
import handlers.LogoutHandler;
import redis.clients.jedis.JedisPool;
import handlers.RegisterHandler;
import services.RegisterService;
import repositories.RedisRepository;
import repositories.UserRepository;
import services.AuthService;
import services.TokenService;
import handlers.UserHandler;
import services.UserService;
import utilities.PortUtil;

import javax.sql.DataSource;

public class ServerSetup {

    public static void init(int port){

        if (!PortUtil.isPortFree(port)) {
            System.err.println("Port " + port + " in use — exiting");
            System.exit(1);
        }

        // ── DB ────────────────────────────────────────────────────
        DataSource      dataSource      = DBConnectionPool.getDataSource();
        UserRepository  userRepository  = new UserRepository(dataSource);

// ── Redis ─────────────────────────────────────────────────
        JedisPool jedisPool       = RedisConnectionPool.getPool();
        RedisRepository redisRepository = new RedisRepository(jedisPool);
        TokenService tokenService    = new TokenService(redisRepository);

// ── services ──────────────────────────────────────────────
        AuthService authService     = new AuthService(userRepository, tokenService);
        RegisterService registerService = new RegisterService(userRepository);
        UserService     userService     = new UserService(userRepository);

// ── handlers ──────────────────────────────────────────────
        AuthHandler authHandler     = new AuthHandler(authService);
        RegisterHandler registerHandler = new RegisterHandler(registerService);
        UserHandler     userHandler     = new UserHandler(userService);
        LogoutHandler logoutHandler = new LogoutHandler(authService, tokenService);

// ── routes ────────────────────────────────────────────────
        AppServer server = AppServer.init(port)
                .addContext("/api/v1/auth/login",              authHandler)
                .addContext("/api/v1/auth/logout",             logoutHandler)
                .addContext("/api/v1/register",                registerHandler)
                .addContext("/api/v1/check-availability",      userHandler);

// ── shutdown ──────────────────────────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            DBConnectionPool.shutdown();
            RedisConnectionPool.shutdown();
        }));

        server.start();
    }
}


//```
//
//        ---
//
//        **Two fixes:**
//
//        | Issue | Fix |
//        |---|---|
//        | `"api/v1/check-availability"` | `"/api/v1/check-availability"` — missing leading `/` |
//        | All contexts chained on one line | Split per line — readable |
//
//        ---
//
//        **What you've built so far:**
//        ```
//POST /api/v1/register          → RegisterHandler → RegisterService → UserRepository
//POST /api/v1/login             → LoginHandler    → LoginService    → UserRepository
//GET  /api/v1/check-availability → UserHandler    → UserService     → UserRepository