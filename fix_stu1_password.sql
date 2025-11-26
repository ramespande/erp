-- Fix stu1 password hash
-- Run: mysql -u root -p erp_auth < fix_stu1_password.sql
-- This will update the password hash for stu1 to match the correct bcrypt hash for "stu123"

USE erp_auth;

-- Update stu1 password hash (this is a bcrypt hash for "stu123" with cost factor 12)
-- If this doesn't work, you may need to regenerate the hash using the PasswordHasher class
UPDATE auth_users 
SET password_hash = '$2a$12$jdgqBZ7idDENwgCMmApOjuPI1uGpADTDARBW3YdRoNDEG6QZz6KEG'
WHERE username = 'stu1';

-- Also reset any lockout
UPDATE auth_users 
SET failed_attempts = 0, lockout_until = NULL
WHERE username = 'stu1';

