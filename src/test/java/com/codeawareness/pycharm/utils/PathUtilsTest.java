package com.codeawareness.pycharm.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PathUtils.
 */
class PathUtilsTest {

    @Test
    void testExpandHome() {
        String home = PathUtils.getHomeDirectory();

        assertEquals(home + "/foo", PathUtils.expandHome("~/foo"));
        assertEquals(home, PathUtils.expandHome("~"));
        assertEquals("/absolute/path", PathUtils.expandHome("/absolute/path"));
        assertEquals("relative/path", PathUtils.expandHome("relative/path"));
        assertNull(PathUtils.expandHome(null));
    }

    @Test
    void testGetCatalogSocketPath() {
        String path = PathUtils.getCatalogSocketPath();
        assertNotNull(path);

        if (PathUtils.isWindows()) {
            assertTrue(path.startsWith("\\\\.\\pipe\\"));
            assertTrue(path.contains("caw.catalog"));
        } else {
            assertTrue(path.contains(".kawa-code/sockets"));
            assertTrue(path.endsWith("caw.catalog"));
        }
    }

    @Test
    void testGetIpcSocketPath() {
        String guid = "123456-789012";
        String path = PathUtils.getIpcSocketPath(guid);
        assertNotNull(path);

        if (PathUtils.isWindows()) {
            assertEquals("\\\\.\\pipe\\caw." + guid, path);
        } else {
            assertTrue(path.contains(".kawa-code/sockets"));
            assertTrue(path.endsWith("caw." + guid));
        }
    }

    @Test
    void testGetIpcSocketPathNullGuid() {
        assertThrows(IllegalArgumentException.class, () -> {
            PathUtils.getIpcSocketPath(null);
        });
    }

    @Test
    void testNormalizePath() {
        assertNotNull(PathUtils.normalizePath("/foo/bar"));
        assertNotNull(PathUtils.normalizePath("~/foo/bar"));
        assertNull(PathUtils.normalizePath(null));
    }

    @Test
    void testGetHomeDirectory() {
        String home = PathUtils.getHomeDirectory();
        assertNotNull(home);
        assertFalse(home.isEmpty());
    }
}
