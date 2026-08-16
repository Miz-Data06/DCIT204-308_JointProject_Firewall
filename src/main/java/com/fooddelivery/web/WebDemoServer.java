package com.fooddelivery.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public final class WebDemoServer {
    private WebDemoServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        WebOrderService service = new WebOrderService();
        WebDemoHandlers handlers = new WebDemoHandlers(service, Path.of("web-app"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", handlers.api());
        server.createContext("/", handlers.staticFiles());
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        System.out.println("Food Delivery web demo running at http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
    }
}
