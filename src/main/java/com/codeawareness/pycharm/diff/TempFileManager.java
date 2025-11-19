package com.codeawareness.pycharm.diff;

import com.codeawareness.pycharm.utils.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages temporary files for peer code diffs.
 * Creates, tracks, and cleans up temp files.
 */
public class TempFileManager {

    private final String tmpDir;
    private final Map<String, File> tempFiles = new HashMap<>();

    public TempFileManager(String tmpDir) {
        this.tmpDir = tmpDir != null ? tmpDir : System.getProperty("java.io.tmpdir");
        ensureTempDirectoryExists();
    }

    /**
     * Ensure the temp directory exists.
     */
    private void ensureTempDirectoryExists() {
        try {
            Path dir = Paths.get(tmpDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                Logger.debug("Created temp directory: " + tmpDir);
            }
        } catch (IOException e) {
            Logger.warn("Failed to create temp directory: " + tmpDir, e);
        }
    }

    /**
     * Create a temporary file with peer content.
     *
     * @param fileName Original file name
     * @param peerName Peer name for identification
     * @param content File content
     * @return The created temp file
     * @throws IOException if file creation fails
     */
    public File createTempFile(String fileName, String peerName, String content) throws IOException {
        // Generate temp file name: original_name.peer_name.ext
        String tempFileName = generateTempFileName(fileName, peerName);
        File tempFile = new File(tmpDir, tempFileName);

        // If file exists and is read-only, make it writable and delete it
        if (tempFile.exists()) {
            if (!tempFile.canWrite()) {
                Logger.debug("Making existing temp file writable before deletion: " + tempFile.getAbsolutePath());
                tempFile.setWritable(true);
            }
            if (!tempFile.delete()) {
                Logger.warn("Failed to delete existing temp file: " + tempFile.getAbsolutePath());
                // Try to overwrite anyway
            }
        }

        // Write content
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }

        // Mark as read-only
        tempFile.setReadOnly();

        // Track for cleanup
        String key = fileName + ":" + peerName;
        tempFiles.put(key, tempFile);

        Logger.debug("Created temp file: " + tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * Generate a temporary file name.
     * Format: basename.peername.extension
     * Example: main.py -> main.alice.py
     */
    private String generateTempFileName(String fileName, String peerName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            String baseName = fileName.substring(0, lastDot);
            String extension = fileName.substring(lastDot);
            return baseName + "." + sanitizePeerName(peerName) + extension;
        } else {
            return fileName + "." + sanitizePeerName(peerName);
        }
    }

    /**
     * Sanitize peer name for use in file names.
     * Removes special characters and spaces.
     */
    private String sanitizePeerName(String peerName) {
        return peerName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Get the temp file for a specific file and peer.
     *
     * @param fileName Original file name
     * @param peerName Peer name
     * @return The temp file, or null if not found
     */
    public File getTempFile(String fileName, String peerName) {
        String key = fileName + ":" + peerName;
        return tempFiles.get(key);
    }

    /**
     * Clean up a specific temp file.
     *
     * @param fileName Original file name
     * @param peerName Peer name
     */
    public void cleanupTempFile(String fileName, String peerName) {
        String key = fileName + ":" + peerName;
        File tempFile = tempFiles.remove(key);
        if (tempFile != null && tempFile.exists()) {
            // Make writable if read-only
            if (!tempFile.canWrite()) {
                tempFile.setWritable(true);
            }
            if (tempFile.delete()) {
                Logger.debug("Deleted temp file: " + tempFile.getAbsolutePath());
            } else {
                Logger.warn("Failed to delete temp file: " + tempFile.getAbsolutePath());
            }
        }
    }

    /**
     * Clean up all temp files.
     */
    public void cleanupAll() {
        Logger.debug("Cleaning up " + tempFiles.size() + " temp files");
        for (File tempFile : tempFiles.values()) {
            if (tempFile != null && tempFile.exists()) {
                // Make writable if read-only
                if (!tempFile.canWrite()) {
                    tempFile.setWritable(true);
                }
                if (!tempFile.delete()) {
                    Logger.warn("Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
        tempFiles.clear();
    }

    /**
     * Get the temp directory path.
     */
    public String getTmpDir() {
        return tmpDir;
    }

    /**
     * Get the count of tracked temp files.
     */
    public int getTempFileCount() {
        return tempFiles.size();
    }
}
