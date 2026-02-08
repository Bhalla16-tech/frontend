import { Upload, FileText, X } from "lucide-react";
import { cn } from "@/lib/utils";

interface FileUploadZoneProps {
  file: File | null;
  isDragging: boolean;
  inputRef: React.RefObject<HTMLInputElement>;
  onDrop: (e: React.DragEvent) => void;
  onDragOver: (e: React.DragEvent) => void;
  onDragLeave: (e: React.DragEvent) => void;
  onClick: () => void;
  onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onClear: () => void;
  acceptedTypes: string[];
  compact?: boolean;
}

export function FileUploadZone({
  file,
  isDragging,
  inputRef,
  onDrop,
  onDragOver,
  onDragLeave,
  onClick,
  onInputChange,
  onClear,
  acceptedTypes,
  compact = false,
}: FileUploadZoneProps) {
  if (file) {
    return (
      <div className={cn(
        "border-2 border-accent/50 bg-accent/5 rounded-xl flex items-center justify-between transition-all",
        compact ? "p-4" : "p-6"
      )}>
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-accent/20 flex items-center justify-center">
            <FileText className="w-5 h-5 text-accent" />
          </div>
          <div>
            <p className="font-medium text-foreground text-sm">{file.name}</p>
            <p className="text-xs text-muted-foreground">
              {(file.size / 1024).toFixed(1)} KB
            </p>
          </div>
        </div>
        <button
          onClick={(e) => {
            e.stopPropagation();
            onClear();
          }}
          className="p-2 hover:bg-destructive/10 rounded-lg transition-colors group"
        >
          <X className="w-4 h-4 text-muted-foreground group-hover:text-destructive" />
        </button>
      </div>
    );
  }

  return (
    <div
      onClick={onClick}
      onDrop={onDrop}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      className={cn(
        "border-2 border-dashed rounded-xl text-center cursor-pointer transition-all",
        isDragging
          ? "border-accent bg-accent/10 scale-[1.02]"
          : "border-border hover:border-accent/50 hover:bg-accent/5",
        compact ? "p-6" : "p-8"
      )}
    >
      <input
        ref={inputRef}
        type="file"
        accept={acceptedTypes.join(',')}
        onChange={onInputChange}
        className="hidden"
      />
      <Upload className={cn(
        "text-muted-foreground mx-auto mb-3 transition-transform",
        isDragging && "scale-110 text-accent",
        compact ? "w-8 h-8 mb-2" : "w-12 h-12 mb-4"
      )} />
      <p className={cn(
        "text-foreground mb-1",
        compact ? "text-sm" : "font-medium"
      )}>
        {isDragging ? "Drop your file here" : "Drop your resume here"}
      </p>
      <p className="text-sm text-muted-foreground">
        or click to browse • {acceptedTypes.join(', ').toUpperCase().replace(/\./g, '')} supported
      </p>
    </div>
  );
}
