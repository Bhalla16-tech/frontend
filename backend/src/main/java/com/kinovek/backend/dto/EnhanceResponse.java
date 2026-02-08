package com.kinovek.backend.dto;

import java.util.List;
import java.util.Map;

public class EnhanceResponse {

    private boolean success;
    private int atsScore;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private List<String> suggestions;
    private Map<String, Object> sectionAnalysis;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public List<String> getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(List<String> matchedKeywords) { this.matchedKeywords = matchedKeywords; }

    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public Map<String, Object> getSectionAnalysis() { return sectionAnalysis; }
    public void setSectionAnalysis(Map<String, Object> sectionAnalysis) { this.sectionAnalysis = sectionAnalysis; }
}
