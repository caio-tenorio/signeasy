CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS customers
(
    id
    UUID
    PRIMARY
    KEY,
    name
    TEXT
    NOT
    NULL,
    email
    TEXT
    NOT
    NULL,
    status
    TEXT
    NOT
    NULL,
    created_at
    TIMESTAMPTZ
    NOT
    NULL,
    updated_at
    TIMESTAMPTZ
    NOT
    NULL,
    CONSTRAINT
    uk_customer_email
    UNIQUE
(
    email
)
    );

CREATE TABLE IF NOT EXISTS plans
(
    id
    UUID
    PRIMARY
    KEY,
    code
    TEXT
    NOT
    NULL,
    name
    TEXT
    NOT
    NULL,
    price_cents
    BIGINT
    NOT
    NULL
    CHECK
(
    price_cents
    >=
    0
),
    period TEXT NOT NULL,
    trial_days INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_plan_code UNIQUE
(
    code
)
    );

CREATE TABLE IF NOT EXISTS subscriptions
(
    id
    UUID
    PRIMARY
    KEY,
    customer_id
    UUID
    NOT
    NULL
    REFERENCES
    customers
(
    id
),
    plan_id UUID NOT NULL REFERENCES plans
(
    id
),
    status TEXT NOT NULL,
    start_date DATE,
    trial_end_date DATE,
    next_billing_date DATE,
    end_date DATE
    );