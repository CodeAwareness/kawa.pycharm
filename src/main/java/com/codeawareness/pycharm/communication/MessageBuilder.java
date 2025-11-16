package com.codeawareness.pycharm.communication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Builder for constructing Code Awareness messages.
 */
public class MessageBuilder {

    private Message.Flow flow;
    private String domain;
    private String action;
    private JsonElement data;
    private String caw;

    private MessageBuilder() {
    }

    /**
     * Create a new request message builder.
     */
    public static MessageBuilder request() {
        MessageBuilder builder = new MessageBuilder();
        builder.flow = Message.Flow.REQ;
        return builder;
    }

    /**
     * Create a new response message builder.
     */
    public static MessageBuilder response() {
        MessageBuilder builder = new MessageBuilder();
        builder.flow = Message.Flow.RES;
        return builder;
    }

    /**
     * Create a new error message builder.
     */
    public static MessageBuilder error() {
        MessageBuilder builder = new MessageBuilder();
        builder.flow = Message.Flow.ERR;
        return builder;
    }

    /**
     * Set the domain.
     */
    public MessageBuilder domain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Set the action.
     */
    public MessageBuilder action(String action) {
        this.action = action;
        return this;
    }

    /**
     * Set the data as a JsonElement.
     */
    public MessageBuilder data(JsonElement data) {
        this.data = data;
        return this;
    }

    /**
     * Set the data as a JsonObject.
     */
    public MessageBuilder data(JsonObject data) {
        this.data = data;
        return this;
    }

    /**
     * Set the client GUID.
     */
    public MessageBuilder caw(String caw) {
        this.caw = caw;
        return this;
    }

    /**
     * Build the message.
     */
    public Message build() {
        if (flow == null) {
            throw new IllegalStateException("Flow must be set");
        }
        return new Message(flow, domain, action, data, caw);
    }

    /**
     * Build a clientId registration message.
     */
    public static Message buildClientId(String guid) {
        JsonObject data = new JsonObject();
        data.addProperty("guid", guid);

        return MessageBuilder.request()
                .domain("*")
                .action("clientId")
                .data(data)
                .caw(guid)
                .build();
    }

    /**
     * Build a clientDisconnect message.
     */
    public static Message buildClientDisconnect(String guid) {
        return MessageBuilder.request()
                .domain("*")
                .action("clientDisconnect")
                .caw(guid)
                .build();
    }

    /**
     * Build an active-path notification message.
     */
    public static Message buildActivePath(String guid, String filePath, String docName) {
        JsonObject data = new JsonObject();
        data.addProperty("fpath", filePath);
        data.addProperty("doc", docName);
        data.addProperty("caw", guid);

        return MessageBuilder.request()
                .domain("code")
                .action("active-path")
                .data(data)
                .caw(guid)
                .build();
    }

    /**
     * Build a file-saved notification message.
     */
    public static Message buildFileSaved(String guid, String filePath, String docName) {
        JsonObject data = new JsonObject();
        data.addProperty("fpath", filePath);
        data.addProperty("doc", docName);
        data.addProperty("caw", guid);

        return MessageBuilder.request()
                .domain("code")
                .action("file-saved")
                .data(data)
                .caw(guid)
                .build();
    }

    /**
     * Build an auth:info request message.
     */
    public static Message buildAuthInfo(String guid) {
        return MessageBuilder.request()
                .domain("*")
                .action("auth:info")
                .caw(guid)
                .build();
    }

    /**
     * Build a diff-peer request message.
     */
    public static Message buildDiffPeer(String guid, String origin, String filePath, String peerGuid) {
        JsonObject data = new JsonObject();
        data.addProperty("origin", origin);
        data.addProperty("fpath", filePath);
        data.addProperty("peer", peerGuid);
        data.addProperty("caw", guid);

        return MessageBuilder.request()
                .domain("code")
                .action("diff-peer")
                .data(data)
                .caw(guid)
                .build();
    }
}
