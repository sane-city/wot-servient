package city.sane.wot.thing.observer;

public class SubjectClosedException extends SubjectException {
    public SubjectClosedException() {
        super("Subject is closed");
    }
}
