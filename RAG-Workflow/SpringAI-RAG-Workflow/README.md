# Spring AI RAG Workflow

This project is the Spring Boot/Spring AI version of the support-ticket RAG example.

## Features

- Loads `synthetic_tickets.json` into Spring AI `Document` objects.
- Embeds and stores documents in an in-memory `SimpleVectorStore`.
- Retrieves the top three relevant tickets and applies a 0.5 similarity threshold.
- Generates grounded answers with OpenAI `gpt-4o-mini` and cites ticket IDs.
- Maintains multi-turn conversation history by conversation ID.

## Run

Prerequisites: Java 17+, Maven, and an OpenAI API key.

```powershell
$env:OPENAI_API_KEY = "your-api-key"
mvn spring-boot:run
```

The application listens on `http://localhost:8080`.

## API

Load the bundled tickets into the vector store:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/rag/tickets/load
```

The response contains the number of tickets currently indexed. Loading is idempotent for the lifetime of the application.

Single-turn query:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/rag/query `
  -ContentType "application/json" `
  -Body '{"question":"How do I fix login failures after a password reset?"}'
```

Multi-turn query:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/rag/conversations/demo/messages `
  -ContentType "application/json" `
  -Body '{"question":"What was the resolution for that issue?"}'
```

Each response contains an `answer` and the retrieved `sources`.

## Configuration

Override settings with environment variables or Spring configuration. The defaults are:

- Chat model: `gpt-4o-mini`
- Embedding model: `text-embedding-3-small`
- Retrieval top-k: `3`
- Minimum similarity: `0.5`

`SimpleVectorStore` is intentionally in-memory for this educational workflow. Replace it with a persistent Spring AI vector store such as Chroma or PGVector when deployment persistence is required.
