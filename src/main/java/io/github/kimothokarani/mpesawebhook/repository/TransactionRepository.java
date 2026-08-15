package io.github.kimothokarani.mpesawebhook.repository;

import io.github.kimothokarani.mpesawebhook.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // I think the consumer needs just a yes/no so that it can know if the
    // external_transaction_id has been applied
    boolean existsByExternalTransactionId(String externalTransactionId);

    // A method to pull the customer transaction history
    List<Transaction> findAllByAccountIdOrderByCreatedAtDesc(
            UUID accountId);

    List<Transaction> findAllByStatus(String status);

}
