package com.codeawareness.pycharm.highlighting;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.intellij.openapi.project.Project;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HighlightManager.
 * Note: Full integration tests require IntelliJ Platform test fixtures.
 * These tests focus on the testable logic without full editor mocking.
 */
class HighlightManagerTest {

    @Test
    void testInitialState() {
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProject.getName()).thenReturn("TestProject");

        HighlightManager manager = new HighlightManager(mockProject);

        assertNotNull(manager, "Manager should be created");
        assertTrue(manager.isHighlightsEnabled(), "Highlights should be enabled by default");
        assertEquals(0, manager.getTotalHighlightCount(), "Initial highlight count should be 0");
    }

    @Test
    void testSetHighlightsEnabled() {
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProject.getName()).thenReturn("TestProject");

        HighlightManager manager = new HighlightManager(mockProject);

        // Disable highlights
        manager.setHighlightsEnabled(false);
        assertFalse(manager.isHighlightsEnabled(), "Highlights should be disabled");

        // Enable highlights
        manager.setHighlightsEnabled(true);
        assertTrue(manager.isHighlightsEnabled(), "Highlights should be enabled");
    }

    @Test
    void testToggleHighlights() {
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProject.getName()).thenReturn("TestProject");

        HighlightManager manager = new HighlightManager(mockProject);

        // Start enabled
        assertTrue(manager.isHighlightsEnabled());

        // Toggle to disabled
        manager.setHighlightsEnabled(false);
        assertFalse(manager.isHighlightsEnabled());

        // Toggle to enabled
        manager.setHighlightsEnabled(true);
        assertTrue(manager.isHighlightsEnabled());
    }

    @Test
    void testGetHighlightCount() {
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProject.getName()).thenReturn("TestProject");

        HighlightManager manager = new HighlightManager(mockProject);

        // Initially no highlights
        assertEquals(0, manager.getHighlightCount("/path/to/file.py"));
        assertEquals(0, manager.getTotalHighlightCount());
    }

    @Test
    void testClearAllHighlights() {
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProject.getName()).thenReturn("TestProject");

        HighlightManager manager = new HighlightManager(mockProject);

        // Clear should not throw even with no highlights
        assertDoesNotThrow(() -> manager.clearAllHighlights());
        assertEquals(0, manager.getTotalHighlightCount());
    }

    @Test
    void testClearHighlightsForFile() {
        Project mockProject = Mockito.mock(Project.class);
        Mockito.when(mockProject.getName()).thenReturn("TestProject");

        HighlightManager manager = new HighlightManager(mockProject);

        // Clear specific file should not throw
        assertDoesNotThrow(() -> manager.clearHighlights("/path/to/file.py"));
        assertEquals(0, manager.getHighlightCount("/path/to/file.py"));
    }

    /**
     * Note: Testing addHighlight() fully requires IntelliJ Platform test fixtures
     * because it needs a real or mocked Editor, Document, MarkupModel, etc.
     * These would be integration tests rather than unit tests.
     */
    @Test
    void testAddHighlightRequiresIntegrationTest() {
        // This is a placeholder to document that addHighlight() needs integration testing
        // with IntelliJ Platform test fixtures (LightPlatformTestCase, etc.)
        assertTrue(true, "addHighlight() requires integration testing with IntelliJ test fixtures");
    }
}
