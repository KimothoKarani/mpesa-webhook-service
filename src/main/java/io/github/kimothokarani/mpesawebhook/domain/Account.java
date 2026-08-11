package io.github.kimothokarani.mpesawebhook.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal balance;

    @Column(name = "account_reference", nullable = false, unique = true, length = 50)
    private String accountReference;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Account() {
        // Empty constructor. Required by Hibernate/JPA. Another developer cannot use this directly
    }

    public Account(String accountReference, String phoneNumber) {
        this.accountReference = accountReference;
        this.phoneNumber = phoneNumber;
        this.balance = BigDecimal.ZERO.setScale(2);
    }

    public UUID getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public BigDecimal getBalance() { return balance; }
    public String getAccountReference() { return accountReference; }
    public Long getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // Have a method to mutate balance. Instead of directly using a setter
    public void applyCredit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

}
