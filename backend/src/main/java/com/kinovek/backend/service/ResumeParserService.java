package com.kinovek.backend.service;

import com.kinovek.backend.util.DOCXParser;
import com.kinovek.backend.util.PDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    /**
     * Parses a resume file and extracts text content.
     * Supports PDF and DOCX formats only.
     *
     * @param file the uploaded resume file
     * @return extracted text content
     * @throws IOException if the file cannot be parsed
     * @throws IllegalArgumentException if the file type is not supported
     */
    public String parseResume(MultipartFile file) throws IOException {
        log.info("=== PARSING RESUME ===");
        String fileName = file.getOriginalFilename();
        log.info("File: {} | Size: {} bytes", fileName, file.getSize());
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }

        String lowerName = fileName.toLowerCase();
        String text;

        if (lowerName.endsWith(".pdf")) {
            log.info("Extracting text from PDF...");
            text = PDFParser.extractText(file);
        } else if (lowerName.endsWith(".docx")) {
            log.info("Extracting text from DOCX...");
            text = DOCXParser.extractText(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only PDF and DOCX files are supported.");
        }

        log.info("Extracted text length: {} chars", text.length());
        return text;
    }
}
