import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { FileSignature, ArrowRight, CheckCircle, Loader2, Copy, Download } from "lucide-react";
import { useState } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";
import { generateCoverLetter } from "@/api/kinovekApi";

const CoverLetter = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [isGenerating, setIsGenerating] = useState(false);
  const [coverLetterText, setCoverLetterText] = useState<string | null>(null);
  const [coverLetterMeta, setCoverLetterMeta] = useState<{ candidateName: string; targetRole: string; companyName: string } | null>(null);

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

  const handleGenerate = async () => {
    if (!jobDescription.trim()) {
      toast.error("Please paste the job description");
      return;
    }
    if (!file) {
      toast.error("Please upload your resume for a personalized cover letter");
      return;
    }

    setIsGenerating(true);
    setCoverLetterText(null);

    try {
      const result = await generateCoverLetter(file, jobDescription);
      setCoverLetterText(result.data.coverLetterText);
      setCoverLetterMeta({
        candidateName: result.data.candidateName,
        targetRole: result.data.targetRole,
        companyName: result.data.companyName,
      });
      toast.success("Cover letter generated successfully!");
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : "Failed to generate cover letter";
      toast.error(message);
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
                    onChange={(e) => setJobDescription(e.target.value)}
                    placeholder="Paste the full job description here..."
                    className="w-full h-40 p-4 rounded-xl border border-border bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-accent/50 transition-all"
                  />
                </div>

                {/* Resume Upload */}
                <div>
                  <label className="block text-sm font-medium text-foreground mb-3">
                    Your Resume (optional)
                  </label>
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
                    compact
                  />
                </div>

                <Button 
                  variant="gradient" 
                  size="xl" 
                  className="w-full"
                  onClick={handleGenerate}
                  disabled={isGenerating}
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
            </div>

            {/* Generated Cover Letter */}
            {coverLetterText && (
              <div className="mt-10 card-elevated p-8 animate-in fade-in-50 duration-500">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="text-xl font-semibold text-white">
                    Your <span className="gradient-gold-text">Cover Letter</span>
                  </h3>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={handleCopy}>
                      <Copy className="w-4 h-4 mr-1" /> Copy
                    </Button>
                  </div>
                </div>
                {coverLetterMeta && (
                  <div className="flex gap-4 mb-4 text-sm text-muted-foreground">
                    <span>Candidate: <strong className="text-foreground">{coverLetterMeta.candidateName}</strong></span>
                    <span>Role: <strong className="text-foreground">{coverLetterMeta.targetRole}</strong></span>
                    <span>Company: <strong className="text-foreground">{coverLetterMeta.companyName}</strong></span>
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
