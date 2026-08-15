package com.fooddelivery.database.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvReaderTest {
    @TempDir
    Path tempDirectory;

    @Test
    void readsQuotedFieldsAndEscapedQuotes() throws IOException {
        Path file = tempDirectory.resolve("sample.csv");
        Files.writeString(file, "id,name,details\n1,\"Pizza, King\",\"said \"\"hi\"\"\"\n");

        CsvRecord record = new CsvReader().read(file).get(0);

        assertEquals("1", record.get("id"));
        assertEquals("Pizza, King", record.get("name"));
        assertEquals("said \"hi\"", record.get("details"));
        assertEquals(2, record.rowNumber());
    }

    @Test
    void rejectsRowsWithWrongColumnCount() throws IOException {
        Path file = tempDirectory.resolve("bad.csv");
        Files.writeString(file, "id,name\n1\n");

        assertThrows(IllegalArgumentException.class, () -> new CsvReader().read(file));
    }
}
