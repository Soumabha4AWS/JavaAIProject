#!/bin/bash

# Build and run script for LangChain Chunking & Vector Store Demo
# Usage: ./run.sh
#        ./run.sh clean
#        ./run.sh package

set -e

echo ""
echo "============================================================================"
echo "LangChain4j Chunking & Vector Store Demo - Build Script"
echo "============================================================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17+ from: https://www.oracle.com/java/technologies/"
    exit 1
fi

echo "[✓] Maven and Java found"
echo ""

# Parse arguments
TARGET="${1:-run}"

case "$TARGET" in
    clean)
        echo "Running: mvn clean"
        echo ""
        mvn clean
        echo ""
        echo "[✓] Clean completed"
        ;;
    package)
        echo "Running: mvn clean package"
        echo ""
        mvn clean package
        echo ""
        echo "[✓] Build completed"
        echo ""
        echo "JAR file: target/chunking-vector-store-1.0.0-shaded.jar"
        echo "Run with: java -jar target/chunking-vector-store-1.0.0-shaded.jar"
        ;;
    test)
        echo "Running: mvn test"
        echo ""
        mvn test
        echo ""
        echo "[✓] Tests completed"
        ;;
    help)
        echo "Usage: $0 [COMMAND]"
        echo ""
        echo "Commands:"
        echo "  (default)    - Compile and run the application"
        echo "  clean        - Clean build artifacts"
        echo "  package      - Build JAR package"
        echo "  test         - Run unit tests"
        echo "  help         - Show this help message"
        echo ""
        echo "Environment Variables:"
        echo "  OPENAI_API_KEY       - Required for vector search operations"
        echo "  MAVEN_OPTS           - JVM options for Maven (e.g., -Xmx2g)"
        echo ""
        echo "Examples:"
        echo "  $0"
        echo "  $0 package"
        echo "  export OPENAI_API_KEY=sk-proj-... && $0"
        echo ""
        ;;
    *)
        # Check for API key
        if [ -z "$OPENAI_API_KEY" ]; then
            echo "WARNING: OPENAI_API_KEY environment variable is not set"
            echo ""
            echo "To set it, run:"
            echo "  export OPENAI_API_KEY=sk-proj-your-key"
            echo ""
            echo "Continuing anyway - search will be skipped..."
            echo ""
        else
            echo "[✓] OPENAI_API_KEY is set"
            echo ""
        fi
        
        echo "Running: mvn compile exec:java@ChunkingVectorStoreDemo"
        echo ""
        mvn compile exec:java@ChunkingVectorStoreDemo
        ;;
esac

echo ""
echo "============================================================================"
echo "Build script completed"
echo "============================================================================"
echo ""
