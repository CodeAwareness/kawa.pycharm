package com.codeawareness.pycharm.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResponseHandlerRegistry.
 */
class ResponseHandlerRegistryTest {

    private ResponseHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ResponseHandlerRegistry();
    }

    @Test
    void testRegisterAndHandle() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        AtomicReference<Object> receivedData = new AtomicReference<>();

        registry.register("test-key", data -> {
            handlerCalled.set(true);
            receivedData.set(data);
        });

        String testData = "test data";
        boolean handled = registry.handle("test-key", testData);

        assertTrue(handled);
        assertTrue(handlerCalled.get());
        assertEquals(testData, receivedData.get());
    }

    @Test
    void testHandlerCalledOnlyOnce() {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        int[] callCount = {0};

        registry.register("test-key", data -> {
            handlerCalled.set(true);
            callCount[0]++;
        });

        // First call should invoke handler
        assertTrue(registry.handle("test-key", "data1"));
        assertEquals(1, callCount[0]);

        // Second call should not invoke handler (already removed)
        assertFalse(registry.handle("test-key", "data2"));
        assertEquals(1, callCount[0]); // Still 1
    }

    @Test
    void testHandleNonExistentKey() {
        boolean handled = registry.handle("non-existent", "data");
        assertFalse(handled);
    }

    @Test
    void testHandleNullKey() {
        boolean handled = registry.handle(null, "data");
        assertFalse(handled);
    }

    @Test
    void testRegisterNullKey() {
        registry.register(null, data -> {});
        assertEquals(0, registry.size());
    }

    @Test
    void testRegisterNullHandler() {
        registry.register("test-key", null);
        assertEquals(0, registry.size());
    }

    @Test
    void testRemove() {
        registry.register("test-key", data -> {});
        assertEquals(1, registry.size());

        registry.remove("test-key");
        assertEquals(0, registry.size());

        boolean handled = registry.handle("test-key", "data");
        assertFalse(handled);
    }

    @Test
    void testClear() {
        registry.register("key1", data -> {});
        registry.register("key2", data -> {});
        registry.register("key3", data -> {});

        assertEquals(3, registry.size());

        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void testSize() {
        assertEquals(0, registry.size());

        registry.register("key1", data -> {});
        assertEquals(1, registry.size());

        registry.register("key2", data -> {});
        assertEquals(2, registry.size());

        registry.handle("key1", "data");
        assertEquals(1, registry.size());

        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test
    void testHandlerException() {
        registry.register("test-key", data -> {
            throw new RuntimeException("Test exception");
        });

        // Handler should catch exception and return false
        boolean handled = registry.handle("test-key", "data");
        assertFalse(handled);

        // Handler should be removed even if it threw exception
        assertEquals(0, registry.size());
    }
}
