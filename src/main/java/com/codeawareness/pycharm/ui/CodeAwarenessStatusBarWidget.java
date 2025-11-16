package com.codeawareness.pycharm.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Status bar widget factory for Code Awareness.
 * Displays connection and authentication status in the IDE status bar.
 */
public class CodeAwarenessStatusBarWidget implements StatusBarWidgetFactory {

    @Override
    public @NonNls @NotNull String getId() {
        return "CodeAwarenessStatusBar";
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
        return new StatusBarWidget() {
            @Override
            public @NonNls @NotNull String ID() {
                return "CodeAwarenessStatusBar";
            }

            @Override
            public void install(@NotNull StatusBar statusBar) {
                // Implementation will be added in Phase 3.2
            }

            @Override
            public void dispose() {
                // Implementation will be added in Phase 3.2
            }
        };
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        // Implementation will be added in Phase 3.2
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
