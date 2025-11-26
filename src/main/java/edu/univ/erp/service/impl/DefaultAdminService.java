package edu.univ.erp.service.impl;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.domain.user.Role;
import edu.univ.erp.service.AdminService;

import java.time.LocalDateTime;
import java.util.Locale;
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
        erpRepository.saveCourse(course);
        return OperationResult.success(course, "Course saved.");
    }

    @Override
    public OperationResult<Section> addSection(Section section) {
        if (section.getCapacity() <= 0) {
            return OperationResult.failure("Capacity must be greater than 0.");
        }
        erpRepository.saveSection(section);
        return OperationResult.success(section, "Section saved.");
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
                            section.getRegistrationDeadline()
                    );
                    erpRepository.saveSection(updated);
                    return OperationResult.<Void>success(null, "Instructor assigned.");
                })
                .orElseGet(() -> OperationResult.failure("Section not found."));
    }
}

