package edu.univ.erp.ui.student;

import edu.univ.erp.api.types.CourseCatalogRow;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.api.types.TimetableEntry;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.StudentService;

import javax.swing.BorderFactory;
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
        tabs.addTab("Catalog", buildCatalogPanel());
        tabs.addTab("Timetable", buildTimetablePanel());
        tabs.addTab("Grades", buildGradesPanel());

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

