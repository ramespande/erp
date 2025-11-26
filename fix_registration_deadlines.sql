-- Fix registration deadlines for all sections
-- Run: mysql -u root -p erp_data < fix_registration_deadlines.sql
-- This will update all section registration deadlines to 1 year in the future

USE erp_data;

-- Update all sections to have registration deadlines 1 year in the future
UPDATE sections 
SET registration_deadline = DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
WHERE registration_deadline < CURDATE() OR registration_deadline IS NULL;

-- Also update any sections that might have very near deadlines (within 7 days)
UPDATE sections 
SET registration_deadline = DATE_ADD(CURDATE(), INTERVAL 1 YEAR)
WHERE registration_deadline < DATE_ADD(CURDATE(), INTERVAL 7 DAY);

-- Verify the update
SELECT section_id, course_id, registration_deadline, 
       DATEDIFF(registration_deadline, CURDATE()) as days_until_deadline
FROM sections
ORDER BY registration_deadline;

