package edu.univ.erp.api.types;

import java.util.List;

public record GradeView(
        String courseCode,
        String sectionId,
        List<ComponentScore> components,
        Double finalGrade) {

    public record ComponentScore(String name, double score, double weight) {
    }
}

