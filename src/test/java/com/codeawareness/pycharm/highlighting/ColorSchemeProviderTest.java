package com.codeawareness.pycharm.highlighting;

import com.intellij.ui.JBColor;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ColorSchemeProvider.
 */
class ColorSchemeProviderTest {

    @Test
    void testGetHighlightJBColor() {
        JBColor color = ColorSchemeProvider.getHighlightJBColor();
        assertNotNull(color, "Highlight color should not be null");
    }
}
