package edu.univ.erp.data.memory;

import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.settings.MaintenanceSetting;
import edu.univ.erp.domain.student.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class InMemoryErpRepository implements ErpRepository {

    private final InMemoryDataStore store;

    public InMemoryErpRepository(InMemoryDataStore store) {
        this.store = store;
    }

    @Override
    public Optional<Student> findStudent(String userId) {
        return Optional.ofNullable(store.students().get(userId));
    }

    @Override
    public void saveStudent(Student student) {
        store.students().put(student.getUserId(), student);
        store.save();
    }

    @Override
    public Optional<Instructor> findInstructor(String userId) {
        return Optional.ofNullable(store.instructors().get(userId));
    }

    @Override
    public void saveInstructor(Instructor instructor) {
        store.instructors().put(instructor.getUserId(), instructor);
        store.save();
    }

    @Override
    public Optional<Course> findCourse(String courseId) {
        return Optional.ofNullable(store.courses().get(courseId));
    }

    @Override
    public List<Course> listCourses() {
        return new ArrayList<>(store.courses().values());
    }

    @Override
    public void saveCourse(Course course) {
        store.courses().put(course.getCourseId(), course);
        store.save();
    }

    @Override
    public void deleteCourse(String courseId) {
        store.courses().remove(courseId);
        store.save();
    }

    @Override
    public Optional<Section> findSection(String sectionId) {
        return Optional.ofNullable(store.sections().get(sectionId));
    }

    @Override
    public List<Section> listSections() {
        return new ArrayList<>(store.sections().values());
    }

    @Override
    public void saveSection(Section section) {
        store.sections().put(section.getSectionId(), section);
        store.save();
    }

    @Override
    public void deleteSection(String sectionId) {
        store.sections().remove(sectionId);
        store.save();
    }

    @Override
    public List<Enrollment> findEnrollmentsByStudent(String studentId) {
        return store.enrollments()
                .values()
                .stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId))
                .toList();
    }

    @Override
    public List<Enrollment> findEnrollmentsBySection(String sectionId) {
        return store.enrollmentsForSection(sectionId);
    }

    @Override
    public Optional<Enrollment> findEnrollment(String studentId, String sectionId) {
        return store.enrollments()
                .values()
                .stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId)
                        && enrollment.getSectionId().equals(sectionId))
                .findFirst();
    }

    @Override
    public void saveEnrollment(Enrollment enrollment) {
        store.enrollments().put(enrollment.getEnrollmentId(), enrollment);
        store.save();
    }

    @Override
    public void deleteEnrollment(String enrollmentId) {
        store.enrollments().remove(enrollmentId);
        store.save();
    }

    @Override
    public Optional<GradeBook> findGradeBook(String enrollmentId) {
        return Optional.ofNullable(store.gradeBooks().get(enrollmentId));
    }

    @Override
    public void saveGradeBook(GradeBook gradeBook) {
        store.gradeBooks().put(gradeBook.getEnrollmentId(), gradeBook);
        store.save();
    }

    @Override
    public MaintenanceSetting getMaintenanceSetting() {
        return store.maintenanceSetting();
    }

    @Override
    public void saveMaintenanceSetting(boolean maintenanceOn) {
        store.setMaintenanceSetting(maintenanceOn);
        store.save();
    }
}

