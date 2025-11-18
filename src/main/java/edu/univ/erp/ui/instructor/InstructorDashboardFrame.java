package edu.univ.erp.ui.instructor;

import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.InstructorService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InstructorDashboardFrame extends JFrame {

    private final InstructorService instructorService = ServiceLocator.instructorService();
    private final String instructorId = ServiceLocator.sessionContext().getUserId();

    private final DefaultTableModel sectionsModel = new DefaultTableModel(new Object[] {"Section ID"}, 0);
    private final JTextArea gradePreview = new JTextArea();
    private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);

    public InstructorDashboardFrame() {
        super("Instructor Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTable table = new JTable(sectionsModel);
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh Sections");
        refresh.addActionListener(e -> loadSections());
        JButton viewGrades = new JButton("View Grades");
        viewGrades.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a section first.");
                return;
            }
            String sectionId = (String) sectionsModel.getValueAt(row, 0);
            showGrades(sectionId);
        });

        JButton recordScores = new JButton("Record Scores…");
        recordScores.addActionListener(e -> recordScoresDialog());

        JButton computeFinals = new JButton("Compute Final Grades…");
        computeFinals.addActionListener(e -> computeFinalsDialog());

        JPanel actions = new JPanel();
        actions.add(refresh);
        actions.add(viewGrades);
        actions.add(recordScores);
        actions.add(computeFinals);
        left.add(actions, BorderLayout.SOUTH);

        gradePreview.setEditable(false);
        gradePreview.setLineWrap(true);
        gradePreview.setWrapStyleWord(true);

        add(left, BorderLayout.WEST);
        add(new JScrollPane(gradePreview), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadSections();
    }

    private void loadSections() {
        sectionsModel.setRowCount(0);
        var result = instructorService.listMySections(instructorId);
        if (!result.isSuccess()) {
            statusLabel.setText(result.getMessage().orElse("Unable to load sections."));
            return;
        }
        for (String id : result.getPayload().orElse(List.of())) {
            sectionsModel.addRow(new Object[] {id});
        }
        statusLabel.setText("Sections loaded: " + sectionsModel.getRowCount());
    }

    private void showGrades(String sectionId) {
        var result = instructorService.viewGradesForSection(instructorId, sectionId);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Unable to fetch grades."));
            return;
        }
        GradeView view = result.getPayload().orElse(null);
        if (view == null) {
            gradePreview.setText("No data.");
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Course: ").append(view.courseCode())
                .append(" | Section: ").append(view.sectionId()).append(System.lineSeparator());
        for (GradeView.ComponentScore component : view.components()) {
            builder.append(" - ")
                    .append(component.name())
                    .append(": ")
                    .append(component.score())
                    .append(" (w=")
                    .append(component.weight())
                    .append(")")
                    .append(System.lineSeparator());
        }
        builder.append("Final Grade: ").append(view.finalGrade() == null ? "-" : view.finalGrade());
        gradePreview.setText(builder.toString());
    }

    private void recordScoresDialog() {
        String sectionId = JOptionPane.showInputDialog(this, "Section ID");
        String enrollmentId = JOptionPane.showInputDialog(this, "Enrollment ID");
        String components = JOptionPane.showInputDialog(this, "Component scores (format Quiz=20,Mid=25)");
        if (sectionId == null || enrollmentId == null || components == null) {
            return;
        }
        Map<String, Double> parsed = parseKeyValuePairs(components);
        var result = instructorService.recordScores(instructorId, sectionId.trim(), enrollmentId.trim(), parsed);
        JOptionPane.showMessageDialog(this, result.getMessage().orElse("Done"));
    }

    private void computeFinalsDialog() {
        String sectionId = JOptionPane.showInputDialog(this, "Section ID");
        String weights = JOptionPane.showInputDialog(this, "Weights (Quiz=0.2,Mid=0.3,End=0.5)");
        if (sectionId == null || weights == null) {
            return;
        }
        Map<String, Double> parsed = parseKeyValuePairs(weights);
        var result = instructorService.computeFinalGrades(instructorId, sectionId.trim(), parsed);
        JOptionPane.showMessageDialog(this, result.getMessage().orElse("Done"));
    }

    private Map<String, Double> parseKeyValuePairs(String input) {
        Map<String, Double> map = new HashMap<>();
        if (input == null || input.isBlank()) {
            return map;
        }
        String[] parts = input.split(",");
        for (String part : parts) {
            String[] tokens = part.split("=");
            if (tokens.length == 2) {
                try {
                    map.put(tokens[0].trim(), Double.parseDouble(tokens[1].trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }
}

