package fr.openent.lool.utils;

public enum Headers {
    AUTO_SAVE("X-LOOL-WOPI-IsAutosave"),
    EXIT_SAVE("X-LOOL-WOPI-IsExitSave");

    private final String name;

    Headers(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
