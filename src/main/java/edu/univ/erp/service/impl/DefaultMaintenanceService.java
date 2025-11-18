package edu.univ.erp.service.impl;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.service.MaintenanceService;

public final class DefaultMaintenanceService implements MaintenanceService {

    private final ErpRepository erpRepository;
    private final AccessController accessController;

    public DefaultMaintenanceService(ErpRepository erpRepository, AccessController accessController) {
        this.erpRepository = erpRepository;
        this.accessController = accessController;
    }

    @Override
    public boolean isMaintenanceOn() {
        return erpRepository.getMaintenanceSetting().isMaintenanceOn();
    }

    @Override
    public OperationResult<Boolean> toggle(boolean enable) {
        erpRepository.saveMaintenanceSetting(enable);
        accessController.setMaintenanceMode(enable);
        return OperationResult.success(enable, enable ? "Maintenance enabled." : "Maintenance disabled.");
    }
}

