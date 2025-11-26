-- Simple migration script to add lockout_until column
-- Run: mysql -u root -p erp_auth < migrate_auth_simple.sql
-- If you get an error that the column already exists, that's fine - it means it's already migrated

USE erp_auth;

ALTER TABLE auth_users 
ADD COLUMN lockout_until TIMESTAMP NULL 
AFTER failed_attempts;

