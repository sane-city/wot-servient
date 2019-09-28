package city.sane.wot.binding.akka;

import city.sane.wot.content.Content;

import java.io.Serializable;

/**
 * This class contains the message types sent between the actors during Thing Interaction.
 */
public class Messages {
    static public class Read implements Serializable {
    }

    static public class RespondRead implements Serializable {
        public final Content content;

        public RespondRead(Content content) {
            this.content = content;
        }
    }

    static public class Write implements Serializable {
        public final Content content;

        public Write(Content content) {
            this.content = content;
        }
    }

    static public class Written implements Serializable {
        public final Content content;

        public Written(Content content) {
            this.content = content;
        }
    }
}
