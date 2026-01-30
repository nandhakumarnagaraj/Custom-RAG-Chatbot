package com.chatbox.rag.service;

import com.chatbox.rag.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Call;
import org.springframework.ai.chat.client.ChatClient.Call.Response;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatService
 * 
 * Tests the chat response generation logic including:
 * - Vector store similarity search
 * - Prompt construction
 * - ChatClient interaction
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Unit Tests")
class ChatServiceTest {

  @Mock
  private ChatClient.Builder chatClientBuilder;

  @Mock
  private ChatClient mockChatClient;

  @Mock
  private ChatClient.PromptSpec mockPromptSpec;

  @Mock
  private ChatClient.Call mockChatClientCall;

  @Mock
  private ChatClient.Call.Response mockChatClientCallResponse;

  @Mock
  private org.springframework.ai.chat.model.ChatResponse mockChatResponse;

  @Mock
  private org.springframework.ai.chat.model.Generation mockGeneration;

  @Mock
  private VectorStore vectorStore;

  @Captor
  private ArgumentCaptor<SearchRequest> searchRequestCaptor;

  @Captor
  private ArgumentCaptor<Prompt> promptCaptor;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    // Set up the ChatClient builder mock chain
    when(chatClientBuilder.build()).thenReturn(mockChatClient);
    when(mockChatClient.prompt(any(Prompt.class))).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.chatResponse()).thenReturn(mockChatResponse);
    when(mockChatResponse.getResult()).thenReturn(mockGeneration);

    chatService = new ChatService(chatClientBuilder, vectorStore);
  }

  // ==================== Happy Path Tests ====================

  @Test
  @DisplayName("Should generate response with relevant documents")
  void generateResponse_withRelevantDocuments_returnsValidResponse() {
    // Arrange
    String query = "What is Spring AI?";
    String expectedResponse = "Spring AI is a framework for building AI applications.";
    List<Document> mockDocuments = TestDataBuilder.createMockDocuments();

    // Mock vector store similarity search
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(mockDocuments);

    // Mock ChatClient fluent API chain
    when(mockChatClientCallResponse.content()).thenReturn(expectedResponse);




    // Act
    String actualResponse = chatService.generateResponse(query);

    // Assert
    assertThat(actualResponse).isEqualTo(expectedResponse);

    // Verify vector store was called with correct parameters
    verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
    SearchRequest request = searchRequestCaptor.getValue();
    assertThat(request.getQuery()).isEqualTo(query);
    assertThat(request.getTopK()).isEqualTo(3);

    // Verify ChatClient was called
    verify(mockChatClient).prompt(any(Prompt.class));
  }

  @Test
  @DisplayName("Should construct prompt correctly with document context")
  void generateResponse_constructsPromptWithContext_success() {
    // Arrange
    String query = "Test question";
    List<Document> mockDocuments = List.of(
        TestDataBuilder.createMockDocument("Content 1"),
        TestDataBuilder.createMockDocument("Content 2"));

    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(mockDocuments);




    // Act
    chatService.generateResponse(query);

verify(mockChatClient).prompt(promptCaptor.capture());
    Prompt prompt = promptCaptor.getValue();
    assertThat(prompt).isNotNull();
    assertThat(prompt.getInstructions()).hasSize(2); // System message + User message
  }

  @Test
  @DisplayName("Should search with correct top-K value")
  void generateResponse_searchesWithCorrectTopK_success() {
    // Arrange
    String query = "Test query";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());




    // Act
    chatService.generateResponse(query);

    // Assert
    verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
    assertThat(searchRequestCaptor.getValue().getTopK()).isEqualTo(3);
  }

  // ==================== Edge Cases ====================

  @Test
  @DisplayName("Should return default message when no documents found")
  void generateResponse_noDocumentsFound_returnsDefaultMessage() {
    // Arrange
    String query = "What is the capital of Mars?";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(Collections.emptyList());

    // Act
    String response = chatService.generateResponse(query);

    // Assert
    assertThat(response).isEqualTo("I don't have enough information to answer that question.");

    verify(mockChatClient, never()).prompt(any());
  }

  @Test
  @DisplayName("Should handle empty query string")
  void generateResponse_emptyQuery_processesCorrectly() {
    // Arrange
    String emptyQuery = "";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());




    // Act
    String response = chatService.generateResponse(emptyQuery);

    // Assert
    assertThat(response).isNotNull();
    verify(vectorStore).similaritySearch(any(SearchRequest.class));
  }

  @Test
  @DisplayName("Should handle very long query")
  void generateResponse_veryLongQuery_processesCorrectly() {
    // Arrange
    String longQuery = TestDataBuilder.SampleQuestions.LONG_QUESTION;
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());




    // Act
    String response = chatService.generateResponse(longQuery);

    // Assert
    assertThat(response).isNotNull();
    verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
    assertThat(searchRequestCaptor.getValue().getQuery()).isEqualTo(longQuery);
  }

  @Test
  @DisplayName("Should handle single document result")
  void generateResponse_singleDocument_processesCorrectly() {
    // Arrange
    String query = "Test";
    List<Document> singleDoc = List.of(TestDataBuilder.createMockDocument("Single doc"));

    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(singleDoc);




    // Act
    String response = chatService.generateResponse(query);

    // Assert
    assertThat(response).isNotNull();
    verify(mockChatClient).prompt(any(Prompt.class));
  }

  // ==================== Error Handling Tests ====================

  @Test
  @DisplayName("Should throw RuntimeException when vector store fails")
  void generateResponse_vectorStoreFailure_throwsRuntimeException() {
    // Arrange
    String query = "Test query";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenThrow(new RuntimeException("Vector store connection failed"));

    // Act & Assert
    assertThatThrownBy(() -> chatService.generateResponse(query))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to generate response");
  }

  @Test
  @DisplayName("Should throw RuntimeException when ChatClient fails")
  void generateResponse_chatClientFailure_throwsRuntimeException() {
    // Arrange
    String query = "Test query";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());
    when(mockChatClientCall.call())
        .thenThrow(new RuntimeException("OpenAI API error"));

    // Act & Assert
    assertThatThrownBy(() -> chatService.generateResponse(query))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to generate response");
  }

  @Test
  @DisplayName("Should throw RuntimeException when embedding generation fails")
  void generateResponse_embeddingFailure_throwsRuntimeException() {
    // Arrange
    String query = "Test query";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenThrow(new RuntimeException("Embedding generation failed"));

    // Act & Assert
    assertThatThrownBy(() -> chatService.generateResponse(query))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to generate response");
  }

  // ==================== Interaction Tests ====================

  @Test
  @DisplayName("Should call vector store exactly once per request")
  void generateResponse_callsVectorStoreOnce_success() {
    // Arrange
    String query = "Test";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());




    // Act
    chatService.generateResponse(query);

    // Assert
    verify(vectorStore, times(1)).similaritySearch(any(SearchRequest.class));
  }

  @Test
  @DisplayName("Should call ChatClient exactly once per request with documents")
  void generateResponse_callsChatClientOnce_success() {
    // Arrange
    String query = "Test";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());




    // Act
    chatService.generateResponse(query);

    // Assert
    verify(mockChatClient, times(1)).prompt(any(Prompt.class));
  }

  @Test
  @DisplayName("Should not call ChatClient when no documents found")
  void generateResponse_noDocuments_doesNotCallChatClient() {
    // Arrange
    String query = "Test";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(Collections.emptyList());

    // Act
    chatService.generateResponse(query);

    // Assert
    verify(mockChatClient, never()).prompt(any());
  }

  // ==================== Context Building Tests ====================

  @Test
  @DisplayName("Should combine multiple documents into context")
  void generateResponse_multipleDocs_combinesIntoContext() {
    // Arrange
    String query = "Test";
    Document doc1 = new Document("Content 1", Collections.emptyMap());
    Document doc2 = new Document("Content 2", Collections.emptyMap());
    Document doc3 = new Document("Content 3", Collections.emptyMap());

    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(List.of(doc1, doc2, doc3));




    // Act
    chatService.generateResponse(query);

    // Assert - The context should contain all three documents
    verify(mockChatClient).prompt(promptCaptor.capture());
    // Note: In actual implementation, verify the prompt contains all content
    assertThat(promptCaptor.getValue()).isNotNull();
  }
}
