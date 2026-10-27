# Quick Start Guide - LangChain4j Chunking & Vector Store

## 1. Set Up Your Environment

### Windows (PowerShell)
```powershell
$env:OPENAI_API_KEY = "sk-proj-your-api-key-here"
cd "d:\Workspace\Java\Interview KickStart\Chunking"
```

### Windows (Command Prompt)
```cmd
set OPENAI_API_KEY=sk-proj-your-api-key-here
cd "d:\Workspace\Java\Interview KickStart\Chunking"
```

### Linux/macOS
```bash
export OPENAI_API_KEY="sk-proj-your-api-key-here"
cd ~/path/to/chunking-vector-store
```

## 2. Verify Prerequisites

```bash
# Check Java version
java -version
# Should be Java 17+

# Check Maven version
mvn -version
# Should be Maven 3.9+
```

## 3. Build the Project

```bash
# Using Maven directly
mvn clean compile

# Using the provided script
./run.sh          # Linux/macOS
run.bat           # Windows
```

## 4. Run the Application

```bash
# Option A: Using Maven exec plugin
mvn exec:java@ChunkingVectorStoreDemo

# Option B: Using the build script
./run.sh          # Linux/macOS
run.bat           # Windows

# Option C: Create and run JAR
mvn clean package
java -jar target/chunking-vector-store-1.0.0-shaded.jar
```

## 5. Verify Output

You should see output like:
```
================================================================================
MODULE 2: CHUNKING & VECTOR STORES (Java/LangChain4j)
================================================================================

### STEP 1: Loading Support Tickets ###
✓ Loaded 20 support tickets

### STEP 2: Converting Tickets to Documents ###
✓ Created 20 documents

### STEP 3: Chunking Strategies ###
--- Strategy 1: Fixed-Size Chunking ---
✓ Created XX chunks
...
```

## Common Issues & Solutions

### Issue: OPENAI_API_KEY not set
**Error**: `OPENAI_API_KEY environment variable not set`
**Solution**: 
```bash
export OPENAI_API_KEY="sk-proj-..."  # Linux/macOS
$env:OPENAI_API_KEY="sk-proj-..."    # PowerShell
set OPENAI_API_KEY=sk-proj-...       # Command Prompt
```

### Issue: Maven not found
**Error**: `maven is not found`
**Solution**: Download from https://maven.apache.org/download.cgi

### Issue: Java version mismatch
**Error**: `incompatible types` or class version errors
**Solution**: Upgrade to Java 17+ (https://www.oracle.com/java/technologies/)

### Issue: API Key not working
**Error**: `Authentication failed` or `401 Unauthorized`
**Solution**: Verify your key at https://platform.openai.com/account/api-keys

## Project Structure

```
├── pom.xml                           # Maven configuration
├── src/
│   ├── main/java/com/langchain/demo/
│   │   ├── ChunkingVectorStoreDemo.java    # Main application
│   │   ├── model/SupportTicket.java        # Data model
│   │   ├── service/
│   │   │   ├── ChunkingService.java        # Chunking logic
│   │   │   └── VectorStoreService.java     # Vector operations
│   │   └── util/DataLoader.java            # JSON loading
│   └── resources/logback.xml               # Logging config
├── synthetic_tickets.json            # Sample data
└── README.md                         # Full documentation
```

## What This Demo Shows

1. **Loading Data**: Reads 20 support tickets from JSON
2. **Chunking Strategies**: 
   - Fixed-size chunking
   - Recursive character splitting
   - Whole document (no chunking)
3. **Vector Embeddings**: Generates embeddings using OpenAI API
4. **Similarity Search**: Finds related tickets using embeddings
5. **Metadata Filtering**: Filters results by category and priority

## Next Steps

1. Explore `ChunkingVectorStoreDemo.java` to see the main logic
2. Modify chunking parameters in `ChunkingService.java`
3. Add your own documents by modifying `DataLoader.java`
4. Implement custom vector store features in `VectorStoreService.java`

## Getting Help

1. Check `chunking-demo.log` for detailed error messages
2. Review README.md for comprehensive documentation
3. Verify LangChain4j docs: https://docs.langchain4j.dev/
4. Check OpenAI docs: https://platform.openai.com/docs/

---

**Happy Learning!** 🚀
