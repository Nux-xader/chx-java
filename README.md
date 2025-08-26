# chx-java

chx-java is a Java library for interacting with a CHX server. It allows you to set, get, and delete data based on keys.

## Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven

## Building the Project

You can getting jar file from release page on this github repository or
Build this project into a production-ready JAR file, use the following command:

```bash
mvn clean install
```

This command will compile the source code, run the test cases, and create a JAR file in the `target` directory.

## Using the Library

To use the `chx-java` library in another Java project, add the following dependency to your project's `pom.xml` file:

```xml
    <dependencies>
        <dependency>
            <groupId>io.nuxxader</groupId>
            <artifactId>chx</artifactId>
            <version>x.x.x</version>
            <scope>system</scope>
            <systemPath>\somepath-to\chx-java-x.x.x.jar</systemPath>
        </dependency>
    </dependencies>
```

Then, you can use the `CHXClient` class to interact with the CHX server:

```java
import io.nuxxader.chx.CHXClient;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        String serverAddress = "127.0.0.1:3800";
        int numberOfThreads = 10;
        String key = "testKey";
        String value = "testValue";

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        try (CHXClient client = new CHXClient(serverAddress)) {
            // Test set
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(() -> {
                    try {
                        client.set(key, value);
                        System.out.println(Thread.currentThread().getName() + ": Set command executed");
                    } catch (CHXServerException e) {
                        System.err.println(Thread.currentThread().getName() + ": Error setting value: " + e.getMessage());
                    }
                });
            }

            // Test get
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(() -> {
                    try {
                        Optional<String> retrievedValue = client.get(key);
                        retrievedValue.ifPresent(val ->
                            System.out.println(Thread.currentThread().getName() + ": Retrieved value: " + val));
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
        } catch (Exception e) {
            System.err.println("Error creating client: " + e.getMessage());
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}
```

## Additional Information

*   The CHX server must be configured correctly for this library to function.
*   The constructor throws an `IllegalArgumentException` if the server address format is invalid.
*   The `CHXClient` constructor and methods `set`, `get`, and `delete` throw `CHXServerException` if there are issues communicating with the CHX server.
*   The `CHXClient` constructor throws an `IllegalArgumentException` if the server address format is invalid or if an invalid command is sent.
*   The sample code demonstrates how to use `ExecutorService` to perform concurrent operations.
*   It's important to call `executorService.shutdown()` and `executorService.awaitTermination()` to properly release resources.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the [GNU License](LICENSE).