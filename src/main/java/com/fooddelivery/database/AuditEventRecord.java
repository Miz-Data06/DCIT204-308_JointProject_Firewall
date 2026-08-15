package com.fooddelivery.database;

import java.time.LocalDateTime;

public final class AuditEventRecord {
    private final String auditId;
    private final String entityType;
    private final String entityId;
    private final String action;
    private final LocalDateTime occurredAt;
    private final String performedBy;
    private final String details;

    public AuditEventRecord(
            String auditId,
            String entityType,
            String entityId,
            String action,
            LocalDateTime occurredAt,
            String performedBy,
            String details) {
        this.auditId = requireNonBlank(auditId, "Audit ID");
        this.entityType = requireNonBlank(entityType, "Entity type");
        this.entityId = requireNonBlank(entityId, "Entity ID");
        this.action = requireNonBlank(action, "Action");
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred-at timestamp cannot be null.");
        }
        this.occurredAt = occurredAt;
        this.performedBy = requireNonBlank(performedBy, "Performed by");
        this.details = requireNonBlank(details, "Details");
    }

    public String getAuditId() {
        return auditId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getDetails() {
        return details;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
        return value;
    }
}
