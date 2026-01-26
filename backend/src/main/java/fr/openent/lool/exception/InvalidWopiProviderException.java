package fr.openent.lool.exception;

public class InvalidWopiProviderException extends Exception {
    public InvalidWopiProviderException() {
        super("Unknown Wopi provider");
    }
}
