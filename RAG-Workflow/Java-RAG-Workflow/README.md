# Java RAG Workflow

This is the Java equivalent of the Python RAG demo. It uses LangChain4j with
OpenAI embeddings and chat completion, retrieves the top three support tickets
using cosine similarity, validates low-confidence matches, and supports
history-aware follow-up questions.

## Run

Requirements: Java 17+ and Maven.

Set the API key in the process environment, then run from this directory:

```powershell
$env:OPENAI_API_KEY = "your-api-key"
mvn compile exec:java
```

The loader reads `synthetic_tickets.json` from the project root. No API key is
stored in source code.