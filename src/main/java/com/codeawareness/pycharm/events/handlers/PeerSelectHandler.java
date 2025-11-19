package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;

/**
 * Handles peer:select events from the Code Awareness backend.
 * Updates the selected peer in project state.
 */
public class PeerSelectHandler implements EventHandler {

    private final Project project;

    public PeerSelectHandler(Project project) {
        this.project = project;
    }

    @Override
    public String getAction() {
        return "code:peer:select";
    }

    /**
     * Get the relative path of a file from the project base directory.
     * Gardener expects relative paths, not absolute paths.
     */
    private String getRelativePath(com.intellij.openapi.vfs.VirtualFile file) {
        String basePath = project.getBasePath();
        String filePath = file.getPath();

        if (basePath != null && filePath.startsWith(basePath)) {
            // Remove base path and leading slash
            String relativePath = filePath.substring(basePath.length());
            if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
                relativePath = relativePath.substring(1);
            }
            return relativePath;
        }

        // Fallback to absolute path if we can't determine relative path
        Logger.warn("Could not determine relative path for file: " + filePath + " (base: " + basePath + ")");
        return filePath;
    }

    @Override
    public void handle(Message message) {
        // Only process RES (response) flow messages - REQ is the request we send, RES is the response from Gardener
        if (message.getFlow() != Message.Flow.RES) {
            Logger.debug("Ignoring peer:select REQ flow message (only processing RES)");
            return;
        }

        Logger.info("Handling peer:select event (flow: " + message.getFlow() + ")");

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("peer:select message has no data");
            return;
        }

        // Log the actual data structure for debugging
        Logger.debug("peer:select data structure: " + data.toString());

        String peerGuid = null;
        String peerName = null;
        String peerEmail = null;

        // Try different possible data formats
        // Format 1: data.peer is an object with guid, name, email
        if (data.has("peer") && data.get("peer").isJsonObject()) {
            JsonObject peer = data.getAsJsonObject("peer");
            peerGuid = peer.has("guid") ? peer.get("guid").getAsString() : 
                      peer.has("_id") ? peer.get("_id").getAsString() : 
                      peer.has("user") ? peer.get("user").getAsString() : null;
            peerName = peer.has("name") ? peer.get("name").getAsString() : null;
            peerEmail = peer.has("email") ? peer.get("email").getAsString() : null;
        }
        // Format 2: peer is a string (GUID) at top level
        else if (data.has("peer") && data.get("peer").isJsonPrimitive() && data.get("peer").getAsJsonPrimitive().isString()) {
            peerGuid = data.get("peer").getAsString();
        }
        // Format 3: peer data is at top level (guid, name, email directly in data)
        else if (data.has("guid") || data.has("_id") || data.has("user")) {
            peerGuid = data.has("guid") ? data.get("guid").getAsString() :
                      data.has("_id") ? data.get("_id").getAsString() :
                      data.has("user") ? data.get("user").getAsString() : null;
            peerName = data.has("name") ? data.get("name").getAsString() : null;
            peerEmail = data.has("email") ? data.get("email").getAsString() : null;
        }

        if (peerGuid != null && !peerGuid.isEmpty()) {
            // Update project state
            CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
            if (projectService != null) {
                projectService.setSelectedPeer(peerGuid);
                Logger.info("Selected peer: " + (peerName != null ? peerName : "unknown") + " (" + peerGuid + ")");

                // Request diff for the active file with the selected peer
                // This will trigger the diff viewer to open
                com.intellij.openapi.vfs.VirtualFile activeFile = projectService.getActiveFile();
                if (activeFile != null && activeFile.isValid()) {
                    // Get the relative path from project base directory
                    // Gardener expects relative paths, not absolute paths
                    String relativePath = getRelativePath(activeFile);
                    Logger.info("Requesting diff for active file: " + relativePath + " with peer: " + peerGuid);
                    projectService.requestDiffWithPeer(relativePath, peerGuid);
                } else {
                    Logger.warn("No active file available to diff with peer. Active file: " +
                               (activeFile != null ? activeFile.getPath() : "null"));
                    // Try to get the currently selected file from the editor
                    com.intellij.openapi.fileEditor.FileEditorManager editorManager =
                        com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project);
                    com.intellij.openapi.vfs.VirtualFile[] selectedFiles = editorManager.getSelectedFiles();
                    if (selectedFiles.length > 0 && selectedFiles[0].isValid()) {
                        String relativePath = getRelativePath(selectedFiles[0]);
                        Logger.info("Using currently selected file for diff: " + relativePath);
                        projectService.requestDiffWithPeer(relativePath, peerGuid);
                    } else {
                        Logger.warn("No file selected in editor to diff with peer");
                    }
                }
            }
        } else {
            Logger.warn("peer:select message has invalid peer data - could not extract peer GUID. Data: " + data.toString());
        }
    }
}
