package com.codeawareness.pycharm.listeners;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for active file changes in the editor.
 * Tracks which file is currently being edited and notifies the backend.
 */
public class ActiveFileListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        // Implementation will be added in Phase 2.3
    }
}
