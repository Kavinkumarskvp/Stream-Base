package kavin.personal_project.streambase.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String code) {
        super("Link not found " + code);
    }
}
