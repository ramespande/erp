
CREATE DATABASE IF NOT EXISTS erp_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_auth;

DROP TABLE IF EXISTS auth_users;

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

INSERT INTO auth_users (user_id, username, role, password_hash, active, last_login, failed_attempts, lockout_until) VALUES
('USR-ADMIN-001', 'ranjan', 'ADMIN', '$2a$12$1eN0XN1Iwvx7/clESWbb1.2L/SzjdugUR0A4BS9D9gtyYYZur/vZ.', TRUE, NULL, 0, NULL),
('USR-INST-001', 'sambuddho', 'INSTRUCTOR', '$2a$12$tCYQ/3wc30MD6oiwIYjDf.D6bOkOiwGRk5zxm3jF.yjrJO/naubOO', TRUE, NULL, 0, NULL),
('USR-STU-001', 'rijul', 'STUDENT', '$2a$12$1UwpU/Pt0IgBvuxgCE2R2eixdAZo/1xfhOP/gJDkMBIRgglkcgJpO', TRUE, NULL, 0, NULL),
('USR-STU-002', 'nakul', 'STUDENT', '$2a$12$42MBZ/eg9k3ZCh2JBTVaduyhVH/I2aRzKLj8zD0upRYiZxSMBcodi', TRUE, NULL, 0, NULL);


CREATE DATABASE IF NOT EXISTS erp_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_data;

DROP TABLE IF EXISTS grade_components;
DROP TABLE IF EXISTS grade_books;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS settings;

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

CREATE TABLE instructors (
    user_id VARCHAR(50) PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    title VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE grade_books (
    enrollment_id VARCHAR(50) PRIMARY KEY,
    final_grade DOUBLE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,

    CONSTRAINT chk_final_grade CHECK (final_grade IS NULL OR (final_grade >= 0 AND final_grade <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE settings (
    key_name VARCHAR(100) PRIMARY KEY,
    value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO students (user_id, roll_no, program, academic_year) VALUES
('USR-STU-001', '2021CS001', 'Computer Science', 3),
('USR-STU-002', '2021CS002', 'Computer Science', 3);

INSERT INTO instructors (user_id, department, title) VALUES
('USR-INST-001', 'Computer Science', 'Associate Professor');

INSERT INTO courses (course_id, code, title, credits) VALUES
('CRS-001', 'CS301', 'Database Systems', 4),
('CRS-002', 'CS302', 'Operating Systems', 4),
('CRS-003', 'HIST101', 'Modern History', 3);

INSERT INTO sections (section_id, course_id, instructor_id, day_of_week, start_time, end_time, room, capacity, semester, academic_year, registration_deadline, weighting_rule, component_names) VALUES
('SEC-DB-001', 'CRS-001', 'USR-INST-001', 'MONDAY', '09:00:00', '10:30:00', 'LH-101', 1, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '30,30,40', 'Quiz,Midterm,Final'),
('SEC-OS-001', 'CRS-002', 'USR-INST-001', 'WEDNESDAY', '14:00:00', '15:30:00', 'LH-201', 40, 1, 2024, DATE_ADD(CURDATE(), INTERVAL 1 YEAR), '20,30,50', 'Lab,Midterm,Final'),
('SEC-HI-001', 'CRS-003', 'USR-INST-001', 'FRIDAY', '11:00:00', '12:30:00', 'LH-202', 30, 1, 2024, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '25,35,40', 'Essay,Midterm,Final');

INSERT INTO enrollments (enrollment_id, student_id, section_id, status) VALUES
('ENR-001', 'USR-STU-001', 'SEC-DB-001', 'ACTIVE'),
('ENR-002', 'USR-STU-002', 'SEC-OS-001', 'ACTIVE'),
('ENR-003', 'USR-STU-001', 'SEC-HI-001', 'ACTIVE');

INSERT INTO grade_books (enrollment_id, final_grade) VALUES
('ENR-001', NULL),
('ENR-002', NULL),
('ENR-003', NULL);

INSERT INTO grade_components (enrollment_id, name, score, weight) VALUES
('ENR-001', 'Quiz', 85.0, 0.30),
('ENR-001', 'Midterm', 78.0, 0.30),
('ENR-001', 'Final', 82.0, 0.40),
('ENR-003', 'Essay', 90.0, 0.25),
('ENR-003', 'Midterm', 88.0, 0.35),
('ENR-003', 'Final', 84.0, 0.40);

INSERT INTO settings (key_name, value) VALUES
('maintenance_on', 'false');