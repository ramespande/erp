package edu.univ.erp.data.jdbc;

import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.enrollment.EnrollmentStatus;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.grade.GradeComponent;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.settings.MaintenanceSetting;
import edu.univ.erp.domain.student.Student;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcErpRepository implements ErpRepository {

    private final DataSource dataSource;

    public JdbcErpRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Student> findStudent(String userId) {
        String sql = "SELECT user_id, roll_no, program, academic_year FROM students WHERE user_id = ?";
        return querySingle(sql, ps -> ps.setString(1, userId), this::mapStudent);
    }

    @Override
    public void saveStudent(Student student) {
        String sql = """
                INSERT INTO students (user_id, roll_no, program, academic_year)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    roll_no = VALUES(roll_no),
                    program = VALUES(program),
                    academic_year = VALUES(academic_year)
                """;
        executeUpdate(sql, ps -> {
            ps.setString(1, student.getUserId());
            ps.setString(2, student.getRollNumber());
            ps.setString(3, student.getProgram());
            ps.setInt(4, student.getYear());
        });
    }

    @Override
    public Optional<Instructor> findInstructor(String userId) {
        String sql = "SELECT user_id, department, title FROM instructors WHERE user_id = ?";
        return querySingle(sql, ps -> ps.setString(1, userId), this::mapInstructor);
    }

    @Override
    public void saveInstructor(Instructor instructor) {
        String sql = """
                INSERT INTO instructors (user_id, department, title)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    department = VALUES(department),
                    title = VALUES(title)
                """;
        executeUpdate(sql, ps -> {
            ps.setString(1, instructor.getUserId());
            ps.setString(2, instructor.getDepartment());
            ps.setString(3, instructor.getTitle());
        });
    }

    @Override
    public Optional<Course> findCourse(String courseId) {
        String sql = "SELECT course_id, code, title, credits FROM courses WHERE course_id = ?";
        return querySingle(sql, ps -> ps.setString(1, courseId), this::mapCourse);
    }

    @Override
    public List<Course> listCourses() {
        String sql = "SELECT course_id, code, title, credits FROM courses";
        return queryList(sql, ps -> {
        }, this::mapCourse);
    }

    @Override
    public void saveCourse(Course course) {
        String sql = """
                INSERT INTO courses (course_id, code, title, credits)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    code = VALUES(code),
                    title = VALUES(title),
                    credits = VALUES(credits)
                """;
        executeUpdate(sql, ps -> {
            ps.setString(1, course.getCourseId());
            ps.setString(2, course.getCode());
            ps.setString(3, course.getTitle());
            ps.setInt(4, course.getCredits());
        });
    }

    @Override
    public void deleteCourse(String courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        executeUpdate(sql, ps -> {
            ps.setString(1, courseId);
        });
    }

    @Override
    public Optional<Section> findSection(String sectionId) {
        String sql = """
                SELECT section_id, course_id, instructor_id, day_of_week, start_time, end_time,
                       room, capacity, semester, academic_year, registration_deadline, weighting_rule, component_names
                FROM sections WHERE section_id = ?
                """;
        return querySingle(sql, ps -> ps.setString(1, sectionId), this::mapSection);
    }

    @Override
    public List<Section> listSections() {
        String sql = """
                SELECT section_id, course_id, instructor_id, day_of_week, start_time, end_time,
                       room, capacity, semester, academic_year, registration_deadline, weighting_rule, component_names
                FROM sections
                """;
        return queryList(sql, ps -> {
        }, this::mapSection);
    }

    @Override
    public void saveSection(Section section) {
        String sql = """
                INSERT INTO sections (section_id, course_id, instructor_id, day_of_week, start_time, end_time,
                                      room, capacity, semester, academic_year, registration_deadline, weighting_rule, component_names)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    course_id = VALUES(course_id),
                    instructor_id = VALUES(instructor_id),
                    day_of_week = VALUES(day_of_week),
                    start_time = VALUES(start_time),
                    end_time = VALUES(end_time),
                    room = VALUES(room),
                    capacity = VALUES(capacity),
                    semester = VALUES(semester),
                    academic_year = VALUES(academic_year),
                    registration_deadline = VALUES(registration_deadline),
                    weighting_rule = VALUES(weighting_rule),
                    component_names = VALUES(component_names)
                """;
        executeUpdate(sql, ps -> {
            ps.setString(1, section.getSectionId());
            ps.setString(2, section.getCourseId());
            ps.setString(3, section.getInstructorId());
            ps.setString(4, section.getDayOfWeek().name());
            ps.setTime(5, Time.valueOf(section.getStartTime()));
            ps.setTime(6, Time.valueOf(section.getEndTime()));
            ps.setString(7, section.getRoom());
            ps.setInt(8, section.getCapacity());
            ps.setInt(9, section.getSemester());
            ps.setInt(10, section.getYear());
            ps.setDate(11, Date.valueOf(section.getRegistrationDeadline()));
            ps.setString(12, section.getWeightingRule());
            ps.setString(13, section.getComponentNames());
        });
    }

    @Override
    public void deleteSection(String sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        executeUpdate(sql, ps -> {
            ps.setString(1, sectionId);
        });
    }

    @Override
    public List<Enrollment> findEnrollmentsByStudent(String studentId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE student_id = ?";
        return queryList(sql, ps -> ps.setString(1, studentId), this::mapEnrollment);
    }

    @Override
    public List<Enrollment> findEnrollmentsBySection(String sectionId) {
        String sql = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE section_id = ?";
        return queryList(sql, ps -> ps.setString(1, sectionId), this::mapEnrollment);
    }

    @Override
    public Optional<Enrollment> findEnrollment(String studentId, String sectionId) {
        String sql = """
                SELECT enrollment_id, student_id, section_id, status
                FROM enrollments
                WHERE student_id = ? AND section_id = ?
                """;
        return querySingle(sql, ps -> {
            ps.setString(1, studentId);
            ps.setString(2, sectionId);
        }, this::mapEnrollment);
    }

    @Override
    public void saveEnrollment(Enrollment enrollment) {
        String sql = """
                INSERT INTO enrollments (enrollment_id, student_id, section_id, status)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    student_id = VALUES(student_id),
                    section_id = VALUES(section_id),
                    status = VALUES(status)
                """;
        executeUpdate(sql, ps -> {
            ps.setString(1, enrollment.getEnrollmentId());
            ps.setString(2, enrollment.getStudentId());
            ps.setString(3, enrollment.getSectionId());
            ps.setString(4, enrollment.getStatus().name());
        });
    }

    @Override
    public void deleteEnrollment(String enrollmentId) {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        executeUpdate(sql, ps -> ps.setString(1, enrollmentId));
    }

    @Override
    public Optional<GradeBook> findGradeBook(String enrollmentId) {
        String sql = "SELECT final_grade FROM grade_books WHERE enrollment_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Double finalGrade = rs.getObject("final_grade") == null ? null : rs.getDouble("final_grade");
                List<GradeComponent> components = listComponents(connection, enrollmentId);
                return Optional.of(new GradeBook(enrollmentId, components, finalGrade));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load grade book", e);
        }
    }

    @Override
    public void saveGradeBook(GradeBook gradeBook) {
        String upsertBook = """
                INSERT INTO grade_books (enrollment_id, final_grade)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE final_grade = VALUES(final_grade)
                """;
        executeUpdate(upsertBook, ps -> {
            ps.setString(1, gradeBook.getEnrollmentId());
            ps.setObject(2, gradeBook.getFinalGrade().orElse(null));
        });

        String deleteComponents = "DELETE FROM grade_components WHERE enrollment_id = ?";
        executeUpdate(deleteComponents, ps -> ps.setString(1, gradeBook.getEnrollmentId()));

        String insertComponent = """
                INSERT INTO grade_components (enrollment_id, name, score, weight)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(insertComponent)) {
            for (GradeComponent component : gradeBook.getComponents()) {
                ps.setString(1, gradeBook.getEnrollmentId());
                ps.setString(2, component.getName());
                ps.setDouble(3, component.getScore());
                ps.setDouble(4, component.getWeight());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to persist grade components", e);
        }
    }

    @Override
    public MaintenanceSetting getMaintenanceSetting() {
        String sql = "SELECT value FROM settings WHERE key_name = 'maintenance_on'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new MaintenanceSetting(Boolean.parseBoolean(rs.getString("value")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load maintenance setting", e);
        }
        return new MaintenanceSetting(false);
    }

    @Override
    public void saveMaintenanceSetting(boolean maintenanceOn) {
        String sql = """
                INSERT INTO settings (key_name, value)
                VALUES ('maintenance_on', ?)
                ON DUPLICATE KEY UPDATE value = VALUES(value)
                """;
        executeUpdate(sql, ps -> ps.setString(1, Boolean.toString(maintenanceOn)));
    }

    private List<GradeComponent> listComponents(Connection connection, String enrollmentId) throws SQLException {
        String sql = """
                SELECT name, score, weight
                FROM grade_components
                WHERE enrollment_id = ?
                """;
        List<GradeComponent> components = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    components.add(new GradeComponent(
                            rs.getString("name"),
                            rs.getDouble("score"),
                            rs.getDouble("weight")
                    ));
                }
            }
        }
        return components;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("user_id"),
                rs.getString("roll_no"),
                rs.getString("program"),
                rs.getInt("academic_year")
        );
    }

    private Instructor mapInstructor(ResultSet rs) throws SQLException {
        return new Instructor(
                rs.getString("user_id"),
                rs.getString("department"),
                rs.getString("title")
        );
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        return new Course(
                rs.getString("course_id"),
                rs.getString("code"),
                rs.getString("title"),
                rs.getInt("credits")
        );
    }

    private Section mapSection(ResultSet rs) throws SQLException {
        return new Section(
                rs.getString("section_id"),
                rs.getString("course_id"),
                rs.getString("instructor_id"),
                DayOfWeek.valueOf(rs.getString("day_of_week")),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                rs.getString("room"),
                rs.getInt("capacity"),
                rs.getInt("semester"),
                rs.getInt("academic_year"),
                rs.getDate("registration_deadline").toLocalDate(),
                rs.getString("weighting_rule"),
                rs.getString("component_names")
        );
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        return new Enrollment(
                rs.getString("enrollment_id"),
                rs.getString("student_id"),
                rs.getString("section_id"),
                EnrollmentStatus.valueOf(rs.getString("status"))
        );
    }

    private <T> Optional<T> querySingle(String sql,
                                        SqlConsumer<PreparedStatement> binder,
                                        SqlFunction<ResultSet, T> mapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.apply(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute query", e);
        }
    }

    private <T> List<T> queryList(String sql,
                                  SqlConsumer<PreparedStatement> binder,
                                  SqlFunction<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute query", e);
        }
        return results;
    }

    private void executeUpdate(String sql, SqlConsumer<PreparedStatement> binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            binder.accept(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute update", e);
        }
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}

