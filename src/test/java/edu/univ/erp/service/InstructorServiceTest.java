package edu.univ.erp.service;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.data.memory.InMemoryDataStore;
import edu.univ.erp.data.memory.InMemoryErpRepository;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.service.impl.DefaultInstructorService;
import edu.univ.erp.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}

