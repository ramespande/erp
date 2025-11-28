### **Summary of Sample Data:**

* **One Admin**: `ranjan` (username: `ranjan`, role: `ADMIN`).
* **One Instructor**: `sambuddho` (username: `sambuddho`, role: `INSTRUCTOR`, has sections).
* **Two Students**: `rijul` and `nakul` (both enrolled in courses).
* **Courses**: Two courses (`CS301` - Database Systems, `CS302` - Operating Systems).
* **Sections**: Two sections, one for each course.
* **Enrollments**: `rijul` is enrolled in `SEC-DB-001` and `nakul` in `SEC-OS-001`.
* **Grade Components**: Grades are stored for `rijul`'s course `SEC-DB-001`.


### **ERP Authentication Database (erp_auth)**

#### **Table: auth_users** (user credentials)

| **user_id**   | **username** | **role**   | **password_hash**                                            | 
| ------------- | ------------ | ---------- | ------------------------------------------------------------ | 
| USR-ADMIN-001 | ranjan       | ADMIN      | $2a$12$1eN0XN1Iwvx7/clESWbb1.2L/SzjdugUR0A4BS9D9gtyYYZur/vZ. | 
| USR-INST-001  | sambuddho    | INSTRUCTOR | $2a$12$tCYQ/3wc30MD6oiwIYjDf.D6bOkOiwGRk5zxm3jF.yjrJO/naubOO | 
| USR-STU-001   | rijul        | STUDENT    | $2a$12$1UwpU/Pt0IgBvuxgCE2R2eixdAZo/1xfhOP/gJDkMBIRgglkcgJpO | 
| USR-STU-002   | nakul        | STUDENT    | $2a$12$42MBZ/eg9k3ZCh2JBTVaduyhVH/I2aRzKLj8zD0upRYiZxSMBcodi | 

---

### **ERP Data Database (erp_data)**

#### **Table: students** (student details)

| **user_id** | **roll_no** | **program**      | **academic_year** |
| ----------- | ----------- | ---------------- | ----------------- |
| USR-STU-001 | 2021CS001   | Computer Science | 3                 |
| USR-STU-002 | 2021CS002   | Computer Science | 3                 |

---

#### **Table: instructors** (instructor details)

| **user_id**  | **department**   | **title**           | 
| ------------ | ---------------- | ------------------- | 
| USR-INST-001 | Computer Science | Associate Professor | 

---

#### **Table: courses** (course details)

| **course_id** | **code** | **title**         | **credits** |
| ------------- | -------- | ----------------- | ----------- |
| CRS-001       | CS301    | Database Systems  | 4           |
| CRS-002       | CS302    | Operating Systems | 4           |

---

#### **Table: sections** (section details)

| **section_id** | **course_id** | **instructor_id** | **day_of_week** | **start_time** | **end_time** | **room** | **capacity** | **semester** | **academic_year** | **registration_deadline**            | **weighting_rule** | **component_names** |
| -------------- | ------------- | ----------------- | --------------- | -------------- | ------------ | -------- | ------------ | ------------ | ----------------- | ------------------------------------ | ------------------ | ------------------- |
| SEC-DB-001     | CRS-001       | USR-INST-001      | MONDAY          | 09:00:00       | 10:30:00     | LH-101   | 1            | 1            | 2024              | DATE_ADD(CURDATE(), INTERVAL 1 YEAR) | 30,30,40           | Quiz,Midterm,Final  |
| SEC-OS-001     | CRS-002       | USR-INST-001      | WEDNESDAY       | 14:00:00       | 15:30:00     | LH-201   | 40           | 1            | 2024              | DATE_ADD(CURDATE(), INTERVAL 1 YEAR) | 20,30,50           | Lab,Midterm,Final   |

---

#### **Table: enrollments** (student enrollments)

| **enrollment_id** | **student_id** | **section_id** | **status** | 
| ----------------- | -------------- | -------------- | ---------- | 
| ENR-001           | USR-STU-001    | SEC-DB-001     | ACTIVE     | 
| ENR-002           | USR-STU-002    | SEC-OS-001     | ACTIVE     | 

---

#### **Table: grade_books** (final grades for enrollments)

| **enrollment_id** | **final_grade** | 
| ----------------- | --------------- | 
| ENR-001           | NULL            | 
| ENR-002           | NULL            | 

---

#### **Table: grade_components** (grade components like quiz, midterm, etc.)

| **component_id** | **enrollment_id** | **name** | **score** | **weight** | 
| ---------------- | ----------------- | -------- | --------- | ---------- | 
| 1                | ENR-001           | Quiz     | 85.0      | 0.30       | 
| 2                | ENR-001           | Midterm  | 78.0      | 0.30       | 
| 3                | ENR-001           | Final    | 82.0      | 0.40       | 

---

#### **Table: settings** (system settings)

| **key_name**   | **value** | 
| -------------- | --------- | 
| maintenance_on | false     | 

---