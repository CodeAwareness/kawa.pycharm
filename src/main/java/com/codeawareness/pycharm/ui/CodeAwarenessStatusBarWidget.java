package com.codeawareness.pycharm.ui;

import com.codeawareness.pycharm.CodeAwarenessApplicationService;
import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.settings.CodeAwarenessSettings;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

/**
 * Status bar widget factory and implementation for Code Awareness.
 * Shows the current mode (ON/OFF) and allows toggling via click.
 */
public class CodeAwarenessStatusBarWidget implements StatusBarWidgetFactory {

    private static final String ID = "CodeAwarenessStatus";

    @Override
    public @NonNls @NotNull String getId() {
        return ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Code Awareness";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new CodeAwarenessWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        // Cleanup if needed
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

    /**
     * The actual status bar widget implementation.
     */
    private static class CodeAwarenessWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

        private final Project project;
        private StatusBar statusBar;

        public CodeAwarenessWidget(Project project) {
            this.project = project;
        }

        @Override
        public @NonNls @NotNull String ID() {
            return ID;
        }

        @Override
        public void install(@NotNull StatusBar statusBar) {
            this.statusBar = statusBar;
        }

        @Override
        public void dispose() {
            this.statusBar = null;
        }

        @Override
        public @Nullable WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @NotNull String getText() {
            CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();
            return settings.isHighlightsEnabled() ? "Code Awareness: ON" : "Code Awareness: OFF";
        }

        @Override
        public @NotNull String getTooltipText() {
            CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();
            return settings.isHighlightsEnabled()
                ? "Click to turn OFF Code Awareness highlights"
                : "Click to turn ON Code Awareness highlights";
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return (MouseEvent event) -> {
                // Toggle the mode
                CodeAwarenessSettings settings = CodeAwarenessSettings.getInstance();
                boolean newState = settings.toggleHighlights();

                Logger.info("Code Awareness toggled to: " + (newState ? "ON" : "OFF"));

                // Update highlight manager immediately (UI operation)
                CodeAwarenessProjectService projectService =
                    project.getService(CodeAwarenessProjectService.class);

                if (projectService != null) {
                    projectService.getHighlightManager().setHighlightsEnabled(newState);
                }

                // Show notification
                if (newState) {
                    NotificationHelper.notifyEnabled(project);
                } else {
                    NotificationHelper.notifyDisabled(project);
                }

                // Update status bar text
                if (statusBar != null) {
                    statusBar.updateWidget(ID);
                }

                // Connect/disconnect from Gardener asynchronously (non-blocking)
                CodeAwarenessApplicationService appService =
                    ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);

                if (appService != null) {
                    if (newState) {
                        // Enable highlights - connect to Gardener asynchronously
                        if (!appService.isConnected()) {
                            Logger.info("Highlights enabled - connecting to Code Awareness backend asynchronously...");
                            // Run connection on background thread to avoid blocking EDT and accessing APIs too early
                            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                                try {
                                    appService.connect();
                                    Logger.info("Successfully connected to Code Awareness backend");
                                    
                                    // Update status bar on EDT after successful connection
                                    ApplicationManager.getApplication().invokeLater(() -> {
                                        if (statusBar != null) {
                                            statusBar.updateWidget(ID);
                                        }
                                    });
                                } catch (Exception e) {
                                    Logger.error("Failed to connect to Code Awareness backend when enabling highlights", e);
                                    // Don't throw - allow highlights to be enabled even if connection fails
                                    // The connection might fail if Gardener is not running, which is OK
                                }
                            });
                        } else {
                            Logger.info("Already connected to Code Awareness backend");
                        }
                    } else {
                        // Disable highlights - optionally disconnect (or keep connection for other features)
                        Logger.info("Highlights disabled (keeping connection active)");
                        // Note: We don't disconnect here to allow re-enabling without reconnection overhead
                    }
                } else {
                    Logger.warn("Code Awareness Application Service not available");
                }
            };
        }

        @Override
        public float getAlignment() {
            return 0.0f;  // Left-align
        }
    }
}
