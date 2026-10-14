-- Milestone 2 Full Platform Database Schema Migration

-- 1. Add apartment and household associations to users
ALTER TABLE users ADD COLUMN apartment_id BIGINT NULL REFERENCES apartments(id) ON DELETE SET NULL;
ALTER TABLE users ADD COLUMN household_id BIGINT NULL REFERENCES households(id) ON DELETE SET NULL;

CREATE INDEX idx_users_apartment ON users(apartment_id);
CREATE INDEX idx_users_household ON users(household_id);

-- 2. Tariff Plans & Tiers
CREATE TABLE tariff_plans (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    effective_start_date DATE NOT NULL,
    effective_end_date DATE NULL,
    base_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tariff_plans_apartment ON tariff_plans(apartment_id);

CREATE TABLE tariff_tiers (
    id BIGSERIAL PRIMARY KEY,
    tariff_plan_id BIGINT NOT NULL REFERENCES tariff_plans(id) ON DELETE CASCADE,
    tier_number INT NOT NULL,
    min_kl DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_kl DECIMAL(10,2) NULL,
    rate_per_kl DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tariff_tiers_plan ON tariff_tiers(tariff_plan_id);

-- 3. Billing Cycles
CREATE TABLE billing_cycles (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    total_water_procured_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_consumption_kl DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tariff_snapshot_json TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_apartment_billing_period UNIQUE (apartment_id, period_start, period_end)
);

CREATE INDEX idx_billing_cycles_apartment ON billing_cycles(apartment_id);

-- 4. Bulk Water Purchases
CREATE TABLE bulk_water_purchases (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    billing_cycle_id BIGINT NULL REFERENCES billing_cycles(id) ON DELETE SET NULL,
    purchase_date DATE NOT NULL,
    supplier VARCHAR(150) NOT NULL,
    purchase_type VARCHAR(50) NOT NULL DEFAULT 'TANKER',
    volume_kl DECIMAL(12,2) NOT NULL,
    total_cost DECIMAL(12,2) NOT NULL,
    unit_cost DECIMAL(10,2) NOT NULL,
    invoice_number VARCHAR(100) NULL,
    notes TEXT NULL,
    created_by_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bulk_purchases_apartment ON bulk_water_purchases(apartment_id);
CREATE INDEX idx_bulk_purchases_cycle ON bulk_water_purchases(billing_cycle_id);

-- 5. Household Bills
CREATE TABLE household_bills (
    id BIGSERIAL PRIMARY KEY,
    billing_cycle_id BIGINT NOT NULL REFERENCES billing_cycles(id) ON DELETE CASCADE,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    metered_consumption_kl DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    progressive_tariff_charge DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    shared_cost_allocation DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    shared_area_allocation DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    adjustments DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_payable DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cycle_household UNIQUE (billing_cycle_id, household_id)
);

CREATE INDEX idx_bills_cycle ON household_bills(billing_cycle_id);
CREATE INDEX idx_bills_household ON household_bills(household_id);

-- 6. Bill Items
CREATE TABLE bill_items (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL REFERENCES household_bills(id) ON DELETE CASCADE,
    item_type VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL DEFAULT 1.00,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_bill_items_bill ON bill_items(bill_id);

-- 7. Shared Cost Allocations
CREATE TABLE shared_cost_allocations (
    id BIGSERIAL PRIMARY KEY,
    billing_cycle_id BIGINT NOT NULL REFERENCES billing_cycles(id) ON DELETE CASCADE,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    allocation_basis VARCHAR(50) NOT NULL,
    allocation_percentage DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
    allocated_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    calculation_inputs_json TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shared_alloc_cycle ON shared_cost_allocations(billing_cycle_id);

-- 8. Anomaly Events
CREATE TABLE anomaly_events (
    id BIGSERIAL PRIMARY KEY,
    meter_id BIGINT NULL REFERENCES meters(id) ON DELETE CASCADE,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    reading_date DATE NOT NULL,
    observed_value DECIMAL(12,2) NOT NULL,
    baseline_mean DECIMAL(12,2) NULL,
    baseline_stddev DECIMAL(12,2) NULL,
    threshold_value DECIMAL(12,2) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_anomalies_household ON anomaly_events(household_id);

-- 9. Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    apartment_id BIGINT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    household_id BIGINT NULL REFERENCES households(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'SENT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_id);
