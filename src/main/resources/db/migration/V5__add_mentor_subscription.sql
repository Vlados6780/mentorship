
ALTER TABLE mentors ADD COLUMN subscription_active BOOLEAN DEFAULT FALSE;
ALTER TABLE mentors ADD COLUMN subscription_expiry_date TIMESTAMP;

CREATE TABLE subscription_payments (
            id_payment SERIAL PRIMARY KEY,
            id_mentor INTEGER NOT NULL,
            payment_amount DECIMAL(10, 2) NOT NULL,
            payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            payment_status VARCHAR(50) NOT NULL,
            transaction_id VARCHAR(255),
            FOREIGN KEY (id_mentor) REFERENCES
                mentors(id_mentor) ON DELETE CASCADE
);

CREATE INDEX idx_subscription_payments_mentor_id ON subscription_payments(id_mentor);