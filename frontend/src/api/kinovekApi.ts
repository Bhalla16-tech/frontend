import axios from "axios";
import API_BASE_URL from "./config";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

// ==================== Health ====================

export async function checkHealth() {
  const response = await api.get("/health");
  return response.data;
}

// ==================== Resume Enhance ====================

export interface EnhanceResponse {
  success: boolean;
  atsScore: number;
  matchedKeywords: string[];
  missingKeywords: string[];
  suggestions: string[];
  sectionAnalysis: Record<string, unknown>;
}

export async function enhanceResume(
  file: File,
  jobDescription: string
): Promise<EnhanceResponse> {
  const formData = new FormData();
  formData.append("resume", file);
  formData.append("jobDescription", jobDescription);

  const response = await api.post<EnhanceResponse>("/resume/enhance", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data;
}

// ==================== ATS Score ====================

export interface ATSScoreResponse {
  success: boolean;
  overallScore: number;
  keywordMatchScore: number;
  formattingScore: number;
  sectionCompletenessScore: number;
  sectionBreakdown: Record<string, unknown>;
}

export async function getATSScore(
  file: File,
  jobDescription = ""
): Promise<ATSScoreResponse> {
  const formData = new FormData();
  formData.append("resume", file);
  formData.append("jobDescription", jobDescription);

  const response = await api.post<ATSScoreResponse>("/resume/ats-score", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return response.data;
}

// ==================== ATS Convert ====================

export async function convertToATSFriendly(file: File): Promise<Blob> {
  const formData = new FormData();
  formData.append("resume", file);

  const response = await api.post("/resume/ats-convert", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    responseType: "blob",
  });
  return response.data;
}

// ==================== Cover Letter ====================

export interface CoverLetterResponse {
  success: boolean;
  data: {
    coverLetterText: string;
    candidateName: string;
    targetRole: string;
    companyName: string;
  };
}

export async function generateCoverLetter(
  file: File,
  jobDescription: string
): Promise<CoverLetterResponse> {
  const formData = new FormData();
  formData.append("resume", file);
  formData.append("jobDescription", jobDescription);

  const response = await api.post<CoverLetterResponse>(
    "/cover-letter/generate",
    formData,
    { headers: { "Content-Type": "multipart/form-data" } }
  );
  return response.data;
}

export default api;
