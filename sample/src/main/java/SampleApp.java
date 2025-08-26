import io.nuxxader.chx.CHXClient;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class SampleApp {

    public static void main(String[] args) throws Exception {
        String serverAddress = "127.0.0.1:3800";
        int numberOfThreads = 100;
        String key = "testKey";
        String value = "testValue";

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        
        try (CHXClient client = new CHXClient(serverAddress)) {
            try {
                // Test set
                for (int i = 0; i < numberOfThreads; i++) {
                    executorService.execute(() -> {
                        try {
                            client.set(key, value);
                            System.out.println(Thread.currentThread().getName() + ": Set command executed");
                        } catch (Exception e) {
                            System.err.println(Thread.currentThread().getName() + ": Error setting value: " + e.getMessage());
                        }
                    });
                }

                // Test get
                for (int i = 0; i < numberOfThreads; i++) {
                    executorService.execute(() -> {
                        try {
                            Optional<String> retrievedValue = client.get(key);
                            retrievedValue.ifPresent(val -> System.out.println(Thread.currentThread().getName() + ": Retrieved value: " + val));
                            if (retrievedValue.isEmpty()) {
                                System.out.println(Thread.currentThread().getName() + ": Key not found");
                            }
                        } catch (Exception e) {
                            System.err.println(Thread.currentThread().getName() + ": Error getting value: " + e.getMessage());
                        }
                    });
                }

                // Test delete
                for (int i = 0; i < numberOfThreads; i++) {
                    executorService.execute(() -> {
                        try {
                            client.delete(key);
                            System.out.println(Thread.currentThread().getName() + ": Delete command executed");
                        } catch (Exception e) {
                            System.err.println(Thread.currentThread().getName() + ": Error deleting value: " + e.getMessage());
                        }
                    });
                }
            } finally {
                executorService.shutdown();
                executorService.awaitTermination(5, TimeUnit.MINUTES);
            }
        }
    }
}