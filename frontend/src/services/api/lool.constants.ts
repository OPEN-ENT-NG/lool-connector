// LOOL document types
export type LoolDocType = "xlsx" | "pptx" | "docx" | "word" | "powerpoint" | "excel";

// LOOL API endpoints
export const LOOL_ENDPOINTS = {
  CREATE_DOCUMENT: "/lool/document",
  OPEN_DOCUMENT: "/lool/documents",
} as const;

// Map document type IDs to file extensions
export const DOC_TYPE_TO_EXTENSION: Record<string, "docx" | "pptx" | "xlsx"> = {
  word: "docx",
  powerpoint: "pptx",
  excel: "xlsx",
};
