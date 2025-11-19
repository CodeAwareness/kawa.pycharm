package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;

/**
 * Handles auth:info response events from the Code Awareness backend.
 * Updates authentication state and user information.
 */
public class AuthInfoHandler implements EventHandler {

    private final Project project;

    public AuthInfoHandler(Project project) {
        this.project = project;
    }

    @Override
    public String getAction() {
        return "auth:info";
    }

    @Override
    public void handle(Message message) {
        Logger.debug("Handling auth:info response");

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.warn("auth:info response has no data");
            return;
        }

        // Extract user information
        if (data.has("user") && data.get("user").isJsonObject()) {
            JsonObject user = data.getAsJsonObject("user");
            String userName = user.has("name") ? user.get("name").getAsString() : null;
            String userEmail = user.has("email") ? user.get("email").getAsString() : null;

            // Update project state
            CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
            if (projectService != null) {
                projectService.setUserName(userName);
                projectService.setUserEmail(userEmail);
                projectService.setAuthenticated(true);

                Logger.info("Authenticated as: " + userName + " (" + userEmail + ")");
            }
        } else {
            // No user data means not authenticated
            CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
            if (projectService != null) {
                projectService.setAuthenticated(false);
                Logger.info("Not authenticated");
            }
        }

        // Extract temp directory if present
        if (data.has("tmpDir")) {
            String tmpDir = data.get("tmpDir").getAsString();
            CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
            if (projectService != null) {
                projectService.setTmpDir(tmpDir);
                Logger.debug("Temp directory: " + tmpDir);
            }
        }
    }
}
