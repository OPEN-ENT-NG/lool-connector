import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { EdificeThemeProvider, Layout } from "@edifice.io/react";
import "@edifice.io/bootstrap/dist/index.css";
import { HomeCreation } from "./components/HomeCreation";
import "./styles/main.css";
import { Providers } from "./providers";
import i18n from "./i18n";
import { I18nextProvider } from "react-i18next";
// React application entry point
const rootElement = document.getElementById("root");

if (rootElement) {
  createRoot(rootElement).render(
     <StrictMode>
    <Providers>
          <I18nextProvider i18n={i18n}>
      <EdificeThemeProvider>
            <Layout whiteBg className="w-100">
            <HomeCreation />
            </Layout>
      </EdificeThemeProvider>
          </I18nextProvider>
    </Providers>
       </StrictMode>
  );
}
