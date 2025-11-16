package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Unix domain socket adapter for Linux, macOS, and Unix systems.
 * Uses Java NIO UnixDomainSocketAddress (Java 16+).
 */
public class UnixSocketAdapter implements SocketAdapter {

    private SocketChannel socketChannel;
    private final String socketPath;
    private final int timeoutMs;

    public UnixSocketAdapter(String socketPath, int timeoutMs) {
        this.socketPath = socketPath;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void connect() throws IOException {
        Logger.debug("Connecting to Unix socket: " + socketPath);

        try {
            // Create Unix domain socket channel
            socketChannel = SocketChannel.open(StandardProtocolFamily.UNIX);
            socketChannel.configureBlocking(true);

            // Connect to socket
            UnixDomainSocketAddress address = UnixDomainSocketAddress.of(Path.of(socketPath));
            socketChannel.connect(address);

            Logger.info("Connected to Unix socket: " + socketPath);
        } catch (IOException e) {
            Logger.error("Failed to connect to Unix socket: " + socketPath, e);
            throw e;
        }
    }

    @Override
    public void write(String message) throws IOException {
        if (socketChannel == null || !socketChannel.isConnected()) {
            throw new IOException("Socket not connected");
        }

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

        Logger.trace("Wrote " + bytes.length + " bytes to Unix socket");
    }

    @Override
    public String read() throws IOException {
        if (socketChannel == null || !socketChannel.isConnected()) {
            throw new IOException("Socket not connected");
        }

        ByteBuffer buffer = ByteBuffer.allocate(8192);
        StringBuilder result = new StringBuilder();

        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            throw new IOException("Socket closed");
        }

        buffer.flip();
        result.append(StandardCharsets.UTF_8.decode(buffer));

        Logger.trace("Read " + bytesRead + " bytes from Unix socket");
        return result.toString();
    }

    @Override
    public String readUntilDelimiter(char delimiter) throws IOException {
        if (socketChannel == null || !socketChannel.isConnected()) {
            throw new IOException("Socket not connected");
        }

        StringBuilder result = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(1);

        while (true) {
            buffer.clear();
            int bytesRead = socketChannel.read(buffer);

            if (bytesRead == -1) {
                throw new IOException("Socket closed before delimiter found");
            }

            if (bytesRead > 0) {
                buffer.flip();
                char c = (char) buffer.get();
                if (c == delimiter) {
                    break;
                }
                result.append(c);
            }
        }

        Logger.trace("Read message of " + result.length() + " bytes until delimiter");
        return result.toString();
    }

    @Override
    public void close() throws IOException {
        if (socketChannel != null) {
            try {
                socketChannel.close();
                Logger.debug("Closed Unix socket: " + socketPath);
            } catch (IOException e) {
                Logger.error("Error closing Unix socket", e);
                throw e;
            } finally {
                socketChannel = null;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }
}
