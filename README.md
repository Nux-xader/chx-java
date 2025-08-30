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

Then, you can use the `CHXClient` class to interact with the CHX server. The following example shows how to use `CHXClient` to perform `set`, `get`, and `delete` operations:

```java
import io.nuxxader.chx.CHXClient;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        String serverAddress = "127.0.0.1:3800";
        String key = "testKey";
        String value = "testValue";

        // Use try-with-resources to ensure the client is closed properly
        try (CHXClient client = new CHXClient(serverAddress)) {
            // Set value
            client.set(key, value);
            System.out.println("Value set successfully");

            // Get value
            Optional<String> retrievedValue = client.get(key);
            retrievedValue.ifPresent(val -> System.out.println("Retrieved value: " + val));

            // Delete value
            client.delete(key);
            System.out.println("Value deleted successfully");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
```

**Key Features:**

*   **Thread Safety:** `CHXClient` is safe to use in multi-threaded environments.
*   **Error Handling:** The methods in `CHXClient` throw exceptions if there is a connection or I/O error.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the [GPL License](LICENSE).