package edu.univ.erp.ui.instructor;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.data.erp.ErpRepository;
import edu.univ.erp.domain.course.Course;
import edu.univ.erp.domain.course.Section;
import edu.univ.erp.domain.enrollment.Enrollment;
import edu.univ.erp.domain.grade.GradeBook;
import edu.univ.erp.domain.grade.GradeComponent;
import edu.univ.erp.domain.student.Student;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.auth.LoginFrame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

public final class InstructorDashboardFrame extends JFrame {

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
    private static final ThemePalette DARK_THEME = new ThemePalette(
            new Color(24, 29, 36),
            new Color(39, 47, 58),
            new Color(55, 65, 78),
            new Color(50, 58, 70),
            new Color(57, 66, 78),
            new Color(230, 236, 243),
            new Color(230, 236, 243),
            new Color(173, 183, 196),
            new Color(150, 160, 175),
            new Color(52, 61, 74),
            new Color(230, 236, 243),
            new Color(59, 85, 98),
            new Color(230, 236, 243),
            new Color(45, 54, 66),
            new Color(173, 183, 196),
            new Color(150, 160, 175),
            new Color(60, 70, 85),
            new Color(39, 47, 58),
            new Color(70, 80, 95)
    );

    private final InstructorService instructorService = ServiceLocator.instructorService();
    private final AuthService authService = ServiceLocator.authService();
    private final ErpRepository erpRepository = ServiceLocator.erpRepository();
    private final String instructorId = ServiceLocator.sessionContext().getUserId();

    private ThemePalette theme = LIGHT_THEME;
    private boolean darkMode;

    private final DefaultTableModel sectionsModel = new DefaultTableModel(new Object[] {
            "Section ID", "Course Code", "Day", "Time", "Room", "Capacity"
    }, 0);
    private final DefaultTableModel classStatsModel = new DefaultTableModel(new Object[] {
            "Section ID", "Course", "Enrolled", "Capacity", "Seats Left", "Fill %", "Avg Grade", "Deadline"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel gradeComponentsModel = new DefaultTableModel(new Object[] {
            "Component", "Score", "Weight"
    }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<JToggleButton> themeToggles = new ArrayList<>();

    private final JLabel maintenanceLabel = new JLabel("", SwingConstants.CENTER);

    public InstructorDashboardFrame() {
        super("Instructor Dashboard");
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
        themeToggles.clear();

        tabs.addTab("Home", buildHomePanel(tabs));
        tabs.addTab("My Sections", buildSectionsPanel(tabs));
        tabs.addTab("My Students", buildStudentsPanel(tabs));
        tabs.addTab("Class Stats", buildClassStatsPanel(tabs));
        tabs.addTab("Grade Entry", buildGradeEntryPanel(tabs));
        
        tabs.addChangeListener(e -> {
            int index = tabs.getSelectedIndex();
            if (index == 1) {
                loadSections();
            } else if (index == 3) {
                loadClassStats();
            }
        });

        if (tabs.getTabCount() > 0) {
            tabs.setSelectedIndex(Math.min(selectedIndex, tabs.getTabCount() - 1));
        }

        tabs.setOpaque(true);
        tabs.setBackground(theme.background());
        tabs.setForeground(theme.textSecondary());

        getContentPane().setBackground(theme.background());
        maintenanceLabel.setBackground(theme.background());
        maintenanceLabel.setForeground(theme.textSecondary());

        updateThemeToggleStates();
        revalidate();
        repaint();
    }

    private void setDarkMode(boolean enabled) {
        if (darkMode == enabled) {
            updateThemeToggleStates();
            return;
        }
        darkMode = enabled;
        theme = enabled ? DARK_THEME : LIGHT_THEME;
        rebuildTabs();
        refreshAll();
    }

    private void updateThemeToggleStates() {
        for (JToggleButton toggle : themeToggles) {
            toggle.setSelected(darkMode);
            styleThemeToggle(toggle);
        }
    }

    private void styleThemeToggle(JToggleButton toggle) {
        toggle.setFocusPainted(false);
        toggle.setBackground(darkMode ? BRAND_PRIMARY.darker() : theme.cardBackground());
        toggle.setForeground(theme.textPrimary());
        toggle.setBorder(BorderFactory.createLineBorder(theme.cardBorder()));
        toggle.setOpaque(true);
        toggle.setText(darkMode ? "🌙" : "☀");
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

        JLabel subtitle = new JLabel("Manage your sections and enter grades.");
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
        quickLinks.add(createPrimaryButton("My Sections", tabs, 1));
        quickLinks.add(createPrimaryButton("My Students", tabs, 2));
        quickLinks.add(createPrimaryButton("Enter Grades", tabs, 3));

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
        heroWrapper.add(createThemeBar(), BorderLayout.NORTH);
        heroWrapper.add(hero, BorderLayout.CENTER);

        panel.add(nav, BorderLayout.WEST);
        panel.add(heroWrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSectionsPanel(JTabbedPane tabs) {
        JTable table = new JTable(sectionsModel);
        styleDataTable(table);

        JScrollPane scrollPane = createTableScrollPane(table);

        JButton refresh = new JButton("Refresh");
        stylePrimaryAction(refresh);
        refresh.addActionListener(e -> loadSections());

        JButton viewGrades = new JButton("View Grades");
        styleSecondaryAction(viewGrades);
        viewGrades.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a section first.");
                return;
            }
            String sectionId = (String) sectionsModel.getValueAt(row, 0);
            showGradesForSection(sectionId);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        actions.add(refresh);
        actions.add(viewGrades);

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

        return createPageLayout(tabs, "My Sections", "View all sections you are teaching.", body);
    }

    private JPanel buildStudentsPanel(JTabbedPane tabs) {
        DefaultTableModel studentsModel = new DefaultTableModel(new Object[] {
                "Section ID", "Course Code", "Student ID", "Student Name", "Roll No", "Enrollment ID"
        }, 0);

        JTable table = new JTable(studentsModel);
        styleDataTable(table);

        JScrollPane scrollPane = createTableScrollPane(table);

        JButton refresh = new JButton("Refresh");
        stylePrimaryAction(refresh);
        refresh.addActionListener(e -> {
            studentsModel.setRowCount(0);
            var sectionsResult = instructorService.listMySections(instructorId);
            if (!sectionsResult.isSuccess()) {
                JOptionPane.showMessageDialog(this, sectionsResult.getMessage().orElse("Unable to load sections."));
                return;
            }
            for (String sectionId : sectionsResult.getPayload().orElse(List.of())) {
                erpRepository.findSection(sectionId).ifPresent(section -> {
                    erpRepository.findCourse(section.getCourseId()).ifPresent(course -> {
                        List<Enrollment> enrollments = erpRepository.findEnrollmentsBySection(sectionId);
                        for (Enrollment enrollment : enrollments) {
                            erpRepository.findStudent(enrollment.getStudentId()).ifPresent(student -> {
                                studentsModel.addRow(new Object[]{
                                        sectionId,
                                        course.getCode(),
                                        enrollment.getStudentId(),
                                        "Student " + enrollment.getStudentId(),
                                        student.getRollNumber(),
                                        enrollment.getEnrollmentId()
                                });
                            });
                        }
                    });
                });
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
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

        // Load students on panel creation
        refresh.doClick();

        return createPageLayout(tabs, "My Students", "View all students enrolled in your sections.", body);
    }

    private JPanel buildClassStatsPanel(JTabbedPane tabs) {
        JTable table = new JTable(classStatsModel);
        styleDataTable(table);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = createTableScrollPane(table);

        JButton refresh = new JButton("Refresh");
        stylePrimaryAction(refresh);
        refresh.addActionListener(e -> loadClassStats());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
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

        loadClassStats();
        return createPageLayout(tabs, "Class Stats", "Monitor fill rates and grade health across sections.", body);
    }

    private JPanel buildGradeEntryPanel(JTabbedPane tabs) {
        DefaultComboBoxModel<SectionOption> sectionOptions = new DefaultComboBoxModel<>();
        populateSectionOptions(sectionOptions);
        JComboBox<SectionOption> sectionCombo = new JComboBox<>(sectionOptions);

        DefaultComboBoxModel<EnrollmentOption> enrollmentOptions = new DefaultComboBoxModel<>();
        JComboBox<EnrollmentOption> enrollmentCombo = new JComboBox<>(enrollmentOptions);

        JPanel selectorCard = createCardPanel();
        selectorCard.setLayout(new GridBagLayout());
        selectorCard.setPreferredSize(new Dimension(260, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        selectorCard.add(new JLabel("Section"), gbc);
        gbc.gridy++;
        sectionCombo.setPreferredSize(new Dimension(220, 28));
        selectorCard.add(sectionCombo, gbc);

        gbc.gridy++;
        selectorCard.add(new JLabel("Enrollment"), gbc);
        gbc.gridy++;
        enrollmentCombo.setPreferredSize(new Dimension(220, 28));
        selectorCard.add(enrollmentCombo, gbc);

        JTable componentTable = new JTable(gradeComponentsModel);
        styleDataTable(componentTable);
        componentTable.setAutoCreateRowSorter(false);
        JScrollPane tableScrollPane = createTableScrollPane(componentTable);

        JLabel weightSummary = new JLabel("Total Weight: 0.00");
        weightSummary.setForeground(theme.textSecondary());
        JLabel previewSummary = new JLabel("Preview Final: -");
        previewSummary.setForeground(theme.textSecondary());

        JPanel summaryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        summaryBar.setOpaque(false);
        summaryBar.add(weightSummary);
        summaryBar.add(previewSummary);

        JPanel tableCard = createCardPanel();
        tableCard.add(tableScrollPane, BorderLayout.CENTER);
        tableCard.add(summaryBar, BorderLayout.SOUTH);

        JButton addButton = new JButton("Add Component");
        styleSecondaryAction(addButton);
        addButton.addActionListener(e -> {
            GradeComponent component = promptForComponent(null);
            if (component != null) {
                gradeComponentsModel.addRow(new Object[] {component.getName(), component.getScore(), component.getWeight()});
                updateComponentSummaries(weightSummary, previewSummary);
            }
        });

        JButton editButton = new JButton("Edit Selected");
        styleSecondaryAction(editButton);
        editButton.addActionListener(e -> {
            int row = componentTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a component to edit.");
                return;
            }
            GradeComponent existing = new GradeComponent(
                    gradeComponentsModel.getValueAt(row, 0).toString(),
                    Double.parseDouble(gradeComponentsModel.getValueAt(row, 1).toString()),
                    Double.parseDouble(gradeComponentsModel.getValueAt(row, 2).toString())
            );
            GradeComponent updated = promptForComponent(existing);
            if (updated != null) {
                gradeComponentsModel.setValueAt(updated.getName(), row, 0);
                gradeComponentsModel.setValueAt(updated.getScore(), row, 1);
                gradeComponentsModel.setValueAt(updated.getWeight(), row, 2);
                updateComponentSummaries(weightSummary, previewSummary);
            }
        });

        JButton deleteButton = new JButton("Delete Selected");
        styleGhostButton(deleteButton);
        deleteButton.addActionListener(e -> {
            int row = componentTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a component to delete.");
                return;
            }
            gradeComponentsModel.removeRow(row);
            updateComponentSummaries(weightSummary, previewSummary);
        });

        JButton saveButton = new JButton("Save Gradebook");
        stylePrimaryAction(saveButton);
        saveButton.addActionListener(e -> {
            SectionOption section = (SectionOption) sectionCombo.getSelectedItem();
            EnrollmentOption enrollment = (EnrollmentOption) enrollmentCombo.getSelectedItem();
            if (section == null || enrollment == null) {
                JOptionPane.showMessageDialog(this, "Select both section and enrollment.");
                return;
            }
            List<GradeComponent> components = collectComponentsFromTable();
            var result = instructorService.saveGradeComponents(instructorId, section.id(), enrollment.id(), components);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse(result.isSuccess() ? "Gradebook saved." : "Failed to save gradebook."));
            if (result.isSuccess()) {
                loadGradeComponents(section.id(), enrollment.id(), weightSummary, previewSummary);
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(saveButton);

        sectionCombo.addActionListener(e -> {
            SectionOption selected = (SectionOption) sectionCombo.getSelectedItem();
            populateEnrollmentOptions(selected == null ? null : selected.id(), enrollmentOptions);
            EnrollmentOption enrollment = enrollmentOptions.getSize() > 0 ? enrollmentOptions.getElementAt(0) : null;
            if (selected != null && enrollment != null) {
                enrollmentCombo.setSelectedItem(enrollment);
                loadGradeComponents(selected.id(), enrollment.id(), weightSummary, previewSummary);
            } else {
                clearGradeComponents(weightSummary, previewSummary);
            }
        });

        enrollmentCombo.addActionListener(e -> {
            SectionOption section = (SectionOption) sectionCombo.getSelectedItem();
            EnrollmentOption enrollment = (EnrollmentOption) enrollmentCombo.getSelectedItem();
            if (section != null && enrollment != null) {
                loadGradeComponents(section.id(), enrollment.id(), weightSummary, previewSummary);
            }
        });

        SectionOption initialSection = sectionOptions.getSize() > 0 ? sectionOptions.getElementAt(0) : null;
        if (initialSection != null) {
            populateEnrollmentOptions(initialSection.id(), enrollmentOptions);
            if (enrollmentOptions.getSize() > 0) {
                enrollmentCombo.setSelectedIndex(0);
                loadGradeComponents(initialSection.id(), enrollmentOptions.getElementAt(0).id(), weightSummary, previewSummary);
            }
        }

        JPanel wrapper = new JPanel(new BorderLayout(16, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(selectorCard, BorderLayout.WEST);
        wrapper.add(tableCard, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(wrapper, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        return createPageLayout(tabs, "Grade Entry", "Design components and ensure weights sum to 1.0 before saving.", body);
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
        headerWrapper.add(createThemeBar(), BorderLayout.NORTH);
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
        nav.add(createNavLink("Sections", tabs, 1));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createNavLink("Students", tabs, 2));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createNavLink("Class Stats", tabs, 3));
        nav.add(Box.createVerticalStrut(8));
        nav.add(createNavLink("Grade Entry", tabs, 4));
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JPanel createThemeBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bar.setOpaque(true);
        bar.setBackground(theme.chromeBackground());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.chromeBorder()),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        JLabel sun = new JLabel("☀");
        sun.setForeground(theme.textSecondary());

        JToggleButton toggle = new JToggleButton();
        toggle.setPreferredSize(new Dimension(56, 24));
        toggle.addActionListener(e -> setDarkMode(toggle.isSelected()));
        themeToggles.add(toggle);
        toggle.setSelected(darkMode);
        styleThemeToggle(toggle);

        JLabel moon = new JLabel("🌙");
        moon.setForeground(theme.textSecondary());

        bar.add(sun);
        bar.add(toggle);
        bar.add(moon);
        return bar;
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

    private void styleGhostButton(JButton button) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(theme.textSecondary());
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
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
        var url = InstructorDashboardFrame.class.getResource("/images/iiitdlogo.png");
        if (url == null) {
            return null;
        }
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
    }

    private void loadSections() {
        sectionsModel.setRowCount(0);
        var result = instructorService.listMySections(instructorId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to load sections."));
            return;
        }
        for (String sectionId : result.getPayload().orElse(List.of())) {
            erpRepository.findSection(sectionId).ifPresent(section -> {
                erpRepository.findCourse(section.getCourseId()).ifPresent(course -> {
                    sectionsModel.addRow(new Object[]{
                            section.getSectionId(),
                            course.getCode(),
                            section.getDayOfWeek().name(),
                            section.getStartTime() + "-" + section.getEndTime(),
                            section.getRoom(),
                            section.getCapacity()
                    });
                });
            });
        }
    }

    private void loadClassStats() {
        classStatsModel.setRowCount(0);
        var result = instructorService.listMySections(instructorId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to load stats."));
            return;
        }
        for (String sectionId : result.getPayload().orElse(List.of())) {
            erpRepository.findSection(sectionId).ifPresent(section -> {
                int enrolled = erpRepository.findEnrollmentsBySection(sectionId).size();
                int capacity = section.getCapacity();
                int seatsLeft = Math.max(0, capacity - enrolled);
                double fill = capacity == 0 ? 0 : (double) enrolled / capacity;
                double avg = calculateAverageFinal(sectionId);
                String courseCode = erpRepository.findCourse(section.getCourseId())
                        .map(Course::getCode)
                        .orElse("Unknown");
                classStatsModel.addRow(new Object[] {
                        sectionId,
                        courseCode,
                        enrolled,
                        capacity,
                        seatsLeft,
                        String.format("%.0f%%", fill * 100),
                        avg < 0 ? "-" : String.format("%.1f", avg),
                        section.getRegistrationDeadline()
                });
            });
        }
    }

    private void populateSectionOptions(DefaultComboBoxModel<SectionOption> model) {
        model.removeAllElements();
        var sections = instructorService.listMySections(instructorId);
        if (!sections.isSuccess()) {
            JOptionPane.showMessageDialog(this, sections.getMessage().orElse("Unable to load sections."));
            return;
        }
        for (String sectionId : sections.getPayload().orElse(List.of())) {
            String label = erpRepository.findSection(sectionId)
                    .flatMap(section -> erpRepository.findCourse(section.getCourseId())
                            .map(course -> course.getCode() + " • " + sectionId))
                    .orElse(sectionId);
            model.addElement(new SectionOption(sectionId, label));
        }
    }

    private void populateEnrollmentOptions(String sectionId, DefaultComboBoxModel<EnrollmentOption> model) {
        model.removeAllElements();
        if (sectionId == null) {
            return;
        }
        for (Enrollment enrollment : erpRepository.findEnrollmentsBySection(sectionId)) {
            String label = erpRepository.findStudent(enrollment.getStudentId())
                    .map(student -> student.getRollNumber() + " • " + enrollment.getEnrollmentId())
                    .orElse("Enrollment " + enrollment.getEnrollmentId());
            model.addElement(new EnrollmentOption(enrollment.getEnrollmentId(), label));
        }
    }

    private void loadGradeComponents(String sectionId,
                                     String enrollmentId,
                                     JLabel weightLabel,
                                     JLabel previewLabel) {
        gradeComponentsModel.setRowCount(0);
        if (sectionId == null || enrollmentId == null) {
            updateComponentSummaries(weightLabel, previewLabel);
            return;
        }
        var result = instructorService.listGradeComponents(instructorId, sectionId, enrollmentId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to load gradebook."));
            return;
        }
        for (GradeComponent component : result.getPayload().orElse(List.of())) {
            gradeComponentsModel.addRow(new Object[] {
                    component.getName(),
                    component.getScore(),
                    component.getWeight()
            });
        }
        updateComponentSummaries(weightLabel, previewLabel);
    }

    private void clearGradeComponents(JLabel weightLabel, JLabel previewLabel) {
        gradeComponentsModel.setRowCount(0);
        updateComponentSummaries(weightLabel, previewLabel);
    }

    private void updateComponentSummaries(JLabel weightLabel, JLabel previewLabel) {
        double totalWeight = 0;
        double previewGrade = 0;
        for (int i = 0; i < gradeComponentsModel.getRowCount(); i++) {
            double score = Double.parseDouble(gradeComponentsModel.getValueAt(i, 1).toString());
            double weight = Double.parseDouble(gradeComponentsModel.getValueAt(i, 2).toString());
            totalWeight += weight;
            previewGrade += score * weight;
        }
        weightLabel.setText(String.format("Total Weight: %.2f", totalWeight));
        previewLabel.setText(gradeComponentsModel.getRowCount() == 0
                ? "Preview Final: -"
                : String.format("Preview Final: %.1f", previewGrade));
    }

    private List<GradeComponent> collectComponentsFromTable() {
        List<GradeComponent> components = new ArrayList<>();
        for (int i = 0; i < gradeComponentsModel.getRowCount(); i++) {
            components.add(new GradeComponent(
                    gradeComponentsModel.getValueAt(i, 0).toString(),
                    Double.parseDouble(gradeComponentsModel.getValueAt(i, 1).toString()),
                    Double.parseDouble(gradeComponentsModel.getValueAt(i, 2).toString())
            ));
        }
        return components;
    }

    private GradeComponent promptForComponent(GradeComponent existing) {
        JTextField nameField = new JTextField(existing == null ? "" : existing.getName(), 20);
        JTextField scoreField = new JTextField(existing == null ? "" : String.valueOf(existing.getScore()), 10);
        JTextField weightField = new JTextField(existing == null ? "" : String.valueOf(existing.getWeight()), 10);
        Object[] message = {
                "Name:", nameField,
                "Score (0-100):", scoreField,
                "Weight (0-1):", weightField
        };
        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                existing == null ? "Add Component" : "Edit Component",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            String name = nameField.getText().trim();
            double score = Double.parseDouble(scoreField.getText().trim());
            double weight = Double.parseDouble(weightField.getText().trim());
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.");
                return null;
            }
            if (score < 0 || score > 100) {
                JOptionPane.showMessageDialog(this, "Score must be between 0 and 100.");
                return null;
            }
            if (weight <= 0 || weight > 1) {
                JOptionPane.showMessageDialog(this, "Weight must be greater than 0 and at most 1.");
                return null;
            }
            return new GradeComponent(name, score, weight);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.");
            return null;
        }
    }

    private void showGradesForSection(String sectionId) {
        var result = instructorService.viewGradesForSection(instructorId, sectionId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to fetch grades."));
            return;
        }
        GradeView view = result.getPayload().orElse(null);
        if (view == null) {
            JOptionPane.showMessageDialog(this, "No data.");
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Course: ").append(view.courseCode())
                .append(" | Section: ").append(view.sectionId()).append("\n\n");
        for (GradeView.ComponentScore component : view.components()) {
            builder.append(" - ")
                    .append(component.name())
                    .append(": ")
                    .append(component.score())
                    .append(" (w=")
                    .append(component.weight())
                    .append(")")
                    .append("\n");
        }
        builder.append("\nFinal Grade: ").append(view.finalGrade() == null ? "-" : view.finalGrade());
        JOptionPane.showMessageDialog(this, builder.toString(), "Grades", JOptionPane.INFORMATION_MESSAGE);
    }

    private double calculateAverageFinal(String sectionId) {
        List<Enrollment> enrollments = erpRepository.findEnrollmentsBySection(sectionId);
        double sum = 0;
        int counted = 0;
        for (Enrollment enrollment : enrollments) {
            var grade = erpRepository.findGradeBook(enrollment.getEnrollmentId())
                    .flatMap(GradeBook::getFinalGrade)
                    .orElse(null);
            if (grade != null) {
                sum += grade;
                counted++;
            }
        }
        return counted == 0 ? -1 : sum / counted;
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

    private void refreshAll() {
        loadSections();
        var maintenance = ServiceLocator.maintenanceService().isMaintenanceOn();
        maintenanceLabel.setText(maintenance ? "Maintenance Mode ON — read-only operations." : "");
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

    private record SectionOption(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record EnrollmentOption(String id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
