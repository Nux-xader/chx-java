import io.nuxxader.chx.CHXClient;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SampleApp {

    private static final int NUM_THREADS = 10;
    private static final int OPERATIONS_PER_THREAD = 100;

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:3800";

        try (CHXClient client = new CHXClient(serverAddress)) {
            System.out.println("Successfully connected to CHX server at " + serverAddress);

            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

            // A shared key for the delete demonstration
            final String sharedKey = "shared-key-for-delete";
            final String sharedValue = "shared-value";

            // Thread 0 will be responsible for the delete cycle
            System.out.println("Starting " + NUM_THREADS + " threads. Thread 0 will handle DELETE demonstration.");

            for (int i = 0; i < NUM_THREADS; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        if (threadId == 0) {
                            // --- Thread 0: Manages the lifecycle of the shared key ---
                            System.out.println("Thread 0: Starting DELETE cycle for key '" + sharedKey + "'");

                            // 1. Set the key
                            client.set(sharedKey, sharedValue);
                            System.out.println("Thread 0 (SET): key='" + sharedKey + "', value='" + sharedValue + "'");

                            // 2. Verify it exists
                            Optional<String> valueBeforeDelete = client.get(sharedKey);
                            if (valueBeforeDelete.isPresent()) {
                                System.out.println("Thread 0 (GET before delete): Found value '" + valueBeforeDelete.get() + "' - OK");
                            } else {
                                System.err.println("Thread 0 (GET before delete): FAILED to find key '" + sharedKey + "'");
                            }

                            // 3. Delete the key
                            client.delete(sharedKey);
                            System.out.println("Thread 0 (DELETE): key='" + sharedKey + "'");

                            // 4. Verify it's gone
                            Optional<String> valueAfterDelete = client.get(sharedKey);
                            if (!valueAfterDelete.isPresent()) {
                                System.out.println("Thread 0 (GET after delete): Key not found as expected - OK");
                            } else {
                                System.err.println("Thread 0 (GET after delete): FAILED, value still exists for key '" + sharedKey + "'");
                            }
                        } else {
                            // --- Other Threads: Attempt to access the shared key ---
                            // Give Thread 0 a moment to set the key
                            Thread.sleep(50); // Small delay
                            
                            Optional<String> retrievedValue = client.get(sharedKey);
                            if (retrievedValue.isPresent()) {
                                System.out.println("Thread " + threadId + " (GET): Found key '" + sharedKey + "' with value '" + retrievedValue.get() + "'");
                            } else {
                                // This is an expected outcome if Thread 0 has already deleted the key
                                System.out.println("Thread " + threadId + " (GET): Key '" + sharedKey + "' not found (may have been deleted).");
                            }
                        }

                        // --- Original workload for all threads ---
                        for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                            String key = "key-" + threadId + "-" + j;
                            String value = "value-" + threadId + "-" + j;
                            
                            client.set(key, value);
                            Optional<String> retrievedValue = client.get(key);
                            if (!retrievedValue.isPresent() || !retrievedValue.get().equals(value)) {
                                System.err.println("Thread " + threadId + ": ERROR - Mismatch for " + key);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Thread " + threadId + ": Operation failed. Error: " + e.getMessage());
                    } catch (InterruptedException e) {
                        System.err.println("Thread " + threadId + " was interrupted.");
                        Thread.currentThread().interrupt();
                    }
                });
            }

            // Shutdown the executor and wait for all tasks to complete
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Threads did not terminate in 60 seconds. Forcing shutdown.");
                    executor.shutdownNow();
                } else {
                    System.out.println("All threads have completed their tasks.");
                }
            } catch (InterruptedException e) {
                System.err.println("Termination was interrupted. Forcing shutdown.");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }

        } catch (IOException e) {
            System.err.println("Failed to connect to the CHX server at " + serverAddress + ". Please ensure the server is running.");
            e.printStackTrace();
        }
    }
}