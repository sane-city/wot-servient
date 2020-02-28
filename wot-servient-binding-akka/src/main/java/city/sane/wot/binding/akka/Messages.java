package city.sane.wot.binding.akka;

import city.sane.wot.content.Content;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * This class contains the message types sent between the actors during Thing Interaction.
 */
@SuppressWarnings("squid:S1192")
public class Messages {
    private Messages() {
        // factory class
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public static class Read {
        public Read() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "Read{}";
        }
    }

    public static class RespondRead {
        public final Content content;

        public RespondRead(Content content) {
            this.content = content;
        }

        RespondRead() {
            // required by jackson
            content = null;
        }

        @Override
        public String toString() {
            return "RespondRead{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class RespondReadFailed {
        public final Throwable e;

        public RespondReadFailed(Throwable e) {
            this.e = e;
        }

        RespondReadFailed() {
            // required by jackson
            e = null;
        }

        @Override
        public String toString() {
            return "RespondReadFailed{" +
                    "e=" + e +
                    '}';
        }
    }

    public static class Write {
        public final Content content;

        public Write(Content content) {
            this.content = content;
        }

        Write() {
            // required by jackson
            content = null;
        }

        @Override
        public String toString() {
            return "Write{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Written {
        public final Content content;

        public Written(Content content) {
            this.content = content;
        }

        Written() {
            // required by jackson
            content = null;
        }

        @Override
        public String toString() {
            return "Written{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Invoke {
        public final Content content;

        public Invoke(Content content) {
            this.content = content;
        }

        Invoke() {
            // required by jackson
            content = null;
        }

        @Override
        public String toString() {
            return "Invoke{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Invoked {
        public final Content content;

        public Invoked(Content content) {
            this.content = content;
        }

        Invoked() {
            // required by jackson
            content = null;
        }

        @Override
        public String toString() {
            return "Invoked{" +
                    "content=" + content +
                    '}';
        }
    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    public static class Subscribe {
        public Subscribe() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "Subscribe{}";
        }
    }

    public static class SubscriptionNext {
        public final Content next;

        SubscriptionNext() {
            // required by jackson
            next = null;
        }

        public SubscriptionNext(Content next) {
            this.next = next;
        }

        @Override
        public String toString() {
            return "SubscriptionNext{" +
                    "next=" + next +
                    '}';
        }
    }

    public static class SubscriptionError {
        public final Throwable e;

        public SubscriptionError(Throwable e) {
            this.e = e;
        }

        SubscriptionError() {
            // required by jackson
            e = null;
        }

        @Override
        public String toString() {
            return "SubscriptionError{" +
                    "e=" + e +
                    '}';
        }
    }

    public static class SubscriptionComplete {
        public SubscriptionComplete() {
            // required by jackson
        }

        @Override
        public String toString() {
            return "SubscriptionComplete{}";
        }
    }
}
