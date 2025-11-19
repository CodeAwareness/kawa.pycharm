package com.codeawareness.pycharm.highlighting;

import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages code highlighting for Code Awareness.
 * Provides full-width line highlights that can be toggled ON/OFF.
 */
public class HighlightManager {

    private final Project project;
    private final Map<String, List<RangeHighlighter>> highlightersByFile = new ConcurrentHashMap<>();
    private boolean highlightsEnabled = true;

    public HighlightManager(Project project) {
        this.project = project;
    }

    /**
     * Add a full-width line highlight to the specified file and line.
     *
     * @param filePath Absolute path to the file
     * @param lineNumber Line number (0-indexed)
     */
    public void addHighlight(String filePath, int lineNumber) {
        runOnUiThread(() -> {
            try {
                VirtualFile file = com.intellij.openapi.vfs.VfsUtil.findFileByIoFile(
                    new java.io.File(filePath), true
                );

                if (file == null || !file.isValid()) {
                    Logger.warn("Cannot highlight: file not found: " + filePath);
                    return;
                }

                Editor editor = getEditorForFile(file);
                if (editor == null) {
                    Logger.debug("No editor open for file: " + filePath);
                    return;
                }

                Document document = editor.getDocument();
                if (lineNumber < 0 || lineNumber >= document.getLineCount()) {
                    Logger.warn("Invalid line number: " + lineNumber + " for file: " + filePath);
                    return;
                }

                // Calculate line offsets
                int lineStartOffset = document.getLineStartOffset(lineNumber);
                int lineEndOffset = document.getLineEndOffset(lineNumber);

                // Create highlight attributes
                TextAttributes attributes = new TextAttributes();
                attributes.setBackgroundColor(ColorSchemeProvider.getHighlightJBColor());

                // Add highlight
                MarkupModel markupModel = editor.getMarkupModel();
                RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                    lineStartOffset,
                    lineEndOffset,
                    HighlighterLayer.SELECTION - 1,  // Just below selection layer
                    attributes,
                    HighlighterTargetArea.LINES_IN_RANGE  // Full-width highlighting
                );

                // Store highlighter
                highlightersByFile.computeIfAbsent(filePath, k -> new ArrayList<>()).add(highlighter);

            } catch (Exception e) {
                Logger.warn("Failed to add highlight", e);
            }
        });
    }

    /**
     * Remove all highlights from a specific file.
     *
     * @param filePath Absolute path to the file
     */
    public void clearHighlights(String filePath) {
        runOnUiThread(() -> {
            List<RangeHighlighter> highlighters = highlightersByFile.remove(filePath);
            if (highlighters != null) {
                for (RangeHighlighter highlighter : highlighters) {
                    if (highlighter.isValid()) {
                        highlighter.dispose();
                    }
                }
                Logger.debug("Cleared " + highlighters.size() + " highlights from: " + filePath);
            }
        });
    }

    /**
     * Remove all highlights from all files.
     */
    public void clearAllHighlights() {
        runOnUiThread(() -> {
            int totalCleared = 0;
            for (Map.Entry<String, List<RangeHighlighter>> entry : highlightersByFile.entrySet()) {
                for (RangeHighlighter highlighter : entry.getValue()) {
                    if (highlighter.isValid()) {
                        highlighter.dispose();
                        totalCleared++;
                    }
                }
            }
            highlightersByFile.clear();
            Logger.info("Cleared all highlights (" + totalCleared + " total)");
        });
    }

    /**
     * Set whether highlights are enabled (visible).
     * When disabled, all existing highlights are hidden.
     * When re-enabled, highlights are shown again.
     *
     * @param enabled true to show highlights, false to hide
     */
    public void setHighlightsEnabled(boolean enabled) {
        this.highlightsEnabled = enabled;

        runOnUiThread(() -> {
            if (!enabled) {
                // Hide all highlights by clearing them
                clearAllHighlights();
            }
            Logger.info("Highlights " + (enabled ? "enabled" : "disabled"));
        });
    }

    /**
     * Refresh all existing highlights with updated colors.
     * This recreates all highlights using the current color scheme settings.
     */
    public void refreshHighlightColors() {
        runOnUiThread(() -> {
            try {
                // Store all current highlights with their file paths and line numbers
                Map<String, List<Integer>> lineNumbersByFile = new ConcurrentHashMap<>();

                for (Map.Entry<String, List<RangeHighlighter>> entry : this.highlightersByFile.entrySet()) {
                    String filePath = entry.getKey();
                    List<Integer> lineNumbers = new ArrayList<>();

                    for (RangeHighlighter highlighter : entry.getValue()) {
                        if (highlighter.isValid()) {
                            // Get the document to find line number from offset
                            Document document = highlighter.getDocument();
                            if (document != null) {
                                int lineNumber = document.getLineNumber(highlighter.getStartOffset());
                                lineNumbers.add(lineNumber);
                            }
                        }
                    }

                    if (!lineNumbers.isEmpty()) {
                        lineNumbersByFile.put(filePath, lineNumbers);
                    }
                }

                // Clear all existing highlights
                for (Map.Entry<String, List<RangeHighlighter>> entry : this.highlightersByFile.entrySet()) {
                    for (RangeHighlighter highlighter : entry.getValue()) {
                        if (highlighter.isValid()) {
                            highlighter.dispose();
                        }
                    }
                }
                this.highlightersByFile.clear();

                // Recreate highlights with new colors
                int totalRefreshed = 0;
                for (Map.Entry<String, List<Integer>> entry : lineNumbersByFile.entrySet()) {
                    String filePath = entry.getKey();
                    for (Integer lineNumber : entry.getValue()) {
                        addHighlight(filePath, lineNumber);
                        totalRefreshed++;
                    }
                }

                Logger.info("Refreshed " + totalRefreshed + " highlights with new colors");

            } catch (Exception e) {
                Logger.warn("Failed to refresh highlight colors", e);
            }
        });
    }

    /**
     * Check if highlights are currently enabled.
     */
    public boolean isHighlightsEnabled() {
        return highlightsEnabled;
    }

    /**
     * Get the editor for a given virtual file.
     */
    private Editor getEditorForFile(VirtualFile file) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        if (fileEditorManager == null) {
            return null;
        }

        FileEditor[] editors = fileEditorManager.getEditors(file);

        for (FileEditor fileEditor : editors) {
            if (fileEditor instanceof TextEditor) {
                return ((TextEditor) fileEditor).getEditor();
            }
        }

        return null;
    }

    /**
     * Get the count of highlights for a specific file.
     */
    public int getHighlightCount(String filePath) {
        List<RangeHighlighter> highlighters = highlightersByFile.get(filePath);
        return highlighters != null ? highlighters.size() : 0;
    }

    /**
     * Get the total count of all highlights.
     */
    public int getTotalHighlightCount() {
        return highlightersByFile.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    private void runOnUiThread(Runnable action) {
        var application = ApplicationManager.getApplication();
        if (application != null) {
            application.invokeLater(action);
        } else {
            action.run();
        }
    }
}
