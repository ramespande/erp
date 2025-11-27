package edu.univ.erp.service;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.student.Student;

public interface AdminService {
    OperationResult<String> addUser(String username, String rawPassword, String role);

    OperationResult<Student> addStudentProfile(Student student);

    OperationResult<Instructor> addInstructorProfile(Instructor instructor);

    OperationResult<Course> addCourse(Course course);

    OperationResult<Void> removeCourse(String courseId);

    OperationResult<Section> addSection(Section section);

    OperationResult<Void> removeSection(String sectionId);

    OperationResult<Void> assignInstructor(String sectionId, String instructorId);

    OperationResult<Void> temporaryLockUser(String username, int minutes);

    OperationResult<Void> unlockUser(String username);
}

