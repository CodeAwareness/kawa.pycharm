package com.codeawareness.pycharm.highlighting;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.JBColor;

import java.awt.Color;

/**
 * Provides colors for Code Awareness highlights based on the current editor theme.
 * Supports both light and dark themes.
 */
public class ColorSchemeProvider {

    private static final Color LIGHT_THEME_COLOR = new Color(0xff, 0xdd, 0x34);
    private static final Color DARK_THEME_COLOR = new Color(0x1f, 0x1c, 0xc2);

    /**
     * Get the highlight color for the current theme.
     * Light theme: #ffdd34 (yellow)
     * Dark theme: #1f1cc2 (blue)
     */
    public static Color getHighlightColor() {
        try {
            EditorColorsManager manager = EditorColorsManager.getInstance();
            if (manager == null) {
                return LIGHT_THEME_COLOR;
            }

            EditorColorsScheme scheme = manager.getGlobalScheme();
            if (scheme == null) {
                return LIGHT_THEME_COLOR;
            }

            // Check if using dark theme
            if (isDarkTheme(scheme)) {
                return DARK_THEME_COLOR; // Blue for dark theme
            } else {
                return LIGHT_THEME_COLOR; // Yellow for light theme
            }
        } catch (Throwable ignored) {
            // In headless/unit-test environments the IntelliJ services might not be available.
            return LIGHT_THEME_COLOR;
        }
    }

    /**
     * Get the highlight color using JBColor (automatically switches based on theme).
     */
    public static JBColor getHighlightJBColor() {
        return new JBColor(
            LIGHT_THEME_COLOR, // Light theme: yellow
            DARK_THEME_COLOR   // Dark theme: blue
        );
    }

    /**
     * Check if the current theme is dark.
     */
    private static boolean isDarkTheme(EditorColorsScheme scheme) {
        Color backgroundColor = scheme.getDefaultBackground();
        if (backgroundColor == null) {
            return false;
        }

        // Calculate luminance to determine if dark theme
        // Dark theme has low luminance (closer to 0)
        int r = backgroundColor.getRed();
        int g = backgroundColor.getGreen();
        int b = backgroundColor.getBlue();

        // Perceived luminance formula
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;

        return luminance < 0.5;
    }
}
