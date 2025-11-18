package edu.univ.erp;

import edu.univ.erp.ui.auth.LoginFrame;
import edu.univ.erp.ui.common.SwingTheme;

import javax.swing.SwingUtilities;

/**
 * Entry point for the University ERP desktop application.
 */
public final class Application {

    private Application() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingTheme.apply();
            new LoginFrame().setVisible(true);
        });
    }
}

