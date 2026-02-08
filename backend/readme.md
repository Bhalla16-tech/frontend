# ==========================================================
# HOW TO USE THIS FILE IN VS CODE WITH GITHUB COPILOT
# ==========================================================
#
# STEP 1: Place this file in your project root folder: D:\My Kinovek\
# STEP 2: Place the Kinovek_PRD_v2.md file also in: D:\My Kinovek\
# STEP 3: Open Copilot Chat in VS Code (Ctrl + Shift + I)
# STEP 4: Copy-paste the prompts below ONE BY ONE
# STEP 5: Wait for Copilot to finish each task before giving next prompt
#
# ==========================================================


# =============================================
# PROMPT 1: FIRST PROMPT (Give this first)
# This makes Copilot understand your full project
# =============================================

Read the file @Kinovek_PRD_v2.md in the project root folder. This is our complete Product Requirements Document. Understand every section carefully. After reading, confirm what you understood about:
1. What is this project about?
2. What is the folder structure?
3. What are the 4 main features?
4. What are the API endpoints?
5. What technologies are we using?

Do not write any code yet. Just confirm your understanding.


# =============================================
# PROMPT 2: SET UP BACKEND STRUCTURE
# Give this after Copilot confirms understanding
# =============================================

Now based on the PRD document you just read, create the complete Spring Boot backend folder structure inside the backend/ folder. Create all the empty Java files mentioned in Section 5.3 of the PRD:

- controller/ResumeController.java
- controller/CoverLetterController.java
- controller/HealthController.java
- service/ResumeEnhancerService.java
- service/ATSConverterService.java
- service/ATSScoringService.java
- service/CoverLetterService.java
- service/ResumeParserService.java
- model/ResumeAnalysisResult.java
- model/ATSScoreResult.java
- model/CoverLetterResult.java
- dto/EnhanceResponse.java
- dto/ATSScoreResponse.java
- dto/ApiResponse.java
- config/WebConfig.java
- util/PDFParser.java
- util/DOCXParser.java
- util/KeywordMatcher.java

Create each file with the correct package name and basic class structure. Do not add full logic yet - just create the skeleton.


# =============================================
# PROMPT 3: ADD MAVEN DEPENDENCIES
# Give this after folder structure is created
# =============================================

Update the pom.xml file in the backend/ folder. Add these dependencies as mentioned in Section 6.5 of the PRD:
- spring-boot-starter-web
- spring-boot-starter-validation
- spring-boot-devtools
- lombok
- Apache PDFBox version 3.0.1
- Apache POI poi-ooxml version 5.2.5
- Gson version 2.10.1

Keep the existing dependencies and just add the new ones.


# =============================================
# PROMPT 4: BUILD CORS CONFIG
# Give this after dependencies are added
# =============================================

Now implement the WebConfig.java file in the config/ folder. As mentioned in Section 10.1 of the PRD, configure CORS to:
- Allow origins: http://localhost:5173 and http://localhost:3000
- Allow methods: GET, POST, PUT, DELETE
- Allow all headers
- Allow credentials
- Map to /api/** path pattern


# =============================================
# PROMPT 5: BUILD HEALTH CHECK API
# Give this to test if backend works
# =============================================

Implement the HealthController.java file. Create a simple GET endpoint at /api/v1/health that returns:
{
  "status": "UP",
  "message": "Kinovek Backend is running",
  "timestamp": "current date and time"
}

This is our first API - we will use it to test if the backend is running and if frontend can connect to it.


# =============================================
# PROMPT 6: BUILD RESUME PARSER
# Give this after health check works
# =============================================

Now implement the ResumeParserService.java and the utility classes PDFParser.java and DOCXParser.java as mentioned in Section 11.1 of the PRD:

PDFParser.java should:
- Accept a PDF file as input
- Use Apache PDFBox to extract all text from the PDF
- Return the extracted text as a String

DOCXParser.java should:
- Accept a DOCX file as input
- Use Apache POI to extract all text from the DOCX
- Return the extracted text as a String

ResumeParserService.java should:
- Accept a MultipartFile
- Check if it is PDF or DOCX
- Call the appropriate parser
- Return the extracted text
- Throw an error if file type is not supported


# =============================================
# PROMPT 7: BUILD KEYWORD MATCHER
# Give this after resume parser is done
# =============================================

Implement the KeywordMatcher.java utility class as mentioned in Section 11.2 of the PRD. It should:
- Accept resume text and job description text as inputs
- Extract keywords from the job description
- Compare keywords against resume text
- Support basic synonym matching (e.g., "JS" = "JavaScript", "React.js" = "React")
- Return: matched keywords list, missing keywords list, and match percentage
- Use case-insensitive matching


# =============================================
# PROMPT 8: BUILD ATS SCORING ENGINE
# Give this after keyword matcher is done
# =============================================

Implement the ATSScoringService.java as mentioned in Section 11.3 of the PRD. The ATS Score formula is:

ATS Score = (Keyword Match Score x 0.4) + (Formatting Score x 0.3) + (Section Completeness Score x 0.3)

The service should:
- Use KeywordMatcher to get keyword match score
- Check formatting (detect tables, images, columns - penalize if found)
- Check section completeness (look for: Summary, Education, Experience, Skills, Certifications)
- Return overall score (0-100) and section-wise breakdown

Also implement the ATSScoreResponse.java DTO with the response format from Section 9.1 of the PRD.


# =============================================
# PROMPT 9: BUILD RESUME ENHANCE API
# Give this after scoring engine is done
# =============================================

Now implement the full Resume Enhance feature. This is the main feature of Kinovek (Section 8.1 of PRD).

Implement:
1. ResumeEnhancerService.java - combines parser + keyword matcher + scorer to produce full analysis
2. ResumeController.java - POST endpoint at /api/v1/resume/enhance

The endpoint should:
- Accept multipart form data with 'resume' file and 'jobDescription' text
- Parse the resume
- Match keywords against JD
- Calculate ATS score
- Return JSON response matching the format in Section 9.1 of the PRD

Also implement the POST /api/v1/resume/ats-score endpoint in the same controller.


# =============================================
# PROMPT 10: BUILD ATS CONVERTER
# Give this after resume enhance is done
# =============================================

Implement the ATS Resume Converter feature (Section 8.2 of PRD).

Implement:
1. ATSConverterService.java - converts resume to ATS-friendly format
2. POST endpoint at /api/v1/resume/ats-convert in ResumeController.java

The converter should:
- Parse the uploaded resume
- Remove tables, columns, images
- Normalize section headers
- Convert to linear structure
- Generate a clean ATS-compliant PDF or DOCX file
- Return the file as a downloadable response


# =============================================
# PROMPT 11: BUILD COVER LETTER GENERATOR
# Give this after ATS converter is done
# =============================================

Implement the Cover Letter Generator feature (Section 8.4 of PRD).

Implement:
1. CoverLetterService.java - generates cover letter from resume + JD
2. CoverLetterController.java - POST endpoint at /api/v1/cover-letter/generate

The generator should:
- Parse the resume to extract name, skills, experience
- Parse the JD to extract company name, role, requirements
- Generate a professional cover letter template
- Fill in the template with extracted information
- Return the cover letter text in JSON response


# =============================================
# PROMPT 12: BUILD FRONTEND API CONNECTION
# Give this after all backend APIs are done
# =============================================

Now connect the frontend to the backend. In the frontend/ folder:

1. Install axios: npm install axios
2. Create frontend/src/api/config.js with API_BASE_URL = "http://localhost:8080/api/v1"
3. Create frontend/src/api/kinovekApi.js with all API call functions as shown in Section 10.2 of the PRD
4. Update the existing frontend components to call these APIs when user uploads a resume or clicks analyze

Make sure the frontend sends files as multipart/form-data and handles the JSON responses correctly.


# =============================================
# PROMPT 13: TEST EVERYTHING
# Give this after frontend connection is done
# =============================================




# =============================================
# FINAL NOTE
# =============================================
# After all 13 prompts are done, your project should be fully working locally.
# Next step would be deployment (Section 13 of PRD).
# You can ask Copilot: "Help me deploy this project. Frontend to Vercel, Backend to Render.com"