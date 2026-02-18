package com.kinovek.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoverLetterService {

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private ATSContentService atsContentService;

    public String generateCoverLetter(String resumeText, String jobDescription) {
        try {
            String industry = atsContentService.detectIndustry(jobDescription);

            System.out.println("=== COVER LETTER GENERATION ===");
            System.out.println("Industry: " + industry);

            String systemPrompt = buildCoverLetterSystemPrompt(industry);
            String userPrompt = buildCoverLetterUserPrompt(resumeText, jobDescription);

            String result = geminiAIService.generateResponse(systemPrompt, userPrompt);

            System.out.println("=== Cover Letter generated successfully ===");
            return result;

        } catch (Exception e) {
            System.err.println("Cover Letter generation failed: " + e.getMessage());
            return "Error generating cover letter: " + e.getMessage() + ". Please try again.";
        }
    }

    private String buildCoverLetterSystemPrompt(String industry) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert cover letter writer with 15+ years of recruitment experience.\n");
        sb.append("Generate a professional, tailored cover letter based on the candidate's resume and the job description.\n\n");

        sb.append("STRICT RULES:\n");
        sb.append("1. Keep it 300-400 words maximum (3-4 paragraphs)\n");
        sb.append("2. NEVER fabricate experience, skills, or achievements the candidate doesn't have\n");
        sb.append("3. ONLY reference skills, experience, and projects that are actually in the resume\n");
        sb.append("4. Address specific requirements from the Job Description\n");
        sb.append("5. Highlight the candidate's most relevant experience for THIS specific role\n");
        sb.append("6. Use professional but confident tone - not arrogant, not overly humble\n");
        sb.append("7. Do NOT use cliches like 'I am writing to express my interest' or 'I believe I am the perfect fit'\n");
        sb.append("8. Include the specific job title and company name from the JD\n\n");

        sb.append("INDUSTRY CONTEXT: ").append(industry).append("\n\n");

        sb.append("FORMAT:\n");
        sb.append("[Date]\n\n");
        sb.append("Dear Hiring Manager,\n\n");
        sb.append("[Opening - Mention the specific role and show genuine interest. 2-3 sentences.]\n\n");
        sb.append("[Body 1 - Connect your most relevant experience/projects to the job requirements. Be specific. 3-4 sentences.]\n\n");
        sb.append("[Body 2 - Highlight technical skills that match the JD and mention how you've applied them. 3-4 sentences.]\n\n");
        sb.append("[Closing - Express enthusiasm, mention availability, and include a call to action. 2-3 sentences.]\n\n");
        sb.append("Sincerely,\n");
        sb.append("[Candidate's Full Name]\n");
        sb.append("[Email]\n");
        sb.append("[Phone]\n\n");

        sb.append("Generate the cover letter as plain text. No markdown formatting.\n");

        return sb.toString();
    }

    private String buildCoverLetterUserPrompt(String resumeText, String jobDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("Write a tailored cover letter for this candidate applying to this job.\n\n");
        sb.append("=== CANDIDATE'S RESUME ===\n");
        sb.append(resumeText);
        sb.append("\n\n=== JOB THEY ARE APPLYING TO ===\n");
        sb.append(jobDescription);
        sb.append("\n\n");
        sb.append("Remember: Use ONLY information from the resume. Do NOT invent anything. ");
        sb.append("Make it specific to this exact job posting. Generate the cover letter now.\n");
        return sb.toString();
    }
}
