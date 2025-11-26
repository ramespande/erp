package edu.univ.erp.ui.admin;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.MaintenanceService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class AdminDashboardFrame extends JFrame {

    private final AdminService adminService = ServiceLocator.adminService();
    private final MaintenanceService maintenanceService = ServiceLocator.maintenanceService();
    private final ErpRepository erpRepository = ServiceLocator.erpRepository();
    private final AuthService authService = ServiceLocator.authService();

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

        JButton viewCourses = new JButton("View Courses & Sections");
        viewCourses.addActionListener(e -> showCoursesAndSections());

        panel.add(addCourse);
        panel.add(addSection);
        panel.add(assignInstructor);
        panel.add(viewCourses);
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

        JButton changePassword = new JButton("Change Password");
        changePassword.addActionListener(e -> showChangePasswordDialog());

        panel.add(toggle);
        panel.add(changePassword);
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

    private void showCoursesAndSections() {
        JFrame viewFrame = new JFrame("Courses & Sections");
        viewFrame.setSize(900, 600);
        viewFrame.setLocationRelativeTo(this);

        DefaultTableModel coursesModel = new DefaultTableModel(
                new Object[]{"Course ID", "Code", "Title", "Credits"}, 0);
        DefaultTableModel sectionsModel = new DefaultTableModel(
                new Object[]{"Section ID", "Course ID", "Instructor ID", "Day", "Time", "Room", "Capacity", "Deadline"}, 0);

        for (Course course : erpRepository.listCourses()) {
            coursesModel.addRow(new Object[]{
                    course.getCourseId(),
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits()
            });
        }

        for (Section section : erpRepository.listSections()) {
            sectionsModel.addRow(new Object[]{
                    section.getSectionId(),
                    section.getCourseId(),
                    section.getInstructorId(),
                    section.getDayOfWeek().name(),
                    section.getStartTime() + "-" + section.getEndTime(),
                    section.getRoom(),
                    section.getCapacity(),
                    section.getRegistrationDeadline()
            });
        }

        JTable coursesTable = new JTable(coursesModel);
        JTable sectionsTable = new JTable(sectionsModel);

        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.add(new JScrollPane(coursesTable));
        panel.add(new JScrollPane(sectionsTable));

        viewFrame.add(panel);
        viewFrame.setVisible(true);
    }

    private void showChangePasswordDialog() {
        JPasswordField currentPasswordField = new JPasswordField(20);
        JPasswordField newPasswordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        
        Object[] message = {
            "Current Password:", currentPasswordField,
            "New Password:", newPasswordField,
            "Confirm New Password:", confirmPasswordField
        };
        
        int option = JOptionPane.showConfirmDialog(
            this,
            message,
            "Change Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (option == JOptionPane.OK_OPTION) {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "New password cannot be empty.");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }
            
            OperationResult<Void> result = authService.changePassword(currentPassword, newPassword);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "Password changed successfully." : "Failed to change password."));
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}

