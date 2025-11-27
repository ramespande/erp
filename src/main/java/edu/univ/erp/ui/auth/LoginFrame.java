package edu.univ.erp.ui.auth;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import edu.univ.erp.api.common.OperationResult;
import edu.univ.erp.domain.user.Role;
import edu.univ.erp.infra.ServiceLocator;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.ui.admin.AdminDashboardFrame;
import edu.univ.erp.ui.instructor.InstructorDashboardFrame;
import edu.univ.erp.ui.student.StudentDashboardFrame;

public final class LoginFrame extends JFrame {

    private static final Color BRAND_PRIMARY = new Color(0, 158, 149);
    private static final Color BRAND_DARK = new Color(6, 38, 52);
    private static final Color CARD_BG = new Color(255, 255, 255, 220);

    private final AuthService authService = ServiceLocator.authService();

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final StatusLabel statusLabel = new StatusLabel("Enter credentials", SwingConstants.CENTER);
    private final JButton loginButton = new JButton("Login");
    private javax.swing.Timer lockoutTimer;
    private long lockoutSecondsRemaining = 0;

    public LoginFrame() {
        super("University ERP - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 600));
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Clean up timer when window closes
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (lockoutTimer != null) {
                    lockoutTimer.stop();
                }
            }
        });

        JLayeredPane layeredPane = new FullSizeLayeredPane();

        BackgroundPanel background = new BackgroundPanel("/images/IIITD.jpeg");
        layeredPane.add(background, Integer.valueOf(0));

        CenteredOverlayPanel overlay = new CenteredOverlayPanel();
        overlay.add(buildLoginCard());
        layeredPane.add(overlay, Integer.valueOf(1));

        setContentPane(layeredPane);
    }

    private JPanel buildLoginCard() {
        FrostedPanel card = new FrostedPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(360, 460));
        card.setMaximumSize(new Dimension(380, 480));
        card.setAlignmentX(CENTER_ALIGNMENT);
        card.setAlignmentY(CENTER_ALIGNMENT);

        JLabel logoImage = new JLabel(loadScaledIcon("/images/iiitdlogo.png", 90, 60));
        logoImage.setAlignmentX(LEFT_ALIGNMENT);
        logoImage.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel logoWordmark = new JLabel("Sign in to ERP");
        logoWordmark.setForeground(BRAND_PRIMARY);
        logoWordmark.setFont(logoWordmark.getFont().deriveFont(Font.BOLD, 24f));
        logoWordmark.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Use your institute credentials");
        subtitle.setForeground(new Color(70, 80, 90));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0, 158, 149, 35));
        statusLabel.setForeground(BRAND_DARK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);

        styleField(usernameField, null);
        usernameField.setText("");
        styleField(passwordField, null);
        passwordField.setText("");
        passwordField.setEchoChar('*');

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setAlignmentX(LEFT_ALIGNMENT);

        fields.add(buildLabeledField("Username", usernameField));
        fields.add(Box.createVerticalStrut(18));
        fields.add(buildLabeledField("Password", passwordField));

        loginButton.setBackground(new Color(0, 158, 149, 230));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> attemptLogin());

        KeyListener enterKeyListener = new EnterKeyListener();
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        card.add(logoImage);
        card.add(logoWordmark);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(fields);
        card.add(Box.createVerticalStrut(14));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(8));
        JLabel hint = new JLabel("Google SSO coming soon. Contact admin for access.");
        hint.setForeground(new Color(120, 130, 140));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 12f));
        hint.setAlignmentX(LEFT_ALIGNMENT);
        card.add(hint);

        return card;
    }

    private JPanel buildLabeledField(String label, JTextField field) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel jLabel = new JLabel(label);
        jLabel.setForeground(BRAND_DARK);
        jLabel.setFont(jLabel.getFont().deriveFont(Font.BOLD, 13f));

        container.add(jLabel);
        container.add(Box.createVerticalStrut(6));
        container.add(field);
        return container;
    }

    private void styleField(JTextField field, String placeholder) {
        field.setColumns(12);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 222)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        field.setForeground(BRAND_DARK);
        field.setBackground(new Color(255, 255, 255, 210));
        field.setOpaque(true);
        field.setCaretColor(BRAND_PRIMARY.darker());
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 14f));
        field.setToolTipText(placeholder);

        Dimension size = new Dimension(280, 38);
        field.setMaximumSize(size);
        field.setPreferredSize(size);
    }

    private void attemptLogin() {
        // Don't allow login attempts while timer is running (still locked out)
        if (lockoutTimer != null && lockoutTimer.isRunning()) {
            return;
        }
        
        loginButton.setEnabled(false);
        OperationResult<Role> result = authService.login(usernameField.getText().trim(), new String(passwordField.getPassword()));
        
        if (!result.isSuccess()) {
            String message = result.getMessage().orElse("Invalid credentials. Try again.");
            
            // Check if account is locked
            if (message.startsWith("LOCKED:")) {
                try {
                    lockoutSecondsRemaining = Long.parseLong(message.substring(7));
                    startLockoutTimer();
                    return;
                } catch (NumberFormatException e) {
                    // Fall through to normal error handling
                }
            }
            
            // Update status label with error message (replaces any previous message)
            statusLabel.setBackground(new Color(210, 35, 35, 30));
            statusLabel.setForeground(new Color(120, 20, 20));
            updateStatusLabelText(message);
            loginButton.setEnabled(true);
            return;
        }

        // Stop timer if running (successful login)
        if (lockoutTimer != null) {
            lockoutTimer.stop();
            lockoutTimer = null;
        }

        statusLabel.setBackground(new Color(0, 158, 149, 26));
        statusLabel.setForeground(BRAND_DARK);
        updateStatusLabelText("Welcome back! Redirecting …");
        loginButton.setEnabled(true);

        Role role = result.getPayload().orElse(Role.STUDENT);
        dispose();
        switch (role) {
            case ADMIN -> new AdminDashboardFrame().setVisible(true);
            case INSTRUCTOR -> new InstructorDashboardFrame().setVisible(true);
            case STUDENT -> new StudentDashboardFrame(ServiceLocator.sessionContext().getUserId()).setVisible(true);
            default -> JOptionPane.showMessageDialog(this, "Unsupported role: " + role);
        }
    }
    
    private void startLockoutTimer() {
        // Stop existing timer if any
        if (lockoutTimer != null) {
            lockoutTimer.stop();
        }
        
        // Disable login button and fields
        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        
        // Update status label
        statusLabel.setBackground(new Color(210, 35, 35, 30));
        statusLabel.setForeground(new Color(120, 20, 20));
        updateLockoutStatus();
        
        // Create timer that updates every second
        lockoutTimer = new javax.swing.Timer(1000, e -> {
            lockoutSecondsRemaining--;
            if (lockoutSecondsRemaining <= 0) {
                lockoutTimer.stop();
                lockoutSecondsRemaining = 0;
                // Re-enable login
                loginButton.setEnabled(true);
                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
                statusLabel.setBackground(new Color(0, 158, 149, 35));
                statusLabel.setForeground(BRAND_DARK);
                updateStatusLabelText("Account unlocked. You can try logging in again.");
            } else {
                updateLockoutStatus();
            }
        });
        lockoutTimer.setRepeats(true);
        lockoutTimer.start();
    }
    
    /**
     * Updates the status label text, ensuring the old text is completely cleared
     * and repainted before the new text is set to prevent visual overlap.
     */
    private void updateStatusLabelText(String newText) {
        statusLabel.updateText(newText);
    }
    
    /**
     * Custom JLabel that ensures clean text updates by completely controlling
     * the rendering process to prevent visual overlap.
     */
    private static final class StatusLabel extends JLabel {
        private volatile String displayText = "";
        
        private StatusLabel(String text, int alignment) {
            super(text, alignment);
            setOpaque(true);
            this.displayText = text != null ? text : "";
        }
        
        @Override
        public void setText(String text) {
            displayText = text != null ? text : "";
            super.setText(text);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Step 1: ALWAYS fill the entire background first to clear ANY old text
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Step 2: Only paint the current text (not any cached/old text)
                if (displayText != null && !displayText.isEmpty()) {
                    g2.setColor(getForeground());
                    g2.setFont(getFont());
                    
                    // Calculate text position based on alignment
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(displayText);
                    int textHeight = fm.getHeight();
                    int x = 0;
                    int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                    
                    if (getHorizontalAlignment() == SwingConstants.CENTER) {
                        x = (getWidth() - textWidth) / 2;
                    } else if (getHorizontalAlignment() == SwingConstants.RIGHT) {
                        x = getWidth() - textWidth - getInsets().right;
                    } else {
                        x = getInsets().left;
                    }
                    
                    // Draw ONLY the current text
                    g2.drawString(displayText, x, y);
                }
            } finally {
                g2.dispose();
            }
        }
        
        /**
         * Updates the text with a clean transition that prevents overlap.
         * This method ensures the old text is completely gone before new text appears.
         */
        void updateText(String newText) {
            // Update the display text atomically
            displayText = newText != null ? newText : "";
            
            // Update the super text (for layout calculations)
            super.setText(displayText);
            
            // Force immediate repaint - our paintComponent will handle the clean rendering
            if (isShowing()) {
                repaint();
                // Also repaint parent to ensure no artifacts
                if (getParent() != null) {
                    java.awt.Rectangle bounds = getBounds();
                    getParent().repaint(bounds.x, bounds.y, bounds.width, bounds.height);
                }
            }
        }
    }
    
    private void updateLockoutStatus() {
        if (lockoutSecondsRemaining > 0) {
            String message = String.format("Account locked. Please try again in %d second(s).", lockoutSecondsRemaining);
            updateStatusLabelText(message);
        }
    }

    private ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
        try (InputStream stream = LoginFrame.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            Image image = ImageIO.read(stream);
            if (image == null) {
                return null;
            }
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static final class BackgroundPanel extends JPanel {
        private final Image backgroundImage;

        private BackgroundPanel(String resourcePath) {
            setOpaque(false);
            Image image = null;
            if (resourcePath != null) {
                try (InputStream stream = LoginFrame.class.getResourceAsStream(resourcePath)) {
                    if (stream != null) {
                        image = ImageIO.read(stream);
                    }
                } catch (IOException ignored) {
                    image = null;
                }
            }
            this.backgroundImage = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                paintFallbackScene(g2);
            }

            GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 39, 54, 140), 0, getHeight(), new Color(0, 39, 54, 60));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }

        private void paintFallbackScene(Graphics2D g2) {
            g2.setPaint(new GradientPaint(0, 0, new Color(167, 219, 255), 0, getHeight(), new Color(72, 157, 212)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(231, 203, 158));
            int buildingWidth = (int) (getWidth() * 0.6);
            int buildingHeight = (int) (getHeight() * 0.4);
            int x = (getWidth() - buildingWidth) / 2;
            int y = (int) (getHeight() * 0.3);
            g2.fillRoundRect(x, y, buildingWidth, buildingHeight, 16, 16);
            g2.setColor(new Color(56, 120, 86));
            g2.fillOval((int) (getWidth() * 0.1), (int) (getHeight() * 0.55), (int) (getWidth() * 0.8), (int) (getHeight() * 0.6));
        }
    }

    private final class EnterKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            // no-op
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                // Only allow login if not locked out
                if (lockoutTimer == null || !lockoutTimer.isRunning()) {
                    attemptLogin();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // no-op
        }
    }

    private static final class FrostedPanel extends JPanel {
        private FrostedPanel() {
            setOpaque(false);
            setBorder(new EmptyBorder(28, 32, 28, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.setColor(new Color(255, 255, 255, 150));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
        }
    }

    private static final class FullSizeLayeredPane extends JLayeredPane {
        @Override
        public void doLayout() {
            for (int i = 0; i < getComponentCount(); i++) {
                getComponent(i).setBounds(0, 0, getWidth(), getHeight());
            }
        }
    }

    private static final class CenteredOverlayPanel extends JPanel {
        private CenteredOverlayPanel() {
            super(new GridBagLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(32, 32, 32, 32));
        }

        @Override
        public Component add(Component comp) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            super.add(comp, gbc);
            return comp;
        }
    }
}

