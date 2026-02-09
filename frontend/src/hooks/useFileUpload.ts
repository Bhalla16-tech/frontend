import { useState, useRef, useCallback } from 'react';
import { toast } from 'sonner';

interface UseFileUploadOptions {
  acceptedTypes?: string[];
  maxSizeMB?: number;
}

const MIME_MAP: Record<string, string[]> = {
  '.pdf': ['application/pdf'],
  '.docx': [
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  ],
};

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function useFileUpload(options: UseFileUploadOptions = {}) {
  const { acceptedTypes = ['.pdf', '.docx'], maxSizeMB = 5 } = options;
  const [file, setFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [fileError, setFileError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const validateFile = useCallback((file: File): boolean => {
    setFileError(null);

    // Check extension
    const extension = '.' + (file.name.split('.').pop()?.toLowerCase() || '');
    const isValidExt = acceptedTypes.includes(extension);

    // Check MIME type as secondary validation
    const allowedMimes = acceptedTypes.flatMap(ext => MIME_MAP[ext] || []);
    const isValidMime = allowedMimes.length === 0 || allowedMimes.includes(file.type);

    if (!isValidExt && !isValidMime) {
      const allowed = acceptedTypes.map(t => t.replace('.', '').toUpperCase()).join(' or ');
      const msg = `Invalid file type "${extension}". Only ${allowed} files are allowed.`;
      setFileError(msg);
      toast.error(msg);
      return false;
    }

    // Check size
    const maxSize = maxSizeMB * 1024 * 1024;
    if (file.size > maxSize) {
      const msg = `File is too large (${formatFileSize(file.size)}). Maximum size is ${maxSizeMB}MB.`;
      setFileError(msg);
      toast.error(msg);
      return false;
    }

    // Check not empty
    if (file.size === 0) {
      const msg = 'File is empty. Please upload a valid resume.';
      setFileError(msg);
      toast.error(msg);
      return false;
    }

    return true;
  }, [acceptedTypes, maxSizeMB]);

  const handleFile = useCallback((selectedFile: File) => {
    if (validateFile(selectedFile)) {
      setFile(selectedFile);
      setFileError(null);
      toast.success(`"${selectedFile.name}" (${formatFileSize(selectedFile.size)}) ready`);
    }
  }, [validateFile]);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    
    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile) {
      handleFile(droppedFile);
    }
  }, [handleFile]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  }, []);

  const handleClick = useCallback(() => {
    inputRef.current?.click();
  }, []);

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      handleFile(selectedFile);
    }
    // Reset input so re-selecting the same file triggers onChange
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  }, [handleFile]);

  const clearFile = useCallback(() => {
    setFile(null);
    setFileError(null);
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  }, []);

  return {
    file,
    fileError,
    isDragging,
    inputRef,
    handleDrop,
    handleDragOver,
    handleDragLeave,
    handleClick,
    handleInputChange,
    clearFile,
    acceptedTypes,
    maxSizeMB,
  };
}
