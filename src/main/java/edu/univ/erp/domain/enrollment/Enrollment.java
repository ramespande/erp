package edu.univ.erp.domain.enrollment;

import java.io.Serializable;
import java.util.Objects;

public final class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String enrollmentId;
    private final String studentId;
    private final String sectionId;
    private final EnrollmentStatus status;

    public Enrollment(String enrollmentId, String studentId, String sectionId, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Enrollment withStatus(EnrollmentStatus newStatus) {
        return new Enrollment(enrollmentId, studentId, sectionId, newStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Enrollment that)) {
            return false;
        }
        return Objects.equals(enrollmentId, that.enrollmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }
}

