package com.codeawareness.pycharm.events;

import com.codeawareness.pycharm.utils.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registry for one-time response handlers.
 * Manages handlers that should be invoked once when a response is received,
 * then automatically cleaned up.
 */
public class ResponseHandlerRegistry {

    private final Map<String, Consumer<Object>> handlers = new ConcurrentHashMap<>();

    /**
     * Register a one-time response handler.
     *
     * @param key     Unique key for this handler (typically request ID or action)
     * @param handler Handler to invoke when response is received
     */
    public void register(String key, Consumer<Object> handler) {
        if (key == null || handler == null) {
            Logger.warn("Cannot register null key or handler");
            return;
        }
        handlers.put(key, handler);
        Logger.debug("Registered response handler for key: " + key);
    }

    /**
     * Handle a response by invoking the registered handler and cleaning up.
     *
     * @param key      Key for the handler
     * @param response Response data to pass to the handler
     * @return true if handler was found and invoked, false otherwise
     */
    public boolean handle(String key, Object response) {
        if (key == null) {
            Logger.warn("Cannot handle response with null key");
            return false;
        }

        Consumer<Object> handler = handlers.remove(key);
        if (handler == null) {
            Logger.debug("No handler found for key: " + key);
            return false;
        }

        try {
            handler.accept(response);
            Logger.debug("Handled response for key: " + key);
            return true;
        } catch (Exception e) {
            // Handler exceptions are caught and logged, but not fatal
            // Use warn instead of error since we handle it gracefully
            Logger.warn("Error in response handler for key: " + key + " - " + e.getMessage());
            Logger.debug("Handler exception details", e);
            return false;
        }
    }

    /**
     * Remove a handler without invoking it.
     *
     * @param key Key for the handler to remove
     */
    public void remove(String key) {
        if (key != null) {
            handlers.remove(key);
            Logger.debug("Removed handler for key: " + key);
        }
    }

    /**
     * Clear all registered handlers.
     */
    public void clear() {
        int count = handlers.size();
        handlers.clear();
        Logger.debug("Cleared " + count + " response handlers");
    }

    /**
     * Get the number of registered handlers.
     */
    public int size() {
        return handlers.size();
    }
}
