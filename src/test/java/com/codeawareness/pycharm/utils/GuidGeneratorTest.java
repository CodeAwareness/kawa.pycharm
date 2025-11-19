package com.codeawareness.pycharm.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GuidGenerator.
 */
class GuidGeneratorTest {

    @Test
    void testGenerateFormat() {
        String guid = GuidGenerator.generate();
        assertNotNull(guid);
        assertTrue(guid.matches("\\d{6}-\\d{6}"), "GUID should match format XXXXXX-XXXXXX");
    }

    @Test
    void testGenerateUniqueness() {
        Set<String> guids = new HashSet<>();
        int count = 1000;

        for (int i = 0; i < count; i++) {
            String guid = GuidGenerator.generate();
            guids.add(guid);
        }

        // All generated GUIDs should be unique
        assertEquals(count, guids.size(), "All generated GUIDs should be unique");
    }

    @Test
    void testIsValid() {
        assertTrue(GuidGenerator.isValid("123456-789012"));
        assertTrue(GuidGenerator.isValid("000000-000000"));
        assertTrue(GuidGenerator.isValid("999999-999999"));

        assertFalse(GuidGenerator.isValid(null));
        assertFalse(GuidGenerator.isValid(""));
        assertFalse(GuidGenerator.isValid("12345-678901")); // Too short
        assertFalse(GuidGenerator.isValid("1234567-7890123")); // Too long
        assertFalse(GuidGenerator.isValid("123456_789012")); // Wrong separator
        assertFalse(GuidGenerator.isValid("abcdef-123456")); // Contains letters
    }

    @Test
    void testGenerateProducesValidGuids() {
        for (int i = 0; i < 100; i++) {
            String guid = GuidGenerator.generate();
            assertTrue(GuidGenerator.isValid(guid), "Generated GUID should be valid: " + guid);
        }
    }
}
