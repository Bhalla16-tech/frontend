import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { Sparkles, ArrowRight, CheckCircle, Loader2, FileText, XCircle, RotateCcw, PartyPopper } from "lucide-react";
import { useState, useRef } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";

interface EnhancementResult {
  atsScore: number;
  matchedKeywords: string[];
  missingKeywords: string[];
  suggestions: string[];
}

const ResumeEnhancer = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [isEnhancing, setIsEnhancing] = useState(false);
  const [enhancementResult, setEnhancementResult] = useState<EnhancementResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const resultsRef = useRef<HTMLDivElement>(null);

  const {
    file,
    fileError,
    isDragging,
    inputRef,
    handleDrop,
    handleDragOver,
    handleDragLeave,
    handleClick,
    handleInputChange,
    clearFile,
    acceptedTypes,
    maxSizeMB,
  } = useFileUpload();

  const handleEnhance = async () => {
    if (!file) {
      toast.error("Please upload your resume first");
      return;
    }

    try {
      setIsEnhancing(true);
      setErrorMessage(null);
      setSuccessMessage(null);
      setEnhancementResult(null);

      const formData = new FormData();
      formData.append('resume', file);
      formData.append('jobDescription', jobDescription);

      const response = await fetch('http://localhost:8080/api/resume/enhance', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Enhancement failed');
      }

      // Download the PDF
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'Enhanced_ATS_Resume.pdf';
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      setIsEnhancing(false);
      setSuccessMessage('Your ATS-optimized resume has been downloaded!');
      toast.success('Resume enhanced and downloaded!');
    } catch (err) {
      setIsEnhancing(false);
      setErrorMessage('Failed to enhance resume. Please try again.');
      toast.error('Failed to enhance resume. Please try again.');
      console.error(err);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-500";
    if (score >= 60) return "text-yellow-500";
    return "text-destructive";
  };

  const getScoreBorderColor = (score: number) => {
    if (score >= 80) return "border-green-500/50";
    if (score >= 60) return "border-yellow-500/50";
    return "border-destructive/50";
  };

  const getScoreLabel = (score: number) => {
    if (score >= 90) return "Excellent";
    if (score >= 80) return "Great";
    if (score >= 60) return "Good";
    if (score >= 40) return "Needs Work";
    return "Low";
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
                  fileError={fileError}
                  isDragging={isDragging}
                  inputRef={inputRef}
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onClick={handleClick}
                  onInputChange={handleInputChange}
                  onClear={clearFile}
                  acceptedTypes={acceptedTypes}
                  maxSizeMB={maxSizeMB}
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
                    disabled={isEnhancing}
                  />
                </div>

                <Button 
                  variant="gradient" 
                  size="xl" 
                  className="w-full"
                  onClick={handleEnhance}
                  disabled={isEnhancing || !file}
                >
                  {isEnhancing ? (
                    <>
                      <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                      Analyzing your resume...
                    </>
                  ) : (
                    <>
                      Enhance My Resume
                      <ArrowRight className="ml-2 h-5 w-5" />
                    </>
                  )}
                </Button>
              </div>

              {/* Processing Overlay */}
              {isEnhancing && (
                <div className="mt-6 p-6 rounded-xl border border-accent/30 bg-accent/5 animate-pulse">
                  <div className="flex items-center gap-4">
                    <Loader2 className="w-8 h-8 text-accent animate-spin flex-shrink-0" />
                    <div>
                      <p className="font-medium text-foreground">Processing your resume...</p>
                      <p className="text-sm text-muted-foreground mt-1">Parsing content, matching keywords, and generating suggestions</p>
                    </div>
                  </div>
                </div>
              )}

              {/* Success Banner */}
              {successMessage && !isEnhancing && (
                <div className="mt-6 p-5 rounded-xl border border-green-500/30 bg-green-500/5">
                  <div className="flex items-center gap-3">
                    <PartyPopper className="w-5 h-5 text-green-500 flex-shrink-0" />
                    <p className="font-medium text-green-400">{successMessage}</p>
                  </div>
                </div>
              )}

              {/* Error Banner */}
              {errorMessage && !isEnhancing && (
                <div className="mt-6 p-5 rounded-xl border border-destructive/30 bg-destructive/5">
                  <div className="flex items-start gap-3">
                    <XCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                    <div className="flex-1">
                      <p className="font-medium text-destructive">Analysis Failed</p>
                      <p className="text-sm text-muted-foreground mt-1">{errorMessage}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={handleEnhance} className="flex-shrink-0">
                      <RotateCcw className="w-4 h-4 mr-1" /> Retry
                    </Button>
                  </div>
                </div>
              )}
            </div>

            {/* Enhancement Results Section */}
            {enhancementResult && (
              <div ref={resultsRef} className="mt-12 space-y-8 animate-in fade-in-50 slide-in-from-bottom-4 duration-500">
                {/* ATS Score */}
                <div className="card-elevated p-8 text-center">
                  <h2 className="text-2xl font-bold text-white mb-6">
                    Your ATS <span className="gradient-gold-text">Score</span>
                  </h2>
                  <div className={`w-36 h-36 rounded-full border-4 ${getScoreBorderColor(enhancementResult.atsScore)} flex items-center justify-center mx-auto bg-accent/10 mb-4`}>
                    <span className={`text-5xl font-bold ${getScoreColor(enhancementResult.atsScore)}`}>{enhancementResult.atsScore}%</span>
                  </div>
                  <p className={`text-lg font-semibold ${getScoreColor(enhancementResult.atsScore)}`}>
                    {getScoreLabel(enhancementResult.atsScore)}
                  </p>
                  <p className="text-sm text-muted-foreground mt-2">
                    {enhancementResult.atsScore >= 80 
                      ? "Your resume is well-aligned with this job description."
                      : enhancementResult.atsScore >= 60 
                      ? "Good foundation — add the missing keywords below to boost your score."
                      : "Your resume needs more alignment with the job description."}
                  </p>
                </div>

                {/* Keywords */}
                <div className="grid lg:grid-cols-2 gap-8">
                  {/* Matched Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-green-500/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-green-500/10 flex items-center justify-center">
                        <CheckCircle className="w-5 h-5 text-green-500" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Matched Keywords</h3>
                        <p className="text-sm text-muted-foreground">{enhancementResult.matchedKeywords.length} found</p>
                      </div>
                    </div>
                    {enhancementResult.matchedKeywords.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {enhancementResult.matchedKeywords.map((kw, i) => (
                          <span key={i} className="px-3 py-1 rounded-full bg-green-500/10 border border-green-500/30 text-green-400 text-sm">
                            {kw}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground italic">No matching keywords found. Try adding a job description for comparison.</p>
                    )}
                  </div>

                  {/* Missing Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-destructive/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-destructive/10 flex items-center justify-center">
                        <FileText className="w-5 h-5 text-destructive" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Missing Keywords</h3>
                        <p className="text-sm text-muted-foreground">{enhancementResult.missingKeywords.length} to add</p>
                      </div>
                    </div>
                    {enhancementResult.missingKeywords.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {enhancementResult.missingKeywords.map((kw, i) => (
                          <span key={i} className="px-3 py-1 rounded-full bg-destructive/10 border border-destructive/30 text-red-400 text-sm">
                            {kw}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <div className="flex items-center gap-2 text-green-400">
                        <PartyPopper className="w-5 h-5" />
                        <p className="text-sm font-medium">All keywords matched! Great job.</p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Suggestions */}
                <div className="card-elevated p-8">
                  <h3 className="text-xl font-semibold text-white mb-6 text-center">
                    Improvement <span className="gradient-gold-text">Suggestions</span>
                  </h3>
                  {enhancementResult.suggestions.length > 0 ? (
                    <div className="grid sm:grid-cols-2 gap-4">
                      {enhancementResult.suggestions.map((suggestion, index) => (
                        <div key={index} className="flex items-start gap-3 p-4 bg-accent/5 rounded-xl border border-accent/20">
                          <CheckCircle className="w-5 h-5 text-accent flex-shrink-0 mt-0.5" />
                          <span className="text-base text-foreground">{suggestion}</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-center text-muted-foreground">No additional suggestions — your resume looks great!</p>
                  )}
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