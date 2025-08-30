package io.nuxxader.chx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;



/**
 * Client for interacting with the CHX server.
 * This class provides methods to set, get, and delete data based on keys.
 * CHXClient is thread-safe.
 */
public class CHXClient implements AutoCloseable {

    private static final String DEFAULT_ADDR = "127.0.0.1:3800";
    private Socket conn;
    private PrintWriter writer;
    private BufferedReader reader;
    private final ReentrantLock lock;

    /**
     * Creates a new instance of {@link CHXClient} and connects to the CHX server at the given address.
     *
     * @param address The address of the CHX server in "host:port" format. If null or empty, the default address "127.0.0.1:3800" will be used.
     * @throws IOException If an I/O error occurs during the connection to the server.
     * @throws IllegalArgumentException If the address format is invalid.
     */
    public CHXClient(String address) throws IOException {
        if (address == null || address.isEmpty()) {
            address = DEFAULT_ADDR;
        }
        String[] parts = address.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid address format. Expected host:port");
        }
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        try {
            this.conn = new Socket(host, port);
            this.writer = new PrintWriter(conn.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            this.lock = new ReentrantLock();
        } catch (IOException e) {
            throw new IOException("Failed to connect to CHX server", e);
        }
    }

    /**
     * Retrieves the value associated with the given key from the CHX server.
     *
     * @param key The key to retrieve the value for.
     * @return The value associated with the key, or {@link Optional#empty()} if the key is not found.
     * @throws IOException If an I/O error occurs during communication with the server.
     */
    public Optional<String> get(String key) throws IOException {
        if (key == null || key.isEmpty()) return Optional.empty();
        return sendCommand("G "+ key);
    }

    /**
     * Sets the value associated with the given key on the CHX server.
     *
     * @param key   The key to set the value for.
     * @param value The value to set.
     * @throws IOException If an I/O error occurs during communication with the server.
     */
    public void set(String key, String value) throws IOException {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) return;
        sendCommand("S "+ key + " " + value);
    }

    /**
     * Deletes the key and its associated value from the CHX server.
     *
     * @param key The key to delete.
     * @throws IOException If an I/O error occurs during communication with the server.
     */
    public void delete(String key) throws IOException {
        if (key == null || key.isEmpty()) return;
        sendCommand("D "+ key);
    }

    /**
     * Closes the connection to the CHX server and releases associated resources.
     *
     * @throws IOException If an I/O error occurs during the closing of the connection.
     */
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    /**
     * Reads the response from the CHX server.
     *
     * @return The response from the CHX server.
     * @throws IOException If an I/O error occurs while reading the response.
     */
    private Optional<String> sendCommand(String command) throws IOException {
        if (writer == null) {
            throw new IOException("Writer is null. The client may be closed.");
        }

        lock.lock();
        writer.printf(command + "\n");
        char[] buffer = new char[524288];
        int bytesRead = 0;

        try {
            try {
                bytesRead = reader.read(buffer, 0, 524288);
            } catch (IOException e) {
                throw new IOException("Error reading response from server: " + e.getMessage(), e);
            }
            
            if (bytesRead == -1) {
                throw new IOException("Server closed connection or sent empty response.");
            }
            String response = new String(buffer, 0, bytesRead).trim();

            Optional<String> result = Optional.empty();

            if (response.startsWith(">")) {
                result = Optional.of(response.substring(1));
            }

            lock.unlock();
            return result;
        } catch (IOException e)  {
            lock.unlock();
            throw e;
        }
    }
}