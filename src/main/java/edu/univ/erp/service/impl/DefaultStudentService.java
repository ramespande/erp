package edu.univ.erp.service.impl;

import com.opencsv.CSVWriter;
import edu.univ.erp.access.AccessController;
import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.api.types.CourseCatalogRow;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.api.types.TimetableEntry;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.enrollment.EnrollmentStatus;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.grade.GradeComponent;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.service.StudentService;

import java.io.IOException;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class DefaultStudentService implements StudentService {

    private final ErpRepository erpRepository;
    private final AccessController accessController;

    public DefaultStudentService(ErpRepository erpRepository, AccessController accessController) {
        this.erpRepository = erpRepository;
        this.accessController = accessController;
    }

    @Override
    public OperationResult<List<CourseCatalogRow>> viewCatalog() {
        List<CourseCatalogRow> rows = new ArrayList<>();
        for (Section section : erpRepository.listSections()) {
            Optional<Course> course = erpRepository.findCourse(section.getCourseId());
            String instructorName = erpRepository.findInstructor(section.getInstructorId())
                    .map(instructor -> instructor.getTitle() + " (" + instructor.getDepartment() + ")")
                    .orElse("TBD");
            int seatsTaken = erpRepository.findEnrollmentsBySection(section.getSectionId()).size();
            course.ifPresent(value -> rows.add(new CourseCatalogRow(
                    section.getSectionId(),
                    value.getCode(),
                    value.getTitle(),
                    value.getCredits(),
                    instructorName,
                    formatSchedule(section),
                    section.getCapacity(),
                    seatsTaken
            )));
        }
        rows.sort(Comparator.comparing(CourseCatalogRow::courseCode));
        return OperationResult.success(rows);
    }

    @Override
    public OperationResult<Void> registerSection(String studentId, String sectionId) {
        if (!accessController.canStudentWrite()) {
            return OperationResult.failure("Maintenance mode is ON. Registration is read-only.");
        }
        Optional<Student> student = erpRepository.findStudent(studentId);
        if (student.isEmpty()) {
            return OperationResult.failure("Student profile not found.");
        }
        Optional<Section> section = erpRepository.findSection(sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Section not found.");
        }

        if (erpRepository.findEnrollment(studentId, sectionId).isPresent()) {
            return OperationResult.failure("Already registered in this section.");
        }

        var sec = section.get();
        if (erpRepository.findEnrollmentsBySection(sectionId).size() >= sec.getCapacity()) {
            return OperationResult.failure("Section full.");
        }
        if (sec.getRegistrationDeadline().isBefore(java.time.LocalDate.now())) {
            return OperationResult.failure("Registration deadline passed.");
        }

        String enrollmentId = "enroll-" + studentId + "-" + sectionId;
        Enrollment enrollment = new Enrollment(enrollmentId, studentId, sectionId, EnrollmentStatus.ACTIVE);
        erpRepository.saveEnrollment(enrollment);
        return OperationResult.success(null, "Registration successful.");
    }

    @Override
    public OperationResult<Void> dropSection(String studentId, String sectionId) {
        if (!accessController.canStudentWrite()) {
            return OperationResult.failure("Maintenance mode is ON. Drop is disabled.");
        }
        Optional<Enrollment> enrollment = erpRepository.findEnrollment(studentId, sectionId);
        if (enrollment.isEmpty()) {
            return OperationResult.failure("Enrollment not found.");
        }

        Optional<Section> section = erpRepository.findSection(sectionId);
        if (section.isEmpty()) {
            return OperationResult.failure("Section not found.");
        }

        if (section.get().getRegistrationDeadline().isBefore(java.time.LocalDate.now())) {
            return OperationResult.failure("Registration deadline has passed. Cannot drop section.");
        }

        erpRepository.deleteEnrollment(enrollment.get().getEnrollmentId());
        return OperationResult.success(null, "Section dropped.");
    }

    @Override
    public OperationResult<List<TimetableEntry>> viewTimetable(String studentId) {
        List<Enrollment> enrollments = erpRepository.findEnrollmentsByStudent(studentId);
        List<TimetableEntry> entries = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            erpRepository.findSection(enrollment.getSectionId())
                    .ifPresent(section -> erpRepository.findCourse(section.getCourseId())
                            .ifPresent(course -> entries.add(new TimetableEntry(
                                    section.getDayOfWeek().name(),
                                    section.getStartTime() + " - " + section.getEndTime(),
                                    course.getCode(),
                                    section.getSectionId(),
                                    section.getRoom()
                            ))));
        }
        entries.sort(Comparator.comparing(TimetableEntry::day).thenComparing(TimetableEntry::timeRange));
        return OperationResult.success(entries);
    }

    @Override
    public OperationResult<List<GradeView>> viewGrades(String studentId) {
        List<GradeView> gradeViews = new ArrayList<>();
        for (Enrollment enrollment : erpRepository.findEnrollmentsByStudent(studentId)) {
            Optional<Section> section = erpRepository.findSection(enrollment.getSectionId());
            if (section.isEmpty()) {
                continue;
            }
            Optional<Course> course = erpRepository.findCourse(section.get().getCourseId());
            if (course.isEmpty()) {
                continue;
            }
            Optional<GradeBook> gradeBook = erpRepository.findGradeBook(enrollment.getEnrollmentId());
            List<GradeView.ComponentScore> componentScores = gradeBook
                    .map(GradeBook::getComponents)
                    .orElse(List.of())
                    .stream()
                    .map(component -> new GradeView.ComponentScore(component.getName(), component.getScore(), component.getWeight()))
                    .toList();

            // Calculate final grade if not present (for old data compatibility)
            Double finalGrade = gradeBook.flatMap(GradeBook::getFinalGrade).orElse(null);
            if (finalGrade == null && !componentScores.isEmpty()) {
                // Calculate from components: sum(score * weight) where weight is 0-1
                finalGrade = componentScores.stream()
                    .mapToDouble(comp -> comp.score() * comp.weight())
                    .sum();
                
                // Update the database with calculated final grade for old data
                if (gradeBook.isPresent()) {
                    GradeBook updated = new GradeBook(
                        gradeBook.get().getEnrollmentId(),
                        gradeBook.get().getComponents(),
                        finalGrade
                    );
                    erpRepository.saveGradeBook(updated);
                }
            }
            gradeViews.add(new GradeView(course.get().getCode(), section.get().getSectionId(), componentScores, finalGrade));
        }
        return OperationResult.success(gradeViews);
    }

    @Override
    public OperationResult<byte[]> downloadTranscriptCsv(String studentId) {
        OperationResult<List<GradeView>> gradeResult = viewGrades(studentId);
        if (!gradeResult.isSuccess()) {
            return OperationResult.failure(gradeResult.getMessage().orElse("Unable to build transcript."));
        }
        List<GradeView> gradeViews = gradeResult.getPayload().orElse(List.of());
        StringWriter writer = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(new String[] {"Course", "Section", "Component", "Score", "Weight", "Final"});
            for (GradeView view : gradeViews) {
                if (view.components().isEmpty()) {
                    csvWriter.writeNext(new String[] {view.courseCode(), view.sectionId(), "-", "-", "-", toStringSafe(view.finalGrade())});
                } else {
                    for (GradeView.ComponentScore component : view.components()) {
                        csvWriter.writeNext(new String[] {
                                view.courseCode(),
                                view.sectionId(),
                                component.name(),
                                String.valueOf(component.score()),
                                String.valueOf(component.weight()),
                                toStringSafe(view.finalGrade())
                        });
                    }
                }
            }
        } catch (IOException ex) {
            return OperationResult.failure("Failed to build transcript: " + ex.getMessage());
        }
        return OperationResult.success(writer.toString().getBytes(), "Transcript ready.");
    }

    private String toStringSafe(Double value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String formatSchedule(Section section) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return section.getDayOfWeek().name() + " " + formatter.format(section.getStartTime())
                + "-" + formatter.format(section.getEndTime());
    }
}

