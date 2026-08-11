package io.github.kimothokarani.mpesawebhook.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "external_transaction_id", length = 100, nullable = false, unique = true)
    private String externalTransactionId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "bill_ref_number", nullable = false)
    private String billRefNumber;

    @Column(name = "related_transaction_id")
    private UUID relatedTransactionId;

    @Column(name = "raw_event_id")
    private UUID rawEventId;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;


    public enum TransactionType {
        CREDIT,
        DEBIT,
        REVERSAL
    }

    public static final String RECEIVED = "RECEIVED";
    public static final String APPLIED = "APPLIED";
    public static final String FAILED = "FAILED";
    public static final String REVERSED = "REVERSED";
    public static final String UNMATCHED = "UNMATCHED";

    protected Transaction() {
        // Empty protected constructor for Hibernate
    }

    public Transaction(
            String externalTransactionId,
            UUID accountId,
            BigDecimal amount,
            TransactionType transactionType,
            String billRefNumber,
            UUID rawEventId
            ) {
        this.externalTransactionId = externalTransactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = RECEIVED;
        this.billRefNumber = billRefNumber;
        this.rawEventId = rawEventId;
    }

    // A reversal only ever exists in relation to an original transaction, so it's built
    // through this factory rather than the general constructor. This makes the coupling
    // explicit and impossible to skip: you cannot create a REVERSAL without supplying the
    // transaction it corrects, and it has no raw_event_id of its own since it's generated
    // internally rather than from a fresh M-Pesa webhook.
    public static Transaction createReversal (
            String externalTransactionId,
            UUID accountId,
            BigDecimal amount,
            String billRefNumber,
            Transaction original
    ) {
        Transaction reversal = new Transaction(
                externalTransactionId, accountId, amount,
                TransactionType.REVERSAL, billRefNumber, null
        );
        reversal.relatedTransactionId = original.getId();
        return reversal;
    }

    // Getters
    public UUID getId() { return id; }
    public String getExternalTransactionId() { return externalTransactionId; }
    public UUID getAccountId() { return  accountId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public String getStatus() { return status; }
    public String getBillRefNumber() { return billRefNumber; }
    public UUID getRelatedTransactionId() { return relatedTransactionId; }
    public UUID getRawEventId() { return rawEventId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }

    public void markApplied() {
        this.status = APPLIED;
    }
    public void markFailed() {
        this.status = FAILED;
    }
    public void markReversed() {
        this.status = REVERSED;
    }
    public void markUnmatched() {
        this.status = UNMATCHED;
    }

    public void markCompleted() {
        this.completedAt = OffsetDateTime.now();
    }


}
