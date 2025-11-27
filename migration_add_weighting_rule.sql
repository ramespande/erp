-- Migration script to add weighting_rule column to sections table
-- Run this script on your existing erp_data database

USE erp_data;

ALTER TABLE sections 
ADD COLUMN weighting_rule VARCHAR(100) NULL 
AFTER registration_deadline;

-- Optional: Set default weighting rule for existing sections
-- UPDATE sections SET weighting_rule = '33,33,34' WHERE weighting_rule IS NULL;

