import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Button } from "@/components/ui/button";
import { ArrowRight, FileText, Search, Settings, CheckCircle } from "lucide-react";
import { Link } from "react-router-dom";

const HowItWorks = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        {/* Hero */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide">
            <div className="max-w-3xl mx-auto text-center">
              <h1 className="text-4xl sm:text-5xl font-bold text-foreground mb-5">
                How Kinovek Works
              </h1>
              <p className="text-lg text-muted-foreground">
                Understanding why resumes fail—and how we help you succeed.
              </p>
            </div>
          </div>
        </section>

        {/* ATS Explanation */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-4xl">
            <div className="grid md:grid-cols-2 gap-12 items-center">
              <div>
                <h2 className="text-3xl font-bold text-foreground mb-5">
                  What is an ATS?
                </h2>
                <p className="text-muted-foreground mb-4 leading-relaxed">
                  An Applicant Tracking System (ATS) is software that companies use to manage job applications. 
                  Before a human ever sees your resume, an ATS scans it for relevant keywords, proper formatting, 
                  and alignment with the job description.
                </p>
                <p className="text-muted-foreground leading-relaxed">
                  <strong className="text-foreground">The reality:</strong> Up to 75% of resumes are rejected 
                  by ATS before reaching a recruiter—even from qualified candidates.
                </p>
              </div>
              <div className="p-8 bg-secondary/50 rounded-2xl">
                <div className="space-y-4">
                  {[
                    "Scans for job-specific keywords",
                    "Parses resume formatting and structure",
                    "Ranks candidates by relevance score",
                    "Filters out non-compliant resumes",
                  ].map((item, i) => (
                    <div key={i} className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-accent/10 flex items-center justify-center">
                        <span className="text-sm font-bold text-accent">{i + 1}</span>
                      </div>
                      <span className="text-foreground">{item}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Why Keywords Matter */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide max-w-4xl">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold text-foreground mb-4">
                Why keywords matter
              </h2>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                ATS systems look for specific terms that match the job description. 
                Missing even a few key phrases can push your resume to the bottom of the pile.
              </p>
            </div>

            <div className="grid sm:grid-cols-3 gap-6">
              {[
                {
                  icon: FileText,
                  title: "Job Title Match",
                  desc: "The exact job title and variations used in the posting",
                },
                {
                  icon: Settings,
                  title: "Technical Skills",
                  desc: "Specific tools, software, and technologies mentioned",
                },
                {
                  icon: Search,
                  title: "Industry Terms",
                  desc: "Standard terminology and certifications in your field",
                },
              ].map((item, i) => (
                <div key={i} className="card-elevated p-6 text-center">
                  <div className="w-12 h-12 rounded-xl bg-accent/10 flex items-center justify-center mx-auto mb-4">
                    <item.icon className="w-6 h-6 text-accent" />
                  </div>
                  <h4 className="font-semibold text-foreground mb-2">{item.title}</h4>
                  <p className="text-sm text-muted-foreground">{item.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Our Approach */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-4xl">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold text-foreground mb-4">
                How Kinovek helps
              </h2>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                We analyze your resume against real job descriptions and provide clear, 
                actionable guidance—transparently and ethically.
              </p>
            </div>

            <div className="space-y-4">
              {[
                "We show you exactly which keywords you have and which you're missing",
                "We explain why certain formatting helps ATS readability",
                "We enhance your resume while keeping your authentic voice",
                "We never use deceptive practices or keyword stuffing",
                "We help you understand and learn, not just pass a test",
              ].map((item, i) => (
                <div key={i} className="flex items-start gap-4 p-5 bg-secondary/30 rounded-xl">
                  <CheckCircle className="w-6 h-6 text-accent flex-shrink-0" />
                  <span className="text-foreground">{item}</span>
                </div>
              ))}
            </div>

            <div className="mt-12 text-center">
              <Link to="/resume-enhancer">
                <Button variant="gradient" size="xl">
                  Try It Now
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Button>
              </Link>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default HowItWorks;
