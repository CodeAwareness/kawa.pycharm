package com.codeawareness.pycharm.listeners;

import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listens for file changes in the virtual file system.
 * Detects file saves and notifies the Code Awareness backend.
 */
public class FileChangeListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        // Implementation will be added in Phase 2.2
    }
}
