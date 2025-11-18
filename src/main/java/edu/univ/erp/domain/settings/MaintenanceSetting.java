package edu.univ.erp.domain.settings;

public final class MaintenanceSetting {
    private final boolean maintenanceOn;

    public MaintenanceSetting(boolean maintenanceOn) {
        this.maintenanceOn = maintenanceOn;
    }

    public boolean isMaintenanceOn() {
        return maintenanceOn;
    }
}

