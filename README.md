# University ERP (Java + Swing)

This repository contains a standalone desktop ERP prototype for universities. It demonstrates a complete stack built from scratch according to the project brief:

- Role-based dashboards for students, instructors, and admins.
- Dual-database mindset (Auth vs ERP data) with clear separation in the code structure.
- Service/API abstraction between UI and data layers.
- Maintenance mode enforcement that blocks student/instructor writes.
- In-memory data store with seed users so the app runs without installing a DB. JDBC repositories can later point to real databases.

## Tech Stack

| Concern | Choice |
| --- | --- |
| Language | Java 21 |
| UI | Swing + FlatLaf + MigLayout |
| Persistence | In-memory seed store (H2-ready repositories wired later) |
| Security | jBCrypt password hashing |
| Exports | OpenCSV, PDFBox (CSV implemented, PDF planned) |
| Build | Plain Java (javac + `build.bat`) |

## Quick Start

1. Install **JDK 21** (any vendor).
2. Download the required third-party jars listed in `lib/README.txt` and place them in `lib/`.
3. Build the project:
   ```
   .\build.bat
   ```
4. Run the desktop app (MAIN_CLASS defaults to `edu.univ.erp.Application`):
   ```
   .\run.bat
   ```
5. Login with any seeded account:

| Username | Role | Password |
| --- | --- | --- |
| `admin1` | Admin | `admin123` |
| `inst1` | Instructor | `inst123` |
| `stu1` | Student | `stu123` |
| `stu2` | Student | `stu123` |

## Current Capabilities

- Auth: bcrypt-backed login + change password placeholder.
- Student: course catalog, register/drop with seat/deadline checks, timetable view, grades snapshot, transcript CSV export, maintenance banner.
- Instructor: list sections, quick grade snapshot, ad-hoc component entry, compute final grades with custom weights, maintenance-aware.
- Admin: add users/profiles, courses, sections, assign instructors, toggle maintenance.
- Architecture documents: see `docs/PROJECT_PLAN.md` for the roadmap.

## Project Layout

```
src/main/java/edu/univ/erp/
 ├── api            # DTOs and OperationResult wrapper
 ├── auth           # Password hashing + session context
 ├── access         # Role/maintenance guard
 ├── data           # Auth/ERP repositories + in-memory seeds
 ├── domain         # POJOs for core entities
 ├── infra          # Service locator / wiring
 ├── service        # Interfaces + default implementations
 └── ui             # Swing frames per role
```

## Build, Run & Package

- `build.bat`: compiles sources into `out/classes`.
- `run.bat`: launches the app (set `MAIN_CLASS` if you change the entry point).
- `package.bat`: creates `dist/erp-app` (classes + libs + helper scripts) and a plain `dist/erp-app.jar`.
- `run-tests.bat`: compiles and executes JUnit tests using the `junit-platform-console-standalone` jar you place in `lib/`.

## Dependencies

All third-party jars are managed manually through the `lib/` directory. See `lib/README.txt` for exact Maven coordinates and download commands (FlatLaf, MigLayout, jBCrypt, OpenCSV, PDFBox, HikariCP, H2, SLF4J/Logback, JUnit, Mockito, etc.).

## Testing

After populating `lib/` with the runtime and test jars:

```
.\run-tests.bat
```

The suite covers student registration/drop flows, instructor grading, and maintenance toggling logic.

## Next Steps

- Swap in actual JDBC repositories backed by MySQL/H2 (two schemas).
- Flesh out instructor gradebook UI (per-student views) and add CSV import/export.
- Implement PDF transcript, change password dialog, login attempt lockout.
- Add Flyway migrations, full test plan, and demo assets under `docs/`.