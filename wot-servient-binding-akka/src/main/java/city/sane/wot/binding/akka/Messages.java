package city.sane.wot.binding.akka;

import city.sane.wot.content.Content;

import java.io.Serializable;

/**
 * This class contains the message types sent between the actors during Thing Interaction.
 */
public class Messages {
    private Messages() {

    }

    public static class Read implements Serializable {
    }

    public static class RespondRead implements Serializable {
        public final Content content;

        public RespondRead(Content content) {
            this.content = content;
        }
    }

    public static class Write implements Serializable {
        public final Content content;

        public Write(Content content) {
            this.content = content;
        }
    }

    public static class Written implements Serializable {
        public final Content content;

        public Written(Content content) {
            this.content = content;
        }
    }
}
