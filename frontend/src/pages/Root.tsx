import { Layout } from "@edifice.io/react";
import { Outlet } from "react-router-dom";

export const Root = () => {
  return (
    <Layout whiteBg className="w-100">
      <Outlet />
    </Layout>
  );
};
