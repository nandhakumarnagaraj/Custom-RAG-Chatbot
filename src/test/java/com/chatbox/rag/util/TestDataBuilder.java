package com.chatbox.rag.util;

import org.springframework.ai.document.Document;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Data Builder - Provides reusable test data for RAG chatbot tests
 * 
 * This builder follows the Builder pattern to create consistent test data
 * across all test classes, ensuring reproducibility and maintainability.
 */
public class TestDataBuilder {

  /**
   * Creates a mock PDF file for testing document upload
   */
  public static MockMultipartFile createMockPdfFile() {
    return createMockPdfFile("test-document.pdf", "Sample PDF content for testing RAG system.");
  }

  /**
   * Creates a mock PDF file with custom name and content
   */
  public static MockMultipartFile createMockPdfFile(String filename, String content) {
    return new MockMultipartFile(
        "file",
        filename,
        "application/pdf",
        content.getBytes());
  }

  /**
   * Creates a mock TXT file for testing
   */
  public static MockMultipartFile createMockTxtFile() {
    String content = """
        Spring AI Framework Overview

        Spring AI is a powerful framework for building AI-powered applications.
        It provides unified APIs for various AI models and services.

        Key features include:
        - Chat model integration
        - Vector store support
        - Document readers
        - Embedding generation
        """;

    return new MockMultipartFile(
        "file",
        "test.txt",
        "text/plain",
        content.getBytes());
  }

  /**
   * Creates a mock DOCX file for testing
   */
  public static MockMultipartFile createMockDocxFile() {
    return new MockMultipartFile(
        "file",
        "test.docx",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "Mock DOCX content".getBytes());
  }

  /**
   * Creates an empty file for testing error cases
   */
  public static MockMultipartFile createEmptyFile() {
    return new MockMultipartFile(
        "file",
        "empty.txt",
        "text/plain",
        new byte[0]);
  }

  /**
   * Creates a large file for testing file size limits
   */
  public static MockMultipartFile createLargeFile() {
    byte[] largeContent = new byte[100 * 1024 * 1024]; // 100MB
    return new MockMultipartFile(
        "file",
        "large-file.pdf",
        "application/pdf",
        largeContent);
  }

  /**
   * Creates a list of mock Document objects for testing vector store
   */
  public static List<Document> createMockDocuments() {
    List<Document> documents = new ArrayList<>();

    Document doc1 = new Document(
        "Spring AI is a framework for building AI applications in Java.",
        Map.of("source", "doc1.txt", "page", 1));

    Document doc2 = new Document(
        "RAG (Retrieval Augmented Generation) combines retrieval and generation.",
        Map.of("source", "doc2.txt", "page", 1));

    Document doc3 = new Document(
        "Vector databases store embeddings for semantic search.",
        Map.of("source", "doc3.txt", "page", 1));

    documents.add(doc1);
    documents.add(doc2);
    documents.add(doc3);

    return documents;
  }

  /**
   * Creates a single mock Document
   */
  public static Document createMockDocument(String content) {
    return new Document(content, Map.of("source", "test.txt"));
  }

  /**
   * Creates a mock document with metadata
   */
  public static Document createMockDocument(String content, Map<String, Object> metadata) {
    return new Document(content, metadata);
  }

  /**
   * Creates chunked documents (simulating document splitting)
   */
  public static List<Document> createChunkedDocuments() {
    List<Document> chunks = new ArrayList<>();

    chunks.add(new Document(
        "Spring AI provides a unified API for AI models. It supports multiple providers.",
        Map.of("chunk_index", 0, "source", "spring-ai.txt")));

    chunks.add(new Document(
        "The framework includes vector store integration for semantic search.",
        Map.of("chunk_index", 1, "source", "spring-ai.txt")));

    chunks.add(new Document(
        "Document readers support PDF, DOCX, and TXT formats.",
        Map.of("chunk_index", 2, "source", "spring-ai.txt")));

    return chunks;
  }

  /**
   * Creates a mock embedding vector (simulating OpenAI embeddings)
   */
  public static List<Double> createMockEmbedding() {
    List<Double> embedding = new ArrayList<>();
    // Typical OpenAI embedding size is 1536 dimensions
    for (int i = 0; i < 1536; i++) {
      embedding.add(Math.random());
    }
    return embedding;
  }

  /**
   * Creates a smaller mock embedding for faster tests
   */
  public static List<Double> createSmallMockEmbedding() {
    List<Double> embedding = new ArrayList<>();
    for (int i = 0; i < 128; i++) {
      embedding.add(Math.random());
    }
    return embedding;
  }

  /**
   * Sample questions for testing
   */
  public static class SampleQuestions {
    public static final String VALID_QUESTION = "What is Spring AI?";
    public static final String COMPLEX_QUESTION = "How does RAG work and what are its benefits?";
    public static final String EMPTY_QUESTION = "";
    public static final String LONG_QUESTION = "This is a very long question that might exceed typical length limits. "
        +
        "It contains multiple sentences and asks about various aspects of the system. " +
        "Can the system handle such long queries effectively? " +
        "What happens when the question contains too much context? " +
        "Will it still provide accurate answers?";
    public static final String SHORT_QUESTION = "Why?";
    public static final String IRRELEVANT_QUESTION = "What is the capital of France?";
  }

  /**
   * Sample AI responses for mocking
   */
  public static class SampleResponses {
    public static final String VALID_RESPONSE = "Spring AI is a framework for building AI-powered applications in Java. "
        +
        "It provides a unified API for working with various AI models and services.";

    public static final String NO_CONTEXT_RESPONSE = "I don't have enough information to answer that question from the loaded documents.";

    public static final String ERROR_RESPONSE = "An error occurred while processing your question.";
  }

  /**
   * Test metadata builder
   */
  public static Map<String, Object> createMetadata(String filename) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("file_name", filename);
    metadata.put("upload_timestamp", System.currentTimeMillis());
    metadata.put("content_type", "text/plain");
    return metadata;
  }

  /**
   * Builder pattern for creating custom test documents
   */
  public static class DocumentBuilder {
    private String content = "Default test content";
    private Map<String, Object> metadata = new HashMap<>();

    public DocumentBuilder withContent(String content) {
      this.content = content;
      return this;
    }

    public DocumentBuilder withMetadata(String key, Object value) {
      this.metadata.put(key, value);
      return this;
    }

    public DocumentBuilder withSource(String source) {
      this.metadata.put("source", source);
      return this;
    }

    public DocumentBuilder withPage(int page) {
      this.metadata.put("page", page);
      return this;
    }

    public Document build() {
      return new Document(content, metadata);
    }

    public List<Document> buildList(int count) {
      List<Document> documents = new ArrayList<>();
      for (int i = 0; i < count; i++) {
        Map<String, Object> meta = new HashMap<>(metadata);
        meta.put("index", i);
        documents.add(new Document(content + " (copy " + i + ")", meta));
      }
      return documents;
    }
  }

  /**
   * Creates a new DocumentBuilder instance
   */
  public static DocumentBuilder documentBuilder() {
    return new DocumentBuilder();
  }
}
