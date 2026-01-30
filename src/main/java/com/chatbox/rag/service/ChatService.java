package com.chatbox.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

  private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
    this.chatClient = chatClientBuilder.build();
    this.vectorStore = vectorStore;
  }

  public String generateResponse(String query) {
    try {
      logger.info("Generating response for query: {}", query);

      // 1. Retrieve similar documents
      List<Document> similarDocuments = vectorStore.similaritySearch(
          SearchRequest.query(query).withTopK(3));

      if (similarDocuments.isEmpty()) {
        logger.warn("No similar documents found for query");
        return "I don't have enough information to answer that question.";
      }

      String context = similarDocuments.stream()
          .map(Document::getContent)
          .collect(Collectors.joining("\n\n"));

      // 2. Build Prompt
      String systemPromptText = """
          You are a helpful assistant.
          Use the following information to answer the user's question.
          If you don't know the answer, just say you don't know.

          Context:
          {context}
          """;

      SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptText);
      Message systemMessage = systemPromptTemplate.createMessage(Map.of("context", context));
      Message userMessage = new UserMessage(query);

      Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

      // 3. Call AI
      String response = chatClient.prompt(prompt).call().content();
      logger.info("Response generated successfully");
      return response;

    } catch (Exception e) {
      logger.error("Error generating response: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to generate response", e);
    }
  }
}
