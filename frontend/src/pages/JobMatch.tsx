import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { FileText, ArrowRight, Loader2, AlertTriangle, AlertCircle, CheckCircle2, XCircle, RotateCcw, PartyPopper } from "lucide-react";
import { useState, useRef } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";
import { enhanceResume, extractErrorMessage } from "@/api/kinovekApi";

interface AnalysisResult {
  matchScore: number;
  matchedKeywords: string[];
  missingKeywords: string[];
}

const JobMatch = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const resultsRef = useRef<HTMLDivElement>(null);
  
  const [jdError, setJdError] = useState<string | null>(null);

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

  const handleAnalyze = async () => {
    if (!file) {
      toast.error("Please upload your resume first");
      return;
    }
    if (!jobDescription.trim()) {
      setJdError("Please paste the job description before analyzing");
      toast.error("Please paste the job description");
      return;
    }

    setJdError(null);

    setIsAnalyzing(true);
    setAnalysisResult(null);
    setErrorMessage(null);

    try {
      const result = await enhanceResume(file, jobDescription);
      setAnalysisResult({
        matchScore: result.atsScore,
        matchedKeywords: result.matchedKeywords,
        missingKeywords: result.missingKeywords,
      });
      toast.success("Analysis complete! Your match score is ready.");
      setTimeout(() => resultsRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 100);
    } catch (error: unknown) {
      const msg = extractErrorMessage(error, "Failed to analyze resume. Please try again.");
      setErrorMessage(msg);
      toast.error(msg);
    } finally {
      setIsAnalyzing(false);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-500";
    if (score >= 60) return "text-yellow-500";
    return "text-destructive";
  };

  const getScoreBorderColor = (score: number) => {
    if (score >= 80) return "border-green-500/30";
    if (score >= 60) return "border-yellow-500/30";
    return "border-destructive/30";
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
                <span className="text-white">Instant </span>
                <span className="gradient-gold-text">Job Match</span>
              </h1>
              <p className="text-lg text-muted-foreground">
                See exactly how your resume aligns with any job description. 
                Understand your match score, identified keywords, and gaps to address.
              </p>
            </div>
          </div>
        </section>

        {/* Main Tool Section */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-4xl">
            <div className="grid lg:grid-cols-2 gap-8">
              {/* Upload Resume */}
              <div className="card-elevated p-8">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-lg bg-accent/10 flex items-center justify-center">
                    <FileText className="w-5 h-5 text-accent" />
                  </div>
                  <h3 className="text-lg font-semibold text-foreground">Your Resume</h3>
                </div>
                
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
              </div>

              {/* Job Description */}
              <div className="card-elevated p-8">
                <div className="flex items-center gap-3 mb-6">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
                    <FileText className="w-5 h-5 text-primary" />
                  </div>
                  <h3 className="text-lg font-semibold text-foreground">Job Description</h3>
                </div>
                
                <textarea 
                  value={jobDescription}
                  onChange={(e) => { setJobDescription(e.target.value); if (e.target.value.trim()) setJdError(null); }}
                  placeholder="Paste the job description here..."
                  className={`w-full h-48 p-4 rounded-xl border bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-accent/50 transition-all ${jdError ? 'border-destructive/50 bg-destructive/5' : 'border-border'}`}
                  disabled={isAnalyzing}
                />
                {jdError && (
                  <p className="mt-2 text-sm text-destructive flex items-center gap-1.5">
                    <AlertCircle className="h-4 w-4 flex-shrink-0" />
                    {jdError}
                  </p>
                )}
              </div>
            </div>

            {/* Analyze Button */}
            <div className="mt-8 text-center">
              <Button 
                variant="gradient" 
                size="xl" 
                onClick={handleAnalyze}
                disabled={isAnalyzing || !file || !jobDescription.trim()}
              >
                {isAnalyzing ? (
                  <>
                    <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                    Analyzing...
                  </>
                ) : (
                  <>
                    Analyze Match
                    <ArrowRight className="ml-2 h-5 w-5" />
                  </>
                )}
              </Button>
              <p className="mt-4 text-sm text-muted-foreground">
                Your data is encrypted and automatically deleted after analysis
              </p>
            </div>

            {/* Processing Overlay */}
            {isAnalyzing && (
              <div className="mt-8 p-6 rounded-xl border border-accent/30 bg-accent/5 animate-pulse">
                <div className="flex items-center gap-4">
                  <Loader2 className="w-8 h-8 text-accent animate-spin flex-shrink-0" />
                  <div>
                    <p className="font-medium text-foreground">Matching your resume to the job...</p>
                    <p className="text-sm text-muted-foreground mt-1">Comparing keywords, skills, and qualifications</p>
                  </div>
                </div>
              </div>
            )}

            {/* Error Banner */}
            {errorMessage && !isAnalyzing && (
              <div className="mt-8 p-5 rounded-xl border border-destructive/30 bg-destructive/5">
                <div className="flex items-start gap-3">
                  <XCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                  <div className="flex-1">
                    <p className="font-medium text-destructive">Analysis Failed</p>
                    <p className="text-sm text-muted-foreground mt-1">{errorMessage}</p>
                  </div>
                  <Button variant="outline" size="sm" onClick={handleAnalyze} className="flex-shrink-0">
                    <RotateCcw className="w-4 h-4 mr-1" /> Retry
                  </Button>
                </div>
              </div>
            )}

            {/* Analysis Results Section */}
            {analysisResult && (
              <div ref={resultsRef} className="mt-12 space-y-8 animate-in fade-in-50 slide-in-from-bottom-4 duration-500">
                {/* Overall Score */}
                <div className="card-elevated p-8 text-center">
                  <h2 className="text-2xl font-bold text-white mb-4">
                    Overall <span className="gradient-gold-text">Match Score</span>
                  </h2>
                  <div className="relative inline-flex items-center justify-center">
                    <div className={`w-32 h-32 rounded-full border-4 ${getScoreBorderColor(analysisResult.matchScore)} flex items-center justify-center bg-accent/5`}>
                      <span className={`text-5xl font-bold ${getScoreColor(analysisResult.matchScore)}`}>{analysisResult.matchScore}%</span>
                    </div>
                  </div>
                  <p className="mt-4 text-lg text-muted-foreground">
                    {analysisResult.matchScore >= 80
                      ? "Excellent match! Your resume aligns strongly with this role."
                      : analysisResult.matchScore >= 60
                      ? `Your resume matches ${analysisResult.matchScore}% — add the missing keywords below to improve.`
                      : `Your resume matches ${analysisResult.matchScore}% — consider adding the missing keywords to strengthen your application.`}
                  </p>
                </div>

                {/* Keywords Section */}
                <div className="grid lg:grid-cols-2 gap-8">
                  {/* Missing Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-destructive/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-destructive/10 flex items-center justify-center">
                        <AlertTriangle className="w-5 h-5 text-destructive" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Missing Keywords</h3>
                        <p className="text-sm text-muted-foreground">{analysisResult.missingKeywords.length > 0 ? "Add these to improve your score" : "Nothing missing!"}</p>
                      </div>
                    </div>
                    {analysisResult.missingKeywords.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {analysisResult.missingKeywords.map((keyword, index) => (
                          <span 
                            key={index}
                            className="px-3 py-1.5 rounded-full text-base font-medium bg-destructive/10 text-destructive border border-destructive/20"
                          >
                            {keyword}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <div className="flex items-center gap-2 text-green-400">
                        <PartyPopper className="w-5 h-5" />
                        <p className="text-sm font-medium">All keywords matched! Perfect alignment.</p>
                      </div>
                    )}
                  </div>

                  {/* Matched Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-green-500/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-green-500/10 flex items-center justify-center">
                        <CheckCircle2 className="w-5 h-5 text-green-500" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Matched Keywords</h3>
                        <p className="text-sm text-muted-foreground">{analysisResult.matchedKeywords.length} already in your resume</p>
                      </div>
                    </div>
                    {analysisResult.matchedKeywords.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {analysisResult.matchedKeywords.map((keyword, index) => (
                          <span 
                            key={index}
                            className="px-3 py-1.5 rounded-full text-base font-medium bg-green-500/10 text-green-500 border border-green-500/20"
                          >
                            {keyword}
                          </span>
                        ))}
                      </div>
                    ) : (
                      <p className="text-sm text-muted-foreground italic">No matching keywords found between your resume and this job description.</p>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </section>

        {/* What You'll Get */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide max-w-4xl">
            <h2 className="text-2xl font-bold text-foreground text-center mb-10">
              What you'll get
            </h2>
            
            <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {[
                { title: "Match Score", desc: "Clear percentage showing alignment" },
                { title: "Matched Keywords", desc: "Keywords found in your resume" },
                { title: "Missing Keywords", desc: "Important terms to add" },
                { title: "Skill Gap Analysis", desc: "Areas to strengthen" },
              ].map((item, i) => (
                <div key={i} className="text-center p-6 bg-card rounded-xl border border-border">
                  <h4 className="font-semibold text-foreground mb-2">{item.title}</h4>
                  <p className="text-sm text-muted-foreground">{item.desc}</p>
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

export default JobMatch;
