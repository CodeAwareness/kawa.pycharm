package com.codeawareness.pycharm.monitoring;

import com.codeawareness.pycharm.CodeAwarenessApplicationService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.communication.MessageBuilder;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Monitors file changes and notifies the Code Awareness backend.
 * Implements debouncing to avoid flooding the backend with rapid changes.
 */
public class FileMonitor {

    private static final int DEFAULT_DEBOUNCE_DELAY_MS = 500;

    private final Project project;
    private final ScheduledExecutorService scheduler;
    private final Map<String, Runnable> pendingNotifications = new ConcurrentHashMap<>();
    private final int debounceDelayMs;

    public FileMonitor(Project project) {
        this(project, DEFAULT_DEBOUNCE_DELAY_MS);
    }

    public FileMonitor(Project project, int debounceDelayMs) {
        this.project = project;
        this.debounceDelayMs = debounceDelayMs;
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "CodeAwareness-FileMonitor");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Notify backend that a file was saved.
     * Debounced to avoid flooding the backend.
     */
    public void notifyFileSaved(VirtualFile file) {
        if (file == null || !file.isValid()) {
            return;
        }

        // Filter: only notify for files in the project
        if (!isProjectFile(file)) {
            Logger.debug("Skipping non-project file: " + file.getPath());
            return;
        }

        String filePath = file.getPath();
        String fileName = file.getName();

        Logger.debug("File saved: " + filePath);

        // Cancel any pending notification for this file
        Runnable existingTask = pendingNotifications.remove(filePath);
        if (existingTask != null) {
            Logger.trace("Cancelled pending notification for: " + filePath);
        }

        // Schedule new notification with debounce
        Runnable notificationTask = () -> {
            pendingNotifications.remove(filePath);
            sendFileSavedMessage(filePath, fileName);
        };

        pendingNotifications.put(filePath, notificationTask);
        scheduler.schedule(notificationTask, debounceDelayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Send file-saved message to backend.
     */
    private void sendFileSavedMessage(String filePath, String fileName) {
        CodeAwarenessApplicationService appService =
            ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);

        if (appService == null || !appService.isConnected()) {
            Logger.debug("Not connected - skipping file-saved notification");
            return;
        }

        try {
            Message message = MessageBuilder.buildFileSaved(
                appService.getClientGuid(),
                filePath,
                fileName
            );

            appService.getIpcConnection().sendMessage(message);
            Logger.debug("Sent file-saved notification: " + fileName);
        } catch (IOException e) {
            Logger.warn("Failed to send file-saved notification: " + e.getMessage());
        }
    }

    /**
     * Check if a file is part of the project (not temp, external, etc.).
     */
    private boolean isProjectFile(VirtualFile file) {
        if (file == null || !file.isValid()) {
            return false;
        }

        // Skip files outside project
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return false;
        }

        String filePath = file.getPath();
        if (!filePath.startsWith(projectBasePath)) {
            return false;
        }

        // Skip build/output directories
        if (filePath.contains("/build/") ||
            filePath.contains("/out/") ||
            filePath.contains("/.gradle/") ||
            filePath.contains("/.idea/")) {
            return false;
        }

        return true;
    }

    /**
     * Shutdown the file monitor and cancel pending notifications.
     */
    public void shutdown() {
        Logger.debug("Shutting down file monitor");
        pendingNotifications.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
