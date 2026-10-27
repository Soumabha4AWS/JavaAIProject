# Installation & Deployment Guide

## ✅ Build Status: SUCCESS

The LlamaIndex Java project has been successfully built and is ready for deployment.

---

## 📦 What You Have

After running `mvn clean package`, you get:

```
target/
├── llama-index-java-jar-with-dependencies.jar  (Standalone fat JAR ~8MB)
├── llama-index-java-1.0.0.jar                  (Thin JAR without dependencies)
├── classes/                                     (Compiled .class files)
└── lib/                                         (All dependencies)
```

**The fat JAR is what you need!** It contains everything required to run the application.

---

## 🚀 Installation Steps

### Step 1: Verify Prerequisites
```bash
# Check Java (11 or higher required)
java -version
# Should output: java version "11..." or higher

# Check Maven (optional, only if rebuilding)
mvn -version
# Should output: Apache Maven 3.6... or higher
```

### Step 2: Download the Standalone JAR
```bash
# The pre-built JAR is located at:
# target/llama-index-java-jar-with-dependencies.jar

# Copy to your desired location
cp target/llama-index-java-jar-with-dependencies.jar ~/myapps/
cd ~/myapps/
```

### Step 3: Prepare OpenAI API Credentials
```bash
# Get your API key from https://platform.openai.com/api-keys

# Store in environment variable
export OPENAI_API_KEY=sk-your-actual-key-here

# Verify it's set
echo $OPENAI_API_KEY  # Should show your key (not "sk-your-actual-key-here")
```

### Step 4: Prepare Data
```bash
# Ensure synthetic_tickets.json is accessible
# Place in same directory as JAR or parent directory:

ls -la synthetic_tickets.json
# Should show the file exists

# Or copy it:
cp /path/to/synthetic_tickets.json .
```

### Step 5: Run the Application
```bash
# Simple way (uses environment variable)
java -jar llama-index-java-jar-with-dependencies.jar

# Or specify API key directly
java -Dopenai.api.key=sk-... -jar llama-index-java-jar-with-dependencies.jar

# With memory settings for large datasets
java -Xmx2g -Xms512m \
     -Dopenai.api.key=sk-... \
     -jar llama-index-java-jar-with-dependencies.jar

# With logging enabled
java -Dlog4j.configuration=file:log4j.properties \
     -Dopenai.api.key=sk-... \
     -jar llama-index-java-jar-with-dependencies.jar
```

---

## 🐧 Platform-Specific Instructions

### Linux / macOS
```bash
# 1. Download or clone the project
git clone <repo> && cd llama-index-java

# 2. Build (if from source)
mvn clean package -DskipTests

# 3. Set API key
export OPENAI_API_KEY=sk-your-key

# 4. Run
java -jar target/llama-index-java-jar-with-dependencies.jar
```

### Windows (PowerShell)
```powershell
# 1. Navigate to project
cd C:\path\to\llama-index-java

# 2. Build (if from source)
mvn clean package -DskipTests

# 3. Set API key
$env:OPENAI_API_KEY = 'sk-your-key'

# 4. Run
java -jar target\llama-index-java-jar-with-dependencies.jar

# Or
java -Dopenai.api.key='sk-your-key' -jar target\llama-index-java-jar-with-dependencies.jar
```

### Windows (CMD)
```cmd
# 1. Navigate to project
cd C:\path\to\llama-index-java

# 2. Build (if from source)
mvn clean package -DskipTests

# 3. Set API key
set OPENAI_API_KEY=sk-your-key

# 4. Run
java -jar target\llama-index-java-jar-with-dependencies.jar
```

### Docker
```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

# Copy JAR
COPY target/llama-index-java-jar-with-dependencies.jar app.jar

# Copy data
COPY synthetic_tickets.json .

# Expose API key as environment variable
ENV OPENAI_API_KEY=""

# Run
CMD ["java", "-Xmx2g", "-jar", "app.jar"]
```

**Build and run Docker image:**
```bash
docker build -t llama-index-java .
docker run -e OPENAI_API_KEY=sk-... llama-index-java
```

---

## 🔧 Java Memory Tuning

### For Different Scenarios

**Small deployment (up to 1,000 documents):**
```bash
java -Xmx512m -Dopenai.api.key=sk-... -jar llama-index-java-jar-with-dependencies.jar
```

**Medium deployment (1,000-10,000 documents):**
```bash
java -Xmx2g -Xms1g -Dopenai.api.key=sk-... -jar llama-index-java-jar-with-dependencies.jar
```

**Large deployment (10,000+ documents):**
```bash
java -Xmx4g -Xms2g -XX:+UseG1GC -Dopenai.api.key=sk-... -jar llama-index-java-jar-with-dependencies.jar
```

**High-performance (parallel processing):**
```bash
java -Xmx8g -Xms4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Dopenai.api.key=sk-... \
     -jar llama-index-java-jar-with-dependencies.jar
```

### Memory Settings Explained
- **-Xmx2g** - Maximum heap size (2 gigabytes)
- **-Xms512m** - Initial heap size (512 megabytes)
- **-XX:+UseG1GC** - Use G1 garbage collector (better for large heaps)

---

## 📊 System Requirements

### Minimum
- **OS:** Linux, macOS, or Windows
- **Java:** JDK 11 or higher
- **RAM:** 512 MB
- **Disk:** 50 MB for JAR
- **Network:** Internet connection for OpenAI API

### Recommended
- **Java:** JDK 17 or higher
- **RAM:** 2-4 GB
- **SSD:** For faster startup

### For Large Deployments
- **RAM:** 4-8 GB or more
- **CPU:** Multi-core processor (4+ cores)
- **Network:** High-speed, low-latency connection

---

## 🔍 Verification Checklist

After installation, verify everything works:

```bash
# ✅ Check Java
java -version

# ✅ Set API Key
export OPENAI_API_KEY=sk-your-key

# ✅ Check data file exists
ls -la synthetic_tickets.json

# ✅ Run the JAR
java -jar llama-index-java-jar-with-dependencies.jar

# ✅ Verify output appears (first index building starts)
# Should see: "Loading Support Tickets", "Building Vector Index", etc.

# ✅ Application completes successfully
# Should see: "✓ ALL DEMONSTRATIONS COMPLETED SUCCESSFULLY"
```

---

## 🐛 Troubleshooting Installation

### Issue: "Command not found: java"
```
Solution: Java not installed or not in PATH
- Install Java 11+ from https://adoptopenjdk.net/
- Add Java bin directory to PATH
- Verify: java -version
```

### Issue: "API key not found"
```
Solution: OpenAI API key not configured
- Check environment variable: echo $OPENAI_API_KEY
- Set it: export OPENAI_API_KEY=sk-...
- Or use: java -Dopenai.api.key=sk-... -jar ...
```

### Issue: "synthetic_tickets.json not found"
```
Solution: Data file missing
- Check file location: ls synthetic_tickets.json
- Copy from project: cp ../synthetic_tickets.json .
- Update path in code if needed
```

### Issue: "OutOfMemoryError"
```
Solution: Not enough memory allocated
- Increase heap: java -Xmx4g -jar ...
- Check available memory: free -h (Linux) or Task Manager (Windows)
- Use larger machine or reduce dataset size
```

### Issue: "Connection refused" / "API error"
```
Solution: OpenAI API connection issue
- Verify internet connection
- Check API key validity
- Verify API rate limits not exceeded
- Check OpenAI status at status.openai.com
```

---

## 🚀 Production Deployment

### Best Practices

1. **Use Environment Variables**
   ```bash
   # Never hardcode secrets!
   export OPENAI_API_KEY=sk-...
   java -jar app.jar
   ```

2. **Run as Service**
   ```bash
   # Create systemd service for Linux
   # Create Windows service for Windows
   # Use Docker for containerized deployment
   ```

3. **Monitor Logs**
   ```bash
   java -Dlog4j.configuration=file:log4j.properties \
        -Dopenai.api.key=sk-... \
        -jar app.jar > logs/output.log 2>&1
   ```

4. **Set Resource Limits**
   ```bash
   # Limit CPU and memory
   java -XX:ActiveProcessorCount=4 \
        -Xmx2g \
        -Dopenai.api.key=sk-... \
        -jar app.jar
   ```

5. **API Key Rotation**
   - Rotate OpenAI API keys regularly
   - Update in environment variables
   - No application restart needed

---

## 📈 Scaling Considerations

### For Small Teams (1-10 users)
- Single JAR deployment
- Shared OpenAI API key
- Basic monitoring
- ~30-60 seconds per query

### For Medium Scale (10-100 concurrent users)
- Load balancer + multiple instances
- API key management system
- Proper monitoring and alerting
- Query caching
- Result persistence

### For Large Scale (100+ concurrent users)
- Containerized with Kubernetes
- Vector database (Weaviate, Milvus, Pinecone)
- API gateway with rate limiting
- Dedicated ML infrastructure
- Query result caching layer

---

## 🔐 Security Checklist

Before production deployment:

- [ ] API key stored in environment variables (never in code)
- [ ] HTTPS only for API communication
- [ ] Network firewall configured
- [ ] Access logs enabled
- [ ] API key rotation policy
- [ ] Data encryption at rest (if needed)
- [ ] Regular security updates
- [ ] Backup strategy
- [ ] Incident response plan

---

## 📞 Support & Help

### Getting Help
1. Check QUICKSTART.md for common issues
2. Review README.md architecture section
3. Check application logs for error messages
4. Verify all prerequisites are met

### Reporting Issues
If problems persist:
1. Gather logs: `java -jar app.jar 2>&1 | tee debug.log`
2. Note Java version: `java -version`
3. Note OS and hardware
4. Include exact error message
5. Include steps to reproduce

### Documentation
- **README.md** - Comprehensive guide
- **QUICKSTART.md** - 5-minute setup
- **PROJECT_SUMMARY.md** - Project overview
- **This file** - Installation guide

---

## ✨ Next Steps

1. ✅ Install Java 11+
2. ✅ Get OpenAI API key
3. ✅ Download/build JAR
4. ✅ Set environment variables
5. ✅ Run application
6. ✅ Review output
7. 🚀 Customize for your use case

---

**You're ready to go!** 🎉

For any questions, refer to the comprehensive documentation included in the project.
