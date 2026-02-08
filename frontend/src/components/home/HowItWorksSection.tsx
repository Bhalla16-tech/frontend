import { Upload, Scan, Download, ArrowRight } from "lucide-react";

const steps = [
  {
    icon: Upload,
    number: "01",
    title: "Upload your resume",
    description: "Drop your PDF or DOCX file. Optionally, paste the job description you're targeting.",
    hasArrow: true,
  },
  {
    icon: Scan,
    number: "02",
    title: "Get instant analysis",
    description: "Our system scans for keywords, formatting issues, and alignment with the role requirements.",
    hasArrow: true,
  },
  {
    icon: Download,
    number: "03",
    title: "Download your optimized resume",
    description: "Receive clear recommendations or an enhanced version ready to submit.",
    hasArrow: false,
  },
];

export function HowItWorksSection() {
  return (
    <section className="py-20 md:py-24 bg-card/30">
      <div className="container-wide">
        <div className="max-w-3xl mx-auto text-center mb-14">
          <span className="inline-flex items-center text-sm font-medium text-accent border border-accent/40 rounded-full px-4 py-1.5 mb-6">
            Simple Process
          </span>
          <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold text-foreground mb-4">
            How <span className="text-accent italic">Kinovek</span> Works
          </h2>
          <p className="text-muted-foreground text-lg">
            No complicated setup. No learning curve. Just results in under 60 seconds.
          </p>
        </div>

        <div className="max-w-6xl mx-auto">
          <div className="grid md:grid-cols-3 gap-6">
            {steps.map((step, index) => (
              <div 
                key={index} 
                className="rounded-2xl border border-accent/20 bg-card p-8 relative"
              >
                {/* Header with icon and number */}
                <div className="flex items-start justify-between mb-6">
                  <div className="w-12 h-12 rounded-xl bg-accent/10 border border-accent/30 flex items-center justify-center">
                    <step.icon className="w-5 h-5 text-accent" />
                  </div>
                  <span className="text-4xl font-bold text-accent/30">
                    {step.number}
                  </span>
                </div>
                
                {/* Title with arrow */}
                <div className="flex items-center gap-3 mb-3">
                  <h3 className="text-lg font-bold text-foreground">
                    {step.title}
                  </h3>
                  {step.hasArrow && (
                    <div className="w-8 h-8 rounded-full border border-accent/30 flex items-center justify-center">
                      <ArrowRight className="w-4 h-4 text-accent" />
                    </div>
                  )}
                </div>
                
                {/* Description */}
                <p className="text-sm text-muted-foreground leading-relaxed">
                  {step.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
