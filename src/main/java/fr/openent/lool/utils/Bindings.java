package fr.openent.lool.utils;

public enum Bindings {
    READ("org-entcore-workspace-service-WorkspaceService|getDocument"),
    CONTRIB("org-entcore-workspace-service-WorkspaceService|updateDocument");

    private final String actionName;

    Bindings(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        return this.actionName;
    }
}
