package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.project.Project;

/**
 * Handles peer:unselect events from the Code Awareness backend.
 * Clears the selected peer in project state.
 */
public class PeerUnselectHandler implements EventHandler {

    private final Project project;

    public PeerUnselectHandler(Project project) {
        this.project = project;
    }

    @Override
    public String getAction() {
        return "code:peer:unselect";
    }

    @Override
    public void handle(Message message) {
        Logger.debug("Handling peer:unselect event");

        // Clear selected peer
        CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
        if (projectService != null) {
            projectService.setSelectedPeer(null);
            Logger.info("Peer unselected");

            // TODO: Clear highlights for the peer
            // This will be implemented in Phase 3
            projectService.clearHighlighters();
        }
    }
}
