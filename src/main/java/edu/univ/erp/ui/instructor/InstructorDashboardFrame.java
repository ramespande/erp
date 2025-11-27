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
import javax.swing.JTextArea;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final InstructorService instructorService = ServiceLocator.instructorService();
    private final AuthService authService = ServiceLocator.authService();
    private final ErpRepository erpRepository = ServiceLocator.erpRepository();
    private final String instructorId = ServiceLocator.sessionContext().getUserId();

    private final ThemePalette theme = LIGHT_THEME;

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

        sectionCombo.setPreferredSize(new Dimension(220, 28));
        enrollmentCombo.setPreferredSize(new Dimension(220, 28));

        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        selectorRow.setOpaque(false);
        JLabel sectionLabel = new JLabel("Section:");
        sectionLabel.setForeground(theme.textPrimary());
        selectorRow.add(sectionLabel);
        selectorRow.add(sectionCombo);
        selectorRow.add(Box.createHorizontalStrut(12));
        JLabel enrollmentLabel = new JLabel("Student:");
        enrollmentLabel.setForeground(theme.textPrimary());
        selectorRow.add(enrollmentLabel);
        selectorRow.add(enrollmentCombo);

        JTextArea guidance = new JTextArea("""
                Pick a section and student, then enter grades for each component.
                Final grade will be calculated automatically using the weighting rule.
                """);
        guidance.setWrapStyleWord(true);
        guidance.setLineWrap(true);
        guidance.setOpaque(false);
        guidance.setEditable(false);
        guidance.setForeground(theme.textSecondary());
        guidance.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

        JPanel selectorCard = createCardPanel();
        selectorCard.setLayout(new BorderLayout(0, 8));
        selectorCard.add(selectorRow, BorderLayout.NORTH);
        selectorCard.add(guidance, BorderLayout.CENTER);

        // Grade entry form panel
        JPanel gradeFormCard = createCardPanel();
        gradeFormCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // This will be populated dynamically based on section's weighting rule
        JPanel gradeFieldsPanel = new JPanel(new GridBagLayout());
        gradeFieldsPanel.setOpaque(false);
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.insets = new java.awt.Insets(4, 4, 4, 4);
        fieldGbc.anchor = GridBagConstraints.WEST;

        JLabel finalGradeLabel = new JLabel("Final Grade: -");
        finalGradeLabel.setForeground(theme.textPrimary());
        finalGradeLabel.setFont(finalGradeLabel.getFont().deriveFont(Font.BOLD, 14f));

        // Store grade fields dynamically
        java.util.List<JTextField> gradeFields = new ArrayList<>();
        java.util.List<Integer> weights = new ArrayList<>();

        Runnable updateFinalGrade = () -> {
            double finalGrade = 0;
            for (int i = 0; i < gradeFields.size() && i < weights.size(); i++) {
                try {
                    String text = gradeFields.get(i).getText().trim();
                    if (!text.isEmpty()) {
                        double score = Double.parseDouble(text);
                        if (score < 0 || score > 100) {
                            finalGradeLabel.setText("Final Grade: Invalid (scores must be 0-100)");
                            finalGradeLabel.setForeground(Color.RED);
                            return;
                        }
                        finalGrade += (weights.get(i) * score) / 100.0;
                    }
                } catch (NumberFormatException ex) {
                    finalGradeLabel.setText("Final Grade: Invalid input");
                    finalGradeLabel.setForeground(Color.RED);
                    return;
                }
            }
            if (gradeFields.isEmpty() || gradeFields.stream().allMatch(f -> f.getText().trim().isEmpty())) {
                finalGradeLabel.setText("Final Grade: -");
                finalGradeLabel.setForeground(theme.textPrimary());
            } else {
                finalGradeLabel.setText(String.format("Final Grade: %.2f", finalGrade));
                finalGradeLabel.setForeground(theme.textPrimary());
            }
        };

        Runnable loadGradeForm = () -> {
            SectionOption section = (SectionOption) sectionCombo.getSelectedItem();
            EnrollmentOption enrollment = (EnrollmentOption) enrollmentCombo.getSelectedItem();
            
            gradeFieldsPanel.removeAll();
            gradeFields.clear();
            weights.clear();
            
            if (section == null || enrollment == null) {
                gradeFormCard.revalidate();
                gradeFormCard.repaint();
                return;
            }

            erpRepository.findSection(section.id()).ifPresent(sec -> {
                String rule = sec.getWeightingRule();
                if (rule == null || rule.isEmpty()) {
                    // Add a message label
                    JLabel messageLabel = new JLabel("<html><center>This section does not have a weighting rule set.<br>Please contact an administrator to configure it.</center></html>");
                    messageLabel.setForeground(Color.RED);
                    messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    gradeFieldsPanel.removeAll();
                    gradeFieldsPanel.add(messageLabel, fieldGbc);
                    
                    gradeFields.clear();
                    weights.clear();
                    finalGradeLabel.setText("Final Grade: -");
                    gradeFormCard.revalidate();
                    gradeFormCard.repaint();
                    return;
                }
                String[] weightStrs = rule.split(",");
                String[] componentNameStrs = sec.getComponentNames() != null && !sec.getComponentNames().isEmpty() 
                    ? sec.getComponentNames().split(",") 
                    : new String[0];
                
                // Use component names if available, otherwise use Component 1, 2, 3...
                for (int i = 0; i < weightStrs.length; i++) {
                    try {
                        int weight = Integer.parseInt(weightStrs[i].trim());
                        weights.add(weight);
                        
                        String componentName;
                        if (i < componentNameStrs.length && !componentNameStrs[i].trim().isEmpty()) {
                            componentName = componentNameStrs[i].trim();
                        } else {
                            componentName = "Component " + (i + 1);
                        }
                        
                        JLabel componentLabel = new JLabel(componentName + " (" + weight + "%):");
                        componentLabel.setForeground(theme.textPrimary());
                        JTextField gradeField = new JTextField(10);
                        gradeField.addKeyListener(new java.awt.event.KeyAdapter() {
                            @Override
                            public void keyReleased(java.awt.event.KeyEvent e) {
                                updateFinalGrade.run();
                            }
                        });
                        gradeFields.add(gradeField);
                        
                        fieldGbc.gridx = 0;
                        fieldGbc.gridy = i;
                        gradeFieldsPanel.add(componentLabel, fieldGbc);
                        fieldGbc.gridx = 1;
                        gradeFieldsPanel.add(gradeField, fieldGbc);
                    } catch (NumberFormatException ex) {
                        // Skip invalid weights
                    }
                }

                // Load existing grades if any
                var result = instructorService.listGradeComponents(instructorId, section.id(), enrollment.id());
                if (result.isSuccess() && !result.getPayload().orElse(List.of()).isEmpty()) {
                    List<GradeComponent> components = result.getPayload().orElse(List.of());
                    for (int i = 0; i < Math.min(components.size(), gradeFields.size()); i++) {
                        gradeFields.get(i).setText(String.valueOf(components.get(i).getScore()));
                    }
                }

                updateFinalGrade.run();
                gradeFormCard.revalidate();
                gradeFormCard.repaint();
            });
        };

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gradeFormCard.add(gradeFieldsPanel, gbc);
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gradeFormCard.add(finalGradeLabel, gbc);

        JButton saveButton = new JButton("Submit Grades");
        stylePrimaryAction(saveButton);
        saveButton.addActionListener(e -> {
            SectionOption section = (SectionOption) sectionCombo.getSelectedItem();
            EnrollmentOption enrollment = (EnrollmentOption) enrollmentCombo.getSelectedItem();
            if (section == null || enrollment == null) {
                JOptionPane.showMessageDialog(this, "Please select both a section and a student.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if section has weighting rule
            erpRepository.findSection(section.id()).ifPresentOrElse(sec -> {
                String rule = sec.getWeightingRule();
                if (rule == null || rule.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "This section does not have a weighting rule set.\nPlease contact an administrator to set the weighting rule.", 
                        "Weighting Rule Missing", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate that grade fields are available
                if (gradeFields.isEmpty() || weights.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Grade entry form is not ready. Please wait for the form to load or select a different section.", 
                        "Form Not Ready", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Validate that grade fields match weights
                if (gradeFields.size() != weights.size()) {
                    JOptionPane.showMessageDialog(this, 
                        "Grade fields do not match weighting rule. Please refresh by selecting the section again.", 
                        "Form Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get component names from section
                Optional<Section> sectionOpt = erpRepository.findSection(section.id());
                String[] finalComponentNames = sectionOpt.isPresent() && sectionOpt.get().getComponentNames() != null && !sectionOpt.get().getComponentNames().isEmpty()
                    ? sectionOpt.get().getComponentNames().split(",")
                    : new String[0];
                
                // Collect grades
                List<GradeComponent> components = new ArrayList<>();
                
                for (int i = 0; i < gradeFields.size() && i < weights.size(); i++) {
                    String text = gradeFields.get(i).getText().trim();
                    if (!text.isEmpty()) {
                        try {
                            double score = Double.parseDouble(text);
                            if (score < 0 || score > 100) {
                                String componentName = (i < finalComponentNames.length && !finalComponentNames[i].trim().isEmpty()) 
                                    ? finalComponentNames[i].trim() 
                                    : "Component " + (i + 1);
                                JOptionPane.showMessageDialog(this, 
                                    String.format("Score for %s must be between 0 and 100.", componentName), 
                                    "Invalid Score", 
                                    JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            // Get component name
                            String componentName;
                            if (i < finalComponentNames.length && !finalComponentNames[i].trim().isEmpty()) {
                                componentName = finalComponentNames[i].trim();
                            } else {
                                componentName = "Component " + (i + 1);
                            }
                            
                            // Weight is stored as percentage (0-100), convert to decimal (0-1) for GradeComponent
                            double weightDecimal = weights.get(i) / 100.0;
                            components.add(new GradeComponent(componentName, score, weightDecimal));
                        } catch (NumberFormatException ex) {
                            String componentName = (i < finalComponentNames.length && !finalComponentNames[i].trim().isEmpty()) 
                                ? finalComponentNames[i].trim() 
                                : "Component " + (i + 1);
                            JOptionPane.showMessageDialog(this, 
                                String.format("Invalid grade value for %s. Please enter a valid number.", componentName), 
                                "Invalid Input", 
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                if (components.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Please enter at least one grade before submitting.", 
                        "No Grades Entered", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Validate that all required components have grades
                if (components.size() < weights.size()) {
                    int missing = weights.size() - components.size();
                    int response = JOptionPane.showConfirmDialog(this, 
                        String.format("You have not entered grades for %d component(s). Do you want to submit anyway?", missing), 
                        "Missing Grades", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // Calculate final grade using the formula: sum((weight * score) / 100)
                double finalGrade = 0;
                for (int i = 0; i < components.size(); i++) {
                    GradeComponent comp = components.get(i);
                    int weight = weights.get(i);
                    finalGrade += (weight * comp.getScore()) / 100.0;
                }

                // Save with calculated final grade
                try {
                    var result = instructorService.saveGradeComponentsWithFinal(instructorId, section.id(), enrollment.id(), components, finalGrade);
                    if (result.isSuccess()) {
                        JOptionPane.showMessageDialog(this, 
                            String.format("Grades submitted successfully!\nFinal Grade: %.2f", finalGrade), 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        loadGradeForm.run();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            result.getMessage().orElse("Failed to submit grades."), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "An error occurred while submitting grades: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }, () -> {
                JOptionPane.showMessageDialog(this, 
                    "Section not found. Please select a valid section.", 
                    "Section Not Found", 
                    JOptionPane.ERROR_MESSAGE);
            });
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        actions.add(saveButton);

        sectionCombo.addActionListener(e -> {
            SectionOption selected = (SectionOption) sectionCombo.getSelectedItem();
            populateEnrollmentOptions(selected == null ? null : selected.id(), enrollmentOptions);
            EnrollmentOption enrollment = enrollmentOptions.getSize() > 0 ? enrollmentOptions.getElementAt(0) : null;
            if (selected != null && enrollment != null) {
                enrollmentCombo.setSelectedItem(enrollment);
            }
            loadGradeForm.run();
        });

        enrollmentCombo.addActionListener(e -> {
            loadGradeForm.run();
        });

        SectionOption initialSection = sectionOptions.getSize() > 0 ? sectionOptions.getElementAt(0) : null;
        if (initialSection != null) {
            populateEnrollmentOptions(initialSection.id(), enrollmentOptions);
            if (enrollmentOptions.getSize() > 0) {
                enrollmentCombo.setSelectedIndex(0);
                // Trigger load after a short delay to ensure UI is ready
                javax.swing.SwingUtilities.invokeLater(() -> {
                    loadGradeForm.run();
                });
            } else {
                loadGradeForm.run();
            }
        } else {
            loadGradeForm.run();
        }

        JPanel centerStack = new JPanel(new BorderLayout(0, 16));
        centerStack.setOpaque(false);
        centerStack.add(selectorCard, BorderLayout.NORTH);
        centerStack.add(gradeFormCard, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(centerStack, BorderLayout.CENTER);
        body.add(actions, BorderLayout.SOUTH);

        return createPageLayout(tabs, "Grade Entry", "Enter grades for each component. Final grade is calculated automatically.", body);
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
