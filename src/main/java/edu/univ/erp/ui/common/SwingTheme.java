package edu.univ.erp.ui.common;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Applies the configured Swing Look & Feel.
 */
public final class SwingTheme {

    private SwingTheme() {
    }

    public static void apply() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Unable to apply FlatLaf: " + e.getMessage());
        }
    }
}

