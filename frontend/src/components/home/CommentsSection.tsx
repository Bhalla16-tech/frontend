import { useState } from "react";
import { Heart, Send } from "lucide-react";
import { Button } from "@/components/ui/button";

interface Comment {
  id: number;
  name: string;
  text: string;
  likes: number;
  isLiked: boolean;
}

const initialComments: Comment[] = [
  { id: 1, name: "Sarah M.", text: "This platform helped me land my dream job! Highly recommend! 🎉", likes: 47, isLiked: false },
  { id: 2, name: "James R.", text: "ATS score checker is a game changer. Got 3 interview calls in one week!", likes: 89, isLiked: false },
  { id: 3, name: "Emily C.", text: "Finally a tool that actually works and is completely free.", likes: 124, isLiked: false },
  { id: 4, name: "Priya S.", text: "Used Job Match before applying - got the offer! Thank you Kinovek! ❤️", likes: 203, isLiked: false },
];

export function CommentsSection() {
  const [comments, setComments] = useState<Comment[]>(initialComments);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [text, setText] = useState("");

  const handleLike = (commentId: number) => {
    setComments(comments.map(comment => {
      if (comment.id === commentId) {
        return {
          ...comment,
          likes: comment.isLiked ? comment.likes - 1 : comment.likes + 1,
          isLiked: !comment.isLiked,
        };
      }
      return comment;
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !text.trim()) return;
    
    const comment: Comment = {
      id: Date.now(),
      name: name.trim(),
      text: text.trim(),
      likes: 0,
      isLiked: false,
    };
    
    setComments([comment, ...comments]);
    setName("");
    setEmail("");
    setText("");
  };

  return (
    <section className="py-20 md:py-24 bg-background">
      <div className="container-wide">
        {/* Header */}
        <div className="text-center mb-10">
          <h2 className="text-3xl sm:text-4xl font-light text-foreground mb-2">
            Community <span className="text-primary font-semibold">Feedback</span>
          </h2>
          <p className="text-muted-foreground text-sm">
            Share your experience with Kinovek
          </p>
        </div>

        {/* Form */}
        <div className="max-w-3xl mx-auto mb-10">
          <form onSubmit={handleSubmit} className="p-5 rounded-xl border border-border/60 bg-card/30">
            <div className="flex flex-col sm:flex-row gap-3 mb-3">
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your name *"
                className="flex-1 px-4 py-3 text-sm bg-transparent rounded-lg border border-border/60 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
                required
              />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Email (optional)"
                className="flex-1 px-4 py-3 text-sm bg-transparent rounded-lg border border-border/60 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
            </div>
            <div className="flex gap-3">
              <input
                type="text"
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Write your feedback..."
                className="flex-1 px-4 py-3 text-sm bg-transparent rounded-lg border border-border/60 text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
                required
              />
              <Button 
                type="submit" 
                size="icon"
                className="w-10 h-10 rounded-lg bg-primary hover:bg-primary/90"
                disabled={!name.trim() || !text.trim()}
              >
                <Send className="w-4 h-4 text-primary-foreground" />
              </Button>
            </div>
          </form>
        </div>

        {/* Comments Grid - 2x2 layout */}
        <div className="max-w-3xl mx-auto grid grid-cols-1 sm:grid-cols-2 gap-4">
          {comments.slice(0, 4).map((comment) => (
            <div
              key={comment.id}
              className="p-4 rounded-xl border border-border/60 bg-card/30 hover:border-border transition-colors duration-300"
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <span className="font-semibold text-primary text-sm">{comment.name}</span>
                <button
                  onClick={() => handleLike(comment.id)}
                  className={`flex items-center gap-1.5 text-sm transition-all duration-200 ${
                    comment.isLiked 
                      ? "text-red-500" 
                      : "text-muted-foreground hover:text-muted-foreground/80"
                  }`}
                >
                  <Heart className={`w-4 h-4 ${comment.isLiked ? "fill-current" : ""}`} />
                  <span>{comment.likes}</span>
                </button>
              </div>
              <p className="text-foreground/90 text-sm leading-relaxed">{comment.text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
