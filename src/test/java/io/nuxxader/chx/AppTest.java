package io.nuxxader.chx;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.ConnectException;

class AppTest {

    private static final String SERVER_ADDRESS = "127.0.0.1:3800";
    private CHXClient client;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testNewClient_validAddress() {
        try {
            client = new CHXClient(SERVER_ADDRESS);
            assertNotNull(client, "Client should not be null");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation: " + e.getMessage());
        }
    }

    @Test
    void testNewClient_invalidAddress() {
        assertThrows(ConnectException.class, () -> {
            client = new CHXClient("127.0.0.1:9999");
        }, "Expected ConnectException for invalid address");
    }

    @Test
    void testNewClient_defaultAddress() {
        try {
            client = new CHXClient("");
            assertNotNull(client, "Client should not be null with default address");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation with default address: " + e.getMessage());
        }
    }

    @Test
    void testGet_notFound() {
        try {
            client = new CHXClient(SERVER_ADDRESS);
            assertThrows(CHXServerException.class, () -> {
                client.get("nonexistent");
            }, "Expected CHXServerException for non-existent key");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation: " + e.getMessage());
        }
    }

    @Test
    void testGet_emptyKey() {
        try {
            client = new CHXClient(SERVER_ADDRESS);
            assertThrows(CHXServerException.class, () -> {
                client.get("");
            }, "Expected CHXServerException for empty key");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation: " + e.getMessage());
        }
    }

    @Test
    void testDelete_notFound() {
        try {
            client = new CHXClient(SERVER_ADDRESS);
            assertThrows(CHXServerException.class, () -> {
                client.delete("nonexistent");
            }, "Expected CHXServerException when deleting a non-existent key");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation: " + e.getMessage());
        }
    }

    @Test
    void testDelete_emptyKey() {
        try {
            client = new CHXClient(SERVER_ADDRESS);
            assertThrows(CHXServerException.class, () -> {
                client.delete("");
            }, "Expected CHXServerException for empty key");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation: " + e.getMessage());
        }
    }

    @Test
    void testConnectionClose() {
        try {
            client = new CHXClient(SERVER_ADDRESS);
            client.close();
            assertThrows(Exception.class, () -> {
                client.get("anykey");
            }, "Expected exception when getting from a closed connection");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during client creation or closing: " + e.getMessage());
        }
    }
}