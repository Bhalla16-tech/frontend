package com.kinovek.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@SuppressWarnings("unchecked")
public class ResumeRewriter {

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private ATSContentService atsContentService;

    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> enhanceResume(
            Map<String, Object> originalResumeData,
            Map<String, Object> analysisResults,
            String jobDescription) {

        try {
            // Step 1: Detect industry and experience level
            String industry = atsContentService.detectIndustry(jobDescription);
            boolean isFresher = determineIfFresher(originalResumeData);

            System.out.println("=== RESUME ENHANCEMENT ===");
            System.out.println("Industry: " + industry);
            System.out.println("Fresher: " + isFresher);

            // Step 2: Gather ATS rules from our JSON databases
            List<String> actionVerbs = atsContentService.getAllActionVerbs(industry);
            List<String> bannedPhrases = atsContentService.getBannedBulletStarters();
            List<String> summaryStarters = atsContentService.getSummaryStartWords();
            Map<String, String> indianItemsToRemove = atsContentService.getIndianItemsToRemove();
            List<String> certifications = atsContentService.getCertifications(industry);

            // Step 3: Build AI prompts
            String systemPrompt = buildEnhancementSystemPrompt(
                industry, isFresher, actionVerbs, bannedPhrases,
                summaryStarters, indianItemsToRemove, certifications);

            String userPrompt = buildEnhancementUserPrompt(
                originalResumeData, jobDescription, analysisResults);

            // Step 4: Call Gemini AI
            System.out.println("=== Calling Gemini AI for enhancement... ===");
            String aiResponse = geminiAIService.generateJsonResponse(systemPrompt, userPrompt);
            System.out.println("=== AI Response received ===");

            // Step 5: Parse response
            Map<String, Object> enhancedData = mapper.readValue(aiResponse, Map.class);
            enhancedData.put("isFresher", isFresher);

            System.out.println("=== Enhancement complete ===");
            return enhancedData;

        } catch (Exception e) {
            System.err.println("AI Enhancement failed: " + e.getMessage());
            e.printStackTrace();
            // Fallback: return original data with isFresher flag
            originalResumeData.put("isFresher", true);
            originalResumeData.put("enhancementError", "AI enhancement temporarily unavailable. Original resume data used.");
            return originalResumeData;
        }
    }

    private boolean determineIfFresher(Map<String, Object> resumeData) {
        try {
            Object experience = resumeData.get("experience");
            if (experience == null) return true;
            if (experience instanceof List) {
                List<?> expList = (List<?>) experience;
                if (expList.isEmpty()) return true;
                // If only 1 entry and it looks like an internship, treat as fresher
                if (expList.size() <= 1) {
                    String expStr = expList.toString().toLowerCase();
                    if (expStr.contains("intern") || expStr.contains("trainee") || expStr.contains("apprentice")) {
                        return true;
                    }
                }
                // If 2+ proper job entries, treat as experienced
                return expList.size() < 2;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private String buildEnhancementSystemPrompt(String industry, boolean isFresher,
            List<String> actionVerbs, List<String> bannedPhrases,
            List<String> summaryStarters, Map<String, String> indianItemsToRemove,
            List<String> certifications) {

        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert ATS Resume Writer and Career Coach. Your job is to enhance resumes to maximize ATS compatibility.\n\n");

        sb.append("===== CRITICAL RULES - YOU MUST FOLLOW ALL OF THESE =====\n\n");

        sb.append("RULE 1 - NEVER FABRICATE:\n");
        sb.append("- NEVER invent experience, projects, companies, job titles, or achievements that don't exist in the original resume\n");
        sb.append("- NEVER add fake metrics, percentages, or numbers\n");
        sb.append("- NEVER create fictional internships, certifications, or work history\n");
        sb.append("- Keep ALL real company names, dates, project names, and educational details EXACTLY as they are\n\n");

        sb.append("RULE 2 - WHAT YOU CAN DO:\n");
        sb.append("- Rewrite bullet points to be stronger (start with action verbs, add clarity)\n");
        sb.append("- Reorganize and restructure existing content into proper sections\n");
        sb.append("- Generate a professional summary using the candidate's REAL skills and background\n");
        sb.append("- Add missing keywords from the Job Description to the SKILLS SECTION ONLY\n");
        sb.append("- Fix grammar and spelling in the original content\n");
        sb.append("- Remove unnecessary sections (DOB, declaration, hobbies, etc.)\n");
        sb.append("- Improve the formatting and category organization of skills\n\n");

        sb.append("RULE 3 - INDUSTRY CONTEXT:\n");
        sb.append("Industry: ").append(industry).append("\n");
        sb.append("Experience Level: ").append(isFresher ? "Fresher (0-2 years)" : "Experienced (2+ years)").append("\n\n");

        sb.append("RULE 4 - PROFESSIONAL SUMMARY:\n");
        sb.append("- Write exactly 2-3 sentences, under 60 words\n");
        sb.append("- Must start with one of: ").append(String.join(", ", summaryStarters.subList(0, Math.min(6, summaryStarters.size())))).append("\n");
        sb.append("- NEVER use: 'I', 'me', 'my', 'hardworking individual', 'looking for a challenging role', 'seeking opportunities to grow', 'team player'\n");
        sb.append("- Include the target job role and 2-3 top relevant skills from the candidate's actual background\n\n");

        sb.append("RULE 5 - BULLET POINTS:\n");
        sb.append("- Every bullet MUST start with a strong action verb from this list: ");
        int verbCount = Math.min(20, actionVerbs.size());
        sb.append(String.join(", ", actionVerbs.subList(0, verbCount))).append("\n");
        sb.append("- NEVER start with: ").append(String.join(", ", bannedPhrases)).append("\n");
        sb.append("- Keep each bullet under 120 characters (roughly 1.5 lines)\n");
        sb.append("- Format: [Action Verb] + [What was done] + [Technology/Tool used] + [Impact/Result if available]\n");
        sb.append("- Maximum 4-5 bullets per job entry, 2-3 per project\n\n");

        sb.append("RULE 6 - SKILLS SECTION:\n");
        sb.append("- Organize into categories: Programming Languages, Frameworks, Databases, Tools, Cloud, Methodologies, etc.\n");
        sb.append("- Format: \"CategoryName: Skill1, Skill2, Skill3\"\n");
        sb.append("- Include the candidate's existing skills PLUS missing keywords from the JD\n");
        sb.append("- Place the most relevant skills first in each category\n\n");

        sb.append("RULE 7 - REMOVE THESE ITEMS (Indian resume cleanup):\n");
        for (Map.Entry<String, String> entry : indianItemsToRemove.entrySet()) {
            sb.append("- Remove ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("- Remove any 'Objective' section (replace with Professional Summary)\n");
        sb.append("- Remove 'References available upon request'\n\n");

        sb.append("RULE 8 - SECTION ORDER IN OUTPUT:\n");
        if (isFresher) {
            sb.append("Fresher order: Summary -> Education -> Skills -> Projects -> Experience (if any) -> Certifications -> Achievements\n");
        } else {
            sb.append("Experienced order: Summary -> Skills -> Experience -> Projects -> Education -> Certifications -> Achievements\n");
        }
        sb.append("Skip any section that has no content.\n\n");

        sb.append("RULE 9 - OUTPUT FORMAT:\n");
        sb.append("Respond with ONLY a valid JSON object. No markdown, no explanation, no text before or after the JSON.\n");
        sb.append("Use this EXACT structure:\n");
        sb.append("{\n");
        sb.append("  \"personalInfo\": {\n");
        sb.append("    \"fullName\": \"Candidate's actual name\",\n");
        sb.append("    \"email\": \"actual@email.com\",\n");
        sb.append("    \"phone\": \"+91-XXXXX-XXXXX\",\n");
        sb.append("    \"linkedin\": \"linkedin.com/in/xxx (if available, empty string if not)\",\n");
        sb.append("    \"location\": \"City, State\"\n");
        sb.append("  },\n");
        sb.append("  \"summary\": \"2-3 sentence professional summary\",\n");
        sb.append("  \"education\": [\n");
        sb.append("    {\n");
        sb.append("      \"degree\": \"Full degree name (e.g., Bachelor of Technology in Computer Science)\",\n");
        sb.append("      \"institution\": \"University/College name\",\n");
        sb.append("      \"year\": \"Start Year - End Year\",\n");
        sb.append("      \"score\": \"CGPA: X.X/10 or XX%\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"skills\": {\n");
        sb.append("    \"Programming Languages\": \"Java, Python, JavaScript\",\n");
        sb.append("    \"Frameworks\": \"Spring Boot, React\",\n");
        sb.append("    \"Databases\": \"MySQL, MongoDB\",\n");
        sb.append("    \"Tools & Platforms\": \"Git, Docker, VS Code\"\n");
        sb.append("  },\n");
        sb.append("  \"experience\": [\n");
        sb.append("    {\n");
        sb.append("      \"title\": \"Actual Job Title\",\n");
        sb.append("      \"company\": \"Actual Company Name\",\n");
        sb.append("      \"location\": \"City\",\n");
        sb.append("      \"dates\": \"Month Year - Month Year\",\n");
        sb.append("      \"bullets\": [\n");
        sb.append("        \"Enhanced bullet starting with action verb\",\n");
        sb.append("        \"Another enhanced bullet\"\n");
        sb.append("      ]\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"projects\": [\n");
        sb.append("    {\n");
        sb.append("      \"name\": \"Actual Project Name\",\n");
        sb.append("      \"techStack\": \"Technologies actually used\",\n");
        sb.append("      \"bullets\": [\n");
        sb.append("        \"Enhanced project bullet\",\n");
        sb.append("        \"Another enhanced bullet\"\n");
        sb.append("      ]\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"certifications\": [\"Only real certifications the candidate has\"],\n");
        sb.append("  \"achievements\": [\"Only real achievements from the resume\"]\n");
        sb.append("}\n\n");

        sb.append("REMEMBER: Return ONLY the JSON. No other text. Do NOT fabricate any information.\n");

        return sb.toString();
    }

    private String buildEnhancementUserPrompt(Map<String, Object> originalResumeData,
            String jobDescription, Map<String, Object> analysisResults) {

        StringBuilder sb = new StringBuilder();
        sb.append("TASK: Enhance this resume to be ATS-optimized for the given Job Description.\n\n");

        sb.append("=== ORIGINAL RESUME DATA ===\n");
        try {
            sb.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(originalResumeData));
        } catch (Exception e) {
            sb.append(originalResumeData.toString());
        }
        sb.append("\n\n");

        sb.append("=== TARGET JOB DESCRIPTION ===\n");
        sb.append(jobDescription);
        sb.append("\n\n");

        if (analysisResults != null) {
            sb.append("=== KEYWORD ANALYSIS ===\n");
            if (analysisResults.containsKey("matchedKeywords")) {
                sb.append("Already matched keywords: ").append(analysisResults.get("matchedKeywords")).append("\n");
            }
            if (analysisResults.containsKey("missingKeywords")) {
                sb.append("Missing keywords (ADD these to skills section): ").append(analysisResults.get("missingKeywords")).append("\n");
            }
            sb.append("\n");
        }

        sb.append("INSTRUCTIONS:\n");
        sb.append("1. Keep ALL real information (name, education, companies, dates) exactly as is\n");
        sb.append("2. Rewrite bullet points to start with strong action verbs\n");
        sb.append("3. Create a professional summary based on REAL background\n");
        sb.append("4. Add missing keywords to the skills section only\n");
        sb.append("5. Remove: DOB, father's name, declaration, hobbies, signature, photo references\n");
        sb.append("6. Organize skills into proper categories\n");
        sb.append("7. Respond with ONLY the JSON object\n");

        return sb.toString();
    }
}
