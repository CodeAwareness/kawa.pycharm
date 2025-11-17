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

    @Override
    public void handle(Message message) {
        Logger.debug("Handling peer:select event");

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("peer:select message has no data");
            return;
        }

        // Extract peer information
        if (data.has("peer") && data.get("peer").isJsonObject()) {
            JsonObject peer = data.getAsJsonObject("peer");
            String peerGuid = peer.has("guid") ? peer.get("guid").getAsString() : null;
            String peerName = peer.has("name") ? peer.get("name").getAsString() : null;
            String peerEmail = peer.has("email") ? peer.get("email").getAsString() : null;

            // Update project state
            CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
            if (projectService != null) {
                projectService.setSelectedPeer(peerGuid);
                Logger.info("Selected peer: " + peerName + " (" + peerGuid + ")");

                // TODO: Request highlights for the selected peer
                // This will be implemented in Phase 3
            }
        } else {
            Logger.warn("peer:select message has invalid peer data");
        }
    }
}
