# chx-java

chx-java is a Java library for interacting with a CHX server. It allows you to set, get, and delete data based on keys.

## Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven

## Building the Project

To build this project into a production-ready JAR file, use the following command:

```bash
mvn clean install
```

This command will compile the source code, run the test cases, and create a JAR file in the `target` directory.

## Using the Library

To use the `chx-java` library in another Java project, add the following dependency to your project's `pom.xml` file:

```xml
<dependency>
    <groupId>io.nuxxader</groupId>
    <artifactId>chx</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then, you can use the `CHXClient` class to interact with the CHX server:

```java
import io.nuxxader.chx.CHXClient;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        CHXClient client = new CHXClient("127.0.0.1:3800");
        client.set("foo", "bar");
        Optional<String> value = client.get("foo");
        value.ifPresent(System.out::println); // Output: bar
        client.delete("foo");
        client.close();
    }
}
```

## Additional Information

*   The CHX server must be configured correctly for this library to function.
*   The constructor throws an `IllegalArgumentException` if the server address format is invalid.
*   This library throws a `CHXServerException` if an error occurs while interacting with the CHX server.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the GNU GPL version 3. See the [LICENSE](LICENSE) file for details.