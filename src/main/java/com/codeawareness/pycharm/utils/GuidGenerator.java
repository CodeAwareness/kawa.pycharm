package com.codeawareness.pycharm.utils;

import java.security.SecureRandom;

/**
 * Generates unique GUIDs for Code Awareness client identification.
 * Format: XXXXXX-XXXXXX (6 digits, hyphen, 6 digits)
 */
public class GuidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SEGMENT_LENGTH = 6;

    /**
     * Generate a unique GUID for the client.
     * Format: XXXXXX-XXXXXX
     */
    public static String generate() {
        String segment1 = generateSegment();
        String segment2 = generateSegment();
        return segment1 + "-" + segment2;
    }

    /**
     * Generate a 6-digit numeric segment.
     */
    private static String generateSegment() {
        int number = RANDOM.nextInt(1000000); // 0 to 999999
        return String.format("%06d", number);
    }

    /**
     * Validate GUID format.
     */
    public static boolean isValid(String guid) {
        if (guid == null) {
            return false;
        }
        return guid.matches("\\d{6}-\\d{6}");
    }
}
