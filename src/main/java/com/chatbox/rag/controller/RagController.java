package com.chatbox.rag.controller;

import com.chatbox.rag.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }
        try {
            ragService.uploadAndProcessDocument(file);
            return ResponseEntity.ok("Document uploaded and processed successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload and process document: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions during processing
            return ResponseEntity.status(500).body("An unexpected error occurred during document processing: " + e.getMessage());
        }
    }

    @GetMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestParam("question") String question) {
        try {
            String answer = ragService.askQuestion(question);
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to answer question: " + e.getMessage()));
        }
    }
}