package com.codeawareness.pycharm.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Whether Code Awareness highlighting is enabled (ON/OFF).
     * When OFF, highlights are hidden and not displayed.
     */
    public boolean highlightsEnabled = true;

    /**
     * Debounce delay for file save notifications (milliseconds).
     */
    public int fileSaveDebounceMs = 500;

    /**
     * Debounce delay for active file notifications (milliseconds).
     */
    public int activeFileDebounceMs = 300;

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
        this.highlightsEnabled = enabled;
    }
}
