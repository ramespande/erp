package edu.univ.erp.infra;

import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;

import java.util.List;

public final class DemoCatalogSeeder {

    private static final int TARGET_COURSES = 100;
    private static final String DEMO_ID_PREFIX = "DEMO-COURSE-";
    private static final String DEMO_CODE_PREFIX = "DEMO";
    private static final List<String> SUBJECTS = List.of(
            "Algorithms", "Data Structures", "Networks", "Operating Systems", "Databases",
            "Machine Learning", "Artificial Intelligence", "Compilers", "Graphics", "Security",
            "Cloud Computing", "Distributed Systems", "Parallel Computing", "Cryptography",
            "Numerical Methods", "Linear Algebra", "Calculus", "Statistics", "Quantum Computing",
            "Signal Processing", "Embedded Systems", "Robotics", "Human Computer Interaction",
            "Software Engineering", "Mobile Computing", "IoT Fundamentals"
    );

    private DemoCatalogSeeder() {
    }

    public static void ensureBaselineCatalog(ErpRepository repository) {
        int existing = repository.listCourses().size();
        if (existing >= TARGET_COURSES) {
            return;
        }
        int toCreate = TARGET_COURSES - existing;
        for (int i = 0; i < toCreate; i++) {
            int sequence = existing + i + 1;
            String courseId = DEMO_ID_PREFIX + String.format("%03d", sequence);
            String code = DEMO_CODE_PREFIX + String.format("%03d", sequence);
            String title = SUBJECTS.get(sequence % SUBJECTS.size()) + " " + sequence;
            int credits = 3 + (sequence % 2);
            Course course = new Course(courseId, code, title, credits);
            repository.saveCourse(course);
        }
    }
}

