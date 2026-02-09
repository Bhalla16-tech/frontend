import axios, { AxiosError } from "axios";
import API_BASE_URL from "./config";

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  timeout: 30000, // 30s timeout for file uploads
});

// ==================== Error Helper ====================

/**
 * Extract a user-friendly error message from an Axios error response.
 * Handles: ApiResponse format, Spring error format, network errors, timeouts.
 */
export function extractErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof AxiosError) {
    // Network error — backend is offline or unreachable
    if (error.code === "ERR_NETWORK") {
      return "Unable to connect to the server. Please check if the backend is running.";
    }
    // Request timed out
    if (error.code === "ECONNABORTED") {
      return "The request timed out. Please try again with a smaller file.";
    }
    // Server responded with an error status
    if (error.response?.data) {
      const data = error.response.data;
      // ApiResponse format: { success: false, error: { code, message } }
      if (data.error?.message) return data.error.message;
      // Spring default format: { status, error, message }
      if (typeof data.message === "string") return data.message;
    }
    // 413 — file too large (might not have JSON body)
    if (error.response?.status === 413) {
      return "File is too large. Maximum upload size is 10MB.";
    }
    // 5xx server errors
    if (error.response && error.response.status >= 500) {
      return "Something went wrong on the server. Please try again.";
    }
  }
  if (error instanceof Error) return error.message;
  return fallback;
}

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
