-- Fix stu1 password hash to "stu123"
-- Run: mysql -u root -p erp_auth < fix_stu1_password.sql
-- This will update the password hash for stu1 to match the correct bcrypt hash for "stu123"
-- Password: stu123

USE erp_auth;

-- Update stu1 password hash (this is a bcrypt hash for "stu123" with cost factor 12)
-- This hash is generated using: BCrypt.hashpw("stu123", BCrypt.gensalt(12))
-- The hash matches the one in schema_auth.sql seed data
UPDATE auth_users 
SET password_hash = '$2a$12$jdgqBZ7idDENwgCMmApOjuPI1uGpADTDARBW3YdRoNDEG6QZz6KEG',
    failed_attempts = 0,
    lockout_until = NULL,
    active = TRUE
WHERE username = 'stu1';

-- Verify the update
SELECT username, role, active, failed_attempts, lockout_until 
FROM auth_users 
WHERE username = 'stu1';

