---
description: Complete Ollama Setup and Testing Workflow
---

# Complete Ollama Setup Workflow for RAG ChatBot

This workflow guides you through setting up Ollama (local AI) for your RAG ChatBot.

## Current Status ✅
- ✅ Code is already configured for Ollama
- ✅ `pom.xml` has `spring-ai-ollama-spring-boot-starter`
- ✅ `application.properties` configured for Ollama
- ✅ ChromaDB docker-compose ready
- ⚠️ Ollama not installed yet

---

## Step 1: Install Ollama on Windows

1. **Download Ollama:**
   - Open browser: https://ollama.ai/download
   - Click "Download for Windows"
   - Run `OllamaSetup.exe`
   - Follow installation wizard

2. **Verify Installation:**
   ```powershell
   ollama --version
   ```
   Expected: Version information (e.g., `ollama version is 0.x.x`)

---

## Step 2: Verify Ollama Service is Running

Ollama should start automatically after installation.

**Check if running:**
```powershell
curl http://localhost:11434/api/tags
```

**Expected Response:** JSON with models list (may be empty initially)

**If not running:**
- Check system tray for Ollama icon
- Search "Ollama" in Start menu and launch it
- Or restart your computer

---

## Step 3: Download Required AI Models

**Download Chat Model (choose ONE based on your RAM):**

```powershell
# Option A: Lightweight (1.3GB) - For 4GB RAM systems
ollama pull llama3.2:1b

# Option B: Balanced (2GB) - RECOMMENDED for 6GB+ RAM
ollama pull llama3.2:3b

# Option C: High Quality (4.7GB) - For 8GB+ RAM
ollama pull llama3.1:8b
```

**Download Embedding Model (REQUIRED):**
```powershell
ollama pull nomic-embed-text
```

**Verify Downloads:**
```powershell
ollama list
```

Expected output:
```
NAME                    ID              SIZE      MODIFIED
llama3.2:3b            a80c4f17acd5    2.0 GB    2 minutes ago
nomic-embed-text       0a109f422b47    274 MB    1 minute ago
```

---

## Step 4: Update application.properties (if needed)

Your `application.properties` is already configured for `llama3.2:3b`.

**If you downloaded a different model**, update line 3:

```properties
# Change this line to match your downloaded model
spring.ai.ollama.chat.options.model=llama3.2:3b
```

For example, if you downloaded `llama3.2:1b`:
```properties
spring.ai.ollama.chat.options.model=llama3.2:1b
```

---

## Step 5: Start ChromaDB

**Start ChromaDB using Docker:**
```powershell
docker-compose up -d
```

**Verify ChromaDB is running:**
```powershell
docker ps
curl http://localhost:8000/api/v1/heartbeat
```

Expected: `{"nanosecond heartbeat":...}`

---

## Step 6: Clean and Build the Project

// turbo
```powershell
.\mvnw.cmd clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

**If build fails:**
```powershell
# Force update dependencies
.\mvnw.cmd clean install -U
```

---

## Step 7: Run the Application

```powershell
.\mvnw.cmd spring-boot:run
```

**Success Indicators in Logs:**
- ✅ `Started RagApplication in X.XXX seconds`
- ✅ `Tomcat started on port(s): 8080`
- ✅ No connection errors to Ollama or ChromaDB

**Common Errors and Solutions:**

**Error: "Connection refused to localhost:11434"**
- Solution: Start Ollama service (see Step 2)

**Error: "Model not found: llama3.2:3b"**
- Solution: Download the model (see Step 3) or update application.properties

**Error: "Connection refused to localhost:8000"**
- Solution: Start ChromaDB (see Step 5)

---

## Step 8: Test the Application

### Test 1: Create a test document

Create `test.txt` in the project root:
```
Artificial Intelligence (AI) is transforming modern technology.
Machine learning is a subset of AI that enables systems to learn from data.
Deep learning uses neural networks to process complex patterns.
```

### Test 2: Upload the document

```powershell
$form = @{
    file = Get-Item -Path "test.txt"
}
Invoke-RestMethod -Uri "http://localhost:8080/api/rag/upload" -Method Post -Form $form
```

**Expected Response:**
```
Document uploaded and processed successfully.
```

### Test 3: Ask a question

```powershell
$question = "What is machine learning?"
$encodedQuestion = [Uri]::EscapeDataString($question)
Invoke-RestMethod -Uri "http://localhost:8080/api/rag/ask?question=$encodedQuestion"
```

**Expected Response:**
```json
{
  "answer": "Based on the provided context, machine learning is a subset of AI that enables systems to learn from data."
}
```

### Test 4: Browser Test

Open browser and visit:
```
http://localhost:8080/api/rag/ask?question=What%20is%20AI?
```

You should see a JSON response with an answer about AI.

---

## Verification Checklist

After completing all steps, verify:

- [ ] Ollama is installed (`ollama --version` works)
- [ ] Ollama service is running (`curl http://localhost:11434/api/tags` works)
- [ ] Models are downloaded (`ollama list` shows models)
- [ ] ChromaDB is running (`docker ps` shows chromadb container)
- [ ] Application starts without errors
- [ ] Can upload documents successfully
- [ ] Can ask questions and get relevant answers

---

## Troubleshooting

### Slow Response Times

**Cause:** Model too large for your system

**Solutions:**
1. Switch to smaller model:
   ```powershell
   ollama pull llama3.2:1b
   ```

2. Update `application.properties`:
   ```properties
   spring.ai.ollama.chat.options.model=llama3.2:1b
   ```

3. Restart application

### Out of Memory Errors

**Solutions:**
- Close other applications
- Use smaller model (`llama3.2:1b`)
- Increase JVM memory:
  ```powershell
  $env:MAVEN_OPTS="-Xmx2g"
  .\mvnw.cmd spring-boot:run
  ```

### Application Won't Start

**Solution:**
```powershell
# Delete target folder and rebuild
Remove-Item -Recurse -Force target
.\mvnw.cmd clean install -U
```

---

## Performance Tips

### Adjust Temperature (Creativity vs Accuracy)

Edit `application.properties`:

```properties
# More focused/deterministic (0.0-0.5)
spring.ai.ollama.chat.options.temperature=0.3

# Balanced (0.5-0.8) - DEFAULT
spring.ai.ollama.chat.options.temperature=0.7

# More creative (0.8-1.0)
spring.ai.ollama.chat.options.temperature=0.9
```

### Try Different Models

```powershell
# Download alternative models
ollama pull mistral:7b        # Good general model
ollama pull codellama:7b      # Better for code
ollama pull phi3:mini         # Smallest, fastest

# Update application.properties
spring.ai.ollama.chat.options.model=mistral:7b
```

---

## Success! 🎉

You now have a **100% FREE, local RAG ChatBot** with:
- ✅ No API costs
- ✅ Unlimited document uploads
- ✅ Unlimited questions
- ✅ Complete privacy (everything runs locally)
- ✅ Offline capability (after model download)

**Next Steps:**
- Upload more documents (PDFs, Word docs, etc.)
- Experiment with different models
- Adjust temperature for your use case
- Build a frontend UI for easier interaction
