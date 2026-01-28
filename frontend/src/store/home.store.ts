import { create } from "zustand";

interface DocumentType {
  id: "word" | "powerpoint" | "excel";
  extension: string;
  label: string;
}

interface Provider {
  connectorId: string;
  connectorName: string;
  docTypes: DocumentType[];
}

interface HomeState {
  // Providers and document types
  providers: Provider[];
  selectedProvider: Provider | null;
  selectedDocType: DocumentType | null;

  // Creation modal
  isCreateModalOpen: boolean;
  documentName: string;
  isProtected: boolean;
  selectedFolder: string | null;

  // Actions
  setProviders: (providers: Provider[]) => void;
  selectProvider: (provider: Provider) => void;
  selectDocType: (docType: DocumentType) => void;
  openCreateModal: () => void;
  closeCreateModal: () => void;
  setDocumentName: (name: string) => void;
  setIsProtected: (isProtected: boolean) => void;
  setSelectedFolder: (folderId: string | null) => void;
  resetForm: () => void;
}

export const useHomeStore = create<HomeState>((set) => ({
  // Initial state
  providers: [],
  selectedProvider: null,
  selectedDocType: null,
  isCreateModalOpen: false,
  documentName: "",
  isProtected: false,
  selectedFolder: null,

  // Actions
  setProviders: (providers) => set({ providers }),
  
  selectProvider: (provider) =>
    set({ selectedProvider: provider, selectedDocType: null }),
  
  selectDocType: (docType) =>
    set({ selectedDocType: docType, isCreateModalOpen: true }),
  
  openCreateModal: () => set({ isCreateModalOpen: true }),
  
  closeCreateModal: () =>
    set({
      isCreateModalOpen: false,
      documentName: "",
      isProtected: false,
      selectedFolder: null,
    }),
  
  setDocumentName: (name) => set({ documentName: name }),
  
  setIsProtected: (isProtected) => set({ isProtected }),
  
  setSelectedFolder: (folderId) => set({ selectedFolder: folderId }),
  
  resetForm: () =>
    set({
      documentName: "",
      isProtected: false,
      selectedFolder: null,
      selectedDocType: null,
    }),
}));

// Selectors
export const selectProviders = (state: HomeState) => state.providers;
export const selectSelectedProvider = (state: HomeState) => state.selectedProvider;
export const selectSelectedDocType = (state: HomeState) => state.selectedDocType;
export const selectIsCreateModalOpen = (state: HomeState) => state.isCreateModalOpen;
export const selectDocumentName = (state: HomeState) => state.documentName;
export const selectIsProtected = (state: HomeState) => state.isProtected;
export const selectSelectedFolder = (state: HomeState) => state.selectedFolder;
