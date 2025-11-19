package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for handling buffered message parsing with form-feed delimiters.
 * Supports fragmented message handling where messages may arrive in multiple chunks.
 */
public class MessageParser {

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Add data to the buffer and extract complete messages.
     * Messages are delimited by form-feed (\f) characters.
     *
     * @param data Data to add to buffer
     * @return List of complete messages found in the buffer
     */
    public List<Message> parse(String data) {
        List<Message> messages = new ArrayList<>();

        if (data == null || data.isEmpty()) {
            return messages;
        }

        // Add new data to buffer
        buffer.append(data);

        // Extract complete messages (delimited by form-feed)
        int delimiterIndex;
        while ((delimiterIndex = buffer.indexOf(String.valueOf(MessageProtocol.DELIMITER))) != -1) {
            // Extract message up to delimiter
            String messageJson = buffer.substring(0, delimiterIndex);

            // Remove the processed message and delimiter from buffer
            buffer.delete(0, delimiterIndex + 1);

            // Skip empty messages
            if (messageJson.trim().isEmpty()) {
                continue;
            }

            // Parse the message
            try {
                Message message = MessageProtocol.deserialize(messageJson);
                messages.add(message);
                Logger.debug("Parsed message: " + message.getAction());
            } catch (Exception e) {
                // Invalid messages are skipped - this is expected behavior
                // Use warn instead of error since we handle it gracefully
                Logger.warn("Skipping invalid message: " + e.getMessage());
                Logger.debug("Invalid message content: " + messageJson);
                // Continue processing other messages
            }
        }

        return messages;
    }

    /**
     * Get the current buffer content (for debugging).
     */
    public String getBufferContent() {
        return buffer.toString();
    }

    /**
     * Get the buffer size.
     */
    public int getBufferSize() {
        return buffer.length();
    }

    /**
     * Clear the buffer.
     */
    public void clear() {
        buffer.setLength(0);
    }

    /**
     * Check if buffer has incomplete message data.
     */
    public boolean hasIncompleteMessage() {
        return buffer.length() > 0;
    }
}
