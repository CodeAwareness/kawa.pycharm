package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * Windows named pipe adapter.
 * Uses RandomAccessFile to communicate with named pipes on Windows.
 */
public class WindowsNamedPipeAdapter implements SocketAdapter {

    private RandomAccessFile pipe;
    private final String pipePath;
    private final int timeoutMs;
    private boolean connected = false;

    public WindowsNamedPipeAdapter(String pipePath, int timeoutMs) {
        this.pipePath = pipePath;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void connect() throws IOException {
        Logger.debug("Connecting to Windows named pipe: " + pipePath);

        try {
            // Open the named pipe for reading and writing
            pipe = new RandomAccessFile(pipePath, "rw");
            connected = true;
            Logger.info("Connected to Windows named pipe: " + pipePath);
        } catch (IOException e) {
            Logger.error("Failed to connect to Windows named pipe: " + pipePath, e);
            connected = false;
            throw e;
        }
    }

    @Override
    public void write(String message) throws IOException {
        if (!connected || pipe == null) {
            throw new IOException("Pipe not connected");
        }

        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        pipe.write(bytes);

        Logger.trace("Wrote " + bytes.length + " bytes to Windows named pipe");
    }

    @Override
    public String read() throws IOException {
        if (!connected || pipe == null) {
            throw new IOException("Pipe not connected");
        }

        byte[] buffer = new byte[8192];
        int bytesRead = pipe.read(buffer);

        if (bytesRead == -1) {
            throw new IOException("Pipe closed");
        }

        String result = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
        Logger.trace("Read " + bytesRead + " bytes from Windows named pipe");
        return result;
    }

    @Override
    public String readUntilDelimiter(char delimiter) throws IOException {
        if (!connected || pipe == null) {
            throw new IOException("Pipe not connected");
        }

        StringBuilder result = new StringBuilder();

        while (true) {
            int b = pipe.read();

            if (b == -1) {
                throw new IOException("Pipe closed before delimiter found");
            }

            char c = (char) b;
            if (c == delimiter) {
                break;
            }
            result.append(c);
        }

        Logger.trace("Read message of " + result.length() + " bytes until delimiter");
        return result.toString();
    }

    @Override
    public void close() throws IOException {
        if (pipe != null) {
            try {
                pipe.close();
                Logger.debug("Closed Windows named pipe: " + pipePath);
            } catch (IOException e) {
                Logger.error("Error closing Windows named pipe", e);
                throw e;
            } finally {
                pipe = null;
                connected = false;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connected && pipe != null;
    }
}
