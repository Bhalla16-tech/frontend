import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { FileText, ArrowRight, Loader2, AlertTriangle, CheckCircle2 } from "lucide-react";
import { useState } from "react";
import { useFileUpload } from "@/hooks/useFileUpload";
import { FileUploadZone } from "@/components/shared/FileUploadZone";
import { toast } from "sonner";

interface AnalysisResult {
  matchScore: number;
  matchedKeywords: string[];
  missingKeywords: string[];
}

const JobMatch = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult | null>(null);
  
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

  const handleAnalyze = () => {
    if (!file) {
      toast.error("Please upload your resume first");
      return;
    }
    if (!jobDescription.trim()) {
      toast.error("Please paste the job description");
      return;
    }

    setIsAnalyzing(true);
    setAnalysisResult(null);
    
    // Simulate analysis with mock results
    setTimeout(() => {
      setIsAnalyzing(false);
      setAnalysisResult({
        matchScore: 72,
        matchedKeywords: ["React", "JavaScript", "TypeScript", "CSS", "HTML", "Git", "Agile", "REST API"],
        missingKeywords: ["Node.js", "AWS", "Docker", "CI/CD", "GraphQL", "MongoDB", "Python", "Kubernetes"],
      });
      toast.success("Analysis complete! Your match score is ready.");
    }, 2000);
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
                  onChange={(e) => setJobDescription(e.target.value)}
                  placeholder="Paste the job description here..."
                  className="w-full h-48 p-4 rounded-xl border border-border bg-background text-foreground placeholder:text-muted-foreground resize-none focus:outline-none focus:ring-2 focus:ring-accent/50 transition-all"
                />
              </div>
            </div>

            {/* Analyze Button */}
            <div className="mt-8 text-center">
              <Button 
                variant="gradient" 
                size="xl" 
                onClick={handleAnalyze}
                disabled={isAnalyzing}
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

            {/* Analysis Results Section */}
            {analysisResult && (
              <div className="mt-12 space-y-8 animate-in fade-in-50 duration-500">
                {/* Overall Score */}
                <div className="card-elevated p-8 text-center">
                  <h2 className="text-2xl font-bold text-white mb-4">
                    Overall <span className="gradient-gold-text">Match Score</span>
                  </h2>
                  <div className="relative inline-flex items-center justify-center">
                    <div className="w-32 h-32 rounded-full border-4 border-accent/30 flex items-center justify-center bg-accent/5">
                      <span className="text-5xl font-bold gradient-gold-text">{analysisResult.matchScore}%</span>
                    </div>
                  </div>
                  <p className="mt-4 text-lg text-muted-foreground">
                    Your resume matches {analysisResult.matchScore}% of the job requirements
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
                        <p className="text-sm text-muted-foreground">Add these to improve your score</p>
                      </div>
                    </div>
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
                  </div>

                  {/* Matched Keywords */}
                  <div className="card-elevated p-8 border-l-4 border-l-green-500/50">
                    <div className="flex items-center gap-3 mb-6">
                      <div className="w-10 h-10 rounded-lg bg-green-500/10 flex items-center justify-center">
                        <CheckCircle2 className="w-5 h-5 text-green-500" />
                      </div>
                      <div>
                        <h3 className="text-xl font-semibold text-white">Matched Keywords</h3>
                        <p className="text-sm text-muted-foreground">Already in your resume</p>
                      </div>
                    </div>
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
