-- ============================================
-- MECANO MICROSERVICES DATABASE SCHEMAS
-- ============================================

-- ============================================
-- AUTH SERVICE DATABASE
-- ============================================

SELECT 'CREATE DATABASE mecano_auth_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_auth_db')\gexec

\c mecano_auth_db

-- Role enum
CREATE TYPE auth_role AS ENUM ('USER', 'AUTOMOBILIST', 'MECHANIC', 'ADMIN');

-- Users credentials table
CREATE TABLE IF NOT EXISTS users_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role auth_role NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users_credentials(id),
    token VARCHAR(512) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_users_credentials_email ON users_credentials(email);

-- Insert super admin user (password: Admin123!)
-- This is the main admin account for managing the entire platform
INSERT INTO users_credentials (email, password, name, role, is_active)
SELECT 'admin@mecano.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Super Admin', 'ADMIN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM users_credentials WHERE email = 'admin@mecano.com');

-- ============================================
-- USERS SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_users_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_users_db')\gexec

\c mecano_users_db

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Automobilists table
CREATE TABLE IF NOT EXISTS automobilists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    vehicle_brand VARCHAR(100),
    vehicle_model VARCHAR(100),
    vehicle_plate VARCHAR(50),
    driving_license_document VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Mechanics table
CREATE TABLE IF NOT EXISTS mechanics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_user_id UUID UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    garage_name VARCHAR(255),
    garage_address TEXT,
    specialities TEXT,
    justification_document VARCHAR(500),
    subscription_level VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_auth_user_id ON users(auth_user_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_automobilists_auth_user_id ON automobilists(auth_user_id);
CREATE INDEX idx_automobilists_user_id ON automobilists(user_id);
CREATE INDEX idx_automobilists_email ON automobilists(email);
CREATE INDEX idx_mechanics_auth_user_id ON mechanics(auth_user_id);
CREATE INDEX idx_mechanics_user_id ON mechanics(user_id);
CREATE INDEX idx_mechanics_email ON mechanics(email);

-- ============================================
-- GEOLOCATION SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_geo_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_geo_db')\gexec

\c mecano_geo_db

-- Subscription level enum
CREATE TYPE geo_subscription_level AS ENUM ('BASIC', 'PREMIUM', 'ENTERPRISE');

-- Mechanic locations table
CREATE TABLE IF NOT EXISTS mechanic_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mechanic_id UUID UNIQUE NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    address TEXT,
    city VARCHAR(100),
    intervention_radius_km DOUBLE PRECISION NOT NULL DEFAULT 15.0,
    subscription_level geo_subscription_level NOT NULL DEFAULT 'BASIC',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_updated TIMESTAMP
);

CREATE INDEX idx_mechanic_locations_mechanic_id ON mechanic_locations(mechanic_id);
CREATE INDEX idx_mechanic_locations_location ON mechanic_locations(latitude, longitude);
CREATE INDEX idx_mechanic_locations_active ON mechanic_locations(active);

-- ============================================
-- ADMIN SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_admin_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_admin_db')\gexec

\c mecano_admin_db

-- Admin action logs table
CREATE TABLE IF NOT EXISTS admin_action_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_email VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    target_entity VARCHAR(255) NOT NULL,
    target_id UUID,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- System metrics table
CREATE TABLE IF NOT EXISTS system_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_name VARCHAR(255) NOT NULL,
    metric_value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

CREATE INDEX idx_admin_action_logs_admin_email ON admin_action_logs(admin_email);
CREATE INDEX idx_admin_action_logs_target_entity ON admin_action_logs(target_entity);
CREATE INDEX idx_system_metrics_metric_name ON system_metrics(metric_name);
CREATE INDEX idx_system_metrics_recorded_at ON system_metrics(recorded_at);

-- ============================================
-- MARKETPLACE SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_marketplace_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_marketplace_db')\gexec

\c mecano_marketplace_db

-- Part listings table
CREATE TABLE IF NOT EXISTS part_listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(255) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255),
    price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    condition VARCHAR(50) NOT NULL DEFAULT 'USED',
    negotiable BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location VARCHAR(100) NOT NULL
);

CREATE INDEX idx_part_listings_seller_id ON part_listings(seller_id);
CREATE INDEX idx_part_listings_category ON part_listings(category);
CREATE INDEX idx_part_listings_status ON part_listings(status);
CREATE INDEX idx_part_listings_created_at ON part_listings(created_at);

-- Offers table
CREATE TABLE IF NOT EXISTS offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES part_listings(id),
    buyer_id UUID NOT NULL,
    offered_price NUMERIC(10, 2) NOT NULL,
    message VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP
);

CREATE INDEX idx_offers_listing_id ON offers(listing_id);
CREATE INDEX idx_offers_buyer_id ON offers(buyer_id);
CREATE INDEX idx_offers_status ON offers(status);
CREATE INDEX idx_offers_created_at ON offers(created_at);

-- ============================================
-- REPAIR SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_repair_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_repair_db')\gexec

\c mecano_repair_db

-- Repair request status enum
CREATE TYPE repair_status AS ENUM ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Repair requests table
CREATE TABLE IF NOT EXISTS repair_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    automobilist_id UUID NOT NULL,
    assigned_mechanic_id UUID,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    description TEXT,
    status repair_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_repair_requests_automobilist_id ON repair_requests(automobilist_id);
CREATE INDEX idx_repair_requests_assigned_mechanic_id ON repair_requests(assigned_mechanic_id);
CREATE INDEX idx_repair_requests_status ON repair_requests(status);
CREATE INDEX idx_repair_requests_created_at ON repair_requests(created_at);

-- ============================================
-- NOTIFICATION SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_notif_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_notif_db')\gexec

\c mecano_notif_db

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repair_request_id UUID NOT NULL,
    automobilist_id UUID NOT NULL,
    mechanic_id UUID NOT NULL,
    rating INTEGER NOT NULL DEFAULT 5 CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_repair_request_id ON reviews(repair_request_id);
CREATE INDEX idx_reviews_automobilist_id ON reviews(automobilist_id);
CREATE INDEX idx_reviews_mechanic_id ON reviews(mechanic_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- ============================================
-- BILLING/SUBSCRIPTION SERVICE DATABASE
-- ============================================

\c postgres

SELECT 'CREATE DATABASE mecano_billing_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mecano_billing_db')\gexec

\c mecano_billing_db

-- Plan level enum
CREATE TYPE plan_level AS ENUM ('BASIC', 'PREMIUM', 'ENTERPRISE');

-- Subscription status enum
CREATE TYPE subscription_status AS ENUM ('PENDING', 'ACTIVE', 'CANCELLED', 'EXPIRED');

-- Subscription plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id SERIAL PRIMARY KEY,
    level plan_level UNIQUE NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    priority_score INTEGER NOT NULL,
    description TEXT,
    stripe_price_id VARCHAR(255)
);

-- Mechanic subscriptions table
CREATE TABLE IF NOT EXISTS mechanic_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mechanic_id UUID NOT NULL,
    plan_id INTEGER NOT NULL REFERENCES subscription_plans(id),
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP,
    status subscription_status NOT NULL DEFAULT 'PENDING',
    stripe_session_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    payment_reference VARCHAR(255)
);

CREATE INDEX idx_mechanic_subscriptions_mechanic_id ON mechanic_subscriptions(mechanic_id);
CREATE INDEX idx_mechanic_subscriptions_plan_id ON mechanic_subscriptions(plan_id);
CREATE INDEX idx_mechanic_subscriptions_status ON mechanic_subscriptions(status);
CREATE INDEX idx_subscription_plans_level ON subscription_plans(level);