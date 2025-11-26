-- Migration script to add lockout_until column to auth_users table
-- Run this if your database doesn't have the lockout_until column yet
-- Usage: mysql -u root -p erp_auth < migrate_auth_add_lockout.sql

USE erp_auth;

-- Check if column exists and add it if it doesn't
-- Note: This will fail if column already exists, which is fine - just means it's already migrated
SET @dbname = DATABASE();
SET @tablename = 'auth_users';
SET @columnname = 'lockout_until';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1', -- Column exists, do nothing
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TIMESTAMP NULL AFTER failed_attempts')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

