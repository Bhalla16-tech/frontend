import { Link } from "react-router-dom";
import kinovekLogo from "@/assets/kinovek-logo.png";

const navLinks = [
  { href: "/job-match", label: "Job Match" },
  { href: "/resume-enhancer", label: "Resume Enhancer" },
  { href: "/ats-score", label: "ATS Score" },
  { href: "/cover-letter", label: "Cover Letter" },
];

export function Footer() {
  return (
    <footer className="bg-card border-t border-border">
      <div className="container-wide py-12 md:py-16">
        {/* Centered Content */}
        <div className="flex flex-col items-center text-center">
          {/* Logo & Brand */}
          <Link to="/" className="inline-flex items-center gap-2 mb-6">
            <img 
              src={kinovekLogo} 
              alt="Kinovek" 
              className="h-10 w-10" 
            />
            <span className="text-xl font-semibold tracking-tight gradient-brand-text">
              Kinovek
            </span>
          </Link>

          {/* Badge */}
          <div className="inline-flex items-center px-5 py-2 rounded-full border border-accent/50 mb-8">
            <span className="text-sm font-medium text-accent">100% Free Platform</span>
          </div>

          {/* Navigation Links */}
          <nav className="flex flex-wrap justify-center gap-6 md:gap-10 mb-10">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                to={link.href}
                className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>

        {/* Bottom Bar */}
        <div className="pt-6 border-t border-border flex flex-col sm:flex-row justify-center items-center gap-3">
          <p className="text-sm text-muted-foreground">
            © 2026 Kinovek. All rights reserved.
          </p>
          <span className="hidden sm:inline text-muted-foreground">•</span>
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-accent"></span>
            <span className="text-sm font-medium text-accent">Your data is always secure</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
