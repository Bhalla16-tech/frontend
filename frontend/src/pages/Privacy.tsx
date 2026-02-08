import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import { Shield, Lock, Eye, Trash2, Server, Bell } from "lucide-react";

const Privacy = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        {/* Hero */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide">
            <div className="max-w-3xl mx-auto text-center">
              <h1 className="text-4xl sm:text-5xl font-bold text-foreground mb-5">
                Privacy & Security
              </h1>
              <p className="text-lg text-muted-foreground">
                Your data is yours. Here's how we protect it.
              </p>
            </div>
          </div>
        </section>

        {/* Privacy Highlights */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-4xl">
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-16">
              {[
                {
                  icon: Shield,
                  title: "Encrypted Storage",
                  desc: "All data is encrypted at rest using AES-256 encryption.",
                },
                {
                  icon: Lock,
                  title: "Secure Transit",
                  desc: "TLS 1.3 encryption protects data during transmission.",
                },
                {
                  icon: Trash2,
                  title: "Auto-Deletion",
                  desc: "Uploaded files are automatically deleted after processing.",
                },
                {
                  icon: Eye,
                  title: "No Tracking",
                  desc: "We don't sell your data or track you across the web.",
                },
                {
                  icon: Server,
                  title: "Secure Infrastructure",
                  desc: "Hosted on SOC 2 compliant cloud infrastructure.",
                },
                {
                  icon: Bell,
                  title: "Breach Notification",
                  desc: "We'll notify you immediately if your data is ever at risk.",
                },
              ].map((item, i) => (
                <div key={i} className="p-6 bg-secondary/30 rounded-xl">
                  <div className="w-12 h-12 rounded-xl bg-accent/10 flex items-center justify-center mb-4">
                    <item.icon className="w-6 h-6 text-accent" />
                  </div>
                  <h4 className="font-semibold text-foreground mb-2">{item.title}</h4>
                  <p className="text-sm text-muted-foreground">{item.desc}</p>
                </div>
              ))}
            </div>

            {/* Detailed Policy */}
            <div className="prose prose-slate max-w-none">
              <h2 className="text-2xl font-bold text-foreground mb-6">
                Our Commitment to Privacy
              </h2>
              
              <div className="space-y-8 text-muted-foreground">
                <div>
                  <h3 className="text-lg font-semibold text-foreground mb-3">
                    What We Collect
                  </h3>
                  <p className="leading-relaxed">
                    When you use Kinovek, we collect only what's necessary to provide our service: 
                    your resume content (temporarily for analysis), your email for account management, 
                    and basic usage analytics to improve our platform.
                  </p>
                </div>

                <div>
                  <h3 className="text-lg font-semibold text-foreground mb-3">
                    How We Use Your Data
                  </h3>
                  <p className="leading-relaxed">
                    Your resume is analyzed to provide job matching, ATS scoring, and enhancement 
                    suggestions. We do not use your personal career information for any other purpose. 
                    We never train models on individual user data without explicit consent.
                  </p>
                </div>

                <div>
                  <h3 className="text-lg font-semibold text-foreground mb-3">
                    Data Retention
                  </h3>
                  <p className="leading-relaxed">
                    Uploaded resume files are processed and then deleted automatically. Account 
                    information is retained while your account is active. You can request complete 
                    deletion of all your data at any time.
                  </p>
                </div>

                <div>
                  <h3 className="text-lg font-semibold text-foreground mb-3">
                    No Data Selling
                  </h3>
                  <p className="leading-relaxed">
                    We will never sell, rent, or share your resume data with recruiters, employers, 
                    data brokers, or any third parties. Your career information remains confidential.
                  </p>
                </div>

                <div>
                  <h3 className="text-lg font-semibold text-foreground mb-3">
                    Your Rights
                  </h3>
                  <p className="leading-relaxed">
                    You have the right to access, correct, export, or delete your data at any time. 
                    Contact us at privacy@kinovek.com for any data-related requests.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default Privacy;
