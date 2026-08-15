package com.fooddelivery;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {
    @Test
    void exitsCleanlyFromMenu() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> Main.run(new Scanner("8\n"),
                new PrintStream(output, true, StandardCharsets.UTF_8)));

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Goodbye."));
    }

    @Test
    void handlesInvalidInputWithoutCrashing() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> Main.run(new Scanner("nope\n8\n"),
                new PrintStream(output, true, StandardCharsets.UTF_8)));

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Invalid option"));
        assertFalse(text.contains("Exception"));
    }

    @Test
    void canShowDatasetCountsFromMenu() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> Main.run(new Scanner("2\n8\n"),
                new PrintStream(output, true, StandardCharsets.UTF_8)));

        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Locations=150, roads=300, requests=900, riders=90"));
    }
}
