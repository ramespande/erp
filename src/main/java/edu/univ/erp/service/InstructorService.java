package edu.univ.erp.service;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.domain.grade.GradeComponent;

import java.util.List;
import java.util.Map;

public interface InstructorService {
    OperationResult<List<String>> listMySections(String instructorId);

    OperationResult<GradeView> viewGradesForSection(String instructorId, String sectionId);

    OperationResult<Void> recordScores(String instructorId,
                                       String sectionId,
                                       String enrollmentId,
                                       Map<String, Double> componentScores);

    OperationResult<Void> computeFinalGrades(String instructorId, String sectionId, Map<String, Double> weights);

    OperationResult<List<GradeComponent>> listGradeComponents(String instructorId,
                                                              String sectionId,
                                                              String enrollmentId);

    OperationResult<Void> saveGradeComponents(String instructorId,
                                              String sectionId,
                                              String enrollmentId,
                                              List<GradeComponent> components);

    OperationResult<Void> saveGradeComponentsWithFinal(String instructorId,
                                                       String sectionId,
                                                       String enrollmentId,
                                                       List<GradeComponent> components,
                                                       double finalGrade);
}

