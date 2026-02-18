package com.kinovek.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class ATSScoreService {

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private ATSContentService atsContentService;

    public Map<String, Object> analyzeResume(String resumeText, String jobDescription) {
        try {
            String industry = atsContentService.detectIndustry(jobDescription);
            Map<String, String> indianItems = atsContentService.getIndianItemsToRemove();
            List<String> bannedPhrases = atsContentService.getBannedBulletStarters();

            String systemPrompt = buildScoringSystemPrompt(industry, indianItems, bannedPhrases);
            String userPrompt = buildScoringUserPrompt(resumeText, jobDescription);

            System.out.println("=== ATS SCORING: Calling Gemini AI ===");
            System.out.println("Industry detected: " + industry);

            String aiResponse = geminiAIService.generateJsonResponse(systemPrompt, userPrompt);

            System.out.println("=== ATS SCORING: AI Response received ===");

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(aiResponse, Map.class);
            result.put("industry", industry);
            return result;

        } catch (Exception e) {
            System.err.println("ATS Scoring failed: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("overallScore", 0);
            fallback.put("error", "AI analysis failed: " + e.getMessage());
            fallback.put("suggestion", "Please try again. If the problem persists, check the API key configuration.");
            return fallback;
        }
    }

    private String buildScoringSystemPrompt(String industry,
            Map<String, String> indianItems, List<String> bannedPhrases) {

        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert ATS (Applicant Tracking System) resume analyzer used by HR professionals.\n");
        sb.append("You evaluate resumes against job descriptions and provide detailed, ACCURATE scoring.\n\n");

        sb.append("You must analyze CAREFULLY and give DIFFERENT scores for different resumes. Never give generic or default scores.\n\n");

        sb.append("SCORING CRITERIA:\n\n");

        sb.append("1. KEYWORD MATCH SCORE (Weight: 40%)\n");
        sb.append("   - Extract ALL required skills, technologies, qualifications, and certifications from the Job Description\n");
        sb.append("   - Check which ones appear in the resume (check exact matches AND common synonyms like JS=JavaScript, ML=Machine Learning)\n");
        sb.append("   - Score formula: (number of matched keywords / total required keywords) * 100\n");
        sb.append("   - List EVERY matched keyword and EVERY missing keyword specifically\n\n");

        sb.append("2. FORMATTING SCORE (Weight: 30%)\n");
        sb.append("   - Check: Standard section headers present? (Education, Experience, Skills - NOT creative names)\n");
        sb.append("   - Check: Appears to be single-column? (multi-column is bad for ATS)\n");
        sb.append("   - Check: Consistent date formats used?\n");
        sb.append("   - Check: Contact info present at top? (email, phone, location)\n");
        sb.append("   - Check: No references to images, tables, or graphics?\n");
        sb.append("   - Check: Professional email address?\n");
        sb.append("   - Deduct points for each formatting issue found\n\n");

        sb.append("3. CONTENT QUALITY SCORE (Weight: 15%)\n");
        sb.append("   - Check: Do experience/project bullets start with strong action verbs?\n");
        sb.append("   - BANNED starting phrases (deduct 5 points per occurrence): ").append(String.join(", ", bannedPhrases)).append("\n");
        sb.append("   - Check: Are achievements quantified with numbers/metrics?\n");
        sb.append("   - Check: Is the professional summary specific and relevant (not generic)?\n");
        sb.append("   - Check: Is content concise without filler words?\n\n");

        sb.append("4. SECTION COMPLETENESS SCORE (Weight: 15%)\n");
        sb.append("   - Required: Contact Info, Professional Summary, Technical Skills, Experience OR Projects, Education\n");
        sb.append("   - Good to have: Certifications, Achievements, Relevant Coursework (for freshers)\n");
        sb.append("   - PENALIZE if these unnecessary sections are present:\n");
        for (Map.Entry<String, String> entry : indianItems.entrySet()) {
            sb.append("     - ").append(entry.getKey()).append(" (").append(entry.getValue()).append(")\n");
        }
        sb.append("\n");

        sb.append("DETECTED INDUSTRY: ").append(industry).append("\n");
        sb.append("Evaluate keywords and skills based on this industry context.\n\n");

        sb.append("IMPORTANT RULES:\n");
        sb.append("- Be SPECIFIC in feedback. Say 'Missing Docker, Kubernetes, AWS' not 'Missing some skills'\n");
        sb.append("- Count EXACT numbers. Say '4 out of 12 required skills matched' not 'some skills matched'\n");
        sb.append("- Each score MUST be justified by specific findings from the actual resume\n");
        sb.append("- Overall score = (keywordMatch.score * 0.40) + (formatting.score * 0.30) + (contentQuality.score * 0.15) + (sectionCompleteness.score * 0.15)\n");
        sb.append("- Round overall score to nearest integer\n\n");

        sb.append("RESPOND WITH ONLY THIS JSON FORMAT (no markdown, no explanation, no text before or after):\n");
        sb.append("{\n");
        sb.append("  \"overallScore\": 58,\n");
        sb.append("  \"breakdown\": {\n");
        sb.append("    \"keywordMatch\": {\n");
        sb.append("      \"score\": 45,\n");
        sb.append("      \"matched\": [\"Java\", \"MySQL\", \"HTML\", \"CSS\"],\n");
        sb.append("      \"missing\": [\"Spring Boot\", \"REST API\", \"Docker\", \"Git\", \"Agile\", \"AWS\", \"Microservices\"],\n");
        sb.append("      \"feedback\": \"Only 4 out of 11 required skills found. Missing critical skills: Spring Boot, REST API, Docker.\"\n");
        sb.append("    },\n");
        sb.append("    \"formatting\": {\n");
        sb.append("      \"score\": 65,\n");
        sb.append("      \"issues\": [\"No professional summary section\", \"Inconsistent date format\", \"Contains photo reference\"],\n");
        sb.append("      \"feedback\": \"Decent structure but missing professional summary and contains photo reference which ATS cannot read.\"\n");
        sb.append("    },\n");
        sb.append("    \"contentQuality\": {\n");
        sb.append("      \"score\": 40,\n");
        sb.append("      \"weakBullets\": [\"Responsible for developing the frontend\", \"Helped in creating the database\", \"Worked on the payment module\"],\n");
        sb.append("      \"feedback\": \"3 out of 6 bullets start with banned phrases. No quantified achievements. Bullets lack impact.\"\n");
        sb.append("    },\n");
        sb.append("    \"sectionCompleteness\": {\n");
        sb.append("      \"score\": 55,\n");
        sb.append("      \"present\": [\"Education\", \"Skills\", \"Projects\", \"Experience\"],\n");
        sb.append("      \"missing\": [\"Professional Summary\", \"Certifications\"],\n");
        sb.append("      \"unnecessary\": [\"Date of Birth\", \"Father's Name\", \"Declaration\", \"Hobbies\"],\n");
        sb.append("      \"feedback\": \"Has core sections but missing Professional Summary. Contains 4 unnecessary sections that should be removed.\"\n");
        sb.append("    }\n");
        sb.append("  },\n");
        sb.append("  \"topIssues\": [\n");
        sb.append("    \"Missing 7 critical keywords from Job Description including Spring Boot, Docker, and AWS\",\n");
        sb.append("    \"No Professional Summary section - this is the first thing ATS and recruiters look for\",\n");
        sb.append("    \"3 bullet points use weak starters like 'Responsible for' and 'Helped'\",\n");
        sb.append("    \"Contains unnecessary sections: DOB, Father's Name, Declaration, Hobbies - remove all\",\n");
        sb.append("    \"No quantified achievements or metrics in any bullet point\"\n");
        sb.append("  ],\n");
        sb.append("  \"suggestions\": [\n");
        sb.append("    \"Add these missing skills to your Technical Skills: Spring Boot, REST API, Docker, Git, Agile, AWS\",\n");
        sb.append("    \"Add a Professional Summary: 'Motivated B.Tech CS graduate with foundation in Java and web development...'\",\n");
        sb.append("    \"Fix bullet: Change 'Responsible for developing frontend' to 'Developed responsive frontend using HTML, CSS, and JavaScript'\",\n");
        sb.append("    \"Remove: Date of Birth, Father's Name, Declaration, Hobbies, and Signature sections\",\n");
        sb.append("    \"Add metrics: 'Built shopping website serving 500+ products with payment integration'\"\n");
        sb.append("  ]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String buildScoringUserPrompt(String resumeText, String jobDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze this resume against the job description. Provide accurate, specific ATS scoring.\n\n");
        sb.append("=== RESUME TEXT ===\n");
        sb.append(resumeText);
        sb.append("\n\n=== JOB DESCRIPTION ===\n");
        sb.append(jobDescription);
        sb.append("\n\n");
        sb.append("INSTRUCTIONS:\n");
        sb.append("- Read EVERY line of the resume carefully\n");
        sb.append("- Extract EVERY skill/keyword from the JD and check against the resume\n");
        sb.append("- Count specific numbers (X out of Y keywords matched)\n");
        sb.append("- Give SPECIFIC feedback with actual text from the resume\n");
        sb.append("- Calculate the overall score using the weighted formula\n");
        sb.append("- Respond with ONLY the JSON object, nothing else\n");
        return sb.toString();
    }
}
