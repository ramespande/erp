package edu.univ.erp.ui.admin;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.MaintenanceService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class AdminDashboardFrame extends JFrame {

    private final AdminService adminService = ServiceLocator.adminService();
    private final MaintenanceService maintenanceService = ServiceLocator.maintenanceService();

    private final JLabel maintenanceLabel = new JLabel("", SwingConstants.CENTER);

    public AdminDashboardFrame() {
        super("Admin Dashboard");
        setSize(720, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridLayout(1, 3, 12, 12));
        root.add(buildUserPanel());
        root.add(buildCoursePanel());
        root.add(buildMaintenancePanel());

        add(root, BorderLayout.CENTER);
        add(maintenanceLabel, BorderLayout.SOUTH);
        refreshMaintenanceLabel();
    }

    private JPanel buildUserPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Users & Profiles"));

        JButton addUser = new JButton("Add User…");
        addUser.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Username");
            String password = JOptionPane.showInputDialog(this, "Temp Password");
            String role = JOptionPane.showInputDialog(this, "Role (ADMIN/INSTRUCTOR/STUDENT)");
            if (username == null || password == null || role == null) {
                return;
            }
            notifyResult(adminService.addUser(username.trim(), password.trim(), role.trim()));
        });

        JButton addStudent = new JButton("Add Student Profile…");
        addStudent.addActionListener(e -> {
            String userId = JOptionPane.showInputDialog(this, "User ID");
            String roll = JOptionPane.showInputDialog(this, "Roll No");
            String program = JOptionPane.showInputDialog(this, "Program");
            String year = JOptionPane.showInputDialog(this, "Year");
            if (userId == null) {
                return;
            }
            Student student = new Student(userId.trim(), roll, program, parseInt(year, 1));
            notifyResult(adminService.addStudentProfile(student));
        });

        JButton addInstructor = new JButton("Add Instructor Profile…");
        addInstructor.addActionListener(e -> {
            String userId = JOptionPane.showInputDialog(this, "User ID");
            String dept = JOptionPane.showInputDialog(this, "Department");
            String title = JOptionPane.showInputDialog(this, "Title");
            if (userId == null) {
                return;
            }
            Instructor instructor = new Instructor(userId.trim(), dept, title);
            notifyResult(adminService.addInstructorProfile(instructor));
        });

        panel.add(addUser);
        panel.add(addStudent);
        panel.add(addInstructor);
        return panel;
    }

    private JPanel buildCoursePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Courses & Sections"));

        JButton addCourse = new JButton("Add Course…");
        addCourse.addActionListener(e -> {
            JTextField codeField = new JTextField();
            JTextField titleField = new JTextField();
            JTextField creditsField = new JTextField();
            Object[] message = {"Code", codeField, "Title", titleField, "Credits", creditsField};
            int option = JOptionPane.showConfirmDialog(this, message, "New Course", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                Course course = new Course(UUID.randomUUID().toString(),
                        codeField.getText(),
                        titleField.getText(),
                        parseInt(creditsField.getText(), 4));
                notifyResult(adminService.addCourse(course));
            }
        });

        JButton addSection = new JButton("Create Section…");
        addSection.addActionListener(e -> {
            String courseId = JOptionPane.showInputDialog(this, "Course ID");
            String instructorId = JOptionPane.showInputDialog(this, "Instructor ID");
            String day = JOptionPane.showInputDialog(this, "Day of Week (e.g., MONDAY)");
            String start = JOptionPane.showInputDialog(this, "Start Time (HH:mm)");
            String end = JOptionPane.showInputDialog(this, "End Time (HH:mm)");
            String room = JOptionPane.showInputDialog(this, "Room");
            String capacity = JOptionPane.showInputDialog(this, "Capacity");
            if (courseId == null || day == null || start == null || end == null) {
                return;
            }
            Section section = new Section(
                    UUID.randomUUID().toString(),
                    courseId.trim(),
                    instructorId == null ? "" : instructorId.trim(),
                    DayOfWeek.valueOf(day.trim().toUpperCase()),
                    LocalTime.parse(start.trim()),
                    LocalTime.parse(end.trim()),
                    room,
                    parseInt(capacity, 30),
                    1,
                    LocalDate.now().getYear(),
                    LocalDate.now().plusWeeks(2)
            );
            notifyResult(adminService.addSection(section));
        });

        JButton assignInstructor = new JButton("Assign Instructor…");
        assignInstructor.addActionListener(e -> {
            String sectionId = JOptionPane.showInputDialog(this, "Section ID");
            String instructorId = JOptionPane.showInputDialog(this, "Instructor ID");
            if (sectionId == null || instructorId == null) {
                return;
            }
            notifyResult(adminService.assignInstructor(sectionId.trim(), instructorId.trim()));
        });

        panel.add(addCourse);
        panel.add(addSection);
        panel.add(assignInstructor);
        return panel;
    }

    private JPanel buildMaintenancePanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Maintenance"));

        JButton toggle = new JButton("Toggle Maintenance");
        toggle.addActionListener(e -> {
            boolean enable = !maintenanceService.isMaintenanceOn();
            OperationResult<Boolean> result = maintenanceService.toggle(enable);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Toggled."));
            refreshMaintenanceLabel();
        });

        panel.add(toggle);
        panel.add(new JLabel("While ON: students & instructors are read-only.", SwingConstants.CENTER));
        return panel;
    }

    private void refreshMaintenanceLabel() {
        maintenanceLabel.setText(maintenanceService.isMaintenanceOn()
                ? "Maintenance Mode ON"
                : "Maintenance Mode OFF");
    }

    private <T> void notifyResult(OperationResult<T> result) {
        JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "Success" : "Failed"));
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}

