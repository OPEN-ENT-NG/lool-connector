import { useState } from "react";
import { createPortal } from "react-dom";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
  Modal,
  Button,
  Input,
  FormControl,
  Label,
} from "@edifice.io/react";
import { DocumentTypeCards } from "./DocumentTypeCards";
import { useHomeStore } from "../store/home.store";
import { loolService } from "../services/api/lool.service";

interface CreateDocumentModalProps {
  isStandalone?: boolean;
  folderId?: string;
}

export const CreateDocumentModal = ({ isStandalone = false, folderId }: CreateDocumentModalProps) => {
  const { t } = useTranslation();
  const [filename, setFilename] = useState("");
  const { selectedDocType, selectDocType, providers } = useHomeStore();
  const navigate = useNavigate();

  const handleClose = () => {
    setFilename("");
    if (isStandalone) {
      // Check if we're in an iframe
      if (window.self !== window.top) {
        // Send message to parent to close iframe
        window.parent.postMessage({ id: "lool@close-modal" }, "*");
      } else {
        // Navigate back to home if not in iframe
        navigate("/");
      }
    }
  };

  const handleCreate = () => {
    if (!filename.trim()) {
      return;
    }

    if (!selectedDocType) {
      return;
    }

    // Build the creation URL and open in new window
    const createUrl = loolService.buildCreateUrl(
      selectedDocType.id,
      filename.trim(),
      false, // protectedDoc = false by default
      folderId
    );
    
    window.open(createUrl, "_blank");

    handleClose();
  };

  const handleCardClick = (docTypeId: string) => {
    const currentProvider = providers[0];
    const docType = currentProvider?.docTypes.find((dt) => dt.id === docTypeId);
    if (docType) {
      selectDocType(docType);
    }
  };

  const modalContent = (
    <Modal
      id="create-document-modal"
      isOpen={true}
      onModalClose={handleClose}
      size="lg"
    >
      <Modal.Header onModalClose={handleClose}>
        {t("lool.modal.create.title")}
      </Modal.Header>
      <Modal.Body>
        <div className="d-flex flex-column gap-24 lool-create-modal">
          {/* Description */}
          <p
            dangerouslySetInnerHTML={{
              __html: t("lool.modal.create.description"),
            }}
          />

          {/* Document type cards with radio buttons */}
          <DocumentTypeCards
            selectedDocTypeId={selectedDocType?.id}
            onCardClick={handleCardClick}
            showRadio={true}
          />

          {/* Filename input */}
          <FormControl id="filename" isRequired>
            <Label>{t("lool.modal.filename.label")}</Label>
            <Input
              type="text"
              value={filename}
              onChange={(e) => setFilename(e.target.value)}
              placeholder={t("lool.modal.filename.placeholder")}
              size="md"
            />
          </FormControl>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button
          color="tertiary"
          type="button"
          variant="ghost"
          onClick={handleClose}
        >
          {t("lool.modal.close")}
        </Button>
        <Button
          color="primary"
          type="button"
          onClick={handleCreate}
          disabled={!filename.trim() || !selectedDocType}
        >
          {t("lool.modal.create.button")}
        </Button>
      </Modal.Footer>
    </Modal>
  );

  return isStandalone ? modalContent : createPortal(
    modalContent,
    document.getElementById("portal") as HTMLElement
  );
};
