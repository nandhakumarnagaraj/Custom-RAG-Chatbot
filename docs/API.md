# API Documentation

## Base URL

```
http://localhost:8080/api/rag
```

## Authentication

Currently, no authentication is required. This is a local development application.

---

## Endpoints

### 1. Upload Document

Upload a document to be processed and stored in the vector database.

**Endpoint:** `POST /api/rag/upload`

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| file | File | Yes | Document file (PDF, TXT, etc.) |

**Request Example (cURL):**

```bash
curl -X POST http://localhost:8080/api/rag/upload \
  -F "file=@document.pdf"
```

**Request Example (JavaScript/Fetch):**

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('http://localhost:8080/api/rag/upload', {
  method: 'POST',
  body: formData
})
.then(response => response.text())
.then(data => console.log(data));
```

**Request Example (Python):**

```python
import requests

url = 'http://localhost:8080/api/rag/upload'
files = {'file': open('document.pdf', 'rb')}
response = requests.post(url, files=files)
print(response.text)
```

**Success Response:**

- **Status Code:** 200 OK
- **Content-Type:** text/plain
- **Body:**
  ```
  Document uploaded and processed successfully.
  ```

**Error Responses:**

**400 Bad Request** - Empty file
```
Please select a file to upload.
```

**500 Internal Server Error** - Processing failed
```
Failed to upload and process document: [error details]
```

---

### 2. Ask Question

Ask a question about the uploaded documents. The system will search for relevant context and generate an AI-powered answer.

**Endpoint:** `GET /api/rag/ask`

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| question | String | Yes | The question to ask about the documents |

**Request Example (cURL):**

```bash
curl "http://localhost:8080/api/rag/ask?question=What%20is%20this%20document%20about?"
```

**Request Example (Browser):**

```
http://localhost:8080/api/rag/ask?question=What is the candidate's experience?
```

**Request Example (JavaScript/Fetch):**

```javascript
const question = "What is this document about?";
const encodedQuestion = encodeURIComponent(question);

fetch(`http://localhost:8080/api/rag/ask?question=${encodedQuestion}`)
  .then(response => response.json())
  .then(data => console.log(data.answer));
```

**Request Example (Python):**

```python
import requests

url = 'http://localhost:8080/api/rag/ask'
params = {'question': 'What is this document about?'}
response = requests.get(url, params=params)
print(response.json()['answer'])
```

**Success Response:**

- **Status Code:** 200 OK
- **Content-Type:** application/json
- **Body:**
  ```json
  {
    "answer": "Based on the document, this is about..."
  }
  ```

**Error Response:**

**500 Internal Server Error** - Failed to process question
```json
{
  "error": "Failed to answer question: [error details]"
}
```

---

## Complete Workflow Example

### Step 1: Upload a Document

```bash
curl -X POST http://localhost:8080/api/rag/upload \
  -F "file=@resume.pdf"
```

**Response:**
```
Document uploaded and processed successfully.
```

### Step 2: Ask Questions

```bash
# Question 1
curl "http://localhost:8080/api/rag/ask?question=What%20are%20the%20candidate%27s%20skills?"

# Response
{
  "answer": "The candidate has skills in Java, Spring Boot, Python, Machine Learning..."
}

# Question 2
curl "http://localhost:8080/api/rag/ask?question=What%20is%20the%20work%20experience?"

# Response
{
  "answer": "The candidate has worked at..."
}
```

---

## Sample Questions by Document Type

### For Resumes/CVs

```
- What is the candidate's name?
- What programming languages does the candidate know?
- What is the candidate's educational background?
- What is the candidate's work experience?
- What projects has the candidate worked on?
- What are the candidate's technical skills?
- Is the candidate suitable for a [role] position?
- Summarize the candidate's qualifications
```

### For Technical Documents

```
- What is this document about?
- Summarize the main points
- What are the key features described?
- What technologies are mentioned?
- What are the system requirements?
- How does [feature] work?
```

### For General Documents

```
- What is the main topic?
- Summarize this document
- What are the key takeaways?
- Who is the target audience?
- What problem does this solve?
```

---

## Response Times

| Operation | Expected Time | Notes |
|-----------|---------------|-------|
| Upload small file (<1MB) | 2-5 seconds | Includes processing and embedding |
| Upload large file (>5MB) | 10-30 seconds | Depends on file size and content |
| First question | 10-20 seconds | Models need to load |
| Subsequent questions | 2-5 seconds | Models are already loaded |

---

## Error Codes

| Status Code | Meaning | Common Causes |
|-------------|---------|---------------|
| 200 | Success | Request completed successfully |
| 400 | Bad Request | Empty file, invalid parameters |
| 500 | Internal Server Error | ChromaDB not running, Ollama models not installed, processing error |

---

## Rate Limiting

Currently, there is no rate limiting. This is a local development application.

---

## CORS Configuration

By default, CORS is not configured. If you need to access the API from a web application on a different origin, you'll need to add CORS configuration to the Spring Boot application.

---

## Best Practices

1. **Upload documents before asking questions** - The system needs context to provide answers
2. **Be specific with questions** - More specific questions yield better answers
3. **Upload multiple related documents** - More context improves answer quality
4. **Wait for upload confirmation** - Ensure document is processed before asking questions
5. **URL encode query parameters** - Always encode the question parameter in URLs

---

## Testing with Postman

### Collection Setup

1. Create a new collection named "RAG ChatBot"
2. Set base URL variable: `{{baseUrl}} = http://localhost:8080`

### Request 1: Upload Document

- **Name:** Upload Document
- **Method:** POST
- **URL:** `{{baseUrl}}/api/rag/upload`
- **Body:** form-data
  - Key: `file` (type: File)
  - Value: Select your document

### Request 2: Ask Question

- **Name:** Ask Question
- **Method:** GET
- **URL:** `{{baseUrl}}/api/rag/ask`
- **Params:**
  - Key: `question`
  - Value: `What is this document about?`

---

## Integration Examples

### React Frontend

```javascript
// Upload document
const uploadDocument = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch('http://localhost:8080/api/rag/upload', {
    method: 'POST',
    body: formData
  });
  
  return await response.text();
};

// Ask question
const askQuestion = async (question) => {
  const response = await fetch(
    `http://localhost:8080/api/rag/ask?question=${encodeURIComponent(question)}`
  );
  
  const data = await response.json();
  return data.answer;
};
```

### Angular Frontend

```typescript
// service.ts
uploadDocument(file: File): Observable<string> {
  const formData = new FormData();
  formData.append('file', file);
  
  return this.http.post(
    'http://localhost:8080/api/rag/upload',
    formData,
    { responseType: 'text' }
  );
}

askQuestion(question: string): Observable<any> {
  const params = new HttpParams().set('question', question);
  return this.http.get('http://localhost:8080/api/rag/ask', { params });
}
```

### Vue.js Frontend

```javascript
// Upload document
async uploadDocument(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await axios.post(
    'http://localhost:8080/api/rag/upload',
    formData
  );
  
  return response.data;
}

// Ask question
async askQuestion(question) {
  const response = await axios.get('http://localhost:8080/api/rag/ask', {
    params: { question }
  });
  
  return response.data.answer;
}
```

---

## WebSocket Support

Currently, the API uses HTTP REST. For real-time streaming responses, WebSocket support could be added in future versions.

---

## API Versioning

Current version: **v1** (implicit in `/api/rag` path)

Future versions may use explicit versioning: `/api/v2/rag`

---

## Health Check

While not explicitly implemented, you can check if the service is running:

```bash
curl http://localhost:8080/api/rag/ask?question=test
```

If you get a response (even an error), the service is running.

---

## Monitoring

For production deployments, consider adding:
- Spring Boot Actuator for health checks
- Prometheus metrics
- Logging aggregation
- Request tracing

---

**Last Updated:** 2026-01-30
