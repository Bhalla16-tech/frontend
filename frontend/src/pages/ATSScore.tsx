import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { BarChart3, ArrowRight, TrendingUp, Loader2, CheckCircle, AlertTriangle, XCircle, RotateCcw, PartyPopper } from "lucide-react";
import { useState, useRef } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";
import { getATSScore, extractErrorMessage } from "@/api/kinovekApi";

interface ScoreBreakdown {
  category: string;
  score: number;
  status: "good" | "warning" | "poor";
}

interface ATSResult {
  overallScore: number;
  breakdown: ScoreBreakdown[];
  suggestions: string[];
}

const ATSScore = () => {
  const [isChecking, setIsChecking] = useState(false);
  const [atsResult, setAtsResult] = useState<ATSResult | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
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

  const handleCheckScore = async () => {
    if (!file) {
      toast.error("Please upload your resume first");
      return;
    }

    setIsChecking(true);
    setAtsResult(null);
    setErrorMessage(null);

    try {
      const result = await getATSScore(file);
      const getStatus = (score: number): "good" | "warning" | "poor" => {
        if (score >= 80) return "good";
        if (score >= 60) return "warning";
        return "poor";
      };

      const suggestions = [
        result.keywordMatchScore < 80 ? "Add more relevant keywords from the job description to your resume" : "",
        result.formattingScore < 80 ? "Remove tables, images, or multi-column layouts for better ATS compatibility" : "",
        result.sectionCompletenessScore < 80 ? "Ensure your resume has Summary, Experience, Education, and Skills sections" : "",
        result.overallScore < 70 ? "Consider restructuring your resume with standard section headers" : "",
      ].filter(Boolean);

      setAtsResult({
        overallScore: result.overallScore,
        breakdown: [
          { category: "Keyword Relevance", score: result.keywordMatchScore, status: getStatus(result.keywordMatchScore) },
          { category: "Formatting Compliance", score: result.formattingScore, status: getStatus(result.formattingScore) },
          { category: "Section Completeness", score: result.sectionCompletenessScore, status: getStatus(result.sectionCompletenessScore) },
        ],
        suggestions,
      });
      toast.success("ATS analysis complete!");
      setTimeout(() => resultsRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 100);
    } catch (error: unknown) {
      const msg = extractErrorMessage(error, "Failed to check ATS score. Please try again.");
      setErrorMessage(msg);
      toast.error(msg);
    } finally {
      setIsChecking(false);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return "text-green-500";
    if (score >= 60) return "text-yellow-500";
    return "text-destructive";
  };

  const getScoreBg = (score: number) => {
    if (score >= 80) return "bg-green-500/10 border-green-500/30";
    if (score >= 60) return "bg-yellow-500/10 border-yellow-500/30";
    return "bg-destructive/10 border-destructive/30";
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
                <span className="text-white">ATS Score </span>
                <span className="gradient-gold-text">Checker</span>
              </h1>
              <p className="text-lg text-muted-foreground">
                Understand exactly how your resume performs with Applicant Tracking Systems. 
                Get a detailed breakdown with actionable improvements.
              </p>
            </div>
          </div>
        </section>

        {/* Upload Section */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-3xl">
            <div className="card-elevated p-10">
              <div className="text-center mb-8">
                <div className="w-16 h-16 rounded-2xl bg-primary flex items-center justify-center mx-auto mb-5">
                  <BarChart3 className="w-8 h-8 text-primary-foreground" />
                </div>
                <h2 className="text-2xl font-bold text-foreground mb-3">
                  Check your ATS score
                </h2>
                <p className="text-muted-foreground">
                  Upload your resume to get a comprehensive ATS compatibility analysis
                </p>
              </div>

              <div className="space-y-6">
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

                <Button 
                  variant="gradient" 
                  size="xl" 
                  className="w-full"
                  onClick={handleCheckScore}
                  disabled={isChecking || !file}
                >
                  {isChecking ? (
                    <>
                      <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                      Analyzing...
                    </>
                  ) : (
                    <>
                      Check ATS Score
                      <ArrowRight className="ml-2 h-5 w-5" />
                    </>
                  )}
                </Button>
              </div>

              {/* Processing Overlay */}
              {isChecking && (
                <div className="mt-6 p-6 rounded-xl border border-accent/30 bg-accent/5 animate-pulse">
                  <div className="flex items-center gap-4">
                    <Loader2 className="w-8 h-8 text-accent animate-spin flex-shrink-0" />
                    <div>
                      <p className="font-medium text-foreground">Scanning your resume...</p>
                      <p className="text-sm text-muted-foreground mt-1">Checking formatting, keywords, and section structure</p>
                    </div>
                  </div>
                </div>
              )}

              {/* Error Banner */}
              {errorMessage && !isChecking && (
                <div className="mt-6 p-5 rounded-xl border border-destructive/30 bg-destructive/5">
                  <div className="flex items-start gap-3">
                    <XCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                    <div className="flex-1">
                      <p className="font-medium text-destructive">Analysis Failed</p>
                      <p className="text-sm text-muted-foreground mt-1">{errorMessage}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={handleCheckScore} className="flex-shrink-0">
                      <RotateCcw className="w-4 h-4 mr-1" /> Retry
                    </Button>
                  </div>
                </div>
              )}
            </div>

            {/* ATS Results Section */}
            {atsResult && (
              <div ref={resultsRef} className="mt-12 space-y-8 animate-in fade-in-50 slide-in-from-bottom-4 duration-500">
                {/* Overall Score */}
                <div className="card-elevated p-8 text-center">
                  <h2 className="text-2xl font-bold text-white mb-6">
                    Your ATS <span className="gradient-gold-text">Score</span>
                  </h2>
                  <div className="relative inline-flex items-center justify-center mb-6">
                    <div className={`w-40 h-40 rounded-full border-4 flex items-center justify-center ${getScoreBg(atsResult.overallScore)}`}>
                      <div className="text-center">
                        <span className={`text-6xl font-bold ${getScoreColor(atsResult.overallScore)}`}>
                          {atsResult.overallScore}
                        </span>
                        <span className="text-2xl text-muted-foreground">/100</span>
                      </div>
                    </div>
                  </div>
                  <p className="text-lg text-muted-foreground">
                    {atsResult.overallScore >= 80 
                      ? "Great! Your resume is well-optimized for ATS systems."
                      : atsResult.overallScore >= 60
                      ? "Good start! A few improvements can boost your score."
                      : "Your resume needs optimization to pass ATS filters."}
                  </p>
                </div>

                {/* Score Breakdown */}
                <div className="card-elevated p-8">
                  <h3 className="text-xl font-semibold text-white mb-6 text-center">
                    Score <span className="gradient-gold-text">Breakdown</span>
                  </h3>
                  <div className="grid sm:grid-cols-2 gap-4">
                    {atsResult.breakdown.map((item, index) => (
                      <div 
                        key={index}
                        className={`p-4 rounded-xl border ${getScoreBg(item.score)}`}
                      >
                        <div className="flex items-center justify-between mb-2">
                          <span className="text-base font-medium text-foreground">{item.category}</span>
                          <span className={`text-xl font-bold ${getScoreColor(item.score)}`}>
                            {item.score}%
                          </span>
                        </div>
                        <div className="w-full bg-secondary/50 rounded-full h-2">
                          <div 
                            className={`h-2 rounded-full transition-all duration-500 ${
                              item.score >= 80 ? "bg-green-500" : item.score >= 60 ? "bg-yellow-500" : "bg-destructive"
                            }`}
                            style={{ width: `${item.score}%` }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Suggestions */}
                <div className="card-elevated p-8">
                  <div className="flex items-center gap-3 mb-6 justify-center">
                    <AlertTriangle className="w-6 h-6 text-accent" />
                    <h3 className="text-xl font-semibold text-white">
                      Improvement <span className="gradient-gold-text">Suggestions</span>
                    </h3>
                  </div>
                  {atsResult.suggestions.length > 0 ? (
                    <div className="space-y-3">
                      {atsResult.suggestions.map((suggestion, index) => (
                        <div 
                          key={index}
                          className="flex items-start gap-3 p-4 bg-accent/5 rounded-xl border border-accent/20"
                        >
                          <CheckCircle className="w-5 h-5 text-accent flex-shrink-0 mt-0.5" />
                          <span className="text-base text-foreground">{suggestion}</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center p-6">
                      <PartyPopper className="w-10 h-10 text-green-500 mx-auto mb-3" />
                      <p className="text-lg font-medium text-green-400">All checks passed!</p>
                      <p className="text-sm text-muted-foreground mt-1">Your resume is well-optimized for ATS systems.</p>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </section>

        {/* Score Breakdown Preview */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide max-w-4xl">
            <h2 className="text-2xl font-bold text-foreground text-center mb-10">
              What we analyze
            </h2>
            
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
              {[
                { title: "Keyword Relevance", desc: "How well your skills match common job requirements" },
                { title: "Formatting Compliance", desc: "Whether ATS can properly parse your resume" },
                { title: "Experience Alignment", desc: "How clearly your experience is presented" },
                { title: "Skills Match", desc: "Technical and soft skills coverage" },
                { title: "Readability Score", desc: "Clarity and scannability for recruiters" },
                { title: "Overall ATS Score", desc: "Combined 0-100 compatibility rating" },
              ].map((item, i) => (
                <div key={i} className="p-6 bg-card rounded-xl border border-border">
                  <TrendingUp className="w-6 h-6 text-accent mb-3" />
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

export default ATSScore;