package com.codeawareness.pycharm.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent settings for Code Awareness plugin.
 * Stores user preferences across IDE restarts.
 */
@State(
    name = "CodeAwarenessSettings",
    storages = @Storage("CodeAwareness.xml")
)
@Service(Service.Level.APP)
public final class CodeAwarenessSettings implements PersistentStateComponent<CodeAwarenessSettings> {

    // Default colors
    private static final String DEFAULT_LIGHT_COLOR = "ffea83";
    private static final String DEFAULT_DARK_COLOR = "0a071d";

    // Settings change listeners
    private final List<SettingsChangeListener> listeners = new ArrayList<>();

    /**
     * Whether Code Awareness highlighting is enabled (ON/OFF).
     * When OFF, highlights are hidden and not displayed.
     * Default is OFF - user must explicitly enable it via status bar widget.
     */
    public boolean highlightsEnabled = false;

    /**
     * Debounce delay for file save notifications (milliseconds).
     */
    public int fileSaveDebounceMs = 500;

    /**
     * Debounce delay for active file notifications (milliseconds).
     */
    public int activeFileDebounceMs = 300;

    /**
     * Highlight color for light theme (RGB hex format without #, e.g., "ffea83").
     */
    public String lightThemeColor = DEFAULT_LIGHT_COLOR;

    /**
     * Highlight color for dark theme (RGB hex format without #, e.g., "0a071d").
     */
    public String darkThemeColor = DEFAULT_DARK_COLOR;

    /**
     * Get the application-level settings instance.
     */
    public static CodeAwarenessSettings getInstance() {
        return ApplicationManager.getApplication().getService(CodeAwarenessSettings.class);
    }

    @Override
    public @Nullable CodeAwarenessSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CodeAwarenessSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    /**
     * Toggle highlights ON/OFF and return the new state.
     *
     * @return true if highlights are now ON, false if OFF
     */
    public boolean toggleHighlights() {
        highlightsEnabled = !highlightsEnabled;
        return highlightsEnabled;
    }

    /**
     * Check if highlights are currently enabled.
     */
    public boolean isHighlightsEnabled() {
        return highlightsEnabled;
    }

    /**
     * Enable or disable highlights.
     */
    public void setHighlightsEnabled(boolean enabled) {
        if (this.highlightsEnabled != enabled) {
            this.highlightsEnabled = enabled;
            notifyHighlightsEnabledChanged(enabled);
        }
    }

    /**
     * Get the light theme highlight color.
     */
    public String getLightThemeColor() {
        return lightThemeColor != null ? lightThemeColor : DEFAULT_LIGHT_COLOR;
    }

    /**
     * Set the light theme highlight color.
     */
    public void setLightThemeColor(String color) {
        if (this.lightThemeColor == null || !this.lightThemeColor.equals(color)) {
            this.lightThemeColor = color;
            notifyColorSettingsChanged();
        }
    }

    /**
     * Get the dark theme highlight color.
     */
    public String getDarkThemeColor() {
        return darkThemeColor != null ? darkThemeColor : DEFAULT_DARK_COLOR;
    }

    /**
     * Set the dark theme highlight color.
     */
    public void setDarkThemeColor(String color) {
        if (this.darkThemeColor == null || !this.darkThemeColor.equals(color)) {
            this.darkThemeColor = color;
            notifyColorSettingsChanged();
        }
    }

    /**
     * Reset colors to defaults.
     */
    public void resetColorsToDefaults() {
        this.lightThemeColor = DEFAULT_LIGHT_COLOR;
        this.darkThemeColor = DEFAULT_DARK_COLOR;
        notifyColorSettingsChanged();
    }

    /**
     * Register a settings change listener.
     */
    public void addSettingsChangeListener(SettingsChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregister a settings change listener.
     */
    public void removeSettingsChangeListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that color settings have changed.
     */
    private void notifyColorSettingsChanged() {
        for (SettingsChangeListener listener : listeners) {
            try {
                listener.onColorSettingsChanged();
            } catch (Exception e) {
                // Ignore listener exceptions
            }
        }
    }

    /**
     * Notify all listeners that highlights enabled state has changed.
     */
    private void notifyHighlightsEnabledChanged(boolean enabled) {
        for (SettingsChangeListener listener : listeners) {
            try {
                listener.onHighlightsEnabledChanged(enabled);
            } catch (Exception e) {
                // Ignore listener exceptions
            }
        }
    }
}
