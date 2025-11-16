package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;

/**
 * Manages connection to the Code Awareness IPC service.
 * This is the main communication channel with the backend after initial
 * registration with the catalog service.
 */
public class IpcConnection {

    private boolean connected = false;

    /**
     * Connect to the IPC service.
     */
    public void connect() {
        Logger.info("Connecting to IPC service...");
        // Implementation will be added in Phase 1.4
    }

    /**
     * Disconnect from the IPC service.
     */
    public void close() {
        if (connected) {
            Logger.info("Closing IPC connection");
            connected = false;
        }
    }

    /**
     * Check if connected to IPC service.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Send a message to the IPC service.
     */
    public void sendMessage(String message) {
        if (!connected) {
            Logger.warn("Cannot send message - not connected");
            return;
        }
        // Implementation will be added in Phase 1.3
    }
}
