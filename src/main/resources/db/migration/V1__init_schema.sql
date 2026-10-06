-- =========================================================
-- E-WALLET DATABASE SCHEMA
-- PostgreSQL
-- =========================================================

-- =========================================================
-- 1. EXTENSION
-- =========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- =========================================================
-- 2. ENUM TYPES
-- =========================================================

CREATE TYPE user_status AS ENUM (
    'ACTIVE',
    'LOCKED'
);

CREATE TYPE wallet_status AS ENUM (
    'ACTIVE',
    'FROZEN',
    'CLOSED'
);

CREATE TYPE transaction_type AS ENUM (
    'DEPOSIT',
    'WITHDRAW',
    'TRANSFER'
);

CREATE TYPE transaction_status AS ENUM (
    'PENDING',
    'SUCCESS',
    'FAILED'
);

CREATE TYPE statement_type AS ENUM (
    'CREDIT',
    'DEBIT'
);

CREATE TYPE idempotency_status AS ENUM (
    'PROCESSING',
    'COMPLETED',
    'FAILED'
);

CREATE TYPE outbox_status AS ENUM (
    'PENDING',
    'PROCESSED',
    'FAILED'
);


-- =========================================================
-- 3. USERS
-- =========================================================

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,

                       full_name VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),

                       status user_status NOT NULL DEFAULT 'ACTIVE',

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uq_users_email
                           UNIQUE (email)
);


-- =========================================================
-- 4. ROLES
-- =========================================================

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       name VARCHAR(50) NOT NULL,

                       CONSTRAINT uq_roles_name
                           UNIQUE (name)
);


-- =========================================================
-- 5. USER ROLES
-- =========================================================

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE CASCADE
);


-- =========================================================
-- 6. WALLETS
-- =========================================================

CREATE TABLE wallets (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         user_id UUID NOT NULL,

                         balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                         currency VARCHAR(3) NOT NULL DEFAULT 'VND',

                         status wallet_status NOT NULL DEFAULT 'ACTIVE',

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT uq_wallets_user
                             UNIQUE (user_id),

                         CONSTRAINT fk_wallets_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id),

                         CONSTRAINT chk_wallets_balance_non_negative
                             CHECK (balance >= 0),

                         CONSTRAINT chk_wallets_currency
                             CHECK (currency = 'VND')
);


-- =========================================================
-- 7. TRANSACTIONS
-- =========================================================

CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              reference VARCHAR(50) NOT NULL,

                              type transaction_type NOT NULL,

                              sender_wallet_id UUID,
                              receiver_wallet_id UUID,

                              amount NUMERIC(19,2) NOT NULL,

                              status transaction_status NOT NULL DEFAULT 'PENDING',

                              description VARCHAR(500),

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uq_transactions_reference
                                  UNIQUE (reference),

                              CONSTRAINT fk_transactions_sender_wallet
                                  FOREIGN KEY (sender_wallet_id)
                                      REFERENCES wallets(id),

                              CONSTRAINT fk_transactions_receiver_wallet
                                  FOREIGN KEY (receiver_wallet_id)
                                      REFERENCES wallets(id),

                              CONSTRAINT chk_transactions_amount
                                  CHECK (amount > 0),

                              CONSTRAINT chk_transactions_different_wallets
                                  CHECK (
                                      sender_wallet_id IS NULL
                                          OR receiver_wallet_id IS NULL
                                          OR sender_wallet_id <> receiver_wallet_id
                                      ),

                              CONSTRAINT chk_transaction_wallets
                                  CHECK (
                                      (
                                          type = 'DEPOSIT'
                                              AND sender_wallet_id IS NULL
                                              AND receiver_wallet_id IS NOT NULL
                                          )
                                          OR
                                      (
                                          type = 'WITHDRAW'
                                              AND sender_wallet_id IS NOT NULL
                                              AND receiver_wallet_id IS NULL
                                          )
                                          OR
                                      (
                                          type = 'TRANSFER'
                                              AND sender_wallet_id IS NOT NULL
                                              AND receiver_wallet_id IS NOT NULL
                                          )
                                      )
);


-- =========================================================
-- 8. WALLET STATEMENTS
-- =========================================================

CREATE TABLE wallet_statements (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                   wallet_id UUID NOT NULL,
                                   transaction_id UUID NOT NULL,

                                   entry_type statement_type NOT NULL,

                                   amount NUMERIC(19,2) NOT NULL,

                                   balance_after NUMERIC(19,2) NOT NULL,

                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_statements_wallet
                                       FOREIGN KEY (wallet_id)
                                           REFERENCES wallets(id),

                                   CONSTRAINT fk_statements_transaction
                                       FOREIGN KEY (transaction_id)
                                           REFERENCES transactions(id),

                                   CONSTRAINT chk_statements_amount
                                       CHECK (amount > 0),

                                   CONSTRAINT chk_statements_balance
                                       CHECK (balance_after >= 0)
);


-- =========================================================
-- 9. IDEMPOTENCY KEYS
-- =========================================================

CREATE TABLE idempotency_keys (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  user_id UUID NOT NULL,

                                  idempotency_key VARCHAR(100) NOT NULL,

                                  request_hash VARCHAR(64) NOT NULL,

                                  status idempotency_status NOT NULL DEFAULT 'PROCESSING',

                                  response_status INTEGER,

                                  response_body JSONB,

                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  expires_at TIMESTAMP NOT NULL,

                                  CONSTRAINT fk_idempotency_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id),

                                  CONSTRAINT uq_idempotency_user_key
                                      UNIQUE (user_id, idempotency_key)
);


-- =========================================================
-- 10. NOTIFICATIONS
-- =========================================================

CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               user_id UUID NOT NULL,

                               type VARCHAR(50) NOT NULL,

                               title VARCHAR(255) NOT NULL,

                               message TEXT NOT NULL,

                               is_read BOOLEAN NOT NULL DEFAULT FALSE,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_notifications_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);


-- =========================================================
-- 11. AUDIT LOGS
-- =========================================================

CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                            user_id UUID,

                            action VARCHAR(100) NOT NULL,

                            entity_type VARCHAR(50),

                            entity_id UUID,

                            metadata JSONB,

                            ip_address VARCHAR(45),

                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_audit_logs_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE SET NULL
);


-- =========================================================
-- 12. OUTBOX EVENTS
-- =========================================================

CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               aggregate_type VARCHAR(100) NOT NULL,

                               aggregate_id UUID NOT NULL,

                               event_type VARCHAR(100) NOT NULL,

                               payload JSONB NOT NULL,

                               status outbox_status NOT NULL DEFAULT 'PENDING',

                               retry_count INTEGER NOT NULL DEFAULT 0,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               processed_at TIMESTAMP
);


-- =========================================================
-- 13. INDEXES
-- =========================================================

-- Transactions

CREATE INDEX idx_transactions_sender
    ON transactions(sender_wallet_id);

CREATE INDEX idx_transactions_receiver
    ON transactions(receiver_wallet_id);

CREATE INDEX idx_transactions_created_at
    ON transactions(created_at DESC);

CREATE INDEX idx_transactions_type_status
    ON transactions(type, status);


-- Wallet statements

CREATE INDEX idx_wallet_statements_wallet_created
    ON wallet_statements(wallet_id, created_at DESC);


-- Idempotency

CREATE INDEX idx_idempotency_expires_at
    ON idempotency_keys(expires_at);


-- Notifications

CREATE INDEX idx_notifications_user_created
    ON notifications(user_id, created_at DESC);


-- Audit logs

CREATE INDEX idx_audit_logs_user_created
    ON audit_logs(user_id, created_at DESC);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action);


-- Outbox

CREATE INDEX idx_outbox_pending
    ON outbox_events(status, created_at);


-- =========================================================
-- 14.OTP
-- =========================================================

CREATE TYPE otp_purpose AS ENUM (
    'LOGIN',
    'REGISTER',
    'RESET_PASSWORD'
);

CREATE TABLE otp_verifications (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                   user_id UUID NOT NULL,

                                   otp_hash VARCHAR(255) NOT NULL,

                                   purpose otp_purpose NOT NULL,

                                   attempts INTEGER NOT NULL DEFAULT 0,

                                   expires_at TIMESTAMP NOT NULL,

                                   verified_at TIMESTAMP,

                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_otp_user
                                       FOREIGN KEY (user_id)
                                           REFERENCES users(id)
                                           ON DELETE CASCADE
);

CREATE INDEX idx_otp_user_purpose
    ON otp_verifications(user_id, purpose);

CREATE INDEX idx_otp_expires_at
    ON otp_verifications(expires_at);

-- =========================================================
-- 15. DEFAULT ROLES
-- =========================================================

INSERT INTO roles (name)
VALUES
    ('USER'),
    ('ADMIN');