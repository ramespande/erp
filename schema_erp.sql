-- ============================================================================
-- ERP DATA DATABASE SCHEMA
-- Database: erp_data
-- Purpose: Core ERP data (students, courses, enrollments, grades, settings)
-- ============================================================================

-- Create database (run separately if needed)
CREATE DATABASE IF NOT EXISTS erp_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_data;

-- Drop existing tables in correct order (respects foreign keys)
DROP TABLE IF EXISTS grade_components;
DROP TABLE IF EXISTS grade_books;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS settings;

-- ============================================================================
-- TABLE: students
-- Student profile data linked to auth_users
-- ============================================================================
CREATE TABLE students (
    user_id VARCHAR(50) PRIMARY KEY,
    roll_no VARCHAR(20) NOT NULL UNIQUE,
    program VARCHAR(100) NOT NULL,
    academic_year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roll_no (roll_no),
    INDEX idx_program (program),
    INDEX idx_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: instructors
-- Instructor profile data linked to auth_users
-- ============================================================================
CREATE TABLE instructors (
    user_id VARCHAR(50) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    title VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: courses
-- Course catalog
-- ============================================================================
CREATE TABLE courses (
    course_id VARCHAR(50) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    credits INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_code (code),
    CONSTRAINT chk_credits CHECK (credits > 0 AND credits <= 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: sections
-- Course sections with schedule, instructor, and capacity
-- ============================================================================
CREATE TABLE sections (
    section_id VARCHAR(50) PRIMARY KEY,
    course_id VARCHAR(50) NOT NULL,
    instructor_id VARCHAR(50) NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    semester INT NOT NULL,
    academic_year INT NOT NULL,
    registration_deadline DATE NOT NULL,
    weighting_rule VARCHAR(100) NULL,
    component_names VARCHAR(200) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id) ON DELETE RESTRICT,
    
    INDEX idx_course (course_id),
    INDEX idx_instructor (instructor_id),
    INDEX idx_semester_year (semester, academic_year),
    INDEX idx_deadline (registration_deadline),
    
    CONSTRAINT chk_capacity CHECK (capacity > 0),
    CONSTRAINT chk_semester CHECK (semester IN (1, 2)),
    CONSTRAINT chk_time CHECK (start_time < end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: enrollments
-- Student course registrations
-- ============================================================================
CREATE TABLE enrollments (
    enrollment_id VARCHAR(50) PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    section_id VARCHAR(50) NOT NULL,
    status ENUM('ACTIVE', 'DROPPED') NOT NULL DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_enrollment (student_id, section_id),
    INDEX idx_student (student_id),
    INDEX idx_section (section_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: grade_books
-- Final grades for enrollments
-- ============================================================================
CREATE TABLE grade_books (
    enrollment_id VARCHAR(50) PRIMARY KEY,
    final_grade DOUBLE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    
    CONSTRAINT chk_final_grade CHECK (final_grade IS NULL OR (final_grade >= 0 AND final_grade <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: grade_components
-- Individual grade components (assignments, exams, etc.)
-- ============================================================================
CREATE TABLE grade_components (
    component_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    score DOUBLE NOT NULL,
    weight DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    
    INDEX idx_enrollment (enrollment_id),
    
    CONSTRAINT chk_score CHECK (score >= 0 AND score <= 100),
    CONSTRAINT chk_weight CHECK (weight >= 0 AND weight <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLE: settings
-- Application settings (maintenance mode, etc.)
-- ============================================================================
CREATE TABLE settings (
    key_name VARCHAR(100) PRIMARY KEY,
    value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SEED DATA
-- Demo data for testing
-- ============================================================================

-- Students
INSERT INTO students (user_id, roll_no, program, academic_year) VALUES
('USR-STU-001', '2021CS001', 'Computer Science', 3),
('USR-STU-002', '2021CS002', 'Computer Science', 3),
('USR-STU-003', '2022EE001', 'Electrical Engineering', 2);

-- Instructors
INSERT INTO instructors (user_id, department, title) VALUES
('USR-INST-001', 'Computer Science', 'Associate Professor'),
('USR-INST-002', 'Mathematics', 'Professor');

-- Courses
INSERT INTO courses (course_id, code, title, credits) VALUES
('CRS-001', 'CS301', 'Database Systems', 4),
('CRS-002', 'CS302', 'Operating Systems', 4),
('CRS-003', 'CS303', 'Computer Networks', 3),
('CRS-004', 'MATH201', 'Linear Algebra', 3),
('CRS-005', 'CS401', 'Machine Learning', 4);

-- Sections (Current semester: Semester 1, Year 2024)
-- Registration deadlines set to 1 year in the future to allow testing
INSERT INTO sections (section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, academic_year, registration_deadline) VALUES
('SEC-001', 'CRS-001', 'USR-INST-001', 'MONDAY', '09:00:00', '10:30:00', 'LH-101', 60, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR)),
('SEC-002', 'CRS-001', 'USR-INST-001', 'WEDNESDAY', '14:00:00', '15:30:00', 'LH-102', 60, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR)),
('SEC-003', 'CRS-002', 'USR-INST-001', 'TUESDAY', '11:00:00', '12:30:00', 'LH-201', 50, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR)),
('SEC-004', 'CRS-003', 'USR-INST-001', 'THURSDAY', '09:00:00', '10:30:00', 'LAB-301', 40, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR)),
('SEC-005', 'CRS-004', 'USR-INST-002', 'FRIDAY', '10:00:00', '11:30:00', 'LH-103', 70, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR)),
('SEC-006', 'CRS-005', 'USR-INST-001', 'MONDAY', '14:00:00', '16:00:00', 'LH-301', 45, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR));

-- Enrollments
INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
('ENR-001', 'USR-STU-001', 'SEC-001', 'ACTIVE'),
('ENR-002', 'USR-STU-001', 'SEC-003', 'ACTIVE'),
('ENR-003', 'USR-STU-001', 'SEC-004', 'ACTIVE'),
('ENR-004', 'USR-STU-002', 'SEC-001', 'ACTIVE'),
('ENR-005', 'USR-STU-002', 'SEC-005', 'ACTIVE'),
('ENR-006', 'USR-STU-003', 'SEC-004', 'ACTIVE'),
('ENR-007', 'USR-STU-003', 'SEC-005', 'ACTIVE');

-- Grade Books (some with final grades, some without)
INSERT INTO grade_books (enrollment_id, final_grade) VALUES
('ENR-001', NULL),
('ENR-002', NULL),
('ENR-003', NULL),
('ENR-004', NULL),
('ENR-005', NULL),
('ENR-006', NULL),
('ENR-007', NULL);

-- Grade Components (sample grades for ENR-001)
INSERT INTO grade_components (enrollment_id, name, score, weight) VALUES
('ENR-001', 'Assignment 1', 85.0, 0.15),
('ENR-001', 'Assignment 2', 90.0, 0.15),
('ENR-001', 'Midterm Exam', 78.0, 0.30),
('ENR-001', 'Final Exam', 82.0, 0.40),
('ENR-004', 'Assignment 1', 92.0, 0.20),
('ENR-004', 'Midterm Exam', 88.0, 0.30),
('ENR-004', 'Final Exam', 91.0, 0.50);

-- Settings
INSERT INTO settings (key_name, value) VALUES
('maintenance_on', 'false');

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
SELECT * FROM students;
SELECT * FROM instructors;
SELECT * FROM courses;
SELECT * FROM sections;
SELECT * FROM enrollments;
SELECT * FROM grade_books;
SELECT * FROM grade_components;
SELECT * FROM settings;

-- Get student enrollments with course details
-- SELECT 
--     s.roll_no,
--     c.code,
--     c.title,
--     sec.day_of_week,
--     sec.start_time,
--     e.status
-- FROM enrollments e
-- JOIN students s ON e.student_id = s.user_id
-- JOIN sections sec ON e.section_id = sec.section_id
-- JOIN courses c ON sec.course_id = c.course_id
-- WHERE s.user_id = 'USR-STU-001';