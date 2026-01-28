import {
  AppHeader,
  LoadingScreen,
  Flex,
  Grid,
  Button,
  Card,
} from "@edifice.io/react";
import { IconAdd } from "@edifice.io/react/icons";
import { useTranslation } from "react-i18next";
import { useEffect } from "react";
import clsx from "clsx";
import { useProviderContext } from "../services/queries/lool.query";
import { useHomeStore } from "../store/home.store";

export const HomeCreation = () => {
  const { t } = useTranslation();
  const {
    data: providerContext,
    isLoading,
    error,
  } = useProviderContext();

  const {
    providers,
    selectedProvider,
    selectedDocType,
    setProviders,
    selectProvider,
    selectDocType,
  } = useHomeStore();

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

  if (error) {
    return (
      <Flex
        direction="column"
        align="center"
        justify="center"
        className="min-vh-100"
      >
        <p className="text-danger">{t("lool.error.loading")}</p>
      </Flex>
    );
  }

  const currentProvider = selectedProvider || providers[0];

  // Handle card click to select document type
  const handleCardClick = (docTypeId: string) => {
    const docType = currentProvider?.docTypes.find((dt) => dt.id === docTypeId);
    if (docType) {
      selectDocType(docType);
    }
  };

  return (
    <>
      <AppHeader>
        <h1 className="text-info h3">{t("lool.title")}</h1>
      </AppHeader>

      <Flex
        direction="column"
        align="center"
        justify="center"
        gap="24"
        className="w-100 py-32 px-3 px-md-0 home-creation-container"
      >
          {/* Illustration SVG */}
          <div className="w-100 d-flex flex-column align-items-center">
            <img
              className="home-creation-svg mx-16"
              src="/lool/public/img/waiting.svg"
              alt={t("lool.create.illustration")}
            />
          <hr className="w-100 m-0 d-none d-md-block text-body" />
          </div>
          {/* Information text */}
          <p 
            className="text-center"
            dangerouslySetInnerHTML={{ __html: t("lool.create.info.text") }}
          />

          {/* Document type cards (3 hardcoded cards) */}
          <Grid className="w-100">
          {/* Word document card */}
          <Grid.Col sm="12" md="4" lg="4">
            <Card
              isClickable={false}
              isSelectable={false}
              onClick={() => handleCardClick("word")}
              className={clsx("h-100", {
                "border-primary": selectedDocType?.id === "word",
              })}
            >
              <Card.Body>
                <div className="d-flex flex-column gap-8 h-100">
                  <div className="d-flex align-items-center gap-8">
                    <img
                      src="/lool/public/img/text.svg"
                      alt="Word"
                    />
                    <Card.Title className="mb-0">{t("lool.doc.word.title")}</Card.Title>
                  </div>
                  <p className="text-muted small mb-0">
                    {t("lool.doc.word.description")}
                  </p>
                </div>
              </Card.Body>
            </Card>
          </Grid.Col>

          {/* PowerPoint document card */}
          <Grid.Col sm="12" md="4" lg="4">
            <Card
              isClickable={false}
              isSelectable={false}
              onClick={() => handleCardClick("powerpoint")}
              className={clsx("h-100", {
                "border-primary": selectedDocType?.id === "powerpoint",
              })}
            >
              <Card.Body>
                <div className="d-flex flex-column gap-8 h-100">
                  <div className="d-flex align-items-center gap-8">
                    <img
                      src="/lool/public/img/presentation.svg"
                      alt="PowerPoint"
                    />
                    <Card.Title className="mb-0">{t("lool.doc.powerpoint.title")}</Card.Title>
                  </div>
                  <p className="text-muted small mb-0">
                    {t("lool.doc.powerpoint.description")}
                  </p>
                </div>
              </Card.Body>
            </Card>
          </Grid.Col>

          {/* Excel document card */}
          <Grid.Col sm="12" md="4" lg="4">
            <Card
              isClickable={false}
              isSelectable={false}
              onClick={() => handleCardClick("excel")}
              className={clsx("h-100", {
                "border-primary": selectedDocType?.id === "excel",
              })}
            >
              <Card.Body>
                <div className="d-flex flex-column gap-8 h-100">
                  <div className="d-flex align-items-center gap-8">
                    <img
                      src="/lool/public/img/sheet.svg"
                      alt="Excel"
                    />
                    <Card.Title className="mb-0">{t("lool.doc.excel.title")}</Card.Title>
                  </div>
                  <p className="text-muted small mb-0">
                    {t("lool.doc.excel.description")}
                  </p>
                </div>
              </Card.Body>
            </Card>
          </Grid.Col>
        </Grid>

          {/* Button to create from workspace */}
          <div className="w-100 d-flex justify-content-end">
            <Button
              leftIcon={<IconAdd></IconAdd>}
              onClick={() => {
                window.location.href = "/workspace/workspace#/lool";
              }}
            >
              {t("lool.create.from.workspace")}
            </Button>
          </div>
      </Flex>
    </>
  );
};
