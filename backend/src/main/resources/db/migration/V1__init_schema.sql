-- Milestone 1 Initial Database Schema Migration

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'RESIDENT',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE apartments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(20) NOT NULL,
    total_units INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE households (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT NOT NULL REFERENCES apartments(id) ON DELETE CASCADE,
    flat_number VARCHAR(50) NOT NULL,
    occupancy INT NOT NULL DEFAULT 1,
    area_sqft DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    metered BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_apartment_flat UNIQUE (apartment_id, flat_number)
);

CREATE TABLE meters (
    id BIGSERIAL PRIMARY KEY,
    household_id BIGINT NOT NULL REFERENCES households(id) ON DELETE CASCADE,
    meter_number VARCHAR(100) NOT NULL UNIQUE,
    meter_type VARCHAR(50) NOT NULL DEFAULT 'DIGITAL',
    installation_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE water_usage_logs (
    id BIGSERIAL PRIMARY KEY,
    meter_id BIGINT NOT NULL REFERENCES meters(id) ON DELETE CASCADE,
    reading_date DATE NOT NULL,
    previous_reading DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    current_reading DECIMAL(12,2) NOT NULL,
    consumption DECIMAL(12,2) NOT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_meter_reading_date UNIQUE (meter_id, reading_date)
);

-- Indexes for efficient queries
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_households_apartment_id ON households(apartment_id);
CREATE INDEX idx_meters_household_id ON meters(household_id);
CREATE INDEX idx_water_usage_meter_date ON water_usage_logs(meter_id, reading_date DESC);
