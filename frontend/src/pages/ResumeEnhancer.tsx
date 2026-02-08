import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { Sparkles, ArrowRight, CheckCircle, Loader2, TrendingUp, FileText, Download } from "lucide-react";
import { useState } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";
import { jsPDF } from "jspdf";

interface EnhancementResult {
  scoreBefore: number;
  scoreAfter: number;
  improvements: string[];
  originalHighlights: string[];
  enhancedHighlights: string[];
}

const ResumeEnhancer = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [isEnhancing, setIsEnhancing] = useState(false);
  const [enhancementResult, setEnhancementResult] = useState<EnhancementResult | null>(null);

  const {
    file,
    isDragging,
    inputRef,
    handleDrop,
    handleDragOver,
    handleDragLeave,
    handleClick,
    handleInputChange,
    clearFile,
    acceptedTypes,
  } = useFileUpload();

  const handleEnhance = () => {
    if (!file) {
      toast.error("Please upload your resume first");
      return;
    }

    setIsEnhancing(true);
    setEnhancementResult(null);
    
    // Simulate enhancement with mock results
    setTimeout(() => {
      setIsEnhancing(false);
      setEnhancementResult({
        scoreBefore: 58,
        scoreAfter: 92,
        improvements: [
          "Added 12 relevant keywords from job description",
          "Reformatted bullet points with quantifiable achievements",
          "Improved ATS-compatible formatting structure",
          "Enhanced professional summary with impact statements",
          "Optimized skills section with industry-standard terms",
        ],
        originalHighlights: [
          "Worked on various projects",
          "Helped team with tasks",
          "Responsible for customer service",
          "Used multiple software tools",
        ],
        enhancedHighlights: [
          "Led 5+ cross-functional projects, delivering 20% ahead of schedule",
          "Collaborated with 8-member team to increase productivity by 35%",
          "Resolved 150+ customer inquiries monthly with 98% satisfaction rate",
          "Proficient in React, TypeScript, Python, and AWS cloud services",
        ],
      });
      toast.success("Resume enhanced successfully! Your optimized resume is ready.");
    }, 2500);
  };

  const handleDownloadPDF = () => {
    if (!enhancementResult) return;

    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    
    // Header
    doc.setFontSize(24);
    doc.setTextColor(40, 40, 40);
    doc.text("ENHANCED RESUME", pageWidth / 2, 25, { align: "center" });
    
    // Divider line
    doc.setDrawColor(212, 175, 55);
    doc.setLineWidth(0.5);
    doc.line(20, 32, pageWidth - 20, 32);
    
    // ATS Score
    doc.setFontSize(14);
    doc.setTextColor(100, 100, 100);
    doc.text(`ATS Score: ${enhancementResult.scoreAfter}/100`, pageWidth / 2, 42, { align: "center" });
    
    // Professional Summary Section
    doc.setFontSize(16);
    doc.setTextColor(40, 40, 40);
    doc.text("PROFESSIONAL SUMMARY", 20, 58);
    
    doc.setFontSize(11);
    doc.setTextColor(60, 60, 60);
    const summaryText = "Results-driven professional with proven track record of delivering high-impact solutions. Skilled in leading cross-functional teams, optimizing processes, and driving measurable business outcomes.";
    const summaryLines = doc.splitTextToSize(summaryText, pageWidth - 40);
    doc.text(summaryLines, 20, 68);
    
    // Experience Section
    doc.setFontSize(16);
    doc.setTextColor(40, 40, 40);
    doc.text("EXPERIENCE HIGHLIGHTS", 20, 92);
    
    doc.setFontSize(11);
    doc.setTextColor(60, 60, 60);
    let yPosition = 102;
    enhancementResult.enhancedHighlights.forEach((highlight) => {
      const bulletPoint = `• ${highlight}`;
      const lines = doc.splitTextToSize(bulletPoint, pageWidth - 45);
      doc.text(lines, 25, yPosition);
      yPosition += lines.length * 7 + 3;
    });
    
    // Skills Section
    yPosition += 5;
    doc.setFontSize(16);
    doc.setTextColor(40, 40, 40);
    doc.text("KEY SKILLS", 20, yPosition);
    
    yPosition += 10;
    doc.setFontSize(11);
    doc.setTextColor(60, 60, 60);
    const skills = "React • TypeScript • JavaScript • Python • AWS • Docker • Agile • Scrum • CI/CD • REST APIs • Team Leadership • Project Management";
    const skillLines = doc.splitTextToSize(skills, pageWidth - 40);
    doc.text(skillLines, 20, yPosition);
    
    // Improvements Made Section
    yPosition += 25;
    doc.setFontSize(16);
    doc.setTextColor(40, 40, 40);
    doc.text("ENHANCEMENTS APPLIED", 20, yPosition);
    
    yPosition += 10;
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    enhancementResult.improvements.forEach((improvement) => {
      const bulletPoint = `✓ ${improvement}`;
      const lines = doc.splitTextToSize(bulletPoint, pageWidth - 45);
      doc.text(lines, 25, yPosition);
      yPosition += lines.length * 6 + 2;
    });
    
    // Footer
    doc.setFontSize(9);
    doc.setTextColor(150, 150, 150);
    doc.text("Generated by Kinovek Resume Enhancer", pageWidth / 2, 285, { align: "center" });
    
    // Save the PDF
    doc.save("enhanced-resume.pdf");
    toast.success("Enhanced resume downloaded successfully!");
  };

  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        {/* Hero */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide">
            <div className="max-w-3xl mx-auto text-center">
              <h1 className="text-4xl sm:text-5xl font-bold mb-5">
                <span className="text-white">Resume </span>
                <span className="gradient-gold-text">Enhancer</span>
              </h1>
              <p className="text-lg text-muted-foreground">
                Transform your resume with ATS-safe formatting, impactful bullet points, 
                and the right keywords—while keeping it authentically you.
              </p>
            </div>
          </div>
        </section>

        {/* Upload Section */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-3xl">
            <div className="card-elevated p-10">
              <div className="text-center mb-8">
                <div className="w-16 h-16 rounded-2xl gradient-teal flex items-center justify-center mx-auto mb-5">
                  <Sparkles className="w-8 h-8 text-white" />
                </div>
                <h2 className="text-2xl font-bold text-foreground mb-3">
                  Enhance your resume
                </h2>
                <p className="text-muted-foreground">
                  Upload your current resume and optionally add a job description for targeted optimization
                </p>
              </div>

              <div className="space-y-6">
                {/* Resume Upload */}
                <FileUploadZone
                  file={file}
                  isDragging={isDragging}
                  inputRef={inputRef}
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onClick={handleClick}
                  onInputChange={handleInputChange}
                  onClear={clearFile}
                  acceptedTypes={acceptedTypes}
                />

                {/* Job Description (Optional) */}
                <div>
                  <label className="block text-sm font-medium text-foreground mb-3">
                    Job Description (optional)
                  </label>
                  <textarea 
                    value={jobDescription}
                    onChange={(e) => setJobDescription(e.target.value)}
                    placeholder="Paste the job description to get targeted enhancements..."
                    className="w-full h-32 p-4 rounded-xl border border-border bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-accent/50 transition-all"
                  />
                </div>

                <Button 
                  variant="gradient" 
                  size="xl" 
                  className="w-full"
                  onClick={handleEnhance}
                  disabled={isEnhancing}
                >
                  {isEnhancing ? (
                    <>
                      <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                      Enhancing...
                    </>
                  ) : (
                    <>
                      Enhance My Resume
                      <ArrowRight className="ml-2 h-5 w-5" />
                    </>
                  )}
                </Button>
              </div>
            </div>

            {/* Enhancement Results Section */}
            {enhancementResult && (
              <div className="mt-12 space-y-8 animate-in fade-in-50 duration-500">
                {/* Score Comparison */}
                <div className="card-elevated p-8">
                  <h2 className="text-2xl font-bold text-white text-center mb-8">
                    ATS Score <span className="gradient-gold-text">Improvement</span>
                  </h2>
                  
                  <div className="grid md:grid-cols-3 gap-6 items-center">
                    {/* Before Score */}
                    <div className="text-center">
                      <p className="text-lg text-muted-foreground mb-3">Before</p>
                      <div className="w-28 h-28 rounded-full border-4 border-destructive/30 flex items-center justify-center mx-auto bg-destructive/5">
                        <span className="text-4xl font-bold text-destructive">{enhancementResult.scoreBefore}%</span>
                      </div>
                    </div>

                    {/* Arrow */}
                    <div className="flex justify-center">
                      <div className="flex items-center gap-2">
                        <TrendingUp className="w-8 h-8 text-accent" />
                        <span className="text-2xl font-bold gradient-gold-text">
                          +{enhancementResult.scoreAfter - enhancementResult.scoreBefore}%
                        </span>
                      </div>
                    </div>

                    {/* After Score */}
                    <div className="text-center">
                      <p className="text-lg text-muted-foreground mb-3">After</p>
                      <div className="w-28 h-28 rounded-full border-4 border-accent/50 flex items-center justify-center mx-auto bg-accent/10">
                        <span className="text-4xl font-bold gradient-gold-text">{enhancementResult.scoreAfter}%</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Resume Comparison */}
                <div className="grid lg:grid-cols-2 gap-8">
                  {/* Original Resume */}
                  <div className="card-elevated p-8 border-l-4 border-l-destructive/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-destructive/10 flex items-center justify-center">
                        <FileText className="w-5 h-5 text-destructive" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Original Resume</h3>
                        <p className="text-sm text-muted-foreground">What you uploaded</p>
                      </div>
                    </div>
                    <div className="space-y-3">
                      {enhancementResult.originalHighlights.map((item, index) => (
                        <div 
                          key={index}
                          className="p-3 rounded-lg bg-destructive/5 border border-destructive/20 text-base text-muted-foreground"
                        >
                          "{item}"
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Enhanced Resume */}
                  <div className="card-elevated p-8 border-l-4 border-l-accent/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-accent/10 flex items-center justify-center">
                        <Sparkles className="w-5 h-5 text-accent" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Enhanced Resume</h3>
                        <p className="text-sm text-muted-foreground">Optimized version</p>
                      </div>
                    </div>
                    <div className="space-y-3">
                      {enhancementResult.enhancedHighlights.map((item, index) => (
                        <div 
                          key={index}
                          className="p-3 rounded-lg bg-accent/5 border border-accent/20 text-base text-foreground"
                        >
                          "{item}"
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Key Improvements */}
                <div className="card-elevated p-8">
                  <h3 className="text-xl font-semibold text-white mb-6 text-center">
                    Key <span className="gradient-gold-text">Improvements Made</span>
                  </h3>
                  <div className="grid sm:grid-cols-2 gap-4">
                    {enhancementResult.improvements.map((improvement, index) => (
                      <div key={index} className="flex items-start gap-3 p-4 bg-accent/5 rounded-xl border border-accent/20">
                        <CheckCircle className="w-5 h-5 text-accent flex-shrink-0 mt-0.5" />
                        <span className="text-base text-foreground">{improvement}</span>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Download Button */}
                <div className="text-center">
                  <Button variant="gradient" size="xl" onClick={handleDownloadPDF}>
                    <Download className="mr-2 h-5 w-5" />
                    Download Enhanced Resume
                  </Button>
                </div>
              </div>
            )}
          </div>
        </section>

        {/* Benefits */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide max-w-4xl">
            <h2 className="text-2xl font-bold text-foreground text-center mb-10">
              What we enhance
            </h2>
            
            <div className="grid sm:grid-cols-2 gap-5">
              {[
                "ATS-compatible formatting that passes every system",
                "Impact-driven bullet points with clear achievements",
                "Natural keyword integration without stuffing",
                "Professional language that sounds human",
                "Consistent structure and spacing",
                "Clean, scannable sections recruiters prefer",
              ].map((item, i) => (
                <div key={i} className="flex items-start gap-3 p-4 bg-card rounded-xl border border-border">
                  <CheckCircle className="w-5 h-5 text-accent flex-shrink-0 mt-0.5" />
                  <span className="text-foreground">{item}</span>
                </div>
              ))}
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default ResumeEnhancer;