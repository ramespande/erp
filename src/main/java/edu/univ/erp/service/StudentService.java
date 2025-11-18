package edu.univ.erp.service;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.api.types.CourseCatalogRow;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.api.types.TimetableEntry;

import java.util.List;

public interface StudentService {
    OperationResult<List<CourseCatalogRow>> viewCatalog();

    OperationResult<Void> registerSection(String studentId, String sectionId);

    OperationResult<Void> dropSection(String studentId, String sectionId);

    OperationResult<List<TimetableEntry>> viewTimetable(String studentId);

    OperationResult<List<GradeView>> viewGrades(String studentId);

    OperationResult<byte[]> downloadTranscriptCsv(String studentId);
}

