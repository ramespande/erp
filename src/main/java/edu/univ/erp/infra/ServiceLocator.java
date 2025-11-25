package edu.univ.erp.infra;

import edu.univ.erp.access.AccessController;
import edu.univ.erp.auth.SessionContext;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.data.jdbc.JdbcAuthRepository;
import edu.univ.erp.data.jdbc.JdbcErpRepository;
import javax.sql.DataSource;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.impl.DefaultAdminService;
import edu.univ.erp.service.impl.DefaultAuthService;
import edu.univ.erp.service.impl.DefaultInstructorService;
import edu.univ.erp.service.impl.DefaultMaintenanceService;
import edu.univ.erp.service.impl.DefaultStudentService;

public final class ServiceLocator {

    private static final AccessController ACCESS_CONTROLLER = new AccessController();
    private static final SessionContext SESSION_CONTEXT = new SessionContext();

    private static final DataSource AUTH_DATA_SOURCE = DataSourceConfig.getAuthDataSource();
    private static final DataSource ERP_DATA_SOURCE = DataSourceConfig.getErpDataSource();

    private static final AuthRepository AUTH_REPOSITORY = new JdbcAuthRepository(AUTH_DATA_SOURCE);
    private static final ErpRepository ERP_REPOSITORY = new JdbcErpRepository(ERP_DATA_SOURCE);

    static {
        ACCESS_CONTROLLER.setMaintenanceMode(ERP_REPOSITORY.getMaintenanceSetting().isMaintenanceOn());
    }

    private static final AuthService AUTH_SERVICE = new DefaultAuthService(AUTH_REPOSITORY, SESSION_CONTEXT);
    private static final StudentService STUDENT_SERVICE = new DefaultStudentService(ERP_REPOSITORY, ACCESS_CONTROLLER);
    private static final InstructorService INSTRUCTOR_SERVICE = new DefaultInstructorService(ERP_REPOSITORY, ACCESS_CONTROLLER);
    private static final AdminService ADMIN_SERVICE = new DefaultAdminService(AUTH_REPOSITORY, ERP_REPOSITORY, ACCESS_CONTROLLER);
    private static final MaintenanceService MAINTENANCE_SERVICE = new DefaultMaintenanceService(ERP_REPOSITORY, ACCESS_CONTROLLER);

    private ServiceLocator() {
    }

    public static AuthService authService() {
        return AUTH_SERVICE;
    }

    public static StudentService studentService() {
        return STUDENT_SERVICE;
    }

    public static InstructorService instructorService() {
        return INSTRUCTOR_SERVICE;
    }

    public static AdminService adminService() {
        return ADMIN_SERVICE;
    }

    public static MaintenanceService maintenanceService() {
        return MAINTENANCE_SERVICE;
    }

    public static SessionContext sessionContext() {
        return SESSION_CONTEXT;
    }

    public static AccessController accessController() {
        return ACCESS_CONTROLLER;
    }

    /**
     * Data is persisted immediately through JDBC, so this is a no-op that remains for
     * backwards compatibility.
     */
    public static void saveData() {
        // No-op: database writes are flushed on each transaction
    }
}

