package com.fooddelivery.database;

import com.fooddelivery.database.repository.AlgorithmRunRepository;
import com.fooddelivery.database.repository.AuditEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExperimentAuditRepositoryTest {
    @TempDir
    Path tempDirectory;

    private AlgorithmRunRepository algorithmRunRepository;
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(
                new DatabaseConfig(tempDirectory.resolve("test.db")));
        new DatabaseInitializer(connectionFactory).initialize();
        algorithmRunRepository = new AlgorithmRunRepository(connectionFactory);
        auditEventRepository = new AuditEventRepository(connectionFactory);
    }

    @Test
    void savesAndReadsAlgorithmRunRecords() {
        AlgorithmRunRecord record = new AlgorithmRunRecord(
                "RUN001",
                "Dijkstra Shortest Path",
                150,
                1,
                657127L,
                342.11,
                LocalDateTime.of(2026, 6, 30, 12, 32));

        algorithmRunRepository.save(record);

        AlgorithmRunRecord saved = algorithmRunRepository.findById("RUN001");
        assertEquals(1, algorithmRunRepository.count());
        assertNotNull(saved);
        assertEquals("Dijkstra Shortest Path", saved.getAlgorithmName());
        assertEquals(150, saved.getInputSize());
        assertEquals(1, algorithmRunRepository.findAll().size());
    }

    @Test
    void savesAndReadsAuditEventRecords() {
        AuditEventRecord record = new AuditEventRecord(
                "AUD001",
                "ServiceRequest",
                "SR001",
                "IMPORT",
                LocalDateTime.of(2026, 6, 30, 12, 45),
                "SYSTEM",
                "Imported normalized service request data");

        auditEventRepository.save(record);

        AuditEventRecord saved = auditEventRepository.findById("AUD001");
        assertEquals(1, auditEventRepository.count());
        assertNotNull(saved);
        assertEquals("SYSTEM", saved.getPerformedBy());
        assertEquals("IMPORT", saved.getAction());
        assertEquals(1, auditEventRepository.findAll().size());
    }
}
