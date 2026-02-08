import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Menu, X } from "lucide-react";
import kinovekLogo from "@/assets/kinovek-logo.png";

const navLinks = [
  { href: "/", label: "Home" },
  { href: "/job-match", label: "Job Match" },
  { href: "/resume-enhancer", label: "Resume Enhancer" },
  { href: "/ats-score", label: "ATS Score" },
  { href: "/cover-letter", label: "Cover Letter" },
];

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const location = useLocation();

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur-md supports-[backdrop-filter]:bg-background/80">
      <div className="container-wide flex h-16 items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 group">
          <img 
            src={kinovekLogo} 
            alt="Kinovek" 
            className="h-12 w-12 transition-transform duration-200 group-hover:scale-105" 
          />
          <span className="text-xl font-bold tracking-tight gradient-brand-text">
            Kinovek
          </span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden lg:flex items-center gap-1">
          {navLinks.map((link) => (
            <Link
              key={link.href}
              to={link.href}
              className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors duration-200 ${
                location.pathname === link.href
                  ? "text-accent bg-secondary"
                  : "text-muted-foreground hover:text-foreground hover:bg-secondary/50"
              }`}
            >
              {link.label}
            </Link>
          ))}
        </nav>

        {/* Desktop CTA */}
        <div className="hidden lg:flex items-center gap-3">
          <span className="px-4 py-1.5 text-sm font-medium text-accent bg-accent/10 backdrop-blur-md border border-accent/30 rounded-full shadow-[0_0_15px_rgba(255,200,0,0.3)]">
            100% Free
          </span>
        </div>

        {/* Mobile Menu Button */}
        <button
          className="lg:hidden p-2 text-foreground"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </div>

      {/* Mobile Navigation */}
      {mobileMenuOpen && (
        <div className="lg:hidden absolute top-16 inset-x-0 bg-background border-b border-border shadow-elevated animate-fade-in">
          <nav className="container-wide py-6 flex flex-col gap-2">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                to={link.href}
                onClick={() => setMobileMenuOpen(false)}
                className={`px-4 py-3 text-base font-medium rounded-lg transition-colors ${
                  location.pathname === link.href
                    ? "text-accent bg-secondary"
                    : "text-muted-foreground hover:text-foreground hover:bg-secondary/50"
                }`}
              >
                {link.label}
              </Link>
            ))}
            <div className="mt-4 pt-4 border-t border-border text-center">
              <span className="px-4 py-1.5 text-sm font-medium text-accent bg-accent/10 backdrop-blur-md border border-accent/30 rounded-full shadow-[0_0_15px_rgba(255,200,0,0.3)]">
                100% Free to Use
              </span>
            </div>
          </nav>
        </div>
      )}
    </header>
  );
}