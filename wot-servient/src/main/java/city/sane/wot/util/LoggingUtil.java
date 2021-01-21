package city.sane.wot.util;

/**
 * Utility class for logging-related operations.
 */
public class LoggingUtil {
    private LoggingUtil() {
        // util class
    }

    /**
     * Cleans <code>obj</code> from line breaks and returns them as \n or \r.
     *
     * @param obj the object to be cleaned
     * @return cleaned string
     */
    public static String sanitizeLogArg(final Object obj) {
        if (obj != null) {
            return obj.toString()
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
        }
        else {
            return null;
        }
    }
}