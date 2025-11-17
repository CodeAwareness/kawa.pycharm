package com.codeawareness.pycharm.events;

import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event dispatcher for routing incoming messages to registered handlers.
 * Handlers are registered by action and invoked asynchronously on background threads.
 */
public class EventDispatcher {

    private final Map<String, EventHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Register an event handler.
     *
     * @param handler The handler to register
     */
    public void registerHandler(EventHandler handler) {
        if (handler == null) {
            Logger.warn("Cannot register null handler");
            return;
        }

        String action = handler.getAction();
        if (action == null || action.isEmpty()) {
            Logger.warn("Cannot register handler with null or empty action");
            return;
        }

        handlers.put(action, handler);
        Logger.debug("Registered event handler: " + action);
    }

    /**
     * Unregister an event handler.
     *
     * @param action The action to unregister
     */
    public void unregisterHandler(String action) {
        if (action != null) {
            EventHandler removed = handlers.remove(action);
            if (removed != null) {
                Logger.debug("Unregistered event handler: " + action);
            }
        }
    }

    /**
     * Dispatch a message to the appropriate handler.
     * Handler is invoked asynchronously on a background thread.
     *
     * @param message The message to dispatch
     * @return true if a handler was found and invoked, false otherwise
     */
    public boolean dispatch(Message message) {
        if (message == null) {
            Logger.warn("Cannot dispatch null message");
            return false;
        }

        // Try to find handler by full action key (domain:action)
        String fullAction = message.getDomain() != null
                ? message.getDomain() + ":" + message.getAction()
                : message.getAction();

        EventHandler handler = handlers.get(fullAction);

        // Fallback to action only
        if (handler == null && message.getAction() != null) {
            handler = handlers.get(message.getAction());
        }

        // Try canHandle method on all handlers
        if (handler == null) {
            for (EventHandler h : handlers.values()) {
                if (h.canHandle(message)) {
                    handler = h;
                    break;
                }
            }
        }

        if (handler == null) {
            Logger.debug("No handler found for message: " + message.getAction());
            return false;
        }

        // Invoke handler asynchronously
        final EventHandler finalHandler = handler;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                Logger.debug("Dispatching message to handler: " + finalHandler.getAction());
                finalHandler.handle(message);
            } catch (Exception e) {
                Logger.warn("Error in event handler: " + finalHandler.getAction() + " - " + e.getMessage());
                Logger.debug("Handler exception details", e);
            }
        });

        return true;
    }

    /**
     * Clear all registered handlers.
     */
    public void clear() {
        int count = handlers.size();
        handlers.clear();
        Logger.debug("Cleared " + count + " event handlers");
    }

    /**
     * Get the number of registered handlers.
     */
    public int size() {
        return handlers.size();
    }

    /**
     * Check if a handler is registered for an action.
     */
    public boolean hasHandler(String action) {
        return handlers.containsKey(action);
    }
}
