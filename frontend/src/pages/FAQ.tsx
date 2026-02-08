import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";

const faqs = [
  {
    question: "Is Kinovek's resume format ATS-safe?",
    answer: "Yes. All enhanced resumes use proven ATS-compatible formatting. We avoid tables, graphics, headers/footers, and complex layouts that confuse parsing systems. Your resume will be easily read by all major ATS platforms including Workday, Greenhouse, Lever, and Taleo.",
  },
  {
    question: "Will recruiters be able to tell my resume was enhanced?",
    answer: "No. Our enhancements focus on clarity, formatting, and keyword optimization—not fabricating content. The result reads naturally and professionally. We improve how your real experience is presented, not what it says.",
  },
  {
    question: "Is my resume data secure?",
    answer: "Absolutely. Your data is encrypted in transit and at rest. We never sell or share your resume with third parties. Files are automatically deleted after processing, and we maintain strict data protection policies aligned with GDPR principles.",
  },
  {
    question: "Does Kinovek work for international job applications?",
    answer: "Yes. Our analysis works for job applications globally. The keyword matching and ATS optimization principles apply across markets, though specific terminology may vary by region and industry.",
  },
  {
    question: "Which industries does Kinovek support?",
    answer: "Kinovek works across all industries—tech, finance, healthcare, marketing, engineering, retail, and more. Our analysis adapts to the specific terminology and requirements in each job description you provide.",
  },
  {
    question: "What file formats are supported?",
    answer: "We support PDF and DOCX (Microsoft Word) formats. For best results, upload your resume in the format most commonly requested by employers in your field—typically PDF or DOCX.",
  },
  {
    question: "How is the match score calculated?",
    answer: "We analyze keyword presence, skill alignment, experience relevance, and role fit based on the job description you provide. The score reflects how well your current resume addresses what the posting asks for.",
  },
  {
    question: "Can I use Kinovek for multiple job applications?",
    answer: "Yes. In fact, we recommend it. Each job description is unique, so running your resume through our analysis for each application helps ensure you're presenting the most relevant version of your experience.",
  },
];

const FAQ = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        {/* Hero */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide">
            <div className="max-w-3xl mx-auto text-center">
              <h1 className="text-4xl sm:text-5xl font-bold text-foreground mb-5">
                Frequently Asked Questions
              </h1>
              <p className="text-lg text-muted-foreground">
                Everything you need to know about using Kinovek.
              </p>
            </div>
          </div>
        </section>

        {/* FAQ Accordion */}
        <section className="section-padding bg-background">
          <div className="container-wide max-w-3xl">
            <Accordion type="single" collapsible className="space-y-4">
              {faqs.map((faq, index) => (
                <AccordionItem 
                  key={index} 
                  value={`item-${index}`}
                  className="border border-border rounded-xl px-6 data-[state=open]:bg-secondary/30"
                >
                  <AccordionTrigger className="text-left font-semibold text-foreground hover:no-underline py-5">
                    {faq.question}
                  </AccordionTrigger>
                  <AccordionContent className="text-muted-foreground pb-5 leading-relaxed">
                    {faq.answer}
                  </AccordionContent>
                </AccordionItem>
              ))}
            </Accordion>
          </div>
        </section>

        {/* Contact CTA */}
        <section className="section-padding bg-secondary/30">
          <div className="container-wide max-w-2xl text-center">
            <h2 className="text-2xl font-bold text-foreground mb-4">
              Still have questions?
            </h2>
            <p className="text-muted-foreground mb-6">
              We're here to help. Reach out and we'll get back to you within 24 hours.
            </p>
            <a 
              href="mailto:support@kinovek.com" 
              className="text-accent hover:text-accent/80 font-medium"
            >
              support@kinovek.com
            </a>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default FAQ;
