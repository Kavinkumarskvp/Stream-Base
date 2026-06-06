package kavin.personal_project.streambase.exception;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String code) {
        super("Link expired: " + code);
    }
}
