package com.codeawareness.pycharm.events;

import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.utils.Logger;
import com.intellij.openapi.application.ApplicationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event dispatcher for routing incoming messages to registered handlers.
 * Handlers are registered by action and invoked asynchronously on background threads.
 * Supports multiple handlers per action to handle multi-project scenarios.
 */
public class EventDispatcher {

    private final Map<String, List<EventHandler>> handlers = new ConcurrentHashMap<>();

    /**
     * Register an event handler.
     * Supports multiple handlers per action to handle multi-project scenarios.
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

        // Use CopyOnWriteArrayList for thread-safe iteration while handlers are being invoked
        handlers.computeIfAbsent(action, k -> new CopyOnWriteArrayList<>()).add(handler);

        int handlerCount = handlers.get(action).size();
        Logger.info("Registered event handler: " + action + " (" + handler.getClass().getSimpleName() +
                   ") - total handlers for this action: " + handlerCount);
    }

    /**
     * Unregister a specific event handler instance.
     *
     * @param handler The handler instance to unregister
     */
    public void unregisterHandler(EventHandler handler) {
        if (handler == null || handler.getAction() == null) {
            return;
        }

        String action = handler.getAction();
        List<EventHandler> handlerList = handlers.get(action);
        if (handlerList != null) {
            boolean removed = handlerList.remove(handler);
            if (removed) {
                Logger.info("Unregistered event handler: " + action + " (" + handler.getClass().getSimpleName() +
                           ") - remaining handlers: " + handlerList.size());
                // Clean up empty lists
                if (handlerList.isEmpty()) {
                    handlers.remove(action);
                }
            }
        }
    }

    /**
     * Unregister all handlers for a specific action.
     *
     * @param action The action to unregister
     */
    public void unregisterAllHandlers(String action) {
        if (action != null) {
            List<EventHandler> removed = handlers.remove(action);
            if (removed != null) {
                Logger.debug("Unregistered all event handlers for action: " + action + " (count: " + removed.size() + ")");
            }
        }
    }

    /**
     * Dispatch a message to the appropriate handlers.
     * All matching handlers are invoked asynchronously on background threads.
     *
     * @param message The message to dispatch
     * @return true if at least one handler was found and invoked, false otherwise
     */
    public boolean dispatch(Message message) {
        if (message == null) {
            Logger.warn("Cannot dispatch null message");
            return false;
        }

        // Try to find handlers by full action key (domain:action)
        String fullAction = message.getDomain() != null
                ? message.getDomain() + ":" + message.getAction()
                : message.getAction();

        List<EventHandler> matchedHandlers = new ArrayList<>();

        // Try full action key first
        List<EventHandler> handlerList = handlers.get(fullAction);
        if (handlerList != null && !handlerList.isEmpty()) {
            matchedHandlers.addAll(handlerList);
        }

        // Fallback to action only if no full action match
        if (matchedHandlers.isEmpty() && message.getAction() != null) {
            handlerList = handlers.get(message.getAction());
            if (handlerList != null && !handlerList.isEmpty()) {
                matchedHandlers.addAll(handlerList);
            }
        }

        // Try canHandle method on all handlers if still no match
        if (matchedHandlers.isEmpty()) {
            for (List<EventHandler> hList : handlers.values()) {
                for (EventHandler h : hList) {
                    if (h.canHandle(message)) {
                        matchedHandlers.add(h);
                    }
                }
            }
        }

        if (matchedHandlers.isEmpty()) {
            Logger.warn("No handler found for message: " + fullAction + " (domain: " + message.getDomain() + ", action: " + message.getAction() + ", flow: " + message.getFlow() + ")");
            return false;
        }

        // Invoke all matched handlers asynchronously
        Logger.info("Dispatching message to " + matchedHandlers.size() + " handler(s) for: " + fullAction);
        for (EventHandler handler : matchedHandlers) {
            final EventHandler finalHandler = handler;
            Logger.info("  -> Invoking handler: " + finalHandler.getClass().getSimpleName());
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    finalHandler.handle(message);
                } catch (Exception e) {
                    Logger.error("Error in event handler: " + finalHandler.getAction() + " (" + finalHandler.getClass().getSimpleName() + ")", e);
                }
            });
        }

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
     * Get the total number of registered handler instances across all actions.
     */
    public int size() {
        return handlers.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Get the number of unique actions with handlers registered.
     */
    public int actionCount() {
        return handlers.size();
    }

    /**
     * Check if any handler is registered for an action.
     */
    public boolean hasHandler(String action) {
        List<EventHandler> handlerList = handlers.get(action);
        return handlerList != null && !handlerList.isEmpty();
    }

    /**
     * Get the number of handlers registered for a specific action.
     */
    public int getHandlerCount(String action) {
        List<EventHandler> handlerList = handlers.get(action);
        return handlerList != null ? handlerList.size() : 0;
    }
}
