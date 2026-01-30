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
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.client.ChatClient.PromptSpec;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RagServiceImpl
 * 
 * Tests document upload, processing, and question answering:
 * - Document upload and validation
 * - Document reading and chunking
 * - Embedding generation and storage
 * - Question answering workflow
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RagServiceImpl Unit Tests")
class RagServiceImplTest {

  @Mock
  private VectorStore vectorStore;

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

  @Captor
  private ArgumentCaptor<List<Document>> documentListCaptor;

  @Captor
  private ArgumentCaptor<SearchRequest> searchRequestCaptor;

  private RagServiceImpl ragService;

  @BeforeEach
  void setUp() {
    when(chatClientBuilder.build()).thenReturn(mockChatClient);
    when(mockChatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.chatResponse()).thenReturn(mockChatResponse);
    when(mockChatResponse.getResult()).thenReturn(mockGeneration);

    ragService = new RagServiceImpl(vectorStore, chatClientBuilder);
  }

  // ==================== Document Upload Tests ====================

  @Test
  @DisplayName("Should successfully upload and process PDF document")
  void uploadAndProcessDocument_validPdf_success() throws IOException {
    // Arrange
    MultipartFile mockFile = TestDataBuilder.createMockPdfFile();
    doNothing().when(vectorStore).add(anyList());

    // Act
    ragService.uploadAndProcessDocument(mockFile);

    // Assert
    verify(vectorStore).add(anyList());
  }

  @Test
  @DisplayName("Should successfully upload and process TXT document")
  void uploadAndProcessDocument_validTxt_success() throws IOException {
    // Arrange
    MultipartFile mockFile = TestDataBuilder.createMockTxtFile();
    doNothing().when(vectorStore).add(anyList());

    // Act
    ragService.uploadAndProcessDocument(mockFile);

    // Assert
    verify(vectorStore).add(anyList());
  }

  @Test
  @DisplayName("Should throw IOException for empty file")
  void uploadAndProcessDocument_emptyFile_throwsIOException() {
    // Arrange
    MultipartFile emptyFile = TestDataBuilder.createEmptyFile();

    // Act & Assert
    assertThatThrownBy(() -> ragService.uploadAndProcessDocument(emptyFile))
        .isInstanceOf(IOException.class);
  }

  @Test
  @DisplayName("Should handle file read error gracefully")
  void uploadAndProcessDocument_fileReadError_throwsIOException() throws IOException {
    // Arrange
    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getInputStream()).thenThrow(new IOException("File read error"));
    when(mockFile.getOriginalFilename()).thenReturn("test.pdf");

    // Act & Assert
    assertThatThrownBy(() -> ragService.uploadAndProcessDocument(mockFile))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("Failed to process document");
  }

  @Test
  @DisplayName("Should store chunked documents in vector store")
  void uploadAndProcessDocument_storesChunks_success() throws IOException {
    // Arrange
    MultipartFile mockFile = TestDataBuilder.createMockTxtFile();
    doNothing().when(vectorStore).add(documentListCaptor.capture());

    // Act
    ragService.uploadAndProcessDocument(mockFile);

    // Assert
    List<Document> storedDocuments = documentListCaptor.getValue();
    assertThat(storedDocuments).isNotEmpty();
    verify(vectorStore, times(1)).add(anyList());
  }

  @Test
  @DisplayName("Should handle vector store error during upload")
  void uploadAndProcessDocument_vectorStoreError_throwsIOException() {
    // Arrange
    MultipartFile mockFile = TestDataBuilder.createMockTxtFile();
    doThrow(new RuntimeException("Vector store unavailable"))
        .when(vectorStore).add(anyList());

    // Act & Assert
    assertThatThrownBy(() -> ragService.uploadAndProcessDocument(mockFile))
        .isInstanceOf(IOException.class);
  }

  // ==================== Question Answering Tests ====================

  @Test
  @DisplayName("Should successfully answer question with relevant documents")
  void askQuestion_withRelevantDocs_returnsAnswer() {
    // Arrange
    String question = TestDataBuilder.SampleQuestions.VALID_QUESTION;
    String expectedAnswer = TestDataBuilder.SampleResponses.VALID_RESPONSE;
    List<Document> mockDocuments = TestDataBuilder.createMockDocuments();

    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn(expectedAnswer);

    // Act
    String answer = ragService.askQuestion(question);

    // Assert
    assertThat(answer).isEqualTo(expectedAnswer);
    verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
    assertThat(searchRequestCaptor.getValue().getQuery()).isEqualTo(question);
    assertThat(searchRequestCaptor.getValue().getTopK()).isEqualTo(5);
  }

  @Test
  @DisplayName("Should return default message when no documents found")
  void askQuestion_noDocuments_returnsDefaultMessage() {
    // Arrange
    String question = TestDataBuilder.SampleQuestions.IRRELEVANT_QUESTION;
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(Collections.emptyList());

    // Act
    String answer = ragService.askQuestion(question);

    // Assert
    assertThat(answer).isEqualTo(
        "I don't have enough information to answer that question from the loaded documents.");
    verify(chatClient, never()).prompt();
  }

  @Test
  @DisplayName("Should search with top-K value of 5")
  void askQuestion_searchesWithCorrectTopK_success() {
    // Arrange
    String question = "Test question";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn("Answer");

    // Act
    ragService.askQuestion(question);

    // Assert
    verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
    assertThat(searchRequestCaptor.getValue().getTopK()).isEqualTo(5);
  }

  @Test
  @DisplayName("Should include source metadata in context")
  void askQuestion_includesSourceMetadata_success() {
    // Arrange
    String question = "Test";
    Document docWithMetadata = TestDataBuilder.createMockDocument(
        "Content",
        TestDataBuilder.createMetadata("source.txt"));

    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(List.of(docWithMetadata));
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn("Answer");

    // Act
    String answer = ragService.askQuestion(question);

    // Assert
    assertThat(answer).isNotNull();
    verify(mockPromptSpec).user(argThat(prompt -> prompt.contains("source.txt")));
  }

  @Test
  @DisplayName("Should handle empty question")
  void askQuestion_emptyQuestion_processesCorrectly() {
    // Arrange
    String emptyQuestion = "";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(Collections.emptyList());

    // Act
    String answer = ragService.askQuestion(emptyQuestion);

    // Assert
    assertThat(answer).isNotNull();
  }

  @Test
  @DisplayName("Should handle very long question")
  void askQuestion_longQuestion_processesCorrectly() {
    // Arrange
    String longQuestion = TestDataBuilder.SampleQuestions.LONG_QUESTION;
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn("Answer");

    // Act
    String answer = ragService.askQuestion(longQuestion);

    // Assert
    assertThat(answer).isNotNull();
    verify(vectorStore).similaritySearch(searchRequestCaptor.capture());
    assertThat(searchRequestCaptor.getValue().getQuery()).isEqualTo(longQuestion);
  }

  // ==================== Error Handling Tests ====================

  @Test
  @DisplayName("Should throw RuntimeException when vector store search fails")
  void askQuestion_vectorStoreSearchFails_throwsRuntimeException() {
    // Arrange
    String question = "Test";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenThrow(new RuntimeException("Search failed"));

    // Act & Assert
    assertThatThrownBy(() -> ragService.askQuestion(question))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to answer question");
  }

  @Test
  @DisplayName("Should throw RuntimeException when ChatClient fails")
  void askQuestion_chatClientFails_throwsRuntimeException() {
    // Arrange
    String question = "Test";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call())
        .thenThrow(new RuntimeException("OpenAI API error"));

    // Act & Assert
    assertThatThrownBy(() -> ragService.askQuestion(question))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to answer question");
  }

  // ==================== Integration Scenario Tests ====================

  @Test
  @DisplayName("Should handle complete upload-ask workflow")
  void completeWorkflow_uploadAndAsk_success() throws IOException {
    // Arrange - Upload
    MultipartFile mockFile = TestDataBuilder.createMockTxtFile();
    doNothing().when(vectorStore).add(anyList());

    // Act - Upload
    ragService.uploadAndProcessDocument(mockFile);

    // Arrange - Ask
    String question = "What is Spring AI?";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn("Answer");

    // Act - Ask
    String answer = ragService.askQuestion(question);

    // Assert
    assertThat(answer).isNotNull();
    verify(vectorStore).add(anyList());
    verify(vectorStore).similaritySearch(any(SearchRequest.class));
  }

  @Test
  @DisplayName("Should handle multiple document uploads")
  void multipleUploads_allProcessedCorrectly_success() throws IOException {
    // Arrange
    MultipartFile file1 = TestDataBuilder.createMockTxtFile();
    MultipartFile file2 = TestDataBuilder.createMockPdfFile();
    doNothing().when(vectorStore).add(anyList());

    // Act
    ragService.uploadAndProcessDocument(file1);
    ragService.uploadAndProcessDocument(file2);

    // Assert
    verify(vectorStore, times(2)).add(anyList());
  }

  @Test
  @DisplayName("Should handle multiple questions")
  void multipleQuestions_allAnsweredCorrectly_success() {
    // Arrange
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn("Answer");

    // Act
    questions.forEach(q -> ragService.askQuestion(q));

    // Assert
    verify(vectorStore, times(3)).similaritySearch(any(SearchRequest.class));
    verify(chatClient, times(3)).prompt();
  }

  // ==================== Performance Tests ====================

  @Test
  @DisplayName("Should complete question answering quickly")
  void askQuestion_performanceCheck_completesQuickly() {
    // Arrange
    String question = "Test";
    when(vectorStore.similaritySearch(any(SearchRequest.class)))
        .thenReturn(TestDataBuilder.createMockDocuments());
    when(chatClient.prompt()).thenReturn(mockPromptSpec);
    when(mockPromptSpec.user(anyString())).thenReturn(mockChatClientCall);
    when(mockChatClientCall.call()).thenReturn(mockChatClientCallResponse);
    when(mockChatClientCallResponse.content()).thenReturn("Answer");

    // Act
    long startTime = System.currentTimeMillis();
    ragService.askQuestion(question);
    long endTime = System.currentTimeMillis();

    // Assert - Should complete in reasonable time (mock should be fast)
    assertThat(endTime - startTime).isLessThan(1000); // Less than 1 second
  }
}
