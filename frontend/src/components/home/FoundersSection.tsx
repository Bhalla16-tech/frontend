import { Linkedin } from "lucide-react";

const founders = [
  {
    name: "Vadla Srikanth",
    title: "QA Tester & AI Journalist",
    role: "Co-Founder & Visionary",
    summary: "\"Ensuring excellence through quality assurance and innovative AI storytelling.\"",
    linkedin: "https://www.linkedin.com/in/vadla-srikanth-982900213"
  },
  {
    name: "Chityala Sai Krishna",
    title: "Graphic Designer & AI Journalist",
    role: "Co-Founder & Strategist",
    summary: "\"Crafting visual experiences and leveraging AI to transform career narratives.\"",
    linkedin: "https://www.linkedin.com/in/sai-krishna-chityala"
  }
];

export const FoundersSection = () => {
  return (
    <section className="py-20 md:py-28 relative">
      <div className="container-wide relative z-10">
        {/* Header */}
        <div className="text-center mb-14">
          <h2 className="text-4xl md:text-5xl font-light text-foreground mb-3 tracking-tight">
            Meet the <span className="text-primary font-semibold">Founders</span>
          </h2>
          <p className="text-muted-foreground text-sm">
            The minds behind Kinovek
          </p>
        </div>
        
        {/* Founders Cards */}
        <div className="flex flex-col md:flex-row justify-center items-stretch gap-6 md:gap-8 lg:gap-10 max-w-4xl mx-auto">
          {founders.map((founder, index) => (
            <div
              key={index}
              className="group relative flex flex-col items-center text-center p-8 md:p-10 rounded-2xl border border-border/60 bg-card/20 transition-all duration-300 hover:border-primary/60 flex-1 max-w-md mx-auto md:mx-0"
            >
              {/* Subtle outer glow on hover */}
              <div 
                className="absolute -inset-px rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none"
                style={{
                  boxShadow: '0 0 30px 0 hsl(48 100% 50% / 0.15)',
                }}
              />
              
              {/* Name */}
              <h3 className="text-xl md:text-2xl font-bold text-foreground tracking-tight mb-2">
                {founder.name}
              </h3>
              
              {/* Job Title */}
              <p className="text-sm md:text-base font-semibold text-primary mb-1">
                {founder.title}
              </p>
              
              {/* Role */}
              <p className="text-xs md:text-sm text-muted-foreground mb-5">
                {founder.role}
              </p>
              
              {/* Summary Quote */}
              <p className="text-sm text-muted-foreground italic leading-relaxed mb-6 max-w-xs">
                {founder.summary}
              </p>
              
              {/* LinkedIn Button */}
              <a
                href={founder.linkedin}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-colors duration-300 text-sm font-medium"
              >
                <Linkedin className="w-4 h-4" />
                <span>Connect on LinkedIn</span>
              </a>
            </div>
          ))}
        </div>
      </div>
      
      {/* Bottom divider line */}
      <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-full max-w-5xl h-px bg-gradient-to-r from-transparent via-border/60 to-transparent" />
    </section>
  );
};
