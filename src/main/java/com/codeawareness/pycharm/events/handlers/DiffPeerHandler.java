package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.diff.DiffViewerManager;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;

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
        Logger.debug("Handling diff-peer response");

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("diff-peer response has no data");
            return;
        }

        // Extract diff data
        String filePath = data.has("fpath") ? data.get("fpath").getAsString() : null;
        String peerName = data.has("peer") ? data.get("peer").getAsString() : null;
        String peerContent = data.has("content") ? data.get("content").getAsString() : null;

        if (filePath == null || peerName == null || peerContent == null) {
            Logger.warn("diff-peer response missing required fields (fpath, peer, content)");
            return;
        }

        // Show diff
        Logger.info("Showing diff for " + filePath + " with peer " + peerName);
        diffViewerManager.showDiff(filePath, peerName, peerContent);
    }
}
