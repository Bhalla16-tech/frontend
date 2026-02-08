import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { Sparkles, ArrowRight, CheckCircle, Loader2, TrendingUp, FileText } from "lucide-react";
import { useState } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";
import { enhanceResume } from "@/api/kinovekApi";

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

  const handleEnhance = async () => {
    if (!file) {
      toast.error("Please upload your resume first");
      return;
    }

    setIsEnhancing(true);
    setEnhancementResult(null);

    try {
      const result = await enhanceResume(file, jobDescription);
      setEnhancementResult({
        atsScore: result.atsScore,
        matchedKeywords: result.matchedKeywords,
        missingKeywords: result.missingKeywords,
        suggestions: result.suggestions,
      });
      toast.success("Resume analysis complete! Your results are ready.");
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Failed to analyze resume";
      toast.error(message);
    } finally {
      setIsEnhancing(false);
    }
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
                {/* ATS Score */}
                <div className="card-elevated p-8 text-center">
                  <h2 className="text-2xl font-bold text-white mb-6">
                    Your ATS <span className="gradient-gold-text">Score</span>
                  </h2>
                  <div className="w-36 h-36 rounded-full border-4 border-accent/50 flex items-center justify-center mx-auto bg-accent/10 mb-4">
                    <span className="text-5xl font-bold gradient-gold-text">{enhancementResult.atsScore}%</span>
                  </div>
                </div>

                {/* Keywords */}
                <div className="grid lg:grid-cols-2 gap-8">
                  {/* Matched Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-green-500/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-green-500/10 flex items-center justify-center">
                        <CheckCircle className="w-5 h-5 text-green-500" />
                      </div>
                      <h3 className="text-xl font-semibold text-white">Matched Keywords</h3>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {enhancementResult.matchedKeywords.map((kw, i) => (
                        <span key={i} className="px-3 py-1 rounded-full bg-green-500/10 border border-green-500/30 text-green-400 text-sm">
                          {kw}
                        </span>
                      ))}
                    </div>
                  </div>

                  {/* Missing Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-destructive/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-destructive/10 flex items-center justify-center">
                        <FileText className="w-5 h-5 text-destructive" />
                      </div>
                      <h3 className="text-xl font-semibold text-white">Missing Keywords</h3>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {enhancementResult.missingKeywords.map((kw, i) => (
                        <span key={i} className="px-3 py-1 rounded-full bg-destructive/10 border border-destructive/30 text-red-400 text-sm">
                          {kw}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                {/* Suggestions */}
                <div className="card-elevated p-8">
                  <h3 className="text-xl font-semibold text-white mb-6 text-center">
                    Improvement <span className="gradient-gold-text">Suggestions</span>
                  </h3>
                  <div className="grid sm:grid-cols-2 gap-4">
                    {enhancementResult.suggestions.map((suggestion, index) => (
                      <div key={index} className="flex items-start gap-3 p-4 bg-accent/5 rounded-xl border border-accent/20">
                        <CheckCircle className="w-5 h-5 text-accent flex-shrink-0 mt-0.5" />
                        <span className="text-base text-foreground">{suggestion}</span>
                      </div>
                    ))}
                  </div>
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