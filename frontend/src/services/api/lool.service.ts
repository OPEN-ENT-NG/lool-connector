import { odeServices } from "@edifice.io/client";

export interface ProviderContext {
  providers: Array<{
    connectorId: string;
    connectorName: string;
    docTypes: Array<{
      id: string;
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
    const response = await odeServices.http().get<ProviderContext>(
      "/lool/providers/context"
    );
    return response;
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
   * Build URL to open an existing document
   */
  buildOpenUrl(documentId: string): string {
    return `/lool/documents/${documentId}/open`;
  },
};
