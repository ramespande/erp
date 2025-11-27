package edu.univ.erp.service.support;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public final class CourseSectionBackupService {

    private static final String COURSE_PREFIX = "COURSE";
    private static final String SECTION_PREFIX = "SECTION";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ErpRepository erpRepository;

    public CourseSectionBackupService(ErpRepository erpRepository) {
        this.erpRepository = erpRepository;
    }

    public OperationResult<String> exportSnapshot() {
        List<String> lines = new ArrayList<>();
        for (Course course : erpRepository.listCourses()) {
            lines.add(String.join("|",
                    COURSE_PREFIX,
                    encode(course.getCourseId()),
                    encode(course.getCode()),
                    encode(course.getTitle()),
                    String.valueOf(course.getCredits())
            ));
        }
        for (Section section : erpRepository.listSections()) {
            lines.add(String.join("|",
                    SECTION_PREFIX,
                    encode(section.getSectionId()),
                    encode(section.getCourseId()),
                    encode(section.getInstructorId()),
                    section.getDayOfWeek().name(),
                    section.getStartTime().toString(),
                    section.getEndTime().toString(),
                    encode(section.getRoom()),
                    String.valueOf(section.getCapacity()),
                    String.valueOf(section.getSemester()),
                    String.valueOf(section.getYear()),
                    section.getRegistrationDeadline().toString()
            ));
        }
        String payload = "# University ERP backup\n" + lines.stream().collect(Collectors.joining("\n"));
        return OperationResult.success(payload, "Backup created.");
    }

    public OperationResult<Void> importSnapshot(String payload) {
        if (payload == null || payload.isBlank()) {
            return OperationResult.failure("Backup data is empty.");
        }
        List<Course> courses = new ArrayList<>();
        List<Section> sections = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(payload))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (COURSE_PREFIX.equals(parts[0]) && parts.length == 5) {
                    courses.add(new Course(
                            decode(parts[1]),
                            decode(parts[2]),
                            decode(parts[3]),
                            Integer.parseInt(parts[4])
                    ));
                } else if (SECTION_PREFIX.equals(parts[0]) && parts.length == 12) {
                    sections.add(new Section(
                            decode(parts[1]),
                            decode(parts[2]),
                            decode(parts[3]),
                            DayOfWeek.valueOf(parts[4]),
                            LocalTime.parse(parts[5]),
                            LocalTime.parse(parts[6]),
                            decode(parts[7]),
                            Integer.parseInt(parts[8]),
                            Integer.parseInt(parts[9]),
                            Integer.parseInt(parts[10]),
                            LocalDate.parse(parts[11])
                    ));
                }
            }
            courses.forEach(erpRepository::saveCourse);
            sections.forEach(erpRepository::saveSection);
            return OperationResult.success(null, "Backup restored.");
        } catch (Exception ex) {
            return OperationResult.failure("Failed to restore backup: " + ex.getMessage());
        }
    }

    private String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String decode(String token) {
        return new String(DECODER.decode(token), java.nio.charset.StandardCharsets.UTF_8);
    }
}

