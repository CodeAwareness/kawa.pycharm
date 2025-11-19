package com.codeawareness.pycharm.listeners;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listens for file changes in the virtual file system.
 * Detects file saves and notifies the Code Awareness backend via FileMonitor.
 */
public class FileChangeListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            // We're interested in content changes (file saves)
            if (event instanceof VFileContentChangeEvent) {
                VirtualFile file = event.getFile();
                if (file != null && file.isValid()) {
                    handleFileSaved(file);
                }
            }
        }
    }

    /**
     * Handle a file save event.
     */
    private void handleFileSaved(VirtualFile file) {
        // Find the project this file belongs to
        Project project = findProjectForFile(file);
        if (project == null) {
            return;
        }

        // Get the project service and notify via FileMonitor
        CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
        if (projectService != null) {
            projectService.getFileMonitor().notifyFileSaved(file);
        }
    }

    /**
     * Find the project that contains the given file.
     */
    private Project findProjectForFile(VirtualFile file) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        for (Project project : openProjects) {
            if (project.isDisposed()) {
                continue;
            }

            String projectBasePath = project.getBasePath();
            if (projectBasePath != null && file.getPath().startsWith(projectBasePath)) {
                return project;
            }
        }

        return null;
    }
}
