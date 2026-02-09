<p align="center">
  <img src="frontend/public/kinovek-logo.png" alt="Kinovek Logo" width="120" />
</p>

<h1 align="center">Kinovek</h1>

<p align="center">
  <strong>Privacy-First AI Resume Enhancement & ATS Optimization Platform</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#tech-stack">Tech Stack</a> •
  <a href="#getting-started">Getting Started</a> •
  <a href="#api-endpoints">API</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#team">Team</a> •
  <a href="#license">License</a>
</p>

---

## About

Kinovek is a free, fast, and privacy-respecting resume optimization platform that helps job seekers improve their shortlisting chances — without sacrificing personal data. Resumes are processed entirely in-memory and **never stored** on any server.

- **No login required** — upload and get results instantly
- **Zero data storage** — resumes are processed in-memory only
- **Stateless backend** — every request is independent, no sessions
- **Explainable scores** — every result includes a clear breakdown

---

## Features

| Feature | Description |
|---------|-------------|
| **Resume Enhancer** | Upload a resume + paste a job description → get tailored enhancement suggestions, keyword matching, and an ATS compatibility score |
| **ATS Score Checker** | Upload a resume → receive a detailed ATS compatibility score with breakdown by formatting, structure, and content quality |
| **ATS-Friendly Converter** | Upload a resume → get it converted into a clean, ATS-optimized plain-text format |
| **Cover Letter Generator** | Upload a resume + paste a job description → generate a personalized, role-specific cover letter |
| **Job Match Analyzer** | Upload a resume + paste a job description → see your match score with matched & missing keywords highlighted |

### Additional Highlights

- Drag-and-drop file upload (PDF & DOCX, max 5 MB)
- Client-side file type and size validation with inline error messages
- Processing overlays with real-time status
- Color-coded scores (green / yellow / red) with labels
- Error banners with one-click retry
- Downloadable cover letters
- Fully responsive design with dark-mode support
- 3D animated hero section

---

## Tech Stack

### Frontend

| Technology | Purpose |
|------------|---------|
| React 18 | UI library |
| TypeScript | Type safety |
| Vite 5 | Build tool & dev server |
| Tailwind CSS 3 | Utility-first styling |
| Shadcn UI (Radix) | Accessible component primitives |
| Axios | HTTP client (30 s timeout) |
| React Router 6 | Client-side routing |
| React Three Fiber | 3D hero scene |
| Sonner | Toast notifications |
| Lucide React | Icon set |

### Backend

| Technology | Purpose |
|------------|---------|
| Java 21 | Runtime |
| Spring Boot 3.2.3 | REST API framework |
| Apache PDFBox 3.0.1 | PDF parsing |
| Apache POI 5.2.5 | DOCX parsing |
| Maven | Build & dependency management |
| H2 (in-memory) | Lightweight DB (future use) |
| Lombok | Boilerplate reduction |
| Gson | JSON processing |

---

## Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |
| Git | Latest |

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/kinovek.git
cd kinovek
```

### 2. Start the backend

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

The API server will start on **http://localhost:8080**.  
Verify it's running:

```bash
curl http://localhost:8080/api/v1/health
```

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server will start on **http://localhost:5173**.  
Open [http://localhost:5173](http://localhost:5173) in your browser.

### 4. Build for production (optional)

```bash
cd frontend
npm run build
npm run preview   # preview the production build locally
```

---

## API Endpoints

All endpoints are prefixed with `/api/v1`.

| Method | Endpoint | Description | Payload |
|--------|----------|-------------|---------|
| `GET` | `/health` | Health check | — |
| `POST` | `/resume/enhance` | Enhance resume with JD keywords | `file` (multipart) + `jobDescription` (text) |
| `POST` | `/resume/ats-score` | Get ATS compatibility score | `file` (multipart) |
| `POST` | `/resume/ats-convert` | Convert to ATS-friendly format | `file` (multipart) |
| `POST` | `/cover-letter/generate` | Generate a cover letter | `file` (multipart) + `jobDescription` (text) |

---

## Project Structure

```
kinovek/
├── frontend/                # React + TypeScript + Vite
│   ├── src/
│   │   ├── api/             # Axios client & API functions
│   │   ├── components/      # Shared & page-specific components
│   │   ├── hooks/           # Custom React hooks (file upload, etc.)
│   │   ├── pages/           # Route-level page components
│   │   └── lib/             # Utility functions
│   └── public/              # Static assets
│
├── backend/                 # Java + Spring Boot
│   └── src/main/java/com/kinovek/backend/
│       ├── controller/      # REST API endpoints
│       ├── service/         # Business logic
│       ├── model/           # Data models
│       ├── dto/             # Request/Response objects
│       ├── config/          # CORS & web configuration
│       ├── util/            # Parsers & keyword matcher
│       └── exception/       # Global error handling
│
└── README.md
```

---

## Screenshots

> _Screenshots coming soon — add them to a `/docs/screenshots/` folder and reference below._

| Page | Preview |
|------|---------|
| Home | ![Home page](docs/screenshots/home.png) |
| Resume Enhancer | ![Resume Enhancer](docs/screenshots/resume-enhancer.png) |
| ATS Score Checker | ![ATS Score](docs/screenshots/ats-score.png) |
| Cover Letter Generator | ![Cover Letter](docs/screenshots/cover-letter.png) |
| Job Match Analyzer | ![Job Match](docs/screenshots/job-match.png) |

---

## Team

| Name | Role | GitHub |
|------|------|--------|
| _Your Name_ | Full-Stack Developer | [@your-handle](https://github.com/your-handle) |

---

## License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Built with ☕ Java &amp; ⚛️ React — <strong>Your resume, your data, your control.</strong>
</p>
