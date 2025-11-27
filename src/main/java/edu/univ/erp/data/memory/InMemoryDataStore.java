package edu.univ.erp.data.memory;

import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.enrollment.EnrollmentStatus;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.grade.GradeComponent;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.settings.MaintenanceSetting;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.domain.user.Role;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InMemoryDataStore implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "erp-data.dat";
    private static final boolean ENABLE_DISK_CACHE = Boolean.parseBoolean(System.getProperty("erp.memory.disk", "false"));
    
    private static Path getDataFilePath() {
        // Try to use a data directory in the current working directory
        // This ensures the file is always in a predictable location
        Path dataDir = Paths.get(DATA_DIR);
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory, using current directory: " + e.getMessage());
            return Paths.get(DATA_FILE);
        }
        return dataDir.resolve(DATA_FILE);
    }

    private final Map<String, AuthRecord> authRecords = new HashMap<>();
    private final Map<String, Student> students = new HashMap<>();
    private final Map<String, Instructor> instructors = new HashMap<>();
    private final Map<String, Course> courses = new HashMap<>();
    private final Map<String, Section> sections = new HashMap<>();
    private final Map<String, Enrollment> enrollments = new HashMap<>();
    private final Map<String, GradeBook> gradeBooks = new HashMap<>();
    private MaintenanceSetting maintenanceSetting = new MaintenanceSetting(false);

    public static InMemoryDataStore seed() {
        InMemoryDataStore store = ENABLE_DISK_CACHE ? load() : null;
        if (store == null) {
            store = new InMemoryDataStore();
            store.bootstrap();
            store.save();
        }
        return store;
    }

    public static InMemoryDataStore load() {
        if (!ENABLE_DISK_CACHE) {
            return null;
        }
        Path dataPath = getDataFilePath();
        if (!Files.exists(dataPath)) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataPath.toFile()))) {
            return (InMemoryDataStore) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load data store: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void save() {
        if (!ENABLE_DISK_CACHE) {
            return;
        }
        Path dataPath = getDataFilePath();
        try {
            // Ensure parent directory exists
            Path parent = dataPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataPath.toFile()))) {
                oos.writeObject(this);
                oos.flush();
            }
        } catch (IOException e) {
            System.err.println("Failed to save data store: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void bootstrap() {
        var adminId = UUID.randomUUID().toString();
        var instId = UUID.randomUUID().toString();
        var stu1Id = UUID.randomUUID().toString();
        var stu2Id = UUID.randomUUID().toString();

        authRecords.put(adminId, new AuthRecord(
                adminId,
                "admin1",
                Role.ADMIN,
                PasswordHasher.hash("admin123"),
                true,
                LocalDateTime.now().minusDays(1),
                0,
                null));
        authRecords.put(instId, new AuthRecord(
                instId,
                "inst1",
                Role.INSTRUCTOR,
                PasswordHasher.hash("inst123"),
                true,
                LocalDateTime.now().minusDays(2),
                0,
                null));
        authRecords.put(stu1Id, new AuthRecord(
                stu1Id,
                "stu1",
                Role.STUDENT,
                PasswordHasher.hash("stu123"),
                true,
                LocalDateTime.now().minusDays(5),
                0,
                null));
        authRecords.put(stu2Id, new AuthRecord(
                stu2Id,
                "stu2",
                Role.STUDENT,
                PasswordHasher.hash("stu123"),
                true,
                LocalDateTime.now().minusDays(6),
                0,
                null));

        instructors.put(instId, new Instructor(instId, "Computer Science", "Assistant Professor"));

        students.put(stu1Id, new Student(stu1Id, "CS21B001", "B.Tech CS", 3));
        students.put(stu2Id, new Student(stu2Id, "CS21B002", "B.Tech CS", 3));

        Course cs101 = new Course("course-1", "CS101", "Introduction to Programming", 4);
        Course cs201 = new Course("course-2", "CS201", "Data Structures", 4);
        courses.put(cs101.getCourseId(), cs101);
        courses.put(cs201.getCourseId(), cs201);

        Section sec1 = new Section(
                "section-1",
                cs101.getCourseId(),
                instId,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(10, 30),
                "LT101",
                30,
                1,
                2024,
                LocalDate.now().plusWeeks(2));
        Section sec2 = new Section(
                "section-2",
                cs201.getCourseId(),
                instId,
                DayOfWeek.WEDNESDAY,
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                "LT202",
                25,
                1,
                2024,
                LocalDate.now().plusWeeks(2));
        sections.put(sec1.getSectionId(), sec1);
        sections.put(sec2.getSectionId(), sec2);

        Enrollment enr1 = new Enrollment("enroll-1", stu1Id, sec1.getSectionId(), EnrollmentStatus.ACTIVE);
        Enrollment enr2 = new Enrollment("enroll-2", stu2Id, sec1.getSectionId(), EnrollmentStatus.ACTIVE);
        enrollments.put(enr1.getEnrollmentId(), enr1);
        enrollments.put(enr2.getEnrollmentId(), enr2);

        List<GradeComponent> components = List.of(
                new GradeComponent("Quiz", 18, 0.2),
                new GradeComponent("Midterm", 25, 0.3),
                new GradeComponent("EndSem", 48, 0.5)
        );
        gradeBooks.put(enr1.getEnrollmentId(), new GradeBook(enr1.getEnrollmentId(), components, 86.0));
    }

    public Map<String, AuthRecord> authRecords() {
        return authRecords;
    }

    public Map<String, Student> students() {
        return students;
    }

    public Map<String, Instructor> instructors() {
        return instructors;
    }

    public Map<String, Course> courses() {
        return courses;
    }

    public Map<String, Section> sections() {
        return sections;
    }

    public Map<String, Enrollment> enrollments() {
        return enrollments;
    }

    public Map<String, GradeBook> gradeBooks() {
        return gradeBooks;
    }

    public MaintenanceSetting maintenanceSetting() {
        return maintenanceSetting;
    }

    public void setMaintenanceSetting(boolean maintenanceOn) {
        maintenanceSetting = new MaintenanceSetting(maintenanceOn);
    }

    public String nextId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    public List<Enrollment> enrollmentsForSection(String sectionId) {
        List<Enrollment> list = new ArrayList<>();
        for (Enrollment enrollment : enrollments.values()) {
            if (enrollment.getSectionId().equals(sectionId)) {
                list.add(enrollment);
            }
        }
        return list;
    }
}

