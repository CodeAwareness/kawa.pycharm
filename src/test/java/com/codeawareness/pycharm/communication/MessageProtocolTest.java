package com.codeawareness.pycharm.communication;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageProtocol.
 */
class MessageProtocolTest {

    @Test
    void testSerializeBasicMessage() {
        Message message = MessageBuilder.request()
                .domain("code")
                .action("test-action")
                .caw("123456-789012")
                .build();

        String serialized = MessageProtocol.serialize(message);

        assertNotNull(serialized);
        assertTrue(serialized.endsWith(String.valueOf(MessageProtocol.DELIMITER)));
        assertTrue(serialized.contains("\"flow\": \"req\""));
        assertTrue(serialized.contains("\"domain\": \"code\""));
        assertTrue(serialized.contains("\"action\": \"test-action\""));
        assertTrue(serialized.contains("\"caw\": \"123456-789012\""));
    }

    @Test
    void testSerializeWithData() {
        JsonObject data = new JsonObject();
        data.addProperty("key", "value");
        data.addProperty("number", 42);

        Message message = MessageBuilder.request()
                .domain("code")
                .action("test")
                .data(data)
                .caw("123456-789012")
                .build();

        String serialized = MessageProtocol.serialize(message);

        assertTrue(serialized.contains("\"data\""));
        assertTrue(serialized.contains("\"key\": \"value\""));
        assertTrue(serialized.contains("\"number\": 42"));
    }

    @Test
    void testSerializeNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageProtocol.serialize(null);
        });
    }

    @Test
    void testDeserializeBasicMessage() {
        String json = "{\"flow\":\"req\",\"domain\":\"code\",\"action\":\"test\",\"caw\":\"123456-789012\"}";

        Message message = MessageProtocol.deserialize(json);

        assertNotNull(message);
        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("code", message.getDomain());
        assertEquals("test", message.getAction());
        assertEquals("123456-789012", message.getCaw());
    }

    @Test
    void testDeserializeWithData() {
        String json = "{\"flow\":\"res\",\"domain\":\"code\",\"action\":\"test\",\"data\":{\"key\":\"value\"},\"caw\":\"123456-789012\"}";

        Message message = MessageProtocol.deserialize(json);

        assertNotNull(message);
        assertEquals(Message.Flow.RES, message.getFlow());
        assertNotNull(message.getData());
        assertTrue(message.getData().isJsonObject());

        JsonObject data = message.getDataAsObject();
        assertEquals("value", data.get("key").getAsString());
    }

    @Test
    void testDeserializeWithDelimiter() {
        String json = "{\"flow\":\"req\",\"domain\":\"code\",\"action\":\"test\"}\f";

        Message message = MessageProtocol.deserialize(json);

        assertNotNull(message);
        assertEquals(Message.Flow.REQ, message.getFlow());
    }

    @Test
    void testDeserializeNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            MessageProtocol.deserialize(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            MessageProtocol.deserialize("");
        });
    }

    @Test
    void testDeserializeInvalidJson() {
        assertThrows(RuntimeException.class, () -> {
            MessageProtocol.deserialize("{invalid json}");
        });
    }

    @Test
    void testDeserializeMissingFlow() {
        assertThrows(RuntimeException.class, () -> {
            MessageProtocol.deserialize("{\"domain\":\"code\"}");
        });
    }

    @Test
    void testSerializeDeserializeRoundTrip() {
        JsonObject data = new JsonObject();
        data.addProperty("test", "value");

        Message original = MessageBuilder.request()
                .domain("code")
                .action("round-trip")
                .data(data)
                .caw("123456-789012")
                .build();

        String serialized = MessageProtocol.serialize(original);
        Message deserialized = MessageProtocol.deserialize(serialized);

        assertEquals(original.getFlow(), deserialized.getFlow());
        assertEquals(original.getDomain(), deserialized.getDomain());
        assertEquals(original.getAction(), deserialized.getAction());
        assertEquals(original.getCaw(), deserialized.getCaw());
        assertEquals(original.getData(), deserialized.getData());
    }

    @Test
    void testIsValid() {
        Message validMessage = MessageBuilder.request()
                .domain("code")
                .action("test")
                .build();

        assertTrue(MessageProtocol.isValid(validMessage));

        Message invalidMessage = new Message();
        invalidMessage.setFlow(null);

        assertFalse(MessageProtocol.isValid(invalidMessage));
        assertFalse(MessageProtocol.isValid(null));
    }

    @Test
    void testMessageFlowTypes() {
        Message request = MessageBuilder.request().build();
        assertTrue(request.isRequest());
        assertFalse(request.isResponse());
        assertFalse(request.isError());

        Message response = MessageBuilder.response().build();
        assertFalse(response.isRequest());
        assertTrue(response.isResponse());
        assertFalse(response.isError());

        Message error = MessageBuilder.error().build();
        assertFalse(error.isRequest());
        assertFalse(error.isResponse());
        assertTrue(error.isError());
    }
}
