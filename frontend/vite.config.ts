/// <reference types="vitest/config" />
import react from "@vitejs/plugin-react";
import { resolve } from "node:path";
import { defineConfig, loadEnv, ProxyOptions } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";
import {
  hashEdificeBootstrap,
  queryHashVersion,
} from "./plugins/vite-plugin-edifice";

export default ({ mode }: { mode: string }) => {
  // Checking environment files
  const envFile = loadEnv(mode, process.cwd());
  const envs = { ...process.env, ...envFile };
  const hasEnvFile = Object.keys(envFile).length;

  // Proxy variables
  const headers = hasEnvFile
    ? {
        "set-cookie": [
          `oneSessionId=${envs.VITE_ONE_SESSION_ID}`,
          `XSRF-TOKEN=${envs.VITE_XSRF_TOKEN}`,
        ],
        "Cache-Control": "public, max-age=300",
      }
    : {};

  const proxyObj: ProxyOptions = hasEnvFile
    ? {
        target: envs.VITE_RECETTE,
        changeOrigin: envs.VITE_RECETTE?.includes("localhost") ? false : true,
        headers: {
          cookie: `oneSessionId=${envs.VITE_ONE_SESSION_ID};authenticated=true; XSRF-TOKEN=${envs.VITE_XSRF_TOKEN}`,
        },
        configure: (proxy) => {
          proxy.on("proxyReq", (proxyReq) => {
            proxyReq.setHeader("X-XSRF-TOKEN", envs.VITE_XSRF_TOKEN || "");
          });
        },
      }
    : {
        target: "http://localhost:8090",
        changeOrigin: false,
      };

  return defineConfig({
    base: mode === "production" ? "/lool" : "",
    root: __dirname,
    cacheDir: "./node_modules/.vite/lool-home",    resolve: {
      alias: {
        "@images": resolve(
          __dirname,
          "node_modules/@edifice.io/bootstrap/dist/images",
        ),
      },
    },    
    build: {
      outDir: "dist-home",
      emptyOutDir: true,
      rollupOptions: {
        input: resolve(__dirname, "index.html"),
        output: {
          format: "iife",
          entryFileNames: "home.js",
          assetFileNames: "[name].[ext]",
          inlineDynamicImports: true,
        },
      },
    },

    server: {
      fs: {
        allow: ["../../"],
      },
      proxy: {
        "/lool": proxyObj,
        "/applications-list": proxyObj,
        "/conf/public": proxyObj,
        "^/(?=help-1d|help-2d)": proxyObj,
        "^/(?=assets)": proxyObj,
        "^/(?=theme|locale|i18n|skin)": proxyObj,
        "^/(?=auth|appregistry|archive|cas|userbook|directory|communication|conversation|portal|session|timeline|workspace|infra)":
          proxyObj,
      },
      port: 4200,
      headers,
      host: "localhost",
      cors: true,
    },

    preview: {
      port: 4300,
      headers,
      host: "localhost",
    },

    plugins: [
      react(),
      tsconfigPaths(),
      hashEdificeBootstrap({
        hash: queryHashVersion,
      }),
    ],
  });
};
