package com.codeawareness.pycharm.highlighting;

import com.codeawareness.pycharm.settings.CodeAwarenessSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.JBColor;

import java.awt.Color;

/**
 * Provides colors for Code Awareness highlights based on the current editor theme.
 * Supports both light and dark themes with user-configurable colors.
 */
public class ColorSchemeProvider {

    // Fallback defaults (used when settings service is unavailable)
    private static final Color DEFAULT_LIGHT_THEME_COLOR = new Color(0xff, 0xea, 0x83);
    private static final Color DEFAULT_DARK_THEME_COLOR = new Color(0x0a, 0x07, 0x1d);

    /**
     * Get the highlight color for the current theme.
     * Reads user-configured colors from settings.
     */
    public static Color getHighlightColor() {
        try {
            EditorColorsManager manager = EditorColorsManager.getInstance();
            if (manager == null) {
                return getLightThemeColor();
            }

            EditorColorsScheme scheme = manager.getGlobalScheme();
            if (scheme == null) {
                return getLightThemeColor();
            }

            // Check if using dark theme
            if (isDarkTheme(scheme)) {
                return getDarkThemeColor();
            } else {
                return getLightThemeColor();
            }
        } catch (Throwable ignored) {
            // In headless/unit-test environments the IntelliJ services might not be available.
            return getLightThemeColor();
        }
    }

    /**
     * Get the highlight color using JBColor (automatically switches based on theme).
     */
    public static JBColor getHighlightJBColor() {
        return new JBColor(
            getLightThemeColor(),
            getDarkThemeColor()
        );
    }

    /**
     * Get the light theme color from settings.
     */
    private static Color getLightThemeColor() {
        try {
            CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();
            return parseColor(settings.getLightThemeColor());
        } catch (Throwable ignored) {
            return DEFAULT_LIGHT_THEME_COLOR;
        }
    }

    /**
     * Get the dark theme color from settings.
     */
    private static Color getDarkThemeColor() {
        try {
            CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();
            return parseColor(settings.getDarkThemeColor());
        } catch (Throwable ignored) {
            return DEFAULT_DARK_THEME_COLOR;
        }
    }

    /**
     * Parse hex color string (without #) to Color object.
     */
    private static Color parseColor(String hex) {
        try {
            return Color.decode("#" + hex);
        } catch (NumberFormatException e) {
            return DEFAULT_LIGHT_THEME_COLOR;
        }
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
