package edu.univ.erp.ui.student;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.api.types.CourseCatalogRow;
import edu.univ.erp.api.types.GradeView;
import edu.univ.erp.api.types.TimetableEntry;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.StudentService;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class StudentDashboardFrame extends JFrame {

    private static final Color BRAND_PRIMARY = new Color(0, 158, 149);
    private static final List<String> WEEK_DAYS = List.of(
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
    );
    private static final int DEFAULT_START_HOUR = 8;
    private static final int DEFAULT_END_HOUR = 18;
    private static final Color[] BLOCK_COLORS = {
            new Color(91, 143, 249),
            new Color(120, 201, 214),
            new Color(255, 167, 125),
            new Color(143, 132, 255),
            new Color(255, 204, 92),
            new Color(255, 121, 157),
            new Color(120, 200, 160)
    };
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
    private final StudentService studentService = ServiceLocator.studentService();
    private final AuthService authService = ServiceLocator.authService();
    private final String studentId;

    private final ThemePalette theme = LIGHT_THEME;

    private final DefaultTableModel catalogModel = new DefaultTableModel(new Object[] {
            "Section ID", "Course", "Title", "Credits", "Instructor", "Schedule", "Capacity", "Taken"
    }, 0);
    private final TimetableGrid timetableGrid = new TimetableGrid();
    private final GradeDeckPanel gradeDeck = new GradeDeckPanel();
    private final JTabbedPane tabs = new JTabbedPane();

    private final JLabel maintenanceLabel = new JLabel("", SwingConstants.CENTER);

    public StudentDashboardFrame(String studentId) {
        super("Student Dashboard");
        this.studentId = studentId;
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
        timetableGrid.refreshTheme();

        tabs.addTab("Home", buildHomePanel(tabs));
        tabs.addTab("Catalog", buildCatalogPanel(tabs));
        tabs.addTab("Timetable", buildTimetablePanel(tabs));
        tabs.addTab("Grades", buildGradesPanel(tabs));
        
        // Refresh catalog when tab is selected
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) { // Catalog tab
                loadCatalog();
            } else if (tabs.getSelectedIndex() == 2) { // Timetable tab
                loadTimetable();
            } else if (tabs.getSelectedIndex() == 3) { // Grades tab
                loadGrades();
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

    private JPanel buildCatalogPanel(JTabbedPane tabs) {
        JTable table = new JTable(catalogModel);
        styleDataTable(table);

        JScrollPane scrollPane = createTableScrollPane(table);

        JButton registerButton = new JButton("Register Section");
        stylePrimaryAction(registerButton);
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
        styleGhostButton(dropButton);
        dropButton.addActionListener(e -> {
            String sectionId = JOptionPane.showInputDialog(this, "Enter Section ID to drop");
            if (sectionId == null || sectionId.isBlank()) {
                return;
            }
            var result = studentService.dropSection(studentId, sectionId.trim());
            JOptionPane.showMessageDialog(this, result.getMessage().orElse("Done"));
            refreshAll();
        });

        JButton refreshButton = new JButton("Refresh Catalog");
        styleSecondaryAction(refreshButton);
        refreshButton.addActionListener(e -> loadCatalog());

        JButton transcriptButton = new JButton("Download Transcript (CSV)");
        styleSecondaryAction(transcriptButton);
        transcriptButton.addActionListener(e -> downloadTranscript());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        actions.add(refreshButton);
        actions.add(transcriptButton);
        actions.add(dropButton);
        actions.add(registerButton);

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

        return createPageLayout(
                tabs,
                "Course Catalog",
                "Browse every open section, see real-time capacity, and take action instantly.",
                body
        );
    }

    private JPanel buildTimetablePanel(JTabbedPane tabs) {
        JScrollPane scrollPane = new JScrollPane(timetableGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(theme.cardBackground());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel tableCard = createCardPanel();
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(tableCard, BorderLayout.CENTER);
        body.add(wrapper, BorderLayout.CENTER);

        return createPageLayout(
                tabs,
                "My Timetable",
                "Visualize registered sections by day and time to avoid conflicts.",
                body
        );
    }

    private JPanel buildGradesPanel(JTabbedPane tabs) {
        JScrollPane scrollPane = new JScrollPane(gradeDeck);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(theme.cardBackground());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(scrollPane, BorderLayout.CENTER);
        body.add(wrapper, BorderLayout.CENTER);

        return createPageLayout(
                tabs,
                "Grades Overview",
                "Track component scores, weightings, and final averages per course.",
                body
        );
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

        JLabel welcome = new JLabel("Welcome, " + resolveDisplayName());
        welcome.setForeground(theme.textPrimary());
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 28f));

        JLabel subtitle = new JLabel("Choose a section to get started.");
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
        quickLinks.add(createPrimaryButton("Go to Catalog", tabs, 1));
        quickLinks.add(createPrimaryButton("View Timetable", tabs, 2));
        quickLinks.add(createPrimaryButton("Check Grades", tabs, 3));
        
        JButton changePasswordButton = new JButton("Change Password");
        styleSecondaryAction(changePasswordButton);
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        
        JButton logoutButton = new JButton("Logout");
        styleSecondaryAction(logoutButton);
        logoutButton.addActionListener(e -> logout());
        
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

    private JPanel createNavigationColumn(JTabbedPane tabs) {
        JPanel nav = new JPanel();
        nav.setBackground(theme.navBackground());
        nav.setPreferredSize(new Dimension(80, 0));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JButton hamburger = new JButton("\u2630");
        hamburger.setAlignmentX(0.5f);
        hamburger.setFocusPainted(false);
        hamburger.setBackground(BRAND_PRIMARY);
        hamburger.setForeground(Color.WHITE);
        hamburger.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel menuLinks = new JPanel();
        menuLinks.setOpaque(false);
        menuLinks.setLayout(new BoxLayout(menuLinks, BoxLayout.Y_AXIS));
        menuLinks.add(Box.createVerticalStrut(16));
        menuLinks.add(createNavLink("Home", tabs, 0));
        menuLinks.add(Box.createVerticalStrut(8));
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
            hamburger.setText(show ? "\u2715" : "\u2630");
            nav.revalidate();
            nav.repaint();
        });

        nav.add(hamburger);
        nav.add(menuLinks);
        return nav;
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

    private final class TimetableGrid extends JPanel {

        private List<ParsedEntry> entries = List.of();
        private int minHour = DEFAULT_START_HOUR;
        private int maxHour = DEFAULT_END_HOUR;

        private TimetableGrid() {
            setBackground(theme.cardBackground());
        }

        void setEntries(List<TimetableEntry> data) {
            if (data == null || data.isEmpty()) {
                entries = List.of();
                minHour = DEFAULT_START_HOUR;
                maxHour = DEFAULT_END_HOUR;
                revalidate();
                repaint();
                return;
            }

            List<ParsedEntry> parsed = new ArrayList<>();
            int detectedStart = DEFAULT_START_HOUR;
            int detectedEnd = DEFAULT_END_HOUR;

            for (TimetableEntry entry : data) {
                ParsedEntry parsedEntry = toParsedEntry(entry);
                if (parsedEntry == null) {
                    continue;
                }
                parsed.add(parsedEntry);
                detectedStart = Math.min(detectedStart, parsedEntry.start().getHour());
                int entryEndHour = parsedEntry.end().getMinute() == 0
                        ? parsedEntry.end().getHour()
                        : parsedEntry.end().getHour() + 1;
                detectedEnd = Math.max(detectedEnd, entryEndHour);
            }

            entries = parsed;
            minHour = Math.min(detectedStart, DEFAULT_START_HOUR);
            maxHour = Math.max(Math.max(detectedEnd, minHour + 1), DEFAULT_END_HOUR);
            revalidate();
            repaint();
        }

        void refreshTheme() {
            setBackground(theme.cardBackground());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int slotWidth = 130;
            int slotHeight = 60;
            int headerHeight = 40;
            int timeColumnWidth = 80;
            int hourSpan = maxHour - minHour;

            int totalWidth = timeColumnWidth + WEEK_DAYS.size() * slotWidth;
            int totalHeight = headerHeight + hourSpan * slotHeight;
            Dimension preferred = new Dimension(totalWidth, totalHeight);
            if (!preferred.equals(getPreferredSize())) {
                setPreferredSize(preferred);
            }

            g2.setColor(theme.cardBackground());
            g2.fillRect(0, 0, totalWidth, totalHeight);

            drawHeaders(g2, timeColumnWidth, headerHeight, slotWidth, totalWidth);
            drawGrid(g2, timeColumnWidth, headerHeight, slotWidth, slotHeight, hourSpan, totalWidth);
            drawTimeLabels(g2, timeColumnWidth, headerHeight, slotHeight);
            drawBlocks(g2, timeColumnWidth, headerHeight, slotWidth, slotHeight);

            g2.dispose();
        }

        private void drawHeaders(Graphics2D g2, int timeColumnWidth, int headerHeight, int slotWidth, int totalWidth) {
            g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
            g2.setColor(theme.tableHeaderBackground());
            g2.fillRect(0, 0, totalWidth, headerHeight);
            g2.setColor(theme.tableHeaderText());
            g2.drawString("Time", 16, headerHeight - 12);

            for (int i = 0; i < WEEK_DAYS.size(); i++) {
                int x = timeColumnWidth + i * slotWidth;
                g2.setColor(theme.tableHeaderBackground());
                g2.fillRect(x, 0, slotWidth, headerHeight);
                g2.setColor(theme.tableHeaderText());
                String label = prettyDayLabel(WEEK_DAYS.get(i));
                int textWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x + (slotWidth - textWidth) / 2, headerHeight - 12);
            }
        }

        private void drawGrid(Graphics2D g2, int timeColumnWidth, int headerHeight, int slotWidth, int slotHeight, int hourSpan, int totalWidth) {
            g2.setColor(theme.gridLine());
            for (int i = 0; i <= WEEK_DAYS.size(); i++) {
                int x = timeColumnWidth + i * slotWidth;
                g2.drawLine(x, headerHeight, x, headerHeight + hourSpan * slotHeight);
            }
            for (int hour = 0; hour <= hourSpan; hour++) {
                int y = headerHeight + hour * slotHeight;
                g2.drawLine(0, y, totalWidth, y);
            }
        }

        private void drawTimeLabels(Graphics2D g2, int timeColumnWidth, int headerHeight, int slotHeight) {
            g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            g2.setColor(theme.textSecondary());
            for (int hour = minHour; hour < maxHour; hour++) {
                int y = headerHeight + (hour - minHour) * slotHeight;
                g2.drawString(formatHourLabel(hour), 16, y + slotHeight - 12);
            }
        }

        private void drawBlocks(Graphics2D g2, int timeColumnWidth, int headerHeight, int slotWidth, int slotHeight) {
            if (entries.isEmpty()) {
                g2.setFont(getFont().deriveFont(Font.ITALIC, 13f));
                g2.setColor(theme.placeholderText());
                g2.drawString("No registered sections yet.", timeColumnWidth + 24, headerHeight + 40);
                return;
            }

            for (ParsedEntry block : entries) {
                double startOffset = hoursFromStart(block.start());
                double endOffset = hoursFromStart(block.end());
                int blockX = timeColumnWidth + block.dayIndex() * slotWidth + 6;
                int blockY = headerHeight + (int) Math.round(startOffset * slotHeight) + 4;
                int blockWidth = slotWidth - 12;
                int blockHeight = Math.max(24, (int) Math.round((endOffset - startOffset) * slotHeight) - 8);

                g2.setColor(block.color());
                g2.fillRoundRect(blockX, blockY, blockWidth, blockHeight, 16, 16);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawRoundRect(blockX, blockY, blockWidth, blockHeight, 16, 16);

                int textY = blockY + 20;
                g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
                g2.setColor(Color.WHITE);
                g2.drawString(block.entry().courseCode() + " – " + block.entry().sectionId(), blockX + 12, textY);

                g2.setFont(getFont().deriveFont(Font.PLAIN, 11f));
                g2.drawString(block.entry().room() + " • " + block.entry().timeRange(), blockX + 12, textY + 16);
            }
        }

        private double hoursFromStart(LocalTime time) {
            int minutesFromStart = (time.getHour() * 60 + time.getMinute()) - (minHour * 60);
            return Math.max(0, minutesFromStart / 60d);
        }

        private ParsedEntry toParsedEntry(TimetableEntry entry) {
            if (entry == null) {
                return null;
            }
            int dayIndex = WEEK_DAYS.indexOf(normalizeDay(entry.day()));
            if (dayIndex < 0) {
                return null;
            }
            LocalTime[] range = parseTimeRange(entry.timeRange());
            if (range == null) {
                return null;
            }
            Color color = colorForCourse(entry.courseCode());
            return new ParsedEntry(dayIndex, range[0], range[1], entry, color);
        }
    }

    private record ParsedEntry(int dayIndex, LocalTime start, LocalTime end, TimetableEntry entry, Color color) {
    }

    private final class GradeDeckPanel extends JPanel {

        private GradeDeckPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(0, 4, 12, 4));
        }

        void setGrades(List<GradeView> views) {
            removeAll();
            boolean hasCards = views != null && !views.isEmpty();
            if (!hasCards) {
                add(createNoGradesCard());
            } else {
                for (GradeView view : views) {
                    add(createGradeCard(view));
                    add(Box.createVerticalStrut(16));
                }
            }
            add(Box.createVerticalStrut(8));
            add(createInfoBanner());
            revalidate();
            repaint();
        }

        private JPanel createGradeCard(GradeView view) {
            JPanel card = createCardPanel();
            card.setLayout(new BorderLayout(0, 12));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);

            JLabel title = new JLabel(view.courseCode() + " • " + view.sectionId());
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            title.setForeground(theme.textPrimary());

            JLabel finalGrade = new JLabel("Final: " + format(view.finalGrade()));
            finalGrade.setFont(finalGrade.getFont().deriveFont(Font.BOLD, 16f));
            finalGrade.setForeground(BRAND_PRIMARY);

            header.add(title, BorderLayout.WEST);
            header.add(finalGrade, BorderLayout.EAST);

            card.add(header, BorderLayout.NORTH);

            if (view.components().isEmpty()) {
                JLabel placeholder = new JLabel("No component scores posted yet.");
                placeholder.setForeground(theme.placeholderText());
                placeholder.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
                card.add(placeholder, BorderLayout.CENTER);
            } else {
                JTable table = createComponentTable(view);
                JScrollPane scrollPane = createTableScrollPane(table);
                card.add(scrollPane, BorderLayout.CENTER);
            }

            return card;
        }

        private JTable createComponentTable(GradeView view) {
            DefaultTableModel model = new DefaultTableModel(new Object[] {"Component", "Score", "Weight"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (GradeView.ComponentScore component : view.components()) {
                model.addRow(new Object[] {
                        component.name(),
                        format(component.score()),
                        formatPercent(component.weight())
                });
            }

            JTable table = new JTable(model);
            styleDataTable(table);
            table.setAutoCreateRowSorter(false);
            table.setRowHeight(26);
            return table;
        }

        private JPanel createNoGradesCard() {
            JPanel card = createCardPanel();
            card.setLayout(new BorderLayout());
            JLabel label = new JLabel("No grades posted yet. Check back after instructors publish results.");
            label.setForeground(theme.placeholderText());
            card.add(label, BorderLayout.CENTER);
            return card;
        }

        private JPanel createInfoBanner() {
            JTextArea tips = new JTextArea("""
                    Final grade uses instructor-provided weights. Download the transcript from the Catalog tab for a CSV copy.
                    """);
            tips.setEditable(false);
            tips.setLineWrap(true);
            tips.setWrapStyleWord(true);
            tips.setBackground(theme.infoBackground());
            tips.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            tips.setForeground(theme.infoText());

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.add(tips, BorderLayout.CENTER);
            return panel;
        }
    }

    private static String prettyDayLabel(String day) {
        if (day == null || day.isBlank()) {
            return "";
        }
        String lower = day.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String formatHourLabel(int hour) {
        int normalized = ((hour % 24) + 24) % 24;
        return String.format("%02d:00", normalized);
    }

    private static String normalizeDay(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static LocalTime[] parseTimeRange(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) {
            return null;
        }
        String normalized = timeRange.replace("–", "-");
        String[] parts = normalized.split("-");
        if (parts.length != 2) {
            return null;
        }
        try {
            LocalTime start = LocalTime.parse(parts[0].trim());
            LocalTime end = LocalTime.parse(parts[1].trim());
            return new LocalTime[] {start, end};
        } catch (Exception ex) {
            return null;
        }
    }

    private static Color colorForCourse(String courseCode) {
        if (courseCode == null) {
            courseCode = "";
        }
        int index = Math.abs(courseCode.hashCode());
        return BLOCK_COLORS[index % BLOCK_COLORS.length];
    }

    private static String formatPercent(Double weight) {
        if (weight == null) {
            return "-";
        }
        double value = weight <= 1 ? weight * 100 : weight;
        if (Math.abs(value - Math.round(value)) < 0.01) {
            return String.format("%.0f%%", value);
        }
        return String.format("%.1f%%", value);
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
        var result = studentService.viewTimetable(studentId);
        if (!result.isSuccess()) {
            timetableGrid.setEntries(List.of());
            return;
        }
        timetableGrid.setEntries(result.getPayload().orElse(List.of()));
    }

    private void loadGrades() {
        var result = studentService.viewGrades(studentId);
        if (!result.isSuccess()) {
            gradeDeck.setGrades(List.of());
            return;
        }
        gradeDeck.setGrades(result.getPayload().orElse(List.of()));
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

    private String format(Double value) {
        return value == null ? "-" : String.format("%.2f", value);
    }
}

