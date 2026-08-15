package com.fooddelivery.database.csv;

import com.fooddelivery.datastructures.linear.CustomDynamicArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal RFC-4180-style CSV reader for the project datasets.
 */
public class CsvReader {
    public CustomDynamicArray<CsvRecord> read(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("CSV path must not be null");
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty: " + path);
            }
            String[] headers = parseLine(headerLine, 1);
            CustomDynamicArray<CsvRecord> records = new CustomDynamicArray<>();
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] values = parseLine(line, rowNumber);
                if (values.length != headers.length) {
                    throw new IllegalArgumentException("CSV row " + rowNumber + " has " + values.length
                            + " columns; expected " + headers.length);
                }
                records.add(new CsvRecord(headers, values, rowNumber));
            }
            return records;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read CSV file: " + path, exception);
        }
    }

    private static String[] parseLine(String line, int rowNumber) {
        CustomDynamicArray<String> values = new CustomDynamicArray<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char value = line.charAt(i);
            if (value == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (value == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(value);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted CSV field at row " + rowNumber);
        }
        values.add(current.toString().trim());
        String[] result = new String[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }
}
