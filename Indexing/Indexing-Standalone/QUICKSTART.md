# Quick Start Guide - LlamaIndex Java

## 5-Minute Setup

### Step 1: Prerequisites Check
```bash
# Check Java version (need 11+)
java -version

# Check Maven version (need 3.6+)
mvn -version
```

### Step 2: Build the Project
```bash
cd llama-index-java
mvn clean package -DskipTests
```

**Expected output:** `BUILD SUCCESS`

### Step 3: Get OpenAI API Key
1. Go to https://platform.openai.com/api-keys
2. Create new secret key
3. Copy the key (format: `sk-...`)

### Step 4: Run the Application

**Option A: Using Environment Variable (Recommended)**
```bash
export OPENAI_API_KEY=sk-your-key-here
java -jar target/llama-index-java-jar-with-dependencies.jar
```

**Option B: Using System Property**
```bash
java -Dopenai.api.key=sk-your-key-here \
     -jar target/llama-index-java-jar-with-dependencies.jar
```

**Option C: Full Example with Memory Settings**
```bash
export OPENAI_API_KEY=sk-your-key-here
java -Xmx1g \
     -jar target/llama-index-java-jar-with-dependencies.jar
```

### Step 5: Review the Output
The application will:
- ✅ Load 25 sample support tickets
- ✅ Create 4 different indexes
- ✅ Run a test query against each
- ✅ Display results

**Typical Execution Time:**
- Vector Index: ~30 seconds (embedding all documents)
- Summary Index: ~2-3 minutes (LLM evaluation per document)
- Keyword Index: ~1 minute (keyword extraction)
- Hybrid Index: ~5 minutes (combination)

**Total: ~10-15 minutes for complete demo**

---

## Understanding the Output

### Vector Index Results Example
```
════════════════════════════════════════════════════════════════════════════════
PART 1: Vector Index (Flat Index)
════════════════════════════════════════════════════════════════════════════════

Vector Index Results:
Found 3 semantically similar documents.
1. [Score: 0.8234] TICK-001
2. [Score: 0.7892] TICK-005
3. [Score: 0.7456] TICK-012

Source Documents:
1. TICK-001
   Score: 0.8234
   Ticket ID: TICK-001
   Title: Users unable to log in after password reset
   ...
```

**Score Interpretation:**
- **0.9-1.0**: Highly relevant (very similar semantics)
- **0.7-0.9**: Relevant (similar topics)
- **0.5-0.7**: Somewhat relevant
- **0.0-0.5**: Not relevant

### Summary Index Results Example
```
════════════════════════════════════════════════════════════════════════════════
PART 2: Summary Index
════════════════════════════════════════════════════════════════════════════════

Summary Index Results:
Found 3 relevant documents via LLM evaluation.
1. [Relevance: 0.9500] TICK-001
2. [Relevance: 0.7200] TICK-015
3. [Relevance: 0.6800] TICK-003
```

**⏱️ Note:** Summary Index is slow because it evaluates EACH document with an LLM call.

### Keyword Index Results Example
```
════════════════════════════════════════════════════════════════════════════════
PART 3: Keyword Table Index
════════════════════════════════════════════════════════════════════════════════

✓ Extracted keywords from all documents
Sample keywords: authentication, password, reset, login, users, issue, database, connection, ...

Keyword Index Results:
Found 3 documents via keyword matching.
1. [Match Score: 1.0000] TICK-001  (2/2 keywords matched)
2. [Match Score: 0.5000] TICK-012  (1/2 keywords matched)
3. [Match Score: 0.5000] TICK-008  (1/2 keywords matched)
```

### Hybrid Index Results Example
```
════════════════════════════════════════════════════════════════════════════════
PART 5: Hybrid Retrieval
════════════════════════════════════════════════════════════════════════════════

Hybrid Retrieval Results (Combined-naive):
Found 4 documents via vector + keyword fusion.
1. [Score: 0.8234] TICK-001      (found by BOTH - highest confidence!)
2. [Score: 0.7892] TICK-005      (vector match - semantic)
3. [Score: 0.6500] TICK-012      (keyword match - exact)
4. [Score: 0.5231] TICK-003      (vector match - semantic)
```

**✅ Success:** Application completed all demonstrations

---

## Common Issues & Solutions

### Issue 1: "OPENAI_API_KEY not found"
```
Error: OPENAI_API_KEY must be set in environment variables
```
**Solution:**
```bash
# Linux/Mac
export OPENAI_API_KEY=sk-...

# Windows PowerShell
$env:OPENAI_API_KEY='sk-...'

# Windows CMD
set OPENAI_API_KEY=sk-...
```

### Issue 2: "synthetic_tickets.json not found"
```
Error: Could not find synthetic_tickets.json in any standard location
```
**Solution:**
```bash
# Ensure file exists in project root
ls -la synthetic_tickets.json

# If missing, copy from parent directory
cp ../synthetic_tickets.json .
```

### Issue 3: API Timeout Errors
```
Error: API request failed with status 408: Request Timeout
```
**Solution:**
- Increase timeout in code or use system property
- Check internet connection
- Verify API key is valid
- Check OpenAI API status at status.openai.com

### Issue 4: Out of Memory
```
Error: java.lang.OutOfMemoryError: Java heap space
```
**Solution:**
```bash
java -Xmx2g -Xms512m -Dopenai.api.key=sk-... \
     -jar target/llama-index-java-jar-with-dependencies.jar
```

### Issue 5: Build Failures
```
mvn clean package -DskipTests -X 2>&1 | tee build.log
```
**Check build.log for detailed error messages**

---

## Next Steps After Quick Start

### 1. Explore the Code
```bash
# Read the main entry point
cat src/main/java/com/indexing/cli/IndexingDemo.java

# Examine index implementations
cat src/main/java/com/indexing/index/VectorIndex.java
cat src/main/java/com/indexing/index/KeywordIndex.java
```

### 2. Customize for Your Data
Replace `synthetic_tickets.json` with your own data:
```json
[
  {
    "ticket_id": "YOUR-001",
    "title": "Your ticket title",
    "description": "Your description",
    "resolution": "How it was solved",
    "category": "Your category",
    "priority": "High/Medium/Low"
  }
]
```

### 3. Integrate into Your Application
```java
// Copy the relevant classes to your project
// Import and use the index classes:

VectorIndex vectorIndex = new VectorIndex(openAIClient, "text-embedding-3-small");
vectorIndex.buildIndex(yourDocuments);
List<SearchResult> results = vectorIndex.search("your query", 5);
```

### 4. Add Unit Tests
```bash
# Add tests to src/test/java/
# Run with:
mvn test
```

### 5. Deploy to Production
```bash
# Create fat JAR with Maven Shade or Assembly plugin
mvn clean package

# Deploy
java -Xmx2g \
     -Dopenai.api.key=$OPENAI_API_KEY \
     -jar target/llama-index-java-jar-with-dependencies.jar
```

---

## Performance Tuning

### For Large Datasets
```bash
# Increase memory
java -Xmx4g -Xms2g ...

# Use parallel processing (in code)
// Modify SummaryIndex to use larger thread pool
ExecutorService executor = Executors.newFixedThreadPool(8);
```

### For Speed
```bash
# Use Vector Index only (fastest)
# Disable Summary Index (slowest)
# Use smaller embedding batch sizes
```

### For Cost Reduction
```bash
# Use cheaper embedding model:
openai.embedding.model=text-embedding-3-large  # or ada-002

# Use smaller LLM:
openai.llm.model=gpt-3.5-turbo

# Cache embeddings/keywords between runs
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                 IndexingDemo.java                    │
│              (Main CLI Application)                 │
└─────────────────────────────────────────────────────┘
               ↓         ↓         ↓         ↓
        ┌──────┴─────────┴─────────┴─────────┴───┐
        │                                        │
   ┌────▼─────┐  ┌────────────┐  ┌────────────┐ │
   │VectorIdx │  │SummaryIdx  │  │KeywordIdx  │ │
   │(Semantic)│  │(LLM Based) │  │(Inverted)  │ │
   └────┬─────┘  └────────────┘  └────────────┘ │
        │              │              │         │
        └──────┬───────┴───────┬──────┘         │
               │               │                │
           Hybrid           QueryEngine         │
          (Combined)        (Executor)          │
               │               │                │
               └───────┬───────┘                │
                       │                       │
                   Results ◄─────────────────┘
                       │
                    Print to Console
```

---

## Support & Troubleshooting

### Get Help
1. Read the README.md
2. Check inline code comments
3. Review Apache logs: `target/logs/`
4. Check OpenAI API status

### Report Issues
Include:
- Java version: `java -version`
- Maven version: `mvn -version`
- Full error message
- Steps to reproduce
- Build log: `mvn clean package -X`

### Documentation
- README.md - Comprehensive guide
- Code comments - Explain what each class does
- Javadoc - API documentation

---

## Success Criteria

✅ **You're successful when:**
- Project builds with `mvn clean package`
- Application runs without errors
- All 4 indexes are created
- Results are returned and displayed
- Test query returns documents from all indexes

**Typical successful run takes 10-15 minutes end-to-end**

---

Good luck! 🚀
