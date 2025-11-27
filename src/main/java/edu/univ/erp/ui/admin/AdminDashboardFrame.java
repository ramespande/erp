package edu.univ.erp.ui.admin;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.data.auth.AuthRecord;
import edu.univ.erp.data.auth.AuthRepository;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.instructor.Instructor;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.infra.DemoCatalogSeeder;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.MaintenanceService;
import edu.univ.erp.service.support.CourseSectionBackupService;
import edu.univ.erp.ui.auth.LoginFrame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AdminDashboardFrame extends JFrame {

    private static final Color BRAND_PRIMARY = new Color(0, 158, 149);
    private static final ThemePalette LIGHT_THEME = new ThemePalette(
            new Color(247, 249, 250),
            new Color(255, 255, 255),
            new Color(219, 226, 232),
            new Color(0, 158, 149, 30),
            new Color(255, 255, 255, 220),
            new Color(30, 50, 70),
            new Color(20, 40, 60),
            new Color(90, 100, 110),
            new Color(60, 70, 80),
            new Color(239, 244, 247),
            new Color(40, 55, 70),
            new Color(227, 245, 242),
            Color.DARK_GRAY,
            new Color(245, 248, 249),
            new Color(90, 100, 110),
            new Color(110, 120, 130),
            new Color(229, 236, 241),
            new Color(255, 255, 255, 200),
            new Color(220, 225, 230)
    );
    private final AdminService adminService = ServiceLocator.adminService();
    private final MaintenanceService maintenanceService = ServiceLocator.maintenanceService();
    private final ErpRepository erpRepository = ServiceLocator.erpRepository();
    private final AuthService authService = ServiceLocator.authService();
    private final AuthRepository authRepository = ServiceLocator.authRepository();
    private final CourseSectionBackupService backupService = new CourseSectionBackupService(erpRepository);

    private final ThemePalette theme = LIGHT_THEME;

    private final DefaultTableModel coursesModel = new DefaultTableModel(
            new Object[]{"Course ID", "Code", "Title", "Credits"}, 0);
    private final DefaultTableModel sectionsModel = new DefaultTableModel(
            new Object[]{"Section ID", "Course Code", "Instructor ID", "Day", "Time", "Room", "Capacity", "Deadline"}, 0);
    private final DefaultTableModel usersModel = new DefaultTableModel(
            new Object[]{"User ID", "Username", "Role", "Active", "Last Login"}, 0);
    private final JTabbedPane tabs = new JTabbedPane();

    private final JLabel maintenanceLabel = new JLabel("", SwingConstants.CENTER);

    public AdminDashboardFrame() {
        super("Admin Dashboard");
        setSize(960, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        rebuildTabs();

        add(tabs, BorderLayout.CENTER);
        add(maintenanceLabel, BorderLayout.SOUTH);

        refreshAll();
    }

    private void rebuildTabs() {
        int selectedIndex = tabs.getTabCount() > 0 ? tabs.getSelectedIndex() : 0;
        tabs.removeAll();

        tabs.addTab("Home", buildHomePanel(tabs));
        tabs.addTab("Users", buildUsersPanel(tabs));
        tabs.addTab("Courses & Sections", buildCoursesSectionsPanel(tabs));
        tabs.addTab("Manage", buildManagePanel(tabs));

        if (tabs.getTabCount() > 0) {
            tabs.setSelectedIndex(Math.min(selectedIndex, tabs.getTabCount() - 1));
        }

        tabs.setOpaque(true);
        tabs.setBackground(theme.background());
        tabs.setForeground(theme.textSecondary());

        getContentPane().setBackground(theme.background());
        maintenanceLabel.setBackground(theme.background());
        maintenanceLabel.setForeground(theme.textSecondary());
        revalidate();
        repaint();
    }

    private JPanel buildHomePanel(JTabbedPane tabs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(theme.background());

        JPanel nav = createNavigationColumn(tabs);
        JPanel hero = new JPanel();
        hero.setOpaque(false);
        hero.setLayout(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + ServiceLocator.sessionContext().getUsername());
        welcome.setForeground(theme.textPrimary());
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("Manage users, courses, and system settings.");
        subtitle.setForeground(theme.subtitleText());
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));

        JPanel welcomeBlock = new JPanel();
        welcomeBlock.setOpaque(false);
        welcomeBlock.setLayout(new BoxLayout(welcomeBlock, BoxLayout.Y_AXIS));
        welcomeBlock.add(welcome);
        welcomeBlock.add(Box.createVerticalStrut(6));
        welcomeBlock.add(subtitle);

        JLabel logo = new JLabel(loadLogoIcon(140, 90));
        logo.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(welcomeBlock, BorderLayout.WEST);
        header.add(logo, BorderLayout.EAST);

        JPanel quickLinks = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        quickLinks.setOpaque(false);
        quickLinks.add(createPrimaryButton("View Users", tabs, 1));
        quickLinks.add(createPrimaryButton("View Courses", tabs, 2));
        quickLinks.add(createPrimaryButton("Manage", tabs, 3));

        JButton logoutButton = new JButton("Logout");
        styleSecondaryAction(logoutButton);
        logoutButton.addActionListener(e -> logout());

        JButton changePasswordButton = new JButton("Change Password");
        styleSecondaryAction(changePasswordButton);
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());

        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        settingsPanel.setOpaque(false);
        settingsPanel.add(changePasswordButton);
        settingsPanel.add(logoutButton);

        hero.add(header, BorderLayout.NORTH);
        hero.add(quickLinks, BorderLayout.CENTER);
        hero.add(settingsPanel, BorderLayout.SOUTH);

        JPanel heroWrapper = new JPanel(new BorderLayout());
        heroWrapper.setOpaque(false);
        heroWrapper.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        heroWrapper.add(hero, BorderLayout.CENTER);

        panel.add(nav, BorderLayout.WEST);
        panel.add(heroWrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUsersPanel(JTabbedPane tabs) {
        JTable table = new JTable(usersModel);
        styleDataTable(table);

        JScrollPane scrollPane = createTableScrollPane(table);

        JButton refresh = new JButton("Refresh");
        stylePrimaryAction(refresh);
        refresh.addActionListener(e -> loadUsers());

        JButton lockButton = new JButton("Temp Lock 10m");
        styleSecondaryAction(lockButton);
        lockButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user first.");
                return;
            }
            String username = usersModel.getValueAt(row, 1).toString();
            var result = adminService.temporaryLockUser(username, 10);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "User locked." : "Failed to lock user."));
            loadUsers();
        });

        JButton unlockButton = new JButton("Unlock User");
        styleSecondaryAction(unlockButton);
        unlockButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a user first.");
                return;
            }
            String username = usersModel.getValueAt(row, 1).toString();
            var result = adminService.unlockUser(username);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "User unlocked." : "Failed to unlock user."));
            loadUsers();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        actions.add(lockButton);
        actions.add(unlockButton);
        actions.add(refresh);

        JPanel tableCard = createCardPanel();
        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.add(actions, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(tableCard, BorderLayout.CENTER);
        body.add(wrapper, BorderLayout.CENTER);

        return createPageLayout(tabs, "All Users", "View all registered users in the system.", body);
    }

    private JPanel buildCoursesSectionsPanel(JTabbedPane tabs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(theme.background());

        JPanel nav = createNavigationColumn(tabs);
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Courses & Sections");
        titleLabel.setForeground(theme.textPrimary());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));

        JLabel subtitleLabel = new JLabel("View all courses and sections in the system.");
        subtitleLabel.setForeground(theme.subtitleText());
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        header.add(titleLabel);
        header.add(subtitleLabel);

        JPanel tablesPanel = new JPanel(new BorderLayout());
        tablesPanel.setOpaque(false);

        JTable coursesTable = new JTable(coursesModel);
        styleDataTable(coursesTable);
        JTable sectionsTable = new JTable(sectionsModel);
        styleDataTable(sectionsTable);

        JPanel coursesCard = createCardPanel();
        coursesCard.add(new JLabel("Courses", SwingConstants.CENTER), BorderLayout.NORTH);
        coursesCard.add(new JScrollPane(coursesTable), BorderLayout.CENTER);

        JPanel sectionsCard = createCardPanel();
        sectionsCard.add(new JLabel("Sections", SwingConstants.CENTER), BorderLayout.NORTH);
        sectionsCard.add(new JScrollPane(sectionsTable), BorderLayout.CENTER);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(coursesCard);
        stack.add(Box.createVerticalStrut(16));
        stack.add(sectionsCard);

        JScrollPane cardsPanel = new JScrollPane(stack);
        cardsPanel.setBorder(BorderFactory.createEmptyBorder());
        cardsPanel.getVerticalScrollBar().setUnitIncrement(16);
        cardsPanel.getViewport().setBackground(theme.cardBackground());

        JButton refresh = new JButton("Refresh");
        stylePrimaryAction(refresh);
        refresh.addActionListener(e -> loadCoursesAndSections());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(refresh);

        tablesPanel.add(cardsPanel, BorderLayout.CENTER);
        tablesPanel.add(actions, BorderLayout.SOUTH);

        content.add(header, BorderLayout.NORTH);
        content.add(tablesPanel, BorderLayout.CENTER);

        panel.add(nav, BorderLayout.WEST);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildManagePanel(JTabbedPane tabs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(theme.background());

        JPanel nav = createNavigationColumn(tabs);
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Manage System");
        titleLabel.setForeground(theme.textPrimary());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));

        JLabel subtitleLabel = new JLabel("Add users, courses, sections, and manage maintenance mode.");
        subtitleLabel.setForeground(theme.subtitleText());
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        header.add(titleLabel);
        header.add(subtitleLabel);

        JPanel formCard = createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Add User Form
        gbc.gridx = 0; gbc.gridy = 0;
        formCard.add(new JLabel("Add User", SwingConstants.LEFT), gbc);
        gbc.gridy++;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField roleField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 1;
        formCard.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formCard.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formCard.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formCard.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formCard.add(new JLabel("Role (ADMIN/INSTRUCTOR/STUDENT):"), gbc);
        gbc.gridx = 1;
        formCard.add(roleField, gbc);

        JButton addUserButton = new JButton("Add User");
        stylePrimaryAction(addUserButton);
        addUserButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = roleField.getText().trim();
            if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }
            var result = adminService.addUser(username, password, role);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "User added." : "Failed."));
            if (result.isSuccess()) {
                usernameField.setText("");
                passwordField.setText("");
                roleField.setText("");
                loadUsers();
            }
        });

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formCard.add(addUserButton, gbc);

        // Add Course Form
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formCard.add(new JLabel("Add Course", SwingConstants.LEFT), gbc);
        gbc.gridy++;

        JTextField courseIdField = new JTextField(20);
        JTextField codeField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextField creditsField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 6;
        formCard.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1;
        formCard.add(courseIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        formCard.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1;
        formCard.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        formCard.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        formCard.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 9;
        formCard.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        formCard.add(creditsField, gbc);

        JButton addCourseButton = new JButton("Add Course");
        stylePrimaryAction(addCourseButton);
        addCourseButton.addActionListener(e -> {
            String courseId = courseIdField.getText().trim();
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String credits = creditsField.getText().trim();
            if (courseId.isEmpty() || code.isEmpty() || title.isEmpty() || credits.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }
            try {
                int creditsValue = parseInt(credits, 4);
                if (creditsValue <= 0 || creditsValue > 6) {
                    JOptionPane.showMessageDialog(this, "Credits must be between 1 and 6.");
                    return;
                }
                Course course = new Course(courseId, code, title, creditsValue);
                var result = adminService.addCourse(course);
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Course added successfully. Students and instructors may need to refresh their views.");
                    courseIdField.setText("");
                    codeField.setText("");
                    titleField.setText("");
                    creditsField.setText("");
                    loadCoursesAndSections();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add course: " + result.getMessage().orElse("Unknown error."), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid credits value. Please enter a number between 1 and 6.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formCard.add(addCourseButton, gbc);

        // Add Section Form
        gbc.gridx = 0; gbc.gridy = 11;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formCard.add(new JLabel("Add Section", SwingConstants.LEFT), gbc);
        gbc.gridy++;

        JTextField sectionIdField = new JTextField(20);
        JTextField sectionCourseIdField = new JTextField(20);
        JTextField instructorIdField = new JTextField(20);
        JTextField dayField = new JTextField(20);
        JTextField startTimeField = new JTextField(20);
        JTextField endTimeField = new JTextField(20);
        JTextField roomField = new JTextField(20);
        JTextField capacityField = new JTextField(20);
        JTextField semesterField = new JTextField(20);
        JTextField yearField = new JTextField(20);
        JTextField deadlineField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 12;
        formCard.add(new JLabel("Section ID:"), gbc);
        gbc.gridx = 1;
        formCard.add(sectionIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 13;
        formCard.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1;
        formCard.add(sectionCourseIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 14;
        formCard.add(new JLabel("Instructor ID:"), gbc);
        gbc.gridx = 1;
        formCard.add(instructorIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 15;
        formCard.add(new JLabel("Day (MONDAY/TUESDAY/etc):"), gbc);
        gbc.gridx = 1;
        formCard.add(dayField, gbc);

        gbc.gridx = 0; gbc.gridy = 16;
        formCard.add(new JLabel("Start Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        formCard.add(startTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 17;
        formCard.add(new JLabel("End Time (HH:mm):"), gbc);
        gbc.gridx = 1;
        formCard.add(endTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 18;
        formCard.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        formCard.add(roomField, gbc);

        gbc.gridx = 0; gbc.gridy = 19;
        formCard.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        formCard.add(capacityField, gbc);

        gbc.gridx = 0; gbc.gridy = 20;
        formCard.add(new JLabel("Semester (1 or 2):"), gbc);
        gbc.gridx = 1;
        formCard.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 21;
        formCard.add(new JLabel("Academic Year:"), gbc);
        gbc.gridx = 1;
        formCard.add(yearField, gbc);

        gbc.gridx = 0; gbc.gridy = 22;
        formCard.add(new JLabel("Registration Deadline (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        formCard.add(deadlineField, gbc);

        JButton addSectionButton = new JButton("Add Section");
        stylePrimaryAction(addSectionButton);
        addSectionButton.addActionListener(e -> {
            try {
                String sectionId = sectionIdField.getText().trim();
                String courseId = sectionCourseIdField.getText().trim();
                String instructorId = instructorIdField.getText().trim();
                String day = dayField.getText().trim();
                String start = startTimeField.getText().trim();
                String end = endTimeField.getText().trim();
                String room = roomField.getText().trim();
                String capacity = capacityField.getText().trim();
                String semester = semesterField.getText().trim();
                String year = yearField.getText().trim();
                String deadline = deadlineField.getText().trim();
                if (sectionId.isEmpty() || courseId.isEmpty() || day.isEmpty() || start.isEmpty() || end.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill required fields (Section ID, Course ID, Day, Start Time, End Time).");
                    return;
                }
                if (instructorId.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Instructor ID is required. Please enter a valid instructor user ID (e.g., USR-INST-001).");
                    return;
                }
                LocalDate deadlineDate;
                if (deadline.isEmpty()) {
                    deadlineDate = LocalDate.now().plusWeeks(2);
                } else {
                    deadlineDate = LocalDate.parse(deadline);
                }
                int capacityValue = parseInt(capacity, 30);
                if (capacityValue <= 0) {
                    JOptionPane.showMessageDialog(this, "Capacity must be greater than 0.");
                    return;
                }
                LocalTime startTime = LocalTime.parse(start);
                LocalTime endTime = LocalTime.parse(end);
                if (!startTime.isBefore(endTime)) {
                    JOptionPane.showMessageDialog(this, "Start time must be before end time.");
                    return;
                }
                Section section = new Section(
                        sectionId,
                        courseId,
                        instructorId,
                        DayOfWeek.valueOf(day.toUpperCase()),
                        startTime,
                        endTime,
                        room,
                        capacityValue,
                        semester.isEmpty() ? 1 : parseInt(semester, 1),
                        year.isEmpty() ? LocalDate.now().getYear() : parseInt(year, LocalDate.now().getYear()),
                        deadlineDate
                );
                var result = adminService.addSection(section);
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Section added successfully. Students and instructors may need to refresh their views.");
                    sectionIdField.setText("");
                    sectionCourseIdField.setText("");
                    instructorIdField.setText("");
                    dayField.setText("");
                    startTimeField.setText("");
                    endTimeField.setText("");
                    roomField.setText("");
                    capacityField.setText("");
                    semesterField.setText("");
                    yearField.setText("");
                    deadlineField.setText("");
                    loadCoursesAndSections();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add section: " + result.getMessage().orElse("Unknown error."), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage() + "\n\nPlease enter valid numbers for capacity, semester, and year.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date/time format: " + ex.getMessage() + "\n\nTime format: HH:mm (e.g., 09:00)\nDate format: YYYY-MM-DD (e.g., 2024-12-31)", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage() + "\n\nDay must be one of: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY\nTime format must be HH:mm (e.g., 09:00)", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding section: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 23;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formCard.add(addSectionButton, gbc);

        // Maintenance Toggle
        gbc.gridx = 0; gbc.gridy = 24;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formCard.add(new JLabel("Maintenance Mode", SwingConstants.LEFT), gbc);
        gbc.gridy++;

        JButton toggleButton = new JButton(maintenanceService.isMaintenanceOn() ? "Turn OFF" : "Turn ON");
        stylePrimaryAction(toggleButton);
        toggleButton.addActionListener(e -> {
            boolean enable = !maintenanceService.isMaintenanceOn();
            OperationResult<Boolean> result = maintenanceService.toggle(enable);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Toggled."));
            toggleButton.setText(enable ? "Turn OFF" : "Turn ON");
            refreshMaintenanceLabel();
        });

        gbc.gridx = 0; gbc.gridy = 25;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formCard.add(toggleButton, gbc);

        gbc.gridx = 0; gbc.gridy = 26;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formCard.add(new JLabel("Data Utilities", SwingConstants.LEFT), gbc);
        gbc.gridy++;

        JButton seedButton = new JButton("Seed 100 Demo Courses");
        styleSecondaryAction(seedButton);
        seedButton.addActionListener(e -> seedDemoCourses());
        gbc.gridx = 0; gbc.gridy = 27;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formCard.add(seedButton, gbc);

        JButton backupButton = new JButton("Backup Courses & Sections");
        stylePrimaryAction(backupButton);
        backupButton.addActionListener(e -> performBackup());
        gbc.gridy = 28;
        formCard.add(backupButton, gbc);

        JButton restoreButton = new JButton("Restore Backup");
        styleSecondaryAction(restoreButton);
        restoreButton.addActionListener(e -> performRestore());
        gbc.gridy = 29;
        formCard.add(restoreButton, gbc);

        content.add(createThemeBar(), BorderLayout.NORTH);
        content.add(header, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(formCard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(theme.cardBackground());
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(nav, BorderLayout.WEST);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPageLayout(JTabbedPane tabs, String title, String subtitle, JComponent body) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        panel.setBackground(theme.background());

        JPanel nav = createNavigationColumn(tabs);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(theme.textPrimary());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 26f));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(theme.subtitleText());
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        header.add(titleLabel);
        header.add(subtitleLabel);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.add(header, BorderLayout.CENTER);

        content.add(headerWrapper, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);

        panel.add(nav, BorderLayout.WEST);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNavigationColumn(JTabbedPane tabs) {
        JPanel nav = new JPanel();
        nav.setBackground(theme.navBackground());
        nav.setPreferredSize(new Dimension(200, 0));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        JLabel label = new JLabel("Navigate");
        label.setForeground(theme.textPrimary());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        label.setAlignmentX(0f);

        nav.add(label);
        nav.add(Box.createVerticalStrut(16));
        nav.add(createNavLink("Home", tabs, 0));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createNavLink("Users", tabs, 1));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createNavLink("Courses", tabs, 2));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createNavLink("Manage", tabs, 3));
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(theme.cardBackground());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.cardBorder()),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(theme.cardBackground());
        return scrollPane;
    }

    private void styleDataTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(true);
        table.setSelectionBackground(theme.tableSelectionBackground());
        table.setSelectionForeground(theme.tableSelectionForeground());
        table.setBackground(theme.cardBackground());
        table.setForeground(theme.textPrimary());
        var header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(theme.tableHeaderBackground());
        header.setForeground(theme.tableHeaderText());
        header.setOpaque(true);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 13f));
    }

    private void stylePrimaryAction(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(BRAND_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    private void styleSecondaryAction(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(theme.cardBackground());
        button.setForeground(theme.textPrimary());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.cardBorder()),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
    }

    private JButton createNavLink(String text, JTabbedPane tabs, int tabIndex) {
        JButton button = new JButton(text);
        button.setAlignmentX(0f);
        button.setFocusPainted(false);
        button.setBackground(theme.navButtonBackground());
        button.setForeground(theme.navButtonForeground());
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setOpaque(true);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addActionListener(e -> tabs.setSelectedIndex(tabIndex));
        return button;
    }

    private JButton createPrimaryButton(String text, JTabbedPane tabs, int tabIndex) {
        JButton button = new JButton(text);
        stylePrimaryAction(button);
        button.addActionListener(e -> tabs.setSelectedIndex(tabIndex));
        return button;
    }

    private ImageIcon loadLogoIcon(int width, int height) {
        var url = AdminDashboardFrame.class.getResource("/images/iiitdlogo.png");
        if (url == null) {
            return null;
        }
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
    }

    private void loadUsers() {
        usersModel.setRowCount(0);
        for (AuthRecord record : authRepository.findAll()) {
            usersModel.addRow(new Object[]{
                    record.userId(),
                    record.username(),
                    record.role().name(),
                    record.active() ? "Yes" : "No",
                    record.lastLogin() != null ? record.lastLogin().toString() : "Never"
            });
        }
    }

    private void loadCoursesAndSections() {
        coursesModel.setRowCount(0);
        sectionsModel.setRowCount(0);

        for (Course course : erpRepository.listCourses()) {
            coursesModel.addRow(new Object[]{
                    course.getCourseId(),
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits()
            });
        }

        for (Section section : erpRepository.listSections()) {
            String courseCode = erpRepository.findCourse(section.getCourseId())
                    .map(Course::getCode)
                    .orElse("Unknown");
            sectionsModel.addRow(new Object[]{
                    section.getSectionId(),
                    courseCode,
                    section.getInstructorId(),
                    section.getDayOfWeek().name(),
                    section.getStartTime() + "-" + section.getEndTime(),
                    section.getRoom(),
                    section.getCapacity(),
                    section.getRegistrationDeadline()
            });
        }
    }

    private void seedDemoCourses() {
        DemoCatalogSeeder.ensureBaselineCatalog(erpRepository);
        loadCoursesAndSections();
        JOptionPane.showMessageDialog(this, "Demo catalog ensured. Reload views if needed.");
    }

    private void performBackup() {
        var result = backupService.exportSnapshot();
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to create backup."));
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("erp-backup.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                writer.write(result.getPayload().orElse(""));
                JOptionPane.showMessageDialog(this, "Backup saved.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save backup: " + ex.getMessage());
            }
        }
    }

    private void performRestore() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String payload = Files.readString(chooser.getSelectedFile().toPath());
                var result = backupService.importSnapshot(payload);
                JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "Backup restored." : "Failed to restore backup."));
                if (result.isSuccess()) {
                    loadCoursesAndSections();
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to read backup: " + ex.getMessage());
            }
        }
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

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void refreshMaintenanceLabel() {
        maintenanceLabel.setText(maintenanceService.isMaintenanceOn()
                ? "Maintenance Mode ON — read-only operations."
                : "");
    }

    private void refreshAll() {
        loadUsers();
        loadCoursesAndSections();
        refreshMaintenanceLabel();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private record ThemePalette(
            Color background,
            Color cardBackground,
            Color cardBorder,
            Color navBackground,
            Color navButtonBackground,
            Color navButtonForeground,
            Color textPrimary,
            Color textSecondary,
            Color subtitleText,
            Color tableHeaderBackground,
            Color tableHeaderText,
            Color tableSelectionBackground,
            Color tableSelectionForeground,
            Color infoBackground,
            Color infoText,
            Color placeholderText,
            Color gridLine,
            Color chromeBackground,
            Color chromeBorder
    ) {
    }
}
