import { FileText, Sparkles, BarChart3, ArrowRight, FileCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Link } from "react-router-dom";

const features = [
  {
    icon: FileCheck,
    title: "Instant Job Match",
    description: "Upload your resume and paste a job description. Get an instant match score with detailed analysis of what's aligned and what's missing.",
    benefits: [
      "Resume-JD match percentage",
      "Matched & missing keywords",
      "Skill gap analysis",
      "Clear, actionable feedback",
    ],
    buttonText: "Try Instant Job Match",
    href: "/job-match",
  },
  {
    icon: Sparkles,
    title: "Resume Enhancer",
    description: "Transform your resume to match specific roles. We optimize structure, enhance bullet points, and add relevant keywords naturally.",
    benefits: [
      "ATS-safe formatting",
      "Impact-driven bullet points",
      "Natural keyword integration",
      "Before & after comparison",
    ],
    buttonText: "Try Resume Enhancer",
    href: "/resume-enhancer",
  },
  {
    icon: BarChart3,
    title: "ATS Score Checker",
    description: "Understand exactly how ATS systems see your resume. Get a detailed breakdown with specific recommendations for improvement.",
    benefits: [
      "Score from 0-100",
      "Section-wise breakdown",
      "Formatting compliance",
      "Improvement roadmap",
    ],
    buttonText: "Try ATS Score Checker",
    href: "/ats-score",
  },
];

export function FeaturesSection() {
  return (
    <section className="py-20 md:py-24 bg-background">
      <div className="container-wide">
        <div className="max-w-3xl mx-auto text-center mb-14">
          <span className="inline-flex items-center gap-2 text-sm font-medium text-accent border border-accent/40 rounded-full px-4 py-1.5 mb-6">
            <FileText className="w-4 h-4" />
            Core Solutions
          </span>
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold text-foreground mb-4">
            Three tools to help you get shortlisted
          </h2>
          <p className="text-muted-foreground text-lg leading-relaxed">
            Each tool addresses a specific gap between your resume and what recruiters look for.
            <br />
            Use them together for maximum impact.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {features.map((feature, index) => (
            <div
              key={index}
              className="rounded-2xl border border-accent/20 bg-card p-8 flex flex-col"
            >
              <div className="w-12 h-12 rounded-xl bg-accent/10 border border-accent/30 flex items-center justify-center mb-6">
                <feature.icon className="w-6 h-6 text-accent" />
              </div>
              
              <h3 className="text-xl font-bold text-foreground mb-3">
                {feature.title}
              </h3>
              
              <p className="text-muted-foreground text-sm leading-relaxed mb-6">
                {feature.description}
              </p>
              
              <ul className="space-y-3 mb-8 flex-grow">
                {feature.benefits.map((benefit, i) => (
                  <li key={i} className="flex items-center gap-2 text-sm text-muted-foreground">
                    <span className="w-1.5 h-1.5 rounded-full bg-accent flex-shrink-0" />
                    {benefit}
                  </li>
                ))}
              </ul>
              
              <Link to={feature.href}>
                <Button 
                  variant="outline" 
                  className="w-full border-accent text-accent hover:bg-accent hover:text-background group"
                >
                  {feature.buttonText}
                  <ArrowRight className="w-4 h-4 ml-2 transition-transform group-hover:translate-x-1" />
                </Button>
              </Link>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
