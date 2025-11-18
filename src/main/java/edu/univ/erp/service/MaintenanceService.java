package edu.univ.erp.service;

import edu.univ.erp.api.common.OperationResult;

public interface MaintenanceService {
    boolean isMaintenanceOn();

    OperationResult<Boolean> toggle(boolean enable);
}

