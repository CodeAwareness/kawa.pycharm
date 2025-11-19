package com.codeawareness.pycharm;

import com.codeawareness.pycharm.communication.Message;
import com.codeawareness.pycharm.communication.MessageBuilder;
import com.codeawareness.pycharm.utils.Logger;

import java.io.IOException;

/**
 * Example of how to use the Code Awareness connection.
 * This is for documentation/testing purposes only.
 */
public class ConnectionExample {

    /**
     * Example: Connect to Code Awareness backend and send messages.
     */
    public static void exampleUsage(CodeAwarenessApplicationService appService) {
        try {
            // Connect to backend
            appService.connect();

            // Send an active-path notification
            Message message = MessageBuilder.buildActivePath(
                    appService.getClientGuid(),
                    "/path/to/file.py",
                    "file.py"
            );

            appService.getIpcConnection().sendMessage(message);

            // Send a message with response handler
            Message authRequest = MessageBuilder.buildAuthInfo(appService.getClientGuid());
            appService.getIpcConnection().sendMessage(authRequest, response -> {
                Logger.info("Received auth response: " + response);
            });

        } catch (IOException e) {
            Logger.error("Connection error", e);
        }
    }

    /**
     * Example: Set up message callback for incoming messages.
     */
    public static void exampleMessageCallback(CodeAwarenessApplicationService appService) {
        appService.getIpcConnection().setMessageCallback(message -> {
            Logger.info("Received message: " + message.getAction());

            // Handle different message types
            if ("peer:select".equals(message.getAction())) {
                Logger.info("Peer selected");
            } else if ("branch:select".equals(message.getAction())) {
                Logger.info("Branch selected");
            }
        });
    }
}
