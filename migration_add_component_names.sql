-- Migration script to add weighting_rule and component_names columns to sections table
-- Run this script on your existing erp_data database

USE erp_data;

ALTER TABLE sections
ADD COLUMN component_names VARCHAR(255) NULL
AFTER weighting_rule;

-- Optional: Set defaults for existing sections
-- UPDATE sections SET weighting_rule = '33,33,34' WHERE weighting_rule IS NULL;
-- UPDATE sections SET component_names = 'Quiz,Assignment,Final' WHERE component_names IS NULL;
