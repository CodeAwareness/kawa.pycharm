package com.codeawareness.pycharm.settings;

/**
 * Listener interface for Code Awareness settings changes.
 * Implementers will be notified when settings are modified.
 */
public interface SettingsChangeListener {

    /**
     * Called when highlight colors have been changed.
     */
    void onColorSettingsChanged();

    /**
     * Called when highlights are enabled or disabled.
     *
     * @param enabled true if highlights are now enabled, false otherwise
     */
    void onHighlightsEnabledChanged(boolean enabled);
}
