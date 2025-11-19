package com.codeawareness.pycharm.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Settings configurable for Code Awareness plugin.
 * Provides UI for plugin configuration in PyCharm settings.
 */
public class CodeAwarenessConfigurable implements Configurable {

    private ColorPanel lightThemeColorPanel;
    private ColorPanel darkThemeColorPanel;
    private JPanel mainPanel;

    @Nls
    @Override
    public String getDisplayName() {
        return "Code Awareness";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();

        // Create color panels
        lightThemeColorPanel = new ColorPanel();
        lightThemeColorPanel.setSelectedColor(parseColor(settings.getLightThemeColor()));

        darkThemeColorPanel = new ColorPanel();
        darkThemeColorPanel.setSelectedColor(parseColor(settings.getDarkThemeColor()));

        // Create reset button
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> {
            lightThemeColorPanel.setSelectedColor(parseColor("ffea83"));
            darkThemeColorPanel.setSelectedColor(parseColor("0a071d"));
        });

        // Build the form
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Light theme highlight color:"), lightThemeColorPanel, 1, false)
            .addLabeledComponent(new JBLabel("Dark theme highlight color:"), darkThemeColorPanel, 1, false)
            .addComponentFillVertically(new JPanel(), 0)
            .addComponent(resetButton)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();

        mainPanel.setBorder(JBUI.Borders.empty(10));

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();

        Color currentLightColor = parseColor(settings.getLightThemeColor());
        Color currentDarkColor = parseColor(settings.getDarkThemeColor());

        Color selectedLightColor = lightThemeColorPanel.getSelectedColor();
        Color selectedDarkColor = darkThemeColorPanel.getSelectedColor();

        return !currentLightColor.equals(selectedLightColor) ||
               !currentDarkColor.equals(selectedDarkColor);
    }

    @Override
    public void apply() {
        CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();

        settings.setLightThemeColor(colorToHex(lightThemeColorPanel.getSelectedColor()));
        settings.setDarkThemeColor(colorToHex(darkThemeColorPanel.getSelectedColor()));
    }

    @Override
    public void reset() {
        CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();
        lightThemeColorPanel.setSelectedColor(parseColor(settings.getLightThemeColor()));
        darkThemeColorPanel.setSelectedColor(parseColor(settings.getDarkThemeColor()));
    }

    /**
     * Parse hex color string (without #) to Color object.
     */
    private Color parseColor(String hex) {
        try {
            return Color.decode("#" + hex);
        } catch (NumberFormatException e) {
            return new Color(0xff, 0xea, 0x83); // Default to light theme color
        }
    }

    /**
     * Convert Color object to hex string (without #).
     */
    private String colorToHex(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
