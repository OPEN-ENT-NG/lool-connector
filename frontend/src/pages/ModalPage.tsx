import { useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { LoadingScreen } from "@edifice.io/react";
import { useProviderContext } from "../services/queries/lool.query";
import { useHomeStore } from "../store/home.store";
import { CreateDocumentModal } from "../components/CreateDocumentModal";

export const ModalPage = () => {
  const { data: providerContext, isLoading } = useProviderContext();
  const { setProviders, selectProvider } = useHomeStore();
  const [searchParams] = useSearchParams();
  const folderId = searchParams.get('folderId') || undefined;

  // Set transparent background and notify parent immediately
  useEffect(() => {
    document.documentElement.style.background = "transparent";
    document.body.style.background = "transparent";
    
    // Notify parent that modal is ready (don't wait for API)
    if (window.self !== window.top) {
      window.parent.postMessage({ id: "lool@modal-ready" }, "*");
    }
  }, []);

  // Initialize providers from API
  useEffect(() => {
    if (providerContext?.providers) {
      setProviders(providerContext.providers);
      // Auto-select first provider if there's only one
      if (providerContext.providers.length === 1) {
        selectProvider(providerContext.providers[0]);
      }
    }
  }, [providerContext, setProviders, selectProvider]);

  if (isLoading) {
    return <LoadingScreen />;
  }

  return <CreateDocumentModal isStandalone folderId={folderId} />;
};
