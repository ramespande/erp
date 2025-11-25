package edu.univ.erp.domain.settings;

import java.io.Serializable;

public final class MaintenanceSetting implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean maintenanceOn;

    public MaintenanceSetting(boolean maintenanceOn) {
        this.maintenanceOn = maintenanceOn;
    }

    public boolean isMaintenanceOn() {
        return maintenanceOn;
    }
}

