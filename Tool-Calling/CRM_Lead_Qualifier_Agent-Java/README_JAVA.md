# CRM Lead Qualifier Agent - Java Conversion

This is a Java conversion of the original Node.js CRM Lead Qualifier Agent. It uses the OpenAI Java SDK to interact with GPT-4 for lead qualification tasks.

## Project Structure

```
src/main/java/com/crmleadqualifier/
├── CrmLeadQualifierMain.java          # Entry point - runs both modules
├── OpenAIConnector.java               # OpenAI client initialization
├── CrmLeadQualifierFunctions.java     # Tool implementations (domain lookup, CRM check, lead score)
├── ToolsSchema.java                   # Tool definitions for OpenAI function calling
└── module/
    ├── CrmLeadQualifierModule1.java   # Agent with predefined step sequence
    └── CrmLeadQualifierModule2.java   # Agent with AI-guided sequence
```

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Build
```bash
mvn clean package
```

### Run
```bash
mvn exec:java -Dexec.mainClass="com.crmleadqualifier.CrmLeadQualifierMain"
```

Or after building:
```bash
java -cp target/classes com.crmleadqualifier.CrmLeadQualifierMain
```

## Configuration

The API key is currently hardcoded in `OpenAIConnector.java`. To use environment variables instead:

```java
String apiKey = System.getenv("OPENAI_API_KEY");
if (apiKey == null || apiKey.isEmpty()) {
    throw new RuntimeException("OPENAI_API_KEY environment variable not set.");
}
return OpenAIClient.builder().apiKey(apiKey).build();
```

## Key Differences from Node.js

1. **Type Safety**: Uses strong typing (Java generics, enums, custom classes)
2. **Async Handling**: Uses blocking OpenAI SDK calls instead of async/await
3. **JSON Processing**: Uses GSON library instead of native JavaScript objects
4. **Tool Schema**: Manually constructed using GSON JsonObject instead of JavaScript objects
5. **Error Handling**: Throws exceptions rather than returning undefined values

## Dependencies

- **openai-java**: Official OpenAI SDK for Java
- **gson**: JSON serialization/deserialization
- **log4j2**: Logging framework

See `pom.xml` for versions and additional details.

## Features

- Two agent implementations demonstrating different strategies:
  - **Module 1**: Follows strict predefined steps
  - **Module 2**: Lets AI choose the function call sequence
- Mock data for domain info and CRM history
- Lead scoring based on company revenue and opportunity status
- Full function calling support with OpenAI's GPT-4 model

## Next Steps

1. Externalize the API key to environment variables
2. Add database connectivity instead of mock data
3. Implement proper logging throughout
4. Add unit tests for functions and message handling
5. Consider async processing with Project Reactor or virtual threads
