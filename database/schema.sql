-- Database Schema - Phase 2 (Complete Database Foundation)

-- 1. USERS
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. INSURANCE PLANS
CREATE TABLE IF NOT EXISTS insurance_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    coverage_amount NUMERIC(12, 2) NOT NULL CHECK (coverage_amount >= 0),
    medical_coverage NUMERIC(12, 2) NOT NULL CHECK (medical_coverage >= 0),
    baggage_coverage NUMERIC(12, 2) NOT NULL CHECK (baggage_coverage >= 0),
    trip_cancellation NUMERIC(12, 2) NOT NULL CHECK (trip_cancellation >= 0),
    emergency_assistance BOOLEAN DEFAULT TRUE,
    base_premium NUMERIC(10, 2) NOT NULL CHECK (base_premium >= 0),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. TRAVEL DETAILS
CREATE TABLE IF NOT EXISTS travel_details (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    traveller_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    passport_number VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    destination VARCHAR(100) NOT NULL,
    departure_date DATE NOT NULL,
    return_date DATE NOT NULL,
    trip_type VARCHAR(50) NOT NULL,
    travel_purpose VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dates CHECK (return_date >= departure_date)
);

-- 4. POLICY APPLICATIONS
CREATE TABLE IF NOT EXISTS policy_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES insurance_plans(id) ON DELETE RESTRICT,
    travel_details_id BIGINT NOT NULL REFERENCES travel_details(id) ON DELETE CASCADE,
    application_number VARCHAR(100) UNIQUE NOT NULL,
    premium_amount NUMERIC(10, 2) NOT NULL CHECK (premium_amount >= 0),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. POLICIES
CREATE TABLE IF NOT EXISTS policies (
    id BIGSERIAL PRIMARY KEY,
    policy_number VARCHAR(100) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT UNIQUE NOT NULL REFERENCES policy_applications(id) ON DELETE RESTRICT,
    plan_id BIGINT NOT NULL REFERENCES insurance_plans(id) ON DELETE RESTRICT,
    coverage_amount NUMERIC(12, 2) NOT NULL CHECK (coverage_amount >= 0),
    premium_amount NUMERIC(10, 2) NOT NULL CHECK (premium_amount >= 0),
    destination VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_policy_dates CHECK (end_date >= start_date)
);

-- 6. PAYMENTS
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES policy_applications(id) ON DELETE RESTRICT,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. POLICY DOCUMENTS
CREATE TABLE IF NOT EXISTS policy_documents (
    id BIGSERIAL PRIMARY KEY,
    policy_id BIGINT UNIQUE NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    document_path VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance optimization
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS idx_policy_applications_num ON policy_applications(application_number);
CREATE INDEX IF NOT EXISTS idx_policy_applications_user ON policy_applications(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_policies_num ON policies(policy_number);
CREATE INDEX IF NOT EXISTS idx_policies_user ON policies(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_tx ON payments(transaction_id);

-- Sample Data Seeding for Insurance Plans
INSERT INTO insurance_plans (name, description, coverage_amount, medical_coverage, baggage_coverage, trip_cancellation, emergency_assistance, base_premium, active)
VALUES 
('Basic Travel Plan', 'Essential medical and luggage coverage for budget travelers.', 500000.00, 350000.00, 100000.00, 50000.00, TRUE, 1500.00, TRUE),
('Premium Travel Plan', 'Enhanced coverage with trip cancellation protection and high medical limits.', 1000000.00, 750000.00, 150000.00, 100000.00, TRUE, 3500.00, TRUE),
('Gold Travel Plan', 'Maximum comprehensive coverage for global travel including adventure sports protection.', 2000000.00, 1500000.00, 300000.00, 200000.00, TRUE, 5500.00, TRUE),
('Family Travel Plan', 'All-in-one comprehensive coverage specifically tailored for families traveling together.', 2500000.00, 1800000.00, 400000.00, 300000.00, TRUE, 7000.00, TRUE)
ON CONFLICT DO NOTHING;
