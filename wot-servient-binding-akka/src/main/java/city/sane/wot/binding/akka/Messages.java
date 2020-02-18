package city.sane.wot.binding.akka;

import city.sane.wot.content.Content;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * This class contains the message types sent between the actors during Thing Interaction.
 */
public class Messages {
    private Messages() {

    }

    // https://stackoverflow.com/a/53845446/1074188
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler"})
    public static class Read implements Serializable {
        @Override
        public String toString() {
            return "Read{}";
        }
    }

    public static class RespondRead implements Serializable {
        public final Content content;

        public RespondRead(Content content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "RespondRead{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Write implements Serializable {
        public final Content content;

        public Write(Content content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Write{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Written implements Serializable {
        public final Content content;

        public Written(Content content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Written{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Invoke implements Serializable {
        public final Content content;

        public Invoke(Content content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Invoke{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Invoked implements Serializable {
        public final Content content;

        public Invoked(Content content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Invoked{" +
                    "content=" + content +
                    '}';
        }
    }

    public static class Subscribe implements Serializable {
        @Override
        public String toString() {
            return "Subscribe{}";
        }
    }

    public static class SubscriptionNext implements Serializable {
        public final Content next;

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

    public static class SubscriptionError implements Serializable {
        public final Throwable e;

        public SubscriptionError(Throwable e) {
            this.e = e;
        }

        @Override
        public String toString() {
            return "SubscriptionError{" +
                    "e=" + e +
                    '}';
        }
    }

    public static class SubscriptionComplete implements Serializable {
        @Override
        public String toString() {
            return "SubscriptionComplete{}";
        }
    }
}
