package com.codeawareness.pycharm.events;

import com.codeawareness.pycharm.communication.Message;

/**
 * Interface for event handlers.
 * Handlers process incoming messages from the Code Awareness backend.
 */
public interface EventHandler {

    /**
     * Get the action this handler processes.
     * Format: "domain:action" (e.g., "code:peer:select", "auth:info")
     */
    String getAction();

    /**
     * Handle an incoming message.
     *
     * @param message The message to handle
     */
    void handle(Message message);

    /**
     * Check if this handler should process the given message.
     * Default implementation checks if message action matches handler action.
     */
    default boolean canHandle(Message message) {
        if (message == null || message.getAction() == null) {
            return false;
        }

        String messageAction = message.getDomain() != null
                ? message.getDomain() + ":" + message.getAction()
                : message.getAction();

        return getAction().equals(messageAction) || getAction().equals(message.getAction());
    }
}
