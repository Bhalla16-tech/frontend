import { Button } from "@/components/ui/button";
import { Upload, Search } from "lucide-react";
import { Link } from "react-router-dom";
import { Suspense, lazy } from "react";

const Hero3DScene = lazy(() => import('./Hero3DScene').then(mod => ({ default: mod.Hero3DScene })));

export function HeroSection() {
  return (
    <section className="relative overflow-hidden bg-background min-h-[90vh]">

      {/* Subtle radial gradient */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,_var(--tw-gradient-stops))] from-gold-900/20 via-transparent to-transparent" />
      
      {/* 3D Animation Scene - centered in background */}
      <Suspense fallback={null}>
        <Hero3DScene />
      </Suspense>
      
      <div className="container-wide relative py-16 md:py-20">
        <div className="max-w-4xl text-left">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-secondary border border-border text-sm font-medium mb-8 animate-fade-up">
            <span className="w-2 h-2 rounded-full bg-accent animate-pulse" />
            <span className="text-foreground">Resume Intelligence Platform</span>
            <span className="text-accent font-semibold">— 100% Free</span>
          </div>

          {/* Headline with gold gradient */}
          <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-extrabold leading-[1.1] tracking-tight mb-6 animate-fade-up stagger-1">
            <span className="text-foreground">Get shortlisted for the</span>{" "}
            <span className="gradient-gold-text">jobs you deserve</span>
          </h1>

          {/* Subheadline */}
          <p className="text-lg sm:text-xl text-muted-foreground max-w-2xl mb-10 leading-relaxed animate-fade-up stagger-2">
            Most qualified candidates fail ATS screening due to missing keywords and formatting issues. 
            Kinovek helps your resume match what recruiters actually look for.
          </p>

          {/* CTAs */}
          <div className="flex flex-col sm:flex-row items-start gap-4 mb-16 animate-fade-up stagger-3">
            <Link to="/resume-enhancer">
              <Button variant="hero" size="xl" className="w-full sm:w-auto">
                <Upload className="mr-2 h-5 w-5" />
                Upload Resume
              </Button>
            </Link>
            <Link to="/job-match">
              <Button variant="hero-outline" size="xl" className="w-full sm:w-auto">
                <Search className="mr-2 h-5 w-5" />
                Check Job Match
              </Button>
            </Link>
          </div>

          {/* Trust Indicators */}
          <div className="flex flex-wrap items-start gap-6 text-sm animate-fade-up stagger-4">
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-accent" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <span className="text-foreground">ATS-Compatible</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-accent" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <span className="text-foreground">Recruiter-Approved</span>
            </div>
            <div className="flex items-center gap-2">
              <svg className="w-5 h-5 text-accent" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
              <span className="text-foreground">Data Secure</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}