package com.kinovek.backend.controller;

import com.kinovek.backend.dto.ApiResponse;
import com.kinovek.backend.service.CoverLetterService;
import com.kinovek.backend.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/cover-letter")
public class CoverLetterController {

    @Autowired
    private CoverLetterService coverLetterService;

    @Autowired
    private ResumeParserService resumeParserService;

    /**
     * POST /api/v1/cover-letter/generate
     * Generate a cover letter from resume + job description.
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateCoverLetter(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            String resumeText = resumeParserService.parseResume(resumeFile);
            String coverLetter = coverLetterService.generateCoverLetter(resumeText, jobDescription);
            return ResponseEntity.ok(ApiResponse.ok(coverLetter));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("INVALID_FILE_TYPE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PROCESSING_ERROR", "Failed to generate cover letter: " + e.getMessage()));
        }
    }
}
