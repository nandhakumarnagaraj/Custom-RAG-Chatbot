package com.chatbox.rag.controller;

import com.chatbox.rag.service.RagService;
import com.chatbox.rag.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RagController
 * 
 * Tests REST API endpoints:
 * - POST /api/rag/upload - Document upload
 * - GET /api/rag/ask - Question answering
 * 
 * Uses @WebMvcTest for focused controller testing with MockMvc
 */
@WebMvcTest(RagController.class)
@DisplayName("RagController Integration Tests")
class RagControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RagService ragService;

  // ==================== Upload Document Tests ====================

  @Test
  @DisplayName("POST /api/rag/upload - Should upload valid PDF successfully")
  void uploadDocument_validPdf_returns200() throws Exception {
    // Arrange
    MockMultipartFile file = TestDataBuilder.createMockPdfFile();
    doNothing().when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(file))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("successfully")));

    verify(ragService, times(1)).uploadAndProcessDocument(any());
  }

  @Test
  @DisplayName("POST /api/rag/upload - Should upload valid TXT successfully")
  void uploadDocument_validTxt_returns200() throws Exception {
    // Arrange
    MockMultipartFile file = TestDataBuilder.createMockTxtFile();
    doNothing().when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(file))
        .andExpect(status().isOk())
        .andExpect(content().string("Document uploaded and processed successfully."));

    verify(ragService, times(1)).uploadAndProcessDocument(any());
  }

  @Test
  @DisplayName("POST /api/rag/upload - Should reject empty file with 400")
  void uploadDocument_emptyFile_returns400() throws Exception {
    // Arrange
    MockMultipartFile emptyFile = TestDataBuilder.createEmptyFile();

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(emptyFile))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("select a file")));

    verify(ragService, never()).uploadAndProcessDocument(any());
  }

  @Test
  @DisplayName("POST /api/rag/upload - Should return 500 when service throws IOException")
  void uploadDocument_serviceThrowsIOException_returns500() throws Exception {
    // Arrange
    MockMultipartFile file = TestDataBuilder.createMockPdfFile();
    doThrow(new IOException("File processing error"))
        .when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(file))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("Failed to upload and process document")));
  }

  @Test
  @DisplayName("POST /api/rag/upload - Should return 500 when unexpected exception occurs")
  void uploadDocument_unexpectedException_returns500() throws Exception {
    // Arrange
    MockMultipartFile file = TestDataBuilder.createMockPdfFile();
    doThrow(new RuntimeException("Unexpected error"))
        .when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(file))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("unexpected error")));
  }

  @Test
  @DisplayName("POST /api/rag/upload - Should handle large file upload")
  void uploadDocument_largeFile_processesCorrectly() throws Exception {
    // Arrange
    MockMultipartFile largeFile = new MockMultipartFile(
        "file",
        "large.pdf",
        "application/pdf",
        new byte[1024 * 1024] // 1MB
    );
    doNothing().when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(largeFile))
        .andExpect(status().isOk());

    verify(ragService, times(1)).uploadAndProcessDocument(any());
  }

  @Test
  @DisplayName("POST /api/rag/upload - Should handle DOCX file")
  void uploadDocument_docxFile_returns200() throws Exception {
    // Arrange
    MockMultipartFile docxFile = TestDataBuilder.createMockDocxFile();
    doNothing().when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(docxFile))
        .andExpect(status().isOk());

    verify(ragService, times(1)).uploadAndProcessDocument(any());
  }

  // ==================== Ask Question Tests ====================

  @Test
  @DisplayName("GET /api/rag/ask - Should return answer for valid question")
  void askQuestion_validQuestion_returns200() throws Exception {
    // Arrange
    String question = TestDataBuilder.SampleQuestions.VALID_QUESTION;
    String expectedAnswer = TestDataBuilder.SampleResponses.VALID_RESPONSE;
    when(ragService.askQuestion(anyString())).thenReturn(expectedAnswer);

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", question)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").value(expectedAnswer));

    verify(ragService, times(1)).askQuestion(question);
  }

  @Test
  @DisplayName("GET /api/rag/ask - Should handle empty question parameter")
  void askQuestion_emptyQuestion_returns200() throws Exception {
    // Arrange
    String emptyQuestion = "";
    when(ragService.askQuestion(anyString()))
        .thenReturn(TestDataBuilder.SampleResponses.NO_CONTEXT_RESPONSE);

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", emptyQuestion))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").exists());

    verify(ragService, times(1)).askQuestion(emptyQuestion);
  }

  @Test
  @DisplayName("GET /api/rag/ask - Should handle very long question")
  void askQuestion_longQuestion_returns200() throws Exception {
    // Arrange
    String longQuestion = TestDataBuilder.SampleQuestions.LONG_QUESTION;
    when(ragService.askQuestion(anyString()))
        .thenReturn("Answer to long question");

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", longQuestion))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").exists());

    verify(ragService, times(1)).askQuestion(longQuestion);
  }

  @Test
  @DisplayName("GET /api/rag/ask - Should return 500 when service fails")
  void askQuestion_serviceThrowsException_returns500() throws Exception {
    // Arrange
    String question = "Test question";
    when(ragService.askQuestion(anyString()))
        .thenThrow(new RuntimeException("Service error"));

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", question))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").value(containsString("Failed to answer question")));
  }

  @Test
  @DisplayName("GET /api/rag/ask - Should handle special characters in question")
  void askQuestion_specialCharacters_returns200() throws Exception {
    // Arrange
    String questionWithSpecialChars = "What is Spring AI? & how does it work?";
    when(ragService.askQuestion(anyString())).thenReturn("Answer");

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", questionWithSpecialChars))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").exists());

    verify(ragService, times(1)).askQuestion(questionWithSpecialChars);
  }

  @Test
  @DisplayName("GET /api/rag/ask - Should URL decode question parameter")
  void askQuestion_urlEncodedQuestion_decodesCorrectly() throws Exception {
    // Arrange
    String question = "What is Spring AI";
    when(ragService.askQuestion(anyString())).thenReturn("Answer");

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask?question=What+is+Spring+AI"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").exists());

    verify(ragService, times(1)).askQuestion(question);
  }

  @Test
  @DisplayName("GET /api/rag/ask - Should return JSON response")
  void askQuestion_returnsJsonFormat_success() throws Exception {
    // Arrange
    String question = "Test";
    when(ragService.askQuestion(anyString())).thenReturn("Test answer");

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", question))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.answer").isString())
        .andExpect(jsonPath("$.error").doesNotExist());
  }

  // ==================== Error Response Format Tests ====================

  @Test
  @DisplayName("GET /api/rag/ask - Error response should have error field")
  void askQuestion_errorResponse_hasErrorField() throws Exception {
    // Arrange
    when(ragService.askQuestion(anyString()))
        .thenThrow(new RuntimeException("Test error"));

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask")
        .param("question", "Test"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error").exists())
        .andExpect(jsonPath("$.answer").doesNotExist());
  }

  @Test
  @DisplayName("POST /api/rag/upload - Error response should be descriptive")
  void uploadDocument_errorResponse_isDescriptive() throws Exception {
    // Arrange
    MockMultipartFile file = TestDataBuilder.createMockPdfFile();
    String errorMessage = "Vector store connection failed";
    doThrow(new RuntimeException(errorMessage))
        .when(ragService).uploadAndProcessDocument(any());

    // Act & Assert
    mockMvc.perform(multipart("/api/rag/upload")
        .file(file))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(containsString("unexpected error")));
  }

  // ==================== Integration Workflow Tests ====================

  @Test
  @DisplayName("Full workflow - Upload then ask question")
  void fullWorkflow_uploadThenAsk_success() throws Exception {
    // Step 1: Upload document
    MockMultipartFile file = TestDataBuilder.createMockTxtFile();
    doNothing().when(ragService).uploadAndProcessDocument(any());

    mockMvc.perform(multipart("/api/rag/upload")
        .file(file))
        .andExpect(status().isOk());

    // Step 2: Ask question
    String question = "What is Spring AI?";
    when(ragService.askQuestion(anyString()))
        .thenReturn("Spring AI is a framework...");

    mockMvc.perform(get("/api/rag/ask")
        .param("question", question))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").exists());

    // Verify both operations were called
    verify(ragService, times(1)).uploadAndProcessDocument(any());
    verify(ragService, times(1)).askQuestion(question);
  }

  @Test
  @DisplayName("Multiple uploads - Should handle sequential uploads")
  void multipleUploads_handleSequentialUploads_success() throws Exception {
    // Arrange
    MockMultipartFile file1 = TestDataBuilder.createMockTxtFile();
    MockMultipartFile file2 = TestDataBuilder.createMockPdfFile();
    doNothing().when(ragService).uploadAndProcessDocument(any());

    // Act & Assert - First upload
    mockMvc.perform(multipart("/api/rag/upload").file(file1))
        .andExpect(status().isOk());

    // Act & Assert - Second upload
    mockMvc.perform(multipart("/api/rag/upload").file(file2))
        .andExpect(status().isOk());

    verify(ragService, times(2)).uploadAndProcessDocument(any());
  }

  @Test
  @DisplayName("Multiple questions - Should handle sequential questions")
  void multipleQuestions_handleSequentialQuestions_success() throws Exception {
    // Arrange
    when(ragService.askQuestion(anyString()))
        .thenReturn("Answer 1")
        .thenReturn("Answer 2")
        .thenReturn("Answer 3");

    // Act & Assert
    mockMvc.perform(get("/api/rag/ask").param("question", "Q1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").value("Answer 1"));

    mockMvc.perform(get("/api/rag/ask").param("question", "Q2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").value("Answer 2"));

    mockMvc.perform(get("/api/rag/ask").param("question", "Q3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answer").value("Answer 3"));

    verify(ragService, times(3)).askQuestion(anyString());
  }

  // ==================== Performance Tests ====================

  @Test
  @DisplayName("Upload endpoint - Should respond quickly")
  void uploadEndpoint_respondsQuickly_success() throws Exception {
    // Arrange
    MockMultipartFile file = TestDataBuilder.createMockTxtFile();
    doNothing().when(ragService).uploadAndProcessDocument(any());

    // Act
    long startTime = System.currentTimeMillis();

    mockMvc.perform(multipart("/api/rag/upload").file(file))
        .andExpect(status().isOk());

    long endTime = System.currentTimeMillis();

    assertThat(endTime - startTime).isLessThan(1000L);
  }

  @Test
  @DisplayName("Ask endpoint - Should respond quickly")
  void askEndpoint_respondsQuickly_success() throws Exception {
    // Arrange
    when(ragService.askQuestion(anyString())).thenReturn("Quick answer");

    // Act
    long startTime = System.currentTimeMillis();

    mockMvc.perform(get("/api/rag/ask").param("question", "Test"))
        .andExpect(status().isOk());

    long endTime = System.currentTimeMillis();

    // Assert
    assertThat(endTime - startTime).isLessThan(1000L);
  }
}
