package com.fooddelivery.database.csv;

/**
 * One CSV row addressed by header name.
 */
public final class CsvRecord {
    private final String[] headers;
    private final String[] values;
    private final int rowNumber;

    CsvRecord(String[] headers, String[] values, int rowNumber) {
        this.headers = headers.clone();
        this.values = values.clone();
        this.rowNumber = rowNumber;
    }

    public String get(String header) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(header)) {
                return values[i];
            }
        }
        throw new IllegalArgumentException("Missing CSV header: " + header);
    }

    public int rowNumber() {
        return rowNumber;
    }

    public int columnCount() {
        return values.length;
    }
}
