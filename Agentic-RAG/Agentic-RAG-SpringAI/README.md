# Agentic RAG Support Assistant

This project is a small Spring Boot and Spring AI demo that shows how to build an agentic retrieval-augmented generation (RAG) system for a support-ticket assistant.

Instead of relying on a single fixed retrieval step, the agent can choose from several tools to answer questions, retrieve ticket details, filter by category, and summarize the ticket database.

## What this project demonstrates

- An agent that uses tools to answer support questions
- Semantic search over past support tickets using vector embeddings
- Ticket lookup by ID
- Category-based filtering
- Ticket statistics and summaries
- A conversational loop with memory across multiple turns

## Project idea

The app loads a sample dataset of support tickets from `synthetic_tickets.json`, builds an in-memory vector store, and exposes tool-based capabilities to a Spring AI `ChatClient`.

This is a good example of "agentic RAG" because the model can decide when to use retrieval versus other structured operations, rather than always following one hard-coded pipeline.

## Main files

- `src/main/java/com/example/agenticrag/SupportTicketTools.java` - defines the support-ticket tools and vector store
- `src/main/java/com/example/agenticrag/SupportAssistantService.java` - configures the ChatClient and conversation memory
- `src/main/java/com/example/agenticrag/SupportAssistantController.java` - exposes the chat endpoint
- `synthetic_tickets.json` – sample ticket data used for retrieval
- `pom.xml` - Spring Boot and Spring AI dependencies
- `pom.xml` - Spring Boot and Spring AI dependencies

## Requirements

- Java 17+
- Maven 3.8+
- An OpenAI API key

Build the project:

```bash
mvn clean package
```

## Setup

Set your OpenAI API key in your environment before running the application:

```bash
setx OPENAI_API_KEY "your-api-key"
```

If you are using PowerShell in the current session, you can also do:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
```

## Running the application

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

Send a query to `POST /api/chat/back-and-forth`:

```bash
curl -X POST http://localhost:8080/api/chat/back-and-forth \
   -H "Content-Type: application/json" \
   -d '{"conversationId":"demo","message":"How do I fix authentication problems after password reset?"}'
```

The response includes the answer and the query rewrite used for the latest turn:

```json
{
  "answer": "...",
  "originalQuery": "How do I fix authentication problems after password reset?",
  "queryRewrite": "How do I fix authentication problems after password reset?"
}
```

Use the same `conversationId` for follow-up questions. The advisor keeps the previous turns in a message window, and the rewrite step resolves references using the previous question:

```json
{"conversationId":"demo","message":"What was the ticket ID for that?"}
```

The second response will contain a standalone `queryRewrite`, such as `Which ticket describes authentication problems after a password reset?`.

The original six scenarios are supported through the endpoint:

1. Troubleshooting queries that trigger semantic ticket search
2. Ticket lookup by ID
3. Category-based searches
4. Database statistics
5. Multi-step reasoning
6. Conversational follow-up questions

## How the flow works

1. The app loads support tickets from the JSON file.
2. It creates embeddings and stores them in an in-memory Spring AI vector store.
3. The agent receives a user query.
4. If needed, it calls one or more tools such as:
   - `searchSimilarTickets`
   - `getTicketById`
   - `searchByCategory`
   - `getTicketStatistics`
5. Spring AI executes the tool calls and returns the model's final answer.

## Notes

- The vector store is built in memory at startup, so each run creates a fresh index.
- The OpenAI key is read from `OPENAI_API_KEY`; no key is stored in source code.
- This project is intended as a learning/demo project rather than a production-ready support system.
