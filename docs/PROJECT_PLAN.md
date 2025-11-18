## University ERP – Project Plan

### Goals
- Deliver a role-aware desktop ERP written in Java + Swing covering student, instructor, and admin workflows.
- Enforce two-database separation (Auth DB for credentials, ERP DB for academic data) with clear service boundaries.
- Provide a maintainable codebase with modular packages, consistent APIs, and automated testing.

### Architecture Snapshot
- **UI Layer (`edu.univ.erp.ui`)**: Swing frames/panels grouped by role, using presenters/controllers to isolate business logic.
- **API Layer (`edu.univ.erp.api.*`)**: Coarse-grained facades that validate inputs, enforce access + maintenance checks, and coordinate services. Returns `OperationResult<T>` objects to the UI.
- **Service Layer (`edu.univ.erp.service`)**: Core business logic for registration, grading, course management, auth, reports, maintenance.
- **Domain Layer (`edu.univ.erp.domain`)**: Immutable POJOs representing Students, Courses, Sections, Enrollments, Grades, Settings.
- **Data Layer (`edu.univ.erp.data`)**: Repositories/DAOs that talk to either Auth DB (users/password hashes) or ERP DB (everything else) via JDBC + HikariCP. Two separate `DataSourceFactory` classes keep connections isolated.
- **Access Layer (`edu.univ.erp.access`)**: Centralized policy engine ensuring role-based permissions and maintenance-mode gating.
- **Auth Layer (`edu.univ.erp.auth`)**: Password hashing/verification (bcrypt via jBCrypt), session management, login attempt tracking.
- **Util Layer (`edu.univ.erp.util`)**: Export helpers (CSV/PDF), i18n message catalog, date/time utils, and lightweight logging façade.

### Technology Stack
| Concern | Choice |
| --- | --- |
| Build | Gradle (Java 21, application + shadow plugin for packaging) |
| UI | Swing + FlatLaf (Look & Feel) + MigLayout |
| DB | MySQL (prod), H2 (dev/test). Separate schemas: `auth_db`, `erp_db`. |
| JDBC | HikariCP pooled connections |
| Hashing | jBCrypt |
| CSV/PDF | OpenCSV, Apache PDFBox |
| Testing | JUnit 5, Mockito |
| Logging | SLF4J + Logback |

### Milestones (Adaptive 8-Week Plan)
1. **Week 1** – Requirements deep dive, UX wireframes, schema draft, choose tech stack, bootstrap Gradle + package layout.
2. **Week 2** – Implement dual DB schema (Flyway scripts), seed sample users/data, core domain objects, and DAO interfaces.
3. **Week 3** – Auth flow (login/logout, maintenance banner hook), dashboards per role, OperationResult messaging.
4. **Week 4** – Student flows (catalog, registration/drop with checks, timetable, grades view, transcript CSV export).
5. **Week 5** – Instructor flows (section management, grade components, final calculation, class stats, optional CSV import/export).
6. **Week 6** – Admin flows (user/course/section CRUD, instructor assignment, maintenance toggle, backup hook).
7. **Week 7** – Polishing: validation, error handling, FlatLaf theming, PDF transcript option, Change Password bonus.
8. **Week 8** – Comprehensive testing, test report, screenshots, documentation, demo recording, packaging.

### Data & Testing Deliverables
- `db/auth_db.sql` & `db/erp_db.sql` (schema + seed data) generated via Flyway migrations.
- `docs/test_plan.md`, `docs/test_summary.md`, `docs/report.md`, `docs/diagrams/*.png`, `docs/how_to_run.md`.
- Sample credentials: `admin1/admin123`, `inst1/inst123`, `stu1/stu123`, `stu2/stu123` (stored as bcrypt hashes).

### Next Steps
1. Configure Gradle multi-module layout (`app`, `data`, `auth`, `ui` packages share a single build).
2. Scaffold source directories with base classes/interfaces.
3. Add Flyway migration scripts and sample seed data.
4. Implement Auth + Maintenance foundations before feature flows.

This plan will evolve as implementation progresses; updates will be logged in `docs/STATUS.md`.

