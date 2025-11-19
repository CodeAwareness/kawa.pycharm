package com.codeawareness.pycharm.communication;

import java.io.IOException;

/**
 * Platform-agnostic socket adapter interface.
 * Provides abstraction over Unix domain sockets and Windows named pipes.
 */
public interface SocketAdapter {

    /**
     * Connect to the socket/pipe.
     */
    void connect() throws IOException;

    /**
     * Write a message to the socket/pipe.
     */
    void write(String message) throws IOException;

    /**
     * Read from the socket/pipe.
     */
    String read() throws IOException;

    /**
     * Read from the socket/pipe until a delimiter is encountered.
     * The delimiter is consumed but not included in the result.
     */
    String readUntilDelimiter(char delimiter) throws IOException;

    /**
     * Close the socket/pipe connection.
     */
    void close() throws IOException;

    /**
     * Check if the socket/pipe is connected.
     */
    boolean isConnected();
}
