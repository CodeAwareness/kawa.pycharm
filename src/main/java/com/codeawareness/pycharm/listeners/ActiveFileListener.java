package com.codeawareness.pycharm.listeners;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for active file changes in the editor.
 * Detects when the user switches between files and notifies the Code Awareness backend via ActiveFileTracker.
 */
public class ActiveFileListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        // Get the project from the event
        FileEditorManager manager = event.getManager();
        Project project = manager.getProject();

        if (project == null || project.isDisposed()) {
            return;
        }

        // Get the project service and notify via ActiveFileTracker
        CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
        if (projectService != null) {
            VirtualFile newFile = event.getNewFile();
            if (newFile != null && newFile.isValid()) {
                projectService.getActiveFileTracker().notifyActiveFileChanged(newFile);
            } else {
                // No file selected (all files closed)
                projectService.getActiveFileTracker().notifyActiveFileChanged(null);
            }
        }
    }
}
