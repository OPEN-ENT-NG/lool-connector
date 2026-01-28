import { RouteObject, createBrowserRouter } from "react-router-dom";
import { Root } from "../pages/Root";
import { HomeCreation } from "../pages/HomeCreation";
import { ModalPage } from "../pages/ModalPage";

const routes: RouteObject[] = [
  {
    path: "/",
    element: <Root />,
    children: [
      {
        index: true,
        element: <HomeCreation />,
      },
    ],
  },
  {
    path: "/modal/create",
    element: <ModalPage />,
  },
  {
    path: "*",
    element: <HomeCreation />,
  },
];

// The base URL for the router, usually set via Vite config
export const basename = import.meta.env.BASE_URL;

/**
 * Creates the browser router instance for the app.
 */
export const router = createBrowserRouter(routes, {
  basename,
});
