package com.kinovek.backend.controller;

import com.kinovek.backend.dto.ATSScoreResponse;
import com.kinovek.backend.dto.ApiResponse;
import com.kinovek.backend.dto.EnhanceResponse;
import com.kinovek.backend.service.ATSScoringService;
import com.kinovek.backend.service.ResumeEnhancerService;
import com.kinovek.backend.service.ResumeParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController {

    @Autowired
    private ResumeEnhancerService resumeEnhancerService;

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private ATSScoringService atsScoringService;

    @Autowired
    private com.kinovek.backend.service.ATSConverterService atsConverterService;

    /**
     * POST /api/v1/resume/enhance
     * Enhance resume against a job description.
     */
    @PostMapping("/enhance")
    public ResponseEntity<?> enhanceResume(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            EnhanceResponse result = resumeEnhancerService.enhanceResume(resumeFile, jobDescription);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("INVALID_FILE_TYPE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PROCESSING_ERROR", "Failed to process resume: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/resume/ats-score
     * Get ATS compatibility score for a resume.
     */
    @PostMapping("/ats-score")
    public ResponseEntity<?> getATSScore(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam(value = "jobDescription", defaultValue = "") String jobDescription) {
        try {
            String resumeText = resumeParserService.parseResume(resumeFile);
            ATSScoreResponse result = atsScoringService.calculateScore(resumeText, jobDescription);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("INVALID_FILE_TYPE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PROCESSING_ERROR", "Failed to calculate ATS score: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/resume/ats-convert
     * Convert resume to ATS-friendly PDF.
     */
    @PostMapping("/ats-convert")
    public ResponseEntity<?> convertToATSFriendly(
            @RequestParam("resume") MultipartFile resumeFile) {
        try {
            byte[] pdfBytes = atsConverterService.convertToATSFriendly(resumeFile);

            String originalName = resumeFile.getOriginalFilename();
            String baseName = (originalName != null && originalName.contains("."))
                    ? originalName.substring(0, originalName.lastIndexOf('.')) : "resume";
            String outputName = baseName + "_ATS_Friendly.pdf";

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + outputName + "\"")
                    .header("Content-Type", "application/pdf")
                    .body(pdfBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("INVALID_FILE_TYPE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PROCESSING_ERROR", "Failed to convert resume: " + e.getMessage()));
        }
    }
}
