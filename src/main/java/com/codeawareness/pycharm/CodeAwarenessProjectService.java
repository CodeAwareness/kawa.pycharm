package com.codeawareness.pycharm;

import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.communication.MessageBuilder;
import com.codeawareness.pycharm.diff.DiffViewerManager;
import com.codeawareness.pycharm.diff.TempFileManager;
import com.codeawareness.pycharm.events.handlers.AuthInfoHandler;
import com.codeawareness.pycharm.events.handlers.BranchSelectHandler;
import com.codeawareness.pycharm.events.handlers.DiffPeerHandler;
import com.codeawareness.pycharm.events.handlers.PeerSelectHandler;
import com.codeawareness.pycharm.events.handlers.PeerUnselectHandler;
import com.codeawareness.pycharm.highlighting.HighlightManager;
import com.codeawareness.pycharm.monitoring.ActiveFileTracker;
import com.codeawareness.pycharm.monitoring.FileMonitor;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project-level service for Code Awareness plugin.
 * Manages project-specific state including active file, peer selection, and highlights.
 * Each project has its own instance of this service.
 */
@Service(Service.Level.PROJECT)
public final class CodeAwarenessProjectService implements Disposable {

    private final Project project;
    private final FileMonitor fileMonitor;
    private final ActiveFileTracker activeFileTracker;
    private final HighlightManager highlightManager;
    private final TempFileManager tempFileManager;
    private final DiffViewerManager diffViewerManager;
    private VirtualFile activeFile;
    private String selectedPeer;
    private String selectedBranch;
    private boolean authenticated = false;
    private String userName;
    private String userEmail;
    private String tmpDir;
    private final Map<String, Object> highlighters = new ConcurrentHashMap<>();

    public CodeAwarenessProjectService(Project project) {
        this.project = project;
        this.fileMonitor = new FileMonitor(project);
        this.activeFileTracker = new ActiveFileTracker(project);
        this.highlightManager = new HighlightManager(project);
        this.tempFileManager = new TempFileManager(null); // Will be updated when tmpDir is received
        this.diffViewerManager = new DiffViewerManager(project, tempFileManager);
        Logger.info("Code Awareness Project Service initialized for project: " + project.getName());

        // Register event handlers
        registerEventHandlers();

        // Request authentication info
        requestAuthInfo();
    }

    /**
     * Register all project-level event handlers with the application-level event dispatcher.
     */
    private void registerEventHandlers() {
        CodeAwarenessApplicationService appService =
            ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);

        if (appService != null) {
            // Register handlers for this project
            appService.getEventDispatcher().registerHandler(new AuthInfoHandler(project));
            appService.getEventDispatcher().registerHandler(new PeerSelectHandler(project));
            appService.getEventDispatcher().registerHandler(new PeerUnselectHandler(project));
            appService.getEventDispatcher().registerHandler(new BranchSelectHandler(project));
            appService.getEventDispatcher().registerHandler(new DiffPeerHandler(project, diffViewerManager));

            Logger.debug("Registered event handlers for project: " + project.getName());
        }
    }

    /**
     * Request authentication info from the backend.
     */
    public void requestAuthInfo() {
        try {
            CodeAwarenessApplicationService appService =
                ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);

            if (appService == null || !appService.isConnected()) {
                Logger.debug("Cannot request auth info: not connected");
                return;
            }

            // Build auth:info request
            Message message = MessageBuilder.buildAuthInfo(appService.getClientGuid());

            // Send via IPC connection
            if (appService.getIpcConnection() != null) {
                appService.getIpcConnection().sendMessage(message);
                Logger.debug("Sent auth:info request");
            }

        } catch (Exception e) {
            Logger.warn("Failed to request auth info", e);
        }
    }

    /**
     * Request a diff with a peer for a specific file.
     *
     * @param filePath Path to the file
     * @param peerGuid GUID of the peer
     */
    public void requestDiffWithPeer(String filePath, String peerGuid) {
        try {
            CodeAwarenessApplicationService appService =
                ApplicationManager.getApplication().getService(CodeAwarenessApplicationService.class);

            if (appService == null || !appService.isConnected()) {
                Logger.debug("Cannot request diff: not connected");
                return;
            }

            // Build diff-peer request
            Message message = MessageBuilder.buildDiffPeer(
                appService.getClientGuid(),
                "local", // origin
                filePath,
                peerGuid
            );

            // Send via IPC connection
            if (appService.getIpcConnection() != null) {
                appService.getIpcConnection().sendMessage(message);
                Logger.debug("Sent diff-peer request for: " + filePath);
            }

        } catch (Exception e) {
            Logger.warn("Failed to request diff", e);
        }
    }

    public Project getProject() {
        return project;
    }

    public VirtualFile getActiveFile() {
        return activeFile;
    }

    public void setActiveFile(VirtualFile activeFile) {
        this.activeFile = activeFile;
    }

    public String getSelectedPeer() {
        return selectedPeer;
    }

    public void setSelectedPeer(String selectedPeer) {
        this.selectedPeer = selectedPeer;
    }

    public String getSelectedBranch() {
        return selectedBranch;
    }

    public void setSelectedBranch(String selectedBranch) {
        this.selectedBranch = selectedBranch;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    public Map<String, Object> getHighlighters() {
        return highlighters;
    }

    public void clearHighlighters() {
        highlighters.clear();
    }

    public FileMonitor getFileMonitor() {
        return fileMonitor;
    }

    public ActiveFileTracker getActiveFileTracker() {
        return activeFileTracker;
    }

    public HighlightManager getHighlightManager() {
        return highlightManager;
    }

    public DiffViewerManager getDiffViewerManager() {
        return diffViewerManager;
    }

    public TempFileManager getTempFileManager() {
        return tempFileManager;
    }

    @Override
    public void dispose() {
        Logger.info("Disposing Code Awareness Project Service for project: " + project.getName());
        clearHighlighters();
        highlightManager.clearAllHighlights();
        diffViewerManager.cleanupAll();
        fileMonitor.shutdown();
        activeFileTracker.shutdown();
    }
}
