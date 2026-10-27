# LlamaIndex Java - Indexing Strategies Demonstration

A standalone Java Maven application demonstrating four different indexing strategies from LlamaIndex, converted from Python. Provides semantic search, LLM-based relevance evaluation, keyword matching, and hybrid retrieval approaches.

## 📋 Overview

This project implements four indexing strategies for searching support tickets:

1. **Vector Index** - Semantic similarity search using embeddings
2. **Summary Index** - LLM-based relevance evaluation
3. **Keyword Index** - Inverted index with exact keyword matching
4. **Hybrid Index** - Combines vector + keyword search with result fusion

## 🏗️ Project Structure

```
llama-index-java/
├── pom.xml                          # Maven configuration
├── synthetic_tickets.json           # Sample data (25 support tickets)
├── src/
│   ├── main/java/com/indexing/
│   │   ├── cli/
│   │   │   └── IndexingDemo.java    # Main entry point
│   │   ├── config/
│   │   │   ├── ConfigurationManager.java    # Configuration & API setup
│   │   │   └── DataLoader.java             # JSON data loading
│   │   ├── index/
│   │   │   ├── VectorIndex.java            # Semantic embedding search
│   │   │   ├── SummaryIndex.java           # LLM relevance evaluation
│   │   │   ├── KeywordIndex.java           # Keyword inverted index
│   │   │   └── HybridIndex.java            # Combined vector + keyword
│   │   ├── model/
│   │   │   ├── Document.java               # Document wrapper
│   │   │   ├── SearchResult.java           # Search result with score
│   │   │   └── SupportTicket.java          # JSON data model
│   │   ├── query/
│   │   │   ├── QueryEngine.java            # Query interface
│   │   │   ├── QueryResponse.java          # Response wrapper
│   │   │   ├── VectorQueryEngine.java      # Vector search engine
│   │   │   ├── SummaryQueryEngine.java     # Summary search engine
│   │   │   ├── KeywordQueryEngine.java     # Keyword search engine
│   │   │   └── HybridQueryEngine.java      # Hybrid search engine
│   │   └── util/
│   │       ├── OpenAIClient.java           # OpenAI API wrapper
│   │       └── VectorUtils.java            # Vector math utilities
│   ├── main/resources/                     # Configuration files
│   └── test/java/                          # Unit tests
└── target/                                  # Compiled output
```

## 🚀 Quick Start

### Prerequisites

- **Java 11+**
- **Maven 3.6+**
- **OpenAI API Key** (for embeddings and LLM calls)

### Setup

1. **Clone/Download the project**
   ```bash
   cd llama-index-java
   ```

2. **Set your OpenAI API Key**
   ```bash
   # Option 1: Environment variable
   export OPENAI_API_KEY=sk-...
   
   # Option 2: System property
   mvn clean compile
   ```

3. **Build the project**
   ```bash
   mvn clean package
   ```

4. **Run the application**
   ```bash
   # With environment variable
   export OPENAI_API_KEY=sk-...
   java -jar target/llama-index-java-jar-with-dependencies.jar
   
   # Or with system property
   java -Dopenai.api.key=sk-... -jar target/llama-index-java-jar-with-dependencies.jar
   ```

## 📊 Indexing Strategies Explained

### 1. Vector Index (Part 1)
**Best for:** Semantic similarity search, finding conceptually related documents

**How it works:**
- Embeds all documents using OpenAI's `text-embedding-3-small` model
- Stores embeddings in memory for fast lookup
- At query time, embeds query and finds top-K documents using cosine similarity
- Returns results sorted by semantic similarity score (0-1)

**Pros:**
- ✅ Captures semantic meaning (synonyms, paraphrases)
- ✅ Fast similarity search
- ✅ Simple and effective

**Cons:**
- ❌ No hierarchical structure
- ❌ May return fragmented chunks
- ❌ All documents treated equally

**Usage:**
```java
VectorIndex vectorIndex = new VectorIndex(openAIClient, embeddingModel);
vectorIndex.buildIndex(documents);
List<SearchResult> results = vectorIndex.search("authentication issues", 3);
```

---

### 2. Summary Index (Part 2)
**Best for:** High-level queries, full document context needed

**How it works:**
- Stores all documents without embeddings
- For each query, evaluates every document using LLM for relevance
- LLM judges: "Is this document relevant to the query?"
- Returns documents sorted by LLM-determined relevance score

**Pros:**
- ✅ Good for high-level queries
- ✅ Returns full documents (not fragments)
- ✅ Nuanced relevance evaluation

**Cons:**
- ❌ Slow for large datasets (O(n) LLM calls)
- ❌ Expensive (many API calls)
- ❌ Doesn't scale well

**Usage:**
```java
SummaryIndex summaryIndex = new SummaryIndex(openAIClient, llmModel);
summaryIndex.buildIndex(documents);
List<SearchResult> results = summaryIndex.search("password reset", 3);
```

---

### 3. Keyword Index (Part 3)
**Best for:** Exact term matching, ticket IDs, error codes

**How it works:**
- Extracts keywords from each document using LLM
- Builds inverted index: keyword → [document IDs]
- At query time, extracts keywords from query
- Finds documents containing query keywords
- Scores by number of matching keywords

**Pros:**
- ✅ No embeddings needed
- ✅ Good for exact keyword queries
- ✅ Fast lookup

**Cons:**
- ❌ No semantic understanding
- ❌ Misses synonyms
- ❌ Requires good keyword extraction

**Inverted Index Example:**
```
"authentication" → [T-001, T-005, T-012]
"password"       → [T-001, T-005, T-020]
"reset"          → [T-001, T-003, T-008]

Query: "password reset" 
Matches: T-001 (2 keywords), T-003 (1), T-005 (1), T-008 (1), T-020 (1)
```

**Usage:**
```java
KeywordIndex keywordIndex = new KeywordIndex(openAIClient, llmModel);
keywordIndex.buildIndex(documents);
List<SearchResult> results = keywordIndex.search("TICK-001", 3);
```

---

### 4. Hybrid Index (Part 4)
**Best for:** Production use, combining strengths of multiple approaches

**How it works:**
1. Performs vector search (semantic)
2. Performs keyword search (exact)
3. Fuses results: documents found by BOTH methods get highest priority
4. Returns combined list sorted by fusion score

**Fusion Strategy:**
- Documents found by both methods = highest confidence (likely most relevant)
- Vector-only matches = semantic relevance
- Keyword-only matches = exact term matches
- Simple union approach (can upgrade to Reciprocal Rank Fusion)

**Pros:**
- ✅ Best of both worlds
- ✅ Semantic + exact matching
- ✅ More robust to query variations
- ✅ Higher overall accuracy

**Cons:**
- ❌ Slower (multiple searches)
- ❌ Complex result fusion logic
- ❌ More expensive (embeddings + keywords)

**Example Fusion:**
```
Query: "authentication timeout T-123"

Vector Results:        Keyword Results:
[T-002: 0.89]    →     [T-123: 3 keywords]
[T-005: 0.85]    ←→    [T-002: 2 keywords]
[T-012: 0.75]    →     [T-008: 1 keyword]

Fused Results (found by BOTH prioritized):
1. T-002 (both) - semantic + keyword
2. T-123 (keyword only) - exact match
3. T-005 (vector only) - semantic match
```

**Usage:**
```java
VectorIndex vectorIdx = new VectorIndex(openAIClient, embeddingModel);
KeywordIndex keywordIdx = new KeywordIndex(openAIClient, llmModel);
HybridIndex hybridIndex = new HybridIndex(vectorIdx, keywordIdx);
hybridIndex.buildIndex(documents);
List<SearchResult> results = hybridIndex.search("query", 5, 5);
```

## 🔄 Data Flow

### Document Loading
```
synthetic_tickets.json
  ↓ (Jackson ObjectMapper)
[SupportTicket, SupportTicket, ...]
  ↓ (DataLoader.loadDocuments())
[Document(text, metadata), ...]
  ↓ (Index.buildIndex())
Index ready for search
```

### Query Execution
```
User Query: "How to fix auth issues?"
  ↓
QueryEngine.query(queryText)
  ↓
Index.search(queryText, topK)
  ↓ (Index-specific logic)
  ├─→ Vector: embed query + similarity search
  ├─→ Summary: LLM evaluates each doc
  ├─→ Keyword: extract keywords + inverted index lookup
  └─→ Hybrid: combine vector + keyword
  ↓
List<SearchResult> (sorted by score)
  ↓
QueryResponse (with source documents)
  ↓
Print results to console
```

## 🛠️ Core Components

### ConfigurationManager
- Loads OpenAI API key from environment
- Configures models and timeouts
- Singleton pattern for global access

### OpenAIClient
- Wraps OpenAI API calls (embeddings + LLM)
- Implements retry logic with exponential backoff
- Handles timeouts and errors gracefully

### Document Model
- Text content + metadata (ticket_id, category, priority, etc.)
- NodeId for tracking across indexes

### VectorUtils
- Cosine similarity calculation
- Euclidean distance
- Vector normalization
- Batch validation

## 📝 Configuration

Edit `ConfigurationManager.java` to customize:

```java
this.embeddingModel = "text-embedding-3-small";  // Embedding model
this.llmModel = "gpt-4o-mini";                   // LLM model
this.embeddingTimeout = 120_000;                 // 2 min
this.llmTimeout = 300_000;                       // 5 min
this.maxRetries = 5;                             // Retry attempts
```

## 🧪 Testing the Application

Run the demo to see all four index types in action:

```bash
java -Dopenai.api.key=sk-... -jar target/llama-index-java-jar-with-dependencies.jar
```

The application will:
1. Load 25 sample support tickets
2. Build all 4 indexes
3. Execute test query against each index
4. Display results with source documents and scores
5. Show metrics and performance notes

**Expected Output:**
```
════════════════════════════════════════════════════════════════════════════════
LlamaIndex Java - Indexing Strategies Demo
════════════════════════════════════════════════════════════════════════════════

✓ Configuration loaded
  ConfigurationManager{embeddingModel='text-embedding-3-small', llmModel='gpt-4o-mini', ...}

✓ Loaded 25 documents from synthetic_tickets.json

════════════════════════════════════════════════════════════════════════════════
PART 1: Vector Index (Flat Index)
════════════════════════════════════════════════════════════════════════════════

Building Vector Index from documents...
  ✓ Processed 5 documents...
  ✓ Processed 10 documents...
  ✓ Processed 15 documents...
  ✓ Processed 20 documents...
  ✓ Processed 25 documents...
✓ Vector Index ready with 25 documents

Test Query: 'How do I fix authentication issues after password reset?'

Vector Index Results:
Found 3 semantically similar documents.
1. [Score: 0.8234] TICK-001
   Score: 0.8234
   Ticket ID: TICK-001...

Source Documents:
1. TICK-001
   Score: 0.8234
   Ticket ID: TICK-001...
```

## 🔌 API Integration

### OpenAI Embeddings API
- **Endpoint:** `POST https://api.openai.com/v1/embeddings`
- **Model:** `text-embedding-3-small`
- **Timeout:** 2 minutes
- **Retries:** 5 with exponential backoff

### OpenAI Chat API
- **Endpoint:** `POST https://api.openai.com/v1/chat/completions`
- **Model:** `gpt-4o-mini`
- **Timeout:** 5 minutes
- **Retries:** 5 with exponential backoff

**Request/Response Format:**
```bash
# Embedding Request
curl -X POST https://api.openai.com/v1/embeddings \
  -H "Authorization: Bearer sk-..." \
  -H "Content-Type: application/json" \
  -d '{"model":"text-embedding-3-small", "input":"text to embed"}'

# Chat Request
curl -X POST https://api.openai.com/v1/chat/completions \
  -H "Authorization: Bearer sk-..." \
  -H "Content-Type: application/json" \
  -d '{
    "model":"gpt-4o-mini",
    "messages":[{"role":"user","content":"prompt"}],
    "temperature":0.7,
    "max_tokens":1000
  }'
```

## 📊 Performance Characteristics

| Index Type | Build Time | Query Time | Memory | Cost |
|-----------|-----------|-----------|--------|------|
| Vector | Slow (embeds all) | Fast (cosine) | O(n*d) | High (embeddings) |
| Summary | Fast (no embed) | Slow (O(n) LLM) | O(n) | Very High (LLM/doc) |
| Keyword | Medium (LLM extract) | Fast (lookup) | O(K) | Medium (keyword extract) |
| Hybrid | Slowest (both) | Medium | O(n*d+K) | Highest (both) |

**Notes:**
- n = number of documents
- d = embedding dimension (1536 for text-embedding-3-small)
- K = number of unique keywords

## 🐛 Troubleshooting

### OpenAI API Key Not Found
```
Error: OPENAI_API_KEY must be set in environment variables
```
**Solution:**
```bash
export OPENAI_API_KEY=sk-...
# OR
java -Dopenai.api.key=sk-... -jar target/...
```

### synthetic_tickets.json Not Found
```
Error: Could not find synthetic_tickets.json in any standard location
```
**Solution:** Ensure `synthetic_tickets.json` is in the project root or copy it:
```bash
cp ../synthetic_tickets.json .
```

### Timeout Errors
```
Error: API request failed with status 408: Request Timeout
```
**Solution:** Increase timeout in `ConfigurationManager.java`:
```java
this.llmTimeout = 600_000; // 10 minutes
```

### Out of Memory
**Solution:** Increase Java heap size:
```bash
java -Xmx2g -Dopenai.api.key=sk-... -jar target/...
```

## 🔐 Security Considerations

1. **API Key Management:**
   - Never commit API keys to version control
   - Use environment variables in production
   - Rotate keys regularly

2. **Data Privacy:**
   - Support ticket data stays in memory
   - No data persisted to disk
   - Use over encrypted connections (HTTPS)

3. **Rate Limiting:**
   - OpenAI API has rate limits
   - Implement request throttling for large batches
   - Monitor API usage costs

## 📚 References

- **LlamaIndex Documentation:** https://docs.llamaindex.ai/
- **OpenAI API Docs:** https://platform.openai.com/docs/api-reference
- **Jackson JSON:** https://github.com/FasterXML/jackson
- **Apache Commons Math:** https://commons.apache.org/proper/commons-math/

## 📄 License

This project is provided as-is for educational and commercial use.

## 🤝 Contributing

Suggestions for improvements:
- Add persistence layer (database, vector DB)
- Implement Reciprocal Rank Fusion (RRF) for better hybrid results
- Add caching layer for embeddings
- Support for custom LLM models
- Distributed processing for large document sets
- Web API interface (REST/GraphQL)

## 📞 Support

For questions or issues:
1. Check logs for detailed error messages
2. Review the troubleshooting section
3. Verify OpenAI API credentials and model availability
4. Check network connectivity to OpenAI API
