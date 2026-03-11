package setup;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class AppServer {

    private static AppServer instance;
    private final HttpServer server;

    private AppServer(HttpServer server) {
        this.server = server;
    }

    public static AppServer init(int port) {
        if (instance != null) {
            throw new IllegalStateException("Server already initialized");
        }
        try {
            InetSocketAddress address    = new InetSocketAddress("localhost", port);
            HttpServer        httpServer = HttpServer.create(address, 0);
            httpServer.setExecutor(Executors.newFixedThreadPool(10));
            instance = new AppServer(httpServer);
            return instance;
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            throw new RuntimeException("Server initialization failed", e);
        }
    }

    public static AppServer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Server not initialized — call init() first");
        }
        return instance;
    }

    public AppServer addContext(String path, HttpHandler handler) {
        HttpContext context = server.createContext(path, handler);
        context.getFilters().add(new CorsFilter());
        return this;
    }

    public void start() {
        server.start();
        System.out.println("Server running on http://localhost:"
                + ((InetSocketAddress) server.getAddress()).getPort());
    }

    public void stop() {
        server.stop(0);
        instance = null;
    }

    // ── CORS Filter ───────────────────────────────────────────────────────────

    private static class CorsFilter extends Filter {

        @Override
        public String description() {
            return "CORS filter";
        }

        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // Intercept preflight — browser sends OPTIONS before every cross-origin POST
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            chain.doFilter(exchange);
        }
    }
}