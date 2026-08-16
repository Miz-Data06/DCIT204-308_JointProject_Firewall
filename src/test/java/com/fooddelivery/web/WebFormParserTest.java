package com.fooddelivery.web;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebFormParserTest {
    @Test
    void parsesUrlEncodedFormValues() {
        Map<String, String> values = WebFormParser.parseUrlEncoded(
                "sourceLocationId=LOC001&category=Food+Delivery&capacityRequired=1.25");

        assertEquals("LOC001", values.get("sourceLocationId"));
        assertEquals("Food Delivery", values.get("category"));
        assertEquals("1.25", values.get("capacityRequired"));
    }

    @Test
    void parsesFlatJsonRequestValues() {
        Map<String, String> values = WebFormParser.parseJsonObject("""
                {"sourceLocationId":"LOC001","urgency":"High","capacityRequired":2.0}
                """);

        assertEquals("LOC001", values.get("sourceLocationId"));
        assertEquals("High", values.get("urgency"));
        assertEquals("2.0", values.get("capacityRequired"));
    }

    @Test
    void rejectsNonObjectJson() {
        assertThrows(IllegalArgumentException.class, () -> WebFormParser.parseJsonObject("[1,2,3]"));
    }
}
