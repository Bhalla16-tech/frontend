# Product Requirements Document (PRD) - Version 2.0

## Product Name
**Kinovek** – Privacy-First AI Resume Enhancement & ATS Optimization Platform

---

## 1. Document Overview

### 1.1 Purpose
This PRD provides a complete, implementation-ready specification for building the Kinovek platform. It is designed for backend developers, AI coding tools (GitHub Copilot, Claude), and system architects.

This document covers:
- Clear backend requirements for Java Spring Boot
- AI-driven resume analysis logic
- Frontend-Backend integration steps (how to connect them)
- Local development setup and folder structure
- API endpoint specifications
- Deployment instructions for going live

### 1.2 Intended Audience
- Backend Engineers (Java / Spring Boot)
- Frontend-Backend Integrators
- AI-assisted IDEs and code generators (GitHub Copilot, Claude)
- Academic evaluators / project reviewers

---

## 2. Problem Definition

### 2.1 Industry Problem
A large percentage of job applications are rejected before human review due to Applicant Tracking Systems (ATS). The most common causes are:
- Resume content not aligned with the Job Description (JD)
- Missing role-specific keywords
- Non-ATS-friendly formatting (tables, columns, graphics)
- Lack of feedback explaining why a resume was rejected

As a result, qualified candidates are filtered out before a human ever sees their resume.

### 2.2 Limitations of Existing Solutions
Current resume tools often:
- Require paid subscriptions for basic features
- Enforce user login and store personal data
- Store sensitive resume data on their servers
- Provide generic or unclear scoring without explanation

---

## 3. Product Vision & Goals

### 3.1 Vision
Kinovek aims to be a free, fast, and privacy-respecting resume optimization platform that helps job seekers improve their shortlisting chances without sacrificing personal data.

### 3.2 Goals
- Improve resume-to-JD alignment using AI keyword matching
- Ensure ATS compliance by converting resumes to ATS-friendly format
- Provide transparent, actionable feedback with clear scores
- Support high user concurrency without performance loss
- Zero data storage - resumes are never saved on any server

---

## 4. Core Product Principles

| Principle | Description |
|-----------|-------------|
| No Authentication | Zero login friction - users upload and get results instantly |
| No Data Storage | Resumes processed in-memory only, never saved to disk or database |
| Stateless Backend | Each API request is independent - no session management needed |
| ATS-First Processing | Every feature is designed around ATS compatibility |
| High Scalability | Java Spring Boot with stateless REST APIs for horizontal scaling |
| Explainable AI | Every score has a clear breakdown showing why |

---

## 5. Local Development Setup Guide

### 5.1 Prerequisites (Install These First)

| Tool | Version | Purpose |
|------|---------|---------|
| Java JDK | 17 or 21 | Required to run Spring Boot |
| Maven | 3.9+ | Build tool for Java project |
| Node.js | 18+ | Required to run React frontend |
| Git | Latest | Version control |
| VS Code | Latest | Code editor |
| MySQL | 8.0+ | Database (optional - project is stateless, but useful for future features) |

### 5.2 VS Code Extensions to Install
- Extension Pack for Java
- Spring Boot Extension Pack
- GitHub Copilot (for AI code assistance)
- ES7+ React/Redux Snippets (for frontend)

### 5.3 Folder Structure (Single Repo - Monorepo)

Create one local folder that contains both frontend and backend:

```
D:\My Kinovek\                              (Root project folder)
│
├── frontend\                                (Cloned from GitHub - Lovable React app)
│   ├── src\
│   │   ├── components\
│   │   ├── pages\
│   │   ├── api\
│   │   │   └── config.js                   (API base URL configuration)
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── public\
│   ├── package.json
│   ├── vite.config.js
│   └── ...
│
├── backend\                                 (Spring Boot project)
│   ├── src\
│   │   ├── main\
│   │   │   ├── java\com\kinovek\backend\
│   │   │   │   ├── controller\              (API endpoints - handles HTTP requests)
│   │   │   │   │   ├── ResumeController.java
│   │   │   │   │   ├── CoverLetterController.java
│   │   │   │   │   └── HealthController.java
│   │   │   │   ├── service\                 (Business logic - processing)
│   │   │   │   │   ├── ResumeEnhancerService.java
│   │   │   │   │   ├── ATSConverterService.java
│   │   │   │   │   ├── ATSScoringService.java
│   │   │   │   │   ├── CoverLetterService.java
│   │   │   │   │   └── ResumeParserService.java
│   │   │   │   ├── model\                   (Data classes)
│   │   │   │   │   ├── ResumeAnalysisResult.java
│   │   │   │   │   ├── ATSScoreResult.java
│   │   │   │   │   └── CoverLetterResult.java
│   │   │   │   ├── dto\                     (Request/Response objects)
│   │   │   │   │   ├── EnhanceRequest.java
│   │   │   │   │   ├── EnhanceResponse.java
│   │   │   │   │   ├── ATSScoreResponse.java
│   │   │   │   │   └── ApiResponse.java
│   │   │   │   ├── config\                  (CORS, Security settings)
│   │   │   │   │   └── WebConfig.java
│   │   │   │   ├── util\                    (Helper functions)
│   │   │   │   │   ├── PDFParser.java
│   │   │   │   │   ├── DOCXParser.java
│   │   │   │   │   └── KeywordMatcher.java
│   │   │   │   └── BackendApplication.java  (Main entry point)
│   │   │   └── resources\
│   │   │       └── application.properties   (Server config, port, etc.)
│   │   └── test\
│   ├── pom.xml                              (Maven dependencies)
│   └── mvnw / mvnw.cmd                     (Maven wrapper)
│
├── .gitignore
└── README.md
```

### 5.4 Setup Steps

1. Create root folder: `mkdir "D:\My Kinovek"` and open it in VS Code
2. Clone frontend: `git clone https://github.com/Bhalla16-tech/KinoVek-Project.git frontend`
3. Go to https://start.spring.io and generate Spring Boot project with dependencies: Spring Web, Lombok, Spring Boot DevTools, Validation
4. Unzip generated project into the `backend\` folder
5. Add extra dependencies to pom.xml (PDFBox, Apache POI - see Section 6.5)
6. Open two terminals in VS Code. Terminal 1: `cd frontend && npm install && npm run dev`. Terminal 2: `cd backend && mvn spring-boot:run`

---

## 6. Technical Architecture

### 6.1 System Architecture Overview

The system follows a client-server model:

```
User Browser --> React Frontend (port 5173) --> Spring Boot Backend (port 8080) --> AI Processing --> Response back to user
```

Since Kinovek is privacy-first, there is no persistent database for user data. All resume processing happens in-memory.

### 6.2 Backend Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| API Style | RESTful APIs (JSON) |
| Build Tool | Maven |
| File Parsing | Apache POI (DOCX), Apache PDFBox (PDF) |
| AI/NLP | Custom keyword matching + optional OpenAI/Gemini API integration |

### 6.3 Frontend Stack
- Framework: React (built with Lovable)
- Build Tool: Vite
- Styling: Tailwind CSS / shadcn-ui
- HTTP Client: Axios (for calling backend APIs)

### 6.4 Backend Responsibilities
- Handle file uploads (resume PDF/DOCX, JD text)
- Parse resume and extract text content
- Parse Job Description and extract keywords
- Run AI/keyword matching algorithms
- Calculate ATS scores
- Generate cover letter text
- Convert resume to ATS-friendly format
- Return JSON responses to frontend

### 6.5 Maven Dependencies (pom.xml)

Add these dependencies inside the dependencies tag in pom.xml:

```xml
<!-- Spring Boot Web (REST APIs) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Input Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- DevTools (auto-reload during development) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok (reduce boilerplate code) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Apache PDFBox (parse PDF files) -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>

<!-- Apache POI (parse DOCX files) -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- Gson (JSON processing) -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

---

## 7. Target Users

### 7.1 Primary Users
- Fresh graduates looking for their first job
- Entry-level professionals switching careers
- Job seekers applying through ATS portals (LinkedIn, Indeed, Naukri, etc.)

### 7.2 Secondary Users
- Placement coordinators at colleges
- Career mentors and counselors
- Resume reviewers and HR consultants

---

## 8. Functional Requirements (Features)

### 8.1 Feature: Resume Enhancer (JD-Based)

**Description:** Enhances a user's resume by comparing it directly with a provided Job Description.

**Inputs:**
- Resume file (PDF or DOCX)
- Job Description (text input or file upload)

**Processing Logic:**
1. Extract text from uploaded resume
2. Parse JD to identify keywords and required skills
3. Perform semantic keyword matching between resume and JD
4. Identify missing skills and matched skills
5. Evaluate overall ATS compatibility

**Outputs (JSON Response):**
- ATS Match Score (0-100)
- List of Missing Keywords and Skills
- List of Matched Keywords
- Content improvement suggestions (what to add/change)

### 8.2 Feature: ATS Resume Converter

**Description:** Transforms a standard resume into an ATS-optimized format.

**Processing Logic:**
- Remove tables, columns, images, and graphics
- Normalize section headers (e.g., "Work Experience" instead of creative headers)
- Convert content to linear top-to-bottom structure
- Standardize bullet formatting

**Output:** Downloadable ATS-compliant resume file (PDF or DOCX)

### 8.3 Feature: ATS Compatibility & Scoring Engine

**Description:** Evaluates how effectively a resume can be parsed and ranked by ATS software.

**Evaluation Criteria:**
- Keyword relevance (how well resume matches typical JD requirements)
- Section completeness (does it have all required sections?)
- Formatting compliance (is it ATS-parseable?)
- Readability score

**Output:** Overall ATS Score + Section-wise detailed analysis

### 8.4 Feature: Cover Letter Generator

**Description:** Generates a tailored, professional cover letter using resume content and JD context.

**Inputs:** Resume + Job Description
**Output:** Editable cover letter text customized to the specific role

---

## 9. API Endpoint Specifications

**Base URL:** `http://localhost:8080/api/v1`

| Method | Endpoint | Description | Input |
|--------|----------|-------------|-------|
| POST | /resume/enhance | Enhance resume against JD | multipart: resume file + JD text |
| POST | /resume/ats-convert | Convert resume to ATS format | multipart: resume file |
| POST | /resume/ats-score | Get ATS compatibility score | multipart: resume file |
| POST | /cover-letter/generate | Generate cover letter | multipart: resume file + JD text |
| GET | /health | Check if backend is running | None |

### 9.1 Example Response Format (Resume Enhance)

```json
{
  "success": true,
  "data": {
    "atsScore": 72,
    "matchedKeywords": ["Java", "Spring Boot", "REST API"],
    "missingKeywords": ["Docker", "Kubernetes", "CI/CD"],
    "suggestions": [
      "Add Docker experience to your skills section",
      "Include CI/CD pipeline experience"
    ],
    "sectionAnalysis": {
      "skills": { "score": 65, "feedback": "Missing 3 key skills" },
      "experience": { "score": 80, "feedback": "Good alignment" },
      "formatting": { "score": 70, "feedback": "Remove table layout" }
    }
  }
}
```

### 9.2 Example Error Response

```json
{
  "success": false,
  "error": {
    "code": "INVALID_FILE_TYPE",
    "message": "Only PDF and DOCX files are supported"
  }
}
```

### 9.3 File Upload Format

All file upload endpoints accept multipart/form-data with these field names:
- `resume` - The resume file (PDF or DOCX, max 5MB)
- `jobDescription` - The job description text (string)

---

## 10. Frontend-Backend Integration Guide

### 10.1 CORS Configuration (Backend Side)

CORS (Cross-Origin Resource Sharing) is required because frontend runs on port 5173 and backend on port 8080. Without this, the browser blocks requests between them.

**Create file:** `backend/src/main/java/com/kinovek/backend/config/WebConfig.java`

```java
package com.kinovek.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173",
                    "http://localhost:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 10.2 API Base URL Configuration (Frontend Side)

**Install Axios:** `npm install axios`

**Create file:** `frontend/src/api/config.js`

```javascript
const API_BASE_URL = "http://localhost:8080/api/v1";
export default API_BASE_URL;
```

**Example API call from frontend:**

```javascript
import axios from 'axios';
import API_BASE_URL from './config';

export const enhanceResume = async (resumeFile, jobDescription) => {
    const formData = new FormData();
    formData.append('resume', resumeFile);
    formData.append('jobDescription', jobDescription);

    const response = await axios.post(`${API_BASE_URL}/resume/enhance`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
};

export const getATSScore = async (resumeFile) => {
    const formData = new FormData();
    formData.append('resume', resumeFile);

    const response = await axios.post(`${API_BASE_URL}/resume/ats-score`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
};

export const convertToATS = async (resumeFile) => {
    const formData = new FormData();
    formData.append('resume', resumeFile);

    const response = await axios.post(`${API_BASE_URL}/resume/ats-convert`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob'
    });
    return response.data;
};

export const generateCoverLetter = async (resumeFile, jobDescription) => {
    const formData = new FormData();
    formData.append('resume', resumeFile);
    formData.append('jobDescription', jobDescription);

    const response = await axios.post(`${API_BASE_URL}/cover-letter/generate`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
};

export const checkHealth = async () => {
    const response = await axios.get(`${API_BASE_URL}/health`);
    return response.data;
};
```

### 10.3 Running Both Together

- **Terminal 1 (Frontend):** `cd frontend && npm run dev` runs on http://localhost:5173
- **Terminal 2 (Backend):** `cd backend && mvn spring-boot:run` runs on http://localhost:8080

**IMPORTANT:** Both terminals must be running at the same time for the website to work.

---

## 11. AI & Processing Logic

### 11.1 Resume Parsing
- Extract structured text from PDF (using PDFBox) and DOCX (using Apache POI)
- Detect logical sections: Summary, Education, Experience, Skills, Certifications
- Handle different formatting styles gracefully

### 11.2 Keyword Matching Engine
- Extract skills and keywords from Job Description
- Map synonyms and role-based variations (e.g., "JS" = "JavaScript")
- Apply weighted relevance scoring (exact match > partial match > synonym match)
- Return match percentage and specific matched/missing terms

### 11.3 ATS Score Formula

**ATS Score = (Keyword Match Score x 0.4) + (Formatting Score x 0.3) + (Section Completeness Score x 0.3)**

| Component | Weight | What It Measures |
|-----------|--------|-----------------|
| Keyword Match | 40% | How many JD keywords appear in resume |
| Formatting | 30% | Is the resume ATS-parseable (no tables, images, etc.) |
| Section Completeness | 30% | Does the resume have all expected sections |

---

## 12. Non-Functional Requirements

### 12.1 Performance
- Average API response time less than 2 seconds
- Efficient handling of concurrent users (multiple requests at same time)
- File upload limit: 5MB per resume

### 12.2 Scalability
- Stateless REST APIs (no server-side sessions)
- Horizontal scalability support (can add more server instances)
- Load balancer compatibility

### 12.3 Security & Privacy
- No database usage for resumes or JDs - everything in-memory
- Ephemeral processing only - data cleared after response is sent
- HTTPS enforcement in production
- File size and input validation on every upload
- Only accept PDF and DOCX file types

### 12.4 Reliability
- Graceful failure handling (meaningful error messages, not crashes)
- Clear JSON error responses with proper HTTP status codes

---

## 13. Deployment Guide

### 13.1 Understanding the Problem

When you run your project locally, the URLs are like http://localhost:5173 and http://localhost:8080. These ONLY work on YOUR computer. Nobody else can access them. To make your website available to everyone on the internet, you need to deploy (host) both frontend and backend on a cloud server.

### 13.2 Deployment Options

| Component | Free Options | Paid Options |
|-----------|-------------|-------------|
| Frontend | Vercel, Netlify, GitHub Pages | AWS S3 + CloudFront, Hostinger |
| Backend | Render.com, Railway.app | AWS EC2, DigitalOcean, Hostinger VPS |

### 13.3 Recommended Free Deployment Path

1. **Frontend:** Deploy on Vercel (free) - connect GitHub repo, auto-deploys on every push
2. **Backend:** Deploy on Render.com (free tier) - supports Java/Spring Boot
3. **Important:** After deployment, update API_BASE_URL in frontend from localhost:8080 to your Render backend URL
4. **Important:** Update CORS config in backend to allow your Vercel frontend URL

---

## 14. User Flow

1. User visits Kinovek website
2. Selects the required feature (Enhance / ATS Convert / Score / Cover Letter)
3. Uploads resume file (PDF or DOCX) and/or pastes Job Description
4. Frontend sends the data to backend API via POST request
5. Backend processes the request (parses files, runs AI matching)
6. Backend sends JSON response back to frontend
7. Frontend displays results (scores, suggestions, downloadable files)
8. User views results or downloads the enhanced/converted resume

---

## 15. Assumptions & Constraints

### 15.1 Assumptions
- Resumes are in readable PDF or DOCX format (not scanned images)
- Job Descriptions contain sufficient detail for keyword extraction
- Users have a modern web browser (Chrome, Firefox, Edge, Safari)

### 15.2 Constraints
- No user authentication system (by design)
- No persistent storage of any user data (by design)
- Free access only - no paid tiers in current version
- File upload limit: 5MB per file
- Supported formats: PDF and DOCX only

---

## 16. Risks & Mitigation

| Risk | Impact | Mitigation Strategy |
|------|--------|-------------------|
| High traffic load | Slow responses | Stateless APIs + horizontal scaling |
| Large file uploads | Server memory issues | 5MB file size limit + validation |
| Resume parsing failures | Empty/wrong results | Fallback logic + clear error messages |
| CORS/connection issues | Frontend can't reach backend | Proper CORS config + health check endpoint |
| Malicious file upload | Security risk | File type validation + size limits + no execution |

---

## 17. Testing Strategy

### 17.1 Manual Testing
- Use Postman or browser to test each API endpoint individually
- Upload different resume formats and verify responses
- Test with various JD lengths and formats

### 17.2 Integration Testing
- Verify frontend can successfully call each backend endpoint
- Test file upload from browser to backend
- Verify CORS is working (no blocked requests in browser console)

### 17.3 Edge Cases to Test
- Empty resume file upload
- Resume with no text (scanned image PDF)
- Very large file (over 5MB)
- Job Description with very few keywords
- Unsupported file format (e.g., .txt, .jpg)
- Simultaneous multiple requests

---

## 18. Future Enhancements (Out of Scope for v1)
- User accounts and login system
- Resume version history and comparison
- Recruiter dashboards
- Multi-language support
- AI-powered interview preparation
- LinkedIn profile optimization
- Bulk resume processing for placement cells

---

## 19. Reference Links
- **Frontend Preview:** https://id-preview--7cbbebd7-858f-4510-9a5f-0987d4a12783.lovable.app/
- **GitHub Repository:** https://github.com/Bhalla16-tech/KinoVek-Project.git
- **Spring Initializr:** https://start.spring.io

---

**End of Document - Kinovek PRD v2.0**