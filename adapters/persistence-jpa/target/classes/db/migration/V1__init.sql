CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS customers
(
    id          UUID        NOT NULL,
    tenant_id   VARCHAR(30) NOT NULL,
    name        TEXT        NOT NULL,
    email       TEXT        NOT NULL,
    status      TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_customer_email UNIQUE (tenant_id, email),
    CONSTRAINT pk_customer PRIMARY KEY (id, tenant_id)
);

CREATE TABLE IF NOT EXISTS plans
(
    id          UUID        NOT NULL,
    tenant_id   VARCHAR(30) NOT NULL,
    plan_type   TEXT        NOT NULL,
    name        TEXT        NOT NULL,
    price_cents BIGINT      NOT NULL CHECK (price_cents >= 0),
    period      TEXT        NOT NULL,
    trial_days  INT         NOT NULL DEFAULT 0,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_plan_type UNIQUE (tenant_id, plan_type),
    CONSTRAINT pk_plan PRIMARY KEY (id, tenant_id)
);

CREATE TABLE IF NOT EXISTS subscriptions
(
    id                UUID        NOT NULL,
    tenant_id         VARCHAR(30) NOT NULL,
    customer_id       UUID        NOT NULL,
    plan_id           UUID        NOT NULL,
    status            TEXT        NOT NULL,
    start_date        DATE,
    trial_end_date    DATE,
    next_billing_date DATE,
    end_date          DATE,
    CONSTRAINT pk_subscription PRIMARY KEY (id, tenant_id),
    CONSTRAINT fk_subscription_customer FOREIGN KEY (customer_id, tenant_id) REFERENCES customers (id, tenant_id),
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id, tenant_id) REFERENCES plans (id, tenant_id)
);