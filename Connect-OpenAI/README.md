# Connect-OpenAI Java Version

This project is a Java conversion of the original Node.js OpenAI wrapper for GPT-4o-mini chat completion.

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Building

```bash
mvn clean package
```

## Running

```bash
mvn exec:java -Dexec.mainClass="com.openai.OpenAIConnectMain"
```

Or after building:

```bash
java -jar target/connect-openai-1.0.0.jar
```

## Project Structure

```
src/main/java/com/openai/
├── OpenAIConnectMain.java    - Entry point (equivalent to openaiConnectMain.js)
└── OpenAIConnectModule.java  - OpenAI module (equivalent to openaiConnectModule.js)
```

## Dependencies

- **jackson-databind**: JSON processing
- **okhttp**: HTTP client

## Changes from Node.js Version

1. **Asynchronous Handling**: Uses Java's `CompletableFuture` instead of Promise
2. **Build Tool**: Maven instead of npm
3. **Package Management**: Maven dependencies in `pom.xml` instead of `package.json`
4. **HTTP Client**: Uses native Java HTTP with Jackson for JSON

## Notes

- API key is hardcoded for testing purposes (not recommended for production)
- Maintains the same async behavior as the original Node.js version
- Error handling is similar to the original implementation
