import { Grid, Card, Radio } from "@edifice.io/react";
import { useTranslation } from "react-i18next";
import clsx from "clsx";

interface DocumentTypeCardsProps {
  selectedDocTypeId?: string;
  onCardClick: (docTypeId: string) => void;
  showRadio?: boolean;
}

export const DocumentTypeCards = ({
  selectedDocTypeId,
  onCardClick,
  showRadio = false,
}: DocumentTypeCardsProps) => {
  const { t } = useTranslation();

  const handleChange = (docTypeId: string) => {
    onCardClick(docTypeId);
  };

  return (
    <Grid className="w-100">
      {/* Word document card */}
      <Grid.Col sm="12" md="4" lg="4">
        <Card
          isClickable={true}
          isSelectable={false}
          isSelected={selectedDocTypeId === "word"}
          onClick={() => handleChange("word")}
          className={clsx("h-100", {
            "border-primary": selectedDocTypeId === "word",
          })}
        >
          <Card.Body>
            <div className="d-flex flex-column gap-8 h-100">
              <div className="d-flex align-items-center gap-8 justify-content-between">
                <div className="d-flex align-items-center gap-8">
                  <img src="/lool/public/img/text.svg" alt="Word" />
                  <Card.Title className="mb-0">
                    {t("lool.doc.word.title")}
                  </Card.Title>
                </div>
                {showRadio && (
                  <Radio
                    model={selectedDocTypeId || ""}
                    name="doc-type"
                    value="word"
                    checked={selectedDocTypeId === "word"}
                    onChange={(e) => {
                      e.stopPropagation();
                      handleChange("word");
                    }}
                  />
                )}
              </div>
              <p className="text-muted small mb-0 flex-grow-1">
                {t("lool.doc.word.description")}
              </p>
            </div>
          </Card.Body>
        </Card>
      </Grid.Col>

      {/* PowerPoint document card */}
      <Grid.Col sm="12" md="4" lg="4">
        <Card
          isClickable={true}
          isSelectable={false}
          isSelected={selectedDocTypeId === "powerpoint"}
          onClick={() => handleChange("powerpoint")}
          className={clsx("h-100", {
            "border-primary": selectedDocTypeId === "powerpoint",
          })}
        >
          <Card.Body>
            <div className="d-flex flex-column gap-8 h-100">
              <div className="d-flex align-items-center gap-8 justify-content-between">
                <div className="d-flex align-items-center gap-8">
                  <img src="/lool/public/img/presentation.svg" alt="PowerPoint" />
                  <Card.Title className="mb-0">
                    {t("lool.doc.powerpoint.title")}
                  </Card.Title>
                </div>
                {showRadio && (
                  <Radio
                    model={selectedDocTypeId || ""}
                    name="doc-type"
                    value="powerpoint"
                    checked={selectedDocTypeId === "powerpoint"}
                    onChange={(e) => {
                      e.stopPropagation();
                      handleChange("powerpoint");
                    }}
                  />
                )}
              </div>
              <p className="text-muted small mb-0 flex-grow-1">
                {t("lool.doc.powerpoint.description")}
              </p>
            </div>
          </Card.Body>
        </Card>
      </Grid.Col>

      {/* Excel document card */}
      <Grid.Col sm="12" md="4" lg="4">
        <Card
          isClickable={true}
          isSelectable={false}
          isSelected={selectedDocTypeId === "excel"}
          onClick={() => handleChange("excel")}
          className={clsx("h-100", {
            "border-primary": selectedDocTypeId === "excel",
          })}
        >
          <Card.Body>
            <div className="d-flex flex-column gap-8 h-100">
              <div className="d-flex align-items-center gap-8 justify-content-between">
                <div className="d-flex align-items-center gap-8">
                  <img src="/lool/public/img/sheet.svg" alt="Excel" />
                  <Card.Title className="mb-0">
                    {t("lool.doc.excel.title")}
                  </Card.Title>
                </div>
                {showRadio && (
                  <Radio
                    model={selectedDocTypeId || ""}
                    name="doc-type"
                    value="excel"
                    checked={selectedDocTypeId === "excel"}
                    onChange={(e) => {
                      e.stopPropagation();
                      handleChange("excel");
                    }}
                  />
                )}
              </div>
              <p className="text-muted small mb-0 flex-grow-1">
                {t("lool.doc.excel.description")}
              </p>
            </div>
          </Card.Body>
        </Card>
      </Grid.Col>
    </Grid>
  );
};
