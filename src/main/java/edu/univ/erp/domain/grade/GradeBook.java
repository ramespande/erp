package edu.univ.erp.domain.grade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class GradeBook implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String enrollmentId;
    private final List<GradeComponent> components;
    private final Double finalGrade;

    public GradeBook(String enrollmentId, List<GradeComponent> components, Double finalGrade) {
        this.enrollmentId = enrollmentId;
        this.components = new ArrayList<>(components);
        this.finalGrade = finalGrade;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public List<GradeComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public Optional<Double> getFinalGrade() {
        return Optional.ofNullable(finalGrade);
    }
}

