package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.diff.DiffViewerManager;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * Handles code:open-peer-file response events from the Code Awareness backend.
 * Opens peer files requested by the user through Muninn (Kawa Code app).
 *
 * Two scenarios:
 * 1. File exists locally: Open it directly in the editor
 * 2. File doesn't exist locally: Show diff between empty file and peer version
 */
public class OpenPeerFileHandler implements EventHandler {

    private final Project project;
    private final DiffViewerManager diffViewerManager;

    public OpenPeerFileHandler(Project project, DiffViewerManager diffViewerManager) {
        this.project = project;
        this.diffViewerManager = diffViewerManager;
    }

    @Override
    public String getAction() {
        return "code:open-peer-file";
    }

    @Override
    public void handle(Message message) {
        // Only process RES (response) flow messages
        if (message.getFlow() != Message.Flow.RES) {
            Logger.debug("Ignoring open-peer-file " + message.getFlow() + " flow message (only processing RES)");
            return;
        }

        Logger.info("OpenPeerFileHandler.handle() called for project: " + project.getName() +
                   ", flow: " + message.getFlow() + ", domain: " + message.getDomain() +
                   ", action: " + message.getAction());

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("open-peer-file response has no data");
            return;
        }

        Logger.info("open-peer-file response data: " + data);

        // Extract data from response
        boolean exists = data.has("exists") && data.get("exists").getAsBoolean();
        String filePath = data.has("filePath") ? data.get("filePath").getAsString() : null;
        String emptyFilePath = data.has("emptyFilePath") ? data.get("emptyFilePath").getAsString() : null;
        String peerId = data.has("peerId") ? data.get("peerId").getAsString() : "unknown";

        if (filePath == null) {
            Logger.warn("open-peer-file response missing filePath");
            return;
        }

        Logger.info("Opening peer file: " + filePath + " (exists locally: " + exists + ", peerId: " + peerId + ")");

        if (exists) {
            // File exists locally - open it directly
            openFileInEditor(filePath);
        } else {
            // File doesn't exist locally - show diff with peer version
            if (emptyFilePath == null) {
                Logger.warn("open-peer-file response missing emptyFilePath for non-existent file");
                return;
            }
            openDiffView(emptyFilePath, filePath, peerId);
        }
    }

    /**
     * Open a file directly in the editor.
     */
    private void openFileInEditor(String filePath) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    Logger.warn("File does not exist: " + filePath);
                    return;
                }

                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                if (virtualFile == null) {
                    Logger.warn("Could not find virtual file: " + filePath);
                    return;
                }

                // Open the file in the editor
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
                fileEditorManager.openTextEditor(descriptor, true);

                Logger.info("Opened file in editor: " + filePath);
            } catch (Exception e) {
                Logger.error("Failed to open file: " + filePath, e);
            }
        });
    }

    /**
     * Open a diff view comparing empty file with peer file.
     */
    private void openDiffView(String emptyFilePath, String peerFilePath, String peerId) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                String title = "New File (Peer: " + peerId + ")";

                Logger.info("Opening diff view: " + emptyFilePath + " vs " + peerFilePath);

                // Use the DiffViewerManager to show the diff
                diffViewerManager.showDiff(emptyFilePath, peerFilePath, title);

                Logger.info("Opened diff view for peer file");
            } catch (Exception e) {
                Logger.error("Failed to open diff view for peer file", e);
            }
        });
    }
}
