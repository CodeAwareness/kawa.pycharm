package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;
import com.codeawareness.pycharm.utils.PathUtils;

import java.io.IOException;

/**
 * Manages connection to the Code Awareness catalog service.
 * The catalog service is used for initial client registration and discovery
 * of the local IPC service socket.
 */
public class CatalogConnection {

    private SocketManager socketManager;
    private final String clientGuid;
    private boolean connected = false;

    public CatalogConnection(String clientGuid) {
        this.clientGuid = clientGuid;
    }

    /**
     * Connect to the catalog service and register the client.
     */
    public void connect() throws IOException {
        Logger.info("Connecting to catalog service...");

        String catalogPath = PathUtils.getCatalogSocketPath();
        Logger.info("Catalog socket path: " + catalogPath);
        socketManager = new SocketManager(catalogPath);

        try {
            // Connect to catalog socket with retry
            Logger.info("Attempting to connect to catalog socket...");
            socketManager.connect();
            connected = true;
            Logger.info("Socket connection established to catalog");

            // Register client with catalog
            registerClient();

            Logger.info("Successfully connected and registered with catalog service (GUID: " + clientGuid + ")");
        } catch (IOException e) {
            connected = false;
            Logger.error("Failed to connect to catalog service at: " + catalogPath, e);
            throw e;
        }
    }

    /**
     * Register this client with the catalog service.
     */
    private void registerClient() throws IOException {
        Logger.info("Registering client with catalog: " + clientGuid);

        Message message = MessageBuilder.buildClientId(clientGuid);
        String serialized = MessageProtocol.serialize(message);

        Logger.info("Sending clientId message to catalog (length: " + serialized.length() + " bytes)");
        socketManager.write(serialized);
        Logger.info("Client registered with catalog: " + clientGuid);
    }

    /**
     * Send a disconnect message to the catalog.
     */
    public void disconnect() throws IOException {
        if (!connected) {
            return;
        }

        try {
            Logger.debug("Sending disconnect to catalog");
            Message message = MessageBuilder.buildClientDisconnect(clientGuid);
            String serialized = MessageProtocol.serialize(message);
            socketManager.write(serialized);
            Logger.info("Sent disconnect to catalog");
        } catch (IOException e) {
            Logger.error("Error sending disconnect to catalog", e);
            throw e;
        }
    }

    /**
     * Close the catalog connection.
     */
    public void close() {
        if (connected) {
            try {
                disconnect();
            } catch (IOException e) {
                Logger.warn("Error disconnecting from catalog: " + e.getMessage());
            }

            if (socketManager != null) {
                socketManager.close();
            }

            connected = false;
            Logger.info("Catalog connection closed");
        }
    }

    /**
     * Check if connected to catalog service.
     */
    public boolean isConnected() {
        return connected && socketManager != null && socketManager.isConnected();
    }

    /**
     * Get the client GUID.
     */
    public String getClientGuid() {
        return clientGuid;
    }
}
