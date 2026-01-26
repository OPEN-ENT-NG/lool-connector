import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import HttpBackend from "i18next-http-backend";

i18n
  .use(HttpBackend)
  .use(initReactI18next)
  .init({
    lng: "fr",
    fallbackLng: "fr",
    debug: false,
    interpolation: {
      escapeValue: false,
    },
    backend: {
      loadPath: "/lool/i18n",
      parse: (data: string) => {
        try {
          return JSON.parse(data);
        } catch (e) {
          console.error("Error parsing i18n data:", e);
          return {};
        }
      },
    },
  });

export default i18n;
