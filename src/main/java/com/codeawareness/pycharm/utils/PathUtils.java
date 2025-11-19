package com.codeawareness.pycharm.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities for path handling and normalization across platforms.
 */
public class PathUtils {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String HOME_DIR = System.getProperty("user.home");

    /**
     * Check if running on Windows.
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * Expand tilde (~) in path to home directory.
     * Example: "~/foo" -> "/home/user/foo"
     */
    public static String expandHome(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("~/") || path.equals("~")) {
            return path.replaceFirst("^~", HOME_DIR);
        }
        return path;
    }

    /**
     * Get the Code Awareness socket directory path.
     * Unix/Linux/macOS: ~/.kawa-code/sockets/
     * Windows: Uses named pipes (no directory needed)
     */
    public static String getSocketDirectory() {
        if (IS_WINDOWS) {
            return null; // Windows uses named pipes, not files
        }
        return expandHome("~/.kawa-code/sockets");
    }

    /**
     * Get the catalog socket path.
     * Unix/Linux/macOS: ~/.kawa-code/sockets/caw.catalog
     * Windows: \\.\pipe\caw.catalog
     */
    public static String getCatalogSocketPath() {
        if (IS_WINDOWS) {
            return "\\\\.\\pipe\\caw.catalog";
        }
        return expandHome("~/.kawa-code/sockets/caw.catalog");
    }

    /**
     * Get the IPC socket path for a given client GUID.
     * Unix/Linux/macOS: ~/.kawa-code/sockets/caw.<GUID>
     * Windows: \\.\pipe\caw.<GUID>
     */
    public static String getIpcSocketPath(String clientGuid) {
        if (clientGuid == null) {
            throw new IllegalArgumentException("Client GUID cannot be null");
        }
        if (IS_WINDOWS) {
            return "\\\\.\\pipe\\caw." + clientGuid;
        }
        return expandHome("~/.kawa-code/sockets/caw." + clientGuid);
    }

    /**
     * Normalize a file path for the current platform.
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        path = expandHome(path);
        return Paths.get(path).normalize().toString();
    }

    /**
     * Check if a file exists.
     */
    public static boolean exists(String path) {
        if (path == null) {
            return false;
        }
        return new File(path).exists();
    }

    /**
     * Create directories if they don't exist.
     */
    public static boolean ensureDirectoryExists(String path) {
        if (path == null) {
            return false;
        }
        File dir = new File(path);
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }

    /**
     * Get the home directory.
     */
    public static String getHomeDirectory() {
        return HOME_DIR;
    }

    /**
     * Convert a file path to a URI-compatible format.
     */
    public static String toUri(String path) {
        if (path == null) {
            return null;
        }
        path = normalizePath(path);
        return Paths.get(path).toUri().toString();
    }
}
