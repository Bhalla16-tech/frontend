package com.kinovek.backend.controller;

import com.kinovek.backend.service.ATSContentService;
import com.kinovek.backend.service.ResumeTextParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * DEBUG ENDPOINTS — Test each service independently.
 * Remove or disable before production deployment.
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private ResumeTextParserService resumeTextParserService;

    @Autowired
    private ATSContentService atsContentService;

    /**
     * Test resume text parsing independently.
     * Usage: POST /api/v1/test/parse  (body = raw resume text)
     */
    @PostMapping("/parse")
    public Map<String, Object> testParse(@RequestBody String text) {
        return resumeTextParserService.parseResumeText(text);
    }

    /**
     * Test industry detection independently.
     * Usage: GET /api/v1/test/industry?jd=Java Developer Spring Boot
     */
    @GetMapping("/industry")
    public Map<String, String> testIndustry(@RequestParam String jd) {
        String industry = atsContentService.detectIndustry(jd);
        return Map.of("jobDescription", jd, "detectedIndustry", industry);
    }
}
