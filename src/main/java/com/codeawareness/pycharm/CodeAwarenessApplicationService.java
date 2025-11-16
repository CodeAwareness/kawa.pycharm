package com.codeawareness.pycharm;

import com.codeawareness.pycharm.communication.CatalogConnection;
import com.codeawareness.pycharm.communication.IpcConnection;
import com.codeawareness.pycharm.events.ResponseHandlerRegistry;
import com.codeawareness.pycharm.utils.GuidGenerator;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;

/**
 * Application-level service for Code Awareness plugin.
 * Manages global state including client GUID, catalog connection, and IPC connection.
 * This is a singleton service that lives for the entire application lifetime.
 */
@Service
public final class CodeAwarenessApplicationService implements Disposable {

    private final String clientGuid;
    private CatalogConnection catalogConnection;
    private IpcConnection ipcConnection;
    private final ResponseHandlerRegistry responseHandlerRegistry;
    private volatile boolean connected = false;

    public CodeAwarenessApplicationService() {
        this.clientGuid = GuidGenerator.generate();
        this.responseHandlerRegistry = new ResponseHandlerRegistry();
        Logger.info("Code Awareness Application Service initialized with GUID: " + clientGuid);
    }

    /**
     * Get the unique client GUID for this PyCharm instance.
     */
    public String getClientGuid() {
        return clientGuid;
    }

    /**
     * Get the catalog connection.
     */
    public CatalogConnection getCatalogConnection() {
        return catalogConnection;
    }

    /**
     * Set the catalog connection.
     */
    public void setCatalogConnection(CatalogConnection catalogConnection) {
        this.catalogConnection = catalogConnection;
    }

    /**
     * Get the IPC connection.
     */
    public IpcConnection getIpcConnection() {
        return ipcConnection;
    }

    /**
     * Set the IPC connection.
     */
    public void setIpcConnection(IpcConnection ipcConnection) {
        this.ipcConnection = ipcConnection;
    }

    /**
     * Get the response handler registry.
     */
    public ResponseHandlerRegistry getResponseHandlerRegistry() {
        return responseHandlerRegistry;
    }

    /**
     * Check if connected to Code Awareness backend.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set connection status.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Initialize and connect to Code Awareness backend.
     */
    public void connect() {
        if (connected) {
            Logger.warn("Already connected to Code Awareness");
            return;
        }

        Logger.info("Connecting to Code Awareness backend...");
        // Connection logic will be implemented in Phase 1.4
    }

    /**
     * Disconnect from Code Awareness backend.
     */
    public void disconnect() {
        if (!connected) {
            Logger.warn("Not connected to Code Awareness");
            return;
        }

        Logger.info("Disconnecting from Code Awareness backend...");

        if (ipcConnection != null) {
            ipcConnection.close();
        }

        if (catalogConnection != null) {
            catalogConnection.close();
        }

        connected = false;
        Logger.info("Disconnected from Code Awareness");
    }

    @Override
    public void dispose() {
        Logger.info("Disposing Code Awareness Application Service");
        disconnect();
    }
}
