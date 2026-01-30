package com.chatbox.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

  @Bean
  public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
    return ChatClient.builder(chatModel);
  }

  // Commented out to use ChromaDB auto-configuration instead
  // @Bean
  // public VectorStore vectorStore(EmbeddingModel embeddingModel) {
  // return new SimpleVectorStore(embeddingModel);
  // }
}
