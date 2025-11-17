package com.codeawareness.pycharm.monitoring;

import com.codeawareness.pycharm.CodeAwarenessApplicationService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.communication.MessageBuilder;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks the currently active file in the editor.
 * Sends active-path notifications to the Code Awareness backend when the active file changes.
 */
public class ActiveFileTracker implements FileEditorManagerListener {

    private final Project project;
    private final ScheduledExecutorService scheduler;
    private final long debounceDelayMs;
    private final AtomicReference<String> currentActiveFile = new AtomicReference<>(null);
    private final AtomicReference<Runnable> pendingNotification = new AtomicReference<>(null);

    /**
     * Create a new active file tracker with default debounce delay (300ms).
     */
    public ActiveFileTracker(Project project) {
        this(project, 300);
    }

    /**
     * Create a new active file tracker with custom debounce delay.
     */
    public ActiveFileTracker(Project project, long debounceDelayMs) {
        this.project = project;
        this.debounceDelayMs = debounceDelayMs;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "ActiveFileTracker-" + project.getName());
            thread.setDaemon(true);
            return thread;
        });

        // Track the currently selected file on initialization
        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            if (selectedFiles.length > 0) {
                notifyActiveFileChanged(selectedFiles[0]);
            }
        });
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile newFile = event.getNewFile();
        if (newFile != null && newFile.isValid()) {
            notifyActiveFileChanged(newFile);
        } else {
            // No file selected (all files closed)
            notifyActiveFileChanged(null);
        }
    }

    /**
     * Notify that the active file has changed.
     * This is debounced to avoid excessive notifications.
     */
    public void notifyActiveFileChanged(VirtualFile file) {
        String filePath = (file != null && file.isValid()) ? file.getPath() : null;

        // Check if this is actually a change
        String previousFile = currentActiveFile.get();
        if (filePath != null && filePath.equals(previousFile)) {
            return; // No change
        }

        // Filter out non-project files
        if (file != null && !isProjectFile(file)) {
            Logger.debug("Skipping non-project file: " + filePath);
            return;
        }

        Logger.debug("Active file changed: " + (filePath != null ? filePath : "<none>"));

        // Update current active file
        currentActiveFile.set(filePath);

        // Cancel any pending notification
        Runnable existingTask = pendingNotification.getAndSet(null);

        // Schedule new notification with debounce
        Runnable notificationTask = () -> {
            pendingNotification.set(null);
            sendActivePathMessage(filePath, file != null ? file.getName() : null);
        };

        pendingNotification.set(notificationTask);
        scheduler.schedule(notificationTask, debounceDelayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Check if the file belongs to this project.
     */
    private boolean isProjectFile(VirtualFile file) {
        String projectBasePath = project.getBasePath();
        if (projectBasePath == null) {
            return false;
        }

        String filePath = file.getPath();

        // Must be under project directory
        if (!filePath.startsWith(projectBasePath)) {
            return false;
        }

        // Filter out common build/generated directories
        String relativePath = filePath.substring(projectBasePath.length());
        if (relativePath.startsWith("/build/") ||
            relativePath.startsWith("/out/") ||
            relativePath.startsWith("/.gradle/") ||
            relativePath.startsWith("/.idea/") ||
            relativePath.startsWith("/target/")) {
            return false;
        }

        return true;
    }

    /**
     * Send active-path message to backend.
     */
    private void sendActivePathMessage(String filePath, String fileName) {
        try {
            CodeAwarenessApplicationService appService =
                ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);

            if (appService == null || !appService.isConnected()) {
                Logger.debug("Cannot send active-path: not connected");
                return;
            }

            // Build message
            MessageBuilder builder = new MessageBuilder()
                .setAction("active-path")
                .setFlow("request");

            if (filePath != null) {
                builder.addDataField("path", filePath);
                if (fileName != null) {
                    builder.addDataField("name", fileName);
                }
            }

            Message message = builder.build();

            // Send via IPC connection
            if (appService.getIpcConnection() != null) {
                appService.getIpcConnection().send(message);
                Logger.debug("Sent active-path message: " + (filePath != null ? filePath : "<none>"));
            }

        } catch (Exception e) {
            Logger.warn("Failed to send active-path message", e);
        }
    }

    /**
     * Get the currently active file path.
     */
    public String getCurrentActiveFile() {
        return currentActiveFile.get();
    }

    /**
     * Shutdown the tracker and clean up resources.
     */
    public void shutdown() {
        Logger.debug("Shutting down ActiveFileTracker for project: " + project.getName());

        // Cancel any pending notification
        pendingNotification.set(null);

        // Shutdown scheduler
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
