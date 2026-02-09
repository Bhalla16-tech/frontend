import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { FileSignature, ArrowRight, CheckCircle, Loader2, Copy, Download, XCircle, RotateCcw, AlertCircle } from "lucide-react";
import { useState, useRef } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";
import { generateCoverLetter, extractErrorMessage } from "@/api/kinovekApi";

const CoverLetter = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [isGenerating, setIsGenerating] = useState(false);
  const [coverLetterText, setCoverLetterText] = useState<string | null>(null);
  const [coverLetterMeta, setCoverLetterMeta] = useState<{ candidateName: string; targetRole: string; companyName: string } | null>(null);
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

  const handleGenerate = async () => {
    if (!jobDescription.trim()) {
      setJdError("Please paste the job description before generating");
      toast.error("Please paste the job description");
      return;
    }
    if (!file) {
      toast.error("Please upload your resume for a personalized cover letter");
      return;
    }

    setJdError(null);

    setIsGenerating(true);
    setCoverLetterText(null);
    setErrorMessage(null);

    try {
      const result = await generateCoverLetter(file, jobDescription);
      setCoverLetterText(result.data.coverLetterText);
      setCoverLetterMeta({
        candidateName: result.data.candidateName,
        targetRole: result.data.targetRole,
        companyName: result.data.companyName,
      });
      toast.success("Cover letter generated successfully!");
      setTimeout(() => resultsRef.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 100);
    } catch (error: unknown) {
      const msg = extractErrorMessage(error, "Failed to generate cover letter. Please try again.");
      setErrorMessage(msg);
      toast.error(msg);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleCopy = () => {
    if (coverLetterText) {
      navigator.clipboard.writeText(coverLetterText);
      toast.success("Copied to clipboard!");
    }
  };

  const handleDownload = () => {
    if (coverLetterText) {
      const blob = new Blob([coverLetterText], { type: "text/plain;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `cover-letter-${coverLetterMeta?.targetRole?.replace(/\s+/g, "-").toLowerCase() || "generated"}.txt`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      toast.success("Cover letter downloaded!");
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
                <span className="text-foreground">Cover Letter </span>
                <span className="gradient-gold-text">Generator</span>
              </h1>
              <p className="text-lg text-muted-foreground">
                Create compelling, role-specific cover letters that connect your experience 
                to the job. Professional, recruiter-ready, never generic.
              </p>
            </div>
          </div>
        </section>

        {/* Generator Section */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-3xl">
            <div className="card-elevated p-10">
              <div className="text-center mb-8">
                <div className="w-16 h-16 rounded-2xl gradient-teal flex items-center justify-center mx-auto mb-5">
                  <FileSignature className="w-8 h-8 text-white" />
                </div>
                <h2 className="text-2xl font-bold text-foreground mb-3">
                  Generate your cover letter
                </h2>
                <p className="text-muted-foreground">
                  Paste the job description and optionally upload your resume for personalized content
                </p>
              </div>

              <div className="space-y-6">
                {/* Job Description */}
                <div>
                  <label className="block text-sm font-medium text-foreground mb-3">
                    Job Description <span className="text-destructive">*</span>
                  </label>
                  <textarea 
                    value={jobDescription}
                    onChange={(e) => { setJobDescription(e.target.value); if (e.target.value.trim()) setJdError(null); }}
                    placeholder="Paste the full job description here..."
                    className={`w-full h-40 p-4 rounded-xl border bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-accent/50 transition-all ${jdError ? 'border-destructive/50 bg-destructive/5' : 'border-border'}`}
                    disabled={isGenerating}
                  />
                  {jdError && (
                    <p className="mt-2 text-sm text-destructive flex items-center gap-1.5">
                      <AlertCircle className="h-4 w-4 flex-shrink-0" />
                      {jdError}
                    </p>
                  )}
                </div>

                {/* Resume Upload */}
                <div>
                  <label className="block text-sm font-medium text-foreground mb-3">
                    Your Resume <span className="text-destructive">*</span>
                  </label>
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
                    compact
                  />
                </div>

                <Button 
                  variant="gradient" 
                  size="xl" 
                  className="w-full"
                  onClick={handleGenerate}
                  disabled={isGenerating || !file || !jobDescription.trim()}
                >
                  {isGenerating ? (
                    <>
                      <Loader2 className="mr-2 h-5 w-5 animate-spin" />
                      Generating...
                    </>
                  ) : (
                    <>
                      Generate Cover Letter
                      <ArrowRight className="ml-2 h-5 w-5" />
                    </>
                  )}
                </Button>
              </div>

              {/* Processing Overlay */}
              {isGenerating && (
                <div className="mt-6 p-6 rounded-xl border border-accent/30 bg-accent/5 animate-pulse">
                  <div className="flex items-center gap-4">
                    <Loader2 className="w-8 h-8 text-accent animate-spin flex-shrink-0" />
                    <div>
                      <p className="font-medium text-foreground">Crafting your cover letter...</p>
                      <p className="text-sm text-muted-foreground mt-1">Analyzing your resume and matching it to the job requirements</p>
                    </div>
                  </div>
                </div>
              )}

              {/* Error Banner */}
              {errorMessage && !isGenerating && (
                <div className="mt-6 p-5 rounded-xl border border-destructive/30 bg-destructive/5">
                  <div className="flex items-start gap-3">
                    <XCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                    <div className="flex-1">
                      <p className="font-medium text-destructive">Generation Failed</p>
                      <p className="text-sm text-muted-foreground mt-1">{errorMessage}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={handleGenerate} className="flex-shrink-0">
                      <RotateCcw className="w-4 h-4 mr-1" /> Retry
                    </Button>
                  </div>
                </div>
              )}
            </div>

            {/* Generated Cover Letter */}
            {coverLetterText && (
              <div ref={resultsRef} className="mt-10 card-elevated p-8 animate-in fade-in-50 slide-in-from-bottom-4 duration-500">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-xl font-semibold text-white">
                    Your <span className="gradient-gold-text">Cover Letter</span>
                  </h3>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={handleCopy}>
                      <Copy className="w-4 h-4 mr-1" /> Copy
                    </Button>
                    <Button variant="outline" size="sm" onClick={handleDownload}>
                      <Download className="w-4 h-4 mr-1" /> Download
                    </Button>
                  </div>
                </div>
                {coverLetterMeta && (
                  <div className="flex flex-wrap gap-4 mb-4 text-sm text-muted-foreground">
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-accent/10 border border-accent/20">
                      Candidate: <strong className="text-foreground">{coverLetterMeta.candidateName}</strong>
                    </span>
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-accent/10 border border-accent/20">
                      Role: <strong className="text-foreground">{coverLetterMeta.targetRole}</strong>
                    </span>
                    <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-accent/10 border border-accent/20">
                      Company: <strong className="text-foreground">{coverLetterMeta.companyName}</strong>
                    </span>
                  </div>
                )}
                <div className="whitespace-pre-wrap text-foreground bg-secondary/30 p-6 rounded-xl border border-border text-base leading-relaxed">
                  {coverLetterText}
                </div>
              </div>
            )}
          </div>
        </section>

        {/* Features */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide max-w-4xl">
            <h2 className="text-2xl font-bold text-foreground text-center mb-10">
              What makes our cover letters effective
            </h2>
            
            <div className="grid sm:grid-cols-2 gap-5">
              {[
                "Role-specific content tailored to each job",
                "Professional, recruiter-approved tone",
                "Clear connection between your experience and requirements",
                "No generic filler or robotic language",
                "Editable and ready for download",
                "Formatted for both digital and print",
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

export default CoverLetter;
