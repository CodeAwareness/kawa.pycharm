package com.codeawareness.pycharm.startup;

import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Delayed startup activity for Code Awareness plugin.
 * Waits for the IDE to fully initialize before activating the plugin.
 * This helps avoid conflicts with other plugins (like Gradle) that may not be ready immediately.
 */
public class CodeAwarenessStartupActivity implements StartupActivity.DumbAware {

    private static final long INITIALIZATION_DELAY_MS = 2000; // 2 second delay

    @Override
    public void runActivity(@NotNull Project project) {
        Logger.info("Code Awareness startup activity triggered for project: " + project.getName());

        // Delay initialization to allow IDE and other plugins to fully initialize
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                Logger.info("Waiting " + INITIALIZATION_DELAY_MS + "ms before initializing Code Awareness plugin...");
                Thread.sleep(INITIALIZATION_DELAY_MS);

                // Run on EDT after delay
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (project.isDisposed()) {
                        Logger.warn("Project disposed before Code Awareness initialization");
                        return;
                    }

                    Logger.info("Initializing Code Awareness plugin for project: " + project.getName());
                    // The project service will be initialized automatically when accessed
                    // We just need to ensure it exists
                    project.getService(com.codeawareness.pycharm.CodeAwarenessProjectService.class);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.warn("Code Awareness startup activity interrupted");
            } catch (Exception e) {
                Logger.error("Error in Code Awareness startup activity", e);
            }
        });
    }
}

