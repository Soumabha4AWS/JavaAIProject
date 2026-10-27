# LangChain4j Chunking & Vector Store Demo

A Java/Maven project demonstrating various text chunking strategies and vector store operations using **LangChain4j**.

## Overview

This project converts the Python LangChain demonstration into a Java implementation using LangChain4j. It showcases:

- **Chunking Strategies**: Fixed-size, recursive, and whole-document approaches
- **Vector Stores**: Embedding generation and similarity search
- **Metadata Filtering**: Targeted retrieval based on document properties
- **Retrieval Comparison**: Evaluating different chunking techniques

## Project Structure

```
chunking-vector-store/
├── pom.xml                              # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/langchain/demo/
│   │   │   ├── ChunkingVectorStoreDemo.java     # Main application
│   │   │   ├── model/
│   │   │   │   └── SupportTicket.java           # Data model
│   │   │   ├── service/
│   │   │   │   ├── ChunkingService.java         # Chunking implementations
│   │   │   │   └── VectorStoreService.java      # Vector store operations
│   │   │   └── util/
│   │   │       └── DataLoader.java              # JSON data loading
│   │   └── resources/
│   │       └── logback.xml                      # Logging configuration
│   └── test/java/                               # Unit tests
├── synthetic_tickets.json                       # Sample data (20 support tickets)
└── README.md                                    # This file
```

## Prerequisites

- **Java 17+** (JDK 17 or later)
- **Maven 3.9+**
- **OpenAI API Key** (for embedding generation)

## Dependencies

### Core
- `langchain4j-core`: Core LangChain4j functionality
- `langchain4j-open-ai`: OpenAI integration for embeddings
- `langchain4j-chroma`: Chroma vector store (optional for advanced features)

### Utilities
- `jackson-databind`: JSON processing
- `slf4j-api` + `logback-classic`: Logging
- `junit-jupiter`: Testing framework

## Getting Started

### 1. Clone/Setup the Project

```bash
cd d:\Workspace\Java\Interview KickStart\Chunking
```

### 2. Set OpenAI API Key

```bash
# Windows PowerShell
$env:OPENAI_API_KEY = "sk-proj-..."

# Linux/macOS
export OPENAI_API_KEY="sk-proj-..."
```

### 3. Build the Project

```bash
mvn clean compile
```

### 4. Run the Application

```bash
# Using Maven
mvn exec:java@ChunkingVectorStoreDemo

# Or build and run the JAR
mvn clean package
java -jar target/chunking-vector-store-1.0.0-shaded.jar
```

## Key Features

### 1. Chunking Strategies

#### Fixed-Size Chunking
```java
// Splits text into equal-sized chunks with overlap
chunkingService.fixedSizeChunking(documents, 200, 20);
```
- **Pros**: Simple, predictable
- **Cons**: May split mid-sentence, loses semantic context

#### Recursive Character Splitting (RECOMMENDED)
```java
// Splits on natural boundaries: paragraphs → sentences → words
chunkingService.recursiveChunking(documents, 300, 50);
```
- **Pros**: Preserves semantic boundaries, works for most content
- **Cons**: Not semantic-aware (doesn't understand meaning)

#### Whole Document (No Chunking)
```java
// Returns documents as-is
chunkingService.wholeDocumentChunking(documents);
```
- **Use When**: Documents are already small and self-contained
- **Our Case**: Support tickets (200-400 chars each)

### 2. Vector Store Operations

```java
// Initialize embedding model
EmbeddingModel embeddingModel = new OpenAiEmbeddingModel.Builder()
    .apiKey(apiKey)
    .modelName("text-embedding-3-small")
    .build();

// Create vector store and add documents
VectorStoreService vectorStore = new VectorStoreService(embeddingModel);
vectorStore.addDocuments(documents);

// Perform similarity search
List<StoredDocument> results = vectorStore.similaritySearch(
    "Authentication problems after password reset", 
    3  // Return top 3 results
);
```

### 3. Metadata Filtering

```java
// Filter by category during search
List<StoredDocument> filtered = vectorStore.similaritySearchWithFilter(
    "Login issues",
    3,
    "category",
    "Authentication"
);

// Filter by priority
List<StoredDocument> highPriority = vectorStore.similaritySearchWithFilter(
    "Database issues",
    3,
    "priority",
    "High"
);
```

## Sample Data

The project includes `synthetic_tickets.json` with 20 realistic support tickets covering:

- Authentication issues
- Database problems
- Payment processing
- Mobile app crashes
- Performance issues
- Email notifications
- Search functionality
- File uploads
- API rate limiting
- Session management
- And more...

Each ticket contains:
- `ticket_id`: Unique identifier (e.g., "TICK-001")
- `title`: Brief description
- `category`: Issue category
- `priority`: Critical, High, Medium
- `description`: Problem details
- `resolution`: How it was fixed
- `created_date`, `resolved_date`: Timestamps

## Logging

Logs are configured in `src/main/resources/logback.xml` and output to:
- **Console**: Real-time output (INFO level and above)
- **File**: `chunking-demo.log` in the project root

Adjust log levels by modifying the configuration file:
```xml
<root level="DEBUG">  <!-- Change to DEBUG for verbose output -->
    ...
</root>
```

## Comparison with Python Version

| Feature | Python (Langchain) | Java (LangChain4j) |
|---------|-------------------|-------------------|
| Chunking | `TextSplitters` | `DocumentSplitters` |
| Embeddings | `OpenAIEmbeddings` | `OpenAiEmbeddingModel` |
| Vector Store | `Chroma` | `ChromaEmbeddingStore` (optional) |
| Document Model | `Document` class | `Document` class |
| Metadata | `metadata` dict | `Metadata` object |

## Advanced Topics

### Using Chroma Vector Store (Production Ready)

For persistent storage and production use, integrate Chroma:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-chroma</artifactId>
    <version>0.31.0</version>
</dependency>
```

### Custom Chunking Logic

Implement custom splitting in `ChunkingService`:

```java
public List<Document> customChunking(List<Document> documents) {
    DocumentSplitter splitter = DocumentSplitters.custom()
        .maxSegmentSize(400)
        .overlapSize(50)
        .build();
    
    return documents.stream()
        .flatMap(doc -> splitter.split(doc).stream())
        .toList();
}
```

### Semantic Chunking

For semantic-aware chunking based on topic changes, use `dev.langchain4j.rag` features.

## Key Learnings

1. **Chunking Matters**: Different strategies significantly impact retrieval quality
2. **Balance Context**: Too-small chunks lose context; too-large chunks dilute relevance
3. **Metadata is Powerful**: Filtering enables efficient, targeted searches
4. **Vector Databases**: Critical for production RAG systems
5. **Embedding Models**: Choice of model affects semantic understanding

## Troubleshooting

### Missing OPENAI_API_KEY

```
Error: OPENAI_API_KEY environment variable not set
```

**Solution**: Set the environment variable before running:
```bash
export OPENAI_API_KEY="your-api-key"
mvn exec:java@ChunkingVectorStoreDemo
```

### Slow Initialization

First run takes time as Maven downloads dependencies. Subsequent runs are faster.

### Memory Issues

Increase heap size:
```bash
MAVEN_OPTS="-Xmx2g" mvn exec:java@ChunkingVectorStoreDemo
```

## Next Steps

1. **Module 3**: Indexing Strategies and RAG Pipeline
2. **Module 4**: Building Q&A Systems
3. **Module 5**: Production Deployment

## References

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [OpenAI Embeddings API](https://platform.openai.com/docs/guides/embeddings)
- [Chroma Documentation](https://docs.trychroma.com/)
- [Text Chunking Guide](https://www.pinecone.io/learn/chunking-strategies/)

## License

MIT

## Support

For issues or questions:
1. Check the logs in `chunking-demo.log`
2. Verify OPENAI_API_KEY is set correctly
3. Ensure Java 17+ and Maven 3.9+ are installed
4. Check LangChain4j documentation for version-specific issues

---

**Created as part of the Interview Kickstart LangChain workshop**
