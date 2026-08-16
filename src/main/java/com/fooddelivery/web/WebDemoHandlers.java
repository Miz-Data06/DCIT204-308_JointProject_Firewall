package com.fooddelivery.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class WebDemoHandlers {
    private final WebOrderService service;
    private final Path staticRoot;

    public WebDemoHandlers(WebOrderService service, Path staticRoot) {
        if (service == null || staticRoot == null) {
            throw new IllegalArgumentException("Service and static root must not be null.");
        }
        this.service = service;
        this.staticRoot = staticRoot.toAbsolutePath().normalize();
    }

    public HttpHandler api() {
        return exchange -> {
            try {
                routeApi(exchange);
            } catch (IllegalArgumentException exception) {
                sendJson(exchange, 400, WebJson.error(exception.getMessage()));
            } catch (RuntimeException exception) {
                sendJson(exchange, 500, WebJson.error("Server error: " + exception.getMessage()));
            }
        };
    }

    public HttpHandler staticFiles() {
        return exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
                return;
            }
            URI uri = exchange.getRequestURI();
            String rawPath = uri.getPath().equals("/") ? "/index.html" : uri.getPath();
            Path target = staticRoot.resolve(rawPath.substring(1)).normalize();
            if (!target.startsWith(staticRoot) || !Files.exists(target) || Files.isDirectory(target)) {
                sendText(exchange, 404, "Not found", "text/plain; charset=utf-8");
                return;
            }
            byte[] bytes = Files.readAllBytes(target);
            exchange.getResponseHeaders().set("Content-Type", contentType(target));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        };
    }

    private void routeApi(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if ("GET".equals(method) && "/api/health".equals(path)) {
            sendJson(exchange, 200, service.healthJson());
        } else if ("GET".equals(method) && "/api/counts".equals(path)) {
            sendJson(exchange, 200, service.countsJson());
        } else if ("GET".equals(method) && "/api/locations".equals(path)) {
            sendJson(exchange, 200, service.locationsJson(false));
        } else if ("GET".equals(method) && "/api/restaurants".equals(path)) {
            sendJson(exchange, 200, service.locationsJson(true));
        } else if ("GET".equals(method) && "/api/recent-orders".equals(path)) {
            sendJson(exchange, 200, service.recentOrdersJson());
        } else if ("GET".equals(method) && "/api/order".equals(path)) {
            sendJson(exchange, 200, service.orderJson(query(exchange).get("id")));
        } else if ("POST".equals(method) && "/api/orders".equals(path)) {
            WebOrderConfirmation confirmation = service.placeOrder(WebFormParser.parse(exchange));
            sendJson(exchange, 201, service.confirmationJson(confirmation));
        } else {
            sendJson(exchange, 404, WebJson.error("Unknown API endpoint."));
        }
    }

    private static Map<String, String> query(HttpExchange exchange) {
        return WebFormParser.parseUrlEncoded(exchange.getRequestURI().getRawQuery());
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        sendText(exchange, status, json, "application/json; charset=utf-8");
    }

    private static void sendText(HttpExchange exchange, int status, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String contentType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (name.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }
}
