package com.codeawareness.pycharm.communication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a Code Awareness protocol message.
 * Message format:
 * {
 *   "flow": "req|res|err",
 *   "domain": "code|auth|*",
 *   "action": "action_name",
 *   "data": {...},
 *   "caw": "client_guid"
 * }
 */
public class Message {

    public enum Flow {
        REQ("req"),    // Request
        RES("res"),    // Response
        ERR("err");    // Error

        private final String value;

        Flow(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Flow fromString(String value) {
            for (Flow flow : values()) {
                if (flow.value.equals(value)) {
                    return flow;
                }
            }
            throw new IllegalArgumentException("Unknown flow: " + value);
        }
    }

    private Flow flow;
    private String domain;
    private String action;
    private JsonElement data;
    private String caw;

    public Message() {
    }

    public Message(Flow flow, String domain, String action, JsonElement data, String caw) {
        this.flow = flow;
        this.domain = domain;
        this.action = action;
        this.data = data;
        this.caw = caw;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public String getCaw() {
        return caw;
    }

    public void setCaw(String caw) {
        this.caw = caw;
    }

    /**
     * Get data as JsonObject, or null if not an object.
     */
    public JsonObject getDataAsObject() {
        if (data != null && data.isJsonObject()) {
            return data.getAsJsonObject();
        }
        return null;
    }

    /**
     * Check if this is a request message.
     */
    public boolean isRequest() {
        return flow == Flow.REQ;
    }

    /**
     * Check if this is a response message.
     */
    public boolean isResponse() {
        return flow == Flow.RES;
    }

    /**
     * Check if this is an error message.
     */
    public boolean isError() {
        return flow == Flow.ERR;
    }

    @Override
    public String toString() {
        return "Message{" +
                "flow=" + flow +
                ", domain='" + domain + '\'' +
                ", action='" + action + '\'' +
                ", caw='" + caw + '\'' +
                ", data=" + data +
                '}';
    }
}
