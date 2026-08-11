-- These two below are independent tables

CREATE TABLE accounts
(
    id                uuid PRIMARY KEY        DEFAULT gen_random_uuid(),
    phone_number      varchar(30),
    balance           numeric(19, 2) NOT NULL DEFAULT 0.00,
    account_reference varchar(50)    NOT NULL UNIQUE,
    version           bigint         NOT NULL DEFAULT 0,
    created_at        timestamptz    NOT NULL DEFAULT now(),
    updated_at        timestamptz    NOT NULL DEFAULT now()
);

CREATE TABLE raw_events
(
    id                uuid PRIMARY KEY     DEFAULT gen_random_uuid(),
    payload           jsonb       NOT NULL,
    received_at       timestamptz NOT NULL DEFAULT now(),
    processing_status varchar(20) NOT NULL CHECK ( processing_status in ('PENDING', 'PROCESSED', 'FAILED'))
);

CREATE TABLE transaction_statuses
(
    name        varchar(20) PRIMARY KEY,
    description varchar(255) NOT NULL
);

-- Seed the table above with allowed values immediately
INSERT INTO transaction_statuses (name, description)
VALUES ('RECEIVED', 'Webhook safely saved, waiting for processing'),
       ('APPLIED', 'Transaction fully processed and balance updated'),
       ('FAILED', 'Terminal failure during processing'),
       ('REVERSED', 'A correcting entry that negates a previous transaction'),
       ('UNMATCHED', 'M-pesa payment received but bill reference does not match any account');

-- The dependent table.

CREATE TABLE transactions
(
    id                      uuid PRIMARY KEY        DEFAULT gen_random_uuid(),
    external_transaction_id varchar(100)   NOT NULL UNIQUE,
    account_id              uuid REFERENCES accounts (id),
    amount                  numeric(19, 2) NOT NULL,
    transaction_type        varchar(20)    NOT NULL CHECK ( transaction_type in ('CREDIT', 'DEBIT', 'REVERSAL')),
    status                  varchar(20)    NOT NULL REFERENCES transaction_statuses (name),
    bill_ref_number         varchar(50)    NOT NULL,
    related_transaction_id  uuid           NULL REFERENCES transactions (id),
    raw_event_id            uuid           NULL REFERENCES raw_events (id), -- to show which event produced this row
    created_at              timestamptz    NOT NULL DEFAULT now(),
    completed_at            timestamptz    NULL
);

-- Indexes
-- For the UI: Fetching a user's transaction history - latest first
CREATE INDEX idx_transactions_account_time
    ON transactions (account_id, created_at DESC);

-- For the recon Job: Finding the stuck ledger transactions
CREATE INDEX idx_transactions_status_time
    ON transactions (status, created_at);

-- For Kafka watchdog: Finding stuck webhooks on the loading dock
CREATE INDEX idx_raw_events_status_time
    ON raw_events (processing_status, received_at);

