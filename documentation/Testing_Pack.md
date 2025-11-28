## Testing Pack

> All tests assume `database.sql` has been loaded (creates both `erp_auth` and `erp_data`) and the desktop app is started via `run.bat`. Default accounts:
> - Admin `ranjan / bose`
> - Instructor `sambuddho / chakravarty`
> - Student 1 `rijul / agarwal`
> - Student 2 `nakul / zutshi`

### 1. Test Plan (Acceptance + Edge)

| ID | Scenario | Steps | Expected Result |
| A.1 | Login wrong password | Attempt login with valid username + wrong password | Error banner “Incorrect username or password.” |
| A.2 | Login per role | Login as each role | Student sees student dashboard; instructor sees instructor view; admin sees admin console |
| B.1 | Student catalog | Student logs in → `Catalog` tab | Table listing code/title/credits/capacity/instructor |
| B.2 | Register free seat | `rijul` registers `SEC-OS-001` (has capacity) | Success toast + entry in “My Registrations” and timetable |
| B.3 | Double register | Try to register same section again | Blocked with message “Already registered” |
| B.4 | Section full | `nakul` attempts `SEC-DB-001` (capacity 1 already filled) | Blocked with “Section full.” |
| B.5 | Drop before deadline | Drop `SEC-OS-001` before deadline | Success message; removed from registrations/timetable |
| B.6 | View grades | Open Grades tab | Shows course list + components + computed total |
| B.7 | Transcript export | Click “Download Transcript (CSV)” | CSV saved locally; open to verify contents |
| C.1 | Instructor sees own sections | Instructor dashboard → “My Sections” | Only sections taught by `sambuddho` |
| C.2 | Enter scores | Instructor selects a section + student, enters quiz/midterm/final and saves | Values persist; final grade computed |
| C.3 | Compute final grade | Click “Compute Final Grade” | Final grade matches weighting rule |
| C.4 | Edit other instructor section | Attempt to edit non-owned section | Blocked with “Not your section.” |
| D.1 | Admin create student | Admin → Manage → Add User (role STUDENT) + student profile | New entry appears in auth + ERP tables |
| D.2 | Create course + section | Admin adds new course and section, assigns instructor | Course listed in catalog; section appears under instructor |
| D.3 | Maintenance ON | Toggle ON | Banner shows; student/instructor view but cannot register/drop/grade (error notice) |
| D.4 | Maintenance OFF | Toggle OFF | Normal behavior returns |
| E.1 | Password separation | Inspect DBs | Auth DB stores hashes only; ERP DB has no password columns |
| F.1 | Capacity validation | Try to create section with negative capacity | Form validation rejects |
| F.2 | Deadline validation | Try to register/drop after deadline (`SEC-HI-001`) | Blocked with message “Deadline passed.” |
| F.3 | Authorization | Student tries to access another student data / instructor grades other instructor | Blocked by access controller |
| F.4 | Maintenance enforcement | With maintenance ON, attempt all write actions | Every change blocked with maintenance message |
| G.1 | Duplicate enrollment guard | Register same student+section twice | Prevented in UI + DB unique constraint |
| G.2 | Remove section with enrollments | Admin tries to delete section with active students | Blocked with message indicating existing enrollments |
| H.1 | UI/UX clarity | Visually inspect buttons/labels/errors | Friendly text, clear states |
| H.2 | Long actions feedback | Trigger export/import | “Please wait” dialog appears |
| I.1 | Performance sanity | Load catalog (~100 demo entries after seeding) | Loads within a few seconds |
| I.2 | App startup | Run `run.bat` | Window appears promptly without crash |
| J.1 | Backup/restore (optional) | Use admin backup + edit + restore | Data reverts to backup snapshot |

### 2. Small Test Dataset (reference)

- Users/passwords: see top of document.
- Sections & constraints:
  - `SEC-DB-001` – Database Systems, cap 1, `rijul` enrolled → used for “section full”.
  - `SEC-OS-001` – Operating Systems, free seats → used for register/drop.
  - `SEC-HI-001` – Modern History, deadline passed → used for deadline validation.
- Maintenance flag stored in `erp_data.settings`.
- Exports use default download folder unless changed.

### 3. One-page Test Summary

- **Build + Unit Tests:** `build.bat` then `run-tests.bat` (JUnit service tests). All must pass before manual runs.
- **Manual Acceptance Run:** Execute each scenario in Section 1 (A–J). Record pass/fail + screenshots.
- **Data Reset:** `mysql -u root -p < database.sql` to restore original accounts/courses before next run.
- **Known Issues / Notes:** track in project tracker; include any failures here when handing off QA notes.

