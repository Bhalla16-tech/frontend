package com.kinovek.backend.service;

import com.kinovek.backend.dto.ATSScoreResponse;
import com.kinovek.backend.dto.EnhanceResponse;
import com.kinovek.backend.util.KeywordMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeEnhancerService {

    private final ResumeParserService resumeParserService;
    private final ATSScoringService atsScoringService;
    private final KeywordMatcher keywordMatcher;

    @Autowired
    public ResumeEnhancerService(ResumeParserService resumeParserService,
                                  ATSScoringService atsScoringService,
                                  KeywordMatcher keywordMatcher) {
        this.resumeParserService = resumeParserService;
        this.atsScoringService = atsScoringService;
        this.keywordMatcher = keywordMatcher;
    }

    /**
     * Full resume enhancement: parses resume, matches keywords, scores ATS compatibility,
     * and generates improvement suggestions.
     *
     * @param resumeFile     the uploaded resume file (PDF or DOCX)
     * @param jobDescription the job description text
     * @return EnhanceResponse with score, keywords, suggestions, and section analysis
     * @throws IOException if file parsing fails
     */
    public EnhanceResponse enhanceResume(MultipartFile resumeFile, String jobDescription) throws IOException {
        // 1. Parse the resume
        String resumeText = resumeParserService.parseResume(resumeFile);

        // 2. Match keywords
        KeywordMatcher.MatchResult matchResult = keywordMatcher.match(resumeText, jobDescription);

        // 3. Calculate ATS score
        ATSScoreResponse scoreResponse = atsScoringService.calculateScore(resumeText, jobDescription);

        // 4. Generate suggestions based on missing keywords
        List<String> suggestions = generateSuggestions(matchResult.getMissingKeywords());

        // 5. Build response matching PRD Section 9.1 format
        EnhanceResponse response = new EnhanceResponse();
        response.setSuccess(true);
        response.setAtsScore(scoreResponse.getOverallScore());
        response.setMatchedKeywords(matchResult.getMatchedKeywords());
        response.setMissingKeywords(matchResult.getMissingKeywords());
        response.setSuggestions(suggestions);
        response.setSectionAnalysis(scoreResponse.getSectionBreakdown());

        return response;
    }

    /**
     * Generates actionable suggestions based on missing keywords.
     */
    private List<String> generateSuggestions(List<String> missingKeywords) {
        List<String> suggestions = new ArrayList<>();

        if (missingKeywords.isEmpty()) {
            suggestions.add("Your resume has excellent keyword alignment with the job description");
            return suggestions;
        }

        for (String keyword : missingKeywords) {
            suggestions.add("Add \"" + keyword + "\" to your skills or experience section");
        }

        if (missingKeywords.size() > 3) {
            suggestions.add("Consider tailoring your resume more closely to this specific job description");
        }

        return suggestions;
    }
}
