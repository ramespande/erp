package edu.univ.erp.service;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.data.memory.InMemoryDataStore;
import edu.univ.erp.data.memory.InMemoryErpRepository;
import edu.univ.erp.service.impl.DefaultStudentService;
import edu.univ.erp.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {

    private InMemoryDataStore store;
    private DefaultStudentService service;
    private AccessController accessController;
    private String studentId;

    @BeforeEach
    void setUp() {
        store = InMemoryDataStore.seed();
        accessController = new AccessController();
        service = new DefaultStudentService(new InMemoryErpRepository(store), accessController);
        studentId = TestData.userIdForUsername(store, "stu1");
    }

    @Test
    void shouldRegisterSectionWhenSeatAvailable() {
        var result = service.registerSection(studentId, "section-2");

        assertTrue(result.isSuccess(), "Registration should succeed");
        assertEquals("Registration successful.", result.getMessage().orElse(""));
    }

    @Test
    void shouldPreventDuplicateEnrollment() {
        var first = service.registerSection(studentId, "section-2");
        assertTrue(first.isSuccess());

        var duplicate = service.registerSection(studentId, "section-2");
        assertFalse(duplicate.isSuccess());
        assertEquals("Already registered in this section.", duplicate.getMessage().orElse(""));
    }

    @Test
    void shouldDropExistingSection() {
        var dropResult = service.dropSection(studentId, "section-1");

        assertTrue(dropResult.isSuccess());
        assertEquals("Section dropped.", dropResult.getMessage().orElse(""));
    }

    @Test
    void shouldBlockRegistrationWhenMaintenanceOn() {
        accessController.setMaintenanceMode(true);

        var result = service.registerSection(studentId, "section-2");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().orElse("").contains("Maintenance mode is ON"));
    }

    @Test
    void shouldExportTranscriptCsv() {
        var result = service.downloadTranscriptCsv(studentId);

        assertTrue(result.isSuccess());
        byte[] bytes = result.getPayload().orElse(null);
        assertNotNull(bytes);
        String csv = new String(bytes);
        assertTrue(csv.contains("Course"));
    }
}

