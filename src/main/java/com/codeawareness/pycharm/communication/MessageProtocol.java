package com.codeawareness.pycharm.communication;

import com.codeawareness.pycharm.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Message protocol handler for Code Awareness.
 * Handles JSON serialization/deserialization with form-feed delimiters.
 */
public class MessageProtocol {

    /**
     * Form-feed delimiter used to separate messages.
     */
    public static final char DELIMITER = '\f';

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Serialize a message to JSON string with form-feed delimiter.
     */
    public static String serialize(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        try {
            // Convert Message to JsonObject for custom serialization
            JsonObject json = new JsonObject();
            json.addProperty("flow", message.getFlow().getValue());

            if (message.getDomain() != null) {
                json.addProperty("domain", message.getDomain());
            }

            if (message.getAction() != null) {
                json.addProperty("action", message.getAction());
            }

            if (message.getData() != null) {
                json.add("data", message.getData());
            }

            if (message.getCaw() != null) {
                json.addProperty("caw", message.getCaw());
            }

            String jsonString = GSON.toJson(json);
            String result = jsonString + DELIMITER;

            Logger.trace("Serialized message: " + jsonString);
            return result;
        } catch (Exception e) {
            Logger.error("Failed to serialize message", e);
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    /**
     * Deserialize a JSON string to a Message object.
     * The string should NOT include the delimiter.
     */
    public static Message deserialize(String json) {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        try {
            // Remove any trailing delimiter if present
            if (json.endsWith(String.valueOf(DELIMITER))) {
                json = json.substring(0, json.length() - 1);
            }

            JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);

            Message message = new Message();

            // Parse flow (required)
            if (jsonObject.has("flow")) {
                String flowValue = jsonObject.get("flow").getAsString();
                message.setFlow(Message.Flow.fromString(flowValue));
            } else {
                throw new IllegalArgumentException("Message missing required 'flow' field");
            }

            // Parse domain (optional)
            if (jsonObject.has("domain")) {
                message.setDomain(jsonObject.get("domain").getAsString());
            }

            // Parse action (optional)
            if (jsonObject.has("action")) {
                message.setAction(jsonObject.get("action").getAsString());
            }

            // Parse data (optional)
            if (jsonObject.has("data")) {
                message.setData(jsonObject.get("data"));
            }

            // Parse caw (optional)
            if (jsonObject.has("caw")) {
                message.setCaw(jsonObject.get("caw").getAsString());
            }

            Logger.trace("Deserialized message: " + message);
            return message;
        } catch (JsonSyntaxException e) {
            // Invalid JSON is an expected error condition, don't log as error
            Logger.debug("Invalid JSON syntax: " + json);
            throw new IllegalArgumentException("Invalid JSON syntax", e);
        } catch (IllegalArgumentException e) {
            // Missing required fields is an expected error condition
            Logger.debug("Invalid message: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Unexpected errors should still be logged
            Logger.error("Failed to deserialize message: " + json, e);
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }

    /**
     * Validate that a message has required fields.
     */
    public static boolean isValid(Message message) {
        if (message == null) {
            return false;
        }

        // Flow is required
        if (message.getFlow() == null) {
            return false;
        }

        return true;
    }

    /**
     * Get the GSON instance used for serialization.
     */
    public static Gson getGson() {
        return GSON;
    }
}
