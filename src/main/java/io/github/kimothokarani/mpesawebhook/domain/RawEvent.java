package io.github.kimothokarani.mpesawebhook.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "raw_events")
@EntityListeners(AuditingEntityListener.class)
public class RawEvent {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "payload", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @Column(name = "received_at", nullable = false, updatable = false)
    @CreatedDate
    private OffsetDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus;

    public enum ProcessingStatus {
        PENDING,
        PROCESSED,
        FAILED
    }

    // No-args constructor for Hibernate. Should be protected
    protected RawEvent() {
        // Hibernate will use this to construct the object and then use reflection to fill in
        // the attributes
    }

    public RawEvent(Map<String, Object> payload) {
        this.payload = payload;
        this.processingStatus = ProcessingStatus.PENDING;
    }

    public UUID getId() { return id; }
    public Map<String, Object> getPayload() { return payload; }
    public OffsetDateTime getReceivedAt() { return  receivedAt; }
    public ProcessingStatus getStatus() { return processingStatus; }

    public void markProcessed() {
        this.processingStatus = ProcessingStatus.PROCESSED;
    }

    public void markFailed() {
        this.processingStatus = ProcessingStatus.FAILED;
    }

}
