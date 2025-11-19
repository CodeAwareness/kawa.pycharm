package com.codeawareness.pycharm.communication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageParser.
 */
class MessageParserTest {

    private MessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new MessageParser();
    }

    @Test
    void testParseSingleCompleteMessage() {
        String json = "{\"flow\":\"req\",\"domain\":\"code\",\"action\":\"test\"}\f";

        List<Message> messages = parser.parse(json);

        assertEquals(1, messages.size());
        Message message = messages.get(0);
        assertEquals(Message.Flow.REQ, message.getFlow());
        assertEquals("code", message.getDomain());
        assertEquals("test", message.getAction());
    }

    @Test
    void testParseMultipleMessages() {
        String json = "{\"flow\":\"req\",\"domain\":\"code\",\"action\":\"test1\"}\f" +
                "{\"flow\":\"res\",\"domain\":\"code\",\"action\":\"test2\"}\f";

        List<Message> messages = parser.parse(json);

        assertEquals(2, messages.size());
        assertEquals(Message.Flow.REQ, messages.get(0).getFlow());
        assertEquals("test1", messages.get(0).getAction());
        assertEquals(Message.Flow.RES, messages.get(1).getFlow());
        assertEquals("test2", messages.get(1).getAction());
    }

    @Test
    void testParseFragmentedMessage() {
        // First fragment (incomplete)
        String fragment1 = "{\"flow\":\"req\",\"domain\":\"code\"";
        List<Message> messages1 = parser.parse(fragment1);
        assertEquals(0, messages1.size());
        assertTrue(parser.hasIncompleteMessage());

        // Second fragment (completes message)
        String fragment2 = ",\"action\":\"test\"}\f";
        List<Message> messages2 = parser.parse(fragment2);
        assertEquals(1, messages2.size());
        assertEquals("test", messages2.get(0).getAction());
        assertFalse(parser.hasIncompleteMessage());
    }

    @Test
    void testParseEmptyString() {
        List<Message> messages = parser.parse("");
        assertEquals(0, messages.size());
    }

    @Test
    void testParseNull() {
        List<Message> messages = parser.parse(null);
        assertEquals(0, messages.size());
    }

    @Test
    void testParseInvalidJson() {
        String json = "{invalid json}\f";

        List<Message> messages = parser.parse(json);

        // Invalid message should be skipped, not throw exception
        assertEquals(0, messages.size());
    }

    @Test
    void testParseWithEmptyMessage() {
        String json = "\f{\"flow\":\"req\",\"domain\":\"code\",\"action\":\"test\"}\f\f";

        List<Message> messages = parser.parse(json);

        // Empty messages should be skipped
        assertEquals(1, messages.size());
        assertEquals("test", messages.get(0).getAction());
    }

    @Test
    void testBufferSize() {
        assertEquals(0, parser.getBufferSize());

        parser.parse("{\"flow\":\"req\"");
        assertTrue(parser.getBufferSize() > 0);

        parser.parse("}\f");
        assertEquals(0, parser.getBufferSize());
    }

    @Test
    void testClear() {
        parser.parse("{\"flow\":\"req\",\"incomplete");
        assertTrue(parser.getBufferSize() > 0);

        parser.clear();
        assertEquals(0, parser.getBufferSize());
        assertFalse(parser.hasIncompleteMessage());
    }

    @Test
    void testGetBufferContent() {
        String partial = "{\"flow\":\"req\"";
        parser.parse(partial);

        String bufferContent = parser.getBufferContent();
        assertEquals(partial, bufferContent);
    }

    @Test
    void testMultipleFragmentsMultipleMessages() {
        // Fragment 1: Incomplete message 1
        parser.parse("{\"flow\":\"req\",");

        // Fragment 2: Completes message 1, starts message 2
        List<Message> messages2 = parser.parse("\"action\":\"test1\"}\f{\"flow\":\"res\",");
        assertEquals(1, messages2.size());
        assertEquals("test1", messages2.get(0).getAction());

        // Fragment 3: Completes message 2
        List<Message> messages3 = parser.parse("\"action\":\"test2\"}\f");
        assertEquals(1, messages3.size());
        assertEquals("test2", messages3.get(0).getAction());

        assertFalse(parser.hasIncompleteMessage());
    }
}
