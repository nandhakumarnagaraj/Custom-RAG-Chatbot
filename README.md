# 🤖 RAG ChatBot Backend

An AI-powered Retrieval-Augmented Generation (RAG) system built with **Spring Boot**, **Ollama**, and **ChromaDB**. This application allows you to upload documents and perform intelligent Q&A using local LLMs.

---

## �️ Software Requirements

Ensure you have the following installed before setting up the project:

| Software | Version | Purpose |
| :--- | :--- | :--- |
| **Java JDK** | 17 | Core Runtime |
| **Maven** | 3.6+ | Build Tool |
| **Python** | 3.8+ | To run ChromaDB |
| **Ollama** | Latest | Local AI Engine |

---

## 🚀 Quick Start Guide

### 1. Download AI Models
Pull the chat and embedding models via Ollama:
```bash
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

### 2. Install Dependencies
Install ChromaDB and compatible NumPy:
```bash
pip install "chromadb>=0.4.0, <1.0.0" "numpy<2.0"
```

### 3. Run the System

Follow these steps in separate terminals:

**Terminal 1: Start ChromaDB**
```bash
# Start the vector store server
chroma run --host localhost --port 8000
```

**Terminal 2: Build & Launch**
```bash
# Navigate to the project folder
mvn clean package -DskipTests

# Run the application
java -jar target/rag-0.0.1-SNAPSHOT.jar
```

---

## 📡 API Endpoints

| Action | Method | Endpoint | Note |
| :--- | :--- | :--- | :--- |
| **Upload File** | `POST` | `/api/rag/upload` | Form-data: `file` |
| **Ask Question**| `GET` | `/api/rag/ask` | Query: `question` |

### Example Test (cURL):
```bash
# Ask a question
curl "http://localhost:8080/api/rag/ask?question=What%20is%20this%20document%20about?"
```

---

### ✨ Author
**Nandhakumar Nagaraj**  
📫 Let's connect on [LinkedIn](https://linkedin.com/in/nandhakumar-nagaraj-458991211)
