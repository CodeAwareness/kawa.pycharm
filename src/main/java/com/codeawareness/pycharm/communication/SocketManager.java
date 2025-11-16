package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;
import com.codeawareness.pycharm.utils.PathUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Cross-platform socket manager.
 * Handles socket connections with retry logic, timeouts, and error handling.
 * Automatically selects Unix domain sockets or Windows named pipes based on platform.
 */
public class SocketManager {

    private static final int DEFAULT_TIMEOUT_MS = 5000;
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private static final long INITIAL_RETRY_DELAY_MS = 500;

    private SocketAdapter adapter;
    private final String socketPath;
    private final int timeoutMs;

    /**
     * Create a socket manager for the given socket path.
     *
     * @param socketPath Path to socket (Unix) or named pipe (Windows)
     */
    public SocketManager(String socketPath) {
        this(socketPath, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Create a socket manager with custom timeout.
     *
     * @param socketPath Path to socket (Unix) or named pipe (Windows)
     * @param timeoutMs  Connection timeout in milliseconds
     */
    public SocketManager(String socketPath, int timeoutMs) {
        this.socketPath = socketPath;
        this.timeoutMs = timeoutMs;
        this.adapter = createAdapter();
    }

    /**
     * Create the appropriate socket adapter for the current platform.
     */
    private SocketAdapter createAdapter() {
        if (PathUtils.isWindows()) {
            Logger.debug("Creating Windows named pipe adapter");
            return new WindowsNamedPipeAdapter(socketPath, timeoutMs);
        } else {
            Logger.debug("Creating Unix domain socket adapter");
            return new UnixSocketAdapter(socketPath, timeoutMs);
        }
    }

    /**
     * Connect to the socket with retry logic.
     *
     * @throws IOException If connection fails after all retries
     */
    public void connect() throws IOException {
        connect(MAX_RETRY_ATTEMPTS);
    }

    /**
     * Connect to the socket with specified number of retries.
     *
     * @param maxAttempts Maximum number of connection attempts
     * @throws IOException If connection fails after all retries
     */
    public void connect(int maxAttempts) throws IOException {
        IOException lastException = null;
        long retryDelay = INITIAL_RETRY_DELAY_MS;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Logger.info("Connection attempt " + attempt + "/" + maxAttempts + " to: " + socketPath);
                adapter.connect();
                Logger.info("Successfully connected to: " + socketPath);
                return;
            } catch (IOException e) {
                lastException = e;
                Logger.warn("Connection attempt " + attempt + " failed: " + e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Logger.debug("Retrying in " + retryDelay + "ms...");
                        TimeUnit.MILLISECONDS.sleep(retryDelay);
                        // Exponential backoff: double the delay each time
                        retryDelay = Math.min(retryDelay * 2, 8000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Connection interrupted", ie);
                    }
                }
            }
        }

        // All attempts failed
        String message = "Failed to connect after " + maxAttempts + " attempts";
        Logger.error(message);
        throw new IOException(message, lastException);
    }

    /**
     * Write a message to the socket.
     *
     * @param message Message to write
     * @throws IOException If write fails
     */
    public void write(String message) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to socket");
        }
        adapter.write(message);
    }

    /**
     * Read from the socket.
     *
     * @return Data read from socket
     * @throws IOException If read fails
     */
    public String read() throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to socket");
        }
        return adapter.read();
    }

    /**
     * Read from the socket until a delimiter is encountered.
     * For Code Awareness, the delimiter is form-feed (\f).
     *
     * @param delimiter Delimiter character
     * @return Data read until delimiter (delimiter not included)
     * @throws IOException If read fails
     */
    public String readUntilDelimiter(char delimiter) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to socket");
        }
        return adapter.readUntilDelimiter(delimiter);
    }

    /**
     * Close the socket connection.
     */
    public void close() {
        if (adapter != null) {
            try {
                adapter.close();
                Logger.info("Socket closed: " + socketPath);
            } catch (IOException e) {
                Logger.error("Error closing socket: " + socketPath, e);
            }
        }
    }

    /**
     * Check if socket is connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return adapter != null && adapter.isConnected();
    }

    /**
     * Get the socket path.
     */
    public String getSocketPath() {
        return socketPath;
    }

    /**
     * Wait for a socket to become available.
     *
     * @param maxWaitMs Maximum time to wait in milliseconds
     * @return true if socket exists, false if timeout
     */
    public static boolean waitForSocket(String socketPath, long maxWaitMs) {
        Logger.debug("Waiting for socket: " + socketPath);

        if (PathUtils.isWindows()) {
            // Windows named pipes don't have file system presence
            // We'll just try to connect
            return true;
        }

        long startTime = System.currentTimeMillis();
        long retryDelay = 100;

        while (System.currentTimeMillis() - startTime < maxWaitMs) {
            if (PathUtils.exists(socketPath)) {
                Logger.debug("Socket found: " + socketPath);
                return true;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(retryDelay);
                retryDelay = Math.min(retryDelay * 2, 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        Logger.warn("Timeout waiting for socket: " + socketPath);
        return false;
    }
}
