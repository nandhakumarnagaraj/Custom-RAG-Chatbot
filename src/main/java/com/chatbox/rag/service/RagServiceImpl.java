package com.chatbox.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagServiceImpl implements RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagServiceImpl.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    @Autowired
    public RagServiceImpl(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public void uploadAndProcessDocument(MultipartFile file) throws IOException {
        logger.info("Processing uploaded file: {}", file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream()) {
            Resource resource = new ByteArrayResource(inputStream.readAllBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            List<Document> documents;
            String contentType = file.getContentType();

            if (contentType != null && contentType.equals("application/pdf")) {
                PagePdfDocumentReader documentReader = new PagePdfDocumentReader(resource);
                documents = documentReader.read(); // ✅ FIXED: Changed from get() to read()
            } else if (contentType != null && (contentType.equals("text/plain") || contentType.startsWith("text/"))) {
                Document doc = new Document(new String(file.getBytes()));
                doc.getMetadata().put("file_name", file.getOriginalFilename());
                documents = List.of(doc);
            } else {
                TikaDocumentReader documentReader = new TikaDocumentReader(resource);
                documents = documentReader.read(); // ✅ FIXED: Changed from get() to read()
            }

            processAndStoreDocuments(documents);

        } catch (Exception e) {
            logger.error("Error processing document: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to process document: " + e.getMessage(), e);
        }
    }

    private void processAndStoreDocuments(List<Document> documents) {
        if (documents.isEmpty()) {
            logger.warn("No documents to process. Skipping chunking and storing.");
            return;
        }
        logger.info("Splitting {} documents into chunks...", documents.size());

        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> chunks = textSplitter.apply(documents);
        logger.info("Generated {} chunks from {} documents.", chunks.size(), documents.size());

        if (chunks.isEmpty()) {
            logger.warn("No chunks generated after splitting. Skipping storing.");
            return;
        }
        logger.info("Storing {} chunks in vector store...", chunks.size());
        vectorStore.add(chunks);
        logger.info("Chunks stored successfully.");
    }

    @Override
    public String askQuestion(String question) {
        logger.info("Received question: {}", question);

        try {
            logger.info("Searching for similar chunks in vector store...");
            List<Document> similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.query(question).withTopK(5));
            logger.info("Found {} similar documents.", similarDocuments.size());

            if (similarDocuments.isEmpty()) {
                logger.warn("No similar documents found for the question.");
                return "I don't have enough information to answer that question from the loaded documents.";
            }

            String retrievedContent = similarDocuments.stream()
                    .map(doc -> doc.getContent() +
                            (doc.getMetadata().containsKey("file_name")
                                    ? " (Source: " + doc.getMetadata().get("file_name") + ")"
                                    : ""))
                    .collect(Collectors.joining("\n\n---\n\n"));

            String promptMessage = String.format(
                    """
                            You are a helpful AI assistant. Use the following pieces of information to answer the user's question.
                            If you don't know the answer, just say that you don't know, don't try to make up an answer.
                            Provide a concise answer based only on the provided context.

                            Context:
                            %s

                            Question: %s
                            """,
                    retrievedContent, question);

            logger.debug("Prompt sent to LLM: {}", promptMessage);

            String answer = chatClient.prompt()
                    .user(promptMessage)
                    .call()
                    .content();

            logger.info("Received answer from chat model.");
            return answer;

        } catch (Exception e) {
            logger.error("Error answering question: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to answer question", e);
        }
    }
}
