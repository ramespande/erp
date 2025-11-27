package edu.univ.erp.service;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.data.memory.InMemoryDataStore;
import edu.univ.erp.data.memory.InMemoryErpRepository;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.grade.GradeComponent;
import edu.univ.erp.service.impl.DefaultInstructorService;
import edu.univ.erp.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructorServiceTest {

    private InMemoryDataStore store;
    private DefaultInstructorService service;
    private AccessController accessController;
    private String instructorId;
    private String enrollmentId;

    @BeforeEach
    void setUp() {
        store = InMemoryDataStore.seed();
        accessController = new AccessController();
        service = new DefaultInstructorService(new InMemoryErpRepository(store), accessController);
        instructorId = TestData.userIdForUsername(store, "inst1");
        enrollmentId = store.enrollments().values().stream()
                .filter(enrollment -> enrollment.getSectionId().equals("section-1"))
                .map(Enrollment::getEnrollmentId)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void shouldListOwnedSections() {
        var result = service.listMySections(instructorId);

        assertTrue(result.isSuccess());
        assertTrue(result.getPayload().orElseThrow().contains("section-1"));
    }

    @Test
    void shouldRecordScores() {
        var result = service.recordScores(instructorId, "section-1", enrollmentId, Map.of("Quiz", 20.0));

        assertTrue(result.isSuccess());
        assertEquals("Scores saved.", result.getMessage().orElse(""));
    }

    @Test
    void shouldComputeFinalGrades() {
        // Ensure scores exist
        service.recordScores(instructorId, "section-1", enrollmentId, Map.of("Quiz", 20.0, "EndSem", 50.0));

        var result = service.computeFinalGrades(instructorId, "section-1", Map.of("Quiz", 0.4, "EndSem", 0.6));

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectOtherInstructor() {
        var result = service.recordScores("other", "section-1", enrollmentId, Map.of("Quiz", 10.0));

        assertFalse(result.isSuccess());
        assertEquals("Not your section.", result.getMessage().orElse(""));
    }

    @Test
    void shouldBlockWhenMaintenanceOn() {
        accessController.setMaintenanceMode(true);

        var result = service.recordScores(instructorId, "section-1", enrollmentId, Map.of("Quiz", 10.0));

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().orElse("").contains("Maintenance mode ON"));
    }

    @Test
    void shouldSaveAndListGradeComponents() {
        var components = List.of(
                new GradeComponent("Quiz", 80, 0.4),
                new GradeComponent("Project", 90, 0.3),
                new GradeComponent("Final", 85, 0.3)
        );

        var saveResult = service.saveGradeComponents(instructorId, "section-1", enrollmentId, components);
        assertTrue(saveResult.isSuccess());

        var listResult = service.listGradeComponents(instructorId, "section-1", enrollmentId);
        assertTrue(listResult.isSuccess());
        assertEquals(3, listResult.getPayload().orElseThrow().size());
    }

    @Test
    void shouldRejectComponentsWhenWeightsDoNotSumToOne() {
        var components = List.of(
                new GradeComponent("Quiz", 80, 0.2),
                new GradeComponent("Final", 90, 0.2)
        );

        var result = service.saveGradeComponents(instructorId, "section-1", enrollmentId, components);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().orElse("").contains("Weights must total 1.0"));
    }
}

