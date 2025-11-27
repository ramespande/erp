package edu.univ.erp.service.impl;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.domain.user.Role;
import edu.univ.erp.service.AdminService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class DefaultAdminService implements AdminService {

    private final AuthRepository authRepository;
    private final ErpRepository erpRepository;
    private final AccessController accessController;

    public DefaultAdminService(AuthRepository authRepository,
                               ErpRepository erpRepository,
                               AccessController accessController) {
        this.authRepository = authRepository;
        this.erpRepository = erpRepository;
        this.accessController = accessController;
    }

    @Override
    public OperationResult<String> addUser(String username, String rawPassword, String role) {
        if (!accessController.canAdminWrite()) {
            return OperationResult.failure("Admin privileges required.");
        }
        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role.trim().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException ex) {
            return OperationResult.failure("Invalid role: " + role);
        }
        if (authRepository.findByUsername(username).isPresent()) {
            return OperationResult.failure("Username already exists.");
        }
        String userId = UUID.randomUUID().toString();
        authRepository.save(new AuthRecord(
                userId,
                username,
                parsedRole,
                PasswordHasher.hash(rawPassword),
                true,
                LocalDateTime.now(),
                0,
                null));
        
        // Automatically create profile records for STUDENT and INSTRUCTOR roles,
        // matching the pattern used in seed data
        if (parsedRole == Role.STUDENT) {
            // Create default student profile - admin can update details later
            String rollNumber = username.toUpperCase() + "-ROLL";
            Student student = new Student(userId, rollNumber, "B.Tech CS", 1);
            erpRepository.saveStudent(student);
        } else if (parsedRole == Role.INSTRUCTOR) {
            // Create default instructor profile - admin can update details later
            Instructor instructor = new Instructor(userId, "Computer Science", "Assistant Professor");
            erpRepository.saveInstructor(instructor);
        }
        
        return OperationResult.success(userId, "User created with profile.");
    }

    @Override
    public OperationResult<Student> addStudentProfile(Student student) {
        erpRepository.saveStudent(student);
        return OperationResult.success(student, "Student profile saved.");
    }

    @Override
    public OperationResult<Instructor> addInstructorProfile(Instructor instructor) {
        erpRepository.saveInstructor(instructor);
        return OperationResult.success(instructor, "Instructor profile saved.");
    }

    @Override
    public OperationResult<Course> addCourse(Course course) {
        // Check if course already exists - prevent duplicate course IDs
        if (erpRepository.findCourse(course.getCourseId()).isPresent()) {
            return OperationResult.failure("Course with ID '" + course.getCourseId() + "' already exists. Cannot create duplicate course.");
        }
        erpRepository.saveCourse(course);
        return OperationResult.success(course, "Course saved.");
    }

    @Override
    public OperationResult<Void> removeCourse(String courseId) {
        Optional<Course> courseOpt = erpRepository.findCourse(courseId);
        if (courseOpt.isEmpty()) {
            return OperationResult.failure("Course not found.");
        }
        
        // Check if sections exist for this course
        List<Section> sections = erpRepository.listSections().stream()
                .filter(section -> section.getCourseId().equals(courseId))
                .toList();
        
        if (!sections.isEmpty()) {
            return OperationResult.failure("Cannot remove course: " + sections.size() + " section(s) are associated with this course. Please remove all sections first.");
        }
        
        erpRepository.deleteCourse(courseId);
        return OperationResult.success(null, "Course removed successfully.");
    }

    @Override
    public OperationResult<Section> addSection(Section section) {
        if (section.getCapacity() <= 0) {
            return OperationResult.failure("Capacity must be greater than 0.");
        }
        // Check if section already exists - prevent duplicate section IDs
        if (erpRepository.findSection(section.getSectionId()).isPresent()) {
            return OperationResult.failure("Section with ID '" + section.getSectionId() + "' already exists. Cannot create duplicate section.");
        }
        erpRepository.saveSection(section);
        return OperationResult.success(section, "Section saved.");
    }

    @Override
    public OperationResult<Void> removeSection(String sectionId) {
        Optional<Section> sectionOpt = erpRepository.findSection(sectionId);
        if (sectionOpt.isEmpty()) {
            return OperationResult.failure("Section not found.");
        }
        
        // Check if students are enrolled in this section
        List<Enrollment> enrollments = erpRepository.findEnrollmentsBySection(sectionId);
        if (!enrollments.isEmpty()) {
            return OperationResult.failure("Cannot remove section: " + enrollments.size() + " student(s) are currently enrolled.");
        }
        
        erpRepository.deleteSection(sectionId);
        return OperationResult.success(null, "Section removed successfully.");
    }

    @Override
    public OperationResult<Void> assignInstructor(String sectionId, String instructorId) {
        return erpRepository.findSection(sectionId)
                .map(section -> {
                    Section updated = new Section(
                            section.getSectionId(),
                            section.getCourseId(),
                            instructorId,
                            section.getDayOfWeek(),
                            section.getStartTime(),
                            section.getEndTime(),
                            section.getRoom(),
                            section.getCapacity(),
                            section.getSemester(),
                            section.getYear(),
                            section.getRegistrationDeadline(),
                            section.getWeightingRule(),
                            section.getComponentNames()
                    );
                    erpRepository.saveSection(updated);
                    return OperationResult.<Void>success(null, "Instructor assigned.");
                })
                .orElseGet(() -> OperationResult.failure("Section not found."));
    }

    @Override
    public OperationResult<Void> temporaryLockUser(String username, int minutes) {
        if (minutes <= 0) {
            return OperationResult.failure("Lock duration must be positive.");
        }
        return authRepository.findByUsername(username)
                .map(record -> {
                    LocalDateTime until = LocalDateTime.now().plusMinutes(minutes);
                    authRepository.save(record.withFailedAttempts(0).withLockoutUntil(until));
                    return OperationResult.<Void>success(null, "User locked for " + minutes + " minute(s).");
                })
                .orElseGet(() -> OperationResult.failure("User not found."));
    }

    @Override
    public OperationResult<Void> unlockUser(String username) {
        return authRepository.findByUsername(username)
                .map(record -> {
                    authRepository.save(record.withFailedAttempts(0).withLockoutUntil(null));
                    return OperationResult.<Void>success(null, "User unlocked.");
                })
                .orElseGet(() -> OperationResult.failure("User not found."));
    }
}

