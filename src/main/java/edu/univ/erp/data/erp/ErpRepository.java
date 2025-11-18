package edu.univ.erp.data.erp;

import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.settings.MaintenanceSetting;
import edu.univ.erp.domain.student.Student;

import java.util.List;
import java.util.Optional;

public interface ErpRepository {
    Optional<Student> findStudent(String userId);

    void saveStudent(Student student);

    Optional<Instructor> findInstructor(String userId);

    void saveInstructor(Instructor instructor);

    Optional<Course> findCourse(String courseId);

    List<Course> listCourses();

    void saveCourse(Course course);

    Optional<Section> findSection(String sectionId);

    List<Section> listSections();

    void saveSection(Section section);

    List<Enrollment> findEnrollmentsByStudent(String studentId);

    List<Enrollment> findEnrollmentsBySection(String sectionId);

    Optional<Enrollment> findEnrollment(String studentId, String sectionId);

    void saveEnrollment(Enrollment enrollment);

    void deleteEnrollment(String enrollmentId);

    Optional<GradeBook> findGradeBook(String enrollmentId);

    void saveGradeBook(GradeBook gradeBook);

    MaintenanceSetting getMaintenanceSetting();

    void saveMaintenanceSetting(boolean maintenanceOn);
}

