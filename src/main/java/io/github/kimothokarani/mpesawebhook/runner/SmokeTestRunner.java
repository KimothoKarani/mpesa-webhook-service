package io.github.kimothokarani.mpesawebhook.runner;

import io.github.kimothokarani.mpesawebhook.domain.Account;
import io.github.kimothokarani.mpesawebhook.domain.RawEvent;
import io.github.kimothokarani.mpesawebhook.domain.Transaction;
import io.github.kimothokarani.mpesawebhook.repository.AccountRepository;
import io.github.kimothokarani.mpesawebhook.repository.RawEventRepository;
import io.github.kimothokarani.mpesawebhook.repository.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Component
public class SmokeTestRunner implements CommandLineRunner {

    //Use constructor injection
    private final AccountRepository accountRepository;
    private final RawEventRepository rawEventRepository;
    private final TransactionRepository transactionRepository;

    public SmokeTestRunner(AccountRepository accountRepository, RawEventRepository rawEventRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.rawEventRepository = rawEventRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Smoke Test Runner starting....");

        // Start by building an account
        Account account = new Account("SMOKE-TEST-001", "254712345678");
        Account savedAccount = accountRepository.save(account);

        System.out.println("Version immediately after first save: " + savedAccount.getVersion());
        if (savedAccount.getVersion() != 0) {
            throw new IllegalStateException("Expected version 0 after first save, got " + savedAccount.getVersion());
        }

        // Reload and confirm round trip ---
        Optional<Account> reloaded = accountRepository.findByAccountReference("SMOKE-TEST-001");
        if (reloaded.isEmpty()) {
            throw new IllegalStateException("Account did not survive round trip!");
        }

        Account reloadedAccount = reloaded.get();
        System.out.println("Reloaded account id: " + reloadedAccount.getId());
        System.out.println("Reloaded balance: " + reloadedAccount.getBalance());
        System.out.println("Version after plain reload (should still be 0): " + reloadedAccount.getVersion());

        // RawEvent with a realistic C2B payload
        Map<String, Object> payload = Map.of(
                "TransactionType", "Pay Bill",
                "TransID", "SE11XXX22X",
                "TransAmount", 500.00,
                "BusinessShortCode", "254254",
                "BillRefNumber", "SMOKE-TEST-001",
                "MSISDN", "254712345678"
        );
        RawEvent rawEvent = new RawEvent(payload);
        RawEvent savedRawEvent = rawEventRepository.save(rawEvent);

        RawEvent reloadedEvent = rawEventRepository.findById(savedRawEvent.getId())
                .orElseThrow(() -> new IllegalStateException("RawEvent did not survive round trip!"));

        System.out.println("Reloaded payload: " + reloadedEvent.getPayload());
        if (!"SMOKE-TEST-001".equals(reloadedEvent.getPayload().get("BillRefNumber"))) {
            throw new IllegalStateException("jsonb round trip corrupted the payload!");
        }

        System.out.println("Smoke test passed.");

        // Let's try out the Transaction
        Transaction transaction = new Transaction(
                (String) reloadedEvent.getPayload().get("TransID"),
                reloadedAccount.getId(),
                new BigDecimal(String.valueOf(reloadedEvent.getPayload().get("TransAmount"))),
                Transaction.TransactionType.DEBIT,
                String.valueOf(reloadedEvent.getPayload().get("BillRefNumber")),
                reloadedEvent.getId()
        );
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Get the transaction back
        Transaction reloadTransaction = transactionRepository.findById(savedTransaction.getId())
                        .orElseThrow(() -> new IllegalStateException("Transaction didn't survive the round trip!"));

        System.out.println("Confirm the transaction defaults to RECEIVED");
        if (!reloadTransaction.getStatus().equals("RECEIVED")) {
            throw new IllegalStateException("Round trip corrupted the transaction.");
        }
        System.out.println("Transaction status: " + reloadTransaction.getStatus());

        System.out.println("Transaction saved and queried successfully");
    }
}
