package edu.univ.erp.service;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.data.memory.InMemoryDataStore;
import edu.univ.erp.data.memory.InMemoryErpRepository;
import edu.univ.erp.service.impl.DefaultMaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaintenanceServiceTest {

    private DefaultMaintenanceService service;
    private AccessController accessController;

    @BeforeEach
    void setUp() {
        InMemoryDataStore store = InMemoryDataStore.seed();
        accessController = new AccessController();
        service = new DefaultMaintenanceService(new InMemoryErpRepository(store), accessController);
    }

    @Test
    void shouldToggleMaintenanceFlag() {
        assertFalse(service.isMaintenanceOn());

        var enabled = service.toggle(true);
        assertTrue(enabled.isSuccess());
        assertTrue(service.isMaintenanceOn());
        assertTrue(accessController.isMaintenanceMode());

        var disabled = service.toggle(false);
        assertTrue(disabled.isSuccess());
        assertFalse(service.isMaintenanceOn());
        assertFalse(accessController.isMaintenanceMode());
    }
}

