package com.codeawareness.pycharm.communication;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageBuilder.
 */
class MessageBuilderTest {

    @Test
    void testBuildRequest() {
        Message message = MessageBuilder.request()
                .domain("code")
                .action("test")
                .caw("123456-789012")
                .build();

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("code", message.getDomain());
        assertEquals("test", message.getAction());
        assertEquals("123456-789012", message.getCaw());
    }

    @Test
    void testBuildResponse() {
        Message message = MessageBuilder.response()
                .domain("code")
                .action("test")
                .build();

        assertEquals(Message.Flow.RES, message.getFlow());
    }

    @Test
    void testBuildError() {
        Message message = MessageBuilder.error()
                .domain("*")
                .action("error")
                .build();

        assertEquals(Message.Flow.ERR, message.getFlow());
    }

    @Test
    void testBuildWithJsonObject() {
        JsonObject data = new JsonObject();
        data.addProperty("key", "value");

        Message message = MessageBuilder.request()
                .data(data)
                .build();

        assertNotNull(message.getData());
        assertEquals(data, message.getData());
    }

    @Test
    void testBuildClientId() {
        String guid = "123456-789012";
        Message message = MessageBuilder.buildClientId(guid);

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("*", message.getDomain());
        assertEquals("clientId", message.getAction());
        assertEquals(guid, message.getCaw());

        JsonObject data = message.getDataAsObject();
        assertNotNull(data);
        assertEquals(guid, data.get("guid").getAsString());
    }

    @Test
    void testBuildClientDisconnect() {
        String guid = "123456-789012";
        Message message = MessageBuilder.buildClientDisconnect(guid);

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("*", message.getDomain());
        assertEquals("clientDisconnect", message.getAction());
        assertEquals(guid, message.getCaw());
    }

    @Test
    void testBuildActivePath() {
        String guid = "123456-789012";
        String filePath = "/path/to/file.py";
        String docName = "file.py";

        Message message = MessageBuilder.buildActivePath(guid, filePath, docName);

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("code", message.getDomain());
        assertEquals("active-path", message.getAction());
        assertEquals(guid, message.getCaw());

        JsonObject data = message.getDataAsObject();
        assertNotNull(data);
        assertEquals(filePath, data.get("fpath").getAsString());
        assertEquals(docName, data.get("doc").getAsString());
    }

    @Test
    void testBuildFileSaved() {
        String guid = "123456-789012";
        String filePath = "/path/to/file.py";
        String docName = "file.py";

        Message message = MessageBuilder.buildFileSaved(guid, filePath, docName);

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("code", message.getDomain());
        assertEquals("file-saved", message.getAction());

        JsonObject data = message.getDataAsObject();
        assertNotNull(data);
        assertEquals(filePath, data.get("fpath").getAsString());
        assertEquals(docName, data.get("doc").getAsString());
    }

    @Test
    void testBuildAuthInfo() {
        String guid = "123456-789012";
        Message message = MessageBuilder.buildAuthInfo(guid);

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("*", message.getDomain());
        assertEquals("auth:info", message.getAction());
        assertEquals(guid, message.getCaw());
    }

    @Test
    void testBuildDiffPeer() {
        String guid = "123456-789012";
        String origin = "https://github.com/user/repo.git";
        String filePath = "/path/to/file.py";
        String peerGuid = "654321-210987";

        Message message = MessageBuilder.buildDiffPeer(guid, origin, filePath, peerGuid);

        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("code", message.getDomain());
        assertEquals("diff-peer", message.getAction());

        JsonObject data = message.getDataAsObject();
        assertNotNull(data);
        assertEquals(origin, data.get("origin").getAsString());
        assertEquals(filePath, data.get("fpath").getAsString());
        assertEquals(peerGuid, data.get("peer").getAsString());
    }

    @Test
    void testBuildWithoutFlow() {
        assertThrows(IllegalStateException.class, () -> {
            new MessageBuilder().build();
        });
    }
}
