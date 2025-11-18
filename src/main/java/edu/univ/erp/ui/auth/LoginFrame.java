package edu.univ.erp.ui.auth;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.domain.user.Role;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.admin.AdminDashboardFrame;
import edu.univ.erp.ui.instructor.InstructorDashboardFrame;
import edu.univ.erp.ui.student.StudentDashboardFrame;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public final class LoginFrame extends JFrame {

    private final AuthService authService = ServiceLocator.authService();

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel statusLabel = new JLabel("Enter credentials", SwingConstants.CENTER);

    public LoginFrame() {
        super("University ERP — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());

        add(form, BorderLayout.CENTER);
        add(loginButton, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
    }

    private void attemptLogin() {
        OperationResult<Role> result = authService.login(usernameField.getText(), new String(passwordField.getPassword()));
        if (!result.isSuccess()) {
            statusLabel.setText(result.getMessage().orElse("Login failed."));
            return;
        }
        Role role = result.getPayload().orElse(Role.STUDENT);
        dispose();
        switch (role) {
            case ADMIN -> new AdminDashboardFrame().setVisible(true);
            case INSTRUCTOR -> new InstructorDashboardFrame().setVisible(true);
            case STUDENT -> new StudentDashboardFrame(ServiceLocator.sessionContext().getUserId()).setVisible(true);
            default -> JOptionPane.showMessageDialog(this, "Unsupported role: " + role);
        }
    }
}

