package edu.univ.erp.ui.student;

import edu.univ.erp.api.types.CourseCatalogRow;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.api.types.TimetableEntry;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class StudentDashboardFrame extends JFrame {

    private final StudentService studentService = ServiceLocator.studentService();
    private final String studentId;

    private final DefaultTableModel catalogModel = new DefaultTableModel(new Object[] {
            "Section ID", "Course", "Title", "Credits", "Instructor", "Schedule", "Capacity", "Taken"
    }, 0);
    private final DefaultTableModel timetableModel = new DefaultTableModel(new Object[] {
            "Day", "Time", "Course", "Section", "Room"
    }, 0);
    private final DefaultTableModel gradeModel = new DefaultTableModel(new Object[] {
            "Course", "Section", "Component", "Score", "Weight", "Final"
    }, 0);

    private final JLabel maintenanceLabel = new JLabel("", SwingConstants.CENTER);

    public StudentDashboardFrame(String studentId) {
        super("Student Dashboard");
        this.studentId = studentId;
        setSize(960, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Home", buildHomePanel(tabs));
        tabs.addTab("Catalog", buildCatalogPanel());
        tabs.addTab("Timetable", buildTimetablePanel());
        tabs.addTab("Grades", buildGradesPanel());
        tabs.setSelectedIndex(0);

        maintenanceLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(tabs, BorderLayout.CENTER);
        add(maintenanceLabel, BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel buildCatalogPanel() {
        JTable table = new JTable(catalogModel);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a section first.");
                return;
            }
            String sectionId = (String) catalogModel.getValueAt(row, 0);
            var result = studentService.registerSection(studentId, sectionId);
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Done"));
            refreshAll();
        });

        JButton dropButton = new JButton("Drop Section…");
        dropButton.addActionListener(e -> {
            String sectionId = JOptionPane.showInputDialog(this, "Enter Section ID to drop");
            if (sectionId == null || sectionId.isBlank()) {
                return;
            }
            var result = studentService.dropSection(studentId, sectionId.trim());
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Done"));
            refreshAll();
        });

        JButton transcriptButton = new JButton("Download Transcript (CSV)");
        transcriptButton.addActionListener(e -> downloadTranscript());

        JPanel actions = new JPanel();
        actions.add(registerButton);
        actions.add(dropButton);
        actions.add(transcriptButton);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTimetablePanel() {
        JTable table = new JTable(timetableModel);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildGradesPanel() {
        JTable table = new JTable(gradeModel);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextArea tips = new JTextArea("""
                Final grade uses instructor-provided weights.
                Download the transcript from the Catalog tab for a CSV copy.
                """);
        tips.setEditable(false);
        tips.setLineWrap(true);
        tips.setWrapStyleWord(true);
        tips.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(tips, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHomePanel(JTabbedPane tabs) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel nav = new JPanel();
        nav.setBackground(new Color(0, 158, 149, 40));
        nav.setPreferredSize(new Dimension(60, 0));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JButton hamburger = new JButton("\u2630");
        hamburger.setAlignmentX(0.5f);
        hamburger.setFocusPainted(false);
        hamburger.setBackground(new Color(0, 158, 149, 120));
        hamburger.setForeground(Color.WHITE);
        hamburger.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel menuLinks = new JPanel();
        menuLinks.setOpaque(false);
        menuLinks.setLayout(new BoxLayout(menuLinks, BoxLayout.Y_AXIS));
        menuLinks.add(Box.createVerticalStrut(16));
        menuLinks.add(createNavLink("Catalog", tabs, 1));
        menuLinks.add(Box.createVerticalStrut(8));
        menuLinks.add(createNavLink("Timetable", tabs, 2));
        menuLinks.add(Box.createVerticalStrut(8));
        menuLinks.add(createNavLink("Grades", tabs, 3));
        menuLinks.add(Box.createVerticalGlue());
        menuLinks.setVisible(false);

        hamburger.addActionListener(e -> {
            boolean show = !menuLinks.isVisible();
            menuLinks.setVisible(show);
            nav.revalidate();
            nav.repaint();
        });

        nav.add(hamburger);
        nav.add(menuLinks);

        JPanel hero = new JPanel();
        hero.setOpaque(false);
        hero.setLayout(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, " + resolveDisplayName());
        welcome.setForeground(new Color(0, 120, 120));
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("Choose a section to get started.");
        subtitle.setForeground(new Color(60, 70, 80));
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
        quickLinks.add(createPrimaryButton("Go to Catalog", tabs, 1));
        quickLinks.add(createPrimaryButton("View Timetable", tabs, 2));
        quickLinks.add(createPrimaryButton("Check Grades", tabs, 3));

        hero.add(header, BorderLayout.NORTH);
        hero.add(quickLinks, BorderLayout.CENTER);
        hero.add(new JPanel(), BorderLayout.SOUTH);

        panel.add(nav, BorderLayout.WEST);
        panel.add(hero, BorderLayout.CENTER);
        return panel;
    }

    private JButton createNavLink(String text, JTabbedPane tabs, int tabIndex) {
        JButton button = new JButton(text);
        button.setAlignmentX(0f);
        button.setFocusPainted(false);
        button.setBackground(new Color(255, 255, 255, 200));
        button.addActionListener(e -> tabs.setSelectedIndex(tabIndex));
        return button;
    }

    private JButton createPrimaryButton(String text, JTabbedPane tabs, int tabIndex) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(0, 158, 149));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.addActionListener(e -> tabs.setSelectedIndex(tabIndex));
        return button;
    }

    private ImageIcon loadLogoIcon(int width, int height) {
        var url = StudentDashboardFrame.class.getResource("/images/iiitdlogo.png");
        if (url == null) {
            return null;
        }
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
    }

    private String resolveDisplayName() {
        var session = ServiceLocator.sessionContext();
        if (session != null && session.getUsername() != null && !session.getUsername().isBlank()) {
            return session.getUsername();
        }
        return studentId;
    }

    private void refreshAll() {
        loadCatalog();
        loadTimetable();
        loadGrades();
        var maintenance = ServiceLocator.maintenanceService().isMaintenanceOn();
        maintenanceLabel.setText(maintenance ? "Maintenance Mode ON — read-only operations." : "");
    }

    private void loadCatalog() {
        catalogModel.setRowCount(0);
        var result = studentService.viewCatalog();
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Catalog unavailable."));
            return;
        }
        List<CourseCatalogRow> rows = result.getPayload().orElse(List.of());
        for (CourseCatalogRow row : rows) {
            catalogModel.addRow(new Object[] {
                    row.sectionId(),
                    row.courseCode(),
                    row.courseTitle(),
                    row.credits(),
                    row.instructorName(),
                    row.schedule(),
                    row.capacity(),
                    row.seatsTaken()
            });
        }
    }

    private void loadTimetable() {
        timetableModel.setRowCount(0);
        var result = studentService.viewTimetable(studentId);
        if (!result.isSuccess()) {
            return;
        }
        for (TimetableEntry entry : result.getPayload().orElse(List.of())) {
            timetableModel.addRow(new Object[] {
                    entry.day(),
                    entry.timeRange(),
                    entry.courseCode(),
                    entry.sectionId(),
                    entry.room()
            });
        }
    }

    private void loadGrades() {
        gradeModel.setRowCount(0);
        var result = studentService.viewGrades(studentId);
        if (!result.isSuccess()) {
            return;
        }
        for (GradeView view : result.getPayload().orElse(List.of())) {
            if (view.components().isEmpty()) {
                gradeModel.addRow(new Object[] {view.courseCode(), view.sectionId(), "-", "-", "-", format(view.finalGrade())});
                continue;
            }
            for (GradeView.ComponentScore component : view.components()) {
                gradeModel.addRow(new Object[] {
                        view.courseCode(),
                        view.sectionId(),
                        component.name(),
                        component.score(),
                        component.weight(),
                        format(view.finalGrade())
                });
            }
        }
    }

    private void downloadTranscript() {
        var result = studentService.downloadTranscriptCsv(studentId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to export transcript."));
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("transcript.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile())) {
                fos.write(result.getPayload().orElse(new byte[0]));
                JOptionPane.showMessageDialog(this, "Transcript saved.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage());
            }
        }
    }

    private String format(Double value) {
        return value == null ? "-" : String.format("%.2f", value);
    }
}

