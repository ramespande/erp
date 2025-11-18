package edu.univ.erp.access;

public final class AccessController {

    private boolean maintenanceMode;

    public boolean canStudentWrite() {
        return !maintenanceMode;
    }

    public boolean canInstructorWrite() {
        return !maintenanceMode;
    }

    public boolean canAdminWrite() {
        return true;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }
}

