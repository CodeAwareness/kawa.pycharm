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
        assertTrue(settings.isHighlightsEnabled(), "Highlights should be enabled by default");
        assertEquals(500, settings.fileSaveDebounceMs, "File save debounce should be 500ms");
        assertEquals(300, settings.activeFileDebounceMs, "Active file debounce should be 300ms");
    }

    @Test
    void testToggleHighlights() {
        // Start with default (enabled)
        assertTrue(settings.isHighlightsEnabled());

        // Toggle OFF
        boolean newState = settings.toggleHighlights();
        assertFalse(newState, "Toggle should return new state (false)");
        assertFalse(settings.isHighlightsEnabled(), "Highlights should be disabled");

        // Toggle ON
        newState = settings.toggleHighlights();
        assertTrue(newState, "Toggle should return new state (true)");
        assertTrue(settings.isHighlightsEnabled(), "Highlights should be enabled");
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
}
