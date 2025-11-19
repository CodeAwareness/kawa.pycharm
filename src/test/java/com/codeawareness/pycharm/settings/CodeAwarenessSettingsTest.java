package com.codeawareness.pycharm.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CodeAwarenessSettings.
 */
class CodeAwarenessSettingsTest {

    private CodeAwarenessSettings settings;

    @BeforeEach
    void setUp() {
        settings = new CodeAwarenessSettings();
    }

    @Test
    void testDefaultValues() {
        assertFalse(settings.isHighlightsEnabled(), "Highlights should NOT be enabled by default");
        assertEquals(500, settings.fileSaveDebounceMs, "File save debounce should be 500ms");
        assertEquals(300, settings.activeFileDebounceMs, "Active file debounce should be 300ms");
        assertEquals("ffea83", settings.getLightThemeColor(), "Light theme color should be default");
        assertEquals("0a071d", settings.getDarkThemeColor(), "Dark theme color should be default");
    }

    @Test
    void testToggleHighlights() {
        // Start with default (enabled)
        assertFalse(settings.isHighlightsEnabled());

        // Toggle OFF
        boolean newState = settings.toggleHighlights();
        assertTrue(newState, "Toggle should return new state (true)");
        assertTrue(settings.isHighlightsEnabled(), "Highlights should be enabled");

        // Toggle ON
        newState = settings.toggleHighlights();
        assertFalse(newState, "Toggle should return new state (false)");
        assertFalse(settings.isHighlightsEnabled(), "Highlights should be disabled");
    }

    @Test
    void testSetHighlightsEnabled() {
        // Set to false
        settings.setHighlightsEnabled(false);
        assertFalse(settings.isHighlightsEnabled(), "Highlights should be disabled");

        // Set to true
        settings.setHighlightsEnabled(true);
        assertTrue(settings.isHighlightsEnabled(), "Highlights should be enabled");
    }

    @Test
    void testGetState() {
        CodeAwarenessSettings state = settings.getState();
        assertNotNull(state, "State should not be null");
        assertSame(settings, state, "getState should return the same instance");
    }

    @Test
    void testLoadState() {
        // Create a new settings instance with custom values
        CodeAwarenessSettings newSettings = new CodeAwarenessSettings();
        newSettings.highlightsEnabled = false;
        newSettings.fileSaveDebounceMs = 1000;
        newSettings.activeFileDebounceMs = 600;

        // Load state into our settings
        settings.loadState(newSettings);

        // Verify values were copied
        assertFalse(settings.highlightsEnabled, "Highlights should be disabled after load");
        assertEquals(1000, settings.fileSaveDebounceMs, "File save debounce should be 1000ms");
        assertEquals(600, settings.activeFileDebounceMs, "Active file debounce should be 600ms");
    }

    @Test
    void testDebounceSettings() {
        // Test file save debounce
        settings.fileSaveDebounceMs = 750;
        assertEquals(750, settings.fileSaveDebounceMs);

        // Test active file debounce
        settings.activeFileDebounceMs = 200;
        assertEquals(200, settings.activeFileDebounceMs);
    }

    @Test
    void testColorSettings() {
        // Test light theme color
        settings.setLightThemeColor("ff0000");
        assertEquals("ff0000", settings.getLightThemeColor(), "Light theme color should be red");

        // Test dark theme color
        settings.setDarkThemeColor("00ff00");
        assertEquals("00ff00", settings.getDarkThemeColor(), "Dark theme color should be green");
    }

    @Test
    void testResetColorsToDefaults() {
        // Change colors
        settings.setLightThemeColor("111111");
        settings.setDarkThemeColor("222222");

        // Reset to defaults
        settings.resetColorsToDefaults();

        // Verify defaults are restored
        assertEquals("ffea83", settings.getLightThemeColor(), "Light theme color should be reset to default");
        assertEquals("0a071d", settings.getDarkThemeColor(), "Dark theme color should be reset to default");
    }

    @Test
    void testColorNullHandling() {
        // Test that null values return defaults
        settings.lightThemeColor = null;
        settings.darkThemeColor = null;

        assertEquals("ffea83", settings.getLightThemeColor(), "Should return default when light color is null");
        assertEquals("0a071d", settings.getDarkThemeColor(), "Should return default when dark color is null");
    }

    @Test
    void testAddAndRemoveSettingsChangeListener() {
        // Create a mock listener
        TestSettingsChangeListener listener = new TestSettingsChangeListener();

        // Add listener
        settings.addSettingsChangeListener(listener);

        // Change color - should trigger listener
        settings.setLightThemeColor("ff0000");
        assertTrue(listener.colorChangedCalled, "Listener should be notified of color change");

        // Reset flag
        listener.colorChangedCalled = false;

        // Remove listener
        settings.removeSettingsChangeListener(listener);

        // Change color again - should NOT trigger listener
        settings.setLightThemeColor("00ff00");
        assertFalse(listener.colorChangedCalled, "Listener should not be notified after removal");
    }

    @Test
    void testColorChangeNotification() {
        TestSettingsChangeListener listener = new TestSettingsChangeListener();
        settings.addSettingsChangeListener(listener);

        // Change light theme color
        settings.setLightThemeColor("aabbcc");
        assertTrue(listener.colorChangedCalled, "Should notify on light color change");

        // Reset flag
        listener.colorChangedCalled = false;

        // Change dark theme color
        settings.setDarkThemeColor("ddeeff");
        assertTrue(listener.colorChangedCalled, "Should notify on dark color change");

        // Reset flag
        listener.colorChangedCalled = false;

        // Reset colors to defaults
        settings.resetColorsToDefaults();
        assertTrue(listener.colorChangedCalled, "Should notify on color reset");

        settings.removeSettingsChangeListener(listener);
    }

    @Test
    void testHighlightsEnabledNotification() {
        TestSettingsChangeListener listener = new TestSettingsChangeListener();
        settings.addSettingsChangeListener(listener);

        // Enable highlights
        settings.setHighlightsEnabled(true);
        assertTrue(listener.highlightsEnabledChangedCalled, "Should notify when highlights enabled");
        assertTrue(listener.lastEnabledValue, "Enabled value should be true");

        // Reset flag
        listener.highlightsEnabledChangedCalled = false;

        // Disable highlights
        settings.setHighlightsEnabled(false);
        assertTrue(listener.highlightsEnabledChangedCalled, "Should notify when highlights disabled");
        assertFalse(listener.lastEnabledValue, "Enabled value should be false");

        settings.removeSettingsChangeListener(listener);
    }

    @Test
    void testListenerNotCalledOnSameValue() {
        TestSettingsChangeListener listener = new TestSettingsChangeListener();
        settings.addSettingsChangeListener(listener);

        // Set light color to a value
        settings.setLightThemeColor("aabbcc");
        assertTrue(listener.colorChangedCalled, "Should notify on first change");

        // Reset flag
        listener.colorChangedCalled = false;

        // Set to same value - should NOT trigger
        settings.setLightThemeColor("aabbcc");
        assertFalse(listener.colorChangedCalled, "Should not notify when value is unchanged");

        // Reset flag
        listener.highlightsEnabledChangedCalled = false;

        // Set highlights to same value - should NOT trigger
        settings.setHighlightsEnabled(false);
        assertFalse(listener.highlightsEnabledChangedCalled, "Should not notify when enabled state is unchanged");

        settings.removeSettingsChangeListener(listener);
    }

    @Test
    void testMultipleListeners() {
        TestSettingsChangeListener listener1 = new TestSettingsChangeListener();
        TestSettingsChangeListener listener2 = new TestSettingsChangeListener();

        settings.addSettingsChangeListener(listener1);
        settings.addSettingsChangeListener(listener2);

        // Change color
        settings.setLightThemeColor("123456");

        // Both listeners should be notified
        assertTrue(listener1.colorChangedCalled, "Listener 1 should be notified");
        assertTrue(listener2.colorChangedCalled, "Listener 2 should be notified");

        settings.removeSettingsChangeListener(listener1);
        settings.removeSettingsChangeListener(listener2);
    }

    /**
     * Test implementation of SettingsChangeListener for testing purposes.
     */
    private static class TestSettingsChangeListener implements SettingsChangeListener {
        boolean colorChangedCalled = false;
        boolean highlightsEnabledChangedCalled = false;
        boolean lastEnabledValue = false;

        @Override
        public void onColorSettingsChanged() {
            colorChangedCalled = true;
        }

        @Override
        public void onHighlightsEnabledChanged(boolean enabled) {
            highlightsEnabledChangedCalled = true;
            lastEnabledValue = enabled;
        }
    }
}
