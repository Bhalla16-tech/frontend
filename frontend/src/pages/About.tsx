import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Linkedin } from "lucide-react";

const founders = [
  {
    name: "Vadla Srikanth",
    tagline: "Visionary & Strategist",
    linkedin: "https://www.linkedin.com/in/vadla-srikanth-982900213",
  },
  {
    name: "Chityala Sai Krishna",
    tagline: "Architect & Innovator",
    linkedin: "https://www.linkedin.com/in/sai-krishna-chityala",
  },
];

const About = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        <section className="py-20 md:py-28 bg-background relative overflow-hidden">
          {/* Background glow effects */}
          <div className="absolute top-1/2 left-1/4 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-accent/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute top-1/2 right-1/4 translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-accent/10 rounded-full blur-3xl pointer-events-none" />
          
          <div className="container-wide relative z-10">
            <div className="max-w-3xl mx-auto text-center mb-16">
              <span className="text-sm font-semibold text-accent uppercase tracking-wider mb-3 block">
                Meet The Team
              </span>
              <h1 className="text-4xl sm:text-5xl font-bold text-foreground mb-4">
                The Founders
              </h1>
              <p className="text-muted-foreground">
                Building the future of career success.
              </p>
            </div>

            {/* Founders Grid - Side by Side */}
            <div className="flex flex-col sm:flex-row items-center justify-center gap-8 md:gap-16">
              {founders.map((founder, index) => (
                <a
                  key={index}
                  href={founder.linkedin}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="group relative"
                >
                  {/* Glow effect on hover */}
                  <div className="absolute -inset-4 bg-accent/0 group-hover:bg-accent/20 rounded-2xl blur-xl transition-all duration-500 pointer-events-none" />
                  
                  <div className="relative flex flex-col items-center p-8 rounded-xl bg-card border border-border group-hover:border-accent/50 transition-all duration-300 group-hover:shadow-2xl group-hover:shadow-accent/20">
                    {/* Avatar placeholder with glow */}
                    <div className="w-24 h-24 rounded-full bg-gradient-to-br from-accent to-accent/60 flex items-center justify-center mb-5 group-hover:shadow-lg group-hover:shadow-accent/40 transition-all duration-300">
                      <span className="text-3xl font-bold text-background">
                        {founder.name.split(" ").map(n => n[0]).join("")}
                      </span>
                    </div>
                    
                    {/* Name with glow on hover */}
                    <h3 className="text-xl font-bold text-foreground group-hover:text-accent transition-colors duration-300 mb-1">
                      {founder.name}
                    </h3>
                    
                    {/* Tagline */}
                    <p className="text-sm text-muted-foreground mb-4">
                      {founder.tagline}
                    </p>
                    
                    {/* LinkedIn Icon */}
                    <div className="flex items-center gap-2 text-sm text-accent group-hover:scale-110 transition-transform duration-300">
                      <Linkedin className="w-5 h-5" />
                      <span>Connect</span>
                    </div>
                  </div>
                </a>
              ))}
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default About;
