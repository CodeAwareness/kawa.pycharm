package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.events.ResponseHandlerRegistry;
import com.codeawareness.pycharm.utils.Logger;
import com.codeawareness.pycharm.utils.PathUtils;
import com.intellij.openapi.application.ApplicationManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Manages connection to the Code Awareness IPC service.
 * This is the main communication channel with the backend after initial
 * registration with the catalog service.
 * Handles async message reading in a background thread.
 */
public class IpcConnection {

    private SocketManager socketManager;
    private final String clientGuid;
    private final ResponseHandlerRegistry responseHandlerRegistry;
    private final MessageParser messageParser;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread readerThread;
    private Consumer<Message> messageCallback;

    public IpcConnection(String clientGuid, ResponseHandlerRegistry responseHandlerRegistry) {
        this.clientGuid = clientGuid;
        this.responseHandlerRegistry = responseHandlerRegistry;
        this.messageParser = new MessageParser();
    }

    /**
     * Set a callback to handle incoming messages.
     */
    public void setMessageCallback(Consumer<Message> callback) {
        this.messageCallback = callback;
    }

    /**
     * Connect to the IPC service and start listening for messages.
     */
    public void connect() throws IOException {
        Logger.info("Connecting to IPC service...");

        String ipcPath = PathUtils.getIpcSocketPath(clientGuid);

        // Wait for IPC socket to be available (catalog creates it)
        long maxWaitMs = 10000; // 10 seconds
        if (!SocketManager.waitForSocket(ipcPath, maxWaitMs)) {
            throw new IOException("IPC socket not available: " + ipcPath);
        }

        socketManager = new SocketManager(ipcPath);

        try {
            // Connect to IPC socket
            socketManager.connect();
            connected.set(true);

            // Start background message reader
            startMessageReader();

            Logger.info("Successfully connected to IPC service");
        } catch (IOException e) {
            connected.set(false);
            Logger.error("Failed to connect to IPC service", e);
            throw e;
        }
    }

    /**
     * Start background thread to read messages from IPC socket.
     */
    private void startMessageReader() {
        running.set(true);
        readerThread = new Thread(() -> {
            Logger.debug("IPC message reader thread started");

            while (running.get() && connected.get()) {
                try {
                    // Read until delimiter
                    String data = socketManager.readUntilDelimiter(MessageProtocol.DELIMITER);

                    if (data != null && !data.isEmpty()) {
                        // Parse messages
                        List<Message> messages = messageParser.parse(data + MessageProtocol.DELIMITER);

                        // Handle each message
                        for (Message message : messages) {
                            handleMessage(message);
                        }
                    }
                } catch (IOException e) {
                    if (running.get() && connected.get()) {
                        Logger.error("Error reading from IPC socket", e);
                        // Connection lost
                        connected.set(false);
                        break;
                    }
                } catch (Exception e) {
                    Logger.error("Unexpected error in message reader", e);
                }
            }

            Logger.debug("IPC message reader thread stopped");
        }, "CodeAwareness-IPC-Reader");

        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Handle an incoming message.
     */
    private void handleMessage(Message message) {
        Logger.debug("Received message: " + message.getAction());

        // Try response handler first
        String handlerKey = message.getDomain() + ":" + message.getAction();
        if (responseHandlerRegistry.handle(handlerKey, message)) {
            Logger.debug("Message handled by response handler: " + handlerKey);
            return;
        }

        // Use callback if set
        if (messageCallback != null) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    messageCallback.accept(message);
                } catch (Exception e) {
                    Logger.error("Error in message callback", e);
                }
            });
        } else {
            Logger.debug("No handler for message: " + message.getAction());
        }
    }

    /**
     * Send a message to the IPC service.
     */
    public void sendMessage(Message message) throws IOException {
        if (!connected.get()) {
            throw new IOException("Not connected to IPC service");
        }

        String serialized = MessageProtocol.serialize(message);
        socketManager.write(serialized);
        Logger.debug("Sent message: " + message.getAction());
    }

    /**
     * Send a message and register a one-time response handler.
     */
    public void sendMessage(Message message, Consumer<Object> responseHandler) throws IOException {
        if (responseHandler != null) {
            String handlerKey = message.getDomain() + ":" + message.getAction();
            responseHandlerRegistry.register(handlerKey, responseHandler);
        }

        sendMessage(message);
    }

    /**
     * Close the IPC connection.
     */
    public void close() {
        if (connected.get()) {
            Logger.info("Closing IPC connection");

            // Stop reader thread
            running.set(false);
            if (readerThread != null) {
                readerThread.interrupt();
                try {
                    readerThread.join(1000); // Wait up to 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Close socket
            if (socketManager != null) {
                socketManager.close();
            }

            connected.set(false);
            Logger.info("IPC connection closed");
        }
    }

    /**
     * Check if connected to IPC service.
     */
    public boolean isConnected() {
        return connected.get() && socketManager != null && socketManager.isConnected();
    }

    /**
     * Get the client GUID.
     */
    public String getClientGuid() {
        return clientGuid;
    }
}
