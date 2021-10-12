package fr.openent.lool.exception;

public class InvalidWopiServerException extends Exception {
    public InvalidWopiServerException(Throwable e) {
        super("Invalid wopi server", e);
    }
}
