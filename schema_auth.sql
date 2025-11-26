-- ============================================================================
-- ERP AUTH DATABASE SCHEMA
-- Database: erp_auth
-- Purpose: User authentication and authorization data
-- ============================================================================

-- Create database (run separately if needed)
CREATE DATABASE IF NOT EXISTS erp_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_auth;

-- Drop existing tables (careful in production!)
DROP TABLE IF EXISTS auth_users;

-- ============================================================================
-- TABLE: auth_users
-- Stores user credentials and authentication metadata
-- ============================================================================
CREATE TABLE auth_users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    failed_attempts INT DEFAULT 0,
    lockout_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SEED DATA
-- Demo accounts for testing (passwords are bcrypt hashed)
-- All passwords: admin123, inst123, or stu123
-- ============================================================================

-- Password hashes generated with jBCrypt (cost factor 10)
-- admin123: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- inst123:  $2a$10$9Xq8l5YhZJBKPpolJvbKNeFwGfr5R3Xr9KXKMqKJZV.lj6QJcHq5G
-- stu123:   $2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGQRQObDQQ5GZo5CmVXJHoC

INSERT INTO auth_users (user_id, username, role, password_hash, active, last_login, failed_attempts, lockout_until) VALUES
('USR-ADMIN-001', 'admin1', 'ADMIN', '$2a$12$6rR0S7iOD5JB.odBsoZWP.I7QwBLM5IdaoxFCdKgu2YXAVlBA6wIe', TRUE, NULL, 0, NULL),
('USR-INST-001', 'inst1', 'INSTRUCTOR', '$2a$12$uSnJDSg3Y8zFTew6rQGO/O81nPl25iEI9WsqGybYYw/A9pn17VovO', TRUE, NULL, 0, NULL),
('USR-INST-002', 'inst2', 'INSTRUCTOR', '$2a$12$uSnJDSg3Y8zFTew6rQGO/O81nPl25iEI9WsqGybYYw/A9pn17VovO', TRUE, NULL, 0, NULL),
('USR-STU-001', 'stu1', 'STUDENT', '$2a$12$jdgqBZ7idDENwgCMmApOjuPI1uGpADTDARBW3YdRoNDEG6QZz6KEG', TRUE, NULL, 0, NULL),
('USR-STU-002', 'stu2', 'STUDENT', '$2a$12$jdgqBZ7idDENwgCMmApOjuPI1uGpADTDARBW3YdRoNDEG6QZz6KEG', TRUE, NULL, 0, NULL),
('USR-STU-003', 'stu3', 'STUDENT', '$2a$12$jdgqBZ7idDENwgCMmApOjuPI1uGpADTDARBW3YdRoNDEG6QZz6KEG', TRUE, NULL, 0, NULL);

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
-- SELECT * FROM auth_users;
-- SELECT username, role, active FROM auth_users WHERE role = 'STUDENT';