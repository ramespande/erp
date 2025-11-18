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

    OperationResult<Section> addSection(Section section);

    OperationResult<Void> assignInstructor(String sectionId, String instructorId);
}

