# Weather Report AI Agent - Spring Boot

A Spring Boot application that uses OpenAI's GPT-4o with tool-calling capabilities to provide weather reports. The agent can dynamically call the weather API to fetch real-time weather data for any location.

## Architecture

This is a conversion of the Python weather agent to Spring Boot using Spring AI framework.

### Components

1. **WeatherReportAgentApplication** - Main Spring Boot application entry point
2. **WeatherAgentService** - Core service implementing the AI agent logic using Spring AI ChatClient
3. **WeatherController** - REST API endpoint for querying weather
4. **WeatherApiClient** - HTTP client for calling the RapidAPI weather service
5. **WeatherAgentRunner** - CommandLineRunner for standalone execution

### Key Features

- **Tool-Calling Agent**: Uses OpenAI's function calling to dynamically invoke weather API
- **Spring AI Integration**: Leverages Spring AI framework for chat model interaction
- **REST API**: Expose weather queries via HTTP endpoints
- **Configurable**: API keys and model settings via application.yml

## Prerequisites

- Java 17+
- Maven 3.8+
- OpenAI API key
- RapidAPI weather-api167 key

## Configuration

Edit `application.yml` to set your API keys:

```yaml
spring:
  ai:
    openai:
      api-key: your-openai-api-key
      chat:
        options:
          model: gpt-4o
```

## Building

```bash
mvn clean package
```

## Running

### As a Spring Boot Application

```bash
mvn spring-boot:run
```

This runs the CommandLineRunner which queries "What is the weather of London".

### Via REST API

After starting the application, query:

```bash
curl "http://localhost:8080/weather?location=London"
```

## Project Structure

```
src/main/java/com/weather/ai/
├── WeatherReportAgentApplication.java  # Spring Boot entry point
├── controller/
│   └── WeatherController.java          # REST endpoints
├── service/
│   └── WeatherAgentService.java        # Core agent logic
├── config/
│   ├── WeatherToolsConfig.java         # Tool definitions
│   ├── WeatherRequest.java             # Tool input model
│   └── WeatherApiClient.java           # External API client
└── runner/
    └── WeatherAgentRunner.java         # CLI runner
```

## How It Works

1. User sends a weather query (e.g., "What is the weather of London")
2. **ChatClient** sends prompt to OpenAI GPT-4o with weather tool available
3. GPT-4o analyzes if it needs to call `getCurrentWeather` function
4. If needed, the function is invoked with location parameter
5. **WeatherApiClient** fetches data from RapidAPI weather service
6. Result is returned to GPT-4o for final response
7. Agent provides comprehensive weather summary to user

## Dependencies

- spring-boot-starter-web: Web server and REST support
- spring-ai-openai: Spring AI OpenAI integration
- jackson-databind: JSON processing
- lombok: Reduce boilerplate (optional)

## Notes

- API keys are embedded in configuration. For production, use environment variables or secure vaults
- The RapidAPI weather endpoint defaults to GB (Great Britain) locations
- GPT-4o model is configured, but can be changed to gpt-4-turbo or gpt-3.5-turbo

## Comparison with Python Version

| Aspect | Python | Spring Boot |
|--------|--------|------------|
| Framework | None (OpenAI SDK) | Spring Boot + Spring AI |
| HTTP Client | http.client | java.net.http.HttpClient |
| Execution | Script-based | Application server + CLI |
| API Exposure | None | REST endpoints |
| Tool-Calling | Manual loop | Spring AI abstraction |
| Configuration | Hardcoded | application.yml |
