package io.nuxxader.chx;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class AppTest {

    private CHXClient client;

    @BeforeEach
    void setUp() throws IOException {
        // Connect to the CHX server, which is assumed to be running at the default address "127.0.0.1:3800"
        try {
            client = new CHXClient(null);
        } catch (RuntimeException e) {
            // Provide a more informative message if the connection fails
            if (e.getCause() instanceof java.net.ConnectException) {
                fail("Connection to CHX server failed. Make sure the server is running at the default address.", e);
            }
            throw e;
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    @Test
    @DisplayName("Integration Test: Set, Get, and Delete")
    void testSetGetDelete_Integration() throws IOException {
        String key = "integration-test-" + UUID.randomUUID().toString();
        String value = "value-" + UUID.randomUUID().toString();

        try {
            // 1. Set
            client.set(key, value);

            // 2. Get
            Optional<String> retrievedValue = client.get(key);
            assertTrue(retrievedValue.isPresent(), "Value should be present after set.");
            assertEquals(value, retrievedValue.get(), "Retrieved value should match the set value.");

        } finally {
            // 3. Delete (cleanup)
            client.delete(key);
        }

        // 4. Verify Deletion
        Optional<String> valueAfterDelete = client.get(key);
        assertFalse(valueAfterDelete.isPresent(), "Value should not be present after deletion.");
    }

    @Test
    @DisplayName("Integration Test: Get Non-Existent Key")
    void testGet_NotFound_Integration() throws IOException {
        String key = "non-existent-key-" + UUID.randomUUID().toString();
        Optional<String> value = client.get(key);
        assertFalse(value.isPresent(), "Should return an empty Optional for a non-existent key.");
    }

    @Test
    @DisplayName("Test Get with Empty or Null Key")
    void testGet_InvalidKey() throws IOException {
        // Assumption: the client should internally reject invalid keys without sending them to the server.
        assertFalse(client.get("").isPresent(), "Get with empty key should return empty Optional.");
        assertFalse(client.get(null).isPresent(), "Get with null key should return empty Optional.");
    }

    @Test
    @DisplayName("Test Set with Empty or Null Key/Value")
    void testSet_InvalidInput() throws IOException {
        // The client should ignore set operations with invalid input.
        // There is no easy way to verify this without mocks, so we just call it
        // to ensure no exceptions are thrown.
        assertDoesNotThrow(() -> client.set("", "some-value"));
        assertDoesNotThrow(() -> client.set("some-key", ""));
        assertDoesNotThrow(() -> client.set(null, "some-value"));
        assertDoesNotThrow(() -> client.set("some-key", null));
    }

    @Test
    @DisplayName("Integration Test: Thread Safety")
    @Timeout(10)
    void testThreadSafety_Integration() throws InterruptedException, IOException {
        int numberOfThreads = 20;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        String keyPrefix = "thread-test-" + UUID.randomUUID().toString() + "-";

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadNum = i;
            String key = keyPrefix + threadNum;
            String value = "value" + threadNum;
            service.submit(() -> {
                try {
                    // Each thread sets and then deletes its own key
                    client.set(key, value);
                    Optional<String> retrieved = client.get(key);
                    assertTrue(retrieved.isPresent() && retrieved.get().equals(value));
                } catch (IOException e) {
                    fail("Exception in thread " + threadNum, e);
                } finally {
                    try {
                        client.delete(key); // Cleanup
                    } catch (IOException e) {
                        // ignore
                    }
                    latch.countDown();
                }
            });
        }

        latch.await();
        service.shutdown();
        assertTrue(service.awaitTermination(5, TimeUnit.SECONDS), "Executor service should shut down cleanly.");

        // Verify that all keys have been deleted
        for (int i = 0; i < numberOfThreads; i++) {
            assertFalse(client.get(keyPrefix + i).isPresent(), "Key " + i + " should be deleted after test.");
        }
    }

    @Test
    @DisplayName("Test Invalid Address Format")
    void testInvalidAddressFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new CHXClient("invalid-address"));
        assertEquals("Invalid address format. Expected host:port", exception.getMessage());
    }

    @Test
    @DisplayName("Test Connection Failure to Invalid Port")
    void testConnectionFailure() {
        // Attempt to connect to a port where no server is running
        IOException exception = assertThrows(IOException.class, () -> new CHXClient("127.0.0.1:9999"));
        assertEquals("Failed to connect to CHX server", exception.getMessage());
        assertInstanceOf(IOException.class, exception.getCause(), "Cause should be a IOException.");
    }
}