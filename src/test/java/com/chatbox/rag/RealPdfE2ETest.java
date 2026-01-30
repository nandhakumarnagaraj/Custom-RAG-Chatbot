package com.chatbox.rag;

import com.chatbox.rag.service.RagService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End tests for RAG Chatbot using real PDF file
 * 
 * This test uses the actual Premkumar Resume.pdf file to test the complete
 * workflow.
 * 
 * NOTE: This test requires:
 * - A valid OpenAI API key in application.properties
 * - The Premkumar Resume.pdf file in the project root
 * 
 * @Disabled by default to avoid consuming OpenAI API credits during regular
 *           test runs.
 *           Remove @Disabled annotation when you want to run real E2E tests.
 */
@SpringBootTest
@DisplayName("End-to-End Tests with Real PDF")
class RealPdfE2ETest {

  @Autowired
  private RagService ragService;

  @Test
  @Disabled("Enable this test when ready to test with real OpenAI API")
  @DisplayName("E2E: Upload Premkumar Resume.pdf and answer questions")
  void endToEnd_uploadResumeAndAskQuestions_success() throws IOException {
    // ==================== STEP 1: Upload Resume PDF ====================

    // Load the actual PDF file from project root
    Path pdfPath = Paths.get("Premkumar Resume.pdf");
    assertThat(pdfPath).exists();

    byte[] pdfContent = Files.readAllBytes(pdfPath);
    MultipartFile resumeFile = new MockMultipartFile(
        "file",
        "Premkumar Resume.pdf",
        "application/pdf",
        pdfContent);

    // Upload and process the document
    System.out.println("📄 Uploading Premkumar Resume.pdf...");
    ragService.uploadAndProcessDocument(resumeFile);
    System.out.println("✅ Document uploaded and processed successfully!\n");

    // ==================== STEP 2: Ask Questions ====================

    // Question 1: About work experience
    System.out.println("❓ Question 1: What is Premkumar's work experience?");
    String answer1 = ragService.askQuestion("What is Premkumar's work experience?");
    System.out.println("💡 Answer: " + answer1 + "\n");
    assertThat(answer1).isNotEmpty();
    assertThat(answer1).doesNotContain("I don't have enough information");

    // Question 2: About skills
    System.out.println("❓ Question 2: What programming languages and technologies does Premkumar know?");
    String answer2 = ragService.askQuestion("What programming languages and technologies does Premkumar know?");
    System.out.println("💡 Answer: " + answer2 + "\n");
    assertThat(answer2).isNotEmpty();

    // Question 3: About education
    System.out.println("❓ Question 3: What is Premkumar's educational background?");
    String answer3 = ragService.askQuestion("What is Premkumar's educational background?");
    System.out.println("💡 Answer: " + answer3 + "\n");
    assertThat(answer3).isNotEmpty();

    // Question 4: About projects
    System.out.println("❓ Question 4: What projects has Premkumar worked on?");
    String answer4 = ragService.askQuestion("What projects has Premkumar worked on?");
    System.out.println("💡 Answer: " + answer4 + "\n");
    assertThat(answer4).isNotEmpty();

    // Question 5: Irrelevant question (should return default message)
    System.out.println("❓ Question 5: What is the capital of France? (Irrelevant question)");
    String answer5 = ragService.askQuestion("What is the capital of France?");
    System.out.println("💡 Answer: " + answer5 + "\n");
    // This might return "I don't have enough information" or an actual answer if
    // the model uses external knowledge
    assertThat(answer5).isNotEmpty();

    System.out.println("🎉 All E2E tests passed successfully!");
  }

  @Test
  @Disabled("Enable for manual testing with real API")
  @DisplayName("E2E: Test with Spring AI documentation")
  void endToEnd_springAiDocumentation_success() throws IOException {
    // Load test document from resources
    ClassPathResource resource = new ClassPathResource("test-documents/spring-ai-overview.txt");
    byte[] content = resource.getInputStream().readAllBytes();

    MultipartFile docFile = new MockMultipartFile(
        "file",
        "spring-ai-overview.txt",
        "text/plain",
        content);

    // Upload
    System.out.println("📄 Uploading Spring AI documentation...");
    ragService.uploadAndProcessDocument(docFile);
    System.out.println("✅ Document uploaded!\n");

    // Ask questions
    System.out.println("❓ What are the key features of Spring AI?");
    String answer1 = ragService.askQuestion("What are the key features of Spring AI?");
    System.out.println("💡 " + answer1 + "\n");
    assertThat(answer1).contains("Chat", "Vector", "Document");

    System.out.println("❓ Which vector stores are supported?");
    String answer2 = ragService.askQuestion("Which vector stores are supported?");
    System.out.println("💡 " + answer2 + "\n");
    assertThat(answer2).containsAnyOf("Chroma", "Pinecone", "Weaviate");
  }

  @Test
  @Disabled("Enable for performance testing")
  @DisplayName("Performance: Measure question answering latency")
  void performance_questionAnsweringLatency_withinThreshold() throws IOException {
    // Setup - upload a document first
    Path pdfPath = Paths.get("Premkumar Resume.pdf");
    if (!Files.exists(pdfPath)) {
      System.out.println("⚠️  Premkumar Resume.pdf not found, skipping test");
      return;
    }

    byte[] pdfContent = Files.readAllBytes(pdfPath);
    MultipartFile resumeFile = new MockMultipartFile(
        "file",
        "Premkumar Resume.pdf",
        "application/pdf",
        pdfContent);

    ragService.uploadAndProcessDocument(resumeFile);

    // Measure response time
    String question = "What is Premkumar's work experience?";

    long startTime = System.currentTimeMillis();
    String answer = ragService.askQuestion(question);
    long endTime = System.currentTimeMillis();

    long duration = endTime - startTime;

    System.out.println("⏱️  Response time: " + duration + "ms");
    System.out.println("💡 Answer: " + answer);

    // Assert response time is reasonable (adjust based on your requirements)
    assertThat(duration).isLessThan(10000); // Less than 10 seconds
    assertThat(answer).isNotEmpty();
  }
}
