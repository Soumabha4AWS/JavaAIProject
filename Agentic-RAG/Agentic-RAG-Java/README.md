# Agentic RAG Support Assistant

This project is a Java demo that shows how to build an agentic retrieval-augmented generation (RAG) system with LangChain4j for a support-ticket assistant.

Instead of relying on a single fixed retrieval step, the agent can choose from several tools to answer questions, retrieve ticket details, filter by category, and summarize the ticket database.

## What this project demonstrates

- An agent that uses tools to answer support questions
- Semantic search over past support tickets using vector embeddings
- Ticket lookup by ID
- Category-based filtering
- Ticket statistics and summaries
- A conversational loop with memory across multiple turns

## Project idea

The app loads a sample dataset of support tickets from `synthetic_tickets.json`, builds an in-memory vector store, and exposes annotated tool methods to a LangChain4j AI service.

This is a good example of "agentic RAG" because the model can decide when to use retrieval versus other structured operations, rather than always following one hard-coded pipeline.

## Main files

- `src/main/java/com/example/agenticrag/SupportTicketTools.java` - defines the support-ticket tools and vector store
- `src/main/java/com/example/agenticrag/SupportAssistant.java` - declares the LangChain4j AI service and memory-aware chat method
- `src/main/java/com/example/agenticrag/AgenticRagDemo.java` - runs all six example scenarios
- `pom.xml` - Maven dependencies and run configuration
- `synthetic_tickets.json` – sample ticket data used for retrieval
- `requirements.txt` – Python dependencies

## Requirements

- Java 17+
- Maven 3.8+
- An OpenAI API key

Build dependencies:

```bash
mvn compile
```

## Setup

Set your OpenAI API key in your environment before running the demo:

```bash
setx OPENAI_API_KEY "your-api-key"
```

For the current PowerShell session:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
```

## Running the examples

Run the Java demo from the project root:

```bash
mvn exec:java
```

These scripts cover:

1. Troubleshooting queries that trigger semantic ticket search
2. Ticket lookup by ID
3. Category-based searches
4. Database statistics
5. Multi-step reasoning
6. Conversational follow-up questions

## How the flow works

1. The app loads support tickets from the JSON file.
2. It creates embeddings and stores them in an in-memory vector store.
3. The agent receives a user query.
4. If needed, it calls one or more tools such as:
   - `searchSimilarTickets`
   - `getTicketById`
   - `searchByCategory`
   - `getTicketStatistics`
5. The tool results are returned to the model, which then produces a final answer.

## Notes

- The vector store is built in memory at startup, so each run creates a fresh index.
- LangChain4j handles the tool-call loop and `MessageWindowChatMemory` preserves conversational follow-ups.
- This project is intended as a learning/demo project rather than a production-ready support system.
