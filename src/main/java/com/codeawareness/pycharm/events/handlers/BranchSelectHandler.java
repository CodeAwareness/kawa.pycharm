package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;

/**
 * Handles branch:select events from the Code Awareness backend.
 * Updates the selected branch in project state.
 */
public class BranchSelectHandler implements EventHandler {

    private final Project project;

    public BranchSelectHandler(Project project) {
        this.project = project;
    }

    @Override
    public String getAction() {
        return "code:branch:select";
    }

    @Override
    public void handle(Message message) {
        Logger.debug("Handling branch:select event");

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("branch:select message has no data");
            return;
        }

        // Extract branch information
        String branchName = data.has("branch") ? data.get("branch").getAsString() : null;

        if (branchName != null) {
            // Update project state
            CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
            if (projectService != null) {
                projectService.setSelectedBranch(branchName);
                Logger.info("Selected branch: " + branchName);

                // TODO: Request highlights for the selected branch
                // This will be implemented in Phase 3
            }
        } else {
            Logger.warn("branch:select message has no branch name");
        }
    }
}
