package com.codeawareness.pycharm.utils;

/**
 * Logging utility for Code Awareness plugin.
 * Wraps IntelliJ's Logger for consistent logging throughout the plugin.
 */
public class Logger {

    private static final com.intellij.openapi.diagnostic.Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Logger.class);
    private static boolean debugEnabled = false;

    /**
     * Enable or disable debug logging.
     */
    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Check if debug logging is enabled.
     */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Log an error message.
     */
    public static void error(String message) {
        LOG.error("[Code Awareness] " + message);
    }

    /**
     * Log an error message with exception.
     */
    public static void error(String message, Throwable throwable) {
        LOG.error("[Code Awareness] " + message, throwable);
    }

    /**
     * Log a warning message.
     */
    public static void warn(String message) {
        LOG.warn("[Code Awareness] " + message);
    }

    /**
     * Log a warning message with exception.
     */
    public static void warn(String message, Throwable throwable) {
        LOG.warn("[Code Awareness] " + message, throwable);
    }

    /**
     * Log an info message.
     */
    public static void info(String message) {
        LOG.info("[Code Awareness] " + message);
    }

    /**
     * Log a debug message (only if debug is enabled).
     */
    public static void debug(String message) {
        if (debugEnabled) {
            LOG.debug("[Code Awareness] " + message);
        }
    }

    /**
     * Log a debug message with exception (only if debug is enabled).
     */
    public static void debug(String message, Throwable throwable) {
        if (debugEnabled) {
            LOG.debug("[Code Awareness] " + message, throwable);
        }
    }

    /**
     * Log a trace message (only if debug is enabled).
     */
    public static void trace(String message) {
        if (debugEnabled) {
            LOG.trace("[Code Awareness] " + message);
        }
    }
}
