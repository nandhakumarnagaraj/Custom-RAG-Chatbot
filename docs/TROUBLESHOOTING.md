# Troubleshooting Guide

Common issues and their solutions for the RAG ChatBot application.

---

## Table of Contents

- [Installation Issues](#installation-issues)
- [Build Issues](#build-issues)
- [Runtime Issues](#runtime-issues)
- [ChromaDB Issues](#chromadb-issues)
- [Ollama Issues](#ollama-issues)
- [Performance Issues](#performance-issues)
- [Network Issues](#network-issues)

---

## Installation Issues

### Java Not Found

**Error:**
```
'java' is not recognized as an internal or external command
```

**Solution:**

1. **Verify Java is installed:**
   ```bash
   java -version
   ```

2. **If not installed, download and install:**
   - [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   - [Adoptium OpenJDK](https://adoptium.net/)

3. **Set JAVA_HOME (Windows):**
   ```powershell
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

4. **Set JAVA_HOME (Linux/Mac):**
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   export PATH=$JAVA_HOME/bin:$PATH
   ```

5. **Restart terminal and verify:**
   ```bash
   java -version
   ```

---

### Maven Not Found

**Error:**
```
'mvn' is not recognized as an internal or external command
```

**Solution:**

1. **Install Maven:**
   - Download from [Apache Maven](https://maven.apache.org/download.cgi)
   - Extract to `C:\Program Files\Apache\maven`

2. **Add to PATH (Windows):**
   ```powershell
   setx PATH "%PATH%;C:\Program Files\Apache\maven\bin"
   ```

3. **Add to PATH (Linux/Mac):**
   ```bash
   export PATH=/opt/maven/bin:$PATH
   ```

4. **Verify:**
   ```bash
   mvn -version
   ```

---

### Python/Pip Not Found

**Error:**
```
'python' is not recognized as an internal or external command
```

**Solution:**

1. **Download Python:**
   - [Python.org](https://www.python.org/downloads/)

2. **During installation:**
   - ✅ Check "Add Python to PATH"

3. **Verify:**
   ```bash
   python --version
   pip --version
   ```

4. **If pip is missing:**
   ```bash
   python -m ensurepip --upgrade
   ```

---

## Build Issues

### Build Fails with Test Errors

**Error:**
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:testCompile
[ERROR] Compilation failure: Compilation failure:
[ERROR] cannot find symbol: class CallResponseSpec
```

**Solution:**

Skip tests during build:
```bash
mvn clean package -DskipTests
```

Or skip test compilation:
```bash
mvn clean package -Dmaven.test.skip=true
```

---

### Dependency Download Fails

**Error:**
```
[ERROR] Failed to execute goal on project rag: Could not resolve dependencies
```

**Solution:**

1. **Check internet connection**

2. **Clear Maven cache:**
   ```bash
   # Windows
   rmdir /s /q %USERPROFILE%\.m2\repository

   # Linux/Mac
   rm -rf ~/.m2/repository
   ```

3. **Rebuild:**
   ```bash
   mvn clean install -U
   ```

4. **Check Maven settings:**
   - Verify proxy settings in `~/.m2/settings.xml`

---

### Out of Memory During Build

**Error:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**

Increase Maven memory:
```bash
# Windows
set MAVEN_OPTS=-Xmx2048m -XX:MaxPermSize=512m

# Linux/Mac
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
```

---

## Runtime Issues

### Port 8080 Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**

**Option 1: Kill the process using port 8080**

Windows:
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

Linux/Mac:
```bash
lsof -ti:8080 | xargs kill -9
```

**Option 2: Change the port**

Edit `src/main/resources/application.properties`:
```properties
server.port=8081
```

---

### Application Fails to Start

**Error:**
```
APPLICATION FAILED TO START
```

**Common Causes & Solutions:**

1. **ChromaDB not running:**
   ```bash
   chroma run --host localhost --port 8000
   ```

2. **Ollama models not installed:**
   ```bash
   ollama pull llama3.2:3b
   ollama pull nomic-embed-text
   ```

3. **Check logs for specific error:**
   - Look in console output for detailed error message
   - Check `logs/` directory if logging is configured

---

### Bean Creation Error

**Error:**
```
Error creating bean with name 'vectorStore'
```

**Solution:**

This usually means ChromaDB is not accessible.

1. **Start ChromaDB:**
   ```bash
   chroma run --host localhost --port 8000
   ```

2. **Verify ChromaDB is running:**
   ```bash
   curl http://localhost:8000/api/v1/heartbeat
   ```

3. **Check configuration in `application.properties`:**
   ```properties
   spring.ai.vectorstore.chroma.host=localhost
   spring.ai.vectorstore.chroma.port=8000
   ```

---

## ChromaDB Issues

### ChromaDB Won't Start

**Error:**
```
chroma: command not found
```

**Solution:**

1. **Install ChromaDB:**
   ```bash
   pip install "chromadb>=0.4.0,<1.0.0"
   ```

2. **Verify installation:**
   ```bash
   chroma --version
   ```

3. **If still not found, use full path:**
   ```bash
   python -m chromadb.cli run --host localhost --port 8000
   ```

---

### ChromaDB API Version Mismatch

**Error:**
```
{"error":"Unimplemented","message":"The v1 API is deprecated. Please use /v2 apis"}
```

**Solution:**

Downgrade ChromaDB to a version that supports v1 API:

```bash
# Uninstall current version
pip uninstall chromadb -y

# Install compatible version
pip install "chromadb>=0.4.0,<1.0.0"

# Restart ChromaDB
chroma run --host localhost --port 8000
```

---

### NumPy Compatibility Error

**Error:**
```
AttributeError: module 'numpy' has no attribute 'float'
```

**Solution:**

Downgrade NumPy:
```bash
pip uninstall numpy -y
pip install "numpy<2.0"
```

---

### ChromaDB Port Already in Use

**Error:**
```
Address already in use
```

**Solution:**

**Option 1: Kill process on port 8000**

Windows:
```powershell
netstat -ano | findstr :8000
taskkill /PID <PID> /F
```

Linux/Mac:
```bash
lsof -ti:8000 | xargs kill -9
```

**Option 2: Use different port**

```bash
chroma run --host localhost --port 8001
```

Then update `application.properties`:
```properties
spring.ai.vectorstore.chroma.port=8001
```

---

## Ollama Issues

### Ollama Not Running

**Error:**
```
Connection refused: connect to localhost:11434
```

**Solution:**

1. **Start Ollama:**

   Windows: Ollama should start automatically, or run from Start Menu

   Linux:
   ```bash
   ollama serve
   ```

   Mac:
   ```bash
   ollama serve
   ```

2. **Verify Ollama is running:**
   ```bash
   curl http://localhost:11434/api/tags
   ```

---

### Model Not Found

**Error:**
```
[404] - {"error":"model \"llama3.2:3b\" not found, try pulling it first"}
```

**Solution:**

Pull the required models:
```bash
# Pull chat model (2GB)
ollama pull llama3.2:3b

# Pull embedding model (274MB)
ollama pull nomic-embed-text

# Verify models are installed
ollama list
```

**Expected output:**
```
NAME                       ID              SIZE      MODIFIED
llama3.2:3b                a80c4f17acd5    2.0 GB    ...
nomic-embed-text:latest    0a109f422b47    274 MB    ...
```

---

### Ollama Model Loading Slow

**Issue:** First request takes 10-20 seconds

**Solution:**

This is normal behavior. The model needs to load into memory on first use.

**To keep model loaded:**
```bash
# Keep model in memory
ollama run llama3.2:3b
# Press Ctrl+D to exit but keep model loaded
```

---

### Ollama Out of Memory

**Error:**
```
failed to load model: insufficient memory
```

**Solution:**

1. **Close other applications**

2. **Use a smaller model:**
   ```bash
   ollama pull llama3.2:1b
   ```

   Update `application.properties`:
   ```properties
   spring.ai.ollama.chat.options.model=llama3.2:1b
   ```

3. **Increase system swap/virtual memory**

---

## Performance Issues

### Slow Response Times

**Issue:** Questions take 30+ seconds to answer

**Possible Causes & Solutions:**

1. **First request is always slower:**
   - Models need to load (10-20 seconds is normal)
   - Subsequent requests should be faster (2-5 seconds)

2. **Large documents:**
   - Break documents into smaller chunks
   - Reduce `topK` parameter in service

3. **Insufficient RAM:**
   - Close other applications
   - Minimum 8GB RAM recommended
   - 16GB RAM for optimal performance

4. **CPU bottleneck:**
   - Use GPU acceleration if available
   - Consider using smaller models

---

### High Memory Usage

**Issue:** Application uses too much RAM

**Solutions:**

1. **Use smaller AI models:**
   ```bash
   ollama pull llama3.2:1b
   ```

2. **Limit JVM memory:**
   ```bash
   java -Xmx2g -jar target/rag-0.0.1-SNAPSHOT.jar
   ```

3. **Reduce document chunk size** in DocumentService

---

### Document Upload Fails

**Error:**
```
Failed to upload and process document
```

**Solutions:**

1. **Check file format:**
   - Supported: PDF, TXT
   - Try with a simple .txt file first

2. **Check file size:**
   - Very large files (>50MB) may timeout
   - Break into smaller documents

3. **Check logs for specific error:**
   - Look for stack trace in console

4. **Verify all services are running:**
   ```bash
   # Check ChromaDB
   curl http://localhost:8000/api/v1/heartbeat
   
   # Check Ollama
   curl http://localhost:11434/api/tags
   ```

---

## Network Issues

### Cannot Access API from Browser

**Issue:** API works in Postman but not in browser

**Solution:**

Add CORS configuration to `VectorStoreConfig.java`:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

---

### Connection Timeout

**Error:**
```
Read timed out
```

**Solution:**

Increase timeout in `application.properties`:
```properties
spring.ai.ollama.timeout=120s
```

---

## Spring Tool Suite (STS) Issues

### Project Not Recognized as Spring Boot

**Solution:**

1. Right-click project → `Configure` → `Convert to Spring Boot Project`

2. Or update Maven:
   - Right-click project → `Maven` → `Update Project`
   - Check `Force Update of Snapshots/Releases`

---

### Cannot Run as Spring Boot App

**Solution:**

1. **Verify main class exists:**
   - Check `RagApplication.java` has `@SpringBootApplication`

2. **Clean and rebuild:**
   - `Project` → `Clean...`
   - `Project` → `Build Project`

3. **Update Maven:**
   - Right-click → `Maven` → `Update Project`

---

## Logging and Debugging

### Enable Debug Logging

Edit `application.properties`:
```properties
# Enable debug logging
logging.level.root=DEBUG
logging.level.com.chatbox.rag=DEBUG
logging.level.org.springframework.ai=DEBUG

# Log to file
logging.file.name=logs/application.log
```

### View Detailed Error Messages

Run with debug flag:
```bash
java -jar target/rag-0.0.1-SNAPSHOT.jar --debug
```

---

## Getting Help

If you're still experiencing issues:

1. **Check the logs** for detailed error messages
2. **Search existing issues** in the repository
3. **Create a new issue** with:
   - Error message
   - Steps to reproduce
   - System information (OS, Java version, etc.)
   - Relevant logs

---

## Common Error Messages Reference

| Error Message | Likely Cause | Solution |
|---------------|--------------|----------|
| "Port already in use" | Another app using the port | Kill process or change port |
| "Connection refused" | Service not running | Start ChromaDB/Ollama |
| "Model not found" | Ollama model not installed | Run `ollama pull` |
| "Cannot find symbol" | Compilation error | Clean and rebuild |
| "Out of memory" | Insufficient RAM | Increase memory or use smaller model |
| "Bean creation failed" | Configuration issue | Check application.properties |
| "File upload failed" | Service issue | Check all services are running |

---

**Last Updated:** 2026-01-30

For additional help, refer to:
- [README.md](../README.md) - Main documentation
- [API.md](API.md) - API documentation
- [SETUP.md](SETUP.md) - Detailed setup guide
