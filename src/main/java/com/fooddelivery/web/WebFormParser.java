package com.fooddelivery.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class WebFormParser {
    private WebFormParser() {
    }

    public static Map<String, String> parse(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            return parseJsonObject(body);
        }
        return parseUrlEncoded(body);
    }

    public static Map<String, String> parseUrlEncoded(String body) {
        Map<String, String> values = new HashMap<>();
        if (body == null || body.isBlank()) {
            return values;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            int equals = pair.indexOf('=');
            String key = equals >= 0 ? pair.substring(0, equals) : pair;
            String value = equals >= 0 ? pair.substring(equals + 1) : "";
            values.put(decode(key), decode(value));
        }
        return values;
    }

    public static Map<String, String> parseJsonObject(String body) {
        Map<String, String> values = new HashMap<>();
        if (body == null || body.isBlank()) {
            return values;
        }
        String trimmed = body.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("JSON body must be an object.");
        }
        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.isEmpty()) {
            return values;
        }
        int index = 0;
        while (index < content.length()) {
            ParseResult key = readJsonString(content, index);
            index = skipWhitespace(content, key.nextIndex());
            if (index >= content.length() || content.charAt(index) != ':') {
                throw new IllegalArgumentException("Invalid JSON object.");
            }
            index = skipWhitespace(content, index + 1);
            ParseResult value = content.charAt(index) == '"'
                    ? readJsonString(content, index)
                    : readBareJsonValue(content, index);
            values.put(key.value(), value.value());
            index = skipWhitespace(content, value.nextIndex());
            if (index < content.length()) {
                if (content.charAt(index) != ',') {
                    throw new IllegalArgumentException("Invalid JSON object.");
                }
                index = skipWhitespace(content, index + 1);
            }
        }
        return values;
    }

    private static ParseResult readJsonString(String value, int start) {
        if (start >= value.length() || value.charAt(start) != '"') {
            throw new IllegalArgumentException("Expected JSON string.");
        }
        StringBuilder builder = new StringBuilder();
        int index = start + 1;
        while (index < value.length()) {
            char current = value.charAt(index++);
            if (current == '"') {
                return new ParseResult(builder.toString(), index);
            }
            if (current == '\\') {
                if (index >= value.length()) {
                    throw new IllegalArgumentException("Invalid JSON escape.");
                }
                char escaped = value.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> builder.append(escaped);
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    default -> throw new IllegalArgumentException("Unsupported JSON escape.");
                }
            } else {
                builder.append(current);
            }
        }
        throw new IllegalArgumentException("Unclosed JSON string.");
    }

    private static ParseResult readBareJsonValue(String value, int start) {
        int index = start;
        while (index < value.length() && value.charAt(index) != ',') {
            index++;
        }
        return new ParseResult(value.substring(start, index).trim(), index);
    }

    private static int skipWhitespace(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private record ParseResult(String value, int nextIndex) {
    }
}
