package com.fooddelivery.web;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

final class WebJson {
    private WebJson() {
    }

    static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder(value.length() + 2);
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (current < 0x20) {
                        builder.append(String.format("\\u%04x", (int) current));
                    } else {
                        builder.append(current);
                    }
                }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    static String stringArray(CustomDynamicArray<String> values) {
        StringBuilder builder = new StringBuilder("[");
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                builder.append(quote(values.get(i)));
            }
        }
        builder.append(']');
        return builder.toString();
    }

    static String error(String message) {
        return "{\"ok\":false,\"error\":" + quote(message) + "}";
    }
}
