import { odeServices } from "@edifice.io/client";
import { LOOL_ENDPOINTS, DOC_TYPE_TO_EXTENSION, LoolDocType } from "./lool.constants";

// Re-export LoolDocType for convenience
export type { LoolDocType } from "./lool.constants";

// Raw API response interface
interface RawProviderContext {
  provider: string;
  capabilities: Array<{
    "content-type": string;
    extension: string;
  }>;
  templates: string[];
}

export interface ProviderContext {
  providers: Array<{
    connectorId: string;
    connectorName: string;
    docTypes: Array<{
      id: "word" | "excel" | "powerpoint";
      extension: string;
      label: string;
    }>;
  }>;
}

export interface CreateDocumentPayload {
  name: string;
  type: string;
  protected: boolean;
  folderId?: string;
}

export interface CreateDocumentResponse {
  _id: string;
  url: string;
}

export const loolService = {
  /**
   * Fetch provider context and available document types
   */
  async getProviderContext(): Promise<ProviderContext> {
    const response = await odeServices.http().get<RawProviderContext>(
      "/lool/providers/context"
    );
    
    // Transform raw API response to expected format
    const docTypes: Array<{
      id: "word" | "excel" | "powerpoint";
      extension: string;
      label: string;
    }> = [];

    // Create docTypes based on templates
    if (response.templates.includes("docx")) {
      docTypes.push({
        id: "word",
        extension: "docx",
        label: "Word",
      });
    }
    if (response.templates.includes("pptx")) {
      docTypes.push({
        id: "powerpoint",
        extension: "pptx",
        label: "PowerPoint",
      });
    }
    if (response.templates.includes("xlsx")) {
      docTypes.push({
        id: "excel",
        extension: "xlsx",
        label: "Excel",
      });
    }

    return {
      providers: [
        {
          connectorId: response.provider.toLowerCase(),
          connectorName: response.provider,
          docTypes,
        },
      ],
    };
  },

  /**
   * Create a new document
   */
  async createDocument(
    payload: CreateDocumentPayload
  ): Promise<CreateDocumentResponse> {
    const response = await odeServices.http().post<CreateDocumentResponse>(
      "/lool/document",
      payload
    );
    return response;
  },

  /**
   * Build URL to create a new LOOL document
   * @param type Document type (word, powerpoint, excel, docx, pptx, xlsx)
   * @param name Document name (without extension)
   * @param protectedDoc Whether the document should be protected
   * @param folderId Optional folder ID where the document should be created
   * @returns Full LOOL creation URL
   */
  buildCreateUrl(
    type: LoolDocType,
    name: string,
    protectedDoc: boolean = false,
    folderId?: string
  ): string {
    const forbiddenRegex = /[/\\<>|]/g;
    const sanitizedName = name.trim().replace(forbiddenRegex, "");
    
    // Convert type ID to extension if needed
    const extension = DOC_TYPE_TO_EXTENSION[type] || type;
    
    const params = new URLSearchParams({
      type: extension,
      name: sanitizedName,
      protected: protectedDoc.toString(),
    });
    
    if (folderId) {
      params.set('folder', folderId);
    }
    
    return `${LOOL_ENDPOINTS.CREATE_DOCUMENT}?${params.toString()}`;
  },

  /**
   * Build URL to open an existing document
   */
  buildOpenUrl(documentId: string): string {
    return `/lool/documents/${documentId}/open`;
  },
};
