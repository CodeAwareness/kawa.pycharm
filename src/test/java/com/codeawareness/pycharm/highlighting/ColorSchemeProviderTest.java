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

        // Verify light theme color (yellow)
        Color lightColor = new Color(0xff, 0xdd, 0x34);
        assertEquals(lightColor, color, "Light theme color should be yellow (#ffdd34)");
    }

    @Test
    void testGetHighlightColor() {
        Color color = ColorSchemeProvider.getHighlightColor();
        assertNotNull(color, "Highlight color should not be null");

        // Color should be either yellow (light) or blue (dark)
        Color yellow = new Color(0xff, 0xdd, 0x34);
        Color blue = new Color(0x1f, 0x1c, 0xc2);

        assertTrue(
            color.equals(yellow) || color.equals(blue),
            "Color should be either yellow (#ffdd34) or blue (#1f1cc2)"
        );
    }

    @Test
    void testColorComponents() {
        // Test that our predefined colors have the correct RGB components
        Color yellow = new Color(0xff, 0xdd, 0x34);
        assertEquals(255, yellow.getRed(), "Yellow red component");
        assertEquals(221, yellow.getGreen(), "Yellow green component");
        assertEquals(52, yellow.getBlue(), "Yellow blue component");

        Color blue = new Color(0x1f, 0x1c, 0xc2);
        assertEquals(31, blue.getRed(), "Blue red component");
        assertEquals(28, blue.getGreen(), "Blue green component");
        assertEquals(194, blue.getBlue(), "Blue blue component");
    }
}
