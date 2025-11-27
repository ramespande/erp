package edu.univ.erp.service.impl;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.grade.GradeComponent;
import edu.univ.erp.service.InstructorService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DefaultInstructorService implements InstructorService {

    private final ErpRepository erpRepository;
    private final AccessController accessController;

    public DefaultInstructorService(ErpRepository erpRepository, AccessController accessController) {
        this.erpRepository = erpRepository;
        this.accessController = accessController;
    }

    @Override
    public OperationResult<List<String>> listMySections(String instructorId) {
        List<String> sectionIds = erpRepository.listSections()
                .stream()
                .filter(section -> section.getInstructorId().equals(instructorId))
                .map(Section::getSectionId)
                .toList();
        return OperationResult.success(sectionIds);
    }

    @Override
    public OperationResult<GradeView> viewGradesForSection(String instructorId, String sectionId) {
        Optional<Section> section = ensureOwnership(instructorId, sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Not your section.");
        }

        List<GradeView.ComponentScore> components = new ArrayList<>();
        Double finalGrade = null;
        var enrollments = erpRepository.findEnrollmentsBySection(sectionId);
        if (enrollments.isEmpty()) {
            return OperationResult.failure("No enrollments found.");
        }

        Enrollment first = enrollments.get(0);
        Optional<GradeBook> gradeBook = erpRepository.findGradeBook(first.getEnrollmentId());
        if (gradeBook.isPresent()) {
            components = gradeBook.get()
                    .getComponents()
                    .stream()
                    .map(component -> new GradeView.ComponentScore(component.getName(), component.getScore(), component.getWeight()))
                    .toList();
            finalGrade = gradeBook.get().getFinalGrade().orElse(null);
        }

        String courseCode = erpRepository.findCourse(section.get().getCourseId()).map(course -> course.getCode()).orElse("Unknown");
        return OperationResult.success(new GradeView(courseCode, sectionId, components, finalGrade));
    }

    @Override
    public OperationResult<Void> recordScores(String instructorId,
                                              String sectionId,
                                              String enrollmentId,
                                              Map<String, Double> componentScores) {
        if (!accessController.canInstructorWrite()) {
            return OperationResult.failure("Maintenance mode ON. Grade entry disabled.");
        }
        Optional<Section> section = ensureOwnership(instructorId, sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Not your section.");
        }
        
        // Verify enrollment belongs to this section
        Optional<Enrollment> enrollment = erpRepository.findEnrollmentsBySection(sectionId)
                .stream()
                .filter(e -> e.getEnrollmentId().equals(enrollmentId))
                .findFirst();
        if (enrollment.isEmpty()) {
            return OperationResult.failure("Enrollment does not belong to this section.");
        }
        
        List<GradeComponent> components = componentScores.entrySet()
                .stream()
                .map(entry -> new GradeComponent(entry.getKey(), entry.getValue(), 0))
                .toList();
        GradeBook gradeBook = new GradeBook(enrollmentId, components, null);
        erpRepository.saveGradeBook(gradeBook);
        return OperationResult.success(null, "Scores saved.");
    }

    @Override
    public OperationResult<Void> computeFinalGrades(String instructorId,
                                                    String sectionId,
                                                    Map<String, Double> weights) {
        if (!accessController.canInstructorWrite()) {
            return OperationResult.failure("Maintenance mode ON. Grade computation disabled.");
        }
        Optional<Section> section = ensureOwnership(instructorId, sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Not your section.");
        }
        for (Enrollment enrollment : erpRepository.findEnrollmentsBySection(sectionId)) {
            GradeBook gradeBook = erpRepository.findGradeBook(enrollment.getEnrollmentId())
                    .orElse(new GradeBook(enrollment.getEnrollmentId(), List.of(), null));

            double finalScore = gradeBook.getComponents()
                    .stream()
                    .mapToDouble(component -> {
                        double weight = weights.getOrDefault(component.getName(), component.getWeight());
                        return component.getScore() * weight;
                    })
                    .sum();
            erpRepository.saveGradeBook(new GradeBook(enrollment.getEnrollmentId(), gradeBook.getComponents(), finalScore));
        }
        return OperationResult.success(null, "Final grades computed.");
    }

    @Override
    public OperationResult<List<GradeComponent>> listGradeComponents(String instructorId,
                                                                     String sectionId,
                                                                     String enrollmentId) {
        Optional<Section> section = ensureOwnership(instructorId, sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Not your section.");
        }
        Optional<Enrollment> enrollment = findEnrollment(sectionId, enrollmentId);
        if (enrollment.isEmpty()) {
            return OperationResult.failure("Enrollment does not belong to section.");
        }
        List<GradeComponent> components = erpRepository.findGradeBook(enrollmentId)
                .map(gradeBook -> new ArrayList<>(gradeBook.getComponents()))
                .orElseGet(ArrayList::new);
        return OperationResult.success(components);
    }

    @Override
    public OperationResult<Void> saveGradeComponents(String instructorId,
                                                     String sectionId,
                                                     String enrollmentId,
                                                     List<GradeComponent> components) {
        if (!accessController.canInstructorWrite()) {
            return OperationResult.failure("Maintenance mode ON. Grade entry disabled.");
        }
        Optional<Section> section = ensureOwnership(instructorId, sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Not your section.");
        }
        Optional<Enrollment> enrollment = findEnrollment(sectionId, enrollmentId);
        if (enrollment.isEmpty()) {
            return OperationResult.failure("Enrollment does not belong to section.");
        }
        List<GradeComponent> sanitized = components == null ? List.of() : components;
        OperationResult<Void> validation = validateComponents(sanitized);
        if (!validation.isSuccess()) {
            return validation;
        }
        Double finalGrade = sanitized.isEmpty()
                ? null
                : sanitized.stream()
                .mapToDouble(component -> component.getScore() * component.getWeight())
                .sum();
        GradeBook gradeBook = new GradeBook(enrollmentId, new ArrayList<>(sanitized), finalGrade);
        erpRepository.saveGradeBook(gradeBook);
        return OperationResult.success(null, "Grade book saved.");
    }

    @Override
    public OperationResult<Void> saveGradeComponentsWithFinal(String instructorId,
                                                              String sectionId,
                                                              String enrollmentId,
                                                              List<GradeComponent> components,
                                                              double finalGrade) {
        if (!accessController.canInstructorWrite()) {
            return OperationResult.failure("Maintenance mode ON. Grade entry disabled.");
        }
        Optional<Section> section = ensureOwnership(instructorId, sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Not your section.");
        }
        Optional<Enrollment> enrollment = findEnrollment(sectionId, enrollmentId);
        if (enrollment.isEmpty()) {
            return OperationResult.failure("Enrollment does not belong to section.");
        }
        List<GradeComponent> sanitized = components == null ? List.of() : components;
        // Validate scores are in range
        for (GradeComponent component : sanitized) {
            if (component.getScore() < 0 || component.getScore() > 100) {
                return OperationResult.failure("Scores must be between 0 and 100.");
            }
        }
        GradeBook gradeBook = new GradeBook(enrollmentId, new ArrayList<>(sanitized), finalGrade);
        erpRepository.saveGradeBook(gradeBook);
        return OperationResult.success(null, "Grades submitted successfully.");
    }

    private Optional<Section> ensureOwnership(String instructorId, String sectionId) {
        return erpRepository.findSection(sectionId)
                .filter(section -> section.getInstructorId().equals(instructorId));
    }

    private Optional<Enrollment> findEnrollment(String sectionId, String enrollmentId) {
        return erpRepository.findEnrollmentsBySection(sectionId)
                .stream()
                .filter(enrollment -> enrollment.getEnrollmentId().equals(enrollmentId))
                .findFirst();
    }

    private OperationResult<Void> validateComponents(List<GradeComponent> components) {
        if (components.isEmpty()) {
            return OperationResult.success(null);
        }
        Set<String> names = new HashSet<>();
        double totalWeight = 0d;
        for (GradeComponent component : components) {
            if (component.getName() == null || component.getName().isBlank()) {
                return OperationResult.failure("Component name cannot be empty.");
            }
            if (!names.add(component.getName().trim().toUpperCase())) {
                return OperationResult.failure("Duplicate component: " + component.getName());
            }
            if (component.getScore() < 0 || component.getScore() > 100) {
                return OperationResult.failure("Scores must be between 0 and 100.");
            }
            if (component.getWeight() <= 0 || component.getWeight() > 1) {
                return OperationResult.failure("Weights must be greater than 0 and at most 1.");
            }
            totalWeight += component.getWeight();
        }
        if (Math.abs(totalWeight - 1.0) > 0.001) {
            return OperationResult.failure("Weights must total 1.0. Current total: " + String.format("%.2f", totalWeight));
        }
        return OperationResult.success(null);
    }
}

