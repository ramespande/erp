# Fixes Applied

## 1. Catalog Refresh Issue
- **Problem**: Students and instructors couldn't see newly added courses/sections
- **Solution**: Added "Refresh Catalog" button to student dashboard. Students and instructors need to click refresh after admin adds new courses/sections.

## 2. Instructor Students View
- **Problem**: Instructor couldn't see all students in their sections
- **Solution**: Added new "My Students" tab in instructor dashboard that shows all enrolled students across all instructor's sections with their details.

## 3. stu1 Login Issue
- **Problem**: stu1 unable to login
- **Solution**: Run the fix script to update password hash:
  ```bash
  mysql -u root -prijul erp_auth < fix_stu1_password.sql
  ```
  
  Or manually reset the password:
  ```sql
  USE erp_auth;
  UPDATE auth_users 
  SET password_hash = '$2a$12$jdgqBZ7idDENwgCMmApOjuPI1uGpADTDARBW3YdRoNDEG6QZz6KEG',
      failed_attempts = 0, 
      lockout_until = NULL
  WHERE username = 'stu1';
  ```

## Notes
- The catalog shows **sections**, not courses. So if admin adds a course but no section, it won't appear in the catalog until a section is created for that course.
- Students and instructors should use the refresh button after admin makes changes to see updates immediately.

