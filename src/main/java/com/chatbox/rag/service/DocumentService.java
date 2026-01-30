package com.chatbox.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

  private final VectorStore vectorStore;

  public DocumentService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  public void ingestFile(MultipartFile file) throws IOException {
    // 1. Read document
    Resource resource = new InputStreamResource(file.getInputStream());
    TikaDocumentReader reader = new TikaDocumentReader(resource);
    List<Document> documents = reader.read(); // ✅ FIXED: Changed from get() to read()

    // 2. Split into chunks
    TokenTextSplitter splitter = new TokenTextSplitter();
    List<Document> chunks = splitter.apply(documents);

    // 3. Store in Vector DB
    vectorStore.add(chunks);
  }
}
