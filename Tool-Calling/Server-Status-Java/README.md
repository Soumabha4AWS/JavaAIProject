# Server Status Agent - Java Edition

A Java-based AI-powered server monitoring and incident response agent using OpenAI's GPT-4o model with function calling capabilities.

## Project Overview

This is a Maven-based Java conversion of a Node.js project that monitors server health and performs automated remediation. The agent:
- Checks server health metrics (CPU, memory)
- Fetches recent logs for diagnostics
- Automatically restarts services when needed
- Escalates complex issues to human engineers

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
- **Maven 3.8 or higher**
- **OpenAI API Key** (update in the code)

## Setup Instructions

### 1. Verify Java and Maven Installation

```bash
java -version
mvn -version
```

### 2. Configure OpenAI API Key

Open `src/main/java/com/serverstatus/service/OpenAIService.java` and `AgentService.java` and replace the API key placeholder with your actual OpenAI API key:

```java
private static final String API_KEY = "your-api-key-here";
```

### 3. Build the Project

```bash
mvn clean compile
```

### 4. Package the Application

```bash
mvn clean package
```

This creates a fat JAR with all dependencies included in the `target/` directory.

### 5. Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.serverstatus.ServerStatusAgent"
```

Or run the compiled JAR:

```bash
java -jar target/server-status-agent-1.0.0-shaded.jar
```

## Project Structure

```
Server-Status/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/serverstatus/
│   │   │   ├── ServerStatusAgent.java      # Main entry point
│   │   │   ├── model/
│   │   │   │   ├── ServerMetrics.java      # Server health data model
│   │   │   │   ├── LogResponse.java        # Log query response model
│   │   │   │   └── ToolResponse.java       # Generic tool response model
│   │   │   ├── service/
│   │   │   │   ├── OpenAIService.java      # OpenAI client initialization
│   │   │   │   ├── ServerStatusService.java # Business logic for tools
│   │   │   │   └── AgentService.java       # Agent orchestration
│   │   │   └── tools/
│   │   │       └── ToolRegistry.java       # Tool function registry
│   │   └── resources/
│   │       └── logback.xml                 # Logging configuration
│   └── test/
└── .gitignore
```

## Dependencies

- **OpenAI Java SDK**: For API communication
- **Jackson**: For JSON serialization/deserialization
- **SLF4J + Logback**: For logging
- **OkHttp**: HTTP client for OpenAI SDK

## Key Features

### Available Tools
1. **get_server_health** - Check CPU and memory usage
2. **fetch_recent_logs** - Retrieve server logs for diagnostics
3. **restart_service** - Restart a service automatically
4. **escalate_to_engineer** - Create a ticket for human review

### Scenarios

The application runs 4 test scenarios:
- **Scenario A**: High CPU (98%) - triggers automatic restart
- **Scenario B**: Healthy server - escalates for investigation
- **Scenario C**: High memory (95%) - memory leak detection
- **Scenario D**: Dependency failure - escalates due to external issue

## Configuration

### Logging Levels
Edit `src/main/resources/logback.xml` to adjust logging verbosity:
- INFO: Standard logging
- DEBUG: Detailed logging (default for com.serverstatus package)

### Java Version
The project targets Java 17. To change:
1. Edit `pom.xml`
2. Update `<maven.compiler.source>` and `<maven.compiler.target>` properties

## Error Handling

The application gracefully handles:
- OpenAI API errors
- JSON serialization issues
- Missing server IDs
- Tool execution failures

## Notes

- API keys should be stored in environment variables in production
- Replace hardcoded API key with proper secret management
- Add database integration for real server metrics
- Implement proper logging strategy for production use

## Maven Commands Reference

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Run application directly
mvn exec:java -Dexec.mainClass="com.serverstatus.ServerStatusAgent"

# Skip tests during build
mvn clean package -DskipTests

# View dependency tree
mvn dependency:tree
```

## Troubleshooting

### Issue: "Maven command not found"
- Ensure Maven is installed and `MAVEN_HOME` is in your PATH

### Issue: "No toolchains found in the Maven toolchains file"
- Run with: `mvn -Dorg.slf4j.simpleLogger.defaultLogLevel=debug clean package`

### Issue: OpenAI API errors
- Verify your API key is correct and has required permissions
- Check API rate limits and quotas

## License

This project is converted from Node.js to Java using Maven and JDK 17.
