package com.codeawareness.pycharm.events.handlers;

import com.codeawareness.pycharm.CodeAwarenessProjectService;
import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.events.EventHandler;
import com.codeawareness.pycharm.highlighting.HighlightManager;
import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles active-path response events from the Code Awareness backend.
 * Applies code highlights based on the hl (highlight) array from Gardener.
 */
public class ActivePathHandler implements EventHandler {

    private final Project project;
    private final HighlightManager highlightManager;

    public ActivePathHandler(Project project, HighlightManager highlightManager) {
        this.project = project;
        this.highlightManager = highlightManager;
    }

    @Override
    public String getAction() {
        return "code:active-path";
    }

    @Override
    public void handle(Message message) {
        // Only process RES (response) flow messages
        if (message.getFlow() != Message.Flow.RES) {
            Logger.debug("Ignoring active-path " + message.getFlow() + " flow message (only processing RES)");
            return;
        }

        Logger.info("ActivePathHandler.handle() called for project: " + project.getName() +
                   ", flow: " + message.getFlow() + ", domain: " + message.getDomain() +
                   ", action: " + message.getAction());

        JsonObject data = message.getDataAsObject();
        if (data == null) {
            Logger.debug("active-path response has no data");
            return;
        }

        // Log the ENTIRE raw JSON to see what we're actually receiving
        Logger.info("=== FULL DATA OBJECT ===");
        Logger.info(data.toString());
        Logger.info("=== END FULL DATA ===");

        // Log the data structure for debugging
        Logger.info("active-path response data keys: " + data.keySet());
        if (data.has("hl")) {
            Logger.info("hl field RAW: " + data.get("hl"));
            Logger.info("hl field toString: " + data.get("hl").toString());

            // Try to get the raw JSON string before Gson parses it
            JsonArray hlArray = data.get("hl").getAsJsonArray();
            for (int i = 0; i < hlArray.size(); i++) {
                Logger.info("hl[" + i + "] raw element: " + hlArray.get(i) + ", as int: " + hlArray.get(i).getAsInt() + ", as long: " + hlArray.get(i).getAsLong());
            }
        }

        // Extract the hl (highlight) array
        if (!data.has("hl")) {
            Logger.debug("active-path response has no hl field - no highlights to apply");
            return;
        }

        JsonElement hlElement = data.get("hl");
        Logger.info("hl element type: " + hlElement.getClass().getName() + ", value: " + hlElement);

        if (!hlElement.isJsonArray()) {
            Logger.warn("active-path hl field is not an array: " + hlElement);
            return;
        }

        JsonArray hlArray = hlElement.getAsJsonArray();
        List<Integer> highlightLines = new ArrayList<>();

        // Parse line numbers from the array
        for (JsonElement element : hlArray) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                int lineNumber = element.getAsInt();
                Logger.debug("Parsed line number from hl array: " + lineNumber + " (raw: " + element + ")");
                highlightLines.add(lineNumber);
            } else {
                Logger.warn("Skipping non-numeric element in hl array: " + element);
            }
        }

        Logger.info("Received " + highlightLines.size() + " highlight lines for project: " + project.getName());
        Logger.info("Line numbers: " + highlightLines);

        // Get the currently active file
        CodeAwarenessProjectService projectService = project.getService(CodeAwarenessProjectService.class);
        VirtualFile activeFile = projectService != null ? projectService.getActiveFile() : null;

        if (activeFile == null) {
            Logger.warn("No active file to apply highlights to");
            return;
        }

        String filePath = activeFile.getPath();
        Logger.info("Applying highlights to file: " + filePath);

        // Clear existing highlights for this file first
        highlightManager.clearHighlights(filePath);

        // Apply new highlights (hl array uses 0-based line numbers, which matches IntelliJ's API)
        for (Integer lineNumber : highlightLines) {
            highlightManager.addHighlight(filePath, lineNumber);
        }

        Logger.info("Applied " + highlightLines.size() + " highlights to: " + filePath);
    }
}
