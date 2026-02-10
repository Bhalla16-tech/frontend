package com.kinovek.backend.controller;

import com.kinovek.backend.dto.ATSScoreResponse;
import com.kinovek.backend.dto.ApiResponse;
import com.kinovek.backend.dto.EnhanceResponse;
import com.kinovek.backend.service.*;
import com.kinovek.backend.util.KeywordMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

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

    @Autowired
    private ResumeTextParserService resumeTextParserService;

    @Autowired
    private KeywordMatcher keywordMatcher;

    @Autowired
    private ResumeRewriter resumeRewriter;

    @Autowired
    private ATSPDFGenerator atsPdfGenerator;

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

    /**
     * POST /api/v1/resume/enhance-pdf
     * Parse resume, analyze keywords, enhance content, and return ATS-optimized PDF.
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/enhance-pdf")
    public ResponseEntity<?> enhanceResumePdf(
            @RequestParam("resume") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            // Step 1: Extract text from uploaded PDF/DOCX
            String resumeText = resumeParserService.parseResume(resumeFile);

            // Step 2: Parse text into structured data
            Map<String, Object> originalResumeData = resumeTextParserService.parseResumeText(resumeText);

            // Step 3: Analyze keywords
            KeywordMatcher.MatchResult matchResult = keywordMatcher.match(resumeText, jobDescription);
            Map<String, Object> analysisResults = new LinkedHashMap<>();
            analysisResults.put("matchedKeywords", matchResult.getMatchedKeywords());
            analysisResults.put("missingKeywords", matchResult.getMissingKeywords());
            analysisResults.put("matchPercentage", matchResult.getMatchPercentage());

            // Step 4: Enhance the resume
            Map<String, Object> enhancedData = resumeRewriter.enhanceResume(
                    originalResumeData, analysisResults, jobDescription);

            // Step 5: Generate ATS PDF
            boolean isFresher = (boolean) enhancedData.getOrDefault("isFresher", true);
            byte[] pdfBytes = atsPdfGenerator.generateATSResume(enhancedData, isFresher);

            // Step 6: Build filename from candidate name
            Map<String, Object> personalInfo = (Map<String, Object>) enhancedData.get("personalInfo");
            String fullName = "User";
            if (personalInfo != null) {
                Object nameObj = personalInfo.get("fullName");
                if (nameObj != null && !nameObj.toString().isBlank()) {
                    fullName = nameObj.toString().trim();
                }
            }
            String fileName = fullName.replaceAll("\\s+", "_") + "_Enhanced_Resume.pdf";

            // Step 7: Return PDF as downloadable file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment").filename(fileName).build()
            );
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("INVALID_FILE_TYPE", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PROCESSING_ERROR", "Failed to enhance resume: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/resume/test-score
     * Test endpoint: accepts plain text resume + JD (no file upload needed).
     * For development/testing only.
     */
    @PostMapping("/test-score")
    public ResponseEntity<?> testScore(@RequestBody java.util.Map<String, String> body) {
        try {
            String resumeText = body.getOrDefault("resumeText", "");
            String jobDescription = body.getOrDefault("jobDescription", "");
            if (resumeText.isBlank()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("MISSING_INPUT", "resumeText is required"));
            }
            ATSScoreResponse result = atsScoringService.calculateScore(resumeText, jobDescription);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PROCESSING_ERROR", "Test score failed: " + e.getMessage()));
        }
    }
}
