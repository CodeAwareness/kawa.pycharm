package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.diff.DiffViewerManager;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Handles diff-peer response events from the Code Awareness backend.
 * Opens a diff viewer comparing local and peer versions.
 */
public class DiffPeerHandler implements EventHandler {

    private final Project project;
    private final DiffViewerManager diffViewerManager;

    public DiffPeerHandler(Project project, DiffViewerManager diffViewerManager) {
        this.project = project;
        this.diffViewerManager = diffViewerManager;
    }

    @Override
    public String getAction() {
        return "code:diff-peer";
    }

    @Override
    public void handle(Message message) {
        Logger.info("DiffPeerHandler.handle() called for project: " + project.getName() +
                   ", flow: " + message.getFlow() + ", domain: " + message.getDomain() +
                   ", action: " + message.getAction());

        // Only process RES (response) flow messages
        if (message.getFlow() != Message.Flow.RES) {
            Logger.debug("Ignoring diff-peer " + message.getFlow() + " flow message (only processing RES)");
            return;
        }

        Logger.info("Handling diff-peer response (flow: " + message.getFlow() + ") for project: " + project.getName());

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("diff-peer response has no data");
            return;
        }

        // Log the actual data structure for debugging
        Logger.info("diff-peer response data structure: " + data.toString());

        // Extract diff data from Gardener response
        // Response format: { title, extractDir, peerFile, userId, fpath }
        String peerFile = data.has("peerFile") ? data.get("peerFile").getAsString() : null;
        String userId = data.has("userId") ? data.get("userId").getAsString() : null;
        String title = data.has("title") ? data.get("title").getAsString() : null;

        // Get the currently active file path (don't use fpath from response as it may be incorrect)
        CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
        com.intellij.openapi.vfs.VirtualFile activeFile = projectService != null ? projectService.getActiveFile() : null;
        String filePath = activeFile != null ? activeFile.getPath() : null;

        Logger.info("Extracted diff data - filePath: " + filePath + ", peerFile: " + peerFile +
                   ", userId: " + userId + ", title: " + title);

        if (filePath == null) {
            Logger.warn("No active file to diff with peer");
            return;
        }

        if (peerFile == null) {
            Logger.warn("diff-peer response missing peerFile field - cannot read peer file content");
            return;
        }

        // Read the peer file content from the extracted file path
        String peerContent = null;
        try {
            peerContent = new String(Files.readAllBytes(Paths.get(peerFile)));
            Logger.info("Read peer file content from: " + peerFile + " (length: " + peerContent.length() + " bytes)");
        } catch (IOException e) {
            Logger.error("Failed to read peer file: " + peerFile, e);
            return;
        }

        // Use title as peer name, or fallback to userId or "Peer"
        String peerName = title != null ? title : 
                         (userId != null ? "Peer " + userId : "Peer");

        // Show diff
        Logger.info("Showing diff for " + filePath + " with peer " + peerName);
        diffViewerManager.showDiff(filePath, peerName, peerContent);
    }
}
