package com.kinovek.backend.dto;

import java.util.Map;

public class ATSScoreResponse {

    private boolean success;
    private int overallScore;
    private double keywordMatchScore;
    private double formattingScore;
    private double sectionCompletenessScore;
    private Map<String, Object> sectionBreakdown;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public double getKeywordMatchScore() { return keywordMatchScore; }
    public void setKeywordMatchScore(double keywordMatchScore) { this.keywordMatchScore = keywordMatchScore; }

    public double getFormattingScore() { return formattingScore; }
    public void setFormattingScore(double formattingScore) { this.formattingScore = formattingScore; }

    public double getSectionCompletenessScore() { return sectionCompletenessScore; }
    public void setSectionCompletenessScore(double sectionCompletenessScore) { this.sectionCompletenessScore = sectionCompletenessScore; }

    public Map<String, Object> getSectionBreakdown() { return sectionBreakdown; }
    public void setSectionBreakdown(Map<String, Object> sectionBreakdown) { this.sectionBreakdown = sectionBreakdown; }
}
