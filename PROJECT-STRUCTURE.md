# Project Structure

This document describes the organized file structure of the RAG ChatBot project.

## 📁 Directory Overview

```
rag/
├── 📄 README.md                    # Main documentation (START HERE!)
├── 📄 pom.xml                      # Maven configuration
├── 📄 docker-compose.yml           # Docker configuration for ChromaDB
├── 📄 .gitignore                   # Git ignore rules
│
├── 📂 src/                         # Source code
│   ├── 📂 main/
│   │   ├── 📂 java/
│   │   │   └── 📂 com/chatbox/rag/
│   │   │       ├── 📄 RagApplication.java          # Main application class
│   │   │       ├── 📂 config/
│   │   │       │   └── 📄 VectorStoreConfig.java   # Spring configuration
│   │   │       ├── 📂 controller/
│   │   │       │   └── 📄 RagController.java       # REST API endpoints
│   │   │       └── 📂 service/
│   │   │           ├── 📄 ChatService.java         # Chat service interface
│   │   │           ├── 📄 DocumentService.java     # Document processing
│   │   │           ├── 📄 RagService.java          # RAG service interface
│   │   │           └── 📄 RagServiceImpl.java      # RAG implementation
│   │   └── 📂 resources/
│   │       └── 📄 application.properties           # Application configuration
│   └── 📂 test/                    # Test files
│
├── 📂 docs/                        # Documentation
│   ├── 📄 API.md                   # API documentation
│   └── 📄 TROUBLESHOOTING.md       # Troubleshooting guide
│
├── 📂 scripts/                     # Utility scripts
│   ├── 📄 start.ps1                # Quick start script (Windows)
│   └── 📄 setup-ollama.ps1         # Ollama setup script
│
├── 📂 sample-data/                 # Sample documents for testing
│   ├── 📄 Premkumar Resume.pdf     # Sample resume
│   └── 📄 test.txt                 # Sample text file
│
├── 📂 target/                      # Compiled files (generated)
│   └── 📄 rag-0.0.1-SNAPSHOT.jar   # Executable JAR
│
├── 📂 chroma/                      # ChromaDB data (gitignored)
├── 📂 chroma_data/                 # ChromaDB persistent data (gitignored)
│
└── 📂 .mvn/                        # Maven wrapper files
    └── 📂 wrapper/
```

## 📋 File Descriptions

### Root Level Files

| File | Purpose |
|------|---------|
| `README.md` | **Main documentation** - Start here for setup and usage instructions |
| `pom.xml` | Maven project configuration and dependencies |
| `docker-compose.yml` | Docker configuration for running ChromaDB |
| `.gitignore` | Specifies files to ignore in version control |
| `mvnw`, `mvnw.cmd` | Maven wrapper scripts for building without Maven installed |

### Source Code (`src/`)

#### Main Application

| File | Purpose |
|------|---------|
| `RagApplication.java` | Main Spring Boot application entry point |
| `VectorStoreConfig.java` | Spring configuration for ChatClient |
| `RagController.java` | REST API endpoints (`/upload`, `/ask`) |

#### Services

| File | Purpose |
|------|---------|
| `ChatService.java` | Interface for chat functionality |
| `DocumentService.java` | Document upload and processing |
| `RagService.java` | Interface for RAG operations |
| `RagServiceImpl.java` | Implementation of RAG logic |

#### Configuration

| File | Purpose |
|------|---------|
| `application.properties` | Application settings (Ollama, ChromaDB, logging) |

### Documentation (`docs/`)

| File | Purpose |
|------|---------|
| `API.md` | Complete API documentation with examples |
| `TROUBLESHOOTING.md` | Common issues and solutions |

### Scripts (`scripts/`)

| File | Purpose |
|------|---------|
| `start.ps1` | **Quick start script** - Automatically starts all services |
| `setup-ollama.ps1` | Ollama installation and setup helper |

### Sample Data (`sample-data/`)

| File | Purpose |
|------|---------|
| `Premkumar Resume.pdf` | Sample PDF for testing document upload |
| `test.txt` | Sample text file for quick testing |

### Build Output (`target/`)

| File | Purpose |
|------|---------|
| `rag-0.0.1-SNAPSHOT.jar` | Executable JAR file (run with `java -jar`) |
| `classes/` | Compiled Java classes |

## 🎯 Quick Navigation

### For First-Time Setup
1. **Start Here:** `README.md`
2. **If Issues:** `docs/TROUBLESHOOTING.md`

### For Development
1. **Main Code:** `src/main/java/com/chatbox/rag/`
2. **Configuration:** `src/main/resources/application.properties`
3. **Tests:** `src/test/java/`

### For Running
1. **Quick Start:** Run `scripts/start.ps1`
2. **Manual Start:** See `README.md` → "Running the Application"

### For API Usage
1. **API Docs:** `docs/API.md`
2. **Sample Data:** `sample-data/`

## 📦 Generated/Ignored Directories

These directories are automatically generated and should not be committed to version control:

- `target/` - Maven build output
- `chroma/` - ChromaDB runtime data
- `chroma_data/` - ChromaDB persistent storage
- `.mvn/wrapper/` - Maven wrapper JAR
- `.settings/` - IDE settings
- `.vscode/` - VS Code settings
- `.idea/` - IntelliJ IDEA settings

## 🔧 Configuration Files

### Maven Configuration (`pom.xml`)

Defines:
- Project metadata (groupId, artifactId, version)
- Dependencies (Spring Boot, Spring AI, ChromaDB, Ollama)
- Build plugins (Spring Boot Maven Plugin, JaCoCo)
- Repositories (Spring Milestones)

### Application Configuration (`application.properties`)

Configures:
- Ollama connection (base URL, models)
- ChromaDB connection (host, port, collection)
- Logging levels

### Docker Configuration (`docker-compose.yml`)

Defines:
- ChromaDB service
- Port mapping (8000:8000)
- Persistent volume for data

## 📝 Important Notes

### What to Edit

**Frequently Modified:**
- `src/main/resources/application.properties` - Change ports, models, etc.
- `src/main/java/com/chatbox/rag/service/` - Add new features
- `src/main/java/com/chatbox/rag/controller/` - Add new endpoints

**Rarely Modified:**
- `pom.xml` - Only when adding new dependencies
- `RagApplication.java` - Only for major architectural changes

### What NOT to Edit

- Files in `target/` - Auto-generated
- Files in `.mvn/` - Maven wrapper
- `.classpath`, `.project`, `.factorypath` - IDE-generated

### What to Backup

**Important:**
- `src/` - Your source code
- `pom.xml` - Project configuration
- `application.properties` - Settings
- `sample-data/` - Your test documents

**Optional:**
- `chroma_data/` - Vector database (can be regenerated)
- `docs/` - Documentation

## 🚀 Typical Workflows

### Development Workflow

1. Edit code in `src/main/java/`
2. Update `application.properties` if needed
3. Build: `mvn clean package -DskipTests`
4. Run: `java -jar target/rag-0.0.1-SNAPSHOT.jar`
5. Test with Postman or cURL

### Testing Workflow

1. Start services: Run `scripts/start.ps1`
2. Upload document: Use sample from `sample-data/`
3. Ask questions: See examples in `docs/API.md`

### Deployment Workflow

1. Build: `mvn clean package`
2. Copy `target/rag-0.0.1-SNAPSHOT.jar` to server
3. Ensure Ollama and ChromaDB are running
4. Run: `java -jar rag-0.0.1-SNAPSHOT.jar`

## 📊 File Size Reference

| Component | Approximate Size |
|-----------|------------------|
| Source code | ~50 KB |
| Compiled JAR | ~80 MB (includes all dependencies) |
| Ollama models | ~2.3 GB (llama3.2:3b + nomic-embed-text) |
| ChromaDB data | Varies (depends on uploaded documents) |

## 🔍 Finding Things

### "Where is the API endpoint code?"
→ `src/main/java/com/chatbox/rag/controller/RagController.java`

### "Where do I configure the Ollama model?"
→ `src/main/resources/application.properties`

### "Where are the sample documents?"
→ `sample-data/`

### "How do I start the application?"
→ Run `scripts/start.ps1` or see `README.md`

### "Where is the API documentation?"
→ `docs/API.md`

### "I'm getting an error, where do I look?"
→ `docs/TROUBLESHOOTING.md`

## 📚 Documentation Index

1. **README.md** - Main documentation, setup guide
2. **docs/API.md** - API reference, request/response examples
3. **docs/TROUBLESHOOTING.md** - Common issues and solutions
4. **PROJECT-STRUCTURE.md** - This file

## 🎉 Summary

This project follows standard Java/Spring Boot conventions with additional organization for:
- 📂 Documentation in `docs/`
- 📂 Utility scripts in `scripts/`
- 📂 Sample data in `sample-data/`

All source code is in `src/`, configuration in `application.properties`, and the executable JAR in `target/`.

For any questions, start with `README.md` and refer to `docs/` for detailed information.

---

**Last Updated:** 2026-01-30
