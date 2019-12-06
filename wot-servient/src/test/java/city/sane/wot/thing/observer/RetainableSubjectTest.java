package city.sane.wot.thing.observer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RetainableSubjectTest {
    @Test
    public void subscribe() {
        Subject<String> subject = new RetainableSubject<>();

        subject.next("Beer").join();
        subject.next("is").join();
        subject.next("Awesome").join();

        StringBuilder sb = new StringBuilder();
        subject.subscribe(sb::append);

        assertEquals("BeerisAwesome", sb.toString());

        subject.next("Awesome").join();
        assertEquals("BeerisAwesomeAwesome", sb.toString());

    }
}