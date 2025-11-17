package com.codeawareness.pycharm.diff;

import com.codeawareness.pycharm.utils.Logger;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;

/**
 * Manages diff viewer operations for peer code comparison.
 * Opens side-by-side diffs between local and peer versions.
 */
public class DiffViewerManager {

    private final Project project;
    private final TempFileManager tempFileManager;

    public DiffViewerManager(Project project, TempFileManager tempFileManager) {
        this.project = project;
        this.tempFileManager = tempFileManager;
    }

    /**
     * Open a diff viewer comparing local file with peer's version.
     *
     * @param localFilePath Path to local file
     * @param peerName Name of the peer
     * @param peerContent Content of peer's version
     */
    public void showDiff(String localFilePath, String peerName, String peerContent) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                // Get local file
                VirtualFile localFile = LocalFileSystem.getInstance().findFileByPath(localFilePath);
                if (localFile == null || !localFile.exists()) {
                    Logger.warn("Local file not found: " + localFilePath);
                    return;
                }

                // Create temp file for peer content
                String fileName = localFile.getName();
                File peerFile = tempFileManager.createTempFile(fileName, peerName, peerContent);

                // Refresh VFS to recognize new temp file
                VirtualFile peerVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(peerFile);
                if (peerVirtualFile == null) {
                    Logger.warn("Failed to create virtual file for peer content");
                    return;
                }

                // Create diff contents
                DiffContentFactory contentFactory = DiffContentFactory.getInstance();
                DiffContent localContent = contentFactory.create(project, localFile);
                DiffContent peerDiffContent = contentFactory.create(project, peerVirtualFile);

                // Create diff request
                String title = "Code Awareness: " + fileName + " - " + peerName;
                SimpleDiffRequest request = new SimpleDiffRequest(
                    title,
                    localContent,
                    peerDiffContent,
                    "Your Version",
                    peerName + "'s Version"
                );

                // Show diff
                DiffManager.getInstance().showDiff(project, request);

                Logger.info("Opened diff viewer: " + fileName + " vs " + peerName);

            } catch (IOException e) {
                Logger.warn("Failed to show diff", e);
            }
        });
    }

    /**
     * Open a peer file in the editor (read-only).
     *
     * @param fileName File name
     * @param peerName Peer name
     * @param peerContent Peer's file content
     */
    public void openPeerFile(String fileName, String peerName, String peerContent) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                // Create temp file for peer content
                File peerFile = tempFileManager.createTempFile(fileName, peerName, peerContent);

                // Refresh VFS to recognize new temp file
                VirtualFile peerVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(peerFile);
                if (peerVirtualFile == null) {
                    Logger.warn("Failed to create virtual file for peer content");
                    return;
                }

                // Open in editor
                FileEditorManager.getInstance(project).openFile(peerVirtualFile, true);

                Logger.info("Opened peer file: " + fileName + " from " + peerName);

            } catch (IOException e) {
                Logger.warn("Failed to open peer file", e);
            }
        });
    }

    /**
     * Clean up temp files for a specific peer.
     *
     * @param fileName File name
     * @param peerName Peer name
     */
    public void cleanup(String fileName, String peerName) {
        tempFileManager.cleanupTempFile(fileName, peerName);
    }

    /**
     * Clean up all temp files.
     */
    public void cleanupAll() {
        tempFileManager.cleanupAll();
    }
}
