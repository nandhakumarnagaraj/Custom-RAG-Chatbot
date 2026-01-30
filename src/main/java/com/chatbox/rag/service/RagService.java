package com.chatbox.rag.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface RagService {
	
	void uploadAndProcessDocument(MultipartFile file) throws IOException;

	String askQuestion(String question);
}
