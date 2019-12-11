package city.sane.wot.thing.observer;

class SubjectClosedException extends SubjectException {
    public SubjectClosedException() {
        super("Subject is closed");
    }
}
